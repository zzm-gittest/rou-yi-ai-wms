<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
      <el-form-item label="费用单号" prop="expenseNo">
        <el-input
          v-model="queryParams.expenseNo"
          placeholder="请输入费用单号"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="费用类型" prop="expenseTypeId">
        <el-select v-model="queryParams.expenseTypeId" placeholder="请选择费用类型" clearable>
          <el-option
            v-for="item in expenseTypeOptions"
            :key="item.id"
            :label="item.typeName"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="审核状态" prop="approveStatus">
        <el-select v-model="queryParams.approveStatus" placeholder="审核状态" clearable>
          <el-option label="待审核" value="0" />
          <el-option label="已审核" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="费用日期">
        <el-date-picker
          v-model="dateRange"
          style="display: flex"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['wms:expense:add']"
        >新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="expenseList">
      <el-table-column label="费用单号" prop="expenseNo" min-width="150" />
      <el-table-column label="费用类型" prop="expenseTypeName" min-width="120" />
      <el-table-column label="费用日期" prop="expenseDate" min-width="110">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.expenseDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="费用金额" prop="amount" min-width="120" align="right">
        <template slot-scope="scope">
          <span>{{ scope.row.amount ? scope.row.amount.toFixed(2) : '0.00' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="付款账户" prop="accountName" min-width="120" />
      <el-table-column label="经办人" prop="handler" min-width="100" />
      <el-table-column label="审核状态" prop="approveStatus" min-width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.approveStatus === '1' ? 'success' : 'warning'">
            {{ scope.row.approveStatus === '1' ? '已审核' : '待审核' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-if="scope.row.approveStatus === '0'"
            v-hasPermi="['wms:expense:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-check"
            @click="handleApprove(scope.row)"
            v-if="showApproveAction && scope.row.approveStatus === '0'"
            v-hasPermi="['wms:expense:approve']"
          >审核</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-if="scope.row.approveStatus === '0'"
            v-hasPermi="['wms:expense:remove']"
          >删除</el-button>
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

    <!-- 添加或修改费用对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="费用类型" prop="expenseTypeId">
              <el-select v-model="form.expenseTypeId" placeholder="请选择费用类型">
                <el-option
                  v-for="item in expenseTypeOptions"
                  :key="item.id"
                  :label="item.typeName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="费用日期" prop="expenseDate">
              <el-date-picker
                v-model="form.expenseDate"
                type="date"
                placeholder="选择日期"
                value-format="yyyy-MM-dd"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="费用金额" prop="amount">
              <el-input-number v-model="form.amount" :min="0" :precision="2" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="付款账户" prop="accountId">
              <el-select v-model="form.accountId" placeholder="请选择付款账户">
                <el-option
                  v-for="item in accountOptions"
                  :key="item.id"
                  :label="item.accountName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="经办人" prop="handler">
              <el-input v-model="form.handler" placeholder="请输入经办人" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
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
import { pageExpense, getExpense, delExpense, addExpense, updateExpense, approveExpense } from "@/api/wms/expense"
import { listExpenseType } from "@/api/wms/expenseType"
import { listAccount } from "@/api/wms/account"
import { getBillNeedApprove } from "@/api/wms/approvePolicy"

export default {
  name: "Expense",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 是否显示审核相关按钮
      showApproveAction: true,
      // 费用表格数据
      expenseList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 日期范围
      dateRange: [],
      // 费用类型选项
      expenseTypeOptions: [],
      // 账户选项
      accountOptions: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        expenseNo: null,
        expenseTypeId: null,
        approveStatus: null,
        startDate: null,
        endDate: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        expenseTypeId: [
          { required: true, message: "费用类型不能为空", trigger: "change" }
        ],
        expenseDate: [
          { required: true, message: "费用日期不能为空", trigger: "change" }
        ],
        amount: [
          { required: true, message: "费用金额不能为空", trigger: "blur" }
        ],
        accountId: [
          { required: true, message: "付款账户不能为空", trigger: "change" }
        ]
      }
    }
  },
  created() {
    this.getList()
    this.getExpenseTypeList()
    this.getAccountList()
    this.loadApproveAction()
  },
  methods: {
    /** 查询费用列表 */
    getList() {
      this.loading = true
      if (this.dateRange && this.dateRange.length === 2) {
        this.queryParams.startDate = this.dateRange[0]
        this.queryParams.endDate = this.dateRange[1]
      }
      pageExpense(this.queryParams).then(response => {
        this.expenseList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 查询费用类型列表 */
    getExpenseTypeList() {
      listExpenseType().then(response => {
        this.expenseTypeOptions = response.data
      })
    },
    /** 查询账户列表 */
    getAccountList() {
      listAccount().then(response => {
        this.accountOptions = response.data
      })
    },
    loadApproveAction() {
      getBillNeedApprove("EXPENSE").then(response => {
        this.showApproveAction = response.data !== false
      }).catch(() => {
        this.showApproveAction = true
      })
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        expenseTypeId: null,
        expenseDate: null,
        amount: null,
        accountId: null,
        handler: null,
        remark: null
      }
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = []
      this.resetForm("queryForm")
      this.handleQuery()
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加费用"
      this.form.expenseDate = new Date().toISOString().split('T')[0]
      this.form.handler = this.$store.getters.name
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id
      getExpense(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改费用"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateExpense(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addExpense(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      this.$modal.confirm('是否确认删除费用单"' + row.expenseNo + '"？').then(function() {
        return delExpense(row.id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 审核按钮操作 */
    handleApprove(row) {
      this.$modal.confirm('是否确认审核费用单"' + row.expenseNo + '"？').then(function() {
        return approveExpense(row.id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("审核成功")
      }).catch(() => {})
    }
  }
}
</script>
