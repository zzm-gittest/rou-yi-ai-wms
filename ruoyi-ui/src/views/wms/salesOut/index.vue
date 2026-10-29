<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="出库单号" prop="outNo">
        <el-input
          v-model="queryParams.outNo"
          placeholder="请输入出库单号"
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
        <el-button type="success" plain icon="el-icon-download" size="mini" :loading="exportLoading" :disabled="exportLoading" @click="handleExport" v-hasPermi="['wms:salesOut:list']">导出</el-button>
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
          v-hasPermi="['wms:salesOut:add']"
        >新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="outList">
      <el-table-column label="出库单号" prop="outNo" min-width="160" />
      <el-table-column label="销售订单号" prop="orderNo" min-width="150" />
      <el-table-column label="客户" prop="customerName" min-width="150" />
      <el-table-column label="仓库" prop="warehouseName" min-width="120" />
      <el-table-column label="出库日期" prop="outDate" min-width="110">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.outDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="出库总额" prop="totalAmount" min-width="120" align="right">
        <template slot-scope="scope">
          <span>{{ scope.row.totalAmount ? scope.row.totalAmount.toFixed(2) : '0.00' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="已收金额" prop="receivedAmount" min-width="120" align="right">
        <template slot-scope="scope">
          <span>{{ scope.row.receivedAmount ? scope.row.receivedAmount.toFixed(2) : '0.00' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="收款状态" prop="paymentStatus" min-width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.paymentStatus === '2' ? 'success' : (scope.row.paymentStatus === '1' ? 'warning' : 'info')">
            {{ scope.row.paymentStatus === '0' ? '未收' : (scope.row.paymentStatus === '1' ? '部分' : '已收') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审核状态" prop="approveStatus" min-width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="isBillApproved(scope.row) ? 'success' : 'warning'">
            {{ isBillApproved(scope.row) ? '已审核' : '待审核' }}
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
            v-if="isBillApproved(scope.row)"
            size="mini"
            type="text"
            icon="el-icon-printer"
            @click="handlePrint(scope.row)"
            v-hasPermi="['wms:salesOut:print']"
          >打印</el-button>
          <el-button
            v-if="!isBillApproved(scope.row)"
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['wms:salesOut:edit']"
          >修改</el-button>
          <el-button
            v-if="showApproveAction && !isBillApproved(scope.row)"
            size="mini"
            type="text"
            icon="el-icon-check"
            @click="handleApprove(scope.row)"
            v-hasPermi="['wms:salesOut:approve']"
          >审核</el-button>
          <el-button
            v-if="showApproveAction && isBillApproved(scope.row)"
            size="mini"
            type="text"
            icon="el-icon-back"
            @click="handleCancelApprove(scope.row)"
            v-hasPermi="['wms:salesOut:approve']"
          >反审核</el-button>
          <el-button
            v-if="!isBillApproved(scope.row)"
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['wms:salesOut:remove']"
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

    <!-- 添加或修改销售出库对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="1600px" :fullscreen="dialogFullscreen" append-to-body>
      <template slot="title">
        <span>{{ title }}</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="toggleFullscreen">
          <i :class="dialogFullscreen ? 'el-icon-news' : 'el-icon-full-screen'"></i>
          {{ dialogFullscreen ? '还原' : '全屏' }}
        </el-button>
      </template>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <!-- 出库方式选择（仅新增时可选择） -->
        <el-row v-if="!form.id && !formView">
          <el-col :span="24">
            <el-form-item label="出库方式">
              <el-radio-group v-model="form.sourceType" @change="handleSourceTypeChange">
                <el-radio :label="1">直接出库</el-radio>
                <el-radio :label="2">订单出库</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 订单出库模式：选择销售订单（仅新增时可选择） -->
        <el-row v-if="form.sourceType === 2 && !form.id && !formView">
          <el-col :span="24">
            <el-form-item label="销售订单" prop="orderId">
              <el-select
                v-model="form.orderId"
                placeholder="请选择销售订单"
                filterable
                @change="handleOrderChange"
                style="width: 100%"
              >
                <el-option
                  v-for="item in approvedOrderOptions"
                  :key="item.id"
                  :label="`${item.orderNo} - ${item.customerName} - ${item.receivableAmount}元`"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 订单信息展示 -->
        <el-row v-if="form.sourceType === 2 && form.orderId">
          <el-col :span="24">
            <el-alert
              :title="selectedOrderInfo ? `销售订单号：${selectedOrderInfo.orderNo} | 客户：${selectedOrderInfo.customerName} | 订单总额：${selectedOrderInfo.receivableAmount}元` : '加载订单信息中...'"
              type="info"
              :closable="false"
              style="margin-bottom: 10px"
            />
          </el-col>
        </el-row>

        <el-row>
          <!-- 显示出库单号 -->
          <el-col :span="8" v-if="form.id">
            <el-form-item label="出库单号">
              <el-input v-model="form.outNo" disabled />
            </el-form-item>
          </el-col>
          <!-- 订单出库模式显示销售订单号 -->
          <el-col :span="8" v-if="form.sourceType === 2 && (form.id || formView)">
            <el-form-item label="销售订单号">
              <el-input v-model="form.orderNo" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="客户" prop="customerId">
              <div class="select-with-action">
                <el-select v-model="form.customerId" placeholder="请选择客户" :disabled="formView || form.id != null || form.sourceType === 2">
                  <el-option
                    v-for="item in customerOptions"
                    :key="item.id"
                    :label="item.customerName"
                    :value="item.id"
                  />
                </el-select>
                <el-button
                  v-if="!formView && form.id == null && form.sourceType !== 2"
                  type="text"
                  icon="el-icon-plus"
                  @click="openQuickCustomerDialog"
                  v-hasPermi="['wms:customer:add']"
                >新增</el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="仓库" prop="warehouseId">
              <div class="select-with-action">
                <el-select v-model="form.warehouseId" placeholder="请选择仓库" :disabled="formView">
                  <el-option
                    v-for="item in warehouseOptions"
                    :key="item.id"
                    :label="item.warehouseName"
                    :value="item.id"
                  />
                </el-select>
                <el-button
                  v-if="!formView"
                  type="text"
                  icon="el-icon-plus"
                  @click="openQuickWarehouseDialog"
                  v-hasPermi="['wms:warehouse:add']"
                >新增</el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="出库日期" prop="outDate">
              <el-date-picker
                v-model="form.outDate"
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

        <el-divider content-position="left">出库明细</el-divider>

        <!-- 订单出库模式：显示订单商品明细 -->
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
            <el-table-column label="订单数量" prop="orderQuantity" width="90" align="right">
              <template slot-scope="scope">
                <span>{{ scope.row.orderQuantity !== undefined ? scope.row.orderQuantity : '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="已发货数量" prop="deliveredQty" width="100" align="right">
              <template slot-scope="scope">
                <span>{{ scope.row.deliveredQty || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="本次出库数量" prop="quantity" width="130" align="right">
              <template slot-scope="scope">
                <span v-if="formView || isBillApproved(form)">{{ scope.row.quantity || 0 }}</span>
                <el-input-number
                  v-else
                  v-model="scope.row.quantity"
                  :min="0"
                  :max="getMaxOutQuantity(scope.row)"
                  :precision="2"
                  controls-position="right"
                  @change="handleItemChange(scope.row)"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column label="单价" prop="price" width="100" align="right">
              <template slot-scope="scope">
                <span>{{ scope.row.price ? scope.row.price.toFixed(2) : '0.00' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="金额" prop="amount" width="110" align="right">
              <template slot-scope="scope">
                <span>{{ ((scope.row.quantity || 0) * (scope.row.price || 0)).toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="备注" min-width="120">
              <template slot-scope="scope">
                <el-input v-model="scope.row.remark" placeholder="备注" :disabled="formView" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="60" v-if="!formView && !isBillApproved(form)">
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
          <el-alert
            v-if="form.items.length === 0"
            title="请先选择销售订单"
            type="warning"
            :closable="false"
            style="margin-top: 10px"
          />
        </div>

        <!-- 直接送出库模式：显示可编辑的商品明细 -->
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
                <span v-if="formView">{{ scope.row.quantity || 0 }}</span>
                <el-input-number
                  v-else
                  v-model="scope.row.quantity"
                  :min="0"
                  :precision="2"
                  controls-position="right"
                  :disabled="formView"
                  @change="handleItemChange(scope.row)"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column label="单价" width="120" align="right">
              <template slot-scope="scope">
                <span v-if="formView">{{ scope.row.price ? scope.row.price.toFixed(2) : '0.00' }}</span>
                <el-input-number
                  v-else
                  v-model="scope.row.price"
                  :min="0"
                  :precision="2"
                  controls-position="right"
                  :disabled="formView"
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
                <span v-if="formView">{{ scope.row.remark || '-' }}</span>
                <el-input v-else v-model="scope.row.remark" placeholder="备注" :disabled="formView" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="60" v-if="!formView && !isBillApproved(form)">
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

    <!-- 快速新增客户 -->
    <el-dialog title="快速新增客户" :visible.sync="quickCustomerOpen" width="520px" append-to-body>
      <el-form ref="quickCustomerForm" :model="quickCustomerForm" :rules="quickCustomerRules" label-width="90px">
        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="quickCustomerForm.customerName" placeholder="请输入客户名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="联系人" prop="contact">
          <el-input v-model="quickCustomerForm.contact" placeholder="请输入联系人" maxlength="50" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="quickCustomerForm.phone" placeholder="请输入联系电话" maxlength="20" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitQuickCustomer">确 定</el-button>
        <el-button @click="quickCustomerOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 快速新增仓库 -->
    <el-dialog title="快速新增仓库" :visible.sync="quickWarehouseOpen" width="520px" append-to-body>
      <el-form ref="quickWarehouseForm" :model="quickWarehouseForm" :rules="quickWarehouseRules" label-width="90px">
        <el-form-item label="仓库名称" prop="warehouseName">
          <el-input v-model="quickWarehouseForm.warehouseName" placeholder="请输入仓库名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="联系人" prop="contact">
          <el-input v-model="quickWarehouseForm.contact" placeholder="请输入联系人" maxlength="50" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="quickWarehouseForm.phone" placeholder="请输入联系电话" maxlength="20" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitQuickWarehouse">确 定</el-button>
        <el-button @click="quickWarehouseOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { pageSalesOut, getSalesOut, delSalesOut, addSalesOut, updateSalesOut, approveSalesOut, cancelApproveSalesOut, exportSalesOut } from "@/api/wms/salesOut"
import { listCustomer, addCustomer } from "@/api/wms/customer"
import { listWarehouse, addWarehouse } from "@/api/wms/warehouse"
import { listApprovedSalesOrder, getSalesOrderItems, getSalesOrder } from "@/api/wms/salesOrder"
import { getBillNeedApprove } from "@/api/wms/approvePolicy"
import GoodsSkuSelector from "@/components/GoodsSkuSelector"
import routeBillNoFilterMixin from "@/views/wms/mixins/routeBillNoFilterMixin"

export default {
  name: "SalesOut",
  components: { GoodsSkuSelector },
  mixins: [routeBillNoFilterMixin],
  data() {
    return {
      routeBillNoQueryKey: 'outNo',
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 是否显示审核相关按钮
      showApproveAction: true,
      // 销售出库表格数据
      outList: [],
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
      // 已审核的销售订单选项
      approvedOrderOptions: [],
      // 当前选中的订单信息
      selectedOrderInfo: null,
      // 当前编辑的明细行索引
      currentIndex: -1,
      // 快速新增客户弹窗
      quickCustomerOpen: false,
      // 快速新增仓库弹窗
      quickWarehouseOpen: false,
      // 导出相关
      exportLoading: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        outNo: null,
        customerId: null,
        warehouseId: null,
        approveStatus: null,
        paymentStatus: null
      },
      // 表单参数
      form: {
        items: [],
        sourceType: 1  // 默认直接出库
      },
      quickCustomerForm: {
        customerName: null,
        contact: null,
        phone: null,
        customerType: "1",
        status: "0",
        settlementType: "1",
        creditLimit: 0,
        paymentDays: 0
      },
      quickWarehouseForm: {
        warehouseName: null,
        contact: null,
        phone: null,
        status: "0",
        sort: 0
      },
      // 表单校验
      rules: {
        customerId: [
          { required: true, message: "客户不能为空", trigger: "change" }
        ],
        warehouseId: [
          { required: true, message: "仓库不能为空", trigger: "change" }
        ],
        outDate: [
          { required: true, message: "出库日期不能为空", trigger: "change" }
        ],
        orderId: [
          { required: true, message: "请选择销售订单", trigger: "change" }
        ]
      },
      quickCustomerRules: {
        customerName: [
          { required: true, message: "客户名称不能为空", trigger: "blur" }
        ]
      },
      quickWarehouseRules: {
        warehouseName: [
          { required: true, message: "仓库名称不能为空", trigger: "blur" }
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
    /** 查询销售出库列表 */
    getList() {
      this.loading = true
      pageSalesOut(this.queryParams).then(response => {
        this.outList = response.rows
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
      getBillNeedApprove("SALES_OUT").then(response => {
        this.showApproveAction = response.data !== false
      }).catch(() => {
        this.showApproveAction = true
      })
    },
    resetQuickCustomerForm() {
      this.quickCustomerForm = {
        customerName: null,
        contact: null,
        phone: null,
        customerType: "1",
        status: "0",
        settlementType: "1",
        creditLimit: 0,
        paymentDays: 0
      }
      this.$nextTick(() => {
        this.$refs.quickCustomerForm && this.$refs.quickCustomerForm.clearValidate()
      })
    },
    resetQuickWarehouseForm() {
      this.quickWarehouseForm = {
        warehouseName: null,
        contact: null,
        phone: null,
        status: "0",
        sort: 0
      }
      this.$nextTick(() => {
        this.$refs.quickWarehouseForm && this.$refs.quickWarehouseForm.clearValidate()
      })
    },
    openQuickCustomerDialog() {
      this.resetQuickCustomerForm()
      this.quickCustomerOpen = true
    },
    openQuickWarehouseDialog() {
      this.resetQuickWarehouseForm()
      this.quickWarehouseOpen = true
    },
    submitQuickCustomer() {
      this.$refs.quickCustomerForm.validate(valid => {
        if (!valid) {
          return
        }
        addCustomer(this.quickCustomerForm).then(() => {
          this.$modal.msgSuccess("客户新增成功")
          this.quickCustomerOpen = false
          listCustomer().then(response => {
            this.customerOptions = response.data
            const created = this.customerOptions
              .filter(item => item.customerName === this.quickCustomerForm.customerName)
              .sort((a, b) => Number(b.id || 0) - Number(a.id || 0))[0]
            if (created) {
              this.form.customerId = created.id
            }
          })
        })
      })
    },
    submitQuickWarehouse() {
      this.$refs.quickWarehouseForm.validate(valid => {
        if (!valid) {
          return
        }
        addWarehouse(this.quickWarehouseForm).then(() => {
          this.$modal.msgSuccess("仓库新增成功")
          this.quickWarehouseOpen = false
          listWarehouse().then(response => {
            this.warehouseOptions = response.data
            const created = this.warehouseOptions
              .filter(item => item.warehouseName === this.quickWarehouseForm.warehouseName)
              .sort((a, b) => Number(b.id || 0) - Number(a.id || 0))[0]
            if (created) {
              this.form.warehouseId = created.id
            }
          })
        })
      })
    },
    /** 查询已审核的销售订单列表（已审核且未全部发货） */
    getApprovedOrderList() {
      listApprovedSalesOrder().then(response => {
        this.approvedOrderOptions = response.data || []
      })
    },
    /** 出库方式切换 */
    handleSourceTypeChange(value) {
      if (value === 2) {
        // 订单出库模式：加载已审核订单
        this.getApprovedOrderList()
        this.form.orderId = null
        this.form.customerId = null
        this.form.items = []
        this.selectedOrderInfo = null
      } else {
        // 直接送出库模式：清空订单相关数据
        this.form.orderId = null
        this.form.items = []
        this.selectedOrderInfo = null
      }
      // 重置表单验证
      this.$nextTick(() => {
        this.$refs.form.clearValidate()
      })
    },
    /** 销售订单选择变化 */
    handleOrderChange(orderId) {
      if (!orderId) {
        this.form.customerId = null
        this.form.items = []
        this.selectedOrderInfo = null
        return
      }

      // 获取订单详情
      getSalesOrder(orderId).then(response => {
        const order = response.data
        this.selectedOrderInfo = order
        this.form.customerId = order.customerId

        // 获取订单商品明细
        getSalesOrderItems(orderId).then(itemResponse => {
          const items = itemResponse.data
          this.form.items = items.map(item => {
            const orderQty = item.quantity || 0              // 订单数量
            const deliveredQty = item.deliveredQty || 0      // 已发货数量
            const availableQty = orderQty - deliveredQty      // 可发货数量 = 订单数量 - 已发货数量
            return {
              skuId: item.skuId,
              skuCode: item.skuCode,
              goodsName: item.goodsName,
              skuName: item.skuName,
              specInfo: item.specInfo,
              unitName: item.unitName,
              orderQuantity: orderQty,         // 订单数量
              deliveredQty: deliveredQty,       // 已发货数量
              availableQty: availableQty,       // 可发货数量
              quantity: availableQty,          // 本次出库数量（默认填入可发货数量）
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

      this.$refs.form.validateField('orderId')
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
        customerId: null,
        warehouseId: null,
        orderId: null,
        sourceType: 1,
        outDate: null,
        receivedAmount: 0,
        accountId: null,
        handler: null,
        remark: null,
        items: []
      }
      this.selectedOrderInfo = null
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
      this.$modal.confirm('是否确认导出销售出库数据？').then(() => {
        this.exportLoading = true
        return exportSalesOut(this.queryParams)
      }).then(response => {
        const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = '销售出库数据.xlsx'
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
      this.title = "添加销售出库"
      this.form.outDate = new Date().toISOString().split('T')[0]
      this.form.sourceType = 1  // 默认直接出库
      this.form.handler = this.$store.getters.name
    },
    /** 查看按钮操作 */
    handleView(row) {
      this.reset()
      const id = row.id
      getSalesOut(id).then(response => {
        this.form = response.data
        // 如果是订单出库，需要加载订单信息
        if (this.form.sourceType === 2 && this.form.orderId) {
          this.loadOrderInfoForView(this.form.orderId)
        }
        this.formView = true
        this.open = true
        this.title = "销售出库详情"
      })
    },
    handlePrint(row) {
      if (!this.isBillApproved(row)) {
        this.$modal.msgWarning("仅已审核单据支持打印")
        return
      }
      const route = this.$router.resolve({
        path: '/wms/salesOut/print/index',
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
      getSalesOut(id).then(response => {
        this.form = response.data
        // 如果是订单出库，需要加载订单信息和可发货数量
        if (this.form.sourceType === 2 && this.form.orderId) {
          this.loadOrderInfoForEdit(this.form.orderId)
        }
        this.formView = false
        this.open = true
        this.title = "修改销售出库"
      })
    },
    /** 加载订单信息（用于查看模式） */
    loadOrderInfoForView(orderId) {
      getSalesOrder(orderId).then(response => {
        this.selectedOrderInfo = response.data
        // 直接设置订单号
        this.$set(this.form, 'orderNo', response.data.orderNo)
        // 订单相关字段（orderQuantity, deliveredQty, availableQty）已由后端填充，无需额外处理
      })
    },
    /** 加载订单信息（用于编辑模式） */
    loadOrderInfoForEdit(orderId) {
      getSalesOrder(orderId).then(response => {
        this.selectedOrderInfo = response.data
        // 直接设置订单号
        this.$set(this.form, 'orderNo', response.data.orderNo)
        // 订单相关字段（orderQuantity, deliveredQty, availableQty）已由后端填充，无需额外处理
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.items.length === 0) {
            this.$modal.msgError("请添加出库明细")
            return
          }
          // 订单出库模式下，检查是否有出库数量
          if (this.form.sourceType === 2) {
            const hasQuantity = this.form.items.some(item => item.quantity > 0)
            if (!hasQuantity) {
              this.$modal.msgError("没有可发货的商品")
              return
            }
            // 过滤掉出库数量为0的商品
            this.form.items = this.form.items.filter(item => item.quantity > 0)
          }
          if (this.form.id != null) {
            updateSalesOut(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addSalesOut(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除出库单"' + row.outNo + '"？').then(function() {
        return delSalesOut(row.id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 审核按钮操作 */
    handleApprove(row) {
      this.$modal.confirm('是否确认审核出库单"' + row.outNo + '"？').then(function() {
        return approveSalesOut(row.id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("审核成功")
      }).catch(() => {})
    },
    /** 反审核按钮操作 */
    handleCancelApprove(row) {
      this.$modal.confirm('是否确认反审核出库单"' + row.outNo + '"？反审核将回退库存和订单发货数量。').then(function() {
        return cancelApproveSalesOut(row.id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("反审核成功")
      }).catch(() => {})
    },
    /** 删除明细 */
    handleDeleteItem(index) {
      this.form.items.splice(index, 1)
    },
    canAddItems() {
      return !this.formView && !this.isBillApproved(this.form) && this.form.sourceType !== 2
    },
    /** 单据是否已审核（兼容历史脏数据：approveBy空格、status兜底） */
    isBillApproved(bill) {
      if (!bill) {
        return false
      }

      const approveBy = bill.approveBy
      const approveByText = approveBy === null || approveBy === undefined ? '' : String(approveBy).trim().toLowerCase()
      if (approveByText !== '' && approveByText !== 'null' && approveByText !== 'undefined') {
        return true
      }

      const approveStatus = String(bill.approveStatus || '').trim()
      if (approveStatus === '0' || approveStatus === '1') {
        return approveStatus === '1'
      }

      const status = String(bill.status || '').trim()
      if (status === '0' || status === '1') {
        return status === '1'
      }

      return false
    },
    /** 明细数据变化 */
    handleItemChange(row) {
      const amount = row.quantity * (row.price || 0)
      row.amount = amount
    },
    /** 计算最大可出库数量（订单数量 - 已发货数量） */
    getMaxOutQuantity(row) {
      const orderQty = row.orderQuantity || 0
      const deliveredQty = row.deliveredQty || 0
      const maxQty = orderQty - deliveredQty
      return maxQty > 0 ? maxQty : 0
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
        } else if (column.property === 'orderQuantity') {
          sums[index] = data.reduce((sum, row) => sum + (row.orderQuantity || 0), 0)
        } else if (column.property === 'deliveredQty') {
          sums[index] = data.reduce((sum, row) => sum + (row.deliveredQty || 0), 0)
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
.select-with-action {
  display: flex;
  align-items: center;
  gap: 8px;
}

.select-with-action .el-select {
  flex: 1;
}

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
