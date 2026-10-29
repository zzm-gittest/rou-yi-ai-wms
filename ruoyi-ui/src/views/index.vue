<template>
  <div class="dashboard-container">
    <OnboardingPanel />

    <!-- 概览卡片 -->
    <el-row :gutter="20" class="overview-cards">
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card goods-card">
          <div class="stat-icon">
            <i class="el-icon-goods"></i>
          </div>
          <div class="stat-content">
            <div class="stat-label">商品总数</div>
            <div class="stat-value">{{ overview.goodsCount || 0 }}</div>
            <div class="stat-desc">SKU: {{ overview.skuCount || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card stock-card">
          <div class="stat-icon">
            <i class="el-icon-box"></i>
          </div>
          <div class="stat-content">
            <div class="stat-label">库存总数</div>
            <div class="stat-value">{{ formatNumber(overview.totalStock) }}</div>
            <div class="stat-desc warning" v-if="overview.lowStockCount > 0">
              <i class="el-icon-warning"></i> {{ overview.lowStockCount }}个低库存
            </div>
            <div class="stat-desc" v-else>库存正常</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card purchase-card">
          <div class="stat-icon">
            <i class="el-icon-shopping-cart-2"></i>
          </div>
          <div class="stat-content">
            <div class="stat-label">本月采购</div>
            <div class="stat-value">{{ formatMoney(overview.monthlyPurchase) }}</div>
            <div class="stat-desc">待审核: {{ overview.pendingPurchaseOrder || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card sales-card">
          <div class="stat-icon">
            <i class="el-icon-sell"></i>
          </div>
          <div class="stat-content">
            <div class="stat-label">本月销售</div>
            <div class="stat-value">{{ formatMoney(overview.monthlySales) }}</div>
            <div class="stat-desc">待审核: {{ overview.pendingSalesOrder || 0 }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 中间区域：待办事项和财务汇总 -->
    <el-row :gutter="20" class="middle-section">
      <el-col :xs="24" :lg="12">
        <el-card class="box-card" shadow="never">
          <div slot="header" class="card-header">
            <span class="card-title"><i class="el-icon-bell"></i> 待办事项</span>
          </div>
          <div class="pending-list">
            <div
              v-for="task in pendingTasks"
              :key="task.name"
              class="pending-item"
              @click="goToPage(task.url)"
            >
              <span class="task-name">
                <el-tag :type="getTaskTypeColor(task.type)" size="mini">{{ task.name }}</el-tag>
              </span>
              <span class="task-count">{{ task.count }}</span>
            </div>
            <el-empty v-if="pendingTasks.length === 0" description="暂无待办事项" :image-size="80"></el-empty>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card class="box-card" shadow="never">
          <div slot="header" class="card-header">
            <span class="card-title"><i class="el-icon-money"></i> 财务汇总</span>
          </div>
          <div class="finance-summary">
            <div class="finance-item receivable">
              <div class="finance-label">应收账款</div>
              <div class="finance-value">¥{{ formatMoney(receivableAmount) }}</div>
              <div class="finance-count">{{ receivableCount }} 单</div>
            </div>
            <div class="finance-item payable">
              <div class="finance-label">应付账款</div>
              <div class="finance-value">¥{{ formatMoney(payableAmount) }}</div>
              <div class="finance-count">{{ payableCount }} 单</div>
            </div>
            <div class="finance-item income">
              <div class="finance-label">本月收入</div>
              <div class="finance-value income">¥{{ formatMoney(finance.monthlyIn) }}</div>
            </div>
            <div class="finance-item expense">
              <div class="finance-label">本月支出</div>
              <div class="finance-value expense">¥{{ formatMoney(finance.monthlyOut) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-section">
      <el-col :xs="24" :lg="12">
        <el-card class="box-card" shadow="never">
          <div slot="header" class="card-header">
            <span class="card-title"><i class="el-icon-data-line"></i> 销售趋势</span>
            <el-radio-group v-model="salesViewType" size="mini" class="view-toggle">
              <el-radio-button label="chart">图表</el-radio-button>
              <el-radio-button label="table">表格</el-radio-button>
            </el-radio-group>
          </div>
          <div v-show="salesViewType === 'chart'" class="chart-container">
            <div ref="salesChartRef" style="width: 100%; height: 300px;"></div>
          </div>
          <div v-show="salesViewType === 'table'" class="table-container">
            <el-table :data="salesTrendData" size="small" stripe>
              <el-table-column prop="month" label="月份" width="80" />
              <el-table-column prop="orderCount" label="订单数" width="80" align="right" />
              <el-table-column prop="orderAmount" label="订单金额" align="right">
                <template slot-scope="scope">
                  ¥{{ formatNumber(scope.row.orderAmount) }}
                </template>
              </el-table-column>
              <el-table-column prop="outAmount" label="出库金额" align="right">
                <template slot-scope="scope">
                  ¥{{ formatNumber(scope.row.outAmount) }}
                </template>
              </el-table-column>
              <el-table-column prop="returnAmount" label="退货金额" align="right">
                <template slot-scope="scope">
                  ¥{{ formatNumber(scope.row.returnAmount) }}
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card class="box-card" shadow="never">
          <div slot="header" class="card-header">
            <span class="card-title"><i class="el-icon-data-analysis"></i> 采购趋势</span>
            <el-radio-group v-model="purchaseViewType" size="mini" class="view-toggle">
              <el-radio-button label="chart">图表</el-radio-button>
              <el-radio-button label="table">表格</el-radio-button>
            </el-radio-group>
          </div>
          <div v-show="purchaseViewType === 'chart'" class="chart-container">
            <div ref="purchaseChartRef" style="width: 100%; height: 300px;"></div>
          </div>
          <div v-show="purchaseViewType === 'table'" class="table-container">
            <el-table :data="purchaseTrendData" size="small" stripe>
              <el-table-column prop="month" label="月份" width="80" />
              <el-table-column prop="orderCount" label="订单数" width="80" align="right" />
              <el-table-column prop="orderAmount" label="订单金额" align="right">
                <template slot-scope="scope">
                  ¥{{ formatNumber(scope.row.orderAmount) }}
                </template>
              </el-table-column>
              <el-table-column prop="inAmount" label="入库金额" align="right">
                <template slot-scope="scope">
                  ¥{{ formatNumber(scope.row.inAmount) }}
                </template>
              </el-table-column>
              <el-table-column prop="returnAmount" label="退货金额" align="right">
                <template slot-scope="scope">
                  ¥{{ formatNumber(scope.row.returnAmount) }}
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 底部图表 -->
    <el-row :gutter="20" class="bottom-section">
      <el-col :xs="24" :lg="12">
        <el-card class="box-card" shadow="never">
          <div slot="header" class="card-header">
            <span class="card-title"><i class="el-icon-pie-chart"></i> 仓库库存分布</span>
          </div>
          <div class="chart-container">
            <div ref="warehouseChartRef" style="width: 100%; height: 280px;"></div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card class="box-card" shadow="never">
          <div slot="header" class="card-header">
            <span class="card-title"><i class="el-icon-warning"></i> 库存预警</span>
          </div>
          <div class="warning-list">
            <el-table :data="stockWarnings" size="small" stripe max-height="280">
              <el-table-column prop="skuName" label="商品名称" :show-overflow-tooltip="true" />
              <el-table-column prop="warehouseName" label="所属仓库" width="120" />
              <el-table-column prop="quantity" label="当前库存" width="100" align="right" />
              <el-table-column prop="unitName" label="单位" width="60" />
            </el-table>
            <el-empty v-if="stockWarnings.length === 0" description="暂无库存预警" :image-size="60"></el-empty>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import OnboardingPanel from '@/components/OnboardingPanel'
import { getOverview, getSalesTrend, getPurchaseTrend, getStockWarning, getFinanceSummary, getPendingTasks, getWarehouseStock } from '@/api/wms/statistics'

export default {
  name: 'Dashboard',
  components: {
    OnboardingPanel
  },
  data() {
    return {
      loading: false,
      overview: {
        goodsCount: 0,
        skuCount: 0,
        warehouseCount: 0,
        customerCount: 0,
        supplierCount: 0,
        totalStock: 0,
        lowStockCount: 0,
        pendingPurchaseOrder: 0,
        pendingSalesOrder: 0,
        totalReceivable: 0,
        totalPayable: 0,
        monthlySales: 0,
        monthlyPurchase: 0
      },
      salesTrendData: [],
      purchaseTrendData: [],
      stockWarnings: [],
      warehouseStockData: [],
      finance: {
        receivable: null,
        payable: null,
        monthlyIn: 0,
        monthlyOut: 0
      },
      pendingTasks: [],
      salesViewType: 'chart',
      purchaseViewType: 'chart',
      salesChart: null,
      purchaseChart: null,
      warehouseChart: null
    }
  },
  computed: {
    receivableAmount() {
      if (this.finance.receivable && this.finance.receivable.amount) {
        return this.finance.receivable.amount
      }
      return 0
    },
    receivableCount() {
      if (this.finance.receivable && this.finance.receivable.count) {
        return this.finance.receivable.count
      }
      return 0
    },
    payableAmount() {
      if (this.finance.payable && this.finance.payable.amount) {
        return this.finance.payable.amount
      }
      return 0
    },
    payableCount() {
      if (this.finance.payable && this.finance.payable.count) {
        return this.finance.payable.count
      }
      return 0
    }
  },
  mounted() {
    this.loadData()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.salesChart) this.salesChart.dispose()
    if (this.purchaseChart) this.purchaseChart.dispose()
    if (this.warehouseChart) this.warehouseChart.dispose()
  },
  methods: {
    loadData() {
      this.loading = true
      Promise.all([
        this.getOverview(),
        this.getSalesTrend(),
        this.getPurchaseTrend(),
        this.getStockWarning(),
        this.getFinanceSummary(),
        this.getPendingTasks(),
        this.getWarehouseStock()
      ]).finally(() => {
        this.loading = false
        this.$nextTick(() => {
          this.initCharts()
        })
      })
    },

    getOverview() {
      return getOverview().then(response => {
        this.overview = response.data
      })
    },

    getSalesTrend() {
      return getSalesTrend().then(response => {
        this.salesTrendData = response.data
      })
    },

    getPurchaseTrend() {
      return getPurchaseTrend().then(response => {
        this.purchaseTrendData = response.data
      })
    },

    getStockWarning() {
      return getStockWarning().then(response => {
        this.stockWarnings = response.data
      })
    },

    getFinanceSummary() {
      return getFinanceSummary().then(response => {
        this.finance = response.data
      })
    },

    getPendingTasks() {
      return getPendingTasks().then(response => {
        this.pendingTasks = response.data
      })
    },

    getWarehouseStock() {
      return getWarehouseStock().then(response => {
        this.warehouseStockData = response.data
      })
    },

    initCharts() {
      this.initSalesChart()
      this.initPurchaseChart()
      this.initWarehouseChart()
    },

    initSalesChart() {
      if (!this.$refs.salesChartRef) return

      this.salesChart = echarts.init(this.$refs.salesChartRef)
      const months = this.salesTrendData.map(d => d.month)
      const orderAmounts = this.salesTrendData.map(d => d.orderAmount || 0)
      const outAmounts = this.salesTrendData.map(d => d.outAmount || 0)
      const returnAmounts = this.salesTrendData.map(d => d.returnAmount || 0)

      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' }
        },
        legend: {
          data: ['订单金额', '出库金额', '退货金额']
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: months
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            formatter: function(value) {
              return value >= 10000 ? (value / 10000).toFixed(1) + '万' : value
            }
          }
        },
        series: [
          {
            name: '订单金额',
            type: 'line',
            smooth: true,
            data: orderAmounts,
            itemStyle: { color: '#409EFF' }
          },
          {
            name: '出库金额',
            type: 'line',
            smooth: true,
            data: outAmounts,
            itemStyle: { color: '#67C23A' }
          },
          {
            name: '退货金额',
            type: 'line',
            smooth: true,
            data: returnAmounts,
            itemStyle: { color: '#F56C6C' }
          }
        ]
      }

      this.salesChart.setOption(option)
    },

    initPurchaseChart() {
      if (!this.$refs.purchaseChartRef) return

      this.purchaseChart = echarts.init(this.$refs.purchaseChartRef)
      const months = this.purchaseTrendData.map(d => d.month)
      const orderAmounts = this.purchaseTrendData.map(d => d.orderAmount || 0)
      const inAmounts = this.purchaseTrendData.map(d => d.inAmount || 0)
      const returnAmounts = this.purchaseTrendData.map(d => d.returnAmount || 0)

      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' }
        },
        legend: {
          data: ['订单金额', '入库金额', '退货金额']
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: months
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            formatter: function(value) {
              return value >= 10000 ? (value / 10000).toFixed(1) + '万' : value
            }
          }
        },
        series: [
          {
            name: '订单金额',
            type: 'bar',
            data: orderAmounts,
            itemStyle: { color: '#409EFF' }
          },
          {
            name: '入库金额',
            type: 'bar',
            data: inAmounts,
            itemStyle: { color: '#67C23A' }
          },
          {
            name: '退货金额',
            type: 'bar',
            data: returnAmounts,
            itemStyle: { color: '#F56C6C' }
          }
        ]
      }

      this.purchaseChart.setOption(option)
    },

    initWarehouseChart() {
      if (!this.$refs.warehouseChartRef) return

      this.warehouseChart = echarts.init(this.$refs.warehouseChartRef)

      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          right: 10,
          top: 'center'
        },
        series: [
          {
            name: '库存分布',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 16,
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: [
              { value: 1048, name: '仓库A' },
              { value: 735, name: '仓库B' },
              { value: 580, name: '仓库C' },
              { value: 484, name: '仓库D' }
            ]
          }
        ]
      }

      if (this.warehouseStockData.length > 0) {
        option.series[0].data = this.warehouseStockData.map(item => ({
          value: item.quantity,
          name: item.warehouseName
        }))
      }

      this.warehouseChart.setOption(option)
    },

    handleResize() {
      if (this.salesChart) this.salesChart.resize()
      if (this.purchaseChart) this.purchaseChart.resize()
      if (this.warehouseChart) this.warehouseChart.resize()
    },

    goToPage(url) {
      // 获取侧边栏路由
      const sidebarRoutes = this.$store.state.permission.sidebarRouters || []

      // 定义 URL 到路由关键词的映射
      const urlKeywordMap = {
        '/wms/purchaseOrder': 'purchaseOrder',
        '/wms/salesOrder': 'salesOrder',
        '/wms/purchaseIn': 'purchaseIn',
        '/wms/salesOut': 'salesOut',
        '/wms/purchaseReturn': 'purchaseReturn',
        '/wms/salesReturn': 'salesReturn',
        '/wms/stock': 'stockQuery',
        '/wms/stockTransfer': 'stockTransfer',
        '/wms/stockCheck': 'stockCheck',
        '/wms/accountsReceivable': 'accountsReceivable',
        '/wms/accountsPayable': 'accountsPayable',
        '/wms/expense': 'expense',
        '/wms/financeLog': 'financeLog'
      }

      const keyword = urlKeywordMap[url]
      if (!keyword) {
        this.$message.warning('未找到对应的页面')
        return
      }

      // 递归查找路由，并构建完整路径
      function findRouteByKeyword(routes, keyword, parentPath = '') {
        for (const route of routes) {
          // 构建当前路由的完整路径
          let fullPath = route.path || ''
          if (parentPath && !fullPath.startsWith('/')) {
            fullPath = parentPath + '/' + fullPath
          }

          // 检查 path 是否包含关键词（精确匹配最后一段）
          if (fullPath && fullPath.toLowerCase().endsWith(keyword.toLowerCase())) {
            return fullPath
          }

          if (route.children && route.children.length > 0) {
            const found = findRouteByKeyword(route.children, keyword, fullPath)
            if (found) return found
          }
        }
        return null
      }

      const targetPath = findRouteByKeyword(sidebarRoutes, keyword)

      if (targetPath) {
        // 如果是库存预警，添加 lowStock 查询参数
        if (url === '/wms/stock') {
          this.$router.push({ path: targetPath, query: { lowStock: 'true' } })
        } else {
          this.$router.push(targetPath)
        }
      } else {
        this.$message.warning('未找到对应的页面路由')
      }
    },

    getTaskTypeColor(type) {
      const colors = {
        purchase: 'warning',
        sales: 'success',
        stock: 'danger',
        finance: 'info'
      }
      return colors[type] || 'info'
    },

    formatNumber(num) {
      if (num === null || num === undefined) return '0'
      return Number(num).toLocaleString()
    },

    formatMoney(amount) {
      if (amount === null || amount === undefined) return '0.00'
      const num = Number(amount)
      if (num >= 10000) {
        return (num / 10000).toFixed(2) + '万'
      }
      return num.toFixed(2)
    }
  }
}
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 16px;
  background: #f5f7fa;
  min-height: calc(100vh - 84px);

  .overview-cards {
    margin-bottom: 16px;
  }

  .stat-card {
    background: #fff;
    border-radius: 4px;
    padding: 20px 24px;
    display: flex;
    align-items: center;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
    border: 1px solid #e5e7eb;
    transition: all 0.2s ease;
    height: 100px;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
      border-color: #d1d5db;
    }

    .stat-icon {
      width: 48px;
      height: 48px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 22px;
      color: #fff;
      flex-shrink: 0;
    }

    &.goods-card .stat-icon {
      background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    }

    &.stock-card .stat-icon {
      background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
    }

    &.purchase-card .stat-icon {
      background: linear-gradient(135deg, #10b981 0%, #059669 100%);
    }

    &.sales-card .stat-icon {
      background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
    }

    .stat-content {
      margin-left: 16px;
      flex: 1;
      min-width: 0;
    }

    .stat-label {
      font-size: 13px;
      color: #6b7280;
      margin-bottom: 4px;
    }

    .stat-value {
      font-size: 26px;
      font-weight: 600;
      color: #111827;
      line-height: 1.2;
      margin-bottom: 4px;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    }

    .stat-desc {
      font-size: 12px;
      color: #9ca3af;

      &.warning {
        color: #ef4444;
        font-weight: 500;
      }
    }
  }

  .middle-section,
  .chart-section,
  .bottom-section {
    margin-bottom: 16px;
  }

  .box-card {
    border-radius: 4px;
    border: 1px solid #e5e7eb;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);

    ::v-deep .el-card__header {
      padding: 16px 20px;
      border-bottom: 1px solid #e5e7eb;
      background: #fafbfc;
    }

    ::v-deep .el-card__body {
      padding: 20px;
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .card-title {
      font-size: 15px;
      font-weight: 600;
      color: #374151;
      display: flex;
      align-items: center;

      i {
        margin-right: 6px;
        color: #3b82f6;
        font-size: 16px;
      }
    }

    .view-toggle {
      ::v-deep .el-radio-group {
        .el-radio-button__inner {
          padding: 5px 12px;
          font-size: 12px;
        }
      }
    }
  }

  .pending-list {
    min-height: 200px;

    .pending-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      border-bottom: 1px solid #f3f4f6;
      cursor: pointer;
      transition: all 0.2s;
      border-radius: 4px;

      &:hover {
        background-color: #f9fafb;
      }

      &:last-child {
        border-bottom: none;
      }

      .task-name {
        ::v-deep .el-tag {
          border-radius: 3px;
          font-weight: 500;
        }
      }

      .task-count {
        font-size: 16px;
        font-weight: 600;
        color: #ef4444;
        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      }
    }
  }

  .finance-summary {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;

    .finance-item {
      padding: 18px;
      border-radius: 4px;
      background: #f9fafb;
      border: 1px solid #e5e7eb;
      transition: all 0.2s;

      &:hover {
        background: #f3f4f6;
      }

      &.receivable {
        background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
        border-color: #fbbf24;
      }

      &.payable {
        background: linear-gradient(135deg, #ddd6fe 0%, #c4b5fd 100%);
        border-color: #a78bfa;
        color: #5b21b6;
      }

      &.income {
        background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
        border-color: #34d399;
        color: #065f46;
      }

      &.expense {
        background: linear-gradient(135deg, #fecaca 0%, #fca5a5 100%);
        border-color: #f87171;
        color: #991b1b;
      }

      .finance-label {
        font-size: 12px;
        opacity: 0.85;
        margin-bottom: 6px;
        font-weight: 500;
      }

      .finance-value {
        font-size: 20px;
        font-weight: 600;
        margin-bottom: 4px;
        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      }

      .finance-count {
        font-size: 12px;
        opacity: 0.7;
      }
    }
  }

  .chart-container {
    min-height: 300px;

    ::v-deep .el-card__body {
      padding: 16px 20px;
    }
  }

  .table-container {
    max-height: 300px;
    overflow-y: auto;

    ::v-deep .el-table {
      th {
        background: #f9fafb;
        color: #6b7280;
        font-weight: 600;
        font-size: 13px;
      }

      td {
        font-size: 13px;
        color: #374151;
      }
    }
  }

  .warning-list {
    min-height: 280px;

    ::v-deep .el-table {
      th {
        background: #f9fafb;
        color: #6b7280;
        font-weight: 600;
        font-size: 13px;
      }

      td {
        font-size: 13px;
        color: #374151;
      }
    }
  }

  // 空状态优化
  ::v-deep .el-empty {
    padding: 30px 0;
  }
}
</style>
