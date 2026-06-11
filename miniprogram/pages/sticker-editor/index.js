const app = getApp();
var drawCtx = wx.createCanvasContext('stickerCanvas');

var saveKey = 'sticker_draft_';

function renderEditorCanvas(data) {
  var layers = data.layers;
  var canvasWidth = data.canvasWidth;
  var canvasHeight = data.canvasHeight;
  drawCtx.clearRect(0, 0, canvasWidth, canvasHeight);
  for (var i = 0; i < layers.length; i++) {
    var layer = layers[i];
    if (layer.type === 'background_image') {
      drawCtx.drawImage(layer.src, 0, 0, canvasWidth, canvasHeight);
    } else if (layer.type === 'text') {
      drawCtx.setFontSize(layer.fontSize || 24);
      drawCtx.setFillStyle(layer.color || '#ffffff');
      drawCtx.fillText(layer.text || '', layer.x, layer.y);
    } else if (layer.type === 'image') {
      drawCtx.drawImage(layer.src, layer.x, layer.y, layer.width, layer.height);
    } else if (layer.type === 'rect') {
      drawCtx.setFillStyle(layer.color || '#667eea');
      if (layer.x !== undefined) { drawCtx.fillRect(layer.x, layer.y, layer.width, layer.height); }
    } else if (layer.type === 'circle') {
      drawCtx.setFillStyle(layer.color || '#667eea');
      drawCtx.beginPath();
      drawCtx.arc(layer.x + layer.width / 2, layer.y + layer.height / 2, layer.width / 2, 0, Math.PI * 2);
      drawCtx.fill();
    } else if (layer.type === 'emoji') {
      drawCtx.setFontSize(layer.fontSize || 48);
      drawCtx.setFillStyle('#ffffff');
      drawCtx.fillText(layer.emoji || '😀', layer.x, layer.y + layer.fontSize);
    }
  }
  drawCtx.draw(false);
}

Page({
  data: {
    apiBase: app.globalData.apiBase,
    token: '',
    profileId: '',
    profile: null,
    vehicle: null,
    canvasWidth: 375,
    canvasHeight: 563,
    layers: [],
    selectedLayerIndex: -1,
    activeTool: '',
    textInputValue: '',
    textSize: 24,
    textColor: '#ffffff',
    textColors: ['#ffffff', '#000000', '#ff6b6b', '#667eea', '#ffd93d', '#6bcb77', '#ff922b'],
    shapeColor: '#667eea',
    shapeColors: ['#667eea', '#ff6b6b', '#ffd93d', '#6bcb77', '#ff922b', '#a55eea', '#ffffff'],
    selectedShape: 'rect',
    showTemplateModal: false,
    showQuickTemplateModal: false,
    templates: [],
    selectedTemplateId: '',
    selectedBgId: '',
    comfortMessage: '',
    loading: false,
    loadingText: '加载中...',
    showConfirmModal: false
  },

  emojis: ['😀', '🚗', '❤️', '🌟', '🎉', '🌈', '🔥', '🌸', '🍀', '⚡', '🌙', '💎', '🐱', '🐶', '🎵', '🏎️'],

  onLoad(options) {
    var token = app.globalData.token || wx.getStorageSync('token') || '';
    var profileId = options.profileId || '';
    saveKey = saveKey + profileId + '_';
    this.setData({
      token: token,
      profileId: profileId,
      canvasWidth: 375,
      canvasHeight: Math.round(375 * (1200 / 800))
    });
    this.loadProfile();
    this.loadTemplates();
    this.loadDraft();
  },

  loadProfile: function() {
    var that = this;
    wx.request({
      url: this.data.apiBase + '/api/profiles',
      method: 'GET',
      header: { Authorization: 'Bearer ' + this.data.token },
      success: function(res) {
        if (Array.isArray(res.data)) {
          for (var i = 0; i < res.data.length; i++) {
            if (res.data[i].id === that.data.profileId) {
              var profile = res.data[i];
              if (profile && profile.vehicles && profile.vehicles.length > 0) {
                that.setData({ profile: profile, vehicle: profile.vehicles[0] });
              }
              break;
            }
          }
        }
      }
    });
  },

  loadTemplates: function() {
    var that = this;
    wx.request({
      url: that.data.apiBase + '/api/templates',
      method: 'GET',
      success: function(res) {
        if (Array.isArray(res.data)) {
          that.setData({ templates: res.data });
        }
      }
    });
  },

  loadDraft: function() {
    try {
      var draft = wx.getStorageSync(saveKey);
      if (draft && draft.layers && draft.layers.length > 0) {
        this.setData({
          layers: draft.layers,
          comfortMessage: draft.comfortMessage || '',
          selectedTemplateId: draft.selectedTemplateId || ''
        });
      }
    } catch (e) {}
  },

  saveDraft: function() {
    try {
      wx.setStorageSync(saveKey, {
        layers: this.data.layers,
        comfortMessage: this.data.comfortMessage,
        selectedTemplateId: this.data.selectedTemplateId
      });
      wx.showToast({ title: '已保存', icon: 'success' });
    } catch (e) {
      wx.showToast({ title: '保存失败', icon: 'none' });
    }
  },

  selectTool: function(e) {
    var tool = e.currentTarget.dataset.tool;
    this.setData({ activeTool: this.data.activeTool === tool ? '' : tool });
  },

  selectShape: function(e) {
    this.setData({ selectedShape: e.currentTarget.dataset.shape });
  },

  selectTextColor: function(e) {
    this.setData({ textColor: e.currentTarget.dataset.color });
  },

  selectShapeColor: function(e) {
    this.setData({ shapeColor: e.currentTarget.dataset.color });
  },

  onComfortInput: function(e) {
    this.setData({ comfortMessage: e.detail.value });
  },

  onTextInput: function(e) {
    this.setData({ textInputValue: e.detail.value });
  },

  onTextSizeChange: function(e) {
    this.setData({ textSize: parseInt(e.detail.value) });
  },

  addTextLayer: function() {
    var text = this.data.textInputValue.trim();
    if (!text) { wx.showToast({ title: '请输入文字', icon: 'none' }); return; }
    var cw = this.data.canvasWidth;
    var ch = this.data.canvasHeight;
    var layers = this.data.layers;
    layers.push({
      type: 'text', text: text, x: cw * 0.1, y: ch * 0.5,
      fontSize: this.data.textSize, color: this.data.textColor,
      width: cw * 0.5, height: this.data.textSize
    });
    this.setData({ layers: layers, textInputValue: '', activeTool: '', selectedLayerIndex: layers.length - 1 });
    this.saveDraftDebounced();
  },

  addShapeLayer: function() {
    var cw = this.data.canvasWidth;
    var ch = this.data.canvasHeight;
    var layers = this.data.layers;
    var size = Math.min(cw, ch) * 0.15;
    var s = this.data.selectedShape;
    var c = this.data.shapeColor;
    var layer;
    if (s === 'line') {
      layer = { type: 'rect', x: cw * 0.2, y: ch * 0.5, width: cw * 0.6, height: 4, color: c };
    } else {
      layer = { type: s, x: cw * 0.3, y: ch * 0.3, width: size, height: size, color: c };
    }
    layers.push(layer);
    this.setData({ layers: layers, activeTool: '', selectedLayerIndex: layers.length - 1 });
    this.saveDraftDebounced();
  },

  addEmojiLayer: function(e) {
    var emoji = e.currentTarget.dataset.emoji;
    var cw = this.data.canvasWidth;
    var ch = this.data.canvasHeight;
    var layers = this.data.layers;
    layers.push({
      type: 'emoji', emoji: emoji, x: cw * 0.1, y: ch * 0.3,
      fontSize: 48, width: 48, height: 48
    });
    this.setData({ layers: layers, activeTool: '', selectedLayerIndex: layers.length - 1 });
    this.saveDraftDebounced();
  },

  handleCanvasTap: function(e) {
    var tapX = e.detail.x;
    var tapY = e.detail.y;
    var layers = this.data.layers;
    var found = -1;
    for (var i = layers.length - 1; i >= 0; i--) {
      var l = layers[i];
      if (l.type !== 'background_image' && l.x !== undefined && tapX >= l.x && tapX <= l.x + l.width && tapY >= l.y && tapY <= l.y + l.height) {
        found = i;
        break;
      }
    }
    this.setData({ selectedLayerIndex: found });
  },

  deleteSelectedLayer: function() {
    if (this.data.selectedLayerIndex < 0) return;
    var layers = this.data.layers.slice();
    layers.splice(this.data.selectedLayerIndex, 1);
    this.setData({ layers: layers, selectedLayerIndex: -1 });
    this.saveDraftDebounced();
  },

  openTemplateModal: function() { this.setData({ showTemplateModal: true }); },
  closeTemplateModal: function() { this.setData({ showTemplateModal: false }); },
  openQuickTemplateModal: function() { this.setData({ showQuickTemplateModal: true }); },
  closeQuickTemplateModal: function() { this.setData({ showQuickTemplateModal: false }); },

  selectTemplateFromModal: function(e) {
    var tmpl = e.currentTarget.dataset.template;
    var layers = this.data.layers;
    var bgLayer = { type: 'background_image', src: tmpl.imageUrl };
    this.setData({
      layers: [bgLayer].concat(layers.filter(function(l) { return l.type !== 'background_image'; })),
      selectedTemplateId: tmpl.id,
      showTemplateModal: false
    });
    this.saveDraftDebounced();
  },

  applyBgTemplate: function(e) {
    var color = e.currentTarget.dataset.color;
    var bgId = e.currentTarget.dataset.bgid;
    var cw = this.data.canvasWidth;
    var ch = this.data.canvasHeight;
    // 用矩形作为背景
    var layers = this.data.layers.filter(function(l) { return l.type !== 'background_image'; });
    layers.unshift({ type: 'rect', x: 0, y: 0, width: cw, height: ch, color: color });
    this.setData({ layers: layers, selectedBgId: bgId });
    this.saveDraftDebounced();
    this.closeQuickTemplateModal();
  },

  applyGradientBg: function(e) {
    // 用较浅的颜色作为背景渐变替代
    var from = e.currentTarget.dataset.from;
    var bgId = e.currentTarget.dataset.bgid;
    var cw = this.data.canvasWidth;
    var ch = this.data.canvasHeight;
    var layers = this.data.layers.filter(function(l) { return l.type !== 'background_image'; });
    // 用第一个渐变色作为背景
    layers.unshift({ type: 'rect', x: 0, y: 0, width: cw, height: ch, color: from });
    this.setData({ layers: layers, selectedBgId: bgId });
    this.saveDraftDebounced();
    this.closeQuickTemplateModal();
  },

  goBack: function() {
    wx.navigateBack();
  },

  renderToBase64: function() {
    var that = this;
    return new Promise(function(resolve, reject) {
      wx.canvasToTempFilePath({
        canvasId: 'stickerCanvas',
        width: that.data.canvasWidth,
        height: that.data.canvasHeight,
        success: function(res) { resolve(res.tempFilePath); },
        fail: reject
      }, that);
    });
  },

  confirmCreate: function() {
    if (!this.data.vehicle) {
      wx.showToast({ title: '车辆信息缺失', icon: 'none' });
      return;
    }
    this.setData({ showConfirmModal: true });
  },

  closeConfirm: function() {
    this.setData({ showConfirmModal: false });
  },

  doCreate: function() {
    var that = this;
    var token = this.data.token;
    var vehicle = this.data.vehicle;

    this.setData({ loading: true, loadingText: '生成中...' });
    this.closeConfirm();

    this.renderToBase64().then(function(tempFilePath) {
      wx.uploadFile({
        url: that.data.apiBase + '/api/stickers/create',
        filePath: tempFilePath,
        name: 'image',
        header: { Authorization: 'Bearer ' + token },
        formData: {
          profileId: that.data.profileId,
          vehicleId: vehicle.id,
          comfortMessage: that.data.comfortMessage || ''
        },
        success: function(res) {
          var data = JSON.parse(res.data);
          wx.showToast({ title: '制作成功', icon: 'success' });
          // 清除草稿
          try { wx.removeStorageSync(saveKey); } catch (e) {}
          setTimeout(function() {
            wx.redirectTo({ url: '/pages/sticker-detail/index?stickerId=' + data.id });
          }, 1500);
        },
        fail: function() {
          wx.showToast({ title: '制作失败', icon: 'none' });
        },
        complete: function() {
          that.setData({ loading: false });
        }
      });
    }).catch(function() {
      wx.showToast({ title: '生成失败', icon: 'none' });
      that.setData({ loading: false });
    });
  },

  saveDraftDebounced: function() {
    var that = this;
    if (that._draftTimer) clearTimeout(that._draftTimer);
    that._draftTimer = setTimeout(function() {
      try {
        wx.setStorageSync(saveKey, {
          layers: that.data.layers,
          comfortMessage: that.data.comfortMessage,
          selectedTemplateId: that.data.selectedTemplateId
        });
      } catch (e) {}
    }, 500);
  }
});
