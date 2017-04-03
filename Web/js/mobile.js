//UNIVERSIDAD MAYOR DE SAN SIMON
//FACULTAD DE CIENCIAS Y TECNOLOGIA
//CARRERA DE INGENIERIA ELECTRONICA
//
//SISTEMA DOMOTICO MODULAR CENTRALIZADO DESARROLLADO POR:
//
//LARA TORRICO MARCOS
//TORREZ JORGE BRIAN

//Funcion que identifica el agente de usuario actual del navegador web del usuario
function identificarmobile() {
    if ((/Android|webOS|iPhone|iPad|iPod|BlackBerry|BB|mobile.+firefox|PlayBook|IEMobile|Windows Phone|Kindle|Silk|Opera Mini/i.test(navigator.userAgent))) {
        document.getElementById('idcamara01').href = "camaramobile01.html";
        document.getElementById('idcamara02').href = "camaramobile02.html";
    }
}

//Instruccion que ejecuta la funcion identificarmobile en cuanto se termine
//la carga de la pagina web
window.onload=identificarmobile;
