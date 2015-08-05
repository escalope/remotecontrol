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
package es.ucm.fdi.grasia.smarttvapp;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by pablo on 18/01/15.
 */
public class AndroidTVControlServer {
    private Handler mUpdateHandler;

    private static final String TAG = "AndroidTVControlServer";

    private Socket mSocket;
    private int mPort = -1;

    ServerSocket mServerSocket = null;
    Thread mThread = null;

    public AndroidTVControlServer(Handler handler) {
        mUpdateHandler = handler;
        mThread = new Thread(new ServerThread());
        mThread.start();
    }

    public void tearDown() {
        mThread.interrupt();
        try {
            mServerSocket.close();
        } catch (IOException ioe) {
            Log.e(TAG, "Error when closing server socket.");
        }
    }

    class ServerThread implements Runnable {

        @Override
        public void run() {

            try {
                // Since discovery will happen via Nsd, we don't need to care which port is
                // used.  Just grab an available one  and advertise it via Nsd.
                mServerSocket = new ServerSocket(0);
                setLocalPort(mServerSocket.getLocalPort());

                while (!Thread.currentThread().isInterrupted()) {
                    Log.d(TAG, "ServerSocket Created, awaiting connection");
                    setSocket(mServerSocket.accept());
                    Log.d(TAG, "Connected.");
                    /*if (mChatClient == null) {
                        int port = mSocket.getPort();
                        InetAddress address = mSocket.getInetAddress();
                        connectToServer(address, port);
                    }*/
                }
            } catch (IOException e) {
                Log.e(TAG, "Error creating ServerSocket: ", e);
                e.printStackTrace();
            }
        }
    }

    public int getLocalPort() {
        return mPort;
    }

    public void setLocalPort(int port) {
        mPort = port;
    }

    private synchronized void setSocket(Socket socket) {
        Log.d(TAG, "setSocket being called.");
        if (socket == null) {
            Log.d(TAG, "Setting a null socket.");
        }
        if (mSocket != null) {
            if (mSocket.isConnected()) {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    // TODO(alexlucas): Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        mSocket = socket;
    }
}
