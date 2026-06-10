const app = getApp();

Page({
  data: {
    apiBase: app.globalData.apiBase,
    token: "",
    vehicleList: [],
    loading: false
  },

  onShow() {
    const token = app.globalData.token || wx.getStorageSync("token") || "";
    this.setData({ token });
    if (token) {
      // 跳转到车主档案页面
      wx.switchTab({ url: '/pages/profiles/index' });
    }
  },

  login() {
    this.setData({ loading: true });
    wx.login({
      success: ({ code }) => {
        wx.request({
          url: `${this.data.apiBase}/api/auth/wechat-login`,
          method: "POST",
          data: { code, nickname: "" },
          header: { "Content-Type": "application/json" },
          success: ({ data }) => {
            app.globalData.token = data.token;
            wx.setStorageSync("token", data.token);
            this.setData({ token: data.token });
            this.loadVehicleList();
          },
          fail: () => wx.showToast({ title: "登录失败", icon: "none" }),
          complete: () => this.setData({ loading: false })
        });
      },
      fail: () => {
        wx.showToast({ title: "微信登录失败", icon: "none" });
        this.setData({ loading: false });
      }
    });
  },

  loadVehicleList() {
    wx.request({
      url: `${this.data.apiBase}/api/vehicles`,
      method: "GET",
      header: { Authorization: `Bearer ${this.data.token}` },
      success: ({ data }) => {
        this.setData({ vehicleList: data || [] });
      },
      fail: () => wx.showToast({ title: "加载列表失败", icon: "none" })
    });
  },

  previewVehicle(event) {
    const id = event.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/call/index?id=${id}` });
  },

  goToCreate() {
    wx.showToast({ title: "请先选择车主档案", icon: "none" });
    setTimeout(() => {
      wx.switchTab({ url: '/pages/profiles/index' });
    }, 1500);
  },

  deleteVehicle(event) {
    const id = event.currentTarget.dataset.id;
    wx.showModal({
      title: "确认删除",
      content: "确定要删除这条挪车码记录吗？",
      success: (res) => {
        if (res.confirm) {
          wx.request({
            url: `${this.data.apiBase}/api/vehicles/${id}`,
            method: "DELETE",
            header: { Authorization: `Bearer ${this.data.token}` },
            success: () => {
              wx.showToast({ title: "删除成功", icon: "success" });
              this.loadVehicleList();
            },
            fail: () => wx.showToast({ title: "删除失败", icon: "none" })
          });
        }
      }
    });
  },

  // 获取样式名称
  getStyleName(styleId) {
    const styleMap = {
      'modern': '现代简约',
      'classic': '经典商务',
      'cute': '可爱卡通',
      'minimal': '极简风格'
    };
    return styleMap[styleId] || '自定义';
  }
});
