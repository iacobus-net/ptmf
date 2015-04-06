//============================================================================
//
//	Copyright (c) 1999 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	File: CanalMulticastNoSeguro.java  1.0 13/09/99
//
//	Description: Class CanalMulticastNoSeguro.
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
 * Clase que representa un <STRONG><b>CANAL MULTICAST NO SEGURO</b></STRONG>.<br>
 * <b>Soporta la emisi�n/recepci�n de paquetes unicast o multicast.</b>
 * Posibilidad de utilizar una funci�n callback usando un thread para
 * notificar la recepci�n de paquetes de forma as�ncrona.
 * @version  1.0
 * @author M. Alejandro Garc�a Dom�nguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */

class CanalMulticastNoSeguro extends CanalMulticast
{
  /** Direcci�n Multicast */
  private Address  addressMcast = null;

  /** Socket multicast */
  private MulticastSocket  socketObj = null;

  /**  un buffer para los datagramas que se env�an. */
  private byte[]  sendBuf = null;

  /** Un paquete datagrama para enviar */
  private DatagramPacket  sendPacket = null;

  /** Un paquete datagrama para recibir */
  private DatagramPacket  recvPacket = null;

  /** Flag para indicar si hay m�todo callback */
  private boolean  callback = false;

  /** El objeto CanalCallback que implementa el m�todo callback si el flag
  * callback est� activado */
  private CanalCallback callbackObject = null;

  /** El argumento para el m�todo callback si el flag callback est� activado */
  private int callbackArgument = 0;

  /** Un thread para este canal multicast para notificaci�n as�ncrona mediante
   callbacks */
  private MulticastThreadNoSeguro  multicastThread = null;

  /** El ttl a usar para todos los paquetes que se env�en */
  private byte  TTL = 1;



  //==========================================================================
  /**
   * Constructor, requiere que se especifique la direcci�n del grupo multicast
   * al que se unir� (join) este host.
   * @param grupoMcast La direcci�n de grupo multicast a la que se unir� el
   *  socket (join). El puerto de la direcci�n se actualiza con el puerto que
   *  autom�ticamente asigne el sistema.
   * @exception PTMFExcepcion Error creando el socket o uni�ndose (join) al
   *  grupo multicast.
   */
  CanalMulticastNoSeguro(Address grupoMcast) throws PTMFExcepcion
  {
      this(grupoMcast,null);
  }

  //==========================================================================
  /**
   * Constructor, requiere que se especifique la direcci�n del grupo multicast
   * al que se unir� (join) este host y la direcci�n de la interfaz de red por
   * la que se enviar�n los paquetes, en vez de la interfaz por defecto del
   * sistema, esto es �til en host con varias interfaces de red.
   * @param grupoMcast La direcci�n de grupo multicast a la que unirse (join).
   *  El n�mero de puerto es actualizado al puerto actualmente usado.
   * @param ifMcast La direcci�n de la interfaz de red utilizada para enviar
   *  los paquetes multicast.
   * @exception PTMFExcepcion Error creando el socket o uni�ndose (join) al
   *  grupo multicast o estableciendo la interfaz de salida de los datos
   *  multicast.
   */
  CanalMulticastNoSeguro(Address grupoMcast,  Address ifMcast) throws PTMFExcepcion
  {
    final String  mn = "CanalMulticastNoSeguro.CanalMulticastNoSeguro(Address,Address)";

    //
    // Crear un socket multicast con la direcci�n de grupo especificada.
    //
    this.callback = false;

    try
    {
      if (grupoMcast.getPort() == 0)
         this.socketObj = new MulticastSocket();
      else
        this.socketObj = new MulticastSocket(grupoMcast.getPort());

      //Log.debug(Log.CANAL_MULTICAST,"CanalMulticastNoSeguro.CanalMulticastNoSeguro","Socket Multicast "+grupoMcast+"--> OK");


      if (this.socketObj == null)
         throw new PTMFExcepcion("Error en la creaci�n del socket multicast. Socket nulo.");

      //Buffer Emision a tama�o m�ximo... (64Kb)
      this.socketObj.setSendBufferSize    (PTMF.SIZE_BUFFER_SOCKET_EMISION);
      this.socketObj.setReceiveBufferSize (PTMF.SIZE_BUFFER_SOCKET_RECEPCION);
      //TAMA�O DE LA COLA DE EMISION....
      Log.debug(Log.CANAL_MULTICAST,mn,"Tama�o del Buffer Emision SocketMulticast : "+socketObj.getSendBufferSize());
      Log.debug(Log.CANAL_MULTICAST,mn,"Tama�o del Buffer Recepcion SocketMulticast : "+socketObj.getReceiveBufferSize());

      //
      // Seleccionar la interfaz multicast.
      //
      if ((ifMcast!=null) && (!ifMcast.getHostAddress().equals("0.0.0.0")) &&
          (!ifMcast.isMulticastAddress()))
      {
          this.socketObj.setInterface(ifMcast.getInetAddress());
          //Log.debug(Log.CANAL_MULTICAST,"CanalMulticastNoSeguro.CanalMulticastNoSeguro","Socket Multicast Interface "+ifMcast+"--> OK");
      }

      //
      //  Join al grupo multicast.
      //
      if (grupoMcast.isMulticastAddress())
      {
        this.socketObj.joinGroup(grupoMcast.getInetAddress());
        //Log.debug(Log.CANAL_MULTICAST,"CanalMulticastNoSeguro.CanalMulticastNoSeguro","Socket Multicast join --> OK");
      }

      // Almacenar la informaci�n de la direcci�n Multicast y del puerto
      // a la que el socket Multicast se ha enlazado...
      // Problema con getLocalAddresss(): devuelve 0.0.0.0 depu�s de enlazarse
      // al puerto especificado en la m�quina.
      // Log.log("DEPU 1","localAddress en Multicast: "+this.socketObj.getLocalAddress());

      this.addressMcast = new Address (grupoMcast.getInetAddress(), this.socketObj.getLocalPort());


      //Log.debug (Log.CANAL_MULTICAST,mn,"BUffer Recepcion:"+this.socketObj.getReceiveBufferSize());
      //Log.debug (Log.CANAL_MULTICAST,mn,"Buffer Emisi�n:"+this.socketObj.getSendBufferSize());

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
      // Si hay un thread de recepci�n activo, desactivarlo.
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

   //Log.debug(Log.CANAL_MULTICAST,"CanalMulticastNoSeguro.close","Socket Multicast close --> OK");
  }

  //==========================================================================
  /**
   * Envia los datos encapsulados en el objeto Buffer a la direcci�n destino especificada
   * por el objeto Address.
   * @param buf El buffer que contiene los datos a enviar.
   * @exception PTMFExcepcion Excepci�n gen�rica
   * @exception ParametroInvalidoExcepcion Par�metro incorrecto.
   * @exception IOException Error enviando los datos
   */
  public synchronized void send(Buffer buf)
   throws PTMFExcepcion, ParametroInvalidoExcepcion, IOException
  {
      send(buf,this.TTL);
  }

  //==========================================================================
  /**
   * Envia los datos encapsulados en el objeto Buffer a la direcci�n destino especificada
   * por el objeto Address.
   * @param buf El buffer que contiene los datos a enviar.
   * @param TTL TTL utilizado para enviar este buffer en vez de utilizar el TTL
   *   de la sesi�n Multicast.
   * @exception PTMFExcepcion Excepci�n gen�rica
   * @exception ParametroInvalidoExcepcion Par�metro incorrecto.
   * @exception IOException Error enviando los datos
   */
long nBytesEnviados = 0;
  public synchronized void send(Buffer buf,  byte TTL)
   throws PTMFExcepcion, ParametroInvalidoExcepcion, IOException
  {
    final String    mn = "CanalMulticastNoSeguro.send";

    //
    // Enviar los datos si el socket est� abierto.
    //
    if (this.socketObj == null)
    {
      throw new PTMFExcepcion(mn, "El socket no est� abierto.");
    }

    //
    // verificar que el buffer y la direcci�n no son null
    //
    if (buf == null)
      throw new ParametroInvalidoExcepcion(mn, "Argumentos requeridos err�neos. Buffer nulo.");


    if ((buf.getBuffer() == null) || (buf.getLength() <= 0))
    {
        throw new ParametroInvalidoExcepcion(mn, "Buffer err�neo.");
    }


    //
    // Enviar los datos del buffer a la direcci�n destino.
    //

    if (this.sendPacket == null)
    {
      //Log.log(mn,"Datagrama: longitud:"+buf.getLength()+" destino: "+this.addressMcast);


      this.sendPacket = new DatagramPacket(buf.getBuffer(), 0/*buf.getOffset()*/,
                        buf.getLength(),this.addressMcast.getInetAddress(),this.addressMcast.getPort());
      if (this.sendPacket == null)
      {
        throw new PTMFExcepcion(mn, "No se pudo crear el datagrama de emisi�n.");
      }
    }
    else
    {
        //Log.log("CanalMulticastNoSeguro.send","Datagrama: longitud:"+buf.getLength()+" destino: "+this.addressMcast+" TTL--> "+TTL);

        this.sendPacket.setData(buf.getBuffer(),0/*buf.getOffset()*/,buf.getLength());
        this.sendPacket.setAddress(this.addressMcast.getInetAddress());
        this.sendPacket.setPort(this.addressMcast.getPort());
    }

    //
    // ENVIAR!!
    //
    //Log.log(mn,"Datagrama: longitud:"+buf.getLength()+" destino: "+this.addressMcast+" TTL--> "+TTL);
    this.socketObj.send(this.sendPacket,TTL);
    nBytesEnviados += sendPacket.getLength();
    //Log.log ("NUMERO BYTES ENVIADOS ACUMULADOS : " + nBytesEnviados,"");
  }

  //==========================================================================
  /**
   * Recibir datos. Este m�todo es s�ncrono (bloqueante).<br>
   * <b>No debe de ser usado si existe un m�todo callback registrado.</b>
   * Este m�todo no puede ser sincronizado porque es s�ncrono y bloquear�a
   * a los otros.
   * @param buf El buffer donde se almacenar�n los datos recibidos.
   * @param src Objeto Address donde se almacenar� la direcci�n del host que
   *  envi� los datos recibidos. Siempre es una direcci�n unicast.
   * @exception PTMFExcepcion Excepci�n gen�rica
   * @exception ParametroInvalidoExcepcion Par�metro incorrecto.
   * @exception IOException Error enviando los datos
   */
  public void receive(Buffer buf, Address src)
   throws PTMFExcepcion, ParametroInvalidoExcepcion, IOException
  {
    final String    mn = "CanalMulticastNoSeguro.receive(Buffer,Address)";

    //Log.debug(Log.CANAL_MULTICAST,"CanalMulticastNoSeguro.receive","Socket Multicast receive.");

    //
    // Recibir datos s�lo si el socket est� abierto y no hay un m�todo callback
    // registrado.
    //

    if (this.socketObj == null)
    {
      throw new PTMFExcepcion(mn, "El socket no est� abierto.");
    }

    if (this.callback)
    {
      throw new PTMFExcepcion(mn,"No se puede utilizar el m�todo receive. Existe un m�todo callback registrado.");
    }

    //
    // Verificar que el buffer y la direcci�n no es null.
    //

    if ((buf == null) || (src == null))
    {
      throw new ParametroInvalidoExcepcion(mn, "Par�metros inv�lidos.");
    }
    if ((buf.getBuffer() == null) || (buf.getMaxLength() <= 0))
    {
      throw new PTMFExcepcion(mn, "Buffer nulo o de tama�o err�neo.");
    }

    //
    // Recibir el datagrama. Obtener la direcci�n del emisor.
    //
    if (this.recvPacket == null)
    {
      this.recvPacket = new DatagramPacket(buf.getBuffer(), buf.getMaxLength());

      if (this.recvPacket == null)
      {
        throw new PTMFExcepcion(mn, "No se pudo obtener el datagrama de recepci�n.");
      }
    }
    else
    {
      this.recvPacket.setData(buf.getBuffer());
      this.recvPacket.setLength(buf.getMaxLength());
    }

    //
    // RECIBIR!!
    //
    this.socketObj.receive(this.recvPacket);

    buf.setOffset(0);
    buf.setLength( this.recvPacket.getLength());

    try{
      src = new Address(this.recvPacket.getAddress(),this.recvPacket.getPort());
    }
    catch(ParametroInvalidoExcepcion e)
    {
      Log.log(mn,"Direcci�n de host desconocido.");
    }

  }

  //==========================================================================
  /**
   * Devuelve el tama�o pr�ctico m�ximo para el canal en bytes.
   * @return El tama�o pr�ctico m�ximo para el canal en bytes.
   */
  public int getMaxPacketSize()
  {

    //
    // Devolver un tama�o m�ximo adecuado, en bytes.
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
    final String  mn = "CanalMulticastNoSeguro.setTTL(byte)";

    //
    // Establecer el ttl para los paquetes de salida si el socket est� abierto.
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
   * M�todo toString()
   * @return cadena identificativa
   */
   public String toString()
   {
    return (this.addressMcast.toString());
   }

  //==========================================================================
  /**
   * Activa el m�todo callback.  No es necesario llamar a DisableCallback()
   * antes de cada llamada a SetCallback().
   * @param obj El objeto callback.
   * @param arg El argumento callback.
   * @exception PTMFExcepcion
   * @exception ParametroInvalidoExcepcion
   */
  public synchronized void setCallback(CanalCallback obj, int arg)
   throws PTMFExcepcion,ParametroInvalidoExcepcion
  {
    final String  mn = "CanalMulticastNoSeguro.setCallback(CanalCallback,int)";

    //
    // Verificar que el socket est� abierto y que el objeto callback no es null.
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
    // Si a�n no hay activo un thread para el canal, hacer el socket
    // as�ncrono (no bloqueante) con un tiempo de 1 mlseg y crear el
    // el thread de recepci�n.
    //

    if (!this.callback)
    {
      this.multicastThread = new MulticastThreadNoSeguro(this, this.socketObj);
      this.multicastThread.start();
    }

    if(this.multicastThread != null)
    {
      this.multicastThread.callbackObject=this.callbackObject;
      this.multicastThread.callbackArgument=this.callbackArgument;
    }

    //Log.debug(Log.CANAL_MULTICAST,"CanalMulticastNoSeguro","Callback registrado: obj:"+obj+" arg:"+arg);

    //
    // El callback est� activado!!
    //
    this.callback = true;

  }

  //==========================================================================
  /**
   * Desactivar cualquier callback
   */
  public synchronized void disableCallback()
  {
    final String  mn = "CanalMulticastNoSeguro.disableCallback()";

    //
    // Verificar que el socket est� abierto
    //

    if (this.socketObj == null)
    {
      //Log.debug (Log.SOCKET,mn, "Socket no abierto.");
      return;
    }

    //
    // si un thread receptor est� activo, establecer el socket a s�ncrono
    // (bloqueante) y cancelar el thread de recepci�n.
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
   * Devuelve la direcci�n Multicast del canal
   * @return Address
   */
  Address getAddressMulticast()
  {
    return this.addressMcast;
  }


}


/**
 * Thread de recepci�n del Canal Multicast. Thread demonio.
 * Llama al m�todo receive() del socket hasta que se reciben datos, despu�s
 * llama al m�todo canalCallback() del objeto callback pas�ndole el argumento,
 * un buffer que contiene los datos recibidos y la direcci�n del emisor de los
 * datos. <br>
 * Esta clase no es thread-safe.
 * @version  1.0
 * @author M. Alejandro Garc�a Dom�nguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */

class MulticastThreadNoSeguro extends Thread
{
  /** El canal multicast que cre� este thread. */
  private CanalMulticastNoSeguro  canalObj = null;

  /** El socket multicast sobre el que se recibir�n los datos. */
  private MulticastSocket  socketObj = null;

  /** El objeto CanalCallback */
  CanalCallback  callbackObject = null;

  /** El argumento para el m�todo canalCallback del objeto callback. */
  int  callbackArgument = 0;

  /** Un flag utilizado para para este thread. */
  private boolean  runFlag = true;


  //==========================================================================
  /**
   * Constructor. Establece el thread como un thread demonio.
   */
  public MulticastThreadNoSeguro(CanalMulticastNoSeguro canal, MulticastSocket sock)
  {
    super("MulticastThread");

    setDaemon(true);

    //M�xima Prioridad...
    //this.setPriority(Thread.MAX_PRIORITY);

    //
    // Almacenar el canal y el socket.
    //

    this.canalObj = canal;
    this.socketObj  = sock;
  }

  //==========================================================================
  /**
   * El m�todo run del thread. Cada vez que se recibe datos, este m�todo pregunta
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
    Buffer          buf = null;
    Address         src = null;
    Buffer          bufferReturn = null;
    int              nBytesRecibidos = 0;
    int iCont = 0;

    //
    // reservar un Buffer. Crear un objeto Address.
    //

    try{
      buf = new Buffer(PTMF.TPDU_MAX_SIZE);
    }
    catch(PTMFExcepcion e)
    {
      Log.log(mn, "Error en la creaci�n del buffer.");
      return;
    }
    catch(ParametroInvalidoExcepcion e)
    {
      Log.log(mn, "Error en la creaci�n del buffer.");
      return;
    }


    if (buf == null)
    {
      Log.log(mn, "Error en la creaci�n del buffer.");
      return;
    }

    localPacket = new DatagramPacket(buf.getBuffer(), buf.getMaxLength());

    if (localPacket == null)
    {
      Log.log(mn, "Error en la creaci�n del datagrama.");
      return;
    }

    try
    {
        this.socketObj.setSoTimeout(0);//(500);
    }
    catch (SocketException se)
    {
        Log.log(mn, "Excepci�n en el timeout del socket.");
    }


    //
    // Bucle infinito, llamar a receive() sobre el socket. Si se reciben
    // datos, llamar al m�todo callback.
    //
    forever = true;
    while (forever)
    {
      //buf.setMaxLength (PTMF.TPDU_MAX_SIZE);
      localPacket.setLength(buf.getMaxLength());


      try
      {
         //Temporizador.sleep(1);
         this.socketObj.receive(localPacket);

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

      buf.setOffset(0);
      buf.setLength(localPacket.getLength());
      //buf.setMaxLength (localPacket.getLength());
     // nBytesRecibidos += localPacket.getLength();
      //iCont ++;

 //     if( (iCont%10) == 0)
   //     Log.log ("NUMERO BYTES RECIBIDOS ACUMULADOS : " + nBytesRecibidos,"");

      if (buf.getLength() > 0)
      {
        try
        {
          src = new Address(localPacket.getAddress(),localPacket.getPort());

        }
        catch(ParametroInvalidoExcepcion e)
        {
          Log.log(mn,"Direcci�n de host desconocida "+localPacket.getAddress().getHostAddress());
        }


        if (this.callbackObject != null)
        {
          try{
           bufferReturn = new Buffer (localPacket.getLength());
           bufferReturn.addBytes (buf,0,0,localPacket.getLength());
           this.callbackObject.canalCallback(this.callbackArgument,bufferReturn,src);
          }catch (ParametroInvalidoExcepcion e) {Log.log(mn,e.toString());}
          catch (PTMFExcepcion e){Log.log (mn,e.toString());}
         }

        if (!this.runFlag)
          return;
      }

      //Temporizador.yield();
    }
  }


  //==========================================================================
  /**
   * El m�todo stop del thread. Pone a false el flag runFlag provocando que
   * el thread finalice. No llama al m�todo stop() de la clase Thread.
   */
  public void stopThread()
  {
     runFlag = false;
  }

}
