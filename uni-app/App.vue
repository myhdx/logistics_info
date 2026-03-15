<script>
export default {
  onLaunch: function() {
    console.log('App Launch');
    // 检查登录状态
    const token = uni.getStorageSync('token');
    if (!token) {
      // 未登录，跳转到登录页（如果是钉钉免密登录，这里可以自动获取钉钉用户信息）
    }
  },
  onShow: function() {
    console.log('App Show');
  },
  onHide: function() {
    console.log('App Hide');
  }
};
</script>

<style>
@import './styles/common.scss';
</style>