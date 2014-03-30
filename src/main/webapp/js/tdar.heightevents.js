/**
 * $.documentHeightEvents plugin - a static jquery plugin that monitors the document height and fires event when height changes
 * 
 * usage:
 *      $.documentHeightEvents()  //start listening w/ default values
 *      $.documentHeightEvents(options)    //start listening,  w/ options:
 *          -interval:  time between polling for changes
 *          -eventName: name of the custom event that the plugin triggers.  
 *      $.documentHeightEvents("destroy")  //turn off monitor 
 * 
 */
(function ($) {
    "use strict";

    var  _cbid  = 0;
    var _active = true;
    var _settings = {};
    var _defaults = {
            interval: 1000,
            eventName: "heightchange",
            dataProp:  "dhe_height"
    };
    
    var $doc = $(document);
    var _oldheight = function() {
        return $doc.data("dhe_height");
    }
    
    var _poll = function(options) {
        if(!_active) return;
        
        var height = $doc.height();
        var oldheight = _oldheight();
        //console.log("polling height:  old:%s   new:%s", oldheight, height);
        if(height !== oldheight) {
            $doc.trigger(_settings.eventName, [height, oldheight]);
            $doc.data(_settings.dataProp, height);
        }
    };


    //FIXME: migrate to TDAR.common
    //FIXME: instead of polling, listen for events that may change size (e.g. onresize, onorientationchange, textarea.mouseup).
    $.extend({
        /**
         * Begin polling for window height changes.  If the plugin detects a change,  fire a custom  event.
         * - calling with no arguments initiates the plugin with all-default options
         * @param arg it depends...
         *
         *  if arg is an  object, the object specifies initialization options:
         *          - interval: polling interval in milliseconds (default: 1000)
         *          - eventName:  name of custom event to fire (default: 'heightchange')
         *          - dataProp:  name of the data-attribute that stores the last-recorded height //FIXME: Adam - is this correct? If so a closure might work better than exposing this value.
         *
         *  if arg is string, the specifies method name:
         *          - "disable"  temporarily stop polling
         *          - "enable" resume polling
         *          - "destroy" stop polling permanently
         *
         *
         */
        "documentHeightEvents": function(arg) {
            
            var _init = function(options) {
                _settings = $.extend({}, _defaults, options);
                console.log("init(%s)", options);
                $doc.data(_settings.dataProp, $doc.height());
                _cbid = window.setInterval(_poll, _settings.interval);
            };

            var _disable = function() {
                _active = false;
            }
            
            var _enable = function() {
                _active = true;
            };
            
            var _destroy = function() {
                if(typeof _cbid === 'undefined') return
                window.clearInterval(_cbid);
                _cbid = undefined;
            };
            
            if(typeof arg === "object") {
                _init(arg);
            } 
            else if (typeof arg === "string") {
                var fn = {
                            "init": _init,
                            "disable": _disable,
                            "enable": _enable,
                            "destroy": _destroy
                        }[arg];
               if(fn){
                   //console.log("about to call %s() [%s]", arg, fn);
                   fn();
               } else {
                   throw "invalid argument: " + arg;
               }
            } else {
                _init();
            }
        }
    });
    
})(jQuery);