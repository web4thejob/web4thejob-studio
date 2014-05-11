var w4tjStudioDesigner = {
    _desktopId: undefined,
    get desktopId() {
        if (!this._desktopId) {
            this.init();
        }
        return this._desktopId;
    },

    _designer: undefined,
    get designer() {
        if (!this._designer) {
            this.init();
        }
        return this._designer;
    },

    init: function() {
        this._designer=zk('$designer').$();
        this._desktopId=zk.Desktop.$().id;
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