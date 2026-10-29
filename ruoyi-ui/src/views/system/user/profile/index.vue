<template>
  <div class="app-container">
    <el-alert
      v-if="tenantExpiredAccessOnly"
      title="当前租户服务已到期，PC 端当前仅开放个人中心，续费完成后刷新页面即可恢复全部功能。"
      type="warning"
      :closable="false"
      show-icon
      style="margin-bottom: 16px;"
    />
    <el-row :gutter="20">
      <el-col :span="6" :xs="24">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>个人信息</span>
          </div>
          <div>
            <div class="text-center">
              <userAvatar />
            </div>
            <ul class="list-group list-group-striped">
              <li class="list-group-item">
                <svg-icon icon-class="user" />用户名称
                <div class="pull-right">{{ user.userName }}</div>
              </li>
              <li class="list-group-item">
                <svg-icon icon-class="phone" />手机号码
                <div class="pull-right">{{ user.phonenumber }}</div>
              </li>
              <li class="list-group-item">
                <svg-icon icon-class="email" />用户邮箱
                <div class="pull-right">{{ user.email }}</div>
              </li>
              <li class="list-group-item">
                <svg-icon icon-class="tree" />所属部门
                <div class="pull-right" v-if="user.dept">{{ user.dept.deptName }} / {{ postGroup }}</div>
              </li>
              <li class="list-group-item">
                <svg-icon icon-class="peoples" />所属角色
                <div class="pull-right">{{ roleGroup }}</div>
              </li>
              <li class="list-group-item">
                <svg-icon icon-class="date" />创建日期
                <div class="pull-right">{{ user.createTime }}</div>
              </li>
            </ul>
          </div>
        </el-card>

        <!-- AI点数信息 -->
        <el-card class="box-card" style="margin-top: 15px;" v-if="quotaInfo">
          <div slot="header" class="clearfix">
            <span>AI点数</span>
            <el-tag v-if="!quotaInfo.aiEnabled" size="mini" type="danger" style="float: right;">未启用</el-tag>
          </div>
          <div class="quota-section">
            <div class="points-balance">
              <span class="points-num">{{ quotaInfo.aiPoints }}</span>
              <span class="points-unit">点</span>
            </div>
            <div class="points-stats">
              <div class="points-stat-item">
                <span class="stat-label">累计充值</span>
                <span class="stat-value">{{ quotaInfo.totalCharged }} 点</span>
              </div>
              <div class="points-stat-item">
                <span class="stat-label">累计消耗</span>
                <span class="stat-value">{{ quotaInfo.totalUsed }} 点</span>
              </div>
            </div>
            <div class="points-cost-info">
              <div class="cost-title">消耗标准</div>
              <div class="cost-row">
                <span>文字解析</span><span class="cost-val">1 点/次</span>
              </div>
              <div class="cost-row">
                <span>语音识别</span><span class="cost-val">4 点/次</span>
              </div>
              <div class="cost-row">
                <span>截图识别</span><span class="cost-val">6 点/次</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="18" :xs="24">
        <el-card>
          <div slot="header" class="clearfix">
            <span>基本资料</span>
          </div>
          <el-tabs v-model="selectedTab">
            <el-tab-pane label="基本资料" name="userinfo">
              <userInfo :user="user" />
            </el-tab-pane>
            <el-tab-pane label="修改密码" name="resetPwd">
              <resetPwd />
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import userAvatar from "./userAvatar"
import userInfo from "./userInfo"
import resetPwd from "./resetPwd"
import { getUserProfile, getQuotaInfo } from "@/api/system/user"
import { mapGetters } from 'vuex'

export default {
  name: "Profile",
  components: { userAvatar, userInfo, resetPwd },
  computed: {
    ...mapGetters([
      'tenantExpiredAccessOnly'
    ])
  },
  data() {
    return {
      user: {},
      roleGroup: {},
      postGroup: {},
      selectedTab: "userinfo",
      quotaInfo: null
    }
  },
  created() {
    const activeTab = this.$route.params && this.$route.params.activeTab
    if (activeTab) {
      this.selectedTab = activeTab
    }
    this.getUser()
    this.getQuota()
  },
  methods: {
    getUser() {
      getUserProfile().then(response => {
        this.user = response.data
        this.roleGroup = response.roleGroup
        this.postGroup = response.postGroup
      })
    },
    getQuota() {
      getQuotaInfo().then(response => {
        this.quotaInfo = response.data
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.quota-section {
  padding: 5px 0;
}
.points-balance {
  text-align: center;
  padding: 10px 0 15px;
}
.points-num {
  font-size: 36px;
  font-weight: 600;
  color: #409EFF;
}
.points-unit {
  font-size: 14px;
  color: #909399;
  margin-left: 4px;
}
.points-stats {
  display: flex;
  justify-content: space-around;
  padding: 10px 0;
  border-top: 1px solid #EBEEF5;
  border-bottom: 1px solid #EBEEF5;
}
.points-stat-item {
  text-align: center;
}
.stat-label {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}
.stat-value {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}
.points-cost-info {
  padding-top: 12px;
}
.cost-title {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
}
.cost-row {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  padding: 3px 0;
}
.cost-val {
  color: #606266;
}
</style>
