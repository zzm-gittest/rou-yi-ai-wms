import { getPurchaseOrder, updatePurchaseOrder, delPurchaseOrder, approvePurchaseOrder } from '@/api/wms/purchaseOrder'
import { getPurchaseIn, updatePurchaseIn, delPurchaseIn, approvePurchaseIn, getOrderItems } from '@/api/wms/purchaseIn'
import { getPurchaseReturn, updatePurchaseReturn, delPurchaseReturn, approvePurchaseReturn } from '@/api/wms/purchaseReturn'
import { getSalesOrder, updateSalesOrder, delSalesOrder, approveSalesOrder } from '@/api/wms/salesOrder'
import { getSalesOut, updateSalesOut, delSalesOut, approveSalesOut } from '@/api/wms/salesOut'
import { getSalesReturn, updateSalesReturn, delSalesReturn, approveSalesReturn } from '@/api/wms/salesReturn'
import { getStockCheck, updateStockCheck, delStockCheck, approveStockCheck } from '@/api/wms/stockCheck'
import { getStockTransfer, delStockTransfer, approveStockTransfer } from '@/api/wms/stockTransfer'

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
  data() {
    return {
      orderDialogOpen: false,
      orderDialogTitle: '',
      orderDialogFullscreen: false,
      orderFormView: false,
      currentOrderMessageIndex: -1,
      currentOrderItemIndex: -1,
      orderForm: {
        items: []
      },
      approvedOrderOptions: [],
      orderRules: {
        supplierId: [
          { required: true, message: '供应商不能为空', trigger: 'change' }
        ],
        customerId: [
          { required: true, message: '客户不能为空', trigger: 'change' }
        ],
        warehouseId: [
          { required: true, message: '仓库不能为空', trigger: 'change' }
        ],
        inDate: [
          { required: true, message: '入库日期不能为空', trigger: 'change' }
        ],
        returnDate: [
          { required: true, message: '退货日期不能为空', trigger: 'change' }
        ]
      }
    }
  },
  methods: {
    resetOrderForm() {
      this.orderForm = {
        id: null,
        supplierId: null,
        customerId: null,
        warehouseId: null,
        orderId: null,
        inId: null,
        sourceType: 1,
        orderDate: '',
        deliveryDate: '',
        inDate: '',
        returnDate: '',
        paidAmount: 0,
        handler: null,
        remark: null,
        items: [],
        domainType: null
      }
    },
    closeOrderDialog() {
      this.orderDialogOpen = false
      this.orderDialogFullscreen = false
      this.orderFormView = false
      this.currentOrderMessageIndex = -1
      this.currentOrderItemIndex = -1
      this.resetOrderForm()
      this.$nextTick(() => {
        this.resetForm('orderForm')
      })
    },
    async openOrderEditor(msg) {
      if (!msg || !msg.card || !msg.card.id) return

      if (msg.card.domainType === 'STOCK_CHECK') {
        this.resetOrderForm()
        const resp = await getStockCheck(msg.card.id)
        const data = resp.data || { items: [] }
        data.checkDate = formatDate(data.checkDate)
        this.orderForm = Object.assign({}, data, {
          domainType: 'STOCK_CHECK',
          orderDate: data.checkDate,
          items: Array.isArray(data.items)
            ? data.items.map(item => ({
              ...item,
              quantity: item.checkQty,
              price: item.costPrice,
              discount: 0,
              amount: 0
            }))
            : []
        })
        this.orderFormView = false
        this.orderDialogOpen = true
        this.orderDialogTitle = '修改库存盘点'
        this.currentOrderMessageIndex = this.messages.indexOf(msg)
        return
      }

      if (msg.card.domainType === 'PURCHASE_IN') {
        this.resetOrderForm()
        const resp = await getPurchaseIn(msg.card.id)
        const data = resp.data || { items: [] }
        data.inDate = formatDate(data.inDate)
        this.orderForm = data
        this.orderForm.domainType = 'PURCHASE_IN'
        this.orderForm.sourceType = this.orderForm.sourceType || 1
        this.orderForm.handler = this.orderForm.handler || this.$store.getters.name
        this.orderFormView = false
        this.orderDialogOpen = true
        this.orderDialogTitle = '修改采购入库'
        this.currentOrderMessageIndex = this.messages.indexOf(msg)
        return
      }

      if (msg.card.domainType === 'PURCHASE_RETURN') {
        this.resetOrderForm()
        const resp = await getPurchaseReturn(msg.card.id)
        const data = resp.data || { items: [] }
        data.returnDate = formatDate(data.returnDate)
        this.orderForm = data
        this.orderForm.domainType = 'PURCHASE_RETURN'
        this.orderForm.sourceType = this.orderForm.sourceType || 1
        this.orderForm.handler = this.orderForm.handler || this.$store.getters.name
        this.orderFormView = false
        this.orderDialogOpen = true
        this.orderDialogTitle = '修改采购退货'
        this.currentOrderMessageIndex = this.messages.indexOf(msg)
        return
      }

      if (msg.card.domainType === 'SALES_RETURN') {
        this.resetOrderForm()
        const resp = await getSalesReturn(msg.card.id)
        const data = resp.data || { items: [] }
        data.returnDate = formatDate(data.returnDate)
        this.orderForm = data
        this.orderForm.domainType = 'SALES_RETURN'
        this.orderForm.sourceType = this.orderForm.sourceType || 1
        this.orderForm.handler = this.orderForm.handler || this.$store.getters.name
        this.orderFormView = false
        this.orderDialogOpen = true
        this.orderDialogTitle = '修改销售退货'
        this.currentOrderMessageIndex = this.messages.indexOf(msg)
        return
      }

      if (msg.card.domainType === 'SALES_OUT') {
        this.resetOrderForm()
        const resp = await getSalesOut(msg.card.id)
        const data = resp.data || { items: [] }
        data.outDate = formatDate(data.outDate)
        this.orderForm = data
        this.orderForm.domainType = 'SALES_OUT'
        this.orderFormView = false
        this.orderDialogOpen = true
        this.orderDialogTitle = '修改销售出库'
        this.currentOrderMessageIndex = this.messages.indexOf(msg)
        return
      }

      if (!['PURCHASE_ORDER', 'PO', 'SALES_ORDER'].includes(msg.card.domainType)) {
        this.openResultModule(msg.card)
        return
      }

      if (msg.card.domainType === 'SALES_ORDER') {
        this.resetOrderForm()
        const resp = await getSalesOrder(msg.card.id)
        const data = resp.data || { items: [] }
        data.orderDate = formatDate(data.orderDate)
        data.deliveryDate = formatDate(data.deliveryDate)
        this.orderForm = data
        this.orderForm.domainType = 'SALES_ORDER'
        this.orderFormView = false
        this.orderDialogOpen = true
        this.orderDialogTitle = '修改销售订单'
        this.currentOrderMessageIndex = this.messages.indexOf(msg)
        return
      }

      this.resetOrderForm()
      const resp = await getPurchaseOrder(msg.card.id)
      const data = resp.data || { items: [] }
      data.orderDate = formatDate(data.orderDate)
      data.deliveryDate = formatDate(data.deliveryDate)
      this.orderForm = data
      this.orderForm.domainType = 'PURCHASE_ORDER'
      this.orderFormView = false
      this.orderDialogOpen = true
      this.orderDialogTitle = '修改采购订单'
      this.currentOrderMessageIndex = this.messages.indexOf(msg)
    },
    async openCandidateDetail(card, opt) {
      if (!this.canViewCandidateDetail(card, opt)) return
      const id = this.extractCandidateSourceId(opt)
      if (!id) return
      this.resetOrderForm()
      if (card.sourceDomainType === 'SALES_ORDER') {
        const resp = await getSalesOrder(id)
        const data = resp.data || { items: [] }
        data.orderDate = data.orderDate || ''
        data.deliveryDate = data.deliveryDate || ''
        this.orderForm = data
        this.orderForm.domainType = 'SALES_ORDER'
        this.orderFormView = true
        this.orderDialogOpen = true
        this.orderDialogTitle = '销售订单详情'
        this.currentOrderMessageIndex = -1
      } else if (card.sourceDomainType === 'SALES_OUT') {
        const resp = await getSalesOut(id)
        const data = resp.data || { items: [] }
        data.outDate = data.outDate || ''
        this.orderForm = data
        this.orderForm.domainType = 'SALES_OUT'
        this.orderFormView = true
        this.orderDialogOpen = true
        this.orderDialogTitle = '销售出库详情'
        this.currentOrderMessageIndex = -1
      } else {
        const resp = await getPurchaseOrder(id)
        const data = resp.data || { items: [] }
        data.orderDate = data.orderDate || ''
        data.deliveryDate = data.deliveryDate || ''
        this.orderForm = data
        this.orderForm.domainType = 'PURCHASE_ORDER'
        this.orderFormView = true
        this.orderDialogOpen = true
        this.orderDialogTitle = '采购订单详情'
        this.currentOrderMessageIndex = -1
      }
    },
    toggleOrderDialogFullscreen() {
      this.orderDialogFullscreen = !this.orderDialogFullscreen
    },
    submitOrderForm() {
      this.$refs.orderForm.validate(async valid => {
        if (!valid) return
        if (!this.orderForm.items.length) {
          this.$modal.msgError('请添加订单明细')
          return
        }
        if (this.orderForm.domainType === 'STOCK_CHECK') {
          const items = this.orderForm.items.map(item => ({
            id: item.id,
            skuId: item.skuId,
            beforeQty: item.beforeQty,
            checkQty: item.checkQty === undefined || item.checkQty === null ? item.quantity : item.checkQty,
            costPrice: item.costPrice === undefined || item.costPrice === null ? item.price : item.costPrice,
            remark: item.remark
          }))
          const invalidRow = items.find(item => !item.skuId || item.checkQty === undefined || item.checkQty === null || Number(item.checkQty) < 0 || (item.costPrice !== undefined && item.costPrice !== null && Number(item.costPrice) < 0))
          if (invalidRow) {
            this.$modal.msgError('盘点明细需包含有效SKU，且实际数量/成本价不能小于0')
            return
          }
          await updateStockCheck({
            id: this.orderForm.id,
            warehouseId: this.orderForm.warehouseId,
            checkDate: this.orderForm.checkDate || this.orderForm.orderDate,
            checkType: this.orderForm.checkType,
            handler: this.orderForm.handler,
            remark: this.orderForm.remark,
            items
          })
        } else if (this.orderForm.domainType === 'PURCHASE_IN') {
          await updatePurchaseIn(this.orderForm)
        } else if (this.orderForm.domainType === 'PURCHASE_RETURN') {
          await updatePurchaseReturn(this.orderForm)
        } else if (this.orderForm.domainType === 'SALES_ORDER') {
          await updateSalesOrder(this.orderForm)
        } else if (this.orderForm.domainType === 'SALES_RETURN') {
          await updateSalesReturn(this.orderForm)
        } else if (this.orderForm.domainType === 'SALES_OUT') {
          await updateSalesOut(this.orderForm)
        } else {
          await updatePurchaseOrder(this.orderForm)
        }
        this.$modal.msgSuccess('修改成功')
        const msg = this.messages[this.currentOrderMessageIndex]
        if (msg) {
          await this.refreshResultOrderCard(msg)
        }
        this.closeOrderDialog()
      })
    },
    handleOrderItemChange(row) {
      if (this.orderForm.domainType === 'STOCK_CHECK') {
        row.checkQty = row.quantity
        row.costPrice = row.price
        row.diffQty = Number(row.quantity || 0) - Number(row.beforeQty || 0)
        return
      }
      row.amount = Number(row.quantity || 0) * Number(row.price || 0) - Number(row.discount || 0)
    },
    handleDeleteOrderItem(index) {
      this.orderForm.items.splice(index, 1)
    },
    handleOrderChangeForIn(orderId) {
      if (!orderId) {
        this.orderForm.supplierId = null
        this.orderForm.items = []
        return
      }
      getPurchaseOrder(orderId).then(response => {
        const order = response.data
        this.orderForm.supplierId = order.supplierId
        getOrderItems(orderId).then(itemResponse => {
          const items = itemResponse.data
          this.orderForm.items = items.map(item => {
            const orderQty = item.quantity || 0
            const receivedQty = item.receivedQty || 0
            const availableQty = orderQty - receivedQty
            return {
              skuId: item.skuId,
              skuCode: item.skuCode,
              goodsName: item.goodsName,
              skuName: item.skuName,
              specInfo: item.specInfo,
              unitName: item.unitName,
              quantity: availableQty,
              price: item.price || 0,
              discount: 0,
              amount: Number(availableQty) * Number(item.price || 0)
            }
          })
        })
      })
    },
    canEditOrderItems() {
      if (this.orderFormView) {
        return false
      }
      return String(this.orderForm.approveStatus || this.orderForm.orderStatus || this.orderForm.checkStatus || this.orderForm.transferStatus || '0') === '0'
    },
    canAddOrderItems() {
      return this.canEditOrderItems() && Number(this.orderForm.sourceType || 1) !== 2
    },
    getOrderSummaries(param) {
      const { columns, data } = param
      const sums = []
      columns.forEach((column, index) => {
        if (index === 0) {
          sums[index] = '合计'
          return
        }
        if (column.property === 'quantity') {
          sums[index] = data.reduce((sum, row) => sum + Number(row.quantity || 0), 0)
        } else if (column.property === 'amount') {
          sums[index] = data.reduce((sum, row) => sum + Number(row.amount || 0), 0).toFixed(2)
        } else {
          sums[index] = ''
        }
      })
      return sums
    },
    handleSelectOrderSku(index) {
      this.currentOrderItemIndex = index !== undefined ? index : -1
      this.$refs.skuSelector.open()
    },
    handleOrderSkuSelected(selectedSkus) {
      if (!Array.isArray(selectedSkus) || !selectedSkus.length) return
      if (this.currentOrderItemIndex >= 0) {
        const sku = selectedSkus[0]
        this.$set(this.orderForm.items, this.currentOrderItemIndex, {
          skuId: sku.id,
          skuCode: sku.skuCode,
          goodsName: sku.goodsName,
          skuName: sku.skuName,
          specInfo: sku.specInfo || '无规格',
          unitName: sku.unitName || '个',
          quantity: 1,
          price: sku.purchasePrice || 0,
          discount: 0,
          amount: sku.purchasePrice || 0,
          remark: null
        })
      } else {
        selectedSkus.forEach(sku => {
          this.orderForm.items.push({
            skuId: sku.id,
            skuCode: sku.skuCode,
            goodsName: sku.goodsName,
            skuName: sku.skuName,
            specInfo: sku.specInfo || '无规格',
            unitName: sku.unitName || '个',
            quantity: 1,
            price: sku.purchasePrice || 0,
            discount: 0,
            amount: sku.purchasePrice || 0,
            remark: null
          })
        })
      }
      this.currentOrderItemIndex = -1
      this.orderForm.items.forEach(row => this.handleOrderItemChange(row))
    },
    async addOrderFormSupplier() {
      try {
        const { value } = await this.$prompt('请输入供应商名称', '新增供应商', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPattern: /\S+/,
          inputErrorMessage: '供应商名称不能为空'
        })
        const supplierName = String(value || '').trim()
        if (!supplierName) return
        await this.addSupplierAndRefresh(supplierName)
        const created = (this.supplierOptions || [])
          .filter(item => item.supplierName === supplierName)
          .sort((left, right) => Number(right.id || 0) - Number(left.id || 0))[0]
        if (created) {
          this.$set(this.orderForm, 'supplierId', created.id)
        }
        this.$message.success('供应商已新增')
      } catch (error) {
        if (error !== 'cancel') throw error
      }
    },
    async addOrderFormWarehouse() {
      try {
        const { value } = await this.$prompt('请输入仓库名称', '新增仓库', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPattern: /\S+/,
          inputErrorMessage: '仓库名称不能为空'
        })
        const warehouseName = String(value || '').trim()
        if (!warehouseName) return
        await this.addWarehouseAndRefresh(warehouseName)
        const created = (this.warehouseOptions || [])
          .filter(item => item.warehouseName === warehouseName)
          .sort((left, right) => Number(right.id || 0) - Number(left.id || 0))[0]
        if (created) {
          this.$set(this.orderForm, 'warehouseId', created.id)
        }
        this.$message.success('仓库已新增')
      } catch (error) {
        if (error !== 'cancel') throw error
      }
    },
    async addOrderFormCustomer() {
      try {
        const { value } = await this.$prompt('请输入客户名称', '新增客户', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPattern: /\S+/,
          inputErrorMessage: '客户名称不能为空'
        })
        const customerName = String(value || '').trim()
        if (!customerName) return
        await this.addCustomerAndRefresh(customerName)
        const created = (this.customerOptions || [])
          .filter(item => item.customerName === customerName)
          .sort((left, right) => Number(right.id || 0) - Number(left.id || 0))[0]
        if (created) {
          this.$set(this.orderForm, 'customerId', created.id)
        }
        this.$message.success('客户已新增')
      } catch (error) {
        if (error !== 'cancel') throw error
      }
    },
    // --- Result card actions ---
    canEditResult(card) {
      return !!card && !card.deleted && this.supportsEditAction(card) && String(card.orderStatus) === '0'
    },
    canApproveResult(card) {
      return !!card && !card.deleted && this.showApproveAction && this.supportsApproveAction(card) && String(card.orderStatus) === '0'
    },
    canDeleteResult(card) {
      return !!card && !card.deleted && this.supportsDeleteAction(card) && String(card.orderStatus) === '0'
    },
    canPrintResult(card) {
      return !!card && !card.deleted && card.id && this.supportsPrintAction(card) && String(card.orderStatus) === '1'
    },
    supportsResultActions(card) {
      return this.supportsEditAction(card) || this.supportsApproveAction(card) || this.supportsDeleteAction(card) || this.supportsPrintAction(card)
    },
    supportsEditAction(card) {
      return !!card && ['PURCHASE_ORDER', 'PO', 'PURCHASE_IN', 'PURCHASE_RETURN', 'SALES_ORDER', 'SALES_OUT', 'SALES_RETURN', 'STOCK_CHECK', 'STOCK_TRANSFER'].includes(card.domainType)
    },
    supportsApproveAction(card) {
      return !!card && ['PURCHASE_ORDER', 'PO', 'PURCHASE_IN', 'PURCHASE_RETURN', 'SALES_ORDER', 'SALES_OUT', 'SALES_RETURN', 'STOCK_CHECK', 'STOCK_TRANSFER'].includes(card.domainType)
    },
    supportsDeleteAction(card) {
      return !!card && ['PURCHASE_ORDER', 'PO', 'PURCHASE_IN', 'PURCHASE_RETURN', 'SALES_ORDER', 'SALES_OUT', 'SALES_RETURN', 'STOCK_CHECK', 'STOCK_TRANSFER'].includes(card.domainType)
    },
    supportsPrintAction(card) {
      return !!card && ['PURCHASE_ORDER', 'PO', 'PURCHASE_IN', 'PURCHASE_RETURN', 'SALES_ORDER', 'SALES_OUT', 'SALES_RETURN', 'STOCK_CHECK', 'STOCK_TRANSFER'].includes(card.domainType)
    },
    async approveResultOrder(msg) {
      if (!msg || !msg.card || !msg.card.id) return
      await this.$confirm(`是否确认审核单据"${msg.card.billNo || msg.card.orderNo || msg.card.id}"？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await this.approveResultByDomain(msg.card)
      await this.refreshResultOrderCard(msg)
      this.$modal.msgSuccess('审核成功')
    },
    async deleteResultOrder(msg) {
      if (!msg || !msg.card || !msg.card.id) return
      await this.$confirm(`是否确认删除单据"${msg.card.billNo || msg.card.orderNo || msg.card.id}"？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await this.deleteResultByDomain(msg.card)
      this.$set(msg, 'card', Object.assign({}, msg.card, { deleted: true, orderStatus: 'deleted', items: [] }))
      msg.text = `${this.formatDomainType(msg.card.domainType)}已删除。`
      this.syncConversationSnapshot()
      this.$modal.msgSuccess('删除成功')
    },
    async approveResultByDomain(card) {
      if (['PURCHASE_ORDER', 'PO'].includes(card.domainType)) return approvePurchaseOrder(card.id)
      if (card.domainType === 'PURCHASE_IN') return approvePurchaseIn(card.id)
      if (card.domainType === 'PURCHASE_RETURN') return approvePurchaseReturn(card.id)
      if (card.domainType === 'SALES_ORDER') return approveSalesOrder(card.id)
      if (card.domainType === 'SALES_OUT') return approveSalesOut(card.id)
      if (card.domainType === 'SALES_RETURN') return approveSalesReturn(card.id)
      if (card.domainType === 'STOCK_CHECK') return approveStockCheck(card.id)
      if (card.domainType === 'STOCK_TRANSFER') return approveStockTransfer(card.id)
    },
    async deleteResultByDomain(card) {
      if (['PURCHASE_ORDER', 'PO'].includes(card.domainType)) return delPurchaseOrder(card.id)
      if (card.domainType === 'PURCHASE_IN') return delPurchaseIn(card.id)
      if (card.domainType === 'PURCHASE_RETURN') return delPurchaseReturn(card.id)
      if (card.domainType === 'SALES_ORDER') return delSalesOrder(card.id)
      if (card.domainType === 'SALES_OUT') return delSalesOut(card.id)
      if (card.domainType === 'SALES_RETURN') return delSalesReturn(card.id)
      if (card.domainType === 'STOCK_CHECK') return delStockCheck(card.id)
      if (card.domainType === 'STOCK_TRANSFER') return delStockTransfer(card.id)
    },
    printResultOrder(card) {
      if (!this.canPrintResult(card)) {
        this.$modal.msgWarning('仅已审核单据支持打印')
        return
      }
      const route = this.$router.resolve({
        path: this.getPrintRouteByDomain(card.domainType),
        query: {
          id: card.id,
          from: this.$route.fullPath
        }
      })
      window.open(route.href, '_blank')
    },
    openResultModule(card) {
      const pathMap = {
        PURCHASE_IN: '/purchase/purchaseIn',
        PURCHASE_RETURN: '/purchase/purchaseReturn',
        SALES_ORDER: '/sales/salesOrder',
        SALES_OUT: '/sales/salesOut',
        SALES_RETURN: '/sales/salesReturn',
        STOCK_CHECK: '/stock/stockCheck',
        STOCK_TRANSFER: '/stock/stockTransfer',
        PURCHASE_ORDER: '/purchase/purchaseOrder',
        PO: '/purchase/purchaseOrder'
      }
      const path = pathMap[card.domainType]
      if (!path) return
      this.$router.push({ path, query: { focusId: card.id } }).catch(err => {
        console.error('路由跳转失败:', err)
      })
    },
    getPrintRouteByDomain(domainType) {
      const map = {
        PURCHASE_ORDER: '/wms/purchaseOrder/print/index',
        PO: '/wms/purchaseOrder/print/index',
        PURCHASE_IN: '/wms/purchaseIn/print/index',
        PURCHASE_RETURN: '/wms/purchaseReturn/print/index',
        SALES_ORDER: '/wms/salesOrder/print/index',
        SALES_OUT: '/wms/salesOut/print/index',
        SALES_RETURN: '/wms/salesReturn/print/index',
        STOCK_CHECK: '/wms/stockCheck/print/index',
        STOCK_TRANSFER: '/wms/stockTransfer/print/index'
      }
      return map[domainType] || '/wms/purchaseOrder/print/index'
    },
    async refreshResultOrderCard(msg) {
      if (!msg || !msg.card || !msg.card.id) return
      const card = await this.fetchResultCardData(msg.card.domainType, msg.card.id)
      if (!card) return
      this.$set(msg, 'card', card)
      this.syncConversationSnapshot()
    }
  }
}
