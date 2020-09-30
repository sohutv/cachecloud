/*
 * jQuery Input Limiter plugin 1.3.1
 * http://rustyjeans.com/jquery-plugins/input-limiter/
 *
 * Copyright (c) 2009 Russel Fones <russel@rustyjeans.com>
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
!function(a){a.fn.inputlimiter=function(b){var c=a.extend({},a.fn.inputlimiter.defaults,b);a(this);c.boxAttach&&!a("#"+c.boxId).length&&(a("<div/>").appendTo("body").attr({id:c.boxId,"class":c.boxClass}).css({position:"absolute"}).hide(),a.fn.bgiframe&&a("#"+c.boxId).bgiframe());var d=function(b){var d=a(this),e=g(d.val());!c.allowExceed&&e>c.limit&&d.val(h(d.val())),c.boxAttach&&a("#"+c.boxId).css({width:d.outerWidth()-(a("#"+c.boxId).outerWidth()-a("#"+c.boxId).width())+"px",left:d.offset().left+"px",top:d.offset().top+d.outerHeight()-1+"px","z-index":2e3});var f=c.limit-e>0?c.limit-e:0,i=c.remTextFilter(c,f),j=c.limitTextFilter(c);if(c.limitTextShow){a("#"+c.boxId).html(i+" "+j);var k=a("<span/>").appendTo("body").attr({id:"19cc9195583bfae1fad88e19d443be7a","class":c.boxClass}).html(i+" "+j).innerWidth();a("#19cc9195583bfae1fad88e19d443be7a").remove(),k>a("#"+c.boxId).innerWidth()&&a("#"+c.boxId).html(i+"<br />"+j),a("#"+c.boxId).show()}else a("#"+c.boxId).html(i).show()},e=function(b){var d=g(a(this).val());if(!c.allowExceed&&d>c.limit){var e=b.ctrlKey||b.altKey||b.metaKey;if(!e&&b.which>=32&&b.which<=122&&this.selectionStart===this.selectionEnd)return!1}},f=function(){var b=a(this);if(count=g(b.val()),!c.allowExceed&&count>c.limit&&b.val(h(b.val())),c.boxAttach)a("#"+c.boxId).fadeOut("fast");else if(c.remTextHideOnBlur){var d=c.limitText;d=d.replace(/\%n/g,c.limit),d=d.replace(/\%s/g,1===c.limit?"":"s"),a("#"+c.boxId).html(d)}},g=function(b){if("words"===c.limitBy.toLowerCase())return b.length>0?a.trim(b).replace(/\ +(?= )/g,"").split(" ").length:0;var d=b.length,e=b.match(/\n/g);return e&&c.lineReturnCount>1&&(d+=e.length*(c.lineReturnCount-1)),d},h=function(b){return"words"===c.limitBy.toLowerCase()?a.trim(b).replace(/\ +(?= )/g,"").split(" ").splice(0,c.limit).join(" ")+" ":b.substring(0,c.limit)};a(this).each(function(g){var h=a(this);(!b||!b.limit)&&c.useMaxlength&&parseInt(h.attr("maxlength"))>0&&parseInt(h.attr("maxlength"))!=c.limit?h.inputlimiter(a.extend({},c,{limit:parseInt(h.attr("maxlength"))})):(!c.allowExceed&&c.useMaxlength&&"characters"===c.limitBy.toLowerCase()&&h.attr("maxlength",c.limit),h.unbind(".inputlimiter"),h.bind("keyup.inputlimiter",d),h.bind("keypress.inputlimiter",e),h.bind("blur.inputlimiter",f))})},a.fn.inputlimiter.remtextfilter=function(a,b){var c=a.remText;return 0===b&&null!==a.remFullText&&(c=a.remFullText),c=c.replace(/\%n/g,b),c=c.replace(/\%s/g,a.zeroPlural?1===b?"":"s":1>=b?"":"s")},a.fn.inputlimiter.limittextfilter=function(a){var b=a.limitText;return b=b.replace(/\%n/g,a.limit),b=b.replace(/\%s/g,a.limit<=1?"":"s")},a.fn.inputlimiter.defaults={limit:255,boxAttach:!0,boxId:"limiterBox",boxClass:"limiterBox",remText:"%n character%s remaining.",remTextFilter:a.fn.inputlimiter.remtextfilter,remTextHideOnBlur:!0,remFullText:null,limitTextShow:!0,limitText:"Field limited to %n character%s.",limitTextFilter:a.fn.inputlimiter.limittextfilter,zeroPlural:!0,allowExceed:!1,useMaxlength:!0,limitBy:"characters",lineReturnCount:1}}(jQuery);