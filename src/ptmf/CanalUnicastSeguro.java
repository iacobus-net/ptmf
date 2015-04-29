//============================================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	File: CanalUnicastSeguro.java  1.0 15/09/99
//
//	Description: Clase CanalUnicastSeguro.
//
// 	Authors: 
//		 Alejandro García-Domínguez (alejandro.garcia.dominguez@gmail.com)
//		 Antonio Berrocal Piris (antonioberrocalpiris@gmail.com)
//
//  Historial: 
//  07.04.2015 Changed licence to Apache 2.0     
//
//  This file is part of PTMF 
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//----------------------------------------------------------------------------

package ptmf;

import java.io.*;
import java.util.*;
import java.net.*;


/**
 * Esta clsae representa un Canal Unicast UDP/IP SEGURO.
 * Soporta el envío/recepción de paquetes UDP tanto unicast como multicast.
 * Permite el uso de callbacks para la notificación de la recpción de datos.
 * Esta clase es thread-safe.
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

 * @version  1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */

public class CanalUnicastSeguro extends CanalUnicast
{

  /**
   * Dirección Unicast del canal.
   */
  private Address addressUnicast  = null;


  /**
   * El socket unicast.
   */
  private MulticastSocket  socketObj = null;

  /**
   * Un buffer para los datagramas que se envían.
   */
  private byte[]  sendBuf = null;


  /**
   * El paquete datagrama a recibir.
   */
  private DatagramPacket  recvPacket = null;

  /**
   * Flag para indicar si hay método callback.
   */
  private boolean  callback = false;

  /**
   * El objeto CanalCallback que implementa el método callback si el flag
   * callback está activado
   */
  private CanalCallback  callbackObject = null;

  /**
   * El argumento para el método callback si el flag callback está activado
   */
  private int  callbackArgument = 0;

  /**
   * Un thread para este canal unicast para notificación asíncrona mediante
   * callbacks.
   */
  private UnicastThreadSeguro  unicastThread = null;

  /**
   * El ttl a usar para todos los paquetes que se envíen.
   */
  private byte  TTL = 1;

  /** Objeto Cipher utilizado para la Codificación*/
  private javax.crypto.Cipher cipher = null;

  /** Objeto Cipher utilizado para la Descodificación*/
  javax.crypto.Cipher unCipher = null;


   //==========================================================================
  /**
   * Constructor que requiere una dirección unicast a la que enlazar el socket
   * (bind) y la dirección de ls interfaz por la que enviar los datos multicast.
   * @param bindAddr La dirección unicast.
   * @param mcastIF La dirección unicast de la interfaz a usar cuando se envían
   *  datos multicast.
   * @param cipher Objeto javax.crypto.Cipher utilizado para codificar
   * @param unCipher Objeto javax.crypto.Cipher utilizado para descodificar.
   * @exception UnknownHostException, PTMFExcepcion
   */
  public CanalUnicastSeguro(Address bindAddr,  Address ifMcast
   ,javax.crypto.Cipher cipher, javax.crypto.Cipher unCipher)
      throws PTMFExcepcion, IOException
  {
    final String  mn = "CanalUnicastSeguro.CanalUnicastSeguro";

    //Cipher
    this.cipher = cipher;
    this.unCipher = unCipher;

    if(cipher == null)
      throw new PTMFExcepcion("Objeto Cipher Nulo");

    if(unCipher == null)
      throw new PTMFExcepcion("Objeto unCipher Nulo");

    //
    // Crea un socket multicast usando la dirección especificada.
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
      throw new PTMFExcepcion(mn, "Error en la creación del socket. Socket nulo.");
    }

    //Buffer Emision a tamaño máximo... (64Kb)
    this.socketObj.setSendBufferSize(1024 * 64);

    if(ifMcast!=null)
    {
      if (ifMcast.isMulticastAddress())
      {
        throw new PTMFExcepcion(mn, "No se puede establecer como interfaz una dirección multicast");
      }

      if (!ifMcast.getHostAddress().equals("0.0.0.0"))
      {
          this.socketObj.setInterface(ifMcast.getInetAddress());
      }
    }

    // Almacenar la información de la dirección Unicast y del puerto
    // a la que el socket Unicast se ha enlazado...
    // Problema: getLocalAddress() en el socket devuelve 0.0.0.0
    // Este método es el suyo pero no devuelve la dirección bien. Creo :).
    // Así que obtengo la dirección unicast pasada en bindAddr.
    this.addressUnicast = new Address (bindAddr.getInetAddress(), this.socketObj.getLocalPort());

  }


  //==========================================================================
  /**
   * Cerrar el socket y parar cualquier thread callback.
   */
  public synchronized void close()
  {

    //
    // Si el socket está abierto, cerrarlo.
    //

    if (this.socketObj != null)
    {

      //
      // Si un thread de recpción está activo, cancelarlo.
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
   * Envia los datos encapsulados en el objeto Buffer a la dirección destino especificada
   * en la creación del canal.
   * @param buf El buffer que contiene los datos a enviar.
   * @param dirUnicastDestino dirección unicast destino
   * @exception PTMFExcepcion Excepción genérica
   * @exception ParametroInvalidoExcepcion Algún parámetro es inválido.
   * @exception IOException Error enviando los datos por el socket.
   */
  public synchronized void send(Buffer buf,Address dirUnicastDestino)
     throws PTMFExcepcion, ParametroInvalidoExcepcion, IOException
  {
    final String    mn = "CanalUnicastSeguro.send(Buffer,Address)";
    byte[] aCifrado = null;

    //
    // Verificar que el socket está abierto
    //
    if (this.socketObj == null)
    {
       throw new PTMFExcepcion(mn, "El socket no está abierto.");
    }

    //
    // Verificar la dirección destino
    //
    if (dirUnicastDestino == null)
    {
       throw new PTMFExcepcion(mn, "No se ha especificado una dirección unicast destino.");
    }

    //
    // verificar que el buffer y la dirección no son null
    //
    if ((buf == null) || (this.addressUnicast == null))
    {
     throw new ParametroInvalidoExcepcion(mn, "Argumentos requeridos erróneos.");
    }
    if ((buf.getBuffer() == null) || (buf.getLength() <= 0))
    {
          throw new ParametroInvalidoExcepcion(mn, "Buffer erróneo. Nulo o longitud errónea.");
    }


    //
    // Enviar los datos del buffer a la dirección destino.
    //
    try
    {
        //CODIFICAR¡¡¡¡
        aCifrado = this.cipher.doFinal(buf.getBuffer(),0,buf.getLength());

        //Crear el Datagrama
        DatagramPacket  sendPacket = new DatagramPacket(aCifrado,0,
                        aCifrado.length,dirUnicastDestino.getInetAddress(),
                            dirUnicastDestino.getPort());

    if (sendPacket == null)
     {
        throw new PTMFExcepcion(mn, "No se pudo crear el datagrama de emisión.");
      }

    //
    // ENVIAR!!!
    //
    //Log.log(mn,"ENVIADO UNO UNICAST A: " + dirUnicastDestino.getPort());

    this.socketObj.send(sendPacket, this.TTL);
    }
    catch(javax.crypto.IllegalBlockSizeException e)
    {
       throw new PTMFExcepcion(e.toString());
    }
    catch(javax.crypto.BadPaddingException e)
    {
       throw new PTMFExcepcion(e.toString());
    }

  }



  //==========================================================================
  /**
   * Recibir datos. <br><b>Este método es síncrono (bloqueante).</b><br>
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
    final String    mn = "CanalUnicastSeguro.receive(Buffer,Address)";
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
    return(PTMF.TPDU_MAX_SIZE);
  }


  //==========================================================================
  /**
   * Devuelve la dirección Unicast del canal
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
    final String  mn = "CanalUnicastSeguro.setTTL(byte)";

    //
    // Establecer el ttl para los paquetes de salida si el socket está abierto.
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
   * Método toString()
   * @return cadena identificativa
   */
   public String toString()
   {
    return (this.addressUnicast.toString());
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
    final String  mn = "CanalUnicastSeguro.setCallback(CanalCallback,int)";

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
      try
      {
        this.socketObj.setSoTimeout(0);
      }
      catch (SocketException se)
      {
        throw new PTMFExcepcion(mn, "Excepción en el timeout del socket.");
      }

      this.unicastThread = new UnicastThreadSeguro(this, this.socketObj);
      this.unicastThread.start();
    }

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
    final String  mn = "CanalMulticast.disableCallback()";

    //
    // Verificar que el socket está abierto
    //

    if (this.socketObj == null)
    {
      Log.log(mn, "Socket no abierto.");
      return;
    }

    //
    // si un thread receptor está activo, establecer el socket a síncrono
    // (bloqueante) y cancelar el thread de recepción.
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
   * Este método establece el objeto calback y el argumento de la clase
   * MulticastThread
   */
  synchronized void getCallback(UnicastThreadSeguro uThread)
  {
    //
    // Establecer el objeto callback y el argumento para ese objeto
    //
    uThread.callbackObject=this.callbackObject;
    uThread.callbackArgument=this.callbackArgument;
  }
}


/**
 * Thread de recepción del Canal Unicast.<br> Thread demonio.<br>
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

class UnicastThreadSeguro extends Thread
{
 /** El canal unicast que creó este thread. */
  private CanalUnicastSeguro  canalObj = null;

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
   public UnicastThreadSeguro(CanalUnicastSeguro canal, MulticastSocket sock)
  {
    super("UnicastThreadSeguro");

    setDaemon(true);


    //
    // Almacenar el canal y el socket.
    //

    this.canalObj = canal;
    this.socketObj  = sock;
  }

  //==========================================================================
  /**
   * El método run del thread. Cada vez que se recibe datos, este método pregunta
   * a la clase CanalUnicastSeguro cual es el objeto callback registrado y su
   * argumento.
   */
  public void run()
  {
    final String    mn = "UnicastThreadSeguro.run()";
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
