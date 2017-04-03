<?php

#UNIVERSIDAD MAYOR DE SAN SIMON
#FACULTAD DE CIENCIAS Y TECNOLOGIA
#CARRERA DE INGENIERIA ELECTRONICA
#
#SISTEMA DOMOTICO MODULAR CENTRALIZADO DESARROLLADO POR:
#
#LARA TORRICO MARCOS
#TORREZ JORGE BRIAN

#Variable que almacena el contenido de los datos de la solicitud POST
$numeromuestra=isset($_POST['numeromostrar']) ? $_POST['numeromostrar'] : null;

#Procedimiento que inserta codigo HTML en el documento de notificaciones
#mostrando las ultimas notificaciones generadas por el sistema domotico
if($numeromuestra!=null){
  #Variable que establece la URL del servidor de la base de datos MongoDB
  $basedatos=new MongoDB\Driver\Manager('mongodb://localhost:27017');
  #Variable que establece los parametros de la consulda a la base de datos
  $opciones=['sort' => ['fecha' => -1], 'limit' => $numeromuestra];
  #Variable que establece el tipo de consulta y sus parametros para el driver de MongoDB
  $consulta=new MongoDB\Driver\Query([], $opciones);
  #Variable que almacena el resultado de la consulta ejecutada en una determinada base de datos
  $resultado=$basedatos->executeQuery("notificaciones.notificacionesemergentes", $consulta);
  #Procedimiento que inserta el codigo HTML de acuerdo a los resultados de la consulta
  foreach ($resultado as $documento){
  	if($documento->tipo=="peligro"){
  		echo '<div class="alert alert-danger">';
  	}
  	elseif($documento->tipo=="info"){
  		echo '<div class="alert alert-success">';
  	}
  	else{
  		echo '<div class="alert alert-warning">';
  	}
  	echo $documento->fecha->toDateTime()->setTimezone(new DateTimeZone('America/La_Paz'))->format('d/m/Y H:i:s');
    echo '</br>';
  	echo $documento->mensaje;
  	echo '</div>';
  }
}
else{
  echo '';
}

?>
