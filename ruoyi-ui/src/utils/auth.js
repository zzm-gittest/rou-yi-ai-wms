import Cookies from 'js-cookie'

const TokenKey = 'Admin-Token'
const TenantIdKey = 'Admin-Tenant-Id'
const TenantNameKey = 'Admin-Tenant-Name'

export function getToken() {
  return Cookies.get(TokenKey)
}

export function setToken(token) {
  return Cookies.set(TokenKey, token)
}

export function removeToken() {
  return Cookies.remove(TokenKey)
}

// ========== 租户相关 ==========

export function getTenantId() {
  return Cookies.get(TenantIdKey)
}

export function setTenantId(tenantId) {
  return Cookies.set(TenantIdKey, tenantId)
}

export function removeTenantId() {
  return Cookies.remove(TenantIdKey)
}

export function getTenantName() {
  return Cookies.get(TenantNameKey)
}

export function setTenantName(tenantName) {
  return Cookies.set(TenantNameKey, tenantName)
}

export function removeTenantName() {
  return Cookies.remove(TenantNameKey)
}

// 清除所有认证信息
export function clearAuth() {
  removeToken()
  removeTenantId()
  removeTenantName()
}

// ========== AI助手会话快照（localStorage，按 租户+用户 隔离） ==========
const AiChatHistoryPrefix = 'wms-ai-pc-chat-history-v2-'

// 清除本机所有租户/用户的 AI 助手会话快照（登出时调用，防止共享电脑残留业务数据）
export function clearAiChatHistoryCache() {
  try {
    const keys = []
    for (let i = 0; i < window.localStorage.length; i++) {
      const key = window.localStorage.key(i)
      if (key && key.indexOf(AiChatHistoryPrefix) === 0) {
        keys.push(key)
      }
    }
    keys.forEach(key => window.localStorage.removeItem(key))
  } catch (e) {
    // localStorage 不可用（如隐私模式受限）时忽略
  }
}
