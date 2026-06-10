const app = getApp();

Page({
  data: {
    apiBase: app.globalData.apiBase,
    token: "",
    editId: "",  // 如果有值则为编辑模式
    nickname: "",
    plateNo: "",
    phone: "",
    submitting: false
  },

  onLoad(options) {
    const token = app.globalData.token || wx.getStorageSync("token") || "";
    if (!token) {
      wx.showToast({ title: "请先登录", icon: "none" });
      setTimeout(() => wx.navigateBack(), 1500);
      return;
    }
    
    this.setData({
      token,
      editId: options.editId || "",
      nickname: options.nickname ? decodeURIComponent(options.nickname) : "",
      plateNo: options.plateNo ? decodeURIComponent(options.plateNo) : "",
      phone: options.phone || ""
    });
  },

  onInput(event) {
    const field = event.currentTarget.dataset.field;
    this.setData({ [field]: event.detail.value });
  },

  submit() {
    const { nickname, plateNo, phone, editId } = this.data;

    if (!nickname.trim()) {
      wx.showToast({ title: "请输入昵称", icon: "none" });
      return;
    }
    if (!phone.trim() || !/^1[3-9]\d{9}$/.test(phone.trim())) {
      wx.showToast({ title: "请输入正确的手机号", icon: "none" });
      return;
    }

    this.setData({ submitting: true });

    const url = editId
      ? `${this.data.apiBase}/api/profiles/${editId}`
      : `${this.data.apiBase}/api/profiles`;
    const method = editId ? "PUT" : "POST";

    wx.request({
      url,
      method,
      header: {
        Authorization: `Bearer ${this.data.token}`,
        "Content-Type": "application/json"
      },
      data: {
        nickname: nickname.trim(),
        plateNo: plateNo.trim().toUpperCase(),
        phone: phone.trim()
      },
      success: ({ statusCode }) => {
        if (statusCode === 200) {
          wx.showToast({ title: editId ? "更新成功" : "添加成功", icon: "success" });
          setTimeout(() => wx.navigateBack(), 1000);
        } else {
          wx.showToast({ title: "操作失败", icon: "none" });
        }
      },
      fail: () => wx.showToast({ title: "网络错误", icon: "none" }),
      complete: () => this.setData({ submitting: false })
    });
  }
});
