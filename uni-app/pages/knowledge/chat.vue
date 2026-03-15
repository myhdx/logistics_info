<template>
  <view class="page">
    <!-- 顶部背景 -->
    <view class="header-bg">
      <text class="header-title">物流知识库</text>
      <text class="header-subtitle">智能问答，解答物流相关问题</text>
    </view>

    <!-- 对话区域 -->
    <scroll-view class="chat-area" scroll-y :scroll-into-view="scrollInto">
      <view class="chat-list">
        <view 
          v-for="(msg, index) in messages" 
          :key="index"
          :id="'msg-' + index"
          class="chat-item"
          :class="msg.role"
        >
          <view class="chat-avatar">
            <text>{{ msg.role === 'user' ? '我' : 'AI' }}</text>
          </view>
          <view class="chat-content">
            <text class="chat-text">{{ msg.content }}</text>
          </view>
        </view>
        
        <view v-if="messages.length === 0" class="empty-tip">
          <text>您好！我是物流知识问答助手</text>
          <text>有什么物流相关问题可以问我哦</text>
        </view>
      </view>
    </scroll-view>

    <!-- 输入区域 -->
    <view class="input-area">
      <input 
        class="input-box" 
        type="text" 
        v-model="inputText" 
        placeholder="请输入问题..."
        @confirm="sendMessage"
      />
      <button class="send-btn" @click="sendMessage" :disabled="!inputText.trim() || sending">
        发送
      </button>
    </view>
  </view>
</template>

<script>
import request from '../../utils/request.js';

export default {
  data() {
    return {
      messages: [],
      inputText: '',
      sending: false,
      scrollInto: '',
      sessionId: '',
      userId: null
    };
  },
  onLoad() {
    // 生成会话ID
    this.sessionId = 'session_' + Date.now();
    // 获取用户ID（从缓存）
    this.userId = uni.getStorageSync('userId') || 1;
  },
  methods: {
    async sendMessage() {
      const text = this.inputText.trim();
      if (!text || this.sending) return;
      
      // 添加用户消息
      this.messages.push({ role: 'user', content: text });
      this.inputText = '';
      this.scrollToBottom();
      
      this.sending = true;
      
      try {
        const res = await request.knowledgeChat({
          userId: this.userId,
          sessionId: this.sessionId,
          message: text
        });
        
        // 添加AI回复
        this.messages.push({ role: 'assistant', content: res.reply });
      } catch (e) {
        this.messages.push({ 
          role: 'assistant', 
          content: '抱歉，请稍后重试' 
        });
      }
      
      this.sending = false;
      this.scrollToBottom();
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const index = this.messages.length - 1;
        this.scrollInto = 'msg-' + index;
      });
    }
  }
};
</script>

<style lang="scss" scoped>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #F5F5F5;
}

/* 顶部背景 */
.header-bg {
  background: linear-gradient(135deg, #E8F4FD 0%, #4A90E2 100%);
  padding: 60px 16px 30px;
  text-align: center;
  
  .header-title {
    font-size: 20px;
    font-weight: 600;
    color: #FFFFFF;
    display: block;
  }
  
  .header-subtitle {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.8);
    margin-top: 8px;
    display: block;
  }
}

/* 对话区域 */
.chat-area {
  flex: 1;
  padding: 16px;
}

.chat-list {
  .chat-item {
    display: flex;
    margin-bottom: 16px;
    
    &.user {
      flex-direction: row-reverse;
      
      .chat-avatar {
        background: #4A90E2;
      }
      
      .chat-content {
        background: #4A90E2;
        
        .chat-text {
          color: #FFFFFF;
        }
      }
    }
    
    .chat-avatar {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background: #7B68EE;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 12px;
      color: #FFFFFF;
      flex-shrink: 0;
    }
    
    .chat-content {
      max-width: 70%;
      background: #FFFFFF;
      border-radius: 12px;
      padding: 12px;
      margin: 0 12px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      
      .chat-text {
        font-size: 14px;
        color: #333333;
        line-height: 1.5;
      }
    }
  }
  
  .empty-tip {
    text-align: center;
    padding: 60px 20px;
    color: #999999;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
}

/* 输入区域 */
.input-area {
  background: #FFFFFF;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  border-top: 1px solid #F5F5F5;
  
  .input-box {
    flex: 1;
    background: #F5F5F5;
    border-radius: 20px;
    padding: 8px 16px;
    font-size: 14px;
  }
  
  .send-btn {
    background: #4A90E2;
    color: #FFFFFF;
    border-radius: 20px;
    padding: 8px 20px;
    font-size: 14px;
    margin-left: 12px;
    border: none;
    
    &[disabled] {
      background: #CCCCCC;
    }
  }
}
</style>