import request from '@/utils/request'

// 查询计量单位分页列表
export function pageUnit(query) {
  return request({
    url: '/wms/unit/page',
    method: 'get',
    params: query
  })
}

// 查询计量单位列表（不分页）
export function listUnit() {
  return request({
    url: '/wms/unit/list',
    method: 'get'
  })
}

// 查询计量单位详细
export function getUnit(id) {
  return request({
    url: '/wms/unit/' + id,
    method: 'get'
  })
}

// 新增计量单位
export function addUnit(data) {
  return request({
    url: '/wms/unit',
    method: 'post',
    data: data
  })
}

// 修改计量单位
export function updateUnit(data) {
  return request({
    url: '/wms/unit',
    method: 'put',
    data: data
  })
}

// 删除计量单位
export function delUnit(id) {
  return request({
    url: '/wms/unit/' + id,
    method: 'delete'
  })
}
