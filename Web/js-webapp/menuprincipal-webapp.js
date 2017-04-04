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

//Funcion que actualiza las ultimas notificaciones del sistema periodicamente
function actualizarnotificaciones(){
    if (solicitud){
        solicitud.abort();
    }
    solicitud=$.ajax({
        url: "../php/notificacionesrecientes.php",
        type: "post"
    });
    solicitud.done(function (respuesta, textStatus, jqXHR){
        $('#panelnotificaciones').html(respuesta);
        temporizador=setTimeout(actualizarnotificaciones,tiempoeventos);
    });
    solicitud.fail(function (jqXHR, textStatus, errorThrown){
        mostrarNotificacion("Ha ocurrido un error en la petición de actualizar notificaciones al servidor");
        temporizador=setTimeout(actualizarnotificaciones,tiempoeventos);
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

//Ejecuta el script de verificar login inmediatamente despues de que se haya terminado de cargar la pagina
$(document).ready(actualizarnotificaciones);
