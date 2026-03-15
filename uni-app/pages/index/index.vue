<template>
  <view class="page">
    <!-- 顶部标题栏 -->
    <view class="header">
      <view class="header-content">
        <text class="header-title">物流查询</text>
        <view class="header-right" @click="goToMine">
          <image class="avatar" src="../../../static/icons/avatar.png" mode="aspectFill"></image>
        </view>
      </view>
    </view>

    <!-- 搜索栏 -->
    <view class="search-box">
      <view class="search-input-wrap">
        <text class="search-icon">🔍</text>
        <input 
          class="search-input" 
          type="text" 
          placeholder="搜索发货单号/客户名/商品" 
          v-model="keyword"
          @confirm="handleSearch"
        />
      </view>
    </view>

    <!-- 功能入口 -->
    <view class="quick-entry">
      <view class="entry-card" @click="scanCode">
        <view class="entry-icon">📦</view>
        <text class="entry-text">扫码查询</text>
      </view>
      <view class="entry-card" @click="goToDeliveryList">
        <view class="entry-icon">📋</view>
        <text class="entry-text">发货单</text>
      </view>
      <view class="entry-card" @click="showVoiceInput">
        <view class="entry-icon">🎤</view>
        <text class="entry-text">语音查询</text>
      </view>
    </view>

    <!-- 最近发货单 -->
    <view class="section">
      <view class="section-header">
        <view class="blue-dot"></view>
        <text class="section-title">最近发货</text>
        <text class="section-more" @click="goToDeliveryList">更多 ></text>
      </view>
      
      <view class="delivery-list">
        <view 
          class="delivery-card" 
          v-for="(item, index) in deliveryList" 
          :key="index"
          @click="goToDetail(item.id)"
        >
          <view class="delivery-top">
            <text class="delivery-no">{{ item.orderNo }}</text>
            <text class="delivery-status" :class="item.logisticsNo ? 'status-sent' : 'status-pending'">
              {{ item.logisticsNo ? '已发货' : '待发货' }}
            </text>
          </view>
          <view class="delivery-info">
            <text class="delivery-customer">{{ item.customerName }}</text>
            <text class="delivery-product ellipsis">{{ item.productName }}</text>
          </view>
          <view class="delivery-bottom">
            <text class="delivery-date">{{ item.deliveryDate }}</text>
            <text class="delivery-address ellipsis">{{ item.deliveryAddress }}</text>
          </view>
        </view>
        
        <view v-if="deliveryList.length === 0" class="empty-tip">
          <text>暂无发货单</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import request from '../../utils/request.js';

export default {
  data() {
    return {
      keyword: '',
      deliveryList: []
    };
  },
  onLoad() {
    this.loadDeliveryList();
  },
  methods: {
    async loadDeliveryList() {
      try {
        const res = await request.getDeliveryList({ pageNum: 1, pageSize: 10 });
        this.deliveryList = res.records || [];
      } catch (e) {
        console.error('加载发货单失败', e);
      }
    },
    handleSearch() {
      if (!this.keyword.trim()) return;
      uni.navigateTo({
        url: `/pages/delivery/search?keyword=${this.keyword}`
      });
    },
    goToMine() {
      uni.switchTab({ url: '/pages/mine/mine' });
    },
    goToDeliveryList() {
      uni.navigateTo({ url: '/pages/delivery/list' });
    },
    goToDetail(id) {
      uni.navigateTo({ url: `/pages/delivery/detail?id=${id}` });
    },
    scanCode() {
      uni.scanCode({
        success: (res) => {
          this.keyword = res.result;
          this.handleSearch();
        }
      });
    },
    showVoiceInput() {
      // 钉钉ASR语音输入
      uni.showToast({ title: '语音功能开发中', icon: 'none' });
    }
  }
};
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #F5F5F5;
}

/* 顶部标题栏 */
.header {
  background: linear-gradient(135deg, #E8F4FD 0%, #4A90E2 100%);
  padding: 40px 16px 20px;
  
  .header-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
  
  .header-title {
    font-size: 20px;
    font-weight: 600;
    color: #FFFFFF;
  }
  
  .avatar {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: #FFFFFF;
  }
}

/* 搜索栏 */
.search-box {
  margin: -30px 16px 16px;
  position: relative;
  z-index: 10;
  
  .search-input-wrap {
    background: #FFFFFF;
    border-radius: 12px;
    padding: 12px 16px;
    display: flex;
    align-items: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }
  
  .search-icon {
    margin-right: 8px;
    font-size: 16px;
  }
  
  .search-input {
    flex: 1;
    font-size: 14px;
    color: #333333;
  }
}

/* 功能入口 */
.quick-entry {
  display: flex;
  padding: 0 12px;
  margin-bottom: 16px;
  
  .entry-card {
    flex: 1;
    background: #FFFFFF;
    border-radius: 12px;
    padding: 16px 8px;
    margin: 0 4px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    
    .entry-icon {
      font-size: 28px;
      margin-bottom: 8px;
    }
    
    .entry-text {
      font-size: 12px;
      color: #333333;
    }
  }
}

/* 列表区域 */
.section {
  padding: 0 16px;
  
  .section-header {
    display: flex;
    align-items: center;
    margin-bottom: 12px;
    
    .blue-dot {
      width: 8px;
      height: 8px;
      background: #4A90E2;
      border-radius: 50%;
      margin-right: 8px;
    }
    
    .section-title {
      flex: 1;
      font-size: 16px;
      font-weight: 600;
      color: #333333;
    }
    
    .section-more {
      font-size: 12px;
      color: #999999;
    }
  }
}

/* 发货单卡片 */
.delivery-list {
  .delivery-card {
    background: #FFFFFF;
    border-radius: 12px;
    padding: 14px;
    margin-bottom: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    
    .delivery-top {
      display: flex;
      justify-content: space-between;
      margin-bottom: 10px;
      
      .delivery-no {
        font-size: 14px;
        font-weight: 600;
        color: #333333;
      }
      
      .delivery-status {
        font-size: 12px;
        padding: 2px 8px;
        border-radius: 4px;
        
        &.status-sent {
          background: #E8F4FD;
          color: #4A90E2;
        }
        
        &.status-pending {
          background: #FFF3E0;
          color: #FF9800;
        }
      }
    }
    
    .delivery-info {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8px;
      
      .delivery-customer {
        font-size: 14px;
        color: #666666;
      }
      
      .delivery-product {
        flex: 1;
        text-align: right;
        font-size: 12px;
        color: #999999;
        margin-left: 12px;
      }
    }
    
    .delivery-bottom {
      display: flex;
      justify-content: space-between;
      font-size: 12px;
      color: #999999;
      
      .delivery-address {
        max-width: 60%;
      }
    }
  }
  
  .empty-tip {
    text-align: center;
    padding: 40px;
    color: #999999;
  }
}
</style>