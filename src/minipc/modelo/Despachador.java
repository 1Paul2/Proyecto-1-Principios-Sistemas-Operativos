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
        // 1. restaurar estado del BCP en la CPU
        // 2. cambiar estado a "Ejecución"
    }

    public void capturar(BCP bcp) {
        // 1. capturar estado de la CPU en el BCP
        // 2. no cambiar de estado (lo decide quien llama)
    }
}