Page({
  data: {
    apiBase: getApp().globalData.apiBase,
    token: "",
    editId: "",
    nickname: "",
    phone: "",
    vehicles: [],
    submitting: false
  },

  onLoad(options) {
    const token = getApp().globalData.token || wx.getStorageSync("token") || "";
    if (!token) {
      wx.showToast({ title: "请先登录", icon: "none" });
      setTimeout(() => wx.navigateBack(), 1500);
      return;
    }
    
    this.setData({ token });

    if (options.editId) {
      this.setData({ 
        editId: options.editId,
        nickname: options.nickname ? decodeURIComponent(options.nickname) : "",
        phone: options.phone || ""
      });
      if (options.vehicles) {
        try {
          this.setData({ vehicles: JSON.parse(decodeURIComponent(options.vehicles)) });
        } catch (e) {
          this.setData({ vehicles: [] });
        }
      } else {
        this.setData({ vehicles: [] });
      }
    } else {
      this.setData({ 
        vehicles: [{ id: "", plateNo: "", phone: "" }]
      });
    }
  },

  onInput(event) {
    const { field, index } = event.currentTarget.dataset;
    if (field === 'vehicle') {
      const vIndex = index;
      const vehicleField = event.currentTarget.dataset.vfield;
      const vehicles = [...this.data.vehicles];
      if (vehicleField === 'phone') {
        vehicles[vIndex].phone = event.detail.value;
      } else if (vehicleField === 'plateNo') {
        vehicles[vIndex].plateNo = event.detail.value;
      }
      this.setData({ vehicles });
    } else {
      this.setData({ [field]: event.detail.value });
    }
  },

  addVehicle() {
    const vehicles = [...this.data.vehicles, { id: "", plateNo: "", phone: "" }];
    this.setData({ vehicles });
  },

  removeVehicle(event) {
    const { index } = event.currentTarget.dataset;
    if (this.data.vehicles.length <= 1) {
      wx.showToast({ title: "至少保留一个车牌", icon: "none" });
      return;
    }
    const vehicles = [...this.data.vehicles];
    vehicles.splice(index, 1);
    this.setData({ vehicles });
  },

  submit() {
    const { nickname, vehicles } = this.data;

    if (!nickname.trim()) {
      wx.showToast({ title: "请输入昵称", icon: "none" });
      return;
    }

    const firstVehicle = vehicles[0];
    if (!firstVehicle || !firstVehicle.phone.trim() || !/^1[3-9]\d{9}$/.test(firstVehicle.phone.trim())) {
      wx.showToast({ title: "请填写正确的手机号", icon: "none" });
      return;
    }

    if (!firstVehicle.plateNo.trim()) {
      wx.showToast({ title: "请填写车牌号", icon: "none" });
      return;
    }

    this.setData({ submitting: true });

    const url = this.data.editId
      ? `${this.data.apiBase}/api/profiles/${this.data.editId}`
      : `${this.data.apiBase}/api/profiles`;
    const method = this.data.editId ? "PUT" : "POST";

    // 对于更新，后端使用UpdateOwnerRequest格式（带vehicles字段）
    // 对于创建，需要转换为CreateProfileRequest格式
    let requestData;
    if (this.data.editId) {
      requestData = {
        nickname: nickname.trim(),
        phone: firstVehicle.phone.trim(),
        vehicles: vehicles.map(v => ({
          id: v.id,
          plateNo: v.plateNo.trim().toUpperCase(),
          phone: v.phone.trim()
        }))
      };
    } else {
      requestData = {
        nickname: nickname.trim(),
        phone: firstVehicle.phone.trim(),
        vehicles: vehicles.map(v => ({
          plateNo: v.plateNo.trim().toUpperCase(),
          phone: v.phone.trim()
        }))
      };
    }

    wx.request({
      url,
      method,
      header: {
        Authorization: `Bearer ${this.data.token}`,
        "Content-Type": "application/json"
      },
      data: requestData,
      success: ({ statusCode }) => {
        if (statusCode === 200) {
          wx.showToast({ title: this.data.editId ? "更新成功" : "添加成功", icon: "success" });
          setTimeout(() => wx.navigateBack(), 1000);
        } else {
          wx.showToast({ title: "操作失败", icon: "none" });
        }
      },
      fail: () => wx.showToast({ title: "网络错误", icon: "none" }),
      complete: () => this.setData({ submitting: false })
    });
  },

  formatDate(dateStr) {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    const month = d.getMonth() + 1;
    const day = d.getDate();
    return `${month}月${day}日`;
  }
});
