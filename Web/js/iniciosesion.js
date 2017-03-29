var solicitud;

function enviarformulario(event){
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

$("#formulario").submit(enviarformulario);