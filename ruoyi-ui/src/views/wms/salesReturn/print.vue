<template>
  <div class="print-bill-page" v-loading="loading">
    <div class="print-toolbar" v-if="!loading && !errorMsg">
      <el-button type="primary" icon="el-icon-printer" @click="handlePrint">打印</el-button>
      <el-button icon="el-icon-close" @click="handleClose">关闭</el-button>
    </div>

    <el-alert
      v-if="errorMsg"
      :title="errorMsg"
      type="error"
      :closable="false"
      show-icon
    />

    <div v-else class="print-bill-pages">
      <div v-for="(pageItems, pageIndex) in pagedItems" :key="pageIndex" class="print-bill-sheet">
        <div class="print-bill-head-top">
          <div class="print-bill-head-center">
            <div class="print-bill-head-company">{{ bill.companyName || '-' }}</div>
            <div class="print-bill-head-title">{{ bill.title || '销售退货单' }}</div>
          </div>
          <div class="print-bill-head-barcode">
            <print-barcode :value="bill.billNo" :height="24" :module-width="0.8" :font-size="7" />
          </div>
        </div>
        <div class="print-bill-head-meta">
          <span>单据编号：{{ bill.billNo || '-' }}</span>
          <span>单据日期：{{ bill.billDate || '-' }}</span>
        </div>

        <table class="print-bill-table">
          <colgroup>
            <col style="width:7%"><col style="width:21%"><col style="width:21%">
            <col style="width:10%"><col style="width:8%"><col style="width:13%"><col style="width:20%">
          </colgroup>
          <thead>
            <tr>
              <th class="center">序号</th>
              <th class="left">商品名称</th>
              <th class="left">商品条码</th>
              <th class="right">数量</th>
              <th class="left">单位</th>
              <th class="right">单价</th>
              <th class="right">金额</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="pageItems.length === 0">
              <td colspan="7" class="print-bill-empty">暂无明细</td>
            </tr>
            <tr v-for="(item, rowIndex) in pageItems" :key="`${pageIndex}-${rowIndex}`">
              <td class="center">{{ pageIndex * pageSize + rowIndex + 1 }}</td>
              <td class="left">
                <div class="name-main">{{ item.goodsName || '-' }}</div>
                <div v-if="isMultiSpec(item) && item.skuName" class="sku-sub">{{ item.skuName }}</div>
              </td>
              <td class="left">{{ item.barcode || '' }}</td>
              <td class="right">{{ formatQty(item.quantity) }}</td>
              <td class="left">{{ item.unitName || '' }}</td>
              <td class="right">{{ formatMoney(item.price) }}</td>
              <td class="right">{{ formatMoney(item.amount) }}</td>
            </tr>
            <tr v-if="pageIndex === pagedItems.length - 1" class="print-bill-summary-row">
              <td></td>
              <td class="left">合计</td>
              <td></td>
              <td class="right">{{ formatQty(totalQuantity) }}</td>
              <td></td>
              <td></td>
              <td class="right">{{ formatMoney(bill.totalAmount) }}</td>
            </tr>
          </tbody>
        </table>

        <div v-if="pageIndex === pagedItems.length - 1" class="print-bill-footer">
          <div v-if="bill.remark" class="print-bill-info-line">备注：{{ bill.remark }}</div>
          <div v-if="bill.companyAddress" class="print-bill-info-line">地址：{{ bill.companyAddress }}</div>
          <div v-if="bill.companyPhone" class="print-bill-info-line">电话：{{ bill.companyPhone }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getSalesReturnPrint } from '@/api/wms/salesReturn'
import PrintBarcode from '@/components/PrintBarcode'

export default {
  name: 'SalesReturnPrint',
  components: { PrintBarcode },
  data() {
    return {
      pageSize: 20,
      loading: false,
      errorMsg: '',
      bill: null
    }
  },
  computed: {
    pagedItems() {
      const items = this.bill && Array.isArray(this.bill.items) ? this.bill.items : []
      if (items.length === 0) {
        return [[]]
      }
      const pages = []
      for (let index = 0; index < items.length; index += this.pageSize) {
        pages.push(items.slice(index, index + this.pageSize))
      }
      return pages
    },
    totalQuantity() {
      const items = this.bill && Array.isArray(this.bill.items) ? this.bill.items : []
      return items.reduce((sum, item) => sum + Number(item.quantity || 0), 0)
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    isMultiSpec(item) {
      if (!item) return false
      if (item.hasSpec === '1') return true
      if (item.hasSpec === '0') return false
      const spec = (item.specInfo || '').trim()
      return spec !== '' && spec !== '无规格' && spec !== '默认规格'
    },
    formatQty(value) {
      const num = Number(value || 0)
      return Number.isFinite(num) ? num.toFixed(2) : '0.00'
    },
    loadData() {
      const id = this.$route.query.id
      if (!id) {
        this.errorMsg = '缺少单据ID'
        return
      }
      this.loading = true
      getSalesReturnPrint(id).then(response => {
        this.bill = response.data || {}
        document.title = `${this.bill.title || '销售退货单'}-${this.bill.billNo || id}`
      }).catch(error => {
        this.errorMsg = error && error.message ? error.message : '打印数据加载失败'
      }).finally(() => {
        this.loading = false
      })
    },
    handlePrint() {
      window.print()
    },
    handleClose() {
      if (window.opener && !window.opener.closed) {
        window.close()
        return
      }
      const from = this.$route.query.from
      if (from) {
        this.$router.replace(from)
        return
      }
      if (window.history.length > 1) {
        this.$router.go(-1)
        return
      }
      this.$router.replace('/index')
    },
    formatMoney(value) {
      const amount = Number(value || 0)
      return amount.toFixed(2)
    },
    formatDateTime(value) {
      if (!value) {
        return '-'
      }
      return this.parseTime(value, '{y}-{m}-{d} {h}:{i}:{s}')
    }
  }
}
</script>

<style lang="scss">
@import '~@/assets/styles/print-bill.scss';
</style>

