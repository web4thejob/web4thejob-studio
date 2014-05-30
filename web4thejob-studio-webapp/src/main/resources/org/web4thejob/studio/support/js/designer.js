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

  init: function(contextPath) {
    this._contextPath=contextPath;
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
      w4tjStudioDesigner.makeOutlineDroppable();
    })
    .on('dragend',function(e){
      e.preventDefault()
    });
  },

  clearAlerts: function(){
    jq('.alert').alert('close');
  },

  alert: function(clazz,title,message,autoclosable,encoded){
    this.clearAlerts();

    if (!encoded) {
      title=zUtl.encodeXML(title);
      message=zUtl.encodeXML(message);
    }

    var id=zk.Desktop.nextUuid() + '-alert';
    jq('body').css('overflow','hidden');
    var a='<div id="'+id+'" style="white-space:nowrap;position:absolute;top:70%;left:'+jq(window).width()+'px;z-index:50000;min-width:200px" class="alert alert-'+clazz+' alert-dismissable mild-shadow"><button type="button" class="close" aria-hidden="true">&times;</button><strong>'+title+'</strong>: '+message+'</div>';
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

  /*inter-frame communication*/
  sendToCanvas: function(name,data) {
    this.getCanvasFrame().w4tjStudioCanvas.sendEvent(name,data);
  },
  sendEvent: function(name,data){
    zAu.send(new zk.Event(zk("$designer").$(), name, data));
  },
  clearCanvasBusy: function(uuid) {
    var f=this.getCanvasFrame();
    if (f) {
      f.zAu.cmd0.clearBusy(uuid);
    }
  },
  selectCanvasWidget: function(w){
    this.getCanvasFrame().w4tjStudioCanvas.select(w);
  },
  getCanvasFrame: function() {
    var f=frames['canvasHolder']; /*ff*/
    if (!f) {
      f=frames[zk("$canvasHolder").$().uuid];
    }
    return f;
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

    jq('.designer-toolbar .toolbar-parsezul').click(function(){
      zAu.cmd0.showBusy("Parsing your zul...");
      zAu.send(new zk.Event(zk("$designer").$(), "onParseZulClicked"));
    });

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
  },

  codeSuccessEffect: function() {
    jq('body').css('overflow','hidden');
    jq('body').append('<span class="code-succeeded-effect">&#xf164</span>');
    var wgt=jq('.code-succeeded-effect');
    wgt.animate({fontSize:200, opacity:1,top:'+150'},500);
    setTimeout(function(){wgt.remove()},1000);
  },

  monitorCanvasHealth: function() {
    var f=this.getCanvasFrame();
    if (f) {
      setTimeout(function(){
        if (jq("#zk_showBusy").length) {
          w4tjStudioDesigner.alert('warning','Too long',"Parsing has been cancelled because it was taking too long.</br> Are you sure your there aren't any javascript errors? Please check and retry.",true,true);
          zAu.send(new zk.Event(zk("$designer").$(), "onCanvasHang"));
        }
      },5000); /*5sec tolerance, TODO: make this user configured*/
    }
  },

  decoratePropertyCaptions: function() {
   jq("$properties .badge").remove();
   jq("$properties .z-caption-content").css("float","left"); /*needed for FF*/
   jq("$properties .z-groupbox").each(function(){
     var $this=jq(this);
     var count=$this.find(".z-row").length;
     var $caption=$this.find(".z-caption");

     var val = 0;
     var inputs = $this.find("input");
     for (var i=0; i < inputs.length; i++){
       if ($(inputs[i]).val()) {
         val++;
       }
     }
     val=val + $this.find(".label-success").length;
     if (val>0){
       $caption.append('<span class="badge badge-caption">' + val + '</span>');
       var $badge=jq($caption.find(".badge"));
       $badge.css("color","#3a87ad");
       $badge.css("background-color","#d9edf7");
       $badge.attr("title",val + " out of " + count + " attribute(s) are set").attr("data-toggle","tooltip").attr("data-placement","auto left").tooltip();;
     }
   })
 },

 makeOutlineDroppable: function(){
  jq('$outline').find('.z-treerow').each(function(){
    if (w4tjStudioDesigner.hasHandler(this,'drop')) return;

    jq(this).on('drop', function(e) {
      e.stopPropagation();
      e.preventDefault();
      jq("[class~=w4tjstudio-hovered]").removeClass("w4tjstudio-hovered");

      var dragged=e.originalEvent.dataTransfer.getData('text');
      var dropped=jq(this);

      if (dragged && dropped) {
        w4tjStudioDesigner.getCanvasFrame().zAu.send(new zk.Event(zk("$canvas").$(), "onTemplateDropped", {
          template: dragged,
          parent: dropped.attr('canvas-uuid')
        }));
      }
    })
    .on('dragover',function(e){
      e.stopPropagation();
      e.preventDefault();
      jq("[class~=w4tjstudio-hovered]").removeClass("w4tjstudio-hovered");
      jq(this).addClass("w4tjstudio-hovered");
    })
    .on('dragleave',function(e){
      jq("[class~=w4tjstudio-hovered]").removeClass("w4tjstudio-hovered");
    });
  });
},

hasHandler: function(element, event) {
  var ev = $._data(element, 'events');
  return (ev && ev[event]) ? true : false;
},

hookCanvas: function() {
    if (!this.getCanvasFrame().zk){
        zAu.send(new zk.Event(zk("$designer").$(), "onNonZKPage"));
        return;
    }

    var contextURI=zk.Desktop.$().contextURI;
    var canvas=this.getCanvasFrame();
    var canvasHead=jq('head',jq('$canvasHolder').contents());

    this.getCanvasFrame().jq.getScript( contextURI+"/w4tjstudio-support/canvas/scripts")
        .done(function(data, textStatus, jqxhr){
            console.log( textStatus );
            canvas.w4tjStudioCanvas.init();
            canvasHead.append( jq('<link rel="stylesheet" type="text/css"/>').attr('href', contextURI+'/w4tjstudio-support/canvas/styles') );
            zAu.send(new zk.Event(zk("$designer").$(), "onZKPage"));
        })
        .fail(function( jqxhr, settings, exception ) {
            w4tjStudioDesigner.alert('danger','This is serious','Unfortunately canvas was not hooked as expected :-(',false);
        });
}


}

