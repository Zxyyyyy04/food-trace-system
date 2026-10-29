package com.trace.mapper;

import com.trace.entity.SysOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统操作日志的数据库访问接口
 */
@Mapper
public interface SysLogMapper {
    void insertLog(SysOperationLog log);
}