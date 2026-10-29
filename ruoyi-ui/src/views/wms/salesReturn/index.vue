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
        <el-button type="success" plain icon="el-icon-download" size="mini" :loading="exportLoading" :disabled="exportLoading" @click="handleExport" v-hasPermi="['wms:salesReturn:list']">导出</el-button>
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
          v-hasPermi="['wms:salesReturn:add']"
        >新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="returnList">
      <el-table-column label="退货单号" prop="returnNo" min-width="160" />
      <el-table-column label="销售出库单号" prop="outNo" min-width="150" />
      <el-table-column label="客户" prop="customerName" min-width="150" />
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
            v-hasPermi="['wms:salesReturn:query']"
          >打印</el-button>
          <el-button
            v-if="!scope.row.approveBy"
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['wms:salesReturn:edit']"
          >修改</el-button>
          <el-button
            v-if="showApproveAction && !scope.row.approveBy"
            size="mini"
            type="text"
            icon="el-icon-check"
            @click="handleApprove(scope.row)"
            v-hasPermi="['wms:salesReturn:approve']"
          >审核</el-button>
          <el-button
            v-if="!scope.row.approveBy"
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['wms:salesReturn:remove']"
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

    <!-- 添加或修改销售退货对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="1600px" :fullscreen="dialogFullscreen" append-to-body>
      <template slot="title">
        <span>{{ title }}</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="toggleFullscreen">
          <i :class="dialogFullscreen ? 'el-icon-news' : 'el-icon-full-screen'"></i>
          {{ dialogFullscreen ? '还原' : '全屏' }}
        </el-button>
      </template>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <!-- 退货方式选择（仅新增时可选择） -->
        <el-row v-if="!form.id && !formView">
          <el-col :span="24">
            <el-form-item label="退货方式">
              <el-radio-group v-model="form.sourceType" @change="handleSourceTypeChange">
                <el-radio :label="1">直接退货</el-radio>
                <el-radio :label="2">出库单退货</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 出库单退货模式：选择销售出库单（仅新增时可选择） -->
        <el-row v-if="form.sourceType === 2 && !form.id && !formView">
          <el-col :span="24">
            <el-form-item label="销售出库单" prop="outId">
              <el-select
                v-model="form.outId"
                placeholder="请选择销售出库单"
                filterable
                @change="handleOutChange"
                style="width: 100%"
              >
                <el-option
                  v-for="item in approvedOutOptions"
                  :key="item.id"
                  :label="`${item.outNo} - ${item.customerName} - ${item.warehouseName}`"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 出库单信息展示 -->
        <el-row v-if="form.sourceType === 2 && selectedOutInfo">
          <el-col :span="24">
            <el-alert
              :title="`出库单号：${selectedOutInfo.outNo} | 客户：${selectedOutInfo.customerName} | 仓库：${selectedOutInfo.warehouseName}`"
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
          <!-- 出库单退货模式显示销售出库单号 -->
          <el-col :span="8" v-if="form.sourceType === 2 && (form.id || formView)">
            <el-form-item label="销售出库单号">
              <el-input v-model="form.outNo" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8" v-if="!(form.id && form.sourceType === 2)">
          </el-col>
          <el-col :span="8">
            <el-form-item label="客户" prop="customerId">
              <el-select v-model="form.customerId" placeholder="请选择客户" :disabled="formView || form.id != null || form.sourceType === 2">
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

        <!-- 出库单退货模式：显示出库单商品明细 -->
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
            <el-table-column label="出库数量" prop="outQuantity" width="90" align="right">
              <template slot-scope="scope">
                <span>{{ scope.row.outQuantity || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="已退货数量" prop="returnedQty" width="100" align="right">
              <template slot-scope="scope">
                <span>{{ scope.row.returnedQty || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="本次退货数量" prop="quantity" width="130" align="right">
              <template slot-scope="scope">
                <el-input-number
                  v-if="canEditItems()"
                  v-model="scope.row.quantity"
                  :min="0"
                  :max="scope.row.availableQty"
                  :precision="2"
                  controls-position="right"
                  @change="handleItemChange(scope.row)"
                  style="width: 100%"
                />
                <span v-else>{{ scope.row.quantity || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="单价" prop="price" width="100" align="right">
              <template slot-scope="scope">
                <span>{{ scope.row.price ? scope.row.price.toFixed(2) : '0.00' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="金额" prop="amount" width="110" align="right">
              <template slot-scope="scope">
                <span>{{ (scope.row.quantity * (scope.row.price || 0)).toFixed(2) }}</span>
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
            title="请先选择销售出库单"
            type="warning"
            :closable="false"
            style="margin-top: 10px"
          />
        </div>

        <!-- 直接退货模式：显示可编辑的商品明细 -->
        <div v-else>
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
            <el-table-column label="SKU名称" width="130">
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
            <el-table-column label="数量" width="120" align="right">
              <template slot-scope="scope">
                <span v-if="!canEditItems()">{{ scope.row.quantity || 0 }}</span>
                <el-input-number
                  v-else
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
            <el-table-column label="单价" width="120" align="right">
              <template slot-scope="scope">
                <span v-if="!canEditItems()">{{ scope.row.price ? scope.row.price.toFixed(2) : '0.00' }}</span>
                <el-input-number
                  v-else
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
            <el-table-column label="金额" prop="amount" width="110" align="right">
              <template slot-scope="scope">
                <span>{{ scope.row.amount ? scope.row.amount.toFixed(2) : '0.00' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="备注" min-width="120">
              <template slot-scope="scope">
                <span v-if="!canEditItems()">{{ scope.row.remark || '-' }}</span>
                <el-input v-else v-model="scope.row.remark" placeholder="备注" :disabled="!canEditItems()" />
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
  </div>
</template>

<script>
import { pageSalesReturn, getSalesReturn, delSalesReturn, addSalesReturn, updateSalesReturn, approveSalesReturn, exportSalesReturn } from "@/api/wms/salesReturn"
import { listCustomer } from "@/api/wms/customer"
import { listWarehouse } from "@/api/wms/warehouse"
import { listApprovedSalesOut, getSalesOutItems, getSalesOut } from "@/api/wms/salesOut"
import { getBillNeedApprove } from "@/api/wms/approvePolicy"
import GoodsSkuSelector from "@/components/GoodsSkuSelector"
import routeBillNoFilterMixin from "@/views/wms/mixins/routeBillNoFilterMixin"

export default {
  name: "SalesReturn",
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
      // 销售退货表格数据
      returnList: [],
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
      // 仓库选项
      warehouseOptions: [],
      // 已审核的销售出库单选项
      approvedOutOptions: [],
      // 当前选中的出库单信息
      selectedOutInfo: null,
      // 当前编辑的明细行索引
      currentIndex: -1,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        returnNo: null,
        customerId: null,
        warehouseId: null,
        approveStatus: null,
        paymentStatus: null
      },
      // 表单参数
      form: {
        items: [],
        sourceType: 1  // 默认直接退货
      },
      // 表单校验
      rules: {
        customerId: [
          { required: true, message: "客户不能为空", trigger: "change" }
        ],
        warehouseId: [
          { required: true, message: "仓库不能为空", trigger: "change" }
        ],
        returnDate: [
          { required: true, message: "退货日期不能为空", trigger: "change" }
        ],
        outId: [
          { required: true, message: "请选择销售出库单", trigger: "change" }
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
    this.getWarehouseList()
    this.loadApproveAction()
  },
  methods: {
    /** 查询销售退货列表 */
    getList() {
      this.loading = true
      pageSalesReturn(this.queryParams).then(response => {
        this.returnList = response.rows
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
    /** 查询仓库列表 */
    getWarehouseList() {
      listWarehouse().then(response => {
        this.warehouseOptions = response.data
      })
    },
    loadApproveAction() {
      getBillNeedApprove("SALES_RETURN").then(response => {
        this.showApproveAction = response.data !== false
      }).catch(() => {
        this.showApproveAction = true
      })
    },
    /** 查询已审核的销售出库单列表 */
    getApprovedOutList() {
      listApprovedSalesOut().then(response => {
        this.approvedOutOptions = response.data || []
      })
    },
    /** 退货方式切换 */
    handleSourceTypeChange(value) {
      if (value === 2) {
        // 出库单退货模式：加载已审核出库单
        this.getApprovedOutList()
        this.form.outId = null
        this.form.customerId = null
        this.form.warehouseId = null
        this.form.items = []
        this.selectedOutInfo = null
      } else {
        // 直接退货模式：清空出库单相关数据
        this.form.outId = null
        this.form.items = []
        this.selectedOutInfo = null
      }
      // 重置表单验证
      this.$nextTick(() => {
        this.$refs.form.clearValidate()
      })
    },
    /** 销售出库单选择变化 */
    handleOutChange(outId) {
      if (!outId) {
        this.form.customerId = null
        this.form.warehouseId = null
        this.form.items = []
        this.selectedOutInfo = null
        return
      }

      // 获取出库单详情
      getSalesOut(outId).then(response => {
        const out = response.data
        this.selectedOutInfo = out
        this.form.customerId = out.customerId
        this.form.warehouseId = out.warehouseId

        // 获取出库单商品明细
        getSalesOutItems(outId).then(itemResponse => {
          const items = itemResponse.data
          this.form.items = items.map(item => {
            const outQty = item.quantity || 0                // 出库数量
            const returnedQty = item.returnedQty || 0        // 已退货数量
            const availableQty = outQty - returnedQty        // 可退货数量 = 出库数量 - 已退货数量
            return {
              skuId: item.skuId,
              skuCode: item.skuCode,
              goodsName: item.goodsName,
              skuName: item.skuName,
              specInfo: item.specInfo,
              unitName: item.unitName,
              outQuantity: outQty,         // 出库数量
              returnedQty: returnedQty,     // 已退货数量
              availableQty: availableQty,   // 可退货数量
              quantity: availableQty,       // 本次退货数量（默认填入可退货数量）
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

      this.$refs.form.validateField('outId')
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
        sourceType: 1,
        outId: null,
        customerId: null,
        warehouseId: null,
        returnDate: null,
        refundAmount: 0,
        accountId: null,
        handler: null,
        remark: null,
        items: []
      }
      this.selectedOutInfo = null
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
      this.$modal.confirm('是否确认导出销售退货数据？').then(() => {
        this.exportLoading = true
        return exportSalesReturn(this.queryParams)
      }).then(response => {
        const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = '销售退货数据.xlsx'
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
      this.title = "添加销售退货"
      this.form.returnDate = new Date().toISOString().split('T')[0]
      this.form.sourceType = 1  // 默认直接退货
      this.form.handler = this.$store.getters.name
    },
    /** 查看按钮操作 */
    handleView(row) {
      this.reset()
      const id = row.id
      getSalesReturn(id).then(response => {
        this.form = response.data
        // 如果是出库单退货，需要加载出库单信息
        if (this.form.sourceType === 2 && this.form.outId) {
          this.loadOutInfoForView(this.form.outId)
        }
        this.formView = true
        this.open = true
        this.title = "销售退货详情"
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
        path: '/wms/salesReturn/print/index',
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
      getSalesReturn(id).then(response => {
        this.form = response.data
        // 如果是出库单退货，需要加载出库单信息和可退货数量
        if (this.form.sourceType === 2 && this.form.outId) {
          this.loadOutInfoForEdit(this.form.outId)
        }
        this.formView = false
        this.open = true
        this.title = "修改销售退货"
      })
    },
    /** 加载出库单信息（用于查看模式） */
    loadOutInfoForView(outId) {
      getSalesOut(outId).then(response => {
        this.selectedOutInfo = response.data
        // 直接设置出库单号
        this.$set(this.form, 'outNo', response.data.outNo)
        // 后端已经在get方法中直接返回了outQuantity、returnedQty、availableQty等字段
        // 不需要再次调用getSalesOutItems
      })
    },
    /** 加载出库单信息（用于编辑模式） */
    loadOutInfoForEdit(outId) {
      getSalesOut(outId).then(response => {
        this.selectedOutInfo = response.data
        // 直接设置出库单号
        this.$set(this.form, 'outNo', response.data.outNo)
        // 后端已经在get方法中直接返回了outQuantity、returnedQty、availableQty等字段
        // 不需要再次调用getSalesOutItems
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
          // 出库单退货模式下，检查是否有退货数量
          if (this.form.sourceType === 2) {
            const hasQuantity = this.form.items.some(item => item.quantity > 0)
            if (!hasQuantity) {
              this.$modal.msgError("没有可退货的商品")
              return
            }
            // 过滤掉退货数量为0的商品
            this.form.items = this.form.items.filter(item => item.quantity > 0)
          }
          if (this.form.id != null) {
            updateSalesReturn(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addSalesReturn(this.form).then(response => {
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
        return delSalesReturn(row.id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 审核按钮操作 */
    handleApprove(row) {
      this.$modal.confirm('是否确认审核退货单"' + row.returnNo + '"？').then(function() {
        return approveSalesReturn(row.id)
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
      const amount = row.quantity * (row.price || 0)
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
        } else if (column.property === 'outQuantity') {
          sums[index] = data.reduce((sum, row) => sum + (row.outQuantity || 0), 0)
        } else if (column.property === 'returnedQty') {
          sums[index] = data.reduce((sum, row) => sum + (row.returnedQty || 0), 0)
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
