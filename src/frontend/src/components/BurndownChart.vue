<template>
  <div class="burndown-chart">
    <div class="chart-header">
      <h3>燃尽图</h3>
      <el-select v-model="currentSprintId" placeholder="选择Sprint" size="small" @change="fetchData" style="width:200px">
        <el-option v-for="s in sprints" :key="s.id" :label="s.name" :value="s.id" />
      </el-select>
    </div>
    
    <div v-if="loading" style="text-align:center;padding:40px">
      <el-icon class="is-loading" style="font-size:24px"><Loading /></el-icon>
    </div>
    
    <div v-else-if="chartData" class="chart-container">
      <div class="stats-row">
        <div class="stat-item">
          <span class="label">总故事点</span>
          <span class="value">{{ chartData.totalPoints }}</span>
        </div>
        <div class="stat-item">
          <span class="label">已完成</span>
          <span class="value success">{{ chartData.completedPoints }}</span>
        </div>
        <div class="stat-item">
          <span class="label">剩余</span>
          <span class="value warning">{{ chartData.remainingPoints }}</span>
        </div>
        <div class="stat-item">
          <span class="label">完成率</span>
          <span class="value">{{ completionRate }}%</span>
        </div>
      </div>
      
      <div ref="chartRef" class="echarts-container"></div>
    </div>
    
    <el-empty v-else description="暂无数据" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getSprintList, getBurndownData } from '@/api/sprint'

const props = defineProps<{
  projectId?: number
}>()

const sprints = ref<any[]>([])
const currentSprintId = ref<number | undefined>()
const chartData = ref<any>(null)
const loading = ref(false)
const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

const completionRate = computed(() => {
  if (!chartData.value || !chartData.value.totalPoints) return 0
  return Math.round((chartData.value.completedPoints / chartData.value.totalPoints) * 100)
})

async function fetchSprints() {
  if (!props.projectId) return
  try {
    const res = await getSprintList(props.projectId)
    if (res.code === 200) {
      sprints.value = res.data || []
      // 默认选中进行中的Sprint
      const active = sprints.value.find((s: any) => s.status === 2)
      currentSprintId.value = active?.id || sprints.value[0]?.id
    }
  } catch (e) {
    console.error('获取Sprint失败', e)
  }
}

async function fetchData() {
  if (!currentSprintId.value) return
  loading.value = true
  try {
    const res = await getBurndownData(currentSprintId.value)
    if (res.code === 200) {
      chartData.value = res.data
      await nextTick()
      renderChart()
    }
  } catch (e) {
    console.error('获取燃尽图数据失败', e)
  } finally {
    loading.value = false
  }
}

function renderChart() {
  if (!chartRef.value || !chartData.value) return
  
  if (chartInstance) {
    chartInstance.dispose()
  }
  
  chartInstance = echarts.init(chartRef.value)
  
  // 生成实际燃尽数据
  const actualData: [number, number][] = []
  const dailyBurndown = chartData.value.dailyBurndown || {}
  let dayIndex = 0
  
  Object.entries(dailyBurndown).forEach(([date, remaining]: [string, any]) => {
    actualData.push([dayIndex, Number(remaining) || 0])
    dayIndex++
  })
  
  // 理想燃尽线
  const idealData: [number, number][] = []
  const idealLine = chartData.value.idealLine || []
  idealLine.forEach((point: any) => {
    idealData.push([point.day, Number(point.remaining) || 0])
  })
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['实际剩余', '理想剩余']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      name: '天数',
      boundaryGap: false
    },
    yAxis: {
      type: 'value',
      name: '故事点',
      min: 0
    },
    series: [
      {
        name: '实际剩余',
        type: 'line',
        data: actualData,
        smooth: true,
        itemStyle: { color: '#409eff' },
        areaStyle: { color: 'rgba(64, 158, 255, 0.1)' }
      },
      {
        name: '理想剩余',
        type: 'line',
        data: idealData,
        smooth: true,
        lineStyle: { type: 'dashed', color: '#67c23a' },
        itemStyle: { color: '#67c23a' }
      }
    ]
  }
  
  chartInstance.setOption(option)
}

function handleResize() {
  chartInstance?.resize()
}

watch(() => props.projectId, () => {
  fetchSprints()
}, { immediate: true })

watch(currentSprintId, () => {
  if (currentSprintId.value) fetchData()
})

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped>
.burndown-chart {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.chart-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.chart-container {
  
}

.stats-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-item .label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.stat-item .value {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.stat-item .value.success {
  color: #67c23a;
}

.stat-item .value.warning {
  color: #e6a23c;
}

.echarts-container {
  width: 100%;
  height: 300px;
}
</style>
