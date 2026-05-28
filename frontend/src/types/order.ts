export interface OrderStatusOption {
  value: number
  label: string
}

export interface Order {
  id: number
  orderNo: string
  customerName: string
  customerPhone: string
  totalAmount: number
  status: number
  statusText: string
  shippingAddress: string
  remark?: string
  createdAt: string
  updatedAt: string
}

export interface OrderItem {
  id: number
  productId: number
  productName: string
  unitPrice: number
  quantity: number
  totalPrice: number
}

export interface OrderDetail extends Order {
  items: OrderItem[]
}

export interface OrderQuery {
  page: number
  size: number
  keyword?: string
  status?: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}
