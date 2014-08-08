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

package org.web4thejob.studio.message;

import org.web4thejob.studio.controller.Controller;

import java.util.Map;

import static org.zkoss.lang.Generics.cast;

/**
 * Created by e36132 on 14/5/2014.
 */
public class Message {
    public Message(MessageEnum id, Controller sender) {
        this.id = id;
        this.sender = sender;
        this.data = null;
    }

    public Message(MessageEnum id, Controller sender, Object data) {
        this.id = id;
        this.sender = sender;
        this.data = data;
    }

    private final MessageEnum id;
    private final Controller sender;
    private final Object data;
    private boolean stopPropagation;

    public MessageEnum getId() {
        return id;
    }

    public Controller getSender() {
        return sender;
    }

    public <T> T getData() {
        return cast(data);
    }

    public <T> T getData(Class<T> clazz) {
        return cast(data);
    }

    public <T> T getData(String key) {
        return cast(((Map) data).get(key));
    }

    public boolean isStopPropagation() {
        return stopPropagation;
    }

    public void setStopPropagation(boolean stopPropagation) {
        this.stopPropagation = stopPropagation;
    }

    @Override
    public String toString() {
        return id.name();
    }


}
