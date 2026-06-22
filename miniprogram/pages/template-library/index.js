const app = getApp();

Page({
  data: {
    apiBase: app.globalData.apiBase,
    token: "",
    profileId: "",
    profile: null,
    selectedVehicle: null,
    templates: [],
    categories: ["全部", "商务", "可爱", "简约", "卡通"],
    currentCategory: "全部",
    loading: false
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
      profileId: options.profileId || ""
    });
    this.loadProfile();
    this.loadTemplates();
  },

  loadProfile() {
    const token = this.data.token;
    const apiBase = this.data.apiBase;
    wx.request({
      url: apiBase + "/api/profiles",
      method: "GET",
      header: { Authorization: "Bearer " + token },
      success: ({ data }) => {
        if (Array.isArray(data)) {
          const profile = data.find(p => p.id === this.data.profileId);
          this.setData({ profile: profile });
          if (profile && profile.vehicles && profile.vehicles.length > 0) {
            this.setData({ selectedVehicle: profile.vehicles[0] });
          }
        }
      }
    });
  },

  loadTemplates() {
    this.setData({ loading: true });
    const category = this.data.currentCategory === "全部" ? "" : this.data.currentCategory;
    const url = category
      ? this.data.apiBase + "/api/templates?category=" + encodeURIComponent(category)
      : this.data.apiBase + "/api/templates";

    wx.request({
      url,
      method: "GET",
      success: ({ data, statusCode }) => {
        if (statusCode === 200 && Array.isArray(data)) {
          this.setData({ templates: data });
        }
      },
      fail: () => wx.showToast({ title: "加载失败", icon: "none" }),
      complete: () => this.setData({ loading: false })
    });
  },

  switchCategory(event) {
    const category = event.currentTarget.dataset.category;
    this.setData({ currentCategory: category });
    this.loadTemplates();
  },

  selectVehicle(event) {
    const vehicle = event.currentTarget.dataset.vehicle;
    this.setData({ selectedVehicle: vehicle });
  },

  selectTemplate(event) {
    const template = event.currentTarget.dataset.template;
    wx.navigateTo({
      url: "/pages/sticker-editor/index?profileId=" + this.data.profileId + "&templateId=" + template.id + "&templateName=" + encodeURIComponent(template.name)
    });
  },

  chooseCustomImage() {
    const that = this;
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      success: (res) => {
        const tempFilePath = res.tempFiles[0].tempFilePath;
        wx.navigateTo({
          url: "/pages/sticker-editor/index?profileId=" + this.data.profileId + "&customImage=" + encodeURIComponent(tempFilePath)
        });
      }
    });
  },

  goBack() {
    wx.navigateBack();
  }
});
