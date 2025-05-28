package es.upm.transcriptor;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;

public class AgenteProcesador extends Agent {
    protected void setup() {
        System.out.println("[Procesador] Iniciado: " + getLocalName());

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("procesamiento");
        sd.setName("ServicioProcesamientoTexto");
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
                    String texto = msg.getContent();
                    String procesado = texto;

                    ACLMessage respuesta = new ACLMessage(ACLMessage.INFORM);
                    respuesta.addReceiver(new AID("agUI", AID.ISLOCALNAME));
                    respuesta.setContent(procesado);
                    send(respuesta);

                    System.out.println("[Procesador] Texto enviado a interfaz.");
                } else {
                    block();
                }
            }
        });
    }
}