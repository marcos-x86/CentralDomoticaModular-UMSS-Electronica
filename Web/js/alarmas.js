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
            alert("Ha ocurrido un error en la petición de cerrar sesión al servidor");
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
        mostrarNotificacion("Ha ocurrido un error en la petición de verificar sesión al servidor");
        window.location.href="../index.html";
    });
}

//Funcion que es ejecutada ciclicamente, verifica el estado de la alarma
//y establece los elementos en la interfaz de acuerdo a la respuesta de la verificacion
function verificaralarma(){
    if (solicitud){
        solicitud.abort();
    }
    solicitud=$.ajax({
        url: "../php/verificaralarma.php",
        type: "post",
        dataType: "json"
    });
    solicitud.done(function (respuesta, textStatus, jqXHR){
        if(respuesta.habilitado){
            if(respuesta.activada){
                $('#alarmaboton').prop('disabled',false);
                $('#alarmaboton').text("DESACTIVAR");
            }
            else{
                $('#alarmaboton').prop('disabled',false);
                $('#alarmaboton').text("ACTIVAR");
            }
        }
        else{
            if(respuesta.activada){
                $('#alarmaboton').prop('disabled',true);
                $('#alarmaboton').text("DESACTIVAR");
            }
            else{
                $('#alarmaboton').prop('disabled',true);
                $('#alarmaboton').text("ACTIVAR");
            }
        }
        temporizador=setTimeout(verificarmodulos,tiempoeventos);
    });
    solicitud.fail(function (jqXHR, textStatus, errorThrown){
        mostrarNotificacion("Ha ocurrido un error en la petición de verificar alarma al servidor");
        temporizador=setTimeout(verificarmodulos,tiempoeventos);
    });
}

//Funcion que verifica cuales de los modulos se encuentran conectados y remueve de la interfaz los que se encuentren
//sin conexion
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
        verificaralarma();
    });
    solicitud.fail(function (jqXHR, textStatus, errorThrown){
        mostrarNotificacion("Ha ocurrido un error en la petición de verificar módulos al servidor");
        verificaralarma();
    });
}

//Funcion que se encarga de conmutar el estado de activacion de la alarma
//a traves de una peticion POST al servidor
function modificaralarma(){
    clearTimeout(temporizador);
    var datoestado=null;
    if (solicitud){
        solicitud.abort();
    }
    if(($("#alarmaboton").text())=="ACTIVAR"){
        datoestado={"estado":"activar"};
    }
    else{
        if(($("#alarmaboton").text())=="DESACTIVAR"){
            datoestado={"estado":"desactivar"};
        }
    }
    var datosserializados=jQuery.param(datoestado);
    solicitud=$.ajax({
        url: "../php/estadoalarma.php",
        type: "post",
        data: datosserializados
    });
    solicitud.done(function (respuesta, textStatus, jqXHR){
        var respuestajson=JSON.parse(respuesta);
        if(respuestajson.confirmacion){
            $('#alarmaboton').prop('disabled', true);
            temporizador=setTimeout(verificarmodulos,tiempoeventos);
        }
        else{
            mostrarNotificacion("Ha ocurrido un error en la respuesta de modificar alarma al servidor");
            temporizador=setTimeout(verificarmodulos,tiempoeventos);
        }
    });
    solicitud.fail(function (jqXHR, textStatus, errorThrown){
        mostrarNotificacion("Ha ocurrido un error en la petición de modificar alarma al servidor");
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
$("#alarmaboton").click(modificaralarma);

//Ejecuta el script de verificar login inmediatamente despues de que se haya terminado de cargar la pagina
$(document).ready(verificarlogin);
