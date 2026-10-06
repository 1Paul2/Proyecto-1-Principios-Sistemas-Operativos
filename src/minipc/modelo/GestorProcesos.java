package minipc.modelo;

/**
 *
 * @author Poll Anthony Garro Vargas - 2024129001
 * @Universidad: Instituto Tecnológico de Costa Rica
 *
 */

import minipc.hardware.CPU;
import minipc.hardware.Memoria;
import minipc.hardware.Disco;
import minipc.util.ConfiguracionSistema;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * GestorProcesos: punto de entrada del "sistema operativo". Conecta CPU,
 * memoria, disco, planificador y despachador. Cada llamada a ejecutarUnPaso()
 * equivale a 1 segundo de CPU (botón "Paso a paso" / "Siguiente").
 */
public class GestorProcesos {

    private CPU cpu;
    private Memoria memoria;
    private Disco disco;
    private Planificador planificador;
    private Despachador despachador;
    private int siguienteIdProceso;

    private List<BCP> todosLosProcesos;   // todos los procesos creados, en orden de llegada
    private int reloj;                    // segundos simulados transcurridos
    private LocalTime horaArranque;       // hora real en que arrancó el sistema

    public static final int MAX_SEGUNDOS_AUTOMATICO = 100000;
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    // E: tamaños de memoria principal, zona del S.O., disco y memoria virtual
    // S: no aplica (constructor)
    // R: tamanoSistema < tamanoMemoria; tamanoDisco > 10 + tamanoVirtual
    public GestorProcesos(int tamanoMemoria, int tamanoSistema, int tamanoDisco, int tamanoVirtual) {
        this.memoria = new Memoria(tamanoMemoria, tamanoSistema);
        this.cpu = new CPU(memoria);
        this.disco = new Disco(tamanoDisco, tamanoVirtual);
        this.planificador = new Planificador(memoria, disco);
        this.cpu.setDisco(disco);
        this.despachador = new Despachador(cpu);
        this.siguienteIdProceso = 1;
        this.todosLosProcesos = new ArrayList<>();
        this.reloj = 0;
        this.horaArranque = LocalTime.now();
    }

    public GestorProcesos(ConfiguracionSistema c) {
        this(c.getMemoriaPrincipal(), c.getMemoriaSistema(), c.getAlmacenamientoSecundario(), c.getMemoriaVirtual());
    }

    // ===================== Carga de programas =====================

    // E: instrucciones (List<Instruccion>) - programa ya validado; nombreArchivo (String)
    // S: BCP - el proceso creado
    // R: lanza RuntimeException si el programa no cabe en memoria o no hay espacio en disco
    public BCP cargarPrograma(List<Instruccion> instrucciones, String nombreArchivo){
        int espacioUsuario = memoria.getTamanoTotal() - memoria.getInicioUsuario();
        int espacioVirtual = disco.getTamanoMemoriaVirtual();
        if (instrucciones.size() > espacioUsuario + espacioVirtual) {
            // Desbordamiento de memoria: ni la RAM de usuario más la memoria virtual alcanzan
            throw new RuntimeException("Desbordamiento de memoria: " + nombreArchivo + " ocupa "
                + instrucciones.size() + " posiciones y el máximo es " + (espacioUsuario + espacioVirtual)
                + " (" + espacioUsuario + " de memoria de usuario + " + espacioVirtual + " de memoria virtual)");
        }

        List<String> lineas = new ArrayList<>();
        for (Instruccion instr : instrucciones) {
            lineas.add(instr.getTextoCompleto());
        }

        // Guardar el programa en disco (si ya existe con el mismo contenido, se reutiliza)
        if (disco.existeArchivo(nombreArchivo)) {
            if (!disco.leerArchivo(nombreArchivo).equals(lineas)) {
                throw new RuntimeException("Ya existe en disco un archivo llamado " + nombreArchivo
                    + " con otro contenido");
            }
        } else if (!disco.guardarArchivo(nombreArchivo, lineas)) {
            throw new RuntimeException("No hay espacio en disco para " + nombreArchivo
                + " (el índice admite " + disco.getTamanoIndice() + " archivos y el área de archivos tiene "
                + (disco.getInicioVirtual() - disco.getTamanoIndice()) + " posiciones)");
        }

        BCP bcp = new BCP(siguienteIdProceso, "Nuevo");
        bcp.setTamanioProceso(instrucciones.size());
        bcp.agregarArchivoAbierto(nombreArchivo);
        bcp.setPrioridad(siguienteIdProceso);       // FCFS: prioridad = orden de llegada
        siguienteIdProceso++;

        todosLosProcesos.add(bcp);
        planificador.admitirBCP(bcp);
        planificador.prepararSiguientes(enEjecucion());
        mostrarMensajesSO();
        actualizarBCPsEnMemoria();
        return bcp;
    }

    // ===================== Ejecución =====================

    // E: no aplica
    // S: boolean - true si se ejecutó un segundo de CPU; false si no había nada que ejecutar
    // R: 1 llamada = 1 segundo de CPU
    public boolean ejecutarUnPaso(){
        if (cpu.getBcpActual() == null) {
            despacharSiguiente();
        }

        BCP actual = cpu.getBcpActual();
        if (actual == null) {
            return false;   // CPU ociosa: no hay procesos preparados
        }

        reloj++;
        actual.setTiempoEmpleado(actual.getTiempoEmpleado() + 1);

        try {
            cpu.tick();
        } catch (RuntimeException e) {
            // Error del proceso (pila, salto, acceso a memoria...): se termina solo ese proceso
            cpu.imprimir("ERROR P" + actual.getIdProceso() + ": " + e.getMessage());
            finalizar(actual);
            despacharSiguiente();
            actualizarBCPsEnMemoria();
            return true;
        }

        if (cpu.consumirSolicitudTeclado()) {
            // INT 09H: cambio de contexto, el proceso queda bloqueado esperando teclado
            despachador.capturar(actual);
            planificador.enEspera(actual);
            cpu.setBcpActual(null);
            cpu.imprimir(">> P" + actual.getIdProceso() + " - Ingresar valor (0-255):");
        } else if (cpu.programaTerminado()) {
            finalizar(actual);
        }

        if (cpu.getBcpActual() == null) {
            despacharSiguiente();
        }
        actualizarBCPsEnMemoria();
        return true;
    }

    // E: no aplica
    // S: no aplica (void)
    // R: se detiene si todos los procesos restantes esperan teclado, o tras MAX_SEGUNDOS_AUTOMATICO
    public void ejecutarTodo(){
        int segundos = 0;
        while (hayProcesos() && segundos < MAX_SEGUNDOS_AUTOMATICO) {
            if (!ejecutarUnPaso()) break;
            segundos++;
        }
        if (segundos >= MAX_SEGUNDOS_AUTOMATICO) {
            cpu.imprimir("Ejecución detenida tras " + MAX_SEGUNDOS_AUTOMATICO + " segundos (¿ciclo infinito?)");
        }
    }

    // E: valor (int) - número ingresado por el usuario
    // S: no aplica (void)
    // R: valor entre 0 y 255; debe haber un proceso en espera
    public void ingresarTeclado(int valor){
        if (valor < 0 || valor > 255) {
            throw new IllegalArgumentException("Solo se aceptan números entre 0 y 255");
        }
        BCP bcp = planificador.getListaEspera().getPrimero();
        if (bcp == null) {
            throw new IllegalStateException("Ningún proceso está esperando entrada de teclado");
        }
        bcp.setDxGuardado(valor);           // INT 09H guarda el valor en DX
        bcp.setDxTextoGuardado(null);
        planificador.salirDeEspera();       // vuelve a preparados (o a la lista de trabajos si estaba suspendido)
        cpu.imprimir(String.valueOf(valor));

        planificador.prepararSiguientes(enEjecucion());
        mostrarMensajesSO();
        if (cpu.getBcpActual() == null) {
            despacharSiguiente();
        }
        actualizarBCPsEnMemoria();
    }

    private void finalizar(BCP bcp){
        despachador.capturar(bcp);
        bcp.setTiempoFin(reloj);
        planificador.terminar(bcp);
        cpu.setBcpActual(null);
        cpu.reiniciarInstruccion();
    }

    private void despacharSiguiente(){
        planificador.prepararSiguientes(0);
        mostrarMensajesSO();
        BCP siguiente = planificador.siguienteProceso();
        if (siguiente != null) {
            if (siguiente.getTiempoInicio() < 0) {
                siguiente.setTiempoInicio(reloj);
            }
            siguiente.setCpuId(1);
            despachador.despachar(siguiente);
        }
    }

    private void mostrarMensajesSO(){
        for (String m : planificador.tomarMensajes()) {
            cpu.imprimir(m);
        }
    }

    private int enEjecucion(){
        return cpu.getBcpActual() != null ? 1 : 0;
    }

    // Escribe cada BCP activo en la zona del S.O. de la memoria (una posición por BCP)
    private void actualizarBCPsEnMemoria(){
        BCP enCpu = cpu.getBcpActual();
        if (enCpu != null) {
            despachador.capturar(enCpu);
            escribirBCP(enCpu);
        }
        for (BCP b = planificador.getListaProcesos().getPrimero(); b != null; b = b.getSiguiente()) {
            escribirBCP(b);
        }
        for (BCP b = planificador.getListaEspera().getPrimero(); b != null; b = b.getSiguiente()) {
            escribirBCP(b);
        }
    }

    private void escribirBCP(BCP b){
        if (b.getDireccionBCP() >= 0) {
            memoria.escribir(b.getDireccionBCP(), b.aTextoMemoria());
        }
    }

    // ===================== Consultas para la GUI =====================

    public boolean hayProcesos(){
        return cpu.getBcpActual() != null
            || !planificador.getListaTrabajos().estaVacia()
            || !planificador.getListaProcesos().estaVacia()
            || !planificador.getListaEspera().estaVacia();
    }

    public boolean esperandoTeclado(){
        return !planificador.getListaEspera().estaVacia();
    }

    // Solo quedan procesos bloqueados por teclado: la ejecución no puede avanzar
    public boolean bloqueadoPorTeclado(){
        return esperandoTeclado() && cpu.getBcpActual() == null
            && planificador.getListaProcesos().estaVacia();
    }

    public List<BCP> getTodosLosProcesos(){
        return todosLosProcesos;
    }

    public int procesosEnMemoria(){
        return planificador.procesosEnMemoria(enEjecucion());
    }

    public int getReloj(){ return reloj; }

    public String getHoraSimulada(){
        return horaArranque.plusSeconds(reloj).format(FORMATO_HORA);
    }

    // E: segundos (int) - segundos simulados desde el arranque
    // S: String - hora "HH:mm:ss" correspondiente
    // R: ninguna
    public String horaDe(int segundos){
        return horaArranque.plusSeconds(segundos).format(FORMATO_HORA);
    }

    // E: no aplica
    // S: List<String[]> - por proceso: id, archivo, inicio, fin, duración (s), tiempo CPU (s), estado
    // R: los procesos sin terminar muestran "-" en fin y duración
    public List<String[]> getEstadisticas(){
        List<String[]> filas = new ArrayList<>();
        for (BCP b : todosLosProcesos) {
            boolean inicio = b.getTiempoInicio() >= 0;
            boolean fin = b.getTiempoFin() >= 0;
            filas.add(new String[]{
                "P" + b.getIdProceso(),
                b.getArchivosAbiertos().isEmpty() ? "-" : b.getArchivosAbiertos().get(0),
                inicio ? horaDe(b.getTiempoInicio()) : "-",
                fin ? horaDe(b.getTiempoFin()) : "-",
                (inicio && fin) ? String.valueOf(b.getTiempoFin() - b.getTiempoInicio()) : "-",
                String.valueOf(b.getTiempoEmpleado()),
                b.getEstado()
            });
        }
        return filas;
    }

    public CPU getCpu() { return cpu; }
    public Memoria getMemoria() { return memoria; }
    public Disco getDisco() { return disco; }
    public Planificador getPlanificador() { return planificador; }
    public Despachador getDespachador() { return despachador; }
}