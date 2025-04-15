define("mService/treebrowser", ["mUtil","i18n"], function(mUtil,i18n) {

    return {
        mounted: function($page, cfg){
            var defCfg = {
                browserId : "",
                browserName: "",
                selectedIds : "",	//选中的id，逗号分隔，如：1,2,3
                closeCallback : function(){mUtil.back();},
                success : function(){}
            };

            $.extend(defCfg, cfg);

            var isMulti = defCfg.browserId == "257" || defCfg.isMulti;
            $(".page-content", $page).addClass(isMulti ? "multi" : "single");

            var _tree = {
                init: function(){
                    var that = this;
                    var $rootPage = $(".root-page", $page);
                    var $rootTreeData = $rootPage.children("li").children(".tree-data");

                    var getTreeRootData = function(){
                        var conditionParams = $.extend({browserName: defCfg.browserName}, that.conditionParamsResult());
                        var url = mUtil.jionActionUrl("com.api.mobilemode.web.mobile.browser.TreeBrowserAction", "action=getTreeRootData");
                        mUtil.getJSON(url, conditionParams, function(result){
                            var data = result["data"];
                            $rootTreeData.html(data.name);

                            $rootTreeData.attr("data-type", defCfg.browserName);
                            $rootTreeData.on("click", function(){
                                var $treeData = $(this);
                                var expanding = $treeData.attr("expanding");
                                if(expanding == "1"){
                                    return;
                                }
                                $treeData.attr("expanding", "1");

                                var $treePage = $treeData.siblings(".tree-page");

                                if($treeData.hasClass("closed")){
                                    $treeData.removeClass("closed");
                                    $treeData.addClass("opened");
                                    if($treePage.length > 0){
                                        $treePage.show();
                                        $treeData.removeAttr("expanding");
                                    }else{

                                        //从服务端加载
                                        var $treeLoading = $("<div class='wev-loading'><span>"+i18n.LOADING_DATA+"</span></div>");
                                        $treeLoading.insertAfter($treeData);

                                        var dataType = $treeData.attr("data-type");
                                        var dataId = $treeData.attr("data-id");

                                        var url = mUtil.jionActionUrl("com.api.mobilemode.web.mobile.browser.TreeBrowserAction", "action=getTreeData&type="+dataType+"&pid="+dataId+"&selectedIds=" + defCfg.selectedIds);
                                        mUtil.getJSON(url, that.conditionParamsResult(), function(result){
                                            $treeLoading.remove();

                                            var status = result["status"];
                                            if(status == "1"){
                                                var data = result["data"];
                                                var datas = data["datas"];
                                                var isonlyleaf = data["isonlyleaf"];
                                                that.fillTreeDatasToPage(datas, $treeData, isonlyleaf);
                                                _result.init(data["sel_datas"]);
                                            }else{
                                                var errMsg = result["errMsg"];
                                                mUtil.getLabel(5359, "加载数据时出现错误:",function(msg){
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
                                }
                            }).triggerHandler("click");
                        });
                    }
                    var browserInstance = mUtil.getInstance(defCfg.browserMecid);
                    if(browserInstance && !browserInstance.viewModel.browser.conditionParams){
                        var url = mUtil.jionActionUrl("com.api.mobilemode.web.mobile.browser.TreeBrowserAction", "action=getTreeConditionParams");
                        mUtil.getJSON(url, {treeIds: defCfg.browserName}, function(result){
                            browserInstance.viewModel.browser.conditionParams = result.data || [];
                            getTreeRootData();
                        });
                    }else{
                        getTreeRootData();
                    }
                },
                build: function($wrap){
                    var that = this;
                    $(".expend[data-haschild='1']", $wrap).click(function(e){
                        var $expend = $(this), $icon = $expend.children("i.wev-css-icon");
                        var $treeData = $(this).parent();
                        var expanding = $expend.attr("expanding");
                        if(expanding == "1"){
                            return;
                        }
                        $expend.attr("expanding", "1");

                        var $treePage = $treeData.siblings(".tree-page");

                        if($expend.hasClass("closed")){
                            $expend.removeClass("closed");
                            $expend.addClass("opened");
                            $icon.removeClass("wev-plus-icon").addClass("wev-minus-icon");

                            if($treePage.length > 0){
                                $treePage.show();
                                $expend.removeAttr("expanding");
                            }else{
                                //从服务端加载
                                var $treeLoading = $("<div class='wev-loading'><span>"+i18n.LOADING_DATA+"</span></div>");
                                $treeLoading.insertAfter($treeData);

                                var dataType = $treeData.attr("data-type");
                                var dataId = $treeData.attr("data-id");
                                var url = mUtil.jionActionUrl("com.api.mobilemode.web.mobile.browser.TreeBrowserAction", "action=getTreeData&type="+dataType+"&pid="+dataId);
                                mUtil.getJSON(url, that.conditionParamsResult(), function(result){
                                    $treeLoading.remove();

                                    var status = result["status"];
                                    if(status == "1"){
                                        var data = result["data"];
                                        var datas = data["datas"];
                                        var isonlyleaf = data["isonlyleaf"];
                                        that.fillTreeDatasToPage(datas, $treeData,isonlyleaf);
                                    }else{
                                        var errMsg = result["errMsg"];
                                        mUtil.getLabel(5359, "加载数据时出现错误:",function(msg){
                                            alert(msg + errMsg);
                                        });

                                        $expend.removeClass("opened").addClass("closed");
                                    }

                                    $expend.removeAttr("expanding");
                                });
                            }
                        }else if($expend.hasClass("opened")){
                            $treePage.hide();
                            $expend.removeClass("opened").addClass("closed").removeAttr("expanding");
                            $icon.addClass("wev-plus-icon").removeClass("wev-minus-icon");
                        }
                        e.stopPropagation();
                    });
                },
                fillTreeDatasToPage: function(datas, $obj, isonlyleaf){
                    var that = this;
                    var $treePage = $("<ul class=\"tree-page\"></ul>");
                    $treePage.insertAfter($obj);
                    for(var i = 0; i < datas.length; i++){
                        var data = datas[i];
                        var id = data["id"];	//id
                        var name = data["name"];	//名称
                        var type = data["type"];	//类型
                        var hasChild = data["hasChild"];	//是否有子节点

                        var isData = true;
                        var liClass = "data";
                        if(isonlyleaf == "1" && hasChild){
                            liClass = "";
                            isData = false;
                        }
                        liClass += _result.indexOfSelectedData(id) == -1 ? "" : " selected";
                        var $li = $("<li class=\""+liClass+"\" data-id=\""+id+"\" data-name=\""+name+"\"></li>");
                        var liHtml = "<div class=\"tree-data\" data-id=\""+id+"\" data-type=\""+type+"\">";
                        if(isData){
                            liHtml += '<i class="wev-css-icon '+(isMulti ? 'wev-multi-check' : 'wev-single-check') + '"></i>';
                        }
                        if(hasChild){
                            liHtml += "<div class=\"expend closed\" data-haschild=\"1\"><i class=\"wev-css-icon wev-plus-icon\"></i></div>";
                        }
                        liHtml += name;
                        liHtml += "</div>";

                        $li.append(liHtml);
                        $treePage.append($li);
                        if(isData){
                            $li.click(function(e){
                                if(isMulti){	//多选
                                    _result[$(this).hasClass("selected") ? "remove" : "add"](this.dataset);
                                }else{	//单选
                                    _result.onOk($(this).data("id"), $(this).data("name"));
                                }
                                e.stopPropagation();
                            });
                        }
                    }
                    that.build($treePage);
                },
                conditionParamsResult: function(){
                    var resultParams = {};
                    var browserInstance = mUtil.getInstance(defCfg.browserMecid);
                    return resultParams;
                }
            };

            var $okBtn = $(".ok-btn", $page);
            var $resultBtn = $(".result-btn", $page);

            var _result = {
                isReady: false,
                selectedData: [],
                addSelectedData : function(d){
                    this.selectedData.push(d);
                },
                indexOfSelectedData : function(id){
                    var index = -1;
                    for(var i = 0; i < this.selectedData.length; i++){
                        if(this.selectedData[i].id == id){
                            index = i;
                            break;
                        }
                    }
                    return index;
                },
                removeSelectedData : function(id){
                    var dIndex = this.indexOfSelectedData(id);
                    if(dIndex != -1){
                        this.selectedData.splice(dIndex, 1);
                    }
                },
                getSelectedNum : function(){
                    return this.selectedData.length;
                },
                init: function(datas){
                    var that = this;
                    $.each(datas, function(i, d){
                        that.innerAdd(d);
                    });
                    $okBtn.click(function(){
                        var id = "";
                        var text = "";
                        var datas = that.selectedData;
                        for(var i = 0; i < datas.length; i++){
                            id += datas[i].id;
                            text += datas[i].name;
                            if(i != (datas.length - 1)){
                                id += ",";
                                text += ",";
                            }
                        }
                        that.onOk(id, text);
                    });
                    $resultBtn.click(function(){
                        _selected.toggle();
                    });
                    that.isReady = true;
                },
                add: function(data){
                    var that = this;
                    if(!that.isReady){
                        return;
                    }

                    that.innerAdd(data);
                },
                innerAdd: function(data){
                    var that = this;
                    that.addSelectedData(data);

                    var id = data["id"];
                    $("li.data[data-id='"+id+"']", $page).addClass("selected");
                    that.refreshResultBtn();
                },
                remove: function(data){
                    var that = this;

                    var id = data["id"];
                    $("li.data[data-id='"+id+"']", $page).removeClass("selected");

                    that.removeSelectedData(id);
                    that.refreshResultBtn();
                },
                refreshResultBtn: function(){
                    var that = this;
                    var n = that.getSelectedNum();
                    $("span", $resultBtn).html(n > 0 ? "("+n+")" : "");
                    n > 0 ? $resultBtn.show() : $resultBtn.hide();
                },
                onOk: function(id, text){
                    var callback = defCfg.success;
                    callback && callback(id, text);
                    var closeCallback = defCfg.closeCallback;
                    closeCallback && closeCallback();
                }
            };

            var $selectedContainer = $(".selected-container", $page),
                $selectedMark = $(".selected-mark", $selectedContainer),
                $selectedContent = $(".selected-content", $selectedContainer),
                $list2 = $(".list-wrap .wev-table-view", $selectedContainer),
                $selectAllBtn = $(".select-all-btn", $selectedContainer),
                $delBtn = $(".del-btn", $selectedContainer);

            var _selected = {
                init: function(){
                    var that = this;
                    $selectedMark.click(function(){
                        that.hide();
                    });
                    $selectAllBtn.click(function(){
                        $(".wev-table-view-cell", $list2).not(".selected").triggerHandler("click");
                    });
                    $delBtn.click(function(){
                        $(".wev-table-view-cell.selected", $list2).each(function(){
                            _result.remove(this.dataset);
                            $(this).remove();
                        });
                        if(_result.getSelectedNum() <= 0){
                            that.hide();
                        }
                    });
                },
                showStatus: false,
                toggle: function(){
                    var that = this;
                    that[that.showStatus ? "hide" : "show"]();
                },
                show: function(){
                    var that = this;

                    var templateHtml = [
                        '{@each datas as d}',
                        '<li class="wev-table-view-cell wev-media" data-id="${d.id}" data-name="${d.name}">',
                        '<a href="javascript:void(0);">',
                        '<i class="wev-css-icon wev-multi-check"></i>',
                        '<div class="wev-media-body">',
                        '$${d.name}',
                        '</div>',
                        '</a>',
                        '</li>',
                        '{@/each}'
                    ].join('');

                    var html = mUtil.parseTemplate(templateHtml, {"datas" : _result.selectedData});
                    var $newList = $(html);
                    $list2.find("*").remove();
                    $list2.append($newList);

                    $newList.click(function(){
                        $(this).toggleClass("selected");
                    });

                    $selectedContainer.show();
                    setTimeout(function(){
                        $selectedMark.addClass("show");
                        $selectedContent.removeClass("hide");
                        that.showStatus = true;
                    }, 10);
                },
                hide: function(){
                    var that = this;
                    $selectedMark.removeClass("show");
                    $selectedContent.addClass("hide");
                    setTimeout(function(){
                        $selectedContainer.hide();
                        that.showStatus = false;
                    }, 300);
                }
            };

            _tree.init();
            _selected.init();
        }
    };
});
