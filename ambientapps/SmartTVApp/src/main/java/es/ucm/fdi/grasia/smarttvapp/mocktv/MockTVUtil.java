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

import java.util.Random;
import java.util.logging.Level;

import jade.util.Logger;

/**
 * Created by pablo on 19/01/15.
 */
public class MockTVUtil {
    private static Logger logger = Logger.getJADELogger(MockTVUtil.class.getName());

    public static MockTV createMockTV(int minAd, int maxAd, int minProg, int maxProg) {
        Random rand = new Random();
        MockTV tv = new MockTV();

        int maxAdsDuration = maxAd - minAd;
        int maxProgDuration = maxProg - minProg;

        for(int ci = 1; ci <= 9; ci++) {
            TVChannel channel = new TVChannel("Channel"+ci, ci);

            for(int pi = 0; pi < 10; pi++) {
                TVProgram program = new TVProgram("Prog"+ci+""+pi, TVProgram.ProgramType.SERIES);
                boolean isAds = false;
                for(int ai = 0; ai < 5; ai++) {
                    int duration = 0;
                    if(isAds) {
                        duration = rand.nextInt(maxAdsDuration) + minAd;
                    } else {
                        duration = rand.nextInt(maxProgDuration) + minProg;
                    }
                    program.addPeriod(isAds, duration);
                    isAds = !isAds;
                }
                channel.add(program);
            }
            tv.add(channel);
        }

        return tv;
    }

    public static void printProgramation(MockTV television) {
        for(TVChannel channel: television.channels) {
            logger.log(Level.INFO, channel.toString());
            for(TVProgram program: channel.programation) {
                logger.log(Level.INFO, "\t"+program.toString());
                for(TVProgram.ProgramPeriod period: program.periods) {
                    logger.log(Level.INFO, "\t\t"+period.toString());
                }
            }
        }
    }
}
