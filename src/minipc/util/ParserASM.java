/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minipc.util;

import minipc.modelo.Instruccion;
import minipc.modelo.TipoOPeracion;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
/**
 *
 * @author elenanito
 */
public class ParserASM {
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
