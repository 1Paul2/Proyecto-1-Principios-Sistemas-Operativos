package minipc.modelo;

/**
 *
 * @author Poll Anthony Garro Vargas - 2024129001
 * @Universidad: Instituto Tecnológico de Costa Rica
 * 
 */


public class TablaPesos {

    // E: operacion (String) - nombre de la operación (ej. "LOAD", "INT");
    //    codigoInterrupcion (Integer o null) - solo aplica cuando operacion es "INT",
    //    el código decimal ya decodificado (0x20, 0x10, 0x09 o 0x21)
    // S: int - el peso de esa instrucción según la tabla del enunciado, o 0 si no se reconoce
    // R: ninguna
    public static int peso(String operacion, Integer codigoInterrupcion){
        switch(operacion){
            case "LOAD": return 2;
            case "STORE": return 2;
            case "MOV": return 1;
            case "ADD": return 3;
            case "SUB": return 3;
            case "INC": return 1;
            case "DEC": return 1;
            case "SWAP": return 1;
            case "JMP": return 2;
            case "CMP": return 2;
            case "JE": return 2;
            case "JNE": return 2;
            case "PARAM": return 3;
            case "PUSH": return 1;
            case "POP": return 1;

            case "INT": {
                if(codigoInterrupcion == null){
                    return 0;
                }
                switch(codigoInterrupcion){
                    case 0x20: return 2; // INT 20H — finaliza el programa
                    case 0x10: return 2; // INT 10H — imprime DX en pantalla
                    case 0x09: return 3; // INT 09H — entrada de teclado (0-255)
                    case 0x21: return 5; // INT 21H — manejo de archivos
                    default: return 0;
                }
            }

            default:
                return 0;
        }
    }

    public static void main(String[] args){

    }
}