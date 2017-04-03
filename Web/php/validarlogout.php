<?php

#Variable que almacena el contenido de los datos de la solicitud POST
$cierresesion=isset($_POST['cerrarsesionusuario']) ? $_POST['cerrarsesionusuario'] : null;

#Procedimiento que realiza las modificaciones en base de datos para el cierre de sesion
if($cierresesion!=null){
	if($cierresesion){
		#Variable que establece la URL del servidor de la base de datos MongoDB
		$basedatos=new MongoDB\Driver\Manager('mongodb://localhost:27017');
		#Variable que se emplea para la escritura o modificacion de un documento
		#en la base de datos MongoDB
		$escritura=new MongoDB\Driver\BulkWrite;
		#Parametros de escritura
		$escritura->update(['nombreusuario'=>'admin'], ['$set'=>['sesion'=>false]]);
		#Ejecucion de la solicitud de escritura en la base de datos
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
