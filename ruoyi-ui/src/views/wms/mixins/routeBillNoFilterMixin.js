/**
 * 飞书/AIGW PC 链接携带 ?orderNo= 或 ?billNo= 时，打开单据列表页自动填入对应查询字段并搜索。
 * 各列表页在 data 中设置 routeBillNoQueryKey（如 outNo、orderNo、inNo）。
 */
export default {
  watch: {
    '$route.query.orderNo'(val) {
      this._applyRouteBillNoWatch(val)
    },
    '$route.query.billNo'(val) {
      this._applyRouteBillNoWatch(val)
    }
  },
  methods: {
    applyBillNoFromRoute() {
      const field = this.routeBillNoQueryKey
      if (!field || !this.queryParams) {
        return false
      }
      const query = this.$route && this.$route.query
      const raw = query
        ? (query.orderNo != null ? query.orderNo : query.billNo)
        : null
      if (raw == null || String(raw).trim() === '') {
        return false
      }
      this.$set(this.queryParams, field, String(raw).trim())
      if (this.showSearch !== undefined) {
        this.showSearch = true
      }
      return true
    },
    _applyRouteBillNoWatch(val) {
      const field = this.routeBillNoQueryKey
      if (!field || !this.queryParams) {
        return
      }
      const next = val != null ? String(val).trim() : ''
      const current = this.queryParams[field] != null ? String(this.queryParams[field]).trim() : ''
      if (next && next !== current) {
        this.$set(this.queryParams, field, next)
        if (this.showSearch !== undefined) {
          this.showSearch = true
        }
        if (typeof this.handleQuery === 'function') {
          this.handleQuery()
        } else if (typeof this.getList === 'function') {
          this.getList()
        }
      }
    }
  }
}
