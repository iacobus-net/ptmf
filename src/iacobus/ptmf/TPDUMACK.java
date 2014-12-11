//============================================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: TPDUMACK.java  1.0 9/9/99
//
//
//	Descripci�n: Clase TPDUMACK.
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
import java.util.*;
import java.lang.*;


/**
 * Clase TPDU MACK.<br>
 * Hereda de la clase TPDUDatos.<br>
 *
 * Para crear un objeto de esta clase se tienen que usar los m�todos est�ticos.
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
 * +      (2 bytes �ltimos)        |                               +<br>
 * +---------------------------------------------------------------+<br>
 * +           Cheksum             | V |0|1|0|1|0|    No Usado     +<br>
 * +---------------------------------------------------------------+<br>
 * +     N�mero de R�faga Fuente   |      Direcci�n IP Fuente      +<br>
 * +                               |      (2 bytes primeros)       +<br>
 * +---------------------------------------------------------------+<br>
 * +      Direcci�n IP Fuente      |     Puerto Unicast Fuente     +<br>
 * +      (2 bytes �ltimos)        |                               +<br>
 * +---------------------------------------------------------------+<br>
 * +              N�mero de secuencia inicial r�faga               +<br>
 * +---------------------------------------------------------------+<br>
 * <br> 
 * <br>
 * Esta clase no es thread-safe.<br>
 * @see      Buffer
 * @version  1.0
 * @author M. Alejandro Garc�a Dom�nguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 * Antonio Berrocal Piris
 * <A HREF="mailto:AntonioBP.wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
 */

public class TPDUMACK extends TPDUDatos
{
  // ATRIBUTOS
  /** Tama�o de la cabecera del TPDUHNACK*/
   static final int LONGHEADER = 7 * 4;

  /**
   * Direcci�n IP Fuente (32 bits)  : direcci�n IP del socket que origin� los datos.
   */
   IPv4 DIR_IP_FUENTE = null;

  /**
   * Puerto Unicast Fuente (16 bits): puerto unicast fuente
   */
   int PUERTO_UNICAST_FUENTE = 0;

  /**
    * N�mero de R�faga Fuente (16 bits): n�mero de r�faga para la que queremos
    * ser CG Local.
    */
   int NUMERO_RAFAGA_FUENTE = 0;

  /**
   * N�mero de secuencia Fuente de inicio de la r�faga.
   */
   NumeroSecuencia N_SEC_INICIAL_RAFAGA = null;

  /**
   * Se forma con el valor de otros campos.<br>
   *    <ul>ID Socket Fuente (6 byte)
   *       <li>Direcci�n IP Fuente (4 byte)</li>
   *       <li>Puerto Unicast Fuente (2 byte)</li>
   *    </ul>
   */
   private ID_Socket ID_SOCKET_FUENTE = null;

  //==========================================================================
  /**
   * Constructor utilizado para crear un TPDUHACK.
   * @param socketPTMFImp Objeto SocketPTMFImp del que obtiene el valor de los
   * campos de la cabecera com�n.
   * @exception PTMFExcepcion
   * @exception ParametroInvalidoExcepcion lanzada si socketPTMFImp es null
   */
  private TPDUMACK (SocketPTMFImp socketPTMFImp)
    throws PTMFExcepcion,ParametroInvalidoExcepcion
  {
   super (socketPTMFImp);
  }




   * Constructor por defecto.
   * Este constructor es para crear TPDUS a partir del parser de un Buffer
   * @exception PTMFExcepcion
   * @exception ParametroInvalido Se lanza en el constructor de la clase TPDU.
   */
  private TPDUMACK ()





 //============================================================================
 /**
  * Crea un TPDUHACK con la informaci�n facilitada.
  * @param socketPTMFImp Objeto SocketPTMFImp del que obtiene el valor de los
  * campos de la cabecera com�n.
  * @param dirIPFuente direcci�n IP Fuente
  * @param nSec n�mero secuencia
  * @param puertoUnicastFuente puerto unicast fuente
  * @param numero_rafaga n�mero de r�faga fuente
  * @return objeto TPDUHACK creado.
  * @exception ParametroInvalidoExcepcion si alguno de los par�metros es err�neo.
  * @exception PTMFExcepcion si hay un error al crear el TPDUHACK

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
   * seg�n el formato especificado en el protocolo.
   * <b>Este buffer no debe de ser modificado.</B>
   * @return un buffer con el TPDUHACK.
   * @exception PTMFExcepcion Se lanza si ocurre alg�n error en la construcci�n
   * del TPDU
   * @exception ParametroInvalidoExcepcion lanzada si ocurre alg�n error en la
   * construcci�n del TPDU
   */
 Buffer construirTPDUMACK () throws PTMFExcepcion,ParametroInvalidoExcepcion
 {
   final String mn = "TPDU.construirTPDUDatos";
   int offset = 14;


   // Crear la cabecera com�n a todos los TPDU
   Buffer bufferResult = construirCabeceraComun (PTMF.SUBTIPO_TPDU_DATOS_MACK,
                                                 TPDUMACK.LONGHEADER);

   if (bufferResult == null)
    throw new PTMFExcepcion (mn + "Buffer nulo");

   // 15� BYTE : Subtipo: (3 bits )
   short anterior = bufferResult.getByte (offset);
   // anterior : XXXX XXXX
   //      and : 1111 1110 = 0xFE
   //           ----------
   //            XXXX XXX0
   anterior &= 0xFE;
   bufferResult.addByte((byte)anterior,offset);
   offset++;






   bufferResult.addShort (this.NUMERO_RAFAGA_FUENTE,offset);
   offset+=2;

   // 19�, 20�, 21� y 22� BYTE : Direcci�n IP fuente
   bufferResult.addBytes (this.DIR_IP_FUENTE.ipv4,0,offset,4);
   offset+=4;

   // 23� y 24� BYTE : Puerto unicast fuente
   bufferResult.addShort (this.PUERTO_UNICAST_FUENTE,offset);
   offset+=2;

   // 25�, 26�, 27� y 28� BYTE : N�mero de Sec. Inicial de la r�faga.
   bufferResult.addInt (this.N_SEC_INICIAL_RAFAGA.tolong(),offset);
   offset+=4;

   return bufferResult;

}

  //==========================================================================
  /**
   * Parse un Buffer de datos recibidos y crea un TPDU HACK que lo encapsule.
   * El buffer debe de contener un TPDU HACK.
   * @param buf Un buffer que contiene el TPDU HACK recibido.
   * @param ipv4Emisor direcci�n IP unicast del emisor.
   * @return Un objeto TPDUHACK
   * @exception PTMFExcepcion El buffer pasado no contiene una cabecera TPDU
   * correcta, el mensaje de la excepci�n especifica el tipo de error.
   * @exception ParametroInvalidoExcepcion Se lanza si el buffer pasado no
   * contiene un TPDUHACK v�lido.
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
  // 17� y 18� BYTE : N�mero de r�faga fuente
  //
  tpduMACK.NUMERO_RAFAGA_FUENTE = buffer.getShort (offset);
  offset+=2;

  //
  // 19�, 20�, 21� y 22� BYTE : Direcci�n IP fuente
  //
  tpduMACK.DIR_IP_FUENTE = new IPv4 (new Buffer (buffer.getBytes (offset,4)));
  offset+=4;

  //
  // 23� y 24� BYTE : Puerto Unicast Fuente
  //
  tpduMACK.PUERTO_UNICAST_FUENTE = buffer.getShort (offset);
  offset+=2;

  //
  // 25�, 26�, 27� y 28� BYTE : N�mero de Secuencia Inicial de la r�faga.
  //
  tpduMACK.N_SEC_INICIAL_RAFAGA = new NumeroSecuencia (buffer.getInt (offset));
  offset+=4;


  return tpduMACK;
 }



 //===========================================================================























   * Devuelve el {@link #ID_SOCKET_FUENTE id_socket fuente}.
   * @return id_socket fuente
   */
 ID_Socket getID_SocketFuente ()












   * Devuelve el n�mero de r�faga fuente.
   */
  int getNumeroRafagaFuente ()






   * Devuelve el n�mero de secuencia inicial de la r�faga.
   * @return n�mero de secuencia inicial de la r�faga
   */
  NumeroSecuencia getNumSecInicialRafaga ()

































