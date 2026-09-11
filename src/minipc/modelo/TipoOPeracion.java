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
            Dato = "0001";
        }else if(Type.equals("STORE")){
            Dato = "0010";
        }else if(Type.equals("MOV")){
            Dato = "0011";
        }else if(Type.equals("SUB")){
            Dato = "0100";
        }else if(Type.equals("ADD")){
            Dato = "0101";
        }else{
            Dato = null;
        }
        return Dato;
    }
    
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
        }else{
            Dato = null;
        }
        return Dato;
    }

    
    public static void main(String[] arg){


    }
}