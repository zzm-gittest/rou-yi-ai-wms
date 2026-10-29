package com.ruoyi.framework.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.ruoyi.common.tenant.TenantLineHandlerImpl;
import org.apache.ibatis.reflection.MetaObject;

import com.ruoyi.common.utils.SecurityUtils;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus配置类
 *
 * @author ruoyi
 */
@Configuration
public class MybatisPlusConfig
{
    /**
     * MyBatis-Plus拦截器配置
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor()
    {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 租户插件 (必须在最前面)
        TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor();
        tenantInterceptor.setTenantLineHandler(new TenantLineHandlerImpl());
        interceptor.addInnerInterceptor(tenantInterceptor);

        // 2. 分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInterceptor.setMaxLimit(500L); // 设置单页最大限制数量
        paginationInterceptor.setOverflow(false); // 溢出总页数后是否进行处理
        interceptor.addInnerInterceptor(paginationInterceptor);

        // 3. 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 4. 防止全表更新和删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }

    /**
     * 字段自动填充处理器
     */
    @Bean
    public MetaObjectHandler metaObjectHandler()
    {
        return new MetaObjectHandler()
        {
            private static final Logger log = LoggerFactory.getLogger("MetaObjectHandler");

            /**
             * 安全获取当前用户名，@Async 线程中 SecurityContext 不可用时返回 "system"
             */
            private String safeGetUsername() {
                try {
                    return SecurityUtils.getUsername();
                } catch (Exception e) {
                    return "system";
                }
            }

            @Override
            public void insertFill(MetaObject metaObject)
            {
                LocalDateTime now = LocalDateTime.now();
                String username = safeGetUsername();

                // 严格模式填充（仅当字段为 null 时写入）
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
                try {
                    this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
                } catch (Exception ignored) { }
                this.strictInsertFill(metaObject, "createBy", String.class, username);

                // 兜底：直接设值（支持继承字段）
                if (metaObject.getValue("createTime") == null) {
                    metaObject.setValue("createTime", now);
                }
                if (metaObject.getValue("updateTime") == null) {
                    metaObject.setValue("updateTime", now);
                }
                if (metaObject.getValue("createBy") == null) {
                    metaObject.setValue("createBy", username);
                }
                if (metaObject.getValue("updateBy") == null) {
                    metaObject.setValue("updateBy", username);
                }
            }

            @Override
            public void updateFill(MetaObject metaObject)
            {
                String username = safeGetUsername();
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
                this.strictUpdateFill(metaObject, "updateBy", String.class, username);

                if (metaObject.getValue("updateTime") == null) {
                    metaObject.setValue("updateTime", LocalDateTime.now());
                }
                if (metaObject.getValue("updateBy") == null) {
                    metaObject.setValue("updateBy", username);
                }
            }
        };
    }
}
