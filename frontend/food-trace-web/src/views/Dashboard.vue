<template>
  <el-container class="dashboard-container">
    <el-header class="header">
      <div class="logo-area">
        <span class="logo-icon">🌿</span>
        <span class="logo-text">区块链食品溯源平台</span>
      </div>
      
      <div class="user-info" v-if="userInfo">
        <el-tag :type="getRoleTagType(userInfo.role)" effect="dark" class="role-tag" round>
          {{ getRoleName(userInfo.role) }}
        </el-tag>
        <div class="user-profile">
          <div class="avatar">{{ userInfo.name.charAt(0).toUpperCase() }}</div>
          <span class="username">{{ userInfo.name }}</span>
        </div>
        <el-button type="danger" plain round size="small" class="logout-btn" @click="logout">
          退出登录
        </el-button>
      </div>
    </el-header>

    <el-container class="main-wrapper">
      <el-aside width="220px" class="aside">
        <el-menu 
          :default-active="activeMenu" 
          @select="handleMenuSelect"
          background-color="#1f2d3d"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          class="custom-menu"
        >
          <el-menu-item index="list">
            <el-icon><List /></el-icon>
            <span>溯源数据列表</span>
          </el-menu-item>
          <el-menu-item index="create" v-if="userInfo?.role === 1">
            <el-icon><Plus /></el-icon>
            <span>生产食品入库</span>
          </el-menu-item>

          <el-menu-item index="screen" @click="$router.push('/screen')">
            <el-icon><DataLine /></el-icon>
            <span>数据可视化大屏</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main class="main-content">
        
        <div v-if="activeMenu === 'list'">
          <el-card class="modern-card" shadow="never">
            <template #header>
              <div class="card-header">
                <div class="header-title">
                  <el-icon><Document /></el-icon>
                  <span>溯源数据列表</span>
                </div>
                <el-button type="primary" icon="Refresh" @click="fetchList" round>刷新列表</el-button>
              </div>
            </template>
            
            <div class="search-bar">
              <el-input 
                v-model="searchId" 
                placeholder="输入食品溯源码直接查询" 
                class="search-input" 
                size="large"
                clearable
                @keyup.enter="searchById"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-button type="primary" size="large" @click="searchById" round>单条查询</el-button>
            </div>

            <el-table 
              :data="displayTableData" 
              style="width: 100%" 
              v-loading="tableLoading" 
              stripe 
              size="large"
              class="large-table"
            >
              <el-table-column prop="foodId" label="溯源码" min-width="120">
                <template #default="scope">
                  <span class="food-id-text">{{ scope.row.foodId }}</span>
                </template>
              </el-table-column>
              
              <el-table-column prop="foodName" label="食品名称" min-width="150">
                <template #default="scope">
                  <b style="font-size: 15px;">{{ scope.row.foodName }}</b>
                </template>
              </el-table-column>
              
              <el-table-column prop="origin" label="原产地" min-width="150"></el-table-column>
              
              <el-table-column label="当前拥有者" min-width="180">
                <template #default="scope">
                  <el-tag size="large" type="info" effect="plain" round>{{ formatAddress(scope.row.currentOwner) }}</el-tag>
                </template>
              </el-table-column>
              
              <el-table-column label="状态" width="140" align="center">
                <template #default="scope">
                  <el-tag :type="getStatusTagType(scope.row.status)" effect="light" size="large" round>
                    {{ getStatusName(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              
              <el-table-column label="操作" min-width="300" fixed="right" align="center">
                <template #default="scope">
                  <el-button type="primary" plain size="large" icon="View" @click="showDetails(scope.row.foodId)">溯源档案</el-button>
                  
                  <el-button type="warning" plain size="large" icon="Van" 
                             v-if="userInfo?.role === 1 && scope.row.status === 0" 
                             @click="openActionDialog('transfer', scope.row)">发货</el-button>
                             
                  <el-button type="success" plain size="large" icon="Box" 
                             v-if="userInfo?.role === 2 && scope.row.status === 1" 
                             @click="openActionDialog('deliver', scope.row)">送达</el-button>
                             
                  <el-button type="danger" plain size="large" icon="ShoppingCart" 
                             v-if="userInfo?.role === 3 && scope.row.status === 2" 
                             @click="openActionDialog('onsale', scope.row)">上架</el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="pagination-container">
              <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :page-sizes="[5, 10, 20, 50]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="total"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                background
              />
            </div>

          </el-card>
        </div>

        <div v-if="activeMenu === 'create'" class="create-page-container">
          <el-card class="modern-card create-card" shadow="never">
            <div class="create-header">
              <div class="create-icon-wrapper">
                <el-icon class="create-icon"><Plus /></el-icon>
              </div>
              <h2>生产食品并上链</h2>
              <p>录入新批次食品信息，数据将永久存储至区块链</p>
            </div>
            
            <div class="form-wrapper">
              <el-form :model="createForm" label-position="top" class="create-form">
                <el-form-item label="食品溯源码">
                  <el-input v-model="createForm.foodId" placeholder="请输入唯一溯源码，如: 1" size="large">
                    <template #prefix><el-icon><Ticket /></el-icon></template>
                  </el-input>
                </el-form-item>
                
                <el-form-item label="食品名称">
                  <el-input v-model="createForm.foodName" placeholder="请输入食品名称，如: 可乐" size="large">
                    <template #prefix><el-icon><Goods /></el-icon></template>
                  </el-input>
                </el-form-item>
                
                <el-form-item label="原产地">
                  <el-input v-model="createForm.origin" placeholder="请输入原产地，如: 福建" size="large">
                    <template #prefix><el-icon><Location /></el-icon></template>
                  </el-input>
                </el-form-item>
                
                <el-form-item style="margin-top: 40px;">
                  <el-button type="primary" size="large" icon="Promotion" @click="doCreate" :loading="btnLoading" class="submit-btn" round>
                    提交上链
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-card>
        </div>

        <el-dialog :title="actionTitle" v-model="actionDialogVisible" width="450px" class="modern-dialog" center>
          <el-form :model="actionForm" label-position="top">
            <el-form-item label="溯源码">
              <el-input v-model="actionForm.foodId" readonly size="large"></el-input>
            </el-form-item>
            <el-form-item label="接收方地址" v-if="actionType !== 'onsale'">
              <el-input v-model="actionForm.targetAddress" placeholder="请输入 0x 地址" size="large"></el-input>
            </el-form-item>
            <el-form-item label="操作地点">
              <el-input v-model="actionForm.location" placeholder="如：北京朝阳中转仓" size="large"></el-input>
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="actionDialogVisible = false" size="large">取消</el-button>
            <el-button type="primary" @click="submitAction" :loading="btnLoading" size="large">确认提交</el-button>
          </template>
        </el-dialog>

        <el-dialog title="全链路溯源档案" v-model="detailVisible" width="650px" class="modern-dialog" center>
          <el-descriptions v-if="foodDetail" :column="2" border class="detail-desc">
            <el-descriptions-item label="食品名称"><b>{{ foodDetail.foodName }}</b></el-descriptions-item>
            <el-descriptions-item label="溯源码">{{ foodDetail.foodId }}</el-descriptions-item>
            <el-descriptions-item label="原产地">{{ foodDetail.origin }}</el-descriptions-item>
            <el-descriptions-item label="当前状态">
              <el-tag :type="getStatusTagType(foodDetail.status)" effect="dark" round>{{ getStatusName(foodDetail.status) }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>

          <div class="timeline-title">
            <el-icon><Clock /></el-icon> 流转轨迹
          </div>
          <el-timeline class="custom-timeline">
            <el-timeline-item
              v-for="(record, index) in historyList"
              :key="index"
              :timestamp="formatTime(record.txTime)"
              placement="top"
              :color="index === historyList.length - 1 ? '#67C23A' : '#409EFF'"
              :hollow="index !== historyList.length - 1"
            >
              <el-card shadow="hover" class="timeline-card">
                <h4 class="timeline-action">
                  {{ record.action }} 
                  <el-tag size="small" :type="getRoleTagType(record.roleType)">{{ getRoleName(record.roleType) }}</el-tag>
                </h4>
                <p class="timeline-info"><el-icon><Location /></el-icon> 地点：{{ record.location }}</p>
                <p class="timeline-info"><el-icon><User /></el-icon> 操作人：{{ formatAddress(record.operator) }}</p>
                <p class="timeline-remark">备注：{{ record.remark }}</p>
                
                <div class="tx-hash-box" v-if="record.txHash && record.txHash !== '0x_mock_hash_'">
                  <div class="tx-label">
                    <el-icon><Connection /></el-icon> 区块链交易哈希：
                  </div>
                  <div class="tx-value" @click="showTxHash(record.txHash)">
                    {{ record.txHash }}
                  </div>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-dialog>

      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { List, Plus, Document, Search, Refresh, View, Van, Box, ShoppingCart, Promotion, Clock, Location, User, Goods, Ticket, DataLine, Connection } from '@element-plus/icons-vue'

const router = useRouter()
const userInfo = ref(JSON.parse(localStorage.getItem('user')))
const activeMenu = ref('list')

const allTableData = ref([]) 
const tableLoading = ref(false)
const searchId = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const displayTableData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return allTableData.value.slice(start, end)
})

const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}
const handleCurrentChange = (val) => {
  currentPage.value = val
}

const createForm = reactive({ foodId: '', foodName: '', origin: '' })
const btnLoading = ref(false)

const detailVisible = ref(false)
const foodDetail = ref(null)
const historyList = ref([])

const actionDialogVisible = ref(false)
const actionType = ref('')
const actionTitle = ref('')
const actionForm = reactive({ foodId: '', targetAddress: '', location: '' })

onMounted(() => {
  if (!userInfo.value) {
    router.push('/login')
  } else {
    fetchList()
  }
})

const getHeaders = () => ({ headers: { Authorization: localStorage.getItem('token') } })

const fetchList = async () => {
  tableLoading.value = true
  try {
    const res = await axios.get('/api/trace/list', getHeaders())
    if (res.data.code === 200) {
      allTableData.value = res.data.data
      total.value = res.data.data.length
      currentPage.value = 1
    }
  } catch (error) {
    ElMessage.error('获取列表失败')
  } finally {
    tableLoading.value = false
  }
}

const searchById = () => {
  if (!searchId.value) return fetchList()
  showDetails(searchId.value)
}

const showDetails = async (foodId) => {
  const headers = getHeaders()
  try {
    const res = await axios.get(`/api/trace/detail/${foodId}`, headers)
    if (res.data.code === 200) {
      foodDetail.value = res.data.data
      detailVisible.value = true
      const hisRes = await axios.get(`/api/trace/history/${foodId}`, headers)
      if (hisRes.data.code === 200) historyList.value = hisRes.data.data
    } else {
      ElMessage.error('未找到该食品')
    }
  } catch (error) {
    ElMessage.error('查询失败')
  }
}

const doCreate = async () => {
  if (!createForm.foodId || !createForm.foodName || !createForm.origin) {
    return ElMessage.warning('请填写完整信息')
  }
  btnLoading.value = true
  const params = new URLSearchParams()
  params.append('foodId', createForm.foodId)
  params.append('foodName', createForm.foodName)
  params.append('origin', createForm.origin)

  try {
    const res = await axios.post('/api/trace/create', params, getHeaders())
    if (res.data.code === 200) {
      ElMessage.success('上链成功！')
      createForm.foodId = ''
      createForm.foodName = ''
      createForm.origin = ''
      activeMenu.value = 'list'
      fetchList()
    } else {
      ElMessage.error(res.data.msg)
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '上链请求失败')
  } finally {
    btnLoading.value = false
  }
}

const openActionDialog = (type, row) => {
  actionType.value = type
  actionForm.foodId = row.foodId
  actionForm.targetAddress = ''
  actionForm.location = ''
  
  if (type === 'transfer') actionTitle.value = '发货给中间商'
  else if (type === 'deliver') actionTitle.value = '送达给销售商'
  else if (type === 'onsale') actionTitle.value = '上架销售'
  
  actionDialogVisible.value = true
}

const submitAction = async () => {
  if (actionType.value !== 'onsale' && !actionForm.targetAddress) {
    return ElMessage.warning('请输入接收方地址')
  }
  if (!actionForm.location) {
    return ElMessage.warning('请输入操作地点')
  }

  btnLoading.value = true
  const headers = getHeaders()
  const params = new URLSearchParams()
  params.append('foodId', actionForm.foodId)
  params.append('location', actionForm.location)

  try {
    let url = ''
    if (actionType.value === 'transfer') {
      url = '/api/trace/transfer/distributor'
      params.append('distributorAddress', actionForm.targetAddress)
    } else if (actionType.value === 'deliver') {
      url = '/api/trace/transfer/retailer'
      params.append('retailerAddress', actionForm.targetAddress)
    } else if (actionType.value === 'onsale') {
      url = '/api/trace/onsale'
    }

    const res = await axios.post(url, params, headers)
    if (res.data.code === 200) {
      ElMessage.success('操作成功并上链！')
      actionDialogVisible.value = false
      fetchList()
    } else {
      ElMessage.error(res.data.msg)
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '操作失败')
  } finally {
    btnLoading.value = false
  }
}

const showTxHash = (txHash) => {
  ElMessageBox.alert(
    `<div style="text-align: center; padding: 10px 0;">
      <div style="font-size: 40px; color: #67C23A; margin-bottom: 10px;">⛓️</div>
      <h3 style="margin: 0 0 10px 0; color: #303133;">链上存证已确认</h3>
      <p style="color: #606266; font-size: 14px; margin-bottom: 15px;">该数据已通过 FISCO-BCOS 节点共识，永久写入区块链，任何人无法篡改。</p>
      <div style="background: #f0f2f5; padding: 10px; border-radius: 6px; word-break: break-all; font-family: monospace; font-size: 12px; color: #409EFF; text-align: left;">
        Hash: ${txHash}
      </div>
    </div>`,
    '区块链存证验证',
    { dangerouslyUseHTMLString: true, confirmButtonText: '查验无误', center: true }
  )
}

const handleMenuSelect = (index) => { activeMenu.value = index }
const logout = () => { localStorage.clear(); router.push('/login') }
const formatAddress = (addr) => addr ? addr.substring(0, 6) + '...' + addr.substring(addr.length - 4) : ''
const formatTime = (ts) => ts ? new Date(ts).toLocaleString() : ''
const getRoleName = (role) => ({ 1: '供应商', 2: '中间商', 3: '销售商' }[role] || '未知')
const getRoleTagType = (role) => ({ 1: 'success', 2: 'warning', 3: 'danger' }[role] || 'info')
const getStatusName = (status) => ({ 0: '已生产', 1: '运输中', 2: '已送达', 3: '销售中' }[status] || '未知')
const getStatusTagType = (status) => ({ 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }[status] || 'info')
</script>

<style scoped>
.dashboard-container { height: 100vh; background-color: #f0f2f5; }

.header {
  background-color: #ffffff; color: #333; display: flex; justify-content: space-between; align-items: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05); z-index: 10; padding: 0 30px;
}
.logo-area { display: flex; align-items: center; gap: 10px; }
.logo-icon { font-size: 24px; }
.logo-text { font-size: 20px; font-weight: bold; color: #2c3e50; letter-spacing: 1px; }
.user-info { display: flex; align-items: center; gap: 20px; }
.role-tag { font-weight: 600; padding: 0 15px; }
.user-profile { display: flex; align-items: center; gap: 10px; }
.avatar {
  width: 36px; height: 36px; background: linear-gradient(135deg, #409EFF, #2979ff); color: white;
  border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: bold; font-size: 16px;
}
.username { font-size: 15px; font-weight: 600; color: #333; }
.logout-btn { margin-left: 10px; }

.aside { background-color: #1f2d3d; box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1); }
.custom-menu { border-right: none; }
.custom-menu .el-menu-item { height: 56px; line-height: 56px; margin: 10px 15px; border-radius: 8px; transition: all 0.3s; }
.custom-menu .el-menu-item:hover { background-color: #2b3643 !important; }
.custom-menu .el-menu-item.is-active { background-color: #263445 !important; font-weight: bold; box-shadow: inset 4px 0 0 #409EFF; }

.main-wrapper { height: calc(100vh - 60px); }
.main-content { padding: 24px; }
.modern-card { border-radius: 12px; border: none; box-shadow: 0 4px 12px rgba(0,0,0,0.03); }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-title { font-size: 18px; font-weight: bold; color: #303133; display: flex; align-items: center; gap: 8px; }

.search-bar { display: flex; gap: 15px; margin-bottom: 24px; }
.search-input { width: 350px; }
.food-id-text { 
  font-family: monospace; font-weight: bold; color: #409EFF; 
  background-color: #ecf5ff; padding: 4px 10px; border-radius: 6px; font-size: 15px;
}

:deep(.large-table) { font-size: 15px !important; }
:deep(.large-table th.el-table__cell) {
  font-size: 16px !important; font-weight: bold !important; color: #303133 !important;
  padding: 14px 0 !important; background-color: #f8f9fa !important;
}
:deep(.large-table td.el-table__cell) { padding: 16px 0 !important; }
:deep(.large-table .el-tag) {
  font-size: 14px !important; height: 32px !important; line-height: 30px !important; padding: 0 14px !important;
}
:deep(.large-table .el-button) { font-size: 14px !important; padding: 10px 16px !important; }

.pagination-container { margin-top: 30px; display: flex; justify-content: flex-end; }

.create-page-container { display: flex; justify-content: center; align-items: flex-start; padding-top: 20px; }
.create-card { width: 100%; max-width: 700px; border-radius: 16px; overflow: hidden; }
.create-header { text-align: center; padding: 40px 0 20px; }
.create-icon-wrapper {
  width: 72px; height: 72px; background: #ecf5ff; border-radius: 50%;
  display: flex; align-items: center; justify-content: center; margin: 0 auto 20px; color: #409EFF;
}
.create-icon { font-size: 36px; }
.create-header h2 { margin: 0; font-size: 24px; color: #303133; letter-spacing: 1px; }
.create-header p { margin: 10px 0 30px; color: #909399; font-size: 14px; }
.form-wrapper { display: flex; justify-content: center; padding: 0 40px 50px; }
.create-form { width: 100%; max-width: 450px; }

:deep(.create-form .el-input__wrapper) {
  border-radius: 10px; padding: 6px 15px; background-color: #f8f9fa;
  box-shadow: 0 0 0 1px #e4e7ed inset; transition: all 0.3s;
}
:deep(.create-form .el-input__wrapper:hover) { box-shadow: 0 0 0 1px #c0c4cc inset; }
:deep(.create-form .el-input__wrapper.is-focus) { background-color: #ffffff; box-shadow: 0 0 0 2px #409EFF inset; }
:deep(.create-form .el-form-item__label) { font-size: 15px; font-weight: 600; color: #606266; padding-bottom: 8px; }

.submit-btn {
  width: 100%; height: 50px; font-size: 18px; font-weight: 600; letter-spacing: 2px;
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.3); transition: all 0.3s; border: none;
  background: linear-gradient(135deg, #409EFF 0%, #2979ff 100%);
}
.submit-btn:hover { transform: translateY(-2px); box-shadow: 0 12px 25px rgba(64, 158, 255, 0.4); }

:deep(.modern-dialog) { border-radius: 16px; box-shadow: 0 20px 50px rgba(0,0,0,0.2); }
:deep(.modern-dialog .el-dialog__header) { padding: 24px; border-bottom: 1px solid #f0f0f0; }
:deep(.modern-dialog .el-dialog__title) { font-size: 20px; font-weight: bold; }
:deep(.modern-dialog .el-dialog__body) { padding: 30px; }

.detail-desc { margin-bottom: 30px; background-color: #fafafa; border-radius: 8px; padding: 10px; }
.timeline-title { font-size: 18px; font-weight: bold; margin-bottom: 20px; display: flex; align-items: center; gap: 8px; color: #303133; }
.custom-timeline { padding-left: 10px; }
.timeline-card { border-radius: 10px; border: 1px solid #ebeef5; }
.timeline-action { margin: 0 0 12px 0; font-size: 16px; color: #303133; display: flex; align-items: center; gap: 10px; }
.timeline-info { margin: 6px 0; color: #606266; font-size: 14px; display: flex; align-items: center; gap: 6px; }
.timeline-remark { margin: 10px 0 0 0; color: #909399; font-size: 13px; background-color: #f8f9fa; padding: 8px; border-radius: 6px; }

.tx-hash-box { margin-top: 15px; background: #f8f9fa; border: 1px dashed #dcdfe6; border-radius: 8px; padding: 10px; }
.tx-label { display: flex; align-items: center; gap: 5px; font-size: 12px; color: #909399; margin-bottom: 5px; }
.tx-value { font-family: monospace; font-size: 13px; color: #409EFF; cursor: pointer; word-break: break-all; transition: opacity 0.3s; }
.tx-value:hover { opacity: 0.7; text-decoration: underline; }

@media screen and (max-width: 768px) {
  .aside { display: none; }
  .header { padding: 0 15px; }
  .logo-text { font-size: 16px; }
  .username { display: none; }
  .main-content { padding: 10px; }
  .search-bar { flex-direction: column; gap: 10px; }
  .search-input { width: 100%; }
  
  :deep(.large-table) { font-size: 13px !important; }
  :deep(.large-table th.el-table__cell) { font-size: 14px !important; padding: 10px 0 !important;}
  :deep(.large-table td.el-table__cell) { padding: 12px 0 !important; }
  :deep(.large-table .el-button) { font-size: 12px !important; padding: 8px 12px !important;}
  
  :deep(.el-dialog) { width: 90% !important; margin-top: 5vh !important; }
}
</style>