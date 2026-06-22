// ===== Config =====
const API_BASE = window.location.origin;
const THEMES = {
    cyber:   { primary: "#00ffc8", bg1: "#0a0f1e", bg2: "#00ffc8", grid: "rgba(0,255,200,0.12)" },
    ocean:   { primary: "#00aaff", bg1: "#001a33", bg2: "#00aaff", grid: "rgba(0,170,255,0.12)" },
    aurora:  { primary: "#aa00ff", bg1: "#0a0f1e", bg2: "#aa00ff", grid: "rgba(170,0,255,0.12)" },
    fire:    { primary: "#ff3300", bg1: "#1a0000", bg2: "#ff3300", grid: "rgba(255,51,0,0.12)" }
};

let currentTheme = "cyber";
let currentVehicle = null;

// ===== Background Animation =====
(function initBgCanvas() {
    const canvas = document.getElementById("bgCanvas");
    const ctx = canvas.getContext("2d");
    let particles = [];
    let w, h;

    function resize() {
        w = canvas.width = window.innerWidth;
        h = canvas.height = window.innerHeight;
    }
    resize();
    window.addEventListener("resize", resize);

    // Create floating particles
    for (let i = 0; i < 60; i++) {
        particles.push({
            x: Math.random() * w,
            y: Math.random() * h,
            vx: (Math.random() - 0.5) * 0.3,
            vy: (Math.random() - 0.5) * 0.3,
            r: Math.random() * 1.5 + 0.5,
            alpha: Math.random() * 0.3 + 0.1
        });
    }

    function animate() {
        ctx.clearRect(0, 0, w, h);
        particles.forEach(p => {
            p.x += p.vx;
            p.y += p.vy;
            if (p.x < 0) p.x = w;
            if (p.x > w) p.x = 0;
            if (p.y < 0) p.y = h;
            if (p.y > h) p.y = 0;

            ctx.beginPath();
            ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
            ctx.fillStyle = `rgba(0,255,200,${p.alpha})`;
            ctx.fill();
        });

        // Draw connections
        for (let i = 0; i < particles.length; i++) {
            for (let j = i + 1; j < particles.length; j++) {
                const dx = particles[i].x - particles[j].x;
                const dy = particles[i].y - particles[j].y;
                const dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < 120) {
                    ctx.beginPath();
                    ctx.moveTo(particles[i].x, particles[i].y);
                    ctx.lineTo(particles[j].x, particles[j].y);
                    ctx.strokeStyle = `rgba(0,255,200,${0.04 * (1 - dist / 120)})`;
                    ctx.lineWidth = 0.5;
                    ctx.stroke();
                }
            }
        }
        requestAnimationFrame(animate);
    }
    animate();
})();

// ===== Tab Navigation =====
document.querySelectorAll(".nav-tab").forEach(tab => {
    tab.addEventListener("click", () => switchTab(tab.dataset.tab));
});

function switchTab(name) {
    document.querySelectorAll(".nav-tab").forEach(t => t.classList.remove("active"));
    document.querySelector(`.nav-tab[data-tab="${name}"]`).classList.add("active");
    document.querySelectorAll(".tab-content").forEach(c => c.classList.remove("active"));
    document.getElementById(`tab-${name}`).classList.add("active");
}

// ===== Theme Selection =====
document.querySelectorAll(".swatch").forEach(sw => {
    sw.addEventListener("click", () => {
        document.querySelectorAll(".swatch").forEach(s => s.classList.remove("active"));
        sw.classList.add("active");
        currentTheme = sw.dataset.theme;
    });
});

// ===== Form Submission =====
document.getElementById("createForm").addEventListener("submit", async (e) => {
    e.preventDefault();

    const data = {
        ownerName: document.getElementById("ownerName").value.trim(),
        plateNo: document.getElementById("plateNo").value.trim(),
        phone: document.getElementById("phone").value.trim(),
        comfortMessage: document.getElementById("comfortMessage").value.trim()
    };

    if (!data.ownerName || !data.phone) {
        showToast("请填写必填项");
        return;
    }
    if (!/^1[3-9]\d{9}$/.test(data.phone)) {
        showToast("请输入正确的手机号");
        return;
    }

    const btn = document.getElementById("generateBtn");
    btn.disabled = true;
    btn.querySelector(".btn-text").style.display = "none";
    btn.querySelector(".btn-loader").style.display = "inline";

    try {
        // Step 1: Create vehicle via demo login
        const token = localStorage.getItem("auth_token");
        let headers = {};
        if (token) headers["Authorization"] = "Bearer " + token;

        let resp;
        if (headers["Authorization"]) {
            resp = await fetch(`${API_BASE}/api/vehicles`, {
                method: "POST",
                headers: { ...headers, "Content-Type": "application/json" },
                body: JSON.stringify(data)
            });
        } else {
            // Demo: create without auth for simplicity
            resp = await fetch(`${API_BASE}/api/vehicles-demo`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(data)
            });
        }

        if (resp.status === 401 || resp.status === 403) {
            showToast("请先登录");
            return;
        }

        if (!resp.ok) {
            const err = await resp.json().catch(() => ({}));
            showToast(err.message || "创建失败");
            return;
        }

        const vehicle = await resp.json();
        currentVehicle = vehicle;
        localStorage.setItem("last_vehicle", JSON.stringify(vehicle));

        // Step 2: Render sticker on canvas
        await renderStickerOnCanvas(vehicle);

        // Show options and actions
        document.getElementById("styleOptions").style.display = "flex";
        document.getElementById("canvasActions").style.display = "flex";

        showToast("贴图生成成功!");
    } catch (err) {
        console.error(err);
        showToast("网络异常，请重试");
    } finally {
        btn.disabled = false;
        btn.querySelector(".btn-text").style.display = "inline";
        btn.querySelector(".btn-loader").style.display = "none";
    }
});

// ===== Render Sticker on Canvas =====
async function renderStickerOnCanvas(vehicle) {
    const sizeParts = document.getElementById("sizeSelect").value.split("x");
    const width = parseInt(sizeParts[0]);
    const height = parseInt(sizeParts[1]);
    const canvas = document.getElementById("stickerCanvas");
    const ctx = canvas.getContext("2d");
    const placeholder = document.getElementById("canvasPlaceholder");

    canvas.width = width;
    canvas.height = height;
    canvas.style.display = "block";
    placeholder.style.display = "none";

    const theme = THEMES[currentTheme];

    // Draw gradient background
    const grad = ctx.createLinearGradient(0, 0, 0, height);
    grad.addColorStop(0, theme.bg1);
    grad.addColorStop(1, darken(theme.bg1, 30));
    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, width, height);

    // Draw subtle grid
    ctx.strokeStyle = theme.grid;
    ctx.lineWidth = 0.5;
    for (let x = 0; x < width; x += 30) {
        ctx.beginPath();
        ctx.moveTo(x, 0);
        ctx.lineTo(x, height);
        ctx.stroke();
    }
    for (let y = 0; y < height; y += 30) {
        ctx.beginPath();
        ctx.moveTo(0, y);
        ctx.lineTo(width, y);
        ctx.stroke();
    }

    // Draw corner decorations
    drawCornerDecor(ctx, width, height, theme);

    // Draw hexagonal decorations
    drawHexagons(ctx, width, height, theme);

    // Draw rounded border
    ctx.strokeStyle = theme.primary + "55";
    ctx.lineWidth = 3;
    roundRect(ctx, 12, 12, width - 24, height - 24, 24);
    ctx.stroke();

    // Fetch QR code image
    const qrResp = await fetch(`${API_BASE}/api/vehicles/${vehicle.id}/qrcode`);
    const qrBlob = await qrResp.blob();
    const qrImg = await loadImage(URL.createObjectURL(qrBlob));

    // Draw QR code with glow
    const qrSize = Math.min(width / 2, 260);
    const qrX = (width - qrSize) / 2;
    const qrY = (height / 2 - qrSize / 2) - 20;

    // Glow layers
    for (let i = 3; i >= 0; i--) {
        ctx.save();
        ctx.shadowColor = theme.primary;
        ctx.shadowBlur = i * 12;
        ctx.globalAlpha = 0.15 - i * 0.03;
        roundRect(ctx, qrX - 6, qrY - 6, qrSize + 12, qrSize + 12, 16);
        ctx.fillStyle = theme.primary;
        ctx.fill();
        ctx.restore();
    }

    // QR image
    ctx.drawImage(qrImg, qrX, qrY, qrSize, qrSize);

    // QR border
    ctx.strokeStyle = theme.primary + "bb";
    ctx.lineWidth = 2;
    roundRect(ctx, qrX - 4, qrY - 4, qrSize + 8, qrSize + 8, 14);
    ctx.stroke();

    // Draw owner info below QR
    let yPos = qrY + qrSize + 50;
    const centerX = width / 2;

    // Owner name
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    ctx.font = "bold 22px Microsoft YaHei, sans-serif";
    ctx.fillStyle = theme.primary;
    ctx.shadowColor = theme.primary;
    ctx.shadowBlur = 8;
    ctx.fillText(vehicle.ownerName || "车主", centerX, yPos);
    ctx.shadowBlur = 0;
    yPos += 40;

    // Plate number
    if (vehicle.plateNo) {
        ctx.font = "16px Microsoft YaHei, sans-serif";
        ctx.fillStyle = "rgba(200,230,220,0.8)";
        ctx.fillText("车牌: " + vehicle.plateNo, centerX, yPos);
        yPos += 30;
    }

    // Phone
    ctx.font = "14px Microsoft YaHei, sans-serif";
    ctx.fillStyle = "rgba(200,230,220,0.5)";
    const masked = vehicle.phone ? vehicle.phone.slice(0,3) + "****" + vehicle.phone.slice(7) : "";
    ctx.fillText("电话: " + masked, centerX, yPos);
    yPos += 35;

    // Comfort message (wrapped)
    const comfort = vehicle.comfortMessage || "";
    if (comfort) {
        wrapText(ctx, comfort, 30, yPos, width - 60, 14, "rgba(200,230,220,0.45)");
    }

    // Scan hint at bottom
    yPos = height - 50;
    ctx.font = "12px Microsoft YaHei, sans-serif";
    ctx.fillStyle = "rgba(0,255,200,0.35)";
    ctx.fillText("扫码联系车主 · SCAN TO CONTACT", centerX, yPos);
}

// ===== Helper Functions =====
function roundRect(ctx, x, y, w, h, r) {
    ctx.beginPath();
    ctx.moveTo(x + r, y);
    ctx.lineTo(x + w - r, y);
    ctx.quadraticCurveTo(x + w, y, x + w, y + r);
    ctx.lineTo(x + w, y + h - r);
    ctx.quadraticCurveTo(x + w, y + h, x + w - r, y + h);
    ctx.lineTo(x + r, y + h);
    ctx.quadraticCurveTo(x, y + h, x, y + h - r);
    ctx.lineTo(x, y + r);
    ctx.quadraticCurveTo(x, y, x + r, y);
    ctx.closePath();
}

function drawCornerDecor(ctx, w, h, theme) {
    ctx.strokeStyle = theme.primary + "88";
    ctx.lineWidth = 2;
    const len = 30;
    const offset = 20;
    // Top-left
    ctx.beginPath(); ctx.moveTo(offset, offset + len); ctx.lineTo(offset, offset); ctx.lineTo(offset + len, offset); ctx.stroke();
    // Top-right
    ctx.beginPath(); ctx.moveTo(w - offset - len, offset); ctx.lineTo(w - offset, offset); ctx.lineTo(w - offset, offset + len); ctx.stroke();
    // Bottom-left
    ctx.beginPath(); ctx.moveTo(offset, h - offset - len); ctx.lineTo(offset, h - offset); ctx.lineTo(offset + len, h - offset); ctx.stroke();
    // Bottom-right
    ctx.beginPath(); ctx.moveTo(w - offset - len, h - offset); ctx.lineTo(w - offset, h - offset); ctx.lineTo(w - offset, h - offset - len); ctx.stroke();
}

function drawHexagons(ctx, w, h, theme) {
    ctx.strokeStyle = theme.grid;
    ctx.lineWidth = 1;
    const positions = [
        [60, 60], [w - 60, 60], [60, h - 60], [w - 60, h - 60]
    ];
    positions.forEach(([cx, cy]) => {
        ctx.beginPath();
        for (let i = 0; i < 6; i++) {
            const angle = Math.PI / 3 * i - Math.PI / 6;
            const x = cx + 25 * Math.cos(angle);
            const y = cy + 25 * Math.sin(angle);
            if (i === 0) ctx.moveTo(x, y);
            else ctx.lineTo(x, y);
        }
        ctx.closePath();
        ctx.stroke();
    });
}

function wrapText(ctx, text, x, y, maxWidth, fontSize, color) {
    ctx.font = `${fontSize}px Microsoft YaHei, sans-serif`;
    ctx.fillStyle = color;
    ctx.textAlign = "left";
    ctx.textBaseline = "top";
    const lineHeight = fontSize + 6;
    let line = "";
    let currentY = y;

    for (let i = 0; i < text.length; i++) {
        const testLine = line + text[i];
        if (ctx.measureText(testLine).width > maxWidth && line.length > 0) {
            ctx.fillText(line, x, currentY);
            line = text[i];
            currentY += lineHeight;
        } else {
            line = testLine;
        }
    }
    if (line.length > 0) ctx.fillText(line, x, currentY);
}

function darken(hex, amount) {
    let r = parseInt(hex.slice(1,3), 16);
    let g = parseInt(hex.slice(3,5), 16);
    let b = parseInt(hex.slice(5,7), 16);
    r = Math.max(0, r - amount);
    g = Math.max(0, g - amount);
    b = Math.max(0, b - amount);
    return `rgb(${r},${g},${b})`;
}

function loadImage(src) {
    return new Promise((resolve, reject) => {
        const img = new Image();
        img.onload = () => resolve(img);
        img.onerror = reject;
        img.src = src;
    });
}

// ===== Download Sticker =====
document.getElementById("downloadBtn").addEventListener("click", () => {
    const canvas = document.getElementById("stickerCanvas");
    const link = document.createElement("a");
    const vehicle = currentVehicle;
    link.download = `挪车码_${vehicle?.plateNo || "临时"}_${Date.now()}.png`;
    link.href = canvas.toDataURL("image/png");
    link.click();
    showToast("贴图已下载!");
});

// ===== Preview =====
document.getElementById("previewBtn").addEventListener("click", () => {
    if (currentVehicle) {
        window.open(`${API_BASE}/move-car?id=${currentVehicle.id}`, "_blank");
    }
});

// ===== Size Change Re-render =====
document.getElementById("sizeSelect").addEventListener("change", () => {
    if (currentVehicle) {
        renderStickerOnCanvas(currentVehicle);
    }
});

// ===== Toast =====
function showToast(msg) {
    let toast = document.getElementById("toast");
    if (!toast) {
        toast = document.createElement("div");
        toast.id = "toast";
        toast.className = "toast";
        document.body.appendChild(toast);
    }
    toast.textContent = msg;
    toast.classList.add("show");
    setTimeout(() => toast.classList.remove("show"), 2500);
}

// ===== Init: Check for saved vehicle =====
(function init() {
    const saved = localStorage.getItem("last_vehicle");
    if (saved) {
        try {
            const v = JSON.parse(saved);
            document.getElementById("ownerName").value = v.ownerName || "";
            document.getElementById("plateNo").value = v.plateNo || "";
            document.getElementById("phone").value = v.phone || "";
            document.getElementById("comfortMessage").value = v.comfortMessage || "";
        } catch(e) {}
    }
})();
