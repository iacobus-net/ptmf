//============================================================================
//
//	Copyright (c) 1999-2015. All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: TPDUMACK.java  1.0 9/9/99
//
//
//	Descripción: Clase TPDUMACK.
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
import java.util.*;
import java.lang.*;


/**
 * Clase TPDU MACK.<br>
 * Hereda de la clase TPDUDatos.<br>
 *
 * Para crear un objeto de esta clase se tienen que usar los métodos estáticos.
 * Una vez creado no puede ser modicado.<br>
 *
 * El formato completo del TPDU MACK es: <br>
 * <br>
 *                      1 1 1 1 1 1 1 1 1 1 2 2 2 2 2 2 2 2 2 2 3 3<br>
 * +0-1-2-3-4-5-6-7-8-9-0-1-2-3-4-5-6-7-8-9-0-1-2-3-4-5-6-7-8-9-0-1<br>
 * +---------------------------------------------------------------+<br>
 * +      Puerto Multicast         |          Puerto Unicast       +<br>
 * +---------------------------------------------------------------+<br>
 * +                        ID_GRUPO_LOCAL                         +<br>
 * +                      (4 bytes primeros)                       +<br>
 * +---------------------------------------------------------------+<br>
 * +        ID_GRUPO_LOCAL         |          Longitud             +<br>
 * +      (2 bytes últimos)        |                               +<br>
 * +---------------------------------------------------------------+<br>
 * +           Cheksum             | V |0|1|0|1|0|    No Usado     +<br>
 * +---------------------------------------------------------------+<br>
 * +     Número de Ráfaga Fuente   |      Dirección IP Fuente      +<br>
 * +                               |      (2 bytes primeros)       +<br>
 * +---------------------------------------------------------------+<br>
 * +      Dirección IP Fuente      |     Puerto Unicast Fuente     +<br>
 * +      (2 bytes últimos)        |                               +<br>
 * +---------------------------------------------------------------+<br>
 * +              Número de secuencia inicial ráfaga               +<br>
 * +---------------------------------------------------------------+<br>
 * <br> 
 * <br>
 * Esta clase no es thread-safe.<br>
 * @see      Buffer
 * @version  1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 * Antonio Berrocal Piris
 * <A HREF="mailto:AntonioBP.wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
 */

public class TPDUMACK extends TPDUDatos
{
  // ATRIBUTOS
  /** Tamaño de la cabecera del TPDUHNACK*/
   static final int LONGHEADER = 7 * 4;

  /**
   * Dirección IP Fuente (32 bits)  : dirección IP del socket que originó los datos.
   */
   IPv4 DIR_IP_FUENTE = null;

  /**
   * Puerto Unicast Fuente (16 bits): puerto unicast fuente
   */
   int PUERTO_UNICAST_FUENTE = 0;

  /**
    * Número de Ráfaga Fuente (16 bits): número de ráfaga para la que queremos
    * ser CG Local.
    */
   int NUMERO_RAFAGA_FUENTE = 0;

  /**
   * Número de secuencia Fuente de inicio de la ráfaga.
   */
   NumeroSecuencia N_SEC_INICIAL_RAFAGA = null;

  /**
   * Se forma con el valor de otros campos.<br>
   *    <ul>ID Socket Fuente (6 byte)
   *       <li>Dirección IP Fuente (4 byte)</li>
   *       <li>Puerto Unicast Fuente (2 byte)</li>
   *    </ul>
   */
   private ID_Socket ID_SOCKET_FUENTE = null;

  //==========================================================================
  /**
   * Constructor utilizado para crear un TPDUHACK.
   * @param socketPTMFImp Objeto SocketPTMFImp del que obtiene el valor de los
   * campos de la cabecera común.
   * @exception PTMFExcepcion
   * @exception ParametroInvalidoExcepcion lanzada si socketPTMFImp es null
   */
  private TPDUMACK (SocketPTMFImp socketPTMFImp)
    throws PTMFExcepcion,ParametroInvalidoExcepcion
  {
   super (socketPTMFImp);
  }


  //==========================================================================
  /**
   * Constructor por defecto.
   * Este constructor es para crear TPDUS a partir del parser de un Buffer
   * @exception PTMFExcepcion
   * @exception ParametroInvalido Se lanza en el constructor de la clase TPDU.
   */
  private TPDUMACK ()
      throws ParametroInvalidoExcepcion,PTMFExcepcion
  {
   super();
  }

 //============================================================================
 /**
  * Crea un TPDUHACK con la información facilitada.
  * @param socketPTMFImp Objeto SocketPTMFImp del que obtiene el valor de los
  * campos de la cabecera común.
  * @param dirIPFuente dirección IP Fuente
  * @param nSec número secuencia
  * @param puertoUnicastFuente puerto unicast fuente
  * @param numero_rafaga número de ráfaga fuente
  * @return objeto TPDUHACK creado.
  * @exception ParametroInvalidoExcepcion si alguno de los parámetros es erróneo.
  * @exception PTMFExcepcion si hay un error al crear el TPDUHACK
  */
 static TPDUMACK crearTPDUMACK (SocketPTMFImp socketPTMFImp,
                                IPv4 dirIPFuente,
                                int numeroRafagaFuente,
                                int puertoUnicastFuente,
                                NumeroSecuencia nSecInicialRafaga)
   throws ParametroInvalidoExcepcion, PTMFExcepcion
 {

   // Crear el TPDUDatos vacio
   TPDUMACK resultTPDU = new TPDUMACK (socketPTMFImp);

   // Guardar los datos en la cabecera para cuando sean pedidos
   resultTPDU.DIR_IP_FUENTE         = dirIPFuente;
   resultTPDU.NUMERO_RAFAGA_FUENTE  = numeroRafagaFuente;
   resultTPDU.PUERTO_UNICAST_FUENTE = puertoUnicastFuente;
   resultTPDU.N_SEC_INICIAL_RAFAGA  = nSecInicialRafaga;

   return resultTPDU;
 }


  //==========================================================================
  /**
   * Construir el TPDU HACK, devuelve un buffer con el contenido del TPDUHACK,
   * según el formato especificado en el protocolo.
   * <b>Este buffer no debe de ser modificado.</B>
   * @return un buffer con el TPDUHACK.
   * @exception PTMFExcepcion Se lanza si ocurre algún error en la construcción
   * del TPDU
   * @exception ParametroInvalidoExcepcion lanzada si ocurre algún error en la
   * construcción del TPDU
   */
 Buffer construirTPDUMACK () throws PTMFExcepcion,ParametroInvalidoExcepcion
 {
   final String mn = "TPDU.construirTPDUDatos";
   int offset = 14;


   // Crear la cabecera común a todos los TPDU
   Buffer bufferResult = construirCabeceraComun (PTMF.SUBTIPO_TPDU_DATOS_MACK,
                                                 TPDUMACK.LONGHEADER);

   if (bufferResult == null)
    throw new PTMFExcepcion (mn + "Buffer nulo");

   // 15º BYTE : Subtipo: (3 bits )
   short anterior = bufferResult.getByte (offset);
   // anterior : XXXX XXXX
   //      and : 1111 1110 = 0xFE
   //           ----------
   //            XXXX XXX0
   anterior &= 0xFE;
   bufferResult.addByte((byte)anterior,offset);
   offset++;

   // 16º BYTE : No usado
   bufferResult.addByte ((byte)0,offset);
   offset++;

   // 17º y 18º BYTE : Número ráfaga fuente
   bufferResult.addShort (this.NUMERO_RAFAGA_FUENTE,offset);
   offset+=2;

   // 19º, 20º, 21º y 22º BYTE : Dirección IP fuente
   bufferResult.addBytes (this.DIR_IP_FUENTE.ipv4,0,offset,4);
   offset+=4;

   // 23º y 24º BYTE : Puerto unicast fuente
   bufferResult.addShort (this.PUERTO_UNICAST_FUENTE,offset);
   offset+=2;

   // 25º, 26º, 27º y 28º BYTE : Número de Sec. Inicial de la ráfaga.
   bufferResult.addInt (this.N_SEC_INICIAL_RAFAGA.tolong(),offset);
   offset+=4;

   return bufferResult;

}

  //==========================================================================
  /**
   * Parse un Buffer de datos recibidos y crea un TPDU HACK que lo encapsule.
   * El buffer debe de contener un TPDU HACK.
   * @param buf Un buffer que contiene el TPDU HACK recibido.
   * @param ipv4Emisor dirección IP unicast del emisor.
   * @return Un objeto TPDUHACK
   * @exception PTMFExcepcion El buffer pasado no contiene una cabecera TPDU
   * correcta, el mensaje de la excepción especifica el tipo de error.
   * @exception ParametroInvalidoExcepcion Se lanza si el buffer pasado no
   * contiene un TPDUHACK válido.
   */
 static  TPDUMACK parserBuffer (Buffer buffer,IPv4 ipv4Emisor)
   throws PTMFExcepcion,ParametroInvalidoExcepcion
 {
  final String mn = "TPDUMACK.parserBuffer (buffer,ipv4)";

  int aux;
  int offset = 16;

  if (buffer==null)
     throw new ParametroInvalidoExcepcion (mn+ "Buffer nulo");

  if (TPDUMACK.LONGHEADER > buffer.getMaxLength())
     throw new ParametroInvalidoExcepcion (mn+ "Buffer incorrecto");


  // Crear el TPDUDatos.
  TPDUMACK tpduMACK = new TPDUMACK ();

  // Analizar los datos comunes
  TPDUDatos.parseCabeceraComun (buffer,tpduMACK,ipv4Emisor);

  // Comprobar si el tipo es correcto
  if (tpduMACK.SUBTIPO != PTMF.SUBTIPO_TPDU_DATOS_MACK)
      throw new PTMFExcepcion (mn + "Subtipo de TPDU Datos no es MACK");


  //
  // 17º y 18º BYTE : Número de ráfaga fuente
  //
  tpduMACK.NUMERO_RAFAGA_FUENTE = buffer.getShort (offset);
  offset+=2;

  //
  // 19º, 20º, 21º y 22º BYTE : Dirección IP fuente
  //
  tpduMACK.DIR_IP_FUENTE = new IPv4 (new Buffer (buffer.getBytes (offset,4)));
  offset+=4;

  //
  // 23º y 24º BYTE : Puerto Unicast Fuente
  //
  tpduMACK.PUERTO_UNICAST_FUENTE = buffer.getShort (offset);
  offset+=2;

  //
  // 25º, 26º, 27º y 28º BYTE : Número de Secuencia Inicial de la ráfaga.
  //
  tpduMACK.N_SEC_INICIAL_RAFAGA = new NumeroSecuencia (buffer.getInt (offset));
  offset+=4;


  return tpduMACK;
 }



 //===========================================================================
 /**
  * Devuelve una cadena informativa del TPDU Datos
  */

 public String toString()
 {
   return "Puerto Multicast: " + this.getPuertoMulticast () +
          "\nPuerto Unicast: " + this.getPuertoUnicast () +
          "\nIDGL: " + this.ID_GRUPO_LOCAL +
          "\nLongitud: " + this.LONGITUD +
          "\nCHECKSUM: " + this.CHEKSUM +
          "\nVersion: " + this.VERSION +
          "\nTipo: " + this.TIPO +
          "\nNúmero Ráfaga Fuente: " + this.NUMERO_RAFAGA_FUENTE +
          "\nIP fuente: " + this.DIR_IP_FUENTE +
          "\nPuerto Unicast Fuente: " + this.PUERTO_UNICAST_FUENTE +
          "\nNúmero de Sec. Inicial Ráfaga: " + this.N_SEC_INICIAL_RAFAGA +
          "\nSubtipo: " + PTMF.SUBTIPO_TPDU_DATOS_MACK
          ;
 }

  //==========================================================================
  /**
   * Devuelve el {@link #ID_SOCKET_FUENTE id_socket fuente}.
   * @return id_socket fuente
   */
 ID_Socket getID_SocketFuente ()
 {
  if (this.ID_SOCKET_FUENTE == null)
   {
    this.ID_SOCKET_FUENTE = new ID_Socket
                             (this.DIR_IP_FUENTE,this.PUERTO_UNICAST_FUENTE);
   }
  return this.ID_SOCKET_FUENTE;
 }


  //==========================================================================
  /**
   * Devuelve el número de ráfaga fuente.
   */
  int getNumeroRafagaFuente ()
  {
   return this.NUMERO_RAFAGA_FUENTE;
  }

  //==========================================================================
  /**
   * Devuelve el número de secuencia inicial de la ráfaga.
   * @return número de secuencia inicial de la ráfaga
   */
  NumeroSecuencia getNumSecInicialRafaga ()
  {
   return this.N_SEC_INICIAL_RAFAGA;
  }

 //===========================================================================
 /**
  * Indica si este TPDUMACK es equivalente al pasado por argumento. Dos TPDUMACK
  * son equivalentes si hacen referencia al mismo par [ID_SocketFuente,nRafaga]
  * @param tpduMACK
  * @return true si son equivalentes
  */
 boolean equivalente (TPDUMACK tpduMACK)
 {
  // Si es null, no son equivalentes.
  if (tpduMACK==null)
     return false;

  // Comprobar Dir IP Fuente
  if (!(this.DIR_IP_FUENTE.equals (tpduMACK.DIR_IP_FUENTE)))
     return false;

  // Comprobar puerto unicast fuente
  if (!(this.PUERTO_UNICAST_FUENTE==tpduMACK.PUERTO_UNICAST_FUENTE))
     return false;

  // Comprobar número de ráfaga.
  if (!(this.NUMERO_RAFAGA_FUENTE==tpduMACK.NUMERO_RAFAGA_FUENTE))
     return false;

  return true;
 }

} // Fin de la clase.

