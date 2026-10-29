<template>
  <el-card v-if="showPanel" class="onboarding-panel" shadow="never">
    <div slot="header" class="panel-header">
      <div class="panel-title-left">
        <span class="panel-title">新手任务</span>
        <span class="panel-progress">{{ completedCount }}/{{ tasks.length }}</span>
      </div>
      <el-button type="text" icon="el-icon-close" @click="closePanel" />
    </div>

    <div class="task-list">
      <div
        v-for="(task, index) in tasks"
        :key="task.taskCode"
        :class="['task-item', { completed: task.isCompleted === 1, locked: isTaskLocked(task, index) }]"
        @click="goToTask(task, index)"
      >
        <span class="task-checkbox">
          <i v-if="task.isCompleted === 1" class="el-icon-circle-check" />
          <i v-else class="el-icon-circle-plus-outline" />
        </span>
        <div class="task-info">
          <div class="task-name">{{ task.name }}</div>
          <div class="task-desc">{{ task.description }}</div>
        </div>
        <div class="task-action">
          <el-tag v-if="task.isCompleted === 1" type="success" size="mini">已完成</el-tag>
          <el-tag v-else-if="isTaskLocked(task, index)" type="info" size="mini">未解锁</el-tag>
          <el-button v-else type="text" size="mini" class="action-btn">去完成</el-button>
        </div>
      </div>
    </div>

    <div class="progress-section">
      <div class="progress-text">
        <span>任务进度</span>
        <span>{{ progressPercentage }}%</span>
      </div>
      <el-progress :percentage="progressPercentage" :stroke-width="8" :show-text="false" />
    </div>
  </el-card>
</template>

<script>
import { getProgress, shouldShow } from '@/api/wms/onboarding'

export default {
  name: 'OnboardingPanel',
  data() {
    return {
      showPanel: false,
      tasks: [
        {
          taskCode: 'add_goods',
          name: '添加第一个商品',
          description: '建立商品档案，完成基础资料准备',
          url: '/wms/goods/detail/index',
          isCompleted: 0
        },
        {
          taskCode: 'purchase_in',
          name: '创建采购入库单',
          description: '完成一次采购入库流程',
          url: '/wms/purchaseIn',
          isCompleted: 0
        },
        {
          taskCode: 'sales_out',
          name: '创建销售出库单',
          description: '完成一次销售出库流程',
          url: '/wms/salesOut',
          isCompleted: 0
        },
        {
          taskCode: 'view_report',
          name: '查看库存报表',
          description: '打开库存进销存，了解库存变化',
          url: '/wms/report/stock-psi/index',
          isCompleted: 0
        }
      ]
    }
  },
  computed: {
    completedCount() {
      return this.tasks.filter(item => item.isCompleted === 1).length
    },
    progressPercentage() {
      return Math.round((this.completedCount / this.tasks.length) * 100)
    }
  },
  mounted() {
    this.bootstrapPanel()
  },
  methods: {
    async bootstrapPanel() {
      try {
        const showRes = await shouldShow()
        if (!showRes.data) {
          this.showPanel = false
          return
        }

        await this.loadProgress()
      } catch (error) {
        console.error('加载新手引导失败', error)
      }
    },
    async loadProgress() {
      const res = await getProgress()
      const progressList = res.data || []
      this.tasks.forEach(task => {
        const progress = progressList.find(item => item.taskCode === task.taskCode)
        if (progress) {
          task.isCompleted = Number(progress.isCompleted || 0)
        }
      })
      this.showPanel = this.completedCount < this.tasks.length
    },
    getFirstIncompleteIndex() {
      return this.tasks.findIndex(item => Number(item.isCompleted || 0) !== 1)
    },
    isTaskLocked(task, index) {
      const firstIncompleteIndex = this.getFirstIncompleteIndex()
      if (firstIncompleteIndex === -1) {
        return false
      }
      return Number(task.isCompleted || 0) !== 1 && index > firstIncompleteIndex
    },
    goToTask(task, index) {
      if (task.isCompleted === 1) {
        return
      }

      if (this.isTaskLocked(task, index)) {
        const requiredTask = this.tasks[this.getFirstIncompleteIndex()]
        this.$message.warning(`请按顺序完成任务，请先完成：${requiredTask ? requiredTask.name : '上一步任务'}`)
        return
      }

      const targetPath = this.resolveTaskRoute(task)
      if (!targetPath) {
        this.$message.warning('当前账号暂无该页面访问路由，请联系管理员分配菜单权限')
        return
      }

      this.$router.push(targetPath)
      setTimeout(() => {
        this.loadProgress().catch(() => {})
      }, 1500)
    },
    resolveTaskRoute(task) {
      const resolved = this.$router.resolve({ path: task.url })
      const route = resolved && resolved.route
      const hasValidMatch = route
        && route.matched
        && route.matched.length > 0
        && route.path !== '/404'
        && route.fullPath !== '/404'
      if (hasValidMatch) {
        return task.url
      }

      const sidebarRoutes = this.$store.state.permission.sidebarRouters || []
      const taskKeywordMap = {
        add_goods: 'goods',
        purchase_in: 'purchaseIn',
        sales_out: 'salesOut',
        view_report: 'stock-psi'
      }
      const keyword = taskKeywordMap[task.taskCode]
      if (!keyword) {
        return null
      }

      const findRouteByKeyword = (routes, routeKeyword, parentPath = '') => {
        for (const route of routes) {
          let fullPath = route.path || ''
          if (parentPath && fullPath && !fullPath.startsWith('/')) {
            fullPath = `${parentPath}/${fullPath}`
          }

          const lowerPath = (fullPath || '').toLowerCase()
          const lowerKeyword = routeKeyword.toLowerCase()
          if (lowerPath && (lowerPath.endsWith(lowerKeyword) || lowerPath.includes(`/${lowerKeyword}/`))) {
            return fullPath
          }

          if (route.children && route.children.length > 0) {
            const found = findRouteByKeyword(route.children, routeKeyword, fullPath)
            if (found) {
              return found
            }
          }
        }
        return null
      }

      return findRouteByKeyword(sidebarRoutes, keyword)
    },
    closePanel() {
      this.showPanel = false
    }
  }
}
</script>

<style lang="scss" scoped>
@import './index.scss';
</style>
