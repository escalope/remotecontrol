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
package es.ucm.fdi.grasia.smarttvapp.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.logging.Level;

import es.ucm.fdi.grasia.smarttvapp.AndroidTVControlServer;
import es.ucm.fdi.grasia.smarttvapp.agent.AndroidTVControlAgent;
import es.ucm.fdi.grasia.smarttvapp.mocktv.MockTV;
import es.ucm.fdi.grasia.smarttvapp.mocktv.MockTVExecutor;
import es.ucm.fdi.grasia.smarttvapp.mocktv.MockTVUtil;
import jade.android.AndroidHelper;
import jade.android.MicroRuntimeService;
import jade.android.MicroRuntimeServiceBinder;
import jade.android.RuntimeCallback;
import jade.core.MicroRuntime;
import jade.core.Profile;
import jade.util.Logger;
import jade.util.leap.Properties;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;


public class TVAppActivity extends Activity {
    private Logger logger = Logger.getJADELogger(this.getClass().getName());

    private MicroRuntimeServiceBinder microRuntimeServiceBinder;
    private ServiceConnection serviceConnection;

    static final int CHAT_REQUEST = 0;
    static final int SETTINGS_REQUEST = 1;

    private MyReceiver myReceiver;

    TextView channelNameView;
    TextView programView;
    LinearLayout screenBackground;

    private Handler mUpdateHandler;

    AndroidTVControlServer androidTVControlServer;
    final String agentId = "AndroidTV1";

    MockTV television;
    MockTVExecutor mockTVExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myReceiver = new MyReceiver();

        IntentFilter killFilter = new IntentFilter();
        killFilter.addAction("jade.grasia.smarttvapp.KILL");
        registerReceiver(myReceiver, killFilter);

        IntentFilter showChatFilter = new IntentFilter();
        showChatFilter.addAction("jade.grasia.smarttvapp.STATE_CHANGED");
        registerReceiver(myReceiver, showChatFilter);

        setContentView(R.layout.text_tv);

        SharedPreferences settings = getSharedPreferences(
                "jadeChatPrefsFile", 0);
        String host = settings.getString("defaultHost", "");
        String port = settings.getString("defaultPort", "");

        logger.log(Level.INFO, "Host:port = "+host+":"+port);

        television = MockTVUtil.createMockTV(5, 10, 10, 20);
        MockTVUtil.printProgramation(television);

        mockTVExecutor = new MockTVExecutor(television, getApplicationContext());
        mockTVExecutor.setOn(false);
        mockTVExecutor.setChannelNum(1);
        mockTVExecutor.start();

        channelNameView = (TextView) findViewById(R.id.channelNameView);
        programView= (TextView) findViewById(R.id.programView);
        screenBackground = (LinearLayout) findViewById(R.id.screenBackground);

        startAndroidTV(host, port, agentStartupCallback);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(myReceiver != null) {
            unregisterReceiver(myReceiver);
        }
        if(mockTVExecutor != null) {
            mockTVExecutor.stop();
        }

        logger.log(Level.INFO, "Destroy activity!");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHAT_REQUEST) {
            if (resultCode == RESULT_CANCELED) {
                // The chat activity was closed.
                logger.log(Level.INFO, "Stopping Jade...");
                microRuntimeServiceBinder
                        .stopAgentContainer(new RuntimeCallback<Void>() {
                            @Override
                            public void onSuccess(Void thisIsNull) {
                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                logger.log(Level.SEVERE, "Failed to stop the "
                                        + AndroidTVControlAgent.class.getName()
                                        + "...");
                                agentStartupCallback.onFailure(throwable);
                            }
                        });
            }
        }
    }

    private RuntimeCallback<AgentController> agentStartupCallback = new RuntimeCallback<AgentController>() {
        @Override
        public void onSuccess(AgentController agent) {
        }

        @Override
        public void onFailure(Throwable throwable) {
            logger.log(Level.INFO, "Nickname already in use!");
            //myHandler.postError(getString(R.string.msg_nickname_in_use));
        }
    };

    public void ShowDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(TVAppActivity.this);
        builder.setMessage(message).setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            logger.log(Level.INFO, "Received intent " + action);
            if (action.equalsIgnoreCase("jade.grasia.smarttvapp.KILL")) {
                finish();
            }
            if (action.equalsIgnoreCase("jade.grasia.smarttvapp.STATE_CHANGED")) {
                if(mockTVExecutor.isOn()) {
                    channelNameView.setText(mockTVExecutor.getCurrentChannelName());
                    String programName = mockTVExecutor.getCurrentProgramName();
                    if(mockTVExecutor.thereAreAds()) {
                        screenBackground.setBackgroundColor(Color.YELLOW);
                        programName += "(Ads)";
                    } else {
                        screenBackground.setBackgroundColor(Color.WHITE);
                    }
                    programView.setText(programName);
                } else {
                    screenBackground.setBackgroundColor(Color.BLACK);
                }
            }
        }
    }

    public void startAndroidTV(final String host,
                          final String port,
                          final RuntimeCallback<AgentController> agentStartupCallback) {

        final Properties profile = new Properties();
        profile.setProperty(Profile.MAIN_HOST, host);
        profile.setProperty(Profile.MAIN_PORT, port);
        profile.setProperty(Profile.MAIN, Boolean.FALSE.toString());
        profile.setProperty(Profile.JVM, Profile.ANDROID);

        if (AndroidHelper.isEmulator()) {
            // Emulator: this is needed to work with emulated devices
            profile.setProperty(Profile.LOCAL_HOST, AndroidHelper.LOOPBACK);
        } else {
            profile.setProperty(Profile.LOCAL_HOST,
                    AndroidHelper.getLocalIPAddress());
        }
        // Emulator: this is not really needed on a real device
        profile.setProperty(Profile.LOCAL_PORT, "2000");

        if (microRuntimeServiceBinder == null) {
            serviceConnection = new ServiceConnection() {
                public void onServiceConnected(ComponentName className,
                                               IBinder service) {
                    microRuntimeServiceBinder = (MicroRuntimeServiceBinder) service;
                    logger.log(Level.INFO, "Gateway successfully bound to MicroRuntimeService");
                    startContainer(profile, agentStartupCallback);
                };

                public void onServiceDisconnected(ComponentName className) {
                    microRuntimeServiceBinder = null;
                    logger.log(Level.INFO, "Gateway unbound from MicroRuntimeService");
                }
            };
            logger.log(Level.INFO, "Binding Gateway to MicroRuntimeService...");
            bindService(new Intent(getApplicationContext(),
                            MicroRuntimeService.class), serviceConnection,
                    Context.BIND_AUTO_CREATE);
        } else {
            logger.log(Level.INFO, "MicroRumtimeGateway already binded to service");
            startContainer(profile, agentStartupCallback);
        }
    }

    private void startContainer(Properties profile,
                                final RuntimeCallback<AgentController> agentStartupCallback) {
        if (!MicroRuntime.isRunning()) {
            microRuntimeServiceBinder.startAgentContainer(profile,
                    new RuntimeCallback<Void>() {
                        @Override
                        public void onSuccess(Void thisIsNull) {
                            logger.log(Level.INFO, "Successfully start of the container...");
                            startAgent(agentId, agentStartupCallback);
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            logger.log(Level.SEVERE, "Failed to start the container...");
                        }
                    });
        } else {
            startAgent(agentId, agentStartupCallback);
        }
    }

    private void startAgent(final String nickname,
                            final RuntimeCallback<AgentController> agentStartupCallback) {
        microRuntimeServiceBinder.startAgent(nickname,
                AndroidTVControlAgent.class.getName(),
                new Object[]{getApplicationContext(), mockTVExecutor},
                new RuntimeCallback<Void>() {
                    @Override
                    public void onSuccess(Void thisIsNull) {
                        logger.log(Level.INFO, "Successfully start of the "
                                + AndroidTVControlAgent.class.getName() + "...");
                        try {
                            agentStartupCallback.onSuccess(MicroRuntime
                                    .getAgent(nickname));
                        } catch (ControllerException e) {
                            // Should never happen
                            agentStartupCallback.onFailure(e);
                        }
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        logger.log(Level.SEVERE, "Failed to start the "
                                + AndroidTVControlAgent.class.getName() + "...");
                        agentStartupCallback.onFailure(throwable);
                    }
                });
    }

    @Override
    protected void onPause() {
        // code here
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // code here
    }
}
