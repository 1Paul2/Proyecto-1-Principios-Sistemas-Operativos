/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minipc.modelo;

/**
 *
 * @author elenanito
 */



    
public class TipoOPeracion {
    
    public static String Tipo(String Type){
        String Dato = "";
        
        if(Type.equals("LOAD")){
            Dato = "001";
        }else if(Type.equals("STORE")){
            Dato = "010";
        }else if(Type.equals("MOV")){
            Dato = "011";
        }else if(Type.equals("SUB")){
            Dato = "100";
        }else if(Type.equals("ADD")){
            Dato = "101";
        }else{
            Dato = null;
        }
        return Dato;
    }
    
        public static String BinarioATipo(String binario){
        String Dato = "";
        if(binario.equals("001")){
            Dato = "LOAD";
        }else if(binario.equals("010")){
            Dato = "STORE";
        }else if(binario.equals("011")){
            Dato = "MOV";
        }else if(binario.equals("100")){
            Dato = "SUB";
        }else if(binario.equals("101")){
            Dato = "ADD";
        }else{
            Dato = null;
        }
        return Dato;
    }

    
    public static void main(String[] arg){


    }
}