var w4tjStudioDesigner = {
    _desktopId: undefined,
    get desktopId() {
        if (!this._desktopId) {
            this.init();
        }
        return this._desktopId;
    },

    init: function() {
        this._desktopId=zk('$designer').$().desktop.id;
        this.makeTemplatesDraggable();
    },

    makeTemplatesDraggable: function() {
        jq('$templates .z-toolbarbutton').attr('draggable','true')
        .on('dragstart',function(e){
            e.originalEvent.dataTransfer.setData('text',$(this).attr('template'));
         })
        .on('dragend',function(e){
            e.preventDefault()
         });
    }



}