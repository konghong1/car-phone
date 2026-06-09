App({
  globalData: {
    apiBase: "http://127.0.0.1:8081",
    token: ""
  },
  onLaunch() {
    const token = wx.getStorageSync("token");
    if (token) {
      this.globalData.token = token;
    }
  }
});
