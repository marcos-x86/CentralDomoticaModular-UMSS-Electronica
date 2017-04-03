//UNIVERSIDAD MAYOR DE SAN SIMON
//FACULTAD DE CIENCIAS Y TECNOLOGIA
//CARRERA DE INGENIERIA ELECTRONICA
//
//SISTEMA DOMOTICO MODULAR CENTRALIZADO DESARROLLADO POR:
//
//LARA TORRICO MARCOS
//TORREZ JORGE BRIAN

#include <DHT.h>          //Libreria del sensor de temperatura DHT11
#include <EmonLib.h>      //Libreria de OpenEnergy para el sensor de corriente electrica
#include <LinkedList.h>   //Libreria que implementa la estructura de datos Lista Enlazada
#include <XBee.h>         //Libreria de los modulos XBee
#include <SoftwareSerial.h>

//Variables constantes que identifican el tipo de sensor
const byte SENSOR_HUMO=0x61;
const byte SENSOR_GAS=0x62;
const byte SENSOR_LDR=0x63;
const byte SENSOR_CORRIENTE=0x64;
const byte SENSOR_CAUDAL=0x65;
const byte SENSOR_TEMPERATURA_HUMEDAD=0x66;
const byte SENSOR_LLUVIA=0x67;
const byte SENSOR_FUEGO=0x68;
const byte SENSOR_MAGNETICO=0x69;
const byte SENSOR_DISTANCIA=0x6A;
const byte SALIDAS_CONTROLADAS=0x6B;
const byte SENSOR_MOVIMIENTO=0x6C;

//Variables constantes que identifican el tipo, la cantidad de funciones y los sensores/actuadores que tiene el modulo
const byte TIPO_MODULO=0x23;
const byte NUMERO_FUNCIONES=0x07;
const byte FUNCIONES_MODULO[NUMERO_FUNCIONES]={SENSOR_LDR,SENSOR_CORRIENTE,SENSOR_CAUDAL,SENSOR_LLUVIA,SENSOR_DISTANCIA,SALIDAS_CONTROLADAS,SENSOR_MOVIMIENTO};

//Variables constantes que indican el PIN al cual se conecta determinado sensor
const byte PIN_SENSOR_MOVIMIENTO=0x03;
const byte PIN_SENSOR_CORRIENTE=0x04;
const byte PIN_SENSOR_LDR=0x05;
const byte PIN_SENSOR_GAS=0x06;
const byte PIN_SENSOR_HUMO=0x07;
const byte PIN_SENSOR_CAUDAL=0x02;
const byte PIN_SENSOR_TEMPERATURA_HUMEDAD=0x03;
const byte PIN_SENSOR_LLUVIA=0x04;
const byte PIN_LED_ESTADO=0x05;
const byte PIN_SENSOR_FUEGO=0x06;
const byte PIN_SENSOR_MAGNETICO=0x07;
const byte PIN_SENSOR_DISTANCIA_DISPARO=0x09;
const byte PIN_SENSOR_DISTANCIA_ENTRADA=0x0A;
const byte PIN_SALIDA_CONTROLADA_1=0x0B;
const byte PIN_SALIDA_CONTROLADA_2=0x0C;

//Variables constantes que identifican el tipo de codigo de comando
const byte REGISTRAR_MODULO=0x41;
const byte ENVIAR_CONFIGURACION=0x42;
const byte OBTENER_DATOS=0x43;
const byte ESTABLECER_SALIDAS=0x44;
const byte MODIFICAR_FUNCIONES=0x45;
const byte ESTADO_ALARMA=0x46;
const byte ENVIO_SMS=0x47;
const byte CODIGO_ERROR=0x48;

//Variables globales auxiliares usadas por alguno sensores
const byte pinRXFlujometro=2;
const byte pinTXFlujometro=8;
const byte pinRXBloqueo=14;
const byte pinTXBloqueo=15;
boolean lecturaFlujometro=false;
String numeroMililitros="";
unsigned int numeroMililitrosInt=0;
char caracterLeido='0';
char intBuffer[5];
SoftwareSerial SerialFlujometro(pinRXFlujometro,pinTXFlujometro);
SoftwareSerial SerialBloqueoFlujometro(pinRXBloqueo,pinTXBloqueo);

//Direccíon de 16 bits del modulo coordinador, al cual se le envian los datos
unsigned short direccionDestinoModuloCoordinador=0x5000;

//Variables auxiliares usadas por las funciones del modulo
unsigned int tiempoInicioComunicacionXbee=7000;
unsigned int tiempoReconexion=20000;
unsigned long inicioTiempoConexion=0;
boolean moduloRegistrado=false;
byte funcionesActivadasModulo[NUMERO_FUNCIONES];
byte numeroFuncionesActivadasModulo;
byte salidasControladas=B00000000;
byte numeroModuloLocal=0x30;

//Instancias de las clases proporcionadas por las librerias
XBee moduloXbee=XBee();   //Instancia de la clase XBee para el manero del modulo
LinkedList<byte> bufferDatosXbee;   //Instancia de la clase Lista Enlazada, funciona como un buffer de los datos recibidos
LinkedList<byte> bufferTramaDatos;  //Instancia de la clase Lista Enlazada, funciona como un buffer de los a transmitirse
EnergyMonitor sensorCorriente;      //Instancia de la clase Energy Monitor que usa el sensor de corriente
DHT sensorTempHum(PIN_SENSOR_TEMPERATURA_HUMEDAD, DHT11);

//Metodo de configuracion para el modulo
void setup(){
  DDRB=B11111011;     //configuracion de entradas/salidas a traves de los registro del ATmega328p
  DDRC=B00000111;
  DDRD=B00100010;
  PORTB|=B00011000;
  Serial.begin(19200,SERIAL_8N1);
  //Inicializacion de la comunicacion con el modulo XBee
  moduloXbee.setSerial(Serial);
  SerialFlujometro.begin(19200);
  SerialBloqueoFlujometro.begin(19200);
  SerialBloqueoFlujometro.listen();
  delay(tiempoInicioComunicacionXbee);
  //Solicitud de registro al modulo coordinador
  solicitarRegistroModulo();
  //Habilitacion de funciones
  for(int i=0;i<NUMERO_FUNCIONES;i++){
    if(FUNCIONES_MODULO[i]==SENSOR_CORRIENTE){
      sensorCorriente.current((int)PIN_SENSOR_CORRIENTE,50);
    }
  }
  //Activacion del LED de estado, cuando el registro del modulo se ha llevado a cabo
  PORTD|=B00100000;
}

//Metodo de ejecucion ciclica
void loop(){
  recibirDatosXbee();
}

//Metodo para el envio de datos hacia el modulo coordinador,
//procesa el buffer de datos convirtiendolos a un arreglo fijo
void enviarDatosXbee(short direccionFisicaModulo, byte comando){
  byte tramaDatos[bufferTramaDatos.size()+2];
  tramaDatos[0]=numeroModuloLocal;
  tramaDatos[1]=comando;
  for(int i=2;i<(bufferTramaDatos.size()+2);i++){
    tramaDatos[i]=bufferTramaDatos.get(i-2);
  }
  Tx16Request datosAEnviar=Tx16Request(direccionFisicaModulo, tramaDatos, sizeof(tramaDatos));
  moduloXbee.send(datosAEnviar);
  bufferTramaDatos.~LinkedList<byte>();
}

//Metodo que procesa la recepcion de datos del XBee,usa una
//estructura de datos para almacenar los datos en un buffer
void recibirDatosXbee(){
  moduloXbee.readPacket();
  if((moduloXbee.getResponse().isAvailable())&&(moduloXbee.getResponse().getApiId()==RX_16_RESPONSE)){
    bufferDatosXbee=LinkedList<byte>();
    Rx16Response tramaRecibida=Rx16Response();
    moduloXbee.getResponse().getRx16Response(tramaRecibida);
    for(int i=0;i<tramaRecibida.getDataLength();i++){
      bufferDatosXbee.add(tramaRecibida.getData(i));
    }
    analizarComando();
    bufferDatosXbee.~LinkedList<byte>();
  }
}

//Metodo que manda solicitudes de registro al modulo coordinador
void solicitarRegistroModulo(){
  do{
    inicioTiempoConexion=millis();
    bufferTramaDatos.add(TIPO_MODULO);
    enviarDatosXbee(direccionDestinoModuloCoordinador,REGISTRAR_MODULO);
    do{
      recibirDatosXbee();
    }while(!((millis()-inicioTiempoConexion)>tiempoReconexion));
  }while(!moduloRegistrado);
}

//Metodo que procesa el tipo de comando recibido, de acuerdo
//a este realiza acciones especificas
void analizarComando(){
  byte codigoComando=bufferDatosXbee.get(1);
  switch(codigoComando){
    case REGISTRAR_MODULO:
      registrarModulo();
      break;
    case ENVIAR_CONFIGURACION:
      enviarConfiguracion();
      break;
    case OBTENER_DATOS:
      obtenerDatos();
      break;
    case ESTABLECER_SALIDAS:
      establecerSalidas();
      break;
    case MODIFICAR_FUNCIONES:
      modificarFunciones();
      break;
  }
}

//Metodo del comando REGISTRAR_MODULO que realiza el registro del modulo, segun
//la informacion recibida por el modulo coordinador
void registrarModulo(){
  byte numeroModuloAsignado=bufferDatosXbee.get(2);
  if((numeroModuloAsignado>=0x31)&&(numeroModuloAsignado<=0x39)){
    numeroFuncionesActivadasModulo=NUMERO_FUNCIONES;
    numeroModuloLocal=numeroModuloAsignado;
    for(int i=0;i<NUMERO_FUNCIONES;i++){
      funcionesActivadasModulo[i]=FUNCIONES_MODULO[i];
    }
    enviarRegistro();
    moduloRegistrado=true;
  }
}

//Metodo que envia la respuesta a una solicitud de registro al modulo coordinador
void enviarRegistro(){
  bufferTramaDatos=LinkedList<byte>();
  bufferTramaDatos.add(TIPO_MODULO);
  enviarDatosXbee(direccionDestinoModuloCoordinador,REGISTRAR_MODULO);
}

//Metodo del comando ENVIAR_CONFIGURACION que manda la configuracion de
//los sensores del modulo
void enviarConfiguracion(){
  bufferTramaDatos=LinkedList<byte>();
  for(int i=0;i<numeroFuncionesActivadasModulo;i++){
    if(funcionesActivadasModulo[i]>=0x61){
      bufferTramaDatos.add(funcionesActivadasModulo[i]);
    }
  }
  enviarDatosXbee(direccionDestinoModuloCoordinador,ENVIAR_CONFIGURACION);
}

//Metodo del comando OBTENER_DATOS que prepara la lectura de los sensores
//habolitados en un buffer para enviarlos al modulo coordinador
void obtenerDatos(){
  bufferTramaDatos=LinkedList<byte>();
  for(int i=0;i<numeroFuncionesActivadasModulo;i++){
    switch(funcionesActivadasModulo[i]){
      case SENSOR_HUMO:
        bufferTramaDatos.add(funcionesActivadasModulo[i]);
        obtenerDatosSensorHumo();
        break;
      case SENSOR_GAS:
        bufferTramaDatos.add(funcionesActivadasModulo[i]);
        obtenerDatosSensorGas();
        break;
      case SENSOR_LDR:
        bufferTramaDatos.add(funcionesActivadasModulo[i]);
        obtenerDatosSensorLDR();
        break;
      case SENSOR_CORRIENTE:
        bufferTramaDatos.add(funcionesActivadasModulo[i]);
        obtenerDatosSensorCorriente();
        break;
      case SENSOR_CAUDAL:
        bufferTramaDatos.add(funcionesActivadasModulo[i]);
        obtenerDatosSensorCaudal();
        break;
      case SENSOR_TEMPERATURA_HUMEDAD:
        bufferTramaDatos.add(funcionesActivadasModulo[i]);
        obtenerDatosSensorTempHum();
        break;
      case SENSOR_LLUVIA:
        bufferTramaDatos.add(funcionesActivadasModulo[i]);
        obtenerDatosSensorLluvia();
        break;
      case SENSOR_FUEGO:
        bufferTramaDatos.add(funcionesActivadasModulo[i]);
        obtenerDatosSensorFuego();
        break;
      case SENSOR_MAGNETICO:
        bufferTramaDatos.add(funcionesActivadasModulo[i]);
        obtenerDatosSensorMagnetico();
        break;
      case SENSOR_DISTANCIA:
        bufferTramaDatos.add(funcionesActivadasModulo[i]);
        obtenerDatosSensorDistancia();
        break;
      case SALIDAS_CONTROLADAS:
        bufferTramaDatos.add(funcionesActivadasModulo[i]);
        obtenerDatosSalidasControladas();
        break;
      case SENSOR_MOVIMIENTO:
        bufferTramaDatos.add(funcionesActivadasModulo[i]);
        obtenerDatosSensorMovimiento();
        break;
    }
  }
  enviarDatosXbee(direccionDestinoModuloCoordinador,OBTENER_DATOS);
}

//Metodo de la funcion ESTABLECER_SALIDAS que maneja las salidas
//controladas, este opera a nivel de registro del ATmega328
void establecerSalidas(){
  byte estadoSalidasRecibido=bufferDatosXbee.get(2);
  if((estadoSalidasRecibido&B00000001)==B00000001){
    PORTB=PORTB&B11110111;
    salidasControladas=salidasControladas|B00000001;
  }
  else{
    PORTB=PORTB|B00001000;
    salidasControladas=salidasControladas&B11111110;
  }
  if((estadoSalidasRecibido&B00000010)==B00000010){
    PORTB=PORTB&B11101111;
    salidasControladas=salidasControladas|B00000010;
  }
  else{
    PORTB=PORTB|B00010000;
    salidasControladas=salidasControladas&B11111101;
  }
  enviarEstablecimientoSalidas();
}

//Metodo que envia el estado de las salidas controladas
void enviarEstablecimientoSalidas(){
  bufferTramaDatos=LinkedList<byte>();
  bufferTramaDatos.add((byte)salidasControladas);
  enviarDatosXbee(direccionDestinoModuloCoordinador,ESTABLECER_SALIDAS);
}

//Metodo del comando MODIFICAR_FUNCIONES, lo que hace es deshabilitar
//y habilitar funciones o sensores del modulo
void modificarFunciones(){
  bufferDatosXbee.remove(0);
  bufferDatosXbee.remove(0);
  for(int i=0;i<numeroFuncionesActivadasModulo;i++){
    funcionesActivadasModulo[i]=0x00;
  }
  for(int i=0;i<bufferDatosXbee.size();i++){
    funcionesActivadasModulo[i]=(byte)bufferDatosXbee.get(i);
  }
  numeroFuncionesActivadasModulo=(byte)bufferDatosXbee.size();
  enviarModificacionFunciones();
}

//Metodo que envia las funciones activadas actualmente en el modulo
void enviarModificacionFunciones(){
  bufferTramaDatos=LinkedList<byte>();
  for(int i=0;i<numeroFuncionesActivadasModulo;i++){
    bufferTramaDatos.add((byte)funcionesActivadasModulo[i]);
  }
  enviarDatosXbee(direccionDestinoModuloCoordinador,MODIFICAR_FUNCIONES);
}

//Metodo de envio de errores, usado para depuracion
void codigoError(){
  enviarDatosXbee(direccionDestinoModuloCoordinador,CODIGO_ERROR);
}

//Metodo de lectura del sensor de humo
void obtenerDatosSensorHumo(){
  int sensorHumo=analogRead(PIN_SENSOR_HUMO);
  sensorHumo=map(sensorHumo,0,1023,0,255);
  sensorHumo=constrain(sensorHumo,0,255);
  bufferTramaDatos.add((byte)sensorHumo);
}

//Metodo de lectura del sensor de gas
void obtenerDatosSensorGas(){
  int sensorGas=analogRead(PIN_SENSOR_GAS);
  sensorGas=map(sensorGas,0,1023,0,255);
  sensorGas=constrain(sensorGas,0,255);
  bufferTramaDatos.add((byte)sensorGas);

}

//Metodo de lectura del sensor de intensidad luminica
void obtenerDatosSensorLDR(){
  int sensorLDR=analogRead(PIN_SENSOR_LDR);
  sensorLDR=map(sensorLDR,0,1023,0,255);
  sensorLDR=constrain(sensorLDR,0,255);
  bufferTramaDatos.add((byte)sensorLDR);
}

//Metodo de lectura del sensor de corriente
void obtenerDatosSensorCorriente(){
  double potenciaAparente=(((sensorCorriente.calcIrms(1480)*220)-52)*2.6)-7.5;
  int potenciaAparenteCorregida=constrain(((int)potenciaAparente),0,10000);
  byte potenciaAparenteL=(byte)(potenciaAparenteCorregida&0xFF);
  byte potenciaAparenteH=(byte)((potenciaAparenteCorregida>>8)&0xFF);
  bufferTramaDatos.add(potenciaAparenteH);
  bufferTramaDatos.add(potenciaAparenteL);
}

//Metodo de lectura del sensor de caudal
void obtenerDatosSensorCaudal(){
  unsigned long limiteTiempoLectura=millis();
  SerialFlujometro.listen();
  limpiarBufferSoftwareSerial();
  while(!(lecturaFlujometro)&&(millis()-limiteTiempoLectura)<1000){
    if(SerialFlujometro.available()>0){
      if(SerialFlujometro.read()=='A'){
        delay(2);
        do{
          caracterLeido=(char)SerialFlujometro.read();
          if(isDigit(caracterLeido)){
            numeroMililitros+=caracterLeido;
            delay(2);
          }
        }while(!(caracterLeido=='Z'));
        numeroMililitros.toCharArray(intBuffer,sizeof(intBuffer));
        numeroMililitrosInt=strtoul(intBuffer,0,10);
        lecturaFlujometro=true;
      }
    }
  }
  if(!lecturaFlujometro){
    numeroMililitrosInt=0;
  }
  SerialBloqueoFlujometro.listen();
  numeroMililitros="";
  lecturaFlujometro=false;
  byte sensorCaudalLL=(byte)((numeroMililitrosInt&0xFF));
  byte sensorCaudalLH=(byte)((numeroMililitrosInt>>8)&0xFF);
  bufferTramaDatos.add(sensorCaudalLH);
  bufferTramaDatos.add(sensorCaudalLL);
}

//Metodo de lectura del sensor de temperatura y humedad relativa
void obtenerDatosSensorTempHum(){
  float valTemp=sensorTempHum.readTemperature();
  float valHum=sensorTempHum.readHumidity();
  valTemp=constrain(valTemp,0,50);
  valHum=constrain(valHum,20,80);
  bufferTramaDatos.add((byte)valTemp);
  bufferTramaDatos.add((byte)valHum);
}

//Metodo de lectura del sensor de lluvia
void obtenerDatosSensorLluvia(){
  byte sensorLluvia;
  if(digitalRead(PIN_SENSOR_LLUVIA)){
    sensorLluvia=0x00;
  }
  else{
    sensorLluvia=0xFF;
  }
  bufferTramaDatos.add(sensorLluvia);
}

//Metodo de lectura del sensor de fuego
void obtenerDatosSensorFuego(){
  byte sensorFuego;
  if(digitalRead(PIN_SENSOR_FUEGO)){
    sensorFuego=0x00;
  }
  else{
    sensorFuego=0xFF;
  }
  bufferTramaDatos.add(sensorFuego);
}

//Metodo de lectura del sensor de contacto magnetico
void obtenerDatosSensorMagnetico(){
  byte sensorMagnetico;
  if(digitalRead(PIN_SENSOR_MAGNETICO)){
    sensorMagnetico=0xFF;
  }
  else{
    sensorMagnetico=0x00;
  }
  bufferTramaDatos.add(sensorMagnetico);
}

//Metodo de lectura del sensor de distancia
void obtenerDatosSensorDistancia(){
  int distanciaMedida;
  long tiempoRespuesta;
  PORTB&=B11111101;
  delayMicroseconds(5);
  PORTB|=B00000010;
  delayMicroseconds(10);
  tiempoRespuesta=pulseIn(PIN_SENSOR_DISTANCIA_ENTRADA, HIGH);
  distanciaMedida=int(0.017*tiempoRespuesta);
  distanciaMedida=constrain(distanciaMedida,0,255);
  bufferTramaDatos.add((byte)distanciaMedida);
}

//Metodo de obtencion del estado de las salidas controladas
void obtenerDatosSalidasControladas(){
  bufferTramaDatos.add(salidasControladas);
}

//Metodo de lectura del sensor de movimiento
void obtenerDatosSensorMovimiento(){
  int sensorMovimiento=analogRead(PIN_SENSOR_MOVIMIENTO);
  sensorMovimiento=map(sensorMovimiento,0,1023,0,255);
  sensorMovimiento=constrain(sensorMovimiento,0,255);
  bufferTramaDatos.add((byte)sensorMovimiento);
}

void limpiarBufferSoftwareSerial(){
  while(SerialFlujometro.available()>0){
    SerialFlujometro.read();
  }
}
