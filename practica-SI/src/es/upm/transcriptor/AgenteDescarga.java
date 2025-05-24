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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class AgenteDescarga extends Agent {
    protected void setup() {
        System.out.println("[Descarga] Iniciado: " + getLocalName());

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
            // Eliminar archivos existentes en la carpeta downloads
            eliminarArchivosExistentes();

            File dir = new File("downloads");
            if (!dir.exists()) {
                dir.mkdirs();
                System.out.println("[Descarga] Carpeta downloads creada.");
            }

            String videoPath = "downloads/video.mp4";
            String audioMp3Path = "downloads/audio.mp3";
            String audioWavPath = "downloads/audio.wav";

            // 1. Descarga el vídeo
            System.out.println("[Descarga] Iniciando descarga del vídeo...");
            ProcessBuilder pbVideo = new ProcessBuilder(
                "yt-dlp", "-f", "bestvideo+bestaudio", "--merge-output-format", "mp4", "-o", videoPath, url);
            pbVideo.redirectErrorStream(true);
            Process pVideo = pbVideo.start();
            
            // Capturar la salida del proceso para depuración
            BufferedReader reader = new BufferedReader(new InputStreamReader(pVideo.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); // Mostrar la salida de yt-dlp
            }

            boolean finished = pVideo.waitFor(120, TimeUnit.SECONDS);  // Espera 2 minutos
            if (!finished) {
                System.out.println("[Descarga] El proceso de descarga del vídeo no terminó a tiempo.");
                pVideo.destroy();  // Termina el proceso si no terminó en el tiempo esperado
                return null;
            }
            System.out.println("[Descarga] Vídeo descargado y combinado: " + videoPath);

            // 2. Descarga el audio
            System.out.println("[Descarga] Iniciando descarga del audio...");
            ProcessBuilder pbAudio = new ProcessBuilder(
                "yt-dlp", "-x", "--audio-format", "mp3", "-o", audioMp3Path, url);
            pbAudio.redirectErrorStream(true);
            Process pAudio = pbAudio.start();
            reader = new BufferedReader(new InputStreamReader(pAudio.getInputStream()));
            while ((line = reader.readLine()) != null) {
                System.out.println(line);  // Mostrar la salida de yt-dlp
            }

            finished = pAudio.waitFor(120, TimeUnit.SECONDS);
            if (!finished) {
                System.out.println("[Descarga] El proceso de descarga del audio no terminó a tiempo.");
                pAudio.destroy();  // Termina el proceso si no terminó en el tiempo esperado
                return null;
            }
            System.out.println("[Descarga] Audio descargado: " + audioMp3Path);

            // 3. Convierte el audio a WAV
            System.out.println("[Descarga] Iniciando conversión del audio...");
            ProcessBuilder pbFfmpeg = new ProcessBuilder(
                "ffmpeg", "-y", "-i", audioMp3Path, "-ar", "16000", "-ac", "1", audioWavPath);
            pbFfmpeg.redirectErrorStream(true);
            Process pFfmpeg = pbFfmpeg.start();
            reader = new BufferedReader(new InputStreamReader(pFfmpeg.getInputStream()));
            while ((line = reader.readLine()) != null) {
                System.out.println(line);  // Mostrar la salida de ffmpeg
            }

            finished = pFfmpeg.waitFor(120, TimeUnit.SECONDS);
            if (!finished) {
                System.out.println("[Descarga] El proceso de conversión del audio no terminó a tiempo.");
                pFfmpeg.destroy();
                return null;
            }
            System.out.println("[Descarga] Audio convertido a WAV: " + audioWavPath);

            // Verifica si el archivo de audio WAV existe
            File wavFile = new File(audioWavPath);
            if (wavFile.exists()) {
                // Limpia archivos temporales si existen
                eliminarArchivosTemporales();
                return audioWavPath;  // Devolvemos la ruta del archivo WAV para enviarlo a Percepcion
            } else {
                System.err.println("[Descarga] Error: El archivo WAV no se ha creado.");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void eliminarArchivosExistentes() {
        File dir = new File("downloads");
        if (dir.exists() && dir.isDirectory()) {
            String[] archivos = dir.list();
            if (archivos != null) {
                for (String archivo : archivos) {
                    File archivoTemp = new File(dir, archivo);
                    if (archivoTemp.delete()) {
                        System.out.println("[Descarga] Archivo eliminado: " + archivo);
                    } else {
                        System.out.println("[Descarga] No se pudo eliminar el archivo: " + archivo);
                    }
                }
            }
        }
    }

    private void eliminarArchivosTemporales() {
        File dir = new File("downloads");
        if (dir.exists() && dir.isDirectory()) {
            String[] archivos = dir.list();
            if (archivos != null) {
                for (String archivo : archivos) {
                    if (archivo.endsWith(".part") || archivo.endsWith(".ytdl")) {
                        File archivoTemp = new File(dir, archivo);
                        boolean borrado = archivoTemp.delete();
                        if (borrado) {
                            System.out.println("[Descarga] Archivo temporal borrado: " + archivo);
                        } else {
                            System.out.println("[Descarga] No se pudo borrar el archivo temporal: " + archivo);
                        }
                    }
                }
            }
        }
    }
}
