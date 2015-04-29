//============================================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: DatagramSocketPTMF.java  1.0 24/09/99
//
//	Descripción: Clase DatagramSocketPTMF.
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

/**
 * <STRONG><b>
 * Clase DatagramSocketPTMF.<br>
 * la clase DatagramSocket es un SocketPTMF NO FIABLE para el
 * "Protocolo de Transporte Multicast Fiable (PTMF)".</STRONG><br>
 * Esta clase proporciona un "Socket Multicast No Fiable".
 * Fácil de utilizar. </b>
 *
 *
 * Esta clase no es thread-safe.
 * @version  1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */

public class DatagramSocketPTMF {

  /** SocketPTMFImp, clase que implementa el API presentada en esta clase. */
  private SocketPTMFImp socketPTMFImp = null;

  //==========================================================================
  /**
   * Constructor genérico.
   * Abre el socket y añade el emisor a un grupo Multicast, cerrando primero
   * cualquier grupo activo. Usa la interfaz por defecto del sistema para enviar
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
  public DatagramSocketPTMF(Address grupoMulticast, byte ttl) throws ParametroInvalidoExcepcion, PTMFExcepcion
  {
    socketPTMFImp = new SocketPTMFImp(grupoMulticast,null,ttl,PTMF.PTMF_NO_FIABLE,null,null,null);
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
  public DatagramSocketPTMF(Address grupoMulticast, Address interfaz, byte ttl) throws ParametroInvalidoExcepcion, PTMFExcepcion
  {
    socketPTMFImp = new SocketPTMFImp(grupoMulticast,interfaz,ttl,PTMF.PTMF_NO_FIABLE,null,null,null);
  }

   //==========================================================================
  /**
   * Este constructor crea un socketPTMF en el modo indicado.
   * El modo del socket puede ser PTMF.PTMF_NO_FIABLE o PTMF.PTMF_NO_FIABLE_ORDENADO.
   * Se añade el emisor a un grupo Multicast,
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
   * @param modo Modo del SocketPTMF
   * @exception ParametroInvalidoExcepcion Se lanza si el parámetro modo
   *  no es correcto
   * @exception PTMFExcepcion Se lanza si ocurre un error.
    * @see PTMF
   */
  public DatagramSocketPTMF(Address grupoMulticast, Address interfaz, byte ttl,int modo ) throws ParametroInvalidoExcepcion, PTMFExcepcion
  {
    switch(modo)
    {
     case PTMF.PTMF_NO_FIABLE:
     case PTMF.PTMF_NO_FIABLE_ORDENADO:
      socketPTMFImp = new SocketPTMFImp(grupoMulticast,interfaz,ttl,modo,null,null,null);
      break;
     default:
         throw new ParametroInvalidoExcepcion("MODO de creación del SocketPTMF erroneo.");
    }
  }


    //==========================================================================
  /**
   * Este constructor crea un socketPTMF en el modo indicado.
   * El modo del socket puede ser PTMF.PTMF_NO_FIABLE o PTMF.PTMF_NO_FIABLE_ORDENADO.
   * Se añade el emisor a un grupo Multicast,
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
   * @param modo Modo del SocketPTMF
   * @param PTMFConexionListener listener
   * @param cipher Objeto javax.crypto.Cipher utilizado para codificar o null
   * si no se quiere codificar
   * @param unCipher Objeto javax.crypto.Cipher utilizado para descodificar o
   * null se no se quiere descodificar.
   * @exception ParametroInvalidoExcepcion Se lanza si el parámetro modo
   *  no es correcto
   * @exception PTMFExcepcion Se lanza si ocurre un error.
    * @see PTMF
   */
  public DatagramSocketPTMF(Address grupoMulticast, Address interfaz, byte ttl,
  int modo, PTMFConexionListener listener,
  javax.crypto.Cipher cipher, javax.crypto.Cipher unCipher ) throws ParametroInvalidoExcepcion, PTMFExcepcion
  {
    switch(modo)
    {
     case PTMF.PTMF_NO_FIABLE:
     case PTMF.PTMF_NO_FIABLE_ORDENADO:
      socketPTMFImp = new SocketPTMFImp(grupoMulticast,interfaz,ttl,modo,listener,cipher,unCipher);
      break;
     default:
         throw new ParametroInvalidoExcepcion("MODO de creación del SocketPTMF erroneo.");
    }
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
    socketPTMFImp.closePTMF(PTMF.CLOSE_INMEDIATO /* no estable */);
  }

  //==========================================================================
  /**
   * Vuelve a activar al recepcion de datos despues de haber llamado
   * al metodo desactivarRecepcion()
   * @exception PTMFExcepcion Se lanza si ocurre algún error
   */
  public void activarRecepcion() throws PTMFExcepcion
  {
   this.socketPTMFImp.activarRecepcion();
  }

  //==========================================================================
  /**
   * Descativa la recepcion de datos.
   * @exception PTMFExcepcion Se lanza si ocurre algún error
   */
  public void desactivarRecepcion() throws PTMFExcepcion
  {
   this.socketPTMFImp.desactivarRecepcion();
  }



  //==========================================================================
  /**
   * Devuelve el número de bytes disponibles para leer.
   * @return Un entero con el número de bytes disponibles para ser leídos.
   */
  public int available()
  {
    return this.socketPTMFImp.getColaRecepcion().getTamaño();
  }

  //==========================================================================
  /**
   * Envía los datos pasados por argumento al CanalMulticast.<br>
   * Si la cola de emisión está llena el método bloquea el hilo llamante
   * hasta que halla espacio en la cola de emisión.
   * LA COLA no bloqueará si se ha llamado a la función setSotimeOut() con
   * un valor mayor que cero.
   * @param datos Array de bytes a enviar
   * @exception IOException si se produce alguna excepción en el flujo de salida.
   */
  public boolean send(Buffer datos) throws IOException
  {
    return this.socketPTMFImp.getColaEmision().add(datos.getBuffer(),0,datos.getBuffer().length,false);
  }

  //==========================================================================
  /**
   * Envía los datos pasados por argumento al CanalMulticast.<br>
   * Si la cola de emisión está llena el método bloquea el hilo llamante
   * hasta que halla espacio en la cola de emisión.
   * LA COLA no bloqueará si se ha llamado a la función setSotimeOut() con
   * un valor mayor que cero.<BR>
   * <STRONG>Con este método se puede especificar el Fin de una Transmisión
   * MULTICAST, LA CONEXIÓN SIGUE ACTIVA, ES UN MÉTODO DE AYUDA AL NIVEL
   * DE APLICACIÓN PARA DETECTAR FIN DE TRANSMISIÓN. </STRONG>
   * @param datos Array de bytes a enviar
   * @param bFinTransmision Especifica un final de una transmisión
   * @exception IOException si se produce alguna excepción en el flujo de salida.
   */
  public boolean send(Buffer datos,boolean bFinTransmsion) throws IOException
  {
    return this.socketPTMFImp.getColaEmision().add(datos.getBuffer(),0,datos.getBuffer().length,bFinTransmsion);
  }

  //==========================================================================
  /**
   * Devuelve un objeto RegistroID_Socket_Buffer. El RegistroID_Socket_Buffer
   * contiene el identificador del socket que envió los datos y los propios datos.<br>
   * La llamada a este método es bloqueante, bloquea el hilo llamante hasta que
   * no halla ningún dato que leer.
   * @param id_socket El ID_Socket del socket que envía la información
   * @param datos Objeto Buffer con los datos recibidos del socket id_socket.
   * @return True si se han obtenidos datos, False en caso contrario
   * @exception IOException Se lanza si no se puede crear el flujo de salida.
   */
  public RegistroID_Socket_Buffer receive() throws IOException
  {
    return this.socketPTMFImp.getColaRecepcion().remove();
  }

  //==========================================================================
  /**
   * Un método para registrar un objeto que implementa la interfaz PTMFConexionListener.
   * La interfaz PTMFConexionListener se utiliza para notificar a las clases que se
   * registren de un evento PTMFEventConexion
   * @param obj El objeto Indohandler.
   * @return PTMFExcepcion Se lanza cuando ocurre un error al registrar el
   *  objeto callback
   */
  public void addPTMFConexionListener(PTMFConexionListener obj)
  {
    this.socketPTMFImp.addPTMFConexionListener(obj);
  }

  //==========================================================================
  /**
   * Elimina un objeto PTMFConexionListener
   */
  public void removePTMFConexionListener(PTMFConexionListener obj)
  {
    this.socketPTMFImp.removePTMFConexionListener(obj);
  }


  //==========================================================================
  /**
   * Un método para registrar un objeto que implementa la interfaz PTMFDatosRecibidosListener.
   * La interfaz PTMFDatosRecibidosListener se utiliza para notificar a las clases que se
   * registren de un evento PTMFEventIDGL
   * @param obj El objeto Indohandler.
   * @return PTMFExcepcion Se lanza cuando ocurre un error al registrar el
   *  objeto callback
   */
  public void addPTMFDatosRecibidosListener(PTMFDatosRecibidosListener obj)
  {
    this.socketPTMFImp.addPTMFDatosRecibidosListener(obj);
  }

  //==========================================================================
  /**
   * Elimina un objeto PTMFDatosRecibidosListener
   */
  public void removePTMFDatosRecibidosListener(PTMFDatosRecibidosListener obj)
  {
    this.socketPTMFImp.removePTMFDatosRecibidosListener(obj);
  }
  //==========================================================================



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
   * @param tamaño del buffer de emisión
   */
  public void setCapacidadColaEmision(int size)
  {
     socketPTMFImp.setCapacidadColaEmision(size);
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
   * @param tamaño del buffer de recepción
   */
  public void setCapacidadColaRecepcion(int size)
  {
     socketPTMFImp.setCapacidadColaRecepcion(size);
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
   * Método main para pruebas.
   */
  public  static void main(String[] string)
  {
   Log.log("","Iniciando DatagramSocket....");

   try
   {

     Log.setDepuracion(true);
     Log.setNivelDepuracion(Log.TODO);
     Address dirMcast = new Address ("224.100.100.100",2000);

     DatagramSocketPTMF s = new DatagramSocketPTMF(dirMcast,(byte)2);

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

