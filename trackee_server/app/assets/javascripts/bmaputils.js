/* maputils.js */

var map = null;
var timer = null;
var myPoint=null;
var destPoint = null;
var myGeo = null;
var routety=0; // 步行，驾车，公交
$ = jQuery;

function initMap() {
    if(map)return;
    
    map = new BMap.Map("map_canvas", {enableHighResolution: true});
    map.enableInertialDragging(); //开启关系拖拽
    map.enableScrollWheelZoom();  //开启鼠标滚动缩放
    
    map.addControl(new BMap.MapTypeControl({mapTypes: [BMAP_NORMAL_MAP,BMAP_HYBRID_MAP]}));     //2D图，卫星图
    
//    map.addControl(new BMap.MapTypeControl({anchor: BMAP_ANCHOR_TOP_LEFT}));    //左上角，默认地图控件
    
    map.addControl(new BMap.NavigationControl());  //添加默认缩放平移控件
//    map.addControl(new BMap.NavigationControl({anchor: BMAP_ANCHOR_TOP_RIGHT, type: BMAP_NAVIGATION_CONTROL_SMALL}));  //右上角，仅包含平移和缩放按钮
//    map.addControl(new BMap.NavigationControl({anchor: BMAP_ANCHOR_BOTTOM_LEFT, type: BMAP_NAVIGATION_CONTROL_PAN}));  //左下角，仅包含平移按钮
//    map.addControl(new BMap.NavigationControl({anchor: BMAP_ANCHOR_BOTTOM_RIGHT, type: BMAP_NAVIGATION_CONTROL_ZOOM}));  //右下角，仅包含缩放按钮

    var myCity = new BMap.LocalCity();
    myCity.get(getCity);
    
//    map.centerAndZoom("深圳", 15);
    
    // update the navbar title using jQuery
//    $('#marker-nav .marker-title')
//    .html("...Navigator Bar Here...")
//    .removeClass('has-detail')
//    .unbind('click');
    
//    alert('initmap');
    
    if(window.location.pathname.search("\/d\/") != -1 )
    {
        var loc = (window.location.pathname.split('d/')[1]);
        if(loc != -1)
        {
            var x = loc.split(',')[0];
            var y = loc.split(',')[1];
            toDest(x,y);
        }
        
        bgLocationUpdate();

        return;
    }
    
    if (navigator.geolocation) {
        watchLocation();
    }
    else {
        alert("你的浏览器不支持定位，请使用chrome或QQ浏览器");
    }
}

function getCity(result){
    var cityName = result.name;
    map.setCenter(cityName);
}

function addTransRoute(){
//    alert('addTransRoute');
    var driving = new BMap.DrivingRoute(map, {renderOptions:{map: map, autoViewport: true}});
    driving.search(myPoint, destPoint);
//    addMarker(myPoint);
//    alert('get route');
}

//在后台获取当前位置
function bgLocationUpdate(){    
    // 添加定位控件
    var geoCtrl = new BMap.GeolocationControl({
                                              showAddressBar       : true //是否显示
                                              , enableAutoLocation : true //首次是否进行自动定位
                                              , offset             : new BMap.Size(0,25)
//                                              ,locationIcon        : new BMap.Icon("http://api.amap.com/webapi/static/Images/marker_sprite.png", new BMap.Size(23, 25))
                                              });
    
    //监听定位成功事件
    geoCtrl.addEventListener("locationSuccess", function(e){ myPoint = e.point;  addTransRoute(); } );
    
    // 将定位控件添加到地图
    map.addControl(geoCtrl);    
}

// 在地图上显示目的地址
function toDest(x, y){
    var lng = new Number(x)/10000;
    var lat = new Number(y)/10000;
//    alert(x + ' ' + y);
    destPoint = new BMap.Point(lng,lat);
    addMarker(destPoint);
    
//    map.centerAndZoom(dest, 15);    
}

function watchLocation() {
    var lo = new BMap.Geolocation();
    lo.getCurrentPosition( procRsl , {enableHighAccuracy: false, timeout:5000, maximumAge:0});
    
    // 添加定位控件
    var geoCtrl = new BMap.GeolocationControl({
                                              showAddressBar       : true //是否显示
                                              , enableAutoLocation : true //首次是否进行自动定位
                                              , offset             : new BMap.Size(0,25)
                                              ////        ,locationIcon        : new BMap.Icon("http://api.amap.com/webapi/static/Images/marker_sprite.png", new BMap.Size(23, 25))
                                              });
    
    //监听定位成功事件
    geoCtrl.addEventListener("locationSuccess", updateLoc);
    
    //监听定位失败事件
    geoCtrl.addEventListener("locationError", function(e){
                             alert('定位失败，请确认GPS处于打开状态'); });
    
    myGeo = new BMap.Geocoder();
    
//    peroid(geoCtrl);
    // 将定位控件添加到地图
    map.addControl(geoCtrl);    
}

function doPost(desc){
    var pathReg = new RegExp("\/(t|event)\/");
    var pathName = window.location.pathname.replace(pathReg, "");
    var track_event = {
    id:pathName,
    trackee_x:myPoint.lng,
    trackee_y:myPoint.lat,
    trackee_desc:desc
    };
    
    //Ajax post to server
    $.ajax({
           type: 'PUT',
           url: window.location.pathname,
           data: track_event,
           success: function() {
           return;
           },
           dataType: "json"
           });
    
}

function procRsl(e){
    addMarker(e.point);
    //      alert('accuracy : ' + e.accuracy + "meter.");
    
    myPoint = e.point;
    
    myGeo.getLocation(myPoint, function(result){
                      if (result){
                        doPost(result.address);
                        }
                      }); 
   }

function updateLoc(e) {
    addMarker(e.point);
    myPoint = e.point;
    
    myGeo.getLocation(myPoint, function(result){
                      if (result){
                      doPost(result.address);
                      }
                      });
}

function displayError(error) {
    var errorTypes = {
        0: "Unknown error",
        1: "Permission denied",
        2: "Position is not available",
        3: "Request timeout"
    };
    var errorMessage = errorTypes[error.code];
    if (error.code == 0 || error.code == 2) {
        errorMessage = errorMessage + " " + error.message;
    }
    alert(errorMessage);
}

function addMarker(pt) {
//    alert('add marker lng ' + pt.lng + ' lat ' + pt.lat);
    
    var marker = new BMap.Marker(pt);
    map.addOverlay(marker);
//          map.panTo(pt);
    map.centerAndZoom(pt, 16);
}

function peroid(geoCtrl){
    geoCtrl.location();
    clearTimeout(timer);
    timer = setTimeout(peroid, 5000);
}
