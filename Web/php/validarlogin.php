<?php

$usuario=isset($_POST['nombreusuario']) ? $_POST['nombreusuario'] : null;
$contrasena=isset($_POST['contrasena']) ? $_POST['contrasena'] : null;

if($usuario!=null){
	$basedatos=new MongoDB\Driver\Manager('mongodb://localhost:27017');
	$filtro=['nombreusuario'=>$usuario];
	$opciones=['projection'=>['_id'=>0]];
	$consulta=new MongoDB\Driver\Query($filtro, $opciones);
    $resultado=$basedatos->executeQuery("usuarios.usuariosautorizados", $consulta);
    $resultadoconsulta=current($resultado->toArray());

    if(!empty($resultadoconsulta)){
    	if(($resultadoconsulta->nombreusuario)==$usuario){
    		if(($resultadoconsulta->contrasena)==$contrasena){
    			$escritura=new MongoDB\Driver\BulkWrite;
    			$escritura->update(['nombreusuario'=>$usuario], ['$set'=>['sesion'=>true]]);
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
