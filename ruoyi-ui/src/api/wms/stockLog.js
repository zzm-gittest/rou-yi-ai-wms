import request from '@/utils/request'

// 查询库存变动日志分页列表
export function pageStockLog(query) {
  return request({
    url: '/wms/stockLog/page',
    method: 'get',
    params: query
  })
}

// 查询库存变动日志详细
export function getStockLog(id) {
  return request({
    url: '/wms/stockLog/' + id,
    method: 'get'
  })
}
