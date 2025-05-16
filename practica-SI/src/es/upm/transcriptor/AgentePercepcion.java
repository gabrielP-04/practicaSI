// Paquete base para todos los agentes
package es.upm.transcriptor;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;

// ============================
// Agente de Percepción
// ============================
public class AgentePercepcion extends Agent {
    protected void setup() {
        System.out.println("[Percepcion] Iniciado: " + getLocalName());

        // Registrar servicio
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
                // Simulamos la salida de Vosk (esto se debe reemplazar por llamada real)
                String textoTranscrito = "Hola mundo. Esto es una prueba de transcripcion.";

                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(new AID("procesador", AID.ISLOCALNAME));
                msg.setContent(textoTranscrito);
                send(msg);
                System.out.println("[Percepcion] Texto enviado al procesador.");
            }
        });
    }
}
