package com.ruoyi.common.core.domain.entity;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.tenant.TenantHelper;

/**
 * 租户表 sys_tenant
 *
 * @author ruoyi
 */
public class SysTenant
{
    /** 租户ID */
    @Excel(name = "租户ID", cellType = ColumnType.NUMERIC)
    private Long tenantId;

    /** 租户名称 */
    @Excel(name = "租户名称")
    private String tenantName;

    /** 联系人 */
    @Excel(name = "联系人")
    private String contactUser;

    /** 联系电话 */
    @Excel(name = "联系电话")
    private String contactPhone;

    /** 账号数量 */
    @Excel(name = "账号数量")
    private Integer accountCount;

    /** 过期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "过期时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    /** 租户状态（0正常 1停用） */
    @Excel(name = "租户状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 创建者 */
    private String createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新者 */
    private String updateBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 备注 */
    private String remark;

    /** 版本类型（free免费版/pro专业版/enterprise企业版） */
    @Excel(name = "版本类型", readConverterExp = "free=免费版,pro=专业版,enterprise=企业版")
    private String edition;

    /** AI功能开关（0禁用 1启用） */
    @Excel(name = "AI功能", readConverterExp = "0=禁用,1=启用")
    private Integer aiEnabled;

    /** 语音功能开关（0禁用 1启用） */
    @Excel(name = "语音功能", readConverterExp = "0=禁用,1=启用")
    private Integer voiceEnabled;

    /** OCR功能开关（0禁用 1启用） */
    @Excel(name = "OCR功能", readConverterExp = "0=禁用,1=启用")
    private Integer ocrEnabled;

    /** 扫码功能开关（0禁用 1启用） */
    @Excel(name = "扫码功能", readConverterExp = "0=禁用,1=启用")
    private Integer scanEnabled;

    /** AI点数余额 */
    private Integer aiPoints;

    /** 累计充值点数 */
    private Integer aiPointsTotalCharged;

    /** 累计消耗点数 */
    private Integer aiPointsTotalUsed;

    /** 打印抬头公司名称 */
    private String printCompanyName;

    /** 打印抬头公司地址 */
    private String printCompanyAddress;

    /** 打印抬头联系电话 */
    private String printCompanyPhone;

    /** 打印抬头Logo路径 */
    private String printLogoPath;

    /** 打印字体缩放倍数（0或1=原始大小，2=2倍，0.5=0.5倍） */
    private java.math.BigDecimal printFontScale;

    /** 打印纸张大小（A4=标准A4, SLIP_241X279=三联等份241×279mm, SLIP_241X140=三联等份241×140mm） */
    private String printPageSize;

    /** AI API Key（租户级自定义，空则使用全局配置） */
    private String aiApiKey;

    /** AI 模型名称（租户级自定义，空则使用全局配置） */
    private String aiModel;

    /** AI API 地址（租户级自定义，空则使用全局配置） */
    private String aiApiUrl;

    /** AI 视觉模型名称（截图识别，租户级自定义，空则使用全局配置） */
    private String aiVisionModel;

    /** AI 语音模型名称（语音识别，租户级自定义，空则使用全局配置） */
    private String aiVoiceModel;

    /** 每月文字解析限额 */
    private Integer monthlyQuotaText;

    /** 每月语音识别限额 */
    private Integer monthlyQuotaVoice;

    /** 每年文字解析限额 */
    private Integer annualQuotaText;

    /** 每年语音识别限额 */
    private Integer annualQuotaVoice;

    /** 本月已用文字解析次数 */
    private Integer usedCountText;

    /** 本月已用语音识别次数 */
    private Integer usedCountVoice;

    /** 本年已用文字解析次数 */
    private Integer usedAnnualText;

    /** 本年已用语音识别次数 */
    private Integer usedAnnualVoice;

    /** 月度限额重置时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date quotaResetDate;

    /** 年度限额重置时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date quotaAnnualResetDate;

    /** 订阅类型：monthly月付/yearly年付，NULL表示未订阅 */
    private String subscriptionType;

    /** 订阅到期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date subscriptionExpireTime;

    /** Apple订阅原始交易ID */
    private String appleOriginalTransactionId;

    /** 是否已提交注销申请（0否 1是） */
    private Integer cancelRequested;

    /** 注销申请提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date cancelRequestedAt;

    /** 注销申请原因 */
    private String cancelReason;

    public Long getTenantId()
    {
        return tenantId;
    }

    public void setTenantId(Long tenantId)
    {
        this.tenantId = tenantId;
    }

    public String getTenantName()
    {
        return tenantName;
    }

    public void setTenantName(String tenantName)
    {
        this.tenantName = tenantName;
    }

    public String getContactUser()
    {
        return contactUser;
    }

    public void setContactUser(String contactUser)
    {
        this.contactUser = contactUser;
    }

    public String getContactPhone()
    {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone)
    {
        this.contactPhone = contactPhone;
    }

    public Integer getAccountCount()
    {
        return accountCount;
    }

    public void setAccountCount(Integer accountCount)
    {
        this.accountCount = accountCount;
    }

    public Date getExpireTime()
    {
        return expireTime;
    }

    public void setExpireTime(Date expireTime)
    {
        this.expireTime = expireTime;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getCreateBy()
    {
        return createBy;
    }

    public void setCreateBy(String createBy)
    {
        this.createBy = createBy;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    public String getUpdateBy()
    {
        return updateBy;
    }

    public void setUpdateBy(String updateBy)
    {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime()
    {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime)
    {
        this.updateTime = updateTime;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    /**
     * 判断租户是否正常
     */
    public boolean isNormal()
    {
        return "0".equals(this.status);
    }

    /**
     * 判断租户是否过期
     */
    public boolean isExpired()
    {
        Date now = new Date();

        Date effectiveExpireTime = getEffectiveServiceExpireTime();
        if (effectiveExpireTime != null)
        {
            return !effectiveExpireTime.after(now);
        }

        // 兼容未落expireTime的试用租户，默认注册后7天过期
        if (getCreateTime() != null)
        {
            long trialEnd = getCreateTime().getTime() + 7L * 24 * 60 * 60 * 1000;
            return now.getTime() >= trialEnd;
        }

        return false;
    }

    /**
     * 判断订阅是否有效（管理员有效期/有效订阅/试用期内，满足其一即可）
     */
    public boolean isSubscriptionValid()
    {
        Date now = new Date();

        Date effectiveExpireTime = getEffectiveServiceExpireTime();
        if (effectiveExpireTime != null && effectiveExpireTime.after(now))
        {
            return true;
        }

        // 未订阅，检查试用期（注册后7天）
        if (getCreateTime() != null)
        {
            long trialEnd = getCreateTime().getTime() + 7L * 24 * 60 * 60 * 1000;
            if (now.getTime() < trialEnd)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断租户是否处于到期受限状态
     */
    @JsonIgnore
    public boolean isAccessRestricted()
    {
        return isExpired() || (!TenantHelper.isDefaultTenant(getTenantId()) && !isSubscriptionValid());
    }

    /**
     * 获取当前生效的服务到期时间。
     * 当后台人工设置了更晚的 expireTime 时，应覆盖已过期的历史订阅时间，避免安卓人工续期失效。
     */
    @JsonIgnore
    public Date getEffectiveServiceExpireTime()
    {
        if (expireTime == null)
        {
            return subscriptionExpireTime;
        }
        if (subscriptionExpireTime == null)
        {
            return expireTime;
        }
        return expireTime.after(subscriptionExpireTime) ? expireTime : subscriptionExpireTime;
    }

    public Integer getCancelRequested()
    {
        return cancelRequested;
    }

    public void setCancelRequested(Integer cancelRequested)
    {
        this.cancelRequested = cancelRequested;
    }

    public Date getCancelRequestedAt()
    {
        return cancelRequestedAt;
    }

    public void setCancelRequestedAt(Date cancelRequestedAt)
    {
        this.cancelRequestedAt = cancelRequestedAt;
    }

    public String getCancelReason()
    {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason)
    {
        this.cancelReason = cancelReason;
    }

    public String getEdition()
    {
        return edition;
    }

    public void setEdition(String edition)
    {
        this.edition = edition;
    }

    public Integer getAiEnabled()
    {
        return aiEnabled;
    }

    public void setAiEnabled(Integer aiEnabled)
    {
        this.aiEnabled = aiEnabled;
    }

    public Integer getVoiceEnabled()
    {
        return voiceEnabled;
    }

    public void setVoiceEnabled(Integer voiceEnabled)
    {
        this.voiceEnabled = voiceEnabled;
    }

    public Integer getOcrEnabled()
    {
        return ocrEnabled;
    }

    public void setOcrEnabled(Integer ocrEnabled)
    {
        this.ocrEnabled = ocrEnabled;
    }

    public Integer getScanEnabled()
    {
        return scanEnabled;
    }

    public void setScanEnabled(Integer scanEnabled)
    {
        this.scanEnabled = scanEnabled;
    }

    public String getPrintCompanyName()
    {
        return printCompanyName;
    }

    public void setPrintCompanyName(String printCompanyName)
    {
        this.printCompanyName = printCompanyName;
    }

    public String getPrintCompanyAddress()
    {
        return printCompanyAddress;
    }

    public void setPrintCompanyAddress(String printCompanyAddress)
    {
        this.printCompanyAddress = printCompanyAddress;
    }

    public String getPrintCompanyPhone()
    {
        return printCompanyPhone;
    }

    public void setPrintCompanyPhone(String printCompanyPhone)
    {
        this.printCompanyPhone = printCompanyPhone;
    }

    public String getPrintLogoPath()
    {
        return printLogoPath;
    }

    public void setPrintLogoPath(String printLogoPath)
    {
        this.printLogoPath = printLogoPath;
    }
    public java.math.BigDecimal getPrintFontScale()
    {
        return printFontScale;
    }

    public void setPrintFontScale(java.math.BigDecimal printFontScale)
    {
        this.printFontScale = printFontScale;
    }

    public String getPrintPageSize()
    {
        return printPageSize;
    }

    public void setPrintPageSize(String printPageSize)
    {
        this.printPageSize = printPageSize;
    }

    public Integer getMonthlyQuotaText()
    {
        return monthlyQuotaText;
    }

    public void setMonthlyQuotaText(Integer monthlyQuotaText)
    {
        this.monthlyQuotaText = monthlyQuotaText;
    }

    public Integer getMonthlyQuotaVoice()
    {
        return monthlyQuotaVoice;
    }

    public void setMonthlyQuotaVoice(Integer monthlyQuotaVoice)
    {
        this.monthlyQuotaVoice = monthlyQuotaVoice;
    }

    public Integer getAnnualQuotaText()
    {
        return annualQuotaText;
    }

    public void setAnnualQuotaText(Integer annualQuotaText)
    {
        this.annualQuotaText = annualQuotaText;
    }

    public Integer getAnnualQuotaVoice()
    {
        return annualQuotaVoice;
    }

    public void setAnnualQuotaVoice(Integer annualQuotaVoice)
    {
        this.annualQuotaVoice = annualQuotaVoice;
    }

    public Integer getUsedCountText()
    {
        return usedCountText;
    }

    public void setUsedCountText(Integer usedCountText)
    {
        this.usedCountText = usedCountText;
    }

    public Integer getUsedCountVoice()
    {
        return usedCountVoice;
    }

    public void setUsedCountVoice(Integer usedCountVoice)
    {
        this.usedCountVoice = usedCountVoice;
    }

    public Integer getUsedAnnualText()
    {
        return usedAnnualText;
    }

    public void setUsedAnnualText(Integer usedAnnualText)
    {
        this.usedAnnualText = usedAnnualText;
    }

    public Integer getUsedAnnualVoice()
    {
        return usedAnnualVoice;
    }

    public void setUsedAnnualVoice(Integer usedAnnualVoice)
    {
        this.usedAnnualVoice = usedAnnualVoice;
    }

    public Date getQuotaResetDate()
    {
        return quotaResetDate;
    }

    public void setQuotaResetDate(Date quotaResetDate)
    {
        this.quotaResetDate = quotaResetDate;
    }

    public Date getQuotaAnnualResetDate()
    {
        return quotaAnnualResetDate;
    }

    public void setQuotaAnnualResetDate(Date quotaAnnualResetDate)
    {
        this.quotaAnnualResetDate = quotaAnnualResetDate;
    }

    public Integer getAiPoints()
    {
        return aiPoints;
    }

    public void setAiPoints(Integer aiPoints)
    {
        this.aiPoints = aiPoints;
    }

    public Integer getAiPointsTotalCharged()
    {
        return aiPointsTotalCharged;
    }

    public void setAiPointsTotalCharged(Integer aiPointsTotalCharged)
    {
        this.aiPointsTotalCharged = aiPointsTotalCharged;
    }

    public Integer getAiPointsTotalUsed()
    {
        return aiPointsTotalUsed;
    }

    public void setAiPointsTotalUsed(Integer aiPointsTotalUsed)
    {
        this.aiPointsTotalUsed = aiPointsTotalUsed;
    }

    public String getSubscriptionType()
    {
        return subscriptionType;
    }

    public void setSubscriptionType(String subscriptionType)
    {
        this.subscriptionType = subscriptionType;
    }

    public Date getSubscriptionExpireTime()
    {
        return subscriptionExpireTime;
    }

    public void setSubscriptionExpireTime(Date subscriptionExpireTime)
    {
        this.subscriptionExpireTime = subscriptionExpireTime;
    }

    public String getAppleOriginalTransactionId()
    {
        return appleOriginalTransactionId;
    }

    public void setAppleOriginalTransactionId(String appleOriginalTransactionId)
    {
        this.appleOriginalTransactionId = appleOriginalTransactionId;
    }

    public String getAiApiKey()
    {
        return aiApiKey;
    }

    public void setAiApiKey(String aiApiKey)
    {
        this.aiApiKey = aiApiKey;
    }

    public String getAiModel()
    {
        return aiModel;
    }

    public void setAiModel(String aiModel)
    {
        this.aiModel = aiModel;
    }

    public String getAiApiUrl()
    {
        return aiApiUrl;
    }

    public void setAiApiUrl(String aiApiUrl)
    {
        this.aiApiUrl = aiApiUrl;
    }

    public String getAiVisionModel()
    {
        return aiVisionModel;
    }

    public void setAiVisionModel(String aiVisionModel)
    {
        this.aiVisionModel = aiVisionModel;
    }

    public String getAiVoiceModel()
    {
        return aiVoiceModel;
    }

    public void setAiVoiceModel(String aiVoiceModel)
    {
        this.aiVoiceModel = aiVoiceModel;
    }
}
