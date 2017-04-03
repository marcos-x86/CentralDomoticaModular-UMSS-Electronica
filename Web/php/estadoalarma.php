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
$estado=isset($_POST['estado']) ? $_POST['estado'] : null;

#Procedimiento que establece la modificacion del estado de la alarma de
#acuerdo al contenido de la variable estado
if($estado!=null){
  #Variable que establece la URL del servidor de la base de datos MongoDB
  $basedatos=new MongoDB\Driver\Manager('mongodb://localhost:27017');
  #Variable que se emplea para la escritura o modificacion de un documento
  #en la base de datos MongoDB
  $escritura=new MongoDB\Driver\BulkWrite;
  if($estado=="activar"){
    #Parametros de escritura
    $escritura->update([], ['$set'=>['encendida'=>true]]);
    #Ejecucion de la solicitud de escritura en la base de datos
    $basedatos->executeBulkWrite('comandosalarmas.estadoalarma', $escritura);
    $escritura=new MongoDB\Driver\BulkWrite;
    #Parametros de escritura
    $escritura->update([], ['$set'=>['modificado'=>true]]);
    #Ejecucion de la solicitud de escritura en la base de datos
    $basedatos->executeBulkWrite('comandosalarmas.estadoalarma', $escritura);
    $respuesta['confirmacion']=true;
    echo json_encode($respuesta);
  }
  else{
    if($estado=="desactivar"){
      #Parametros de escritura
      $escritura->update([], ['$set'=>['encendida'=>false]]);
      #Ejecucion de la solicitud de escritura en la base de datos
      $basedatos->executeBulkWrite('comandosalarmas.estadoalarma', $escritura);
      $escritura=new MongoDB\Driver\BulkWrite;
      #Parametros de escritura
      $escritura->update([], ['$set'=>['modificado'=>true]]);
      #Ejecucion de la solicitud de escritura en la base de datos
      $basedatos->executeBulkWrite('comandosalarmas.estadoalarma', $escritura);
      $respuesta['confirmacion']=true;
      echo json_encode($respuesta);
    }
    else{
      $respuesta['confirmacion']=false;
      echo json_encode($respuesta);
    }
  }
}
else{
  $respuesta['confirmacion']=false;
  echo json_encode($respuesta);
}

?>
