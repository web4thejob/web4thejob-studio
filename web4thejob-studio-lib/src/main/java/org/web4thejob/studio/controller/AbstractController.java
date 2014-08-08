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

package org.web4thejob.studio.controller;

import org.web4thejob.studio.message.Message;
import org.web4thejob.studio.message.MessageEnum;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.web4thejob.studio.message.MessageEnum.*;
import static org.web4thejob.studio.support.StudioUtil.*;
import static org.zkoss.lang.Generics.cast;

/**
 * Created by Veniamin on 10/5/2014.
 */
public abstract class AbstractController extends SelectorComposer<Component> implements Controller {
    private final List<Message> queue = new ArrayList<>();

    private static synchronized void register(Controller controller) {
        synchronized (Executions.getCurrent().getDesktop()) {
            SortedMap<ControllerEnum, Controller> controllers = cast(Executions.getCurrent().getDesktop().getAttribute
                    (ATTR_STUDIO_CONTROLLERS));
            if (controllers == null) {
                Executions.getCurrent().getDesktop().setAttribute(ATTR_STUDIO_CONTROLLERS,
                        controllers = new TreeMap<>());

            }
            controllers.put(controller.getId(), controller);
        }
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        register(this);
        init();
    }

    @Override
    public void publish(MessageEnum id) {
        publish(id, null);
    }

    @Override
    public void publish(MessageEnum id, Object data) {
        queue.add(new Message(id, this, data));
        if (queue.size() > 1) return;

        try {
            while (!queue.isEmpty()) {
                Message message = queue.get(0);
                for (Controller controller : getLocalControllers()) {
                    if (message.isStopPropagation()) break;
                    controller.process(message);
                }
                queue.remove(message);

                if (EVALUATE_ZUL == id) {
                    //ZUL_EVAL_SUCCEEDED will come from the onCanvasReady event
                } else if (EVALUATE_XML == id) {
                    publish(XML_EVAL_SUCCEEDED, data);
                } else if (ATTRIBUTE_CHANGED == id) {
                    publish(EVALUATE_ZUL, ATTRIBUTE_CHANGED);
                } else if (COMPONENT_DETACHED == id) {
                    publish(EVALUATE_ZUL, COMPONENT_DETACHED);
                } else if (ZUL_EVAL_FAILED == id) {
                    publish(COMPONENT_SELECTED);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

            queue.clear();

            if (EVALUATE_ZUL == id) {
                publish(ZUL_EVAL_FAILED, e);
                //ZUL_EVAL_FAILED will come from the onCanvasReady event
                //however I add this here in case the code breaks prior to
                //calling canvasHolder.setSrc
            } else if (EVALUATE_XML == id) {
                publish(XML_EVAL_FAILED, e);
            } else {
                showError(e);
            }

        }
    }

    @Override
    public void process(Message message) {
        //override
    }


    protected void init() throws Exception {
        //override
    }


    @Override
    public int compareTo(Controller o) {
        return this.getId().compareTo(o.getId());
    }


}
