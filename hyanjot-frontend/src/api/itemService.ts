import request from './request'
import type { Item, TodoItem, DeleteResponse } from '@/types/item'

// 硬编码的用户ID（主人说的先用hardcode）
const CURRENT_USER_ID = 1

// 获取用户的所有备忘项
export const getUserItems = (): Promise<Item[]> => {
  return request.get(`/api/item/user/${CURRENT_USER_ID}`)
}

// 获取单个备忘项详情（包含子表数据）
export const getItemById = (id: number): Promise<Item> => {
  return request.get(`/api/item/${id}`)
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

// 创建TODO类型的备忘项
export const createTodo = (todo: TodoItem): Promise<Item> => {
  // 普通模式：progressMode为null时，subtasks应该为null
  // 子任务模式：progressMode为false时，subtasks可以是数组
  const subtasks = todo.progressMode === null || todo.progressMode === undefined
    ? null
    : (todo.progressMode === false && todo.subtasks && todo.subtasks.length > 0
        ? todo.subtasks.map(s => ({
            text: s.text,
            completed: s.completed || false
          }))
        : null)

  return request.post('/api/item/todo', {
    userId: CURRENT_USER_ID,
    title: todo.title,
    content: todo.content,
    sortOrder: todo.sortOrder,
    deadline: todo.deadline,
    priority: todo.priority,
    progressMode: todo.progressMode,
    progressTotal: todo.progressTotal,
    subtasks: subtasks
  })
}

// 更新TODO类型的备忘项
export const updateTodo = (id: number, todo: TodoItem): Promise<Item> => {
  return request.put(`/api/item/todo/${id}`, {
    userId: CURRENT_USER_ID,
    title: todo.title,
    content: todo.content,
    sortOrder: todo.sortOrder,
    deadline: todo.deadline,
    priority: todo.priority,
    progressMode: todo.progressMode,
    progressTotal: todo.progressTotal,
    subtasks: todo.subtasks?.map(s => ({
      text: s.text,
      completed: s.completed || false
    }))
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

// TODO相关操作
export const incrementProgress = (id: number): Promise<any> => {
  return request.put(`/api/item/${id}/progress/increment`)
}

export const decrementProgress = (id: number): Promise<any> => {
  return request.put(`/api/item/${id}/progress/decrement`)
}

export const setProgress = (id: number, current: number, total: number): Promise<any> => {
  return request.put(`/api/item/${id}/progress`, { current, total })
}

export const toggleSubtask = (id: number, index: number): Promise<any> => {
  return request.put(`/api/item/${id}/subtask/${index}/toggle`)
}

export const addSubtask = (id: number, text: string): Promise<any> => {
  return request.post(`/api/item/${id}/subtask`, { text })
}

export const deleteSubtask = (id: number, index: number): Promise<any> => {
  return request.delete(`/api/item/${id}/subtask/${index}`)
}
