<template>
  <el-dialog v-model="visible" title="添加备忘项" width="600px" @close="$emit('close')">
    <el-form :model="formData" label-width="100px">
      <el-form-item label="标题" required>
        <el-input v-model="formData.title" placeholder="请输入标题" />
      </el-form-item>
      <el-form-item label="类型" required>
        <el-select v-model="formData.type" placeholder="请选择类型">
          <el-option label="TODO" value="TODO" />
          <el-option label="习惯" value="HABIT" />
          <el-option label="提醒" value="REMINDER" />
        </el-select>
      </el-form-item>
      <el-form-item label="内容">
        <el-input
          v-model="formData.content"
          type="textarea"
          :rows="3"
          placeholder="请输入内容详情（可选）"
        />
      </el-form-item>

      <!-- TODO专用字段 -->
      <template v-if="formData.type === 'TODO'">
        <el-form-item label="截止日期" required>
          <el-date-picker
            v-model="formData.deadline"
            type="date"
            placeholder="选择截止日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="优先级" required>
          <el-select v-model="formData.priority" placeholder="请选择优先级">
            <el-option label="低" value="low" />
            <el-option label="中" value="medium" />
            <el-option label="高" value="high" />
          </el-select>
        </el-form-item>
        <el-form-item label="模式">
          <el-radio-group v-model="formData.progressMode">
            <el-radio :label="null">普通模式</el-radio>
            <el-radio :label="true">进度模式</el-radio>
            <el-radio :label="false">子任务模式</el-radio>
          </el-radio-group>
          <div style="font-size: 12px; color: #8c8c8c; margin-top: 4px">
            普通模式：简单的待办事项，无需进度或子任务
          </div>
        </el-form-item>

        <!-- 进度模式 -->
        <el-form-item v-if="formData.progressMode === true" label="目标总数" required>
          <el-input-number
            v-model="formData.progressTotal"
            :min="1"
            :max="9999"
            placeholder="输入目标数量"
            style="width: 100%"
          />
        </el-form-item>

        <!-- 子任务模式 -->
        <el-form-item v-if="formData.progressMode === false" label="子任务">
          <div style="width: 100%">
            <div
              v-for="(subtask, index) in (formData.subtasks || [])"
              :key="index"
              style="display: flex; gap: 8px; margin-bottom: 8px; align-items: center"
            >
              <el-input v-model="subtask.text" placeholder="输入子任务内容" style="flex: 1" />
              <el-button
                type="danger"
                text
                @click="removeSubtask(index)"
                :disabled="(formData.subtasks?.length || 0) <= 1"
              >
                删除
              </el-button>
            </div>
            <el-button type="primary" text @click="addSubtask" style="width: 100%">
              + 添加子任务
            </el-button>
          </div>
        </el-form-item>
      </template>

      <!-- HABIT和REMINDER字段（后续添加） -->
    </el-form>
    <template #footer>
      <el-button @click="$emit('close')">取消</el-button>
      <el-button type="primary" @click="$emit('submit', formData)">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import type { TodoItem, Subtask } from '@/types/item'

interface Props {
  modelValue: boolean
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'close'): void
  (e: 'submit', data: Partial<TodoItem>): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const formData = ref<Partial<TodoItem>>({
  title: '',
  type: 'TODO',
  content: '',
  userId: 1,
  deadline: '',
  priority: 'medium',
  progressMode: null,
  progressTotal: undefined,
  progressCurrent: 0,
  subtasks: [],
})

const addSubtask = () => {
  if (!formData.value.subtasks) {
    formData.value.subtasks = []
  }
  formData.value.subtasks.push({ text: '', completed: false })
}

const removeSubtask = (index: number) => {
  if (formData.value.subtasks && formData.value.subtasks.length > 1) {
    formData.value.subtasks.splice(index, 1)
  }
}

watch(visible, (val) => {
  if (!val) {
    // 重置表单
    formData.value = {
      title: '',
      type: 'TODO',
      content: '',
      userId: 1,
      deadline: '',
      priority: 'medium',
      progressMode: null,
      progressTotal: undefined,
      progressCurrent: 0,
      subtasks: [],
    }
  }
})

watch(
  () => formData.value.type,
  (newType) => {
    if (newType === 'TODO') {
      formData.value = {
        ...formData.value,
        type: 'TODO',
        deadline: '',
        priority: 'medium',
        progressMode: null,
        progressTotal: undefined,
        progressCurrent: 0,
        subtasks: [],
      }
    } else {
      formData.value = {
        title: formData.value.title,
        type: newType,
        content: formData.value.content,
        userId: 1,
      }
    }
  }
)
</script>
