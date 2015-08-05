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
package es.ucm.fdi.grasia.smarttvapp.mocktv;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import jade.util.Logger;

/**
 * Created by pablo on 20/01/15.
 */
public class MockTVExecutor {

    private Logger logger = Logger.getJADELogger(this.getClass().getName());

    List<ChannelExecutor> simChannels;

    TVProgram.ProgramPeriod cperiod;

    private Context context;
    MockTV television;
    int channelNum = 1;
    boolean on;

    Timer timer;
    TimerTask timerTask;

    public MockTVExecutor(MockTV television, Context context) {
        this.context = context;
        this.television = television;
        this.simChannels = new ArrayList<ChannelExecutor>();
        init();
    }

    public void init() {
        for(TVChannel ch: television.channels) {
            simChannels.add(new ChannelExecutor(ch));
        }
    }

    public void notifyTVStateChanged() {
        Intent broadcast = new Intent();
        broadcast.setAction("jade.grasia.smarttvapp.STATE_CHANGED");
        logger.log(Level.INFO, "notifyTVStateChanged: " + isOn()+", "+getCurrentChannelName()+
                ", "+getCurrentProgramName()+", ads="+thereAreAds());
        context.sendBroadcast(broadcast);
    }

    public void setChannelNum(int channelNum) {
        this.channelNum = channelNum;
        notifyTVStateChanged();
    }

    public String getCurrentProgramName() {
        ChannelExecutor ce = getChannelExecutor(channelNum);
        if(ce != null) {
            return ce.program.name;
        }
        return null;
    }

    public boolean thereAreAds() {
        ChannelExecutor ce = getChannelExecutor(channelNum);
        if(ce != null) {
            return ce.period.ads;
        }
        return false;
    }

    public TVProgram.ProgramPeriod getCurrentProgramPeriod() {
        ChannelExecutor ce = getChannelExecutor(channelNum);
        if(ce != null) {
            return ce.period;
        }
        return null;
    }

    private ChannelExecutor getChannelExecutor(int channelNum) {
        for(ChannelExecutor ce: simChannels) {
            if(ce.channel.number == channelNum) {
                return ce;
            }
        }
        return null;
    }

    public void start() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 0, 1000); //
    }

    public void tick() {
        for(ChannelExecutor ce: simChannels) {
            ce.tick();
        }
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {
                tick();
                TVProgram.ProgramPeriod period = getCurrentProgramPeriod();
                if(period != cperiod) {
                    cperiod = period;
                    notifyTVStateChanged();
                }
            }
        };
    }

    public String getCurrentChannelName() {
        ChannelExecutor ce = getChannelExecutor(channelNum);
        if(ce != null) {
            return ce.channel.name;
        }
        return null;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
        notifyTVStateChanged();
    }

    public MockTV getTelevision() {
        return television;
    }
}
