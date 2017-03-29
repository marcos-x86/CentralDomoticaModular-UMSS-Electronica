<?php

$basedatos=new MongoDB\Driver\Manager('mongodb://localhost:27017');
$filtro=['nombreusuario'=>'admin'];
$opciones=['projection'=>['_id'=>0]];
$consulta=new MongoDB\Driver\Query($filtro, $opciones);
$resultado=$basedatos->executeQuery("usuarios.usuariosautorizados", $consulta);
$resultadoconsulta=current($resultado->toArray());

if(!empty($resultadoconsulta)){
	if($resultadoconsulta->sesion){
		$respuesta['autorizado']=true;
		echo json_encode($respuesta);
	}
	else{
		$respuesta['autorizado']=false;
		echo json_encode($respuesta);
	}
}

?>
