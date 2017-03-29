<?php

$basedatos=new MongoDB\Driver\Manager('mongodb://localhost:27017');
$opciones=['sort' => ['fecha' => -1], 'limit' => 1];
$consulta=new MongoDB\Driver\Query([], $opciones);
$resultado=$basedatos->executeQuery("modulohabitacion.datosmodulohabitacion", $consulta);
$resultadoconsulta=current($resultado->toArray());

$opcionesmodo=['projection'=>['_id'=>0]];
$consultamodo=new MongoDB\Driver\Query([], $opcionesmodo);
$resultadomodo=$basedatos->executeQuery("modosoperacion.modulohabitacion", $consultamodo);
$resultadoconsultamodo=current($resultadomodo->toArray());

if((!empty($resultadoconsulta))&&(!empty($resultadoconsultamodo))){
  $respuesta['temperatura']=$resultadoconsulta->temperatura;
  $respuesta['humedad']=$resultadoconsulta->humedad;
  if(($resultadoconsulta->movimiento)==true){
    $respuesta['movimiento']="SI";
  }
  else{
    if(($resultadoconsulta->movimiento)==false){
      $respuesta['movimiento']="NO";
    }
  }
  if(($resultadoconsulta->magnetico)==true){
    $respuesta['magnetico']="SI";
  }
  else{
    if(($resultadoconsulta->magnetico)==false){
      $respuesta['magnetico']="NO";
    }
  }
  $respuesta['humo']=$resultadoconsulta->humo;
  if(($resultadoconsulta->actuador1)==true){
    $respuesta['actuador1']="ON";
  }
  else{
    if(($resultadoconsulta->actuador1)==false){
      $respuesta['actuador1']="OFF";
    }
  }
  if(($resultadoconsulta->actuador2)==true){
    $respuesta['actuador2']="ON";
  }
  else{
    if(($resultadoconsulta->actuador2)==false){
      $respuesta['actuador2']="OFF";
    }
  }
  $respuesta['controles']=$resultadoconsulta->controles;
  if(($resultadoconsultamodo->modonormal)==true){
    $respuesta['modonormal']=true;
  }
  else{
    if(($resultadoconsultamodo->modonormal)==false){
      $respuesta['modonormal']=false;
    }
  }
  if(($resultadoconsultamodo->modosemi)==true){
    $respuesta['modosemi']=true;
  }
  else{
    if(($resultadoconsultamodo->modosemi)==false){
      $respuesta['modosemi']=false;
    }
  }
  if(($resultadoconsultamodo->modoarmado)==true){
    $respuesta['modoarmado']=true;
  }
  else{
    if(($resultadoconsultamodo->modoarmado)==false){
      $respuesta['modoarmado']=false;
    }
  }
  echo json_encode($respuesta);
}

?>
