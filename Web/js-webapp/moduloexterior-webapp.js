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
//recientes registrados por el sistema domotico a traves del modulo exterior
function actualizardatos(){
    if (solicitud){
        solicitud.abort();
    }
    solicitud=$.ajax({
        url: "../php/actualizardatosexterior.php",
        type: "post",
        dataType: "json"
    });
    solicitud.done(function (respuesta, textStatus, jqXHR){
        $('#mod3lluvia').text(respuesta.lluvia);
        $('#mod3nivelagua').text(respuesta.nivelagua+"L");
        $('#mod3movimiento').text(respuesta.movimiento);
        $('#mod3luz').text(respuesta.luz+"lx");
        $('#mod3consumoelectrico').text(respuesta.consumoelectrico+"W");
        $('#mod3consumoagua').text(respuesta.consumoagua+"mL/s");
        $('#mod3actuador1').text(respuesta.actuador1);
        $('#mod3actuador2').text(respuesta.actuador2);
        if(respuesta.controles){
            if(respuesta.modonormal){
                $('#mod3btnmanual').text("ACTIVADO");
                $('#mod3btnsemi').text("ACTIVAR MODO");
                $('#mod3btnarmado').text("ACTIVAR MODO");
                $('#mod3actuador1btn').prop('disabled', false);
                $('#mod3actuador2btn').prop('disabled', false);
                $('#mod3btnmanual').prop('disabled', true);
                $('#mod3btnsemi').prop('disabled', false);
                $('#mod3btnarmado').prop('disabled', false);
            }
            else{
                if(respuesta.modosemi){
                    $('#mod3btnmanual').text("ACTIVAR MODO");
                    $('#mod3btnsemi').text("ACTIVADO");
                    $('#mod3btnarmado').text("ACTIVAR MODO");
                    $('#mod3actuador1btn').prop('disabled', true);
                    $('#mod3actuador2btn').prop('disabled', true);
                    $('#mod3btnmanual').prop('disabled', false);
                    $('#mod3btnsemi').prop('disabled', true);
                    $('#mod3btnarmado').prop('disabled', false);
                }
                else{
                    if(respuesta.modoarmado){
                        $('#mod3btnmanual').text("ACTIVAR MODO");
                        $('#mod3btnsemi').text("ACTIVAR MODO");
                        $('#mod3btnarmado').text("ACTIVADO");
                        $('#mod3actuador1btn').prop('disabled', false);
                        $('#mod3actuador2btn').prop('disabled', false);
                        $('#mod3btnmanual').prop('disabled', false);
                        $('#mod3btnsemi').prop('disabled', false);
                        $('#mod3btnarmado').prop('disabled', true);
                    }
                }
            }
        }
        else{
            $('#mod3actuador1btn').prop('disabled', true);
            $('#mod3actuador2btn').prop('disabled', true);
            $('#mod3btnmanual').prop('disabled', true);
            $('#mod3btnsemi').prop('disabled', true);
            $('#mod3btnarmado').prop('disabled', true);
        }
        temporizador=setTimeout(actualizardatos,tiempoeventos);
    });
    solicitud.fail(function (jqXHR, textStatus, errorThrown){
        mostrarNotificacion("Ha ocurrido un error en la petición de actualización de datos al servidor");
        temporizador=setTimeout(actualizardatos,tiempoeventos);
    });
}

//Funcion que realiza una peticion POST para modificar el estado del actuador 1 en el modulo exterior
function modificaract1(){
    clearTimeout(temporizador);
    var datoestado=null;
    if (solicitud){
        solicitud.abort();
    }
    if(($("#mod3actuador1").text())=="OFF"){
        if(($("#mod3actuador2").text())=="OFF"){
            datoestado={"actuador1":"activar","actuador2":"desactivar"};
        }
        else{
            if(($("#mod3actuador2").text())=="ON"){
                datoestado={"actuador1":"activar","actuador2":"activar"};
            }
        }
    }
    else{
        if(($("#mod3actuador1").text())=="ON"){
            if(($("#mod3actuador2").text())=="OFF"){
                datoestado={"actuador1":"desactivar","actuador2":"desactivar"};
            }
            else{
                if(($("#mod3actuador2").text())=="ON"){
                    datoestado={"actuador1":"desactivar","actuador2":"activar"};
                }
            }
        }
    }
    var datosserializados=jQuery.param(datoestado);
    solicitud=$.ajax({
        url: "../php/modactmod3.php",
        type: "post",
        data: datosserializados
    });
    solicitud.done(function (respuesta, textStatus, jqXHR){
        var respuestajson=JSON.parse(respuesta);
        if(respuestajson.modificado){
            $('#mod3actuador1btn').prop('disabled', true);
            $('#mod3actuador2btn').prop('disabled', true);
            $('#mod3btnmanual').prop('disabled', true);
            $('#mod3btnsemi').prop('disabled', true);
            $('#mod3btnarmado').prop('disabled', true);
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

//Realiza una peticion POST para modificar el estado del actuador 2 en el modulo exterior
function modificaract2(){
    clearTimeout(temporizador);
    var datoestado=null;
    if(($("#mod3actuador2").text())=="OFF"){
        if(($("#mod3actuador1").text())=="OFF"){
            datoestado={"actuador1":"desactivar","actuador2":"activar"};
        }
        else{
            if(($("#mod3actuador1").text())=="ON"){
                datoestado={"actuador1":"activar","actuador2":"activar"};
            }
        }
    }
    else{
        if(($("#mod3actuador2").text())=="ON"){
            if(($("#mod3actuador1").text())=="OFF"){
                datoestado={"actuador1":"desactivar","actuador2":"desactivar"};
            }
            else{
                if(($("#mod3actuador1").text())=="ON"){
                    datoestado={"actuador1":"activar","actuador2":"desactivar"};
                }
            }
        }
    }
    var datosserializados=jQuery.param(datoestado);
    solicitud=$.ajax({
        url: "../php/modactmod3.php",
        type: "post",
        data: datosserializados
    });
    solicitud.done(function (respuesta, textStatus, jqXHR){
        var respuestajson=JSON.parse(respuesta);
        if(respuestajson.modificado){
            $('#mod3actuador1btn').prop('disabled', true);
            $('#mod3actuador2btn').prop('disabled', true);
            $('#mod3btnmanual').prop('disabled', true);
            $('#mod3btnsemi').prop('disabled', true);
            $('#mod3btnarmado').prop('disabled', true);
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

//Realiza una peticion POST para que el modulo exterior entre al modo de operacion manual
function modomanual(){
    clearTimeout(temporizador);
    var datoestado=null;
    if (solicitud){
        solicitud.abort();
    }
    datoestado={"modo":"normal"};
    var datosserializados=jQuery.param(datoestado);
    solicitud=$.ajax({
        url: "../php/modooperacionmod3.php",
        type: "post",
        data: datosserializados
    });
    solicitud.done(function (respuesta, textStatus, jqXHR){
        var respuestajson=JSON.parse(respuesta);
        if(respuestajson.modificado){
            $('#mod3actuador1btn').prop('disabled', true);
            $('#mod3actuador2btn').prop('disabled', true);
            $('#mod3btnmanual').prop('disabled', true);
            $('#mod3btnsemi').prop('disabled', true);
            $('#mod3btnarmado').prop('disabled', true);
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

//Realiza una peticion POST para que el modulo exterior entre al modo de operacion semiautomatico
function modosemi(){
    clearTimeout(temporizador);
    var datoestado=null;
    if (solicitud){
        solicitud.abort();
    }
    datoestado={"modo":"semi"};
    var datosserializados=jQuery.param(datoestado);
    solicitud=$.ajax({
        url: "../php/modooperacionmod3.php",
        type: "post",
        data: datosserializados
    });
    solicitud.done(function (respuesta, textStatus, jqXHR){
        var respuestajson=JSON.parse(respuesta);
        if(respuestajson.modificado){
            $('#mod3actuador1btn').prop('disabled', true);
            $('#mod3actuador2btn').prop('disabled', true);
            $('#mod3btnmanual').prop('disabled', true);
            $('#mod3btnsemi').prop('disabled', true);
            $('#mod3btnarmado').prop('disabled', true);
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

//Realiza una peticion POST para que el modulo exterior entre al modo de operacion armado
function modoarmado(){
    clearTimeout(temporizador);
    var datoestado=null;
    if (solicitud){
        solicitud.abort();
    }
    datoestado={"modo":"armado"};
    var datosserializados=jQuery.param(datoestado);
    solicitud=$.ajax({
        url: "../php/modooperacionmod3.php",
        type: "post",
        data: datosserializados
    });
    solicitud.done(function (respuesta, textStatus, jqXHR){
        var respuestajson=JSON.parse(respuesta);
        if(respuestajson.modificado){
            $('#mod3actuador1btn').prop('disabled', true);
            $('#mod3actuador2btn').prop('disabled', true);
            $('#mod3btnmanual').prop('disabled', true);
            $('#mod3btnsemi').prop('disabled', true);
            $('#mod3btnarmado').prop('disabled', true);
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
$("#mod3actuador1btn").click(modificaract1);
$("#mod3actuador2btn").click(modificaract2);
$("#mod3btnmanual").click(modomanual);
$("#mod3btnsemi").click(modosemi);
$("#mod3btnarmado").click(modoarmado);

//Ejecuta el script de verificar login inmediatamente despues de que se haya terminado de cargar la pagina
$(document).ready(actualizardatos);
