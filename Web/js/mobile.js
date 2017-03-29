function identificarmobile() {
    if ((/Android|webOS|iPhone|iPad|iPod|BlackBerry|BB|mobile.+firefox|PlayBook|IEMobile|Windows Phone|Kindle|Silk|Opera Mini/i.test(navigator.userAgent))) {
        document.getElementById('idcamara01').href = "camaramobile01.html";
        document.getElementById('idcamara02').href = "camaramobile02.html";
    }         
}
window.onload=identificarmobile;