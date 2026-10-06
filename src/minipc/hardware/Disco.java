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
        this.tamanoTotal = tamanoTotal;
        this.datos = new String[tamanoTotal];
        for(int i = 0; i < tamanoTotal; i++){
            datos[i] = "";
        }
        this.tamanoIndice = 10;         
        this.tamanoMemoriaVirtual = 64; 
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

