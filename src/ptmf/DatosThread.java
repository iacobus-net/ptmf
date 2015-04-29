//============================================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: DatosThread.java  1.0 13/10/99
//
//	Autores: Antonio Berrocal Piris (AntonioBP@wanadoo.es)
//               M. Alejandro García Domínguez (Malejandro.garcia.dominguez@gmail.com)
//
//	Descripción: Procesa y envía todos los TPDU de datos (TPDUDatosNormal,
//                   TPDUDatosRtx, TPDUMACK, TPDUACK, TPDUHACK, TPDUHSACK,
//                   TPDUNACK y TPDUHNACK).
//                   Implementa los modos:
//                   <ul>
//                       <li>{@link PTMF#PTMF_FIABLE}</li>
//                       <li>{@link PTMF#PTMF_FIABLE_RETRASADO}</li>
//                       <li>{@link PTMF#PTMF_NO_FIABLE}</li>
//                       <li>{@link PTMF#PTMF_NO_FIABLE_ORDENADO}</li>
//                    </ul>
//                   Basada en la especificación del protocolo PTMF.
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
  * Procesa y envía los TPDU de datos: TPDUDatosNormal, TPDUDatosRtx, TPDUMACK,
  * TPDUACK, TPDUHACK, TPDUHSACK, TPDUNACK y TPDUHNACK.<BR>
  * Implementa los modos:
  *   <ul>
  *      <li>{@link PTMF#PTMF_FIABLE}</li>
  *      <li>{@link PTMF#PTMF_FIABLE_RETRASADO}</li>
  *      <li>{@link PTMF#PTMF_NO_FIABLE}</li>
  *      <li>{@link PTMF#PTMF_NO_FIABLE_ORDENADO}</li>
  *   </ul>
  * Basada en la especificación del protocolo PTMF.
  * @see SocketPTMF
  * @see PTMF
  * @version  1.0
  * @author Antonio Berrocal Piris
  * <A HREF="mailto:AntonioBP@wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
  * M. Alejandro García Domínguez
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
   * Almacena la información sobre los Asentimientos positivos (ACK o HACK) que
   * están pendientes de envio.
   * <table border=1>
   *  <tr>  <td><b>Key:</b></td>
   *	    <td>{@link ID_TPDU} fuente que se tiene que asentir.</td>
   *  </tr>
   *  <tr>  <td><b>Value:</b></td>
   *	    <td>Número de ráfaga como un objeto Integer.</td>
   *  </tr>
   * </table>
   * @see TPDUACK
   * @see TPDUHACK
   */
  private ListaOrdID_TPDU listaAsentPositPendientesDeEnviar = null;


  /**
   * Almacena la información sobre los HSACK que están pendientes de envio.
   * <table border=1>
   *  <tr>  <td><b>Key:</b></td>
   *	    <td>{@link ID_TPDU} fuente que se tiene que asentir.</td>
   *  </tr>
   *  <tr>  <td><b>Value:</b></td>
   *	    <td>Número de ráfaga como un objeto Integer.</td>
   *  </tr>
   * </table>
   * @see RegistroHSACK
   * @see TPDUHSACK
   */
  private ListaOrdID_TPDU listaHSACKPendientesDeEnviar = null;


  /**
   * Almacena instancias de {@link RegistroVectorMACK} con información de los MACK que
   * están pendientes de envío.
   * @see TPDUMACK
   */
  private Vector vectorMACK = null;

  /**
   * Almacena la información de los TPDU Datos  que han sido solicitados para
   * retransmisión multicast.
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
   * Almacena la información de los TPDU Datos  que han sido solicitados para
   * retransmisión unicast.
   * <table border=1>
   *  <tr>  <td><b>Key:</b></td>
   *	    <td>{@link ID_TPDU} fuente solicitado para retransmitir.</td>
   *  </tr>
   *  <tr>  <td><b>Value:</b></td>
   *	    <td>TreeMap con los {@link ID_Socket} que han solicitado la retransmisión
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
   * Almacena el {@link ID_Socket} fuente y el {@link NumeroSecuencia número de secuencia}
   * de los {@link TPDUDatosNormal TPDU Datos Normal} o {@link TPDUDatosRtx Rtx}.
   * recibidos o enviados multicast (han pasado por la red y los han visto todos
   * los hosts pertenecientes al Grupo Local) durante la 2ª mitad de RTT. <br>
   * <table border=1>
   *  <tr>  <td><b>Key:</b></td>
   *	    <td>{@link ID_Socket} de la fuente del TPDU.</td>
   *  </tr>
   *  <tr>  <td><b>Value:</b></td>
   *	    <td>Número de secuencia mayor recibido o enviado multicast.</td>
   *  </tr>
   * </table>
   */
  private TreeMap treeMapIDs_TPDU2MitadRTT = null;

  /** Identificación de este socket */
  private ID_Socket id_SocketLocal = null;

  /**
   * Referencia al socketPTMF que crea este hilo.
   */
  private SocketPTMFImp socketPTMFImp = null;


  /**
   * Almacena información de los sockets que envían {@link TPDUDatosNormal},
   * es decir, de los sockets que son fuente de datos.
   */
  private TablaID_SocketsEmisores tablaID_SocketsEmisores = null;


  /**
   * Almacena información de los ID_TPDU para los cuales este socket tiene que
   * recibir un asentimiento positivo.<br>
   * Es decir, almacena los ID_TPDU de los TPDU de datos que tiene el bit ACK
   * activado y para los que este socket es su CG local.
   */
  private TablaAsentimientos tablaAsentimientos = null;

  /**
   * Almacena toda la información relativa a los CG locales.
   */
  private TablaCGLocales tablaCGLocales = null;

  /**
   * Contiene los {@link ID_TPDU} que han agotado las oportunidades máximas de espera
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
   * Almacena los id_socket que se han añadido y de los que no se tiene constancia de
   * que estén sincronizados con todos los emisores fuentes. Sólo es utlizado en el modo
   * de fiabilidad retrasada.
   * La clave es id_socket y el valor un treeMap cuya clave es un emisor fuente
   * (id_socket), y su valor un Boolean, que indica si para dicho emisor ya se
   * ha RTX algún TPDU en el que figure el id_socket que se añadio. Si el valor
   * booleano es true indica que el socket ya está sincronizado para dicho emisor.
   * Si es false que ya se ha rtx algún tpdu de dicho emisor en el que figura el socket en
   * su lista de no asentidos.
   */
  private TreeMap treeMapID_SocketNoSincronizado = null;

  /**
   * Almacena los idgl que se han añadido y de los que no se tiene constancia de
   * que estén sincronizados con todos los emisores fuentes. Sólo es utlizado en el modo
   * fiabilidad retrasada.
   * La clave es idgl y el valor un treeMap cuya clave es un emisor fuente (id_socket),
   * y su valor un Boolean, que indica si para dicho emisor ya se ha RTX algún
   * TPDU en el que figure el idgl que se añadio. Si el valor booleano es
   * true indica que el idgl ya está sincronizado para dicho emisor. Si es false
   * que ya se ha rtx algún tpdu de dicho emisor en el que figura el idgl en
   * su lista de no asentidos.
   */
  private TreeMap treeMapIDGLNoSincronizado = null;

  /**
   * Información de los <b>nuevos sockets emisores </b>que surgan durante la comunicación.
   * Es utilizado sólo en el modo fiable retrasado para asegurar la sincronización
   * de los hijos y vecinos existentes.
   * Indexado por el id_socket del emisor fuente al que se asocia el id_tpdu inicial
   * de dicho emisor (al menos para este socket). Se eliminarán del treemap cuando
   * todos los hijos y vecinos hayan semiasentido, al menos, el id_tpdu asociado.
   */
  private TreeMap treeMapNuevosEmisoresASincronizar = null;

  /**
   * Almacena los id_socket emisores que tienen pendientes datos para entrega
   * al usuario y no se han entregado por estar la cola de recepción llena.
   */
  TreeMap treeMapID_SocketEntregarUsuario = null;

  /** Ventana de emisión con los TPDUDatosNormal que tienen que ser enviados. <br>
      Sólo utilizado si este socket es fuente de datos.*/
  VentanaEmision  ventanaEmision = null;

  /** Número de secuencia inicial de la ventana de emisión. <br>
      Sólo utilizado si este socket es fuente de datos.*/
  private long N_SECUENCIA_INICIAL = 1;

  /** Número de secuencia del siguiente TPDU datos a añadir a la ventana de emisión. <br>
      Sólo utilizado si este socket es fuente de datos.*/
  private NumeroSecuencia NSecuencia = null;

  /**
   * Número de Ráfaga del siguiente TPDU datos a añadir a la ventana de emisión.
   * Comienza numeración en 1.<br>
   *  Sólo utilizado si este socket es fuente de datos.
   */
  private int iNRafaga = 1;

  /** Se ha enviado un TPDU de datos con el bit fin emisión a true.<br>
      Sólo utilizado si este socket es fuente de datos. */
  boolean bYaEnviadoFinEmision = false;

  /** Número de TPDUs máximos que puede contener una ráfaga.<br>
      Sólo utilizado si este socket es fuente de datos. */
  private int iTPDUsRafaga = 0;

  /** Número de TPDUs consecutivos añadidos a la vta. emisión sin el bit ACK a 1.<br>
      Sólo utilizado si este socket es fuente de datos.*/
  private int iTPDUsConsecutivosSinACK = 0;

  /** Número de TPDU que se pueden enviar entre dos TPDU's con el bit ACK activado.
      Cómo máximo puede ser el tamaño de la ventana de emisión menos 1, para
      asegurar que siempre hay un TPDU de datos con el bit ACK activado en la
      ventana de emisión.<br>
      Sólo utilizado si este socket es fuente de datos. */
  private int iACKCadaXTPDUS = PTMF.ACKS_CADA_X_TPDUS;

  /** Boolean de inicio de ráfaga.<br>
      Sólo utilizado si este socket es fuente de datos. */
  private boolean bSetIR = true;

  /** Boolean de asentimiento de TPDU.<br>
      Sólo utilizado si este socket es fuente de datos. */
  private boolean bSetACK = false;

  /** Boolean de fin de conexión.<br>
      Sólo utilizado si este socket es fuente de datos. */
  private boolean bSetFIN_CONEXION = false;

  /** Boolean para time out por inactividad. Si vale true hay que enviar
      un TPDU de datos para evitar que el resto de miembros crean que hemos
      dejado de emitir.<br>
      Sólo utilizado si este socket es fuente de datos.
      */
  private boolean bTime_out_inactividad = false;

  /** Boolean para time_out último TPDU de datos enviado sin el bit ACK activado.
      Si vale true hay que enviar un TPDU de datos con el bit ACK activado. <br>
      Sólo utilizado si este socket es fuente de datos.*/
  private boolean bTime_out_ultimoTPDUSinACK = false;

  /** Número de bytes de usuario enviados. Sólo cuenta los bytes enviados en
      los TPDU de datos normal enviados.<br>
      Sólo utilizado si este socket es fuente de datos. */
  private long lNBytesUsuarioEnviados = 0;

  /** Ratio que no tiene que ser superado para el envío de nuevos datos de
      usuario. <br>
      Sólo utilizado si este socket es fuente de datos.*/
  private long lRatioUsuario_Bytes_x_seg = PTMF.RATIO_USUARIO_BYTES_X_SEG;

  /** Indica que este socket no va a enviar más datos al grupo multicast. */
  private boolean bFIN_EMISION = false;

  /** Indica si se tiene que comprobar la entrega de datos al usuario. Será
      verdadera cuando se haya intentado añadir a la cola de recepción sin éxito
      (está llena).*/
  private boolean bComprobarEntregaDatosUsuario = false;

  /** Boolean que indica si este socket va a dejar la comunicación. */
  private boolean bABANDONAR_COMUNICACION = false;

  /** Si vale true, el socket abandona la comunicación de forma estable,
      y si vale false, de forma inmediata.*/
  private boolean bORDENADO = false;

  /**
   * Para cada socket fuente de datos almacena el último número de secuencia
   * entregado al usuario.
   * <br>Sólo usado en el modo PTMF_NO_FIABLE_ORDENADO.
   * <table border=1>
   *  <tr>  <td><b>Key:</b></td>
   *	    <td>{@link ID_Socket} fuente.</td>
   *  </tr>
   *  <tr>  <td><b>Value:</b></td>
   *	    <td>Número de secuencia último entregado al usuario.</td>
   *  </tr>
   * </table>
   */
  private TreeMap treeMapNSecUltimoEntregado = null;

  // MÁSCARAS para activar varias posibles opciones con un sólo parámetro.
  /**  */
  private static final int iSIN_OPCION                         = 0x00000000;
  /** Indica que un id_tpdu ha sido solicitado en un HNACK recibido por unicast. */
  private static final int iPEDIDO_POR_UNICAST                 = 0x00000001;
  /** Indica que un id_tpdu ha sido solicitado en un NACK o HNACK enviado por un
      miembro del grupo local */
  private static final int iPEDIDO_POR_VECINO                  = 0x00000002;
  /** Indica que un id_tpdu ha sido solicitado en un NACK o HNACK enviado por un
      miembro de un grupo local hijo jerárquico. */
  private static final int iPEDIDO_POR_HIJO                    = 0x00000004;
  /** Indica que se tiene que rtx si no está sincronizado. */
  private static final int iRTX_SI_NO_SINCRONIZADO             = 0x00000008;
  /** Indica que se tiene que rtx si no está sincronizado y es la primera rtx.
      realizada para el id_socket fuente no sincronizado. */
  private static final int iRTX_SI_NO_SINCRONIZADO_Y_ES_1ª_RTX = 0x00000010;
  /** Indica que se rtx. sólo a los hijos. */
  private static final int iRTX_SOLO_A_HIJOS                   = 0x00000020;
  /** Indica que se rtx. sólo a los vecinos */
  private static final int iRTX_SOLO_A_VECINOS                 = 0x00000040;
  /** Indica que se rtx. si contiene identificadores de id_socket o IDGLS que
      no estén sincronizados. */
  private static final int iRTX_SI_CONTIENE_ID_NO_ASENTIDOS    = 0x00000080;


  //-----------------------------------------------------------------------
  // VARIABLES RELACIONADAS CON EL TIEMPO..
  //-----------------------------------------------------------------------
  // Cuando estas variables valen cero indica que no se tiene que esperar o
  // que ha finalizado el tiempo de espera.

  /** Generador de números aleatorios. */
  private Random random  = null;

  /**
   * Tiempo mínimo de espera entre envios consecutivos de cualquier TPDU (Normal,
   * Rtx, ACK, HACK, ...). Cuando es igual a cero indica que la espera ha finalizado.
   */
  private long lT_Min_Entre_Envios = 0;

  /**
   * Tiempo mínimo de espera entre envíos de nuevos datos de usuario al grupo
   * multicast (milisegundos).
   */
  private long lT_Out_Ajustar_Ratio_Usuario = 0;

  /** Tiempo máximo de inactividad de este socket.<br>
      Sólo utilizado si este socket es fuente de datos.*/
  private long lT_TIME_OUT_INACTIVIDAD = (PTMF.TIEMPO_MAX_INACTIVIDAD_EMISOR * 2) / 3  ;

  /**
   * Tiempo aleatorio de espera antes de enviar un {@link TPDUMACK}.
   * Cuando es igual a cero indica que la espera ha finalizado. */
  private long lTRandomMACK = 0;

  /** Indica el modo en el que este socket está actuando, por defecto fiable.*/
  private int iMODO_FIABILIDAD = PTMF.PTMF_FIABLE;

  /**
   * Indica si ha finalizado, al menos una vez, RTT. <br>
   * No asegura que sólo haya finalizado una vez.
   */
  private boolean bFinRTT = false;

  /**
   * Si es true se tiene que comprobar el tiempo que lleva cada emisor sin
   * haber enviado algún tpdu.
   */
  private boolean bComprobarInactividadEmisores;

  //-----------------------------------------------------------------------
  // Eventos que se registran en el primer argumento de la función Callback
  // y que son disparados por el Temporizador.
  //-----------------------------------------------------------------------
  /** Evento disparado en cada vencimiento de RTT. */
  private static final int iEVENTO_RTT = 1;

  /** Evento de consumido el número de RTT máximo para un ID_TPDU.*/
  private static final int iEVENTO_FIN_INTENTOS = 2;

  /** Evento de finalización del tiempo aleatorio para poder enviar un MACK */
  private static final int iEVENTO_FIN_T_RANDOM_MACK = 3;

  /** Evento de finalización del tiempo mínimo entre envios */
  private static final int iEVENTO_T_MIN_ENTRE_ENVIOS = 4;

  /** Evento de finalización del tiempo de espera para comprobar inactividad */
  private static final int iEVENTO_COMPROBAR_INACTIVIDAD_EMISORES = 5;

  /** Evento Time Out utilizado para enviar TPDUs vacíos cada T_TIME_OUT seg*/
  private static final int iEVENTO_TIME_OUT_INACTIVIDAD = 6;

  /** Evento Time Out utilizado para enviar TPDUs vacíos cada T_TIME_OUT seg*/
  private static final int iEVENTO_TIME_OUT_ULTIMO_TPDU_SIN_ACK = 7;

  /** Evento de consumido el tiempo de espera para que un nuevo SOCKET
      esté sincronizado. (Sólo utilizada en modo fiable retrasado.)
      Al registrar en el temporizador se registrará también el id_socket
      al cual se le ha acabado el tiempo para sincronización.
      */
  private static final int iEVENTO_FIN_SINCRONIZACION_ID_SOCKET = 8;

  /** Evento de consumido el tiempo de espera para que un nuevo IDGL
      esté sincronizado. (Sólo utilizada en modo fiable retrasado.)
      Al registrar en el temporizador se registrará también el idgl
      al cual se le ha acabado el tiempo para sincronización.
      */
  private static final int iEVENTO_FIN_SINCRONIZACION_IDGL = 9;

  /** Evento de consumido el tiempo de espera para ajustar el ratio máximo
      al que pueden ser enviados los datos. Sólo utilizada en el caso de que
      este socket sea fuente de datos.
   */
  private static final int iEVENTO_T_OUT_AJUSTAR_RATIO_USUARIO = 10;

  /**
   * Cuenta el número de veces que la función enviarTPDUFiable ha ejecutado
   * la función enviarDatosSolicitadosRtx(). Se ejecuta una vez enviarDatosSolicitadoRtx ()
   * por cada cinco ejecuciones de enviarTPDUFiable().
   */
  private int iContadorIteracionEnviarTPDUFiable = 0;

  /** Cuenta de inactividad*/
  private int iInactividadEmisor  = 0;

  /** Cuenta de inactividad*/
  private int iInactividadReceptor  = 0;

  /** Vector con los id_sockets e idgls añadidos.
      CGL almacena los ID_Socket y los IDGL que se incorporan a la comunicación.
      El thread CGLThread añade los identificadores y el thread DatosThread los
      elimina. Al vector se puede acceder de forma concurrente.*/
  private Vector vectorIDAñadidos = null;

  /** Vector con los id_sockets e idgls eliminados
      CGL almacena los ID_Socket y los IDGL que abandonan la comunicación
      El thread CGLThread añade los identificadores y el thread DatosThread los
      elimina. Al vector se puede acceder de forma concurrente.*/
  private Vector vectorIDEliminados = null;



  //==========================================================================
  /**
   * Inicializa todas las variables utilizadas.
   * @param socketPTMFImpparam referencia al socketPTMFImp que crea este thread.
   * @param modo_fiabilidad indica el modo en que está actuando este socket,
   * puede ser:
   * <ul>
   *    <li>{@link PTMF#PTMF_FIABLE}</li>
        <li>{@link PTMF#PTMF_FIABLE_RETRASADO}</li>
   *    <li>{@link PTMF#PTMF_NO_FIABLE}</li>
   *    <li>{@link PTMF#PTMF_NO_FIABLE_ORDENADO}</li>
   * </ul>
   * Si no es niguno de los anteriores el socket actúa en modo NO FIABLE.
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
       this.vectorIDAñadidos = new Vector ();
       this.vectorIDEliminados = new Vector ();

       // Registrar función para llamar cada fin de RTT
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
   * Método run del thread. Lanzado cuando el socketPTMF es abierto.
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
    // Bucle infinito de procesamiento de los TPDUs de datos tanto de emisión
    // como de recepción
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
      // 2. ESPERAR HASTA QUE EL GRUPO MULTICAST ESTÉ ACTIVO Y CON ELLO
      //   EL CGL THREAD Y TODAS LAS VARIABLES DE SOCKETPTMFIMP
      //-------------------------------------------------------------------
      if (!this.socketPTMFImp.isGrupoMcastActivo())
      {
        this.socketPTMFImp.getTemporizador().sleep(1);
        continue; // Esperar hasta que esté activo.
      }



      //-------------------------------------------------------------------
      // 3. Comprobar si hay que quitar algún emisor por no emitir.
      //-------------------------------------------------------------------
      if (this.bComprobarInactividadEmisores)
       {
        Iterator iteradorID_Socket =
           this.tablaID_SocketsEmisores.comprobarInactividad().keySet().iterator();

        // Eliminar todos los que hayan superado el tiempo máx. de inactividad.
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
       // 4. Comprobar si se ha intentado añadir a la cola de recepción y estaba llena
       // en cuyo caso, si en la cola de recepción hay espacio intentar entregar
       // datos al usuario. Si no se entrega al usuario no se podrá vaciar las
       // ventanas de recepción cuando se llenen. No se comprueba si la cola está
       // llena porque puede ser que tenga capacidad disponible pero no la suficiente
       // para añadir nuevos datos, por ejemplo: tiene 1 byte libre, pero todo lo
       // que se intenta añadir supera 1 byte de tamaño.
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
      // 5. Ver si hay TPDUDatos recibidos que analizar. Como máximo se procesan
      //    PTMF.MAX_TPDU_PROCESAR_CONSECUTIVAMENTE TPDU datos consecutivamente,
      //    es decir, antes de enviar ningún TPDU.
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
      // 6. Comprobar si hay algún paquete listo para ser enviado y lo envía.
      //-------------------------------------------------------------------
      this.enviarTPDU ();

      //-------------------------------------------------------------------
      // 7. Comprueba los id_socket e IDGL que han abandonado el grupo sin avisar.
      // Sólo si estamos en modo fiable.
      //-------------------------------------------------------------------
      this.comprobarlistaIDs_TPDUNoRecibidoAsentimiento ();

      //-------------------------------------------------------------------
      // 8. Comprobar los ID_Sockets e idgls añadidos
      //-------------------------------------------------------------------
      this.comprobarIDAñadidos ();

      //-------------------------------------------------------------------
      // 9. Comprobar los ID_Sockets e idgls eliminados
      //-------------------------------------------------------------------
      this.comprobarIDEliminados ();


     //-------------------------------------------------------------------
     // 10. Comprobación de inactividad. Dormir algunos instantes el thread
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
   * Comprueba los id_sockets e idgls que se han añadido al vectorIDAñadidos
   */
  private void comprobarIDAñadidos ()
  {
   // Comprueba los nuevos ID_Socket o idgls que se han añadido al grupo.
   if (this.vectorIDAñadidos != null
       &&
       !this.vectorIDAñadidos.isEmpty ())
      {
        for (int i=0;i<this.vectorIDAñadidos.size();i++)
         {
          Object obj = this.vectorIDAñadidos.remove(0);
          if (obj instanceof ID_Socket)
               this.añadirID_Socket ((ID_Socket)obj);
          if (obj instanceof IDGL)
               this.añadirIDGL ((IDGL)obj);
         }
      }
  }

  //==========================================================================
  /**
   * Comprueba los id_sockets e idgls que se han añadido al vectorIDEliminados
   */
  private void comprobarIDEliminados ()
  {
   // Comprueba los nuevos ID_Socket o idgls que se han añadido al grupo.
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
   * Elimina un id_socket fuente de datos. No lo elimina de la comunicación
   * sólo lo anula como fuente de datos.
   * @param id_socketFuente id_socket fuente a eliminar.
   * @param msg mensaje a enviar al usuario, si es null no se envía ninguno.
   * Explica el motivo de la eliminación.
   */
  private void eliminarID_SocketFuente (ID_Socket id_socketFuente,String msg)
  {
   final String mn = "DatosThread.eliminarID_SocketFuente (id_socketFuente,msg)";

    if (id_socketFuente==null)
      return;

    // ¿Puede ser este socket?
    if (id_socketFuente.equals (this.id_SocketLocal))
      {
       this.ventanaEmision = null;
      }
    else // Borrar al emisor de la tabla de emisores
        {
         // Comprobar si tenía aún datos que entregar al usuario.
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
   * Procesa los TPDU Datos que se reciben cuando el socket esté actuando en los
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
       /** Actualizar tiempo de última recepción del emisor del tpduDatos*/
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
   * Procesa los TPDU Datos que se reciben cuando el socket esté actuando en los
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
   * Procesa un TPDU datos normal recibido cuando el socket está en los modos:
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
   * Procesa un tpdu de datos normal recibido cuando el socket está en los modos:
   *  <ul>
   *     <li>{@link PTMF#PTMF_FIABLE}</li>
   *     <li>{@link PTMF#PTMF_FIABLE_RETRASADO}</li>
   *  </ul>
   * Las operaciones que realiza son:
   * <ul>
   *    <li>Comprueba si es el primer tpdu que ha enviado, en cuyo caso añade un
   * nuevo emisor a la tablaID_SocketsEmisores.</li>
   *    <li>Si no es el primero y no tiene registrado al emisor solicitar  el
   * número de secuencia 1.</li>
   *    <li>Almacena el tpdu en la ventana de recepción del socket fuente del tpdu.</li>
   *    <li>Comprobar si tiene un CG Local asignado, sino intentar ser su CG Local.</li>
   *    <li>Si se tiene que enviar asentimiento (bit ACK) apuntar para enviarlo o
   * recibirlo si somos su CG Local.</li>
   *    <li>Quitar de la lista de peticiones (NACK o HNACK).</li>
   *    <li>Comprobar si se pueden entregar datos al usuario.</li>
   *    <li>Llamar a limpiarVentana para comprobar si se pueden eliminar TPDU
   * de la ventana de recepción asociada al emisor fuente del TPDU de datos normal.</li>
   *    <li>Calcular si se han perdido tpdu observando el número de secuencia del
   * recibido con los de la ventana de recepción.</li>
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
   // Si el número de ráfaga es cero indica que el que lo envió está en modo
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
   // 2. Obtener Ventana de recepción parar guardar el TPDU

   // Guardar el TPDU Datos en la ventana de recepción del socket que lo envió.
   VentanaRecepcion vtaRecp = this.tablaID_SocketsEmisores.getVentanaRecepcion (id_SocketSrc);

   if (vtaRecp == null)
   {    // No esta registrado como emisor.
        // Si IR activo y Número Ráfaga 1 crear ventana de tpduDatosNormal.getTamañoVentana()
        // Tiene que ser el primero para así obtener el número de secuencia inicial.

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
                                               tpduDatosNormal.getTamañoVentana(),
                       /*Número Sec Inicial*/  nSec);

              bNuevoEmisor = true;
              // Informar al usuario de donde hemos comenzado a recibir del emisor.
              this.socketPTMFImp.sendPTMFEventConexion("Comienzo emisión del id_socket: " + id_SocketSrc);

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
              // Sólo pido si se está en la primera ráfaga.
              if (tpduDatosNormal.getNumeroRafaga()!=1)
                return;

              try { // Pedir el 1º.
               ID_TPDU id_tpduAux = new ID_TPDU (id_SocketSrc,NumeroSecuencia.LIMITEINFERIOR);
               Log.log (mn,"Añadido a gestion asent neg : " + id_tpduAux);
               this.gestionAsentNeg.addID_TPDU (id_tpduAux);
              }catch (ParametroInvalidoExcepcion e)
                 {
                  Log.log(mn,e.toString());
                 }
              return;
             } // Fin del else
      } // Fin el if

   try {

      // Si el tpduDatosNormal es mayor al número de secuencia final
      // Sería bueno comprobar si es menor a numsecfinal + tamañoVentana*X
      // llamar a limpiarVentana (id_Socket)
      if ( nSec.tolong() > vtaRecp.getNumSecFinal() )
      {
           Log.log (mn,"Superada el último de la ventana.");
           this.limpiarVentanaHastaPrimerTPDUConACK (tpduDatosNormal.getID_SocketEmisor());
      }

      vtaRecp.addTPDUDatosNormal (nSec.tolong(),tpduDatosNormal);
      vtaRecp.setCapacidadVentana (tpduDatosNormal.getTamañoVentana());
      this.actualizarListaID_TPDU2MitadRTT (tpduDatosNormal.getID_TPDU ());
   }catch (ParametroInvalidoExcepcion e)
     { // El paquete está erróneo.
       Log.log (mn,"El TPDU es erróneo, por lo que es tirado");
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

   // Actualizamos la ráfaga
   this.tablaCGLocales.actualizarNumSecInicial (id_SocketSrc,iNumRafaga,nSec);

   this.gestionAsentNeg.removeID_TPDU (tpduDatosNormal.getID_TPDU());

   // Entregar los datos posibles al usuario.
   this.tablaID_SocketsEmisores.entregaDatosUsuario (tpduDatosNormal.getID_SocketEmisor(),
                                        this.socketPTMFImp.getColaRecepcion());

   this.limpiarVentana (tpduDatosNormal.getID_SocketEmisor());

   // Compruebo si supone averiguar la pérdida de algún TPDU.
   this.comprobarID_TPDUNoRecibidos (tpduDatosNormal.getID_TPDU());

   // En limpiar ventana se comprueba el bit fin
  }

  //==========================================================================
  /**
   * Procesa un tpdu de datos retransmitido recibido cuando el socket está en
   * los modos:
   *  <ul>
   *     <li>{@link PTMF#PTMF_FIABLE}</li>
   *     <li>{@link PTMF#PTMF_FIABLE_RETRASADO}</li>
   *  </ul>
   * Las operaciones que realiza son:
   * <ul>
   *    <li>Comprueba si ha sido enviado por un socket de un grupo local padre, o
   * por uno del mismo grupo local.</li>
   *    <li>Comprueba si es el primer tpdu que ha enviado, en cuyo caso añade un
   * nuevo emisor a la tablaID_SocketsEmisores. Si el modo es FIABLE_RETRASADO,
   * sólo actualiza las tablas si figura en su lista de no asentidos. Enviar
   * evento de información al usuario.</li>
   *    <li>Quitar de la lista de peticiones (NACK o HNACK),</li>
   *    <li>Si no es el primero y no tiene registrado al emisor solicitar  el
   * número de secuencia 1.</li>
   *    <li>Si ha sido rtx. por un miembro del mismo grupo local:
   *        <ul>
   *          <li>Actualizar la tabla de CG locales.</li>
   *          <li>Si somos el CG, tiene el bit ACK activado y no se había
   *              recibido antes poner para enviar asentimiento positivo.</li>
   *          <li>Si no somos CG y estamos en su lista de no asentidos, anotar
   *              para enviar asentimiento positivo.</li>
   *        </ul>
   *    </li>
   *    <li>Si ha sido rtx. por un miembro de un grupo local padre jerárquico:
   *        <ul>
   *          <li>Si no tiene CG asignado, registrar MACK para enviarlo.</li>
   *          <li>Si tiene el bit ACK activado y pertenece a la ventana de
   *              recepción correspondiente, anotar para enviar asentimiento
   *              positivo.</li>
   *          <li>Si figuramos en su lista de IDGL que no han enviado asentimiento,
   *               anotar para enviar un HSACK, y si además es su CG y pertenece
   *               a la ventana de recepción correspondiente, anotar para enviar
   *               asentimiento positivo.</li>
   *        </ul>
   *    </li>
   *    <li>Si tiene el bit IR a 1, actulizar la tabla de CG locales.</li>
   *    <li>Comprobar si se pueden entregar datos al usuario.</li>
   *    <li>Llamar a limpiarVentana para comprobar si se pueden eliminar TPDU
   * de la ventana de recepción asociada al emisor fuente del TPDU de datos normal.</li>
   *    <li>Calcular si se han perdido tpdu observando el número de secuencia del
   * recibido con los de la ventana de recepción.</li>
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


     // Tiene que venir de un miembro de mi grupo local o de un padre jerárquico.
     bRtxPorMismoGrupoLocal = tpduDatosRtx.getIDGL().equals (
                                      this.socketPTMFImp.getCGLThread().getIDGL());

     if (!bRtxPorMismoGrupoLocal)
     // Ver si viene de un padre jerárquico
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
            // Si IR activo y Número Ráfaga 1 crear ventana de
            // tpduDatosNormal.getTamañoVentana(). Tiene que ser el primero
            // para así obtener el número de secuencia inicial.
            if ( !(tpduDatosRtx.getIR()
                  &&
                  (tpduDatosRtx.getNumeroRafagaFuente()==1)) )

              { // Tirar el TPDU.
                // Sólo pido si se está en la primera ráfaga.
                if (tpduDatosRtx.getNumeroRafagaFuente()!=1)
                        return;

                // No pido este. Se supone que se han perdido varios
                // this.gestionAsentNeg.addID_TPDU (tpduDatosRtx.getID_TPDUFuente());
                // Pido el de Numero de secuencia 1.
                ID_TPDU id_tpduAux = new ID_TPDU (
                             tpduDatosRtx.getID_TPDUFuente().getID_Socket(),
                                        NumeroSecuencia.LIMITEINFERIOR);
                //Log.log (mn,"Añadido a gestion asent neg: " + id_tpduAux);
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


        // Añadir el nuevo emisor y obtener su ventana de recepción.
        this.tablaID_SocketsEmisores.addID_Socket (
                                 tpduDatosRtx.getID_TPDUFuente().getID_Socket(),
                                 tpduDatosRtx.getIDGLFuente(),
                                 tpduDatosRtx.getTamañoVentana(),
                /*Núm Sec Inic*/ nSec);


        if (tpduDatosRtx.getIR() && tpduDatosRtx.getNumeroRafagaFuente()==1)
         {
          Log.log (mn,"Comienzo  de emisión de la fuente: " + tpduDatosRtx.getID_TPDUFuente().getID_Socket());
          this.socketPTMFImp.sendPTMFEventConexion("Comienzo de emsion del id_socket: " +
                                tpduDatosRtx.getID_TPDUFuente().getID_Socket());
         }
        else
         {
          Log.log (mn,"Comienzo  de emisión RETRASADA de la fuente: " + tpduDatosRtx.getID_TPDUFuente().getID_Socket());
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
           // Si el tpduDatosNormal es mayor al número de secuencia final
           // Sería bueno comprobar si es menor a numsecfinal + tamañoVentana*X
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
           vtaRec.setCapacidadVentana (tpduDatosRtx.getTamañoVentana());
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

    // Sólo actualizamos la tabla de CG si este TPDU viene de mi grupo local.
    if (bRtxPorMismoGrupoLocal)
       {
        NumeroSecuencia nSecInicRafaga = this.tablaCGLocales.getNumSecInicialRafaga
                                 ( tpduDatosRtx.getID_TPDUFuente().getID_Socket(),
                                   tpduDatosRtx.getNumeroRafagaFuente());

        ID_Socket id_socketCGLocal = actualizarTablaCGLocales (
           /*id_socket Fuente */tpduDatosRtx.getID_TPDUFuente().getID_Socket(),
           /*N. Ráfaga        */tpduDatosRtx.getNumeroRafagaFuente(),
           /*nSecInicRáfaga   */(nSecInicRafaga!=null?nSecInicRafaga:
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
          // Si figuro, almacenar la información sobre el ACK que queda pendiente
          // de envío.
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

           // Si todavía pertenece a la ventana de recepción, aunque no lo tenga,
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
            { // No ha recibido ningún asentimiento de mi grupo local.

              // Cualquier socket del grupo local puede mandar el HSACK por
              // si se ha caido el CG Local.
              this.actualizarHSACKPendientesDeEnviar
                                         (tpduDatosRtx.getID_TPDUFuente(),
                                          tpduDatosRtx.getNumeroRafagaFuente(),
                      /*Pedido en rtx.*/  true);

              // SI NO ESTAMOS EN SU LISTA ES PORQUE HA SIDO ASENTIDO CON ANTERIORIDAD.

              // Si somos su CG local apuntar para enviar el HACK, sólo si todavía
              // lo tenemos en la ventana. Quiere decir que probablemente no se haya
              // enviado un HACK. (Cuando se envía un HACK se elimina de la ventana)
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
   * Procesa un tpdu ACK, sólo si es el CG para el ID_TPDU que están asintiendo.
   * Si no lo esperaba, y el número de secuencia pertenece a la ventana, lo añade
   * a this.tablaAsentimientos.
   * @param tpduACK tpdu ACK a procesar.
   */
  private void procesarTPDUACK (TPDUACK tpduACK)
  {
  final String mn = "DatosThread.procesarTPDUACK";

  // Comprobar que somos el CG Local de dicha ráfaga.
  ID_Socket id_socketCGLocal = this.tablaCGLocales.getCGLocal (tpduACK.getID_TPDUFuente().getID_Socket(),
                                                               tpduACK.getNumeroRafagaFuente());

  if (id_socketCGLocal==null)
    return;

  // Comprobar si somos CG Local del id_tpdu que me están asintiendo
  // Nota: EL Emisor siempre es el CGL de todos los paquetes enviados por el.
  if (!id_socketCGLocal.equals (this.id_SocketLocal))
    return;

  // Puede ser que el tpdu que me están asintiendo no lo hayamos recibido,
  // por lo que no estamos en espera de asentimiento por él.
  if (!this.tablaAsentimientos.contieneID_TPDU (tpduACK.getID_TPDUFuente()))
     {
      // Comprobar si el tpdu que me están asintiendo, todavía pertenece
      // a la ventana correspondiente, aunque el propio TPDU no lo tenga.
      if (tpduACK.getID_TPDUFuente().getID_Socket().equals (this.id_SocketLocal))
       { // Buscar en la ventana de emisión
         if (this.ventanaEmision==null)
             return;

         if (!this.ventanaEmision.nSecEntreInicialyFinal(tpduACK.getID_TPDUFuente().
                                        getNumeroSecuencia().tolong()))
           return;
      }
      else // Buscar en la ventana de recepción
          {
            VentanaRecepcion vtaRecp = this.tablaID_SocketsEmisores.
                  getVentanaRecepcion(tpduACK.getID_TPDUFuente().getID_Socket());
            if (vtaRecp==null)
               return;

            if (!vtaRecp.nSecEntreInicialyFinal(tpduACK.getID_TPDUFuente().
                                        getNumeroSecuencia().tolong()))
              return;
          } // Fin del else

      // Añadir a la tabla de hijos
      this.ponerEnEsperaAsentimiento (tpduACK.getID_TPDUFuente(),
                                      tpduACK.getNumeroRafagaFuente(),false);

      // Actualizar la listas de HSACK pendientes de enviar.
      this.actualizarHSACKPendientesDeEnviar (tpduACK.getID_TPDUFuente(),
                                              tpduACK.getNumeroRafagaFuente(),
                                              false);

     }

  Log.debug(Log.ACK,mn,"RECIBIDO ACK UNICAST: " + tpduACK.getID_TPDUFuente()+" Rafaga: "+ tpduACK.getNumeroRafagaFuente());

  // Anotar el ACK recibido en la tabla de los hijos que están a mi cargo.
  this.tablaAsentimientos.addACK (tpduACK.getID_SocketEmisor(),tpduACK.getID_TPDUFuente());

  // Comprobar si TODOS han mandado asentimiento para el id_tpdu.
  this.limpiarVentana (tpduACK.getID_TPDUFuente(),false);

  // Actualizar la lista de id_socket no sincronizados
  // El id_socket que ha enviado el ACK está sincronizado para dicho emisor
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
   * Procesa un tpdu NACK.  Sólo se comprueba si es enviado por un miembro del
   * del grupo local.<br>
   * Las operaciones que realiza son:
   * <ul>
   *    <li>Comprueba aquellos ID_TPDU solicitados de los que es CG Local, retransmitiéndolo
   * si lo tiene.</li>
   *    <li>Actualiza los TPDUNACK que tiene y que sean equivalentes al recibido.</li>
   * </ul>
   * @param tpduNACK tpdu NACK a analizar.
   */
  private void procesarTPDUNACK (TPDUNACK tpduNACK)
  {
  final String mn = "DatosThread.procesarTPDUNACK";
  TPDUDatosNormal tpduDatosNormal;

    //Depuración ====================================
      Log.debug(Log.NACK,mn,"<-- RECIBIDO NACK: " + tpduNACK.getID_SocketEmisor());
      Log.debug(Log.NACK,"","TPDUs perdidos:");

      ListaOrdID_TPDU lista =  tpduNACK.getListaID_TPDU();
      Iterator itID_TPDU = lista.iteradorID_TPDU();

      while(itID_TPDU.hasNext())
      {
        Log.debug(Log.NACK,"",""+(ID_TPDU)itID_TPDU.next());
      }
    //Depuración ====================================

    // Comprobar si soy el CG Local para alguno de los TPDU solicitados.
    // Si lo soy, ver si tengo el TPDU solicitado. Se tiene que recorrer toda la
    // información del TPDU y ver si somos CG local de alguno de los indicados.

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

     // Este socket es la fuente de uno de los que está pidiendo.
     // Todo socket es CGLocal de los que contiene en su ventana de emisión.
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
     if (vtaRecep!=null) // 1º if
     {
      if (vtaRecep.recibido(nSec)) // 2º if
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
   // la función RtxTPDUFinRTT, por lo que se actualiza igual que si estuviésemos
   // en la 2ª mitad de rtt.

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
  //Depuración ====================================
     Log.debug(Log.HNACK,mn,"<-- Recibido un HNACK: " + tpduHNACK.getID_SocketEmisor());
      Log.debug(Log.HNACK,"","TPDUs perdidos:");

      ListaOrdID_TPDU lista =  tpduHNACK.getListaID_TPDU();
      Iterator itID_TPDU = lista.iteradorID_TPDU();

      while(itID_TPDU.hasNext())
      {
        Log.debug(Log.HNACK,"",""+(ID_TPDU)itID_TPDU.next());
      }
    //Depuración ====================================



   // Comprobar si soy el CG Local para alguno de los TPDU solicitados.
   // Si lo soy, ver si tengo el TPDU solicitado. Se tiene que recorrer toda la
   // información del TPDU y ver si somos CG local de alguno de los indicados.

   // Ver los cambios que se hagan en el envío de los HNACK en gestionAsentNeg
   // y tratar según lo indicado.

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
       // Por unicast sólo me pueden pedir los que yo emito (ventanaEmision)
       if (id_SocketSrc.equals (this.id_SocketLocal))
          this.actualizarListaID_TPDUSolicitadoRtx(id_tpduNext,tpduHNACK.getID_SocketEmisor(),
                                                   iPEDIDO_POR_UNICAST);
       continue;
      }

     // RECIBIDO POR MULTICAST
     // Comprobar si es hijo jerárquico para dicho id_tpdu o si ha sido enviado
     // por un miembro de mi grupo local, en cuyo caso también lo acepto
     // Obtener el IDGL de la fuente del id_tpdu que me están asentiendo.
     IDGL idglFuente = this.getIDGL (id_tpduNext.getID_Socket());
     TreeMap treeMapIDGLHijos = this.socketPTMFImp.getCGLThread().getCGHijos(idglFuente);
     bCondicion = (treeMapIDGLHijos==null?this.socketPTMFImp.getIDGL().equals(idglEmisor):
                                    treeMapIDGLHijos.containsKey (idglEmisor)
                                    ||
                                    this.socketPTMFImp.getIDGL().equals(idglEmisor));

     if (!bCondicion)
        continue;

     // Este host es la fuente de uno de los que está pidiendo.
     if (id_SocketSrc.equals(this.id_SocketLocal))
       {
        if (this.ventanaEmision==null)
                continue;

        this.actualizarListaID_TPDUSolicitadoRtx(id_tpduNext,tpduHNACK.getID_SocketEmisor(),
                                                 iPEDIDO_POR_HIJO);
        continue;
       }

     VentanaRecepcion vtaRecep = this.tablaID_SocketsEmisores.getVentanaRecepcion(id_SocketSrc);
     if (vtaRecep!=null) // 1º if
     {
      if (vtaRecep.recibido(nSec)) // 2º if
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
  // registrado como vecino. Por ejemplo: alguién que dí por caído y que realmente
  // no lo estaba, lo elimine como vecino pero sigue activo y ahora envía un MACK.

  // Sólo actualizo si es de mi mismo Grupo Local.
  if (tpduMACK.getIDGL().equals (this.socketPTMFImp.getCGLThread().getIDGL()))
     actualizarTablaCGLocales (
                /*id_socket Fuente*/tpduMACK.getID_SocketFuente(),
                /*N. Ráfaga       */tpduMACK.getNumeroRafagaFuente(),
                /*nSecInicRáfaga  */tpduMACK.getNumSecInicialRafaga(),
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

   // Comprobar que somos el CG Local de dicha ráfaga.
   ID_Socket id_socketCGLocal = this.tablaCGLocales.getCGLocal (tpduHSACK.getID_TPDUFuente().getID_Socket(),
                                                               tpduHSACK.getNumeroRafagaFuente());

   if ( id_socketCGLocal==null || !id_socketCGLocal.equals (this.id_SocketLocal) )
   {
    // Actualizamos la tabla de asentimientos, puede que no estemos esperando
    // asentimiento por el id_tpdu contenido en el tpduHSACK pero sí para otros
    // menores. En tabla de asentimientos se comprueba si es o no hijo jerárquico.
    this.tablaAsentimientos.addHSACK (tpduHSACK.getIDGL(),
                                     tpduHSACK.getID_TPDUFuente());
    return;
   }

   // Puede ser que el tpdu que me están asentiendo no lo hayamos recibido,
   // por lo que no estamos en espera de asentimiento por él.
   if (!this.tablaAsentimientos.contieneID_TPDU (tpduHSACK.getID_TPDUFuente()))
     {
      // Comprobar si el tpdu que me están asintiendo, todavía pertenece
      // a la ventana correspondiente, aunque el propio TPDU no lo tenga.
      if (tpduHSACK.getID_TPDUFuente().getID_Socket().equals (this.id_SocketLocal))
       {
         if (this.ventanaEmision==null)
           return;

         // Buscar en la ventana de emisión
         if (!this.ventanaEmision.nSecEntreInicialyFinal (tpduHSACK.getID_TPDUFuente().
                                getNumeroSecuencia().tolong()))
           return;
       }
       else // Buscar en la ventana de recepción
          {
            VentanaRecepcion vtaRecp = this.tablaID_SocketsEmisores.
                  getVentanaRecepcion(tpduHSACK.getID_TPDUFuente().getID_Socket());
            if (vtaRecp==null)
               return;
            if (!vtaRecp.nSecEntreInicialyFinal (tpduHSACK.getID_TPDUFuente().
                                getNumeroSecuencia().tolong()))
              return;
          } // Fin del else

       // Añadir a la tabla de hijos
       this.ponerEnEsperaAsentimiento (tpduHSACK.getID_TPDUFuente(),
                                      tpduHSACK.getNumeroRafagaFuente(),false);

       // No pongo enviar HSACK, puesto que con esta información no garantizo que
       // haya sido recibido por alguno de mi grupo local o yo mismo.
     } // Fin del if

   // Anotar el HSACK recibido en la tabla de los hijos que están a mi cargo.
   this.tablaAsentimientos.addHSACK (tpduHSACK.getIDGL(),
                                      tpduHSACK.getID_TPDUFuente());

   //Log.log (mn,"RECIBIDO HSACK : " + tpduHSACK.getID_TPDUFuente());

   // Actualizar la lista de IDGL no sincronizados
   // El IDGL que ha enviado el HSACK está sincronizado para dicho emisor
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

    // Comprobar que somos el CG Local de dicha ráfaga.
    ID_Socket id_socketCGLocal = this.tablaCGLocales.getCGLocal (tpduHACK.getID_TPDUFuente().getID_Socket(),
                                                               tpduHACK.getNumeroRafagaFuente());

    if (id_socketCGLocal==null || !id_socketCGLocal.equals (this.id_SocketLocal) )
     {
      // Actualizamos la tabla de asentimientos, puede que no estemos esperando
      // asentimiento por el id_tpdu contenido en el tpduHACK pero sí para otros
      // menores. En tabla de asentimientos se comprueba si es o no hijo jerárquico.
      this.tablaAsentimientos.addHACK (tpduHACK.getIDGL(),
                                     tpduHACK.getID_TPDUFuente());
      return;
     }

    // Puede ser que el tpdu que me están asentiendo no lo hayamos recibido,
    // por lo que no estamos en espera de asentimiento por él.
    if (!this.tablaAsentimientos.contieneID_TPDU (tpduHACK.getID_TPDUFuente()))
     {
      // Comprobar si el tpdu que me están asintiendo, todavía pertenece
      // a la ventana correspondiente, aunque el propio TPDU no lo tenga.
      if (tpduHACK.getID_TPDUFuente().getID_Socket().equals (this.id_SocketLocal))
       {
         if (this.ventanaEmision==null)
           return;
         // Buscar en la ventana de emisión
         if (!this.ventanaEmision.nSecEntreInicialyFinal (tpduHACK.getID_TPDUFuente().
                                getNumeroSecuencia().tolong()))
           return;
       }
      else // Buscar en la ventana de recepción
          {
            VentanaRecepcion vtaRecp = this.tablaID_SocketsEmisores.
                  getVentanaRecepcion(tpduHACK.getID_TPDUFuente().getID_Socket());
            if (vtaRecp==null)
               return;
            if (!vtaRecp.nSecEntreInicialyFinal (tpduHACK.getID_TPDUFuente().
                                getNumeroSecuencia().tolong()))
              return;
          } // Fin del else

      // Añadir a la tabla de hijos
      this.ponerEnEsperaAsentimiento (tpduHACK.getID_TPDUFuente(),
                                      tpduHACK.getNumeroRafagaFuente(),false);
     }// Fin del if

    //Log.log ("","RECIBIDO UN HACK: " + tpduHACK.getID_TPDUFuente() + " rafaga: " + tpduHACK.getNumeroRafagaFuente());

    // Anotar el HACK recibido en la tabla de los hijos que están a mi cargo.
    this.tablaAsentimientos.addHACK (tpduHACK.getIDGL(),
                                     tpduHACK.getID_TPDUFuente());

    // Limpiar la ventana
    this.limpiarVentana (tpduHACK.getID_TPDUFuente(),false);

    // Actualizar la lista de IDGL no sincronizados
    // El IDGL que ha enviado el HACK está sincronizado para dicho emisor
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
   * Envía tpdu de datos (TPDUDatosNormal, TPDUDatosRtx, TPDUMACK, ...).
   * Delega en la función {@link #enviarTPDUFiable()} si el socket está en modo
   * fiable, o en la función {@link #enviarTPDUNoFiable()} si el modo es no fiable.
   * Comprueba si ha transcurrido el tiempo mínimo entre envíos.
   * @return true ha enviado algún tpdu de datos, y false en caso contrario.
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

   if (bResult) // Se ha enviado algún TPDU
    {
     // Volver a registrar el tiempo mínimo entre envíos.
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
   * Envía los tpdu de datos pendientes. Ejecuta de forma secuencial las distintas
   * funciones de envío. El orden seguido es:
   * <ol>
   *    <il>{@link #enviarTPDUMACK()}</il>
   *    <il>{@link #enviarAsentNeg()}</il>
   *    <il>{@link #enviarAsentimientoPositivo()}</il>
   *    <il>{@link #enviarTPDUHSACK()}</il>
   *    <il>{@link #enviarTPDUDatosSolicitadosRtx()} Envía hasta cinco rtx. antes
   * de seguir enviando otro tipo de TPDUs.</il>
   *    <il>{@link #enviarTPDUDatosNormalFiable()}</il>
   *    <il>{@link #RtxTPDUFinRTT()}</il>
   * </ol>
   * @return true si se ha enviado algún tpdu de datos.
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

   // Se envían hasta 5 rtx. antes de seguir enviando otro tipo de TPDUs.
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
   * Envía los tpdu de datos pendientes.
   * LLama a la función {@link #enviarTPDUDatosNormalNoFiable()}.
   * @return true si se ha enviado algún tpdu de datos.
   */
  private boolean enviarTPDUNoFiable ()
  {
    return this.enviarTPDUDatosNormalNoFiable ();
  }

  //==========================================================================
  /**
   * Comprueba en la ventana de emisión si hay un TPDU datos normal que enviar y
   * lo envía.
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

   // Si hay datos que enviar los pasa a la ventana de emisión.
   pasarBufferUsuarioAVentanaEmisionFiable();

   if (this.ventanaEmision==null)
      return false;

   // Buscar el siguiente TPDUDatos a enviar de la ventana de emisión.
   TPDUDatosNormal tpduDatosNormal = this.ventanaEmision.siguienteTPDUDatosNormal ();

   if (tpduDatosNormal!=null) // 1º if
       {
        try {

        // Primero enviamos el MACK, si es necesario, y luego el TPDU.
        if (tpduDatosNormal.getIR())
            {
              // Ver si ya estaba registrada la ráfaga.
              if (this.tablaCGLocales.getCGLocal (
                                     tpduDatosNormal.getID_SocketEmisor(),
                                     tpduDatosNormal.getNumeroRafaga())==null)
              {
               // Envio el TPDUMACK directamente, sin esperas.
               this.enviarTPDUMACK (tpduDatosNormal.getID_SocketEmisor(),
                                    tpduDatosNormal.getNumeroRafaga(),
                                    tpduDatosNormal.getNumeroSecuencia());

               // Anoto la ráfaga y me establezco como su CG Local.
               this.tablaCGLocales.addRafaga (tpduDatosNormal.getID_SocketEmisor(),
                                              tpduDatosNormal.getNumeroRafaga(),
                                              tpduDatosNormal.getNumeroSecuencia(),
                                              this.id_SocketLocal);

               // Llamo a limpiar ventana.. sino puede ser que tenga que esperar a fin RTT
               limpiarVentana (tpduDatosNormal.getID_SocketEmisor());
              } // Fin del if
            } // Fin del if
          //Destino: Todo los hosts conectados a la dirección multicast.
          Buffer bufAEnviar = tpduDatosNormal.construirTPDUDatosNormal();
          this.socketPTMFImp.getCanalMcast().send (bufAEnviar);

          //ALEX:  depuración, comentar
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



          // Sólo se lanza aquí, puesto que este es el único TPDU enviado que
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
              // Añadir a la tabla de hijos
              this.ponerEnEsperaAsentimiento (tpduDatosNormal.getID_TPDU(),
                                              tpduDatosNormal.getNumeroRafaga(),false);
             } // Fin del if

        this.actualizarListaID_TPDU2MitadRTT (tpduDatosNormal.getID_TPDU());
        return true;
        } // fin del 1º if

   return false;
  }

  //==========================================================================
  /**
   * Actualiza el temporizador de inactivadad, cancelándolo y volviéndolo a lanzar.
   * Sólo es llamada cuando se envía un TPDU de datos normal ya que es el único que
   * tiene que llegar a todos los miembros del grupo multicast.
   */
  private void lanzarTimeOutInactividad ()
  {
   // bTime_out_inactividad es false si está registrada y no ha saltado el evento,
   // o es el primero que se envía.
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
   * Pasa los buffers con los datos del usuario de la cola de emisión a la
   * ventana de emisión encapsulándolos en TPDUs.
   */
  private void pasarBufferUsuarioAVentanaEmisionFiable()
  {
    final String mn = "DatosThread.pasarBufferUsuarioAVentanaEmisionFiable";
    ColaEmision colaEmision = this.socketPTMFImp.getColaEmision();
    Buffer buffer = null;
    RegistroBuffer regBuf = null;

    // Si ya se ha enviado un TPDU con el bit fin activado, entonces no se
    // tienen que enviar más TPDU.
    if (bYaEnviadoFinEmision)
      return;

    try
    {
     if (this.ventanaEmision==null)
     {
          // Si no hay ningún dato que añadir entonces no se crea.
          if((colaEmision == null) || (colaEmision.getTamaño()<=0))
              return;

          // Crear la ventana de emisión
          this.crearVentanaEmision ();

          // Cómo máximo puede ser el tamaño de la ventana de emisión menos 1,
          // para  asegurar que siempre hay un TPDU con el bit ACK activado en
          // la ventana de emisión.
          if (PTMF.ACKS_CADA_X_TPDUS >= this.ventanaEmision.getTamañoMaximoVentana())
               this.iACKCadaXTPDUS = this.ventanaEmision.getTamañoMaximoVentana() - 1;
          else this.iACKCadaXTPDUS = PTMF.ACKS_CADA_X_TPDUS;

          // Poner ACK al primero que se envíe si el modo es FIABLE Retrasado
          if (this.iMODO_FIABILIDAD == PTMF.PTMF_FIABLE_RETRASADO)
                  this.bSetACK = true;

          // El primero siempre deberá tener el bit IR a true
          this.bSetIR = true;
          this.iNRafaga = 1;
      }//Fin if

     // Comprobar que la ventana de Emisión no está llena
     if (this.ventanaEmision==null || this.ventanaEmision.estaLlena())
      {
        // Puede ser que los TPDU de la ventana de emisión esten asentidos.
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

     // COMPROBAR SI SE TIENE QUE CREAR Y AÑADIR A LA VTA. EMISIÓN UN TPDU.
     // EN LOS SIGUIENTES CASOS SE TENDRÁ QUE CREAR UN TPDU, CON DATOS O NULL:
     // 1. - Si hay datos en la cola emisión que enviar.
     // 2. - Si no hay datos en la cola emisión y:
     //       Fin emisión es true o
     //       Expirado el temporizador de inactividad o
     //       Expirado el temporizador del último TPDU que se envío con bit ACK a 1
     if((colaEmision != null) && (!colaEmision.vacia()))
      {
       // Obtener un buffer de la cola de emisión.
       regBuf = colaEmision.remove();
       if(regBuf == null)
          Log.debug(Log.DATOS_THREAD,"DatosThread --> RegistroBuffer NULO¡¡¡¡ ","");

       if(regBuf.getBuffer()== null)
          Log.debug(Log.DATOS_THREAD,"DatosThread -->  Buffer NULO¡¡¡¡ ","");

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
      2. - Superado el número de TPDU sin ACK.
      3. - Si se ha superado un tiempo desde el último TPDU con el ACK activado
           el siguiente deberá tener el bit ACK activado.
      4. - Es el último TPDU de la ráfaga.
      NOTA:
         - Superado el tiempo máximo para que el resto de socket de la comunicación
           no piensen que hemos dejado de emitir, se enviará uno null con el bit
           ACK activado. En este caso buffer es null.
         - Fin de emisión (se comprueba más adelante).
         - Por lo menos se tiene que enviar un TPDU con el bit ACK activo por ventana.
           Esto se asegura ajustando la variable this.iACKCadaXTPDUS a un máximo del
           tamaño de la ventana de emisión  menos 1. Realizado al construir
           la ventana de emisión.
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

      // NUMERACIÓN DE RÁFAGAS Y BIT IR
      // Se activa el bit IR si se supera el número de TPDU por ráfaga. No se
      // cuentan los TPDU's null enviados.
      // IMPORTANTE: NO CAMBIAR EL ORDEN DE ÉSTE IF Y EL SIGUIENTE
      if (this.iTPDUsRafaga >= PTMF.TPDUS_POR_RAFAGA)
       {
        this.bSetIR = true;
        this.iNRafaga ++;
        //Log.log ( "", "El número de ráfaga se ha incrementado:" + this.iNRafaga);
        this.iTPDUsRafaga = 0;
       }

      // Bit Fin de emisión.
      /* Se tiene que crear un tpdu, aunque sea null, para indicar el fin de
         la emisión. Además, no comenzar una siguiente ráfaga porque es el último
         TPDU que vamos a enviar. */
       if (buffer!=null)
         {
          if (this.bFIN_EMISION && colaEmision.getTamaño()<=0)
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

       // -Añadir el TPDU a la ventana de Emisión
       this.ventanaEmision.addTPDUDatosNormal(this.NSecuencia.tolong(),tpdu);

       if (this.bSetACK)
         {
          this.iTPDUsConsecutivosSinACK = 0;

          this.bTime_out_ultimoTPDUSinACK = false;

          // -Cancelar Temporizador de Time Out último tpdu sin ack
          this.socketPTMFImp.getTemporizador().cancelarFuncion(this,this.iEVENTO_TIME_OUT_ULTIMO_TPDU_SIN_ACK);

          // -Lanzar temporizador para Time Out último tpdu sin ack
          this.socketPTMFImp.getTemporizador().registrarFuncion(PTMF.T_MAX_TPDU_SIN_ACK,
                                        this,
                                        this.iEVENTO_TIME_OUT_ULTIMO_TPDU_SIN_ACK);
         }
       else this.iTPDUsConsecutivosSinACK++;


       // -Si se ha enviado con el bit fin conexion activado indicarlo.
       if (this.bSetFIN_CONEXION)
        this.bYaEnviadoFinEmision = true;

       // Preparar para siguiente TPDU
       // 1. -Incrementar Número de Secuencia
       // 2. -Establecer bSetIR a false
       // 3. -Establecer bSetACK a false
      try{
       this.NSecuencia = this.NSecuencia.incrementar(1);
      }catch (ParametroInvalidoExcepcion e)
        {
         Log.log ("","EL NÚMERO DE SECUENCIA NO HA PODIDO INCREMENTARSE");
        }
       this.bSetIR = false;
       this.bSetACK = false;
    }
    catch(ParametroInvalidoExcepcion e)
      { // Volver a añadir los datos a la cola de emisión, pero al principio
        // de la misma.
        if (regBuf != null)
             colaEmision.addFirst(regBuf);
      }
    catch(PTMFExcepcion e)
      { // Volver a añadir los datos a la cola de emisión, pero al principio
        // de la misma.
        if (regBuf != null)
             colaEmision.addFirst(regBuf);
      }
  }

  //==========================================================================
  /**
   * Crea la ventana de emisión.
   * @return true si ha sido creada o false en caso contrario.
   * @throws ParametroInvalidoExcepcion Lanzada si hay un error en la creación
   * del número de secuencia inicial al crear la ventana.
   */
  private boolean crearVentanaEmision() throws ParametroInvalidoExcepcion
   {
    final String mn = "DatosThread.crearVentanaEmision()";

     // this.socketPTMFImp.getTemporizador().tiempoActualEnMseg() devuelve un long y el número de
     // secuencia está compuesto de 32 bits, por lo que hay que recortarlo
     // a 32 bits. El número inicial de la ventana estará comprendido entre
     // 0x00000000 y 0x8FFFFFFF. Por lo que se tienen que generar 2147783647
     // números de secuencia antes de que se finalicen los 32 bits ( en el caso
     // peor).

     // Si ha dejado de emitir, y vuelve a empezar, empieza en el número de
     // secuencia de la última sesión.
     if ((this.NSecuencia==null)||(this.NSecuencia.tolong() <= 0))
       {
        this.N_SECUENCIA_INICIAL = this.socketPTMFImp.getTemporizador().tiempoActualEnMseg();
        this.N_SECUENCIA_INICIAL = (this.N_SECUENCIA_INICIAL << 33) >>> 33;
        this.NSecuencia = new NumeroSecuencia(this.N_SECUENCIA_INICIAL);
       }
     else this.N_SECUENCIA_INICIAL = this.NSecuencia.tolong();

     this.iNRafaga = 1;

     // Crear la ventana de emisión.
     this.ventanaEmision = new VentanaEmision(PTMF.TAMAÑO_VENTANA_EMISION,N_SECUENCIA_INICIAL);

     if (this.iMODO_FIABILIDAD == PTMF.PTMF_FIABLE_RETRASADO)
       {
        ID_TPDU id_tpdu = new ID_TPDU (this.id_SocketLocal,new NumeroSecuencia (this.N_SECUENCIA_INICIAL));
        this.treeMapNuevosEmisoresASincronizar.put (this.id_SocketLocal,id_tpdu);
       }

    this.lNBytesUsuarioEnviados = 0;
    this.socketPTMFImp.getTemporizador().registrarFuncionPeriodica (
                                         this, // TimerHandler
                                         1000, // Periodo en mseg
                                            0, // Número de Periodos (infinitos)
      this.iEVENTO_T_OUT_AJUSTAR_RATIO_USUARIO // arg1
                                            );
     return true;
    }

  //==========================================================================
  /**
   * Pasa los buffers con los datos del usuario de la cola de emisión a la
   * ventana de emisión encapsulándolos en TPDUs.
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
          // Si no hay ningún dato que añadir entonces no se crea.
          if((colaEmision == null) || (colaEmision.getTamaño()<=0))
              return;

          // Crear la ventana de emisión
          this.crearVentanaEmision ();
     }//Fin if

     // Comprobar que la ventana de Emision no está llena
     if (this.ventanaEmision== null || this.ventanaEmision.estaLlena())
         return;

     // Obtener un buffer de la cola de emisión
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

      // -Añadir el TPDU a la ventana de Emisión
      this.ventanaEmision.addTPDUDatosNormal(this.NSecuencia.tolong(),tpdu);
      try{
        this.NSecuencia = this.NSecuencia.incrementar(1);
      }catch (ParametroInvalidoExcepcion e)
        {
         Log.log ("","EL NÚMERO DE SECUENCIA NO HA PODIDO INCREMENTARSE");
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
   * Comprueba en la ventana de emisión si hay un TPDU datos normal que enviar y
   * lo envía. No dá fiabilidad para el tpdu datos enviado.
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

   // Buscar el siguiente TPDUDatos a enviar de la ventana de emisión.
   TPDUDatosNormal tpduDatosNormal = this.ventanaEmision.siguienteTPDUDatosNormal ();

   if (tpduDatosNormal!=null)
       {
        try {
          // Destino: Todos los hosts conectados a la dirección multicast.
          Buffer bufAEnviar = tpduDatosNormal.construirTPDUDatosNormal();
          this.socketPTMFImp.getCanalMcast().send(bufAEnviar);

          if (tpduDatosNormal.getBufferDatos()!=null)
             this.lNBytesUsuarioEnviados += tpduDatosNormal.getBufferDatos().getMaxLength();

          // Conforme se van enviando los datos se eliminan de la ventana de
          // emisión.
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
   * según lo especificado en el protocolo. Límita a quince el número de TPDU que
   * puede retransmitir cada fin de RTT.<br>
   * Rtx. todos los asentimientos negativos por los que estemos esperando.<br>
   * Comprueba si hay asentidos en la tabla de asentimiento (sólo habrá cuando
   * no tenga hijos jerárquicos ni vecinos).<br>
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

      // Sólo añade uno por id_socket, como la lista de id_tpdu en espera de
      // asentimientos está ordenada, nos quedamos con el de mayor NSec. Sólo
      // tenemos que ver que lo tenemos en la ventana de recepción.
      // Nota: TreeMap.put (indice,valor) si índice tenía un valor asociado
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
      //        - Asegurar tráfico para que se puedan detectar pérdidas
      //        - Que los vecinos sepan quien me ha mandado ACK y quién no.
      //        - Los vecinos sepan quien es el CG (pueden pensar que es otro).
      //        - Que los hijos jerárquicos sepan si han enviado un asentimiento
      //        positivo (HSACK o HACK).
      //        - En el caso de Fiable Retrasado que sepan quien está sincronizado.

/*      // Coger de la lista this.treeMapIDs_TPDU2MitadRTT el mayor de este host
      nSecMayor2MitadRTT = (NumeroSecuencia)this.treeMapIDs_TPDU2MitadRTT.get
                                        (id_tpduNext.getID_Socket());

      if((nSecMayor2MitadRTT!=null)
         &&
        (nSecMayor2MitadRTT.compareTo(id_tpduNext.getNumeroSecuencia())>=0))
          { // Rtx. sólo si incluye en sus listas de no asentidos a un socket
            // o idgl nuevo (no sincronizado).
            // Log.debug (Log.TPDU_RTX,mn,"llamar a rtx tpdu si no sincronizado");
            this.rtxTPDU (id_tpduNext,iRTX_SI_NO_SINCRONIZADO);
            continue;
          }
*/
      // Enviar
      // No hay que poner como opción iRTX_SI_NO_SINCRONIZADO porque está
      // implícita en iRTX_SI_CONTIENE_ID_NO_ASENTIDOS. Además si se incluye
      // tendremos que estar en modo FIABLE RETRASADO puesto que si no nunca
      // se rtx. (rtxTPDU comprueba si esa opción está activada y el modo es
      // fiable retrasado)
      if ( !this.rtxTPDU (id_tpduNext,iRTX_SI_CONTIENE_ID_NO_ASENTIDOS) )
         {// Intentar Limpiar ventana. Puede ser que este asentido
          this.limpiarVentana (id_tpduNext.getID_Socket());
         }
      else iCont --; // Se ha enviado un TPDU
   } // Fin del while.

   // Comprobar si hay asentidos en la tabla de asentimiento
   // Sólo habrá cuando no tengamos hijos o vecinos.
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
   // EJECUCIÓN DE ESTA FUNCIÓN. HABRÁ QUE PENSAR ALGO .......................
   this.bFinRTT = false;

   return false;
  }

  //=========================================================================
  /**
   * Busca el id_tpdu en la ventana correspondiente.<br>
   * Si el que me pide no está en la ventana correspondiente y es el
   * número de secuencia inferior posible ({@link NumeroSecuencia#LIMITEINFERIOR}),
   * devuelve el primero de la ventana, si éste tiene el bit IR activo y pertenece
   * a la ráfaga 1.
   * @param id_tpdu id_tpdu a buscar, puede ser null, en cuyo caso devuelve null.
   * @return TPDU datos normal solicitado, si existe, y null en caso contrario.
   */
  private TPDUDatosNormal buscarTPDUEnVentana (ID_TPDU id_tpdu)
  {
   // Si el que me pide no está en la ventana correspondiente y es el
   // número de secuencia inferior posible, dar el primero de la ventana
   // si tiene el bit IR activo y pertenece a la ráfaga 1.
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
   * Envía los tpdu datos rtx. que son solicitados por los socket´s.<br>
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
          // Destino: La dirección Unicast Destino
          this.socketPTMFImp.getCanalUnicast().send (tpduDatosNormal.construirTPDUDatosNormal(),
                                                     dirUnicastDestino);

          //ALEX: depuración, comentar
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

   // Si tenía el bit ACK activado, ya se estaba esperando por el.
   // Si lo que se está retransmitiendo no tiene el bit ACK activado, la
   // lista de ID_TPDU que no han enviado asentimiento estará vacía.
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
   * De los que no es CG local, envía un ACK directamente. Y de los que sí es
   * CG, los anota en {@link #tablaAsentimientos}.<br>
   * Consulta la lista {@link #listaAsentPositPendientesDeEnviar}. <br>
   * NOTA: los HACK se envían en {@link #limpiarVentana (ID_TPDU,boolean)}.
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

       // Si todavía no tiene asignado un CG Local, continuar con otro
       if (id_SocketCGLocal == null)
         {
           // Intentamos registrarnos como CG.
           // Si ya estaba registrado, la función sólo actualiza el número
           // de secuencia inicial de la ráfaga.
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

            /* Destino: Por unicast al CG Local. La información del socket
                        destino está en la variable id_SocketCGLocal
            */
            Address dirUnicastDestino = new Address (id_SocketCGLocal);
            this.socketPTMFImp.getCanalUnicast().send (tpduACK.construirTPDUACK(),
                                                       dirUnicastDestino);

           //     Log.log ("","ENVIADO UN ACK UNICAST: " + id_tpdu );

          //ALEX: depuración, comentar
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
          return true; // Sólo enviamos el primero que lo cumpla.
         }
       else {
             // NO ESTÁN TODOS CONSECUTIVOS
            }
      } // Fin del while
     } // Fin del if
   return false;
  }

  //==========================================================================
  /**
   * Comprueba si hay que enviar asentimientos negativos, en cuyo caso envía
   * uno.<br>
   * Ejecuta el método {@link GestionAsentNeg#enviarNuevoAsentNeg()}.
   * @return true si se ha enviado alguno.
   */
  private boolean enviarAsentNeg ()
  {
   return
        this.gestionAsentNeg.enviarNuevoAsentNeg ();
  }

  //==========================================================================
  /**
   * Comprueba si hay que enviar {@link TPDUMACK} y lo envía. <br>
   * Consulta el vector {@link #vectorMACK}.
   * @return true si se ha enviado alguno.
   */
  private boolean enviarTPDUMACK ()
  {
   final String mn = "DatosThread.enviarTPDUMACK";

   if (this.lTRandomMACK>0)
        return false; // Hay que esperar.

   if (this.vectorMACK.size ()>0) // 1º if
    {
     RegistroVectorMACK reg = (RegistroVectorMACK)this.vectorMACK.remove(0);
     // Lo envio.
     this.enviarTPDUMACK (reg.id_tpdu.getID_Socket (),       // id_socketSrc
                          reg.iNRafaga,                      // Número de ráfaga
                          reg.id_tpdu.getNumeroSecuencia()); // NSecInicial Ráfaga

     // Lo registro en la tabla de CG locales. Es enviado por este socket
     this.tablaCGLocales.addRafaga (reg.id_tpdu.getID_Socket(),
                                    reg.iNRafaga,
                                    reg.id_tpdu.getNumeroSecuencia(),
                                    this.id_SocketLocal);

     // Llamo a limpiar ventana.. sino puede ser que tenga que esperar a fin RTT
     // Puede ser que nadie tenga que mandar asentimiento.
     limpiarVentana (reg.id_tpdu.getID_Socket());

     if (this.vectorMACK.size ()>0) // Quedan más MACK por enviar.
      {
       // Si no hay ningún vecino, no hay que esperar ningún tiempo.
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
     } // Fin 1º if

   return false;
  }

  //==========================================================================
  /**
   * Forma un TPDUMACK con la información pasada en los argumentos
   * y lo envía.<br>
   * <b> SOLO ENVIA EL MACK SIN MAS.</b>
   * @param id_SocketSrc id_socket fuente de la ráfaga.
   * @param iNumeroRafagaParam ráfaga.
   * @param numSecInicialRafaga número de secuencia inicial de la ráfaga.
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
                Si el número de vecinos es cero, no se envía a la red.
    */
    Buffer bufAEnviar = tpduMACK.construirTPDUMACK();
    if (this.getCGLThread().numeroVecinos()>0)
      {
       this.socketPTMFImp.getCanalMcast().send (bufAEnviar,(byte)this.getCGLThread().getTTLSocketsGL());

       //  Log.log ("","ENVIADO UN MACK");

         //ALEX: depuración, comentar
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
   * Envía los TPDU HSACK que estén pendientes. Puede enviar más de uno.
   * @param true si envío alguno.
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
           id_socketCGLocal.equals (this.id_SocketLocal)) // 1º if
       { // Somos su CG Local.
         // Puede ser que este id_tpdu no esté en espera de asentimiento (por ser
         // antiguo o cambios de CG Local), pero el padre-s me esté pidiendo
         // que se lo asienta. Si lo hemos recibido le enviamos un HSACK.
        if (this.tablaAsentimientos.contieneID_TPDU (reg.id_tpdu)) // 2º if
          {
           // Comprobar si ya habíamos enviado HSACK.
           if (this.tablaAsentimientos.enviadoHSACK (reg.id_tpdu)) // 3º if
           {
             // Ver si lo han pedido por rtx. en cuyo caso hay que re-enviarlo
             // La condición es true puesto que anteriormente ya se había enviado
             // por lo que seguro que cumple las condiciones.
             if (reg.bPedidoRtx)
                  bCondicion = true;
             else {
                   iteradorRegistrosHSACK.remove();
                   continue;
                  } // Fin del if
           } // Fin del 3º if
           else
              // Comprobar si algún vecino lo ha asentido con ACK.
              bCondicion = this.tablaAsentimientos.algunACKID_Socket (reg.id_tpdu);
          } // Fin del 2º if

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
             // que el padre jerárquico descarte a todo el grupo local.
             VentanaRecepcion vtaRecp =
               this.tablaID_SocketsEmisores.getVentanaRecepcion (reg.id_tpdu.getID_Socket());

             // EsConsecutivo devuelve true si es menor que el inicial de la ventana
             // o la ventana contiene todos los tpdu hasta el indicado.
             if (vtaRecp!=null)
                bCondicion = vtaRecp.esConsecutivo (reg.id_tpdu.getNumeroSecuencia().tolong());
           } // Fin del 1º if

     if (bCondicion)
      {// Enviar HSACK
       iteradorRegistrosHSACK.remove();
       try{
        // Coger de la lista de padres y mandar el HSACK
        // Obtenemos los padres para el IDGL dado.
        IDGL idglFuente = this.getIDGL (reg.id_tpdu.getID_Socket());

        if (idglFuente==null)
            continue;

        // Deberá existir la tarea CGL Thread.
        TreeMap treemapPadres = this.socketPTMFImp.getCGLThread().getCGPadres(idglFuente);
        if( treemapPadres != null)
         {
          // Obtenemos el TTL máximo de esos padres
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
          /* Destino: A todos los padres jerárquicos para el id_tpdu que
                      estamos asintiendo.
             */
          Buffer bufAEnviar = tpduHSACK.construirTPDUHSACK();
          this.socketPTMFImp.getCanalMcast().send(bufAEnviar,(byte)TTLMaxPadres);

          //ALEX: depuración, comentar.
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
   * Forma con los parámetros un TPDUHACK y lo envía.
   * @param id_tpduFuente ID_TPDU que se está asintiendo.
   * @param iNumeroRafagaFuente número de ráfaga a la que pertenece id_tpduFuente.
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
     // Obtenemos el TTL máximo de entre todos los padres jerárquicos.
     TreeMap treemapPadres = this.socketPTMFImp.getCGLThread().getCGPadres(idglFuente);
     if( treemapPadres != null)
       {
        // Obtenemos el TTL máximo de esos padres
        int iTTLMaxPadres = this.socketPTMFImp.getCGLThread().getTTLGLMaximo(treemapPadres);

        //Enviar el TPDU con el TTL correspondiente.
        /* Destino: A todos los padres jerárquicos para el id_tpdu que
                    estamos asintiendo.
        */
        Buffer bufAEnviar = tpduHACK.construirTPDUHACK();
        this.socketPTMFImp.getCanalMcast().send(bufAEnviar,(byte)iTTLMaxPadres);

        //ALEX: depuración, comentar.
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
    // las listas de los que no me lo han asentido estarán vacias, no eliminando
    // a nadie, salvo a la fuente del tpdu.

    ID_TPDU id_tpdu = null;
    boolean bCondicion = false;
    while (iteradorID_TPDU.hasNext())
     {
      id_tpdu = (ID_TPDU)iteradorID_TPDU.next();
      Log.debug(Log.TEMPORIZADOR,mn,"ID_TPDU que ha vencido de oportunidades: " + id_tpdu);
      iteradorID_TPDU.remove();

      // Comprobar si lo tenemos: condicion indicará si lo tenemos (true)
      // o si no (false)

      // Comprobar si somos la fuente ==> Lo tenemos en la vta. emisión.
      bCondicion = id_tpdu.getID_Socket().equals (this.id_SocketLocal);

      if (bCondicion && this.ventanaEmision == null)
      {
        // No somos emisores.
        continue;
      }

      if (!bCondicion)// Comprobar si lo hemos recibido en la vta. de recepción.
      {
         VentanaRecepcion vtaRecp =
           this.tablaID_SocketsEmisores.getVentanaRecepcion (id_tpdu.getID_Socket());

         if (vtaRecp!=null)
           // Comprobar si está consecutivo. ?¿?¿?¿?
           bCondicion = vtaRecp.esConsecutivo (id_tpdu.getNumeroSecuencia().tolong());
         else
           continue; // No existe el emisor
      } // Fin del if


      if (!bCondicion)
       // Eliminar el id_socket fuente del tpdu.
       { // No es necesario limpiar la ventana puesto que se elimina la fuente
         // y con ella la ventana de recepción asociada, mediante el listener
         // de CGLThread.
         Log.debug(Log.DATOS_THREAD,mn,"Eliminar ID_SOCKET: " + id_tpdu.getID_Socket()+" Motivo:?¿?¿?¿");
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

         // Limpiar la ventana. Sin tener que verificar si están asentido
         // Convertir en HACK los HSACK recibidos para id_tpdu
         this.tablaAsentimientos.convertirHSACKaHACK (id_tpdu);
         this.limpiarVentana (id_tpdu,true);

         ID_Socket id_socket = null;
         // Eliminar los sockets de los que no he recibido asentimiento.
         while (iteradorID_Socket.hasNext())
          {
           id_socket = (ID_Socket)iteradorID_Socket.next();
           Log.debug(Log.DATOS_THREAD,mn,"Eliminar ID_SOCKET: " + id_socket+" Motivo: no he recibido ACK para el ID_TPDU "+id_tpdu+" en el número máximo de intentos (RTT*"+PTMF.OPORTUNIDADES_RTT+")");
           this.socketPTMFImp.getCGLThread().removeID_SOCKET(id_socket);
           } // Fin del while

         IDGL idgl = null;
         while (iteradorIDGL.hasNext())
            {
             idgl = (IDGL)iteradorIDGL.next();
             //Log.log (mn,"2.2- VOY A LLAMAR A REMOVE IDGL: " + idgl);
             Log.debug(Log.DATOS_THREAD,mn,"Eliminar IDDL: " + idgl+" Motivo: no he recibido HACK para el ID_TPDU "+id_tpdu+" en el número máximo de intentos (RTT*"+PTMF.OPORTUNIDADES_RTT+")");
             this.socketPTMFImp.getCGLThread().removeIDGL (idgl);
            } // Fin del while
        } // Fin del if
    } // Fin del while

    this.mutex.unlock();
    // Eliminar los sockets que no han enviado los id_tpdu solicitados.
    // Nunca será uno de la ventana de emisión.
    // NOTA: LA CULPA HA PODIDO SER DEL IDGL PADRE QUE SE HA CAIDO, PERO NO,
    // PORQUE EN ÚLTIMA INSTANCIA SON PEDIDOS POR UNICAST A LA FUENTE.
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
   * Función Callback que se registra en el temporizador.
   * Actualiza variables y listas de acuerdo con el evento indicado en lEvento.<br>
   * <b>Este método es ejecutado por el thread del {@link Temporizador}.</b>
   * @param lEvento Indica el tipo de evento.
   * @param o Object asociado, será tenido en cuenta dependiendo de lEvento.
   */
  public void TimerCallback(long lEvento, Object o)
  {
    // MUY IMPORTANTE:
    //
    //  ESTA FUNCIÓN ES EJECUTADA POR EL THREAD DEL TEMPORIZADOR
    //  TODO EL CÓDGIGO QUE SE EJECUTE AQUI DEBE DE SER MÍNIMO!!!
    //
    //  SE RECOMIENDA SOLO ESTABLECER FLAGS Y ALGUNAS VARIABLES,
    //  EL RESTO DEL CÓDIGO DEBERÁ DE SER PROCESADO EN CADA VUELTA
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
   * Registra para enviar un TPDUMACK. Asegura los tiempos aleatorios. No envía
   * el MACK.
   * @param id_TPDU contiene el id_socket fuente y el número de secuencia inicial
   * del MACK que se registra.
   * @param iNumeroRafagaParam número de ráfaga del MACK que se registra.
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

   // Comprobar si ya estaba en el vector un MACK para la misma ráfaga
   // y en caso afirmativo actualizar el número de secuencia inicial.
   for (int i=0;i<this.vectorMACK.size();i++)
   {
    RegistroVectorMACK regNext = (RegistroVectorMACK)this.vectorMACK.get (i);

    if (regNext.id_tpdu.getID_Socket().equals (id_TPDU.getID_Socket())
        &&
        regNext.iNRafaga==iNumeroRafagaParam)
      {
       // Comprobar el número de secuencia y quedar el menor.
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
        { // Se está esperando para mandar un MACK.
        }
   else { // Esperar un tiempo aleatorio para mandar MACK.
          // Si no hay ningún vecino, no hay que esperar ningún tiempo.
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
   * Comprueba si hay un MACK pendiente de enviar para id_socket,ráfaga
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
   * Analiza el id_TPDU para saber si hay algún número de secuencia
   * que no se haya recibido. Comprueba si es continuación del último TPDUDatos
   * que se recibió de la fuente del id_TPDU, si lo es, no ha habido pérdidas, y sino,
   * se han perdido los TPDUDatos comprendidos entre el último recibido y este.
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

    // Ver los que faltan en la ventana de recepción hasta este.
    // Es suficiente con comprobar desde el último recibido, y no de los consecutivos,
    // puesto que los que no se hayan recibido anteriores al último ya se habrán
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
   * que haberse llamado a la función {@link #stopThread(boolean)} previamente.
   * Según el modo de finalización indicado en la llamada a stopThread devolverá:
   *  <li>No Estable : Devuelve true.</li>
   *  <li>Estable    : Devuelve true, si la cola de emisión y la ventana de
   * emisión están vacias, y además no se está esperando recibir asentimiento
   * por ningún ID_TPDU.</li>
   * </ul>
   * @return true si el thread puede finalizar.
   */
  private boolean finComunicacion ()
  {
   final String mn = "DatosThread.finComunicacion()";

   if (this.bABANDONAR_COMUNICACION!=true)
        return false;

    // Esperar hasta que la ventana de emisión, la cola de emisión y la tabla de
    // asentimientos estén vacías.
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

        // ¿¿??
        if ((this.ventanaEmision==null)
             &&
            (this.socketPTMFImp.getColaEmision().getTamaño()==0))
            return true;

        if ((this.socketPTMFImp.getColaEmision().getTamaño()==0)
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
   * finalizará cuando se lo indique la función {@link #finComunicacion()}.<br>
   * EL CÓDIGO DE ESTE MÉTODO ES EJECUTADO POR EL HILO DEL USUARIO.
   * @param bOrdenado si true la finalización del thread será estable.
   */
  public void stopThread(boolean bOrdenado)
  {
    final String mn = "DatosThread.stopThread ()";

    // Indicar que queremos dejar la comunicación.
    this.bABANDONAR_COMUNICACION = true;

    this.bORDENADO = bOrdenado;

    this.finEmision();
  }

  //==========================================================================
  /**
   * Indica que este socket no va a emitir más datos.
   */
  void finEmision ()
  {
   this.bFIN_EMISION = true;
  }

  //==========================================================================
  /**
   * Actualiza la lista con el nuevo identificador, siempre que no exista
   * previamente o sea superior al que exista con la misma dirección, si estamos
   * en la 2ª mitad de RTT.
   * @param id_TPDUParam identificador del TPDU que se quiere añadir, si se
   * está en la 2ª mitad de RTT
   */
  private void actualizarListaID_TPDU2MitadRTT (ID_TPDU id_TPDUParam)
  {
   final String mn = "DatosThread.actualizarListaID_TPDU2MitadRTT (id_Tpdu)";

   // Realmente sólo es necesario para aquellos que sea CG Local, de momento,
   // es más óptimo no buscar en la tabla de Hijos, es muy dinámico.
   // Comprobar que estamos en la segunda mitad de RTT
   //Log.debug (Log.DATOS_THREAD,mn,"Porcentaje: " + this.socketPTMFImp.getTemporizador().getPorcentajeRTTActual());
   // CUIDADO: ESTO ESTÁ ASÍ PORQUE FINRTT PUEDE SER CAMBIADO POR EL HILO
   // DEL TEMPORIZADOR Y SIN EMBARGO NO SE HA LLAMADO A LA FUNCIÓN this.RtxTPDUFinRTT
   // Si bFinRTT es true se ha cumplido, al menos una vez, RTT y no se ha ejecutado
   // la función RTXTPDUFINRTT, por lo que se actualiza igual que si estuviésemos
   // en la 2ª mitad de rtt.
   if ( !( (this.bFinRTT)
           ||
           (this.socketPTMFImp.getTemporizador().getPorcentajeRTTActual () > 50) ) )
      return;

   // Estamos en la 2ª mitad RTT

   // Comprobar si existe un id_TPDU en la lista con la misma dirección que el
   // pasado  por argumento.
   NumeroSecuencia nSecAnterior=(NumeroSecuencia)this.treeMapIDs_TPDU2MitadRTT.get
                                                 (id_TPDUParam.getID_Socket());
   if (nSecAnterior!=null) // Veo si es mayor y lo reemplazo.
    {
      // Si el anterior registrado es mayor o igual a este, no hacer nada.
      if (nSecAnterior.compareTo (id_TPDUParam.getNumeroSecuencia ())>= 0)
          return;

      // Reemplazar el anterior nSec asociado a la dirección del ID_TPDU
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
   * En la lista sólo se almacena el mayor número de secuencia para un id_socket
   * determinado (LOS ASENT. POSIT. SON ACUMULATIVOS PARA LA MISMA RÁFAGA).
   * @param id_tpduFuente id_tpdu que se tiene que asentir positivamente.
   * @param iNumeroRafaga número de la ráfaga a la que pertenece id_tpduFuente.
   */
  private void actualizarAsentPositPendientesDeEnviar (ID_TPDU id_tpduFuente,
                                                int iNumeroRafaga)
  {
   if (id_tpduFuente==null)
     return;

   // Almacenar la información sobre el ACK que queda pendiente de envio,
   // Son ACUMULATIVOS para la misma ráfaga.

   // Buscar si ya había alguno en la lista para la misma ráfaga que este.
   ID_Socket id_socket  = id_tpduFuente.getID_Socket();
   NumeroSecuencia nSec = id_tpduFuente.getNumeroSecuencia();

   // Iterador con los id_tpdu que contienen a id_socket y el número de ráfaga.
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
     // Dentro de la misma ráfaga puesto que pueden tener CG Locales diferentes
     // dos ráfagas distintas.
     if (iNumeroRafagaNext==iNumeroRafaga)
      // Dejar sólo el que tenga mayor número de secuencia (ACK ACUMULATIVOS)
      {
       if (nSec.compareTo(nSecNext)<=0)
         return;
       else // Quitar el menor
          iteradorID_TPDU.remove ();

       // Añadir el mayor
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
   * cuenta que sólo se almacena el mayor número de secuencia para un id_socket
   * determinado (los HSACK son acumulativos).
   * @param id_tpdu id_tpdu que se tiene que semi-asentir (HSACK)
   * @param iNumeroRafaga número de la ráfaga a la que pertenece id_tpdu.
   * @param bPedidoRtx si true indica que se recibió un TPDU de datos rtx. por
   * un padre jerárquico en el que figuramos en su lista de IDGL pendientes de
   * asentir.
   */
  private void actualizarHSACKPendientesDeEnviar (ID_TPDU id_tpdu,int iNumeroRafaga,
                                                  boolean bPedidoRtx)
  {
   final String mn = "DatosThread.actualizarHSACKPendientesDeEnviar (id_tpdu,nRaf,bPedidoRtx)";
   // Almacenar la información sobre el HSACK que queda pendiente de envio,
   if (id_tpdu==null)
     return;

   // Almacenar la información sobre el HSACK que queda pendiente de envio,
   // Son ACUMULATIVOS para la misma ráfaga.

   // Buscar si ya había alguno en la lista para el mismo emisor fuente que este.
   ID_Socket id_socket  = id_tpdu.getID_Socket();
   NumeroSecuencia nSec = id_tpdu.getNumeroSecuencia();

   // Iterador con los id_tpdu que contienen a id_socket y el número de ráfaga.
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
    // a la misma ráfaga o no para hacer supresión. Es indipendiente quién sea
    // el CG local porque sea el que sea deberá recogerla y actualizar sus lista
    // de espera de asentimiento.

    // Dejar sólo el que tenga mayor número de secuencia (HSACK ACUMULATIVOS)
    // El valor de pedidoRtx. del que se quede en la lista deberá ser true
    // si lo es el candidato a entrar (pasado en los parámetros) o el que
    // estaba en la lista. Con ello se evita que un padre jerárquico crea
    // que hemos abandonado la comunicación, esto se puede ver en enviarHSACK().
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

    // Añadir el mayor
    this.listaHSACKPendientesDeEnviar.put (id_tpdu,reg);

    return;
   }// Fin del while

   // Crear un registro.
   RegistroHSACK reg = new RegistroHSACK ();
   reg.id_tpdu   = id_tpdu;
   reg.iNRafaga  = iNumeroRafaga;
   reg.bPedidoRtx = bPedidoRtx;

   // Añade el indicado en el argumento, por ser el de mayor nSec, o por no haber
   // en la lista otro para el mismo [id_socket,número Ráfaga]
   this.listaHSACKPendientesDeEnviar.put (id_tpdu,reg);
  }

  //=========================================================================
  /**
   * Comprueba si se está esperando mandar un asentimiento positivo para id_tpdu.yield
   * @param id_tpdu id_tpdu a comprobar.
   * @return true si se está esperando enviar un asentimiento positivo para id_tpdu.
   */
  private boolean esperandoMandarAsentPosit (ID_TPDU id_tpdu)
  {
   if (id_tpdu == null)
      return false;

   return this.listaAsentPositPendientesDeEnviar.contiene (id_tpdu);
  }

  //==========================================================================
  /**
   * Actualiza la información contenida en la tabla de CG locales para la ráfaga
   * indicada, según especificado en el protocolo PTMF.
   * @param id_socketFuente id_socket fuente de la ráfaga
   * @param iNumeroRafaga número de la ráfaga
   * @param nSecInicRafaga número de secuencia inicial de la ráfaga
   * @param id_socketCGLocal id_socket CG local para la ráfaga.
   * @return id_socket CG de la ráfaga.
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
   else // Ver quien se queda como CG Local // 1º else
    {
     /* Sólo tiene que comprobarse estas condiciones en el caso de que nosotros
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
      { // Cambiar el CGLocal de la ráfaga.

        // ESTA PARRAFADA SÓLO SIRVE PARA EL CASO EN EL QUE SE OPTE POR INCLUIR
        // TAMBIÉN LA PRIMERA CONDICIÓN:
        // Somos el CG Anterior y se tiene que cambiar el CG a otro socket.
        // en ese caso no hacer nada. Pero si somos el CG anterior y además
        // nos mantenemos, tenemos que re-enviar un MACK para advertir a los
        // demás que seguimos siendo CG.
        this.tablaCGLocales.actualizaID_SocketCGLocal
                                            ( id_socketFuente,
                                              iNumeroRafaga,
                                              id_socketCGLocal );
       id_socketResult = id_socketCGLocal;

       // LLegado a este punto es por que el id_socket anterior es este socket.
       if (bSoyCGAnterior)
        {
         // Si dejo de ser CG Local de ciertas ráfagas, tengo que eliminar
         // de la lista de espera por Asentimientos todos los id_Tpdu referentes
         // a dicha ráfaga, puesto que será otro su CG Local.
         // IMPORTANTE: TODOS LOS QUE QUITAMOS DE AQUÍ DEBERÍAN PASAR A LA
         // LISTA DE ACK PENDIENTES DE ENVIAR Y CANCELAR EL TEMPORIZADOR QUE
         // TENÍA ASOCIADO.
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
     else // Ver si este host era el CGLocal de la ráfaga, en cuyo caso
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
   } //Fin del 1º else

   return id_socketResult;
}

  //=============================================================================
  /**
   * Actualiza el ratio de envío de datos de usuario, el cual especifica la
   * máxima velocidad de envío de nuevos datos de usuario al grupo multicast.
   * @param bytes_x_seg bytes por segundo, tiene que ser mayor que cero.
   * @return el valor al que se ha actualizado el ratio de datos de usuario en bytes
   * por segundo
   */
  public long setRatioUsuario (long lBytes_x_seg)
   {
    // PONER UN VALOR POR DEFECTO del cual no puede ser menor
    if (lBytes_x_seg > 0)
       this.lRatioUsuario_Bytes_x_seg = lBytes_x_seg;

    // Puede hacer que suba momentáneamente mucho el ratio, si se llama
    // repetidamente esta función.
    this.lT_Out_Ajustar_Ratio_Usuario = 0;
    this.lNBytesUsuarioEnviados = 0;

    return this.lRatioUsuario_Bytes_x_seg;
   }

  //=============================================================================
  /**
   * Comprueba si el id_tpdu se puede eliminar de la ventana  correspondiente,
   * siempre que la fiabilidad esté asegurada. Al eliminar el id_tpduParam, también
   * se eliminan todos los anteriores a él.
   * Si es CG de id_tpduParam y lo ha podido eliminar de la ventana, entonces
   * intenta eliminar el siguietne con el bit ACK activado de la misma ventana, y
   * así sucesivamente.
   * @param id_tpduParam id_tpdu a eliminar de la ventana.
   * @param bFinOportunidades lo elimina de la ventana esté o no asentido por todos.
   * @return true si lo eliminó.
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
      { // Buscar en la ventana de emisión
        ventana = this.ventanaEmision;
        bEsVtaRecepcion = false;
      }
    else // Buscar en la ventana de recepción.
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
     // sólo en el caso de que no se haya decicido abandonar la comunicación.
     if (!this.bABANDONAR_COMUNICACION
         &&
         bEsVtaRecepcion)
       {
        // No es necesario comprobar en vta emisión
        if (!ventana.esConsecutivo(id_tpduAnalizar.getNumeroSecuencia().tolong()))
         {
          Log.log ("",""+id_tpduAnalizar+" NO CONSECUTIVO");
          return false; // Este socket aún no los tiene todos hasta id_tpduAnalizar.
         }
        if (!this.tablaID_SocketsEmisores.entregadoAlUsuario (id_tpduAnalizar))
        {
         if (bFinOportunidades)
           { /*
               Cierro la recepción de más datos. El usuario no es capaz de
               leerlos o no quiere leerlos.
               CUIDADO SOCKETPTMFIMP ES USADO POR MÁS HILOS
               SEGURAMENTE EN ESTE PUNTO OTROS ME HABRÁN DADO POR MUERTO
               PUEDO HACER ESTO EN EL RUN CON UN TEMPORIZADOR QUE COMPRUEBA SI
               LLEVO MUCHO TIEMPO SIN PODER METER DATOS EN LA COLA, EN ESE CASO
               PUEDO YO CERRAR LA RECEPCIÓN CUANDO NO ES IMPRESCINDIBLE PARA
               CONTINUAR FUNCIONANDO SIN QUE OTROS SE ALERTEN DE MÍ PROBLEMA
               RETIRO EL PÁRRAFO ANTERIOR (POR LO MENOS UNA PARTE) PUESTO QUE
               COMO YO ENVIÉ UN HSACK NO ME DARÁN POR MUERTO
              */
              this.socketPTMFImp.desactivarRecepcion ();

              Log.log ("SE HA CERRADO LA RECEPCION","");

              // Mandar un aviso
              this.socketPTMFImp.sendPTMFEventError ("Se ha cerrado la recepción.");

              // LLamar a entrega datos usuario: Como la recepción está cerrada
              // los datos serán eliminados.
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
               return false; // Aún no han sido entregado al usuario.
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
     // Si todavía no tiene CG Local asignado ==> no continuar.
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

           // Yo sí tengo todos los tpdu hasta id_tpdu Analizar
           // Este socket es su CG, pero no es la fuente, por lo que no ha sido
           // enviado por un miembro del grupo local.
           if (!id_tpduAnalizar.getID_Socket().equals (this.id_SocketLocal))
               // Enviar HACK a los padres jerárquicos.
               this.enviarTPDUHACK (
                   id_tpduAnalizar,
                   iNumRafaga,
                   this.tablaID_SocketsEmisores.getIDGLFuente (id_tpduAnalizar.getID_Socket()));

           // Eliminar de la ventana de recepción
           ventana.removeTPDUDatosNormal (id_tpduAnalizar.getNumeroSecuencia().tolong());

           // Ya no estamos esperando ACK por él.
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

           // Si es el último de la ráfaga, eliminar de la tabla CG Locales
           this.tablaCGLocales.removeRafagaNSecFinalMenorIgual (
                                          id_tpduAnalizar.getID_Socket(),
                                          id_tpduAnalizar.getNumeroSecuencia());
           // Si el que se ha borrado tenía el bit fin de emisión activo:
           //     - Comprobar que la ventana de emisión está vacía.
           //     - Eliminar el id_socket de los emisores.
           //     - Enviar información al usuario.
           //     - Eliminar el id_socket de la cola de recepción
           if (tpduDatosNormal.getFIN_CONEXION())
              {
                 this.eliminarID_SocketFuente (id_tpduAnalizar.getID_Socket(),"Fin de emisión");
                 Log.debug(Log.DATOS_THREAD,mn,"RECIBIDO EL BIT FIN CONEXION");
                 return true;
              }

           // Condición del bucle while
           // Compruebo siguiente TPDU con el ACK activado.
           // Tanto si es ventana de emisión como si es ventana de recepción
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
              // Comprobar si se está esperando para mandar asentimiento
              // Por si hay un problema y tenemos que ser nosotros el CG Local
              // y quizás por algo más que ahora no recuerdo.
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
              // Eliminar de Vta. Recepción.
              ventana.removeTPDUDatosNormal (id_tpduAnalizar.getNumeroSecuencia().tolong());

              // Si es el último de la ráfaga, eliminar de la tabla CG Locales
              this.tablaCGLocales.removeRafagaNSecFinalMenorIgual (
                                            id_tpduAnalizar.getID_Socket(),
                                            id_tpduAnalizar.getNumeroSecuencia());

              // Si el que se ha borrado tenía el bit fin de emisión activo:
              //     - Comprobar que la ventana de emisión está vacía.
              //     - Eliminar el id_socket de los emisores.
              //     - Enviar información al usuario.
              //     - Eliminar el id_socket de la cola de recepción
              if (tpduDatosNormal.getFIN_CONEXION())
                  {
                    this.eliminarID_SocketFuente (id_tpduAnalizar.getID_Socket(),"Fin de emisión");
                    Log.debug(Log.DATOS_THREAD,mn,"RECIBIDO EL BIT FIN CONEXION");
                  }
              return true;
            } // Fin del else;
  } // Fin del while

}

//=============================================================================
/**
 * Por cada TPDU Datos normal con el bit ACK activado contenido en la ventana
 * asociada a id_socket llama a la función {@link #limpiarVentana(ID_TPDU,boolean)}.
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
  {// Buscar en la ventana de emisión
     ventana = this.ventanaEmision;
  }
  else // Buscar en la ventana de recepción.
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
  // Con esto se asegura que si el tpdu que se ha recibido pertenece a una ráfaga,
  // de la que no somos CG, posterior a una de la que sí lo somos, no se eliminarán
  // de la ventana correspondientes puesto que para ello deberán estar asentidos
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
 * LLama a la función {@link #limpiarVentana(ID_TPDU,boolean)} con el primer TPDU de datos
 * con el bit ACK activado contenido en la ventana asociada a id_socket.
 * @param id_socket id_socket fuente.
 */
 private void limpiarVentanaHastaPrimerTPDUConACK (ID_Socket id_socket)
 {
  final String mn = "DatosThread.limpiarVentanaHastaPrimerTPDUConACK (id_socket)";

  if (id_socket==null)
     return;

  Ventana ventana = null;

  if (id_socket.equals (this.id_SocketLocal))// Buscar en la ventana de emesión
     ventana = this.ventanaEmision;
  else // Buscar en la ventana de recepción.
     {
      ventana = this.tablaID_SocketsEmisores.getVentanaRecepcion(id_socket);
      this.tablaID_SocketsEmisores.entregaDatosUsuario (id_socket,
                                        this.socketPTMFImp.getColaRecepcion());
     }

  if (ventana==null)
     return;

  // Sólo analiza el primero de la ventana con el bit ACK activado.
  TPDUDatosNormal tpduDatosNormal = ventana.getPrimerTPDUConACKActivado();

  if (tpduDatosNormal != null)
    limpiarVentana (tpduDatosNormal.getID_TPDU(),true);
 }

//=============================================================================
/**
 * Entrega datos al usuario en modo no fiable y no fiable ordenado.
 * @param id_socket id_socket fuente de los datos.
 * @param nSec número de secuencia de los datos a entregar.
 * @param bufferDatos datos a entregar.
 * @param bSetFinTransmision true si el TPDU donde venían los datos tenía el bit
 * Fin Transmisión activado.
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
                                   // no entregamos más datos.
    {
     if (this.iMODO_FIABILIDAD == PTMF.PTMF_NO_FIABLE_ORDENADO)
      {
        if (this.treeMapNSecUltimoEntregado.containsKey(id_socket))
          { // Comprobar el número de secuencia almacenado
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
     // Si ha habido problemas para añadir, los datos se habrán perdido.
     this.socketPTMFImp.getColaRecepcion().add(id_socket,bufferDatos,bSetFinTransmision);
    }
 }

 //============================================================================
 /**
  * Devuelve un treemap con los id_socket fuentes actuales.
  * @return si el modo es FIABLE o FIABLE_RETRASADO: devuelve TreeMap con los
  * id_Socket fuentes actuales o vacío si no hay ninguno. <br>
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
  * @param id_socket id_socket que solicita la retransmisión
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
  * Añadir un nuevo ID_TPDU a la tabla de asentimientos, si estamos en modo
  * fiable retrasado, rtx. el tpdu dependiendo de lo que figure en los treemap
  * de no sincronizado.
  * @param id_tpdu id_tpdu a añadir a la tabla de asentimientos
  * @param iNumRafaga número de ráfaga a la que pertenece id_tpdu
  * @param bRtxSoloHijosJerarquicos si es true y el modo es FIABLE_RETRASADO
  * indica que se tiene que RTX sólo a los hijos jerárquicos.
  * @return true si ha sido añadido.
  */
  private boolean ponerEnEsperaAsentimiento (ID_TPDU id_tpdu,int iNumRafaga,
                                               boolean bRtxSoloHijosJerarquicos)
  {
   if (this.tablaAsentimientos.addID_TPDUEnEsperaAsentimiento (id_tpdu,iNumRafaga))
   {
       // Registro función para que me avise cuando se acaban las
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
                iRTX_SI_NO_SINCRONIZADO_Y_ES_1ª_RTX
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
  *   <li>{@link #iRTX_SI_NO_SINCRONIZADO_Y_ES_1ª_RTX}</li>
  *   <li>{@link #iRTX_SI_NO_SINCRONIZADO}</li>
  *   <li>{@link #iRTX_SI_CONTIENE_ID_NO_ASENTIDOS}</li>
  * </ul>
  * @return true si enviado un TPDU retransmitido.
  * si rtxSiContieneNoSincronizado es true indica que sólo se rtx. si en sus
  * listas de no asentidos figura un socket o idgl que aún no esté sincronizado
  * para dicho emisor.
  * Si rtxSolohijosjerarqu es true entonces no se añaden los id_sockets que no
  * hayan enviado asentimiento.
  * Si el
  */
 private boolean rtxTPDU (ID_TPDU id_tpdu,int iOpciones)
 {
    final String mn = "DatosThread.rtxTPDU";

    if (id_tpdu==null)
     return false;

    // Si indico solo a hijos y sólo a vecinos se entiende que se rtx a todos.
    boolean bRtxSoloAHijos   = (iOpciones & iRTX_SOLO_A_HIJOS) == iRTX_SOLO_A_HIJOS;
    boolean bRtxSoloAVecinos = (iOpciones & iRTX_SOLO_A_VECINOS) == iRTX_SOLO_A_VECINOS;
    if (bRtxSoloAHijos && bRtxSoloAVecinos)
      {
       bRtxSoloAHijos   = false;
       bRtxSoloAVecinos = false;
      }
    boolean bRtxSiNoSincronizadoyEs1ªRtx =
                (iOpciones & iRTX_SI_NO_SINCRONIZADO_Y_ES_1ª_RTX) == iRTX_SI_NO_SINCRONIZADO_Y_ES_1ª_RTX;

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

    // No devuelven null. Si no hay devuelve un treeMap vacío

    // Puesto para optimizar: si se quita funciana igual, pero se ahorra
    // muchos pasos
    if ( (bRtxSiNoSincronizado || bRtxSiNoSincronizadoyEs1ªRtx)
          &&
         (this.iMODO_FIABILIDAD != PTMF.PTMF_FIABLE_RETRASADO) )
         return false;

    // Treemap con los sockets que figurarán en la lista de no asentidos
    // del tpdu que se va a rtx.
    if (!bRtxSoloAHijos)
         treeMapID_SocketsNoEnviadoACK =
                    this.tablaAsentimientos.getTreeMapID_SocketsNoEnviadoACK(id_tpdu);
    else treeMapID_SocketsNoEnviadoACK = new TreeMap ();

    // Treemap con los sockets que figurarán en la lista de no asentidos
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

          // Comprobar si este socket está entre los que se van a mandar en la
          // lista de no asentidos del tpdu rtx
          if (treeMapID_SocketsNoEnviadoACK.containsKey (id_socketNuevoNext))
           {
            treeMapValue = (TreeMap)this.treeMapID_SocketNoSincronizado.get(id_socketNuevoNext);
            if (!treeMapValue.containsKey (id_tpdu.getID_Socket()))
             {
              // Añadirlo
              treeMapValue.put (id_tpdu.getID_Socket(),Boolean.FALSE);
              bHayQueRtx = true;
             }
            else // Lo contiene habría que preguntar si es TRUE O FALSE EN CASO
                 // de que no haya establecido que se rtx sólo si es la primera vez.
                 {
                  if (!bRtxSiNoSincronizadoyEs1ªRtx
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

          // Comprobar si este socket está entre los que se van a mandar en la
          // lista de no asentidos del tpdu rtx
          if (treeMapIDGLNoEnviadoAsentimiento.containsKey (idglNuevoNext))
           {
            treeMapValue = (TreeMap)this.treeMapIDGLNoSincronizado.get(idglNuevoNext);
            if (!treeMapValue.containsKey (id_tpdu.getID_Socket()))
             {
              // Añadirlo
              treeMapValue.put (id_tpdu.getID_Socket(),Boolean.FALSE);
              bHayQueRtx = true;
             }
            else // Lo contiene habría que preguntar si es TRUE O FALSE EN CASO
                 // de que no haya establecido que se rtx sólo si es la primera vez.
                 {
                  if (!bRtxSiNoSincronizadoyEs1ªRtx
                      &&
                      !((Boolean)treeMapValue.get(id_tpdu.getID_Socket())).booleanValue() )
                            bHayQueRtx = true;
                 }
           }
         }// Fin del while
       } // Fin del if

      if (!bRtxSiNoSincronizado
         &&
         !bRtxSiNoSincronizadoyEs1ªRtx)
            bHayQueRtx = true;

      // Comprobar si figura como nuevo emisor
      ID_TPDU id_tpduInicioNuevoEmisor = (ID_TPDU)
              this.treeMapNuevosEmisoresASincronizar.get (id_tpdu.getID_Socket());
      if (id_tpduInicioNuevoEmisor != null)
       {
        // Si esta semiasentido es porque soy su CG y todos han enviado un
        // asentimiento o semiasentimiento positivo.
        // Si no lo tengo en la ventana es porque lo he eliminado. Otro sería
        // el CG y ese será el que lo haya sincronizado.
        if (this.tablaAsentimientos.semiAsentido (id_tpduInicioNuevoEmisor)
            ||
            this.buscarTPDUEnVentana (id_tpduInicioNuevoEmisor)==null
            )
         {
          this.treeMapNuevosEmisoresASincronizar.remove (id_tpdu.getID_Socket());
         }
         else {
               if (bRtxSiNoSincronizadoyEs1ªRtx)
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
               bRtxSiNoSincronizadoyEs1ªRtx)
                 bHayQueRtx = false;
           else bHayQueRtx = true;
          }

    // Rtx. el TPDU si hay algún socket en la lista de no sincronizado
    if (bHayQueRtx)
     {
      //Log.debug (Log.TPDU_RTX,mn,"HAY QUE RTX HA RESULTADO TRUE");
      if (bRtxSiContieneIdNoAsentidos)
       {
         // Si los treeMap de identificadores que no han enviado asentimiento
         // están vacios entonces no rtx.
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
              - Hijos y vecinos a excepción de lo que diga las variables
              rtxSoloAHijos y rtxSoloAVecinos, es decir, en principio tiene que
              llegar a todos.
        */
        TPDUDatosRtx tpduDatosRtx;
        for (int i=0;i<vectorTPDURtx.size();i++)
         {
          tpduDatosRtx = (TPDUDatosRtx)vectorTPDURtx.elementAt (i);
          Buffer bufAEnviar = tpduDatosRtx.construirTPDUDatosRtx();

          // Si ha llegado hasta aquí es porque tiene que ser enviado.
          // Sólo hay que ajustar el TTL de envío.
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
  * Notifica que IDGL ha sido añadido. Si el modo es fiable retrasado anota el
  * idgl como no sincronizado para todos los emisores fuentes, y apunta como
  * recibido HSACK para todos los id_tpdus por los que se está esperando
  * asentimiento.
  * Ejecutada por el thread DatosThread.
  * @param idgl idgl añadido
  * @see #treeMapIDGLNoSincronizado
  * @see #treeMapID_SocketNoSincronizado
  */
  private void añadirIDGL(IDGL idgl)
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
   // Código ejecutado por CGLThread

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
  * Notifica que id_socket ha sido añadido. Si el modo es fiable retrasado anota el
  * id_socket como no sincronizado para todos los emisores fuentes, y apunta como
  * recibido ACK para todos los id_tpdus por los que se está esperando
  * asentimiento.
  * Ejecutada por el thread DatosThread.
  * @param id_socket id_socket añadido
  * @see #treeMapID_SocketNoSincronizado
  * @see #treeMapIDGLNoSincronizado
  */
  private void añadirID_Socket(ID_Socket id_socket)
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
  * Elimina toda la información almacenada que haga referencia a id_Socket.<br>
  * Ejecutada por el thread DatosThread.
  * @param id_Socket id_socket que ha sido eliminado de la comunicación.
  */
  private void eliminarID_Socket(ID_Socket id_socket)
  {
   // Código ejecutado por CGLThread

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

   // No elimino del temporizador. Conforme vaya venciendo se irá eliminando
   // automáticamente.
   if (this.iMODO_FIABILIDAD == PTMF.PTMF_FIABLE_RETRASADO)
    {
     /*
     Buscar por la clave y eliminar. No es necesario eliminar a id_socket
     de los valores asociados a todas las claves, puesto que se supone que
     el emisor id_socket (figura en values) no enviará más tpdu.
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
  * Notifica que ID_Socket ha sido añadido.
  * Ejecutada por el thread CGLThread.
  * @param id_socket id_socket añadido
  */
  public void ID_SocketAñadido(ID_Socket id_socket)
  {
   if (this.vectorIDAñadidos==null)
    return;

   this.vectorIDAñadidos.add (id_socket);
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
  * Notifica que IDGL ha sido añadido
  * Ejecutada por el thread CGLThread.
  * @param idgl idgl añadido
  */
  public void IDGLAñadido(IDGL idgl)
  {
   if (this.vectorIDAñadidos==null)
    return;

   this.vectorIDAñadidos.add (idgl);
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
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */
class RegistroHSACK
{
  /** id_tpdu que se tiene que semi-asentir */
  ID_TPDU id_tpdu   = null;

  /** número de ráfaga a la que pertenece id_tpdu */
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
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */
class RegistroVectorMACK

{
  /** ID_Socket fuente y número de secuencia inicial del MACK */
  ID_TPDU id_tpdu  = null;
  /** Número de ráfaga del MACK. */
  int     iNRafaga = 0;

   //=================================================================
   /**
    * Comprueba si este RegistroVectorMACK es igual al pasado por parámetro.
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

