package es.upm.transcriptor;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class AgenteTraduccion extends Agent {

    protected void setup() {
        System.out.println("[Traduccion] Iniciado: " + getLocalName());

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String texto = msg.getContent();
                    try {
                        String traduccion = traducir(texto, "es", "en");
                        System.out.println("[Traduccion] Traducción completada.");
                        ACLMessage respuesta = msg.createReply();
                        respuesta.setContent(traduccion);
                        send(respuesta);
                    } catch (IOException e) {
                        System.err.println("[Traduccion] Error: " + e.getMessage());
                    }
                } else {
                    block();
                }
            }
        });
    }

    private String traducir(String texto, String desde, String a) throws IOException {
        String url = "https://libretranslate.de/translate";
        String data = "q=" + URLEncoder.encode(texto, "UTF-8") +
                      "&source=" + desde +
                      "&target=" + a +
                      "&format=text";

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(data.getBytes(StandardCharsets.UTF_8));
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder res = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) res.append(line);
            return extraerTexto(res.toString());
        }
    }

    private String extraerTexto(String json) {
        int start = json.indexOf("\"translatedText\":\"") + 18;
        int end = json.indexOf("\"", start);
        return json.substring(start, end).replace("\\n", "\n");
    }
}