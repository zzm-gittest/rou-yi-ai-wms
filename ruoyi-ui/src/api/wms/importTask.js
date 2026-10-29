import request from '@/utils/request'

export function createImportTask(data) {
  return request({
    url: '/wms/import/task/create',
    method: 'post',
    data: data
  })
}

export function pageImportTask(params) {
  return request({
    url: '/wms/import/task/page',
    method: 'get',
    params: params
  })
}

export function previewImportTask(taskId, file, goodsTemplateType, mappingJson) {
  const formData = new FormData()
  formData.append('file', file)
  if (goodsTemplateType) {
    formData.append('goodsTemplateType', goodsTemplateType)
  }
  if (mappingJson) {
    formData.append('mappingJson', mappingJson)
  }
  return request({
    url: '/wms/import/task/' + taskId + '/preview',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 600000
  })
}

export function executeImportTask(taskId) {
  return request({
    url: '/wms/import/task/' + taskId + '/execute',
    method: 'post'
  })
}

export function retryImportTask(taskId, file, goodsTemplateType, mappingJson) {
  const formData = new FormData()
  formData.append('file', file)
  if (goodsTemplateType) {
    formData.append('goodsTemplateType', goodsTemplateType)
  }
  if (mappingJson) {
    formData.append('mappingJson', mappingJson)
  }
  return request({
    url: '/wms/import/task/' + taskId + '/retry',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 600000
  })
}

export function getImportTaskReport(taskId) {
  return request({
    url: '/wms/import/task/' + taskId + '/report',
    method: 'get'
  })
}

export function parseImportHeaders(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/wms/import/task/headers',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
