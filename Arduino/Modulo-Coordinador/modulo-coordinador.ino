#include <XBee.h>             //Libreria de los modulos XBee
#include <SoftwareSerial.h>   //Libreria para la habilitación de puertos seriales por software
#include <LinkedList.h>       //Libreria que implementa la estructura de datos Lista Enlazada

//Instruccion en Assembler que consume un ciclo de reloj
#define NOP __asm__ __volatile__ ("nop\n\t");

//Variables constantes que indican el PIN al cual se conecta determinado módulo o dispositivo
const byte PIN_RX_XBEE=0x03;
const byte PIN_TX_XBEE=0x02;
const byte PIN_BUZZER=0x06;
const byte PIN_RX_GSM=0x0B;
const byte PIN_TX_GSM=0x0C;
const byte PIN_LED_ESTADO=0x0D;

//Codigos de comando exclusivos del modulo coordinador
const byte ESTADO_ALARMA=0x46;
const byte ENVIO_SMS=0x47;
const byte CODIGO_ERROR=0x48;

//Variables auxiliares usadas para banderas o temporizacion
volatile boolean buzzerActivado=false;

unsigned long inicioLectura=0;
unsigned int tiempoInicioComunicacionXbee=7000;
unsigned short direccionModuloDestino=0x0000;

//Instancias de las clases proporcionadas por las librerias
XBee moduloXbee=XBee();   //Instancia de la clase XBee para el manero del modulo
SoftwareSerial SerialXbee(PIN_RX_XBEE,PIN_TX_XBEE); //Puerto serial por software para el modulo XBee
SoftwareSerial SerialGSM(PIN_RX_GSM,PIN_TX_GSM);    //Puerto serial por software para el modulo GSM
LinkedList<byte> bufferDatosCentralDomotica;  //Instancia de la clase Lista Enlazada, funciona como un buffer de los datos recibidos
LinkedList<byte> bufferDatosXbeeModulos;      //Instancia de la clase Lista Enlazada, funciona como un buffer de los a transmitirse
LinkedList<short> direccionesDestinoModulos;  //Instancia de la clase Energy Monitor que usa el sensor de corriente

//Metodo de configuracion para el modulo
void setup(){
  //Configuracion de entradas y salidas digitales
  DDRB=B11110111;
  DDRC=B11111111;
  DDRD=B11110110;
  Serial.begin(19200,SERIAL_8N1);
  SerialXbee.begin(19200);
  SerialGSM.begin(19200);
  configurarModuloGSM();
  SerialXbee.listen();
  moduloXbee.setSerial(SerialXbee);
  //Se adicionan  las direcciones de 16 bit de los 9 modulos (numero maximo) del sistema
  direccionesDestinoModulos.add(0x5001);
  direccionesDestinoModulos.add(0x5002);
  direccionesDestinoModulos.add(0x5003);
  direccionesDestinoModulos.add(0x5004);
  direccionesDestinoModulos.add(0x5005);
  direccionesDestinoModulos.add(0x5006);
  direccionesDestinoModulos.add(0x5007);
  direccionesDestinoModulos.add(0x5008);
  direccionesDestinoModulos.add(0x5009);
  TCCR1A=0;
  TCCR1B=0;
  TCCR1B|=(1<<CS12);
  interrupts();
  delay(tiempoInicioComunicacionXbee);
  PORTB|=B00100000;
}

//Interrupcion usada para emitir tonos de alera de la alarma sonora
ISR(TIMER1_OVF_vect){
  if(buzzerActivado){
    analogWrite(PIN_BUZZER,20);
    buzzerActivado=false;
  }
  else{
    analogWrite(PIN_BUZZER,0);
    buzzerActivado=true;
  }
}

//Metodo de ejecucion ciclica
void loop(){
  recibirDatosXbee();
  recibirDatosCentral();
}

//Metodo que configura el modulo GSM y lo activa en modo texto, borrando los SMS de la memoria
void configurarModuloGSM(){
  SerialGSM.println("ATE0");
  delay(100);
  SerialGSM.println("AT+CMGF=1");
  delay(100);
  SerialGSM.println("AT+CMGDA=\"DEL ALL\"");
  delay(100);
}

//Metodo que recibe un comando de la central domotica y lo almacena en un buffer de datos
void recibirDatosCentral(){
  if(Serial.available()>0){
    bufferDatosCentralDomotica=LinkedList<byte>();
    do{
      inicioLectura = millis();
      bufferDatosCentralDomotica.add(Serial.read());
      while((millis()-inicioLectura)<2){
        NOP;
      }
    }while(Serial.available()>0);
    analizarTramaCentralDomotica();
    bufferDatosCentralDomotica.~LinkedList<byte>();
  }
}

//Metodo que recibe datos de algun modulo XBee dentro del sistema y los almacena en un buffer
void recibirDatosXbee(){
  moduloXbee.readPacket();
  if((moduloXbee.getResponse().isAvailable())&&(moduloXbee.getResponse().getApiId()==RX_16_RESPONSE)){
    Rx16Response tramaRecibida=Rx16Response();
    moduloXbee.getResponse().getRx16Response(tramaRecibida);
    bufferDatosXbeeModulos=LinkedList<byte>();
    for(int i=0;i<tramaRecibida.getDataLength();i++){
      bufferDatosXbeeModulos.add(tramaRecibida.getData(i));
    }
    reenviarTramaHaciaCentral();
    bufferDatosXbeeModulos.~LinkedList<byte>();
  }
}

//Metodo que analiza si el comando es para reenviar o debe activar una funcion en el coordinador
void analizarTramaCentralDomotica(){
  switch(bufferDatosCentralDomotica.get(1)){
    case ESTADO_ALARMA:
      modificarEstadoAlarma();
      break;
    case ENVIO_SMS:
      enviarSMS();
      break;
    default:
      reenviarDatosAModulo();
      break;
  }
}

//MEtodo que reenvia los datos del buffer de recepcion XBee hacia la central domotica
void reenviarTramaHaciaCentral(){
  for(int i=0;i<bufferDatosXbeeModulos.size();i++){
    Serial.write(bufferDatosXbeeModulos.get(i));
  }
}

//MEtodo que ctiva o desactiva la alarma sonora del modulo coordinador
void modificarEstadoAlarma(){
  byte estadoAlarma=bufferDatosCentralDomotica.get(2);
  if(estadoAlarma==0xFF){
    buzzerActivado=true;
    TCNT1 = 3036;
    TIMSK1 |= (1 << TOIE1);
  }
  else{
    buzzerActivado=false;
    analogWrite(PIN_BUZZER,0);
    TIMSK1 &= ~(1 << TOIE1);
  }
  Serial.write(0x30);
  Serial.write(0x46);
  Serial.write(estadoAlarma);
}

//Metodo que sirve para emviar un mensaje de emergencia a traves de un codigo de identificacion
void enviarSMS(){
  byte tipoMensaje=bufferDatosCentralDomotica.get(2);
  SerialGSM.println("ATH");
  delay(10);
  SerialGSM.println("AT+CMGS=\"79759189\"");
  delay(10);
  switch(tipoMensaje){
    case 0x00:
      SerialGSM.print("Cambio del modo de operacion del UPS. Se esta usando el sistema de respaldo de energia en el hogar.");
      break;
    case 0x01:
      SerialGSM.print("Alerta de presencia de fuego en un ambiente del hogar.");
      break;
    case 0x02:
      SerialGSM.print("Alerta de presencia de humo en un ambiente del hogar.");
      break;
    case 0x03:
      SerialGSM.print("Alerta de presencia de gas en un ambiente del hogar.");
      break;
    case 0x04:
      SerialGSM.print("Alerta de bajo nivel en el tanque de agua del hogar.");
      break;
    case 0x05:
      SerialGSM.print("Alerta de deteccion de movimiento en los ambientes del hogar, alarma activada.");
      break;
    case 0x06:
      SerialGSM.print("Alerta de apertura de puerta forzada en el hogar, alarma activada.");
      break;
    case 0x07:
      SerialGSM.print("Alerta de deteccion de precipitacion fluvial en el hogar, alarma activada.");
      break;
  }
  delay(50);
  SerialGSM.write(0x1A);
  delay(5);
  SerialGSM.println("");
  delay(5);
  SerialGSM.println("AT+CMGDA=\"DEL ALL\"");
  delay(10);
  Serial.write(0x30);
  Serial.write(0x47);
  Serial.write(tipoMensaje);
}

//Metodo que reenvia los datos recibidos de la central hacia un modulo en especifico
void reenviarDatosAModulo(){
  byte moduloDestino=bufferDatosCentralDomotica.pop();
  direccionModuloDestino=direccionesDestinoModulos.get((int)moduloDestino-1);
  byte tramaDatos[bufferDatosCentralDomotica.size()];
  for(int i=0;i<bufferDatosCentralDomotica.size();i++){
    tramaDatos[i]=bufferDatosCentralDomotica.get(i);
  }
  Tx16Request datosAEnviar=Tx16Request(direccionModuloDestino, tramaDatos, sizeof(tramaDatos));
  moduloXbee.send(datosAEnviar);
}
