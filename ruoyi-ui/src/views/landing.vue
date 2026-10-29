<template>
  <div class="landing-page">
    <header class="header">
      <div class="header-content">
        <a href="#top" class="brand-mark">
          <img v-if="logoUrl" :src="logoUrl" alt="羽辽进销存 Logo" class="brand-logo" />
          <div v-else class="brand-logo brand-logo-placeholder">羽辽</div>
          <div class="brand-copy">
            <strong>羽辽进销存</strong>
            <span>AI 驱动的仓储经营平台</span>
          </div>
        </a>
        <nav class="nav">
          <a href="#features" class="nav-link">核心能力</a>
          <a href="#ai" class="nav-link">AI 场景</a>
          <a href="#preview" class="nav-link">产品预览</a>
          <a href="#download" class="nav-link">下载</a>
          <router-link to="/index" class="nav-link admin-link">进入后台</router-link>
        </nav>
      </div>
    </header>

    <main id="top">
      <section class="hero">
        <div class="hero-glow hero-glow-left"></div>
        <div class="hero-glow hero-glow-right"></div>
        <div class="hero-content">
          <div class="hero-copy">
            <span class="hero-tag">羽辽进销存</span>
            <h1 class="hero-title">
              让采购销售管理像对话一样自然，
              <span>AI赋能高效管理采购或销售流程</span>
            </h1>
            <p class="hero-subtitle">
              羽辽进销存 将进销存、库存预警、业务报表与 AI 助手整合到同一套系统里，
              面向老板、仓管、销售与采购，提供能落地执行的仓储协同体验。
            </p>
            <div class="hero-actions">
              <a href="#download" class="btn btn-primary">下载 iPhone / iPad 版</a>
              <router-link to="/index" class="btn btn-secondary">查看后台管理</router-link>
            </div>
            <div class="hero-metrics">
              <div v-for="metric in showcaseMetrics" :key="metric.label" class="metric-card">
                <strong>{{ metric.value }}</strong>
                <span>{{ metric.label }}</span>
              </div>
            </div>
          </div>

          <div class="hero-panel">
            <div class="hero-brand-card">
              <div class="hero-brand-top">
                <img v-if="logoUrl" :src="logoUrl" alt="羽辽进销存 品牌图形" class="hero-brand-logo" />
                <div v-else class="hero-brand-logo brand-logo-panel-placeholder">羽辽进销存</div>
                <div>
                  <p class="panel-eyebrow">降本增效</p>
                  <h2>羽辽进销存</h2>
                  <p>一个AI管理入口打通进销存，让每一次出入库都更快、更准、更省心。</p>
                </div>
              </div>
              <div class="hero-brand-strip">
                <span>采购入库</span>
                <span>销售出库</span>
                <span>库存盘点</span>
                <span>经营分析</span>
              </div>
            </div>

            <div class="hero-chat-card">
              <div class="chat-card-header">
                <span class="chat-dot"></span>
                <strong>AI 助手实时演示</strong>
              </div>
              <div class="chat-list">
                <div class="chat-item user">
                  帮我生成今天经营简报，并把异常提醒一起列出来。
                </div>
                <div class="chat-item ai">
                  今日经营简报：净销售额 ￥192,779.60，毛利 ￥31,926.60；采购入库 ￥420.00；当前共有 48 项待处理。异常提醒包括 8 笔已审核销售订单超 3 天未创建出库单、12 笔已审核采购订单超 3 天未创建入库单，以及 229 款近 30 天零销量且库存金额大于 ￥500 的商品。
                </div>
                <div class="chat-item user">
                  再给我几个我可以继续追问的问题。
                </div>
                <div class="chat-item ai">
                  你可以继续问我：本周销售情况怎么样、今日哪个商品卖得最好、查看待办事项、查看积压订单。
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="section intro-section">
        <div class="section-content">
          <div class="section-heading left">
            <span class="section-label">为什么选择</span>
            <h2>面向仓库一线执行，覆盖从建单到复盘的完整业务流程</h2>
            <p>首页信息围绕业务场景、AI能力与核心单据流程组织，帮助首次访问用户快速理解系统价值并开始试用。</p>
          </div>
          <div class="advantage-grid">
            <article v-for="advantage in advantages" :key="advantage.title" class="advantage-card">
              <span class="advantage-icon">{{ advantage.icon }}</span>
              <h3>{{ advantage.title }}</h3>
              <p>{{ advantage.description }}</p>
            </article>
          </div>
        </div>
      </section>

      <section id="ai" class="section ai-section">
        <div class="section-content">
          <div class="section-heading">
            <span class="section-label">AI 场景</span>
            <h2>让自然语言参与进销存全流程，而不是只做问答展示</h2>
            <p>从建单、查单、审核到报表总结，AI 助手围绕真实业务动作工作。</p>
          </div>
          <div class="scenario-grid">
            <article v-for="scenario in aiScenarios" :key="scenario.title" class="scenario-card">
              <span class="scenario-kicker">{{ scenario.kicker }}</span>
              <h3>{{ scenario.title }}</h3>
              <p>{{ scenario.description }}</p>
              <span class="scenario-example">{{ scenario.example }}</span>
            </article>
          </div>
        </div>
      </section>

      <section id="features" class="section features-section">
        <div class="section-content">
          <div class="section-heading">
            <span class="section-label">核心能力</span>
            <h2>AI语音/OCR/微信截图 建单，单据快速打印</h2>
            <p>流畅高效的采购销售订单管理流程，AI智能识别提升工作效率，打印快捷，让你更快了解单据准确性。</p>
          </div>
          <div class="features-grid">
            <article v-for="capability in capabilities" :key="capability.title" class="feature-card">
              <div class="feature-card-top">
                <span class="feature-icon">{{ capability.icon }}</span>
                <h3>{{ capability.title }}</h3>
              </div>
              <p>{{ capability.description }}</p>
            </article>
          </div>
        </div>
      </section>

      <section id="preview" class="section preview-section">
        <div class="section-content">
          <div class="section-heading">
            <span class="section-label">产品预览</span>
            <h2>移动端与后台数据联动，关键业务界面一目了然</h2>
            <p>移动端与后台数据实时同步，关键业务界面清晰展示，帮助用户快速做出决策。</p>
          </div>
          <div class="screenshots-grid">
            <article v-for="shot in screenshots" :key="shot.title" class="screenshot-card">
              <div class="screenshot-frame">
                <img :src="shot.url" :alt="shot.title" />
              </div>
              <h3>{{ shot.title }}</h3>
              <p>{{ shot.description }}</p>
            </article>
          </div>
        </div>
      </section>

      <section id="download" class="section download-section">
        <div class="section-content download-layout">
          <div class="download-main-card">
            <span class="section-label">官方下载</span>
            <h2>前往 App Store 下载，或直接进入后台管理系统</h2>
            <p>
              适用于 iPhone 与 iPad，用户可通过官方商店下载安装当前版本，后台端负责完整业务管理与复盘。
            </p>
            <div class="download-actions">
              <a :href="appStoreUrl" target="_blank" rel="noopener noreferrer" class="store-btn">
                <span class="store-icon">APP</span>
                <span>
                  <strong>App Store 下载</strong>
                  <small>适用于 iPhone / iPad</small>
                </span>
              </a>
              <router-link to="/index" class="store-btn store-btn-secondary">
                <span class="store-icon">WEB</span>
                <span>
                  <strong>进入后台</strong>
                  <small>查看完整业务功能</small>
                </span>
              </router-link>
            </div>
          </div>

          <div class="download-side-card">
            <div class="qr-shell">
              <img :src="qrCodeUrl" alt="羽辽进销存 App Store 下载二维码" class="qr-image" />
            </div>
            <h3>扫描二维码前往 App Store</h3>
            <p>使用 iPhone 或 iPad 扫码后即可跳转官方商店下载页面。</p>
            <div class="brand-mini">
              <img v-if="logoUrl" :src="logoUrl" alt="羽辽进销存" />
              <div v-else class="brand-mini-placeholder">羽辽</div>
              <span>羽辽进销存</span>
            </div>
          </div>
        </div>
      </section>

   
    </main>

    <footer class="footer">
      <div class="footer-content">
        <div class="footer-brand">
          <img v-if="logoUrl" :src="logoUrl" alt="羽辽进销存" />
          <div v-else class="brand-mini-placeholder">羽辽</div>
          <div>
            <strong>羽辽进销存</strong>
            <span>AI 驱动的采购销售管理平台</span>
          </div>
        </div>
        <div class="footer-links">
          <a href="/static/privacy.html" target="_blank" rel="noopener noreferrer">隐私政策</a>
          <a href="/static/protocol.html" target="_blank" rel="noopener noreferrer">用户协议</a>
          <a href="mailto:289710565@qq.com">联系我们</a>
        </div>
        <p class="copyright">© 2026 羽辽进销存 All Rights Reserved</p>
      </div>
    </footer>
  </div>
</template>

<script>
import { buildCosImageUrl } from '@/utils/index'

const brandLogoUrl = buildCosImageUrl('logo.png')

export default {
  name: 'LandingPage',
  data() {
    return {
      logoUrl: brandLogoUrl,
      appStoreUrl: 'https://apps.apple.com/app/id6761791831',
      showcaseMetrics: [
        { value: 'AI 建单', label: '核心单据场景' },
        { value: 'AI 协同', label: '支持自然语言操作' },
        { value: '实时', label: '库存与经营同步' }
      ],
      advantages: [
        {
          icon: '01',
          title: '业务流程更清晰',
          description: '采购、销售、库存等高频动作按流程呈现，降低学习与跨岗位协同成本。'
        },
        {
          icon: '02',
          title: 'AI 直接参与执行',
          description: 'AI用于建单、查单、异常提醒与经营汇总，不停留在对话展示层。'
        },
        {
          icon: '03',
          title: '上手路径更顺畅',
          description: '下载APP与进入后台入口清晰可见，新用户访问后可立即进入实际操作。'
        }
      ],
      aiScenarios: [
        {
          kicker: '语音建单',
          title: '一句话生成采购或销售单据',
          description: '把商品、数量、客户、单价等关键信息解析成结构化单据，减少人工录入时间。',
          example: '示例：给王姐下 30 箱冻虾，今天安排发货。'
        },
        {
          kicker: '业务追踪',
          title: '按自然语言查看订单、库存与异常',
          description: '不必切换多个菜单，直接问“今天有哪些待发货订单”“哪个商品缺货”即可获得结果。',
          example: '示例：帮我看低于安全库存的商品，并按仓区排序。'
        },
        {
          kicker: '经营分析',
          title: '自动生成晨会摘要与经营日报',
          description: '自动汇总待处理事项、订单趋势、库存波动与异常记录，减少管理层人工汇报成本。',
          example: '示例：生成今天仓库晨会纪要，并提炼三个重点风险。'
        }
      ],
      capabilities: [
        { icon: '销', title: '销售管理', description: '销售订单、销售出库、退货处理一体化衔接。' },
        { icon: '采', title: '采购管理', description: '采购下单、到货入库、供应商往来全流程可追踪。' },
        { icon: '库', title: '库存管理', description: '库存查询、调拨、盘点、预警与仓区状态实时联动。' },
        { icon: '财', title: '财务协同', description: '应收应付、结算记录、费用与业务数据互相对齐。' },
        { icon: '客', title: '客户供应商', description: '客户、供应商、商品档案统一管理，降低信息分散。' },
        { icon: '报', title: '报表中心', description: '销售、库存、财务与经营指标按角色分层查看。' }
      ],
      screenshots: [
        {
          title: 'AI 菜单导航',
          description: '按业务角色组织入口，常用功能可一步直达，减少找菜单时间。',
          url: buildCosImageUrl('pc/app-preview-1.jpg')
        },
        {
          title: '智能语聊',
          description: '支持语音与文本交互，快速完成查单、查库存与异常提醒。',
          url: buildCosImageUrl('pc/app-preview-2.jpg')
        },
        {
          title: '单据打印',
          description: '移动端直接根据单据选择打印机在线打印条码单据，提升操作效率与准确性。',
          url: buildCosImageUrl('pc/app-preview-3.jpg')
        },
        {
          title: '更多功能扩展',
          description: '持续扩展采购、销售、库存与报表能力，适配不同发展阶段。',
          url: buildCosImageUrl('pc/app-preview-4.jpg')
        }
      ]
    }
  },
  computed: {
    qrCodeUrl() {
      return `https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=${encodeURIComponent(this.appStoreUrl)}`
    }
  }
}
</script>

<style lang="scss" scoped>
.landing-page {
  --brand-blue: #1251a3;
  --brand-blue-deep: #0d3975;
  --brand-orange: #ff7a21;
  --brand-ink: #17304d;
  --brand-muted: #60758e;
  --brand-line: rgba(18, 81, 163, 0.12);
  --brand-surface: rgba(255, 255, 255, 0.86);
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(255, 122, 33, 0.18), transparent 24%),
    radial-gradient(circle at 82% 12%, rgba(18, 81, 163, 0.16), transparent 20%),
    linear-gradient(180deg, #fffaf4 0%, #fff 32%, #f5f9ff 100%);
  color: var(--brand-ink);
  font-family: "Avenir Next", "PingFang SC", "Microsoft YaHei", sans-serif;
}

.landing-page *,
.landing-page *::before,
.landing-page *::after {
  box-sizing: border-box;
}

.header {
  position: sticky;
  top: 0;
  z-index: 20;
  backdrop-filter: blur(18px);
  background: rgba(255, 250, 244, 0.78);
  border-bottom: 1px solid rgba(18, 81, 163, 0.08);
}

.header-content,
.section-content,
.hero-content,
.footer-content {
  width: min(1180px, calc(100% - 40px));
  margin: 0 auto;
}

.header-content {
  min-height: 78px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.brand-mark {
  display: inline-flex;
  align-items: center;
  gap: 14px;
  text-decoration: none;
  color: inherit;
}

.brand-logo {
  width: 68px;
  height: 48px;
  object-fit: contain;
  filter: drop-shadow(0 10px 18px rgba(18, 81, 163, 0.14));
}

.brand-logo-placeholder,
.brand-logo-panel-placeholder,
.brand-mini-placeholder {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(18, 81, 163, 0.12), rgba(255, 122, 33, 0.12));
  color: var(--brand-blue);
  font-weight: 800;
}

.brand-logo-placeholder {
  border-radius: 14px;
  font-size: 14px;
}

.brand-copy {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.brand-copy strong {
  font-size: 20px;
  letter-spacing: 0.02em;
}

.brand-copy span {
  color: var(--brand-muted);
  font-size: 12px;
}

.nav {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.nav-link {
  padding: 10px 14px;
  border-radius: 999px;
  color: var(--brand-ink);
  text-decoration: none;
  font-size: 14px;
  transition: 0.2s ease;
}

.nav-link:hover {
  background: rgba(18, 81, 163, 0.08);
  color: var(--brand-blue);
}

.admin-link {
  background: var(--brand-blue);
  color: #fff;
  box-shadow: 0 14px 24px rgba(18, 81, 163, 0.18);
}

.admin-link:hover {
  background: var(--brand-blue-deep);
  color: #fff;
}

.hero {
  position: relative;
  overflow: hidden;
  padding: 54px 0 48px;
}

.hero-glow {
  position: absolute;
  border-radius: 999px;
  filter: blur(10px);
  pointer-events: none;
}

.hero-glow-left {
  width: 260px;
  height: 260px;
  background: rgba(255, 122, 33, 0.18);
  top: 54px;
  left: -40px;
}

.hero-glow-right {
  width: 340px;
  height: 340px;
  background: rgba(18, 81, 163, 0.14);
  top: 70px;
  right: -80px;
}

.hero-content {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(340px, 0.95fr);
  gap: 34px;
  align-items: center;
}

.hero-copy {
  padding: 40px 0;
}

.hero-tag,
.section-label,
.panel-eyebrow,
.scenario-kicker {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 7px 12px;
  border-radius: 999px;
  background: rgba(18, 81, 163, 0.08);
  color: var(--brand-blue);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.hero-title {
  margin: 20px 0 18px;
  font-size: clamp(38px, 5vw, 66px);
  line-height: 1.08;
  letter-spacing: -0.03em;
}

.hero-title span {
  display: block;
  color: var(--brand-orange);
}

.hero-subtitle {
  max-width: 640px;
  font-size: 18px;
  line-height: 1.8;
  color: var(--brand-muted);
}

.hero-actions {
  display: flex;
  gap: 14px;
  margin: 32px 0;
  flex-wrap: wrap;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 52px;
  padding: 0 24px;
  border-radius: 16px;
  text-decoration: none;
  font-size: 15px;
  font-weight: 700;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.btn:hover,
.store-btn:hover,
.feature-card:hover,
.scenario-card:hover,
.advantage-card:hover,
.screenshot-card:hover {
  transform: translateY(-4px);
}

.btn-primary {
  background: linear-gradient(135deg, var(--brand-orange) 0%, #ff9852 100%);
  color: #fff;
  box-shadow: 0 18px 28px rgba(255, 122, 33, 0.22);
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(18, 81, 163, 0.12);
  color: var(--brand-blue);
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.metric-card {
  padding: 18px 16px;
  border: 1px solid var(--brand-line);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.62);
  box-shadow: 0 18px 34px rgba(34, 62, 108, 0.06);
}

.metric-card strong {
  display: block;
  font-size: 24px;
  color: var(--brand-blue-deep);
}

.metric-card span {
  display: block;
  margin-top: 8px;
  font-size: 13px;
  color: var(--brand-muted);
}

.hero-panel {
  display: grid;
  gap: 18px;
}

.hero-brand-card,
.hero-chat-card,
.advantage-card,
.scenario-card,
.feature-card,
.screenshot-card,
.download-main-card,
.download-side-card {
  background: var(--brand-surface);
  border: 1px solid rgba(255, 255, 255, 0.72);
  box-shadow: 0 22px 48px rgba(34, 62, 108, 0.08);
  backdrop-filter: blur(14px);
}

.hero-brand-card {
  padding: 28px;
  border-radius: 28px;
}

.hero-brand-top {
  display: grid;
  grid-template-columns: 132px minmax(0, 1fr);
  gap: 18px;
  align-items: center;
}

.hero-brand-logo {
  width: 132px;
  height: 94px;
  object-fit: contain;
}

.brand-logo-panel-placeholder {
  width: 132px;
  height: 94px;
  border-radius: 22px;
  font-size: 20px;
}

.hero-brand-top h2 {
  margin: 10px 0 8px;
  font-size: 28px;
}

.hero-brand-top p:last-child {
  color: var(--brand-muted);
  line-height: 1.7;
}

.hero-brand-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 22px;
}

.hero-brand-strip span {
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(255, 122, 33, 0.08);
  color: var(--brand-orange);
  font-size: 13px;
  font-weight: 700;
}

.hero-chat-card {
  padding: 24px;
  border-radius: 28px;
  background: linear-gradient(180deg, rgba(13, 57, 117, 0.96) 0%, rgba(18, 81, 163, 0.94) 100%);
  color: #fff;
}

.chat-card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 18px;
}

.chat-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #7dffb2;
  box-shadow: 0 0 0 6px rgba(125, 255, 178, 0.18);
}

.chat-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.chat-item {
  max-width: 88%;
  padding: 14px 16px;
  border-radius: 18px;
  line-height: 1.7;
  font-size: 14px;
}

.chat-item.user {
  align-self: flex-end;
  background: rgba(255, 255, 255, 0.18);
  border-bottom-right-radius: 6px;
}

.chat-item.ai {
  align-self: flex-start;
  background: rgba(255, 255, 255, 0.94);
  color: var(--brand-ink);
  border-bottom-left-radius: 6px;
}

.section {
  padding: 48px 0 88px;
}

.section-heading {
  max-width: 820px;
  margin: 0 auto 38px;
  text-align: center;
}

.section-heading.left {
  margin-left: 0;
  text-align: left;
}

.section-heading h2 {
  margin: 18px 0 14px;
  font-size: clamp(30px, 4vw, 46px);
  line-height: 1.18;
}

.section-heading p {
  color: var(--brand-muted);
  font-size: 16px;
  line-height: 1.8;
}

.intro-section {
  padding-top: 18px;
}

.advantage-grid,
.scenario-grid,
.features-grid,
.screenshots-grid {
  display: grid;
  gap: 20px;
}

.advantage-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.advantage-card {
  padding: 28px;
  border-radius: 24px;
}

.advantage-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(18, 81, 163, 0.12), rgba(255, 122, 33, 0.12));
  color: var(--brand-blue);
  font-weight: 800;
}

.advantage-card h3,
.scenario-card h3,
.feature-card h3,
.screenshot-card h3,
.download-side-card h3 {
  margin: 18px 0 10px;
  font-size: 22px;
}

.advantage-card p,
.scenario-card p,
.feature-card p,
.screenshot-card p,
.download-main-card p,
.download-side-card p {
  color: var(--brand-muted);
  line-height: 1.8;
}

.ai-section {
  background: linear-gradient(180deg, rgba(18, 81, 163, 0.03) 0%, rgba(255, 122, 33, 0.03) 100%);
}

.scenario-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.scenario-card {
  padding: 28px;
  border-radius: 26px;
}

.scenario-example {
  display: block;
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px dashed rgba(18, 81, 163, 0.18);
  color: var(--brand-orange);
  font-size: 14px;
  line-height: 1.7;
}

.features-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.feature-card {
  padding: 26px;
  border-radius: 24px;
}

.feature-card-top {
  display: flex;
  align-items: center;
  gap: 14px;
}

.feature-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--brand-blue), #2b79db);
  color: #fff;
  font-weight: 800;
}

.preview-section {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0) 0%, rgba(18, 81, 163, 0.03) 100%);
}

.screenshots-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.screenshot-card {
  padding: 18px;
  border-radius: 28px;
}

.screenshot-frame {
  padding: 10px;
  background: linear-gradient(180deg, #eef4ff 0%, #fff 100%);
  border-radius: 24px;
}

.screenshot-frame img {
  display: block;
  width: 100%;
  aspect-ratio: 9 / 18;
  border-radius: 18px;
  object-fit: cover;
  box-shadow: 0 14px 30px rgba(34, 62, 108, 0.14);
}

.download-section {
  padding-bottom: 96px;
}

.download-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(300px, 0.7fr);
  gap: 20px;
}

.download-main-card,
.download-side-card {
  padding: 32px;
  border-radius: 30px;
}

.download-main-card h2 {
  margin: 18px 0 14px;
  font-size: clamp(28px, 4vw, 42px);
  line-height: 1.18;
}

.download-actions {
  display: flex;
  gap: 14px;
  margin-top: 28px;
  flex-wrap: wrap;
}

.store-btn {
  display: inline-flex;
  align-items: center;
  gap: 14px;
  min-width: 220px;
  padding: 14px 18px;
  border-radius: 18px;
  background: linear-gradient(135deg, var(--brand-blue) 0%, #2a73cb 100%);
  text-decoration: none;
  color: #fff;
  box-shadow: 0 16px 28px rgba(18, 81, 163, 0.18);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.store-btn-secondary {
  background: linear-gradient(135deg, var(--brand-orange) 0%, #ff9852 100%);
}

.store-btn span {
  display: flex;
  flex-direction: column;
}

.store-btn strong {
  font-size: 16px;
}

.store-btn small {
  font-size: 12px;
  opacity: 0.82;
}

.store-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 54px;
  height: 54px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.14);
  font-size: 14px;
  font-weight: 800;
  letter-spacing: 0.06em;
}

.download-side-card {
  text-align: center;
}

.qr-shell {
  display: inline-flex;
  padding: 14px;
  border-radius: 26px;
  background: linear-gradient(135deg, rgba(18, 81, 163, 0.08), rgba(255, 122, 33, 0.08));
}

.qr-image {
  width: 220px;
  height: 220px;
  border-radius: 22px;
  background: #fff;
}

.brand-mini {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  margin-top: 18px;
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(18, 81, 163, 0.08);
}

.brand-mini img,
.footer-brand img {
  width: 44px;
  height: 32px;
  object-fit: contain;
}

.brand-mini-placeholder {
  width: 44px;
  height: 32px;
  border-radius: 10px;
  font-size: 12px;
}

.brand-mini span,
.footer-brand span {
  color: var(--brand-muted);
}

.footer {
  padding: 28px 0 42px;
  border-top: 1px solid rgba(18, 81, 163, 0.08);
}

.footer-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
}

.footer-brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
}

.footer-brand strong {
  display: block;
  font-size: 18px;
}

.footer-links {
  display: flex;
  gap: 18px;
  flex-wrap: wrap;
}

.footer-links a,
.copyright {
  color: var(--brand-muted);
  text-decoration: none;
  font-size: 14px;
}

.footer-links a:hover {
  color: var(--brand-blue);
}

@media (max-width: 1100px) {
  .hero-content,
  .download-layout {
    grid-template-columns: 1fr 1fr;
  }

  .screenshots-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .advantage-grid,
  .scenario-grid,
  .features-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .header-content,
  .section-content,
  .hero-content,
  .footer-content {
    width: min(100% - 24px, 1180px);
  }

  .header-content {
    padding: 12px 0;
    align-items: flex-start;
  }

  .brand-logo {
    width: 58px;
    height: 42px;
  }

  .brand-copy strong {
    font-size: 18px;
  }

  .nav {
    justify-content: flex-start;
  }

  .hero {
    padding: 24px 0 32px;
  }

  .hero-content,
  .advantage-grid,
  .scenario-grid,
  .features-grid,
  .screenshots-grid,
  .download-layout,
  .hero-metrics {
    grid-template-columns: 1fr;
  }

  .hero-copy {
    padding: 16px 0 0;
  }

  .hero-subtitle {
    font-size: 16px;
  }

  .hero-brand-top {
    grid-template-columns: 1fr;
  }

  .hero-brand-logo {
    width: 120px;
    height: 86px;
  }

  .hero-chat-card,
  .hero-brand-card,
  .advantage-card,
  .scenario-card,
  .feature-card,
  .screenshot-card,
  .download-main-card,
  .download-side-card {
    padding: 22px;
  }

  .section {
    padding: 30px 0 58px;
  }

  .download-actions,
  .hero-actions,
  .footer-content {
    flex-direction: column;
    align-items: stretch;
  }

  .store-btn,
  .btn {
    width: 100%;
  }

  .qr-image {
    width: min(220px, 100%);
    height: auto;
    aspect-ratio: 1;
  }

  .footer-links {
    gap: 12px;
  }
}
</style>
