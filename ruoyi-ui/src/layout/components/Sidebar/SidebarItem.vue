<template>
  <div v-if="!item.hidden" class="sidebar-item-wrapper">
    <!-- 单个子菜单项 -->
    <template v-if="hasOneShowingChild(item.children,item) && (!onlyOneChild.children||onlyOneChild.noShowingChildren)&&!item.alwaysShow">
      <app-link v-if="onlyOneChild.meta" :to="resolvePath(onlyOneChild.path, onlyOneChild.query)">
        <el-menu-item :index="resolvePath(onlyOneChild.path)" :class="{'submenu-title-noDropdown':!isNest}">
          <item :icon="onlyOneChild.meta.icon||(item.meta&&item.meta.icon)" :title="onlyOneChild.meta.title" />
        </el-menu-item>
      </app-link>
    </template>

    <!-- 折叠状态：使用原生 el-submenu -->
    <el-submenu
      v-else-if="isCollapse && !isNest"
      ref="subMenu"
      :index="resolvePath(item.path)"
      popper-class="sidebar-submenu-popper"
      popper-append-to-body
    >
      <template slot="title">
        <item v-if="item.meta" :icon="item.meta && item.meta.icon" :title="item.meta.title" />
      </template>
      <sidebar-item
        v-for="(child, index) in item.children"
        :key="child.path + index"
        :is-nest="true"
        :item="child"
        :base-path="resolvePath(child.path)"
        class="nest-menu"
      />
    </el-submenu>

    <!-- 展开状态或嵌套菜单：使用自定义悬停弹出 -->
    <div
      v-else
      ref="submenuWrapper"
      class="custom-submenu-wrapper"
      @mouseenter="handleMouseEnter"
      @mouseleave="handleMouseLeave"
    >
      <el-menu-item :index="resolvePath(item.path)" class="submenu-trigger">
        <item :icon="item.meta && item.meta.icon" :title="item.meta.title" />
        <i class="el-icon-arrow-right el-submenu__icon-arrow" />
      </el-menu-item>

      <!-- 自定义弹出子菜单 -->
      <transition name="submenu-fade">
        <div
          v-show="isPopupVisible"
          ref="popupMenu"
          class="custom-submenu-popup"
          :style="popupStyle"
          @mouseenter="handlePopupEnter"
          @mouseleave="handlePopupLeave"
        >
          <div class="custom-submenu-inner">
            <sidebar-item
              v-for="(child, index) in item.children"
              :key="child.path + index"
              :is-nest="true"
              :item="child"
              :base-path="resolvePath(child.path)"
              class="nest-menu"
            />
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script>
import path from 'path'
import { isExternal } from '@/utils/validate'
import Item from './Item'
import AppLink from './Link'
import FixiOSBug from './FixiOSBug'

export default {
  name: 'SidebarItem',
  components: { Item, AppLink },
  mixins: [FixiOSBug],
  props: {
    // route object
    item: {
      type: Object,
      required: true
    },
    isNest: {
      type: Boolean,
      default: false
    },
    basePath: {
      type: String,
      default: ''
    }
  },
  data() {
    this.onlyOneChild = null
    return {
      isHovering: false,
      isPopupHovering: false,
      popupPosition: { top: 0, left: 0 },
      popupMaxHeight: null,
      closeTimer: null
    }
  },
  computed: {
    isCollapse() {
      return !this.$store.state.app.sidebar.opened
    },
    isPopupVisible() {
      // 只在悬停时显示（用于展开状态的自定义弹出菜单）
      return this.isHovering
    },
    popupStyle() {
      const style = {
        top: this.popupPosition.top + 'px',
        left: this.popupPosition.left + 'px',
        zIndex: this.popupZIndex
      }
      // 动态限制最大高度，避免面板底部超出视口被裁掉
      if (this.popupMaxHeight) {
        style.maxHeight = this.popupMaxHeight + 'px'
      }
      return style
    },
    // 计算嵌套层级，用于设置 z-index
    nestLevel() {
      let level = 0
      let parent = this.$parent
      while (parent) {
        if (parent.$options.name === 'SidebarItem') {
          level++
        }
        parent = parent.$parent
      }
      return level
    },
    popupZIndex() {
      return 3000 + this.nestLevel * 100
    }
  },
  mounted() {
    if (!this.isCollapse || this.isNest) {
      this.updatePopupPosition()
      window.addEventListener('resize', this.updatePopupPosition)
    }
    // 监听路由变化更新位置
    this.$watch('$route', () => {
      if (!this.isCollapse || this.isNest) {
        this.$nextTick(() => {
          this.updatePopupPosition()
        })
      }
    })
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.updatePopupPosition)
    if (this.closeTimer) {
      clearTimeout(this.closeTimer)
    }
  },
  methods: {
    hasOneShowingChild(children = [], parent) {
      if (!children) {
        children = []
      }
      const showingChildren = children.filter(item => {
        if (item.hidden) {
          return false
        }
        this.onlyOneChild = item
        return true
      })

      if (showingChildren.length === 1) {
        return true
      }

      if (showingChildren.length === 0) {
        this.onlyOneChild = { ... parent, path: '', noShowingChildren: true }
        return true
      }

      return false
    },
    updatePopupPosition() {
      this.$nextTick(() => {
        const wrapper = this.$refs.submenuWrapper
        if (wrapper) {
          const rect = wrapper.getBoundingClientRect()
          this.popupPosition = {
            top: rect.top,
            left: rect.left + rect.width
          }
          // 触发项在屏幕下方时，限制面板高度为「触发行到视口底部」的剩余空间，
          // 防止固定定位的面板超出屏幕导致底部子菜单不可见（超出部分面板内滚动）
          const bottomMargin = 12
          const available = window.innerHeight - rect.top - bottomMargin
          this.popupMaxHeight = Math.max(available, 160)
        }
      })
    },
    handleMouseEnter() {
      if (this.closeTimer) {
        clearTimeout(this.closeTimer)
        this.closeTimer = null
      }
      this.isHovering = true
      this.updatePopupPosition()
    },
    handleMouseLeave(e) {
      // 检查是否是移动到子菜单的弹出层上
      if (e.relatedTarget && this.$refs.popupMenu && this.$refs.popupMenu.contains(e.relatedTarget)) {
        return // 移动到自己的弹出层上，不关闭
      }
      this.isHovering = false
      // 延迟关闭，给用户时间移动到弹出菜单
      this.closeTimer = setTimeout(() => {
        if (!this.isPopupHovering) {
          // 已离开且不在弹出菜单上，关闭
        }
      }, 200)
    },
    handlePopupEnter(e) {
      // 阻止事件冒泡到父级弹出层
      e.stopPropagation()
      this.isPopupHovering = true
      if (this.closeTimer) {
        clearTimeout(this.closeTimer)
        this.closeTimer = null
      }
    },
    handlePopupLeave(e) {
      e.stopPropagation()
      this.isPopupHovering = false
      this.isHovering = false
    },
    resolvePath(routePath, routeQuery) {
      if (isExternal(routePath)) {
        return routePath
      }
      if (isExternal(this.basePath)) {
        return this.basePath
      }
      if (routeQuery) {
        let query = JSON.parse(routeQuery)
        return { path: path.resolve(this.basePath, routePath), query: query }
      }
      return path.resolve(this.basePath, routePath)
    }
  }
}
</script>

<style lang="scss" scoped>
.sidebar-item-wrapper {
  width: 100%;
}

.custom-submenu-wrapper {
  position: relative;
  width: 100%;

  .submenu-trigger {
    position: relative;
    cursor: pointer;

    .el-submenu__icon-arrow {
      position: absolute;
      right: 20px;
      top: 50%;
      transform: translateY(-50%);
      transition: transform 0.3s;
    }

    &:hover .el-submenu__icon-arrow {
      transform: translateY(-50%) translateX(3px);
    }
  }
}

.custom-submenu-popup {
  position: fixed;
  min-width: 180px;
  background: #fff;
  border: none;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.15);
  border-radius: 0 4px 4px 0;
  padding: 8px 0;
  z-index: 3000;
  max-height: 80vh;
  overflow-y: auto;
  overflow-x: hidden;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: #dcdfe6;
    border-radius: 3px;

    &:hover {
      background: #c0c4cc;
    }
  }

  // 弹出菜单内的菜单项样式
  ::v-deep .el-menu-item {
    background-color: transparent !important;
    color: #606266 !important;
    height: 44px !important;
    line-height: 44px !important;
    padding: 0 20px !important;
    transition: all 0.2s !important;
    border-left: 3px solid transparent;

    &:hover {
      background-color: #f5f7fa !important;
      color: #409eff !important;
    }

    &.is-active {
      background-color: #ecf5ff !important;
      color: #409eff !important;
      font-weight: 500;
      border-left-color: #409eff;
    }
  }

  // 嵌套的子菜单包装器
  ::v-deep .custom-submenu-wrapper {
    .submenu-trigger {
      background-color: transparent;
      color: #606266;
      height: 44px;
      line-height: 44px;
      padding: 0 20px;
      transition: all 0.2s;
      border-left: 3px solid transparent;

      &:hover {
        background-color: #f5f7fa !important;
        color: #409eff !important;
      }

      &.is-active {
        background-color: #ecf5ff !important;
        color: #409eff !important;
        font-weight: 500;
        border-left-color: #409eff;
      }
    }
  }
}

.custom-submenu-inner {
  width: 100%;
}

// 深色主题 - 弹出菜单保持白色背景
.theme-dark .custom-submenu-popup {
  background: #fff;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.15);

  ::v-deep .el-menu-item {
    color: #606266 !important;

    &:hover {
      background-color: #f5f7fa !important;
      color: #409eff !important;
    }

    &.is-active {
      background-color: #ecf5ff !important;
      color: #409eff !important;
      font-weight: 500;
      border-left-color: #409eff;
    }
  }

  ::v-deep .custom-submenu-wrapper .submenu-trigger {
    color: #606266;

    &:hover {
      background-color: #f5f7fa !important;
      color: #409eff !important;
    }

    &.is-active {
      background-color: #ecf5ff !important;
      color: #409eff !important;
      font-weight: 500;
      border-left-color: #409eff;
    }
  }
}

// 弹出动画
.submenu-fade-enter-active,
.submenu-fade-leave-active {
  transition: opacity 0.2s, transform 0.2s;
  transform-origin: left center;
}

.submenu-fade-enter,
.submenu-fade-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}
</style>
