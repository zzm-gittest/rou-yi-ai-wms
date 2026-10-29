/**
 * 统一商品展示名工具（PC端）
 * 规则：skuName 去空 + 去重（等于 goodsName 则视为空）
 *   displayName = suffix 为空 ? goodsName : goodsName（suffix）
 */

/**
 * 获取商品展示名称
 * @param {Object} item - 含 goodsName / skuName 的对象
 * @returns {string}
 */
export function formatGoodsDisplayName(item) {
  if (!item) return '-'
  const g = (item.goodsName || '').trim()
  const s = (item.skuName || '').trim()
  if (!g && !s) return '-'
  if (!g) return s
  if (!s || s === g) return g
  return `${g}（${s}）`
}
