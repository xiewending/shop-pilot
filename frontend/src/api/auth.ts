import { http } from './http'
import type { ApiResponse } from '../types/api'
import type { LoginRequest, LoginResponse } from '../types/auth'

export function loginApi(data: LoginRequest) {
  return http.post<ApiResponse<LoginResponse>>('/auth/login', data)
}
