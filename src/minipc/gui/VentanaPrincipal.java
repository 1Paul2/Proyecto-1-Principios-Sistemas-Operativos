/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package minipc.gui;

/**
 *
 * @author Poll Anthony Garro Vargas - 2024129001
 * @Universidad: Instituto Tecnológico de Costa Rica

 */

import minipc.hardware.CPU;
import minipc.hardware.Memoria;
import minipc.hardware.Disco;
import minipc.modelo.BCP;
import minipc.modelo.Instruccion;
import minipc.util.ParserASM;
import minipc.util.ConfiguracionSistema;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class VentanaPrincipal extends JFrame {

    private CPU cpu;
    private Memoria memoria;
    private Disco disco;
    private BCP bcp;
    private List<Instruccion> instruccionesActuales;
    private long tiempoInicio;
    private ConfiguracionSistema configuracion;

    // ---- Componentes ----
    private JButton btnCargarArchivos;
    private JButton btnEjecutar;
    private JButton btnPasoAPaso;
    private JButton btnLimpiar;
    private JButton btnEstadisticas;
    private JButton btnConfigurar;

    private JTable tablaProcesos;

    private JLabel lblIdBCP;
    private JLabel lblEstadoBCP;
    private JLabel lblPC;
    private JLabel lblIR;
    private JLabel lblAC;
    private JLabel lblAX;
    private JLabel lblBX;
    private JLabel lblCX;
    private JLabel lblDX;

    private JTable tablaMemoria;
    private JTable tablaDisco;

    private JTextArea areaPantalla;
    private JTextField txtEntradaTeclado;
    private JButton btnEnviarEntrada;

    public VentanaPrincipal() {
        super("Proyecto 1 de SO");
        configuracion = new ConfiguracionSistema();
        initComponents();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1000, 620));
        pack();
        setLocationRelativeTo(null);
    }

    // E: no aplica
    // S: no aplica (void)
    // R: arma todos los paneles, tablas y botones de la ventana
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // ---- Barra superior de botones ----
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnCargarArchivos = new JButton("Cargar archivos \uD83D\uDCC1");
        btnEjecutar = new JButton("Ejecutar");
        btnPasoAPaso = new JButton("Paso a paso");
        btnLimpiar = new JButton("Limpiar");
        btnEstadisticas = new JButton("Estadísticas");
        btnConfigurar = new JButton("Configurar memoria/disco");

        panelBotones.add(btnCargarArchivos);
        panelBotones.add(btnEjecutar);
        panelBotones.add(btnPasoAPaso);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnEstadisticas);
        panelBotones.add(btnConfigurar);
        add(panelBotones, BorderLayout.NORTH);

        // ---- Panel central: 3 columnas (Procesos | BCP | Memoria+Disco+Pantalla) ----
        JPanel panelCentral = new JPanel(new GridLayout(1, 3, 10, 10));

        // Columna 1: tabla de Procesos (Lista de Trabajos)
        DefaultTableModel modeloProcesos = new DefaultTableModel(new Object[0][2], new String[]{"Procesos", "Estados"});
        tablaProcesos = new JTable(modeloProcesos);
        JScrollPane scrollProcesos = new JScrollPane(tablaProcesos);
        scrollProcesos.setBorder(BorderFactory.createTitledBorder("Procesos"));
        panelCentral.add(scrollProcesos);

        // Columna 2: BCP actual — cuadrícula alineada (etiqueta en negrita + valor monoespaciado)
        JPanel panelBCP = new JPanel(new GridBagLayout());
        panelBCP.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("BCP actual CPU1"),
                BorderFactory.createEmptyBorder(8, 12, 10, 12)
        ));

        Font fuenteEtiquetaBCP = new Font("SansSerif", Font.BOLD, 13);
        Font fuenteValorBCP = new Font("Monospaced", Font.PLAIN, 13);
        String[] etiquetasBCP = {"Proceso", "Estado", "PC", "IR", "AC", "AX", "BX", "CX", "DX"};
        JLabel[] valoresBCP = new JLabel[etiquetasBCP.length];

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;   // alinea a la izquierda
        gbc.fill = GridBagConstraints.NONE;     // no estira los componentes
        gbc.insets = new Insets(5, 4, 5, 12);

        for (int i = 0; i < etiquetasBCP.length; i++) {
            // --- Columna 0: etiqueta ---
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0;                     // sin peso: ancho natural
            JLabel lblNombre = new JLabel(etiquetasBCP[i] + ":");
            lblNombre.setFont(fuenteEtiquetaBCP);
            panelBCP.add(lblNombre, gbc);

            // --- Columna 1: valor ---
            gbc.gridx = 1;
            gbc.weightx = 1;                    
            JLabel lblValor = new JLabel("-");
            lblValor.setFont(fuenteValorBCP);
            valoresBCP[i] = lblValor;
            panelBCP.add(lblValor, gbc);
        }

        lblIdBCP = valoresBCP[0];
        lblEstadoBCP = valoresBCP[1];
        lblPC = valoresBCP[2];
        lblIR = valoresBCP[3];
        lblAC = valoresBCP[4];
        lblAX = valoresBCP[5];
        lblBX = valoresBCP[6];
        lblCX = valoresBCP[7];
        lblDX = valoresBCP[8];

        // Fila final que empuja todo el contenido hacia arriba (pegado al borde superior)
        gbc.gridx = 0;
        gbc.gridy = etiquetasBCP.length;
        gbc.gridwidth = 2;
        gbc.weighty = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        panelBCP.add(Box.createVerticalGlue(), gbc);

        panelCentral.add(panelBCP);
        // Columna 3: memoria/disco arriba, pantalla abajo
        JPanel panelDerecha = new JPanel(new GridLayout(2, 1, 5, 5));

        JPanel panelTablas = new JPanel(new GridLayout(1, 2, 5, 5));
        DefaultTableModel modeloMemoria = new DefaultTableModel(new Object[0][2], new String[]{"Pos", "Valor en memoria"});
        tablaMemoria = new JTable(modeloMemoria);
        DefaultTableModel modeloDisco = new DefaultTableModel(new Object[0][2], new String[]{"Pos", "Valor en disco"});
        tablaDisco = new JTable(modeloDisco);
        panelTablas.add(new JScrollPane(tablaMemoria));
        panelTablas.add(new JScrollPane(tablaDisco));
        panelDerecha.add(panelTablas);

        JPanel panelPantalla = new JPanel(new BorderLayout(5, 5));
        panelPantalla.setBorder(BorderFactory.createTitledBorder("Pantalla"));
        areaPantalla = new JTextArea();
        areaPantalla.setEditable(false);
        panelPantalla.add(new JScrollPane(areaPantalla), BorderLayout.CENTER);

        JPanel panelEntrada = new JPanel(new BorderLayout(5, 5));
        txtEntradaTeclado = new JTextField();
        txtEntradaTeclado.setEnabled(false);
        btnEnviarEntrada = new JButton("Enviar");
        btnEnviarEntrada.setEnabled(false);
        panelEntrada.add(new JLabel(">> Ingresar valor:"), BorderLayout.WEST);
        panelEntrada.add(txtEntradaTeclado, BorderLayout.CENTER);
        panelEntrada.add(btnEnviarEntrada, BorderLayout.EAST);
        panelPantalla.add(panelEntrada, BorderLayout.SOUTH);

        panelDerecha.add(panelPantalla);
        panelCentral.add(panelDerecha);

        add(panelCentral, BorderLayout.CENTER);

        // ---- Listeners ----
        btnCargarArchivos.addActionListener(this::btnCargarActionPerformed);
        btnEjecutar.addActionListener(this::btnEjecutarActionPerformed);
        btnPasoAPaso.addActionListener(this::btnPasoAPasoActionPerformed);
        btnLimpiar.addActionListener(this::btnLimpiarActionPerformed);
        btnEstadisticas.addActionListener(this::btnEstadisticasActionPerformed);
        btnConfigurar.addActionListener(this::btnConfigurarActionPerformed);
        txtEntradaTeclado.addActionListener(this::btnEnviarEntradaActionPerformed);
        btnEnviarEntrada.addActionListener(this::btnEnviarEntradaActionPerformed);
    }

        txtTamanoTotal.setText("256");
        txtTamanoTotal.addActionListener(this::txtTamanoTotalActionPerformed);

        txtTamanoSistema.setText("64");
        txtTamanoSistema.addActionListener(this::txtTamanoSistemaActionPerformed);

        btnAsignarMemoria.setText("Asignar memoria");
        btnAsignarMemoria.addActionListener(this::btnAsignarMemoriaActionPerformed);

        jLabel1.setText("Tamaño S.O.:");

        jLabel2.setText("Tamaño total:");

        btnSalir.setText("Salir");
        btnSalir.addActionListener(this::btnSalirActionPerformed);

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        lblTituloAC.setText("AC:");

        lblValorAC.setText("0");

        lblTituloAX.setText("AX:");

        lblValorAX.setText("0");

        lblTituloBX.setText("BX:");

        lblValorBX.setText("0");

        lblTituloCX.setText("CX:");

        lblTituloDX.setText("DX:");

        lblValorCX.setText("0");

        lblValorDX.setText("0");

        jLabel15.setText("BCP actual CPU");

        lblTituloPC.setText("PC:");

        lblValorPC.setText("0");

        lblTituloIR.setText("IR:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(lblTituloDX)
                        .addGap(18, 18, 18)
                        .addComponent(lblValorDX))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(lblTituloPC)
                                .addGap(18, 18, 18)
                                .addComponent(lblValorPC))
                            .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(lblTituloBX)
                                .addGap(18, 18, 18)
                                .addComponent(lblValorBX, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(lblTituloAX)
                                .addGap(20, 20, 20)
                                .addComponent(lblValorAX))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(lblTituloAC)
                                .addGap(18, 18, 18)
                                .addComponent(lblValorAC))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(lblTituloIR)
                                .addGap(18, 18, 18)
                                .addComponent(lblValorIR, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(lblTituloCX)
                            .addGap(18, 18, 18)
                            .addComponent(lblValorCX)
                            .addGap(139, 139, 139))))
                .addContainerGap(31, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel15)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTituloPC)
                    .addComponent(lblValorPC))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTituloIR, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblValorIR, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTituloAC)
                    .addComponent(lblValorAC))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTituloAX)
                    .addComponent(lblValorAX))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTituloBX)
                    .addComponent(lblValorBX))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTituloCX)
                    .addComponent(lblValorCX))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTituloDX)
                    .addComponent(lblValorDX))
                .addGap(98, 98, 98))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 386, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 378, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnSalir)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnCargar)
                        .addGap(18, 18, 18)
                        .addComponent(btnPaso_Paso)
                        .addGap(18, 18, 18)
                        .addComponent(btnEjecutar)
                        .addGap(18, 18, 18)
                        .addComponent(btnLimpiar)
                        .addGap(18, 18, 18)
                        .addComponent(btnEstadisticas)))
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addGap(74, 74, 74)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtTamanoSistema, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtTamanoTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAsignarMemoria, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 141, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnSalir, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(btnAsignarMemoria, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnEstadisticas, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnEjecutar, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnPaso_Paso, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnCargar, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel2))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtTamanoSistema, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtTamanoTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(6, 6, 6)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 477, Short.MAX_VALUE)
                    .addComponent(jScrollPane2))
                .addGap(17, 17, 17))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // E: no aplica
    // S: no aplica (void)
    // R: refresca labels de CPU, tablas de memoria/disco/procesos y la pantalla
    private void actualizarVista() {
        lblPC.setText(String.valueOf(cpu.getPC()));
        lblIR.setText(cpu.getIR());
        lblAC.setText(String.valueOf(cpu.getAC()));
        lblAX.setText(String.valueOf(cpu.getAX()));
        lblBX.setText(String.valueOf(cpu.getBX()));
        lblCX.setText(String.valueOf(cpu.getCX()));
        lblDX.setText(String.valueOf(cpu.getDX()));

        sincronizarBCP();
        actualizarTablaMemoria();
        actualizarTablaDisco();
        actualizarPantalla();
        actualizarTablaProcesos();
    }

    // E: no aplica
    // S: no aplica (void)
    // R: actualiza el estado del BCP según lo que está haciendo la CPU en este momento
    //    (esto es una simplificación: cuando exista Planificador/Lista de Trabajos real
    //    con varios procesos, este método debería moverse a la lógica de esa clase)
    private void sincronizarBCP() {
        if (bcp == null || cpu == null) {
            return;
        }
        if (cpu.programaTerminado()) {
            bcp.setEstado("Finalizado");
        } else if (cpu.isEsperandoEntrada()) {
            bcp.setEstado("Bloqueado");
        } else {
            bcp.setEstado("Ejecución");
        }
        bcp.capturaEstado(cpu);
        lblIdBCP.setText(String.valueOf(bcp.getIdProceso()));
        lblEstadoBCP.setText(bcp.getEstado());
        lblEstadoBCP.setForeground(colorParaEstado(bcp.getEstado()));
    }

    // E: estado (String) - "Preparado", "Ejecución", "Bloqueado" o "Finalizado"
    // S: Color - el color asociado a ese estado, para resaltarlo visualmente en el panel BCP
    // R: ninguna
    private Color colorParaEstado(String estado) {
        switch (estado) {
            case "Ejecución": return new Color(0, 128, 0);
            case "Bloqueado": return new Color(200, 120, 0);
            case "Finalizado": return new Color(120, 120, 120);
            case "Preparado": return new Color(0, 90, 190);
            default: return Color.BLACK;
        }
    }

    // E: no aplica
    // S: no aplica (void)
    // R: muestra en la tabla de Procesos el (único, por ahora) proceso cargado y su estado
    private void actualizarTablaProcesos() {
        Object[][] filas;
        if (bcp == null) {
            filas = new Object[0][2];
        } else {
            filas = new Object[][]{{bcp.getIdProceso(), bcp.getEstado()}};
        }
        tablaProcesos.setModel(new DefaultTableModel(filas, new String[]{"Procesos", "Estados"}));
    }

    // E: no aplica
    // S: no aplica (void)
    // R: dump crudo posición/valor de toda la memoria principal
    private void actualizarTablaMemoria() {
        String[] datos = memoria.getTodasLasPosiciones();
        Object[][] filas = new Object[datos.length][2];
        for (int i = 0; i < datos.length; i++) {
            filas[i][0] = i;
            filas[i][1] = datos[i];
        }
        tablaMemoria.setModel(new DefaultTableModel(filas, new String[]{"Pos", "Valor en memoria"}));
    }

    // E: no aplica
    // S: no aplica (void)
    // R: dump crudo posición/valor de todo el almacenamiento secundario
    private void actualizarTablaDisco() {
        if (disco == null) {
            tablaDisco.setModel(new DefaultTableModel(new Object[0][2], new String[]{"Pos", "Valor en disco"}));
            return;
        }
        int tamano = disco.getTamanoTotal();
        Object[][] filas = new Object[tamano][2];
        for (int i = 0; i < tamano; i++) {
            filas[i][0] = i;
            filas[i][1] = disco.leer(i);
        }
        tablaDisco.setModel(new DefaultTableModel(filas, new String[]{"Pos", "Valor en disco"}));
    }

    // E: no aplica
    // S: no aplica (void)
    // R: muestra la salida de INT 10H y, si la CPU está esperando teclado (INT 09H),
    //    el prompt ">> Ingresar valor:" y habilita el campo de entrada
    private void actualizarPantalla() {
        StringBuilder sb = new StringBuilder();
        for (String linea : cpu.getPantalla()) {
            sb.append(linea).append("\n");
        }
        if (cpu.isEsperandoEntrada()) {
            sb.append(">> Ingresar valor:\n");
        }
        areaPantalla.setText(sb.toString());

        boolean esperando = cpu.isEsperandoEntrada();
        txtEntradaTeclado.setEnabled(esperando);
        btnEnviarEntrada.setEnabled(esperando);
        if (esperando) {
            txtEntradaTeclado.requestFocusInWindow();
        }
    }

    // E: evt - clic en "Cargar archivos"
    // S: no aplica (void)
    // R: crea memoria/disco con la configuración actual si no existen, abre el
    //    selector de archivo, valida extensión .asm, parsea y carga el programa
    private void btnCargarActionPerformed(java.awt.event.ActionEvent evt) {
        if (memoria == null) {
            memoria = new Memoria(configuracion.getMemoriaPrincipal(), configuracion.getMemoriaSistema());
        }
        if (disco == null) {
            disco = new Disco(configuracion.getAlmacenamientoSecundario(), configuracion.getMemoriaVirtual());
        }

        JFileChooser selector = new JFileChooser();
        int resultado = selector.showOpenDialog(this);
        if (resultado != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File archivo = selector.getSelectedFile();
        if (!archivo.getName().endsWith(".asm")) {
            JOptionPane.showMessageDialog(this, "El archivo debe tener extensión .asm", "Archivo inválido", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            instruccionesActuales = ParserASM.leerArchivo(archivo);

            cpu = new CPU(memoria);
            cpu.setDisco(disco);
            tiempoInicio = System.currentTimeMillis();
            cpu.cargarPrograma(instruccionesActuales);

            bcp = new BCP(1, "Preparado");
            bcp.capturaEstado(cpu);

            actualizarVista();

        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error en el archivo", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "No se pudo leer el archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "El archivo no tiene un formato de texto válido. Asegúrate de que sea un archivo .asm de texto plano.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // E: evt - clic en "Paso a paso"
    // S: no aplica (void)
    // R: ejecuta un ciclo si hay programa cargado, no terminó y no está esperando teclado
    private void btnPasoAPasoActionPerformed(java.awt.event.ActionEvent evt) {
        if (cpu == null) {
            JOptionPane.showMessageDialog(this, "Primero cargá un archivo", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (cpu.programaTerminado()) {
            JOptionPane.showMessageDialog(this, "El programa ya terminó de ejecutarse", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (cpu.isEsperandoEntrada()) {
            JOptionPane.showMessageDialog(this, "La CPU está esperando un valor de teclado. Ingresalo en el panel Pantalla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        cpu.pasoAPaso();
        actualizarVista();
    }

    // E: evt - clic en "Limpiar"
    // S: no aplica (void)
    // R: pide confirmación si el programa no terminó; resetea CPU/BCP/instrucciones (memoria y disco persisten)
    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {
        if (cpu == null) {
            JOptionPane.showMessageDialog(this, "No hay nada cargado para limpiar", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!cpu.programaTerminado()) {
            int confirmacion = JOptionPane.showConfirmDialog(this, "El programa todavía no terminó de ejecutarse.\n¿Querés limpiarlo de todas formas?", "Confirmar limpieza", JOptionPane.YES_NO_OPTION);
            if (confirmacion != JOptionPane.YES_OPTION) {
                return;
            }
        }

        cpu = null;
        bcp = null;
        instruccionesActuales = null;

        lblIdBCP.setText("-");
        lblEstadoBCP.setText("-");
        lblEstadoBCP.setForeground(Color.BLACK);
        lblPC.setText("0");
        lblIR.setText("");
        lblAC.setText("0");
        lblAX.setText("0");
        lblBX.setText("0");
        lblCX.setText("0");
        lblDX.setText("0");
        areaPantalla.setText("");
        txtEntradaTeclado.setEnabled(false);
        btnEnviarEntrada.setEnabled(false);

        tablaProcesos.setModel(new DefaultTableModel(new Object[0][2], new String[]{"Procesos", "Estados"}));
        tablaMemoria.setModel(new DefaultTableModel(new Object[0][2], new String[]{"Pos", "Valor en memoria"}));
        tablaDisco.setModel(new DefaultTableModel(new Object[0][2], new String[]{"Pos", "Valor en disco"}));

        JOptionPane.showMessageDialog(this, "Datos limpiados con éxito.");
    }

    // E: evt - clic en "Ejecutar"
    // S: no aplica (void)
    // R: ejecuta todo el programa; si queda esperando teclado a mitad de camino, avisa y se detiene ahí
    private void btnEjecutarActionPerformed(java.awt.event.ActionEvent evt) {
        if (cpu == null) {
            JOptionPane.showMessageDialog(this, "Primero cargá un archivo", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (cpu.programaTerminado()) {
            JOptionPane.showMessageDialog(this, "El programa ya terminó de ejecutarse", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        cpu.ejecutarTodo();
        actualizarVista();

        if (cpu.isEsperandoEntrada()) {
            JOptionPane.showMessageDialog(this, "El programa se detuvo: está esperando un valor de teclado. Ingresalo en el panel Pantalla y volvé a darle a Ejecutar o Paso a paso.", "Esperando entrada", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // E: evt - clic en "Enviar" (o Enter en el campo de entrada)
    // S: no aplica (void)
    // R: entrega el valor de teclado a la CPU si estaba esperando uno
    private void btnEnviarEntradaActionPerformed(java.awt.event.ActionEvent evt) {
        if (cpu == null || !cpu.isEsperandoEntrada()) {
            return;
        }
        try {
            int valor = Integer.parseInt(txtEntradaTeclado.getText().trim());
            cpu.entregarEntradaTeclado(valor);
            txtEntradaTeclado.setText("");
            actualizarVista();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ingresá un número entero entre 0 y 255.", "Valor inválido", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        if(tamanoTotal > 1024){
            JOptionPane.showMessageDialog(this, "El tamaño total no puede ser mayor a 1024", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

    // E: evt - clic en "Estadísticas"
    // S: no aplica (void)
    // R: muestra progreso, tiempo simulado (por pesos) y desglose de operaciones ejecutadas
    private void btnEstadisticasActionPerformed(java.awt.event.ActionEvent evt) {
        if (cpu == null || instruccionesActuales == null) {
            JOptionPane.showMessageDialog(this, "Primero cargá un archivo", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int instruccionesEjecutadas = cpu.getPC() - memoria.getInicioUsuario();

        Map<String, Integer> contadores = new LinkedHashMap<>();
        for (int i = 0; i < instruccionesEjecutadas; i++) {
            String operacion = instruccionesActuales.get(i).getoperacion();
            contadores.put(operacion, contadores.getOrDefault(operacion, 0) + 1);
        }

        long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicio;
        String estadoPrograma = cpu.programaTerminado() ? "Terminado" : "En proceso";

        StringBuilder mensaje = new StringBuilder();
        mensaje.append("=== PROGRESO DE EJECUCIÓN ===\n");
        mensaje.append("Estado: ").append(estadoPrograma).append("\n");
        mensaje.append("Duración (reloj real): ").append(tiempoTranscurrido).append(" ms\n");
        mensaje.append("Tiempo de CPU simulado (suma de pesos): ").append(cpu.getTiempoSimulado()).append("\n");
        mensaje.append("Instrucciones ejecutadas: ").append(instruccionesEjecutadas).append(" de ").append(instruccionesActuales.size()).append("\n");
        mensaje.append("PC actual: ").append(cpu.getPC()).append("\n\n");
        mensaje.append("=== OPERACIONES UTILIZADAS ===\n");

        if (contadores.isEmpty()) {
            mensaje.append("(todavía no se ejecutó ninguna instrucción)");
        } else {
            for (Map.Entry<String, Integer> entrada : contadores.entrySet()) {
                mensaje.append(entrada.getKey()).append(": ").append(entrada.getValue()).append("\n");
            }
        }

        JOptionPane.showMessageDialog(this, mensaje.toString(), "Estadísticas de ejecución", JOptionPane.INFORMATION_MESSAGE);
    }

    // E: evt - clic en "Configurar memoria/disco"
    // S: no aplica (void)
    // R: abre un diálogo para editar/guardar/cargar la configuración de tamaños;
    //    al aplicar, invalida memoria/disco actuales para que se recreen con los nuevos valores
    private void btnConfigurarActionPerformed(java.awt.event.ActionEvent evt) {
        if (cpu != null && !cpu.programaTerminado()) {
            JOptionPane.showMessageDialog(this, "No se puede reconfigurar mientras un programa está en ejecución.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialogo = new JDialog(this, "Configurar memoria y disco", true);
        dialogo.setLayout(new BorderLayout(10, 10));

        JPanel panelCampos = new JPanel(new GridLayout(4, 2, 5, 5));
        JTextField campoMemPrincipal = new JTextField(String.valueOf(configuracion.getMemoriaPrincipal()));
        JTextField campoMemSistema = new JTextField(String.valueOf(configuracion.getMemoriaSistema()));
        JTextField campoDisco = new JTextField(String.valueOf(configuracion.getAlmacenamientoSecundario()));
        JTextField campoMemVirtual = new JTextField(String.valueOf(configuracion.getMemoriaVirtual()));

        panelCampos.add(new JLabel("Memoria principal:"));
        panelCampos.add(campoMemPrincipal);
        panelCampos.add(new JLabel("Memoria de sistema:"));
        panelCampos.add(campoMemSistema);
        panelCampos.add(new JLabel("Almacenamiento secundario:"));
        panelCampos.add(campoDisco);
        panelCampos.add(new JLabel("Memoria virtual:"));
        panelCampos.add(campoMemVirtual);

        JPanel panelBotonesDialogo = new JPanel(new FlowLayout());
        JButton btnCargarConfig = new JButton("Cargar desde archivo");
        JButton btnGuardarConfig = new JButton("Guardar en archivo");
        JButton btnAplicar = new JButton("Aplicar");
        JButton btnCancelar = new JButton("Cancelar");

        btnCargarConfig.addActionListener(e -> {
            JFileChooser selector = new JFileChooser();
            if (selector.showOpenDialog(dialogo) == JFileChooser.APPROVE_OPTION) {
                try {
                    configuracion.cargarDesdeArchivo(selector.getSelectedFile());
                    campoMemPrincipal.setText(String.valueOf(configuracion.getMemoriaPrincipal()));
                    campoMemSistema.setText(String.valueOf(configuracion.getMemoriaSistema()));
                    campoDisco.setText(String.valueOf(configuracion.getAlmacenamientoSecundario()));
                    campoMemVirtual.setText(String.valueOf(configuracion.getMemoriaVirtual()));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(dialogo, "No se pudo leer el archivo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnGuardarConfig.addActionListener(e -> {
            JFileChooser selector = new JFileChooser();
            if (selector.showSaveDialog(dialogo) == JFileChooser.APPROVE_OPTION) {
                try {
                    configuracion.guardarEnArchivo(selector.getSelectedFile());
                    JOptionPane.showMessageDialog(dialogo, "Configuración guardada.");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(dialogo, "No se pudo guardar el archivo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnAplicar.addActionListener(e -> {
            try {
                configuracion.setMemoriaPrincipal(Integer.parseInt(campoMemPrincipal.getText().trim()));
                configuracion.setMemoriaSistema(Integer.parseInt(campoMemSistema.getText().trim()));
                configuracion.setAlmacenamientoSecundario(Integer.parseInt(campoDisco.getText().trim()));
                configuracion.setMemoriaVirtual(Integer.parseInt(campoMemVirtual.getText().trim()));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialogo, "Todos los valores deben ser números enteros.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // se invalidan para que btnCargarActionPerformed las recree con los nuevos tamaños
            memoria = null;
            disco = null;
            dialogo.dispose();
            JOptionPane.showMessageDialog(this, "Configuración aplicada. Se usará al cargar el próximo archivo.");
        });

        btnCancelar.addActionListener(e -> dialogo.dispose());

        panelBotonesDialogo.add(btnCargarConfig);
        panelBotonesDialogo.add(btnGuardarConfig);
        panelBotonesDialogo.add(btnAplicar);
        panelBotonesDialogo.add(btnCancelar);

        dialogo.add(panelCampos, BorderLayout.CENTER);
        dialogo.add(panelBotonesDialogo, BorderLayout.SOUTH);
        dialogo.pack();
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException ex) {
            // se ignora: si Nimbus no está disponible, se usa el look and feel por defecto
        }

        EventQueue.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}