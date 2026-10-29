<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="订单编号" prop="orderNo">
        <el-input
          v-model="queryParams.orderNo"
          placeholder="请输入订单编号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="供应商" prop="supplierId">
        <el-select v-model="queryParams.supplierId" placeholder="请选择供应商" clearable>
          <el-option
            v-for="item in supplierOptions"
            :key="item.id"
            :label="item.supplierName"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="订单状态" prop="orderStatus">
        <el-select v-model="queryParams.orderStatus" placeholder="订单状态" clearable>
          <el-option label="待审核" value="0" />
          <el-option label="已审核" value="1" />
          <el-option label="部分入库" value="2" />
          <el-option label="全部入库" value="3" />
          <el-option label="已关闭" value="4" />
        </el-select>
      </el-form-item>
      <el-form-item label="付款状态" prop="paymentStatus">
        <el-select v-model="queryParams.paymentStatus" placeholder="付款状态" clearable>
          <el-option label="未付" value="0" />
          <el-option label="部分" value="1" />
          <el-option label="已付" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
        <el-button type="success" plain icon="el-icon-download" size="mini" :loading="exportLoading" :disabled="exportLoading" @click="handleExport" v-hasPermi="['wms:purchaseOrder:query']">导出</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['wms:purchaseOrder:add']"
        >新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="orderList">
      <el-table-column label="订单编号" prop="orderNo" min-width="150" />
      <el-table-column label="供应商" prop="supplierName" min-width="150" :show-overflow-tooltip="true" />
      <el-table-column label="订单日期" prop="orderDate" min-width="110">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.orderDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="交货日期" prop="deliveryDate" min-width="110">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.deliveryDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="订单总额" prop="totalAmount" min-width="100" align="right">
        <template slot-scope="scope">
          <span>{{ scope.row.totalAmount ? scope.row.totalAmount.toFixed(2) : '0.00' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="应付金额" prop="payableAmount" min-width="100" align="right">
        <template slot-scope="scope">
          <span>{{ scope.row.payableAmount ? scope.row.payableAmount.toFixed(2) : '0.00' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="付款状态" prop="paymentStatus" min-width="80" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.paymentStatus === '2' ? 'success' : (scope.row.paymentStatus === '1' ? 'warning' : 'info')">
            {{ scope.row.paymentStatus === '0' ? '未付' : (scope.row.paymentStatus === '1' ? '部分' : '已付') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="订单状态" prop="orderStatus" min-width="80" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.orderStatus === '3' ? 'success' : (scope.row.orderStatus === '4' ? 'info' : 'warning')">
            {{ scope.row.orderStatus === '0' ? '待审核' : (scope.row.orderStatus === '1' ? '已审核' : (scope.row.orderStatus === '2' ? '部分入库' : (scope.row.orderStatus === '3' ? '全部入库' : '已关闭'))) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="经办人" prop="handler" min-width="80" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="320">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
          >详情</el-button>
          <el-button
            v-if="isPrintableRow(scope.row)"
            size="mini"
            type="text"
            icon="el-icon-printer"
            @click="handlePrint(scope.row)"
            v-hasPermi="['wms:purchaseOrder:query']"
          >打印</el-button>
          <el-button
            v-if="scope.row.orderStatus === '0'"
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['wms:purchaseOrder:edit']"
          >修改</el-button>
          <el-button
            v-if="showApproveAction && scope.row.orderStatus === '0'"
            size="mini"
            type="text"
            icon="el-icon-check"
            @click="handleApprove(scope.row)"
            v-hasPermi="['wms:purchaseOrder:approve']"
          >审核</el-button>
          <el-button
            v-if="scope.row.orderStatus === '0'"
            size="mini"
            type="text"
            icon="el-icon-close"
            @click="handleClose(scope.row)"
            v-hasPermi="['wms:purchaseOrder:close']"
          >关闭</el-button>
          <el-button
            v-if="scope.row.orderStatus === '0'"
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['wms:purchaseOrder:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改采购订单对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="1600px" :fullscreen="dialogFullscreen" append-to-body>
      <template slot="title">
        <span>{{ title }}</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="toggleFullscreen">
          <i :class="dialogFullscreen ? 'el-icon-news' : 'el-icon-full-screen'"></i>
          {{ dialogFullscreen ? '还原' : '全屏' }}
        </el-button>
      </template>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="8">
            <el-form-item label="供应商" prop="supplierId">
              <div class="select-with-action">
                <el-select v-model="form.supplierId" placeholder="请选择供应商" :disabled="formView">
                  <el-option
                    v-for="item in supplierOptions"
                    :key="item.id"
                    :label="item.supplierName"
                    :value="item.id"
                  />
                </el-select>
                <el-button
                  v-if="!formView && !isPrintableRow(form)"
                  type="text"
                  icon="el-icon-plus"
                  @click="openQuickSupplierDialog"
                  v-hasPermi="['wms:supplier:add']"
                >新增</el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="订单日期" prop="orderDate">
              <el-date-picker
                v-model="form.orderDate"
                type="date"
                placeholder="选择日期"
                value-format="yyyy-MM-dd"
                :disabled="formView"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="交货日期" prop="deliveryDate">
              <el-date-picker
                v-model="form.deliveryDate"
                type="date"
                placeholder="选择日期"
                value-format="yyyy-MM-dd"
                :disabled="formView"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" :disabled="formView" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">订单明细</el-divider>

        <el-table :data="form.items" border style="width: 100%" show-summary :summary-method="getSummaries">
          <el-table-column type="index" width="50" label="序号" />
          <el-table-column label="SKU" width="200">
            <template slot-scope="scope">
              <div style="display: flex; align-items: center; gap: 5px;">
                <el-input
                  v-model="scope.row.skuName"
                  placeholder="请选择商品"
                  readonly
                  :disabled="!canEditItems()"
                  size="small"
                  style="flex: 1"
                />
                <el-button
                  icon="el-icon-search"
                  size="mini"
                  :disabled="!canEditItems()"
                  @click="handleSelectSku(scope.$index)"
                >选择</el-button>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="商品名称" prop="goodsName" width="150" show-overflow-tooltip>
            <template slot-scope="scope">
              <span>{{ scope.row.goodsName || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="SKU名称" prop="skuName" width="150" />
          <el-table-column label="规格" prop="specInfo" width="120" />
          <el-table-column label="单位" width="140">
            <template slot-scope="scope">
              <el-select
                v-if="canEditItems()"
                v-model="scope.row.unitName"
                filterable
                allow-create
                default-first-option
                clearable
                placeholder="单位可选"
                size="small"
              >
                <el-option
                  v-for="item in unitOptions"
                  :key="'purchase-order-unit-' + item.id"
                  :label="item.unitName"
                  :value="item.unitName"
                />
              </el-select>
              <span v-else>{{ scope.row.unitName || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="120">
            <template slot-scope="scope">
              <el-input-number
                v-model="scope.row.quantity"
                :min="0"
                :precision="2"
                controls-position="right"
                :disabled="!canEditItems()"
                @change="handleItemChange(scope.row)"
                style="width: 100%"
              />
            </template>
          </el-table-column>
          <el-table-column label="单价" width="120">
            <template slot-scope="scope">
              <el-input-number
                v-model="scope.row.price"
                :min="0"
                :precision="2"
                controls-position="right"
                :disabled="!canEditItems()"
                @change="handleItemChange(scope.row)"
                style="width: 100%"
              />
            </template>
          </el-table-column>
          <el-table-column label="折扣(元)" width="140">
            <template slot-scope="scope">
              <el-input-number
                v-model="scope.row.discount"
                :min="0"
                :precision="2"
                controls-position="right"
                :disabled="!canEditItems()"
                @change="handleItemChange(scope.row)"
                style="width: 100%"
              />
            </template>
          </el-table-column>
          <el-table-column label="金额" prop="amount" width="90" align="right">
            <template slot-scope="scope">
              <span>{{ scope.row.amount ? scope.row.amount.toFixed(2) : '0.00' }}</span>
            </template>
          </el-table-column>
          <el-table-column
            label="备注"
            min-width="110"
          >
            <template slot-scope="scope">
              <el-input v-model="scope.row.remark" placeholder="备注" :disabled="!canEditItems()" />
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="100" v-if="canEditItems()">
            <template slot-scope="scope">
              <el-button
                size="mini"
                type="text"
                icon="el-icon-delete"
                @click="handleDeleteItem(scope.$index)"
              >删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-button
          v-if="canEditItems()"
          type="dashed"
          icon="el-icon-plus"
          size="small"
          @click="handleSelectSku()"
          style="width: 100%; margin-top: 10px"
        >添加商品</el-button>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button v-if="!formView" type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">{{ formView ? '关 闭' : '取 消' }}</el-button>
      </div>
    </el-dialog>

    <!-- 商品选择器 -->
    <goods-sku-selector ref="skuSelector" price-mode="purchase" @confirm="handleSkuSelected" />

    <el-dialog title="快速新增供应商" :visible.sync="quickSupplierOpen" width="520px" append-to-body>
      <el-form ref="quickSupplierForm" :model="quickSupplierForm" :rules="quickSupplierRules" label-width="90px">
        <el-form-item label="供应商名称" prop="supplierName">
          <el-input v-model="quickSupplierForm.supplierName" placeholder="请输入供应商名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="联系人" prop="contact">
          <el-input v-model="quickSupplierForm.contact" placeholder="请输入联系人" maxlength="50" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="quickSupplierForm.phone" placeholder="请输入联系电话" maxlength="20" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitQuickSupplier">确 定</el-button>
        <el-button @click="quickSupplierOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { pagePurchaseOrder, getPurchaseOrder, delPurchaseOrder, addPurchaseOrder, updatePurchaseOrder, approvePurchaseOrder, closePurchaseOrder, exportPurchaseOrder } from "@/api/wms/purchaseOrder"
import { listSupplier, addSupplier } from "@/api/wms/supplier"
import { listUnit } from "@/api/wms/unit"
import { getBillNeedApprove } from "@/api/wms/approvePolicy"
import GoodsSkuSelector from "@/components/GoodsSkuSelector"
import routeBillNoFilterMixin from "@/views/wms/mixins/routeBillNoFilterMixin"

export default {
  name: "PurchaseOrder",
  components: { GoodsSkuSelector },
  mixins: [routeBillNoFilterMixin],
  data() {
    return {
      routeBillNoQueryKey: 'orderNo',
      // 导出相关
      exportLoading: false,
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 是否显示审核相关按钮
      showApproveAction: true,
      // 采购订单表格数据
      orderList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 是否查看模式
      formView: false,
      // 是否全屏
      dialogFullscreen: false,
      // 供应商选项
      supplierOptions: [],
      // 单位选项
      unitOptions: [],
      // 当前编辑的明细行索引
      currentIndex: -1,
      quickSupplierOpen: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderNo: null,
        supplierId: null,
        orderStatus: null,
        paymentStatus: null
      },
      // 表单参数
      form: {
        items: []
      },
      quickSupplierForm: {
        supplierName: null,
        contact: null,
        phone: null,
        status: "0",
        settlementType: "1",
        creditLimit: 0,
        paymentDays: 0
      },
      // 表单校验
      rules: {
        supplierId: [
          { required: true, message: "供应商不能为空", trigger: "change" }
        ]
      },
      quickSupplierRules: {
        supplierName: [
          { required: true, message: "供应商名称不能为空", trigger: "blur" }
        ]
      }
    }
  },
  created() {
    if (this.applyBillNoFromRoute()) {
      this.handleQuery()
    } else {
      this.getList()
    }
    this.getSupplierList()
    this.getUnitList()
    this.loadApproveAction()
  },
  methods: {
    /** 查询采购订单列表 */
    getList() {
      this.loading = true
      pagePurchaseOrder(this.queryParams).then(response => {
        this.orderList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 查询供应商列表 */
    getSupplierList() {
      listSupplier().then(response => {
        this.supplierOptions = response.data
      })
    },
    getUnitList() {
      listUnit().then(response => {
        this.unitOptions = response.data || []
      })
    },
    resetQuickSupplierForm() {
      this.quickSupplierForm = {
        supplierName: null,
        contact: null,
        phone: null,
        status: "0",
        settlementType: "1",
        creditLimit: 0,
        paymentDays: 0
      }
      this.$nextTick(() => {
        this.$refs.quickSupplierForm && this.$refs.quickSupplierForm.clearValidate()
      })
    },
    openQuickSupplierDialog() {
      this.resetQuickSupplierForm()
      this.quickSupplierOpen = true
    },
    submitQuickSupplier() {
      this.$refs.quickSupplierForm.validate(valid => {
        if (!valid) {
          return
        }
        addSupplier(this.quickSupplierForm).then(() => {
          this.$modal.msgSuccess("供应商新增成功")
          this.quickSupplierOpen = false
          listSupplier().then(response => {
            this.supplierOptions = response.data || []
            const created = this.supplierOptions
              .filter(item => item.supplierName === this.quickSupplierForm.supplierName)
              .sort((a, b) => Number(b.id || 0) - Number(a.id || 0))[0]
            if (created) {
              this.form.supplierId = created.id
            }
          })
        })
      })
    },
    loadApproveAction() {
      getBillNeedApprove("PURCHASE_ORDER").then(response => {
        this.showApproveAction = response.data !== false
      }).catch(() => {
        this.showApproveAction = true
      })
    },
    /** 打开商品选择器 */
    handleSelectSku(index) {
      this.currentIndex = index !== undefined ? index : -1
      this.$refs.skuSelector.open()
    },
    /** 商品选择确认 */
    handleSkuSelected(selectedSkus) {
      if (this.currentIndex >= 0) {
        // 替换模式：只取第一个选中的商品替换当前行
        const sku = selectedSkus[0]
        const item = {
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
        }
        this.$set(this.form.items, this.currentIndex, item)
      } else {
        // 新增模式：追加所有选中的商品
        selectedSkus.forEach(sku => {
          const item = {
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
          }
          this.form.items.push(item)
        })
      }
      this.currentIndex = -1
      // 重新计算合计
      this.form.items.forEach(row => {
        this.handleItemChange(row)
      })
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.formView = false
      this.dialogFullscreen = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        supplierId: null,
        orderDate: null,
        deliveryDate: null,
        discountAmount: 0,
        paidAmount: 0,
        accountId: null,
        handler: null,
        remark: null,
        items: []
      }
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    /** 导出按钮操作 */
    handleExport() {
      this.$modal.confirm('是否确认导出采购订单数据？').then(() => {
        this.exportLoading = true
        return exportPurchaseOrder(this.queryParams)
      }).then(response => {
        const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = '采购订单数据.xlsx'
        link.click()
        URL.revokeObjectURL(link.href)
        this.$modal.msgSuccess("导出成功")
        this.exportLoading = false
      }).catch((err) => {
        this.exportLoading = false
        if (err && err !== 'cancel') {
          this.$modal.msgError("导出失败")
        }
      })
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.formView = false
      this.title = "添加采购订单"
    },
    /** 查看按钮操作 */
    handleView(row) {
      this.reset()
      const id = row.id
      getPurchaseOrder(id).then(response => {
        this.form = response.data
        this.formView = true
        this.open = true
        this.title = "采购订单详情"
      })
    },
    isPrintableRow(row) {
      return !!(row && row.orderStatus && row.orderStatus !== '0')
    },
    handlePrint(row) {
      if (!this.isPrintableRow(row)) {
        this.$modal.msgWarning("仅已审核单据支持打印")
        return
      }
      const route = this.$router.resolve({
        path: '/wms/purchaseOrder/print/index',
        query: {
          id: row.id,
          from: this.$route.fullPath
        }
      })
      window.open(route.href, '_blank')
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id
      getPurchaseOrder(id).then(response => {
        this.form = response.data
        this.formView = false
        this.open = true
        this.title = "修改采购订单"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.items.length === 0) {
            this.$modal.msgError("请添加订单明细")
            return
          }
          if (this.form.id != null) {
            updatePurchaseOrder(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addPurchaseOrder(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      this.$modal.confirm('是否确认删除订单"' + row.orderNo + '"？').then(function() {
        return delPurchaseOrder(row.id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 审核按钮操作 */
    handleApprove(row) {
      this.$modal.confirm('是否确认审核订单"' + row.orderNo + '"？').then(function() {
        return approvePurchaseOrder(row.id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("审核成功")
      }).catch(() => {})
    },
    /** 关闭按钮操作 */
    handleClose(row) {
      this.$modal.confirm('是否确认关闭订单"' + row.orderNo + '"？').then(function() {
        return closePurchaseOrder(row.id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("关闭成功")
      }).catch(() => {})
    },
    /** 切换全屏 */
    toggleFullscreen() {
      this.dialogFullscreen = !this.dialogFullscreen
    },
    /** 删除明细 */
    handleDeleteItem(index) {
      this.form.items.splice(index, 1)
    },
    canEditItems() {
      return !this.formView && !this.isPrintableRow(this.form)
    },
    /** 明细数据变化 */
    handleItemChange(row) {
      const amount = row.quantity * (row.price || 0) - (row.discount || 0)
      row.amount = amount
    },
    /** 合计 */
    getSummaries(param) {
      const { columns, data } = param
      const sums = []
      columns.forEach((column, index) => {
        if (index === 0) {
          sums[index] = '合计'
          return
        }
        if (column.property === 'quantity') {
          sums[index] = data.reduce((sum, row) => sum + (row.quantity || 0), 0)
        } else if (column.property === 'amount') {
          sums[index] = data.reduce((sum, row) => sum + (row.amount || 0), 0).toFixed(2)
        } else {
          sums[index] = ''
        }
      })
      return sums
    }
  }
}
</script>

<style scoped>
/* 优化禁用状态下的输入框样式，使其更易读 */
::v-deep .el-input.is-disabled .el-input__inner,
::v-deep .el-textarea.is-disabled .el-textarea__inner,
::v-deep .el-select .el-input.is-disabled .el-input__inner {
  color: #606266 !important;
  background-color: #f5f7fa;
  border-color: #e4e7ed;
}

::v-deep .el-input-number.is-disabled .el-input-number__decrease,
::v-deep .el-input-number.is-disabled .el-input-number__increase {
  color: #909399 !important;
  background-color: #f5f7fa;
  border-color: #e4e7ed;
}

::v-deep .el-input-number.is-disabled .el-input__inner {
  color: #606266 !important;
  background-color: #f5f7fa;
  border-color: #e4e7ed;
}

/* 优化只读状态下的输入框样式 */
::v-deep .el-input.is-read-only .el-input__inner,
::v-deep .el-textarea.is-read-only .el-textarea__inner {
  color: #606266 !important;
  background-color: #fff;
  border-color: #dcdfe6;
  cursor: default;
}

.select-with-action {
  display: flex;
  align-items: center;
  gap: 10px;
}

.select-with-action .el-select {
  flex: 1;
}
</style>
