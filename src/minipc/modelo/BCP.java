/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minipc.modelo;
        
/**
 *
 * @author Poll Anthony Garro Vargas - 2024129001
 * @Universidad: Instituto Tecnológico de Costa Rica
 * 
 */

import minipc.hardware.CPU;
import minipc.hardware.Memoria;

/**
 * BCP (Bloque de Control de Proceso): representa el estado de un proceso en
 * un momento dado - su identificador, estado, y una copia de los registros
 * de la CPU capturados al momento de invocar capturaEstado().
 */
public class BCP {
    private int idProceso;
    private String estado;
    private int pcGuardado;
    private int acGuardado;
    private int axGuardado;
    private int bxGuardado;
    private int cxGuardado;
    private int dxGuardado;
    
        // E: idProceso (int) - identificador del proceso; estado (String) - ej. "Nuevo"
        // S: no aplica (constructor)
        // R: ninguna
        public BCP(int idProceso, String estado){
            this.idProceso = idProceso;
            this.estado = estado;
            this.pcGuardado = 0;
            this.acGuardado = 0;
            this.axGuardado = 0;
            this.bxGuardado = 0;
            this.cxGuardado = 0;
            this.dxGuardado = 0;

        }
        
        // E: cpu (CPU) - la CPU de la que se va a copiar el estado actual
        // S: no aplica (void)
        // R: ninguna
        public void capturaEstado(CPU cpu){
            this.pcGuardado = cpu.getPC();
            this.acGuardado = cpu.getAC();
            this.axGuardado = cpu.getAX();
            this.bxGuardado = cpu.getBX();
            this.cxGuardado = cpu.getCX();
            this.dxGuardado = cpu.getDX();
        }
        
        public void setEstado(String nuevoEstado){
            this.estado = nuevoEstado;
        }
        
        public int getIdProceso(){
            return idProceso;
        }

        public String getEstado(){
            return estado;
        }

        public int getPcGuardado(){
            return pcGuardado;
        }

        public int getAcGuardado(){
            return acGuardado;
        }

        public int getAxGuardado(){
            return axGuardado;
        }

        public int getBxGuardado(){
            return bxGuardado;
        }

        public int getCxGuardado(){
            return cxGuardado;
        }

        public int getDxGuardado(){
            return dxGuardado;
        }
        
        public static void main(String[] args){
    
    }
}