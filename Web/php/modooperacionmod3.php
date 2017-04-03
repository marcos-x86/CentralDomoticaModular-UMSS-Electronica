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
$modo=isset($_POST['modo']) ? $_POST['modo'] : null;

#Procedimiento que establece la modificacion del modo de operacion de
#acuerdo al contenido de la variable estado en el modulo exterior
if($modo!=null){
  #Procedimiento que establece la modificacion del estado de la alarma de
  #acuerdo al contenido de la variable estado
  $basedatos=new MongoDB\Driver\Manager('mongodb://localhost:27017');
  if($modo=="normal"){
    #Variable que se emplea para la escritura o modificacion de un documento
    #en la base de datos MongoDB
    $escritura=new MongoDB\Driver\BulkWrite;
    #Parametros de escritura
    $escritura->update([], ['$set'=>['modonormal'=>true]]);
    #Ejecucion de la solicitud de escritura en la base de datos
    $basedatos->executeBulkWrite('comandosmodosoperacion.moduloexterior', $escritura);
    $escritura=new MongoDB\Driver\BulkWrite;
    #Parametros de escritura
    $escritura->update([], ['$set'=>['modosemi'=>false]]);
    #Ejecucion de la solicitud de escritura en la base de datos
    $basedatos->executeBulkWrite('comandosmodosoperacion.moduloexterior', $escritura);
    $escritura=new MongoDB\Driver\BulkWrite;
    #Parametros de escritura
    $escritura->update([], ['$set'=>['modoarmado'=>false]]);
    #Ejecucion de la solicitud de escritura en la base de datos
    $basedatos->executeBulkWrite('comandosmodosoperacion.moduloexterior', $escritura);
    $escritura=new MongoDB\Driver\BulkWrite;
    #Parametros de escritura
    $escritura->update([], ['$set'=>['modificado'=>true]]);
    #Ejecucion de la solicitud de escritura en la base de datos
    $basedatos->executeBulkWrite('comandosmodosoperacion.moduloexterior', $escritura);
  }
  else{
    if($modo=="semi"){
      $escritura=new MongoDB\Driver\BulkWrite;
      #Parametros de escritura
      $escritura->update([], ['$set'=>['modonormal'=>false]]);
      #Ejecucion de la solicitud de escritura en la base de datos
      $basedatos->executeBulkWrite('comandosmodosoperacion.moduloexterior', $escritura);
      $escritura=new MongoDB\Driver\BulkWrite;
      #Parametros de escritura
      $escritura->update([], ['$set'=>['modosemi'=>true]]);
      #Ejecucion de la solicitud de escritura en la base de datos
      $basedatos->executeBulkWrite('comandosmodosoperacion.moduloexterior', $escritura);
      $escritura=new MongoDB\Driver\BulkWrite;
      #Parametros de escritura
      $escritura->update([], ['$set'=>['modoarmado'=>false]]);
      #Ejecucion de la solicitud de escritura en la base de datos
      $basedatos->executeBulkWrite('comandosmodosoperacion.moduloexterior', $escritura);
      $escritura=new MongoDB\Driver\BulkWrite;
      #Parametros de escritura
      $escritura->update([], ['$set'=>['modificado'=>true]]);
      #Ejecucion de la solicitud de escritura en la base de datos
      $basedatos->executeBulkWrite('comandosmodosoperacion.moduloexterior', $escritura);
    }
    else{
      if($modo=="armado"){
        $escritura=new MongoDB\Driver\BulkWrite;
        #Parametros de escritura
        $escritura->update([], ['$set'=>['modonormal'=>false]]);
        #Ejecucion de la solicitud de escritura en la base de datos
        $basedatos->executeBulkWrite('comandosmodosoperacion.moduloexterior', $escritura);
        $escritura=new MongoDB\Driver\BulkWrite;
        #Parametros de escritura
        $escritura->update([], ['$set'=>['modosemi'=>false]]);
        #Ejecucion de la solicitud de escritura en la base de datos
        $basedatos->executeBulkWrite('comandosmodosoperacion.moduloexterior', $escritura);
        $escritura=new MongoDB\Driver\BulkWrite;
        #Parametros de escritura
        $escritura->update([], ['$set'=>['modoarmado'=>true]]);
        #Ejecucion de la solicitud de escritura en la base de datos
        $basedatos->executeBulkWrite('comandosmodosoperacion.moduloexterior', $escritura);
        $escritura=new MongoDB\Driver\BulkWrite;
        #Parametros de escritura
        $escritura->update([], ['$set'=>['modificado'=>true]]);
        #Ejecucion de la solicitud de escritura en la base de datos
        $basedatos->executeBulkWrite('comandosmodosoperacion.moduloexterior', $escritura);
      }
    }
  }
  $respuesta['modificado']=true;
  echo json_encode($respuesta);
}
else{
  $respuesta['modificado']=false;
  echo json_encode($respuesta);
}

?>
