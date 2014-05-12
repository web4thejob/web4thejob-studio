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

        this.buildToolbar();
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


    alert: function(clazz,title,message,autoclosable){
        var id=zk.Desktop.nextUuid() + '-alert';
        jq('body').css('overflow','hidden');
        var a='<div id="'+id+'" style="white-space:nowrap;position:absolute;top:70%;left:'+jq(window).width()+'px;z-index:10000;min-width:200px" class="alert '+clazz+' alert-dismissable mild-shadow"><button type="button" class="close" aria-hidden="true">&times;</button><strong>'+title+'</strong> '+message+'.</div>';
        id='#'+id;
        jq('body').append(a);

        jq(id).css('border-left-color',jq(id).css('color')).css('border-left-width','10px');

        jq(id).click( function () {
            jq( this ).fadeOut( "slow",function(){
                jq( this ).remove();
                jq('.alert').css('top','+=10'); 
            });
        });
        jq('.alert').css('top','-=10'); 
        jq(id).animate({left:jq('body').width()/2 - jq(id).width()/2},1000);

        if (autoclosable)
            setTimeout(function(){
                jq(id).animate({left:'-200px',opacity:0},500);
            },5000);

    },

    //inter-frame communication
    sendToCanvas: function(name,data) {
        frames[zk("$canvasHolder").$().uuid].w4tjStudioCanvas.sendEvent(name,data);
    },
    sendEvent: function(name,data){
        zAu.send(new zk.Event(zk("$designer").$(), name, data));
    },

    buildToolbar: function(){
        var a='<div class="z-toolbar z-toolbar-tabs"> \
        <div class="btn-group btn-xs"> \
        <button type="button" class="btn btn-default btn-xs"> \
        <i class="z-icon-refresh"/> Actions \
        </button> \
        <button type="button" class="btn btn-default btn-xs dropdown-toggle" data-toggle="dropdown"> \
        <span class="caret"></span> \
        <span class="sr-only">Toggle Dropdown</span> \
        </button> \
        </div> \
        </div>';
        jq('$views').append(a);
    }



}