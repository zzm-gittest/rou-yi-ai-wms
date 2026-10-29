import request from '@/utils/request'

/**
 * 分页查询WMS业务操作日志
 */
export function pageBizOperLog(query) {
  return request({
    url: '/wms/bizLog/page',
    method: 'get',
    params: query
  })
}
