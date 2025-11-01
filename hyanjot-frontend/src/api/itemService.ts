import request from './request'
import type { Item, DeleteResponse } from '@/types/item'

// 硬编码的用户ID（主人说的先用hardcode）
const CURRENT_USER_ID = 1

// 获取用户的所有备忘项
export const getUserItems = (): Promise<Item[]> => {
  return request.get(`/api/item/user/${CURRENT_USER_ID}`)
}

// 按类型获取备忘项
export const getItemsByType = (type: string): Promise<Item[]> => {
  return request.get(`/api/item/user/${CURRENT_USER_ID}/type/${type}`)
}

// 按状态获取备忘项
export const getItemsByStatus = (status: string): Promise<Item[]> => {
  return request.get(`/api/item/user/${CURRENT_USER_ID}/status/${status}`)
}

// 创建备忘项
export const createItem = (item: Item): Promise<Item> => {
  return request.post('/api/item', {
    ...item,
    userId: CURRENT_USER_ID
  })
}

// 更新备忘项
export const updateItem = (id: number, item: Item): Promise<Item> => {
  return request.put(`/api/item/${id}`, item)
}

// 切换完成状态
export const toggleItemStatus = (id: number): Promise<Item> => {
  return request.put(`/api/item/${id}/toggle`)
}

// 删除备忘项
export const deleteItem = (id: number): Promise<DeleteResponse> => {
  return request.delete(`/api/item/${id}`)
}
