<?php

$modo=isset($_POST['modo']) ? $_POST['modo'] : null;

if($modo!=null){
  $basedatos=new MongoDB\Driver\Manager('mongodb://localhost:27017');
  if($modo=="normal"){
    $escritura=new MongoDB\Driver\BulkWrite;
    $escritura->update([], ['$set'=>['modonormal'=>true]]);
    $basedatos->executeBulkWrite('comandosmodosoperacion.modulococina', $escritura);
    $escritura=new MongoDB\Driver\BulkWrite;
    $escritura->update([], ['$set'=>['modosemi'=>false]]);
    $basedatos->executeBulkWrite('comandosmodosoperacion.modulococina', $escritura);
    $escritura=new MongoDB\Driver\BulkWrite;
    $escritura->update([], ['$set'=>['modoarmado'=>false]]);
    $basedatos->executeBulkWrite('comandosmodosoperacion.modulococina', $escritura);
    $escritura=new MongoDB\Driver\BulkWrite;
    $escritura->update([], ['$set'=>['modificado'=>true]]);
    $basedatos->executeBulkWrite('comandosmodosoperacion.modulococina', $escritura);
  }
  else{
    if($modo=="semi"){
      $escritura=new MongoDB\Driver\BulkWrite;
      $escritura->update([], ['$set'=>['modonormal'=>false]]);
      $basedatos->executeBulkWrite('comandosmodosoperacion.modulococina', $escritura);
      $escritura=new MongoDB\Driver\BulkWrite;
      $escritura->update([], ['$set'=>['modosemi'=>true]]);
      $basedatos->executeBulkWrite('comandosmodosoperacion.modulococina', $escritura);
      $escritura=new MongoDB\Driver\BulkWrite;
      $escritura->update([], ['$set'=>['modoarmado'=>false]]);
      $basedatos->executeBulkWrite('comandosmodosoperacion.modulococina', $escritura);
      $escritura=new MongoDB\Driver\BulkWrite;
      $escritura->update([], ['$set'=>['modificado'=>true]]);
      $basedatos->executeBulkWrite('comandosmodosoperacion.modulococina', $escritura);
    }
    else{
      if($modo=="armado"){
        $escritura=new MongoDB\Driver\BulkWrite;
        $escritura->update([], ['$set'=>['modonormal'=>false]]);
        $basedatos->executeBulkWrite('comandosmodosoperacion.modulococina', $escritura);
        $escritura=new MongoDB\Driver\BulkWrite;
        $escritura->update([], ['$set'=>['modosemi'=>false]]);
        $basedatos->executeBulkWrite('comandosmodosoperacion.modulococina', $escritura);
        $escritura=new MongoDB\Driver\BulkWrite;
        $escritura->update([], ['$set'=>['modoarmado'=>true]]);
        $basedatos->executeBulkWrite('comandosmodosoperacion.modulococina', $escritura);
        $escritura=new MongoDB\Driver\BulkWrite;
        $escritura->update([], ['$set'=>['modificado'=>true]]);
        $basedatos->executeBulkWrite('comandosmodosoperacion.modulococina', $escritura);
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