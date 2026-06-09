const app = getApp();

Page({
  data: {
    apiBase: app.globalData.apiBase,
    token: "",
    submitting: false,
    form: {
      ownerName: "",
      plateNo: "",
      phone: "",
      comfortMessage: "您好，给您添麻烦了。车主已开启挪车电话，请点击下方按钮联系车主，感谢您的理解。"
    },
    vehicle: null
  },

  onShow() {
    this.setData({ token: app.globalData.token || wx.getStorageSync("token") || "" });
  },

  login() {
    wx.login({
      success: ({ code }) => {
        wx.request({
          url: `${this.data.apiBase}/api/auth/wechat-login`,
          method: "POST",
          data: { code, nickname: "" },
          success: ({ data }) => {
            app.globalData.token = data.token;
            wx.setStorageSync("token", data.token);
            this.setData({ token: data.token });
          },
          fail: () => wx.showToast({ title: "登录失败", icon: "none" })
        });
      },
      fail: () => wx.showToast({ title: "微信登录失败", icon: "none" })
    });
  },

  onInput(event) {
    const field = event.currentTarget.dataset.field;
    this.setData({ [`form.${field}`]: event.detail.value });
  },

  createVehicle() {
    const { ownerName, phone } = this.data.form;
    if (!ownerName || !/^1[3-9]\d{9}$/.test(phone)) {
      wx.showToast({ title: "请填写称呼和正确手机号", icon: "none" });
      return;
    }

    this.setData({ submitting: true });
    wx.request({
      url: `${this.data.apiBase}/api/vehicles`,
      method: "POST",
      header: { Authorization: `Bearer ${this.data.token}` },
      data: this.data.form,
      success: ({ statusCode, data }) => {
        if (statusCode >= 400) {
          wx.showToast({ title: data.message || "生成失败", icon: "none" });
          return;
        }
        this.setData({ vehicle: data });
      },
      fail: () => wx.showToast({ title: "网络异常", icon: "none" }),
      complete: () => this.setData({ submitting: false })
    });
  },

  previewCallPage() {
    wx.navigateTo({ url: `/pages/call/index?id=${this.data.vehicle.id}` });
  }
});
