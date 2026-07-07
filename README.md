# Combates Monstruo 🔥💧🌿

Juego de combates por turnos estilo Pokémon para Android, escrito en **Java puro**:

- **Cero dependencias** — ni AndroidX, ni librerías externas. Solo el SDK de Android.
- **Sin XML de layouts** — toda la interfaz se construye en código Java.
- **Gráficos con Canvas** — escenario, criaturas, barras de vida y animaciones dibujadas a mano.

## Cómo compilarlo e instalarlo

### Opción A: Android Studio (recomendada)

1. Abre Android Studio → **Open** → selecciona la carpeta `JuegoCombates`.
2. Espera a que sincronice Gradle (descargará el wrapper automáticamente).
3. Conecta tu móvil con la **depuración USB activada** (Ajustes → Opciones de desarrollador) y pulsa **Run ▶**.

### Opción B: línea de comandos

Con el SDK de Android y Gradle instalados:

```
cd JuegoCombates
gradle assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

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

## Estructura del código

```
app/src/main/java/com/juego/combates/
├── MainActivity.java          # UI en código puro + flujo del combate
├── modelo/
│   ├── Tipo.java              # Tipos y tabla de efectividades
│   ├── Movimiento.java        # Ataques y curaciones con PP
│   ├── Criatura.java          # Estadísticas y estado
│   └── Fabrica.java           # Las 6 especies y equipos aleatorios
├── batalla/
│   └── Batalla.java           # Motor de turnos, daño, IA rival
└── ui/
    └── VistaBatalla.java      # Escenario dibujado con Canvas
```
