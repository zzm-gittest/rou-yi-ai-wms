<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="仓库" prop="warehouseId">
        <el-select v-model="queryParams.warehouseId" placeholder="请选择仓库" clearable>
          <el-option
            v-for="item in warehouseOptions"
            :key="item.id"
            :label="item.warehouseName"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="商品编码" prop="goodsCode">
        <el-input
          v-model="queryParams.goodsCode"
          placeholder="请输入商品编码"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="商品名称" prop="goodsName">
        <el-input
          v-model="queryParams.goodsName"
          placeholder="请输入商品名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="SKU编码" prop="skuCode">
        <el-input
          v-model="queryParams.skuCode"
          placeholder="请输入SKU编码"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="仅预警" prop="lowStock">
        <el-checkbox v-model="queryParams.lowStock">仅显示库存预警(&lt;10)</el-checkbox>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['wms:stock:query']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="stockList" show-summary :summary-method="getSummaries">
      <el-table-column label="仓库" prop="warehouseName" min-width="120" />
      <el-table-column label="商品编码" prop="goodsCode" min-width="120" />
      <el-table-column label="商品名称" prop="goodsName" :show-overflow-tooltip="true" />
      <el-table-column label="SKU编码" prop="skuCode" min-width="120" />
      <el-table-column label="SKU名称" prop="skuName" :show-overflow-tooltip="true" />
      <el-table-column label="规格" prop="specInfo" min-width="120" :show-overflow-tooltip="true" />
      <el-table-column label="单位" prop="unitName" min-width="80" align="center" />
      <el-table-column label="库存数量" prop="quantity" min-width="100" align="right">
        <template slot-scope="scope">
          <span :style="{ color: scope.row.quantity < 0 ? 'red' : '' }">{{ formatNumber(scope.row.quantity) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="成本价" prop="costPrice" min-width="100" align="right">
        <template slot-scope="scope">
          <span>{{ scope.row.costPrice ? scope.row.costPrice.toFixed(2) : '0.00' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="库存金额" prop="amount" min-width="120" align="right">
        <template slot-scope="scope">
          <span>{{ scope.row.amount ? scope.row.amount.toFixed(2) : '0.00' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" align="center" prop="updateTime" min-width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
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
import { pageStock } from "@/api/wms/stock"
import { listWarehouse } from "@/api/wms/warehouse"

export default {
  name: "Stock",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 库存表格数据
      stockList: [],
      // 仓库选项
      warehouseOptions: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        warehouseId: null,
        goodsCode: null,
        goodsName: null,
        skuCode: null,
        lowStock: null
      }
    }
  },
  created() {
    // 检查是否从预警进入
    if (this.$route.query.lowStock === 'true') {
      this.queryParams.lowStock = true
    }
    this.getList()
    this.getWarehouseList()
  },
  methods: {
    /** 查询库存列表 */
    getList() {
      this.loading = true
      pageStock(this.queryParams).then(response => {
        this.stockList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 查询仓库列表 */
    getWarehouseList() {
      listWarehouse().then(response => {
        this.warehouseOptions = response.data
      })
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
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
        if (column.property === 'quantity') {
          const total = data.reduce((sum, row) => sum + (row.quantity || 0), 0)
          sums[index] = this.formatNumber(total)
        } else if (column.property === 'amount') {
          sums[index] = data.reduce((sum, row) => sum + (row.amount || 0), 0).toFixed(2)
        } else {
          sums[index] = ''
        }
      })
      return sums
    },
    /** 格式化数字，去除多余小数位 */
    formatNumber(val) {
      if (val === null || val === undefined) return '0'
      const num = Number(val)
      if (isNaN(num)) return val
      // 去除末尾多余的0，最多保留2位小数
      return parseFloat(num.toFixed(2))
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('wms/stock/export', {
        ...this.queryParams
      }, `库存_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
