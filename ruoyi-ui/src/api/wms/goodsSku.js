import request from '@/utils/request'

// 查询商品SKU分页列表
export function pageGoodsSku(query) {
  const pageNum = Number(query?.pageNum) || 1
  const pageSize = Number(query?.pageSize) || 10
  const params = {
    ...query,
    pageNum,
    pageSize,
    // 兼容 MyBatis-Plus 风格分页参数
    current: pageNum,
    size: pageSize
  }
  return request({
    url: '/wms/goodsSku/page',
    method: 'get',
    params
  })
}

// 查询商品SKU列表（按商品ID）
export function listGoodsSku(goodsId) {
  return request({
    url: '/wms/goodsSku/list/' + goodsId,
    method: 'get'
  })
}

// 查询商品SKU详细
export function getGoodsSku(id) {
  return request({
    url: '/wms/goodsSku/' + id,
    method: 'get'
  })
}

// 新增商品SKU
export function addGoodsSku(data) {
  return request({
    url: '/wms/goodsSku',
    method: 'post',
    data: data
  })
}

// 修改商品SKU
export function updateGoodsSku(data) {
  return request({
    url: '/wms/goodsSku',
    method: 'put',
    data: data
  })
}

// 删除商品SKU
export function delGoodsSku(id) {
  return request({
    url: '/wms/goodsSku/' + id,
    method: 'delete'
  })
}
