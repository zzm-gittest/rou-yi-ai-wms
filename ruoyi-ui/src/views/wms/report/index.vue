<template>
  <div class="app-container report-center">
    <div class="report-header">
      <h2>报表中心</h2>
      <p>查看销售、采购、库存、财务等各类业务报表</p>
    </div>

    <!-- 报表分类 -->
    <div class="report-sections">
      <!-- 销售报表 -->
      <div class="report-section">
        <div class="section-header">
          <i class="el-icon-s-goods"></i>
          <h3>销售报表</h3>
        </div>
        <div class="report-grid">
          <div class="report-card" @click="goToReport('sales-daily')">
            <div class="card-icon" style="background: #ecf5ff;">
              <i class="el-icon-date" style="color: #409eff;"></i>
            </div>
            <div class="card-info">
              <div class="card-title">销售日报表</div>
              <div class="card-desc">按日期统计销售订单、出库、退货数据</div>
            </div>
          </div>
          <div class="report-card" @click="goToReport('goods-rank')">
            <div class="card-icon" style="background: #f0f9ff;">
              <i class="el-icon-trophy" style="color: #67c23a;"></i>
            </div>
            <div class="card-info">
              <div class="card-title">商品销售排行</div>
              <div class="card-desc">按销售额/销量/利润排名统计</div>
            </div>
          </div>
          <div class="report-card" @click="goToReport('customer-analysis')">
            <div class="card-icon" style="background: #fdf6ec;">
              <i class="el-icon-user" style="color: #e6a23c;"></i>
            </div>
            <div class="card-info">
              <div class="card-title">客户销售分析</div>
              <div class="card-desc">按客户统计销售、回款情况</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 采购报表 -->
      <div class="report-section">
        <div class="section-header">
          <i class="el-icon-shopping-cart-2"></i>
          <h3>采购报表</h3>
        </div>
        <div class="report-grid">
          <div class="report-card" @click="goToReport('supplier-analysis')">
            <div class="card-icon" style="background: #fef0f0;">
              <i class="el-icon-office-building" style="color: #f56c6c;"></i>
            </div>
            <div class="card-info">
              <div class="card-title">供应商采购分析</div>
              <div class="card-desc">按供应商统计采购、付款情况</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 库存报表 -->
      <div class="report-section">
        <div class="section-header">
          <i class="el-icon-box"></i>
          <h3>库存报表</h3>
        </div>
        <div class="report-grid">
          <div class="report-card" @click="goToReport('stock-psi')">
            <div class="card-icon" style="background: #f4f4f5;">
              <i class="el-icon-data-line" style="color: #909399;"></i>
            </div>
            <div class="card-info">
              <div class="card-title">库存进销存</div>
              <div class="card-desc">统计期初、入库、出库、期末库存</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 财务报表 -->
      <div class="report-section">
        <div class="section-header">
          <i class="el-icon-coin"></i>
          <h3>财务报表</h3>
        </div>
        <div class="report-grid">
          <div class="report-card" @click="goToReport('ar-aging')">
            <div class="card-icon" style="background: #fff7e6;">
              <i class="el-icon-warning-outline" style="color: #fa8c16;"></i>
            </div>
            <div class="card-info">
              <div class="card-title">应收账款账龄</div>
              <div class="card-desc">按账龄区间统计应收账款</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'WmsReportCenter',
  data() {
    return {}
  },
  methods: {
    goToReport(reportName) {
      this.$router.push({ path: `/wms/report/${reportName}/index` })
    }
  }
}
</script>

<style lang="scss" scoped>
.report-center {
  padding: 20px;
}

.report-header {
  text-align: center;
  margin-bottom: 40px;
  padding: 40px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  color: #fff;

  h2 {
    margin: 0 0 10px;
    font-size: 28px;
    font-weight: 500;
  }

  p {
    margin: 0;
    font-size: 14px;
    opacity: 0.9;
  }
}

.report-sections {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.report-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
  margin-bottom: 20px;

  i {
    font-size: 24px;
    color: #409eff;
    margin-right: 10px;
  }

  h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 500;
    color: #303133;
  }
}

.report-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.report-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    background: #f0f0f0;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
}

.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  flex-shrink: 0;

  i {
    font-size: 28px;
  }
}

.card-info {
  flex: 1;
}

.card-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.card-desc {
  font-size: 13px;
  color: #909399;
}
</style>
