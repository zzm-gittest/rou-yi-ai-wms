<template>
  <div class="app-container">
    <!-- 查询条件 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" class="query-form">
      <el-form-item label="日期范围">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
          style="width: 240px"
        />
      </el-form-item>
      <el-form-item label="客户">
        <el-select v-model="queryParams.customerIds" multiple placeholder="请选择客户" clearable style="width: 200px">
          <el-option
            v-for="item in customerList"
            :key="item.id"
            :label="item.customerName"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
        <el-button
          type="success"
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['wms:report:export']"
        >导出</el-button>
      </el-form-item>
    </el-form>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #ecf5ff;">
              <i class="el-icon-user" style="color: #409eff;"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ reportData.length }}</div>
              <div class="stat-label">客户数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f0f9ff;">
              <i class="el-icon-document" style="color: #67c23a;"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summary.orderCount }}</div>
              <div class="stat-label">总订单数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fef0f0;">
              <i class="el-icon-coin" style="color: #f56c6c;"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value" :title="'¥' + formatMoney(summary.netSales)">¥{{ formatMoneyShort(summary.netSales) }}</div>
              <div class="stat-label">总净销售额</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fdf6ec;">
              <i class="el-icon-wallet" style="color: #e6a23c;"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summary.avgReceiptRate }}%</div>
              <div class="stat-label">平均回款率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="reportData" border stripe style="margin-top: 16px">
      <el-table-column prop="customerCode" label="客户编码" width="120" />
      <el-table-column prop="customerName" label="客户名称" width="200" show-overflow-tooltip />
      <el-table-column prop="contact" label="联系人" width="100" />
      <el-table-column prop="phone" label="联系电话" width="120" />
      <el-table-column prop="orderCount" label="订单数" width="100" align="right" />
      <el-table-column prop="orderAmount" label="订单金额" width="120" align="right">
        <template slot-scope="scope">
          <span>{{ formatMoney(scope.row.orderAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="outAmount" label="出库金额" width="120" align="right">
        <template slot-scope="scope">
          <span>{{ formatMoney(scope.row.outAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="returnAmount" label="退货金额" width="120" align="right">
        <template slot-scope="scope">
          <span>{{ formatMoney(scope.row.returnAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="netSales" label="净销售额" width="120" align="right">
        <template slot-scope="scope">
          <span class="net-sales">{{ formatMoney(scope.row.netSales) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="arAmount" label="未收金额" width="120" align="right">
        <template slot-scope="scope">
          <span class="ar-amount">{{ formatMoney(scope.row.arAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="receivedAmount" label="已收金额" width="120" align="right">
        <template slot-scope="scope">
          <span class="received-amount">{{ formatMoney(scope.row.receivedAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="receiptRate" label="回款率(%)" width="100" align="right">
        <template slot-scope="scope">
          <el-tag :type="getReceiptRateType(scope.row.receiptRate)" size="small">
            {{ scope.row.receiptRate }}%
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastOrderDate" label="最后下单日期" width="120" />
    </el-table>

    <!-- 分页 -->
    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import { customerAnalysisReport, exportCustomerAnalysisReport } from '@/api/wms/report'
import { listCustomer } from '@/api/wms/customer'

export default {
  name: 'CustomerAnalysisReport',
  data() {
    return {
      showSearch: true,
      loading: false,
      reportData: [],
      total: 0,
      dateRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 20,
        customerIds: []
      },
      customerList: [],
      summary: {
        orderCount: 0,
        netSales: 0,
        avgReceiptRate: 0
      }
    }
  },
  created() {
    const endDate = new Date()
    const startDate = new Date()
    startDate.setDate(startDate.getDate() - 30)
    this.dateRange = [
      this.formatDate(startDate),
      this.formatDate(endDate)
    ]
    this.getBaseData()
    this.getList()
  },
  methods: {
    getBaseData() {
      listCustomer({ status: '0' }).then(response => {
        this.customerList = response.data || []
      })
    },
    getList() {
      this.loading = true
      const params = {
        pageNum: this.queryParams.pageNum,
        pageSize: this.queryParams.pageSize,
        customerIds: this.queryParams.customerIds
      }
      if (this.dateRange && this.dateRange.length === 2) {
        params.startDate = this.dateRange[0]
        params.endDate = this.dateRange[1]
      }

      customerAnalysisReport(params).then(response => {
        this.reportData = response.rows || []
        this.total = response.total || 0
        this.calculateSummary()
      }).finally(() => {
        this.loading = false
      })
    },
    calculateSummary() {
      const totalOrderCount = this.reportData.reduce((sum, item) => sum + (item.orderCount || 0), 0)
      const totalNetSales = this.reportData.reduce((sum, item) => sum + (item.netSales || 0), 0)
      const avgReceipt = this.reportData.reduce((sum, item) => sum + (item.receiptRate || 0), 0) / (this.reportData.length || 1)

      this.summary = {
        orderCount: totalOrderCount,
        netSales: totalNetSales,
        avgReceiptRate: avgReceipt.toFixed(2)
      }
    },
    getReceiptRateType(rate) {
      if (rate >= 80) return 'success'
      if (rate >= 50) return ''
      return 'warning'
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      const endDate = new Date()
      const startDate = new Date()
      startDate.setDate(startDate.getDate() - 30)
      this.dateRange = [
        this.formatDate(startDate),
        this.formatDate(endDate)
      ]
      this.queryParams = {
        pageNum: 1,
        pageSize: 20,
        customerIds: []
      }
      this.getList()
    },
    handleExport() {
      this.download('wms/report/sales/customerAnalysis/export', {
        ...this.queryParams,
        startDate: this.dateRange && this.dateRange.length === 2 ? this.dateRange[0] : null,
        endDate: this.dateRange && this.dateRange.length === 2 ? this.dateRange[1] : null
      }, `客户销售分析表_${new Date().getTime()}.xlsx`)
    },
    formatMoney(value) {
      if (value === null || value === undefined) return '0.00'
      const num = Number(value)
      if (isNaN(num)) return '0.00'
      return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
    },
    /** 大金额简化显示：超过1万显示为万，超过1亿显示为亿 */
    formatMoneyShort(value) {
      if (value === null || value === undefined) return '0.00'
      const num = Number(value)
      if (isNaN(num)) return '0.00'
      const abs = Math.abs(num)
      if (abs >= 100000000) {
        return (num / 100000000).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + '亿'
      }
      if (abs >= 10000) {
        return (num / 10000).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + '万'
      }
      return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
    },
    formatDate(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    }
  }
}
</script>

<style lang="scss" scoped>
.query-form {
  background-color: #f5f7fa;
  padding: 18px 18px 0;
  border-radius: 4px;
}

.stat-cards {
  margin: 16px 0;
}

.stat-card {
  ::v-deep .el-card__body {
    padding: 16px;
  }
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;

  i {
    font-size: 24px;
  }
}

.stat-info {
  margin-left: 16px;
  flex: 1;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.net-sales {
  color: #67c23a;
  font-weight: bold;
}

.ar-amount {
  color: #f56c6c;
}

.received-amount {
  color: #67c23a;
}
</style>
