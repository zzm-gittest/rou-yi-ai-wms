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

      <!-- 右侧：规格管理 -->
      <el-col :span="16" :xs="24">
        <div class="pad-container">
          <el-card shadow="never" v-if="currentGoods">
            <div slot="header" class="clearfix">
              <span>{{ currentGoods.goodsName }} - 规格管理</span>
              <el-button
                style="float: right"
                type="primary"
                icon="el-icon-plus"
                size="mini"
                @click="handleAddSpec"
                v-hasPermi="['wms:goodsSpec:add']"
              >新增规格</el-button>
            </div>

            <el-table v-loading="specLoading" :data="specList">
              <el-table-column label="规格名称" prop="specName" />
              <el-table-column label="排序" prop="sort" min-width="80" />
              <el-table-column label="规格值" min-width="300">
                <template slot-scope="scope">
                  <el-tag
                    v-for="value in scope.row.values"
                    :key="value.id"
                    size="mini"
                    style="margin-right: 5px"
                  >{{ value.specValue }}</el-tag>
                  <el-button
                    type="text"
                    size="mini"
                    icon="el-icon-plus"
                    @click="handleAddValue(scope.row)"
                    v-hasPermi="['wms:goodsSpec:edit']"
                  >添加值</el-button>
                </template>
              </el-table-column>
              <el-table-column label="操作" align="center" width="180">
                <template slot-scope="scope">
                  <el-button
                    size="mini"
                    type="text"
                    icon="el-icon-edit"
                    @click="handleEditSpec(scope.row)"
                    v-hasPermi="['wms:goodsSpec:edit']"
                  >修改</el-button>
                  <el-button
                    size="mini"
                    type="text"
                    icon="el-icon-delete"
                    @click="handleDeleteSpec(scope.row)"
                    v-hasPermi="['wms:goodsSpec:remove']"
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

    <!-- 规格对话框 -->
    <el-dialog :title="specTitle" :visible.sync="specOpen" width="600px" append-to-body>
      <el-form ref="specForm" :model="specForm" :rules="specRules" label-width="100px">
        <el-form-item label="规格名称" prop="specName">
          <el-input v-model="specForm.specName" placeholder="如：颜色、尺寸" maxlength="50" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="specForm.sort" :min="0" controls-position="right" style="width: 200px"/>
        </el-form-item>
        <el-form-item label="规格值" prop="values">
          <div v-for="(value, index) in specForm.values" :key="index" style="margin-bottom: 10px">
            <el-input v-model="value.specValue" placeholder="规格值" style="width: 200px" />
            <el-button
              type="text"
              icon="el-icon-delete"
              @click="specForm.values.splice(index, 1)"
              style="margin-left: 10px"
            >删除</el-button>
          </div>
          <el-button type="dashed" icon="el-icon-plus" @click="addSpecValue">添加规格值</el-button>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitSpec">确 定</el-button>
        <el-button @click="cancelSpec">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 规格值对话框 -->
    <el-dialog title="添加规格值" :visible.sync="valueOpen" width="400px" append-to-body>
      <el-form ref="valueForm" :model="valueForm" :rules="valueRules" label-width="100px">
        <el-form-item label="规格值" prop="specValue">
          <el-input v-model="valueForm.specValue" placeholder="如：红色、L" maxlength="50" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="valueForm.sort" :min="0" controls-position="right" style="width: 200px"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitValue">确 定</el-button>
        <el-button @click="valueOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listGoods } from "@/api/wms/goods"
import { listGoodsSpec, getGoodsSpec, addGoodsSpec, updateGoodsSpec, delGoodsSpec } from "@/api/wms/goodsSpec"

export default {
  name: "GoodsSpec",
  data() {
    return {
      // 商品列表
      loading: false,
      goodsList: [],
      currentGoods: null,

      // 规格列表
      specLoading: false,
      specList: [],

      // 规格对话框
      specOpen: false,
      specTitle: "",
      specForm: {
        id: null,
        goodsId: null,
        specName: null,
        sort: 0,
        values: []
      },
      specRules: {
        specName: [
          { required: true, message: "规格名称不能为空", trigger: "blur" }
        ]
      },

      // 规格值对话框
      valueOpen: false,
      currentSpec: null,
      valueForm: {
        specId: null,
        specValue: null,
        sort: 0
      },
      valueRules: {
        specValue: [
          { required: true, message: "规格值不能为空", trigger: "blur" }
        ]
      }
    }
  },
  created() {
    this.getGoodsList()
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

    /** 选择商品 */
    handleCurrentChange(row) {
      this.currentGoods = row
      if (row) {
        this.getSpecList()
      }
    },

    /** 查询规格列表 */
    getSpecList() {
      if (!this.currentGoods) return

      this.specLoading = true
      listGoodsSpec(this.currentGoods.id).then(response => {
        this.specList = response.data
        this.specLoading = false
      })
    },

    /** 新增规格 */
    handleAddSpec() {
      this.resetSpecForm()
      this.specForm.goodsId = this.currentGoods.id
      this.specOpen = true
      this.specTitle = "添加规格"
    },

    /** 修改规格 */
    handleEditSpec(row) {
      this.resetSpecForm()
      getGoodsSpec(row.id).then(response => {
        this.specForm = response.data
        this.specOpen = true
        this.specTitle = "修改规格"
      })
    },

    /** 删除规格 */
    handleDeleteSpec(row) {
      this.$modal.confirm('是否确认删除规格"' + row.specName + '"？').then(function() {
        return delGoodsSpec(row.id)
      }).then(() => {
        this.getSpecList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },

    /** 添加规格值 */
    handleAddValue(spec) {
      this.currentSpec = spec
      this.valueForm = {
        specId: spec.id,
        specValue: null,
        sort: 0
      }
      this.valueOpen = true
    },

    /** 提交规格 */
    submitSpec() {
      this.$refs["specForm"].validate(valid => {
        if (valid) {
          // 过滤空规格值
          const validValues = this.specForm.values.filter(v => v.specValue && v.specValue.trim() !== '')

          const data = {
            ...this.specForm,
            values: validValues
          }

          if (data.id != null) {
            updateGoodsSpec(data).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.specOpen = false
              this.getSpecList()
            })
          } else {
            addGoodsSpec(data).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.specOpen = false
              this.getSpecList()
            })
          }
        }
      })
    },

    /** 提交规格值 */
    submitValue() {
      this.$refs["valueForm"].validate(valid => {
        if (valid) {
          // 添加到当前规格的值列表
          const newValue = {
            specId: this.valueForm.specId,
            specValue: this.valueForm.specValue,
            sort: this.valueForm.sort
          }

          const spec = this.specList.find(s => s.id === this.currentSpec.id)
          if (spec) {
            if (!spec.values) {
              spec.values = []
            }
            spec.values.push(newValue)

            // 更新规格
            updateGoodsSpec(spec).then(response => {
              this.$modal.msgSuccess("添加成功")
              this.valueOpen = false
              this.getSpecList()
            })
          }
        }
      })
    },

    /** 添加规格值行 */
    addSpecValue() {
      this.specForm.values.push({
        specValue: null,
        sort: 0
      })
    },

    /** 取消规格对话框 */
    cancelSpec() {
      this.specOpen = false
      this.resetSpecForm()
    },

    /** 重置规格表单 */
    resetSpecForm() {
      this.specForm = {
        id: null,
        goodsId: null,
        specName: null,
        sort: 0,
        values: []
      }
      this.resetForm("specForm")
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
</style>
