//UNIVERSIDAD MAYOR DE SAN SIMON
//FACULTAD DE CIENCIAS Y TECNOLOGIA
//CARRERA DE INGENIERIA ELECTRONICA
//
//SISTEMA DOMOTICO MODULAR CENTRALIZADO DESARROLLADO POR:
//
//LARA TORRICO MARCOS
//TORREZ JORGE BRIAN

//Variables globales del script
var solicitud; //Variable usada para manipular las solicitudes Ajax
var temporizador; //Variable empleada para programar eventos Jquery
var tiempoeventos=2250; //Tiempo de frecuencia para solicitudes Ajax (en milisegundos)

//Funcion que actualiza los datos de la interfaz del modulo con los datos mas
//recientes registrados por el sistema domotico a traves del modulo cocina
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
        temporizador=setTimeout(actualizardatos,tiempoeventos);
    });
    solicitud.fail(function (jqXHR, textStatus, errorThrown){
        mostrarNotificacion("Ha ocurrido un error en la petición de actualización de datos al servidor");
        temporizador=setTimeout(actualizardatos,tiempoeventos);
    });
}


//Funcion que realiza una peticion POST para modificar el estado del actuador 1 en el modulo cocina
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
            temporizador=setTimeout(actualizardatos,tiempoeventos);
        }
        else{
            mostrarNotificacion("Ha ocurrido un error en la respuesta de modificar el estado del actuador 1 al servidor");
            temporizador=setTimeout(actualizardatos,tiempoeventos);
        }
    });
    solicitud.fail(function (jqXHR, textStatus, errorThrown){
        mostrarNotificacion("Ha ocurrido un error en la petición de modificar el estado del actuador 1 al servidor");
        temporizador=setTimeout(actualizardatos,tiempoeventos);
    });
}

//Realiza una peticion POST para modificar el estado del actuador 2 en el modulo cocina
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
            temporizador=setTimeout(actualizardatos,tiempoeventos);
        }
        else{
            mostrarNotificacion("Ha ocurrido un error en la respuesta de modificar el estado del actuador 2 al servidor");
            temporizador=setTimeout(actualizardatos,tiempoeventos);
        }
    });
    solicitud.fail(function (jqXHR, textStatus, errorThrown){
        mostrarNotificacion("Ha ocurrido un error en la petición de modificar el estado del actuador 2 al servidor");
        temporizador=setTimeout(actualizardatos,tiempoeventos);
    });
}

//Realiza una peticion POST para que el modulo cocina entre al modo de operacion manual
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
            temporizador=setTimeout(actualizardatos,tiempoeventos);
        }
        else{
            mostrarNotificacion("Ha ocurrido un error en la respuesta de modificar el modo de operación manual al servidor");
            temporizador=setTimeout(actualizardatos,tiempoeventos);
        }
    });
    solicitud.fail(function (jqXHR, textStatus, errorThrown){
        mostrarNotificacion("Ha ocurrido un error en la petición de modificar el modo de operación manual al servidor");
        temporizador=setTimeout(actualizardatos,tiempoeventos);
    });
}

//Realiza una peticion POST para que el modulo cocina entre al modo de operacion semiautomatico
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
            temporizador=setTimeout(actualizardatos,tiempoeventos);
        }
        else{
            mostrarNotificacion("Ha ocurrido un error en la respuesta de modificar el modo de operación semiautomático al servidor");
            temporizador=setTimeout(actualizardatos,tiempoeventos);
        }
    });
    solicitud.fail(function (jqXHR, textStatus, errorThrown){
        mostrarNotificacion("Ha ocurrido un error en la petición de modificar el modo de operación semiautomático al servidor");
        temporizador=setTimeout(actualizardatos,tiempoeventos);
    });
}

//Realiza una peticion POST para que el modulo cocina entre al modo de operacion armado
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
            temporizador=setTimeout(actualizardatos,tiempoeventos);
        }
        else{
            mostrarNotificacion("Ha ocurrido un error en la respuesta de modificar el modo de operación armado al servidor");
            temporizador=setTimeout(actualizardatos,tiempoeventos);
        }
    });
    solicitud.fail(function (jqXHR, textStatus, errorThrown){
        mostrarNotificacion("Ha ocurrido un error en la petición de modificar el modo de operación armado al servidor");
        temporizador=setTimeout(actualizardatos,tiempoeventos);
    });
}

//Funcion que despliega notificaciones emergentes en la interfaz, recibe como parametro cualquier texto
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

//Se establecen acciones para los botones de la interfaz a traves de sus identificadores
//mediante sentencias JQuery, que se ejecutaran cuando ocurran eventos de click
$("#mod2actuador1btn").click(modificaract1);
$("#mod2actuador2btn").click(modificaract2);
$("#mod2btnmanual").click(modomanual);
$("#mod2btnsemi").click(modosemi);
$("#mod2btnarmado").click(modoarmado);

//Ejecuta el script de verificar login inmediatamente despues de que se haya terminado de cargar la pagina
$(document).ready(actualizardatos);