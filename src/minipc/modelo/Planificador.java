package minipc.modelo;

/**
 *
 * @author Poll Anthony Garro Vargas - 2024129001
 * @Universidad: Instituto Tecnológico de Costa Rica
 *
 */

import minipc.hardware.Memoria;
import minipc.hardware.Disco;
import java.util.ArrayList;
import java.util.List;

/**
 * Planificador: administra la Lista de Trabajos (programas en disco esperando
 * memoria) y la Lista de Procesos (procesos en memoria). Cumple dos roles:
 *  - Planificador de trabajos (largo plazo): prepararSiguientes() pasa
 *    programas del disco a memoria cuando hay espacio.
 *    Si un programa no cabe completo en RAM, la parte que sobra se carga en la
 *    memoria virtual del disco.
 *    Si un proceso bloqueado por teclado impide que entre otro, se suspende
 *    (sale de memoria) y se recarga cuando recibe su valor.
 *  - Planificador de CPU (corto plazo, FCFS): siguienteProceso() entrega el
 *    primer proceso preparado al despachador.
 */
public class Planificador {

    // Colas
    private ColaTrabajo listaTrabajos;     // Nuevo: en disco, sin memoria
    private ColaTrabajo listaProcesos;     // Preparado: en memoria, esperando CPU
    private ColaTrabajo listaEspera;       // En espera: bloqueado por E/S 
    private ColaTrabajo listaFinalizados;  // Finalizado

    private Memoria memoria;
    private Disco disco;

    // Posiciones de la zona del S.O. donde se guardan los BCP (una por BCP)
    private boolean[] slotsBCP;

    public static final int MAX_PROCESOS_EN_MEMORIA = 5;

    // Avisos del S.O. (p. ej. uso de memoria virtual) que el gestor muestra en pantalla
    private final List<String> mensajes = new ArrayList<>();

    // E: no aplica
    // S: List<String> - avisos pendientes; la lista interna queda vacía
    // R: ninguna
    public List<String> tomarMensajes(){
        List<String> copia = new ArrayList<>(mensajes);
        mensajes.clear();
        return copia;
    }

    // E: memoria (Memoria), disco (Disco)
    // S: no aplica (constructor)
    // R: la zona del S.O. debe tener al menos 1 posición para guardar BCPs
    public Planificador(Memoria memoria, Disco disco) {
        this.listaTrabajos = new ColaTrabajo();
        this.listaProcesos = new ColaTrabajo();
        this.listaEspera = new ColaTrabajo();
        this.listaFinalizados = new ColaTrabajo();
        this.memoria = memoria;
        this.disco = disco;
        int slots = Math.min(MAX_PROCESOS_EN_MEMORIA, memoria.getInicioUsuario());
        this.slotsBCP = new boolean[slots];
    }

    // E: bcp (BCP) - proceso recién creado
    // S: no aplica (void)
    // R: ninguna
    public void admitirBCP(BCP bcp){
        bcp.setEstado("Nuevo");
        listaTrabajos.encolar(bcp);
    }

    // E: enEjecucion (int) - 1 si hay un proceso en la CPU, 0 si no
    // S: no aplica (void)
    // R: respeta FCFS estricto: si el primero no cabe, los de atrás esperan.
    //    Si el primero no puede entrar (límite de 5 procesos o falta de memoria) y hay
    //    un proceso en memoria bloqueado esperando el teclado, ese proceso se SUSPENDE
    //    (sale de la memoria) para darle espacio.
    public void prepararSiguientes(int enEjecucion) {
        while (!listaTrabajos.estaVacia()) {
            boolean cargado = procesosEnMemoria(enEjecucion) < MAX_PROCESOS_EN_MEMORIA
                && intentarCargar(listaTrabajos.getPrimero());
            if (cargado) continue;
            if (!suspenderUnoEnEspera(listaTrabajos.getPrimero())) break;   // nadie para suspender: espera
        }
    }

    // E: bcp (BCP) - primer trabajo de la lista de trabajos
    // S: boolean - true si se pudo cargar en memoria (y pasó a la lista de procesos)
    // R: si no cabe completo en RAM, la parte que sobra va a la memoria virtual
    private boolean intentarCargar(BCP bcp){
        int slot = slotLibre();
        if (slot == -1) return false;                        // no hay espacio para su BCP

        int tamano = bcp.getTamanioProceso();
        List<String> lineas = disco.leerArchivo(bcp.getArchivosAbiertos().get(0));

        int hueco = memoria.encontrarHueco(tamano);
        int enRAM = tamano;
        int baseVirtual = -1;

        if (hueco == -1) {
            // No cabe completo: se carga en RAM lo que quepa en el hueco más grande
            // y el resto se manda a la memoria virtual del disco
            int[] mayor = memoria.huecoMasGrande();
            if (mayor[0] == -1) return false;                // RAM llena
            enRAM = Math.min(mayor[1], tamano);
            baseVirtual = disco.buscarEspacioVirtual(tamano - enRAM);
            if (baseVirtual == -1) return false;             // memoria virtual llena
            hueco = mayor[0];
            disco.escribirVirtual(baseVirtual, lineas.subList(enRAM, tamano));
            mensajes.add("[SO] P" + bcp.getIdProceso() + " no cabe completo en RAM: "
                + enRAM + " instrucciones en memoria [" + hueco + "-" + (hueco + enRAM - 1) + "] y "
                + (tamano - enRAM) + " en memoria virtual (disco [" + baseVirtual + "-"
                + (baseVirtual + tamano - enRAM - 1) + "])");
        }

        // Cargar en memoria principal la parte que cabe
        for (int i = 0; i < enRAM; i++) {
            memoria.escribir(hueco + i, lineas.get(i));
        }
        bcp.setEnRAM(enRAM);
        bcp.setBaseVirtual(baseVirtual);
        bcp.setDireccionBase(hueco);

        if (bcp.getDesplazamientoPC() >= 0) {
            // Proceso que estaba suspendido: se reubica y continúa donde iba
            bcp.setPcGuardado(hueco + bcp.getDesplazamientoPC());
            bcp.setDesplazamientoPC(-1);
            mensajes.add("[SO] P" + bcp.getIdProceso() + " vuelve a memoria (nueva base " + hueco + ")");
        } else {
            bcp.setPcGuardado(hueco);
        }

        slotsBCP[slot] = true;
        bcp.setDireccionBCP(slot);
        bcp.setEstado("Preparado");

        listaTrabajos.desencolar();
        listaProcesos.encolar(bcp);
        return true;
    }

    // E: necesita (BCP) - el trabajo que no pudo entrar a memoria
    // S: boolean - true si se suspendió algún proceso
    // R: solo se suspenden procesos "En espera" (bloqueados por teclado), que no usan la CPU.
    //    Su programa sigue guardado en disco, así que al volver se recarga desde ahí.
    private boolean suspenderUnoEnEspera(BCP necesita){
        for (BCP b = listaEspera.getPrimero(); b != null; b = b.getSiguiente()) {
            if (!"En espera".equals(b.getEstado())) continue;

            b.setDesplazamientoPC(b.getPcGuardado() - b.getDireccionBase());
            liberarMemoria(b);
            b.setEstado(BCP.SUSPENDIDO_ESPERA);      
            mensajes.add("[SO] P" + b.getIdProceso() + " suspendido: sale de memoria mientras espera el teclado,"
                + " para dar espacio a P" + necesita.getIdProceso());
            return true;
        }
        return false;
    }

    // Libera la RAM, la memoria virtual y la posición del BCP de un proceso
    private void liberarMemoria(BCP bcp){
        if (bcp.getDireccionBase() >= 0) {
            memoria.liberar(bcp.getDireccionBase(), bcp.getEnRAM());
        }
        if (bcp.usaMemoriaVirtual()) {
            disco.liberarVirtual(bcp.getBaseVirtual(), bcp.getEnVirtual());
        }
        if (bcp.getDireccionBCP() >= 0) {
            memoria.liberar(bcp.getDireccionBCP(), 1);
            slotsBCP[bcp.getDireccionBCP()] = false;
            bcp.setDireccionBCP(-1);
        }
        bcp.setDireccionBase(-1);
        bcp.setEnRAM(0);
        bcp.setBaseVirtual(-1);
    }

    // E: enEjecucion (int) - 1 si hay un proceso en la CPU
    // S: int - procesos que ocupan memoria (los suspendidos no cuentan)
    // R: ninguna
    public int procesosEnMemoria(int enEjecucion){
        int enEspera = 0;
        for (BCP b = listaEspera.getPrimero(); b != null; b = b.getSiguiente()) {
            if (!b.getEstado().startsWith("Suspendido")) enEspera++;
        }
        return listaProcesos.tamanio() + enEspera + enEjecucion;
    }

    private int slotLibre(){
        for (int i = 0; i < slotsBCP.length; i++) {
            if (!slotsBCP[i]) return i;
        }
        return -1;
    }

    // E: no aplica
    // S: BCP - el primer proceso preparado (FCFS), o null si no hay
    // R: ninguna
    public BCP siguienteProceso(){
        return listaProcesos.desencolar();
    }

    // E: bcp (BCP) - proceso que pidió entrada de teclado
    // S: no aplica (void)
    // R: ninguna
    public void enEspera(BCP bcp){
        bcp.setEstado("En espera");
        listaEspera.encolar(bcp);
    }

    // E: no aplica
    // S: BCP - el primer proceso en espera, ya movido al final de la cola de preparados
    // R: devuelve null si no hay procesos en espera
    public BCP salirDeEspera(){
        BCP bcp = listaEspera.desencolar();
        if (bcp == null) return null;
        if (bcp.getEstado().startsWith("Suspendido")) {
            // Ya ocurrió su evento, pero sigue fuera de memoria
            bcp.setEstado(BCP.SUSPENDIDO_PREPARADO);
            // No está en memoria: vuelve a la lista de trabajos para recargarse cuando haya espacio
            listaTrabajos.encolar(bcp);
        } else {
            bcp.setEstado("Preparado");
            listaProcesos.encolar(bcp);
        }
        return bcp;
    }

    // E: bcp (BCP) - proceso que terminó
    // S: no aplica (void)
    // R: libera su memoria y la posición de su BCP
    public void terminar(BCP bcp) {
        bcp.setEstado("Finalizado");
        int base = bcp.getDireccionBase();
        liberarMemoria(bcp);
        bcp.setDireccionBase(base);       // se conserva para las estadísticas
        listaFinalizados.encolar(bcp);
    }

    public ColaTrabajo getListaTrabajos() {
        return listaTrabajos; 
    }
    public ColaTrabajo getListaProcesos() {
        return listaProcesos; 
    }
    public ColaTrabajo getListaEspera() {
        return listaEspera; 
    }
    public ColaTrabajo getListaFinalizados() {
        return listaFinalizados; 
    }
}
