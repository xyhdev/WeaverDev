function GlobalReger(f){
	var w = window;
	while (w != top) {
		f(w);
		w = w.parent;
	}
}

top.CriviaJavaScriptFunctions = 
	undefined!=top.CriviaJavaScriptFunctions
	?top.CriviaJavaScriptFunctions:{
		ev : 'EcologyVersion'
	,
	get: function(params){
		  var data={};
		  try{
			  var uriparam=devTools.getParams();
			  //data= {...params,_m: Math.random()*10,...uriparam,debug:1};
			 var data2= $.extend( params, uriparam );
			      data= $.extend( data2, {_m: Math.random()*10,debug:1} );
		  }catch(e){

		  }
		   //if(window.console) console.log("lth:254====params",data); //2020/6/21, 4:20 PM lth    end
		   if(window.console) console.log("lth:255====params2",data2); //2020/6/21, 4:20 PM lth    end
           var res=[];
            jQuery.ajax({
                url:"rsapi.jsp",
                type:"get",
                data:data,
                dataType: "json",
                async:false,
                cache:false,
                success:function(json){
                        res=json;
                }
            });
			 if(window.console) console.log("lth:415====res:",res); //2020/6/20, 4:57 PM lth    end
           	return res ;
        }
	,
	post: function(params){
		  var data={};
		  try{
			  var uriparam=devTools.getParams();
			  var data2= $.extend( params, uriparam );
			  data= $.extend( data2, {_m: Math.random()*10,debug:1} );
		  }catch(e){

		  }
           var res=[];
            jQuery.ajax({
                url:"rsapi.jsp",
                type:"post",
                data:data,
                dataType: "json",
                async:false,
                cache:false,
                success:function(json){
                        res=json;
                }
            });
           	return res ;
        }
	,
	getParams:function (urlStr) {
				/**
		 * [获取URL中的参数名及参数值的集合]
		 * 示例URL:http://htmlJsTest/getrequest.html?uid=admin&rid=1&fid=2&name=小明
		 * @param {[string]} urlStr [当该参数不为空的时候，则解析该url中的参数集合]
		 * @return {[string]}       [参数集合]
		 */
    if (typeof urlStr == "undefined") {
        var url = decodeURI(location.search); //获取url中"?"符后的字符串
    } else {
        var url = "?" + urlStr.split("?")[1];
    }
    var theRequest = new Object();
    if (url.indexOf("?") != -1) {
        var str = url.substr(1);
        strs = str.split("&");
        for (var i = 0; i < strs.length; i++) {
			var v=decodeURI(strs[i].split("=")[1]);
			if(v == "undefined") v='';
            theRequest[strs[i].split("=")[0]] = v;
        }
    }
    return theRequest;
	},getPath:function(){
		return window.location.pathname;
	},createUrlHiddenInput4Form(id){
		var params=devTools.getParams();
		var shtml='';
		for(var key in params){
			if(key!='keywords') shtml+='<input name="'+key+'" type="Hidden" value="'+params[key]+'"/>'	;
		}//获取
		$("#"+id).append(shtml);

	},getQueryString:function(name) {
				  var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
				  var r = window.location.search.substr(1).match(reg);
				  if (r != null) return decodeURI(r[2]);
				  return null;
				}


	}

GlobalReger(function(w){
	w.CriviaJavaScriptFunctions = top.CriviaJavaScriptFunctions;
});

top.devTools = CriviaJavaScriptFunctions==top.devTools
	?top.devTools:CriviaJavaScriptFunctions;
GlobalReger(function(w){
	w.devTools = top.devTools;
});
if(window.console) console.log("lth:103====tool.js load ok"); //2020/6/20, 4:49 PM lth    end