package minipc.hardware;

import minipc.modelo.BCP;
import java.util.ArrayList;
import java.util.List;

public class CPU {
    private int PC;
    private String IR;
    private int AC;
    private int AX;
    private int BX;
    private int CX;
    private int DX;
    private int AH;               // función de INT 21H (3CH, 3DH, 4DH, 40H, 41H)
    private int AL;               // dato que se lee o escribe con INT 21H
    private String DXTexto;       // nombre de archivo cargado en DX con MOV DX, "archivo.txt"
    private Memoria memoria;
    private Disco disco;          // para leer la parte del programa que está en memoria virtual
    private BCP bcpActual;
    private boolean flagIgual;
    private int segundosRestantes = 0;
    private String[] instrActual;
    private List<String> pantalla = new ArrayList<>();
    private boolean solicitudTeclado = false;

    public CPU(Memoria memoria){
        this.memoria = memoria;
        this.PC = 0;
        this.AC = 0;
        this.AX = 0;
        this.BX = 0;
        this.CX = 0;
        this.DX = 0;
        this.AH = 0;
        this.AL = 0;
        this.DXTexto = null;
        this.IR = "";
        this.bcpActual = null;
        this.flagIgual = false;
    }

    // ===== Getters y Setters =====

    public int getPC(){ return PC; }
    public int getAC(){ return AC; }
    public int getAX(){ return AX; }
    public int getBX(){ return BX; }
    public int getCX(){ return CX; }
    public int getDX(){ return DX; }
    public String getIR(){ return IR; }
    public void setAC(int ac){ this.AC = ac; }
    public void setAX(int ax){ this.AX = ax; }
    public void setBX(int bx){ this.BX = bx; }
    public void setCX(int cx){ this.CX = cx; }
    public void setDX(int dx){ this.DX = dx; }
    public int getAH(){ return AH; }
    public int getAL(){ return AL; }
    public void setAH(int ah){ this.AH = ah; }
    public void setAL(int al){ this.AL = al; }
    public String getDXTexto(){ return DXTexto; }
    public void setDXTexto(String t){ this.DXTexto = t; }
    public void setIR(String ir){ this.IR = ir; }
    public void setPC(int nuevoPC){ this.PC = nuevoPC; }

    public BCP getBcpActual(){ return bcpActual; }
    public void setBcpActual(BCP bcp){ this.bcpActual = bcp; }

    public boolean isFlagIgual(){ return flagIgual; }
    public void setFlagIgual(boolean flag){ this.flagIgual = flag; }

    // ===== Pantalla y teclado =====

    // E: texto (String) - línea a mostrar en la pantalla del Mini PC
    // S: no aplica (void)
    // R: ninguna
    public void imprimir(String texto){ pantalla.add(texto); }
    public List<String> getPantalla(){ return pantalla; }
    public void limpiarPantalla(){ pantalla.clear(); }

    // E: no aplica
    // S: boolean - true si la última instrucción fue INT 09H (pide teclado); se reinicia al leerlo
    // R: ninguna
    public boolean consumirSolicitudTeclado(){
        boolean s = solicitudTeclado;
        solicitudTeclado = false;
        return s;
    }

    // Para mostrar en la GUI cuántos segundos le faltan a la instrucción actual
    public int getSegundosRestantes(){ return segundosRestantes; }

    // E: no aplica
    // S: int - primera posición fuera del programa actual (base + alcance)
    // R: devuelve 0 si no hay proceso en la CPU
    public int getLimite(){
        if(bcpActual == null) return 0;
        return bcpActual.getDireccionBase() + bcpActual.getTamanioProceso();
    }

    // ===== Ciclo de instrucción =====

    // Separa la instrucción en operación y operandos: "MOV BX 5" -> ["MOV", "BX", "5"]
    public String[] decode(String dato){
        return dato.trim().split("[,\\s]+");
    }

    public void setDisco(Disco disco){ this.disco = disco; }

    // Lee la instrucción apuntada por el PC, validando que esté dentro del proceso.
    // El PC es una dirección lógica: base + desplazamiento. Si el desplazamiento cae
    // en la parte del programa que no cupo en RAM, la instrucción se lee de la
    // memoria virtual del disco.
    public void fetch(){
        int base = bcpActual.getDireccionBase();
        memoria.validarAcceso(PC, base, bcpActual.getTamanioProceso());
        int desplazamiento = PC - base;
        if (!bcpActual.usaMemoriaVirtual() || desplazamiento < bcpActual.getEnRAM()) {
            IR = memoria.leer(PC);
        } else {
            int dirVirtual = bcpActual.getBaseVirtual() + (desplazamiento - bcpActual.getEnRAM());
            IR = disco.leer(dirVirtual);
            if (!bcpActual.isAvisoVirtual()) {
                imprimir("[SO] P" + bcpActual.getIdProceso() + " ejecuta desde memoria virtual (disco ["
                    + dirVirtual + "])");
                bcpActual.setAvisoVirtual(true);
            }
        }
        PC = PC + 1;
    }

    // E: no aplica
    // S: int - posición del disco de la instrucción apuntada por el PC si está en memoria virtual, o -1
    // R: ninguna
    public int posicionVirtualPC(){
        if (bcpActual == null || !bcpActual.usaMemoriaVirtual()) return -1;
        int desplazamiento = PC - bcpActual.getDireccionBase();
        if (desplazamiento < bcpActual.getEnRAM() || desplazamiento >= bcpActual.getTamanioProceso()) return -1;
        return bcpActual.getBaseVirtual() + (desplazamiento - bcpActual.getEnRAM());
    }

    // E: no aplica
    // S: no aplica (void) - avanza 1 segundo de CPU; la instrucción se ejecuta
    //    cuando se cumplen los segundos de su peso
    // R: debe haber un proceso en la CPU
    public void tick(){
        if(bcpActual == null) return;
        if(segundosRestantes == 0){
            fetch();
            instrActual = decode(IR);
            segundosRestantes = Pesos.de(instrActual[0], arg(instrActual, 1));
        }
        segundosRestantes--;
        if(segundosRestantes == 0){
            execute(instrActual);
            instrActual = null;
        }
    }

    // Ejecuta una instrucción completa, sin importar su peso (útil para pruebas)
    public void pasoAPaso(){
        do {
            tick();
        } while(segundosRestantes > 0);
    }

    public void ejecutarTodo(){
        while(!programaTerminado()){
            pasoAPaso();
        }
    }

    public boolean programaTerminado(){
        return bcpActual == null || (segundosRestantes == 0 && PC >= getLimite());
    }

    // Llamalo desde Despachador.despachar() al cargar un proceso nuevo
    public void reiniciarInstruccion(){
        segundosRestantes = 0;
        instrActual = null;
        solicitudTeclado = false;
    }

    // ===== Execute =====

    // E: p (String[]) - instrucción decodificada: operación y operandos
    // S: no aplica (void)
    // R: lanza RuntimeException si la instrucción es inválida
    public void execute(String[] p){
        if(p == null || p.length == 0) return;

        switch (p[0]) {
            case "LOAD":  ejecutarLOAD(arg(p, 1)); break;
            case "STORE": ejecutarSTORE(arg(p, 1)); break;
            case "MOV":   ejecutarMOV(arg(p, 1), arg(p, 2)); break;
            case "SUB":   ejecutarSUB(arg(p, 1)); break;
            case "ADD":   ejecutarADD(arg(p, 1)); break;
            case "INC":   ejecutarINC(arg(p, 1)); break;
            case "DEC":   ejecutarDEC(arg(p, 1)); break;
            case "PUSH":  ejecutarPUSH(arg(p, 1)); break;
            case "POP":   ejecutarPOP(arg(p, 1)); break;
            case "JMP":   saltar(arg(p, 1)); break;
            case "CMP":   ejecutarCMP(arg(p, 1), arg(p, 2)); break;
            case "JE":    if(flagIgual)  saltar(arg(p, 1)); break;
            case "JNE":   if(!flagIgual) saltar(arg(p, 1)); break;
            case "SWAP":  ejecutarSWAP(arg(p, 1), arg(p, 2)); break;
            case "PARAM": ejecutarPARAM(p); break;
            case "INT":   ejecutarINT(arg(p, 1)); break;
            default:
                throw new RuntimeException("Instrucción desconocida: \"" + IR + "\"");
        }
    }

    // Devuelve el operando i, o null si la instrucción no lo tiene
    private String arg(String[] p, int i){
        return (i < p.length) ? p[i] : null;
    }

    // Un registro null significa AC (por ejemplo "INC" sin registro)
    private int leerRegistro(String registro){
        if(registro == null) return AC;
        switch (registro) {
            case "AC": return AC;
            case "AX": return AX;
            case "BX": return BX;
            case "CX": return CX;
            case "DX": return DX;
            case "AH": return AH;
            case "AL": return AL;
            default: throw new RuntimeException("Registro inválido: " + registro);
        }
    }

    private void escribirRegistro(String registro, int valor){
        if(registro == null){ AC = valor; return; }
        switch (registro) {
            case "AC": AC = valor; break;
            case "AX": AX = valor; break;
            case "BX": BX = valor; break;
            case "CX": CX = valor; break;
            case "DX": DX = valor; DXTexto = null; break;
            case "AH": AH = valor & 0xFF; break;
            case "AL": AL = valor & 0xFF; break;
            default: throw new RuntimeException("Registro inválido: " + registro);
        }
    }

    private boolean esRegistro(String texto){
        return "AX".equals(texto) || "BX".equals(texto)
            || "CX".equals(texto) || "DX".equals(texto) || "AC".equals(texto)
            || "AH".equals(texto) || "AL".equals(texto);
    }

    // LOAD AX: AC = AX
    private void ejecutarLOAD(String registro){
        AC = leerRegistro(registro);
    }

    // STORE BX: BX = AC
    private void ejecutarSTORE(String registro){
        escribirRegistro(registro, AC);
    }

    // MOV BX, AX  o  MOV BX, 5
    // MOV DX, "datos.txt" (nombre de archivo para INT 21H o texto para INT 10H)
    private void ejecutarMOV(String destino, String origen){
        if (origen != null && origen.startsWith("\"") && origen.endsWith("\"") && origen.length() >= 2) {
            DX = 0;
            DXTexto = origen.substring(1, origen.length() - 1);
            return;
        }
        if ("DX".equals(origen) && "DX".equals(destino)) return;
        String textoDX = "DX".equals(origen) ? DXTexto : null;
        int valor = esRegistro(origen) ? leerRegistro(origen) : Integer.parseInt(origen);
        escribirRegistro(destino, valor);
        if ("DX".equals(destino) && textoDX != null) DXTexto = textoDX;
    }

    // SUB BX: AC = AC - BX
    private void ejecutarSUB(String registro){
        AC = AC - leerRegistro(registro);
    }

    // ADD BX: AC = AC + BX
    private void ejecutarADD(String registro){
        AC = AC + leerRegistro(registro);
    }

    // INC (AC) o INC AX
    private void ejecutarINC(String registro){
        escribirRegistro(registro, leerRegistro(registro) + 1);
    }

    // DEC (AC) o DEC AX
    private void ejecutarDEC(String registro){
        escribirRegistro(registro, leerRegistro(registro) - 1);
    }

    // PUSH AX: guarda AX en la pila del proceso (lanza error si se desborda)
    private void ejecutarPUSH(String registro){
        bcpActual.push(leerRegistro(registro));
    }

    // POP AX: saca de la pila y lo guarda en AX
    private void ejecutarPOP(String registro){
        escribirRegistro(registro, bcpActual.pop());
    }

    // CMP AX, BX: flag = (AX == BX)
    private void ejecutarCMP(String reg1, String reg2){
        flagIgual = (leerRegistro(reg1) == leerRegistro(reg2));
    }

    // SWAP AX, BX: intercambia los valores de los dos registros
    private void ejecutarSWAP(String reg1, String reg2){
        int temp = leerRegistro(reg1);
        escribirRegistro(reg1, leerRegistro(reg2));
        escribirRegistro(reg2, temp);
    }

    // PARAM v1, v2, v3: mete cada valor en la pila, en orden
    private void ejecutarPARAM(String[] p){
        for(int i = 1; i < p.length; i++){
            bcpActual.push(Integer.parseInt(p[i]));
        }
    }

    // JMP/JE/JNE: salta "desplazamiento" instrucciones desde la actual.
    // Valida desbordamiento: el destino debe quedar dentro del proceso.
    private void saltar(String desplazamientoTexto){
        int desplazamiento = Integer.parseInt(desplazamientoTexto);
        int actual = PC - 1;                  // fetch ya avanzó el PC
        int destino = actual + desplazamiento;
        int base = bcpActual.getDireccionBase();
        if(destino < base || destino >= getLimite()){
            throw new RuntimeException("Desbordamiento en salto: la instrucción en la posición "
                + actual + " intenta saltar a " + destino + ", fuera del proceso ("
                + base + " a " + (getLimite() - 1) + ")");
        }
        PC = destino;
    }

    // ===== INT 21H: manejo de archivos =====
    // DX = nombre del archivo (MOV DX, "datos.txt"); AH = función; AL = dato leído/escrito.
    //   AH = 3CH crear archivo (queda abierto)   AH = 3DH abrir archivo
    //   AH = 4DH leer siguiente valor -> AL      AH = 40H escribir AL al final del archivo
    //   AH = 41H eliminar archivo
    // Protección: los programas .asm no se pueden modificar ni eliminar con INT 21H.
    private void ejecutarINT21(){
        String p = "P" + bcpActual.getIdProceso() + " > ";
        String nombre = DXTexto;
        if (nombre == null || nombre.isEmpty()) {
            imprimir(p + "INT 21H: DX no tiene un nombre de archivo (use MOV DX, \"archivo.txt\")");
            return;
        }
        boolean esPrograma = nombre.toLowerCase().endsWith(".asm");
        switch (AH) {
            case 0x3C: {   // crear
                if (esPrograma) { imprimir(p + "INT 21H: no se permite crear archivos .asm"); return; }
                if (disco.existeArchivo(nombre)) {
                    imprimir(p + "INT 21H: el archivo " + nombre + " ya existe");
                } else if (!disco.guardarArchivo(nombre, new ArrayList<>())) {
                    imprimir(p + "INT 21H: no hay espacio en el índice del disco para " + nombre);
                    return;
                } else {
                    imprimir(p + "archivo " + nombre + " creado");
                }
                bcpActual.abrirArchivo(nombre);
                break;
            }
            case 0x3D: {   // abrir
                if (!disco.existeArchivo(nombre)) {
                    imprimir(p + "INT 21H: el archivo " + nombre + " no existe");
                    return;
                }
                bcpActual.abrirArchivo(nombre);
                imprimir(p + "archivo " + nombre + " abierto");
                break;
            }
            case 0x4D: {   // leer
                if (!bcpActual.tieneAbierto(nombre)) {
                    imprimir(p + "INT 21H: el archivo " + nombre + " no está abierto");
                    return;
                }
                List<String> lineas = disco.leerArchivo(nombre);
                int pos = bcpActual.getPunteroLectura(nombre);
                if (pos >= lineas.size()) {
                    AL = 0;
                    imprimir(p + "fin del archivo " + nombre + " (AL = 0)");
                    return;
                }
                try {
                    AL = Integer.parseInt(lineas.get(pos).trim()) & 0xFF;
                } catch (NumberFormatException e) {
                    AL = 0;
                }
                bcpActual.setPunteroLectura(nombre, pos + 1);
                imprimir(p + "leído de " + nombre + ": AL = " + AL);
                break;
            }
            case 0x40: {   // escribir
                if (esPrograma) { imprimir(p + "INT 21H: protección, no se puede escribir en un programa .asm"); return; }
                if (!bcpActual.tieneAbierto(nombre)) {
                    imprimir(p + "INT 21H: el archivo " + nombre + " no está abierto");
                    return;
                }
                List<String> lineas = new ArrayList<>(disco.leerArchivo(nombre));
                lineas.add(String.valueOf(AL));
                if (disco.reescribirArchivo(nombre, lineas)) {
                    imprimir(p + "escrito en " + nombre + ": " + AL);
                } else {
                    imprimir(p + "INT 21H: no hay espacio en disco para escribir en " + nombre);
                }
                break;
            }
            case 0x41: {   // eliminar
                if (esPrograma) { imprimir(p + "INT 21H: protección, no se puede eliminar un programa .asm"); return; }
                if (!disco.eliminarArchivo(nombre)) {
                    imprimir(p + "INT 21H: el archivo " + nombre + " no existe");
                    return;
                }
                bcpActual.cerrarArchivo(nombre);
                imprimir(p + "archivo " + nombre + " eliminado");
                break;
            }
            default:
                imprimir(p + "INT 21H: AH = " + String.format("%02XH", AH)
                    + " no es una función válida (3CH, 3DH, 4DH, 40H, 41H)");
        }
    }

    private void ejecutarINT(String codigo){
        if(codigo == null) return;
        switch (codigo) {
            case "20H": PC = getLimite(); break;
            case "10H": imprimir("P" + bcpActual.getIdProceso() + " > " + (DXTexto != null ? DXTexto : String.valueOf(DX))); break;   // pantalla
            case "09H": solicitudTeclado = true; break;                                  // teclado
            case "21H": ejecutarINT21(); break;                                          // archivos
            default: throw new RuntimeException("Interrupción inválida: " + codigo);
        }
    }
}