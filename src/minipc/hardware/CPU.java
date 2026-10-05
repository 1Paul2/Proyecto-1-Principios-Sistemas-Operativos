package minipc.hardware;

import minipc.modelo.TipoOperacion;
import minipc.modelo.Instruccion;
import minipc.util.ConversorBinario;
import java.util.List;
import minipc.modelo.BCP;
import java.util.ArrayList;
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
    private BCP bcpActual;
    private boolean flagIgual;
    
    public CPU(Memoria memoria){
        this.memoria = memoria;
        this.PC = 0;
        this.AC = 0;
        this.AX = 0;
        this.BX = 0;
        this.CX = 0;
        this.DX = 0;
        this.IR = "";   
        this.bcpActual = null;
        this.flagIgual = false;
    }
    
    public int getPC(){ return PC; }
    public int getAC(){ return AC; }
    public int getAX(){ return AX; }
    public int getBX(){ return BX; }
    public int getCX(){ return CX; }
    public int getDX(){ return DX; }
    public String getIR(){ return IR; }
    public void setAC(int ac){ this.AC = ac; }
    public void setAX(int ax){ this.AX = ax; }
    public void setBX(int bx){ this.BX = bx; }
    public void setCX(int cx){ this.CX = cx; }
    public void setDX(int dx){ this.DX = dx; }
    public void setIR(String ir){ this.IR = ir; }
    public void setPC(int nuevoPC){
        this.PC = nuevoPC;
    }
    
    public String[] decode(String dato){
        String[] partes = dato.split(" ");
        String operacion = TipoOperacion.BinarioATipo(partes[0]);
        String registro;

        if(operacion != null && operacion.equals("INT")){
            registro = ConversorBinario.BinarioAInt(partes[1]);
        } else {
            registro = ConversorBinario.BinarioARegistro(partes[1]);
        }

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
            int valor = ConversorBinario.BinarioAEntero(valorBinario);
            if(registro.equals("AX")){ AX = valor; }
            else if(registro.equals("BX")){ BX = valor; }
            else if(registro.equals("CX")){ CX = valor; }
            else if(registro.equals("DX")){ DX = valor; }
        }else if(operacion.equals("INC")){
            if(registro.equals("AX")){ AX = AX + 1; }
            else if(registro.equals("BX")){ BX = BX + 1; }
            else if(registro.equals("CX")){ CX = CX + 1; }
            else if(registro.equals("DX")){ DX = DX + 1; }
            else { AC = AC + 1; }
        }else if(operacion.equals("DEC")){
            if(registro.equals("AX")){ AX = AX - 1; }
            else if(registro.equals("BX")){ BX = BX - 1; }
            else if(registro.equals("CX")){ CX = CX - 1; }
            else if(registro.equals("DX")){ DX = DX - 1; }
            else { AC = AC - 1; }
        }else if(operacion.equals("PUSH")){
            int valor = 0;
            if(registro.equals("AX")){ valor = AX; }
            else if(registro.equals("BX")){ valor = BX; }
            else if(registro.equals("CX")){ valor = CX; }
            else if(registro.equals("DX")){ valor = DX; }
            else { valor = AC; }
            bcpActual.push(valor);
        }else if(operacion.equals("POP")){
            int valor = bcpActual.pop();
            if(registro.equals("AX")){ AX = valor; }
            else if(registro.equals("BX")){ BX = valor; }
            else if(registro.equals("CX")){ CX = valor; }
            else if(registro.equals("DX")){ DX = valor; }
            else { AC = valor; }
        }else if(operacion.equals("JMP")){
            int desplazamiento = ConversorBinario.BinarioAEntero(valorBinario);
            PC = PC + desplazamiento - 1;
        }else if(operacion.equals("CMP")){
            int valor = 0;
            if(registro.equals("AX")){ valor = AX; }
            else if(registro.equals("BX")){ valor = BX; }
            else if(registro.equals("CX")){ valor = CX; }
            else if(registro.equals("DX")){ valor = DX; }
            flagIgual = (AC == valor);
        }else if(operacion.equals("JE")){
            if(flagIgual){
                int desplazamiento = ConversorBinario.BinarioAEntero(valorBinario);
                PC = PC + desplazamiento - 1;
            }
        }else if(operacion.equals("JNE")){
            if(!flagIgual){
                int desplazamiento = ConversorBinario.BinarioAEntero(valorBinario);
                PC = PC + desplazamiento - 1;
            }
        }else if(operacion.equals("SWAP")){
            int temp;
            if(registro.equals("AX")){ temp = AX; AX = AC; AC = temp; }
            else if(registro.equals("BX")){ temp = BX; BX = AC; AC = temp; }
            else if(registro.equals("CX")){ temp = CX; CX = AC; AC = temp; }
            else if(registro.equals("DX")){ temp = DX; DX = AC; AC = temp; }
        }else if(operacion.equals("PARAM")){
            int valor = ConversorBinario.BinarioAEntero(valorBinario);
            bcpActual.push(valor);
        }else if(operacion.equals("INT")){
            if(registro != null){
                if(registro.equals("INT20")){
                    PC = limitePrograma;
                }else if(registro.equals("INT10")){
                    System.out.println("PANTALLA: " + DX);
                }else if(registro.equals("INT09")){
                    System.out.println("TECLADO: (no implementado aún)");
                }else if(registro.equals("INT21")){
                    System.out.println("ARCHIVOS: (no implementado aún)");
                }
            }
        }
   }
    
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
    
    public BCP getBcpActual() {
        return bcpActual;
    }

    public void setBcpActual(BCP bcp) {
        this.bcpActual = bcp;
    }
    
    public boolean isFlagIgual() {
    return flagIgual;
}

    public void setFlagIgual(boolean flag) {
        this.flagIgual = flag;
    }
    
    public static void main(String[] args) {
        Memoria memoria = new Memoria(256, 20);
        CPU cpu = new CPU(memoria);

        BCP bcp = new BCP(1, "Nuevo");
        bcp.setTamanioProceso(10);
        bcp.setDireccionBase(20);
        cpu.setBcpActual(bcp);

        List<Instruccion> programa = new ArrayList<>();
        programa.add(new Instruccion("MOV", "AX", 5));    // AX = 5
        programa.add(new Instruccion("MOV", "BX", 99));   // BX = 99
        programa.add(new Instruccion("INT", "AX", 1));    // INT 20H (AX → binario 0001)
        programa.add(new Instruccion("MOV", "CX", 99));   // no se ejecuta

        cpu.cargarPrograma(programa);

        while (!cpu.programaTerminado()) {
            cpu.pasoAPaso();
            System.out.println("PC=" + cpu.getPC()
                + " AX=" + cpu.getAX()
                + " BX=" + cpu.getBX()
                + " CX=" + cpu.getCX());
        }

        System.out.println("--- FINAL ---");
        System.out.println("AX = " + cpu.getAX());  // 5
        System.out.println("BX = " + cpu.getBX());  // 99
        System.out.println("CX = " + cpu.getCX());  // 0 (no se ejecutó)
    }
}