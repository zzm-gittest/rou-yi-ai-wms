package com.ruoyi.wms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BaseDO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI会话日志表 wms_ai_chat_log
 *
 * @author ruoyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_ai_chat_log")
@ApiModel(value = "WmsAiChatLog", description = "AI会话日志表")
public class WmsAiChatLog extends BaseDO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "会话ID")
    private String conversationId;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "操作类型")
    private String operationType;

    @ApiModelProperty(value = "输入类型(voice/text)")
    private String inputType;

    @ApiModelProperty(value = "输入内容")
    private String inputContent;

    @ApiModelProperty(value = "语音文件路径")
    private String voiceFilePath;

    @ApiModelProperty(value = "响应内容")
    private String responseContent;

    @ApiModelProperty(value = "处理状态(success/failed)")
    private String resultStatus;

    @ApiModelProperty(value = "错误信息")
    private String errorMessage;

    @ApiModelProperty(value = "处理耗时(毫秒)")
    private Integer processTime;
}
