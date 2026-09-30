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
import java.util.List;
import java.util.ArrayList;

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
    private String irGuardado;
    private int[] pila;
    private int topePila;
    private int tiempoInicio;
    private int tiempoEmpleado;
    private int cpuId;
    private List<String> archivosAbiertos;
    private BCP siguiente;
    private int direccionBase;
    private int tamanioProceso;
    private int prioridad;
            
        // E: idProceso (int) - identificador del proceso; estado (String) 
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
            this.irGuardado = "";
            this.pila = new int[5]; 
            this.topePila = -1;
            this.tiempoInicio = 0;
            this.tiempoEmpleado = 0;
            this.cpuId = -1;
            this.archivosAbiertos = new ArrayList<>();
            this.siguiente = null;
            this.direccionBase = -1;
            this.tamanioProceso = 0;
            this.prioridad = 0;  
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
            this.irGuardado = cpu.getIR();
            this.irGuardado = cpu.getIR();   
        }
        
        public void restaurarEstado(CPU cpu){
            cpu.setPC(this.pcGuardado);
            cpu.setAC(this.acGuardado);
            cpu.setAX(this.axGuardado);
            cpu.setBX(this.bxGuardado);
            cpu.setCX(this.cxGuardado);
            cpu.setDX(this.dxGuardado);
            cpu.setIR(this.irGuardado);
        }
        
        public void setEstado(String nuevoEstado){
            this.estado = nuevoEstado;
        }
        
        public void push(int valor) {
            if (topePila < 4) {
                topePila++;
                pila[topePila] = valor;
            } else {
                throw new RuntimeException("Desbordamiento de pila en el proceso " + idProceso);
            }
        }
        public int pop(){
            if(topePila >= 0){
                int valor = pila[topePila];
                topePila--;
                return valor;
            }else{ 
                throw new RuntimeException("Pila vacía en el proceso " + idProceso);
            }
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
        public String getIrGuardado(){
            return irGuardado;
        }

        public int getTiempoInicio(){
            return tiempoInicio;
        }

        public int getTiempoEmpleado(){
            return tiempoEmpleado;
        }

        public int getCpuId(){
            return cpuId;
        }

        public List<String> getArchivosAbiertos(){
            return archivosAbiertos;
        }

        public BCP getSiguiente(){
            return siguiente;
        }

        public int getDireccionBase(){
            return direccionBase;
        }

        public int getTamanioProceso(){
            return tamanioProceso;
        }

        public int getPrioridad(){
            return prioridad;
        }

        public void setPcGuardado(int pc){
            this.pcGuardado = pc;
        }

        public void setAcGuardado(int ac){
            this.acGuardado = ac;
        }

        public void setAxGuardado(int ax){
            this.axGuardado = ax;
        }

        public void setBxGuardado(int bx){
            this.bxGuardado = bx;
        }

        public void setCxGuardado(int cx){
            this.cxGuardado = cx;
        }

        public void setDxGuardado(int dx){
            this.dxGuardado = dx;
        }

        public void setIrGuardado(String ir){
            this.irGuardado = ir;
        }

        public void setTiempoInicio(int t){
            this.tiempoInicio = t;
        }

        public void setTiempoEmpleado(int t){
            this.tiempoEmpleado = t;
        }

        public void setCpuId(int id){
            this.cpuId = id;
        }

        public void setSiguiente(BCP sig){
            this.siguiente = sig;
        }

        public void setDireccionBase(int dir){
            this.direccionBase = dir;
        }

        public void setTamanioProceso(int tam){
            this.tamanioProceso = tam;
        }

        public void setPrioridad(int p){
            this.prioridad = p;
        }

        public void agregarArchivoAbierto(String archivo){
            this.archivosAbiertos.add(archivo);
        }
        
}