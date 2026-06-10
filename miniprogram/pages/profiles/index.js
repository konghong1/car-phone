const app = getApp();

Page({
  data: {
    apiBase: app.globalData.apiBase,
    token: "",
    profiles: [],
    loading: false
  },

  onShow() {
    const token = app.globalData.token || wx.getStorageSync("token") || "";
    this.setData({ token });
    if (token) {
      this.loadProfiles();
    } else {
      // 未登录，跳转我的页面登录
      wx.switchTab({ url: '/pages/mine/index' });
    }
  },

  loadProfiles() {
    this.setData({ loading: true });
    wx.request({
      url: `${this.data.apiBase}/api/profiles`,
      method: "GET",
      header: { Authorization: `Bearer ${this.data.token}` },
      success: ({ data, statusCode }) => {
        if (statusCode === 200 && Array.isArray(data)) {
          this.setData({ profiles: data });
        } else if (statusCode === 401) {
          wx.showToast({ title: '请重新登录', icon: 'none' });
          wx.switchTab({ url: '/pages/mine/index' });
        }
      },
      fail: () => wx.showToast({ title: "加载失败", icon: "none" }),
      complete: () => this.setData({ loading: false })
    });
  },

  // 点击档案 → 进入该档案的贴图列表
  selectProfile(event) {
    const profile = event.currentTarget.dataset.profile;
    wx.navigateTo({
      url: `/pages/stickers/index?profileId=${profile.id}&nickname=${encodeURIComponent(profile.nickname || '')}`
    });
  },

  // 长按档案 → 编辑/删除
  editProfile(event) {
    const profile = event.currentTarget.dataset.profile;
    wx.showActionSheet({
      itemList: ['编辑信息', '删除档案'],
      success: (res) => {
        if (res.tapIndex === 0) {
          wx.navigateTo({
            url: `/pages/profile-create/index?editId=${profile.id}&nickname=${encodeURIComponent(profile.nickname)}&plateNo=${encodeURIComponent(profile.plateNo || '')}&phone=${profile.phone}`
          });
        } else if (res.tapIndex === 1) {
          this.deleteProfile(profile.id);
        }
      }
    });
  },

  // 跳转到添加档案页面
  addProfile() {
    wx.navigateTo({ url: '/pages/profile-create/index' });
  },

  deleteProfile(id) {
    wx.showModal({
      title: '确认删除',
      content: '删除后将同时删除该档案的所有贴图，确定吗？',
      confirmColor: '#ff4d4f',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '删除中...' });
          wx.request({
            url: `${this.data.apiBase}/api/profiles/${id}`,
            method: 'DELETE',
            header: { Authorization: `Bearer ${this.data.token}` },
            success: ({ statusCode }) => {
              if (statusCode === 204 || statusCode === 200) {
                wx.showToast({ title: '删除成功', icon: 'success' });
                this.loadProfiles();
              } else {
                wx.showToast({ title: '删除失败', icon: 'none' });
              }
            },
            fail: () => wx.showToast({ title: '网络错误', icon: 'none' }),
            complete: () => wx.hideLoading()
          });
        }
      }
    });
  }
});
