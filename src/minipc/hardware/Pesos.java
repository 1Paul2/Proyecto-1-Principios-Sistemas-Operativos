/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minipc.hardware;

public class Pesos {

    // E: operacion (String), registro (String) - p. ej. "ADD", "BX" o "INT", "20H"
    // S: int - segundos que tarda la instrucción
    // R: si la operación no se reconoce, devuelve 1
    public static int de(String operacion, String registro){
        if(operacion == null) return 1;
        switch (operacion) {
            case "LOAD": case "STORE": return 2;
            case "ADD":  case "SUB":   return 3;
            case "PARAM":              return 3;
            case "JMP": case "CMP": case "JE": case "JNE": return 2;
            case "MOV": case "INC": case "DEC": case "SWAP":
            case "PUSH": case "POP":   return 1;
            case "INT":
                if("21H".equals(registro)) return 5;
                if("09H".equals(registro)) return 1; // luego queda en espera del teclado
                return 2;                            // 20H y 10H
            default: return 1;
        }
    }
}