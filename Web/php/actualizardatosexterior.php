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
#Variable que establece los parametros de la consulda a la base de datos
$opciones=['sort' => ['fecha' => -1], 'limit' => 1];
#Variable que establece el tipo de consulta y sus parametros para el driver de MongoDB
$consulta=new MongoDB\Driver\Query([], $opciones);
#Variable que almacena el resultado de la consulta ejecutada en una determinada base de datos
$resultado=$basedatos->executeQuery("moduloexterior.datosmoduloexterior", $consulta);
#Variable que apunta al primer resultado de la consulta a la base de datos
$resultadoconsulta=current($resultado->toArray());

#Variable que establece las opciones de proyeccion dentro del formato de consulta
$opcionesmodo=['projection'=>['_id'=>0]];
#Variable que establece el tipo de consulta y sus proyecciones para el driver de MongoDB
$consultamodo=new MongoDB\Driver\Query([], $opcionesmodo);
#Variable que almacena el resultado de la consulta ejecutada en una determinada base de datos
$resultadomodo=$basedatos->executeQuery("modosoperacion.moduloexterior", $consultamodo);
#Variable que apunta al primer resultado de la consulta a la base de datos
$resultadoconsultamodo=current($resultadomodo->toArray());

#Procedimiento que construye la respuesta en base a los resultados de la consulta
#para recuperar los datos mas recientes registrados por el modulo exterior
if((!empty($resultadoconsulta))&&(!empty($resultadoconsultamodo))){
  if(($resultadoconsulta->lluvia)==true){
    $respuesta['lluvia']="SI";
  }
  else{
    if(($resultadoconsulta->lluvia)==false){
      $respuesta['lluvia']="NO";
    }
  }
  $respuesta['nivelagua']=$resultadoconsulta->nivelagua;
  if(($resultadoconsulta->movimiento)==true){
    $respuesta['movimiento']="SI";
  }
  else{
    if(($resultadoconsulta->movimiento)==false){
      $respuesta['movimiento']="NO";
    }
  }
  $respuesta['luz']=$resultadoconsulta->luz;
  $respuesta['consumoelectrico']=$resultadoconsulta->consumoelectrico;
  $respuesta['consumoagua']=$resultadoconsulta->consumoagua;
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
