import request from '@/utils/request'

// 查询应收账款分页列表
export function pageAr(query) {
  return request({
    url: '/wms/accountsReceivable/page',
    method: 'get',
    params: query
  })
}

// 查询应收账款详情
export function getAr(id) {
  return request({
    url: '/wms/accountsReceivable/' + id,
    method: 'get'
  })
}

// 查询应收账款打印数据
export function getArPrint(id) {
  return request({
    url: '/wms/accountsReceivable/' + id,
    method: 'get'
  })
}

// 收款
export function collectAr(data) {
  return request({
    url: '/wms/accountsReceivable/collect',
    method: 'post',
    data: data
  })
}
