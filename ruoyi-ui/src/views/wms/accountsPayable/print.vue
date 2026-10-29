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
      <div class="print-bill-sheet">
        <div class="print-bill-head-top">
          <div class="print-bill-head-center">
            <div class="print-bill-head-company">{{ bill.companyName || '-' }}</div>
            <div class="print-bill-head-title">{{ bill.title || '应付账款结算单' }}</div>
          </div>
          <div class="print-bill-head-barcode">
            <print-barcode :value="bill.billNo" :height="24" :module-width="0.8" :font-size="7" />
          </div>
        </div>
        <div class="print-bill-head-meta">
          <span>单据编号：{{ bill.billNo || '-' }}</span>
          <span>单据日期：{{ bill.billDate || '-' }}</span>
        </div>

        <table class="finance-print-table">
          <tbody>
            <tr>
              <th>应付金额</th>
              <td>{{ formatMoney(bill.apAmount) }}</td>
              <th>已付金额</th>
              <td>{{ formatMoney(bill.paidAmount) }}</td>
            </tr>
            <tr>
              <th>未付金额</th>
              <td>{{ formatMoney(bill.unpaidAmount) }}</td>
              <th>到期日期</th>
              <td>{{ bill.dueDate || '-' }}</td>
            </tr>
            <tr>
              <th>备注</th>
              <td colspan="3">{{ bill.remark }}</td>
            </tr>
          </tbody>
        </table>

        <div class="print-bill-footer">
          <div v-if="bill.companyAddress" class="print-bill-info-line">地址：{{ bill.companyAddress }}</div>
          <div v-if="bill.companyPhone" class="print-bill-info-line">电话：{{ bill.companyPhone }}</div>
        </div>

      </div>
    </div>
  </div>
</template>

<script>
import { getApPrint } from '@/api/wms/accountsPayable'
import { getTenantPrintProfile } from '@/api/system/tenant'
import PrintBarcode from '@/components/PrintBarcode'

export default {
  name: 'AccountsPayablePrint',
  components: { PrintBarcode },
  data() {
    return {
      loading: false,
      errorMsg: '',
      bill: null
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    loadData() {
      const id = this.$route.query.id
      if (!id) {
        this.errorMsg = '缺少单据ID'
        return
      }
      this.loading = true
      Promise.all([getApPrint(id), getTenantPrintProfile()]).then(([response, profileRes]) => {
        const detail = response.data || {}
        const profile = profileRes.data || {}
        this.bill = this.buildBill(detail)
        this.bill.companyName = profile.printCompanyName
        this.bill.companyAddress = profile.printCompanyAddress
        this.bill.companyPhone = profile.printCompanyPhone
        document.title = `${this.bill.title}-${this.bill.billNo || id}`
      }).catch(error => {
        this.errorMsg = error && error.message ? error.message : '打印数据加载失败'
      }).finally(() => {
        this.loading = false
      })
    },
    buildBill(detail) {
      const apAmount = Number(detail.apAmount || 0)
      const paidAmount = Number(detail.paidAmount || 0)
      return {
        title: '应付账款结算单',
        billNo: detail.billNo,
        billDate: this.formatDate(detail.billDate),
        billTypeName: detail.billTypeName,
        counterpartName: detail.billType === '3' ? (detail.customerName || '-') : (detail.supplierName || '-'),
        statusName: detail.apStatus === '1' ? '已结清' : '未结清',
        apAmount,
        paidAmount,
        unpaidAmount: apAmount - paidAmount,
        dueDate: this.formatDate(detail.dueDate),
        remark: detail.remark,
        handler: detail.handler,
        approveBy: detail.approveBy,
        printBy: this.$store && this.$store.getters ? this.$store.getters.name : '-',
        printTime: new Date()
      }
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
      return Number(value || 0).toFixed(2)
    },
    formatDate(value) {
      if (!value) {
        return '-'
      }
      return this.parseTime(value, '{y}-{m}-{d}')
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

.finance-print-table {
  width: 100%;
  border-collapse: collapse;
  margin: 0;

  th,
  td {
    border: 1px solid #888;
    padding: 4pt 5pt;
    font-size: 9pt;
    line-height: 1.4;
    vertical-align: top;
  }

  th {
    width: 120px;
    text-align: left;
    background: #eee;
    font-weight: 600;
  }
}
</style>

