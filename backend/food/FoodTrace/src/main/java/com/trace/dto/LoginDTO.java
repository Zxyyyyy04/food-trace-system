package com.trace.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 登录请求传输对象
 */
@Data
@ApiModel("登录请求参数")
public class LoginDTO {
    
    @ApiModelProperty(value = "Webase测试用户名称", example = "供应商张三")
    private String name;
    
    @ApiModelProperty(value = "Webase测试用户地址", example = "0x1111111111111111111111111111111111111111")
    private String address;
    
    @ApiModelProperty(value = "角色 (1:供应商, 2:中间商, 3:销售商)", example = "1")
    private Integer role;
}