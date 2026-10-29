import request from '@/utils/request'

// 查询商品分类树列表
export function treeGoodsCategory(query) {
  return request({
    url: '/wms/goodsCategory/tree',
    method: 'get',
    params: query
  })
}

// 查询商品分类列表（不分页）
export function listGoodsCategory() {
  return request({
    url: '/wms/goodsCategory/list',
    method: 'get'
  })
}

// 查询商品分类详细
export function getGoodsCategory(id) {
  return request({
    url: '/wms/goodsCategory/' + id,
    method: 'get'
  })
}

// 新增商品分类
export function addGoodsCategory(data) {
  return request({
    url: '/wms/goodsCategory',
    method: 'post',
    data: data
  })
}

// 修改商品分类
export function updateGoodsCategory(data) {
  return request({
    url: '/wms/goodsCategory',
    method: 'put',
    data: data
  })
}

// 删除商品分类
export function delGoodsCategory(id) {
  return request({
    url: '/wms/goodsCategory/' + id,
    method: 'delete'
  })
}
