# 安心挪车码

这是一个“扫码联系车主挪车”的完整示例，包含 Java 后端、普通二维码兜底 H5 页，以及微信小程序实现。

## 功能

- 车主在微信小程序内登录。
- 车主填写称呼、车牌、手机号和给扫码人的安慰语。
- 后端生成二维码：
  - 配置了 `WECHAT_APP_ID` 和 `WECHAT_APP_SECRET` 时，生成微信小程序码，扫码进入小程序呼叫页。
  - 未配置微信密钥时，生成普通二维码，扫码进入后端 `/move-car` H5 兜底页，便于本地调试。
- 扫码人先看到安慰语和脱敏电话，点击“呼叫车主”后发起电话。

## 后端启动

```powershell
mvn -s .mvn/settings.xml spring-boot:run
```

默认地址是 `http://127.0.0.1:8081`。

生产环境建议配置：

```powershell
$env:WECHAT_APP_ID="你的小程序AppID"
$env:WECHAT_APP_SECRET="你的小程序AppSecret"
$env:APP_PUBLIC_BASE_URL="https://你的后端域名"
$env:SERVER_PORT="8081"
```

`SERVER_PORT` 控制后端监听端口，`APP_PUBLIC_BASE_URL` 控制二维码里写入的公开访问地址。

## 小程序运行

1. 用微信开发者工具打开 `miniprogram/`。
2. 修改 `miniprogram/app.js` 中的 `apiBase` 为你的后端地址。
3. 本地调试时可在开发者工具里关闭域名校验；正式发布时后端域名必须配置到微信小程序合法 request/download 域名。
4. 发布前把 `miniprogram/project.config.json` 里的 `appid` 改成真实小程序 AppID。

## 核心接口

### 微信登录

`POST /api/auth/wechat-login`

```json
{
  "code": "wx.login 返回的 code",
  "nickname": "可选昵称"
}
```

返回：

```json
{
  "token": "登录令牌",
  "ownerId": "车主ID",
  "openid": "微信openid"
}
```

未配置微信密钥时，后端会使用演示 openid，方便本地联调。

### 创建挪车码

`POST /api/vehicles`

Header:

```text
Authorization: Bearer <token>
```

Body:

```json
{
  "ownerName": "王先生",
  "plateNo": "沪A12345",
  "phone": "13800138000",
  "comfortMessage": "您好，给您添麻烦了。请点击下方按钮联系我，我会尽快挪车。"
}
```

### 获取二维码图片

`GET /api/vehicles/{id}/qrcode`

返回 PNG 图片。

### 扫码页获取公开信息

`GET /api/public/vehicles/{id}`

返回安慰语、车牌、脱敏电话和用于拨号的手机号。

## 生产化建议

- 把 `InMemoryStore` 换成 MySQL 或 PostgreSQL，并对手机号加密存储。
- 上线后建议接入隐私号或云呼叫能力，避免直接暴露车主真实手机号。
- 给二维码增加启用/停用、访问频率限制和异常呼叫记录。
- 微信小程序码生成结果可缓存到对象存储，避免每次打开都调用微信接口。
