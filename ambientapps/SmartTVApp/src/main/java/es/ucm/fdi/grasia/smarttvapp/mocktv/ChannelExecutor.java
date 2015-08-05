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

/**
 * Created by pablo on 19/01/15.
 */
public class ChannelExecutor {
    TVChannel channel;

    TVProgram program;
    TVProgram.ProgramPeriod period;
    int iprogram = 0;
    int iperiod = 0;
    int duration = 0;

    public ChannelExecutor(TVChannel channel) {
        this.channel = channel;
        program = nextProgram();
        period = nextProgramPeriod();
        duration = period.duration;
    }

    public void tick() {
        duration--;
        if(duration <= 0) {
            period = nextProgramPeriod();
            if(period == null) {
                program = nextProgram();
                if(program != null) {
                    iperiod = 0;
                    period = nextProgramPeriod();
                } else {
                    // No more prgramation! Restart
                    iprogram = 0;
                    iperiod = 0;
                }
            }
            if(period != null) {
                duration = period.duration;
            }
        }
    }

    private TVProgram.ProgramPeriod nextProgramPeriod() {
        TVProgram.ProgramPeriod result = null;
        if(iperiod < program.periods.size()) {
            result = program.periods.get(iperiod);
            iperiod++;
        }
        return result;
    }

    private TVProgram nextProgram() {
        TVProgram result = null;
        if(iprogram < channel.programation.size()) {
            result = channel.programation.get(iprogram);
            iprogram++;
        }
        return result;
    }
}
