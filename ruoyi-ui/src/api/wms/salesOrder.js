import request from '@/utils/request'

// 查询销售订单分页列表
export function pageSalesOrder(query) {
  return request({
    url: '/wms/salesOrder/page',
    method: 'get',
    params: query
  })
}

// 查询销售订单详细
export function getSalesOrder(id) {
  return request({
    url: '/wms/salesOrder/' + id,
    method: 'get'
  })
}

// 查询销售订单打印数据
export function getSalesOrderPrint(id) {
  return request({
    url: '/wms/salesOrder/print/' + id,
    method: 'get'
  })
}

// 新增销售订单
export function addSalesOrder(data) {
  return request({
    url: '/wms/salesOrder',
    method: 'post',
    data: data
  })
}

// 修改销售订单
export function updateSalesOrder(data) {
  return request({
    url: '/wms/salesOrder',
    method: 'put',
    data: data
  })
}

// 删除销售订单
export function delSalesOrder(id) {
  return request({
    url: '/wms/salesOrder/' + id,
    method: 'delete'
  })
}

// 审核销售订单
export function approveSalesOrder(id) {
  return request({
    url: '/wms/salesOrder/approve/' + id,
    method: 'post'
  })
}

// 关闭销售订单
export function closeSalesOrder(id) {
  return request({
    url: '/wms/salesOrder/close/' + id,
    method: 'post'
  })
}

// 查询已审核的销售订单列表
export function listApprovedSalesOrder() {
  return request({
    url: '/wms/salesOrder/approved',
    method: 'get'
  })
}

// 获取销售订单明细
export function getSalesOrderItems(orderId) {
  return request({
    url: '/wms/salesOrder/items/' + orderId,
    method: 'get'
  })
}

// 搜索可用于出库的已审核销售订单
export function getAvailableSalesOrderForOut(keyword) {
  return request({
    url: '/wms/salesOrder/availableForOut',
    method: 'get',
    params: keyword ? { keyword } : undefined
  })
}

// 导出销售订单
export function exportSalesOrder(query) {
  return request({
    url: '/wms/salesOrder/export',
    method: 'post',
    params: query,
    responseType: 'blob',
    timeout: 120000
  })
}
