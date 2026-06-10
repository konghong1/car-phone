const app = getApp();

Page({
  data: {
    apiBase: app.globalData.apiBase,
    token: "",
    vehicleId: "",
    plateNo: "",
    cardList: [],
    loading: false
  },

  onLoad(options) {
    const token = app.globalData.token || wx.getStorageSync("token") || "";
    this.setData({ 
      token,
      vehicleId: options.vehicleId,
      plateNo: decodeURIComponent(options.plateNo || '')
    });
    this.loadCardList();
  },

  // 加载该车辆的所有卡片
  loadCardList() {
    wx.request({
      url: `${this.data.apiBase}/api/vehicles`,
      method: "GET",
      header: { Authorization: `Bearer ${this.data.token}` },
      success: ({ data }) => {
        // 过滤出当前车辆的卡片
        const cards = (data || []).filter(card => card.vehicleId === this.data.vehicleId);
        this.setData({ cardList: cards });
      },
      fail: () => wx.showToast({ title: "加载失败", icon: "none" })
    });
  },

  // 预览卡片（扫码效果）
  previewCard(event) {
    const cardId = event.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/call/index?id=${cardId}` });
  },

  // 删除卡片
  deleteCard(event) {
    const cardId = event.currentTarget.dataset.id;
    wx.showModal({
      title: "确认删除",
      content: "确定要删除这个挪车贴图吗？",
      success: (res) => {
        if (res.confirm) {
          wx.request({
            url: `${this.data.apiBase}/api/vehicles/${cardId}`,
            method: "DELETE",
            header: { Authorization: `Bearer ${this.data.token}` },
            success: () => {
              wx.showToast({ title: "删除成功", icon: "success" });
              this.loadCardList();
            },
            fail: () => wx.showToast({ title: "删除失败", icon: "none" })
          });
        }
      }
    });
  },

  // 创建新卡片
  createNewCard() {
    wx.navigateTo({ 
      url: `/pages/create/index?vehicleId=${this.data.vehicleId}&plateNo=${encodeURIComponent(this.data.plateNo)}` 
    });
  },

  // 返回
  goBack() {
    wx.navigateBack();
  }
});
