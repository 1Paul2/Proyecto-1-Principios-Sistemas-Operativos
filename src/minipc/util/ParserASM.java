/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minipc.util;

/**
 *
 * @author Poll Anthony Garro Vargas - 2024129001
 * @Universidad: Instituto Tecnológico de Costa Rica
 * 
 */

import minipc.modelo.Instruccion;
import minipc.modelo.TipoOPeracion;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * ParserASM: se encarga de leer un archivo .asm de texto plano, validar que
 * cada línea tenga un formato correcto, y convertirlo en una lista de
 * Instruccion listas para cargar en memoria.
 */
public class ParserASM {

        // E: valor (int) - el valor numérico a validar
        // S: no aplica (void)
        // R: lanza RuntimeException si valor no cabe en 8 bits con signo (-127 a 127)
        private static void validarRango(int valor){
            if(valor < -127 || valor > 127){
                throw new RuntimeException("El valor " + valor + " no cabe en 8 bits.\nDebe de ser entre -127 a 127 contando negativos.");
            }
        }

        // E: linea (String) - una línea de texto del archivo .asm, como "MOV AX, 5"
        // S: Instruccion si la línea es válida, o null si la operación/registro/formato no son válidos
        // R: lanza RuntimeException si algún valor numérico no cabe en 8 bits (-127 a 127),
        //    o si el código de INT no viene en formato hexadecimal terminado en 'H'
        public static Instruccion procesarLinea(String linea){
            String[] partes = linea.trim().split("[,\\s]+");

            String operacion = partes[0];

            // validar que la operación exista
            if(TipoOPeracion.Tipo(operacion) == null){
                return null; // línea inválida
            }

            switch(operacion){

                // -------- operaciones clásicas: operacion + 1 registro --------
                case "LOAD":
                case "STORE":
                case "ADD":
                case "SUB":
                case "PUSH":
                case "POP": {
                    if(partes.length != 2){
                        return null;
                    }
                    String registro = partes[1];
                    if(ConversorBinario.RegistroBinario(registro) == null){
                        return null;
                    }
                    return new Instruccion(operacion, registro, null);
                }

                // -------- MOV reg_destino, reg_origen  O  MOV reg_destino, valor --------
                case "MOV": {
                    if(partes.length != 3){
                        return null;
                    }
                    String registroDestino = partes[1];
                    if(ConversorBinario.RegistroBinario(registroDestino) == null){
                        return null;
                    }

                    String segundo = partes[2];
                    if(ConversorBinario.RegistroBinario(segundo) != null){
                        // MOV reg_destino, reg_origen
                        return new Instruccion(operacion, registroDestino, segundo, null, null, null);
                    } else {
                        // MOV reg_destino, valor
                        Integer valor;
                        try {
                            valor = Integer.parseInt(segundo);
                        } catch(NumberFormatException e){
                            return null;
                        }
                        validarRango(valor);
                        return new Instruccion(operacion, registroDestino, valor);
                    }
                }

                case "INC":
                case "DEC": {
                    if(partes.length == 1){
                        return new Instruccion(operacion, null, null);
                    } else if(partes.length == 2){
                        String registro = partes[1];
                        if(ConversorBinario.RegistroBinario(registro) == null){
                            return null;
                        }
                        return new Instruccion(operacion, registro, null);
                    } else {
                        return null;
                    }
                }

                // -------- SWAP reg1, reg2  /  CMP reg1, reg2 --------
                case "SWAP":
                case "CMP": {
                    if(partes.length != 3){
                        return null;
                    }
                    String reg1 = partes[1];
                    String reg2 = partes[2];
                    if(ConversorBinario.RegistroBinario(reg1) == null){
                        return null;
                    }
                    if(ConversorBinario.RegistroBinario(reg2) == null){
                        return null;
                    }
                    return new Instruccion(operacion, reg1, reg2, null, null, null);
                }

                // -------- JMP / JE / JNE [+/-Desplazamiento] --------
                case "JMP":
                case "JE":
                case "JNE": {
                    if(partes.length != 2){
                        return null;
                    }
                    Integer desplazamiento;
                    try {
                        desplazamiento = Integer.parseInt(partes[1]);
                    } catch(NumberFormatException e){
                        return null;
                    }
                    validarRango(desplazamiento);
                    return new Instruccion(operacion, null, null, desplazamiento, null, null);
                }

                // -------- PARAM v1, v2, .. vN (máximo 3 valores) --------
                case "PARAM": {
                    if(partes.length < 2 || partes.length > 4){
                        return null;
                    }
                    Integer[] valores = new Integer[partes.length - 1];
                    for(int i = 1; i < partes.length; i++){
                        try {
                            valores[i - 1] = Integer.parseInt(partes[i]);
                        } catch(NumberFormatException e){
                            return null;
                        }
                        validarRango(valores[i - 1]);
                    }
                    return new Instruccion(operacion, null, null, null, valores, null);
                }

                // -------- INT 20H / 10H / 09H / 21H --------
                case "INT": {
                    if(partes.length != 2){
                        return null;
                    }
                    String codigo = partes[1].toUpperCase();
                    if(!codigo.endsWith("H")){
                        return null;
                    }
                    // valida que lo que está antes de la "H" sea hexadecimal válido
                    try {
                        ConversorBinario.HexAEntero(codigo);
                    } catch(NumberFormatException e){
                        return null;
                    }
                    return new Instruccion(operacion, null, null, null, null, codigo);
                }

                default:
                    return null;
            }
        }
        
        // E: archivo (File) - el archivo .asm seleccionado por el usuario
        // S: List<Instruccion> - todas las instrucciones válidas leídas del archivo
        // R: lanza IOException si el archivo no se puede abrir; lanza RuntimeException
        //    si alguna línea tiene formato inválido, valor fuera de rango, o el archivo no parece ser texto plano
        public static List<Instruccion> leerArchivo(File archivo) throws IOException {
            List<Instruccion> instrucciones = new ArrayList<>();

            BufferedReader lector = new BufferedReader(new FileReader(archivo));
            String linea;
            int numeroLinea = 0;

            while((linea = lector.readLine()) != null){
                numeroLinea = numeroLinea + 1;

                if(linea.length() > 200){
                    lector.close();
                    throw new RuntimeException("El archivo no parece ser un .asm de texto plano válido (línea " + numeroLinea + " es demasiado larga o contiene datos binarios)");
                }

                if(linea.trim().isEmpty()){
                    continue;
                }

                Instruccion instr = procesarLinea(linea);

                if(instr == null){
                    lector.close();
                    throw new RuntimeException("Error en la línea " + numeroLinea + ": \"" + linea + "\" no es válida");
                } else {
                    instrucciones.add(instr);
                }
            }

            lector.close();
            return instrucciones;
        }
        
}