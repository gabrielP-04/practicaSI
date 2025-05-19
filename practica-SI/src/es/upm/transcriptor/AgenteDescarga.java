// Paquete base para todos los agentes
package es.upm.transcriptor;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;

import java.io.*;
import java.util.UUID;

public class AgenteDescarga extends Agent {
    protected void setup() {
        System.out.println("[Descarga] Iniciado: " + getLocalName());

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("descarga");
        sd.setName("ServicioDescargaAudio");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    System.out.println("[Descarga] Mensaje recibido");
                    String url = msg.getContent();
                    System.out.println("[Descarga] URL recibida: " + url);

                    String audioWav = descargarYConvertirAudio(url);
                    if (audioWav != null) {
                        System.out.println("[Descarga] WAV generado en: " + audioWav);

                        ACLMessage mensajeAudio = new ACLMessage(ACLMessage.INFORM);
                        mensajeAudio.addReceiver(new AID("agPer", AID.ISLOCALNAME));
                        mensajeAudio.setContent(audioWav);
                        send(mensajeAudio);

                        System.out.println("[Descarga] Enviado a agPer");
                    } else {
                        System.err.println("[Descarga] Error generando el WAV");
                    }
                } else {
                    block();
                }
            }
        });

    }

    private String descargarYConvertirAudio(String url) {
        try {
            String baseName = "downloads/audio_" + UUID.randomUUID();
            String outputMp3 = baseName + ".mp3";
            String outputWav = baseName + ".wav";

            // Ejecutar yt-dlp para descargar el audio
            ProcessBuilder pbYt = new ProcessBuilder("yt-dlp", "-x", "--audio-format", "mp3", "-o", outputMp3, url);
            pbYt.redirectErrorStream(true);
            Process procYt = pbYt.start();
            procYt.waitFor();

            // Convertir a wav 16kHz mono
            ProcessBuilder pbFfmpeg = new ProcessBuilder(
                "ffmpeg", "-i", outputMp3, "-ar", "16000", "-ac", "1", outputWav
            );
            pbFfmpeg.redirectErrorStream(true);
            Process procFfmpeg = pbFfmpeg.start();
            procFfmpeg.waitFor();

            // Verificamos que el archivo existe
            File wav = new File(outputWav);
            if (wav.exists()) {
                return outputWav;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}