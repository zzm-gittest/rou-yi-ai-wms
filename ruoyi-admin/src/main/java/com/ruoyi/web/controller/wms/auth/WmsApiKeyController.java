package com.ruoyi.web.controller.wms.auth;

import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginBody;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.framework.web.service.SysLoginService;
import com.ruoyi.framework.web.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * WMS API Key管理控制器
 * <p>
 * 提供API Key的生成、验证、刷新等功能
 * 用于第三方系统（如客服机器人）免登录访问WMS数据
 * </p>
 *
 * @author ruoyi
 */
@Slf4j
@RestController
@RequestMapping("/wms/apikey")
public class WmsApiKeyController {

    @Autowired
    private SysLoginService loginService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    private static final String API_KEY_PREFIX = "wms:apikey:";
    private static final String IM_SSO_TICKET_PREFIX = "wms:im_sso:";
    private static final long API_KEY_TTL = 365 * 24 * 3600; // 1年有效期
    /** IM 机器人「操作」跳转免登票据有效期（秒） */
    private static final long IM_SSO_TICKET_TTL = 300;

    /**
     * 生成API Key（需要登录后调用）
     * <p>
     * 前端流程：
     * 1. 用户在iframe中登录WMS
     * 2. 登录成功后，调用此接口生成API Key
     * 3. 返回API Key和Secret给父窗口（客服系统）
     * </p>
     *
     * @return API Key信息
     */
    @PostMapping("/generate")
    public AjaxResult generateApiKey() {
        try {
            // 检查Redis是否可用
            if (redisTemplate == null) {
                log.warn("Redis未配置或不可用，无法生成API Key");
                return AjaxResult.error("Redis服务不可用，请联系管理员");
            }
            
            // 获取当前登录用户信息
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (loginUser == null) {
                return AjaxResult.error("请先登录WMS");
            }
            
            Long tenantId = loginUser.getTenantId();
            Long userId = loginUser.getUserId();
            String username = loginUser.getUsername();

            // 生成API Key和Secret
            String apiKey = "ak_" + IdUtils.fastSimpleUUID();
            String apiSecret = "sk_" + IdUtils.fastSimpleUUID();

            // 构建API Key信息
            Map<String, Object> keyInfo = new HashMap<>();
            keyInfo.put("apiKey", apiKey);
            keyInfo.put("apiSecret", apiSecret);
            keyInfo.put("tenantId", tenantId);
            keyInfo.put("userId", userId);
            keyInfo.put("username", username);
            keyInfo.put("createTime", System.currentTimeMillis());
            keyInfo.put("expireTime", System.currentTimeMillis() + (API_KEY_TTL * 1000));

            // 缓存到Redis（1年有效期）
            String cacheKey = API_KEY_PREFIX + apiKey;
            redisTemplate.opsForValue().set(cacheKey, keyInfo, API_KEY_TTL, TimeUnit.SECONDS);

            log.info("生成WMS API Key成功: apiKey={}, tenantId={}, username={}", apiKey, tenantId, username);

            // 返回API Key信息（Secret只返回一次）
            Map<String, Object> result = new HashMap<>();
            result.put("apiKey", apiKey);
            result.put("apiSecret", apiSecret);  // 只返回一次，后续无法查看
            result.put("tenantId", tenantId);
            result.put("expireDays", 365);
            result.put("message", "请妥善保存API Secret，关闭后将无法再次查看");

            return AjaxResult.success("API Key生成成功", result);

        } catch (Exception e) {
            log.error("生成WMS API Key失败", e);
            return AjaxResult.error("生成API Key失败：" + e.getMessage());
        }
    }

    /**
     * 验证API Key
     * <p>
     * 供客服系统调用，验证API Key的有效性
     * </p>
     *
     * @param apiKey API Key
     * @param apiSecret API Secret
     * @return 验证结果
     */
    @PostMapping("/validate")
    public AjaxResult validateApiKey(@RequestBody Map<String, String> params) {
        // 检查Redis是否可用
        if (redisTemplate == null) {
            log.warn("Redis未配置或不可用，无法验证API Key");
            return AjaxResult.error("Redis服务不可用，请联系管理员");
        }
        
        String apiKey = params.get("apiKey");
        String apiSecret = params.get("apiSecret");

        if (apiKey == null || apiSecret == null) {
            return AjaxResult.error("API Key和Secret不能为空");
        }

        try {
            String cacheKey = API_KEY_PREFIX + apiKey;
            Map<String, Object> keyInfo = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);

            if (keyInfo == null) {
                return AjaxResult.error("API Key无效或已过期");
            }

            String storedSecret = (String) keyInfo.get("apiSecret");
            if (!apiSecret.equals(storedSecret)) {
                return AjaxResult.error("API Secret错误");
            }

            // 验证通过，返回租户ID
            Map<String, Object> result = new HashMap<>();
            result.put("valid", true);
            result.put("tenantId", keyInfo.get("tenantId"));
            result.put("expireTime", keyInfo.get("expireTime"));

            return AjaxResult.success("验证成功", result);

        } catch (Exception e) {
            log.error("验证WMS API Key失败", e);
            return AjaxResult.error("验证失败：" + e.getMessage());
        }
    }

    /**
     * 撤销API Key
     *
     * @param apiKey API Key
     * @return 结果
     */
    @PostMapping("/revoke")
    public AjaxResult revokeApiKey(@RequestParam String apiKey) {
        // 检查Redis是否可用
        if (redisTemplate == null) {
            log.warn("Redis未配置或不可用，无法撤销API Key");
            return AjaxResult.error("Redis服务不可用，请联系管理员");
        }
        
        try {
            String cacheKey = API_KEY_PREFIX + apiKey;
            redisTemplate.delete(cacheKey);

            log.info("撤销WMS API Key成功: apiKey={}", apiKey);
            return AjaxResult.success("API Key已撤销");

        } catch (Exception e) {
            log.error("撤销WMS API Key失败", e);
            return AjaxResult.error("撤销失败：" + e.getMessage());
        }
    }

    /**
     * 快速授权接口（一步完成登录+生成API Key）
     * <p>
     * 供客服系统调用，简化授权流程
     * </p>
     *
     * @param loginBody 登录信息
     * @return API Key信息
     */
    @PostMapping("/quickAuth")
    public AjaxResult quickAuth(@RequestBody LoginBody loginBody) {
        // 检查Redis是否可用
        if (redisTemplate == null) {
            log.warn("Redis未配置或不可用，无法进行快速授权");
            return AjaxResult.error("Redis服务不可用，请联系管理员");
        }
        
        try {
            // 1. 先登录WMS（跳过验证码）
            String token = loginService.login(
                loginBody.getUsername(),
                loginBody.getPassword(),
                null,  // code
                null   // uuid
            );

            if (token == null) {
                return AjaxResult.error("登录失败，请检查用户名和密码");
            }

            // 2. 从Redis中获取LoginUser信息（因为quickAuth接口是免登录的，SecurityContext中没有用户信息）
            // 注意：TokenService.createToken返回的是JWT字符串，其中login_user_key存储的是UUID
            String uuid = tokenService.parseToken(token).get(Constants.LOGIN_USER_KEY).toString();
            String userKey = CacheConstants.LOGIN_TOKEN_KEY + uuid;
            LoginUser loginUser = (LoginUser) redisTemplate.opsForValue().get(userKey);
            
            if (loginUser == null) {
                log.error("登录成功但无法从Redis获取用户信息: token={}", token);
                return AjaxResult.error("登录成功但无法获取用户信息");
            }
            
            Long tenantId = loginUser.getTenantId();
            Long userId = loginUser.getUserId();
            String username = loginUser.getUsername();

            // 3. 生成API Key
            String apiKey = "ak_" + IdUtils.fastSimpleUUID();
            String apiSecret = "sk_" + IdUtils.fastSimpleUUID();

            // 构建API Key信息
            Map<String, Object> keyInfo = new HashMap<>();
            keyInfo.put("apiKey", apiKey);
            keyInfo.put("apiSecret", apiSecret);
            keyInfo.put("tenantId", tenantId);
            keyInfo.put("userId", userId);
            keyInfo.put("username", username);
            keyInfo.put("createTime", System.currentTimeMillis());
            keyInfo.put("expireTime", System.currentTimeMillis() + (API_KEY_TTL * 1000));

            // 缓存到Redis
            String cacheKey = API_KEY_PREFIX + apiKey;
            redisTemplate.opsForValue().set(cacheKey, keyInfo, API_KEY_TTL, TimeUnit.SECONDS);

            log.info("WMS快速授权成功: apiKey={}, tenantId={}, username={}", apiKey, tenantId, username);

            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("apiKey", apiKey);
            result.put("apiSecret", apiSecret);
            result.put("tenantId", tenantId);
            result.put("token", token);  // 也返回token，方便后续使用
            result.put("expireDays", 365);
            result.put("message", "授权成功！请妥善保存API Secret，关闭后将无法再次查看");

            return AjaxResult.success("授权成功", result);

        } catch (Exception e) {
            log.error("WMS快速授权失败", e);
            return AjaxResult.error("授权失败：" + e.getMessage());
        }
    }

    /**
     * 签发 IM 跳转免登一次性票据（供 AIGW 机器人查单「操作」链接使用，不暴露 JWT）
     */
    @PostMapping("/imSsoTicket")
    public AjaxResult createImSsoTicket(@RequestBody LoginBody loginBody) {
        if (redisTemplate == null) {
            return AjaxResult.error("Redis服务不可用，请联系管理员");
        }
        if (loginBody == null || loginBody.getUsername() == null || loginBody.getPassword() == null) {
            return AjaxResult.error("用户名和密码不能为空");
        }
        try {
            String token = loginService.login(
                loginBody.getUsername(),
                loginBody.getPassword(),
                null,
                null
            );
            if (token == null) {
                return AjaxResult.error("登录失败，请检查用户名和密码");
            }
            String uuid = tokenService.parseToken(token).get(Constants.LOGIN_USER_KEY).toString();
            String userKey = CacheConstants.LOGIN_TOKEN_KEY + uuid;
            LoginUser loginUser = (LoginUser) redisTemplate.opsForValue().get(userKey);
            if (loginUser == null) {
                return AjaxResult.error("登录成功但无法获取用户信息");
            }
            String ticket = "st_" + IdUtils.fastSimpleUUID();
            Map<String, Object> payload = new HashMap<>();
            payload.put("token", token);
            payload.put("tenantId", loginUser.getTenantId());
            redisTemplate.opsForValue().set(
                IM_SSO_TICKET_PREFIX + ticket, payload, IM_SSO_TICKET_TTL, TimeUnit.SECONDS);
            Map<String, Object> result = new HashMap<>();
            result.put("ticket", ticket);
            result.put("expireSeconds", IM_SSO_TICKET_TTL);
            log.info("IM SSO 票据已签发: tenantId={}, username={}", loginUser.getTenantId(), loginUser.getUsername());
            return AjaxResult.success(result);
        } catch (Exception e) {
            log.error("IM SSO 票据签发失败", e);
            return AjaxResult.error("签发失败：" + e.getMessage());
        }
    }

    /**
     * 用一次性票据换取 WMS 登录 Token（PC / App 打开链接时调用，票据用后即删）
     */
    @GetMapping("/imSsoExchange")
    public AjaxResult exchangeImSsoTicket(@RequestParam("ticket") String ticket) {
        if (redisTemplate == null) {
            return AjaxResult.error("Redis服务不可用，请联系管理员");
        }
        if (ticket == null || ticket.isBlank()) {
            return AjaxResult.error("票据无效");
        }
        try {
            String cacheKey = IM_SSO_TICKET_PREFIX + ticket.trim();
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);
            if (payload == null) {
                return AjaxResult.error("票据已过期或无效，请回到飞书重新查询后再点「操作」");
            }
            redisTemplate.delete(cacheKey);
            Map<String, Object> result = new HashMap<>();
            result.put("token", payload.get("token"));
            result.put("tenantId", payload.get("tenantId"));
            return AjaxResult.success(result);
        } catch (Exception e) {
            log.error("IM SSO 票据兑换失败", e);
            return AjaxResult.error("兑换失败：" + e.getMessage());
        }
    }
}
