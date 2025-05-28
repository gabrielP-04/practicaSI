package es.upm.transcriptor;

import jade.core.AID;
import jade.lang.acl.ACLMessage;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class InterfazSwing extends JFrame {

    private JTextField urlField;
    private JTextArea outputArea;

    public InterfazSwing() {
        setTitle("Transcriptor desde YouTube");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());

        JLabel label = new JLabel("Introduce enlace de YouTube:");
        urlField = new JTextField();
        JButton enviarBtn = new JButton("Transcribir");

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(label, BorderLayout.NORTH);
        topPanel.add(urlField, BorderLayout.CENTER);
        topPanel.add(enviarBtn, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        add(panel);

        enviarBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String url = urlField.getText().trim();
                if (!url.isEmpty()) {
                    enviarMensaje(url);
                    outputArea.append("Enlace enviado: " + url + "\n");
                } else {
                    JOptionPane.showMessageDialog(null, "Por favor, introduce un enlace.");
                }
            }
        });
    }

    private void enviarMensaje(String url) {
        try {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("agDesc", AID.ISLOCALNAME));
            msg.setContent(url);
            // Requiere un agente en ejecución que maneje el envío
            // Aquí puedes usar un agente JADE ya activo que tenga acceso a la plataforma
            // Este método depende de tener acceso a un agente para enviar mensajes
            // En su defecto, se puede usar un agente Swing lanzado que tenga `send(msg)`

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            InterfazSwing gui = new InterfazSwing();
            gui.setVisible(true);
        });
    }
}