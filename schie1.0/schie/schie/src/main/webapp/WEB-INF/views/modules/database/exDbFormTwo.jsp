<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>数据库管理</title>
    <%@ include file="/WEB-INF/views/include/headMeta.jsp" %>
    <%@ include file="/WEB-INF/views/include/headCss.jsp" %>
    <%@ include file="/WEB-INF/views/include/headJs.jsp" %>
	<%@ include file="/WEB-INF/views/include/echarts.jsp" %>
	<%@include file="/WEB-INF/views/include/treeview.jsp" %>
</head>
<body>
<script>

	<%--$(function () {--%>
		<%--var dbType = document.getElementById('dbType');--%>
		<%--for(var i=0;i<dbType.options.length;i++){--%>
			<%--if(dbType.options[i].value=='${exDb.dbType}'){--%>
				<%--dbType.options[i].selected=true;--%>
				<%--break;--%>
			<%--}--%>
		<%--}--%>
	<%--})--%>
</script>


<div class="col-sm-12 hidden-xs" style="border-right: 1px solid #eee; padding-right: 0px;">
<!-- 内容-->
<div class="wrapper">
    <!-- 内容盒子-->
    <div class="box box-main">
        <!-- 内容盒子头部 -->
        <div class="box-header">
            <div class="box-title"><i class="fa fa-edit"></i>
                数据库管理
            </div>
        </div>
        <!-- 内容盒子身体 -->
        <div class="box-body">

			<form:form id="inputForm" modelAttribute="exDb" action="${ctx}/database/exDb/save" method="post" class="form-horizontal content-background">
				<div class="content">
					<form:hidden path="id"/>
					<sys:message content="${message}"/>
					<div class="form-unit">基本信息</div>
					<div class="row">
						<div class="form-group col-xs-6">
							<label class="control-label col-sm-4 pull-left">数据库名称</label>
							<div class="col-sm-8">
									<form:input placeholder="数据库名称" path="dbName" htmlEscape="false" maxlength="100" class="form-control required "/>
							</div>
						</div>
						<div class="form-group col-xs-6">
							<label class="control-label col-sm-4 pull-left">数据库类型</label>
							<div class="col-sm-8">
								<form:select id="dbtype" path="dbType" class="form-control" onchange="urlFunction()">
									<form:option value="" label="请选择"/>
									<form:options items="${fns:getDictList('db_type')}" itemLabel="label"
												  itemValue="value" htmlEscape="false"/>
								</form:select>
							</div>
						</div>
						<div class="form-group col-xs-12" style="position:relative;padding-right:45px">
							<label class="control-label col-sm-2 pull-left">jdbcurl：</label>
							<div class="col-sm-10">
								<form:input  placeholder="请填写自己的jdbcurl" path="dbUrl" class="form-control required"  htmlEscape="false" maxlength="200"/>
								<span id="helpdburl" class="help-inline"></span>
							</div>
						</div>
						<div class="form-group col-xs-6">
							<label class="control-label col-sm-4 pull-left">用户名</label>
							<div class="col-sm-8">
									<form:input placeholder="用户名" path="dbUser" htmlEscape="false" maxlength="100" class="form-control required"/>
							</div>
						</div>
						<div class="form-group col-xs-6">
							<label class="control-label col-sm-4 pull-left">数据库密码</label>
							<div class="col-sm-8">
									<%--<form:input placeholder="数据库密码" path="dbPwd" htmlEscape="false" maxlength="100" class="form-control "/>--%>
										<input id="dbPwd" name="dbPwd" type="password" value="" maxlength="50" class="form-control required"/>
										<c:if test="${not empty exDb.id}"><span
												class="help-inline">若不修改密码，请留空。</span></c:if>
							</div>
						</div>
						<div class="form-group col-xs-6">
							<label class="control-label col-sm-4 pull-left">最大读线程</label>
							<div class="col-sm-8">
								<form:input placeholder="最大读线程" path="readThread" htmlEscape="false" class="form-control"/>
							</div>
						</div>
						<div class="form-group col-xs-6">
							<label class="control-label col-sm-4 pull-left">最大写线程</label>
							<div class="col-sm-8">
								<form:input placeholder="最大写线程" path="writeThread" htmlEscape="false" class="form-control" />
							</div>
						</div>
						<div class="form-group col-xs-6">
							<label class="control-label col-sm-4 pull-left">排序</label>
								<div class="col-sm-8">
									<form:input placeholder="排序" path="sort" htmlEscape="false" class="form-control  number"/>
							</div>
						</div>
						<div class="form-group col-xs-6">
							<label class="control-label col-sm-4 pull-left">描述</label>
							<div class="col-sm-8">
								<form:textarea path="remarks" htmlEscape="false" rows="4" maxlength="255" class="form-control "/>
							</div>
						</div>
						<form:hidden placeholder="关联树表ID" path="exTabId" htmlEscape="false" maxlength="100" class="form-control "/>

					</div>
					<input id="oldSearch" name="oldSearch" type="hidden" htmlEscape="true" value="${oldSearch}"/>
				    <div class="form-group">
                        <c:if test="${action ne 'view'}">
                        <a id="btnSubmit" class="btn btn-primary">保存</a>
                        </c:if>
                        <a id="btnBack" class="btn btn-default">返回</a>
                    </div>
				</div>
		</form:form>
		 </div>
	</div>
    </div>
</div>
<div id="messageBox">${message}</div>
<%@ include file="/WEB-INF/views/include/footJs.jsp" %>
<script src="/staticViews/viewBase.js"></script>
<script type="text/javascript">
    $(document).ready(function(){
        var id = '${exDb.id}';
        if (id!=null&&id!=''){
            $("#dbPwd").attr("class","form-control");
        }
    });
    //根据不同数据库类型  提示jdbcurl
    function urlFunction(){
        var dbtype = document.getElementById("dbtype").value;
        var dburl;
        switch (dbtype) {
			case "oracle": dburl = "jdbc:oracle:thin:@127.0.0.1:1521:orcl";
			break;
			case "sqlserver": dburl = "jdbc:sqlserver://localhost:1433; DatabaseName=schie";
			break;
			case "mysql": dburl = "jdbc:mysql://127.0.0.1:3306/schie";
			break;
			case "postgresql": dburl = "jdbc:postgresql://127.0.0.1:5432/schie?currentSchema=public";
			break;
			default: dburl = "请填写自己的jdbcurl";
        }
        $("#helpdburl").html(dburl);//给辅助栏赋值
        // document.getElementById("dburl").setAttribute("placeholder",dburl);
	}
</script>
<script src="/staticViews/modules/database//exDbForm.js" type="text/javascript"></script>
<link href="/staticViews/modules/database//exDbForm.css" rel="stylesheet" />
</body>
</html>