import request from '@/utils/request'

// 查询销售出库分页列表
export function pageSalesOut(query) {
  return request({
    url: '/wms/salesOut/page',
    method: 'get',
    params: query
  })
}

// 查询销售出库详细
export function getSalesOut(id) {
  return request({
    url: '/wms/salesOut/' + id,
    method: 'get'
  })
}

// 查询销售出库打印数据
export function getSalesOutPrint(id) {
  return request({
    url: '/wms/salesOut/print/' + id,
    method: 'get'
  })
}

// 新增销售出库
export function addSalesOut(data) {
  return request({
    url: '/wms/salesOut',
    method: 'post',
    data: data
  })
}

// 修改销售出库
export function updateSalesOut(data) {
  return request({
    url: '/wms/salesOut',
    method: 'put',
    data: data
  })
}

// 删除销售出库
export function delSalesOut(id) {
  return request({
    url: '/wms/salesOut/' + id,
    method: 'delete'
  })
}

// 审核销售出库
export function approveSalesOut(id) {
  return request({
    url: '/wms/salesOut/approve/' + id,
    method: 'post'
  })
}

// 反审核销售出库
export function cancelApproveSalesOut(id) {
  return request({
    url: '/wms/salesOut/cancelApprove/' + id,
    method: 'post'
  })
}

// 查询已审核的销售出库列表
export function listApprovedSalesOut() {
  return request({
    url: '/wms/salesOut/approved',
    method: 'get'
  })
}

// 获取销售出库明细
export function getSalesOutItems(outId) {
  return request({
    url: '/wms/salesOut/items/' + outId,
    method: 'get'
  })
}

// 销售出库单保存回调（移动端保存后回调到聊天界面）
export function salesOutSavedCallback(tempDataKey, outId) {
  return request({
    url: '/wms/ai/callback/sales-out/saved',
    method: 'post',
    params: {
      tempDataKey: tempDataKey,
      outId: outId
    }
  })
}

// 查询可退货的销售出库单列表（已审核且未全部退货）
export function getAvailableSalesOutForReturn(keyword) {
  return request({
    url: '/wms/salesOut/availableForReturn',
    method: 'get',
    params: { keyword }
  })
}

// 导出销售出库
export function exportSalesOut(query) {
  return request({
    url: '/wms/salesOut/export',
    method: 'post',
    params: query,
    responseType: 'blob',
    timeout: 120000
  })
}
