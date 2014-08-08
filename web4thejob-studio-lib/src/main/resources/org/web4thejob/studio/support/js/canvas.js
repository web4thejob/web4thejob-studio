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

var w4tjStudioCanvas = {
    _desktopId: undefined,
    get desktopId() {
        if (!this._desktopId) {
            this.init();
        }
        return this._desktopId;
    },

    init: function() {
        this._desktopId = zk.Desktop.$().id;
        var designerDesktopId = top.w4tjStudioDesigner.desktopId;

        this.makeWidgetsDroppable();
        this.monitorActivity();

        zAu.send(new zk.Event(zk.Desktop.$(), "onCanvasReady", {
            designerDesktopId: designerDesktopId
        }));
    },

    makeWidgetsDroppable: function() {
        jq('body').find('*').andSelf()
            .on('drop', function(e) {
                e.stopPropagation();
                e.preventDefault();
                jq("[class~=w4tjstudio-hovered]").removeClass("w4tjstudio-hovered");

                var dragged = e.originalEvent.dataTransfer.getData('text');
                var dropped = zk(e.target).$();

                if (dragged && dropped) {
//                    zAu.cmd0.showBusy();
                    zAu.send(new zk.Event(zk.Desktop.$(), "onTemplateDropped", {
                        template: dragged,
                        parent: dropped.uuid
                    }));
                }
            })
            .on('dragover', function(e) {
                e.stopPropagation();
                e.preventDefault();
                var wgt = zk(e.target).$();
                if (wgt) {
                    //unhover others first
                    jq("[class~=w4tjstudio-hovered]").removeClass("w4tjstudio-hovered");
                    jq(wgt).addClass("w4tjstudio-hovered");
                }
            })
            .on('dragleave', function(e) {
                if (zk(e.target).$()) {
                    jq("[class~=w4tjstudio-hovered]").removeClass("w4tjstudio-hovered");
                }
            });
    },

    monitorActivity: function() {
        jq("body").undelegate("*", "click"); /*reset*/
        jq("body").delegate("*", "click", function(e) {
            var wgt = zk(this).$();
            if (wgt) { //select
                if (e.target == this && !jq(wgt.$n()).is(".selected")) {
                    w4tjStudioCanvas.select(wgt.$n());
                }
            }
        });

    },

    select: function(e) {
        var uuid,
            fromServer = typeof e === "string";

        if (!fromServer) {
            var wgt = zk(e).$();
                top.w4tjStudioDesigner.onWidgetSelected(true, {
                    target: wgt.uuid
                });
        } else { //from server
            uuid = e;
        }

        if (!uuid) {
            jq("[class~=w4tjstudio-selected]").removeClass("w4tjstudio-selected");
            return;
        }

        var jqosel = this.tojqo(uuid)
        var isNew = !jq("[class~=w4tjstudio-selected]").is(jqosel);
        jq("[class~=w4tjstudio-selected]").removeClass("w4tjstudio-selected");
        jqosel.addClass("w4tjstudio-selected");
        zAu.cmd0.scrollIntoView(jqosel.get(0).id);

        if (fromServer && isNew) {
            var wgt = zk(uuid).$();
            if (wgt.className.startsWith("zul.layout.") && wgt.$instanceof(zul.layout.LayoutRegion)) return;

            while (wgt) {
                //if the package is not loaded yet an error is raised.
                //this is annoying and undocumented!
                if (wgt.className.startsWith("zul.tab.") && wgt.$instanceof(zul.tab.Tabpanel)) {
                    wgt.getLinkedTab().setSelected(true);
                } else if (wgt.className.startsWith("zul.layout.") && wgt.$instanceof(zul.layout.LayoutRegion)) {
                    wgt.setOpen(true);
                }
                wgt = wgt.parent;
            }
        }

    },

    //fine tunes selection for the real zk widget (e.g. with borderlayout regions)
    tojqo: function(uuid) {
        var jqo = jq("#" + uuid + ">*[id$='-real']");
        if (jqo.length > 0) {
            return jqo;
        }

        jqo = jq("#" + uuid);
        if (jqo.length > 0) {
            return jqo;
        }

        return null;
    },

    //inter-frame communication
    sendToDesigner: function(name, data) {
        top.w4tjStudioDesigner.sendEvent(name, data);
    },
    sendEvent: function(name, data) {
        zAu.send(new zk.Event(zk("$canvas").$(), name, data));
    }


}