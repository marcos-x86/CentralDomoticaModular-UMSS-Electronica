<?php

#Variable que establece la URL del servidor de la base de datos MongoDB
$basedatos=new MongoDB\Driver\Manager('mongodb://localhost:27017');
#Variable que establece el filtro para la busqueda en la base de datos
$filtro=['nombreusuario'=>'admin'];
#Variable que establece los parametros de la consulda a la base de datos
$opciones=['projection'=>['_id'=>0]];
#Variable que establece el tipo de consulta y sus parametros para el driver de MongoDB
$consulta=new MongoDB\Driver\Query($filtro, $opciones);
#Variable que almacena el resultado de la consulta ejecutada en una determinada base de datos
$resultado=$basedatos->executeQuery("usuarios.usuariosautorizados", $consulta);
#Variable que apunta al primer resultado de la consulta a la base de datos
$resultadoconsulta=current($resultado->toArray());

#Procedimiento que contruye la respuesta en base a la consulta de la base de datos parametros
#verificar si el usuario ha iniciado sesion correctamente
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
