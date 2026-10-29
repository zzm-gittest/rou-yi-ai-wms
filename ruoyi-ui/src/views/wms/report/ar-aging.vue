<template>
  <div class="app-container">
    <!-- 查询条件 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" class="query-form">
      <el-form-item label="统计日期">
        <el-date-picker
          v-model="statisticsDate"
          type="date"
          placeholder="选择统计日期"
          value-format="yyyy-MM-dd"
          style="width: 150px"
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
            <div class="stat-icon" style="background: #fef0f0;">
              <i class="el-icon-wallet" style="color: #f56c6c;"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value" :title="'¥' + formatMoney(summary.totalAr)">¥{{ formatMoneyShort(summary.totalAr) }}</div>
              <div class="stat-label">应收总额</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fdf6ec;">
              <i class="el-icon-warning" style="color: #e6a23c;"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value" :title="'¥' + formatMoney(summary.overdueAmount)">¥{{ formatMoneyShort(summary.overdueAmount) }}</div>
              <div class="stat-label">逾期金额</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f0f9ff;">
              <i class="el-icon-data-analysis" style="color: #67c23a;"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summary.avgOverdueRate }}%</div>
              <div class="stat-label">平均逾期率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 账龄分布图表 -->
    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="24">
        <el-card shadow="hover">
          <div slot="header" class="card-header">
            <span>账龄分布</span>
          </div>
          <div class="chart-container">
            <div class="chart-item">
              <div class="chart-bar">
                <div class="bar-fill within-30" :style="{ width: getPercent(summary.within30, summary.totalAr) }"></div>
              </div>
              <div class="chart-info">
                <span class="chart-label">30天内</span>
                <span class="chart-value">¥{{ formatMoney(summary.within30) }}</span>
              </div>
            </div>
            <div class="chart-item">
              <div class="chart-bar">
                <div class="bar-fill days30-60" :style="{ width: getPercent(summary.days30_60, summary.totalAr) }"></div>
              </div>
              <div class="chart-info">
                <span class="chart-label">30-60天</span>
                <span class="chart-value">¥{{ formatMoney(summary.days30_60) }}</span>
              </div>
            </div>
            <div class="chart-item">
              <div class="chart-bar">
                <div class="bar-fill days60-90" :style="{ width: getPercent(summary.days60_90, summary.totalAr) }"></div>
              </div>
              <div class="chart-info">
                <span class="chart-label">60-90天</span>
                <span class="chart-value">¥{{ formatMoney(summary.days60_90) }}</span>
              </div>
            </div>
            <div class="chart-item">
              <div class="chart-bar">
                <div class="bar-fill days90-180" :style="{ width: getPercent(summary.days90_180, summary.totalAr) }"></div>
              </div>
              <div class="chart-info">
                <span class="chart-label">90-180天</span>
                <span class="chart-value">¥{{ formatMoney(summary.days90_180) }}</span>
              </div>
            </div>
            <div class="chart-item">
              <div class="chart-bar">
                <div class="bar-fill over180" :style="{ width: getPercent(summary.over180, summary.totalAr) }"></div>
              </div>
              <div class="chart-info">
                <span class="chart-label">180天以上</span>
                <span class="chart-value">¥{{ formatMoney(summary.over180) }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="reportData" border stripe style="margin-top: 16px">
      <el-table-column prop="customerCode" label="客户编码" width="120" />
      <el-table-column prop="customerName" label="客户名称" width="200" show-overflow-tooltip />
      <el-table-column prop="totalAr" label="应收总额" width="120" align="right">
        <template slot-scope="scope">
          <span class="total-ar">{{ formatMoney(scope.row.totalAr) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="within30" label="30天内" width="120" align="right">
        <template slot-scope="scope">
          <span class="within-30-text">{{ formatMoney(scope.row.within30) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="days30_60" label="30-60天" width="120" align="right">
        <template slot-scope="scope">
          <span class="days30-60-text">{{ formatMoney(scope.row.days30_60) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="days60_90" label="60-90天" width="120" align="right">
        <template slot-scope="scope">
          <span class="days60-90-text">{{ formatMoney(scope.row.days60_90) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="days90_180" label="90-180天" width="120" align="right">
        <template slot-scope="scope">
          <span class="days90-180-text">{{ formatMoney(scope.row.days90_180) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="over180" label="180天以上" width="120" align="right">
        <template slot-scope="scope">
          <span class="over180-text">{{ formatMoney(scope.row.over180) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="overdueAmount" label="逾期金额" width="120" align="right">
        <template slot-scope="scope">
          <span class="overdue-amount">{{ formatMoney(scope.row.overdueAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="overdueRate" label="逾期率(%)" width="100" align="right">
        <template slot-scope="scope">
          <el-tag :type="getOverdueRateType(scope.row.overdueRate)" size="small">
            {{ scope.row.overdueRate }}%
          </el-tag>
        </template>
      </el-table-column>
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
import { arAgingReport, exportArAgingReport } from '@/api/wms/report'
import { listCustomer } from '@/api/wms/customer'

export default {
  name: 'ArAgingReport',
  data() {
    return {
      showSearch: true,
      loading: false,
      reportData: [],
      total: 0,
      statisticsDate: null,
      queryParams: {
        pageNum: 1,
        pageSize: 20,
        customerIds: []
      },
      customerList: [],
      summary: {
        totalAr: 0,
        within30: 0,
        days30_60: 0,
        days60_90: 0,
        days90_180: 0,
        over180: 0,
        overdueAmount: 0,
        avgOverdueRate: 0
      }
    }
  },
  created() {
    this.statisticsDate = this.formatDate(new Date())
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
        statisticsDate: this.statisticsDate,
        customerIds: this.queryParams.customerIds
      }

      arAgingReport(params).then(response => {
        this.reportData = response.rows || []
        this.total = response.total || 0
        this.calculateSummary()
      }).finally(() => {
        this.loading = false
      })
    },
    calculateSummary() {
      const totalAr = this.reportData.reduce((sum, item) => sum + (item.totalAr || 0), 0)
      const overdueAmount = this.reportData.reduce((sum, item) => sum + (item.overdueAmount || 0), 0)
      const avgRate = this.reportData.reduce((sum, item) => sum + (item.overdueRate || 0), 0) / (this.reportData.length || 1)

      this.summary = {
        totalAr: totalAr,
        within30: this.reportData.reduce((sum, item) => sum + (item.within30 || 0), 0),
        days30_60: this.reportData.reduce((sum, item) => sum + (item.days30_60 || 0), 0),
        days60_90: this.reportData.reduce((sum, item) => sum + (item.days60_90 || 0), 0),
        days90_180: this.reportData.reduce((sum, item) => sum + (item.days90_180 || 0), 0),
        over180: this.reportData.reduce((sum, item) => sum + (item.over180 || 0), 0),
        overdueAmount: overdueAmount,
        avgOverdueRate: avgRate.toFixed(2)
      }
    },
    getPercent(value, total) {
      if (!total || total === 0) return '0%'
      return Math.min((value / total) * 100, 100).toFixed(2) + '%'
    },
    getOverdueRateType(rate) {
      if (rate < 10) return 'success'
      if (rate < 30) return ''
      if (rate < 50) return 'warning'
      return 'danger'
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.statisticsDate = this.formatDate(new Date())
      this.queryParams = {
        pageNum: 1,
        pageSize: 20,
        customerIds: []
      }
      this.getList()
    },
    handleExport() {
      this.download('wms/report/finance/arAging/export', {
        statisticsDate: this.statisticsDate,
        customerIds: this.queryParams.customerIds
      }, `应收账款账龄分析表_${new Date().getTime()}.xlsx`)
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

.card-header {
  font-weight: 500;
}

.chart-container {
  padding: 10px 0;
}

.chart-item {
  margin-bottom: 20px;

  &:last-child {
    margin-bottom: 0;
  }
}

.chart-bar {
  height: 24px;
  background-color: #f5f7fa;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 8px;
}

.bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease;

  &.within-30 { background-color: #67c23a; }
  &.days30-60 { background-color: #409eff; }
  &.days60-90 { background-color: #e6a23c; }
  &.days90-180 { background-color: #f56c6c; }
  &.over180 { background-color: #909399; }
}

.chart-info {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.chart-label {
  color: #606266;
}

.chart-value {
  font-weight: bold;
  color: #303133;
}

.total-ar {
  color: #f56c6c;
  font-weight: bold;
}

.within-30-text { color: #67c23a; }
.days30-60-text { color: #409eff; }
.days60-90-text { color: #e6a23c; }
.days90-180-text { color: #f56c6c; }
.over180-text { color: #909399; }

.overdue-amount {
  color: #f56c6c;
  font-weight: bold;
}
</style>
