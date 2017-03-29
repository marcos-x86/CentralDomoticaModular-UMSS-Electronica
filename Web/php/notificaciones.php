<?php

$numeromuestra=isset($_POST['numeromostrar']) ? $_POST['numeromostrar'] : null;

if($numeromuestra!=null){
  $basedatos=new MongoDB\Driver\Manager('mongodb://localhost:27017');
  $opciones=['sort' => ['fecha' => -1], 'limit' => $numeromuestra];
  $consulta=new MongoDB\Driver\Query([], $opciones);
  $resultado=$basedatos->executeQuery("notificaciones.notificacionesemergentes", $consulta);
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
