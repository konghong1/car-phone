# Tab栏图标说明

## 📱 需要准备的图标

由于微信小程序TabBar需要使用图片图标，请准备以下4个图标文件（建议尺寸：81x81px）：

### 1. 挪车码图标
- **文件名**: `tab-qrcode.png` (未选中状态)
- **文件名**: `tab-qrcode-active.png` (选中状态)
- **颜色**: 
  - 未选中: #7A7E83 (灰色)
  - 选中: #667eea (紫色)
- **建议图案**: 二维码或车辆图标

### 2. 我的图标
- **文件名**: `tab-mine.png` (未选中状态)
- **文件名**: `tab-mine-active.png` (选中状态)
- **颜色**:
  - 未选中: #7A7E83 (灰色)
  - 选中: #667eea (紫色)
- **建议图案**: 用户头像或个人中心图标

---

## 🎨 临时解决方案（开发阶段）

在开发阶段，你可以：

### 方案1: 使用在线工具生成简单图标
1. 访问 https://www.iconfont.cn/
2. 搜索"二维码"和"用户"图标
3. 下载PNG格式，调整颜色
4. 放入 `miniprogram/images/` 目录

### 方案2: 使用Emoji作为临时图标（仅测试用）
暂时注释掉iconPath，只显示文字：

```json
{
  "tabBar": {
    "list": [
      {
        "pagePath": "pages/index/index",
        "text": "挪车码"
        // "iconPath": "images/tab-qrcode.png",
        // "selectedIconPath": "images/tab-qrcode-active.png"
      },
      {
        "pagePath": "pages/mine/index",
        "text": "我的"
        // "iconPath": "images/tab-mine.png",
        // "selectedIconPath": "images/tab-mine-active.png"
      }
    ]
  }
}
```

### 方案3: 使用设计工具制作
推荐使用：
- Figma (https://figma.com/)
- Sketch
- Photoshop
- 在线工具: https://canva.com/

---

## ✅ 图标放置位置

将图标文件放在：
```
miniprogram/
├── images/
│   ├── tab-qrcode.png
│   ├── tab-qrcode-active.png
│   ├── tab-mine.png
│   └── tab-mine-active.png
├── pages/
├── app.js
├── app.json
└── ...
```

---

## 🎯 推荐的图标资源

### 免费图标网站
1. **IconFont** - https://www.iconfont.cn/
2. **Flaticon** - https://www.flaticon.com/
3. **Icons8** - https://icons8.com/
4. **Material Icons** - https://fonts.google.com/icons

### 搜索关键词
- 二维码: qrcode, qr code, scan
- 用户: user, profile, account, person
- 车辆: car, vehicle, auto

---

## 💡 提示

如果你暂时没有图标，可以先使用方案2（注释掉图标路径），小程序会只显示文字标签，功能完全正常。等准备好图标后再添加即可。
