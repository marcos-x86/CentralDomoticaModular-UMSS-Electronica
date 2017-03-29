<?php

$cierresesion=isset($_POST['cerrarsesionusuario']) ? $_POST['cerrarsesionusuario'] : null;

if($cierresesion!=null){
	if($cierresesion){
		$basedatos=new MongoDB\Driver\Manager('mongodb://localhost:27017');
		$escritura=new MongoDB\Driver\BulkWrite;
		$escritura->update(['nombreusuario'=>'admin'], ['$set'=>['sesion'=>false]]);
		$basedatos->executeBulkWrite('usuarios.usuariosautorizados', $escritura);
		$respuesta['cerrado']=true;
    echo json_encode($respuesta);
	}
	else{
		$respuesta['cerrado']=false;
    echo json_encode($respuesta);
	}
}else{
	$respuesta['cerrado']=false;
  echo json_encode($respuesta);
}

?>
