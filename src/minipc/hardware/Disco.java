/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minipc.hardware;

import java.util.List;
import java.util.ArrayList;

public class Disco {

    private String[] datos;
    private int tamanoTotal;
    private int tamanoIndice;
    private int tamanoMemoriaVirtual;

    public Disco(int tamanoTotal) {
        this(tamanoTotal, 64);
    }

    // E: tamanoTotal (int) - posiciones del disco; tamanoMemoriaVirtual (int) - posiciones reservadas al final
    // S: no aplica (constructor)
    // R: tamanoTotal debe ser mayor que índice + memoria virtual
    public Disco(int tamanoTotal, int tamanoMemoriaVirtual) {
        this.tamanoTotal = tamanoTotal;
        this.datos = new String[tamanoTotal];
        for(int i = 0; i < tamanoTotal; i++){
            datos[i] = "";
        }
        // Índice de archivos en los primeros registros del disco: 1 entrada por archivo
        // (nombre, dirección, tamaño). Con 512 de disco caben 32 archivos.
        this.tamanoIndice = Math.max(10, tamanoTotal / 16);
        this.tamanoMemoriaVirtual = tamanoMemoriaVirtual;
    }

    public boolean guardarArchivo(String nombre, List<String> contenido){
        if(existeArchivo(nombre)){
            return false;
        }

        int posicionIndice = -1;
        for(int i = 0; i < tamanoIndice; i++){
            if(datos[i].isEmpty()){
                posicionIndice = i;
                break;
            }
        }
        if(posicionIndice == -1){
            return false;
        }
        int limiteArchivos = tamanoTotal - tamanoMemoriaVirtual;
        int direccion = -1;

        for(int i = tamanoIndice; i <= limiteArchivos - contenido.size(); i++){
            boolean huecoLibre = true;
            for(int j = 0; j < contenido.size(); j++){
                if(!datos[i + j].isEmpty()){
                    huecoLibre = false;
                    break;
                }
            }
            if(huecoLibre){
                direccion = i;
                break;
            }
        }
        if(direccion == -1){
            return false;
        }
        datos[posicionIndice] = nombre + "," + direccion + "," + contenido.size();

        for(int i = 0; i < contenido.size(); i++){
            datos[direccion + i] = contenido.get(i);
        }
        return true;
    }  

    // E: nombre (String), contenido (List<String>) - contenido nuevo completo del archivo
    // S: boolean - true si se pudo guardar; si no cabe, el archivo queda como estaba
    // R: el archivo debe existir
    public boolean reescribirArchivo(String nombre, List<String> contenido){
        List<String> anterior = leerArchivo(nombre);
        eliminarArchivo(nombre);
        if (guardarArchivo(nombre, contenido)) return true;
        guardarArchivo(nombre, anterior);
        return false;
    }

    public List<String> leerArchivo(String nombre){
        for(int i = 0; i < tamanoIndice; i++){
            if(datos[i] != null && !datos[i].isEmpty()){
                String[] partes = datos[i].split(",");
                if(partes[0].equals(nombre)){
                    int direccion = Integer.parseInt(partes[1]);
                    int tamanio = Integer.parseInt(partes[2]);
                    List<String> contenido = new ArrayList<>();
                    for(int j = 0; j < tamanio; j++){
                        contenido.add(datos[direccion + j]);
                    }
                    return contenido;
                }
            }
        }
        return new ArrayList<>();
    }

    public boolean eliminarArchivo(String nombre){
        for(int i = 0; i < tamanoIndice; i++){
            if(datos[i] != null && !datos[i].isEmpty()){
                String[] partes = datos[i].split(",");
                if(partes[0].equals(nombre)){
                    int direccion = Integer.parseInt(partes[1]);
                    int tamanio = Integer.parseInt(partes[2]);
                    for(int j = 0; j < tamanio; j++){
                        datos[direccion + j] = "";
                    }
                    datos[i] = "";
                    return true;
                }
            }
        }
        return false;
    }

    public boolean existeArchivo(String nombre){
        for(int i = 0; i < tamanoIndice; i++){
            if(datos[i] != null && !datos[i].isEmpty()){
                String[] partes = datos[i].split(",");
                if(partes[0].equals(nombre)){
                    return true;
                }
            }
        }
        return false;
    }
    
    // ======================= Memoria virtual =======================

    // E: no aplica
    // S: int - primera posición del disco que pertenece a la memoria virtual
    // R: ninguna
    public int getInicioVirtual(){
        return tamanoTotal - tamanoMemoriaVirtual;
    }

    // E: no aplica
    // S: int - posiciones de memoria virtual ocupadas en este momento
    // R: ninguna
    public int virtualUsada(){
        int usadas = 0;
        for(int i = getInicioVirtual(); i < tamanoTotal; i++){
            if(!datos[i].isEmpty()) usadas++;
        }
        return usadas;
    }

    // E: cantidad (int) - posiciones contiguas que se necesitan en la memoria virtual
    // S: int - dirección del disco donde empieza el bloque libre, o -1 si no hay espacio
    // R: ninguna
    public int buscarEspacioVirtual(int cantidad){
        if(cantidad <= 0) return -1;
        int contador = 0;
        for(int i = getInicioVirtual(); i < tamanoTotal; i++){
            if(datos[i].isEmpty()){
                contador++;
                if(contador == cantidad) return i - cantidad + 1;
            } else {
                contador = 0;
            }
        }
        return -1;
    }

    // E: direccion (int) - inicio del bloque en memoria virtual; lineas (List<String>) - contenido
    // S: no aplica (void)
    // R: el bloque debe haberse obtenido con buscarEspacioVirtual
    public void escribirVirtual(int direccion, List<String> lineas){
        for(int i = 0; i < lineas.size(); i++){
            datos[direccion + i] = lineas.get(i);
        }
    }

    // E: direccion (int), cantidad (int) - bloque de memoria virtual a liberar
    // S: no aplica (void)
    // R: ninguna
    public void liberarVirtual(int direccion, int cantidad){
        for(int i = direccion; i < direccion + cantidad && i < tamanoTotal; i++){
            if(i >= getInicioVirtual()) datos[i] = "";
        }
    }

    public String leer(int posicion){
        return datos[posicion];
    }

    public int getTamanoIndice(){
        return tamanoIndice;
    }

    public String[] getTodasLasPosiciones(){
        return datos;
    }

    public int getTamanoTotal(){
        return tamanoTotal;
    }

    public int getTamanoMemoriaVirtual(){
        return tamanoMemoriaVirtual;
    }
    
    public static void main(String[] args) {
    Disco disco = new Disco(512);
    
    // Guardar archivo
    List<String> contenido = new ArrayList<>();
    contenido.add("MOV AX, 5");
    contenido.add("MOV BX, 99");
    contenido.add("INT 20H");
    
    boolean ok = disco.guardarArchivo("programa1.asm", contenido);
    System.out.println("Guardado: " + ok);
    System.out.println("Existe: " + disco.existeArchivo("programa1.asm"));
    
    // Leer archivo
    List<String> leido = disco.leerArchivo("programa1.asm");
    System.out.println("Leído: " + leido);
    
    // Eliminar
    boolean borrado = disco.eliminarArchivo("programa1.asm");
    System.out.println("Borrado: " + borrado);
    System.out.println("Existe después: " + disco.existeArchivo("programa1.asm"));
}
}