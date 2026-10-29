import request from '@/utils/request'

// 查询费用分页列表
export function pageExpense(query) {
  return request({
    url: '/wms/expense/page',
    method: 'get',
    params: query
  })
}

// 查询费用详细
export function getExpense(id) {
  return request({
    url: '/wms/expense/' + id,
    method: 'get'
  })
}

// 新增费用
export function addExpense(data) {
  return request({
    url: '/wms/expense',
    method: 'post',
    data: data
  })
}

// 修改费用
export function updateExpense(data) {
  return request({
    url: '/wms/expense',
    method: 'put',
    data: data
  })
}

// 删除费用
export function delExpense(id) {
  return request({
    url: '/wms/expense/' + id,
    method: 'delete'
  })
}

// 审核费用
export function approveExpense(id) {
  return request({
    url: '/wms/expense/approve/' + id,
    method: 'post'
  })
}
