const app = getApp();

Page({
  data: {
    token: "",
    profileCount: 0,
    stickerCount: 0
  },

  onShow() {
    const token = app.globalData.token || wx.getStorageSync("token") || "";
    this.setData({ token });
    if (token) {
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
            this.loadStats();
            wx.showToast({ title: "登录成功", icon: "success" });
          },
          fail: () => wx.showToast({ title: "登录失败", icon: "none" })
        });
      },
      fail: () => wx.showToast({ title: "微信登录失败", icon: "none" })
    });
  },

  loadStats() {
    if (!this.data.token) return;
    
    wx.request({
      url: `${app.globalData.apiBase}/api/profiles`,
      method: "GET",
      header: { Authorization: `Bearer ${this.data.token}` },
      success: ({ data, statusCode }) => {
        if (statusCode === 200 && Array.isArray(data)) {
          let totalVehicles = 0;
          data.forEach(p => {
            if (p.vehicles) totalVehicles += p.vehicles.length;
          });
          this.setData({ profileCount: data.length, 'stats.totalVehicles': totalVehicles });
        }
      }
    });

    wx.request({
      url: `${app.globalData.apiBase}/api/stickers`,
      method: "GET",
      header: { Authorization: `Bearer ${this.data.token}` },
      success: ({ data, statusCode }) => {
        if (statusCode === 200 && Array.isArray(data)) {
          this.setData({ 'stats.totalStickers': data.length });
        }
      }
    });
  },

  goToProfiles() {
    wx.switchTab({ url: '/pages/profiles/index' });
  },

  goToCreate() {
    wx.navigateTo({ url: '/pages/profile-create/index' });
  },

  viewHelp() {
    wx.showModal({
      title: "使用帮助",
      content: "1. 添加车主档案（支持多车牌）\n2. 选择档案和车牌\n3. 选择模板或上传自定义图片\n4. 制作并保存挪车贴图\n5. 将二维码贴纸贴于车窗",
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
          this.setData({ token: "" });
          wx.showToast({ title: "已退出", icon: "success" });
        }
      }
    });
  }
});
