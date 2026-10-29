package com.trace.mapper;

import com.trace.entity.FoodInfo;
import com.trace.entity.TraceRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 食品与溯源记录的数据库访问接口
 */
@Mapper
public interface FoodMapper {

    int insertFood(FoodInfo foodInfo);

    int updateOwnerAndStatus(@Param("foodId") String foodId, 
                             @Param("newOwner") String newOwner, 
                             @Param("status") Integer status);

    FoodInfo selectByFoodId(@Param("foodId") String foodId);

    List<FoodInfo> selectAllFoods();

    int insertTraceRecord(TraceRecord record);

    List<TraceRecord> selectHistoryByFoodId(@Param("foodId") String foodId);

    // --- 大屏统计接口 ---
    @Select("SELECT COUNT(*) FROM food_info")
    int countTotalFoods();

    @Select("SELECT COUNT(*) FROM trace_record")
    int countTotalRecords();

    @Select("SELECT COUNT(*) FROM food_info WHERE status = 3")
    int countOnSaleFoods();

    // 按状态分组统计 (返回 status 和 count)
    @Select("SELECT status, COUNT(*) as count FROM food_info GROUP BY status")
    List<Map<String, Object>> countByStatus();

    // 按角色分组统计操作次数 (返回 role_type 和 count)
    @Select("SELECT role_type, COUNT(*) as count FROM trace_record GROUP BY role_type")
    List<Map<String, Object>> countByRole();

    // 查询最新 10 条溯源动态
    @Select("SELECT action, location, operator, tx_time FROM trace_record ORDER BY tx_time DESC LIMIT 10")
    List<Map<String, Object>> getLatestRecords();
}