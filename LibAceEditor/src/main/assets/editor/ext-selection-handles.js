ace.define("ace/ext/selection-handles",["require","exports","module","ace/editor"], function(require, exports, module) {

// Selection handles
var SelectionHandles = function(editor, conf) {
    var DEFAULT_HANDLE_COLOR = "#4285f4";
    var DEFAULT_HANDLE_SIZE = 20;

    var dom = require("../lib/dom");
    var aceRange = require("ace/range");
    this.$editor = editor;
    this.$conf = conf;
    var self = this;
    var drag = null;
    var lastRange = null;
    var padding = null;
    var $_defaultHandlers = null;

    registerContainerEvents();
    var leftHandle = createHandle('left');
    var rightHandle = createHandle('right');

    if (window["MutationObserver"]) {
        this.$contentObserver = new MutationObserver(function(mutations) {
            mutations.forEach(function(mutationRecord) {
                onDOMStyleChanged();
            });
        })
    } else {
        this.onDOMAttrModified = function(e) {
            onDOMStyleChanged();
        }
    }
    var t0 = 0;
    function onDOMStyleChanged() {
        if (!drag) {
            clearTimeout(t1);
            t1 = setTimeout(function(){
                hideHandles();
                self.$editor._emit("changeSelection");
            }, 50);
        }
    }

    var t1 = 0;
    this.onChange = function() {
        if (!drag) {
            clearTimeout(t1);
            t1 = setTimeout(function(){
                var hasSelection = !editor.session.selection.getRange().isEmpty();
                if (hasSelection) {
                    showHandles();
                } else {
                    hideHandles();
                }
            }, 100);
        }
    };
    this.onScroll = function() {
        hideHandles();
        self.$editor._emit("changeSelection");
    };

    function showHandles() {
        var selection = editor.container.getElementsByClassName('ace_selection');
        if (selection.length > 0) {
            var gutter = styleToSize(editor.container.getElementsByClassName('ace_gutter-layer')[0].style.width);
            var textPadding = styleToSize(editor.container.getElementsByClassName('ace_text-layer')[0].style.paddingLeft);
            var padding = gutter + textPadding;
            var content = editor.container.getElementsByClassName('ace_content')[0];
            var scrollTopPadding = styleToSize(content.style.marginTop);
            var scrollLeftPadding = styleToSize(content.style.marginLeft);
            var start = selectionStart(selection);
            var end = selectionEnd(selection);
            var handleSize = (self.$conf.size) ? self.$conf.size : DEFAULT_HANDLE_SIZE;
            var hideBelow = gutter - handleSize;

            leftHandle.style.top = scrollTopPadding + (styleToSize(start.style.top) + styleToSize(start.style.height)) + 'px';
            var pos = scrollLeftPadding + (padding - handleSize - (handleSize/4)) + styleToSize(start.style.left);
            leftHandle.style.left = pos + 'px';
            leftHandle.style.display = pos < hideBelow ? 'none' : 'block';

            rightHandle.style.top = scrollTopPadding + (styleToSize(end.style.top) + styleToSize(end.style.height)) + 'px';
            pos = scrollLeftPadding + (padding - (handleSize/4))  + styleToSize(end.style.left) + styleToSize(end.style.width);
            if (pos > (screen.width - handleSize)) {
                pos = screen.width - handleSize;
            }
            rightHandle.style.left = pos + 'px';
            rightHandle.style.display = pos < gutter ? 'none' : 'block';
        }
    };

    function hideHandles() {
        leftHandle.style.display = 'none';
        rightHandle.style.display = 'none';
    };

    function styleToSize(v) {
        return parseInt(v.toLowerCase().trim().replace("px", ""));
    };


    function createHandle(name) {
        var div = dom.createElement("div");
        div.handle = name;
        div.style.cssText = 'position: absolute; z-index: 99; top: 0px; left:0px; display: none; cursor: default;';

        var handleSize = (self.$conf.size) ? self.$conf.size : DEFAULT_HANDLE_SIZE;
        if (self.$conf.handles) {
            var img = dom.createElement("img");
            img.src = (name == 'left') ? self.$conf.handles.left : self.$conf.handles.right;
            img.style.cssText = 'width: ' + handleSize + 'px; height: ' + handleSize + 'px';
            div.appendChild(img);
        } else {
            // Create default svg for the handle
            var color = (self.$conf.color) ? self.$conf.color : DEFAULT_HANDLE_COLOR;
            var xmlns = "http://www.w3.org/2000/svg";
            var svg = document.createElementNS (xmlns, "svg");
            svg.setAttributeNS (null, "viewBox", "0 0 24 24");
            svg.setAttributeNS (null, "width", handleSize);
            svg.setAttributeNS (null, "height", handleSize);
            svg.setAttributeNS (null, "version", "1.1");
            var g = document.createElementNS (xmlns, "g");
            svg.appendChild (g);
            g.setAttributeNS (null, 'transform', 'translate(0,-72)');
            var path = document.createElementNS (xmlns, "path");
            path.setAttributeNS (null, 'fill', color);
            if (name == 'left') {
                path.setAttributeNS (null, 'd', "m 23.953019297,84.002795889 c 0.01312782,6.61836066 -5.365251948,11.98362563 "
                + "-11.98362563,11.98362563 -6.6183736799,0 -11.98362562899,-5.365251951 -11.98362562899,-11.98362563 "
                + "7.4e-10,-6.61837368 5.36525222639,-11.981711712 11.98362562899,-11.983625628 l 11.959848693,-0.0034585 z");
            } else {
                path.setAttributeNS (null, 'd', "M -0.01420801103,83.987007718 C -0.02731854471,90.596651343 5.3439773713,95.954849728 "
                    + "11.953634,95.954849728 c 6.609656628,0 11.96784201,-5.358185381 11.96784201,-11.96784201 0,-6.609656628 "
                    + "-5.358185659,-11.965930614 -11.96784201,-11.96784201 l -11.94409639013,-0.0034539 z");
            }
            g.appendChild (path);
            div.appendChild(svg);
        }

        // Register dragging listener
        div.addEventListener('mousedown', function(e) {startDragging(e, div)});
        div.addEventListener('touchstart', function(e) {startDragging(e, div)});

        editor.container.appendChild(div);
        return div;
    };

    function registerContainerEvents() {
        editor.container.addEventListener('mousemove', function(e) {dragHandle(e, e.clientX, e.clientY)});
        editor.container.addEventListener('touchmove', function(e) {dragHandle(e, e.touches[0].clientX, e.touches[0].clientY)});
        editor.container.addEventListener('mouseup', function(e) {endDragging(e)});
        editor.container.addEventListener('touchend', function(e) {endDragging(e)});
    };

    function startDragging(e, el) {
        $_defaultHandlers = editor._defaultHandlers;
        editor._defaultHandlers = null;
        drag = el;
        e.preventDefault();
        return false;
    }

    function dragHandle(e, x, y) {
        if (drag) {
            var handleSize = (self.$conf.size) ? self.$conf.size : DEFAULT_HANDLE_SIZE;
            if (x < (padding - handleSize)) {
                return;
            }

            drag.style.left = (x + window.pageXOffset) + 'px';
            drag.style.top = (y + window.pageYOffset) + 'px';
            var range = editor.session.selection.getRange();
            if (drag.handle == 'left') {
                var coords = editor.renderer.screenToTextCoordinates(x - handleSize, Math.max(0, y - (handleSize / 2)));
                range.start.row = coords.row;
                range.start.column = coords.column;
            } else {
                var coords = editor.renderer.screenToTextCoordinates(x, Math.max(0, y - (handleSize / 2)));
                range.end.row = coords.row;
                range.end.column = coords.column;
            }

            var reverse = range.start.row > range.end.row || (range.start.row <= range.end.row && range.start.col < range.end.col);
            if (reverse) {
                range = new aceRange.Range(range.end.row, range.end.column, range.start.row, range.start.column);
                drag = drag.handle == 'left' ? rightHandle : leftHandle;
                showHandles();
            }
            if (lastRange == null || !(range.isEqual(lastRange))) {
                editor.session.selection.setSelectionRange(range, false);
                lastRange = range;
                showHandles();
            }
        }
    }

    function endDragging(e) {
        if (drag) {
            showHandles();
        }
        lastRange = null;
        drag = null;
        if ($_defaultHandlers) {
            editor._defaultHandlers = $_defaultHandlers;
            $_defaultHandlers = null;
        }
    }

    function selectionStart(selection) {
        var count = selection.length;
        for (var i = 0; i < count; i++) {
            if (selection[i].className.indexOf('ace_start') != -1) {
                return selection[i];
            }
        }
        return null;
    };

    function selectionEnd(selection) {
        var count = selection.length;
        var s = null;
        for (var i = 0; i < count; i++) {
            if (s == null) {
                s = selection[i];
                continue;
            }
            var t1 = styleToSize(selection[i].style.top);
            var t2 = styleToSize(s.style.top);
            if (t1 > t2) {
                s = selection[i];
            }
        }
        return s;
    };
};

// Configuration
//
//  selectionHandles: {
//      color: '#4285f4',
//      size: 20,
//      handles: {
//         left: './selection_handle_left.svg',
//         right: './selection_handle_right.svg'
//      }
//  }
//
//  color => handles color
//  size => handles size
//  handles => external image handles.
//

var Editor = require("../editor").Editor;
require("../config").defineOptions(Editor.prototype, "editor", {
    selectionHandles: {
        set: function(conf) {
            if (conf) {
                if (!this.selectionHandles) {
                    this.selectionHandles = new SelectionHandles(this, conf);

                    // register observers and listeners
                    var content = this.selectionHandles.$editor.container.getElementsByClassName('ace_content')[0];
                    if (window["MutationObserver"]) {
                        this.selectionHandles.$contentObserver.observe(content,
                                { attributes : true, attributeFilter : ['style'] });
                    } else {
                        content.addEventListener("DOMNodeInserted",
                                this.selectionHandles.onDOMAttrModified);
                    }
                    this.on("changeSelection", this.selectionHandles.onChange);
                    window.addEventListener("scroll", this.selectionHandles.onScroll);

                }
            } else if (this.selectionHandles) {
                // unregister observers and listeners
                if (window["MutationObserver"]) {
                    this.selectionHandles.$contentObserver.disconnect();
                } else {
                    content.removeEventListener("DOMNodeInserted",
                            this.selectionHandles.onDOMAttrModified);
                }
                this.removeListener("changeSelection", this.selectionHandles.onChange);
                window.removeEventListener("scroll", this.selectionHandles.onScroll);
            }
        }
    }
});

});
(function() {
    ace.require(["ace/ext/selection-handles"], function() {});
})();
