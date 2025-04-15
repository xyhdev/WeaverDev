
fillDatasToPage: function(t, a) {
    var e = $('<ul class="tree-page"></ul>');
    e.insertAfter(a);
    for (var s = 0; s < t.length; s++) {
        var i = t[s]
            , l = i.id
            , r = i.name = i.name.replaceAll(/(desensitization____random__([0-9a-zA-Z]{32}))/, '<span class="wev-encrypted-data" data-encrypted="$2"></span>')
            , d = i.type;
        if ("4" == d) {
            var n = ['<li class="wev-table-view-cell wev-media hrm ${id|hrmbrowser_tree_selected}" data-id="${id}" data-lastname="${name}" data-fontcolor = "${avatar.fontColor}" data-background = "${avatar.background}" data-headformat = "${avatar.headFormat}" data-messagerurl="${avatar.messagerUrls}" data-shortname="${avatar.shortname}">', '<a class="tree-data" href="javascript:void(0);">', '<i class="wev-css-icon ' + (h ? "wev-multi-check" : "wev-single-check") + '"></i>', '<div class="wev-media-object wev-pull-left">', '{@if avatar.headFormat == "1" || avatar.headFormat == "3"}', '<div style="background:${avatar.background};color:${avatar.fontColor}">${avatar.shortname}</div>', "{@else}", '<img src="${avatar.messagerUrls}">', "{@/if}", "</div>", '<div class="wev-media-body">', "$${name}", "</div>", "</a>", "</li>"].join("")
                , c = w.parseTemplate(n, i)
                , o = $(c);
            if (e.append(o),
                o.click(function(t) {
                    $(t.target).hasClass("wev-encrypted-data") || (h ? g[$(this).hasClass("selected") ? "remove" : "add"](this.dataset) : g.onOk($(this).data("id"), $(this).data("lastname")))
                }),
                h) {
                var v = "group" == u.dataType ? e.prev(".tree-data[data-type='2']") : e.prev(".tree-data[data-type='3']");
                if (0 < v.length) {
                    var p = v.children(".select-all");
                    v.children("span").addClass("select-control"),
                    0 == p.length && (p = $("<div class='select-all'><i class=\"wev-css-icon wev-multi-check\"></i>全选</div>"),
                        v.append(p),
                        p.click(function(t) {
                            var i = $(this);
                            i.toggleClass("selected");
                            var l = $(".wev-table-view-cell.hrm", e);
                            l.forEach(function(t, a) {
                                var e = $(t).hasClass("selected");
                                if (i.hasClass("selected") === !e) {
                                    var s = ++a === l.length;
                                    g[e ? "remove" : "add"]($(t)[0].dataset, s)
                                }
                            }),
                                t.stopPropagation()
                        }))
                }
            }
        } else {
            var m = i.hasChild;
            o = $('<li><div class="tree-data ' + (m ? "closed" : "") + '" data-id="' + l + '" data-type="' + d + '" data-haschild="' + (m ? "1" : "0") + '">' + (m ? '<i class="wev-css-icon wev-plus-icon"></i>' : "") + "<span>" + r + "</span></div></li>");
            e.append(o)
        }
    }
    this.build(e)
}