/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minipc.hardware;

/**
 *
 * @author Poll Anthony Garro Vargas - 2024129001
 * @Universidad: Instituto Tecnológico de Costa Rica
 * 
 */


/**
 * Memoria: representa la memoria del Mini PC como un arreglo de posiciones,
 * cada una con un codigo binario de 8 bits. Se divide en zona de Sistema
 * Operativo (desde 0 hasta inicioUsuario-1) y zona de Usuario (desde
 * inicioUsuario hasta tamanoTotal-1).
 */
public class Memoria {
        private String[] datos;
        private int inicioUsuario;
        private int tamanoTotal;
        
        // E: tamanoTotal (int) - cantidad total de posiciones de memoria; tamanoSistema (int) - cantidad de posiciones reservadas para el S.O.
        // S: no aplica (constructor)
        // R: tamanoSistema debe ser menor que tamanoTotal
        public Memoria(int tamanoTotal, int tamanoSistema){
                this.tamanoTotal = tamanoTotal;
                this.inicioUsuario = tamanoSistema;
                this.datos = new String[tamanoTotal];
                for(int x = 0; x < datos.length; x++){
                    datos[x] = "00000000";
            }
                
        }
        
        // E: posicion (int) - indice de memoria a escribir; valorBinario (String) - dato a guardar
        // S: no aplica (void)
        // R: posicion debe estar entre 0 y tamanoTotal-1
        public void escribir(int posicion, String valorBinario){
            datos[posicion] = valorBinario;
        }

        // E: posicion (int) - indice de memoria a leer
        // S: String - el valor binario guardado en esa posicion
        // R: posicion debe estar entre 0 y tamanoTotal-1
        public String leer(int posicion){
            return datos[posicion];
        }

        // E: posicion (int) - indice de memoria a validar
        // S: boolean - true si la posicion pertenece a la zona de usuario
        // R: ninguna
        public boolean estaEnZonaUsuario(int posicion){
            return posicion >= inicioUsuario && posicion < tamanoTotal;
        }

        // NUEVO: protección de memoria — evita que un proceso lea o escriba
        // fuera del rango que le corresponde según su base (dirección de
        // inicio) y su alcance (tamaño del proceso), ambos guardados en el BCP.
        // E: posicion (int) - dirección a la que se quiere acceder;
        //    base (int) - dirección de inicio del proceso (desde su BCP);
        //    alcance (int) - tamaño del proceso (desde su BCP)
        // S: boolean - true si el acceso está dentro del rango permitido
        // R: ninguna
        public boolean accesoPermitido(int posicion, int base, int alcance){
            return posicion >= base && posicion < (base + alcance);
        }

        // E: posicion (int), base (int), alcance (int) - igual que accesoPermitido
        // S: no aplica (void)
        // R: lanza RuntimeException si el acceso está fuera del rango permitido
        public void validarAcceso(int posicion, int base, int alcance){
            if(!accesoPermitido(posicion, base, alcance)){
                throw new RuntimeException("Violación de acceso a memoria: el proceso (base=" + base + ", alcance=" + alcance + ") intentó acceder a la posición " + posicion + ", fuera de su espacio asignado.");
            }
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