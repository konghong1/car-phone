const app = getApp();

Page({
  data: {
    token: "",
    userInfo: null,
    stats: {
      totalVehicles: 0,
      totalCalls: 0
    }
  },

  onShow() {
    const token = app.globalData.token || wx.getStorageSync("token") || "";
    this.setData({ token });
    if (token) {
      this.loadUserInfo();
      this.loadStats();
    }
  },

  login() {
    wx.login({
      success: ({ code }) => {
        wx.request({
          url: `${app.globalData.apiBase}/api/auth/wechat-login`,
          method: "POST",
          data: { code, nickname: "" },
          header: { "Content-Type": "application/json" },
          success: ({ data }) => {
            app.globalData.token = data.token;
            wx.setStorageSync("token", data.token);
            this.setData({ token: data.token });
            this.loadUserInfo();
            this.loadStats();
            wx.showToast({ title: "登录成功", icon: "success" });
          },
          fail: () => wx.showToast({ title: "登录失败", icon: "none" })
        });
      },
      fail: () => wx.showToast({ title: "微信登录失败", icon: "none" })
    });
  },

  loadUserInfo() {
    // 这里可以从后端获取用户信息，暂时使用本地存储
    const userInfo = wx.getStorageSync("userInfo");
    if (userInfo) {
      this.setData({ userInfo });
    }
  },

  loadStats() {
    // 加载统计数据
    if (!this.data.token) {
      return;
    }
    
    wx.request({
      url: `${app.globalData.apiBase}/api/vehicles`,
      method: "GET",
      header: { Authorization: `Bearer ${this.data.token}` },
      success: ({ data, statusCode }) => {
        if (statusCode === 200 && Array.isArray(data)) {
          this.setData({
            'stats.totalVehicles': data.length
          });
        }
      },
      fail: (err) => {
        console.error('加载统计失败:', err);
      }
    });
  },

  goToVehicles() {
    wx.switchTab({ url: '/pages/profiles/index' });
  },

  goToCreate() {
    wx.showToast({ title: "请先选择车主档案", icon: "none" });
    setTimeout(() => {
      wx.switchTab({ url: '/pages/profiles/index' });
    }, 1500);
  },

  viewHelp() {
    wx.showModal({
      title: "使用帮助",
      content: "1. 点击\"新增挪车码\"创建您的挪车二维码\n2. 将二维码打印并贴在车上\n3. 他人扫码后可查看您的联系方式\n4. 可在\"挪车码\"页面管理所有二维码",
      showCancel: false
    });
  },

  contactSupport() {
    wx.makePhoneCall({
      phoneNumber: "400-123-4567",
      fail: () => wx.showToast({ title: "呼叫失败", icon: "none" })
    });
  },

  logout() {
    wx.showModal({
      title: "确认退出",
      content: "确定要退出登录吗？",
      success: (res) => {
        if (res.confirm) {
          app.globalData.token = "";
          wx.removeStorageSync("token");
          this.setData({ 
            token: "",
            userInfo: null,
            'stats.totalVehicles': 0
          });
          wx.showToast({ title: "已退出", icon: "success" });
        }
      }
    });
  }
});
