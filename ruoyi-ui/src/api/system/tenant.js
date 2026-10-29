import request from '@/utils/request'

// 查询租户列表
export function listTenant(query) {
  return request({
    url: '/system/tenant/list',
    method: 'get',
    params: query
  })
}

// 获取启用的租户列表（公开接口，用于注册）
export function listEnableTenant() {
  return request({
    url: '/system/tenant/listEnable',
    method: 'get',
    headers: {
      isToken: false
    }
  })
}

// 查询租户详细
export function getTenant(tenantId) {
  return request({
    url: '/system/tenant/' + tenantId,
    method: 'get'
  })
}

// 根据租户名称查询租户
export function getTenantByName(tenantName) {
  return request({
    url: '/system/tenant/name/' + tenantName,
    method: 'get',
    headers: {
      isToken: false
    }
  })
}

// 新增租户
export function addTenant(data) {
  return request({
    url: '/system/tenant',
    method: 'post',
    data: data
  })
}

// 修改租户
export function updateTenant(data) {
  return request({
    url: '/system/tenant',
    method: 'put',
    data: data
  })
}

// 删除租户
export function delTenant(tenantId) {
  return request({
    url: '/system/tenant/' + tenantId,
    method: 'delete'
  })
}

// 租户注册（公开接口）
export function registerTenant(data) {
  return request({
    url: '/system/tenant/register',
    method: 'post',
    headers: {
      isToken: false
    },
    data: data
  })
}

// 检查租户名称是否存在
export function checkTenantName(tenantName) {
  return request({
    url: '/system/tenant/check/' + tenantName,
    method: 'get',
    headers: {
      isToken: false
    }
  })
}

// 获取当前租户的功能配置
export function getFeatureConfig() {
  return request({
    url: '/system/tenant/feature',
    method: 'get'
  })
}

// 获取当前租户打印资料
export function getTenantPrintProfile() {
  return request({
    url: '/system/tenant/printProfile',
    method: 'get'
  })
}

// 保存当前租户打印资料
export function updateTenantPrintProfile(data) {
  return request({
    url: '/system/tenant/printProfile',
    method: 'put',
    data: data
  })
}

// 管理员给租户充值AI点数
export function rechargePoints(data) {
  return request({
    url: '/system/tenant/rechargePoints',
    method: 'post',
    data: data
  })
}

// 查询租户AI点数信息
export function getPointsInfo(tenantId) {
  return request({
    url: '/system/tenant/pointsInfo/' + tenantId,
    method: 'get'
  })
}

// 查询租户AI点数流水
export function getPointsLog(tenantId) {
  return request({
    url: '/system/tenant/pointsLog/' + tenantId,
    method: 'get'
  })
}
