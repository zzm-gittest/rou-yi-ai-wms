package com.ruoyi.common.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.page.PageDomain;
import com.ruoyi.common.core.page.TableSupport;

/**
 * 分页工具类
 *
 * @author ruoyi
 */
public class PageUtils
{
    /**
     * 获取分页对象
     */
    public static <T> Page<T> getPage()
    {
        return getPage(null, null);
    }

    /**
     * 获取分页对象
     *
     * @param defaultPageNum 默认页码
     * @param defaultPageSize 默认每页条数
     */
    public static <T> Page<T> getPage(Integer defaultPageNum, Integer defaultPageSize)
    {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();

        if (pageNum == null || pageNum < 1)
        {
            pageNum = defaultPageNum != null ? defaultPageNum : 1;
        }
        if (pageSize == null || pageSize < 1)
        {
            pageSize = defaultPageSize != null ? defaultPageSize : 10;
        }

        // 设置最大单页限制
        if (pageSize > 500)
        {
            pageSize = 500;
        }

        return new Page<>(pageNum, pageSize);
    }

    /**
     * 获取分页对象（使用默认每页10条）
     */
    public static <T> Page<T> getPage(Integer defaultPageNum)
    {
        return getPage(defaultPageNum, 10);
    }
}
