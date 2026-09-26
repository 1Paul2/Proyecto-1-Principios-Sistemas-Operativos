package minipc.util;

/**
 *
 * @author Poll Anthony Garro Vargas - 2024129001
 * @Universidad: Instituto Tecnológico de Costa Rica
 * 
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * ConfiguracionSistema: guarda y carga los tamaños configurables de la
 * minicomputadora (memoria principal, memoria de sistema, almacenamiento
 * secundario, memoria virtual) desde un archivo de texto tipo
 * "clave=valor" (java.util.Properties), tal como pide el enunciado: la
 * configuración no debe quedar hardcodeada en el código.
 *
 * NOTA: el enunciado no da un valor por defecto para "memoriaSistema"
 * (el espacio reservado para el S.O. dentro de la memoria principal) — se
 * dejó en 32 como valor razonable; ajustar según lo que pida el profesor.
 */
public class ConfiguracionSistema {

    private static final int DEFAULT_MEMORIA_PRINCIPAL = 256;
    private static final int DEFAULT_MEMORIA_SISTEMA = 32;
    private static final int DEFAULT_ALMACENAMIENTO_SECUNDARIO = 512;
    private static final int DEFAULT_MEMORIA_VIRTUAL = 64;

    private int memoriaPrincipal;
    private int memoriaSistema;
    private int almacenamientoSecundario;
    private int memoriaVirtual;

    // E: no aplica
    // S: no aplica (constructor)
    // R: inicializa con los valores por defecto del enunciado
    public ConfiguracionSistema(){
        this.memoriaPrincipal = DEFAULT_MEMORIA_PRINCIPAL;
        this.memoriaSistema = DEFAULT_MEMORIA_SISTEMA;
        this.almacenamientoSecundario = DEFAULT_ALMACENAMIENTO_SECUNDARIO;
        this.memoriaVirtual = DEFAULT_MEMORIA_VIRTUAL;
    }

    // E: archivo (File) - archivo de configuración en formato clave=valor
    // S: no aplica (void)
    // R: lanza IOException si el archivo no se puede leer; si una clave no
    //    está presente, se conserva el valor por defecto para esa clave
    public void cargarDesdeArchivo(File archivo) throws IOException {
        Properties propiedades = new Properties();
        try (FileInputStream entrada = new FileInputStream(archivo)) {
            propiedades.load(entrada);
        }

        memoriaPrincipal = Integer.parseInt(propiedades.getProperty("memoriaPrincipal", String.valueOf(memoriaPrincipal)));
        memoriaSistema = Integer.parseInt(propiedades.getProperty("memoriaSistema", String.valueOf(memoriaSistema)));
        almacenamientoSecundario = Integer.parseInt(propiedades.getProperty("almacenamientoSecundario", String.valueOf(almacenamientoSecundario)));
        memoriaVirtual = Integer.parseInt(propiedades.getProperty("memoriaVirtual", String.valueOf(memoriaVirtual)));
    }

    // E: archivo (File) - dónde guardar la configuración actual
    // S: no aplica (void)
    // R: lanza IOException si el archivo no se puede escribir
    public void guardarEnArchivo(File archivo) throws IOException {
        Properties propiedades = new Properties();
        propiedades.setProperty("memoriaPrincipal", String.valueOf(memoriaPrincipal));
        propiedades.setProperty("memoriaSistema", String.valueOf(memoriaSistema));
        propiedades.setProperty("almacenamientoSecundario", String.valueOf(almacenamientoSecundario));
        propiedades.setProperty("memoriaVirtual", String.valueOf(memoriaVirtual));

        try (FileOutputStream salida = new FileOutputStream(archivo)) {
            propiedades.store(salida, "Configuracion Mini PC - Proyecto 1 Gestor de Procesos");
        }
    }

    public int getMemoriaPrincipal(){ return memoriaPrincipal; }
    public int getMemoriaSistema(){ return memoriaSistema; }
    public int getAlmacenamientoSecundario(){ return almacenamientoSecundario; }
    public int getMemoriaVirtual(){ return memoriaVirtual; }

    public void setMemoriaPrincipal(int valor){ this.memoriaPrincipal = valor; }
    public void setMemoriaSistema(int valor){ this.memoriaSistema = valor; }
    public void setAlmacenamientoSecundario(int valor){ this.almacenamientoSecundario = valor; }
    public void setMemoriaVirtual(int valor){ this.memoriaVirtual = valor; }

    public static void main(String[] args){

    }
}