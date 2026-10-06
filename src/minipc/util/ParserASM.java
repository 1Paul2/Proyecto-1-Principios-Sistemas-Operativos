/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minipc.util;

import minipc.modelo.Instruccion;
import minipc.modelo.TipoOperacion;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * ParserASM: lee un archivo .asm, valida cada línea y lo convierte en una
 * lista de Instruccion listas para cargar en memoria.
 */
public class ParserASM {

    /**
     * Convierte una línea del .asm en una Instruccion.
     * Devuelve null si la operación o el registro son inválidos.
     */
    public static Instruccion procesarLinea(String linea){
        String[] partes = linea.trim().split("[,\\s]+");
        String operacion = partes[0];

        // Validar que la operación exista
        if(TipoOperacion.Tipo(operacion) == null){
            return null;
        }

        switch (operacion) {

            case "INC":
            case "DEC": {
                // "INC" o "INC AX"
                if(partes.length == 1){
                    return new Instruccion(operacion, null, null);
                }
                String reg = partes[1];
                if(!ConversorBinario.esRegistroValido(reg)) return null;
                return new Instruccion(operacion, reg, null);
            }

            case "LOAD":
            case "STORE":
            case "PUSH":
            case "POP": {
              
                String reg = partes[1];
                if(!ConversorBinario.esRegistroValido(reg)) return null;
                return new Instruccion(operacion, reg, null);
            }

            case "MOV":
            case "ADD":
            case "SUB": {
                // operación + registro + valor
                String reg = partes[1];
                if(!ConversorBinario.esRegistroValido(reg)) return null;
                Integer valor = Integer.parseInt(partes[2]);
                validarRango(valor);
                return new Instruccion(operacion, reg, valor);
            }

            case "SWAP":
            case "CMP": {
                // operación + registro (para CMP AX, BX solo se usa el primero)
                String reg = partes[1];
                if(!ConversorBinario.esRegistroValido(reg)) return null;
                return new Instruccion(operacion, reg, null);
            }

            case "JMP":
            case "JE":
            case "JNE": {
                // operación + desplazamiento (puede ser +2, -3, etc.)
                Integer desp = Integer.parseInt(partes[1]);
                return new Instruccion(operacion, null, desp);
            }

            case "PARAM": {
                // operación + valor (cada PARAM mete un solo valor a la pila)
                Integer valor = Integer.parseInt(partes[1]);
                validarRango(valor);
                return new Instruccion(operacion, null, valor);
            }

            case "INT": {
                // "INT 20H", "INT 10H", "INT 09H", "INT 21H"
                String numero = partes[1].toUpperCase();
                return new Instruccion(operacion, numero, null);
            }

            default:
                return null;
        }
    }

    /**
     * Valida que un valor esté entre -127 y 127 (8 bits con signo).
     */
    private static void validarRango(int valor){
        if(valor < -127 || valor > 127){
            throw new RuntimeException("El valor " + valor +
                " no cabe en 8 bits. Debe ser entre -127 y 127.");
        }
    }

    /**
     * Lee un archivo .asm y devuelve la lista de instrucciones.
     */
    public static List<Instruccion> leerArchivo(File archivo) throws IOException {
        List<Instruccion> instrucciones = new ArrayList<>();
        BufferedReader lector = new BufferedReader(new FileReader(archivo));
        String linea;
        int numeroLinea = 0;

        while((linea = lector.readLine()) != null){
            numeroLinea = numeroLinea + 1;

            if(linea.length() > 200){
                lector.close();
                throw new RuntimeException("El archivo no parece ser un .asm válido (línea "
                    + numeroLinea + " es demasiado larga o contiene datos binarios)");
            }

            if(linea.trim().isEmpty()){
                continue;
            }

            Instruccion instr = procesarLinea(linea);

            if(instr == null){
                lector.close();
                throw new RuntimeException("Error en la línea " + numeroLinea
                    + ": \"" + linea + "\" no es válida");
            } else {
                instrucciones.add(instr);
            }
        }

        lector.close();
        return instrucciones;
    }

    public static void main(String[] args) {
        try {
            File archivo = new File("programa1.asm");
            List<Instruccion> instrucciones = ParserASM.leerArchivo(archivo);

            System.out.println("Instrucciones leídas:");
            for(Instruccion instr : instrucciones){
                System.out.println("  " + instr.getTextoCompleto());
            }
        } catch (IOException e) {
            System.out.println("Error leyendo archivo: " + e.getMessage());
        }
    }
}