import { http } from './http'
import type { ApiResponse } from '../types/api'
import type { CategoryOption, PageResult, Product, ProductForm, ProductQuery } from '../types/product'

export function getCategoryOptions() {
  return http.get<ApiResponse<CategoryOption[]>>('/categories/options')
}

export function getProducts(params: ProductQuery) {
  return http.get<ApiResponse<PageResult<Product>>>('/products', { params })
}

export function createProduct(data: ProductForm) {
  return http.post<ApiResponse<number>>('/products', data)
}

export function updateProduct(id: number, data: ProductForm) {
  return http.put<ApiResponse<void>>(`/products/${id}`, data)
}

export function updateProductStatus(id: number, status: number) {
  return http.patch<ApiResponse<void>>(`/products/${id}/status`, { status })
}

export function deleteProduct(id: number) {
  return http.delete<ApiResponse<void>>(`/products/${id}`)
}
