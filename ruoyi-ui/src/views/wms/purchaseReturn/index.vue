<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="退货单号" prop="returnNo">
        <el-input
          v-model="queryParams.returnNo"
          placeholder="请输入退货单号"
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
      <el-form-item label="审核状态" prop="approveStatus">
        <el-select v-model="queryParams.approveStatus" placeholder="审核状态" clearable>
          <el-option label="待审核" value="0" />
          <el-option label="已审核" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="退款状态" prop="paymentStatus">
        <el-select v-model="queryParams.paymentStatus" placeholder="退款状态" clearable>
          <el-option label="未退" value="0" />
          <el-option label="部分" value="1" />
          <el-option label="已退" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
        <el-button type="success" plain icon="el-icon-download" size="mini" :loading="exportLoading" :disabled="exportLoading" @click="handleExport" v-hasPermi="['wms:purchaseReturn:list']">导出</el-button>
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
          v-hasPermi="['wms:purchaseReturn:add']"
        >新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="returnList">
      <el-table-column label="退货单号" prop="returnNo" min-width="160" />
      <el-table-column label="采购入库单号" prop="inNo" min-width="150" />
      <el-table-column label="供应商" prop="supplierName" min-width="150" />
      <el-table-column label="仓库" prop="warehouseName" min-width="120" />
      <el-table-column label="退货日期" prop="returnDate" min-width="110">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.returnDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="退货总额" prop="totalAmount" min-width="120" align="right">
        <template slot-scope="scope">
          <span>{{ scope.row.totalAmount ? scope.row.totalAmount.toFixed(2) : '0.00' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="已退金额" prop="refundAmount" min-width="120" align="right">
        <template slot-scope="scope">
          <span>{{ scope.row.refundAmount ? scope.row.refundAmount.toFixed(2) : '0.00' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="退款状态" prop="paymentStatus" min-width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.paymentStatus === '2' ? 'success' : (scope.row.paymentStatus === '1' ? 'warning' : 'info')">
            {{ scope.row.paymentStatus === '0' ? '未退' : (scope.row.paymentStatus === '1' ? '部分' : '已退') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审核状态" prop="approveStatus" min-width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.approveBy ? 'success' : 'warning'">
            {{ scope.row.approveBy ? '已审核' : '待审核' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="经办人" prop="handler" min-width="100" />
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
            v-hasPermi="['wms:purchaseReturn:query']"
          >打印</el-button>
          <el-button
            v-if="!scope.row.approveBy"
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['wms:purchaseReturn:edit']"
          >修改</el-button>
          <el-button
            v-if="showApproveAction && !scope.row.approveBy"
            size="mini"
            type="text"
            icon="el-icon-check"
            @click="handleApprove(scope.row)"
            v-hasPermi="['wms:purchaseReturn:approve']"
          >审核</el-button>
          <el-button
            v-if="!scope.row.approveBy"
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['wms:purchaseReturn:remove']"
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

    <!-- 添加或修改采购退货对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="1600px" :fullscreen="dialogFullscreen" append-to-body>
      <template slot="title">
        <span>{{ title }}</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="toggleFullscreen">
          <i :class="dialogFullscreen ? 'el-icon-news' : 'el-icon-full-screen'"></i>
          {{ dialogFullscreen ? '还原' : '全屏' }}
        </el-button>
      </template>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <!-- 退货方式选择 -->
        <el-row v-if="!form.id && !formView">
          <el-col :span="24">
            <el-form-item label="退货方式">
              <el-radio-group v-model="form.sourceType" @change="handleSourceTypeChange">
                <el-radio :label="1">直接退货</el-radio>
                <el-radio :label="2">入库单退货</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 入库单退货模式：选择采购入库单 -->
        <el-row v-if="form.sourceType === 2 && !form.id && !formView">
          <el-col :span="24">
            <el-form-item label="采购入库单" prop="inId">
              <el-select
                v-model="form.inId"
                placeholder="请选择采购入库单"
                filterable
                @change="handleInChange"
                style="width: 100%"
              >
                <el-option
                  v-for="item in approvedInOptions"
                  :key="item.id"
                  :label="`${item.inNo} - ${item.supplierName} - ${item.warehouseName}`"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 入库单信息展示 -->
        <el-row v-if="form.sourceType === 2 && selectedInInfo">
          <el-col :span="24">
            <el-alert
              :title="`入库单号：${selectedInInfo.inNo} | 供应商：${selectedInInfo.supplierName} | 仓库：${selectedInInfo.warehouseName}`"
              type="info"
              :closable="false"
              style="margin-bottom: 10px"
            />
          </el-col>
        </el-row>

        <el-row>
          <!-- 显示退货单号 -->
          <el-col :span="8" v-if="form.id">
            <el-form-item label="退货单号">
              <el-input v-model="form.returnNo" disabled />
            </el-form-item>
          </el-col>
          <!-- 入库单退货模式显示采购入库单号 -->
          <el-col :span="8" v-if="form.sourceType === 2 && (form.id || formView)">
            <el-form-item label="采购入库单号">
              <el-input v-model="form.inNo" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8" v-if="!(form.id && form.sourceType === 2)">
          </el-col>
          <el-col :span="8">
            <el-form-item label="供应商" prop="supplierId">
              <div class="select-with-action">
                <el-select v-model="form.supplierId" placeholder="请选择供应商" :disabled="formView || form.id != null || form.sourceType === 2">
                  <el-option
                    v-for="item in supplierOptions"
                    :key="item.id"
                    :label="item.supplierName"
                    :value="item.id"
                  />
                </el-select>
                <el-button
                  v-if="!formView && form.id == null && form.sourceType !== 2"
                  type="text"
                  icon="el-icon-plus"
                  @click="openQuickSupplierDialog"
                  v-hasPermi="['wms:supplier:add']"
                >新增</el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="仓库" prop="warehouseId">
              <el-select v-model="form.warehouseId" placeholder="请选择仓库" :disabled="formView || form.id != null || form.sourceType === 2">
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
            <el-form-item label="退货日期" prop="returnDate">
              <el-date-picker
                v-model="form.returnDate"
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
            <el-form-item label="已退金额" prop="refundAmount">
              <el-input-number v-model="form.refundAmount" :min="0" :precision="2" :disabled="formView" style="width: 100%" />
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

        <el-divider content-position="left">退货明细</el-divider>

        <!-- 入库单退货模式：显示入库商品明细 -->
        <div v-if="form.sourceType === 2">
          <el-table :data="form.items" border style="width: 100%" show-summary :summary-method="getSummaries">
            <el-table-column type="index" width="50" label="序号" />
            <el-table-column label="SKU编码" width="130">
              <template slot-scope="scope">
                <span>{{ scope.row.skuCode || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="商品名称" width="140" show-overflow-tooltip>
              <template slot-scope="scope">
                <span>{{ scope.row.goodsName || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="SKU名称" width="120">
              <template slot-scope="scope">
                <span>{{ scope.row.skuName || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="规格" width="100">
              <template slot-scope="scope">
                <span>{{ scope.row.specInfo || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="单位" width="70">
              <template slot-scope="scope">
                <span>{{ scope.row.unitName || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="入库数量" width="90" align="right">
              <template slot-scope="scope">
                <span>{{ scope.row.inQuantity || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="已退货数量" width="100" align="right">
              <template slot-scope="scope">
                <span>{{ scope.row.returnedQty || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="本次退货数量" prop="returnQty" width="130" align="right">
              <template slot-scope="scope">
                <el-input-number
                  v-model="scope.row.returnQty"
                  :min="0"
                  :max="scope.row.inQuantity - scope.row.returnedQty"
                  :precision="2"
                  controls-position="right"
                  :disabled="!canEditItems()"
                  @change="handleItemChange(scope.row)"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column label="单价" width="100" align="right">
              <template slot-scope="scope">
                <span>{{ scope.row.price ? scope.row.price.toFixed(2) : '0.00' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="金额" width="110" align="right">
              <template slot-scope="scope">
                <span>{{ scope.row.amount ? scope.row.amount.toFixed(2) : '0.00' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="备注" min-width="120">
              <template slot-scope="scope">
                <el-input v-model="scope.row.remark" placeholder="备注" :disabled="!canEditItems()" />
              </template>
            </el-table-column>
          </el-table>
          <el-alert
            v-if="form.items.length === 0"
            title="请先选择采购入库单"
            type="warning"
            :closable="false"
            style="margin-top: 10px"
          />
        </div>

        <!-- 直接退货模式：显示可编辑的商品明细 -->
        <div v-else>
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
            <el-table-column label="金额" prop="amount" width="120" align="right">
              <template slot-scope="scope">
                <span>{{ scope.row.amount ? scope.row.amount.toFixed(2) : '0.00' }}</span>
              </template>
            </el-table-column>
            <el-table-column
              label="备注"
              min-width="150"
            >
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
            v-if="canAddItems()"
            type="dashed"
            icon="el-icon-plus"
            size="small"
            @click="handleSelectSku()"
            style="width: 100%; margin-top: 10px"
          >添加商品</el-button>
        </div>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button v-if="!formView" type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">{{ formView ? '关 闭' : '取 消' }}</el-button>
      </div>
    </el-dialog>

    <!-- 商品选择器 -->
    <goods-sku-selector ref="skuSelector" @confirm="handleSkuSelected" />

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
import { pagePurchaseReturn, getPurchaseReturn, delPurchaseReturn, addPurchaseReturn, updatePurchaseReturn, approvePurchaseReturn, getInItems, exportPurchaseReturn } from "@/api/wms/purchaseReturn"
import { listSupplier, addSupplier } from "@/api/wms/supplier"
import { listWarehouse } from "@/api/wms/warehouse"
import { getPurchaseIn, getAvailablePurchaseInForReturn } from "@/api/wms/purchaseIn"
import { getBillNeedApprove } from "@/api/wms/approvePolicy"
import GoodsSkuSelector from "@/components/GoodsSkuSelector"
import routeBillNoFilterMixin from "@/views/wms/mixins/routeBillNoFilterMixin"

export default {
  name: "PurchaseReturn",
  components: { GoodsSkuSelector },
  mixins: [routeBillNoFilterMixin],
  data() {
    return {
      routeBillNoQueryKey: 'returnNo',
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
      // 采购退货表格数据
      returnList: [],
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
      // 仓库选项
      warehouseOptions: [],
      // 已审核的采购入库单选项
      approvedInOptions: [],
      // 当前选中的入库单信息
      selectedInInfo: null,
      // 当前编辑的明细行索引
      currentIndex: -1,
      quickSupplierOpen: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        returnNo: null,
        supplierId: null,
        warehouseId: null,
        approveStatus: null,
        paymentStatus: null
      },
      // 表单参数
      form: {
        items: [],
        sourceType: 1  // 默认直接退货
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
        ],
        warehouseId: [
          { required: true, message: "仓库不能为空", trigger: "change" }
        ],
        returnDate: [
          { required: true, message: "退货日期不能为空", trigger: "change" }
        ],
        inId: [
          { required: true, message: "请选择采购入库单", trigger: "change" }
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
    this.getWarehouseList()
    this.loadApproveAction()
  },
  methods: {
    /** 查询采购退货列表 */
    getList() {
      this.loading = true
      pagePurchaseReturn(this.queryParams).then(response => {
        this.returnList = response.rows
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
    /** 查询仓库列表 */
    getWarehouseList() {
      listWarehouse().then(response => {
        this.warehouseOptions = response.data
      })
    },
    loadApproveAction() {
      getBillNeedApprove("PURCHASE_RETURN").then(response => {
        this.showApproveAction = response.data !== false
      }).catch(() => {
        this.showApproveAction = true
      })
    },
    /** 查询可退货的采购入库单列表（已审核且未全部退货） */
    getApprovedInList() {
      getAvailablePurchaseInForReturn().then(response => {
        this.approvedInOptions = response.data || []
      })
    },
    /** 退货方式切换 */
    handleSourceTypeChange(value) {
      if (value === 2) {
        // 入库单退货模式：加载已审核入库单
        this.getApprovedInList()
        this.form.inId = null
        this.form.supplierId = null
        this.form.items = []
        this.selectedInInfo = null
      } else {
        // 直退货模式：清空入库单相关数据
        this.form.inId = null
        this.form.items = []
        this.selectedInInfo = null
      }
      // 重置表单验证
      this.$nextTick(() => {
        this.$refs.form.clearValidate()
      })
    },
    /** 采购入库单选择变化 */
    handleInChange(inId) {
      if (!inId) {
        this.form.supplierId = null
        this.form.warehouseId = null
        this.form.items = []
        this.selectedInInfo = null
        return
      }

      // 获取入库单详情
      getPurchaseIn(inId).then(response => {
        const inData = response.data
        this.selectedInInfo = inData
        this.form.supplierId = inData.supplierId
        this.form.warehouseId = inData.warehouseId
        this.$set(this.form, 'inNo', inData.inNo)

        // 获取入库单商品明细
        getInItems(inId).then(itemResponse => {
          const items = itemResponse.data
          this.form.items = items.map(item => {
            const qty = Number(item.quantity || 0)                 // 入库数量
            const returnedQty = Number(item.returnedQty || 0)       // 已退货数量
            const availableQty = Math.max(qty - returnedQty, 0)     // 可退货数量
            return {
              skuId: item.skuId,
              skuCode: item.skuCode,
              goodsName: item.goodsName,
              skuName: item.skuName,
              specInfo: item.specInfo,
              unitName: item.unitName,
              inQuantity: qty,                   // 入库数量（用于显示）
              quantity: availableQty,            // 退货数量
              returnedQty: returnedQty,          // 已退货数量（用于显示）
              returnQty: availableQty,           // 退货数量（用于显示）
              price: item.price,
              amount: availableQty * (item.price || 0),
              remark: null
            }
          })
          // 重新计算合计
          this.form.items.forEach(row => {
            this.handleItemChange(row)
          })
        })
      })

      this.$refs.form.validateField('inId')
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
        warehouseId: null,
        inId: null,
        sourceType: 1,
        returnDate: null,
        refundAmount: 0,
        accountId: null,
        handler: null,
        remark: null,
        items: []
      }
      this.selectedInInfo = null
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
      this.$modal.confirm('是否确认导出采购退货数据？').then(() => {
        this.exportLoading = true
        return exportPurchaseReturn(this.queryParams)
      }).then(response => {
        const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = '采购退货数据.xlsx'
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
      this.title = "添加采购退货"
      this.form.returnDate = new Date().toISOString().split('T')[0]
      this.form.sourceType = 1  // 默认直接退货
      this.form.handler = this.$store.getters.name
    },
    /** 查看按钮操作 */
    handleView(row) {
      this.reset()
      const id = row.id
      getPurchaseReturn(id).then(response => {
        this.form = response.data
        this.formView = true
        this.open = true
        this.title = "采购退货详情"
      })
    },
    isPrintableRow(row) {
      return !!(row && (row.approveStatus === '1' || row.approveBy))
    },
    handlePrint(row) {
      if (!this.isPrintableRow(row)) {
        this.$modal.msgWarning("仅已审核单据支持打印")
        return
      }
      const route = this.$router.resolve({
        path: '/wms/purchaseReturn/print/index',
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
      getPurchaseReturn(id).then(response => {
        this.form = response.data
        this.formView = false
        this.open = true
        this.title = "修改采购退货"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.items.length === 0) {
            this.$modal.msgError("请添加退货明细")
            return
          }
          // 订单退货模式下，检查是否有退货数量
          if (this.form.sourceType === 2) {
            const hasQuantity = this.form.items.some(item => item.returnQty > 0)
            if (!hasQuantity) {
              this.$modal.msgError("请至少填写一个商品的退货数量")
              return
            }
            const overLimit = this.form.items.find(item => {
              const inQty = Number(item.inQuantity || 0)
              const returnedQty = Number(item.returnedQty || 0)
              const availableQty = Math.max(inQty - returnedQty, 0)
              return Number(item.returnQty || 0) > availableQty
            })
            if (overLimit) {
              const inQty = Number(overLimit.inQuantity || 0)
              const returnedQty = Number(overLimit.returnedQty || 0)
              const availableQty = Math.max(inQty - returnedQty, 0)
              this.$modal.msgError(`商品【${overLimit.goodsName || overLimit.skuName || '-'}】退货数量不能超过可退货数量 ${availableQty}`)
              return
            }
            // 过滤掉退货数量为0的商品，并将 returnQty 转为 quantity
            this.form.items = this.form.items
              .filter(item => item.returnQty > 0)
              .map(item => ({
                ...item,
                quantity: item.returnQty
              }))
          }
          if (this.form.id != null) {
            updatePurchaseReturn(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addPurchaseReturn(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除退货单"' + row.returnNo + '"？').then(function() {
        return delPurchaseReturn(row.id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 审核按钮操作 */
    handleApprove(row) {
      this.$modal.confirm('是否确认审核退货单"' + row.returnNo + '"？').then(function() {
        return approvePurchaseReturn(row.id)
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
    canAddItems() {
      return this.canEditItems() && this.form.sourceType !== 2
    },
    /** 明细数据变化 */
    handleItemChange(row) {
      // 根据模式计算金额
      const qty = this.form.sourceType === 2 ? (row.returnQty || 0) : (row.quantity || 0)
      const amount = qty * (row.price || 0)
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
        if (column.property === 'returnQty') {
          sums[index] = data.reduce((sum, row) => sum + (row.returnQty || 0), 0)
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

.select-with-action {
  display: flex;
  align-items: center;
  gap: 10px;
}

.select-with-action .el-select {
  flex: 1;
}
</style>
