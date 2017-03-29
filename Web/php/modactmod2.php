<?php

$actuador1=isset($_POST['actuador1']) ? $_POST['actuador1'] : null;
$actuador2=isset($_POST['actuador2']) ? $_POST['actuador2'] : null;

if($actuador1!=null&&$actuador2!=null){
  $basedatos=new MongoDB\Driver\Manager('mongodb://localhost:27017');
  if($actuador1=="activar"){
    $escritura=new MongoDB\Driver\BulkWrite;
    $escritura->update([], ['$set'=>['actuador1'=>true]]);
    $basedatos->executeBulkWrite('comandoscocina.actuadorescocina', $escritura);
  }
  else{
    if($actuador1=="desactivar"){
      $escritura=new MongoDB\Driver\BulkWrite;
      $escritura->update([], ['$set'=>['actuador1'=>false]]);
      $basedatos->executeBulkWrite('comandoscocina.actuadorescocina', $escritura);
    }
  }
  if($actuador2=="activar"){
    $escritura=new MongoDB\Driver\BulkWrite;
    $escritura->update([], ['$set'=>['actuador2'=>true]]);
    $basedatos->executeBulkWrite('comandoscocina.actuadorescocina', $escritura);
  }
  else{
    if($actuador2=="desactivar"){
      $escritura=new MongoDB\Driver\BulkWrite;
      $escritura->update([], ['$set'=>['actuador2'=>false]]);
      $basedatos->executeBulkWrite('comandoscocina.actuadorescocina', $escritura);
    }
  }
  $escritura=new MongoDB\Driver\BulkWrite;
  $escritura->update([], ['$set'=>['modificado'=>true]]);
  $basedatos->executeBulkWrite('comandoscocina.actuadorescocina', $escritura);
  $respuesta['modificado']=true;
  echo json_encode($respuesta);
}
else{
  $respuesta['modificado']=false;
  echo json_encode($respuesta);
}

?>