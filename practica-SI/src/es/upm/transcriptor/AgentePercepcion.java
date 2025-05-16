package es.upm.transcriptor;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;

import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.*;
import java.util.regex.*;

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

        addBehaviour(new OneShotBehaviour(this) {
            public void action() {
                String modeloRuta = "models/vosk-model-small-es-0.42";
                String audioRuta = "audios/salida.wav";
                String textoTranscrito = transcribirAudio(modeloRuta, audioRuta);

                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(new AID("agProc", AID.ISLOCALNAME));
                msg.setContent(textoTranscrito);
                send(msg);
                System.out.println("[Percepcion] Texto enviado al procesador.");
            }
        });
    }

    private String transcribirAudio(String modeloPath, String audioPath) {
        StringBuilder resultado = new StringBuilder();

        try (Model model = new Model(modeloPath);
             InputStream ais = new FileInputStream(audioPath);
             AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(ais))) {

            AudioFormat format = audioInputStream.getFormat();
            if (format.getSampleRate() != 16000 || format.getChannels() != 1) {
                System.err.println("[ERROR] El archivo .wav debe estar a 16kHz y en mono canal.");
                return "";
            }

            Recognizer recognizer = new Recognizer(model, 16000);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = audioInputStream.read(buffer)) >= 0) {
                if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                    String result = recognizer.getResult();
                    String texto = extraerTexto(result);
                    if (!texto.isEmpty()) {
                    	String[] palabras = texto.toUpperCase().split(" ");
                    	for (int i = 0; i < palabras.length; i++) {
                    	    resultado.append(palabras[i]).append(" ");
                    	    if ((i + 1) % 10 == 0) {
                    	        resultado.append("\n");
                    	    }
                    	}
                    }
                }
            }
            String finalResult = recognizer.getFinalResult();
            resultado.append(extraerTexto(finalResult).toUpperCase());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultado.toString().trim();
    }

    private String extraerTexto(String json) {
        Pattern pattern = Pattern.compile("\\\"text\\\" *: *\\\"(.*?)\\\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
}