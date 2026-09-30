/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minipc.modelo;

public class Planificador {

    // Colas
    private ColaTrabajo listaTrabajos;
    private ColaTrabajo listaProcesos;
    private ColaTrabajo listaFinalizados;

    // Límite
    private static final int MAX_PROCESOS_ACTIVOS = 5;

    // Constructor
    public Planificador() {
        this.listaTrabajos = new ColaTrabajo();
        this.listaProcesos = new ColaTrabajo();
        this.listaFinalizados = new ColaTrabajo();
    }
}
