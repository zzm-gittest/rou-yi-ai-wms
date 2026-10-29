package com.ruoyi.common.core.domain.entity;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * AI点数流水表 ai_points_log
 *
 * @author ruoyi
 */
public class AiPointsLog
{
    /** 主键 */
    private Long id;

    /** 租户ID */
    private Long tenantId;

    /** 变动类型: charge充值/consume消耗/refund退还/gift赠送 */
    private String changeType;

    /** 变动点数（正数增加，负数减少） */
    private Integer changeAmount;

    /** 变动后余额 */
    private Integer balanceAfter;

    /** 来源: text/voice/ocr_image/recharge/gift/rollback */
    private String source;

    /** 关联业务ID（会话ID/充值单号） */
    private String bizId;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getTenantId()
    {
        return tenantId;
    }

    public void setTenantId(Long tenantId)
    {
        this.tenantId = tenantId;
    }

    public String getChangeType()
    {
        return changeType;
    }

    public void setChangeType(String changeType)
    {
        this.changeType = changeType;
    }

    public Integer getChangeAmount()
    {
        return changeAmount;
    }

    public void setChangeAmount(Integer changeAmount)
    {
        this.changeAmount = changeAmount;
    }

    public Integer getBalanceAfter()
    {
        return balanceAfter;
    }

    public void setBalanceAfter(Integer balanceAfter)
    {
        this.balanceAfter = balanceAfter;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public String getBizId()
    {
        return bizId;
    }

    public void setBizId(String bizId)
    {
        this.bizId = bizId;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
}
