import { http } from './http'
import type { ApiResponse } from '../types/api'

export interface UploadResponse {
  url: string
}

export function uploadProductImage(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post<ApiResponse<UploadResponse>>('/upload/product-image', formData)
}
