<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 左侧：商品列表 -->
      <el-col :span="8" :xs="24">
        <div class="pad-container">
          <el-card shadow="never">
            <div slot="header" class="clearfix">
              <span>商品列表</span>
            </div>
            <el-table
              v-loading="loading"
              :data="goodsList"
              highlight-current-row
              @current-change="handleCurrentChange"
              style="cursor: pointer"
            >
              <el-table-column label="商品编码" prop="goodsCode" width="100" />
              <el-table-column label="商品名称" prop="goodsName" :show-overflow-tooltip="true" />
              <el-table-column label="规格" align="center" width="80">
                <template slot-scope="scope">
                  <el-tag :type="scope.row.hasSpec === '1' ? 'success' : 'info'" size="mini">
                    {{ scope.row.hasSpec === '1' ? '有' : '无' }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </el-col>

      <!-- 右侧：SKU管理 -->
      <el-col :span="16" :xs="24">
        <div class="pad-container">
          <el-card shadow="never" v-if="currentGoods">
            <div slot="header" class="clearfix">
              <span>{{ currentGoods.goodsName }} - SKU管理</span>
              <el-button
                style="float: right"
                type="primary"
                icon="el-icon-plus"
                size="mini"
                @click="handleAdd"
                v-hasPermi="['wms:goodsSku:add']"
              >新增SKU</el-button>
            </div>

            <!-- 商品规格展示 -->
            <div class="spec-info-box" v-if="specList.length > 0">
              <div class="spec-title">商品规格：</div>
              <div class="spec-list">
                <el-tag
                  v-for="spec in specList"
                  :key="spec.id"
                  type="info"
                  size="small"
                  style="margin-right: 10px; margin-bottom: 5px"
                >
                  {{ spec.specName }}:
                  <template v-for="value in spec.values">
                    {{ value.specValue }}
                  </template>
                </el-tag>
              </div>
            </div>

            <el-table v-loading="skuLoading" :data="skuList">
              <el-table-column label="图片" align="center" min-width="80">
                <template slot-scope="scope">
                  <el-image
                    v-if="scope.row.image"
                    :src="scope.row.image"
                    :preview-src-list="[scope.row.image]"
                    style="width: 40px; height: 40px"
                    fit="cover"
                  />
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column label="SKU编码" prop="skuCode" min-width="140" />
              <el-table-column label="SKU名称" prop="skuName" :show-overflow-tooltip="true" />
              <el-table-column label="规格组合" min-width="200">
                <template slot-scope="scope">
                  <span v-if="scope.row.specDisplay">{{ scope.row.specDisplay }}</span>
                  <span v-else class="text-muted">无规格</span>
                </template>
              </el-table-column>
              <el-table-column label="单位" prop="unitName" min-width="80" />
              <el-table-column label="销售价" prop="salePrice" min-width="100">
                <template slot-scope="scope">
                  <span v-if="scope.row.salePrice">{{ scope.row.salePrice.toFixed(2) }}</span>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column label="状态" align="center" min-width="80">
                <template slot-scope="scope">
                  <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'" size="mini">
                    {{ scope.row.status === '0' ? '正常' : '停用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" align="center" width="180">
                <template slot-scope="scope">
                  <el-button
                    size="mini"
                    type="text"
                    icon="el-icon-edit"
                    @click="handleUpdate(scope.row)"
                    v-hasPermi="['wms:goodsSku:edit']"
                  >修改</el-button>
                  <el-button
                    size="mini"
                    type="text"
                    icon="el-icon-delete"
                    @click="handleDelete(scope.row)"
                    v-hasPermi="['wms:goodsSku:remove']"
                  >删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>

          <el-card shadow="never" v-else>
            <div class="empty-text">请选择商品</div>
          </el-card>
        </div>
      </el-col>
    </el-row>

    <!-- 添加或修改SKU对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="700px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-alert
          v-if="specList.length === 0"
          title="该商品暂无规格，请先在规格管理中添加规格"
          type="warning"
          :closable="false"
          style="margin-bottom: 15px"
        />

        <!-- 规格选择区域 -->
        <div v-if="specList.length > 0" class="spec-selector">
          <div class="form-item-title">选择规格组合</div>
          <div v-for="spec in specList" :key="spec.id" class="spec-group">
            <div class="spec-name">{{ spec.specName }}：</div>
            <el-radio-group v-model="selectedSpecValues[spec.id]" size="small">
              <el-radio
                v-for="value in spec.values"
                :key="value.id"
                :label="value.id"
                border
              >{{ value.specValue }}</el-radio>
            </el-radio-group>
          </div>
        </div>

        <el-divider></el-divider>

        <el-row>
          <el-col :span="12">
            <el-form-item label="SKU编码" prop="skuCode">
              <el-input v-model="form.skuCode" placeholder="留空则自动生成" maxlength="50">
                <template slot="append">
                  <el-button @click="generateSkuCode" icon="el-icon-refresh">自动生成</el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="SKU名称" prop="skuName">
              <el-input v-model="form.skuName" placeholder="请输入SKU名称" maxlength="100" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="条形码" prop="barcode">
              <el-input v-model="form.barcode" placeholder="请输入条形码" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位" prop="unitId">
              <el-select v-model="form.unitId" placeholder="请选择单位" clearable style="width: 100%">
                <el-option
                  v-for="item in unitOptions"
                  :key="item.id"
                  :label="item.unitName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="采购价" prop="purchasePrice">
              <el-input-number v-model="form.purchasePrice" :min="0" :precision="2" controls-position="right" style="width: 100%"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="销售价" prop="salePrice">
              <el-input-number v-model="form.salePrice" :min="0" :precision="2" controls-position="right" style="width: 100%"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="批发价" prop="wholesalePrice">
              <el-input-number v-model="form.wholesalePrice" :min="0" :precision="2" controls-position="right" style="width: 100%"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成本价" prop="costPrice">
              <el-input-number v-model="form.costPrice" :min="0" :precision="2" controls-position="right" style="width: 100%"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="最小库存" prop="minStock">
              <el-input-number v-model="form.minStock" :min="0" controls-position="right" style="width: 100%"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大库存" prop="maxStock">
              <el-input-number v-model="form.maxStock" :min="0" controls-position="right" style="width: 100%"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="SKU图片" prop="image">
              <image-upload v-model="form.image" :limit="1" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio label="0">正常</el-radio>
                <el-radio label="1">停用</el-radio>
              </el-radio-group>
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
import { listGoods } from "@/api/wms/goods"
import { listGoodsSku, getGoodsSku, addGoodsSku, updateGoodsSku, delGoodsSku } from "@/api/wms/goodsSku"
import { listGoodsSpec } from "@/api/wms/goodsSpec"
import { listUnit } from "@/api/wms/unit"

export default {
  name: "GoodsSku",
  data() {
    return {
      // 商品列表
      loading: false,
      goodsList: [],
      currentGoods: null,

      // SKU列表
      skuLoading: false,
      skuList: [],

      // 规格列表
      specList: [],
      selectedSpecValues: {},

      // 单位选项
      unitOptions: [],

      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,

      // 表单参数
      form: {},
      // 表单校验
      rules: {
        skuName: [
          { required: true, message: "SKU名称不能为空", trigger: "blur" }
        ]
      }
    }
  },
  created() {
    this.getGoodsList()
    this.getUnitList()
  },
  mounted() {
    // 从URL参数获取商品ID并自动选中
    const goodsId = this.$route.query.goodsId
    if (goodsId) {
      // 等待商品列表加载完成
      this.$watch('goodsList', (newVal) => {
        if (newVal && newVal.length > 0) {
          const goods = newVal.find(g => g.id == goodsId)
          if (goods) {
            this.currentGoods = goods
            this.getSkuList()
            this.getSpecList()
          }
        }
      }, { immediate: true })
    }
  },
  methods: {
    /** 查询商品列表 */
    getGoodsList() {
      this.loading = true
      listGoods().then(response => {
        this.goodsList = response.data
        this.loading = false
      })
    },

    /** 查询单位列表 */
    getUnitList() {
      listUnit().then(response => {
        this.unitOptions = response.data
      })
    },

    /** 查询规格列表 */
    getSpecList() {
      if (!this.currentGoods) return

      listGoodsSpec(this.currentGoods.id).then(response => {
        this.specList = response.data || []
      })
    },

    /** 选择商品 */
    handleCurrentChange(row) {
      this.currentGoods = row
      if (row) {
        this.getSkuList()
        this.getSpecList()
      }
    },

    /** 查询SKU列表 */
    getSkuList() {
      if (!this.currentGoods) return

      this.skuLoading = true
      listGoodsSku(this.currentGoods.id).then(response => {
        const skuList = response.data || []
        // 处理规格显示
        skuList.forEach(sku => {
          sku.specDisplay = this.formatSpecDisplay(sku.specInfo)
        })
        this.skuList = skuList
        this.skuLoading = false
      })
    },

    /** 格式化规格显示 */
    formatSpecDisplay(specInfo) {
      if (!specInfo) return ''
      try {
        const spec = JSON.parse(specInfo)
        return Object.entries(spec).map(([key, value]) => `${key}:${value}`).join(' / ')
      } catch (e) {
        return specInfo
      }
    },

    /** 生成规格信息字符串 */
    generateSpecInfo() {
      const specInfo = {}
      const specIds = []

      this.specList.forEach(spec => {
        const valueId = this.selectedSpecValues[spec.id]
        if (valueId) {
          const value = spec.values.find(v => v.id === valueId)
          if (value) {
            specInfo[spec.specName] = value.specValue
            specIds.push(valueId)
          }
        }
      })

      return {
        specInfo: JSON.stringify(specInfo),
        specIds: specIds.join(',')
      }
    },

    /** 生成SKU编码 */
    generateSkuCode() {
      if (!this.currentGoods) return

      // 基础编码：商品编码
      let skuCode = this.currentGoods.goodsCode || ''

      // 添加规格值后缀
      const { specInfo } = this.generateSpecInfo()
      if (specInfo) {
        try {
          const spec = JSON.parse(specInfo)
          const suffix = Object.values(spec).join('')
          skuCode += '-' + suffix
        } catch (e) {
          // ignore
        }
      }

      // 检查是否已存在，如果存在则添加序号
      let finalCode = skuCode
      let index = 1
      while (this.skuList.some(s => s.skuCode === finalCode)) {
        finalCode = skuCode + index
        index++
      }

      this.form.skuCode = finalCode

      // 自动生成SKU名称
      if (specInfo) {
        try {
          const spec = JSON.parse(specInfo)
          const specStr = Object.values(spec).join(' ')
          this.form.skuName = this.currentGoods.goodsName + ' ' + specStr
        } catch (e) {
          this.form.skuName = this.currentGoods.goodsName
        }
      } else {
        this.form.skuName = this.currentGoods.goodsName
      }
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
        goodsId: this.currentGoods ? this.currentGoods.id : null,
        skuCode: null,
        skuName: null,
        barcode: null,
        unitId: null,
        specInfo: null,
        specIds: null,
        purchasePrice: null,
        salePrice: null,
        wholesalePrice: null,
        costPrice: null,
        minStock: 0,
        maxStock: 999999,
        image: null,
        status: "0"
      }
      this.selectedSpecValues = {}
      this.resetForm("form")
    },

    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      // 重置规格选择
      this.specList.forEach(spec => {
        if (spec.values && spec.values.length > 0) {
          this.$set(this.selectedSpecValues, spec.id, null)
        }
      })
      this.open = true
      this.title = "添加SKU"
    },

    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      getGoodsSku(row.id).then(response => {
        this.form = response.data

        // 解析规格信息并回显
        if (this.form.specInfo) {
          try {
            const specInfo = JSON.parse(this.form.specInfo)
            this.specList.forEach(spec => {
              const value = spec.values.find(v => v.specValue === specInfo[spec.specName])
              if (value) {
                this.$set(this.selectedSpecValues, spec.id, value.id)
              }
            })
          } catch (e) {
            // ignore
          }
        }

        this.open = true
        this.title = "修改SKU"
      })
    },

    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          // 生成规格信息
          const { specInfo, specIds } = this.generateSpecInfo()
          this.form.specInfo = specInfo
          this.form.specIds = specIds

          if (this.form.id != null) {
            updateGoodsSku(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getSkuList()
            })
          } else {
            addGoodsSku(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getSkuList()
            })
          }
        }
      })
    },

    /** 删除按钮操作 */
    handleDelete(row) {
      this.$modal.confirm('是否确认删除SKU"' + row.skuName + '"？').then(function() {
        return delGoodsSku(row.id)
      }).then(() => {
        this.getSkuList()
        this.$modal.msgSuccess("删除成功")
      }).catch((err) => {
        if (!err) {
          return
        }
        const msg = err.msg || (err.data && err.data.msg) || '删除失败'
        this.$modal.msgError(msg)
      })
    }
  }
}
</script>

<style scoped>
.pad-container {
  padding: 10px;
}

.empty-text {
  text-align: center;
  padding: 50px 0;
  color: #909399;
  font-size: 14px;
}

.spec-info-box {
  background-color: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  margin-bottom: 15px;
}

.spec-title {
  font-weight: bold;
  margin-bottom: 8px;
  color: #333;
}

.spec-list {
  display: flex;
  flex-wrap: wrap;
}

.spec-selector {
  margin-bottom: 20px;
}

.form-item-title {
  font-weight: bold;
  margin-bottom: 12px;
  color: #333;
}

.spec-group {
  margin-bottom: 15px;
}

.spec-name {
  font-weight: 500;
  margin-bottom: 8px;
  color: #606266;
}

.text-muted {
  color: #909399;
}
</style>
