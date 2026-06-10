const app = getApp();

Page({
  data: {
    apiBase: app.globalData.apiBase,
    token: "",
    stickerId: "",
    sticker: null,
    loading: true
  },

  onLoad(options) {
    const token = app.globalData.token || wx.getStorageSync("token") || "";
    this.setData({
      token,
      stickerId: options.stickerId || ""
    });
    if (token && this.data.stickerId) {
      this.loadSticker();
    }
  },

  loadSticker() {
    this.setData({ loading: true });
    // 使用全局车辆列表接口找到该贴图
    wx.request({
      url: `${this.data.apiBase}/api/vehicles`,
      method: "GET",
      header: { Authorization: `Bearer ${this.data.token}` },
      success: ({ data, statusCode }) => {
        if (statusCode === 200 && Array.isArray(data)) {
          const sticker = data.find(s => s.id === this.data.stickerId);
          this.setData({ sticker });
        }
      },
      fail: () => wx.showToast({ title: "加载失败", icon: "none" }),
      complete: () => this.setData({ loading: false })
    });
  },

  // 下载贴图到相册
  downloadSticker() {
    const url = this.data.sticker.finalImageUrl;
    if (!url) {
      wx.showToast({ title: "贴图不存在", icon: "none" });
      return;
    }

    wx.showLoading({ title: "下载中..." });
    wx.downloadFile({
      url,
      success: (res) => {
        if (res.statusCode === 200) {
          wx.saveImageToPhotosAlbum({
            filePath: res.tempFilePath,
            success: () => wx.showToast({ title: "已保存到相册", icon: "success" }),
            fail: () => wx.showToast({ title: "保存失败，请授权相册权限", icon: "none" })
          });
        }
      },
      fail: () => wx.showToast({ title: "下载失败", icon: "none" }),
      complete: () => wx.hideLoading()
    });
  },

  // 下载二维码
  downloadQrcode() {
    const url = `${this.data.apiBase}/api/vehicles/${this.data.stickerId}/qrcode`;
    wx.showLoading({ title: "下载中..." });
    wx.downloadFile({
      url,
      success: (res) => {
        if (res.statusCode === 200) {
          wx.saveImageToPhotosAlbum({
            filePath: res.tempFilePath,
            success: () => wx.showToast({ title: "二维码已保存", icon: "success" }),
            fail: () => wx.showToast({ title: "保存失败", icon: "none" })
          });
        }
      },
      fail: () => wx.showToast({ title: "下载失败", icon: "none" }),
      complete: () => wx.hideLoading()
    });
  },

  // 删除贴图
  deleteSticker() {
    wx.showModal({
      title: "确认删除",
      content: "删除后不可恢复，确定吗？",
      confirmColor: "#ff4d4f",
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: "删除中..." });
          wx.request({
            url: `${this.data.apiBase}/api/vehicles/${this.data.stickerId}`,
            method: "DELETE",
            header: { Authorization: `Bearer ${this.data.token}` },
            success: ({ statusCode }) => {
              if (statusCode === 204 || statusCode === 200) {
                wx.showToast({ title: "已删除", icon: "success" });
                setTimeout(() => wx.navigateBack(), 1000);
              } else {
                wx.showToast({ title: "删除失败", icon: "none" });
              }
            },
            fail: () => wx.showToast({ title: "网络错误", icon: "none" }),
            complete: () => wx.hideLoading()
          });
        }
      }
    });
  }
});
