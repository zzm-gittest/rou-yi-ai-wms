<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="凭证号" prop="voucherNo">
        <el-input
          v-model="queryParams.voucherNo"
          placeholder="请输入凭证号"
          clearable
          @keyup.enter.native="handleQuery"
        />
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
          <el-option label="收款" value="1" />
          <el-option label="付款" value="2" />
          <el-option label="费用" value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="方向" prop="direction">
        <el-select v-model="queryParams.direction" placeholder="方向" clearable>
          <el-option label="收入" value="1" />
          <el-option label="支出" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="账户" prop="accountId">
        <el-select v-model="queryParams.accountId" placeholder="请选择账户" clearable>
          <el-option
            v-for="item in accountOptions"
            :key="item.id"
            :label="item.accountName"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="单据日期">
        <el-date-picker
          v-model="dateRange"
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
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['wms:financeLog:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="logList" show-summary :summary-method="getSummaries">
      <el-table-column label="凭证号" prop="voucherNo" min-width="130" />
      <el-table-column label="日期" prop="billDate" min-width="110">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.billDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="摘要" prop="summary" min-width="220" show-overflow-tooltip />
      <el-table-column label="单据编号" prop="billNo" min-width="130" />
      <el-table-column label="单据类型" prop="billTypeName" min-width="80" align="center" />
      <el-table-column label="原始单据" prop="originalBillTypeName" min-width="100" align="center" />
      <el-table-column label="往来单位" prop="partnerName" min-width="120" show-overflow-tooltip />
      <el-table-column label="账户" prop="accountName" min-width="120" show-overflow-tooltip />
      <el-table-column label="收入金额" prop="amount" min-width="110" align="right">
        <template slot-scope="scope">
          <span v-if="scope.row.direction === '1'" style="color: #67C23A; font-weight: bold">
            {{ scope.row.amount ? scope.row.amount.toFixed(2) : '0.00' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="支出金额" prop="amount" min-width="110" align="right">
        <template slot-scope="scope">
          <span v-if="scope.row.direction === '2'" style="color: #F56C6C; font-weight: bold">
            {{ scope.row.amount ? scope.row.amount.toFixed(2) : '0.00' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="余额" min-width="120" align="right">
        <template slot-scope="scope">
          <span style="color: #409EFF">{{ scope.row.afterBalance ? scope.row.afterBalance.toFixed(2) : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="支付方式" prop="paymentMethodName" min-width="90" align="center" />
      <el-table-column label="备注" prop="remark" min-width="150" show-overflow-tooltip />
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import { pageFinanceLog } from "@/api/wms/financeLog"
import { listAccount } from "@/api/wms/account"

export default {
  name: "FinanceLog",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 财务日志表格数据
      logList: [],
      // 弹出层标题
      title: "",
      // 日期范围
      dateRange: [],
      // 账户选项
      accountOptions: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        voucherNo: null,
        billNo: null,
        billType: null,
        direction: null,
        accountId: null,
        startDate: null,
        endDate: null
      }
    }
  },
  created() {
    this.getList()
    this.getAccountList()
  },
  methods: {
    /** 查询财务日志列表 */
    getList() {
      this.loading = true
      if (this.dateRange && this.dateRange.length === 2) {
        this.queryParams.startDate = this.dateRange[0]
        this.queryParams.endDate = this.dateRange[1]
      }
      pageFinanceLog(this.queryParams).then(response => {
        this.logList = response.rows
        this.total = response.total
        this.loading = false
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
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = []
      this.resetForm("queryForm")
      this.handleQuery()
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('wms/financeLog/export', {
        ...this.queryParams
      }, `财务日志_${new Date().getTime()}.xlsx`)
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
        if (column.label === '收入金额') {
          const incomeTotal = data.reduce((sum, row) => row.direction === '1' ? sum + (row.amount || 0) : sum, 0)
          sums[index] = incomeTotal.toFixed(2)
        } else if (column.label === '支出金额') {
          const expenseTotal = data.reduce((sum, row) => row.direction === '2' ? sum + (row.amount || 0) : sum, 0)
          sums[index] = expenseTotal.toFixed(2)
        } else if (column.label === '余额') {
          // 取最后一行的余额
          if (data.length > 0) {
            const lastBalance = data[data.length - 1].afterBalance || 0
            sums[index] = lastBalance.toFixed(2)
          } else {
            sums[index] = '0.00'
          }
        } else {
          sums[index] = ''
        }
      })
      return sums
    }
  }
}
</script>
