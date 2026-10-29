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
        <el-select v-model="queryParams.customerIds" multiple placeholder="请选择客户" clearable style="width: 200px" @change="handleSearchChange">
          <el-option
            v-for="item in customerList"
            :key="item.id"
            :label="item.customerName"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="仓库">
        <el-select v-model="queryParams.warehouseIds" multiple placeholder="请选择仓库" clearable style="width: 200px" @change="handleSearchChange">
          <el-option
            v-for="item in warehouseList"
            :key="item.id"
            :label="item.warehouseName"
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
              <i class="el-icon-document" style="color: #409eff;"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summary.orderCount }}</div>
              <div class="stat-label">订单数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f0f9ff;">
              <i class="el-icon-shopping-cart-2" style="color: #67c23a;"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value" :title="'¥' + formatMoney(summary.orderAmount)">¥{{ formatMoneyShort(summary.orderAmount) }}</div>
              <div class="stat-label">订单金额</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fef0f0;">
              <i class="el-icon-sold-out" style="color: #f56c6c;"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value" :title="'¥' + formatMoney(summary.outAmount)">¥{{ formatMoneyShort(summary.outAmount) }}</div>
              <div class="stat-label">出库金额</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fdf6ec;">
              <i class="el-icon-coin" style="color: #e6a23c;"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value" :title="'¥' + formatMoney(summary.netSales)">¥{{ formatMoneyShort(summary.netSales) }}</div>
              <div class="stat-label">净销售额</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="reportData" border stripe style="margin-top: 16px">
      <!-- 日期列 -->
      <el-table-column v-if="showDateColumn" prop="reportDate" label="报表日期" width="120" fixed />

      <!-- 客户列 -->
      <el-table-column v-if="showCustomerColumn" prop="customerName" label="客户名称" width="150" show-overflow-tooltip />

      <!-- 仓库列 -->
      <el-table-column v-if="showWarehouseColumn" prop="warehouseName" label="仓库名称" width="150" show-overflow-tooltip />

      <el-table-column prop="orderCount" label="订单数" width="100" align="right" />
      <el-table-column prop="orderAmount" label="订单金额" width="120" align="right">
        <template slot-scope="scope">
          <span>{{ formatMoney(scope.row.orderAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="outCount" label="出库单数" width="100" align="right" />
      <el-table-column prop="outAmount" label="出库金额" width="120" align="right">
        <template slot-scope="scope">
          <span>{{ formatMoney(scope.row.outAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="returnCount" label="退货单数" width="100" align="right" />
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
import { salesDailyReport } from '@/api/wms/report'
import { listCustomer } from '@/api/wms/customer'
import { listWarehouse } from '@/api/wms/warehouse'

export default {
  name: 'SalesDailyReport',
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
        customerIds: [],
        warehouseIds: []
      },
      customerList: [],
      warehouseList: [],
      summary: {
        orderCount: 0,
        orderAmount: 0,
        outAmount: 0,
        netSales: 0
      }
    }
  },
  computed: {
    // 是否有日期范围
    hasDateRange() {
      return this.dateRange && this.dateRange.length === 2
    },
    // 是否选择了客户
    hasCustomer() {
      return this.queryParams.customerIds && this.queryParams.customerIds.length > 0
    },
    // 是否选择了仓库
    hasWarehouse() {
      return this.queryParams.warehouseIds && this.queryParams.warehouseIds.length > 0
    },
    // 自动计算分组方式
    autoGroupBy() {
      // 根据选择条件自动决定分组方式
      if (this.hasDateRange) {
        // 有日期
        if (this.hasCustomer && this.hasWarehouse) {
          return 'date_customer_warehouse'
        } else if (this.hasCustomer) {
          return 'date_customer'
        } else if (this.hasWarehouse) {
          return 'date_warehouse'
        } else {
          return 'date'
        }
      } else {
        // 没有日期
        if (this.hasCustomer && this.hasWarehouse) {
          return 'customer_warehouse'
        } else if (this.hasCustomer) {
          return 'customer'
        } else if (this.hasWarehouse) {
          return 'warehouse'
        } else {
          return 'date'
        }
      }
    },
    // 是否显示日期列
    showDateColumn() {
      return this.hasDateRange
    },
    // 是否显示客户列
    showCustomerColumn() {
      return this.hasCustomer
    },
    // 是否显示仓库列
    showWarehouseColumn() {
      return this.hasWarehouse
    }
  },
  created() {
    console.log('===== SalesDailyReport 组件已加载 =====')
    console.log('版本: 2024-02-16 v4 - 自动分组')
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
      listWarehouse({ status: '0' }).then(response => {
        this.warehouseList = response.data || []
      })
    },
    getList() {
      console.log('=== getList 被调用 ===')
      this.loading = true
      const params = {
        pageNum: this.queryParams.pageNum,
        pageSize: this.queryParams.pageSize,
        customerIds: this.queryParams.customerIds,
        warehouseIds: this.queryParams.warehouseIds,
        groupBy: this.autoGroupBy
      }
      if (this.hasDateRange) {
        params.startDate = this.dateRange[0]
        params.endDate = this.dateRange[1]
      }
      console.log('请求参数:', params)
      console.log('自动分组方式:', this.autoGroupBy)

      salesDailyReport(params).then(response => {
        console.log('接口返回:', response)
        this.reportData = response.rows || []
        this.total = response.total || 0
        console.log('reportData:', this.reportData)
        this.calculateSummary()
      }).finally(() => {
        this.loading = false
      })
    },
    calculateSummary() {
      this.summary = {
        orderCount: this.reportData.reduce((sum, item) => sum + (item.orderCount || 0), 0),
        orderAmount: this.reportData.reduce((sum, item) => sum + (item.orderAmount || 0), 0),
        outAmount: this.reportData.reduce((sum, item) => sum + (item.outAmount || 0), 0),
        netSales: this.reportData.reduce((sum, item) => sum + (item.netSales || 0), 0)
      }
    },
    handleSearchChange() {
      // 搜索条件变化时，重置页码并重新查询
      this.handleQuery()
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
        customerIds: [],
        warehouseIds: []
      }
      this.getList()
    },
    handleExport() {
      this.download('wms/report/sales/daily/export', {
        pageNum: undefined,
        pageSize: undefined,
        customerIds: this.queryParams.customerIds,
        warehouseIds: this.queryParams.warehouseIds,
        groupBy: this.autoGroupBy,
        startDate: this.hasDateRange ? this.dateRange[0] : null,
        endDate: this.hasDateRange ? this.dateRange[1] : null
      }, `销售日报表_${new Date().getTime()}.xlsx`)
    },
    formatMoney(value) {
      if (value === null || value === undefined) return '0.00'
      // 千分位格式化，保留两位小数
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
</style>
