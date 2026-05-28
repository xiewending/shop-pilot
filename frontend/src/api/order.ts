import { http } from './http'
import type { ApiResponse } from '../types/api'
import type { Order, OrderDetail, OrderQuery, OrderStatusOption, PageResult } from '../types/order'

export function getOrderStatusOptions() {
  return http.get<ApiResponse<OrderStatusOption[]>>('/orders/status-options')
}

export function getOrders(params: OrderQuery) {
  return http.get<ApiResponse<PageResult<Order>>>('/orders', { params })
}

export function getOrderDetail(id: number) {
  return http.get<ApiResponse<OrderDetail>>(`/orders/${id}`)
}

export function updateOrderStatus(id: number, status: number) {
  return http.patch<ApiResponse<void>>(`/orders/${id}/status`, { status })
}
