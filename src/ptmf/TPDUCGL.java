//============================================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: TPDUCGL.java  1.0 23/09/99
//
//
//	Descripción: Clase TPDUCGL
//
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


import java.util.TreeMap;
import java.util.Iterator;


/**
 * Clase TPDU CGL (Control de Grupo Local) de PTMF.<br>
 * Hereda de la clase TPDU.<br>
 *
 * Para crear un objeto de esta clase se tienen que usar los métodos estáticos.
 * Una vez creado no puede ser modicado.<br>
 *
 * El formato completo del TPDU CGL si el bit IP vale 1 es:<br>
 *
 *                      1 1 1 1 1 1 1 1 1 1 2 2 2 2 2 2 2 2 2 2 3 3<br>
 * +0-1-2-3-4-5-6-7-8-9-0-1-2-3-4-5-6-7-8-9-0-1-2-3-4-5-6-7-8-9-0-1<br>
 * +---------------------------------------------------------------+<br>
 * +      Puerto Multicast         |          Puerto Unicast       +<br>
 * +---------------------------------------------------------------+<br>
 * +                    ID_GRUPO_LOCAL(4 bytes primeros)           +<br>
 * +---------------------------------------------------------------+<br>
 * +ID_GRUPO_LOCAL(2 bytes últimos)|          Longitud             +<br>
 * +---------------------------------------------------------------+<br>
 * +           Cheksum             | V |0|0|Subtipo|     TTL       +<br>
 * +---------------------------------------------------------------+<br>
 * + Mé  |I|  No   |     Número    |                               +<br>
 * + tri |P| Usado |   Secuencia   |     Número de IDs             +<br>
 * + ca  |=|       |               |                               +<br>
 * +     |1|       |               |                               +<br>
 * +---------------------------------------------------------------+<br>
 * + Nº MÁXIMO de Sockets/GL       |    Nº de Sockets en GL        |<br>
 * +---------------------------------------------------------------+<br>
 * +                           IP 1                                +<br>
 * +---------------------------------------------------------------+<br>
 * +       Puerto Unicast 1        |            ...                +<br>
 * +---------------------------------------------------------------+<br>
 * <br>
 * El formato completo del TPDU CGL si el bit IP vale 0 es:<br>
 *
 *                      1 1 1 1 1 1 1 1 1 1 2 2 2 2 2 2 2 2 2 2 3 3<br>
 * +0-1-2-3-4-5-6-7-8-9-0-1-2-3-4-5-6-7-8-9-0-1-2-3-4-5-6-7-8-9-0-1<br>
 * +---------------------------------------------------------------+<br>
 * +      Puerto Multicast         |          Puerto Unicast       +<br>
 * +---------------------------------------------------------------+<br>
 * +                    ID_GRUPO_LOCAL(4 bytes primeros)           +<br>
 * +---------------------------------------------------------------+<br>
 * +ID_GRUPO_LOCAL(2 bytes últimos)|          Longitud             +<br>
 * +---------------------------------------------------------------+<br>
 * +           Cheksum             | V |0|0|Subtipo|     TTL       +<br>
 * +---------------------------------------------------------------+<br>
 * + Mé  |I|  No   |     Número    |                               +<br>
 * + tri |P| Usado |   Secuencia   |     Número de IDs             +<br>
 * + ca  |=|       |               |                               +<br>
 * +     |0|       |               |                               +<br>
 * +---------------------------------------------------------------+<br>
 * + Nº MÁXIMO de Sockets/GL       |    Nº de Sockets en GL        |<br>
 * +---------------------------------------------------------------+<br>
 * +                Identificador Grupo Local ....                 +<br>
 * +----------z-----------------------------------------------------+<br>
 * <br>
 * <br>
 * Esta clase no es thread-safe.
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 * Antonio Berrocal Piris
 * <A HREF="mailto:AntonioBP.wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
*/
class TPDUCGL extends TPDU implements Cloneable
{

  /** Tamaño de la cabecera del TPDUCGL*/
  static final int LONGHEADER = 15 + 9;


  /** Indica en bytes el tamaño del TPDU. Es calculado en el constructor*/
  int tamaño = 0;

  /**
   * Subtipo (4 bits): Subtipo del TPDU Control de Grupo Local:
   * <table>
   * <tr><td> Bits </td><td> Mensaje:</td></tr>
   * <tr><td> 0001 </td><td> BUSCAR_GRUPO_LOCAL</td></tr>
   * <tr><td> 0010 </td><td> GRUPO_LOCAL</td></tr>
   * <tr><td> 0011 </td><td> UNIRSE_A_GRUPO_LOCAL</td></tr>
   * <tr><td> 0100 </td><td> SOCKET_ACEPTADO_EN_GRUPO_LOCAL</td></tr>
   * <tr><td> 0101 </td><td> DEJAR_GRUPO_LOCAL</td></tr>
   * <tr><td> 0110 </td><td> ELIMINACION_GRUPO_LOCAL</td></tr>
   * <tr><td> 0111 </td><td> BUSCAR_GRUPO_LOCAL_VECINO</td></tr>
   * <tr><td> 1000 </td><td> GRUPO_LOCAL_VECINO</td></tr>
   * <tr><td> 1001 </td><td> BUSCAR_GL_PARA_EMISOR</td></tr>
   * <tr><td> 1010 </td><td> GL_PARA_EMISOR</td></tr>
   * </table>
   */
  private byte SUBTIPO = 0;

  /**
   * TTL (8 bits): Especifica el TTL utilizado en el TPDU
   */
  private byte TTL = 0;

  /**
   * Métrica (8 bits): Especifica la métrica utilizada en los mensajes CGL.
   *
   * <table>
   * <tr><td> Bits </td><td> Significado:</td></tr>
   * <tr><td> 000  </td><td> Número de saltos (TTL)</td></tr>
   * </table>
   */
  private short METRICA = 0;

  /**
   * IP (1 bit): Flag de control que especifica si la cabecera contiene
   * identificadores de grupos locales o dirección IP de miembros del grupo local.
   */
  private short IP = 0;

  /**
   * N_SECUENCIA (8 bitS): Número de secuencia del TPDU CGL.
   */
  private short N_SECUENCIA = 0;

  /**
   * Nº Ids (10 bits):  Especifica el número de identificadores de grupos
   * locales que contiene el mensaje CGL
   */
  private int N_IDS = 0;

  /**
   * Nº MAX de sockets (10 bits): El número máximo de sockets establecido para el
   * grupo local con id "id_grupo_local".
   */
  private int N_MAX_SOCKETS = 0;

  /**
   * Nº de sockets (10 bits): El número de sockets que tiene a su cargo
   * actualmente el CG.
   */
  private int N_SOCKETS = 0;

  /**
   * TreeMap de identificadores de grupos locales(ID_GRUPO_LOCAL N,(32 bits))
   */
  private TreeMap treeMapIDGL = null;

  /**
   * IDGL Emisor
   */
  private IDGL IDGL_EMISOR = null;

  /**
   * TreeMap de ID_SOCKET
   */
  private TreeMap treeMapID_SOCKET = null;

  /**
   * ID_Socket Almacena un objeto IPv4 y el Puerto Unicast.
   */
  private ID_Socket ID_SOCKET = null;

  /**
   * Número de Secuencia para los TPDU CGL
   */
  private static short NUMERO_SECUENCIA = 0;


  //==========================================================================
  /**
   * Constructor por defecto.
   * Este constructor es para crear TPDUS a partir del parser de un Buffer
   * @exception PTMFExcepcion
   * @exception ParametroInvalido Se lanza en el constructor de la clase TPDU.
   */
  private TPDUCGL() throws PTMFExcepcion, ParametroInvalidoExcepcion
  {
    super();

    final String  mn = "TPDUCGL.TPDUCGL";
  }

  //==========================================================================
  /**
   * Constructor utilizado para crear un TPDUCGL.
   * @param socketPTMFImp Objeto SocketPTMFImp del que obtiene el valor de los
   * campos de la cabecera común.
   * @exception PTMFExcepcion
   * @exception ParametroInvalidoExcepcion lanzada si socketPTMFImp es null
   */
  private TPDUCGL(SocketPTMFImp socketPTMFImp) throws PTMFExcepcion, ParametroInvalidoExcepcion
  {
    super(socketPTMFImp);

    final String  mn = "TPDUCGL.TPDUCGL";
  }

  //============================================================================
  /**
   * Crea un TPDUDCGL con la información facilitada.
   * @param socketPTMFImp Objeto SocketPTMFImp del que obtiene el valor de los
   * campos de la cabecera común.
   * @param SUBTIPO Subtipo del TPDU CGL
   * @param TTL campo TTL
   * @param METRICA campo metrica
   * @param flagIP Campo FlagIP
   * @param N_MAX_SOCKETS Campo N_MAX_SOCKETS
   * @param N_SOCKETS Campo N_SOCKETS
   * @param treeMapIDGL Campos IDGL
   * @param treeMapID_SOCKET Campos ID_SOCkETs
   * @param ID_SOCKET Campo ID_Socket
   * @return objeto TPDUCGL creado
   * @exception ParametroInvalidoexcepcion
   * @exception PTMFExcepcion
   */
  static TPDUCGL crearTPDUCGL (
    SocketPTMFImp socketPTMFImp,
    byte SUBTIPO,
    byte TTL,
    short METRICA,
    boolean flagIP,
    int N_MAX_SOCKETS,
    int N_SOCKETS,
    TreeMap treeMapIDGL,
    TreeMap treeMapID_SOCKET,
    ID_Socket ID_SOCKET)
   throws ParametroInvalidoExcepcion, PTMFExcepcion
  {
    final String mn = "TPDUCGL.crearTPDUCGL";
    TPDUCGL resultTPDU = null;


    int tamaño = TPDUCGL.LONGHEADER;
    resultTPDU = new TPDUCGL (socketPTMFImp);


    // Verificar parámetros importantes..
    switch(SUBTIPO)
    {

          case PTMF.TPDU_CGL_BUSCAR_GRUPO_LOCAL:
          case PTMF.TPDU_CGL_GRUPO_LOCAL:
          case PTMF.TPDU_CGL_BUSCAR_GRUPO_LOCAL_VECINO:
          case PTMF.TPDU_CGL_UNIRSE_A_GRUPO_LOCAL:
          case PTMF.TPDU_CGL_SOCKET_ACEPTADO_EN_GRUPO_LOCAL:
          case PTMF.TPDU_CGL_DEJAR_GRUPO_LOCAL:
          case PTMF.TPDU_CGL_ELIMINACION_GRUPO_LOCAL:
          case PTMF.TPDU_CGL_GL_PARA_EMISOR:
          case PTMF.TPDU_CGL_BUSCAR_GL_PARA_EMISOR:
          case PTMF.TPDU_CGL_GRUPO_LOCAL_VECINO:
           break;

          default:
           throw new ParametroInvalidoExcepcion(mn, "Subtipo de cabecera TPDU CGL incorrecta.");
    }


    resultTPDU.N_IDS = 0;
    // Calcular el tamaño...

    if (!flagIP)
    {
      if (treeMapIDGL != null)
      {
          tamaño+= treeMapIDGL.size()*(6 +1/*TTL*/);
          resultTPDU.N_IDS += treeMapIDGL.size();
      }

    }
    else
    {
      if (ID_SOCKET == null)
        throw new ParametroInvalidoExcepcion(mn,"Parámetro ID_SOCKET nulo.");

      resultTPDU.N_IDS ++;

      if (treeMapID_SOCKET != null)
      {
        tamaño += treeMapID_SOCKET.size()*6+6/* de ID_SOCKET*/;
        resultTPDU.N_IDS += treeMapID_SOCKET.size();

      }
      else
        tamaño += 6/* de ID_SOCKET*/;
    }

    resultTPDU.tamaño = tamaño;

    // Guardar los datos ...
    resultTPDU.SUBTIPO = SUBTIPO;
    resultTPDU.TTL = TTL;
    resultTPDU.METRICA= METRICA;
    resultTPDU.IP = (byte) (flagIP ? 1 : 0);
    resultTPDU.N_MAX_SOCKETS = N_MAX_SOCKETS;
    resultTPDU.N_SOCKETS = N_SOCKETS;
    resultTPDU.treeMapIDGL = treeMapIDGL;
    resultTPDU.treeMapID_SOCKET = treeMapID_SOCKET;
    resultTPDU.ID_SOCKET = ID_SOCKET;

    return resultTPDU;
  }

  //============================================================================
  /**
   * Crea un TPDUDCGL con la información facilitada.
   * @param socketPTMFImp Objeto SocketPTMFImp del que obtiene el valor de los
   * campos de la cabecera común.
   * @param SUBTIPO Subtipo del TPDU CGL
   * @param TTL campo TTL
   * @param METRICA campo metrica
   * @param flagIP Campo FlagIP
   * @param N_MAX_SOCKETS Campo N_MAX_SOCKETS
   * @param N_SOCKETS Campo N_SOCKETS
   * @param IDGL_EMISOR IDGL Emisor
   * @param treeMapIDGL Campos IDGL
   * @param treeMapID_SOCKET Campos ID_SOCkETs
   * @param ID_SOCKET Campo ID_Socket
   * @return objeto TPDUCGL creado
   * @exception ParametroInvalidoexcepcion
   * @exception PTMFExcepcion
   */
  static TPDUCGL crearTPDUCGL (
    SocketPTMFImp socketPTMFImp,
    byte SUBTIPO,
    byte TTL,
    short METRICA,
    boolean flagIP,
    int N_MAX_SOCKETS,
    int N_SOCKETS,
    IDGL IDGL_EMISOR,
    TreeMap treeMapIDGL,
    TreeMap treeMapID_SOCKET,
    ID_Socket ID_SOCKET)
   throws ParametroInvalidoExcepcion, PTMFExcepcion
  {
    final String mn = "TPDUCGL.crearTPDUCGL";
    TPDUCGL resultTPDU = null;

    int tamaño = TPDUCGL.LONGHEADER;
    resultTPDU = new TPDUCGL (socketPTMFImp);

    // Verificar parámetros importantes..
    switch(SUBTIPO)
    {
          case PTMF.TPDU_CGL_BUSCAR_GRUPO_LOCAL:
          case PTMF.TPDU_CGL_GRUPO_LOCAL:
          case PTMF.TPDU_CGL_BUSCAR_GRUPO_LOCAL_VECINO:
          case PTMF.TPDU_CGL_UNIRSE_A_GRUPO_LOCAL:
          case PTMF.TPDU_CGL_SOCKET_ACEPTADO_EN_GRUPO_LOCAL:
          case PTMF.TPDU_CGL_DEJAR_GRUPO_LOCAL:
          case PTMF.TPDU_CGL_ELIMINACION_GRUPO_LOCAL:
          case PTMF.TPDU_CGL_GL_PARA_EMISOR:
          case PTMF.TPDU_CGL_BUSCAR_GL_PARA_EMISOR:
          case PTMF.TPDU_CGL_GRUPO_LOCAL_VECINO:
           break;

          default:
           throw new ParametroInvalidoExcepcion(mn, "Subtipo de cabecera TPDU CGL incorrecta.");
    }


    resultTPDU.N_IDS = 0;
    // Calcular el tamaño...

    if (!flagIP)
    {
      if (treeMapIDGL != null)
      {
          tamaño+= treeMapIDGL.size()*(6 +1/*TTL*/);
          resultTPDU.N_IDS += treeMapIDGL.size();
      }
      if (IDGL_EMISOR != null)
      {
          tamaño += 6/*IDGL*/ + 1/*TTL*/; //bytes
          resultTPDU.N_IDS += 1;
      }

    }
    else
    {
      if (ID_SOCKET == null)
        throw new ParametroInvalidoExcepcion(mn,"Parámetro ID_SOCKET nulo.");

      resultTPDU.N_IDS ++;

      if (treeMapID_SOCKET != null)
      {
        tamaño += treeMapID_SOCKET.size()*6+6/* de ID_SOCKET*/;
        resultTPDU.N_IDS += treeMapID_SOCKET.size();

      }
      else
        tamaño += 6/* de ID_SOCKET*/;
    }

    resultTPDU.tamaño = tamaño;
    // Guardar los datos ...
    resultTPDU.SUBTIPO = SUBTIPO;
    resultTPDU.TTL = TTL;
    resultTPDU.METRICA= METRICA;
    resultTPDU.IP = (byte) (flagIP ? 1 : 0);
    resultTPDU.N_MAX_SOCKETS = N_MAX_SOCKETS;
    resultTPDU.N_SOCKETS = N_SOCKETS;
    resultTPDU.treeMapIDGL = treeMapIDGL;
    resultTPDU.treeMapID_SOCKET = treeMapID_SOCKET;
    resultTPDU.ID_SOCKET = ID_SOCKET;
    resultTPDU.IDGL_EMISOR = IDGL_EMISOR;

    return resultTPDU;
  }

  //==========================================================================
  /**
   * Construir el TPDU CGL, devuelve un buffer con el contenido del TPDUCGL,
   * según el formato especificado en el protocolo.
   * <b>Este buffer no debe de ser modificado.</B>
   * @return un buffer con el TPDUCGL.
   * @exception PTMFExcepcion Se lanza si ocurre algún error en la construcción
   * del TPDU
   * @exception ParametroInvalidoExcepcion lanzada si ocurre algún error en la
   * construcción del TPDU
   */
  Buffer construirTPDU() throws PTMFExcepcion,ParametroInvalidoExcepcion
  {
    final String  mn = "TPDUCGL.construirTPDU";
    int   offset = 0;
    Buffer bufferResult = null;

    // El tamaño del buffer a crear se calculó en el constructor.

    // Construir la cabecera común.
    bufferResult = construirCabeceraComun ((short)PTMF.TPDU_CGL,this.tamaño);

    //
    // Añadir la cabecera CGL al buffer.
    //
    if (bufferResult == null)
       throw new PTMFExcepcion(mn,"Buffer nulo!");

    // NÚMERO DE SECUENCIA
    TPDUCGL.NUMERO_SECUENCIA = (short) ((TPDUCGL.NUMERO_SECUENCIA +1) %  256);
    this.N_SECUENCIA = TPDUCGL.NUMERO_SECUENCIA ;

    try
    {
      offset = 14;
      //
      // 15º BYTE : VERSION (2 bits), TIPO (2 bits), SUBTIPO (4 bits).
      //
      bufferResult.addByte((byte)
        ( bufferResult.getByte(offset) |
        (this.SUBTIPO & 0x0F)) ,offset);
      //Log.debug(Log.TPDU_CGL,mn,"SUBTIPO: "+this.SUBTIPO);
      offset++;

      //
      // 16º BYTE: TTL (8 bits)
      //
      bufferResult.addByte((byte) (this.TTL & 0xFF), offset);
      //Log.debug(Log.TPDU_CGL,mn,"TTL: "+this.TTL);
      offset++;

      //
      // 17º BYTE: Metrica (3 bits) e IP (1 bit)
      //
      bufferResult.addByte((byte)(((this.METRICA << 5) & 0xE0)
        | ((this.IP << 4) & 0x10)),offset);
      //Log.debug(Log.TPDU_CGL,mn,"METRICA: "+this.METRICA);
      //Log.debug(Log.TPDU_CGL,mn,"IP: "+this.IP);
      offset++;

      //
      // 18º BYTE: N_SECUENCIA
      //
      bufferResult.addByte((byte)(this.N_SECUENCIA & 0xff),offset);
      //Log.debug(Log.TPDU_CGL,mn,"N_SECUENCIA: "+this.N_SECUENCIA);
      offset++;


      //
      // 19º y 20º BYTE: N_IDs
      //
      bufferResult.addShort((this.N_IDS& 0xFF),offset);
      //Log.log("CGLLThread","Contruir --> N_IDS: "+this.N_IDS);
      offset+=2;

      //
      // 21º y 22º BYTE: N_MAX_SOCKETS
      //
      bufferResult.addShort((this.N_MAX_SOCKETS & 0xFF),offset);
      //Log.debug(Log.TPDU_CGL,mn,"N_MAX_SOCKETS: "+this.N_MAX_SOCKETS);
      offset+=2;

      //
      // 23º y 24º BYTES: N_SOCKETS
      //
      bufferResult.addShort((this.N_SOCKETS & 0xFF),offset);
      //Log.debug(Log.TPDU_CGL,mn,"N_SOCKETS: "+this.N_SOCKETS);
      offset+=2;


      //
      // Identificadores de grupos locales..
      //
      if ((this.IP == 0) && (this.N_IDS > 0) && (this.treeMapIDGL != null))
      {

        //Almacenar IDGL_EMISOR si el TPDU es GRUPO_LOCAL_VECINO¡¡¡
        if (this.SUBTIPO== PTMF.TPDU_CGL_GRUPO_LOCAL_VECINO
           && this.IDGL_EMISOR != null)
        {
          bufferResult.addBytes(this.IDGL_EMISOR.id,0,offset,6);
          offset += 6;
          bufferResult.addByte((byte)this.IDGL_EMISOR.TTL,offset);
          offset += 1;
          //Log.debug(Log.TPDU_CGL,mn,"IDGL_EMISOR: "+this.IDGL_EMISOR);
        }

        //
        // Identificadores grupos locales
        //
        Iterator iterator = this.treeMapIDGL.keySet().iterator();
        while(iterator.hasNext())
        {
         IDGL idgl = (IDGL) iterator.next();
         bufferResult.addBytes(idgl.id,0,offset,6);
         offset += 6;
         bufferResult.addByte((byte)idgl.TTL,offset);
         offset += 1;
         //Log.debug(Log.TPDU_CGL,mn,"ID_GRUPO_LOCAL "+i+": "+idgl);
        }

      }
      else if(IP==1 && this.N_IDS > 0)
      {
        //
        // ID_SOCKET
        //

        //Primer ID_SOCKET
        bufferResult.addBytes(this.ID_SOCKET.getDireccion().ipv4,0,offset,4);
        offset += 4;
        bufferResult.addShort(this.ID_SOCKET.getPuertoUnicast(),offset);
        offset += 2;

        if (this.N_IDS > 1 && this.treeMapID_SOCKET != null)
        {

        //Log.debug(Log.TPDU_CGL,mn,"ID_SOCKET: "+this.ID_SOCKET);

          Iterator iterator = this.treeMapID_SOCKET.keySet().iterator();
          //Log.log("CGLThread","Treemap ID_SOCKET: "+this.treeMapID_SOCKET);
          while( iterator.hasNext())
          {

           ID_Socket id = (ID_Socket) iterator.next();
           bufferResult.addBytes(id.getDireccion().ipv4,0,offset,4);
           offset += 4;
           bufferResult.addShort(id.getPuertoUnicast(),offset);
           offset += 2;
           //Log.log("CGLThread","ID_SOCKET: "+id);
          }
       }
     }

    }
    catch(ParametroInvalidoExcepcion e)
    {
     throw new PTMFExcepcion(mn,e.getMessage());
    }

    // Retornar el buffer
    return bufferResult;
  }

  //==========================================================================
  /**
   * Parse un Buffer de datos recibidos y crea un TPDU CGL que lo encapsule.
   * El buffer debe de contener un TPDU CGL.
   * @param buf Un buffer que contiene el TPDU CGL recibido.
   * @return Un objeto TPDUCGL
   * @exception PTMFExcepcion El buffer pasado no contiene una cabecera TPDU
   *  correcta, el mensaje de la excepción especifica el tipo de error.
   * @exception ParametroInvalidoExcepcion Se lanza si buffer no contiene un
   *  TPDUCGL válido.
   */
  static TPDUCGL parseBuffer(Buffer buf)
    throws PTMFExcepcion, ParametroInvalidoExcepcion
  {
    final String  mn = "TPDUCGL.ParseBuffer";
    int offset = 0;
    TPDUCGL tpduCGL  = null;
    boolean bGLVecino = false;

    //
    // NUEVO TPDUCGL ..
    //
    tpduCGL  = new TPDUCGL();

    //
    // Parser cabecera común..
    //
    TPDU.parseCabeceraComun(buf,(TPDU)tpduCGL);

    //
    // 15º BYTE :  SUBTIPO (4 bits).
    //
    offset = 14;
    tpduCGL.SUBTIPO = (byte)(buf.getByte(offset) & 0x0F) ;
    //Log.debug(Log.TPDU_CGL,mn,"SUBTIPO: "+tpduCGL.SUBTIPO);
    offset++;

    switch (tpduCGL.TIPO)
    {
       case PTMF.TPDU_CGL:
        break;
       default:
         throw new PTMFExcepcion(mn, "Cabecera TPDU CGL no válida. El campo TIPO debe de ser 00, encontrado valor "+(tpduCGL.TIPO & 0x2 )+(tpduCGL.TIPO & 0x1));
    }

    switch (tpduCGL.SUBTIPO)
    {

       case PTMF.TPDU_CGL_BUSCAR_GRUPO_LOCAL:
       case PTMF.TPDU_CGL_GRUPO_LOCAL:
       case PTMF.TPDU_CGL_UNIRSE_A_GRUPO_LOCAL:
       case PTMF.TPDU_CGL_SOCKET_ACEPTADO_EN_GRUPO_LOCAL:
       case PTMF.TPDU_CGL_DEJAR_GRUPO_LOCAL:
       case PTMF.TPDU_CGL_ELIMINACION_GRUPO_LOCAL:
       case PTMF.TPDU_CGL_BUSCAR_GRUPO_LOCAL_VECINO:
       case PTMF.TPDU_CGL_BUSCAR_GL_PARA_EMISOR:
       case PTMF.TPDU_CGL_GL_PARA_EMISOR:      break;
       case PTMF.TPDU_CGL_GRUPO_LOCAL_VECINO: bGLVecino=true;  break;
       default:
         throw new PTMFExcepcion(mn, "Cabecera TPDU CGL no válida. El campo SUBTIPO no es válido, encontrado valor "
          +(tpduCGL.SUBTIPO & 0x8 )+(tpduCGL.SUBTIPO & 0x4)+(tpduCGL.SUBTIPO & 0x2 )+(tpduCGL.SUBTIPO & 0x1));
    }

    //
    // 16º BYTE:  TTL (8 bits)
    //
    tpduCGL.TTL = (byte)(buf.getByte(offset) & 0xFF) ;
    tpduCGL.ID_GRUPO_LOCAL.TTL = tpduCGL.TTL;
    //Log.debug(Log.TPDU_CGL,mn,"TTL: "+tpduCGL.TTL);
    offset++;

    //
    // 17º BYTE: Metrica(3 bits) e IP (1 bit)
    //
    tpduCGL.METRICA = (short) ((buf.getByte(offset) & 0xE0) >>> 5);
    tpduCGL.IP = (short) ((buf.getByte(offset) & 0x10) >>> 4);
    //Log.debug(Log.TPDU_CGL,mn,"METRICA: "+tpduCGL.METRICA);
    //Log.debug(Log.TPDU_CGL,mn,"IP: "+tpduCGL.IP);
    offset++;

    //
    // 18º BYTE: N_SECUENCIA.
    //
    tpduCGL.N_SECUENCIA = (short)(buf.getByte(offset) & 0xFF) ;
    //Log.debug(Log.TPDU_CGL,mn,"N_SECUENCIA: "+(int)tpduCGL.N_SECUENCIA);
    offset++;

    //
    // 19º BYTE y 20º Byte: N_IDs
    //
    tpduCGL.N_IDS = (int)buf.getShort(offset);
    //Log.log("CGLThread","Parse-> N_IDS: "+tpduCGL.N_IDS);
    offset+=2;

    //
    // 21º y 22º BYTES: N_MAX_SOCKETS
    //
    tpduCGL.N_MAX_SOCKETS = (int) buf.getShort(offset);
    //Log.debug(Log.TPDU_CGL,mn,"N_MAX_SOCKETS: "+tpduCGL.N_MAX_SOCKETS);
    offset+=2;

    //
    // 23º y 24º BYTES: N_SOCKETS
    //
    tpduCGL.N_SOCKETS = (int) buf.getShort(offset);
    //Log.debug(Log.TPDU_CGL,mn,"N_SOCKETS: "+tpduCGL.N_SOCKETS);
    offset+=2;



    //
    // Identificadores de grupos locales y TTLs...
    //
    if ((tpduCGL.N_IDS > 0) && (tpduCGL.IP==0))
    {
      if(tpduCGL.treeMapIDGL == null)
        tpduCGL.treeMapIDGL = new TreeMap();
      else
        tpduCGL.treeMapIDGL.clear();

      //Si el TPDU es GRUPO_LOCAL_VECINO, obtener el IDGL_EMISOR
      if(bGLVecino)
      {
        tpduCGL.IDGL_EMISOR = new IDGL(new Buffer(buf.getBytes(offset, 6)),(byte)0);
        tpduCGL.IDGL_EMISOR.TTL = buf.getByte(offset+6);
      }

      for (int i = 0; i < tpduCGL.N_IDS; i++) {
        IDGL idgl = new IDGL(new Buffer(buf.getBytes(offset, 6)),(byte)0);
        offset += 6;
        idgl.TTL = buf.getByte(offset);
        tpduCGL.treeMapIDGL.put(idgl,null);
        //Log.debug(Log.TPDU_CGL,mn,"ID_GRUPO_LOCAL "+(i+1)+": "+idgl.toString());
        offset += 1;
      }
    }

    //
    // .. ó ID_SOCKETS..
    //
    else if (tpduCGL.IP==1 && tpduCGL.N_IDS > 0)
    {
      //Primer ID_SOCKET aparte..
      tpduCGL.ID_SOCKET = new ID_Socket(new IPv4(new Buffer(buf.getBytes(offset, 4))),buf.getShort(offset+4));
     // Log.log("CGLThread","ID_SOCKET: "+tpduCGL.ID_SOCKET);
      offset += 6;

      if (tpduCGL.N_IDS > 1)
      {
        if(tpduCGL.treeMapID_SOCKET == null)
        tpduCGL.treeMapID_SOCKET = new TreeMap();
       else
        tpduCGL.treeMapID_SOCKET.clear();


       for (int i = 1; i < tpduCGL.N_IDS; i++)
       {
        ID_Socket id = new ID_Socket( new IPv4(new Buffer(buf.getBytes(offset, 4))),buf.getShort(offset+4) );
        tpduCGL.treeMapID_SOCKET.put(id,null);
        //Log.log("CGLThread","ID_SOCKET "+id);
        offset += 6;
       }

       //Log.log("TPDUCGL <--- parse ID_soKCets: "+tpduCGL.treeMapID_SOCKET,"");
      }
    }
    //
    // LLamar a parseBuffer de la clase base, pasándole el offset computado.
    //
    //tpduCGL.parseBuffer(buf, offset);

    return tpduCGL;
  }

  //==========================================================================
  /**
   * Método toString()
   * @return cadena identificativa
   */
  public String toString()
  {
   return (this.toString());
  }

  //==========================================================================
  /**
   * El buffer pasado por argumento contiene un TPDUCGL recibido de la red.
   * Esta función extrae valor del campo número de secuencia.
   * @param buf buffer que contiene un TPDUCGL.
   * @return Número de secuencia
   * @exception PTMFExcepcion lanzada si hubo un error al leer el buffer
   */
  static int getN_Secuencia(Buffer buf) throws PTMFExcepcion
  {
    final String mn = "TPDUCGL.getN_Secuencia";
    int N_SECUENCIA = 0;

    //
    // Asegurarse que se ha recibido algún dato,
    // almenos los bytes de la cabecera del TPDU.
    //
//    Log.log("DEPU 2","tamaño del buffer: "+buf.getLength());
    if (buf.getLength() < TPDUCGL.LONGHEADER)
      throw new PTMFExcepcion(mn, "La cabecera del TPDU (paquete) recibido no se ha recibido entera. No se puede procesar el TPDU.");

    try
    {
      N_SECUENCIA = buf.getByte(17);
    }
    catch(ParametroInvalidoExcepcion e)
    {
      throw new PTMFExcepcion(mn,e.getMessage());
    }

    return N_SECUENCIA;
  }

  //==========================================================================
  /**
   * Obtiene el campo SUBTIPO
   */
  byte getSUBTIPO(){  return SUBTIPO;  }

  //==========================================================================
  /**
   * Obtiene el campo TTL
   */
  byte getTTL() {  return TTL;}

  //==========================================================================
  /**
   * Obtiene el campo METRICA
   */
  short getMETRICA() { return METRICA;}

  //==========================================================================
  /**
   * Obtiene el campo FlagIP
   */
  boolean getFlagIP() { return ((IP==1) ? true: false);}

  //==========================================================================
  /**
   * Obtiene el campo N_SECUENCIA
   */
  short getN_SECUENCIA() { return N_SECUENCIA;}

  //==========================================================================
  /**
   * Obtiene el campo N_IDS
   */
  int getN_IDS() { return N_IDS;}

  //==========================================================================
  /**
   * Obtiene el campo N_MAX_SOCKETS
   */
  int getN_MAX_SOCKETS() {return N_MAX_SOCKETS;}

  //==========================================================================
  /**
   * Obtiene el campo N_SOCKETS
   */
  int getN_SOCKETS() { return N_SOCKETS;}

  //==========================================================================
  /**
   * Obtiene el campo IDGL_EMISOR
   */
  IDGL getIDGL_EMISOR() { return IDGL_EMISOR;}

  //==========================================================================
  /**
   * Obtiene el TreeMap IDGL
   */
  TreeMap getTreeMapIDGL() { return treeMapIDGL;}

  //==========================================================================
  /**
   * Obtiene el TreeMap ID_Socket
   */
  TreeMap getTreeMapID_SOCKET() {return treeMapID_SOCKET;}

  //==========================================================================
  /**
   * Obtiene el campo ID_Socket
   */
  ID_Socket getID_SOCKET(){ return ID_SOCKET;}
}
