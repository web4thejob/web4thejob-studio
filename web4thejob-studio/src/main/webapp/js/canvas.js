var w4tjStudioCanvas = {
    _desktopId: undefined,
    get desktopId() {
        if (!this._desktopId) {
            this.init();
        }
        return this._desktopId;
    },

    init: function() {
        this._desktopId=zk("$canvas").$().desktop.id;
        var designerDesktopId=top.w4tjStudioDesigner.desktopId;

        zAu.send(new zk.Event(zk("$canvas").$(), "onPairedWithDesigner", {
            designerDesktopId: designerDesktopId
        }));

        this.makeWidgetsDroppable();
    },

    makeWidgetsDroppable: function() {
        $('$canvas').find('*').andSelf().on('drop', function(e) {
            e.stopPropagation();
            e.preventDefault();

            var dragged=e.originalEvent.dataTransfer.getData('text');
            var dropped=zk(e.target).$();

            if (dragged && dropped) {
                zAu.send(new zk.Event(zk("$canvas").$(), "onTemplateDropped", {
                    template: dragged,
                    parent: dropped.uuid
                }));
            }
        })
        .on('dragover',function(e){
            e.preventDefault();
        })
        .on('dragleave',function(e){
            e.preventDefault();
        });
    }



}