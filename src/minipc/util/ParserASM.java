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
        // E: linea (String) - una línea de texto del archivo .asm, como "MOV AX, 5"
        // S: Instruccion si la línea es válida, o null si la operación/registro no existen
        // R: lanza RuntimeException si el valor numérico no cabe en 8 bits (-127 a 127)
        public static Instruccion procesarLinea(String linea){
            String[] partes = linea.trim().split("[,\\s]+");

            String operacion = partes[0];
            String registro = partes[1];

            // validar que la operación exista
            if(TipoOPeracion.Tipo(operacion) == null){
                return null; // línea inválida
            }

            // validar que el registro exista
            if(ConversorBinario.RegistroBinario(registro) == null){
                return null; // línea inválida
            }

            Integer valor = null;
            if(partes.length == 3){
                valor = Integer.parseInt(partes[2]);
                    if(valor < -127 || valor > 127){
                        throw new RuntimeException("El valor " + valor + " no cabe en 8 bits.\nDebe de ser entre -127 a 127 contando negativos.");
                        
                    }
            }

            return new Instruccion(operacion, registro, valor);
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
        
        public static void main(String[] args){

        }
}