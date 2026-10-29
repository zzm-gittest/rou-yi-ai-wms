package com.ruoyi.wms.domain.vo.integration;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * IM/网关统一查单请求
 */
@Data
@ApiModel("统一查单请求")
public class BillQueryRequestVO {

    @ApiModelProperty(value = "用户自然语言或模板句，如：查询近7天待审核销售出库", required = true)
    private String message;

    @ApiModelProperty(value = "返回条数上限（与 pageNo/pageSize 二选一；未分页时默认 5，最大 20）")
    private Integer maxRows;

    @ApiModelProperty(value = "页码，从 1 开始；与 pageSize 同时传入时启用分页")
    private Integer pageNo;

    @ApiModelProperty(value = "每页条数，默认 3，最大 10")
    private Integer pageSize;
}
