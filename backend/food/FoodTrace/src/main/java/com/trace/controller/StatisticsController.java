package com.trace.controller;

import com.trace.common.Result;
import com.trace.mapper.FoodMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private FoodMapper foodMapper;

    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalFoods", foodMapper.countTotalFoods());
        data.put("totalRecords", foodMapper.countTotalRecords());
        data.put("onSaleFoods", foodMapper.countOnSaleFoods());
        return Result.success(data);
    }

    @GetMapping("/status")
    public Result<List<Map<String, Object>>> getStatus() {
        return Result.success(foodMapper.countByStatus());
    }

    @GetMapping("/role")
    public Result<List<Map<String, Object>>> getRole() {
        return Result.success(foodMapper.countByRole());
    }

    @GetMapping("/latest")
    public Result<List<Map<String, Object>>> getLatest() {
        return Result.success(foodMapper.getLatestRecords());
    }
}