import { http } from './http'
import type { HealthResponse } from '../types/health'

export function getHealth() {
  return http.get<HealthResponse>('/health')
}
