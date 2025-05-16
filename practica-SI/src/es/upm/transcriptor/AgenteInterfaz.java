package es.upm.transcriptor;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
public class AgenteInterfaz extends Agent {
    protected void setup() {
        System.out.println("[Interfaz] Iniciado: " + getLocalName());

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("interfaz");
        sd.setName("ServicioVisualizacion");
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
                    System.out.println("[Interfaz] Texto final recibido:\n" + msg.getContent());
                } else {
                    block();
                }
            }
        });
    }
}