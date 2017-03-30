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
            mostrarNotificacion("Ha ocurrido un error en la respuesta de cerrar sesión al servidor");
        };
    });
    solicitud.fail(function (jqXHR, textStatus, errorThrown){
        mostrarNotificacion("Ha ocurrido un error en la petición de cerrar sesión al servidor");
    });
}


function verificarlogin(){
    $('#modulo1').hide();
    $('#modulo2').hide();
    $('#modulo3').hide();
    $('#mod2actuador1btn').prop('disabled', true);
    $('#mod2actuador2btn').prop('disabled', true);
    $('#mod2btnmanual').prop('disabled', true);
    $('#mod2btnsemi').prop('disabled', true);
    $('#mod2btnarmado').prop('disabled', true);
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
            temporizador=setTimeout(verificarmodulos,tiempoeventos);
        }
        else{
            window.location.href="../index.html";
        }
    });
    solicitud.fail(function (jqXHR, textStatus, errorThrown){
        mostrarNotificacion("Ha ocurrido un error en la petición de verificar sesión al servidor");
        window.location.href="../index.html";
    });
}

function actualizardatos(){
    if (solicitud){
        solicitud.abort();
    }
    solicitud=$.ajax({
        url: "../php/actualizardatosmodcocina.php",
        type: "post",
        dataType: "json"
    });
    solicitud.done(function (respuesta, textStatus, jqXHR){
        $('#mod2temperatura').text(respuesta.temperatura+"°C");
        $('#mod2humedad').text(respuesta.humedad+"%");
        $('#mod2gas').text(respuesta.gas);
        $('#mod2humo').text(respuesta.humo);
        $('#mod2fuego').text(respuesta.fuego);
        $('#mod2actuador1').text(respuesta.actuador1);
        $('#mod2actuador2').text(respuesta.actuador2);
        if(respuesta.controles){
            if(respuesta.modonormal){
                $('#mod2btnmanual').text("ACTIVADO");
                $('#mod2btnsemi').text("ACTIVAR MODO");
                $('#mod2btnarmado').text("ACTIVAR MODO");
                $('#mod2actuador1btn').prop('disabled', false);
                $('#mod2actuador2btn').prop('disabled', false);
                $('#mod2btnmanual').prop('disabled', true);
                $('#mod2btnsemi').prop('disabled', false);
                $('#mod2btnarmado').prop('disabled', false);
            }
            else{
                if(respuesta.modosemi){
                    $('#mod2btnmanual').text("ACTIVAR MODO");
                    $('#mod2btnsemi').text("ACTIVADO");
                    $('#mod2btnarmado').text("ACTIVAR MODO");
                    $('#mod2actuador1btn').prop('disabled', true);
                    $('#mod2actuador2btn').prop('disabled', true);
                    $('#mod2btnmanual').prop('disabled', false);
                    $('#mod2btnsemi').prop('disabled', true);
                    $('#mod2btnarmado').prop('disabled', false);
                }
                else{
                    if(respuesta.modoarmado){
                        $('#mod2btnmanual').text("ACTIVAR MODO");
                        $('#mod2btnsemi').text("ACTIVAR MODO");
                        $('#mod2btnarmado').text("ACTIVADO");
                        $('#mod2actuador1btn').prop('disabled', false);
                        $('#mod2actuador2btn').prop('disabled', false);
                        $('#mod2btnmanual').prop('disabled', false);
                        $('#mod2btnsemi').prop('disabled', false);
                        $('#mod2btnarmado').prop('disabled', true);
                    }
                }
            }
        }
        else{
            $('#mod2actuador1btn').prop('disabled', true);
            $('#mod2actuador2btn').prop('disabled', true);
            $('#mod2btnmanual').prop('disabled', true);
            $('#mod2btnsemi').prop('disabled', true);
            $('#mod2btnarmado').prop('disabled', true);
        }
        temporizador=setTimeout(verificarmodulos,tiempoeventos);
    });
    solicitud.fail(function (jqXHR, textStatus, errorThrown){
        mostrarNotificacion("Ha ocurrido un error en la petición de actualización de datos al servidor");
        temporizador=setTimeout(verificarmodulos,tiempoeventos);
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
            window.location.href="menuprincipal.html";
        }
        if(respuesta.exterior){
            $('#modulo3').show("fast");
        }
        else{
            $('#modulo3').hide("fast");
        }
        actualizardatos();
    });
    solicitud.fail(function (jqXHR, textStatus, errorThrown){
        mostrarNotificacion("Ha ocurrido un error en la petición de verificar módulos al servidor");
        actualizardatos();
    });
}

function modificaract1(){
    clearTimeout(temporizador);
    var datoestado=null;
    if (solicitud){
        solicitud.abort();
    }
    if(($("#mod2actuador1").text())=="OFF"){
        if(($("#mod2actuador2").text())=="OFF"){
            datoestado={"actuador1":"activar","actuador2":"desactivar"};
        }
        else{
            if(($("#mod2actuador2").text())=="ON"){
                datoestado={"actuador1":"activar","actuador2":"activar"};
            }
        }
    }
    else{
        if(($("#mod2actuador1").text())=="ON"){
            if(($("#mod2actuador2").text())=="OFF"){
                datoestado={"actuador1":"desactivar","actuador2":"desactivar"};
            }
            else{
                if(($("#mod2actuador2").text())=="ON"){
                    datoestado={"actuador1":"desactivar","actuador2":"activar"};
                }
            }
        }
    }
    var datosserializados=jQuery.param(datoestado);
    solicitud=$.ajax({
        url: "../php/modactmod2.php",
        type: "post",
        data: datosserializados
    });
    solicitud.done(function (respuesta, textStatus, jqXHR){
        var respuestajson=JSON.parse(respuesta);
        if(respuestajson.modificado){
            $('#mod2actuador1btn').prop('disabled', true);
            $('#mod2actuador2btn').prop('disabled', true);
            $('#mod2btnmanual').prop('disabled', true);
            $('#mod2btnsemi').prop('disabled', true);
            $('#mod2btnarmado').prop('disabled', true);
            temporizador=setTimeout(verificarmodulos,tiempoeventos);
        }
        else{
            mostrarNotificacion("Ha ocurrido un error en la respuesta de modificar el estado del actuador 1 al servidor");
            temporizador=setTimeout(verificarmodulos,tiempoeventos);
        }
    });
    solicitud.fail(function (jqXHR, textStatus, errorThrown){
        mostrarNotificacion("Ha ocurrido un error en la petición de modificar el estado del actuador 1 al servidor");
        temporizador=setTimeout(verificarmodulos,tiempoeventos);
    });
}

function modificaract2(){
    clearTimeout(temporizador);
    var datoestado=null;
    if (solicitud){
        solicitud.abort();
    }
    if(($("#mod2actuador2").text())=="OFF"){
        if(($("#mod2actuador1").text())=="OFF"){
            datoestado={"actuador1":"desactivar","actuador2":"activar"};
        }
        else{
            if(($("#mod2actuador1").text())=="ON"){
                datoestado={"actuador1":"activar","actuador2":"activar"};
            }
        }
    }
    else{
        if(($("#mod2actuador2").text())=="ON"){
            if(($("#mod2actuador1").text())=="OFF"){
                datoestado={"actuador1":"desactivar","actuador2":"desactivar"};
            }
            else{
                if(($("#mod2actuador1").text())=="ON"){
                    datoestado={"actuador1":"activar","actuador2":"desactivar"};
                }
            }
        }
    }
    var datosserializados=jQuery.param(datoestado);
    solicitud=$.ajax({
        url: "../php/modactmod2.php",
        type: "post",
        data: datosserializados
    });
    solicitud.done(function (respuesta, textStatus, jqXHR){
        var respuestajson=JSON.parse(respuesta);
        if(respuestajson.modificado){
            $('#mod2actuador1btn').prop('disabled', true);
            $('#mod2actuador2btn').prop('disabled', true);
            $('#mod2btnmanual').prop('disabled', true);
            $('#mod2btnsemi').prop('disabled', true);
            $('#mod2btnarmado').prop('disabled', true);
            temporizador=setTimeout(verificarmodulos,tiempoeventos);
        }
        else{
            mostrarNotificacion("Ha ocurrido un error en la respuesta de modificar el estado del actuador 2 al servidor");
            temporizador=setTimeout(verificarmodulos,tiempoeventos);
        }
    });
    solicitud.fail(function (jqXHR, textStatus, errorThrown){
        mostrarNotificacion("Ha ocurrido un error en la petición de modificar el estado del actuador 2 al servidor");
        temporizador=setTimeout(verificarmodulos,tiempoeventos);
    });
}

function modomanual(){
    clearTimeout(temporizador);
    var datoestado=null;
    if (solicitud){
        solicitud.abort();
    }
    datoestado={"modo":"normal"};
    var datosserializados=jQuery.param(datoestado);
    solicitud=$.ajax({
        url: "../php/modooperacionmod2.php",
        type: "post",
        data: datosserializados
    });
    solicitud.done(function (respuesta, textStatus, jqXHR){
        var respuestajson=JSON.parse(respuesta);
        if(respuestajson.modificado){
            $('#mod2actuador1btn').prop('disabled', true);
            $('#mod2actuador2btn').prop('disabled', true);
            $('#mod2btnmanual').prop('disabled', true);
            $('#mod2btnsemi').prop('disabled', true);
            $('#mod2btnarmado').prop('disabled', true);
            temporizador=setTimeout(verificarmodulos,tiempoeventos);
        }
        else{
            mostrarNotificacion("Ha ocurrido un error en la respuesta de modificar el modo de operación manual al servidor");
            temporizador=setTimeout(verificarmodulos,tiempoeventos);
        }
    });
    solicitud.fail(function (jqXHR, textStatus, errorThrown){
        mostrarNotificacion("Ha ocurrido un error en la petición de modificar el modo de operación manual al servidor");
        temporizador=setTimeout(verificarmodulos,tiempoeventos);
    });
}

function modosemi(){
    clearTimeout(temporizador);
    var datoestado=null;
    if (solicitud){
        solicitud.abort();
    }
    datoestado={"modo":"semi"};
    var datosserializados=jQuery.param(datoestado);
    solicitud=$.ajax({
        url: "../php/modooperacionmod2.php",
        type: "post",
        data: datosserializados
    });
    solicitud.done(function (respuesta, textStatus, jqXHR){
        var respuestajson=JSON.parse(respuesta);
        if(respuestajson.modificado){
            $('#mod2actuador1btn').prop('disabled', true);
            $('#mod2actuador2btn').prop('disabled', true);
            $('#mod2btnmanual').prop('disabled', true);
            $('#mod2btnsemi').prop('disabled', true);
            $('#mod2btnarmado').prop('disabled', true);
            temporizador=setTimeout(verificarmodulos,tiempoeventos);
        }
        else{
            mostrarNotificacion("Ha ocurrido un error en la respuesta de modificar el modo de operación semiautomático al servidor");
            temporizador=setTimeout(verificarmodulos,tiempoeventos);
        }
    });
    solicitud.fail(function (jqXHR, textStatus, errorThrown){
        mostrarNotificacion("Ha ocurrido un error en la petición de modificar el modo de operación semiautomático al servidor");
        temporizador=setTimeout(verificarmodulos,tiempoeventos);
    });
}

function modoarmado(){
    clearTimeout(temporizador);
    var datoestado=null;
    if (solicitud){
        solicitud.abort();
    }
    datoestado={"modo":"armado"};
    var datosserializados=jQuery.param(datoestado);
    solicitud=$.ajax({
        url: "../php/modooperacionmod2.php",
        type: "post",
        data: datosserializados
    });
    solicitud.done(function (respuesta, textStatus, jqXHR){
        var respuestajson=JSON.parse(respuesta);
        if(respuestajson.modificado){
            $('#mod2actuador1btn').prop('disabled', true);
            $('#mod2actuador2btn').prop('disabled', true);
            $('#mod2btnmanual').prop('disabled', true);
            $('#mod2btnsemi').prop('disabled', true);
            $('#mod2btnarmado').prop('disabled', true);
            temporizador=setTimeout(verificarmodulos,tiempoeventos);
        }
        else{
            mostrarNotificacion("Ha ocurrido un error en la respuesta de modificar el modo de operación armado al servidor");
            temporizador=setTimeout(verificarmodulos,tiempoeventos);
        }
    });
    solicitud.fail(function (jqXHR, textStatus, errorThrown){
        mostrarNotificacion("Ha ocurrido un error en la petición de modificar el modo de operación armado al servidor");
        temporizador=setTimeout(verificarmodulos,tiempoeventos);
    });
}

function mostrarNotificacion(texto){
    $.notify({
        icon: 'fa fa-ban',
        message: texto
        },{
            animate:{
                enter: 'animated fadeInRight',
                exit: 'animated fadeOutRight'
            },
            type: "danger",
            placement: {
                from: "bottom",
                align: "right"
            }
    });
}

$("#cerrarsesion").click(cerrarsesion);

$("#mod2actuador1btn").click(modificaract1);

$("#mod2actuador2btn").click(modificaract2);

$("#mod2btnmanual").click(modomanual);

$("#mod2btnsemi").click(modosemi);

$("#mod2btnarmado").click(modoarmado);

$(document).ready(verificarlogin);