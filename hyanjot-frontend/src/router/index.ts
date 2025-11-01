import { createRouter, createWebHistory } from 'vue-router'
import ItemList from '../views/ItemList.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: ItemList,
    },
  ],
})

export default router
