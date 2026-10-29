package com.trace.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 溯源流转记录实体类
 */
@Data
@ApiModel("溯源流转记录")
public class TraceRecord {
    
    @ApiModelProperty("数据库主键")
    private Long id;
    
    @ApiModelProperty("关联的食品溯源码")
    private String foodId;
    
    @ApiModelProperty("操作人的区块链地址")
    private String operator;
    
    @ApiModelProperty("操作人角色: 1-供应商, 2-中间商, 3-销售商")
    private Integer roleType;
    
    @ApiModelProperty("动作描述 (如: Production, Shipped)")
    private String action;
    
    @ApiModelProperty("操作地点")
    private String location;
    
    @ApiModelProperty("备注信息")
    private String remark;
    
    @ApiModelProperty("交易发生的时间戳")
    private Long txTime;

    @ApiModelProperty("区块链交易哈希")
    private String txHash;
}