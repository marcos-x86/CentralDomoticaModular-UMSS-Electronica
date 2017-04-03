//UNIVERSIDAD MAYOR DE SAN SIMON
//FACULTAD DE CIENCIAS Y TECNOLOGIA
//CARRERA DE INGENIERIA ELECTRONICA
//
//SISTEMA DOMOTICO MODULAR CENTRALIZADO DESARROLLADO POR:
//
//LARA TORRICO MARCOS
//TORREZ JORGE BRIAN

//Variable global del script
var solicitud; //Variable usada para manipular las solicitudes Ajax

//Funcion que envia los datos de usuario y contrasena al servidor
//a traves de una peticion POST para ser validados. De ser afirmativa
//la respuesta se redirige al usuario a la pagina principal
//De ser negativa la respuesta se informa al usuario de introducir un
//nombre de usuario y contrasena validos
function enviardatoslogin(event){
    event.preventDefault();
    event.preventDefault();
    if (solicitud) {
        solicitud.abort();
    }
    var $form=$(this);
    var $entradas=$form.find("input");
    var datosserializados=$form.serialize();
    $entradas.prop("disabled", true);
    solicitud=$.ajax({
        url: "/php/validarlogin.php",
        type: "post",
        data: datosserializados
    });
    solicitud.done(function (respuesta, textStatus, jqXHR){
        var respuestajson=JSON.parse(respuesta);
        if (respuestajson.autorizado){
            window.location.href = "pages/menuprincipal.html"
        }
        else{
            $("#mensaje-servidor").html("Usuario y/o contraseña incorrecta");
        }
    });
    solicitud.fail(function (jqXHR, textStatus, errorThrown){
        $("#mensaje-servidor").html("Ha ocurrido un error en la petición al servidor");
    });
    solicitud.always(function () {
        $entradas.prop("disabled", false);
    });
}

//Se establecen acciones para los botones de la interfaz a traves de sus identificadores
//mediante sentencias JQuery, que se ejecutaran cuando ocurran eventos de click
$("#formulario").submit(enviardatoslogin);
