<template>
  <view class="page">
    <!-- 顶部背景 -->
    <view class="header-bg">
      <view class="user-info">
        <image class="avatar" src="../../../static/icons/avatar.png" mode="aspectFill"></image>
        <text class="username">{{ userInfo.name || '请登录' }}</text>
        <text class="user-phone" v-if="userInfo.phone">{{ userInfo.phone }}</text>
      </view>
    </view>

    <!-- 功能列表 -->
    <view class="card menu-card">
      <view class="menu-item" @click="goToHistory">
        <text class="menu-icon">📋</text>
        <text class="menu-text">查询历史</text>
        <text class="menu-arrow">></text>
      </view>
      <view class="menu-item" @click="goToFavorites">
        <text class="menu-icon">⭐</text>
        <text class="menu-text">我的收藏</text>
        <text class="menu-arrow">></text>
      </view>
      <view class="menu-item" @click="goToSettings">
        <text class="menu-icon">⚙️</text>
        <text class="menu-text">设置</text>
        <text class="menu-arrow">></text>
      </view>
    </view>

    <!-- 关于 -->
    <view class="card about-card">
      <view class="menu-item">
        <text class="menu-icon">ℹ️</text>
        <text class="menu-text">关于我们</text>
        <text class="menu-arrow">v1.0.0</text>
      </view>
    </view>

    <!-- 退出登录 -->
    <view class="logout-btn" v-if="isLoggedIn" @click="handleLogout">
      退出登录
    </view>
  </view>
</template>

<script>
import request from '../../utils/request.js';

export default {
  data() {
    return {
      userInfo: {},
      isLoggedIn: false
    };
  },
  onShow() {
    this.checkLogin();
  },
  methods: {
    async checkLogin() {
      const token = uni.getStorageSync('token');
      if (token) {
        this.isLoggedIn = true;
        try {
          const res = await request.getUserInfo();
          this.userInfo = res || {};
        } catch (e) {
          this.isLoggedIn = false;
        }
      }
    },
    goToHistory() {
      uni.navigateTo({ url: '/pages/mine/history' });
    },
    goToFavorites() {
      uni.navigateTo({ url: '/pages/mine/favorites' });
    },
    goToSettings() {
      uni.navigateTo({ url: '/pages/mine/settings' });
    },
    async handleLogout() {
      uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await request.logout();
            } catch (e) {}
            uni.removeStorageSync('token');
            uni.removeStorageSync('userId');
            this.isLoggedIn = false;
            this.userInfo = {};
            uni.showToast({ title: '已退出', icon: 'none' });
          }
        }
      });
    }
  }
};
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #F5F5F5;
}

/* 顶部背景 */
.header-bg {
  background: linear-gradient(135deg, #E8F4FD 0%, #4A90E2 100%);
  padding: 60px 16px 40px;
  
  .user-info {
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .avatar {
      width: 70px;
      height: 70px;
      border-radius: 50%;
      background: #FFFFFF;
      margin-bottom: 12px;
    }
    
    .username {
      font-size: 18px;
      font-weight: 600;
      color: #FFFFFF;
    }
    
    .user-phone {
      font-size: 14px;
      color: rgba(255, 255, 255, 0.8);
      margin-top: 4px;
    }
  }
}

/* 卡片通用 */
.card {
  margin: 16px;
  background: #FFFFFF;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.menu-card {
  padding: 0;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #F5F5F5;
  
  &:last-child {
    border-bottom: none;
  }
  
  .menu-icon {
    font-size: 20px;
    margin-right: 12px;
  }
  
  .menu-text {
    flex: 1;
    font-size: 14px;
    color: #333333;
  }
  
  .menu-arrow {
    font-size: 14px;
    color: #CCCCCC;
  }
}

.about-card {
  padding: 0;
  
  .menu-item {
    .menu-arrow {
      font-size: 12px;
      color: #999999;
    }
  }
}

/* 退出按钮 */
.logout-btn {
  margin: 32px 16px;
  background: #FFFFFF;
  border-radius: 12px;
  padding: 14px;
  text-align: center;
  font-size: 14px;
  color: #FF5722;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
</style>