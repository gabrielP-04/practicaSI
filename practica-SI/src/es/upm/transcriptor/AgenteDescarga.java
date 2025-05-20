package es.upm.transcriptor;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.io.File;

public class AgenteDescarga extends Agent {
    protected void setup() {
        System.out.println("[Descarga] Iniciado: " + getLocalName());
        
        limpiarCarpetaDownloads();

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("descarga");
        sd.setName("ServicioDescargaAudioVideo");
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
                    String url = msg.getContent();
                    System.out.println("[Descarga] URL recibida: " + url);

                    String audioWav = descargarYConvertirAudioVideo(url);
                    if (audioWav != null) {
                        ACLMessage mensajeAudio = new ACLMessage(ACLMessage.INFORM);
                        mensajeAudio.addReceiver(new AID("agPer", AID.ISLOCALNAME));
                        mensajeAudio.setContent(audioWav);
                        send(mensajeAudio);
                        System.out.println("[Descarga] Audio convertido enviado a AgentePercepcion.");

                        // Avisar a AgenteInterfaz que el video está listo
                        ACLMessage avisoVideoListo = new ACLMessage(ACLMessage.INFORM);
                        avisoVideoListo.addReceiver(new AID("agUI", AID.ISLOCALNAME));
                        avisoVideoListo.setContent("VIDEO_LISTO");
                        send(avisoVideoListo);
                        System.out.println("[Descarga] Aviso VIDEO_LISTO enviado a AgenteInterfaz.");
                    } else {
                        System.err.println("[Descarga] Error en la descarga o conversión del audio.");
                    }
                } else {
                    block();
                }
            }
        });
    }

    private String descargarYConvertirAudioVideo(String url) {
        try {
            File dir = new File("downloads");
            if (!dir.exists()) {
                dir.mkdirs();
                System.out.println("[Descarga] Carpeta downloads creada.");
            }

            String videoPath = "downloads/video.mp4";
            String audioMp3Path = "downloads/audio.mp3";
            String audioWavPath = "downloads/audio.wav";

            // Descarga vídeo mp4
            ProcessBuilder pbVideo = new ProcessBuilder(
                "yt-dlp", "-f", "mp4", "-o", videoPath, url);
            pbVideo.redirectErrorStream(true);
            Process pVideo = pbVideo.start();
            pVideo.waitFor();
            System.out.println("[Descarga] Vídeo descargado: " + videoPath);

            // Descarga audio mp3
            ProcessBuilder pbAudio = new ProcessBuilder(
                "yt-dlp", "-x", "--audio-format", "mp3", "-o", audioMp3Path, url);
            pbAudio.redirectErrorStream(true);
            Process pAudio = pbAudio.start();
            pAudio.waitFor();
            System.out.println("[Descarga] Audio descargado: " + audioMp3Path);

            // Convierte audio a wav 16kHz mono
            ProcessBuilder pbFfmpeg = new ProcessBuilder(
                "ffmpeg", "-y", "-i", audioMp3Path, "-ar", "16000", "-ac", "1", audioWavPath);
            pbFfmpeg.redirectErrorStream(true);
            Process pFfmpeg = pbFfmpeg.start();
            pFfmpeg.waitFor();
            System.out.println("[Descarga] Audio convertido a WAV: " + audioWavPath);

            File wavFile = new File(audioWavPath);
            if (wavFile.exists()) {
                return audioWavPath;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private void limpiarCarpetaDownloads() {
        File dir = new File("downloads");
        if (dir.exists() && dir.isDirectory()) {
            File[] archivos = dir.listFiles();
            if (archivos != null) {
                for (File f : archivos) {
                    if (f.isFile()) {
                        boolean borrado = f.delete();
                        
                    }
                }
            }
        }
    }

}
