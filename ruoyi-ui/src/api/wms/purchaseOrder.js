import request from '@/utils/request'

// 查询采购订单分页列表
export function pagePurchaseOrder(query) {
  return request({
    url: '/wms/purchaseOrder/page',
    method: 'get',
    params: query
  })
}

// 查询采购订单详细
export function getPurchaseOrder(id) {
  return request({
    url: '/wms/purchaseOrder/' + id,
    method: 'get'
  })
}

// 查询采购订单打印数据
export function getPurchaseOrderPrint(id) {
  return request({
    url: '/wms/purchaseOrder/print/' + id,
    method: 'get'
  })
}

// 新增采购订单
export function addPurchaseOrder(data) {
  return request({
    url: '/wms/purchaseOrder',
    method: 'post',
    data: data
  })
}

// 修改采购订单
export function updatePurchaseOrder(data) {
  return request({
    url: '/wms/purchaseOrder',
    method: 'put',
    data: data
  })
}

// 删除采购订单
export function delPurchaseOrder(id) {
  return request({
    url: '/wms/purchaseOrder/' + id,
    method: 'delete'
  })
}

// 审核采购订单
export function approvePurchaseOrder(id) {
  return request({
    url: '/wms/purchaseOrder/approve/' + id,
    method: 'post'
  })
}

// 关闭采购订单
export function closePurchaseOrder(id) {
  return request({
    url: '/wms/purchaseOrder/close/' + id,
    method: 'post'
  })
}

// 获取可入库的采购订单列表（已审核且未全部入库）
export function getAvailablePurchaseOrdersForIn() {
  return request({
    url: '/wms/purchaseOrder/availableForIn',
    method: 'get'
  })
}

// 导出采购订单
export function exportPurchaseOrder(query) {
  return request({
    url: '/wms/purchaseOrder/export',
    method: 'post',
    params: query,
    responseType: 'blob',
    timeout: 120000
  })
}
