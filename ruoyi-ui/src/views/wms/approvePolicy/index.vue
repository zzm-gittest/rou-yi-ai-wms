<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item v-if="isAdmin" label="租户" prop="tenantId">
        <el-select
          v-model="queryParams.tenantId"
          placeholder="请选择租户"
          clearable
          filterable
          style="width: 220px"
        >
          <el-option
            v-for="item in tenantOptions"
            :key="item.tenantId"
            :label="item.tenantName"
            :value="item.tenantId"
          />
        </el-select>
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
      <el-form-item label="审核要求" prop="needApprove">
        <el-select v-model="queryParams.needApprove" placeholder="请选择审核要求" clearable>
          <el-option label="需要审核" :value="1" />
          <el-option label="免审核" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="启用" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col v-if="isAdmin" :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['wms:approvePolicy:edit']"
        >新增策略</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="policyList">
      <el-table-column v-if="isAdmin" label="租户" prop="tenantId" width="180">
        <template slot-scope="scope">
          {{ getTenantName(scope.row.tenantId) }}
        </template>
      </el-table-column>
      <el-table-column label="单据类型" prop="billType" min-width="160">
        <template slot-scope="scope">
          <el-tag size="mini" type="info">{{ getBillTypeLabel(scope.row.billType) }}</el-tag>
          <span style="margin-left: 8px">{{ scope.row.billType }}</span>
        </template>
      </el-table-column>
      <el-table-column label="审核要求" prop="needApprove" width="110" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.needApprove === 1 ? 'warning' : 'success'" size="mini">
            {{ scope.row.needApprove === 1 ? '需要审核' : '免审核' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="免审自动打印" prop="autoPrint" width="130" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.needApprove === 0" :type="scope.row.autoPrint === 1 ? 'success' : 'info'" size="mini">
            {{ scope.row.autoPrint === 1 ? '自动打印' : '不打印' }}
          </el-tag>
          <span v-else style="color: #c0c4cc; font-size: 12px">—</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="90" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'" size="mini">
            {{ scope.row.status === '0' ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" show-overflow-tooltip />
      <el-table-column label="更新人" prop="updateBy" width="110" />
      <el-table-column label="更新时间" prop="updateTime" width="170">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="120">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['wms:approvePolicy:edit']"
          >配置</el-button>
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

    <el-dialog :title="title" :visible.sync="open" width="640px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col v-if="isAdmin" :span="12">
            <el-form-item label="租户" prop="tenantId">
              <el-select
                v-model="form.tenantId"
                placeholder="请选择租户"
                filterable
                style="width: 100%"
              >
                <el-option
                  v-for="item in tenantOptions"
                  :key="item.tenantId"
                  :label="item.tenantName"
                  :value="item.tenantId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="isAdmin ? 12 : 24">
            <el-form-item label="单据类型" prop="billType">
              <el-select v-model="form.billType" placeholder="请选择单据类型" style="width: 100%">
                <el-option
                  v-for="item in billTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="审核要求" prop="needApprove">
              <el-radio-group v-model="form.needApprove" @change="onNeedApproveChange">
                <el-radio :label="1">需要审核</el-radio>
                <el-radio :label="0">免审核</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio label="0">启用</el-radio>
                <el-radio label="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="自动打印小票">
              <el-radio-group v-model="form.autoPrint" :disabled="form.needApprove !== 0">
                <el-radio :label="1">是（免审通过后自动触发蓝牙打印）</el-radio>
                <el-radio :label="0">否</el-radio>
              </el-radio-group>
              <div style="color:#909399;font-size:12px;line-height:1.4;margin-top:4px">仅在选择「免审核」时生效，自动审核通过后自动触发蓝牙小票打印。</div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" :rows="3" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { pageApprovePolicy, getApprovePolicy, updateApprovePolicy } from "@/api/wms/approvePolicy"
import { listEnableTenant } from "@/api/system/tenant"
import store from "@/store"

export default {
  name: "ApprovePolicy",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      policyList: [],
      tenantOptions: [],
      title: "",
      open: false,
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        tenantId: null,
        billType: null,
        needApprove: null,
        status: null
      },
      form: {},
      rules: {
        tenantId: [
          { required: true, message: "请选择租户", trigger: "change" }
        ],
        billType: [
          { required: true, message: "单据类型不能为空", trigger: "change" }
        ],
        needApprove: [
          { required: true, message: "审核要求不能为空", trigger: "change" }
        ],
        status: [
          { required: true, message: "状态不能为空", trigger: "change" }
        ]
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
  computed: {
    /** 当前用户是否为超级管理员(userId === 1) */
    isAdmin() {
      return store.getters && store.getters.id === 1
    }
  },
  created() {
    if (this.isAdmin) {
      this.loadTenantOptions()
    }
    this.getList()
  },
  methods: {
    /** 加载租户下拉列表 */
    loadTenantOptions() {
      listEnableTenant().then(res => {
        this.tenantOptions = res.data || []
      })
    },
    getList() {
      this.loading = true
      pageApprovePolicy(this.queryParams).then(response => {
        this.policyList = response.rows || []
        this.total = response.total || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    reset() {
      this.form = {
        id: null,
        tenantId: null,
        billType: null,
        needApprove: 0,
        autoPrint: 0,
        status: "0",
        remark: null
      }
      this.resetForm("form")
    },
    handleQuery() {
      this.queryParams.pageNo = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "新增审核策略"
    },
    handleUpdate(row) {
      this.reset()
      getApprovePolicy(row.id).then(response => {
        this.form = response.data || {}
        this.open = true
        this.title = "配置审核策略"
      })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return
        }
        updateApprovePolicy(this.form).then(() => {
          this.$modal.msgSuccess("保存成功")
          this.open = false
          this.getList()
        })
      })
    },
    onNeedApproveChange(val) {
      // 切换为需要审核时，自动打印重置为否
      if (val === 1) {
        this.form.autoPrint = 0
      }
    },
    cancel() {
      this.open = false
      this.reset()
    },
    getBillTypeLabel(value) {
      const found = this.billTypeOptions.find(item => item.value === value)
      return found ? found.label : value
    },
    /** 根据租户ID获取租户名称 */
    getTenantName(tenantId) {
      const found = this.tenantOptions.find(item => item.tenantId === tenantId)
      return found ? found.tenantName + ' (ID: ' + tenantId + ')' : 'ID: ' + tenantId
    }
  }
}
</script>
