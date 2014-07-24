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
    this._contextPath = contextPath;
    this._designer = zk('$designer').$();
    this._desktopId = zk.Desktop.$().id;
    this.makeTemplatesDraggable();

    this.buildToolbar();
    this.fileName = 'Untitled';

    //sends busy when outline item is selected
    zk.override(zul.sel.Treeitem.prototype, 'doSelect_', function() {
      if (this.getTree().id === 'outline') {
        w4tjStudioDesigner.onWidgetSelected(false);
      }
    });
  },

  makeTemplatesDraggable: function() {
    jq('$templates .z-toolbarbutton').attr('draggable', 'true')
      .on('dragstart', function(e) {
        e.originalEvent.dataTransfer.setData('text', $(this).attr('template'));
        w4tjStudioDesigner.makeOutlineDroppable();
      })
      .on('dragend', function(e) {
        e.preventDefault()
      });
  },

  clearAlerts: function() {
    jq('.alert').alert('close');
  },

  alert: function(clazz, title, message, autoclosable, encoded) {
    this.clearAlerts();

    if (!encoded) {
      title = zUtl.encodeXML(title);
      message = zUtl.encodeXML(message);
    }

    var id = zk.Desktop.nextUuid() + '-alert';
    jq('body').css('overflow', 'hidden');
    var a = '<div id="' + id + '" style="white-space:nowrap;position:absolute;top:70%;left:' + jq(window).width() + 'px;z-index:50000;min-width:200px" class="alert alert-' + clazz + ' alert-dismissable mild-shadow"><button type="button" class="close" aria-hidden="true">&times;</button><strong>' + title + '</strong>: ' + message + '</div>';
    id = '#' + id;
    jq('body').append(a);

    jq(id).css('border-left-color', jq(id).css('color')).css('border-left-width', '10px');

    jq(id).click(function() {
      jq(this).fadeOut("slow", function() {
        jq(this).remove();
        jq('.alert').css('top', '+=10');
      });
    });
    jq('.alert').css('top', '-=10');
    jq(id).animate({
      left: jq('body').width() / 2 - jq(id).width() / 2
    }, 1000);

    if (autoclosable)
      setTimeout(function() {
        jq(id).animate({
          left: '-200px',
          opacity: 0
        }, 500, null, function() {
          jq(id).remove()
        });
      }, 5000);

  },

  /*inter-frame communication*/
  sendToCanvas: function(name, data) {
    this.getCanvasFrame().w4tjStudioCanvas.sendEvent(name, data);
  },
  sendEvent: function(name, data) {
    zAu.send(new zk.Event(zk("$designer").$(), name, data));
  },
  clearCanvasBusy: function(uuid) {
    var f = this.getCanvasFrame();
    if (f) {
      f.zAu.cmd0.clearBusy(uuid);
    }
  },
  selectCanvasWidget: function(w) {
    try {
      this.getCanvasFrame().w4tjStudioCanvas.select(w);
    } catch (err) {
      /*suppress*/
    }
  },
  getCanvasFrame: function() {
    var f = frames['canvasHolder']; /*ff*/
    if (!f) {
      f = frames[zk("$canvasHolder").$().uuid];
    }
    return f;
  },

  buildToolbar: function() {
    var e = '<div class="designer-toolbar"> \
    <div class="btn-group btn-xs toolbar-actions" style="padding-right:0px"> \
    <button type="button" class="btn btn-default btn-xs dropdown-toggle toolbar-actions-dropdown" data-toggle="dropdown"> \
    <i class="z-icon-gear"/> Actions \
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

    var actionsHandler = function() {
      var $group = jq(jq('.designer-toolbar .toolbar-actions-dropdown').parent());
      var p = $group.offset();
      var w = $group.outerWidth();
      var h = $group.outerHeight();
      var r = jq(window).width() - (p.left + w);
      zAu.send(new zk.Event(zk("$designer").$(), "onActionsClicked", {
        top: Math.round(p.top + h),
        right: Math.round(r)
      }));
    }
    jq('.designer-toolbar .toolbar-actions').click(actionsHandler);

    var parseDropdownHandler = function() {
      var $group = jq(jq('.designer-toolbar .toolbar-parsezul-dropdown').parent());
      var p = $group.offset();
      var w = $group.outerWidth();
      var h = $group.outerHeight();
      var r = jq(window).width() - (p.left + w);
      zAu.send(new zk.Event(zk("$designer").$(), "onParseZulDropdownClicked", {
        top: Math.round(p.top + h),
        right: Math.round(r)
      }));
    }
    jq('.designer-toolbar .toolbar-parsezul-dropdown').click(parseDropdownHandler);

    jq('.designer-toolbar .toolbar-parsezul').click(function() {
      //            zAu.cmd0.showBusy("Parsing your zul...");
      zAu.send(new zk.Event(zk("$designer").$(), "onParseZulClicked"));
    });

  },

  _fileName: 'Untitled',
  get fileName() {
    return this._fileName;
  },
  set fileName(fileName) {
    this._fileName = fileName;

    var e = jq('$views').find('.designer-file .label a');
    if (!e.length) {
      jq('$views').append('<div class="designer-file"><span class="label label-default"><span class="dot">&#xf111;</span><a target="_blank"/><span class="dot">&#xf111;</span></span></div>');
      e = jq('$views').find('.designer-file .label a');
    }

    var jqo = jq(e);
    jqo.empty();
    jqo.attr("href", this._fileName);

    var s = this._fileName;
    if (s.length > 25) {
      s = "..." + s.substr(s.length - 25);
    }
    jqo.text(s);
  },

  codeSuccessEffect: function() {
    jq('body').css('overflow', 'hidden');
    jq('body').append('<span class="code-succeeded-effect">&#xf164</span>');
    var wgt = jq('.code-succeeded-effect');
    wgt.animate({
      fontSize: 200,
      opacity: 1,
      top: '+150'
    }, 500);
    setTimeout(function() {
      wgt.remove()
    }, 1000);
  },

  monitorCanvasHealth: function() {
    var f = this.getCanvasFrame();
    if (f) {
      setTimeout(function() {
        if (jq("#zk_showBusy").length) {
          var msg = 'Parsing is taking too long.</br> Are you sure your there aren\'t any javascript errors? Click <a href="javascript:;" onclick="zAu.send(new zk.Event(zk(\'$designer\').$(), \'onCanvasHang\'))">here</a> to cancel.';
          w4tjStudioDesigner.alert('warning', 'This is weird', msg, false, true);
          //                    zAu.send(new zk.Event(zk('$designer').$(), 'onCanvasHang'));
        }
      }, 5000); /*5sec tolerance, TODO: make this user configured*/
    }
  },

  decoratePropertyCaptions: function() {
    jq("$properties .badge").remove();
    jq("$properties .z-caption-content").css("float", "left"); /*needed for FF*/
    jq("$properties .z-groupbox").each(function() {
      var $this = jq(this);
      var count = $this.find(".z-row").length;
      var $caption = $this.find(".z-caption");

      var val = 0;
      var inputs = $this.find("input");
      for (var i = 0; i < inputs.length; i++) {
        if ($(inputs[i]).val()) {
          val++;
        }
      }
      val = val + $this.find(".label-success").length;
      if (val > 0) {
        $caption.append('<span class="badge badge-caption">' + val + '</span>');
        var $badge = jq($caption.find(".badge"));
        $badge.css("color", "#3a87ad");
        $badge.css("background-color", "#d9edf7");
        $badge.css("border", "1px solid #A5CDE0");
        $badge.attr("title", val + " out of " + count + " attribute(s) are set").attr("data-toggle", "tooltip").attr("data-placement", "auto left").tooltip();;
      }
    })
  },

  makeOutlineDroppable: function() {
    jq('$outline').find('.z-treerow').each(function() {
      if (w4tjStudioDesigner.hasHandler(this, 'drop')) return;

      jq(this).on('drop', function(e) {
          e.stopPropagation();
          e.preventDefault();
          jq("[class~=w4tjstudio-hovered]").removeClass("w4tjstudio-hovered");

          var dragged = e.originalEvent.dataTransfer.getData('text');
          var dropped = jq(this);

          if (dragged && dropped) {
            //                        zAu.cmd0.showBusy();
            w4tjStudioDesigner.getCanvasFrame().zAu.send(new zk.Event(zk("$canvas").$(), "onTemplateDropped", {
              template: dragged,
              parent: dropped.attr('canvas-uuid')
            }));
          }
        })
        .on('dragover', function(e) {
          e.stopPropagation();
          e.preventDefault();
          jq("[class~=w4tjstudio-hovered]").removeClass("w4tjstudio-hovered");
          jq(this).addClass("w4tjstudio-hovered");
        })
        .on('dragleave', function(e) {
          jq("[class~=w4tjstudio-hovered]").removeClass("w4tjstudio-hovered");
        });
    });
  },

  hasHandler: function(element, event) {
    var ev = jq._data(element, 'events');
    return (ev && ev[event]) ? true : false;
  },

  hookCanvas: function() {
    var canvas = this.getCanvasFrame();

    this.fileName = canvas.location.pathname.split('?')[0];
    if (!canvas.zk) {
      jq(".designer-toolbar button").attr("disabled", "disabled");
      zAu.send(new zk.Event(zk("$designer").$(), "onNonZKPage"));
      return;
    }

    zAu.cmd0.showBusy("Hooking the canvas...");
    var contextURI = zk.Desktop.$().contextURI;
    var canvasHead = jq('head', jq('$canvasHolder').contents());

    this.getCanvasFrame().jq.getScript(contextURI + "/w4tjstudio-support/canvas/scripts")
      .done(function(data, textStatus, jqxhr) {
        canvas.w4tjStudioCanvas.init();
        canvasHead.append(jq('<link rel="stylesheet" type="text/css"/>').attr('href', contextURI + '/w4tjstudio-support/canvas/styles'));

        jq(".designer-toolbar button").removeAttr("disabled");
        zAu.send(new zk.Event(zk("$designer").$(), "onZKPage"));
      })
      .fail(function(jqxhr, settings, exception) {
        w4tjStudioDesigner.alert('danger', 'This is serious', 'Unfortunately canvas was not hooked as expected. Refreshing may fix the problem...', false);
        zAu.cmd0.showBusy("Sorry, something broke :-(");
      });
  },

  refreshCode: function() {
    if (myCodeMirror) {
      myCodeMirror.refresh();
      try {
        myCodeMirror.setCursor(myCodeMirror.lastCursorPosition);
        myCodeMirror.scrollIntoView(myCodeMirror.lastCursorPosition, Math.round(jq(".CodeMirror-scroll").height() / 2));
        myCodeMirror.focus();
      } catch (err) {
        //suppress
      }
    }
  },

  centerOutlineSelection: function() {
    var selected = zk("$outline").$().getSelectedItem();
    if (selected) {
      var treebody = jq(jq("$outline").find(".z-tree-body"));
      treebody.scrollTop(0);
      treebody.animate({
        "scrollTop": jq(selected).offset().top - Math.round(treebody.height() / 2)
      });
    }
  },

  onWidgetSelected: function(sendToServer, data) {
    jq(".designer-toolbar .open").removeClass("open");
    var actionsMenupopup = zk("$actionsMenupopup").$();
    if (actionsMenupopup) actionsMenupopup.close();

    var parsePopup = zk("$parseMenupopup").$();
    if (parsePopup) parsePopup.close();

    this.propertyEditorBusy();

    if (sendToServer)
      zAu.send(new zk.Event(zk("$designer").$(), "onWidgetSelected", data));

  },

  propertyEditorBusy: function() {
    var w = zk("$propertyeditor").$();

    //prevents a problem of unremovable mask when user double clicks quickly on widget
    if (typeof w.effects_.showBusy === "object") return;

    var r = jq(w).find('#' + w.uuid + '-real');
    w.effects_.showBusy = new zk.eff.Mask({
      id: w.uuid + '-shby',
      anchor: r,
      message: 'Refreshing...'
    });
  },

  showPopover: function(id, text, sclass, autoclose) {
    this.hidePopovers();
    jq(id).popover({
      placement: "auto bottom",
      content: text,
      container: "body",
      trigger: "manual",
      html: true
    });
    var p = jq(id).popover("show");

    if (sclass) {
      p.data("bs.popover").$tip.addClass(sclass);
    }

    if (autoclose)
      setTimeout(function() {
        jq(id).popover("destroy")
      }, 5000);
  },

  hidePopovers: function() {
    jq(".popover").remove();
  },

  prepareEntityToolbox: function(id) {
    var e = jq('$' + id);
    var wgt = zk(e).$();
    e.resizable({
      stop: function(event, ui) {
        wgt.setHeight(Math.round(ui.size.height) + "px")
      }
    });
    e.find('.panel-title').prepend('<i class="fa fa-database" style="margin-right:5px"></i>');
    e.find('.panel-heading .btn-group').append('<i class="toolbox-roll z-icon-caret-up"></i><i class="toolbox-close z-icon-times" style="padding-left:10px">');
    e.find('.toolbox-close').click(function() {
//      zAu.send(new zk.Event(wgt, "onToolboxClose", {
//        target: wgt.uuid
//      }));
      wgt.detach();
    });
    e.find('.toolbox-roll').click(function() {
      var rollbtn = e.find(".toolbox-roll");
      if (wgt.isOpen()) {
        e.resizable('disable');
        wgt.setHeight();
        wgt.setOpen(false);
        rollbtn.removeClass("z-icon-caret-up");
        rollbtn.addClass("z-icon-caret-down")
      } else {
        e.resizable('enable');
        wgt.setOpen(true);
        rollbtn.removeClass("z-icon-caret-down");
        rollbtn.addClass("z-icon-caret-up")
      }
    });

    //draggable bind types
    e.find('.jpa-bindtype').attr('draggable', 'true')
          .on('dragstart', function(e) {
            e.originalEvent.dataTransfer.setData('text', $(this).attr('bind-data'));
                jq('$properties').find('.z-row').each(function() {
                    w4tjStudioDesigner.prepareForBindingDrop(this);
                });
          })
          .on('dragend', function(e) {
            e.preventDefault()
          });
  },

   prepareForBindingDrop: function(target) {
       if (w4tjStudioDesigner.hasHandler(target, 'drop')) return;

       jq(target).on('drop', function(e) {
           e.stopPropagation();
           e.preventDefault();
           jq("[class~=w4tjstudio-hovered]").removeClass("w4tjstudio-hovered");

           var dragged = e.originalEvent.dataTransfer.getData('text');
           var dropped = jq(target);

           if (dragged && dropped) {
                zAu.send(new zk.Event(zk("$designer").$(), "onBindingDroppped", {
                   binding: dragged,
                   property: dropped.attr('w4tjstudio-property')
               }));
           }
         })
         .on('dragover', function(e) {
           e.stopPropagation();
           e.preventDefault();
           jq("[class~=w4tjstudio-hovered]").removeClass("w4tjstudio-hovered");
           jq(target).addClass("w4tjstudio-hovered");
         })
         .on('dragleave', function(e) {
           jq("[class~=w4tjstudio-hovered]").removeClass("w4tjstudio-hovered");
         });
   }


}