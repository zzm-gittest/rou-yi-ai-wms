import request from '@/utils/request'

// 查询销售退货分页列表
export function pageSalesReturn(query) {
  return request({
    url: '/wms/salesReturn/page',
    method: 'get',
    params: query
  })
}

// 查询销售退货详细
export function getSalesReturn(id) {
  return request({
    url: '/wms/salesReturn/' + id,
    method: 'get'
  })
}

// 查询销售退货打印数据
export function getSalesReturnPrint(id) {
  return request({
    url: '/wms/salesReturn/print/' + id,
    method: 'get'
  })
}

// 新增销售退货
export function addSalesReturn(data) {
  return request({
    url: '/wms/salesReturn',
    method: 'post',
    data: data
  })
}

// 修改销售退货
export function updateSalesReturn(data) {
  return request({
    url: '/wms/salesReturn',
    method: 'put',
    data: data
  })
}

// 删除销售退货
export function delSalesReturn(id) {
  return request({
    url: '/wms/salesReturn/' + id,
    method: 'delete'
  })
}

// 审核销售退货
export function approveSalesReturn(id) {
  return request({
    url: '/wms/salesReturn/approve/' + id,
    method: 'post'
  })
}

// 导出销售退货
export function exportSalesReturn(query) {
  return request({
    url: '/wms/salesReturn/export',
    method: 'post',
    params: query,
    responseType: 'blob',
    timeout: 120000
  })
}
