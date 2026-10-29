import request from '@/utils/request'

// 查询应付账款分页列表
export function pageAp(query) {
  return request({
    url: '/wms/accountsPayable/page',
    method: 'get',
    params: query
  })
}

// 查询应付账款详情
export function getAp(id) {
  return request({
    url: '/wms/accountsPayable/' + id,
    method: 'get'
  })
}

// 查询应付账款打印数据
export function getApPrint(id) {
  return request({
    url: '/wms/accountsPayable/' + id,
    method: 'get'
  })
}

// 付款
export function payAp(data) {
  return request({
    url: '/wms/accountsPayable/pay',
    method: 'post',
    data: data
  })
}
