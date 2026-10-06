package minipc.hardware;

import minipc.modelo.Instruccion;
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
    private int segundosRestantes = 0;
    private String[] instrActual;
    
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

    // ===== Getters y Setters =====

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
    public void setPC(int nuevoPC){ this.PC = nuevoPC; }

    public BCP getBcpActual(){ return bcpActual; }
    public void setBcpActual(BCP bcp){ this.bcpActual = bcp; }

    public boolean isFlagIgual(){ return flagIgual; }
    public void setFlagIgual(boolean flag){ this.flagIgual = flag; }

    // ===== Decode: ahora solo separa por espacios =====

    public String[] decode(String dato){
        String[] partes = dato.split(" ");
        String operacion = partes[0];
        String registro = null;
        if(partes.length >= 2){
            registro = partes[1];
        }
        String valorTexto = null;
        if(partes.length >= 3){
            valorTexto = partes[2];
        }
        String[] resultado = new String[3];
        resultado[0] = operacion;
        resultado[1] = registro;
        resultado[2] = valorTexto;
        return resultado;
    }

    public void fetch(){
        IR = memoria.leer(PC);
        PC = PC + 1;
    }

    // ===== Execute: switch + metodos =====

    public void execute(String operacion, String registro, String valorTexto){
        if(operacion == null) return;

        switch (operacion) {
            case "LOAD":  ejecutarLOAD(registro); break;
            case "STORE": ejecutarSTORE(registro); break;
            case "MOV":   ejecutarMOV(registro, valorTexto); break;
            case "SUB":   ejecutarSUB(registro); break;
            case "ADD":   ejecutarADD(registro); break;
            case "INC":   ejecutarINC(registro); break;
            case "DEC":   ejecutarDEC(registro); break;
            case "PUSH":  ejecutarPUSH(registro); break;
            case "POP":   ejecutarPOP(registro); break;
            case "JMP":   ejecutarJMP(valorTexto); break;
            case "CMP":   ejecutarCMP(registro); break;
            case "JE":    ejecutarJE(valorTexto); break;
            case "JNE":   ejecutarJNE(valorTexto); break;
            case "SWAP":  ejecutarSWAP(registro); break;
            case "PARAM": ejecutarPARAM(valorTexto); break;
            case "INT":   ejecutarINT(registro); break;
            default: break;
        }
    }

    private int leerRegistro(String registro){
        if(registro == null) return 0;
        switch (registro) {
            case "AX": return AX;
            case "BX": return BX;
            case "CX": return CX;
            case "DX": return DX;
            default: return AC;
        }
    }

    private void escribirRegistro(String registro, int valor){
        if(registro == null){ AC = valor; return; }
        switch (registro) {
            case "AX": AX = valor; break;
            case "BX": BX = valor; break;
            case "CX": CX = valor; break;
            case "DX": DX = valor; break;
            default: AC = valor; break;
        }
    }

    private void ejecutarLOAD(String registro){
        AC = leerRegistro(registro);
    }

    private void ejecutarSTORE(String registro){
        escribirRegistro(registro, AC);
    }

    private void ejecutarMOV(String registro, String valorTexto){
        int valor = Integer.parseInt(valorTexto);
        escribirRegistro(registro, valor);
    }

    private void ejecutarSUB(String registro){
        AC = AC - leerRegistro(registro);
    }

    private void ejecutarADD(String registro){
        AC = AC + leerRegistro(registro);
    }

    private void ejecutarINC(String registro){
        escribirRegistro(registro, leerRegistro(registro) + 1);
    }

    private void ejecutarDEC(String registro){
        escribirRegistro(registro, leerRegistro(registro) - 1);
    }

    private void ejecutarPUSH(String registro){
        int valor = leerRegistro(registro);
        bcpActual.push(valor);
    }

    private void ejecutarPOP(String registro){
        int valor = bcpActual.pop();
        escribirRegistro(registro, valor);
    }

    private void ejecutarJMP(String valorTexto){
        int desplazamiento = Integer.parseInt(valorTexto);
        PC = PC + desplazamiento - 1;
    }

    private void ejecutarCMP(String registro){
        int valor = leerRegistro(registro);
        flagIgual = (AC == valor);
    }

    private void ejecutarJE(String valorTexto){
        if(flagIgual){
            int desplazamiento = Integer.parseInt(valorTexto);
            PC = PC + desplazamiento - 1;
        }
    }

    private void ejecutarJNE(String valorTexto){
        if(!flagIgual){
            int desplazamiento = Integer.parseInt(valorTexto);
            PC = PC + desplazamiento - 1;
        }
    }

    private void ejecutarSWAP(String registro){
        int valor = leerRegistro(registro);
        escribirRegistro(registro, AC);
        AC = valor;
    }

    private void ejecutarPARAM(String valorTexto){
        int valor = Integer.parseInt(valorTexto);
        bcpActual.push(valor);
    }

    private void ejecutarINT(String registro){
        if(registro == null) return;
        switch (registro) {
            case "20H": PC = limitePrograma; break;
            case "10H": System.out.println("PANTALLA: " + DX); break;
            case "09H": System.out.println("TECLADO: (no implementado)"); break;
            case "21H": System.out.println("ARCHIVOS: (no implementado)"); break;
            default: break;
        }
    }

    // ===== Carga de programa =====

    public void cargarPrograma(List<Instruccion> instrucciones){
        int posicionActual = bcpActual.getDireccionBase();
        int espacioDisponible = memoria.getTamanoTotal() - posicionActual;

        if(instrucciones.size() > espacioDisponible){
            throw new RuntimeException("El programa necesita " + instrucciones.size() +
                " de memoria, pero solo hay " + espacioDisponible + " disponibles.");
        }

        for(int i = 0; i < instrucciones.size(); i++){
            Instruccion instr = instrucciones.get(i);
            memoria.escribir(posicionActual, instr.getTextoCompleto());
            posicionActual = posicionActual + 1;
        }

        this.limitePrograma = posicionActual;
        this.PC = bcpActual.getDireccionBase();
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
        return segundosRestantes == 0 && PC >= limitePrograma;
    }

    // Llamalo desde Despachador.despachar() al cargar un proceso nuevo
    public void reiniciarInstruccion(){
        segundosRestantes = 0;
        instrActual = null;
    }

    // Para mostrar en la GUI cuántos segundos le faltan a la instrucción actual
    public int getSegundosRestantes(){ return segundosRestantes; }

    public void tick(){                 // 1 llamada = 1 segundo
    if (segundosRestantes == 0) {
        fetch();
        instrActual = decode(IR);
        segundosRestantes = Pesos.de(instrActual[0], instrActual[1]);
    }
    segundosRestantes--;
    if (segundosRestantes == 0) {
        execute(instrActual[0], instrActual[1], instrActual[2]);
    }
}
}