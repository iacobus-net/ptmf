//============================================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: TPDUDatos.java  1.0 9/9/99
//
//
//	Descripción: Clase TPDUDatos.
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
//
//----------------------------------------------------------------------------

package ptmf;

import java.util.*;
import java.lang.*;


/**
 * Clase TPDU. Los siguiente campos son comunes a todos los TPDU Datos.<br>
 *
 * El formato completo del TPDU Datos es:<br>
 *
 *                      1 1 1 1 1 1 1 1 1 1 2 2 2 2 2 2 2 2 2 2 3 3<br>
 * +0-1-2-3-4-5-6-7-8-9-0-1-2-3-4-5-6-7-8-9-0-1-2-3-4-5-6-7-8-9-0-1<br>
 * +---------------------------------------------------------------+<br>
 * +      Puerto Mulitcast         |          Puerto Unicast       +<br>
 * +---------------------------------------------------------------+<br>
 * +                    ID_GRUPO_LOCAL(4 bytes primeros)           +<br>
 * +---------------------------------------------------------------+<br>
 * +ID_GRUPO_LOCAL(2 bytes últimos)|          Longitud             +<br>
 * +---------------------------------------------------------------+<br>
 * +           Cheksum             | V |0|1|sub  |<br>
 * +                               |   | | |tipo |<br>
 * +---------------------------------------------+<br>
 * <br>
 * <br>
 * Esta clase no es thread-safe.<br>
 * Subclases:
 * <ul>
 *  <li>{@link TPDUDatosNormal}</li>
 *  <li>{@link TPDUDatosRtx}</li>
 *  <li>{@link TPDUMACK}</li>
 *  <li>{@link TPDUACK}</li>
 *  <li>{@link TPDUHACK}</li>
 *  <li>{@link TPDUHSACK}</li>
 *  <li>{@link TPDUNACK}</li>
 *  <li>{@link TPDUHNACK}</li>
 * </ul>
 * @version  1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 * Antonio Berrocal Piris
 * <A HREF="mailto:AntonioBP.wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
 */
public class TPDUDatos extends TPDU
{
  // ATRIBUTOS
  /** Longitud de los datos comunes a todos los TPDUDatos  */
  static final int LONGHEADER = 3 * 4 + 3; // 15 bytes

  /**
   * Subtipo (3 bits): Subtipo del TPDU Control de Grupo Local:
   * <table border=1>
   * <tr><td> Bits </td><td> Mensaje:</td></tr>
   * <tr><td> 000 </td><td> {@link TPDUDatosNormal}</td></tr>
   * <tr><td> 001 </td><td> {@link TPDUDatosRtx}</td></tr>
   * <tr><td> 010 </td><td> {@link TPDUMACK}</td></tr>
   * <tr><td> 011 </td><td> {@link TPDUACK}</td></tr>
   * <tr><td> 100 </td><td> {@link TPDUHACK}</td></tr>
   * <tr><td> 101 </td><td> {@link TPDUHSACK}</td></tr>
   * <tr><td> 110 </td><td> {@link TPDUNACK}</td></tr>
   * <tr><td> 111 </td><td> {@link TPDUHNACK}</td></tr>
   * </table>
   */
  byte SUBTIPO = 0;


  /**
   * Almacena la información del socket PTMF que ha enviado este TPDU.
   * Se reserva a las subclases el asignar valor a este campo.
   */
  protected ID_Socket id_SocketEmisor = null;


  /**
   * Indica si el tpdu datos ha sido recibido por unicast o por multicast.
   * Sólo es usado para los paquetes que son recibidos.
   */
  boolean recibidoPorUnicast = false;


  //==========================================================================
  /**
   * Constructor utilizado para crear un TPDUDatos, obtiene la información de los
   * campos a partir del objeto socketPTMFImp.
   * @param socketPTMFImp Objeto SocketPTMFImp del que obtiene el valor de los
   * campos de la cabecera común.
   * @exception PTMFExcepcion
   * @exception ParametroInvalidoExcepcion lanzada si socketPTMFImp es null
   */
  protected TPDUDatos (SocketPTMFImp socketPTMFImp)
    throws PTMFExcepcion,ParametroInvalidoExcepcion
  {
   super (socketPTMFImp);
   // Asignar valor a this.id_SocketEmisor
   this.id_SocketEmisor = socketPTMFImp.getAddressLocal().toID_Socket ();
  }


  //==========================================================================

  /**
   * Constructor utilizado para crear un TPDU a partir de la información
   * facilitada en los argumentos.
   * @param puertoMulticast
   * @param puertoUnicast
   * @param idgl
   * @param dirIp dirección IP unicast del emisor del TPDUDatos
   * @exception PTMFExcepcion
   * @exception ParametroInvalidoExcepcion lanzada si algún parámetro tienen un
   * valor no válido.
   */

  protected TPDUDatos (int puertoMulticast,
                       int puertoUnicast,
                       IDGL idgl,
                       IPv4 dirIp)
    throws PTMFExcepcion,ParametroInvalidoExcepcion
  {
   super (puertoMulticast,puertoUnicast,idgl);

   this.id_SocketEmisor = new ID_Socket (dirIp,puertoUnicast);
  }


  //==========================================================================
  /**
   * Constructor por defecto.
   * Este constructor es para crear TPDUS a partir del parser de un Buffer
   * @exception PTMFExcepcion
   * @exception ParametroInvalido Se lanza en el constructor de la clase TPDU.
   */
  protected TPDUDatos ()

      throws ParametroInvalidoExcepcion,PTMFExcepcion

  {

   super();

  }


  //==========================================================================
  /**
   * Crea un buffer del tamaño indicado y le introduce los datos de la cabecera
   * común.
   * @param Tipo valor del campo tipo
   * @exception PTMFExcepcion Es lanzada cuando ocurre algún error.
   * @exception ParametroInvalidoExcepcion si el tamaño del buffer a crear no
   * es suficiente para introducir los campos de la cabecera común, o si el tipo
   * no es válido.
   */
  protected Buffer construirCabeceraComun (short SUBTIPO,int tamañoBuffer) throws PTMFExcepcion,

                        ParametroInvalidoExcepcion
  {
    final String mn = "TPDU.construirCabeceraComun";
    int offset = 14;

    // El tamaño del buffer deberá ser al menos suficientes para añadir la
    // cabecera común a todos los TPDU de Datos.
    if (TPDUDatos.LONGHEADER > tamañoBuffer)
        throw new ParametroInvalidoExcepcion ("Tamaño de buffer insuficiente: " + tamañoBuffer);

    Buffer bufferResult = super.construirCabeceraComun ((short)PTMF.TPDU_DATOS,tamañoBuffer);

    // Comprobar que es del tipo TPDUDatos
    if (this.TIPO!=PTMF.TPDU_DATOS)
         throw new PTMFExcepcion ("El tipo no es TPDUDatos");

    // 15º BYTE : Subtipo: (3 bits )
    short anterior = bufferResult.getByte(offset);
    // anterior  : XXXX XXXX
    //      and  : 1111 0001 = 0xF1
    //            ----------
    //             XXXX 000X
    // subtipo   : 0000 XXX0 = 0x0E
    //            ----------
    //             XXXX XXX0
    anterior &= 0xF1;
    bufferResult.addByte((byte)(((SUBTIPO << 1) & 0x0E) | anterior),offset);

    return bufferResult;
  }



  //==========================================================================

  /**
   * El buffer pasado tiene que contener un TPDU Datos. Esta función extrae los
   * valores de la cabecera común a todos los TPDU Datos:
   * <ul>
   *   <li>Puerto Multicast</li>
   *   <li>Puerto Unicast</li>
   *   <li>IDGL</li>
   *   <li>Longiud</li>
   *   <li>Cheksum</li>
   *   <li>Versión</li>
   *   <li>Tipo</li>
   *   <li>Subtipo</li>
   * </ul><br>
   * Asigna al tpdu datos pasado por argumento los valores extraidos.
   * @param buf Objeto Buffer que contiene un TPDU Datos
   * @param tpduDatos tpdu al que se asignan los valores de los campos de la cabecera
   * común extraidos desde el buffer.
   * @param ipv4Emisor dirección IP unicast del emisor del TPDU Datos contenido
   * en el buffer.
   * @exception PTMFExcepcion Se lanza si ocurre un error al extraer los campos
   * del buffer.
   */

   static protected void parseCabeceraComun(Buffer buf,TPDUDatos tpduDatos,IPv4 ipv4Emisor)

                   throws PTMFExcepcion
  {
     final String mn = "TPDU.parseCabeceraComun";

     TPDU.parseCabeceraComun (buf,tpduDatos);

     // Obtener el subtipo
     tpduDatos.SUBTIPO = TPDUDatos.getSubtipo (buf);

     // Asignar valor a id_SocketEmisor
     tpduDatos.id_SocketEmisor = new ID_Socket (ipv4Emisor,tpduDatos.getPuertoUnicast());

  }

  //==========================================================================

  /**
   * El buffer pasado por argumento contiene un TPDU Datos recibido de la red.
   * Esta función extrae el valor del compo subtipo.
   * @param buf buffer que contiene un TPDU Datos.
   * @return subtipo
   * @exception PTMFExcepcion lanzada si hubo un error al leer el buffer
   */
  static byte getSubtipo(Buffer buf) throws PTMFExcepcion

  {
   final String mn ="TPDU.getTipo";
   int offset = 14;

   byte subtipo    = 0;

   //
   // Asegurarse que se ha recibido algún dato,
   // al menos los bytes de la cabecera del TPDU.
   //
   if (buf.getLength() < TPDU.LONGHEADER)
      throw new PTMFExcepcion(mn, "La cabecera del TPDU (paquete) recibido no se ha recibido entera. No se puede procesar el TPDU.");

   try
   {
      //
      // 15º BYTE : VERSION (2 bits), TIPO (2 bits), SUBTIPO (3 bits).
      //
      subtipo = (byte)((buf.getByte(offset) & 0x0E) >>> 1);
   }
   catch(ParametroInvalidoExcepcion e)
   {
      throw new PTMFExcepcion(mn,e.getMessage());
   }


   return subtipo;
  }

 //============================================================================

 /**

  * Devuelve el identificador del socket que emitio este TPDU Datos.

  */

 ID_Socket getID_SocketEmisor ()
 {
  return this.id_SocketEmisor;
 }

 //===========================================================================

 /**

  * Devuelve una cadena informativa del TPDU Datos

  */

 public String toString()

 {

   return "Puerto Multicast: " + this.getPuertoMulticast() +

          "\nPuerto Unicast: " + this.getPuertoUnicast() +

          "\nIDGL: " + this.ID_GRUPO_LOCAL +

          "\nLongitud: " + this.LONGITUD +

          "\nCHECKSUM: " + this.CHEKSUM +

          "\nVersion: " + this.VERSION +

          "\nTipo: " + this.TIPO +

          "\nSubtipo: " + this.SUBTIPO

          ;

 }



 } // Fin de la clase.




