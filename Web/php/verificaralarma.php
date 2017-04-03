<?php

#UNIVERSIDAD MAYOR DE SAN SIMON
#FACULTAD DE CIENCIAS Y TECNOLOGIA
#CARRERA DE INGENIERIA ELECTRONICA
#
#SISTEMA DOMOTICO MODULAR CENTRALIZADO DESARROLLADO POR:
#
#LARA TORRICO MARCOS
#TORREZ JORGE BRIAN

#Variable que establece la URL del servidor de la base de datos MongoDB
$basedatos=new MongoDB\Driver\Manager('mongodb://localhost:27017');
#Variable que establece las opciones de proyeccion dentro del formato de consulta
$opciones=['projection'=>['_id'=>0]];
#Variable que establece el tipo de consulta y sus parametros para el driver de MongoDB
$consulta=new MongoDB\Driver\Query([], $opciones);
#Variable que almacena el resultado de la consulta ejecutada en una determinada base de datos
$resultado=$basedatos->executeQuery("modulocoordinador.alarma", $consulta);
#Variable que apunta al primer resultado de la consulta a la base de datos
$resultadoconsulta=current($resultado->toArray());

$respuesta=null;

if($resultadoconsulta->encendida){
	$respuesta['activada']=true;
}
else{
	$respuesta['activada']=false;
}

if($resultadoconsulta->habilitado){
	$respuesta['habilitado']=true;
}
else{
	$respuesta['habilitado']=false;
}

echo json_encode($respuesta);

?>
