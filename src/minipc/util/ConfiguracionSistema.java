/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package minipc.util;

/**
 *
 * @author Poll Anthony Garro Vargas - 2024129001
 * @Universidad: Instituto Tecnológico de Costa Rica
 *
 */

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * ConfiguracionSistema: tamaños de memoria principal, zona del S.O., disco y
 * memoria virtual. Se leen de un archivo de texto (config.txt) para que no
 * queden fijos en el código. Si el archivo no existe, se crea con los valores
 * por defecto del enunciado (256 / 512 / 64, S.O. = 25%).
 *
 * Solo se configuran la memoria principal y el disco. Los otros dos tamaños se
 * calculan siempre como porcentaje:
 *   - Zona del S.O.     = 25 %   de la memoria principal 
 *   - Memoria virtual   = 12,5 % del disco                
 *
 * Formato de config.txt:
 *   memoriaPrincipal=256
 *   memoriaSistema=64           
 *   almacenamientoSecundario=512
 *   memoriaVirtual=64            
 */
public class ConfiguracionSistema {

    public static final String ARCHIVO_POR_DEFECTO = "config.txt";

    // Valores predeterminados del enunciado
    public static final int MEMORIA_PRINCIPAL_DEF = 256;
    public static final int MEMORIA_SISTEMA_DEF = 64;          // 25% de la memoria principal
    public static final int ALMACENAMIENTO_DEF = 512;
    public static final int MEMORIA_VIRTUAL_DEF = 64;

    // Porcentajes con los que se calculan la zona del S.O. y la memoria virtual
    public static final double PORCENTAJE_SO = 0.25;         // 25 % de la memoria principal
    public static final double PORCENTAJE_VIRTUAL = 0.125;   // 12,5 % del disco

    // E: memoriaPrincipal (int)
    // S: int - tamaño de la zona del S.O. (25 % de la memoria principal)
    // R: ninguna
    public static int calcularZonaSO(int memoriaPrincipal){
        return (int) Math.round(memoriaPrincipal * PORCENTAJE_SO);
    }

    // E: almacenamiento (int) - tamaño del disco
    // S: int - tamaño de la memoria virtual (12,5 % del disco)
    // R: ninguna
    public static int calcularMemoriaVirtual(int almacenamiento){
        return (int) Math.round(almacenamiento * PORCENTAJE_VIRTUAL);
    }

    private int memoriaPrincipal = MEMORIA_PRINCIPAL_DEF;
    private int memoriaSistema = MEMORIA_SISTEMA_DEF;
    private int almacenamientoSecundario = ALMACENAMIENTO_DEF;
    private int memoriaVirtual = MEMORIA_VIRTUAL_DEF;

    // E: no aplica
    // S: no aplica (constructor)
    // R: carga config.txt de la carpeta del proyecto; si no existe, lo crea con los valores por defecto
    public ConfiguracionSistema(){
        File archivo = new File(ARCHIVO_POR_DEFECTO);
        try {
            if (archivo.exists()) {
                cargarDesdeArchivo(archivo);
            } else {
                guardarEnArchivo(archivo);
            }
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("No se pudo usar " + ARCHIVO_POR_DEFECTO + ": " + e.getMessage()
                + ". Se usan los valores por defecto.");
        }
    }

    // E: archivo (File) - archivo de configuración clave=valor
    // S: no aplica (void)
    // R: lanza IllegalArgumentException si un valor no es válido
    public void cargarDesdeArchivo(File archivo) throws IOException {
        Properties p = new Properties();
        try (FileReader lector = new FileReader(archivo)) {
            p.load(lector);
        }
        int mp = leerEntero(p, "memoriaPrincipal", memoriaPrincipal);
        int as = leerEntero(p, "almacenamientoSecundario", almacenamientoSecundario);
        setTamanos(mp, as);   // la zona del S.O. y la memoria virtual se calculan por porcentaje
    }

    // E: archivo (File) - destino
    // S: no aplica (void)
    // R: ninguna
    public void guardarEnArchivo(File archivo) throws IOException {
        Properties p = new Properties();
        p.setProperty("memoriaPrincipal", String.valueOf(memoriaPrincipal));
        p.setProperty("memoriaSistema", String.valueOf(memoriaSistema));
        p.setProperty("almacenamientoSecundario", String.valueOf(almacenamientoSecundario));
        p.setProperty("memoriaVirtual", String.valueOf(memoriaVirtual));
        try (FileWriter escritor = new FileWriter(archivo)) {
            p.store(escritor, "Configuracion del Mini PC");
        }
    }

    // E: los cuatro tamaños
    // S: no aplica (void)
    // R: lanza IllegalArgumentException con un mensaje claro si algún valor no es coherente
    public static void validar(int mp, int ms, int as, int mv){
        if (mp < 128 || mp > 4096) 
            throw new IllegalArgumentException("La memoria principal debe estar entre 128 y 4096");
        if (ms < 5 || ms >= mp) 
            throw new IllegalArgumentException("La zona del S.O. debe ser al menos 5 (para los BCP) y menor que la memoria principal");
        if (mv < 0) 
            throw new IllegalArgumentException("La memoria virtual no puede ser negativa");
        if (as < 128 || as > 8192) 
            throw new IllegalArgumentException("El disco debe estar entre 128 y 8192");
    }

    private int leerEntero(Properties p, String clave, int porDefecto){
        String v = p.getProperty(clave);
        if (v == null) return porDefecto;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El valor de \"" + clave + "\" no es un número: " + v);
        }
    }

    public int getMemoriaPrincipal(){
        return memoriaPrincipal; 
    }
    public int getMemoriaSistema(){
        return memoriaSistema;
    }
    public int getAlmacenamientoSecundario(){
        return almacenamientoSecundario; 
    }
    public int getMemoriaVirtual(){
        return memoriaVirtual;
    }

    // E: los cuatro tamaños
    // S: no aplica (void)
    // R: valida antes de asignar
    // E: no aplica
    // S: no aplica (void)
    // R: vuelve a los valores predeterminados y los guarda en config.txt
    public void restaurarPredeterminados(){
        memoriaPrincipal = MEMORIA_PRINCIPAL_DEF;
        memoriaSistema = MEMORIA_SISTEMA_DEF;
        almacenamientoSecundario = ALMACENAMIENTO_DEF;
        memoriaVirtual = MEMORIA_VIRTUAL_DEF;
        try {
            guardarEnArchivo(new File(ARCHIVO_POR_DEFECTO));
        } catch (IOException e) {
            System.err.println("No se pudo restaurar " + ARCHIVO_POR_DEFECTO + ": " + e.getMessage());
        }
    }

    // E: mp (int) - memoria principal; as (int) - disco
    // S: no aplica (void)
    // R: calcula la zona del S.O. (25 %) y la memoria virtual (12,5 %) y valida todo antes de asignar
    public void setTamanos(int mp, int as){
        int ms = calcularZonaSO(mp);
        int mv = calcularMemoriaVirtual(as);
        validar(mp, ms, as, mv);
        memoriaPrincipal = mp;
        memoriaSistema = ms;
        almacenamientoSecundario = as;
        memoriaVirtual = mv;
    }
}
