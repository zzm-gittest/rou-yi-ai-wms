<template>
  <div class="print-barcode-wrapper" v-if="svgContent" v-html="svgContent"></div>
</template>

<script>
import { generateBarcodeSvg } from '@/utils/barcode'

export default {
  name: 'PrintBarcode',
  props: {
    value: { type: String, default: '' },
    height: { type: Number, default: 40 },
    moduleWidth: { type: Number, default: 1.5 },
    showText: { type: Boolean, default: true },
    fontSize: { type: Number, default: 10 }
  },
  computed: {
    svgContent() {
      return generateBarcodeSvg(this.value, {
        height: this.height,
        moduleWidth: this.moduleWidth,
        showText: this.showText,
        fontSize: this.fontSize
      })
    }
  }
}
</script>

<style scoped>
.print-barcode-wrapper {
  display: inline-block;
  line-height: 0;
}
</style>
