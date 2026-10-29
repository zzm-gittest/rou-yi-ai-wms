import { getAvailablePurchaseInForReturn } from '@/api/wms/purchaseIn'
import { getAvailableSalesOrderForOut } from '@/api/wms/salesOrder'
import { getAvailableSalesOutForReturn } from '@/api/wms/salesOut'

export default {
  methods: {
    isMultiCandidateCard(card) {
      return !!card && card.type === 'CANDIDATE' && card.selectionMode === 'multi-list'
    },
    getCandidateSelectionCacheKey(card) {
      if (!card || card.type !== 'CANDIDATE') return ''
      const otherValue = card.otherOption && card.otherOption.value
      if (typeof otherValue === 'string' && otherValue.indexOf('__SELECT__GOODS:') === 0) {
        return 'goods'
      }
      return ''
    },
    candidateOptionId(opt) {
      if (!opt) return ''
      const value = typeof opt === 'string' ? opt : opt.value
      if (!value) return ''
      if (String(value).indexOf('__SELECT__GOODS:') === 0) {
        return String(value).substring('__SELECT__GOODS:'.length)
      }
      return String(value)
    },
    candidateOptionLabel(opt) {
      if (!opt) return '-'
      return opt.label || opt.name || this.candidateOptionId(opt) || '-'
    },
    candidateDisplayOptions(card) {
      if (!card || !Array.isArray(card.options)) return []
      if (card.remoteSearch) return card.options
      const keyword = String(card.searchKeyword || '').trim().toLowerCase()
      if (!keyword) return card.options
      return card.options.filter(opt => this.candidateOptionLabel(opt).toLowerCase().includes(keyword))
    },
    extractCandidateSourceId(opt) {
      if (!opt) return null
      if (opt.id) return Number(opt.id)
      const value = String(opt.value || '')
      if (value.indexOf('__SELECT__LINK_ORDER:') === 0) {
        return Number(value.substring('__SELECT__LINK_ORDER:'.length))
      }
      return null
    },
    handleCandidateSearchInput(msg) {
      if (!msg || !msg.card || !msg.card.remoteSearch) return
      if (this._remoteSearchTimer) {
        clearTimeout(this._remoteSearchTimer)
      }
      this._remoteSearchTimer = setTimeout(() => {
        this.remoteSearchCandidates(msg)
      }, 300)
    },
    remoteSearchCandidates(msg) {
      if (!msg || !msg.card || !msg.card.remoteSearch) return
      const keyword = String(msg.card.searchKeyword || '').trim()
      if (!keyword) {
        this.$set(msg.card, 'options', [])
        this.$set(msg.card, 'totalCount', 0)
        return
      }
      if (msg.card.remoteSearchDomain === 'PURCHASE_RETURN') {
        this.$set(msg.card, '_searching', true)
        getAvailablePurchaseInForReturn(keyword).then(res => {
          const list = (res.data || []).map(item => ({
            label: item.inNo + ' / ' + (item.supplierName || '未知供应商'),
            value: '__SELECT__LINK_ORDER:' + item.id,
            id: item.id
          }))
          this.$set(msg.card, 'options', list)
          this.$set(msg.card, 'totalCount', list.length)
        }).catch((e) => {
          console.error('远程搜索失败', e)
          this.$message.error('搜索失败，请重试')
        }).finally(() => {
          this.$set(msg.card, '_searching', false)
        })
      } else if (msg.card.remoteSearchDomain === 'SALES_OUT') {
        this.$set(msg.card, '_searching', true)
        getAvailableSalesOrderForOut(keyword).then(res => {
          const list = (res.data || []).map(item => ({
            label: item.order_no + ' / ' + (item.customer_name || '未知客户'),
            value: '__SELECT__LINK_ORDER:' + item.id,
            id: item.id
          }))
          this.$set(msg.card, 'options', list)
          this.$set(msg.card, 'totalCount', list.length)
        }).catch((e) => {
          console.error('远程搜索失败', e)
          this.$message.error('搜索失败，请重试')
        }).finally(() => {
          this.$set(msg.card, '_searching', false)
        })
      } else if (msg.card.remoteSearchDomain === 'SALES_RETURN') {
        this.$set(msg.card, '_searching', true)
        getAvailableSalesOutForReturn(keyword).then(res => {
          const list = (res.data || []).map(item => ({
            label: item.outNo + ' / ' + (item.customerName || '未知客户'),
            value: '__SELECT__LINK_ORDER:' + item.id,
            id: item.id
          }))
          this.$set(msg.card, 'options', list)
          this.$set(msg.card, 'totalCount', list.length)
        }).catch((e) => {
          console.error('远程搜索失败', e)
          this.$message.error('搜索失败，请重试')
        }).finally(() => {
          this.$set(msg.card, '_searching', false)
        })
      }
    },
    canViewCandidateDetail(card, opt) {
      return !!card && (card.sourceDomainType === 'PURCHASE_ORDER' || card.sourceDomainType === 'SALES_ORDER' || card.sourceDomainType === 'SALES_OUT') && !!this.extractCandidateSourceId(opt)
    },
    getCachedCandidateSelections(card) {
      const key = this.getCandidateSelectionCacheKey(card)
      if (!key) return []
      const list = this.candidateSelectionCache && Array.isArray(this.candidateSelectionCache[key])
        ? this.candidateSelectionCache[key]
        : []
      return Array.from(new Set(list.map(item => String(item)).filter(Boolean)))
    },
    syncCandidateSelectionCache(card) {
      const key = this.getCandidateSelectionCacheKey(card)
      if (!key) return
      const selected = Array.isArray(card.selectedSkuIds)
        ? Array.from(new Set(card.selectedSkuIds.map(item => String(item)).filter(Boolean)))
        : []
      this.$set(this.candidateSelectionCache, key, selected)
      this.syncConversationSnapshot()
    },
    handleCandidateSelectionChange(card) {
      if (!this.isMultiCandidateCard(card)) return
      this.syncCandidateSelectionCache(card)
    },
    currentPageCandidateIds(card) {
      if (!card || !Array.isArray(card.options)) return []
      return card.options.map(opt => this.candidateOptionId(opt)).filter(Boolean)
    },
    hasCurrentPageCandidate(card) {
      return this.currentPageCandidateIds(card).length > 0
    },
    hasCurrentPageSelected(card) {
      if (!this.isMultiCandidateCard(card)) return false
      const selected = new Set((card.selectedSkuIds || []).map(item => String(item)))
      return this.currentPageCandidateIds(card).some(id => selected.has(String(id)))
    },
    selectAllCurrentPage(msg) {
      if (!msg || !msg.card || !this.isMultiCandidateCard(msg.card)) return
      const merged = new Set((msg.card.selectedSkuIds || []).map(item => String(item)))
      this.currentPageCandidateIds(msg.card).forEach(id => merged.add(String(id)))
      this.$set(msg.card, 'selectedSkuIds', Array.from(merged))
      this.syncCandidateSelectionCache(msg.card)
    },
    clearCurrentPageSelection(msg) {
      if (!msg || !msg.card || !this.isMultiCandidateCard(msg.card)) return
      const currentPageIds = new Set(this.currentPageCandidateIds(msg.card).map(item => String(item)))
      const kept = (msg.card.selectedSkuIds || []).map(item => String(item)).filter(id => !currentPageIds.has(id))
      this.$set(msg.card, 'selectedSkuIds', kept)
      this.syncCandidateSelectionCache(msg.card)
    },
    clearAllCandidateSelection(msg) {
      if (!msg || !msg.card || !this.isMultiCandidateCard(msg.card)) return
      this.$set(msg.card, 'selectedSkuIds', [])
      this.syncCandidateSelectionCache(msg.card)
    },
    candidateSelectedCount(card) {
      if (this.isMultiCandidateCard(card)) {
        return Array.isArray(card.selectedSkuIds) ? card.selectedSkuIds.length : 0
      }
      return card && card.selectedValue ? 1 : 0
    },
    canConfirmCandidate(card) {
      return this.candidateSelectedCount(card) > 0
    },
    confirmCandidateSelection(msg) {
      if (!msg || !msg.card) {
        this.$message.warning('请先勾选候选项')
        return
      }
      if (this.isMultiCandidateCard(msg.card)) {
        const selectedSkuIds = Array.isArray(msg.card.selectedSkuIds)
          ? Array.from(new Set(msg.card.selectedSkuIds.map(item => String(item)).filter(Boolean)))
          : []
        if (!selectedSkuIds.length) {
          this.$message.warning('请先勾选至少一个候选项')
          return
        }
        const payload = `__SELECT__GOODS:${selectedSkuIds.join(',')}`
        this.send(payload, `已确认 ${selectedSkuIds.length} 个商品候选`)
        return
      }
      if (!msg.card.selectedValue) {
        this.$message.warning('请先勾选一个候选项')
        return
      }
      const selected = (msg.card.options || []).find(item => item.value === msg.card.selectedValue)
      this.send(msg.card.selectedValue, selected ? `选择：${this.candidateOptionLabel(selected)}` : '已确认选择')
    },
    isHistoryMessage(index) {
      const latestInteractiveIndex = this.getLatestInteractiveMessageIndex()
      return latestInteractiveIndex >= 0 ? index !== latestInteractiveIndex : false
    },
    getLatestInteractiveMessageIndex() {
      for (let index = this.messages.length - 1; index >= 0; index -= 1) {
        const msg = this.messages[index]
        if (msg && msg.role === 'assistant' && msg.card) {
          return index
        }
      }
      return -1
    },
    candidateIndex(card, index) {
      return index + 1
    },
    buildCandidatePager(card) {
      const total = Number(card.totalPages || 1)
      const page = Number(card.page || 1)
      if (total <= 1) return '...'
      if (total <= 5) {
        return Array.from({ length: total }, (_, idx) => idx + 1).map(num => (num === page ? `[${num}]` : String(num))).join(' ')
      }
      if (page <= 3) {
        return `1 2 3 ... ${total}`
      }
      if (page >= total - 2) {
        return `1 ... ${total - 2} ${total - 1} ${total}`
      }
      return `1 ... ${page - 1} [${page}] ${page + 1} ... ${total}`
    },
    sendCandidatePaging(action) {
      this.send(action, action)
    }
  }
}
