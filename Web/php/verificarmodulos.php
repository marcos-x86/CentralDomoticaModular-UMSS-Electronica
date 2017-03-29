<?php

$basedatos=new MongoDB\Driver\Manager('mongodb://localhost:27017');
$opciones=['projection'=>['_id'=>0]];
$consulta=new MongoDB\Driver\Query([], $opciones);
$resultado=$basedatos->executeQuery("modulosactivados.registro", $consulta);
$resultadoconsulta=current($resultado->toArray());
$respuesta=null;

if($resultadoconsulta->modulohabitacion){
  $respuesta['habitacion']=true;
}
else{
  $respuesta['habitacion']=false;
}

if($resultadoconsulta->modulococina){
  $respuesta['cocina']=true;
}
else{
  $respuesta['cocina']=false;
}

if($resultadoconsulta->moduloexterior){
  $respuesta['exterior']=true;
}
else{
  $respuesta['exterior']=false;
}

echo json_encode($respuesta);

?>
