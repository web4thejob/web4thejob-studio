var w4tjStudioCanvas = {
    _desktopId: undefined,
    get desktopId() {
        if (!this._desktopId) {
            this.init();
        }
        return this._desktopId;
    },

    init: function() {
        this._desktopId=zk.Desktop.$().id;
        var designerDesktopId=top.w4tjStudioDesigner.desktopId;

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

            var dragged=e.originalEvent.dataTransfer.getData('text');
            var dropped=zk(e.target).$();

            if (dragged && dropped) {
                zAu.cmd0.showBusy();
                zAu.send(new zk.Event(zk.Desktop.$(), "onTemplateDropped", {
                    template: dragged,
                    parent: dropped.uuid
                }));
            }
        })
        .on('dragover',function(e){
            e.stopPropagation();
            e.preventDefault();
            var wgt = zk(e.target).$();
            if (wgt) {
                //unhover others first
                jq("[class~=w4tjstudio-hovered]").removeClass("w4tjstudio-hovered");
                jq(wgt).addClass("w4tjstudio-hovered");
            }
        })
        .on('dragleave',function(e){
            if (zk(e.target).$()) {
                jq("[class~=w4tjstudio-hovered]").removeClass("w4tjstudio-hovered");
            }
        });
    },

    monitorActivity: function() {
        jq("body").undelegate("*", "click"); /*reset*/
        jq("body").delegate("*", "click", function (e) {
            var wgt = zk(this).$();
            if (wgt) { //select
                if (e.target == this && !jq(wgt.$n()).is(".selected")) {
                    w4tjStudioCanvas.select(wgt.$n());
                }
            }
        });

    },

    select: function (e) {
        var uuid;
        jq("[class~=w4tjstudio-selected]").removeClass("w4tjstudio-selected");
        if (jq(e).length > 0) { //from client
            uuid = zk(e).$().uuid;
            top.zAu.send(new top.zk.Event(top.zk("$designer").$(), "onWidgetSelected", {target: uuid}));

        } else if (jq("#" + e).length > 0) { //from server
            uuid = e;
            //jq("#"+e).addClass("w4tj-selected");
        }

        if (!uuid) return;
        this.tojqo(uuid).addClass("w4tjstudio-selected");
    },

    //fine tunes selection for the real zk widget (e.g. with borderlayout regions)
    tojqo: function(uuid){
        var jqo = jq("#" + uuid + ">*[id$='-real']");
        if (jqo.length > 0) {
            return jqo;
        }

        jqo=jq("#" + uuid);
        if (jqo.length > 0) {
           return jqo;
       }

       return null;
   },

   //inter-frame communication
   sendToDesigner: function(name,data) {
        top.w4tjStudioDesigner.sendEvent(name,data);
   },
   sendEvent: function(name,data){
       zAu.send(new zk.Event(zk("$canvas").$(), name, data));
   }


}