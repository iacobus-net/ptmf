//============================================================================
//
//	Copyright (c) 1999-2015. All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	File: TPDUHSACK.java  1.0 9/9/99
//
//	Descripción: Clase TPDUHSACK.
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
 * Clase TPDU HSACK.<br>
 * Hereda de la clase TPDUDatos.<br>
 *
 * Para crear un objeto de esta clase se tienen que usar los métodos estáticos.
 * Una vez creado no puede ser modicado.<br>
 *
 * El formato completo del TPDU HSACK es: <br>
 * <br>
 *                      1 1 1 1 1 1 1 1 1 1 2 2 2 2 2 2 2 2 2 2 3 3<br>
 * +0-1-2-3-4-5-6-7-8-9-0-1-2-3-4-5-6-7-8-9-0-1-2-3-4-5-6-7-8-9-0-1<br>
 * +---------------------------------------------------------------+<br>
 * +      Puerto Mulitcast         |          Puerto Unicast       +<br>
 * +---------------------------------------------------------------+<br>
 * +                    ID_GRUPO_LOCAL(4 bytes primeros)           +<br>
 * +---------------------------------------------------------------+<br>
 * +ID_GRUPO_LOCAL(2 bytes últimos)|          Longitud             +<br>
 * +---------------------------------------------------------------+<br>
 * +           Cheksum             | V |0|1|1|0|1|    No Usado     +<br>
 * +---------------------------------------------------------------+<br>
 * +    Número de Ráfaga Fuente    |      Dirección IP Fuente      +<br>
 * +                               |      (16 bits superiores)     +<br>
 * +---------------------------------------------------------------+<br>
 * +    Dirección IP Fuente        |      Puerto Unicast Fuente    +<br>
 * +    (16 bits superiores)       |                               +<br>
 * +---------------------------------------------------------------+<br>
 * +                   Número de Secuencia Fuente                  +<br>
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

public class TPDUHSACK extends TPDUDatos
{
  // ATRIBUTOS

  /** Tamaño de la cabecera del TPDUHSACK*/
   static final int LONGHEADER = 7*4;

  /**
   * Dirección IP Fuente (32 bits)  : dirección IP del socket que originó los datos.
   */
   IPv4 DIR_IP_FUENTE = null;

  /**
   * Puerto Unicast Fuente (16 bits): puerto unicast fuente
   */
   int PUERTO_UNICAST_FUENTE = 0;

  /**
   * Número de secuencia (32 bits): número de secuencia del TPDU que estoy
   * asentiendo.
   */
   NumeroSecuencia NUMERO_SECUENCIA_FUENTE = null;

  /**
   * Número de Ráfaga (16 bits): Número de ráfaga al que pertenece el TPDU.
   */

  int NUMERO_RAFAGA_FUENTE = 0;


  /**
   * Se forma con el valor de otros campos.<br>
   * <ul>ID TPDU Fuente : (10 bytes)
   *                    <li><ul>ID Socket Fuente (6 byte)
   *                            <li>Dirección IP Fuente (4 byte)</li>
   *                            <li>Puerto Unicast Fuente (2 byte)</li></ul></li>
   *                    <li>Número secuencia Fuente (4 bytes)</li></ul>
   */
   private ID_TPDU ID_TPDU_FUENTE = null;

  //==========================================================================
  /**
   * Constructor utilizado para crear un TPDUHSACK.
   * @param socketPTMFImp Objeto SocketPTMFImp del que obtiene el valor de los
   * campos de la cabecera común.
   * @exception PTMFExcepcion
   * @exception ParametroInvalidoExcepcion lanzada si socketPTMFImp es null
   */
  private TPDUHSACK (SocketPTMFImp socketPTMFImp)
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
  private TPDUHSACK ()

      throws ParametroInvalidoExcepcion,PTMFExcepcion

  {

   super();

  }

 //============================================================================
 /**
  * Crea un TPDUHSACK con la información facilitada.
  * @param socketPTMFImp Objeto SocketPTMFImp del que obtiene el valor de los
  * campos de la cabecera común.
  * @param dirIPFuente dirección IP Fuente
  * @param nSec número secuencia
  * @param puertoUnicastFuente puerto unicast fuente
  * @param numero_rafaga número de ráfaga fuente
  * @return objeto TPDUHSACK creado.
  * @exception ParametroInvalidoExcepcion si alguno de los parámetros es erróneo.
  * @exception PTMFExcepcion si hay un error al crear el TPDUHSACK

  */
 static TPDUHSACK crearTPDUHSACK (SocketPTMFImp socketPTMFImp,
                                  IPv4 dirIPFuente,
                                  NumeroSecuencia nSec,
                                  int puertoUnicastFuente,
                                  int numero_rafaga)
   throws ParametroInvalidoExcepcion, PTMFExcepcion
 {

   // Crear el TPDUDatos vacio
   TPDUHSACK resultTPDU = new TPDUHSACK (socketPTMFImp);

   // Guardar los datos en la cabecera para cuando sean pedidos
   resultTPDU.DIR_IP_FUENTE           = dirIPFuente;
   resultTPDU.NUMERO_SECUENCIA_FUENTE = (NumeroSecuencia)nSec.clone();
   resultTPDU.PUERTO_UNICAST_FUENTE   = puertoUnicastFuente;
   resultTPDU.NUMERO_RAFAGA_FUENTE = numero_rafaga;


   return resultTPDU;
 }

  //==========================================================================
  /**
   * Construir el TPDU HSACK, devuelve un buffer con el contenido del TPDUHSACK,
   * según el formato especificado en el protocolo.
   * <b>Este buffer no debe de ser modificado.</B>
   * @return un buffer con el TPDUHSACK.
   * @exception PTMFExcepcion Se lanza si ocurre algún error en la construcción
   * del TPDU
   * @exception ParametroInvalidoExcepcion lanzada si ocurre algún error en la
   * construcción del TPDU
   */
 Buffer construirTPDUHSACK () throws PTMFExcepcion,ParametroInvalidoExcepcion
 {
  final String mn = "TPDU.construirTPDUDatos";
  int offset = 14;

   // Crear la cabecera común a todos los TPDU
   Buffer bufferResult =super.construirCabeceraComun (PTMF.SUBTIPO_TPDU_DATOS_HSACK,
                                                      TPDUHSACK.LONGHEADER);

   if (bufferResult == null)
    throw new PTMFExcepcion (mn + "Error en el parser");

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


   // 17º, 18º, 19º y 20º BYTE : Dirección IP Fuente
   bufferResult.addBytes (this.DIR_IP_FUENTE.ipv4,0,offset,4);
   offset+=4;

   // 21º y 22º BYTE : Puerto Unicast Fuente
   bufferResult.addShort (this.PUERTO_UNICAST_FUENTE,offset);
   offset+=2;

   // 23º y 24º BYTE : Número de ráfaga Fuente
   bufferResult.addShort (this.NUMERO_RAFAGA_FUENTE,offset);
   offset+=2;

   // 25º, 26º, 27º y 28º BYTE : Número de Secuencia
   bufferResult.addInt (this.NUMERO_SECUENCIA_FUENTE.tolong(),offset);
   offset+=4;

   return bufferResult;
}

  //==========================================================================
  /**
   * Parse un Buffer de datos recibidos y crea un TPDU HSACK que lo encapsule.
   * El buffer debe de contener un TPDU HSACK.
   * @param buf Un buffer que contiene el TPDU HSACK recibido.
   * @param ipv4Emisor dirección IP unicast del emisor del HSACK.
   * @return Un objeto TPDUHSACK
   * @exception PTMFExcepcion El buffer pasado no contiene una cabecera TPDU
   * correcta, el mensaje de la excepción especifica el tipo de error.
   * @exception ParametroInvalidoExcepcion Se lanza si el buffer pasado no
   * contiene un TPDUHSACK válido.
   */
 static  TPDUHSACK parserBuffer (Buffer buffer,IPv4 ipv4Emisor)
   throws PTMFExcepcion,ParametroInvalidoExcepcion
 {
  final String mn = "TPDUHSACK.parserBuffer (buffer,ipv4)";
  int aux;


  if (buffer==null)
     throw new ParametroInvalidoExcepcion (mn + "Buffer nulo");

  if (TPDUHSACK.LONGHEADER > buffer.getMaxLength())
     throw new ParametroInvalidoExcepcion (mn + "Buffer incorrecto");

  TPDUHSACK tpduHSACK = new TPDUHSACK ();

  // Analizar los datos comunes
  TPDUDatos.parseCabeceraComun (buffer,tpduHSACK,ipv4Emisor);

  // Comprobar si el tipo es correcto
  if (tpduHSACK.SUBTIPO != PTMF.SUBTIPO_TPDU_DATOS_HSACK)
         throw new PTMFExcepcion (mn+ "Subtipo de TPDU Datos no es HSACK");


  //
  // 16º BYTE : No usado
  //

  int offset = 16;
  //
  // 17º, 18º, 19º y 20º BYTE : Dirección IP Fuente
  //
  tpduHSACK.DIR_IP_FUENTE = new IPv4 (new Buffer (buffer.getBytes (offset,4)));
  offset +=4;

  //
  // 21º y 22º BYTE : Puerto Unicast Fuente
  //
  tpduHSACK.PUERTO_UNICAST_FUENTE = buffer.getShort (offset);
  offset +=2;

  //
  // 23º y 24º BYTE : Número de Ráfaga
  //
  tpduHSACK.NUMERO_RAFAGA_FUENTE = buffer.getShort(offset);
  offset += 2;

  //
  // 25º, 26º, 27º y 28º BYTE : Número de secuencia
  //
  tpduHSACK.NUMERO_SECUENCIA_FUENTE = new NumeroSecuencia (buffer.getInt (offset));
  offset += 4;


  return tpduHSACK;
 }




 //===========================================================================

 /**

  * Devuelve una cadena informativa del TPDU HSACK

  */

 public String toString()

 {

   return "===================================================="+
          "\nPuerto Multicast: " + this.getPuertoMulticast() +
          "\nPuerto Unicast: " + this.getPuertoUnicast () +
          "\nIDGL: " + this.ID_GRUPO_LOCAL +
          "\nLongitud: " + this.LONGITUD +
          "\nCHECKSUM: " + this.CHEKSUM +
          "\nVersion: " + this.VERSION +
          "\nTipo: " + this.TIPO +
          "\nIP fuente: " + this.DIR_IP_FUENTE +
          "\nPuerto Unicast Fuente: " + this.PUERTO_UNICAST_FUENTE +
          "\nNúmero Secuencia Fuente: " + this.NUMERO_SECUENCIA_FUENTE +
          "\nBúmero Ráfaga Fuente: "+ this.NUMERO_RAFAGA_FUENTE+
          "\nSubtipo: " + PTMF.SUBTIPO_TPDU_DATOS_HSACK+
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

  *  Devuelve el número de ráfaga fuente

  */

 int getNumeroRafagaFuente()

 {

  return this.NUMERO_RAFAGA_FUENTE;

 }


} // Fin de la clase.

