<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="调拨单号" prop="transferNo">
        <el-input
          v-model="queryParams.transferNo"
          placeholder="请输入调拨单号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="调出仓库" prop="outWarehouseId">
        <el-select v-model="queryParams.outWarehouseId" placeholder="请选择仓库" clearable>
          <el-option
            v-for="item in warehouseOptions"
            :key="item.id"
            :label="item.warehouseName"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="调入仓库" prop="inWarehouseId">
        <el-select v-model="queryParams.inWarehouseId" placeholder="请选择仓库" clearable>
          <el-option
            v-for="item in warehouseOptions"
            :key="item.id"
            :label="item.warehouseName"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="调拨状态" prop="transferStatus">
        <el-select v-model="queryParams.transferStatus" placeholder="调拨状态" clearable>
          <el-option label="待审核" value="0" />
          <el-option label="已审核" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
        <el-button type="success" plain icon="el-icon-download" size="mini" :loading="exportLoading" :disabled="exportLoading" @click="handleExport" v-hasPermi="['wms:stockTransfer:query']">导出</el-button>
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
          v-hasPermi="['wms:stockTransfer:add']"
        >新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="transferList">
      <el-table-column label="调拨单号" prop="transferNo" min-width="150" />
      <el-table-column label="调出仓库" prop="outWarehouseName" min-width="120" />
      <el-table-column label="调入仓库" prop="inWarehouseName" min-width="120" />
      <el-table-column label="调拨日期" prop="transferDate" min-width="110">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.transferDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="经办人" prop="handler" min-width="100" />
      <el-table-column label="调拨状态" prop="transferStatus" min-width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.transferStatus === '1' ? 'success' : 'warning'">
            {{ scope.row.transferStatus === '1' ? '已审核' : '待审核' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审核人" prop="approveBy" min-width="100" />
      <el-table-column label="审核时间" prop="approveTime" min-width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.approveTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" min-width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="280">
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
            v-hasPermi="['wms:stockTransfer:query']"
          >打印</el-button>
          <el-button
            v-if="scope.row.transferStatus === '0'"
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['wms:stockTransfer:edit']"
          >修改</el-button>
          <el-button
            v-if="showApproveAction && scope.row.transferStatus === '0'"
            size="mini"
            type="text"
            icon="el-icon-check"
            @click="handleApprove(scope.row)"
            v-hasPermi="['wms:stockTransfer:approve']"
          >审核</el-button>
          <el-button
            v-if="scope.row.transferStatus === '0'"
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['wms:stockTransfer:remove']"
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

    <!-- 添加或修改库存调拨单对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="1100px" :fullscreen="dialogFullscreen" append-to-body>
      <template slot="title">
        <span>{{ title }}</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="toggleFullscreen">
          <i :class="dialogFullscreen ? 'el-icon-news' : 'el-icon-full-screen'"></i>
          {{ dialogFullscreen ? '还原' : '全屏' }}
        </el-button>
      </template>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="调出仓库" prop="outWarehouseId">
              <el-select v-model="form.outWarehouseId" placeholder="请选择调出仓库" :disabled="formView || form.id != null">
                <el-option
                  v-for="item in warehouseOptions"
                  :key="item.id"
                  :label="item.warehouseName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="调入仓库" prop="inWarehouseId">
              <el-select v-model="form.inWarehouseId" placeholder="请选择调入仓库" :disabled="formView || form.id != null">
                <el-option
                  v-for="item in warehouseOptions"
                  :key="item.id"
                  :label="item.warehouseName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="调拨日期" prop="transferDate">
              <el-date-picker
                v-model="form.transferDate"
                type="date"
                placeholder="选择日期"
                value-format="yyyy-MM-dd"
                :disabled="formView"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
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

        <el-divider content-position="left">调拨明细</el-divider>

        <el-table :data="form.items" border style="width: 100%">
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
                style="width: 100%"
              />
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
    <goods-sku-selector ref="skuSelector" :warehouse-id="form.outWarehouseId" @confirm="handleSkuSelected" />
  </div>
</template>

<script>
import { pageStockTransfer, getStockTransfer, delStockTransfer, addStockTransfer, updateStockTransfer, approveStockTransfer, exportStockTransfer } from "@/api/wms/stockTransfer"
import { listWarehouse } from "@/api/wms/warehouse"
import { getBillNeedApprove } from "@/api/wms/approvePolicy"
import GoodsSkuSelector from "@/components/GoodsSkuSelector"
import routeBillNoFilterMixin from "@/views/wms/mixins/routeBillNoFilterMixin"

export default {
  name: "StockTransfer",
  components: { GoodsSkuSelector },
  mixins: [routeBillNoFilterMixin],
  data() {
    return {
      routeBillNoQueryKey: 'transferNo',
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
      // 库存调拨单表格数据
      transferList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 是否查看模式
      formView: false,
      // 是否全屏
      dialogFullscreen: false,
      // 仓库选项
      warehouseOptions: [],
      // 当前编辑的明细行索引
      currentIndex: -1,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        transferNo: null,
        outWarehouseId: null,
        inWarehouseId: null,
        transferStatus: null
      },
      // 表单参数
      form: {
        items: []
      },
      // 表单校验
      rules: {
        outWarehouseId: [
          { required: true, message: "调出仓库不能为空", trigger: "change" }
        ],
        inWarehouseId: [
          { required: true, message: "调入仓库不能为空", trigger: "change" },
          {
            validator: (rule, value, callback) => {
              if (value && value === this.form.outWarehouseId) {
                callback(new Error('调入仓库不能与调出仓库相同'))
              } else {
                callback()
              }
            },
            trigger: 'change'
          }
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
    this.getWarehouseList()
    this.loadApproveAction()
  },
  watch: {
    'form.outWarehouseId'(newVal, oldVal) {
      // 当调出仓库变化时，如果调入仓库与之相同，则触发校验
      if (newVal && newVal === this.form.inWarehouseId) {
        this.$nextTick(() => {
          this.$refs.form.validateField('inWarehouseId')
        })
      }
      // 调出仓库变更后清空已添加的商品明细
      if (oldVal && newVal !== oldVal && this.form.items && this.form.items.length > 0) {
        this.$confirm('切换调出仓库将清空已添加的商品明细，是否继续？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.form.items = []
        }).catch(() => {
          this.form.outWarehouseId = oldVal
        })
      }
    }
  },
  methods: {
    /** 查询库存调拨单列表 */
    getList() {
      this.loading = true
      pageStockTransfer(this.queryParams).then(response => {
        this.transferList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 查询仓库列表 */
    getWarehouseList() {
      listWarehouse().then(response => {
        this.warehouseOptions = response.data
      })
    },
    loadApproveAction() {
      getBillNeedApprove("STOCK_TRANSFER").then(response => {
        this.showApproveAction = response.data !== false
      }).catch(() => {
        this.showApproveAction = true
      })
    },
    /** 打开商品选择器 */
    handleSelectSku(index) {
      if (!this.form.outWarehouseId) {
        this.$modal.msgWarning("请先选择调出仓库")
        return
      }
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
            remark: null
          }
          this.form.items.push(item)
        })
      }
      this.currentIndex = -1
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
        outWarehouseId: null,
        inWarehouseId: null,
        transferDate: null,
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
      this.$modal.confirm('是否确认导出库存调拨数据？').then(() => {
        this.exportLoading = true
        return exportStockTransfer(this.queryParams)
      }).then(response => {
        const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = '库存调拨数据.xlsx'
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
      this.title = "添加库存调拨单"
      this.form.handler = this.$store.getters.name
    },
    /** 查看按钮操作 */
    handleView(row) {
      this.reset()
      const id = row.id
      getStockTransfer(id).then(response => {
        this.form = response.data
        this.formView = true
        this.open = true
        this.title = "库存调拨单详情"
      })
    },
    isPrintableRow(row) {
      return !!(row && row.transferStatus === '1')
    },
    handlePrint(row) {
      if (!this.isPrintableRow(row)) {
        this.$modal.msgWarning("仅已审核单据支持打印")
        return
      }
      const route = this.$router.resolve({
        path: '/wms/stockTransfer/print/index',
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
      getStockTransfer(id).then(response => {
        this.form = response.data
        this.formView = false
        this.open = true
        this.title = "修改库存调拨单"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.items.length === 0) {
            this.$modal.msgError("请添加调拨明细")
            return
          }
          if (this.form.id != null) {
            updateStockTransfer(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addStockTransfer(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除调拨单"' + row.transferNo + '"？').then(function() {
        return delStockTransfer(row.id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 审核按钮操作 */
    handleApprove(row) {
      this.$modal.confirm('是否确认审核调拨单"' + row.transferNo + '"？').then(function() {
        return approveStockTransfer(row.id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("审核成功")
      }).catch(() => {})
    },
    /** 删除明细 */
    handleDeleteItem(index) {
      this.form.items.splice(index, 1)
    },
    canEditItems() {
      return !this.formView && !this.isPrintableRow(this.form)
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
