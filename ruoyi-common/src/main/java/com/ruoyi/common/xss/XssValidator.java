package com.ruoyi.common.xss;

import com.ruoyi.common.utils.StringUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自定义xss校验注解实现
 *
 * @author ruoyi
 */
public class XssValidator implements ConstraintValidator<Xss, String>
{
    /**
     * HTML标签检测正则 - 匹配各种HTML标签格式
     */
    private static final Pattern HTML_PATTERN = Pattern.compile(
            "<[^>]+>", // 匹配任何HTML标签
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 危险脚本检测正则 - 匹配script标签和javascript协议
     */
    private static final Pattern DANGEROUS_PATTERN = Pattern.compile(
            "<\\s*script[^>]*>.*?<\\s*/\\s*script\\s*>|" +  // script标签
            "javascript\\s*:|" +                            // javascript协议
            "on\\w+\\s*=|" +                                // 事件处理器如onclick=
            "<\\s*iframe[^>]*>|" +                          // iframe标签
            "<\\s*object[^>]*>|" +                          // object标签
            "<\\s*embed[^>]*>|" +                           // embed标签
            "<\\s*link[^>]*>|" +                            // link标签
            "<\\s*meta[^>]*>|" +                            // meta标签
            "<\\s*style[^>]*>.*?<\\s*/\\s*style\\s*>|" +   // style标签
            "expression\\s*\\(|" +                          // CSS expression
            "vbscript\\s*:|" +                              // vbscript协议
            "data\\s*:\\s*text/html",                       // data协议
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext)
    {
        if (StringUtils.isBlank(value))
        {
            return true;
        }
        return !containsHtml(value) && !containsDangerousContent(value);
    }

    /**
     * 检测是否包含HTML标签
     */
    public static boolean containsHtml(String value)
    {
        return HTML_PATTERN.matcher(value).find();
    }

    /**
     * 检测是否包含危险脚本内容
     */
    public static boolean containsDangerousContent(String value)
    {
        return DANGEROUS_PATTERN.matcher(value).find();
    }
}