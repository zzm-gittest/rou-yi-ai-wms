import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

/* Layout */
import Layout from '@/layout'
import { buildBillListRouteAliases } from './billListRouteAliases'

/**
 * Note: 路由配置项
 *
 * hidden: true                     // 当设置 true 的时候该路由不会再侧边栏出现 如401，login等页面，或者如一些编辑页面/edit/1
 * alwaysShow: true                 // 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面
 *                                  // 只有一个时，会将那个子路由当做根路由显示在侧边栏--如引导页面
 *                                  // 若你想不管路由下面的 children 声明的个数都显示你的根路由
 *                                  // 你可以设置 alwaysShow: true，这样它就会忽略之前定义的规则，一直显示根路由
 * redirect: noRedirect             // 当设置 noRedirect 的时候该路由在面包屑导航中不可被点击
 * name:'router-name'               // 设定路由的名字，一定要填写不然使用<keep-alive>时会出现各种问题
 * query: '{"id": 1, "name": "ry"}' // 访问路由的默认传递参数
 * roles: ['admin', 'common']       // 访问路由的角色权限
 * permissions: ['a:a:a', 'b:b:b']  // 访问路由的菜单权限
 * meta : {
    noCache: true                   // 如果设置为true，则不会被 <keep-alive> 缓存(默认 false)
    title: 'title'                  // 设置该路由在侧边栏和面包屑中展示的名字
    icon: 'svg-name'                // 设置该路由的图标，对应路径src/assets/icons/svg
    breadcrumb: false               // 如果设置为false，则不会在breadcrumb面包屑中显示
    activeMenu: '/system/user'      // 当路由设置了该属性，则会高亮相对应的侧边栏。
  }
 */

// 公共路由
export const constantRoutes = [
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect')
      }
    ]
  },
  {
    path: '/login',
    component: () => import('@/views/login'),
    hidden: true
  },
  {
    path: '/register-tenant',
    component: () => import('@/views/register-tenant'),
    hidden: true
  },
  {
    path: '/register',
    component: () => import('@/views/register'),
    hidden: true
  },
  {
    path: '/404',
    component: () => import('@/views/error/404'),
    hidden: true
  },
  {
    path: '/401',
    component: () => import('@/views/error/401'),
    hidden: true
  },
  {
    path: '/download',
    component: () => import('@/views/download'),
    name: 'Download',
    hidden: true
  },
  {
    path: '/',
    component: () => import('@/views/landing.vue'),
    name: 'Landing',
    hidden: true,
    meta: { title: '羽辽进销存' }
  },
  {
    path: '',
    component: Layout,
    redirect: 'index',
    children: [
      {
        path: 'index',
        component: () => import('@/views/index'),
        name: 'Index',
        meta: { title: '控制台', icon: 'dashboard', affix: true }
      }
    ]
  },
  {
    path: '/user',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'profile',
        component: () => import('@/views/system/user/profile/index'),
        name: 'Profile',
        meta: { title: '个人中心', icon: 'user' }
      }
    ]
  }
]

// 动态路由，基于用户权限动态去加载
export const dynamicRoutes = [
  {
    path: '/system/user-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:user:edit'],
    children: [
      {
        path: 'role/:userId(\\d+)',
        component: () => import('@/views/system/user/authRole'),
        name: 'AuthRole',
        meta: { title: '分配角色', activeMenu: '/system/user' }
      }
    ]
  },
  {
    path: '/system/role-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:role:edit'],
    children: [
      {
        path: 'user/:roleId(\\d+)',
        component: () => import('@/views/system/role/authUser'),
        name: 'AuthUser',
        meta: { title: '分配用户', activeMenu: '/system/role' }
      }
    ]
  },
  {
    path: '/system/dict-data',
    component: Layout,
    hidden: true,
    permissions: ['system:dict:list'],
    children: [
      {
        path: 'index/:dictId(\\d+)',
        component: () => import('@/views/system/dict/data'),
        name: 'Data',
        meta: { title: '字典数据', activeMenu: '/system/dict' }
      }
    ]
  },
  {
    path: '/monitor/job-log',
    component: Layout,
    hidden: true,
    permissions: ['monitor:job:list'],
    children: [
      {
        path: 'index/:jobId(\\d+)',
        component: () => import('@/views/monitor/job/log'),
        name: 'JobLog',
        meta: { title: '调度日志', activeMenu: '/monitor/job' }
      }
    ]
  },
  {
    path: '/tool/gen-edit',
    component: Layout,
    hidden: true,
    permissions: ['tool:gen:edit'],
    children: [
      {
        path: 'index/:tableId(\\d+)',
        component: () => import('@/views/tool/gen/editTable'),
        name: 'GenEdit',
        meta: { title: '修改生成配置', activeMenu: '/tool/gen' }
      }
    ]
  },
  {
    path: '/wms/goodsSpec',
    component: Layout,
    hidden: true,
    permissions: ['wms:goodsSpec:query'],
    children: [
      {
        path: 'index',
        component: () => import('@/views/wms/goodsSpec/index'),
        name: 'WmsGoodsSpec',
        meta: { title: '商品规格管理', activeMenu: '/wms/goods' }
      }
    ]
  },
  {
    path: '/wms/goodsSku',
    component: Layout,
    hidden: true,
    permissions: ['wms:goodsSku:query'],
    children: [
      {
        path: 'index',
        component: () => import('@/views/wms/goodsSku/index'),
        name: 'WmsGoodsSku',
        meta: { title: '商品SKU管理', activeMenu: '/wms/goods' }
      }
    ]
  },
  {
    path: '/wms/goods/detail',
    component: Layout,
    hidden: true,
    permissions: ['wms:goods:add', 'wms:goods:edit'],
    children: [
      {
        path: 'index',
        component: () => import('@/views/wms/goods/detail'),
        name: 'WmsGoodsDetail',
        meta: { title: '商品详情', activeMenu: '/wms/goods' }
      }
    ]
  },
  // 报表中心路由
  {
    path: '/wms/report/sales-daily',
    component: Layout,
    hidden: true,
    permissions: ['wms:report:sales'],
    children: [
      {
        path: 'index',
        component: () => import('@/views/wms/report/sales-daily'),
        name: 'sales-daily',
        meta: { title: '销售日报表', activeMenu: '/wms/report' }
      }
    ]
  },
  {
    path: '/wms/purchaseIn/print',
    component: Layout,
    hidden: true,
    permissions: ['wms:purchaseIn:print'],
    children: [
      {
        path: 'index',
        component: () => import('@/views/wms/purchaseIn/print'),
        name: 'PurchaseInPrint',
        meta: { title: '采购入库打印', activeMenu: '/wms/purchaseIn' }
      }
    ]
  },
  {
    path: '/wms/salesOut/print',
    component: Layout,
    hidden: true,
    permissions: ['wms:salesOut:print'],
    children: [
      {
        path: 'index',
        component: () => import('@/views/wms/salesOut/print'),
        name: 'SalesOutPrint',
        meta: { title: '销售出库打印', activeMenu: '/wms/salesOut' }
      }
    ]
  },
  {
    path: '/wms/purchaseOrder/print',
    component: Layout,
    hidden: true,
    permissions: ['wms:purchaseOrder:query'],
    children: [
      {
        path: 'index',
        component: () => import('@/views/wms/purchaseOrder/print'),
        name: 'PurchaseOrderPrint',
        meta: { title: '采购订单打印', activeMenu: '/wms/purchaseOrder' }
      }
    ]
  },
  {
    path: '/wms/salesOrder/print',
    component: Layout,
    hidden: true,
    permissions: ['wms:salesOrder:query'],
    children: [
      {
        path: 'index',
        component: () => import('@/views/wms/salesOrder/print'),
        name: 'SalesOrderPrint',
        meta: { title: '销售订单打印', activeMenu: '/wms/salesOrder' }
      }
    ]
  },
  {
    path: '/wms/purchaseReturn/print',
    component: Layout,
    hidden: true,
    permissions: ['wms:purchaseReturn:query'],
    children: [
      {
        path: 'index',
        component: () => import('@/views/wms/purchaseReturn/print'),
        name: 'PurchaseReturnPrint',
        meta: { title: '采购退货打印', activeMenu: '/wms/purchaseReturn' }
      }
    ]
  },
  {
    path: '/wms/salesReturn/print',
    component: Layout,
    hidden: true,
    permissions: ['wms:salesReturn:query'],
    children: [
      {
        path: 'index',
        component: () => import('@/views/wms/salesReturn/print'),
        name: 'SalesReturnPrint',
        meta: { title: '销售退货打印', activeMenu: '/wms/salesReturn' }
      }
    ]
  },
  {
    path: '/wms/stockTransfer/print',
    component: Layout,
    hidden: true,
    permissions: ['wms:stockTransfer:query'],
    children: [
      {
        path: 'index',
        component: () => import('@/views/wms/stockTransfer/print'),
        name: 'StockTransferPrint',
        meta: { title: '库存调拨打印', activeMenu: '/wms/stockTransfer' }
      }
    ]
  },
  {
    path: '/wms/stockCheck/print',
    component: Layout,
    hidden: true,
    permissions: ['wms:stockCheck:query'],
    children: [
      {
        path: 'index',
        component: () => import('@/views/wms/stockCheck/print'),
        name: 'StockCheckPrint',
        meta: { title: '库存盘点打印', activeMenu: '/wms/stockCheck' }
      }
    ]
  },
  {
    path: '/wms/accountsReceivable/print',
    component: Layout,
    hidden: true,
    permissions: ['wms:accountsReceivable:query'],
    children: [
      {
        path: 'index',
        component: () => import('@/views/wms/accountsReceivable/print'),
        name: 'AccountsReceivablePrint',
        meta: { title: '应收账款打印', activeMenu: '/wms/accountsReceivable' }
      }
    ]
  },
  {
    path: '/wms/accountsPayable/print',
    component: Layout,
    hidden: true,
    permissions: ['wms:accountsPayable:query'],
    children: [
      {
        path: 'index',
        component: () => import('@/views/wms/accountsPayable/print'),
        name: 'AccountsPayablePrint',
        meta: { title: '应付账款打印', activeMenu: '/wms/accountsPayable' }
      }
    ]
  },
  {
    path: '/wms/report/goods-rank',
    component: Layout,
    hidden: true,
    permissions: ['wms:report:sales'],
    children: [
      {
        path: 'index',
        component: () => import('@/views/wms/report/goods-rank'),
        name: 'goods-rank',
        meta: { title: '商品销售排行', activeMenu: '/wms/report' }
      }
    ]
  },
  {
    path: '/wms/report/customer-analysis',
    component: Layout,
    hidden: true,
    permissions: ['wms:report:sales'],
    children: [
      {
        path: 'index',
        component: () => import('@/views/wms/report/customer-analysis'),
        name: 'customer-analysis',
        meta: { title: '客户销售分析', activeMenu: '/wms/report' }
      }
    ]
  },
  {
    path: '/wms/report/supplier-analysis',
    component: Layout,
    hidden: true,
    permissions: ['wms:report:purchase'],
    children: [
      {
        path: 'index',
        component: () => import('@/views/wms/report/supplier-analysis'),
        name: 'supplier-analysis',
        meta: { title: '供应商采购分析', activeMenu: '/wms/report' }
      }
    ]
  },
  {
    path: '/wms/report/stock-psi',
    component: Layout,
    hidden: true,
    permissions: ['wms:report:stock'],
    children: [
      {
        path: 'index',
        component: () => import('@/views/wms/report/stock-psi'),
        name: 'stock-psi',
        meta: { title: '库存进销存', activeMenu: '/wms/report' }
      }
    ]
  },
  {
    path: '/wms/report/ar-aging',
    component: Layout,
    hidden: true,
    permissions: ['wms:report:finance'],
    children: [
      {
        path: 'index',
        component: () => import('@/views/wms/report/ar-aging'),
        name: 'ar-aging',
        meta: { title: '应收账款账龄', activeMenu: '/wms/report' }
      }
    ]
  },
  ...buildBillListRouteAliases()
]

// 防止连续点击多次路由报错
let routerPush = Router.prototype.push
let routerReplace = Router.prototype.replace
// push
Router.prototype.push = function push(location) {
  return routerPush.call(this, location).catch(err => err)
}
// replace
Router.prototype.replace = function push(location) {
  return routerReplace.call(this, location).catch(err => err)
}

export default new Router({
  mode: 'history', // 去掉url中的#
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRoutes
})
