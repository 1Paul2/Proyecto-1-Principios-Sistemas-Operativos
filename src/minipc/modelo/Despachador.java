package minipc.modelo;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author PC
 */
import minipc.hardware.CPU;

public class Despachador {

    private CPU cpu;

    public Despachador(CPU cpu) {
        this.cpu = cpu;
    }

    public void despachar(BCP bcp) {
        cpu.setBcpActual(bcp);
        bcp.restaurarEstado(cpu);
        cpu.reiniciarInstruccion(); 
        bcp.setEstado("Ejecución");
    }
    
    public void capturar(BCP bcp) {
        bcp.capturaEstado(cpu);
    }
}