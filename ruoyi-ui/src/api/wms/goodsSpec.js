import request from '@/utils/request'

// 查询商品规格列表
export function listGoodsSpec(goodsId) {
  return request({
    url: '/wms/goodsSpec/list',
    method: 'get',
    params: { goodsId }
  })
}

// 查询商品规格详细
export function getGoodsSpec(id) {
  return request({
    url: '/wms/goodsSpec/' + id,
    method: 'get'
  })
}

// 新增商品规格
export function addGoodsSpec(data) {
  return request({
    url: '/wms/goodsSpec',
    method: 'post',
    data: data
  })
}

// 修改商品规格
export function updateGoodsSpec(data) {
  return request({
    url: '/wms/goodsSpec',
    method: 'put',
    data: data
  })
}

// 删除商品规格
export function delGoodsSpec(id) {
  return request({
    url: '/wms/goodsSpec/' + id,
    method: 'delete'
  })
}
