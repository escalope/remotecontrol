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
package es.ucm.fdi.grasia.androidtvremote.gui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.logging.Level;

import es.ucm.fdi.grasia.androidtvremote.agent.AndroidTVControlInterface;
import es.ucm.fdi.grasia.androidtvremote.gui.R;
import es.ucm.fdi.grasia.androidtvremote.agent.AndroidTVControlAgent;
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
import jade.wrapper.StaleProxyException;


public class TVRemoteActivity extends Activity {
    private Logger logger = Logger.getJADELogger(this.getClass().getName());

    private MicroRuntimeServiceBinder microRuntimeServiceBinder;
    private ServiceConnection serviceConnection;

    AndroidTVControlInterface androidTVControlInterface;

    static final int CHAT_REQUEST = 0;
    static final int SETTINGS_REQUEST = 1;

    private MyReceiver myReceiver;

    private String agentId = "AndroidTVRemote1";

    public TVRemoteActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myReceiver = new MyReceiver();

        IntentFilter killFilter = new IntentFilter();
        killFilter.addAction("jade.grasia.smarttvapp.KILL");
        registerReceiver(myReceiver, killFilter);

        IntentFilter showControlsFilter = new IntentFilter();
        showControlsFilter.addAction("jade.grasia.smarttvapp.SHOW_CONTROLS");
        registerReceiver(myReceiver, showControlsFilter);

        SharedPreferences settings = getSharedPreferences(
                "jadeChatPrefsFile", 0);
        String host = settings.getString("defaultHost", "");
        String port = settings.getString("defaultPort", "");

        startAndroidTV(host, port, agentStartupCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(myReceiver != null) {
            unregisterReceiver(myReceiver);
        }

        logger.log(Level.INFO, "Destroy activity!");
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

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            logger.log(Level.INFO, "Received intent " + action);
            if (action.equalsIgnoreCase("jade.grasia.smarttvapp.KILL")) {
                finish();
            }
            if (action.equalsIgnoreCase("jade.grasia.smarttvapp.SHOW_CONTROLS")) {

                try {
                    logger.log(Level.INFO, "agentId = "+agentId);
                    androidTVControlInterface = MicroRuntime.getAgent(agentId)
                            .getO2AInterface(AndroidTVControlInterface.class);
                    logger.log(Level.INFO, "jade.grasia.smarttvapp.SHOW_CONTROLS -> "+androidTVControlInterface);

                } catch (StaleProxyException e) {
                    logger.log(Level.SEVERE, e.getMessage());
                } catch (ControllerException e) {
                    logger.log(Level.SEVERE, e.getMessage());
                }

                setContentView(R.layout.activity_main);
                initButtons();
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
                new Object[]{getApplicationContext()},
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initButtons() {
        Button button = (Button) findViewById(R.id.onOffButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logger.log(Level.INFO, "onOffButton");
                logger.log(Level.INFO, "androidTVControlAgent = "+androidTVControlInterface);
                if(androidTVControlInterface != null) {
                    androidTVControlInterface.powerOnOff();
                }
            }
        });
        button = (Button) findViewById(R.id.button0);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(androidTVControlInterface != null) {
                    androidTVControlInterface.setChannel(0);
                }
            }
        });
        button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(androidTVControlInterface != null) {
                    androidTVControlInterface.setChannel(1);
                }
            }
        });
        button = (Button) findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(androidTVControlInterface != null) {
                    androidTVControlInterface.setChannel(2);
                }
            }
        });
        button = (Button) findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(androidTVControlInterface != null) {
                    androidTVControlInterface.setChannel(3);
                }
            }
        });
        button = (Button) findViewById(R.id.button4);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(androidTVControlInterface != null) {
                    androidTVControlInterface.setChannel(4);
                }
            }
        });
        button = (Button) findViewById(R.id.button5);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(androidTVControlInterface != null) {
                    androidTVControlInterface.setChannel(5);
                }
            }
        });
        button = (Button) findViewById(R.id.button6);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(androidTVControlInterface != null) {
                    androidTVControlInterface.setChannel(6);
                }
            }
        });
        button = (Button) findViewById(R.id.button7);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(androidTVControlInterface != null) {
                    androidTVControlInterface.setChannel(7);
                }
            }
        });
        button = (Button) findViewById(R.id.button8);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(androidTVControlInterface != null) {
                    androidTVControlInterface.setChannel(8);
                }
            }
        });
        button = (Button) findViewById(R.id.button9);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(androidTVControlInterface != null) {
                    androidTVControlInterface.setChannel(9);
                }
            }
        });
    }
}
