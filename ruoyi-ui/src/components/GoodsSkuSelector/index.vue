<template>
  <el-dialog
    title="选择商品"
    :visible.sync="visible"
    width="900px"
    append-to-body
    :before-close="handleClose"
  >
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true">
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
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table
      v-loading="loading"
      :data="skuList"
      @selection-change="handleSelectionChange"
      border
      height="400"
      ref="table"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="商品图片" align="center" width="80">
        <template slot-scope="scope">
          <el-image
            v-if="scope.row.image"
            :src="scope.row.image"
            :preview-src-list="[scope.row.image]"
            style="width: 40px; height: 40px"
            fit="cover"
            lazy
          >
            <div slot="error" class="image-slot">
              <i class="el-icon-picture-outline"></i>
            </div>
          </el-image>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="SKU编码" prop="skuCode" width="150" />
      <el-table-column label="商品名称" prop="goodsName" show-overflow-tooltip />
      <el-table-column label="SKU名称" prop="skuName" show-overflow-tooltip />
      <el-table-column label="规格" prop="specInfo" width="120">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.specInfo" size="mini">{{ scope.row.specInfo }}</el-tag>
          <span v-else class="text-muted">无规格</span>
        </template>
      </el-table-column>
      <el-table-column v-if="priceMode === 'both'" label="成本价" prop="costPrice" width="85" align="right">
        <template slot-scope="scope">
          <span>¥{{ formatAmount(scope.row.costPrice) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="采购价" prop="purchasePrice" width="85" align="right">
        <template slot-scope="scope">
          <span>¥{{ formatAmount(scope.row.purchasePrice) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="销售价" prop="salePrice" width="85" align="right">
        <template slot-scope="scope">
          <span>¥{{ formatAmount(scope.row.salePrice) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="批发价" prop="wholesalePrice" width="85" align="right">
        <template slot-scope="scope">
          <span style="color: #52c41a">¥{{ formatAmount(scope.row.wholesalePrice) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="库存" prop="stock" width="80" align="center" />
      <el-table-column label="单位" prop="unitName" width="60" align="center" />
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="handlePagination"
      :small="true"
    />

    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="handleConfirm" :disabled="selectedList.length === 0">
        确定 (已选 {{ selectedList.length }} 项)
      </el-button>
      <el-button @click="handleClose">取消</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { pageGoodsSku } from "@/api/wms/goodsSku"
import { listGoodsCategory } from "@/api/wms/goodsCategory"
import { pageStock } from "@/api/wms/stock"

export default {
  name: "GoodsSkuSelector",
  props: {
    warehouseId: {
      type: [Number, String],
      default: null
    },
    // 参考价格模式：both(原样)、purchase(采购价)、sale(销售价)
    priceMode: {
      type: String,
      default: "both",
      validator: value => ["both", "purchase", "sale"].includes(value)
    }
  },
  data() {
    return {
      visible: false,
      loading: false,
      skuList: [],
      selectedList: [],
      total: 0,
      categoryOptions: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        goodsName: null,
        skuCode: null,
        categoryId: null
      }
    }
  },
  methods: {
    /** 打开弹窗 */
    open() {
      this.visible = true
      this.getList()
      this.getCategoryList()
    },
    /** 查询SKU列表 */
    getList() {
      this.loading = true
      if (this.warehouseId) {
        const params = {
          pageNum: this.queryParams.pageNum,
          pageSize: this.queryParams.pageSize,
          warehouseId: this.warehouseId,
          goodsName: this.queryParams.goodsName,
          skuCode: this.queryParams.skuCode
        }
        pageStock(params).then(response => {
          this.skuList = (response.rows || []).map(row => ({
            id: row.skuId,
            skuId: row.skuId,
            skuCode: row.skuCode,
            goodsName: row.goodsName,
            skuName: row.skuName,
            specInfo: row.specInfo,
            costPrice: row.costPrice,
            purchasePrice: row.purchasePrice,
            salePrice: row.salePrice,
            wholesalePrice: row.wholesalePrice,
            stock: row.quantity,
            unitName: row.unitName,
            image: null
          })).filter(row => Number(row.stock) > 0)
          this.total = response.total || 0
          this.loading = false
        }).catch(() => {
          this.loading = false
        })
      } else {
        pageGoodsSku(this.queryParams).then(response => {
          this.skuList = response.rows || []
          this.total = response.total || 0
          this.loading = false
        }).catch(() => {
          this.loading = false
        })
      }
    },
    /** 查询分类列表 */
    getCategoryList() {
      listGoodsCategory().then(response => {
        this.categoryOptions = response.data || []
      })
    },
    /** 搜索 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 分页 */
    handlePagination({ page, limit }) {
      this.queryParams.pageNum = Number(page) || 1
      this.queryParams.pageSize = Number(limit) || 10
      this.getList()
    },
    /** 重置 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    /** 选择变化 */
    handleSelectionChange(selection) {
      this.selectedList = selection
    },
    /** 确认选择 */
    handleConfirm() {
      if (this.selectedList.length === 0) {
        this.$modal.msgWarning("请至少选择一个商品")
        return
      }
      this.$emit("confirm", this.selectedList)
      this.handleClose()
    },
    /** 关闭弹窗 */
    handleClose() {
      this.visible = false
      this.selectedList = []
      this.$refs.table && this.$refs.table.clearSelection()
    },
    /** 金额格式化，支持字段兜底 */
    formatAmount(primary, fallback) {
      const value = primary != null ? primary : fallback
      const amount = Number(value)
      return Number.isFinite(amount) ? amount.toFixed(2) : "0.00"
    }
  }
}
</script>

<style scoped>
.text-muted {
  color: #909399;
}
.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 40px;
  height: 40px;
  background: #f5f7fa;
  color: #909399;
  font-size: 20px;
}
</style>
