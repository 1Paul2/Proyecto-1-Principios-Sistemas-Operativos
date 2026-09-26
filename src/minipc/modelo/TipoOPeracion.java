/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minipc.modelo;

/**
 *
 * @author Poll Anthony Garro Vargas - 2024129001
 * @Universidad: Instituto Tecnológico de Costa Rica
 * 
 */

/**
 * TipoOPeracion: convierte entre el texto de una operación del lenguaje
 * ensamblador y su código binario de 4 bits, y viceversa.

 */
public class TipoOPeracion {
    
    // E: Type (String) - nombre de la operación, como "LOAD", "MOV"
    // S: String - código binario de 4 bits de la operación, o null si no es válida
    // R: Type debe ser exactamente una de las operaciones existentes
    public static String Tipo(String Type){
        String Dato = "";
        
        if(Type.equals("LOAD")){
            Dato = "0001";
        }else if(Type.equals("STORE")){
            Dato = "0010";
        }else if(Type.equals("MOV")){
            Dato = "0011";
        }else if(Type.equals("SUB")){
            Dato = "0100";
        }else if(Type.equals("ADD")){
            Dato = "0101";
        }else if(Type.equals("INC")){
            Dato = "0110";
        }else if(Type.equals("DEC")){
            Dato = "0111";
        }else if(Type.equals("SWAP")){
            Dato = "1000";
        }else if(Type.equals("JMP")){
            Dato = "1001";
        }else if(Type.equals("CMP")){
            Dato = "1010";
        }else if(Type.equals("JE")){
            Dato = "1011";
        }else if(Type.equals("JNE")){
            Dato = "1100";
        }else if(Type.equals("PARAM")){
            Dato = "1101";
        }else if(Type.equals("PUSH")){
            Dato = "1110";
        }else if(Type.equals("POP")){
            Dato = "1111";
        }else if(Type.equals("INT")){
            Dato = "0000";
        }else{
            Dato = null;
        }
        return Dato;
    }
    
    // E: binario (String) - código binario de 4 bits de una operación
    // S: String - nombre de la operación, o null si el código no es válido
    // R: binario debe ser uno de los códigos válidos definidos
    public static String BinarioATipo(String binario){
        String Dato = "";
        if(binario.equals("0001")){
            Dato = "LOAD";
        }else if(binario.equals("0010")){
            Dato = "STORE";
        }else if(binario.equals("0011")){
            Dato = "MOV";
        }else if(binario.equals("0100")){
            Dato = "SUB";
        }else if(binario.equals("0101")){
            Dato = "ADD";
        }else if(binario.equals("0110")){
            Dato = "INC";
        }else if(binario.equals("0111")){
            Dato = "DEC";
        }else if(binario.equals("1000")){
            Dato = "SWAP";
        }else if(binario.equals("1001")){
            Dato = "JMP";
        }else if(binario.equals("1010")){
            Dato = "CMP";
        }else if(binario.equals("1011")){
            Dato = "JE";
        }else if(binario.equals("1100")){
            Dato = "JNE";
        }else if(binario.equals("1101")){
            Dato = "PARAM";
        }else if(binario.equals("1110")){
            Dato = "PUSH";
        }else if(binario.equals("1111")){
            Dato = "POP";
        }else if(binario.equals("0000")){
            Dato = "INT";
        }else{
            Dato = null;
        }
        return Dato;
    }

    
    public static void main(String[] arg){


    }
}