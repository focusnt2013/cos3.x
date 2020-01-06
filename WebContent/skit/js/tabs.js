$(function () {
    "use strict";
    var allow_edit = true;
    
    var cEditor = {
        type: 'cm'//or 'ace'
    };
    var _p = cEditor;
    _p.init = function(node, mode, readOnly){
        //debugger;
        if(!node){
            return;
        }
        if(this.type==='ace'){
            var editor = ace.edit(node);
            //editor.getSession().setUseWorker(false);
            editor.setOptions({
                maxLines: Infinity
            });
            //editor.setTheme("ace/theme/monokai");
            //editor.setTheme("ace/theme/dawn");
            editor.setTheme("ace/theme/textmate");
            //editor.setTheme("ace/theme/terminal");
            editor.getSession().setMode("ace/mode/"+mode);            
        }
        else{   
            //debugger;
            var text = $(node).text();
            $(node).empty();
            CodeMirror(node, {
                //var myCodeMirror = CodeMirror.fromTextArea(editor , {
                value: text,
                lineNumbers: true,
                //mode: 'javascript',
                mode: (mode==="html"?"xml":mode),
                htmlMode: (mode==="html"),
                viewportMargin: Infinity,
                readOnly: readOnly,
                //lineWrapping: true,
                extraKeys: { "Ctrl-Q": function (cm) { 
                        cm.foldCode(cm.getCursor()); 
                    }
                    /*"F11": function(cm) {
                      cm.setOption("fullScreen", !cm.getOption("fullScreen"));
                    },
                    "Esc": function(cm) {
                      if (cm.getOption("fullScreen")) cm.setOption("fullScreen", false);
                    }*/                            
                },
                foldGutter: true,
                gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"],
                theme: "eclipse",//"eclipse",//monokai
                styleActiveLine: true,
                matchBrackets: true,
                indentWithTabs: true,
                indentUnit: 4
            });            
        }
    };
    _p.getValue = function(node){
        if(!node){
            return;
        }        
        if(this.type==='ace'){
            return ace.edit(node).getValue();            
        }
        else{
            var cm = $(node).find('.CodeMirror')[0].CodeMirror;
            return cm.getValue();            
        }        
    };
    /*_p.setValue = function(val){
        if(this.type==='ace'){
            
        }
        else{
            
        }        
    };*/    
    //debugger;
    //var scriptt, scriptt2;
    var ppcss, pphtml, ppjs, ppc, php, java;
    //alert($( "#accordion" ).accordion('option','heightStyle'));
    function removeTab(tabId) {
        $("a[href='#tabL-" + tabId + "']").parent("li").remove();
        $("div#tabL-" + tabId).remove();
    }
    var $loadingpane;
    function showLoading() {
        $loadingpane = $("#loadingDialogDiv"); //.appendTo($rightPane);
        $loadingpane.dialog({ modal: true, height: 150 });
    }
    function addTab(xml, json) {
        if (allow_edit) {
            var $jsfiddle = $(".pp_jsfiddle"),
                jsfiddle = $jsfiddle.length;

            $("#tabL").prepend(["<div style='position:absolute;right:8px;top:8px;'>",
                    jsfiddle? "<form style='position:absolute;left:30px;top:-60px;' action='http://jsfiddle.net/api/post/library/pure/' method='POST' target='_blank'><input type='hidden' name='html'/><input type='hidden' name='js'/><input type='hidden' name='css'/>":"",
                    jsfiddle? "<a href='' id='jsfiddle_link'><img src='/pro2/content/images/jsfiddle.png'/></a></form>": "",
                    "<button id='ace_button' type='button'>保存</button></div>"].join(""));
        }
        
        if (xml) {
            var $ed = $("pre.xml").text(xml);
            cEditor.init($ed[0], "xml");  
        }
        else {
            removeTab("xml");
        }

        if (json) {
            var $ed = $("pre.json").text(json);
            cEditor.init($ed[0], "javascript");
        }
        else {
            removeTab("json");
        }

        //prettyPrint();
        //SyntaxHighlighter.all();
        if(!allow_edit){
            SyntaxHighlighter.highlight();
        }
        $("#tabL").tabs({
            activate: function(evt, ui){
            }
        });

    }
    (function () {
        addTab();
    })();
});
