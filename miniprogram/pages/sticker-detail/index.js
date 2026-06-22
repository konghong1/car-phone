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
    this.setData({ token, stickerId: options.stickerId || "" });
    if (token && this.data.stickerId) {
      this.loadSticker();
    } else {
      this.setData({ loading: false });
    }
  },

  onShow() {
    const token = app.globalData.token || wx.getStorageSync("token") || "";
    if (token && this.data.stickerId) {
      this.setData({ token });
      this.loadSticker();
    }
  },

  loadSticker() {
    this.setData({ loading: true });
    wx.request({
      url: `${this.data.apiBase}/api/stickers/${this.data.stickerId}`,
      method: "GET",
      header: { Authorization: `Bearer ${this.data.token}` },
      success: ({ data, statusCode }) => {
        if (statusCode === 200 && data) {
          this.setData({ sticker: data });
        }
      },
      fail: () => wx.showToast({ title: "加载失败", icon: "none" }),
      complete: () => this.setData({ loading: false })
    });
  },

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
      complete: () => {
        wx.hideLoading();
      }
    });
  },

  downloadQrcode() {
    const sticker = this.data.sticker;
    const profileId = sticker ? sticker.profileId : '';
    const vehicleId = sticker ? sticker.vehicleId : '';
    const url = `${this.data.apiBase}/api/profiles/${profileId}/vehicles/${vehicleId}/qrcode`;
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
      complete: () => {
        wx.hideLoading();
      }
    });
  },

  deleteSticker() {
    const that = this;
    wx.showModal({
      title: "确认删除",
      content: "删除后不可恢复，确定吗？",
      confirmColor: "#ff4d4f",
      success: (res) => {
        if (res.confirm) {
          wx.request({
            url: `${this.data.apiBase}/api/stickers/${this.data.stickerId}`,
            method: "DELETE",
            header: { Authorization: `Bearer ${this.data.token}` },
            success: ({ statusCode }) => {
              if (statusCode === 204 || statusCode === 200) {
                wx.showToast({ title: "已删除", icon: "success" });
                setTimeout(() => that.goBack(), 1000);
              } else {
                wx.showToast({ title: "删除失败", icon: "none" });
              }
            },
            fail: () => wx.showToast({ title: "网络错误", icon: "none" })
          });
        }
      }
    });
  },

  goBack() {
    wx.navigateBack();
  }
});
