//============================================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	File: CanalUnicastNoSeguro.java  1.0 15/09/99
//
//
//	Description: Class CanalUnicastNoSeguro.
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

package net.iacobus.ptmf;

import java.io.*;
import java.util.*;
import java.net.*;


/**
 * Esta clsae representa un canal unicast UDP/IP.
 * Soporta el env�o/recepci�n de paquetes UDP tanto unicast como multicast.
 * Permite el uso de callbacks para la notificaci�n de la recpci�n de datos.
 * Esta clase es thread-safe.
 * @version  1.0
 * @author M. Alejandro Garc�a Dom�nguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */

public class CanalUnicastNoSeguro extends CanalUnicast
{

  /**
   * Direcci�n Unicast del canal.
   */
  private Address addressUnicast  = null;


  /**
   * El socket unicast.
   */
  private MulticastSocket  socketObj = null;

  /**
   * Un buffer para los datagramas que se env�an.
   */
  private byte[]  sendBuf = null;


  /**
   * El paquete datagrama a recibir.
   */
  private DatagramPacket  recvPacket = null;

  /**
   * Flag para indicar si hay m�todo callback.
   */
  private boolean  callback = false;

  /**
   * El objeto CanalCallback que implementa el m�todo callback si el flag
   * callback est� activado
   */
  private CanalCallback  callbackObject = null;

  /**
   * El argumento para el m�todo callback si el flag callback est� activado
   */
  private int  callbackArgument = 0;

  /**
   * Un thread para este canal unicast para notificaci�n as�ncrona mediante
   * callbacks.
   */
  private UnicastThreadNoSeguro  unicastThread = null;

  /**
   * El ttl a usar para todos los paquetes que se env�en.
   */
  private byte  TTL = 1;



  //==========================================================================
  /**
   * El constructor que requiere que se especifique una direcci�n a la que
   * se enlazar� el socket (bind).
   * @param bindAddr La direcci�n unicast a la que se enlazar� el socket.
   * @exception PTMFExcepcion Socket nulo.
   * @exception ParametroInvalidoExcepcion Par�metro inv�lido
   * @exception IOException Error creando el socket o estableciendo la interfaz
   *  de salida de los datos multicast.
   */
  public CanalUnicastNoSeguro(Address bindAddr)
    throws PTMFExcepcion, ParametroInvalidoExcepcion, IOException
  {
    this(bindAddr,null);
  }

  //==========================================================================
  /**
   * Constructor que requiere una direcci�n unicast a la que enlazar el socket
   * (bind) y la direcci�n de ls interfaz por la que enviar los datos multicast.
   * @param bindAddr La direcci�n unicast.
   * @param mcastIF La direcci�n unicast de la interfaz a usar cuando se env�an
   *  datos multicast.
   * @exception UnknownHostException, PTMFExcepcion
   */
  public CanalUnicastNoSeguro(Address bindAddr,  Address ifMcast)
      throws PTMFExcepcion, IOException
  {
    final String  mn = "CanalUnicastNoSeguro.CanalUnicastNoSeguro";

    //
    // Crea un socket multicast usando la direcci�n especificada.
    // Necesitamos utilizar un socket multicast para poder especificar las
    // opciones del TTL y de la interfaz utilizada para enviar los datos.
    //

    this.callback = false;
    this.addressUnicast = (Address) bindAddr.clone();

    if (bindAddr.getPort() == 0)
    {
        this.socketObj = new MulticastSocket();
    }
    else
    {
        this.socketObj = new MulticastSocket(bindAddr.getPort());
    }

    if (this.socketObj == null)
    {
      throw new PTMFExcepcion(mn, "Error en la creaci�n del socket. Socket nulo.");
    }

    //Buffer Emision a tama�o m�ximo... (64Kb)
    this.socketObj.setSendBufferSize(1024 * 64);

    if(ifMcast!=null)
    {
      if (ifMcast.isMulticastAddress())
      {
        throw new PTMFExcepcion(mn, "No se puede establecer como interfaz una direcci�n multicast");
      }

      if (!ifMcast.getHostAddress().equals("0.0.0.0"))
      {
          this.socketObj.setInterface(ifMcast.getInetAddress());
      }
    }

    // Almacenar la informaci�n de la direcci�n Unicast y del puerto
    // a la que el socket Unicast se ha enlazado...
    // Problema: getLocalAddress() en el socket devuelve 0.0.0.0
    // Este m�todo es el suyo pero no devuelve la direcci�n bien. Creo :).
    // As� que obtengo la direcci�n unicast pasada en bindAddr.
    this.addressUnicast = new Address (bindAddr.getInetAddress(), this.socketObj.getLocalPort());

      //Buffer Emision a tama�o m�ximo... (64Kb)
      this.socketObj.setSendBufferSize    (PTMF.SIZE_BUFFER_SOCKET_EMISION);
      this.socketObj.setReceiveBufferSize (PTMF.SIZE_BUFFER_SOCKET_RECEPCION);
      //TAMA�O DE LA COLA DE EMISION....
      Log.debug(Log.CANAL_UNICAST,mn,"Tama�o del Buffer Emision SocketUnicast : "+socketObj.getSendBufferSize());
      Log.debug(Log.CANAL_UNICAST,mn,"Tama�o del Buffer Recepcion SocketUnicast : "+socketObj.getReceiveBufferSize());

  }


  //==========================================================================
  /**
   * Cerrar el socket y parar cualquier thread callback.
   */
  public synchronized void close()
  {

    //
    // Si el socket est� abierto, cerrarlo.
    //

    if (this.socketObj != null)
    {

      //
      // Si un thread de recpci�n est� activo, cancelarlo.
      //

      if (this.callback)
      {
        this.unicastThread.stopThread();
      }

      this.socketObj.close();

      this.socketObj     = null;
      this.callback      = false;
      this.callbackObject        = null;
      this.callbackArgument      = 0;
      this.unicastThread = null;
    }
  }


  //==========================================================================
  /**
   * Envia los datos encapsulados en el objeto Buffer a la direcci�n destino especificada
   * en la creaci�n del canal.
   * @param buf El buffer que contiene los datos a enviar.
   * @param dirUnicastDestino direcci�n unicast destino
   * @exception PTMFExcepcion Excepci�n gen�rica
   * @exception ParametroInvalidoExcepcion Alg�n par�metro es inv�lido.
   * @exception IOException Error enviando los datos por el socket.
   */
  public synchronized void send(Buffer buf,Address dirUnicastDestino)
     throws PTMFExcepcion, ParametroInvalidoExcepcion, IOException
  {
    final String    mn = "CanalUnicastNoSeguro.send(Buffer,Address)";


    //
    // Verificar que el socket est� abierto
    //
    if (this.socketObj == null)
    {
       throw new PTMFExcepcion(mn, "El socket no est� abierto.");
    }

    //
    // Verificar la direcci�n destino
    //
    if (dirUnicastDestino == null)
    {
       throw new PTMFExcepcion(mn, "No se ha especificado una direcci�n unicast destino.");
    }

    //
    // verificar que el buffer y la direcci�n no son null
    //
    if ((buf == null) || (this.addressUnicast == null))
    {
     throw new ParametroInvalidoExcepcion(mn, "Argumentos requeridos err�neos.");
    }
    if ((buf.getBuffer() == null) || (buf.getLength() <= 0))
    {
          throw new ParametroInvalidoExcepcion(mn, "Buffer err�neo. Nulo o longitud err�nea.");
    }


    //
    // Enviar los datos del buffer a la direcci�n destino.
    //
    DatagramPacket  sendPacket = new DatagramPacket(buf.getBuffer(), buf.getOffset(),
                        buf.getLength(),dirUnicastDestino.getInetAddress(),
                        dirUnicastDestino.getPort());

    if (sendPacket == null)
     {
        throw new PTMFExcepcion(mn, "No se pudo crear el datagrama de emisi�n.");
      }

    //
    // ENVIAR!!!
    //
    //Log.debug (Log.ACK,mn,"ENVIADO UNO UNICAST A: " + dirUnicastDestino.getPort());
    //Log.log(mn,"ENVIADO UNO UNICAST A: " + dirUnicastDestino.getPort());

    this.socketObj.send(sendPacket, this.TTL);

  }



  //==========================================================================
  /**
   * Recibir datos. <br><b>Este m�todo es s�ncrono (bloqueante).</b><br>
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
    final String    mn = "CanalUnicastNoSeguro.receive(Buffer,Address)";

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

    src = new Address(this.recvPacket.getAddress(),this.recvPacket.getPort());

    //
    // NOTA.NOTA.NOTA.NOTA.NOTA: VERIFICAR UNICAST PACKET DROP
    // Si la opci�n drop del paquete est� activada, ver si el paquete debe
    // de ser descartado.
    //
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
    return(PTMF.TPDU_MAX_SIZE);
  }


  //==========================================================================
  /**
   * Devuelve la direcci�n Unicast del canal
   * @return Address
   */
  Address getAddressUnicast()
  {
    return this.addressUnicast;
  }

  //==========================================================================
  /**
   * Establece el TTL para todos los paquetes de salida.
   * @param ttl El valor del ttl.
   */
  public synchronized void setTTL(byte ttl)
  {
    final String  mn = "CanalUnicastNoSeguro.setTTL(byte)";

    //
    // Establecer el ttl para los paquetes de salida si el socket est� abierto.
    //

    if (this.socketObj == null)
    {
      Log.log(mn, "Socket no abierto.");
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
    return (this.addressUnicast.toString());
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
    final String  mn = "CanalUnicastNoSeguro.setCallback(CanalCallback,int)";

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
      try
      {
        this.socketObj.setSoTimeout(0);
      }
      catch (SocketException se)
      {
        throw new PTMFExcepcion(mn, "Excepci�n en el timeout del socket.");
      }

      this.unicastThread = new UnicastThreadNoSeguro(this, this.socketObj);
      this.unicastThread.start();
    }

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
    final String  mn = "CanalMulticast.disableCallback()";

    //
    // Verificar que el socket est� abierto
    //

    if (this.socketObj == null)
    {
      Log.log(mn, "Socket no abierto.");
      return;
    }

    //
    // si un thread receptor est� activo, establecer el socket a s�ncrono
    // (bloqueante) y cancelar el thread de recepci�n.
    //

    if (this.callback)
    {
      this.unicastThread.stopThread();

      try
      {
        this.socketObj.setSoTimeout(0);
      }
      catch (SocketException se)
      {
        Log.log(mn, "Socket timeout exception.");
      }
    }

    //
    // Limpiar el flag callback, el objeto y el argumento.
    //

    this.callback = false;
    this.callbackObject = null;
    this.callbackArgument = 0;
    this.unicastThread = null;
  }

  //==========================================================================
  /**
   * Este m�todo establece el objeto calback y el argumento de la clase
   * MulticastThread
   */
  synchronized void getCallback(UnicastThreadNoSeguro uThread)
  {
    //
    // Establecer el objeto callback y el argumento para ese objeto
    //
    uThread.callbackObject=this.callbackObject;
    uThread.callbackArgument=this.callbackArgument;
  }
}


/**
 * Thread de recepci�n del Canal Unicast.<br> Thread demonio.<br>
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

class UnicastThreadNoSeguro extends Thread
{
 /** El canal unicast que cre� este thread. */
  private CanalUnicastNoSeguro  canalObj = null;

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
   public UnicastThreadNoSeguro(CanalUnicastNoSeguro canal, MulticastSocket sock)
  {
    super("UnicastThreadNoSeguro");

    setDaemon(true);


    //
    // Almacenar el canal y el socket.
    //

    this.canalObj = canal;
    this.socketObj  = sock;
  }

  //==========================================================================
  /**
   * El m�todo run del thread. Cada vez que se recibe datos, este m�todo pregunta
   * a la clase CanalUnicastNoSeguro cual es el objeto callback registrado y su
   * argumento.
   */
  public void run()
  {
    final String    mn = "UnicastThreadNoSeguro.run()";
    DatagramPacket  localPacket;
    String          localAddress;
    InetAddress     localInetAddress;
    int             localPort;
    boolean         forever;
    Buffer          buf = null;
    Buffer          bufferReturn = null;
    Address         src = null;

    //
    // Reservar un Buffer. Crear un objeto Address.
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

      localPacket.setLength(buf.getMaxLength());

      try
      {
        this.socketObj.receive(localPacket);
      }
      catch (IOException ioe)
      {
        if (!this.runFlag)
          return;

       // Temporizador.yield();

        continue;
      }


      if (!this.runFlag)
        return;

      buf.setOffset(0);
      buf.setLength(localPacket.getLength());

      if (buf.getLength() > 0)
      {
        try
        {
          src = new Address(localPacket.getAddress(),localPacket.getPort());
        }
        catch(ParametroInvalidoExcepcion e)
        {
          Log.log(mn,"Direcci�n de host desconocida " + localPacket.getAddress().getHostAddress());
        }


        //
        // Obtener el objeto callback para realizar la notificaci�n.
        //
        this.canalObj.getCallback(this);

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
        {
          return;
        }
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
