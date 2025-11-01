// 基础接口 - 所有类型共有的字段
export interface BaseItem {
  id?: number
  userId: number
  type: 'TODO' | 'HABIT' | 'REMINDER'
  title: string
  content?: string
  status?: 'active' | 'completed' | 'deleted'
  sortOrder?: number
  createdAt?: string
  updatedAt?: string
}

// 子任务类型
export interface Subtask {
  text: string
  completed: boolean
}

// TODO类型 - 待办事项
export interface TodoItem extends BaseItem {
  type: 'TODO'
  // TODO专用字段
  deadline: string  // 必填：截止日期
  priority: 'low' | 'medium' | 'high'  // 必填：优先级
  progressMode: boolean  // true=进度模式, false=子任务模式
  // 进度模式字段
  progressCurrent?: number  // 当前进度
  progressTotal?: number    // 总进度
  // 子任务模式字段
  subtasks?: Subtask[]  // 子任务列表
}

// HABIT类型 - 习惯
export interface HabitItem extends BaseItem {
  type: 'HABIT'
  // HABIT专用字段（后续讨论后添加）
  repeatRule?: string
  streakDays?: number
  targetDays?: number
  lastCheckDate?: string
  checkHistory?: string
}

// REMINDER类型 - 提醒
export interface ReminderItem extends BaseItem {
  type: 'REMINDER'
  // REMINDER专用字段（后续讨论后添加）
  remindTime?: string
  repeatType?: string
  advanceMinutes?: number
  notified?: boolean
  nextRemindTime?: string
}

// 联合类型
export type Item = TodoItem | HabitItem | ReminderItem

export type ItemType = 'TODO' | 'HABIT' | 'REMINDER'
export type ItemStatus = 'active' | 'completed' | 'deleted'

// 删除响应类型
export interface DeleteResponse {
  success: boolean
  message: string
}
