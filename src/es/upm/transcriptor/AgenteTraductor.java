package es.upm.transcriptor;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class AgenteTraductor extends Agent {

    protected void setup() {
        System.out.println("[Traductor] Iniciado: " + getLocalName());

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String texto = msg.getContent();
                    System.out.println("[Traductor] Texto recibido para traducir.");
                    String traducido = traducirTextoLibreTranslate(texto);
                    ACLMessage respuesta = new ACLMessage(ACLMessage.INFORM);
                    respuesta.addReceiver(new AID("agUI", AID.ISLOCALNAME));
                    respuesta.setContent(traducido);
                    send(respuesta);
                    System.out.println("[Traductor] Traducción enviada a la interfaz.");
                } else {
                    block();
                }
            }
        });
    }

    private String traducirTextoLibreTranslate(String texto) {
        try {
            URL url = new URL("http://127.0.0.1:5000/translate_file");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            String jsonInputString = String.format("{\"q\": \"%s\", \"source\": \"auto\", \"target\": \"es\", \"format\": \"text\"}",
                    texto.replace("\"", "\\\""));

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            StringBuilder respuesta = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    respuesta.append(responseLine.trim());
                }
            }

            String resultado = respuesta.toString();
            int start = resultado.indexOf("\"translatedText\":\"") + 18;
            int end = resultado.indexOf("\"", start);
            return resultado.substring(start, end);

        } catch (Exception e) {
            e.printStackTrace();
            return "[ERROR] No se pudo traducir el texto.";
        }
    }
}
