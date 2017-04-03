//UNIVERSIDAD MAYOR DE SAN SIMON
//FACULTAD DE CIENCIAS Y TECNOLOGIA
//CARRERA DE INGENIERIA ELECTRONICA
//
//SISTEMA DOMOTICO MODULAR CENTRALIZADO DESARROLLADO POR:
//
//LARA TORRICO MARCOS
//TORREZ JORGE BRIAN

//Importacion de librerias externas

//Libreria para la comunicacion a traves del puerto serial
import com.fazecast.jSerialComm.*;
//Liberias del cliente para operaciones CRUD en MongoDB
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import static com.mongodb.client.model.Filters.eq;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.bson.Document;
//Librerias de java para manejo de fechas, estructuras de datos, y temporizadores
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.LinkedList;
import java.util.Comparator;

//Clase principal de la central domotica
public class CentralDomotica{

    //Variables que almacenan las referencias a las diferentes bases de datos del sistema domotico
    public MongoClient clienteMongo=new MongoClient("localhost",27017);
    public MongoDatabase baseDatosComandosAlarmas=clienteMongo.getDatabase("comandosalarmas");
    public MongoDatabase baseDatosComandosCocina=clienteMongo.getDatabase("comandoscocina");
    public MongoDatabase baseDatosComandosExterior=clienteMongo.getDatabase("comandosexterior");
    public MongoDatabase baseDatosComandosHabitacion=clienteMongo.getDatabase("comandoshabitacion");
    public MongoDatabase baseDatosComandosModosOperacion=clienteMongo.getDatabase("comandosmodosoperacion");
    public MongoDatabase baseDatosModosOperacion=clienteMongo.getDatabase("modosoperacion");
    public MongoDatabase baseDatosModuloCocina=clienteMongo.getDatabase("modulococina");
    public MongoDatabase baseDatosModuloCoordinador=clienteMongo.getDatabase("modulocoordinador");
    public MongoDatabase baseDatosModuloExterior=clienteMongo.getDatabase("moduloexterior");
    public MongoDatabase baseDatosModuloHabitacion=clienteMongo.getDatabase("modulohabitacion");
    public MongoDatabase baseDatosModulosActivados=clienteMongo.getDatabase("modulosactivados");
    public MongoDatabase baseDatosNotificaciones=clienteMongo.getDatabase("notificaciones");
    public MongoDatabase baseDatosUsuarios=clienteMongo.getDatabase("usuarios");

    //Variables que almacenan las referencias a las colecciones de las bases de datos del sistema domotico
    public MongoCollection coleccionComandoEstadoAlarma=baseDatosComandosAlarmas.getCollection("estadoalarma");
    public MongoCollection coleccionComandoActuadoresCocina=baseDatosComandosCocina.getCollection("actuadorescocina");
    public MongoCollection coleccionComandoActuadoresExterior=baseDatosComandosExterior.getCollection("actuadoresexterior");
    public MongoCollection coleccionComandoActuadoresHabitacion=baseDatosComandosHabitacion.getCollection("actuadoreshabitacion");
    public MongoCollection coleccionComandosModoOperacionCocina=baseDatosComandosModosOperacion.getCollection("modulococina");
    public MongoCollection coleccionComandosModoOperacionExterior=baseDatosComandosModosOperacion.getCollection("moduloexterior");
    public MongoCollection coleccionComandosModoOperacionHabitacion=baseDatosComandosModosOperacion.getCollection("modulohabitacion");
    public MongoCollection coleccionModoOperacionCocina=baseDatosModosOperacion.getCollection("modulococina");
    public MongoCollection coleccionModoOperacionExterior=baseDatosModosOperacion.getCollection("moduloexterior");
    public MongoCollection coleccionModoOperacionHabitacion=baseDatosModosOperacion.getCollection("modulohabitacion");
    public MongoCollection coleccionDatosModuloCocina=baseDatosModuloCocina.getCollection("datosmodulococina");
    public MongoCollection coleccionDatosModuloExterior=baseDatosModuloExterior.getCollection("datosmoduloexterior");
    public MongoCollection coleccionDatosModuloHabitacion=baseDatosModuloHabitacion.getCollection("datosmodulohabitacion");
    public MongoCollection coleccionAlarmaModuloCoordinador=baseDatosModuloCoordinador.getCollection("alarma");
    public MongoCollection coleccionRegistroModulos=baseDatosModulosActivados.getCollection("registro");
    public MongoCollection coleccionNotificaciones=baseDatosNotificaciones.getCollection("notificacionesemergentes");
    public MongoCollection coleccionUsuariosAutorizados=baseDatosUsuarios.getCollection("usuariosautorizados");

    //Variables auxiliares que emplea el planificador de tareas
    public int contadorPlanificador=0;
    public int quantumPlanificador=0;

    //Variables que controlan los niveles maximos y minimos para el control de algunas variables fisicas detectadas por los sensores
    public int limiteMaximoContadorDesconexion=5;
    public int limiteMaximoTemperatura=30;
    public int limiteMinimoTemperatura=7;
    public int limiteMaximoHumo=2000;
    public int limiteMaximoGas=1500;
    public int limiteMinimoNivelAgua=3;
    public int limiteMinimoLuz=700;
    public int largoTanque=33;
    public int anchoTanque=28;

    //Variables que parametrizan e inicializan los temporizadores
    public Timer ejecucionTareas;
    public Timer lecturaPuertoSerial;
    public TemporizadorTareas tareasProgramadas;
    public TemporizadorLecturaDatos leerDatosRecibidos;

    //Variables de las estructuras de datos usadas por el sistema
    public LinkedList<Modulo> modulosRegistrados=new LinkedList<Modulo>();
    public LinkedList<Tarea> listaTareas=new LinkedList<Tarea>();
    public LinkedList<Byte> listaModificaciones=new LinkedList<Byte>();
    public LinkedList<Byte> listaNumerosModulos=new LinkedList<Byte>();
    public LinkedList<Byte> comandoActual=new LinkedList<Byte>();
    public LinkedList<String> tipoMensaje=new LinkedList<String>();

    //Variables usadas para la comunicacion por el puerto serie
    public volatile SerialPort arduinoSerial;
    public volatile byte datosLeidos[];
    public volatile LinkedList<LinkedList<Byte>> comandosRecibidos=new LinkedList<LinkedList<Byte>>();

    //=========================================================================
    //Codigos (en bytes) de comando del subprotocolo de comunicacion M2M del sistema
    //
    //REGISTRAR_MODULO: 0x41
    //ENVIAR_CONFIGURACION: 0x42
    //OBTENER_DATOS: 0x43
    //ESTABLECER_SALIDAS: 0x44
    //MODIFICAR_FUNCIONES: 0x45
    //ESTADO_ALARMA: 0x46
    //ENVIO_SMS: 0x47
    //CODIGO_ERROR: 0x48
    //=========================================================================

    //=========================================================================
    //Codigos (en bytes) de los tipos de modificacione para lista de modificaciones
    //
    //
    //modificacion estado alarma: 0x00
    //
    //modificacion actuadores cocina: 0x01
    //modificacion actuadores exterior: 0x02
    //modificacion actuadores habitacion: 0x03
    //
    //modificacion modo operacion cocina: 0x04
    //modificacion modo operacion exterior: 0x05
    //modificacion modo operacion habitacion: 0x06
    //=========================================================================

    //=========================================================================
    //Codigos (en bytes) de los tipos de tareas manejados por el programador de tareas
    //enviar estado actuadores cocina: 0x00
    //enviar estado actuadores exterior: 0x01
    //enviar estado actuadores habitacion: 0x02
    //
    //enviar estado alarma: 0x03
    //enviar mensaje alerta: 0x04
    //
    //solicitar datos modulo cocina: 0x05
    //solicitar datos modulo exterior: 0x06
    //solicitar datos modulo habilitacion: 0x07
    //=========================================================================


    //Creacion de instancia de la clase principal para ejecutar el programa
    public static void main (String args[]){
        try{
            CentralDomotica backend=new CentralDomotica();
            backend.correrPrograma();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    //Metodo que ejecuta el programa principal e inicializa los temporizadores
    public void correrPrograma(){
        //Creacion de la instancia de la clase SerialPort para la manipulacion de puertos seriales
        SerialPort[] listaPuertosSeriales=SerialPort.getCommPorts();
        boolean puertoEncontrado=false;
        int numeroPuertoIdentificado=0;
        //Deteccion del puerto serial
        for(SerialPort puertoActual : listaPuertosSeriales){
            if(puertoActual.getSystemPortName().equals("ttyUSB0")){
                puertoEncontrado=true;
                break;
            }
            else{
                numeroPuertoIdentificado++;
            }
        }
        if(puertoEncontrado){
            //Inicio de comunicacion a traves del puerto serie
            arduinoSerial=listaPuertosSeriales[numeroPuertoIdentificado];
            //Configuracion de los parametros del puerto serie
            arduinoSerial.setComPortParameters(19200,8,arduinoSerial.ONE_STOP_BIT,arduinoSerial.NO_PARITY);
            if(arduinoSerial.openPort()){
                System.out.println("Conexion establecida");
                iniciarValoresPorDefecto();
                System.out.println("inicie valores por defecto");
                iniciarTemporizadorTareas();
                System.out.println("inicie temporizador tarea");
                iniciarTemporizadorLecturaDatos();
                System.out.println("inicie temporizador lectura datos");
                escribirNotificacion("La central domotica se ha iniciado correctamente","info");

                Thread.currentThread().suspend();

            }
            else{
                System.out.println("Conexion no establecida");
            }
            //arduinoSerial.closePort();
            //clienteMongo.close();
        }
        else{
            System.out.println("Puerto no encontrado");
        }
    }

    //Clase interna que hereda de la clase TimerTask. esta implementa el programador de tareas
    public class TemporizadorTareas extends TimerTask{
        public TemporizadorTareas(){
            super();
        }

        @Override
        public void run(){
            if(verificarModificaciones()){
                System.out.println("interpretare modificacion");
                interpretarModificaciones();
            }
            if(comandosRecibidos.size()>0){
                System.out.println("interpretare comando");
                interpretarComando();
            }
            if(listaTareas.size()>0){
                System.out.println("realizare tarea");
                ejecutarTarea();
            }
        }
    }

    //Clase interna que hereda de la clase TimerTask. esta se encarga exclusivamente de la recepcion y almacenamiento de datos
    public class TemporizadorLecturaDatos extends TimerTask{
        public TemporizadorLecturaDatos(){
            super();
        }

        @Override
        public void run(){
            if(arduinoSerial.bytesAvailable()>0){
                datosLeidos=new byte[arduinoSerial.bytesAvailable()];
                arduinoSerial.readBytes(datosLeidos,datosLeidos.length);
                LinkedList<Byte>datosRecibidos=new LinkedList<Byte>();
                for(byte dato : datosLeidos){
                    datosRecibidos.add(dato);
                }
                comandosRecibidos.add(datosRecibidos);
            }
        }
    }

    //Clase interna que agrupa los atributos y funciones de un modulo recolector de datos
    public class Modulo{
        //Atributos comunes de un modulo recolector de datos
        byte numeroModuloAsignado=0x00;
        byte tipoModulo=0x00;
        byte modoOperacion=0x00;
        byte contadorQuantum=0x00;
        byte contadorDesconexion=0x00;
        boolean respuestaPendiente=false;
        boolean controles=true;
        boolean alarmaMovimiento=false;
        boolean alarmaMagnetico=false;
        boolean alarmaHumo=false;
        boolean alarmaGas=false;
        boolean alarmaFuego=false;
        boolean alarmaNivelAgua=false;
        boolean alarmaLuz=false;
        boolean alarmaLluvia=false;

        //Constructor parametrizado de la clase Modulo
        public Modulo(byte numero,byte tipo,byte modo,boolean pendiente,boolean control){
            numeroModuloAsignado=numero;
            tipoModulo=tipo;
            modoOperacion=modo;
            respuestaPendiente=pendiente;
            controles=control;
        }

        public byte obtenerNumeroAsignado(){
            return numeroModuloAsignado;
        }

        public byte obtenerTipoModulo(){
            return tipoModulo;
        }

        public void establecerRespuestaPendiente(boolean estado){
            respuestaPendiente=estado;
        }

        public boolean obtenerRespuestaPendiente(){
            return respuestaPendiente;
        }

        public void establecerModoOperacion(byte modo){
            modoOperacion=modo;
        }

        public byte obtenerModoOperacion(){
            return modoOperacion;
        }

        public void establecerContadorQuantum(byte contador){
            contadorQuantum=contador;
        }

        public void incrementarContadorQuantum(){
            contadorQuantum+=1;
        }

        public byte obtenerContadorQuantum(){
            return contadorQuantum;
        }

        public void establecerContadorDesconexion(byte contador){
            contadorDesconexion=contador;
        }

        public void incrementarContadorDesconexion(){
            contadorDesconexion+=1;
        }

        public byte obtenerContadorDesconexion(){
            return contadorDesconexion;
        }

        public void establecerControles(boolean estado){
            controles=estado;
        }

        public boolean obtenerControles(){
            return controles;
        }

        public boolean obtenerAlarmaMovimiento(){
            return alarmaMovimiento;
        }

        public void establecerAlarmaMovimiento(boolean estado){
            alarmaMovimiento=estado;
        }

        public boolean obtenerAlarmaMagnetico(){
            return alarmaMagnetico;
        }

        public void establecerAlarmaMagnetico(boolean estado){
            alarmaMagnetico=estado;
        }

        public boolean obtenerAlarmaHumo(){
            return alarmaHumo;
        }

        public void establecerAlarmaHumo(boolean estado){
            alarmaHumo=estado;
        }

        public boolean obtenerAlarmaGas(){
            return alarmaGas;
        }

        public void establecerAlarmaGas(boolean estado){
            alarmaGas=estado;
        }

        public boolean obtenerAlarmaFuego(){
            return alarmaFuego;
        }

        public void establecerAlarmaFuego(boolean estado){
            alarmaFuego=estado;
        }

        public boolean obtenerAlarmaNivelAgua(){
            return alarmaNivelAgua;
        }

        public void establecerAlarmaNivelAgua(boolean estado){
            alarmaNivelAgua=estado;
        }

        public boolean obtenerAlarmaLuz(){
            return alarmaLuz;
        }

        public void establecerAlarmaLuz(boolean estado){
            alarmaLuz=estado;
        }

        public boolean obtenerAlarmaLluvia(){
            return alarmaLluvia;
        }

        public void establecerAlarmaLluvia(boolean estado){
            alarmaLluvia=estado;
        }
    }

    //Clase interna que agrupa los atributos comunes de una tarea a desarrollarse en el sistema
    public class Tarea{
        //Atributos comunes de una tarea en el sistema
        byte codigoTarea=0x00;
        byte valorNumericoPrincipal=0x00;
        boolean valorLogicoPrincipal=false;
        boolean valorLogicoSecundario=false;

        //Constructor principal de la clase tarea
        public Tarea(byte codigo){
            codigoTarea=codigo;
        }

        //Constructor sobrecargado de la clase tarea
        public Tarea(byte codigo,byte valorNumerico){
            codigoTarea=codigo;
            valorNumericoPrincipal=valorNumerico;
        }

        //Constructor sobrecargado de la clase tarea
        public Tarea(byte codigo,boolean valorPrincipal){
            codigoTarea=codigo;
            valorLogicoPrincipal=valorPrincipal;
        }

        //Constructor sobrecargado de la clase tarea
        public Tarea(byte codigo,boolean valorPrincipal,boolean valorSecundario){
            codigoTarea=codigo;
            valorLogicoPrincipal=valorPrincipal;
            valorLogicoSecundario=valorSecundario;
        }

        public byte obtenerCodigoTarea(){
            return codigoTarea;
        }

        public byte obtenerValorNumericoPrincipal(){
            return valorNumericoPrincipal;
        }

        public boolean obtenerValorLogicoPrincipal(){
            return valorLogicoPrincipal;
        }
        public boolean obtenerValorLogicoSecundario(){
            return valorLogicoSecundario;
        }
    }

    //Metodo que inicia el temporizador del programador de tareas
    public void iniciarTemporizadorTareas(){
        TemporizadorTareas tareasProgramadas=new TemporizadorTareas();
        Timer ejecucionTareas=new Timer(true);
        ejecucionTareas.scheduleAtFixedRate(tareasProgramadas,10,300);
    }

    //Metodo de inicia el temporizador de lectura y almacenamiento de datos
    public void iniciarTemporizadorLecturaDatos(){
        TemporizadorLecturaDatos leerDatosRecibidos=new TemporizadorLecturaDatos();
        Timer lecturaPuertoSerial=new Timer(true);
        lecturaPuertoSerial.scheduleAtFixedRate(leerDatosRecibidos,0,60);
    }

    //Metodo que inicia los valores por defecto en el sistema y la base de datos
    public void iniciarValoresPorDefecto(){
        listaNumerosModulos.add((byte)0x31);
        listaNumerosModulos.add((byte)0x32);
        listaNumerosModulos.add((byte)0x33);
        listaNumerosModulos.add((byte)0x34);
        listaNumerosModulos.add((byte)0x35);
        listaNumerosModulos.add((byte)0x36);
        listaNumerosModulos.add((byte)0x37);
        listaNumerosModulos.add((byte)0x38);
        listaNumerosModulos.add((byte)0x39);
        tipoMensaje.add("Cambio del modo de operacion del UPS. Se esta usando el sistema de respaldo de energia en el hogar.");
        tipoMensaje.add("Alerta de presencia de fuego en un ambiente del hogar.");
        tipoMensaje.add("Alerta de presencia de humo en un ambiente del hogar.");
        tipoMensaje.add("Alerta de presencia de gas en un ambiente del hogar.");
        tipoMensaje.add("Alerta de bajo nivel en el tanque de agua del hogar.");
        tipoMensaje.add("Alerta de deteccion de movimiento en los ambientes del hogar, alarma activada.");
        tipoMensaje.add("Alerta de apertura de puertas forzada en el hogar, alarma activada.");
        tipoMensaje.add("Alerta de deteccion de precipitacion fluvial en el hogar, alarma activada.");
        restablecerModificacionComandoAlarmaBD();
        establecerEstadoComandoAlarmaBD(false);
        restablecerModificacionComandoActCocinaBD();
        establecerEstadoComandoAct1CocinaBD(false);
        establecerEstadoComandoAct2CocinaBD(false);
        restablecerModificacionComandoActExteriorBD();
        establecerEstadoComandoAct1ExteriorBD(false);
        establecerEstadoComandoAct2ExteriorBD(false);
        restablecerModificacionComandoActHabitacionBD();
        establecerEstadoComandoAct1HabitacionBD(false);
        establecerEstadoComandoAct2HabitacionBD(false);
        restablecerModificacionComandoModoOpCocinaBD();
        restablecerModificacionComandoModoOpExteriorBD();
        restablecerModificacionComandoModoOpHabitacionBD();
        restablecerComandoModoOperacionCocinaBD();
        restablecerComandoModoOperacionExteriorBD();
        restablecerComandoModoOperacionHabitacionBD();
        establecerModoOperacionCocina(true,false,false);
        establecerModoOperacionExterior(true,false,false);
        establecerModoOperacionHabitacion(true,false,false);
        establecerEstadoAlarmaCoordinadorBD(false);
        establecerHabilitacionAlarmaCoordinadorBD(true);
        establecerActivacionModuloCocinaDB(false);
        establecerActivacionModuloExteriorDB(false);
        establecerActivacionModuloHabitacionDB(false);
        restablecerInicioSesion();
    }

    //Metodo que inicia los valores por defecto en la base de datos del modulo cocina
    public void iniciarValoresPorDefectoCocina(){
        restablecerModificacionComandoActCocinaBD();
        establecerEstadoComandoAct1CocinaBD(false);
        establecerEstadoComandoAct2CocinaBD(false);
        restablecerModificacionComandoModoOpCocinaBD();
        restablecerComandoModoOperacionCocinaBD();
        establecerModoOperacionCocina(true,false,false);
        establecerActivacionModuloCocinaDB(false);
    }

    //Metodo que inicia los valores por defecto en la base de datos del modulo exterior
    public void iniciarValoresPorDefectoExterior(){
        restablecerModificacionComandoActExteriorBD();
        establecerEstadoComandoAct1ExteriorBD(false);
        establecerEstadoComandoAct2ExteriorBD(false);
        restablecerModificacionComandoModoOpExteriorBD();
        restablecerComandoModoOperacionExteriorBD();
        establecerModoOperacionExterior(true,false,false);
        establecerActivacionModuloExteriorDB(false);
    }

    //Metodo que inicia los valores por defecto en la base de datos del modulo habitacion
    public void iniciarValoresPorDefectoHabitacion(){
        restablecerModificacionComandoActHabitacionBD();
        establecerEstadoComandoAct1HabitacionBD(false);
        establecerEstadoComandoAct2HabitacionBD(false);
        restablecerModificacionComandoModoOpHabitacionBD();
        restablecerComandoModoOperacionHabitacionBD();
        establecerModoOperacionHabitacion(true,false,false);
        establecerActivacionModuloHabitacionDB(false);
    }

    //Metodo que verifica las modificaciones realizadas a traves de la aplicacion web o movil
    //y las adiciona a la lista de tareas a pendientes del programador de tareas
    public boolean verificarModificaciones(){
        boolean respuesta=false;
        if(obtenerModificacionComandoAlarmaBD()){
            listaModificaciones.add((byte)0x00);
            restablecerModificacionComandoAlarmaBD();
            respuesta=true;
        }
        if(obtenerModificacionComandoActCocinaBD()){
            listaModificaciones.add((byte)0x01);
            restablecerModificacionComandoActCocinaBD();
            respuesta=true;
        }
        if(obtenerModificacionComandoActExteriorBD()){
            listaModificaciones.add((byte)0x02);
            restablecerModificacionComandoActExteriorBD();
            respuesta=true;
        }
        if(obtenerModificacionComandoActHabitacionBD()){
            listaModificaciones.add((byte)0x03);
            restablecerModificacionComandoActHabitacionBD();
            respuesta=true;
        }
        if(obtenerModificacionComandoModoOpCocinaBD()){
            listaModificaciones.add((byte)0x04);
            restablecerModificacionComandoModoOpCocinaBD();
            respuesta=true;
        }
        if(obtenerModificacionComandoModoOpExteriorBD()){
            listaModificaciones.add((byte)0x05);
            restablecerModificacionComandoModoOpExteriorBD();
            respuesta=true;
        }
        if(obtenerModificacionComandoModoOpHabitacionBD()){
            listaModificaciones.add((byte)0x06);
            restablecerModificacionComandoModoOpHabitacionBD();
            respuesta=true;
        }
        return respuesta;
    }

    //Metodo que interpreta un comando recibido desde el modulo coordinador o un modulo de adquisicion de datos
    public void interpretarComando(){
        comandoActual=comandosRecibidos.pollFirst();
        if(comandoActual.size()>1){
            byte comando=comandoActual.get(1);
            switch(comando){
                case (byte)0x41:
                    registrarModulo();
                    break;
                case (byte)0x43:
                    registrarDatosModulo();
                    break;
                case (byte)0x44:
                    registrarEstablecimientoSalidasModulo();
                    break;
                case (byte)0x46:
                    registrarEstablecimientoEstadoAlarma();
                    break;
                case (byte)0x47:
                    registrarEstablecimientoEnvioSMS();
                    break;
            }
        }
    }

    //Metodo que realiza el registro de un nuevo modulo recolector de datos al sistema
    public void registrarModulo(){
        byte moduloOrigen=comandoActual.get(0);
        if(moduloOrigen==((byte)0x30)){
            System.out.println("registrare nuevo modulo");
            if(listaNumerosModulos.size()>0){
                byte numeroModuloAsignar=listaNumerosModulos.getFirst();
                byte tipoModuloOrigen=comandoActual.get(2);
                enviarComandoRegistro(numeroModuloAsignar,((byte)(tipoModuloOrigen-0x20)));
            }
        }
        else{
            System.out.println("confirmare registro nuevo modulo");
            byte numeroModuloAsignar=listaNumerosModulos.pollFirst();
            byte tipoModuloOrigen=comandoActual.get(2);
            modulosRegistrados.add(new Modulo(numeroModuloAsignar,tipoModuloOrigen,(byte)0x01,false,true));
            switch(tipoModuloOrigen){
                case (byte)0x21:
                    establecerActivacionModuloHabitacionDB(true);
                    escribirNotificacion("El modulo habitacion se ha integrado correctamente al sistema","info");
                    break;
                case (byte)0x22:
                    establecerActivacionModuloCocinaDB(true);
                    escribirNotificacion("El modulo cocina se ha integrado correctamente al sistema","info");
                    break;
                case (byte)0x23:
                    establecerActivacionModuloExteriorDB(true);
                    escribirNotificacion("El modulo exterior se ha integrado correctamente al sistema","info");
                    break;
            }
            System.out.println("tipo de modulo: "+tipoModuloOrigen);
            System.out.println("numero de modulo: "+numeroModuloAsignar);
            limpiarTareasModulos();
            limpiarContadoresDesconexionModulos();
            planificarTareaModulo();
        }
    }

    //Metodo que registra nuevos datos provenientes de un modulo de adquisicion de datos
    public void registrarDatosModulo(){
        int temperaturaModulo=0;
        int humedadModulo=0;
        int gasModulo=0;
        int humoModulo=0;
        int nivelAguaModulo=0;
        int luzModulo=0;
        int consumoElectricoModulo=0;
        int consumoAguaModulo=0;
        boolean lluviaModulo=false;
        boolean fuegoModulo=false;
        boolean movimientoModulo=false;
        boolean magneticoModulo=false;
        boolean act1Modulo=false;
        boolean act2Modulo=false;
        byte moduloOrigen=comandoActual.get(0);
        int indiceModuloDetectado=-1;
        for(int i=0;i<modulosRegistrados.size();i++){
            if((modulosRegistrados.get(i).obtenerNumeroAsignado())==moduloOrigen){
                indiceModuloDetectado=i;
                break;
            }
        }
        if(indiceModuloDetectado!=-1){
            System.out.println("Registre datos del modulo: "+modulosRegistrados.get(indiceModuloDetectado).obtenerNumeroAsignado());
            byte tipoModuloOrigen=modulosRegistrados.get(indiceModuloDetectado).obtenerTipoModulo();
            byte modoOperacionModuloOrigen=modulosRegistrados.get(indiceModuloDetectado).obtenerModoOperacion();
            boolean estadoControlesModulo=modulosRegistrados.get(indiceModuloDetectado).obtenerControles();
            for(int i=2;i<comandoActual.size();i++){
                switch(comandoActual.get(i)){
                    case (byte)0x61:
                        humoModulo=calculoExponencialSensorHumo(comandoActual.get(i+1)&0xFF);
                        i+=1;
                        break;
                    case (byte)0x62:
                        gasModulo=calculoExponencialSensorGas(comandoActual.get(i+1)&0xFF);
                        i+=1;
                        break;
                    case (byte)0x63:
                        luzModulo=calculoLogaritmicoSensorLuz(comandoActual.get(i+1)&0xFF);
                        i+=1;
                        break;
                    case (byte)0x64:
                        consumoElectricoModulo=((comandoActual.get(i+1)&0xFF)<<8)|comandoActual.get(i+2)&0xFF;
                        i+=2;
                        break;
                    case (byte)0x65:
                        consumoAguaModulo=((comandoActual.get(i+1)&0xFF)<<8|(comandoActual.get(i+2)&0xFF))&0xFF;
                        i+=2;
                        break;
                    case (byte)0x66:
                        temperaturaModulo=comandoActual.get(i+1)&0xFF;
                        humedadModulo=comandoActual.get(i+2)&0xFF;
                        i+=2;
                        break;
                    case (byte)0x67:
                        if(comandoActual.get(i+1)==(byte)0xFF){
                            lluviaModulo=true;
                        }
                        else{
                            lluviaModulo=false;
                        }
                        i+=1;
                        break;
                    case (byte)0x68:
                        if(comandoActual.get(i+1)==(byte)0xFF){
                            fuegoModulo=true;
                        }
                        else{
                            fuegoModulo=false;
                        }
                        i+=1;
                        break;
                    case (byte)0x69:
                        if(comandoActual.get(i+1)==(byte)0xFF){
                            magneticoModulo=false;
                        }
                        else{
                            magneticoModulo=true;
                        }
                        i+=1;
                        break;
                    case (byte)0x6A:
                        nivelAguaModulo=((largoTanque*anchoTanque)*(21-comandoActual.get(i+1)&0xFF))/1000;
                        i+=1;
                        break;
                    case (byte)0x6B:
                        if((comandoActual.get(i+1)&0b00000001)==0b00000001){
                            act1Modulo=true;
                        }
                        else{
                            act1Modulo=false;
                        }
                        if((comandoActual.get(i+1)&0b00000010)==0b00000010){
                            act2Modulo=true;
                        }
                        else{
                            act2Modulo=false;
                        }
                        i+=1;
                        break;
                    case (byte)0x6C:
                        if((int)(comandoActual.get(i+1)&0xFF)>(int)(0xA0&0xFF)){
                            movimientoModulo=true;
                        }
                        else{
                            movimientoModulo=false;
                        }
                        i+=1;
                        break;
                }
            }
            //Procedimiento que establece los nuevos datos en la base de datos segun  el tipo de modulo de adquisicion de datos
            //adicionalmente se controlan los valores de acuerdo a los limites establecidos y los modos de operacion del modulo
            //desencadenando notificaciones o acciones especificas de cada modulo
            switch(tipoModuloOrigen){
                case (byte)0x21:
                    establecerNuevosDatosHabitacionDB(temperaturaModulo,humedadModulo,movimientoModulo,magneticoModulo,humoModulo,act1Modulo,act2Modulo,estadoControlesModulo);
                    if(modoOperacionModuloOrigen==(byte)0x02){
                        if(humoModulo>limiteMaximoHumo){
                            if(!modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaHumo()){
                                escribirNotificacion("En el ambiente de habitacion se ha detectado una concentracion peligrosa de humo","peligro");
                                modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaHumo(true);
                            }
                        }
                        else{
                            if(modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaHumo()){
                                modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaHumo(false);
                            }
                        }
                        if(temperaturaModulo>limiteMaximoTemperatura){
                            if(!((act1Modulo==true)&&(act2Modulo==false))){
                                escribirNotificacion("En el ambiente de habitacion se ha detectado una temperatura elevada, se encendera la ventilacion","info");
                                listaTareas.addFirst(new Tarea((byte)0x02,true,false));
                            }
                        }
                        else{
                            if(temperaturaModulo<limiteMinimoTemperatura){
                                if(!((act1Modulo==false)&&(act2Modulo==true))){
                                    escribirNotificacion("En el ambiente de habitacion se ha detectado una temperatura baja, se encendera la calefaccion","info");
                                    listaTareas.addFirst(new Tarea((byte)0x02,false,true));
                                }
                            }
                            else{
                                if(!((act1Modulo==false)&&(act2Modulo==false))){
                                    listaTareas.addFirst(new Tarea((byte)0x02,false,false));
                                }
                            }
                        }
                    }
                    else{
                        if(modoOperacionModuloOrigen==(byte)0x03){
                            if(humoModulo>limiteMaximoHumo){
                                if(!modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaHumo()){
                                    escribirNotificacion("En el ambiente de habitacion se ha detectado una concentracion peligrosa de humo","peligro");
                                    modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaHumo(true);
                                    listaTareas.addFirst(new Tarea((byte)0x04,(byte)0x02));
                                    listaTareas.addFirst(new Tarea((byte)0x03,true));
                                }
                            }
                            else{
                                if(modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaHumo()){
                                    modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaHumo(false);
                                }
                            }
                            if(movimientoModulo){
                                if(!modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaMovimiento()){
                                    escribirNotificacion("En el ambiente de habitacion se ha detectado movimiento","peligro");
                                    modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaMovimiento(true);
                                    listaTareas.addFirst(new Tarea((byte)0x04,(byte)0x05));
                                    listaTareas.addFirst(new Tarea((byte)0x03,true));
                                }
                            }
                            else{
                                if(modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaMovimiento()){
                                    modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaMovimiento(false);
                                }
                            }
                            if(magneticoModulo){
                                if(!modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaMagnetico()){
                                    escribirNotificacion("En el ambiente de habitacion se ha detectado la apertura forzada de la puerta","peligro");
                                    modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaMagnetico(true);
                                    listaTareas.addFirst(new Tarea((byte)0x04,(byte)0x06));
                                    listaTareas.addFirst(new Tarea((byte)0x03,true));
                                }
                            }
                            else{
                                if(modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaMagnetico()){
                                    modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaMagnetico(false);
                                }
                            }
                        }
                    }
                    break;
                case (byte)0x22:
                    establecerNuevosDatosCocinaDB(temperaturaModulo,humedadModulo,gasModulo,humoModulo,fuegoModulo,act1Modulo,act2Modulo,estadoControlesModulo);
                    if(modoOperacionModuloOrigen==(byte)0x02){
                        if(humoModulo>limiteMaximoHumo){
                            if(!modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaHumo()){
                                escribirNotificacion("En el ambiente de cocina se ha detectado una concentracion peligrosa de humo","peligro");
                                modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaHumo(true);
                            }
                        }
                        else{
                            if(modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaHumo()){
                                modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaHumo(false);
                            }
                        }
                        if(gasModulo>limiteMaximoGas){
                            if(!modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaGas()){
                                escribirNotificacion("En el ambiente de cocina se ha detectado una concentracion peligrosa de gas","peligro");
                                modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaGas(true);
                            }
                        }
                        else{
                            if(modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaGas()){
                                modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaGas(false);
                            }
                        }
                        if(fuegoModulo){
                            if(!modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaFuego()){
                                escribirNotificacion("En el ambiente de cocina se ha detectado fuego","peligro");
                                modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaFuego(true);
                            }
                        }
                        else{
                            if(modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaFuego()){
                                modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaFuego(false);
                            }
                        }
                        if(temperaturaModulo>limiteMaximoTemperatura){
                            if(!((act1Modulo==true)&&(act2Modulo==false))){
                                escribirNotificacion("En el ambiente de cocina se ha detectado una temperatura elevada, se encendera la ventilacion","info");
                                listaTareas.addFirst(new Tarea((byte)0x00,true,false));
                            }
                        }
                        else{
                            if(temperaturaModulo<limiteMinimoTemperatura){
                                if(!((act1Modulo==true)&&(act2Modulo==false))){
                                    escribirNotificacion("En el ambiente de cocina se ha detectado una temperatura baja, se encendera la calefaccion","info");
                                    listaTareas.addFirst(new Tarea((byte)0x00,true,false));
                                }
                            }
                            else{
                                if(!((act1Modulo==false)&&(act2Modulo==false))){
                                    listaTareas.addFirst(new Tarea((byte)0x00,false,false));
                                }
                            }
                        }
                    }
                    else{
                        if(modoOperacionModuloOrigen==(byte)0x03){
                            if(humoModulo>limiteMaximoHumo){
                                if(!modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaHumo()){
                                    escribirNotificacion("En el ambiente de cocina se ha detectado una concentracion peligrosa de humo","peligro");
                                    modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaHumo(true);
                                    listaTareas.addFirst(new Tarea((byte)0x04,(byte)0x02));
                                    listaTareas.addFirst(new Tarea((byte)0x03,true));
                                }
                            }
                            else{
                                if(modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaHumo()){
                                    modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaHumo(false);
                                }
                            }
                            if(gasModulo>limiteMaximoGas){
                                if(!modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaGas()){
                                    escribirNotificacion("En el ambiente de cocina se ha detectado una concentracion peligrosa de gas","peligro");
                                    modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaGas(true);
                                    listaTareas.addFirst(new Tarea((byte)0x04,(byte)0x03));
                                    listaTareas.addFirst(new Tarea((byte)0x03,true));
                                }
                            }
                            else{
                                if(modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaGas()){
                                    modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaGas(false);
                                }
                            }
                            if(fuegoModulo){
                                if(!modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaFuego()){
                                    escribirNotificacion("En el ambiente de cocina se ha detectado fuego","peligro");
                                    modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaFuego(true);
                                    listaTareas.addFirst(new Tarea((byte)0x04,(byte)0x01));
                                    listaTareas.addFirst(new Tarea((byte)0x03,true));
                                }
                            }
                            else{
                                if(modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaFuego()){
                                    modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaFuego(false);
                                }
                            }
                        }
                    }
                    break;
                case (byte)0x23:
                    establecerNuevosDatosExteriorDB(lluviaModulo,nivelAguaModulo,movimientoModulo,luzModulo,consumoElectricoModulo,consumoAguaModulo,act1Modulo,act2Modulo,estadoControlesModulo);
                    if(modoOperacionModuloOrigen==(byte)0x02){
                        if(nivelAguaModulo<limiteMinimoNivelAgua){
                            if(!modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaNivelAgua()){
                                escribirNotificacion("En el ambiente exterior se ha detectado un bajo nivel de agua en el tanque","alerta");
                                modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaNivelAgua(true);
                            }
                        }
                        else{
                            if(modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaNivelAgua()){
                                modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaNivelAgua(false);
                            }
                        }
                        if(luzModulo<limiteMinimoLuz){
                            if(!modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaLuz()){
                                escribirNotificacion("En el ambiente exterior se ha detectado un bajo nivel de luz, se encendera la iluminacion","alerta");
                                modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaLuz(true);
                                listaTareas.addFirst(new Tarea((byte)0x01,true,true));
                            }
                        }
                        else{
                            if(modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaLuz()){
                                modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaLuz(false);
                                listaTareas.addFirst(new Tarea((byte)0x01,false,false));
                            }
                        }
                    }
                    else{
                        if(modoOperacionModuloOrigen==(byte)0x03){
                            if(nivelAguaModulo<limiteMinimoNivelAgua){
                                if(!modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaNivelAgua()){
                                    escribirNotificacion("En el ambiente exterior se ha detectado un bajo nivel de agua en el tanque","alerta");
                                    modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaNivelAgua(true);
                                    listaTareas.addFirst(new Tarea((byte)0x04,(byte)0x04));
                                }
                            }
                            else{
                                if(modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaNivelAgua()){
                                    modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaNivelAgua(false);
                                }
                            }
                            if(lluviaModulo){
                                if(!modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaLluvia()){
                                    escribirNotificacion("En el ambiente exterior se ha detectado precipitacion fluvial","alerta");
                                    modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaLluvia(true);
                                    listaTareas.addFirst(new Tarea((byte)0x04,(byte)0x07));
                                }
                            }
                            else{
                                if(modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaLluvia()){
                                    modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaLluvia(false);
                                }
                            }
                            if(movimientoModulo){
                                if(!modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaMovimiento()){
                                    escribirNotificacion("En el ambiente exterior se ha detectado movimiento","peligro");
                                    modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaMovimiento(true);
                                    listaTareas.addFirst(new Tarea((byte)0x04,(byte)0x05));
                                    listaTareas.addFirst(new Tarea((byte)0x03,true));
                                }
                            }
                            else{
                                if(modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaMovimiento()){
                                    modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaMovimiento(false);
                                }
                            }
                            if(luzModulo<limiteMinimoLuz){
                                if(!modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaLuz()){
                                    escribirNotificacion("En el ambiente exterior se ha detectado un bajo nivel de luz, se encendera la iluminacion","alerta");
                                    modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaLuz(true);
                                    listaTareas.addFirst(new Tarea((byte)0x01,true,true));
                                }
                            }
                            else{
                                if(modulosRegistrados.get(indiceModuloDetectado).obtenerAlarmaLuz()){
                                    modulosRegistrados.get(indiceModuloDetectado).establecerAlarmaLuz(false);
                                    listaTareas.addFirst(new Tarea((byte)0x01,false,false));
                                }
                            }
                        }
                    }
                    break;
            }
            modulosRegistrados.get(indiceModuloDetectado).establecerRespuestaPendiente(false);
            modulosRegistrados.get(indiceModuloDetectado).establecerContadorDesconexion((byte)0x00);
            modulosRegistrados.get(indiceModuloDetectado).establecerContadorQuantum((byte)0x00);
        }
    }

    //Metodo que registra la respuesta al comando de establecimiento de salidas enviado por un modulo de adquisicion de datos
    public void registrarEstablecimientoSalidasModulo(){
        byte moduloOrigen=comandoActual.get(0);
        int indiceModuloDetectado=-1;
        for(int i=0;i<modulosRegistrados.size();i++){
            if((modulosRegistrados.get(i).obtenerNumeroAsignado())==moduloOrigen){
                indiceModuloDetectado=i;
                break;
            }
        }
        if(indiceModuloDetectado!=-1){
            modulosRegistrados.get(indiceModuloDetectado).establecerControles(true);
            modulosRegistrados.get(indiceModuloDetectado).establecerRespuestaPendiente(false);
            modulosRegistrados.get(indiceModuloDetectado).establecerContadorDesconexion((byte)0x00);
            modulosRegistrados.get(indiceModuloDetectado).establecerContadorQuantum((byte)0x00);
        }
    }

    //Metodo que registra la respuesta al comando de establecimiento de alarma enviado por el modulo coordinador
    public void registrarEstablecimientoEstadoAlarma(){
        if(comandoActual.get(2)==(byte)0xFF){
            establecerEstadoAlarmaCoordinadorBD(true);
            escribirNotificacion("Se ha activado la alarma sonora principal del sistema","peligro");
        }
        else{
            establecerEstadoAlarmaCoordinadorBD(false);
            escribirNotificacion("Se ha desactivado la alarma sonora principal del sistema","info");
        }
        establecerHabilitacionAlarmaCoordinadorBD(true);
    }

    //Metodo que registra la respuesta al comando de envio de SMS enviado por el modulo coordinador
    public void registrarEstablecimientoEnvioSMS(){
        String nuevoMensaje="Se ha enviado el siguiente SMS al administrador: "+tipoMensaje.get((int)(comandoActual.get(2))&0xFF);
        escribirNotificacion(nuevoMensaje,"info");
    }

    //Metodo que interpreta las modificaciones generadas desde la aplicacion web y movil para ser adicionadas como tareas en el programador de tareas
    public void interpretarModificaciones(){
        byte modificacion=listaModificaciones.pollFirst();
        switch(modificacion){
            case (byte)0x00:
                boolean estadoAlarmaComando=obtenerEstadoComandoAlarmaBD();
                listaTareas.addFirst(new Tarea((byte)0x03,estadoAlarmaComando));
                break;
            case (byte)0x01:
                boolean actuador1CocinaEstado=obtenerEstadoComandoAct1CocinaBD();
                boolean actuador2CocinaEstado=obtenerEstadoComandoAct2CocinaBD();
                listaTareas.addFirst(new Tarea((byte)0x00,actuador1CocinaEstado,actuador2CocinaEstado));
                break;
            case (byte)0x02:
                boolean actuador1ExteriorEstado=obtenerEstadoComandoAct1ExteriorBD();
                boolean actuador2ExteriorEstado=obtenerEstadoComandoAct2ExteriorBD();
                listaTareas.addFirst(new Tarea((byte)0x01,actuador1ExteriorEstado,actuador2ExteriorEstado));
                break;
            case (byte)0x03:
                boolean actuador1HabitacionEstado=obtenerEstadoComandoAct1HabitacionBD();
                boolean actuador2HabitacionEstado=obtenerEstadoComandoAct2HabitacionBD();
                listaTareas.addFirst(new Tarea((byte)0x02,actuador1HabitacionEstado,actuador2HabitacionEstado));
                break;
            case (byte)0x04:
                byte modoOperacionCocina=obtenerComandoModoOperacionCocinaBD();
                System.out.println("Modo de operacion a activar Cocina: "+modoOperacionCocina);
                if(modoOperacionCocina==(byte)0x01){
                    establecerModoOperacionCocina(true,false,false);
                    escribirNotificacion("se ha configurado el modulo cocina en Modo Operacion Normal","info");
                }
                else{
                    if(modoOperacionCocina==(byte)0x02){
                        establecerModoOperacionCocina(false,true,false);
                        escribirNotificacion("se ha configurado el modulo cocina en Modo Operacion Semiautomatico","info");
                    }
                    else{
                        if(modoOperacionCocina==(byte)0x03){
                            establecerModoOperacionCocina(false,false,true);
                            escribirNotificacion("se ha configurado el modulo cocina en Modo Operacion Armado","info");
                        }
                    }
                }
                int indiceModuloCocinaDetectado=-1;
                for(int i=0;i<modulosRegistrados.size();i++){
                    if((modulosRegistrados.get(i).obtenerTipoModulo())==(byte)0x22){
                        indiceModuloCocinaDetectado=i;
                        break;
                    }
                }
                if(indiceModuloCocinaDetectado!=-1){
                    modulosRegistrados.get(indiceModuloCocinaDetectado).establecerModoOperacion(modoOperacionCocina);
                }
                limpiarQuantumsModulos();
                listaTareas.addFirst(new Tarea((byte)0x00,false,false));
                break;
            case (byte)0x05:
                byte modoOperacionExterior=obtenerComandoModoOperacionExteriorBD();
                System.out.println("Modo de operacion a activar Exterior: "+modoOperacionExterior);
                if(modoOperacionExterior==(byte)0x01){
                    establecerModoOperacionExterior(true,false,false);
                    escribirNotificacion("se ha configurado el modulo exterior en Modo Operacion Normal","info");
                }
                else{
                    if(modoOperacionExterior==(byte)0x02){
                        establecerModoOperacionExterior(false,true,false);
                        escribirNotificacion("se ha configurado el modulo exterior en Modo Operacion Semiautomatico","info");
                    }
                    else{
                        if(modoOperacionExterior==(byte)0x03){
                            establecerModoOperacionExterior(false,false,true);
                            escribirNotificacion("se ha configurado el modulo exterior en Modo Operacion Armado","info");
                        }
                    }
                }
                int indiceModuloExteriorDetectado=-1;
                for(int i=0;i<modulosRegistrados.size();i++){
                    if((modulosRegistrados.get(i).obtenerTipoModulo())==(byte)0x23){
                        indiceModuloExteriorDetectado=i;
                        break;
                    }
                }
                if(indiceModuloExteriorDetectado!=-1){
                    modulosRegistrados.get(indiceModuloExteriorDetectado).establecerModoOperacion(modoOperacionExterior);
                }
                limpiarQuantumsModulos();
                listaTareas.addFirst(new Tarea((byte)0x01,false,false));
                break;
            case (byte)0x06:
                byte modoOperacionHabitacion=obtenerComandoModoOperacionHabitacionBD();
                System.out.println("Modo de operacion a activar Habitacion: "+modoOperacionHabitacion);
                if(modoOperacionHabitacion==(byte)0x01){
                    establecerModoOperacionHabitacion(true,false,false);
                    escribirNotificacion("se ha configurado el modulo habitacion en Modo Operacion Normal","info");
                }
                else{
                    if(modoOperacionHabitacion==(byte)0x02){
                        establecerModoOperacionHabitacion(false,true,false);
                        escribirNotificacion("se ha configurado el modulo habitacion en Modo Operacion Semiautomatico","info");
                    }
                    else{
                        if(modoOperacionHabitacion==(byte)0x03){
                            establecerModoOperacionHabitacion(false,false,true);
                            escribirNotificacion("se ha configurado el modulo habitacion en Modo Operacion Armado","info");
                        }
                    }
                }
                int indiceModuloHabitacionDetectado=-1;
                for(int i=0;i<modulosRegistrados.size();i++){
                    if((modulosRegistrados.get(i).obtenerTipoModulo())==(byte)0x21){
                        indiceModuloHabitacionDetectado=i;
                        break;
                    }
                }
                if(indiceModuloHabitacionDetectado!=-1){
                    modulosRegistrados.get(indiceModuloHabitacionDetectado).establecerModoOperacion(modoOperacionHabitacion);
                }
                limpiarQuantumsModulos();
                listaTareas.addFirst(new Tarea((byte)0x02,false,false));
                break;
        }
    }

    //Metodo que ejecuta una tarea de la lista de tareas controlada por el programador de tareas
    public void ejecutarTarea(){
        Tarea tareaAtendida=listaTareas.pollFirst();
        byte tareaActual=tareaAtendida.obtenerCodigoTarea();
        switch(tareaActual){
            case (byte)0x00:
                limpiarQuantumsModulos();
                boolean estadoAct1Cocina=tareaAtendida.obtenerValorLogicoPrincipal();
                boolean estadoAct2Cocina=tareaAtendida.obtenerValorLogicoSecundario();
                int indiceModuloCocinaDetectado=-1;
                for(int i=0;i<modulosRegistrados.size();i++){
                    if((modulosRegistrados.get(i).obtenerTipoModulo())==(byte)0x22){
                        indiceModuloCocinaDetectado=i;
                        break;
                    }
                }
                if(indiceModuloCocinaDetectado!=-1){
                    modulosRegistrados.get(indiceModuloCocinaDetectado).establecerRespuestaPendiente(true);
                    modulosRegistrados.get(indiceModuloCocinaDetectado).establecerControles(false);
                    enviarComandoEstablecerSalidas(estadoAct1Cocina,estadoAct2Cocina,(byte)0x02);
                }
                break;
            case (byte)0x01:
                limpiarQuantumsModulos();
                boolean estadoAct1Exterior=tareaAtendida.obtenerValorLogicoPrincipal();
                boolean estadoAct2Exterior=tareaAtendida.obtenerValorLogicoSecundario();
                int indiceModuloExteriorDetectado=-1;
                for(int i=0;i<modulosRegistrados.size();i++){
                    if((modulosRegistrados.get(i).obtenerTipoModulo())==(byte)0x23){
                        indiceModuloExteriorDetectado=i;
                        break;
                    }
                }
                if(indiceModuloExteriorDetectado!=-1){
                    modulosRegistrados.get(indiceModuloExteriorDetectado).establecerRespuestaPendiente(true);
                    modulosRegistrados.get(indiceModuloExteriorDetectado).establecerControles(false);
                    enviarComandoEstablecerSalidas(estadoAct1Exterior,estadoAct2Exterior,(byte)0x03);
                }
                break;
            case (byte)0x02:
                limpiarQuantumsModulos();
                boolean estadoAct1Habitacion=tareaAtendida.obtenerValorLogicoPrincipal();
                boolean estadoAct2Habitacion=tareaAtendida.obtenerValorLogicoSecundario();
                int indiceModuloHabitacionDetectado=-1;
                for(int i=0;i<modulosRegistrados.size();i++){
                    if((modulosRegistrados.get(i).obtenerTipoModulo())==(byte)0x21){
                        indiceModuloHabitacionDetectado=i;
                        break;
                    }
                }
                if(indiceModuloHabitacionDetectado!=-1){
                    modulosRegistrados.get(indiceModuloHabitacionDetectado).establecerRespuestaPendiente(true);
                    modulosRegistrados.get(indiceModuloHabitacionDetectado).establecerControles(false);
                    enviarComandoEstablecerSalidas(estadoAct1Habitacion,estadoAct2Habitacion,(byte)0x01);
                }
                break;
            case (byte)0x03:
                boolean estadoAlarmaNuevo=tareaAtendida.obtenerValorLogicoPrincipal();
                enviarComandoEstadoAlarmaCoordinador(estadoAlarmaNuevo);
                establecerHabilitacionAlarmaCoordinadorBD(false);
                break;
            case (byte)0x04:
                byte codigoMensaje=(byte)tareaAtendida.obtenerValorNumericoPrincipal();
                enviarComandoMensajeAlarmaCoordinador(codigoMensaje);
                break;
            case (byte)0x05:
                System.out.println("datos del modulo cocina");
                int indiceModuloCocina=-1;
                for(int i=0;i<modulosRegistrados.size();i++){
                    if((modulosRegistrados.get(i).obtenerTipoModulo())==(byte)0x22){
                        indiceModuloCocina=i;
                        break;
                    }
                }
                if(indiceModuloCocina!=-1){
                    if((modulosRegistrados.get(indiceModuloCocina).obtenerContadorDesconexion())>=limiteMaximoContadorDesconexion){
                        System.out.println("retirare modulo cocina");
                        byte numeroLibreCocina=modulosRegistrados.get(indiceModuloCocina).obtenerNumeroAsignado();
                        listaNumerosModulos.add(numeroLibreCocina);
                        listaNumerosModulos.sort(Comparator.naturalOrder());
                        modulosRegistrados.remove(indiceModuloCocina);
                        limpiarModificacionesCocina();
                        limpiarTareasCocina();
                        iniciarValoresPorDefectoCocina();

                        planificarTareaModulo();
                        escribirNotificacion("El modulo cocina se ha desconectado del sistema","alerta");
                    }
                    else{
                        if(modulosRegistrados.get(indiceModuloCocina).obtenerContadorQuantum()>=quantumPlanificador){
                            if(modulosRegistrados.get(indiceModuloCocina).obtenerRespuestaPendiente()){
                                System.out.println("Incrementare contador desconexion cocina");
                                modulosRegistrados.get(indiceModuloCocina).incrementarContadorDesconexion();
                            }
                            System.out.println("Solicitare datos del modulo cocina");
                            enviarComandoObtenerDatos((byte)0x02);
                            modulosRegistrados.get(indiceModuloCocina).establecerRespuestaPendiente(true);
                            modulosRegistrados.get(indiceModuloCocina).establecerControles(true);
                            planificarTareaModulo();
                        }
                        else{
                            System.out.println("Incrementare quantum cocina");
                            modulosRegistrados.get(indiceModuloCocina).incrementarContadorQuantum();
                            listaTareas.add(new Tarea((byte)0x05));
                        }
                    }
                }
                break;
            case (byte)0x06:
                System.out.println("datos del modulo exterior");
                int indiceModuloExterior=-1;
                for(int i=0;i<modulosRegistrados.size();i++){
                    if((modulosRegistrados.get(i).obtenerTipoModulo())==(byte)0x23){
                        indiceModuloExterior=i;
                        break;
                    }
                }
                if(indiceModuloExterior!=-1){
                    if((modulosRegistrados.get(indiceModuloExterior).obtenerContadorDesconexion())>=limiteMaximoContadorDesconexion){
                        System.out.println("Retirare modulo exterior");
                        byte numeroLibreExterior=modulosRegistrados.get(indiceModuloExterior).obtenerNumeroAsignado();
                        listaNumerosModulos.add(numeroLibreExterior);
                        listaNumerosModulos.sort(Comparator.naturalOrder());
                        modulosRegistrados.remove(indiceModuloExterior);
                        limpiarModificacionesExterior();
                        limpiarTareasExterior();
                        iniciarValoresPorDefectoExterior();
                        planificarTareaModulo();
                        escribirNotificacion("El modulo exterior se ha desconectado del sistema","alerta");
                    }
                    else{
                        if(modulosRegistrados.get(indiceModuloExterior).obtenerContadorQuantum()>=quantumPlanificador){
                            if(modulosRegistrados.get(indiceModuloExterior).obtenerRespuestaPendiente()){
                                modulosRegistrados.get(indiceModuloExterior).incrementarContadorDesconexion();
                            }
                            System.out.println("Solicitare datos del modulo exterior");
                            enviarComandoObtenerDatos((byte)0x03);
                            modulosRegistrados.get(indiceModuloExterior).establecerRespuestaPendiente(true);
                            modulosRegistrados.get(indiceModuloExterior).establecerControles(true);
                            planificarTareaModulo();
                        }
                        else{
                            modulosRegistrados.get(indiceModuloExterior).incrementarContadorQuantum();
                            listaTareas.add(new Tarea((byte)0x06));
                        }
                    }
                }
                break;
            case (byte)0x07:
                System.out.println("datos del modulo habitacion");
                int indiceModuloHabitacion=-1;
                for(int i=0;i<modulosRegistrados.size();i++){
                    if((modulosRegistrados.get(i).obtenerTipoModulo())==(byte)0x21){
                        indiceModuloHabitacion=i;
                        break;
                    }
                }
                if(indiceModuloHabitacion!=-1){
                    if((modulosRegistrados.get(indiceModuloHabitacion).obtenerContadorDesconexion())>=limiteMaximoContadorDesconexion){
                        System.out.println("Retirare modulo habitacion");
                        byte numeroLibreHabitacion=modulosRegistrados.get(indiceModuloHabitacion).obtenerNumeroAsignado();
                        listaNumerosModulos.add(numeroLibreHabitacion);
                        listaNumerosModulos.sort(Comparator.naturalOrder());
                        modulosRegistrados.remove(indiceModuloHabitacion);
                        limpiarModificacionesHabitacion();
                        limpiarTareasHabitacion();
                        iniciarValoresPorDefectoHabitacion();
                        planificarTareaModulo();
                        escribirNotificacion("El modulo habitacion se ha desconectado del sistema","alerta");
                    }
                    else{
                        if(modulosRegistrados.get(indiceModuloHabitacion).obtenerContadorQuantum()>=quantumPlanificador){
                            if(modulosRegistrados.get(indiceModuloHabitacion).obtenerRespuestaPendiente()){
                                System.out.println("Incrementare contador desconexion habitacion");
                                modulosRegistrados.get(indiceModuloHabitacion).incrementarContadorDesconexion();
                            }
                            System.out.println("Solicitare datos del modulo habitacion");
                            enviarComandoObtenerDatos((byte)0x01);
                            modulosRegistrados.get(indiceModuloHabitacion).establecerRespuestaPendiente(true);
                            modulosRegistrados.get(indiceModuloHabitacion).establecerControles(true);
                            planificarTareaModulo();
                        }
                        else{
                            System.out.println("Incrementare Quantum Habitacion");
                            modulosRegistrados.get(indiceModuloHabitacion).incrementarContadorQuantum();
                            listaTareas.add(new Tarea((byte)0x07));
                        }
                    }
                }
                break;
        }
    }

    //Limpia las modificaciones generadas de la lista de modificaciones para el modulo cocina
    public void limpiarModificacionesCocina(){
        for(int i=0;i<listaModificaciones.size();i++){
            if(((listaModificaciones.get(i))==(byte)0x01)||((listaModificaciones.get(i))==(byte)0x04)){
                listaModificaciones.remove(i);
                i--;
            }
        }
    }

    //Limpia las tareas generadas de la lista de tareas para el modulo cocina
    public void limpiarTareasCocina(){
      for(int i=0;i<listaTareas.size();i++){
          if(((listaTareas.get(i).obtenerCodigoTarea())==(byte)0x00)||((listaTareas.get(i).obtenerCodigoTarea())==(byte)0x05)){
              listaTareas.remove(i);
              i--;
          }
      }
    }

    //Limpia las modificaciones generadas de la lista de modificaciones para el modulo exterior
    public void limpiarModificacionesExterior(){
        for(int i=0;i<listaModificaciones.size();i++){
            if(((listaModificaciones.get(i))==(byte)0x02)||((listaModificaciones.get(i))==(byte)0x05)){
                listaModificaciones.remove(i);
                i--;
            }
        }
    }

    //Limpia las tareas generadas de la lista de tareas para el modulo exterior
    public void limpiarTareasExterior(){
        for(int i=0;i<listaTareas.size();i++){
            if(((listaTareas.get(i).obtenerCodigoTarea())==(byte)0x01)||((listaTareas.get(i).obtenerCodigoTarea())==(byte)0x06)){
                listaTareas.remove(i);
                i--;
            }
        }
    }

    //Limpia las modificaciones generadas de la lista de modificaciones para el modulo habitacion
    public void limpiarModificacionesHabitacion(){
        for(int i=0;i<listaModificaciones.size();i++){
            if(((listaModificaciones.get(i))==(byte)0x03)||((listaModificaciones.get(i))==(byte)0x06)){
                listaModificaciones.remove(i);
                i--;
            }
        }
    }

    //Limpia las tareas generadas de la lista de tareas para el modulo habitacion
    public void limpiarTareasHabitacion(){
        for(int i=0;i<listaTareas.size();i++){
            if(((listaTareas.get(i).obtenerCodigoTarea())==(byte)0x02)||((listaTareas.get(i).obtenerCodigoTarea())==(byte)0x07)){
                listaTareas.remove(i);
                i--;
            }
        }
    }

    //Limpia las tareas relacionadas a cualquier modulo de la lista de tareas pendientes
    public void limpiarTareasModulos(){
        for(int i=0;i<listaTareas.size();i++){
            if(((listaTareas.get(i).obtenerCodigoTarea())==(byte)0x05)||((listaTareas.get(i).obtenerCodigoTarea())==(byte)0x06)||((listaTareas.get(i).obtenerCodigoTarea())==(byte)0x07)){
                listaTareas.remove(i);
                i--;
            }
        }
    }

    //Limpia el quantum de los objetos de tipo modulo contenido en la lista de modulos registrados
    public void limpiarQuantumsModulos(){
        for(int i=0;i<modulosRegistrados.size();i++){
            modulosRegistrados.get(i).establecerContadorQuantum((byte)0x00);
        }
    }

    //Limpia el contador de desconexion de los objetos de tipo modulo contenido en la lista de modulos registrados
    public void limpiarContadoresDesconexionModulos(){
        for(int i=0;i<modulosRegistrados.size();i++){
            modulosRegistrados.get(i).establecerContadorDesconexion((byte)0x00);
        }
    }

    //Planifica las tareas de solicitud de datos nuevos acuerdo a la cantidad y tipos de modulos registrados
    public void planificarTareaModulo(){
        if(modulosRegistrados.size()>0){
            quantumPlanificador=6/(modulosRegistrados.size());
            limpiarQuantumsModulos();
            if((contadorPlanificador+1)>(modulosRegistrados.size()-1)){
                contadorPlanificador=0;
            }
            else{
                contadorPlanificador+=1;
            }
            byte tipoModuloPlanificado=modulosRegistrados.get(contadorPlanificador).obtenerTipoModulo();
            switch(tipoModuloPlanificado){
                case (byte)0x21:
                    listaTareas.add(new Tarea((byte)0x07));
                    break;
                case (byte)0x22:
                    listaTareas.add(new Tarea((byte)0x05));
                    break;
                case (byte)0x23:
                    listaTareas.add(new Tarea((byte)0x06));
                    break;
            }
        }
    }

    //Mapea un valor entero de una regresion lineal dada a otra propuesta en los parametros
    public int mapearValorEntero(int numero,int entradaMinimo,int entradaMaximo,int salidaMinimo,int salidaMaximo){
        return ((numero-entradaMinimo)*(salidaMaximo-salidaMinimo)/(entradaMaximo-entradaMinimo)+salidaMinimo);
    }

    //Calcula el valor correspondiente a la unidad fisica ppm desde una lectura de voltaje analogico para el sensor de humo
    public int calculoExponencialSensorHumo(int valor){
        return ((int)(30*Math.exp(0.0172*valor)));
    }

    //Calcula el valor correspondiente a la unidad fisica ppm desde una lectura de voltaje analogico para el sensor de gas
    public int calculoExponencialSensorGas(int valor){
        return ((int)(300*Math.exp(0.0153*valor)));
    }

    //Calcula el valor correspondiente a la unidad fisica lux desde una lectura de voltaje analogico para el sensor de intensidad luminica
    public int calculoLogaritmicoSensorLuz(int valor){
        return ((int)(-892.3*Math.log(valor+0.01)+4945.4));
    }

    //Metodo que envia datos al modulo coordinador a traves del puerto serie, a partir de una lista enlazada
    public void enviarDatosCoordinador(LinkedList<Byte> datosEnviar){
        byte datosEnviarArreglo[]=new byte[datosEnviar.size()];
        for(int i=0;i<datosEnviar.size();i++){
            datosEnviarArreglo[i]=datosEnviar.get(i);
        }
        arduinoSerial.writeBytes(datosEnviarArreglo,datosEnviarArreglo.length);
    }

    //Sobrecarga del metodo que envia datos al modulo coordinador a traves del puerto serie, a partir den arreglo de bytes
    public void enviarDatosCoordinador(byte datosEnviar[]){
        arduinoSerial.writeBytes(datosEnviar,datosEnviar.length);
    }

    //Metodo que envia el comando de registro a un numero de modulo determinado
    public void enviarComandoRegistro(byte numeroAsignado,byte numeroModulo){
        byte comandoRegistro[]=new byte[]{0x30,0x41,(byte)numeroAsignado,(byte)numeroModulo};
        enviarDatosCoordinador(comandoRegistro);
    }

    //Metodo que envia el comando de envio de configuracion a un numero de modulo determinado
    public void enviarComandoEnviarConfiguracion(byte numeroModulo){
        byte comandoEnvioConfiguracion[]=new byte[]{0x30,0x42,(byte)numeroModulo};
        enviarDatosCoordinador(comandoEnvioConfiguracion);
    }

    //Metodo que envia el comando de obtencion de datos a un numero de modulo determinado
    public void enviarComandoObtenerDatos(byte numeroModulo){
        byte comandoDatos[]=new byte[]{0x30,0x43,(byte)numeroModulo};
        enviarDatosCoordinador(comandoDatos);
    }

    //Metodo que envia el comando de establecimiento de salidas a un numero de modulo determinado
    public void enviarComandoEstablecerSalidas(byte estados,byte numeroModulo){
        byte comandoSalidas[]=new byte[]{0x30,0x44,(byte)estados,(byte)numeroModulo};
        enviarDatosCoordinador(comandoSalidas);
    }

    //Sobrecarga del metodo de envio de comando de establecimiento de salidas a un numero determinado, a partir de parametros booleanos
    public void enviarComandoEstablecerSalidas(boolean actuador1Estado,boolean actuador2Estado,byte numeroModulo){
        byte estado=(byte)((byte)(((byte)(actuador2Estado?1:0))<<1)|((byte)(actuador1Estado?1:0)));
        byte comandoSalidas[]=new byte[]{0x30,0x44,(byte)estado,(byte)numeroModulo};
        enviarDatosCoordinador(comandoSalidas);
    }

    //Metodo que envia el comando de modificacion de funciones un numero de modulo determinado
    public void enviarComandoModificarFunciones(byte funciones[],byte numeroModulo){
        byte comandoFunciones[]=new byte[funciones.length+3];
        comandoFunciones[0]=0x30;
        comandoFunciones[1]=0x45;
        for(int i=0;i<funciones.length;i++){
            comandoFunciones[i+2]=funciones[i];
        }
        comandoFunciones[funciones.length+2]=(byte)numeroModulo;
        enviarDatosCoordinador(comandoFunciones);
    }

    //Metodo que envia el comando de cambio de estado de la alarma sonora al modulo coordinador
    public void enviarComandoEstadoAlarmaCoordinador(boolean estado){
        if(estado){
            byte comandoEncendido[]=new byte[]{0x30,0x46,(byte)0xFF};
            enviarDatosCoordinador(comandoEncendido);
        }
        else{
            byte comandoApagado[]=new byte[]{0x30,0x46,0x00};
            enviarDatosCoordinador(comandoApagado);
        }
    }

    //Metodo que envia el comando de mensaje de emergenia al modulo coordinador
    public void enviarComandoMensajeAlarmaCoordinador(byte tipo){
        byte comandoMensaje[]=new byte[]{0x30,0x47,(byte)tipo};
        enviarDatosCoordinador(comandoMensaje);
    }

    //
    //Metodos que realizan consultas y modificaciones a las bases de datos
    //del sistema domotico
    //
    public boolean obtenerModificacionComandoAlarmaBD(){
        List<Document> documento=(List<Document>)coleccionComandoEstadoAlarma.find().limit(1).into(new LinkedList<Document>());
        boolean respuesta=documento.get(0).getBoolean("modificado");
        return respuesta;
    }

    public void restablecerModificacionComandoAlarmaBD(){
        Bson nuevoValor=new Document("modificado",false);
        Bson operacionActualizacion=new Document ("$set",nuevoValor);
        coleccionComandoEstadoAlarma.updateOne(eq("_id",new ObjectId("5899e6f901a5251c48d9d5d3")),operacionActualizacion);
    }

    public boolean obtenerEstadoComandoAlarmaBD(){
        List<Document> documento=(List<Document>)coleccionComandoEstadoAlarma.find().limit(1).into(new LinkedList<Document>());
        boolean respuesta=documento.get(0).getBoolean("encendida");
        return respuesta;
    }

    public void establecerEstadoComandoAlarmaBD(boolean nuevoEstado){
        Bson nuevoValor=new Document("encendida",nuevoEstado);
        Bson operacionActualizacion=new Document ("$set",nuevoValor);
        coleccionComandoEstadoAlarma.updateOne(eq("_id",new ObjectId("5899e6f901a5251c48d9d5d3")),operacionActualizacion);
    }

    public boolean obtenerModificacionComandoActCocinaBD(){
        List<Document> documento=(List<Document>)coleccionComandoActuadoresCocina.find().limit(1).into(new LinkedList<Document>());
        boolean respuesta=documento.get(0).getBoolean("modificado");
        return respuesta;
    }

    public void restablecerModificacionComandoActCocinaBD(){
        Bson nuevoValor=new Document("modificado",false);
        Bson operacionActualizacion=new Document ("$set",nuevoValor);
        coleccionComandoActuadoresCocina.updateOne(eq("_id",new ObjectId("5899e80001a5251c48d9d5d5")),operacionActualizacion);
    }

    public boolean obtenerEstadoComandoAct1CocinaBD(){
        List<Document> documento=(List<Document>)coleccionComandoActuadoresCocina.find().limit(1).into(new LinkedList<Document>());
        boolean respuesta=documento.get(0).getBoolean("actuador1");
        return respuesta;
    }

    public boolean obtenerEstadoComandoAct2CocinaBD(){
        List<Document> documento=(List<Document>)coleccionComandoActuadoresCocina.find().limit(1).into(new LinkedList<Document>());
        boolean respuesta=documento.get(0).getBoolean("actuador2");
        return respuesta;
    }

    public void establecerEstadoComandoAct1CocinaBD(boolean nuevoEstado){
        Bson nuevoValor=new Document("actuador1",nuevoEstado);
        Bson operacionActualizacion=new Document ("$set",nuevoValor);
        coleccionComandoActuadoresCocina.updateOne(eq("_id",new ObjectId("5899e80001a5251c48d9d5d5")),operacionActualizacion);
    }

    public void establecerEstadoComandoAct2CocinaBD(boolean nuevoEstado){
        Bson nuevoValor=new Document("actuador2",nuevoEstado);
        Bson operacionActualizacion=new Document ("$set",nuevoValor);
        coleccionComandoActuadoresCocina.updateOne(eq("_id",new ObjectId("5899e80001a5251c48d9d5d5")),operacionActualizacion);
    }

    public boolean obtenerModificacionComandoActExteriorBD(){
        List<Document> documento=(List<Document>)coleccionComandoActuadoresExterior.find().limit(1).into(new LinkedList<Document>());
        boolean respuesta=documento.get(0).getBoolean("modificado");
        return respuesta;
    }

    public void restablecerModificacionComandoActExteriorBD(){
        Bson nuevoValor=new Document("modificado",false);
        Bson operacionActualizacion=new Document ("$set",nuevoValor);
        coleccionComandoActuadoresExterior.updateOne(eq("_id",new ObjectId("5899e80f01a5251c48d9d5d6")),operacionActualizacion);
    }

    public boolean obtenerEstadoComandoAct1ExteriorBD(){
        List<Document> documento=(List<Document>)coleccionComandoActuadoresExterior.find().limit(1).into(new LinkedList<Document>());
        boolean respuesta=documento.get(0).getBoolean("actuador1");
        return respuesta;
    }

    public boolean obtenerEstadoComandoAct2ExteriorBD(){
        List<Document> documento=(List<Document>)coleccionComandoActuadoresExterior.find().limit(1).into(new LinkedList<Document>());
        boolean respuesta=documento.get(0).getBoolean("actuador2");
        return respuesta;
    }

    public void establecerEstadoComandoAct1ExteriorBD(boolean nuevoEstado){
        Bson nuevoValor=new Document("actuador1",nuevoEstado);
        Bson operacionActualizacion=new Document ("$set",nuevoValor);
        coleccionComandoActuadoresExterior.updateOne(eq("_id",new ObjectId("5899e80f01a5251c48d9d5d6")),operacionActualizacion);
    }

    public void establecerEstadoComandoAct2ExteriorBD(boolean nuevoEstado){
        Bson nuevoValor=new Document("actuador2",nuevoEstado);
        Bson operacionActualizacion=new Document ("$set",nuevoValor);
        coleccionComandoActuadoresExterior.updateOne(eq("_id",new ObjectId("5899e80f01a5251c48d9d5d6")),operacionActualizacion);
    }

    public boolean obtenerModificacionComandoActHabitacionBD(){
        List<Document> documento=(List<Document>)coleccionComandoActuadoresHabitacion.find().limit(1).into(new LinkedList<Document>());
        boolean respuesta=documento.get(0).getBoolean("modificado");
        return respuesta;
    }

    public void restablecerModificacionComandoActHabitacionBD(){
        Bson nuevoValor=new Document("modificado",false);
        Bson operacionActualizacion=new Document ("$set",nuevoValor);
        coleccionComandoActuadoresHabitacion.updateOne(eq("_id",new ObjectId("5899e81a01a5251c48d9d5d7")),operacionActualizacion);
    }

    public boolean obtenerEstadoComandoAct1HabitacionBD(){
        List<Document> documento=(List<Document>)coleccionComandoActuadoresHabitacion.find().limit(1).into(new LinkedList<Document>());
        boolean respuesta=documento.get(0).getBoolean("actuador1");
        return respuesta;
    }

    public boolean obtenerEstadoComandoAct2HabitacionBD(){
        List<Document> documento=(List<Document>)coleccionComandoActuadoresHabitacion.find().limit(1).into(new LinkedList<Document>());
        boolean respuesta=documento.get(0).getBoolean("actuador2");
        return respuesta;
    }

    public void establecerEstadoComandoAct1HabitacionBD(boolean nuevoEstado){
        Bson nuevoValor=new Document("actuador1",nuevoEstado);
        Bson operacionActualizacion=new Document ("$set",nuevoValor);
        coleccionComandoActuadoresHabitacion.updateOne(eq("_id",new ObjectId("5899e81a01a5251c48d9d5d7")),operacionActualizacion);
    }

    public void establecerEstadoComandoAct2HabitacionBD(boolean nuevoEstado){
        Bson nuevoValor=new Document("actuador2",nuevoEstado);
        Bson operacionActualizacion=new Document ("$set",nuevoValor);
        coleccionComandoActuadoresHabitacion.updateOne(eq("_id",new ObjectId("5899e81a01a5251c48d9d5d7")),operacionActualizacion);
    }

    public boolean obtenerModificacionComandoModoOpCocinaBD(){
        List<Document> documento=(List<Document>)coleccionComandosModoOperacionCocina.find().limit(1).into(new LinkedList<Document>());
        boolean respuesta=documento.get(0).getBoolean("modificado");
        return respuesta;
    }

    public boolean obtenerModificacionComandoModoOpExteriorBD(){
        List<Document> documento=(List<Document>)coleccionComandosModoOperacionExterior.find().limit(1).into(new LinkedList<Document>());
        boolean respuesta=documento.get(0).getBoolean("modificado");
        return respuesta;
    }

    public boolean obtenerModificacionComandoModoOpHabitacionBD(){
        List<Document> documento=(List<Document>)coleccionComandosModoOperacionHabitacion.find().limit(1).into(new LinkedList<Document>());
        boolean respuesta=documento.get(0).getBoolean("modificado");
        return respuesta;
    }

    public void restablecerModificacionComandoModoOpCocinaBD(){
        Bson nuevoValor=new Document("modificado",false);
        Bson operacionActualizacion=new Document ("$set",nuevoValor);
        coleccionComandosModoOperacionCocina.updateOne(eq("_id",new ObjectId("58bc30ff01a5251fe83c7c80")),operacionActualizacion);
    }

    public void restablecerModificacionComandoModoOpExteriorBD(){
        Bson nuevoValor=new Document("modificado",false);
        Bson operacionActualizacion=new Document ("$set",nuevoValor);
        coleccionComandosModoOperacionExterior.updateOne(eq("_id",new ObjectId("58bc30cf01a5251fe83c7c7f")),operacionActualizacion);
    }

    public void restablecerModificacionComandoModoOpHabitacionBD(){
        Bson nuevoValor=new Document("modificado",false);
        Bson operacionActualizacion=new Document ("$set",nuevoValor);
        coleccionComandosModoOperacionHabitacion.updateOne(eq("_id",new ObjectId("58bc30b401a5251fe83c7c7e")),operacionActualizacion);
    }

    public byte obtenerComandoModoOperacionCocinaBD(){
        byte respuesta=0x00;
        List<Document> documento=(List<Document>)coleccionComandosModoOperacionCocina.find().limit(1).into(new LinkedList<Document>());
        if(documento.get(0).getBoolean("modonormal")){
            respuesta=0x01;
        }
        else{
            if(documento.get(0).getBoolean("modosemi")){
                respuesta=0x02;
            }
            else{
                if(documento.get(0).getBoolean("modoarmado")){
                  respuesta=0x03;
                }
            }
        }
        return respuesta;
    }

    public byte obtenerComandoModoOperacionExteriorBD(){
        byte respuesta=0x00;
        List<Document> documento=(List<Document>)coleccionComandosModoOperacionExterior.find().limit(1).into(new LinkedList<Document>());
        if(documento.get(0).getBoolean("modonormal")){
            respuesta=0x01;
        }
        else{
            if(documento.get(0).getBoolean("modosemi")){
                respuesta=0x02;
            }
            else{
                if(documento.get(0).getBoolean("modoarmado")){
                  respuesta=0x03;
                }
            }
        }
        return respuesta;
    }

    public byte obtenerComandoModoOperacionHabitacionBD(){
        byte respuesta=0x00;
        List<Document> documento=(List<Document>)coleccionComandosModoOperacionHabitacion.find().limit(1).into(new LinkedList<Document>());
        if(documento.get(0).getBoolean("modonormal")){
            respuesta=0x01;
        }
        else{
            if(documento.get(0).getBoolean("modosemi")){
                respuesta=0x02;
            }
            else{
                if(documento.get(0).getBoolean("modoarmado")){
                  respuesta=0x03;
                }
            }
        }
        return respuesta;
    }

    public void restablecerComandoModoOperacionCocinaBD(){
        Bson activarNormal=new Document("modonormal",false);
        Bson desactivarSemi=new Document("modosemi",false);
        Bson desactivarArmado=new Document("modoarmado",false);
        Bson operacionNormal=new Document ("$set",activarNormal);
        Bson operacionSemi=new Document ("$set",desactivarSemi);
        Bson operacionArmado=new Document ("$set",desactivarArmado);
        coleccionComandosModoOperacionCocina.updateOne(eq("_id",new ObjectId("58bc30ff01a5251fe83c7c80")),operacionNormal);
        coleccionComandosModoOperacionCocina.updateOne(eq("_id",new ObjectId("58bc30ff01a5251fe83c7c80")),operacionSemi);
        coleccionComandosModoOperacionCocina.updateOne(eq("_id",new ObjectId("58bc30ff01a5251fe83c7c80")),operacionArmado);
    }

    public void restablecerComandoModoOperacionExteriorBD(){
        Bson activarNormal=new Document("modonormal",false);
        Bson desactivarSemi=new Document("modosemi",false);
        Bson desactivarArmado=new Document("modoarmado",false);
        Bson operacionNormal=new Document ("$set",activarNormal);
        Bson operacionSemi=new Document ("$set",desactivarSemi);
        Bson operacionArmado=new Document ("$set",desactivarArmado);
        coleccionComandosModoOperacionExterior.updateOne(eq("_id",new ObjectId("58bc30cf01a5251fe83c7c7f")),operacionNormal);
        coleccionComandosModoOperacionExterior.updateOne(eq("_id",new ObjectId("58bc30cf01a5251fe83c7c7f")),operacionSemi);
        coleccionComandosModoOperacionExterior.updateOne(eq("_id",new ObjectId("58bc30cf01a5251fe83c7c7f")),operacionArmado);
    }

    public void restablecerComandoModoOperacionHabitacionBD(){
        Bson activarNormal=new Document("modonormal",false);
        Bson desactivarSemi=new Document("modosemi",false);
        Bson desactivarArmado=new Document("modoarmado",false);
        Bson operacionNormal=new Document ("$set",activarNormal);
        Bson operacionSemi=new Document ("$set",desactivarSemi);
        Bson operacionArmado=new Document ("$set",desactivarArmado);
        coleccionComandosModoOperacionHabitacion.updateOne(eq("_id",new ObjectId("58bc30b401a5251fe83c7c7e")),operacionNormal);
        coleccionComandosModoOperacionHabitacion.updateOne(eq("_id",new ObjectId("58bc30b401a5251fe83c7c7e")),operacionSemi);
        coleccionComandosModoOperacionHabitacion.updateOne(eq("_id",new ObjectId("58bc30b401a5251fe83c7c7e")),operacionArmado);
    }

    public void establecerModoOperacionCocina(boolean normal,boolean semi, boolean armado){
        Bson activarNormal=new Document("modonormal",normal);
        Bson desactivarSemi=new Document("modosemi",semi);
        Bson desactivarArmado=new Document("modoarmado",armado);
        Bson operacionNormal=new Document ("$set",activarNormal);
        Bson operacionSemi=new Document ("$set",desactivarSemi);
        Bson operacionArmado=new Document ("$set",desactivarArmado);
        coleccionModoOperacionCocina.updateOne(eq("_id",new ObjectId("58a523e901a52524f8d3d40c")),operacionNormal);
        coleccionModoOperacionCocina.updateOne(eq("_id",new ObjectId("58a523e901a52524f8d3d40c")),operacionSemi);
        coleccionModoOperacionCocina.updateOne(eq("_id",new ObjectId("58a523e901a52524f8d3d40c")),operacionArmado);
    }

    public void establecerModoOperacionExterior(boolean normal,boolean semi, boolean armado){
        Bson activarNormal=new Document("modonormal",normal);
        Bson desactivarSemi=new Document("modosemi",semi);
        Bson desactivarArmado=new Document("modoarmado",armado);
        Bson operacionNormal=new Document ("$set",activarNormal);
        Bson operacionSemi=new Document ("$set",desactivarSemi);
        Bson operacionArmado=new Document ("$set",desactivarArmado);
        coleccionModoOperacionExterior.updateOne(eq("_id",new ObjectId("58a523b801a52524f8d3d40a")),operacionNormal);
        coleccionModoOperacionExterior.updateOne(eq("_id",new ObjectId("58a523b801a52524f8d3d40a")),operacionSemi);
        coleccionModoOperacionExterior.updateOne(eq("_id",new ObjectId("58a523b801a52524f8d3d40a")),operacionArmado);
    }

    public void establecerModoOperacionHabitacion(boolean normal,boolean semi, boolean armado){
        Bson activarNormal=new Document("modonormal",normal);
        Bson desactivarSemi=new Document("modosemi",semi);
        Bson desactivarArmado=new Document("modoarmado",armado);
        Bson operacionNormal=new Document ("$set",activarNormal);
        Bson operacionSemi=new Document ("$set",desactivarSemi);
        Bson operacionArmado=new Document ("$set",desactivarArmado);
        coleccionModoOperacionHabitacion.updateOne(eq("_id",new ObjectId("58a523dd01a52524f8d3d40b")),operacionNormal);
        coleccionModoOperacionHabitacion.updateOne(eq("_id",new ObjectId("58a523dd01a52524f8d3d40b")),operacionSemi);
        coleccionModoOperacionHabitacion.updateOne(eq("_id",new ObjectId("58a523dd01a52524f8d3d40b")),operacionArmado);
    }

    public void establecerNuevosDatosCocinaDB(int temperatura,int humedad,int gas,int humo,boolean fuego,boolean actuador1,boolean actuador2,boolean controles){
        Document nuevoDocumento=new Document();
        nuevoDocumento.put("temperatura",temperatura);
        nuevoDocumento.put("humedad",humedad);
        nuevoDocumento.put("gas",gas);
        nuevoDocumento.put("humo",humo);
        nuevoDocumento.put("fuego",fuego);
        nuevoDocumento.put("actuador1",actuador1);
        nuevoDocumento.put("actuador2",actuador2);
        nuevoDocumento.put("controles",controles);
        nuevoDocumento.put("fecha",new Date());
        coleccionDatosModuloCocina.insertOne(nuevoDocumento);
    }

    public void establecerEstadoAlarmaCoordinadorBD(boolean estado){
        Bson cambioEstado=new Document("encendida",estado);
        Bson operacionEstado=new Document ("$set",cambioEstado);
        coleccionAlarmaModuloCoordinador.updateOne(eq("_id",new ObjectId("58bc2cc401a5251fe83c7c7d")),operacionEstado);
    }

    public void establecerHabilitacionAlarmaCoordinadorBD(boolean habilitacion){
        Bson cambiohabilitacion=new Document("habilitado",habilitacion);
        Bson operacionHabilitacion=new Document ("$set",cambiohabilitacion);
        coleccionAlarmaModuloCoordinador.updateOne(eq("_id",new ObjectId("58bc2cc401a5251fe83c7c7d")),operacionHabilitacion);
    }

    public void establecerNuevosDatosExteriorDB(boolean lluvia,int nivelagua,boolean movimiento,int luz,int consumoelectrico,long consumoagua,boolean actuador1,boolean actuador2,boolean controles){
        Document nuevoDocumento=new Document();
        nuevoDocumento.put("lluvia",lluvia);
        nuevoDocumento.put("nivelagua",nivelagua);
        nuevoDocumento.put("movimiento",movimiento);
        nuevoDocumento.put("luz",luz);
        nuevoDocumento.put("consumoelectrico",consumoelectrico);
        nuevoDocumento.put("consumoagua",consumoagua);
        nuevoDocumento.put("actuador1",actuador1);
        nuevoDocumento.put("actuador2",actuador2);
        nuevoDocumento.put("controles",controles);
        nuevoDocumento.put("fecha",new Date());
        coleccionDatosModuloExterior.insertOne(nuevoDocumento);
    }

    public void establecerNuevosDatosHabitacionDB(int temperatura,int humedad,boolean movimiento,boolean magnetico,int humo,boolean actuador1,boolean actuador2,boolean controles){
        Document nuevoDocumento=new Document();
        nuevoDocumento.put("temperatura",temperatura);
        nuevoDocumento.put("humedad",humedad);
        nuevoDocumento.put("movimiento",movimiento);
        nuevoDocumento.put("magnetico",magnetico);
        nuevoDocumento.put("humo",humo);
        nuevoDocumento.put("actuador1",actuador1);
        nuevoDocumento.put("actuador2",actuador2);
        nuevoDocumento.put("controles",controles);
        nuevoDocumento.put("fecha",new Date());
        coleccionDatosModuloHabitacion.insertOne(nuevoDocumento);
    }

    public void establecerActivacionModuloCocinaDB(boolean nuevoEstado){
        Bson nuevoValor=new Document("modulococina",nuevoEstado);
        Bson operacionActualizacion=new Document ("$set",nuevoValor);
        coleccionRegistroModulos.updateOne(eq("_id",new ObjectId("589c0eb101a5250504745cde")),operacionActualizacion);
    }

    public void establecerActivacionModuloExteriorDB(boolean nuevoEstado){
        Bson nuevoValor=new Document("moduloexterior",nuevoEstado);
        Bson operacionActualizacion=new Document ("$set",nuevoValor);
        coleccionRegistroModulos.updateOne(eq("_id",new ObjectId("589c0eb101a5250504745cde")),operacionActualizacion);
    }

    public void establecerActivacionModuloHabitacionDB(boolean nuevoEstado){
        Bson nuevoValor=new Document("modulohabitacion",nuevoEstado);
        Bson operacionActualizacion=new Document ("$set",nuevoValor);
        coleccionRegistroModulos.updateOne(eq("_id",new ObjectId("589c0eb101a5250504745cde")),operacionActualizacion);
    }

    public void escribirNotificacion(String mensaje,String tipo){
        Document nuevoDocumento=new Document();
        nuevoDocumento.put("mensaje",mensaje);
        nuevoDocumento.put("tipo",tipo);
        nuevoDocumento.put("fecha",new Date());
        coleccionNotificaciones.insertOne(nuevoDocumento);
    }

    public void restablecerInicioSesion(){
        Bson nuevoValor=new Document("sesion",false);
        Bson operacionActualizacion=new Document ("$set",nuevoValor);
        coleccionUsuariosAutorizados.updateOne(eq("_id",new ObjectId("5899e67201a5251c48d9d5d1")),operacionActualizacion);
    }
}
