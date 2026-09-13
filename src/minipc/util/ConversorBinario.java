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
public class ConversorBinario {
    
    // E: Type (String) - nombre del registro, ej. "AX", "BX", "CX", "DX"
    // S: String - código binario de 4 bits del registro, o null si no es válido
    // R: Type debe ser exactamente uno de los 4 registros existentes
    public static String RegistroBinario(String Type){
        String Dato = "";
        if(Type.equals("AX")){
            Dato = "0001";
        }else if(Type.equals("BX")){
            Dato = "0010";
        }else if(Type.equals("CX")){
            Dato = "0011";
        }else if(Type.equals("DX")){
            Dato = "0100";
        }else{
            Dato = null;
            
        }
        return Dato;
    }
    
    // E: n (int) - número entero a convertir, positivo o negativo
    // S: String - representación binaria de 8 bits (1 bit de signo + 7 bits de valor)
    // R: n debe estar en el rango -127 a 127 para que entre en 7 bits de magnitud
    public static String NumeroBinario(int n){
        String binario = "";
        String Dato = "00000000";
        if(n < 0){
            StringBuilder sb = new StringBuilder(Dato);
            sb.setCharAt(0,'1');
            Dato = sb.toString();
            n = n * -1;
        }
        
        while (n > 0) {
            binario = (n % 2) + binario;
            n = n / 2;
        }
        StringBuilder sb = new StringBuilder(Dato);
        sb.replace((8-(binario.length())),8, binario);
        Dato = sb.toString();
        return Dato;
    }
  
    // E: binario (String) - cadena de 8 bits (1 bit de signo + 7 bits de valor)
    // S: int - el valor entero representado, positivo o negativo
    // R: binario debe tener exactamente 8 caracteres, solo '0' y '1'
    public static int BinarioAEntero(String binario){
        boolean esNegativo = true;
        int num = 0;
        if(binario.charAt(0) == '1'){
            StringBuilder sb = new StringBuilder(binario);
            sb.setCharAt(0,'0');
            binario = sb.toString();
            esNegativo = false;
        }
        num = Integer.parseInt(binario, 2);
        if(!esNegativo){
            num = (num * (-1));
        }
        return num;
    }
    
    // E: binario (String) - código binario de 4 bits de un registro
    // S: String - nombre del registro ("AX","BX","CX","DX"), o null si no es válido
    // R: binario debe ser uno de los 4 códigos válidos definidos
    public static String BinarioARegistro(String binario){
        String Dato = "";
        if(binario.equals("0001")){
            Dato = "AX";
        }else if(binario.equals("0010")){
            Dato = "BX";
        }else if(binario.equals("0011")){
            Dato = "CX";
        }else if(binario.equals("0100")){
            Dato = "DX";
        }else{
            Dato = null;
            
        }
        return Dato;
    }
    
    public static void main(String[] args){

    }
}