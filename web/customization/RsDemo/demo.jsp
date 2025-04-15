<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="java.util.regex.*" %>
<%@ page import="net.sf.json.*" %>
<%@ page import="weaver.general.Util" %>
<%@ page import="weaver.general.BaseBean" %>
<%@ page import="weaver.hrm.HrmUserVarify" %>
<%@ page import="weaver.hrm.User" %>
<%
JSONObject resultObj = new JSONObject();
resultObj.put("v", "2020062001");
resultObj.put("status", 0);

User user = HrmUserVarify.getUser (request , response) ;
if (user==null){
    resultObj.put("info", "登录信息失效");
	response.sendRedirect("/wui/index.html");
    return;
}
%>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8">
		<meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate" />
		<meta http-equiv="Pragma" content="no-cache" />
		<meta http-equiv="Expires" content="0" />	
		<meta name="viewport" content="initial-scale=1.0, maximum-scale=1, user-scalable=no">
		<link href="css/style.css" rel="stylesheet" />
		<title>RS API</title>
		<script type="text/javascript" src="/cloudstore/resource/pc/jquery/jquery-1.8.3.min.js"></script>
        <script type="text/javascript" src="js/tool.js"></script>
		<script type="text/javascript" src="js/data.js"></script>
	</head>
	<body>
		<div class="book-wrapper">
			<div class="booktop"> 
				<div class="tabbook">
					<div class="ative" onclick='changemode(0)' id='tabbook_0'>累计完成率<span></span></div>
					<div onclick='changemode(1)' id='tabbook_1'>月度完成率<span></span></div>
				</div>
				<div class="percentage">
					<div id='yearPreComplete'></div>
					<div id='jyj_003'>65%</div>
					<div>37<span>累计预警</span></div>
				</div>
			</div>
			<div class="booklist">
				<div class="jydw-list">
					<div class="list-title">
						<div class="line"></div>
						<span>经营单位</span>
					</div> 
					<div class="listtable">
						<div class="listhead">
							<span style="width: 26px;">排名</span>
							<span style="width: 25%;">单位</span>
							<span style="width: 16%;">初审</span>
							<span style="width: 15%;">终审</span>
							<span style="max-width: 19%;">待终审(条)</span>
						</div>
						<div class="listtbody" id="listtbody">
						</div>
					</div>
				</div>
				<div class="jydw-list">
					<div class="list-title">
						<div class="line"></div>
						<span>职能部门</span>
					</div> 
					<div class="listtable">
						<div class="listhead">
							<span style="width: 37px;">排名</span>
							<span style="width: 25%;">单位</span>
							<span style="width: 16%;">初审</span>
							<span style="width: 15%;">终审</span>
							<span style="max-width: 19%;">待终审(条)</span>
						</div>
						<div class="listtbody" id="z-listtbody">
						</div>
					</div>
				</div>
				
			</div>
		</div>
	</body>
</html>
