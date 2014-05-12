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

        //bs tooltip example
        jq("#nav-top-navbar a").attr("data-toggle","tooltip").attr("data-placement","bottom").tooltip();
    },

    makeTemplatesDraggable: function() {
        jq('$templates .z-toolbarbutton').attr('draggable','true')
        .on('dragstart',function(e){
            e.originalEvent.dataTransfer.setData('text',$(this).attr('template'));
         })
        .on('dragend',function(e){
            e.preventDefault()
         });
    },


    alert: function(clazz,title,message){
        jq('body').css('overflow','hidden');
        var a='<div style="white-space:nowrap;position:absolute;top:70%;left:'+jq(window).width()+'px;z-index:10000;min-width:200px" class="alert '+clazz+' alert-dismissable"><button type="button" class="close" aria-hidden="true">&times;</button><strong>'+title+'</strong> '+message+'.</div>';
        jq('body').append(a);
        jq('.alert .close').click( function () {
          jq( ".alert" ).fadeOut( "slow",function(){jq( ".alert" ).remove();});
        });
        jq('.alert').animate({left:'40%'},1000);
    },

    //inter-frame communication
    sendToCanvas: function(name,data) {
        frames[zk("$canvasHolder").$().uuid].w4tjStudioCanvas.sendEvent(name,data);
    },
    sendEvent: function(name,data){
        zAu.send(new zk.Event(zk("$designer").$(), name, data));
    }



}