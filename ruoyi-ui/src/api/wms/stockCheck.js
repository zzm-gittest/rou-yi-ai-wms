import request from '@/utils/request'

// 查询库存盘点单分页列表
export function pageStockCheck(query) {
  return request({
    url: '/wms/stockCheck/page',
    method: 'get',
    params: query
  })
}

// 查询库存盘点单详细
export function getStockCheck(id) {
  return request({
    url: '/wms/stockCheck/' + id,
    method: 'get'
  })
}

// 查询库存盘点打印数据
export function getStockCheckPrint(id) {
  return request({
    url: '/wms/stockCheck/print/' + id,
    method: 'get'
  })
}

// 新增库存盘点单
export function addStockCheck(data) {
  return request({
    url: '/wms/stockCheck',
    method: 'post',
    data: data
  })
}

// 修改库存盘点单
export function updateStockCheck(data) {
  return request({
    url: '/wms/stockCheck',
    method: 'put',
    data: data
  })
}

// 删除库存盘点单
export function delStockCheck(id) {
  return request({
    url: '/wms/stockCheck/' + id,
    method: 'delete'
  })
}

// 审核库存盘点单
export function approveStockCheck(id) {
  return request({
    url: '/wms/stockCheck/approve/' + id,
    method: 'post'
  })
}

// 导出库存盘点
export function exportStockCheck(query) {
  return request({
    url: '/wms/stockCheck/export',
    method: 'post',
    params: query,
    responseType: 'blob',
    timeout: 120000
  })
}
