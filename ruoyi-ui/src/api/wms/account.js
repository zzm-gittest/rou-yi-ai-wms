import request from '@/utils/request'

// 查询结算账户分页列表
export function pageAccount(query) {
  return request({
    url: '/wms/account/page',
    method: 'get',
    params: query
  })
}

// 查询结算账户列表
export function listAccount() {
  return request({
    url: '/wms/account/list',
    method: 'get'
  })
}

// 查询结算账户详细
export function getAccount(id) {
  return request({
    url: '/wms/account/' + id,
    method: 'get'
  })
}

// 新增结算账户
export function addAccount(data) {
  return request({
    url: '/wms/account',
    method: 'post',
    data: data
  })
}

// 修改结算账户
export function updateAccount(data) {
  return request({
    url: '/wms/account',
    method: 'put',
    data: data
  })
}

// 删除结算账户
export function delAccount(id) {
  return request({
    url: '/wms/account/' + id,
    method: 'delete'
  })
}
