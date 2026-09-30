/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minipc.modelo;
import minipc.hardware.Memoria;
public class Planificador {

    // Colas
    private ColaTrabajo listaTrabajos;
    private ColaTrabajo listaProcesos;
    private ColaTrabajo listaFinalizados;
    private Memoria memoria;
    private ColaTrabajo listaEspera;

    // Límite
    private static final int MAX_PROCESOS_ACTIVOS = 5;

    // Constructor
    public Planificador(Memoria memoria) {
        this.listaTrabajos = new ColaTrabajo();
        this.listaProcesos = new ColaTrabajo();
        this.listaFinalizados = new ColaTrabajo();
        this.listaEspera = new ColaTrabajo();
        this.memoria = memoria;
    }
    
    public void admitirBCP(BCP bcp){
        bcp.setEstado("Nuevo");
        listaTrabajos.encolar(bcp);
    }
    
    public void prepararSiguientes() {
        while (listaProcesos.tamanio() < MAX_PROCESOS_ACTIVOS && !listaTrabajos.estaVacia()) {
            BCP bcp = listaTrabajos.getPrimero();
            int hueco = memoria.encontrarHueco(bcp.getTamanioProceso());

            if (hueco == -1) {
                break;  
            }

            // asignar dirección base
            bcp.setDireccionBase(hueco);

            // marcar posiciones en memoria como ocupadas
            for (int x = hueco; x < hueco + bcp.getTamanioProceso(); x++) {
                memoria.escribir(x, "00000000");
            }
            
            // cambiar estado
            bcp.setEstado("Preparado");

            // mover de listaTrabajos a listaProcesos
            listaTrabajos.desencolar();
            listaProcesos.encolar(bcp);
        }
    }
    
    public BCP siguienteProceso(){
        return listaProcesos.desencolar();
    }
    
    public void enEspera(BCP bcp){
        bcp.setEstado("En Espera");
        listaEspera.encolar(bcp);
    }
     
    public void terminar(BCP bcp) {
        bcp.setEstado("Finalizado");
        memoria.liberar(bcp.getDireccionBase(), bcp.getTamanioProceso());
        listaFinalizados.encolar(bcp);
    }
        
 
    
}
