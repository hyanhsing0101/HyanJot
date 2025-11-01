<template>
  <div class="todo-details">
    <!-- 描述 -->
    <div v-if="details?.content" class="todo-description">
      <div class="description-label">描述：</div>
      <div class="description-content">{{ details.content }}</div>
    </div>

    <div class="todo-meta-row">
      <span class="todo-meta-item">
        <el-tag :type="getPriorityType(details?.priority || 'medium')" size="small">
          {{ getPriorityText(details?.priority || 'medium') }}
        </el-tag>
      </span>
      <span class="todo-meta-item">
        截止：{{ formatDate(details?.deadline || '') }}
      </span>
    </div>

    <!-- 进度模式 -->
    <div
      v-if="details?.progressMode === true && details?.progressTotal"
      class="todo-progress"
    >
      <div class="progress-info">
        <span>进度：{{ details?.progressCurrent || 0 }} / </span>
        <span v-if="editingProgressTotalId !== todoId" class="progress-total-display">
          {{ details?.progressTotal }}
          <el-button
            type="primary"
            text
            size="small"
            @click="$emit('start-edit-progress')"
            style="margin-left: 8px"
          >
            编辑
          </el-button>
        </span>
        <span v-else style="display: flex; align-items: center; gap: 4px">
          <el-input-number
            v-model="localProgressTotal"
            :min="1"
            :max="9999"
            size="small"
            style="width: 100px"
            @keyup.enter="$emit('save-progress', localProgressTotal)"
          />
          <el-button type="primary" text size="small" @click="$emit('save-progress', localProgressTotal)">
            保存
          </el-button>
          <el-button text size="small" @click="$emit('cancel-edit-progress')">取消</el-button>
        </span>
        <div class="progress-buttons">
          <el-button
            size="small"
            @click="$emit('decrement-progress')"
            :disabled="(details?.progressCurrent || 0) <= 0"
            >-</el-button
          >
          <el-button
            size="small"
            @click="$emit('increment-progress')"
            :disabled="(details?.progressCurrent || 0) >= (details?.progressTotal || 0)"
            >+</el-button
          >
        </div>
      </div>
      <el-progress
        :percentage="
          Math.round(
            ((details?.progressCurrent || 0) / (details?.progressTotal || 1)) * 100,
          )
        "
        :stroke-width="8"
      />
    </div>

    <!-- 普通模式：只显示基本信息（优先级和截止日期），不显示额外提示 -->

    <!-- 子任务模式 -->
    <div
      v-else-if="details?.progressMode === false"
      class="todo-subtasks"
    >
      <div class="subtasks-header">子任务：</div>
      <template
        v-if="details?.subtasks && (details?.subtasks?.length || 0) > 0"
      >
        <div
          v-for="(subtask, index) in details?.subtasks || []"
          :key="index"
          class="subtask-item"
          style="
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 8px;
          "
        >
          <el-checkbox
            :model-value="subtask.completed"
            @change="$emit('toggle-subtask', index)"
          >
            <span :class="{ 'subtask-completed': subtask.completed }">{{
              subtask.text
            }}</span>
          </el-checkbox>
          <el-button type="danger" text size="small" @click="$emit('delete-subtask', index)">
            删除
          </el-button>
        </div>
      </template>
      <div v-else style="color: #8c8c8c; font-size: 12px; margin-bottom: 8px">
        暂无子任务
      </div>
      <el-button
        type="primary"
        text
        size="small"
        @click="$emit('show-add-subtask-dialog')"
        style="margin-top: 8px; width: 100%"
      >
        + 添加子任务
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { TodoItem } from '@/types/item'

interface Props {
  todoId: number
  details?: TodoItem
  editingProgressTotalId?: number | null
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'increment-progress': []
  'decrement-progress': []
  'toggle-subtask': [index: number]
  'delete-subtask': [index: number]
  'show-add-subtask-dialog': []
  'start-edit-progress': []
  'save-progress': [total: number]
  'cancel-edit-progress': []
}>()

const localProgressTotal = ref(props.details?.progressTotal || 0)

watch(
  () => props.details?.progressTotal,
  (newVal) => {
    if (newVal) localProgressTotal.value = newVal
  }
)

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

const getPriorityType = (priority: string) => {
  switch (priority) {
    case 'high':
      return 'danger'
    case 'medium':
      return 'warning'
    case 'low':
      return 'info'
    default:
      return 'info'
  }
}

const getPriorityText = (priority: string) => {
  switch (priority) {
    case 'high':
      return '高优先级'
    case 'medium':
      return '中优先级'
    case 'low':
      return '低优先级'
    default:
      return priority
  }
}
</script>

<style scoped>
.todo-details {
  margin-top: 16px;
  padding-top: 16px;
  padding-bottom: 8px;
  border-top: 2px solid #e8e8e8;
  background-color: #fafafa;
  border-radius: 4px;
  padding-left: 12px;
  padding-right: 12px;
}

.todo-description {
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e8e8e8;
}

.description-label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 6px;
}

.description-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.todo-meta-row {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  align-items: center;
}

.todo-meta-item {
  font-size: 14px;
  color: #666;
}

.todo-progress {
  margin-top: 12px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
  flex-wrap: wrap;
  gap: 8px;
}

.progress-buttons {
  display: flex;
  gap: 8px;
}

.progress-total-display {
  display: inline-flex;
  align-items: center;
}

.todo-subtasks {
  margin-top: 12px;
}

.subtasks-header {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 8px;
  color: #333;
}

.subtask-item {
  margin-bottom: 8px;
  font-size: 14px;
}

.subtask-completed {
  text-decoration: line-through;
  color: #8c8c8c;
}
</style>
