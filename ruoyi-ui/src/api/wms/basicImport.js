import request from '@/utils/request'

export function importBasicData(type, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/wms/basicImport/import/' + type,
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function downloadBasicTemplate(type, goodsTemplateType) {
  return request({
    url: '/wms/basicImport/template/' + type,
    method: 'get',
    params: { goodsTemplateType: goodsTemplateType },
    responseType: 'blob'
  })
}

export function downloadBasicTemplatePackage() {
  return request({
    url: '/wms/basicImport/template/package',
    method: 'get',
    responseType: 'blob'
  })
}
