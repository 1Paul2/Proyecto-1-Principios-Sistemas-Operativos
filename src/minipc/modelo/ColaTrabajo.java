/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minipc.modelo;

/**
 *
 * @author PC
 */
public class ColaTrabajo {
    //Atributos de ColaTrabajo
    private BCP primero;
    private BCP ultimo;
    
    //Constructor de ColaTrabajo
    public ColaTrabajo(){
        this.primero = null;
        this.ultimo = null;
    }
     // ======== Métodos ==========
    
    
    // Lo que pasa aqui es esto
    //primero -> BCP1 -> BCP2 -> BCP3
    //ultimo  -> BCP3

    public void encolar(BCP bcp) {
        bcp.setSiguiente(null);
        if(primero == null){
            primero = bcp;
            ultimo = bcp;
        }else {
            ultimo.setSiguiente(bcp);
            ultimo = bcp;
        }
    }

    public BCP desencolar() {
        if (estaVacia()) {
            return null;
        }
        BCP temp = primero;                     
        primero = primero.getSiguiente();       
        if (primero == null) {
            ultimo = null;                       
        }
        temp.setSiguiente(null);                 
        return temp;                            
    }
    
    public boolean estaVacia() {
        return primero == null;
    }

    public BCP getPrimero() {
        return primero;
    }

    public BCP getUltimo() {
        return ultimo;
    }

    public int tamanio() {
        int contador = 0;
        BCP actual = primero;
        while (actual != null) {
            contador++;
            actual = actual.getSiguiente();
        }
        return contador;
    }
}
