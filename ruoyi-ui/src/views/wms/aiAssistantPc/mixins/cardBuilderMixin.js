import { getPurchaseOrder } from '@/api/wms/purchaseOrder'
import { getPurchaseIn } from '@/api/wms/purchaseIn'
import { getPurchaseReturn } from '@/api/wms/purchaseReturn'
import { getSalesOrder } from '@/api/wms/salesOrder'
import { getSalesOut } from '@/api/wms/salesOut'
import { getSalesReturn } from '@/api/wms/salesReturn'
import { getStockCheck } from '@/api/wms/stockCheck'
import { getStockTransfer } from '@/api/wms/stockTransfer'

function formatDate(date) {
  if (!date) return ''
  if (typeof date === 'string') return date
  if (typeof date === 'object' && date.toString) {
    const str = date.toString()
    return str.split('T')[0] || ''
  }
  return ''
}

export default {
  methods: {
    prepareCard(card) {
      if (!card) return
      if (card.type === 'CANDIDATE' && card.selectionMode === 'single-list') {
        this.$set(card, 'selectedValue', '')
        this.$set(card, 'searchKeyword', card.searchKeyword || '')
        return
      }
      if (this.isMultiCandidateCard(card)) {
        this.$set(card, 'selectedSkuIds', this.getCachedCandidateSelections(card))
        return
      }
      if (card.type === 'REQUIRED_FORM') {
        const model = {}
        ;(card.fields || []).forEach(field => {
          model[field.key] = field.defaultValue !== undefined && field.defaultValue !== null ? field.defaultValue : ''
        })
        this.$set(card, 'formModel', model)
        return
      }
      if (card.type === 'GOODS_CREATE_FORM') {
        this.$set(card, 'formModel', {
          goodsName: (card.formModel && card.formModel.goodsName) || '',
          unitName: (card.formModel && card.formModel.unitName) || '',
          categoryId: card.formModel && card.formModel.categoryId,
          categoryName: (card.formModel && card.formModel.categoryName) || ''
        })
        return
      }
      if (card.type === 'GOODS_SPEC_FORM') {
        this.$set(card, 'formModel', {
          goodsName: (card.formModel && card.formModel.goodsName) || '',
          unitName: (card.formModel && card.formModel.unitName) || '',
          specs: this.normalizeSpecRows(card.formModel && card.formModel.specs)
        })
        return
      }
      if (card.type === 'GOODS_SKU_FORM') {
        this.$set(card, 'formModel', {
          goodsName: (card.formModel && card.formModel.goodsName) || '',
          unitName: (card.formModel && card.formModel.unitName) || '',
          skuList: this.normalizeSkuRows(card.formModel && card.formModel.skuList)
        })
        return
      }
      if (card.type === 'DIFF_PREVIEW' && card.editable) {
        this.$set(card, 'formModel', {
          supplierId: card.formModel && card.formModel.supplierId !== undefined ? card.formModel.supplierId : (card.supplierId || null),
          warehouseId: card.formModel && card.formModel.warehouseId !== undefined ? card.formModel.warehouseId : (card.warehouseId || null),
          customerId: card.formModel && card.formModel.customerId !== undefined ? card.formModel.customerId : (card.customerId || null),
          items: this.normalizePreviewRows(card.formModel && card.formModel.items)
        })
      }
    },
    lockEditablePreviewCards() {
      this.messages.forEach(msg => {
        if (msg && msg.card && msg.card.type === 'DIFF_PREVIEW' && msg.card.editable) {
          this.$set(msg.card, 'editable', false)
          this.$set(msg.card, 'items', this.normalizePreviewRows((msg.card.formModel && msg.card.formModel.items) || msg.card.items || []))
        }
      })
    },
    async hydrateExecResultCard(card) {
      if (!card || !card.id || !card.domainType) return
      const needsHydrate = !Array.isArray(card.items) || !card.items.length || ['PURCHASE_RETURN', 'STOCK_CHECK', 'STOCK_TRANSFER'].includes(card.domainType)
      console.log('hydrateExecResultCard:', card.domainType, 'needsHydrate:', needsHydrate, 'items:', card.items)
      if (!needsHydrate) return
      try {
        const freshCard = await this.fetchResultCardData(card.domainType, card.id)
        console.log('freshCard:', freshCard)
        if (!freshCard) return
        Object.keys(freshCard).forEach(key => {
          this.$set(card, key, freshCard[key])
        })
        this.syncConversationSnapshot()
      } catch (error) {
        console.error('hydrateExecResultCard error:', error)
      }
    },
    buildExecResultCard(order) {
      const card = {
        type: 'EXEC_RESULT',
        domainType: 'PURCHASE_ORDER',
        id: order.id,
        orderNo: order.orderNo,
        supplierName: order.supplierName,
        orderDate: formatDate(order.orderDate),
        orderStatus: order.orderStatus,
        totalAmount: order.totalAmount,
        items: []
      }
      card.items = Array.isArray(order.items)
        ? order.items.map(item => ({
          goods: item.goodsName,
          sku: item.skuName,
          unit: item.unitName,
          quantity: item.quantity,
          price: item.price,
          amount: item.amount
        }))
        : []
      return card
    },
    buildGenericResultCard(domainType, data, options = {}) {
      const billNo = data[options.billNoKey || 'billNo'] || ''
      const partner = options.partnerFormatter ? options.partnerFormatter(data) : (options.partnerKey ? data[options.partnerKey] : '')
      const warehouse = options.warehouseFormatter ? options.warehouseFormatter(data) : (options.warehouseKey ? data[options.warehouseKey] : '')
      console.log('buildGenericResultCard:', domainType, 'data.items:', data.items)
      return {
        type: 'EXEC_RESULT',
        domainType,
        id: data.id,
        billNo,
        orderNo: data.orderNo,
        partner,
        supplierName: options.supplierNameKey ? data[options.supplierNameKey] : undefined,
        customerName: options.customerNameKey ? data[options.customerNameKey] : undefined,
        warehouse,
        checkType: options.checkTypeKey ? data[options.checkTypeKey] : undefined,
        orderDate: options.dateKey ? formatDate(data[options.dateKey]) : undefined,
        orderStatus: options.statusKey ? data[options.statusKey] : undefined,
        totalAmount: data.totalAmount,
        sourceNo: options.sourceKey ? data[options.sourceKey] : undefined,
        items: Array.isArray(data.items)
          ? data.items.map(item => ({
            goods: item.goodsName,
            sku: item.skuName,
            unit: item.unitName,
            beforeQty: options.beforeQtyKey ? item[options.beforeQtyKey] : item.beforeQty,
            quantity: options.quantityKey ? item[options.quantityKey] : (item.quantity || item.checkQty),
            diffQty: options.diffQtyKey ? item[options.diffQtyKey] : item.diffQty,
            price: options.priceKey ? item[options.priceKey] : item.price,
            amount: options.amountKey ? item[options.amountKey] : item.amount
          }))
          : []
      }
    },
    async fetchResultCardData(domainType, id) {
      if (!id) return null
      if (['PURCHASE_ORDER', 'PO'].includes(domainType)) {
        const resp = await getPurchaseOrder(id)
        return this.buildExecResultCard(resp.data || {})
      }
      if (domainType === 'PURCHASE_IN') {
        const resp = await getPurchaseIn(id)
        const data = resp.data || {}
        return this.buildGenericResultCard('PURCHASE_IN', data, {
          billNoKey: 'inNo', partnerKey: 'supplierName', supplierNameKey: 'supplierName', warehouseKey: 'warehouseName', dateKey: 'inDate', statusKey: 'approveStatus', sourceKey: 'orderNo'
        })
      }
      if (domainType === 'PURCHASE_RETURN') {
        const resp = await getPurchaseReturn(id)
        console.log('PURCHASE_RETURN resp:', resp)
        return this.buildGenericResultCard('PURCHASE_RETURN', resp.data || {}, {
          billNoKey: 'returnNo', partnerKey: 'supplierName', supplierNameKey: 'supplierName', warehouseKey: 'warehouseName', dateKey: 'returnDate', statusKey: 'approveStatus', sourceKey: 'inNo'
        })
      }
      if (domainType === 'SALES_ORDER') {
        const resp = await getSalesOrder(id)
        return this.buildGenericResultCard('SALES_ORDER', resp.data || {}, {
          billNoKey: 'orderNo', partnerKey: 'customerName', customerNameKey: 'customerName', dateKey: 'orderDate', statusKey: 'orderStatus'
        })
      }
      if (domainType === 'SALES_OUT') {
        const resp = await getSalesOut(id)
        return this.buildGenericResultCard('SALES_OUT', resp.data || {}, {
          billNoKey: 'outNo', partnerKey: 'customerName', customerNameKey: 'customerName', warehouseKey: 'warehouseName', dateKey: 'outDate', statusKey: 'approveStatus', sourceKey: 'orderNo'
        })
      }
      if (domainType === 'SALES_RETURN') {
        const resp = await getSalesReturn(id)
        return this.buildGenericResultCard('SALES_RETURN', resp.data || {}, {
          billNoKey: 'returnNo', partnerKey: 'customerName', customerNameKey: 'customerName', warehouseKey: 'warehouseName', dateKey: 'returnDate', statusKey: 'approveStatus', sourceKey: 'outNo'
        })
      }
      if (domainType === 'STOCK_CHECK') {
        const resp = await getStockCheck(id)
        return this.buildGenericResultCard('STOCK_CHECK', resp.data || {}, {
          billNoKey: 'checkNo', partnerKey: 'warehouseName', warehouseKey: 'warehouseName', checkTypeKey: 'checkType', dateKey: 'checkDate', statusKey: 'checkStatus', quantityKey: 'checkQty', diffQtyKey: 'diffQty', amountKey: 'diffAmount', priceKey: 'costPrice', beforeQtyKey: 'beforeQty'
        })
      }
      if (domainType === 'STOCK_TRANSFER') {
        const resp = await getStockTransfer(id)
        return this.buildGenericResultCard('STOCK_TRANSFER', resp.data || {}, {
          billNoKey: 'transferNo', partnerFormatter: data => `${data.outWarehouseName || '-'} -> ${data.inWarehouseName || '-'}`.trim(), warehouseFormatter: data => `${data.outWarehouseName || '-'} -> ${data.inWarehouseName || '-'}`.trim(), dateKey: 'transferDate', statusKey: 'transferStatus'
        })
      }
      return null
    },
    createSpecRow() {
      return {
        specName: '',
        values: [{ specValue: '' }]
      }
    },
    normalizeSpecRows(specs) {
      if (!Array.isArray(specs) || !specs.length) {
        return [this.createSpecRow()]
      }
      return specs.map(spec => ({
        specName: spec.specName || '',
        values: Array.isArray(spec.values) && spec.values.length
          ? spec.values.map(value => ({ specValue: value.specValue || '' }))
          : [{ specValue: '' }]
      }))
    },
    normalizeSkuRows(skuList) {
      if (!Array.isArray(skuList)) return []
      return skuList.map(row => ({
        skuName: row.skuName || '',
        specInfo: row.specInfo || '',
        unitName: row.unitName || '',
        quantity: this.normalizeDecimalInput(row.quantity),
        price: this.normalizeDecimalInput(row.price)
      }))
    },
    normalizePreviewRows(items) {
      if (!Array.isArray(items)) return []
      return items.map(row => ({
        sourceLineId: row.sourceLineId,
        skuId: row.skuId,
        goods: row.goods || '',
        sku: row.sku || '',
        unit: row.unit || '',
        orderQuantity: this.normalizeDecimalInput(row.orderQuantity),
        receivedQty: this.normalizeDecimalInput(row.receivedQty),
        quantity: this.normalizeDecimalInput(row.quantity),
        price: this.normalizeDecimalInput(row.price),
        beforeQty: this.normalizeDecimalInput(row.beforeQty),
        maxQty: this.normalizeDecimalInput(row.maxQty)
      }))
    },
    normalizeDecimalInput(value) {
      const text = value === undefined || value === null ? '' : String(value)
      const cleaned = text
        .replace(/[^\d.]/g, '')
        .replace(/\.(?=.*\.)/g, '')
        .replace(/^(\d+\.\d{0,2}).*$/, '$1')
      return cleaned.startsWith('.') ? `0${cleaned}` : cleaned
    },
    handleSkuNumberInput(row, field, value) {
      if (!row) return
      this.$set(row, field, this.normalizeDecimalInput(value))
    },
    handlePreviewNumberInput(row, field, value) {
      if (!row) return
      let nextValue = this.normalizeDecimalInput(value)
      if (field === 'quantity') {
        const quantity = Number(nextValue)
        const maxQty = row.maxQty === '' || row.maxQty === null || row.maxQty === undefined ? null : Number(row.maxQty)
        if (maxQty !== null && !Number.isNaN(maxQty) && !Number.isNaN(quantity) && quantity > maxQty) {
          nextValue = this.normalizeDecimalInput(maxQty)
        }
      }
      row[field] = nextValue
    },
    previewRowAmount(row) {
      if (!row) return null
      const quantity = Number(row.quantity)
      const price = Number(row.price)
      if (Number.isNaN(quantity) || Number.isNaN(price)) {
        return row.amount
      }
      return quantity * price
    },
    previewRowDiffQty(row) {
      if (!row) return null
      const beforeQty = Number(row.beforeQty)
      const quantity = Number(row.quantity)
      if (Number.isNaN(beforeQty) || Number.isNaN(quantity)) {
        return row.diffQty
      }
      return quantity - beforeQty
    }
  }
}
