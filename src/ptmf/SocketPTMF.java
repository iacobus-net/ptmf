//============================================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: SocketPTMF.java  1.0 24/09/99
//
//
//	Descripción: Clase SocketPTMF.
//
//  Historial: 
//	14/10/2014 Change Licence to LGPL
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.TreeMap;

/**
 * <STRONG><b>
 * Clase SocketPTMF.<br>
 * Socket "Protocolo de Transporte Multicast Fiable (PTMF)".</STRONG><br>
 * Esta clase proporciona un "Socket Multicast Fiable". </b>
 *
 * La implementación del socket la realiza una instancia de SocketPTMFImp.<br>
 *
 * Esta clase no es thread-safe.
 * @version  1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */

public class SocketPTMF {

  /**
   * SocketPTMFImp, clase que implementa el API presentada en esta clase.
   */
  private SocketPTMFImp socketPTMFImp = null;

  //==========================================================================
  /**
   * Constructor genérico.
   * Crea un Socket PTMF en el modo FIABLE y añade el emisor a un grupo Multicast,
   * cerrando primero cualquier grupo activo.
   * Usa la interfaz por defecto del sistema para enviar
   * y recibir los datos.<br>
   * Solo un grupo multicast puede estar activo en un determinado momento.
   * @param grupoMulticast Un objeto Address con la dirección del grupo
   *  multicast al que se unirá (join) este emisor y con el puerto PTMF al que
   *  se enviarán/recibirán los datos multicast.
   * @param ttl El valor del TTL usado para todos los paquetes multicast.
   * <ul>
   * <il>ttl 1   = Subred
   * <il>ttl 8   = Local
   * <il>ttl 32  = Regional
   * <il>ttl 48  = Nacional
   * <il>ttl 64  = Europea
   * <il>ttl 128 = Internacional
   * </ul>
   * @exception ParametroInvalidoExcepcion Se lanza si el parámetro modo
   *  no es correcto
   * @exception PTMFExcepcion Se lanza si ocurre un error.
    * @see PTMF
   */
  public SocketPTMF(Address grupoMulticast, byte ttl) throws ParametroInvalidoExcepcion, PTMFExcepcion
  {
    socketPTMFImp = new SocketPTMFImp(grupoMulticast,null,ttl,PTMF.PTMF_FIABLE,null,null,null);
  }

  //==========================================================================
  /**
   * Constructor genérico.
   * Crea un Socket PTMF sin especificar el modo del socket. El modo del Socket
   * solo puede ser PTMF_FIABLE ó PTMF_FIABLE_RETRASADO.
   * Se añade el emisor a un grupo Multicast,
   * cerrando primero cualquier grupo activo.
   * Usa la interfaz por defecto del sistema para enviar
   * y recibir los datos.<br>
   * Solo un grupo multicast puede estar activo en un determinado momento.
   * @param grupoMulticast Un objeto Address con la dirección del grupo
   *  multicast al que se unirá (join) este emisor y con el puerto PTMF al que
   *  se enviarán/recibirán los datos multicast.
   * @param ttl El valor del TTL usado para todos los paquetes multicast.
   * <ul>
   * <il>ttl 1   = Subred
   * <il>ttl 8   = Local
   * <il>ttl 32  = Regional
   * <il>ttl 48  = Nacional
   * <il>ttl 64  = Europea
   * <il>ttl 128 = Internacional
   * </ul>
   * @param Modo de creación del SocketPTMF
   * @param cipher Objeto javax.crypto.Cipher utilizado para codificar o null
   * si no se quiere codificar
   * @param unCipher Objeto javax.crypto.Cipher utilizado para descodificar o
   * null se no se quiere descodificar.
   * @exception ParametroInvalidoExcepcion Se lanza si el parámetro modo
   *  no es correcto
   * @exception PTMFExcepcion Se lanza si ocurre un error.
    * @see PTMF
   */
  public SocketPTMF(Address grupoMulticast,Address interfaz, byte ttl, int modo
  ,PTMFConexionListener listener,
   javax.crypto.Cipher cipher, javax.crypto.Cipher unCipher) throws ParametroInvalidoExcepcion, PTMFExcepcion
  {
    switch(modo)
    {
     case PTMF.PTMF_FIABLE:
     case PTMF.PTMF_FIABLE_RETRASADO:
       socketPTMFImp = new SocketPTMFImp(grupoMulticast,interfaz,ttl,modo,listener,cipher,unCipher);
       break;
     default:
      throw new ParametroInvalidoExcepcion("MODO de creación del SocketPTMF erroneo.");
    }
  }
    //==========================================================================
  /**
   * Constructor genérico.
   * Crea un Socket PTMF sin especificar el modo del socket. El modo del Socket
   * solo puede ser PTMF_FIABLE ó PTMF_FIABLE_RETRASADO.
   * Se añade el emisor a un grupo Multicast,
   * cerrando primero cualquier grupo activo.
   * Usa la interfaz por defecto del sistema para enviar
   * y recibir los datos.<br>
   * Solo un grupo multicast puede estar activo en un determinado momento.
   * @param grupoMulticast Un objeto Address con la dirección del grupo
   *  multicast al que se unirá (join) este emisor y con el puerto PTMF al que
   *  se enviarán/recibirán los datos multicast.
   * @param ttl El valor del TTL usado para todos los paquetes multicast.
   * <ul>
   * <il>ttl 1   = Subred
   * <il>ttl 8   = Local
   * <il>ttl 32  = Regional
   * <il>ttl 48  = Nacional
   * <il>ttl 64  = Europea
   * <il>ttl 128 = Internacional
   * </ul>
   * @param Modo de creación del SocketPTMF
   * @exception ParametroInvalidoExcepcion Se lanza si el parámetro modo
   *  no es correcto
   * @exception PTMFExcepcion Se lanza si ocurre un error.
    * @see PTMF
   */
  public SocketPTMF(Address grupoMulticast,Address interfaz, byte ttl, int modo) throws ParametroInvalidoExcepcion, PTMFExcepcion
  {
    switch(modo)
    {
     case PTMF.PTMF_FIABLE:
     case PTMF.PTMF_FIABLE_RETRASADO:
       socketPTMFImp = new SocketPTMFImp(grupoMulticast,interfaz,ttl,modo,null,null,null);
       break;
     default:
      throw new ParametroInvalidoExcepcion("MODO de creación del SocketPTMF erroneo.");
    }
  }

    //==========================================================================
  /**
   * Constructor genérico.
   * Crea un Socket PTMF sin especificar el modo del socket. El modo del Socket
   * solo puede ser PTMF_FIABLE ó PTMF_FIABLE_RETRASADO.
   * Se añade el emisor a un grupo Multicast,
   * cerrando primero cualquier grupo activo.
   * Usa la interfaz por defecto del sistema para enviar
   * y recibir los datos.<br>
   * Solo un grupo multicast puede estar activo en un determinado momento.
   * @param grupoMulticast Un objeto Address con la dirección del grupo
   *  multicast al que se unirá (join) este emisor y con el puerto PTMF al que
   *  se enviarán/recibirán los datos multicast.
   * @param ttl El valor del TTL usado para todos los paquetes multicast.
   * <ul>
   * <il>ttl 1   = Subred
   * <il>ttl 8   = Local
   * <il>ttl 32  = Regional
   * <il>ttl 48  = Nacional
   * <il>ttl 64  = Europea
   * <il>ttl 128 = Internacional
   * </ul>
   * @param Modo de creación del SocketPTMF
   * @param PTMFConexionListener listener
   * @exception ParametroInvalidoExcepcion Se lanza si el parámetro modo
   *  no es correcto
   * @exception PTMFExcepcion Se lanza si ocurre un error.
    * @see PTMF
   */
  public SocketPTMF(Address grupoMulticast,Address interfaz, byte ttl, int modo, PTMFConexionListener listener) throws ParametroInvalidoExcepcion, PTMFExcepcion
  {
    switch(modo)
    {
     case PTMF.PTMF_FIABLE:
     case PTMF.PTMF_FIABLE_RETRASADO:
       socketPTMFImp = new SocketPTMFImp(grupoMulticast,interfaz,ttl,modo,listener,null,null);
       break;
     default:
      throw new ParametroInvalidoExcepcion("MODO de creación del SocketPTMF erroneo.");
    }
  }

  //==========================================================================
  /**
   * Este constructor abre el socket y añade el emisor a un grupo Multicast,
   * cerrando primero cualquier grupo activo. Usa la interfaz que se especifica
   * para enviar y recibir los datos.<br>
   * Solo un grupo multicast puede estar activo en un determinado momento.
   * @param grupoMulticast Un objeto Address con la dirección del grupo
   *  multicast al que se unirá (join) este emisor y con el puerto PTMF al que
   *  se enviarán/recibirán los datos multicast.
   * @param interfaz Interfaz por la que se envian/reciben los datos.
   * @param ttl El valor del TTL usado para todos los paquetes multicast.<br>
   * <ul>
   * <il>ttl 1   = Subred
   * <il>ttl 8   = Local
   * <il>ttl 32  = Regional
   * <il>ttl 48  = Nacional
   * <il>ttl 64  = Europea
   * <il>ttl 128 = Internacional
   * </ul>
   * @exception ParametroInvalidoExcepcion Se lanza si el parámetro modo
   *  no es correcto
   * @exception PTMFExcepcion Se lanza si ocurre un error.
    * @see PTMF
   */
  public SocketPTMF(Address grupoMulticast, Address interfaz, byte ttl) throws ParametroInvalidoExcepcion, PTMFExcepcion
  {
    socketPTMFImp = new SocketPTMFImp(grupoMulticast,interfaz,ttl,PTMF.PTMF_FIABLE,null,null,null);
  }

  //==========================================================================
  /**
   * Cierra el socket. Quita al emisor (leave) del grupo multicast activo,
   * cualquier dato recibido del grupo multiast se perderá.<br>
   * Sólo un grupo multicast puede estar activo en cualquier momento.
   * @param bOrdenado Boolean que indica el modo de cierre. <br>
   * si el valor es true el socket realiza una desconexion ordenada (RECOMENDADO),
   * si es false realiza una desconexion abrupta.
   * @exception PTMFExcepcion Se lanza si ocurre algún error cerrando el socket.
   */
  public void close(boolean bOrdenada) throws PTMFExcepcion
  {
    socketPTMFImp.closePTMF(bOrdenada);
  }

  //==========================================================================
  /**
   * Una vez llamada esta función no se podrá enviar ningún byte más.
   */
  public void closeEmision() throws PTMFExcepcion
  {
    socketPTMFImp.closePTMFEmision();
  }

  //==========================================================================
  /**
   * Cierra el socket. Quita al emisor (leave) del grupo multicast activo,
   * cualquier dato recibido del grupo multiast se perderá.<br>
   * Sólo un grupo multicast puede estar activo en cualquier momento.
   * @exception PTMFExcepcion Se lanza si ocurre algún error cerrando el socket.
   */
  public void close() throws PTMFExcepcion
  {
    socketPTMFImp.closePTMF(true);
  }



  //==========================================================================
  /**
   * Los datos obtenidos por el socket son pasados al usuario
   * @exception PTMFExcepcion Se lanza si ocurre algún error cerrando el socket.
   */
  public void activarRecepcion() throws PTMFExcepcion
  {
    socketPTMFImp.activarRecepcion();
  }

  //==========================================================================
  /**
   * Los datos obtenidos por el socket no serán pasados al usuario
   * @exception PTMFExcepcion Se lanza si ocurre algún error cerrando el socket.
   */
  public void desactivarRecepcion() throws PTMFExcepcion
  {
    socketPTMFImp.desactivarRecepcion();
  }


  //==========================================================================
  /**
   * Devuelve un objeto MulticastInputStream, un flujo de entrada para este socket.
   * @return Un flujo de entrada MulticastInputStream para leer bytes desde este socket.
   * @exception IOException Se lanza si no se puede crear el flujo de entrada.
   */
  public MulticastInputStream getMulticastInputStream() throws IOException
  {
    return socketPTMFImp.getMulticastInputStream();
  }

  //==========================================================================
  /**
   * Devuelve un objeto MulticastOutputStream, un flujo de salida para este socket.
   * @return Un flujo de salida MulticastOutputStream para escribir bytes en este socket.
   * @exception IOException Se lanza si no se puede crear el flujo de salida.
   */
  public MulticastOutputStream getMulticastOutputStream() throws IOException
  {
    return socketPTMFImp.getMulticastOutputStream();
  }

  //==========================================================================
  /**
   * Un método para registrar un objeto que implementa la interfaz PTMFConexionListener.
   * La interfaz PTMFConexionListener se utiliza para notificar a las clases que se
   * registren de un evento PTMFEventConexion
   * @param obj El objeto PTMFConexionListener
   * @return PTMFExcepcion Se lanza cuando ocurre un error al registrar el
   *  objeto callback
   */
  public void addPTMFConexionListener(PTMFConexionListener obj)
  {
     this.socketPTMFImp.addPTMFConexionListener(obj);
  }


  //==========================================================================
  /**
   * Devuelve los id_socket de todos los emisores actuales.
   * @return un treeMap de ID_Sockets emisores
   */
  public TreeMap getID_SocketEmisores()
  {
   return this.socketPTMFImp.getID_SocketEmisores ();
  }

  //==========================================================================
  /**
   * Elimina un objeto PTMFConexionListener
   * @param un objeto PTMFConexionListener
   */
  public void removePTMFConexionListener(PTMFConexionListener obj)
  {
    this.socketPTMFImp.removePTMFConexionListener(obj);
  }

  //==========================================================================
  /**
   * Un método para registrar un objeto que implementa la interfaz PTMFErrorListener.
   * La interfaz PTMFErrorListener se utiliza para notificar a las clases que se
   * registren de un evento PTMFEventError
   * @param obj El objeto PTMFErrorListener
   */
  public void addPTMFErrorListener(PTMFErrorListener obj)
  {
    this.socketPTMFImp.addPTMFErrorListener(obj);
  }

  //==========================================================================
  /**
   * Elimina un objeto PTMFErrorListener
   * @param onj El objeto PTMFErrorListener
   */
  public void removePTMFErrorListener(PTMFErrorListener obj)
  {
    this.socketPTMFImp.removePTMFErrorListener(obj);
  }



  //==========================================================================
  /**
   * Un método para registrar un objeto que implementa la interfaz PTMFIDGLListener.
   * La interfaz PTMFIDGLListener se utiliza para notificar a las clases que se
   * registren de un evento PTMFEventIDGL
   * @param obj El objeto PTMFIDGLListener
   * @return PTMFExcepcion Se lanza cuando ocurre un error al registrar el
   *  objeto callback
   */
  public void addPTMFIDGLListener(PTMFIDGLListener obj)
  {
    this.socketPTMFImp.addPTMFIDGLListener(obj);
  }

  //==========================================================================
  /**
   * Elimina un objeto PTMFIDGLListener
   * @param obj El objeto PTMFIDGLListener
   */
  public void removePTMFIDGLListener(PTMFIDGLListener obj)
  {
    this.socketPTMFImp.removePTMFIDGLListener(obj);
  }


  //=========================================================================
  /**
   * Un método para registrar un objeto que implementa la interfaz PTMFID_SocketListener.
   * La interfaz PTMFID_SocketListener se utiliza para notificar a las clases que se
   * registren de un evento PTMFEventIDGL
   * @param obj El objeto PTMFID_SocketListener
   * @return PTMFExcepcion Se lanza cuando ocurre un error al registrar el
   *  objeto callback
   */
  public void addPTMFID_SocketListener(PTMFID_SocketListener obj)
  {
    this.socketPTMFImp.addPTMFID_SocketListener(obj);
  }

  //==========================================================================
  /**
   * Elimina un objeto PTMFID_SocketListener
   * @param El objeto PTMFID_SocketListener
   */
  public void removePTMFID_SocketListener(PTMFID_SocketListener obj)
  {
    this.socketPTMFImp.removePTMFID_SocketListener(obj);
  }

  //==========================================================================
  /**
   * Devuelve el tamaño del buffer de emisión.
   * @return un int con el tamaño del buffer de emisión
   */
  public int getCapacidadColaEmision()
  {
    return (socketPTMFImp.getCapacidadColaEmision());
  }

  //==========================================================================
  /**
   * Establece el tamaño del buffer de emisión
   * @param iSize tamaño del buffer de emisión
   */
  public void setCapacidadColaEmision(int isize)
  {
     socketPTMFImp.setCapacidadColaEmision(isize);
  }

  //==========================================================================
  /**
   * Devuelve el tamaño del buffer de recepción
   * @return un int con el tamaño del buffer de recepción
   */
  public int getCapacidadColaRecepcion()
  {
    return socketPTMFImp.getCapacidadColaRecepcion();
  }

  //==========================================================================
  /**
   * Establece el tamaño del buffer de recepción
   * @param isize tamaño del buffer de recepción
   */
  public void setCapacidadColaRecepcion(int isize)
  {
     socketPTMFImp.setCapacidadColaRecepcion(isize);
  }

  //==========================================================================
  /**
   * Establece el tiempo de espera máximo que el thread de usuario espera
   * en una llamada al método receive() sin que hallan llegado datos.
   * @param iTiempo Tiempo máximo de espera en mseg. 0 espera infinita.
   */
  public void setSoTimeOut(int iTiempo)
  {
     socketPTMFImp.setSoTimeOut(iTiempo);
  }

  //==========================================================================
  /**
   * Establece el tiempo de espera máximo que el thread de usuario espera
   * en una llamada al método receive() sin que hallan llegado datos.
   * @param iTiempo Tiempo máximo de espera en mseg. 0 espera infinita.
   */
  public int getSoTimeOut()
  {
     return socketPTMFImp.getSoTimeOut();
  }

  //==========================================================================
  /**
   * Devuelve un TreeMap con los ID_Sockets del Grupo Local
   * @return TreeMap con los ID_Sockets del Grupo Local.
   */
   public TreeMap getID_Sockets()
   {
      return this.socketPTMFImp.getID_Sockets();
   }

  //==========================================================================
  /**
   * Devuelve un TreeMap con los IDGLs a los que alcanzamos
   * @return TreeMap con los IDGLs a los que alcanzamos
   */
   public TreeMap getIDGLs()
   {
      return this.socketPTMFImp.getIDGLs();
   }


  //==========================================================================
  /**
   * Obtener el numero de IDGLs
   * @return int el numero de IDGLs
   */
   public int getNumeroIDGLs()
   {
        return this.socketPTMFImp.getNumeroIDGLs();
   }

  //==========================================================================
  /**
   * Obtener el numero de IDSockets
   * @return int el numero de ID_Sockets
   */
   public  int getNumeroID_Sockets()
   {
        return this.socketPTMFImp.getNumeroID_Sockets();
   }


  //=============================================================================
 /**
  * Actualiza el ratio de envío de datos de usuario, el cual especifica la
  * máxima velocidad de envío de nuevos datos de usuario al grupo multicast.
  * Comienza a considerar el ratio a partir de este momento.
  * @param bytes_x_seg bytes por segundo.
  * @return el valor al que se ha actualizado el ratio de datos de usuario en bytes
  * por segundo
  */
  public long setRatioUsuario (long bytes_x_seg)
  {
    if(this.socketPTMFImp!= null)
    return this.socketPTMFImp.setRatioUsuario(bytes_x_seg);
    return 0;
  }


 /**
  * Establece el nivel de depuración.
  * @param i El nivel de depuracion. Debe de tomar uno de los sigioentes valores hexadecimales:
  *  NO DEPURAR      = 0;
  *  TODO            = 0xFFFFFFFF;
  *  IDGL            = 0x00000001;
  *  TPDU            = 0x00000002;
  *  TPDU_CGL        = 0x00000004;
  *  TPDU_DATOS      = 0x00000008;
  *  TEMPORIZADOR    = 0x00000010;
  *  CGL             = 0x00000020;
  *  SOCKET          = 0x00000040;
  *  CANAL_MULTICAST = 0x00000080;
  *  ACK             = 0x00000100;
  *  TPDU_NORMAL     = 0x00000200;
  *  NACK            = 0x00000400;
  *  HACK            = 0x00000800;
  *  HSACK           = 0x00001000;
  *  MACK            = 0x00002000;
  *  CGLOCALES       = 0x00004000;
  *  ID_SOCKETS_EMISORES   = 0x00008000;
  *  TABLA_ASENTIMIENTOS = 0x00020000;
  *  VENTANA         = 0x00080000;
  *  DATOS_THREAD    = 0x00100000;
  *  TPDU_RTX        = 0x00200000;
  *  DATOS_USUARIO   = 0x00800000;
  *  HNACK           = 0x01000000;
  *  ID_SOCKET       = 0x02000000;
  */
  public static void setNivelDepuracion(int i)
  {
    if( i != 0)
    {
     Log.setDepuracion(true);
     Log.setNivelDepuracion(i);
    }
    else
    {
      Log.setDepuracion(false);
    }

  }
  //==========================================================================
  /**
   * Metodo main()
   */
  public  static void main(String[] string)
  {
   Log.log("","Iniciando Socket....");

   try
   {

     Log.setDepuracion(true);
     Log.setNivelDepuracion(Log.TODO);
     Address dirMcast = new Address ("224.100.100.100",2000);

     SocketPTMF s = new SocketPTMF(dirMcast,(byte)2);

     while(true);
   }
   catch(UnknownHostException e)
   {
    Log.log("",""+e);
   }
   catch(PTMFExcepcion e)
   {
    Log.log("",""+e);
   }
   catch(ParametroInvalidoExcepcion e)
   {
    Log.log("",""+e);
   }
  }

  }

