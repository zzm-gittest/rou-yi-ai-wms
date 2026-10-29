<template>
  <div class="app-container">
    <el-card shadow="never">
      <div slot="header" class="card-header">
        <span class="title">{{ form.id ? '修改商品' : '新增商品' }}</span>
        <el-button icon="el-icon-back" size="small" @click="handleBack">返回</el-button>
      </div>

      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品编码" prop="goodsCode">
              <el-input v-model="form.goodsCode" placeholder="留空则自动生成" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品名称" prop="goodsName">
              <el-input v-model="form.goodsName" placeholder="请输入商品名称" maxlength="100" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="商品分类" prop="categoryId">
              <div class="inline-select-with-action">
                <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                  <el-option
                    v-for="item in categoryOptions"
                    :key="item.id"
                    :label="item.categoryName"
                    :value="item.id"
                  />
                </el-select>
                <el-button type="primary" plain icon="el-icon-plus" @click="openQuickCategoryDialog">新增分类</el-button>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="品牌" prop="brand">
              <el-input v-model="form.brand" placeholder="请输入品牌" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产地" prop="place">
              <el-input v-model="form.place" placeholder="请输入产地" maxlength="50" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="是否有规格" prop="hasSpec">
              <el-radio-group v-model="form.hasSpec" :disabled="isEditMode">
                <el-radio label="0">无规格</el-radio>
                <el-radio label="1">有规格</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio
                  v-for="dict in dict.type.sys_normal_disable"
                  :key="dict.value"
                  :label="dict.value"
                >{{dict.label}}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
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

        <el-divider content-position="left">SKU 明细</el-divider>
        <div class="spec-toolbar" v-if="form.hasSpec === '1'">
          <div class="spec-tags">
            <el-tag
              v-for="spec in allSpecs"
              :key="spec.id"
              size="mini"
              type="info"
              style="margin-right: 6px; margin-bottom: 6px"
            >
              {{ spec.specName }}：{{ (spec.values || []).map(v => v.specValue).join(' / ') }}
            </el-tag>
            <span v-if="!allSpecs.length" class="spec-empty">暂无规格，请先新增规格</span>
          </div>
          <div class="spec-actions" v-if="!isEditMode">
            <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="openSpecDialog">新增规格</el-button>
            <el-button type="success" plain icon="el-icon-refresh" size="mini" @click="regenerateSkuList">按规格生成SKU</el-button>
          </div>
        </div>
        <el-alert
          :title="form.hasSpec === '1' ? (isEditMode ? '修改页已锁定规格，仅可编辑现有SKU字段' : '新增页可维护规格并按组合生成SKU') : '无规格商品固定为 1 条 SKU 明细'"
          type="info"
          :closable="false"
          style="margin-bottom: 12px"
        />
        <div class="sku-batch-bar" v-if="form.skuList && form.skuList.length">
          <span class="sku-batch-label">批量设置单位</span>
          <el-select v-model="batchUnitId" placeholder="请选择单位" clearable size="small" class="sku-batch-select">
            <el-option
              v-for="item in unitOptions"
              :key="item.id"
              :label="item.unitName"
              :value="item.id"
            />
          </el-select>
          <el-button type="primary" plain size="small" @click="applyBatchUnitToSku">应用到全部SKU</el-button>
        </div>
        <el-table :data="form.skuList" border :fit="false" class="sku-detail-table" style="width: 100%">
          <el-table-column v-if="form.hasSpec === '1'" label="规格组合" width="180" show-overflow-tooltip>
            <template slot-scope="scope">
              <span>{{ formatSkuSpecDisplay(scope.row) || '未设置规格' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="SKU名称" width="260" show-overflow-tooltip>
            <template slot-scope="scope">
              <div class="sku-name-cell">
                <el-input v-model="scope.row.skuName" placeholder="请输入SKU名称" maxlength="100" size="small" />
                <el-button
                  v-if="!isEditMode && form.hasSpec === '1' && !scope.row.id"
                  type="text"
                  class="sku-inline-delete"
                  @click="removeUnsavedSkuRow(scope.$index)"
                >删除</el-button>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="SKU编码" width="160" show-overflow-tooltip>
            <template slot-scope="scope">
              <el-input v-model="scope.row.skuCode" placeholder="留空自动生成" maxlength="50" size="small" />
            </template>
          </el-table-column>
          <el-table-column width="120">
            <template slot="header">
              <span>单位</span>
              <el-button
                type="primary"
                circle
                icon="el-icon-plus"
                class="unit-header-add"
                @click="openQuickUnitDialog"
              />
            </template>
            <template slot-scope="scope">
              <el-select v-model="scope.row.unitId" placeholder="请选择" clearable size="small" style="width: 100%">
                <el-option
                  v-for="item in unitOptions"
                  :key="item.id"
                  :label="item.unitName"
                  :value="item.id"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="条码" width="140" show-overflow-tooltip>
            <template slot-scope="scope">
              <el-input v-model="scope.row.barcode" type="text" placeholder="选填" maxlength="80" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="采购价" width="140">
            <template slot-scope="scope">
              <el-input-number v-model="scope.row.purchasePrice" :min="0" :precision="2" controls-position="right" size="small" style="width: 100%" />
            </template>
          </el-table-column>
          <el-table-column label="销售价" width="140">
            <template slot-scope="scope">
              <el-input-number v-model="scope.row.salePrice" :min="0" :precision="2" controls-position="right" size="small" style="width: 100%" />
            </template>
          </el-table-column>
          <el-table-column label="成本价" width="140">
            <template slot-scope="scope">
              <el-input-number v-model="scope.row.costPrice" :min="0" :precision="2" controls-position="right" size="small" style="width: 100%" />
            </template>
          </el-table-column>
          <el-table-column label="批发价" width="140">
            <template slot-scope="scope">
              <el-input-number v-model="scope.row.wholesalePrice" :min="0" :precision="2" controls-position="right" size="small" style="width: 100%" />
            </template>
          </el-table-column>
          <el-table-column label="最小库存" width="120">
            <template slot-scope="scope">
              <el-input-number v-model="scope.row.minStock" :min="0" controls-position="right" size="small" style="width: 100%" />
            </template>
          </el-table-column>
          <el-table-column label="最大库存" width="120">
            <template slot-scope="scope">
              <el-input-number v-model="scope.row.maxStock" :min="0" controls-position="right" size="small" style="width: 100%" />
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template slot-scope="scope">
              <el-select v-model="scope.row.status" placeholder="状态" style="width: 100%">
                <el-option label="正常" value="0" />
                <el-option label="停用" value="1" />
              </el-select>
            </template>
          </el-table-column>
        </el-table>

        <el-row :gutter="20" class="image-section-row">
          <el-col :span="24">
            <el-form-item label="商品主图" prop="image">
              <image-upload v-model="form.image" :limit="5" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="商品详情" prop="detail">
              <editor v-model="form.detail" :min-height="400" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24" style="text-align: center; margin-top: 20px">
            <el-button type="primary" icon="el-icon-check" @click="submitForm" :loading="submitLoading">保存</el-button>
            <el-button icon="el-icon-close" @click="handleBack">取消</el-button>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-dialog title="快速新增商品分类" :visible.sync="quickCategoryOpen" width="520px" append-to-body>
      <el-form ref="quickCategoryForm" :model="quickCategoryForm" :rules="quickCategoryRules" label-width="90px">
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="quickCategoryForm.categoryName" placeholder="请输入分类名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="quickCategoryForm.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" :loading="quickCategoryLoading" @click="submitQuickCategory">确 定</el-button>
        <el-button @click="quickCategoryOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="快速新增计量单位" :visible.sync="quickUnitOpen" width="480px" append-to-body>
      <el-form ref="quickUnitForm" :model="quickUnitForm" :rules="quickUnitRules" label-width="90px">
        <el-form-item label="单位名称" prop="unitName">
          <el-input v-model="quickUnitForm.unitName" placeholder="请输入单位名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="quickUnitForm.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" :loading="quickUnitLoading" @click="submitQuickUnit">确 定</el-button>
        <el-button @click="quickUnitOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="新增规格" :visible.sync="specOpen" width="520px" append-to-body>
      <el-form ref="specForm" :model="specForm" :rules="specRules" label-width="90px">
        <el-form-item label="规格名称" prop="specName">
          <el-input v-model="specForm.specName" placeholder="如：颜色、尺寸" maxlength="50" />
        </el-form-item>
        <el-form-item label="规格值" prop="values">
          <div v-for="(value, index) in specForm.values" :key="index" class="spec-value-row">
            <el-input v-model="value.specValue" placeholder="请输入规格值，如：红色、L" maxlength="50" />
            <el-button type="text" icon="el-icon-delete" @click="removeSpecValue(index)">删除</el-button>
          </div>
          <el-button type="primary" plain size="mini" icon="el-icon-plus" @click="addSpecValue">添加规格值</el-button>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" :loading="specSubmitLoading" @click="submitSpec">确 定</el-button>
        <el-button @click="specOpen = false">取 消</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { getGoodsDetail, addGoods, updateGoods } from "@/api/wms/goods"
import { listGoodsCategory, addGoodsCategory } from "@/api/wms/goodsCategory"
import { listUnit, addUnit } from "@/api/wms/unit"
import { listGoodsSpec, addGoodsSpec } from "@/api/wms/goodsSpec"

export default {
  name: "GoodsDetail",
  dicts: ['sys_normal_disable'],
  data() {
    const validateSpecValues = (rule, values, callback) => {
      const hasValue = (values || []).some(item => item && item.specValue && item.specValue.trim())
      if (!hasValue) {
        callback(new Error("请至少添加一个规格值"))
        return
      }
      callback()
    }

    return {
      submitLoading: false,
      categoryOptions: [],
      unitOptions: [],
      batchUnitId: null,
      quickCategoryOpen: false,
      quickCategoryLoading: false,
      quickUnitOpen: false,
      quickUnitLoading: false,
      quickCategoryForm: {
        parentId: 0,
        categoryName: null,
        orderNum: 0,
        status: "0",
        image: null,
        remark: null
      },
      quickUnitForm: {
        unitName: null,
        status: "0",
        remark: null
      },
      specOpen: false,
      specSubmitLoading: false,
      specForm: {
        specName: null,
        values: [{ specValue: null, sort: 0 }]
      },
      specRules: {
        specName: [
          { required: true, message: "规格名称不能为空", trigger: "blur" }
        ],
        values: [
          { validator: validateSpecValues, trigger: "change" }
        ]
      },
      remoteSpecs: [],
      localSpecs: [],
      form: {},
      rules: {
        goodsName: [
          { required: true, message: "商品名称不能为空", trigger: "blur" }
        ],
        categoryId: [
          { required: true, message: "请选择商品分类", trigger: "change" }
        ]
      },
      quickCategoryRules: {
        categoryName: [
          { required: true, message: "分类名称不能为空", trigger: "blur" }
        ]
      },
      quickUnitRules: {
        unitName: [
          { required: true, message: "单位名称不能为空", trigger: "blur" }
        ]
      }
    }
  },
  computed: {
    allSpecs() {
      return [...(this.remoteSpecs || []), ...(this.localSpecs || [])]
    },
    isEditMode() {
      return !!this.form.id
    }
  },
  watch: {
    'form.hasSpec'(value) {
      this.handleHasSpecChange(value)
    }
  },
  created() {
    this.getCategoryList()
    this.getUnitList()
    const id = this.$route.query.id
    if (id) {
      this.getDetail(id)
    } else {
      this.reset()
    }
  },
  methods: {
    /** 获取商品详情 */
    getDetail(id) {
      getGoodsDetail(id).then(response => {
        const data = response.data || {}
        this.form = {
          ...this.form,
          ...data,
          skuList: (data.skuList || []).map(item => this.normalizeSkuRow(item))
        }
        if (!this.form.skuList || this.form.skuList.length === 0) {
          this.form.skuList = [this.createEmptySkuRow()]
        }
        this.loadSpecs()
      })
    },
    loadSpecs() {
      if (!this.form.id) {
        this.remoteSpecs = []
        return Promise.resolve()
      }
      return listGoodsSpec(this.form.id).then(response => {
        this.remoteSpecs = response.data || []
      })
    },
    /** 查询分类列表 */
    getCategoryList() {
      return listGoodsCategory().then(response => {
        this.categoryOptions = response.data
      })
    },
    /** 查询单位列表 */
    getUnitList() {
      return listUnit().then(response => {
        this.unitOptions = response.data
      })
    },
    /** 打开快速新增分类 */
    openQuickCategoryDialog() {
      this.quickCategoryForm = {
        parentId: 0,
        categoryName: null,
        orderNum: 0,
        status: "0",
        image: null,
        remark: null
      }
      this.quickCategoryOpen = true
      this.$nextTick(() => this.resetForm("quickCategoryForm"))
    },
    /** 打开快速新增单位 */
    openQuickUnitDialog() {
      this.quickUnitForm = {
        unitName: null,
        status: "0",
        remark: null
      }
      this.quickUnitOpen = true
      this.$nextTick(() => this.resetForm("quickUnitForm"))
    },
    /** 提交快速新增分类 */
    submitQuickCategory() {
      this.$refs["quickCategoryForm"].validate(valid => {
        if (!valid) {
          return
        }
        this.quickCategoryLoading = true
        addGoodsCategory(this.quickCategoryForm).then(() => {
          this.$modal.msgSuccess("分类新增成功")
          this.quickCategoryOpen = false
          return this.getCategoryList().then(() => {
            // 新增后按名称回填到当前表单
            const target = this.categoryOptions.find(item => item.categoryName === this.quickCategoryForm.categoryName)
            if (target) {
              this.form.categoryId = target.id
            }
          })
        }).finally(() => {
          this.quickCategoryLoading = false
        })
      })
    },
    /** 提交快速新增单位 */
    submitQuickUnit() {
      this.$refs["quickUnitForm"].validate(valid => {
        if (!valid) {
          return
        }
        this.quickUnitLoading = true
        addUnit(this.quickUnitForm).then(() => {
          this.$modal.msgSuccess("单位新增成功")
          this.quickUnitOpen = false
          return this.getUnitList().then(() => {
            const target = this.unitOptions.find(item => item.unitName === this.quickUnitForm.unitName)
            if (target) {
              this.batchUnitId = target.id
            }
          })
        }).finally(() => {
          this.quickUnitLoading = false
        })
      })
    },
    /** 表单重置 */
    reset() {
      this.form = {
        id: null,
        goodsCode: null,
        goodsName: null,
        categoryId: null,
        unitId: null,
        brand: null,
        place: null,
        image: null,
        detail: null,
        hasSpec: "0",
        minStock: 0,
        maxStock: 999999,
        status: "0",
        remark: null,
        skuList: [this.createEmptySkuRow()]
      }
      this.remoteSpecs = []
      this.localSpecs = []
      this.handleHasSpecChange(this.form.hasSpec)
    },
    openSpecDialog() {
      if (this.isEditMode) {
        this.$modal.msgWarning("修改页面不允许新增或修改规格")
        return
      }
      this.specForm = {
        specName: null,
        values: [{ specValue: null, sort: 0 }]
      }
      this.specOpen = true
      this.$nextTick(() => this.resetForm("specForm"))
    },
    addSpecValue() {
      this.specForm.values.push({
        specValue: null,
        sort: this.specForm.values.length
      })
    },
    removeSpecValue(index) {
      this.specForm.values.splice(index, 1)
      if (!this.specForm.values.length) {
        this.specForm.values.push({ specValue: null, sort: 0 })
      }
    },
    submitSpec() {
      if (this.isEditMode) {
        this.$modal.msgWarning("修改页面不允许新增或修改规格")
        return
      }
      this.$refs["specForm"].validate(valid => {
        if (!valid) {
          return
        }
        const values = (this.specForm.values || [])
          .map(item => (item && item.specValue ? item.specValue.trim() : ""))
          .filter(item => !!item)
          .map((value, index) => ({
            specValue: value,
            sort: index
          }))

        if (!values.length) {
          this.$modal.msgError("请至少输入一个规格值")
          return
        }

        this.specSubmitLoading = true
        if (this.form.id) {
          addGoodsSpec({
            goodsId: this.form.id,
            specName: this.specForm.specName,
            sort: 0,
            values
          }).then(() => {
            this.$modal.msgSuccess("新增规格成功")
            this.specOpen = false
            return this.loadSpecs().then(() => {
              this.rebuildSkuListBySpecs({ preserveExisting: true })
            })
          }).finally(() => {
            this.specSubmitLoading = false
          })
        } else {
          const now = Date.now()
          const localSpec = {
            id: `tmp_spec_${now}`,
            specName: this.specForm.specName,
            values: values.map((value, index) => ({
              id: `tmp_val_${now}_${index}`,
              specValue: value.specValue,
              sort: value.sort
            }))
          }
          this.localSpecs.push(localSpec)
          this.specOpen = false
          this.specSubmitLoading = false
          this.rebuildSkuListBySpecs({ preserveExisting: true })
          this.$modal.msgSuccess("规格已添加，可直接用于SKU选择")
        }
      })
    },
    handleHasSpecChange(value) {
      if (value === '1') {
        if (!this.allSpecs.length && (!this.form.skuList || !this.form.skuList.length)) {
          this.form.skuList = []
        }
      } else {
        this.ensureSingleNoSpecSku()
      }
    },
    ensureSingleNoSpecSku() {
      const firstRow = (this.form.skuList && this.form.skuList.length)
        ? this.normalizeSkuRow(this.form.skuList[0])
        : this.createEmptySkuRow()
      firstRow.specInfo = null
      firstRow.specIds = null
      this.form.skuList = [firstRow]
    },
    regenerateSkuList() {
      if (this.isEditMode) {
        this.$modal.msgWarning("修改页面不允许调整规格组合")
        return
      }
      this.rebuildSkuListBySpecs({ preserveExisting: true, showMessage: true })
    },
    parseSpecInfo(row) {
      if (!row || !row.specInfo) {
        return null
      }
      try {
        return typeof row.specInfo === 'string' ? JSON.parse(row.specInfo) : row.specInfo
      } catch (error) {
        return null
      }
    },
    buildSpecKey(specInfoObj) {
      if (!specInfoObj) {
        return ''
      }
      return Object.keys(specInfoObj)
        .sort()
        .map(key => `${key}:${specInfoObj[key]}`)
        .join('|')
    },
    buildSkuDefaultName(specInfoObj) {
      const goodsName = (this.form.goodsName || '').trim()
      if (!specInfoObj) {
        return goodsName || null
      }
      const specText = Object.entries(specInfoObj).map(([key, value]) => `${key}:${value}`).join(' ')
      return `${goodsName} ${specText}`.trim() || null
    },
    normalizeSpecsForCombination(specs) {
      return (specs || [])
        .filter(spec => spec && spec.specName && (spec.values || []).length)
        .map(spec => ({
          ...spec,
          values: (spec.values || []).slice().sort((a, b) => {
            const sortA = Number(a.sort || 0)
            const sortB = Number(b.sort || 0)
            if (sortA !== sortB) {
              return sortA - sortB
            }
            return String(a.id || '').localeCompare(String(b.id || ''))
          })
        }))
        .sort((a, b) => {
          const sortA = Number(a.sort || 0)
          const sortB = Number(b.sort || 0)
          if (sortA !== sortB) {
            return sortA - sortB
          }
          return String(a.id || '').localeCompare(String(b.id || ''))
        })
    },
    buildSpecCombinations(specs) {
      const validSpecs = this.normalizeSpecsForCombination(specs)
      if (!validSpecs.length) {
        return []
      }

      let combinations = [{}]
      validSpecs.forEach(spec => {
        const nextList = []
        ;(spec.values || []).forEach(value => {
          combinations.forEach(base => {
            nextList.push({
              ...base,
              [spec.specName]: {
                specId: spec.id,
                valueId: value.id,
                valueText: value.specValue
              }
            })
          })
        })
        combinations = nextList
      })

      return combinations.map(item => {
        const specInfoObj = {}
        const specIds = []
        Object.keys(item).forEach(specName => {
          const value = item[specName]
          specInfoObj[specName] = value.valueText
          if (!String(value.valueId).startsWith('tmp_')) {
            specIds.push(value.valueId)
          }
        })
        return {
          specInfoObj,
          specInfo: JSON.stringify(specInfoObj),
          specIds: specIds.length ? specIds.join(',') : null,
          key: this.buildSpecKey(specInfoObj)
        }
      })
    },
    rebuildSkuListBySpecs(options = {}) {
      const { preserveExisting = true, showMessage = false } = options
      if (this.form.hasSpec !== '1') {
        this.ensureSingleNoSpecSku()
        return
      }

      const combinations = this.buildSpecCombinations(this.allSpecs)
      if (!combinations.length) {
        this.form.skuList = []
        if (showMessage) {
          this.$modal.msgWarning('请先添加至少一个规格和值，再生成SKU')
        }
        return
      }

      const existedMap = {}
      if (preserveExisting) {
        ;(this.form.skuList || []).forEach(item => {
          const row = this.normalizeSkuRow(item)
          const specInfoObj = this.parseSpecInfo(row)
          const key = this.buildSpecKey(specInfoObj)
          if (key) {
            existedMap[key] = row
          }
        })
      }

      this.form.skuList = combinations.map(combo => {
        const existed = existedMap[combo.key]
        const row = existed ? this.normalizeSkuRow(existed) : this.createEmptySkuRow()
        row.specInfo = combo.specInfo
        row.specIds = combo.specIds
        if (!row.skuName || !row.skuName.trim()) {
          row.skuName = this.buildSkuDefaultName(combo.specInfoObj)
        }
        return row
      })

      if (showMessage) {
        this.$modal.msgSuccess(`已生成 ${this.form.skuList.length} 条SKU明细`)
      }
    },
    formatSkuSpecDisplay(row) {
      const specObj = this.parseSpecInfo(row)
      if (!specObj) {
        return ''
      }
      return Object.entries(specObj).map(([k, v]) => `${k}:${v}`).join(' / ')
    },
    createEmptySkuRow() {
      return {
        id: null,
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
    },
    normalizeSkuRow(row) {
      const normalized = {
        ...this.createEmptySkuRow(),
        ...row
      }
      if (normalized.barcode !== null && normalized.barcode !== undefined && normalized.barcode !== '') {
        normalized.barcode = String(normalized.barcode)
      } else {
        normalized.barcode = null
      }
      return normalized
    },
    removeUnsavedSkuRow(index) {
      if (this.isEditMode) {
        return
      }
      const row = this.form.skuList[index]
      if (row && row.id) {
        this.$modal.msgWarning("已入库SKU不允许在此删除")
        return
      }
      this.$modal.confirm('确认删除这条SKU明细吗？').then(() => {
        this.form.skuList.splice(index, 1)
        if (!this.form.skuList.length && this.form.hasSpec !== '1') {
          this.form.skuList.push(this.createEmptySkuRow())
        }
      }).catch(() => {})
    },
    applyBatchUnitToSku() {
      if (!this.form.skuList || !this.form.skuList.length) {
        return
      }
      if (!this.batchUnitId) {
        this.$modal.msgWarning("请先选择要批量设置的单位")
        return
      }
      this.form.skuList = (this.form.skuList || []).map(item => {
        const row = this.normalizeSkuRow(item)
        row.unitId = this.batchUnitId
        return row
      })
      this.$modal.msgSuccess(`已批量设置 ${this.form.skuList.length} 条SKU单位`)
    },
    buildValidSkuList() {
      return (this.form.skuList || [])
        .map(item => this.normalizeSkuRow(item))
        .map(item => {
          const row = { ...item }
          if (!row.skuName || !row.skuName.trim()) {
            row.skuName = this.buildSkuDefaultName(this.parseSpecInfo(row))
          }
          if (row.barcode !== null && row.barcode !== undefined && row.barcode !== '') {
            row.barcode = String(row.barcode).trim()
          } else {
            row.barcode = null
          }
          return row
        })
        .filter(item => item.skuName && item.skuName.trim())
        .map(item => {
          if (item.specIds && String(item.specIds).includes('tmp_')) {
            item.specIds = null
          }
          return item
        })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.hasSpec === '1' && !this.form.skuList.length) {
            this.$modal.msgError("当前商品已启用规格，请先添加规格并生成SKU明细")
            return
          }

          if (this.form.hasSpec === '1' && !this.isEditMode) {
            const expectedList = this.buildSpecCombinations(this.allSpecs)
            if (!expectedList.length) {
              this.$modal.msgError("当前商品已启用规格，请先添加规格和值")
              return
            }
            const currentSpecKeyList = (this.form.skuList || [])
              .map(item => this.buildSpecKey(this.parseSpecInfo(item)))
              .filter(item => !!item)
            const expectedKeyList = expectedList.map(item => item.key)
            const matchAll = expectedKeyList.every(key => currentSpecKeyList.includes(key))
            if (!matchAll || currentSpecKeyList.length !== expectedKeyList.length) {
              this.$modal.msgWarning("规格组合已变化，请先点击“按规格生成SKU”后再保存，避免覆盖已录入数据")
              return
            }
          }

          const validSkuList = this.buildValidSkuList()
          if (!validSkuList.length) {
            this.$modal.msgError("请至少保留1条SKU明细")
            return
          }

          const payload = {
            ...this.form,
            syncSkuList: true,
            skuList: validSkuList
          }

          this.submitLoading = true
          if (this.form.id != null) {
            updateGoods(payload).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.closeTab()
            }).finally(() => {
              this.submitLoading = false
            })
          } else {
            addGoods(payload).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.closeTab()
            }).finally(() => {
              this.submitLoading = false
            })
          }
        }
      })
    },
    /** 关闭当前标签页并返回列表 */
    closeTab() {
      this.$store.dispatch('tagsView/delView', this.$route).then(() => {
        this.$router.push({ path: '/wms/goods', query: { t: Date.now() } })
      })
    },
    /** 返回列表 */
    handleBack() {
      this.closeTab()
    }
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header .title {
  font-size: 16px;
  font-weight: bold;
}

.inline-select-with-action {
  display: flex;
  gap: 8px;
  align-items: center;
}

.spec-toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 12px;
}

.spec-tags {
  flex: 1;
}

.spec-actions {
  flex-shrink: 0;
  margin-left: 12px;
}

.spec-value-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.sku-batch-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.sku-batch-label {
  color: #606266;
  font-size: 13px;
}

.sku-batch-select {
  width: 200px;
}

.sku-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.sku-name-cell .el-input {
  flex: 1;
  min-width: 0;
}

.sku-inline-delete {
  flex-shrink: 0;
}

.image-section-row {
  margin-top: 16px;
}

.unit-header-add {
  margin-left: 8px;
  width: 26px;
  height: 26px;
  line-height: 26px;
  padding: 0;
  font-size: 16px;
}

.sku-detail-table ::v-deep .el-input-number {
  width: 100%;
}
</style>
