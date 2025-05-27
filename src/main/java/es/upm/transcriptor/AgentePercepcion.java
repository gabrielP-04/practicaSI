// Paquete base para todos los agentes
package es.upm.transcriptor;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;

import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AgentePercepcion extends Agent {
    protected void setup() {
        System.out.println("[Percepcion] Iniciado: " + getLocalName());

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("percepcion");
        sd.setName("ServicioTranscripcion");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = myAgent.receive();
                if (msg != null && msg.getPerformative() == ACLMessage.INFORM) {
                    String contenido = msg.getContent();
                    if (contenido.contains(";")) {
                        String[] partes = contenido.split(";");
                        String rutaAudio = partes[0];
                        String idioma = partes.length > 1 ? partes[1] : "es";

                        System.out.println("[Percepcion] Ruta de audio recibida: " + rutaAudio);
                        System.out.println("[Percepcion] Idioma detectado: " + idioma);

                        // Seleccionar el modelo de Vosk según el idioma
                        String modeloPath = getModeloPorIdioma(idioma);
                        if (modeloPath != null) {
                            try {
                                transcribirAudio(rutaAudio, modeloPath);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            System.err.println("[Percepcion] Idioma no soportado: " + idioma);
                        }

                    } else {
                        System.err.println("[Percepcion] Formato de mensaje inválido: " + contenido);
                    }
                } else {
                    block();
                }
            }
        });
    }

    private String transcribirAudio(String modelPath, String audioPath) throws Exception {
        LibVosk.setLogLevel(LogLevel.WARNINGS);

        String outputSrt = "downloads/video.srt"; // Salida SRT

        Model model = new Model(modelPath);
        Recognizer recognizer = new Recognizer(model, 16000.0f);
        recognizer.setWords(true);

        AudioInputStream ais = AudioSystem.getAudioInputStream(new File(audioPath));
        ais = AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, ais);

        byte[] buffer = new byte[4096];
        int bytesRead;

        List<SubtitleSegment> segments = new ArrayList<>();
        int index = 1;

        while ((bytesRead = ais.read(buffer)) >= 0) {
            if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                String result = recognizer.getResult();
                SubtitleSegment segment = SubtitleSegment.fromJson(index++, result);
                if (segment != null)
                    segments.add(segment);
            }
        }

        // Resultado final
        String finalResult = recognizer.getFinalResult();
        SubtitleSegment finalSegment = SubtitleSegment.fromJson(index++, finalResult);
        if (finalSegment != null)
            segments.add(finalSegment);

        recognizer.close();

        // Escribe archivo .srt
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputSrt))) {
            for (SubtitleSegment seg : segments) {
                writer.write(seg.toSrtFormat());
                writer.newLine();
            }
        }

        System.out.println("Subtítulos guardados en: " + outputSrt);
        return outputSrt;
    }

    private String getModeloPorIdioma(String idioma) {
        switch (idioma.toLowerCase()) {
            case "es":
            case "spa":
            case "spanish":
                return "models/vosk-model-small-es-0.42";

            case "en":
            case "eng":
            case "english":
                return "models/vosk-model-small-en-us-0.15";

            case "de":
            case "deu":
            case "german":
                return "models/vosk-model-small-de-0.15";

            case "fr":
            case "fra":
            case "french":
                return "models/vosk-model-small-fr-0.22";

            case "it":
            case "ita":
            case "italian":
                return "models/vosk-model-small-it-0.4";

            case "pt":
            case "portugués":
            case "portuguese":
                return "models/vosk-model-small-pt-0.3";

            case "zh":
            case "zho":
            case "chinese":
            case "mandarin":
                return "models/vosk-model-small-cn-0.22";

            default:
                return null; // o puedes retornar un modelo por defecto, como español
        }
    }

    static class SubtitleSegment {
        int index;
        double start;
        double end;
        String text;

        static SubtitleSegment fromJson(int index, String json) {
            try {
                // Solo arregla comas decimales mal formateadas (sin romper el JSON)
                json = json.replaceAll("(\\d),(\\d{3,})", "$1.$2");

                org.json.JSONObject obj = new org.json.JSONObject(json);
                org.json.JSONArray words = obj.getJSONArray("result");
                if (words.length() == 0)
                    return null;

                double start = words.getJSONObject(0).getDouble("start");
                double end = words.getJSONObject(words.length() - 1).getDouble("end");
                String text = obj.getString("text");

                SubtitleSegment s = new SubtitleSegment();
                s.index = index;
                s.start = start;
                s.end = end;
                s.text = text;
                return s;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        String formatTime(double seconds) {
            int h = (int) (seconds / 3600);
            int m = (int) ((seconds % 3600) / 60);
            int s = (int) (seconds % 60);
            int ms = (int) ((seconds - (int) seconds) * 1000);
            return String.format("%02d:%02d:%02d,%03d", h, m, s, ms);
        }

        String toSrtFormat() {
            return String.format("%d\n%s --> %s\n%s\n",
                    index,
                    formatTime(start),
                    formatTime(end),
                    text);
        }
    }
}