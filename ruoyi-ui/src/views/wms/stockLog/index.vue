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
      <el-form-item label="单据类型" prop="billType">
        <el-select v-model="queryParams.billType" placeholder="单据类型" clearable>
          <el-option label="采购入库" value="1" />
          <el-option label="销售出库" value="2" />
          <el-option label="采购退货" value="3" />
          <el-option label="销售退货" value="4" />
          <el-option label="盘点" value="5" />
          <el-option label="调拨" value="6" />
        </el-select>
      </el-form-item>
      <el-form-item label="变动类型" prop="changeType">
        <el-select v-model="queryParams.changeType" placeholder="变动类型" clearable>
          <el-option label="增加" value="1" />
          <el-option label="减少" value="2" />
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
      <el-form-item label="商品名称" prop="goodsName">
        <el-input
          v-model="queryParams.goodsName"
          placeholder="请输入商品名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini"
          @click="handleExport" v-hasPermi="['wms:stockLog:query']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="logList">
      <el-table-column label="仓库" prop="warehouseName" min-width="120" />
      <el-table-column label="商品名称" prop="goodsName" min-width="150" show-overflow-tooltip />
      <el-table-column label="SKU编码" prop="skuCode" min-width="150" />
      <el-table-column label="SKU名称" prop="skuName" min-width="200" show-overflow-tooltip />
      <el-table-column label="单据类型" prop="billTypeName" min-width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="getBillTypeTag(scope.row.billType)" size="mini">
            {{ scope.row.billTypeName }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="单据编号" prop="billNo" min-width="160" />
      <el-table-column label="变动类型" prop="changeTypeName" min-width="80" align="center">
        <template slot-scope="scope">
          <span :style="{color: scope.row.changeType === '1' ? '#67C23A' : '#F56C6C'}">
            {{ scope.row.changeTypeName }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="变动数量" prop="quantity" min-width="100" align="right">
        <template slot-scope="scope">
          <span :style="{color: scope.row.changeType === '1' ? '#67C23A' : '#F56C6C'}">
            {{ scope.row.changeType === '1' ? '+' : '-' }}{{ scope.row.quantity }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="变动前" prop="beforeQty" min-width="100" align="right" />
      <el-table-column label="变动后" prop="afterQty" min-width="100" align="right" />
      <el-table-column label="操作人" prop="createBy" min-width="100" />
      <el-table-column label="操作时间" prop="createTime" min-width="160" />
      <el-table-column label="备注" prop="remark" show-overflow-tooltip />
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNo"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import { pageStockLog } from "@/api/wms/stockLog"
import { listWarehouse } from "@/api/wms/warehouse"

export default {
  name: "StockLog",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 库存变动日志表格数据
      logList: [],
      // 仓库选项
      warehouseOptions: [],
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        warehouseId: null,
        billType: null,
        changeType: null,
        billNo: null,
        goodsName: null
      }
    }
  },
  created() {
    this.getList()
    this.getWarehouseList()
  },
  methods: {
    /** 查询库存变动日志列表 */
    getList() {
      this.loading = true
      pageStockLog(this.queryParams).then(response => {
        this.logList = response.rows || []
        this.total = response.total || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    /** 查询仓库列表 */
    getWarehouseList() {
      listWarehouse().then(response => {
        this.warehouseOptions = response.data || []
      })
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('wms/stockLog/export', {
        ...this.queryParams
      }, `库存日志_${new Date().getTime()}.xlsx`)
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNo = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    /** 获取单据类型标签颜色 */
    getBillTypeTag(billType) {
      const tagMap = {
        '1': 'success',  // 采购入库
        '2': 'warning',  // 销售出库
        '3': 'info',     // 采购退货
        '4': 'danger',   // 销售退货
        '5': '',         // 盘点
        '6': 'primary'   // 调拨
      }
      return tagMap[billType] || ''
    }
  }
}
</script>

<style scoped>
</style>
