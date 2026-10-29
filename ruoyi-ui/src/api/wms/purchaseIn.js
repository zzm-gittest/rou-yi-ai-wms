import request from '@/utils/request'

// 查询采购入库分页列表
export function pagePurchaseIn(query) {
  return request({
    url: '/wms/purchaseIn/page',
    method: 'get',
    params: query
  })
}

// 查询采购入库详细
export function getPurchaseIn(id) {
  return request({
    url: '/wms/purchaseIn/' + id,
    method: 'get'
  })
}

// 查询采购入库打印数据
export function getPurchaseInPrint(id) {
  return request({
    url: '/wms/purchaseIn/print/' + id,
    method: 'get'
  })
}

// 新增采购入库
export function addPurchaseIn(data) {
  return request({
    url: '/wms/purchaseIn',
    method: 'post',
    data: data
  })
}

// 修改采购入库
export function updatePurchaseIn(data) {
  return request({
    url: '/wms/purchaseIn',
    method: 'put',
    data: data
  })
}

// 删除采购入库
export function delPurchaseIn(id) {
  return request({
    url: '/wms/purchaseIn/' + id,
    method: 'delete'
  })
}

// 审核采购入库
export function approvePurchaseIn(id) {
  return request({
    url: '/wms/purchaseIn/approve/' + id,
    method: 'post'
  })
}

// 反审核采购入库
export function cancelApprovePurchaseIn(id) {
  return request({
    url: '/wms/purchaseIn/cancelApprove/' + id,
    method: 'post'
  })
}

// 根据采购订单ID获取可入库商品明细
export function getOrderItems(orderId) {
  return request({
    url: '/wms/purchaseIn/orderItems/' + orderId,
    method: 'get'
  })
}

// 获取可退货的采购入库单列表（已审核且未全部退货）
export function getAvailablePurchaseInForReturn(keyword) {
  return request({
    url: '/wms/purchaseIn/availableForReturn',
    method: 'get',
    params: keyword ? { keyword } : undefined
  })
}

// 采购入库单保存回调（移动端保存后回调到聊天界面）
export function purchaseInSavedCallback(tempDataKey, inId) {
  return request({
    url: '/wms/ai/callback/purchase-in/saved',
    method: 'post',
    params: {
      tempDataKey: tempDataKey,
      inId: inId
    }
  })
}

// 导出采购入库
export function exportPurchaseIn(query) {
  return request({
    url: '/wms/purchaseIn/export',
    method: 'post',
    params: query,
    responseType: 'blob',
    timeout: 120000
  })
}
