package com.ruoyi.wms.domain.vo.integration;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("统一查单结果")
public class BillQueryResultVO {

    @ApiModelProperty("摘要说明")
    private String summary;

    @ApiModelProperty("匹配总数（截断前）")
    private int totalMatched;

    @ApiModelProperty("实际返回行数")
    private int returned;

    @ApiModelProperty("当前页码（分页时）")
    private int pageNo;

    @ApiModelProperty("每页条数（分页时）")
    private int pageSize;

    @ApiModelProperty("总页数（分页时）")
    private int totalPages;

    @ApiModelProperty("结果行")
    private List<BillQueryRowVO> rows = new ArrayList<>();

    @ApiModelProperty("推荐查询模板（与 PC 统一查询入口一致）")
    private List<String> templates = new ArrayList<>();
}
