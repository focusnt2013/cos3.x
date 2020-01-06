    function el(e){
        return document.querySelector(e)
    }
    function on(e,t,r){
        el(e).addEventListener(t,r,!1)
    }
    function show(e){
        el(e).style.display="block"
    }
    function hide(e){
        el(e).style.display="none"
    }
    function each(e,t){
        Object.keys(e).forEach(function(r){
            t(e[r],r)
        })
    }
    function save(e,t){
        localStorage.setItem(e,JSON.stringify(t))
    }
    function restore(e){
        try{
            return JSON.parse(localStorage.getItem(e))
        }
        catch(t){
            return localStorage.removeItem(e),null
        }
    }
    function pref(e,t){
        var r=null;
        return each(prefs,function(t,n){
            void 0!==t[e]&&(r=n)
        }),r?(void 0!==t&&(prefs[r][e]=t),prefs[r][e]||null):null
    }
    function setup(){
        each(prefs,function(e,t){
            each(e,function(e,t){
                el("#"+t).className=e?"active":""
            })
        }),on("body","click",function(e){
            if("toggle"===e.target.getAttribute("data-type")){
                var t=e.target,r=t.getAttribute("data-target");
                r.split(",").forEach(function(e,r){
                    var n=el("#"+e),i=cache.toggled[e];
                    cache.toggled[e]=i=!i,n.style.display=i?"block":"none",i||save("prefs",prefs),0===r&&(t.className=i?"active":"")
                })
            }
        }),on("body","click",function(e){
            if("pref"===e.target.getAttribute("data-type")){
                var t=e.target,r=t.getAttribute("id");
                pref(r,!pref(r)),t.className=pref(r)?"active":"",lint()
            }
        })
    }
    function main(){
        var e=el("#text-intro").innerHTML;
        e=e.split("\n"),e=e.slice(1,e.length-1),e=e.map(function(e){
            return e.slice(6)
        }).join("\n"),editor=CodeMirror(document.body,{
            value:e,mode:"javascript",tabSize:2,indentUnit:2,lineNumbers:!0,indentWithTabs:!1
        }),setup(),lint();
        var t=null;
        editor.on("change",function(e){
            t&&clearTimeout(t),t=setTimeout(function(){
                t=null,lint()
            }
            ,200)
        })
    }
    function lint(){
        var e=editor.getValue(),t={
            
        }
        ;
        worker||(worker=new Worker("worker.js"),worker.addEventListener("message",function(e){
            display(JSON.parse(e.data.result))
        })),each(prefs.opts,function(e,r){
            t[r]=e
        }),each(prefs.rev,function(e,r){
            t[r]=!e
        }),worker.postMessage({
            task:"lint",code:e,config:t
        })
    }
    function makeRow(e,t){
        var r=document.createElement("tr"),n=document.createElement("td");
        if(n.innerHTML=t,null!==e){
            var i=document.createElement("td");
            i.className="lineno",i.innerHTML=e,r.appendChild(i),r.addEventListener("mouseover",function(){
                editor.setSelection({
                    line:e-1,ch:0
                }
                ,{
                    line:e-1,ch:1/0
                })
            }),r.addEventListener("mouseout",function(){
                editor.setCursor({
                    line:e-1,ch:0
                })
            })
        }
        else n.className="header",n.setAttribute("colspan",2);
        return r.appendChild(n),r
    }
    function display(e){
        showUndef(e.implieds),showUnused(e.unused),showErrors(e.errors),showMetrics(e.functions)
    }
    function showErrors(e){
        var t=el("[data-type=errors]"),r="";
        return t.innerHTML="",e?(r=1===e.length?"One warning":e.length<11?NUM_TEXTS[e.length]+" warnings":e.length+" warnings",t.appendChild(makeRow(null,r)),void e.forEach(function(e){
            null!==e&&t.appendChild(makeRow(e.line,e.reason))
        })):!1
    }
    function showUndef(e){
        var t=el("[data-type=undef]"),r=t.innerHTML="";
        return prefs.meta.undef&&e?(r=1===e.length?"One undefined variable":e.length<11?NUM_TEXTS[e.length]+" undefined variables":e.length+" undefined variables",t.appendChild(makeRow(null,r)),e.forEach(function(e){
            e.line.forEach(function(r){
                t.appendChild(makeRow(r,e.name))
            })
        }),e.length>0):!1
    }
    function showUnused(e){
        var t=el("[data-type=unused]"),r=t.innerHTML="";
        return prefs.meta.unused&&e?(r=1===e.length?"One unused variable":e.length<11?NUM_TEXTS[e.length]+" unused variables":e.length+" unused variables",t.appendChild(makeRow(null,r)),e.forEach(function(e){
            t.appendChild(makeRow(e.line,e.name))
        }),e.length>0):!1
    }
    function showMetrics(e){
        function t(e){
            var t=document.createElement("p");
            t.innerHTML=e,i.appendChild(t)
        }
        function r(e){
            var t,r=e.length;
            return e.sort(function(e,t){
                return e-t
            }),t=Math.floor(r/2),console.log(e),{
                max:e[r-1],med:r%2?e[t]:(e[t-1]+e[t])/2
            }
        }
        var n=el("[data-type=metrics]"),i=el("[data-type=metrics] > div");
        if(i.innerHTML="",!prefs.meta.complex||!e.length)return n.style.display="none",!1;
        if(n.style.display="block",1===e.length){
            switch(t("There is only <b>one</b> function in this file."),e[0].metrics.parameters){
                case 0:t("It takes <b>no</b> arguments.");
                break;
                case 1:t("It takes <b>one</b> argument.");
                break;
                default:t("It takes <b>"+e[0].metrics.parameters+"</b> arguments.")
            }
            switch(e[0].metrics.statements){
                case 0:t("This function is <b>empty</b>.");
                break;
                case 1:t("This function contains only <b>one</b> statement.");
                break;
                default:t("This function contains <b>"+e[0].metrics.statements+"</b> statements.")
            }
            return void t("Cyclomatic complexity number for this function is <b>"+e[0].metrics.complexity+"</b>.")
        }
        var o=e.length,l=r(e.map(function(e){
            return e.metrics.parameters
        })),s=r(e.map(function(e){
            return e.metrics.statements
        })),a=r(e.map(function(e){
            return e.metrics.complexity
        }));
        t("There are <b>"+o+"</b> functions in this file."),t("Function with the largest signature take <b>"+l.max+"</b> arguments, while the median is <b>"+l.med+"</b>."),t("Largest function has <b>"+s.max+"</b> statements in it, while the median is <b>"+s.med+"</b>."),t("The most complex function has a cyclomatic complexity value of <b>"+a.max+"</b> while the median is <b>"+a.med+"</b>.")
    }
    window.CodeMirror=function(){
        "use strict";
        function e(r,n){
            if(!(this instanceof e))return new e(r,n);
            this.options=n=n||{
                
            }
            ;
            for(var i in ni)!n.hasOwnProperty(i)&&ni.hasOwnProperty(i)&&(n[i]=ni[i]);
            h(n);
            var o="string"==typeof n.value?0:n.value.first,l=this.display=t(r,o);
            l.wrapper.CodeMirror=this,u(this),n.autofocus&&!Pn&&de(this),this.state={
                keyMaps:[],overlays:[],modeGen:0,overwrite:!1,focused:!1,suppressEdits:!1,pasteIncoming:!1,draggingText:!1,highlight:new Ur
            }
            ,s(this),n.lineWrapping&&(this.display.wrapper.className+=" CodeMirror-wrap");
            var a=n.value;
            "string"==typeof a&&(a=new vi(n.value,n.mode)),oe(this,ur)(this,a),kn&&setTimeout(Qr(he,this,!0),20),me(this);
            var c;
            try{
                c=document.activeElement==l.input
            }
            catch(f){
                
            }
            c||n.autofocus&&!Pn?setTimeout(Qr(We,this),20):Ee(this),oe(this,function(){
                for(var e in ri)ri.propertyIsEnumerable(e)&&ri[e](this,n[e],ii);
                for(var t=0;
                t<ai.length;
                ++t)ai[t](this)
            })()
        }
        function t(e,t){
            var r={
                
            }
            ,n=r.input=rn("textarea",null,null,"position: absolute;
            padding: 0;
            width: 1px;
            height: 1em;
            outline: none;
            font-size: 4px;
            ","jsTextarea");
            return Tn?n.style.width="1000px":n.setAttribute("wrap","off"),zn&&(n.style.border="1px solid black"),n.setAttribute("autocorrect","off"),n.setAttribute("autocapitalize","off"),n.setAttribute("spellcheck","false"),r.inputDiv=rn("div",[n],null,"overflow: hidden;
            position: relative;
            width: 3px;
            height: 0px;
            "),r.scrollbarH=rn("div",[rn("div",null,null,"height: 1px")],"CodeMirror-hscrollbar"),r.scrollbarV=rn("div",[rn("div",null,null,"width: 1px")],"CodeMirror-vscrollbar"),r.scrollbarFiller=rn("div",null,"CodeMirror-scrollbar-filler"),r.gutterFiller=rn("div",null,"CodeMirror-gutter-filler"),r.lineDiv=rn("div",null,"CodeMirror-code"),r.selectionDiv=rn("div",null,null,"position: relative;
            z-index: 1"),r.cursor=rn("div","\xa0","CodeMirror-cursor"),r.otherCursor=rn("div","\xa0","CodeMirror-cursor CodeMirror-secondarycursor"),r.measure=rn("div",null,"CodeMirror-measure"),r.lineSpace=rn("div",[r.measure,r.selectionDiv,r.lineDiv,r.cursor,r.otherCursor],null,"position: relative;
            outline: none"),r.mover=rn("div",[rn("div",[r.lineSpace],"CodeMirror-lines")],null,"position: relative"),r.sizer=rn("div",[r.mover],"CodeMirror-sizer"),r.heightForcer=rn("div",null,null,"position: absolute;
            height: "+Ci+"px;
            width: 1px;
            "),r.gutters=rn("div",null,"CodeMirror-gutters"),r.lineGutter=null,r.scroller=rn("div",[r.sizer,r.heightForcer,r.gutters],"CodeMirror-scroll"),r.scroller.setAttribute("tabIndex","-1"),r.wrapper=rn("div",[r.inputDiv,r.scrollbarH,r.scrollbarV,r.scrollbarFiller,r.gutterFiller,r.scroller],"CodeMirror"),Sn&&(r.gutters.style.zIndex=-1,r.scroller.style.paddingRight=0),e.appendChild?e.appendChild(r.wrapper):e(r.wrapper),zn&&(n.style.width="0px"),Tn||(r.scroller.draggable=!0),En?(r.inputDiv.style.height="1px",r.inputDiv.style.position="absolute"):Sn&&(r.scrollbarH.style.minWidth=r.scrollbarV.style.minWidth="18px"),r.viewOffset=r.lastSizeC=0,r.showingFrom=r.showingTo=t,r.lineNumWidth=r.lineNumInnerWidth=r.lineNumChars=null,r.prevInput="",r.alignWidgets=!1,r.pollingFast=!1,r.poll=new Ur,r.cachedCharWidth=r.cachedTextHeight=null,r.measureLineCache=[],r.measureLineCachePos=0,r.inaccurateSelection=!1,r.maxLine=null,r.maxLineLength=0,r.maxLineChanged=!1,r.wheelDX=r.wheelDY=r.wheelStartX=r.wheelStartY=null,r
        }
        function r(t){
            t.doc.mode=e.getMode(t.options,t.doc.modeOption),t.doc.iter(function(e){
                e.stateAfter&&(e.stateAfter=null),e.styles&&(e.styles=null)
            }),t.doc.frontier=t.doc.first,E(t,100),t.state.modeGen++,t.curOp&&ae(t)
        }
        function n(e){
            e.options.lineWrapping?(e.display.wrapper.className+=" CodeMirror-wrap",e.display.sizer.style.minWidth=""):(e.display.wrapper.className=e.display.wrapper.className.replace(" CodeMirror-wrap",""),f(e)),o(e),ae(e),K(e),setTimeout(function(){
                d(e)
            }
            ,100)
        }
        function i(e){
            var t=te(e.display),r=e.options.lineWrapping,n=r&&Math.max(5,e.display.scroller.clientWidth/re(e.display)-3);
            return function(i){
                return Rt(e.doc,i)?0:r?(Math.ceil(i.text.length/n)||1)*t:t
            }
        }
        function o(e){
            var t=e.doc,r=i(e);
            t.iter(function(e){
                var t=r(e);
                t!=e.height&&dr(e,t)
            })
        }
        function l(e){
            var t=fi[e.options.keyMap],r=t.style;
            e.display.wrapper.className=e.display.wrapper.className.replace(/\s*cm-keymap-\S+/g,"")+(r?" cm-keymap-"+r:""),e.state.disableInput=t.disableInput
        }
        function s(e){
            e.display.wrapper.className=e.display.wrapper.className.replace(/\s*cm-s-\S+/g,"")+e.options.theme.replace(/(^|\s)\s*/g," cm-s-"),K(e)
        }
        function a(e){
            u(e),ae(e),setTimeout(function(){
                m(e)
            }
            ,20)
        }
        function u(e){
            var t=e.display.gutters,r=e.options.gutters;
            nn(t);
            for(var n=0;
            n<r.length;
            ++n){
                var i=r[n],o=t.appendChild(rn("div",null,"CodeMirror-gutter "+i));
                "CodeMirror-linenumbers"==i&&(e.display.lineGutter=o,o.style.width=(e.display.lineNumWidth||1)+"px")
            }
            t.style.display=n?"":"none"
        }
        function c(e,t){
            if(0==t.height)return 0;
            for(var r,n=t.text.length,i=t;
            r=zt(i);
            ){
                var o=r.find();
                i=cr(e,o.from.line),n+=o.from.ch-o.to.ch
            }
            for(i=t;
            r=Pt(i);
            ){
                var o=r.find();
                n-=i.text.length-o.from.ch,i=cr(e,o.to.line),n+=i.text.length-o.to.ch
            }
            return n
        }
        function f(e){
            var t=e.display,r=e.doc;
            t.maxLine=cr(r,r.first),t.maxLineLength=c(r,t.maxLine),t.maxLineChanged=!0,r.iter(function(e){
                var n=c(r,e);
                n>t.maxLineLength&&(t.maxLineLength=n,t.maxLine=e)
            })
        }
        function h(e){
            for(var t=!1,r=0;
            r<e.gutters.length;
            ++r)"CodeMirror-linenumbers"==e.gutters[r]&&(e.lineNumbers?t=!0:e.gutters.splice(r--,1));
            !t&&e.lineNumbers&&e.gutters.push("CodeMirror-linenumbers")
        }
        function d(e){
            var t=e.display,r=e.doc.height,n=r+P(t);
            t.sizer.style.minHeight=t.heightForcer.style.top=n+"px",t.gutters.style.height=Math.max(n,t.scroller.clientHeight-Ci)+"px";
            var i=Math.max(n,t.scroller.scrollHeight),o=t.scroller.scrollWidth>t.scroller.clientWidth+1,l=i>t.scroller.clientHeight+1;
            l?(t.scrollbarV.style.display="block",t.scrollbarV.style.bottom=o?un(t.measure)+"px":"0",t.scrollbarV.firstChild.style.height=i-t.scroller.clientHeight+t.scrollbarV.clientHeight+"px"):t.scrollbarV.style.display="",o?(t.scrollbarH.style.display="block",t.scrollbarH.style.right=l?un(t.measure)+"px":"0",t.scrollbarH.firstChild.style.width=t.scroller.scrollWidth-t.scroller.clientWidth+t.scrollbarH.clientWidth+"px"):t.scrollbarH.style.display="",o&&l?(t.scrollbarFiller.style.display="block",t.scrollbarFiller.style.height=t.scrollbarFiller.style.width=un(t.measure)+"px"):t.scrollbarFiller.style.display="",o&&e.options.coverGutterNextToScrollbar&&e.options.fixedGutter?(t.gutterFiller.style.display="block",t.gutterFiller.style.height=un(t.measure)+"px",t.gutterFiller.style.width=t.gutters.offsetWidth+"px"):t.gutterFiller.style.display="",Dn&&0===un(t.measure)&&(t.scrollbarV.style.minWidth=t.scrollbarH.style.minHeight=On?"18px":"12px")
        }
        function p(e,t,r){
            var n=e.scroller.scrollTop,i=e.wrapper.clientHeight;
            "number"==typeof r?n=r:r&&(n=r.top,i=r.bottom-r.top),n=Math.floor(n-z(e));
            var o=Math.ceil(n+i);
            return{
                from:mr(t,n),to:mr(t,o)
            }
        }
        function m(e){
            var t=e.display;
            if(t.alignWidgets||t.gutters.firstChild&&e.options.fixedGutter){
                for(var r=y(t)-t.scroller.scrollLeft+e.doc.scrollLeft,n=t.gutters.offsetWidth,i=r+"px",o=t.lineDiv.firstChild;
                o;
                o=o.nextSibling)if(o.alignable)for(var l=0,s=o.alignable;
                l<s.length;
                ++l)s[l].style.left=i;
                e.options.fixedGutter&&(t.gutters.style.left=r+n+"px")
            }
        }
        function g(e){
            if(!e.options.lineNumbers)return!1;
            var t=e.doc,r=v(e.options,t.first+t.size-1),n=e.display;
            if(r.length!=n.lineNumChars){
                var i=n.measure.appendChild(rn("div",[rn("div",r)],"CodeMirror-linenumber CodeMirror-gutter-elt")),o=i.firstChild.offsetWidth,l=i.offsetWidth-o;
                return n.lineGutter.style.width="",n.lineNumInnerWidth=Math.max(o,n.lineGutter.offsetWidth-l),n.lineNumWidth=n.lineNumInnerWidth+l,n.lineNumChars=n.lineNumInnerWidth?r.length:-1,n.lineGutter.style.width=n.lineNumWidth+"px",!0
            }
            return!1
        }
        function v(e,t){
            return String(e.lineNumberFormatter(t+e.firstLineNumber))
        }
        function y(e){
            return sn(e.scroller).left-sn(e.sizer).left
        }
        function b(e,t,r,n){
            for(var i,o=e.display.showingFrom,l=e.display.showingTo,s=p(e.display,e.doc,r);
            x(e,t,s,n)&&(n=!1,i=!0,A(e),d(e),r&&(r=Math.min(e.display.scroller.scrollHeight-e.display.scroller.clientHeight,"number"==typeof r?r:r.top)),s=p(e.display,e.doc,r),!(s.from>=e.display.showingFrom&&s.to<=e.display.showingTo));
            )t=[];
            return i&&(Rr(e,"update",e),(e.display.showingFrom!=o||e.display.showingTo!=l)&&Rr(e,"viewportChange",e,e.display.showingFrom,e.display.showingTo)),i
        }
        function x(e,t,r,n){
            var i=e.display,o=e.doc;
            if(!i.wrapper.clientWidth)return i.showingFrom=i.showingTo=o.first,void(i.viewOffset=0);
            if(!(!n&&0==t.length&&r.from>i.showingFrom&&r.to<i.showingTo)){
                g(e)&&(t=[{
                    from:o.first,to:o.first+o.size
                }
                ]);
                var l=i.sizer.style.marginLeft=i.gutters.offsetWidth+"px";
                i.scrollbarH.style.left=e.options.fixedGutter?l:"0";
                var s=1/0;
                if(e.options.lineNumbers)for(var a=0;
                a<t.length;
                ++a)if(t[a].diff){
                    s=t[a].from;
                    break
                }
                var u=o.first+o.size,c=Math.max(r.from-e.options.viewportMargin,o.first),f=Math.min(u,r.to+e.options.viewportMargin);
                if(i.showingFrom<c&&c-i.showingFrom<20&&(c=Math.max(o.first,i.showingFrom)),i.showingTo>f&&i.showingTo-f<20&&(f=Math.min(u,i.showingTo)),Xn)for(c=pr(Ft(o,cr(o,c)));
                u>f&&Rt(o,cr(o,f));
                )++f;
                var h=[{
                    from:Math.max(i.showingFrom,o.first),to:Math.min(i.showingTo,u)
                }
                ];
                if(h=h[0].from>=h[0].to?[]:L(h,t),Xn)for(var a=0;
                a<h.length;
                ++a)for(var d,p=h[a];
                d=Pt(cr(o,p.to-1));
                ){
                    var m=d.find().from.line;
                    if(!(m>p.from)){
                        h.splice(a--,1);
                        break
                    }
                    p.to=m
                }
                for(var v=0,a=0;
                a<h.length;
                ++a){
                    var p=h[a];
                    p.from<c&&(p.from=c),p.to>f&&(p.to=f),p.from>=p.to?h.splice(a--,1):v+=p.to-p.from
                }
                if(!n&&v==f-c&&c==i.showingFrom&&f==i.showingTo)return void C(e);
                h.sort(function(e,t){
                    return e.from-t.from
                });
                try{
                    var y=document.activeElement
                }
                catch(b){
                    
                }
                .7*(f-c)>v&&(i.lineDiv.style.display="none"),S(e,c,f,h,s),i.lineDiv.style.display="",y&&document.activeElement!=y&&y.offsetHeight&&y.focus();
                var x=c!=i.showingFrom||f!=i.showingTo||i.lastSizeC!=i.wrapper.clientHeight;
                return x&&(i.lastSizeC=i.wrapper.clientHeight,E(e,400)),i.showingFrom=c,i.showingTo=f,w(e),C(e),!0
            }
        }
        function w(e){
            for(var t,r=e.display,n=r.lineDiv.offsetTop,i=r.lineDiv.firstChild;
            i;
            i=i.nextSibling)if(i.lineObj){
                if(Sn){
                    var o=i.offsetTop+i.offsetHeight;
                    t=o-n,n=o
                }
                else{
                    var l=sn(i);
                    t=l.bottom-l.top
                }
                var s=i.lineObj.height-t;
                if(2>t&&(t=te(r)),s>.001||-.001>s){
                    dr(i.lineObj,t);
                    var a=i.lineObj.widgets;
                    if(a)for(var u=0;
                    u<a.length;
                    ++u)a[u].height=a[u].node.offsetHeight
                }
            }
        }
        function C(e){
            var t=e.display.viewOffset=gr(e,cr(e.doc,e.display.showingFrom));
            e.display.mover.style.top=t+"px"
        }
        function L(e,t){
            for(var r=0,n=t.length||0;
            n>r;
            ++r){
                for(var i=t[r],o=[],l=i.diff||0,s=0,a=e.length;
                a>s;
                ++s){
                    var u=e[s];
                    i.to<=u.from&&i.diff?o.push({
                        from:u.from+l,to:u.to+l
                    }):i.to<=u.from||i.from>=u.to?o.push(u):(i.from>u.from&&o.push({
                        from:u.from,to:i.from
                    }),i.to<u.to&&o.push({
                        from:i.to+l,to:u.to+l
                    }))
                }
                e=o
            }
            return e
        }
        function k(e){
            for(var t=e.display,r={
                
            }
            ,n={
                
            }
            ,i=t.gutters.firstChild,o=0;
            i;
            i=i.nextSibling,++o)r[e.options.gutters[o]]=i.offsetLeft,n[e.options.gutters[o]]=i.offsetWidth;
            return{
                fixedPos:y(t),gutterTotalWidth:t.gutters.offsetWidth,gutterLeft:r,gutterWidth:n,wrapperWidth:t.wrapper.clientWidth
            }
        }
        function S(e,t,r,n,i){
            function o(t){
                var r=t.nextSibling;
                return Tn&&Fn&&e.display.currentWheelTarget==t?(t.style.display="none",t.lineObj=null):t.parentNode.removeChild(t),r
            }
            var l=k(e),s=e.display,a=e.options.lineNumbers;
            n.length||Tn&&e.display.currentWheelTarget||nn(s.lineDiv);
            var u=s.lineDiv,c=u.firstChild,f=n.shift(),h=t;
            for(e.doc.iter(t,r,function(t){
                if(f&&f.to==h&&(f=n.shift()),Rt(e.doc,t)){
                    if(0!=t.height&&dr(t,0),t.widgets&&c.previousSibling)for(var r=0;
                    r<t.widgets.length;
                    ++r){
                        var s=t.widgets[r];
                        if(s.showIfHidden){
                            var d=c.previousSibling;
                            if(/pre/i.test(d.nodeName)){
                                var p=rn("div",null,null,"position: relative");
                                d.parentNode.replaceChild(p,d),p.appendChild(d),d=p
                            }
                            var m=d.appendChild(rn("div",[s.node],"CodeMirror-linewidget"));
                            s.handleMouseEvents||(m.ignoreEvents=!0),T(s,m,d,l)
                        }
                    }
                }
                else if(f&&f.from<=h&&f.to>h){
                    for(;
                    c.lineObj!=t;
                    )c=o(c);
                    a&&h>=i&&c.lineNumber&&ln(c.lineNumber,v(e.options,h)),c=c.nextSibling
                }
                else{
                    if(t.widgets)for(var g,y=0,b=c;
                    b&&20>y;
                    ++y,b=b.nextSibling)if(b.lineObj==t&&/div/i.test(b.nodeName)){
                        g=b;
                        break
                    }
                    var x=M(e,t,h,l,g);
                    if(x!=g)u.insertBefore(x,c);
                    else{
                        for(;
                        c!=g;
                        )c=o(c);
                        c=c.nextSibling
                    }
                    x.lineObj=t
                }
                ++h
            });
            c;
            )c=o(c)
        }
        function M(e,t,r,n,i){
            var o,l=Qt(e,t),s=t.gutterMarkers,a=e.display;
            if(!(e.options.lineNumbers||s||t.bgClass||t.wrapClass||t.widgets))return l;
            if(i){
                i.alignable=null;
                for(var u,c=!0,f=0,h=null,d=i.firstChild;
                d;
                d=u)if(u=d.nextSibling,/\bCodeMirror-linewidget\b/.test(d.className)){
                    for(var p=0;
                    p<t.widgets.length;
                    ++p){
                        var m=t.widgets[p];
                        if(m.node==d.firstChild){
                            m.above||h||(h=d),T(m,d,i,n),++f;
                            break
                        }
                    }
                    if(p==t.widgets.length){
                        c=!1;
                        break
                    }
                }
                else i.removeChild(d);
                i.insertBefore(l,h),c&&f==t.widgets.length&&(o=i,i.className=t.wrapClass||"")
            }
            if(o||(o=rn("div",null,t.wrapClass,"position: relative"),o.appendChild(l)),t.bgClass&&o.insertBefore(rn("div",null,t.bgClass+" CodeMirror-linebackground"),o.firstChild),e.options.lineNumbers||s){
                var g=o.insertBefore(rn("div",null,null,"position: absolute;
                left: "+(e.options.fixedGutter?n.fixedPos:-n.gutterTotalWidth)+"px"),o.firstChild);
                if(e.options.fixedGutter&&(o.alignable||(o.alignable=[])).push(g),!e.options.lineNumbers||s&&s["CodeMirror-linenumbers"]||(o.lineNumber=g.appendChild(rn("div",v(e.options,r),"CodeMirror-linenumber CodeMirror-gutter-elt","left: "+n.gutterLeft["CodeMirror-linenumbers"]+"px;
                width: "+a.lineNumInnerWidth+"px"))),s)for(var y=0;
                y<e.options.gutters.length;
                ++y){
                    var b=e.options.gutters[y],x=s.hasOwnProperty(b)&&s[b];
                    x&&g.appendChild(rn("div",[x],"CodeMirror-gutter-elt","left: "+n.gutterLeft[b]+"px;
                    width: "+n.gutterWidth[b]+"px"))
                }
            }
            if(Sn&&(o.style.zIndex=2),t.widgets&&o!=i)for(var p=0,w=t.widgets;
            p<w.length;
            ++p){
                var m=w[p],C=rn("div",[m.node],"CodeMirror-linewidget");
                m.handleMouseEvents||(C.ignoreEvents=!0),T(m,C,o,n),m.above?o.insertBefore(C,e.options.lineNumbers&&0!=t.height?g:l):o.appendChild(C),Rr(m,"redraw")
            }
            return o
        }
        function T(e,t,r,n){
            if(e.noHScroll){
                (r.alignable||(r.alignable=[])).push(t);
                var i=n.wrapperWidth;
                t.style.left=n.fixedPos+"px",e.coverGutter||(i-=n.gutterTotalWidth,t.style.paddingLeft=n.gutterTotalWidth+"px"),t.style.width=i+"px"
            }
            e.coverGutter&&(t.style.zIndex=5,t.style.position="relative",e.noHScroll||(t.style.marginLeft=-n.gutterTotalWidth+"px"))
        }
        function A(e){
            var t=e.display,r=Ke(e.doc.sel.from,e.doc.sel.to);
            if(r||e.options.showCursorWhenSelecting?N(e):t.cursor.style.display=t.otherCursor.style.display="none",r?t.selectionDiv.style.display="none":H(e),e.options.moveInputWithCursor){
                var n=Z(e,e.doc.sel.head,"div"),i=sn(t.wrapper),o=sn(t.lineDiv);
                t.inputDiv.style.top=Math.max(0,Math.min(t.wrapper.clientHeight-10,n.top+o.top-i.top))+"px",t.inputDiv.style.left=Math.max(0,Math.min(t.wrapper.clientWidth-10,n.left+o.left-i.left))+"px"
            }
        }
        function N(e){
            var t=e.display,r=Z(e,e.doc.sel.head,"div");
            t.cursor.style.left=r.left+"px",t.cursor.style.top=r.top+"px",t.cursor.style.height=Math.max(0,r.bottom-r.top)*e.options.cursorHeight+"px",t.cursor.style.display="",r.other?(t.otherCursor.style.display="",t.otherCursor.style.left=r.other.left+"px",t.otherCursor.style.top=r.other.top+"px",t.otherCursor.style.height=.85*(r.other.bottom-r.other.top)+"px"):t.otherCursor.style.display="none"
        }
        function H(e){
            function t(e,t,r,n){
                0>t&&(t=0),l.appendChild(rn("div",null,"CodeMirror-selected","position: absolute;
                left: "+e+"px;
                top: "+t+"px;
                width: "+(null==r?s-e:r)+"px;
                height: "+(n-t)+"px"))
            }
            function r(r,n,o){
                function l(t,n){
                    return q(e,Ue(r,t),"div",f,n)
                }
                var u,c,f=cr(i,r),h=f.text.length;
                return fn(vr(f),n||0,null==o?h:o,function(e,r,i){
                    var f,d,p,m=l(e,"left");
                    if(e==r)f=m,d=p=m.left;
                    else{
                        if(f=l(r-1,"right"),"rtl"==i){
                            var g=m;
                            m=f,f=g
                        }
                        d=m.left,p=f.right
                    }
                    null==n&&0==e&&(d=a),f.top-m.top>3&&(t(d,m.top,null,m.bottom),d=a,m.bottom<f.top&&t(d,m.bottom,null,f.top)),null==o&&r==h&&(p=s),(!u||m.top<u.top||m.top==u.top&&m.left<u.left)&&(u=m),(!c||f.bottom>c.bottom||f.bottom==c.bottom&&f.right>c.right)&&(c=f),a+1>d&&(d=a),t(d,f.top,p-d,f.bottom)
                }),{
                    start:u,end:c
                }
            }
            var n=e.display,i=e.doc,o=e.doc.sel,l=document.createDocumentFragment(),s=n.lineSpace.offsetWidth,a=F(e.display);
            if(o.from.line==o.to.line)r(o.from.line,o.from.ch,o.to.ch);
            else{
                var u=cr(i,o.from.line),c=cr(i,o.to.line),f=Ft(i,u)==Ft(i,c),h=r(o.from.line,o.from.ch,f?u.text.length:null).end,d=r(o.to.line,f?0:null,o.to.ch).start;
                f&&(h.top<d.top-2?(t(h.right,h.top,null,h.bottom),t(a,d.top,d.left,d.bottom)):t(h.right,h.top,d.left-h.right,h.bottom)),h.bottom<d.top&&t(a,h.bottom,null,d.top)
            }
            on(n.selectionDiv,l),n.selectionDiv.style.display=""
        }
        function W(e){
            if(e.state.focused){
                var t=e.display;
                clearInterval(t.blinker);
                var r=!0;
                t.cursor.style.visibility=t.otherCursor.style.visibility="",t.blinker=setInterval(function(){
                    t.cursor.style.visibility=t.otherCursor.style.visibility=(r=!r)?"":"hidden"
                }
                ,e.options.cursorBlinkRate)
            }
        }
        function E(e,t){
            e.doc.mode.startState&&e.doc.frontier<e.display.showingTo&&e.state.highlight.set(t,Qr(D,e))
        }
        function D(e){
            var t=e.doc;
            if(t.frontier<t.first&&(t.frontier=t.first),!(t.frontier>=e.display.showingTo)){
                var r,n=+new Date+e.options.workTime,i=gt(t.mode,I(e,t.frontier)),o=[];
                t.iter(t.frontier,Math.min(t.first+t.size,e.display.showingTo+500),function(l){
                    if(t.frontier>=e.display.showingFrom){
                        var s=l.styles;
                        l.styles=$t(e,l,i);
                        for(var a=!s||s.length!=l.styles.length,u=0;
                        !a&&u<s.length;
                        ++u)a=s[u]!=l.styles[u];
                        a&&(r&&r.end==t.frontier?r.end++:o.push(r={
                            start:t.frontier,end:t.frontier+1
                        })),l.stateAfter=gt(t.mode,i)
                    }
                    else Zt(e,l,i),l.stateAfter=t.frontier%5==0?gt(t.mode,i):null;
                    return++t.frontier,+new Date>n?(E(e,e.options.workDelay),!0):void 0
                }),o.length&&oe(e,function(){
                    for(var e=0;
                    e<o.length;
                    ++e)ae(this,o[e].start,o[e].end)
                })()
            }
        }
        function O(e,t,r){
            for(var n,i,o=e.doc,l=t,s=t-100;
            l>s;
            --l){
                if(l<=o.first)return o.first;
                var a=cr(o,l-1);
                if(a.stateAfter&&(!r||l<=o.frontier))return l;
                var u=Kr(a.text,null,e.options.tabSize);
                (null==i||n>u)&&(i=l-1,n=u)
            }
            return i
        }
        function I(e,t,r){
            var n=e.doc,i=e.display;
            if(!n.mode.startState)return!0;
            var o=O(e,t,r),l=o>n.first&&cr(n,o-1).stateAfter;
            return l=l?gt(n.mode,l):vt(n.mode),n.iter(o,t,function(r){
                Zt(e,r,l);
                var s=o==t-1||o%5==0||o>=i.showingFrom&&o<i.showingTo;
                r.stateAfter=s?gt(n.mode,l):null,++o
            }),l
        }
        function z(e){
            return e.lineSpace.offsetTop
        }
        function P(e){
            return e.mover.offsetHeight-e.lineSpace.offsetHeight
        }
        function F(e){
            var t=on(e.measure,rn("pre",null,null,"text-align: left")).appendChild(rn("span","x"));
            return t.offsetLeft
        }
        function R(e,t,r,n,i){
            var o=-1;
            n=n||B(e,t);
            for(var l=r;
            ;
            l+=o){
                var s=n[l];
                if(s)break;
                0>o&&0==l&&(o=1)
            }
            return i=l>r?"left":r>l?"right":i,"left"==i&&s.leftSide?s=s.leftSide:"right"==i&&s.rightSide&&(s=s.rightSide),{
                left:r>l?s.right:s.left,right:l>r?s.left:s.right,top:s.top,bottom:s.bottom
            }
        }
        function V(e,t){
            for(var r=e.display.measureLineCache,n=0;
            n<r.length;
            ++n){
                var i=r[n];
                if(i.text==t.text&&i.markedSpans==t.markedSpans&&e.display.scroller.clientWidth==i.width&&i.classes==t.textClass+"|"+t.bgClass+"|"+t.wrapClass)return i
            }
        }
        function G(e,t){
            var r=V(e,t);
            r&&(r.text=r.measure=r.markedSpans=null)
        }
        function B(e,t){
            var r=V(e,t);
            if(r)return r.measure;
            var n=j(e,t),i=e.display.measureLineCache,o={
                text:t.text,width:e.display.scroller.clientWidth,markedSpans:t.markedSpans,measure:n,classes:t.textClass+"|"+t.bgClass+"|"+t.wrapClass
            }
            ;
            return 16==i.length?i[++e.display.measureLineCachePos%16]=o:i.push(o),n
        }
        function j(e,t){
            function r(e){
                var t=e.top-p.top,r=e.bottom-p.top;
                r>v&&(r=v),0>t&&(t=0);
                for(var n=m.length-2;
                n>=0;
                n-=2){
                    var i=m[n],o=m[n+1];
                    if(!(i>r||t>o)&&(t>=i&&o>=r||i>=t&&r>=o||Math.min(r,o)-Math.max(t,i)>=r-t>>1)){
                        m[n]=Math.min(t,i),m[n+1]=Math.max(r,o);
                        break
                    }
                }
                return 0>n&&(n=m.length,m.push(t,r)),{
                    left:e.left-p.left,right:e.right-p.left,top:n,bottom:null
                }
            }
            function n(e){
                e.bottom=m[e.top+1],e.top=m[e.top]
            }
            var i=e.display,o=Jr(t.text.length),l=Qt(e,t,o,!0);
            if(kn&&!Sn&&!e.options.lineWrapping&&l.childNodes.length>100){
                for(var s=document.createDocumentFragment(),a=10,u=l.childNodes.length,c=0,f=Math.ceil(u/a);
                f>c;
                ++c){
                    for(var h=rn("div",null,null,"display: inline-block"),d=0;
                    a>d&&u;
                    ++d)h.appendChild(l.firstChild),--u;
                    s.appendChild(h)
                }
                l.appendChild(s)
            }
            on(i.measure,l);
            var p=sn(i.lineDiv),m=[],g=Jr(t.text.length),v=l.offsetHeight;
            Mn&&i.measure.first!=l&&on(i.measure,l);
            for(var y,c=0;
            c<o.length;
            ++c)if(y=o[c]){
                var b=y,x=null;
                if(/\bCodeMirror-widget\b/.test(y.className)&&y.getClientRects){
                    1==y.firstChild.nodeType&&(b=y.firstChild);
                    var w=b.getClientRects();
                    w.length>1&&(x=g[c]=r(w[0]),x.rightSide=r(w[w.length-1]))
                }
                x||(x=g[c]=r(sn(b))),y.measureRight&&(x.right=sn(y.measureRight).left),y.leftSide&&(x.leftSide=r(sn(y.leftSide)))
            }
            for(var y,c=0;
            c<g.length;
            ++c)(y=g[c])&&(n(y),y.leftSide&&n(y.leftSide),y.rightSide&&n(y.rightSide));
            return g
        }
        function U(e,t){
            var r=!1;
            if(t.markedSpans)for(var n=0;
            n<t.markedSpans;
            ++n){
                var i=t.markedSpans[n];
                !i.collapsed||null!=i.to&&i.to!=t.text.length||(r=!0)
            }
            var o=!r&&V(e,t);
            if(o)return R(e,t,t.text.length,o.measure,"right").right;
            var l=Qt(e,t,null,!0),s=l.appendChild(cn(e.display.measure));
            return on(e.display.measure,l),sn(s).right-sn(e.display.lineDiv).left
        }
        function K(e){
            e.display.measureLineCache.length=e.display.measureLineCachePos=0,e.display.cachedCharWidth=e.display.cachedTextHeight=null,e.options.lineWrapping||(e.display.maxLineChanged=!0),e.display.lineNumChars=null
        }
        function _(){
            return window.pageXOffset||(document.documentElement||document.body).scrollLeft
        }
        function X(){
            return window.pageYOffset||(document.documentElement||document.body).scrollTop
        }
        function Y(e,t,r,n){
            if(t.widgets)for(var i=0;
            i<t.widgets.length;
            ++i)if(t.widgets[i].above){
                var o=Ut(t.widgets[i]);
                r.top+=o,r.bottom+=o
            }
            if("line"==n)return r;
            n||(n="local");
            var l=gr(e,t);
            if("local"==n?l+=z(e.display):l-=e.display.viewOffset,"page"==n||"window"==n){
                var s=sn(e.display.lineSpace);
                l+=s.top+("window"==n?0:X());
                var a=s.left+("window"==n?0:_());
                r.left+=a,r.right+=a
            }
            return r.top+=l,r.bottom+=l,r
        }
        function $(e,t,r){
            if("div"==r)return t;
            var n=t.left,i=t.top;
            if("page"==r)n-=_(),i-=X();
            else if("local"==r||!r){
                var o=sn(e.display.sizer);
                n+=o.left,i+=o.top
            }
            var l=sn(e.display.lineSpace);
            return{
                left:n-l.left,top:i-l.top
            }
        }
        function q(e,t,r,n,i){
            return n||(n=cr(e.doc,t.line)),Y(e,n,R(e,n,t.ch,null,i),r)
        }
        function Z(e,t,r,n,i){
            function o(t,o){
                var l=R(e,n,t,i,o?"right":"left");
                return o?l.left=l.right:l.right=l.left,Y(e,n,l,r)
            }
            function l(e,t){
                var r=s[t],n=r.level%2;
                return e==hn(r)&&t&&r.level<s[t-1].level?(r=s[--t],e=dn(r)-(r.level%2?0:1),n=!0):e==dn(r)&&t<s.length-1&&r.level<s[t+1].level&&(r=s[++t],e=hn(r)-r.level%2,n=!1),n&&e==r.to&&e>r.from?o(e-1):o(e,n)
            }
            n=n||cr(e.doc,t.line),i||(i=B(e,n));
            var s=vr(n),a=t.ch;
            if(!s)return o(a);
            var u=bn(s,a),c=l(a,u);
            return null!=Oi&&(c.other=l(a,Oi)),c
        }
        function J(e,t,r,n){
            var i=new Ue(e,t);
            return i.xRel=n,r&&(i.outside=!0),i
        }
        function Q(e,t,r){
            var n=e.doc;
            if(r+=e.display.viewOffset,0>r)return J(n.first,0,!0,-1);
            var i=mr(n,r),o=n.first+n.size-1;
            if(i>o)return J(n.first+n.size-1,cr(n,o).text.length,!0,1);
            for(0>t&&(t=0);
            ;
            ){
                var l=cr(n,i),s=ee(e,l,i,t,r),a=Pt(l),u=a&&a.find();
                if(!a||!(s.ch>u.from.ch||s.ch==u.from.ch&&s.xRel>0))return s;
                i=u.to.line
            }
        }
        function ee(e,t,r,n,i){
            function o(n){
                var i=Z(e,Ue(r,n),"line",t,u);
                return s=!0,l>i.bottom?i.left-a:l<i.top?i.left+a:(s=!1,i.left)
            }
            var l=i-gr(e,t),s=!1,a=2*e.display.wrapper.clientWidth,u=B(e,t),c=vr(t),f=t.text.length,h=pn(t),d=mn(t),p=o(h),m=s,g=o(d),v=s;
            if(n>g)return J(r,d,v,1);
            for(;
            ;
            ){
                if(c?d==h||d==wn(t,h,1):1>=d-h){
                    for(var y=p>n||g-n>=n-p?h:d,b=n-(y==h?p:g);
                    Mi.test(t.text.charAt(y));
                    )++y;
                    var x=J(r,y,y==h?m:v,0>b?-1:b?1:0);
                    return x
                }
                var w=Math.ceil(f/2),C=h+w;
                if(c){
                    C=h;
                    for(var L=0;
                    w>L;
                    ++L)C=wn(t,C,1)
                }
                var k=o(C);
                k>n?(d=C,g=k,(v=s)&&(g+=1e3),f=w):(h=C,p=k,m=s,f-=w)
            }
        }
        function te(e){
            if(null!=e.cachedTextHeight)return e.cachedTextHeight;
            if(null==Gn){
                Gn=rn("pre");
                for(var t=0;
                49>t;
                ++t)Gn.appendChild(document.createTextNode("x")),Gn.appendChild(rn("br"));
                Gn.appendChild(document.createTextNode("x"))
            }
            on(e.measure,Gn);
            var r=Gn.offsetHeight/50;
            return r>3&&(e.cachedTextHeight=r),nn(e.measure),r||1
        }
        function re(e){
            if(null!=e.cachedCharWidth)return e.cachedCharWidth;
            var t=rn("span","x"),r=rn("pre",[t]);
            on(e.measure,r);
            var n=t.offsetWidth;
            return n>2&&(e.cachedCharWidth=n),n||10
        }
        function ne(e){
            e.curOp={
                changes:[],forceUpdate:!1,updateInput:null,userSelChange:null,textChanged:null,selectionChanged:!1,cursorActivity:!1,updateMaxLine:!1,updateScrollPos:!1,id:++Yn
            }
            ,wi++||(xi=[])
        }
        function ie(e){
            var t=e.curOp,r=e.doc,n=e.display;
            if(e.curOp=null,t.updateMaxLine&&f(e),n.maxLineChanged&&!e.options.lineWrapping&&n.maxLine){
                var i=U(e,n.maxLine);
                n.sizer.style.minWidth=Math.max(0,i+3+Ci)+"px",n.maxLineChanged=!1;
                var o=Math.max(0,n.sizer.offsetLeft+n.sizer.offsetWidth-n.scroller.clientWidth);
                o<r.scrollLeft&&!t.updateScrollPos&&Le(e,Math.min(n.scroller.scrollLeft,o),!0)
            }
            var l,s;
            if(t.updateScrollPos)l=t.updateScrollPos;
            else if(t.selectionChanged&&n.scroller.clientHeight){
                var a=Z(e,r.sel.head);
                l=lt(e,a.left,a.top,a.left,a.bottom)
            }
            (t.changes.length||t.forceUpdate||l&&null!=l.scrollTop)&&(s=b(e,t.changes,l&&l.scrollTop,t.forceUpdate),e.display.scroller.offsetHeight&&(e.doc.scrollTop=e.display.scroller.scrollTop)),!s&&t.selectionChanged&&A(e),t.updateScrollPos?(n.scroller.scrollTop=n.scrollbarV.scrollTop=r.scrollTop=l.scrollTop,n.scroller.scrollLeft=n.scrollbarH.scrollLeft=r.scrollLeft=l.scrollLeft,m(e),t.scrollToPos&&it(e,$e(e.doc,t.scrollToPos),t.scrollToPosMargin)):l&&nt(e),t.selectionChanged&&W(e),e.state.focused&&t.updateInput&&he(e,t.userSelChange);
            var u=t.maybeHiddenMarkers,c=t.maybeUnhiddenMarkers;
            if(u)for(var h=0;
            h<u.length;
            ++h)u[h].lines.length||Fr(u[h],"hide");
            if(c)for(var h=0;
            h<c.length;
            ++h)c[h].lines.length&&Fr(c[h],"unhide");
            var d;
            if(--wi||(d=xi,xi=null),t.textChanged&&Fr(e,"change",e,t.textChanged),t.cursorActivity&&Fr(e,"cursorActivity",e),d)for(var h=0;
            h<d.length;
            ++h)d[h]()
        }
        function oe(e,t){
            return function(){
                var r=e||this,n=!r.curOp;
                n&&ne(r);
                try{
                    var i=t.apply(r,arguments)
                }
                finally{
                    n&&ie(r)
                }
                return i
            }
        }
        function le(e){
            return function(){
                var t,r=this.cm&&!this.cm.curOp;
                r&&ne(this.cm);
                try{
                    t=e.apply(this,arguments)
                }
                finally{
                    r&&ie(this.cm)
                }
                return t
            }
        }
        function se(e,t){
            var r,n=!e.curOp;
            n&&ne(e);
            try{
                r=t()
            }
            finally{
                n&&ie(e)
            }
            return r
        }
        function ae(e,t,r,n){
            null==t&&(t=e.doc.first),null==r&&(r=e.doc.first+e.doc.size),e.curOp.changes.push({
                from:t,to:r,diff:n
            })
        }
        function ue(e){
            e.display.pollingFast||e.display.poll.set(e.options.pollInterval,function(){
                fe(e),e.state.focused&&ue(e)
            })
        }
        function ce(e){
            function t(){
                var n=fe(e);
                n||r?(e.display.pollingFast=!1,ue(e)):(r=!0,e.display.poll.set(60,t))
            }
            var r=!1;
            e.display.pollingFast=!0,e.display.poll.set(20,t)
        }
        function fe(e){
            var t=e.display.input,r=e.display.prevInput,n=e.doc,i=n.sel;
            if(!e.state.focused||Wi(t)||pe(e)||e.state.disableInput)return!1;
            var o=t.value;
            if(o==r&&Ke(i.from,i.to))return!1;
            if(kn&&!Mn&&e.display.inputHasSelection===o)return he(e,!0),!1;
            var l=!e.curOp;
            l&&ne(e),i.shift=!1;
            for(var s=0,a=Math.min(r.length,o.length);
            a>s&&r.charCodeAt(s)==o.charCodeAt(s);
            )++s;
            var u=i.from,c=i.to;
            s<r.length?u=Ue(u.line,u.ch-(r.length-s)):e.state.overwrite&&Ke(u,c)&&!e.state.pasteIncoming&&(c=Ue(c.line,Math.min(cr(n,c.line).text.length,c.ch+(o.length-s))));
            var f=e.curOp.updateInput,h={
                from:u,to:c,text:Hi(o.slice(s)),origin:e.state.pasteIncoming?"paste":"+input"
            }
            ;
            return Pe(e.doc,h,"end"),
e.curOp.updateInput=f,Rr(e,"inputRead",e,h),o.length>1e3||o.indexOf("\n")>-1?t.value=e.display.prevInput="":e.display.prevInput=o,l&&ie(e),e.state.pasteIncoming=!1,!0
        }
        function he(e,t){
            var r,n,i=e.doc;
            if(Ke(i.sel.from,i.sel.to))t&&(e.display.prevInput=e.display.input.value="",kn&&!Mn&&(e.display.inputHasSelection=null));
            else{
                e.display.prevInput="",r=Ei&&(i.sel.to.line-i.sel.from.line>100||(n=e.getSelection()).length>1e3);
                var o=r?"-":n||e.getSelection();
                e.display.input.value=o,e.state.focused&&Yr(e.display.input),kn&&!Mn&&(e.display.inputHasSelection=o)
            }
            e.display.inaccurateSelection=r
        }
        function de(e){
            "nocursor"==e.options.readOnly||Pn&&document.activeElement==e.display.input||e.display.input.focus()
        }
        function pe(e){
            return e.options.readOnly||e.doc.cantEdit
        }
        function me(e){
            function t(){
                e.state.focused&&setTimeout(Qr(de,e),0)
            }
            function r(){
                null==s&&(s=setTimeout(function(){
                    s=null,l.cachedCharWidth=l.cachedTextHeight=Ai=null,K(e),se(e,Qr(ae,e))
                }
                ,100))
            }
            function n(){
                for(var e=l.wrapper.parentNode;
                e&&e!=document.body;
                e=e.parentNode);
                e?setTimeout(n,5e3):Pr(window,"resize",r)
            }
            function i(t){
                Vr(e,t)||e.options.onDragEvent&&e.options.onDragEvent(e,Nr(t))||Dr(t)
            }
            function o(){
                l.inaccurateSelection&&(l.prevInput="",l.inaccurateSelection=!1,l.input.value=e.getSelection(),Yr(l.input))
            }
            var l=e.display;
            zr(l.scroller,"mousedown",oe(e,ye)),kn?zr(l.scroller,"dblclick",oe(e,function(t){
                if(!Vr(e,t)){
                    var r=ve(e,t);
                    if(r&&!be(e,t)&&!ge(e.display,t)){
                        Hr(t);
                        var n=dt(cr(e.doc,r.line).text,r);
                        Je(e.doc,n.from,n.to)
                    }
                }
            })):zr(l.scroller,"dblclick",function(t){
                Vr(e,t)||Hr(t)
            }),zr(l.lineSpace,"selectstart",function(e){
                ge(l,e)||Hr(e)
            }),Kn||zr(l.scroller,"contextmenu",function(t){
                De(e,t)
            }),zr(l.scroller,"scroll",function(){
                l.scroller.clientHeight&&(Ce(e,l.scroller.scrollTop),Le(e,l.scroller.scrollLeft,!0),Fr(e,"scroll",e))
            }),zr(l.scrollbarV,"scroll",function(){
                l.scroller.clientHeight&&Ce(e,l.scrollbarV.scrollTop)
            }),zr(l.scrollbarH,"scroll",function(){
                l.scroller.clientHeight&&Le(e,l.scrollbarH.scrollLeft)
            }),zr(l.scroller,"mousewheel",function(t){
                ke(e,t)
            }),zr(l.scroller,"DOMMouseScroll",function(t){
                ke(e,t)
            }),zr(l.scrollbarH,"mousedown",t),zr(l.scrollbarV,"mousedown",t),zr(l.wrapper,"scroll",function(){
                l.wrapper.scrollTop=l.wrapper.scrollLeft=0
            });
            var s;
            zr(window,"resize",r),setTimeout(n,5e3),zr(l.input,"keyup",oe(e,function(t){
                Vr(e,t)||e.options.onKeyEvent&&e.options.onKeyEvent(e,Nr(t))||16==t.keyCode&&(e.doc.sel.shift=!1)
            })),zr(l.input,"input",Qr(ce,e)),zr(l.input,"keydown",oe(e,Ne)),zr(l.input,"keypress",oe(e,He)),zr(l.input,"focus",Qr(We,e)),zr(l.input,"blur",Qr(Ee,e)),e.options.dragDrop&&(zr(l.scroller,"dragstart",function(t){
                we(e,t)
            }),zr(l.scroller,"dragenter",i),zr(l.scroller,"dragover",i),zr(l.scroller,"drop",oe(e,xe))),zr(l.scroller,"paste",function(t){
                ge(l,t)||(de(e),ce(e))
            }),zr(l.input,"paste",function(){
                e.state.pasteIncoming=!0,ce(e)
            }),zr(l.input,"cut",o),zr(l.input,"copy",o),En&&zr(l.sizer,"mouseup",function(){
                document.activeElement==l.input&&l.input.blur(),de(e)
            })
        }
        function ge(e,t){
            for(var r=Or(t);
            r!=e.wrapper;
            r=r.parentNode)if(!r||r.ignoreEvents||r.parentNode==e.sizer&&r!=e.mover)return!0
        }
        function ve(e,t,r){
            var n=e.display;
            if(!r){
                var i=Or(t);
                if(i==n.scrollbarH||i==n.scrollbarH.firstChild||i==n.scrollbarV||i==n.scrollbarV.firstChild||i==n.scrollbarFiller||i==n.gutterFiller)return null
            }
            var o,l,s=sn(n.lineSpace);
            try{
                o=t.clientX,l=t.clientY
            }
            catch(t){
                return null
            }
            return Q(e,o-s.left,l-s.top)
        }
        function ye(e){
            function t(e){
                if(!Ke(v,e)){
                    if(v=e,"single"==c)return void Je(i.doc,$e(l,a),e);
                    if(m=$e(l,m),g=$e(l,g),"double"==c){
                        var t=dt(cr(l,e.line).text,e);
                        _e(e,m)?Je(i.doc,t.from,g):Je(i.doc,m,t.to)
                    }
                    else"triple"==c&&(_e(e,m)?Je(i.doc,g,$e(l,Ue(e.line,0))):Je(i.doc,m,$e(l,Ue(e.line+1,0))))
                }
            }
            function r(e){
                var n=++b,s=ve(i,e,!0);
                if(s)if(Ke(s,h)){
                    var a=e.clientY<y.top?-20:e.clientY>y.bottom?20:0;
                    a&&setTimeout(oe(i,function(){
                        b==n&&(o.scroller.scrollTop+=a,r(e))
                    }),50)
                }
                else{
                    i.state.focused||We(i),h=s,t(s);
                    var u=p(o,l);
                    (s.line>=u.to||s.line<u.from)&&setTimeout(oe(i,function(){
                        b==n&&r(e)
                    }),150)
                }
            }
            function n(e){
                b=1/0,Hr(e),de(i),Pr(document,"mousemove",x),Pr(document,"mouseup",w)
            }
            if(!Vr(this,e)){
                var i=this,o=i.display,l=i.doc,s=l.sel;
                if(s.shift=e.shiftKey,ge(o,e))return void(Tn||(o.scroller.draggable=!1,setTimeout(function(){
                    o.scroller.draggable=!0
                }
                ,100)));
                if(!be(i,e)){
                    var a=ve(i,e);
                    switch(Ir(e)){
                        case 3:return void(Kn&&De.call(i,i,e));
                        case 2:return a&&Je(i.doc,a),setTimeout(Qr(de,i),20),void Hr(e)
                    }
                    if(!a)return void(Or(e)==o.scroller&&Hr(e));
                    i.state.focused||We(i);
                    var u=+new Date,c="single";
                    if(jn&&jn.time>u-400&&Ke(jn.pos,a))c="triple",Hr(e),setTimeout(Qr(de,i),20),pt(i,a.line);
                    else if(Bn&&Bn.time>u-400&&Ke(Bn.pos,a)){
                        c="double",jn={
                            time:u,pos:a
                        }
                        ,Hr(e);
                        var f=dt(cr(l,a.line).text,a);
                        Je(i.doc,f.from,f.to)
                    }
                    else Bn={
                        time:u,pos:a
                    }
                    ;
                    var h=a;
                    if(i.options.dragDrop&&Ti&&!pe(i)&&!Ke(s.from,s.to)&&!_e(a,s.from)&&!_e(s.to,a)&&"single"==c){
                        var d=oe(i,function(t){
                            Tn&&(o.scroller.draggable=!1),i.state.draggingText=!1,Pr(document,"mouseup",d),Pr(o.scroller,"drop",d),Math.abs(e.clientX-t.clientX)+Math.abs(e.clientY-t.clientY)<10&&(Hr(t),Je(i.doc,a),de(i))
                        });
                        return Tn&&(o.scroller.draggable=!0),i.state.draggingText=d,o.scroller.dragDrop&&o.scroller.dragDrop(),zr(document,"mouseup",d),void zr(o.scroller,"drop",d)
                    }
                    Hr(e),"single"==c&&Je(i.doc,$e(l,a));
                    var m=s.from,g=s.to,v=a,y=sn(o.wrapper),b=0,x=oe(i,function(e){
                        kn||Ir(e)?r(e):n(e)
                    }),w=oe(i,n);
                    zr(document,"mousemove",x),zr(document,"mouseup",w)
                }
            }
        }
        function be(e,t){
            var r=e.display;
            try{
                var n=t.clientX,i=t.clientY
            }
            catch(t){
                return!1
            }
            if(n>=Math.floor(sn(r.gutters).right))return!1;
            if(Hr(t),!Br(e,"gutterClick"))return!0;
            var o=sn(r.lineDiv);
            if(i>o.bottom)return!0;
            i-=o.top-r.viewOffset;
            for(var l=0;
            l<e.options.gutters.length;
            ++l){
                var s=r.gutters.childNodes[l];
                if(s&&sn(s).right>=n){
                    var a=mr(e.doc,i),u=e.options.gutters[l];
                    Rr(e,"gutterClick",e,a,u,t);
                    break
                }
            }
            return!0
        }
        function xe(e){
            var t=this;
            if(!(Vr(t,e)||ge(t.display,e)||t.options.onDragEvent&&t.options.onDragEvent(t,Nr(e)))){
                Hr(e),kn&&($n=+new Date);
                var r=ve(t,e,!0),n=e.dataTransfer.files;
                if(r&&!pe(t))if(n&&n.length&&window.FileReader&&window.File)for(var i=n.length,o=Array(i),l=0,s=function(e,n){
                    var s=new FileReader;
                    s.onload=function(){
                        o[n]=s.result,++l==i&&(r=$e(t.doc,r),Pe(t.doc,{
                            from:r,to:r,text:Hi(o.join("\n")),origin:"paste"
                        }
                        ,"around"))
                    }
                    ,s.readAsText(e)
                }
                ,a=0;
                i>a;
                ++a)s(n[a],a);
                else{
                    if(t.state.draggingText&&!_e(r,t.doc.sel.from)&&!_e(t.doc.sel.to,r))return t.state.draggingText(e),void setTimeout(Qr(de,t),20);
                    try{
                        var o=e.dataTransfer.getData("Text");
                        if(o){
                            var u=t.doc.sel.from,c=t.doc.sel.to;
                            et(t.doc,r,r),t.state.draggingText&&je(t.doc,"",u,c,"paste"),t.replaceSelection(o,null,"paste"),de(t),We(t)
                        }
                    }
                    catch(e){
                        
                    }
                }
            }
        }
        function we(e,t){
            if(kn&&(!e.state.draggingText||+new Date-$n<100))return void Dr(t);
            if(!Vr(e,t)&&!ge(e.display,t)){
                var r=e.getSelection();
                if(t.dataTransfer.setData("Text",r),t.dataTransfer.setDragImage&&!Wn){
                    var n=rn("img",null,null,"position: fixed;
                    left: 0;
                    top: 0;
                    ");
                    Hn&&(n.width=n.height=1,e.display.wrapper.appendChild(n),n._top=n.offsetTop),t.dataTransfer.setDragImage(n,0,0),Hn&&n.parentNode.removeChild(n)
                }
            }
        }
        function Ce(e,t){
            Math.abs(e.doc.scrollTop-t)<2||(e.doc.scrollTop=t,Ln||b(e,[],t),e.display.scroller.scrollTop!=t&&(e.display.scroller.scrollTop=t),e.display.scrollbarV.scrollTop!=t&&(e.display.scrollbarV.scrollTop=t),Ln&&b(e,[]),E(e,100))
        }
        function Le(e,t,r){
            (r?t==e.doc.scrollLeft:Math.abs(e.doc.scrollLeft-t)<2)||(t=Math.min(t,e.display.scroller.scrollWidth-e.display.scroller.clientWidth),e.doc.scrollLeft=t,m(e),e.display.scroller.scrollLeft!=t&&(e.display.scroller.scrollLeft=t),e.display.scrollbarH.scrollLeft!=t&&(e.display.scrollbarH.scrollLeft=t))
        }
        function ke(e,t){
            var r=t.wheelDeltaX,n=t.wheelDeltaY;
            null==r&&t.detail&&t.axis==t.HORIZONTAL_AXIS&&(r=t.detail),null==n&&t.detail&&t.axis==t.VERTICAL_AXIS?n=t.detail:null==n&&(n=t.wheelDelta);
            var i=e.display,o=i.scroller;
            if(r&&o.scrollWidth>o.clientWidth||n&&o.scrollHeight>o.clientHeight){
                if(n&&Fn&&Tn)for(var l=t.target;
                l!=o;
                l=l.parentNode)if(l.lineObj){
                    e.display.currentWheelTarget=l;
                    break
                }
                if(r&&!Ln&&!Hn&&null!=Zn)return n&&Ce(e,Math.max(0,Math.min(o.scrollTop+n*Zn,o.scrollHeight-o.clientHeight))),Le(e,Math.max(0,Math.min(o.scrollLeft+r*Zn,o.scrollWidth-o.clientWidth))),Hr(t),void(i.wheelStartX=null);
                if(n&&null!=Zn){
                    var s=n*Zn,a=e.doc.scrollTop,u=a+i.wrapper.clientHeight;
                    0>s?a=Math.max(0,a+s-50):u=Math.min(e.doc.height,u+s+50),b(e,[],{
                        top:a,bottom:u
                    })
                }
                20>qn&&(null==i.wheelStartX?(i.wheelStartX=o.scrollLeft,i.wheelStartY=o.scrollTop,i.wheelDX=r,i.wheelDY=n,setTimeout(function(){
                    if(null!=i.wheelStartX){
                        var e=o.scrollLeft-i.wheelStartX,t=o.scrollTop-i.wheelStartY,r=t&&i.wheelDY&&t/i.wheelDY||e&&i.wheelDX&&e/i.wheelDX;
                        i.wheelStartX=i.wheelStartY=null,r&&(Zn=(Zn*qn+r)/(qn+1),++qn)
                    }
                }
                ,200)):(i.wheelDX+=r,i.wheelDY+=n))
            }
        }
        function Se(e,t,r){
            if("string"==typeof t&&(t=ci[t],!t))return!1;
            e.display.pollingFast&&fe(e)&&(e.display.pollingFast=!1);
            var n=e.doc,i=n.sel.shift,o=!1;
            try{
                pe(e)&&(e.state.suppressEdits=!0),r&&(n.sel.shift=!1),o=t(e)!=Li
            }
            finally{
                n.sel.shift=i,e.state.suppressEdits=!1
            }
            return o
        }
        function Me(e){
            var t=e.state.keyMaps.slice(0);
            return e.options.extraKeys&&t.push(e.options.extraKeys),t.push(e.options.keyMap),t
        }
        function Te(e,t){
            var r=yt(e.options.keyMap),n=r.auto;
            clearTimeout(Jn),n&&!xt(t)&&(Jn=setTimeout(function(){
                yt(e.options.keyMap)==r&&(e.options.keyMap=n.call?n.call(null,e):n,l(e))
            }
            ,50));
            var i=wt(t,!0),o=!1;
            if(!i)return!1;
            var s=Me(e);
            return o=t.shiftKey?bt("Shift-"+i,s,function(t){
                return Se(e,t,!0)
            })||bt(i,s,function(t){
                return("string"==typeof t?/^go[A-Z]/.test(t):t.motion)?Se(e,t):void 0
            }):bt(i,s,function(t){
                return Se(e,t)
            }),o&&(Hr(t),W(e),Mn&&(t.oldKeyCode=t.keyCode,t.keyCode=0),Rr(e,"keyHandled",e,i,t)),o
        }
        function Ae(e,t,r){
            var n=bt("'"+r+"'",Me(e),function(t){
                return Se(e,t,!0)
            });
            return n&&(Hr(t),W(e),Rr(e,"keyHandled",e,"'"+r+"'",t)),n
        }
        function Ne(e){
            var t=this;
            if(t.state.focused||We(t),kn&&27==e.keyCode&&(e.returnValue=!1),!(Vr(t,e)||t.options.onKeyEvent&&t.options.onKeyEvent(t,Nr(e)))){
                var r=e.keyCode;
                t.doc.sel.shift=16==r||e.shiftKey;
                var n=Te(t,e);
                Hn&&(ei=n?r:null,!n&&88==r&&!Ei&&(Fn?e.metaKey:e.ctrlKey)&&t.replaceSelection(""))
            }
        }
        function He(e){
            var t=this;
            if(!(Vr(t,e)||t.options.onKeyEvent&&t.options.onKeyEvent(t,Nr(e)))){
                var r=e.keyCode,n=e.charCode;
                if(Hn&&r==ei)return ei=null,void Hr(e);
                if(!(Hn&&(!e.which||e.which<10)||En)||!Te(t,e)){
                    var i=String.fromCharCode(null==n?r:n);
                    this.options.electricChars&&this.doc.mode.electricChars&&this.options.smartIndent&&!pe(this)&&this.doc.mode.electricChars.indexOf(i)>-1&&setTimeout(oe(t,function(){
                        ut(t,t.doc.sel.to.line,"smart")
                    }),75),Ae(t,e,i)||(kn&&!Mn&&(t.display.inputHasSelection=null),ce(t))
                }
            }
        }
        function We(e){
            "nocursor"!=e.options.readOnly&&(e.state.focused||(Fr(e,"focus",e),e.state.focused=!0,-1==e.display.wrapper.className.search(/\bCodeMirror-focused\b/)&&(e.display.wrapper.className+=" CodeMirror-focused"),he(e,!0)),ue(e),W(e))
        }
        function Ee(e){
            e.state.focused&&(Fr(e,"blur",e),e.state.focused=!1,e.display.wrapper.className=e.display.wrapper.className.replace(" CodeMirror-focused","")),clearInterval(e.display.blinker),setTimeout(function(){
                e.state.focused||(e.doc.sel.shift=!1)
            }
            ,150)
        }
        function De(e,t){
            function r(){
                if(null!=i.input.selectionStart){
                    var e=i.input.value=" "+(Ke(o.from,o.to)?"":i.input.value);
                    i.prevInput=" ",i.input.selectionStart=1,i.input.selectionEnd=e.length
                }
            }
            function n(){
                if(i.inputDiv.style.position="relative",i.input.style.cssText=a,Mn&&(i.scrollbarV.scrollTop=i.scroller.scrollTop=s),ue(e),null!=i.input.selectionStart){
                    (!kn||Mn)&&r(),clearTimeout(Qn);
                    var t=0,n=function(){
                        " "==i.prevInput&&0==i.input.selectionStart?oe(e,ci.selectAll)(e):t++<10?Qn=setTimeout(n,500):he(e)
                    }
                    ;
                    Qn=setTimeout(n,200)
                }
            }
            if(!Vr(e,t,"contextmenu")){
                var i=e.display,o=e.doc.sel;
                if(!ge(i,t)){
                    var l=ve(e,t),s=i.scroller.scrollTop;
                    if(l&&!Hn){
                        (Ke(o.from,o.to)||_e(l,o.from)||!_e(l,o.to))&&oe(e,et)(e.doc,l,l);
                        var a=i.input.style.cssText;
                        if(i.inputDiv.style.position="absolute",i.input.style.cssText="position: fixed;
                        width: 30px;
                        height: 30px;
                        top: "+(t.clientY-5)+"px;
                        left: "+(t.clientX-5)+"px;
                        z-index: 1000;
                        background: white;
                        outline: none;
                        border-width: 0;
                        outline: none;
                        overflow: hidden;
                        opacity: .05;
                        -ms-opacity: .05;
                        filter: alpha(opacity=5);
                        ",de(e),he(e,!0),Ke(o.from,o.to)&&(i.input.value=i.prevInput=" "),kn&&!Mn&&r(),Kn){
                            Dr(t);
                            var u=function(){
                                Pr(window,"mouseup",u),setTimeout(n,20)
                            }
                            ;
                            zr(window,"mouseup",u)
                        }
                        else setTimeout(n,50)
                    }
                }
            }
        }
        function Oe(e,t,r){
            if(!_e(t.from,r))return $e(e,r);
            var n=t.text.length-1-(t.to.line-t.from.line);
            if(r.line>t.to.line+n){
                var i=r.line-n,o=e.first+e.size-1;
                return i>o?Ue(o,cr(e,o).text.length):qe(r,cr(e,i).text.length)
            }
            if(r.line==t.to.line+n)return qe(r,Xr(t.text).length+(1==t.text.length?t.from.ch:0)+cr(e,t.to.line).text.length-t.to.ch);
            var l=r.line-t.from.line;
            return qe(r,t.text[l].length+(l?0:t.from.ch))
        }
        function Ie(e,t,r){
            if(r&&"object"==typeof r)return{
                anchor:Oe(e,t,r.anchor),head:Oe(e,t,r.head)
            }
            ;
            if("start"==r)return{
                anchor:t.from,head:t.from
            }
            ;
            var n=ti(t);
            if("around"==r)return{
                anchor:t.from,head:n
            }
            ;
            if("end"==r)return{
                anchor:n,head:n
            }
            ;
            var i=function(e){
                if(_e(e,t.from))return e;
                if(!_e(t.to,e))return n;
                var r=e.line+t.text.length-(t.to.line-t.from.line)-1,i=e.ch;
                return e.line==t.to.line&&(i+=n.ch-t.to.ch),Ue(r,i)
            }
            ;
            return{
                anchor:i(e.sel.anchor),head:i(e.sel.head)
            }
        }
        function ze(e,t,r){
            var n={
                canceled:!1,from:t.from,to:t.to,text:t.text,origin:t.origin,cancel:function(){
                    this.canceled=!0
                }
            }
            ;
            return r&&(n.update=function(t,r,n,i){
                t&&(this.from=$e(e,t)),r&&(this.to=$e(e,r)),n&&(this.text=n),void 0!==i&&(this.origin=i)
            }),Fr(e,"beforeChange",e,n),e.cm&&Fr(e.cm,"beforeChange",e.cm,n),n.canceled?null:{
                from:n.from,to:n.to,text:n.text,origin:n.origin
            }
        }
        function Pe(e,t,r,n){
            if(e.cm){
                if(!e.cm.curOp)return oe(e.cm,Pe)(e,t,r,n);
                if(e.cm.state.suppressEdits)return
            }
            if(!(Br(e,"beforeChange")||e.cm&&Br(e.cm,"beforeChange"))||(t=ze(e,t,!0))){
                var i=_n&&!n&&Ot(e,t.from,t.to);
                if(i){
                    for(var o=i.length-1;
                    o>=1;
                    --o)Fe(e,{
                        from:i[o].from,to:i[o].to,text:[""]
                    });
                    i.length&&Fe(e,{
                        from:i[0].from,to:i[0].to,text:t.text
                    }
                    ,r)
                }
                else Fe(e,t,r)
            }
        }
        function Fe(e,t,r){
            var n=Ie(e,t,r);
            wr(e,t,n,e.cm?e.cm.curOp.id:NaN),Ge(e,t,n,Et(e,t));
            var i=[];
            ar(e,function(e,r){
                r||-1!=$r(i,e.history)||(Tr(e.history,t),i.push(e.history)),Ge(e,t,null,Et(e,t))
            })
        }
        function Re(e,t){
            if(!e.cm||!e.cm.state.suppressEdits){
                var r=e.history,n=("undo"==t?r.done:r.undone).pop();
                if(n){
                    var i={
                        changes:[],anchorBefore:n.anchorAfter,headBefore:n.headAfter,anchorAfter:n.anchorBefore,headAfter:n.headBefore,generation:r.generation
                    }
                    ;
                    ("undo"==t?r.undone:r.done).push(i),r.generation=n.generation||++r.maxGeneration;
                    for(var o=Br(e,"beforeChange")||e.cm&&Br(e.cm,"beforeChange"),l=n.changes.length-1;
                    l>=0;
                    --l){
                        var s=n.changes[l];
                        if(s.origin=t,o&&!ze(e,s,!1))return void(("undo"==t?r.done:r.undone).length=0);
                        i.changes.push(xr(e,s));
                        var a=l?Ie(e,s,null):{
                            anchor:n.anchorBefore,head:n.headBefore
                        }
                        ;
                        Ge(e,s,a,Dt(e,s));
                        var u=[];
                        ar(e,function(e,t){
                            t||-1!=$r(u,e.history)||(Tr(e.history,s),u.push(e.history)),Ge(e,s,null,Dt(e,s))
                        })
                    }
                }
            }
        }
        function Ve(e,t){
            function r(e){
                return Ue(e.line+t,e.ch)
            }
            e.first+=t,e.cm&&ae(e.cm,e.first,e.first,t),e.sel.head=r(e.sel.head),e.sel.anchor=r(e.sel.anchor),e.sel.from=r(e.sel.from),e.sel.to=r(e.sel.to)
        }
        function Ge(e,t,r,n){
            if(e.cm&&!e.cm.curOp)return oe(e.cm,Ge)(e,t,r,n);
            if(t.to.line<e.first)return void Ve(e,t.text.length-1-(t.to.line-t.from.line));
            if(!(t.from.line>e.lastLine())){
                if(t.from.line<e.first){
                    var i=t.text.length-1-(e.first-t.from.line);
                    Ve(e,i),t={
                        from:Ue(e.first,0),to:Ue(t.to.line+i,t.to.ch),text:[Xr(t.text)],origin:t.origin
                    }
                }
                var o=e.lastLine();
                t.to.line>o&&(t={
                    from:t.from,to:Ue(o,cr(e,o).text.length),text:[t.text[0]],origin:t.origin
                }),t.removed=fr(e,t.from,t.to),r||(r=Ie(e,t,null)),e.cm?Be(e.cm,t,n,r):or(e,t,n,r)
            }
        }
        function Be(e,t,r,n){
            var o=e.doc,l=e.display,s=t.from,a=t.to,u=!1,f=s.line;
            e.options.lineWrapping||(f=pr(Ft(o,cr(o,s.line))),o.iter(f,a.line+1,function(e){
                return e==l.maxLine?(u=!0,!0):void 0
            })),_e(o.sel.head,t.from)||_e(t.to,o.sel.head)||(e.curOp.cursorActivity=!0),or(o,t,r,n,i(e)),e.options.lineWrapping||(o.iter(f,s.line+t.text.length,function(e){
                var t=c(o,e);
                t>l.maxLineLength&&(l.maxLine=e,l.maxLineLength=t,l.maxLineChanged=!0,u=!1)
            }),u&&(e.curOp.updateMaxLine=!0)),o.frontier=Math.min(o.frontier,s.line),E(e,400);
            var h=t.text.length-(a.line-s.line)-1;
            if(ae(e,s.line,a.line+1,h),Br(e,"change")){
                var d={
                    from:s,to:a,text:t.text,removed:t.removed,origin:t.origin
                }
                ;
                if(e.curOp.textChanged){
                    for(var p=e.curOp.textChanged;
                    p.next;
                    p=p.next);
                    p.next=d
                }
                else e.curOp.textChanged=d
            }
        }
        function je(e,t,r,n,i){
            if(n||(n=r),_e(n,r)){
                var o=n;
                n=r,r=o
            }
            "string"==typeof t&&(t=Hi(t)),Pe(e,{
                from:r,to:n,text:t,origin:i
            }
            ,null)
        }
        function Ue(e,t){
            return this instanceof Ue?(this.line=e,void(this.ch=t)):new Ue(e,t)
        }
        function Ke(e,t){
            return e.line==t.line&&e.ch==t.ch
        }
        function _e(e,t){
            return e.line<t.line||e.line==t.line&&e.ch<t.ch
        }
        function Xe(e){
            return Ue(e.line,e.ch)
        }
        function Ye(e,t){
            return Math.max(e.first,Math.min(t,e.first+e.size-1))
        }
        function $e(e,t){
            if(t.line<e.first)return Ue(e.first,0);
            var r=e.first+e.size-1;
            return t.line>r?Ue(r,cr(e,r).text.length):qe(t,cr(e,t.line).text.length)
        }
        function qe(e,t){
            var r=e.ch;
            return null==r||r>t?Ue(e.line,t):0>r?Ue(e.line,0):e
        }
        function Ze(e,t){
            return t>=e.first&&t<e.first+e.size
        }
        function Je(e,t,r,n){
            if(e.sel.shift||e.sel.extend){
                var i=e.sel.anchor;
                if(r){
                    var o=_e(t,i);
                    o!=_e(r,i)?(i=t,t=r):o!=_e(t,r)&&(t=r)
                }
                et(e,i,t,n)
            }
            else et(e,t,r||t,n);
            e.cm&&(e.cm.curOp.userSelChange=!0)
        }
        function Qe(e,t,r){
            var n={
                anchor:t,head:r
            }
            ;
            return Fr(e,"beforeSelectionChange",e,n),e.cm&&Fr(e.cm,"beforeSelectionChange",e.cm,n),n.anchor=$e(e,n.anchor),n.head=$e(e,n.head),n
        }
        function et(e,t,r,n,i){
            if(!i&&Br(e,"beforeSelectionChange")||e.cm&&Br(e.cm,"beforeSelectionChange")){
                var o=Qe(e,t,r);
                r=o.head,t=o.anchor
            }
            var l=e.sel;
            if(l.goalColumn=null,(i||!Ke(t,l.anchor))&&(t=rt(e,t,n,"push"!=i)),(i||!Ke(r,l.head))&&(r=rt(e,r,n,"push"!=i)),!Ke(l.anchor,t)||!Ke(l.head,r)){
                l.anchor=t,l.head=r;
                var s=_e(r,t);
                l.from=s?r:t,l.to=s?t:r,e.cm&&(e.cm.curOp.updateInput=e.cm.curOp.selectionChanged=e.cm.curOp.cursorActivity=!0),Rr(e,"cursorActivity",e)
            }
        }
        function tt(e){
            et(e.doc,e.doc.sel.from,e.doc.sel.to,null,"push")
        }
        function rt(e,t,r,n){
            var i=!1,o=t,l=r||1;
            e.cantEdit=!1;
            e:for(;
            ;
            ){
                var s=cr(e,o.line);
                if(s.markedSpans)for(var a=0;
                a<s.markedSpans.length;
                ++a){
                    var u=s.markedSpans[a],c=u.marker;
                    if((null==u.from||(c.inclusiveLeft?u.from<=o.ch:u.from<o.ch))&&(null==u.to||(c.inclusiveRight?u.to>=o.ch:u.to>o.ch))){
                        if(n&&(Fr(c,"beforeCursorEnter"),c.explicitlyCleared)){
                            if(s.markedSpans){
                                --a;
                                continue
                            }
                            break
                        }
                        if(!c.atomic)continue;
                        var f=c.find()[0>l?"from":"to"];
                        if(Ke(f,o)&&(f.ch+=l,f.ch<0?f=f.line>e.first?$e(e,Ue(f.line-1)):null:f.ch>s.text.length&&(f=f.line<e.first+e.size-1?Ue(f.line+1,0):null),!f)){
                            if(i)return n?(e.cantEdit=!0,Ue(e.first,0)):rt(e,t,r,!0);
                            i=!0,f=t,l=-l
                        }
                        o=f;
                        continue e
                    }
                }
                return o
            }
        }
        function nt(e){
            var t=it(e,e.doc.sel.head,e.options.cursorScrollMargin);
            if(e.state.focused){
                var r=e.display,n=sn(r.sizer),i=null;
                if(t.top+n.top<0?i=!0:t.bottom+n.top>(window.innerHeight||document.documentElement.clientHeight)&&(i=!1),null!=i&&!In){
                    var o="none"==r.cursor.style.display;
                    o&&(r.cursor.style.display="",r.cursor.style.left=t.left+"px",r.cursor.style.top=t.top-r.viewOffset+"px"),r.cursor.scrollIntoView(i),o&&(r.cursor.style.display="none")
                }
            }
        }
        function it(e,t,r){
            for(null==r&&(r=0);
            ;
            ){
                var n=!1,i=Z(e,t),o=lt(e,i.left,i.top-r,i.left,i.bottom+r),l=e.doc.scrollTop,s=e.doc.scrollLeft;
                if(null!=o.scrollTop&&(Ce(e,o.scrollTop),Math.abs(e.doc.scrollTop-l)>1&&(n=!0)),null!=o.scrollLeft&&(Le(e,o.scrollLeft),Math.abs(e.doc.scrollLeft-s)>1&&(n=!0)),!n)return i
            }
        }
        function ot(e,t,r,n,i){
            var o=lt(e,t,r,n,i);
            null!=o.scrollTop&&Ce(e,o.scrollTop),null!=o.scrollLeft&&Le(e,o.scrollLeft)
        }
        function lt(e,t,r,n,i){
            var o=e.display,l=te(e.display);
            0>r&&(r=0);
            var s=o.scroller.clientHeight-Ci,a=o.scroller.scrollTop,u={
                
            }
            ,c=e.doc.height+P(o),f=l>r,h=i>c-l;
            if(a>r)u.scrollTop=f?0:r;
            else if(i>a+s){
                var d=Math.min(r,(h?c:i)-s);
                d!=a&&(u.scrollTop=d)
            }
            var p=o.scroller.clientWidth-Ci,m=o.scroller.scrollLeft;
            t+=o.gutters.offsetWidth,n+=o.gutters.offsetWidth;
            var g=o.gutters.offsetWidth,v=g+10>t;
            return m+g>t||v?(v&&(t=0),u.scrollLeft=Math.max(0,t-10-g)):n>p+m-3&&(u.scrollLeft=n+10-p),u
        }
        function st(e,t,r){
            e.curOp.updateScrollPos={
                scrollLeft:null==t?e.doc.scrollLeft:t,scrollTop:null==r?e.doc.scrollTop:r
            }
        }
        function at(e,t,r){
            var n=e.curOp.updateScrollPos||(e.curOp.updateScrollPos={
                scrollLeft:e.doc.scrollLeft,scrollTop:e.doc.scrollTop
            }),i=e.display.scroller;
            n.scrollTop=Math.max(0,Math.min(i.scrollHeight-i.clientHeight,n.scrollTop+r)),n.scrollLeft=Math.max(0,Math.min(i.scrollWidth-i.clientWidth,n.scrollLeft+t))
        }
        function ut(e,t,r,n){
            var i=e.doc;
            if(null==r&&(r="add"),"smart"==r)if(e.doc.mode.indent)var o=I(e,t);
            else r="prev";
            var l,s=e.options.tabSize,a=cr(i,t),u=Kr(a.text,null,s),c=a.text.match(/^\s*/)[0];
            if("smart"==r&&(l=e.doc.mode.indent(o,a.text.slice(c.length),a.text),l==Li)){
                if(!n)return;
                r="prev"
            }
            "prev"==r?l=t>i.first?Kr(cr(i,t-1).text,null,s):0:"add"==r?l=u+e.options.indentUnit:"subtract"==r?l=u-e.options.indentUnit:"number"==typeof r&&(l=u+r),l=Math.max(0,l);
            var f="",h=0;
            if(e.options.indentWithTabs)for(var d=Math.floor(l/s);
            d;
            --d)h+=s,f+="	";
            l>h&&(f+=_r(l-h)),f!=c&&je(e.doc,f,Ue(t,0),Ue(t,c.length),"+input"),a.stateAfter=null
        }
        function ct(e,t,r){
            var n=t,i=t,o=e.doc;
            return"number"==typeof t?i=cr(o,Ye(o,t)):n=pr(t),null==n?null:r(i,n)?(ae(e,n,n+1),i):null
        }
        function ft(e,t,r,n,i){
            function o(){
                var t=s+r;
                return t<e.first||t>=e.first+e.size?f=!1:(s=t,c=cr(e,t))
            }
            function l(e){
                var t=(i?wn:Cn)(c,a,r,!0);
                if(null==t){
                    if(e||!o())return f=!1;
                    a=i?(0>r?mn:pn)(c):0>r?c.text.length:0
                }
                else a=t;
                return!0
            }
            var s=t.line,a=t.ch,u=r,c=cr(e,s),f=!0;
            if("char"==n)l();
            else if("column"==n)l(!0);
            else if("word"==n||"group"==n)for(var h=null,d="group"==n,p=!0;
            !(0>r)||l(!p);
            p=!1){
                var m=c.text.charAt(a)||"\n",g=en(m)?"w":d?/\s/.test(m)?null:"p":null;
                if(h&&h!=g){
                    0>r&&(r=1,l());
                    break
                }
                if(g&&(h=g),r>0&&!l(!p))break
            }
            var v=rt(e,Ue(s,a),u,!0);
            return f||(v.hitSide=!0),v
        }
        function ht(e,t,r,n){
            var i,o=e.doc,l=t.left;
            if("page"==n){
                var s=Math.min(e.display.wrapper.clientHeight,window.innerHeight||document.documentElement.clientHeight);
                i=t.top+r*(s-(0>r?1.5:.5)*te(e.display))
            }
            else"line"==n&&(i=r>0?t.bottom+3:t.top-3);
            for(;
            ;
            ){
                var a=Q(e,l,i);
                if(!a.outside)break;
                if(0>r?0>=i:i>=o.height){
                    a.hitSide=!0;
                    break
                }
                i+=5*r
            }
            return a
        }
        function dt(e,t){
            var r=t.ch,n=t.ch;
            if(e){
                (t.xRel<0||n==e.length)&&r?--r:++n;
                for(var i=e.charAt(r),o=en(i)?en:/\s/.test(i)?function(e){
                    return/\s/.test(e)
                }
                :function(e){
                    return!/\s/.test(e)&&!en(e)
                }
                ;
                r>0&&o(e.charAt(r-1));
                )--r;
                for(;
                n<e.length&&o(e.charAt(n));
                )++n
            }
            return{
                from:Ue(t.line,r),to:Ue(t.line,n)
            }
        }
        function pt(e,t){
            Je(e.doc,Ue(t,0),$e(e.doc,Ue(t+1,0)))
        }
        function mt(t,r,n,i){
            e.defaults[t]=r,n&&(ri[t]=i?function(e,t,r){
                r!=ii&&n(e,t,r)
            }
            :n)
        }
        function gt(e,t){
            if(t===!0)return t;
            if(e.copyState)return e.copyState(t);
            var r={
                
            }
            ;
            for(var n in t){
                var i=t[n];
                i instanceof Array&&(i=i.concat([])),r[n]=i
            }
            return r
        }
        function vt(e,t,r){
            return e.startState?e.startState(t,r):!0
        }
        function yt(e){
            return"string"==typeof e?fi[e]:e
        }
        function bt(e,t,r){
            function n(t){
                t=yt(t);
                var i=t[e];
                if(i===!1)return"stop";
                if(null!=i&&r(i))return!0;
                if(t.nofallthrough)return"stop";
                var o=t.fallthrough;
                if(null==o)return!1;
                if("[object Array]"!=Object.prototype.toString.call(o))return n(o);
                for(var l=0,s=o.length;
                s>l;
                ++l){
                    var a=n(o[l]);
                    if(a)return a
                }
                return!1
            }
            for(var i=0;
            i<t.length;
            ++i){
                var o=n(t[i]);
                if(o)return"stop"!=o
            }
        }
        function xt(e){
            var t=Di[e.keyCode];
            return"Ctrl"==t||"Alt"==t||"Shift"==t||"Mod"==t
        }
        function wt(e,t){
            if(Hn&&34==e.keyCode&&e["char"])return!1;
            var r=Di[e.keyCode];
            return null==r||e.altGraphKey?!1:(e.altKey&&(r="Alt-"+r),(Un?e.metaKey:e.ctrlKey)&&(r="Ctrl-"+r),(Un?e.ctrlKey:e.metaKey)&&(r="Cmd-"+r),!t&&e.shiftKey&&(r="Shift-"+r),r)
        }
        function Ct(e,t){
            this.pos=this.start=0,this.string=e,this.tabSize=t||8,this.lastColumnPos=this.lastColumnValue=0
        }
        function Lt(e,t){
            this.lines=[],this.type=t,this.doc=e
        }
        function kt(e,t,r,n,i){
            if(n&&n.shared)return Mt(e,t,r,n,i);
            if(e.cm&&!e.cm.curOp)return oe(e.cm,kt)(e,t,r,n,i);
            var o=new Lt(e,i);
            if("range"==i&&!_e(t,r))return o;
            n&&Zr(n,o),o.replacedWith&&(o.collapsed=!0,o.replacedWith=rn("span",[o.replacedWith],"CodeMirror-widget"),n.handleMouseEvents||(o.replacedWith.ignoreEvents=!0)),o.collapsed&&(Xn=!0),o.addToHistory&&wr(e,{
                from:t,to:r,origin:"markText"
            }
            ,{
                head:e.sel.head,anchor:e.sel.anchor
            }
            ,NaN);
            var l,s,a,u=t.line,c=0,f=e.cm;
            if(e.iter(u,r.line+1,function(n){
                f&&o.collapsed&&!f.options.lineWrapping&&Ft(e,n)==f.display.maxLine&&(a=!0);
                var i={
                    from:null,to:null,marker:o
                }
                ;
                c+=n.text.length,u==t.line&&(i.from=t.ch,c-=t.ch),u==r.line&&(i.to=r.ch,c-=n.text.length-r.ch),o.collapsed&&(u==r.line&&(s=It(n,r.ch)),u==t.line?l=It(n,t.ch):dr(n,0)),Nt(n,i),++u
            }),o.collapsed&&e.iter(t.line,r.line+1,function(t){
                Rt(e,t)&&dr(t,0)
            }),o.clearOnEnter&&zr(o,"beforeCursorEnter",function(){
                o.clear()
            }),o.readOnly&&(_n=!0,(e.history.done.length||e.history.undone.length)&&e.clearHistory()),o.collapsed){
                if(l!=s)throw new Error("Inserting collapsed marker overlapping an existing one");
                o.size=c,o.atomic=!0
            }
            return f&&(a&&(f.curOp.updateMaxLine=!0),(o.className||o.title||o.startStyle||o.endStyle||o.collapsed)&&ae(f,t.line,r.line+1),o.atomic&&tt(f)),o
        }
        function St(e,t){
            this.markers=e,this.primary=t;
            for(var r=0,n=this;
            r<e.length;
            ++r)e[r].parent=this,zr(e[r],"clear",function(){
                n.clear()
            })
        }
        function Mt(e,t,r,n,i){
            n=Zr(n),n.shared=!1;
            var o=[kt(e,t,r,n,i)],l=o[0],s=n.replacedWith;
            return ar(e,function(e){
                s&&(n.replacedWith=s.cloneNode(!0)),o.push(kt(e,$e(e,t),$e(e,r),n,i));
                for(var a=0;
                a<e.linked.length;
                ++a)if(e.linked[a].isParent)return;
                l=Xr(o)
            }),new St(o,l)
        }
        function Tt(e,t){
            if(e)for(var r=0;
            r<e.length;
            ++r){
                var n=e[r];
                if(n.marker==t)return n
            }
        }
        function At(e,t){
            for(var r,n=0;
            n<e.length;
            ++n)e[n]!=t&&(r||(r=[])).push(e[n]);
            return r
        }
        function Nt(e,t){
            e.markedSpans=e.markedSpans?e.markedSpans.concat([t]):[t],t.marker.attachLine(e)
        }
        function Ht(e,t,r){
            if(e)for(var n,i=0;
            i<e.length;
            ++i){
                var o=e[i],l=o.marker,s=null==o.from||(l.inclusiveLeft?o.from<=t:o.from<t);
                if(s||"bookmark"==l.type&&o.from==t&&(!r||!o.marker.insertLeft)){
                    var a=null==o.to||(l.inclusiveRight?o.to>=t:o.to>t);
                    (n||(n=[])).push({
                        from:o.from,to:a?null:o.to,marker:l
                    })
                }
            }
            return n
        }
        function Wt(e,t,r){
            if(e)for(var n,i=0;
            i<e.length;
            ++i){
                var o=e[i],l=o.marker,s=null==o.to||(l.inclusiveRight?o.to>=t:o.to>t);
                if(s||"bookmark"==l.type&&o.from==t&&(!r||o.marker.insertLeft)){
                    var a=null==o.from||(l.inclusiveLeft?o.from<=t:o.from<t);
                    (n||(n=[])).push({
                        from:a?null:o.from-t,to:null==o.to?null:o.to-t,marker:l
                    })
                }
            }
            return n
        }
        function Et(e,t){
            var r=Ze(e,t.from.line)&&cr(e,t.from.line).markedSpans,n=Ze(e,t.to.line)&&cr(e,t.to.line).markedSpans;
            if(!r&&!n)return null;
            var i=t.from.ch,o=t.to.ch,l=Ke(t.from,t.to),s=Ht(r,i,l),a=Wt(n,o,l),u=1==t.text.length,c=Xr(t.text).length+(u?i:0);
            if(s)for(var f=0;
            f<s.length;
            ++f){
                var h=s[f];
                if(null==h.to){
                    var d=Tt(a,h.marker);
                    d?u&&(h.to=null==d.to?null:d.to+c):h.to=i
                }
            }
            if(a)for(var f=0;
            f<a.length;
            ++f){
                var h=a[f];
                if(null!=h.to&&(h.to+=c),null==h.from){
                    var d=Tt(s,h.marker);
                    d||(h.from=c,u&&(s||(s=[])).push(h))
                }
                else h.from+=c,u&&(s||(s=[])).push(h)
            }
            if(u&&s){
                for(var f=0;
                f<s.length;
                ++f)null!=s[f].from&&s[f].from==s[f].to&&"bookmark"!=s[f].marker.type&&s.splice(f--,1);
                s.length||(s=null)
            }
            var p=[s];
            if(!u){
                var m,g=t.text.length-2;
                if(g>0&&s)for(var f=0;
                f<s.length;
                ++f)null==s[f].to&&(m||(m=[])).push({
                    from:null,to:null,marker:s[f].marker
                });
                for(var f=0;
                g>f;
                ++f)p.push(m);
                p.push(a)
            }
            return p
        }
        function Dt(e,t){
            var r=Lr(e,t),n=Et(e,t);
            if(!r)return n;
            if(!n)return r;
            for(var i=0;
            i<r.length;
            ++i){
                var o=r[i],l=n[i];
                if(o&&l)e:for(var s=0;
                s<l.length;
                ++s){
                    for(var a=l[s],u=0;
                    u<o.length;
                    ++u)if(o[u].marker==a.marker)continue e;
                    o.push(a)
                }
                else l&&(r[i]=l)
            }
            return r
        }
        function Ot(e,t,r){
            var n=null;
            if(e.iter(t.line,r.line+1,function(e){
                if(e.markedSpans)for(var t=0;
                t<e.markedSpans.length;
                ++t){
                    var r=e.markedSpans[t].marker;
                    !r.readOnly||n&&-1!=$r(n,r)||(n||(n=[])).push(r)
                }
            }),!n)return null;
            for(var i=[{
                from:t,to:r
            }
            ],o=0;
            o<n.length;
            ++o)for(var l=n[o],s=l.find(),a=0;
            a<i.length;
            ++a){
                var u=i[a];
                if(!_e(u.to,s.from)&&!_e(s.to,u.from)){
                    var c=[a,1];
                    (_e(u.from,s.from)||!l.inclusiveLeft&&Ke(u.from,s.from))&&c.push({
                        from:u.from,to:s.from
                    }),(_e(s.to,u.to)||!l.inclusiveRight&&Ke(u.to,s.to))&&c.push({
                        from:s.to,to:u.to
                    }),i.splice.apply(i,c),a+=c.length-1
                }
            }
            return i
        }
        function It(e,t){
            var r,n=Xn&&e.markedSpans;
            if(n)for(var i,o=0;
            o<n.length;
            ++o)i=n[o],i.marker.collapsed&&(null==i.from||i.from<t)&&(null==i.to||i.to>t)&&(!r||r.width<i.marker.width)&&(r=i.marker);
            return r
        }
        function zt(e){
            return It(e,-1)
        }
        function Pt(e){
            return It(e,e.text.length+1)
        }
        function Ft(e,t){
            for(var r;
            r=zt(t);
            )t=cr(e,r.find().from.line);
            return t
        }
        function Rt(e,t){
            var r=Xn&&t.markedSpans;
            if(r)for(var n,i=0;
            i<r.length;
            ++i)if(n=r[i],n.marker.collapsed){
                if(null==n.from)return!0;
                if(!n.marker.replacedWith&&0==n.from&&n.marker.inclusiveLeft&&Vt(e,t,n))return!0
            }
        }
        function Vt(e,t,r){
            if(null==r.to){
                var n=r.marker.find().to,i=cr(e,n.line);
                return Vt(e,i,Tt(i.markedSpans,r.marker))
            }
            if(r.marker.inclusiveRight&&r.to==t.text.length)return!0;
            for(var o,l=0;
            l<t.markedSpans.length;
            ++l)if(o=t.markedSpans[l],o.marker.collapsed&&!o.marker.replacedWith&&o.from==r.to&&(o.marker.inclusiveLeft||r.marker.inclusiveRight)&&Vt(e,t,o))return!0
        }
        function Gt(e){
            var t=e.markedSpans;
            if(t){
                for(var r=0;
                r<t.length;
                ++r)t[r].marker.detachLine(e);
                e.markedSpans=null
            }
        }
        function Bt(e,t){
            if(t){
                for(var r=0;
                r<t.length;
                ++r)t[r].marker.attachLine(e);
                e.markedSpans=t
            }
        }
        function jt(e){
            return function(){
                var t=!this.cm.curOp;
                t&&ne(this.cm);
                try{
                    var r=e.apply(this,arguments)
                }
                finally{
                    t&&ie(this.cm)
                }
                return r
            }
        }
        function Ut(e){
            return null!=e.height?e.height:(e.node.parentNode&&1==e.node.parentNode.nodeType||on(e.cm.display.measure,rn("div",[e.node],null,"position: relative")),e.height=e.node.offsetHeight)
        }
        function Kt(e,t,r,n){
            var i=new hi(e,r,n);
            return i.noHScroll&&(e.display.alignWidgets=!0),ct(e,t,function(t){
                var r=t.widgets||(t.widgets=[]);
                if(null==i.insertAt?r.push(i):r.splice(Math.min(r.length-1,Math.max(0,i.insertAt)),0,i),i.line=t,!Rt(e.doc,t)||i.showIfHidden){
                    var n=gr(e,t)<e.doc.scrollTop;
                    dr(t,t.height+Ut(i)),n&&at(e,0,i.height)
                }
                return!0
            }),i
        }
        function _t(e,t,r,n){
            e.text=t,e.stateAfter&&(e.stateAfter=null),e.styles&&(e.styles=null),null!=e.order&&(e.order=null),Gt(e),Bt(e,r);
            var i=n?n(e):1;
            i!=e.height&&dr(e,i)
        }
        function Xt(e){
            e.parent=null,Gt(e)
        }
        function Yt(e,t,r,n,i){
            var o=r.flattenSpans;
            null==o&&(o=e.options.flattenSpans);
            var l,s=0,a=null,u=new Ct(t,e.options.tabSize);
            for(""==t&&r.blankLine&&r.blankLine(n);
            !u.eol();
            )u.pos>e.options.maxHighlightLength?(o=!1,u.pos=Math.min(t.length,u.start+5e4),l=null):l=r.token(u,n),o&&a==l||(s<u.start&&i(u.start,a),s=u.start,a=l),u.start=u.pos;
            s<u.pos&&i(u.pos,a)
        }
        function $t(e,t,r){
            var n=[e.state.modeGen];
            Yt(e,t.text,e.doc.mode,r,function(e,t){
                n.push(e,t)
            });
            for(var i=0;
            i<e.state.overlays.length;
            ++i){
                var o=e.state.overlays[i],l=1,s=0;
                Yt(e,t.text,o.mode,!0,function(e,t){
                    for(var r=l;
                    e>s;
                    ){
                        var i=n[l];
                        i>e&&n.splice(l,1,e,n[l+1],i),l+=2,s=Math.min(e,i)
                    }
                    if(t)if(o.opaque)n.splice(r,l-r,e,t),l=r+2;
                    else for(;
                    l>r;
                    r+=2){
                        var a=n[r+1];
                        n[r+1]=a?a+" "+t:t
                    }
                })
            }
            return n
        }
        function qt(e,t){
            return t.styles&&t.styles[0]==e.state.modeGen||(t.styles=$t(e,t,t.stateAfter=I(e,pr(t)))),t.styles
        }
        function Zt(e,t,r){
            var n=e.doc.mode,i=new Ct(t.text,e.options.tabSize);
            for(""==t.text&&n.blankLine&&n.blankLine(r);
            !i.eol()&&i.pos<=e.options.maxHighlightLength;
            )n.token(i,r),i.start=i.pos
        }
        function Jt(e){
            return e?pi[e]||(pi[e]="cm-"+e.replace(/ +/g," cm-")):null
        }
        function Qt(e,t,r,n){
            for(var i,o=t,l=!0;
            i=zt(o);
            )o=cr(e.doc,i.find().from.line);
            var s={
                pre:rn("pre"),col:0,pos:0,measure:null,measuredSomething:!1,cm:e,copyWidgets:n
            }
            ;
            o.textClass&&(s.pre.className=o.textClass);
            do{
                o.text&&(l=!1),s.measure=o==t&&r,s.pos=0,s.addToken=s.measure?tr:er,(kn||Tn)&&e.getOption("lineWrapping")&&(s.addToken=rr(s.addToken));
                var a=ir(o,s,qt(e,o));
                r&&o==t&&!s.measuredSomething&&(r[0]=s.pre.appendChild(cn(e.display.measure)),s.measuredSomething=!0),a&&(o=cr(e.doc,a.to.line))
            }
            while(a);
            !r||s.measuredSomething||r[0]||(r[0]=s.pre.appendChild(l?rn("span","\xa0"):cn(e.display.measure))),s.pre.firstChild||Rt(e.doc,t)||s.pre.appendChild(document.createTextNode("\xa0"));
            var u;
            if(r&&kn&&(u=vr(o))){
                var c=u.length-1;
                u[c].from==u[c].to&&--c;
                var f=u[c],h=u[c-1];
                if(f.from+1==f.to&&h&&f.level<h.level){
                    var d=r[s.pos-1];
                    d&&d.parentNode.insertBefore(d.measureRight=cn(e.display.measure),d.nextSibling)
                }
            }
            return Fr(e,"renderLine",e,t,s.pre),s.pre
        }
        function er(e,t,r,n,i,o){
            if(t){
                if(mi.test(t))for(var l=document.createDocumentFragment(),s=0;
                ;
                ){
                    mi.lastIndex=s;
                    var a=mi.exec(t),u=a?a.index-s:t.length-s;
                    if(u&&(l.appendChild(document.createTextNode(t.slice(s,s+u))),e.col+=u),!a)break;
                    if(s+=u+1,"	"==a[0]){
                        var c=e.cm.options.tabSize,f=c-e.col%c;
                        l.appendChild(rn("span",_r(f),"cm-tab")),e.col+=f
                    }
                    else{
                        var h=rn("span","\u2022","cm-invalidchar");
                        h.title="\\u"+a[0].charCodeAt(0).toString(16),l.appendChild(h),e.col+=1
                    }
                }
                else{
                    e.col+=t.length;
                    var l=document.createTextNode(t)
                }
                if(r||n||i||e.measure){
                    var d=r||"";
                    n&&(d+=n),i&&(d+=i);
                    var h=rn("span",[l],d);
                    return o&&(h.title=o),e.pre.appendChild(h)
                }
                e.pre.appendChild(l)
            }
        }
        function tr(e,t,r,n,i){
            for(var o=e.cm.options.lineWrapping,l=0;
            l<t.length;
            ++l){
                var s=t.charAt(l),a=0==l;
                s>="\ud800"&&"\udbff">s&&l<t.length-1?(s=t.slice(l,l+2),++l):l&&o&&an(t,l)&&e.pre.appendChild(rn("wbr"));
                var u=e.measure[e.pos],c=e.measure[e.pos]=er(e,s,r,a&&n,l==t.length-1&&i);
                u&&(c.leftSide=u.leftSide||u),kn&&o&&" "==s&&l&&!/\s/.test(t.charAt(l-1))&&l<t.length-1&&!/\s/.test(t.charAt(l+1))&&(c.style.whiteSpace="normal"),e.pos+=s.length
            }
            t.length&&(e.measuredSomething=!0)
        }
        function rr(e){
            function t(e){
                for(var t=" ",r=0;
                r<e.length-2;
                ++r)t+=r%2?" ":"\xa0";
                return t+=" "
            }
            return function(r,n,i,o,l,s){
                return e(r,n.replace(/ {
                    3,
                }
                /,t),i,o,l,s)
            }
        }
        function nr(e,t,r,n){
            var i=!n&&r.replacedWith;
            if(i&&(e.copyWidgets&&(i=i.cloneNode(!0)),e.pre.appendChild(i),e.measure)){
                if(t)e.measure[e.pos]=i;
                else{
                    var o=e.measure[e.pos]=cn(e.cm.display.measure);
                    "bookmark"!=r.type||r.insertLeft?e.pre.insertBefore(o,i):e.pre.appendChild(o)
                }
                e.measuredSomething=!0
            }
            e.pos+=t
        }
        function ir(e,t,r){
            var n=e.markedSpans,i=e.text,o=0;
            if(n)for(var l,s,a,u,c,f,h=i.length,d=0,p=1,m="",g=0;
            ;
            ){
                if(g==d){
                    s=a=u=c="",f=null,g=1/0;
                    for(var v=null,y=0;
                    y<n.length;
                    ++y){
                        var b=n[y],x=b.marker;
                        b.from<=d&&(null==b.to||b.to>d)?(null!=b.to&&g>b.to&&(g=b.to,a=""),x.className&&(s+=" "+x.className),x.startStyle&&b.from==d&&(u+=" "+x.startStyle),x.endStyle&&b.to==g&&(a+=" "+x.endStyle),x.title&&!c&&(c=x.title),x.collapsed&&(!f||f.marker.size<x.size)&&(f=b)):b.from>d&&g>b.from&&(g=b.from),"bookmark"==x.type&&b.from==d&&x.replacedWith&&(v=x)
                    }
                    if(f&&(f.from||0)==d&&(nr(t,(null==f.to?h:f.to)-d,f.marker,null==f.from),null==f.to))return f.marker.find();
                    v&&!f&&nr(t,0,v)
                }
                if(d>=h)break;
                for(var w=Math.min(h,g);
                ;
                ){
                    if(m){
                        var C=d+m.length;
                        if(!f){
                            var L=C>w?m.slice(0,w-d):m;
                            t.addToken(t,L,l?l+s:s,u,d+L.length==g?a:"",c)
                        }
                        if(C>=w){
                            m=m.slice(w-d),d=w;
                            break
                        }
                        d=C,u=""
                    }
                    m=i.slice(o,o=r[p++]),l=Jt(r[p++])
                }
            }
            else for(var p=1;
            p<r.length;
            p+=2)t.addToken(t,i.slice(o,o=r[p]),Jt(r[p+1]))
        }
        function or(e,t,r,n,i){
            function o(e){
                return r?r[e]:null
            }
            function l(e,r,n){
                _t(e,r,n,i),Rr(e,"change",e,t)
            }
            var s=t.from,a=t.to,u=t.text,c=cr(e,s.line),f=cr(e,a.line),h=Xr(u),d=o(u.length-1),p=a.line-s.line;
            if(0==s.ch&&0==a.ch&&""==h){
                for(var m=0,g=u.length-1,v=[];
                g>m;
                ++m)v.push(new di(u[m],o(m),i));
                l(f,f.text,d),p&&e.remove(s.line,p),v.length&&e.insert(s.line,v)
            }
            else if(c==f)if(1==u.length)l(c,c.text.slice(0,s.ch)+h+c.text.slice(a.ch),d);
            else{
                for(var v=[],m=1,g=u.length-1;
                g>m;
                ++m)v.push(new di(u[m],o(m),i));
                v.push(new di(h+c.text.slice(a.ch),d,i)),l(c,c.text.slice(0,s.ch)+u[0],o(0)),e.insert(s.line+1,v)
            }
            else if(1==u.length)l(c,c.text.slice(0,s.ch)+u[0]+f.text.slice(a.ch),o(0)),e.remove(s.line+1,p);
            else{
                l(c,c.text.slice(0,s.ch)+u[0],o(0)),l(f,h+f.text.slice(a.ch),d);
                for(var m=1,g=u.length-1,v=[];
                g>m;
                ++m)v.push(new di(u[m],o(m),i));
                p>1&&e.remove(s.line+1,p-1),e.insert(s.line+1,v)
            }
            Rr(e,"change",e,t),et(e,n.anchor,n.head,null,!0)
        }
        function lr(e){
            this.lines=e,this.parent=null;
            for(var t=0,r=e.length,n=0;
            r>t;
            ++t)e[t].parent=this,n+=e[t].height;
            this.height=n
        }
        function sr(e){
            this.children=e;
            for(var t=0,r=0,n=0,i=e.length;
            i>n;
            ++n){
                var o=e[n];
                t+=o.chunkSize(),r+=o.height,o.parent=this
            }
            this.size=t,this.height=r,this.parent=null
        }
        function ar(e,t,r){
            function n(e,i,o){
                if(e.linked)for(var l=0;
                l<e.linked.length;
                ++l){
                    var s=e.linked[l];
                    if(s.doc!=i){
                        var a=o&&s.sharedHist;
                        (!r||a)&&(t(s.doc,a),n(s.doc,e,a))
                    }
                }
            }
            n(e,null,!0)
        }
        function ur(e,t){
            if(t.cm)throw new Error("This document is already in use.");
            e.doc=t,t.cm=e,o(e),r(e),e.options.lineWrapping||f(e),e.options.mode=t.modeOption,ae(e)
        }
        function cr(e,t){
            for(t-=e.first;
            !e.lines;
            )for(var r=0;
            ;
            ++r){
                var n=e.children[r],i=n.chunkSize();
                if(i>t){
                    e=n;
                    break
                }
                t-=i
            }
            return e.lines[t]
        }
        function fr(e,t,r){
            var n=[],i=t.line;
            return e.iter(t.line,r.line+1,function(e){
                var o=e.text;
                i==r.line&&(o=o.slice(0,r.ch)),i==t.line&&(o=o.slice(t.ch)),n.push(o),++i
            }),n
        }
        function hr(e,t,r){
            var n=[];
            return e.iter(t,r,function(e){
                n.push(e.text)
            }),n
        }
        function dr(e,t){
            for(var r=t-e.height,n=e;
            n;
            n=n.parent)n.height+=r
        }
        function pr(e){
            if(null==e.parent)return null;
            for(var t=e.parent,r=$r(t.lines,e),n=t.parent;
            n;
            t=n,n=n.parent)for(var i=0;
            n.children[i]!=t;
            ++i)r+=n.children[i].chunkSize();
            return r+t.first
        }
        function mr(e,t){
            var r=e.first;
            e:do{
                for(var n=0,i=e.children.length;
                i>n;
                ++n){
                    var o=e.children[n],l=o.height;
                    if(l>t){
                        e=o;
                        continue e
                    }
                    t-=l,r+=o.chunkSize()
                }
                return r
            }
            while(!e.lines);
            for(var n=0,i=e.lines.length;
            i>n;
            ++n){
                var s=e.lines[n],a=s.height;
                if(a>t)break;
                t-=a
            }
            return r+n
        }
        function gr(e,t){
            t=Ft(e.doc,t);
            for(var r=0,n=t.parent,i=0;
            i<n.lines.length;
            ++i){
                var o=n.lines[i];
                if(o==t)break;
                r+=o.height
            }
            for(var l=n.parent;
            l;
            n=l,l=n.parent)for(var i=0;
            i<l.children.length;
            ++i){
                var s=l.children[i];
                if(s==n)break;
                r+=s.height
            }
            return r
        }
        function vr(e){
            var t=e.order;
            return null==t&&(t=e.order=Ii(e.text)),t
        }
        function yr(e){
            return{
                done:[],undone:[],undoDepth:1/0,lastTime:0,lastOp:null,lastOrigin:null,generation:e||1,maxGeneration:e||1
            }
        }
        function br(e,t,r,n){
            var i=t["spans_"+e.id],o=0;
            e.iter(Math.max(e.first,r),Math.min(e.first+e.size,n),function(r){
                r.markedSpans&&((i||(i=t["spans_"+e.id]={
                    
                }))[o]=r.markedSpans),++o
            })
        }
        function xr(e,t){
            var r={
                line:t.from.line,ch:t.from.ch
            }
            ,n={
                from:r,to:ti(t),text:fr(e,t.from,t.to)
            }
            ;
            return br(e,n,t.from.line,t.to.line+1),ar(e,function(e){
                br(e,n,t.from.line,t.to.line+1)
            }
            ,!0),n
        }
        function wr(e,t,r,n){
            var i=e.history;
            i.undone.length=0;
            var o=+new Date,l=Xr(i.done);
            if(l&&(i.lastOp==n||i.lastOrigin==t.origin&&t.origin&&("+"==t.origin.charAt(0)&&e.cm&&i.lastTime>o-e.cm.options.historyEventDelay||"*"==t.origin.charAt(0)))){
                var s=Xr(l.changes);
                Ke(t.from,t.to)&&Ke(t.from,s.to)?s.to=ti(t):l.changes.push(xr(e,t)),l.anchorAfter=r.anchor,l.headAfter=r.head
            }
            else for(l={
                changes:[xr(e,t)],generation:i.generation,anchorBefore:e.sel.anchor,headBefore:e.sel.head,anchorAfter:r.anchor,headAfter:r.head
            }
            ,i.done.push(l),i.generation=++i.maxGeneration;
            i.done.length>i.undoDepth;
            )i.done.shift();
            i.lastTime=o,i.lastOp=n,i.lastOrigin=t.origin
        }
        function Cr(e){
            if(!e)return null;
            for(var t,r=0;
            r<e.length;
            ++r)e[r].marker.explicitlyCleared?t||(t=e.slice(0,r)):t&&t.push(e[r]);
            return t?t.length?t:null:e
        }
        function Lr(e,t){
            var r=t["spans_"+e.id];
            if(!r)return null;
            for(var n=0,i=[];
            n<t.text.length;
            ++n)i.push(Cr(r[n]));
            return i
        }
        function kr(e,t){
            for(var r=0,n=[];
            r<e.length;
            ++r){
                var i=e[r],o=i.changes,l=[];
                n.push({
                    changes:l,anchorBefore:i.anchorBefore,headBefore:i.headBefore,anchorAfter:i.anchorAfter,headAfter:i.headAfter
                });
                for(var s=0;
                s<o.length;
                ++s){
                    var a,u=o[s];
                    if(l.push({
                        from:u.from,to:u.to,text:u.text
                    }),t)for(var c in u)(a=c.match(/^spans_(\d+)$/))&&$r(t,Number(a[1]))>-1&&(Xr(l)[c]=u[c],delete u[c])
                }
            }
            return n
        }
        function Sr(e,t,r,n){
            r<e.line?e.line+=n:t<e.line&&(e.line=t,e.ch=0)
        }
        function Mr(e,t,r,n){
            for(var i=0;
            i<e.length;
            ++i){
                for(var o=e[i],l=!0,s=0;
                s<o.changes.length;
                ++s){
                    var a=o.changes[s];
                    if(o.copied||(a.from=Xe(a.from),a.to=Xe(a.to)),r<a.from.line)a.from.line+=n,a.to.line+=n;
                    else if(t<=a.to.line){
                        l=!1;
                        break
                    }
                }
                o.copied||(o.anchorBefore=Xe(o.anchorBefore),o.headBefore=Xe(o.headBefore),o.anchorAfter=Xe(o.anchorAfter),o.readAfter=Xe(o.headAfter),o.copied=!0),l?(Sr(o.anchorBefore),Sr(o.headBefore),Sr(o.anchorAfter),Sr(o.headAfter)):(e.splice(0,i+1),i=0)
            }
        }
        function Tr(e,t){
            var r=t.from.line,n=t.to.line,i=t.text.length-(n-r)-1;
            Mr(e.done,r,n,i),Mr(e.undone,r,n,i)
        }
        function Ar(){
            Dr(this)
        }
        function Nr(e){
            return e.stop||(e.stop=Ar),e
        }
        function Hr(e){
            e.preventDefault?e.preventDefault():e.returnValue=!1
        }
        function Wr(e){
            e.stopPropagation?e.stopPropagation():e.cancelBubble=!0
        }
        function Er(e){
            return null!=e.defaultPrevented?e.defaultPrevented:0==e.returnValue
        }
        function Dr(e){
            Hr(e),Wr(e)
        }
        function Or(e){
            return e.target||e.srcElement
        }
        function Ir(e){
            var t=e.which;
            return null==t&&(1&e.button?t=1:2&e.button?t=3:4&e.button&&(t=2)),Fn&&e.ctrlKey&&1==t&&(t=3),t
        }
        function zr(e,t,r){
            if(e.addEventListener)e.addEventListener(t,r,!1);
            else if(e.attachEvent)e.attachEvent("on"+t,r);
            else{
                var n=e._handlers||(e._handlers={
                    
                }),i=n[t]||(n[t]=[]);
                i.push(r)
            }
        }
        function Pr(e,t,r){
            if(e.removeEventListener)e.removeEventListener(t,r,!1);
            else if(e.detachEvent)e.detachEvent("on"+t,r);
            else{
                var n=e._handlers&&e._handlers[t];
                if(!n)return;
                for(var i=0;
                i<n.length;
                ++i)if(n[i]==r){
                    n.splice(i,1);
                    break
                }
            }
        }
        function Fr(e,t){
            var r=e._handlers&&e._handlers[t];
            if(r)for(var n=Array.prototype.slice.call(arguments,2),i=0;
            i<r.length;
            ++i)r[i].apply(null,n)
        }
        function Rr(e,t){
            function r(e){
                return function(){
                    e.apply(null,i)
                }
            }
            var n=e._handlers&&e._handlers[t];
            if(n){
                var i=Array.prototype.slice.call(arguments,2);
                xi||(++wi,xi=[],setTimeout(Gr,0));
                for(var o=0;
                o<n.length;
                ++o)xi.push(r(n[o]))
            }
        }
        function Vr(e,t,r){
            return Fr(e,r||t.type,e,t),Er(t)||t.codemirrorIgnore
        }
        function Gr(){
            --wi;
            var e=xi;
            xi=null;
            for(var t=0;
            t<e.length;
            ++t)e[t]()
        }
        function Br(e,t){
            var r=e._handlers&&e._handlers[t];
            return r&&r.length>0
        }
        function jr(e){
            e.prototype.on=function(e,t){
                zr(this,e,t)
            }
            ,e.prototype.off=function(e,t){
                Pr(this,e,t)
            }
        }
        function Ur(){
            this.id=null
        }
        function Kr(e,t,r,n,i){
            null==t&&(t=e.search(/[^\s\u00a0]/),-1==t&&(t=e.length));
            for(var o=n||0,l=i||0;
            t>o;
            ++o)"	"==e.charAt(o)?l+=r-l%r:++l;
            return l
        }
        function _r(e){
            for(;
            ki.length<=e;
            )ki.push(Xr(ki)+" ");
            return ki[e]
        }
        function Xr(e){
            return e[e.length-1]
        }
        function Yr(e){
            if(zn)e.selectionStart=0,e.selectionEnd=e.value.length;
            else try{
                e.select()
            }
            catch(t){
                
            }
        }
        function $r(e,t){
            if(e.indexOf)return e.indexOf(t);
            for(var r=0,n=e.length;
            n>r;
            ++r)if(e[r]==t)return r;
            return-1
        }
        function qr(e,t){
            function r(){
                
            }
            r.prototype=e;
            var n=new r;
            return t&&Zr(t,n),n
        }
        function Zr(e,t){
            t||(t={
                
            });
            for(var r in e)e.hasOwnProperty(r)&&(t[r]=e[r]);
            return t
        }
        function Jr(e){
            for(var t=[],r=0;
            e>r;
            ++r)t.push(void 0);
            return t
        }
        function Qr(e){
            var t=Array.prototype.slice.call(arguments,1);
            return function(){
                return e.apply(null,t)
            }
        }
        function en(e){
            return/\w/.test(e)||e>"\x80"&&(e.toUpperCase()!=e.toLowerCase()||Si.test(e))
        }
        function tn(e){
            for(var t in e)if(e.hasOwnProperty(t)&&e[t])return!1;
            return!0
        }
        function rn(e,t,r,n,k){
            var i=document.createElement(e);
            if(k) i.id=k;
            if(r&&(i.className=r),n&&(i.style.cssText=n),"string"==typeof t)ln(i,t);
            else if(t)for(var o=0;
            o<t.length;
            ++o)i.appendChild(t[o]);
            return i
        }
        function nn(e){
            for(var t=e.childNodes.length;
            t>0;
            --t)e.removeChild(e.firstChild);
            return e
        }
        function on(e,t){
            return nn(e).appendChild(t)
        }
        function ln(e,t){
            Mn?(e.innerHTML="",e.appendChild(document.createTextNode(t))):e.textContent=t
        }
        function sn(e){
            return e.getBoundingClientRect()
        }
        function an(){
            return!1
        }
        function un(e){
            if(null!=Ai)return Ai;
            var t=rn("div",null,null,"width: 50px;
            height: 50px;
            overflow-x: scroll");
            return on(e,t),t.offsetWidth&&(Ai=t.offsetHeight-t.clientHeight),Ai||0
        }
        function cn(e){
            if(null==Ni){
                var t=rn("span","\u200b");
                on(e,rn("span",[t,document.createTextNode("x")])),0!=e.firstChild.offsetHeight&&(Ni=t.offsetWidth<=1&&t.offsetHeight>2&&!Sn)
            }
            return Ni?rn("span","\u200b"):rn("span","\xa0",null,"display: inline-block;
            width: 1px;
            margin-right: -1px")
        }
        function fn(e,t,r,n){
            if(!e)return n(t,r,"ltr");
            for(var i=!1,o=0;
            o<e.length;
            ++o){
                var l=e[o];
                (l.from<r&&l.to>t||t==r&&l.to==t)&&(n(Math.max(l.from,t),Math.min(l.to,r),1==l.level?"rtl":"ltr"),i=!0)
            }
            i||n(t,r,"ltr")
        }
        function hn(e){
            return e.level%2?e.to:e.from
        }
        function dn(e){
            return e.level%2?e.from:e.to
        }
        function pn(e){
            var t=vr(e);
            return t?hn(t[0]):0
        }
        function mn(e){
            var t=vr(e);
            return t?dn(Xr(t)):e.text.length
        }
        function gn(e,t){
            var r=cr(e.doc,t),n=Ft(e.doc,r);
            n!=r&&(t=pr(n));
            var i=vr(n),o=i?i[0].level%2?mn(n):pn(n):0;
            return Ue(t,o)
        }
        function vn(e,t){
            for(var r,n;
            r=Pt(n=cr(e.doc,t));
            )t=r.find().to.line;
            var i=vr(n),o=i?i[0].level%2?pn(n):mn(n):n.text.length;
            return Ue(t,o)
        }
        function yn(e,t,r){
            var n=e[0].level;
            return t==n?!0:r==n?!1:r>t
        }
        function bn(e,t){
            for(var r,n=0;
            n<e.length;
            ++n){
                var i=e[n];
                if(i.from<t&&i.to>t)return Oi=null,n;
                if(i.from==t||i.to==t){
                    if(null!=r)return yn(e,i.level,e[r].level)?(Oi=r,n):(Oi=n,r);
                    r=n
                }
            }
            return Oi=null,r
        }
        function xn(e,t,r,n){
            if(!n)return t+r;
            do t+=r;
            while(t>0&&Mi.test(e.text.charAt(t)));
            return t
        }
        function wn(e,t,r,n){
            var i=vr(e);
            if(!i)return Cn(e,t,r,n);
            for(var o=bn(i,t),l=i[o],s=xn(e,t,l.level%2?-r:r,n);
            ;
            ){
                if(s>l.from&&s<l.to)return s;
                if(s==l.from||s==l.to)return bn(i,s)==o?s:(l=i[o+=r],r>0==l.level%2?l.to:l.from);
                if(l=i[o+=r],!l)return null;
                s=r>0==l.level%2?xn(e,l.to,-1,n):xn(e,l.from,1,n)
            }
        }
        function Cn(e,t,r,n){
            var i=t+r;
            if(n)for(;
            i>0&&Mi.test(e.text.charAt(i));
            )i+=r;
            return 0>i||i>e.text.length?null:i
        }
        var Ln=/gecko\/\d/i.test(navigator.userAgent),kn=/MSIE \d/.test(navigator.userAgent),Sn=kn&&(null==document.documentMode||document.documentMode<8),Mn=kn&&(null==document.documentMode||document.documentMode<9),Tn=/WebKit\//.test(navigator.userAgent),An=Tn&&/Qt\/\d+\.\d+/.test(navigator.userAgent),Nn=/Chrome\//.test(navigator.userAgent),Hn=/Opera\//.test(navigator.userAgent),Wn=/Apple Computer/.test(navigator.vendor),En=/KHTML\//.test(navigator.userAgent),Dn=/Mac OS X 1\d\D([7-9]|\d\d)\D/.test(navigator.userAgent),On=/Mac OS X 1\d\D([8-9]|\d\d)\D/.test(navigator.userAgent),In=/PhantomJS/.test(navigator.userAgent),zn=/AppleWebKit/.test(navigator.userAgent)&&/Mobile\/\w+/.test(navigator.userAgent),Pn=zn||/Android|webOS|BlackBerry|Opera Mini|Opera Mobi|IEMobile/i.test(navigator.userAgent),Fn=zn||/Mac/.test(navigator.platform),Rn=/windows/i.test(navigator.platform),Vn=Hn&&navigator.userAgent.match(/Version\/(\d*\.\d*)/);
        Vn&&(Vn=Number(Vn[1])),Vn&&Vn>=15&&(Hn=!1,Tn=!0);
        var Gn,Bn,jn,Un=Fn&&(An||Hn&&(null==Vn||12.11>Vn)),Kn=Ln||kn&&!Mn,_n=!1,Xn=!1,Yn=0,$n=0,qn=0,Zn=null;
        kn?Zn=-.53:Ln?Zn=15:Nn?Zn=-.7:Wn&&(Zn=-1/3);
        var Jn,Qn,ei=null,ti=e.changeEnd=function(e){
            return e.text?Ue(e.from.line+e.text.length-1,Xr(e.text).length+(1==e.text.length?e.from.ch:0)):e.to
        }
        ;
        e.Pos=Ue,e.prototype={
            constructor:e,focus:function(){
                window.focus(),de(this),We(this),ce(this)
            }
            ,setOption:function(e,t){
                var r=this.options,n=r[e];
                (r[e]!=t||"mode"==e)&&(r[e]=t,ri.hasOwnProperty(e)&&oe(this,ri[e])(this,t,n))
            }
            ,getOption:function(e){
                return this.options[e]
            }
            ,getDoc:function(){
                return this.doc
            }
            ,addKeyMap:function(e,t){
                this.state.keyMaps[t?"push":"unshift"](e)
            }
            ,removeKeyMap:function(e){
                for(var t=this.state.keyMaps,r=0;
                r<t.length;
                ++r)if(t[r]==e||"string"!=typeof t[r]&&t[r].name==e)return t.splice(r,1),!0
            }
            ,addOverlay:oe(null,function(t,r){
                var n=t.token?t:e.getMode(this.options,t);
                if(n.startState)throw new Error("Overlays may not be stateful.");
                this.state.overlays.push({
                    mode:n,modeSpec:t,opaque:r&&r.opaque
                }),this.state.modeGen++,ae(this)
            }),removeOverlay:oe(null,function(e){
                for(var t=this.state.overlays,r=0;
                r<t.length;
                ++r){
                    var n=t[r].modeSpec;
                    if(n==e||"string"==typeof e&&n.name==e)return t.splice(r,1),this.state.modeGen++,void ae(this)
                }
            }),indentLine:oe(null,function(e,t,r){
                "string"!=typeof t&&"number"!=typeof t&&(t=null==t?this.options.smartIndent?"smart":"prev":t?"add":"subtract"),Ze(this.doc,e)&&ut(this,e,t,r)
            }),indentSelection:oe(null,function(e){
                var t=this.doc.sel;
                if(Ke(t.from,t.to))return ut(this,t.from.line,e);
                for(var r=t.to.line-(t.to.ch?0:1),n=t.from.line;
                r>=n;
                ++n)ut(this,n,e)
            }),getTokenAt:function(e,t){
                var r=this.doc;
                e=$e(r,e);
                for(var n=I(this,e.line,t),i=this.doc.mode,o=cr(r,e.line),l=new Ct(o.text,this.options.tabSize);
                l.pos<e.ch&&!l.eol();
                ){
                    l.start=l.pos;
                    var s=i.token(l,n)
                }
                return{
                    start:l.start,end:l.pos,string:l.current(),className:s||null,type:s||null,state:n
                }
            }
            ,getTokenTypeAt:function(e){
                e=$e(this.doc,e);
                var t=qt(this,cr(this.doc,e.line)),r=0,n=(t.length-1)/2,i=e.ch;
                if(0==i)return t[2];
                for(;
                ;
                ){
                    var o=r+n>>1;
                    if((o?t[2*o-1]:0)>=i)n=o;
                    else{
                        if(!(t[2*o+1]<i))return t[2*o+2];
                        r=o+1
                    }
                }
            }
            ,getModeAt:function(t){
                var r=this.doc.mode;
                return r.innerMode?e.innerMode(r,this.getTokenAt(t).state).mode:r
            }
            ,getHelper:function(e,t){
                if(ui.hasOwnProperty(t)){
                    var r=ui[t],n=this.getModeAt(e);
                    return n[t]&&r[n[t]]||n.helperType&&r[n.helperType]||r[n.name]
                }
            }
            ,getStateAfter:function(e,t){
                var r=this.doc;
                return e=Ye(r,null==e?r.first+r.size-1:e),I(this,e+1,t)
            }
            ,cursorCoords:function(e,t){
                var r,n=this.doc.sel;
                return r=null==e?n.head:"object"==typeof e?$e(this.doc,e):e?n.from:n.to,Z(this,r,t||"page")
            }
            ,charCoords:function(e,t){
                return q(this,$e(this.doc,e),t||"page")
            }
            ,coordsChar:function(e,t){
                return e=$(this,e,t||"page"),Q(this,e.left,e.top)
            }
            ,lineAtHeight:function(e,t){
                return e=$(this,{
                    top:e,left:0
                }
                ,t||"page").top,mr(this.doc,e+this.display.viewOffset)
            }
            ,heightAtLine:function(e,t){
                var r=!1,n=this.doc.first+this.doc.size-1;
                e<this.doc.first?e=this.doc.first:e>n&&(e=n,r=!0);
                var i=cr(this.doc,e);
                return Y(this,cr(this.doc,e),{
                    top:0,left:0
                }
                ,t||"page").top+(r?i.height:0)
            }
            ,defaultTextHeight:function(){
                return te(this.display)
            }
            ,defaultCharWidth:function(){
                return re(this.display)
            }
            ,setGutterMarker:oe(null,function(e,t,r){
                return ct(this,e,function(e){
                    var n=e.gutterMarkers||(e.gutterMarkers={
                        
                    });
                    return n[t]=r,!r&&tn(n)&&(e.gutterMarkers=null),!0
                })
            }),clearGutter:oe(null,function(e){
                var t=this,r=t.doc,n=r.first;
                r.iter(function(r){
                    r.gutterMarkers&&r.gutterMarkers[e]&&(r.gutterMarkers[e]=null,ae(t,n,n+1),tn(r.gutterMarkers)&&(r.gutterMarkers=null)),++n
                })
            }),addLineClass:oe(null,function(e,t,r){
                return ct(this,e,function(e){
                    var n="text"==t?"textClass":"background"==t?"bgClass":"wrapClass";
                    if(e[n]){
                        if(new RegExp("(?:^|\\s)"+r+"(?:$|\\s)").test(e[n]))return!1;
                        e[n]+=" "+r
                    }
                    else e[n]=r;
                    return!0
                })
            }),removeLineClass:oe(null,function(e,t,r){
                return ct(this,e,function(e){
                    var n="text"==t?"textClass":"background"==t?"bgClass":"wrapClass",i=e[n];
                    if(!i)return!1;
                    if(null==r)e[n]=null;
                    else{
                        var o=i.match(new RegExp("(?:^|\\s+)"+r+"(?:$|\\s+)"));
                        if(!o)return!1;
                        var l=o.index+o[0].length;
                        e[n]=i.slice(0,o.index)+(o.index&&l!=i.length?" ":"")+i.slice(l)||null
                    }
                    return!0
                })
            }),addLineWidget:oe(null,function(e,t,r){
                return Kt(this,e,t,r)
            }),removeLineWidget:function(e){
                e.clear()
            }
            ,lineInfo:function(e){
                if("number"==typeof e){
                    if(!Ze(this.doc,e))return null;
                    var t=e;
                    if(e=cr(this.doc,e),!e)return null
                }
                else{
                    var t=pr(e);
                    if(null==t)return null
                }
                return{
                    line:t,handle:e,text:e.text,gutterMarkers:e.gutterMarkers,textClass:e.textClass,bgClass:e.bgClass,wrapClass:e.wrapClass,widgets:e.widgets
                }
            }
            ,getViewport:function(){
                return{
                    from:this.display.showingFrom,to:this.display.showingTo
                }
            }
            ,addWidget:function(e,t,r,n,i){
                var o=this.display;
                e=Z(this,$e(this.doc,e));
                var l=e.bottom,s=e.left;
                if(t.style.position="absolute",o.sizer.appendChild(t),"over"==n)l=e.top;
                else if("above"==n||"near"==n){
                    var a=Math.max(o.wrapper.clientHeight,this.doc.height),u=Math.max(o.sizer.clientWidth,o.lineSpace.clientWidth);
                    ("above"==n||e.bottom+t.offsetHeight>a)&&e.top>t.offsetHeight?l=e.top-t.offsetHeight:e.bottom+t.offsetHeight<=a&&(l=e.bottom),s+t.offsetWidth>u&&(s=u-t.offsetWidth)
                }
                t.style.top=l+"px",t.style.left=t.style.right="","right"==i?(s=o.sizer.clientWidth-t.offsetWidth,t.style.right="0px"):("left"==i?s=0:"middle"==i&&(s=(o.sizer.clientWidth-t.offsetWidth)/2),t.style.left=s+"px"),r&&ot(this,s,l,s+t.offsetWidth,l+t.offsetHeight)
            }
            ,triggerOnKeyDown:oe(null,Ne),execCommand:function(e){
                return ci[e](this)
            }
            ,findPosH:function(e,t,r,n){
                var i=1;
                0>t&&(i=-1,t=-t);
                for(var o=0,l=$e(this.doc,e);
                t>o&&(l=ft(this.doc,l,i,r,n),!l.hitSide);
                ++o);
                return l
            }
            ,moveH:oe(null,function(e,t){
                var r,n=this.doc.sel;
                r=n.shift||n.extend||Ke(n.from,n.to)?ft(this.doc,n.head,e,t,this.options.rtlMoveVisually):0>e?n.from:n.to,Je(this.doc,r,r,e)
            }),deleteH:oe(null,function(e,t){
                var r=this.doc.sel;
                Ke(r.from,r.to)?je(this.doc,"",r.from,ft(this.doc,r.head,e,t,!1),"+delete"):je(this.doc,"",r.from,r.to,"+delete"),this.curOp.userSelChange=!0
            }),findPosV:function(e,t,r,n){
                var i=1,o=n;
                0>t&&(i=-1,t=-t);
                for(var l=0,s=$e(this.doc,e);
                t>l;
                ++l){
                    var a=Z(this,s,"div");
                    if(null==o?o=a.left:a.left=o,s=ht(this,a,i,r),s.hitSide)break
                }
                return s
            }
            ,moveV:oe(null,function(e,t){
                var r=this.doc.sel,n=Z(this,r.head,"div");
                null!=r.goalColumn&&(n.left=r.goalColumn);
                var i=ht(this,n,e,t);
                "page"==t&&at(this,0,q(this,i,"div").top-n.top),Je(this.doc,i,i,e),r.goalColumn=n.left
            }),toggleOverwrite:function(e){
                (null==e||e!=this.state.overwrite)&&((this.state.overwrite=!this.state.overwrite)?this.display.cursor.className+=" CodeMirror-overwrite":this.display.cursor.className=this.display.cursor.className.replace(" CodeMirror-overwrite",""))
            }
            ,hasFocus:function(){
                return this.state.focused
            }
            ,scrollTo:oe(null,function(e,t){
                st(this,e,t)
            }),getScrollInfo:function(){
                var e=this.display.scroller,t=Ci;
                return{
                    left:e.scrollLeft,top:e.scrollTop,height:e.scrollHeight-t,width:e.scrollWidth-t,clientHeight:e.clientHeight-t,clientWidth:e.clientWidth-t
                }
            }
            ,scrollIntoView:oe(null,function(e,t){
                "number"==typeof e&&(e=Ue(e,0)),t||(t=0);
                var r=e;
                e&&null==e.line||(this.curOp.scrollToPos=e?$e(this.doc,e):this.doc.sel.head,this.curOp.scrollToPosMargin=t,r=Z(this,this.curOp.scrollToPos));
                var n=lt(this,r.left,r.top-t,r.right,r.bottom+t);
                st(this,n.scrollLeft,n.scrollTop)
            }),setSize:oe(null,function(e,t){
                function r(e){
                    return"number"==typeof e||/^\d+$/.test(String(e))?e+"px":e
                }
                null!=e&&(this.display.wrapper.style.width=r(e)),null!=t&&(this.display.wrapper.style.height=r(t)),this.options.lineWrapping&&(this.display.measureLineCache.length=this.display.measureLineCachePos=0),this.curOp.forceUpdate=!0
            }),operation:function(e){
                return se(this,e)
            }
            ,refresh:oe(null,function(){
                K(this),st(this,this.doc.scrollLeft,this.doc.scrollTop),ae(this)
            }),swapDoc:oe(null,function(e){
                var t=this.doc;
                return t.cm=null,ur(this,e),K(this),he(this,!0),st(this,e.scrollLeft,e.scrollTop),t
            }),getInputField:function(){
                return this.display.input
            }
            ,getWrapperElement:function(){
                return this.display.wrapper
            }
            ,getScrollerElement:function(){
                return this.display.scroller
            }
            ,getGutterElement:function(){
                return this.display.gutters
            }
        }
        ,jr(e);
        var ri=e.optionHandlers={
            
        }
        ,ni=e.defaults={
            
        }
        ,ii=e.Init={
            toString:function(){
                return"CodeMirror.Init"
            }
        }
        ;
        mt("value","",function(e,t){
            e.setValue(t)
        }
        ,!0),mt("mode",null,function(e,t){
            e.doc.modeOption=t,r(e)
        }
        ,!0),mt("indentUnit",2,r,!0),mt("indentWithTabs",!1),mt("smartIndent",!0),mt("tabSize",4,function(e){
            r(e),K(e),ae(e)
        }
        ,!0),mt("electricChars",!0),mt("rtlMoveVisually",!Rn),mt("theme","default",function(e){
            s(e),a(e)
        }
        ,!0),mt("keyMap","default",l),mt("extraKeys",null),mt("onKeyEvent",null),mt("onDragEvent",null),mt("lineWrapping",!1,n,!0),mt("gutters",[],function(e){
            h(e.options),a(e)
        }
        ,!0),mt("fixedGutter",!0,function(e,t){
            e.display.gutters.style.left=t?y(e.display)+"px":"0",e.refresh()
        }
        ,!0),mt("coverGutterNextToScrollbar",!1,d,!0),mt("lineNumbers",!1,function(e){
            h(e.options),a(e)
        }
        ,!0),mt("firstLineNumber",1,a,!0),mt("lineNumberFormatter",function(e){
            return e
        }
        ,a,!0),mt("showCursorWhenSelecting",!1,A,!0),mt("readOnly",!1,function(e,t){
            "nocursor"==t?(Ee(e),e.display.input.blur()):t||he(e,!0)
        }),mt("dragDrop",!0),mt("cursorBlinkRate",530),mt("cursorScrollMargin",0),mt("cursorHeight",1),mt("workTime",100),mt("workDelay",100),mt("flattenSpans",!0),mt("pollInterval",100),mt("undoDepth",40,function(e,t){
            e.doc.history.undoDepth=t
        }),mt("historyEventDelay",500),mt("viewportMargin",10,function(e){
            e.refresh()
        }
        ,!0),mt("maxHighlightLength",1e4,function(e){
            r(e),e.refresh()
        }
        ,!0),mt("moveInputWithCursor",!0,function(e,t){
            t||(e.display.inputDiv.style.top=e.display.inputDiv.style.left=0)
        }),mt("tabindex",null,function(e,t){
            e.display.input.tabIndex=t||""
        }),mt("autofocus",null);
        var oi=e.modes={
            
        }
        ,li=e.mimeModes={
            
        }
        ;
        e.defineMode=function(t,r){
            if(e.defaults.mode||"null"==t||(e.defaults.mode=t),arguments.length>2){
                r.dependencies=[];
                for(var n=2;
                n<arguments.length;
                ++n)r.dependencies.push(arguments[n])
            }
            oi[t]=r
        }
        ,e.defineMIME=function(e,t){
            li[e]=t
        }
        ,e.resolveMode=function(t){
            if("string"==typeof t&&li.hasOwnProperty(t))t=li[t];
            else if(t&&"string"==typeof t.name&&li.hasOwnProperty(t.name)){
                var r=li[t.name];
                t=qr(r,t),t.name=r.name
            }
            else if("string"==typeof t&&/^[\w\-]+\/[\w\-]+\+xml$/.test(t))return e.resolveMode("application/xml");
            return"string"==typeof t?{
                name:t
            }
            :t||{
                name:"null"
            }
        }
        ,e.getMode=function(t,r){
            var r=e.resolveMode(r),n=oi[r.name];
            if(!n)return e.getMode(t,"text/plain");
            var i=n(t,r);
            if(si.hasOwnProperty(r.name)){
                var o=si[r.name];
                for(var l in o)o.hasOwnProperty(l)&&(i.hasOwnProperty(l)&&(i["_"+l]=i[l]),i[l]=o[l])
            }
            return i.name=r.name,i
        }
        ,e.defineMode("null",function(){
            return{
                token:function(e){
                    e.skipToEnd()
                }
            }
        }),e.defineMIME("text/plain","null");
        var si=e.modeExtensions={
            
        }
        ;
        e.extendMode=function(e,t){
            var r=si.hasOwnProperty(e)?si[e]:si[e]={
                
            }
            ;
            Zr(t,r)
        }
        ,e.defineExtension=function(t,r){
            e.prototype[t]=r
        }
        ,e.defineDocExtension=function(e,t){
            vi.prototype[e]=t
        }
        ,e.defineOption=mt;
        var ai=[];
        e.defineInitHook=function(e){
            ai.push(e)
        }
        ;
        var ui=e.helpers={
            
        }
        ;
        e.registerHelper=function(t,r,n){
            ui.hasOwnProperty(t)||(ui[t]=e[t]={
                
            }),ui[t][r]=n
        }
        ,e.isWordChar=en,e.copyState=gt,e.startState=vt,e.innerMode=function(e,t){
            for(;
            e.innerMode;
            ){
                var r=e.innerMode(t);
                if(!r||r.mode==e)break;
                t=r.state,e=r.mode
            }
            return r||{
                mode:e,state:t
            }
        }
        ;
        var ci=e.commands={
            selectAll:function(e){
                e.setSelection(Ue(e.firstLine(),0),Ue(e.lastLine()))
            }
            ,killLine:function(e){
                var t=e.getCursor(!0),r=e.getCursor(!1),n=!Ke(t,r);
                n||e.getLine(t.line).length!=t.ch?e.replaceRange("",t,n?r:Ue(t.line),"+delete"):e.replaceRange("",t,Ue(t.line+1,0),"+delete")
            }
            ,deleteLine:function(e){
                var t=e.getCursor().line;
                e.replaceRange("",Ue(t,0),Ue(t),"+delete")
            }
            ,delLineLeft:function(e){
                var t=e.getCursor();
                e.replaceRange("",Ue(t.line,0),t,"+delete")
            }
            ,undo:function(e){
                e.undo()
            }
            ,redo:function(e){
                e.redo()
            }
            ,goDocStart:function(e){
                e.extendSelection(Ue(e.firstLine(),0))
            }
            ,goDocEnd:function(e){
                e.extendSelection(Ue(e.lastLine()))
            }
            ,goLineStart:function(e){
                e.extendSelection(gn(e,e.getCursor().line))
            }
            ,goLineStartSmart:function(e){
                var t=e.getCursor(),r=gn(e,t.line),n=e.getLineHandle(r.line),i=vr(n);
                if(i&&0!=i[0].level)e.extendSelection(r);
                else{
                    var o=Math.max(0,n.text.search(/\S/)),l=t.line==r.line&&t.ch<=o&&t.ch;
                    e.extendSelection(Ue(r.line,l?0:o))
                }
            }
            ,goLineEnd:function(e){
                e.extendSelection(vn(e,e.getCursor().line))
            }
            ,goLineRight:function(e){
                var t=e.charCoords(e.getCursor(),"div").top+5;
                e.extendSelection(e.coordsChar({
                    left:e.display.lineDiv.offsetWidth+100,top:t
                }
                ,"div"))
            }
            ,goLineLeft:function(e){
                var t=e.charCoords(e.getCursor(),"div").top+5;
                e.extendSelection(e.coordsChar({
                    left:0,top:t
                }
                ,"div"))
            }
            ,goLineUp:function(e){
                e.moveV(-1,"line")
            }
            ,goLineDown:function(e){
                e.moveV(1,"line")
            }
            ,goPageUp:function(e){
                e.moveV(-1,"page")
            }
            ,goPageDown:function(e){
                e.moveV(1,"page")
            }
            ,goCharLeft:function(e){
                e.moveH(-1,"char")
            }
            ,goCharRight:function(e){
                e.moveH(1,"char")
            }
            ,goColumnLeft:function(e){
                e.moveH(-1,"column")
            }
            ,goColumnRight:function(e){
                e.moveH(1,"column")
            }
            ,goWordLeft:function(e){
                e.moveH(-1,"word")
            }
            ,goGroupRight:function(e){
                e.moveH(1,"group")
            }
            ,goGroupLeft:function(e){
                e.moveH(-1,"group")
            }
            ,goWordRight:function(e){
                e.moveH(1,"word")
            }
            ,delCharBefore:function(e){
                e.deleteH(-1,"char")
            }
            ,delCharAfter:function(e){
                e.deleteH(1,"char")
            }
            ,delWordBefore:function(e){
                e.deleteH(-1,"word")
            }
            ,delWordAfter:function(e){
                e.deleteH(1,"word")
            }
            ,delGroupBefore:function(e){
                e.deleteH(-1,"group")
            }
            ,delGroupAfter:function(e){
                e.deleteH(1,"group")
            }
            ,indentAuto:function(e){
                e.indentSelection("smart")
            }
            ,indentMore:function(e){
                e.indentSelection("add")
            }
            ,indentLess:function(e){
                e.indentSelection("subtract")
            }
            ,insertTab:function(e){
                e.replaceSelection("	","end","+input")
            }
            ,defaultTab:function(e){
                e.somethingSelected()?e.indentSelection("add"):e.replaceSelection("	","end","+input")
            }
            ,transposeChars:function(e){
                var t=e.getCursor(),r=e.getLine(t.line);
                t.ch>0&&t.ch<r.length-1&&e.replaceRange(r.charAt(t.ch)+r.charAt(t.ch-1),Ue(t.line,t.ch-1),Ue(t.line,t.ch+1))
            }
            ,newlineAndIndent:function(e){
                oe(e,function(){
                    e.replaceSelection("\n","end","+input"),e.indentLine(e.getCursor().line,null,!0)
                })()
            }
            ,toggleOverwrite:function(e){
                e.toggleOverwrite()
            }
        }
        ,fi=e.keyMap={
            
        }
        ;
        fi.basic={
            Left:"goCharLeft",Right:"goCharRight",Up:"goLineUp",Down:"goLineDown",End:"goLineEnd",Home:"goLineStartSmart",PageUp:"goPageUp",PageDown:"goPageDown",Delete:"delCharAfter",Backspace:"delCharBefore",Tab:"defaultTab","Shift-Tab":"indentAuto",Enter:"newlineAndIndent",Insert:"toggleOverwrite"
        }
        ,fi.pcDefault={
            "Ctrl-A":"selectAll","Ctrl-D":"deleteLine","Ctrl-Z":"undo","Shift-Ctrl-Z":"redo","Ctrl-Y":"redo","Ctrl-Home":"goDocStart","Alt-Up":"goDocStart","Ctrl-End":"goDocEnd","Ctrl-Down":"goDocEnd","Ctrl-Left":"goGroupLeft","Ctrl-Right":"goGroupRight","Alt-Left":"goLineStart","Alt-Right":"goLineEnd","Ctrl-Backspace":"delGroupBefore","Ctrl-Delete":"delGroupAfter","Ctrl-S":"save","Ctrl-F":"find","Ctrl-G":"findNext","Shift-Ctrl-G":"findPrev","Shift-Ctrl-F":"replace","Shift-Ctrl-R":"replaceAll","Ctrl-[":"indentLess","Ctrl-]":"indentMore",fallthrough:"basic"
        }
        ,fi.macDefault={
            "Cmd-A":"selectAll","Cmd-D":"deleteLine","Cmd-Z":"undo","Shift-Cmd-Z":"redo","Cmd-Y":"redo","Cmd-Up":"goDocStart","Cmd-End":"goDocEnd","Cmd-Down":"goDocEnd","Alt-Left":"goGroupLeft","Alt-Right":"goGroupRight","Cmd-Left":"goLineStart","Cmd-Right":"goLineEnd","Alt-Backspace":"delGroupBefore","Ctrl-Alt-Backspace":"delGroupAfter","Alt-Delete":"delGroupAfter","Cmd-S":"save","Cmd-F":"find","Cmd-G":"findNext","Shift-Cmd-G":"findPrev","Cmd-Alt-F":"replace","Shift-Cmd-Alt-F":"replaceAll","Cmd-[":"indentLess","Cmd-]":"indentMore","Cmd-Backspace":"delLineLeft",fallthrough:["basic","emacsy"]
        }
        ,fi["default"]=Fn?fi.macDefault:fi.pcDefault,fi.emacsy={
            "Ctrl-F":"goCharRight","Ctrl-B":"goCharLeft","Ctrl-P":"goLineUp","Ctrl-N":"goLineDown","Alt-F":"goWordRight","Alt-B":"goWordLeft","Ctrl-A":"goLineStart","Ctrl-E":"goLineEnd","Ctrl-V":"goPageDown","Shift-Ctrl-V":"goPageUp","Ctrl-D":"delCharAfter","Ctrl-H":"delCharBefore","Alt-D":"delWordAfter","Alt-Backspace":"delWordBefore","Ctrl-K":"killLine","Ctrl-T":"transposeChars"
        }
        ,e.lookupKey=bt,e.isModifierKey=xt,e.keyName=wt,e.fromTextArea=function(t,r){
            function n(){
                t.value=u.getValue()
            }
            if(r||(r={
                
            }),r.value=t.value,!r.tabindex&&t.tabindex&&(r.tabindex=t.tabindex),!r.placeholder&&t.placeholder&&(r.placeholder=t.placeholder),null==r.autofocus){
                var i=document.body;
                try{
                    i=document.activeElement
                }
                catch(o){
                    
                }
                r.autofocus=i==t||null!=t.getAttribute("autofocus")&&i==document.body
            }
            if(t.form&&(zr(t.form,"submit",n),!r.leaveSubmitMethodAlone)){
                var l=t.form,s=l.submit;
                try{
                    var a=l.submit=function(){
                        n(),l.submit=s,l.submit(),l.submit=a
                    }
                }
                catch(o){
                    
                }
            }
            t.style.display="none";
            var u=e(function(e){
                t.parentNode.insertBefore(e,t.nextSibling)
            }
            ,r);
            return u.save=n,u.getTextArea=function(){
                return t
            }
            ,u.toTextArea=function(){
                n(),t.parentNode.removeChild(u.getWrapperElement()),t.style.display="",t.form&&(Pr(t.form,"submit",n),"function"==typeof t.form.submit&&(t.form.submit=s))
            }
            ,u
        }
        ,Ct.prototype={
            eol:function(){
                return this.pos>=this.string.length
            }
            ,sol:function(){
                return 0==this.pos
            }
            ,peek:function(){
                return this.string.charAt(this.pos)||void 0
            }
            ,next:function(){
                return this.pos<this.string.length?this.string.charAt(this.pos++):void 0
            }
            ,eat:function(e){
                var t=this.string.charAt(this.pos);
                if("string"==typeof e)var r=t==e;
                else var r=t&&(e.test?e.test(t):e(t));
                return r?(++this.pos,t):void 0
            }
            ,eatWhile:function(e){
                for(var t=this.pos;
                this.eat(e);
                );
                return this.pos>t
            }
            ,eatSpace:function(){
                for(var e=this.pos;
                /[\s\u00a0]/.test(this.string.charAt(this.pos));
                )++this.pos;
                return this.pos>e
            }
            ,skipToEnd:function(){
                this.pos=this.string.length
            }
            ,skipTo:function(e){
                var t=this.string.indexOf(e,this.pos);
                return t>-1?(this.pos=t,!0):void 0
            }
            ,backUp:function(e){
                this.pos-=e
            }
            ,column:function(){
                return this.lastColumnPos<this.start&&(this.lastColumnValue=Kr(this.string,this.start,this.tabSize,this.lastColumnPos,this.lastColumnValue),this.lastColumnPos=this.start),this.lastColumnValue
            }
            ,indentation:function(){
                return Kr(this.string,null,this.tabSize)
            }
            ,match:function(e,t,r){
                if("string"!=typeof e){
                    var n=this.string.slice(this.pos).match(e);
                    return n&&n.index>0?null:(n&&t!==!1&&(this.pos+=n[0].length),n)
                }
                var i=function(e){
                    return r?e.toLowerCase():e
                }
                ,o=this.string.substr(this.pos,e.length);
                return i(o)==i(e)?(t!==!1&&(this.pos+=e.length),!0):void 0
            }
            ,current:function(){
                return this.string.slice(this.start,this.pos)
            }
        }
        ,e.StringStream=Ct,e.TextMarker=Lt,jr(Lt),Lt.prototype.clear=function(){
            if(!this.explicitlyCleared){
                var e=this.doc.cm,t=e&&!e.curOp;
                if(t&&ne(e),Br(this,"clear")){
                    var r=this.find();
                    r&&Rr(this,"clear",r.from,r.to)
                }
                for(var n=null,i=null,o=0;
                o<this.lines.length;
                ++o){
                    var l=this.lines[o],s=Tt(l.markedSpans,this);
                    null!=s.to&&(i=pr(l)),l.markedSpans=At(l.markedSpans,s),null!=s.from?n=pr(l):this.collapsed&&!Rt(this.doc,l)&&e&&dr(l,te(e.display))
                }
                if(e&&this.collapsed&&!e.options.lineWrapping)for(var o=0;
                o<this.lines.length;
                ++o){
                    var a=Ft(e.doc,this.lines[o]),u=c(e.doc,a);
                    u>e.display.maxLineLength&&(e.display.maxLine=a,e.display.maxLineLength=u,e.display.maxLineChanged=!0)
                }
                null!=n&&e&&ae(e,n,i+1),this.lines.length=0,this.explicitlyCleared=!0,this.atomic&&this.doc.cantEdit&&(this.doc.cantEdit=!1,e&&tt(e)),t&&ie(e)
            }
        }
        ,Lt.prototype.find=function(){
            for(var e,t,r=0;
            r<this.lines.length;
            ++r){
                var n=this.lines[r],i=Tt(n.markedSpans,this);
                if(null!=i.from||null!=i.to){
                    var o=pr(n);
                    null!=i.from&&(e=Ue(o,i.from)),null!=i.to&&(t=Ue(o,i.to))
                }
            }
            return"bookmark"==this.type?e:e&&{
                from:e,to:t
            }
        }
        ,Lt.prototype.changed=function(){
            var e=this.find(),t=this.doc.cm;
            if(e&&t){
                var r=cr(this.doc,e.from.line);
                if(G(t,r),e.from.line>=t.display.showingFrom&&e.from.line<t.display.showingTo){
                    for(var n=t.display.lineDiv.firstChild;
                    n;
                    n=n.nextSibling)if(n.lineObj==r){
                        n.offsetHeight!=r.height&&dr(r,n.offsetHeight);
                        break
                    }
                    se(t,function(){
                        t.curOp.selectionChanged=t.curOp.forceUpdate=t.curOp.updateMaxLine=!0
                    })
                }
            }
        }
        ,Lt.prototype.attachLine=function(e){
            if(!this.lines.length&&this.doc.cm){
                var t=this.doc.cm.curOp;
                t.maybeHiddenMarkers&&-1!=$r(t.maybeHiddenMarkers,this)||(t.maybeUnhiddenMarkers||(t.maybeUnhiddenMarkers=[])).push(this)
            }
            this.lines.push(e)
        }
        ,Lt.prototype.detachLine=function(e){
            if(this.lines.splice($r(this.lines,e),1),!this.lines.length&&this.doc.cm){
                var t=this.doc.cm.curOp;
                (t.maybeHiddenMarkers||(t.maybeHiddenMarkers=[])).push(this)
            }
        }
        ,e.SharedTextMarker=St,jr(St),St.prototype.clear=function(){
            if(!this.explicitlyCleared){
                this.explicitlyCleared=!0;
                for(var e=0;
                e<this.markers.length;
                ++e)this.markers[e].clear();
                Rr(this,"clear")
            }
        }
        ,St.prototype.find=function(){
            return this.primary.find()
        }
        ;
        var hi=e.LineWidget=function(e,t,r){
            if(r)for(var n in r)r.hasOwnProperty(n)&&(this[n]=r[n]);
            this.cm=e,this.node=t
        }
        ;
        jr(hi),hi.prototype.clear=jt(function(){
            var e=this.line.widgets,t=pr(this.line);
            if(null!=t&&e){
                for(var r=0;
                r<e.length;
                ++r)e[r]==this&&e.splice(r--,1);
                e.length||(this.line.widgets=null);
                var n=gr(this.cm,this.line)<this.cm.doc.scrollTop;
                dr(this.line,Math.max(0,this.line.height-Ut(this))),n&&at(this.cm,0,-this.height),ae(this.cm,t,t+1)
            }
        }),hi.prototype.changed=jt(function(){
            var e=this.height;
            this.height=null;
            var t=Ut(this)-e;
            if(t){
                dr(this.line,this.line.height+t);
                var r=pr(this.line);
                ae(this.cm,r,r+1)
            }
        });
        var di=e.Line=function(e,t,r){
            this.text=e,Bt(this,t),this.height=r?r(this):1
        }
        ;
        jr(di);
        var pi={
            
        }
        ,mi=/[\t\u0000-\u0019\u00ad\u200b\u2028\u2029\uFEFF]/g;
        lr.prototype={
            chunkSize:function(){
                return this.lines.length
            }
            ,removeInner:function(e,t){
                for(var r=e,n=e+t;
                n>r;
                ++r){
                    var i=this.lines[r];
                    this.height-=i.height,Xt(i),Rr(i,"delete")
                }
                this.lines.splice(e,t)
            }
            ,collapse:function(e){
                e.splice.apply(e,[e.length,0].concat(this.lines))
            }
            ,insertInner:function(e,t,r){
                this.height+=r,this.lines=this.lines.slice(0,e).concat(t).concat(this.lines.slice(e));
                for(var n=0,i=t.length;
                i>n;
                ++n)t[n].parent=this
            }
            ,iterN:function(e,t,r){
                for(var n=e+t;
                n>e;
                ++e)if(r(this.lines[e]))return!0
            }
        }
        ,sr.prototype={
            chunkSize:function(){
                return this.size
            }
            ,removeInner:function(e,t){
                this.size-=t;
                for(var r=0;
                r<this.children.length;
                ++r){
                    var n=this.children[r],i=n.chunkSize();
                    if(i>e){
                        var o=Math.min(t,i-e),l=n.height;
                        if(n.removeInner(e,o),this.height-=l-n.height,i==o&&(this.children.splice(r--,1),n.parent=null),0==(t-=o))break;
                        e=0
                    }
                    else e-=i
                }
                if(this.size-t<25){
                    var s=[];
                    this.collapse(s),this.children=[new lr(s)],this.children[0].parent=this
                }
            }
            ,collapse:function(e){
                for(var t=0,r=this.children.length;
                r>t;
                ++t)this.children[t].collapse(e)
            }
            ,insertInner:function(e,t,r){
                this.size+=t.length,this.height+=r;
                for(var n=0,i=this.children.length;
                i>n;
                ++n){
                    var o=this.children[n],l=o.chunkSize();
                    if(l>=e){
                        if(o.insertInner(e,t,r),o.lines&&o.lines.length>50){
                            for(;
                            o.lines.length>50;
                            ){
                                var s=o.lines.splice(o.lines.length-25,25),a=new lr(s);
                                o.height-=a.height,this.children.splice(n+1,0,a),a.parent=this
                            }
                            this.maybeSpill()
                        }
                        break
                    }
                    e-=l
                }
            }
            ,maybeSpill:function(){
                if(!(this.children.length<=10)){
                    var e=this;
                    do{
                        var t=e.children.splice(e.children.length-5,5),r=new sr(t);
                        if(e.parent){
                            e.size-=r.size,e.height-=r.height;
                            var n=$r(e.parent.children,e);
                            e.parent.children.splice(n+1,0,r)
                        }
                        else{
                            var i=new sr(e.children);
                            i.parent=e,e.children=[i,r],e=i
                        }
                        r.parent=e.parent
                    }
                    while(e.children.length>10);
                    e.parent.maybeSpill()
                }
            }
            ,iterN:function(e,t,r){
                for(var n=0,i=this.children.length;
                i>n;
                ++n){
                    var o=this.children[n],l=o.chunkSize();
                    if(l>e){
                        var s=Math.min(t,l-e);
                        if(o.iterN(e,s,r))return!0;
                        if(0==(t-=s))break;
                        e=0
                    }
                    else e-=l
                }
            }
        }
        ;
        var gi=0,vi=e.Doc=function(e,t,r){
            if(!(this instanceof vi))return new vi(e,t,r);
            null==r&&(r=0),sr.call(this,[new lr([new di("",null)])]),this.first=r,this.scrollTop=this.scrollLeft=0,this.cantEdit=!1,this.history=yr(),this.cleanGeneration=1,this.frontier=r;
            var n=Ue(r,0);
            this.sel={
                from:n,to:n,head:n,anchor:n,shift:!1,extend:!1,goalColumn:null
            }
            ,this.id=++gi,this.modeOption=t,"string"==typeof e&&(e=Hi(e)),or(this,{
                from:n,to:n,text:e
            }
            ,null,{
                head:n,anchor:n
            })
        }
        ;
        vi.prototype=qr(sr.prototype,{
            constructor:vi,iter:function(e,t,r){
                r?this.iterN(e-this.first,t-e,r):this.iterN(this.first,this.first+this.size,e)
            }
            ,insert:function(e,t){
                for(var r=0,n=0,i=t.length;
                i>n;
                ++n)r+=t[n].height;
                this.insertInner(e-this.first,t,r)
            }
            ,remove:function(e,t){
                this.removeInner(e-this.first,t)
            }
            ,getValue:function(e){
                var t=hr(this,this.first,this.first+this.size);
                return e===!1?t:t.join(e||"\n")
            }
            ,setValue:function(e){
                var t=Ue(this.first,0),r=this.first+this.size-1;
                Pe(this,{
                    from:t,to:Ue(r,cr(this,r).text.length),text:Hi(e),origin:"setValue"
                }
                ,{
                    head:t,anchor:t
                }
                ,!0)
            }
            ,replaceRange:function(e,t,r,n){
                t=$e(this,t),r=r?$e(this,r):t,je(this,e,t,r,n)
            }
            ,getRange:function(e,t,r){
                var n=fr(this,$e(this,e),$e(this,t));
                return r===!1?n:n.join(r||"\n")
            }
            ,getLine:function(e){
                var t=this.getLineHandle(e);
                return t&&t.text
            }
            ,setLine:function(e,t){
                Ze(this,e)&&je(this,t,Ue(e,0),$e(this,Ue(e)))
            }
            ,removeLine:function(e){
                e?je(this,"",$e(this,Ue(e-1)),$e(this,Ue(e))):je(this,"",Ue(0,0),$e(this,Ue(1,0)))
            }
            ,getLineHandle:function(e){
                return Ze(this,e)?cr(this,e):void 0
            }
            ,getLineNumber:function(e){
                return pr(e)
            }
            ,getLineHandleVisualStart:function(e){
                return"number"==typeof e&&(e=cr(this,e)),Ft(this,e)
            }
            ,lineCount:function(){
                return this.size
            }
            ,firstLine:function(){
                return this.first
            }
            ,lastLine:function(){
                return this.first+this.size-1
            }
            ,clipPos:function(e){
                return $e(this,e)
            }
            ,getCursor:function(e){
                var t,r=this.sel;
                return t=null==e||"head"==e?r.head:"anchor"==e?r.anchor:"end"==e||e===!1?r.to:r.from,Xe(t)
            }
            ,somethingSelected:function(){
                return!Ke(this.sel.head,this.sel.anchor)
            }
            ,setCursor:le(function(e,t,r){
                var n=$e(this,"number"==typeof e?Ue(e,t||0):e);
                r?Je(this,n):et(this,n,n)
            }),setSelection:le(function(e,t){
                et(this,$e(this,e),$e(this,t||e))
            }),extendSelection:le(function(e,t){
                Je(this,$e(this,e),t&&$e(this,t))
            }),getSelection:function(e){
                return this.getRange(this.sel.from,this.sel.to,e)
            }
            ,replaceSelection:function(e,t,r){
                Pe(this,{
                    from:this.sel.from,to:this.sel.to,text:Hi(e),origin:r
                }
                ,t||"around")
            }
            ,undo:le(function(){
                Re(this,"undo")
            }),redo:le(function(){
                Re(this,"redo")
            }),setExtending:function(e){
                this.sel.extend=e
            }
            ,historySize:function(){
                var e=this.history;
                return{
                    undo:e.done.length,redo:e.undone.length
                }
            }
            ,clearHistory:function(){
                this.history=yr(this.history.maxGeneration)
            }
            ,markClean:function(){
                this.cleanGeneration=this.changeGeneration()
            }
            ,changeGeneration:function(){
                return this.history.lastOp=this.history.lastOrigin=null,this.history.generation
            }
            ,isClean:function(e){
                return this.history.generation==(e||this.cleanGeneration)
            }
            ,getHistory:function(){
                return{
                    done:kr(this.history.done),undone:kr(this.history.undone)
                }
            }
            ,setHistory:function(e){
                var t=this.history=yr(this.history.maxGeneration);
                t.done=e.done.slice(0),t.undone=e.undone.slice(0)
            }
            ,markText:function(e,t,r){
                return kt(this,$e(this,e),$e(this,t),r,"range")
            }
            ,setBookmark:function(e,t){
                var r={
                    replacedWith:t&&(null==t.nodeType?t.widget:t),insertLeft:t&&t.insertLeft
                }
                ;
                return e=$e(this,e),kt(this,e,e,r,"bookmark")
            }
            ,findMarksAt:function(e){
                e=$e(this,e);
                var t=[],r=cr(this,e.line).markedSpans;
                if(r)for(var n=0;
                n<r.length;
                ++n){
                    var i=r[n];
                    (null==i.from||i.from<=e.ch)&&(null==i.to||i.to>=e.ch)&&t.push(i.marker.parent||i.marker)
                }
                return t
            }
            ,getAllMarks:function(){
                var e=[];
                return this.iter(function(t){
                    var r=t.markedSpans;
                    if(r)for(var n=0;
                    n<r.length;
                    ++n)null!=r[n].from&&e.push(r[n].marker)
                }),e
            }
            ,posFromIndex:function(e){
                var t,r=this.first;
                return this.iter(function(n){
                    var i=n.text.length+1;
                    return i>e?(t=e,!0):(e-=i,void++r)
                }),$e(this,Ue(r,t))
            }
            ,indexFromPos:function(e){
                e=$e(this,e);
                var t=e.ch;
                return e.line<this.first||e.ch<0?0:(this.iter(this.first,e.line,function(e){
                    t+=e.text.length+1
                }),t)
            }
            ,copy:function(e){
                var t=new vi(hr(this,this.first,this.first+this.size),this.modeOption,this.first);
                return t.scrollTop=this.scrollTop,t.scrollLeft=this.scrollLeft,t.sel={
                    from:this.sel.from,to:this.sel.to,head:this.sel.head,anchor:this.sel.anchor,shift:this.sel.shift,extend:!1,goalColumn:this.sel.goalColumn
                }
                ,e&&(t.history.undoDepth=this.history.undoDepth,t.setHistory(this.getHistory())),t
            }
            ,linkedDoc:function(e){
                e||(e={
                    
                });
                var t=this.first,r=this.first+this.size;
                null!=e.from&&e.from>t&&(t=e.from),null!=e.to&&e.to<r&&(r=e.to);
                var n=new vi(hr(this,t,r),e.mode||this.modeOption,t);
                return e.sharedHist&&(n.history=this.history),(this.linked||(this.linked=[])).push({
                    doc:n,sharedHist:e.sharedHist
                }),n.linked=[{
                    doc:this,isParent:!0,sharedHist:e.sharedHist
                }
                ],n
            }
            ,unlinkDoc:function(t){
                if(t instanceof e&&(t=t.doc),this.linked)for(var r=0;
                r<this.linked.length;
                ++r){
                    var n=this.linked[r];
                    if(n.doc==t){
                        this.linked.splice(r,1),t.unlinkDoc(this);
                        break
                    }
                }
                if(t.history==this.history){
                    var i=[t.id];
                    ar(t,function(e){
                        i.push(e.id)
                    }
                    ,!0),t.history=yr(),t.history.done=kr(this.history.done,i),t.history.undone=kr(this.history.undone,i)
                }
            }
            ,iterLinkedDocs:function(e){
                ar(this,e)
            }
            ,getMode:function(){
                return this.mode
            }
            ,getEditor:function(){
                return this.cm
            }
        }),vi.prototype.eachLine=vi.prototype.iter;
        var yi="iter insert remove copy getEditor".split(" ");
        for(var bi in vi.prototype)vi.prototype.hasOwnProperty(bi)&&$r(yi,bi)<0&&(e.prototype[bi]=function(e){
            return function(){
                return e.apply(this.doc,arguments)
            }
        }
        (vi.prototype[bi]));
        jr(vi),e.e_stop=Dr,e.e_preventDefault=Hr,e.e_stopPropagation=Wr;
        var xi,wi=0;
        e.on=zr,e.off=Pr,e.signal=Fr;
        var Ci=30,Li=e.Pass={
            toString:function(){
                return"CodeMirror.Pass"
            }
        }
        ;
        Ur.prototype={
            set:function(e,t){
                clearTimeout(this.id),this.id=setTimeout(t,e)
            }
        }
        ,e.countColumn=Kr;
        var ki=[""],Si=/[\u3040-\u309f\u30a0-\u30ff\u3400-\u4db5\u4e00-\u9fcc\uac00-\ud7af]/,Mi=/[\u0300-\u036F\u0483-\u0487\u0488-\u0489\u0591-\u05BD\u05BF\u05C1-\u05C2\u05C4-\u05C5\u05C7\u0610-\u061A\u064B-\u065F\u0670\u06D6-\u06DC\u06DF-\u06E4\u06E7-\u06E8\u06EA-\u06ED\uA66F\uA670-\uA672\uA674-\uA67D\uA69F\udc00-\udfff]/;
        e.replaceGetRect=function(e){
            sn=e
        }
        ;
        var Ti=function(){
            if(Mn)return!1;
            var e=rn("div");
            return"draggable"in e||"dragDrop"in e
        }
        ();
        Ln?an=function(e,t){
            return 36==e.charCodeAt(t-1)&&39==e.charCodeAt(t)
        }
        :Wn&&!/Version\/([6-9]|\d\d)\b/.test(navigator.userAgent)?an=function(e,t){
            return/\-[^ \-?]|\?[^ !\'\"\),.\-\/:;
            \?\]\
        }
        ]/.test(e.slice(t-1,t+1))
    }
    :Tn&&!/Chrome\/(?:29|[3-9]\d|\d\d\d)\./.test(navigator.userAgent)&&(an=function(e,t){
        if(t>1&&45==e.charCodeAt(t-1)){
            if(/\w/.test(e.charAt(t-2))&&/[^\-?\.]/.test(e.charAt(t)))return!0;
            if(t>2&&/[\d\.,]/.test(e.charAt(t-2))&&/[\d\.,]/.test(e.charAt(t)))return!1
        }
        return/[~!#%&*)=+
    }
    \]|\"\.>,:;
    ][({
        [<]|-[^\-?\.\u2010-\u201f\u2026]|\?[\w~`@#$%\^&*(_=+{
            [|><]|\xe2\x80\xa6[\w~`@#$%\^&*(_=+{
                [><]/.test(e.slice(t-1,t+1))
            });
            var Ai,Ni,Hi=3!="\n\nb".split(/\n/).length?function(e){
                for(var t=0,r=[],n=e.length;
                n>=t;
                ){
                    var i=e.indexOf("\n",t);
                    -1==i&&(i=e.length);
                    var o=e.slice(t,"\r"==e.charAt(i-1)?i-1:i),l=o.indexOf("\r");
                    -1!=l?(r.push(o.slice(0,l)),t+=l+1):(r.push(o),t=i+1)
                }
                return r
            }
            :function(e){
                return e.split(/\r\n?|\n/)
            }
            ;
            e.splitLines=Hi;
            var Wi=window.getSelection?function(e){
                try{
                    return e.selectionStart!=e.selectionEnd
                }
                catch(t){
                    return!1
                }
            }
            :function(e){
                try{
                    var t=e.ownerDocument.selection.createRange()
                }
                catch(r){
                    
                }
                return t&&t.parentElement()==e?0!=t.compareEndPoints("StartToEnd",t):!1
            }
            ,Ei=function(){
                var e=rn("div");
                return"oncopy"in e?!0:(e.setAttribute("oncopy","return;
                "),"function"==typeof e.oncopy)
            }
            (),Di={
                3:"Enter",8:"Backspace",9:"Tab",13:"Enter",16:"Shift",17:"Ctrl",18:"Alt",19:"Pause",20:"CapsLock",27:"Esc",32:"Space",33:"PageUp",34:"PageDown",35:"End",36:"Home",37:"Left",38:"Up",39:"Right",40:"Down",44:"PrintScrn",45:"Insert",46:"Delete",59:";
                ",91:"Mod",92:"Mod",93:"Mod",109:"-",107:"=",127:"Delete",186:";
                ",187:"=",188:",",189:"-",190:".",191:"/",192:"`",219:"[",220:"\\",221:"]",222:"'",63276:"PageUp",63277:"PageDown",63275:"End",63273:"Home",63234:"Left",63232:"Up",63235:"Right",63233:"Down",63302:"Insert",63272:"Delete"
            }
            ;
            e.keyNames=Di,function(){
                for(var e=0;
                10>e;
                e++)Di[e+48]=String(e);
                for(var e=65;
                90>=e;
                e++)Di[e]=String.fromCharCode(e);
                for(var e=1;
                12>=e;
                e++)Di[e+111]=Di[e+63235]="F"+e
            }
            ();
            var Oi,Ii=function(){
                function e(e){
                    return 255>=e?t.charAt(e):e>=1424&&1524>=e?"R":e>=1536&&1791>=e?r.charAt(e-1536):e>=1792&&2220>=e?"r":"L"
                }
                var t="bbbbbbbbbtstwsbbbbbbbbbbbbbbssstwNN%%%NNNNNN,N,N1111111111NNNNNNNLLLLLLLLLLLLLLLLLLLLLLLLLLNNNNNNLLLLLLLLLLLLLLLLLLLLLLLLLLNNNNbbbbbbsbbbbbbbbbbbbbbbbbbbbbbbbbb,N%%%%NNNNLNNNNN%%11NLNNN1LNNNNNLLLLLLLLLLLLLLLLLLLLLLLNLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLNLLLLLLLL",r="rrrrrrrrrrrr,rNNmmmmmmrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrmmmmmmmmmmmmmmrrrrrrrnnnnnnnnnn%nnrrrmrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrmmmmmmmmmmmmmmmmmmmNmmmmrrrrrrrrrrrrrrrrrr",n=/[\u0590-\u05f4\u0600-\u06ff\u0700-\u08ac]/,i=/[stwN]/,o=/[LRr]/,l=/[Lb1n]/,s=/[1n]/,a="L";
                return function(t){
                    if(!n.test(t))return!1;
                    for(var r,u=t.length,c=[],f=0;
                    u>f;
                    ++f)c.push(r=e(t.charCodeAt(f)));
                    for(var f=0,h=a;
                    u>f;
                    ++f){
                        var r=c[f];
                        "m"==r?c[f]=h:h=r
                    }
                    for(var f=0,d=a;
                    u>f;
                    ++f){
                        var r=c[f];
                        "1"==r&&"r"==d?c[f]="n":o.test(r)&&(d=r,"r"==r&&(c[f]="R"))
                    }
                    for(var f=1,h=c[0];
                    u-1>f;
                    ++f){
                        var r=c[f];
                        "+"==r&&"1"==h&&"1"==c[f+1]?c[f]="1":","!=r||h!=c[f+1]||"1"!=h&&"n"!=h||(c[f]=h),h=r
                    }
                    for(var f=0;
                    u>f;
                    ++f){
                        var r=c[f];
                        if(","==r)c[f]="N";
                        else if("%"==r){
                            for(var p=f+1;
                            u>p&&"%"==c[p];
                            ++p);
                            for(var m=f&&"!"==c[f-1]||u-1>p&&"1"==c[p]?"1":"N",g=f;
                            p>g;
                            ++g)c[g]=m;
                            f=p-1
                        }
                    }
                    for(var f=0,d=a;
                    u>f;
                    ++f){
                        var r=c[f];
                        "L"==d&&"1"==r?c[f]="L":o.test(r)&&(d=r)
                    }
                    for(var f=0;
                    u>f;
                    ++f)if(i.test(c[f])){
                        for(var p=f+1;
                        u>p&&i.test(c[p]);
                        ++p);
                        for(var v="L"==(f?c[f-1]:a),y="L"==(u-1>p?c[p]:a),m=v||y?"L":"R",g=f;
                        p>g;
                        ++g)c[g]=m;
                        f=p-1
                    }
                    for(var b,x=[],f=0;
                    u>f;
                    )if(l.test(c[f])){
                        var w=f;
                        for(++f;
                        u>f&&l.test(c[f]);
                        ++f);
                        x.push({
                            from:w,to:f,level:0
                        })
                    }
                    else{
                        var C=f,L=x.length;
                        for(++f;
                        u>f&&"L"!=c[f];
                        ++f);
                        for(var g=C;
                        f>g;
                        )if(s.test(c[g])){
                            g>C&&x.splice(L,0,{
                                from:C,to:g,level:1
                            });
                            var k=g;
                            for(++g;
                            f>g&&s.test(c[g]);
                            ++g);
                            x.splice(L,0,{
                                from:k,to:g,level:2
                            }),C=g
                        }
                        else++g;
                        f>C&&x.splice(L,0,{
                            from:C,to:f,level:1
                        })
                    }
                    return 1==x[0].level&&(b=t.match(/^\s+/))&&(x[0].from=b[0].length,x.unshift({
                        from:0,to:b[0].length,level:0
                    })),1==Xr(x).level&&(b=t.match(/\s+$/))&&(Xr(x).to-=b[0].length,x.push({
                        from:u-b[0].length,to:u,level:0
                    })),x[0].level!=Xr(x).level&&x.push({
                        from:u,to:u,level:x[0].level
                    }),x
                }
            }
            ();
            return e.version="3.15.0",e
        }
        (),CodeMirror.defineMode("javascript",function(e,t){
            function r(e,t,r){
                return t.tokenize=r,r(e,t)
            }
            function n(e,t){
                for(var r,n=!1;
                null!=(r=e.next());
                ){
                    if(r==t&&!n)return!1;
                    n=!n&&"\\"==r
                }
                return n
            }
            function i(e,t,r){
                return U=e,K=r,t
            }
            function o(e,t){
                var o=e.next();
                if('"'==o||"'"==o)return r(e,t,l(o));
                if(/[\[\]{
                    
                }
                \(\),;
                \:\.]/.test(o))return i(o);
                if("0"==o&&e.eat(/x/i))return e.eatWhile(/[\da-f]/i),i("number","number");
                if(/\d/.test(o)||"-"==o&&e.eat(/\d/))return e.match(/^\d*(?:\.\d*)?(?:[eE][+\-]?\d+)?/),i("number","number");
                if("/"==o)return e.eat("*")?r(e,t,s):e.eat("/")?(e.skipToEnd(),i("comment","comment")):"operator"==t.lastType||"keyword c"==t.lastType||/^[\[{
                    
                }
                \(,;
                :]$/.test(t.lastType)?(n(e,"/"),e.eatWhile(/[gimy]/),i("regexp","string-2")):(e.eatWhile(Z),i("operator",null,e.current()));
                if("#"==o)return e.skipToEnd(),i("error","error");
                if(Z.test(o))return e.eatWhile(Z),i("operator",null,e.current());
                e.eatWhile(/[\w\$_]/);
                var a=e.current(),u=q.propertyIsEnumerable(a)&&q[a];
                return u&&"."!=t.lastType?i(u.type,u.style,a):i("variable","variable",a)
            }
            function l(e){
                return function(t,r){
                    return n(t,e)||(r.tokenize=o),i("string","string")
                }
            }
            function s(e,t){
                for(var r,n=!1;
                r=e.next();
                ){
                    if("/"==r&&n){
                        t.tokenize=o;
                        break
                    }
                    n="*"==r
                }
                return i("comment","comment")
            }
            function a(e,t,r,n,i,o){
                this.indented=e,this.column=t,this.type=r,this.prev=i,this.info=o,null!=n&&(this.align=n)
            }
            function u(e,t){
                for(var r=e.localVars;
                r;
                r=r.next)if(r.name==t)return!0
            }
            function c(e,t,r,n,i){
                var o=e.cc;
                for(Q.state=e,Q.stream=i,Q.marked=null,Q.cc=o,e.lexical.hasOwnProperty("align")||(e.lexical.align=!0);
                ;
                ){
                    var l=o.length?o.pop():Y?x:b;
                    if(l(r,n)){
                        for(;
                        o.length&&o[o.length-1].lex;
                        )o.pop()();
                        return Q.marked?Q.marked:"variable"==r&&u(e,n)?"variable-2":t
                    }
                }
            }
            function f(){
                for(var e=arguments.length-1;
                e>=0;
                e--)Q.cc.push(arguments[e])
            }
            function h(){
                return f.apply(null,arguments),!0
            }
            function d(e){
                function t(t){
                    for(var r=t;
                    r;
                    r=r.next)if(r.name==e)return!0;
                    return!1
                }
                var r=Q.state;
                if(r.context){
                    if(Q.marked="def",t(r.localVars))return;
                    r.localVars={
                        name:e,next:r.localVars
                    }
                }
                else{
                    if(t(r.globalVars))return;
                    r.globalVars={
                        name:e,next:r.globalVars
                    }
                }
            }
            function p(){
                Q.state.context={
                    prev:Q.state.context,vars:Q.state.localVars
                }
                ,Q.state.localVars=ee
            }
            function m(){
                Q.state.localVars=Q.state.context.vars,Q.state.context=Q.state.context.prev
            }
            function g(e,t){
                var r=function(){
                    var r=Q.state,n=r.indented;
                    "stat"==r.lexical.type&&(n=r.lexical.indented),r.lexical=new a(n,Q.stream.column(),e,null,r.lexical,t)
                }
                ;
                return r.lex=!0,r
            }
            function v(){
                var e=Q.state;
                e.lexical.prev&&(")"==e.lexical.type&&(e.indented=e.lexical.indented),e.lexical=e.lexical.prev)
            }
            function y(e){
                return function(t){
                    return t==e?h():";
                    "==e?f():h(arguments.callee)
                }
            }
            function b(e){
                return"var"==e?h(g("vardef"),I,y(";
                "),v):"keyword a"==e?h(g("form"),x,b,v):"keyword b"==e?h(g("form"),b,v):"{
                    "==e?h(g("
                }
                "),E,v):";
                "==e?h():"if"==e?h(g("form"),x,b,v,P):"function"==e?h(B):"for"==e?h(g("form"),y("("),g(")"),F,y(")"),v,b,v):"variable"==e?h(g("stat"),T):"switch"==e?h(g("form"),x,g("
            }
            ","switch"),y("{
                "),E,v,v):"case"==e?h(x,y(":")):"default"==e?h(y(":")):"catch"==e?h(g("form"),p,y("("),j,y(")"),b,v,m):f(g("stat"),x,y(";
                "),v)
            }
            function x(e){
                return C(e,!1)
            }
            function w(e){
                return C(e,!0)
            }
            function C(e,t){
                var r=t?M:S;
                return J.hasOwnProperty(e)?h(r):"function"==e?h(B):"keyword c"==e?h(t?k:L):"("==e?h(g(")"),L,y(")"),v,r):"operator"==e?h(t?w:x):"["==e?h(g("]"),W(w,"]"),v,r):"{
                    "==e?h(g("
                }
                "),W(N,"
            }
            "),v,r):h()
        }
        function L(e){
            return e.match(/[;
            \
        }
        \)\],]/)?f():f(x)
    }
    function k(e){
        return e.match(/[;
        \
    }
    \)\],]/)?f():f(w)
}
function S(e,t){
    return","==e?h(x):M(e,t,!1)
}
function M(e,t,r){
    var n=0==r?S:M,i=0==r?x:w;
    return"operator"==e?/\+\+|--/.test(t)?h(n):"?"==t?h(x,y(":"),i):h(i):";
    "!=e?"("==e?h(g(")","call"),W(w,")"),v,n):"."==e?h(A,n):"["==e?h(g("]"),L,y("]"),v,n):void 0:void 0
}
function T(e){
    return":"==e?h(v,b):f(S,y(";
    "),v)
}
function A(e){
    return"variable"==e?(Q.marked="property",h()):void 0
}
function N(e,t){
    if("variable"==e){
        if(Q.marked="property","get"==t||"set"==t)return h(H)
    }
    else("number"==e||"string"==e)&&(Q.marked=e+" property");
    return J.hasOwnProperty(e)?h(y(":"),w):void 0
}
function H(e){
    return":"==e?h(x):"variable"!=e?h(y(":"),x):(Q.marked="property",h(B))
}
function W(e,t){
    function r(n){
        if(","==n){
            var i=Q.state.lexical;
            return"call"==i.info&&(i.pos=(i.pos||0)+1),h(e,r)
        }
        return n==t?h():h(y(t))
    }
    return function(n){
        return n==t?h():f(e,r)
    }
}
function E(e){
    return"
}
"==e?h():f(b,E)
}
function D(e){
return":"==e?h(O):f()
}
function O(e){
return"variable"==e?(Q.marked="variable-3",h()):f()
}
function I(e,t){
return"variable"==e?(d(t),$?h(D,z):h(z)):f()
}
function z(e,t){
return"="==t?h(w,z):","==e?h(I):void 0
}
function P(e,t){
return"keyword b"==e&&"else"==t?h(g("form"),b,v):void 0
}
function F(e){
return"var"==e?h(I,y(";
"),V):";
"==e?h(V):"variable"==e?h(R):f(x,y(";
"),V)
}
function R(e,t){
return"in"==t?h(x):h(S,V)
}
function V(e,t){
return";
"==e?h(G):"in"==t?h(x):f(x,y(";
"),G)
}
function G(e){
")"!=e&&h(x)
}
function B(e,t){
return"variable"==e?(d(t),h(B)):"("==e?h(g(")"),p,W(j,")"),v,b,m):void 0
}
function j(e,t){
return"variable"==e?(d(t),$?h(D):h()):void 0
}
var U,K,_=e.indentUnit,X=t.statementIndent,Y=t.json,$=t.typescript,q=function(){
function e(e){
    return{
        type:e,style:"keyword"
    }
}
var t=e("keyword a"),r=e("keyword b"),n=e("keyword c"),i=e("operator"),o={
    type:"atom",style:"atom"
}
,l={
    "if":e("if"),"while":t,"with":t,"else":r,"do":r,"try":r,"finally":r,"return":n,"break":n,"continue":n,"new":n,"delete":n,"throw":n,"var":e("var"),"const":e("var"),let:e("var"),"function":e("function"),"catch":e("catch"),"for":e("for"),"switch":e("switch"),"case":e("case"),"default":e("default"),"in":i,"typeof":i,"instanceof":i,"true":o,"false":o,"null":o,undefined:o,NaN:o,Infinity:o,"this":e("this")
}
;
if($){
    var s={
        type:"variable",style:"variable-3"
    }
    ,a={
        "interface":e("interface"),"class":e("class"),"extends":e("extends"),constructor:e("constructor"),"public":e("public"),"private":e("private"),"protected":e("protected"),"static":e("static"),"super":e("super"),string:s,number:s,bool:s,any:s
    }
    ;
    for(var u in a)l[u]=a[u]
}
return l
}
(),Z=/[+\-*&%=<>!?|~^]/,J={
atom:!0,number:!0,variable:!0,string:!0,regexp:!0,"this":!0
}
,Q={
state:null,column:null,marked:null,cc:null
}
,ee={
name:"this",next:{
    name:"arguments"
}
}
;
return v.lex=!0,{
startState:function(e){
    return{
        tokenize:o,lastType:null,cc:[],lexical:new a((e||0)-_,0,"block",!1),localVars:t.localVars,globalVars:t.globalVars,context:t.localVars&&{
            vars:t.localVars
        }
        ,indented:0
    }
}
,token:function(e,t){
    if(e.sol()&&(t.lexical.hasOwnProperty("align")||(t.lexical.align=!1),t.indented=e.indentation()),t.tokenize!=s&&e.eatSpace())return null;
    var r=t.tokenize(e,t);
    return"comment"==U?r:(t.lastType="operator"!=U||"++"!=K&&"--"!=K?U:"incdec",c(t,r,U,K,e))
}
,indent:function(e,r){
    if(e.tokenize==s)return CodeMirror.Pass;
    if(e.tokenize!=o)return 0;
    for(var n=r&&r.charAt(0),i=e.lexical,l=e.cc.length-1;
    l>=0;
    --l){
        var a=e.cc[l];
        if(a==v)i=i.prev;
        else if(a!=P||/^else\b/.test(r))break
    }
    "stat"==i.type&&"
}
"==n&&(i=i.prev),X&&")"==i.type&&"stat"==i.prev.type&&(i=i.prev);
var u=i.type,c=n==u;
return"vardef"==u?i.indented+("operator"==e.lastType||","==e.lastType?4:0):"form"==u&&"{
    "==n?i.indented:"form"==u?i.indented+_:"stat"==u?i.indented+("operator"==e.lastType||","==e.lastType?X||_:0):"switch"!=i.info||c||0==t.doubleIndentSwitch?i.align?i.column+(c?0:1):i.indented+(c?0:_):i.indented+(/^(?:case|default)\b/.test(r)?_:2*_)
}
,electricChars:":{
    
}
",blockCommentStart:Y?null:"/*",blockCommentEnd:Y?null:"*/",lineComment:Y?null:"//",fold:"brace",helperType:Y?"json":"javascript",jsonMode:Y
}
}),CodeMirror.defineMIME("text/javascript","javascript"),CodeMirror.defineMIME("text/ecmascript","javascript"),CodeMirror.defineMIME("application/javascript","javascript"),CodeMirror.defineMIME("application/ecmascript","javascript"),CodeMirror.defineMIME("application/json",{
name:"javascript",json:!0
}),CodeMirror.defineMIME("application/x-json",{
name:"javascript",json:!0
}),CodeMirror.defineMIME("text/typescript",{
name:"javascript",typescript:!0
}),CodeMirror.defineMIME("application/typescript",{
name:"javascript",typescript:!0
});
var NUM_TEXTS=[null,"One","Two","Three","Four","Five","Six","Seven","Eight","Nine","Ten"],editor=null,worker=null,cache={
toggled:{
configure:!1,output:!0
}
}
,prefs=restore("prefs")||{
opts:{
forin:!0,noarg:!0,bitwise:!0,nonew:!0,strict:!1,browser:!0,devel:!0,node:!1,jquery:!1,esnext:!1,moz:!1,es3:!1
}
,rev:{
eqnull:!0,debug:!0,boss:!0,evil:!0,loopfunc:!0,laxbreak:!0
}
,meta:{
unused:!0,undef:!0,complex:!0
}
}
;
