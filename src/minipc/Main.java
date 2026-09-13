package minipc;

/**
 *
 * @author Poll Anthony Garro Vargas - 2024129001
 * @Universidad: Instituto Tecnológico de Costa Rica
 * 
 */
import minipc.gui.VentanaPrincipal;



/**
 * Clase principal del programa Mini PC.
 * Su unico proposito es crear e iniciar la ventana principal (VentanaPrincipal),
 * que contiene toda la logica de interfaz grafica del simulador.
 */
public class Main {
    
    public static void main(String[] args){
        VentanaPrincipal ventana = new VentanaPrincipal();
        ventana.setVisible(true);
    }
}