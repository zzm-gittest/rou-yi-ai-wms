<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="盘点单号" prop="checkNo">
        <el-input
          v-model="queryParams.checkNo"
          placeholder="请输入盘点单号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="仓库" prop="warehouseId">
        <el-select v-model="queryParams.warehouseId" placeholder="请选择仓库" clearable>
          <el-option
            v-for="item in warehouseOptions"
            :key="item.id"
            :label="item.warehouseName"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="盘点状态" prop="checkStatus">
        <el-select v-model="queryParams.checkStatus" placeholder="盘点状态" clearable>
          <el-option label="待审核" value="0" />
          <el-option label="已审核" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="盘点类型" prop="checkType">
        <el-select v-model="queryParams.checkType" placeholder="盘点类型" clearable>
          <el-option label="全盘" value="FULL" />
          <el-option label="抽盘" value="SAMPLE" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
        <el-button type="success" plain icon="el-icon-download" size="mini" :loading="exportLoading" :disabled="exportLoading" @click="handleExport" v-hasPermi="['wms:stockCheck:query']">导出</el-button>
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
          v-hasPermi="['wms:stockCheck:add']"
        >新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="checkList">
      <el-table-column label="盘点单号" prop="checkNo" min-width="150" />
      <el-table-column label="仓库" prop="warehouseName" min-width="120" />
      <el-table-column label="盘点日期" prop="checkDate" min-width="110">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.checkDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="盘点类型" prop="checkType" min-width="110">
        <template slot-scope="scope">
          <span>{{ formatCheckType(scope.row.checkType) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="经办人" prop="handler" min-width="100" />
      <el-table-column label="盘点状态" prop="checkStatus" min-width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.checkStatus === '1' ? 'success' : 'warning'">
            {{ scope.row.checkStatus === '1' ? '已审核' : '待审核' }}
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
            v-hasPermi="['wms:stockCheck:query']"
          >打印</el-button>
          <el-button
            v-if="scope.row.checkStatus === '0'"
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['wms:stockCheck:edit']"
          >修改</el-button>
          <el-button
            v-if="showApproveAction && scope.row.checkStatus === '0'"
            size="mini"
            type="text"
            icon="el-icon-check"
            @click="handleApprove(scope.row)"
            v-hasPermi="['wms:stockCheck:approve']"
          >审核</el-button>
          <el-button
            v-if="scope.row.checkStatus === '0'"
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['wms:stockCheck:remove']"
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

    <!-- 添加或修改库存盘点单对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="1300px" :fullscreen="dialogFullscreen" append-to-body>
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
            <el-form-item label="仓库" prop="warehouseId">
              <el-select v-model="form.warehouseId" placeholder="请选择仓库" :disabled="formView || form.id != null" style="width: 100%" @change="handleWarehouseChange">
                <el-option
                  v-for="item in warehouseOptions"
                  :key="item.id"
                  :label="item.warehouseName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="盘点日期" prop="checkDate">
              <el-date-picker
                v-model="form.checkDate"
                type="date"
                placeholder="选择日期"
                value-format="yyyy-MM-dd"
                :disabled="formView"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="盘点类型" prop="checkType">
              <el-select v-model="form.checkType" placeholder="请选择盘点类型" :disabled="formView" style="width: 100%">
                <el-option label="全盘" value="FULL" />
                <el-option label="抽盘" value="SAMPLE" />
              </el-select>
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

        <el-divider content-position="left">盘点明细</el-divider>

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
          <el-table-column label="账面数量" prop="beforeQty" width="100" align="right">
            <template slot-scope="scope">
              <span>{{ scope.row.beforeQty || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="成本价" width="120" align="right">
            <template slot-scope="scope">
              <el-input-number
                v-model="scope.row.costPrice"
                :min="0"
                :precision="2"
                controls-position="right"
                :disabled="!canEditItems()"
                style="width: 100%"
              />
            </template>
          </el-table-column>
          <el-table-column label="实际数量" width="120">
            <template slot-scope="scope">
              <el-input-number
                v-model="scope.row.checkQty"
                :min="0"
                :precision="2"
                controls-position="right"
                :disabled="!canEditItems()"
                @change="handleQtyChange(scope.row)"
                style="width: 100%"
              />
            </template>
          </el-table-column>
          <el-table-column label="差异数量" prop="diffQty" width="100" align="right">
            <template slot-scope="scope">
              <span :style="{ color: scope.row.diffQty > 0 ? 'green' : (scope.row.diffQty < 0 ? 'red' : '') }">
                {{ scope.row.diffQty }}
              </span>
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
    <goods-sku-selector ref="skuSelector" :warehouse-id="form.warehouseId" @confirm="handleSkuSelected" />
  </div>
</template>

<script>
import { pageStockCheck, getStockCheck, delStockCheck, addStockCheck, updateStockCheck, approveStockCheck, exportStockCheck } from "@/api/wms/stockCheck"
import { listWarehouse } from "@/api/wms/warehouse"
import { getStockByWarehouseAndSku } from "@/api/wms/stock"
import { getBillNeedApprove } from "@/api/wms/approvePolicy"
import GoodsSkuSelector from "@/components/GoodsSkuSelector"
import routeBillNoFilterMixin from "@/views/wms/mixins/routeBillNoFilterMixin"

export default {
  name: "StockCheck",
  components: { GoodsSkuSelector },
  mixins: [routeBillNoFilterMixin],
  data() {
    return {
      routeBillNoQueryKey: 'checkNo',
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
      // 库存盘点单表格数据
      checkList: [],
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
      // 当前编辑仓库变化保护
      internalWarehouseChange: false,
      previousWarehouseId: null,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        checkNo: null,
        warehouseId: null,
        checkType: null,
        checkStatus: null
      },
      // 表单参数
      form: {
        items: []
      },
      // 表单校验
      rules: {
        warehouseId: [
          { required: true, message: "仓库不能为空", trigger: "change" }
        ],
        checkDate: [
          { required: true, message: "盘点日期不能为空", trigger: "change" }
        ],
        checkType: [
          { required: true, message: "盘点类型不能为空", trigger: "change" }
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
  methods: {
    /** 查询库存盘点单列表 */
    getList() {
      this.loading = true
      pageStockCheck(this.queryParams).then(response => {
        this.checkList = response.rows
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
      getBillNeedApprove("STOCK_CHECK").then(response => {
        this.showApproveAction = response.data !== false
      }).catch(() => {
        this.showApproveAction = true
      })
    },
    /** 打开商品选择器 */
    handleSelectSku(index) {
      if (!this.form.warehouseId) {
        this.$modal.msgWarning("请先选择仓库")
        return
      }
      this.currentIndex = index !== undefined ? index : -1
      this.$refs.skuSelector.open()
    },
    /** 商品选择确认 */
    handleSkuSelected(selectedSkus) {
      if (!this.form.warehouseId) {
        this.$modal.msgWarning("请先选择仓库")
        return
      }

      if (this.currentIndex >= 0) {
        // 替换模式：只取第一个选中的商品替换当前行
        const sku = selectedSkus[0]
        this.updateItemWithSku(this.form.items[this.currentIndex], sku)
      } else {
        // 新增模式：追加所有选中的商品
        selectedSkus.forEach(sku => {
          // 检查是否已存在
          const exists = this.form.items.some(item => item.skuId === sku.id)
          if (exists) {
            return
          }
          const item = {
            skuId: sku.id,
            skuCode: sku.skuCode,
            goodsName: sku.goodsName,
            skuName: sku.skuName,
            specInfo: sku.specInfo || '无规格',
            unitName: sku.unitName || '个',
            beforeQty: 0,
            checkQty: 0,
            diffQty: 0,
            costPrice: sku.costPrice || 0,
            remark: null
          }
          this.form.items.push(item)
          // 获取账面数量
          this.loadBeforeQty(item)
        })
      }
      this.currentIndex = -1
    },
    /** 更新明细行商品信息 */
    updateItemWithSku(item, sku) {
      item.skuId = sku.id
      item.skuCode = sku.skuCode
      item.goodsName = sku.goodsName
      item.skuName = sku.skuName
      item.specInfo = sku.specInfo || '无规格'
      item.unitName = sku.unitName || '个'
      item.checkQty = 0
      item.diffQty = 0
      item.costPrice = sku.costPrice || 0
      // 获取账面数量
      this.loadBeforeQty(item)
    },
    /** 加载账面数量 */
    loadBeforeQty(item) {
      if (!this.form.warehouseId || !item.skuId) {
        item.beforeQty = 0
        return
      }
      getStockByWarehouseAndSku(this.form.warehouseId, item.skuId).then(response => {
        item.beforeQty = response.data ? response.data.quantity : 0
        if (response.data && response.data.costPrice !== undefined && response.data.costPrice !== null) {
          item.costPrice = response.data.costPrice
        }
        item.checkQty = item.beforeQty
        item.diffQty = 0
      }).catch(() => {
        item.beforeQty = 0
        item.checkQty = 0
        item.diffQty = 0
        item.costPrice = 0
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
        warehouseId: null,
        checkDate: null,
        checkType: 'FULL',
        handler: null,
        remark: null,
        items: []
      }
      this.previousWarehouseId = null
      this.internalWarehouseChange = false
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
      this.$modal.confirm('是否确认导出库存盘点数据？').then(() => {
        this.exportLoading = true
        return exportStockCheck(this.queryParams)
      }).then(response => {
        const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = '库存盘点数据.xlsx'
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
      this.title = "添加库存盘点单"
      this.form.handler = this.$store.getters.name
      this.previousWarehouseId = this.form.warehouseId
    },
    formatCheckType(checkType) {
      const map = {
        FULL: '全盘',
        SAMPLE: '抽盘',
        DYNAMIC: '抽盘'
      }
      return map[checkType] || '全盘'
    },
    /** 查看按钮操作 */
    handleView(row) {
      this.reset()
      const id = row.id
      getStockCheck(id).then(response => {
        this.form = response.data
        this.formView = true
        this.open = true
        this.title = "库存盘点单详情"
        this.previousWarehouseId = this.form.warehouseId
      })
    },
    isPrintableRow(row) {
      return !!(row && row.checkStatus === '1')
    },
    handlePrint(row) {
      if (!this.isPrintableRow(row)) {
        this.$modal.msgWarning("仅已审核单据支持打印")
        return
      }
      const route = this.$router.resolve({
        path: '/wms/stockCheck/print/index',
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
      getStockCheck(id).then(response => {
        this.form = response.data
        this.formView = false
        this.open = true
        this.title = "修改库存盘点单"
        this.previousWarehouseId = this.form.warehouseId
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.items.length === 0) {
            this.$modal.msgError("请添加盘点明细")
            return
          }
          const invalidItem = this.form.items.find(item => {
            const checkQty = Number(item.checkQty)
            const costPrice = Number(item.costPrice)
            return !item.skuId || Number.isNaN(checkQty) || checkQty < 0 || Number.isNaN(costPrice) || costPrice < 0
          })
          if (invalidItem) {
            this.$modal.msgError("请检查盘点明细：实际数量和成本价不能小于0")
            return
          }
          if (this.form.id != null) {
            updateStockCheck(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addStockCheck(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除盘点单"' + row.checkNo + '"？').then(function() {
        return delStockCheck(row.id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 审核按钮操作 */
    handleApprove(row) {
      this.$modal.confirm('是否确认审核盘点单"' + row.checkNo + '"？').then(function() {
        return approveStockCheck(row.id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("审核成功")
      }).catch(() => {})
    },
    /** 删除明细 */
    handleDeleteItem(index) {
      this.form.items.splice(index, 1)
    },
    async handleWarehouseChange(value) {
      if (this.internalWarehouseChange || this.formView || this.form.id != null) {
        this.previousWarehouseId = value
        return
      }
      if (!this.previousWarehouseId) {
        this.previousWarehouseId = value
        return
      }
      if (String(this.previousWarehouseId) === String(value)) {
        return
      }
      if (this.form.items && this.form.items.length > 0) {
        try {
          await this.$confirm('切换盘点仓库将清空已添加的盘点明细，是否继续？', '提示', {
            confirmButtonText: '继续',
            cancelButtonText: '取消',
            type: 'warning'
          })
          this.form.items = []
          this.previousWarehouseId = value
        } catch (e) {
          this.internalWarehouseChange = true
          this.form.warehouseId = this.previousWarehouseId
          this.$nextTick(() => {
            this.internalWarehouseChange = false
          })
          return
        }
      } else {
        this.previousWarehouseId = value
      }
    },
    canEditItems() {
      return !this.formView && !this.isPrintableRow(this.form)
    },
    /** 数量变化 */
    handleQtyChange(row) {
      row.diffQty = (row.checkQty || 0) - (row.beforeQty || 0)
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
