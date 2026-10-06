# Proyecto #1 — Gestor de Procesos (Sistemas Operativos)

# Integrantes:

**2024129001 - Poll Anthony Garro Vargas**

# Estado del proyecto: Completo

Todos los requisitos del enunciado están implementados (ver [Objetivos alcanzados](#objetivos-alcanzados)).

# Enlace del video:

_(pendiente: agregar aquí el enlace de YouTube)_

---

# Documentación:

# Mini PC — Gestor de Procesos

**Curso:** IC-6600 Principios de Sistemas Operativos

**Profesor:** Ing. Cristian Campos Agüero

**Autor:** Poll Anthony Garro Vargas — 2024129001

**Universidad:** Instituto Tecnológico de Costa Rica (TEC)

## Descripción general

Aplicación de escritorio en Java (NetBeans) que simula una minicomputadora con
**CPU, memoria principal, almacenamiento secundario (disco), pantalla y teclado**,
junto con un pequeño **sistema operativo** que administra la ejecución de varios
programas escritos en un mini ensamblador (archivos `.asm`).

El sistema carga uno o varios programas, los guarda en disco, crea un proceso
con su **BCP** por cada uno, los pasa a memoria cuando hay espacio y los ejecuta
con planificación **FCFS**, mostrando en todo momento el estado de la CPU, la
memoria, el disco, los BCP y la lista de trabajos.

---

## Objetivos alcanzados

- Carga de uno o varios archivos `.asm` a la vez, con validación de sintaxis y mensajes claros (archivo, línea y motivo).
- Los programas se guardan en disco, con un **índice** (nombre, dirección y tamaño) en los primeros registros.
- Creación de un **BCP** por proceso con todos los campos del enunciado: estado, PC, registros (AC, AX, BX, CX, DX, IR), pila de 5 con control de desbordamiento, CPU, tiempo de inicio, tiempo empleado, archivos abiertos, enlace al siguiente BCP, base, alcance y prioridad.
- **Modelo de 7 estados** (Stallings, cap. 3): **Nuevo, Preparado, Ejecución, En espera, Suspendido en espera, Suspendido preparado y Finalizado**.
- Los BCP se guardan en la **zona del S.O. de la memoria principal**, cada uno con la dirección de memoria del siguiente BCP.
- **Lista de trabajos**, **planificador de trabajos** y **lista de procesos**.
- 1 CPU que administra **hasta 5 procesos** en memoria; si no hay espacio, el proceso espera hasta que se libere.
- Las **21 instrucciones** de la tabla con sus **pesos** (incluye `INT 21H` para manejo de archivos).
- Ejecución **paso a paso** (cada clic = 1 segundo de CPU) y **automática** (1 segundo real por paso).
- Interrupciones / llamadas al sistema: `INT 20H`, `INT 10H` (pantalla), `INT 09H` (teclado 0–255) e `INT 21H` (archivos).
- **Despachador** y **cambios de contexto** (al terminar, al pedir teclado o ante un error).
- **Memoria virtual**: si un programa no cabe completo en RAM, la parte restante se guarda en la memoria virtual del disco.
- **Desbordamiento de memoria**: se rechaza un programa más grande que la RAM de usuario + la memoria virtual.
- **Planificador FCFS**.
- **Protección y seguridad** (ver sección propia).
- Visualización del BCP actual, de los BCP en memoria, de los registros (PC, IR, AC…), del tiempo de ejecución y de la lista de trabajos con sus cambios de estado.
- Configuración de memoria principal, zona del S.O., disco y memoria virtual desde un **archivo de texto** (`config.txt`) y desde un menú de configuración.
- **Estadísticas**: por proceso, hora:minuto de inicio, hora:minuto final, duración en segundos y tiempo de CPU.

## Objetivos no alcanzados

- Ninguno de los requisitos funcionales del enunciado quedó sin implementar.

### Decisiones de diseño a tomar en cuenta

- El enunciado menciona "7 estados": se siguió el modelo de 7 estados del libro de Stallings, donde *Suspendido* se divide en **Suspendido en espera** (bloqueado y fuera de memoria) y **Suspendido preparado** (ya ocurrió su evento pero sigue fuera de memoria).
- El "peso" de `INT 09H` se tomó como 1 segundo; luego el proceso queda **En espera** hasta que el usuario ingrese el valor.
- Los procesos después del 5.º **esperan** en la lista de trabajos (no van a memoria virtual); la memoria virtual se usa cuando un programa no cabe completo en la RAM.

---

## Cómo ejecutar

1. Abrir el proyecto en **NetBeans**.
2. Ejecutar `minipc.Main` (F6).
3. **Cargar archivos** → elegir uno o varios `.asm`.
4. **Paso a paso** (1 segundo de CPU por clic) o **Ejecutar** (automático).

La configuración se lee de `config.txt` en la raíz del proyecto. Si no existe, se
crea con los valores por defecto. Al cerrar el programa, el archivo vuelve a los
valores por defecto.

```
memoriaPrincipal=256
memoriaSistema=64
almacenamientoSecundario=512
memoriaVirtual=64
```

---

## Estructura de paquetes

```
minipc
├── Main.java
├── gui
│   └── VentanaPrincipal.java
├── hardware
│   ├── CPU.java
│   ├── Memoria.java
│   ├── Disco.java
│   └── Pesos.java
├── modelo
│   ├── GestorProcesos.java
│   ├── Planificador.java
│   ├── Despachador.java
│   ├── BCP.java
│   ├── ColaTrabajo.java
│   ├── Instruccion.java
│   └── TipoOperacion.java
└── util
    ├── ParserASM.java
    ├── ConfiguracionSistema.java
    └── ConversorBinario.java
```

- **hardware**: la máquina simulada (CPU, memoria principal, disco y tabla de pesos).
- **modelo**: el sistema operativo (gestor de procesos, planificador, despachador, BCP y colas).
- **util**: lectura/validación de los `.asm` y configuración.
- **gui**: la interfaz gráfica. Solo muestra datos y reacciona a los botones; toda la lógica está en `GestorProcesos`.

### Orden de dependencias

```
gui.VentanaPrincipal  ->  modelo.GestorProcesos, util.ParserASM, util.ConfiguracionSistema
modelo.GestorProcesos ->  hardware.CPU, hardware.Memoria, hardware.Disco,
                          modelo.Planificador, modelo.Despachador, modelo.BCP
modelo.Planificador   ->  modelo.ColaTrabajo, modelo.BCP, hardware.Memoria, hardware.Disco
modelo.Despachador    ->  hardware.CPU, modelo.BCP
hardware.CPU          ->  hardware.Memoria, hardware.Disco, hardware.Pesos, modelo.BCP
util.ParserASM        ->  modelo.Instruccion, modelo.TipoOperacion, util.ConversorBinario
```

---

## Flujo de un programa

```
Cargar .asm ──► ParserASM valida ──► se guarda en Disco ──► se crea BCP
      │
      ▼
[Lista de trabajos: Nuevo]
      │  Planificador de trabajos (hay memoria y < 5 procesos)
      ▼
[Lista de procesos: Preparado] ──FCFS──► Despachador ──► CPU: Ejecución
      ▲                                                      │
      │                   INT 09H (teclado) ◄────────────────┤
      └── valor ingresado ── [En espera / Suspendido]        │
                                                             ▼
                                   INT 20H o error ──► Finalizado (libera memoria)
```

---

## Mapa de memoria principal (256 por defecto)

| Posiciones | Uso |
|---|---|
| 0 – 63 (25 %) | **Zona del S.O.**: un BCP por posición. Ej.: `BCP3\|Ejecución\|PC=85\|AC=0\|…\|Sig=4` (`Sig` = dirección del siguiente BCP) |
| 64 – 255 | **Zona de usuario**: instrucciones de los programas, cada proceso en un bloque contiguo `[base, base + alcance)` |

## Mapa del disco (512 por defecto)

| Posiciones | Uso |
|---|---|
| 0 – 31 | **Índice**: `nombre,dirección,tamaño` de cada archivo |
| 32 – 447 | **Archivos**: programas `.asm` y archivos creados con `INT 21H` |
| 448 – 511 | **Memoria virtual** (64) |

---

## Instrucciones soportadas

| Instrucción | Descripción | Peso |
|---|---|---|
| `LOAD AX` | AC = AX | 2 |
| `STORE BX` | BX = AC | 2 |
| `MOV BX, AX` / `MOV BX, 5` | Mueve un registro o un valor al destino | 1 |
| `ADD BX` / `SUB BX` | AC = AC ± BX | 3 |
| `INC` / `INC AX` | Incrementa AC o el registro | 1 |
| `DEC` / `DEC AX` | Decrementa AC o el registro | 1 |
| `SWAP AX, BX` | Intercambia los registros | 1 |
| `INT 20H` | Finaliza el programa | 2 |
| `INT 10H` | Imprime DX en la pantalla | 2 |
| `INT 09H` | Lee del teclado un número 0–255 y lo guarda en DX | 1 + espera |
| `INT 21H` | Archivos: AH = `3CH` crear, `3DH` abrir, `4DH` leer, `40H` escribir, `41H` eliminar. DX = nombre del archivo, AL = dato | 5 |
| `JMP ±n` | Salto relativo | 2 |
| `CMP R1, R2` | Compara dos registros | 2 |
| `JE ±n` / `JNE ±n` | Salto si es igual / distinto (con control de desbordamiento) | 2 |
| `PARAM v1, v2, v3` | Mete hasta 3 valores en la pila | 3 |
| `PUSH AX` / `POP AX` | Pila del proceso | 1 |

Cada clic de "Paso a paso" equivale a 1 segundo de CPU. Una instrucción se
ejecuta cuando se cumplen los segundos de su peso (`CPU.tick()`).

Para `INT 21H`, el nombre del archivo se carga con `MOV DX, "datos.txt"` y los
valores hexadecimales se escriben como `MOV AH, 3CH`.

### Validación del archivo `.asm` (`ParserASM`)

- Extensión `.asm` y contenido de texto plano.
- Operación existente, registros válidos (AX, BX, CX, DX, AH, AL) y cantidad correcta de operandos.
- Formato estricto: `OPERACION op1, op2` — una sola coma entre operandos; se rechazan comas repetidas, al inicio o al final, y operandos sin coma.
- Valores en 8 bits con signo (-127 a 127); `PARAM` con máximo 3 valores.
- Interrupciones válidas: `20H`, `10H`, `09H`, `21H`.
- Se permiten comentarios con `;`.
- El error indica archivo, línea y motivo. Ej.: `prueba.asm, línea 3 ("MOV BX, 3,"): coma de más al final de la línea`.

---

## Clases principales

### `modelo.GestorProcesos`
Punto de entrada del "sistema operativo". Conecta CPU, memoria, disco,
planificador y despachador.

| Método | Qué hace |
|---|---|
| `cargarPrograma(instrucciones, nombre)` | Valida el tamaño (desbordamiento de memoria), guarda el programa en disco, crea el BCP y lo pone en la lista de trabajos |
| `ejecutarUnPaso()` | 1 segundo de CPU: despacha si la CPU está libre, ejecuta `cpu.tick()` y maneja fin de programa, `INT 09H` y errores (cambio de contexto) |
| `ejecutarTodo()` | Repite `ejecutarUnPaso()` hasta terminar o hasta que todos esperen teclado |
| `ingresarTeclado(valor)` | Entrega el valor (0–255) al proceso en espera: va a DX y el proceso vuelve a la cola |
| `getEstadisticas()` | Inicio, final, duración y tiempo de CPU de cada proceso |

### `modelo.Planificador`
| Método | Qué hace |
|---|---|
| `prepararSiguientes()` | **Planificador de trabajos**: pasa programas del disco a memoria (máx. 5); si no caben completos usa memoria virtual; si un proceso en espera bloquea a otro, lo **suspende** |
| `siguienteProceso()` | **Planificador de CPU (FCFS)**: entrega el primer proceso preparado |
| `enEspera()` / `salirDeEspera()` | Manejo de procesos bloqueados por teclado |
| `terminar()` | Libera la memoria, la memoria virtual y la posición del BCP |

### `modelo.Despachador`
| Método | Qué hace |
|---|---|
| `despachar(bcp)` | Pone el proceso en la CPU y restaura sus registros desde el BCP |
| `capturar(bcp)` | Guarda los registros de la CPU en el BCP |

### `modelo.BCP`
Bloque de Control de Proceso. `capturaEstado(cpu)` y `restaurarEstado(cpu)`
implementan el **cambio de contexto**; `push()`/`pop()` manejan la pila de 5;
`aTextoMemoria()` genera la representación que se guarda en la zona del S.O.

### `hardware.CPU`
Registros PC, IR, AC, AX, BX, CX, DX, AH, AL y bandera de CMP. Ciclo
**fetch → decode → execute** en `tick()`. `fetch()` valida el acceso a memoria
y, si el PC cae en la parte del programa que está en memoria virtual, lee la
instrucción desde el disco.

### `hardware.Memoria` / `hardware.Disco`
Memoria principal (con zona S.O. y zona de usuario, búsqueda de huecos y
validación de acceso) y almacenamiento secundario (índice, archivos y memoria
virtual).

### `util.ParserASM` / `util.ConfiguracionSistema`
Validación de los `.asm` y lectura/escritura de `config.txt`.

---

## Memoria virtual

1. Si el programa cabe completo en RAM, se carga completo.
2. Si no cabe, se carga en el hueco más grande lo que quepa y **el resto se escribe en la memoria virtual** del disco (448–511).
3. Cuando el PC llega a esa parte, la CPU lee las instrucciones desde el disco.
4. Si no hay espacio ni en RAM ni en memoria virtual, el proceso espera en la lista de trabajos.
5. Si el programa es más grande que la RAM de usuario + la memoria virtual, se rechaza por **desbordamiento de memoria**.

## Estado Suspendido

Si un proceso está **En espera** del teclado y otro proceso no puede entrar a
memoria, el S.O. **suspende** al que espera (**Suspendido en espera**): lo saca de
memoria (su programa sigue en disco) y libera su espacio. Al recibir su valor pasa
a **Suspendido preparado**, vuelve a la lista de trabajos y se recarga cuando haya
espacio, continuando desde donde iba (con su PC reubicado a la nueva base).

---

## Protección y seguridad (estrategia)

1. **Base y límite**: en cada `fetch()` se valida que el PC esté dentro de `[base, base + alcance)` del proceso (`Memoria.validarAcceso`).
2. **Saltos controlados**: `JMP`/`JE`/`JNE` no pueden salir del espacio del proceso (desbordamiento).
3. **Pila protegida**: desbordamiento y pila vacía se detectan en el BCP.
4. **Aislamiento de errores**: un error termina solo al proceso que falló; los demás siguen ejecutándose.
5. **Validación previa**: los programas mal escritos se rechazan antes de entrar al sistema.
6. **Separación S.O. / usuario**: los programas solo se cargan en la zona de usuario; los BCP viven en la zona del S.O.
7. **Archivos protegidos**: `INT 21H` no puede modificar ni eliminar programas `.asm`.

---

## Interfaz gráfica

| Panel | Muestra |
|---|---|
| Cola de trabajos | Todos los procesos con su estado (en colores), base y alcance |
| Recursos del sistema | Procesos en memoria (x/5), memoria de usuario, memoria virtual, zona del S.O. y almacenamiento |
| Dispositivos | Estado de CPU, pantalla, teclado y disco |
| Memoria principal | Mapa visual (S.O., procesos, libre, PC) + tabla con cada posición y su dueño |
| Pantalla | Salida de `INT 10H`, avisos del S.O., errores y entrada de teclado |
| Disco | Índice, archivos y memoria virtual (con la instrucción actual resaltada si se ejecuta desde ahí) |
| CPU 1 | PC, IR, AC, AX, BX, CX, DX, AH, AL, bandera de CMP y progreso de la instrucción según su peso |
| Pila | Las 5 posiciones de la pila del proceso actual con el tope (SP) |
| BCP actual | Proceso, estado, base, alcance, prioridad, CPU, inicio, tiempo empleado, dirección del BCP en memoria y archivos abiertos |

| Botón | Función |
|---|---|
| Cargar archivos | Selecciona uno o varios `.asm` y los valida |
| Ejecutar | Ejecución automática (1 segundo real por paso) |
| Paso a paso | Avanza 1 segundo de CPU |
| Detener | Pausa la ejecución automática |
| Limpiar | Reinicia el sistema (pide confirmación si hay procesos activos) |
| Estadísticas | Inicio, final, duración y tiempo de CPU por proceso |
| Configuración | Tamaños de memoria, S.O., disco y memoria virtual (se guardan en `config.txt`) |
| Salir | Cierra la aplicación (con confirmación) |

---

## Ejemplo de archivo `.asm`

```
; Ciclo: imprime 1, 2, 3, 4, 5
MOV CX, 5
MOV DX, 0
INC DX         ; inicio del ciclo
INT 10H        ; imprime DX
CMP DX, CX
JNE -3         ; vuelve a INC DX mientras DX != 5
INT 20H
```
