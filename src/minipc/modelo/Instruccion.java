package minipc.modelo;
/**
 *
 * @author Poll Anthony Garro Vargas - 2024129001
 * @Universidad: Instituto Tecnológico de Costa Rica
 * 
 */
import minipc.util.ConversorBinario;

/**
 * Instruccion: representa una linea del programa .asm ya procesada, con sus
 * operandos (registros, valores, o código de interrupción según el tipo de
 * operación) y el código binario completo correspondiente, listo para
 * guardarse en memoria y mostrarse en la interfaz.
 *
 * NOTA de diseño: como ahora hay operaciones con formas muy distintas de
 * operandos (0, 1 o 2 registros; 0 o 1 valor; hasta 3 valores para PARAM;
 * un código de interrupción para INT), se generalizó la clase con campos
 * opcionales (quedan en null los que no aplican a la operación). Los
 * getters "clásicos" (getregistro, getvalor) se mantienen para no romper
 * código existente, pero ya NO existen getcodigoBinarioOperacion() /
 * getcodigoBinarioValor() por separado, porque con dos registros o varios
 * valores esa división en dos partes ya no tiene sentido — revisar si algo
 * más (por ejemplo la GUI) dependía de esos dos getters.
 */
public class Instruccion {
    private String operacion;
    private String registro;            // primer registro (o null)
    private String registro2;           // segundo registro: CMP, SWAP (o null)
    private Integer valor;              // valor simple: MOV con valor, JMP/JE/JNE desplazamiento (o null)
    private Integer[] valoresParam;     // hasta 3 valores: PARAM (o null)
    private String codigoInterrupcion;  // "20H","10H","09H","21H": INT (o null)

    private String codigoBinarioCompleto;

    // Constructor general: cualquier campo que no aplique a la operación va en null
    // E: operacion (String); registro, registro2 (String o null); valor (Integer o null);
    //    valoresParam (Integer[] o null, máx 3); codigoInterrupcion (String o null, ej. "20H")
    // S: no aplica (constructor)
    // R: los campos deben ser consistentes con lo que espera esa operación (ver construirCodigoBinario)
    public Instruccion(String operacion, String registro, String registro2, Integer valor, Integer[] valoresParam, String codigoInterrupcion){
        this.operacion = operacion;
        this.registro = registro;
        this.registro2 = registro2;
        this.valor = valor;
        this.valoresParam = valoresParam;
        this.codigoInterrupcion = codigoInterrupcion;

        this.codigoBinarioCompleto = construirCodigoBinario();
    }

    // Constructor de conveniencia para las operaciones "clásicas" de un solo
    // registro y/o un solo valor (LOAD, STORE, ADD, SUB, MOV con valor,
    // INC/DEC con registro, PUSH, POP). Se mantiene por compatibilidad con
    // el código que ya llamaba new Instruccion(operacion, registro, valor).
    public Instruccion(String operacion, String registro, Integer valor){
        this(operacion, registro, null, valor, null, null);
    }

    // E: no aplica
    // S: String - el código binario completo de la instrucción, listo para
    //    guardarse en una sola posición de memoria
    // R: operacion debe ser una de las operaciones válidas de TipoOPeracion
    private String construirCodigoBinario(){
        String opBin = TipoOPeracion.Tipo(operacion);
        if(opBin == null){
            throw new RuntimeException("Operación no válida: " + operacion);
        }
        StringBuilder sb = new StringBuilder(opBin);

        switch(operacion){
            case "LOAD":
            case "STORE":
            case "ADD":
            case "SUB":
            case "PUSH":
            case "POP":
                sb.append(" ").append(ConversorBinario.RegistroBinario(registro));
                break;

            case "MOV":
                sb.append(" ").append(ConversorBinario.RegistroBinario(registro));
                if(registro2 != null){
                    sb.append(" ").append(ConversorBinario.RegistroBinario(registro2));
                } else {
                    sb.append(" ").append(ConversorBinario.NumeroBinario(valor));
                }
                break;

            case "INC":
            case "DEC":
                // si registro es null, la operación actúa sobre AC y no lleva operando
                if(registro != null){
                    sb.append(" ").append(ConversorBinario.RegistroBinario(registro));
                }
                break;

            case "SWAP":
            case "CMP":
                sb.append(" ").append(ConversorBinario.RegistroBinario(registro));
                sb.append(" ").append(ConversorBinario.RegistroBinario(registro2));
                break;

            case "JMP":
            case "JE":
            case "JNE":
                sb.append(" ").append(ConversorBinario.NumeroBinario(valor));
                break;

            case "PARAM":
                for(int i = 0; i < valoresParam.length; i++){
                    sb.append(" ").append(ConversorBinario.NumeroBinario(valoresParam[i]));
                }
                break;

            case "INT": {
                int codigo = ConversorBinario.HexAEntero(codigoInterrupcion);
                sb.append(" ").append(ConversorBinario.NumeroBinario(codigo));
                break;
            }

            default:
                throw new RuntimeException("Operación no reconocida al construir el código binario: " + operacion);
        }

        return sb.toString();
    }

    public String getoperacion(){
        return operacion;
    }
    
    public String getregistro(){
        return registro;
    }

    public String getregistro2(){
        return registro2;
    }
    
    public Integer getvalor(){
        return valor;
    }

    public Integer[] getvaloresParam(){
        return valoresParam;
    }

    public String getcodigoInterrupcion(){
        return codigoInterrupcion;
    }

    // NUEVO: representación legible de la instrucción (para mostrar en la
    // GUI: TablaDisco, tabla de memoria, etc.), sin importar la forma de
    // operandos que tenga cada operación.
    // E: no aplica
    // S: String - la instrucción tal como se vería en el .asm original
    // R: ninguna
    public String getTextoLegible(){
        switch(operacion){
            case "LOAD":
            case "STORE":
            case "ADD":
            case "SUB":
            case "PUSH":
            case "POP":
                return operacion + " " + registro;

            case "MOV":
                if(registro2 != null){
                    return operacion + " " + registro + ", " + registro2;
                } else {
                    return operacion + " " + registro + ", " + valor;
                }

            case "INC":
            case "DEC":
                return (registro != null) ? (operacion + " " + registro) : operacion;

            case "SWAP":
            case "CMP":
                return operacion + " " + registro + ", " + registro2;

            case "JMP":
            case "JE":
            case "JNE":
                return operacion + " " + (valor >= 0 ? "+" : "") + valor;

            case "PARAM": {
                StringBuilder sb = new StringBuilder(operacion + " ");
                for(int i = 0; i < valoresParam.length; i++){
                    sb.append(valoresParam[i]);
                    if(i < valoresParam.length - 1){
                        sb.append(", ");
                    }
                }
                return sb.toString();
            }

            case "INT":
                return operacion + " " + codigoInterrupcion;

            default:
                return operacion;
        }
    }
    
    // E: no aplica
    // S: String - el codigo binario completo de la instruccion, listo para guardarse en una sola posicion de memoria
    // R: ninguna
    public String getCodigoBinarioCompleto(){
        return codigoBinarioCompleto;
    }
}