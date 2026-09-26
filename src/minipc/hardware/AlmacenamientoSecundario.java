package minipc.hardware;

/**
 *
 * @author Poll Anthony Garro Vargas - 2024129001
 * @Universidad: Instituto Tecnológico de Costa Rica
 * 
 */

/**
 * AlmacenamientoSecundario: representa el "disco" del Mini PC, análogo a
 * Memoria pero con tamaño configurable propio y un índice de archivos que
 * vive en los primeros registros de la unidad (nombre + dirección de cada
 * archivo), tal como pide el enunciado.
 *
 * NOTA de diseño: TAMANO_INDICE (cantidad máxima de archivos indexables) no
 * lo especifica el enunciado; se dejó en 16 como valor razonable — ajustar
 * si el profesor espera otro límite o si se vuelve configurable también.
 */
public class AlmacenamientoSecundario {
    private static final int TAMANO_INDICE = 16;

    private String[] datos;             // el disco: arreglo de posiciones de 8 bits, igual que Memoria
    private int tamanoTotal;            // configurable, default 512
    private int tamanoMemoriaVirtual;   // configurable, default 64

    private String[] nombresIndice;     // primeros TAMANO_INDICE registros: nombre del archivo (o null si vacío)
    private int[] direccionesIndice;    // primeros TAMANO_INDICE registros: dirección donde inicia ese archivo
    private int siguientePosicionLibre; // primera posición de datos disponible, después del índice

    // E: tamanoTotal (int) - tamaño total del almacenamiento secundario;
    //    tamanoMemoriaVirtual (int) - tamaño reservado como memoria virtual
    // S: no aplica (constructor)
    // R: tamanoTotal debe ser mayor que TAMANO_INDICE
    public AlmacenamientoSecundario(int tamanoTotal, int tamanoMemoriaVirtual){
        if(tamanoTotal <= TAMANO_INDICE){
            throw new RuntimeException("El almacenamiento secundario debe ser mayor que " + TAMANO_INDICE + " (espacio reservado para el índice).");
        }

        this.tamanoTotal = tamanoTotal;
        this.tamanoMemoriaVirtual = tamanoMemoriaVirtual;

        this.datos = new String[tamanoTotal];
        for(int i = 0; i < tamanoTotal; i++){
            datos[i] = "00000000";
        }

        this.nombresIndice = new String[TAMANO_INDICE];
        this.direccionesIndice = new int[TAMANO_INDICE];
        for(int i = 0; i < TAMANO_INDICE; i++){
            nombresIndice[i] = null;
            direccionesIndice[i] = -1;
        }

        // el índice ocupa las primeras TAMANO_INDICE posiciones del disco
        this.siguientePosicionLibre = TAMANO_INDICE;
    }

    // E: nombre (String) - nombre del archivo a crear; tamano (int) - cuántas posiciones necesita
    // S: int - la dirección donde quedó guardado el archivo
    // R: lanza RuntimeException si el índice está lleno o si no hay espacio en el disco
    public int crearArchivo(String nombre, int tamano){
        if(buscarArchivo(nombre) != -1){
            throw new RuntimeException("Ya existe un archivo con el nombre \"" + nombre + "\".");
        }

        int slot = buscarSlotLibreIndice();
        if(slot == -1){
            throw new RuntimeException("El índice de archivos está lleno (máximo " + TAMANO_INDICE + " archivos).");
        }

        if(siguientePosicionLibre + tamano > tamanoTotal){
            throw new RuntimeException("No hay espacio suficiente en el almacenamiento secundario para el archivo \"" + nombre + "\".");
        }

        int direccion = siguientePosicionLibre;
        nombresIndice[slot] = nombre;
        direccionesIndice[slot] = direccion;
        siguientePosicionLibre = siguientePosicionLibre + tamano;

        return direccion;
    }

    // E: nombre (String) - nombre del archivo a buscar
    // S: int - la dirección donde inicia ese archivo, o -1 si no existe
    // R: ninguna
    public int buscarArchivo(String nombre){
        for(int i = 0; i < TAMANO_INDICE; i++){
            if(nombre.equals(nombresIndice[i])){
                return direccionesIndice[i];
            }
        }
        return -1;
    }

    // E: nombre (String) - nombre del archivo a eliminar
    // S: no aplica (void)
    // R: lanza RuntimeException si el archivo no existe
    public void eliminarArchivo(String nombre){
        for(int i = 0; i < TAMANO_INDICE; i++){
            if(nombre.equals(nombresIndice[i])){
                nombresIndice[i] = null;
                direccionesIndice[i] = -1;
                return;
            }
        }
        throw new RuntimeException("No existe un archivo con el nombre \"" + nombre + "\".");
    }

    // E: posicion (int) - dirección de disco a escribir; valorBinario (String) - dato a guardar
    // S: no aplica (void)
    // R: posicion debe estar entre 0 y tamanoTotal-1, y no debe caer dentro del área del índice
    public void escribir(int posicion, String valorBinario){
        if(posicion < TAMANO_INDICE){
            throw new RuntimeException("La posición " + posicion + " pertenece al índice de archivos, no se puede escribir ahí directamente.");
        }
        datos[posicion] = valorBinario;
    }

    // E: posicion (int) - dirección de disco a leer
    // S: String - el valor binario guardado en esa posición
    // R: posicion debe estar entre 0 y tamanoTotal-1
    public String leer(int posicion){
        return datos[posicion];
    }

    private int buscarSlotLibreIndice(){
        for(int i = 0; i < TAMANO_INDICE; i++){
            if(nombresIndice[i] == null){
                return i;
            }
        }
        return -1;
    }

    public int getTamanoTotal(){
        return tamanoTotal;
    }

    public int getTamanoMemoriaVirtual(){
        return tamanoMemoriaVirtual;
    }

    public String[] getNombresIndice(){
        return nombresIndice;
    }

    public int[] getDireccionesIndice(){
        return direccionesIndice;
    }

    public static void main(String[] args){

    }
}