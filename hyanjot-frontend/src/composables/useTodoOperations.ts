import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getItemById,
  createTodo,
  updateTodo,
  toggleItemStatus,
  deleteItem,
  incrementProgress,
  decrementProgress,
  toggleSubtask,
  deleteSubtask,
  setProgress,
} from '@/api/itemService'
import type { TodoItem, Subtask } from '@/types/item'
import axios from 'axios'

interface TodoItemData {
  deadline?: string
  priority?: string
  progressMode?: boolean | null
  progressCurrent?: number | null
  progressTotal?: number | null
  subtasks?: string | Subtask[]
}

interface ItemResponse {
  id?: number
  type: string
  title?: string
  content?: string
  todoItem?: TodoItemData
  deadline?: string
  [key: string]: unknown
}

export function useTodoOperations() {
  const itemDetails = ref<Record<number, TodoItem>>({})
  const expandedTodos = ref<Record<number, boolean>>({})
  
  // 回调函数，用于在进度更新后刷新Item列表
  let onProgressUpdateCallback: (() => void) | null = null
  
  const setProgressUpdateCallback = (callback: () => void) => {
    onProgressUpdateCallback = callback
  }

  // 加载TODO详情
  const loadItemDetails = async (id: number) => {
    try {
      const response = (await getItemById(id)) as unknown as ItemResponse

      if (response.type === 'TODO') {
        let todoData: TodoItemData = {}

        if (response.todoItem) {
          todoData = response.todoItem
        } else if ('deadline' in response) {
          todoData = response
        }

        // 解析subtasks JSON
        let subtasks: Subtask[] = []
        if (todoData.subtasks) {
          if (typeof todoData.subtasks === 'string') {
            try {
              subtasks = JSON.parse(todoData.subtasks)
            } catch {
              subtasks = []
            }
          } else if (Array.isArray(todoData.subtasks)) {
            subtasks = todoData.subtasks
          }
        }

        itemDetails.value[id] = {
          ...response,
          deadline: todoData.deadline || '',
          priority: (todoData.priority as 'low' | 'medium' | 'high') || 'medium',
          progressMode: todoData.progressMode !== undefined ? todoData.progressMode : null,
          progressCurrent: todoData.progressCurrent ?? 0,
          progressTotal: todoData.progressTotal,
          subtasks: subtasks || [],
        } as TodoItem
      }
    } catch (error) {
      console.error('加载TODO详情失败:', error)
      ElMessage.error('加载详情失败')
    }
  }

  // 切换详情展开/收起
  const toggleDetails = async (id: number) => {
    if (expandedTodos.value[id]) {
      expandedTodos.value[id] = false
    } else {
      if (!itemDetails.value[id]) {
        await loadItemDetails(id)
      }
      expandedTodos.value[id] = true
    }
  }

  // 更新TODO
  const updateTodoItem = async (id: number, data: { title: string; content?: string; priority: string; deadline: string }) => {
    const todo = itemDetails.value[id]
    if (!todo) return

    const updateData: TodoItem = {
      ...todo,
      title: data.title,
      content: data.content !== undefined ? data.content : todo.content,
      priority: data.priority as 'low' | 'medium' | 'high',
      deadline: data.deadline,
    } as TodoItem

    await updateTodo(id, updateData)
    await loadItemDetails(id)
    expandedTodos.value[id] = true
  }

  // 进度操作
  const incrementProgressValue = async (id: number) => {
    await incrementProgress(id)
    await loadItemDetails(id)
    expandedTodos.value[id] = true
    // 刷新Item列表以更新status
    if (onProgressUpdateCallback) {
      onProgressUpdateCallback()
    }
  }

  const decrementProgressValue = async (id: number) => {
    await decrementProgress(id)
    await loadItemDetails(id)
    expandedTodos.value[id] = true
    // 刷新Item列表以更新status
    if (onProgressUpdateCallback) {
      onProgressUpdateCallback()
    }
  }

  const saveProgressTotal = async (id: number, total: number) => {
    const todo = itemDetails.value[id]
    if (!todo) return

    await setProgress(id, todo.progressCurrent || 0, total)
    await loadItemDetails(id)
    expandedTodos.value[id] = true
    // 刷新Item列表以更新status
    if (onProgressUpdateCallback) {
      onProgressUpdateCallback()
    }
  }

  // 子任务操作
  const toggleSubtaskStatus = async (id: number, index: number) => {
    await toggleSubtask(id, index)
    await loadItemDetails(id)
    expandedTodos.value[id] = true
    // 刷新Item列表以更新status（当所有子任务完成时）
    if (onProgressUpdateCallback) {
      onProgressUpdateCallback()
    }
  }

  const deleteSubtaskItem = async (id: number, index: number) => {
    await deleteSubtask(id, index)
    await loadItemDetails(id)
    expandedTodos.value[id] = true
    // 刷新Item列表以更新status（当所有子任务完成时）
    if (onProgressUpdateCallback) {
      onProgressUpdateCallback()
    }
  }

  // 添加子任务
  const addSubtaskItem = async (id: number, text: string) => {
    interface AddSubtaskResponse {
      id?: number
      deadline?: string
      priority?: string
      progressMode?: boolean | null
      progressCurrent?: number | null
      progressTotal?: number | null
      subtasks?: string
      error?: boolean
      message?: string
    }

    let result: AddSubtaskResponse | null = null
    try {
      const response = await axios.post(
        `http://localhost:8080/api/item/${id}/subtask`,
        { text },
        {
          headers: {
            'Content-Type': 'application/json',
          },
        },
      )
      result = response.data

      if (result && result.error) {
        throw new Error(result.message || '添加子任务失败')
      }
    } catch (axiosError: unknown) {
      interface AxiosErrorResponse {
        response?: {
          status: number
          data?: {
            error?: boolean
            message?: string
          }
        }
        message?: string
      }

      const error = axiosError as AxiosErrorResponse
      if (error?.response) {
        const errorData = error.response.data
        if (errorData?.error && errorData?.message) {
          throw new Error(errorData.message)
        }
      }
      if (error?.message) {
        throw new Error(error.message)
      }
      throw new Error('添加子任务失败，请查看后端日志')
    }

    if (!result) {
      // 如果返回为空，重新加载详情
      await new Promise((resolve) => setTimeout(resolve, 300))
      await loadItemDetails(id)
      expandedTodos.value[id] = true
      return
    }

    // 解析返回的subtasks
    let subtasks: Subtask[] = []
    if (result.subtasks) {
      if (typeof result.subtasks === 'string') {
        try {
          subtasks = JSON.parse(result.subtasks)
        } catch {
          subtasks = []
        }
      } else if (Array.isArray(result.subtasks)) {
        subtasks = result.subtasks
      }
    }

    // 更新本地数据
    if (itemDetails.value[id]) {
      itemDetails.value[id] = {
        ...itemDetails.value[id],
        subtasks: subtasks,
      }
    }

    await loadItemDetails(id)
    expandedTodos.value[id] = true
    // 刷新Item列表以更新status（当所有子任务完成时，或添加子任务后可能变成未完成）
    if (onProgressUpdateCallback) {
      onProgressUpdateCallback()
    }
  }

  return {
    itemDetails,
    expandedTodos,
    loadItemDetails,
    toggleDetails,
    updateTodoItem,
    incrementProgressValue,
    decrementProgressValue,
    saveProgressTotal,
    toggleSubtaskStatus,
    deleteSubtaskItem,
    addSubtaskItem,
    setProgressUpdateCallback,
  }
}
