<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="租户名称" prop="tenantName">
        <el-input
          v-model="queryParams.tenantName"
          placeholder="请输入租户名称"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="联系人" prop="contactUser">
        <el-input
          v-model="queryParams.contactUser"
          placeholder="请输入联系人"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable size="small">
          <el-option
            v-for="dict in statusOptions"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="small" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="small" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="small"
          @click="handleAdd"
          v-hasPermi="['system:tenant:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="small"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['system:tenant:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="small"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['system:tenant:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="small"
          @click="handleExport"
          v-hasPermi="['system:tenant:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar @queryTable="getList" :columns="columns"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="tenantList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="租户ID" align="center" prop="tenantId" width="80" />
      <el-table-column label="租户名称" align="center" prop="tenantName" :show-overflow-tooltip="true" />
      <el-table-column label="联系人" align="center" prop="contactUser" />
      <el-table-column label="联系电话" align="center" prop="contactPhone" />
      <el-table-column label="账号数量" align="center" prop="accountCount" width="80" />
      <el-table-column label="过期时间" align="center" prop="expireTime" width="120">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.expireTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="版本" align="center" prop="edition" width="80">
        <template slot-scope="scope">
          <el-tag :type="getEditionTagType(scope.row.edition)" size="small">
            {{ getEditionLabel(scope.row.edition) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="功能" align="center" width="180">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.aiEnabled === 1" type="success" size="mini" style="margin-right: 4px;">AI</el-tag>
          <el-tag v-if="scope.row.voiceEnabled === 1" type="success" size="mini" style="margin-right: 4px;">语音</el-tag>
          <el-tag v-if="scope.row.ocrEnabled === 1" type="success" size="mini" style="margin-right: 4px;">OCR</el-tag>
          <el-tag v-if="scope.row.scanEnabled === 1" type="success" size="mini">扫码</el-tag>
          <span v-if="!scope.row.aiEnabled && !scope.row.voiceEnabled && !scope.row.ocrEnabled && !scope.row.scanEnabled" style="color: #909399;">-</span>
        </template>
      </el-table-column>
      <el-table-column label="AI点数" align="center" width="100">
        <template slot-scope="scope">
          <span :style="{ color: (scope.row.aiPoints || 0) <= 10 ? '#F56C6C' : '#67C23A', fontWeight: 'bold' }">{{ scope.row.aiPoints || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'" size="small">
            {{ scope.row.status === '0' ? '正常' : '停用' }}
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
            v-hasPermi="['system:tenant:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['system:tenant:remove']"
          >删除</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-coin"
            @click="handleRecharge(scope.row)"
            v-hasPermi="['system:tenant:edit']"
          >充值</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改租户对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="租户名称" prop="tenantName">
          <el-input v-model="form.tenantName" placeholder="请输入租户名称" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactUser">
          <el-input v-model="form.contactUser" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="账号数量" prop="accountCount">
          <el-input-number v-model="form.accountCount" :min="1" :max="999" />
        </el-form-item>
        <el-form-item label="过期时间" prop="expireTime">
          <el-date-picker
            v-model="form.expireTime"
            type="datetime"
            placeholder="选择过期时间"
            value-format="yyyy-MM-dd HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in statusOptions"
              :key="dict.value"
              :label="dict.value"
            >{{ dict.label }}</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-divider content-position="left">版本配置</el-divider>

        <el-form-item label="版本类型" prop="edition">
          <el-select v-model="form.edition" placeholder="请选择版本类型" @change="handleEditionChange">
            <el-option label="免费版" value="free" />
            <el-option label="专业版" value="pro" />
            <el-option label="企业版" value="enterprise" />
          </el-select>
        </el-form-item>

        <el-form-item label="AI功能">
          <el-switch v-model="form.aiEnabled" :active-value="1" :inactive-value="0" />
          <span style="margin-left: 10px; color: #909399; font-size: 12px;">AI智能助手、智能查询</span>
        </el-form-item>

        <el-form-item label="语音功能">
          <el-switch v-model="form.voiceEnabled" :active-value="1" :inactive-value="0" />
          <span style="margin-left: 10px; color: #909399; font-size: 12px;">语音下单、语音识别</span>
        </el-form-item>

        <el-form-item label="OCR功能">
          <el-switch v-model="form.ocrEnabled" :active-value="1" :inactive-value="0" />
          <span style="margin-left: 10px; color: #909399; font-size: 12px;">图片识别、单据识别</span>
        </el-form-item>

        <el-form-item label="扫码功能">
          <el-switch v-model="form.scanEnabled" :active-value="1" :inactive-value="0" />
          <span style="margin-left: 10px; color: #909399; font-size: 12px;">条码扫描、二维码扫描</span>
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
    <!-- 充值AI点数对话框 -->
    <el-dialog title="充值AI点数" :visible.sync="rechargeOpen" width="500px" append-to-body>
      <div v-if="rechargeTarget" style="margin-bottom: 16px; padding: 12px; background: #f5f7fa; border-radius: 4px;">
        <span style="font-weight: bold;">{{ rechargeTarget.tenantName }}</span>
        <span style="margin-left: 12px; color: #909399;">当前余额：</span>
        <span style="color: #67C23A; font-weight: bold;">{{ rechargeTarget.aiPoints || 0 }} 点</span>
      </div>
      <el-form ref="rechargeForm" :model="rechargeForm" :rules="rechargeRules" label-width="80px">
        <el-form-item label="充值点数" prop="amount">
          <el-input-number v-model="rechargeForm.amount" :min="1" :max="100000" :step="100" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="快捷选择">
          <el-button size="small" @click="rechargeForm.amount = 100">100点</el-button>
          <el-button size="small" @click="rechargeForm.amount = 500">500点</el-button>
          <el-button size="small" @click="rechargeForm.amount = 1000">1000点</el-button>
          <el-button size="small" @click="rechargeForm.amount = 5000">5000点</el-button>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="rechargeForm.remark" placeholder="如：用户微信转账100元" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" :loading="rechargeLoading" @click="submitRecharge">确认充值</el-button>
        <el-button @click="rechargeOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 点数流水抽屉 -->
    <el-drawer title="AI点数流水" :visible.sync="pointsLogVisible" size="500px" append-to-body>
      <div v-if="rechargeTarget" style="padding: 0 20px 12px;">
        <span style="font-weight: bold;">{{ rechargeTarget.tenantName }}</span>
        <span style="margin-left: 12px;">余额：<b style="color: #67C23A;">{{ rechargeTarget.aiPoints || 0 }}</b> 点</span>
      </div>
      <el-table :data="pointsLogList" v-loading="pointsLogLoading" style="width: 100%;">
        <el-table-column label="时间" prop="createTime" width="160" />
        <el-table-column label="类型" width="80" align="center">
          <template slot-scope="scope">
            <el-tag :type="getLogTagType(scope.row.changeType)" size="mini">{{ getLogTypeLabel(scope.row.changeType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="变动" width="80" align="center">
          <template slot-scope="scope">
            <span :style="{ color: scope.row.changeAmount > 0 ? '#67C23A' : '#F56C6C', fontWeight: 'bold' }">
              {{ scope.row.changeAmount > 0 ? '+' : '' }}{{ scope.row.changeAmount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="余额" prop="balanceAfter" width="80" align="center" />
        <el-table-column label="备注" prop="remark" :show-overflow-tooltip="true" />
      </el-table>
    </el-drawer>
  </div>
</template>

<script>
import { listTenant, getTenant, delTenant, addTenant, updateTenant, rechargePoints, getPointsLog } from "@/api/system/tenant";

export default {
  name: "Tenant",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 租户表格数据
      tenantList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 状态数据
      statusOptions: [
        { value: "0", label: "正常" },
        { value: "1", label: "停用" }
      ],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        tenantName: undefined,
        contactUser: undefined,
        status: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        tenantName: [
          { required: true, message: "租户名称不能为空", trigger: "blur" }
        ],
        contactPhone: [
          { required: true, message: "联系电话不能为空", trigger: "blur" }
        ]
      },
      // 列显隐信息
      // 充值对话框
      rechargeOpen: false,
      rechargeLoading: false,
      rechargeTarget: null,
      rechargeForm: { amount: 100, remark: '' },
      rechargeRules: {
        amount: [{ required: true, message: '请输入充值点数', trigger: 'blur' }]
      },
      // 点数流水
      pointsLogVisible: false,
      pointsLogLoading: false,
      pointsLogList: [],
      columns: [
        { key: 0, label: `租户ID`, visible: true },
        { key: 1, label: `租户名称`, visible: true },
        { key: 2, label: `联系人`, visible: true },
        { key: 3, label: `联系电话`, visible: true },
        { key: 4, label: `账号数量`, visible: true },
        { key: 5, label: `过期时间`, visible: true },
        { key: 6, label: `版本`, visible: true },
        { key: 7, label: `功能`, visible: true },
        { key: 8, label: `AI点数`, visible: true },
        { key: 9, label: `状态`, visible: true }
      ]
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询租户列表 */
    getList() {
      this.loading = true;
      listTenant(this.queryParams).then(response => {
        this.tenantList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    /** 取消按钮 */
    cancel() {
      this.open = false;
      this.reset();
    },
    /** 表单重置 */
    reset() {
      this.form = {
        tenantId: undefined,
        tenantName: undefined,
        contactUser: undefined,
        contactPhone: undefined,
        accountCount: 10,
        expireTime: undefined,
        status: "0",
        remark: undefined,
        edition: "free",
        aiEnabled: 0,
        voiceEnabled: 0,
        ocrEnabled: 0,
        scanEnabled: 0
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 多选框选中数据 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.tenantId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加租户";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const tenantId = row.tenantId || this.ids;
      getTenant(tenantId).then(response => {
        this.form = response.data;
        // 确保版本字段有默认值
        if (!this.form.edition) {
          this.form.edition = "free";
        }
        if (this.form.aiEnabled === null || this.form.aiEnabled === undefined) {
          this.form.aiEnabled = 0;
        }
        if (this.form.voiceEnabled === null || this.form.voiceEnabled === undefined) {
          this.form.voiceEnabled = 0;
        }
        if (this.form.ocrEnabled === null || this.form.ocrEnabled === undefined) {
          this.form.ocrEnabled = 0;
        }
        if (this.form.scanEnabled === null || this.form.scanEnabled === undefined) {
          this.form.scanEnabled = 0;
        }
        this.open = true;
        this.title = "修改租户";
      });
    },
    /** 版本类型变更 */
    handleEditionChange(val) {
      if (val === "pro" || val === "enterprise") {
        // 专业版和企业版默认开启所有功能
        this.form.aiEnabled = 1;
        this.form.voiceEnabled = 1;
        this.form.ocrEnabled = 1;
        this.form.scanEnabled = 1;
      } else {
        // 免费版默认关闭所有功能
        this.form.aiEnabled = 0;
        this.form.voiceEnabled = 0;
        this.form.ocrEnabled = 0;
        this.form.scanEnabled = 0;
      }
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.tenantId != undefined) {
            updateTenant(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addTenant(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const tenantIds = row.tenantId || this.ids;
      this.$modal.confirm('是否确认删除租户编号为"' + tenantIds + '"的数据项？').then(function() {
        return delTenant(tenantIds);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download("system/tenant/export", {
        ...this.queryParams
      }, `tenant_${new Date().getTime()}.xlsx`);
    },
    /** 获取版本标签类型 */
    getEditionTagType(edition) {
      if (edition === "pro") return "success";
      if (edition === "enterprise") return "warning";
      return "info";
    },
    /** 获取版本标签文字 */
    getEditionLabel(edition) {
      if (edition === "pro") return "专业版";
      if (edition === "enterprise") return "企业版";
      if (edition === "basic") return "基础版";
      return "免费版";
    },
    /** 打开充值对话框 */
    handleRecharge(row) {
      this.rechargeTarget = row;
      this.rechargeForm = { amount: 100, remark: '' };
      this.rechargeOpen = true;
    },
    /** 提交充值 */
    submitRecharge() {
      this.$refs['rechargeForm'].validate(valid => {
        if (valid) {
          this.$modal.confirm('确认给「' + this.rechargeTarget.tenantName + '」充值 ' + this.rechargeForm.amount + ' 点？').then(() => {
            this.rechargeLoading = true;
            rechargePoints({
              tenantId: this.rechargeTarget.tenantId,
              amount: this.rechargeForm.amount,
              remark: this.rechargeForm.remark
            }).then(res => {
              this.$modal.msgSuccess('充值成功，当前余额：' + res.data.aiPoints + ' 点');
              this.rechargeOpen = false;
              this.getList();
            }).finally(() => {
              this.rechargeLoading = false;
            });
          }).catch(() => {});
        }
      });
    },
    /** 查看点数流水 */
    handlePointsLog(row) {
      this.rechargeTarget = row;
      this.pointsLogVisible = true;
      this.pointsLogLoading = true;
      getPointsLog(row.tenantId).then(res => {
        this.pointsLogList = res.data || [];
      }).finally(() => {
        this.pointsLogLoading = false;
      });
    },
    getLogTagType(type) {
      if (type === 'charge' || type === 'gift') return 'success';
      if (type === 'consume') return 'danger';
      if (type === 'refund') return 'warning';
      return 'info';
    },
    getLogTypeLabel(type) {
      const map = { charge: '充值', consume: '消耗', refund: '退还', gift: '赠送' };
      return map[type] || type;
    }
  }
};
</script>
