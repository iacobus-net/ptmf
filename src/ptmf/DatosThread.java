//============================================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: DatosThread.java  1.0 13/10/99
//
//	Autores: Antonio Berrocal Piris (AntonioBP@wanadoo.es)
//               M. Alejandro Garc�a Dom�nguez (Malejandro.garcia.dominguez@gmail.com)
//
//	Descripci�n: Procesa y env�a todos los TPDU de datos (TPDUDatosNormal,
//                   TPDUDatosRtx, TPDUMACK, TPDUACK, TPDUHACK, TPDUHSACK,
//                   TPDUNACK y TPDUHNACK).
//                   Implementa los modos:
//                   <ul>
//                       <li>{@link PTMF#PTMF_FIABLE}</li>
//                       <li>{@link PTMF#PTMF_FIABLE_RETRASADO}</li>
//                       <li>{@link PTMF#PTMF_NO_FIABLE}</li>
//                       <li>{@link PTMF#PTMF_NO_FIABLE_ORDENADO}</li>
//                    </ul>
//                   Basada en la especificaci�n del protocolo PTMF.
//
// 	Authors: 
//		 Alejandro Garc�a-Dom�nguez (alejandro.garcia.dominguez@gmail.com)
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
//
//----------------------------------------------------------------------------

package ptmf;

import java.util.TreeMap;
import java.util.SortedMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.Random;
import java.io.IOException;
import java.net.UnknownHostException;
import java.lang.Integer;
import java.lang.Math;

/**
  * Procesa y env�a los TPDU de datos: TPDUDatosNormal, TPDUDatosRtx, TPDUMACK,
  * TPDUACK, TPDUHACK, TPDUHSACK, TPDUNACK y TPDUHNACK.<BR>
  * Implementa los modos:
  *   <ul>
  *      <li>{@link PTMF#PTMF_FIABLE}</li>
  *      <li>{@link PTMF#PTMF_FIABLE_RETRASADO}</li>
  *      <li>{@link PTMF#PTMF_NO_FIABLE}</li>
  *      <li>{@link PTMF#PTMF_NO_FIABLE_ORDENADO}</li>
  *   </ul>
  * Basada en la especificaci�n del protocolo PTMF.
  * @see SocketPTMF
  * @see PTMF
  * @version  1.0
  * @author Antonio Berrocal Piris
  * <A HREF="mailto:AntonioBP@wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
  * M. Alejandro Garc�a Dom�nguez
  * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 */
class DatosThread  extends Thread implements TimerHandler,ID_SocketListener,IDGLListener
{
  /**
   * Gestiona los {@link TPDUNACK} y {@link TPDUHNACK} que se han enviado o se
   * tienen que enviar.
   */
  private GestionAsentNeg gestionAsentNeg = null;

  /**
   * Almacena la informaci�n sobre los Asentimientos positivos (ACK o HACK) que
   * est�n pendientes de envio.
   * <table border=1>
   *  <tr>  <td><b>Key:</b></td>
   *	    <td>{@link ID_TPDU} fuente que se tiene que asentir.</td>
   *  </tr>
   *  <tr>  <td><b>Value:</b></td>
   *	    <td>N�mero de r�faga como un objeto Integer.</td>
   *  </tr>
   * </table>
   * @see TPDUACK
   * @see TPDUHACK
   */
  private ListaOrdID_TPDU listaAsentPositPendientesDeEnviar = null;


  /**
   * Almacena la informaci�n sobre los HSACK que est�n pendientes de envio.
   * <table border=1>
   *  <tr>  <td><b>Key:</b></td>
   *	    <td>{@link ID_TPDU} fuente que se tiene que asentir.</td>
   *  </tr>
   *  <tr>  <td><b>Value:</b></td>
   *	    <td>N�mero de r�faga como un objeto Integer.</td>
   *  </tr>
   * </table>
   * @see RegistroHSACK
   * @see TPDUHSACK
   */
  private ListaOrdID_TPDU listaHSACKPendientesDeEnviar = null;


  /**
   * Almacena instancias de {@link RegistroVectorMACK} con informaci�n de los MACK que
   * est�n pendientes de env�o.
   * @see TPDUMACK
   */
  private Vector vectorMACK = null;

  /**
   * Almacena la informaci�n de los TPDU Datos  que han sido solicitados para
   * retransmisi�n multicast.
   * <table border=1>
   *  <tr>  <td><b>Key:</b></td>
   *	    <td>{@link ID_TPDU} fuente solicitado para retransmitir.</td>
   *  </tr>
   *  <tr>  <td><b>Value:</b></td>
   *	    <td>Un integer que indica si ha sido pedido por hijo o vecino o ambos.</td>
   *  </tr>
   * </table>
   * @see #actualizarListaID_TPDUSolicitadoRtx
   */
  private ListaOrdID_TPDU listaID_TPDUSolicitadosRtxMulticast = null;


  /**
   * Almacena la informaci�n de los TPDU Datos  que han sido solicitados para
   * retransmisi�n unicast.
   * <table border=1>
   *  <tr>  <td><b>Key:</b></td>
   *	    <td>{@link ID_TPDU} fuente solicitado para retransmitir.</td>
   *  </tr>
   *  <tr>  <td><b>Value:</b></td>
   *	    <td>TreeMap con los {@link ID_Socket} que han solicitado la retransmisi�n
   * unicast:
   *            <table border=1>
   *              <tr>  <td><b>Key:</b></td>
   *	                <td>ID_Socket</td>
   *              </tr>
   *              <tr>  <td><b>Value:</b></td>
   *	                <td>NULL</td>
   *              </tr>
   *            </table>
   *           </td>
   *  </tr>
   * </table>
   * @see #actualizarListaID_TPDUSolicitadoRtx
   */
  private ListaOrdID_TPDU listaID_TPDUSolicitadosRtxUnicast = null;


  /**
   * Almacena el {@link ID_Socket} fuente y el {@link NumeroSecuencia n�mero de secuencia}
   * de los {@link TPDUDatosNormal TPDU Datos Normal} o {@link TPDUDatosRtx Rtx}.
   * recibidos o enviados multicast (han pasado por la red y los han visto todos
   * los hosts pertenecientes al Grupo Local) durante la 2� mitad de RTT. <br>
   * <table border=1>
   *  <tr>  <td><b>Key:</b></td>
   *	    <td>{@link ID_Socket} de la fuente del TPDU.</td>
   *  </tr>
   *  <tr>  <td><b>Value:</b></td>
   *	    <td>N�mero de secuencia mayor recibido o enviado multicast.</td>
   *  </tr>
   * </table>
   */
  private TreeMap treeMapIDs_TPDU2MitadRTT = null;

  /** Identificaci�n de este socket */
  private ID_Socket id_SocketLocal = null;

  /**
   * Referencia al socketPTMF que crea este hilo.
   */
  private SocketPTMFImp socketPTMFImp = null;


  /**
   * Almacena informaci�n de los sockets que env�an {@link TPDUDatosNormal},
   * es decir, de los sockets que son fuente de datos.
   */
  private TablaID_SocketsEmisores tablaID_SocketsEmisores = null;


  /**
   * Almacena informaci�n de los ID_TPDU para los cuales este socket tiene que
   * recibir un asentimiento positivo.<br>
   * Es decir, almacena los ID_TPDU de los TPDU de datos que tiene el bit ACK
   * activado y para los que este socket es su CG local.
   */
  private TablaAsentimientos tablaAsentimientos = null;

  /**
   * Almacena toda la informaci�n relativa a los CG locales.
   */
  private TablaCGLocales tablaCGLocales = null;

  /**
   * Contiene los {@link ID_TPDU} que han agotado las oportunidades m�ximas de espera
   * sin que se hayan recibido todos los asentimientos positivos pendientes para
   * dicho ID_TPDU.
   * <table border=1>
   *  <tr>  <td><b>Key:</b></td>
   *	    <td>{@link ID_TPDU}.</td>
   *  </tr>
   *  <tr>  <td><b>Value:</b></td>
   *	    <td>NULL</td>
   *  </tr>
   * </table>
   */
  private ListaOrdID_TPDU listaIDs_TPDUNoRecibidoAsentimiento = null;

  /**
   *  Mutex para sincronizar el acceso a listaIDs_TPDUNoRecibidoAsentimiento.a
   */
  private Mutex mutex = null;

  /**
   * Almacena los id_socket que se han a�adido y de los que no se tiene constancia de
   * que est�n sincronizados con todos los emisores fuentes. S�lo es utlizado en el modo
   * de fiabilidad retrasada.
   * La clave es id_socket y el valor un treeMap cuya clave es un emisor fuente
   * (id_socket), y su valor un Boolean, que indica si para dicho emisor ya se
   * ha RTX alg�n TPDU en el que figure el id_socket que se a�adio. Si el valor
   * booleano es true indica que el socket ya est� sincronizado para dicho emisor.
   * Si es false que ya se ha rtx alg�n tpdu de dicho emisor en el que figura el socket en
   * su lista de no asentidos.
   */
  private TreeMap treeMapID_SocketNoSincronizado = null;

  /**
   * Almacena los idgl que se han a�adido y de los que no se tiene constancia de
   * que est�n sincronizados con todos los emisores fuentes. S�lo es utlizado en el modo
   * fiabilidad retrasada.
   * La clave es idgl y el valor un treeMap cuya clave es un emisor fuente (id_socket),
   * y su valor un Boolean, que indica si para dicho emisor ya se ha RTX alg�n
   * TPDU en el que figure el idgl que se a�adio. Si el valor booleano es
   * true indica que el idgl ya est� sincronizado para dicho emisor. Si es false
   * que ya se ha rtx alg�n tpdu de dicho emisor en el que figura el idgl en
   * su lista de no asentidos.
   */
  private TreeMap treeMapIDGLNoSincronizado = null;

  /**
   * Informaci�n de los <b>nuevos sockets emisores </b>que surgan durante la comunicaci�n.
   * Es utilizado s�lo en el modo fiable retrasado para asegurar la sincronizaci�n
   * de los hijos y vecinos existentes.
   * Indexado por el id_socket del emisor fuente al que se asocia el id_tpdu inicial
   * de dicho emisor (al menos para este socket). Se eliminar�n del treemap cuando
   * todos los hijos y vecinos hayan semiasentido, al menos, el id_tpdu asociado.
   */
  private TreeMap treeMapNuevosEmisoresASincronizar = null;

  /**
   * Almacena los id_socket emisores que tienen pendientes datos para entrega
   * al usuario y no se han entregado por estar la cola de recepci�n llena.
   */
  TreeMap treeMapID_SocketEntregarUsuario = null;

  /** Ventana de emisi�n con los TPDUDatosNormal que tienen que ser enviados. <br>
      S�lo utilizado si este socket es fuente de datos.*/
  VentanaEmision  ventanaEmision = null;

  /** N�mero de secuencia inicial de la ventana de emisi�n. <br>
      S�lo utilizado si este socket es fuente de datos.*/
  private long N_SECUENCIA_INICIAL = 1;

  /** N�mero de secuencia del siguiente TPDU datos a a�adir a la ventana de emisi�n. <br>
      S�lo utilizado si este socket es fuente de datos.*/
  private NumeroSecuencia NSecuencia = null;

  /**
   * N�mero de R�faga del siguiente TPDU datos a a�adir a la ventana de emisi�n.
   * Comienza numeraci�n en 1.<br>
   *  S�lo utilizado si este socket es fuente de datos.
   */
  private int iNRafaga = 1;

  /** Se ha enviado un TPDU de datos con el bit fin emisi�n a true.<br>
      S�lo utilizado si este socket es fuente de datos. */
  boolean bYaEnviadoFinEmision = false;

  /** N�mero de TPDUs m�ximos que puede contener una r�faga.<br>
      S�lo utilizado si este socket es fuente de datos. */
  private int iTPDUsRafaga = 0;

  /** N�mero de TPDUs consecutivos a�adidos a la vta. emisi�n sin el bit ACK a 1.<br>
      S�lo utilizado si este socket es fuente de datos.*/
  private int iTPDUsConsecutivosSinACK = 0;

  /** N�mero de TPDU que se pueden enviar entre dos TPDU's con el bit ACK activado.
      C�mo m�ximo puede ser el tama�o de la ventana de emisi�n menos 1, para
      asegurar que siempre hay un TPDU de datos con el bit ACK activado en la
      ventana de emisi�n.<br>
      S�lo utilizado si este socket es fuente de datos. */
  private int iACKCadaXTPDUS = PTMF.ACKS_CADA_X_TPDUS;

  /** Boolean de inicio de r�faga.<br>
      S�lo utilizado si este socket es fuente de datos. */
  private boolean bSetIR = true;

  /** Boolean de asentimiento de TPDU.<br>
      S�lo utilizado si este socket es fuente de datos. */
  private boolean bSetACK = false;

  /** Boolean de fin de conexi�n.<br>
      S�lo utilizado si este socket es fuente de datos. */
  private boolean bSetFIN_CONEXION = false;

  /** Boolean para time out por inactividad. Si vale true hay que enviar
      un TPDU de datos para evitar que el resto de miembros crean que hemos
      dejado de emitir.<br>
      S�lo utilizado si este socket es fuente de datos.
      */
  private boolean bTime_out_inactividad = false;

  /** Boolean para time_out �ltimo TPDU de datos enviado sin el bit ACK activado.
      Si vale true hay que enviar un TPDU de datos con el bit ACK activado. <br>
      S�lo utilizado si este socket es fuente de datos.*/
  private boolean bTime_out_ultimoTPDUSinACK = false;

  /** N�mero de bytes de usuario enviados. S�lo cuenta los bytes enviados en
      los TPDU de datos normal enviados.<br>
      S�lo utilizado si este socket es fuente de datos. */
  private long lNBytesUsuarioEnviados = 0;

  /** Ratio que no tiene que ser superado para el env�o de nuevos datos de
      usuario. <br>
      S�lo utilizado si este socket es fuente de datos.*/
  private long lRatioUsuario_Bytes_x_seg = PTMF.RATIO_USUARIO_BYTES_X_SEG;

  /** Indica que este socket no va a enviar m�s datos al grupo multicast. */
  private boolean bFIN_EMISION = false;

  /** Indica si se tiene que comprobar la entrega de datos al usuario. Ser�
      verdadera cuando se haya intentado a�adir a la cola de recepci�n sin �xito
      (est� llena).*/
  private boolean bComprobarEntregaDatosUsuario = false;

  /** Boolean que indica si este socket va a dejar la comunicaci�n. */
  private boolean bABANDONAR_COMUNICACION = false;

  /** Si vale true, el socket abandona la comunicaci�n de forma estable,
      y si vale false, de forma inmediata.*/
  private boolean bORDENADO = false;

  /**
   * Para cada socket fuente de datos almacena el �ltimo n�mero de secuencia
   * entregado al usuario.
   * <br>S�lo usado en el modo PTMF_NO_FIABLE_ORDENADO.
   * <table border=1>
   *  <tr>  <td><b>Key:</b></td>
   *	    <td>{@link ID_Socket} fuente.</td>
   *  </tr>
   *  <tr>  <td><b>Value:</b></td>
   *	    <td>N�mero de secuencia �ltimo entregado al usuario.</td>
   *  </tr>
   * </table>
   */
  private TreeMap treeMapNSecUltimoEntregado = null;

  // M�SCARAS para activar varias posibles opciones con un s�lo par�metro.
  /**  */
  private static final int iSIN_OPCION                         = 0x00000000;
  /** Indica que un id_tpdu ha sido solicitado en un HNACK recibido por unicast. */
  private static final int iPEDIDO_POR_UNICAST                 = 0x00000001;
  /** Indica que un id_tpdu ha sido solicitado en un NACK o HNACK enviado por un
      miembro del grupo local */
  private static final int iPEDIDO_POR_VECINO                  = 0x00000002;
  /** Indica que un id_tpdu ha sido solicitado en un NACK o HNACK enviado por un
      miembro de un grupo local hijo jer�rquico. */
  private static final int iPEDIDO_POR_HIJO                    = 0x00000004;
  /** Indica que se tiene que rtx si no est� sincronizado. */
  private static final int iRTX_SI_NO_SINCRONIZADO             = 0x00000008;
  /** Indica que se tiene que rtx si no est� sincronizado y es la primera rtx.
      realizada para el id_socket fuente no sincronizado. */
  private static final int iRTX_SI_NO_SINCRONIZADO_Y_ES_1�_RTX = 0x00000010;
  /** Indica que se rtx. s�lo a los hijos. */
  private static final int iRTX_SOLO_A_HIJOS                   = 0x00000020;
  /** Indica que se rtx. s�lo a los vecinos */
  private static final int iRTX_SOLO_A_VECINOS                 = 0x00000040;
  /** Indica que se rtx. si contiene identificadores de id_socket o IDGLS que
      no est�n sincronizados. */
  private static final int iRTX_SI_CONTIENE_ID_NO_ASENTIDOS    = 0x00000080;


  //-----------------------------------------------------------------------
  // VARIABLES RELACIONADAS CON EL TIEMPO..
  //-----------------------------------------------------------------------
  // Cuando estas variables valen cero indica que no se tiene que esperar o
  // que ha finalizado el tiempo de espera.

  /** Generador de n�meros aleatorios. */
  private Random random  = null;

  /**
   * Tiempo m�nimo de espera entre envios consecutivos de cualquier TPDU (Normal,
   * Rtx, ACK, HACK, ...). Cuando es igual a cero indica que la espera ha finalizado.
   */
  private long lT_Min_Entre_Envios = 0;

  /**
   * Tiempo m�nimo de espera entre env�os de nuevos datos de usuario al grupo
   * multicast (milisegundos).
   */
  private long lT_Out_Ajustar_Ratio_Usuario = 0;

  /** Tiempo m�ximo de inactividad de este socket.<br>
      S�lo utilizado si este socket es fuente de datos.*/
  private long lT_TIME_OUT_INACTIVIDAD = (PTMF.TIEMPO_MAX_INACTIVIDAD_EMISOR * 2) / 3  ;

  /**
   * Tiempo aleatorio de espera antes de enviar un {@link TPDUMACK}.
   * Cuando es igual a cero indica que la espera ha finalizado. */
  private long lTRandomMACK = 0;

  /** Indica el modo en el que este socket est� actuando, por defecto fiable.*/
  private int iMODO_FIABILIDAD = PTMF.PTMF_FIABLE;

  /**
   * Indica si ha finalizado, al menos una vez, RTT. <br>
   * No asegura que s�lo haya finalizado una vez.
   */
  private boolean bFinRTT = false;

  /**
   * Si es true se tiene que comprobar el tiempo que lleva cada emisor sin
   * haber enviado alg�n tpdu.
   */
  private boolean bComprobarInactividadEmisores;

  //-----------------------------------------------------------------------
  // Eventos que se registran en el primer argumento de la funci�n Callback
  // y que son disparados por el Temporizador.
  //-----------------------------------------------------------------------
  /** Evento disparado en cada vencimiento de RTT. */
  private static final int iEVENTO_RTT = 1;

  /** Evento de consumido el n�mero de RTT m�ximo para un ID_TPDU.*/
  private static final int iEVENTO_FIN_INTENTOS = 2;

  /** Evento de finalizaci�n del tiempo aleatorio para poder enviar un MACK */
  private static final int iEVENTO_FIN_T_RANDOM_MACK = 3;

  /** Evento de finalizaci�n del tiempo m�nimo entre envios */
  private static final int iEVENTO_T_MIN_ENTRE_ENVIOS = 4;

  /** Evento de finalizaci�n del tiempo de espera para comprobar inactividad */
  private static final int iEVENTO_COMPROBAR_INACTIVIDAD_EMISORES = 5;

  /** Evento Time Out utilizado para enviar TPDUs vac�os cada T_TIME_OUT seg*/
  private static final int iEVENTO_TIME_OUT_INACTIVIDAD = 6;

  /** Evento Time Out utilizado para enviar TPDUs vac�os cada T_TIME_OUT seg*/
  private static final int iEVENTO_TIME_OUT_ULTIMO_TPDU_SIN_ACK = 7;

  /** Evento de consumido el tiempo de espera para que un nuevo SOCKET
      est� sincronizado. (S�lo utilizada en modo fiable retrasado.)
      Al registrar en el temporizador se registrar� tambi�n el id_socket
      al cual se le ha acabado el tiempo para sincronizaci�n.
      */
  private static final int iEVENTO_FIN_SINCRONIZACION_ID_SOCKET = 8;

  /** Evento de consumido el tiempo de espera para que un nuevo IDGL
      est� sincronizado. (S�lo utilizada en modo fiable retrasado.)
      Al registrar en el temporizador se registrar� tambi�n el idgl
      al cual se le ha acabado el tiempo para sincronizaci�n.
      */
  private static final int iEVENTO_FIN_SINCRONIZACION_IDGL = 9;

  /** Evento de consumido el tiempo de espera para ajustar el ratio m�ximo
      al que pueden ser enviados los datos. S�lo utilizada en el caso de que
      este socket sea fuente de datos.
   */
  private static final int iEVENTO_T_OUT_AJUSTAR_RATIO_USUARIO = 10;

  /**
   * Cuenta el n�mero de veces que la funci�n enviarTPDUFiable ha ejecutado
   * la funci�n enviarDatosSolicitadosRtx(). Se ejecuta una vez enviarDatosSolicitadoRtx ()
   * por cada cinco ejecuciones de enviarTPDUFiable().
   */
  private int iContadorIteracionEnviarTPDUFiable = 0;

  /** Cuenta de inactividad*/
  private int iInactividadEmisor  = 0;

  /** Cuenta de inactividad*/
  private int iInactividadReceptor  = 0;

  /** Vector con los id_sockets e idgls a�adidos.
      CGL almacena los ID_Socket y los IDGL que se incorporan a la comunicaci�n.
      El thread CGLThread a�ade los identificadores y el thread DatosThread los
      elimina. Al vector se puede acceder de forma concurrente.*/
  private Vector vectorIDA�adidos = null;

  /** Vector con los id_sockets e idgls eliminados
      CGL almacena los ID_Socket y los IDGL que abandonan la comunicaci�n
      El thread CGLThread a�ade los identificadores y el thread DatosThread los
      elimina. Al vector se puede acceder de forma concurrente.*/
  private Vector vectorIDEliminados = null;



  //==========================================================================
  /**
   * Inicializa todas las variables utilizadas.
   * @param socketPTMFImpparam referencia al socketPTMFImp que crea este thread.
   * @param modo_fiabilidad indica el modo en que est� actuando este socket,
   * puede ser:
   * <ul>
   *    <li>{@link PTMF#PTMF_FIABLE}</li>
        <li>{@link PTMF#PTMF_FIABLE_RETRASADO}</li>
   *    <li>{@link PTMF#PTMF_NO_FIABLE}</li>
   *    <li>{@link PTMF#PTMF_NO_FIABLE_ORDENADO}</li>
   * </ul>
   * Si no es niguno de los anteriores el socket act�a en modo NO FIABLE.
   * @see PTMF
   */
  public DatosThread (SocketPTMFImp socketPTMFImpparam,int iModo_fiabilidad)
  {
    super("DatosThread");
    //PRIORIDAD..
    this.setPriority(Thread.NORM_PRIORITY + 2);
    this.setDaemon (true);

    final String mn = "DatosThread.DatosThread (SocketPTMF)";

    this.iMODO_FIABILIDAD = iModo_fiabilidad;
    this.socketPTMFImp = socketPTMFImpparam;
    this.random = new Random (System.currentTimeMillis ());
    this.id_SocketLocal = this.socketPTMFImp.getAddressLocal().toID_Socket();

    if ((iModo_fiabilidad == PTMF.PTMF_FIABLE) || (iModo_fiabilidad == PTMF.PTMF_FIABLE_RETRASADO))
      {
       // Crear todas las estructuras de datos utilizadas.
       this.bComprobarInactividadEmisores = true;
       this.treeMapID_SocketEntregarUsuario = new TreeMap();
       this.tablaID_SocketsEmisores = new TablaID_SocketsEmisores ();
       this.gestionAsentNeg = new GestionAsentNeg (this.socketPTMFImp);
       this.treeMapIDs_TPDU2MitadRTT = new TreeMap ();
       this.tablaCGLocales = new TablaCGLocales ();
       try
        {
        this.tablaAsentimientos = new TablaAsentimientos (this);
        }catch (ParametroInvalidoExcepcion e)
           {
            // ALEX:
            this.socketPTMFImp.sendPTMFEventError ("No se ha podido crear las estructuras de datos necesarias.");
            Log.exit (-1);
           }

       this.listaAsentPositPendientesDeEnviar = new ListaOrdID_TPDU ();
       this.listaHSACKPendientesDeEnviar = new ListaOrdID_TPDU ();
       this.listaID_TPDUSolicitadosRtxMulticast = new ListaOrdID_TPDU ();
       this.listaID_TPDUSolicitadosRtxUnicast = new ListaOrdID_TPDU ();
       this.listaIDs_TPDUNoRecibidoAsentimiento = new ListaOrdID_TPDU ();
       this.mutex = new Mutex();
       this.vectorMACK = new Vector ();
       this.vectorIDA�adidos = new Vector ();
       this.vectorIDEliminados = new Vector ();

       // Registrar funci�n para llamar cada fin de RTT
       this.socketPTMFImp.getTemporizador().registrarFuncionRTT (this,this.iEVENTO_RTT);

       // Registrar IDGLListener y ID_SocketListener en CGLThread
       this.socketPTMFImp.getCGLThread().addID_SocketListener(this);
       this.socketPTMFImp.getCGLThread().addIDGLListener (this);
      }

    if (this.iMODO_FIABILIDAD == PTMF.PTMF_NO_FIABLE_ORDENADO)
      this.treeMapNSecUltimoEntregado = new TreeMap();

    if (this.iMODO_FIABILIDAD == PTMF.PTMF_FIABLE_RETRASADO)
     {
      this.treeMapID_SocketNoSincronizado    = new TreeMap();
      this.treeMapIDGLNoSincronizado         = new TreeMap();
      this.treeMapNuevosEmisoresASincronizar = new TreeMap();
     }


  }

  //==========================================================================
  /**
   * Devuelve true si se ha llamado al close de SocketPTMF.
   * @see SocketPTMF
   */
  private boolean getAbandonarComunicacion()
  {
   return this.bABANDONAR_COMUNICACION;
  }

  //==========================================================================
  /**
   * M�todo run del thread. Lanzado cuando el socketPTMF es abierto.
   * Procesa los TPDU Datos recibidos y envia los TPDU Datos necesarios.
   * @see SocketPTMF
   */
  public void run()
  {
    final String    mn = "DatosThread.run()";
    boolean         bForever;
    Buffer          buf;
    TPDUDatos tpduDatos = null;
    int iContadorIteraciones;
    int iContadorIteracionesRun = 0;


    bForever = true;

    //========================================================================
    // Bucle infinito de procesamiento de los TPDUs de datos tanto de emisi�n
    // como de recepci�n
    //========================================================================
    while (bForever)
    {
      //Incrementar iContadorIteracionesRun
      iContadorIteracionesRun++;



      //-------------------------------------------------------------------
      // 1. Comprobar si el thread tiene que finalizar.
      //-------------------------------------------------------------------
      if (this.finComunicacion())
      {
          Log.debug(Log.DATOS_THREAD,mn,"Fin del hilo DatosThread");
          return;
      }

      //-------------------------------------------------------------------
      // 2. ESPERAR HASTA QUE EL GRUPO MULTICAST EST� ACTIVO Y CON ELLO
      //   EL CGL THREAD Y TODAS LAS VARIABLES DE SOCKETPTMFIMP
      //-------------------------------------------------------------------
      if (!this.socketPTMFImp.isGrupoMcastActivo())
      {
        this.socketPTMFImp.getTemporizador().sleep(1);
        continue; // Esperar hasta que est� activo.
      }



      //-------------------------------------------------------------------
      // 3. Comprobar si hay que quitar alg�n emisor por no emitir.
      //-------------------------------------------------------------------
      if (this.bComprobarInactividadEmisores)
       {
        Iterator iteradorID_Socket =
           this.tablaID_SocketsEmisores.comprobarInactividad().keySet().iterator();

        // Eliminar todos los que hayan superado el tiempo m�x. de inactividad.
        ID_Socket id_socketNext;
        while (iteradorID_Socket.hasNext())
         {
          id_socketNext = (ID_Socket)iteradorID_Socket.next();
          eliminarID_SocketFuente (id_socketNext,"Eliminado por inactividad.");
         } // Fin del while

        // Dar un aviso al usuario.
        this.bComprobarInactividadEmisores = false;
        this.socketPTMFImp.getTemporizador().registrarFuncion (PTMF.TIEMPO_COMPROBACION_INACTIVIDAD_EMISORES,
                                       this,
                                       iEVENTO_COMPROBAR_INACTIVIDAD_EMISORES);
       }

       //-------------------------------------------------------------------
       // 4. Comprobar si se ha intentado a�adir a la cola de recepci�n y estaba llena
       // en cuyo caso, si en la cola de recepci�n hay espacio intentar entregar
       // datos al usuario. Si no se entrega al usuario no se podr� vaciar las
       // ventanas de recepci�n cuando se llenen. No se comprueba si la cola est�
       // llena porque puede ser que tenga capacidad disponible pero no la suficiente
       // para a�adir nuevos datos, por ejemplo: tiene 1 byte libre, pero todo lo
       // que se intenta a�adir supera 1 byte de tama�o.
       //-------------------------------------------------------------------
       if (this.bComprobarEntregaDatosUsuario)
         {
          Iterator iteradorID_Socket =
                        this.treeMapID_SocketEntregarUsuario.keySet().iterator();
          ID_Socket id_socketNext;
          while (iteradorID_Socket.hasNext())
           {
            id_socketNext = (ID_Socket)iteradorID_Socket.next();

            // Entregar al usuario.
            if (this.tablaID_SocketsEmisores.entregaDatosUsuario (id_socketNext,
                                         this.socketPTMFImp.getColaRecepcion()))
                {
                  this.limpiarVentana (id_socketNext);
                  continue;
                }
            // Se pone a false puesto que se ha entregado todo los disponible.
            this.bComprobarEntregaDatosUsuario = false;
            break;
           } // Fin del while
         }

      //-------------------------------------------------------------------
      // 5. Ver si hay TPDUDatos recibidos que analizar. Como m�ximo se procesan
      //    PTMF.MAX_TPDU_PROCESAR_CONSECUTIVAMENTE TPDU datos consecutivamente,
      //    es decir, antes de enviar ning�n TPDU.
      //-------------------------------------------------------------------
      iContadorIteraciones = PTMF.MAX_TPDU_PROCESAR_CONSECUTIVAMENTE;
      while ( (!this.socketPTMFImp.vectorRegistroDatos.isEmpty())
              &&
              (iContadorIteraciones >= 1)  )
       { // Analizar los TPDUDatos recibidos
        tpduDatos = (TPDUDatos)this.socketPTMFImp.vectorRegistroDatos.remove (0);
        this.procesarTPDUDatos (tpduDatos);
        // Decrementar las iteracciones
        iContadorIteraciones --;
       } // Fin del while


      //-------------------------------------------------------------------
      // 6. Comprobar si hay alg�n paquete listo para ser enviado y lo env�a.
      //-------------------------------------------------------------------
      this.enviarTPDU ();

      //-------------------------------------------------------------------
      // 7. Comprueba los id_socket e IDGL que han abandonado el grupo sin avisar.
      // S�lo si estamos en modo fiable.
      //-------------------------------------------------------------------
      this.comprobarlistaIDs_TPDUNoRecibidoAsentimiento ();

      //-------------------------------------------------------------------
      // 8. Comprobar los ID_Sockets e idgls a�adidos
      //-------------------------------------------------------------------
      this.comprobarIDA�adidos ();

      //-------------------------------------------------------------------
      // 9. Comprobar los ID_Sockets e idgls eliminados
      //-------------------------------------------------------------------
      this.comprobarIDEliminados ();


     //-------------------------------------------------------------------
     // 10. Comprobaci�n de inactividad. Dormir algunos instantes el thread
     //     si no se puede enviar datos o procesar.....
     //-------------------------------------------------------------------

     // Parar 1 milisegundo si:
     if (this.ventanaEmision != null)
     { // Este socket es fuente de datos.
         if (this.ventanaEmision.estaLlena()
             &&
             this.socketPTMFImp.vectorRegistroDatos.isEmpty())
         {
           iInactividadEmisor++;
           if(iInactividadEmisor > 1000)
           {
             this.socketPTMFImp.getTemporizador().sleep(1);
             //Log.debug(Log.DATOS_THREAD,"Emisor: sleep_1","");
             iInactividadEmisor = 0;
           }
         }
     }
     else
        {// Este socket no es fuente de datos
         if (this.socketPTMFImp.vectorRegistroDatos.isEmpty()
              &&
             this.listaAsentPositPendientesDeEnviar.size()==0
              &&
             this.listaHSACKPendientesDeEnviar.size()==0
              &&
             this.vectorMACK.isEmpty())
          {

              //   iInactividadReceptor++;
              //   if(iInactividadReceptor > 1000)
              //   {
                           this.socketPTMFImp.getTemporizador().sleep(7);
                            //Log.debug(Log.DATOS_THREAD,"Receptor: sleep_1","");
              //         iInactividadReceptor = 0;
              //   }
          }

        }


      Temporizador.yield();
   } // Fin del while
  }

  //==========================================================================
  /**
   * Comprueba los id_sockets e idgls que se han a�adido al vectorIDA�adidos
   */
  private void comprobarIDA�adidos ()
  {
   // Comprueba los nuevos ID_Socket o idgls que se han a�adido al grupo.
   if (this.vectorIDA�adidos != null
       &&
       !this.vectorIDA�adidos.isEmpty ())
      {
        for (int i=0;i<this.vectorIDA�adidos.size();i++)
         {
          Object obj = this.vectorIDA�adidos.remove(0);
          if (obj instanceof ID_Socket)
               this.a�adirID_Socket ((ID_Socket)obj);
          if (obj instanceof IDGL)
               this.a�adirIDGL ((IDGL)obj);
         }
      }
  }

  //==========================================================================
  /**
   * Comprueba los id_sockets e idgls que se han a�adido al vectorIDEliminados
   */
  private void comprobarIDEliminados ()
  {
   // Comprueba los nuevos ID_Socket o idgls que se han a�adido al grupo.
   if (this.vectorIDEliminados != null
       &&
       !this.vectorIDEliminados.isEmpty ())
      {
        for (int i=0;i<this.vectorIDEliminados.size();i++)
         {
          Object obj = this.vectorIDEliminados.remove(0);
          if (obj instanceof ID_Socket)
               this.eliminarID_Socket ((ID_Socket)obj);
          if (obj instanceof IDGL)
               this.eliminarIDGL ((IDGL)obj);
         }
      }
  }

  //==========================================================================
  /**
   * Elimina un id_socket fuente de datos. No lo elimina de la comunicaci�n
   * s�lo lo anula como fuente de datos.
   * @param id_socketFuente id_socket fuente a eliminar.
   * @param msg mensaje a enviar al usuario, si es null no se env�a ninguno.
   * Explica el motivo de la eliminaci�n.
   */
  private void eliminarID_SocketFuente (ID_Socket id_socketFuente,String msg)
  {
   final String mn = "DatosThread.eliminarID_SocketFuente (id_socketFuente,msg)";

    if (id_socketFuente==null)
      return;

    // �Puede ser este socket?
    if (id_socketFuente.equals (this.id_SocketLocal))
      {
       this.ventanaEmision = null;
      }
    else // Borrar al emisor de la tabla de emisores
        {
         // Comprobar si ten�a a�n datos que entregar al usuario.
         this.tablaID_SocketsEmisores.entregaDatosUsuario (id_socketFuente,
                                        this.socketPTMFImp.getColaRecepcion());
         if (this.tablaID_SocketsEmisores.removeID_Socket (id_socketFuente))
          {
           // Enviar un aviso al usuario.
           if (msg!=null)
             this.socketPTMFImp.sendPTMFEventConexion("[" + id_socketFuente + "]" + msg);
          }
          else return;
        }
    // Borrar a la fuente de la tabla de CG Locales, como emisor.
    this.tablaCGLocales.removeID_SocketFuente (id_socketFuente);

    // Eliminar de la cola de recepcion.
    this.socketPTMFImp.getColaRecepcion().remove (id_socketFuente);
  }

  //==========================================================================
  /**
   * Procesa cualquier TPDU Datos recibido. Utilizada en cualquiera de los modos:
   *  <ul>
   *     <li>{@link PTMF#PTMF_FIABLE}</li>
   *     <li>{@link PTMF#PTMF_FIABLE_RETRASADO}</li>
   *     <li>{@link PTMF#PTMF_NO_FIABLE}</li>
   *     <li>{@link PTMF#PTMF_NO_FIABLE_ORDENADO}</li>
   *  </ul>
   * @param tpduDatos tpdu datos a procesar.
   */
 private  void procesarTPDUDatos (TPDUDatos tpduDatos)
  {
   switch (this.iMODO_FIABILIDAD)
    {
     case PTMF.PTMF_FIABLE:
     case PTMF.PTMF_FIABLE_RETRASADO:
          this.procesarTPDUDatosFiable (tpduDatos);
          break;
     case PTMF.PTMF_NO_FIABLE:
     case PTMF.PTMF_NO_FIABLE_ORDENADO:
          this.procesarTPDUDatosNoFiable (tpduDatos);
          break;
     case PTMF.PTMF_MONITOR:
     default:
    } // Fin del switch
  }

  //==========================================================================
  /**
   * Procesa los TPDU Datos que se reciben cuando el socket est� actuando en los
   * modos:
   *  <ul>
   *     <li>{@link PTMF#PTMF_FIABLE}</li>
   *     <li>{@link PTMF#PTMF_FIABLE_RETRASADO}</li>
   *  </ul>
   * @param tpduDatos tpud datos a procesar.
   */
  private void procesarTPDUDatosFiable (TPDUDatos tpduDatos)
  {
   final String mn = "DatosThread.procesarTPDUDatosFiable (tpduDatos)";

   if (tpduDatos instanceof TPDUDatosNormal)
      {
       this.procesarTPDUDatosNormalFiable ((TPDUDatosNormal)tpduDatos);
       /** Actualizar tiempo de �ltima recepci�n del emisor del tpduDatos*/
       this.tablaID_SocketsEmisores.actualizarTiempoUltimaRecepcion (
                                        tpduDatos.getID_SocketEmisor());
       return;
      }
   if (tpduDatos instanceof TPDUDatosRtx)
      {
       this.procesarTPDUDatosRtx ((TPDUDatosRtx)tpduDatos);
       return;
      }
   if (tpduDatos instanceof TPDUACK)
      {
       this.procesarTPDUACK ((TPDUACK)tpduDatos);
       return;
      }
   if (tpduDatos instanceof TPDUHACK)
      {
       this.procesarTPDUHACK ((TPDUHACK)tpduDatos);
       return;
      }
   if (tpduDatos instanceof TPDUHSACK)
      {
       this.procesarTPDUHSACK ((TPDUHSACK)tpduDatos);
       return;
      }
   if (tpduDatos instanceof TPDUMACK)
      {
       this.procesarTPDUMACK ((TPDUMACK)tpduDatos);
       return;
      }
   if (tpduDatos instanceof TPDUNACK)
      {
       this.procesarTPDUNACK ((TPDUNACK)tpduDatos);
       return;
      }
   if (tpduDatos instanceof TPDUHNACK)
      {
       this.procesarTPDUHNACK ((TPDUHNACK)tpduDatos);
       return;
      }
  }

  //==========================================================================
  /**
   * Procesa los TPDU Datos que se reciben cuando el socket est� actuando en los
   * modos:
   *  <ul>
   *     <li>{@link PTMF#PTMF_NO_FIABLE}</li>
   *     <li>{@link PTMF#PTMF_NO_FIABLE_ORDENADO}</li>
   *  </ul>
   * @param tpduDatos tpud datos a procesar.
   */
  private void procesarTPDUDatosNoFiable (TPDUDatos tpduDatos)
  {
   final String mn = "DatosThread.procesarTPDUDatosNoFiable (tpduDatos)";

   if (tpduDatos instanceof TPDUDatosNormal)
       this.procesarTPDUDatosNormalNoFiable ((TPDUDatosNormal)tpduDatos);
  }

  //==========================================================================
  /**
   * Procesa un TPDU datos normal recibido cuando el socket est� en los modos:
   *  <ul>
   *     <li>{@link PTMF#PTMF_NO_FIABLE}</li>
   *     <li>{@link PTMF#PTMF_NO_FIABLE_ORDENADO}</li>
   *  </ul>
   * Entrega los datos contenidos en el tpduDatos al usuario.
   * @param tpduDatosNormal tpdu datos a procesar.
   */
  private void procesarTPDUDatosNormalNoFiable (TPDUDatosNormal tpduDatosNormal)
  {
   final String mn = "DatosThread.procesarTPDUDatosNormalNoFiable ";

   ID_Socket  id_SocketSrc = tpduDatosNormal.getID_SocketEmisor ();

   if (id_SocketSrc==null)
     return;

   // Entregar los datos al usuario.
   this.entregaDatosUsuarioNoFiable (id_SocketSrc,tpduDatosNormal.getNumeroSecuencia(),
                                     tpduDatosNormal.getBufferDatos(),tpduDatosNormal.getFIN_TRANSMISION());
  }

  //==========================================================================
  /**
   * Procesa un tpdu de datos normal recibido cuando el socket est� en los modos:
   *  <ul>
   *     <li>{@link PTMF#PTMF_FIABLE}</li>
   *     <li>{@link PTMF#PTMF_FIABLE_RETRASADO}</li>
   *  </ul>
   * Las operaciones que realiza son:
   * <ul>
   *    <li>Comprueba si es el primer tpdu que ha enviado, en cuyo caso a�ade un
   * nuevo emisor a la tablaID_SocketsEmisores.</li>
   *    <li>Si no es el primero y no tiene registrado al emisor solicitar  el
   * n�mero de secuencia 1.</li>
   *    <li>Almacena el tpdu en la ventana de recepci�n del socket fuente del tpdu.</li>
   *    <li>Comprobar si tiene un CG Local asignado, sino intentar ser su CG Local.</li>
   *    <li>Si se tiene que enviar asentimiento (bit ACK) apuntar para enviarlo o
   * recibirlo si somos su CG Local.</li>
   *    <li>Quitar de la lista de peticiones (NACK o HNACK).</li>
   *    <li>Comprobar si se pueden entregar datos al usuario.</li>
   *    <li>Llamar a limpiarVentana para comprobar si se pueden eliminar TPDU
   * de la ventana de recepci�n asociada al emisor fuente del TPDU de datos normal.</li>
   *    <li>Calcular si se han perdido tpdu observando el n�mero de secuencia del
   * recibido con los de la ventana de recepci�n.</li>
   * </ul>
   * @param tpduDatosNormal tpdu datos normal a procesar.
   */
  private void procesarTPDUDatosNormalFiable (TPDUDatosNormal tpduDatosNormal)
  {
   final String mn = "DatosThread.procesarTPDUDatosNormalFiable ";

   ID_Socket  id_SocketSrc = tpduDatosNormal.getID_SocketEmisor ();
   NumeroSecuencia nSec    = tpduDatosNormal.getNumeroSecuencia ();
   int       iNumRafaga    = tpduDatosNormal.getNumeroRafaga    ();
   boolean bNuevoEmisor    = false;

   //Log.debug(Log.TPDU_DATOS,"procesarTPDUDatosNormalFiable","RECIBIDO EL TPDU: " + tpduDatosNormal.getNumeroSecuencia());
   //Log.debug(Log.TPDU_DATOS,"procesarTPDUDatosNormalFiable","ACK?=" + tpduDatosNormal.getACK());
   Log.debug(Log.TPDU_DATOS,"procesarTPDUDatosNormalFiable",""+tpduDatosNormal);

   //=======================================================================
   // 1. Comprobar si es modo Fiable
   // Si el n�mero de r�faga es cero indica que el que lo envi� est� en modo
   // NO FIABLE y descartamos el TPDU.
   if (iNumRafaga < 1)
   {
     return;
   }

   if ( (id_SocketSrc==null)||(nSec==null) )
   {
     return;
   }

   //=======================================================================
   // 2. Obtener Ventana de recepci�n parar guardar el TPDU

   // Guardar el TPDU Datos en la ventana de recepci�n del socket que lo envi�.
   VentanaRecepcion vtaRecp = this.tablaID_SocketsEmisores.getVentanaRecepcion (id_SocketSrc);

   if (vtaRecp == null)
   {    // No esta registrado como emisor.
        // Si IR activo y N�mero R�faga 1 crear ventana de tpduDatosNormal.getTama�oVentana()
        // Tiene que ser el primero para as� obtener el n�mero de secuencia inicial.

        // Si el modo es FIABLE_RETRASADO no crear la ventana
        if(this.iMODO_FIABILIDAD == PTMF.PTMF_FIABLE_RETRASADO)
        {
                return;
        }

        if( tpduDatosNormal.getIR()
             &&
           (tpduDatosNormal.getNumeroRafaga()==1) )
        {
            try{
              this.tablaID_SocketsEmisores.addID_Socket (id_SocketSrc,
                                               tpduDatosNormal.getIDGL(),
                                               tpduDatosNormal.getTama�oVentana(),
                       /*N�mero Sec Inicial*/  nSec);

              bNuevoEmisor = true;
              // Informar al usuario de donde hemos comenzado a recibir del emisor.
              this.socketPTMFImp.sendPTMFEventConexion("Comienzo emisi�n del id_socket: " + id_SocketSrc);

              // Puede ser que se haya enviado un asentimiento negativo para
              // el TPDU primero (NumeroSecuencia.LIMITE_INFERIOR).
              ID_TPDU id_tpduAux = new ID_TPDU (id_SocketSrc,NumeroSecuencia.LIMITEINFERIOR);
              this.gestionAsentNeg.removeID_TPDU (id_tpduAux);

              }catch (ParametroInvalidoExcepcion e)
                 {
                  Log.log (mn,e.toString());
                 }

               catch (ClaveDuplicadaExcepcion e)
                {
                  Log.log (mn,e.toString());
                  return;
                 }
            vtaRecp = this.tablaID_SocketsEmisores.getVentanaRecepcion(id_SocketSrc);
        }
        else // Tirar el TPDU y pedir el primero.
             {
              // S�lo pido si se est� en la primera r�faga.
              if (tpduDatosNormal.getNumeroRafaga()!=1)
                return;

              try { // Pedir el 1�.
               ID_TPDU id_tpduAux = new ID_TPDU (id_SocketSrc,NumeroSecuencia.LIMITEINFERIOR);
               Log.log (mn,"A�adido a gestion asent neg : " + id_tpduAux);
               this.gestionAsentNeg.addID_TPDU (id_tpduAux);
              }catch (ParametroInvalidoExcepcion e)
                 {
                  Log.log(mn,e.toString());
                 }
              return;
             } // Fin del else
      } // Fin el if

   try {

      // Si el tpduDatosNormal es mayor al n�mero de secuencia final
      // Ser�a bueno comprobar si es menor a numsecfinal + tama�oVentana*X
      // llamar a limpiarVentana (id_Socket)
      if ( nSec.tolong() > vtaRecp.getNumSecFinal() )
      {
           Log.log (mn,"Superada el �ltimo de la ventana.");
           this.limpiarVentanaHastaPrimerTPDUConACK (tpduDatosNormal.getID_SocketEmisor());
      }

      vtaRecp.addTPDUDatosNormal (nSec.tolong(),tpduDatosNormal);
      vtaRecp.setCapacidadVentana (tpduDatosNormal.getTama�oVentana());
      this.actualizarListaID_TPDU2MitadRTT (tpduDatosNormal.getID_TPDU ());
   }catch (ParametroInvalidoExcepcion e)
     { // El paquete est� err�neo.
       Log.log (mn,"El TPDU es err�neo, por lo que es tirado");
       return;
     }

   //=======================================================================
   // 3. Obtener el CG del emisor del TPDU

   ID_Socket id_socketCGLocal = this.tablaCGLocales.getCGLocal (
                                           tpduDatosNormal.getID_SocketEmisor(),
                                           tpduDatosNormal.getNumeroRafaga());
   // Ver si hay un CG asignado a este TPDU Datos. Si no lo hay empezar
   // mecanismo para serlo.
   if (id_socketCGLocal==null)
     { /* No Exise un CG Local para el paquete.*/
       this.registrarMACK (tpduDatosNormal.getID_TPDU(),
                           tpduDatosNormal.getNumeroRafaga());
     }

   // Si el TPDU tiene el bit 'set ACK' activo actualizar el vector de ACK
   // pendientes de enviar.
   if (tpduDatosNormal.getACK ())
     {
       this.actualizarAsentPositPendientesDeEnviar (tpduDatosNormal.getID_TPDU(),
                                             tpduDatosNormal.getNumeroRafaga());
       this.actualizarHSACKPendientesDeEnviar (tpduDatosNormal.getID_TPDU(),
                                               tpduDatosNormal.getNumeroRafaga(),
                                               false);
     }

   // Actualizamos la r�faga
   this.tablaCGLocales.actualizarNumSecInicial (id_SocketSrc,iNumRafaga,nSec);

   this.gestionAsentNeg.removeID_TPDU (tpduDatosNormal.getID_TPDU());

   // Entregar los datos posibles al usuario.
   this.tablaID_SocketsEmisores.entregaDatosUsuario (tpduDatosNormal.getID_SocketEmisor(),
                                        this.socketPTMFImp.getColaRecepcion());

   this.limpiarVentana (tpduDatosNormal.getID_SocketEmisor());

   // Compruebo si supone averiguar la p�rdida de alg�n TPDU.
   this.comprobarID_TPDUNoRecibidos (tpduDatosNormal.getID_TPDU());

   // En limpiar ventana se comprueba el bit fin
  }

  //==========================================================================
  /**
   * Procesa un tpdu de datos retransmitido recibido cuando el socket est� en
   * los modos:
   *  <ul>
   *     <li>{@link PTMF#PTMF_FIABLE}</li>
   *     <li>{@link PTMF#PTMF_FIABLE_RETRASADO}</li>
   *  </ul>
   * Las operaciones que realiza son:
   * <ul>
   *    <li>Comprueba si ha sido enviado por un socket de un grupo local padre, o
   * por uno del mismo grupo local.</li>
   *    <li>Comprueba si es el primer tpdu que ha enviado, en cuyo caso a�ade un
   * nuevo emisor a la tablaID_SocketsEmisores. Si el modo es FIABLE_RETRASADO,
   * s�lo actualiza las tablas si figura en su lista de no asentidos. Enviar
   * evento de informaci�n al usuario.</li>
   *    <li>Quitar de la lista de peticiones (NACK o HNACK),</li>
   *    <li>Si no es el primero y no tiene registrado al emisor solicitar  el
   * n�mero de secuencia 1.</li>
   *    <li>Si ha sido rtx. por un miembro del mismo grupo local:
   *        <ul>
   *          <li>Actualizar la tabla de CG locales.</li>
   *          <li>Si somos el CG, tiene el bit ACK activado y no se hab�a
   *              recibido antes poner para enviar asentimiento positivo.</li>
   *          <li>Si no somos CG y estamos en su lista de no asentidos, anotar
   *              para enviar asentimiento positivo.</li>
   *        </ul>
   *    </li>
   *    <li>Si ha sido rtx. por un miembro de un grupo local padre jer�rquico:
   *        <ul>
   *          <li>Si no tiene CG asignado, registrar MACK para enviarlo.</li>
   *          <li>Si tiene el bit ACK activado y pertenece a la ventana de
   *              recepci�n correspondiente, anotar para enviar asentimiento
   *              positivo.</li>
   *          <li>Si figuramos en su lista de IDGL que no han enviado asentimiento,
   *               anotar para enviar un HSACK, y si adem�s es su CG y pertenece
   *               a la ventana de recepci�n correspondiente, anotar para enviar
   *               asentimiento positivo.</li>
   *        </ul>
   *    </li>
   *    <li>Si tiene el bit IR a 1, actulizar la tabla de CG locales.</li>
   *    <li>Comprobar si se pueden entregar datos al usuario.</li>
   *    <li>Llamar a limpiarVentana para comprobar si se pueden eliminar TPDU
   * de la ventana de recepci�n asociada al emisor fuente del TPDU de datos normal.</li>
   *    <li>Calcular si se han perdido tpdu observando el n�mero de secuencia del
   * recibido con los de la ventana de recepci�n.</li>
   * </ul>
   * @param tpduDatosRtx tpdu datos retransimitido a procesar.
   */
   private void procesarTPDUDatosRtx (TPDUDatosRtx tpduDatosRtx)
   {
     final   String mn = "DatosThread.procesarTPDUDatosRtx ";
     boolean bRtxPorPadreJerarquico = false;
     boolean bRtxPorMismoGrupoLocal = false;
     boolean bNoLoTenia = false;

     NumeroSecuencia nSec = tpduDatosRtx.getID_TPDUFuente().getNumeroSecuencia();

     // Comprobar si la fuente del TPDU es este host.
     if (tpduDatosRtx.getID_TPDUFuente().getID_Socket().equals (this.id_SocketLocal))
        return;

     //Debug
     Log.debug(Log.TPDU_RTX,mn,"RECIBIDO TPDU RTX: " + tpduDatosRtx.getID_TPDUFuente());


     // Tiene que venir de un miembro de mi grupo local o de un padre jer�rquico.
     bRtxPorMismoGrupoLocal = tpduDatosRtx.getIDGL().equals (
                                      this.socketPTMFImp.getCGLThread().getIDGL());

     if (!bRtxPorMismoGrupoLocal)
     // Ver si viene de un padre jer�rquico
         {

          TreeMap treeMapIDGLPadres = this.socketPTMFImp.getCGLThread().getCGPadres
                                                (tpduDatosRtx.getIDGLFuente());
          bRtxPorPadreJerarquico =
                        (treeMapIDGLPadres!=null)
                        &&
                        treeMapIDGLPadres.containsKey (tpduDatosRtx.getIDGL());

         }
     else bRtxPorPadreJerarquico = false;

    if (bRtxPorMismoGrupoLocal || bRtxPorPadreJerarquico)
      {
       // Continuar.
      }
    else { // Tirar el TPDU datos rtx.
          return;
         }

    // Comprobar si tengo registrado a la fuente del tpdu
    VentanaRecepcion vtaRec = this.tablaID_SocketsEmisores.getVentanaRecepcion
                               (tpduDatosRtx.getID_TPDUFuente().getID_Socket());
    if (vtaRec==null)
      try {
        if (this.iMODO_FIABILIDAD == PTMF.PTMF_FIABLE_RETRASADO)
          {
           // Comprobar si este socket figura entre los vecinos que no
           // han asentido.
           // Si el modo es PTMF.PTMF_FIABLE_RETARDADO entonces, si figuramos en
           // la lista de no asentidos poner este TPDU como el inicial de la
           // ventana.
           boolean bSalir = true;
           if (bRtxPorMismoGrupoLocal
                &&
                tpduDatosRtx.getListaID_Socket().
                                             containsKey(this.id_SocketLocal))
                {
                  bSalir = false;
                }
           else {
                 if (bRtxPorPadreJerarquico
                     /* &&
                     somosElCreadorDelGrupoLocal*/
                     &&
                     tpduDatosRtx.getListaIDGL().
                                    containsKey(this.getCGLThread().getIDGL()))
                          bSalir = false;
                 }

           if (bSalir) return;

          }
         else // MODO ES FIABLE
           {
            // Si IR activo y N�mero R�faga 1 crear ventana de
            // tpduDatosNormal.getTama�oVentana(). Tiene que ser el primero
            // para as� obtener el n�mero de secuencia inicial.
            if ( !(tpduDatosRtx.getIR()
                  &&
                  (tpduDatosRtx.getNumeroRafagaFuente()==1)) )

              { // Tirar el TPDU.
                // S�lo pido si se est� en la primera r�faga.
                if (tpduDatosRtx.getNumeroRafagaFuente()!=1)
                        return;

                // No pido este. Se supone que se han perdido varios
                // this.gestionAsentNeg.addID_TPDU (tpduDatosRtx.getID_TPDUFuente());
                // Pido el de Numero de secuencia 1.
                ID_TPDU id_tpduAux = new ID_TPDU (
                             tpduDatosRtx.getID_TPDUFuente().getID_Socket(),
                                        NumeroSecuencia.LIMITEINFERIOR);
                //Log.log (mn,"A�adido a gestion asent neg: " + id_tpduAux);
                this.gestionAsentNeg.addID_TPDU (id_tpduAux);

                return;
              } // Fin del if
             else // Es el primer tpdu del emisor.
                 {
                  // Puede ser que se haya enviado un asentimiento negativo para
                  // el TPDU primero (NumeroSecuencia.LIMITE_INFERIOR).
                  ID_TPDU id_tpduAux = new ID_TPDU (
                                tpduDatosRtx.getID_TPDUFuente().getID_Socket(),
                                NumeroSecuencia.LIMITEINFERIOR);
                  this.gestionAsentNeg.removeID_TPDU (id_tpduAux);
                 }
            } // Fin del else


        // A�adir el nuevo emisor y obtener su ventana de recepci�n.
        this.tablaID_SocketsEmisores.addID_Socket (
                                 tpduDatosRtx.getID_TPDUFuente().getID_Socket(),
                                 tpduDatosRtx.getIDGLFuente(),
                                 tpduDatosRtx.getTama�oVentana(),
                /*N�m Sec Inic*/ nSec);


        if (tpduDatosRtx.getIR() && tpduDatosRtx.getNumeroRafagaFuente()==1)
         {
          Log.log (mn,"Comienzo  de emisi�n de la fuente: " + tpduDatosRtx.getID_TPDUFuente().getID_Socket());
          this.socketPTMFImp.sendPTMFEventConexion("Comienzo de emsion del id_socket: " +
                                tpduDatosRtx.getID_TPDUFuente().getID_Socket());
         }
        else
         {
          Log.log (mn,"Comienzo  de emisi�n RETRASADA de la fuente: " + tpduDatosRtx.getID_TPDUFuente().getID_Socket());
          this.socketPTMFImp.sendPTMFEventConexion("Comienzo de emsion retrasada del id_socket" +
                                tpduDatosRtx.getID_TPDUFuente().getID_Socket());
         }

        if (this.iMODO_FIABILIDAD == PTMF.PTMF_FIABLE_RETRASADO)
         {
          this.treeMapNuevosEmisoresASincronizar.put
                                (tpduDatosRtx.getID_TPDUFuente().getID_Socket(),
                                 tpduDatosRtx.getID_TPDUFuente());
         }

        vtaRec = tablaID_SocketsEmisores.getVentanaRecepcion
                               (tpduDatosRtx.getID_TPDUFuente().getID_Socket());

      }catch (ClaveDuplicadaExcepcion e)
        {
         Log.log (mn,e.toString());
         return;
        }
       catch (ParametroInvalidoExcepcion e)
         {
          Log.log (mn,e.toString());
          return;
         }

    if (!vtaRec.recibido (nSec))
          try{
           bNoLoTenia = true;
           // Si el tpduDatosNormal es mayor al n�mero de secuencia final
           // Ser�a bueno comprobar si es menor a numsecfinal + tama�oVentana*X
           // llamar a limpiarVentana (id_Socket)
           if ( nSec.tolong() > vtaRec.getNumSecFinal() )
           {
                Log.log (mn,"Superado el final de la ventana.");
                this.limpiarVentanaHastaPrimerTPDUConACK
                                (tpduDatosRtx.getID_TPDUFuente().getID_Socket());
           }
           // Crear un TPDUDatosNormal a partir del TPDUDatosRtx
           TPDUDatosNormal tpduDatosNormal =
                                      tpduDatosRtx.convertirATPDUDatosNormal ();
           vtaRec.addTPDUDatosNormal (nSec.tolong(),tpduDatosNormal);
           vtaRec.setCapacidadVentana (tpduDatosRtx.getTama�oVentana());
           this.gestionAsentNeg.removeID_TPDU (tpduDatosNormal.getID_TPDU());
          }catch (ParametroInvalidoExcepcion pie)
             {
              Log.log (mn,pie.toString());
              return;
             }
           catch (PTMFExcepcion e)
             {
              Log.log(mn,e.toString());
              return;
             }
    else bNoLoTenia = false;

    // S�lo actualizamos la tabla de CG si este TPDU viene de mi grupo local.
    if (bRtxPorMismoGrupoLocal)
       {
        NumeroSecuencia nSecInicRafaga = this.tablaCGLocales.getNumSecInicialRafaga
                                 ( tpduDatosRtx.getID_TPDUFuente().getID_Socket(),
                                   tpduDatosRtx.getNumeroRafagaFuente());

        ID_Socket id_socketCGLocal = actualizarTablaCGLocales (
           /*id_socket Fuente */tpduDatosRtx.getID_TPDUFuente().getID_Socket(),
           /*N. R�faga        */tpduDatosRtx.getNumeroRafagaFuente(),
           /*nSecInicR�faga   */(nSecInicRafaga!=null?nSecInicRafaga:
                                             tpduDatosRtx.getNumeroSecuencia()),
           /*id_socketCGLocal */tpduDatosRtx.getID_SocketEmisor());

        // Puede ser que ahora seamos nosotros el CG Local
        if ( (tpduDatosRtx.getACK ())
             &&
             (id_socketCGLocal!=null))
        {
         if (!id_socketCGLocal.equals (this.id_SocketLocal))
         {
          // Ver si figuro en su lista como que no lo he asentido.
          // Si figuro, almacenar la informaci�n sobre el ACK que queda pendiente
          // de env�o.
          TreeMap listaNoAsentidos = tpduDatosRtx.getListaID_Socket ();
          if (listaNoAsentidos!=null)
           if (listaNoAsentidos.containsKey (this.id_SocketLocal))
            { // Actualizar la lista de ACK pendientes de enviar.
              this.actualizarAsentPositPendientesDeEnviar (tpduDatosRtx.getID_TPDUFuente(),
                                                 tpduDatosRtx.getNumeroRafagaFuente());
            } // Fin del if.
         }// Fin del if
         else // Somos su CG Local
             if (bNoLoTenia)
                 this.actualizarAsentPositPendientesDeEnviar (tpduDatosRtx.getID_TPDUFuente(),
                                                 tpduDatosRtx.getNumeroRafagaFuente());

        } // Fin del if

       } // Fin del if (rtxPorMismoGrupoLocal)

    if (bRtxPorPadreJerarquico)
     { // Comprobar si tiene un grupo local asignado.
       ID_Socket id_socketCGLocal = this.tablaCGLocales.getCGLocal (
                                tpduDatosRtx.getID_TPDUFuente().getID_Socket(),
                                tpduDatosRtx.getNumeroRafagaFuente());

       // Si no lo tiene comenzar mecanismo para serlo.
       // Puede ser que ya haya tenido y se haya eliminado.
       if (id_socketCGLocal == null)
         { // No Exise un CG Local para el paquete.
          this.registrarMACK (tpduDatosRtx.getID_TPDUFuente(),
                              tpduDatosRtx.getNumeroRafagaFuente());

           // Si todav�a pertenece a la ventana de recepci�n, aunque no lo tenga,
           // lo apunto para enviar asentimiento positivo.
           if (tpduDatosRtx.getACK())
           {
             if (vtaRec.nSecEntreInicialyFinal (tpduDatosRtx.getID_TPDUFuente().
                                        getNumeroSecuencia().tolong()))
                this.actualizarAsentPositPendientesDeEnviar (tpduDatosRtx.getID_TPDUFuente(),
                                                      tpduDatosRtx.getNumeroRafagaFuente());
           } // Fin del if
         } // Fin del if

       if (tpduDatosRtx.getACK ())
         {
          // Ver si figuramos en su lista de hijos que no han enviado asentimiento.
          TreeMap listaIDGLNoAsentidos = tpduDatosRtx.getListaIDGL ();
          if (listaIDGLNoAsentidos!=null)
           if (listaIDGLNoAsentidos.containsKey (this.socketPTMFImp.getCGLThread().getIDGL()))
            { // No ha recibido ning�n asentimiento de mi grupo local.

              // Cualquier socket del grupo local puede mandar el HSACK por
              // si se ha caido el CG Local.
              this.actualizarHSACKPendientesDeEnviar
                                         (tpduDatosRtx.getID_TPDUFuente(),
                                          tpduDatosRtx.getNumeroRafagaFuente(),
                      /*Pedido en rtx.*/  true);

              // SI NO ESTAMOS EN SU LISTA ES PORQUE HA SIDO ASENTIDO CON ANTERIORIDAD.

              // Si somos su CG local apuntar para enviar el HACK, s�lo si todav�a
              // lo tenemos en la ventana. Quiere decir que probablemente no se haya
              // enviado un HACK. (Cuando se env�a un HACK se elimina de la ventana)
              if (id_socketCGLocal != null)
               {
                if (id_socketCGLocal.equals (this.id_SocketLocal))
                  if (vtaRec.nSecEntreInicialyFinal (tpduDatosRtx.getID_TPDUFuente().
                                        getNumeroSecuencia().tolong()))
                     {
                      this.ponerEnEsperaAsentimiento (tpduDatosRtx.getID_TPDUFuente(),
                                             tpduDatosRtx.getNumeroRafagaFuente(),
                                             true/*solo rtx, si es necesario, a hijos*/);
                     }
               }
            }
           } // Fin del if (tpduDatosRtx.getACK())
     } // Fin del if (rtxPorPadreJerarquico)

    if (tpduDatosRtx.getIR ())
     {
      // Actualizar la tabla de CG Locales.
      this.tablaCGLocales.actualizarNumSecInicial (tpduDatosRtx.getID_TPDUFuente().getID_Socket(),
                                                   tpduDatosRtx.getNumeroRafagaFuente(),
                                                   tpduDatosRtx.getNumeroSecuencia());
     }

    // Entregar los datos posibles al usuario.
    this.tablaID_SocketsEmisores.entregaDatosUsuario (tpduDatosRtx.getID_TPDUFuente().getID_Socket(),
                                        this.socketPTMFImp.getColaRecepcion());

    this.limpiarVentana (tpduDatosRtx.getID_TPDUFuente().getID_Socket());

    // Comprobar si ha habido perdidas de TPDU.
    this.comprobarID_TPDUNoRecibidos (tpduDatosRtx.getID_TPDUFuente());

    // En limpiar ventana se comprueba si tiene el bit fin activo.
   }

  //==========================================================================
  /**
   * Procesa un tpdu ACK, s�lo si es el CG para el ID_TPDU que est�n asintiendo.
   * Si no lo esperaba, y el n�mero de secuencia pertenece a la ventana, lo a�ade
   * a this.tablaAsentimientos.
   * @param tpduACK tpdu ACK a procesar.
   */
  private void procesarTPDUACK (TPDUACK tpduACK)
  {
  final String mn = "DatosThread.procesarTPDUACK";

  // Comprobar que somos el CG Local de dicha r�faga.
  ID_Socket id_socketCGLocal = this.tablaCGLocales.getCGLocal (tpduACK.getID_TPDUFuente().getID_Socket(),
                                                               tpduACK.getNumeroRafagaFuente());

  if (id_socketCGLocal==null)
    return;

  // Comprobar si somos CG Local del id_tpdu que me est�n asintiendo
  // Nota: EL Emisor siempre es el CGL de todos los paquetes enviados por el.
  if (!id_socketCGLocal.equals (this.id_SocketLocal))
    return;

  // Puede ser que el tpdu que me est�n asintiendo no lo hayamos recibido,
  // por lo que no estamos en espera de asentimiento por �l.
  if (!this.tablaAsentimientos.contieneID_TPDU (tpduACK.getID_TPDUFuente()))
     {
      // Comprobar si el tpdu que me est�n asintiendo, todav�a pertenece
      // a la ventana correspondiente, aunque el propio TPDU no lo tenga.
      if (tpduACK.getID_TPDUFuente().getID_Socket().equals (this.id_SocketLocal))
       { // Buscar en la ventana de emisi�n
         if (this.ventanaEmision==null)
             return;

         if (!this.ventanaEmision.nSecEntreInicialyFinal(tpduACK.getID_TPDUFuente().
                                        getNumeroSecuencia().tolong()))
           return;
      }
      else // Buscar en la ventana de recepci�n
          {
            VentanaRecepcion vtaRecp = this.tablaID_SocketsEmisores.
                  getVentanaRecepcion(tpduACK.getID_TPDUFuente().getID_Socket());
            if (vtaRecp==null)
               return;

            if (!vtaRecp.nSecEntreInicialyFinal(tpduACK.getID_TPDUFuente().
                                        getNumeroSecuencia().tolong()))
              return;
          } // Fin del else

      // A�adir a la tabla de hijos
      this.ponerEnEsperaAsentimiento (tpduACK.getID_TPDUFuente(),
                                      tpduACK.getNumeroRafagaFuente(),false);

      // Actualizar la listas de HSACK pendientes de enviar.
      this.actualizarHSACKPendientesDeEnviar (tpduACK.getID_TPDUFuente(),
                                              tpduACK.getNumeroRafagaFuente(),
                                              false);

     }

  Log.debug(Log.ACK,mn,"RECIBIDO ACK UNICAST: " + tpduACK.getID_TPDUFuente()+" Rafaga: "+ tpduACK.getNumeroRafagaFuente());

  // Anotar el ACK recibido en la tabla de los hijos que est�n a mi cargo.
  this.tablaAsentimientos.addACK (tpduACK.getID_SocketEmisor(),tpduACK.getID_TPDUFuente());

  // Comprobar si TODOS han mandado asentimiento para el id_tpdu.
  this.limpiarVentana (tpduACK.getID_TPDUFuente(),false);

  // Actualizar la lista de id_socket no sincronizados
  // El id_socket que ha enviado el ACK est� sincronizado para dicho emisor
  if (this.iMODO_FIABILIDAD == PTMF.PTMF_FIABLE_RETRASADO)
   {
    TreeMap treeMapEmisores = (TreeMap)
           this.treeMapID_SocketNoSincronizado.get (tpduACK.getID_SocketEmisor());
    if (treeMapEmisores!=null)
     {
      treeMapEmisores.put (tpduACK.getID_TPDUFuente().getID_Socket(),Boolean.TRUE);
     }
   }
  }

  //==========================================================================
  /**
   * Procesa un tpdu NACK.  S�lo se comprueba si es enviado por un miembro del
   * del grupo local.<br>
   * Las operaciones que realiza son:
   * <ul>
   *    <li>Comprueba aquellos ID_TPDU solicitados de los que es CG Local, retransmiti�ndolo
   * si lo tiene.</li>
   *    <li>Actualiza los TPDUNACK que tiene y que sean equivalentes al recibido.</li>
   * </ul>
   * @param tpduNACK tpdu NACK a analizar.
   */
  private void procesarTPDUNACK (TPDUNACK tpduNACK)
  {
  final String mn = "DatosThread.procesarTPDUNACK";
  TPDUDatosNormal tpduDatosNormal;

    //Depuraci�n ====================================
      Log.debug(Log.NACK,mn,"<-- RECIBIDO NACK: " + tpduNACK.getID_SocketEmisor());
      Log.debug(Log.NACK,"","TPDUs perdidos:");

      ListaOrdID_TPDU lista =  tpduNACK.getListaID_TPDU();
      Iterator itID_TPDU = lista.iteradorID_TPDU();

      while(itID_TPDU.hasNext())
      {
        Log.debug(Log.NACK,"",""+(ID_TPDU)itID_TPDU.next());
      }
    //Depuraci�n ====================================

    // Comprobar si soy el CG Local para alguno de los TPDU solicitados.
    // Si lo soy, ver si tengo el TPDU solicitado. Se tiene que recorrer toda la
    // informaci�n del TPDU y ver si somos CG local de alguno de los indicados.

   // Si es de otro Grupo Local, desechar
   if (!(tpduNACK.getIDGL().equals (this.socketPTMFImp.getCGLThread().getIDGL())))
         return;

   ListaOrdID_TPDU listaNACK = tpduNACK.getListaID_TPDU ();
   Iterator iterador = listaNACK.iteradorID_TPDU();
   ID_TPDU id_tpduNext;
   ID_Socket id_SocketSrc;
   NumeroSecuencia nSec;
   while (iterador.hasNext())
    {
     id_tpduNext   = (ID_TPDU)iterador.next ();
     id_SocketSrc  = id_tpduNext.getID_Socket ();
     nSec          = id_tpduNext.getNumeroSecuencia ();

     // Este socket es la fuente de uno de los que est� pidiendo.
     // Todo socket es CGLocal de los que contiene en su ventana de emisi�n.
     if (id_SocketSrc.equals(this.id_SocketLocal))
       {
        iterador.remove (); // Lo borro del NACK
        if (this.ventanaEmision==null)
                continue;
        this.actualizarListaID_TPDUSolicitadoRtx(id_tpduNext,tpduNACK.getID_SocketEmisor(),
                                                 iPEDIDO_POR_VECINO);
        continue;
       }

     VentanaRecepcion vtaRecep = this.tablaID_SocketsEmisores.getVentanaRecepcion(id_SocketSrc);
     if (vtaRecep!=null) // 1� if
     {
      if (vtaRecep.recibido(nSec)) // 2� if
          {
           iterador.remove();
           ID_Socket id_socketCGLocal = this.tablaCGLocales.getCGLocal (id_tpduNext);
           if (id_socketCGLocal!=null
                &&
               id_socketCGLocal.equals (this.id_SocketLocal))
             {
               actualizarListaID_TPDUSolicitadoRtx(id_tpduNext,tpduNACK.getID_SocketEmisor(),
                                                   iPEDIDO_POR_VECINO);
             }
          }
          else this.gestionAsentNeg.addID_TPDU (id_tpduNext);
     } else    this.gestionAsentNeg.addID_TPDU (id_tpduNext);
    } // Fin del WHILE

   // Si bFinRTT es true se ha cumplido, al menos una vez, RTT y no se ha ejecutado
   // la funci�n RtxTPDUFinRTT, por lo que se actualiza igual que si estuvi�semos
   // en la 2� mitad de rtt.

   // Comprobar los TPDUNACK que tenemos y que sean equivalentes al recibido.
   this.gestionAsentNeg.actualizar (listaNACK,this.bFinRTT);
  }

  //==========================================================================
  /**
   * Procesa un tpdu HNACK.
   * @param tpduHNACK tpdu HNACK a analizar.
   */
  private void procesarTPDUHNACK (TPDUHNACK tpduHNACK)
  {
   final String mn = "DatosThread.procesarTPDUHNACK";
   TPDUDatosNormal tpduDatosNormal;

   //Log.log (mn,"<<<<<<<<<<<<<<< RECIBIDO UN HNACK >>>>>>>>>>>>>>>");
  //Depuraci�n ====================================
     Log.debug(Log.HNACK,mn,"<-- Recibido un HNACK: " + tpduHNACK.getID_SocketEmisor());
      Log.debug(Log.HNACK,"","TPDUs perdidos:");

      ListaOrdID_TPDU lista =  tpduHNACK.getListaID_TPDU();
      Iterator itID_TPDU = lista.iteradorID_TPDU();

      while(itID_TPDU.hasNext())
      {
        Log.debug(Log.HNACK,"",""+(ID_TPDU)itID_TPDU.next());
      }
    //Depuraci�n ====================================



   // Comprobar si soy el CG Local para alguno de los TPDU solicitados.
   // Si lo soy, ver si tengo el TPDU solicitado. Se tiene que recorrer toda la
   // informaci�n del TPDU y ver si somos CG local de alguno de los indicados.

   // Ver los cambios que se hagan en el env�o de los HNACK en gestionAsentNeg
   // y tratar seg�n lo indicado.

   IDGL idglEmisor = tpduHNACK.getIDGL();
   // Comprobar si es de este grupo local.
   boolean bCondicion = idglEmisor.equals (this.socketPTMFImp.getCGLThread().getIDGL());

   ListaOrdID_TPDU listaHNACK = tpduHNACK.getListaID_TPDU ();
   if (bCondicion)
   {
    // Comprobar los TPDUHNACK que tenemos y que sean equivalentes al recibido.
    this.gestionAsentNeg.actualizar (listaHNACK,this.bFinRTT);
    return;
   }

   Iterator iterador = listaHNACK.iteradorID_TPDU();
   ID_TPDU id_tpduNext = null;
   ID_Socket id_SocketSrc = null;
   NumeroSecuencia nSec = null;
   while (iterador.hasNext())
    {
     id_tpduNext   = (ID_TPDU)iterador.next ();
     id_SocketSrc  = id_tpduNext.getID_Socket ();
     nSec          = id_tpduNext.getNumeroSecuencia ();

     iterador.remove();

     if (tpduHNACK.recibidoPorUnicast)// Recibido por unicast
      {
       // Por unicast s�lo me pueden pedir los que yo emito (ventanaEmision)
       if (id_SocketSrc.equals (this.id_SocketLocal))
          this.actualizarListaID_TPDUSolicitadoRtx(id_tpduNext,tpduHNACK.getID_SocketEmisor(),
                                                   iPEDIDO_POR_UNICAST);
       continue;
      }

     // RECIBIDO POR MULTICAST
     // Comprobar si es hijo jer�rquico para dicho id_tpdu o si ha sido enviado
     // por un miembro de mi grupo local, en cuyo caso tambi�n lo acepto
     // Obtener el IDGL de la fuente del id_tpdu que me est�n asentiendo.
     IDGL idglFuente = this.getIDGL (id_tpduNext.getID_Socket());
     TreeMap treeMapIDGLHijos = this.socketPTMFImp.getCGLThread().getCGHijos(idglFuente);
     bCondicion = (treeMapIDGLHijos==null?this.socketPTMFImp.getIDGL().equals(idglEmisor):
                                    treeMapIDGLHijos.containsKey (idglEmisor)
                                    ||
                                    this.socketPTMFImp.getIDGL().equals(idglEmisor));

     if (!bCondicion)
        continue;

     // Este host es la fuente de uno de los que est� pidiendo.
     if (id_SocketSrc.equals(this.id_SocketLocal))
       {
        if (this.ventanaEmision==null)
                continue;

        this.actualizarListaID_TPDUSolicitadoRtx(id_tpduNext,tpduHNACK.getID_SocketEmisor(),
                                                 iPEDIDO_POR_HIJO);
        continue;
       }

     VentanaRecepcion vtaRecep = this.tablaID_SocketsEmisores.getVentanaRecepcion(id_SocketSrc);
     if (vtaRecep!=null) // 1� if
     {
      if (vtaRecep.recibido(nSec)) // 2� if
          {
           ID_Socket id_socketCGLocal = this.tablaCGLocales.getCGLocal (id_tpduNext);
           if (id_socketCGLocal!=null
                &&
               id_socketCGLocal.equals (this.id_SocketLocal))
             {
               this.actualizarListaID_TPDUSolicitadoRtx(id_tpduNext,tpduHNACK.getID_SocketEmisor(),
                                                        iPEDIDO_POR_HIJO);
             }
          }
          else this.gestionAsentNeg.addID_TPDU (id_tpduNext);
     } else    this.gestionAsentNeg.addID_TPDU (id_tpduNext);
    } // Fin del While
  }

  //==========================================================================
  /**
   * Procesa un tpdu MACK.<br>
   * Actuliza la tabla de CG locales.
   * @param tpduMACK tpdu MACK a analizar.
   */
  private void procesarTPDUMACK (TPDUMACK tpduMACK)
  {
  final String mn = "DatosThread.procesarTPDUMACK";

   Log.debug(Log.MACK,mn,"<-- RECIBIDO MACK: " + tpduMACK.getID_SocketEmisor());

  // Se asegura que el MACK viene de mi mismo grupo local, pero no que lo tenga
  // registrado como vecino. Por ejemplo: algui�n que d� por ca�do y que realmente
  // no lo estaba, lo elimine como vecino pero sigue activo y ahora env�a un MACK.

  // S�lo actualizo si es de mi mismo Grupo Local.
  if (tpduMACK.getIDGL().equals (this.socketPTMFImp.getCGLThread().getIDGL()))
     actualizarTablaCGLocales (
                /*id_socket Fuente*/tpduMACK.getID_SocketFuente(),
                /*N. R�faga       */tpduMACK.getNumeroRafagaFuente(),
                /*nSecInicR�faga  */tpduMACK.getNumSecInicialRafaga(),
                /*idsocketCGLocal */tpduMACK.getID_SocketEmisor());
   else { // No hacer nada
         return;
        }
  }

  //==========================================================================
  /**
   * Procesa un tpdu HSACK. <br>
   * @param tpduHSACK tpdu HSACK a analizar.
   */
   private void procesarTPDUHSACK (TPDUHSACK tpduHSACK)
   {
    final String mn = "DatosThread.procesarTPDUHSACK";

    Log.debug(Log.HSACK,mn,"<-- Recibido un HSACK: " + tpduHSACK.getID_TPDUFuente());

   // Comprobar que somos el CG Local de dicha r�faga.
   ID_Socket id_socketCGLocal = this.tablaCGLocales.getCGLocal (tpduHSACK.getID_TPDUFuente().getID_Socket(),
                                                               tpduHSACK.getNumeroRafagaFuente());

   if ( id_socketCGLocal==null || !id_socketCGLocal.equals (this.id_SocketLocal) )
   {
    // Actualizamos la tabla de asentimientos, puede que no estemos esperando
    // asentimiento por el id_tpdu contenido en el tpduHSACK pero s� para otros
    // menores. En tabla de asentimientos se comprueba si es o no hijo jer�rquico.
    this.tablaAsentimientos.addHSACK (tpduHSACK.getIDGL(),
                                     tpduHSACK.getID_TPDUFuente());
    return;
   }

   // Puede ser que el tpdu que me est�n asentiendo no lo hayamos recibido,
   // por lo que no estamos en espera de asentimiento por �l.
   if (!this.tablaAsentimientos.contieneID_TPDU (tpduHSACK.getID_TPDUFuente()))
     {
      // Comprobar si el tpdu que me est�n asintiendo, todav�a pertenece
      // a la ventana correspondiente, aunque el propio TPDU no lo tenga.
      if (tpduHSACK.getID_TPDUFuente().getID_Socket().equals (this.id_SocketLocal))
       {
         if (this.ventanaEmision==null)
           return;

         // Buscar en la ventana de emisi�n
         if (!this.ventanaEmision.nSecEntreInicialyFinal (tpduHSACK.getID_TPDUFuente().
                                getNumeroSecuencia().tolong()))
           return;
       }
       else // Buscar en la ventana de recepci�n
          {
            VentanaRecepcion vtaRecp = this.tablaID_SocketsEmisores.
                  getVentanaRecepcion(tpduHSACK.getID_TPDUFuente().getID_Socket());
            if (vtaRecp==null)
               return;
            if (!vtaRecp.nSecEntreInicialyFinal (tpduHSACK.getID_TPDUFuente().
                                getNumeroSecuencia().tolong()))
              return;
          } // Fin del else

       // A�adir a la tabla de hijos
       this.ponerEnEsperaAsentimiento (tpduHSACK.getID_TPDUFuente(),
                                      tpduHSACK.getNumeroRafagaFuente(),false);

       // No pongo enviar HSACK, puesto que con esta informaci�n no garantizo que
       // haya sido recibido por alguno de mi grupo local o yo mismo.
     } // Fin del if

   // Anotar el HSACK recibido en la tabla de los hijos que est�n a mi cargo.
   this.tablaAsentimientos.addHSACK (tpduHSACK.getIDGL(),
                                      tpduHSACK.getID_TPDUFuente());

   //Log.log (mn,"RECIBIDO HSACK : " + tpduHSACK.getID_TPDUFuente());

   // Actualizar la lista de IDGL no sincronizados
   // El IDGL que ha enviado el HSACK est� sincronizado para dicho emisor
   if (this.iMODO_FIABILIDAD == PTMF.PTMF_FIABLE_RETRASADO)
    {
     TreeMap treeMapEmisores = (TreeMap)
                        this.treeMapIDGLNoSincronizado.get (tpduHSACK.getIDGL());
     if (treeMapEmisores!=null)
      {
       treeMapEmisores.put (tpduHSACK.getID_TPDUFuente().getID_Socket(),Boolean.TRUE);
      }
    }
   }

  //==========================================================================
  /**
   * Procesa un tpdu HACK.
   * @param tpduHACK tpdu HACK a analizar.
   */
   private void procesarTPDUHACK (TPDUHACK tpduHACK)
   {
    final String mn = "DatosThread.procesarTPDUHACK";


    Log.debug(Log.HACK,mn,"<-- Recibido un HACK de "+tpduHACK.getID_SocketEmisor()+ " asiente "+tpduHACK.getID_TPDUFuente());

    // Comprobar que somos el CG Local de dicha r�faga.
    ID_Socket id_socketCGLocal = this.tablaCGLocales.getCGLocal (tpduHACK.getID_TPDUFuente().getID_Socket(),
                                                               tpduHACK.getNumeroRafagaFuente());

    if (id_socketCGLocal==null || !id_socketCGLocal.equals (this.id_SocketLocal) )
     {
      // Actualizamos la tabla de asentimientos, puede que no estemos esperando
      // asentimiento por el id_tpdu contenido en el tpduHACK pero s� para otros
      // menores. En tabla de asentimientos se comprueba si es o no hijo jer�rquico.
      this.tablaAsentimientos.addHACK (tpduHACK.getIDGL(),
                                     tpduHACK.getID_TPDUFuente());
      return;
     }

    // Puede ser que el tpdu que me est�n asentiendo no lo hayamos recibido,
    // por lo que no estamos en espera de asentimiento por �l.
    if (!this.tablaAsentimientos.contieneID_TPDU (tpduHACK.getID_TPDUFuente()))
     {
      // Comprobar si el tpdu que me est�n asintiendo, todav�a pertenece
      // a la ventana correspondiente, aunque el propio TPDU no lo tenga.
      if (tpduHACK.getID_TPDUFuente().getID_Socket().equals (this.id_SocketLocal))
       {
         if (this.ventanaEmision==null)
           return;
         // Buscar en la ventana de emisi�n
         if (!this.ventanaEmision.nSecEntreInicialyFinal (tpduHACK.getID_TPDUFuente().
                                getNumeroSecuencia().tolong()))
           return;
       }
      else // Buscar en la ventana de recepci�n
          {
            VentanaRecepcion vtaRecp = this.tablaID_SocketsEmisores.
                  getVentanaRecepcion(tpduHACK.getID_TPDUFuente().getID_Socket());
            if (vtaRecp==null)
               return;
            if (!vtaRecp.nSecEntreInicialyFinal (tpduHACK.getID_TPDUFuente().
                                getNumeroSecuencia().tolong()))
              return;
          } // Fin del else

      // A�adir a la tabla de hijos
      this.ponerEnEsperaAsentimiento (tpduHACK.getID_TPDUFuente(),
                                      tpduHACK.getNumeroRafagaFuente(),false);
     }// Fin del if

    //Log.log ("","RECIBIDO UN HACK: " + tpduHACK.getID_TPDUFuente() + " rafaga: " + tpduHACK.getNumeroRafagaFuente());

    // Anotar el HACK recibido en la tabla de los hijos que est�n a mi cargo.
    this.tablaAsentimientos.addHACK (tpduHACK.getIDGL(),
                                     tpduHACK.getID_TPDUFuente());

    // Limpiar la ventana
    this.limpiarVentana (tpduHACK.getID_TPDUFuente(),false);

    // Actualizar la lista de IDGL no sincronizados
    // El IDGL que ha enviado el HACK est� sincronizado para dicho emisor
    if (this.iMODO_FIABILIDAD == PTMF.PTMF_FIABLE_RETRASADO)
     {
      TreeMap treeMapEmisores = (TreeMap)
                        this.treeMapIDGLNoSincronizado.get (tpduHACK.getIDGL());
      if (treeMapEmisores!=null)
       {
        treeMapEmisores.put (tpduHACK.getID_TPDUFuente().getID_Socket(),Boolean.TRUE);
       }
     }
   }

  //==========================================================================
  /**
   * Env�a tpdu de datos (TPDUDatosNormal, TPDUDatosRtx, TPDUMACK, ...).
   * Delega en la funci�n {@link #enviarTPDUFiable()} si el socket est� en modo
   * fiable, o en la funci�n {@link #enviarTPDUNoFiable()} si el modo es no fiable.
   * Comprueba si ha transcurrido el tiempo m�nimo entre env�os.
   * @return true ha enviado alg�n tpdu de datos, y false en caso contrario.
   */
  private  boolean enviarTPDU ()
  {
   boolean bResult = false;

   if (this.lT_Min_Entre_Envios > 0)
        return false; // Hay que esperar.

   switch (this.iMODO_FIABILIDAD)
    {
     case PTMF.PTMF_FIABLE:
     case PTMF.PTMF_FIABLE_RETRASADO:
          bResult = this.enviarTPDUFiable ();
          break;
     case PTMF.PTMF_NO_FIABLE:
     case PTMF.PTMF_NO_FIABLE_ORDENADO:
          bResult = this.enviarTPDUNoFiable ();
          break;
     case PTMF.PTMF_MONITOR:
     default:
    } // Fin del switch

   if (bResult) // Se ha enviado alg�n TPDU
    {
     // Volver a registrar el tiempo m�nimo entre env�os.
     this.lT_Min_Entre_Envios = PTMF.T_MIN_ENTRE_ENVIOS;
     this.socketPTMFImp.getTemporizador().registrarFuncion (PTMF.T_MIN_ENTRE_ENVIOS,  /* mseg */
                                    this,                     /* TimerHandler */
                                    iEVENTO_T_MIN_ENTRE_ENVIOS /* arg1 */
                                    );
    } // Fin del if
   return bResult;
  }


  //==========================================================================
  /**
   * Env�a los tpdu de datos pendientes. Ejecuta de forma secuencial las distintas
   * funciones de env�o. El orden seguido es:
   * <ol>
   *    <il>{@link #enviarTPDUMACK()}</il>
   *    <il>{@link #enviarAsentNeg()}</il>
   *    <il>{@link #enviarAsentimientoPositivo()}</il>
   *    <il>{@link #enviarTPDUHSACK()}</il>
   *    <il>{@link #enviarTPDUDatosSolicitadosRtx()} Env�a hasta cinco rtx. antes
   * de seguir enviando otro tipo de TPDUs.</il>
   *    <il>{@link #enviarTPDUDatosNormalFiable()}</il>
   *    <il>{@link #RtxTPDUFinRTT()}</il>
   * </ol>
   * @return true si se ha enviado alg�n tpdu de datos.
   */
  private boolean enviarTPDUFiable ()
  {
   final String mn = "DatosThread.enviarTPDUFiable()";

   // Intenta mandar estableciendo una preferencia.
   boolean bResult;
   bResult = this.enviarTPDUMACK ();

   if (bResult)
    this.enviarAsentNeg ();
   else bResult = this.enviarAsentNeg ();

   if (bResult)
    this.enviarAsentimientoPositivo ();
   else bResult = this.enviarAsentimientoPositivo ();

   if (bResult)
    this.enviarTPDUHSACK ();
   else bResult = this.enviarTPDUHSACK ();

   // Se env�an hasta 5 rtx. antes de seguir enviando otro tipo de TPDUs.
   iContadorIteracionEnviarTPDUFiable ++;
   if (iContadorIteracionEnviarTPDUFiable>=5)
       {
        iContadorIteracionEnviarTPDUFiable = 0;
        if (bResult)
          this.enviarTPDUDatosSolicitadosRtx ();
        else bResult = this.enviarTPDUDatosSolicitadosRtx ();
       }

   if (bResult)
    this.enviarTPDUDatosNormalFiable ();
   else bResult = this.enviarTPDUDatosNormalFiable ();

   if (bResult)
     this.RtxTPDUFinRTT ();
   else bResult = this.RtxTPDUFinRTT ();

   return bResult;
  }

  //==========================================================================
  /**
   * Env�a los tpdu de datos pendientes.
   * LLama a la funci�n {@link #enviarTPDUDatosNormalNoFiable()}.
   * @return true si se ha enviado alg�n tpdu de datos.
   */
  private boolean enviarTPDUNoFiable ()
  {
    return this.enviarTPDUDatosNormalNoFiable ();
  }

  //==========================================================================
  /**
   * Comprueba en la ventana de emisi�n si hay un TPDU datos normal que enviar y
   * lo env�a.
   * @return true si ha enviado un tpdu datos normal.
   * @see #pasarBufferUsuarioAVentanaEmisionFiable
   */
  private boolean enviarTPDUDatosNormalFiable ()
  {
   final String mn = "DatosThread.enviarTPDUDatosNormalFiable";

   // Comprobar si podemos enviar TPDU nuevos:
   if ( this.lNBytesUsuarioEnviados > lRatioUsuario_Bytes_x_seg )
      {
       return false;
      }

   // Si hay datos que enviar los pasa a la ventana de emisi�n.
   pasarBufferUsuarioAVentanaEmisionFiable();

   if (this.ventanaEmision==null)
      return false;

   // Buscar el siguiente TPDUDatos a enviar de la ventana de emisi�n.
   TPDUDatosNormal tpduDatosNormal = this.ventanaEmision.siguienteTPDUDatosNormal ();

   if (tpduDatosNormal!=null) // 1� if
       {
        try {

        // Primero enviamos el MACK, si es necesario, y luego el TPDU.
        if (tpduDatosNormal.getIR())
            {
              // Ver si ya estaba registrada la r�faga.
              if (this.tablaCGLocales.getCGLocal (
                                     tpduDatosNormal.getID_SocketEmisor(),
                                     tpduDatosNormal.getNumeroRafaga())==null)
              {
               // Envio el TPDUMACK directamente, sin esperas.
               this.enviarTPDUMACK (tpduDatosNormal.getID_SocketEmisor(),
                                    tpduDatosNormal.getNumeroRafaga(),
                                    tpduDatosNormal.getNumeroSecuencia());

               // Anoto la r�faga y me establezco como su CG Local.
               this.tablaCGLocales.addRafaga (tpduDatosNormal.getID_SocketEmisor(),
                                              tpduDatosNormal.getNumeroRafaga(),
                                              tpduDatosNormal.getNumeroSecuencia(),
                                              this.id_SocketLocal);

               // Llamo a limpiar ventana.. sino puede ser que tenga que esperar a fin RTT
               limpiarVentana (tpduDatosNormal.getID_SocketEmisor());
              } // Fin del if
            } // Fin del if
          //Destino: Todo los hosts conectados a la direcci�n multicast.
          Buffer bufAEnviar = tpduDatosNormal.construirTPDUDatosNormal();
          this.socketPTMFImp.getCanalMcast().send (bufAEnviar);

          //ALEX:  depuraci�n, comentar
          if(tpduDatosNormal.getACK())
          {
            Log.debug(Log.TPDU_DATOS,"DatosThread.enviarTPDUDatosNormalFiable","--> TPDU DatosNormal (con ACK):"+tpduDatosNormal.getID_TPDU());
            //Log.debug(Log.DATOS_THREAD,"",""+tpduDatosNormal.getBufferDatos());
          }
          else
          {
            Log.debug(Log.TPDU_DATOS,"DatosThread.enviarTPDUDatosNormalFiable","--> TPDU DatosNormal:"+tpduDatosNormal.getID_TPDU());
            //Log.debug(Log.DATOS_THREAD,"",""+tpduDatosNormal.getBufferDatos());
          }



          // S�lo se lanza aqu�, puesto que este es el �nico TPDU enviado que
          // tiene que llegar a todos los miembros del grupo multicast.
          this.lanzarTimeOutInactividad ();

          if (tpduDatosNormal.getBufferDatos()!=null)
             this.lNBytesUsuarioEnviados += tpduDatosNormal.getBufferDatos().getMaxLength();

        }catch (ParametroInvalidoExcepcion pie)
             {
              Log.log (mn,pie.toString());
              return false;
             }
         catch (PTMFExcepcion e)
             {
              Log.log (mn,e.toString());
              return false;
             }
         catch (IOException ioe)
             {
              Log.log(mn,ioe.toString());
              return false;
             }

        if (tpduDatosNormal.getACK())
             {
              // A�adir a la tabla de hijos
              this.ponerEnEsperaAsentimiento (tpduDatosNormal.getID_TPDU(),
                                              tpduDatosNormal.getNumeroRafaga(),false);
             } // Fin del if

        this.actualizarListaID_TPDU2MitadRTT (tpduDatosNormal.getID_TPDU());
        return true;
        } // fin del 1� if

   return false;
  }

  //==========================================================================
  /**
   * Actualiza el temporizador de inactivadad, cancel�ndolo y volvi�ndolo a lanzar.
   * S�lo es llamada cuando se env�a un TPDU de datos normal ya que es el �nico que
   * tiene que llegar a todos los miembros del grupo multicast.
   */
  private void lanzarTimeOutInactividad ()
  {
   // bTime_out_inactividad es false si est� registrada y no ha saltado el evento,
   // o es el primero que se env�a.
   if (!bTime_out_inactividad)
    this.socketPTMFImp.getTemporizador().cancelarFuncion(this,this.iEVENTO_TIME_OUT_INACTIVIDAD);

   bTime_out_inactividad = false;

   // Lanzar temporizador para bTime_out_inactividad
   this.socketPTMFImp.getTemporizador().registrarFuncion(this.lT_TIME_OUT_INACTIVIDAD,
                                     this,
                                     this.iEVENTO_TIME_OUT_INACTIVIDAD);
  }

  //==========================================================================
  /**
   * Pasa los buffers con los datos del usuario de la cola de emisi�n a la
   * ventana de emisi�n encapsul�ndolos en TPDUs.
   */
  private void pasarBufferUsuarioAVentanaEmisionFiable()
  {
    final String mn = "DatosThread.pasarBufferUsuarioAVentanaEmisionFiable";
    ColaEmision colaEmision = this.socketPTMFImp.getColaEmision();
    Buffer buffer = null;
    RegistroBuffer regBuf = null;

    // Si ya se ha enviado un TPDU con el bit fin activado, entonces no se
    // tienen que enviar m�s TPDU.
    if (bYaEnviadoFinEmision)
      return;

    try
    {
     if (this.ventanaEmision==null)
     {
          // Si no hay ning�n dato que a�adir entonces no se crea.
          if((colaEmision == null) || (colaEmision.getTama�o()<=0))
              return;

          // Crear la ventana de emisi�n
          this.crearVentanaEmision ();

          // C�mo m�ximo puede ser el tama�o de la ventana de emisi�n menos 1,
          // para  asegurar que siempre hay un TPDU con el bit ACK activado en
          // la ventana de emisi�n.
          if (PTMF.ACKS_CADA_X_TPDUS >= this.ventanaEmision.getTama�oMaximoVentana())
               this.iACKCadaXTPDUS = this.ventanaEmision.getTama�oMaximoVentana() - 1;
          else this.iACKCadaXTPDUS = PTMF.ACKS_CADA_X_TPDUS;

          // Poner ACK al primero que se env�e si el modo es FIABLE Retrasado
          if (this.iMODO_FIABILIDAD == PTMF.PTMF_FIABLE_RETRASADO)
                  this.bSetACK = true;

          // El primero siempre deber� tener el bit IR a true
          this.bSetIR = true;
          this.iNRafaga = 1;
      }//Fin if

     // Comprobar que la ventana de Emisi�n no est� llena
     if (this.ventanaEmision==null || this.ventanaEmision.estaLlena())
      {
        // Puede ser que los TPDU de la ventana de emisi�n esten asentidos.
        this.limpiarVentana (this.id_SocketLocal);

        // HAY QUE BORRAR EL IF. ES DE PRUEBAS
        /*if(this.ventanaEmision.estaLlena())
          {
           this.socketPTMFImp.getTemporizador().sleep (1);
           Log.debug(Log.DATOS_THREAD,"VENTANA EMISION LLENA","");
          }*/
         return;
     }

     boolean bCrearTPDU = false;

     // COMPROBAR SI SE TIENE QUE CREAR Y A�ADIR A LA VTA. EMISI�N UN TPDU.
     // EN LOS SIGUIENTES CASOS SE TENDR� QUE CREAR UN TPDU, CON DATOS O NULL:
     // 1. - Si hay datos en la cola emisi�n que enviar.
     // 2. - Si no hay datos en la cola emisi�n y:
     //       Fin emisi�n es true o
     //       Expirado el temporizador de inactividad o
     //       Expirado el temporizador del �ltimo TPDU que se env�o con bit ACK a 1
     if((colaEmision != null) && (!colaEmision.vacia()))
      {
       // Obtener un buffer de la cola de emisi�n.
       regBuf = colaEmision.remove();
       if(regBuf == null)
          Log.debug(Log.DATOS_THREAD,"DatosThread --> RegistroBuffer NULO���� ","");

       if(regBuf.getBuffer()== null)
          Log.debug(Log.DATOS_THREAD,"DatosThread -->  Buffer NULO���� ","");

       buffer = regBuf.getBuffer();

       //****

       bCrearTPDU = true;
      }
     else {
           if (this.bFIN_EMISION || bTime_out_inactividad || this.bTime_out_ultimoTPDUSinACK)
                  bCrearTPDU = true;
          }

     if (!bCrearTPDU)
     {
         if(colaEmision.vacia() &&
             this.socketPTMFImp.vectorRegistroDatos.isEmpty())
         {
           Temporizador.sleep(1);
         }
         return;
     }

     // Condiciones para poner el bit ACK a 1
     /*
      1. - Si el buffer es null.
      2. - Superado el n�mero de TPDU sin ACK.
      3. - Si se ha superado un tiempo desde el �ltimo TPDU con el ACK activado
           el siguiente deber� tener el bit ACK activado.
      4. - Es el �ltimo TPDU de la r�faga.
      NOTA:
         - Superado el tiempo m�ximo para que el resto de socket de la comunicaci�n
           no piensen que hemos dejado de emitir, se enviar� uno null con el bit
           ACK activado. En este caso buffer es null.
         - Fin de emisi�n (se comprueba m�s adelante).
         - Por lo menos se tiene que enviar un TPDU con el bit ACK activo por ventana.
           Esto se asegura ajustando la variable this.iACKCadaXTPDUS a un m�ximo del
           tama�o de la ventana de emisi�n  menos 1. Realizado al construir
           la ventana de emisi�n.
     */
     if ( buffer == null
          ||
          this.iTPDUsConsecutivosSinACK >= this.iACKCadaXTPDUS
          ||
          bTime_out_ultimoTPDUSinACK
          ||
          this.iTPDUsRafaga == (PTMF.TPDUS_POR_RAFAGA-1) )

      {
        this.bSetACK = true;
      }

      // NUMERACI�N DE R�FAGAS Y BIT IR
      // Se activa el bit IR si se supera el n�mero de TPDU por r�faga. No se
      // cuentan los TPDU's null enviados.
      // IMPORTANTE: NO CAMBIAR EL ORDEN DE �STE IF Y EL SIGUIENTE
      if (this.iTPDUsRafaga >= PTMF.TPDUS_POR_RAFAGA)
       {
        this.bSetIR = true;
        this.iNRafaga ++;
        //Log.log ( "", "El n�mero de r�faga se ha incrementado:" + this.iNRafaga);
        this.iTPDUsRafaga = 0;
       }

      // Bit Fin de emisi�n.
      /* Se tiene que crear un tpdu, aunque sea null, para indicar el fin de
         la emisi�n. Adem�s, no comenzar una siguiente r�faga porque es el �ltimo
         TPDU que vamos a enviar. */
       if (buffer!=null)
         {
          if (this.bFIN_EMISION && colaEmision.getTama�o()<=0)
                 {
                   this.bSetFIN_CONEXION = true;
                   this.bSetIR  = false;
                   this.bSetACK = true;
                 }
          ++this.iTPDUsRafaga;
         }
       else {
             this.bSetIR  = false;
             if (this.bFIN_EMISION)
                {
                   this.bSetFIN_CONEXION = true;
                   this.bSetACK = true;
                }
            }

        //Crear TPDU
        TPDUDatosNormal tpdu = TPDUDatosNormal.crearTPDUDatosNormal
            (/*SocketPTMFImp*/this.socketPTMFImp,
             /*boolean bSetIR*/bSetIR,
             /*boolean bSetACK*/bSetACK,
             /*boolean bSetFIN_CONEXION*/bSetFIN_CONEXION,
             /*boolean setFIN_TRANSMISION*/(regBuf != null ? regBuf.esFinTransmision() : false),
             /*int numeroRafaga*/iNRafaga,
             /*NumeroSecuencia nSec*/this.NSecuencia,
             /*Buffer datos*/buffer);

       // -A�adir el TPDU a la ventana de Emisi�n
       this.ventanaEmision.addTPDUDatosNormal(this.NSecuencia.tolong(),tpdu);

       if (this.bSetACK)
         {
          this.iTPDUsConsecutivosSinACK = 0;

          this.bTime_out_ultimoTPDUSinACK = false;

          // -Cancelar Temporizador de Time Out �ltimo tpdu sin ack
          this.socketPTMFImp.getTemporizador().cancelarFuncion(this,this.iEVENTO_TIME_OUT_ULTIMO_TPDU_SIN_ACK);

          // -Lanzar temporizador para Time Out �ltimo tpdu sin ack
          this.socketPTMFImp.getTemporizador().registrarFuncion(PTMF.T_MAX_TPDU_SIN_ACK,
                                        this,
                                        this.iEVENTO_TIME_OUT_ULTIMO_TPDU_SIN_ACK);
         }
       else this.iTPDUsConsecutivosSinACK++;


       // -Si se ha enviado con el bit fin conexion activado indicarlo.
       if (this.bSetFIN_CONEXION)
        this.bYaEnviadoFinEmision = true;

       // Preparar para siguiente TPDU
       // 1. -Incrementar N�mero de Secuencia
       // 2. -Establecer bSetIR a false
       // 3. -Establecer bSetACK a false
      try{
       this.NSecuencia = this.NSecuencia.incrementar(1);
      }catch (ParametroInvalidoExcepcion e)
        {
         Log.log ("","EL N�MERO DE SECUENCIA NO HA PODIDO INCREMENTARSE");
        }
       this.bSetIR = false;
       this.bSetACK = false;
    }
    catch(ParametroInvalidoExcepcion e)
      { // Volver a a�adir los datos a la cola de emisi�n, pero al principio
        // de la misma.
        if (regBuf != null)
             colaEmision.addFirst(regBuf);
      }
    catch(PTMFExcepcion e)
      { // Volver a a�adir los datos a la cola de emisi�n, pero al principio
        // de la misma.
        if (regBuf != null)
             colaEmision.addFirst(regBuf);
      }
  }

  //==========================================================================
  /**
   * Crea la ventana de emisi�n.
   * @return true si ha sido creada o false en caso contrario.
   * @throws ParametroInvalidoExcepcion Lanzada si hay un error en la creaci�n
   * del n�mero de secuencia inicial al crear la ventana.
   */
  private boolean crearVentanaEmision() throws ParametroInvalidoExcepcion
   {
    final String mn = "DatosThread.crearVentanaEmision()";

     // this.socketPTMFImp.getTemporizador().tiempoActualEnMseg() devuelve un long y el n�mero de
     // secuencia est� compuesto de 32 bits, por lo que hay que recortarlo
     // a 32 bits. El n�mero inicial de la ventana estar� comprendido entre
     // 0x00000000 y 0x8FFFFFFF. Por lo que se tienen que generar 2147783647
     // n�meros de secuencia antes de que se finalicen los 32 bits ( en el caso
     // peor).

     // Si ha dejado de emitir, y vuelve a empezar, empieza en el n�mero de
     // secuencia de la �ltima sesi�n.
     if ((this.NSecuencia==null)||(this.NSecuencia.tolong() <= 0))
       {
        this.N_SECUENCIA_INICIAL = this.socketPTMFImp.getTemporizador().tiempoActualEnMseg();
        this.N_SECUENCIA_INICIAL = (this.N_SECUENCIA_INICIAL << 33) >>> 33;
        this.NSecuencia = new NumeroSecuencia(this.N_SECUENCIA_INICIAL);
       }
     else this.N_SECUENCIA_INICIAL = this.NSecuencia.tolong();

     this.iNRafaga = 1;

     // Crear la ventana de emisi�n.
     this.ventanaEmision = new VentanaEmision(PTMF.TAMA�O_VENTANA_EMISION,N_SECUENCIA_INICIAL);

     if (this.iMODO_FIABILIDAD == PTMF.PTMF_FIABLE_RETRASADO)
       {
        ID_TPDU id_tpdu = new ID_TPDU (this.id_SocketLocal,new NumeroSecuencia (this.N_SECUENCIA_INICIAL));
        this.treeMapNuevosEmisoresASincronizar.put (this.id_SocketLocal,id_tpdu);
       }

    this.lNBytesUsuarioEnviados = 0;
    this.socketPTMFImp.getTemporizador().registrarFuncionPeriodica (
                                         this, // TimerHandler
                                         1000, // Periodo en mseg
                                            0, // N�mero de Periodos (infinitos)
      this.iEVENTO_T_OUT_AJUSTAR_RATIO_USUARIO // arg1
                                            );
     return true;
    }

  //==========================================================================
  /**
   * Pasa los buffers con los datos del usuario de la cola de emisi�n a la
   * ventana de emisi�n encapsul�ndolos en TPDUs.
   */
  private void pasarBufferUsuarioAVentanaEmisionNoFiable()
  {
    final String mn = "DatosThread.pasarBufferUsuarioAVentanaEmisionNoFiable";
    ColaEmision colaEmision = this.socketPTMFImp.getColaEmision();
    Buffer buffer = null;
    RegistroBuffer regBuf = null;

    try
    {
     if (this.ventanaEmision==null)
     {
          // Si no hay ning�n dato que a�adir entonces no se crea.
          if((colaEmision == null) || (colaEmision.getTama�o()<=0))
              return;

          // Crear la ventana de emisi�n
          this.crearVentanaEmision ();
     }//Fin if

     // Comprobar que la ventana de Emision no est� llena
     if (this.ventanaEmision== null || this.ventanaEmision.estaLlena())
         return;

     // Obtener un buffer de la cola de emisi�n
     regBuf = colaEmision.remove();
     if(regBuf == null)
      return;
     buffer = regBuf.getBuffer();
     if(buffer == null)
       return;

     //Crear TPDU
     TPDUDatosNormal tpdu = TPDUDatosNormal.crearTPDUDatosNormal
            (/*SocketPTMFImp*/this.socketPTMFImp,
             /*boolean bSetIR*/false,
             /*boolean bSetACK*/false,
             /*boolean bSetFIN_CONEXION*/bSetFIN_CONEXION,
             /*boolean setFINTRANSMISION*/regBuf.esFinTransmision(),
             /*int numeroRafaga*/0,
             /*NumeroSecuencia nSec*/this.NSecuencia,
             /*Buffer datos*/regBuf.getBuffer());

      // -A�adir el TPDU a la ventana de Emisi�n
      this.ventanaEmision.addTPDUDatosNormal(this.NSecuencia.tolong(),tpdu);
      try{
        this.NSecuencia = this.NSecuencia.incrementar(1);
      }catch (ParametroInvalidoExcepcion e)
        {
         Log.log ("","EL N�MERO DE SECUENCIA NO HA PODIDO INCREMENTARSE");
        }
    }
    catch(ParametroInvalidoExcepcion e)
     {
      if (regBuf != null)
         this.socketPTMFImp.getColaEmision().addFirst (regBuf);
     }
    catch(PTMFExcepcion e)
     {
      if (regBuf != null)
         this.socketPTMFImp.getColaEmision().addFirst (regBuf);
     }
  }

  //==========================================================================
  /**
   * Comprueba en la ventana de emisi�n si hay un TPDU datos normal que enviar y
   * lo env�a. No d� fiabilidad para el tpdu datos enviado.
   * @return true si ha enviado un tpdu datos normal.
   * @see TPDUDatosNormal
   */
  private boolean enviarTPDUDatosNormalNoFiable ()
  {
   final String mn = "DatosThread.enviarTPDUDatosNormalNoFiable";

   // Comprobar si podemos enviar TPDU nuevos:
   if ( this.lNBytesUsuarioEnviados > lRatioUsuario_Bytes_x_seg )
       return false;

   pasarBufferUsuarioAVentanaEmisionNoFiable();

   if (this.ventanaEmision==null)
      return false;

   // Buscar el siguiente TPDUDatos a enviar de la ventana de emisi�n.
   TPDUDatosNormal tpduDatosNormal = this.ventanaEmision.siguienteTPDUDatosNormal ();

   if (tpduDatosNormal!=null)
       {
        try {
          // Destino: Todos los hosts conectados a la direcci�n multicast.
          Buffer bufAEnviar = tpduDatosNormal.construirTPDUDatosNormal();
          this.socketPTMFImp.getCanalMcast().send(bufAEnviar);

          if (tpduDatosNormal.getBufferDatos()!=null)
             this.lNBytesUsuarioEnviados += tpduDatosNormal.getBufferDatos().getMaxLength();

          // Conforme se van enviando los datos se eliminan de la ventana de
          // emisi�n.
          this.ventanaEmision.removeTPDUDatosNormal (tpduDatosNormal.getNumeroSecuencia().tolong());
        }catch (ParametroInvalidoExcepcion pie)
             {
              Log.debug (Log.TPDU_DATOS,mn,pie.toString());
              return false;
              }
         catch (PTMFExcepcion e)
             {
              Log.debug (Log.TPDU_DATOS,mn,e.toString());
              return false;
             }
         catch (IOException ioe)
             {
              Log.debug (Log.TPDU_DATOS,mn,ioe.toString());
              return false;
             }
        return true;
        }
   return false;
  }

  //==========================================================================
  /**
   * Realiza todas las retransmisiones que se tienen que hacer al finalizar RTT,
   * seg�n lo especificado en el protocolo. L�mita a quince el n�mero de TPDU que
   * puede retransmitir cada fin de RTT.<br>
   * Rtx. todos los asentimientos negativos por los que estemos esperando.<br>
   * Comprueba si hay asentidos en la tabla de asentimiento (s�lo habr� cuando
   * no tenga hijos jer�rquicos ni vecinos).<br>
   * @return true si ha enviado alguno.
   */
  private boolean RtxTPDUFinRTT ()
  {
   final String mn = "DatosThread.RtxTPDUFinRTT";

   if (!this.bFinRTT)
     return false;

   TreeMap treeMapTPDURtx = new TreeMap();
   TPDUDatosNormal tpduDatosNormal;

   // Rtx. el mayor de cada una de las fuentes para las que sea CG Local.
   ListaOrdID_TPDU listaID_TPDUNoAsentidos =
                                this.tablaAsentimientos.getID_TPDUNoAsentidos();

   Iterator iteradorID_TPDU = listaID_TPDUNoAsentidos.iteradorID_TPDU ();
   ID_TPDU id_tpduNext = null;
   Ventana ventana = null;
   while (iteradorID_TPDU.hasNext())
   {
      id_tpduNext = (ID_TPDU)iteradorID_TPDU.next();

      // S�lo a�ade uno por id_socket, como la lista de id_tpdu en espera de
      // asentimientos est� ordenada, nos quedamos con el de mayor NSec. S�lo
      // tenemos que ver que lo tenemos en la ventana de recepci�n.
      // Nota: TreeMap.put (indice,valor) si �ndice ten�a un valor asociado
      // lo quita y le pone el que se le indica.
      treeMapTPDURtx.put (id_tpduNext.getID_Socket(),id_tpduNext);
   } // Fin del while

   // Recorrer treeMapID_TPDURtx y enviarlos.
   NumeroSecuencia nSecMayor2MitadRTT = null;
   Iterator iterador = treeMapTPDURtx.values().iterator();
   int iCont = 15; // Como mucho Rtx. 10 TPDU
   while (iterador.hasNext() && (iCont > 0)  )
   {
      id_tpduNext = (ID_TPDU)iterador.next();

      // Al fin de RTT se RTX el mayor de cada uno de los que estoy esperando
      // por los siguientes objetivos:
      //        - Asegurar tr�fico para que se puedan detectar p�rdidas
      //        - Que los vecinos sepan quien me ha mandado ACK y qui�n no.
      //        - Los vecinos sepan quien es el CG (pueden pensar que es otro).
      //        - Que los hijos jer�rquicos sepan si han enviado un asentimiento
      //        positivo (HSACK o HACK).
      //        - En el caso de Fiable Retrasado que sepan quien est� sincronizado.

/*      // Coger de la lista this.treeMapIDs_TPDU2MitadRTT el mayor de este host
      nSecMayor2MitadRTT = (NumeroSecuencia)this.treeMapIDs_TPDU2MitadRTT.get
                                        (id_tpduNext.getID_Socket());

      if((nSecMayor2MitadRTT!=null)
         &&
        (nSecMayor2MitadRTT.compareTo(id_tpduNext.getNumeroSecuencia())>=0))
          { // Rtx. s�lo si incluye en sus listas de no asentidos a un socket
            // o idgl nuevo (no sincronizado).
            // Log.debug (Log.TPDU_RTX,mn,"llamar a rtx tpdu si no sincronizado");
            this.rtxTPDU (id_tpduNext,iRTX_SI_NO_SINCRONIZADO);
            continue;
          }
*/
      // Enviar
      // No hay que poner como opci�n iRTX_SI_NO_SINCRONIZADO porque est�
      // impl�cita en iRTX_SI_CONTIENE_ID_NO_ASENTIDOS. Adem�s si se incluye
      // tendremos que estar en modo FIABLE RETRASADO puesto que si no nunca
      // se rtx. (rtxTPDU comprueba si esa opci�n est� activada y el modo es
      // fiable retrasado)
      if ( !this.rtxTPDU (id_tpduNext,iRTX_SI_CONTIENE_ID_NO_ASENTIDOS) )
         {// Intentar Limpiar ventana. Puede ser que este asentido
          this.limpiarVentana (id_tpduNext.getID_Socket());
         }
      else iCont --; // Se ha enviado un TPDU
   } // Fin del while.

   // Comprobar si hay asentidos en la tabla de asentimiento
   // S�lo habr� cuando no tengamos hijos o vecinos.
   ListaOrdID_TPDU listaID_TPDUAsentidos = this.tablaAsentimientos.getID_TPDUAsentidos ();
   iteradorID_TPDU = listaID_TPDUAsentidos.iteradorID_TPDU ();
   id_tpduNext = null;
   while (iteradorID_TPDU.hasNext())
   {
      id_tpduNext = (ID_TPDU)iteradorID_TPDU.next();

      // No podemos salir del bucle si devuelve false porque los id_tpduNext pueden
      // ser de id_socket distintos.
      this.limpiarVentana (id_tpduNext,false);
    } // Fin del while


   // Rtx. todos los asentimientos negativos por lo que estemos esperando.
   this.gestionAsentNeg.RtxAsentNeg ();

   // Ha finalizado RTT ==> Limpiar las listas.
   this.treeMapIDs_TPDU2MitadRTT.clear ();

   this.gestionAsentNeg.vaciarIDs_AsentNeg2MitadRTT ();

   // ESTO ES PROVISIONAL, PUESTO QUE FINRTT HA PODIDO SER CAMBIADO DURANTE LA
   // EJECUCI�N DE ESTA FUNCI�N. HABR� QUE PENSAR ALGO .......................
   this.bFinRTT = false;

   return false;
  }

  //=========================================================================
  /**
   * Busca el id_tpdu en la ventana correspondiente.<br>
   * Si el que me pide no est� en la ventana correspondiente y es el
   * n�mero de secuencia inferior posible ({@link NumeroSecuencia#LIMITEINFERIOR}),
   * devuelve el primero de la ventana, si �ste tiene el bit IR activo y pertenece
   * a la r�faga 1.
   * @param id_tpdu id_tpdu a buscar, puede ser null, en cuyo caso devuelve null.
   * @return TPDU datos normal solicitado, si existe, y null en caso contrario.
   */
  private TPDUDatosNormal buscarTPDUEnVentana (ID_TPDU id_tpdu)
  {
   // Si el que me pide no est� en la ventana correspondiente y es el
   // n�mero de secuencia inferior posible, dar el primero de la ventana
   // si tiene el bit IR activo y pertenece a la r�faga 1.
   if (id_tpdu==null)
     return null;

   ID_Socket id_socket = id_tpdu.getID_Socket();
   NumeroSecuencia nSec = id_tpdu.getNumeroSecuencia();
   Ventana  ventana = null;

   if (id_socket.equals (this.id_SocketLocal))
      ventana = this.ventanaEmision;
   else
      ventana = this.tablaID_SocketsEmisores.getVentanaRecepcion (id_socket);

   if (ventana == null)
        return null;

   if (NumeroSecuencia.LIMITEINFERIOR.equals(nSec))
    {
     TPDUDatosNormal tpduDatosNormal = ventana.getTPDUDatosNormal
                                                  (ventana.getNumSecInicial());
     if (tpduDatosNormal!=null)
       if (tpduDatosNormal.getIR() || tpduDatosNormal.getNumeroRafaga()==1)
          return tpduDatosNormal;

     return null;
    }

    return ventana.getTPDUDatosNormal (nSec.tolong());
  }

  //==========================================================================
  /**
   * Env�a los tpdu datos rtx. que son solicitados por los socket�s.<br>
   * Consulta la lista {@link #listaID_TPDUSolicitadosRtxMulticast} y
   * {@link #listaID_TPDUSolicitadosRtxUnicast}.
   * @return true si ha enviado alguno.
   * @see TPDUDatosRtx
   */
  private boolean enviarTPDUDatosSolicitadosRtx ()
  {
   final String mn = "DatosThread.enviarTPDUDatosSolicitadosRtx";

   if (this.listaID_TPDUSolicitadosRtxUnicast.size()>0)
   {
      ID_TPDU id_tpdu = this.listaID_TPDUSolicitadosRtxUnicast.firstKey ();

      TreeMap treeMapID_SocketSolRtx = (TreeMap)
                  this.listaID_TPDUSolicitadosRtxUnicast.removeFirstElement();

      if (treeMapID_SocketSolRtx==null)
        return false;

      // Buscar el TPDU en la ventana
      TPDUDatosNormal tpduDatosNormal = this.buscarTPDUEnVentana (id_tpdu);

      if (tpduDatosNormal==null)
        return false;

      /*
        Destino: Hay que mandar por unicast a cada uno de los id_socket contenidos
                 en el treeMapID_SocketSolRtx
      */
      try{
        Iterator iteradorID_SocketSolRtx = treeMapID_SocketSolRtx.keySet().iterator();
        ID_Socket id_socketNext;
        Address dirUnicastDestino;
        while (iteradorID_SocketSolRtx.hasNext())
         {
          id_socketNext = (ID_Socket)iteradorID_SocketSolRtx.next();
          dirUnicastDestino = new Address (id_socketNext);
          // Destino: La direcci�n Unicast Destino
          this.socketPTMFImp.getCanalUnicast().send (tpduDatosNormal.construirTPDUDatosNormal(),
                                                     dirUnicastDestino);

          //ALEX: depuraci�n, comentar
          Log.debug(Log.TPDU_RTX,mn,"--> TPDU RTX: "+tpduDatosNormal.getID_TPDU()+" Destino UNICAST: "+dirUnicastDestino);

         }//Fin del while
       }catch (UnknownHostException e)
        {
         Log.debug (Log.TPDU_RTX,mn,""+e);
        }
        catch (IOException e)
        {
         Log.debug (Log.TPDU_RTX,mn,""+e);
        }
   } // Fin del if recibidoPorUnicast

   // Si ten�a el bit ACK activado, ya se estaba esperando por el.
   // Si lo que se est� retransmitiendo no tiene el bit ACK activado, la
   // lista de ID_TPDU que no han enviado asentimiento estar� vac�a.
   if (this.listaID_TPDUSolicitadosRtxMulticast.size()>0)
     {
      ID_TPDU id_tpdu = this.listaID_TPDUSolicitadosRtxMulticast.firstKey ();
      Integer integerTipoSolicitud = (Integer)
                                this.listaID_TPDUSolicitadosRtxMulticast.removeFirstElement();

      if (integerTipoSolicitud==null)
        return false;

      boolean bPedidoPorHijo   = (integerTipoSolicitud.intValue() & iPEDIDO_POR_HIJO) == iPEDIDO_POR_HIJO;
      boolean bPedidoPorVecino = (integerTipoSolicitud.intValue() & iPEDIDO_POR_VECINO) == iPEDIDO_POR_VECINO;
      // En las opciones especificaremos el alcance: hijos y/o vecinos.
      int iOpciones = iSIN_OPCION;

      if (!(bPedidoPorHijo && bPedidoPorVecino))
        {
         if (bPedidoPorHijo)
            iOpciones = iRTX_SOLO_A_HIJOS;
         else if (bPedidoPorVecino)
                  iOpciones = iRTX_SOLO_A_VECINOS;
        }
      return rtxTPDU (id_tpdu,iOpciones);
     }
   return false;
  }

  //==========================================================================
  /**
   * Comprueba los asentimientos positivos que se tienen que enviar.
   * De los que no es CG local, env�a un ACK directamente. Y de los que s� es
   * CG, los anota en {@link #tablaAsentimientos}.<br>
   * Consulta la lista {@link #listaAsentPositPendientesDeEnviar}. <br>
   * NOTA: los HACK se env�an en {@link #limpiarVentana (ID_TPDU,boolean)}.
   * @return true si se ha enviado alguno.
   */
  private boolean enviarAsentimientoPositivo ()
  {
   final String mn = "DatosThread.enviarAsentimientoPositivo()";

   if (this.listaAsentPositPendientesDeEnviar.size()>0)
    {
     ID_TPDU id_tpdu = null;
     int iNumeroRafaga;
     ID_Socket id_SocketCGLocal = null;

     Iterator iteradorID_TPDU = this.listaAsentPositPendientesDeEnviar.iteradorID_TPDU();
     while (iteradorID_TPDU.hasNext ())
      {
       id_tpdu  = (ID_TPDU) iteradorID_TPDU.next ();
       iNumeroRafaga = ((Integer)this.listaAsentPositPendientesDeEnviar.
                                                      get (id_tpdu)).intValue();

       id_SocketCGLocal = this.tablaCGLocales.getCGLocal (id_tpdu.getID_Socket(),
                                                          iNumeroRafaga);

       // Si todav�a no tiene asignado un CG Local, continuar con otro
       if (id_SocketCGLocal == null)
         {
           // Intentamos registrarnos como CG.
           // Si ya estaba registrado, la funci�n s�lo actualiza el n�mero
           // de secuencia inicial de la r�faga.
           this.registrarMACK (id_tpdu,iNumeroRafaga);
           continue;
         }

       VentanaRecepcion vtaRecp = this.tablaID_SocketsEmisores.getVentanaRecepcion
                                                       (id_tpdu.getID_Socket());
       if (vtaRecp==null)
         {
          iteradorID_TPDU.remove();
          continue;
         }

       // Si el CG Local es este host
       if (id_SocketCGLocal.equals (this.id_SocketLocal))
         {
          // Eliminar, no tenemos que enviar ACK.
          iteradorID_TPDU.remove ();

          // Hay que comprobar si lo tenemos en la ventana. En ese caso
          // poner en espera de asentimiento. Sino, es porque se ha recibido
          // y asentido con anterioridad. (Pusimos en espera cuando se recibio
          // un ACK).
          if (vtaRecp.nSecEntreInicialyFinal (id_tpdu.getNumeroSecuencia().tolong()))
             this.ponerEnEsperaAsentimiento(id_tpdu,iNumeroRafaga,false);
          else
             continue;

          // Puede ser que nadie tenga que mandar asentimiento.
          this.limpiarVentana (id_tpdu.getID_Socket());

          continue;
         }

       // Comprobar si consecutivo y entregado al usuario: Hasta que no se
       // entrege al usuario no se asiente.
       if (vtaRecp.esConsecutivo(id_tpdu.getNumeroSecuencia().tolong())
           &&
           this.tablaID_SocketsEmisores.entregadoAlUsuario (id_tpdu))
         {
          // Eliminar de this.listaAsentPositPendientesDeEnviar.
          iteradorID_TPDU.remove ();

          // Formar el ACK y enviarlo
          try {
           TPDUACK tpduACK = TPDUACK.crearTPDUACK (
                              this.socketPTMFImp,
                              iNumeroRafaga,
                              id_tpdu.getID_Socket().getDireccion(),
                              id_tpdu.getNumeroSecuencia(),
                              id_tpdu.getID_Socket().getPuertoUnicast());

            /* Destino: Por unicast al CG Local. La informaci�n del socket
                        destino est� en la variable id_SocketCGLocal
            */
            Address dirUnicastDestino = new Address (id_SocketCGLocal);
            this.socketPTMFImp.getCanalUnicast().send (tpduACK.construirTPDUACK(),
                                                       dirUnicastDestino);

           //     Log.log ("","ENVIADO UN ACK UNICAST: " + id_tpdu );

          //ALEX: depuraci�n, comentar
          Log.debug(Log.ACK,"--> TPDU ACK: "+tpduACK.getID_TPDUFuente(),"Destino UNICAST: "+dirUnicastDestino);

            this.limpiarVentana (id_tpdu,false);

          }catch (ParametroInvalidoExcepcion pie)
             {
              Log.debug (Log.ACK,mn,pie.toString());
              return false;
             }
           catch (PTMFExcepcion e)
             {
              Log.debug (Log.ACK,mn,e.toString());
              return false;
             }
           catch (IOException ioe)
             {
              Log.debug (Log.ACK,mn,ioe.toString());
              return false;
             }
          return true; // S�lo enviamos el primero que lo cumpla.
         }
       else {
             // NO EST�N TODOS CONSECUTIVOS
            }
      } // Fin del while
     } // Fin del if
   return false;
  }

  //==========================================================================
  /**
   * Comprueba si hay que enviar asentimientos negativos, en cuyo caso env�a
   * uno.<br>
   * Ejecuta el m�todo {@link GestionAsentNeg#enviarNuevoAsentNeg()}.
   * @return true si se ha enviado alguno.
   */
  private boolean enviarAsentNeg ()
  {
   return
        this.gestionAsentNeg.enviarNuevoAsentNeg ();
  }

  //==========================================================================
  /**
   * Comprueba si hay que enviar {@link TPDUMACK} y lo env�a. <br>
   * Consulta el vector {@link #vectorMACK}.
   * @return true si se ha enviado alguno.
   */
  private boolean enviarTPDUMACK ()
  {
   final String mn = "DatosThread.enviarTPDUMACK";

   if (this.lTRandomMACK>0)
        return false; // Hay que esperar.

   if (this.vectorMACK.size ()>0) // 1� if
    {
     RegistroVectorMACK reg = (RegistroVectorMACK)this.vectorMACK.remove(0);
     // Lo envio.
     this.enviarTPDUMACK (reg.id_tpdu.getID_Socket (),       // id_socketSrc
                          reg.iNRafaga,                      // N�mero de r�faga
                          reg.id_tpdu.getNumeroSecuencia()); // NSecInicial R�faga

     // Lo registro en la tabla de CG locales. Es enviado por este socket
     this.tablaCGLocales.addRafaga (reg.id_tpdu.getID_Socket(),
                                    reg.iNRafaga,
                                    reg.id_tpdu.getNumeroSecuencia(),
                                    this.id_SocketLocal);

     // Llamo a limpiar ventana.. sino puede ser que tenga que esperar a fin RTT
     // Puede ser que nadie tenga que mandar asentimiento.
     limpiarVentana (reg.id_tpdu.getID_Socket());

     if (this.vectorMACK.size ()>0) // Quedan m�s MACK por enviar.
      {
       // Si no hay ning�n vecino, no hay que esperar ning�n tiempo.
       if (this.getCGLThread().numeroVecinos()==0)
            {
             this.lTRandomMACK = 0;
            }
       else {
             this.lTRandomMACK = (this.random.nextInt (PTMF.MAX_TRANDOM_MACK-PTMF.T_BASE)+PTMF.T_BASE)
                                 % PTMF.MAX_TRANDOM_MACK;
             this.socketPTMFImp.getTemporizador().registrarFuncion (this.lTRandomMACK,this,
                                        this.iEVENTO_FIN_T_RANDOM_MACK);
             }
       }
     return true;
     } // Fin 1� if

   return false;
  }

  //==========================================================================
  /**
   * Forma un TPDUMACK con la informaci�n pasada en los argumentos
   * y lo env�a.<br>
   * <b> SOLO ENVIA EL MACK SIN MAS.</b>
   * @param id_SocketSrc id_socket fuente de la r�faga.
   * @param iNumeroRafagaParam r�faga.
   * @param numSecInicialRafaga n�mero de secuencia inicial de la r�faga.
   * @return true si ha podido formar el TPDUMACK y enviarlo.
   */
  private boolean enviarTPDUMACK (ID_Socket id_SocketSrc,int iNumeroRafagaParam,
                                  NumeroSecuencia numSecInicialRafaga)
  {
   final String mn = "DatosThread.enviarTPDUMACK (id_SocketSrc,numRaf,nSecIniRaf)";

   // Formar TPDUMACK.
   try{
    TPDUMACK tpduMACK = TPDUMACK.crearTPDUMACK (this.socketPTMFImp,
                                                id_SocketSrc.getDireccion(),
                                                iNumeroRafagaParam,
                                                id_SocketSrc.getPuertoUnicast(),
                                                numSecInicialRafaga);
    // Enviarlo.
    /* Destino: Todos los vecinos (mismo IDGL) pertenecientes al GRUPO LOCAL.
                Si el n�mero de vecinos es cero, no se env�a a la red.
    */
    Buffer bufAEnviar = tpduMACK.construirTPDUMACK();
    if (this.getCGLThread().numeroVecinos()>0)
      {
       this.socketPTMFImp.getCanalMcast().send (bufAEnviar,(byte)this.getCGLThread().getTTLSocketsGL());

       //  Log.log ("","ENVIADO UN MACK");

         //ALEX: depuraci�n, comentar
          Log.debug(Log.MACK,mn,"--> TPDU MACK: "+tpduMACK.getID_SocketFuente());

      }
    return true;
   }catch (ParametroInvalidoExcepcion pie)
             {
              Log.debug (Log.MACK,mn,pie.toString());
              return false;
              }
    catch (PTMFExcepcion e)
             {
              Log.debug (Log.MACK,mn,e.toString());
              return false;
             }
    catch (IOException ioe)
             {
              Log.debug (Log.MACK,mn,ioe.toString());
              return false;
             }
  }

  //==========================================================================
  /**
   * Env�a los TPDU HSACK que est�n pendientes. Puede enviar m�s de uno.
   * @param true si env�o alguno.
   */
  private boolean enviarTPDUHSACK ()
  {
   final String mn = "DatosThread.enviarTPDUHSACK ()";

   // Obtener la lista de los que no hemos enviado HSACK:
   // Comprobar si este host los ha recibido todos,
   Iterator iteradorRegistrosHSACK = this.listaHSACKPendientesDeEnviar.iteradorObjetos ();
   ID_Socket id_socketCGLocal = null;
   boolean bCondicion;


   while (iteradorRegistrosHSACK.hasNext())
    {
     RegistroHSACK reg = (RegistroHSACK)iteradorRegistrosHSACK.next ();
     // Buscar el CG Local.
     id_socketCGLocal =
           this.tablaCGLocales.getCGLocal (reg.id_tpdu.getID_Socket(),reg.iNRafaga);

     bCondicion = false;
     // Si somos su CG Local y no habiamos enviado HSACK ==> enviarlo.
     if ( (id_socketCGLocal!=null)
           &&
           id_socketCGLocal.equals (this.id_SocketLocal)) // 1� if
       { // Somos su CG Local.
         // Puede ser que este id_tpdu no est� en espera de asentimiento (por ser
         // antiguo o cambios de CG Local), pero el padre-s me est� pidiendo
         // que se lo asienta. Si lo hemos recibido le enviamos un HSACK.
        if (this.tablaAsentimientos.contieneID_TPDU (reg.id_tpdu)) // 2� if
          {
           // Comprobar si ya hab�amos enviado HSACK.
           if (this.tablaAsentimientos.enviadoHSACK (reg.id_tpdu)) // 3� if
           {
             // Ver si lo han pedido por rtx. en cuyo caso hay que re-enviarlo
             // La condici�n es true puesto que anteriormente ya se hab�a enviado
             // por lo que seguro que cumple las condiciones.
             if (reg.bPedidoRtx)
                  bCondicion = true;
             else {
                   iteradorRegistrosHSACK.remove();
                   continue;
                  } // Fin del if
           } // Fin del 3� if
           else
              // Comprobar si alg�n vecino lo ha asentido con ACK.
              bCondicion = this.tablaAsentimientos.algunACKID_Socket (reg.id_tpdu);
          } // Fin del 2� if

        if (!bCondicion)
         {// Comprobar si lo tenemos todos hasta este.
          VentanaRecepcion vtaRecp =
            this.tablaID_SocketsEmisores.getVentanaRecepcion (reg.id_tpdu.getID_Socket());

          // EsConsecutivo devuelve true si es menor que el inicial de la ventana
          // o la ventana contiene todos los tpdu hasta el indicado.
          if (vtaRecp!=null)
           bCondicion = vtaRecp.esConsecutivo (reg.id_tpdu.getNumeroSecuencia().tolong());
         } // Fin del if.
       }
      else { // PUEDE O NO TENER CG LOCAL ASIGNADO
             if (!reg.bPedidoRtx)
                 {
                  // Si no tiene CG Local asignado no lo borro.
                  if (id_socketCGLocal != null)
                     iteradorRegistrosHSACK.remove();
                  continue;
                 }
             // Enviar el HSACK por si se ha caido el CG Local, con ello evitamos
             // que el padre jer�rquico descarte a todo el grupo local.
             VentanaRecepcion vtaRecp =
               this.tablaID_SocketsEmisores.getVentanaRecepcion (reg.id_tpdu.getID_Socket());

             // EsConsecutivo devuelve true si es menor que el inicial de la ventana
             // o la ventana contiene todos los tpdu hasta el indicado.
             if (vtaRecp!=null)
                bCondicion = vtaRecp.esConsecutivo (reg.id_tpdu.getNumeroSecuencia().tolong());
           } // Fin del 1� if

     if (bCondicion)
      {// Enviar HSACK
       iteradorRegistrosHSACK.remove();
       try{
        // Coger de la lista de padres y mandar el HSACK
        // Obtenemos los padres para el IDGL dado.
        IDGL idglFuente = this.getIDGL (reg.id_tpdu.getID_Socket());

        if (idglFuente==null)
            continue;

        // Deber� existir la tarea CGL Thread.
        TreeMap treemapPadres = this.socketPTMFImp.getCGLThread().getCGPadres(idglFuente);
        if( treemapPadres != null)
         {
          // Obtenemos el TTL m�ximo de esos padres
          int TTLMaxPadres = this.socketPTMFImp.getCGLThread().
                                                 getTTLGLMaximo(treemapPadres);

          // Formar el TPDUHSACK
          TPDUHSACK tpduHSACK = TPDUHSACK.crearTPDUHSACK (
                                     this.socketPTMFImp,
                                     reg.id_tpdu.getID_Socket().getDireccion(),
                                     reg.id_tpdu.getNumeroSecuencia(),
                                     reg.id_tpdu.getID_Socket().getPuertoUnicast(),
                                     reg.iNRafaga);
          //Enviar el TPDU con el TTL correspondiente.
          /* Destino: A todos los padres jer�rquicos para el id_tpdu que
                      estamos asintiendo.
             */
          Buffer bufAEnviar = tpduHSACK.construirTPDUHSACK();
          this.socketPTMFImp.getCanalMcast().send(bufAEnviar,(byte)TTLMaxPadres);

          //ALEX: depuraci�n, comentar.
          Log.debug(Log.HSACK,mn,"--> TPDU HSACK"+ tpduHSACK.getID_TPDUFuente());

          //Log.log (mn,"ENVIADO UN HSACK: " + tpduHSACK.getID_TPDUFuente());

          // Anotar como que el HSACK ha sido enviado. Si no pertenece a la tabla
          // de asentimientos no pasa nada.
          this.tablaAsentimientos.setEnviadoHSACK (reg.id_tpdu);
         }// Fin del if
       }catch (ParametroInvalidoExcepcion pie)
             {
              Log.debug (Log.HSACK,mn,pie.toString());
              continue;
              }
        catch (PTMFExcepcion e)
             {
              Log.debug (Log.MACK,mn,e.toString());
              continue;
             }
        catch (IOException ioe)
             {
              Log.debug (Log.MACK,mn,ioe.toString());
              continue;
             }
       this.limpiarVentana (reg.id_tpdu,false);
      } // Fin del if
   } // Fin del while;

   return false;
  }

  //==========================================================================
  /**
   * Forma con los par�metros un TPDUHACK y lo env�a.
   * @param id_tpduFuente ID_TPDU que se est� asintiendo.
   * @param iNumeroRafagaFuente n�mero de r�faga a la que pertenece id_tpduFuente.
   * @param idglFuente IDGL al que pertenece el socket fuente de id_tpduFuente.
   * @return true si ha enviado alguno.
   */
  private boolean enviarTPDUHACK (ID_TPDU id_tpduFuente,int iNumeroRafagaFuente,
                                  IDGL idglFuente)
  {
   final String mn = "DatosThread.enviarTPDUHACK";

   if ((id_tpduFuente==null)||(idglFuente==null))
     return false;

   // Formar el TPDUHACK
   try{
     TPDUHACK tpduHACK = TPDUHACK.crearTPDUHACK (
                               this.socketPTMFImp,
                               id_tpduFuente.getID_Socket().getDireccion(),
                               id_tpduFuente.getNumeroSecuencia(),
                               id_tpduFuente.getID_Socket().getPuertoUnicast(),
                               iNumeroRafagaFuente);

     if (tpduHACK==null)
         return false;

     // Eliminar todos los HSACK que estuviesen pendientes y a los que este
     // HACK sustituye.
     this.listaHSACKPendientesDeEnviar.getSublistaMenorIgual
                        (id_tpduFuente.getID_Socket(),
                         id_tpduFuente.getNumeroSecuencia()).clear ();

     // Coger de la lista de padres y mandar el HACK
     // Obtenemos los padres para el IDGL dado.
     // Obtenemos el TTL m�ximo de entre todos los padres jer�rquicos.
     TreeMap treemapPadres = this.socketPTMFImp.getCGLThread().getCGPadres(idglFuente);
     if( treemapPadres != null)
       {
        // Obtenemos el TTL m�ximo de esos padres
        int iTTLMaxPadres = this.socketPTMFImp.getCGLThread().getTTLGLMaximo(treemapPadres);

        //Enviar el TPDU con el TTL correspondiente.
        /* Destino: A todos los padres jer�rquicos para el id_tpdu que
                    estamos asintiendo.
        */
        Buffer bufAEnviar = tpduHACK.construirTPDUHACK();
        this.socketPTMFImp.getCanalMcast().send(bufAEnviar,(byte)iTTLMaxPadres);

        //ALEX: depuraci�n, comentar.
        Log.debug(Log.HACK,mn,"--> TPDU HACK"+ tpduHACK.getID_TPDUFuente());

        return true;
       }
     }catch (ParametroInvalidoExcepcion pie)
             {
              Log.debug (Log.HACK,mn,pie.toString());
              return false;
             }
     catch (PTMFExcepcion e)
             {
              Log.debug (Log.HACK,mn,e.toString());
              return false;
             }
     catch (IOException ioe)
             {
              Log.debug (Log.HACK,mn,ioe.toString());
              return false;
             }
   return false;
  }

  //===========================================================================
  /**
   * Elimina de todas las tablas aquellos sockets que se detecten que ya no
   * pertenecen al grupo, por los motivos que sea.<br>
   * Consulta la lista de {@link #listaIDs_TPDUNoRecibidoAsentimiento}.
   */
  private void comprobarlistaIDs_TPDUNoRecibidoAsentimiento ()
  {
    // Consulta la lista this.listaIDs_TPDUNoRecibidoAsentimiento para ver quien
    // no ha asentido los id_TPDU contenidos en la lista y eliminarlos.
    final String mn = "DatosThread.comprobarlistaIDs_TPDUNoRecibidoAsentimiento()";

    if (!((this.iMODO_FIABILIDAD==PTMF.PTMF_FIABLE)||(this.iMODO_FIABILIDAD==PTMF.PTMF_FIABLE_RETRASADO)))
      return;

    this.mutex.lock();
    Iterator iteradorID_TPDU = this.listaIDs_TPDUNoRecibidoAsentimiento.iteradorID_TPDU ();

    // Somos CG Local de todos los ID_TPDU que figuran en
    // listaIDs_TPDUNoRecibidoAsentimiento. Puede ocurrir que hayamos dejado de ser
    // CG Local de alguno, pero no influye en el algoritmo, puesto que entonces
    // las listas de los que no me lo han asentido estar�n vacias, no eliminando
    // a nadie, salvo a la fuente del tpdu.

    ID_TPDU id_tpdu = null;
    boolean bCondicion = false;
    while (iteradorID_TPDU.hasNext())
     {
      id_tpdu = (ID_TPDU)iteradorID_TPDU.next();
      Log.debug(Log.TEMPORIZADOR,mn,"ID_TPDU que ha vencido de oportunidades: " + id_tpdu);
      iteradorID_TPDU.remove();

      // Comprobar si lo tenemos: condicion indicar� si lo tenemos (true)
      // o si no (false)

      // Comprobar si somos la fuente ==> Lo tenemos en la vta. emisi�n.
      bCondicion = id_tpdu.getID_Socket().equals (this.id_SocketLocal);

      if (bCondicion && this.ventanaEmision == null)
      {
        // No somos emisores.
        continue;
      }

      if (!bCondicion)// Comprobar si lo hemos recibido en la vta. de recepci�n.
      {
         VentanaRecepcion vtaRecp =
           this.tablaID_SocketsEmisores.getVentanaRecepcion (id_tpdu.getID_Socket());

         if (vtaRecp!=null)
           // Comprobar si est� consecutivo. ?�?�?�?
           bCondicion = vtaRecp.esConsecutivo (id_tpdu.getNumeroSecuencia().tolong());
         else
           continue; // No existe el emisor
      } // Fin del if


      if (!bCondicion)
       // Eliminar el id_socket fuente del tpdu.
       { // No es necesario limpiar la ventana puesto que se elimina la fuente
         // y con ella la ventana de recepci�n asociada, mediante el listener
         // de CGLThread.
         Log.debug(Log.DATOS_THREAD,mn,"Eliminar ID_SOCKET: " + id_tpdu.getID_Socket()+" Motivo:?�?�?�");
         this.socketPTMFImp.getCGLThread().removeID_SOCKET(id_tpdu.getID_Socket());
       }
      else
       // Eliminar los que no hayan enviado asentimiento para id_tpdu.
       {
        TreeMap treeMapID_SocketNoEnviadoACK   =
             this.tablaAsentimientos.getTreeMapID_SocketsNoEnviadoACK (id_tpdu);
        TreeMap treeMapIDGLNoEnviadoHACKoHSACK =
             this.tablaAsentimientos.getTreeMapIDGLNoEnviadoHACKoHSACK (id_tpdu);

         // Eliminar cada uno de los que no haya recibido asentimiento.
         Iterator iteradorID_Socket = treeMapID_SocketNoEnviadoACK.
                                                            keySet().iterator();

         Iterator iteradorIDGL = treeMapIDGLNoEnviadoHACKoHSACK.
                                                            keySet().iterator();

         // Limpiar la ventana. Sin tener que verificar si est�n asentido
         // Convertir en HACK los HSACK recibidos para id_tpdu
         this.tablaAsentimientos.convertirHSACKaHACK (id_tpdu);
         this.limpiarVentana (id_tpdu,true);

         ID_Socket id_socket = null;
         // Eliminar los sockets de los que no he recibido asentimiento.
         while (iteradorID_Socket.hasNext())
          {
           id_socket = (ID_Socket)iteradorID_Socket.next();
           Log.debug(Log.DATOS_THREAD,mn,"Eliminar ID_SOCKET: " + id_socket+" Motivo: no he recibido ACK para el ID_TPDU "+id_tpdu+" en el n�mero m�ximo de intentos (RTT*"+PTMF.OPORTUNIDADES_RTT+")");
           this.socketPTMFImp.getCGLThread().removeID_SOCKET(id_socket);
           } // Fin del while

         IDGL idgl = null;
         while (iteradorIDGL.hasNext())
            {
             idgl = (IDGL)iteradorIDGL.next();
             //Log.log (mn,"2.2- VOY A LLAMAR A REMOVE IDGL: " + idgl);
             Log.debug(Log.DATOS_THREAD,mn,"Eliminar IDDL: " + idgl+" Motivo: no he recibido HACK para el ID_TPDU "+id_tpdu+" en el n�mero m�ximo de intentos (RTT*"+PTMF.OPORTUNIDADES_RTT+")");
             this.socketPTMFImp.getCGLThread().removeIDGL (idgl);
            } // Fin del while
        } // Fin del if
    } // Fin del while

    this.mutex.unlock();
    // Eliminar los sockets que no han enviado los id_tpdu solicitados.
    // Nunca ser� uno de la ventana de emisi�n.
    // NOTA: LA CULPA HA PODIDO SER DEL IDGL PADRE QUE SE HA CAIDO, PERO NO,
    // PORQUE EN �LTIMA INSTANCIA SON PEDIDOS POR UNICAST A LA FUENTE.
    ListaOrdID_TPDU listaID_TPDU = gestionAsentNeg.removeIDs_TPDUAgotados();
    if (listaID_TPDU==null)
        return;
    iteradorID_TPDU = listaID_TPDU.iteradorID_TPDU ();
    while (iteradorID_TPDU.hasNext())
     {
      id_tpdu = (ID_TPDU)iteradorID_TPDU.next ();
      iteradorID_TPDU.remove();
      // Eliminar:
       Log.debug(Log.DATOS_THREAD,mn,"Eliminar ID_SOCKET: " + id_tpdu.getID_Socket()+" Motivo: no he recibido los TPDUS solicitados");
       this.socketPTMFImp.getCGLThread().removeID_SOCKET (id_tpdu.getID_Socket());
     } // Fin del while


  }


  //===========================================================================
  /**
   * Funci�n Callback que se registra en el temporizador.
   * Actualiza variables y listas de acuerdo con el evento indicado en lEvento.<br>
   * <b>Este m�todo es ejecutado por el thread del {@link Temporizador}.</b>
   * @param lEvento Indica el tipo de evento.
   * @param o Object asociado, ser� tenido en cuenta dependiendo de lEvento.
   */
  public void TimerCallback(long lEvento, Object o)
  {
    // MUY IMPORTANTE:
    //
    //  ESTA FUNCI�N ES EJECUTADA POR EL THREAD DEL TEMPORIZADOR
    //  TODO EL C�DGIGO QUE SE EJECUTE AQUI DEBE DE SER M�NIMO!!!
    //
    //  SE RECOMIENDA SOLO ESTABLECER FLAGS Y ALGUNAS VARIABLES,
    //  EL RESTO DEL C�DIGO DEBER� DE SER PROCESADO EN CADA VUELTA
    //  DEL THREAD DE DATOS_THREAD.
    //

   final String mn = "DatosThread.TimerCallback";

   // Comprobar el evento
   switch ((int)lEvento)
        {
         case iEVENTO_RTT: // Ha transcurrido RTT
            this.bFinRTT = true;
            break;
         case iEVENTO_FIN_INTENTOS:
            Log.debug (Log.TEMPORIZADOR,mn,"FIN INTENTOS: " + o);
            if (o instanceof ID_TPDU)
              {
               ID_TPDU id_tpdu = (ID_TPDU)o;
               this.mutex.lock();
               this.listaIDs_TPDUNoRecibidoAsentimiento.put(id_tpdu,null);
               this.mutex.unlock();
              }
            break;
         case iEVENTO_FIN_T_RANDOM_MACK:
            this.lTRandomMACK = 0;
            break;
         case iEVENTO_T_MIN_ENTRE_ENVIOS:
            this.lT_Min_Entre_Envios = 0;
            break;
         case iEVENTO_COMPROBAR_INACTIVIDAD_EMISORES:
            this.bComprobarInactividadEmisores = true;
            break;
         case iEVENTO_TIME_OUT_INACTIVIDAD:
            this.bTime_out_inactividad = true;
            break;
         case iEVENTO_TIME_OUT_ULTIMO_TPDU_SIN_ACK:
            this.bTime_out_ultimoTPDUSinACK = true;
            break;
         case iEVENTO_T_OUT_AJUSTAR_RATIO_USUARIO:
            this.lNBytesUsuarioEnviados = 0;
            this.lT_Out_Ajustar_Ratio_Usuario = 0;
            break;
         case iEVENTO_FIN_SINCRONIZACION_ID_SOCKET:
            if (o instanceof ID_Socket)
              {
               ID_Socket id_socket = (ID_Socket)o;
               if (this.treeMapID_SocketNoSincronizado != null)
                   this.treeMapID_SocketNoSincronizado.remove (id_socket);
              }
            break;
         case iEVENTO_FIN_SINCRONIZACION_IDGL:
            if (o instanceof IDGL)
              {
               IDGL idgl = (IDGL)o;
               if (this.treeMapIDGLNoSincronizado != null)
                     this.treeMapIDGLNoSincronizado.remove (idgl);
              }
            break;
         default:  // No puede ocurrir.
        } /* Fin del switch */
  }

  //==========================================================================
  /**
   * Registra para enviar un TPDUMACK. Asegura los tiempos aleatorios. No env�a
   * el MACK.
   * @param id_TPDU contiene el id_socket fuente y el n�mero de secuencia inicial
   * del MACK que se registra.
   * @param iNumeroRafagaParam n�mero de r�faga del MACK que se registra.
   * @see #enviarTPDUMACK
   */
  private void registrarMACK (ID_TPDU id_TPDU,int iNumeroRafagaParam)
  {
  final String mn = "DatosThread.registrarMACK (id_tpdu,numRaf)";

  if ((id_TPDU==null) || (iNumeroRafagaParam < 0))
        return ;

  if (this.getAbandonarComunicacion())
      {
        this.vectorMACK.clear();
        return;
      }

   // Comprobar si ya estaba en el vector un MACK para la misma r�faga
   // y en caso afirmativo actualizar el n�mero de secuencia inicial.
   for (int i=0;i<this.vectorMACK.size();i++)
   {
    RegistroVectorMACK regNext = (RegistroVectorMACK)this.vectorMACK.get (i);

    if (regNext.id_tpdu.getID_Socket().equals (id_TPDU.getID_Socket())
        &&
        regNext.iNRafaga==iNumeroRafagaParam)
      {
       // Comprobar el n�mero de secuencia y quedar el menor.
       if (regNext.id_tpdu.getNumeroSecuencia().
                        compareTo(id_TPDU.getNumeroSecuencia()) > 0)
          { // El nSec en el MACK registrado es mayor.
           this.vectorMACK.remove (i);
           break;
          }
       else return;
      } // Fin del if.
   } // Fin del for.

   // Almacenar un registro en el VectorMACK
   RegistroVectorMACK reg = new RegistroVectorMACK ();

   reg.id_tpdu  = id_TPDU;
   reg.iNRafaga = iNumeroRafagaParam;

   this.vectorMACK.add (reg);

   if (this.lTRandomMACK>0)
        { // Se est� esperando para mandar un MACK.
        }
   else { // Esperar un tiempo aleatorio para mandar MACK.
          // Si no hay ning�n vecino, no hay que esperar ning�n tiempo.
          if (this.getCGLThread().numeroVecinos()==0)
            {
             this.lTRandomMACK = 0;
            }
          else {
                this.lTRandomMACK = (this.random.nextInt (PTMF.MAX_TRANDOM_MACK-PTMF.T_BASE)+PTMF.T_BASE)
                         % PTMF.MAX_TRANDOM_MACK;

                this.socketPTMFImp.getTemporizador().registrarFuncion (this.lTRandomMACK,this,
                                          this.iEVENTO_FIN_T_RANDOM_MACK);
               }
          }
  }

  //==========================================================================
  /**
   * Elimina todos los MACK registrados para ser enviados cuya fuente sea la
   * indicada.
   * @id_socket id_socket a eliminar de los MACK registrados.
   */
  private void removeMACK (ID_Socket id_socket)
  {
   final String mn = "DatosThread.removeMACK (ID_Socket)";

   if (id_socket==null)
     return;

   RegistroVectorMACK reg = null;
   boolean bEliminadoPrimerMACK = false;


   if (this.vectorMACK.size()>0)
    {
     reg = (RegistroVectorMACK)this.vectorMACK.firstElement();

     if (reg.id_tpdu.getID_Socket().equals (id_socket))
      {
       this.vectorMACK.remove (0); // Borro el MACK
       bEliminadoPrimerMACK = true;
      } // Fin del if
    } // Fin del if
   else return;

   for (int i=0;i<this.vectorMACK.size();i++)
    {
      reg = (RegistroVectorMACK)this.vectorMACK.get (i);

      if (reg.id_tpdu.getID_Socket().equals (id_socket))
         this.vectorMACK.remove (i); // Borro el MACK
    } // Fin del for

   if (bEliminadoPrimerMACK)
    {
     if (this.lTRandomMACK>0)
       {
        this.lTRandomMACK = 0;
        this.socketPTMFImp.getTemporizador().cancelarFuncion (this,this.iEVENTO_FIN_T_RANDOM_MACK);
       } // Fin de if
     if (this.vectorMACK.size()>0)
       {// Esperar un tiempo aleatorio para mandar MACK.
       if (this.getCGLThread().numeroVecinos()==0)
            {
             this.lTRandomMACK = 0;
            }
        else{
             this.lTRandomMACK = (this.random.nextInt (PTMF.MAX_TRANDOM_MACK-PTMF.T_BASE)+PTMF.T_BASE)
                             % PTMF.MAX_TRANDOM_MACK;
             this.socketPTMFImp.getTemporizador().registrarFuncion (this.lTRandomMACK,this,iEVENTO_FIN_T_RANDOM_MACK);
            }
      } // Fin del if
   } // Fin del if
  }

  //==========================================================================
  /**
   * Comprueba si hay un MACK pendiente de enviar para id_socket,r�faga
   * @return true si lo hay.
   */
  private boolean hayMACKPendientesDeEnviar (ID_Socket id_Socket,int iNumeroRafagaParam)
  {
   if (id_Socket==null)
      return false;

   if (this.vectorMACK.size ()>0)
    {
      RegistroVectorMACK reg = null;
      for (int i=0;i<this.vectorMACK.size();i++)
       {
        reg = (RegistroVectorMACK)this.vectorMACK.get (i);
        if (reg.id_tpdu.getID_Socket().equals (id_Socket)
            &&
           (reg.iNRafaga==iNumeroRafagaParam) )
         {
           return true;
         }
       } // Fin del for.
     } // Fin del if

    return false;
  }

  //==========================================================================
  /**
   * Analiza el id_TPDU para saber si hay alg�n n�mero de secuencia
   * que no se haya recibido. Comprueba si es continuaci�n del �ltimo TPDUDatos
   * que se recibi� de la fuente del id_TPDU, si lo es, no ha habido p�rdidas, y sino,
   * se han perdido los TPDUDatos comprendidos entre el �ltimo recibido y este.
   * @param tpduDatos TPDUDatos a analizar
   */
  private void comprobarID_TPDUNoRecibidos (ID_TPDU id_TPDU)
  {
    final String mn = "DatosThread.comprobarID_TPDUNoRecibidos (id_tpdu)";
    NumeroSecuencia nSecFinal = null;

    ID_Socket       id_Socket = id_TPDU.getID_Socket ();
    NumeroSecuencia numSec    = id_TPDU.getNumeroSecuencia ();

    VentanaRecepcion vtaRecp = this.tablaID_SocketsEmisores.getVentanaRecepcion(id_Socket);

    // Si es consecutivo, no hay que hacer nada.
    if ( (vtaRecp==null) ||
         vtaRecp.esConsecutivo (numSec.tolong()) )
            return;

    // Ver los que faltan en la ventana de recepci�n hasta este.
    // Es suficiente con comprobar desde el �ltimo recibido, y no de los consecutivos,
    // puesto que los que no se hayan recibido anteriores al �ltimo ya se habr�n
    // comprobado.
    Vector vectorNumsSecNoRecibidos = vtaRecp.getNumsSecNoRecibidos (
                                                 id_TPDU.getNumeroSecuencia());
    // Actualizar this.gestionAsentNeg
    ID_TPDU id_tpduAux;
    for (int i=0;i<vectorNumsSecNoRecibidos.size();i++)
     {
       try{
        id_tpduAux = new ID_TPDU (id_TPDU.getID_Socket(),
                                   (NumeroSecuencia)vectorNumsSecNoRecibidos.get(i));
        this.gestionAsentNeg.addID_TPDU (id_tpduAux);
       }catch (ParametroInvalidoExcepcion pie)
           {
            Log.debug (Log.NACK,mn,pie.toString());
           }
     } // Fin del for
  }

  //===========================================================================
  /**
   * Comprueba si el hilo tiene que terminar. Para que el thread finalice tiene
   * que haberse llamado a la funci�n {@link #stopThread(boolean)} previamente.
   * Seg�n el modo de finalizaci�n indicado en la llamada a stopThread devolver�:
   *  <li>No Estable : Devuelve true.</li>
   *  <li>Estable    : Devuelve true, si la cola de emisi�n y la ventana de
   * emisi�n est�n vacias, y adem�s no se est� esperando recibir asentimiento
   * por ning�n ID_TPDU.</li>
   * </ul>
   * @return true si el thread puede finalizar.
   */
  private boolean finComunicacion ()
  {
   final String mn = "DatosThread.finComunicacion()";

   if (this.bABANDONAR_COMUNICACION!=true)
        return false;

    // Esperar hasta que la ventana de emisi�n, la cola de emisi�n y la tabla de
    // asentimientos est�n vac�as.
    if (this.bORDENADO)
     {
       // Comprobar si hay asentidos en la tabla de asentimiento
       ListaOrdID_TPDU listaID_TPDUAsentidos = this.tablaAsentimientos.getID_TPDUAsentidos ();
       Iterator iteradorID_TPDU = listaID_TPDUAsentidos.iteradorID_TPDU ();
       ID_TPDU id_tpduNext = null;
       while (iteradorID_TPDU.hasNext())
        {
         id_tpduNext = (ID_TPDU)iteradorID_TPDU.next();

         // No podemos salir del bucle si devuelve false porque los id_tpduNext
         // pueden ser de id_socket distintos.
         this.limpiarVentana (id_tpduNext,false);
        } // Fin del while

        // ��??
        if ((this.ventanaEmision==null)
             &&
            (this.socketPTMFImp.getColaEmision().getTama�o()==0))
            return true;

        if ((this.socketPTMFImp.getColaEmision().getTama�o()==0)
                   && this.ventanaEmision.estaVacia())
           if ((this.iMODO_FIABILIDAD==PTMF.PTMF_NO_FIABLE)||(this.iMODO_FIABILIDAD==PTMF.PTMF_NO_FIABLE_ORDENADO))
                return true;
           else return this.tablaAsentimientos.estaVacia();

      }
    else return true;

    return false;
  }

  //==========================================================================
  /**
   * Es llamado para indicar que el thread tiene que finalizar. El thread
   * finalizar� cuando se lo indique la funci�n {@link #finComunicacion()}.<br>
   * EL C�DIGO DE ESTE M�TODO ES EJECUTADO POR EL HILO DEL USUARIO.
   * @param bOrdenado si true la finalizaci�n del thread ser� estable.
   */
  public void stopThread(boolean bOrdenado)
  {
    final String mn = "DatosThread.stopThread ()";

    // Indicar que queremos dejar la comunicaci�n.
    this.bABANDONAR_COMUNICACION = true;

    this.bORDENADO = bOrdenado;

    this.finEmision();
  }

  //==========================================================================
  /**
   * Indica que este socket no va a emitir m�s datos.
   */
  void finEmision ()
  {
   this.bFIN_EMISION = true;
  }

  //==========================================================================
  /**
   * Actualiza la lista con el nuevo identificador, siempre que no exista
   * previamente o sea superior al que exista con la misma direcci�n, si estamos
   * en la 2� mitad de RTT.
   * @param id_TPDUParam identificador del TPDU que se quiere a�adir, si se
   * est� en la 2� mitad de RTT
   */
  private void actualizarListaID_TPDU2MitadRTT (ID_TPDU id_TPDUParam)
  {
   final String mn = "DatosThread.actualizarListaID_TPDU2MitadRTT (id_Tpdu)";

   // Realmente s�lo es necesario para aquellos que sea CG Local, de momento,
   // es m�s �ptimo no buscar en la tabla de Hijos, es muy din�mico.
   // Comprobar que estamos en la segunda mitad de RTT
   //Log.debug (Log.DATOS_THREAD,mn,"Porcentaje: " + this.socketPTMFImp.getTemporizador().getPorcentajeRTTActual());
   // CUIDADO: ESTO EST� AS� PORQUE FINRTT PUEDE SER CAMBIADO POR EL HILO
   // DEL TEMPORIZADOR Y SIN EMBARGO NO SE HA LLAMADO A LA FUNCI�N this.RtxTPDUFinRTT
   // Si bFinRTT es true se ha cumplido, al menos una vez, RTT y no se ha ejecutado
   // la funci�n RTXTPDUFINRTT, por lo que se actualiza igual que si estuvi�semos
   // en la 2� mitad de rtt.
   if ( !( (this.bFinRTT)
           ||
           (this.socketPTMFImp.getTemporizador().getPorcentajeRTTActual () > 50) ) )
      return;

   // Estamos en la 2� mitad RTT

   // Comprobar si existe un id_TPDU en la lista con la misma direcci�n que el
   // pasado  por argumento.
   NumeroSecuencia nSecAnterior=(NumeroSecuencia)this.treeMapIDs_TPDU2MitadRTT.get
                                                 (id_TPDUParam.getID_Socket());
   if (nSecAnterior!=null) // Veo si es mayor y lo reemplazo.
    {
      // Si el anterior registrado es mayor o igual a este, no hacer nada.
      if (nSecAnterior.compareTo (id_TPDUParam.getNumeroSecuencia ())>= 0)
          return;

      // Reemplazar el anterior nSec asociado a la direcci�n del ID_TPDU
      // indicado en el argumento por el nuevo nSec que viene en el argumento.
      this.treeMapIDs_TPDU2MitadRTT.put (id_TPDUParam.getID_Socket(),
                                             id_TPDUParam.getNumeroSecuencia());
    }
   else this.treeMapIDs_TPDU2MitadRTT.put (id_TPDUParam.getID_Socket(),
                                    id_TPDUParam.getNumeroSecuencia());
  }

  //==========================================================================
  /**
   * Actualiza la lista de asentimientos positivos que tiene pendientes de enviar.
   * En la lista s�lo se almacena el mayor n�mero de secuencia para un id_socket
   * determinado (LOS ASENT. POSIT. SON ACUMULATIVOS PARA LA MISMA R�FAGA).
   * @param id_tpduFuente id_tpdu que se tiene que asentir positivamente.
   * @param iNumeroRafaga n�mero de la r�faga a la que pertenece id_tpduFuente.
   */
  private void actualizarAsentPositPendientesDeEnviar (ID_TPDU id_tpduFuente,
                                                int iNumeroRafaga)
  {
   if (id_tpduFuente==null)
     return;

   // Almacenar la informaci�n sobre el ACK que queda pendiente de envio,
   // Son ACUMULATIVOS para la misma r�faga.

   // Buscar si ya hab�a alguno en la lista para la misma r�faga que este.
   ID_Socket id_socket  = id_tpduFuente.getID_Socket();
   NumeroSecuencia nSec = id_tpduFuente.getNumeroSecuencia();

   // Iterador con los id_tpdu que contienen a id_socket y el n�mero de r�faga.
   Iterator iteradorID_TPDU = this.listaAsentPositPendientesDeEnviar.
                                    getSublista (id_socket).keySet().iterator();
   ID_TPDU id_tpduNext      = null;
   NumeroSecuencia nSecNext = null;
   int iNumeroRafagaNext;
   while (iteradorID_TPDU.hasNext())
    {
     id_tpduNext      = (ID_TPDU)iteradorID_TPDU.next();
     nSecNext         = id_tpduNext.getNumeroSecuencia();
     iNumeroRafagaNext = ((Integer)this.listaAsentPositPendientesDeEnviar.get(id_tpduNext))
                                                                     .intValue();
     // Dentro de la misma r�faga puesto que pueden tener CG Locales diferentes
     // dos r�fagas distintas.
     if (iNumeroRafagaNext==iNumeroRafaga)
      // Dejar s�lo el que tenga mayor n�mero de secuencia (ACK ACUMULATIVOS)
      {
       if (nSec.compareTo(nSecNext)<=0)
         return;
       else // Quitar el menor
          iteradorID_TPDU.remove ();

       // A�adir el mayor
       this.listaAsentPositPendientesDeEnviar.put (id_tpduFuente,
                                            new Integer (iNumeroRafaga));
       return;
      } // Fin del if
    }// Fin del while

   this.listaAsentPositPendientesDeEnviar.put (id_tpduFuente,
                                       new Integer (iNumeroRafaga));
  }

  //==========================================================================
  /**
   * Actualiza la lista de HSACK que tiene pendientes de enviar, teniendo en
   * cuenta que s�lo se almacena el mayor n�mero de secuencia para un id_socket
   * determinado (los HSACK son acumulativos).
   * @param id_tpdu id_tpdu que se tiene que semi-asentir (HSACK)
   * @param iNumeroRafaga n�mero de la r�faga a la que pertenece id_tpdu.
   * @param bPedidoRtx si true indica que se recibi� un TPDU de datos rtx. por
   * un padre jer�rquico en el que figuramos en su lista de IDGL pendientes de
   * asentir.
   */
  private void actualizarHSACKPendientesDeEnviar (ID_TPDU id_tpdu,int iNumeroRafaga,
                                                  boolean bPedidoRtx)
  {
   final String mn = "DatosThread.actualizarHSACKPendientesDeEnviar (id_tpdu,nRaf,bPedidoRtx)";
   // Almacenar la informaci�n sobre el HSACK que queda pendiente de envio,
   if (id_tpdu==null)
     return;

   // Almacenar la informaci�n sobre el HSACK que queda pendiente de envio,
   // Son ACUMULATIVOS para la misma r�faga.

   // Buscar si ya hab�a alguno en la lista para el mismo emisor fuente que este.
   ID_Socket id_socket  = id_tpdu.getID_Socket();
   NumeroSecuencia nSec = id_tpdu.getNumeroSecuencia();

   // Iterador con los id_tpdu que contienen a id_socket y el n�mero de r�faga.
   Iterator iteradorRegistrosHSACK = this.listaHSACKPendientesDeEnviar.
                                    getSublista (id_socket).values().iterator();
   ID_TPDU id_tpduNext      = null;
   NumeroSecuencia nSecNext = null;
   RegistroHSACK regNext;
   while (iteradorRegistrosHSACK.hasNext())
   {
    regNext  = (RegistroHSACK)iteradorRegistrosHSACK.next();
    nSecNext = regNext.id_tpdu.getNumeroSecuencia();

    // Como los HSACK son multicast no es necesario que se compruebe si pertenecen
    // a la misma r�faga o no para hacer supresi�n. Es indipendiente qui�n sea
    // el CG local porque sea el que sea deber� recogerla y actualizar sus lista
    // de espera de asentimiento.

    // Dejar s�lo el que tenga mayor n�mero de secuencia (HSACK ACUMULATIVOS)
    // El valor de pedidoRtx. del que se quede en la lista deber� ser true
    // si lo es el candidato a entrar (pasado en los par�metros) o el que
    // estaba en la lista. Con ello se evita que un padre jer�rquico crea
    // que hemos abandonado la comunicaci�n, esto se puede ver en enviarHSACK().
    if (nSec.compareTo(nSecNext)<=0)
        {
         regNext.bPedidoRtx = regNext.bPedidoRtx || bPedidoRtx;
         return;
        }
    else // Quitar el menor
         iteradorRegistrosHSACK.remove ();

    // Crear un registro.
    RegistroHSACK reg = new RegistroHSACK ();
    reg.id_tpdu   = id_tpdu;
    reg.iNRafaga  = iNumeroRafaga;
    reg.bPedidoRtx = bPedidoRtx || regNext.bPedidoRtx;

    // A�adir el mayor
    this.listaHSACKPendientesDeEnviar.put (id_tpdu,reg);

    return;
   }// Fin del while

   // Crear un registro.
   RegistroHSACK reg = new RegistroHSACK ();
   reg.id_tpdu   = id_tpdu;
   reg.iNRafaga  = iNumeroRafaga;
   reg.bPedidoRtx = bPedidoRtx;

   // A�ade el indicado en el argumento, por ser el de mayor nSec, o por no haber
   // en la lista otro para el mismo [id_socket,n�mero R�faga]
   this.listaHSACKPendientesDeEnviar.put (id_tpdu,reg);
  }

  //=========================================================================
  /**
   * Comprueba si se est� esperando mandar un asentimiento positivo para id_tpdu.yield
   * @param id_tpdu id_tpdu a comprobar.
   * @return true si se est� esperando enviar un asentimiento positivo para id_tpdu.
   */
  private boolean esperandoMandarAsentPosit (ID_TPDU id_tpdu)
  {
   if (id_tpdu == null)
      return false;

   return this.listaAsentPositPendientesDeEnviar.contiene (id_tpdu);
  }

  //==========================================================================
  /**
   * Actualiza la informaci�n contenida en la tabla de CG locales para la r�faga
   * indicada, seg�n especificado en el protocolo PTMF.
   * @param id_socketFuente id_socket fuente de la r�faga
   * @param iNumeroRafaga n�mero de la r�faga
   * @param nSecInicRafaga n�mero de secuencia inicial de la r�faga
   * @param id_socketCGLocal id_socket CG local para la r�faga.
   * @return id_socket CG de la r�faga.
   */
  private ID_Socket actualizarTablaCGLocales (ID_Socket id_socketFuente,int iNumeroRafaga,
                      NumeroSecuencia nSecInicRafaga,ID_Socket id_socketCGLocal)
  {
   ID_Socket id_socketResult = null;

   if ((id_socketFuente==null) || (id_socketCGLocal==null) || (nSecInicRafaga==null))
        return id_socketResult;

   ID_Socket id_socketCGLocalAnterior = this.tablaCGLocales.getCGLocal (
                                                               id_socketFuente,
                                                               iNumeroRafaga);
   if (id_socketCGLocalAnterior==null)
   {
       this.tablaCGLocales.addRafaga (id_socketFuente,iNumeroRafaga,nSecInicRafaga,
                                      id_socketCGLocal);
       id_socketResult = id_socketCGLocal;

       // Buscar en el vectorMACK y quitar.
       for (int i = 0; i < this.vectorMACK.size(); i++)
        {
         RegistroVectorMACK reg = (RegistroVectorMACK)this.vectorMACK.get (i);

         if (reg.id_tpdu.getID_Socket().equals (id_socketFuente)
             &&
             (reg.iNRafaga==iNumeroRafaga))
            {
             this.vectorMACK.remove (i);
             break; // Fin del for
            }
        } // Fin del for
   }
   else // Ver quien se queda como CG Local // 1� else
    {
     /* S�lo tiene que comprobarse estas condiciones en el caso de que nosotros
        seamos el CG Anterior. */
     /* SI  ( id_socketCGLocalAnterior es este host )
          {
           SI (id_socketCGLocalAnterior NO es la fuente)
                 AND
                 [ (id_socketCGLocal es la fuente)
                   OR
                   (id_socketCGLocal es menor que id_socketCGLocalAnterior)]
           ENTONCES Cambiar CGLocal
           ELSE Reenviar MACK
          }
        ELSE Cambiar CG Local
     */
     boolean bSoyCGAnterior = id_socketCGLocalAnterior.equals (this.id_SocketLocal);
     boolean bCambiarCG = true;

     if (bSoyCGAnterior)
      {
       bCambiarCG = !id_socketCGLocalAnterior.equals (id_socketFuente);
       if (bCambiarCG)
           bCambiarCG = /*condicion &&*/
                      ( id_socketCGLocal.equals (id_socketFuente)
                        ||
                       (id_socketCGLocal.compareTo(id_socketCGLocalAnterior) < 0) );
      }

     if  (bCambiarCG)
      { // Cambiar el CGLocal de la r�faga.

        // ESTA PARRAFADA S�LO SIRVE PARA EL CASO EN EL QUE SE OPTE POR INCLUIR
        // TAMBI�N LA PRIMERA CONDICI�N:
        // Somos el CG Anterior y se tiene que cambiar el CG a otro socket.
        // en ese caso no hacer nada. Pero si somos el CG anterior y adem�s
        // nos mantenemos, tenemos que re-enviar un MACK para advertir a los
        // dem�s que seguimos siendo CG.
        this.tablaCGLocales.actualizaID_SocketCGLocal
                                            ( id_socketFuente,
                                              iNumeroRafaga,
                                              id_socketCGLocal );
       id_socketResult = id_socketCGLocal;

       // LLegado a este punto es por que el id_socket anterior es este socket.
       if (bSoyCGAnterior)
        {
         // Si dejo de ser CG Local de ciertas r�fagas, tengo que eliminar
         // de la lista de espera por Asentimientos todos los id_Tpdu referentes
         // a dicha r�faga, puesto que ser� otro su CG Local.
         // IMPORTANTE: TODOS LOS QUE QUITAMOS DE AQU� DEBER�AN PASAR A LA
         // LISTA DE ACK PENDIENTES DE ENVIAR Y CANCELAR EL TEMPORIZADOR QUE
         // TEN�A ASOCIADO.
         ListaOrdID_TPDU listaOrdID_TPDU = this.tablaAsentimientos.
                removeID_TPDUEnEsperaAsentimiento (id_socketFuente,iNumeroRafaga);

         Iterator iteradorID_TPDU = listaOrdID_TPDU.iteradorID_TPDU ();
         ID_TPDU id_tpduNext = null;
         while (iteradorID_TPDU.hasNext())
          {
              id_tpduNext = (ID_TPDU)iteradorID_TPDU.next();

              // Cancelar temporizador que estaba en marcha.
              this.socketPTMFImp.getTemporizador().cancelarRTTID_TPDU (this,id_tpduNext);

              // Ahora tenemos que mandar ACK para ellos.
              this.actualizarAsentPositPendientesDeEnviar (id_tpduNext,iNumeroRafaga);
          } // Fin del while.
        } // Fin del if
      }
     else // Ver si este host era el CGLocal de la r�faga, en cuyo caso
          // re-enviar MACK para que el resto se enteren y eliminar de las listas
          // de espera por asentimiento.
      {
         id_socketResult = id_socketCGLocalAnterior;
         if (bSoyCGAnterior)
          {
            // Re-Enviar inmediatamente el MACK
            this.enviarTPDUMACK (id_socketFuente,iNumeroRafaga,nSecInicRafaga);
           }//Fin del if
      } // Fin del else
   } //Fin del 1� else

   return id_socketResult;
}

  //=============================================================================
  /**
   * Actualiza el ratio de env�o de datos de usuario, el cual especifica la
   * m�xima velocidad de env�o de nuevos datos de usuario al grupo multicast.
   * @param bytes_x_seg bytes por segundo, tiene que ser mayor que cero.
   * @return el valor al que se ha actualizado el ratio de datos de usuario en bytes
   * por segundo
   */
  public long setRatioUsuario (long lBytes_x_seg)
   {
    // PONER UN VALOR POR DEFECTO del cual no puede ser menor
    if (lBytes_x_seg > 0)
       this.lRatioUsuario_Bytes_x_seg = lBytes_x_seg;

    // Puede hacer que suba moment�neamente mucho el ratio, si se llama
    // repetidamente esta funci�n.
    this.lT_Out_Ajustar_Ratio_Usuario = 0;
    this.lNBytesUsuarioEnviados = 0;

    return this.lRatioUsuario_Bytes_x_seg;
   }

  //=============================================================================
  /**
   * Comprueba si el id_tpdu se puede eliminar de la ventana  correspondiente,
   * siempre que la fiabilidad est� asegurada. Al eliminar el id_tpduParam, tambi�n
   * se eliminan todos los anteriores a �l.
   * Si es CG de id_tpduParam y lo ha podido eliminar de la ventana, entonces
   * intenta eliminar el siguietne con el bit ACK activado de la misma ventana, y
   * as� sucesivamente.
   * @param id_tpduParam id_tpdu a eliminar de la ventana.
   * @param bFinOportunidades lo elimina de la ventana est� o no asentido por todos.
   * @return true si lo elimin�.
   */
  private boolean limpiarVentana (ID_TPDU id_tpduParam,boolean bFinOportunidades)
   {
    Ventana ventana = null;
    ID_Socket id_socketCGLocal = null;
    int iNumRafaga;
    String mn = "DatosThread.limpiarVentana";

    if (id_tpduParam==null)
      {
       //Log.log ("","PARAMETRO NULL");
       return false;
      }

    boolean bEsVtaRecepcion;

    if (id_tpduParam.getID_Socket().equals (this.id_SocketLocal))
      { // Buscar en la ventana de emisi�n
        ventana = this.ventanaEmision;
        bEsVtaRecepcion = false;
      }
    else // Buscar en la ventana de recepci�n.
     {
       ventana = this.tablaID_SocketsEmisores.getVentanaRecepcion(id_tpduParam.getID_Socket());
       bEsVtaRecepcion = true;
     }

   if (ventana == null)
     {
       //Log.log ("","VENTANA NULL");
       return false;
     }
   // Ver el ID_TPDU
   Iterator iteradorNSecTPDUConACK = null;
   ID_TPDU id_tpduAnalizar = id_tpduParam;
   while (true)
    {
     // Comprobar si los tenemos todos y se los hemos entregado al usuario
     // s�lo en el caso de que no se haya decicido abandonar la comunicaci�n.
     if (!this.bABANDONAR_COMUNICACION
         &&
         bEsVtaRecepcion)
       {
        // No es necesario comprobar en vta emisi�n
        if (!ventana.esConsecutivo(id_tpduAnalizar.getNumeroSecuencia().tolong()))
         {
          Log.log ("",""+id_tpduAnalizar+" NO CONSECUTIVO");
          return false; // Este socket a�n no los tiene todos hasta id_tpduAnalizar.
         }
        if (!this.tablaID_SocketsEmisores.entregadoAlUsuario (id_tpduAnalizar))
        {
         if (bFinOportunidades)
           { /*
               Cierro la recepci�n de m�s datos. El usuario no es capaz de
               leerlos o no quiere leerlos.
               CUIDADO SOCKETPTMFIMP ES USADO POR M�S HILOS
               SEGURAMENTE EN ESTE PUNTO OTROS ME HABR�N DADO POR MUERTO
               PUEDO HACER ESTO EN EL RUN CON UN TEMPORIZADOR QUE COMPRUEBA SI
               LLEVO MUCHO TIEMPO SIN PODER METER DATOS EN LA COLA, EN ESE CASO
               PUEDO YO CERRAR LA RECEPCI�N CUANDO NO ES IMPRESCINDIBLE PARA
               CONTINUAR FUNCIONANDO SIN QUE OTROS SE ALERTEN DE M� PROBLEMA
               RETIRO EL P�RRAFO ANTERIOR (POR LO MENOS UNA PARTE) PUESTO QUE
               COMO YO ENVI� UN HSACK NO ME DAR�N POR MUERTO
              */
              this.socketPTMFImp.desactivarRecepcion ();

              Log.log ("SE HA CERRADO LA RECEPCION","");

              // Mandar un aviso
              this.socketPTMFImp.sendPTMFEventError ("Se ha cerrado la recepci�n.");

              // LLamar a entrega datos usuario: Como la recepci�n est� cerrada
              // los datos ser�n eliminados.
              this.tablaID_SocketsEmisores.entregaDatosUsuario (id_tpduAnalizar.getID_Socket(),
                                         this.socketPTMFImp.getColaRecepcion());
            }
          else
              {
               // Apuntar variable para que se intente entregar al usuario en el
               // siguiente control de inactividad de emisores.
               this.treeMapID_SocketEntregarUsuario.put
                                        (id_tpduAnalizar.getID_Socket(),null);
               this.bComprobarEntregaDatosUsuario = true;

               Log.debug (Log.DATOS_THREAD,mn,""+id_tpduAnalizar+" NO ENTREGADO AL USUARIO");
               return false; // A�n no han sido entregado al usuario.
              }
        }
      } // Fin del if (!this.bABANDONAR_COMUNICACION)

     TPDUDatosNormal tpduDatosNormal =
          ventana.getTPDUDatosNormal (id_tpduAnalizar.getNumeroSecuencia().tolong());

     if (tpduDatosNormal == null)
        {
         //Log.log ("","TPDU NULL");
         return false;
        }
     if (!tpduDatosNormal.getACK ())
       {
        //Log.log ("","NO ACTIVO ACK");
        return false;
       }
     iNumRafaga = tpduDatosNormal.getNumeroRafaga();
     id_socketCGLocal = this.tablaCGLocales.getCGLocal(
                                              id_tpduAnalizar.getID_Socket(),iNumRafaga);
     // Si todav�a no tiene CG Local asignado ==> no continuar.
     if (id_socketCGLocal==null)
      {
       //Log.log ("","NO TIENE CG LOCAL");
       return false;
      }

     // Comprobar si soy su CG Local
     if (id_socketCGLocal.equals(this.id_SocketLocal))
       {
        // Comprobar si ha sido asentido por todos.
        if (bFinOportunidades||this.tablaAsentimientos.asentido (id_tpduAnalizar))
          {

           // Yo s� tengo todos los tpdu hasta id_tpdu Analizar
           // Este socket es su CG, pero no es la fuente, por lo que no ha sido
           // enviado por un miembro del grupo local.
           if (!id_tpduAnalizar.getID_Socket().equals (this.id_SocketLocal))
               // Enviar HACK a los padres jer�rquicos.
               this.enviarTPDUHACK (
                   id_tpduAnalizar,
                   iNumRafaga,
                   this.tablaID_SocketsEmisores.getIDGLFuente (id_tpduAnalizar.getID_Socket()));

           // Eliminar de la ventana de recepci�n
           ventana.removeTPDUDatosNormal (id_tpduAnalizar.getNumeroSecuencia().tolong());

           // Ya no estamos esperando ACK por �l.
           this.tablaAsentimientos.removeID_TPDUMenorIgualEnEsperaAsentimiento
                                                 (id_tpduAnalizar);


           // Si estaba pendiente de Rtx. quitarlo de la lista.
           SortedMap sortedMapSolRtx = this.listaID_TPDUSolicitadosRtxMulticast.getSublistaMenorIgual
                                        (id_tpduAnalizar.getID_Socket(),
                                         id_tpduAnalizar.getNumeroSecuencia());
           if (sortedMapSolRtx!=null)
                 sortedMapSolRtx.clear ();

           // Cancelar temporizador que estaba en marcha.
           // Cancelar todos los menores o iguales
           this.socketPTMFImp.getTemporizador().cancelarRTTID_TPDUMenorIgual (this,id_tpduAnalizar);

           // Si es el �ltimo de la r�faga, eliminar de la tabla CG Locales
           this.tablaCGLocales.removeRafagaNSecFinalMenorIgual (
                                          id_tpduAnalizar.getID_Socket(),
                                          id_tpduAnalizar.getNumeroSecuencia());
           // Si el que se ha borrado ten�a el bit fin de emisi�n activo:
           //     - Comprobar que la ventana de emisi�n est� vac�a.
           //     - Eliminar el id_socket de los emisores.
           //     - Enviar informaci�n al usuario.
           //     - Eliminar el id_socket de la cola de recepci�n
           if (tpduDatosNormal.getFIN_CONEXION())
              {
                 this.eliminarID_SocketFuente (id_tpduAnalizar.getID_Socket(),"Fin de emisi�n");
                 Log.debug(Log.DATOS_THREAD,mn,"RECIBIDO EL BIT FIN CONEXION");
                 return true;
              }

           // Condici�n del bucle while
           // Compruebo siguiente TPDU con el ACK activado.
           // Tanto si es ventana de emisi�n como si es ventana de recepci�n
           tpduDatosNormal = ventana.getPrimerTPDUConACKActivado ();
           if (tpduDatosNormal != null)
             {
              id_tpduAnalizar = tpduDatosNormal.getID_TPDU();
              bFinOportunidades = false;
              continue;
             }
           return true;
          }
        //Log.log ("","NO ASENTIDO POR TODOS");
        return false;
       }
       else {
              // Comprobar si se est� esperando para mandar asentimiento
              // Por si hay un problema y tenemos que ser nosotros el CG Local
              // y quiz�s por algo m�s que ahora no recuerdo.
              if (this.esperandoMandarAsentPosit(id_tpduAnalizar))
                {
                 //Log.log ("","ESPERANDO MANDAR ACK");
                 return false;
                }
              // Comprobar que no estamos esperando asentimiento por uno menor.
              ID_TPDU id_tpduMenor = tablaAsentimientos.getID_TPDUMenorEnEsperaAsentimiento
                                                    (id_tpduAnalizar.getID_Socket());
              if (id_tpduMenor != null)
                {
                 // Ver si es menor del que se quiere borrar.
                 if (id_tpduMenor.getNumeroSecuencia().compareTo(
                                                  id_tpduAnalizar.getNumeroSecuencia())<0)
                   {
                     Log.log ("","EN ESPERA DE UNO INFERIOR");
                     return false; // No borrar.
                   }
                }
              // Eliminar de Vta. Recepci�n.
              ventana.removeTPDUDatosNormal (id_tpduAnalizar.getNumeroSecuencia().tolong());

              // Si es el �ltimo de la r�faga, eliminar de la tabla CG Locales
              this.tablaCGLocales.removeRafagaNSecFinalMenorIgual (
                                            id_tpduAnalizar.getID_Socket(),
                                            id_tpduAnalizar.getNumeroSecuencia());

              // Si el que se ha borrado ten�a el bit fin de emisi�n activo:
              //     - Comprobar que la ventana de emisi�n est� vac�a.
              //     - Eliminar el id_socket de los emisores.
              //     - Enviar informaci�n al usuario.
              //     - Eliminar el id_socket de la cola de recepci�n
              if (tpduDatosNormal.getFIN_CONEXION())
                  {
                    this.eliminarID_SocketFuente (id_tpduAnalizar.getID_Socket(),"Fin de emisi�n");
                    Log.debug(Log.DATOS_THREAD,mn,"RECIBIDO EL BIT FIN CONEXION");
                  }
              return true;
            } // Fin del else;
  } // Fin del while

}

//=============================================================================
/**
 * Por cada TPDU Datos normal con el bit ACK activado contenido en la ventana
 * asociada a id_socket llama a la funci�n {@link #limpiarVentana(ID_TPDU,boolean)}.
 * Empieza desde el inicio de la ventana, por lo que si limpiarVentana devuelve
 * false no sigue, puesto que no se van a poder eliminar los siguientes.
 * @param id_socket id_socket cuya ventana se va a intentar vaciar, parcial
 * o totalmente.
 */
 private void limpiarVentana (ID_Socket id_socket)
 {
  final String mn = "DatosThread.limpiarVentana (id_socket)";

  if (id_socket==null)
     return;

  Ventana ventana = null;

  if (id_socket.equals (this.id_SocketLocal))
  {// Buscar en la ventana de emisi�n
     ventana = this.ventanaEmision;
  }
  else // Buscar en la ventana de recepci�n.
  {
      ventana = this.tablaID_SocketsEmisores.getVentanaRecepcion(id_socket);
      this.tablaID_SocketsEmisores.entregaDatosUsuario (id_socket,
                                        this.socketPTMFImp.getColaRecepcion());
  }

  if (ventana==null)
  {
     return;
  }

  // Empieza en el primero de la ventana con el bit ACK activado.
  // Con esto se asegura que si el tpdu que se ha recibido pertenece a una r�faga,
  // de la que no somos CG, posterior a una de la que s� lo somos, no se eliminar�n
  // de la ventana correspondientes puesto que para ello deber�n estar asentidos
  // los anteriores (de la que somos CG).
  TPDUDatosNormal tpduDatosNormal = ventana.getPrimerTPDUConACKActivado ();

  if (tpduDatosNormal == null)
  {
       return;
  }

  limpiarVentana (tpduDatosNormal.getID_TPDU(),false);
 }

//=============================================================================
/**
 * LLama a la funci�n {@link #limpiarVentana(ID_TPDU,boolean)} con el primer TPDU de datos
 * con el bit ACK activado contenido en la ventana asociada a id_socket.
 * @param id_socket id_socket fuente.
 */
 private void limpiarVentanaHastaPrimerTPDUConACK (ID_Socket id_socket)
 {
  final String mn = "DatosThread.limpiarVentanaHastaPrimerTPDUConACK (id_socket)";

  if (id_socket==null)
     return;

  Ventana ventana = null;

  if (id_socket.equals (this.id_SocketLocal))// Buscar en la ventana de emesi�n
     ventana = this.ventanaEmision;
  else // Buscar en la ventana de recepci�n.
     {
      ventana = this.tablaID_SocketsEmisores.getVentanaRecepcion(id_socket);
      this.tablaID_SocketsEmisores.entregaDatosUsuario (id_socket,
                                        this.socketPTMFImp.getColaRecepcion());
     }

  if (ventana==null)
     return;

  // S�lo analiza el primero de la ventana con el bit ACK activado.
  TPDUDatosNormal tpduDatosNormal = ventana.getPrimerTPDUConACKActivado();

  if (tpduDatosNormal != null)
    limpiarVentana (tpduDatosNormal.getID_TPDU(),true);
 }

//=============================================================================
/**
 * Entrega datos al usuario en modo no fiable y no fiable ordenado.
 * @param id_socket id_socket fuente de los datos.
 * @param nSec n�mero de secuencia de los datos a entregar.
 * @param bufferDatos datos a entregar.
 * @param bSetFinTransmision true si el TPDU donde ven�an los datos ten�a el bit
 * Fin Transmisi�n activado.
 */
 private void entregaDatosUsuarioNoFiable (ID_Socket id_socket,NumeroSecuencia nSec,
                                  Buffer bufferDatos,boolean bSetFinTransmision)
 {
  final String mn = "entregaDatosUsuarioNoFiable (id_socket)";

  // Mira la ventana de id_socket para entregar datos al usuario.
  if ((id_socket==null) || (bufferDatos==null) || (nSec == null))
     return;

  // Comprobar si se trata del socket local.
  if (id_socket.equals (this.id_SocketLocal))
    return;

  if (!getAbandonarComunicacion()) // Si se ha cerrado el socket
                                   // no entregamos m�s datos.
    {
     if (this.iMODO_FIABILIDAD == PTMF.PTMF_NO_FIABLE_ORDENADO)
      {
        if (this.treeMapNSecUltimoEntregado.containsKey(id_socket))
          { // Comprobar el n�mero de secuencia almacenado
            NumeroSecuencia nSecEntregado = (NumeroSecuencia)
                                this.treeMapNSecUltimoEntregado.get (id_socket);
            if (nSecEntregado.compareTo (nSec) >= 0)
              {
                // Aviso al usuario de que se ha tirado
                this.socketPTMFImp.sendPTMFEventError ("Se han descartado datos del emisor " + id_socket + " por recibirse en desorden.");
                return; // No entregar
              }
            else // Actualizar
                 this.treeMapNSecUltimoEntregado.put (id_socket,nSec);
          }
        else this.treeMapNSecUltimoEntregado.put (id_socket,nSec);
      }

     // Entregar al usuario: bufferDatos
     // Si ha habido problemas para a�adir, los datos se habr�n perdido.
     this.socketPTMFImp.getColaRecepcion().add(id_socket,bufferDatos,bSetFinTransmision);
    }
 }

 //============================================================================
 /**
  * Devuelve un treemap con los id_socket fuentes actuales.
  * @return si el modo es FIABLE o FIABLE_RETRASADO: devuelve TreeMap con los
  * id_Socket fuentes actuales o vac�o si no hay ninguno. <br>
  * <b>Key  : </b>id_socket
  * <b>Value: </b>null<br>
  * Si el modo es NO_FIABLE o NO_FIABLE_ORDENADO: devuelve null.
  */
 public TreeMap getID_SocketEmisores ()
 {
  if (this.tablaID_SocketsEmisores!=null)
   return this.tablaID_SocketsEmisores.getID_SocketEmisores();
  return null;
 }

 //============================================================================
 /**
  * Actualiza las listar de ID_TPDU solicitados para RTX.
  * @param id_tpdu id_tpdu solicitado
  * @param id_socket id_socket que solicita la retransmisi�n
  * @param iTipoSolicitud indica si ha sido pedido por unicast({@link #iPEDIDO_POR_UNICAST})
  * @see #listaID_TPDUSolicitadosRtxUnicast
  * @see #listaID_TPDUSolicitadosRtxMulticast
  */
  private void actualizarListaID_TPDUSolicitadoRtx (ID_TPDU id_tpdu,
                                          ID_Socket id_socket,int iTipoSolicitud)
  {
   if (id_tpdu==null || id_socket==null)
        return;

   if ( (iTipoSolicitud & iPEDIDO_POR_UNICAST) == iPEDIDO_POR_UNICAST  )
    { // PEDIDO POR UNICAST -- > APUNTAR PARA ENVIAR POR UNICAST
     TreeMap treeMapID_SocketSolRtx = (TreeMap)
                                   this.listaID_TPDUSolicitadosRtxUnicast.get (id_tpdu);
     if (treeMapID_SocketSolRtx == null)
      {
       treeMapID_SocketSolRtx = new TreeMap ();
       treeMapID_SocketSolRtx.put (id_socket,null);
       this.listaID_TPDUSolicitadosRtxUnicast.put (id_tpdu,treeMapID_SocketSolRtx);
       return;
      }

     treeMapID_SocketSolRtx.put (id_socket,null);
     return;
    }

   // Pedido por multicast
   Integer integerTipoSolicitud = (Integer)
                         this.listaID_TPDUSolicitadosRtxMulticast.get (id_tpdu);
   int iTipoSolicitudAux = 0;
   if (integerTipoSolicitud != null)
    {
     iTipoSolicitudAux = integerTipoSolicitud.intValue() | iTipoSolicitud;
    }
   else iTipoSolicitudAux = iTipoSolicitud;

   integerTipoSolicitud = new Integer (iTipoSolicitudAux);
   this.listaID_TPDUSolicitadosRtxMulticast.put (id_tpdu,integerTipoSolicitud);
  }

 //=========================================================================
 /**
  * A�adir un nuevo ID_TPDU a la tabla de asentimientos, si estamos en modo
  * fiable retrasado, rtx. el tpdu dependiendo de lo que figure en los treemap
  * de no sincronizado.
  * @param id_tpdu id_tpdu a a�adir a la tabla de asentimientos
  * @param iNumRafaga n�mero de r�faga a la que pertenece id_tpdu
  * @param bRtxSoloHijosJerarquicos si es true y el modo es FIABLE_RETRASADO
  * indica que se tiene que RTX s�lo a los hijos jer�rquicos.
  * @return true si ha sido a�adido.
  */
  private boolean ponerEnEsperaAsentimiento (ID_TPDU id_tpdu,int iNumRafaga,
                                               boolean bRtxSoloHijosJerarquicos)
  {
   if (this.tablaAsentimientos.addID_TPDUEnEsperaAsentimiento (id_tpdu,iNumRafaga))
   {
       // Registro funci�n para que me avise cuando se acaban las
       // oportunidades.
       this.socketPTMFImp.getTemporizador().registrarAvisoRTT(this,this.iEVENTO_FIN_INTENTOS,id_tpdu);
    }
    else
    {
     return false;
    }

    if (this.iMODO_FIABILIDAD == PTMF.PTMF_FIABLE_RETRASADO)
    {
       rtxTPDU (id_tpdu,
                iRTX_SI_NO_SINCRONIZADO_Y_ES_1�_RTX
                 |
                (bRtxSoloHijosJerarquicos?iRTX_SOLO_A_HIJOS:0));
    }
    return true;
 }

 //=========================================================================
 /**
  * Rtx el id_tpdu indicado, teniendo en cuenta el modo de fiabilidad.
  * @param iOpciones puede ser alguno o varios de los siguientes valores:
  * <ul>
  *   <li>{@link #iRTX_SOLO_A_HIJOS}</li>
  *   <li>{@link #iRTX_SOLO_A_VECINOS}</li>
  *   <li>{@link #iRTX_SI_NO_SINCRONIZADO_Y_ES_1�_RTX}</li>
  *   <li>{@link #iRTX_SI_NO_SINCRONIZADO}</li>
  *   <li>{@link #iRTX_SI_CONTIENE_ID_NO_ASENTIDOS}</li>
  * </ul>
  * @return true si enviado un TPDU retransmitido.
  * si rtxSiContieneNoSincronizado es true indica que s�lo se rtx. si en sus
  * listas de no asentidos figura un socket o idgl que a�n no est� sincronizado
  * para dicho emisor.
  * Si rtxSolohijosjerarqu es true entonces no se a�aden los id_sockets que no
  * hayan enviado asentimiento.
  * Si el
  */
 private boolean rtxTPDU (ID_TPDU id_tpdu,int iOpciones)
 {
    final String mn = "DatosThread.rtxTPDU";

    if (id_tpdu==null)
     return false;

    // Si indico solo a hijos y s�lo a vecinos se entiende que se rtx a todos.
    boolean bRtxSoloAHijos   = (iOpciones & iRTX_SOLO_A_HIJOS) == iRTX_SOLO_A_HIJOS;
    boolean bRtxSoloAVecinos = (iOpciones & iRTX_SOLO_A_VECINOS) == iRTX_SOLO_A_VECINOS;
    if (bRtxSoloAHijos && bRtxSoloAVecinos)
      {
       bRtxSoloAHijos   = false;
       bRtxSoloAVecinos = false;
      }
    boolean bRtxSiNoSincronizadoyEs1�Rtx =
                (iOpciones & iRTX_SI_NO_SINCRONIZADO_Y_ES_1�_RTX) == iRTX_SI_NO_SINCRONIZADO_Y_ES_1�_RTX;

    boolean bRtxSiNoSincronizado =
                (iOpciones & iRTX_SI_NO_SINCRONIZADO) == iRTX_SI_NO_SINCRONIZADO;

    boolean bRtxSiContieneIdNoAsentidos =
                (iOpciones & iRTX_SI_CONTIENE_ID_NO_ASENTIDOS) == iRTX_SI_CONTIENE_ID_NO_ASENTIDOS;

    // Obtener el tpdu a rtx.
    TPDUDatosNormal tpduDatosNormal = this.buscarTPDUEnVentana (id_tpdu);
    if (tpduDatosNormal == null)
        return false;

    TreeMap treeMapID_SocketsNoEnviadoACK    = null;
    TreeMap treeMapIDGLNoEnviadoAsentimiento = null;

    // No devuelven null. Si no hay devuelve un treeMap vac�o

    // Puesto para optimizar: si se quita funciana igual, pero se ahorra
    // muchos pasos
    if ( (bRtxSiNoSincronizado || bRtxSiNoSincronizadoyEs1�Rtx)
          &&
         (this.iMODO_FIABILIDAD != PTMF.PTMF_FIABLE_RETRASADO) )
         return false;

    // Treemap con los sockets que figurar�n en la lista de no asentidos
    // del tpdu que se va a rtx.
    if (!bRtxSoloAHijos)
         treeMapID_SocketsNoEnviadoACK =
                    this.tablaAsentimientos.getTreeMapID_SocketsNoEnviadoACK(id_tpdu);
    else treeMapID_SocketsNoEnviadoACK = new TreeMap ();

    // Treemap con los sockets que figurar�n en la lista de no asentidos
    // del tpdu que se va a rtx.
    if (!bRtxSoloAVecinos)
         treeMapIDGLNoEnviadoAsentimiento =
                    this.tablaAsentimientos.getTreeMapIDGLNoEnviadoHACKoHSACK(id_tpdu);
    else treeMapIDGLNoEnviadoAsentimiento = new TreeMap ();

    boolean bHayQueRtx = false;

    // Si estamos en modo fiable retrasado actualizamos las listas de no sincronizados
    if (this.iMODO_FIABILIDAD == PTMF.PTMF_FIABLE_RETRASADO)
     {
      if (!this.treeMapID_SocketNoSincronizado.isEmpty()
          &&
          !treeMapID_SocketsNoEnviadoACK.isEmpty())
       {
        // Actualizar los emisores de los sockets nuevos
        Iterator iteradorID_SocketsNuevos =
                        this.treeMapID_SocketNoSincronizado.keySet().iterator();

        TreeMap treeMapValue;
        ID_Socket id_socketNuevoNext;
        while (iteradorID_SocketsNuevos.hasNext())
         {
          id_socketNuevoNext = (ID_Socket)iteradorID_SocketsNuevos.next();

          // Comprobar si este socket est� entre los que se van a mandar en la
          // lista de no asentidos del tpdu rtx
          if (treeMapID_SocketsNoEnviadoACK.containsKey (id_socketNuevoNext))
           {
            treeMapValue = (TreeMap)this.treeMapID_SocketNoSincronizado.get(id_socketNuevoNext);
            if (!treeMapValue.containsKey (id_tpdu.getID_Socket()))
             {
              // A�adirlo
              treeMapValue.put (id_tpdu.getID_Socket(),Boolean.FALSE);
              bHayQueRtx = true;
             }
            else // Lo contiene habr�a que preguntar si es TRUE O FALSE EN CASO
                 // de que no haya establecido que se rtx s�lo si es la primera vez.
                 {
                  if (!bRtxSiNoSincronizadoyEs1�Rtx
                      &&
                      !((Boolean)treeMapValue.get(id_tpdu.getID_Socket())).booleanValue() )
                            bHayQueRtx = true;
                 }
           }
         }// Fin del while
       } // Fin del if

      if (!this.treeMapIDGLNoSincronizado.isEmpty()
          &&
          !treeMapIDGLNoEnviadoAsentimiento.isEmpty())
       {
        // Actualizar los emisores de los sockets nuevos
        Iterator iteradorIDGLNuevos =
                        this.treeMapIDGLNoSincronizado.keySet().iterator();

        TreeMap treeMapValue;
        IDGL idglNuevoNext;
        while (iteradorIDGLNuevos.hasNext())
         {
          idglNuevoNext = (IDGL)iteradorIDGLNuevos.next();

          // Comprobar si este socket est� entre los que se van a mandar en la
          // lista de no asentidos del tpdu rtx
          if (treeMapIDGLNoEnviadoAsentimiento.containsKey (idglNuevoNext))
           {
            treeMapValue = (TreeMap)this.treeMapIDGLNoSincronizado.get(idglNuevoNext);
            if (!treeMapValue.containsKey (id_tpdu.getID_Socket()))
             {
              // A�adirlo
              treeMapValue.put (id_tpdu.getID_Socket(),Boolean.FALSE);
              bHayQueRtx = true;
             }
            else // Lo contiene habr�a que preguntar si es TRUE O FALSE EN CASO
                 // de que no haya establecido que se rtx s�lo si es la primera vez.
                 {
                  if (!bRtxSiNoSincronizadoyEs1�Rtx
                      &&
                      !((Boolean)treeMapValue.get(id_tpdu.getID_Socket())).booleanValue() )
                            bHayQueRtx = true;
                 }
           }
         }// Fin del while
       } // Fin del if

      if (!bRtxSiNoSincronizado
         &&
         !bRtxSiNoSincronizadoyEs1�Rtx)
            bHayQueRtx = true;

      // Comprobar si figura como nuevo emisor
      ID_TPDU id_tpduInicioNuevoEmisor = (ID_TPDU)
              this.treeMapNuevosEmisoresASincronizar.get (id_tpdu.getID_Socket());
      if (id_tpduInicioNuevoEmisor != null)
       {
        // Si esta semiasentido es porque soy su CG y todos han enviado un
        // asentimiento o semiasentimiento positivo.
        // Si no lo tengo en la ventana es porque lo he eliminado. Otro ser�a
        // el CG y ese ser� el que lo haya sincronizado.
        if (this.tablaAsentimientos.semiAsentido (id_tpduInicioNuevoEmisor)
            ||
            this.buscarTPDUEnVentana (id_tpduInicioNuevoEmisor)==null
            )
         {
          this.treeMapNuevosEmisoresASincronizar.remove (id_tpdu.getID_Socket());
         }
         else {
               if (bRtxSiNoSincronizadoyEs1�Rtx)
                {
                  if (id_tpduInicioNuevoEmisor.equals (id_tpdu))
                      bHayQueRtx = true;
                }
               else bHayQueRtx = true;
              }
       }
     } // Fin del if (FIABLE_RETRASADO)
     else {
           if (bRtxSiNoSincronizado
               ||
               bRtxSiNoSincronizadoyEs1�Rtx)
                 bHayQueRtx = false;
           else bHayQueRtx = true;
          }

    // Rtx. el TPDU si hay alg�n socket en la lista de no sincronizado
    if (bHayQueRtx)
     {
      //Log.debug (Log.TPDU_RTX,mn,"HAY QUE RTX HA RESULTADO TRUE");
      if (bRtxSiContieneIdNoAsentidos)
       {
         // Si los treeMap de identificadores que no han enviado asentimiento
         // est�n vacios entonces no rtx.
         if (treeMapID_SocketsNoEnviadoACK.isEmpty()
            && treeMapIDGLNoEnviadoAsentimiento.isEmpty())
               return false;
       }
      // Preparar el TPDU RTX y enviarlo
      try {
        Vector vectorTPDURtx =
                tpduDatosNormal.convertirAVectorTPDUDatosRtx (this.socketPTMFImp,
                                                treeMapID_SocketsNoEnviadoACK,
                                                treeMapIDGLNoEnviadoAsentimiento);
        /* Destino:
              - Hijos y vecinos a excepci�n de lo que diga las variables
              rtxSoloAHijos y rtxSoloAVecinos, es decir, en principio tiene que
              llegar a todos.
        */
        TPDUDatosRtx tpduDatosRtx;
        for (int i=0;i<vectorTPDURtx.size();i++)
         {
          tpduDatosRtx = (TPDUDatosRtx)vectorTPDURtx.elementAt (i);
          Buffer bufAEnviar = tpduDatosRtx.construirTPDUDatosRtx();

          // Si ha llegado hasta aqu� es porque tiene que ser enviado.
          // S�lo hay que ajustar el TTL de env�o.
          this.socketPTMFImp.getCanalMcast().send (bufAEnviar);
          Log.debug(Log.TPDU_RTX,mn,"--> TPDU RTX: " + tpduDatosRtx.getID_TPDUFuente());

          // No se actualiza el bTime_out_inactividad porque este TPDU no tiene por
          // que llegar a todos los usuarios del grupo multicast.
         } // Fin del for
      }catch (ParametroInvalidoExcepcion e)
         {
          Log.debug (Log.TPDU_RTX,mn,""+e);
         }
       catch (PTMFExcepcion e)
         {
          Log.debug (Log.TPDU_RTX,mn,""+e);
         }
       catch (IOException e)
         {
          Log.debug (Log.TPDU_RTX,mn,""+e);
         }
     } // Fin del if de RTX.
     else return false;

    return true;
  }

 //============================================================================
 /**
  * Notifica que IDGL ha sido a�adido. Si el modo es fiable retrasado anota el
  * idgl como no sincronizado para todos los emisores fuentes, y apunta como
  * recibido HSACK para todos los id_tpdus por los que se est� esperando
  * asentimiento.
  * Ejecutada por el thread DatosThread.
  * @param idgl idgl a�adido
  * @see #treeMapIDGLNoSincronizado
  * @see #treeMapID_SocketNoSincronizado
  */
  private void a�adirIDGL(IDGL idgl)
  {
   if (idgl==null)
        return;

   /*
    Si el modo es semi fiable, anotar como asentido todos los
    id_tpdu por los que se esten esperando.
   */
   if (iMODO_FIABILIDAD == PTMF.PTMF_FIABLE_RETRASADO)
    {
       if (!this.treeMapIDGLNoSincronizado.containsKey (idgl))
         {
           this.treeMapIDGLNoSincronizado.put(idgl, new TreeMap());
           this.socketPTMFImp.getTemporizador().registrarFuncion (PTMF.TIEMPO_MAXIMO_SINCRONIZACION,
                                          this,
                                          this.iEVENTO_FIN_SINCRONIZACION_IDGL,
                                          idgl);
         }
        tablaAsentimientos.addHSACKAID_TPDUEnEspera (idgl);
     } // Fin del if
   }


 //============================================================================
 /**
  * Notifica que IDGL ha sido eliminado
  * Ejecutada por el thread DatosThread.
  * @param idgl idgl eliminado
  */
  private void eliminarIDGL(IDGL idgl)
  {
   // C�digo ejecutado por CGLThread

   this.tablaAsentimientos.removeIDGL (idgl);

   // De la lista de rtx. solicitadas no es necesario, se
   // comprueba cuando va a ser enviado.

   // Comprobar si hay asentidos en la tabla de asentimiento
   ListaOrdID_TPDU listaID_TPDUAsentidos = tablaAsentimientos.getID_TPDUAsentidos ();

   Iterator iteradorID_TPDU = listaID_TPDUAsentidos.iteradorID_TPDU ();
   ID_TPDU id_tpduNext = null;
   while (iteradorID_TPDU.hasNext())
   {
      id_tpduNext = (ID_TPDU)iteradorID_TPDU.next();
      limpiarVentana (id_tpduNext,false);
    } // Fin del while

   if (this.iMODO_FIABILIDAD == PTMF.PTMF_FIABLE_RETRASADO)
     this.treeMapIDGLNoSincronizado.remove (idgl);

  }

 //============================================================================
 /**
  * Notifica que id_socket ha sido a�adido. Si el modo es fiable retrasado anota el
  * id_socket como no sincronizado para todos los emisores fuentes, y apunta como
  * recibido ACK para todos los id_tpdus por los que se est� esperando
  * asentimiento.
  * Ejecutada por el thread DatosThread.
  * @param id_socket id_socket a�adido
  * @see #treeMapID_SocketNoSincronizado
  * @see #treeMapIDGLNoSincronizado
  */
  private void a�adirID_Socket(ID_Socket id_socket)
  {
   if (id_socket == null)
      return;
   /*
    Si el modo es fiable retrasado, anotar como asentido todos los
    id_tpdu por los que se esten esperando.
   */
   if (iMODO_FIABILIDAD == PTMF.PTMF_FIABLE_RETRASADO)
     {
       if (!this.treeMapID_SocketNoSincronizado.containsKey (id_socket))
          {
           this.treeMapID_SocketNoSincronizado.put(id_socket, new TreeMap());
           this.socketPTMFImp.getTemporizador().registrarFuncion (PTMF.TIEMPO_MAXIMO_SINCRONIZACION,
                                          this,
                                          this.iEVENTO_FIN_SINCRONIZACION_ID_SOCKET,
                                          id_socket);
          }
       tablaAsentimientos.addACKAID_TPDUEnEspera (id_socket);
     }
  }

 //============================================================================
 /**
  * Notifica que id_socket ha sido eliminado. <br>
  * Elimina toda la informaci�n almacenada que haga referencia a id_Socket.<br>
  * Ejecutada por el thread DatosThread.
  * @param id_Socket id_socket que ha sido eliminado de la comunicaci�n.
  */
  private void eliminarID_Socket(ID_Socket id_socket)
  {
   // C�digo ejecutado por CGLThread

   if (id_socket==null)
         return;

   if (id_socket.equals (id_SocketLocal))
      {
       // HACER: ALEX ??
       return;
      }

   this.socketPTMFImp.sendPTMFEventError ("ID_Socket eliminado " + id_socket);

   // Eliminar de los NACK:
   gestionAsentNeg.removeID_Socket (id_socket);

   listaAsentPositPendientesDeEnviar.removeID_Socket (id_socket);

   listaHSACKPendientesDeEnviar.removeID_Socket (id_socket);

   this.tablaAsentimientos.removeID_Socket (id_socket);

   // No elimino del temporizador. Conforme vaya venciendo se ir� eliminando
   // autom�ticamente.
   if (this.iMODO_FIABILIDAD == PTMF.PTMF_FIABLE_RETRASADO)
    {
     /*
     Buscar por la clave y eliminar. No es necesario eliminar a id_socket
     de los valores asociados a todas las claves, puesto que se supone que
     el emisor id_socket (figura en values) no enviar� m�s tpdu.
     */
     this.treeMapID_SocketNoSincronizado.remove (id_socket);
     this.treeMapNuevosEmisoresASincronizar.remove (id_socket);
    }

   // Comprobar si hay asentidos en la tabla de asentimiento
   ListaOrdID_TPDU listaID_TPDUAsentidos = (ListaOrdID_TPDU)tablaAsentimientos.getID_TPDUAsentidos().clone();
   //Log.debug (Log.ID_SOCKET,mn,"Lista de Asentidos: " + listaID_TPDUAsentidos);
   // Nota: Los cambios hechos a listaID_TPDUAsentidos no afectan a la tabla de
   // asentimientos, pero si los cambios hechos a los objetos que contiene.
   // No podemos usar clone porque los valores de la lista en la Tabla de asentimientos
   // son treeMap.

   Iterator iteradorID_TPDU = listaID_TPDUAsentidos.iteradorID_TPDU ();
   ID_TPDU id_tpduNext = null;
   while (iteradorID_TPDU.hasNext())
   {
      id_tpduNext = (ID_TPDU)iteradorID_TPDU.next();

      limpiarVentana (id_tpduNext,false);
    } // Fin del while

   //  id_socket es CG Local de otros sockets
   tablaCGLocales.removeID_Socket (id_socket);

   // Eliminar de emisor si lo es.
   this.eliminarID_SocketFuente (id_socket,"Fin por error del emisor");
   removeMACK (id_socket);
  }


 //============================================================================
 /**
  * Notifica que ID_Socket ha sido a�adido.
  * Ejecutada por el thread CGLThread.
  * @param id_socket id_socket a�adido
  */
  public void ID_SocketA�adido(ID_Socket id_socket)
  {
   if (this.vectorIDA�adidos==null)
    return;

   this.vectorIDA�adidos.add (id_socket);
  }

 //============================================================================
 /**
  * Notifica que id_socket ha sido eliminado
  * Ejecutada por el thread CGLThread.
  * @param id_socket id_socket eliminado.
  */
  public void ID_SocketEliminado(ID_Socket id_socket)
  {
   if (this.vectorIDEliminados==null)
    return;

   this.vectorIDEliminados.add (id_socket);
  }

 //============================================================================
 /**
  * Notifica que IDGL ha sido a�adido
  * Ejecutada por el thread CGLThread.
  * @param idgl idgl a�adido
  */
  public void IDGLA�adido(IDGL idgl)
  {
   if (this.vectorIDA�adidos==null)
    return;

   this.vectorIDA�adidos.add (idgl);
  }

 //============================================================================
 /**
  * Notifica que IDGL ha sido eliminado
  * Ejecutada por el thread CGLThread.
  * @param idgl idgl eliminado
  */
  public void IDGLEliminado(IDGL idgl)
  {
   if (this.vectorIDEliminados==null)
    return;

   this.vectorIDEliminados.add (idgl);
  }




 //============================================================================
 /**
  * Devuelve el IDGL al que pertenece  id_socket
  */
  IDGL getIDGL (ID_Socket id_socket)
  {
   if (id_socket==null)
        return null;

   // Comprobar si es este host.
   if (id_socket.equals (this.id_SocketLocal))
        return this.socketPTMFImp.getCGLThread().getIDGL();

   // Buscar en la tabla de hosts emisores.
   return this.tablaID_SocketsEmisores.getIDGLFuente (id_socket);
  }

 //============================================================================
 /**
  * Devuelve una referencia al CGLThread.
  */
  CGLThread getCGLThread ()
  {
   return this.socketPTMFImp.getCGLThread ();
  }

} // Fin de la clase DatosThread

//---------------------------------------------------------------------------
//
//               CLASE : RegistroHSACK
//
//---------------------------------------------------------------------------
/*
 * Almacena datos referentes a los HSACK que se tienen que enviar.
 * @see DatosThread
 * @version  1.0
 * @author M. Alejandro Garc�a Dom�nguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */
class RegistroHSACK
{
  /** id_tpdu que se tiene que semi-asentir */
  ID_TPDU id_tpdu   = null;

  /** n�mero de r�faga a la que pertenece id_tpdu */
  int     iNRafaga  = 0;

  /** Si es true indica que se ha recibido un TPDU Datos Rtx. en el que figura
      el IDGL de este socket en la lista de los que no han enviado asentimiento.  */
  boolean bPedidoRtx = false;
} // Fin de la clase RegistroHSACK

//---------------------------------------------------------------------------
//
//               CLASE : RegistroVectorMACK
//
//---------------------------------------------------------------------------
/*
 * Almacena datos referentes a los MACK que este socket tiene que enviar.
 * @see DatosThread
 * @version  1.0
 * @author M. Alejandro Garc�a Dom�nguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */
class RegistroVectorMACK

{
  /** ID_Socket fuente y n�mero de secuencia inicial del MACK */
  ID_TPDU id_tpdu  = null;
  /** N�mero de r�faga del MACK. */
  int     iNRafaga = 0;

   //=================================================================
   /**
    * Comprueba si este RegistroVectorMACK es igual al pasado por par�metro.
    * Dos RegistroVectorMACK son iguales si sus datos son iguales.
    * @param o RegistroVectorMACK con el que se compara
    * @return true si son iguales, y false en caso contrario.
    */
   public boolean equals (Object o)
   {
    RegistroVectorMACK reg  = (RegistroVectorMACK) o;

    if ((reg.id_tpdu == null) ||(this.id_tpdu==null))
        return false;

    if (reg.id_tpdu.equals (this.id_tpdu)
        &&
        (reg.iNRafaga == this.iNRafaga))
           return true;
    return false;
   }

 //======================================================================
 /**
  * Devuelve una cadena informativa.
  * @return cadena informativa.
  */
 public String toString ()
 {
  return new String ("[" + this.id_tpdu  + "," +
                           "NRafaga: " + this.iNRafaga +
                     "]");
 }
} // Fin de la clase RegistroVectorMACK

