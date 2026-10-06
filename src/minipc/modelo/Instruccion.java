package minipc.modelo;

/**
 * Instruccion: representa una linea del programa .asm ya procesada, con su
 * operacion, registro y valor, lista para guardarse en memoria como texto.
 */
public class Instruccion {
    private String operacion;
    private String registro;
    private Integer valor;

    public Instruccion(String operacion, String registro, Integer valor){
        this.operacion = operacion;
        this.registro = registro;
        this.valor = valor;
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

    /**
     * Devuelve la instruccion como texto plano, lista para guardarse en memoria.
     * Ejemplos: "MOV AX 5", "LOAD AX", "INT 20H", "PARAM 5".
     */
    public String getTextoCompleto(){
        String texto = operacion;
        if(registro != null && !registro.isEmpty()){
            texto = texto + " " + registro;
        }
        if(valor != null){
            texto = texto + " " + valor;
        }
        return texto;
    }
}