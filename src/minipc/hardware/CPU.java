package minipc.hardware;
import minipc.modelo.TipoOPeracion;
import minipc.modelo.Instruccion;
import minipc.util.ConversorBinario;
import java.util.List;

public class CPU {
    private int PC;
    private String IR;
    private int AC;
    private int AX;
    private int BX;
    private int CX;
    private int DX;
    private Memoria memoria;
    private int limitePrograma;
    
    public CPU(Memoria memoria){
        this.memoria = memoria;
        this.PC = 0;
        this.AC = 0;
        this.AX = 0;
        this.BX = 0;
        this.CX = 0;
        this.DX = 0;
        this.IR = "";   
    }
    
    public int getPC(){ return PC; }
    public int getAC(){ return AC; }
    public int getAX(){ return AX; }
    public int getBX(){ return BX; }
    public int getCX(){ return CX; }
    public int getDX(){ return DX; }
    public String getIR(){ return IR; }
    
    public void setPC(int nuevoPC){
        this.PC = nuevoPC;
    }
    
    // decode ahora devuelve 3 cosas: operacion, registro, y el valor binario (o null si no aplica)
    public String[] decode(String dato){
        String[] partes = dato.split(" ");
        String operacion = TipoOPeracion.BinarioATipo(partes[0]);
        String registro = ConversorBinario.BinarioARegistro(partes[1]);
        
        String valorBinario = null;
        if(partes.length == 3){
            valorBinario = partes[2];
        }
        
        String[] resultado = new String[3];
        resultado[0] = operacion;
        resultado[1] = registro;
        resultado[2] = valorBinario;
        return resultado;
    }
    
    public void fetch(){
        IR = memoria.leer(PC);
        PC = PC + 1;
    }
    
    // execute ahora recibe también el valorBinario (puede ser null)
    public void execute(String operacion, String registro, String valorBinario){
        if(operacion.equals("LOAD")){
            if(registro.equals("AX")){ AC = AX; }
            else if(registro.equals("BX")){ AC = BX; }
            else if(registro.equals("CX")){ AC = CX; }
            else if(registro.equals("DX")){ AC = DX; }
        }else if(operacion.equals("STORE")){
            if(registro.equals("AX")){ AX = AC; }
            else if(registro.equals("BX")){ BX = AC; }
            else if(registro.equals("CX")){ CX = AC; }
            else if(registro.equals("DX")){ DX = AC; }
        }else if(operacion.equals("SUB")){
            if(registro.equals("AX")){ AC = AC - AX; }
            else if(registro.equals("BX")){ AC = AC - BX; }
            else if(registro.equals("CX")){ AC = AC - CX; }
            else if(registro.equals("DX")){ AC = AC - DX; }
        }else if(operacion.equals("ADD")){
            if(registro.equals("AX")){ AC = AC + AX; }
            else if(registro.equals("BX")){ AC = AC + BX; }
            else if(registro.equals("CX")){ AC = AC + CX; }
            else if(registro.equals("DX")){ AC = AC + DX; }
        }else if(operacion.equals("MOV")){
            // YA NO leemos memoria en PC ni avanzamos PC extra: el valor ya vino en decode()
            int valor = ConversorBinario.BinarioAEntero(valorBinario);
            if(registro.equals("AX")){ AX = valor; }
            else if(registro.equals("BX")){ BX = valor; }
            else if(registro.equals("CX")){ CX = valor; }
            else if(registro.equals("DX")){ DX = valor; }
        }
    }
    
    // cargarPrograma ahora avanza SIEMPRE 1 posición por instrucción
    public void cargarPrograma(List<Instruccion> instrucciones){
        int posicionActual = memoria.getInicioUsuario();
        int espacioDisponible = memoria.getTamanoTotal() - memoria.getInicioUsuario();

        if(instrucciones.size() > espacioDisponible){
            throw new RuntimeException("El programa necesita " + instrucciones.size() + " de memoria, pero solo hay " + espacioDisponible + " memoria disponibles para el usuario.");
        }

        for(int i = 0; i < instrucciones.size(); i++){
            Instruccion instr = instrucciones.get(i);
            memoria.escribir(posicionActual, instr.getCodigoBinarioCompleto());
            posicionActual = posicionActual + 1;
        }

        this.limitePrograma = posicionActual;
        this.PC = memoria.getInicioUsuario();
    }
    
    public void pasoAPaso(){
        fetch();
        String[] decodificado = decode(IR);
        execute(decodificado[0], decodificado[1], decodificado[2]);
    }
    
    public void ejecutarTodo(){
        while(PC < limitePrograma){
            pasoAPaso();
        }
    }
           public boolean programaTerminado(){
            return PC >= limitePrograma;
        }
    
    public static void main(String[] args){
 
    }
}