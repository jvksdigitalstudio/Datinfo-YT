# YouTube Data — Android Nativo

App Android 100% nativa (Kotlin + XML) para analizar canales de YouTube públicos.

## 📁 Estructura del proyecto

```
youtube-android/
├── .github/workflows/build.yml       ← GitHub Actions (compila el APK)
├── app/
│   ├── build.gradle                  ← Dependencias
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/youtubedata/app/
│       │   ├── data/
│       │   │   ├── api/              ← Retrofit + API service
│       │   │   ├── model/            ← Modelos de datos
│       │   │   └── repository/       ← Lógica de negocio
│       │   ├── ui/
│       │   │   ├── MainActivity.kt   ← Pantalla búsqueda
│       │   │   ├── MainViewModel.kt  ← ViewModel
│       │   │   └── channel/
│       │   │       ├── ChannelActivity.kt
│       │   │       ├── VideosAdapter.kt
│       │   │       └── SocialsAdapter.kt
│       │   └── utils/
│       │       ├── SocialParser.kt   ← Extrae redes sociales
│       │       ├── FormatUtils.kt    ← Formatea números/fechas
│       │       ├── PrefsManager.kt   ← API Key guardada
│       │       └── ExportUtils.kt    ← Exportar JSON/TXT
│       └── res/                      ← Layouts, colores, drawables
├── build.gradle
├── settings.gradle
└── gradlew
```

## 🚀 Compilar en GitHub Actions

### Paso 1 — Subir el proyecto
```bash
cd youtube-android
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/TU_USUARIO/youtube-data-android.git
git push -u origin main
```

### Paso 2 — GitHub Actions compila automáticamente
Cada push a `main` lanza el workflow `.github/workflows/build.yml` y genera el APK.

Descárgalo en: **Actions → Build Android APK → Artifacts → YouTubeData-debug-X**

### Paso 3 — APK firmado (opcional, para Play Store)
Agrega estos Secrets en GitHub → Settings → Secrets → Actions:

| Secret | Descripción |
|--------|-------------|
| `KEYSTORE_BASE64` | Tu keystore en base64: `base64 -w0 mi_keystore.jks` |
| `KEYSTORE_PASSWORD` | Contraseña del keystore |
| `KEY_ALIAS` | Alias de la clave |
| `KEY_PASSWORD` | Contraseña de la clave |

## 🔑 API Key de YouTube

1. Ve a [console.cloud.google.com](https://console.cloud.google.com)
2. Crea un proyecto
3. Habilita **YouTube Data API v3**
4. Crea una API Key en Credenciales
5. La app te la pide al primer uso — la guarda automáticamente

## 📱 Qué extrae la app

| Dato | Fuente API |
|------|-----------|
| Nombre, handle, URL | `snippet` |
| Foto de perfil (avatar) | `snippet.thumbnails.maxres` |
| Banner/portada HD | `brandingSettings.image.bannerExternalUrl` |
| País con bandera | `snippet.country` |
| Fecha de creación | `snippet.publishedAt` |
| Suscriptores | `statistics.subscriberCount` |
| Videos subidos | `statistics.videoCount` |
| Vistas totales | `statistics.viewCount` |
| Keywords/tags | `brandingSettings.channel.keywords` |
| Categorías temáticas | `topicDetails.topicCategories` |
| Redes sociales | Parseadas de la descripción |
| Videos recientes (12) | `playlistItems` + `videos` |
| Likes, vistas por video | `videos.statistics` |

## 🛠️ Compilar localmente

Requisitos: Android Studio o JDK 17 + Android SDK

```bash
./gradlew assembleDebug
# APK en: app/build/outputs/apk/debug/app-debug.apk
```
