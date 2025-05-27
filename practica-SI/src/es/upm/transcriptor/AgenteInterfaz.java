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

import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

public class AgenteInterfaz extends Agent {
    private JTextField urlField;
    private JTextArea outputArea;
    private EmbeddedMediaPlayerComponent mediaPlayerComponent;
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
                            reproducirVideo();
                            System.out.println("[Interfaz] Texto final recibido:");
                            System.out.println(contenido);
                            outputArea.setText(contenido);
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
        frame.setSize(1000, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel("Introduce enlace de YouTube:");
        label.setFont(new Font("SansSerif", Font.BOLD, 16));

        urlField = new JTextField();
        urlField.setFont(new Font("SansSerif", Font.PLAIN, 16));

        JButton enviarBtn = new JButton("Transcribir y reproducir");
        enviarBtn.setFont(new Font("SansSerif", Font.BOLD, 16));

        JButton btnPlay = new JButton("Play");
        JButton btnPause = new JButton("Pause");
        btnPlay.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnPause.setFont(new Font("SansSerif", Font.BOLD, 14));

        outputArea = new JTextArea();
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(label, BorderLayout.NORTH);
        topPanel.add(urlField, BorderLayout.CENTER);
        topPanel.add(enviarBtn, BorderLayout.SOUTH);

        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        mediaPlayerComponent.setPreferredSize(new Dimension(960, 540));

        JPanel controlsPanel = new JPanel();
        controlsPanel.add(btnPlay);
        controlsPanel.add(btnPause);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(mediaPlayerComponent, BorderLayout.CENTER);
        panel.add(controlsPanel, BorderLayout.EAST);
        panel.add(scrollPane, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);

        enviarBtn.addActionListener(e -> {
            String url = urlField.getText().trim();
            if (!url.isEmpty()) {
                enviarMensaje(url);
                outputArea.setText("[Enviado] " + url + "\n");
            } else {
                JOptionPane.showMessageDialog(null, "Por favor, introduce un enlace.");
            }
        });

        btnPlay.addActionListener(e -> mediaPlayerComponent.mediaPlayer().controls().play());
        btnPause.addActionListener(e -> mediaPlayerComponent.mediaPlayer().controls().pause());
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
            return;
        }
        MediaPlayer player = mediaPlayerComponent.mediaPlayer();
        boolean started = player.media().start(videoFile.getAbsolutePath());
        System.out.println("Intento de reproducción devuelto: " + started);
    }
}