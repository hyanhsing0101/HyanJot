<template>
  <div
    class="item-card"
    :class="getPriorityClass(details?.priority || item.priority || 'medium')"
  >
    <div class="item-checkbox">
      <el-checkbox
        :model-value="item.status === 'completed'"
        @change="$emit('toggle-status')"
      />
    </div>
    <div class="item-content" style="flex: 1">
      <div class="item-title" :class="{ completed: item.status === 'completed' }">
        {{ item.title }}
      </div>
      <div class="item-meta">
        {{ item.type }} · {{ formatDate(item.createdAt) }}
        <!-- TODO类型标识 -->
        <template v-if="details">
          <span style="margin-left: 8px; color: #409eff">
            <el-tag v-if="details.progressMode === true" type="success" size="small">
              进度模式
            </el-tag>
            <el-tag
              v-else-if="details.progressMode === false"
              type="warning"
              size="small"
            >
              子任务模式
            </el-tag>
            <el-tag v-else-if="details.progressMode === null || details.progressMode === undefined" type="info" size="small"> 普通模式 </el-tag>
          </span>
          <!-- 进度信息 -->
          <span
            v-if="details.progressMode === true && details.progressTotal"
            style="margin-left: 8px; color: #8c8c8c; font-size: 12px"
          >
            ({{ details.progressCurrent || 0 }}/{{ details.progressTotal }})
          </span>
          <!-- 子任务完成情况：只在子任务模式且确实有子任务时显示 -->
          <span
            v-if="
              details.progressMode === false &&
              details.subtasks &&
              Array.isArray(details.subtasks) &&
              details.subtasks.length > 0
            "
            style="margin-left: 8px; color: #8c8c8c; font-size: 12px"
          >
            ({{
              details.subtasks.filter((s) => s.completed).length
            }}/{{ details.subtasks.length }})
          </span>
        </template>
      </div>

      <!-- TODO详情展示 -->
      <TodoDetails
        v-if="details && expanded"
        :todo-id="item.id!"
        :details="details"
        :editing-progress-total-id="editingProgressTotalId"
        @increment-progress="$emit('increment-progress')"
        @decrement-progress="$emit('decrement-progress')"
        @toggle-subtask="(index) => $emit('toggle-subtask', index)"
        @delete-subtask="(index) => $emit('delete-subtask', index)"
        @show-add-subtask-dialog="$emit('show-add-subtask-dialog')"
        @start-edit-progress="$emit('start-edit-progress')"
        @save-progress="(total) => $emit('save-progress', total)"
        @cancel-edit-progress="$emit('cancel-edit-progress')"
      />
    </div>
    <div class="item-actions">
      <el-button type="primary" text @click="$emit('toggle-details')">
        {{ expanded ? '收起' : '详情' }}
      </el-button>
      <el-button type="primary" text @click="$emit('edit')"> 编辑 </el-button>
      <el-button type="danger" text @click="$emit('delete')"> 删除 </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import TodoDetails from './TodoDetails.vue'
import type { Item, TodoItem } from '@/types/item'

interface Props {
  item: Item
  details?: TodoItem
  expanded: boolean
  editingProgressTotalId?: number | null
}

defineProps<Props>()

defineEmits<{
  'toggle-status': []
  'toggle-details': []
  edit: []
  delete: []
  'increment-progress': []
  'decrement-progress': []
  'toggle-subtask': [index: number]
  'delete-subtask': [index: number]
  'show-add-subtask-dialog': []
  'start-edit-progress': []
  'save-progress': [total: number]
  'cancel-edit-progress': []
}>()

const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
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
</script>

<style scoped>
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
