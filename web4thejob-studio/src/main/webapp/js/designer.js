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

        this.buildToolbar();
        this.fileName='Untitled';
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
        var a='<div id="'+id+'" style="white-space:nowrap;position:absolute;top:70%;left:'+jq(window).width()+'px;z-index:10000;min-width:200px" class="alert alert-'+clazz+' alert-dismissable mild-shadow"><button type="button" class="close" aria-hidden="true">&times;</button><strong>'+zUtl.encodeXML(title)+'</strong> '+zUtl.encodeXML(message)+'.</div>';
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
                jq(id).animate({left:'-200px',opacity:0},500,null,function(){jq(id).remove()});
            },5000);

    },

    //inter-frame communication
    sendToCanvas: function(name,data) {
        frames[zk("$canvasHolder").$().uuid].w4tjStudioCanvas.sendEvent(name,data);
    },
    sendEvent: function(name,data){
        zAu.send(new zk.Event(zk("$designer").$(), name, data));
    },
    clearCanvasBusy: function(uuid) {
        frames[zk("$canvasHolder").$().uuid].zAu.cmd0.clearBusy(uuid);
    },

    buildToolbar: function(){
        var e='<div class="designer-toolbar"> \
        <div class="btn-group btn-xs" style="padding-right:0px"> \
        <button type="button" class="btn btn-default btn-xs toolbar-actions"> \
        <i class="z-icon-gear"/> Actions \
        </button> \
        <button type="button" class="btn btn-default btn-xs toolbar-actions-dropdown"> \
        <span class="caret"></span> \
        </button> \
        </div> \
        <div class="btn-group btn-xs" style="padding-left:0px"> \
        <button type="button" class="btn btn-primary btn-xs toolbar-parsezul"> \
        <i class="z-icon-refresh"/> Parse zul \
        </button> \
        <button type="button" class="btn btn-primary btn-xs toolbar-parsezul-dropdown"> \
        <span class="caret"></span> \
        </button> \
        </div> \
        </div>';
        jq('$views').append(e);

        var actionsHandler=function() {
            var $group=jq(jq('.designer-toolbar .toolbar-actions').parent());
            var p=$group.offset();
            var w=$group.outerWidth()+2;
            var h=$group.height();
            var r=jq(window).width() - (p.left + w);
            zAu.send(new zk.Event(zk("$designer").$(), "onActionsClicked",{top:p.top+h,right:r}));
        }
        jq('.designer-toolbar .toolbar-actions').click(actionsHandler);
        jq('.designer-toolbar .toolbar-actions-dropdown').click(actionsHandler);
    },

    _fileName: 'Untitled',
    get fileName() {
        return this._fileName;
    },
    set fileName(fileName) {
        this._fileName=fileName;

        var e=jq('$views').find('.designer-file .label');
        if (!e.length) {
            jq('$views').append('<div class="designer-file"><span class="label label-default"/></div>');
            e=jq('$views').find('.designer-file .label');
        }
        jq(e).empty();
        jq(e).text(this._fileName);
        jq(e).prepend('<span style="margin-right:5px">&#xf111;</span>');
        jq(e).append('<span style="margin-left:5px">&#xf111;</span>');
     }


}