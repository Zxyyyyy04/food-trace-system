package com.trace.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

/**
 * 系统操作日志实体类 (由 AOP 自动记录)
 */
@Data
@ApiModel("系统操作审计日志")
public class SysOperationLog {
    
    @ApiModelProperty("数据库主键")
    private Long id;
    
    @ApiModelProperty("操作人名称")
    private String operatorName;
    
    @ApiModelProperty("操作人区块链地址")
    private String operatorAddress;
    
    @ApiModelProperty("请求URL")
    private String requestUrl;
    
    @ApiModelProperty("请求方式 (GET/POST)")
    private String requestMethod;
    
    @ApiModelProperty("请求参数(JSON)")
    private String requestParams;
    
    @ApiModelProperty("返回结果(JSON)")
    private String responseResult;
    
    @ApiModelProperty("耗时(毫秒)")
    private Long costTime;
    
    @ApiModelProperty("客户端IP")
    private String clientIp;
    
    @ApiModelProperty("操作时间")
    private Date createTime;
}