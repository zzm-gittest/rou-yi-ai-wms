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
      <el-form-item label="客户" prop="customerId">
        <el-select v-model="queryParams.customerId" placeholder="请选择客户" clearable>
          <el-option
            v-for="item in customerOptions"
            :key="item.id"
            :label="item.customerName"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="订单状态" prop="orderStatus">
        <el-select v-model="queryParams.orderStatus" placeholder="订单状态" clearable>
          <el-option label="待审核" value="0" />
          <el-option label="已审核" value="1" />
          <el-option label="部分发货" value="2" />
          <el-option label="全部发货" value="3" />
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
        <el-button type="success" plain icon="el-icon-download" size="mini" :loading="exportLoading" :disabled="exportLoading" @click="handleExport" v-hasPermi="['wms:salesOrder:list']">导出</el-button>
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
          v-hasPermi="['wms:salesOrder:add']"
        >新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="orderList">
      <el-table-column label="订单编号" prop="orderNo" min-width="150" />
      <el-table-column label="客户" prop="customerName" min-width="150" :show-overflow-tooltip="true" />
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
      <el-table-column label="应收金额" prop="receivableAmount" min-width="100" align="right">
        <template slot-scope="scope">
          <span>{{ scope.row.receivableAmount ? scope.row.receivableAmount.toFixed(2) : '0.00' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="收款状态" prop="paymentStatus" min-width="80" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.paymentStatus === '2' ? 'success' : (scope.row.paymentStatus === '1' ? 'warning' : 'info')">
            {{ scope.row.paymentStatus === '0' ? '未收' : (scope.row.paymentStatus === '1' ? '部分' : '已收') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="订单状态" prop="orderStatus" min-width="80" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.orderStatus === '3' ? 'success' : (scope.row.orderStatus === '4' ? 'info' : 'warning')">
            {{ scope.row.orderStatus === '0' ? '待审核' : (scope.row.orderStatus === '1' ? '已审核' : (scope.row.orderStatus === '2' ? '部分发货' : (scope.row.orderStatus === '3' ? '全部发货' : '已关闭'))) }}
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
            v-hasPermi="['wms:salesOrder:query']"
          >打印</el-button>
          <el-button
            v-if="scope.row.orderStatus === '0'"
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['wms:salesOrder:edit']"
          >修改</el-button>
          <el-button
            v-if="showApproveAction && scope.row.orderStatus === '0'"
            size="mini"
            type="text"
            icon="el-icon-check"
            @click="handleApprove(scope.row)"
            v-hasPermi="['wms:salesOrder:approve']"
          >审核</el-button>
          <el-button
            v-if="scope.row.orderStatus === '0'"
            size="mini"
            type="text"
            icon="el-icon-close"
            @click="handleClose(scope.row)"
            v-hasPermi="['wms:salesOrder:close']"
          >关闭</el-button>
          <el-button
            v-if="scope.row.orderStatus === '0'"
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['wms:salesOrder:remove']"
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

    <!-- 添加或修改销售订单对话框 -->
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
            <el-form-item label="客户" prop="customerId">
              <el-select v-model="form.customerId" placeholder="请选择客户" :disabled="formView">
                <el-option
                  v-for="item in customerOptions"
                  :key="item.id"
                  :label="item.customerName"
                  :value="item.id"
                />
              </el-select>
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
          <el-col :span="8">
            <el-form-item label="优惠金额" prop="discountAmount">
              <el-input-number v-model="form.discountAmount" :min="0" :precision="2" :disabled="formView" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="已收金额" prop="receivedAmount">
              <el-input-number v-model="form.receivedAmount" :min="0" :precision="2" :disabled="formView" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="经办人" prop="handler">
              <el-input v-model="form.handler" placeholder="请输入经办人" :disabled="formView" />
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
          <el-table-column label="单位" prop="unitName" width="80" />
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
          <el-table-column label="折扣(元)" width="100">
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
          <el-table-column label="金额" prop="amount" width="120" align="right">
            <template slot-scope="scope">
              <span>{{ scope.row.amount ? scope.row.amount.toFixed(2) : '0.00' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="150">
            <template slot-scope="scope">
              <el-input v-model="scope.row.remark" placeholder="备注" :disabled="!canEditItems()" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="60" v-if="canEditItems()">
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
    <goods-sku-selector ref="skuSelector" price-mode="sale" @confirm="handleSkuSelected" />
  </div>
</template>

<script>
import { pageSalesOrder, getSalesOrder, delSalesOrder, addSalesOrder, updateSalesOrder, approveSalesOrder, closeSalesOrder, exportSalesOrder } from "@/api/wms/salesOrder"
import { listCustomer } from "@/api/wms/customer"
import { getBillNeedApprove } from "@/api/wms/approvePolicy"
import GoodsSkuSelector from "@/components/GoodsSkuSelector"
import routeBillNoFilterMixin from "@/views/wms/mixins/routeBillNoFilterMixin"

export default {
  name: "SalesOrder",
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
      // 销售订单表格数据
      orderList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 是否查看模式
      formView: false,
      // 是否全屏
      dialogFullscreen: false,
      // 客户选项
      customerOptions: [],
      // 当前编辑的明细行索引
      currentIndex: -1,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderNo: null,
        customerId: null,
        orderStatus: null,
        paymentStatus: null
      },
      // 表单参数
      form: {
        items: []
      },
      // 表单校验
      rules: {
        customerId: [
          { required: true, message: "客户不能为空", trigger: "change" }
        ],
        orderDate: [
          { required: true, message: "订单日期不能为空", trigger: "change" }
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
    this.getCustomerList()
    this.loadApproveAction()
  },
  methods: {
    /** 查询销售订单列表 */
    getList() {
      this.loading = true
      pageSalesOrder(this.queryParams).then(response => {
        this.orderList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 查询客户列表 */
    getCustomerList() {
      listCustomer().then(response => {
        this.customerOptions = response.data
      })
    },
    loadApproveAction() {
      getBillNeedApprove("SALES_ORDER").then(response => {
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
          price: sku.salePrice || 0,
          discount: 0,
          amount: sku.salePrice || 0,
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
            price: sku.salePrice || 0,
            discount: 0,
            amount: sku.salePrice || 0,
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
        customerId: null,
        orderDate: null,
        deliveryDate: null,
        discountAmount: 0,
        receivedAmount: 0,
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
      this.$modal.confirm('是否确认导出销售订单数据？').then(() => {
        this.exportLoading = true
        return exportSalesOrder(this.queryParams)
      }).then(response => {
        const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = '销售订单数据.xlsx'
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
      this.title = "添加销售订单"
      this.form.orderDate = new Date().toISOString().split('T')[0]
      this.form.handler = this.$store.getters.name
    },
    /** 查看按钮操作 */
    handleView(row) {
      this.reset()
      const id = row.id
      getSalesOrder(id).then(response => {
        this.form = response.data
        this.formView = true
        this.open = true
        this.title = "销售订单详情"
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
        path: '/wms/salesOrder/print/index',
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
      getSalesOrder(id).then(response => {
        this.form = response.data
        this.formView = false
        this.open = true
        this.title = "修改销售订单"
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
            updateSalesOrder(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addSalesOrder(this.form).then(response => {
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
        return delSalesOrder(row.id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 审核按钮操作 */
    handleApprove(row) {
      this.$modal.confirm('是否确认审核订单"' + row.orderNo + '"？').then(function() {
        return approveSalesOrder(row.id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("审核成功")
      }).catch(() => {})
    },
    /** 关闭按钮操作 */
    handleClose(row) {
      this.$modal.confirm('是否确认关闭订单"' + row.orderNo + '"？').then(function() {
        return closeSalesOrder(row.id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("关闭成功")
      }).catch(() => {})
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
    },
    /** 切换全屏 */
    toggleFullscreen() {
      this.dialogFullscreen = !this.dialogFullscreen
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
</style>
