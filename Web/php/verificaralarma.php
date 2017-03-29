<?php

$basedatos=new MongoDB\Driver\Manager('mongodb://localhost:27017');
$opciones=['projection'=>['_id'=>0]];
$consulta=new MongoDB\Driver\Query([], $opciones);
$resultado=$basedatos->executeQuery("modulocoordinador.alarma", $consulta);
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
