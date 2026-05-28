<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Box, Connection, House, Refresh, Tickets } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

import { getHealth } from '../api/health'
import { getHotProducts } from '../api/product'
import { useAuthStore } from '../stores/auth'
import type { HealthResponse } from '../types/health'
import type { HotProduct } from '../types/product'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const hotLoading = ref(false)
const health = ref<HealthResponse | null>(null)
const hotProducts = ref<HotProduct[]>([])
const errorMessage = ref('')

const iconMap = {
  House,
  Box,
  Tickets
}
const menuCards = computed(() => authStore.menuRoutes.filter((menu) => menu.path !== '/dashboard'))

function resolveIcon(icon?: string) {
  return icon ? iconMap[icon as keyof typeof iconMap] ?? House : House
}

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

async function loadHotProducts() {
  if (!authStore.hasPermission('product:view')) {
    return
  }
  hotLoading.value = true
  try {
    const { data } = await getHotProducts(5)
    hotProducts.value = data.data
  } finally {
    hotLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadHealth(), loadHotProducts()])
})
</script>

<template>
  <section class="workspace">
    <header class="topbar">
      <div>
        <p class="eyebrow">电商运营后台</p>
        <h1>工作台</h1>
        <p class="welcome">欢迎，{{ authStore.user?.nickname ?? authStore.user?.username }}</p>
      </div>
      <div class="actions">
        <el-button :icon="Refresh" :loading="loading" @click="loadHealth">刷新状态</el-button>
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
      <button v-for="menu in menuCards" :key="menu.id" class="module-card" type="button" @click="router.push(menu.path)">
        <span class="module-icon">
          <el-icon><component :is="resolveIcon(menu.icon)" /></el-icon>
        </span>
        <span>
          <strong>{{ menu.name }}</strong>
          <small>{{ menu.permission === 'product:view' ? '维护商品目录、库存、价格和上下架状态。' : '查看订单、核对明细，并按规则流转订单状态。' }}</small>
        </span>
      </button>
    </section>

    <section v-if="authStore.hasPermission('product:view')" class="detail-panel hot-panel">
      <div class="topbar compact">
        <div>
          <p class="eyebrow">Redis 缓存</p>
          <h2>热门商品排行榜</h2>
        </div>
        <el-button :icon="Refresh" :loading="hotLoading" @click="loadHotProducts">刷新榜单</el-button>
      </div>
      <el-table v-loading="hotLoading" :data="hotProducts" border>
        <el-table-column type="index" label="排名" width="80" />
        <el-table-column prop="name" label="商品名称" min-width="160" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="price" label="价格" width="120">
          <template #default="{ row }">¥{{ Number(row.price).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="score" label="热度" width="100">
          <template #default="{ row }">{{ Number(row.score).toFixed(0) }}</template>
        </el-table-column>
      </el-table>
    </section>
  </section>
</template>
