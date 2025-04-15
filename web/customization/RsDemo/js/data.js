function changemode(type){};//book.jsp切换顶部菜单
function qkclick(){};//book.jsp切换顶部菜单

var bookmode=0;//0累计  1 月度

//2020/6/20, 4:53 PM lth    start    
jQuery(document).ready(function(){
	
	 if(window.console) console.log("lth:655====getPath:",devTools.getPath()); //2020/6/20, 6:21 PM lth    end   
	/*book.jsp start*/
	if(devTools.getPath().indexOf('book.jsp')>0){
		
		
		 
		
		
		 changemode = function(type){
			 bookmode=type;
			
			//移除divclass
			$("#tabbook_0,#tabbook_1").removeClass("ative");
			$("#tabbook_"+type).addClass("ative");
			if(type==1){
				$('#jyj_003').hide();
			}else{
				$('#jyj_003').show();
			}
			
			
			if($('#yearPreComplete')){//初审完成率
				var apikey='yearPreComplete';
				if(type==1){apikey='monthInitComplete';}
				  var res =devTools.get ({apikey:apikey});
				  $('#yearPreComplete').html(res.datas[0].cswcl+'<span>初审</span>');  
			}	
			if($('#monthProComplete')){
				var apikey='monthProComplete';
				if(type==1){apikey='monthEndComplete';}
				  var res =devTools.get ({apikey:apikey});
				 $('#monthProComplete').html(res.datas[0].yjts+'<span>终审</span>');  
			}
			if($('#jyj_003')&&type==0){
				var apikey='jyj_003';
				 
				 var res =devTools.get ({apikey:apikey});
				 $('#jyj_003').html(res.datas[0].yjts+'<span>累计预警</span>');  
			}
			
			if($('#listtbody')){//经营完成列表
				var apikey='yearDepCompletelist';//累计
				if(type==1){apikey='monthComCompletelist';}//月度
				  var res =devTools.get ({apikey:apikey});
				  var shtml='';
				  if(res.datas){
					   $('#listtbody').html(pshtml(res.datas,0));  
				  }
			  
			}
			
			if($('#z-listtbody')){//部门完成列表
				var apikey='jydwwcl';//累计
				if(type==1){apikey=='monthComCompletelist'}//月度
				  var res =devTools.get ({apikey:apikey});
				  if(res.datas){
					   $('#z-listtbody').html(pshtml(res.datas,1));  
				  }
			  
			}
			
			function pshtml(datas,deptype){
				var shtml=''
				 for(var i=0; i<datas.length;i++){
					  var d=datas[i];
						shtml+='<div class="listtd ';
						if(i<3){
							shtml+=' tdredbg ';
						}else if(i>=datas.length-3){
							shtml+=' tdgreenbg';
						}else{
							shtml+=' tdyellowbg';
						}
						shtml+=' ">';
						shtml+='<div class="linebg">'+d.rn+'</div>';
						shtml+='<div class="dw1">'+d.departmentname;
						if(d.yjsl!=''){
							
							if(deptype==0){//单位
								shtml+='<span><a href="ewarning.jsp?modetype=dep&ssdw='+d.id+'&apikey=jydwyjlb">预警'+d.yjsl+'条</a></span>';
							}else{//部门
								shtml+='<span><a href="ewarning.jsp?modetype=dep&ssdw='+d.id+'&apikey=znbmyjlbtz">预警'+d.yjsl+'条</a></span>';
								
							}
						}
						shtml+='</div>';
						shtml+='<div class="dw2"><a href="mrates.jsp?deptype='+deptype+'&bmid='+d.id+'&rwzt=2&bookmode='+bookmode+'">'+d.cswcl+'</a></div>';
						shtml+='<div class="dw3"><a href="mrates.jsp?deptype='+deptype+'&bmid='+d.id+'&rwzt=3&bookmode='+bookmode+'">'+d.zswcl+'</a></div>';
						shtml+='<div class="dw4">'+d.dwcsl+'</div>';
						shtml+='<div class="xqimg"><img src="images/xq.png"/></div>';
						shtml+='</div>';	  
				  }
				  return shtml;
				
			}
			 
			
		}
		changemode(0);
		
	/*book.jsp end*/
	}else if(devTools.getPath().indexOf('mrates.jsp')>0){/*mrates.jsp start*/
		
		
			var keywords=devTools.getQueryString('keywords'); //2020/6/21, 8:29 AM lth    填充关键字到搜索框
			if(keywords!=''){
				$('#keywords').val(keywords);
			}
		
		
		    if($('#warning-list')){
				
			 var apikey='cswcl001';   
			 if(devTools.getQueryString('bookmode')==1){//季度
				 apikey='YDWCLB';
				 $('#lineChart').hide();
			 }else{//累计
				
				var chartapikey='';
				var deptype=devTools.getQueryString('deptype');
				if(deptype==0){//单位
					chartapikey='ndwcslzxtjydw';
				}else{//部门
					chartapikey='ndwcslzxtbm';
				}
					
					  var res =devTools.get ({apikey:chartapikey});
					  var shtml='';
					  if(res.datas){
						  if(window.console) console.log("lth:822====chart",res.datas); //2020/6/21, 5:24 PM lth    end   
					  }
					
						$('#lineChart').show();	 	 
						var myChart = echarts.init(document.getElementById('lineChart'));	
						// 指定图表的配置项和数据
						var xdata = ['01月', '02月', '03月', '04月', '05月', '06月', '07月'];
						var ydata = [32, 60, 90, 92, 130, 40, 140];
						var option = {
								grid: {
									show: false, //---是否显示直角坐标系网格
									right: 26, //---相对位置，top\bottom\left\right  
									top: 40,
									bottom:45
								},
								xAxis: {
									type: 'category',
									data: xdata
								},
								yAxis: {
									axisLabel: {  
										show: true,  
										interval: 'auto',  
										formatter: '{value} %'  
										},  
									show: true,
									type: 'value'
								},
								series: [{
									data: ydata,
									type: 'line',
									itemStyle : { 
										normal : { 
										color:'#2C79C2', //改变折线点的颜色
										lineStyle:{ 
										color:'#2C79C2' //改变折线颜色
										} 
									} ,
									}
									
								}]
							};	
							// 使用刚指定的配置项和数据显示图表。
							myChart.setOption(option);
				
					 
				 }
			 var res =devTools.get ({apikey:apikey});
			 if(res.datas){
				  var shtml=''
				  var datas=res.datas;
				 for(var i=0; i<datas.length;i++){
					var d=datas[i];				   
					shtml+=' <div class="listdiv" >';
					shtml+='<div class="listtit">';
					shtml+='	<div class="title">编号 '+d.jhbh+'</div>';
					shtml+='	<span class="yqtime red">';
					if(devTools.getQueryString('rwzt')==2){
						shtml+='初审完成';
					}else{shtml+='终审完成';}
					shtml+='</span>';
					shtml+='</div>';
					shtml+='<div class="listtxt sndian">';
					shtml+='	<font size="" color="">任务名称：</font>';
					shtml+='	<span>'+d.jhfj+'</span>';
					shtml+='</div>';
					shtml+='<div class="listtxt">';
					shtml+='	<font size="" color="">计划完成日期：</font><span>'+d.jhwcsj+'</span>';
					shtml+='	<a class="xqbtn" href="details.jsp?id='+d.id+'&apikey=rwqx">查看详情</a>';
					shtml+='</div>';
					shtml+='</div>';
				  }
			 }
			 $("#warning-list").html(shtml);
			 
			 	
			}
			
			devTools.createUrlHiddenInput4Form('baseform');
			

			
	}else if(devTools.getPath().indexOf('details.jsp')>0){/*details.jsp start  //2020/6/20, 10:07 PM lth    */
	 if($('#details-wrapper')){
			var apikey=devTools.getQueryString('apikey');
			if(apikey=='fsxxxx'){
				$(document).attr("title","分数详情页");
			}else if(apikey=='rwqx'){
				$(document).attr("title","任务详情页");
			}
			var res =devTools.get ({apikey:apikey});
			 if(res.datas[0]){
				  var shtml=''
				  var datas=res.datas[0];
				  
					for(var key in datas){
						if(key=='rn') continue;
						shtml+='<div class="listtd">';
						shtml+='<span class="tit">'+key+'</span>';
						shtml+='<span class="details">'+datas[key]+'</span>';
						shtml+='</div>';				  
					}
					
				$('#details-wrapper').append(shtml);	
			 }else{
				  if(window.console) console.log("lth:405====nodata"); //2020/6/20, 9:48 PM lth    end   
			 }
	 }
	 
	 
	 
	  /*details.jsp end*/
	}else if(devTools.getPath().indexOf('monthRanking.jsp')>0){/*monthRanking.jsp start*/ //2020/6/21, 6:31 AM lth    
			 
			 initdata('','');
	
			function pshtml(datas){
					 var shtml=''
					 for(var i=0; i<datas.length;i++){
						var d=datas[i];
						shtml+='<a href="monthlist.jsp?bmdw='+d.bmdw+'"><div class="listtd ';
						if(i<3){
							shtml+=' tdredbg ';
						}else if(i>=datas.length-3){
							shtml+=' tdgreenbg';
						}else{
							shtml+=' tdyellowbg';
						}
						shtml+=' ">';
						shtml+='<div class="linebg">'+d.rn+'</div>';
						shtml+='<div class="dw1">'+d.departmentname+'</div>';
						shtml+='<div class="dw2">'+d.fs+'</div>	';	
						shtml+='<div class="xqimg"><img src="images/xq.png"/></div>';
						shtml+='</div></a>';
					 				  
					}
			
				  return shtml;
				
			}
			function initdata(startdate,enddate){
				
							    if($('#listtbody')){
									 var res =devTools.get ({apikey:'fspm',startdate:startdate,enddate:enddate});
									 if(res.datas){
										 var shtml=pshtml(res.datas);
										 $('#listtbody').html(shtml);
									 }
									 
								}
								if($('#z-listtbody')){
									 var res =devTools.get ({apikey:'z-fspm',startdate:startdate,enddate:enddate});
									 if(res.datas){
										 var shtml=pshtml(res.datas);
										 $('#z-listtbody').html(shtml);
									 }
									 
								}
			}
			
			
			new Jdate({
							el: '#startdate',
							format: 'YYYY-MM',
							beginYear: 2000,
							endYear: 2100,
							init: function() {
								console.log('插件开始触发');
							},
							moveEnd: function() {
								console.log('滚动结束');
							},
							confirm: function(date) {
								var startdate=date;
								var enddate=$('#enddate').val();
								 initdata(startdate,enddate);
								 $("#datadivqkbtn").show();
								
							},
							cancel: function() {
								console.log('插件运行取消');
							}
						});
				new Jdate({
							el: '#enddate',
							format: 'YYYY-MM',
							beginYear: 2000,
							endYear: 2100,
							init: function() {
								console.log('插件开始触发');
							},
							moveEnd: function() {
								console.log('滚动结束');
							},
							confirm: function(date) {
								var startdate=$('#startdate').val();
								var enddate=date;
								initdata(startdate,enddate);
								$("#datadivqkbtn").show();
							},
							cancel: function() {
								console.log('插件运行取消');
							}
						});
			 qkclick= function(){
				$("#startdate").val('');
				$("#enddate").val('');
				$("#datadivqkbtn").hide();
				initdata('','');
			}
	
	
	}else if(devTools.getPath().indexOf('monthlist.jsp')>0){/*monthlist.jsp start*/  //2020/6/21, 7:40 AM lth     
			
			var keywords=devTools.getQueryString('keywords');
			if(keywords!=''){
				$('#keywords').val(keywords);
			}
			if($('#listtbody')){
					 var res =devTools.get ({apikey:'fs2j'});
					 if(res.datas){
						var datas=res.datas;
						var shtml='';
						  for(var i=0; i<datas.length;i++){
							var d=datas[i]; 
								shtml+='<div class="listdiv" >';
								shtml+='<div class="listtit">';
								shtml+='	<div class="title">'+d.khx+'</div>';
								shtml+='	<span class="yqtime red">'+d.fs+'</span>';
								shtml+='</div>';
								shtml+='<div class="listtxt">';
								shtml+='	<font size="" color="">考核时间：</font><span>'+d.khyd+'</span>';
								shtml+='</div>';
								shtml+='<div class="listtxt">';
								shtml+='	<font size="" color="">计划完成日期：</font><span>'+d.spwcsj+'</span>';
								shtml+='	<a class="xqbtn" href="details.jsp?id='+d.id+'&apikey=fsxxxx">查看详情</a>';
								shtml+='</div>';
								shtml+='</div>';			  
						}
						$('#listtbody').html(shtml);
						 
					 }
					 
			 }
			 
			 devTools.createUrlHiddenInput4Form('baseform');
			
	
	}else if(devTools.getPath().indexOf('ewarning.jsp')>0){/*ewarning.jsp start*/  //2020/6/21, 3:17 PM lth    
	
		
		
		
		var keywords=devTools.getQueryString('keywords');
			if(keywords!=''){
				$('#keywords').val(keywords);
		}
		
		var modetype=devTools.getQueryString('modetype');
		if(modetype=='dep'){
			$('#titlenum').hide();
		}else{
			$('#titlenum').show();
			if($('#yjts')){//填充页面即将到期预警天数
					 var res =devTools.get ({apikey:'yjts'});
					 if(res.datas[0]){
						$('#yjts').html(res.datas[0].yjts+' <span>即将到期</span>');
					 }
			}
			if($('#yqyjzs')){//填充延期预警天数
						 var res =devTools.get ({apikey:'yqyjzs'});
						 if(res.datas[0]){
							$('#yqyjzs').html(res.datas[0].yjts+'<span>已经延期</span>');
						 }
			}
		}
		
		 
		
		if($('#warning-list')){//填充预警列表
		
			var apikey=devTools.getQueryString('apikey');
			 if(window.console) console.log("lth:766====apikey",apikey); //2020/6/21, 4:52 PM lth    end   
			if(apikey==''||apikey==null){
				apikey='yjzylb';
			}
			var res =devTools.get ({apikey:apikey});
			var shtml='';
			if(res.datas){
				var datas=res.datas;
					
				for(var i=0; i<datas.length;i++){
				    var d=datas[i]; 
					shtml+='<div class="listdiv" >';
					shtml+='<div class="listtit">';
					shtml+='	<div class="title">'+d.departmentname+'</div>';
					shtml+='	<span class="yqtime red">'+d.datecoun+'</span>';
					shtml+='</div>';
					shtml+='<div class="listtxt">';
					shtml+='	<font size="" color="">编号：</font><span>'+d.bh+'</span>';
					shtml+='</div>';
					shtml+='<div class="listtxt sndian">';
					shtml+='	<font size="" color="">标题：</font>';
					shtml+='	<span>'+d.rw+'</span>';
					shtml+='</div>';
					shtml+='<div class="listtxt">';
					shtml+='	<font size="" color="">计划完成日期：</font><span>'+d.jhwcsj+'</span>';
					if(d.deptype==0){
						shtml+='	<a class="xqbtn" href="details.jsp?id='+d.id+'&apikey=jydwrwxq">查看详情</a>';
					}else{
						shtml+='	<a class="xqbtn" href="details.jsp?id='+d.id+'&apikey=znbmrwxq">查看详情</a>';
					}
					shtml+='</div>';
					shtml+='</div>';
				}
				$('#warning-list').html(shtml);
			}
				
		}
		 devTools.createUrlHiddenInput4Form('baseform');

	
	}
	

	
}); 
//2020/6/20, 4:53 PM lth    end   
