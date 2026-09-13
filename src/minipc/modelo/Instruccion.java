package minipc.modelo;
import minipc.util.ConversorBinario;

public class Instruccion {
    private String operacion;        
    private String registro;        
    private Integer valor;           
    private String codigoBinarioOperacion;  
    private String codigoBinarioValor;      
    
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
    
 
    public String getCodigoBinarioCompleto(){
        if(codigoBinarioValor != null){
            return codigoBinarioOperacion + " " + codigoBinarioValor;
        } else {
            return codigoBinarioOperacion;
        }
    }
}