/*
 * Copyright (C) 2014 Pablo Campillo-Sanchez <pabcampi@ucm.es>
 *
 * This software has been developed as part of the 
 * SociAAL project directed by Jorge J. Gomez Sanz
 * (http://grasia.fdi.ucm.es/sociaal)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.ucm.fdi.grasia.smarttvapp.agent;

import android.content.Context;
import android.content.Intent;

import java.util.logging.Level;

import es.ucm.fdi.grasia.smarttvapp.mocktv.MockTVExecutor;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;
import jade.util.leap.Iterator;
import jade.util.leap.Set;

/**
 * Created by pablo on 19/01/15.
 */
public class AndroidTVControlAgent extends Agent implements AndroidTVControlInterface {

    private static final long serialVersionUID = 1594371294421614291L;

    private Logger logger = Logger.getJADELogger(this.getClass().getName());

    private static final String TV_CONTROL_ID = "__control-tv__";

    private ACLMessage spokenMsg;

    private Context context;

    MockTVExecutor mockTVExecutor;

    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            if (args[0] instanceof Context) {
                context = (Context) args[0];
            }
            if(args.length > 1) {
                mockTVExecutor = (MockTVExecutor) args[1];
            }
        }

        // Add initial behaviours
        addBehaviour(new TVCommandListener(this));

        // Initialize the message used to convey spoken sentences
        spokenMsg = new ACLMessage(ACLMessage.INFORM);
        spokenMsg.setConversationId(TV_CONTROL_ID);

        // Activate the GUI
        registerO2AInterface(AndroidTVControlInterface.class, this);

        Intent broadcast = new Intent();
        broadcast.setAction("jade.demo.chat.SHOW_CHAT");
        logger.log(Level.INFO, "Sending broadcast " + broadcast.getAction());
        context.sendBroadcast(broadcast);
    }

    protected void takeDown() {
    }

    /**
     * Inner class ChatListener. This behaviour registers as a chat participant
     * and keeps the list of participants up to date by managing the information
     * received from the ChatManager agent.
     */
    class TVCommandListener extends CyclicBehaviour {
        private static final long serialVersionUID = 741233963737842521L;
        private MessageTemplate template = MessageTemplate
                .MatchConversationId(TV_CONTROL_ID);

        TVCommandListener(Agent a) {
            super(a);
        }

        public void action() {
            ACLMessage msg = myAgent.receive(template);
            if (msg != null) {
                if (msg.getPerformative() == ACLMessage.REQUEST) {
                    String content = msg.getContent();
                    if(content.equals("ON-OFF")) {
                        powerOnOff();
                    } else if(content.charAt(0) == 'C') {
                        int number = Integer.parseInt(content.substring(1));
                        if(number > 0 && number <= mockTVExecutor.getTelevision().numChannels()) {
                            setChannel(number);
                        }
                    }
                } else {
                    //handleUnexpected(msg);
                }
            } else {
                block();
            }
        }
    } // END of inner class ChatListener

    // ///////////////////////////////////////
    // Methods called by the interface
    // ///////////////////////////////////////

    @Override
    public int getNumChannels() {
        return mockTVExecutor.getTelevision().numChannels();
    }

    @Override
    public void setChannel(int numChannel) {
        mockTVExecutor.setChannelNum(numChannel);
    }

    @Override
    public void powerOnOff() {
        mockTVExecutor.setOn(!mockTVExecutor.isOn());
    }
}
