<template>
  <div class="data-screen">
    <div class="header">
      <div class="title">🌿 区块链食品溯源数据驾驶舱</div>
      <div class="time">{{ currentTime }}</div>
      <el-button text style="color: #00f6ff; position: absolute; right: 20px;" @click="$router.push('/dashboard')">返回后台</el-button>
    </div>

    <div class="metrics">
      <div class="metric-card">
        <div class="metric-title">累计生产食品 (件)</div>
        <div class="metric-value">{{ overview.totalFoods }}</div>
      </div>
      <div class="metric-card">
        <div class="metric-title">链上溯源记录 (次)</div>
        <div class="metric-value">{{ overview.totalRecords }}</div>
      </div>
      <div class="metric-card">
        <div class="metric-title">当前在售食品 (件)</div>
        <div class="metric-value">{{ overview.onSaleFoods }}</div>
      </div>
    </div>

    <div class="charts-container">
      <div class="chart-box">
        <div class="chart-title">各角色上链操作次数</div>
        <div id="roleChart" class="chart"></div>
      </div>
      
      <div class="chart-box">
        <div class="chart-title">食品流转状态分布</div>
        <div id="statusChart" class="chart"></div>
      </div>
    </div>

    <div class="latest-box">
      <div class="chart-title">实时上链动态</div>
      <div class="scroll-list">
        <div class="list-item" v-for="(item, index) in latestList" :key="index">
          <span class="time">{{ formatTime(item.tx_time) }}</span>
          <span class="action">{{ item.action }}</span>
          <span class="location">地点: {{ item.location }}</span>
          <span class="operator">操作人: {{ formatAddress(item.operator) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import axios from 'axios'
import * as echarts from 'echarts'

const overview = ref({ totalFoods: 0, totalRecords: 0, onSaleFoods: 0 })
const latestList = ref([])
const currentTime = ref('')
let roleChartInstance = null
let statusChartInstance = null
let timer = null

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString()
}

const fetchData = async () => {
  try {
    const ovRes = await axios.get('/api/statistics/overview')
    if (ovRes.data.code === 200) overview.value = ovRes.data.data

    const ltRes = await axios.get('/api/statistics/latest')
    if (ltRes.data.code === 200) latestList.value = ltRes.data.data

    const roleRes = await axios.get('/api/statistics/role')
    renderRoleChart(roleRes.data.data)

    const statusRes = await axios.get('/api/statistics/status')
    renderStatusChart(statusRes.data.data)
  } catch (error) {
    console.log('获取大屏数据失败', error)
  }
}

const renderRoleChart = (data) => {
  if (!roleChartInstance) {
    roleChartInstance = echarts.init(document.getElementById('roleChart'))
  }
  const roleMap = { 1: '供应商', 2: '中间商', 3: '销售商' }
  const xData = data.map(item => roleMap[item.role_type] || '未知')
  const yData = data.map(item => item.count)

  const option = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '10%', right: '5%', bottom: '15%', top: '15%' },
    xAxis: { type: 'category', data: xData, axisLabel: { color: '#00f6ff' } },
    yAxis: { type: 'value', axisLabel: { color: '#00f6ff' }, splitLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } } },
    series: [{
      type: 'bar',
      data: yData,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#00f6ff' }, { offset: 1, color: '#0077ff' }
        ])
      },
      barWidth: '40%'
    }]
  }
  roleChartInstance.setOption(option)
}

const renderStatusChart = (data) => {
  if (!statusChartInstance) {
    statusChartInstance = echarts.init(document.getElementById('statusChart'))
  }
  const statusMap = { 0: '已生产', 1: '运输中', 2: '已送达', 3: '销售中' }
  const pieData = data.map(item => ({
    name: statusMap[item.status] || '未知',
    value: item.count
  }))

  const option = {
    tooltip: { trigger: 'item' },
    legend: { top: 'bottom', textStyle: { color: '#fff' } },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#0b1526', borderWidth: 2 },
      label: { show: false },
      data: pieData,
      color: ['#909399', '#E6A23C', '#67C23A', '#F56C6C']
    }]
  }
  statusChartInstance.setOption(option)
}

const formatTime = (ts) => ts ? new Date(ts).toLocaleTimeString() : ''
const formatAddress = (addr) => addr ? addr.substring(0, 6) + '...' + addr.substring(addr.length - 4) : ''

onMounted(() => {
  updateTime()
  timer = setInterval(() => {
    updateTime()
    fetchData()
  }, 5000)
  fetchData()

  window.addEventListener('resize', () => {
    roleChartInstance?.resize()
    statusChartInstance?.resize()
  })
})

onUnmounted(() => {
  clearInterval(timer)
})
</script>

<style scoped>
@media screen and (max-width: 768px) {
  .aside { display: none; }
  .header { padding: 0 15px; }
  .logo-text { font-size: 16px; }
  .user-info { gap: 10px; }
  .username { display: none; }
  .main-content { padding: 10px; }
  .search-bar { flex-direction: column; gap: 10px; }
  .search-input { width: 100%; }
  .el-table { overflow-x: auto; display: block; }
  :deep(.el-dialog) { width: 90% !important; margin-top: 5vh !important; }
}

.data-screen {
  width: 100vw;
  height: 100vh;
  background-color: #0b1526;
  background-image: radial-gradient(circle at 50% 50%, #112240 0%, #0b1526 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  padding: 20px;
  box-sizing: border-box;
  overflow: hidden;
}

.header {
  position: relative;
  text-align: center;
  height: 60px;
}
.header .title {
  font-size: 32px;
  font-weight: bold;
  color: #00f6ff;
  text-shadow: 0 0 10px rgba(0, 246, 255, 0.5);
  letter-spacing: 2px;
}
.header .time {
  position: absolute;
  left: 20px;
  top: 10px;
  color: #a0cfff;
  font-size: 16px;
}

.metrics {
  display: flex;
  justify-content: space-around;
  margin: 20px 0;
  height: 120px;
}
.metric-card {
  flex: 1;
  margin: 0 15px;
  background: rgba(0, 246, 255, 0.05);
  border: 1px solid rgba(0, 246, 255, 0.2);
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  box-shadow: inset 0 0 20px rgba(0, 246, 255, 0.1);
}
.metric-title { color: #a0cfff; font-size: 16px; margin-bottom: 10px; }
.metric-value {
  color: #00f6ff;
  font-size: 42px;
  font-weight: bold;
  font-family: 'Courier New', Courier, monospace;
  text-shadow: 0 0 15px rgba(0, 246, 255, 0.8);
}

.charts-container {
  display: flex;
  flex: 2;
  margin-bottom: 20px;
}
.chart-box {
  flex: 1;
  background: rgba(0, 246, 255, 0.05);
  border: 1px solid rgba(0, 246, 255, 0.2);
  border-radius: 10px;
  margin: 0 15px;
  padding: 15px;
  display: flex;
  flex-direction: column;
}
.chart-title {
  font-size: 18px;
  color: #00f6ff;
  margin-bottom: 10px;
  border-left: 4px solid #00f6ff;
  padding-left: 10px;
}
.chart {
  flex: 1;
  width: 100%;
}

.latest-box {
  flex: 1;
  background: rgba(0, 246, 255, 0.05);
  border: 1px solid rgba(0, 246, 255, 0.2);
  border-radius: 10px;
  margin: 0 15px;
  padding: 15px;
  overflow: hidden;
}
.scroll-list {
  height: calc(100% - 30px);
  overflow-y: auto;
}
.list-item {
  display: flex;
  justify-content: space-between;
  padding: 10px;
  border-bottom: 1px dashed rgba(255, 255, 255, 0.1);
  font-size: 14px;
}
.list-item:nth-child(even) { background: rgba(255, 255, 255, 0.02); }
.list-item .time { color: #67C23A; width: 100px; }
.list-item .action { color: #00f6ff; flex: 1; font-weight: bold; }
.list-item .location { color: #a0cfff; flex: 1; }
.list-item .operator { color: #E6A23C; width: 150px; text-align: right; }

::-webkit-scrollbar { width: 6px; }
::-webkit-scrollbar-thumb { background: rgba(0, 246, 255, 0.3); border-radius: 3px; }
</style>