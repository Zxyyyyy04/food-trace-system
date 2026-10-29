package com.trace.controller;

import com.trace.common.Result;
import com.trace.entity.FoodInfo;
import com.trace.entity.TraceRecord;
import com.trace.mapper.FoodMapper;
import com.trace.service.FoodTraceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trace")
@Api(tags = "食品溯源核心业务")
public class FoodTraceController {

    @Autowired
    private FoodTraceService traceService;

    @Autowired
    private FoodMapper foodMapper;

    @PostMapping("/initRoles")
    @ApiOperation("0. 初始化角色 (供应商必调一次，发执照)")
    public Result<String> initRoles(
            @ApiParam(value = "中间商地址", required = true) @RequestParam String xssAddress,
            @ApiParam(value = "销售商地址", required = true) @RequestParam String zjsAddress) {
        try {
            traceService.initRoles(xssAddress, zjsAddress);
            return Result.success("角色分配成功！中间商与销售商已获得链上操作权限。");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/create")
    @ApiOperation("1. 生产食品 (仅供应商)")
    public Result<String> createFood(
            @ApiParam(value = "溯源码", required = true) @RequestParam String foodId,
            @ApiParam(value = "食品名称", required = true) @RequestParam String foodName,
            @ApiParam(value = "原产地", required = true) @RequestParam String origin) {
        try {
            traceService.createFood(foodId, foodName, origin);
            return Result.success("生产成功并上链");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/transfer/distributor")
    @ApiOperation("2. 供应商发货给中间商")
    public Result<String> transferToDistributor(
            @ApiParam(value = "溯源码", required = true) @RequestParam String foodId,
            @ApiParam(value = "中间商地址", required = true) @RequestParam String distributorAddress,
            @ApiParam(value = "发货地点", required = true) @RequestParam String location) {
        try {
            traceService.transferToDistributor(foodId, distributorAddress, location);
            return Result.success("发货成功并上链");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/transfer/retailer")
    @ApiOperation("3. 中间商送达给销售商")
    public Result<String> transferToRetailer(
            @ApiParam(value = "溯源码", required = true) @RequestParam String foodId,
            @ApiParam(value = "销售商地址", required = true) @RequestParam String retailerAddress,
            @ApiParam(value = "送达地点", required = true) @RequestParam String location) {
        try {
            traceService.transferToRetailer(foodId, retailerAddress, location);
            return Result.success("送达成功并上链");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/onsale")
    @ApiOperation("4. 销售商上架")
    public Result<String> putOnSale(
            @ApiParam(value = "溯源码", required = true) @RequestParam String foodId,
            @ApiParam(value = "上架地点", required = true) @RequestParam String location) {
        try {
            traceService.putOnSale(foodId, location);
            return Result.success("上架成功并上链");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/detail/{foodId}")
    @ApiOperation("查询单条食品详情")
    public Result<FoodInfo> getDetail(@ApiParam(value = "溯源码") @PathVariable String foodId) {
        FoodInfo info = foodMapper.selectByFoodId(foodId);
        return info != null ? Result.success(info) : Result.error("未找到该食品");
    }

    @GetMapping("/history/{foodId}")
    @ApiOperation("查询溯源历史列表")
    public Result<List<TraceRecord>> getHistory(@ApiParam(value = "溯源码") @PathVariable String foodId) {
        List<TraceRecord> list = foodMapper.selectHistoryByFoodId(foodId);
        return Result.success(list);
    }

    @GetMapping("/list")
    @ApiOperation("查询所有食品列表")
    public Result<List<FoodInfo>> list() {
        return Result.success(foodMapper.selectAllFoods());
    }
}