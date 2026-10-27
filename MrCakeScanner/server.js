
const express = require("express");
const app = express();

app.use(express.json());

let ultimoCodigo = null;

// Página del escáner
app.get("/", (req, res) => {
    res.send(`<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Mr Cake Scanner</title>

<style>
body {
    font-family: Arial, sans-serif;
    text-align: center;
    padding: 20px;
    background: #fff8f0;
    color: #422719;
}
h1 { color: #8b4513; }
video {
    width: 100%;
    max-width: 420px;
    border-radius: 12px;
    background: #222;
}
button {
    padding: 14px 20px;
    margin: 8px;
    font-size: 17px;
    border: none;
    border-radius: 8px;
    cursor: pointer;
}
#iniciar { background: #27834a; color: white; }
#detener { background: #b83232; color: white; }
#resultado {
    font-size: 20px;
    font-weight: bold;
    overflow-wrap: anywhere;
}
</style>

<script src="https://unpkg.com/@zxing/browser@0.1.5/umd/zxing-browser.min.js"></script>
</head>

<body>
<h1>Mr Cake</h1>
<h2>Lector de códigos de barras</h2>

<video id="video" autoplay playsinline muted></video>

<p id="resultado">Pulsa Iniciar escáner para comenzar.</p>

<button id="iniciar">Iniciar escáner</button>
<button id="detener">Detener escáner</button>

<script>
window.addEventListener("DOMContentLoaded", () => {
    const video = document.getElementById("video");
    const resultado = document.getElementById("resultado");
    const botonIniciar = document.getElementById("iniciar");
    const botonDetener = document.getElementById("detener");

    let lector = null;
    let control = null;
    let iniciando = false;
    let ultimoEnviado = "";
    let tiempoUltimo = 0;

    function detenerCamara() {
        if (control) {
            control.stop();
            control = null;
        }

        if (video.srcObject) {
            video.srcObject.getTracks().forEach(pista => pista.stop());
            video.srcObject = null;
        }

        botonIniciar.disabled = false;
        iniciando = false;
    }

    botonIniciar.addEventListener("click", async () => {
        if (iniciando || control) return;

        if (!window.ZXingBrowser) {
            resultado.textContent =
                "No se cargó ZXing. Recarga la página e inténtalo otra vez.";
            return;
        }

        iniciando = true;
        botonIniciar.disabled = true;
        resultado.textContent = "Solicitando cámara trasera...";

        try {
            if (!lector) {
                lector = new ZXingBrowser.BrowserMultiFormatReader();
            }

            control = await lector.decodeFromConstraints(
                {
                    audio: false,
                    video: {
                        facingMode: { ideal: "environment" }
                    }
                },
                video,
                async (lectura, error) => {
                    if (!lectura) return;

                    const codigo = lectura.getText();
                    const ahora = Date.now();

                    if (
                        codigo === ultimoEnviado &&
                        ahora - tiempoUltimo < 2000
                    ) {
                        return;
                    }

                    ultimoEnviado = codigo;
                    tiempoUltimo = ahora;
                    resultado.textContent = "Código leído: " + codigo;

                    try {
                        const respuesta = await fetch("/codigo", {
                            method: "POST",
                            headers: {
                                "Content-Type": "application/json"
                            },
                            body: JSON.stringify({ codigo })
                        });

                        if (!respuesta.ok) {
                            resultado.textContent =
                                "Código leído, pero no se pudo enviar.";
                        }
                    } catch (e) {
                        resultado.textContent =
                            "Código leído: " + codigo +
                            ". Error al enviarlo al servidor.";
                    }
                }
            );

            resultado.textContent =
                "Escáner activo. Apunta al código de barras.";
        } catch (e) {
            detenerCamara();
            resultado.textContent =
                "Error al abrir la cámara: " + e.message;
        } finally {
            iniciando = false;
            botonIniciar.disabled = false;
        }
    });

    botonDetener.addEventListener("click", () => {
        detenerCamara();
        resultado.textContent = "Escáner detenido.";
    });
});
</script>
</body>
</html>`);
});

// Recibir el código leído
app.post("/codigo", (req, res) => {
    ultimoCodigo = String(req.body.codigo || "");
    console.log("Código escaneado:", ultimoCodigo);
    res.json({ ok: true });
});

// Consultar el último código
app.get("/codigo", (req, res) => {
    const codigo = ultimoCodigo || "";
    ultimoCodigo = null;

    res.type("text/plain").send(codigo);
});

app.listen(8080, "0.0.0.0", () => {
    console.log("Mr Cake Scanner iniciado en puerto 8080");
});
