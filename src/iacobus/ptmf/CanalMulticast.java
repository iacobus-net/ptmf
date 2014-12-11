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
 * Clase que representa un <STRONG><b>Canal Multicast Genérico</b></STRONG>.<br>
 * <b>Soporta la emisión/recepción de paquetes unicast o multicast.</b>
 * Posibilidad de utilizar una función callback usando un thread para
 * notificar la recepción de paquetes de forma asíncrona.
 * @version  1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */

public abstract class CanalMulticast 
{

  //==========================================================================
  /**
   * Método Fábrica para crear un Canal Multicast No Seguro o Seguro.<br>
   * La creación de un Canal Multicast Seguro la determina el parámetro cipher
   *
   * ADVERTENCIA SOBRE LOS OBJETOS javax.crypto.Cipher:<br>
   *  Los datagramas son enviados codificados, debido a la naturaleza de los datagramas
   *  (pueden perderse,llegar desordenados, duplicados, etc.) <strong> Los objetos
   *  Codificador (cipher) y  Descodificador(uncipher) deben de ser iniciados
   *  con un algoritmo en MODO ECB, por ejemplo "RC2/ECB/PKCS5Padding".</STRONG>
   *
   *  De esta manera se puede descodificar cada datagrama de forma independiente,
   * en otros modos de codificación como CBC o PCBC los datos codificados
   * dependen de todos los datos anteriores ya codificados, en estos modos se
   * realizan operaciones XOR con los datos anteriores, una pérdida de un
   * datagrama o la llegada desordenada o duplicada implicaría que la
   * decodificación no se realice bien, debido a que los datagramas deberían
   * de llegar en el mismo orden en el que se codificaron.<br>
   * <STRONG> Si el usuario desea aumentar la seguridad deberá codificar los
   * datos que transmita a través de PTMF.</STRONG>.
   *
   * @param grupoMcast La dirección de grupo multicast a la que unirse (join).
   *  El número de puerto es actualizado al puerto actualmente usado.
   * @param ifMcast La dirección de la interfaz de red utilizada para enviar
   *  los paquetes multicast.
   * @param cipher Un objeto javax.crypto.Cipher ya iniciado para la realización de
   *  la codificación o null si no se desea codificar.
   * @param unCipher Un objeto javax.crypto.Cipher ya iniciado para la realización de
   *  la descodificación o null si no se desea descodificar..
   * @exception PTMFExcepcion Error creando el socket o uniéndose (join) al
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
   * Envia los datos encapsulados en el objeto Buffer a la dirección destino especificada
   * por el objeto Address.
   * @param buf El buffer que contiene los datos a enviar.
   * @exception PTMFExcepcion Excepción genérica
   * @exception ParametroInvalidoExcepcion Parámetro incorrecto.
   * @exception IOException Error enviando los datos
   */
  public  abstract void send(Buffer buf)
   throws PTMFExcepcion, ParametroInvalidoExcepcion, IOException;

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
  public abstract  void send(Buffer buf,  byte TTL)
   throws PTMFExcepcion, ParametroInvalidoExcepcion, IOException;

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
  public abstract void receive(Buffer buf, Address src)
   throws PTMFExcepcion, ParametroInvalidoExcepcion, IOException;

  //==========================================================================
  /**
   * Devuelve el tamaño práctico máximo para el canal en bytes.
   * @return El tamaño práctico máximo para el canal en bytes.
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
   * Método toString()
   * @return cadena identificativa
   */
   public abstract String toString();

  //==========================================================================
  /**
   * Activa el método callback.  No es necesario llamar a DisableCallback()
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
   * Devuelve la dirección Multicast del canal
   * @return Address
   */
  abstract Address getAddressMulticast();


}



