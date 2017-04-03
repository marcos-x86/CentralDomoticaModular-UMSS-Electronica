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
$usuario=isset($_POST['nombreusuario']) ? $_POST['nombreusuario'] : null;
$contrasena=isset($_POST['contrasena']) ? $_POST['contrasena'] : null;

#Procedimiento que valida el login buscando el usuario y contrasena en
#la base de datos MongoDB
if($usuario!=null){
	#Variable que establece la URL del servidor de la base de datos MongoDB
	$basedatos=new MongoDB\Driver\Manager('mongodb://localhost:27017');
	#Variable que establece el filtro para la busqueda en la base de datos
	$filtro=['nombreusuario'=>$usuario];
	#Variable que establece las opciones de proyeccion dentro del formato de consulta
	$opciones=['projection'=>['_id'=>0]];
	#Variable que establece el tipo de consulta y sus parametros para el driver de MongoDB
	$consulta=new MongoDB\Driver\Query($filtro, $opciones);
	#Variable que almacena el resultado de la consulta ejecutada en una determinada base de datos
  $resultado=$basedatos->executeQuery("usuarios.usuariosautorizados", $consulta);
	#Variable que apunta al primer resultado de la consulta a la base de datos
  $resultadoconsulta=current($resultado->toArray());

  if(!empty($resultadoconsulta)){
  	if(($resultadoconsulta->nombreusuario)==$usuario){
  		if(($resultadoconsulta->contrasena)==$contrasena){
				#Variable que se emplea para la escritura o modificacion de un documento
		    #en la base de datos MongoDB
  			$escritura=new MongoDB\Driver\BulkWrite;
				#Parametros de escritura
  			$escritura->update(['nombreusuario'=>$usuario], ['$set'=>['sesion'=>true]]);
				#Ejecucion de la solicitud de escritura en la base de datos
  			$basedatos->executeBulkWrite('usuarios.usuariosautorizados', $escritura);
  			$respuesta['autorizado']=true;
  			echo json_encode($respuesta);
  		}
  		else{
  			$respuesta['autorizado']=false;
  			echo json_encode($respuesta);
  		}
  	}
  	else{
  		$respuesta['autorizado']=false;
  		echo json_encode($respuesta);
  	}
  }
  else{
  	$respuesta['autorizado']=false;
  	echo json_encode($respuesta);
  }
}
else{
	$respuesta['autorizado']=false;
	echo json_encode($respuesta);
}

?>
