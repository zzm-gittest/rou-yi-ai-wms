import request from '@/utils/request'

export function getProgress() {
  return request({
    url: '/wms/onboarding/progress',
    method: 'get'
  })
}

export function completeTask(taskCode) {
  return request({
    url: '/wms/onboarding/complete/' + taskCode,
    method: 'post'
  })
}

export function shouldShow() {
  return request({
    url: '/wms/onboarding/should-show',
    method: 'get'
  })
}
