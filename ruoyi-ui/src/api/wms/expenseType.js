import request from '@/utils/request'

// 查询费用类型分页列表
export function pageExpenseType(query) {
  return request({
    url: '/wms/expenseType/page',
    method: 'get',
    params: query
  })
}

// 查询费用类型列表
export function listExpenseType() {
  return request({
    url: '/wms/expenseType/list',
    method: 'get'
  })
}

// 查询费用类型详细
export function getExpenseType(id) {
  return request({
    url: '/wms/expenseType/' + id,
    method: 'get'
  })
}

// 新增费用类型
export function addExpenseType(data) {
  return request({
    url: '/wms/expenseType',
    method: 'post',
    data: data
  })
}

// 修改费用类型
export function updateExpenseType(data) {
  return request({
    url: '/wms/expenseType',
    method: 'put',
    data: data
  })
}

// 删除费用类型
export function delExpenseType(id) {
  return request({
    url: '/wms/expenseType/' + id,
    method: 'delete'
  })
}
