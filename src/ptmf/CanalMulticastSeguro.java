//============================================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	File: CanalMulticastSeguro.java  1.0 13/09/99
//
//
//	Description: Clase CanalMulticastSeguro.
//
//  Historial: 
//	14/10/2014 Change Licence to LGPL
//
// 	Authors: 
//		 Alejandro Garcia Dominguez (alejandro.garcia.dominguez@gmail.com)
//		 Antonio Berrocal Piris (antonioberrocalpiris@gmail.com)
//
//
//      This file is part of PTMF 
//
//      PTMF is free software: you can redistribute it and/or modify
//      it under the terms of the Lesser GNU General Public License as published by
//      the Free Software Foundation, either version 3 of the License, or
//      (at your option) any later version.
//
//      PTMF is distributed in the hope that it will be useful,
//      but WITHOUT ANY WARRANTY; without even the implied warranty of
//      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//      Lesser GNU General Public License for more details.
//
//      You should have received a copy of the Lesser GNU General Public License
//      along with PTMF.  If not, see <http://www.gnu.org/licenses/>.
//
//----------------------------------------------------------------------------


package ptmf;

import java.io.*;
import java.util.*;
import java.net.*;


/**
 * Clase que representa un <STRONG><b>CANAL MULTICAST SEGURO</b></STRONG>.<br>
 * <b>Soporta la emisión/recepción de paquetes unicast o multicast.</b>
 * Posibilidad de utilizar una función callback usando un thread para
 * notificar la recepción de paquetes de forma asíncrona.<br><br>
 *
 * Esta clase utiliza dos objetos javax.crypto.Cipher para realizar la
 * codificación/decofificación de los datos que se le pase.<br>
 * Los datagramas son enviados codificados, debido a la naturaleza de los datagramas
 * (pueden perderse,llegar desordenados, duplicados, etc.) <strong> Los objetos Codificador y
 * Descodificador deben de ser iniciados con un algoritmo en MODO ECB, por
 * ejemplo "RC2/ECB/PKCS5Padding".</STRONG>
 *
 * De esta manera se puede descodificar cada datagrama de forma independiente,
 * en otros modos de codificación como CBC o PCBC los datos codificados
 * dependen de todos los datos anteriores ya codificados, en estos modos se
 * realizan operaciones XOR con los datos anteriores, una pérdida de un
 * datagrama o la llegada desordenada o duplicada implicaría que la
 * decodificación no se realice bien, debido a que los datagramas deberían
 * de llegar en el mismo orden en el que se codificaron.<br>
 * <STRONG> Si el usuario desea aumentar la seguridad deberá codificar los
 * datos que transmita a través de PTMF.</STRONG>.
 *
 * @version  1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */

class CanalMulticastSeguro extends CanalMulticast
{
  /** Dirección Multicast */
  private Address  addressMcast = null;

  /** Socket multicast */
  private MulticastSocket  socketObj = null;

  /**  un buffer para los datagramas que se envían. */
  private byte[]  sendBuf = null;

  /** Un paquete datagrama para enviar */
  private DatagramPacket  sendPacket = null;

  /** Un paquete datagrama para recibir */
  private DatagramPacket  recvPacket = null;

  /** Flag para indicar si hay método callback */
  private boolean  callback = false;

  /** El objeto CanalCallback que implementa el método callback si el flag
  * callback está activado */
  private CanalCallback callbackObject = null;

  /** El argumento para el método callback si el flag callback está activado */
  private int callbackArgument = 0;

  /** Un thread para este canal multicast para notificación asíncrona mediante
   callbacks */
  private MulticastThreadSeguro  multicastThread = null;

  /** El ttl a usar para todos los paquetes que se envíen */
  private byte  TTL = 1;

  /** Objeto Cipher utilizado para la Codificación*/
  private javax.crypto.Cipher cipher = null;

  /** Objeto Cipher utilizado para la Descodificación*/
  javax.crypto.Cipher unCipher = null;

  //==========================================================================
  /**
   * Constructor, requiere que se especifique la dirección del grupo multicast
   * al que se unirá (join) este host y la dirección de la interfaz de red por
   * la que se enviarán los paquetes, en vez de la interfaz por defecto del
   * sistema, esto es útil en host con varias interfaces de red.
   * @param grupoMcast La dirección de grupo multicast a la que unirse (join).
   *  El número de puerto es actualizado al puerto actualmente usado.
   * @param ifMcast La dirección de la interfaz de red utilizada para enviar
   *  los paquetes multicast.
   * @param cipher Objeto javax.crypto.Cipher utilizado para codificar
   * @param unCipher Objeto javax.crypto.Cipher utilizado para descodificar.
   * @exception PTMFExcepcion Error creando el socket o uniéndose (join) al
   *  grupo multicast o estableciendo la interfaz de salida de los datos
   *  multicast.
   */
  CanalMulticastSeguro(Address grupoMcast,  Address ifMcast
   ,javax.crypto.Cipher cipher, javax.crypto.Cipher unCipher) throws PTMFExcepcion
  {
    final String  mn = "CanalMulticastSeguro.CanalMulticastSeguro(Address,Address,javax.crypto.Cipher)";

    //Cipher
    this.cipher = cipher;
    this.unCipher = unCipher;

    if(cipher == null)
      throw new PTMFExcepcion("Objeto Cipher Nulo");

    if(unCipher == null)
      throw new PTMFExcepcion("Objeto unCipher Nulo");

    //
    // Crear un socket multicast con la dirección de grupo especificada.
    //
    this.callback = false;

    try
    {
      if (grupoMcast.getPort() == 0)
         this.socketObj = new MulticastSocket();
      else
        this.socketObj = new MulticastSocket(grupoMcast.getPort());

      //Log.debug(Log.CANAL_MULTICAST,"CanalMulticastSeguro.CanalMulticastSeguro","Socket Multicast "+grupoMcast+"--> OK");


      if (this.socketObj == null)
         throw new PTMFExcepcion("Error en la creación del socket multicast. Socket nulo.");

      //Buffer Emision a tamaño máximo... (64Kb)
      this.socketObj.setSendBufferSize    (PTMF.SIZE_BUFFER_SOCKET_EMISION);
      this.socketObj.setReceiveBufferSize (PTMF.SIZE_BUFFER_SOCKET_RECEPCION);
      //TAMAÑO DE LA COLA DE EMISION....
      Log.log("Tamaño del Buffer Socket Emision: "+socketObj.getSendBufferSize(),"");
      Log.log("TAmaño del Buffer Socket Recepcion: "+socketObj.getReceiveBufferSize(),"");

      //
      // Seleccionar la interfaz multicast.
      //
      if ((ifMcast!=null) && (!ifMcast.getHostAddress().equals("0.0.0.0")) &&
          (!ifMcast.isMulticastAddress()))
      {
          this.socketObj.setInterface(ifMcast.getInetAddress());
          //Log.debug(Log.CANAL_MULTICAST,"CanalMulticastSeguro.CanalMulticastSeguro","Socket Multicast Interface "+ifMcast+"--> OK");
      }

      //
      //  Join al grupo multicast.
      //
      if (grupoMcast.isMulticastAddress())
      {
        this.socketObj.joinGroup(grupoMcast.getInetAddress());
        //Log.debug(Log.CANAL_MULTICAST,"CanalMulticastSeguro.CanalMulticastSeguro","Socket Multicast join --> OK");
      }

      // Almacenar la información de la dirección Multicast y del puerto
      // a la que el socket Multicast se ha enlazado...
      // Problema con getLocalAddresss(): devuelve 0.0.0.0 depués de enlazarse
      // al puerto especificado en la máquina.
      // Log.log("DEPU 1","localAddress en Multicast: "+this.socketObj.getLocalAddress());

      this.addressMcast = new Address (grupoMcast.getInetAddress(), this.socketObj.getLocalPort());


      //Log.debug (Log.CANAL_MULTICAST,mn,"BUffer Recepcion:"+this.socketObj.getReceiveBufferSize());
      //Log.debug (Log.CANAL_MULTICAST,mn,"Buffer Emisión:"+this.socketObj.getSendBufferSize());

    }
    catch(ParametroInvalidoExcepcion ioe)
    {
      throw new PTMFExcepcion(mn,ioe.getMessage());
    }
    catch(IOException ioe)
    {
      throw new PTMFExcepcion(mn,ioe.getMessage());
    }

   }


  //==========================================================================
  /**
   * Cierra el socket y para cualquier thread callback.
   */
  public synchronized void close()
  {

    //
    // Si hay un socket abierto, cerrarlo.
    //

    if (this.socketObj != null)
    {

      //
      // Si hay un thread de recepción activo, desactivarlo.
      //

      if (this.callback)
      {
        this.multicastThread.stopThread();
      }

      this.socketObj.close();

      this.socketObj       = null;
      this.callback        = false;
      this.callbackObject  = null;
      this.callbackArgument= 0;
      this.multicastThread = null;
   }

   //Log.debug(Log.CANAL_MULTICAST,"CanalMulticastSeguro.close","Socket Multicast close --> OK");
  }

  //==========================================================================
  /**
   * Envia los datos encapsulados en el objeto Buffer a la dirección destino especificada
   * por el objeto Address.
   * @param buf El buffer que contiene los datos a enviar.
   * @exception PTMFExcepcion Excepción genérica
   * @exception ParametroInvalidoExcepcion Parámetro incorrecto.
   * @exception IOException Error enviando los datos
   */
  public synchronized void send(Buffer buf)
   throws PTMFExcepcion, ParametroInvalidoExcepcion, IOException
  {
      send(buf,this.TTL);
  }

  //==========================================================================
  /**
   * Envia los datos encapsulados en el objeto Buffer a la dirección destino especificada
   * por el objeto Address.
   * @param buf El buffer que contiene los datos a enviar.
   * @param TTL TTL utilizado para enviar este buffer en vez de utilizar el TTL
   *   de la sesión Multicast.
   * @exception PTMFExcepcion Excepción genérica
   * @exception ParametroInvalidoExcepcion Parámetro incorrecto.
   * @exception IOException Error enviando los datos
   */
long nBytesEnviados = 0;
  public synchronized void send(Buffer buf,  byte TTL)
   throws PTMFExcepcion, ParametroInvalidoExcepcion, IOException
  {
    final String    mn = "CanalMulticastSeguro.send";


    //
    // Enviar los datos si el socket está abierto.
    //
    if (this.socketObj == null)
    {
      throw new PTMFExcepcion(mn, "El socket no está abierto.");
    }

    //
    // verificar que el buffer y la dirección no son null
    //
    if (buf == null)
      throw new ParametroInvalidoExcepcion(mn, "Argumentos requeridos erróneos. Buffer nulo.");


    if ((buf.getBuffer() == null) || (buf.getLength() <= 0))
    {
        throw new ParametroInvalidoExcepcion(mn, "Buffer erróneo.");
    }


    //
    // Enviar los datos del buffer a la dirección destino.
    //
    try
    {
     if (this.sendPacket == null)
     {
      //Log.log(mn,"Datagrama: longitud:"+buf.getLength()+" destino: "+this.addressMcast);

        byte[] aCifrado = this.cipher.doFinal(buf.getBuffer(),0,buf.getLength());

        this.sendPacket = new DatagramPacket(aCifrado, 0/*buf.getOffset()*/,
                        aCifrado.length,this.addressMcast.getInetAddress(),this.addressMcast.getPort());

      if (this.sendPacket == null)
      {
        throw new PTMFExcepcion(mn, "No se pudo crear el datagrama de emisión.");
      }
     }
     else
     {
        ////Log.debug(Log.CANAL_MULTICAST,"CanalMulticastSeguro.send","Datagrama: longitud:"+buf.getLength()+" destino: "+this.addressMcast);
        byte[] aCifrado = this.cipher.doFinal(buf.getBuffer(),0,buf.getLength());

        this.sendPacket.setData(aCifrado,0/*buf.getOffset()*/,aCifrado.length);
        this.sendPacket.setAddress(this.addressMcast.getInetAddress());
        this.sendPacket.setPort(this.addressMcast.getPort());
     }
    }
    catch(javax.crypto.IllegalBlockSizeException e)
    {
       throw new PTMFExcepcion(e.toString());
    }
    catch(javax.crypto.BadPaddingException e)
    {
       throw new PTMFExcepcion(e.toString());
    }

    //
    // ENVIAR!!
    //
    // Log.log(mn,"Datagrama: longitud:"+buf.getLength()+" destino: "+this.addressMcast);

    this.socketObj.send(this.sendPacket,TTL);
    nBytesEnviados += sendPacket.getLength();
    //Log.log ("NUMERO BYTES ENVIADOS ACUMULADOS : " + nBytesEnviados,"");
  }

  //==========================================================================
  /**
   * Recibir datos. Este método es síncrono (bloqueante).<br>
   * <b>No debe de ser usado si existe un método callback registrado.</b>
   * Este método no puede ser sincronizado porque es síncrono y bloquearía
   * a los otros.
   * @param buf El buffer donde se almacenarán los datos recibidos.
   * @param src Objeto Address donde se almacenará la dirección del host que
   *  envió los datos recibidos. Siempre es una dirección unicast.
   * @exception PTMFExcepcion Excepción genérica
   * @exception ParametroInvalidoExcepcion Parámetro incorrecto.
   * @exception IOException Error enviando los datos
   */
  public void receive(Buffer buf, Address src)
   throws PTMFExcepcion, ParametroInvalidoExcepcion, IOException
  {
    final String    mn = "CanalMulticastSeguro.receive(Buffer,Address)";
    Buffer bufCodificado = null;

    //Log.debug(Log.CANAL_MULTICAST,"CanalMulticastSeguro.receive","Socket Multicast receive.");

    //
    // Recibir datos sólo si el socket está abierto y no hay un método callback
    // registrado.
    //

    if (this.socketObj == null)
    {
      throw new PTMFExcepcion(mn, "El socket no está abierto.");
    }

    if (this.callback)
    {
      throw new PTMFExcepcion(mn,"No se puede utilizar el método receive. Existe un método callback registrado.");
    }

    //
    // Verificar que el buffer y la dirección no es null.
    //

    if ((buf == null) || (src == null))
    {
      throw new ParametroInvalidoExcepcion(mn, "Parámetros inválidos.");
    }
    if ((buf.getBuffer() == null) || (buf.getMaxLength() <= 0))
    {
      throw new PTMFExcepcion(mn, "Buffer nulo o de tamaño erróneo.");
    }

    //Crear Buffer Codificado.
    bufCodificado = new Buffer(buf.getMaxLength());

    //
    // Recibir el datagrama. Obtener la dirección del emisor.
    //
    if (this.recvPacket == null)
    {
      this.recvPacket = new DatagramPacket(bufCodificado.getBuffer(), bufCodificado.getMaxLength());

      if (this.recvPacket == null)
      {
        throw new PTMFExcepcion(mn, "No se pudo obtener el datagrama de recepción.");
      }
    }
    else
    {
      this.recvPacket.setData(bufCodificado.getBuffer());
      this.recvPacket.setLength(bufCodificado.getMaxLength());
    }

    //
    // RECIBIR!!

    this.socketObj.receive(this.recvPacket);

    try
    {
      //Descodificar
      byte[] aDescodificado = this.unCipher.doFinal(bufCodificado.getBuffer(),0,bufCodificado.getMaxLength());

      //Copiar en el Buffer los bytes descodificados...
      buf.addBytes(new Buffer(aDescodificado),0,0,this.recvPacket.getLength());
    }
    catch(javax.crypto.IllegalBlockSizeException e)
    {
       throw new PTMFExcepcion(e.toString());
    }
    catch(javax.crypto.BadPaddingException e)
    {
       throw new PTMFExcepcion(e.toString());
    }

    buf.setOffset(0);
    buf.setLength( this.recvPacket.getLength());

    try{
      src = new Address(this.recvPacket.getAddress(),this.recvPacket.getPort());
    }
    catch(ParametroInvalidoExcepcion e)
    {
      Log.log(mn,"Dirección de host desconocido.");
    }

  }

  //==========================================================================
  /**
   * Devuelve el tamaño práctico máximo para el canal en bytes.
   * @return El tamaño práctico máximo para el canal en bytes.
   */
  public int getMaxPacketSize()
  {

    //
    // Devolver un tamaño máximo adecuado, en bytes.
    //
    return (PTMF.TPDU_MAX_SIZE );
  }

  //==========================================================================
  /**
   * Establece el TTL para todos los paquetes de salida.
   * @param ttl El valor del ttl.
   */
  public synchronized void setTTL(byte ttl)
  {
    final String  mn = "CanalMulticastSeguro.setTTL(byte)";

    //
    // Establecer el ttl para los paquetes de salida si el socket está abierto.
    //

    if (this.socketObj == null)
    {
      //Log.debug (Log.SOCKET,mn, "Socket no abierto.");
      return;
    }

    this.TTL = ttl;
  }

   //==========================================================================
  /**
   * Método toString()
   * @return cadena identificativa
   */
   public String toString()
   {
    return (this.addressMcast.toString());
   }

  //==========================================================================
  /**
   * Activa el método callback.  No es necesario llamar a DisableCallback()
   * antes de cada llamada a SetCallback().
   * @param obj El objeto callback.
   * @param arg El argumento callback.
   * @exception PTMFExcepcion
   * @exception ParametroInvalidoExcepcion
   */
  public synchronized void setCallback(CanalCallback obj, int arg)
   throws PTMFExcepcion,ParametroInvalidoExcepcion
  {
    final String  mn = "CanalMulticastSeguro.setCallback(CanalCallback,int)";

    //
    // Verificar que el socket está abierto y que el objeto callback no es null.
    //

    if (this.socketObj == null)
    {
      throw new PTMFExcepcion(mn, "Socket no abierto.");
    }

    if (obj == null)
    {
      throw new ParametroInvalidoExcepcion(mn, "Objeto callback nulo.");
    }

    //
    // Almacenar el objeto callback y el argumento.
    //

    this.callbackObject   = obj;
    this.callbackArgument = arg;

    //
    // Si aún no hay activo un thread para el canal, hacer el socket
    // asíncrono (no bloqueante) con un tiempo de 1 mlseg y crear el
    // el thread de recepción.
    //

    if (!this.callback)
    {
      this.multicastThread = new MulticastThreadSeguro(this, this.socketObj);
      this.multicastThread.start();
    }

    if(this.multicastThread != null)
    {
      this.multicastThread.callbackObject=this.callbackObject;
      this.multicastThread.callbackArgument=this.callbackArgument;
    }

    //Log.debug(Log.CANAL_MULTICAST,"CanalMulticastSeguro","Callback registrado: obj:"+obj+" arg:"+arg);

    //
    // El callback está activado!!
    //
    this.callback = true;

  }

  //==========================================================================
  /**
   * Desactivar cualquier callback
   */
  public synchronized void disableCallback()
  {
    final String  mn = "CanalMulticastSeguro.disableCallback()";

    //
    // Verificar que el socket está abierto
    //

    if (this.socketObj == null)
    {
      //Log.debug (Log.SOCKET,mn, "Socket no abierto.");
      return;
    }

    //
    // si un thread receptor está activo, establecer el socket a síncrono
    // (bloqueante) y cancelar el thread de recepción.
    //

    if (this.callback)
    {
      this.multicastThread.stopThread();

      try
      {
        this.socketObj.setSoTimeout(0);
      }
      catch (SocketException se)
      {
        //Log.debug (Log.SOCKET,mn, "Socket timeout exception.");
      }
    }

    //
    // Limpiar el flag callback, el objeto y el argumento.
    //

    this.callback        = false;
    this.callbackObject          = null;
    this.callbackArgument        = 0;
    this.multicastThread = null;
  }


  //==========================================================================
  /**
   * Devuelve la dirección Multicast del canal
   * @return Address
   */
  Address getAddressMulticast()
  {
    return this.addressMcast;
  }


}


/**
 * Thread de recepción del Canal Multicast. Thread demonio.
 * Llama al método receive() del socket hasta que se reciben datos, después
 * llama al método canalCallback() del objeto callback pasándole el argumento,
 * un buffer que contiene los datos recibidos y la dirección del emisor de los
 * datos. <br>
 * Esta clase no es thread-safe.
 * @version  1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */

class MulticastThreadSeguro extends Thread
{
  /** El canal multicast que creó este thread. */
  private CanalMulticastSeguro  canalObj = null;

  /** El socket multicast sobre el que se recibirán los datos. */
  private MulticastSocket  socketObj = null;

  /** El objeto CanalCallback */
  CanalCallback  callbackObject = null;

  /** El argumento para el método canalCallback del objeto callback. */
  int  callbackArgument = 0;

  /** Un flag utilizado para para este thread. */
  private boolean  runFlag = true;


  //==========================================================================
  /**
   * Constructor. Establece el thread como un thread demonio.
   */
  public MulticastThreadSeguro(CanalMulticastSeguro canal, MulticastSocket sock)
  {
    super("MulticastThread");

    setDaemon(true);

    //Máxima Prioridad...
    //this.setPriority(Thread.MAX_PRIORITY);

    //
    // Almacenar el canal y el socket.
    //

    this.canalObj = canal;
    this.socketObj  = sock;
  }

  //==========================================================================
  /**
   * El método run del thread. Cada vez que se recibe datos, este método pregunta
   * a la clase canalMulticast cual es el objeto callback registrado y su
   * argumento.
   */
  public void run()
  {
    final String    mn = "MulticastThread.run()";
    DatagramPacket  localPacket;
    String          localAddress;
    InetAddress     localInetAddress;
    int             localPort;
    boolean         forever;
    Address         src = null;

    byte[]          abytesCodificado = null;
    byte[]          abytesDescodificado = null;
    Buffer          bufferReturn = null;

    int             nBytesRecibidos = 0;
    int iCont = 0;

    //
    // reservar un Buffer. Crear un objeto Address.
    //

    //try{
      abytesCodificado = new byte[PTMF.TPDU_MAX_SIZE];
    /*}
    catch(PTMFExcepcion e)
    {
      Log.log(mn, "Error en la creación del buffer.");
      return;
    }

    catch(ParametroInvalidoExcepcion e)
    {
      Log.log(mn, "Error en la creación del buffer.");
      return;
    }
    */

    if (abytesCodificado == null)
    {
      Log.log(mn, "Error en la creación del buffer.");
      return;
    }

    localPacket = new DatagramPacket(abytesCodificado,abytesCodificado.length);

    if (localPacket == null)
    {
      Log.log(mn, "Error en la creación del datagrama.");
      return;
    }

    try
    {
        this.socketObj.setSoTimeout(0);//(500);
    }
    catch (SocketException se)
    {
        Log.log(mn, "Excepción en el timeout del socket.");
    }



     //
     // Bucle infinito, llamar a receive() sobre el socket. Si se reciben
     // datos, llamar al método callback.
     //
     forever = true;
     while (forever)
     {
      try
      {
       bufferReturn = null;
       //Longitud del datagrama
       localPacket.setLength(abytesCodificado.length);

       //Recibir los datos.
       this.socketObj.receive(localPacket);

       //Descodificar los datos
       bufferReturn = new Buffer (this.canalObj.unCipher.doFinal(abytesCodificado,0,localPacket.getLength()));

       if(bufferReturn == null)
        Log.log("bufferReturn NULL!","");
      }
      catch(javax.crypto.IllegalBlockSizeException e)
      {
        Log.log(mn,e.toString());
      }
      catch(javax.crypto.BadPaddingException e)
      {
        Log.log(mn,e.toString());
      }

      catch (IOException ioe)
      {
        if (!this.runFlag)
        {
          return;
        }

        //Temporizador.yield();
        //Temporizador.sleep(50);

        continue;
      }

      if (!this.runFlag)
      {
        return;
      }

      //bufCodificado.setOffset(0);
      //bufCodificado.setLength(localPacket.getLength());
      //buf.setMaxLength (localPacket.getLength());
     // nBytesRecibidos += localPacket.getLength();
      //iCont ++;

 //     if( (iCont%10) == 0)
   //     Log.log ("NUMERO BYTES RECIBIDOS ACUMULADOS : " + nBytesRecibidos,"");

      if (bufferReturn.getLength() > 0)
      {
        try
        {
          src = new Address(localPacket.getAddress(),localPacket.getPort());

        }
        catch(ParametroInvalidoExcepcion e)
        {
          Log.log(mn,"Dirección de host desconocida "+localPacket.getAddress().getHostAddress());
        }


        if (this.callbackObject != null)
        {
          //try{
           //bufferReturn = new Buffer (bytesDescodificado.length);
           //bufferReturn.addBytes (newbytesDescodificado,0,0,localPacket.getLength());
           this.callbackObject.canalCallback(this.callbackArgument,bufferReturn,src);
          /*}catch (ParametroInvalidoExcepcion e) {Log.log(mn,e.toString());}
          catch (PTMFExcepcion e){Log.log (mn,e.toString());}
          */
         }

        if (!this.runFlag)
          return;
      }

      //Temporizador.yield();
    }
  }


  //==========================================================================
  /**
   * El método stop del thread. Pone a false el flag runFlag provocando que
   * el thread finalice. No llama al método stop() de la clase Thread.
   */
  public void stopThread()
  {
     runFlag = false;
  }

}
