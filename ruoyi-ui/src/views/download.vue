<template>
  <div class="download-page">
    <div class="download-container">
      <div class="logo-section">
        <span class="logo-icon">📦</span>
        <h1>羽辽进销存</h1>
        <p>进销存产品</p>
      </div>

      <div class="device-info">
        <div v-if="deviceType === 'android'" class="device-card android">
          <div class="device-icon">🤖</div>
          <h2>Android 设备</h2>
          <p>检测到您正在使用 Android 设备</p>
          <a :href="apkDownloadUrl" class="download-btn primary">
            立即下载 APK
          </a>
          <p class="file-size">文件大小: 约 15MB</p>
        </div>

        <div v-else-if="deviceType === 'ios'" class="device-card ios">
          <div class="device-icon">🍎</div>
          <h2>iOS 设备</h2>
          <p>检测到您正在使用 iOS 设备</p>
          <a :href="appStoreUrl" target="_blank" rel="noopener noreferrer" class="download-btn primary">
            App Store 下载
          </a>
          <p class="coming-soon">请通过官方 App Store 下载当前版本</p>
        </div>

        <div v-else class="device-card unknown">
          <div class="device-icon">💻</div>
          <h2>选择您的设备</h2>
          <p>无法自动识别设备类型，请手动选择</p>
          <div class="manual-select">
            <a :href="apkDownloadUrl" class="download-btn android-btn">
              <span class="btn-icon">🤖</span>
              Android 下载
            </a>
            <a :href="appStoreUrl" target="_blank" rel="noopener noreferrer" class="download-btn ios-btn">
              <span class="btn-icon">🍎</span>
              App Store
            </a>
          </div>
        </div>
      </div>

      <div class="features">
        <div class="feature-item">
          <span class="feature-icon">🎤</span>
          <span>语音录入</span>
        </div>
        <div class="feature-item">
          <span class="feature-icon">💬</span>
          <span>智能问答</span>
        </div>
        <div class="feature-item">
          <span class="feature-icon">📊</span>
          <span>数据分析</span>
        </div>
      </div>

      <div class="footer-links">
        <router-link to="/">返回官网</router-link>
        <span class="divider">|</span>
        <a href="/static/privacy.html" target="_blank">隐私政策</a>
      </div>
    </div>
  </div>
</template>

<script>
const APP_STORE_URL = 'https://apps.apple.com/app/id6761791831'

export default {
  name: 'DownloadPage',
  data() {
    return {
      deviceType: 'unknown', // android, ios, unknown
      apkDownloadUrl: `${process.env.VUE_APP_BASE_API || ''}/app/download`,
      appStoreUrl: APP_STORE_URL
    }
  },
  mounted() {
    this.detectDevice()
    // 如果是 Android，自动开始下载
    if (this.deviceType === 'android') {
      // 延迟1秒后自动下载，给用户看到页面的时间
      setTimeout(() => {
        // window.location.href = this.apkDownloadUrl
      }, 1000)
    }
  },
  methods: {
    detectDevice() {
      const ua = navigator.userAgent.toLowerCase()

      // 检测 iOS
      if (/iphone|ipad|ipod/.test(ua)) {
        this.deviceType = 'ios'
      }
      // 检测 Android
      else if (/android/.test(ua)) {
        this.deviceType = 'android'
      }
      // 检测 Windows/Mac/Linux (显示手动选择)
      else {
        this.deviceType = 'unknown'
      }
    }
  }
}
</script>

<style lang="scss" scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.download-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.download-container {
  background: white;
  border-radius: 24px;
  padding: 40px 30px;
  max-width: 400px;
  width: 100%;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.logo-section {
  margin-bottom: 40px;
}

.logo-icon {
  font-size: 64px;
  display: block;
  margin-bottom: 16px;
}

.logo-section h1 {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 8px;
}

.logo-section p {
  color: #6b7280;
  font-size: 14px;
}

.device-card {
  padding: 30px;
  border-radius: 16px;
  margin-bottom: 30px;
}

.device-card.android {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
}

.device-card.ios {
  background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
}

.device-card.unknown {
  background: #f3f4f6;
}

.device-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.device-card h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 8px;
}

.device-card p {
  color: #4b5563;
  font-size: 14px;
  margin-bottom: 20px;
}

.download-btn {
  display: inline-block;
  padding: 14px 40px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.3s;
}

.download-btn.primary {
  background: #1f2937;
  color: white;
}

.download-btn.primary:hover {
  transform: scale(1.05);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.file-size {
  color: #6b7280;
  font-size: 12px;
  margin-top: 12px;
}

.coming-soon {
  color: #f59e0b;
  font-size: 14px;
  margin-top: 12px;
}

.manual-select {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.download-btn.android-btn,
.download-btn.ios-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 20px;
  border-radius: 12px;
  font-weight: 600;
  text-decoration: none;
}

.download-btn.android-btn {
  background: #3ddc84;
  color: #1f2937;
}

.download-btn.ios-btn {
  background: #007aff;
  color: white;
}

.btn-icon {
  font-size: 20px;
}

.features {
  display: flex;
  justify-content: center;
  gap: 30px;
  padding: 20px 0;
  border-top: 1px solid #e5e7eb;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.feature-icon {
  font-size: 28px;
}

.feature-item span:last-child {
  font-size: 12px;
  color: #6b7280;
}

.footer-links {
  margin-top: 20px;
  font-size: 14px;
}

.footer-links a,
.footer-links span {
  color: #6b7280;
  text-decoration: none;
}

.footer-links a:hover {
  color: #1890ff;
}

.divider {
  margin: 0 8px;
}
</style>
