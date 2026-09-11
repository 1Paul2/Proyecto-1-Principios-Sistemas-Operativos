/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minipc.modelo;
import minipc.hardware.CPU;
import minipc.hardware.Memoria;
/**
 *
 * @author elenanito
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
            Memoria memoria = new Memoria(128, 64);
            CPU cpu = new CPU(memoria);
            cpu.setPC(70); 

            BCP bcp = new BCP(1, "Ejecutando");
            bcp.capturaEstado(cpu);

            System.out.println(bcp.getIdProceso());   
            System.out.println(bcp.getEstado());          
            System.out.println(bcp.getPcGuardado());     
    }
}
