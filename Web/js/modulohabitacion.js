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

//Funcion que cierra la sesion del usuario
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

//Funcion que verifica si el usuario ha iniciado correctamente la sesion
//Caso contrario es redireccionado a la pagina de inicio de sesion
function verificarlogin(){
    $('#modulo1').hide();
    $('#modulo2').hide();
    $('#modulo3').hide();
    $('#mod1actuador1btn').prop('disabled', true);
    $('#mod1actuador2btn').prop('disabled', true);
    $('#mod1btnmanual').prop('disabled', true);
    $('#mod1btnsemi').prop('disabled', true);
    $('#mod1btnarmado').prop('disabled', true);
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

//Funcion que actualiza los datos de la interfaz del modulo con los datos mas
//recientes registrados por el sistema domotico a traves del modulo habitacion
function actualizardatos(){
    if (solicitud){
        solicitud.abort();
    }
    solicitud=$.ajax({
        url: "../php/actualizardatosmodhabitacion.php",
        type: "post",
        dataType: "json"
    });
    solicitud.done(function (respuesta, textStatus, jqXHR){
        $('#mod1temperatura').text(respuesta.temperatura+"°C");
        $('#mod1humedad').text(respuesta.humedad+"%");
        $('#mod1movimiento').text(respuesta.movimiento);
        $('#mod1magnetico').text(respuesta.magnetico);
        $('#mod1humo').text(respuesta.humo);
        $('#mod1actuador1').text(respuesta.actuador1);
        $('#mod1actuador2').text(respuesta.actuador2);
        if(respuesta.controles){
            if(respuesta.modonormal){
                $('#mod1actuador1btn').prop('disabled',false);
                $('#mod1actuador2btn').prop('disabled',false);
                $('#mod1btnmanual').text("ACTIVADO");
                $('#mod1btnsemi').text("ACTIVAR MODO");
                $('#mod1btnarmado').text("ACTIVAR MODO");
                $('#mod1btnmanual').prop('disabled',true);
                $('#mod1btnsemi').prop('disabled',false);
                $('#mod1btnarmado').prop('disabled',false);
            }
            else{
                if(respuesta.modosemi){
                    $('#mod1actuador1btn').prop('disabled',true);
                    $('#mod1actuador2btn').prop('disabled',true);
                    $('#mod1btnmanual').text("ACTIVAR MODO");
                    $('#mod1btnsemi').text("ACTIVADO");
                    $('#mod1btnarmado').text("ACTIVAR MODO");
                    $('#mod1btnmanual').prop('disabled',false);
                    $('#mod1btnsemi').prop('disabled',true);
                    $('#mod1btnarmado').prop('disabled',false);
                }
                else{
                    if(respuesta.modoarmado){
                        $('#mod1actuador1btn').prop('disabled', false);
                        $('#mod1actuador2btn').prop('disabled', false);
                        $('#mod1btnmanual').text("ACTIVAR MODO");
                        $('#mod1btnsemi').text("ACTIVAR MODO");
                        $('#mod1btnarmado').text("ACTIVADO");
                        $('#mod1btnmanual').prop('disabled',false);
                        $('#mod1btnsemi').prop('disabled',false);
                        $('#mod1btnarmado').prop('disabled',true);
                    }
                }
            }
        }
        else{
            $('#mod1actuador1btn').prop('disabled',true);
            $('#mod1actuador2btn').prop('disabled',true);
            $('#mod1btnmanual').prop('disabled', true);
            $('#mod1btnsemi').prop('disabled', true);
            $('#mod1btnarmado').prop('disabled', true);
        }
        temporizador=setTimeout(verificarmodulos,tiempoeventos);
    });
    solicitud.fail(function (jqXHR, textStatus, errorThrown){
        mostrarNotificacion("Ha ocurrido un error en la petición de actualización de datos al servidor");
        temporizador=setTimeout(verificarmodulos,tiempoeventos);
    });
}

//Funcion que verifica cuales de los modulos se encuentran conectados y remueve de la interfaz los que se encuentren
//sin conexion, si el modulo habitacion se desconectase el usuario es redireccionado a la pagina principal
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
            window.location.href="menuprincipal.html";
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
        actualizardatos();
    });
    solicitud.fail(function (jqXHR, textStatus, errorThrown){
        mostrarNotificacion("Ha ocurrido un error en la petición de verificar módulos al servidor");
        actualizardatos();
    });
}

//Funcion que realiza una peticion POST para modificar el estado del actuador 1 en el modulo habitacion
function modificaract1(){
    clearTimeout(temporizador);
    var datoestado=null;
    if (solicitud){
        solicitud.abort();
    }
    if(($("#mod1actuador1").text())=="OFF"){
        if(($("#mod1actuador2").text())=="OFF"){
            datoestado={"actuador1":"activar","actuador2":"desactivar"};
        }
        else{
            if(($("#mod1actuador2").text())=="ON"){
                datoestado={"actuador1":"activar","actuador2":"activar"};
            }
        }
    }
    else{
        if(($("#mod1actuador1").text())=="ON"){
            if(($("#mod1actuador2").text())=="OFF"){
                datoestado={"actuador1":"desactivar","actuador2":"desactivar"};
            }
            else{
                if(($("#mod1actuador2").text())=="ON"){
                    datoestado={"actuador1":"desactivar","actuador2":"activar"};
                }
            }
        }
    }
    var datosserializados=jQuery.param(datoestado);
    solicitud=$.ajax({
        url: "../php/modactmod1.php",
        type: "post",
        data: datosserializados
    });
    solicitud.done(function (respuesta, textStatus, jqXHR){
        var respuestajson=JSON.parse(respuesta);
        if(respuestajson.modificado){
            $('#mod1actuador1btn').prop('disabled', true);
            $('#mod1actuador2btn').prop('disabled', true);
            $('#mod1btnmanual').prop('disabled', true);
            $('#mod1btnsemi').prop('disabled', true);
            $('#mod1btnarmado').prop('disabled', true);
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

//Realiza una peticion POST para modificar el estado del actuador 2 en el modulo habitacion
function modificaract2(){
    clearTimeout(temporizador);
    var datoestado=null;
    if (solicitud){
        solicitud.abort();
    }
    if(($("#mod1actuador2").text())=="OFF"){
        if(($("#mod1actuador1").text())=="OFF"){
            datoestado={"actuador1":"desactivar","actuador2":"activar"};
        }
        else{
            if(($("#mod1actuador1").text())=="ON"){
                datoestado={"actuador1":"activar","actuador2":"activar"};
            }
        }
    }
    else{
        if(($("#mod1actuador2").text())=="ON"){
            if(($("#mod1actuador1").text())=="OFF"){
                datoestado={"actuador1":"desactivar","actuador2":"desactivar"};
            }
            else{
                if(($("#mod1actuador1").text())=="ON"){
                    datoestado={"actuador1":"activar","actuador2":"desactivar"};
                }
            }
        }
    }
    var datosserializados=jQuery.param(datoestado);
    solicitud=$.ajax({
        url: "../php/modactmod1.php",
        type: "post",
        data: datosserializados
    });
    solicitud.done(function (respuesta, textStatus, jqXHR){
        var respuestajson=JSON.parse(respuesta);
        if(respuestajson.modificado){
            $('#mod1actuador1btn').prop('disabled', true);
            $('#mod1actuador2btn').prop('disabled', true);
            $('#mod1btnmanual').prop('disabled', true);
            $('#mod1btnsemi').prop('disabled', true);
            $('#mod1btnarmado').prop('disabled', true);
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

//Realiza una peticion POST para que el modulo habitacion entre al modo de operacion manual
function modomanual(){
    clearTimeout(temporizador);
    var datoestado=null;
    if (solicitud){
        solicitud.abort();
    }
    datoestado={"modo":"normal"};
    var datosserializados=jQuery.param(datoestado);
    solicitud=$.ajax({
        url: "../php/modooperacionmod1.php",
        type: "post",
        data: datosserializados
    });
    solicitud.done(function (respuesta, textStatus, jqXHR){
        var respuestajson=JSON.parse(respuesta);
        if(respuestajson.modificado){
            $('#mod1actuador1btn').prop('disabled', true);
            $('#mod1actuador2btn').prop('disabled', true);
            $('#mod1btnmanual').prop('disabled', true);
            $('#mod1btnsemi').prop('disabled', true);
            $('#mod1btnarmado').prop('disabled', true);
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

//Realiza una peticion POST para que el modulo habitacion entre al modo de operacion semiautomatico
function modosemi(){
    clearTimeout(temporizador);
    var datoestado=null;
    if (solicitud){
        solicitud.abort();
    }
    datoestado={"modo":"semi"};
    var datosserializados=jQuery.param(datoestado);
    solicitud=$.ajax({
        url: "../php/modooperacionmod1.php",
        type: "post",
        data: datosserializados
    });
    solicitud.done(function (respuesta, textStatus, jqXHR){
        var respuestajson=JSON.parse(respuesta);
        if(respuestajson.modificado){
            $('#mod1actuador1btn').prop('disabled', true);
            $('#mod1actuador2btn').prop('disabled', true);
            $('#mod1btnmanual').prop('disabled', true);
            $('#mod1btnsemi').prop('disabled', true);
            $('#mod1btnarmado').prop('disabled', true);
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

//Realiza una peticion POST para que el modulo habitacion entre al modo de operacion armado
function modoarmado(){
    clearTimeout(temporizador);
    var datoestado=null;
    if (solicitud){
        solicitud.abort();
    }
    datoestado={"modo":"armado"};
    var datosserializados=jQuery.param(datoestado);
    solicitud=$.ajax({
        url: "../php/modooperacionmod1.php",
        type: "post",
        data: datosserializados
    });
    solicitud.done(function (respuesta, textStatus, jqXHR){
        var respuestajson=JSON.parse(respuesta);
        if(respuestajson.modificado){
            $('#mod1actuador1btn').prop('disabled', true);
            $('#mod1actuador2btn').prop('disabled', true);
            $('#mod1btnmanual').prop('disabled', true);
            $('#mod1btnsemi').prop('disabled', true);
            $('#mod1btnarmado').prop('disabled', true);
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
$("#cerrarsesion").click(cerrarsesion);
$("#mod1actuador1btn").click(modificaract1);
$("#mod1actuador2btn").click(modificaract2);
$("#mod1btnmanual").click(modomanual);
$("#mod1btnsemi").click(modosemi);
$("#mod1btnarmado").click(modoarmado);

//Ejecuta el script de verificar login inmediatamente despues de que se haya terminado de cargar la pagina
$(document).ready(verificarlogin);
