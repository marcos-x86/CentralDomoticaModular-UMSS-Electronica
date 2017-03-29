var solicitud;
var temporizador;
var tiempoeventos=2250;

function cerrarsesion(){
    clearTimeout(temporizador);
    if (solicitud){
        solicitud.abort();
    }
    var sesion={"cerrarsesionusuario":true};
    solicitud=$.ajax({
        url: "../php/validarlogout.php",
        type: "post",
        data: sesion,
        dataType: "json"
    });
    solicitud.done(function (respuesta, textStatus, jqXHR){
        if (respuesta.cerrado){
            window.location.href="../index.html" 
        }
        else{
            alert("Ha ocurrido un error en la petición de cerrar sesión al servidor");
        }
    });
    solicitud.fail(function (jqXHR, textStatus, errorThrown){
        alert("Ha ocurrido un error en la petición de cerrar sesión al servidor");
    });
}

function actualizarnotificaciones(){
    if (solicitud){
        solicitud.abort();
    }
    var datosnotificacion={"numeromostrar":parseInt($("#numeronotificaciones option:selected").text())};
    var datosserializados=jQuery.param(datosnotificacion);
    solicitud=$.ajax({
        url: "../php/notificaciones.php",
        type: "post",
        data: datosserializados
    });
    solicitud.done(function (respuesta, textStatus, jqXHR){
        $('#panelnotificaciones').html(respuesta);
        temporizador=setTimeout(verificarmodulos,tiempoeventos);
    });
    solicitud.fail(function (jqXHR, textStatus, errorThrown){
        alert("Ha ocurrido un error en la petición de actualizar notificaciones al servidor");
        temporizador=setTimeout(verificarmodulos,tiempoeventos);
    });
}

function verificarlogin(){
    $('#modulo1').hide();
    $('#modulo2').hide();
    $('#modulo3').hide();
    if (solicitud){
        solicitud.abort();
    }
    solicitud=$.ajax({
        url: "../php/verificarlogin.php",
        type: "post",
        dataType: "json"
    });
    solicitud.done(function (respuesta, textStatus, jqXHR){
        if (respuesta.autorizado){
            verificarmodulos();
        }
        else{
            window.location.href="../index.html";
        }
    });
    solicitud.fail(function (jqXHR, textStatus, errorThrown){
        alert("Ha ocurrido un error en la petición de verificar sesión al servidor");
    });
}

function verificarmodulos(){
    if (solicitud){
        solicitud.abort();
    }
    solicitud=$.ajax({
        url: "../php/verificarmodulos.php",
        type: "post",
        dataType: "json"
    });
    solicitud.done(function (respuesta, textStatus, jqXHR){
        if(respuesta.habitacion){
            $('#modulo1').show("fast");
        }
        else{
            $('#modulo1').hide("fast");
        }
        if(respuesta.cocina){
            $('#modulo2').show("fast");
        }
        else{
            $('#modulo2').hide("fast");
        }
        if(respuesta.exterior){
            $('#modulo3').show("fast");
        }
        else{
            $('#modulo3').hide("fast");
        }
        actualizarnotificaciones();
    });
    solicitud.fail(function (jqXHR, textStatus, errorThrown){
        alert("Ha ocurrido un error en la petición de verificar módulos al servidor");
        actualizarnotificaciones();
    });
}

$("#cerrarsesion").click(cerrarsesion);

$(document).ready(verificarlogin);