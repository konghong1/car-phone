const app = getApp();

Page({
  data: {
    apiBase: app.globalData.apiBase,
    token: "",
    ownerInfo: null,
    vehicleList: [],  // 按vehicleId分组的车辆列表
    loading: false
  },

  onShow() {
    const token = app.globalData.token || wx.getStorageSync("token") || "";
    this.setData({ token });
    if (token) {
      this.loadOwnerInfo();
      this.loadVehicleList();
    }
  },

  // 加载车主信息
  loadOwnerInfo() {
    if (!this.data.token) {
      console.log('未登录，跳过加载');
      return;
    }
    
    wx.request({
      url: `${this.data.apiBase}/api/owner/profile`,
      method: "GET",
      header: { Authorization: `Bearer ${this.data.token}` },
      success: ({ data, statusCode }) => {
        if (statusCode === 200 && data) {
          this.setData({ ownerInfo: data });
        } else if (statusCode === 401) {
          console.log('Token失效，请重新登录');
          wx.showToast({ title: '请重新登录', icon: 'none' });
        }
      },
      fail: (err) => {
        console.error('加载车主信息失败:', err);
      }
    });
  },

  // 加载车辆列表（按vehicleId分组）
  loadVehicleList() {
    if (!this.data.token) {
      console.log('未登录，跳过加载');
      return;
    }
    
    wx.request({
      url: `${this.data.apiBase}/api/vehicles`,
      method: "GET",
      header: { Authorization: `Bearer ${this.data.token}` },
      success: ({ data, statusCode }) => {
        if (statusCode === 401) {
          console.log('Token失效，请重新登录');
          wx.showToast({ title: '请重新登录', icon: 'none' });
          return;
        }
        
        if (statusCode !== 200) {
          console.error('请求失败:', statusCode);
          return;
        }
        
        // 确保data是数组
        const cards = Array.isArray(data) ? data : [];
        
        // 按vehicleId分组
        const vehicleMap = {};
        cards.forEach(card => {
          const vid = card.vehicleId;
          if (!vehicleMap[vid]) {
            vehicleMap[vid] = {
              vehicleId: vid,
              plateNo: card.plateNo,
              ownerName: card.ownerName,
              phone: card.phone,
              cards: []
            };
          }
          vehicleMap[vid].cards.push(card);
        });
        
        const vehicleList = Object.values(vehicleMap).sort((a, b) => {
          return new Date(b.cards[0].createdAt) - new Date(a.cards[0].createdAt);
        });
        
        this.setData({ vehicleList });
      },
      fail: (err) => {
        console.error('加载车辆列表失败:', err);
        wx.showToast({ title: "加载失败", icon: "none" });
      }
    });
  },

  // 进入车辆详情
  goToVehicleDetail(event) {
    const vehicle = event.currentTarget.dataset.vehicle;
    wx.navigateTo({ 
      url: `/pages/vehicle-detail/index?vehicleId=${vehicle.vehicleId}&plateNo=${encodeURIComponent(vehicle.plateNo)}` 
    });
  },

  // 添加新车
  goToAddVehicle() {
    wx.navigateTo({ url: '/pages/create/index' });
  },

  // 编辑车主信息
  editOwnerInfo() {
    wx.showModal({
      title: "编辑个人信息",
      editable: true,
      placeholderText: this.data.ownerInfo?.phone || "请输入手机号",
      success: (res) => {
        if (res.confirm && res.content) {
          const phone = res.content.trim();
          if (!/^1[3-9]\d{9}$/.test(phone)) {
            wx.showToast({ title: "手机号格式不正确", icon: "none" });
            return;
          }
          
          wx.request({
            url: `${this.data.apiBase}/api/owner/profile`,
            method: "PUT",
            header: { 
              Authorization: `Bearer ${this.data.token}`,
              "Content-Type": "application/json"
            },
            data: { phone },
            success: () => {
              wx.showToast({ title: "更新成功", icon: "success" });
              this.loadOwnerInfo();
            },
            fail: () => wx.showToast({ title: "更新失败", icon: "none" })
          });
        }
      }
    });
  }
});
