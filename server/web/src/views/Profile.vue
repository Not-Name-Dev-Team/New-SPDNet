<template>
  <div class="profile">
    <div class="card">
      <h2 class="card-title">个人中心</h2>

      <div class="user-info">
        <div class="info-item">
          <span class="label">用户名:</span>
          <span class="value">{{ authStore.user?.name }}</span>
        </div>
        <div class="info-item">
          <span class="label">身份:</span>
          <span class="value">{{ authStore.user?.role }}</span>
        </div>
      </div>
    </div>

    <div class="card">
      <h3 class="card-title">修改用户名</h3>

      <div v-if="nameMessage" :class="['alert', nameSuccess ? 'alert-success' : 'alert-error']">
        {{ nameMessage }}
      </div>

      <form @submit.prevent="handleChangeName">
        <div class="form-group">
          <label for="newName">新用户名</label>
          <input
            type="text"
            id="newName"
            v-model="nameForm.newName"
            placeholder="请输入新用户名"
            required
          />
        </div>

        <div class="form-group">
          <label for="namePassword">确认密码</label>
          <input
            type="password"
            id="namePassword"
            v-model="nameForm.password"
            placeholder="请输入密码确认"
            required
          />
        </div>

        <button type="submit" class="btn" :disabled="nameLoading">
          {{ nameLoading ? '修改中...' : '修改用户名' }}
        </button>
      </form>
    </div>

    <div class="card">
      <h3 class="card-title">修改密码</h3>

      <div v-if="pwdMessage" :class="['alert', pwdSuccess ? 'alert-success' : 'alert-error']">
        {{ pwdMessage }}
      </div>

      <form @submit.prevent="handleChangePassword">
        <div class="form-group">
          <label for="oldPassword">原密码</label>
          <input
            type="password"
            id="oldPassword"
            v-model="pwdForm.oldPassword"
            placeholder="请输入原密码"
            required
          />
        </div>

        <div class="form-group">
          <label for="newPassword">新密码</label>
          <input
            type="password"
            id="newPassword"
            v-model="pwdForm.newPassword"
            placeholder="请输入新密码"
            required
          />
        </div>

        <div class="form-group">
          <label for="confirmPassword">确认新密码</label>
          <input
            type="password"
            id="confirmPassword"
            v-model="pwdForm.confirmPassword"
            placeholder="请再次输入新密码"
            required
          />
        </div>

        <button type="submit" class="btn" :disabled="pwdLoading">
          {{ pwdLoading ? '修改中...' : '修改密码' }}
        </button>
      </form>
    </div>

    <div class="card">
      <h3 class="card-title">快捷操作</h3>
      <router-link :to="`/player/${authStore.user?.name}`" class="btn">
        查看我的主页
      </router-link>
      <button class="btn btn-danger" @click="handleLogout" style="margin-left: 1rem;">
        退出登录
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { playerApi } from '../api'
import { authStore } from '../store/auth'

const router = useRouter()

const nameForm = reactive({
  newName: '',
  password: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const nameLoading = ref(false)
const nameMessage = ref('')
const nameSuccess = ref(false)

const pwdLoading = ref(false)
const pwdMessage = ref('')
const pwdSuccess = ref(false)

async function handleChangeName() {
  if (!nameForm.newName.trim()) {
    nameSuccess.value = false
    nameMessage.value = '请输入新用户名'
    return
  }

  nameLoading.value = true
  nameMessage.value = ''

  try {
    const res = await playerApi.changeName({
      currentName: authStore.user.name,
      password: nameForm.password,
      newName: nameForm.newName
    })

    if (res.data.success) {
      nameSuccess.value = true
      nameMessage.value = res.data.message
      authStore.updateUser(res.data.data)
      nameForm.newName = ''
      nameForm.password = ''
    } else {
      nameSuccess.value = false
      nameMessage.value = res.data.message
    }
  } catch (error) {
    nameSuccess.value = false
    nameMessage.value = error.response?.data?.message || '修改失败，请稍后重试'
  } finally {
    nameLoading.value = false
  }
}

async function handleChangePassword() {
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    pwdSuccess.value = false
    pwdMessage.value = '两次输入的新密码不一致'
    return
  }

  pwdLoading.value = true
  pwdMessage.value = ''

  try {
    const res = await playerApi.changePassword({
      name: authStore.user.name,
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })

    if (res.data.success) {
      pwdSuccess.value = true
      pwdMessage.value = res.data.message
      pwdForm.oldPassword = ''
      pwdForm.newPassword = ''
      pwdForm.confirmPassword = ''
    } else {
      pwdSuccess.value = false
      pwdMessage.value = res.data.message
    }
  } catch (error) {
    pwdSuccess.value = false
    pwdMessage.value = error.response?.data?.message || '修改失败，请稍后重试'
  } finally {
    pwdLoading.value = false
  }
}

function handleLogout() {
  authStore.logout()
  router.push('/')
}
</script>

<style scoped>
.profile {
  max-width: 600px;
  margin: 0 auto;
}

.user-info {
  padding: 1rem 0;
}

.info-item {
  display: flex;
  padding: 0.5rem 0;
  border-bottom: 1px solid var(--border-color);
}

.info-item:last-child {
  border-bottom: none;
}

.info-item .label {
  width: 80px;
  color: var(--text-secondary);
}

.info-item .value {
  flex: 1;
  font-weight: 500;
}

.btn-danger {
  background-color: #dc3545;
  border-color: #dc3545;
}

.btn-danger:hover {
  background-color: #c82333;
  border-color: #bd2130;
}
</style>
