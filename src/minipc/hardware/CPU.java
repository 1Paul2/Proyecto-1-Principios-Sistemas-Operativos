/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minipc.hardware;
import minipc.modelo.TipoOPeracion;
import minipc.util.ConversorBinario;
import minipc.modelo.Instruccion;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author elenanito
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
    
    public int getPC(){
        return PC;
    }

    public int getAC(){
        return AC;
    }

    public int getAX(){
        return AX;
    }

    public int getBX(){
        return BX;
    }

    public int getCX(){
        return CX;
    }

    public int getDX(){
        return DX;
    }

    public String getIR(){
        return IR;
    }
    
    
    public String[] decode(String dato){
        String[] partes = dato.split(" ");
        String operacion = TipoOPeracion.BinarioATipo(partes[0]);
        String registro = ConversorBinario.BinarioARegistro(partes[1]);

        String[] resultado = new String[2];
        resultado[0] = operacion;
        resultado[1] = registro;
        return resultado;
    }
    
    public void fetch(){
        IR = memoria.leer(PC);
        PC = PC + 1;
    }
    
    public void setPC(int nuevoPC){
    this.PC = nuevoPC;
    }
    
    public void execute(String operacion, String registro){
        if(operacion.equals("LOAD")){
            if(registro.equals("AX")){
                AC = AX;
            }else if(registro.equals("BX")){
                AC = BX;
            }else if(registro.equals("CX")){
                AC = CX;
            }else if(registro.equals("DX")){
                AC = DX;
            }
        }else if(operacion.equals("STORE")){
            if(registro.equals("AX")){
                AX = AC;
            }else if(registro.equals("BX")){
                BX = AC;
            }else if(registro.equals("CX")){
                CX = AC;
            }else if(registro.equals("DX")){
                DX = AC;
            }
        }else if(operacion.equals("SUB")){
            if(registro.equals("AX")){
                AC = AC - AX;
            }else if(registro.equals("BX")){
                AC = AC - BX;
            }else if(registro.equals("CX")){
                AC = AC - CX;
            }else if(registro.equals("DX")){
                AC = AC - DX;
            }
        }else if(operacion.equals("ADD")){
            if(registro.equals("AX")){
                AC = AC + AX;
            }else if(registro.equals("BX")){
                AC = AC + BX;
            }else if(registro.equals("CX")){
                AC = AC + CX;
            }else if(registro.equals("DX")){
                AC = AC + DX;
            }
        }else if(operacion.equals("MOV")){
            String valorBinario = memoria.leer(PC);
            int valor = ConversorBinario.BinarioAEntero(valorBinario);
            PC = PC + 1;

            if(registro.equals("AX")){
                AX = valor;
            }else if(registro.equals("BX")){
                BX = valor;
            }else if(registro.equals("CX")){
                CX = valor;
            }else if(registro.equals("DX")){
                DX = valor;
            }
        }
        
    }
    
    public void cargarPrograma(List<Instruccion> instrucciones){
        int posicionActual = memoria.getInicioUsuario();
        for(int x = 0; x < instrucciones.size(); x++){
            Instruccion instr = instrucciones.get(x);
            
            memoria.escribir(posicionActual, instr.getcodigoBinarioOperacion());
            posicionActual = posicionActual + 1;

            
            if(instr.getcodigoBinarioValor() != null){
                memoria.escribir(posicionActual, instr.getcodigoBinarioValor());
                posicionActual = posicionActual + 1;
            }
        }

        this.limitePrograma = posicionActual;
        this.PC = memoria.getInicioUsuario();
     }
    
    public void pasoAPaso(){
        fetch();
        String[] decodificado = decode(IR);
        execute(decodificado[0], decodificado[1]);
    }
    
    public void ejecutarTodo(){
        while(PC < limitePrograma){
            pasoAPaso();
        }
    }

    public static void main(String[] args){
        List<Instruccion> instrucciones = new ArrayList<>();
        instrucciones.add(new Instruccion("MOV", "AX", 5));
        instrucciones.add(new Instruccion("MOV", "BX", 3));
        instrucciones.add(new Instruccion("LOAD", "AX", null));
        instrucciones.add(new Instruccion("ADD", "BX", null));
        instrucciones.add(new Instruccion("SUB", "AX", null));
        instrucciones.add(new Instruccion("STORE", "AX", null));
        instrucciones.add(new Instruccion("MOV", "BX", -8));

        Memoria memoria = new Memoria(128, 64);
        CPU cpu = new CPU(memoria);

        cpu.cargarPrograma(instrucciones);
        cpu.ejecutarTodo();

        System.out.println("AC: " + cpu.getAC());  
        System.out.println("AX: " + cpu.getAX());  
        System.out.println("BX: " + cpu.getBX());  
    }
}
