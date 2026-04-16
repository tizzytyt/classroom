import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import { getRoleCode, isAuthed } from '../utils/auth'
import AdminLayout from '../layouts/AdminLayout.vue'
import UserManagement from '../views/admin/UserManagement.vue'
import Dashboard from '../views/admin/Dashboard.vue'
import TeacherLayout from '../layouts/TeacherLayout.vue'
import TeacherGrades from '../views/teacher/TeacherGrades.vue'
import TeacherProgress from '../views/teacher/TeacherProgress.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/admin' },
    { path: '/login', name: 'login', component: Login, meta: { public: true } },
    {
      path: '/teacher',
      component: TeacherLayout,
      meta: { roles: ['TEACHER'] },
      children: [
        { path: '', redirect: '/teacher/grades' },
        { path: 'grades', name: 'teacherGrades', component: TeacherGrades, meta: { title: '成绩' } },
        { path: 'progress', name: 'teacherProgress', component: TeacherProgress, meta: { title: '学习进度' } },
      ],
    },
    {
      path: '/admin',
      component: AdminLayout,
      children: [
        { path: '', redirect: '/admin/dashboard' },
        { path: 'dashboard', name: 'adminDashboard', component: Dashboard, meta: { title: '系统概览', roles: ['ADMIN'] } },
        { path: 'users', name: 'adminUsers', component: UserManagement, meta: { title: '用户管理', roles: ['ADMIN'] } },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/admin' },
  ],
})

router.beforeEach((to) => {
  if (to.meta?.public) return true
  if (isAuthed()) return true
  return { name: 'login', query: { redirect: to.fullPath } }
})

router.beforeEach((to) => {
  if (to.meta?.public) return true
  const roleCode = getRoleCode()
  const roleMeta = [...to.matched].reverse().find((record) => Array.isArray(record.meta?.roles))
  const allowedRoles = roleMeta?.meta?.roles
  if (!allowedRoles || allowedRoles.length === 0) return true
  if (allowedRoles.includes(roleCode)) return true
  if (roleCode === 'TEACHER') return { name: 'teacherGrades' }
  return { name: 'adminDashboard' }
})

export default router

