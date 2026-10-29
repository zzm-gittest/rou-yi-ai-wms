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
        <el-select v-model="queryParams.categoryIds" multiple placeholder="请选择商品分类" clearable style="width: 200px">
          <el-option
            v-for="item in categoryList"
            :key="item.id"
            :label="item.categoryName"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="仓库">
        <el-select v-model="queryParams.warehouseIds" multiple placeholder="请选择仓库" clearable style="width: 200px">
          <el-option
            v-for="item in warehouseList"
            :key="item.id"
            :label="item.warehouseName"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="商品名称">
        <el-input
          v-model="queryParams.goodsName"
          placeholder="请输入商品名称/编码"
          clearable
          style="width: 200px"
          @keyup.enter.native="handleQuery"
        />
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
              <i class="el-icon-box" style="color: #409eff;"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatQty(summary.totalQty) }}</div>
              <div class="stat-label">期初总量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f0f9ff;">
              <i class="el-icon-download" style="color: #67c23a;"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatQty(summary.inQty) }}</div>
              <div class="stat-label">入库总量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fef0f0;">
              <i class="el-icon-upload2" style="color: #f56c6c;"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatQty(summary.outQty) }}</div>
              <div class="stat-label">出库总量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fdf6ec;">
              <i class="el-icon-goods" style="color: #e6a23c;"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatQty(summary.endQty) }}</div>
              <div class="stat-label">期末总量</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="pagedData" border stripe style="margin-top: 16px">
      <el-table-column prop="warehouseName" label="仓库" width="120" fixed />
      <el-table-column prop="categoryName" label="分类" width="120" show-overflow-tooltip />
      <el-table-column prop="goodsCode" label="商品编码" width="120" fixed />
      <el-table-column prop="goodsName" label="商品名称" width="200" show-overflow-tooltip />
      <el-table-column prop="spec" label="规格" width="120" show-overflow-tooltip />
      <el-table-column prop="unit" label="单位" width="80" align="center" />
      <el-table-column prop="beginQty" label="期初数量" width="100" align="right">
        <template slot-scope="scope">
          <span>{{ formatQty(scope.row.beginQty) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="beginAmount" label="期初金额" width="120" align="right">
        <template slot-scope="scope">
          <span>{{ formatMoney(scope.row.beginAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="inQty" label="入库数量" width="100" align="right">
        <template slot-scope="scope">
          <span class="in-qty">{{ formatQty(scope.row.inQty) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="inAmount" label="入库金额" width="120" align="right">
        <template slot-scope="scope">
          <span class="in-amount">{{ formatMoney(scope.row.inAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="outQty" label="出库数量" width="100" align="right">
        <template slot-scope="scope">
          <span class="out-qty">{{ formatQty(scope.row.outQty) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="outAmount" label="出库金额" width="120" align="right">
        <template slot-scope="scope">
          <span class="out-amount">{{ formatMoney(scope.row.outAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="endQty" label="期末数量" width="100" align="right">
        <template slot-scope="scope">
          <span>{{ formatQty(scope.row.endQty) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="endAmount" label="期末金额" width="120" align="right">
        <template slot-scope="scope">
          <span>{{ formatMoney(scope.row.endAmount) }}</span>
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
import { stockPsiReport, exportStockPsiReport } from '@/api/wms/report'
import { completeTask } from '@/api/wms/onboarding'
import { listWarehouse } from '@/api/wms/warehouse'
import { listGoodsCategory } from '@/api/wms/goodsCategory'

export default {
  name: 'StockPsiReport',
  data() {
    return {
      showSearch: true,
      loading: false,
      reportData: [],
      allData: [],
      total: 0,
      dateRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        categoryIds: [],
        warehouseIds: [],
        goodsName: ''
      },
      warehouseList: [],
      categoryList: [],
      summary: {
        totalQty: 0,
        inQty: 0,
        outQty: 0,
        endQty: 0
      }
    }
  },
  computed: {
    /** 当前页显示的数据（客户端分页） */
    pagedData() {
      const start = (this.queryParams.pageNum - 1) * this.queryParams.pageSize
      const end = start + this.queryParams.pageSize
      return this.allData.slice(start, end)
    }
  },
  created() {
    completeTask('view_report').catch(() => {})

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
        goodsName: this.queryParams.goodsName || undefined
      }
      if (this.dateRange && this.dateRange.length === 2) {
        params.startDate = this.dateRange[0]
        params.endDate = this.dateRange[1]
      }

      stockPsiReport(params).then(response => {
        this.allData = response.rows || []
        this.reportData = this.allData
        this.total = this.allData.length
        this.calculateSummary()
      }).finally(() => {
        this.loading = false
      })
    },
    calculateSummary() {
      this.summary = {
        totalQty: this.allData.reduce((sum, item) => sum + Number(item.beginQty || 0), 0),
        inQty: this.allData.reduce((sum, item) => sum + Number(item.inQty || 0), 0),
        outQty: this.allData.reduce((sum, item) => sum + Number(item.outQty || 0), 0),
        endQty: this.allData.reduce((sum, item) => sum + Number(item.endQty || 0), 0)
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
        pageSize: 10,
        categoryIds: [],
        warehouseIds: [],
        goodsName: ''
      }
      this.getList()
    },
    handleExport() {
      this.download('wms/report/stock/psi/export', {
        ...this.queryParams,
        startDate: this.dateRange && this.dateRange.length === 2 ? this.dateRange[0] : null,
        endDate: this.dateRange && this.dateRange.length === 2 ? this.dateRange[1] : null
      }, `库存进销存报表_${new Date().getTime()}.xlsx`)
    },
    formatMoney(value) {
      if (value === null || value === undefined) return '0.00'
      const num = Number(value)
      if (isNaN(num)) return '0.00'
      return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
    },
    /** 数量格式化：去除多余小数位，加千分位 */
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

.in-qty, .in-amount {
  color: #67c23a;
  font-weight: bold;
}

.out-qty, .out-amount {
  color: #f56c6c;
  font-weight: bold;
}
</style>
