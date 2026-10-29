<template>
  <div class="app-container basic-import-page">
    <el-card shadow="never" class="top-card">
      <div class="top-row">
        <div>
          <h3>数据迁移中心</h3>
          <p>先导入基础数据（客户/仓库/SKU），再完成本页 Excel 导入。</p>
        </div>
        <div class="top-actions">
          <el-button
            type="primary"
            icon="el-icon-download"
            @click="handleDownloadPackage"
            v-hasPermi="['wms:goods:importTemplate']"
          >下载模板包</el-button>
        </div>
      </div>
    </el-card>

    <el-row :gutter="16" class="card-row">
      <el-col :xs="24" :sm="12" :md="8" v-for="item in typeOptions" :key="item.type">
        <el-card shadow="hover" class="type-card">
          <div class="type-title">{{ item.label }}</div>
          <div class="type-desc">{{ item.desc }}</div>
          <div class="type-actions">
            <el-button
              size="mini"
              plain
              icon="el-icon-download"
              @click="handleDownloadTemplate(item.type)"
              v-hasPermi="['wms:goods:importTemplate']"
            >下载模板</el-button>
            <el-button
              size="mini"
              type="primary"
              plain
              icon="el-icon-upload2"
              @click="openImportDialog(item.type)"
              v-hasPermi="['wms:goods:import']"
            >导入</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="task-card">
      <div slot="header" class="clearfix">
        <span>导入任务</span>
      </div>

      <el-table :data="taskList" v-loading="taskLoading" border>
        <el-table-column prop="taskNo" label="任务编号" min-width="150" show-overflow-tooltip />
        <el-table-column label="任务类型" min-width="110">
          <template slot-scope="scope">
            {{ getTypeLabel(scope.row.taskType) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="100">
          <template slot-scope="scope">
            <el-tag :type="statusTagType(scope.row.status)">{{ scope.row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="成功/失败" min-width="120">
          <template slot-scope="scope">
            {{ scope.row.successRows || 0 }}/{{ scope.row.failRows || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="totalRows" label="总行数" min-width="90" />
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column prop="endedTime" label="结束时间" min-width="160" />
        <el-table-column label="操作" width="280" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="handleViewReport(scope.row)">报告</el-button>
            <el-button
              type="text"
              size="mini"
              @click="handleExecute(scope.row)"
              v-if="scope.row.status === 0"
            >执行</el-button>
            <el-button
              type="text"
              size="mini"
              @click="openImportDialog(scope.row.taskType, scope.row.id)"
              v-if="scope.row.status === 3 || scope.row.status === 4"
            >失败重试</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="taskTotal > 0"
        :total="taskTotal"
        :page.sync="taskQuery.pageNo"
        :limit.sync="taskQuery.pageSize"
        @pagination="getTaskList"
      />
    </el-card>

    <el-dialog :title="dialogTitle" :visible.sync="dialogOpen" width="980px" :close-on-click-modal="false">
      <el-steps :active="stepActive" finish-status="success" align-center class="step-bar">
        <el-step title="选择类型" />
        <el-step title="上传文件" />
        <el-step title="字段映射" />
        <el-step title="预览执行" />
      </el-steps>

      <el-form label-width="110px" size="small">
        <el-form-item label="导入类型">
          <el-tag>{{ getTypeLabel(activeType) }}</el-tag>
        </el-form-item>
        <el-form-item label="商品模板类型" v-if="activeType === 'goods'">
          <el-radio-group v-model="goodsTemplateType">
            <el-radio label="simple">简易模板（无规格）</el-radio>
            <el-radio label="full">完整模板（有规格）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="金额策略" v-if="activeType === 'purchase_in'">
          <el-radio-group v-model="purchaseAmountStrategy">
            <el-radio label="strict">严格校验</el-radio>
            <el-radio label="auto">自动重算</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="金额策略" v-if="activeType === 'sales_out'">
          <el-radio-group v-model="salesOutAmountStrategy">
            <el-radio label="strict">严格校验</el-radio>
            <el-radio label="auto">自动重算</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="库存策略" v-if="activeType === 'sales_out'">
          <el-switch
            v-model="salesOutAllowNegativeStock"
            active-text="允许负库存"
            inactive-text="库存不足阻断"
          />
        </el-form-item>
        <el-form-item label="入库状态" v-if="activeType === 'purchase_in'">
          <el-tag type="info">按全局审核策略执行（免审直接入库，需审保持待审核）</el-tag>
        </el-form-item>
        <el-form-item label="Excel文件">
          <el-upload
            ref="upload"
            :limit="1"
            :auto-upload="false"
            :on-change="handleFileChange"
            :file-list="fileList"
            action="#"
            accept=".xlsx,.xls"
          >
            <el-button slot="trigger" size="small" type="primary">选取文件</el-button>
            <div slot="tip" class="el-upload__tip">仅支持 xlsx / xls</div>
          </el-upload>
        </el-form-item>
        <el-form-item label="字段映射">
          <el-alert
            title="支持自动匹配，规则支持直接映射/字典映射/固定值，必填字段需确认后才能导入。"
            type="info"
            :closable="false"
            show-icon
              style="margin-bottom: 10px"
          />

            <el-table
              v-if="fieldMappings.length > 0"
              :data="fieldMappings"
              border
              size="mini"
              max-height="280"
              v-loading="mappingLoading"
            >
              <el-table-column label="系统字段" min-width="140">
                <template slot-scope="scope">
                  <span>{{ scope.row.systemFieldName }}</span><span v-if="scope.row.required" class="required-star">*</span>
                </template>
              </el-table-column>
              <el-table-column label="规则" width="120">
                <template slot-scope="scope">
                  <el-select v-model="scope.row.ruleType" size="mini" @change="handleRuleTypeChange(scope.row)">
                    <el-option label="直接映射" value="direct" />
                    <el-option label="字典映射" value="dict" />
                    <el-option label="固定值" value="fixed" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="源字段" min-width="170">
                <template slot-scope="scope">
                  <el-select
                    v-if="scope.row.ruleType !== 'fixed'"
                    v-model="scope.row.sourceHeader"
                    size="mini"
                    filterable
                    clearable
                    placeholder="请选择源字段"
                  >
                    <el-option
                      v-for="header in sourceHeaders"
                      :key="scope.row.targetField + '_' + header"
                      :label="header"
                      :value="header"
                    />
                  </el-select>
                  <span v-else class="muted">固定值模式不需要源字段</span>
                </template>
              </el-table-column>
              <el-table-column label="固定值" min-width="160">
                <template slot-scope="scope">
                  <el-input
                    v-if="scope.row.ruleType === 'fixed'"
                    v-model="scope.row.fixedValue"
                    size="mini"
                    placeholder="请输入固定值"
                  />
                  <span v-else class="muted">-</span>
                </template>
              </el-table-column>
            </el-table>

            <el-empty v-else description="请先上传Excel以解析字段" :image-size="56" />
        </el-form-item>
      </el-form>

      <el-alert
        v-if="importResult"
        :title="resultMessage(importResult)"
        :type="(importResult.task && importResult.task.failRows > 0) ? 'warning' : 'success'"
        :closable="false"
        show-icon
        style="margin-bottom: 12px;"
      />

      <el-table
        v-if="importResult && importResult.errors && importResult.errors.length > 0"
        :data="importResult.errors"
        border
        height="260"
      >
        <el-table-column prop="rowNum" label="行号" width="80" />
        <el-table-column prop="errorMsg" label="错误信息" min-width="180" />
        <el-table-column prop="rawDataJson" label="原始数据" min-width="220" show-overflow-tooltip />
      </el-table>

      <el-alert
        v-if="importResult && importResult.suggestFix"
        :title="importResult.suggestFix"
        type="info"
        :closable="false"
        show-icon
        style="margin-top: 12px;"
      />

      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogOpen = false">取消</el-button>
        <el-button
          v-if="activeType === 'goods'"
          @click="handleDownloadTemplate(activeType)"
          v-hasPermi="['wms:goods:importTemplate']"
        >下载当前模板</el-button>
        <el-button type="primary" :loading="importLoading" @click="submitImport">
          {{ retryTaskId ? '开始重试' : '创建并导入' }}
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { downloadBasicTemplate, downloadBasicTemplatePackage } from '@/api/wms/basicImport'
import {
  createImportTask,
  pageImportTask,
  previewImportTask,
  executeImportTask,
  retryImportTask,
  getImportTaskReport,
  parseImportHeaders
} from '@/api/wms/importTask'

export default {
  name: 'WmsBasicImport',
  data() {
    return {
      typeOptions: [
        { type: 'unit', label: '单位导入', desc: '导入计量单位主数据。' },
        { type: 'category', label: '商品分类导入', desc: '支持父分类依赖校验。' },
        { type: 'goods', label: '商品+SKU导入', desc: '复用现有无规格/有规格模板。' },
        { type: 'supplier', label: '供应商导入', desc: '导入供应商档案信息。' },
        { type: 'customer', label: '客户导入', desc: '导入客户档案信息。' },
        { type: 'warehouse', label: '仓库导入', desc: '导入仓库及排序信息。' },
        { type: 'purchase_in', label: '采购入库导入', desc: '单Sheet导入，按单据号自动聚合。' },
        { type: 'sales_out', label: '销售出库导入', desc: '单Sheet导入，按单据号自动聚合。' }
      ],
      dialogOpen: false,
      dialogTitle: '基础数据导入',
      activeType: 'unit',
      retryTaskId: null,
      stepActive: 1,
      goodsTemplateType: 'simple',
      purchaseAmountStrategy: 'strict',
      salesOutAmountStrategy: 'strict',
      salesOutAllowNegativeStock: false,
      fileList: [],
      sourceHeaders: [],
      fieldMappings: [],
      mappingLoading: false,
      importLoading: false,
      importResult: null,
      taskLoading: false,
      taskList: [],
      taskTotal: 0,
      taskQuery: {
        pageNo: 1,
        pageSize: 10
      },
      mappingMetaByType: {
        unit: [
          { targetField: 'unitName', systemFieldName: '单位名称', required: true, aliases: ['单位名称', '单位', '名称'] },
          { targetField: 'status', systemFieldName: '状态', required: false, aliases: ['状态', '状态(正常/停用)'] },
          { targetField: 'remark', systemFieldName: '备注', required: false, aliases: ['备注'] }
        ],
        category: [
          { targetField: 'categoryName', systemFieldName: '分类名称', required: true, aliases: ['分类名称', '名称'] },
          { targetField: 'parentCategoryName', systemFieldName: '父分类名称', required: false, aliases: ['父分类名称', '父分类'] },
          { targetField: 'status', systemFieldName: '状态', required: false, aliases: ['状态', '状态(正常/停用)'] },
          { targetField: 'orderNum', systemFieldName: '排序', required: false, aliases: ['排序'] },
          { targetField: 'remark', systemFieldName: '备注', required: false, aliases: ['备注'] }
        ],
        supplier: [
          { targetField: 'supplierCode', systemFieldName: '供应商编码', required: false, aliases: ['供应商编码'] },
          { targetField: 'supplierName', systemFieldName: '供应商名称', required: true, aliases: ['供应商名称', '名称'] },
          { targetField: 'contact', systemFieldName: '联系人', required: false, aliases: ['联系人'] },
          { targetField: 'phone', systemFieldName: '联系电话', required: false, aliases: ['联系电话', '电话'] },
          { targetField: 'mobile', systemFieldName: '手机号', required: false, aliases: ['手机号', '手机'] },
          { targetField: 'address', systemFieldName: '地址', required: false, aliases: ['地址'] },
          { targetField: 'status', systemFieldName: '状态', required: false, aliases: ['状态', '状态(正常/停用)'] },
          { targetField: 'remark', systemFieldName: '备注', required: false, aliases: ['备注'] }
        ],
        customer: [
          { targetField: 'customerCode', systemFieldName: '客户编码', required: false, aliases: ['客户编码'] },
          { targetField: 'customerName', systemFieldName: '客户名称', required: true, aliases: ['客户名称', '名称'] },
          { targetField: 'customerType', systemFieldName: '客户类型', required: false, aliases: ['客户类型', '客户类型(批发/零售)'] },
          { targetField: 'contact', systemFieldName: '联系人', required: false, aliases: ['联系人'] },
          { targetField: 'phone', systemFieldName: '联系电话', required: false, aliases: ['联系电话', '电话'] },
          { targetField: 'mobile', systemFieldName: '手机号', required: false, aliases: ['手机号', '手机'] },
          { targetField: 'address', systemFieldName: '地址', required: false, aliases: ['地址'] },
          { targetField: 'status', systemFieldName: '状态', required: false, aliases: ['状态', '状态(正常/停用)'] },
          { targetField: 'remark', systemFieldName: '备注', required: false, aliases: ['备注'] }
        ],
        warehouse: [
          { targetField: 'warehouseCode', systemFieldName: '仓库编码', required: false, aliases: ['仓库编码'] },
          { targetField: 'warehouseName', systemFieldName: '仓库名称', required: true, aliases: ['仓库名称', '名称'] },
          { targetField: 'contact', systemFieldName: '联系人', required: false, aliases: ['联系人'] },
          { targetField: 'phone', systemFieldName: '联系电话', required: false, aliases: ['联系电话', '电话'] },
          { targetField: 'address', systemFieldName: '仓库地址', required: false, aliases: ['仓库地址', '地址'] },
          { targetField: 'status', systemFieldName: '状态', required: false, aliases: ['状态', '状态(正常/停用)'] },
          { targetField: 'sort', systemFieldName: '排序', required: false, aliases: ['排序'] },
          { targetField: 'remark', systemFieldName: '备注', required: false, aliases: ['备注'] }
        ],
        goodsSimple: [
          { targetField: 'goodsCode', systemFieldName: '商品编码', required: false, aliases: ['商品编码', '货号', '商品编号', '物料编码'] },
          { targetField: 'goodsName', systemFieldName: '商品名称', required: true, aliases: ['商品名称', '品名', '货品名称'] },
          { targetField: 'skuName', systemFieldName: 'SKU名称', required: false, aliases: ['SKU名称', 'SKU名称(选填)', 'SKU', '规格名称'] },
          { targetField: 'categoryName', systemFieldName: '分类名称', required: true, aliases: ['分类名称', '分类', '商品分类'] },
          { targetField: 'unitName', systemFieldName: '单位名称', required: true, aliases: ['单位名称', '单位', '计量单位'] },
          { targetField: 'brand', systemFieldName: '品牌', required: false, aliases: ['品牌'] },
          { targetField: 'place', systemFieldName: '产地', required: false, aliases: ['产地', '产地/厂家', '厂家'] },
          { targetField: 'status', systemFieldName: '状态', required: false, aliases: ['状态', '状态(正常/停用)', '启用状态'] },
          { targetField: 'costPrice', systemFieldName: '成本价', required: false, aliases: ['成本价', '进货价'] },
          { targetField: 'salePrice', systemFieldName: '销售价', required: false, aliases: ['销售价', '售价', '零售价'] },
          { targetField: 'minStock', systemFieldName: '最小库存', required: false, aliases: ['最小库存', '库存下限'] },
          { targetField: 'maxStock', systemFieldName: '最大库存', required: false, aliases: ['最大库存', '库存上限'] }
        ],
        goodsFull: [
          { targetField: 'goodsCode', systemFieldName: '商品编码', required: false, aliases: ['商品编码', '货号', '商品编号', '物料编码'] },
          { targetField: 'goodsName', systemFieldName: '商品名称', required: true, aliases: ['商品名称', '品名', '货品名称'] },
          { targetField: 'categoryName', systemFieldName: '分类名称', required: true, aliases: ['分类名称', '分类', '商品分类'] },
          { targetField: 'unitName', systemFieldName: '单位名称', required: true, aliases: ['单位名称', '单位', '计量单位'] },
          { targetField: 'brand', systemFieldName: '品牌', required: false, aliases: ['品牌'] },
          { targetField: 'place', systemFieldName: '产地', required: false, aliases: ['产地', '产地/厂家', '厂家'] },
          { targetField: 'status', systemFieldName: '状态', required: false, aliases: ['状态', '状态(正常/停用)', '启用状态'] },
          { targetField: 'remark', systemFieldName: '备注', required: false, aliases: ['备注'] },
          { targetField: 'skuCode', systemFieldName: 'SKU编码', required: false, aliases: ['SKU编码', 'sku编码', 'sku code'] },
          { targetField: 'skuName', systemFieldName: 'SKU名称', required: false, aliases: ['SKU名称', 'SKU', '规格名称'] },
          { targetField: 'specName1', systemFieldName: '规格名1', required: false, aliases: ['规格名1'] },
          { targetField: 'specValue1', systemFieldName: '规格值1', required: false, aliases: ['规格值1'] },
          { targetField: 'specName2', systemFieldName: '规格名2', required: false, aliases: ['规格名2'] },
          { targetField: 'specValue2', systemFieldName: '规格值2', required: false, aliases: ['规格值2'] },
          { targetField: 'specName3', systemFieldName: '规格名3', required: false, aliases: ['规格名3'] },
          { targetField: 'specValue3', systemFieldName: '规格值3', required: false, aliases: ['规格值3'] },
          { targetField: 'specName4', systemFieldName: '规格名4', required: false, aliases: ['规格名4'] },
          { targetField: 'specValue4', systemFieldName: '规格值4', required: false, aliases: ['规格值4'] },
          { targetField: 'specName5', systemFieldName: '规格名5', required: false, aliases: ['规格名5'] },
          { targetField: 'specValue5', systemFieldName: '规格值5', required: false, aliases: ['规格值5'] },
          { targetField: 'costPrice', systemFieldName: '成本价', required: false, aliases: ['成本价', '进货价'] },
          { targetField: 'salePrice', systemFieldName: '销售价', required: false, aliases: ['销售价', '售价', '零售价'] },
          { targetField: 'minStock', systemFieldName: '最小库存', required: false, aliases: ['最小库存', '库存下限'] },
          { targetField: 'maxStock', systemFieldName: '最大库存', required: false, aliases: ['最大库存', '库存上限'] }
        ],
        purchase_in: [
          { targetField: 'billNo', systemFieldName: '单据号', required: true, aliases: ['单据号', '单据号*'] },
          { targetField: 'inDate', systemFieldName: '单据日期', required: true, aliases: ['单据日期', '单据日期*', '入库日期'] },
          { targetField: 'supplierCode', systemFieldName: '供应商编码', required: false, aliases: ['供应商编码', '供应商编码*'] },
          { targetField: 'supplierName', systemFieldName: '供应商名称', required: false, aliases: ['供应商名称', '供应商', '名称', '供应商名称*'] },
          { targetField: 'warehouseCode', systemFieldName: '仓库编码', required: false, aliases: ['仓库编码', '仓库编码*'] },
          { targetField: 'warehouseName', systemFieldName: '仓库名称', required: false, aliases: ['仓库名称', '仓库', '名称', '仓库名称*'] },
          { targetField: 'skuCode', systemFieldName: 'SKU编码', required: false, aliases: ['SKU编码', 'sku编码', 'SKU编码*'] },
          { targetField: 'skuName', systemFieldName: 'SKU名称', required: false, aliases: ['SKU名称', '商品名称', '品名', 'SKU名称*'] },
          { targetField: 'goodsName', systemFieldName: '商品名称', required: false, aliases: ['商品名称', '商品', '货品名称'] },
          { targetField: 'specInfo', systemFieldName: '规格名称', required: false, aliases: ['规格名称', '规格', '规格信息', 'spec'] },
          { targetField: 'quantity', systemFieldName: '数量', required: true, aliases: ['数量', '数量*'] },
          { targetField: 'price', systemFieldName: '单价', required: true, aliases: ['单价', '单价*'] },
          { targetField: 'amount', systemFieldName: '金额', required: false, aliases: ['金额'] },
          { targetField: 'remark', systemFieldName: '备注', required: false, aliases: ['备注'] }
        ],
        sales_out: [
          { targetField: 'billNo', systemFieldName: '单据号', required: true, aliases: ['单据号', '单据号*'] },
          { targetField: 'outDate', systemFieldName: '单据日期', required: true, aliases: ['单据日期', '单据日期*', '出库日期'] },
          { targetField: 'customerCode', systemFieldName: '客户编码', required: false, aliases: ['客户编码', '客户编码*'] },
          { targetField: 'customerName', systemFieldName: '客户名称', required: false, aliases: ['客户名称', '客户', '名称', '客户名称*'] },
          { targetField: 'warehouseCode', systemFieldName: '仓库编码', required: false, aliases: ['仓库编码', '仓库编码*'] },
          { targetField: 'warehouseName', systemFieldName: '仓库名称', required: false, aliases: ['仓库名称', '仓库', '名称', '仓库名称*'] },
          { targetField: 'skuCode', systemFieldName: 'SKU编码', required: false, aliases: ['SKU编码', 'sku编码', 'SKU编码*'] },
          { targetField: 'skuName', systemFieldName: 'SKU名称', required: false, aliases: ['SKU名称', '商品名称', '品名', 'SKU名称*'] },
          { targetField: 'goodsName', systemFieldName: '商品名称', required: false, aliases: ['商品名称', '商品', '货品名称'] },
          { targetField: 'specInfo', systemFieldName: '规格名称', required: false, aliases: ['规格名称', '规格', '规格信息', 'spec'] },
          { targetField: 'quantity', systemFieldName: '数量', required: true, aliases: ['数量', '数量*'] },
          { targetField: 'price', systemFieldName: '单价', required: true, aliases: ['单价', '单价*'] },
          { targetField: 'amount', systemFieldName: '金额', required: false, aliases: ['金额'] },
          { targetField: 'remark', systemFieldName: '备注', required: false, aliases: ['备注'] }
        ]
      }
    }
  },
  created() {
    this.getTaskList()
  },
  watch: {
    goodsTemplateType() {
      if (this.activeType === 'goods') {
        this.fieldMappings = this.buildDefaultMappings(this.activeType, this.sourceHeaders)
      }
    }
  },
  methods: {
    getMappingMeta(type) {
      if (type === 'goods') {
        return this.goodsTemplateType === 'simple'
          ? (this.mappingMetaByType.goodsSimple || [])
          : (this.mappingMetaByType.goodsFull || [])
      }
      return this.mappingMetaByType[type] || []
    },
    getTaskList() {
      this.taskLoading = true
      pageImportTask(this.taskQuery).then(res => {
        this.taskList = res.rows || []
        this.taskTotal = res.total || 0
      }).finally(() => {
        this.taskLoading = false
      })
    },
    resultMessage(report) {
      if (!report || !report.task) {
        return ''
      }
      const success = report.task.successRows || 0
      const fail = report.task.failRows || 0
      return '导入完成：成功 ' + success + ' 条，失败 ' + fail + ' 条'
    },
    getTypeLabel(type) {
      const found = this.typeOptions.find(item => item.type === type)
      return found ? found.label : type
    },
    statusTagType(status) {
      if (status === 2) {
        return 'success'
      }
      if (status === 3) {
        return 'warning'
      }
      if (status === 4) {
        return 'danger'
      }
      return 'info'
    },
    openImportDialog(type, taskId) {
      this.activeType = type
      this.retryTaskId = taskId || null
      this.dialogTitle = '基础数据导入 - ' + this.getTypeLabel(type)
      this.fileList = []
      this.sourceHeaders = []
      this.fieldMappings = []
      this.purchaseAmountStrategy = 'strict'
      this.salesOutAmountStrategy = 'strict'
      this.salesOutAllowNegativeStock = false
      this.importResult = null
      this.stepActive = 1
      this.dialogOpen = true
      this.$nextTick(() => {
        this.$refs.upload && this.$refs.upload.clearFiles()
      })
    },
    async handleFileChange(file) {
      this.fileList = file ? [file] : []
      if (this.fileList.length === 0) {
        this.sourceHeaders = []
        this.fieldMappings = []
        this.stepActive = 1
        return
      }

      this.stepActive = 2
      this.mappingLoading = true
      try {
        const res = await parseImportHeaders(this.fileList[0].raw)
        this.sourceHeaders = (res.data || []).filter(Boolean)
        this.fieldMappings = this.buildDefaultMappings(this.activeType, this.sourceHeaders)
        this.stepActive = 3
      } catch (e) {
        this.sourceHeaders = []
        this.fieldMappings = this.buildDefaultMappings(this.activeType, [])
        this.$modal.msgError('解析表头失败，请检查Excel格式')
      } finally {
        this.mappingLoading = false
      }
    },
    buildDefaultMappings(type, headers) {
      const defs = this.getMappingMeta(type)
      const headerMap = {}
      headers.forEach(h => {
        const key = this.normalizeHeader(h)
        if (key && !headerMap[key]) {
          headerMap[key] = h
        }
      })

      return defs.map(def => {
        let sourceHeader = ''
        for (let i = 0; i < def.aliases.length; i++) {
          const alias = def.aliases[i]
          const hit = headerMap[this.normalizeHeader(alias)]
          if (hit) {
            sourceHeader = hit
            break
          }
        }
        return {
          targetField: def.targetField,
          systemFieldName: def.systemFieldName,
          required: !!def.required,
          ruleType: 'direct',
          sourceHeader,
          fixedValue: ''
        }
      })
    },
    normalizeHeader(value) {
      if (!value) {
        return ''
      }
      return String(value)
        .replace(/\*/g, '')
        .replace(/\s+/g, '')
        .replace(/（/g, '(')
        .replace(/）/g, ')')
        .toLowerCase()
    },
    handleRuleTypeChange(row) {
      if (row.ruleType === 'fixed') {
        row.sourceHeader = ''
      } else {
        row.fixedValue = ''
      }
    },
    validateMappings() {
      const requiredMissing = this.fieldMappings.find(item => {
        if (!item.required) {
          return false
        }
        if (item.ruleType === 'fixed') {
          return !item.fixedValue
        }
        return !item.sourceHeader
      })
      if (requiredMissing) {
        this.$modal.msgWarning('必填字段未映射：' + requiredMissing.systemFieldName)
        return false
      }

      if (this.activeType === 'purchase_in' || this.activeType === 'sales_out') {
        const hasMapped = targetField => {
          const item = this.fieldMappings.find(it => it.targetField === targetField)
          if (!item) {
            return false
          }
          if (item.ruleType === 'fixed') {
            return !!item.fixedValue
          }
          return !!item.sourceHeader
        }

        const supplierMatched = hasMapped('supplierCode') || hasMapped('supplierName')
        const customerMatched = hasMapped('customerCode') || hasMapped('customerName')
        const warehouseMatched = hasMapped('warehouseCode') || hasMapped('warehouseName')
        const skuMatched = hasMapped('skuCode') || hasMapped('skuName') || hasMapped('goodsName')

        if (this.activeType === 'purchase_in' && !supplierMatched) {
          this.$modal.msgWarning('请至少映射一个字段：供应商编码 或 供应商名称')
          return false
        }
        if (this.activeType === 'sales_out' && !customerMatched) {
          this.$modal.msgWarning('请至少映射一个字段：客户编码 或 客户名称')
          return false
        }
        if (!warehouseMatched) {
          this.$modal.msgWarning('请至少映射一个字段：仓库编码 或 仓库名称')
          return false
        }
        if (!skuMatched) {
          this.$modal.msgWarning('请至少映射一个字段：SKU编码 或 SKU名称 或 商品名称')
          return false
        }
      }

      return true
    },
    buildMappingPayload() {
      const mappings = (this.fieldMappings || []).map(item => ({
        targetField: item.targetField,
        sourceHeader: item.ruleType === 'fixed' ? '' : (item.sourceHeader || ''),
        ruleType: item.ruleType || 'direct',
        fixedValue: item.ruleType === 'fixed' ? (item.fixedValue || '') : ''
      }))

      if (this.activeType === 'purchase_in') {
        return {
          mappings,
          strategies: {
            amountStrategy: this.purchaseAmountStrategy
          }
        }
      }

      if (this.activeType === 'sales_out') {
        return {
          mappings,
          strategies: {
            amountStrategy: this.salesOutAmountStrategy,
            allowNegativeStock: this.salesOutAllowNegativeStock
          }
        }
      }

      return mappings
    },
    submitImport() {
      if (this.fileList.length === 0) {
        this.$modal.msgWarning('请先选择文件')
        return
      }

      if (!this.validateMappings()) {
        return
      }

      const file = this.fileList[0].raw
      const mappingJson = JSON.stringify(this.buildMappingPayload())
      this.importLoading = true

      const request = this.retryTaskId
        ? retryImportTask(this.retryTaskId, file, this.goodsTemplateType, mappingJson)
        : createImportTask({ taskType: this.activeType }).then(createRes => {
          const taskId = createRes.data.id
          return previewImportTask(taskId, file, this.goodsTemplateType, mappingJson)
        })

      request.then(res => {
        this.importResult = res.data
        this.stepActive = 4
        const failRows = this.importResult && this.importResult.task ? this.importResult.task.failRows : 0
        if (failRows > 0) {
          this.$modal.msgWarning('导入完成，存在失败行，可在任务列表进行失败重试')
        } else {
          this.$modal.msgSuccess('导入成功')
        }
        this.getTaskList()
      }).finally(() => {
        this.importLoading = false
      })
    },
    handleExecute(row) {
      executeImportTask(row.id).then(res => {
        this.importResult = res.data
        this.dialogTitle = '导入报告 - ' + row.taskNo
        this.dialogOpen = true
        this.$modal.msgSuccess('执行完成')
        this.getTaskList()
      })
    },
    handleViewReport(row) {
      getImportTaskReport(row.id).then(res => {
        this.importResult = res.data
        this.dialogTitle = '导入报告 - ' + row.taskNo
        this.activeType = row.taskType
        this.retryTaskId = row.id
        this.dialogOpen = true
      })
    },
    handleDownloadTemplate(type) {
      const goodsTemplateType = type === 'goods' ? this.goodsTemplateType : undefined
      downloadBasicTemplate(type, goodsTemplateType).then(data => {
        const blob = new Blob([data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        const suffix = type === 'goods' ? '_' + goodsTemplateType : ''
        link.download = 'basic_import_' + type + suffix + '.xlsx'
        link.click()
        URL.revokeObjectURL(link.href)
      }).catch(() => {
        this.$modal.msgError('模板下载失败')
      })
    },
    handleDownloadPackage() {
      downloadBasicTemplatePackage().then(data => {
        const blob = new Blob([data], { type: 'application/zip' })
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = 'basic_import_template_package.zip'
        link.click()
        URL.revokeObjectURL(link.href)
      }).catch(() => {
        this.$modal.msgError('模板包下载失败')
      })
    }
  }
}
</script>

<style scoped>
.top-card {
  margin-bottom: 16px;
}

.top-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.top-row h3 {
  margin: 0;
  font-size: 18px;
}

.top-row p {
  margin: 8px 0 0;
  color: #6b7280;
}

.top-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  justify-content: flex-end;
}

.card-row {
  margin-bottom: 8px;
}

.task-card {
  margin-top: 8px;
}

.type-card {
  margin-bottom: 16px;
  min-height: 160px;
}

.step-bar {
  margin-bottom: 16px;
}

.type-title {
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 8px;
}

.type-desc {
  color: #6b7280;
  min-height: 40px;
}

.type-actions {
  margin-top: 16px;
  display: flex;
  gap: 8px;
}

.required-star {
  color: #f56c6c;
  margin-left: 4px;
}

.muted {
  color: #909399;
}
</style>
