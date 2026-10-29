import request from '@/utils/request'

// 获取概览统计
export function getOverview() {
  return request({
    url: '/wms/statistics/overview',
    method: 'get'
  })
}

// 获取销售趋势
export function getSalesTrend() {
  return request({
    url: '/wms/statistics/salesTrend',
    method: 'get'
  })
}

// 获取采购趋势
export function getPurchaseTrend() {
  return request({
    url: '/wms/statistics/purchaseTrend',
    method: 'get'
  })
}

// 获取库存Top
export function getStockTop() {
  return request({
    url: '/wms/statistics/stockTop',
    method: 'get'
  })
}

// 获取库存预警
export function getStockWarning() {
  return request({
    url: '/wms/statistics/stockWarning',
    method: 'get'
  })
}

// 获取热销商品
export function getTopSellingGoods() {
  return request({
    url: '/wms/statistics/topSellingGoods',
    method: 'get'
  })
}

// 获取仓库库存分布
export function getWarehouseStock() {
  return request({
    url: '/wms/statistics/warehouseStock',
    method: 'get'
  })
}

// 获取财务汇总
export function getFinanceSummary() {
  return request({
    url: '/wms/statistics/financeSummary',
    method: 'get'
  })
}

// 获取待办事项
export function getPendingTasks() {
  return request({
    url: '/wms/statistics/pendingTasks',
    method: 'get'
  })
}
