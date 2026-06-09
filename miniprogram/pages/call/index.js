const app = getApp();

Page({
  data: {
    loading: true,
    vehicle: {}
  },

  onLoad(options) {
    const id = this.resolveVehicleId(options);
    if (!id) {
      wx.showToast({ title: "二维码无效", icon: "none" });
      this.setData({ loading: false });
      return;
    }
    this.fetchVehicle(id);
  },

  resolveVehicleId(options) {
    if (options.id) {
      return options.id;
    }
    if (!options.scene) {
      return "";
    }
    const scene = decodeURIComponent(options.scene);
    const params = {};
    scene.split("&").forEach((pair) => {
      const [key, value] = pair.split("=");
      params[key] = value;
    });
    return params.id || "";
  },

  fetchVehicle(id) {
    wx.request({
      url: `${app.globalData.apiBase}/api/public/vehicles/${id}`,
      success: ({ statusCode, data }) => {
        if (statusCode >= 400) {
          wx.showToast({ title: data.message || "二维码不存在", icon: "none" });
          return;
        }
        this.setData({ vehicle: data });
      },
      fail: () => wx.showToast({ title: "网络异常", icon: "none" }),
      complete: () => this.setData({ loading: false })
    });
  },

  callOwner() {
    wx.makePhoneCall({ phoneNumber: this.data.vehicle.phone });
  }
});
