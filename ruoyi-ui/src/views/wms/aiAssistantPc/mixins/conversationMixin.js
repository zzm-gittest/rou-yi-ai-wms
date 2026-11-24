import {
  startPcPurchaseOrderChat,
  clearPcPurchaseOrderContext
} from '@/api/wms/aiAssistantPc'

export default {
  methods: {
    async startNewConversation() {
      const startResp = await startPcPurchaseOrderChat()
      const data = startResp.data || {}
      this.conversationId = data.conversationId || ''
      this.activeConversationId = this.conversationId
      this.messages = []
      this.quickActions = []
      this.context = {}
      this.candidateSelectionCache = { goods: [] }
      this.showNewMessageHint = false
      this.pushAssistant(data)
      this.syncConversationSnapshot()
    },
    restoreConversations() {
      const raw = window.localStorage.getItem(this.storageKey)
      if (!raw) {
        this.conversations = []
        return
      }
      try {
        const parsed = JSON.parse(raw)
        this.conversations = Array.isArray(parsed)
          ? parsed.sort((a, b) => (b.updatedAt || 0) - (a.updatedAt || 0))
          : []
      } catch (e) {
        this.conversations = []
      }
    },
    persistConversations() {
      window.localStorage.setItem(this.storageKey, JSON.stringify(this.conversations))
    },
    activateConversation(conversationId) {
      const current = this.conversations.find(item => item.conversationId === conversationId)
      if (!current) return
      this.activeConversationId = current.conversationId
      this.conversationId = current.conversationId
      this.messages = current.messages || []
      this.quickActions = current.quickActions || []
      this.context = current.context || {}
      this.candidateSelectionCache = current.candidateSelectionCache || { goods: [] }
      this.draft = ''
      this.showNewMessageHint = false
      this.$nextTick(this.scrollBottom)
    },
    deriveConversationTitle() {
      if (this.context && (this.context.domainType || this.context.lastCreatedDomainType)) {
        const domainType = this.context.domainType || this.context.lastCreatedDomainType
        return this.formatDomainType(domainType)
      }
      const firstUser = this.messages.find(item => item.role === 'user' && item.text)
      const fallback = this.messages.find(item => item.role === 'assistant' && item.text)
      const source = firstUser ? firstUser.text : (fallback ? fallback.text : '新对话')
      return (source || '新对话').replace(/\s+/g, ' ').slice(0, 18)
    },
    deriveConversationSummary() {
      const last = this.messages[this.messages.length - 1]
      return last && last.text ? last.text.replace(/\s+/g, ' ').slice(0, 28) : ''
    },
    syncConversationSnapshot() {
      if (!this.conversationId) return
      const snapshot = {
        conversationId: this.conversationId,
        title: this.deriveConversationTitle(),
        summary: this.deriveConversationSummary(),
        updatedAt: Date.now(),
        messages: this.messages,
        quickActions: this.quickActions,
        context: this.context,
        candidateSelectionCache: this.candidateSelectionCache
      }
      const index = this.conversations.findIndex(item => item.conversationId === this.conversationId)
      if (index >= 0) {
        this.conversations.splice(index, 1, snapshot)
      } else {
        this.conversations.unshift(snapshot)
      }
      this.conversations = this.conversations.slice().sort((a, b) => (b.updatedAt || 0) - (a.updatedAt || 0))
      this.activeConversationId = this.conversationId
      this.persistConversations()
    },
    async clearContext() {
      if (!this.conversationId) return
      await clearPcPurchaseOrderContext(this.conversationId)
      this.messages = []
      this.quickActions = []
      this.context = {}
      this.candidateSelectionCache = { goods: [] }
      this.syncConversationSnapshot()
      await this.startNewConversation()
    }
  }
}
