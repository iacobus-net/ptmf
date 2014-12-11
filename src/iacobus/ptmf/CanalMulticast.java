//============================================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	File: CanalMulticast.java  1.0 13/09/99
//
//	Description: Class CanalMulticast.
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
//----------------------------------------------------------------------------


package iacobus.ptmf;

import java.io.*;
import java.util.*;
import java.net.*;


/**
 * Clase que representa un <STRONG><b>Canal Multicast Gen�rico</b></STRONG>.<br>
 * <b>Soporta la emisi�n/recepci�n de paquetes unicast o multicast.</b>
 * Posibilidad de utilizar una funci�n callback usando un thread para
 * notificar la recepci�n de paquetes de forma as�ncrona.
 * @version  1.0
 * @author M. Alejandro Garc�a Dom�nguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */

public abstract class CanalMulticast 
{

  //==========================================================================
  /**
   * M�todo F�brica para crear un Canal Multicast No Seguro o Seguro.<br>
   * La creaci�n de un Canal Multicast Seguro la determina el par�metro cipher
   *
   * ADVERTENCIA SOBRE LOS OBJETOS javax.crypto.Cipher:<br>
   *  Los datagramas son enviados codificados, debido a la naturaleza de los datagramas
   *  (pueden perderse,llegar desordenados, duplicados, etc.) <strong> Los objetos
   *  Codificador (cipher) y  Descodificador(uncipher) deben de ser iniciados
   *  con un algoritmo en MODO ECB, por ejemplo "RC2/ECB/PKCS5Padding".</STRONG>
   *
   *  De esta manera se puede descodificar cada datagrama de forma independiente,
   * en otros modos de codificaci�n como CBC o PCBC los datos codificados
   * dependen de todos los datos anteriores ya codificados, en estos modos se
   * realizan operaciones XOR con los datos anteriores, una p�rdida de un
   * datagrama o la llegada desordenada o duplicada implicar�a que la
   * decodificaci�n no se realice bien, debido a que los datagramas deber�an
   * de llegar en el mismo orden en el que se codificaron.<br>
   * <STRONG> Si el usuario desea aumentar la seguridad deber� codificar los
   * datos que transmita a trav�s de PTMF.</STRONG>.
   *
   * @param grupoMcast La direcci�n de grupo multicast a la que unirse (join).
   *  El n�mero de puerto es actualizado al puerto actualmente usado.
   * @param ifMcast La direcci�n de la interfaz de red utilizada para enviar
   *  los paquetes multicast.
   * @param cipher Un objeto javax.crypto.Cipher ya iniciado para la realizaci�n de
   *  la codificaci�n o null si no se desea codificar.
   * @param unCipher Un objeto javax.crypto.Cipher ya iniciado para la realizaci�n de
   *  la descodificaci�n o null si no se desea descodificar..
   * @exception PTMFExcepcion Error creando el socket o uni�ndose (join) al
   *  grupo multicast o estableciendo la interfaz de salida de los datos
   *  multicast.
   */
    public static CanalMulticast getInstance(Address grupoMcast,  Address ifMcast,
    javax.crypto.Cipher cipher, javax.crypto.Cipher unCipher) throws PTMFExcepcion
   {
     if(cipher == null && unCipher == null)
       return new CanalMulticastNoSeguro(grupoMcast,ifMcast);
     else
      return new CanalMulticastSeguro(grupoMcast,ifMcast,cipher,unCipher);
   }

  //==========================================================================
  /**
   * Cierra el socket y para cualquier thread callback.
   */
  public abstract void close();

  //==========================================================================
  /**
   * Envia los datos encapsulados en el objeto Buffer a la direcci�n destino especificada
   * por el objeto Address.
   * @param buf El buffer que contiene los datos a enviar.
   * @exception PTMFExcepcion Excepci�n gen�rica
   * @exception ParametroInvalidoExcepcion Par�metro incorrecto.
   * @exception IOException Error enviando los datos
   */
  public  abstract void send(Buffer buf)
   throws PTMFExcepcion, ParametroInvalidoExcepcion, IOException;

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
  public abstract  void send(Buffer buf,  byte TTL)
   throws PTMFExcepcion, ParametroInvalidoExcepcion, IOException;

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
   * Establece el TTL para todos los paquetes de salida.
   * @param ttl El valor del ttl.
   */
  public abstract  void setTTL(byte ttl);

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
  public abstract  void setCallback(CanalCallback obj, int arg)
   throws PTMFExcepcion,ParametroInvalidoExcepcion;

  //==========================================================================
  /**
   * Desactivar cualquier callback
   */
  public abstract  void disableCallback();


  //==========================================================================
  /**
   * Devuelve la direcci�n Multicast del canal
   * @return Address
   */
  abstract Address getAddressMulticast();


}



