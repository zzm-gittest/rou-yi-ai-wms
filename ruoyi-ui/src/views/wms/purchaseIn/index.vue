<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="入库单号" prop="inNo">
        <el-input
          v-model="queryParams.inNo"
          placeholder="请输入入库单号"
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
        <el-button type="success" plain icon="el-icon-download" size="mini" :loading="exportLoading" :disabled="exportLoading" @click="handleExport" v-hasPermi="['wms:purchaseIn:query']">导出</el-button>
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
          v-hasPermi="['wms:purchaseIn:add']"
        >新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="inList">
      <el-table-column label="入库单号" prop="inNo" min-width="160" />
      <el-table-column label="采购订单号" prop="orderNo" min-width="150" />
      <el-table-column label="供应商" prop="supplierName" min-width="150" />
      <el-table-column label="仓库" prop="warehouseName" min-width="120" />
      <el-table-column label="入库日期" prop="inDate" min-width="110">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.inDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="入库总额" prop="totalAmount" min-width="120" align="right">
        <template slot-scope="scope">
          <span>{{ scope.row.totalAmount ? scope.row.totalAmount.toFixed(2) : '0.00' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="已付金额" prop="paidAmount" min-width="120" align="right">
        <template slot-scope="scope">
          <span>{{ scope.row.paidAmount ? scope.row.paidAmount.toFixed(2) : '0.00' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="付款状态" prop="paymentStatus" min-width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.paymentStatus === '2' ? 'success' : (scope.row.paymentStatus === '1' ? 'warning' : 'info')">
            {{ scope.row.paymentStatus === '0' ? '未付' : (scope.row.paymentStatus === '1' ? '部分' : '已付') }}
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
            v-hasPermi="['wms:purchaseIn:print']"
          >打印</el-button>
          <el-button
            v-if="!isBillApproved(scope.row)"
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['wms:purchaseIn:edit']"
          >修改</el-button>
          <el-button
            v-if="showApproveAction && !isBillApproved(scope.row)"
            size="mini"
            type="text"
            icon="el-icon-check"
            @click="handleApprove(scope.row)"
            v-hasPermi="['wms:purchaseIn:approve']"
          >审核</el-button>
          <el-button
            v-if="showApproveAction && isBillApproved(scope.row)"
            size="mini"
            type="text"
            icon="el-icon-back"
            @click="handleCancelApprove(scope.row)"
            v-hasPermi="['wms:purchaseIn:approve']"
          >反审核</el-button>
          <el-button
            v-if="!isBillApproved(scope.row)"
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['wms:purchaseIn:remove']"
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

    <!-- 添加或修改采购入库对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="1400px" :fullscreen="dialogFullscreen" append-to-body>
      <template slot="title">
        <span>{{ title }}</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="toggleFullscreen">
          <i :class="dialogFullscreen ? 'el-icon-news' : 'el-icon-full-screen'"></i>
          {{ dialogFullscreen ? '还原' : '全屏' }}
        </el-button>
      </template>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <!-- 入库方式选择（仅新增时可选择） -->
        <el-row v-if="!form.id && !formView">
          <el-col :span="24">
            <el-form-item label="入库方式">
              <el-radio-group v-model="form.sourceType" @change="handleSourceTypeChange">
                <el-radio :label="1">直接入库</el-radio>
                <el-radio :label="2">订单入库</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 订单入库模式：选择采购订单（仅新增时可选择） -->
        <el-row v-if="form.sourceType === 2 && !form.id && !formView">
          <el-col :span="24">
            <el-form-item label="采购订单" prop="orderId">
              <el-select
                v-model="form.orderId"
                placeholder="请选择采购订单"
                filterable
                @change="handleOrderChange"
                style="width: 100%"
              >
                <el-option
                  v-for="item in approvedOrderOptions"
                  :key="item.id"
                  :label="`${item.orderNo} - ${item.supplierName} - ${item.totalAmount}元`"
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
              :title="selectedOrderInfo ? `采购订单号：${selectedOrderInfo.orderNo} | 供应商：${selectedOrderInfo.supplierName} | 订单总额：${selectedOrderInfo.totalAmount}元` : '加载订单信息中...'"
              type="info"
              :closable="false"
              style="margin-bottom: 10px"
            />
          </el-col>
        </el-row>

        <el-row>
          <!-- 显示入库单号 -->
          <el-col :span="8" v-if="form.id">
            <el-form-item label="入库单号">
              <el-input v-model="form.inNo" disabled />
            </el-form-item>
          </el-col>
          <!-- 订单入库模式显示采购订单号 -->
          <el-col :span="8" v-if="form.sourceType === 2 && (form.id || formView)">
            <el-form-item label="采购订单号">
              <el-input v-model="form.orderNo" disabled />
            </el-form-item>
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
            <el-form-item label="入库日期" prop="inDate">
              <el-date-picker
                v-model="form.inDate"
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
            <el-form-item label="已付金额" prop="paidAmount">
              <el-input-number v-model="form.paidAmount" :min="0" :precision="2" :disabled="formView" style="width: 100%" />
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

        <el-divider content-position="left">入库明细</el-divider>

        <!-- 订单入库模式：显示订单商品明细 -->
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
                <span>{{ scope.row.orderQuantity || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="已入库数量" prop="receivedQty" width="100" align="right">
              <template slot-scope="scope">
                <span>{{ scope.row.receivedQty || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="本次入库数量" prop="quantity" width="130" align="right">
              <template slot-scope="scope">
                <span v-if="formView || isBillApproved(form)">{{ scope.row.quantity || 0 }}</span>
                <el-input-number
                  v-else
                  v-model="scope.row.quantity"
                  :min="0"
                  :max="getMaxInQuantity(scope.row)"
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
                <span>{{ scope.row.amount ? scope.row.amount.toFixed(2) : '0.00' }}</span>
              </template>
            </el-table-column>
            <el-table-column
              label="备注"
              min-width="120"
            >
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
            title="请先选择采购订单"
            type="warning"
            :closable="false"
            style="margin-top: 10px"
          />
        </div>

        <!-- 直接入库模式：显示可编辑的商品明细 -->
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
                  :max="getMaxInQuantity(scope.row)"
                  :precision="2"
                  controls-position="right"
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
            <el-table-column
              label="备注"
              min-width="120"
            >
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

    <!-- 快速新增供应商 -->
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
import { pagePurchaseIn, getPurchaseIn, delPurchaseIn, addPurchaseIn, updatePurchaseIn, approvePurchaseIn, cancelApprovePurchaseIn, getOrderItems, exportPurchaseIn } from "@/api/wms/purchaseIn"
import { listSupplier, addSupplier } from "@/api/wms/supplier"
import { listWarehouse, addWarehouse } from "@/api/wms/warehouse"
import { getAvailablePurchaseOrdersForIn, getPurchaseOrder } from "@/api/wms/purchaseOrder"
import { getBillNeedApprove } from "@/api/wms/approvePolicy"
import GoodsSkuSelector from "@/components/GoodsSkuSelector"
import routeBillNoFilterMixin from "@/views/wms/mixins/routeBillNoFilterMixin"

export default {
  name: "PurchaseIn",
  components: { GoodsSkuSelector },
  mixins: [routeBillNoFilterMixin],
  data() {
    return {
      routeBillNoQueryKey: 'inNo',
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 是否显示审核相关按钮
      showApproveAction: true,
      // 采购入库表格数据
      inList: [],
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
      // 已审核的采购订单选项
      approvedOrderOptions: [],
      // 当前选中的订单信息
      selectedOrderInfo: null,
      // 当前编辑的明细行索引
      currentIndex: -1,
      // 快速新增供应商弹窗
      quickSupplierOpen: false,
      // 快速新增仓库弹窗
      quickWarehouseOpen: false,
      // 导出相关
      exportLoading: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        inNo: null,
        supplierId: null,
        warehouseId: null,
        approveStatus: null,
        paymentStatus: null
      },
      // 表单参数
      form: {
        items: [],
        sourceType: 1  // 默认直接入库
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
      quickWarehouseForm: {
        warehouseName: null,
        contact: null,
        phone: null,
        status: "0",
        sort: 0
      },
      // 表单校验
      rules: {
        supplierId: [
          { required: true, message: "供应商不能为空", trigger: "change" }
        ],
        warehouseId: [
          { required: true, message: "仓库不能为空", trigger: "change" }
        ],
        inDate: [
          { required: true, message: "入库日期不能为空", trigger: "change" }
        ],
        orderId: [
          { required: true, message: "请选择采购订单", trigger: "change" }
        ]
      },
      quickSupplierRules: {
        supplierName: [
          { required: true, message: "供应商名称不能为空", trigger: "blur" }
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
    this.getSupplierList()
    this.getWarehouseList()
    this.loadApproveAction()
  },
  methods: {
    /** 查询采购入库列表 */
    getList() {
      this.loading = true
      pagePurchaseIn(this.queryParams).then(response => {
        this.inList = response.rows
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
    /** 查询仓库列表 */
    getWarehouseList() {
      listWarehouse().then(response => {
        this.warehouseOptions = response.data
      })
    },
    loadApproveAction() {
      getBillNeedApprove("PURCHASE_IN").then(response => {
        this.showApproveAction = response.data !== false
      }).catch(() => {
        this.showApproveAction = true
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
    openQuickSupplierDialog() {
      this.resetQuickSupplierForm()
      this.quickSupplierOpen = true
    },
    openQuickWarehouseDialog() {
      this.resetQuickWarehouseForm()
      this.quickWarehouseOpen = true
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
            this.supplierOptions = response.data
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
    /** 查询可入库的采购订单列表（已审核且未全部入库） */
    getApprovedOrderList() {
      getAvailablePurchaseOrdersForIn().then(response => {
        this.approvedOrderOptions = response.data || []
      })
    },
    /** 入库方式切换 */
    handleSourceTypeChange(value) {
      if (value === 2) {
        // 订单入库模式：加载已审核订单
        this.getApprovedOrderList()
        this.form.orderId = null
        this.form.supplierId = null
        this.form.items = []
        this.selectedOrderInfo = null
      } else {
        // 直接入库模式：清空订单相关数据
        this.form.orderId = null
        this.form.items = []
        this.selectedOrderInfo = null
      }
      // 重置表单验证
      this.$nextTick(() => {
        this.$refs.form.clearValidate()
      })
    },
    /** 采购订单选择变化 */
    handleOrderChange(orderId) {
      if (!orderId) {
        this.form.supplierId = null
        this.form.items = []
        this.selectedOrderInfo = null
        return
      }

      // 获取订单详情
      getPurchaseOrder(orderId).then(response => {
        const order = response.data
        this.selectedOrderInfo = order
        this.form.supplierId = order.supplierId

        // 获取订单商品明细
        getOrderItems(orderId).then(itemResponse => {
          const items = itemResponse.data
          this.form.items = items.map(item => {
            const orderQty = item.quantity || 0              // 订单数量
            const receivedQty = item.receivedQty || 0        // 已入库数量（后端返回）
            const availableQty = orderQty - receivedQty      // 可入库数量 = 订单数量 - 已入库数量
            // 折扣按比例分摊
            const proratedDiscount = orderQty > 0 ? (item.discount || 0) * (availableQty / orderQty) : 0
            return {
              skuId: item.skuId,
              skuCode: item.skuCode,
              goodsName: item.goodsName,
              skuName: item.skuName,
              specInfo: item.specInfo,
              unitName: item.unitName,
              orderQuantity: orderQty,         // 订单数量
              receivedQty: receivedQty,         // 已入库数量
              availableQty: availableQty,       // 可入库数量
              quantity: availableQty,          // 本次入库数量（默认填入可入库数量）
              price: item.price,
              discount: proratedDiscount,
              amount: availableQty * (item.price || 0) - proratedDiscount,
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
    /** 供应商变化 */
    handleSupplierChange() {
      // 如果是直接入库模式，清空商品列表
      if (this.form.sourceType === 1) {
        // 不做处理
      }
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
        warehouseId: null,
        orderId: null,
        sourceType: 1,
        inDate: null,
        paidAmount: 0,
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
      this.$modal.confirm('是否确认导出采购入库数据？').then(() => {
        this.exportLoading = true
        return exportPurchaseIn(this.queryParams)
      }).then(response => {
        const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = '采购入库数据.xlsx'
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
      this.title = "添加采购入库"
      this.form.inDate = new Date().toISOString().split('T')[0]
      this.form.sourceType = 1  // 默认直接入库
      this.form.handler = this.$store.getters.name
    },
    /** 查看按钮操作 */
    handleView(row) {
      this.reset()
      const id = row.id
      getPurchaseIn(id).then(response => {
        this.form = response.data
        // 如果是订单入库，需要加载订单信息
        if (this.form.sourceType === 2 && this.form.orderId) {
          this.loadOrderInfoForView(this.form.orderId)
        }
        this.formView = true
        this.open = true
        this.title = "采购入库详情"
      })
    },
    handlePrint(row) {
      if (!this.isBillApproved(row)) {
        this.$modal.msgWarning("仅已审核单据支持打印")
        return
      }
      const route = this.$router.resolve({
        path: '/wms/purchaseIn/print/index',
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
      getPurchaseIn(id).then(response => {
        this.form = response.data
        // 如果是订单入库，需要加载订单信息和可入库数量
        if (this.form.sourceType === 2 && this.form.orderId) {
          this.loadOrderInfoForEdit(this.form.orderId)
        }
        this.formView = false
        this.open = true
        this.title = "修改采购入库"
      })
    },
    /** 加载订单信息（用于查看模式） */
    loadOrderInfoForView(orderId) {
      getPurchaseOrder(orderId).then(response => {
        this.selectedOrderInfo = response.data
        // 直接设置订单号
        this.$set(this.form, 'orderNo', response.data.orderNo)
        // 将items转换为订单模式格式，用于显示
        if (this.form.items && this.form.items.length > 0) {
          getOrderItems(orderId).then(itemResponse => {
            const orderItems = itemResponse.data
            // 创建订单item的映射
            const orderItemMap = {}
            orderItems.forEach(item => {
              orderItemMap[item.skuId] = item
            })
            // 为每个item添加订单相关字段
            // 注意：后端返回的receivedQty是已入库数量
            this.form.items.forEach(item => {
              const orderItem = orderItemMap[item.skuId]
              if (orderItem) {
                const orderQty = orderItem.quantity || 0           // 订单数量
                const receivedQty = orderItem.receivedQty || 0     // 已入库数量（后端返回）
                const availableQty = orderQty - receivedQty        // 可入库数量
                item.orderQuantity = orderQty
                item.receivedQty = receivedQty
                item.availableQty = availableQty
              }
            })
          })
        }
      })
    },
    /** 加载订单信息（用于编辑模式） */
    loadOrderInfoForEdit(orderId) {
      getPurchaseOrder(orderId).then(response => {
        this.selectedOrderInfo = response.data
        // 直接设置订单号
        this.$set(this.form, 'orderNo', response.data.orderNo)
        // 获取订单商品明细，包含可入库数量
        getOrderItems(orderId).then(itemResponse => {
          const orderItems = itemResponse.data
          // 创建订单item的映射
          const orderItemMap = {}
          orderItems.forEach(item => {
            orderItemMap[item.skuId] = item
          })
          // 为当前form.items添加订单相关字段
          // 注意：后端返回的receivedQty是已入库数量
          this.form.items.forEach(item => {
            const orderItem = orderItemMap[item.skuId]
            if (orderItem) {
              const orderQty = orderItem.quantity || 0           // 订单数量
              const receivedQty = orderItem.receivedQty || 0     // 已入库数量（后端返回）
              const availableQty = orderQty - receivedQty        // 可入库数量
              item.orderQuantity = orderQty
              item.receivedQty = receivedQty
              item.availableQty = availableQty
            }
          })
        })
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.items.length === 0) {
            this.$modal.msgError("请添加入库明细")
            return
          }
          // 订单入库模式下，检查是否有入库数量
          if (this.form.sourceType === 2) {
            const hasQuantity = this.form.items.some(item => item.quantity > 0)
            if (!hasQuantity) {
              this.$modal.msgError("没有可入库的商品")
              return
            }
            // 过滤掉入库数量为0的商品
            this.form.items = this.form.items.filter(item => item.quantity > 0)
          }
          if (this.form.id != null) {
            updatePurchaseIn(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addPurchaseIn(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除入库单"' + row.inNo + '"？').then(function() {
        return delPurchaseIn(row.id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 审核按钮操作 */
    handleApprove(row) {
      this.$modal.confirm('是否确认审核入库单"' + row.inNo + '"？').then(function() {
        return approvePurchaseIn(row.id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("审核成功")
      }).catch(() => {})
    },
    /** 反审核按钮操作 */
    handleCancelApprove(row) {
      this.$modal.confirm('是否确认反审核入库单"' + row.inNo + '"？反审核将回退库存和订单入库数量。').then(function() {
        return cancelApprovePurchaseIn(row.id)
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
      const amount = row.quantity * (row.price || 0) - (row.discount || 0)
      row.amount = amount
    },
    /** 获取最大入库数量 */
    getMaxInQuantity(row) {
      // 如果是订单入库，不能超过可入库数量
      if (this.form.sourceType === 2 && row.availableQty !== undefined) {
        const maxQty = row.availableQty || 0
        return maxQty > 0 ? maxQty : 0
      }
      // 直接入库没有限制
      return 999999
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
        } else if (column.property === 'receivedQty') {
          sums[index] = data.reduce((sum, row) => sum + (row.receivedQty || 0), 0)
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
