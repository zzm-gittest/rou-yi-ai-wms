import request from '@/utils/request'

/**
 * 识别商品信息
 * @param {FormData} data 图片文件
 */
export function recognizeGoods(data) {
  return request({
    url: '/wms/ocr/recognizeGoods',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 识别采购订单
 * @param {FormData} data 图片文件
 */
export function recognizePurchaseOrder(data) {
  return request({
    url: '/wms/ocr/recognizePurchaseOrder',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 识别条码
 * @param {FormData} data 图片文件
 */
export function recognizeBarcode(data) {
  return request({
    url: '/wms/ocr/recognizeBarcode',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
