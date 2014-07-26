/* panel-bs.js

	Purpose:
		
	Description:
		
	History:
		Wed, Aug 28, 2013 12:51:51 PM, Created by jumperchen

Copyright (C) 2013 Potix Corporation. All Rights Reserved.
*/
zk.afterLoad('zul.wnd', function () {
	var _panel = {},
		_panelMolds = {};

zk.override(zul.wnd.Panel.molds, _panelMolds, {
	'w4tjstudio': function (out, skipper) {
	var uuid = this.uuid,
		title = this.getTitle(),
		caption = this.caption;

	out.push('<div', this.domAttrs_(), '>');
	if (caption || title) {
		out.push('<div id="', uuid, '-head" class="', this.$s('head'), '">', 
				'<div id="', uuid, '-cap" class="', this.$s('header'), '">');
		if (caption) caption.redraw(out);
		else {
//		    out.push('<h4 class="panel-title pull-left">',zUtl.encodeXML(title),'</h4>');
		    out.push('<h4 class="panel-title pull-left">',title,'</h4>');
		    out.push('<div class="btn-group btn-group-xs pull-right">');
			var	icon = this.$s('icon');
			if (this._maximizable) {
				var maxd = this._maximized;
				out.push('<a id="', uuid, '-max" class="', icon, ' ', this.$s('maximize'));
				if (maxd)
					out.push(' ', this.$s('maximized'));
				var maxIcon = maxd ? this.getMaximizedIconClass_() : this.getMaximizableIconClass_();
				out.push('" style="padding:5px"><i class="', maxIcon, '"></i></a>');
			}
			if (this._minimizable) {
				out.push('<a id="', uuid , '-min" class="', icon, ' ',
						this.$s('minimize'), '" style="padding:5px"><i class="',
						this.getMinimizableIconClass_(), '"></i></a>');
			}
			if (this._collapsible) {
				var openIcon = this._open ? this.getCollapseOpenIconClass_() : this.getCollapseCloseIconClass_();
				out.push('<a id="', uuid , '-exp" class="', icon, ' ',
						this.$s('expand'), '" style="padding:5px"><i class="', openIcon, '"></i></a>');
			}
			if (this._closable) {
				out.push('<a id="', uuid , '-close" class="', icon, ' ',
					this.$s('close'), '" style="padding:5px"><i class="', this.getClosableIconClass_(), '"></i></a>');
			}
			out.push('</div>');

		}
		out.push('</div></div>');
	} 
	
	out.push('<div id="', uuid, '-body" class="', this.$s('body'), '"');
	if (!this._open) 
		out.push(' style="display:none;"');
	out.push('>');
	
	if (!skipper) {
		if (this.tbar) {
			out.push('<div id="', uuid, '-tb" class="', this.$s('top'), '">');
			this.tbar.redraw(out);
			out.push('</div>');
		}
		
		if (this.panelchildren)
			this.panelchildren.redraw(out);
			
		if (this.bbar) {
			out.push('<div id="', uuid, '-bb" class="', this.$s('bottom'), '">');
			this.bbar.redraw(out);
			out.push('</div>');
		}
		
		if (this.fbar) {
			out.push('<div id="', uuid, '-fb" class="', this.$s('footer'), '">');
			this.fbar.redraw(out);
			out.push('</div>');
		}
	}
	
	out.push('</div></div>');
}
});

zk.override(zul.wnd.Panel.prototype, _panel, {
	_inBSMold: function () {
		return this._mold == 'w4tjstudio';
	},
	getSclass: function () {
		if (this._inBSMold()) {
			return this._sclass ? this._sclass : 'panel-default';
		} else
			return _panel.getSclass.apply(this, arguments);
	},
	getZclass: function () {
		if (this._inBSMold())
			return this._zclass != null ? this._zclass : 'panel';
		return _panel.getZclass.apply(this, arguments);
	},
	$s: function (subclass) {
		if (this._inBSMold()) {
			switch (subclass) {
			case 'head':
				subclass = 'heading clearfix';
				break;
			case 'header':
				return '';
				break;
			case 'icon':
			    subclass = 'btn'; //TODO should follow panels semantic class
			    break;
			}
		}
		return _panel.$s.apply(this, arguments);
	}
});

var _panelchildren = {};

zk.override(zul.wnd.Panelchildren.prototype, _panelchildren, {
	_inBSMold: function () {
		return this.parent && this.parent._inBSMold();
	},
	getZclass: function () {
		if (this._inBSMold())
			return this._zclass != null ? this._zclass : '';
		return _panelchildren.getZclass.apply(this, arguments);
	},
	$s: function (subclass) {
		if (this._inBSMold())
			return '';
		return _panelchildren.$s.apply(this, arguments);
	}
});

});