package es.upm.transcriptor;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

public class AgenteInterfaz extends Agent {
    private JTextField urlField;
    private JTextArea outputArea;
    private JLabel statusLabel;
    private JProgressBar progressBar;
    private EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private JSlider volumeSlider;
    boolean videoListo = false;

    protected void setup() {
        System.out.println("[Interfaz] Iniciado: " + getLocalName());

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("interfaz");
        sd.setName("ServicioVisualizacion");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(this::crearVentana);

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String contenido = msg.getContent();
                    if (videoListo || "VIDEO_LISTO".equals(contenido)) {
                        videoListo = true;
                        if ("SUBT_LISTO".equals(contenido)) {
                            System.out.println("[Interfaz] Se esta reproduciendo el video");
                            actualizarEstado("Reproduciendo video...");
                            reproducirVideo();
                            SwingUtilities.invokeLater(() -> {
                                try {
                                    String texto = new String(Files.readAllBytes(Paths.get("downloads/video.srt")));
                                    outputArea.setText(texto);
                                } catch (Exception ex) {
                                    outputArea.setText("[Error] No se pudo leer la transcripción.");
                                }
                            });
                            videoListo = false;
                        }
                    }
                } else {
                    block();
                }
            }
        });
    }

    private void crearVentana() {
        JFrame frame = new JFrame("Transcriptor desde YouTube");
        frame.setSize(1000, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        Color fondo = new Color(245, 245, 245);
        Color azul = new Color(70, 130, 180);
        Color gris = new Color(120, 120, 120);
        Color texto = new Color(33, 33, 33);
        Font fuente = new Font("Segoe UI", Font.PLAIN, 16);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(fondo);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel label = new JLabel("Introduce enlace de YouTube:");
        label.setFont(fuente);
        label.setForeground(texto);
        urlField = new JTextField();
        urlField.setFont(fuente);
        JButton enviarBtn = new JButton("Transcribir y reproducir");
        estilizarBoton(enviarBtn, azul);

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBackground(fondo);
        topPanel.add(label, BorderLayout.NORTH);
        topPanel.add(urlField, BorderLayout.CENTER);
        topPanel.add(enviarBtn, BorderLayout.SOUTH);

        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        mediaPlayerComponent.setPreferredSize(new Dimension(960, 540));

        JButton btnPlay = new JButton("Play");
        JButton btnPause = new JButton("Pause");
        estilizarBoton(btnPlay, gris);
        estilizarBoton(btnPause, gris);

        volumeSlider = new JSlider(0, 100, 50);
        volumeSlider.addChangeListener(e -> mediaPlayerComponent.mediaPlayer().audio().setVolume(volumeSlider.getValue()));

        JButton restartBtn = new JButton("Restart");
        estilizarBoton(restartBtn, gris);
        restartBtn.addActionListener(e -> {
            mediaPlayerComponent.mediaPlayer().controls().setTime(0);
        });


        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        controlsPanel.setBackground(fondo);
        controlsPanel.add(btnPlay);
        controlsPanel.add(btnPause);
        controlsPanel.add(new JLabel("Volumen:"));
        controlsPanel.add(volumeSlider);
        controlsPanel.add(restartBtn);

        JButton btnVerTexto = new JButton("Ver transcripción completa");
        estilizarBoton(btnVerTexto, azul);
        btnVerTexto.addActionListener(e -> {
            try {
                String raw = new String(Files.readAllBytes(Paths.get("downloads/video.srt")));
                StringBuilder textoLimpio = new StringBuilder();
                for (String linea : raw.split("\r?\n")) {
                    if (linea.trim().isEmpty() || linea.matches("^\\d+$") || linea.contains("-->")) {
                        continue;
                    }
                    textoLimpio.append(linea).append("\n");
                }
                JTextArea area = new JTextArea(textoLimpio.toString());
                area.setEditable(false);
                area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                JScrollPane scroll = new JScrollPane(area);
                scroll.setPreferredSize(new Dimension(600, 400));
                JOptionPane.showMessageDialog(null, scroll, "Transcripción", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error al leer transcripción.");
            }
        });

        JPanel accionesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        accionesPanel.setBackground(fondo);
        accionesPanel.add(btnVerTexto);

        JPanel estadoPanel = new JPanel(new BorderLayout());
        estadoPanel.setBackground(fondo);

        statusLabel = new JLabel("Esperando enlace...");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setForeground(Color.DARK_GRAY);
        estadoPanel.add(statusLabel, BorderLayout.NORTH);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        estadoPanel.add(progressBar, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBackground(fondo);
        bottomPanel.add(controlsPanel, BorderLayout.NORTH);
        bottomPanel.add(accionesPanel, BorderLayout.CENTER);
        bottomPanel.add(estadoPanel, BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(mediaPlayerComponent, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        outputArea = new JTextArea();
        outputArea.setEditable(false);

        frame.add(mainPanel);
        frame.setVisible(true);

        enviarBtn.addActionListener(e -> {
            String url = urlField.getText().trim();
            if (!url.isEmpty()) {
                mostrarCarga();
                actualizarEstado("Procesando video...");
                enviarMensaje(url);
                outputArea.setText("[Enviado] " + url + "\n");
            } else {
                JOptionPane.showMessageDialog(null, "Por favor, introduce un enlace.");
            }
        });

        btnPlay.addActionListener(e -> mediaPlayerComponent.mediaPlayer().controls().play());
        btnPause.addActionListener(e -> mediaPlayerComponent.mediaPlayer().controls().pause());
    }

    private void estilizarBoton(JButton boton, Color colorFondo) {
        boton.setBackground(colorFondo);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void actualizarEstado(String texto) {
        if (statusLabel != null) {
            statusLabel.setText(texto);
            statusLabel.setVisible(true);
        }
    }

    private void mostrarCarga() {
        if (progressBar != null) {
            progressBar.setVisible(true);
        }
    }

    private void ocultarCarga() {
        if (progressBar != null) {
            progressBar.setVisible(false);
        }
    }

    private void enviarMensaje(String url) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID("agDesc", AID.ISLOCALNAME));
        msg.setContent(url);
        send(msg);
    }

    private void reproducirVideo() {
        File videoFile = new File("downloads/video.mp4");
        System.out.println("Reproduciendo vídeo: " + videoFile.getAbsolutePath());
        if (!videoFile.exists()) {
            outputArea.append("\n[ERROR] El vídeo no se encuentra en: " + videoFile.getAbsolutePath());
            actualizarEstado("El vídeo no existe.");
            ocultarCarga();
            return;
        }
        MediaPlayer player = mediaPlayerComponent.mediaPlayer();
        boolean started = player.media().start(videoFile.getAbsolutePath());
        System.out.println("Intento de reproducción devuelto: " + started);
        ocultarCarga();
        if (started) actualizarEstado("Reproducción iniciada");
        else actualizarEstado("Fallo al iniciar reproducción");
    }
}