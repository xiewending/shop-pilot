<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  ElMessage,
  ElMessageBox,
  type FormInstance,
  type FormRules,
  type UploadProps,
  type UploadRequestOptions
} from 'element-plus'
import { Close, Delete, Edit, Plus, Refresh, Search, UploadFilled } from '@element-plus/icons-vue'

import {
  createProduct,
  deleteProduct,
  getCategoryOptions,
  getProducts,
  updateProduct,
  updateProductStatus
} from '../api/product'
import { uploadProductImage } from '../api/upload'
import { useAuthStore } from '../stores/auth'
import type { CategoryOption, Product, ProductForm } from '../types/product'

const authStore = useAuthStore()
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingProduct = ref<Product | null>(null)
const formRef = ref<FormInstance>()
const products = ref<Product[]>([])
const categories = ref<CategoryOption[]>([])
const total = ref(0)

const canCreate = computed(() => authStore.hasPermission('product:add'))
const canEdit = computed(() => authStore.hasPermission('product:edit'))
const canDelete = computed(() => authStore.hasPermission('product:delete'))
const canChangeStatus = computed(() => authStore.hasPermission('product:status'))

const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
  categoryId: undefined as number | undefined,
  status: undefined as number | undefined
})

const form = reactive<ProductForm>({
  categoryId: undefined,
  name: '',
  price: 0,
  stock: 0,
  status: 0,
  description: '',
  imageUrl: ''
})

const dialogTitle = computed(() => (editingProduct.value ? '编辑商品' : '新增商品'))

const rules: FormRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入商品价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入商品库存', trigger: 'blur' }]
}

async function loadCategories() {
  const { data } = await getCategoryOptions()
  categories.value = data.data
}

async function loadProducts() {
  loading.value = true
  try {
    const { data } = await getProducts({
      page: query.page,
      size: query.size,
      keyword: query.keyword || undefined,
      categoryId: query.categoryId,
      status: query.status
    })
    products.value = data.data.records
    total.value = data.data.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  loadProducts()
}

function resetForm() {
  editingProduct.value = null
  form.categoryId = undefined
  form.name = ''
  form.price = 0
  form.stock = 0
  form.status = 0
  form.description = ''
  form.imageUrl = ''
  formRef.value?.clearValidate()
}

function openCreateDialog() {
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(product: Product) {
  editingProduct.value = product
  form.categoryId = product.categoryId
  form.name = product.name
  form.price = Number(product.price)
  form.stock = product.stock
  form.status = product.status
  form.description = product.description ?? ''
  form.imageUrl = product.imageUrl ?? ''
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

const beforeImageUpload: UploadProps['beforeUpload'] = (rawFile) => {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif']
  if (!allowedTypes.includes(rawFile.type)) {
    ElMessage.error('仅支持 JPG、PNG、WEBP、GIF 图片')
    return false
  }
  if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

async function handleImageUpload(options: UploadRequestOptions) {
  try {
    const { data } = await uploadProductImage(options.file)
    form.imageUrl = data.data.url
    options.onSuccess(data.data)
    ElMessage.success('图片上传成功')
  } catch (error) {
    options.onError(error as never)
  }
}

function removeImage() {
  form.imageUrl = ''
}

async function submitForm() {
  const valid = await formRef.value?.validate()
  if (!valid) {
    return
  }

  saving.value = true
  try {
    if (editingProduct.value) {
      await updateProduct(editingProduct.value.id, form)
      ElMessage.success('商品已更新')
    } else {
      await createProduct(form)
      ElMessage.success('商品已创建')
    }
    dialogVisible.value = false
    await loadProducts()
  } finally {
    saving.value = false
  }
}

async function handleStatusChange(product: Product, nextStatus: boolean) {
  const status = nextStatus ? 1 : 0
  const previous = product.status
  product.status = status
  try {
    await updateProductStatus(product.id, status)
    ElMessage.success(status === 1 ? '商品已上架' : '商品已下架')
  } catch (error) {
    product.status = previous
  }
}

async function handleDelete(product: Product) {
  await ElMessageBox.confirm(`确认删除商品“${product.name}”吗？`, '删除确认', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await deleteProduct(product.id)
  ElMessage.success('商品已删除')
  await loadProducts()
}

onMounted(async () => {
  await loadCategories()
  await loadProducts()
})
</script>

<template>
  <section class="workspace wide">
    <header class="topbar">
      <div>
        <p class="eyebrow">商品运营</p>
        <h1>商品管理</h1>
      </div>
      <div class="actions">
        <el-button :icon="Refresh" :loading="loading" @click="loadProducts">刷新</el-button>
        <el-button v-if="canCreate" type="primary" :icon="Plus" @click="openCreateDialog">新增商品</el-button>
      </div>
    </header>

    <section class="toolbar">
      <el-input
        v-model="query.keyword"
        class="filter-input"
        clearable
        placeholder="搜索商品名称"
        :prefix-icon="Search"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="query.categoryId" class="filter-select" clearable placeholder="商品分类">
        <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
      </el-select>
      <el-select v-model="query.status" class="filter-select" clearable placeholder="上下架状态">
        <el-option label="上架" :value="1" />
        <el-option label="下架" :value="0" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
    </section>

    <el-table v-loading="loading" :data="products" class="data-table" border>
      <el-table-column label="图片" width="92">
        <template #default="{ row }">
          <el-image
            v-if="row.imageUrl"
            class="product-thumb"
            fit="cover"
            :src="row.imageUrl"
            :preview-src-list="[row.imageUrl]"
            preview-teleported
          />
          <span v-else class="table-muted">无图</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="商品名称" min-width="180" />
      <el-table-column prop="categoryName" label="分类" width="140" />
      <el-table-column prop="price" label="价格" width="120">
        <template #default="{ row }">¥{{ Number(row.price).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="110" />
      <el-table-column label="状态" width="130">
        <template #default="{ row }">
          <el-switch
            v-if="canChangeStatus"
            :model-value="row.status === 1"
            active-text="上架"
            inactive-text="下架"
            inline-prompt
            @change="(value: string | number | boolean) => handleStatusChange(row, Boolean(value))"
          />
          <el-tag v-else :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="更新时间" width="190" />
      <el-table-column v-if="canEdit || canDelete" label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <el-button v-if="canEdit" :icon="Edit" link type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button v-if="canDelete" :icon="Delete" link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        :total="total"
        @size-change="loadProducts"
        @current-change="loadProducts"
      />
    </div>
  </section>

  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item label="商品名称" prop="name">
        <el-input v-model="form.name" maxlength="128" show-word-limit />
      </el-form-item>
      <el-form-item label="商品分类" prop="categoryId">
        <el-select v-model="form.categoryId" class="full-width" placeholder="请选择商品分类">
          <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
        </el-select>
      </el-form-item>
      <div class="form-grid">
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" class="full-width" :min="0" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" class="full-width" :min="0" :step="1" />
        </el-form-item>
      </div>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio-button :label="1">上架</el-radio-button>
          <el-radio-button :label="0">下架</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="商品图片">
        <div class="image-upload-field">
          <el-image v-if="form.imageUrl" class="image-preview" fit="cover" :src="form.imageUrl" />
          <el-upload
            class="image-uploader"
            drag
            :show-file-list="false"
            :before-upload="beforeImageUpload"
            :http-request="handleImageUpload"
          >
            <el-icon class="upload-icon"><UploadFilled /></el-icon>
            <div class="upload-text">点击或拖拽上传图片</div>
            <template #tip>
              <div class="upload-tip">支持 JPG、PNG、WEBP、GIF，最大 2MB</div>
            </template>
          </el-upload>
          <el-button v-if="form.imageUrl" :icon="Close" plain type="danger" @click="removeImage">移除图片</el-button>
        </div>
      </el-form-item>
      <el-form-item label="商品描述" prop="description">
        <el-input v-model="form.description" maxlength="500" rows="3" show-word-limit type="textarea" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.product-thumb {
  width: 52px;
  height: 52px;
  border-radius: 6px;
  background: #f5f7fa;
}

.table-muted {
  color: #909399;
  font-size: 13px;
}

.image-upload-field {
  display: grid;
  gap: 12px;
}

.image-preview {
  width: 128px;
  height: 128px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  background: #f5f7fa;
}

.image-uploader {
  width: 100%;
}

.upload-icon {
  color: #909399;
  font-size: 28px;
}

.upload-text {
  color: #606266;
  font-size: 14px;
}

.upload-tip {
  color: #909399;
  font-size: 12px;
  line-height: 20px;
}
</style>
