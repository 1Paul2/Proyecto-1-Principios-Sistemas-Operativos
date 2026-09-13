/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minipc.hardware;

/**
 *
 * @author elenanito
 */
public class Memoria {
        private String[] datos;
        private int inicioUsuario;
        private int tamanoTotal;
        
        public Memoria(int tamanoTotal, int tamanoSistema){
                this.tamanoTotal = tamanoTotal;
                this.inicioUsuario = tamanoSistema;
                this.datos = new String[tamanoTotal];
                for(int x = 0; x < datos.length; x++){
                    datos[x] = "00000000";
            }
                
        }
        public void escribir(int posicion, String valorBinario){
            datos[posicion] = valorBinario;
        }

        public String leer(int posicion){
            return datos[posicion];
        }

        public boolean estaEnZonaUsuario(int posicion){
            return posicion >= inicioUsuario && posicion < tamanoTotal;
        }

        public int getInicioUsuario(){
            return inicioUsuario;
        }
        
        public String[] getTodasLasPosiciones(){
            return datos;
        }
        
        public int getTamanoTotal(){
            return tamanoTotal;
        }
        
        public static void main(String[] args){

        }
}
