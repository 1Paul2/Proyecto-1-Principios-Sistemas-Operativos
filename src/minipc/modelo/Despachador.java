package minipc.modelo;
import minipc.hardware.CPU;

public class Despachador {
    private CPU cpu;
    public Despachador(CPU cpu) { this.cpu = cpu; }
    public void despachar(BCP bcp) {
        cpu.setBcpActual(bcp);
        bcp.restaurarEstado(cpu);
        cpu.reiniciarInstruccion();
        bcp.setEstado("Ejecución");
    }
    public void capturar(BCP bcp) { bcp.capturaEstado(cpu); }
}