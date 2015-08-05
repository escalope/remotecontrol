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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pablo on 19/01/15.
 */
public class TVProgram {
    enum ProgramType {MOVIE, NEWS, SERIES};

    String name;
    ProgramType programType;
    List<ProgramPeriod> periods;

    public TVProgram(String name, ProgramType programType) {
        this.name = name;
        this.programType = programType;
        this.periods = new ArrayList<ProgramPeriod>();
    }

    public void addPeriod(boolean ads, int duration) {
        periods.add(new ProgramPeriod(ads, duration));
    }

    class ProgramPeriod {
        boolean ads;
        int duration;

        private ProgramPeriod(boolean ads, int duration) {
            this.ads = ads;
            this.duration = duration;
        }

        public String toString() {
            return ads+": "+duration;
        }
    }

    public String toString() {
        return name+": "+programType.name();
    }
}
