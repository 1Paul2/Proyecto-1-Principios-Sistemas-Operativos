/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minipc.util;

/**
 * ConversorBinario: utilidades para validar registros y convertir valores
 * hexadecimales. Ya no se usa para convertir a binario porque el sistema
 * ahora trabaja con texto plano en memoria.
 */
public class ConversorBinario {

    /**
     * Valida si un texto es uno de los registros válidos (AX, BX, CX, DX, AH, AL).
     */
    public static boolean esRegistroValido(String reg){
        if(reg == null) return false;
        return reg.equals("AX") || reg.equals("BX")
            || reg.equals("CX") || reg.equals("DX")
            || reg.equals("AH") || reg.equals("AL");     // AH y AL se usan con INT 21H
    }

    /**
     * Convierte un texto hexadecimal como "20H" o "1A" a entero.
     * Usado por el parser ASM para interpretar valores como INT 20H.
     */
    public static int HexAEntero(String texto){
        String limpio = texto.trim().toUpperCase();
        if(limpio.endsWith("H")){
            limpio = limpio.substring(0, limpio.length() - 1);
        }
        return Integer.parseInt(limpio, 16);
    }

    public static void main(String[] args){
    }
}