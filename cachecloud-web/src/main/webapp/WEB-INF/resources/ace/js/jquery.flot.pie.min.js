/* Flot plugin for rendering pie charts.

Copyright (c) 2007-2014 IOLA and Ole Laursen.
Licensed under the MIT license.

The plugin assumes that each series has a single data value, and that each
value is a positive integer or zero.  Negative numbers don't make sense for a
pie chart, and have unpredictable results.  The values do NOT need to be
passed in as percentages; the plugin will calculate the total and per-slice
percentages internally.

* Created by Brian Medendorp

* Updated with contributions from btburnett3, Anthony Aragues and Xavi Ivars

The plugin supports these options:

	series: {
		pie: {
			show: true/false
			radius: 0-1 for percentage of fullsize, or a specified pixel length, or 'auto'
			innerRadius: 0-1 for percentage of fullsize or a specified pixel length, for creating a donut effect
			startAngle: 0-2 factor of PI used for starting angle (in radians) i.e 3/2 starts at the top, 0 and 2 have the same result
			tilt: 0-1 for percentage to tilt the pie, where 1 is no tilt, and 0 is completely flat (nothing will show)
			offset: {
				top: integer value to move the pie up or down
				left: integer value to move the pie left or right, or 'auto'
			},
			stroke: {
				color: any hexidecimal color value (other formats may or may not work, so best to stick with something like '#FFF')
				width: integer pixel width of the stroke
			},
			label: {
				show: true/false, or 'auto'
				formatter:  a user-defined function that modifies the text/style of the label text
				radius: 0-1 for percentage of fullsize, or a specified pixel length
				background: {
					color: any hexidecimal color value (other formats may or may not work, so best to stick with something like '#000')
					opacity: 0-1
				},
				threshold: 0-1 for the percentage value at which to hide labels (if they're too small)
			},
			combine: {
				threshold: 0-1 for the percentage value at which to combine slices (if they're too small)
				color: any hexidecimal color value (other formats may or may not work, so best to stick with something like '#CCC'), if null, the plugin will automatically use the color of the first slice to be combined
				label: any text value of what the combined slice should be labeled
			}
			highlight: {
				opacity: 0-1
			}
		}
	}

More detail and specific examples can be found in the included HTML file.

*/
!function(a){function b(b){function e(b,c,d){x||(x=!0,r=b.getCanvas(),s=a(r).parent(),t=b.getOptions(),b.setData(f(b.getData())))}function f(b){for(var c=0,d=0,e=0,f=t.series.pie.combine.color,g=[],h=0;h<b.length;++h){var i=b[h].data;a.isArray(i)&&1==i.length&&(i=i[0]),a.isArray(i)?!isNaN(parseFloat(i[1]))&&isFinite(i[1])?i[1]=+i[1]:i[1]=0:i=!isNaN(parseFloat(i))&&isFinite(i)?[1,+i]:[1,0],b[h].data=[i]}for(var h=0;h<b.length;++h)c+=b[h].data[0][1];for(var h=0;h<b.length;++h){var i=b[h].data[0][1];i/c<=t.series.pie.combine.threshold&&(d+=i,e++,f||(f=b[h].color))}for(var h=0;h<b.length;++h){var i=b[h].data[0][1];(2>e||i/c>t.series.pie.combine.threshold)&&g.push(a.extend(b[h],{data:[[1,i]],color:b[h].color,label:b[h].label,angle:i*Math.PI*2/c,percent:i/(c/100)}))}return e>1&&g.push({data:[[1,d]],color:f,label:t.series.pie.combine.label,angle:d*Math.PI*2/c,percent:d/(c/100)}),g}function g(b,e){function f(){y.clearRect(0,0,j,k),s.children().filter(".pieLabel, .pieLabelBackground").remove()}function g(){var a=t.series.pie.shadow.left,b=t.series.pie.shadow.top,c=10,d=t.series.pie.shadow.alpha,e=t.series.pie.radius>1?t.series.pie.radius:u*t.series.pie.radius;if(!(e>=j/2-a||e*t.series.pie.tilt>=k/2-b||c>=e)){y.save(),y.translate(a,b),y.globalAlpha=d,y.fillStyle="#000",y.translate(v,w),y.scale(1,t.series.pie.tilt);for(var f=1;c>=f;f++)y.beginPath(),y.arc(0,0,e,0,2*Math.PI,!1),y.fill(),e-=f;y.restore()}}function i(){function b(a,b,c){0>=a||isNaN(a)||(c?y.fillStyle=b:(y.strokeStyle=b,y.lineJoin="round"),y.beginPath(),Math.abs(a-2*Math.PI)>1e-9&&y.moveTo(0,0),y.arc(0,0,e,f,f+a/2,!1),y.arc(0,0,e,f+a/2,f+a,!1),y.closePath(),f+=a,c?y.fill():y.stroke())}function c(){function b(b,c,d){if(0==b.data[0][1])return!0;var f,g=t.legend.labelFormatter,h=t.series.pie.label.formatter;f=g?g(b.label,b):b.label,h&&(f=h(f,b));var i=(c+b.angle+c)/2,l=v+Math.round(Math.cos(i)*e),m=w+Math.round(Math.sin(i)*e)*t.series.pie.tilt,n="<span class='pieLabel' id='pieLabel"+d+"' style='position:absolute;top:"+m+"px;left:"+l+"px;'>"+f+"</span>";s.append(n);var o=s.children("#pieLabel"+d),p=m-o.height()/2,q=l-o.width()/2;if(o.css("top",p),o.css("left",q),0-p>0||0-q>0||k-(p+o.height())<0||j-(q+o.width())<0)return!1;if(0!=t.series.pie.label.background.opacity){var r=t.series.pie.label.background.color;null==r&&(r=b.color);var u="top:"+p+"px;left:"+q+"px;";a("<div class='pieLabelBackground' style='position:absolute;width:"+o.width()+"px;height:"+o.height()+"px;"+u+"background-color:"+r+";'></div>").css("opacity",t.series.pie.label.background.opacity).insertBefore(o)}return!0}for(var c=d,e=t.series.pie.label.radius>1?t.series.pie.label.radius:u*t.series.pie.label.radius,f=0;f<m.length;++f){if(m[f].percent>=100*t.series.pie.label.threshold&&!b(m[f],c,f))return!1;c+=m[f].angle}return!0}var d=Math.PI*t.series.pie.startAngle,e=t.series.pie.radius>1?t.series.pie.radius:u*t.series.pie.radius;y.save(),y.translate(v,w),y.scale(1,t.series.pie.tilt),y.save();for(var f=d,g=0;g<m.length;++g)m[g].startAngle=f,b(m[g].angle,m[g].color,!0);if(y.restore(),t.series.pie.stroke.width>0){y.save(),y.lineWidth=t.series.pie.stroke.width,f=d;for(var g=0;g<m.length;++g)b(m[g].angle,t.series.pie.stroke.color,!1);y.restore()}return h(y),y.restore(),t.series.pie.label.show?c():!0}if(s){var j=b.getPlaceholder().width(),k=b.getPlaceholder().height(),l=s.children().filter(".legend").children().width()||0;y=e,x=!1,u=Math.min(j,k/t.series.pie.tilt)/2,w=k/2+t.series.pie.offset.top,v=j/2,"auto"==t.series.pie.offset.left?(t.legend.position.match("w")?v+=l/2:v-=l/2,u>v?v=u:v>j-u&&(v=j-u)):v+=t.series.pie.offset.left;var m=b.getData(),n=0;do n>0&&(u*=d),n+=1,f(),t.series.pie.tilt<=.8&&g();while(!i()&&c>n);n>=c&&(f(),s.prepend("<div class='error'>Could not draw pie with labels contained inside canvas</div>")),b.setSeries&&b.insertLegend&&(b.setSeries(m),b.insertLegend())}}function h(a){if(t.series.pie.innerRadius>0){a.save();var b=t.series.pie.innerRadius>1?t.series.pie.innerRadius:u*t.series.pie.innerRadius;a.globalCompositeOperation="destination-out",a.beginPath(),a.fillStyle=t.series.pie.stroke.color,a.arc(0,0,b,0,2*Math.PI,!1),a.fill(),a.closePath(),a.restore(),a.save(),a.beginPath(),a.strokeStyle=t.series.pie.stroke.color,a.arc(0,0,b,0,2*Math.PI,!1),a.stroke(),a.closePath(),a.restore()}}function i(a,b){for(var c=!1,d=-1,e=a.length,f=e-1;++d<e;f=d)(a[d][1]<=b[1]&&b[1]<a[f][1]||a[f][1]<=b[1]&&b[1]<a[d][1])&&b[0]<(a[f][0]-a[d][0])*(b[1]-a[d][1])/(a[f][1]-a[d][1])+a[d][0]&&(c=!c);return c}function j(a,c){for(var d,e,f=b.getData(),g=b.getOptions(),h=g.series.pie.radius>1?g.series.pie.radius:u*g.series.pie.radius,j=0;j<f.length;++j){var k=f[j];if(k.pie.show){if(y.save(),y.beginPath(),y.moveTo(0,0),y.arc(0,0,h,k.startAngle,k.startAngle+k.angle/2,!1),y.arc(0,0,h,k.startAngle+k.angle/2,k.startAngle+k.angle,!1),y.closePath(),d=a-v,e=c-w,y.isPointInPath){if(y.isPointInPath(a-v,c-w))return y.restore(),{datapoint:[k.percent,k.data],dataIndex:0,series:k,seriesIndex:j}}else{var l=h*Math.cos(k.startAngle),m=h*Math.sin(k.startAngle),n=h*Math.cos(k.startAngle+k.angle/4),o=h*Math.sin(k.startAngle+k.angle/4),p=h*Math.cos(k.startAngle+k.angle/2),q=h*Math.sin(k.startAngle+k.angle/2),r=h*Math.cos(k.startAngle+k.angle/1.5),s=h*Math.sin(k.startAngle+k.angle/1.5),t=h*Math.cos(k.startAngle+k.angle),x=h*Math.sin(k.startAngle+k.angle),z=[[0,0],[l,m],[n,o],[p,q],[r,s],[t,x]],A=[d,e];if(i(z,A))return y.restore(),{datapoint:[k.percent,k.data],dataIndex:0,series:k,seriesIndex:j}}y.restore()}}return null}function k(a){m("plothover",a)}function l(a){m("plotclick",a)}function m(a,c){var d=b.offset(),e=parseInt(c.pageX-d.left),f=parseInt(c.pageY-d.top),g=j(e,f);if(t.grid.autoHighlight)for(var h=0;h<z.length;++h){var i=z[h];i.auto!=a||g&&i.series==g.series||o(i.series)}g&&n(g.series,a);var k={pageX:c.pageX,pageY:c.pageY};s.trigger(a,[k,g])}function n(a,c){var d=p(a);-1==d?(z.push({series:a,auto:c}),b.triggerRedrawOverlay()):c||(z[d].auto=!1)}function o(a){null==a&&(z=[],b.triggerRedrawOverlay());var c=p(a);-1!=c&&(z.splice(c,1),b.triggerRedrawOverlay())}function p(a){for(var b=0;b<z.length;++b){var c=z[b];if(c.series==a)return b}return-1}function q(a,b){function c(a){a.angle<=0||isNaN(a.angle)||(b.fillStyle="rgba(255, 255, 255, "+d.series.pie.highlight.opacity+")",b.beginPath(),Math.abs(a.angle-2*Math.PI)>1e-9&&b.moveTo(0,0),b.arc(0,0,e,a.startAngle,a.startAngle+a.angle/2,!1),b.arc(0,0,e,a.startAngle+a.angle/2,a.startAngle+a.angle,!1),b.closePath(),b.fill())}var d=a.getOptions(),e=d.series.pie.radius>1?d.series.pie.radius:u*d.series.pie.radius;b.save(),b.translate(v,w),b.scale(1,d.series.pie.tilt);for(var f=0;f<z.length;++f)c(z[f].series);h(b),b.restore()}var r=null,s=null,t=null,u=null,v=null,w=null,x=!1,y=null,z=[];b.hooks.processOptions.push(function(a,b){b.series.pie.show&&(b.grid.show=!1,"auto"==b.series.pie.label.show&&(b.legend.show?b.series.pie.label.show=!1:b.series.pie.label.show=!0),"auto"==b.series.pie.radius&&(b.series.pie.label.show?b.series.pie.radius=.75:b.series.pie.radius=1),b.series.pie.tilt>1?b.series.pie.tilt=1:b.series.pie.tilt<0&&(b.series.pie.tilt=0))}),b.hooks.bindEvents.push(function(a,b){var c=a.getOptions();c.series.pie.show&&(c.grid.hoverable&&b.unbind("mousemove").mousemove(k),c.grid.clickable&&b.unbind("click").click(l))}),b.hooks.processDatapoints.push(function(a,b,c,d){var f=a.getOptions();f.series.pie.show&&e(a,b,c,d)}),b.hooks.drawOverlay.push(function(a,b){var c=a.getOptions();c.series.pie.show&&q(a,b)}),b.hooks.draw.push(function(a,b){var c=a.getOptions();c.series.pie.show&&g(a,b)})}var c=10,d=.95,e={series:{pie:{show:!1,radius:"auto",innerRadius:0,startAngle:1.5,tilt:1,shadow:{left:5,top:15,alpha:.02},offset:{top:0,left:"auto"},stroke:{color:"#fff",width:1},label:{show:"auto",formatter:function(a,b){return"<div style='font-size:x-small;text-align:center;padding:2px;color:"+b.color+";'>"+a+"<br/>"+Math.round(b.percent)+"%</div>"},radius:1,background:{color:null,opacity:0},threshold:0},combine:{threshold:-1,color:null,label:"Other"},highlight:{opacity:.5}}}};a.plot.plugins.push({init:b,options:e,name:"pie",version:"1.1"})}(jQuery);