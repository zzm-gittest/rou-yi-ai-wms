import request from '@/utils/request'

// 查询采购退货分页列表
export function pagePurchaseReturn(query) {
  return request({
    url: '/wms/purchaseReturn/page',
    method: 'get',
    params: query
  })
}

// 查询采购退货详细
export function getPurchaseReturn(id) {
  return request({
    url: '/wms/purchaseReturn/' + id,
    method: 'get'
  })
}

// 查询采购退货打印数据
export function getPurchaseReturnPrint(id) {
  return request({
    url: '/wms/purchaseReturn/print/' + id,
    method: 'get'
  })
}

// 新增采购退货
export function addPurchaseReturn(data) {
  return request({
    url: '/wms/purchaseReturn',
    method: 'post',
    data: data
  })
}

// 修改采购退货
export function updatePurchaseReturn(data) {
  return request({
    url: '/wms/purchaseReturn',
    method: 'put',
    data: data
  })
}

// 删除采购退货
export function delPurchaseReturn(id) {
  return request({
    url: '/wms/purchaseReturn/' + id,
    method: 'delete'
  })
}

// 审核采购退货
export function approvePurchaseReturn(id) {
  return request({
    url: '/wms/purchaseReturn/approve/' + id,
    method: 'post'
  })
}

// 获取已审核的采购入库单列表（用于退货）
export function getApprovedPurchaseInList(query) {
  return request({
    url: '/wms/purchaseReturn/approvedIns',
    method: 'get',
    params: query
  })
}

// 获取采购入库单的商品明细（用于退货）
export function getInItems(inId) {
  return request({
    url: '/wms/purchaseReturn/inItems/' + inId,
    method: 'get'
  })
}

// 导出采购退货
export function exportPurchaseReturn(query) {
  return request({
    url: '/wms/purchaseReturn/export',
    method: 'post',
    params: query,
    responseType: 'blob',
    timeout: 120000
  })
}
