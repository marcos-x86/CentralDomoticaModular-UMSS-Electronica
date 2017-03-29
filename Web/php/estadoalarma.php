<?php

$estado=isset($_POST['estado']) ? $_POST['estado'] : null;

if($estado!=null){
  $basedatos=new MongoDB\Driver\Manager('mongodb://localhost:27017');
  $escritura=new MongoDB\Driver\BulkWrite;
  if($estado=="activar"){
    $escritura->update([], ['$set'=>['encendida'=>true]]);
    $basedatos->executeBulkWrite('comandosalarmas.estadoalarma', $escritura);
    $escritura=new MongoDB\Driver\BulkWrite;
    $escritura->update([], ['$set'=>['modificado'=>true]]);
    $basedatos->executeBulkWrite('comandosalarmas.estadoalarma', $escritura);
    $respuesta['confirmacion']=true;
    echo json_encode($respuesta);
  }
  else{
    if($estado=="desactivar"){
      $escritura->update([], ['$set'=>['encendida'=>false]]);
      $basedatos->executeBulkWrite('comandosalarmas.estadoalarma', $escritura);
      $escritura=new MongoDB\Driver\BulkWrite;
      $escritura->update([], ['$set'=>['modificado'=>true]]);
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
