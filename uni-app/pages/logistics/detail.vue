<template>
  <view class="page">
    <!-- 顶部地图区域 -->
    <view class="map-container">
      <view class="map-placeholder">
        <text class="map-text">📍 货物位置</text>
        <text class="map-coord" v-if="currentLocation">{{ currentLocation }}</text>
      </view>
    </view>

    <!-- 物流状态 -->
    <view class="card status-card">
      <view class="status-header">
        <text class="status-title">物流状态</text>
        <text class="status-badge" :class="statusClass">{{ statusText }}</text>
      </view>
      
      <view class="trace-timeline">
        <view 
          class="trace-item" 
          v-for="(item, index) in traceList" 
          :key="index"
          :class="{ active: index === 0 }"
        >
          <view class="trace-dot"></view>
          <view class="trace-content">
            <text class="trace-desc">{{ item.desc }}</text>
            <text class="trace-time">{{ item.time }}</text>
          </view>
        </view>
        
        <view v-if="traceList.length === 0" class="empty-trace">
          <text>暂无物流轨迹</text>
        </view>
      </view>
    </view>

    <!-- 发货方联系信息（查不到时展示） -->
    <view class="card" v-if="showSenderInfo">
      <view class="section-header">
        <view class="blue-dot"></view>
        <text class="section-title">发货方联系信息</text>
      </view>
      
      <view class="sender-info">
        <view class="sender-row">
          <text class="sender-label">发货公司</text>
          <text class="sender-value">{{ senderInfo.senderName }}</text>
        </view>
        <view class="sender-row">
          <text class="sender-label">联系电话</text>
          <text class="sender-value">{{ senderInfo.senderPhone }}</text>
        </view>
        <view class="sender-row">
          <text class="sender-label">联系人</text>
          <text class="sender-value">{{ senderInfo.senderContact }}</text>
        </view>
        <view class="sender-row">
          <text class="sender-label">地址</text>
          <text class="sender-value">{{ senderInfo.senderAddress }}</text>
        </view>
      </view>
      
      <button class="btn-primary" v-if="senderInfo.senderPhone" @click="callSender">
        📞 一键拨打
      </button>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-tip">
      <text>查询中...</text>
    </view>
  </view>
</template>

<script>
import request from '../../utils/request.js';

export default {
  data() {
    return {
      logisticsNo: '',
      company: '',
      loading: true,
      traceList: [],
      currentLocation: '',
      statusText: '运输中',
      statusClass: 'sent',
      showSenderInfo: false,
      senderInfo: {}
    };
  },
  onLoad(options) {
    this.logisticsNo = options.logisticsNo || '';
    this.company = options.company || '';
    this.queryLogistics();
  },
  methods: {
    async queryLogistics() {
      if (!this.logisticsNo) {
        this.loading = false;
        return;
      }
      
      try {
        const res = await request.queryLogistics(this.logisticsNo, this.company);
        this.loading = false;
        
        if (res.fromCache) {
          console.log('从缓存读取');
        }
        
        const data = res.data;
        if (data && data.traces) {
          // 有物流轨迹
          this.traceList = data.traces.map(t => ({
            desc: t.desc || t.content,
            time: t.time || t.ftime
          }));
          this.currentLocation = data.current || '';
          this.statusText = data.state === '2' ? '已签收' : '运输中';
        } else {
          // 无轨迹，显示发货方信息
          this.showSenderInfo = true;
          this.statusText = '待揽收';
          this.statusClass = 'pending';
          await this.loadSenderInfo();
        }
      } catch (e) {
        this.loading = false;
        console.error('查询失败', e);
      }
    },
    async loadSenderInfo() {
      try {
        const res = await request.getSenderInfo(this.logisticsNo);
        if (res.found) {
          this.senderInfo = {
            senderName: res.senderName,
            senderPhone: res.senderPhone,
            senderContact: res.senderContact,
            senderAddress: res.senderAddress
          };
        }
      } catch (e) {
        console.error('获取发货方信息失败', e);
      }
    },
    callSender() {
      if (this.senderInfo.senderPhone) {
        uni.makePhoneCall({ phoneNumber: this.senderInfo.senderPhone });
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

/* 地图区域 */
.map-container {
  height: 200px;
  background: linear-gradient(135deg, #E8F4FD 0%, #4A90E2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  
  .map-placeholder {
    text-align: center;
    color: #FFFFFF;
    
    .map-text {
      font-size: 18px;
      display: block;
      margin-bottom: 8px;
    }
    
    .map-coord {
      font-size: 14px;
      opacity: 0.8;
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

/* 状态卡片 */
.status-card {
  margin-top: -20px;
  
  .status-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    .status-title {
      font-size: 16px;
      font-weight: 600;
      color: #333333;
    }
    
    .status-badge {
      font-size: 12px;
      padding: 4px 12px;
      border-radius: 12px;
      
      &.sent {
        background: #E8F4FD;
        color: #4A90E2;
      }
      
      &.pending {
        background: #FFF3E0;
        color: #FF9800;
      }
    }
  }
}

/* 物流时间线 */
.trace-timeline {
  .trace-item {
    display: flex;
    padding-bottom: 20px;
    position: relative;
    
    &:last-child {
      padding-bottom: 0;
      
      .trace-dot {
        background: #E0E0E0;
      }
      
      &::before {
        display: none;
      }
    }
    
    &::before {
      content: '';
      position: absolute;
      left: 4px;
      top: 16px;
      bottom: 0;
      width: 2px;
      background: #E0E0E0;
    }
    
    .trace-dot {
      width: 10px;
      height: 10px;
      border-radius: 50%;
      background: #4A90E2;
      margin-right: 12px;
      flex-shrink: 0;
      margin-top: 4px;
    }
    
    &.active .trace-dot {
      background: #4A90E2;
      box-shadow: 0 0 0 4px rgba(74, 144, 226, 0.2);
    }
    
    .trace-content {
      flex: 1;
      
      .trace-desc {
        font-size: 14px;
        color: #333333;
        display: block;
        margin-bottom: 4px;
      }
      
      .trace-time {
        font-size: 12px;
        color: #999999;
      }
    }
  }
  
  .empty-trace {
    text-align: center;
    padding: 20px;
    color: #999999;
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

/* 发货方信息 */
.sender-info {
  margin-bottom: 16px;
  
  .sender-row {
    display: flex;
    justify-content: space-between;
    padding: 8px 0;
    
    .sender-label {
      font-size: 14px;
      color: #999999;
    }
    
    .sender-value {
      font-size: 14px;
      color: #333333;
    }
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

/* 加载 */
.loading-tip {
  text-align: center;
  padding: 40px;
  color: #999999;
}
</style>