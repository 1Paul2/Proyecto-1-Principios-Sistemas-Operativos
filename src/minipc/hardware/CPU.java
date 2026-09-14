package minipc.hardware;

/**
 *
 * @author Poll Anthony Garro Vargas - 2024129001
 * @Universidad: Instituto Tecnológico de Costa Rica
 * 
 */

import minipc.modelo.TipoOPeracion;
import minipc.modelo.Instruccion;
import minipc.util.ConversorBinario;
import java.util.List;

/**
 * CPU: implementa el ciclo fetch-decode-execute del Mini PC, con sus registros
 * (PC, IR, AC, AX, BX, CX, DX) y una referencia a la Memoria sobre la que opera.
 */
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
    
    // E: memoria (Memoria) - la memoria que va a usar esta CPU
    // S: no aplica (constructor)
    // R: ninguna
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
    
    // decode ahora devuelve 3 cosas: operacion, registro, y el valor binario o null
    // E: dato (String) - el codigo binario completo leido de memoria (IR)
    // S: String[3] - [operacion, registro, valorBinario o null]
    // R: dato debe tener el formato "operador registro" o "operador registro valor"
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
    
    // E: no aplica
    // S: no aplica (void)
    // R: lee memoria en la posicion PC actual y avanza PC en 1
    public void fetch(){
        IR = memoria.leer(PC);
        PC = PC + 1;
    }
    
    // execute ahora recibe también el valorBinario puede ser null
    // E: operacion (String), registro (String), valorBinario (String o null)
    // S: no aplica (void)
    // R: modifica los registros de la CPU segun la operacion (LOAD, STORE, ADD, SUB, MOV)
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
    
    // E: instrucciones (List<Instruccion>) - el programa ya parseado a cargar
    // S: no aplica (void)
    // R: lanza RuntimeException si el programa no cabe en el espacio de usuario disponible
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
    
    // E: no aplica
    // S: no aplica (void)
    // R: ejecuta un solo ciclo fetch-decode-execute
    public void pasoAPaso(){
        fetch();
        String[] decodificado = decode(IR);
        execute(decodificado[0], decodificado[1], decodificado[2]);
    }
    
    // E: no aplica
    // S: no aplica (void)
    // R: ejecuta pasoAPaso() en ciclo hasta que el PC alcance el limite del programa
    public void ejecutarTodo(){
        while(PC < limitePrograma){
            pasoAPaso();
        }
    }
    
    // E: no aplica
    // S: boolean - true si el PC ya alcanzo o supero el limite del programa cargado
    // R: ninguna
    public boolean programaTerminado(){
        return PC >= limitePrograma;
    }
    
    public static void main(String[] args){
 
    }
}