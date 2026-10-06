package minipc.modelo;

import minipc.hardware.CPU;
import minipc.hardware.Memoria;
import minipc.hardware.Disco;
import java.util.List;
import java.util.ArrayList;

public class GestorProcesos {

    private CPU cpu;
    private Memoria memoria;
    private Disco disco;
    private Planificador planificador;
    private Despachador despachador;
    private int siguienteIdProceso;

    public GestorProcesos(int tamanoMemoria, int tamanoSistema, int tamanoDisco) {
        this.memoria = new Memoria(tamanoMemoria, tamanoSistema);
        this.cpu = new CPU(memoria);
        this.disco = new Disco(tamanoDisco);
        this.planificador = new Planificador(memoria);
        this.despachador = new Despachador(cpu);
        this.siguienteIdProceso = 1;
    }

    public void cargarPrograma(List<Instruccion> instrucciones, String nombreArchivo){
        BCP bcp = new BCP(siguienteIdProceso, "Nuevo");
        bcp.setTamanioProceso(instrucciones.size());
        bcp.agregarArchivoAbierto(nombreArchivo);
        siguienteIdProceso++;

        List<String> lineas = new ArrayList<>();
        for(Instruccion instr : instrucciones){
            lineas.add(instr.getTextoCompleto());
        }
        disco.guardarArchivo(nombreArchivo, lineas);

        planificador.admitirBCP(bcp);
    }

    public void ejecutarUnPaso(){
        BCP actual = cpu.getBcpActual();

        if(actual != null && !cpu.programaTerminado()){
            cpu.pasoAPaso();
        }

        if(actual != null && cpu.programaTerminado()){
            despachador.capturar(actual);
            planificador.terminar(actual);
            cpu.setBcpActual(null);
        }

        if(cpu.getBcpActual() == null){
            planificador.prepararSiguientes();
            BCP siguiente = planificador.siguienteProceso();
            if(siguiente != null){
                despachador.despachar(siguiente);
            }
        }
    }

    public void ejecutarTodo(){
        while(hayProcesos()){
            ejecutarUnPaso();
        }
    }

    public boolean hayProcesos(){
        return !planificador.getListaTrabajos().estaVacia()
            || !planificador.getListaProcesos().estaVacia()
            || !planificador.getListaEspera().estaVacia();
    }

    public CPU getCpu() { return cpu; }
    public Memoria getMemoria() { return memoria; }
    public Disco getDisco() { return disco; }
    public Planificador getPlanificador() { return planificador; }
    public Despachador getDespachador() { return despachador; }
    
    public static void main(String[] args) {
    try {
        GestorProcesos gestor = new GestorProcesos(256, 20, 512);

        // Programa 1
        List<Instruccion> p1 = new ArrayList<>();
        p1.add(new Instruccion("MOV", "AX", 5));
        p1.add(new Instruccion("MOV", "BX", 99));
        p1.add(new Instruccion("INT", "20H", null));

        // Programa 2
        List<Instruccion> p2 = new ArrayList<>();
        p2.add(new Instruccion("MOV", "CX", 7));
        p2.add(new Instruccion("MOV", "DX", 8));
        p2.add(new Instruccion("INT", "20H", null));

        gestor.cargarPrograma(p1, "programa1.asm");
        gestor.cargarPrograma(p2, "programa2.asm");

        // Ejecutar todo
        gestor.ejecutarTodo();

        System.out.println("--- Finalizados ---");
        BCP temp = gestor.getPlanificador().getListaFinalizados().getPrimero();
        while(temp != null){
            System.out.println("Proceso " + temp.getIdProceso() + " → estado " + temp.getEstado());
            temp = temp.getSiguiente();
        }

    } catch(Exception e) {
        e.printStackTrace();
    }
}
}