<template>
  <div class="app-container ai-pc-page">
    <div class="ai-shell">
      <aside class="sidebar">
        <button class="new-chat-btn" :disabled="sending || queryLoading" @click="startNewConversation">+ 新建对话</button>
        <div class="session-list">
          <div v-for="group in groupedConversations" :key="group.key" class="session-group">
            <div class="group-title">{{ group.label }}</div>
            <button
              v-for="item in group.items"
              :key="item.conversationId"
              :class="['session-item', { active: item.conversationId === activeConversationId }]"
              @click="activateConversation(item.conversationId)"
            >
              <div class="session-main">
                <div class="session-title">{{ item.title }}</div>
                <div class="session-summary">{{ item.summary || '暂无摘要' }}</div>
              </div>
              <div class="session-meta">{{ formatRelativeTime(item.updatedAt) }}</div>
            </button>
          </div>
          <div v-if="!conversations.length" class="empty-history">还没有历史对话，先新建一条。</div>
        </div>
      </aside>

      <section class="chat-panel">
        <header class="chat-header">
          <div>
            <div class="chat-title">{{ currentTitle }}</div>
            <div class="chat-subtitle">{{ currentSubtitle }}</div>
          </div>
        </header>

        <div class="message-wrap">
          <div
            class="chat-list"
            ref="chatList"
            v-loading="queryLoading"
            :element-loading-text="queryLoadingText"
            element-loading-spinner="el-icon-loading"
            element-loading-background="rgba(255, 255, 255, 0.72)"
            @scroll="handleChatScroll"
          >
            <div v-for="(msg, idx) in messages" :key="idx" :class="['msg-row', msg.role]">
              <div :class="['avatar', msg.role]">
                <img v-if="msg.role === 'user'" :src="avatar" alt="用户头像" class="avatar-image">
                <img v-else :src="aiLogo" alt="智存AI" class="avatar-image">
              </div>
              <div :class="['bubble', { wide: msg.card && ['DIFF_PREVIEW', 'EXEC_RESULT', 'GOODS_SKU_FORM', 'ANALYSIS_BRIEFING', 'ANALYSIS_RANKING'].includes(msg.card.type) }]">
                <div class="text">{{ msg.text }}</div>
                <div v-if="msg.imageUrl" class="msg-image-wrap">
                  <img :src="msg.imageUrl" class="msg-image" @click="previewMsgImage(msg.imageUrl)" />
                </div>

                <div v-if="msg.card && msg.card.type === 'ACTION_LIST'" class="card-block">
                  <div class="card-title">可直接发起的操作</div>
                  <div class="chip-list">
                    <button
                      v-for="(action, aIdx) in msg.card.actions || []"
                      :key="'action-' + aIdx"
                      class="message-chip"
                      :disabled="isHistoryMessage(idx) || sending"
                      @click="send(action.value || action.name, action.name)"
                    >{{ action.name }}</button>
                  </div>
                </div>

                <div v-if="msg.card && msg.card.type === 'TEMPLATE'" class="card-block">
                  <div class="card-title">{{ msg.card.title }}</div>
                  <div
                    v-for="(tpl, tIdx) in msg.card.templates || []"
                    :key="'tpl-' + tIdx"
                    class="template-item"
                    :class="{ disabled: isHistoryMessage(idx) || sending }"
                  >
                    <button
                      class="template-main"
                      :disabled="isHistoryMessage(idx) || sending"
                      @click="applyTemplateDraft(tpl)"
                    >{{ templateLabel(tpl) }}</button>
                    <el-button
                      class="template-copy-btn"
                      type="text"
                      icon="el-icon-document-copy"
                      :disabled="isHistoryMessage(idx) || sending"
                      @click.stop="copyTemplateText(tpl)"
                    >复制</el-button>
                  </div>
                </div>

                <div v-else-if="msg.card && msg.card.type === 'CANDIDATE'" class="card-block">
                  <div class="card-title">{{ msg.card.title }}</div>
                  <div v-if="msg.card.selectionMode === 'single-list' || msg.card.selectionMode === 'multi-list'" class="candidate-panel">
                    <div class="candidate-panel-meta">
                      <span>共 {{ candidateDisplayOptions(msg.card).length }} / {{ msg.card.totalCount || 0 }} 条</span>
                      <span v-if="(msg.card.totalPages || 1) > 1">第 {{ msg.card.page || 1 }} / {{ msg.card.totalPages || 1 }} 页</span>
                      <span v-if="(msg.card.totalPages || 1) > 1" class="candidate-page-hint">{{ buildCandidatePager(msg.card) }}</span>
                      <span v-if="isMultiCandidateCard(msg.card)">已临时勾选 {{ candidateSelectedCount(msg.card) }} 条</span>
                    </div>
                    <el-input
                      v-if="msg.card.allowSearch"
                      v-model="msg.card.searchKeyword"
                      size="mini"
                      clearable
                      :placeholder="msg.card.remoteSearch ? '输入单据号或供应商名称搜索' : '输入单据号或供应商名称过滤'"
                      class="candidate-search"
                      @input="handleCandidateSearchInput(msg)"
                    />
                    <el-checkbox-group
                      v-if="isMultiCandidateCard(msg.card)"
                      v-model="msg.card.selectedSkuIds"
                      class="candidate-check-group"
                      @change="handleCandidateSelectionChange(msg.card)"
                    >
                      <label
                        v-for="(opt, oIdx) in candidateDisplayOptions(msg.card)"
                        :key="'candidate-' + oIdx"
                        :class="['candidate-item', 'selectable', { disabled: isHistoryMessage(idx) || sending }]"
                      >
                        <el-checkbox :label="candidateOptionId(opt)" :disabled="isHistoryMessage(idx) || sending">
                          <span class="candidate-index">{{ candidateIndex(msg.card, oIdx) }}</span>
                          <span class="candidate-label">{{ candidateOptionLabel(opt) }}</span>
                        </el-checkbox>
                        <el-button
                          v-if="canViewCandidateDetail(msg.card, opt)"
                          type="text"
                          size="mini"
                          class="candidate-detail-btn"
                          :disabled="isHistoryMessage(idx) || sending"
                          @click.stop="openCandidateDetail(msg.card, opt)"
                        >详情</el-button>
                      </label>
                    </el-checkbox-group>
                    <el-radio-group v-else v-model="msg.card.selectedValue" class="candidate-radio-group">
                      <label
                        v-for="(opt, oIdx) in candidateDisplayOptions(msg.card)"
                        :key="'candidate-' + oIdx"
                        :class="['candidate-item', 'selectable', { disabled: isHistoryMessage(idx) || sending }]"
                      >
                        <el-radio :label="opt.value" :disabled="isHistoryMessage(idx) || sending">
                          <span class="candidate-index">{{ candidateIndex(msg.card, oIdx) }}</span>
                          <span class="candidate-label">{{ candidateOptionLabel(opt) }}</span>
                        </el-radio>
                        <el-button
                          v-if="canViewCandidateDetail(msg.card, opt)"
                          type="text"
                          size="mini"
                          class="candidate-detail-btn"
                          :disabled="isHistoryMessage(idx) || sending"
                          @click.stop="openCandidateDetail(msg.card, opt)"
                        >详情</el-button>
                      </label>
                    </el-radio-group>
                    <div class="candidate-toolbar">
                      <el-button
                        v-if="isMultiCandidateCard(msg.card)"
                        size="mini"
                        plain
                        :disabled="isHistoryMessage(idx) || sending || !hasCurrentPageCandidate(msg.card)"
                        @click="selectAllCurrentPage(msg)"
                      >当前页全选</el-button>
                      <el-button
                        v-if="isMultiCandidateCard(msg.card)"
                        size="mini"
                        plain
                        :disabled="isHistoryMessage(idx) || sending || !hasCurrentPageSelected(msg.card)"
                        @click="clearCurrentPageSelection(msg)"
                      >清空当前页</el-button>
                      <el-button
                        v-if="isMultiCandidateCard(msg.card)"
                        size="mini"
                        plain
                        :disabled="isHistoryMessage(idx) || sending || !candidateSelectedCount(msg.card)"
                        @click="clearAllCandidateSelection(msg)"
                      >清空已选</el-button>
                      <el-button v-if="msg.card.hasPrev || msg.card.hasNext" size="mini" plain :disabled="isHistoryMessage(idx) || sending || !msg.card.hasPrev" @click="sendCandidatePaging('上一页')">上一页</el-button>
                      <el-button v-if="msg.card.hasPrev || msg.card.hasNext" size="mini" plain :disabled="isHistoryMessage(idx) || sending || !msg.card.hasNext" @click="sendCandidatePaging('下一页')">下一页</el-button>
                      <el-button
                        v-if="msg.card.otherOption"
                        size="mini"
                        plain
                        :disabled="isHistoryMessage(idx) || sending"
                        @click="send(msg.card.otherOption.value, msg.card.otherOption.label)"
                      >{{ msg.card.otherOption.label }}</el-button>
                      <el-button
                        type="primary"
                        size="mini"
                        :disabled="isHistoryMessage(idx) || sending || !canConfirmCandidate(msg.card)"
                        @click="confirmCandidateSelection(msg)"
                      >确定</el-button>
                    </div>
                  </div>
                  <div v-else>
                    <div v-if="msg.card.options && msg.card.options.length" class="candidate-list">
                      <div v-for="(opt, oIdx) in msg.card.options || []" :key="'candidate-' + oIdx" class="candidate-item">
                        <span class="candidate-index">{{ oIdx + 1 }}</span>
                        <span class="candidate-label">{{ candidateOptionLabel(opt) }}</span>
                      </div>
                    </div>
                    <div class="chip-list candidate-actions">
                      <button
                        v-for="(opt, oIdx) in msg.card.options || []"
                        :key="'opt-' + oIdx"
                        class="message-chip"
                        :disabled="isHistoryMessage(idx) || sending"
                        @click="send(opt.value || opt.label, opt.label)"
                      >{{ opt.label }}</button>
                    </div>
                  </div>
                </div>

                <div v-else-if="msg.card && msg.card.type === 'GOODS_CREATE_FORM'" class="card-block form-card goods-flow-card">
                  <div class="card-title">{{ msg.card.title }}</div>
                  <el-form label-position="top" size="mini">
                    <el-form-item label="商品名称 *">
                      <el-input v-model="msg.card.formModel.goodsName" placeholder="请输入商品名称" :disabled="isHistoryMessage(idx) || sending" />
                    </el-form-item>
                    <el-form-item label="单位">
                      <div class="inline-select-action">
                        <el-select
                          v-model="msg.card.formModel.unitName"
                          filterable
                          clearable
                          placeholder="请选择单位"
                          style="width: 100%"
                          :disabled="isHistoryMessage(idx) || sending"
                        >
                          <el-option
                            v-for="unit in unitOptions"
                            :key="'unit-' + unit.id"
                            :label="unit.unitName"
                            :value="unit.unitName"
                          />
                        </el-select>
                        <el-button size="mini" plain :disabled="isHistoryMessage(idx) || sending" @click="addInlineUnit(msg)">新增</el-button>
                      </div>
                    </el-form-item>
                    <el-form-item label="分类 *">
                      <div class="inline-select-action">
                        <el-select
                          v-model="msg.card.formModel.categoryId"
                          filterable
                          clearable
                          placeholder="请选择分类"
                          style="width: 100%"
                          :disabled="isHistoryMessage(idx) || sending"
                          @change="handleGoodsCategoryChange(msg, $event)"
                        >
                          <el-option
                            v-for="category in categoryOptions"
                            :key="'category-' + category.id"
                            :label="category.categoryName"
                            :value="category.id"
                          />
                        </el-select>
                        <el-button size="mini" plain :disabled="isHistoryMessage(idx) || sending" @click="addInlineCategory(msg)">新增</el-button>
                      </div>
                    </el-form-item>
                    <div class="form-actions">
                      <el-button size="mini" type="primary" :disabled="isHistoryMessage(idx) || sending" @click="submitForm(msg, 'SAVE_NO_SPEC', { displayText: '保存无规格商品' })">保存</el-button>
                      <el-button size="mini" plain :disabled="isHistoryMessage(idx) || sending" @click="submitForm(msg, 'GO_MULTI_SPEC', { displayText: '进入多规格设置' })">多规格设置</el-button>
                    </div>
                  </el-form>
                </div>

                <div v-else-if="msg.card && msg.card.type === 'GOODS_SPEC_FORM'" class="card-block form-card goods-flow-card">
                  <div class="card-title">{{ msg.card.title }}</div>
                  <div class="goods-flow-meta">商品：{{ msg.card.formModel.goodsName || '-' }} / 单位：{{ msg.card.formModel.unitName || '个' }}</div>
                  <div class="spec-editor">
                    <div v-for="(spec, specIndex) in msg.card.formModel.specs" :key="'spec-' + specIndex" class="spec-editor-item">
                      <div class="spec-editor-head">
                        <el-input v-model="spec.specName" placeholder="规格名称，如：颜色、尺寸" size="mini" :disabled="isHistoryMessage(idx) || sending" />
                        <el-button type="text" :disabled="isHistoryMessage(idx) || sending" @click="removeSpecRow(msg, specIndex)">删除规格</el-button>
                      </div>
                      <div v-for="(value, valueIndex) in spec.values" :key="'spec-value-' + specIndex + '-' + valueIndex" class="spec-value-row">
                        <el-input v-model="value.specValue" placeholder="规格值，如：红色、L" size="mini" :disabled="isHistoryMessage(idx) || sending" />
                        <el-button type="text" :disabled="isHistoryMessage(idx) || sending" @click="removeSpecValue(msg, specIndex, valueIndex)">删除</el-button>
                      </div>
                      <el-button size="mini" plain icon="el-icon-plus" :disabled="isHistoryMessage(idx) || sending" @click="addSpecValue(msg, specIndex)">添加规格值</el-button>
                    </div>
                    <el-button size="mini" type="primary" plain icon="el-icon-plus" :disabled="isHistoryMessage(idx) || sending" @click="addSpecRow(msg)">新增规格</el-button>
                  </div>
                  <div class="form-actions">
                    <el-button size="mini" type="primary" :disabled="isHistoryMessage(idx) || sending" @click="submitForm(msg, 'GO_SKU_LIST', { displayText: '生成SKU列表' })">下一步</el-button>
                    <el-button
                      size="mini"
                      plain
                      :disabled="isHistoryMessage(idx) || sending"
                      @click="submitForm(msg, 'BACK_TO_NO_SPEC', { confirmMessage: '当前多规格未保存，返回将按无规格商品保存，是否继续？', displayText: '返回并按无规格保存' })"
                    >返回</el-button>
                  </div>
                </div>

                <div v-else-if="msg.card && msg.card.type === 'GOODS_SKU_FORM'" class="card-block form-card goods-flow-card">
                  <div class="card-title">{{ msg.card.title }}</div>
                  <div class="goods-flow-meta">商品：{{ msg.card.formModel.goodsName || '-' }} / 单位：{{ msg.card.formModel.unitName || '个' }}</div>
                  <el-table :data="msg.card.formModel.skuList || []" size="mini" border class="sku-chat-table">
                    <el-table-column label="规格组合" min-width="180">
                      <template slot-scope="scope">
                        <span>{{ formatSpecDisplay(scope.row.specInfo) }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column label="SKU名称" min-width="180">
                      <template slot-scope="scope">
                        <el-input v-model="scope.row.skuName" size="mini" placeholder="请输入SKU名称" :disabled="isHistoryMessage(idx) || sending" />
                      </template>
                    </el-table-column>
                    <el-table-column label="单位" width="90">
                      <template slot-scope="scope">
                        <span>{{ scope.row.unitName || msg.card.formModel.unitName || '个' }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column label="数量" width="120">
                      <template slot-scope="scope">
                        <el-input
                          :value="scope.row.quantity"
                          size="mini"
                          placeholder="0"
                          inputmode="decimal"
                          :disabled="isHistoryMessage(idx) || sending"
                          @input="handleSkuNumberInput(scope.row, 'quantity', $event)"
                        />
                      </template>
                    </el-table-column>
                    <el-table-column label="采购单价" width="140">
                      <template slot-scope="scope">
                        <el-input
                          :value="scope.row.price"
                          size="mini"
                          placeholder="0.00"
                          inputmode="decimal"
                          :disabled="isHistoryMessage(idx) || sending"
                          @input="handleSkuNumberInput(scope.row, 'price', $event)"
                        />
                      </template>
                    </el-table-column>
                    <el-table-column label="操作" width="80">
                      <template slot-scope="scope">
                        <el-button type="text" size="mini" :disabled="isHistoryMessage(idx) || sending" @click="removeSkuRow(msg, scope.$index)">删除</el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                  <div class="form-actions">
                    <el-button size="mini" type="primary" :disabled="isHistoryMessage(idx) || sending" @click="submitForm(msg, 'SAVE_MULTI_SPEC', { displayText: '保存多规格商品' })">保存</el-button>
                    <el-button
                      size="mini"
                      plain
                      :disabled="isHistoryMessage(idx) || sending"
                      @click="submitForm(msg, 'BACK_TO_NO_SPEC', { confirmMessage: '当前多规格未保存，返回将按无规格商品保存，是否继续？', displayText: '返回并按无规格保存' })"
                    >返回</el-button>
                  </div>
                </div>

                <div v-else-if="msg.card && msg.card.type === 'REQUIRED_FORM'" class="card-block form-card">
                  <div class="card-title">{{ msg.card.title }}</div>
                  <el-form label-position="top" size="mini">
                    <el-form-item
                      v-for="(field, fIdx) in msg.card.fields || []"
                      :key="'field-' + fIdx"
                      :label="field.label + (field.required ? ' *' : '')"
                    >
                      <div v-if="field.component === 'readonly'" class="readonly-field">{{ msg.card.formModel[field.key] || '-' }}</div>
                      <el-select
                        v-else-if="field.component === 'select'"
                        v-model="msg.card.formModel[field.key]"
                        filterable
                        clearable
                        :placeholder="field.placeholder || ('请选择' + field.label)"
                        style="width: 100%"
                      >
                        <el-option
                          v-for="option in getFormFieldOptions(field)"
                          :key="field.key + '-' + option.value"
                          :label="option.label"
                          :value="option.value"
                        />
                      </el-select>
                      <el-date-picker
                        v-else-if="field.component === 'date'"
                        v-model="msg.card.formModel[field.key]"
                        type="date"
                        value-format="yyyy-MM-dd"
                        :placeholder="field.placeholder || '请选择日期'"
                        style="width: 100%"
                      />
                      <el-input
                        v-else
                        v-model="msg.card.formModel[field.key]"
                        :placeholder="field.placeholder || ''"
                        :type="field.key === 'items' ? 'textarea' : 'text'"
                        :rows="field.key === 'items' ? 3 : undefined"
                        resize="none"
                      />
                    </el-form-item>
                    <div class="form-actions">
                      <el-button size="mini" type="primary" @click="submitForm(msg)">{{ msg.card.submitLabel || '提交' }}</el-button>
                      <el-button v-if="msg.card.cancelAction" size="mini" plain @click="send(msg.card.cancelAction, msg.card.cancelLabel || '取消')">{{ msg.card.cancelLabel || '取消' }}</el-button>
                    </div>
                  </el-form>
                </div>

                <div v-if="msg.card && msg.card.type === 'TABLE'" class="card-block table-card">
                  <div class="card-title">{{ msg.card.title }}</div>
                  <el-table :data="msg.card.rows" size="mini" border>
                    <el-table-column label="单据类型" min-width="120">
                      <template slot-scope="scope">
                        <span>{{ scope.row.domainTypeName || formatDomainType(scope.row.domainType) }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column prop="billNo" label="单据号" min-width="160" />
                    <el-table-column label="往来方" min-width="140">
                      <template slot-scope="scope">
                        <span>{{ scope.row.partnerName || scope.row.supplierName || '-' }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column label="状态" width="100">
                      <template slot-scope="scope">
                        <span>{{ formatQueryStatus(scope.row) }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column prop="bizDate" label="业务日期" width="120" />
                    <el-table-column prop="totalAmount" label="金额" width="120">
                      <template slot-scope="scope">
                        <span>{{ scope.row.totalAmount === null || scope.row.totalAmount === undefined ? '-' : formatCurrency(scope.row.totalAmount) }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column label="操作" width="70" fixed="right">
                      <template slot-scope="scope">
                        <el-button type="text" size="mini" :disabled="sending || queryLoading" @click="viewBillDetail(scope.row)">详情</el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>

                <div v-else-if="msg.card && msg.card.type === 'DIFF_PREVIEW'" class="card-block preview-card">
                  <div class="card-title">{{ msg.card.title }}</div>
                  <div class="preview-meta">
                    <span v-if="msg.card.supplier">供应商：{{ msg.card.supplier }}</span>
                    <span v-if="msg.card.customer">客户：{{ msg.card.customer }}</span>
                    <span v-if="msg.card.warehouse">仓库：{{ msg.card.warehouse }}</span>
                    <span v-if="msg.card.outWarehouse">调出仓：{{ msg.card.outWarehouse }}</span>
                    <span v-if="msg.card.inWarehouse">调入仓：{{ msg.card.inWarehouse }}</span>
                    <span v-if="msg.card.checkType">盘点类型：{{ formatCheckType(msg.card.checkType) }}</span>
                    <span v-if="msg.card.linkOrderNo">来源单号：{{ msg.card.linkOrderNo }}</span>
                  </div>
                  <div v-if="msg.card.editable" class="preview-edit-tip">{{ previewTipText(msg.card) }}</div>
                  <div v-if="msg.card.editable && msg.card.allowSupplierEdit" class="preview-inline-form">
                    <div class="inline-select-action">
                      <el-select
                        v-model="msg.card.formModel.supplierId"
                        filterable
                        clearable
                        placeholder="请选择供应商"
                        style="width: 100%"
                        :disabled="isHistoryMessage(idx) || sending"
                      >
                        <el-option
                          v-for="item in supplierOptions"
                          :key="'preview-supplier-' + item.id"
                          :label="item.supplierName"
                          :value="item.id"
                        />
                      </el-select>
                      <el-button
                        size="mini"
                        plain
                        :disabled="isHistoryMessage(idx) || sending"
                        @click="addInlinePreviewSupplier(msg)"
                      >新增供应商</el-button>
                    </div>
                  </div>
                  <div v-if="msg.card.editable && msg.card.allowWarehouseEdit" class="preview-inline-form preview-inline-form-warehouse">
                    <div class="inline-select-action">
                      <el-select
                        v-model="msg.card.formModel.warehouseId"
                        filterable
                        clearable
                        placeholder="请选择仓库"
                        style="width: 100%"
                        :disabled="isHistoryMessage(idx) || sending"
                      >
                        <el-option
                          v-for="item in warehouseOptions"
                          :key="'preview-warehouse-' + item.id"
                          :label="item.warehouseName"
                          :value="item.id"
                        />
                      </el-select>
                      <el-button
                        size="mini"
                        plain
                        :disabled="isHistoryMessage(idx) || sending"
                        @click="addInlinePreviewWarehouse(msg)"
                      >新增仓库</el-button>
                    </div>
                  </div>
                  <div v-if="msg.card.editable && msg.card.allowCustomerEdit" class="preview-inline-form preview-inline-form-customer">
                    <div class="inline-select-action">
                      <el-select
                        v-model="msg.card.formModel.customerId"
                        filterable
                        clearable
                        placeholder="请选择客户"
                        style="width: 100%"
                        :disabled="isHistoryMessage(idx) || sending"
                      >
                        <el-option
                          v-for="item in customerOptions"
                          :key="'preview-customer-' + item.id"
                          :label="item.customerName"
                          :value="item.id"
                        />
                      </el-select>
                      <el-button
                        size="mini"
                        plain
                        :disabled="isHistoryMessage(idx) || sending"
                        @click="addInlinePreviewCustomer(msg)"
                      >新增客户</el-button>
                    </div>
                  </div>
                  <div v-if="msg.card.editable && msg.card.allowRemarkEdit" class="preview-inline-form preview-inline-form-remark">
                    <el-input
                      v-model="msg.card.formModel.remark"
                      size="mini"
                      placeholder="请输入备注（必填）"
                      :disabled="isHistoryMessage(idx) || sending"
                    />
                  </div>
                  <div v-if="msg.card.editable && msg.card.allowInWarehouseEdit" class="preview-inline-form preview-inline-form-warehouse">
                    <div class="inline-select-action">
                      <el-select
                        v-model="msg.card.formModel.inWarehouseId"
                        filterable
                        placeholder="请选择调入仓库"
                        style="width: 100%"
                        :disabled="isHistoryMessage(idx) || sending"
                      >
                        <el-option
                          v-for="item in warehouseOptions.filter(w => Number(w.id) !== Number(msg.card.formModel.outWarehouseId))"
                          :key="'preview-in-warehouse-' + item.id"
                          :label="item.warehouseName"
                          :value="item.id"
                        />
                      </el-select>
                    </div>
                  </div>
                  <el-table :data="msg.card.editable ? ((msg.card.formModel && msg.card.formModel.items) || []) : (msg.card.items || [])" size="mini" border>
                    <el-table-column prop="goods" label="商品" min-width="120" />
                    <el-table-column prop="sku" label="规格" min-width="120" />
                    <el-table-column prop="unit" label="单位" width="120">
                      <template slot-scope="scope">
                        <el-select
                          v-if="msg.card.editable && ['PURCHASE_ORDER', 'PO'].includes(msg.card.domainType)"
                          v-model="scope.row.unit"
                          filterable
                          allow-create
                          default-first-option
                          clearable
                          placeholder="单位可选"
                          size="mini"
                          :disabled="isHistoryMessage(idx) || sending"
                        >
                          <el-option
                            v-for="unit in unitOptions"
                            :key="'preview-unit-' + unit.id"
                            :label="unit.unitName"
                            :value="unit.unitName"
                          />
                        </el-select>
                        <span v-else>{{ scope.row.unit || '-' }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column v-if="msg.card.domainType === 'PURCHASE_IN' && msg.card.linkedMode" prop="orderQuantity" label="订单数量" width="100">
                      <template slot-scope="scope">
                        <span>{{ scope.row.orderQuantity || 0 }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column v-if="msg.card.domainType === 'PURCHASE_IN' && msg.card.linkedMode" prop="receivedQty" label="已入库数量" width="100">
                      <template slot-scope="scope">
                        <span>{{ scope.row.receivedQty || 0 }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column v-if="msg.card.domainType === 'PURCHASE_IN' && msg.card.linkedMode" prop="maxQty" label="剩余最大入库数量" width="130">
                      <template slot-scope="scope">
                        <span>{{ scope.row.maxQty || 0 }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column v-if="msg.card.domainType === 'SALES_OUT' && msg.card.linkedMode" prop="orderQuantity" label="订单数量" width="100">
                      <template slot-scope="scope">
                        <span>{{ scope.row.orderQuantity || 0 }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column v-if="msg.card.domainType === 'SALES_OUT' && msg.card.linkedMode" prop="receivedQty" label="已出库数量" width="100">
                      <template slot-scope="scope">
                        <span>{{ scope.row.receivedQty || 0 }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column v-if="msg.card.domainType === 'SALES_OUT' && msg.card.linkedMode" prop="maxQty" label="可出库数量" width="110">
                      <template slot-scope="scope">
                        <span>{{ scope.row.maxQty || 0 }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column v-if="msg.card.domainType === 'STOCK_TRANSFER'" prop="maxQty" label="库存数量" width="100">
                      <template slot-scope="scope">
                        <span>{{ scope.row.maxQty || 0 }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column v-if="msg.card.domainType === 'STOCK_TRANSFER'" prop="price" label="成本价" width="100">
                      <template slot-scope="scope">
                        <span>{{ formatCurrency(scope.row.price) }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column prop="quantity" :label="msg.card.domainType === 'STOCK_CHECK' ? '实际数量' : '数量'" width="120">
                      <template slot-scope="scope">
                        <el-input
                          v-if="msg.card.editable"
                          :value="scope.row.quantity"
                          size="mini"
                          placeholder="0"
                          inputmode="decimal"
                          :disabled="isHistoryMessage(idx) || sending"
                          @input="handlePreviewNumberInput(scope.row, 'quantity', $event)"
                        />
                        <span v-else>{{ scope.row.quantity }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column v-if="msg.card.domainType === 'STOCK_CHECK'" prop="beforeQty" label="账面数" width="100" />
                    <el-table-column v-if="msg.card.domainType === 'STOCK_CHECK'" prop="price" label="成本价" width="110">
                      <template slot-scope="scope">
                        <el-input
                          v-if="msg.card.editable"
                          :value="scope.row.price"
                          size="mini"
                          placeholder="0.00"
                          inputmode="decimal"
                          :disabled="isHistoryMessage(idx) || sending"
                          @input="handlePreviewNumberInput(scope.row, 'price', $event)"
                        />
                        <span v-else>{{ formatCurrency(scope.row.price) }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column v-if="msg.card.domainType === 'STOCK_CHECK'" label="差异数量" width="100" align="right">
                      <template slot-scope="scope">
                        <span :style="{ color: previewRowDiffQty(scope.row) > 0 ? 'green' : (previewRowDiffQty(scope.row) < 0 ? 'red' : '') }">
                          {{ previewRowDiffQty(scope.row) }}
                        </span>
                      </template>
                    </el-table-column>
                    <el-table-column v-if="msg.card.editable && msg.card.linkedMode && msg.card.domainType !== 'STOCK_CHECK' && !['PURCHASE_ORDER', 'PO', 'PURCHASE_IN', 'PI', 'SALES_OUT'].includes(msg.card.domainType)" prop="maxQty" :label="['PURCHASE_RETURN', 'SALES_RETURN'].includes(msg.card.domainType) ? '可退货数量' : '可用上限'" width="110" />
                    <el-table-column v-if="!['STOCK_CHECK', 'STOCK_TRANSFER'].includes(msg.card.domainType)" prop="price" label="单价" width="110">
                      <template slot-scope="scope">
                        <el-input
                          v-if="msg.card.editable && msg.card.allowPriceEdit"
                          :value="scope.row.price"
                          size="mini"
                          placeholder="0.00"
                          inputmode="decimal"
                          :disabled="isHistoryMessage(idx) || sending"
                          @input="handlePreviewNumberInput(scope.row, 'price', $event)"
                        />
                        <span v-else>{{ formatCurrency(scope.row.price) }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column v-if="!['STOCK_CHECK', 'STOCK_TRANSFER'].includes(msg.card.domainType)" prop="amount" label="金额" width="120">
                      <template slot-scope="scope">
                        <span>{{ formatCurrency(previewRowAmount(scope.row)) }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column v-if="msg.card.editable" label="操作" width="80">
                      <template slot-scope="scope">
                        <el-button type="text" size="mini" :disabled="isHistoryMessage(idx) || sending" @click="removePreviewRow(msg, scope.$index)">删除</el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                  <div v-if="msg.card.editable" class="form-actions">
                    <el-button size="mini" type="primary" :disabled="isHistoryMessage(idx) || sending" @click="confirmPreviewAndCreate(msg)">确认</el-button>
                    <el-button size="mini" plain :disabled="isHistoryMessage(idx) || sending" @click="cancelPreviewCreate(msg)">取消</el-button>
                    <el-button v-if="msg.card.nextAction" size="mini" plain :disabled="isHistoryMessage(idx) || sending" @click="submitPreviewAndContinue(msg)">{{ msg.card.nextLabel || '下一步' }}</el-button>
                  </div>
                </div>

                <div v-else-if="msg.card && msg.card.type === 'EXEC_RESULT'" class="card-block result-card">
                  <div class="result-head">
                    <div class="card-title">执行结果</div>
                    <div v-if="supportsResultActions(msg.card)" class="result-actions">
                      <el-button size="mini" plain :disabled="isHistoryMessage(idx) || !canEditResult(msg.card)" @click="openOrderEditor(msg)">修改</el-button>
                      <el-button size="mini" plain :disabled="isHistoryMessage(idx) || !canApproveResult(msg.card)" @click="approveResultOrder(msg)">审核</el-button>
                      <el-button size="mini" plain :disabled="isHistoryMessage(idx) || !canPrintResult(msg.card)" @click="printResultOrder(msg.card)">打印</el-button>
                      <el-button size="mini" plain :disabled="isHistoryMessage(idx) || !canDeleteResult(msg.card)" @click="deleteResultOrder(msg)">删除</el-button>
                    </div>
                  </div>
                  <div class="result-grid">
                    <div>类型：{{ formatDomainType(msg.card.domainType) }}</div>
                    <div v-if="msg.card.orderNo">单号：{{ msg.card.orderNo }}</div>
                    <div v-if="msg.card.supplierName">供应商：{{ msg.card.supplierName }}</div>
                    <div v-if="msg.card.customerName">客户：{{ msg.card.customerName }}</div>
                    <div v-if="msg.card.partner">对象：{{ msg.card.partner }}</div>
                    <div v-if="msg.card.warehouse">仓库：{{ msg.card.warehouse }}</div>
                    <div v-if="msg.card.checkType">盘点类型：{{ formatCheckType(msg.card.checkType) }}</div>
                    <div v-if="msg.card.sourceNo">来源单号：{{ msg.card.sourceNo }}</div>
                    <div v-if="msg.card.orderDate">下单日期：{{ msg.card.orderDate }}</div>
                    <div v-if="msg.card.totalAmount !== undefined">总金额：{{ formatCurrency(msg.card.totalAmount) }}</div>
                    <div v-if="msg.card.billNo">单号：{{ msg.card.billNo }}</div>
                    <div v-if="msg.card.orderStatus !== undefined">状态：{{ formatOrderStatus(msg.card.orderStatus) }}</div>
                  </div>
                  <el-table v-if="msg.card.items && msg.card.items.length" :data="msg.card.items" size="mini" border show-summary :summary-method="param => getExecResultSummary(param, msg.card)">
                    <el-table-column label="商品" min-width="200">
                      <template slot-scope="scope">
                        <span>{{ formatGoodsDisplay({ goodsName: scope.row.goods, skuName: scope.row.sku }) }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column prop="unit" label="单位" width="80" />
                    <el-table-column v-if="msg.card.domainType === 'STOCK_CHECK'" prop="beforeQty" label="账面数" width="90" />
                    <el-table-column prop="quantity" :label="msg.card.domainType === 'STOCK_CHECK' ? '实际数' : '数量'" width="90" />
                    <el-table-column v-if="msg.card.domainType === 'STOCK_CHECK'" prop="price" label="成本价" width="100">
                      <template slot-scope="scope">
                        <span>{{ formatCurrency(scope.row.price) }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column v-if="msg.card.domainType === 'STOCK_CHECK'" prop="diffQty" label="差异数量" width="100" />
                    <el-table-column v-if="!['STOCK_CHECK', 'STOCK_TRANSFER'].includes(msg.card.domainType)" prop="price" label="单价" width="110">
                      <template slot-scope="scope">
                        <span>{{ formatCurrency(scope.row.price) }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column v-if="!['STOCK_CHECK', 'STOCK_TRANSFER'].includes(msg.card.domainType)" prop="amount" label="金额" width="120">
                      <template slot-scope="scope">
                        <span>{{ formatCurrency(scope.row.amount) }}</span>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>

                <!-- ──── 经营简报卡片 ──── -->
                <div v-else-if="msg.card && msg.card.type === 'ANALYSIS_BRIEFING'" class="card-block analysis-card">
                  <div class="analysis-sections">
                    <template v-if="msg.card.summary">
                      <div v-if="msg.card.summary.sales" class="analysis-section">
                        <div class="as-header"><i class="el-icon-s-data" /> 销售概况</div>
                        <div class="as-grid">
                          <div class="as-item"><span class="as-label">销售额</span><span class="as-value">{{ formatCurrency(msg.card.summary.sales.salesAmount) }}</span></div>
                          <div class="as-item"><span class="as-label">退货</span><span class="as-value warn">{{ formatCurrency(msg.card.summary.sales.returnAmount) }}</span></div>
                          <div class="as-item"><span class="as-label">净销售</span><span class="as-value primary">{{ formatCurrency(msg.card.summary.sales.netSalesAmount) }}</span></div>
                          <div class="as-item"><span class="as-label">订单数</span><span class="as-value">{{ msg.card.summary.sales.orderCount }}</span></div>
                          <div class="as-item"><span class="as-label">客单价</span><span class="as-value">{{ formatCurrency(msg.card.summary.sales.avgOrderAmount) }}</span></div>
                          <div class="as-item"><span class="as-label">毛利</span><span class="as-value success">{{ formatCurrency(msg.card.summary.sales.grossProfit) }}</span></div>
                        </div>
                      </div>
                      <div v-if="msg.card.summary.purchase" class="analysis-section">
                        <div class="as-header"><i class="el-icon-shopping-bag-1" /> 采购概况</div>
                        <div class="as-grid">
                          <div class="as-item"><span class="as-label">采购额</span><span class="as-value">{{ formatCurrency(msg.card.summary.purchase.purchaseAmount) }}</span></div>
                          <div class="as-item"><span class="as-label">退货</span><span class="as-value warn">{{ formatCurrency(msg.card.summary.purchase.returnAmount) }}</span></div>
                          <div class="as-item"><span class="as-label">净采购</span><span class="as-value primary">{{ formatCurrency(msg.card.summary.purchase.netPurchaseAmount) }}</span></div>
                          <div class="as-item"><span class="as-label">入库单数</span><span class="as-value">{{ msg.card.summary.purchase.inCount }}</span></div>
                        </div>
                      </div>
                      <div v-if="msg.card.summary.finance" class="analysis-section">
                        <div class="as-header"><i class="el-icon-money" /> 财务概况</div>
                        <div class="as-grid">
                          <div class="as-item"><span class="as-label">应收总额</span><span class="as-value">{{ formatCurrency(msg.card.summary.finance.receivableTotal) }}</span></div>
                          <div class="as-item"><span class="as-label">应付总额</span><span class="as-value">{{ formatCurrency(msg.card.summary.finance.payableTotal) }}</span></div>
                          <div class="as-item"><span class="as-label">逾期笔数</span><span class="as-value warn">{{ msg.card.summary.finance.overdueReceivableCount }}</span></div>
                          <div class="as-item"><span class="as-label">逾期金额</span><span class="as-value warn">{{ formatCurrency(msg.card.summary.finance.overdueReceivableAmount) }}</span></div>
                        </div>
                      </div>
                      <div v-if="msg.card.summary.pending" class="analysis-section">
                        <div class="as-header"><i class="el-icon-document-checked" /> 待办事项</div>
                        <div class="as-grid">
                          <div class="as-item"><span class="as-label">待审采购</span><span class="as-value">{{ msg.card.summary.pending.pendingPurchaseOrderCount }}</span></div>
                          <div class="as-item"><span class="as-label">待审销售</span><span class="as-value">{{ msg.card.summary.pending.pendingSalesOrderCount }}</span></div>
                          <div class="as-item"><span class="as-label">待入库</span><span class="as-value">{{ msg.card.summary.pending.pendingPurchaseInCount }}</span></div>
                          <div class="as-item"><span class="as-label">待出库</span><span class="as-value">{{ msg.card.summary.pending.pendingSalesOutCount }}</span></div>
                        </div>
                      </div>
                    </template>
                    <div v-if="msg.card.topGoods && msg.card.topGoods.length" class="analysis-section">
                      <div class="as-header"><i class="el-icon-trophy" /> 热销 TOP{{ msg.card.topGoods.length }}</div>
                      <div class="as-ranking-list">
                        <div v-for="(item, rIdx) in msg.card.topGoods" :key="'tg-' + rIdx" class="ranking-row">
                          <span class="ranking-no" :class="{ top3: rIdx < 3 }">{{ rIdx + 1 }}</span>
                          <span class="ranking-name">{{ item.goodsName }}<template v-if="item.spec"> ({{ item.spec }})</template></span>
                          <span class="ranking-amount">{{ formatCurrency(item.salesAmount) }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div v-if="msg.card.anomalies && msg.card.anomalies.length" class="analysis-anomalies">
                    <div class="as-header"><i class="el-icon-warning-outline" /> 异常提醒</div>
                    <div v-for="(a, aIdx) in msg.card.anomalies" :key="'anomaly-' + aIdx" class="anomaly-item" :class="'severity-' + (a.severity || 'WHITE').toLowerCase()">
                      <span class="anomaly-title">{{ a.title }}</span>
                      <span class="anomaly-desc">{{ a.description }}</span>
                    </div>
                  </div>
                  <div v-if="msg.card.guideQuestions && msg.card.guideQuestions.length" class="analysis-guide">
                    <div class="as-header">你可以接着问我</div>
                    <div class="guide-chips">
                      <button v-for="(q, qIdx) in msg.card.guideQuestions" :key="'guide-' + qIdx"
                              class="message-chip" :disabled="isHistoryMessage(idx) || sending"
                              @click="send(q.value || q.label, q.label)">{{ q.label }}</button>
                    </div>
                  </div>
                </div>

                <!-- ──── 经营分析-排行卡片 ──── -->
                <div v-else-if="msg.card && msg.card.type === 'ANALYSIS_RANKING'" class="card-block analysis-card">
                  <div class="card-title">{{ msg.card.rankingType === 'customer' ? '客户销售排名' : '热销商品排行' }} · {{ msg.card.period }}</div>
                  <div class="as-ranking-list" v-if="msg.card.ranking && msg.card.ranking.length">
                    <div v-for="(item, rIdx) in msg.card.ranking" :key="'rank-' + rIdx" class="ranking-row">
                      <span class="ranking-no" :class="{ top3: rIdx < 3 }">{{ item.rank || (rIdx + 1) }}</span>
                      <span class="ranking-name">{{ msg.card.rankingType === 'customer' ? item.customerName : item.goodsName }}<template v-if="item.spec"> ({{ item.spec }})</template></span>
                      <span class="ranking-amount">{{ formatCurrency(item.salesAmount) }}</span>
                      <span v-if="item.orderCount" class="ranking-extra">{{ item.orderCount }}单</span>
                    </div>
                  </div>
                  <div v-else class="analysis-empty">暂无排行数据</div>
                </div>

                <!-- ──── 经营分析-通用回答卡片 ──── -->
                <div v-else-if="msg.card && msg.card.type === 'ANALYSIS_ANSWER'" class="card-block analysis-card">
                  <div class="card-title">{{ msg.card.period }}</div>
                </div>

              </div>
            </div>
          </div>

          <button v-if="showNewMessageHint" class="new-message-hint" @click="jumpToLatest">有新消息，点此查看</button>
        </div>

        <footer class="composer">
          <div class="action-chips" v-if="actionChips.length">
            <button
              v-for="(chip, idx) in actionChips"
              :key="'action-chip-' + idx"
              class="quick-chip subtle"
              :disabled="sending || queryLoading"
              @click="send(chip.value || chip.name, chip.name || chip.label)"
            >{{ chip.name || chip.label }}</button>
          </div>
          <div v-if="queryLoading" class="query-loading-hint">正在查询，请勿重复点击，结果返回后会自动展示。</div>

          <div class="input-shell">
            <!-- 图片预览区 -->
            <div v-if="pendingImageUrl" class="image-preview-bar">
              <div class="image-preview-item">
                <img :src="pendingImageUrl" alt="待识别图片" class="preview-thumb" />
                <span class="preview-label">截图待识别</span>
                <i class="el-icon-close preview-remove" @click="removePendingImage"></i>
              </div>
            </div>
            <el-input
              ref="composerInput"
              v-model="draft"
              type="textarea"
              :rows="3"
              resize="none"
              :disabled="chatLocked || sending"
              :placeholder="composerPlaceholder"
              @keydown.native="handleComposerKeydown"
            />
            <div class="composer-footer">
              <div class="composer-tip">Enter 发送，Shift+Enter 换行，Ctrl+V 粘贴截图</div>
              <div class="composer-actions">
                <el-tooltip content="上传图片识别（仅支持采购订单、销售订单）" placement="top">
                  <el-button plain :disabled="sending || queryLoading" @click="triggerImageUpload">
                    <i class="el-icon-picture-outline"></i>
                  </el-button>
                </el-tooltip>
                <input ref="imageUploadInput" type="file" accept="image/jpeg,image/png,image/webp" style="display:none" @change="handleImageFileChange" />
                <el-button plain :disabled="sending || queryLoading" @click="clearContext">清空当前会话</el-button>
                <el-button type="primary" :loading="sending || queryLoading" :disabled="chatLocked || queryLoading" @click="send()">发送</el-button>
              </div>
            </div>
          </div>
        </footer>
      </section>
    </div>

    <el-dialog :title="orderDialogTitle" :visible.sync="orderDialogOpen" width="1200px" :fullscreen="orderDialogFullscreen" append-to-body>
      <template slot="title">
        <span>{{ orderDialogTitle }}</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="toggleOrderDialogFullscreen">
          <i :class="orderDialogFullscreen ? 'el-icon-news' : 'el-icon-full-screen'"></i>
          {{ orderDialogFullscreen ? '还原' : '全屏' }}
        </el-button>
      </template>
      <el-form ref="orderForm" :model="orderForm" :rules="orderRules" label-width="100px">
        <el-row>
          <el-col :span="8">
            <!-- 采购订单显示供应商 -->
            <el-form-item v-if="['PURCHASE_ORDER', 'PO', 'PURCHASE_IN', 'PURCHASE_RETURN'].includes(orderForm.domainType)" label="供应商" prop="supplierId">
              <div class="select-with-action">
                <el-select v-model="orderForm.supplierId" placeholder="请选择供应商" :disabled="orderFormView" filterable clearable>
                  <el-option v-for="item in supplierOptions" :key="item.id" :label="item.supplierName" :value="item.id" />
                </el-select>
                <el-button v-if="!orderFormView" type="text" icon="el-icon-plus" @click="addOrderFormSupplier">新增</el-button>
              </div>
            </el-form-item>
            <!-- 销售订单/销售出库/销售退货显示客户 -->
            <el-form-item v-else-if="['SALES_ORDER', 'SALES_OUT', 'SALES_RETURN'].includes(orderForm.domainType)" label="客户" prop="customerId">
              <div class="select-with-action">
                <el-select v-model="orderForm.customerId" placeholder="请选择客户" :disabled="orderFormView" filterable clearable>
                  <el-option v-for="item in customerOptions" :key="item.id" :label="item.customerName" :value="item.id" />
                </el-select>
                <el-button v-if="!orderFormView" type="text" icon="el-icon-plus" @click="addOrderFormCustomer">新增</el-button>
              </div>
            </el-form-item>
          </el-col>
          <!-- 采购入库/退货/销售退货显示仓库 -->
          <el-col v-if="['PURCHASE_IN', 'PURCHASE_RETURN', 'SALES_RETURN'].includes(orderForm.domainType)" :span="8">
            <el-form-item label="仓库" prop="warehouseId">
              <div class="select-with-action">
                <el-select v-model="orderForm.warehouseId" placeholder="请选择仓库" :disabled="orderFormView" filterable clearable>
                  <el-option v-for="item in warehouseOptions" :key="item.id" :label="item.warehouseName" :value="item.id" />
                </el-select>
                <el-button v-if="!orderFormView" type="text" icon="el-icon-plus" @click="addOrderFormWarehouse">新增</el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col v-else-if="orderForm.domainType === 'SALES_OUT'" :span="8">
            <el-form-item label="出库日期" prop="outDate">
              <el-date-picker v-model="orderForm.outDate" type="date" placeholder="选择日期" value-format="yyyy-MM-dd" :disabled="orderFormView" />
            </el-form-item>
          </el-col>
          <el-col v-else :span="8">
            <el-form-item label="订单日期" prop="orderDate">
              <el-date-picker v-model="orderForm.orderDate" type="date" placeholder="选择日期" value-format="yyyy-MM-dd" :disabled="orderFormView" />
            </el-form-item>
          </el-col>
          <!-- 采购入库显示入库日期，采购退货显示退货日期，采购订单显示交货日期 -->
          <el-col v-if="orderForm.domainType === 'PURCHASE_IN'" :span="8">
            <el-form-item label="入库日期" prop="inDate">
              <el-date-picker v-model="orderForm.inDate" type="date" placeholder="选择日期" value-format="yyyy-MM-dd" :disabled="orderFormView" />
            </el-form-item>
          </el-col>
          <el-col v-else-if="['PURCHASE_RETURN', 'SALES_RETURN'].includes(orderForm.domainType)" :span="8">
            <el-form-item label="退货日期" prop="returnDate">
              <el-date-picker v-model="orderForm.returnDate" type="date" placeholder="选择日期" value-format="yyyy-MM-dd" :disabled="orderFormView" />
            </el-form-item>
          </el-col>
          <el-col v-else-if="orderForm.domainType === 'SALES_OUT'" :span="8">
            <el-form-item label="仓库">
              <el-select v-model="orderForm.warehouseId" placeholder="请选择仓库" :disabled="orderFormView" filterable clearable>
                <el-option v-for="item in warehouseOptions" :key="item.id" :label="item.warehouseName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-else :span="8">
            <el-form-item label="交货日期" prop="deliveryDate">
              <el-date-picker v-model="orderForm.deliveryDate" type="date" placeholder="选择日期" value-format="yyyy-MM-dd" :disabled="orderFormView" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-if="['PURCHASE_IN', 'PURCHASE_RETURN', 'SALES_RETURN'].includes(orderForm.domainType)">
          <el-col :span="8">
            <el-form-item label="经办人" prop="handler">
              <el-input v-model="orderForm.handler" placeholder="请输入经办人" :disabled="orderFormView" />
            </el-form-item>
          </el-col>
          <el-col v-if="orderForm.domainType === 'PURCHASE_IN'" :span="8">
            <el-form-item label="订单入库" prop="orderId">
              <el-select v-model="orderForm.orderId" placeholder="请选择采购订单" :disabled="orderFormView || orderForm.id != null" @change="handleOrderChangeForIn">
                <el-option v-for="item in approvedOrderOptions" :key="item.id" :label="item.orderNo" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="orderForm.remark" type="textarea" placeholder="请输入备注" :disabled="orderFormView" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">订单明细</el-divider>

        <el-table :data="orderForm.items" border style="width: 100%" show-summary :summary-method="getOrderSummaries">
          <el-table-column type="index" width="50" label="序号" />
          <el-table-column label="SKU" width="200">
            <template slot-scope="scope">
              <div style="display: flex; align-items: center; gap: 5px;">
                <el-input v-model="scope.row.skuName" placeholder="请选择商品" readonly :disabled="orderFormView || orderForm.id != null" size="small" style="flex: 1" />
                <el-button icon="el-icon-search" size="mini" :disabled="orderFormView || orderForm.id != null" @click="handleSelectOrderSku(scope.$index)">选择</el-button>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="商品名称" width="250" show-overflow-tooltip>
            <template slot-scope="scope">
              <span>{{ formatGoodsDisplay(scope.row) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="规格" prop="specInfo" width="120" />
          <el-table-column label="单位" width="140">
            <template slot-scope="scope">
              <el-select
                v-if="!orderFormView && ['PURCHASE_ORDER', 'PO'].includes(orderForm.domainType)"
                v-model="scope.row.unitName"
                filterable
                allow-create
                default-first-option
                clearable
                placeholder="单位可选"
                size="mini"
              >
                <el-option
                  v-for="unit in unitOptions"
                  :key="'order-unit-' + unit.id"
                  :label="unit.unitName"
                  :value="unit.unitName"
                />
              </el-select>
              <span v-else>{{ scope.row.unitName || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="120">
            <template slot-scope="scope">
              <el-input-number v-model="scope.row.quantity" :min="0" :precision="2" controls-position="right" :disabled="orderFormView" @change="handleOrderItemChange(scope.row)" style="width: 100%" />
            </template>
          </el-table-column>
          <el-table-column label="单价" width="120">
            <template slot-scope="scope">
              <el-input-number v-model="scope.row.price" :min="0" :precision="2" controls-position="right" :disabled="orderFormView" @change="handleOrderItemChange(scope.row)" style="width: 100%" />
            </template>
          </el-table-column>
          <el-table-column label="折扣(元)" width="100">
            <template slot-scope="scope">
              <el-input-number v-model="scope.row.discount" :min="0" :precision="2" controls-position="right" :disabled="orderFormView" @change="handleOrderItemChange(scope.row)" style="width: 100%" />
            </template>
          </el-table-column>
          <el-table-column label="金额" prop="amount" width="120" align="right">
            <template slot-scope="scope">
              <span>{{ scope.row.amount ? Number(scope.row.amount).toFixed(2) : '0.00' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="备注" width="150">
            <template slot-scope="scope">
              <el-input v-model="scope.row.remark" placeholder="备注" :disabled="orderFormView" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="60" v-if="canEditOrderItems()">
            <template slot-scope="scope">
              <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDeleteOrderItem(scope.$index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-button v-if="canAddOrderItems()" type="dashed" icon="el-icon-plus" size="small" @click="handleSelectOrderSku()" style="width: 100%; margin-top: 10px">添加商品</el-button>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button v-if="!orderFormView" type="primary" @click="submitOrderForm">确 定</el-button>
        <el-button @click="closeOrderDialog">{{ orderFormView ? '关 闭' : '取 消' }}</el-button>
      </div>
    </el-dialog>

    <goods-sku-selector ref="skuSelector" @confirm="handleOrderSkuSelected" />
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import {
  getPcPurchaseOrderActions,
  chatPcPurchaseOrder,
  imageChatPc
} from '@/api/wms/aiAssistantPc'
import { addUnit, listUnit } from '@/api/wms/unit'
import { addGoodsCategory, listGoodsCategory } from '@/api/wms/goodsCategory'
import { addSupplier, listSupplier } from '@/api/wms/supplier'
import { listCustomer, addCustomer } from '@/api/wms/customer'
import { addWarehouse, listWarehouse } from '@/api/wms/warehouse'
import { getBillNeedApprove } from '@/api/wms/approvePolicy'
import { getAvailablePurchaseOrdersForIn } from '@/api/wms/purchaseOrder'
import GoodsSkuSelector from '@/components/GoodsSkuSelector'
import conversationMixin from './mixins/conversationMixin'
import candidateSelectionMixin from './mixins/candidateSelectionMixin'
import orderFormMixin from './mixins/orderFormMixin'
import cardBuilderMixin from './mixins/cardBuilderMixin'
import { formatGoodsDisplayName } from '@/utils/goods-display'

export default {
  name: 'WmsAiAssistantPc',
  components: { GoodsSkuSelector },
  mixins: [conversationMixin, candidateSelectionMixin, orderFormMixin, cardBuilderMixin],
  data() {
    return {
      actions: [],
      conversations: [],
      activeConversationId: '',
      messages: [],
      quickActions: [],
      context: {},
      unitOptions: [],
      categoryOptions: [],
      supplierOptions: [],
      customerOptions: [],
      warehouseOptions: [],
      conversationId: '',
      draft: '',
      sending: false,
      queryLoading: false,
      queryLoadingText: '正在查询，请稍候...',
      scenarioRunning: false,
      isNearBottom: true,
      showNewMessageHint: false,
      candidateSelectionCache: {
        goods: []
      },
      showApproveAction: true,
      pendingImage: null,
      pendingImageUrl: ''
    }
  },
  created() {
    this.initialize()
  },
  mounted() {
    this._pasteHandler = this.handlePaste.bind(this)
    document.addEventListener('paste', this._pasteHandler)
  },
  beforeDestroy() {
    if (this._pasteHandler) {
      document.removeEventListener('paste', this._pasteHandler)
    }
    if (this.pendingImageUrl) {
      URL.revokeObjectURL(this.pendingImageUrl)
    }
  },
  computed: {
    ...mapGetters([
      'avatar',
      'id',
      'name'
    ]),
    // 会话快照按 租户+用户 隔离，避免同一浏览器切换账号/租户后加载到其他租户的会话
    storageKey() {
      const tenantId = this.$store.state.user.tenantId || 'no-tenant'
      const userId = this.id || this.name || 'no-user'
      return 'wms-ai-pc-chat-history-v2-' + tenantId + '-' + userId
    },
    aiLogo() {
      return this.$buildCosImageUrl('logo.png')
    },
    actionChips() {
      return this.actions || []
    },
    currentTitle() {
      const current = this.conversations.find(item => item.conversationId === this.activeConversationId)
      return current && current.title ? current.title : '新对话'
    },
    currentSubtitle() {
      if (this.context && this.context.action === 'BUSINESS_ANALYSIS') {
        return '经营分析模式 — 可追问销售、采购、库存、财务等任何经营问题'
      }
      if (this.context && this.context.step && this.context.domainType) {
        const mode = this.context.createMode === 'LINKED'
          ? '关联单'
          : (this.context.createMode === 'DIRECT' ? '直接建单' : '')
        return `当前流程：${this.formatDomainType(this.context.domainType)}${mode ? ' / ' + mode : ''} / ${this.context.step}`
      }
      if (this.context && this.context.step) {
        return `当前流程：${this.context.step}`
      }
      if (this.context && this.context.lastOrderNo) {
        return `最近创建单号：${this.context.lastOrderNo}`
      }
      return '一句话创建采购、销售、盘点、调拨单据，支持自然语句输入、候选分页确认和聊天内新增基础资料。'
    },
    activeAssistantCard() {
      for (let index = this.messages.length - 1; index >= 0; index -= 1) {
        const message = this.messages[index]
        if (message && message.role === 'assistant' && message.card) {
          return message.card
        }
      }
      return null
    },
    chatLocked() {
      const card = this.activeAssistantCard
      return !!card && (
        card.type === 'REQUIRED_FORM' ||
        (card.type === 'DIFF_PREVIEW' && card.editable)
      )
    },
    composerPlaceholder() {
      if (this.chatLocked) {
        return '请先完成当前卡片操作，再继续自由输入'
      }
      return '直接输入一句业务话术，例如：卖给客户A苹果笔记本10台，单价9000元；从一号仓库调30台到二号仓库；盘点一号仓库所有商品'
    },
    groupedConversations() {
      const now = Date.now()
      const buckets = [
        { key: 'today', label: '今天', items: [] },
        { key: 'week', label: '近7天', items: [] },
        { key: 'older', label: '更早', items: [] }
      ]
      ;(this.conversations || []).forEach(item => {
        const diff = now - (item.updatedAt || now)
        if (diff < 24 * 60 * 60 * 1000) {
          buckets[0].items.push(item)
        } else if (diff < 7 * 24 * 60 * 60 * 1000) {
          buckets[1].items.push(item)
        } else {
          buckets[2].items.push(item)
        }
      })
      return buckets.filter(group => group.items.length)
    }
  },
  methods: {
    formatGoodsDisplay(item) {
      return formatGoodsDisplayName(item)
    },
    // ─── 初始化 ─────────────────────────────────
    async initialize() {
      const [actionsResp, unitResp, categoryResp, supplierResp, customerResp, warehouseResp, approveResp, availableOrdersResp] = await Promise.all([
        getPcPurchaseOrderActions(),
        listUnit(),
        listGoodsCategory(),
        listSupplier(),
        listCustomer(),
        listWarehouse(),
        getBillNeedApprove('PURCHASE_ORDER').catch(() => ({ data: true })),
        getAvailablePurchaseOrdersForIn().catch(() => ({ data: [] }))
      ])
      this.actions = actionsResp.data || []
      this.unitOptions = unitResp.data || []
      this.categoryOptions = this.normalizeCategoryOptions(categoryResp.data || [])
      this.supplierOptions = supplierResp.data || []
      this.customerOptions = customerResp.data || []
      this.warehouseOptions = warehouseResp.data || []
      this.approvedOrderOptions = availableOrdersResp.data || []
      this.showApproveAction = approveResp.data !== false
      this.restoreConversations()
      if (this.conversations.length) {
        this.activateConversation(this.conversations[0].conversationId)
        return
      }
      await this.startNewConversation()
    },

    // ─── 格式化 ─────────────────────────────────
    formatOrderStatus(status) {
      return status === '0'
        ? '待审核'
        : (status === '1'
          ? '已审核'
          : (status === '2'
            ? '部分入库'
            : (status === '3' ? '全部入库' : '已关闭')))
    },
    formatQueryStatus(row) {
      if (!row) return '-'
      if (row.statusLabel) return row.statusLabel
      const status = row.status !== undefined ? String(row.status) : String(row.orderStatus || '')
      if (status === '0') return '待审核'
      if (status === '1') return '已审核'
      if (status === '2') return '执行中'
      if (status === '3') return '已完成'
      return status || '-'
    },
    viewBillDetail(row) {
      const domainType = this.normalizeQueryDomainType(row && row.domainType)
      const id = row && row.id
      if (!domainType || id === null || id === undefined) {
        this.$message.warning('当前行缺少可用的详情参数')
        return
      }
      const domainLabel = (row && row.domainTypeName) || this.formatDomainType(domainType)
      const billNo = row && row.billNo ? row.billNo : String(id)
      this.send('__ACTION__:BILL_DETAIL:' + domainType + ':' + id, '查看' + domainLabel + '详情：' + billNo)
    },
    normalizeQueryDomainType(domainType) {
      const value = String(domainType || '').trim().toUpperCase()
      const map = {
        PO: 'PO', PURCHASE_ORDER: 'PO',
        PI: 'PI', PURCHASE_IN: 'PI',
        PR: 'PR', PURCHASE_RETURN: 'PR',
        SO: 'SO', SALES_ORDER: 'SO',
        SOUT: 'SOUT', SALES_OUT: 'SOUT',
        SR: 'SR', SALES_RETURN: 'SR',
        SC: 'SC', STOCK_CHECK: 'SC',
        ST: 'ST', STOCK_TRANSFER: 'ST'
      }
      return map[value] || ''
    },
    formatDomainType(domainType) {
      const map = {
        PURCHASE_ORDER: '采购订单', PO: '采购订单',
        PURCHASE_IN: '采购入库', PI: '采购入库',
        PURCHASE_RETURN: '采购退货', PR: '采购退货',
        SALES_ORDER: '销售订单', SO: '销售订单',
        SALES_OUT: '销售出库', SOUT: '销售出库',
        SALES_RETURN: '销售退货', SR: '销售退货',
        STOCK_CHECK: '库存盘点', SC: '库存盘点',
        STOCK_TRANSFER: '库存调拨', ST: '库存调拨'
      }
      return map[domainType] || domainType || '-'
    },
    formatCheckType(value) {
      const map = { FULL: '全盘', SAMPLE: '抽盘', DYNAMIC: '抽盘' }
      return map[value] || value || '-'
    },
    formatCurrency(value) {
      const amount = Number(value)
      return Number.isNaN(amount) ? '￥0.00' : `￥${amount.toFixed(2)}`
    },
    getExecResultSummary({ columns, data }, card) {
      const sums = []
      columns.forEach((col, index) => {
        if (index === 0) { sums[index] = '合计'; return }
        const prop = col.property
        if (prop === 'quantity' || prop === 'beforeQty' || prop === 'diffQty') {
          const total = data.reduce((s, row) => s + Number(row[prop] || 0), 0)
          sums[index] = Number.isInteger(total) ? String(total) : total.toFixed(2)
        } else if (prop === 'amount') {
          const total = data.reduce((s, row) => s + Number(row.amount || 0), 0)
          sums[index] = this.formatCurrency(total)
        } else {
          sums[index] = ''
        }
      })
      return sums
    },
    formatRelativeTime(time) {
      if (!time) return '-'
      const diff = Date.now() - time
      if (diff < 60 * 1000) return '刚刚'
      if (diff < 60 * 60 * 1000) return `${Math.floor(diff / (60 * 1000))} 分钟前`
      if (diff < 24 * 60 * 60 * 1000) return `${Math.floor(diff / (60 * 60 * 1000))} 小时前`
      return `${Math.floor(diff / (24 * 60 * 60 * 1000))} 天前`
    },
    formatSpecDisplay(specInfo) {
      if (!specInfo) return '无规格'
      try {
        const parsed = typeof specInfo === 'string' ? JSON.parse(specInfo) : specInfo
        return Object.keys(parsed || {}).map(key => `${key}:${parsed[key]}`).join(' / ') || '无规格'
      } catch (error) {
        return specInfo
      }
    },
    normalizeCategoryOptions(list) {
      const result = []
      const walk = items => {
        ;(items || []).forEach(item => {
          if (!item || item.status === '1') return
          result.push({ id: item.id, categoryName: item.categoryName })
          if (Array.isArray(item.children) && item.children.length) walk(item.children)
        })
      }
      walk(Array.isArray(list) ? list : [])
      return result
    },

    // ─── 基础数据刷新 ───────────────────────────
    async refreshUnitOptions() {
      const resp = await listUnit()
      this.unitOptions = resp.data || []
    },
    async refreshCategoryOptions() {
      const resp = await listGoodsCategory()
      this.categoryOptions = this.normalizeCategoryOptions(resp.data || [])
    },
    async initializeSupplierOptions() {
      const resp = await listSupplier()
      this.supplierOptions = resp.data || []
    },
    async initializeWarehouseOptions() {
      const resp = await listWarehouse()
      this.warehouseOptions = resp.data || []
    },
    async initializeCustomerOptions() {
      const resp = await listCustomer()
      this.customerOptions = resp.data || []
    },
    // 供应商/仓库/客户新增后刷新，供 orderFormMixin 调用
    async addSupplierAndRefresh(supplierName) {
      await addSupplier({ supplierName, status: '0', settlementType: '1', creditLimit: 0, paymentDays: 0 })
      await this.initializeSupplierOptions()
    },
    async addWarehouseAndRefresh(warehouseName) {
      await addWarehouse({ warehouseName, status: '0', sortNum: 0, remark: 'PC-AI助手快捷新增' })
      await this.initializeWarehouseOptions()
    },
    async addCustomerAndRefresh(customerName) {
      await addCustomer({ customerName, status: '0', remark: 'PC-AI助手快捷新增' })
      await this.initializeCustomerOptions()
    },

    // ─── 聊天内快速新增基础数据 ─────────────────
    async addInlineUnit(msg) {
      try {
        const { value } = await this.$prompt('请输入单位名称', '新增单位', {
          confirmButtonText: '确定', cancelButtonText: '取消',
          inputValue: msg && msg.card && msg.card.formModel ? msg.card.formModel.unitName : '',
          inputPattern: /\S+/, inputErrorMessage: '单位名称不能为空'
        })
        await addUnit({ unitName: value, status: '0' })
        await this.refreshUnitOptions()
        if (msg && msg.card && msg.card.formModel) this.$set(msg.card.formModel, 'unitName', value)
        this.$message.success('单位已新增')
      } catch (error) { if (error !== 'cancel') throw error }
    },
    async addInlineCategory(msg) {
      try {
        const { value } = await this.$prompt('请输入分类名称', '新增分类', {
          confirmButtonText: '确定', cancelButtonText: '取消',
          inputValue: msg && msg.card && msg.card.formModel ? msg.card.formModel.categoryName : '',
          inputPattern: /\S+/, inputErrorMessage: '分类名称不能为空'
        })
        const resp = await addGoodsCategory({ categoryName: value, parentId: 0, orderNum: 0, status: '0' })
        await this.refreshCategoryOptions()
        if (msg && msg.card && msg.card.formModel) {
          this.$set(msg.card.formModel, 'categoryName', value)
          this.$set(msg.card.formModel, 'categoryId', resp.data)
        }
        this.$message.success('分类已新增')
      } catch (error) { if (error !== 'cancel') throw error }
    },
    handleGoodsCategoryChange(msg, value) {
      if (!msg || !msg.card || !msg.card.formModel) return
      const hit = this.categoryOptions.find(item => item.id === value)
      this.$set(msg.card.formModel, 'categoryName', hit ? hit.categoryName : '')
    },
    async addInlinePreviewSupplier(msg) {
      try {
        const { value } = await this.$prompt('请输入供应商名称', '新增供应商', {
          confirmButtonText: '确定', cancelButtonText: '取消',
          inputPattern: /\S+/, inputErrorMessage: '供应商名称不能为空'
        })
        const supplierName = String(value || '').trim()
        if (!supplierName) return
        await this.addSupplierAndRefresh(supplierName)
        const created = (this.supplierOptions || [])
          .filter(item => item.supplierName === supplierName)
          .sort((left, right) => Number(right.id || 0) - Number(left.id || 0))[0]
        if (msg && msg.card && msg.card.formModel && created) {
          this.$set(msg.card.formModel, 'supplierId', created.id)
          this.$set(msg.card, 'supplier', created.supplierName)
        }
        this.$message.success('供应商已新增')
      } catch (error) { if (error !== 'cancel') throw error }
    },
    async addInlinePreviewWarehouse(msg) {
      try {
        const { value } = await this.$prompt('请输入仓库名称', '新增仓库', {
          confirmButtonText: '确定', cancelButtonText: '取消',
          inputPattern: /\S+/, inputErrorMessage: '仓库名称不能为空'
        })
        const warehouseName = String(value || '').trim()
        if (!warehouseName) return
        await this.addWarehouseAndRefresh(warehouseName)
        const created = (this.warehouseOptions || [])
          .filter(item => item.warehouseName === warehouseName)
          .sort((left, right) => Number(right.id || 0) - Number(left.id || 0))[0]
        if (msg && msg.card && msg.card.formModel && created) {
          this.$set(msg.card.formModel, 'warehouseId', created.id)
          this.$set(msg.card, 'warehouse', created.warehouseName)
        }
        this.$message.success('仓库已新增')
      } catch (error) { if (error !== 'cancel') throw error }
    },
    async addInlinePreviewCustomer(msg) {
      try {
        const { value } = await this.$prompt('请输入客户名称', '新增客户', {
          confirmButtonText: '确定', cancelButtonText: '取消',
          inputPattern: /\S+/, inputErrorMessage: '客户名称不能为空'
        })
        const customerName = String(value || '').trim()
        if (!customerName) return
        await this.addCustomerAndRefresh(customerName)
        const created = (this.customerOptions || [])
          .filter(item => item.customerName === customerName)
          .sort((left, right) => Number(right.id || 0) - Number(left.id || 0))[0]
        if (msg && msg.card && msg.card.formModel && created) {
          this.$set(msg.card.formModel, 'customerId', created.id)
          this.$set(msg.card, 'customer', created.customerName)
        }
        this.$message.success('客户已新增')
      } catch (error) { if (error !== 'cancel') throw error }
    },

    // ─── 模板 & 快捷动作 ────────────────────────
    templateLabel(tpl) {
      if (!tpl) return '-'
      return typeof tpl === 'string' ? tpl : (tpl.label || tpl.text || tpl.value || '-')
    },
    templateValue(tpl) {
      if (!tpl) return ''
      return typeof tpl === 'string' ? tpl : (tpl.value || tpl.label || '')
    },
    normalizeTemplateDraftValue(tpl) {
      const raw = this.templateValue(tpl)
      if (!raw) return this.templateLabel(tpl)
      if (raw.startsWith('__ROUTE__')) {
        const index = raw.indexOf('::')
        return index >= 0 ? raw.substring(index + 2) : this.templateLabel(tpl)
      }
      return raw
    },
    async copyText(text, successMessage) {
      if (!text) return
      try {
        if (navigator.clipboard && navigator.clipboard.writeText) {
          await navigator.clipboard.writeText(text)
        } else {
          const input = document.createElement('textarea')
          input.value = text
          input.setAttribute('readonly', 'readonly')
          input.style.position = 'fixed'
          input.style.left = '-9999px'
          document.body.appendChild(input)
          input.select()
          document.execCommand('copy')
          document.body.removeChild(input)
        }
        this.$message.success(successMessage || '复制成功')
      } catch (error) {
        this.$message.error('复制失败')
      }
    },
    copyTemplateText(tpl) {
      return this.copyText(this.normalizeTemplateDraftValue(tpl), '模板内容已复制')
    },
    applyTemplateDraft(tpl) {
      const text = this.normalizeTemplateDraftValue(tpl)
      this.draft = text
      this.$nextTick(() => {
        if (this.$refs.composerInput && this.$refs.composerInput.focus) this.$refs.composerInput.focus()
      })
      this.$message.success('模板内容已填入输入框，可修改后再发送')
    },
    previewTipText(card) {
      if (!card) return ''
      if (card.domainType === 'STOCK_CHECK') return '盘点确认前可直接修改实际数量，也可以删除误选商品行。'
      if (card.linkedMode) return '关联单模式下仅允许删减来源行或修改数量，不允许新增来源单外商品。'
      if (card.domainType === 'PURCHASE_ORDER') return '确认前可补充供应商，并直接修改商品数量和单价。'
      if (card.domainType === 'PURCHASE_IN') return '确认前可补充供应商、仓库，并直接修改商品数量和单价。'
      if (card.domainType === 'SALES_ORDER') return '确认前可补充客户，并直接修改商品数量和单价。'
      return '确认前可直接修改明细数量。'
    },
    quickActionLabel(action) {
      if (!action) return '-'
      return typeof action === 'string' ? action : (action.label || action.name || action.value || '-')
    },
    quickActionValue(action) {
      if (!action) return ''
      if (typeof action !== 'string') return action.value || action.label || ''
      const routeMap = {
        '采购订单': '__ACTION__:CREATE_PO', '采购入库': '__ACTION__:CREATE_PI',
        '采购退货': '__ACTION__:CREATE_PR', '销售订单': '__ACTION__:CREATE_SO',
        '销售出库': '__ACTION__:CREATE_SOUT', '销售退货': '__ACTION__:CREATE_SR',
        '库存盘点': '__ACTION__:CREATE_SC', '库存调拨': '__ACTION__:CREATE_ST',
        '查询单据': '__ACTION__:QUERY_BILL_CENTER', '统一查询单据': '__ACTION__:QUERY_BILL_CENTER',
        '查看刚创建的订单': '__ACTION__:QUERY_PO', '查询采购订单': '__ACTION__:QUERY_PO',
        '再来一单采购': '__ACTION__:CREATE_PO', '再来一单采购订单': '__ACTION__:CREATE_PO',
        '再来一单采购入库': '__ACTION__:CREATE_PI', '再来一单采购退货': '__ACTION__:CREATE_PR',
        '再来一单销售订单': '__ACTION__:CREATE_SO', '再来一单销售出库': '__ACTION__:CREATE_SOUT',
        '再来一单销售退货': '__ACTION__:CREATE_SR', '再来一单库存盘点': '__ACTION__:CREATE_SC',
        '再来一单库存调拨': '__ACTION__:CREATE_ST'
      }
      return routeMap[action] || action
    },

    // ─── 表单操作 ───────────────────────────────
    getFormFieldOptions(field) {
      if (!field) return []
      if (Array.isArray(field.options) && field.options.length) {
        return field.options.map(option => ({
          label: option.label || option.name || option.text || option.value,
          value: option.value
        }))
      }
      if (field.key === 'supplierId') return (this.supplierOptions || []).map(item => ({ label: item.supplierName, value: item.id }))
      if (field.key === 'customerId') return (this.customerOptions || []).map(item => ({ label: item.customerName, value: item.id }))
      if (field.key === 'warehouseId') return (this.warehouseOptions || []).map(item => ({ label: item.warehouseName, value: item.id }))
      if (field.key === 'orderId') return (this.approvedOrderOptions || []).map(item => ({ label: item.orderNo, value: item.id }))
      return []
    },
    addSpecRow(msg) {
      msg.card.formModel.specs.push(this.createSpecRow())
    },
    removeSpecRow(msg, specIndex) {
      if (msg.card.formModel.specs.length === 1) {
        msg.card.formModel.specs.splice(0, 1, this.createSpecRow())
        return
      }
      msg.card.formModel.specs.splice(specIndex, 1)
    },
    addSpecValue(msg, specIndex) {
      msg.card.formModel.specs[specIndex].values.push({ specValue: '' })
    },
    removeSpecValue(msg, specIndex, valueIndex) {
      const values = msg.card.formModel.specs[specIndex].values
      if (values.length === 1) {
        values.splice(0, 1, { specValue: '' })
        return
      }
      values.splice(valueIndex, 1)
    },
    removeSkuRow(msg, skuIndex) {
      if (!msg || !msg.card || !msg.card.formModel || !Array.isArray(msg.card.formModel.skuList)) return
      msg.card.formModel.skuList.splice(skuIndex, 1)
    },
    removePreviewRow(msg, rowIndex) {
      if (!msg || !msg.card || !msg.card.formModel || !Array.isArray(msg.card.formModel.items)) return
      msg.card.formModel.items.splice(rowIndex, 1)
    },
    async submitForm(msg, submitAction = 'SUBMIT', options = {}) {
      if (!msg || !msg.card || !msg.card.formModel) return
      if (msg.card.type === 'REQUIRED_FORM') {
        const fields = msg.card.fields || []
        const missingField = fields.find(field => {
          if (!field.required) return false
          const value = msg.card.formModel[field.key]
          return value === '' || value === null || value === undefined
        })
        if (missingField) {
          this.$message.warning(`请先填写${missingField.label}`)
          return
        }
      }
      if (msg.card.type === 'GOODS_CREATE_FORM' && !msg.card.formModel.categoryId) {
        this.$message.warning('请选择商品分类')
        return
      }
      if (msg.card.type === 'DIFF_PREVIEW' && msg.card.editable) {
        const items = msg.card.formModel.items || []
        if (msg.card.allowSupplierEdit && !msg.card.formModel.supplierId) { this.$message.warning('请先选择或新增供应商'); return }
        if (msg.card.allowCustomerEdit && !msg.card.formModel.customerId) { this.$message.warning('请先选择或新增客户'); return }
        if (msg.card.allowWarehouseEdit && !msg.card.formModel.warehouseId) { this.$message.warning('请先选择或新增仓库'); return }
        if (msg.card.allowInWarehouseEdit && !msg.card.formModel.inWarehouseId) { this.$message.warning('请选择调入仓库'); return }
        if (msg.card.allowRemarkEdit && !msg.card.formModel.remark) { this.$message.warning('请填写备注'); return }
        if (!items.length) {
          this.$message.warning(msg.card.domainType === 'STOCK_CHECK' ? '盘点单至少保留一条商品明细' : '关联单模式至少保留一条来源明细')
          return
        }
        const invalidRow = items.find(row => {
          const quantity = Number(row.quantity)
          const maxQty = row.maxQty === '' || row.maxQty === null || row.maxQty === undefined ? null : Number(row.maxQty)
          const price = row.price === '' || row.price === null || row.price === undefined ? null : Number(row.price)
          if (msg.card.domainType === 'STOCK_CHECK') {
            return Number.isNaN(quantity) || quantity < 0 || (price !== null && (Number.isNaN(price) || price < 0))
          }
          if (msg.card.allowPriceEdit && msg.card.priceRequired !== false && (price === null || Number.isNaN(price) || price <= 0)) return true
          if (msg.card.allowPriceEdit && msg.card.priceRequired === false && price !== null && !Number.isNaN(price) && price <= 0) return true
          return Number.isNaN(quantity) || quantity <= 0 || (maxQty !== null && !Number.isNaN(maxQty) && quantity > maxQty)
        })
        if (invalidRow) {
          if (msg.card.domainType === 'STOCK_CHECK') this.$message.warning('实际数量和成本价都不能小于0')
          else if (msg.card.allowPriceEdit) this.$message.warning('数量和单价都必须大于0')
          else this.$message.warning('数量必须大于0，且不能超过来源单可用上限')
          return
        }
      }
      if (msg.card.type === 'GOODS_SKU_FORM') {
        const skuList = msg.card.formModel.skuList || []
        if (!skuList.length) { this.$message.warning('请至少保留一条 SKU'); return }
        const invalidRow = skuList.find(row => {
          const quantity = Number(row.quantity)
          const price = Number(row.price)
          return !row.skuName || Number.isNaN(quantity) || Number.isNaN(price) || quantity <= 0 || price <= 0
        })
        if (invalidRow) { this.$message.warning('SKU名称、数量和采购单价都必须填写且大于0'); return }
      }
      if (options.confirmMessage) {
        try {
          await this.$confirm(options.confirmMessage, '提示', { confirmButtonText: '继续', cancelButtonText: '取消', type: 'warning' })
        } catch (error) { return }
      }
      const payloadData = JSON.parse(JSON.stringify(msg.card.formModel))
      payloadData._submitAction = submitAction
      const payload = '__FORM__' + JSON.stringify(payloadData)
      await this.send(payload, options.displayText || '已提交表单')
    },
    async confirmPreviewAndCreate(msg) {
      await this.submitForm(msg, 'CONFIRM_EXEC', { displayText: '确认并保存单据' })
    },
    async submitPreviewAndContinue(msg) {
      if (!msg || !msg.card || !msg.card.nextAction) return
      await this.submitForm(msg, 'UPDATE_PREVIEW', { displayText: '已更新商品确认' })
      await this.send(msg.card.nextAction, msg.card.nextLabel || '下一步')
    },
    cancelPreviewCreate() {
      this.send('取消', '取消')
    },

    // ─── 聊天核心 ───────────────────────────────
    afterMessagePushed(role) {
      this.syncConversationSnapshot()
      this.$nextTick(() => {
        if (role === 'user' || this.isNearBottom) { this.scrollBottom(); return }
        this.showNewMessageHint = true
      })
    },
    async pushAssistant(data) {
      if (!data) return
      this.conversationId = data.conversationId || this.conversationId
      this.quickActions = data.quickActions || []
      this.context = data.context || {}
      if (this.context.step !== 'WAIT_GOODS_CANDIDATE') {
        this.candidateSelectionCache = { goods: [] }
      }
      const card = data.card || null
      console.log('pushAssistant card:', card)
      this.prepareCard(card)
      if (card && card.type === 'EXEC_RESULT') {
        this.lockEditablePreviewCards()
        await this.hydrateExecResultCard(card)
      }
      this.messages.push({ role: 'assistant', text: data.text, card })
      this.afterMessagePushed('assistant')
    },
    pushUser(text) {
      this.messages.push({ role: 'user', text })
      this.afterMessagePushed('user')
    },
    scrollBottom() {
      const el = this.$refs.chatList
      if (el) { el.scrollTop = el.scrollHeight; this.isNearBottom = true; this.showNewMessageHint = false }
    },
    jumpToLatest() { this.scrollBottom() },
    handleChatScroll() {
      const el = this.$refs.chatList
      if (!el) return
      const distance = el.scrollHeight - el.scrollTop - el.clientHeight
      this.isNearBottom = distance < 48
      if (this.isNearBottom) this.showNewMessageHint = false
    },
    handleComposerKeydown(event) {
      if (this.chatLocked) return
      if (event.key === 'Enter' && !event.shiftKey) { event.preventDefault(); this.send() }
    },
    isQueryMessage(message) {
      if (!message) return false
      if (message.includes('__ACTION__:QUERY_BILL_CENTER') || message.includes('__ACTION__:QUERY_PO')) return true
      return (message.includes('查') || message.includes('查询')) &&
        (message.includes('单据') || message.includes('订单') || message.includes('单号') || message.includes('单据号') || message.includes('订单号') || message.includes('待审核'))
    },
    async send(text, displayText) {
      // 如果有待发送图片，走图片识别流程
      if (this.pendingImage && !text) {
        return this.sendImage()
      }
      const message = (text || this.draft || '').trim()
      if (!message || this.sending || (this.chatLocked && !text)) return
      this.pushUser((displayText || message).trim())
      this.draft = ''
      const isQuery = this.isQueryMessage(message)
      if (isQuery) {
        this.queryLoading = true
        this.queryLoadingText = '正在查询单据，请稍候...'
      }
      this.sending = true
      try {
        const resp = await chatPcPurchaseOrder({ conversationId: this.conversationId, message })
        this.pushAssistant(resp.data)
      } catch (error) {
        const backendMessage = error && error.response && error.response.data
          ? (error.response.data.msg || error.response.data.message || error.response.data.error) : ''
        this.pushAssistant({
          conversationId: this.conversationId,
          text: backendMessage || (error && error.message) || '本次请求失败，请稍后重试。',
          quickActions: this.quickActions, context: this.context
        })
      } finally {
        this.sending = false
        this.queryLoading = false
      }
    },
    // ─── 图片识别 ───────────────────────────────
    handlePaste(e) {
      const items = (e.clipboardData || {}).items
      if (!items) return
      for (let i = 0; i < items.length; i++) {
        if (items[i].type.indexOf('image') !== -1) {
          e.preventDefault()
          const blob = items[i].getAsFile()
          if (blob) this.setPendingImage(blob)
          return
        }
      }
    },
    triggerImageUpload() {
      if (this.$refs.imageUploadInput) {
        this.$refs.imageUploadInput.value = ''
        this.$refs.imageUploadInput.click()
      }
    },
    handleImageFileChange(e) {
      const file = e.target.files && e.target.files[0]
      if (file) this.setPendingImage(file)
    },
    setPendingImage(file) {
      // 检查当前模块是否支持图片识别
      const dt = this.context && this.context.domainType
      if (dt && !this.isOcrSupportedDomain(dt)) {
        this.$message.warning('图片识别仅支持采购订单和销售订单，当前流程不支持')
        return
      }
      if (file.size > 5 * 1024 * 1024) {
        this.$message.warning('图片大小不能超过5MB')
        return
      }
      if (this.pendingImageUrl) URL.revokeObjectURL(this.pendingImageUrl)
      this.pendingImage = file
      this.pendingImageUrl = URL.createObjectURL(file)
    },
    removePendingImage() {
      if (this.pendingImageUrl) URL.revokeObjectURL(this.pendingImageUrl)
      this.pendingImage = null
      this.pendingImageUrl = ''
    },
    isOcrSupportedDomain(dt) {
      const supported = ['PURCHASE_ORDER', 'PO', 'SALES_ORDER', 'SO']
      return supported.includes(dt)
    },
    async sendImage() {
      if (!this.pendingImage || this.sending) return
      // 模块校验
      const dt = this.context && this.context.domainType
      if (dt && !this.isOcrSupportedDomain(dt)) {
        this.$message.warning('图片识别仅支持采购订单和销售订单，当前流程不支持')
        this.removePendingImage()
        return
      }
      const imageUrl = this.pendingImageUrl
      const imageFile = this.pendingImage
      this.pendingImage = null
      this.pendingImageUrl = ''
      // 显示用户图片消息
      this.messages.push({ role: 'user', text: '[截图识别]', imageUrl })
      this.afterMessagePushed('user')
      this.sending = true
      try {
        const formData = new FormData()
        formData.append('file', imageFile)
        formData.append('conversationId', this.conversationId)
        const resp = await imageChatPc(formData)
        this.pushAssistant(resp.data)
      } catch (error) {
        const backendMessage = error && error.response && error.response.data
          ? (error.response.data.msg || error.response.data.message || error.response.data.error) : ''
        this.pushAssistant({
          conversationId: this.conversationId,
          text: backendMessage || (error && error.message) || '截图识别失败，请重试。',
          quickActions: this.quickActions, context: this.context
        })
      } finally {
        this.sending = false
      }
    },
    previewMsgImage(url) {
      if (url) window.open(url, '_blank')
    },

    // ─── 调试 ───────────────────────────────────
    pretty(v) { return JSON.stringify(v, null, 2) },
    async runScenario(mode) {
      if (this.sending || this.scenarioRunning) return
      const cases = {
        PO_FULL: ['创建采购订单，供应商云腾科技，苹果笔记本,2,6800；无线鼠标,10,59', '确认'],
        PI_FULL: ['创建采购入库，仓库A仓，供应商云腾科技，苹果笔记本,1,6800', '确认'],
        QUERY: ['查询采购订单']
      }
      const steps = cases[mode] || []
      if (!steps.length) return
      this.scenarioRunning = true
      try { for (const step of steps) { await this.send(step); await this.sleep(180) } } finally { this.scenarioRunning = false }
    },
    sleep(ms) { return new Promise(resolve => setTimeout(resolve, ms)) }
  }
}
</script>

<style lang="scss" scoped>
@import './styles/index.scss';

.image-preview-bar {
  display: flex;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 6px 6px 0 0;
  border-bottom: 1px solid #e4e7ed;
}
.image-preview-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  background: #fff;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}
.preview-thumb {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
}
.preview-label {
  font-size: 12px;
  color: #909399;
}
.preview-remove {
  cursor: pointer;
  color: #909399;
  font-size: 14px;
  &:hover { color: #f56c6c; }
}
.msg-image-wrap {
  margin-top: 8px;
}
.msg-image {
  max-width: 240px;
  max-height: 180px;
  border-radius: 6px;
  cursor: pointer;
  border: 1px solid #e4e7ed;
}
</style>
