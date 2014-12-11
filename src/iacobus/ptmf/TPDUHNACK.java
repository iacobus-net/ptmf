//============================================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	File: TPDUDatosHNACK.java  1.0 20/09/99
//
//	Descripción: Clase TPDUDatosHNACK
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

import java.util.Iterator;
import java.util.Vector;

/**
 * Clase TPDU HNACK.<br>
 * Hereda de la clase TPDUDatos.<br>
 *
 * Para crear un objeto de esta clase se tienen que usar los métodos estáticos.
 * Una vez creado no puede ser modicado.<br>
 *
 * El formato completo del TPDU HNACK es: <br>
 * <br>
 *                      1 1 1 1 1 1 1 1 1 1 2 2 2 2 2 2 2 2 2 2 3 3<br>
 * +0-1-2-3-4-5-6-7-8-9-0-1-2-3-4-5-6-7-8-9-0-1-2-3-4-5-6-7-8-9-0-1<br>
 * +---------------------------------------------------------------+<br>
 * +      Puerto Mulitcast         |          Puerto Unicast       +<br>
 * +---------------------------------------------------------------+<br>
 * +                        ID_GRUPO_LOCAL                         +<br>
 * +                      (4 bytes primeros)                       +<br>
 * +---------------------------------------------------------------+<br>
 * +        ID_GRUPO_LOCAL         |          Longitud             +<br>
 * +      (2 bytes últimos)        |                               +<br>
 * +---------------------------------------------------------------+<br>
 * +           Cheksum             | V |0|1|1|1|1|    No Usado     +<br>
 * +---------------------------------------------------------------+<br>
 * +                          Dirección IP 1                       +<br>
 * +---------------------------------------------------------------+<br>
 * +       Puerto Unicast 1        |     Numero de Secuencia 1     +<br>
 * +                               |     (16 bits superiores)      +<br>
 * +---------------------------------------------------------------+<br>
 * +      Número de Secuencia 1    |         Dirección IP 2        +<br>
 * +      (16 bits inferiores)     |       (16 bits superiores)    +<br>
 * +---------------------------------------------------------------+<br>
 * +         Dirección IP 2        |         Puerto Unicast 2      +<br>
 * +      (16 bits inferiores)     |                               +<br>
 * +---------------------------------------------------------------+<br>
 * +                      Número de Secuencia 2                    +<br>
 * +---------------------------------------------------------------+<br>
 * +                              ...                              +<br>
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
public class TPDUHNACK extends TPDUDatos
{

  // ATRIBUTOS
  /** Tamaño de la cabecera del TPDUHNACK*/
  static final int LONGHEADER = 4*4 ;

   /**
    * Lista con los id_tpuds que solicita por no haberlos recibido.
    * <table border=1>
    *  <tr>  <td><b>Key:</b></td>
    *	    <td>{@link ID_TPDU} no recibido.</td>
    *  </tr>
    *  <tr>  <td><b>Value:</b></td>
    *	    <td>null</td>
    *  </tr>
    * </table>
    */
   ListaOrdID_TPDU LISTA_ORD_ID_TPDU = null;

  //==========================================================================
  /**
   * Constructor utilizado para crear un TPDUHNACK.
   * @param socketPTMFImp Objeto SocketPTMFImp del que obtiene el valor de los
   * campos de la cabecera común.
   * @exception PTMFExcepcion
   * @exception ParametroInvalidoExcepcion lanzada si socketPTMFImp es null
   */
  private TPDUHNACK (SocketPTMFImp socketPTMFImp)
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
  private TPDUHNACK ()
      throws ParametroInvalidoExcepcion,PTMFExcepcion
  {
   super();
  }

 //============================================================================
 /**
  * Crea un TPDUHNACK con la información facilitada.
  * @param socketPTMFImp Objeto SocketPTMFImp del que obtiene el valor de los
  * campos de la cabecera común.
  * @param listaID_TPDU lista con los ID_TPDU solicitados
  * @return objeto TPDUHNACK creado.
  * @exception ParametroInvalidoExcepcion si alguno de los parámetros es erróneo.
  * @exception PTMFExcepcion si hay un error al crear el TPDUHNACK
  */
 static TPDUHNACK crearTPDUHNACK (SocketPTMFImp socketPTMFImp,
                                ListaOrdID_TPDU listaID_TPDU)
   throws ParametroInvalidoExcepcion, PTMFExcepcion
 {
   TPDUHNACK resultTPDU = null;

   if (listaID_TPDU == null)
      throw new PTMFExcepcion ("La lista pasada no puede ser null.");

   // Crear el TPDUHNACK vacio. Cada ID_TPDU ocupa 10 bytes
   resultTPDU = new TPDUHNACK (socketPTMFImp);

   // Guardar los datos en la cabecera para cuando sean pedidos
   resultTPDU.LISTA_ORD_ID_TPDU = (ListaOrdID_TPDU)listaID_TPDU.clone();

   return resultTPDU;
 }

  //==========================================================================
  /**
   * Construir el TPDU HNACK, devuelve un buffer con el contenido del TPDUHNACK,
   * según el formato especificado en el protocolo.
   * <b>Este buffer no debe de ser modificado.</B>
   * @return un buffer con el TPDUHNACK.
   * @exception PTMFExcepcion Se lanza si ocurre algún error en la construcción
   * del TPDU
   * @exception ParametroInvalidoExcepcion lanzada si ocurre algún error en la
   * construcción del TPDU
   */
 Buffer construirTPDUHNACK () throws PTMFExcepcion,ParametroInvalidoExcepcion
 {
  final String mn = "TPDU.construirTPDUHNACK";
  int offset = 14;


   int tamaño = TPDUHNACK.LONGHEADER + this.LISTA_ORD_ID_TPDU.size()*10;

   // Crear la cabecera común a todos los TPDU
   Buffer bufferResult =this.construirCabeceraComun (PTMF.SUBTIPO_TPDU_DATOS_HNACK,tamaño);

   if (bufferResult==null)
        throw new PTMFExcepcion (mn + "Error en el parser");


   // 15º BYTE : Subtipo: 110 ACK : (1 bit)
   short anterior = bufferResult.getByte (offset);
   // anterior : XXXX XXXX
   //      and : 1111 1110 = 0xFE
   //           ----------
   //            XXXX XXX0
   anterior &= 0xFE;
   bufferResult.addByte((byte)anterior,offset);
   offset++;

   // 16º BYTE : IR (1 bit) Número de IP (7 bits).
   //   IR : 0000  0000
   bufferResult.addByte ((byte)0,offset);
   offset++;

   // 17º y sucesivos BYTE : [IP,Puerto Unicast,Número Secuencia]
   Iterator iteradorID_TPDU = this.LISTA_ORD_ID_TPDU.iteradorID_TPDU();
   ID_TPDU id_TPDUNext  = null;
   ID_Socket id_Socket  = null;
   NumeroSecuencia nSec = null;
   while (iteradorID_TPDU.hasNext())
    {
     id_TPDUNext = (ID_TPDU)iteradorID_TPDU.next();
     id_Socket   = id_TPDUNext.getID_Socket ();
     nSec        = id_TPDUNext.getNumeroSecuencia ();

     // Añadir IP
     bufferResult.addBytes (id_Socket.getDireccion().ipv4,0,offset,4);
     offset += 4;
     // Añadir Puerto Unicast
     bufferResult.addShort (id_Socket.getPuertoUnicast(),offset);
     offset += 2;
     // Añadir el número de secuencia
     bufferResult.addInt (nSec.tolong(),offset);
     offset += 4;
    } // Fin del while

   return bufferResult;

 }

  //==========================================================================
  /**
   * Parse un Buffer de datos recibidos y crea un TPDU HNACK que lo encapsule.
   * El buffer debe de contener un TPDU HNACK.
   * @param buffer Un buffer que contiene el TPDU HNACK recibido.
   * @param ipv4Emisor dirección IP unicast del emisor.
   * @return Un objeto TPDUHNACK
   * @exception PTMFExcepcion El buffer pasado no contiene una cabecera TPDU
   * correcta, el mensaje de la excepción especifica el tipo de error.
   * @exception ParametroInvalidoExcepcion Se lanza si el buffer pasado no
   * contiene un TPDUHNACK válido.
   */
 static  TPDUHNACK parserBuffer (Buffer buffer,IPv4 ipv4Emisor)
   throws PTMFExcepcion,ParametroInvalidoExcepcion
 {
  final String mn = "TPDUHNACK.parserBuffer";
  int aux;
  int offset = 16;


  if (buffer==null)
     throw new ParametroInvalidoExcepcion (mn + "Buffer nulo");

  if (TPDUHNACK.LONGHEADER > buffer.getMaxLength())
     throw new ParametroInvalidoExcepcion (mn + "Buffer incorrecto");

  // Crear el TPDUHNACK.
  TPDUHNACK tpduHNACK = new TPDUHNACK ();

  // Analizar los datos comunes
  TPDUDatos.parseCabeceraComun (buffer,tpduHNACK,ipv4Emisor);

  // Comprobar si el tipo es correcto
  if (tpduHNACK.SUBTIPO != PTMF.SUBTIPO_TPDU_DATOS_HNACK)
      throw new PTMFExcepcion (mn+"Subtipo del TPDU Datos no es HNACK");

  // 13º BYTE : SUBTIPO (3 BITS) ACK (1 BIT)

  // 14º BYTE : No usado

  // 15º y sucesivos BYTE : [IP,Puerto Unicast,Número Secuencia]
  // Crear la lista

  // Crear la lista
  tpduHNACK.LISTA_ORD_ID_TPDU = new ListaOrdID_TPDU ();

  ID_TPDU id_TPDU      = null;
  ID_Socket id_Socket  = null;
  IPv4           ipv4  = null;
  while (offset<buffer.getMaxLength())
   {
    // Obtener Dirección IP
    ipv4 = new IPv4 (new Buffer (buffer.getBytes(offset,4)));
    offset += 4;
    // Unir la Dirección IP con el puerto unicast que se obtenga
    id_Socket = new ID_Socket (ipv4,buffer.getShort (offset));
    offset += 2;

    // Unir id_Socket con el número de secuencia que se obtenga
    id_TPDU = new ID_TPDU (id_Socket,new NumeroSecuencia (buffer.getInt (offset)));
    offset += 4;

    tpduHNACK.LISTA_ORD_ID_TPDU.put (id_TPDU,null);
    } // Fin del while

  return tpduHNACK;
 }

 //===========================================================================
 /**
  * Devuelve una cadena informativa del TPDUHNACK
  */
 public String toString()
 {
   String result = new String (
          "Puerto Multicast: " + this.getPuertoMulticast () +
          "\nPuerto Unicast: " + this.getPuertoUnicast () +
          "\nIDGL: " + this.ID_GRUPO_LOCAL +
          "\nLongitud: " + this.LONGITUD +
          "\nCHECKSUM: " + this.CHEKSUM +
          "\nVersion: " + this.VERSION +
          "\nTipo: " + this.TIPO +
          "\nSubtipo: " + PTMF.SUBTIPO_TPDU_DATOS_HNACK
         );
   // Añadir al String los id. socket
   if (this.LISTA_ORD_ID_TPDU != null)
     result = result + "\nID_TPDU: " + this.LISTA_ORD_ID_TPDU;

   return result;
 }

  //===========================================================================
  /**
   * Devuelve <b>una copia</b> de la lista de id_tpdus solicitados.
   * @see #LISTA_ORD_ID_TPDU
   */
  public ListaOrdID_TPDU getListaID_TPDU ()
  {
   return (ListaOrdID_TPDU)this.LISTA_ORD_ID_TPDU.clone();
  }


}

