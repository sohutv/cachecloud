/*
 
 Highcharts funnel module, Beta

 (c) 2010-2012 Torstein Hønsi

 License: www.highcharts.com/license
*/
(function(d){var u=d.getOptions().plotOptions,p=d.seriesTypes,D=d.merge,B=function(){},z=d.each;u.funnel=D(u.pie,{center:["50%","50%"],width:"90%",neckWidth:"30%",height:"100%",neckHeight:"25%",dataLabels:{connectorWidth:1,connectorColor:"#606060"},size:!0,states:{select:{color:"#C0C0C0",borderColor:"#000000",shadow:!1}}});p.funnel=d.extendClass(p.pie,{type:"funnel",animate:B,translate:function(){var a=function(k,a){return/%$/.test(k)?a*parseInt(k,10)/100:parseInt(k,10)},g=0,e=this.chart,f=e.plotWidth,
e=e.plotHeight,h=0,c=this.options,C=c.center,b=a(C[0],f),d=a(C[0],e),p=a(c.width,f),i,q,j=a(c.height,e),r=a(c.neckWidth,f),s=a(c.neckHeight,e),v=j-s,a=this.data,w,x,u=c.dataLabels.position==="left"?1:0,y,m,A,n,l,t,o;this.getWidthAt=q=function(k){return k>j-s||j===s?r:r+(p-r)*((j-s-k)/(j-s))};this.getX=function(k,a){return b+(a?-1:1)*(q(k)/2+c.dataLabels.distance)};this.center=[b,d,j];this.centerX=b;z(a,function(a){g+=a.y});z(a,function(a){o=null;x=g?a.y/g:0;m=d-j/2+h*j;l=m+x*j;i=q(m);y=b-i/2;A=y+
i;i=q(l);n=b-i/2;t=n+i;m>v?(y=n=b-r/2,A=t=b+r/2):l>v&&(o=l,i=q(v),n=b-i/2,t=n+i,l=v);w=["M",y,m,"L",A,m,t,l];o&&w.push(t,o,n,o);w.push(n,l,"Z");a.shapeType="path";a.shapeArgs={d:w};a.percentage=x*100;a.plotX=b;a.plotY=(m+(o||l))/2;a.tooltipPos=[b,a.plotY];a.slice=B;a.half=u;h+=x});this.setTooltipPoints()},drawPoints:function(){var a=this,g=a.options,e=a.chart.renderer;z(a.data,function(f){var h=f.graphic,c=f.shapeArgs;h?h.animate(c):f.graphic=e.path(c).attr({fill:f.color,stroke:g.borderColor,"stroke-width":g.borderWidth}).add(a.group)})},
drawDataLabels:function(){var a=this.data,g=this.options.dataLabels.distance,e,f,h,c=a.length,d,b;for(this.center[2]-=2*g;c--;)h=a[c],f=(e=h.half)?1:-1,b=h.plotY,d=this.getX(b,e),h.labelPos=[0,b,d+(g-5)*f,b,d+g*f,b,e?"right":"left",0];p.pie.prototype.drawDataLabels.call(this)}})})(Highcharts);
