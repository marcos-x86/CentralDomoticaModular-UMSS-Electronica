<?php

#UNIVERSIDAD MAYOR DE SAN SIMON
#FACULTAD DE CIENCIAS Y TECNOLOGIA
#CARRERA DE INGENIERIA ELECTRONICA
#
#SISTEMA DOMOTICO MODULAR CENTRALIZADO DESARROLLADO POR:
#
#LARA TORRICO MARCOS
#TORREZ JORGE BRIAN

#Variables que almacenan el contenido de los datos de la solicitud POST
$actuador1=isset($_POST['actuador1']) ? $_POST['actuador1'] : null;
$actuador2=isset($_POST['actuador2']) ? $_POST['actuador2'] : null;

#Procedimiento que realiza las modificaciones a los documentos de la base de datos
#en MongoDB para los actuadores del modulo cocina
if($actuador1!=null&&$actuador2!=null){
  #Variable que establece la URL del servidor de la base de datos MongoDB
  $basedatos=new MongoDB\Driver\Manager('mongodb://localhost:27017');
  if($actuador1=="activar"){
    #Variable que se emplea para la escritura o modificacion de un documento
    #en la base de datos MongoDB
    $escritura=new MongoDB\Driver\BulkWrite;
    #Parametros de escritura
    $escritura->update([], ['$set'=>['actuador1'=>true]]);
    #Ejecucion de la solicitud de escritura en la base de datos
    $basedatos->executeBulkWrite('comandoscocina.actuadorescocina', $escritura);
  }
  else{
    if($actuador1=="desactivar"){
      #Variable que se emplea para la escritura o modificacion de un documento
      #en la base de datos MongoDB
      $escritura=new MongoDB\Driver\BulkWrite;
      #Parametros de escritura
      $escritura->update([], ['$set'=>['actuador1'=>false]]);
      #Ejecucion de la solicitud de escritura en la base de datos
      $basedatos->executeBulkWrite('comandoscocina.actuadorescocina', $escritura);
    }
  }
  if($actuador2=="activar"){
    #Variable que se emplea para la escritura o modificacion de un documento
    #en la base de datos MongoDB
    $escritura=new MongoDB\Driver\BulkWrite;
    #Parametros de escritura
    $escritura->update([], ['$set'=>['actuador2'=>true]]);
    #Ejecucion de la solicitud de escritura en la base de datos
    $basedatos->executeBulkWrite('comandoscocina.actuadorescocina', $escritura);
  }
  else{
    if($actuador2=="desactivar"){
      #Variable que se emplea para la escritura o modificacion de un documento
      #en la base de datos MongoDB
      $escritura=new MongoDB\Driver\BulkWrite;
      #Parametros de escritura
      $escritura->update([], ['$set'=>['actuador2'=>false]]);
      #Ejecucion de la solicitud de escritura en la base de datos
      $basedatos->executeBulkWrite('comandoscocina.actuadorescocina', $escritura);
    }
  }
  #Variable que se emplea para la escritura o modificacion de un documento
  #en la base de datos MongoDB
  $escritura=new MongoDB\Driver\BulkWrite;
  #Parametros de escritura
  $escritura->update([], ['$set'=>['modificado'=>true]]);
  #Ejecucion de la solicitud de escritura en la base de datos
  $basedatos->executeBulkWrite('comandoscocina.actuadorescocina', $escritura);
  $respuesta['modificado']=true;
  echo json_encode($respuesta);
}
else{
  $respuesta['modificado']=false;
  echo json_encode($respuesta);
}

?>
