package minipc.util;

/**
 *
 * @author Poll Anthony Garro Vargas - 2024129001
 * @Universidad: Instituto Tecnológico de Costa Rica
 *
 */

import minipc.modelo.Instruccion;
import minipc.modelo.TipoOperacion;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * ParserASM: lee un archivo .asm, valida la sintaxis de cada línea según la
 * tabla de instrucciones del enunciado y la convierte en una Instruccion.
 * Si una línea es inválida, lanza una excepción con un mensaje claro que
 * indica la línea y el motivo.
 */
public class ParserASM {

    private static final int VALOR_MIN = -127;
    private static final int VALOR_MAX = 127;
    private static final int MAX_PARAMETROS = 3;

    // E: linea (String) - una línea del .asm, sin comentarios y no vacía
    // S: Instruccion - la instrucción validada
    // R: lanza IllegalArgumentException con el motivo si la línea no es válida
    public static Instruccion procesarLinea(String linea){
        // Un texto entre comillas (nombre de archivo) se respeta tal cual, sin pasarlo a mayúsculas
        String literal = null;
        String codigo = linea.trim();
        int comilla = codigo.indexOf('"');
        if (comilla >= 0) {
            literal = codigo.substring(comilla).trim();
            codigo = codigo.substring(0, comilla);
            if (!literal.matches("\"[A-Za-z0-9_.\\-]+\"")) {
                throw new IllegalArgumentException("el texto " + literal
                    + " no es válido (use comillas y solo letras, números, punto, guion o guion bajo, sin espacios)");
            }
        }
        String[] partes = codigo.trim().toUpperCase().split("[,\\s]+");
        if (literal != null) {
            partes = Arrays.copyOf(partes, partes.length + 1);
            partes[partes.length - 1] = literal;
        }
        String operacion = partes[0];
        String[] ops = Arrays.copyOfRange(partes, 1, partes.length);
        if (literal != null && !"MOV".equals(operacion)) {
            throw new IllegalArgumentException("solo MOV DX acepta un texto entre comillas");
        }

        if(!TipoOperacion.esOperacionValida(operacion)){
            throw new IllegalArgumentException("la operación \"" + operacion + "\" no existe");
        }

        switch (operacion) {

            // ---- Un registro: LOAD AX, STORE BX, ADD BX, SUB BX, PUSH AX, POP AX ----
            case "LOAD":
            case "STORE":
            case "ADD":
            case "SUB":
            case "PUSH":
            case "POP": {
                exigirCantidad(operacion, ops, 1, 1);
                exigirRegistro(ops[0]);
                return new Instruccion(operacion, ops[0]);
            }

            // ---- Registro opcional: INC / INC AX, DEC / DEC AX ----
            case "INC":
            case "DEC": {
                exigirCantidad(operacion, ops, 0, 1);
                if(ops.length == 1){
                    exigirRegistro(ops[0]);
                    return new Instruccion(operacion, ops[0]);
                }
                return new Instruccion(operacion);
            }

            // ---- MOV destino, origen (origen = registro o número) ----
            case "MOV": {
                exigirCantidad(operacion, ops, 2, 2);
                exigirRegistro(ops[0]);
                if(ops[1].startsWith("\"")){
                    // MOV DX, "archivo.txt": nombre de archivo para INT 21H (o texto para INT 10H)
                    if(!"DX".equals(ops[0])){
                        throw new IllegalArgumentException("un texto entre comillas solo se puede mover a DX");
                    }
                    return new Instruccion(operacion, ops[0], ops[1]);
                }
                if(ConversorBinario.esRegistroValido(ops[1])){
                    return new Instruccion(operacion, ops[0], ops[1]);
                }
                int valor = exigirNumero(ops[1]);
                validarRango(valor);
                return new Instruccion(operacion, ops[0], String.valueOf(valor));
            }

            // ---- Dos registros: SWAP AX, BX y CMP AX, BX ----
            case "SWAP":
            case "CMP": {
                exigirCantidad(operacion, ops, 2, 2);
                exigirRegistro(ops[0]);
                exigirRegistro(ops[1]);
                return new Instruccion(operacion, ops[0], ops[1]);
            }

            // ---- Desplazamiento: JMP +2, JE -3, JNE 4 ----
            case "JMP":
            case "JE":
            case "JNE": {
                exigirCantidad(operacion, ops, 1, 1);
                int desp = exigirNumero(ops[0]);
                if(desp == 0){
                    throw new IllegalArgumentException("un desplazamiento de 0 salta a la misma instrucción (ciclo infinito)");
                }
                return new Instruccion(operacion, String.valueOf(desp));
            }

            // ---- PARAM v1, v2, v3 (de 1 a 3 valores) ----
            case "PARAM": {
                exigirCantidad(operacion, ops, 1, MAX_PARAMETROS);
                String[] valores = new String[ops.length];
                for(int i = 0; i < ops.length; i++){
                    int v = exigirNumero(ops[i]);
                    validarRango(v);
                    valores[i] = String.valueOf(v);
                }
                return new Instruccion(operacion, valores);
            }

            // ---- INT 20H / 10H / 09H / 21H ----
            case "INT": {
                exigirCantidad(operacion, ops, 1, 1);
                if(!TipoOperacion.esInterrupcionValida(ops[0])){
                    throw new IllegalArgumentException("la interrupción \"" + ops[0]
                        + "\" no existe (válidas: 20H, 10H, 09H, 21H)");
                }
                return new Instruccion(operacion, ops[0]);
            }

            default:
                throw new IllegalArgumentException("la operación \"" + operacion + "\" no está soportada");
        }
    }

    // E: operacion (String), ops (String[]) - operandos; min, max (int) - cantidad permitida
    // S: no aplica (void)
    // R: lanza IllegalArgumentException si la cantidad de operandos no está en [min, max]
    private static void exigirCantidad(String operacion, String[] ops, int min, int max){
        if(ops.length < min || ops.length > max){
            String esperado = (min == max) ? String.valueOf(min) : ("entre " + min + " y " + max);
            throw new IllegalArgumentException(operacion + " espera " + esperado
                + " operando(s) y recibió " + ops.length);
        }
    }

    // E: texto (String) - operando que debe ser un registro
    // S: no aplica (void)
    // R: lanza IllegalArgumentException si no es AX, BX, CX o DX
    private static void exigirRegistro(String texto){
        if(!ConversorBinario.esRegistroValido(texto)){
            throw new IllegalArgumentException("\"" + texto
                + "\" no es un registro válido (use AX, BX, CX, DX, AH o AL)");
        }
    }

    // E: texto (String) - operando que debe ser un número entero
    // S: int - el número
    // R: lanza IllegalArgumentException si no es un número
    private static int exigirNumero(String texto){
        try {
            if (texto.matches("[0-9][0-9A-F]*H")) {          // hexadecimal: 3CH, 41H, 0FH
                return Integer.parseInt(texto.substring(0, texto.length() - 1), 16);
            }
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("\"" + texto + "\" no es un número válido");
        }
    }

    // E: valor (int)
    // S: no aplica (void)
    // R: lanza IllegalArgumentException si el valor no cabe en 8 bits con signo
    private static void validarRango(int valor){
        if(valor < VALOR_MIN || valor > VALOR_MAX){
            throw new IllegalArgumentException("el valor " + valor + " no cabe en 8 bits (debe estar entre "
                + VALOR_MIN + " y " + VALOR_MAX + ")");
        }
    }

    // E: archivo (File) - archivo .asm a leer
    // S: List<Instruccion> - instrucciones validadas
    // R: lanza RuntimeException con la línea y el motivo si el archivo no es válido
    public static List<Instruccion> leerArchivo(File archivo) throws IOException {
        if(!archivo.getName().toLowerCase().endsWith(".asm")){
            throw new RuntimeException("\"" + archivo.getName() + "\" no es un archivo .asm");
        }

        List<Instruccion> instrucciones = new ArrayList<>();

        try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
            String linea;
            int numeroLinea = 0;

            while((linea = lector.readLine()) != null){
                numeroLinea++;

                if(linea.length() > 200){
                    throw new RuntimeException(archivo.getName() + ", línea " + numeroLinea
                        + ": es demasiado larga o contiene datos binarios");
                }

                // Quitar comentarios (todo lo que sigue a ';')
                int comentario = linea.indexOf(';');
                String limpia = (comentario >= 0) ? linea.substring(0, comentario) : linea;

                if(limpia.trim().isEmpty()){
                    continue;
                }

                try {
                    instrucciones.add(procesarLinea(limpia));
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException(archivo.getName() + ", línea " + numeroLinea
                        + " (\"" + linea.trim() + "\"): " + e.getMessage());
                }
            }
        }

        if(instrucciones.isEmpty()){
            throw new RuntimeException("\"" + archivo.getName() + "\" no contiene instrucciones");
        }
        return instrucciones;
    }
}