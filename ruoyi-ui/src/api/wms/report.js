import request from '@/utils/request'

// 获取报表分类
export function getReportCategories() {
  return request({
    url: '/wms/report/categories',
    method: 'get'
  })
}

// 获取报表配置
export function getReportConfig(reportCode) {
  return request({
    url: '/wms/report/config/' + reportCode,
    method: 'get'
  })
}

// ==================== 销售报表 ====================

// 查询销售日报表
export function salesDailyReport(query) {
  return request({
    url: '/wms/report/sales/daily',
    method: 'post',
    data: query
  })
}

// 导出销售日报表
export function exportSalesDailyReport(query) {
  return request({
    url: '/wms/report/sales/daily/export',
    method: 'post',
    params: query
  })
}

// 查询商品销售排行榜
export function goodsRankReport(query) {
  return request({
    url: '/wms/report/sales/goodsRank',
    method: 'get',
    params: query
  })
}

// 导出商品销售排行榜
export function exportGoodsRankReport(query) {
  return request({
    url: '/wms/report/sales/goodsRank/export',
    method: 'post',
    params: query
  })
}

// 查询客户销售分析表
export function customerAnalysisReport(query) {
  return request({
    url: '/wms/report/sales/customerAnalysis',
    method: 'get',
    params: query
  })
}

// 导出客户销售分析表
export function exportCustomerAnalysisReport(query) {
  return request({
    url: '/wms/report/sales/customerAnalysis/export',
    method: 'post',
    params: query
  })
}

// ==================== 库存报表 ====================

// 查询库存进销存报表
export function stockPsiReport(query) {
  return request({
    url: '/wms/report/stock/psi',
    method: 'get',
    params: query
  })
}

// 导出库存进销存报表
export function exportStockPsiReport(query) {
  return request({
    url: '/wms/report/stock/psi/export',
    method: 'post',
    params: query
  })
}

// ==================== 财务报表 ====================

// 查询应收账款账龄分析表
export function arAgingReport(query) {
  return request({
    url: '/wms/report/finance/arAging',
    method: 'get',
    params: query
  })
}

// 导出应收账款账龄分析表
export function exportArAgingReport(query) {
  return request({
    url: '/wms/report/finance/arAging/export',
    method: 'post',
    params: query
  })
}

// 查询供应商采购分析表
export function supplierAnalysisReport(query) {
  return request({
    url: '/wms/report/purchase/supplierAnalysis',
    method: 'get',
    params: query
  })
}

// 导出供应商采购分析表
export function exportSupplierAnalysisReport(query) {
  return request({
    url: '/wms/report/purchase/supplierAnalysis/export',
    method: 'post',
    params: query
  })
}
