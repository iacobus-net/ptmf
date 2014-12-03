//============================================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: TPDUHACK.java  1.0 9/9/99
//
//
//	Descripci�n: Clase TPDUHACK.
//
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

package net.iacobus.ptmf;

import java.util.*;
import java.lang.*;

/**
 * Clase TPDU HACK.<br>
 * Hereda de la clase TPDUDatos.<br>
 *
 * Para crear un objeto de esta clase se tienen que usar los m�todos est�ticos.
 * Una vez creado no puede ser modicado.<br>
 *
 * El formato completo del TPDU HACK es: <br>
 *
 *                      1 1 1 1 1 1 1 1 1 1 2 2 2 2 2 2 2 2 2 2 3 3<br>
 * +0-1-2-3-4-5-6-7-8-9-0-1-2-3-4-5-6-7-8-9-0-1-2-3-4-5-6-7-8-9-0-1<br>
 * +---------------------------------------------------------------+<br>
 * +      Puerto Mulitcast         |          Puerto Unicast       +<br>
 * +---------------------------------------------------------------+<br>
 * +                    ID_GRUPO_LOCAL(4 bytes primeros)           +<br>
 * +---------------------------------------------------------------+<br>
 * +ID_GRUPO_LOCAL(2 bytes �ltimos)|          Longitud             +<br>
 * +---------------------------------------------------------------+<br>
 * +           Cheksum             | V |0|1|1|0|0|    No Usado     +<br>
 * +---------------------------------------------------------------+<br>
 * +    N�mero de R�faga Fuente    |      Direcci�n IP Fuente      +<br>
 * +                               |      (16 bits superiores)     +<br>
 * +---------------------------------------------------------------+<br>
 * +    Direcci�n IP Fuente        |      Puerto Unicast Fuente    +<br>
 * +    (16 bits superiores)       |                               +<br>
 * +---------------------------------------------------------------+<br>
 * +                   N�mero de Secuencia Fuente                  +<br>
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
public class TPDUHACK extends TPDUDatos
{
  // ATRIBUTOS
  /** Tama�o de la cabecera del TPDUHACK*/
   static final int LONGHEADER = 7*4; // BYTES

  /**
   * Direcci�n IP Fuente (32 bits)  : direcci�n IP del socket que origin� los datos.
   */
   IPv4 DIR_IP_FUENTE = null;

  /**
   * Puerto Unicast Fuente (16 bits): puerto unicast fuente
   */
   int PUERTO_UNICAST_FUENTE = 0;

  /**
   * N�mero de secuencia (32 bits): n�mero de secuencia del TPDU que estoy
   * asentiendo.
   */
   NumeroSecuencia NUMERO_SECUENCIA_FUENTE = null;

  /**
   * N�mero de R�faga (16 bits): N�mero de r�faga al que pertenece el TPDU.
   */
  int NUMERO_RAFAGA_FUENTE = 0;

  /**
   * Se forma con el valor de otros campos.<br>
   * <ul>ID TPDU Fuente : (10 bytes)
   *                    <li><ul>ID Socket Fuente (6 byte)
   *                            <li>Direcci�n IP Fuente (4 byte)</li>
   *                            <li>Puerto Unicast Fuente (2 byte)</li></ul></li>
   *                    <li>N�mero secuencia Fuente (4 bytes)</li></ul>
   */
   private ID_TPDU ID_TPDU_FUENTE = null;


  //==========================================================================
  /**
   * Constructor utilizado para crear un TPDUHACK.
   * @param socketPTMFImp Objeto SocketPTMFImp del que obtiene el valor de los
   * campos de la cabecera com�n.
   * @exception PTMFExcepcion
   * @exception ParametroInvalidoExcepcion lanzada si socketPTMFImp es null
   */
  private TPDUHACK (SocketPTMFImp socketPTMFImp)
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
  private TPDUHACK () throws ParametroInvalidoExcepcion,PTMFExcepcion
  {
   super();
  }

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

  */
 static TPDUHACK crearTPDUHACK (SocketPTMFImp socketPTMFImp,
                                IPv4 dirIPFuente,
                                NumeroSecuencia nSec,
                                int puertoUnicastFuente,
                                int numero_rafaga)
   throws ParametroInvalidoExcepcion, PTMFExcepcion
 {

   // Crear el TPDUDatos vacio
   TPDUHACK resultTPDU = new TPDUHACK (socketPTMFImp);

   // Guardar los datos en la cabecera para cuando sean pedidos
   resultTPDU.DIR_IP_FUENTE           = dirIPFuente;
   resultTPDU.NUMERO_SECUENCIA_FUENTE = (NumeroSecuencia)nSec.clone();
   resultTPDU.PUERTO_UNICAST_FUENTE   = puertoUnicastFuente;
   resultTPDU.NUMERO_RAFAGA_FUENTE = numero_rafaga;

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
 Buffer construirTPDUHACK () throws PTMFExcepcion,ParametroInvalidoExcepcion
 {
   final String mn = "TPDU.construirTPDUDatos";
   int offset = 14;
   // Crear la cabecera com�n a todos los TPDU
   Buffer bufferResult = this.construirCabeceraComun (PTMF.SUBTIPO_TPDU_DATOS_HACK,
                                                      TPDUHACK.LONGHEADER);

   if (bufferResult == null)
      throw new ParametroInvalidoExcepcion (mn + "Buffer Nulo");

   // 15� BYTE :
   short anterior = bufferResult.getByte (offset);
   // anterior : XXXX XXXX
   //      and : 1111 1110 = 0xFE
   //           ----------
   //            XXXX XXX0
   anterior &= 0xFE;
   bufferResult.addByte((byte)anterior,offset);
   //Log.debug (Log.TPDU,mn,"SUBTIPO:  HACK :  IR : ");

   offset++;


   // 16� BYTE : No usado

   bufferResult.addByte ((byte)0,offset);

   offset++;


   // 17�, 18�, 19� y 20� BYTE : Direcci�n IP Fuente
   bufferResult.addBytes (this.DIR_IP_FUENTE.ipv4,0,offset,4);
   offset+=4;

   // 21� y 22� BYTE : Puerto Unicast Fuente
   bufferResult.addShort (this.PUERTO_UNICAST_FUENTE,offset);
   offset+=2;

   // 23�, 24� BYTE : N�mero de r�faga
   bufferResult.addShort (this.NUMERO_RAFAGA_FUENTE,offset);
   offset+=2;

   // 25�, 26�, 27� y 28� BYTE : N�mero de Secuencia
   bufferResult.addInt (this.NUMERO_SECUENCIA_FUENTE.tolong(),offset);
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
 static  TPDUHACK parserBuffer (Buffer buffer,IPv4 ipv4Emisor)
   throws PTMFExcepcion,ParametroInvalidoExcepcion
 {
  final String mn = "TPDUHACK.parserBuffer (buffer,ipv4)";

  int aux;
  int offset = 16;
  // Crear el TPDUDatos.
  if (buffer==null)
     throw new ParametroInvalidoExcepcion (mn + "Buffer nulo");

  if (TPDUHACK.LONGHEADER > buffer.getMaxLength())
    throw new ParametroInvalidoExcepcion (mn + "Buffer incorrecto");

  TPDUHACK tpduHACK = new TPDUHACK ();


  // Analizar los datos comunes
  TPDUDatos.parseCabeceraComun (buffer,tpduHACK,ipv4Emisor);

  // Comprobar si el tipo es correcto
  if (tpduHACK.SUBTIPO != PTMF.SUBTIPO_TPDU_DATOS_HACK)
         throw new PTMFExcepcion (mn+"Subtipo del TPDU Datos no es HACK");

  // 15� BYTE : Version, etc.

  //
  // 16� BYTE : No usado
  //

  //
  // 17�, 18�, 19� y 20� BYTE : Direcci�n IP fuente
  //
  tpduHACK.DIR_IP_FUENTE = new IPv4 (new Buffer (buffer.getBytes (offset,4)));
  offset+=4;

  //
  // 21� y 22� BYTE : Puerto Unicast Fuente
  //
  tpduHACK.PUERTO_UNICAST_FUENTE = buffer.getShort (offset);
  offset+=2;

  //
  // 23� y 24� BYTE : N�mero de R�faga
  //
  tpduHACK.NUMERO_RAFAGA_FUENTE = buffer.getShort(offset);
  offset+=2;

  //
  // 25�, 26�, 27� y 28� BYTE : N�mero de secuencia
  //
  tpduHACK.NUMERO_SECUENCIA_FUENTE = new NumeroSecuencia (buffer.getInt (offset));
  offset+=4;

  return tpduHACK;
 }

 //===========================================================================
 /**
  * Devuelve una cadena informativa del TPDU HACK
  */
 public String toString()
 {
   return "===================================================="+
          "\nPuerto Multicast: " + this.getPuertoMulticast() +
          "\nPuerto Unicast: " + this.getPuertoUnicast() +
          "\nIDGL: " + this.ID_GRUPO_LOCAL +
          "\nLongitud: " + this.LONGITUD +
          "\nCHECKSUM: " + this.CHEKSUM +
          "\nVersion: " + this.VERSION +
          "\nTipo: " + this.TIPO +
          "\nIP fuente: " + this.DIR_IP_FUENTE +
          "\nPuerto Unicast Fuente: " + this.PUERTO_UNICAST_FUENTE +
          "\nN�mero Secuencia Fuente: " + this.NUMERO_SECUENCIA_FUENTE +
          "\nB�mero R�faga Fuente: "+ this.NUMERO_RAFAGA_FUENTE+
          "\nSubtipo: " + PTMF.SUBTIPO_TPDU_DATOS_HACK+
          "\n====================================================";

 }

//==========================================================================
/**

 * Devuelve el {@link #ID_TPDU_FUENTE ID_TDPU Fuente}.

 */

 ID_TPDU getID_TPDUFuente ()
 {
   if (ID_TPDU_FUENTE == null)
   {

    try {
      ID_Socket id_SocketFuente = new ID_Socket
                              (this.DIR_IP_FUENTE,this.PUERTO_UNICAST_FUENTE);
      this.ID_TPDU_FUENTE = new ID_TPDU (id_SocketFuente,this.NUMERO_SECUENCIA_FUENTE);
     } catch (ParametroInvalidoExcepcion e) {}
    }
  return this.ID_TPDU_FUENTE;

 }

 //===========================================================================
 /**
  *  Devuelve el n�mero de r�faga fuente
  */
 int getNumeroRafagaFuente()
 {
  return this.NUMERO_RAFAGA_FUENTE;
 }

} // Fin de la clase.

