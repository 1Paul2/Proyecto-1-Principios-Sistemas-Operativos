package minipc.modelo;

/**
 *
 * @author Poll Anthony Garro Vargas - 2024129001
 * @Universidad: Instituto Tecnológico de Costa Rica
 *
 */

import java.util.Set;

/**
 * TipoOperacion: define las operaciones e interrupciones válidas del
 * lenguaje ensamblador del Mini PC. La usa el ParserASM para validar
 * cada línea del archivo .asm.
 */
public class TipoOperacion {

    private static final Set<String> OPERACIONES = Set.of(
        "LOAD",
        "STORE",
        "MOV", 
        "ADD", 
        "SUB", 
        "INC", 
        "DEC", 
        "SWAP",
        "INT", 
        "JMP", 
        "CMP", 
        "JE", 
        "JNE", 
        "PARAM", 
        "PUSH", 
        "POP"
    );

    private static final Set<String> INTERRUPCIONES = Set.of(
        "20H", 
        "10H", 
        "09H", 
        "21H"
    );

    // E: operacion (String) - nombre de la operación, como "LOAD" o "MOV"
    // S: boolean - true si la operación existe en el lenguaje
    // R: ninguna (null devuelve false)
    public static boolean esOperacionValida(String operacion){
        return operacion != null && OPERACIONES.contains(operacion.toUpperCase());
    }

    // E: codigo (String) - código de interrupción, como "20H"
    // S: boolean - true si la interrupción existe
    // R: ninguna (null devuelve false)
    public static boolean esInterrupcionValida(String codigo){
        return codigo != null && INTERRUPCIONES.contains(codigo.toUpperCase());
    }
}