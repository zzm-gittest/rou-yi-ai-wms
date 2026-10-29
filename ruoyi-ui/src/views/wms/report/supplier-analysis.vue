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
      <el-form-item label="供应商">
        <el-select v-model="queryParams.supplierIds" multiple placeholder="请选择供应商" clearable style="width: 200px">
          <el-option
            v-for="item in supplierList"
            :key="item.id"
            :label="item.supplierName"
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
              <div class="stat-label">供应商数量</div>
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
              <div class="stat-value" :title="'¥' + formatMoney(summary.netPurchase)">¥{{ formatMoneyShort(summary.netPurchase) }}</div>
              <div class="stat-label">总净采购额</div>
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
              <div class="stat-value">{{ summary.avgPaymentRate }}%</div>
              <div class="stat-label">平均付款率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="reportData" border stripe style="margin-top: 16px">
      <el-table-column prop="supplierCode" label="供应商编码" width="120" />
      <el-table-column prop="supplierName" label="供应商名称" width="200" show-overflow-tooltip />
      <el-table-column prop="contact" label="联系人" width="100" />
      <el-table-column prop="phone" label="联系电话" width="120" />
      <el-table-column prop="orderCount" label="订单数" width="100" align="right" />
      <el-table-column prop="orderAmount" label="订单金额" width="120" align="right">
        <template slot-scope="scope">
          <span>{{ formatMoney(scope.row.orderAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="inAmount" label="入库金额" width="120" align="right">
        <template slot-scope="scope">
          <span>{{ formatMoney(scope.row.inAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="returnAmount" label="退货金额" width="120" align="right">
        <template slot-scope="scope">
          <span class="return-amount">{{ formatMoney(scope.row.returnAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="netPurchase" label="净采购额" width="120" align="right">
        <template slot-scope="scope">
          <span class="net-purchase">{{ formatMoney(scope.row.netPurchase) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="apAmount" label="未付金额" width="120" align="right">
        <template slot-scope="scope">
          <span class="ap-amount">{{ formatMoney(scope.row.apAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="paidAmount" label="已付金额" width="120" align="right">
        <template slot-scope="scope">
          <span class="paid-amount">{{ formatMoney(scope.row.paidAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="paymentRate" label="付款率(%)" width="100" align="right">
        <template slot-scope="scope">
          <el-tag :type="getPaymentRateType(scope.row.paymentRate)" size="small">
            {{ scope.row.paymentRate }}%
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
import { supplierAnalysisReport, exportSupplierAnalysisReport } from '@/api/wms/report'
import { listSupplier } from '@/api/wms/supplier'

export default {
  name: 'SupplierAnalysisReport',
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
        supplierIds: []
      },
      supplierList: [],
      summary: {
        orderCount: 0,
        netPurchase: 0,
        avgPaymentRate: 0
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
      listSupplier({ status: '0' }).then(response => {
        this.supplierList = response.data || []
      })
    },
    getList() {
      this.loading = true
      const params = {
        pageNum: this.queryParams.pageNum,
        pageSize: this.queryParams.pageSize,
        supplierIds: this.queryParams.supplierIds
      }
      if (this.dateRange && this.dateRange.length === 2) {
        params.startDate = this.dateRange[0]
        params.endDate = this.dateRange[1]
      }

      supplierAnalysisReport(params).then(response => {
        this.reportData = response.rows || []
        this.total = response.total || 0
        this.calculateSummary()
      }).finally(() => {
        this.loading = false
      })
    },
    calculateSummary() {
      const totalOrderCount = this.reportData.reduce((sum, item) => sum + (item.orderCount || 0), 0)
      const totalNetPurchase = this.reportData.reduce((sum, item) => sum + (item.netPurchase || 0), 0)
      const avgPayment = this.reportData.reduce((sum, item) => sum + (item.paymentRate || 0), 0) / (this.reportData.length || 1)

      this.summary = {
        orderCount: totalOrderCount,
        netPurchase: totalNetPurchase,
        avgPaymentRate: avgPayment.toFixed(2)
      }
    },
    getPaymentRateType(rate) {
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
        supplierIds: []
      }
      this.getList()
    },
    handleExport() {
      this.download('wms/report/purchase/supplierAnalysis/export', {
        ...this.queryParams,
        startDate: this.dateRange && this.dateRange.length === 2 ? this.dateRange[0] : null,
        endDate: this.dateRange && this.dateRange.length === 2 ? this.dateRange[1] : null
      }, `供应商采购分析表_${new Date().getTime()}.xlsx`)
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

.net-purchase {
  color: #67c23a;
  font-weight: bold;
}

.ap-amount {
  color: #f56c6c;
}

.paid-amount {
  color: #67c23a;
}

.return-amount {
  color: #e6a23c;
}
</style>
