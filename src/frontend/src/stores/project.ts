import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getProjectList } from '@/api/project'

export const useProjectStore = defineStore('project', () => {
  const projects = ref<any[]>([])
  const selectedProjectId = ref<number | null>(
    (() => {
      const stored = localStorage.getItem('selectedProjectId')
      return stored ? Number(stored) : null
    })()
  )
  const loading = ref(false)

  const selectedProject = computed(() =>
    projects.value.find(p => p.id === selectedProjectId.value) || null
  )

  const hasProject = computed(() => !!selectedProjectId.value && !!selectedProject.value)

  const projectName = computed(() => selectedProject.value?.name || '')

  async function fetchProjects() {
    loading.value = true
    try {
      const res = await getProjectList({ page: 0, size: 500 })
      if (res.code === 200) {
        projects.value = res.data.content || []
        // 验证已选中的项目是否仍存在
        if (selectedProjectId.value && !projects.value.find(p => p.id === selectedProjectId.value)) {
          clearSelection()
        }
      }
    } catch (e) {
      console.error('获取项目列表失败', e)
    } finally {
      loading.value = false
    }
  }

  function selectProject(id: number) {
    selectedProjectId.value = id
    localStorage.setItem('selectedProjectId', String(id))
  }

  function clearSelection() {
    selectedProjectId.value = null
    localStorage.removeItem('selectedProjectId')
  }

  return {
    projects,
    selectedProjectId,
    loading,
    selectedProject,
    hasProject,
    projectName,
    fetchProjects,
    selectProject,
    clearSelection
  }
})
