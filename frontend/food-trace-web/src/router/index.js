import { createRouter, createWebHashHistory  } from 'vue-router'
import Login from '../views/Login.vue'
import DataScreen from '../views/DataScreen.vue'
import Dashboard from '../views/Dashboard.vue'
import Consumer from '../views/Consumer.vue' 

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  { path: '/dashboard', component: Dashboard },
  { path: '/consumer', component: Consumer },
  { path: '/screen', component: DataScreen } 
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && to.path !== '/consumer' && to.path !== '/screen' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router