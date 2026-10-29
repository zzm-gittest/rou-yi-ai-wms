<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="供应商" prop="supplierId">
        <el-select v-model="queryParams.supplierId" placeholder="请选择供应商" clearable filterable>
          <el-option
            v-for="item in supplierOptions"
            :key="item.id"
            :label="item.supplierName"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="单据编号" prop="billNo">
        <el-input
          v-model="queryParams.billNo"
          placeholder="请输入单据编号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="单据类型" prop="billType">
        <el-select v-model="queryParams.billType" placeholder="单据类型" clearable>
          <el-option label="采购订单" value="1" />
          <el-option label="采购入库" value="2" />
          <el-option label="销售退货" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="apStatus">
        <el-select v-model="queryParams.apStatus" placeholder="状态" clearable>
          <el-option label="未结清" value="0" />
          <el-option label="已结清" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="单据日期">
        <el-date-picker
          v-model="billDateRange"
          style="display: flex"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="apList" show-summary :summary-method="getSummaries" fit>
      <el-table-column label="单据日期" prop="billDate" min-width="100">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.billDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="单据编号" prop="billNo" min-width="140" />
      <el-table-column label="单据类型" prop="billTypeName" min-width="90" />
      <el-table-column label="往来单位" prop="partnerName" min-width="150" show-overflow-tooltip>
        <template slot-scope="scope">
          {{ scope.row.billType === '3' ? (scope.row.customerName || '-') : (scope.row.supplierName || '-') }}
        </template>
      </el-table-column>
      <el-table-column label="应付金额" prop="apAmount" min-width="110" align="right">
        <template slot-scope="scope">
          {{ scope.row.apAmount ? scope.row.apAmount.toFixed(2) : '0.00' }}
        </template>
      </el-table-column>
      <el-table-column label="已付金额" prop="paidAmount" min-width="110" align="right">
        <template slot-scope="scope">
          <span>{{ scope.row.paidAmount ? scope.row.paidAmount.toFixed(2) : '0.00' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="未付金额" min-width="110" align="right">
        <template slot-scope="scope">
          {{ getUnpaidAmount(scope.row) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="apStatusName" min-width="80" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.apStatus === '0'" type="warning">未结清</el-tag>
          <el-tag v-else type="success">已结清</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="到期日期" prop="dueDate" min-width="100">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.dueDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-money"
            @click="handlePay(scope.row)"
            v-hasPermi="['wms:accountsPayable:pay']"
            v-if="scope.row.apStatus === '0'"
          >付款</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-printer"
            @click="handlePrint(scope.row)"
            v-hasPermi="['wms:accountsPayable:query']"
            v-if="scope.row.apStatus === '1'"
          >打印</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNo"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 付款对话框 -->
    <el-dialog title="付款" :visible.sync="payOpen" width="500px" append-to-body>
      <el-form ref="payForm" :model="payForm" :rules="payRules" label-width="100px">
        <el-form-item label="单据编号">
          <el-input v-model="payForm.billNo" disabled />
        </el-form-item>
        <el-form-item label="应付金额">
          <el-input v-model="payForm.apAmount" disabled>
            <template slot="append">元</template>
          </el-input>
        </el-form-item>
        <el-form-item label="已付金额">
          <el-input v-model="payForm.paidAmount" disabled>
            <template slot="append">元</template>
          </el-input>
        </el-form-item>
        <el-form-item label="未付金额">
          <el-input v-model="payForm.unpaidAmount" disabled>
            <template slot="append">元</template>
          </el-input>
        </el-form-item>
        <el-form-item label="付款金额" prop="payAmount">
          <el-input-number v-model="payForm.payAmount" :min="0.01" :max="parseFloat(payForm.unpaidAmount)" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结算账户" prop="accountId">
          <el-select v-model="payForm.accountId" placeholder="请选择结算账户" style="width: 100%">
            <el-option
              v-for="item in accountOptions"
              :key="item.id"
              :label="item.accountName + ' (余额:' + (item.balance || 0) + ')'"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="付款凭证">
          <el-upload
            class="upload-demo"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-remove="handleRemove"
            :file-list="fileList"
            list-type="picture-card"
            :limit="1"
          >
            <i class="el-icon-plus"></i>
          </el-upload>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="payForm.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitPay">确 定</el-button>
        <el-button @click="cancelPay">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { pageAp, payAp } from "@/api/wms/accountsPayable"
import { listSupplier } from "@/api/wms/supplier"
import { listAccount } from "@/api/wms/account"
import { getToken } from "@/utils/auth"

export default {
  name: "AccountsPayable",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 应付账款表格数据
      apList: [],
      // 供应商选项
      supplierOptions: [],
      // 账户选项
      accountOptions: [],
      // 日期范围
      billDateRange: [],
      // 付款对话框
      payOpen: false,
      // 上传地址
      uploadUrl: process.env.VUE_APP_BASE_API + "/common/upload",
      // 上传请求头
      uploadHeaders: { Authorization: "Bearer " + getToken() },
      // 文件列表
      fileList: [],
      // 付款表单
      payForm: {
        id: null,
        billNo: null,
        apAmount: 0,
        paidAmount: 0,
        unpaidAmount: 0,
        payAmount: null,
        accountId: null,
        paymentProof: null,
        remark: null
      },
      // 付款表单校验
      payRules: {
        payAmount: [
          { required: true, message: "付款金额不能为空", trigger: "blur" }
        ],
        accountId: [
          { required: true, message: "请选择结算账户", trigger: "change" }
        ]
      },
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        supplierId: null,
        billNo: null,
        billType: null,
        apStatus: null,
        billDateStart: null,
        billDateEnd: null
      }
    }
  },
  created() {
    this.getList()
    this.getSupplierList()
    this.getAccountList()
  },
  methods: {
    /** 查询应付账款列表 */
    getList() {
      this.loading = true
      if (this.billDateRange && this.billDateRange.length === 2) {
        this.queryParams.billDateStart = this.billDateRange[0]
        this.queryParams.billDateEnd = this.billDateRange[1]
      } else {
        this.queryParams.billDateStart = null
        this.queryParams.billDateEnd = null
      }
      pageAp(this.queryParams).then(response => {
        this.apList = response.rows
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
    /** 查询账户列表 */
    getAccountList() {
      listAccount().then(response => {
        this.accountOptions = response.data
      })
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNo = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.billDateRange = []
      this.resetForm("queryForm")
      this.handleQuery()
    },
    /** 计算未付金额 */
    getUnpaidAmount(row) {
      const paid = row.paidAmount || 0
      const unpaid = (row.apAmount || 0) - paid
      return unpaid.toFixed(2)
    },
    /** 付款按钮操作 */
    handlePay(row) {
      this.payForm.id = row.id
      this.payForm.billNo = row.billNo
      this.payForm.apAmount = row.apAmount
      this.payForm.paidAmount = row.paidAmount || 0
      this.payForm.unpaidAmount = this.getUnpaidAmount(row)
      this.payForm.payAmount = parseFloat(this.payForm.unpaidAmount)
      this.payForm.accountId = null
      this.payForm.paymentProof = null
      this.payForm.remark = null
      this.fileList = []
      this.payOpen = true
    },
    /** 上传成功回调 */
    handleUploadSuccess(response, file, fileList) {
      if (response.code === 200) {
        this.payForm.paymentProof = response.url
        this.fileList = [{
          name: file.name,
          url: response.url
        }]
      }
    },
    /** 删除文件回调 */
    handleRemove() {
      this.payForm.paymentProof = null
      this.fileList = []
    },
    /** 提交付款 */
    submitPay() {
      this.$refs.payForm.validate(valid => {
        if (valid) {
          payAp(this.payForm).then(() => {
            this.$modal.msgSuccess("付款成功")
            this.payOpen = false
            this.getList()
            this.getAccountList()
          })
        }
      })
    },
    /** 取消付款 */
    cancelPay() {
      this.payOpen = false
      this.fileList = []
      this.resetForm("payForm")
    },
    /** 打印（仅已结清） */
    handlePrint(row) {
      if (row.apStatus !== '1') {
        this.$modal.msgWarning('仅已结清单据支持打印')
        return
      }
      const route = this.$router.resolve({
        path: '/wms/accountsPayable/print/index',
        query: {
          id: row.id,
          from: this.$route.fullPath
        }
      })
      window.open(route.href, '_blank')
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
        if (column.property === 'apAmount') {
          const total = data.reduce((sum, row) => sum + (row.apAmount || 0), 0)
          sums[index] = total.toFixed(2)
        } else if (column.label === '已付金额') {
          const total = data.reduce((sum, row) => sum + (row.paidAmount || 0), 0)
          sums[index] = total.toFixed(2)
        } else if (column.label === '未付金额') {
          const total = data.reduce((sum, row) => {
            const paid = row.paidAmount || 0
            return sum + ((row.apAmount || 0) - paid)
          }, 0)
          sums[index] = total.toFixed(2)
        } else {
          sums[index] = ''
        }
      })
      return sums
    }
  }
}
</script>
