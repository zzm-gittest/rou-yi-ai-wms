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
      <el-form-item label="商品分类">
        <el-select v-model="queryParams.categoryIds" multiple placeholder="请选择商品分类" clearable style="width: 200px" @change="handleSearchChange">
          <el-option
            v-for="item in categoryList"
            :key="item.id"
            :label="item.categoryName"
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
      <el-form-item label="排序方式">
        <el-select v-model="queryParams.orderBy" placeholder="请选择排序方式" clearable style="width: 150px">
          <el-option label="按销售额" value="amount" />
          <el-option label="按销售量" value="quantity" />
          <el-option label="按利润" value="profit" />
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
              <i class="el-icon-goods" style="color: #409eff;"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ reportData.length }}</div>
              <div class="stat-label">商品数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f0f9ff;">
              <i class="el-icon-sold-out" style="color: #67c23a;"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatQty(summary.saleQuantity) }}</div>
              <div class="stat-label">总销售量</div>
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
              <div class="stat-value" :title="'¥' + formatMoney(summary.saleAmount)">¥{{ formatMoneyShort(summary.saleAmount) }}</div>
              <div class="stat-label">总销售额</div>
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
              <div class="stat-value" :title="'¥' + formatMoney(summary.profitAmount)">¥{{ formatMoneyShort(summary.profitAmount) }}</div>
              <div class="stat-label">总利润</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="reportData" border stripe style="margin-top: 16px">
      <el-table-column prop="rankNo" label="排名" width="80" align="center" />
      <el-table-column prop="goodsCode" label="商品编码" width="120" />
      <el-table-column prop="goodsName" label="商品名称" width="200" show-overflow-tooltip />
      <el-table-column prop="spec" label="规格" width="120" show-overflow-tooltip />
      <el-table-column prop="unit" label="单位" width="80" align="center" />
      <!-- 分类列（选择了分类才显示） -->
      <el-table-column v-if="showCategoryColumn" prop="categoryName" label="分类名称" width="120" show-overflow-tooltip />
      <!-- 仓库列（选择了仓库才显示） -->
      <el-table-column v-if="showWarehouseColumn" prop="warehouseName" label="仓库名称" width="120" show-overflow-tooltip />
      <el-table-column prop="saleQuantity" label="销售数量" width="100" align="right">
        <template slot-scope="scope">
          <span>{{ formatQty(scope.row.saleQuantity) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="saleAmount" label="销售金额" width="120" align="right">
        <template slot-scope="scope">
          <span>{{ formatMoney(scope.row.saleAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="profitAmount" label="利润金额" width="120" align="right">
        <template slot-scope="scope">
          <span :class="scope.row.profitAmount >= 0 ? 'profit-positive' : 'profit-negative'">
            {{ formatMoney(scope.row.profitAmount) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="profitRate" label="利润率(%)" width="100" align="right">
        <template slot-scope="scope">
          <span :class="scope.row.profitRate >= 0 ? 'profit-positive' : 'profit-negative'">
            {{ scope.row.profitRate }}%
          </span>
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
import { goodsRankReport, exportGoodsRankReport } from '@/api/wms/report'
import { listWarehouse } from '@/api/wms/warehouse'
import { listGoodsCategory } from '@/api/wms/goodsCategory'

export default {
  name: 'GoodsRankReport',
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
        categoryIds: [],
        warehouseIds: [],
        orderBy: 'amount'
      },
      warehouseList: [],
      categoryList: [],
      summary: {
        saleQuantity: 0,
        saleAmount: 0,
        profitAmount: 0
      }
    }
  },
  computed: {
    // 是否选择了分类
    hasCategory() {
      return this.queryParams.categoryIds && this.queryParams.categoryIds.length > 0
    },
    // 是否选择了仓库
    hasWarehouse() {
      return this.queryParams.warehouseIds && this.queryParams.warehouseIds.length > 0
    },
    // 是否显示分类列
    showCategoryColumn() {
      return this.hasCategory
    },
    // 是否显示仓库列
    showWarehouseColumn() {
      return this.hasWarehouse
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
      listWarehouse({ status: '0' }).then(response => {
        this.warehouseList = response.data || []
      })
      listGoodsCategory({ status: '0' }).then(response => {
        this.categoryList = response.data || []
      })
    },
    getList() {
      this.loading = true
      const params = {
        pageNum: this.queryParams.pageNum,
        pageSize: this.queryParams.pageSize,
        categoryIds: this.queryParams.categoryIds,
        warehouseIds: this.queryParams.warehouseIds,
        orderBy: this.queryParams.orderBy
      }
      if (this.dateRange && this.dateRange.length === 2) {
        params.startDate = this.dateRange[0]
        params.endDate = this.dateRange[1]
      }

      goodsRankReport(params).then(response => {
        this.reportData = response.rows || []
        this.total = response.total || 0
        this.calculateSummary()
      }).finally(() => {
        this.loading = false
      })
    },
    handleSearchChange() {
      // 搜索条件变化时，重置页码并重新查询
      this.handleQuery()
    },
    calculateSummary() {
      this.summary = {
        saleQuantity: this.reportData.reduce((sum, item) => sum + (item.saleQuantity || 0), 0),
        saleAmount: this.reportData.reduce((sum, item) => sum + (item.saleAmount || 0), 0),
        profitAmount: this.reportData.reduce((sum, item) => sum + (item.profitAmount || 0), 0)
      }
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
        categoryIds: [],
        warehouseIds: [],
        orderBy: 'amount'
      }
      this.getList()
    },
    handleExport() {
      const params = {
        startDate: this.dateRange && this.dateRange.length === 2 ? this.dateRange[0] : null,
        endDate: this.dateRange && this.dateRange.length === 2 ? this.dateRange[1] : null,
        orderBy: this.queryParams.orderBy
      }
      if (this.queryParams.categoryIds && this.queryParams.categoryIds.length > 0) {
        params.categoryIds = this.queryParams.categoryIds
      }
      if (this.queryParams.warehouseIds && this.queryParams.warehouseIds.length > 0) {
        params.warehouseIds = this.queryParams.warehouseIds
      }
      this.download('wms/report/sales/goodsRank/export', params, `商品销售排行榜_${new Date().getTime()}.xlsx`)
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
    /** 数量千分位格式化 */
    formatQty(value) {
      if (value === null || value === undefined) return '0'
      const num = Number(value)
      if (isNaN(num)) return value
      return parseFloat(num.toFixed(2)).toLocaleString('zh-CN')
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

.profit-positive {
  color: #67c23a;
  font-weight: bold;
}

.profit-negative {
  color: #f56c6c;
  font-weight: bold;
}
</style>
