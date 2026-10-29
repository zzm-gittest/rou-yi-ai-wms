package com.ruoyi.common.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 分页上下文持有者
 * 使用ThreadLocal存储当前请求的分页对象，供Service/Mapper层使用
 *
 * @author ruoyi
 */
public class PageHolder
{
    private static final ThreadLocal<Page<?>> PAGE_HOLDER = new ThreadLocal<>();

    /**
     * 设置分页对象
     */
    public static void setPage(Page<?> page)
    {
        PAGE_HOLDER.set(page);
    }

    /**
     * 获取分页对象
     */
    @SuppressWarnings("unchecked")
    public static <T> Page<T> getPage()
    {
        return (Page<T>) PAGE_HOLDER.get();
    }

    /**
     * 清除分页对象
     */
    public static void clear()
    {
        PAGE_HOLDER.remove();
    }
}
