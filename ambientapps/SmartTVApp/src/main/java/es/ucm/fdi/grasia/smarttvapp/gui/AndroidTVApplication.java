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

import java.util.logging.Level;

import jade.util.Logger;
import android.app.Application;
import android.content.SharedPreferences;

/**
 * Created by pablo on 19/01/15.
 */
public class AndroidTVApplication  extends Application {
    private Logger logger = Logger.getJADELogger(this.getClass().getName());

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences settings = getSharedPreferences("jadeChatPrefsFile", 0);

        String defaultHost = settings.getString("defaultHost", "");
        String defaultPort = settings.getString("defaultPort", "");
        if (defaultHost.isEmpty() || defaultPort.isEmpty()) {
            logger.log(Level.INFO, "Create default properties");
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("defaultHost", "10.0.2.2");
            editor.putString("defaultPort", "1099");
            editor.commit();
        }
    }
}
