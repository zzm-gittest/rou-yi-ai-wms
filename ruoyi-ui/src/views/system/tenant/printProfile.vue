<template>
  <div class="app-container tenant-print-profile">
    <el-card shadow="never">
      <div slot="header" class="clearfix">
        <span>企业设置</span>
      </div>

      <el-alert
        title="仅维护当前登录租户的企业资料与AI配置，不可切换或修改其他租户信息。"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      />

      <el-form ref="form" :model="form" :rules="rules" label-width="130px" v-loading="loading">
        <el-divider content-position="left">打印资料</el-divider>

        <el-form-item label="公司名称" prop="printCompanyName">
          <el-input v-model="form.printCompanyName" maxlength="100" placeholder="请输入打印抬头公司名称" />
        </el-form-item>

        <el-form-item label="公司地址" prop="printCompanyAddress">
          <el-input
            v-model="form.printCompanyAddress"
            type="textarea"
            :rows="3"
            maxlength="200"
            placeholder="请输入打印抬头公司地址"
          />
        </el-form-item>

        <el-form-item label="联系电话" prop="printCompanyPhone">
          <el-input v-model="form.printCompanyPhone" maxlength="50" placeholder="请输入打印抬头联系电话" />
        </el-form-item>

        <el-form-item label="Logo上传">
          <image-upload
            v-model="form.printLogoPath"
            :limit="1"
            :fileType="['png', 'jpg', 'jpeg', 'svg']"
          />
        </el-form-item>

        <el-form-item label="Logo路径" prop="printLogoPath">
          <el-input v-model="form.printLogoPath" placeholder="可直接填写图片路径，也可通过上方上传生成" />
        </el-form-item>

        <el-divider content-position="left">PDF 打印设置</el-divider>

        <el-form-item label="字体缩放倍数" prop="printFontScale">
          <el-input-number
            v-model="form.printFontScale"
            :min="0.5"
            :max="5"
            :step="0.1"
            :precision="1"
            :controls="true"
            style="width: 200px"
          />
          <span style="margin-left: 10px; color: #909399; font-size: 12px;">
            0.5=缩小一半，1=原始大小，2=放大2倍（移动端PDF打印字体大小控制）
          </span>
        </el-form-item>

        <el-form-item label="纸张大小" prop="printPageSize">
          <el-select v-model="form.printPageSize" placeholder="请选择纸张大小" style="width: 300px">
            <el-option label="A4（坚排 210×297mm）" value="A4" />
            <el-option label="A4（横排 297×210mm）" value="A4_LANDSCAPE" />
            <el-option label="三联等份（241×279mm）" value="SLIP_241X279" />
            <el-option label="三联半页（241×140mm）" value="SLIP_241X140" />
          </el-select>
          <span style="margin-left: 10px; color: #909399; font-size: 12px;">
            三联单打印请选择对应纸张规格，否则内容可能显示不饱满
          </span>
        </el-form-item>

        <el-divider content-position="left">AI 配置（可选）</el-divider>

        <el-alert
          title="如未配置，系统将自动使用全局默认的 AI 模型与密钥。"
          type="warning"
          :closable="false"
          show-icon
          style="margin-bottom: 12px"
        />

        <el-alert
          title="注意：语音识别功能目前仅支持阿里云 DashScope 的 API Key，若 LLM 使用其他厂商模型（如 DeepSeek），语音识别仍需配置有效的 DashScope Key。"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 20px"
        />

        <el-form-item label="AI API Key" prop="aiApiKey">
          <el-input
            v-model="form.aiApiKey"
            type="password"
            show-password
            maxlength="255"
            placeholder="请输入 DashScope / 兼容 OpenAI 的 API Key"
          />
        </el-form-item>

        <el-form-item label="AI API 地址" prop="aiApiUrl">
          <el-input v-model="form.aiApiUrl" maxlength="255" placeholder="例如：https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions" />
        </el-form-item>

        <el-form-item label="AI 模型名称" prop="aiModel">
          <el-input v-model="form.aiModel" maxlength="100" placeholder="例如：qwen3.6-flash、qwen-turbo" />
        </el-form-item>

        <el-form-item label="视觉模型名称" prop="aiVisionModel">
          <el-input v-model="form.aiVisionModel" maxlength="100" placeholder="例如：qwen-vl-plus、qwen3.6-plus-2026-04-02" />
        </el-form-item>

        <el-form-item label="语音模型名称" prop="aiVoiceModel">
          <el-input v-model="form.aiVoiceModel" maxlength="100" placeholder="例如：paraformer-v2、paraformer-realtime-v2" />
        </el-form-item>
      </el-form>

      <div class="tenant-print-profile__actions">
        <el-button type="primary" icon="el-icon-check" @click="submitForm">保存</el-button>
        <el-button icon="el-icon-refresh" @click="resetFormData">重置</el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
import ImageUpload from '@/components/ImageUpload'
import { getTenantPrintProfile, updateTenantPrintProfile } from '@/api/system/tenant'

export default {
  name: 'TenantPrintProfile',
  components: { ImageUpload },
  data() {
    return {
      loading: false,
      originForm: null,
      form: {
        printCompanyName: '',
        printCompanyAddress: '',
        printCompanyPhone: '',
        printLogoPath: '',
        printFontScale: 1.0,
        printPageSize: 'A4',
        aiApiKey: '',
        aiModel: '',
        aiApiUrl: '',
        aiVisionModel: '',
        aiVoiceModel: ''
      },
      rules: {
        printCompanyName: [
          { max: 100, message: '公司名称长度不能超过100个字符', trigger: 'blur' }
        ],
        printCompanyAddress: [
          { max: 200, message: '公司地址长度不能超过200个字符', trigger: 'blur' }
        ],
        printCompanyPhone: [
          { max: 50, message: '联系电话长度不能超过50个字符', trigger: 'blur' }
        ],
        printLogoPath: [
          { max: 255, message: 'Logo路径长度不能超过255个字符', trigger: 'blur' }
        ],
        aiApiKey: [
          { max: 255, message: 'API Key 长度不能超过255个字符', trigger: 'blur' }
        ],
        aiModel: [
          { max: 100, message: '模型名称长度不能超过100个字符', trigger: 'blur' }
        ],
        aiVisionModel: [
          { max: 100, message: '视觉模型名称长度不能超过100个字符', trigger: 'blur' }
        ],
        aiVoiceModel: [
          { max: 100, message: '语音模型名称长度不能超过100个字符', trigger: 'blur' }
        ],
        aiApiUrl: [
          { max: 255, message: 'API 地址长度不能超过255个字符', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    loadData() {
      this.loading = true
      getTenantPrintProfile().then(response => {
        this.form = Object.assign({}, this.form, response.data || {})
        this.originForm = JSON.parse(JSON.stringify(this.form))
      }).finally(() => {
        this.loading = false
      })
    },
    resetFormData() {
      this.form = this.originForm
        ? JSON.parse(JSON.stringify(this.originForm))
        : {
            printCompanyName: '',
            printCompanyAddress: '',
            printCompanyPhone: '',
            printLogoPath: '',
            printFontScale: 1.0,
            printPageSize: 'A4',
            aiApiKey: '',
            aiModel: '',
            aiApiUrl: '',
            aiVisionModel: '',
            aiVoiceModel: ''
          }
      this.$nextTick(() => {
        this.$refs.form && this.$refs.form.clearValidate()
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        this.loading = true
        updateTenantPrintProfile(this.form).then(() => {
          this.$modal.msgSuccess('保存成功')
          this.originForm = JSON.parse(JSON.stringify(this.form))
        }).finally(() => {
          this.loading = false
        })
      })
    }
  }
}
</script>

<style scoped lang="scss">
.tenant-print-profile__actions {
  margin-top: 8px;
}
</style>
