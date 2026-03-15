/**
 * API 请求封装
 */
const BASE_URL = 'http://localhost:8080/api';

const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token');
    
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
        ...options.header
      },
      success: (res) => {
        if (res.data.code === 200) {
          resolve(res.data.data);
        } else {
          uni.showToast({
            title: res.data.message || '请求失败',
            icon: 'none'
          });
          reject(res.data);
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '网络请求失败',
          icon: 'none'
        });
        reject(err);
      }
    });
  });
};

export default {
  // 认证
  login: (data) => request({ url: '/auth/login', method: 'POST', data }),
  logout: () => request({ url: '/auth/logout', method: 'POST' }),
  getUserInfo: () => request({ url: '/auth/me', method: 'GET' }),
  
  // 发货单
  getDeliveryList: (params) => request({ url: '/delivery/list', method: 'GET', data: params }),
  getDeliveryDetail: (id) => request({ url: `/delivery/${id}`, method: 'GET' }),
  searchDelivery: (keyword) => request({ url: '/delivery/search', method: 'GET', data: { keyword } }),
  
  // 物流
  queryLogistics: (logisticsNo, company) => request({ 
    url: '/logistics/query', 
    method: 'GET', 
    data: { logisticsNo, logisticsCompany: company } 
  }),
  getSenderInfo: (logisticsNo) => request({ 
    url: '/logistics/company', 
    method: 'GET', 
    data: { logisticsNo } 
  }),
  
  // 知识库
  importDocument: (filePath) => request({ url: '/knowledge/import', method: 'POST', data: { file: filePath } }),
  knowledgeChat: (data) => request({ url: '/knowledge/chat', method: 'POST', data }),
  
  // ERP
  queryERPDelivery: (params) => request({ url: '/erp/delivery', method: 'GET', data: params }),
};