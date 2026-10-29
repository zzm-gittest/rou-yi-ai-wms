import request from '@/utils/request'

// 查询财务日志分页列表
export function pageFinanceLog(query) {
  return request({
    url: '/wms/financeLog/page',
    method: 'get',
    params: query
  })
}

// 查询财务日志详细
export function getFinanceLog(id) {
  return request({
    url: '/wms/financeLog/' + id,
    method: 'get'
  })
}
