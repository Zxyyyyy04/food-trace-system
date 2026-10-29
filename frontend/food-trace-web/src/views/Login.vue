<template>
  <div class="login-page">
    <div class="header">
      <img src="/images/logo.png" alt="logo.png" class="platform-logo" />
      <h1>区块链食品溯源平台</h1>
    </div>

    <div class="consumer-section">
      <div class="consumer-btn" @click="goToConsumer">
        <el-icon class="consumer-icon"><Search /></el-icon>
        <span>我不是商家，我是消费者，去扫码查溯源 ➔</span>
      </div>
    </div>

    <div class="role-cards">
      <div class="card supplier-card" @click="openLogin('Supplier', 1)">
        <div class="card-content">
          <img src="/images/Supplier.png" alt="供应商" class="role-avatar" />
          <span class="role-title">供应商登录</span>
        </div>
        <div class="card-action">点击进入系统</div>
      </div>

      <div class="card distributor-card" @click="openLogin('Distributor', 2)">
        <div class="card-content">
          <img src="/images/Distributor.png" alt="中间商" class="role-avatar" />
          <span class="role-title">中间商登录</span>
        </div>
        <div class="card-action">点击进入系统</div>
      </div>

      <div class="card retailer-card" @click="openLogin('Retailer', 3)">
        <div class="card-content">
          <img src="/images/Retailer.png" alt="销售商" class="role-avatar" />
          <span class="role-title">销售商登录</span>
        </div>
        <div class="card-action">点击进入系统</div>
      </div>
    </div>

    <div class="poem-carousel-section">
      <transition name="fade" mode="out-in">
        <div class="poem-display" :key="currentPoemIndex">
          <div class="quote-mark left-quote">“</div>
          <div class="quote-mark right-quote">”</div>
          
          <div class="poem-header">
            <h2>{{ currentPoem.title }}</h2>
            <span class="author">{{ currentPoem.author }}</span>
          </div>
          <div class="poem-content">
            <p v-for="(line, index) in currentPoem.lines" :key="index">{{ line }}</p>
          </div>
        </div>
      </transition>
    </div>

    <div class="login-footer">
      <div class="footer-line"></div>
      <div class="footer-content">
        <span>© 2026 区块链食品溯源平台</span>
        <span class="divider">|</span>
        <span>底层依托 FISCO-BCOS 联盟链构建</span>
        <span class="divider">|</span>
        <span class="tech">Powered by Vue3 & Spring Boot</span>
      </div>
    </div>

    <div class="floating-ball-container" @click="logDialogVisible = true">
      <div class="hover-text">
        点我
        <div class="triangle"></div>
      </div>
      <div class="ball">
        <el-icon class="rolling-icon"><InfoFilled /></el-icon>
      </div>
    </div>

    <el-dialog 
      v-model="logDialogVisible" 
      title="系统架构与业务逻辑" 
      width="850px"
      center
      top="3vh"
      class="large-text-dialog"
    >
      <div class="text-content-wrapper">
        <div class="text-section">
          <h3>💻 一、 核心技术与架构栈</h3>
          <p><b>前端架构：</b>采用 Vue 3 + Vite 构建现代化单页应用，结合 Element Plus 框架实现高品质交互，通过 Axios 进行 RESTful API 通信。引入 ECharts 实现可视化大屏。</p>
          <p><b>后端架构：</b>基于 Java 8 + Spring Boot 2.7 搭建。采用 SSM 架构结合 MySQL 8.0 进行数据落库。使用拦截器实现基于 Token 的身份鉴权，利用 AOP 切面记录操作日志。</p>
          <p><b>区块链层：</b>底层依托 FISCO-BCOS 联盟链，通过 WeBASE-Front 进行节点管理。业务逻辑封装于 Solidity 智能合约中，通过严格的状态机控制与角色权限校验，确保数据不可篡改。</p>
          <p><b>扩展与安全：</b>生产环境采用 Nginx 反向代理，隐藏后端与区块链网关端口，仅对外暴露 80 端口，保障系统安全。通过解析回执，对外展示真实的区块链交易哈希（TxHash）。</p>
        </div>
        
        <div class="text-section">
          <h3>⚙️ 二、 核心业务逻辑与流转机制</h3>
          <p><b>1. 角色权限体系：</b>系统分为供应商、中间商、销售商及消费者。前三者必须通过 WeBASE 预置的链上地址进行身份验证登录，消费者查询接口免登录。</p>
          <p><b>2. 状态机流转控制：</b>食品生命周期严格遵循“已生产 (0) -> 运输中 (1) -> 已送达 (2) -> 销售中 (3)”的不可逆状态流转。智能合约利用 <code>require</code> 语句进行强校验，杜绝越权操作。</p>
          <p><b>3. 数据双写与存证：</b>商家执行操作时，后端首先调用 WeBASE-Front 接口将数据上链；交易成功后，同步将记录与 TxHash 写入 MySQL，兼顾防篡改与查询效率。</p>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" size="large" @click="logDialogVisible = false" class="close-btn">
            我知道了
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog 
      :title="currentRoleName + ' 身份验证'" 
      v-model="dialogVisible" 
      width="420px" 
      center 
      class="custom-dialog"
      :show-close="true"
    >
      <div class="dialog-body">
        <p class="dialog-tips">请粘贴您在 WeBASE 中对应的链上地址</p>
        
        <el-form label-position="top">
          <el-form-item label="当前身份">
            <el-input v-model="loginForm.name" readonly size="large">
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>
          
          <el-form-item label="区块链地址">
            <el-input 
              v-model="loginForm.address" 
              placeholder="请输入 0x 开头的地址" 
              size="large"
              clearable
            >
              <template #prefix><el-icon><Connection /></el-icon></template>
            </el-input>
            
            <div class="quick-address-box">
              <span class="quick-label">快捷填入：</span>
              <span v-if="loginForm.role === 1" class="address-text color-green" @click="copyAddress('0xc8d95843e76348ac85204e65b9230a9bfb6a9b55')">
                0xc8d95...6a9b55 <el-icon><CopyDocument /></el-icon>
              </span>
              <span v-if="loginForm.role === 2" class="address-text color-orange" @click="copyAddress('0x28255b6c2edf079cfa95aac26fe7db88a4923c33')">
                0x28255...923c33 <el-icon><CopyDocument /></el-icon>
              </span>
              <span v-if="loginForm.role === 3" class="address-text color-red" @click="copyAddress('0x188aded2b4a211bae2b907125cd78d8860fa7e13')">
                0x188ad...fa7e13 <el-icon><CopyDocument /></el-icon>
              </span>
            </div>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false" size="large">取消</el-button>
          <el-button type="primary" @click="handleLogin" :loading="loading" size="large" class="login-btn">
            验证并登录
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { User, Connection, CopyDocument, Search, InfoFilled } from '@element-plus/icons-vue'

const router = useRouter()
const dialogVisible = ref(false)
const logDialogVisible = ref(false)
const loading = ref(false)
const currentRoleName = ref('')
const loginForm = reactive({ name: '', address: '', role: null })

const poems = [
  { title: "乌衣巷", author: "唐 · 刘禹锡", lines: ["朱雀桥边野草花，乌衣巷口夕阳斜。", "旧时王谢堂前燕，飞入寻常百姓家。"] },
  { title: "题都城南庄", author: "唐 · 崔护", lines: ["去年今日此门中，人面桃花相映红。", "人面不知何处去，桃花依旧笑春风。"] },
  { title: "凉州词", author: "唐 · 王翰", lines: ["葡萄美酒夜光杯，欲饮琵琶马上催。", "醉卧沙场君莫笑，古来征战几人回？"] },
  { title: "村居", author: "清 · 高鼎", lines: ["草长莺飞二月天，拂堤杨柳醉春烟。", "儿童散学归来早，忙趁东风放纸鸢。"] },
  { title: "春日", author: "宋 · 朱熹", lines: ["胜日寻芳泗水滨，无边光景一时新。", "等闲识得东风面，万紫千红总是春。"] },
  { title: "饮湖上初晴后雨", author: "宋 · 苏轼", lines: ["水光潋滟晴方好，山色空蒙雨亦奇。", "欲把西湖比西子，淡妆浓抹总相宜。"] },
  { title: "登鹳雀楼", author: "唐 · 王之涣", lines: ["白日依山尽，黄河入海流。", "欲穷千里目，更上一层楼。"] },
  { title: "题西林壁", author: "宋 · 苏轼", lines: ["横看成岭侧成峰，远近高低各不同。", "不识庐山真面目，只缘身在此山中。"] },
  { title: "观书有感·其一", author: "宋 · 朱熹", lines: ["半亩方塘一鉴开，天光云影共徘徊。", "问渠那得清如许？为有源头活水来。"] },
  { title: "枫桥夜泊", author: "唐 · 张继", lines: ["月落乌啼霜满天，江枫渔火对愁眠。", "姑苏城外寒山寺，夜半钟声到客船。"] },
  { title: "梅花", author: "宋 · 王安石", lines: ["墙角数枝梅，凌寒独自开。", "遥知不是雪，为有暗香来。"] },
  { title: "冬夜读书示子聿", author: "宋 · 陆游", lines: ["古人学问无遗力，少壮工夫老始成。", "纸上得来终觉浅，绝知此事要躬行。"] }
]

const currentPoemIndex = ref(0)
const currentPoem = computed(() => poems[currentPoemIndex.value])
let poemTimer = null

onMounted(() => {
  poemTimer = setInterval(() => {
    currentPoemIndex.value = (currentPoemIndex.value + 1) % poems.length
  }, 10000)
})

onUnmounted(() => {
  if (poemTimer) clearInterval(poemTimer)
})

const goToConsumer = () => {
  router.push('/consumer')
}

const openLogin = (defaultName, role) => {
  currentRoleName.value = role === 1 ? '供应商' : role === 2 ? '中间商' : '销售商'
  loginForm.name = defaultName
  loginForm.address = ''
  loginForm.role = role
  dialogVisible.value = true
}

const copyAddress = (addr) => {
  if (navigator.clipboard && window.isSecureContext) {
    navigator.clipboard.writeText(addr).then(() => {
      ElMessage.success('完整地址已复制')
    }).catch(() => {
      fallbackCopy(addr)
    })
  } else {
    fallbackCopy(addr)
  }
}

const fallbackCopy = (text) => {
  const textArea = document.createElement("textarea")
  textArea.value = text
  textArea.style.position = "fixed"
  document.body.appendChild(textArea)
  textArea.focus()
  textArea.select()
  try {
    document.execCommand('copy')
    ElMessage.success('完整地址已复制')
  } catch (err) {
    ElMessage.error('复制失败，请手动输入')
  }
  document.body.removeChild(textArea)
}

const handleLogin = async () => {
  if (!loginForm.address.startsWith('0x')) {
    return ElMessage.error('请输入正确的 0x 开头的地址')
  }
  loading.value = true
  try {
    const res = await axios.post('/api/auth/login', loginForm)
    if (res.data.code === 200) {
      localStorage.setItem('token', res.data.data.token)
      localStorage.setItem('user', JSON.stringify(res.data.data.user))
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } else {
      ElMessage.error(res.data.msg || '登录失败，地址与角色不匹配')
    }
  } catch (error) {
    ElMessage.error('网络异常，请检查 Java 后端是否启动')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background-color: #ffffff;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 6vh; 
  padding-bottom: 20px;
  background-image: radial-gradient(#f1f1f1 1px, transparent 1px);
  background-size: 20px 20px;
  box-sizing: border-box;
  position: relative;
  overflow-x: hidden;
}

.header { display: flex; align-items: center; margin-bottom: 20px; }
.platform-logo { width: 48px; height: 48px; margin-right: 16px; object-fit: contain; }
.header h1 { color: #2c3e50; font-size: 36px; font-weight: 600; letter-spacing: 2px; margin: 0; }

.consumer-section { margin-bottom: 45px; }

.consumer-btn {
  display: flex; align-items: center; gap: 12px; background-color: #ffffff; padding: 14px 40px; border-radius: 50px; font-size: 16px; font-weight: bold;
  cursor: pointer; border: 2px solid #409EFF;
  animation: floatAndGlow 3s ease-in-out infinite;
  transition: all 0.3s ease;
}
.consumer-btn:hover { background-color: #ecf5ff; transform: translateY(-5px) scale(1.02); animation-play-state: paused; box-shadow: 0 10px 25px rgba(64, 158, 255, 0.4); }
.consumer-icon { font-size: 20px; }

@keyframes floatAndGlow {
  0% { transform: translateY(0); border-color: #409EFF; color: #409EFF; box-shadow: 0 4px 15px rgba(64, 158, 255, 0.1); }
  50% { transform: translateY(-10px); border-color: #00d2ff; color: #00d2ff; box-shadow: 0 15px 30px rgba(0, 210, 255, 0.3); }
  100% { transform: translateY(0); border-color: #409EFF; color: #409EFF; box-shadow: 0 4px 15px rgba(64, 158, 255, 0.1); }
}

.role-cards { display: flex; gap: 40px; justify-content: center; width: 100%; max-width: 1100px; margin-bottom: 40px; }
.card {
  width: 300px; height: 320px; background: #ffffff; border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08); display: flex; flex-direction: column;
  align-items: center; justify-content: center; cursor: pointer;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  border: 2px solid transparent; position: relative; overflow: hidden;
}
.card-content { display: flex; flex-direction: column; align-items: center; }
.role-avatar { width: 100px; height: 100px; object-fit: contain; margin-bottom: 20px; transition: transform 0.3s; }
.role-title { font-size: 24px; font-weight: bold; color: #333; }
.card-action { position: absolute; bottom: 30px; font-size: 14px; color: #999; padding: 8px 24px; border-radius: 30px; background: #f5f7fa; transition: all 0.3s; }

.supplier-card:hover { border-color: #67C23A; transform: translateY(-10px); box-shadow: 0 15px 35px rgba(103, 194, 58, 0.2); }
.supplier-card:hover .card-action { background: #67C23A; color: #fff; }
.distributor-card:hover { border-color: #E6A23C; transform: translateY(-10px); box-shadow: 0 15px 35px rgba(230, 162, 60, 0.2); }
.distributor-card:hover .card-action { background: #E6A23C; color: #fff; }
.retailer-card:hover { border-color: #F56C6C; transform: translateY(-10px); box-shadow: 0 15px 35px rgba(245, 108, 108, 0.2); }
.retailer-card:hover .card-action { background: #F56C6C; color: #fff; }

.poem-carousel-section {
  width: 100%;
  max-width: 900px; 
  height: 160px;    
  margin-bottom: 30px;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}

.poem-display {
  text-align: center;
  background: linear-gradient(135deg, #ffffff 0%, #fcfcfc 100%);
  border: 1px solid #ebeef5;
  border-radius: 16px;
  padding: 20px 50px;
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.05);
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  box-sizing: border-box;
}

.quote-mark { position: absolute; font-family: "Georgia", serif; font-size: 60px; color: #e4e7ed; line-height: 1; z-index: 0; }
.left-quote { top: 15px; left: 25px; }
.right-quote { bottom: -10px; right: 25px; }

.poem-header {
  display: flex;
  justify-content: center;
  align-items: baseline;
  gap: 15px;
  margin-bottom: 12px;
  border-bottom: 1px dashed #dcdfe6;
  padding-bottom: 8px;
  position: relative;
  z-index: 1;
}

.poem-header h2 {
  margin: 0;
  font-family: "STSong", "SimSun", serif;
  font-size: 22px; 
  color: #2c3e50;
  font-weight: bold;
  letter-spacing: 2px;
}

.poem-header .author {
  font-family: "STSong", "SimSun", serif;
  font-size: 15px;
  color: #909399;
}

.poem-content {
  position: relative;
  z-index: 1;
}

.poem-content p {
  margin: 8px 0;
  font-family: "STSong", "SimSun", "宋体", serif;
  font-size: 18px; 
  color: #303133;
  line-height: 1.6;
  letter-spacing: 2px;
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.6s ease, transform 0.6s ease; }
.fade-enter-from { opacity: 0; transform: translateY(-10px); }
.fade-leave-to { opacity: 0; transform: translateY(10px); }

.floating-ball-container {
  position: fixed; bottom: 40px; right: 40px; display: flex; flex-direction: column; align-items: center;
  gap: 12px; cursor: pointer; z-index: 999;
}
.hover-text {
  background: #2c3e50; color: #fff; padding: 6px 18px; border-radius: 20px; font-size: 16px; font-weight: bold;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15); position: relative; animation: textFloat 2s infinite ease-in-out;
}
.triangle {
  position: absolute; bottom: -6px; left: 50%; transform: translateX(-50%);
  border-left: 6px solid transparent; border-right: 6px solid transparent; border-top: 6px solid #2c3e50;
}
.ball {
  width: 76px; height: 76px; border-radius: 50%; display: flex; justify-content: center; align-items: center; color: #ffffff;
  animation: colorGlow 3s infinite alternate ease-in-out; transition: all 0.3s;
}
.rolling-icon { font-size: 38px; animation: roll 4s linear infinite; }

.floating-ball-container:hover .ball {
  transform: scale(1.1) rotate(15deg); animation-play-state: paused; background: #409EFF; box-shadow: 0 0 30px rgba(64, 158, 255, 0.8);
}
.floating-ball-container:hover .rolling-icon { animation-play-state: paused; }
.floating-ball-container:hover .hover-text { animation: none; background: #409EFF; }
.floating-ball-container:hover .triangle { border-top-color: #409EFF; }

@keyframes colorGlow {
  0% { background: #409EFF; box-shadow: 0 0 15px rgba(64, 158, 255, 0.5), inset 0 0 10px rgba(255,255,255,0.3); border: 2px solid #409EFF; }
  100% { background: #F56C6C; box-shadow: 0 0 25px rgba(245, 108, 108, 0.8), inset 0 0 10px rgba(255,255,255,0.3); border: 2px solid #F56C6C; }
}
@keyframes roll { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
@keyframes textFloat { 0% { transform: translateY(0); } 50% { transform: translateY(-5px); } 100% { transform: translateY(0); } }

.large-text-dialog :deep(.el-dialog) { border-radius: 16px; box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2); margin-top: 3vh !important; }
.large-text-dialog :deep(.el-dialog__header) { text-align: center; font-size: 22px; font-weight: bold; color: #2c3e50; padding: 15px 20px 10px; border-bottom: 1px solid #f0f0f0; margin-right: 0; }
.large-text-dialog :deep(.el-dialog__footer) { padding: 10px 20px 20px; }
.text-content-wrapper { padding: 15px 35px; background-color: #fafbfc; }
.text-section { margin-bottom: 15px; }
.text-section:last-child { margin-bottom: 0; }
.text-section h3 { font-size: 16px; color: #303133; margin-bottom: 10px; text-align: center; font-weight: bold; }
.text-section p { font-size: 14px; line-height: 1.6; color: #4a4a4a; margin-bottom: 8px; text-align: justify; }
.text-section b { color: #2c3e50; font-weight: 600; }
.text-section code { background: #eef1f6; color: #d63200; padding: 2px 6px; border-radius: 4px; font-family: monospace; font-size: 13px; }
.close-btn { width: 150px; border-radius: 20px; background: linear-gradient(135deg, #409EFF 0%, #2979ff 100%); border: none; }

.login-footer {
  width: 100%; display: flex; flex-direction: column; align-items: center; gap: 12px; margin-top: auto;
}
.footer-line {
  width: 80%; max-width: 1000px; height: 1px; background: linear-gradient(90deg, transparent, #e4e7ed, transparent);
}
.footer-content {
  font-size: 13px; color: #909399; display: flex; align-items: center; gap: 12px; letter-spacing: 0.5px;
}
.footer-content .divider { color: #dcdfe6; }
.footer-content .tech { font-family: monospace; color: #b0b5bd; }

:deep(.custom-dialog) { border-radius: 16px; box-shadow: 0 20px 50px rgba(0, 0, 0, 0.15); }
:deep(.custom-dialog .el-dialog__header) { text-align: center; }
.dialog-tips { color: #888; font-size: 14px; margin-bottom: 20px; text-align: center; }
:deep(.el-input__wrapper) { border-radius: 10px; box-shadow: 0 0 0 1px #dcdfe6 inset; padding: 8px 15px; }
.quick-address-box { margin-top: 12px; background-color: #f8f9fa; padding: 10px 12px; border-radius: 8px; font-size: 13px; display: flex; align-items: center; justify-content: space-between; }
.address-text { font-family: monospace; font-weight: bold; cursor: pointer; display: flex; align-items: center; gap: 4px; }
.color-green { color: #67C23A; }
.color-orange { color: #E6A23C; }
.color-red { color: #F56C6C; }
.login-btn { background: linear-gradient(135deg, #409EFF 0%, #2979ff 100%); border: none; }

@media screen and (max-width: 768px) {
  .login-page { padding-top: 5vh; padding-bottom: 15px; }
  .header h1 { font-size: 24px; }
  .platform-logo { width: 36px; height: 36px; }
  
  .consumer-btn { font-size: 13px; padding: 10px 20px; }
  
  .role-cards { flex-direction: column; align-items: center; gap: 15px; padding: 0 20px; margin-bottom: 30px; }
  .card { width: 100%; max-width: 320px; height: 180px; }
  .role-avatar { width: 60px; height: 60px; margin-bottom: 10px; }
  .role-title { font-size: 20px; }
  .card-action { bottom: 15px; }
  
  .poem-carousel-section { height: 130px; margin-bottom: 20px; max-width: 90%; }
  .poem-display { padding: 15px 20px; }
  .poem-header h2 { font-size: 18px; }
  .poem-content p { font-size: 15px; margin: 4px 0; }
  .quote-mark { display: none; }

  .floating-ball-container { bottom: 20px; right: 20px; gap: 8px; }
  .ball { width: 60px; height: 60px; }
  .rolling-icon { font-size: 30px; }
  .hover-text { font-size: 14px; padding: 4px 12px; }

  .large-text-dialog :deep(.el-dialog) { width: 95% !important; margin-top: 2vh !important; }
  .text-content-wrapper { padding: 10px 15px; }
  .text-section h3 { font-size: 15px; margin-bottom: 8px;}
  .text-section p { font-size: 13px; line-height: 1.5; margin-bottom: 6px; }

  .footer-content { flex-direction: column; gap: 5px; font-size: 12px; }
  .footer-content .divider { display: none; }
}
</style>