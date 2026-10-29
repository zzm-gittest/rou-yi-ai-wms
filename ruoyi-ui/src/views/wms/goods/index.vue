<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch">
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
      <el-form-item label="商品分类" prop="categoryId">
        <el-select v-model="queryParams.categoryId" placeholder="请选择分类" clearable>
          <el-option
            v-for="item in categoryOptions"
            :key="item.id"
            :label="item.categoryName"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="品牌" prop="brand">
        <el-input
          v-model="queryParams.brand"
          placeholder="请输入品牌"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="商品状态" clearable>
          <el-option
            v-for="dict in dict.type.sys_normal_disable"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
        <el-button type="success" plain icon="el-icon-download" size="mini" :loading="exportLoading" :disabled="exportLoading" @click="handleExport" v-hasPermi="['wms:goods:query']">导出</el-button>
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
          v-hasPermi="['wms:goods:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          class="goods-action-btn goods-import-btn"
          type="warning"
          plain
          icon="el-icon-upload"
          size="mini"
          @click="handleImport"
          v-hasPermi="['wms:goods:import']"
        >导入</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-dropdown @command="handleDownloadTemplate" v-hasPermi="['wms:goods:importTemplate']">
          <el-button class="goods-action-btn goods-template-btn" type="info" plain icon="el-icon-download" size="mini">
            模板<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="simple">简易模板（无规格）</el-dropdown-item>
            <el-dropdown-item command="full">完整模板（有规格）</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="goodsList">
      <el-table-column label="商品编码" prop="goodsCode" min-width="120" />
      <el-table-column label="商品名称" prop="goodsName" :show-overflow-tooltip="true" />
      <el-table-column label="商品分类" prop="categoryName" min-width="120" />
      <el-table-column label="品牌" prop="brand" min-width="100" />
      <el-table-column label="产地" prop="place" min-width="100" :show-overflow-tooltip="true" />
      <el-table-column label="是否有规格" align="center" prop="hasSpec" min-width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.hasSpec === '1' ? 'success' : 'info'">
            {{ scope.row.hasSpec === '1' ? '有规格' : '无规格' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" min-width="80">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_normal_disable" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" min-width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['wms:goods:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['wms:goods:remove']"
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

    <!-- 导入商品对话框 -->
    <el-dialog :title="importTitle" :visible.sync="importOpen" width="600px" :close-on-click-modal="false">
      <el-form ref="importForm" :model="importForm" label-width="100px">
        <el-form-item label="Excel文件">
          <el-upload
            ref="upload"
            :limit="1"
            :auto-upload="false"
            :on-change="handleFileChange"
            :file-list="fileList"
            action="#"
            accept=".xlsx,.xls"
          >
            <el-button slot="trigger" size="small" type="primary">选取文件</el-button>
            <div slot="tip" class="el-upload__tip">只能上传xlsx或xls文件</div>
          </el-upload>
        </el-form-item>
        <el-form-item label="导入结果" v-if="importResult">
          <el-alert
            :title="getImportResultMessage()"
            :type="importResult.failCount > 0 ? 'warning' : 'success'"
            :closable="false"
          />
          <el-table :data="importResult.errors" border stripe style="margin-top: 10px" v-if="importResult.errors && importResult.errors.length > 0">
            <el-table-column prop="rowNumber" label="行号" width="80" />
            <el-table-column prop="errorMessage" label="错误信息" />
            <el-table-column prop="rawData" label="原始数据" show-overflow-tooltip />
          </el-table>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitImport" :loading="importLoading">导入</el-button>
        <el-button @click="importOpen = false">取消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { pageGoods, delGoods, importGoods, downloadTemplate, exportGoods } from "@/api/wms/goods"
import { listGoodsCategory } from "@/api/wms/goodsCategory"

export default {
  name: "Goods",
  dicts: ['sys_normal_disable'],
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 商品表格数据
      goodsList: [],
      // 分类选项
      categoryOptions: [],
      // 导入相关
      importOpen: false,
      importTitle: "导入商品",
      fileList: [],
      importForm: {},
      importLoading: false,
      importResult: null,
      // 导出相关
      exportLoading: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        goodsCode: null,
        goodsName: null,
        categoryId: null,
        brand: null,
        status: null,
        orderByColumn: 'createTime',
        isAsc: 'desc'
      }
    }
  },
  created() {
    this.getList()
    this.getCategoryList()
  },
  activated() {
    const time = this.$route.query.t
    if (time) {
      this.getList()
    }
  },
  methods: {
    /** 查询商品列表 */
    getList() {
      this.loading = true
      pageGoods(this.queryParams).then(response => {
        this.goodsList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    /** 查询分类列表 */
    getCategoryList() {
      listGoodsCategory().then(response => {
        this.categoryOptions = response.data
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
    /** 导出按钮操作 */
    handleExport() {
      this.$modal.confirm('是否确认导出商品数据？').then(() => {
        this.exportLoading = true
        return exportGoods(this.queryParams)
      }).then(response => {
        const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = '商品数据.xlsx'
        link.click()
        URL.revokeObjectURL(link.href)
        this.$modal.msgSuccess("导出成功")
        this.exportLoading = false
      }).catch((err) => {
        this.exportLoading = false
        if (err && err !== 'cancel') {
          this.$modal.msgError("导出失败")
        }
      })
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.$router.push({ path: '/wms/goods/detail/index' })
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.$router.push({
        path: '/wms/goods/detail/index',
        query: { id: row.id }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      this.$modal.confirm('是否确认删除商品"' + row.goodsName + '"？').then(function() {
        return delGoods(row.id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch((err) => {
        if (!err) {
          return
        }
        const msg = err.msg || (err.data && err.data.msg) || '删除失败'
        this.$modal.msgError(msg)
      })
    },
    /** 导入按钮操作 */
    handleImport() {
      this.importOpen = true
      this.fileList = []
      this.importResult = null
      this.$nextTick(() => {
        this.$refs.upload && this.$refs.upload.clearFiles()
      })
    },
    /** 下载模板按钮操作 */
    handleDownloadTemplate(templateType) {
      const fileName = templateType === 'full' ? '商品导入模板_完整版.xlsx' : '商品导入模板_简易版.xlsx'
      downloadTemplate(templateType).then(response => {
        const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = fileName
        link.click()
        URL.revokeObjectURL(link.href)
      }).catch(() => {
        this.$modal.msgError("下载模板失败")
      })
    },
    /** 文件选择处理 */
    handleFileChange(file) {
      this.fileList = file ? [file] : []
    },
    /** 提交导入 */
    submitImport() {
      if (this.fileList.length === 0) {
        this.$modal.msgWarning("请先选择文件")
        return
      }
      const file = this.fileList[0].raw
      this.importLoading = true
      importGoods(file).then(response => {
        this.importResult = response.data
        this.importLoading = false
        if (this.importResult.failCount > 0) {
          // 失败信息留在弹窗中，便于用户逐条查看修正
          this.$nextTick(() => {
            const dialogBody = this.$el.querySelector('.el-dialog__body')
            if (dialogBody) {
              dialogBody.scrollTop = dialogBody.scrollHeight
            }
          })
          return
        }

        if (this.importResult.successCount > 0) {
          this.$modal.msgSuccess("导入成功 " + this.importResult.successCount + " 条")
          this.getList()
          // 导入成功后立即关闭对话框
          this.importOpen = false
        } else {
          this.$modal.msgWarning("导入完成，但没有成功导入的数据")
        }
      }).catch(() => {
        this.importLoading = false
        this.$modal.msgError("导入失败")
      })
    },
    /** 获取导入结果消息 */
    getImportResultMessage() {
      if (!this.importResult) return ''
      const successMsg = '成功导入 ' + this.importResult.successCount + ' 条数据'
      const failMsg = this.importResult.failCount > 0 ? '，失败 ' + this.importResult.failCount + ' 条' : ''
      return successMsg + failMsg
    }
  }
}
</script>

<style scoped>
.goods-action-btn.el-button--warning.is-plain:hover,
.goods-action-btn.el-button--warning.is-plain:focus {
  color: #ffffff;
  background-color: #e6a23c;
  border-color: #e6a23c;
}

.goods-action-btn.el-button--info.is-plain:hover,
.goods-action-btn.el-button--info.is-plain:focus {
  color: #ffffff;
  background-color: #909399;
  border-color: #909399;
}
</style>
