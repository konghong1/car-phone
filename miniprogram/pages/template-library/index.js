const app = getApp();

Page({
  data: {
    apiBase: app.globalData.apiBase,
    token: "",
    profileId: "",
    templates: [],
    categories: ["全部", "商务", "可爱", "简约", "卡通"],
    currentCategory: "全部",
    loading: false,
    creating: false
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
    this.loadTemplates();
  },

  loadTemplates() {
    this.setData({ loading: true });
    const category = this.data.currentCategory === "全部" ? "" : this.data.currentCategory;
    const url = category
      ? `${this.data.apiBase}/api/templates?category=${encodeURIComponent(category)}`
      : `${this.data.apiBase}/api/templates`;

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

  // 切换分类
  switchCategory(event) {
    const category = event.currentTarget.dataset.category;
    this.setData({ currentCategory: category });
    this.loadTemplates();
  },

  // 选择模板 → 确认制作
  selectTemplate(event) {
    const template = event.currentTarget.dataset.template;
    
    wx.showModal({
      title: "确认制作",
      content: `使用「${template.name}」模板制作贴图？`,
      confirmText: "确认制作",
      confirmColor: "#667eea",
      success: (res) => {
        if (res.confirm) {
          this.createSticker(template);
        }
      }
    });
  },

  // 调用后端创建贴图
  createSticker(template) {
    this.setData({ creating: true });
    wx.showLoading({ title: "制作中..." });

    wx.request({
      url: `${this.data.apiBase}/api/vehicles`,
      method: "POST",
      header: {
        Authorization: `Bearer ${this.data.token}`,
        "Content-Type": "application/json"
      },
      data: {
        profileId: this.data.profileId,
        templateId: template.id
      },
      success: ({ data, statusCode }) => {
        if (statusCode === 200 && data.id) {
          wx.showToast({ title: "制作成功", icon: "success" });
          // 跳转到贴图详情
          wx.redirectTo({
            url: `/pages/sticker-detail/index?stickerId=${data.id}`
          });
        } else {
          wx.showToast({ title: "制作失败", icon: "none" });
        }
      },
      fail: () => wx.showToast({ title: "网络错误", icon: "none" }),
      complete: () => {
        this.setData({ creating: false });
        wx.hideLoading();
      }
    });
  }
});
