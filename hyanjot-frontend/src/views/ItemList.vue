<template>
  <div class="item-list-container">
    <!-- 顶部标题栏 -->
    <div class="header">
      <h1 class="title">HyanJot</h1>
      <el-button type="primary" @click="showAddDialog = true" circle>
        <span style="font-size: 20px">+</span>
      </el-button>
    </div>

    <!-- 类型筛选 -->
    <div class="filter-tabs">
      <div
        class="tab-item"
        :class="{ active: currentFilter === 'ALL' }"
        @click="currentFilter = 'ALL'"
      >
        全部
      </div>
      <div
        class="tab-item"
        :class="{ active: currentFilter === 'TODO' }"
        @click="currentFilter = 'TODO'"
      >
        TODO
      </div>
      <div
        class="tab-item"
        :class="{ active: currentFilter === 'HABIT' }"
        @click="currentFilter = 'HABIT'"
      >
        习惯
      </div>
      <div
        class="tab-item"
        :class="{ active: currentFilter === 'REMINDER' }"
        @click="currentFilter = 'REMINDER'"
      >
        提醒
      </div>
    </div>

    <!-- 备忘项列表 -->
    <draggable v-model="items" item-key="id" class="item-list" @end="handleDragEnd">
      <template #item="{ element: item }">
        <TodoItemCard
          v-if="shouldShowItem(item) && item.type === 'TODO' && item.id"
          :item="item"
          :details="item.id ? todoOps.itemDetails.value[item.id] : undefined"
          :expanded="item.id ? todoOps.expandedTodos.value[item.id] || false : false"
          :editing-progress-total-id="editingProgressTotalId"
          @toggle-status="item.id && toggleItem(item.id)"
          @toggle-details="item.id && todoOps.toggleDetails(item.id)"
          @edit="item.id && handleEdit(item.id)"
          @delete="item.id && handleDelete(item.id)"
          @increment-progress="item.id && todoOps.incrementProgressValue(item.id)"
          @decrement-progress="item.id && todoOps.decrementProgressValue(item.id)"
          @toggle-subtask="(index) => item.id && todoOps.toggleSubtaskStatus(item.id, index)"
          @delete-subtask="(index) => item.id && todoOps.deleteSubtaskItem(item.id, index)"
          @show-add-subtask-dialog="item.id && handleShowAddSubtaskDialog(item.id)"
          @start-edit-progress="item.id && handleStartEditProgressTotal(item.id)"
          @save-progress="(total) => item.id && handleSaveProgressTotal(item.id, total)"
          @cancel-edit-progress="handleCancelEditProgressTotal"
        />

        <!-- 其他类型的卡片（HABIT、REMINDER） -->
        <div
          v-else-if="shouldShowItem(item)"
          class="item-card"
          :class="getPriorityClassForItem(item)"
        >
          <div class="item-checkbox">
            <el-checkbox
              :model-value="item.status === 'completed'"
              @change="item.id && toggleItem(item.id)"
            />
          </div>
          <div class="item-content" style="flex: 1">
            <div class="item-title" :class="{ completed: item.status === 'completed' }">
              {{ item.title }}
            </div>
            <div class="item-meta">{{ item.type }} · {{ formatDate(item.createdAt) }}</div>
          </div>
          <div class="item-actions">
            <el-button type="danger" text @click="handleDelete(item.id!)"> 删除 </el-button>
          </div>
        </div>
      </template>
    </draggable>

    <div v-if="items.length === 0" class="empty-state">暂无备忘项</div>

    <!-- 添加对话框 -->
    <AddItemDialog
      v-model="showAddDialog"
      @close="handleAddDialogClose"
      @submit="handleAddSubmit"
    />

    <!-- 编辑对话框 -->
    <EditTodoDialog
      v-model="showEditDialog"
      :initial-data="editItemData"
      @close="handleEditDialogClose"
      @submit="handleEditSubmit"
    />

    <!-- 添加子任务对话框 -->
    <el-dialog v-model="showAddSubtaskDialog" title="添加子任务" width="400px">
      <el-input
        v-model="newSubtaskText"
        placeholder="请输入子任务内容"
        @keyup.enter="handleConfirmAddSubtask"
        clearable
      />
      <template #footer>
        <el-button @click="handleCloseAddSubtaskDialog">取消</el-button>
        <el-button type="primary" @click="handleConfirmAddSubtask">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getUserItems,
  createItem,
  createTodo,
  toggleItemStatus,
  deleteItem,
  updateItem,
} from '@/api/itemService'
import type { Item, TodoItem } from '@/types/item'
import draggable from 'vuedraggable'
import TodoItemCard from '@/components/TodoItemCard.vue'
import AddItemDialog from '@/components/AddItemDialog.vue'
import EditTodoDialog from '@/components/EditTodoDialog.vue'
import { useTodoOperations } from '@/composables/useTodoOperations'

// 使用composable
const todoOps = useTodoOperations()

// 设置进度更新回调，用于在进度变化后刷新Item列表
todoOps.setProgressUpdateCallback(() => {
  loadItems()
})

// 数据状态
const items = ref<Item[]>([])
const currentFilter = ref<'ALL' | 'TODO' | 'HABIT' | 'REMINDER'>('ALL')
const showAddDialog = ref(false)
const showEditDialog = ref(false)
const editingTodoId = ref<number | null>(null)
const showAddSubtaskDialog = ref(false)
const newSubtaskText = ref('')
const addingSubtaskTodoId = ref<number | null>(null)
const editingProgressTotalId = ref<number | null>(null)
const editingProgressTotalValue = ref<number>(0)
const editItemData = ref<
  { title: string; content?: string; priority: string; deadline: string } | undefined
>(undefined)

// 加载数据
const loadItems = async () => {
  try {
    const data = await getUserItems()
    items.value = data.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
    // 自动加载所有TODO的详情
    for (const item of items.value) {
      if (item.type === 'TODO' && item.id && !todoOps.itemDetails.value[item.id]) {
        todoOps.loadItemDetails(item.id).catch(() => {})
      }
    }
  } catch {
    ElMessage.error('加载失败')
  }
}

// 筛选逻辑
const shouldShowItem = (item: Item) => {
  if (currentFilter.value === 'ALL') return true
  return item.type === currentFilter.value
}

// 格式化日期
const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

// 根据优先级获取CSS类名
const getPriorityClassForItem = (item: Item) => {
  // TODO类型：从details或item中获取priority
  if (item.type === 'TODO') {
    const todoItem = item as TodoItem
    const details = todoItem.id ? todoOps.itemDetails.value[todoItem.id] : undefined
    const priority = details?.priority || todoItem.priority || 'medium'
    return getPriorityClass(priority)
  }
  // HABIT和REMINDER类型：默认使用中等优先级
  return 'priority-medium'
}

const getPriorityClass = (priority: string) => {
  switch (priority) {
    case 'high':
      return 'priority-high'
    case 'medium':
      return 'priority-medium'
    case 'low':
      return 'priority-low'
    default:
      return 'priority-medium'
  }
}

// 添加备忘项
const handleAddSubmit = async (formData: Partial<TodoItem>) => {
  try {
    if (formData.type === 'TODO') {
      await createTodo(formData as TodoItem)
      ElMessage.success('添加成功')
    } else {
      await createItem(formData as Item)
      ElMessage.success('添加成功')
    }
    handleAddDialogClose()
    loadItems()
  } catch {
    ElMessage.error('添加失败')
  }
}

const handleAddDialogClose = () => {
  showAddDialog.value = false
}

// 编辑TODO
const handleEdit = async (id: number) => {
  editingTodoId.value = id
  if (!todoOps.itemDetails.value[id]) {
    await todoOps.loadItemDetails(id)
  }
  const todo = todoOps.itemDetails.value[id]
  if (todo) {
    editItemData.value = {
      title: todo.title,
      content: todo.content,
      priority: todo.priority || 'medium',
      deadline: todo.deadline,
    }
  }
  showEditDialog.value = true
}

const handleEditSubmit = async (data: {
  title: string
  content?: string
  priority: string
  deadline: string
}) => {
  if (!editingTodoId.value) return
  await todoOps.updateTodoItem(editingTodoId.value, data)
  ElMessage.success('更新成功')
  handleEditDialogClose()
  loadItems()
}

const handleEditDialogClose = () => {
  showEditDialog.value = false
  editingTodoId.value = null
  editItemData.value = undefined
}

// 切换完成状态
const toggleItem = async (id: number) => {
  try {
    await toggleItemStatus(id)
    loadItems()
  } catch {
    ElMessage.error('操作失败')
  }
}

// 删除备忘项
const handleDelete = async (id: number) => {
  try {
    await deleteItem(id)
    ElMessage.success('删除成功')
    loadItems()
  } catch {
    ElMessage.error('删除失败')
  }
}

// 拖拽结束
const handleDragEnd = async () => {
  try {
    const updatePromises = items.value.map((item, index) =>
      updateItem(item.id!, { ...item, sortOrder: index + 1 } as Item),
    )
    await Promise.all(updatePromises)
    ElMessage.success('顺序已更新')
  } catch {
    ElMessage.error('更新顺序失败')
    loadItems()
  }
}

// 添加子任务
const handleShowAddSubtaskDialog = (id: number) => {
  addingSubtaskTodoId.value = id
  newSubtaskText.value = ''
  showAddSubtaskDialog.value = true
}

const handleConfirmAddSubtask = async () => {
  if (!newSubtaskText.value.trim()) {
    ElMessage.warning('请输入子任务内容')
    return
  }
  if (!addingSubtaskTodoId.value) return

  try {
    await todoOps.addSubtaskItem(addingSubtaskTodoId.value, newSubtaskText.value.trim())
    showAddSubtaskDialog.value = false
    newSubtaskText.value = ''
    addingSubtaskTodoId.value = null
    ElMessage.success('添加成功')
    loadItems()
  } catch (error: unknown) {
    interface AxiosErrorResponse {
      response?: {
        data?: {
          message?: string
        }
      }
      message?: string
    }
    const err = error as AxiosErrorResponse
    const errorMsg = err?.response?.data?.message || err?.message || '添加失败'
    ElMessage.error(errorMsg)
  }
}

const handleCloseAddSubtaskDialog = () => {
  showAddSubtaskDialog.value = false
  newSubtaskText.value = ''
  addingSubtaskTodoId.value = null
}

// 编辑进度总数
const handleStartEditProgressTotal = (id: number) => {
  const todo = todoOps.itemDetails.value[id]
  if (todo && todo.progressTotal) {
    editingProgressTotalId.value = id
    editingProgressTotalValue.value = todo.progressTotal
  }
}

const handleSaveProgressTotal = async (id: number, total: number) => {
  await todoOps.saveProgressTotal(id, total)
  // 保存成功后退出编辑模式
  editingProgressTotalId.value = null
  editingProgressTotalValue.value = 0
}

const handleCancelEditProgressTotal = () => {
  editingProgressTotalId.value = null
  editingProgressTotalValue.value = 0
}

// 初始化
onMounted(() => {
  loadItems()
})
</script>

<style scoped>
.item-list-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 30px 16px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
}

.title {
  font-size: 32px;
  font-weight: 500;
  color: #1a1a1a;
  margin: 0;
}

.filter-tabs {
  display: flex;
  gap: 32px;
  margin-bottom: 40px;
  border-bottom: 1px solid #e8e8e8;
  width: 100%;
}

.tab-item {
  padding: 12px 0;
  font-size: 16px;
  color: #8c8c8c;
  cursor: pointer;
  transition: color 0.3s;
  position: relative;
}

.tab-item:hover {
  color: #1a1a1a;
}

.tab-item.active {
  color: #1a1a1a;
  font-weight: 500;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 2px;
  background: #1a1a1a;
}

.item-list {
  min-height: 200px;
}

.item-card {
  display: flex;
  align-items: flex-start;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
  transition: background 0.3s;
  margin-bottom: 16px;
}

.item-card:hover {
  background: #f5f5f5;
}

.item-checkbox {
  margin-right: 16px;
  margin-top: 4px;
}

.item-content {
  flex: 1;
}

.item-title {
  font-size: 16px;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.item-title.completed {
  color: #8c8c8c;
  text-decoration: line-through;
}

.item-meta {
  font-size: 14px;
  color: #8c8c8c;
}

.item-actions {
  display: flex;
  gap: 8px;
  margin-left: 16px;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
  color: #8c8c8c;
  font-size: 16px;
}

/* 优先级颜色 */
.item-card.priority-high {
  border-left: 4px solid #f56c6c; /* 红色 - 高优先级 */
}

.item-card.priority-medium {
  border-left: 4px solid #e6a23c; /* 橙色 - 中优先级 */
}

.item-card.priority-low {
  border-left: 4px solid #409eff; /* 蓝色 - 低优先级 */
}
</style>
