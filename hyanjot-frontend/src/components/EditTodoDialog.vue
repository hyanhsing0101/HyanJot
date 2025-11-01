<template>
  <el-dialog v-model="visible" title="编辑TODO" width="500px" @close="$emit('close')">
    <el-form :model="formData" label-width="100px">
      <el-form-item label="标题" required>
        <el-input v-model="formData.title" placeholder="请输入标题" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input
          v-model="formData.content"
          type="textarea"
          :rows="3"
          placeholder="请输入描述（可选）"
        />
      </el-form-item>
      <el-form-item label="优先级" required>
        <el-select v-model="formData.priority" placeholder="请选择优先级" style="width: 100%">
          <el-option label="低" value="low" />
          <el-option label="中" value="medium" />
          <el-option label="高" value="high" />
        </el-select>
      </el-form-item>
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
    </el-form>
    <template #footer>
      <el-button @click="$emit('close')">取消</el-button>
      <el-button type="primary" @click="$emit('submit', formData)">更新</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'

interface Props {
  modelValue: boolean
  initialData?: { title: string; content?: string; priority: string; deadline: string }
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'close'): void
  (e: 'submit', data: { title: string; content?: string; priority: string; deadline: string }): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const formData = ref({
  title: '',
  content: '',
  priority: 'medium',
  deadline: '',
})

watch(
  () => props.initialData,
  (data) => {
    if (data) {
      formData.value = { ...data }
    }
  },
  { immediate: true }
)

watch(visible, (val) => {
  if (!val) {
    formData.value = { title: '', content: '', priority: 'medium', deadline: '' }
  }
})
</script>
