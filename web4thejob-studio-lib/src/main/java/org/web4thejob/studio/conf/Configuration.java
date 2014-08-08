/*
 * Copyright 2014 Veniamin Isaias
 *
 * This file is part of Web4thejob Studio.
 *
 * Web4thejob Studio is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Web4thejob Studio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Web4thejob Studio.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.web4thejob.studio.conf;

import java.io.Serializable;

/**
 * Created by e36132 on 24/6/2014.
 */
public class Configuration implements Serializable {
    private boolean alwaysReturnToCanvas;

    public boolean isAlwaysReturnToCanvas() {
        return alwaysReturnToCanvas;
    }

    public void setAlwaysReturnToCanvas(boolean alwaysReturnToCanvas) {
        this.alwaysReturnToCanvas = alwaysReturnToCanvas;
    }

}
