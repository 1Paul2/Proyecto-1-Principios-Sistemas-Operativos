# Tarea#1 Sistemas Operativos

# Integrantes:

**2024129001 - Poll ANthony Garro Vargas**

# Estado del proyecto: Escala es 1 

# Enlace del video:

https://youtu.be/jMfvC0CLhJw



# Documentación:

# Mini PC — Simulador de CPU

**Curso:** Principios de Sistemas Operativos

**Autor:** Poll Anthony Garro Vargas — 2024129001

**Universidad:** Instituto Tecnológico de Costa Rica (TEC)

## Descripción general

Aplicación de escritorio en Java que simula un computador simplificado
capaz de leer un programa escrito en un lenguaje tipo ensamblador (archivo
`.asm`), cargarlo en una memoria configurable, y ejecutarlo instrucción por
instrucción (o de una sola vez) mediante el ciclo clásico **fetch → decode →
execute**.

---

## Estructura de paquetes

```
minipc
├── hardware
│   ├── CPU.java
│   └── Memoria.java
├── modelo
│   ├── Instruccion.java
│   ├── BCP.java
│   └── TipoOPeracion.java
├── util
│   ├── ConversorBinario.java
│   └── ParserASM.java
├── gui
│   └── VentanaPrincipal.java
└── Main.java
```

- **hardware**: el "computador" en sí — memoria y CPU con sus registros.
- **modelo**: estructuras de datos (una instrucción, el BCP, el enum de operaciones).
- **util**: lógica de conversión binaria y lectura/validación del archivo `.asm`,
  independiente de la interfaz gráfica.
- **gui**: la ventana y todo lo visual.

### Orden de dependencias

```
TipoOPeracion, ConversorBinario  -> (sin dependencias)
Instruccion                      ->  usa TipoOPeracion y ConversorBinario
Memoria                          ->  (sin dependencias)
CPU                              ->  usa Memoria, Instruccion, TipoOPeracion, ConversorBinario
ParserASM                        ->  usa Instruccion, TipoOPeracion, ConversorBinario
BCP                              ->  usa CPU
VentanaPrincipal                 ->  usa todo lo anterior
Main                             ->  usa VentanaPrincipal
```

---

## Formato de instrucción

Cada instrucción del `.asm` ocupa **una sola posición de memoria**, formada por:

| Bits | Contenido |
|---|---|
| 0–3 | Operador (4 bits) |
| 4–7 | Registro (4 bits) |
| 8–15 (opcional) | Valor numérico, solo si la instrucción lo requiere (ej. `MOV`) |

### Operadores

| Código binario | Operación |
|---|---|
| 0001 | LOAD |
| 0010 | STORE |
| 0011 | MOV |
| 0100 | SUB |
| 0101 | ADD |

### Registros

| Código binario | Registro |
|---|---|
| 0001 | AX |
| 0010 | BX |
| 0011 | CX |
| 0100 | DX |

### Formato del número entero (8 bits)

- Bit 0: signo (`0` = positivo, `1` = negativo)
- Bits 1–7: magnitud del valor

Esto limita el rango de valores permitidos en el `.asm` a **-127 a 127**.

---

## Paquete `modelo`

### `TipoOPeracion`

Enum-like estático que traduce entre el nombre de una operación y su código
binario de 4 bits.

| Método | Parámetros | Devuelve | Qué hace |
|---|---|---|---|
| `Tipo(String Type)` | Nombre de la operación (`"MOV"`, `"LOAD"`, etc.) | `String` con el código de 4 bits, o `null` si no existe | Recorre las 5 operaciones válidas con `if/else` y devuelve su código binario correspondiente |
| `BinarioATipo(String binario)` | Código binario de 4 bits | `String` con el nombre de la operación, o `null` si no coincide con ninguno | Es el proceso inverso a `Tipo()`: dado el binario, reconstruye el nombre de texto |

### `Instruccion`

Representa **una línea ya procesada** del `.asm`, lista para guardarse en
memoria y mostrarse en pantalla.

**Campos internos:** `operacion`, `registro`, `valor` (puede ser `null`),
`codigoBinarioOperacion`, `codigoBinarioValor`.

| Método | Parámetros | Devuelve | Qué hace |
|---|---|---|---|
| `Instruccion(operacion, registro, valor)` (constructor) | operación en texto, registro en texto, valor entero opcional | — | Guarda los tres datos recibidos y calcula automáticamente `codigoBinarioOperacion` (operador+registro en binario) y, si hay valor, `codigoBinarioValor` |
| `getoperacion()` / `getregistro()` / `getvalor()` | — | los valores originales guardados | Getters simples |
| `getcodigoBinarioOperacion()` / `getcodigoBinarioValor()` | — | `String` binario | Getters de los códigos ya calculados |
| `getCodigoBinarioCompleto()` | — | `String` | Une `codigoBinarioOperacion` y `codigoBinarioValor` (si existe) en un solo string — esto es lo que efectivamente se guarda en una posición de `Memoria` |

### `BCP` (Bloque de Control de Proceso)

Representa la "foto" del estado de un proceso en un instante dado — un
concepto de Sistemas Operativos que permite pausar y retomar un proceso sin
perder su contexto.

**Campos internos:** `idProceso`, `estado`, y una copia de cada registro
(`pcGuardado`, `acGuardado`, `axGuardado`, `bxGuardado`, `cxGuardado`,
`dxGuardado`).

| Método | Parámetros | Devuelve | Qué hace |
|---|---|---|---|
| `BCP(idProceso, estado)` (constructor) | id numérico, estado inicial en texto | — | Inicializa el id y el estado; los registros guardados arrancan todos en 0 |
| `capturaEstado(CPU cpu)` | una instancia de `CPU` | — | Copia el valor **actual** de cada registro de la CPU hacia los campos `*Guardado` del BCP — es la "foto" en sí |
| `setEstado(String nuevoEstado)` | nuevo texto de estado | — | Permite cambiar el estado (ej. de "Nuevo" a "Terminado") |
| `getIdProceso()`, `getEstado()`, `getPcGuardado()`, etc. | — | el valor correspondiente | Getters de cada atributo, usados por la interfaz para mostrarlos |

---

## Paquete `hardware`

### `Memoria`

Modela la memoria del computador como un arreglo de `String`, cada posición
con un código binario de 8 bits (`"00000000"` por defecto), dividido en zona
de Sistema Operativo y zona de Usuario.

| Método | Parámetros | Devuelve | Qué hace |
|---|---|---|---|
| `Memoria(tamanoTotal, tamanoSistema)` (constructor) | tamaño total de la memoria, tamaño reservado al S.O. | — | Crea el arreglo del tamaño indicado y lo llena por completo con `"00000000"`; guarda `inicioUsuario = tamanoSistema` |
| `escribir(posicion, valorBinario)` | índice de memoria, texto binario a guardar | — | Asigna directamente `datos[posicion] = valorBinario` |
| `leer(posicion)` | índice de memoria | `String` | Devuelve `datos[posicion]` |
| `estaEnZonaUsuario(posicion)` | índice de memoria | `boolean` | Verifica si la posición cae dentro del rango `[inicioUsuario, tamanoTotal)` |
| `getInicioUsuario()` | — | `int` | Primera posición disponible para el usuario |
| `getTamanoTotal()` | — | `int` | Tamaño total configurado |
| `getTodasLasPosiciones()` | — | `String[]` | Devuelve el arreglo completo, usado por la interfaz para pintar la tabla de memoria |

### `CPU`

El corazón del simulador: implementa el ciclo **fetch → decode → execute** y
mantiene los 7 registros.

**Campos internos:** `PC`, `IR`, `AC`, `AX`, `BX`, `CX`, `DX`, una referencia a
`Memoria`, y `limitePrograma` (posición donde termina el programa cargado).

| Método | Parámetros | Devuelve | Qué hace |
|---|---|---|---|
| `CPU(Memoria memoria)` (constructor) | la memoria a usar | — | Guarda la referencia a memoria y arranca todos los registros en 0 |
| `getPC()`, `getAC()`, `getAX()`, `getBX()`, `getCX()`, `getDX()`, `getIR()` | — | el valor correspondiente | Getters simples de cada registro |
| `setPC(int nuevoPC)` | nueva posición | — | Fuerza el valor del `PC`; lo usa `cargarPrograma()` para posicionarlo al inicio del programa |
| `fetch()` | — | — | Lee `memoria.leer(PC)`, lo guarda en `IR`, y avanza `PC` en 1 |
| `decode(String dato)` | el binario leído (normalmente el `IR`) | `String[3]`: operación, registro, valor binario (o `null`) | Separa el `dato` por espacios y traduce cada parte usando `TipoOPeracion` y `ConversorBinario` |
| `execute(operacion, registro, valorBinario)` | los 3 valores que devuelve `decode()` | — | Según la operación, modifica los registros: `LOAD` copia el registro hacia `AC`; `STORE` copia `AC` hacia el registro; `ADD`/`SUB` suman/restan el registro a `AC`; `MOV` convierte `valorBinario` a entero y lo asigna al registro |
| `cargarPrograma(List<Instruccion> instrucciones)` | la lista de instrucciones ya parseadas | — | Calcula el espacio disponible en la zona de usuario; si el programa no cabe, lanza una excepción; si cabe, escribe cada instrucción (`getCodigoBinarioCompleto()`) en una posición consecutiva de memoria, guarda dónde termina en `limitePrograma`, y deja el `PC` apuntando al inicio |
| `pasoAPaso()` | — | — | Encadena `fetch()` → `decode()` → `execute()` una sola vez |
| `ejecutarTodo()` | — | — | Repite `pasoAPaso()` en un `while` hasta que `PC` alcance `limitePrograma` |
| `programaTerminado()` | — | `boolean` | `true` si `PC >= limitePrograma` |

---

## Paquete `util`

### `ConversorBinario`

Funciones puras de conversión, sin estado propio (todas `static`).

| Método | Parámetros | Devuelve | Qué hace |
|---|---|---|---|
| `RegistroBinario(String Type)` | nombre del registro | código de 4 bits, o `null` | Traduce "AX"/"BX"/"CX"/"DX" a su binario |
| `NumeroBinario(int n)` | número entero (positivo o negativo) | `String` de 8 bits | Si `n` es negativo, marca el bit de signo (posición 0) y trabaja con el valor absoluto; convierte a binario por divisiones sucesivas entre 2 y lo alinea a la derecha en los últimos 7 bits |
| `BinarioAEntero(String binario)` | cadena de 8 bits | `int` | Lee el bit de signo; si es negativo, lo limpia antes de convertir; usa `Integer.parseInt(binario, 2)` para el valor y aplica el signo al final |
| `BinarioARegistro(String binario)` | código de 4 bits | nombre del registro, o `null` | Proceso inverso a `RegistroBinario()` |

### `ParserASM`

Lee el archivo `.asm`, valida su contenido y lo convierte en objetos
`Instruccion`.

| Método | Parámetros | Devuelve | Qué hace |
|---|---|---|---|
| `procesarLinea(String linea)` | una línea de texto, ej. `"MOV AX, 5"` | `Instruccion`, o `null` si la operación/registro no existen | Separa la línea por espacios y comas (`split("[,\\s]+")`); valida la operación y el registro contra `TipoOPeracion`/`ConversorBinario`; si hay un tercer valor, lo convierte a `int` y **lanza una excepción** si no cabe en 8 bits (-127 a 127); si todo es válido, crea y devuelve la `Instruccion` |
| `leerArchivo(File archivo)` | el archivo seleccionado por el usuario | `List<Instruccion>` | Abre el archivo con `BufferedReader`, lee línea por línea llevando un contador; salta líneas vacías; si una línea es sospechosamente larga (>200 caracteres), asume que el archivo no es texto plano y lanza una excepción; llama a `procesarLinea()` por cada línea real y, si devuelve `null`, lanza una excepción indicando el número de línea exacto |

---

## Paquete `gui` — `VentanaPrincipal`

### Métodos propios (no generados por el editor visual)

| Método | Qué hace |
|---|---|
| `actualizarVista()` | Punto central de refresco: actualiza los 7 labels de la CPU (PC, IR, AC, AX, BX, CX, DX) y llama a `actualizarTablaInstrucciones()` y `actualizarTablaMemoria()`. Se llama después de cargar, dar un paso o ejecutar todo |
| `actualizarTablaInstrucciones()` | Reconstruye el modelo (`DefaultTableModel`) de la tabla de instrucciones a partir de `instruccionesActuales`, mostrando el texto de cada línea y su binario completo (con relleno `00000000` si la instrucción no lleva valor) |
| `actualizarTablaMemoria()` | Reconstruye el modelo de la tabla de memoria mostrando **todas** las posiciones configuradas; en la posición donde arranca cada instrucción muestra su texto, el resto lo deja en blanco |
| `btnCargarActionPerformed(evt)` | Si no hay memoria asignada, pregunta si continuar con los valores por defecto; abre un `JFileChooser`; valida que el archivo termine en `.asm`; llama a `ParserASM.leerArchivo()`, crea `CPU`/`Memoria`/`BCP` según corresponda, y llama a `actualizarVista()`. Atrapa por separado errores de formato (`RuntimeException`), errores de lectura (`IOException`) y cualquier otro error inesperado |
| `btnPaso_PasoActionPerformed(evt)` | Valida que haya un programa cargado y que no haya terminado; si todo está bien, llama a `cpu.pasoAPaso()` y refresca la vista |
| `btnEjecutarActionPerformed(evt)` | Misma validación que "Paso a paso", pero llama a `cpu.ejecutarTodo()` |
| `btnLimpiarActionPerformed(evt)` | Si el programa está a mitad de ejecución, pide confirmación; luego pone en `null` `cpu`, `memoria`, `bcp` e `instruccionesActuales`, y resetea labels y tablas a su estado inicial |
| `btnEstadisticasActionPerformed(evt)` | Calcula cuántas instrucciones ya se ejecutaron (`PC - inicioUsuario`), cuenta cuántas de cada tipo de operación hay entre las ya ejecutadas, calcula la duración transcurrida desde la carga, y muestra todo en un `JOptionPane` con el estado ("En proceso" / "Terminado") |
| `btnAsignarMemoriaActionPerformed(evt)` | Bloquea el cambio si hay un programa en ejecución; valida que los campos de texto sean números válidos y cumplan los mínimos (total ≥ 256, S.O. ≥ 64, S.O. < total); si todo es correcto, resetea el estado actual y crea una `Memoria`/`CPU` nuevas con los valores indicados |
| `btnSalirActionPerformed(evt)` | Pide confirmación y, si el usuario acepta, cierra la aplicación con `System.exit(0)` |

### Componentes principales

- **Tabla de Instrucciones**: instrucción en texto + su código binario completo.
- **Tabla de Memoria**: todas las posiciones de memoria; las que corresponden
  al inicio de una instrucción muestran su texto, el resto queda en blanco.
- **Panel "BCP actual CPU"**: PC, IR, AC, AX, BX, CX, DX en tiempo real.
- **Campos de memoria**: tamaño total y tamaño del S.O., con botón
  "Asignar memoria".

### Botones y su función (resumen)

| Botón | Función |
|---|---|
| Cargar archivo | Abre un selector de archivos, valida extensión `.asm`, parsea el archivo y carga el programa en memoria |
| Paso a paso | Ejecuta una sola instrucción (fetch-decode-execute) |
| Ejecutar | Corre el programa completo de una sola vez |
| Limpiar | Resetea CPU, memoria y tablas (pide confirmación si el programa no ha terminado) |
| Estadísticas | Muestra estado, duración e instrucciones ejecutadas hasta el momento, con desglose por tipo de operación |
| Asignar memoria | Crea una memoria nueva con el tamaño total y de S.O. indicados (mínimos: 256 y 64) |
| Salir | Cierra la aplicación, con confirmación previa |

### Validaciones implementadas

- Extensión del archivo debe ser `.asm`.
- El archivo debe ser texto plano (se rechazan líneas anormalmente largas,
  indicio de contenido binario).
- Cada línea debe tener una operación y un registro válidos.
- Los valores numéricos deben caber en 8 bits con signo (-127 a 127).
- El programa debe caber en el espacio de usuario disponible según la
  memoria configurada.
- No se puede reasignar memoria mientras un programa está en ejecución.
- Si no se asignó memoria antes de cargar un archivo, se pregunta si se
  desea continuar con los valores por defecto (256 total, 64 de S.O.).

---

## `Main`

Punto de entrada del programa. Su único trabajo es crear una instancia de
`VentanaPrincipal` y hacerla visible con `setVisible(true)`.

---

## Ejemplo de archivo `.asm`

```
MOV AX, 5
MOV BX, 3
LOAD AX
ADD BX
SUB AX
STORE AX
MOV BX, -8
```

### Traza de ejecución esperada

| Paso | Instrucción | AC | AX | BX |
|---|---|---|---|---|
| 1 | MOV AX, 5 | 0 | 5 | 0 |
| 2 | MOV BX, 3 | 0 | 5 | 3 |
| 3 | LOAD AX | 5 | 5 | 3 |
| 4 | ADD BX | 8 | 5 | 3 |
| 5 | SUB AX | 3 | 5 | 3 |
| 6 | STORE AX | 3 | 3 | 3 |
| 7 | MOV BX, -8 | 3 | 3 | -8 |

Resultado final esperado: `AC = 3`, `AX = 3`, `BX = -8`.
