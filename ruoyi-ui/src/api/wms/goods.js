import request from '@/utils/request'

// 查询商品分页列表
export function pageGoods(query) {
  return request({
    url: '/wms/goods/page',
    method: 'get',
    params: query
  })
}

// 查询商品列表（不分页）
export function listGoods() {
  return request({
    url: '/wms/goods/list',
    method: 'get'
  })
}

// 查询商品详细
export function getGoods(id) {
  return request({
    url: '/wms/goods/' + id,
    method: 'get'
  })
}

// 查询商品详情（含SKU列表）
export function getGoodsDetail(id) {
  return request({
    url: '/wms/goods/detail/' + id,
    method: 'get'
  })
}

// 新增商品
export function addGoods(data) {
  return request({
    url: '/wms/goods',
    method: 'post',
    data: data
  })
}

// 修改商品
export function updateGoods(data) {
  return request({
    url: '/wms/goods',
    method: 'put',
    data: data
  })
}

// 删除商品
export function delGoods(id) {
  return request({
    url: '/wms/goods/' + id,
    method: 'delete'
  })
}

// 导入商品
export function importGoods(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/wms/goods/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 下载导入模板
export function downloadTemplate(templateType) {
  return request({
    url: '/wms/goods/importTemplate',
    method: 'get',
    params: { type: templateType },
    responseType: 'blob'
  })
}

// 导出商品
export function exportGoods(query) {
  return request({
    url: '/wms/goods/export',
    method: 'post',
    params: query,
    responseType: 'blob',
    timeout: 120000
  })
}
