<template>
  <view class="chat-page">

    <!-- ===== 顶部标题栏（KFC红） ===== -->
    <view class="header">
      <view class="header-left">
        <text class="header-title">🍗 KFC售后智能客服</text>
        <text class="header-sub">7×24 小时在线 · 3 分钟内响应</text>
      </view>
    </view>

    <!-- ===== 消息列表 ===== -->
    <scroll-view scroll-y class="msg-list" :scroll-into-view="scrollInto">
      <view v-for="(m, i) in messages" :key="i" :id="'msg' + i"
            :class="['msg-row', m.role === 'me' ? 'me' : 'ai']">

        <!-- AI 头像 -->
        <view v-if="m.role !== 'me'" class="avatar ai-avatar">🍗</view>

        <view class="bubble" :class="m.role === 'me' ? 'bubble-me' : 'bubble-ai'">
          {{ m.content }}
        </view>

        <!-- 用户头像 -->
        <view v-if="m.role === 'me'" class="avatar me-avatar">🧑</view>
      </view>
    </scroll-view>

    <!-- ===== 底部输入区 ===== -->
    <view class="input-bar">
      <input v-model="text" class="input" placeholder="请输入您的问题..."
             placeholder-class="ph" confirm-type="send" @confirm="send" />
      <button class="send-btn" @click="send" :disabled="sending">
        {{ sending ? '回复中...' : '发送' }}
      </button>
    </view>

  </view>
</template>

<script setup>
import { ref } from 'vue'

// ====== 改成你后端的地址 + 端口（要和 server.port 一致） ======
const BASE_URL = 'http://localhost:9000'

const messages = ref([
  { role: 'ai', content: '您好，我是KFC售后智能客服 🍟\n请问有什么可以帮您？\n\n✅ 漏送/错送/缺料\n✅ 餐品洒漏破损\n✅ 退款/发票咨询\n✅ 超时补偿\n\n（请提供订单号与下单时间，方便快速处理）' }
])
const text = ref('')
const sending = ref(false)
const scrollInto = ref('')

function send() {
  const msg = text.value.trim()
  if (!msg || sending.value) return

  messages.value.push({ role: 'me', content: msg })
  text.value = ''
  scrollToBottom()

  sending.value = true
  uni.request({
    url: BASE_URL + '/ai/chat-json',
    method: 'POST',
    header: { 'Content-Type': 'application/json' },
    data: { message: msg },
    success: (res) => {
      const reply = res.data && res.data.reply ? res.data.reply : '（后端无回复，请检查服务是否启动）'
      messages.value.push({ role: 'ai', content: reply })
    },
    fail: (err) => {
      messages.value.push({
        role: 'ai',
        content: '请求失败，请确认：\n1. 后端是否已启动\n2. 是否勾选"不校验合法域名"\n' + JSON.stringify(err)
      })
    },
    complete: () => {
      sending.value = false
      scrollToBottom()
    }
  })
}

function scrollToBottom() {
  setTimeout(() => {
    scrollInto.value = 'msg' + (messages.value.length - 1)
  }, 50)
}
</script>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #fff7f7;
}

/* ===== 顶部标题栏 ===== */
.header {
  background: linear-gradient(135deg, #e4002b, #ff5733); /* KFC 红渐变 */
  padding: 24rpx 30rpx;
  display: flex;
  align-items: center;
  border-bottom: 4rpx solid #b30021;
}
.header-title {
  color: #ffffff;
  font-size: 36rpx;
  font-weight: bold;
  letter-spacing: 2rpx;
}
.header-sub {
  color: #ffe3e3;
  font-size: 22rpx;
  margin-top: 6rpx;
  display: block;
}

/* ===== 消息列表 ===== */
.msg-list {
  flex: 1;
  padding: 24rpx;
  box-sizing: border-box;
}
.msg-row {
  display: flex;
  margin-bottom: 24rpx;
  align-items: flex-start;
}
.msg-row.me {
  justify-content: flex-end;
}
.msg-row.ai {
  justify-content: flex-start;
}

/* 头像 */
.avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  flex-shrink: 0;
  margin-top: 6rpx;
}
.me-avatar {
  background: #ffe3e3;
  border: 2rpx solid #e4002b;
  margin-left: 16rpx;
}
.ai-avatar {
  background: #fff0c2;
  border: 2rpx solid #e4002b;
  margin-right: 16rpx;
}

/* 气泡 */
.bubble {
  max-width: 66%;
  padding: 20rpx 26rpx;
  border-radius: 20rpx;
  font-size: 28rpx;
  line-height: 1.6;
  word-break: break-all;
  white-space: pre-wrap;
}

/* 我发的：红底白字 */
.bubble-me {
  background: linear-gradient(135deg, #e4002b, #ff4d2e);
  color: #ffffff;
  border: 2rpx solid #b30021;
  border-top-right-radius: 4rpx;
}

/* AI 发的：白底 + 红色边框 */
.bubble-ai {
  background: #ffffff;
  color: #4a2b2b;
  border: 2rpx solid #e4002b;
  border-top-left-radius: 4rpx;
  box-shadow: 0 4rpx 12rpx rgba(228, 0, 43, 0.08);
}

/* ===== 底部输入栏 ===== */
.input-bar {
  display: flex;
  align-items: center;
  padding: 18rpx 24rpx;
  border-top: 4rpx solid #e4002b; /* 红色顶边 */
  background: #ffffff;
}
.input {
  flex: 1;
  height: 76rpx;
  background: #fff5f5;
  border: 2rpx solid #e4002b;
  border-radius: 40rpx;
  padding: 0 30rpx;
  font-size: 28rpx;
}
.ph {
  color: #d99a9a;
}
.send-btn {
  margin-left: 20rpx;
  height: 76rpx;
  line-height: 76rpx;
  padding: 0 40rpx;
  font-size: 28rpx;
  color: #ffffff;
  background: linear-gradient(135deg, #e4002b, #ff5733);
  border: 2rpx solid #b30021;
  border-radius: 40rpx;
}
.send-btn[disabled] {
  opacity: 0.6;
}
</style>