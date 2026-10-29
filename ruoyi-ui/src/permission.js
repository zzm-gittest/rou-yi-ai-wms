import router from './router'
import store from './store'
import { Message } from 'element-ui'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken, setToken, setTenantId } from '@/utils/auth'
import { isPathMatch } from '@/utils/validate'
import { isRelogin } from '@/utils/request'
import request from '@/utils/request'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/register', '/register-tenant', '/', '/download']

const isWhiteList = (path) => {
  return whiteList.some(pattern => isPathMatch(pattern, path))
}

const isTenantExpiredAccessOnly = () => store.getters.tenantExpiredAccessOnly === true

const shouldRedirectToProfile = (to) => {
  return isTenantExpiredAccessOnly() && !isWhiteList(to.path) && to.path !== '/user/profile'
}

/** 飞书 IM「操作」链接携带的一次性免登票据 */
const IM_SSO_QUERY = 'imSsoTicket'
let imSsoExchanging = false

function stripImSsoQuery(query) {
  const next = { ...query }
  delete next[IM_SSO_QUERY]
  return next
}

function tryExchangeImSsoTicket(to, next) {
  const ticket = to.query && to.query[IM_SSO_QUERY]
  if (!ticket || getToken() || imSsoExchanging) {
    return false
  }
  imSsoExchanging = true
  request({
    url: '/wms/apikey/imSsoExchange',
    method: 'get',
    params: { ticket },
    headers: { isToken: false }
  }).then(res => {
    const data = res.data
    const token = data && data.token
    if (!token) {
      throw new Error('免登兑换未返回 token')
    }
    setToken(token)
    if (data.tenantId != null && data.tenantId !== '') {
      setTenantId(String(data.tenantId))
    }
    next({ path: to.path, query: stripImSsoQuery(to.query), replace: true })
  }).catch(err => {
    Message.warning((err && err.message) || '免登链接已失效，请重新登录')
    const redirect = encodeURIComponent(to.fullPath.replace(/[?&]imSsoTicket=[^&]+/g, '').replace(/\?&/, '?'))
    next(`/login?redirect=${redirect}`)
  }).finally(() => {
    imSsoExchanging = false
    NProgress.done()
  })
  return true
}

router.beforeEach((to, from, next) => {
  NProgress.start()
  if (tryExchangeImSsoTicket(to, next)) {
    return
  }
  if (getToken()) {
    to.meta.title && store.dispatch('settings/setTitle', to.meta.title)
    /* has token*/
    if (to.path === '/login') {
      next({ path: isTenantExpiredAccessOnly() ? '/user/profile' : '/index' })
      NProgress.done()
    } else if (isWhiteList(to.path)) {
      next()
    } else {
      if (store.getters.roles.length === 0) {
        isRelogin.show = true
        // 判断当前用户是否已拉取完user_info信息
        store.dispatch('GetInfo').then(() => {
          isRelogin.show = false
          store.dispatch('GenerateRoutes').then(accessRoutes => {
            // 根据roles权限生成可访问的路由表
            router.addRoutes(accessRoutes) // 动态添加可访问路由表
            if (shouldRedirectToProfile(to)) {
              next({ path: '/user/profile', replace: true })
              return
            }
            next({ ...to, replace: true }) // hack方法 确保addRoutes已完成
          })
        }).catch(err => {
            store.dispatch('LogOut').then(() => {
              Message.error(err)
              next({ path: '/login' })
            })
          })
      } else {
        if (shouldRedirectToProfile(to)) {
          next({ path: '/user/profile', replace: true })
          NProgress.done()
          return
        }
        next()
      }
    }
  } else {
    // 没有token
    if (isWhiteList(to.path)) {
      // 在免登录白名单，直接进入
      next()
    } else {
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`) // 否则全部重定向到登录页
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})
