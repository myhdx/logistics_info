<template>
  <view class="page">
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
      <text class="cancel-btn" @click="goBack">取消</text>
    </view>

    <!-- 列表 -->
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
      
      <view v-if="loading" class="loading-tip">
        <text>加载中...</text>
      </view>
      
      <view v-if="!loading && deliveryList.length === 0" class="empty-tip">
        <text>暂无发货单</text>
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
      deliveryList: [],
      pageNum: 1,
      pageSize: 20,
      loading: false,
      hasMore: true
    };
  },
  onLoad(options) {
    if (options.keyword) {
      this.keyword = options.keyword;
      this.handleSearch();
    } else {
      this.loadList();
    }
  },
  onReachBottom() {
    if (this.hasMore && !this.loading) {
      this.pageNum++;
      this.loadList(true);
    }
  },
  methods: {
    async loadList(isAppend = false) {
      if (this.loading) return;
      this.loading = true;
      
      try {
        const res = await request.getDeliveryList({
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          keyword: this.keyword
        });
        
        const list = res.records || [];
        if (isAppend) {
          this.deliveryList = [...this.deliveryList, ...list];
        } else {
          this.deliveryList = list;
        }
        
        this.hasMore = list.length >= this.pageSize;
      } catch (e) {
        console.error('加载失败', e);
      }
      
      this.loading = false;
    },
    handleSearch() {
      this.pageNum = 1;
      this.deliveryList = [];
      this.loadList();
    },
    goToDetail(id) {
      uni.navigateTo({ url: `/pages/delivery/detail?id=${id}` });
    },
    goBack() {
      uni.navigateBack();
    }
  }
};
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #F5F5F5;
}

/* 搜索栏 */
.search-box {
  background: linear-gradient(135deg, #E8F4FD 0%, #4A90E2 100%);
  padding: 50px 16px 20px;
  display: flex;
  align-items: center;
  
  .search-input-wrap {
    flex: 1;
    background: #FFFFFF;
    border-radius: 12px;
    padding: 10px 16px;
    display: flex;
    align-items: center;
    margin-right: 12px;
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
  
  .cancel-btn {
    font-size: 14px;
    color: #FFFFFF;
  }
}

/* 列表 */
.delivery-list {
  padding: 16px;
  
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
  
  .loading-tip, .empty-tip {
    text-align: center;
    padding: 40px;
    color: #999999;
  }
}
</style>