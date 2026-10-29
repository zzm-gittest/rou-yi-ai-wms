import request from '@/utils/request'

export function getPcPurchaseOrderActions() {
  return request({
    url: '/wms/ai/pc/actions',
    method: 'get'
  })
}

export function startPcPurchaseOrderChat() {
  return request({
    url: '/wms/ai/pc/start',
    method: 'post'
  })
}

export function chatPcPurchaseOrder(data) {
  return request({
    url: '/wms/ai/pc/chat',
    method: 'post',
    data,
    // 文本对话会调用本地NLU/大模型解析，耗时可能超过默认10秒
    timeout: 60000
  })
}

export function clearPcPurchaseOrderContext(conversationId) {
  return request({
    url: '/wms/ai/pc/context/' + conversationId,
    method: 'delete'
  })
}

export function imageChatPc(data) {
  return request({
    url: '/wms/ai/pc/imageChat',
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data' },
    // 截图识别需先调用视觉大模型识别图片再解析，耗时较长，避免默认10秒超时
    timeout: 120000
  })
}
