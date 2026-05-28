export interface CategoryOption {
  id: number
  name: string
}

export interface Product {
  id: number
  categoryId: number
  categoryName: string
  name: string
  price: number
  stock: number
  status: number
  description?: string
  createdAt: string
  updatedAt: string
}

export interface ProductQuery {
  page: number
  size: number
  keyword?: string
  categoryId?: number
  status?: number
}

export interface ProductForm {
  categoryId?: number
  name: string
  price: number
  stock: number
  status: number
  description?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}
