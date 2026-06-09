App({
  globalData: {
    apiBase: "http://localhost:8081",
    token: ""
  },
  onLaunch() {
    const token = wx.getStorageSync("token");
    if (token) {
      this.globalData.token = token;
    }
  }
});
