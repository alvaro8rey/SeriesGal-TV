# SeriesGal TV (Android TV)

Base inicial de la app Android TV con:

- Kotlin
- Jetpack Compose para TV
- Arquitectura MVVM + Repository
- Retrofit + OkHttp + kotlinx.serialization
- Media3 ExoPlayer
- Coil
- DataStore + Room
- Coroutines + Flow

## Requisitos

- Android Studio Ladybug o superior
- JDK 17
- Android SDK Platform 35
- Android TV Emulator o dispositivo Android TV

## Configuracion centralizada de servidor

Se define en `app/build.gradle.kts` via `BuildConfig`:

- `API_BASE_URL = "https://servidor.tail0dc0c0.ts.net/api/"`
- `WEB_BASE_URL = "https://servidor.tail0dc0c0.ts.net/"`

Consumo centralizado en:

- `com.seriegel.tv.core.config.ServerConfig`

## Como abrir y compilar

1. Abrir Android Studio.
2. `File > Open` y seleccionar la carpeta del proyecto.
3. Esperar sync de Gradle.
4. Seleccionar target TV (`app`).
5. Ejecutar `Run 'app'`.

## Como validar base Fase 1

1. La app abre en launcher TV (leanback).
2. Muestra pantalla inicial con botones foco navegable por D-pad.
3. Se puede navegar a pantalla de perfil.
4. El modulo `app` compila en `debug` y `release` con SDK configurado.
