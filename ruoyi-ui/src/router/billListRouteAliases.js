import Layout from '@/layout'

/**
 * 飞书/AIGW 外链与历史菜单 path 别名：同一列表页同时支持 /purchase|/sales|/stock 与 /wms 前缀。
 * 避免外链 path 与动态菜单不一致时出现白屏、ChunkLoadError。
 */
function aliasRoute(parentPath, childPath, viewImport, title, activeMenu) {
  return {
    path: parentPath,
    component: Layout,
    hidden: true,
    children: [
      {
        path: childPath,
        component: viewImport,
        name: `BillAlias_${parentPath}_${childPath}`.replace(/[^\w]/g, '_'),
        meta: { title, activeMenu: activeMenu || `${parentPath}/${childPath}` }
      }
    ]
  }
}

const BILL_LIST_PAGES = [
  { views: () => import('@/views/wms/purchaseOrder/index'), title: '采购订单', keys: ['purchaseOrder'] },
  { views: () => import('@/views/wms/purchaseIn/index'), title: '采购入库', keys: ['purchaseIn'] },
  { views: () => import('@/views/wms/purchaseReturn/index'), title: '采购退货', keys: ['purchaseReturn'] },
  { views: () => import('@/views/wms/salesOrder/index'), title: '销售订单', keys: ['salesOrder'] },
  { views: () => import('@/views/wms/salesOut/index'), title: '销售出库', keys: ['salesOut'] },
  { views: () => import('@/views/wms/salesReturn/index'), title: '销售退货', keys: ['salesReturn'] },
  { views: () => import('@/views/wms/stockCheck/index'), title: '库存盘点', keys: ['stockCheck'] },
  { views: () => import('@/views/wms/stockTransfer/index'), title: '库存调拨', keys: ['stockTransfer'] }
]

/** 仅补充 /wms 前缀别名，避免与动态菜单 /purchase、/sales、/stock 重复注册 */
const PARENT_PREFIXES = ['/wms']

export function buildBillListRouteAliases() {
  const routes = []
  BILL_LIST_PAGES.forEach(page => {
    page.keys.forEach(key => {
      PARENT_PREFIXES.forEach(parent => {
        const active = `${parent}/${key}`
        routes.push(aliasRoute(parent, key, page.views, page.title, active))
      })
    })
  })
  return routes
}
