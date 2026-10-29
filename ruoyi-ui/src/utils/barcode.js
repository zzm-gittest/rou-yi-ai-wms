/**
 * 轻量级 Code128B 条码生成器
 * 输出内联 SVG 字符串，不依赖任何外部库
 * 用于 PC 端打印页面显示单据号条码
 */

const CODE128B_PATTERNS = [
  '212222','222122','222221','121223','121322','131222','122213','122312','132212','221213',
  '221312','231212','112232','122132','122231','113222','123122','123221','223211','221132',
  '221231','213212','223112','312131','311222','321122','321221','312212','322112','322211',
  '212123','212321','232121','111323','131123','131321','112313','132113','132311','211313',
  '231113','231311','112133','112331','132131','113123','113321','133121','313121','211331',
  '231131','213113','213311','213131','311123','311321','331121','312113','312311','332111',
  '314111','221411','431111','111224','111422','121124','121421','141122','141221','112214',
  '112412','122114','122411','142112','142211','241211','221114','413111','241112','134111',
  '111242','121142','121241','114212','124112','124211','411212','421112','421211','212141',
  '214121','412121','111143','111341','131141','114113','114311','411113','411311','113141',
  '114131','311141','411131','211412','211214','211232','2331112'
]

const START_B = 104
const STOP = 106

function encodeCode128B(text) {
  const codes = [START_B]
  let checksum = START_B
  for (let i = 0; i < text.length; i++) {
    const code = text.charCodeAt(i) - 32
    if (code < 0 || code > 94) continue
    codes.push(code)
    checksum += code * (i + 1)
  }
  codes.push(checksum % 103)
  codes.push(STOP)
  return codes
}

/**
 * 生成条码 SVG 字符串
 * @param {string} text - 要编码的文本（单据号）
 * @param {object} options - 可选配置
 * @returns {string} SVG 字符串
 */
export function generateBarcodeSvg(text, options = {}) {
  if (!text) return ''

  const height = options.height || 40
  const moduleWidth = options.moduleWidth || 1.5
  const showText = options.showText !== false
  const fontSize = options.fontSize || 10
  const textHeight = showText ? fontSize + 4 : 0

  const codes = encodeCode128B(text)
  const bars = []
  codes.forEach(code => {
    const pattern = CODE128B_PATTERNS[code]
    if (!pattern) return
    for (let j = 0; j < pattern.length; j++) {
      bars.push({ width: parseInt(pattern[j], 10), isBar: j % 2 === 0 })
    }
  })

  const totalWidth = bars.reduce((sum, b) => sum + b.width, 0) * moduleWidth
  const svgHeight = height + textHeight
  const padding = 8

  let x = 0
  let rects = ''
  bars.forEach(b => {
    const w = b.width * moduleWidth
    if (b.isBar) {
      rects += `<rect x="${padding + x}" y="0" width="${w}" height="${height}" fill="#000"/>`
    }
    x += w
  })

  const svgWidth = totalWidth + padding * 2
  let textEl = ''
  if (showText) {
    textEl = `<text x="${svgWidth / 2}" y="${height + fontSize + 1}" text-anchor="middle" font-family="monospace,sans-serif" font-size="${fontSize}" fill="#000">${escapeXml(text)}</text>`
  }

  return `<svg xmlns="http://www.w3.org/2000/svg" width="${svgWidth}" height="${svgHeight}" viewBox="0 0 ${svgWidth} ${svgHeight}">${rects}${textEl}</svg>`
}

function escapeXml(str) {
  return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
}
