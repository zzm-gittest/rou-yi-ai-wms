import request from '@/utils/request'

// 查询审核策略分页列表
export function pageApprovePolicy(query) {
  return request({
    url: '/wms/approvePolicy/page',
    method: 'get',
    params: query
  })
}

// 查询审核策略详情
export function getApprovePolicy(id) {
  return request({
    url: '/wms/approvePolicy/' + id,
    method: 'get'
  })
}

// 保存审核策略配置
export function updateApprovePolicy(data) {
  return request({
    url: '/wms/approvePolicy',
    method: 'put',
    data: data
  })
}

// 查询当前租户指定单据类型是否需要审核
export function getBillNeedApprove(billType) {
  return request({
    url: '/wms/approvePolicy/needApprove/' + billType,
    method: 'get'
  })
}

// 查询当前租户指定单据类型的完整策略（含是否自动打印）
export function getBillPolicy(billType) {
  return request({
    url: '/wms/approvePolicy/policy/' + billType,
    method: 'get'
  })
}
