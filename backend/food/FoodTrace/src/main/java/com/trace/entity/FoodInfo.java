package com.trace.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

/**
 * 食品基础信息实体类
 */
@Data
@ApiModel("食品基础信息")
public class FoodInfo {
    
    @ApiModelProperty("数据库主键")
    private Long id;
    
    @ApiModelProperty("食品唯一溯源码 (如 APPLE_001)")
    private String foodId;
    
    @ApiModelProperty("食品名称")
    private String foodName;
    
    @ApiModelProperty("原产地")
    private String origin;
    
    @ApiModelProperty("当前所有者区块链地址")
    private String currentOwner;
    
    @ApiModelProperty("状态: 0-已生产, 1-运输中, 2-已送达, 3-销售中")
    private Integer status;
    
    @ApiModelProperty("创建时间")
    private Date createTime;
    
    @ApiModelProperty("更新时间")
    private Date updateTime;
}