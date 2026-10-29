<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="租户ID" prop="tenantId">
        <el-input
          v-model="queryParams.tenantId"
          placeholder="请输入租户ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="单据类型" prop="billType">
        <el-select v-model="queryParams.billType" placeholder="请选择单据类型" clearable>
          <el-option
            v-for="item in billTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
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
      <el-form-item label="结果" prop="result">
        <el-select v-model="queryParams.result" placeholder="请选择结果" clearable>
          <el-option label="成功" value="SUCCESS" />
          <el-option label="失败" value="FAIL" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作人" prop="operator">
        <el-input
          v-model="queryParams.operator"
          placeholder="请输入操作人"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>

    <el-table v-loading="loading" :data="logList">
      <el-table-column label="租户ID" prop="tenantId" width="90" />
      <el-table-column label="单据类型" prop="billType" min-width="140">
        <template slot-scope="scope">
          <el-tag size="mini" type="info">{{ getBillTypeLabel(scope.row.billType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="动作" prop="actionType" width="90" align="center" />
      <el-table-column label="模式" prop="approveMode" width="120" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.approveMode === 'AUTO_POLICY' ? 'success' : 'warning'" size="mini">
            {{ scope.row.approveMode === 'AUTO_POLICY' ? '自动策略' : '人工审核' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="结果" prop="result" width="90" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.result === 'SUCCESS' ? 'success' : 'danger'" size="mini">
            {{ scope.row.result === 'SUCCESS' ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="单据编号" prop="billNo" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作人" prop="operator" width="120" />
      <el-table-column label="时间" prop="createTime" width="170">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="100">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleDetail(scope.row)"
            v-hasPermi="['wms:approveLog:query']"
          >详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNo"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog title="审核日志详情" :visible.sync="open" width="720px" append-to-body>
      <el-descriptions :column="2" border v-if="detail">
        <el-descriptions-item label="租户ID">{{ detail.tenantId }}</el-descriptions-item>
        <el-descriptions-item label="单据类型">{{ getBillTypeLabel(detail.billType) }}</el-descriptions-item>
        <el-descriptions-item label="单据ID">{{ detail.billId }}</el-descriptions-item>
        <el-descriptions-item label="单据编号">{{ detail.billNo }}</el-descriptions-item>
        <el-descriptions-item label="动作">{{ detail.actionType }}</el-descriptions-item>
        <el-descriptions-item label="模式">{{ detail.approveMode }}</el-descriptions-item>
        <el-descriptions-item label="结果">{{ detail.result }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detail.operator }}</el-descriptions-item>
        <el-descriptions-item label="操作人ID">{{ detail.operatorId }}</el-descriptions-item>
        <el-descriptions-item label="时间">{{ parseTime(detail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2">{{ detail.errorMsg || '-' }}</el-descriptions-item>
        <el-descriptions-item label="策略快照" :span="2">{{ detail.policySnapshot || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { pageApproveLog, getApproveLog } from "@/api/wms/approveLog"

export default {
  name: "ApproveLog",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      logList: [],
      open: false,
      detail: null,
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        tenantId: null,
        billType: null,
        billNo: null,
        result: null,
        operator: null
      },
      billTypeOptions: [
        { label: "采购订单", value: "PURCHASE_ORDER" },
        { label: "采购入库", value: "PURCHASE_IN" },
        { label: "采购退货", value: "PURCHASE_RETURN" },
        { label: "销售订单", value: "SALES_ORDER" },
        { label: "销售出库", value: "SALES_OUT" },
        { label: "销售退货", value: "SALES_RETURN" },
        { label: "库存盘点", value: "STOCK_CHECK" },
        { label: "库存调拨", value: "STOCK_TRANSFER" }
      ]
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      pageApproveLog(this.queryParams).then(response => {
        this.logList = response.rows || []
        this.total = response.total || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNo = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleDetail(row) {
      getApproveLog(row.id).then(response => {
        this.detail = response.data || null
        this.open = true
      })
    },
    getBillTypeLabel(value) {
      const found = this.billTypeOptions.find(item => item.value === value)
      return found ? found.label : value
    }
  }
}
</script>
