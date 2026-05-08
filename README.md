# SeriesGal TV (Android TV)

App Android TV completa en Kotlin + Compose TV, conectada al backend real definido en el contrato.

## Stack técnico

- Kotlin
- Jetpack Compose TV (`androidx.tv:tv-material`)
- Arquitectura MVVM + Repository
- Retrofit + OkHttp + kotlinx.serialization
- Media3 ExoPlayer + HLS + DownloadManager offline
- Coil para imágenes
- DataStore (token/settings)
- Room (estado de descargas y cache local de progreso)
- Coroutines + Flow

## Requisitos

- Android Studio Ladybug o superior
- JDK 17
- Android SDK Platform 35
- Android TV Emulator o dispositivo Android TV
- Conectividad a:
  - `https://servidor.tail0dc0c0.ts.net/api`
  - `https://servidor.tail0dc0c0.ts.net`

## Configuración centralizada del servidor

En `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://servidor.tail0dc0c0.ts.net/api/\"")
buildConfigField("String", "WEB_BASE_URL", "\"https://servidor.tail0dc0c0.ts.net/\"")
```

Uso centralizado en:

- `com.seriegel.tv.core.config.ServerConfig`

### Cómo cambiar las base URLs

1. Editar `app/build.gradle.kts` en los `buildConfigField`.
2. Hacer Gradle Sync.
3. Recompilar.

## Estructura principal

```text
app/
  data/        # remote/local/repository
  domain/      # modelos y contratos de repositorio
  ui/          # pantallas + viewmodels + navegación
  player/      # player factory + coordinator
  downloads/   # Media3 download service/infrastructure
  core/        # config/network/errores/utilidades
```

## Backend contract implementado

### Auth
- `POST /login`
- `POST /register`
- `GET /me` (si 401/403 => logout local; si error red => mantener sesión)

### Catálogo
- `GET /catalog.json`
- Normalización de series:
  - soporta `seasons[]`
  - soporta `episodes[]` directo (crea temporada virtual)

### Favoritos
- `GET /favorites` (parser tolerante `seriesId` / `series_id`)
- `POST /favorites` (`toggle`)

### Progreso
- `POST /progress`
- `GET /continue-watching` (parser tolerante snake_case/camelCase)
- `GET /progress/{seriesId}/{episodeId}`
- `GET /series-progress/{seriesId}`

## Funcionalidades implementadas

### Sesión
- Login/Register.
- Persistencia de token en DataStore.
- Validación automática de sesión con `/me`.
- Logout manual desde perfil.

### Home Android TV
- Hero dinámico auto-rotando.
- Secciones:
  - Seguir viendo
  - Pendientes por terminar
  - Porque viste X
  - Recientemente añadidos
  - Más vistos esta semana
  - Series / Películas
- Navegación por foco D-pad.

### Detalles de serie/película
- Serie:
  - Temporadas + episodios
  - Progreso por episodio
  - Reanudar / inicio
  - Descarga por episodio con estados
- Película:
  - Reproducir / reanudar
  - Descarga offline con selección de calidad

### Player
- Media3 ExoPlayer HLS online.
- Reproducción con cache/offline cuando está descargado.
- Guardado periódico de progreso (cada 15s) + snapshot al salir.
- Auto siguiente episodio con cuenta atrás (10s), cancelar o reproducir ahora.

### Descargas offline
- Media3 DownloadManager + DownloadService.
- Estados de descarga y progreso.
- Panel de descargas:
  - Memoria usada
  - Elementos descargados
  - Reproducir descargado
  - Borrar individual / borrar todo
- Cabecera Home:
  - Icono de descargas activas
  - Progreso agregado circular
  - Badge con cantidad activa
  - Panel rápido y cancelar por ítem activo

## Build y ejecución

### Desde Android Studio

1. `File > Open` y seleccionar el proyecto.
2. Esperar Gradle Sync.
3. Seleccionar módulo `app`.
4. Ejecutar en emulador Android TV o dispositivo real.

### Desde terminal

```bash
./gradlew :app:assembleDebug
```

En Windows:

```bat
gradlew.bat :app:assembleDebug
```

## Validación end-to-end recomendada

1. Abrir app en launcher TV.
2. Login/Register exitoso.
3. Home carga hero y secciones.
4. Entrar a detalle de serie y reproducir episodio.
5. Salir del player y confirmar progreso guardado.
6. Volver a episodio y comprobar reanudación.
7. Marcar/quitar favorito y validar persistencia.
8. Descargar episodio/película.
9. Ver progreso y estado en panel de descargas.
10. Reproducir contenido descargado.
11. Borrar descarga individual y luego borrar todo.

## Notas de depuración

- Logging de red habilitado con OkHttp (`BASIC`).
- Repositorios principales emiten logs (`AuthRepository`, `CatalogRepository`, `DownloadsRepository`).
