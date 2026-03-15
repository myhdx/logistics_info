<template>
  <view class="page">
    <!-- 顶部背景 -->
    <view class="header-bg">
      <view class="header-content">
        <text class="order-no">{{ order.orderNo }}</text>
        <text class="status-text" :class="order.logisticsNo ? 'sent' : 'pending'">
          {{ order.logisticsNo ? '已发货' : '待发货' }}
        </text>
      </view>
    </view>

    <!-- 发货单信息 -->
    <view class="card info-card">
      <view class="info-row">
        <text class="info-label">客户名称</text>
        <text class="info-value">{{ order.customerName || '-' }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">商品名称</text>
        <text class="info-value">{{ order.productName || '-' }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">数量</text>
        <text class="info-value">{{ order.quantity || '-' }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">金额</text>
        <text class="info-value">¥{{ order.amount || '0' }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">发货日期</text>
        <text class="info-value">{{ order.deliveryDate || '-' }}</text>
      </view>
      <view class="info-row">
        <text class="info-label">收货地址</text>
        <text class="info-value">{{ order.deliveryAddress || '-' }}</text>
      </view>
    </view>

    <!-- 物流信息 -->
    <view class="card" v-if="order.logisticsNo">
      <view class="section-header">
        <view class="blue-dot"></view>
        <text class="section-title">物流信息</text>
      </view>
      
      <view class="logistics-info">
        <view class="logistics-row">
          <text class="logistics-label">物流单号</text>
          <text class="logistics-value">{{ order.logisticsNo }}</text>
        </view>
        <view class="logistics-row">
          <text class="logistics-label">物流公司</text>
          <text class="logistics-value">{{ order.logisticsCompany || '-' }}</text>
        </view>
      </view>
      
      <button class="btn-primary" @click="goToLogistics">查看物流轨迹</button>
    </view>

    <!-- 无物流时显示发货方联系 -->
    <view class="card" v-else>
      <view class="section-header">
        <view class="blue-dot"></view>
        <text class="section-title">发货方联系信息</text>
      </view>
      
      <view class="sender-info">
        <view class="sender-row">
          <text class="sender-label">发货公司</text>
          <text class="sender-value">{{ order.senderName || '-' }}</text>
        </view>
        <view class="sender-row">
          <text class="sender-label">联系电话</text>
          <text class="sender-value">{{ order.senderPhone || '-' }}</text>
        </view>
        <view class="sender-row">
          <text class="sender-label">联系人</text>
          <text class="sender-value">{{ order.senderContact || '-' }}</text>
        </view>
        <view class="sender-row">
          <text class="sender-label">地址</text>
          <text class="sender-value">{{ order.senderAddress || '-' }}</text>
        </view>
      </view>
      
      <button class="btn-primary" v-if="order.senderPhone" @click="callSender">
        📞 一键拨打
      </button>
    </view>
  </view>
</template>

<script>
import request from '../../utils/request.js';

export default {
  data() {
    return {
      orderId: null,
      order: {}
    };
  },
  onLoad(options) {
    this.orderId = options.id;
    this.loadDetail();
  },
  methods: {
    async loadDetail() {
      try {
        const res = await request.getDeliveryDetail(this.orderId);
        this.order = res || {};
      } catch (e) {
        console.error('加载详情失败', e);
      }
    },
    goToLogistics() {
      uni.navigateTo({
        url: `/pages/logistics/detail?logisticsNo=${this.order.logisticsNo}&company=${this.order.logisticsCompany}`
      });
    },
    callSender() {
      if (this.order.senderPhone) {
        uni.makePhoneCall({ phoneNumber: this.order.senderPhone });
      }
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
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .order-no {
    font-size: 18px;
    font-weight: 600;
    color: #FFFFFF;
  }
  
  .status-text {
    font-size: 14px;
    padding: 4px 12px;
    border-radius: 12px;
    
    &.sent {
      background: rgba(255, 255, 255, 0.3);
      color: #FFFFFF;
    }
    
    &.pending {
      background: rgba(255, 255, 255, 0.3);
      color: #FFFFFF;
    }
  }
}

/* 卡片通用 */
.card {
  margin: 16px;
  background: #FFFFFF;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.info-card {
  margin-top: -20px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #F5F5F5;
  
  &:last-child {
    border-bottom: none;
  }
  
  .info-label {
    font-size: 14px;
    color: #999999;
  }
  
  .info-value {
    font-size: 14px;
    color: #333333;
    font-weight: 500;
    max-width: 60%;
    text-align: right;
  }
}

/* 区块标题 */
.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  
  .blue-dot {
    width: 8px;
    height: 8px;
    background: #4A90E2;
    border-radius: 50%;
    margin-right: 8px;
  }
  
  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #333333;
  }
}

/* 物流信息 */
.logistics-info, .sender-info {
  margin-bottom: 16px;
}

.logistics-row, .sender-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  
  .logistics-label, .sender-label {
    font-size: 14px;
    color: #999999;
  }
  
  .logistics-value, .sender-value {
    font-size: 14px;
    color: #333333;
  }
}

/* 主按钮 */
.btn-primary {
  background: linear-gradient(135deg, #4A90E2 0%, #357ABD 100%);
  color: #FFFFFF;
  border-radius: 8px;
  padding: 12px;
  text-align: center;
  font-size: 14px;
  border: none;
}
</style>