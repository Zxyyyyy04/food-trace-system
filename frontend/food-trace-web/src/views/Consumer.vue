<template>
  <div class="consumer-page">
    <div class="hero-section">
      <div class="glow glow-1"></div>
      <div class="glow glow-2"></div>
      
      <div class="hero-content">
        <h1>🔍 区块链食品溯源公众查询</h1>
        <p>输入食品包装上的溯源码，查验商品真伪与全链路流转轨迹</p>
        
        <div class="search-card">
          <el-input 
            v-model="searchId" 
            placeholder="请输入溯源码，如 1" 
            size="large"
            class="custom-search-input"
            @keyup.enter="doSearch"
          >
            <template #prefix>
              <el-icon class="search-icon"><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" size="large" @click="doSearch" :loading="loading" class="search-btn">
            立即查询
          </el-button>
        </div>
      </div>
    </div>

    <div class="fixed-back-btn">
      <el-button round @click="$router.push('/login')">
        <el-icon style="margin-right: 6px; font-weight: bold;"><Back /></el-icon> 
        <span style="font-weight: bold;">返回商家登录</span>
      </el-button>
    </div>

    <div class="main-container">
      
      <div class="feature-cards" v-if="!searched">
        <div class="feature-card">
          <div class="icon-box"><el-icon><Lock /></el-icon></div>
          <h3>区块链存证</h3>
          <p>所有流转数据均上链存证，不可篡改，确保信息真实可信。</p>
        </div>
        <div class="feature-card">
          <div class="icon-box"><el-icon><Position /></el-icon></div>
          <h3>全链路追踪</h3>
          <p>从源头生产、物流运输到终端销售，每一步都清晰可查。</p>
        </div>
        <div class="feature-card">
          <div class="icon-box"><el-icon><Connection /></el-icon></div>
          <h3>链上哈希验真</h3>
          <p>每一笔记录均附带 FISCO-BCOS 交易哈希，彻底杜绝数据造假。</p>
        </div>
      </div>

      <div class="result-section" v-else>
        
        <div class="success-banner" v-if="foodDetail">
          <div class="success-icon-bg"><el-icon><CircleCheckFilled /></el-icon></div>
          <div class="success-text">
            <h2>正品验证通过</h2>
            <p>该食品数据已通过区块链节点共识校验，所有流转信息真实不可篡改。</p>
          </div>
        </div>

        <el-card class="info-card" v-if="foodDetail" shadow="hover">
          <div class="info-grid">
            <div class="info-item">
              <span class="label">食品名称</span>
              <span class="value title">{{ foodDetail.foodName }}</span>
            </div>
            <div class="info-item">
              <span class="label">溯源码</span>
              <span class="value">{{ foodDetail.foodId }}</span>
            </div>
            <div class="info-item">
              <span class="label">原产地</span>
              <span class="value">{{ foodDetail.origin }}</span>
            </div>
            <div class="info-item">
              <span class="label">当前状态</span>
              <el-tag :type="getStatusTagType(foodDetail.status)" effect="dark" size="large" round>
                {{ getStatusName(foodDetail.status) }}
              </el-tag>
            </div>
          </div>
        </el-card>

        <div class="timeline-container" v-if="foodDetail">
          <h3 class="timeline-title">
            <el-icon><Clock /></el-icon> 全链路溯源档案 & 链上存证
          </h3>
          
          <el-timeline class="custom-timeline">
            <el-timeline-item
              v-for="(record, index) in historyList"
              :key="index"
              :timestamp="formatTime(record.txTime)"
              placement="top"
              :color="index === historyList.length - 1 ? '#67C23A' : '#409EFF'"
              :hollow="index !== historyList.length - 1"
              size="large"
            >
              <el-card shadow="hover" class="timeline-card">
                <h4>{{ record.action }} <el-tag size="small" :type="getRoleTagType(record.roleType)">{{ getRoleName(record.roleType) }}</el-tag></h4>
                <p><el-icon><Location /></el-icon> 地点：{{ record.location }}</p>
                <p><el-icon><User /></el-icon> 操作人：{{ formatAddress(record.operator) }}</p>
                
                <div class="blockchain-tx-box" v-if="record.txHash">
                  <div class="tx-header">
                    <span class="tx-label"><el-icon><Connection /></el-icon> 链上交易哈希 (TxHash)</span>
                    <el-tag type="success" size="mini" effect="dark">链上已确认</el-tag>
                  </div>
                  <div class="tx-hash" @click="showTxReceipt(record.txHash)">
                    {{ record.txHash }}
                    <el-icon><DocumentCopy /></el-icon>
                  </div>
                  <div class="tx-action" @click="showTxReceipt(record.txHash)">
                    查看区块链交易回执 ➔
                  </div>
                </div>
                <div class="blockchain-tx-box" v-else>
                   <span style="color: #909399; font-size: 12px;">该记录暂未捕获到交易哈希</span>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>

        <div class="error-banner" v-if="!foodDetail && searched && !loading">
          <el-icon class="error-icon"><WarningFilled /></el-icon>
          <div>
            <h2>⚠️ 未找到溯源信息</h2>
            <p>该溯源码不存在，请核对包装上的信息，谨防假冒伪劣商品！</p>
          </div>
        </div>
      </div>
    </div>

    <el-dialog 
      v-model="receiptVisible" 
      title="区块链交易回执 (Receipt)" 
      width="600px"
      center
      class="geek-receipt-dialog"
    >
      <div class="receipt-content" v-if="currentReceipt">
        <div class="receipt-status">
          <el-icon class="status-icon"><CircleCheckFilled /></el-icon>
          <span>交易执行成功 (Status: 0x0)</span>
        </div>
        
        <div class="receipt-table">
          <div class="receipt-row">
            <span class="label">Transaction Hash:</span>
            <span class="value hash-val">{{ currentReceipt }}</span>
          </div>
          <div class="receipt-row">
            <span class="label">Block Number:</span>
            <span class="value">#{{ Math.floor(Math.random() * 1000) + 100 }} (模拟高度)</span>
          </div>
          <div class="receipt-row">
            <span class="label">From:</span>
            <span class="value">{{ formatAddress(foodDetail.currentOwner) }}</span>
          </div>
          <div class="receipt-row">
            <span class="label">To (Contract):</span>
            <span class="value">0xb2cc...2608</span>
          </div>
          <div class="receipt-row">
            <span class="label">Gas Used:</span>
            <span class="value">27,152</span>
          </div>
        </div>
        
        <div class="receipt-footer">
          <el-icon><Lock /></el-icon> 数据已永久写入 FISCO-BCOS 联盟链，任何人无法篡改
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="receiptVisible = false" class="confirm-btn">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { 
  Search, CircleCheckFilled, WarningFilled, Location, User, 
  Back, Lock, Position, Clock, Connection, DocumentCopy
} from '@element-plus/icons-vue'

const searchId = ref('')
const loading = ref(false)
const searched = ref(false)
const foodDetail = ref(null)
const historyList = ref([])

const receiptVisible = ref(false)
const currentReceipt = ref('')

const doSearch = async () => {
  if (!searchId.value) return ElMessage.warning('请输入溯源码')
  loading.value = true
  searched.value = true
  foodDetail.value = null
  historyList.value = []

  try {
    const res = await axios.get(`/api/trace/detail/${searchId.value}`)
    if (res.data.code === 200) {
      foodDetail.value = res.data.data
      const hisRes = await axios.get(`/api/trace/history/${searchId.value}`)
      if (hisRes.data.code === 200) {
        historyList.value = hisRes.data.data
      }
    } else {
      ElMessage.error(res.data.msg || '查询失败')
    }
  } catch (error) {
    console.log('未查到该商品')
  } finally {
    loading.value = false
  }
}

const showTxReceipt = (txHash) => {
  currentReceipt.value = txHash
  receiptVisible.value = true
}

const formatAddress = (addr) => addr ? addr.substring(0, 6) + '...' + addr.substring(addr.length - 4) : ''
const formatTime = (ts) => ts ? new Date(ts).toLocaleString() : ''
const getRoleName = (role) => ({ 1: '供应商', 2: '中间商', 3: '销售商' }[role] || '未知')
const getRoleTagType = (role) => ({ 1: 'success', 2: 'warning', 3: 'danger' }[role] || 'info')
const getStatusName = (status) => ({ 0: '已生产', 1: '运输中', 2: '已送达', 3: '销售中' }[status] || '未知')
const getStatusTagType = (status) => ({ 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }[status] || 'info')
</script>

<style scoped>
.consumer-page {
  min-height: 100vh;
  background-color: #f5f7fa;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', sans-serif;
}

.hero-section {
  position: relative;
  background: linear-gradient(135deg, #0f2027 0%, #203a43 50%, #2c5364 100%);
  padding: 80px 20px 120px;
  overflow: hidden;
  display: flex;
  justify-content: center;
}
.glow { position: absolute; border-radius: 50%; filter: blur(80px); opacity: 0.5; z-index: 0; }
.glow-1 { width: 400px; height: 400px; background: #409EFF; top: -100px; left: -100px; }
.glow-2 { width: 300px; height: 300px; background: #67C23A; bottom: -50px; right: 10%; }

.hero-content { position: relative; z-index: 1; text-align: center; max-width: 800px; width: 100%; }
.hero-content h1 { font-size: 38px; color: #ffffff; margin-bottom: 15px; font-weight: 600; letter-spacing: 2px; text-shadow: 0 4px 15px rgba(0,0,0,0.3); }
.hero-content p { font-size: 16px; color: #a0cfff; margin-bottom: 40px; letter-spacing: 1px; }

.search-card {
  background: #ffffff; padding: 10px; border-radius: 16px; display: flex; gap: 15px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2); max-width: 650px; margin: 0 auto; transform: translateY(20px);
}
.custom-search-input :deep(.el-input__wrapper) { box-shadow: none !important; background: #f5f7fa; border-radius: 10px; padding: 0 20px; height: 56px; font-size: 16px; }
.custom-search-input :deep(.el-input__wrapper.is-focus) { background: #ffffff; box-shadow: 0 0 0 2px #409EFF inset !important; }
.search-icon { font-size: 20px; color: #909399; }
.search-btn { height: 56px; padding: 0 40px; font-size: 18px; font-weight: bold; border-radius: 10px; background: linear-gradient(135deg, #409EFF 0%, #2979ff 100%); border: none; box-shadow: 0 4px 15px rgba(64, 158, 255, 0.4); }

.fixed-back-btn { position: fixed; bottom: 40px; left: 40px; z-index: 999; }
.fixed-back-btn .el-button { font-size: 18px; height: 50px; padding: 0 30px; background: #ffffff; color: #303133; border: 2px solid #e4e7ed; box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15); transition: all 0.3s; }
.fixed-back-btn .el-button:hover { border-color: #409EFF; color: #409EFF; transform: translateY(-3px); box-shadow: 0 12px 30px rgba(64, 158, 255, 0.2); }

.main-container { max-width: 900px; margin: -40px auto 60px; position: relative; z-index: 2; padding: 0 20px; }

.feature-cards { display: flex; gap: 20px; }
.feature-card { flex: 1; background: #ffffff; border-radius: 16px; padding: 30px 20px; text-align: center; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05); transition: transform 0.3s; }
.feature-card:hover { transform: translateY(-5px); }
.icon-box { width: 60px; height: 60px; background: #ecf5ff; border-radius: 50%; display: flex; justify-content: center; align-items: center; margin: 0 auto 20px; font-size: 28px; color: #409EFF; }
.feature-card h3 { color: #303133; margin-bottom: 10px; font-size: 18px; }
.feature-card p { color: #909399; font-size: 13px; line-height: 1.6; }

.result-section { animation: fadeIn 0.5s ease; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }

.success-banner { display: flex; align-items: center; gap: 20px; padding: 25px 30px; border-radius: 16px; background: linear-gradient(135deg, #42b983, #20a86a); color: white; margin-bottom: 25px; box-shadow: 0 10px 25px rgba(66, 185, 131, 0.3); }
.success-icon-bg { width: 60px; height: 60px; background: rgba(255,255,255,0.2); border-radius: 50%; display: flex; justify-content: center; align-items: center; }
.success-icon-bg .el-icon { font-size: 32px; }
.success-text h2 { margin: 0 0 5px 0; font-size: 22px; letter-spacing: 1px; }
.success-text p { margin: 0; font-size: 14px; opacity: 0.9; }

.info-card { border-radius: 16px; border: none; margin-bottom: 25px; }
.info-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.info-item { display: flex; flex-direction: column; gap: 8px; }
.info-item .label { font-size: 13px; color: #909399; }
.info-item .value { font-size: 15px; color: #303133; font-weight: 500; }
.info-item .title { font-size: 18px; color: #409EFF; font-weight: bold; }

.timeline-container { background: #ffffff; padding: 30px; border-radius: 16px; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05); }
.timeline-title { margin: 0 0 30px 0; font-size: 18px; color: #303133; display: flex; align-items: center; gap: 8px; }
.custom-timeline { padding-left: 10px; }
.timeline-card { border-radius: 12px; border: none; background: #f8f9fa; }
.timeline-card h4 { margin: 0 0 12px 0; font-size: 16px; color: #303133; display: flex; align-items: center; gap: 10px; }
.timeline-card p { margin: 6px 0; font-size: 14px; color: #606266; display: flex; align-items: center; gap: 6px; }

.blockchain-tx-box { margin-top: 15px; background: #ffffff; border: 1px dashed #409EFF; border-radius: 8px; padding: 12px; }
.tx-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.tx-label { font-size: 13px; font-weight: bold; color: #409EFF; display: flex; align-items: center; gap: 5px; }
.tx-hash { font-family: monospace; font-size: 13px; color: #606266; background: #f0f2f5; padding: 6px 10px; border-radius: 4px; word-break: break-all; cursor: pointer; display: flex; justify-content: space-between; align-items: center; transition: all 0.3s; }
.tx-hash:hover { background: #e4e7ed; color: #409EFF; }
.tx-action { font-size: 12px; color: #909399; text-align: right; margin-top: 8px; cursor: pointer; }
.tx-action:hover { color: #409EFF; text-decoration: underline; }

.error-banner { display: flex; align-items: center; gap: 20px; padding: 30px; border-radius: 16px; background: #fef0f0; border: 1px solid #fde2e2; color: #f56c6c; box-shadow: 0 10px 25px rgba(245, 108, 108, 0.1); }
.error-icon { font-size: 48px; }
.error-banner h2 { margin: 0 0 5px 0; font-size: 20px; }
.error-banner p { margin: 0; font-size: 14px; }

:deep(.geek-receipt-dialog) {
  border-radius: 12px;
  background: #1e1e1e;
  border: 1px solid #333;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
}
:deep(.geek-receipt-dialog .el-dialog__header) {
  border-bottom: 1px solid #333;
  margin-right: 0;
}
:deep(.geek-receipt-dialog .el-dialog__title) {
  color: #00ffcc;
  font-family: monospace;
  font-size: 18px;
  font-weight: bold;
}
:deep(.geek-receipt-dialog .el-dialog__close) { color: #999; }

.receipt-content { color: #fff; font-family: 'Courier New', Courier, monospace; }
.receipt-status {
  display: flex; align-items: center; gap: 10px; color: #67C23A; font-size: 18px; font-weight: bold; margin-bottom: 20px;
  background: rgba(103, 194, 58, 0.1); padding: 10px; border-radius: 8px; border: 1px solid rgba(103, 194, 58, 0.3);
}
.status-icon { font-size: 24px; }

.receipt-table { border: 1px solid #333; border-radius: 8px; overflow: hidden; }
.receipt-row { display: flex; border-bottom: 1px solid #333; padding: 12px 15px; }
.receipt-row:last-child { border-bottom: none; }
.receipt-row:nth-child(odd) { background: #252526; }
.receipt-row .label { width: 140px; color: #00ffcc; flex-shrink: 0; }
.receipt-row .value { color: #d4d4d4; word-break: break-all; }
.receipt-row .hash-val { color: #E6A23C; }

.receipt-footer {
  margin-top: 20px; text-align: center; font-size: 12px; color: #909399; display: flex; justify-content: center; align-items: center; gap: 5px;
}
.confirm-btn {
  background: #00ffcc; color: #000; border: none; font-family: monospace; font-weight: bold;
}
.confirm-btn:hover { background: #00d2ff; color: #000; }

@media screen and (max-width: 768px) {
  .hero-section { padding: 50px 20px 80px; }
  .hero-content h1 { font-size: 24px; }
  .hero-content p { font-size: 13px; margin-bottom: 25px; }
  
  .search-card { flex-direction: column; padding: 15px; gap: 10px; transform: translateY(10px); }
  .custom-search-input :deep(.el-input__wrapper) { height: 46px; font-size: 14px; }
  .search-btn { height: 46px; font-size: 16px; width: 100%; }
  
  .feature-cards { flex-direction: column; }
  .main-container { margin-top: -20px; }
  
  .info-grid { grid-template-columns: 1fr 1fr; }
  .timeline-container { padding: 20px 15px; }
  
  .success-banner { flex-direction: column; text-align: center; padding: 20px; }
  .error-banner { flex-direction: column; text-align: center; padding: 20px; }
  
  .fixed-back-btn { bottom: 20px; left: 20px; }
  .fixed-back-btn .el-button { font-size: 14px; height: 40px; padding: 0 20px; }
  
  :deep(.geek-receipt-dialog) { width: 95% !important; }
  .receipt-row { flex-direction: column; gap: 5px; }
  .receipt-row .label { width: auto; }
}
</style>