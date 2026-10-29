<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" label-width="90px">
      <el-form-item label="业务模块" prop="bizModule">
        <el-select v-model="queryParams.bizModule" placeholder="全部" clearable style="width: 160px">
          <el-option v-for="item in bizModuleOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作类型" prop="bizType">
        <el-select v-model="queryParams.bizType" placeholder="全部" clearable style="width: 140px">
          <el-option label="新增" value="CREATE" />
          <el-option label="修改" value="UPDATE" />
          <el-option label="删除" value="DELETE" />
          <el-option label="审核" value="APPROVE" />
          <el-option label="反审核" value="CANCEL_APPROVE" />
          <el-option label="导出" value="EXPORT" />
          <el-option label="导入" value="IMPORT" />
          <el-option label="其他" value="OTHER" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作人" prop="operUserName">
        <el-input v-model="queryParams.operUserName" placeholder="操作人用户名" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item label="业务单号" prop="bizNo">
        <el-input v-model="queryParams.bizNo" placeholder="业务单号" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item label="操作时间">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
          style="width: 240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 日志列表 -->
    <el-table v-loading="loading" :data="logList" border style="width: 100%">
      <el-table-column type="index" label="序号" width="55" align="center" />
      <el-table-column prop="bizModule" label="业务模块" width="120" align="center" />
      <el-table-column prop="bizType" label="操作类型" width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="bizTypeTag(scope.row.bizType)" size="small">{{ bizTypeText(scope.row.bizType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operUserName" label="操作人" width="120" align="center" />
      <el-table-column prop="operUrl" label="请求URL" min-width="200" show-overflow-tooltip />
      <el-table-column prop="operMethod" label="方法" width="80" align="center" />
      <el-table-column prop="status" label="状态" width="80" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'" size="small">
            {{ scope.row.status === 0 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="costTime" label="耗时(ms)" width="100" align="center" />
      <el-table-column prop="operIp" label="操作IP" width="140" align="center" />
      <el-table-column prop="createTime" label="操作时间" width="168" align="center" />
      <el-table-column label="操作" width="100" align="center" fixed="right">
        <template slot-scope="scope">
          <el-button type="text" size="small" @click="handleDetail(scope.row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNo"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 详情弹窗 -->
    <el-dialog title="操作日志详情" :visible.sync="detailOpen" width="720px" append-to-body>
      <el-descriptions :column="2" border v-if="detailRow">
        <el-descriptions-item label="业务模块">{{ detailRow.bizModule }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ bizTypeText(detailRow.bizType) }}</el-descriptions-item>
        <el-descriptions-item label="业务表">{{ detailRow.bizTable }}</el-descriptions-item>
        <el-descriptions-item label="业务单号">{{ detailRow.bizNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detailRow.operUserName }}</el-descriptions-item>
        <el-descriptions-item label="操作IP">{{ detailRow.operIp }}</el-descriptions-item>
        <el-descriptions-item label="请求URL" :span="2">{{ detailRow.operUrl }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ detailRow.operMethod }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detailRow.costTime }} ms</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailRow.status === 0 ? 'success' : 'danger'" size="small">
            {{ detailRow.status === 0 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ detailRow.createTime }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <div style="max-height: 200px; overflow: auto; word-break: break-all">{{ detailRow.operParam || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="返回结果" :span="2">
          <div style="max-height: 200px; overflow: auto; word-break: break-all">{{ detailRow.operResult || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="detailRow.status === 1">
          <div style="color: #f56c6c">{{ detailRow.errorMsg || '-' }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { pageBizOperLog } from '@/api/wms/wmsBizLog'

export default {
  name: 'WmsBizLog',
  data() {
    return {
      loading: false,
      logList: [],
      total: 0,
      dateRange: [],
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        bizModule: undefined,
        bizType: undefined,
        operUserName: undefined,
        bizNo: undefined
      },
      detailOpen: false,
      detailRow: null,
      bizModuleOptions: [
        '采购订单', '采购入库', '采购退货',
        '销售订单', '销售出库', '销售退货',
        '调拨单', '盘点单',
        '仓库管理', '商品管理', 'SKU管理', '商品分类',
        '供应商管理', '客户管理', '单位管理',
        '账户管理', '费用管理', '费用类型',
        '数据导入', '审核策略', 'PC端AI助手',
        '应收账款', '应付账款'
      ],
      bizTypeMap: {
        'CREATE': '新增',
        'UPDATE': '修改',
        'DELETE': '删除',
        'APPROVE': '审核',
        'CANCEL_APPROVE': '反审核',
        'EXPORT': '导出',
        'IMPORT': '导入',
        'OTHER': '其他'
      },
      bizTypeTagMap: {
        'CREATE': 'success',
        'UPDATE': 'warning',
        'DELETE': 'danger',
        'APPROVE': '',
        'CANCEL_APPROVE': 'info',
        'EXPORT': 'info',
        'IMPORT': 'info',
        'OTHER': 'info'
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      const params = { ...this.queryParams }
      if (this.dateRange && this.dateRange.length === 2) {
        params.beginTime = this.dateRange[0]
        params.endTime = this.dateRange[1]
      }
      pageBizOperLog(params).then(res => {
        this.logList = res.rows || []
        this.total = res.total || 0
      }).finally(() => {
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNo = 1
      this.getList()
    },
    resetQuery() {
      this.$refs.queryForm.resetFields()
      this.dateRange = []
      this.queryParams.pageNo = 1
      this.getList()
    },
    handleDetail(row) {
      this.detailRow = row
      this.detailOpen = true
    },
    bizTypeText(type) {
      return this.bizTypeMap[type] || type
    },
    bizTypeTag(type) {
      return this.bizTypeTagMap[type] || 'info'
    }
  }
}
</script>
