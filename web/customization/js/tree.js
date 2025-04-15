//tree
fillTreeDatasToPage: function(e, t, a) {
    var s = $('<ul class="tree-page"></ul>');
    s.insertAfter(t);
    for (var i = 0; i < e.length; i++) {
        var n = e[i]
            , d = n.id
            , l = n.name
            , o = n.type
            , r = n.hasChild
            , c = !0
            , v = "data";
        "1" == a && r && (v = "",
            c = !1),
            v += -1 == f.indexOfSelectedData(d) ? "" : " selected";
        var h = $('<li class="' + v + '" data-id="' + d + '" data-name="' + l + '"></li>')
            , u = '<div class="tree-data" data-id="' + d + '" data-type="' + o + '">';
        c && (u += '<i class="wev-css-icon ' + (m ? "wev-multi-check" : "wev-single-check") + '"></i>'),
        r && (u += '<div class="expend closed" data-haschild="1"><i class="wev-css-icon wev-plus-icon"></i></div>'),
            u += l,
            u += "</div>",
            h.append(u),
            s.append(h),
        c && h.click(function(e) {
            m ? f[$(this).hasClass("selected") ? "remove" : "add"](this.dataset) : f.onOk($(this).data("id"), $(this).data("name")),
                e.stopPropagation()
        })
    }
    this.build(s)
},