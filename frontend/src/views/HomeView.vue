<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Box, Connection, Refresh, SwitchButton, Tickets } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

import { getHealth } from '../api/health'
import { useAuthStore } from '../stores/auth'
import type { HealthResponse } from '../types/health'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const health = ref<HealthResponse | null>(null)
const errorMessage = ref('')

async function loadHealth() {
  loading.value = true
  errorMessage.value = ''

  try {
    const { data } = await getHealth()
    health.value = data
  } catch (error) {
    health.value = null
    errorMessage.value = '无法连接后端服务'
    ElMessage.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

async function handleLogout() {
  authStore.logout()
  await router.replace('/login')
}

onMounted(loadHealth)
</script>

<template>
  <main class="page-shell">
    <section class="workspace">
      <header class="topbar">
        <div>
          <p class="eyebrow">电商运营后台</p>
          <h1>ShopPilot</h1>
          <p class="welcome">欢迎，{{ authStore.user?.nickname ?? authStore.user?.username }}</p>
        </div>
        <div class="actions">
          <el-button :icon="Refresh" :loading="loading" @click="loadHealth">刷新状态</el-button>
          <el-button :icon="SwitchButton" @click="handleLogout">退出登录</el-button>
        </div>
      </header>

      <section class="status-panel">
        <div class="status-icon">
          <el-icon><Connection /></el-icon>
        </div>
        <div class="status-copy">
          <p class="label">后端状态</p>
          <h2>{{ health?.status ?? 'UNKNOWN' }}</h2>
          <p>{{ health?.message ?? (errorMessage || '正在读取后端健康状态') }}</p>
          <span v-if="health?.timestamp">更新时间：{{ health.timestamp }}</span>
        </div>
      </section>

      <section class="module-grid">
        <button class="module-card" type="button" @click="router.push('/products')">
          <span class="module-icon">
            <el-icon><Box /></el-icon>
          </span>
          <span>
            <strong>商品管理</strong>
            <small>维护商品目录、库存、价格和上下架状态。</small>
          </span>
        </button>
        <button class="module-card" type="button" @click="router.push('/orders')">
          <span class="module-icon">
            <el-icon><Tickets /></el-icon>
          </span>
          <span>
            <strong>订单管理</strong>
            <small>查看订单、核对明细，并按规则流转订单状态。</small>
          </span>
        </button>
      </section>
    </section>
  </main>
</template>
