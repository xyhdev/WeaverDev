define("mService/hrmbrowser/tree", ["mUtil", "mService/hrmbrowser/result","i18n", "secondaryAuth"], function(mUtil, hrmResult,i18n, secondaryAuth) {

    return {
        mounted: function($page, cfg){
            var defCfg = {
                browserType : "1", //浏览框类型 1.单选  2.多选
                selectedIds : "",	//选中的id，逗号分隔，如：1,2,3
                dataType : "all",
                _parentResult : null,
                closeCallback : function(){mUtil.back();},
                success : function(){}
            };

            $.extend(defCfg, cfg);

            var isMulti = defCfg.browserType != "1" || defCfg.isMulti;
            $(".page-content", $page).addClass(isMulti ? "multi" : "single");

            var _tree = {

                init: function(){
                    var that = this;
                    var $rootPage = $(".root-page", $page);
                    var $rootTreeData = $rootPage.children("li").children(".tree-data");
                    if(defCfg.dataType == "group"){//常用组
                        $rootTreeData.removeClass("company-data").addClass("group-data");
                        mUtil.getLabel(81554, "常用组",function(msg){
                            $rootTreeData.html(msg);
                        });


                    }else{
                        var url = mUtil.jionActionUrl("com.api.mobilemode.web.mobile.browser.HrmBrowserAction", "action=getCompanyName");
                        mUtil.getJSON(url, function(result){
                            $rootTreeData.html(result["data"]);
                        });
                    }

                    that.build($rootPage);
                    $rootTreeData.triggerHandler("click");
                },
                build: function($wrap){
                    var that = this;
                    $(".tree-data[data-haschild='1']", $wrap).click(function(){
                        var $treeData = $(this), $icon = $treeData.children("i.wev-css-icon");
                        var expanding = $treeData.attr("expanding");
                        if(expanding == "1"){
                            return;
                        }
                        $treeData.attr("expanding", "1");

                        var $treePage = $treeData.siblings(".tree-page");

                        if($treeData.hasClass("closed")){
                            $treeData.removeClass("closed");
                            $treeData.addClass("opened");
                            $icon.removeClass("wev-plus-icon").addClass("wev-minus-icon");

                            if($treePage.length > 0){
                                $treePage.show();
                                $treeData.removeAttr("expanding");
                            }else{
                                //从服务端加载
                                var $treeLoading = $("<div class='wev-loading'><span>"+i18n.LOADING_DATA+"</span></div>");
                                $treeLoading.insertAfter($treeData);

                                var dataType = $treeData.attr("data-type");
                                var dataId = $treeData.attr("data-id");

                                //常用组处理
                                var actionName = defCfg.dataType == "group" ? "getGroupData" : "getTreeData";
                                var url = mUtil.jionActionUrl("com.api.mobilemode.web.mobile.browser.HrmBrowserAction", "action="+actionName+"&type="+dataType+"&pid="+dataId);

                                mUtil.getJSON(url, function(data){
                                    $treeLoading.remove();

                                    var status = data["status"];
                                    if(status == "1"){
                                        var datas = data["datas"];
                                        that.fillDatasToPage(datas, $treeData);
                                    }else{
                                        var errMsg = data["errMsg"];
                                        mUtil.getLabel(5359, "加载数据时出现错误：",function(msg){
                                            alert(msg + errMsg);
                                        });

                                        $treeData.removeClass("opened").addClass("closed");
                                    }

                                    $treeData.removeAttr("expanding");
                                });
                            }
                        }else if($treeData.hasClass("opened")){
                            $treePage.hide();
                            $treeData.removeClass("opened").addClass("closed").removeAttr("expanding");
                            $icon.addClass("wev-plus-icon").removeClass("wev-minus-icon");
                        }

                    });
                },
                fillDatasToPage: function(datas, $obj){
                    var that = this;
                    var $treePage = $("<ul class=\"tree-page\"></ul>");
                    $treePage.insertAfter($obj);
                    for(var i = 0; i < datas.length; i++){
                        var data = datas[i];
                        var id = data["id"];	//id
                        var name = data["name"] = data["name"].replaceAll(/(desensitization____random__([0-9a-zA-Z]{32}))/, '<span class="wev-encrypted-data" data-encrypted="$2"></span>');	//名称
                        var type = data["type"];	//类型

                        if(type == "4"){	//人员
                            var templateHtml = [
                                '<li class="wev-table-view-cell wev-media hrm ${id|hrmbrowser_tree_selected}" data-id="${id}" data-lastname="${name}" data-fontcolor = "${avatar.fontColor}" data-background = "${avatar.background}" data-headformat = "${avatar.headFormat}" data-messagerurl="${avatar.messagerUrls}" data-shortname="${avatar.shortname}">',
                                '<a class="tree-data" href="javascript:void(0);">',
                                '<i class="wev-css-icon '+(isMulti ? 'wev-multi-check' : 'wev-single-check') + '"></i>',
                                '<div class="wev-media-object wev-pull-left">',
                                '{@if avatar.headFormat == "1" || avatar.headFormat == "3"}',
                                '<div style="background:${avatar.background};color:${avatar.fontColor}">${avatar.shortname}</div>',
                                '{@else}',
                                '<img src="${avatar.messagerUrls}">',
                                '{@/if}',
                                '</div>',
                                '<div class="wev-media-body">',
                                '$${name}',
                                '</div>',
                                '</a>',
                                '</li>',
                            ].join('');

                            var html = mUtil.parseTemplate(templateHtml, data);
                            var $li = $(html);
                            $treePage.append($li);
                            $li.click(function(e){
                                if ($(e.target).hasClass("wev-encrypted-data")) return;
                                if(isMulti){	//多选
                                    _result[$(this).hasClass("selected") ? "remove" : "add"](this.dataset);
                                }else{	//单选
                                    _result.onOk($(this).data("id"), $(this).data("lastname"));
                                }
                            });
                            if(isMulti){
                                var $dept = defCfg.dataType == "group" ? $treePage.prev(".tree-data[data-type='2']") : $treePage.prev(".tree-data[data-type='3']");

                                if($dept.length > 0){
                                    var $selectAll = $dept.children(".select-all");
                                    $dept.children("span").addClass("select-control");
                                    if($selectAll.length == 0){
                                        $selectAll = $("<div class='select-all'><i class=\"wev-css-icon wev-multi-check\"></i>全选</div>");
                                        $dept.append($selectAll);
                                        $selectAll.click(function(e){
                                            var $sa = $(this);
                                            $sa.toggleClass("selected");

                                            var hrmArr = $(".wev-table-view-cell.hrm", $treePage);
                                            hrmArr.forEach(function(item, idx){
                                                var hasSelected = $(item).hasClass("selected");
                                                if($sa.hasClass("selected") === !hasSelected){
                                                    var moveToit = (++idx === hrmArr.length) ? true : false;
                                                    _result[hasSelected ? "remove" : "add"]($(item)[0].dataset, moveToit);
                                                }
                                            });

                                            e.stopPropagation();
                                        });
                                    }
                                }
                            }
                        }else{
                            var hasChild = data["hasChild"];	//是否有子节点
                            var hasChildFlag = hasChild ? "1" : "0";
                            var cssStr = hasChild ? "closed" : "";
                            var $li = $("<li><div class=\"tree-data "+cssStr+"\" data-id=\""+id+"\" data-type=\""+type+"\" data-haschild=\""+hasChildFlag+"\">"
                                + (hasChild ? '<i class="wev-css-icon wev-plus-icon"></i>' : '')
                                +"<span>"+name+"</span>"
                                +"</div></li>");
                            $treePage.append($li);
                        }

                    }
                    that.build($treePage);
                }
            };

            var _result = new hrmResult($page, defCfg.success, defCfg.closeCallback, defCfg._parentResult);

            var juicer = require("juicer");
            juicer.unregister('hrmbrowser_tree_selected');
            juicer.register('hrmbrowser_tree_selected', function(id){
                return _result.indexOfSelectedData(id) == -1 ? "" : "selected";
            });

            _tree.init();
            _result.init(defCfg.selectedIds);
            secondaryAuth.bindViewDataTrigger($page);
        }
    };
});
