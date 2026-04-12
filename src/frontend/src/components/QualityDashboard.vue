<template>
  <div class="quality-dashboard">
    <div class="dashboard-header">
      <h3>质量仪表盘</h3>
      <el-select v-model="selectedProject" placeholder="选择项目" @change="fetchStats" style="width:200px">
        <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
      </el-select>
    </div>

    <el-row :gutter="20" class="stats-row" v-loading="loading">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon blue"><Document /></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalCases || 0 }}</div>
            <div class="stat-label">用例总数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon green"><CircleCheck /></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.passedCases || 0 }}</div>
            <div class="stat-label">通过用例</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon orange"><Warning /></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.openBugs || 0 }}</div>
            <div class="stat-label">待处理Bug</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon purple"><DataLine /></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.bugCloseRate || 0 }}%</div>
            <div class="stat-label">Bug 关闭率</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card><template #header><span>Bug 严重等级分布</span></template>
          <div ref="severityChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card><template #header><span>质量趋势</span></template>
          <div class="quality-trend">
            <div class="trend-item">
              <span class="label">用例覆盖率</span>
              <el-progress :percentage="casePassRate" :color="progressColor" />
            </div>
            <div class="trend-item">
              <span class="label">Bug 修复率</span>
              <el-progress :percentage="stats.bugCloseRate || 0" :color="progressColor2" />
            </div>
            <div class="trend-item">
              <span class="label">致命Bug数</span>
              <el-progress :percentage="fatalRate" :color="'#ff4d4f'" :show-text="false" />
              <span class="value">{{ severityStats['致命'] || 0 }} 个</span>
            </div>
            <div class="trend-item">
              <span class="label">严重Bug数</span>
              <el-progress :percentage="criticalRate" :color="'#faad14'" :show-text="false" />
              <span class="value">{{ severityStats['严重'] || 0 }} 个</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getQualityStats } from '@/api/qa'
import { getProjectList } from '@/api/project'

const projects = ref<any[]>([])
const selectedProject = ref<number | undefined>()
const stats = ref<any>({})
const loading = ref(false)
const severityChartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

const progressColor = [{ color: '#67c23a', percentage: 60 }, { color: '#e6a23c', percentage: 80 }, { color: '#f56c6c', percentage: 100 }]
const progressColor2 = [{ color: '#67c23a', percentage: 70 }, { color: '#409eff', percentage: 100 }]

const severityStats = computed(() => stats.value.severityStats || {})
const casePassRate = computed(() => stats.value.casePassRate || 0)
const fatalRate = computed(() => {
  const total = Object.values(severityStats.value).reduce((a: any, b: any) => a + b, 0)
  return total > 0 ? Math.round(((severityStats.value['致命'] || 0) / total) * 100) : 0
})
const criticalRate = computed(() => {
  const total = Object.values(severityStats.value).reduce((a: any, b: any) => a + b, 0)
  return total > 0 ? Math.round(((severityStats.value['严重'] || 0) / total) * 100) : 0
})

async function fetchProjects() {
  try {
    const res = await getProjectList({ page: 0, size: 100 })
    if (res.code === 200) {
      projects.value = res.data.content || []
      if (projects.value.length) selectedProject.value = projects.value[0].id
    }
  } catch (e) { console.error(e) }
}

async function fetchStats() {
  if (!selectedProject.value) return
  loading.value = true
  try {
    const res = await getQualityStats(selectedProject.value)
    if (res.code === 200) {
      stats.value = res.data
      await nextTick()
      renderChart()
    }
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function renderChart() {
  if (!severityChartRef.value) return
  if (chartInstance) chartInstance.dispose()
  chartInstance = echarts.init(severityChartRef.value)
  
  const data = [
    { value: severityStats.value['致命'] || 0, name: '致命' },
    { value: severityStats.value['严重'] || 0, name: '严重' },
    { value: severityStats.value['一般'] || 0, name: '一般' },
    { value: severityStats.value['轻微'] || 0, name: '轻微' }
  ]
  
  chartInstance.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}: {c}' },
      data,
      color: ['#ff4d4f', '#faad14', '#409eff', '#67c23a']
    }]
  })
}

function handleResize() { chartInstance?.resize() }

watch(selectedProject, () => fetchStats())

onMounted(() => {
  fetchProjects().then(() => fetchStats())
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped>
.quality-dashboard { padding: 16px; }
.dashboard-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.dashboard-header h3 { margin: 0; font-size: 16px; color: #333; }
.stats-row { margin-bottom: 20px; }
.stat-card { display: flex; align-items: center; padding: 20px; background: #fff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.stat-icon { width: 50px; height: 50px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 24px; color: #fff; margin-right: 16px; }
.stat-icon.blue { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.stat-icon.green { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
.stat-icon.orange { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.stat-icon.purple { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.stat-value { font-size: 28px; font-weight: bold; color: #333; }
.stat-label { font-size: 14px; color: #999; }
.charts-row { }
.chart-container { width: 100%; height: 250px; }
.quality-trend { display: flex; flex-direction: column; gap: 20px; }
.trend-item { display: flex; align-items: center; gap: 12px; }
.trend-item .label { width: 80px; font-size: 14px; color: #666; }
.trend-item .el-progress { flex: 1; }
.trend-item .value { width: 60px; text-align: right; font-size: 14px; color: #333; }
</style>
