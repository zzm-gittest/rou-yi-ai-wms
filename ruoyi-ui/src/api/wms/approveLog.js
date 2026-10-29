import request from '@/utils/request'

// 查询审核日志分页列表
export function pageApproveLog(query) {
  return request({
    url: '/wms/approveLog/page',
    method: 'get',
    params: query
  })
}

// 查询审核日志详情
export function getApproveLog(id) {
  return request({
    url: '/wms/approveLog/' + id,
    method: 'get'
  })
}
