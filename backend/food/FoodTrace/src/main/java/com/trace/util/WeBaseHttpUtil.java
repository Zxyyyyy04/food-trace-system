package com.trace.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class WeBaseHttpUtil {

    @Value("${webase.front.url}")
    private String webaseUrl;

    private static final String CONTRACT_ABI = "[{\"inputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"internalType\":\"string\",\"name\":\"foodId\",\"type\":\"string\"},{\"indexed\":false,\"internalType\":\"address\",\"name\":\"supplier\",\"type\":\"address\"}],\"name\":\"FoodCreated\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":true,\"internalType\":\"address\",\"name\":\"account\",\"type\":\"address\"},{\"indexed\":false,\"internalType\":\"enum DataModel.Role\",\"name\":\"role\",\"type\":\"uint8\"}],\"name\":\"RoleAssigned\",\"type\":\"event\"},{\"inputs\":[],\"name\":\"admin\",\"outputs\":[{\"internalType\":\"address\",\"name\":\"\",\"type\":\"address\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"_account\",\"type\":\"address\"},{\"internalType\":\"enum DataModel.Role\",\"name\":\"_role\",\"type\":\"uint8\"}],\"name\":\"assignRole\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"_foodId\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"_name\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"_origin\",\"type\":\"string\"}],\"name\":\"createFood\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"_foodId\",\"type\":\"string\"}],\"name\":\"getFoodDetails\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"name\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"origin\",\"type\":\"string\"},{\"internalType\":\"address\",\"name\":\"currentOwner\",\"type\":\"address\"},{\"internalType\":\"uint8\",\"name\":\"status\",\"type\":\"uint8\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"_foodId\",\"type\":\"string\"}],\"name\":\"getFoodHistory\",\"outputs\":[{\"internalType\":\"address[]\",\"name\":\"operators\",\"type\":\"address[]\"},{\"internalType\":\"uint8[]\",\"name\":\"roleList\",\"type\":\"uint8[]\"},{\"internalType\":\"string[]\",\"name\":\"actions\",\"type\":\"string[]\"},{\"internalType\":\"string[]\",\"name\":\"locations\",\"type\":\"string[]\"},{\"internalType\":\"uint256[]\",\"name\":\"timestamps\",\"type\":\"uint256[]\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"getFoodIds\",\"outputs\":[{\"internalType\":\"string[]\",\"name\":\"\",\"type\":\"string[]\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"_foodId\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"_location\",\"type\":\"string\"}],\"name\":\"putOnSale\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"\",\"type\":\"address\"}],\"name\":\"roles\",\"outputs\":[{\"internalType\":\"enum DataModel.Role\",\"name\":\"\",\"type\":\"uint8\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"_foodId\",\"type\":\"string\"},{\"internalType\":\"address\",\"name\":\"_distributor\",\"type\":\"address\"},{\"internalType\":\"string\",\"name\":\"_location\",\"type\":\"string\"}],\"name\":\"transferToDistributor\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"_foodId\",\"type\":\"string\"},{\"internalType\":\"address\",\"name\":\"_retailer\",\"type\":\"address\"},{\"internalType\":\"string\",\"name\":\"_location\",\"type\":\"string\"}],\"name\":\"transferToRetailer\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
    private final RestTemplate restTemplate = new RestTemplate();

    public String sendTransaction(String contractAddress, String funcName, List<Object> funcParam, String userAddress) {
        // 按照官方文档和网页真实请求，使用 trans/handle 接口
        String url = webaseUrl + "trans/handle";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("groupId", "1");
        requestMap.put("user", userAddress); // 传 0x 地址
        requestMap.put("contractName", "FoodTraceability");
        requestMap.put("contractAddress", contractAddress);
        requestMap.put("funcName", funcName);
        requestMap.put("funcParam", funcParam); // 这里正常传参数
        requestMap.put("useCns", false);
        requestMap.put("cnsName", "");
        requestMap.put("useAes", false);
        requestMap.put("version", "");
        requestMap.put("contractPath", "food"); // 网页端有，直接照抄

        // 【核心修复】按照文档要求，传标准 ABI 的 JSONArray 格式
        requestMap.put("contractAbi", JSON.parseArray(CONTRACT_ABI));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestMap, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("调用 WeBASE-Front 交易接口失败: " + e.getMessage());
        }
    }




    public JSONArray getWeBaseUserList() {
        // 路径不变，或者有些版本不要 ?groupId=1 也可以
        String url = webaseUrl + "privateKey/localUserList";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 构造 POST 请求的 JSON Body
        Map<String, Object> requestBody = new java.util.HashMap<>();
        requestBody.put("groupId", 1);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // 【核心修改点】把 HttpMethod.GET 改成 HttpMethod.POST
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            JSONObject resJson = JSON.parseObject(response.getBody());

            if (resJson != null && "0".equals(resJson.getString("code"))) {
                return resJson.getJSONArray("data");
            } else {
                throw new RuntimeException("获取 WeBASE 用户列表失败: " + response.getBody());
            }
        } catch (Exception e) {
            throw new RuntimeException("HTTP 请求 WeBASE 用户列表失败: " + e.getMessage());
        }
    }
}