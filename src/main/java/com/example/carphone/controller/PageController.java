package com.example.carphone.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PageController {
    @GetMapping(value = "/move-car", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String moveCarPage() {
        return """
                <!doctype html>
                <html lang="zh-CN">
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>联系车主挪车</title>
                    <style>
                        body { margin: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif; background: #f6f7fb; color: #172033; }
                        main { min-height: 100vh; display: grid; align-content: center; padding: 24px; box-sizing: border-box; }
                        section { max-width: 420px; width: 100%; margin: 0 auto; background: #fff; border-radius: 8px; padding: 28px; box-shadow: 0 12px 36px rgba(20, 31, 50, .08); box-sizing: border-box; }
                        h1 { margin: 0 0 10px; font-size: 24px; }
                        p { line-height: 1.7; color: #4f5d73; }
                        .plate { display: inline-block; margin: 10px 0; padding: 6px 10px; border: 1px solid #ccd5e5; border-radius: 6px; font-weight: 700; }
                        a { display: block; text-align: center; margin-top: 20px; padding: 14px 16px; background: #1473e6; color: #fff; border-radius: 6px; text-decoration: none; font-weight: 700; }
                    </style>
                </head>
                <body>
                    <main>
                        <section>
                            <h1>联系车主挪车</h1>
                            <div class="plate" id="plate">车辆信息加载中</div>
                            <p id="message">请稍候，正在为您连接车主联系方式。</p>
                            <p id="phone"></p>
                            <a id="call" href="#">呼叫车主</a>
                        </section>
                    </main>
                    <script>
                        const id = new URLSearchParams(location.search).get('id');
                        fetch('/api/public/vehicles/' + encodeURIComponent(id))
                          .then(res => res.json())
                          .then(data => {
                            document.getElementById('plate').textContent = data.plateNo || '临时挪车联系';
                            document.getElementById('message').textContent = data.comfortMessage;
                            document.getElementById('phone').textContent = '车主电话：' + data.maskedPhone;
                            document.getElementById('call').href = 'tel:' + data.phone;
                          })
                          .catch(() => {
                            document.getElementById('message').textContent = '二维码信息暂时不可用，请稍后再试。';
                          });
                    </script>
                </body>
                </html>
                """;
    }
}
