//============================================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	File: CanalUnicast.java  1.0 15/09/99
//
//
//	Description: Clase CanalUnicast.
//
// 	Authors: 
//		 Alejandro Garc�a-Dom�nguez (alejandro.garcia.dominguez@gmail.com)
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
 * Esta clsae representa un canal unicast UDP/IP.
 * Soporta el env�o/recepci�n de paquetes UDP tanto unicast como multicast.
 * Permite el uso de callbacks para la notificaci�n de la recpci�n de datos.
 * Esta clase es thread-safe.
 * @version  1.0
 * @author M. Alejandro Garc�a Dom�nguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */

public abstract class CanalUnicast
{


  //==========================================================================
  /**
   * M�todo F�brica para crear un Canal Unicast No Seguro o Seguro.<br>
   * La creaci�n de un Canal Unicast Seguro la determina el par�metro cipher
   *
   * @param bindAddr La direcci�n unicast.
   * @param mcastIF La direcci�n unicast de la interfaz a usar cuando se env�an
   *  datos multicast.
   * @param cipher Un objeto javax.crypto.Cipher ya iniciado para la realizaci�n de
   *  la codificaci�n o null si no se desea codificar.
   * @param unCipher Un objeto javax.crypto.Cipher ya iniciado para la realizaci�n de
   *  la descodificaci�n o null si no se desea descodificar..
   * @exception UnknownHostException,PTMFExcepcion Error creando el socket o uni�ndose (join) al
   *  grupo multicast o estableciendo la interfaz de salida de los datos
   *  multicast.
   */
    public static CanalUnicast getInstance(Address bindAddres, Address ifMcast,
    javax.crypto.Cipher cipher, javax.crypto.Cipher unCipher) throws PTMFExcepcion, IOException
   {
     if(cipher == null && unCipher == null)
      return new CanalUnicastNoSeguro(bindAddres,ifMcast);
     else
      return new CanalUnicastSeguro(bindAddres,ifMcast,cipher,unCipher);
   }



  //==========================================================================
  /**
   * Cerrar el socket y parar cualquier thread callback.
   */
  public abstract  void close();

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
  public abstract void send(Buffer buf,Address dirUnicastDestino)
     throws PTMFExcepcion, ParametroInvalidoExcepcion, IOException;

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
  public abstract void receive(Buffer buf, Address src)
    throws PTMFExcepcion, ParametroInvalidoExcepcion, IOException;

  //==========================================================================
  /**
   * Devuelve el tama�o pr�ctico m�ximo para el canal en bytes.
   * @return El tama�o pr�ctico m�ximo para el canal en bytes.
   */
  public abstract int getMaxPacketSize();


  //==========================================================================
  /**
   * Devuelve la direcci�n Unicast del canal
   * @return Address
   */
  abstract Address  getAddressUnicast();

  //==========================================================================
  /**
   * Establece el TTL para todos los paquetes de salida.
   * @param ttl El valor del ttl.
   */
  public abstract void setTTL(byte ttl);

  //==========================================================================
  /**
   * M�todo toString()
   * @return cadena identificativa
   */
   public abstract String toString();

  //==========================================================================
  /**
   * Activa el m�todo callback.  No es necesario llamar a DisableCallback()
   * antes de cada llamada a SetCallback().
   * @param obj El objeto callback.
   * @param arg El argumento callback.
   * @exception PTMFExcepcion
   * @exception ParametroInvalidoExcepcion
   */
  public abstract void setCallback(CanalCallback obj, int arg)
   throws PTMFExcepcion,ParametroInvalidoExcepcion;

  //==========================================================================
  /**
   * Desactivar cualquier callback
   */
  public abstract void disableCallback();


}


