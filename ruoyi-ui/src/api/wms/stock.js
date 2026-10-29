import request from '@/utils/request'

// 查询库存分页列表
export function pageStock(query) {
  return request({
    url: '/wms/stock/page',
    method: 'get',
    params: query
  })
}

// 查询库存详细
export function getStock(id) {
  return request({
    url: '/wms/stock/' + id,
    method: 'get'
  })
}

// 根据仓库和SKU查询库存
export function getStockByWarehouseAndSku(warehouseId, skuId) {
  return request({
    url: '/wms/stock/byWarehouseAndSku',
    method: 'get',
    params: { warehouseId, skuId }
  })
}
