<script setup>
import { onMounted, reactive, ref } from 'vue'
import {
  createUserApi,
  deleteUserApi,
  listUsersApi,
  resetUserPasswordApi,
  updateUserProfileApi,
  updateUserRoleApi,
  updateUserStatusApi,
} from '../../api/adminUsers'

const loading = ref(false)
const errorMsg = ref('')

const query = reactive({
  keyword: '',
  roleCode: '',
  status: '',
  page: 1,
  size: 10,
})

const data = reactive({
  total: 0,
  items: [],
})

const createOpen = ref(false)
const createLoading = ref(false)
const createError = ref('')
const createForm = reactive({
  username: '',
  password: '',
  realName: '',
  roleCode: 'STUDENT',
  phone: '',
  email: '',
  status: 1,
})

const editOpen = ref(false)
const editLoading = ref(false)
const editError = ref('')
const editTarget = ref(null)
const editForm = reactive({
  realName: '',
  phone: '',
  email: '',
  roleCode: 'STUDENT',
  status: 1,
})

function normalizeError(err) {
  const msg =
    err?.response?.data?.message ||
    err?.response?.data?.error ||
    err?.message ||
    '请求失败，请稍后重试'
  return String(msg)
}

async function fetchList() {
  loading.value = true
  errorMsg.value = ''
  try {
    const params = {
      page: query.page,
      size: query.size,
    }
    if (query.keyword?.trim()) params.keyword = query.keyword.trim()
    if (query.roleCode) params.roleCode = query.roleCode
    if (query.status !== '') params.status = Number(query.status)

    const resp = await listUsersApi(params)
    data.total = Number(resp?.total || 0)
    data.items = Array.isArray(resp?.items) ? resp.items : []
  } catch (e) {
    errorMsg.value = normalizeError(e)
  } finally {
    loading.value = false
  }
}

function onSearch() {
  query.page = 1
  fetchList()
}

function totalPages() {
  return Math.max(1, Math.ceil((data.total || 0) / query.size))
}

function gotoPage(p) {
  const tp = totalPages()
  const next = Math.min(tp, Math.max(1, p))
  if (next === query.page) return
  query.page = next
  fetchList()
}

async function toggleStatus(u) {
  const next = u.status === 1 ? 0 : 1
  const ok = window.confirm(`确定要将用户「${u.username}」${next === 1 ? '启用' : '禁用'}吗？`)
  if (!ok) return
  try {
    await updateUserStatusApi(u.id, next)
    await fetchList()
  } catch (e) {
    window.alert(normalizeError(e))
  }
}

async function resetPwd(u) {
  const ok = window.confirm(`确定要重置用户「${u.username}」的密码吗？（默认 123456）`)
  if (!ok) return
  try {
    await resetUserPasswordApi(u.id)
    window.alert('已重置为 123456')
  } catch (e) {
    window.alert(normalizeError(e))
  }
}

async function removeUser(u) {
  const ok = window.confirm(`确定要删除用户「${u.username}」吗？该操作不可恢复。`)
  if (!ok) return
  try {
    await deleteUserApi(u.id)
    await fetchList()
  } catch (e) {
    window.alert(normalizeError(e))
  }
}

function openCreate() {
  createError.value = ''
  createForm.username = ''
  createForm.password = ''
  createForm.realName = ''
  createForm.roleCode = 'STUDENT'
  createForm.phone = ''
  createForm.email = ''
  createForm.status = 1
  createOpen.value = true
}

function closeCreate() {
  if (createLoading.value) return
  createOpen.value = false
}

async function submitCreate() {
  if (createLoading.value) return
  createError.value = ''
  const username = createForm.username?.trim()
  if (!username) {
    createError.value = '请输入用户名'
    return
  }
  if (!createForm.roleCode) {
    createError.value = '请选择角色'
    return
  }

  createLoading.value = true
  try {
    const payload = {
      username,
      roleCode: createForm.roleCode,
      status: Number(createForm.status) === 1 ? 1 : 0,
    }
    if (createForm.password?.trim()) payload.password = createForm.password
    if (createForm.realName?.trim()) payload.realName = createForm.realName.trim()
    if (createForm.phone?.trim()) payload.phone = createForm.phone.trim()
    if (createForm.email?.trim()) payload.email = createForm.email.trim()

    await createUserApi(payload)
    createOpen.value = false
    await fetchList()
  } catch (e) {
    createError.value = normalizeError(e)
  } finally {
    createLoading.value = false
  }
}

function openEdit(u) {
  editError.value = ''
  editTarget.value = u
  editForm.realName = u?.realName || ''
  editForm.phone = u?.phone || ''
  editForm.email = u?.email || ''
  editForm.roleCode = u?.roleCode || 'STUDENT'
  editForm.status = u?.status === 0 ? 0 : 1
  editOpen.value = true
}

function closeEdit() {
  if (editLoading.value) return
  editOpen.value = false
  editTarget.value = null
}

async function submitEdit() {
  if (editLoading.value) return
  editError.value = ''
  const u = editTarget.value
  if (!u?.id) return

  editLoading.value = true
  try {
    const tasks = []

    // profile
    const profilePayload = {
      realName: editForm.realName?.trim() || null,
      phone: editForm.phone?.trim() || null,
      email: editForm.email?.trim() || null,
    }
    tasks.push(updateUserProfileApi(u.id, profilePayload))

    // role/status (避免无意义请求：仅在变化时调用)
    if (String(editForm.roleCode || '').trim() && editForm.roleCode !== u.roleCode) {
      tasks.push(updateUserRoleApi(u.id, editForm.roleCode))
    }
    if ((Number(editForm.status) === 1 ? 1 : 0) !== (u.status === 0 ? 0 : 1)) {
      tasks.push(updateUserStatusApi(u.id, Number(editForm.status) === 1 ? 1 : 0))
    }

    await Promise.all(tasks)
    editOpen.value = false
    editTarget.value = null
    await fetchList()
  } catch (e) {
    editError.value = normalizeError(e)
  } finally {
    editLoading.value = false
  }
}

onMounted(fetchList)
</script>

<template>
  <div class="panel">
    <div class="panel__header">
      <div class="panel__title">用户管理</div>
      <div class="panel__headerRight">
        <div class="panel__sub">支持搜索、筛选与分页</div>
        <button class="btn btn--sm" :disabled="loading" @click="openCreate">新增用户</button>
      </div>
    </div>

    <div class="filters">
      <input v-model="query.keyword" class="input input--sm" placeholder="搜索用户名/姓名/手机号/邮箱" @keyup.enter="onSearch" />

      <select v-model="query.roleCode" class="input input--sm">
        <option value="">全部角色</option>
        <option value="ADMIN">管理员</option>
        <option value="TEACHER">教师</option>
        <option value="STUDENT">学生</option>
      </select>

      <select v-model="query.status" class="input input--sm">
        <option value="">全部状态</option>
        <option value="1">启用</option>
        <option value="0">禁用</option>
      </select>

      <select v-model.number="query.size" class="input input--sm" @change="onSearch">
        <option :value="10">10/页</option>
        <option :value="20">20/页</option>
        <option :value="50">50/页</option>
      </select>

      <button class="btn btn--sm" :disabled="loading" @click="onSearch">查询</button>
    </div>

    <div v-if="errorMsg" class="alert" role="alert">{{ errorMsg }}</div>

    <div class="tableWrap">
      <table class="table">
        <thead>
          <tr>
            <th style="width: 86px">ID</th>
            <th style="width: 140px">用户名</th>
            <th style="width: 140px">姓名</th>
            <th style="width: 110px">角色</th>
            <th style="width: 140px">手机号</th>
            <th>邮箱</th>
            <th style="width: 90px">状态</th>
            <th style="width: 210px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="!loading && data.items.length === 0">
            <td colspan="8" class="table__empty">暂无数据</td>
          </tr>
          <tr v-for="u in data.items" :key="u.id">
            <td>{{ u.id }}</td>
            <td>{{ u.username }}</td>
            <td>{{ u.realName || '-' }}</td>
            <td>
              <span class="tag" :class="`tag--${String(u.roleCode || '').toLowerCase()}`">{{ u.roleCode }}</span>
            </td>
            <td>{{ u.phone || '-' }}</td>
            <td>{{ u.email || '-' }}</td>
            <td>
              <span class="tag" :class="u.status === 1 ? 'tag--ok' : 'tag--off'">
                {{ u.status === 1 ? '启用' : '禁用' }}
              </span>
            </td>
            <td>
              <div class="ops">
                <button class="btn btn--ghost btn--xs" :disabled="loading" @click="openEdit(u)">编辑</button>
                <button class="btn btn--ghost btn--xs" :disabled="loading" @click="toggleStatus(u)">
                  {{ u.status === 1 ? '禁用' : '启用' }}
                </button>
                <button class="btn btn--ghost btn--xs" :disabled="loading" @click="resetPwd(u)">重置密码</button>
                <button class="btn btn--danger btn--xs" :disabled="loading" @click="removeUser(u)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="pager">
      <div class="pager__left">共 {{ data.total }} 条</div>
      <div class="pager__right">
        <button class="btn btn--ghost btn--sm" :disabled="loading || query.page <= 1" @click="gotoPage(query.page - 1)">
          上一页
        </button>
        <div class="pager__num">第 {{ query.page }} / {{ totalPages() }} 页</div>
        <button
          class="btn btn--ghost btn--sm"
          :disabled="loading || query.page >= totalPages()"
          @click="gotoPage(query.page + 1)"
        >
          下一页
        </button>
      </div>
    </div>

    <div v-if="createOpen" class="modal" @click.self="closeCreate">
      <div class="modal__card">
        <div class="modal__title">新增用户</div>
        <div class="modal__sub">密码不填则默认 123456</div>

        <div class="formGrid">
          <label class="field">
            <span class="field__label">用户名 *</span>
            <input v-model="createForm.username" class="input input--sm" placeholder="必填" :disabled="createLoading" />
          </label>

          <label class="field">
            <span class="field__label">密码</span>
            <input
              v-model="createForm.password"
              class="input input--sm"
              type="password"
              placeholder="可选，默认 123456"
              :disabled="createLoading"
            />
          </label>

          <label class="field">
            <span class="field__label">姓名</span>
            <input v-model="createForm.realName" class="input input--sm" placeholder="可选" :disabled="createLoading" />
          </label>

          <label class="field">
            <span class="field__label">角色 *</span>
            <select v-model="createForm.roleCode" class="input input--sm" :disabled="createLoading">
              <option value="ADMIN">管理员</option>
              <option value="TEACHER">教师</option>
              <option value="STUDENT">学生</option>
            </select>
          </label>

          <label class="field">
            <span class="field__label">手机号</span>
            <input v-model="createForm.phone" class="input input--sm" placeholder="可选" :disabled="createLoading" />
          </label>

          <label class="field">
            <span class="field__label">邮箱</span>
            <input v-model="createForm.email" class="input input--sm" placeholder="可选" :disabled="createLoading" />
          </label>

          <label class="field">
            <span class="field__label">状态</span>
            <select v-model.number="createForm.status" class="input input--sm" :disabled="createLoading">
              <option :value="1">启用</option>
              <option :value="0">禁用</option>
            </select>
          </label>
        </div>

        <div v-if="createError" class="alert" role="alert" style="margin-top: 10px">
          {{ createError }}
        </div>

        <div class="modal__actions">
          <button class="btn btn--ghost btn--sm" :disabled="createLoading" @click="closeCreate">取消</button>
          <button class="btn btn--sm" :disabled="createLoading" @click="submitCreate">
            <span v-if="createLoading">提交中...</span>
            <span v-else>创建</span>
          </button>
        </div>
      </div>
    </div>

    <div v-if="editOpen" class="modal" @click.self="closeEdit">
      <div class="modal__card">
        <div class="modal__title">编辑用户</div>
        <div class="modal__sub">
          {{ editTarget?.username || '' }}（ID: {{ editTarget?.id || '-' }}）
        </div>

        <div class="formGrid">
          <label class="field">
            <span class="field__label">姓名</span>
            <input v-model="editForm.realName" class="input input--sm" placeholder="可选" :disabled="editLoading" />
          </label>

          <label class="field">
            <span class="field__label">手机号</span>
            <input v-model="editForm.phone" class="input input--sm" placeholder="可选" :disabled="editLoading" />
          </label>

          <label class="field">
            <span class="field__label">邮箱</span>
            <input v-model="editForm.email" class="input input--sm" placeholder="可选" :disabled="editLoading" />
          </label>

          <label class="field">
            <span class="field__label">角色</span>
            <select v-model="editForm.roleCode" class="input input--sm" :disabled="editLoading">
              <option value="ADMIN">管理员</option>
              <option value="TEACHER">教师</option>
              <option value="STUDENT">学生</option>
            </select>
          </label>

          <label class="field">
            <span class="field__label">状态</span>
            <select v-model.number="editForm.status" class="input input--sm" :disabled="editLoading">
              <option :value="1">启用</option>
              <option :value="0">禁用</option>
            </select>
          </label>
        </div>

        <div v-if="editError" class="alert" role="alert" style="margin-top: 10px">
          {{ editError }}
        </div>

        <div class="modal__actions">
          <button class="btn btn--ghost btn--sm" :disabled="editLoading" @click="closeEdit">取消</button>
          <button class="btn btn--sm" :disabled="editLoading" @click="submitEdit">
            <span v-if="editLoading">保存中...</span>
            <span v-else>保存</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

