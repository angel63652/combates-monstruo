# Combates Monstruo 🔥💧🌿

Juego de combates por turnos estilo Pokémon para Android, en **Java** y sin librerías externas:

- **Cero dependencias** — ni AndroidX, ni librerías externas. Solo el SDK de Android.
- **Interfaz en XML editable en Android Studio** — el menú, la selección de equipo y el combate están en `res/layout/` (más textos en `res/values/strings.xml` y colores en `res/values/colors.xml`), así que se pueden tocar en el editor de diseño.
- **Gráficos con Canvas** — el escenario, las criaturas, las barras de vida y las animaciones se dibujan a mano (vistas `VistaBatalla` y `VistaCriatura`).

## Cómo compilarlo e instalarlo

### Opción A: instalar el APK ya hecho (lo más rápido)

En la [última versión publicada](https://github.com/angel63652/combates-monstruo/releases/latest) hay un APK listo. Descárgalo en el móvil, ábrelo y acepta instalar apps de origen desconocido.

### Opción B: Android Studio

1. Abre Android Studio → **Open** → selecciona la carpeta `JuegoCombates`.
2. Espera a que sincronice Gradle. El proyecto ya trae el *wrapper* incluido, así que Android Studio descarga solo la versión de Gradle correcta (8.7).
3. Conecta tu móvil con la **depuración USB activada** (Ajustes → Opciones de desarrollador) y pulsa **Run ▶**.

### Opción C: línea de comandos

Con el SDK de Android instalado y la variable `ANDROID_HOME` (o un archivo `local.properties` con `sdk.dir`) apuntando a él, usa el *wrapper* del proyecto (no hace falta tener Gradle instalado):

```
cd JuegoCombates
./gradlew assembleDebug        # en Windows: gradlew.bat assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

El wrapper fija Gradle 8.7, la versión que necesita el plugin de Android (AGP 8.2.2).

Requisitos: Android 7.0 (API 24) o superior.

## Cómo se juega

- Cada combate enfrenta tu equipo de **3 criaturas** contra el de un rival controlado por la IA. Los equipos son aleatorios en cada partida.
- Elige uno de los **4 movimientos** de tu criatura activa. El orden de acción lo decide la **velocidad**.
- Botón **Equipo** para cambiar de criatura (gasta el turno). Si la tuya se debilita, el cambio es gratuito.
- Ganas cuando las 3 criaturas rivales se debilitan.

## Mecánicas

- **6 tipos** (Normal, Fuego, Agua, Planta, Eléctrico, Roca) con tabla de efectividades (×2, ×1, ×0.5).
- **STAB**: los movimientos del mismo tipo que la criatura hacen un 50% más de daño.
- **Golpes críticos** (1/12, daño ×1.5), **precisión** por movimiento y **PP** limitados (sin PP → Forcejeo).
- Fórmula de daño inspirada en la clásica de Pokémon, con variación aleatoria del 85–100%.
- **IA rival**: elige el ataque con mayor daño esperado (con algo de aleatoriedad), se cura cuando está baja de vida y, al caer una criatura, envía la que mejor tipo tenga contra la tuya.

## Editar la interfaz en Android Studio

Abre el proyecto y usa el editor de diseño (pestaña **Design**) sobre estos archivos:

- `res/layout/activity_menu.xml` — el menú de inicio.
- `res/layout/activity_seleccion.xml` + `res/layout/item_criatura.xml` — elegir equipo.
- `res/layout/activity_main.xml` — la pantalla de combate.
- `res/values/strings.xml` — todos los textos.
- `res/values/colors.xml` — los colores de la interfaz.
- `res/values/dimens.xml` — tamaños y márgenes.

El escenario del combate (los monstruos, barras y animación) se dibuja por
código con Canvas, así que ese dibujo se cambia en `ui/DibujoCriatura.java` y
`ui/VistaBatalla.java`. Los colores de cada criatura están en `modelo/Tipo.java`.

## Estructura del código

```
app/src/main/
├── java/com/juego/combates/
│   ├── MenuActivity.java         # Menú de inicio
│   ├── SeleccionActivity.java    # Elegir equipo de 3 criaturas
│   ├── MainActivity.java         # Flujo del combate
│   ├── modelo/
│   │   ├── Tipo.java             # Tipos, colores y tabla de efectividades
│   │   ├── Movimiento.java       # Ataques y curaciones con PP
│   │   ├── Criatura.java         # Estadísticas y estado
│   │   └── Fabrica.java          # Las 6 especies y equipos aleatorios
│   ├── batalla/
│   │   └── Batalla.java          # Motor de turnos, daño, IA rival
│   └── ui/
│       ├── DibujoCriatura.java   # Dibujo de una criatura (Canvas)
│       ├── VistaCriatura.java    # Vista de una criatura suelta
│       └── VistaBatalla.java     # Escenario del combate (Canvas)
└── res/
    ├── layout/                   # Pantallas (editables en el diseñador)
    ├── values/                   # strings, colors, dimens, themes
    ├── drawable/                 # Fondos y botones
    └── mipmap-*/                 # Icono de la app
```
