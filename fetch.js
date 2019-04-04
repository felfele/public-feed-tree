var myn = n;
var ehandler = this;
var blogArray = [];
ehandler.myGetMore = function(offset, limit) {
    ehandler.set("loading", !0),
        ehandler.set('nextOffset', offset);
    ehandler.set('limit', limit);

    ehandler._xhr = myn.ajax({
        type: "GET",
        url: "/svc/discover/blogs" + window.location.search,
        data: {
            offset: ehandler.get("nextOffset"),
            limit: ehandler.get("limit"),
            type: "staff-picks"//ehandler.get("type")
        }
    }).done(function(e){
        if (
            e && e.response && e.response.DiscoveryBlogs) {
            var t = e.response.DiscoveryBlogs;
            blogArray.push(t.blogs)
            console.log(t.blogs);
        }
        ehandler.set("loading", !1)
        var offset = ehandler.get("nextOffset") + 10;
        if (offset < 1000) {
            ehandler.myGetMore(offset, 10);
        }else{
            console.log(blogArray);
        }
    })

};


ehandler.myGetMore(0,10);

ehandler.getMore = function() {
    ehandler.myGetMore(0,10)
};
