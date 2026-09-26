package minipc.hardware;

/**
 *
 * @author Poll Anthony Garro Vargas - 2024129001
 * @Universidad: Instituto Tecnológico de Costa Rica
 * 
 */

import minipc.modelo.TipoOPeracion;
import minipc.modelo.Instruccion;
import minipc.util.ConversorBinario;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * CPU: implementa el ciclo fetch-decode-execute del Mini PC, con sus registros
 * (PC, IR, AC, AX, BX, CX, DX), pila, bandera de comparación, pantalla y
 * teclado simulados, y una referencia a la Memoria sobre la que opera.
 */
public class CPU {
    private int PC;
    private String IR;
    private int AC;
    private int AX;
    private int BX;
    private int CX;
    private int DX;

    // NUEVO: AH y AL se usan únicamente para representar el código de
    // operación de archivo (AH) y el contenido a leer/escribir (AL) que pide
    // INT 21H. El enunciado los menciona como parte de esa interrupción pero
    // no los lista como registros generales, así que aquí son campos aparte
    // y no se tocan con LOAD/STORE/ADD/SUB/MOV. Ajustar si el profesor
    // esperaba otra convención.
    private int AH;
    private int AL;

    private Memoria memoria;
    private int limitePrograma;

    // NUEVO: pila de tamaño fijo 5, con su propio puntero (tope = -1 vacía)
    private static final int TAMANO_PILA = 5;
    private int[] pila;
    private int tope;

    // NUEVO: bandera que guarda el resultado de la última comparación (CMP),
    // usada por JE/JNE
    private boolean banderaIgual;

    // NUEVO: buffer de salida simulando la "pantalla" (INT 10H escribe aquí)
    private List<String> pantalla;

    // NUEVO: entrada simulando el "teclado" (INT 09H lee de aquí)
    private Scanner teclado;

    // NUEVO: bandera de fin de programa, la enciende INT 20H
    private boolean terminado;

    // E: memoria (Memoria) - la memoria que va a usar esta CPU
    // S: no aplica (constructor)
    // R: ninguna
    public CPU(Memoria memoria){
        this.memoria = memoria;
        this.PC = 0;
        this.AC = 0;
        this.AX = 0;
        this.BX = 0;
        this.CX = 0;
        this.DX = 0;
        this.AH = 0;
        this.AL = 0;
        this.IR = "";
        this.pila = new int[TAMANO_PILA];
        this.tope = -1;
        this.banderaIgual = false;
        this.pantalla = new ArrayList<>();
        this.teclado = new Scanner(System.in);
        this.terminado = false;
    }

    public int getPC(){ return PC; }
    public int getAC(){ return AC; }
    public int getAX(){ return AX; }
    public int getBX(){ return BX; }
    public int getCX(){ return CX; }
    public int getDX(){ return DX; }
    public int getAH(){ return AH; }
    public int getAL(){ return AL; }
    public String getIR(){ return IR; }
    public boolean getBanderaIgual(){ return banderaIgual; }
    public boolean isTerminado(){ return terminado; }
    public List<String> getPantalla(){ return pantalla; }
    public int[] getPila(){ return pila; }
    public int getTope(){ return tope; }

    public void setPC(int nuevoPC){
        this.PC = nuevoPC;
    }

    // ============================================================
    //  PILA (PUSH / POP) — tamaño fijo 5, con desbordamiento
    // ============================================================

    // E: valor (int) - el valor a apilar
    // S: no aplica (void)
    // R: lanza RuntimeException si la pila ya está llena (desbordamiento)
    public void push(int valor){
        if(tope >= TAMANO_PILA - 1){
            throw new RuntimeException("Error de desbordamiento de pila (stack overflow): la pila ya tiene " + TAMANO_PILA + " elementos.");
        }
        tope = tope + 1;
        pila[tope] = valor;
    }

    // E: no aplica
    // S: int - el valor que estaba en el tope de la pila
    // R: lanza RuntimeException si la pila está vacía
    public int pop(){
        if(tope < 0){
            throw new RuntimeException("Error: la pila está vacía, no se puede hacer POP.");
        }
        int valor = pila[tope];
        tope = tope - 1;
        return valor;
    }

    // ============================================================
    //  DECODE
    // ============================================================

    // decode ahora devuelve la operación y el arreglo de "partes" restante
    // (sin el código de operación), para que execute() lo interprete según
    // el tipo de instrucción (algunas traen 1 registro, otras 2, otras un
    // desplazamiento, etc.)
    // E: dato (String) - el codigo binario completo leido de memoria (IR)
    // S: Object[2] - [operacion (String), partes (String[] con el resto de tokens binarios)]
    // R: dato debe tener el formato "operador [operando1] [operando2] ..."
    public Object[] decode(String dato){
        String[] tokens = dato.trim().split(" ");
        String operacion = TipoOPeracion.BinarioATipo(tokens[0]);

        String[] resto = new String[tokens.length - 1];
        for(int i = 1; i < tokens.length; i++){
            resto[i - 1] = tokens[i];
        }

        Object[] resultado = new Object[2];
        resultado[0] = operacion;
        resultado[1] = resto;
        return resultado;
    }

    // E: no aplica
    // S: no aplica (void)
    // R: lee memoria en la posicion PC actual y avanza PC en 1
    public void fetch(){
        IR = memoria.leer(PC);
        PC = PC + 1;
    }

    // ============================================================
    //  EXECUTE
    // ============================================================

    // E: operacion (String), partes (String[]) - tokens binarios restantes de la instrucción
    // S: no aplica (void)
    // R: modifica registros/pila/PC/bandera/pantalla según la operación
    public void execute(String operacion, String[] partes){
        switch(operacion){

            case "LOAD": {
                String registro = ConversorBinario.BinarioARegistro(partes[0]);
                AC = leerRegistro(registro);
                break;
            }
            case "STORE": {
                String registro = ConversorBinario.BinarioARegistro(partes[0]);
                escribirRegistro(registro, AC);
                break;
            }
            case "ADD": {
                String registro = ConversorBinario.BinarioARegistro(partes[0]);
                AC = AC + leerRegistro(registro);
                break;
            }
            case "SUB": {
                String registro = ConversorBinario.BinarioARegistro(partes[0]);
                AC = AC - leerRegistro(registro);
                break;
            }
            case "MOV": {
                // MOV reg_destino, reg_origen  O  MOV reg_destino, valor
                String registroDestino = ConversorBinario.BinarioARegistro(partes[0]);
                String posibleRegistroOrigen = ConversorBinario.BinarioARegistro(partes[1]);
                if(posibleRegistroOrigen != null){
                    escribirRegistro(registroDestino, leerRegistro(posibleRegistroOrigen));
                } else {
                    int valor = ConversorBinario.BinarioAEntero(partes[1]);
                    escribirRegistro(registroDestino, valor);
                }
                break;
            }

            // -------- NUEVO: incremento / decremento --------
            case "INC": {
                if(partes.length == 0){
                    AC = AC + 1;
                } else {
                    String registro = ConversorBinario.BinarioARegistro(partes[0]);
                    escribirRegistro(registro, leerRegistro(registro) + 1);
                }
                break;
            }
            case "DEC": {
                if(partes.length == 0){
                    AC = AC - 1;
                } else {
                    String registro = ConversorBinario.BinarioARegistro(partes[0]);
                    escribirRegistro(registro, leerRegistro(registro) - 1);
                }
                break;
            }

            // -------- NUEVO: intercambio --------
            case "SWAP": {
                String reg1 = ConversorBinario.BinarioARegistro(partes[0]);
                String reg2 = ConversorBinario.BinarioARegistro(partes[1]);
                int temporal = leerRegistro(reg1);
                escribirRegistro(reg1, leerRegistro(reg2));
                escribirRegistro(reg2, temporal);
                break;
            }

            // -------- NUEVO: pila --------
            case "PUSH": {
                String registro = ConversorBinario.BinarioARegistro(partes[0]);
                push(leerRegistro(registro));
                break;
            }
            case "POP": {
                String registro = ConversorBinario.BinarioARegistro(partes[0]);
                escribirRegistro(registro, pop());
                break;
            }
            case "PARAM": {
                // PARAM v1, v2, .. vN (máximo 3 valores numéricos, van a la pila)
                if(partes.length > 3){
                    throw new RuntimeException("PARAM solo admite máximo 3 valores.");
                }
                for(int i = 0; i < partes.length; i++){
                    push(ConversorBinario.BinarioAEntero(partes[i]));
                }
                break;
            }

            // -------- NUEVO: saltos y comparación --------
            case "JMP": {
                int desplazamiento = ConversorBinario.BinarioAEntero(partes[0]);
                saltar(desplazamiento);
                break;
            }
            case "CMP": {
                String reg1 = ConversorBinario.BinarioARegistro(partes[0]);
                String reg2 = ConversorBinario.BinarioARegistro(partes[1]);
                banderaIgual = (leerRegistro(reg1) == leerRegistro(reg2));
                break;
            }
            case "JE": {
                int desplazamiento = ConversorBinario.BinarioAEntero(partes[0]);
                if(banderaIgual){
                    saltar(desplazamiento);
                }
                break;
            }
            case "JNE": {
                int desplazamiento = ConversorBinario.BinarioAEntero(partes[0]);
                if(!banderaIgual){
                    saltar(desplazamiento);
                }
                break;
            }

            // -------- NUEVO: interrupciones --------
            case "INT": {
                // partes[0] llega como el binario de 8 bits del código (ej. 0x20 = 32),
                // no como el texto "20H" — eso solo existe en el archivo .asm de origen
                int codigo = ConversorBinario.BinarioAEntero(partes[0]);
                ejecutarInterrupcion(codigo);
                break;
            }

            default:
                throw new RuntimeException("Operación no reconocida: " + operacion);
        }
    }

    // E: registro (String) - "AX", "BX", "CX" o "DX"
    // S: int - el valor actual de ese registro
    // R: ninguna
    private int leerRegistro(String registro){
        switch(registro){
            case "AX": return AX;
            case "BX": return BX;
            case "CX": return CX;
            case "DX": return DX;
            default: throw new RuntimeException("Registro desconocido: " + registro);
        }
    }

    // E: registro (String), valor (int)
    // S: no aplica (void)
    // R: ninguna
    private void escribirRegistro(String registro, int valor){
        switch(registro){
            case "AX": AX = valor; break;
            case "BX": BX = valor; break;
            case "CX": CX = valor; break;
            case "DX": DX = valor; break;
            default: throw new RuntimeException("Registro desconocido: " + registro);
        }
    }

    // E: desplazamiento (int) - cuántas posiciones saltar (+/-)
    // S: no aplica (void)
    // R: lanza RuntimeException si el nuevo PC se sale del rango del programa (desbordamiento)
    private void saltar(int desplazamiento){
        int nuevoPC = PC + desplazamiento;
        if(nuevoPC < memoria.getInicioUsuario() || nuevoPC >= limitePrograma){
            throw new RuntimeException("Error de desbordamiento: el salto lleva el PC a la posición " + nuevoPC + ", fuera del rango del programa.");
        }
        PC = nuevoPC;
    }

    // E: codigo (int) - 0x20 (fin), 0x10 (pantalla), 0x09 (teclado) o 0x21 (archivos)
    // S: no aplica (void)
    // R: ejecuta la interrupción correspondiente
    private void ejecutarInterrupcion(int codigo){
        switch(codigo){
            case 0x20:
                // Finaliza el programa
                terminado = true;
                break;

            case 0x10:
                // Imprime en pantalla el valor del DX
                pantalla.add(String.valueOf(DX));
                break;

            case 0x09: {
                // Entrada de teclado (solo numérico 0-255), se guarda en DX, termina con ENTER
                int valor = teclado.nextInt();
                if(valor < 0 || valor > 255){
                    throw new RuntimeException("El valor ingresado debe estar entre 0 y 255.");
                }
                DX = valor;
                break;
            }

            case 0x21:
                // Manejo de archivos. DX = nombre de archivo (como texto),
                // AH = sub-operación, AL = contenido a leer/escribir.
                // NOTA: esto asume que en algún punto DX/AH se cargaron con
                // el nombre de archivo y el código de operación antes del
                // INT 21H; ajustar según cómo se resuelva "DX como cadena de
                // texto" en el diseño final (probablemente necesite un mapa
                // registro->nombre de archivo en vez de un int).
                ejecutarOperacionArchivo();
                break;

            default:
                throw new RuntimeException("Código de interrupción no reconocido: " + Integer.toHexString(codigo) + "H");
        }
    }

    // NOTA: implementación preliminar de INT 21H. El almacenamiento
    // secundario probablemente debería vivir en su propia clase (Disco /
    // Almacenamiento) en vez de manejarse directo desde la CPU con
    // java.io.File; esto es solo un punto de partida para no dejar el caso
    // vacío.
    private void ejecutarOperacionArchivo(){
        String nombreArchivo = "archivo_" + DX + ".txt";
        try {
            switch(AH){
                case 0x3C: // crear archivo
                    new File(nombreArchivo).createNewFile();
                    break;
                case 0x3D: // abrir archivo
                    // no-op explícito: abrir se resuelve al leer/escribir
                    break;
                case 0x4D: { // leer archivo
                    BufferedReader lector = new BufferedReader(new FileReader(nombreArchivo));
                    String linea = lector.readLine();
                    lector.close();
                    AL = (linea != null) ? Integer.parseInt(linea.trim()) : 0;
                    break;
                }
                case 0x40: { // escribir archivo
                    FileWriter escritor = new FileWriter(nombreArchivo);
                    escritor.write(String.valueOf(AL));
                    escritor.close();
                    break;
                }
                case 0x41: // eliminar archivo
                    new File(nombreArchivo).delete();
                    break;
                default:
                    throw new RuntimeException("Sub-operación de archivo no reconocida (AH = " + AH + ")");
            }
        } catch(IOException e){
            throw new RuntimeException("Error de E/S en INT 21H: " + e.getMessage());
        }
    }

    // ============================================================
    //  CARGA Y CICLO DE EJECUCIÓN
    // ============================================================

    // E: instrucciones (List<Instruccion>) - el programa ya parseado a cargar
    // S: no aplica (void)
    // R: lanza RuntimeException si el programa no cabe en el espacio de usuario disponible
    public void cargarPrograma(List<Instruccion> instrucciones){
        int posicionActual = memoria.getInicioUsuario();
        int espacioDisponible = memoria.getTamanoTotal() - memoria.getInicioUsuario();

        if(instrucciones.size() > espacioDisponible){
            throw new RuntimeException("El programa necesita " + instrucciones.size() + " de memoria, pero solo hay " + espacioDisponible + " memoria disponibles para el usuario.");
        }

        for(int i = 0; i < instrucciones.size(); i++){
            Instruccion instr = instrucciones.get(i);
            memoria.escribir(posicionActual, instr.getCodigoBinarioCompleto());
            posicionActual = posicionActual + 1;
        }

        this.limitePrograma = posicionActual;
        this.PC = memoria.getInicioUsuario();
        this.terminado = false;
    }

    // E: no aplica
    // S: no aplica (void)
    // R: ejecuta un solo ciclo fetch-decode-execute
    public void pasoAPaso(){
        fetch();
        Object[] decodificado = decode(IR);
        String operacion = (String) decodificado[0];
        String[] partes = (String[]) decodificado[1];
        execute(operacion, partes);
    }

    // E: no aplica
    // S: no aplica (void)
    // R: ejecuta pasoAPaso() en ciclo hasta que el PC alcance el limite del
    //    programa o se ejecute INT 20H (terminado = true)
    public void ejecutarTodo(){
        while(PC < limitePrograma && !terminado){
            pasoAPaso();
        }
    }

    // E: no aplica
    // S: boolean - true si el PC ya alcanzo o supero el limite del programa
    //    cargado, o si el programa terminó explícitamente con INT 20H
    // R: ninguna
    public boolean programaTerminado(){
        return PC >= limitePrograma || terminado;
    }

    public static void main(String[] args){

    }
}