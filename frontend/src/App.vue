<template>
  <div class="chat-container">
    <!-- 顶部标题栏 -->
    <div class="chat-header">
      <div class="header-icon">🤖</div>
      <span class="header-title">AIChat 智能客服</span>
    </div>

    <!-- 消息列表 -->
    <div class="chat-messages" ref="chatMessages">
      <div v-for="(msg, idx) in messages" :key="idx" class="message-row" :class="msg.role">
        <div class="bubble" :class="msg.role">
          <span class="bubble-text">{{ msg.content }}</span>
        </div>
      </div>
      <div ref="bottomRef"></div>
    </div>

    <!-- 底部输入区 -->
    <div class="chat-input">
      <input 
        v-model="input" 
        type="text" 
        placeholder="输入你的问题..." 
        @keyup.enter="send"
        class="input-field"
      />
      <button class="send-btn" @click="send">发送</button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue';
import axios from 'axios';

const sessionId = 'user-' + Math.random().toString(36).slice(2);
const input = ref('');
const messages = ref([]);
const chatMessages = ref(null);
const bottomRef = ref(null);

async function send() {
  if (!input.value.trim()) return;
  const question = input.value;
  messages.value.push({ role: 'user', content: question });
  input.value = '';

  try {
    const formData = new FormData();
    formData.append('sessionId', sessionId);
    formData.append('question', question);
    const res = await axios.post('http://localhost:8080/chat', formData);
    messages.value.push({ role: 'assistant', content: res.data });
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '网络错误，请稍后再试' });
  }
  
  await nextTick();
  bottomRef.value?.scrollIntoView({ behavior: 'smooth' });
}
</script>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background: #f0f2f5;
}

/* 主容器 */
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: 700px;
  margin: 0 auto;
  background: #ffffff;
  box-shadow: 0 0 20px rgba(0,0,0,0.05);
}

/* 头部 */
.chat-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.header-icon {
  font-size: 28px;
}

/* 消息区域 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f7f8fc;
}

.message-row {
  display: flex;
  margin-bottom: 24px;
}

.message-row.user {
  justify-content: flex-end;
}

.message-row.assistant {
  justify-content: flex-start;
}

/* 气泡样式 */
.bubble {
  max-width: 75%;
  padding: 12px 18px;
  border-radius: 18px;
  word-break: break-word;
  line-height: 1.6;
  font-size: 15px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
  position: relative;
}

.bubble.user {
  background: #667eea;
  color: white;
  border-bottom-right-radius: 4px;
}

.bubble.assistant {
  background: white;
  color: #333;
  border-bottom-left-radius: 4px;
  border: 1px solid #eaecf0;
}

.bubble-text {
  white-space: pre-wrap;
}

/* 输入区 */
.chat-input {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  background: white;
  border-top: 1px solid #eee;
}

.input-field {
  flex: 1;
  padding: 12px 18px;
  border: 2px solid #e0e0e0;
  border-radius: 24px;
  font-size: 15px;
  outline: none;
  transition: border-color 0.2s;
}

.input-field:focus {
  border-color: #667eea;
}

.send-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 24px;
  padding: 12px 28px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}

.send-btn:hover {
  opacity: 0.9;
}

.send-btn:active {
  transform: scale(0.98);
}

/* 滚动条美化 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #d0d5dd;
  border-radius: 3px;
}
</style>