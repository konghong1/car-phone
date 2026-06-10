const app = getApp();

Page({
  data: {
    apiBase: app.globalData.apiBase,
    token: "",
    profileId: "",
    nickname: "",
    stickers: [],
    loading: false
  },

  onLoad(options) {
    this.setData({
      profileId: options.profileId || "",
      nickname: options.nickname ? decodeURIComponent(options.nickname) : ""
    });
  },

  onShow() {
    const token = app.globalData.token || wx.getStorageSync("token") || "";
    this.setData({ token });
    if (token && this.data.profileId) {
      this.loadStickers();
    }
  },

  loadStickers() {
    this.setData({ loading: true });
    wx.request({
      url: `${this.data.apiBase}/api/profiles/${this.data.profileId}/stickers`,
      method: "GET",
      header: { Authorization: `Bearer ${this.data.token}` },
      success: ({ data, statusCode }) => {
        if (statusCode === 200 && Array.isArray(data)) {
          this.setData({ stickers: data });
        }
      },
      fail: () => wx.showToast({ title: "加载失败", icon: "none" }),
      complete: () => this.setData({ loading: false })
    });
  },

  // 查看贴图详情
  viewSticker(event) {
    const sticker = event.currentTarget.dataset.sticker;
    wx.navigateTo({
      url: `/pages/sticker-detail/index?stickerId=${sticker.id}`
    });
  },

  // 制作新贴图 → 跳转贴图库
  createSticker() {
    wx.navigateTo({
      url: `/pages/template-library/index?profileId=${this.data.profileId}`
    });
  },

  goBack() {
    wx.navigateBack();
  }
});
