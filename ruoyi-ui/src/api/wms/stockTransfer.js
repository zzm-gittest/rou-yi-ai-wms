import request from '@/utils/request'

// 查询库存调拨单分页列表
export function pageStockTransfer(query) {
  return request({
    url: '/wms/stockTransfer/page',
    method: 'get',
    params: query
  })
}

// 查询库存调拨单详细
export function getStockTransfer(id) {
  return request({
    url: '/wms/stockTransfer/' + id,
    method: 'get'
  })
}

// 查询库存调拨打印数据
export function getStockTransferPrint(id) {
  return request({
    url: '/wms/stockTransfer/print/' + id,
    method: 'get'
  })
}

// 新增库存调拨单
export function addStockTransfer(data) {
  return request({
    url: '/wms/stockTransfer',
    method: 'post',
    data: data
  })
}

// 修改库存调拨单
export function updateStockTransfer(data) {
  return request({
    url: '/wms/stockTransfer',
    method: 'put',
    data: data
  })
}

// 删除库存调拨单
export function delStockTransfer(id) {
  return request({
    url: '/wms/stockTransfer/' + id,
    method: 'delete'
  })
}

// 审核库存调拨单
export function approveStockTransfer(id) {
  return request({
    url: '/wms/stockTransfer/approve/' + id,
    method: 'post'
  })
}

// 导出库存调拨
export function exportStockTransfer(query) {
  return request({
    url: '/wms/stockTransfer/export',
    method: 'post',
    params: query,
    responseType: 'blob',
    timeout: 120000
  })
}
