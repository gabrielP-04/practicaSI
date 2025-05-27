package es.upm.transcriptor;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.LibVosk;
import org.vosk.LogLevel;

import javax.sound.sampled.*;
import java.io.*;
import java.util.*;

public class VoskSubtitleGenerator {

    public static void main(String[] args) throws Exception {
        LibVosk.setLogLevel(LogLevel.WARNINGS);

        String modelPath = "models/vosk-model-small-es-0.42"; // Ruta del modelo
        String audioPath = "downloads/audio.wav";             // Audio PCM mono 16kHz
        String outputSrt = "downloads/video.srt";             // Salida SRT

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
                System.out.println("JSON bruto:\n" + result);
                SubtitleSegment segment = SubtitleSegment.fromJson(index++, result);
                if (segment != null)
                    segments.add(segment);
            }
        }

        // Resultado final
        String finalResult = recognizer.getFinalResult();
        System.out.println("Final:\n" + finalResult);
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
