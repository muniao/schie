<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>数据表管理</title>
	<%@ include file="/WEB-INF/views/include/headMeta.jsp" %>
    <%@ include file="/WEB-INF/views/include/headCss.jsp" %>
    <%@ include file="/WEB-INF/views/include/headJs.jsp" %>
    <%@ include file="/WEB-INF/views/include/echarts.jsp" %>
    <%@include file="/WEB-INF/views/include/treeview.jsp" %>
</head>
<body>
<div class="col-sm-2 hidden-xs" style="border-right: 1px solid #eee; padding-right: 0px;">
    <!-- 内容-->
    <div class="wrapper">
        <!-- 内容盒子-->
        <div class="box box-main">
            <!-- 内容盒子头部 -->
            <div class="box-header">
                <div class="box-title">
                    <i class="fa fa-edit"></i>数据表
                </div>
                <div class="box-tools pull-right">
                    <button type="button" class="btn btn-box-tool" id="btnEdit" title="管理">
                        <i class="fa fa-address-card"></i></button>
                    <button type="button" class="btn btn-box-tool" id="btnExpand" title="展开" style="display:none;">
                        <i class="fa fa-chevron-up"></i></button>
                    <button type="button" class="btn btn-box-tool" id="btnCollapse" title="折叠"><i
                            class="fa fa-chevron-down"></i></button>
                    <button type="button" class="btn btn-box-tool" id="btnRefresh" title="刷新"><i
                            class="fa fa-refresh"></i></button>
                    <div id="organ" class="col-sm-12" style="display: none">
                        <%--${user.company.name}--%>
                        <sys:treeselect id="company" name="company.id" value="${user.company.id}"
                                        labelName="company.name" labelValue="请选择机构"
                                        title="公司" url="/sys/office/treeData?type=1"
                                        cssClass="form-control required" onChange="adminfreshTree()"  />
                    </div>
                    <%--<button type="button" class="btn btn-box-tool" id="btnAdmin" title="展示其他机构树" style="display: none"><i--%>
                            <%--class="fa fa-file"></i></button>--%>
                </div>
            </div>
            <!-- 内容盒子身体 -->
            <div class="box-body">
                <div id="ztree" class="ztree leftBox-content"></div>
            </div>
        </div>
    </div>
</div>


<script type="text/javascript">
    var num = 0;
    var tree;
    var setting = {
        data: {simpleData: {enable: true, idKey: "id", pIdKey: "pId", rootPId: '0'}},
        callback: {
            onClick: function (event, treeId, treeNode) {
                var id = treeNode.pId == '0' ? '' : treeNode.pId;
                console.log(treeNode);
                //执行查询按钮的方法
                // sortOrRefresh();
                if (treeNode.orgid!=5){
                    if (num!=0){
                        $('#iframeContent').attr("src", "${ctx}/exchange/exDbStandard/listVue?exTabId=" + treeNode.id+"&orgid="+treeNode.orgid);
                        return;
                    }
                    $('#iframeContent').attr("src", "${ctx}/exchange/exDbStandard/list?exTabId=" + treeNode.id+"&orgid="+treeNode.orgid);
                }
                else {
                    if (id==''){
                        $('#iframeContent').attr("src", "${ctx}/exchange/exDbStandard/list?exTabId=&orgid=4545");
                        return;
                    }

                    $('#iframeContent').attr("src", "${ctx}/exchange/exDbStandard/list?exTabId=" + treeNode.id);
                }
            }
        }
    };


    $(document).ready(function(){
        $.ajax({
            url: "${ctx}/test/tree/testTree/judgeOrg",
            type: "post",
            dataType: "json",
            success: function(data) {
                console.log(data);
                if (data.orgid=="true"){
                    $('#organ').css('display','');
                }
            }
            });

    });
    function refreshTree() {
        $.getJSON("${ctx}/test/tree/testTree/treeData?type=2", function (data) {
            tree =$.fn.zTree.init($("#ztree"), setting, data);
            //tree.expandAll(true);
            console.log(data);
            // 展开第一级节点
            var nodes = tree.getNodesByParam("level", 0);
            console.log(nodes);
            for(var i=0; i<nodes.length; i++) {
                tree.expandNode(nodes[i], true, false, false);
            }
        });


    }

    refreshTree();

    function refresh() {//刷新
        window.location = "${ctx}/exchange/exDbStandard/index";

    }
    function ahreftree() {//跳转
        //数据表type为2
        window.location = "${ctx}/test/tree/testTree/list?type=2";
    }
    function adminfreshTree() {
        var orgid = $('#companyId').val();
        num=1;
        $.getJSON("${ctx}/test/tree/testTree/treeData?type=2&extId=1&orgid="+orgid, function (data) {
            tree =$.fn.zTree.init($("#ztree"), setting, data);
            //tree.expandAll(true);
            // 展开第一级节点
            var nodes = tree.getNodesByParam("level", 0);
            for(var i=0; i<nodes.length; i++) {
                tree.expandNode(nodes[i], true, false, false);
            }
        });
    }

    // 工具栏按钮绑定
    $('#btnExpand').click(function () {
        tree.expandAll(true);
        $(this).hide();
        $('#btnCollapse').show();
    });
    $('#btnCollapse').click(function () {
        tree.expandAll(false);
        $(this).hide();
        $('#btnExpand').show();
    });
    $('#btnRefresh').click(function () {
        refresh();
    });
    $('#btnEdit').click(function () {
        ahreftree();
    });
    // $('#btnAdmin').click(function () {
    //     adminfreshTree();
    // });

</script>

<div class="col-sm-10" style="padding-left: 0px;">
    <iframe id="iframeContent" name="iframeContent" src="${ctx}/exchange/exDbStandard/list?orgid=4545" width="100%" height="100%"
            frameborder="0"></iframe>
</div>

<!-- 信息-->
<div id="messageBox">${message}</div>
<script src="/staticViews/viewBase.js"></script>
<%--<script src="/staticViews/modules/exchange/exDbStandardList.js" type="text/javascript"></script>--%>
<link href="/staticViews/modules/exchange/exDbStandardList.css" rel="stylesheet" />
</body>
</html>