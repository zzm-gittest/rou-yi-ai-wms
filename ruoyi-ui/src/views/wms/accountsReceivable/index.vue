<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="客户" prop="customerId">
        <el-select v-model="queryParams.customerId" placeholder="请选择客户" clearable filterable>
          <el-option
            v-for="item in customerOptions"
            :key="item.id"
            :label="item.customerName"
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
          <el-option label="销售订单" value="1" />
          <el-option label="销售出库" value="2" />
          <el-option label="采购退货" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="arStatus">
        <el-select v-model="queryParams.arStatus" placeholder="状态" clearable>
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

    <el-table v-loading="loading" :data="arList" show-summary :summary-method="getSummaries" fit>
      <el-table-column label="单据日期" prop="billDate" min-width="100">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.billDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="单据编号" prop="billNo" min-width="140" />
      <el-table-column label="单据类型" prop="billTypeName" min-width="90" />
      <el-table-column label="往来单位" prop="partnerName" min-width="150" show-overflow-tooltip>
        <template slot-scope="scope">
          {{ scope.row.billType === '3' ? (scope.row.supplierName || '-') : (scope.row.customerName || '-') }}
        </template>
      </el-table-column>
      <el-table-column label="应收金额" prop="arAmount" min-width="110" align="right">
        <template slot-scope="scope">
          {{ scope.row.arAmount ? scope.row.arAmount.toFixed(2) : '0.00' }}
        </template>
      </el-table-column>
      <el-table-column label="已收金额" prop="receivedAmount" min-width="110" align="right">
        <template slot-scope="scope">
          <span>{{ scope.row.receivedAmount ? scope.row.receivedAmount.toFixed(2) : '0.00' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="未收金额" min-width="110" align="right">
        <template slot-scope="scope">
          {{ getUnreceivedAmount(scope.row) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="arStatusName" min-width="80" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.arStatus === '0'" type="warning">未结清</el-tag>
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
            @click="handleCollect(scope.row)"
            v-hasPermi="['wms:accountsReceivable:collect']"
            v-if="scope.row.arStatus === '0'"
          >收款</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-printer"
            @click="handlePrint(scope.row)"
            v-hasPermi="['wms:accountsReceivable:query']"
            v-if="scope.row.arStatus === '1'"
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

    <!-- 收款对话框 -->
    <el-dialog title="收款" :visible.sync="collectOpen" width="500px" append-to-body>
      <el-form ref="collectForm" :model="collectForm" :rules="collectRules" label-width="100px">
        <el-form-item label="单据编号">
          <el-input v-model="collectForm.billNo" disabled />
        </el-form-item>
        <el-form-item label="应收金额">
          <el-input v-model="collectForm.arAmount" disabled>
            <template slot="append">元</template>
          </el-input>
        </el-form-item>
        <el-form-item label="已收金额">
          <el-input v-model="collectForm.receivedAmount" disabled>
            <template slot="append">元</template>
          </el-input>
        </el-form-item>
        <el-form-item label="未收金额">
          <el-input v-model="collectForm.unreceivedAmount" disabled>
            <template slot="append">元</template>
          </el-input>
        </el-form-item>
        <el-form-item label="收款金额" prop="collectAmount">
          <el-input-number v-model="collectForm.collectAmount" :min="0.01" :max="parseFloat(collectForm.unreceivedAmount)" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结算账户" prop="accountId">
          <el-select v-model="collectForm.accountId" placeholder="请选择结算账户" style="width: 100%">
            <el-option
              v-for="item in accountOptions"
              :key="item.id"
              :label="item.accountName + ' (余额:' + (item.balance || 0) + ')'"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="收款凭证">
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
          <el-input v-model="collectForm.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitCollect">确 定</el-button>
        <el-button @click="cancelCollect">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { pageAr, collectAr } from "@/api/wms/accountsReceivable"
import { listCustomer } from "@/api/wms/customer"
import { listAccount } from "@/api/wms/account"
import { getToken } from "@/utils/auth"

export default {
  name: "AccountsReceivable",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 应收账款表格数据
      arList: [],
      // 客户选项
      customerOptions: [],
      // 账户选项
      accountOptions: [],
      // 日期范围
      billDateRange: [],
      // 收款对话框
      collectOpen: false,
      // 上传地址
      uploadUrl: process.env.VUE_APP_BASE_API + "/common/upload",
      // 上传请求头
      uploadHeaders: { Authorization: "Bearer " + getToken() },
      // 文件列表
      fileList: [],
      // 收款表单
      collectForm: {
        id: null,
        billNo: null,
        arAmount: 0,
        receivedAmount: 0,
        unreceivedAmount: 0,
        collectAmount: null,
        accountId: null,
        collectionProof: null,
        remark: null
      },
      // 收款表单校验
      collectRules: {
        collectAmount: [
          { required: true, message: "收款金额不能为空", trigger: "blur" }
        ],
        accountId: [
          { required: true, message: "请选择结算账户", trigger: "change" }
        ]
      },
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        customerId: null,
        billNo: null,
        billType: null,
        arStatus: null,
        billDateStart: null,
        billDateEnd: null
      }
    }
  },
  created() {
    this.getList()
    this.getCustomerList()
    this.getAccountList()
  },
  methods: {
    /** 查询应收账款列表 */
    getList() {
      this.loading = true
      if (this.billDateRange && this.billDateRange.length === 2) {
        this.queryParams.billDateStart = this.billDateRange[0]
        this.queryParams.billDateEnd = this.billDateRange[1]
      } else {
        this.queryParams.billDateStart = null
        this.queryParams.billDateEnd = null
      }
      pageAr(this.queryParams).then(response => {
        this.arList = response.rows
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
    /** 计算未收金额 */
    getUnreceivedAmount(row) {
      const received = row.receivedAmount || 0
      const unreceived = (row.arAmount || 0) - received
      return unreceived.toFixed(2)
    },
    /** 收款按钮操作 */
    handleCollect(row) {
      this.collectForm.id = row.id
      this.collectForm.billNo = row.billNo
      this.collectForm.arAmount = row.arAmount
      this.collectForm.receivedAmount = row.receivedAmount || 0
      this.collectForm.unreceivedAmount = this.getUnreceivedAmount(row)
      this.collectForm.collectAmount = parseFloat(this.collectForm.unreceivedAmount)
      this.collectForm.accountId = null
      this.collectForm.collectionProof = null
      this.collectForm.remark = null
      this.fileList = []
      this.collectOpen = true
    },
    /** 上传成功回调 */
    handleUploadSuccess(response, file, fileList) {
      if (response.code === 200) {
        this.collectForm.collectionProof = response.url
        this.fileList = [{
          name: file.name,
          url: response.url
        }]
      }
    },
    /** 删除文件回调 */
    handleRemove() {
      this.collectForm.collectionProof = null
      this.fileList = []
    },
    /** 提交收款 */
    submitCollect() {
      this.$refs.collectForm.validate(valid => {
        if (valid) {
          collectAr(this.collectForm).then(() => {
            this.$modal.msgSuccess("收款成功")
            this.collectOpen = false
            this.getList()
            this.getAccountList()
          })
        }
      })
    },
    /** 取消收款 */
    cancelCollect() {
      this.collectOpen = false
      this.fileList = []
      this.resetForm("collectForm")
    },
    /** 打印（仅已结清） */
    handlePrint(row) {
      if (row.arStatus !== '1') {
        this.$modal.msgWarning('仅已结清单据支持打印')
        return
      }
      const route = this.$router.resolve({
        path: '/wms/accountsReceivable/print/index',
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
        if (column.property === 'arAmount') {
          const total = data.reduce((sum, row) => sum + (row.arAmount || 0), 0)
          sums[index] = total.toFixed(2)
        } else if (column.label === '已收金额') {
          const total = data.reduce((sum, row) => sum + (row.receivedAmount || 0), 0)
          sums[index] = total.toFixed(2)
        } else if (column.label === '未收金额') {
          const total = data.reduce((sum, row) => {
            const received = row.receivedAmount || 0
            return sum + ((row.arAmount || 0) - received)
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
