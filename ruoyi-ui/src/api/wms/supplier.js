import request from '@/utils/request'

// 查询供应商分页列表
export function pageSupplier(query) {
  return request({
    url: '/wms/supplier/page',
    method: 'get',
    params: query
  })
}

// 查询供应商列表（不分页）
export function listSupplier() {
  return request({
    url: '/wms/supplier/list',
    method: 'get'
  })
}

// 查询供应商详细
export function getSupplier(id) {
  return request({
    url: '/wms/supplier/' + id,
    method: 'get'
  })
}

// 新增供应商
export function addSupplier(data) {
  return request({
    url: '/wms/supplier',
    method: 'post',
    data: data
  })
}

// 修改供应商
export function updateSupplier(data) {
  return request({
    url: '/wms/supplier',
    method: 'put',
    data: data
  })
}

// 删除供应商
export function delSupplier(id) {
  return request({
    url: '/wms/supplier/' + id,
    method: 'delete'
  })
}
