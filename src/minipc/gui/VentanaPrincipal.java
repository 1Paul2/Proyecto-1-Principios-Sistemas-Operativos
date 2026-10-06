package minipc.gui;

/**
 *
 * @author Poll Anthony Garro Vargas - 2024129001
 * @Universidad: Instituto Tecnológico de Costa Rica
 *
 */

import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import minipc.gui.VentanaPrincipal.Componentes.BarraUso;
import minipc.gui.VentanaPrincipal.Componentes.Boton;
import minipc.gui.VentanaPrincipal.Componentes.Distintivo;
import minipc.gui.VentanaPrincipal.Componentes.RenderEstado;
import minipc.gui.VentanaPrincipal.Componentes.Tarjeta;
import minipc.hardware.CPU;
import minipc.hardware.Disco;
import minipc.hardware.Memoria;
import minipc.modelo.BCP;
import minipc.modelo.GestorProcesos;
import minipc.modelo.Instruccion;
import minipc.modelo.Planificador;
import minipc.util.ConfiguracionSistema;
import minipc.util.ParserASM;

/**
 * VentanaPrincipal: interfaz gráfica del Mini PC. Muestra la cola de trabajos,
 * la CPU y sus registros, el BCP actual, la pila, la memoria principal (mapa
 * y tabla), el disco y la pantalla/teclado. Toda la lógica la delega en
 * GestorProcesos; esta clase solo dibuja y reacciona a los botones.
 */
public class VentanaPrincipal extends JFrame {

    private final ConfiguracionSistema configuracion = new ConfiguracionSistema();
    private GestorProcesos gestor;

    // Ejecución automática
    private final Timer timerAuto;
    // En Siguiente: mientras un proceso espera el teclado (INT 09H), el reloj
    // avanza solo cada segundo real hasta que se ingrese el valor
    private final Timer timerEspera;
    private boolean modoAutomatico = false;

    // ---- Encabezado y barra de herramientas ----
    private JLabel lblReloj, lblHora;
    private Boton btnCargar, btnEjecutar, btnPaso, btnDetener, btnLimpiar, btnEstadisticas, btnConfigurar, btnSalir;
    private JComboBox<String> cmbVelocidad;

    // ---- Cola de trabajos ----
    private Tarjeta tarjetaCola;
    private DefaultTableModel modeloCola;
    private JTable tablaCola;

    // ---- Recursos y dispositivos ----
    private BarraUso barraProcesos, barraMemoria, barraBCP, barraDisco, barraVirtual;
    private Distintivo dispCPU, dispPantalla, dispTeclado, dispDisco;

    // ---- Memoria ----
    private Tarjeta tarjetaMemoria;
    private MapaMemoria mapaMemoria;
    private DefaultTableModel modeloMemoria;
    private JTable tablaMemoria;
    private String[] duenoPosicion = new String[0];   // "S.O.", "P1"  por posición
    private int posicionPC = -1;
    private int posicionPCVirtual = -1;   // posición del disco del PC si ejecuta desde memoria virtual

    // ---- CPU ----
    private JLabel lblProcesoActual, lblInstruccion;
    private Distintivo distEstadoCPU;
    private final JLabel[] valoresRegistro = new JLabel[10];  // PC, IR, AC, AX, BX, CX, DX, FLAG, AH, AL
    private JProgressBar barraInstruccion;

    // ---- Pila ----
    private PanelPila panelPila;

    // ---- BCP ----
    private final JLabel[] valoresBCP = new JLabel[10];
    private Distintivo distEstadoBCP;

    // ---- Disco ----
    private Tarjeta tarjetaDisco;
    private DefaultTableModel modeloDisco;
    private JTable tablaDisco;

    // ---- Pantalla y teclado ----
    private JTextPane areaPantalla;
    private JTextField txtTeclado;
    private Boton btnEnviar;
    private JLabel lblTeclado;

    // E: no aplica
    // S: no aplica constructor
    // R: arma la ventana completa con la configuración de config.txt
    public VentanaPrincipal(){
        super("Mini PC · Gestor de Procesos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Al cerrar el programa (botón Salir, la X de la ventana o Stop de NetBeans)
        // la configuración vuelve a los valores predeterminados en config.txt
        Runtime.getRuntime().addShutdownHook(new Thread(configuracion::restaurarPredeterminados));
        getContentPane().setBackground(Tema.FONDO);
        setLayout(new BorderLayout(0, 12));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(14, 16, 14, 16));

        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearCuerpo(), BorderLayout.CENTER);

        timerAuto = new Timer(400, e -> pasoAutomatico());
        timerEspera = new Timer(1000, e -> segundoDeEspera());

        setMinimumSize(new Dimension(1280, 780));
        setSize(new Dimension(1500, 900));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        actualizarVista();
    }


    //ENCABEZADO


    private JPanel crearEncabezado(){
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        // ---- Título ----
        JPanel fila1 = new JPanel(new BorderLayout());
        fila1.setOpaque(false);

        JPanel titulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titulo.setOpaque(false);
        JLabel logo = new JLabel("SO", SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, Tema.VERDE, getWidth(), getHeight(), Tema.VERDE_OSCURO));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logo.setFont(new Font(Tema.TITULO.getFamily(), Font.BOLD, 16));
        logo.setForeground(Color.WHITE);
        logo.setPreferredSize(new Dimension(46, 46));
        titulo.add(logo);

        JPanel textos = new JPanel(new GridLayout(1, 1));
        textos.setOpaque(false);
        JLabel lblTitulo = new JLabel("Mini PC  ·  Gestor de Procesos");
        lblTitulo.setFont(Tema.TITULO);
        lblTitulo.setForeground(Tema.TEXTO);
        textos.add(lblTitulo);
        titulo.add(textos);
        fila1.add(titulo, BorderLayout.WEST);

        // ---- Reloj ----
        JPanel reloj = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        reloj.setOpaque(false);
        lblReloj = chip("Tiempo  0 s", Tema.VERDE_OSCURO, Tema.VERDE_CLARO);
        lblHora = chip("--:--:--", Tema.AZUL, Tema.AZUL_CLARO);
        reloj.add(lblReloj);
        reloj.add(lblHora);
        fila1.add(reloj, BorderLayout.EAST);
        panel.add(fila1, BorderLayout.NORTH);

        // ---- Botones ----
        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        botones.setOpaque(false);

        btnCargar       = new Boton("Cargar archivos", Tema.VERDE_OSCURO);
        btnEjecutar     = new Boton("Ejecutar", Tema.VERDE);
        btnPaso         = new Boton("Siguiente", Tema.AZUL);
        btnDetener      = new Boton("Detener", Tema.ROJO);
        btnLimpiar      = new Boton("Limpiar", Tema.GRIS);
        btnEstadisticas = new Boton("Estadísticas", Tema.MORADO);
        btnConfigurar   = new Boton("Configuración", new Color(0x334155));
        btnSalir        = new Boton("Salir", new Color(0xB91C1C));
        btnSalir.setToolTipText("Cerrar el Mini PC");

        btnCargar.setToolTipText("Cargar uno o varios archivos .asm");
        btnEjecutar.setToolTipText("Ejecutar automáticamente todos los procesos hasta su finalización");
        btnPaso.setToolTipText("Avanzar 1 segundo de CPU (botón \"siguiente\")");
        btnDetener.setToolTipText("Pausar la ejecución automática");
        btnDetener.setEnabled(false);

        for (Boton b : new Boton[]{btnCargar, btnEjecutar, btnPaso, btnDetener, btnLimpiar, btnEstadisticas, btnConfigurar}) {
            botones.add(b);
        }
        barra.add(botones, BorderLayout.WEST);

        JPanel velocidad = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        velocidad.setOpaque(false);
        JLabel lblVel = new JLabel("Velocidad de Ejecutar:");
        lblVel.setFont(Tema.NORMAL);
        lblVel.setForeground(Tema.TEXTO_SUAVE);
        // Por defecto, "Ejecutar" avanza 1 segundo de CPU por cada segundo real,
        // igual que presionar "Siguiente" una vez por segundo.
        // "Instantánea" corre todo de una vez.
        cmbVelocidad = new JComboBox<>(new String[]{"1 segundo", "Instantánea"});
        cmbVelocidad.setSelectedIndex(0);
        cmbVelocidad.setFont(Tema.NORMAL);
        cmbVelocidad.addActionListener(e -> timerAuto.setDelay(retardoSeleccionado()));
        velocidad.add(lblVel);
        velocidad.add(cmbVelocidad);
        velocidad.add(Box.createHorizontalStrut(8));
        velocidad.add(btnSalir);
        barra.add(velocidad, BorderLayout.EAST);
        panel.add(barra, BorderLayout.SOUTH);

        btnCargar.addActionListener(e -> cargarArchivos());
        btnEjecutar.addActionListener(e -> iniciarAutomatico());
        btnPaso.addActionListener(e -> pasoManual());
        btnDetener.addActionListener(e -> detenerAutomatico(true));
        btnLimpiar.addActionListener(e -> limpiar());
        btnEstadisticas.addActionListener(e -> mostrarEstadisticas());
        btnConfigurar.addActionListener(e -> mostrarConfiguracion());
        btnSalir.addActionListener(e -> salir());
        return panel;
    }

    private JLabel chip(String texto, Color frente, Color fondo){
        Distintivo d = new Distintivo();
        d.setFont(Tema.NEGRITA);
        d.setBorder(new EmptyBorder(6, 14, 6, 14));
        d.setColores(texto, frente, fondo);
        return d;
    }

    //CUERPO
 

    private JPanel crearCuerpo(){
        JPanel cuerpo = new JPanel(new GridBagLayout());
        cuerpo.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 1;
        g.insets = new Insets(0, 0, 0, 12);

        g.gridx = 0; g.weightx = 0.27;
        cuerpo.add(crearColumnaIzquierda(), g);
        g.gridx = 1; g.weightx = 0.45;
        cuerpo.add(crearColumnaCentral(), g);
        g.gridx = 2; g.weightx = 0.28; g.insets = new Insets(0, 0, 0, 0);
        cuerpo.add(crearColumnaDerecha(), g);
        return cuerpo;
    }

    // Columna con altura proporcional por fila
    private JPanel columna(Component[] partes, double[] pesos){
        JPanel col = new JPanel(new GridBagLayout());
        col.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.fill = GridBagConstraints.BOTH;
        g.weightx = 1;
        for (int i = 0; i < partes.length; i++) {
            g.gridy = i;
            g.weighty = pesos[i];
            g.insets = new Insets(0, 0, i < partes.length - 1 ? 12 : 0, 0);
            col.add(partes[i], g);
        }
        return col;
    }

    // ------------------------- Columna izquierda -------------------------

    private JPanel crearColumnaIzquierda(){
        // Cola de trabajos (lista de trabajos + lista de procesos)
        tarjetaCola = new Tarjeta("Cola de trabajos", Tema.VERDE);
        modeloCola = new DefaultTableModel(new String[]{"#", "Programa", "Estado", "Base", "Alcance"}, 0) {
            @Override public boolean isCellEditable(int r, int c){ return false; }
        };
        tablaCola = new JTable(modeloCola);
        Componentes.estilizarTabla(tablaCola);
        tablaCola.getColumnModel().getColumn(0).setCellRenderer(new RenderProceso());
        tablaCola.getColumnModel().getColumn(2).setCellRenderer(new RenderEstado());
        tablaCola.getColumnModel().getColumn(0).setPreferredWidth(44);
        tablaCola.getColumnModel().getColumn(1).setPreferredWidth(130);
        tablaCola.getColumnModel().getColumn(2).setPreferredWidth(100);
        tablaCola.getColumnModel().getColumn(3).setPreferredWidth(50);
        tablaCola.getColumnModel().getColumn(4).setPreferredWidth(60);
        tarjetaCola.getContenido().add(Componentes.scroll(tablaCola), BorderLayout.CENTER);
        tarjetaCola.getContenido().add(crearLeyendaEstados(), BorderLayout.SOUTH);

        // Recursos del sistema
        Tarjeta recursos = new Tarjeta("Recursos del sistema", Tema.AZUL);
        JPanel barras = new JPanel(new GridLayout(5, 1, 0, 10));
        barras.setOpaque(false);
        barraProcesos = new BarraUso("Procesos en memoria", Tema.VERDE);
        barraMemoria = new BarraUso("Memoria de usuario", Tema.AZUL);
        barraBCP = new BarraUso("Zona S.O. (BCP)", Tema.NARANJA_SO);
        barraDisco = new BarraUso("Almacenamiento", Tema.MORADO);
        barraVirtual = new BarraUso("Memoria virtual", new Color(0x14B8A6));
        barras.add(barraProcesos);
        barras.add(barraMemoria);
        barras.add(barraVirtual);
        barras.add(barraBCP);
        barras.add(barraDisco);
        recursos.getContenido().add(barras, BorderLayout.NORTH);

        // Dispositivos
        Tarjeta dispositivos = new Tarjeta("Dispositivos", Tema.MORADO);
        JPanel filas = new JPanel(new GridLayout(4, 1, 0, 6));
        filas.setOpaque(false);
        dispCPU = new Distintivo();
        dispPantalla = new Distintivo();
        dispTeclado = new Distintivo();
        dispDisco = new Distintivo();
        filas.add(filaDispositivo("CPU 1", dispCPU));
        filas.add(filaDispositivo("Pantalla", dispPantalla));
        filas.add(filaDispositivo("Teclado (entrada)", dispTeclado));
        filas.add(filaDispositivo("Disco", dispDisco));
        dispositivos.getContenido().add(filas, BorderLayout.NORTH);

        return columna(new Component[]{tarjetaCola, recursos, dispositivos}, new double[]{1.0, 0.0, 0.0});
    }

    private JPanel crearLeyendaEstados(){
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        p.setOpaque(false);
        for (String e : new String[]{"Nuevo", "Preparado", "Ejecución", "En espera", "Suspendido en espera", "Suspendido preparado", "Finalizado"}) {
            Distintivo d = new Distintivo();
            d.setEstado(e);
            p.add(d);
        }
        return p;
    }

    private JPanel filaDispositivo(String nombre, Distintivo estado){
        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        JLabel lbl = new JLabel(nombre);
        lbl.setFont(Tema.NORMAL);
        lbl.setForeground(Tema.TEXTO);
        fila.add(lbl, BorderLayout.WEST);
        estado.setHorizontalAlignment(SwingConstants.RIGHT);
        estado.setPreferredSize(new Dimension(170, 24));
        fila.add(estado, BorderLayout.EAST);
        return fila;
    }

    // -------------------------- Columna central --------------------------

    private JPanel crearColumnaCentral(){
        // Memoria principal: mapa visual + tabla
        tarjetaMemoria = new Tarjeta("Memoria principal", Tema.VERDE);
        mapaMemoria = new MapaMemoria();

        modeloMemoria = new DefaultTableModel(new String[]{"Pos", "Valor en memoria", "Dueño"}, 0) {
            @Override public boolean isCellEditable(int r, int c){ return false; }
        };
        tablaMemoria = new JTable(modeloMemoria);
        Componentes.estilizarTabla(tablaMemoria);
        tablaMemoria.setRowHeight(22);
        RenderMemoria render = new RenderMemoria();
        for (int i = 0; i < 3; i++) tablaMemoria.getColumnModel().getColumn(i).setCellRenderer(render);
        tablaMemoria.getColumnModel().getColumn(0).setPreferredWidth(70);
        tablaMemoria.getColumnModel().getColumn(0).setMinWidth(64);
        tablaMemoria.getColumnModel().getColumn(1).setPreferredWidth(300);
        tablaMemoria.getColumnModel().getColumn(2).setPreferredWidth(60);

        JPanel mapaConLeyenda = new JPanel(new BorderLayout(0, 6));
        mapaConLeyenda.setOpaque(false);
        mapaConLeyenda.add(mapaMemoria, BorderLayout.CENTER);
        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leyenda.setOpaque(false);
        leyenda.add(itemLeyenda("S.O. / BCP", Tema.NARANJA_SO));
        leyenda.add(itemLeyenda("Libre", new Color(0xCBD5D0)));
        leyenda.add(itemLeyenda("PC", Tema.ROJO));
        leyenda.add(itemLeyenda("(+N virtual) = parte en disco", Tema.MORADO));
        mapaConLeyenda.add(leyenda, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mapaConLeyenda, Componentes.scroll(tablaMemoria));
        split.setResizeWeight(0.42);
        split.setBorder(null);
        split.setOpaque(false);
        split.setDividerSize(8);
        tarjetaMemoria.getContenido().add(split, BorderLayout.CENTER);

        // Pantalla + teclado
        Tarjeta pantalla = new Tarjeta("Pantalla", new Color(0x0F766E));
        areaPantalla = new JTextPane();
        areaPantalla.setEditable(false);
        areaPantalla.setFont(Tema.MONO_NORMAL);
        areaPantalla.setBackground(new Color(0xFBFDFB));
        areaPantalla.setBorder(new EmptyBorder(8, 10, 8, 10));
        pantalla.getContenido().add(Componentes.scroll(areaPantalla), BorderLayout.CENTER);

        JPanel entrada = new JPanel(new BorderLayout(8, 0));
        entrada.setOpaque(false);
        entrada.setBorder(new EmptyBorder(8, 0, 0, 0));
        lblTeclado = new JLabel(">> Ingresar valor:");
        lblTeclado.setFont(Tema.NEGRITA);
        lblTeclado.setForeground(Tema.TEXTO_SUAVE);
        txtTeclado = new JTextField();
        txtTeclado.setFont(Tema.MONO_NORMAL);
        txtTeclado.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Tema.BORDE), new EmptyBorder(6, 8, 6, 8)));
        btnEnviar = new Boton("Enviar", Tema.AMBAR);
        entrada.add(lblTeclado, BorderLayout.WEST);
        entrada.add(txtTeclado, BorderLayout.CENTER);
        entrada.add(btnEnviar, BorderLayout.EAST);
        pantalla.getContenido().add(entrada, BorderLayout.SOUTH);
        txtTeclado.addActionListener(e -> enviarTeclado());
        btnEnviar.addActionListener(e -> enviarTeclado());

        // Disco
        tarjetaDisco = new Tarjeta("Disco", Tema.MORADO);
        modeloDisco = new DefaultTableModel(new String[]{"Pos", "Valor en disco", "Zona"}, 0) {
            @Override public boolean isCellEditable(int r, int c){ return false; }
        };
        tablaDisco = new JTable(modeloDisco);
        Componentes.estilizarTabla(tablaDisco);
        tablaDisco.setRowHeight(22);
        RenderDisco renderDisco = new RenderDisco();
        for (int i = 0; i < 3; i++) tablaDisco.getColumnModel().getColumn(i).setCellRenderer(renderDisco);
        tablaDisco.getColumnModel().getColumn(0).setPreferredWidth(64);
        tablaDisco.getColumnModel().getColumn(0).setMinWidth(60);
        tablaDisco.getColumnModel().getColumn(1).setPreferredWidth(220);
        tablaDisco.getColumnModel().getColumn(2).setPreferredWidth(80);
        tarjetaDisco.getContenido().add(Componentes.scroll(tablaDisco), BorderLayout.CENTER);

        JPanel abajo = new JPanel(new GridLayout(1, 2, 12, 0));
        abajo.setOpaque(false);
        abajo.add(pantalla);
        abajo.add(tarjetaDisco);

        return columna(new Component[]{tarjetaMemoria, abajo}, new double[]{0.58, 0.42});
    }

    private JLabel itemLeyenda(String texto, Color c){
        JLabel l = new JLabel(texto, new Componentes.IconoPunto(c, 9), SwingConstants.LEFT);
        l.setFont(Tema.PEQUENA);
        l.setForeground(Tema.TEXTO_SUAVE);
        return l;
    }

    // -------------------------- Columna derecha --------------------------

    private JPanel crearColumnaDerecha(){
        // CPU
        Tarjeta cpu = new Tarjeta("CPU 1", Tema.VERDE);
        JPanel contCPU = new JPanel(new BorderLayout(0, 10));
        contCPU.setOpaque(false);

        JPanel filaProceso = new JPanel(new BorderLayout());
        filaProceso.setOpaque(false);
        lblProcesoActual = new JLabel("CPU libre");
        lblProcesoActual.setFont(Tema.NEGRITA);
        lblProcesoActual.setForeground(Tema.TEXTO);
        distEstadoCPU = new Distintivo();
        distEstadoCPU.setPreferredSize(new Dimension(110, 24));
        filaProceso.add(lblProcesoActual, BorderLayout.CENTER);
        filaProceso.add(distEstadoCPU, BorderLayout.EAST);
        contCPU.add(filaProceso, BorderLayout.NORTH);

        JPanel registros = new JPanel(new GridLayout(5, 2, 8, 6));
        registros.setOpaque(false);
        String[] nombres = {"PC", "IR", "AC", "AX", "BX", "CX", "DX", "Flag CMP", "AH (INT 21H)", "AL (INT 21H)"};
        Color[] colores = {Tema.ROJO, Tema.VERDE_OSCURO, Tema.AZUL, Tema.MORADO,
                           Tema.MORADO, Tema.MORADO, Tema.MORADO, Tema.AMBAR,
                           new Color(0x0F766E), new Color(0x0F766E)};
        for (int i = 0; i < nombres.length; i++) {
            registros.add(crearRegistro(nombres[i], colores[i], i));
        }
        contCPU.add(registros, BorderLayout.CENTER);

        JPanel instr = new JPanel(new BorderLayout(0, 4));
        instr.setOpaque(false);
        lblInstruccion = new JLabel("Instrucción en curso: -");
        lblInstruccion.setFont(Tema.PEQUENA);
        lblInstruccion.setForeground(Tema.TEXTO_SUAVE);
        barraInstruccion = new JProgressBar(0, 1);
        barraInstruccion.setForeground(Tema.VERDE);
        barraInstruccion.setBackground(Tema.VERDE_CLARO);
        barraInstruccion.setBorderPainted(false);
        barraInstruccion.setPreferredSize(new Dimension(10, 8));
        instr.add(lblInstruccion, BorderLayout.NORTH);
        instr.add(barraInstruccion, BorderLayout.CENTER);
        contCPU.add(instr, BorderLayout.SOUTH);
        cpu.getContenido().add(contCPU, BorderLayout.CENTER);

        // Pila
        Tarjeta pila = new Tarjeta("Pila del proceso (tamaño 5)", Tema.AMBAR);
        panelPila = new PanelPila();
        pila.getContenido().add(panelPila, BorderLayout.CENTER);

        // BCP
        Tarjeta bcp = new Tarjeta("BCP actual", Tema.AZUL);
        JPanel datos = new JPanel(new GridBagLayout());
        datos.setOpaque(false);
        String[] campos = {"Proceso", "Estado", "Base", "Alcance", "Prioridad", "CPU",
                           "Inicio", "Tiempo empleado", "Dirección del BCP", "Archivos abiertos"};
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(2, 0, 2, 10);
        for (int i = 0; i < campos.length; i++) {
            g.gridy = i;
            g.gridx = 0; g.weightx = 0; g.fill = GridBagConstraints.NONE;
            JLabel lbl = new JLabel(campos[i]);
            lbl.setFont(Tema.NORMAL);
            lbl.setForeground(Tema.TEXTO_SUAVE);
            datos.add(lbl, g);
            g.gridx = 1; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
            if (i == 1) {
                distEstadoBCP = new Distintivo();
                distEstadoBCP.setHorizontalAlignment(SwingConstants.LEFT);
                distEstadoBCP.setPreferredSize(new Dimension(110, 22));
                datos.add(distEstadoBCP, g);
            } else {
                JLabel v = new JLabel("-");
                v.setFont(Tema.MONO_NORMAL);
                v.setForeground(Tema.TEXTO);
                valoresBCP[i] = v;
                datos.add(v, g);
            }
        }
        bcp.getContenido().add(datos, BorderLayout.NORTH);

        return columna(new Component[]{cpu, pila, bcp}, new double[]{0.0, 0.0, 1.0});
    }

    private JPanel crearRegistro(String nombre, Color color, int indice){
        JPanel p = new JPanel(new BorderLayout(0, 2)) {
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Tema.aclarar(color, 0.9f));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(4, 12, 4, 8));
        JLabel lbl = new JLabel(nombre);
        lbl.setFont(Tema.PEQUENA_B);
        lbl.setForeground(color);
        JLabel valor = new JLabel("0");
        valor.setFont(indice == 1 ? Tema.MONO_NORMAL : Tema.MONO_GRANDE);
        valor.setForeground(Tema.TEXTO);
        valoresRegistro[indice] = valor;
        p.add(lbl, BorderLayout.NORTH);
        p.add(valor, BorderLayout.CENTER);
        return p;
    }

 
    // ACCIONES


    private void asegurarGestor(){
        if (gestor == null) {
            gestor = new GestorProcesos(configuracion);
        }
    }

    // E: no aplica
    // S: no aplica 
    // R: carga uno o varios .asm; los inválidos se reportan con su línea y motivo
    private void cargarArchivos(){
        JFileChooser selector = new JFileChooser(new File("."));
        selector.setMultiSelectionEnabled(true);
        selector.setFileFilter(new FileNameExtensionFilter("Programas ensamblador (*.asm)", "asm"));
        if (selector.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        asegurarGestor();
        List<String> errores = new ArrayList<>();
        int cargados = 0;
        for (File archivo : selector.getSelectedFiles()) {
            try {
                List<Instruccion> instrucciones = ParserASM.leerArchivo(archivo);
                BCP bcp = gestor.cargarPrograma(instrucciones, archivo.getName());
                gestor.getCpu().imprimir("[SO] " + archivo.getName() + " cargado como P"
                    + bcp.getIdProceso() + " (" + instrucciones.size() + " instrucciones)");
                cargados++;
            } catch (Exception e) {
                errores.add("• " + e.getMessage());
            }
        }
        actualizarVista();

        if (!errores.isEmpty()) {
            JTextArea txt = new JTextArea(String.join("\n", errores));
            txt.setEditable(false);
            txt.setFont(Tema.MONO_NORMAL);
            txt.setForeground(Tema.ROJO);
            txt.setBackground(Tema.ROJO_CLARO);
            txt.setBorder(new EmptyBorder(8, 8, 8, 8));
            txt.setLineWrap(true);
            txt.setWrapStyleWord(true);
            JScrollPane sp = new JScrollPane(txt);
            sp.setPreferredSize(new Dimension(560, Math.min(260, 60 + errores.size() * 40)));
            JOptionPane.showMessageDialog(this, sp,
                cargados + " cargado(s), " + errores.size() + " con errores", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Botón "Siguiente": 1 segundo de CPU
    private void pasoManual(){
        if (!validarHayProcesos()) return;
        if (gestor.esperandoTeclado()) {
            // El tiempo ya corre solo; no se suman segundos extra con clics
            avisar("El proceso está esperando un valor de teclado.\nIngreselo en la Pantalla y presione Enter.");
            return;
        }
        gestor.ejecutarUnPaso();
        actualizarVista();
        if (gestor.esperandoTeclado()) {
            iniciarEsperaTeclado();
        } else if (!gestor.hayProcesos()) {
            finEjecucion();
        }
    }

    // Arranca el conteo automático de segundos mientras se espera el teclado (modo Siguiente)
    private void iniciarEsperaTeclado(){
        if (!modoAutomatico && !timerEspera.isRunning()) timerEspera.start();
    }

    // Cada segundo real de espera: suma 1 s al reloj y al tiempo del proceso detenido en INT 09H
    private void segundoDeEspera(){
        if (gestor == null || !gestor.esperandoTeclado() || modoAutomatico) {
            timerEspera.stop();
            return;
        }
        gestor.ejecutarUnPaso();
        actualizarVista();
    }

    // Botón "Ejecutar": modo automático
    private void iniciarAutomatico(){
        if (!validarHayProcesos()) return;
        if (cmbVelocidad.getSelectedIndex() == 1) {          // instantánea
            gestor.ejecutarTodo();
            actualizarVista();
            if (gestor.bloqueadoPorTeclado()) {
                avisar("La ejecución se detuvo: un proceso espera un valor de teclado.");
            } else if (!gestor.hayProcesos()) {
                finEjecucion();
            }
            return;
        }
        modoAutomatico = true;
        timerEspera.stop();                 // en automático el timer principal cuenta la espera
        timerAuto.setDelay(retardoSeleccionado());
        timerAuto.start();
        actualizarBotones();
    }

    private void pasoAutomatico(){
        if (gestor == null || !gestor.hayProcesos()) {
            detenerAutomatico(false);
            actualizarVista();
            if (gestor != null) finEjecucion();
            return;
        }
        if (!gestor.ejecutarUnPaso() && gestor.bloqueadoPorTeclado()) {
            timerAuto.stop();       // se reanuda solo al ingresar el valor
        }
        actualizarVista();
    }

    private void detenerAutomatico(boolean manual){
        modoAutomatico = false;
        timerAuto.stop();
        if (manual && gestor != null) gestor.getCpu().imprimir("[SO] Ejecución automática pausada");
        actualizarBotones();
        actualizarVista();
    }

    private int retardoSeleccionado(){
        return 1000;
    }

    private void finEjecucion(){
        detenerAutomatico(false);
        gestor.getCpu().imprimir("[SO] Todos los procesos finalizaron en " + gestor.getReloj() + " s");
        actualizarVista();
        int r = JOptionPane.showConfirmDialog(this, "Todos los procesos finalizaron.\n¿Ver estadísticas?",
            "Ejecución completa", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (r == JOptionPane.YES_OPTION) mostrarEstadisticas();
    }

    private void enviarTeclado(){
        if (gestor == null || !gestor.esperandoTeclado()) return;
        String texto = txtTeclado.getText().trim();
        try {
            gestor.ingresarTeclado(Integer.parseInt(texto));
            timerEspera.stop();             // ya llegó el valor: el reloj deja de correr solo
            txtTeclado.setText("");
            if (modoAutomatico && !timerAuto.isRunning()) timerAuto.start();
        } catch (NumberFormatException e) {
            avisar("Ingresá un número entero entre 0 y 255.");
        } catch (RuntimeException e) {
            avisar(e.getMessage());
        }
        actualizarVista();
    }

    // E: no aplica
    // S: no aplica (void)
    // R: pide confirmación si hay procesos sin terminar y cierra la aplicación
    private void salir(){
        String mensaje = (gestor != null && gestor.hayProcesos())
            ? "Hay procesos sin terminar. ¿Seguro que querés salir?"
            : "¿Seguro que querés salir del Mini PC?";
        int r = JOptionPane.showConfirmDialog(this, mensaje, "Salir",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            timerAuto.stop();
            dispose();
            System.exit(0);
        }
    }

    // E: no aplica
    // S: no aplica (void)
    // R: siempre pide confirmación antes de borrar procesos, memoria, disco, pantalla y estadísticas
    private void limpiar(){
        if (gestor == null) {
            JOptionPane.showMessageDialog(this, "No hay nada que limpiar.", "Limpiar", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String mensaje = gestor.hayProcesos()
            ? "Hay procesos sin terminar.\n¿Está seguro de que quiere limpiar todo?"
            : "¿Está seguro de que quiere limpiar todo?";
        int r = JOptionPane.showConfirmDialog(this,
            mensaje + "\nSe borrarán los procesos, la memoria, el disco, la pantalla y las estadísticas.",
            "Confirmar limpieza", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION) return;
        detenerAutomatico(false);
        timerEspera.stop();
        gestor = null;
        txtTeclado.setText("");
        actualizarVista();
    }

    private boolean validarHayProcesos(){
        if (gestor == null || gestor.getTodosLosProcesos().isEmpty()) {
            avisar("Primero cargá uno o más archivos .asm");
            return false;
        }
        if (!gestor.hayProcesos()) {
            avisar("Todos los procesos ya finalizaron. Usá Limpiar para empezar de nuevo.");
            return false;
        }
        return true;
    }

    private void avisar(String msg){
        JOptionPane.showMessageDialog(this, msg, "Aviso", JOptionPane.WARNING_MESSAGE);
    }


    // ACTUALIZAR LA VISTA
 

    // E: no aplica
    // S: no aplica (void)
    // R: refresca todos los paneles con el estado actual del GestorProcesos
    private void actualizarVista(){
        actualizarEncabezado();
        actualizarCola();
        actualizarRecursos();
        actualizarCPU();
        actualizarBCP();
        actualizarMemoria();
        actualizarDisco();
        actualizarPantalla();
        actualizarBotones();
    }

    private void actualizarEncabezado(){
        if (gestor == null) {
            ((Distintivo) lblReloj).setColores("Tiempo  0 s", Tema.VERDE_OSCURO, Tema.VERDE_CLARO);
            ((Distintivo) lblHora).setColores("--:--:--", Tema.AZUL, Tema.AZUL_CLARO);
            return;
        }
        ((Distintivo) lblReloj).setColores("Tiempo  " + gestor.getReloj() + " s", Tema.VERDE_OSCURO, Tema.VERDE_CLARO);
        ((Distintivo) lblHora).setColores("Hora  " + gestor.getHoraSimulada(), Tema.AZUL, Tema.AZUL_CLARO);
    }

    private void actualizarCola(){
        modeloCola.setRowCount(0);
        if (gestor == null) {
            tarjetaCola.setExtra("");
            return;
        }
        for (BCP b : gestor.getTodosLosProcesos()) {
            modeloCola.addRow(new Object[]{
                b.getIdProceso(),
                b.getArchivosAbiertos().isEmpty() ? "-" : b.getArchivosAbiertos().get(0),
                b.getEstado(),
                b.getDireccionBase() >= 0 && !"Finalizado".equals(b.getEstado()) ? b.getDireccionBase() : "-",
                b.getTamanioProceso()
            });
        }
        int trabajos = gestor.getPlanificador().getListaTrabajos().tamanio();
        tarjetaCola.setExtra(gestor.getTodosLosProcesos().size() + " procesos · " + trabajos + " esperando memoria");
    }

    private void actualizarRecursos(){
        if (gestor == null) {
            int usuario = configuracion.getMemoriaPrincipal() - configuracion.getMemoriaSistema();
            barraProcesos.setValor(0, Planificador.MAX_PROCESOS_EN_MEMORIA);
            barraMemoria.setValor(0, usuario);
            barraBCP.setValor(0, Math.min(Planificador.MAX_PROCESOS_EN_MEMORIA, configuracion.getMemoriaSistema()));
            barraDisco.setValor(0, configuracion.getAlmacenamientoSecundario());
            barraVirtual.setValor(0, configuracion.getMemoriaVirtual());
            dispCPU.setColores("Libre", Tema.GRIS, Tema.GRIS_CLARO);
            dispPantalla.setColores("Lista", Tema.VERDE_OSCURO, Tema.VERDE_CLARO);
            dispTeclado.setColores("Libre", Tema.GRIS, Tema.GRIS_CLARO);
            dispDisco.setColores("Sin archivos", Tema.GRIS, Tema.GRIS_CLARO);
            return;
        }
        Memoria m = gestor.getMemoria();
        Disco d = gestor.getDisco();
        int usuarioTotal = m.getTamanoTotal() - m.getInicioUsuario();
        int usuarioUsado = 0, bcps = 0;
        for (BCP b : gestor.getTodosLosProcesos()) {
            String e = b.getEstado();
            if (!"Nuevo".equals(e) && !"Finalizado".equals(e)) {
                usuarioUsado += b.getEnRAM();
                if (b.getDireccionBCP() >= 0) bcps++;
            }
        }
        int discoUsado = 0, archivos = 0;
        for (int i = 0; i < d.getTamanoTotal(); i++) {
            String v = d.leer(i);
            if (v != null && !v.isEmpty()) {
                discoUsado++;
                if (i < d.getTamanoIndice()) archivos++;
            }
        }
        barraProcesos.setValor(gestor.procesosEnMemoria(), Planificador.MAX_PROCESOS_EN_MEMORIA);
        barraMemoria.setValor(usuarioUsado, usuarioTotal);
        barraBCP.setValor(bcps, Math.min(Planificador.MAX_PROCESOS_EN_MEMORIA, m.getInicioUsuario()));
        barraDisco.setValor(discoUsado, d.getTamanoTotal());
        barraVirtual.setValor(d.virtualUsada(), d.getTamanoMemoriaVirtual());

        BCP enCpu = gestor.getCpu().getBcpActual();
        if (enCpu != null) dispCPU.setColores("Ejecutando P" + enCpu.getIdProceso(), Tema.VERDE_OSCURO, Tema.VERDE_CLARO);
        else dispCPU.setColores("Libre", Tema.GRIS, Tema.GRIS_CLARO);

        dispPantalla.setColores(gestor.getCpu().getPantalla().isEmpty() ? "Lista" : "Mostrando salida",
            Tema.VERDE_OSCURO, Tema.VERDE_CLARO);

        BCP esperando = gestor.getProcesoEsperandoTeclado();
        if (esperando != null) dispTeclado.setColores("Esperando P" + esperando.getIdProceso(), Tema.AMBAR, Tema.AMBAR_CLARO);
        else dispTeclado.setColores("Libre", Tema.GRIS, Tema.GRIS_CLARO);

        dispDisco.setColores(archivos + " archivo(s)", Tema.MORADO, Tema.MORADO_CLARO);
    }

    private void actualizarCPU(){
        CPU cpu = gestor == null ? null : gestor.getCpu();
        BCP actual = cpu == null ? null : cpu.getBcpActual();

        if (actual == null) {
            lblProcesoActual.setText("CPU libre");
            lblProcesoActual.setIcon(new Componentes.IconoPunto(Tema.GRIS, 10));
            distEstadoCPU.setColores("Ociosa", Tema.GRIS, Tema.GRIS_CLARO);
        } else {
            lblProcesoActual.setText("Proceso P" + actual.getIdProceso() + "  ·  "
                + actual.getArchivosAbiertos().get(0));
            lblProcesoActual.setIcon(new Componentes.IconoPunto(Tema.colorProceso(actual.getIdProceso()), 10));
            distEstadoCPU.setEstado(actual.getEstado());
        }

        if (cpu == null) {
            for (int i = 0; i < valoresRegistro.length; i++) valoresRegistro[i].setText(i == 1 ? "-" : "0");
            valoresRegistro[7].setText("-");
            valoresRegistro[6].setFont(Tema.MONO_GRANDE);
            lblInstruccion.setText("Instrucción en curso: -");
            barraInstruccion.setMaximum(1);
            barraInstruccion.setValue(0);
            return;
        }
        valoresRegistro[0].setText(String.valueOf(cpu.getPC()));
        valoresRegistro[1].setText(cpu.getIR() == null || cpu.getIR().isEmpty() ? "-" : cpu.getIR());
        valoresRegistro[2].setText(String.valueOf(cpu.getAC()));
        valoresRegistro[3].setText(String.valueOf(cpu.getAX()));
        valoresRegistro[4].setText(String.valueOf(cpu.getBX()));
        valoresRegistro[5].setText(String.valueOf(cpu.getCX()));
        if (cpu.getDXTexto() != null) {
            valoresRegistro[6].setText("\"" + cpu.getDXTexto() + "\"");    // nombre de archivo
            valoresRegistro[6].setFont(Tema.MONO_NORMAL);
        } else {
            valoresRegistro[6].setText(String.valueOf(cpu.getDX()));
            valoresRegistro[6].setFont(Tema.MONO_GRANDE);
        }
        valoresRegistro[7].setText(cpu.isFlagIgual() ? "Igual" : "Distinto");
        valoresRegistro[8].setText(String.format("%d (%02XH)", cpu.getAH(), cpu.getAH()));
        valoresRegistro[9].setText(String.valueOf(cpu.getAL()));

        int restantes = cpu.getSegundosRestantes();
        if (actual != null && restantes > 0) {
            String[] partes = cpu.getIR().trim().split("[,\\s]+");
            int peso = minipc.hardware.Pesos.de(partes[0], partes.length > 1 ? partes[1] : null);
            boolean desdeVirtual = actual.usaMemoriaVirtual()
                && cpu.getPC() - 1 - actual.getDireccionBase() >= actual.getEnRAM();
            lblInstruccion.setText("Ejecutando " + cpu.getIR() + "  ·  " + (peso - restantes) + " de " + peso + " s"
                + (desdeVirtual ? "  ·  desde memoria virtual" : ""));
            barraInstruccion.setMaximum(peso);
            barraInstruccion.setValue(peso - restantes);
        } else {
            lblInstruccion.setText(actual == null ? "Instrucción en curso: -" : "Lista para la siguiente instrucción");
            barraInstruccion.setMaximum(1);
            barraInstruccion.setValue(actual == null ? 0 : 1);
        }
    }

    private void actualizarBCP(){
        BCP b = (gestor == null) ? null : gestor.getCpu().getBcpActual();
        panelPila.setBCP(b);
        if (b == null) {
            for (JLabel l : valoresBCP) if (l != null) l.setText("-");
            distEstadoBCP.setColores("-", Tema.GRIS, Tema.GRIS_CLARO);
            return;
        }
        valoresBCP[0].setText("P" + b.getIdProceso());
        distEstadoBCP.setEstado(b.getEstado());
        valoresBCP[2].setText(String.valueOf(b.getDireccionBase()));
        valoresBCP[3].setText(b.usaMemoriaVirtual()
            ? b.getTamanioProceso() + " (" + b.getEnRAM() + " RAM + " + b.getEnVirtual() + " virtual)"
            : b.getTamanioProceso() + " posiciones");
        valoresBCP[4].setText(String.valueOf(b.getPrioridad()));
        valoresBCP[5].setText("CPU " + b.getCpuId());
        valoresBCP[6].setText(b.getTiempoInicio() >= 0 ? gestor.horaDe(b.getTiempoInicio()) : "-");
        valoresBCP[7].setText(b.getTiempoEmpleado() + " s");
        valoresBCP[8].setText(b.getDireccionBCP() >= 0 ? "Memoria[" + b.getDireccionBCP() + "]" : "-");
        valoresBCP[9].setText(String.join(", ", b.getArchivosAbiertos()));
    }

    private void actualizarMemoria(){
        mapaMemoria.setGestor(gestor);
        int total = gestor == null ? configuracion.getMemoriaPrincipal() : gestor.getMemoria().getTamanoTotal();
        tarjetaMemoria.setExtra(total + " posiciones");
        if (gestor == null) {
            modeloMemoria.setRowCount(0);
            duenoPosicion = new String[0];
            posicionPC = -1;
            return;
        }
        Memoria m = gestor.getMemoria();
        duenoPosicion = new String[total];
        for (int i = 0; i < total; i++) duenoPosicion[i] = i < m.getInicioUsuario() ? "S.O." : "";
        for (BCP b : gestor.getTodosLosProcesos()) {
            String e = b.getEstado();
            if ("Nuevo".equals(e) || "Finalizado".equals(e) || b.getDireccionBase() < 0) continue;
            for (int i = b.getDireccionBase(); i < b.getDireccionBase() + b.getEnRAM() && i < total; i++) {
                duenoPosicion[i] = "P" + b.getIdProceso();
            }
            if (b.getDireccionBCP() >= 0) duenoPosicion[b.getDireccionBCP()] = "BCP P" + b.getIdProceso();
        }

        if (modeloMemoria.getRowCount() != total) modeloMemoria.setRowCount(total);
        for (int i = 0; i < total; i++) {
            modeloMemoria.setValueAt(i, i, 0);
            modeloMemoria.setValueAt(m.leer(i), i, 1);
            modeloMemoria.setValueAt(duenoPosicion[i], i, 2);
        }

        BCP actual = gestor.getCpu().getBcpActual();
        int nuevoPC = (actual == null || gestor.getCpu().posicionVirtualPC() >= 0) ? -1 : gestor.getCpu().getPC();
        if (nuevoPC != posicionPC && nuevoPC >= 0 && nuevoPC < total) {
            tablaMemoria.scrollRectToVisible(tablaMemoria.getCellRect(Math.min(total - 1, nuevoPC + 3), 0, true));
            tablaMemoria.scrollRectToVisible(tablaMemoria.getCellRect(nuevoPC, 0, true));
        }
        posicionPC = nuevoPC;
        tablaMemoria.repaint();
    }

    private void actualizarDisco(){
        if (gestor == null) {
            modeloDisco.setRowCount(0);
            tarjetaDisco.setExtra(configuracion.getAlmacenamientoSecundario() + " posiciones");
            return;
        }
        Disco d = gestor.getDisco();
        int total = d.getTamanoTotal();
        tarjetaDisco.setExtra(total + " · " + d.virtualUsada() + "/" + d.getTamanoMemoriaVirtual() + " virtual");
        if (modeloDisco.getRowCount() != total) modeloDisco.setRowCount(total);
        int inicioVirtual = total - d.getTamanoMemoriaVirtual();
        String[] duenoVirtual = new String[total];
        for (BCP b : gestor.getTodosLosProcesos()) {
            if (!b.usaMemoriaVirtual() || "Finalizado".equals(b.getEstado())) continue;
            for (int i = b.getBaseVirtual(); i < b.getBaseVirtual() + b.getEnVirtual() && i < total; i++) {
                duenoVirtual[i] = "Virtual P" + b.getIdProceso();
            }
        }
        int nuevoPCVirtual = gestor.getCpu().posicionVirtualPC();
        for (int i = 0; i < total; i++) {
            String zona = i < d.getTamanoIndice() ? "Índice"
                : (i >= inicioVirtual ? (duenoVirtual[i] != null ? duenoVirtual[i] : "Virtual") : "Archivos");
            modeloDisco.setValueAt(i, i, 0);
            modeloDisco.setValueAt(d.leer(i), i, 1);
            modeloDisco.setValueAt(zona, i, 2);
        }
        if (nuevoPCVirtual >= 0 && nuevoPCVirtual != posicionPCVirtual) {
            tablaDisco.scrollRectToVisible(tablaDisco.getCellRect(Math.min(total - 1, nuevoPCVirtual + 3), 0, true));
            tablaDisco.scrollRectToVisible(tablaDisco.getCellRect(nuevoPCVirtual, 0, true));
        }
        posicionPCVirtual = nuevoPCVirtual;
        tablaDisco.repaint();
    }

    private void actualizarPantalla(){
        StyledDocument doc = areaPantalla.getStyledDocument();
        areaPantalla.setText("");
        boolean esperando = gestor != null && gestor.esperandoTeclado();

        if (gestor != null) {
            for (String linea : gestor.getCpu().getPantalla()) {
                SimpleAttributeSet a = new SimpleAttributeSet();
                StyleConstants.setFontFamily(a, Tema.MONO_NORMAL.getFamily());
                StyleConstants.setFontSize(a, 13);
                if (linea.startsWith("ERROR")) {
                    StyleConstants.setForeground(a, Tema.ROJO);
                    StyleConstants.setBold(a, true);
                } else if (linea.startsWith(">>")) {
                    StyleConstants.setForeground(a, Tema.AMBAR);
                    StyleConstants.setBold(a, true);
                } else if (linea.startsWith("[SO]")) {
                    StyleConstants.setForeground(a, Tema.TEXTO_SUAVE);
                    StyleConstants.setItalic(a, true);
                } else if (linea.startsWith("P")) {
                    StyleConstants.setForeground(a, Tema.VERDE_OSCURO);
                    StyleConstants.setBold(a, true);
                } else {
                    StyleConstants.setForeground(a, Tema.AZUL);   // valores ingresados por teclado
                }
                try {
                    doc.insertString(doc.getLength(), linea + "\n", a);
                } catch (javax.swing.text.BadLocationException ignorada) { }
            }
        }
        areaPantalla.setCaretPosition(doc.getLength());

        txtTeclado.setEnabled(esperando);
        btnEnviar.setEnabled(esperando);
        if (esperando) {
            BCP b = gestor.getProcesoEsperandoTeclado();
            lblTeclado.setText(">> P" + b.getIdProceso() + " valor (0-255):");
            lblTeclado.setForeground(Tema.AMBAR);
            txtTeclado.setBackground(Tema.AMBAR_CLARO);
            txtTeclado.requestFocusInWindow();
        } else {
            lblTeclado.setText(">> Ingresar valor:");
            lblTeclado.setForeground(Tema.TEXTO_SUAVE);
            txtTeclado.setBackground(Color.WHITE);
        }
    }

    private void actualizarBotones(){
        boolean corriendo = modoAutomatico;
        btnDetener.setEnabled(corriendo);
        btnEjecutar.setEnabled(!corriendo);
        btnPaso.setEnabled(!corriendo);
        btnCargar.setEnabled(!corriendo);
        btnConfigurar.setEnabled(!corriendo);
    }

 
    // ESTADÍSTICAS Y CONFIGURACIÓN
 

    private void mostrarEstadisticas(){
        if (gestor == null || gestor.getTodosLosProcesos().isEmpty()) {
            avisar("Todavía no hay procesos.");
            return;
        }
        JDialog dlg = new JDialog(this, "Estadísticas de ejecución", true);
        JPanel raiz = new JPanel(new BorderLayout(0, 12));
        raiz.setBackground(Tema.FONDO);
        raiz.setBorder(new EmptyBorder(16, 16, 16, 16));

        List<String[]> filas = gestor.getEstadisticas();
        int terminados = 0, sumaDuracion = 0, sumaCPU = 0;
        for (String[] f : filas) {
            if (!"-".equals(f[4])) { terminados++; sumaDuracion += Integer.parseInt(f[4]); }
            sumaCPU += Integer.parseInt(f[5]);
        }

        JPanel resumen = new JPanel(new GridLayout(1, 4, 12, 0));
        resumen.setOpaque(false);
        resumen.add(tarjetaDato("Procesos", String.valueOf(filas.size()), Tema.VERDE));
        resumen.add(tarjetaDato("Finalizados", String.valueOf(terminados), Tema.AZUL));
        resumen.add(tarjetaDato("Tiempo total", gestor.getReloj() + " s", Tema.MORADO));
        resumen.add(tarjetaDato("Duración promedio",
            terminados == 0 ? "-" : String.format("%.1f s", (double) sumaDuracion / terminados), Tema.AMBAR));
        raiz.add(resumen, BorderLayout.NORTH);

        DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Proceso", "Programa", "Inicio", "Final", "Duración (s)", "Tiempo CPU (s)", "Estado"}, 0) {
            @Override public boolean isCellEditable(int r, int c){ return false; }
        };
        for (String[] f : filas) modelo.addRow(f);
        JTable tabla = new JTable(modelo);
        Componentes.estilizarTabla(tabla);
        tabla.getColumnModel().getColumn(6).setCellRenderer(new RenderEstado());
        DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
        centro.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 2; i <= 5; i++) tabla.getColumnModel().getColumn(i).setCellRenderer(centro);

        Tarjeta t = new Tarjeta("Duración por proceso  (hora:minuto:segundo simulados)", Tema.VERDE);
        t.getContenido().add(Componentes.scroll(tabla), BorderLayout.CENTER);
        raiz.add(t, BorderLayout.CENTER);

        Boton cerrar = new Boton("Cerrar", Tema.VERDE);
        cerrar.addActionListener(e -> dlg.dispose());
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pie.setOpaque(false);
        pie.add(cerrar);
        raiz.add(pie, BorderLayout.SOUTH);

        dlg.setContentPane(raiz);
        dlg.setSize(860, 460);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private JPanel tarjetaDato(String titulo, String valor, Color color){
        Tarjeta t = new Tarjeta(titulo, color);
        JLabel v = new JLabel(valor);
        v.setFont(new Font(Tema.TITULO.getFamily(), Font.BOLD, 24));
        v.setForeground(color);
        t.getContenido().add(v, BorderLayout.CENTER);
        return t;
    }

    private void mostrarConfiguracion(){
        JDialog dlg = new JDialog(this, "Configuración del Mini PC", true);
        Tarjeta t = new Tarjeta("Tamaños (se guardan en " + ConfiguracionSistema.ARCHIVO_POR_DEFECTO + ")", Tema.VERDE);
        JPanel campos = new JPanel(new GridLayout(4, 2, 10, 10));
        campos.setOpaque(false);
        JTextField mp = campo(configuracion.getMemoriaPrincipal());
        JTextField ms = campo(configuracion.getMemoriaSistema());
        JTextField as = campo(configuracion.getAlmacenamientoSecundario());
        JTextField mv = campo(configuracion.getMemoriaVirtual());
        // La zona del S.O. y la memoria virtual se calculan solas (no se editan)
        for (JTextField calc : new JTextField[]{ms, mv}) {
            calc.setEditable(false);
            calc.setBackground(Tema.GRIS_CLARO);
            calc.setForeground(Tema.TEXTO_SUAVE);
        }
        Runnable recalcular = () -> {
            try { ms.setText(String.valueOf(ConfiguracionSistema.calcularZonaSO(Integer.parseInt(mp.getText().trim())))); }
            catch (NumberFormatException ex) { ms.setText("-"); }
            try { mv.setText(String.valueOf(ConfiguracionSistema.calcularMemoriaVirtual(Integer.parseInt(as.getText().trim())))); }
            catch (NumberFormatException ex) { mv.setText("-"); }
        };
        javax.swing.event.DocumentListener alCambiar = new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e){ recalcular.run(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e){ recalcular.run(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e){ recalcular.run(); }
        };
        mp.getDocument().addDocumentListener(alCambiar);
        as.getDocument().addDocumentListener(alCambiar);
        campos.add(etiqueta("Memoria principal"));               campos.add(mp);
        campos.add(etiqueta("Zona del S.O. (25 %)"));            campos.add(ms);
        campos.add(etiqueta("Almacenamiento secundario"));       campos.add(as);
        campos.add(etiqueta("Memoria virtual (12,5 % disco)"));  campos.add(mv);
        t.getContenido().add(campos, BorderLayout.CENTER);

        Boton cargar = new Boton("Cargar archivo...", Tema.GRIS);
        Boton aplicar = new Boton("Guardar y aplicar", Tema.VERDE);
        Boton cancelar = new Boton("Cancelar", new Color(0x94A3B8));

        cargar.addActionListener(e -> {
            JFileChooser sel = new JFileChooser(new File("."));
            if (sel.showOpenDialog(dlg) == JFileChooser.APPROVE_OPTION) {
                try {
                    configuracion.cargarDesdeArchivo(sel.getSelectedFile());
                    mp.setText(String.valueOf(configuracion.getMemoriaPrincipal()));
                    ms.setText(String.valueOf(configuracion.getMemoriaSistema()));
                    as.setText(String.valueOf(configuracion.getAlmacenamientoSecundario()));
                    mv.setText(String.valueOf(configuracion.getMemoriaVirtual()));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        aplicar.addActionListener(e -> {
            try {
                configuracion.setTamanos(
                    Integer.parseInt(mp.getText().trim()), Integer.parseInt(as.getText().trim()));
                configuracion.guardarEnArchivo(new File(ConfiguracionSistema.ARCHIVO_POR_DEFECTO));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "La memoria principal y el disco deben ser números enteros.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            boolean habiaProcesos = gestor != null;
            gestor = null;
            dlg.dispose();
            actualizarVista();
            if (habiaProcesos) {
                JOptionPane.showMessageDialog(this, "Configuración aplicada. Se reinició el sistema: volvé a cargar los programas.");
            }
        });
        cancelar.addActionListener(e -> dlg.dispose());

        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pie.setOpaque(false);
        pie.add(cargar);
        pie.add(cancelar);
        pie.add(aplicar);

        JPanel raiz = new JPanel(new BorderLayout(0, 12));
        raiz.setBackground(Tema.FONDO);
        raiz.setBorder(new EmptyBorder(16, 16, 16, 16));
        raiz.add(t, BorderLayout.CENTER);
        raiz.add(pie, BorderLayout.SOUTH);
        dlg.setContentPane(raiz);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private JTextField campo(int valor){
        JTextField f = new JTextField(String.valueOf(valor), 8);
        f.setFont(Tema.MONO_NORMAL);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Tema.BORDE), new EmptyBorder(5, 8, 5, 8)));
        return f;
    }

    private JLabel etiqueta(String t){
        JLabel l = new JLabel(t);
        l.setFont(Tema.NORMAL);
        l.setForeground(Tema.TEXTO);
        return l;
    }

 
    // RENDERIZADORES Y PILA
   

    /** Columna "#" de la cola: punto del color del proceso + "P1". */
    private static class RenderProceso extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c){
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, "P" + v, s, f, r, c);
            l.setFont(Tema.NEGRITA);
            l.setIcon(new Componentes.IconoPunto(Tema.colorProceso((Integer) v), 9));
            l.setIconTextGap(6);
            l.setBackground(s ? Tema.VERDE_CLARO : (r % 2 == 0 ? Color.WHITE : Tema.FILA_ALTERNA));
            return l;
        }
    }

    /** Tabla de memoria: cada fila se tiñe con el color de su dueño; el PC se resalta. */
    private class RenderMemoria extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c){
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
            String dueno = r < duenoPosicion.length ? duenoPosicion[r] : "";
            Color fondo = Color.WHITE;
            Color texto = Tema.TEXTO;
            if (dueno.startsWith("BCP")) {
                fondo = Tema.aclarar(Tema.NARANJA_SO, 0.6f);
                texto = Tema.NARANJA_SO.darker().darker();
            } else if ("S.O.".equals(dueno)) {
                fondo = Tema.aclarar(Tema.NARANJA_SO, 0.88f);
                texto = Tema.TEXTO_SUAVE;
            } else if (dueno.startsWith("P")) {
                int id = Integer.parseInt(dueno.substring(1));
                fondo = Tema.aclarar(Tema.colorProceso(id), 0.82f);
            }
            boolean esPC = r == posicionPC;
            if (esPC) {
                fondo = Tema.ROJO_CLARO;
                texto = Tema.ROJO;
            }
            l.setBackground(s ? Tema.VERDE_CLARO : fondo);
            l.setForeground(texto);
            l.setFont(c == 1 ? Tema.MONO_NORMAL : (esPC ? Tema.NEGRITA : Tema.NORMAL));
            l.setBorder(new EmptyBorder(0, 8, 0, 8));
            if (c == 0 && esPC) l.setText("▶ " + v);
            return l;
        }
    }

    /** Tabla de disco: índice en verde, memoria virtual en morado. */
    private class RenderDisco extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c){
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
            String zona = String.valueOf(t.getModel().getValueAt(r, 2));
            String valor = String.valueOf(t.getModel().getValueAt(r, 1));
            Color fondo = Color.WHITE;
            if ("Índice".equals(zona)) fondo = valor.isEmpty() ? Tema.aclarar(Tema.VERDE, 0.92f) : Tema.VERDE_CLARO;
            else if (zona.startsWith("Virtual P")) {
                int id = Integer.parseInt(zona.substring("Virtual P".length()));
                fondo = Tema.aclarar(Tema.colorProceso(id), 0.7f);    // parte del proceso en memoria virtual
            }
            else if ("Virtual".equals(zona)) fondo = Tema.aclarar(Tema.MORADO, 0.92f);
            else if (!valor.isEmpty()) fondo = Tema.aclarar(Tema.MORADO, 0.8f);
            boolean esPC = r == posicionPCVirtual;
            Color texto = c == 2 ? Tema.TEXTO_SUAVE : Tema.TEXTO;
            if (esPC) { fondo = Tema.ROJO_CLARO; texto = Tema.ROJO; }
            l.setBackground(s ? Tema.VERDE_CLARO : fondo);
            l.setForeground(texto);
            l.setFont(c == 1 ? Tema.MONO_NORMAL : (esPC ? Tema.PEQUENA_B : Tema.PEQUENA));
            l.setBorder(new EmptyBorder(0, 8, 0, 8));
            if (c == 0 && esPC) l.setText("▶ " + v);
            return l;
        }
    }

    /** Dibuja las 5 posiciones de la pila del proceso actual con la flecha del tope (SP). */
    private static class PanelPila extends JComponent {
        private BCP bcp;

        PanelPila(){ setPreferredSize(new Dimension(200, 140)); setMinimumSize(new Dimension(200, 120)); }

        void setBCP(BCP b){ this.bcp = b; repaint(); }

        @Override
        protected void paintComponent(Graphics g){
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int filas = 5;
            int gap = 4;
            int alto = Math.max(16, (getHeight() - gap * (filas - 1)) / filas);
            int xCaja = 60, wCaja = getWidth() - xCaja - 10;
            int tope = bcp == null ? -1 : bcp.getTopePila();
            String[] valores = new String[filas];
            if (bcp != null) {
                String p = bcp.pilaTexto();          // "[1,2,3]"
                String interior = p.substring(1, p.length() - 1);
                String[] partes = interior.isEmpty() ? new String[0] : interior.split(",");
                for (int i = 0; i < partes.length && i < filas; i++) valores[i] = partes[i];
            }
            for (int i = filas - 1, fila = 0; i >= 0; i--, fila++) {
                int y = fila * (alto + gap);
                boolean llena = i <= tope;
                g2.setColor(llena ? Tema.AMBAR_CLARO : new Color(0xF8FAF9));
                g2.fillRoundRect(xCaja, y, wCaja, alto, 10, 10);
                g2.setColor(llena ? Tema.AMBAR : Tema.BORDE);
                g2.drawRoundRect(xCaja, y, wCaja - 1, alto - 1, 10, 10);

                g2.setFont(Tema.PEQUENA);
                g2.setColor(Tema.TEXTO_SUAVE);
                g2.drawString(String.valueOf(i), xCaja - 14, y + alto / 2 + 4);

                g2.setFont(Tema.MONO_NORMAL);
                g2.setColor(llena ? Tema.TEXTO : new Color(0xB6C2BB));
                String texto = llena ? valores[i] : "vacío";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(texto, xCaja + (wCaja - fm.stringWidth(texto)) / 2, y + (alto + fm.getAscent()) / 2 - 2);

                if (i == tope) {
                    g2.setFont(Tema.PEQUENA_B);
                    g2.setColor(Tema.AMBAR);
                    g2.drawString("SP", 2, y + alto / 2 + 4);
                    int ax = 22, ay = y + alto / 2;
                    g2.fillPolygon(new int[]{ax + 18, ax + 10, ax + 10}, new int[]{ay, ay - 5, ay + 5}, 3);
                    g2.drawLine(ax, ay, ax + 10, ay);
                }
            }
            if (tope < 0) {
                g2.setFont(Tema.PEQUENA_B);
                g2.setColor(Tema.TEXTO_SUAVE);
                g2.drawString("SP", 2, (filas - 1) * (alto + gap) + alto + 0);
            }
            g2.dispose();
        }
    }


    // CLASES DE APOYO tema, componentes, mapa de memoria
 

    /**
     * Tema: paleta de colores y fuentes de la interfaz. Fondo blanco con acentos
     * verdes; cada estado de proceso y cada proceso en memoria tiene su color.
     */
    public static final class Tema {

        private Tema() {}

        // ---- Base ----
        public static final Color FONDO = new Color(0xF3F6F4);
        public static final Color TARJETA = Color.WHITE;
        public static final Color BORDE = new Color(0xDDE5E0);
        public static final Color TEXTO = new Color(0x1F2A24);
        public static final Color TEXTO_SUAVE = new Color(0x6B7A72);
        public static final Color FILA_ALTERNA = new Color(0xF7FAF8);

        // ---- Acento verde ----
        public static final Color VERDE = new Color(0x16A34A);
        public static final Color VERDE_OSCURO = new Color(0x15803D);
        public static final Color VERDE_CLARO = new Color(0xDCFCE7);
        public static final Color VERDE_MEDIO = new Color(0x86EFAC);

        // ---- Colores de apoyo ----
        public static final Color AZUL = new Color(0x2563EB);
        public static final Color AZUL_CLARO = new Color(0xDBEAFE);
        public static final Color AMBAR = new Color(0xD97706);
        public static final Color AMBAR_CLARO = new Color(0xFEF3C7);
        public static final Color ROJO = new Color(0xDC2626);
        public static final Color ROJO_CLARO = new Color(0xFEE2E2);
        public static final Color MORADO = new Color(0x7C3AED);
        public static final Color MORADO_CLARO = new Color(0xEDE9FE);
        public static final Color GRIS = new Color(0x64748B);
        public static final Color GRIS_CLARO = new Color(0xF1F5F9);
        public static final Color NARANJA_SO = new Color(0xF59E0B);

        // Un color por proceso (se repite si hay más de 8)
        private static final List<Color> COLORES_PROCESO = Arrays.asList(
            new Color(0x22C55E), new Color(0x3B82F6), new Color(0xA855F7), new Color(0x14B8A6),
            new Color(0xEC4899), new Color(0xF97316), new Color(0x6366F1), new Color(0x84CC16)
        );

        // ---- Fuentes ----
        private static final String SANS = elegirFuente("Segoe UI", "Inter", "Helvetica Neue", "SansSerif");
        private static final String MONO = elegirFuente("JetBrains Mono", "Consolas", "Menlo", "Monospaced");

        public static final Font TITULO = new Font(SANS, Font.BOLD, 20);
        public static final Font SUBTITULO = new Font(SANS, Font.PLAIN, 12);
        public static final Font TITULO_CARD = new Font(SANS, Font.BOLD, 14);
        public static final Font NORMAL = new Font(SANS, Font.PLAIN, 13);
        public static final Font NEGRITA = new Font(SANS, Font.BOLD, 13);
        public static final Font PEQUENA = new Font(SANS, Font.PLAIN, 11);
        public static final Font PEQUENA_B = new Font(SANS, Font.BOLD, 11);
        public static final Font MONO_NORMAL = new Font(MONO, Font.PLAIN, 13);
        public static final Font MONO_GRANDE = new Font(MONO, Font.BOLD, 18);

        private static String elegirFuente(String... opciones){
            List<String> disponibles = Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
            for (String f : opciones) {
                if (disponibles.contains(f)) return f;
            }
            return opciones[opciones.length - 1];
        }

        // E: idProceso (int)
        // S: Color - color fijo que identifica a ese proceso en la memoria y las tablas
        // R: ninguna
        public static Color colorProceso(int idProceso){
            return COLORES_PROCESO.get(Math.floorMod(idProceso - 1, COLORES_PROCESO.size()));
        }

        // E: estado (String) - estado del proceso
        // S: Color[] - {color del texto/borde, color de fondo} para el distintivo del estado
        // R: ninguna
        public static Color[] coloresEstado(String estado){
            if (estado == null) return new Color[]{GRIS, GRIS_CLARO};
            switch (estado) {
                case "Nuevo":return new Color[]{GRIS, GRIS_CLARO};
                case "Preparado":return new Color[]{AZUL, AZUL_CLARO};
                case "Ejecución":return new Color[]{VERDE_OSCURO, VERDE_CLARO};
                case "En espera":return new Color[]{AMBAR, AMBAR_CLARO};
                case "Suspendido en espera": return new Color[]{MORADO, MORADO_CLARO};
                case "Suspendido preparado": return new Color[]{new Color(0x9333EA), new Color(0xF3E8FF)};
                case "Finalizado":return new Color[]{new Color(0x475569), new Color(0xE2E8F0)};
                default:return new Color[]{GRIS, GRIS_CLARO};
            }
        }

        // E: c (Color), factor (float) - 0..1, cuánto aclarar hacia blanco
        // S: Color - versión más clara del color
        // R: ninguna
        public static Color aclarar(Color c, float factor){
            int r = (int) (c.getRed() + (255 - c.getRed()) * factor);
            int g = (int) (c.getGreen() + (255 - c.getGreen()) * factor);
            int b = (int) (c.getBlue() + (255 - c.getBlue()) * factor);
            return new Color(r, g, b);
        }
    }

    /**
     * Componentes: piezas visuales reutilizables de la interfaz (tarjetas con
     * esquinas redondeadas, botones planos, distintivos de estado, barras de uso
     * y estilo de tablas).
     */
    public static final class Componentes {

        private Componentes() {}

        // ======================= Tarjeta =======================

        /** Panel blanco con esquinas redondeadas, franja de color y título. */
        public static class Tarjeta extends JPanel {
            private final JPanel contenido;
            private final JLabel lblExtra;

            // E: titulo (String), acento (Color) - color de la franja y del punto del título
            // S: no aplica (constructor)
            // R: el contenido se agrega con getContenido()
            public Tarjeta(String titulo, Color acento){
                super(new BorderLayout(0, 8));
                setOpaque(false);
                setBorder(new BordeRedondeado(acento));

                JPanel encabezado = new JPanel(new BorderLayout());
                encabezado.setOpaque(false);
                JLabel lbl = new JLabel(titulo);
                lbl.setFont(Tema.TITULO_CARD);
                lbl.setForeground(Tema.TEXTO);
                lbl.setIcon(new IconoPunto(acento, 10));
                lbl.setIconTextGap(8);
                encabezado.add(lbl, BorderLayout.WEST);
                lblExtra = new JLabel();
                lblExtra.setFont(Tema.PEQUENA_B);
                lblExtra.setForeground(Tema.TEXTO_SUAVE);
                encabezado.add(lblExtra, BorderLayout.EAST);
                add(encabezado, BorderLayout.NORTH);

                contenido = new JPanel(new BorderLayout());
                contenido.setOpaque(false);
                add(contenido, BorderLayout.CENTER);
            }

            public JPanel getContenido(){ return contenido; }

            // Texto pequeño a la derecha del título (p. ej. "3 / 5")
            public void setExtra(String texto){ lblExtra.setText(texto); }

            @Override
            protected void paintComponent(Graphics g){
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Tema.TARJETA);
                g2.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 16, 16));
                g2.dispose();
                super.paintComponent(g);
            }
        }

        /** Borde redondeado con una franja de color arriba. */
        public static class BordeRedondeado extends AbstractBorder {
            private final Color acento;
            public BordeRedondeado(Color acento){ this.acento = acento; }

            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int w, int h){
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Shape forma = new RoundRectangle2D.Float(x + 1, y + 1, w - 3, h - 3, 16, 16);
                g2.setClip(forma);
                g2.setColor(acento);
                g2.fillRect(x, y, w, 4);
                g2.setClip(null);
                g2.setColor(Tema.BORDE);
                g2.draw(forma);
                g2.dispose();
            }

            @Override
            public Insets getBorderInsets(Component c){ return new Insets(14, 14, 12, 14); }
        }

        /** Ícono circular de color (para títulos y leyendas). */
        public static class IconoPunto implements Icon {
            private final Color color;
            private final int tam;
            public IconoPunto(Color color, int tam){ this.color = color; this.tam = tam; }
            @Override public void paintIcon(Component c, Graphics g, int x, int y){
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(x, y, tam, tam);
                g2.dispose();
            }
            @Override public int getIconWidth(){ return tam; }
            @Override public int getIconHeight(){ return tam; }
        }

        // ======================= Botón =======================

        /** Botón plano redondeado con color propio y efecto al pasar el mouse. */
        public static class Boton extends JButton {
            private Color base;
            private boolean encima = false;

            public Boton(String texto, Color base){
                super(texto);
                this.base = base;
                setFont(Tema.NEGRITA);
                setForeground(Color.WHITE);
                setContentAreaFilled(false);
                setFocusPainted(false);
                setBorderPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setBorder(new EmptyBorder(9, 16, 9, 16));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e){ encima = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e){ encima = false; repaint(); }
                });
            }

            public void setColorBase(Color c){ this.base = c; repaint(); }

            @Override
            protected void paintComponent(Graphics g){
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = !isEnabled() ? new Color(0xCBD5E1) : (encima ? base.darker() : base);
                g2.setColor(c);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        }

        // ======================= Distintivo (badge) =======================

        /** Etiqueta con forma de píldora y colores según el estado. */
        public static class Distintivo extends JLabel {
            private Color fondo = Tema.GRIS_CLARO;

            public Distintivo(){
                setFont(Tema.PEQUENA_B);
                setHorizontalAlignment(CENTER);
                setBorder(new EmptyBorder(3, 10, 3, 10));
            }

            // E: estado (String) - texto a mostrar; se colorea con Tema.coloresEstado
            // S: no aplica (void)
            // R: ninguna
            public void setEstado(String estado){
                Color[] c = Tema.coloresEstado(estado);
                setText(estado == null ? "-" : estado);
                setForeground(c[0]);
                fondo = c[1];
                repaint();
            }

            public void setColores(String texto, Color frente, Color fondo){
                setText(texto);
                setForeground(frente);
                this.fondo = fondo;
                repaint();
            }

            @Override
            protected void paintComponent(Graphics g){
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                FontMetrics fm = g2.getFontMetrics(getFont());
                int w = Math.min(getWidth() - 2, fm.stringWidth(getText()) + 20);
                int h = Math.min(getHeight() - 2, fm.getHeight() + 6);
                int x;
                if (getHorizontalAlignment() == LEFT) x = 0;
                else if (getHorizontalAlignment() == RIGHT) x = getWidth() - w - 1;
                else x = (getWidth() - w) / 2;
                int y = (getHeight() - h) / 2;
                g2.setColor(fondo);
                g2.fillRoundRect(x, y, w, h, h, h);
                g2.dispose();
                super.paintComponent(g);
            }
        }

        /** Renderizador de celdas que muestra el estado como distintivo de color. */
        public static class RenderEstado extends DefaultTableCellRenderer {
            private final Distintivo d = new Distintivo();
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int fila, int col){
                d.setEstado(v == null ? "-" : v.toString());
                d.setOpaque(true);
                d.setBackground(sel ? Tema.VERDE_CLARO : (fila % 2 == 0 ? Color.WHITE : Tema.FILA_ALTERNA));
                return d;
            }
        }

        // ======================= Barra de uso =======================

        /** Barra de progreso redondeada con etiqueta y valor. */
        public static class BarraUso extends JPanel {
            private final JLabel lblValor;
            private final Color color;
            private double fraccion = 0;

            public BarraUso(String nombre, Color color){
                super(new BorderLayout(0, 4));
                this.color = color;
                setOpaque(false);
                JPanel arriba = new JPanel(new BorderLayout());
                arriba.setOpaque(false);
                JLabel lbl = new JLabel(nombre);
                lbl.setFont(Tema.NORMAL);
                lbl.setForeground(Tema.TEXTO);
                lbl.setIcon(new IconoPunto(color, 8));
                lbl.setIconTextGap(6);
                lblValor = new JLabel("-");
                lblValor.setFont(Tema.PEQUENA_B);
                lblValor.setForeground(Tema.TEXTO_SUAVE);
                arriba.add(lbl, BorderLayout.WEST);
                arriba.add(lblValor, BorderLayout.EAST);
                add(arriba, BorderLayout.NORTH);

                JComponent barra = new JComponent() {
                    @Override protected void paintComponent(Graphics g){
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        int h = getHeight();
                        g2.setColor(Tema.aclarar(color, 0.85f));
                        g2.fillRoundRect(0, 0, getWidth(), h, h, h);
                        int w = (int) Math.round(getWidth() * Math.max(0, Math.min(1, fraccion)));
                        if (w > 0) {
                            g2.setColor(color);
                            g2.fillRoundRect(0, 0, Math.max(w, h), h, h, h);
                        }
                        g2.dispose();
                    }
                };
                barra.setPreferredSize(new Dimension(10, 8));
                add(barra, BorderLayout.CENTER);
            }

            // E: usado, total (int)
            // S: no aplica (void)
            // R: total > 0
            public void setValor(int usado, int total){
                fraccion = total <= 0 ? 0 : (double) usado / total;
                lblValor.setText(usado + " / " + total);
                repaint();
            }
        }

        // ======================= Tablas =======================

        // E: tabla (JTable)
        // S: no aplica (void)
        // R: aplica el estilo del tema: filas altas, sin rejilla vertical, encabezado verde claro
        public static void estilizarTabla(JTable tabla){
            tabla.setFont(Tema.NORMAL);
            tabla.setRowHeight(26);
            tabla.setShowVerticalLines(false);
            tabla.setGridColor(new Color(0xEEF2EF));
            tabla.setSelectionBackground(Tema.VERDE_CLARO);
            tabla.setSelectionForeground(Tema.TEXTO);
            tabla.setFillsViewportHeight(true);
            tabla.setIntercellSpacing(new Dimension(0, 1));
            tabla.setDefaultEditor(Object.class, null);
            JTableHeader h = tabla.getTableHeader();
            h.setReorderingAllowed(false);
            h.setDefaultRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c){
                    JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                    l.setFont(Tema.PEQUENA_B);
                    l.setForeground(Tema.VERDE_OSCURO);
                    l.setBackground(Tema.VERDE_CLARO);
                    l.setBorder(new EmptyBorder(6, 8, 6, 8));
                    return l;
                }
            });
        }

        // E: tabla (JTable)
        // S: JScrollPane - la tabla dentro de un scroll sin borde
        // R: ninguna
        public static JScrollPane scroll(JComponent c){
            JScrollPane sp = new JScrollPane(c);
            sp.setBorder(BorderFactory.createLineBorder(Tema.BORDE));
            sp.getViewport().setBackground(Color.WHITE);
            sp.getVerticalScrollBar().setUnitIncrement(16);
            sp.setPreferredSize(new Dimension(200, 120));   // deja que el layout decida el alto
            return sp;
        }
    }

    /**
     * MapaMemoria: dibuja la memoria principal como una barra vertical dividida
     * en zonas: S.O. (con los BCP), un bloque de color por proceso cargado y el
     * espacio libre. Marca con una flecha la posición del PC.
     */
    public static class MapaMemoria extends JComponent {

        private GestorProcesos gestor;

        public MapaMemoria(){
            setPreferredSize(new Dimension(260, 300));
            setToolTipText("");
        }

        public void setGestor(GestorProcesos gestor){
            this.gestor = gestor;
            repaint();
        }

        // Procesos que tienen memoria asignada en este momento
        private List<BCP> procesosEnMemoria(){
            List<BCP> lista = new ArrayList<>();
            if (gestor == null) return lista;
            for (BCP b : gestor.getTodosLosProcesos()) {
                String e = b.getEstado();
                if (!"Nuevo".equals(e) && !"Finalizado".equals(e) && b.getDireccionBase() >= 0) {
                    lista.add(b);
                }
            }
            return lista;
        }

        @Override
        protected void paintComponent(Graphics g){
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int margenIzq = 44, margenDer = 18, arriba = 4, abajo = 4;
            int x = margenIzq;
            int w = getWidth() - margenIzq - margenDer;
            int h = getHeight() - arriba - abajo;

            if (gestor == null) {
                g2.setColor(Tema.GRIS_CLARO);
                g2.fillRoundRect(x, arriba, w, h, 12, 12);
                g2.setColor(Tema.TEXTO_SUAVE);
                g2.setFont(Tema.NORMAL);
                dibujarCentrado(g2, "Cargá programas para ver la memoria", x, arriba, w, h);
                g2.dispose();
                return;
            }

            int total = gestor.getMemoria().getTamanoTotal();
            int inicioUsuario = gestor.getMemoria().getInicioUsuario();
            double escala = (double) h / total;

            // Fondo: espacio libre con rayas suaves
            g2.setColor(new Color(0xF8FAF9));
            g2.fillRoundRect(x, arriba, w, h, 12, 12);
            g2.setClip(x, arriba, w, h);
            g2.setColor(new Color(0xEAF0EC));
            for (int i = -h; i < w + h; i += 10) {
                g2.drawLine(x + i, arriba + h, x + i + h, arriba);
            }
            g2.setClip(null);

            // Zona del S.O.
            int ySO = arriba;
            int hSO = Math.max(14, (int) Math.round(inicioUsuario * escala));
            g2.setColor(Tema.aclarar(Tema.NARANJA_SO, 0.55f));
            g2.fillRoundRect(x, ySO, w, hSO, 10, 10);
            g2.setColor(Tema.NARANJA_SO.darker());
            g2.setFont(Tema.PEQUENA_B);
            int bcps = 0;
            for (BCP b : procesosEnMemoria()) if (b.getDireccionBCP() >= 0) bcps++;
            dibujarCentrado(g2, "S.O. · " + bcps + " BCP", x, ySO, w, hSO);
            etiquetaDireccion(g2, 0, ySO);

            // Bloques de procesos (ordenados por base; si son muy pequeños se separan
            // un poco para que las etiquetas no se encimen)
            BCP enCpu = gestor.getCpu().getBcpActual();
            List<BCP> bloques = procesosEnMemoria();
            bloques.sort((a, b) -> Integer.compare(a.getDireccionBase(), b.getDireccionBase()));
            int finAnterior = ySO + hSO + 2;
            int ultimaEtiqueta = ySO;
            int yPCBloque = -1, altoPCBloque = 0;
            for (BCP b : bloques) {
                int y = Math.max(arriba + (int) Math.round(b.getDireccionBase() * escala), finAnterior);
                int alto = Math.max(18, (int) Math.round(b.getEnRAM() * escala));
                Color c = Tema.colorProceso(b.getIdProceso());
                g2.setColor(c);
                g2.fillRoundRect(x, y, w, alto, 10, 10);
                if (b == enCpu) {
                    g2.setColor(Tema.TEXTO);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(x + 1, y + 1, w - 3, alto - 3, 10, 10);
                    g2.setStroke(new BasicStroke(1f));
                    yPCBloque = y;
                    altoPCBloque = alto;
                }
                g2.setColor(Color.WHITE);
                g2.setFont(Tema.PEQUENA_B);
                String texto = "P" + b.getIdProceso() + "  Base " + b.getDireccionBase()
                    + " · Alcance " + b.getTamanioProceso()
                    + (b.usaMemoriaVirtual() ? " (+" + b.getEnVirtual() + " virtual)" : "");
                if (g2.getFontMetrics().stringWidth(texto) > w - 8) texto = "P" + b.getIdProceso();
                dibujarCentrado(g2, texto, x, y, w, alto);
                if (y - ultimaEtiqueta >= 13) {
                    etiquetaDireccion(g2, b.getDireccionBase(), y);
                    ultimaEtiqueta = y;
                }
                finAnterior = y + alto + 2;
            }

            // Flecha del PC
            if (enCpu != null && yPCBloque >= 0 && gestor.getCpu().posicionVirtualPC() < 0) {
                int pc = gestor.getCpu().getPC();
                double rel = (pc - enCpu.getDireccionBase() + 0.5) / Math.max(1, enCpu.getEnRAM());
                int yPC = yPCBloque + (int) Math.round(Math.max(0, Math.min(1, rel)) * altoPCBloque);
                int xf = x + w + 2;
                g2.setColor(Tema.ROJO);
                g2.fillPolygon(new int[]{xf, xf + 10, xf + 10}, new int[]{yPC, yPC - 6, yPC + 6}, 3);
            }

            // Aviso cuando el PC está en la parte del programa que vive en memoria virtual
            int pcVirtual = gestor.getCpu().posicionVirtualPC();
            if (pcVirtual >= 0) {
                String aviso = "PC en memoria virtual · disco [" + pcVirtual + "]";
                g2.setFont(Tema.PEQUENA_B);
                FontMetrics fm = g2.getFontMetrics();
                int aw = fm.stringWidth(aviso) + 16, ah = fm.getHeight() + 6;
                int ax = x + (w - aw) / 2, ay = arriba + h - ah - 6;
                g2.setColor(Tema.ROJO_CLARO);
                g2.fillRoundRect(ax, ay, aw, ah, ah, ah);
                g2.setColor(Tema.ROJO);
                g2.drawString(aviso, ax + 8, ay + (ah + fm.getAscent()) / 2 - 2);
            }

            etiquetaDireccion(g2, total - 1, arriba + h - 10);
            g2.dispose();
        }

        private void etiquetaDireccion(Graphics2D g2, int dir, int y){
            g2.setFont(Tema.PEQUENA);
            g2.setColor(Tema.TEXTO_SUAVE);
            String s = String.format("%04d", dir);
            g2.drawString(s, 4, y + 11);
        }

        private void dibujarCentrado(Graphics2D g2, String s, int x, int y, int w, int h){
            FontMetrics fm = g2.getFontMetrics();
            if (h < fm.getHeight() - 4) return;
            int tx = x + (w - fm.stringWidth(s)) / 2;
            int ty = y + (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(s, tx, ty);
        }

        @Override
        public String getToolTipText(java.awt.event.MouseEvent e){
            if (gestor == null) return null;
            int total = gestor.getMemoria().getTamanoTotal();
            int h = getHeight() - 8;
            int pos = (int) ((e.getY() - 4) / ((double) h / total));
            if (pos < 0 || pos >= total) return null;
            return "Posición " + pos + ": " + gestor.getMemoria().leer(pos);
        }
    }
}
