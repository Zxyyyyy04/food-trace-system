package com.trace.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.trace.common.UserContext;
import com.trace.dto.LoginDTO;
import com.trace.entity.FoodInfo;
import com.trace.entity.TraceRecord;
import com.trace.mapper.FoodMapper;
import com.trace.util.WeBaseHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
public class FoodTraceService {

    @Autowired
    private FoodMapper foodMapper;

    @Autowired
    private WeBaseHttpUtil weBaseHttpUtil;

    // ⚠️ 必须替换为你真实的合约地址
    private static final String CONTRACT_ADDRESS = "0xf3e400bb2b3c752fdc1601a6ca35037b8b090d41";

    /**
     * 0. 初始化角色
     */
    @Transactional(rollbackFor = Exception.class)
    public void initRoles(String xssAddress, String zjsAddress) {
        LoginDTO user = UserContext.getUser();
        if (user.getRole() != 1) throw new RuntimeException("只有管理员(供应商)可以分配角色");

        String res1 = weBaseHttpUtil.sendTransaction(CONTRACT_ADDRESS, "assignRole",
                Arrays.asList(xssAddress, 2), user.getName());
        checkChainResult(res1);

        String res2 = weBaseHttpUtil.sendTransaction(CONTRACT_ADDRESS, "assignRole",
                Arrays.asList(zjsAddress, 3), user.getName());
        checkChainResult(res2);
    }

    /**
     * 1. 供应商生产食品
     */
    @Transactional(rollbackFor = Exception.class)
    public void createFood(String foodId, String foodName, String origin) {
        LoginDTO user = UserContext.getUser();
        if (user.getRole() != 1) throw new RuntimeException("权限不足: 只有供应商可以生产");

        String chainRes = weBaseHttpUtil.sendTransaction(CONTRACT_ADDRESS, "createFood",
                Arrays.asList(foodId, foodName, origin), user.getAddress());

        // 提取 TxHash
        String txHash = checkChainResult(chainRes);

        FoodInfo food = new FoodInfo();
        food.setFoodId(foodId);
        food.setFoodName(foodName);
        food.setOrigin(origin);
        food.setCurrentOwner(user.getAddress());
        food.setStatus(0);
        foodMapper.insertFood(food);

        saveTraceRecord(foodId, user.getAddress(), 1, "Production", origin, "食品生产完成", txHash);
    }

    /**
     * 2. 供应商发货给中间商
     */
    @Transactional(rollbackFor = Exception.class)
    public void transferToDistributor(String foodId, String distributorAddress, String location) {
        LoginDTO user = UserContext.getUser();
        if (user.getRole() != 1) throw new RuntimeException("权限不足: 只有供应商可以发货");

        // 🌟 后端格式校验拦截
        if (!distributorAddress.matches("^0x[a-fA-F0-9]{40}$")) {
            throw new RuntimeException("接收方地址格式不合法，必须为 0x 开头的 42 位字符串！");
        }

        FoodInfo food = foodMapper.selectByFoodId(foodId);
        if (food == null) throw new RuntimeException("食品不存在");
        if (food.getStatus() != 0) throw new RuntimeException("状态错误：食品必须为已生产状态");

        String chainRes = weBaseHttpUtil.sendTransaction(CONTRACT_ADDRESS, "transferToDistributor",
                Arrays.asList(foodId, distributorAddress, location), user.getAddress());

        String txHash = checkChainResult(chainRes);

        foodMapper.updateOwnerAndStatus(foodId, distributorAddress, 1);
        saveTraceRecord(foodId, user.getAddress(), 1, "Shipped to Distributor", location, "发货至中间商", txHash);
    }

    /**
     * 3. 中间商送达给销售商
     */
    @Transactional(rollbackFor = Exception.class)
    public void transferToRetailer(String foodId, String retailerAddress, String location) {
        LoginDTO user = UserContext.getUser();
        if (user.getRole() != 2) throw new RuntimeException("权限不足: 只有中间商可以送达");

        // 🌟 后端格式校验拦截
        if (!retailerAddress.matches("^0x[a-fA-F0-9]{40}$")) {
            throw new RuntimeException("接收方地址格式不合法，必须为 0x 开头的 42 位字符串！");
        }

        FoodInfo food = foodMapper.selectByFoodId(foodId);
        if (food == null) throw new RuntimeException("食品不存在");
        if (food.getStatus() != 1) throw new RuntimeException("状态错误：食品必须为运输中状态");

        String chainRes = weBaseHttpUtil.sendTransaction(CONTRACT_ADDRESS, "transferToRetailer",
                Arrays.asList(foodId, retailerAddress, location), user.getAddress());

        String txHash = checkChainResult(chainRes);

        foodMapper.updateOwnerAndStatus(foodId, retailerAddress, 2);
        saveTraceRecord(foodId, user.getAddress(), 2, "Delivered to Retailer", location, "送达销售商", txHash);
    }

    /**
     * 4. 销售商上架
     */
    @Transactional(rollbackFor = Exception.class)
    public void putOnSale(String foodId, String location) {
        LoginDTO user = UserContext.getUser();
        if (user.getRole() != 3) throw new RuntimeException("权限不足: 只有销售商可以上架");

        FoodInfo food = foodMapper.selectByFoodId(foodId);
        if (food == null) throw new RuntimeException("食品不存在");
        if (food.getStatus() != 2) throw new RuntimeException("状态错误：食品必须为已送达状态");

        String chainRes = weBaseHttpUtil.sendTransaction(CONTRACT_ADDRESS, "putOnSale",
                Arrays.asList(foodId, location), user.getAddress());

        String txHash = checkChainResult(chainRes);

        foodMapper.updateOwnerAndStatus(foodId, user.getAddress(), 3);
        saveTraceRecord(foodId, user.getAddress(), 3, "Put on Sale", location, "上架销售", txHash);
    }

    /**
     * 🌟 核心检查与报错翻译
     */
    private String checkChainResult(String res) {
        try {
            JSONObject json = JSON.parseObject(res);

            if (json.containsKey("code") && !"0".equals(json.getString("code"))) {
                throw new RuntimeException("WeBASE调用失败: " + json.getString("errorMessage"));
            }

            if (json.containsKey("statusOK")) {
                boolean isSuccess = json.getBooleanValue("statusOK");
                if (!isSuccess) {
                    String errorMsg = json.getString("message");
                    // 🌟 完美的报错翻译系统
                    if ("Food ID already exists".equals(errorMsg)) {
                        throw new RuntimeException("生产失败：该溯源码已被使用！");
                    } else if ("Permission denied: Invalid role".equals(errorMsg)) {
                        throw new RuntimeException("操作失败：您的链上角色无权进行此操作！");
                    } else if ("Target must be a distributor".equals(errorMsg)) {
                        throw new RuntimeException("接收方不是合法的中间商（该地址未分配中间商角色）！");
                    } else if ("Target must be a retailer".equals(errorMsg)) {
                        throw new RuntimeException("接收方不是合法的销售商（该地址未分配销售商角色）！");
                    } else if ("Only current owner can operate".equals(errorMsg)) {
                        throw new RuntimeException("越权操作：您不是该食品的当前所有者！");
                    } else {
                        throw new RuntimeException("区块链执行失败: " + errorMsg);
                    }
                }
            }

            // 提取 TxHash
            String txHash = null;
            if (json.containsKey("transactionHash")) {
                txHash = json.getString("transactionHash");
            } else if (json.containsKey("receipt") && json.getJSONObject("receipt").containsKey("transactionHash")) {
                txHash = json.getJSONObject("receipt").getString("transactionHash");
            }
            return txHash;

        } catch (Exception e) {
            if (e instanceof RuntimeException) throw e;
            throw new RuntimeException("解析区块链响应失败: " + e.getMessage());
        }
    }

    /**
     * 保存溯源记录 (带上 txHash)
     */
    private void saveTraceRecord(String foodId, String operator, Integer role, String action, String location, String remark, String txHash) {
        TraceRecord record = new TraceRecord();
        record.setFoodId(foodId);
        record.setOperator(operator);
        record.setRoleType(role);
        record.setAction(action);
        record.setLocation(location);
        record.setRemark(remark);
        record.setTxTime(System.currentTimeMillis());
        record.setTxHash(txHash);
        foodMapper.insertTraceRecord(record);
    }
}