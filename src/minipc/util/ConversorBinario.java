/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minipc.util;

/**
 *
 * @author elenanito
 */
public class ConversorBinario {
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
