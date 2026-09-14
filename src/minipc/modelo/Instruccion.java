package minipc.modelo;
/**
 *
 * @author Poll Anthony Garro Vargas - 2024129001
 * @Universidad: Instituto Tecnológico de Costa Rica
 * 
 */
import minipc.util.ConversorBinario;

/**
 * Instruccion: representa una linea del programa .asm ya procesada, con su
 * operacion, registro, valor y los codigos binarios correspondientes,
 * lista para guardarse en memoria y mostrarse en la interfaz.
 */
public class Instruccion {
    private String operacion;        
    private String registro;        
    private Integer valor;           
    private String codigoBinarioOperacion;  
    private String codigoBinarioValor;      
    
    // E: operacion (String) - ej. "MOV", "LOAD"; registro (String) valor (Integer) - puede ser null si la operacion no lleva valor numerico
    // S: no aplica (constructor)
    // R: operacion y registro deben ser validos segun TipoOPeracion y ConversorBinario
    public Instruccion(String operacion, String registro, Integer valor){
        this.operacion = operacion;
        this.registro = registro;
        this.valor = valor;
        
        this.codigoBinarioOperacion = TipoOPeracion.Tipo(operacion) + " " + ConversorBinario.RegistroBinario(registro);
        
        if(valor != null){
            this.codigoBinarioValor = ConversorBinario.NumeroBinario(valor);
        } else {
            this.codigoBinarioValor = null;
        }
    }
    
    public String getoperacion(){
        return operacion;
    }
    
    public String getregistro(){
        return registro;
    }
    
    public Integer getvalor(){
        return valor;
    }
    
    public String getcodigoBinarioOperacion(){
        return codigoBinarioOperacion;
    }
    
    public String getcodigoBinarioValor(){
        return codigoBinarioValor;
    }
    
    // E: no aplica
    // S: String - el codigo binario completo de la instruccion (operacion+registro, mas el valor si existe), listo para guardarse en una sola posicion de memoria
    // R: ninguna
    public String getCodigoBinarioCompleto(){
        if(codigoBinarioValor != null){
            return codigoBinarioOperacion + " " + codigoBinarioValor;
        } else {
            return codigoBinarioOperacion;
        }
    }
}