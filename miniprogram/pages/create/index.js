const app = getApp();

Page({
  data: {
    apiBase: app.globalData.apiBase,
    token: "",
    submitting: false,
    showQrcode: false,
    vehicleId: "",
    // 车主档案信息（从上一页传入）
    profileId: "",
    nickname: "",
    plateNo: "",
    phone: "",
    form: {
      comfortMessage: "您好，给您添麻烦了。车主已开启挪车电话，请点击下方按钮联系车主，感谢您的理解。"
    },
    // 样式配置
    cardStyle: "modern",  // modern, classic, cute, minimal
    backgroundColor: "#ffffff",
    themeColor: "#667eea",
    showPlateBadge: true,
    customText: "",
    // 预设样式模板
    styleTemplates: [
      { id: "modern", name: "现代简约", icon: "✨", colors: ["#667eea", "#764ba2"] },
      { id: "classic", name: "经典商务", icon: "💼", colors: ["#1e3a8a", "#1e40af"] },
      { id: "cute", name: "可爱卡通", icon: "🎨", colors: ["#f472b6", "#ec4899"] },
      { id: "minimal", name: "极简风格", icon: "⚪", colors: ["#374151", "#6b7280"] }
    ]
  },

  onLoad(options) {
    const token = app.globalData.token || wx.getStorageSync("token") || "";
    if (!token) {
      wx.showToast({ title: "请先登录", icon: "none" });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
      return;
    }
    
    // 处理传入的档案参数
    const profileId = options.profileId || "";
    const nickname = options.nickname ? decodeURIComponent(options.nickname) : "";
    const plateNo = options.plateNo ? decodeURIComponent(options.plateNo) : "";
    const phone = options.phone || "";
    
    if (!profileId) {
      wx.showToast({ title: "请先选择车主档案", icon: "none" });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
      return;
    }
    
    this.setData({ 
      token,
      profileId,
      nickname,
      plateNo,
      phone
    });
  },

  onInput(event) {
    const field = event.currentTarget.dataset.field;
    this.setData({ [`form.${field}`]: event.detail.value });
  },

  // 选择卡片样式
  selectStyle(event) {
    const styleId = event.currentTarget.dataset.style;
    const template = this.data.styleTemplates.find(t => t.id === styleId);
    if (template) {
      this.setData({
        cardStyle: styleId,
        themeColor: template.colors[0],
        backgroundColor: styleId === 'minimal' ? '#f9fafb' : '#ffffff'
      });
    }
  },

  // 自定义颜色
  onColorChange(event) {
    const field = event.currentTarget.dataset.field;
    this.setData({ [field]: event.detail.value });
  },

  // 切换车牌徽章显示
  togglePlateBadge(event) {
    this.setData({ showPlateBadge: event.detail.value });
  },

  createVehicle() {
    if (!this.data.profileId) {
      wx.showToast({ title: "车主档案信息缺失", icon: "none" });
      return;
    }

    this.setData({ submitting: true });
    
    // 构建请求数据
    const requestData = {
      profileId: this.data.profileId,  // 关键：传入档案ID
      comfortMessage: this.data.form.comfortMessage,
      cardStyle: this.data.cardStyle,
      backgroundColor: this.data.backgroundColor,
      themeColor: this.data.themeColor,
      showPlateBadge: this.data.showPlateBadge,
      customText: this.data.customText
    };
    
    wx.request({
      url: `${this.data.apiBase}/api/vehicles`,
      method: "POST",
      header: { 
        Authorization: `Bearer ${this.data.token}`,
        "Content-Type": "application/json"
      },
      data: requestData,
      success: ({ statusCode, data }) => {
        if (statusCode >= 400) {
          wx.showToast({ title: data.message || "生成失败", icon: "none" });
          return;
        }
        // 显示二维码页面
        this.setData({
          showQrcode: true,
          vehicleId: data.id
        });
        wx.showToast({ 
          title: "生成成功", 
          icon: "success",
          duration: 1500
        });
      },
      fail: () => wx.showToast({ title: "网络异常", icon: "none" }),
      complete: () => this.setData({ submitting: false })
    });
  },

  goBack() {
    if (this.data.showQrcode) {
      // 如果正在显示二维码，返回列表页
      const pages = getCurrentPages();
      if (pages.length > 1) {
        const prevPage = pages[pages.length - 2];
        if (prevPage && typeof prevPage.loadVehicleList === 'function') {
          prevPage.loadVehicleList();
        }
      }
    }
    wx.navigateBack();
  },

  backToList() {
    const pages = getCurrentPages();
    if (pages.length > 1) {
      const prevPage = pages[pages.length - 2];
      // 尝试刷新多个可能的页面
      if (prevPage && typeof prevPage.loadVehicleList === 'function') {
        prevPage.loadVehicleList();
      } else if (prevPage && typeof prevPage.loadCardList === 'function') {
        prevPage.loadCardList();
      }
    }
    wx.navigateBack();
  },

  // 保存二维码到相册
  saveQrcode() {
    const qrcodeUrl = `${this.data.apiBase}/api/vehicles/${this.data.vehicleId}/qrcode`;
    
    wx.downloadFile({
      url: qrcodeUrl,
      success: (res) => {
        if (res.statusCode === 200) {
          wx.saveImageToPhotosAlbum({
            filePath: res.tempFilePath,
            success: () => {
              wx.showToast({ title: '保存成功', icon: 'success' });
            },
            fail: () => {
              wx.showToast({ title: '保存失败', icon: 'none' });
            }
          });
        }
      },
      fail: () => {
        wx.showToast({ title: '下载失败', icon: 'none' });
      }
    });
  },

  // 分享二维码
  shareQrcode() {
    wx.showShareMenu({
      withShareTicket: true,
      menus: ['shareAppMessage', 'shareTimeline']
    });
  }
});
