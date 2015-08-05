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

package es.ucm.fdi.grasia.androidtvremote.agent;

import android.content.Context;
import android.content.Intent;
import java.util.logging.Level;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;

/**
 * Created by pablo on 19/01/15.
 */
public class AndroidTVControlAgent extends Agent implements AndroidTVControlInterface {

    private static final long serialVersionUID = 2594371294421614291L;

    private Logger logger = Logger.getJADELogger(this.getClass().getName());

    private static final String TV_CONTROL_ID = "__control-tv__";

    private ACLMessage requestAndroidTV;

    private Context context;

    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            if (args[0] instanceof Context) {
                context = (Context) args[0];
            }
        }

        // Initialize the message used to convey spoken sentences
        requestAndroidTV = new ACLMessage(ACLMessage.REQUEST);
        requestAndroidTV.setConversationId(TV_CONTROL_ID);

        // Activate the GUI
        logger.log(Level.INFO, "registerO2AInterface(AndroidTVControlInterface.class, this);");
        registerO2AInterface(AndroidTVControlInterface.class, this);

        Intent broadcast = new Intent();
        broadcast.setAction("jade.grasia.smarttvapp.SHOW_CONTROLS");
        logger.log(Level.INFO, "Sending broadcast " + broadcast.getAction());
        context.sendBroadcast(broadcast);
    }

    protected void takeDown() {
    }

    /**
     * Inner class ChatSpeaker. INFORMs other participants about a spoken
     * sentence
     */
    private class CommandSender extends OneShotBehaviour {
        private static final long serialVersionUID = -1426033904935339194L;
        private String command;

        private CommandSender(Agent a, String s) {
            super(a);
            command = s;
        }

        public void action() {
            requestAndroidTV.clearAllReceiver();
            requestAndroidTV.addReceiver(new AID("AndroidTV1", AID.ISLOCALNAME));
            requestAndroidTV.setContent(command);
            send(requestAndroidTV);
        }
    } // END of inner class ChatSpeaker

    // ///////////////////////////////////////
    // Methods called by the interface
    // ///////////////////////////////////////

    @Override
    public int getNumChannels() {
        //addBehaviour(new CommandSender(this, ""));
        return 10;
    }

    @Override
    public void setChannel(int numChannel) {
        addBehaviour(new CommandSender(this, "C"+numChannel));
    }

    @Override
    public void powerOnOff() {
        logger.log(Level.INFO, "send ON-OFF message...");
        addBehaviour(new CommandSender(this, "ON-OFF"));
    }
}
