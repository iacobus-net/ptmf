//============================================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: TPDUDatosNormal.java  1.0 9/9/99
//
//
//	Descripci�n: Clase TPDUDatosNormal.
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

import java.util.TreeMap;

import java.util.Vector;

/**
 * Clase TPDU Datos Normal.<br>
 * Hereda de la clase TPDUDatos.<br>
 *
 * Para crear un objeto de esta clase se tienen que usar los m�todos est�ticos.
 * Una vez creado no puede ser modificado.<br>
 *
 * El formato completo del TPDU Datos Normal es: <br>
 *
 *                      1 1 1 1 1 1 1 1 1 1 2 2 2 2 2 2 2 2 2 2 3 3<br>
 * +0-1-2-3-4-5-6-7-8-9-0-1-2-3-4-5-6-7-8-9-0-1-2-3-4-5-6-7-8-9-0-1<br>
 * +---------------------------------------------------------------+<br>
 * +      Puerto Multicast         |          Puerto Unicast       +<br>
 * +---------------------------------------------------------------+<br>
 * +                    ID_GRUPO_LOCAL(4 bytes primeros)           +<br>
 * +---------------------------------------------------------------+<br>
 * +ID_GRUPO_LOCAL(2 bytes �ltimos)|          Longitud             +<br>
 * +---------------------------------------------------------------+<br>
 * +                               |   | | | | | |A|I|F|F|         +<br>
 * +           Cheksum             | V |0|1|0|0|0|C|R|I|I| / / / / +<br>
 * +                               |   | | | | | |K| |N|N|         +<br>
 * +                               |   | | | | | |K| |C|T|         +<br>
 * +---------------------------------------------------------------+<br>
 * +        Tama�o Ventana         |           N�mero R�faga       +<br>
 * +---------------------------------------------------------------+<br>
 * +                      NUMERO DE SECUENCIA                      +<br>
 * +---------------------------------------------------------------+<br>
 * +                           Datos   ...                         +<br>
 * +---------------------------------------------------------------+<br>
 * <br>
 * <br>
 * Esta clase no es thread-safe.<br>
 * @see      Buffer
 * @version  1.0
 * @author M. Alejandro Garc�a Dom�nguez
 * <A HREF="mailto:Malejandro.garcia.dominguez@gmail.com">(Malejandro.garcia.dominguez@gmail.com)</A><p>
 * Antonio Berrocal Piris
 * <A HREF="mailto:AntonioBP.wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
 */
public class TPDUDatosNormal extends TPDUDatos
{
  // ATRIBUTOS
  /** Longitud de cabecera com�n a todos los TPDUDatosNormal */
  static final int LONGHEADER =  5*4  + 4;

  /**
   * IR (1 bit): Inicio de r�faga
   */
  byte IR = 0;

  /**
   * FIN_CONEXION (1 bit): Fin de la conexi�n
   */
  byte FIN_CONEXION = 0;

  /**
   * FIN_TRANSMISION (1 bit): Fin de la transmisi�n
   */
  byte FIN_TRANSMISION = 0;

  /**
   * ACK (1 bit):
   */
  byte ACK = 0;

  /**
   * N�mero de R�faga (16 bits): N�mero de r�faga al que pertenece el TPDU.
   */

  int NUMERO_RAFAGA = 0;


  /**

   * Tama�o de ventana (16 bits):

   */

  int TAMA�O_VENTANA = 0;


  /** El n�mero de secuencia de este TPDU de datos Normal. */
  NumeroSecuencia NUMERO_SECUENCIA = null;

  /** Datos */
  Buffer BUFFERDATOS = null;

  /** ID TPDU : utilizado por la funci�n {@link #getID_TPDU()}*/
  ID_TPDU ID_TPDU_FUENTE = null;

  //==========================================================================
  /**
   * Constructor utilizado para crear un TPDUDatosNormal.
   * @param socketPTMFImp Objeto SocketPTMFImp del que obtiene el valor de los
   * campos de la cabecera com�n.
   * @exception PTMFExcepcion
   * @exception ParametroInvalidoExcepcion lanzada si socketPTMFImp es null
   */
  private TPDUDatosNormal (SocketPTMFImp socketPTMFImp)
    throws PTMFExcepcion,ParametroInvalidoExcepcion
  {
   super (socketPTMFImp);
  }

 //============================================================================
 /**
  * Crea un TPDUDatosNormal con la informaci�n facilitada.
  * @param socketPTMFImp Objeto SocketPTMFImp del que obtiene el valor de los
  * campos de la cabecera com�n.
  * @param puertoMulticast
  * @param puertoUnicast
  * @param idgl
  * @param dirIp direcci�n IP unicast del emisor del TPDUDatosNormal
  * @param puertoUnicastFuente puerto unicast fuente
  */
  private TPDUDatosNormal (int puertoMulticast,
                           int puertoUnicast,
                           IDGL idgl,
                           IPv4 dirIp)
    throws PTMFExcepcion,ParametroInvalidoExcepcion
  {
   super (puertoMulticast,puertoUnicast,idgl,dirIp);
  }

  //==========================================================================

  /**
   * Constructor por defecto.
   * Este constructor es para crear TPDUS a partir del parser de un Buffer
   * @exception PTMFExcepcion
   * @exception ParametroInvalido Se lanza en el constructor de la clase TPDU.
   */
  private TPDUDatosNormal ()

      throws ParametroInvalidoExcepcion,PTMFExcepcion

  {

   super();

  }



 //============================================================================

 /**
  * Crea un vector con TPDUDatosRtx. que contiene este TPDUDatosNormal y las
  * listas de no asentidos. El vector contendr� uno o m�s TPDUDatosRtx.
  * @param socketPTMFImpParam utilzado para obtener informaci�n de la cabecera
  * com�n a todos los TPDU.
  * @param treeMapID_Socket contiene los id_sockets que no han enviado ACK
  * para el TPDU Datos Rtx.
  * @param treeMapIDGL contiene los idgls que no han enviado HACK o HSACK
  * para el TPDU Datos Rtx.
  * @return vector con los TPDUDatosRtx formados
  * @exception ParametroInvalidoExcepcion si alguno de los par�metros es err�neo.
  * @exception PTMFExcepcion si hay un error al crear los TPDUDatosRtx a partir
  * de la informaci�n facilitada en los argumentos.
  * @see TPDUDatosRtx
  */

 Vector convertirAVectorTPDUDatosRtx (SocketPTMFImp socketPTMFImpParam,
                                      TreeMap treeMapID_Socket,
                                      TreeMap treeMapIDGL)
                             throws ParametroInvalidoExcepcion, PTMFExcepcion
 {

  return TPDUDatosRtx.crearVectorTPDUDatosRtx (socketPTMFImpParam,
                                         ((this.IR  == 1) ? true : false),
                                         ((this.ACK == 1) ? true : false),
                                         ((this.FIN_CONEXION == 1) ? true : false),
                                         ((this.FIN_TRANSMISION == 1) ? true : false),
                                         this.NUMERO_RAFAGA,
                                         this.ID_GRUPO_LOCAL,
                                         this.ID_TPDU_FUENTE,
                                         treeMapID_Socket,
                                         treeMapIDGL,
                                         this.BUFFERDATOS);
 }


 //============================================================================
 /**
  * Crea un TPDUDatosNormal con la informaci�n facilitada.
  * @param socketPTMFImp Objeto SocketPTMFImp del que obtiene el valor de los
  * campos de la cabecera com�n.
  * @param setIR
  * @param setACK
  * @param setFIN_CONEXION
  * @param setFIN_TRANSMISION
  * @param numeroRafaga
  * @param nSec n�mero de secuencia
  * @param datos
  * @exception ParametroInvalidoExcepcion si alguno de los par�metros es err�neo.
  * @exception PTMFExcepcion si hay un error al crear el TPDUDatosNormal

  */
 static TPDUDatosNormal crearTPDUDatosNormal (SocketPTMFImp socketPTMFImp,
                                  boolean setIR,boolean setACK,boolean setFIN_CONEXION,
                                  boolean setFIN_TRANSMISION,
                                  int numeroRafaga,NumeroSecuencia nSec,
                                  Buffer datos)
   throws ParametroInvalidoExcepcion, PTMFExcepcion
 {
   final String mn = "TPDUDatosNormal.crearTPDUDatosNormal";

   TPDUDatosNormal resultTPDU = null;

   // Crear el TPDUDatosNormal vacio
   resultTPDU = new TPDUDatosNormal (socketPTMFImp);
   resultTPDU.BUFFERDATOS = datos;


   // Guardar los datos en la cabecera para cuando sean pedidos
   resultTPDU.IR  = (byte)(setIR  ? 1 : 0);
   resultTPDU.ACK = (byte)(setACK ? 1 : 0);
   resultTPDU.FIN_CONEXION = (byte)(setFIN_CONEXION ? 1 : 0);
   resultTPDU.FIN_TRANSMISION = (byte)(setFIN_TRANSMISION ? 1 : 0);
   resultTPDU.NUMERO_RAFAGA = numeroRafaga;
   resultTPDU.NUMERO_SECUENCIA = (NumeroSecuencia)nSec.clone();

   // Crear ID_TPDU_FUENTE
   resultTPDU.ID_TPDU_FUENTE = new ID_TPDU (resultTPDU.getID_SocketEmisor(),
                                             resultTPDU.getNumeroSecuencia());
   return resultTPDU;
 }

 //============================================================================
 /**
  * Crea un TPDUDatosNormal con la informaci�n facilitada.
  * @param puertoMulticast
  * @param puertoUnicast
  * @param idgl
  * @param dirIP
  * @param setIR
  * @param setACK
  * @param setFIN_CONEXION
  * @param setFIN_TRANSMISION
  * @param numeroRafaga
  * @param nSec numero secuencia
  * @param datos
  * @return objeto TPDUDatosNormal creado
  * @exception ParametroInvalidoExcepcion si alguno de los par�metros es err�neo.
  * @exception PTMFExcepcion si hay un error al crear el TPDUACK

  */
 static TPDUDatosNormal crearTPDUDatosNormal (int puertoMulticast,
                                              int puertoUnicast,
                                              IDGL idgl,
                                              IPv4 dirIp,
                                              boolean setIR,
                                              boolean setACK,
                                              boolean setFIN_CONEXION,
                                              boolean setFIN_TRANSMISION,
                                              int numeroRafaga,
                                              NumeroSecuencia nSec,
                                              Buffer datos)
   throws ParametroInvalidoExcepcion, PTMFExcepcion
 {
   final String mn = "TPDUDatosNormal.crearTPDUDatosNormal";

   TPDUDatosNormal resultTPDU = null;

   // Crear el TPDUDatosNormal vacio
   resultTPDU = new TPDUDatosNormal (puertoMulticast,puertoUnicast,idgl,dirIp);
   // if (datos!=null)
   resultTPDU.BUFFERDATOS = (Buffer)datos;//.clone();
   //else
   //   resultTPDU.BUFFERDATOS = null;

   // Guardar los datos en la cabecera para cuando sean pedidos
   resultTPDU.IR  = (byte)(setIR  ? 1 : 0);
   resultTPDU.ACK = (byte)(setACK ? 1 : 0);
   resultTPDU.FIN_CONEXION = (byte)(setFIN_CONEXION ? 1 : 0);
   resultTPDU.FIN_TRANSMISION = (byte)(setFIN_TRANSMISION ? 1 : 0);
   resultTPDU.NUMERO_RAFAGA = numeroRafaga;
   resultTPDU.NUMERO_SECUENCIA = (NumeroSecuencia)nSec.clone();

   // Crear ID_TPDU_FUENTE
   resultTPDU.ID_TPDU_FUENTE = new ID_TPDU (resultTPDU.getID_SocketEmisor(),
                                             resultTPDU.getNumeroSecuencia());

   return resultTPDU;
 }

  //==========================================================================
  /**
   * Construir el TPDU Datos Normal, devuelve un buffer con el contenido del TPDUDatosNormal,
   * seg�n el formato especificado en el protocolo.
   * <b>Este buffer no debe de ser modificado.</B>
   * @return un buffer con el TPDUDatosNormal.
   * @exception PTMFExcepcion Se lanza si ocurre alg�n error en la construcci�n
   * del TPDU
   * @exception ParametroInvalidoExcepcion lanzada si ocurre alg�n error en la
   * construcci�n del TPDU
   */
 Buffer construirTPDUDatosNormal () throws PTMFExcepcion,ParametroInvalidoExcepcion
 {
   final String mn = "TPDU.construirTPDUDatosNormal";
   int offset = 14;

   // Calcular el tama�o del tpdu a crear.
   int tama�o = TPDUDatosNormal.LONGHEADER;
   if (this.BUFFERDATOS != null)
      tama�o += this.BUFFERDATOS.getMaxLength();

   // Crear la cabecera com�n a todos los TPDU
   Buffer bufferResult = construirCabeceraComun (PTMF.SUBTIPO_TPDU_DATOS_NORMAL,
                                                 tama�o);

   // 15� BYTE : ACK : (1 bit)
   short anterior = bufferResult.getByte (offset);
   // anterior : XXXX XXXX
   //      and : 1111 1110 = 0xFE
   //           ----------
   //            XXXX XXX0
   //   ACK    : 0000 000X
   //           ----------
   //            XXXX XXXX
   anterior &= 0xFE;
   bufferResult.addByte((byte)((this.ACK & 0x01) | anterior),offset);
   offset ++;


   // 16�        BYTE : IR (1 bit)

   //              IR : X000  0000

   //     FIN_CONEXION: 0X00  0000

   //  FIN_TRANSMISION: 00X0  0000

   bufferResult.addByte ((byte)(((this.IR<<7)&0x80) | ((this.FIN_CONEXION<<6)&0x40) | ((this.FIN_TRANSMISION<<5)&0x20)),offset);

   offset++;

   // 17� Y 18� BYTE : Tama�o Ventana
   bufferResult.addShort (this.TAMA�O_VENTANA,offset);
   offset+=2;

   // 19� Y 20� BYTE : N�mero de r�faga
   bufferResult.addShort (this.NUMERO_RAFAGA,offset);
   offset+=2;

   // 21�, 22�, 23� y 24� BYTE : N�mero de Secuencia
   bufferResult.addInt (this.NUMERO_SECUENCIA.tolong(),offset);
   offset+=4;

   // 25� y sucesivos BYTE : Datos
   if (this.BUFFERDATOS!=null)
      bufferResult.addBytes (this.BUFFERDATOS,0,offset,
                      this.BUFFERDATOS.getMaxLength());

   return bufferResult;
 }

  //==========================================================================
  /**
   * Parse un Buffer de datos recibidos y crea un TPDU Datos Normal que lo encapsule.
   * El buffer debe de contener un TPDU Datos Normal.
   * @param buf Un buffer que contiene el TPDU Datos Normal recibido.
   * @param ipv4Emisor direcci�n IP unicast del emisor.
   * @exception PTMFExcepcion El buffer pasado no contiene una cabecera TPDU
   * correcta, el mensaje de la excepci�n especifica el tipo de error.
   * @exception ParametroInvalidoExcepcion Se lanza si el buffer pasado no
   * contiene un TPDUDatosNormal v�lido.
   */
 static  TPDUDatosNormal parserBuffer (Buffer buffer,IPv4 ipv4Emisor)
   throws PTMFExcepcion,ParametroInvalidoExcepcion
 {
  final String mn = "TPDUDatosNormal.parserBuffer";
  int aux;
  int offset = 14;

  if (buffer==null)
     throw new ParametroInvalidoExcepcion ("Buffer nulo.");

  // El tama�o del buffer debe ser al menos igual a TPDUDatosNormal.LONGHEADER
  if (TPDUDatosNormal.LONGHEADER > buffer.getMaxLength())
    throw new ParametroInvalidoExcepcion ("Buffer no v�lido");

  // Crear el TPDUDatosNormal.
  TPDUDatosNormal tpduDatosNormal = new TPDUDatosNormal ();

  // Analizar los datos comunes CURIOSIDAD: OBSERVAR QUE HAY DOS METODOS
  // EST�TICOS HEREDADOS Y QUE EN PRINCIPIO NO PUEDE DISTINGUIR.
  TPDUDatos.parseCabeceraComun (buffer,tpduDatosNormal,ipv4Emisor);

  // Comprobar que el subtipo es correcto.
  if (tpduDatosNormal.SUBTIPO != PTMF.SUBTIPO_TPDU_DATOS_NORMAL)
        throw new PTMFExcepcion ("El subtipo del TPDUDatos no es correcto");

  // 15� BYTE : ACK (1 BIT)
  aux = buffer.getByte (offset);


  //     ACK:   XXXX XXXX
  //     And:   0000 0001 = 0x01
  //            ---------
  //            0000 000X
  tpduDatosNormal.ACK = (byte)(aux & 0x01);
  offset++;


  // 16� BYTE : IR (1 bit) FIN_CONEXION(1 bit)
  aux = buffer.getByte (offset);
  //      IR:   XXXX XXXX
  //     And:   1000 0000 = 0x80
  //            ---------
  //            X000 0000
  //     >>>:   0000 000X
  tpduDatosNormal.IR = (byte) ((aux & 0x80) >>> 7);
  //     FIN_CONEXION:   XXXX XXXX
  //     And:   0100 0000 = 0x40
  //            ---------
  //            0X00 0000
  //     >>>:   0000 000X
  tpduDatosNormal.FIN_CONEXION = (byte) ((aux & 0x40) >>> 6);
  //     FIN_TRANSMISION:   XXXX XXXX
  //     And:   0100 0000 = 0x20
  //            ---------
  //            00X0 0000
  //     >>>:   0000 000X
  tpduDatosNormal.FIN_TRANSMISION = (byte) ((aux & 0x20) >>> 5);
  offset++;

  //
  // 17� y 18� BYTE : Tama�o Ventana
  //
  tpduDatosNormal.TAMA�O_VENTANA = buffer.getShort (offset);
  offset+=2;

  //
  // 19� y 20� BYTE : N�mero R�faga
  //
  tpduDatosNormal.NUMERO_RAFAGA = buffer.getShort (offset);
  offset+=2;

  //
  // 21�, 22�, 23� y 24�  BYTE : N�mero de secuencia
  //
  tpduDatosNormal.NUMERO_SECUENCIA = new NumeroSecuencia (buffer.getInt (offset));
  offset+=4;

  // Crear ID_TPDU_FUENTE
  tpduDatosNormal.ID_TPDU_FUENTE = new ID_TPDU (tpduDatosNormal.getID_SocketEmisor(),
                                                     tpduDatosNormal.getNumeroSecuencia());

  //
  // 25� y sucesivos BYTES : Datos
  //
  int tama�oDatos = buffer.getMaxLength() - offset;
  if (tama�oDatos>0)
     tpduDatosNormal.BUFFERDATOS = new Buffer (buffer.getBytes (offset,tama�oDatos));
  else tpduDatosNormal.BUFFERDATOS = null;

  return tpduDatosNormal;
 }


 //============================================================================
 /**
  * Devuelve el n�mero de secuencia.
  */
 NumeroSecuencia getNumeroSecuencia ()
 {
   return this.NUMERO_SECUENCIA;
 }

 //============================================================================
 /**
  * Devuelve el tama�o de ventana.
  */
 int getTama�oVentana ()
 {
  return PTMF.TAMA�O_VENTANA_RECEPCION;
 }

 //===========================================================================
 /**
  * Devuelve el ID_TPDU.
  */
 ID_TPDU getID_TPDU ()
 {
  return this.ID_TPDU_FUENTE;
 }

 //===========================================================================
 /**
  * Devuelve el n�mero de la r�faga.
  */
 int getNumeroRafaga ()
 {
    return this.NUMERO_RAFAGA;
 }


 //===========================================================================

 /**
  * Devuelve true si el bit IR vale 1.
  */
 boolean getIR ()
 {

  if (this.IR == 1)
   return true;

  return false;
 }



 //===========================================================================
 /**
  * Devuelve true si el bit FIN_CONEXION vale 1.
  */
 boolean getFIN_CONEXION()
 {
  if (this.FIN_CONEXION == 1)
   return true;

  return false;
 }


 //===========================================================================

 /**
  * Devuelve true si el bit FIN_TRANSMISION vale 1.
  */
 boolean getFIN_TRANSMISION()
 {

  if (this.FIN_TRANSMISION == 1)
   return true;

  return false;
 }


 //===========================================================================
 /**
  * Devuelve true si el bit ACK vale 1.
  */
 boolean getACK ()
 {

  if (this.ACK == 1)
   return true;

  return false;
 }


 //===========================================================================
 /**
  * Devuelve el buffer de datos.
  */
 Buffer getBufferDatos ()
 {

  return this.BUFFERDATOS;

 }


 //===========================================================================
 /**
  * Devuelve una cadena informativa.
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
          "\nSubtipo: " + PTMF.SUBTIPO_TPDU_DATOS_NORMAL +
          "\nACK: " + this.ACK +
          "\nIR: " + this.IR +
          "\nFIN_CONEXION: " + this.FIN_CONEXION +
          "\nFIN_TRANSMISION: " + this.FIN_TRANSMISION +
          "\nN�mero R�faga: " + this.NUMERO_RAFAGA +
          "\nN�mero Secuencia: " + this.NUMERO_SECUENCIA+
          "\n====================================================";
          //"\nDatos: " + this.BUFFERDATOS+

 }


} // Fin de la clase.


