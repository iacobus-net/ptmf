//============================================================================
//
//	Copyright (c) 1999,2004 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	File: CGLThread.java  1.0 13/10/99
//
//	Description: Class CGLThread.
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


package ptmf;

import java.io.IOException;

import java.util.TreeMap;
import java.util.Hashtable;
import java.util.WeakHashMap;
import java.util.Random;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.Vector;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Thread que implementa la maquina de estados CGL.<br>
 * Thread de procesamiento de TPDUs CGL.<br>
 * Procesamiento interno de la clase SocketPTMF.
 * @see SocketPTMF
 * @author M. Alejandro Garcï¿½a Domï¿½nguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */

class CGLThread extends Thread
{
  //== CONSTANTES ===========================================================

  /** Nï¿½ de mensajes CGL redundantes mandados por la red.*/
  private static final short REDUNDANCIA_CGL = 2;

  /** Tiempo de redundancia. Tiempo a esperar entre las TPDU redundantes */
  private static final int TIEMPO_REDUNDANCIA = 10; //10 mseg.

  /** Nï¿½ Mï¿½ximo de intentos de bï¿½squeda de un grupo local.*/
  private static final short MAX_INTENTOS_BUSQUEDA_GRUPO_LOCAL = 2;

  /** Timepo minimo de espera en temporizadores */
  private static final int T_BASE = 10;

  /** Nï¿½ Mï¿½ximo de intentos de bï¿½squeda de CGL para un IDGL Emisor.*/
  private static final short MAX_INTENTOS_BUSQUEDA_GL_EMISOR = 1;

  /** TiME OUT para un TPDU SOCKET_ACEPTADO_EN_GRUPO_LOCAL*/
  private static final int T_ESPERA_ACEPTACION_EN_GRUPO_LOCAL = 2000; //2 Seg.

  /** Nï¿½ MAX. de intentos de espera aceptacion en Grupo Local */
  private static final short MAX_INTENTOS_ESPERA_ACEPTACION_GRUPO_LOCAL = 2;

  /**
   * Tiempo para TEMPORIZADOR TesperaGL.
   * Tiempo a esperar despuï¿½s de enviar una peticiï¿½n
   * de bï¿½squeda de grupo local  (TPDU_CGL_BUSCAR_GRUPO_LOCAL)
   * y antes de enviar otra
   */
  private static final int T_ESPERA_BUSQUEDA_GL = 2000;//5000; //msg

  /**
   * Tiempo para TEMPORIZADOR TesperaGLVECINO.
   * Tiempo a esperar despuï¿½s de enviar una peticiï¿½n
   * de bï¿½squeda de grupo local vecino (TPDU_CGL_BUSCAR_GRUPO_LOCAL_VECINO)
   * y antes de enviar otra.
   */
  private static final int T_ESPERA_BUSQUEDA_GL_VECINO = 2000;//5000; //msg

  /**
   * Tiempo para TEMPORIZADOR TesperaAceptacionGL.
   * Tiempo de espera de aceptaciï¿½n de un
   * mensaje ACEPTADO_EN_GRUPO_LOCAL.
   */
  private static final int T_ESPERA_ACEPTACION_GL = 2000;//4000; //mseg

  /**
   * Tiempo para TEMPORIZADOR TretrasoEsperaBuesquedaCGEmisor
   */
  private static final int T_ESPERA_BUSQUEDA_GL_EMISOR = 6000;//6000; // msg

  /**
   * Tiempo para TEMPORIZADOR TretrasoNotificacionGL.
   * Tiempo aleatorio de retraso antes
   * de enviar un mensaje GRUPO_LOCAL
   */
  private static final int T_RETRASO_NOTIFICACION_GL = 500; // msg

  /**
   * Tiempo para TEMPORIZADOR TretrasoNotificacionGLVecinos.
   * Tiempo aleatorio de retraso antes
   * de enviar un mensaje GRUPO_LOCAL "SOLO" cuando se ha recibido un
   *  TPDU BUSCAR_GRUPO_LOCAL_VECINO
   */
  private static final int T_RETRASO_NOTIFICACION_GL_VECINO = 1000; // msg


  /**
   * Tiempo para TEMPORIZADOR TRetrasoNotificacionGLSocketAceptado.
   * Tiempo aleatorio de retraso antes
   * de enviar un mensaje SOCKET_ACEPTADO_EN_GRUPO_LOCAL.
   */
  private static final int T_RETRASO_NOTIFICACION_SOCKET_ACEPTADO = 600; // msg


   /**
   * Tiempo para TEMPORIZADOR TretrasoNotificacionCGParaEmisor
   */
  private static int T_RETRASO_NOTIFICACION_GL_PARA_EMISOR = 600; //msg


  /** Tiempo de retraso para volver a procesar un estado de la mï¿½quina de estados
      CGL  */
  private static final int   TIEMPO_RETRASO_PROCESO_MAQUINA_ESTADO_CGL = 2; // msg

  /** Nï¿½mero Mï¿½ximo de sockets en el grupo local.  */
  private int N_MAX_SOCKETS_GL = PTMF.MAX_SOCKETS_GL;

  /**
   * Evento Notificar GL VECINOS.
   * Se manda un TPDU GRUPO_LOCAL cuando expira el timer T_RETRASONOTIFICACIONGLVECINO
   * Este temporizador se activa cuando se recibe el TPDU BUSCAR_GRUPO_LOCAL_VECINOS
   */
  protected static final int  EVENTO_NOTIFICAR_GL_VECINOS = 1;

  /**
   * Evento Notificar GL
   * Se manda un TPDU GRUPO_LOCAL cuando expira el timer T_RETRASO_NOTIFICACION_GL
   * Este temporizador se activa cuando se recibe el TPDU BUSCAR_GRUPO_LOCAL
   */
  protected static final int  EVENTO_NOTIFICAR_GL = 2;
  /** Evento Notificar CG PARA EMISOR*/
  protected static final int  EVENTO_NOTIFICAR_GL_EMISOR = 7;
  /** Evento Notificar Aceptaciï¿½n de socket*/
  protected static final int  EVENTO_ACEPTAR_SOCKET = 3;
  /** Evento BUSCAR GL*/
  protected static final int  EVENTO_BUSCAR_GL = 4;
  /** Evento BUSCAR GL Vecino*/
  protected static final int  EVENTO_BUSCAR_GL_VECINO = 5;
  /** Evento BUSQUEDA DE CG PARA EMISOR*/
  protected static final int  EVENTO_BUSCAR_GL_EMISOR = 6;
  /** Evento TIME OUT espera de TPDU SOCKET_ACEPTADO_EN_GRUPO_LOCAL*/
  protected static final int  EVENTO_TIME_OUT = 8;



  //== PROTOCOLO CGL ========================================================
  /** IDGL al que pertenece este socket. */
  private IDGL idgl = null;

  /** TreeMap de sockets que pertenecen al grupo local. KEY= IPv4 VALUE=null */
  private TreeMap treeMapID_Socket = null;


  /**
   * TreeMap de IDGLs.
   * KEY= IDGL
   * VALUE= TreeMap de IDGLS a los que llega el "IDGL" KEY.
   */
  private TreeMap treeMapIDGLs = null;


  /**
   * WeakHashMap Cache
   * KEY= IDGLFuente
   * VALUE= IDGLs Padres
   */
  private WeakHashMap cachePadres = null;
  /** La capacidad de la cache Padre */
  private int CAPACIDAD_CACHE_PADRES = 50;

  /**
   * WeakHashMap Cache Hijos
   * KEY= IDGLFuente
   * VALUE= IDGLs Hijos
   */
  private WeakHashMap cacheHijos = null;
  /** La capacidad de la cache Hijo */
  private int CAPACIDAD_CACHE_HIJOS = 50;


  /** Nï¿½mero de sockets actuales en el grupo local*/
  private int N_SOCKETS = 0;

  /**
   * TTL usado en los mensajes CGL. Distinto del de la sesiï¿½n, utilizado en el
   *   anillo de expansiï¿½n en la bï¿½squeda de grupos locales.
   */
  private byte TTL = 0;


  //== VAR. DE LA CLASE ======================================================
  /** Un flag utilizado para iniciar/parar este thread. */
  private boolean  runFlag = true;

  /** El socket PTMF */
  private SocketPTMFImp socketPTMFImp = null;

  /** Estado de la mï¿½quina de estados CGL */
  private int estadoCGL = PTMF.ESTADO_CGL_NULO;

  /**
   * Semï¿½foro para parar a DatosThread cuando nos pregunta por IDGLsPadres y
   * tenhemos que mANDAR un tpdu buscar_cg_emisor
   */
  private Semaforo semaforoDatosThread = null;

  /**  Nï¿½ de Intentos (Bï¿½squeda de grupo, ï¿½tc.) */
  private short N_INTENTOS = 0;

  /** TPDU CGL */
  private TPDUCGL  tpduCGL   = null;

  /** Direccion del emisor del TPDU CGL */
  private Address  src       = null;

  /** Boolean. Buscar GL */
  private boolean bBuscarGL  = false;

  /** Boolean. Buscar GL Vecino*/
  private boolean bBuscarGLVecino  = false;

  /** Boolean para Aceptaciï¿½n de un socket en el grupo local */
  private boolean bAceptarSocket = false;

  /** Boolean para Notificaciï¿½n de Grupo Local*/
  private boolean bNotificarGL = false;

  /** Boolean para Notificaciï¿½n de Grupo Local a los grupos vecinos */
  private boolean bNotificarGLVecinos = false;

  /** Boolean para Notificaciï¿½n de BUSQUEDA DE EMISOR */
  private boolean bNotificarBusquedaEmisor = false;

  /** Boolean para Notificaciï¿½n de CG Emisor*/
  private boolean bNotificarCGEmisor = false;

  /** Boolean para indicar que se ha lanzado un Temporizador para enviar un TPDU GL */
  private boolean bLanzadoTemporizadorNotificacionGL = false;

  /** Boolean para indicar que se ha lanzado un Temporizador para enviar un TPDU GL VECINO*/
  private boolean bLanzadoTemporizadorNotificacionGLVecino = false;

  /** Boolean para Retransmitir UNIRSE_A_GRUPO_LOCAL */
  private boolean bRetransmitirUnirseGrupoLocal = false;
  /** Objeto Random */
  private Random random = null;

  /** Temporizador  */
  private TimerHandler timerHandler = null;

  /** Cola de aceptaciï¿½n de sockets */
  private  LinkedList colaAceptacionID_SOCKET = null;

  /** Lista de interface de notificaciï¿½n ID_Socket */
  private LinkedList listaId_SocketListener = null;

  /** Lista de interfaces de notificaciï¿½n ID_SOCKET */
  private  LinkedList listaIDGLListener = null;

  /** Lista de Bï¿½squeda de CG para Emisores...*/
  //
  //  key = idgl_emisor
  //  value = clase Intentos_Emisor
  //
  private  TreeMap treeMapBusquedaEmisores = null;

  /** Lista de repuesta a Busqueda de CG para Emisores...*/
  private  TreeMap treeMapRespuestaBusquedaEmisores = null;

  /** Lista de Grupos que buscan GRUPOS_LOCALES_VECINOS...*/
  private  TreeMap treeMapRespuestaGLVecinos = null;

  /** TTL de notificaciï¿½n del Grupo Local */
  private short TTL_Notificacion_GL = 1;

  private short TTL_BUSCAR_GRUPO_LOCAL_VECINO = 1;

  //==========================================================================
  /**
   * Constructor. Establece el thread como un thread demonio.
   */
  CGLThread (SocketPTMFImp socketPTMFImp)
  {
    super("CGLThread");

    //PRIORIDAD..
    //this.setPriority(Thread.MAX_PRIORITY - 2);

    if(socketPTMFImp != null)
    {
      //
      // Thread demonio...
      //
      setDaemon(true);
      this.socketPTMFImp = socketPTMFImp;
      this.runFlag = true;

      try
      {
        //
        // Crear hash,treemap,linkedlist,
        //   obtener direcciï¿½n unicast local, ...
        //
        this.treeMapIDGLs = new TreeMap();
         this.colaAceptacionID_SOCKET = new LinkedList();
        this.treeMapBusquedaEmisores = new TreeMap();
        this.treeMapRespuestaBusquedaEmisores = new TreeMap();
        this.treeMapRespuestaGLVecinos = new TreeMap();
        this.listaIDGLListener = new LinkedList();
        this.listaId_SocketListener = new LinkedList();
        this.random = new Random();
        this.treeMapID_Socket = new TreeMap();

        //Semï¿½foro...
        this.semaforoDatosThread = new Semaforo(true,1);

        //
        //CACHES...
        cachePadres = new WeakHashMap(CAPACIDAD_CACHE_PADRES,1);
        cacheHijos = new WeakHashMap(CAPACIDAD_CACHE_HIJOS,1);

        //
        // TPDUCGL y Direccion del emisor
        //
        this.src       = new Address();

        //
        // Iniciar Temporizadores...
        //
        this.timerHandler  =
         new TimerHandler()
         {
             public void TimerCallback(long arg1, Object o)
             {
               switch((int)arg1)
               {
                case EVENTO_NOTIFICAR_GL:
                  Log.debug(Log.CGL,"CGLThread.timerHandler","EVENTO_NOTIFICAR_GL");
                  bNotificarGL = true;
                  break;
                case EVENTO_NOTIFICAR_GL_VECINOS:
                  Log.debug(Log.CGL,"CGLThread.timerHandler","EVENTO_NOTIFICAR_GL_VECINOS");
                  bNotificarGLVecinos = true;
                  break;
                case EVENTO_ACEPTAR_SOCKET:
                  Log.debug(Log.CGL,"CGLThread.timerHandler","EVENTO_ACEPTAR_SOCKET");
                  bAceptarSocket = true;
                  break;
                case EVENTO_BUSCAR_GL:
                  Log.debug(Log.CGL,"CGLThread.timerHandler","EVENTO_BUSCAR_GL");
                  bBuscarGL = true;
                  break;
                case EVENTO_BUSCAR_GL_VECINO:
                  Log.debug(Log.CGL,"CGLThread.timerHandler","EVENTO_BUSCAR_GL_VECINO");
                  bBuscarGLVecino = true;
                  break;
                case EVENTO_BUSCAR_GL_EMISOR:
                  Log.debug(Log.CGL,"CGLThread.timerHandler","EVENTO_BUSCAR_EMISOR");
                  bNotificarBusquedaEmisor = true;
                  break;
                case EVENTO_NOTIFICAR_GL_EMISOR:
                  Log.debug(Log.CGL,"CGLThread.timerHandler","EVENTO_GL_EMISOR");
                  bNotificarCGEmisor = true;
                  break;
                case EVENTO_TIME_OUT:
                  //Este evento se utiliza en las esperas del TPDU SOCKET_ACEPTADO_EN_GRUPO_LOCAL
                  Log.debug(Log.CGL,"CGLThread.timerHandler","EVENTO_TIME_OUT");
                  //setEstadoCGL(PTMF.ESTADO_CGL_BUSCAR_GL);
                  N_INTENTOS++;
                  if(N_INTENTOS > MAX_INTENTOS_ESPERA_ACEPTACION_GRUPO_LOCAL)
                  {
                    setEstadoCGL(PTMF.ESTADO_CGL_BUSCAR_GL);
                    N_INTENTOS = 0;
                  }
                  else
                    bRetransmitirUnirseGrupoLocal = true;

                  break;
               }
             }
          };

      }
      catch(UnknownHostException e){error(e);}
      catch(ParametroInvalidoExcepcion e){error(e);}

    }
  }


  //==========================================================================
  /**
   * Aï¿½adir una interface ID_SocketListener.
   * @param IDSocketListener
   */
   void addID_SocketListener(ID_SocketListener id_socketListener)
   {
      listaId_SocketListener.add(id_socketListener);
   }

  //==========================================================================
  /**
   * Eliminar una interface ID_SocketListener.
   * @param IDSocketListener
   */
   void removeID_SocketListener(ID_SocketListener id_socketListener)
   {
     listaId_SocketListener.remove(id_socketListener);
   }

  //==========================================================================
  /**
   * Aï¿½ade una interface IDGLListener.
   * @param IDGLListener
   */
   void addIDGLListener(IDGLListener idglListener)
   {
      this.listaIDGLListener.add(idglListener);
   }

  //==========================================================================
  /**
   * Elimina una interface IDGLListener.
   * @param IDGLListener
   */
   void removeIDGLListener(IDGLListener idglListener)
   {
      this.listaIDGLListener.remove(idglListener);
   }

  //==========================================================================
  /**
   * El mï¿½todo run del thread. <br>
   * Procesa TPDU recibidos CGL y datos.<br>
   * Gestiona la mï¿½quina de estados CGL.<br>
   * Gestiona la fiabilidad de los datos y el control de flujo y congestiï¿½n.
   */
  public void run()
  {
    final String    mn = "SocketPTMFThread.run()";
    Buffer          buf;
    Address         src;


    //
    // Bucle infinito.
    //
    Log.debug(Log.CGL,"CGLThread","INICIADO");
    while (this.runFlag || (getEstadoCGL()!= PTMF.ESTADO_CGL_NULO ))
    {

      //
      // Procesar estado y TPDUs CGL
      //
      maquinaEstadoCGL();

      if (!this.runFlag)
       {
          Log.debug(Log.CGL,mn,"Fin del hilo CGL.");
          return;
       }

      //
      // Esperar un tiempo para volver a procesar...
      //
      this.socketPTMFImp.getTemporizador().sleep(TIEMPO_RETRASO_PROCESO_MAQUINA_ESTADO_CGL);
      //this.socketPTMFImp.getTemporizador().yield();
    }
    Log.debug(Log.CGL,"CGLThread","FINALIZADO");

  }


  //==========================================================================
  /**
   * El mï¿½todo stop del thread. Pone a false el flag runFlag provocando que
   * el thread finalice. No llama al mï¿½todo stop() de la clase Thread.
   */
  void stopThread()
  {
    //
    // CAMBIAR DE ESTADO --> DEJAR_GL
    //
    this.setEstadoCGL(PTMF.ESTADO_CGL_DEJAR_GL);
    runFlag = false;
  }

  //==========================================================================
  /**
   * Este mï¿½todo implementa la mï¿½quina de estados CGL.<br>
   * El estado de la mï¿½quina se almacena en la variable estadoCGL.
   */
  private void maquinaEstadoCGL()
  {
    int      estadoCGL = 0;

    //
    // Obtener el estado CGL...
    //
    estadoCGL = getEstadoCGL();

    switch(estadoCGL)
    {
      case PTMF.ESTADO_CGL_NULO:
        maquinaEstadoCGL_NULO();
        break;
      case PTMF.ESTADO_CGL_BUSCAR_GL:
        maquinaEstadoCGL_BUSCAR_GL();
        break;
      case PTMF.ESTADO_CGL_ESPERAR_ACEPTACION_GL:
        maquinaEstadoCGL_ESPERAR_ACEPTACION_GL();
        break;
      case PTMF.ESTADO_CGL_MIEMBRO_GL:
        maquinaEstadoCGL_MIEMBRO_GL();
        break;
      case PTMF.ESTADO_CGL_CREAR_GL:
        maquinaEstadoCGL_CREAR_GL();
        break;
      case PTMF.ESTADO_CGL_BUSCAR_GL_VECINOS:
        maquinaEstadoCGL_BUSCAR_GL_VECINOS();
        break;
      case PTMF.ESTADO_CGL_DEJAR_GL:
        maquinaEstadoCGL_DEJAR_GL();
        break;
      case PTMF.ESTADO_CGL_MONITOR:
        maquinaEstadoCGL_MONITOR();
        break;
      default:
        Log.log("CGLThread.maquinaEstadoCGL()","estado CGL incorrecto.");
    }
  }


  //==========================================================================
  /**
   * Se devuelve un TreeMap con los IDGLs que actï¿½an como CG "Padres" para
   * este socket dado el IDGL del emisor.
   *
   * POLï¿½TICA:
   *
   * Un IDGL Padre es todo aquel IDGL que hemos recibido mediante un mensaje
   *  GRUPO_LOCAL_VECINO

   *  DEVOLVER:
   *
   * 1ï¿½ Caso: Si el socket emisor es este o pertenece a este GL.
        -	No existen GL Padres.
   *
   * 2ï¿½ Caso: Si el socket emisor NO Pertenece a este GL.
        1.Si el IDGL del emisor estï¿½ en la lista listaIDGLs el IDGL emisor es
          alcanzable por este GL y por lo tanto serï¿½ GL Padre.
        2.Si el IDGL del emisor no estï¿½ en la lista listaIDGLs se recorre
          la lista listaIDGLs para averiguar si en las sublistas de cada IDGL
          estï¿½ el IDGL Emisor, aquellos IDGLs en cuyas sublistas se halle
          el IDGL del emisor serï¿½n GLs Padres.
        3.Si no se encuentra el IDGL Emisor en la lista ni en las sublistas
          de los IDGLs, se recurre a una bï¿½squeda especï¿½fica
          tal como se describe en la secciï¿½n 6.15



   *  -Si llegamos directamente al emisor devolvemos el Emisor
   *  -Si no llegamos ver si algï¿½nh IDGL Padre Potencial llega y devolver
   *    la lista de aquellos que lleguen.
   *  -Si no hay ningï¿½n IDGL que llegue al IDGL Emisor buscarlo enviando un
   *    mensaje BUSCAR_GL_PARA_EMISOR. SE BLOQUEA EL THREAD LLAMANTE HASTA QUE
   *    SE OBTENGA UNA RESPUESTA O SE LLEGUE A UN TIME-OUT..
   *
   * @param idglEmisor IDGL del emisor para el que tenemos que buscar CG "Padres".
   *  Los IDGL estï¿½n en Key, Value es siempre NULL.
   * @return Devuelve un objeto TreeMap con los IDGLs de los Controladores de
   *  grupo padres para este emisor.
   */
  TreeMap getCGPadres(IDGL idglEmisor)
  {
      final String mn = "CGLThread.getCGPadres (idgl)";
      TreeMap treemap = null;


      if (idglEmisor==null)
        return new TreeMap();

      //1ï¿½ Caso: El IDGL emisor es igual a este.
      if (idglEmisor.equals (this.getIDGL()))
      {
        //DEPURACION
        Log.debug(Log.CGL,mn,"No hay IDGLS Padres!");//Log.log("getCGhIJOS() == 0","");

        return new TreeMap();
      }

      //
      // 1ï¿½. Comprobar si estï¿½ en la cache....
      // NOTA: Si llegan nuevos datos que afecten a la cache, esta elimina la
      // entrada para el IDGL afectado
      //

      if( this.cachePadres.containsKey(idgl))
        return (TreeMap)this.cachePadres.get(idgl);

      //
      // 2.1
      //
      if( this.treeMapIDGLs.containsKey(idglEmisor) )
      {
        treemap = new TreeMap();

        Log.debug(Log.CGL,mn,"El IDGL emisor es directamente alcanzable");

        //Obtener el IDGL emisor del treemap , para obtener el TTL correcto...
        RegistroIDGL_TreeMap reg  = (RegistroIDGL_TreeMap) treeMapIDGLs.get(idglEmisor);
        treemap.put(reg.idgl,null);

        return treemap;
      }

      //
      //  2.2
      //
      // No llegamos directamente, ver si algï¿½n GL llega al GL solicitado
      //

      Iterator iterator = this.treeMapIDGLs.values().iterator();
      while(iterator.hasNext())
      {
          RegistroIDGL_TreeMap reg = (RegistroIDGL_TreeMap) iterator.next();

          if(reg.idgl.equals(this.idgl))
          {
            continue;
          }

          if((reg.treemap!= null) &&(reg.treemap.containsKey(idglEmisor)))
          {
            if(treemap == null)
            {
               treemap = new TreeMap();
            }

            //Aï¿½adir el IDGL que llega al "Emisor"....
            treemap.put(reg.idgl,null);

            Log.debug(Log.CGL,mn,"El IDGL emisor se alcanza por:"+ reg.idgl);


          }

       }



      if (treemap == null)
      {
        //
        //  2.3
        //
        //  No llegamos ni existe conocimiento de que algï¿½n Vecino llegue,
        //  "PO" PREGUNTAMOS....
        //
        //.... ï¿½bloqueamos cuando DatosThread nos pregunte?
        //.... ï¿½Establecemos un callback? .....

        Log.debug(Log.CGL,mn,"El IDGL emisor no es directamente alcanzable por nadie, iniciando procedimiento de bï¿½squeda...");

        this.treeMapBusquedaEmisores.put(idglEmisor,new Intentos_Emisor(idglEmisor,(short)0));
        this.TTL_BUSCAR_GRUPO_LOCAL_VECINO = 1;
        enviarTPDU_CGL_BUSCAR_GL_PARA_EMISOR(idglEmisor);

        //
        //Temporizador de espera para volver a reintentar...
        //
        this.socketPTMFImp.getTemporizador().registrarFuncion(this.T_ESPERA_BUSQUEDA_GL_EMISOR,
          timerHandler,this.EVENTO_BUSCAR_GL_EMISOR);

        //Bloquer a DatosThread....
        //Log.log (mn,"HA DORMIDO AL HILO DE DATOS.");
        this.semaforoDatosThread.down();

        //Cuando nos despertemos, puede haber sucedido dos cosas:
        //1. Se ha encontrado un IDGL "Padre" para el IDGL preguntado y se ha
        // almacenado en la cache.... ï¿½
        //2. No se ha encontrado ningï¿½n IDGL "Padre" y no estï¿½ en la cache...
        if( this.cachePadres.containsKey(idgl))
        {
         return (TreeMap)this.cachePadres.get(idgl);
        }

      }
      else
      {   //Aï¿½adir a la cache...
         this.cachePadres.put(idglEmisor,treemap);
      }


     return treemap;
  }

  //==========================================================================
  /**
   * Obtener IDGL "Hijos" que dependen de este IDGL para el Control de la
   * Fiabilidad. Devuelve un TreeMap con los IDGLs
   *  que actï¿½an como "hijos" para este IDGL dado el IDGL de un determinado emisor.
   * @param idglEmisor IDGL del emisor para el que tenemos que buscar CG "Hijos".
   * @return Devuelve un objeto TreeMap con los IDGLs de los Controladores de
   *  de grupo hijos para este emisor.
   */
  private TreeMap getCGHijos()
  {
   return this.getCGHijos(this.idgl);
  }


  //==========================================================================
  /**
   * Devuelve un TreeMap con los IDGLs  que actï¿½an como "hijos" para
   * este socket dado un IDGL "Emisor".<br>
                                                    <br>
   *
   * POLï¿½TICA:<br>
   *          <br>


   *  1ï¿½ Caso: Si el socket emisor es este o pertenece a este GL
   *           - Todos los IDGLs de la lista listaIDGLs son considerados
   *             como GL Hijos

   *
   *  2ï¿½ Caso: Si el socket emisor NO es este o NO pertenece a este GL:
   *           -  Todos aquellos IDGLs de la lista listaIDGLs
   *                 (menos el IDGL emisor si estï¿½) son considerados como
   *                  GL Hijos solo si en la sublista de estos IDGLs
   *                  no aparece el IDGL emisor.


   * @param idglEmisor IDGL del emisor para el que tenemos que buscar CG "Hijos".
   * @return Devuelve un objeto TreeMap con los IDGLs de los Controladores de
   *  de grupo hijos para este emisor.
   */
  TreeMap getCGHijos(IDGL idglEmisor)
  {
      TreeMap treemap = null;
      String mn = "CGLThread.getCGHijos";

      if (idglEmisor==null || this.idgl==null)
        return new TreeMap();


      //
      // 1ï¿½. Comprobar si estï¿½ en la cache....
      // NOTA: Si llegan nuevos datos que afecten a la cache, esta elimina la
      // entrada para el IDGL afectado
      //
      if( this.cacheHijos.containsKey(idgl))
        return (TreeMap)this.cacheHijos.get(idgl);

      //
      // -1* 1ï¿½ CASO: Si el IDGL Emisor es el mismo que el nuestro:
      //          -->>>> devolver todos los IDGLs
      //
      if(idglEmisor.equals(this.idgl))
      {
       Log.debug(Log.CGL,mn,"Soy IDGL emisor");
       TreeMap treemap1 = (TreeMap)this.treeMapIDGLs.clone();
       treemap1.remove(this.idgl);
       //Aï¿½adir a la cache...
       this.cacheHijos.put(idglEmisor,treemap1);


       // DEPURACION:
       Log.debug(Log.CGL,mn,"IDGLS Hijos: ");
       if(treemap1 != null && treemap1.size() > 0)
       {
        Iterator iterator = treemap1.keySet().iterator();

         while(iterator.hasNext())
         {
           IDGL idgl = (IDGL) iterator.next();
           Log.debug(Log.CGL,"","IDGL --> "+idgl);
         }
       }

       return treemap1;
      }


      // -2* 2ï¿½ CASO: Si el IDGL Emisor Fuente no es este:
      //      -->>>> Devolver como IDGL Hijos:
      //            Aquellos IDGLs que no lleguen
      //            directamente al IDGL Emisor.
      //
      treemap = new TreeMap();

      if(this.treeMapIDGLs.keySet().size() > 0)
      {

        //Log.log("getCGHijos() 2.2","");
        //Recorrer todos los IDGLs, si llegan al "Emisor"
        //no incluirlos en la lista....

        Iterator iterator = this.treeMapIDGLs.values().iterator();

        while(iterator.hasNext())
        {
          RegistroIDGL_TreeMap reg = (RegistroIDGL_TreeMap)iterator.next();

          if(reg == null)
          {
          //  Log.log("reg NULL","");
            continue;
          }

          if(reg.treemap == null)
          { // Log.log("reg.treemap NULL","");
            continue;
          }

          if(reg.idgl == null)
          { // Log.log("reg.idgl NULL","");
                       continue;
          }

          //Si no es el "Emisor" ver si llega al "Emisor"....
          if( !reg.idgl.equals(idglEmisor) && !reg.idgl.equals(this.idgl) &&reg.treemap!= null && !reg.treemap.containsKey(idglEmisor) )
          {
                //El "IDGL" no llega al "Emisor",depende de nosostros...
                treemap.put(reg.idgl,null);
          }
        }


      //DEPURACION
      if (treemap.size() <= 0)
      {
        Log.debug(Log.CGL,mn,"IDGLS Hijos: 0");//Log.log("getCGhIJOS() == 0","");
      }
      else
      {
         Log.debug(Log.CGL,mn,"IDGLS Hijos: 0");
        //Log.log("getCGhIJOS().size() == ",""+treemap.size());
        Iterator iterator1 = treemap.keySet().iterator();

        while(iterator1.hasNext())
        {
          IDGL idgl = (IDGL) iterator1.next();
           Log.debug(Log.CGL,"","IDGL --> "+idgl);
          //Log.log("HIJO --> IDGL: "+idgl,"");

        }
      }



        //Aï¿½adir a la cache...
        this.cacheHijos.put(idglEmisor,treemap);
      }

      //Si no se encuentra nada---> treemap vacï¿½o.
      return treemap;
  }



 //==========================================================================
  /**
   * Obtener todos los ID_Sockets
   * @return treemap con ID_Sockets en KEY. Valor = null.
   */
   TreeMap getID_Sockets()
   {
     return (TreeMap)this.treeMapID_Socket.clone();
   }

  //==========================================================================
  /**
   * Obtener todos los IDGLs Vecinos
   * @return treemap con IDGLs en KEY. Valor = null.
   */
   TreeMap getIDGLs()
   {
     return (TreeMap)this.treeMapIDGLs.clone();
   }

  //==========================================================================
  /**
   * Obtener el numero de IDGLs
   * @return int
   */
   int getNumeroIDGLs()
   {
     return this.treeMapIDGLs.size();
   }

  //==========================================================================
  /**
   * Obtener el numero de IDSockets
   * @return int
   */
   int getNumeroID_Sockets()
   {
     return this.treeMapID_Socket.size();
   }

  //==========================================================================
  /**
   * Notificar nuevo ID_Socket.
   * @param ID_Socket Objeto ID_Socket nuevo.
   */
   private void notificarNuevoID_Socket(ID_Socket id_socket)
   {
     if (id_socket == null)
      return;

      ListIterator iterator = this.listaId_SocketListener.listIterator();
      while(iterator.hasNext())
      {
        ID_SocketListener  id_socketListener = (ID_SocketListener) iterator.next();
        id_socketListener.ID_SocketAñadido(id_socket);
      }

      //Y a los usuarios...
      this.socketPTMFImp.sendPTMFEventAddID_Socket(""+id_socket,id_socket);


   }

  //==========================================================================
  /**
   * Notificar Eliminaciï¿½n ID_Socket.
   * @param ID_Socket Objeto ID_Socket eliminado.
   */
   private void notificarEliminacionID_Socket(ID_Socket id_socket)
   {
     if (id_socket == null)
      return;

      ListIterator  iterator = this.listaId_SocketListener.listIterator();
      while(iterator.hasNext())
      {
        ID_SocketListener  id_socketListener = (ID_SocketListener) iterator.next();
        id_socketListener.ID_SocketEliminado(id_socket);
      }
      //Y a los usuarios...
      this.socketPTMFImp.sendPTMFEventRemoveID_Socket(""+id_socket,id_socket);


   }

  //==========================================================================
  /**
   * Notificar nuevo IDGL.
   * @param IDGL Objeto IDGL nuevo.
   */
   private void notificarNuevoIDGL(IDGL idgl)
   {
     if (idgl == null)
      return;

      ListIterator iterator = this.listaIDGLListener.listIterator();
      while(iterator.hasNext())
      {
        IDGLListener  idglListener = (IDGLListener) iterator.next();
        idglListener.IDGLAñadido(idgl);
      }
      //Y a los usuarios...
      this.socketPTMFImp.sendPTMFEventAddIDGL(""+idgl,idgl);

   }

  //==========================================================================
  /**
   * Notificar Eliminaciï¿½n IDGL.
   * @param IDGL Objeto IDGL eliminado.
   */
   private void notificarEliminacionIDGL(IDGL idgl)
   {
     String mn = "CGLThread.notificarEliminacionIDGL";

     if (idgl == null)
      return;


      ListIterator iterator = this.listaIDGLListener.listIterator();
      while(iterator.hasNext())
      {
        IDGLListener  idglListener = (IDGLListener) iterator.next();
        idglListener.IDGLEliminado(idgl);
        Log.debug(Log.CGL,mn,"Notificado a DATOS_THREAD ELIMINACION IDGL: "+idgl);
      }
      //Y a los usuarios...
      this.socketPTMFImp.sendPTMFEventRemoveIDGL(""+idgl,idgl);


   }

  //==========================================================================
  /**
   * Devuelve el TTL de los sockets del grupo local.
   * Esta funciï¿½n es necesario cuando los sockets de un grupo local
   * no estï¿½n en la misma subred.
   * @return
   */
   short getTTLSocketsGL()
   {
      //En esta implementaciï¿½n es SIEMPRE 1
      return 1;
   }


  //==========================================================================
  /**
   * Devuelve el TTL mï¿½s grande de todos los IDGLs contenidos en el treemap
   *  TreeMapIDGLVecinos. Devuelve como mï¿½nimo ttl=2.
   * @return El TTL Mï¿½ximo de todos los IDGLs contenidos en el treemap. Mï¿½nimo ttl = 2.
   */
   private short getTTLGLMaximo()
   {
      Iterator iterator = this.treeMapIDGLs.keySet().iterator();
      IDGL idgl = null;
      short ttl = 2;

      while(iterator.hasNext())
      {
        idgl = (IDGL) iterator.next();
        if (ttl < idgl.TTL)
          ttl = idgl.TTL;
      }
      //Log.log ("getTTLGLMaximo:",""+ttl);
      return ttl;
   }

  //==========================================================================
  /**
   * Devuelve el TTL mï¿½s grande de todos los IDGLs pasados mediante un TreeMap.
   * @param treemap TreeMap con IDGLS.
   * @return El TTL Mï¿½ximo de los IDGLs padres para un IDGL dado. Mï¿½nimo devuelve
   * el valor 1.
   */
   short getTTLGLMaximo(TreeMap treemap)
   {
      Iterator iterator = treemap.keySet().iterator();
      IDGL idgl = null;
      short ttl = 1;

      while(iterator.hasNext())
      {
        idgl = (IDGL) iterator.next();
        //Log.log("getTTLGLMaximo()--> IDGL: "+idgl,"");

        if (idgl.TTL > ttl)
          ttl = idgl.TTL;
      }

      //Log.log ("getTTLGLMaximo (treeMap):",""+ttl);
      return ttl;
   }


  //==========================================================================
  /**
   * Elimina un ID_SOCKET del treemap ID_SOCKETS. EL ID_SOCKET identifica a un
   *  socket que se ha caï¿½do o no responde y se quita de la lista.
   * @param id_socket El socket que se elimina de la lista.
   */
  void removeID_SOCKET(ID_Socket id_socket)
  {
    //Eliminar
    this.treeMapID_Socket.remove(id_socket);
    //Quitar nï¿½mero de sockets
    this.N_SOCKETS--;

    if(this.getEstadoCGL() == PTMF.ESTADO_CGL_MIEMBRO_GL)
    {
     //
     // enviar un TPDU notificando que el socket se elimina...
     //
     // NO--> Evitar posibles prï¿½cticas hackers.... :)
     //this.enviarTPDU_CGL_DEJAR_GRUPO_LOCAL(id_socket);
    }

    //Notificar eliminaciï¿½n a las clases de PTMF....
    this.notificarEliminacionID_Socket(id_socket);



  }

  //==========================================================================
  /**
   * Elimina un IDGL, del treemap treemapIDGLVecinos
   */
  void removeIDGL (IDGL idgl)
  {
    if (this.idgl.equals(idgl))
      return;

    //Eliminar el IDGL del Treemap
    this.treeMapIDGLs.remove(idgl);


    //Y de los Treemaps internos de Padres Potenciales...
    Iterator iterator = this.treeMapIDGLs.values().iterator();
    while(iterator.hasNext())
    {
       RegistroIDGL_TreeMap reg = (RegistroIDGL_TreeMap)iterator.next();
       if(reg.treemap!=null)
         reg.treemap.remove(idgl);
    }

    //Comprobar cache...
    comprobarCache(idgl);

    //Notificar eliminaciï¿½n a las clases de PTMF....
    this.notificarEliminacionIDGL(idgl);
   }

  //==========================================================================
  /**
   * Obtener un TreeMap de ID_Sockets de los MIEMBROS DE ESTE GRUPO.
   * @return Devuelve un objeto TreeMap de ID_SOCKETS.
   */
  private TreeMap getTreeMapID_Socket()
  {
   return this.treeMapID_Socket;
  }


 //==========================================================================
  /**
   * Obtener un TreeMap de ID_Sockets de los MIEMBROS VECINOS DE ESTE GRUPO.
   * Es decir, de los miembros de este grupo menos este socket.
   * @return Devuelve un objeto TreeMap de ID_SOCKETS.
   * Devuelve una copia: Las modificaciones no afectarï¿½n.
   */
  TreeMap getTreeMapID_SocketVecinos ()
  {
    //TreeMap result = (TreeMap)this.getTreeMapID_Socket().clone();
    TreeMap result = new TreeMap();

    result.putAll(this.treeMapID_Socket);

    result.remove (this.socketPTMFImp.getID_Socket());


    //Log.log ("","Socket Vecinos: " + result);
    return result;
  }

  //==========================================================================
  /**
   * Devuelve true si id_socket pertenece a este mismo grupo local (es vecino).
   */
  boolean esVecino (ID_Socket id_socket)
  {
   return this.treeMapID_Socket.containsKey (id_socket);
  }

  //==========================================================================
  /**
   * Devuelve el nï¿½mero de vecinos de este Grupo Local.
   */
  int numeroVecinos()
  {
   // El nï¿½mero de socket pertenecientes al grupo local menos 1 correspondiente
   // a este socket.
   return this.treeMapID_Socket.size()-1;
  }

  //==========================================================================
  /**
   * Devuelve true si idglPadre es padre jerï¿½rquico para idglFuente.
   */
  boolean esPadre(IDGL idglPadre,IDGL idglFuente)
  {
    if ((idglPadre==null)||(idglFuente==null))
     return false;

    //Comprobar si estamos en la cache para ese idglFuente....
    if(this.cachePadres.containsKey(idglFuente))
    {
      TreeMap treemap = (TreeMap) this.cachePadres.get(idglFuente);
      if (treemap.containsKey(idglPadre))
        return true;
    }

    else
    {
      //Comprobar obteniendo los padres para ese idglFuente..
      TreeMap treemap = (TreeMap) this.getCGPadres(idglFuente);
      if (treemap.containsKey(idglPadre))
        return true;
    }

    return false;
  }

  //==========================================================================
  /**
   * Devuelve true si idglHijo es hijo jerï¿½rquico para idglFuente.
   */
  boolean esHijo (IDGL idglHijo,IDGL idglFuente)
  {
   if ((idglHijo==null)||(idglFuente==null))
     return false;

    //Comprobar si estamos en la cache para ese idglFuente....
    if(this.cacheHijos.containsKey(idglFuente))
    {
      TreeMap treemap = (TreeMap) this.cacheHijos.get(idglFuente);
      if (treemap.containsKey(idglHijo))
        return true;
    }

    else
    {
      //Comprobar obteniendo los hijos para ese idglFuente..
      TreeMap treemap = (TreeMap) this.getCGHijos(idglFuente);
      if (treemap.containsKey(idglHijo))
        return true;
    }

   return false;
  }



  //==========================================================================
  /**
   * Estado NULO de la mï¿½quina de estados CGL.<br>
   */
  private void maquinaEstadoCGL_NULO()
  {
    //
    // Si hay Datos en el vector, quitarlos.
    //

    if (socketPTMFImp.getVectorRegistroCGL().size() != 0)
    {
      this.socketPTMFImp.getVectorRegistroCGL().clear();
    }

    this.socketPTMFImp.getTemporizador().sleep(1000);
  }

  //==========================================================================
  /**
   * Estado MIEMBRO_GL de la mï¿½quina de estados CGL.<br>
   * Mensajes permitidos:<p>
   * <table>
   * <tr><td> <b>Enviar</b> </td>
   *      <td> <UL>
   *           <IL> GRUPO_LOCAL
   *           <IL> SOCKET_ACEPTADO_EN_GRUPO_LOCAL
   *           </UL>
   *      </td></tr>
   * <tr><td> <b>Recibir SOLO</b></td>
   *      <td> <UL>
   *           <IL> GRUPO_LOCAL
   *           <IL> SOCKET_ACEPTADO_EN_GRUPO_LOCAL
   *           <IL> DEJAR_GRUPO_LOCAL
   *           <IL> ELIMINACION_GRUPO_LOCAL
   *           <IL> BUSCAR_GRUPO_LOCAL
   *           <IL> BUSCAR_GRUPO_LOCAL_VECINO
   *           <IL> UNIRSE_A_GRUPO_LOCAL
   *           </UL>
   *      </td></tr>
   * </table>
   */
  private void maquinaEstadoCGL_MIEMBRO_GL()
  {

    //Notificaciones...
    this.notificacionesTemporizadores();

    //
    // Obtener registros CGLs.
    //
    if (getRegistroCGL())
    {


      switch(tpduCGL.getSUBTIPO())
      {
        //
        // TPDU  <--  TPDU_CGL_GRUPO_LOCAL
        //
       case PTMF.TPDU_CGL_GRUPO_LOCAL:
        procesarTPDU_CGL_GRUPO_LOCAL();
        break;

        //
        // TPDU  <--  TPDU_CGL_GRUPO_LOCAL_VECINO
        //
       case PTMF.TPDU_CGL_GRUPO_LOCAL_VECINO:
        procesarTPDU_CGL_GRUPO_LOCAL_VECINO();
        break;


        //
        // TPDU  <--  TPDU_CGL_ELIMINACION_GRUPO_LOCAL
        //
       case PTMF.TPDU_CGL_ELIMINACION_GRUPO_LOCAL:
        procesarTPDU_CGL_ELIMINACION_GRUPO_LOCAL();
        break;

        //
        // TPDU  <--  TPDU_CGL_SOCKET_ACEPTADO_EN_GRUPO_LOCAL
        //
       case PTMF.TPDU_CGL_SOCKET_ACEPTADO_EN_GRUPO_LOCAL:
        procesarTPDU_CGL_SOCKET_ACEPTADO_EN_GRUPO_LOCAL();
        break;

        //
        // TPDU  <--  TPDU_CGL_DEJAR_GRUPO_LOCAL
        //
       case PTMF.TPDU_CGL_DEJAR_GRUPO_LOCAL:
          procesarTPDU_CGL_DEJAR_GRUPO_LOCAL();
        break;

        //
        // TPDU  <--  TPDU_CGL_BUSCAR_GRUPO_LOCAL
        //
       case PTMF.TPDU_CGL_BUSCAR_GRUPO_LOCAL:
          procesarTPDU_CGL_BUSCAR_GRUPO_LOCAL();
        break;

        //
        // TPDU  <--  TPDU_CGL_BUSCAR_GRUPO_LOCAL_VECINO
        //
       case PTMF.TPDU_CGL_BUSCAR_GRUPO_LOCAL_VECINO:
          procesarTPDU_CGL_BUSCAR_GRUPO_LOCAL_VECINO();
        break;

        //
        // TPDU  <--  TPDU_CGL_UNIRSE_A_GRUPO_LOCAL
        //
       case PTMF.TPDU_CGL_UNIRSE_A_GRUPO_LOCAL:
          procesarTPDU_CGL_UNIRSE_A_GRUPO_LOCAL();
        break;


       default:
          break;
      }

    }
  }

  //==========================================================================
  /**
   * Estado BUSCAR_GL de la mï¿½quina de estados CGL.<br>
   * Mensajes permitidos:<p>
   * <table>
   * <tr><td> <b>Enviar</b> </td>
   *      <td> <UL>
   *           <IL> BUSCAR_GRUPO_LOCAL
   *           <IL> UNIRSE_A_GRUPO_LOCAL
   *           </UL>
   *      </td></tr>
   * <tr><td> <b>Recibir SOLO</b></td>
   *      <td> <UL>
   *           <IL> GRUPO_LOCAL
   *           </UL>
   *      </td></tr>
   * </table>
   */
  private void maquinaEstadoCGL_BUSCAR_GL()
  {
    //Notificaciones
    this.notificacionesTemporizadores();

    //
    // Obteber registros CGLs.
    //
    while (getRegistroCGL())
    {

      if(tpduCGL.getSUBTIPO()== PTMF.TPDU_CGL_GRUPO_LOCAL)
      {
        //
        // TPDU  <--  TPDU_CGL_GRUPO_LOCAL
        //
        procesarTPDU_CGL_GRUPO_LOCAL();
        return;
      }
    }

    while(bBuscarGL == false)
      return;

    //
    // Verificar Nï¿½ Mï¿½ximo de intentos
    //
    if (N_INTENTOS >= MAX_INTENTOS_BUSQUEDA_GRUPO_LOCAL)
    {
      setEstadoCGL(PTMF.ESTADO_CGL_CREAR_GL);
      return;
    }

    //
    // TPDU --> BUSCAR_GRUPO_LOCAL
    //
    enviarTPDU_CGL_BUSCAR_GRUPO_LOCAL();
    bBuscarGL = false;

    //
    // Incrementar el nï¿½mero de intentos de bï¿½squeda y esperar un poco.
    //
    N_INTENTOS++;

    //
    //Temporizador de espera...
    //
    this.socketPTMFImp.getTemporizador().registrarFuncion(T_ESPERA_BUSQUEDA_GL,timerHandler,EVENTO_BUSCAR_GL);
  }

  //==========================================================================
  /**
   * Estado ESPERAR_ACEPTACION_GL de la mï¿½quina de estados CGL.<br>
   * Mensajes permitidos:<p>
   * <table>
   * <tr><td> <b>Enviar</b> </td>
   *      <td> <UL>
   *           <IL> -
   *           </UL>
   *      </td></tr>
   * <tr><td> <b>Recibir SOLO</b></td>
   *      <td> <UL>
   *           <IL> SOCKET_ACEPTADO_EN_GRUPO_LOCAL
   *           </UL>
   *      </td></tr>
   * </table>
   */
  private void maquinaEstadoCGL_ESPERAR_ACEPTACION_GL()
  {
     //Notificaciones
    this.notificacionesTemporizadores();

    //
    // Obteber registro CGL.
    //
    while (getRegistroCGL())
    {

      if( tpduCGL.getSUBTIPO() == PTMF.TPDU_CGL_SOCKET_ACEPTADO_EN_GRUPO_LOCAL)
      {
        //
        // TPDU  <--  TPDU_CGL_SOCKET_ACEPTADO_EN_GRUPO_LOCAL
        //
        procesarTPDU_CGL_SOCKET_ACEPTADO_EN_GRUPO_LOCAL();
        return;
       }
    }
  }


  //==========================================================================
  /**
   * Estado CREAR_GL de la mï¿½quina de estados CGL.<br>
   * Mensajes permitidos:<p>
   * <table>
   * <tr><td> <b>Enviar</b> </td>
   *      <td> <UL>
   *           <IL> TPDU_CGL_GRUPO_LOCAL
   *           </UL>
   *      </td></tr>
   * <tr><td> <b>Recibir SOLO</b></td>
   *      <td> <UL>
   *           <IL> -
   *           </UL>
   *      </td></tr>
   * </table>
   */
  private void maquinaEstadoCGL_CREAR_GL()
  {
    final String mn = "CGLThread.maquinaEstadoCGL_CREAR_GL";

    Buffer   buf       = null;

      //
      // Limpiar vector de registros CGL.
      //
      this.socketPTMFImp.getVectorRegistroCGL().clear();


      //
      // CREAR:
      //  1ï¿½ IDGL PARA EL GRUPO LOCAL QUE VAMOS A CREAR...
      //  2ï¿½ Establecer nï¿½ mï¿½ximo de sockets en el grupo.
      //  3ï¿½ Crear vectores (IP, IDGLs, ...)
      //  4ï¿½ Aï¿½adirnos al treeMapId_Socket, lï¿½a lista de sockets del grupo
      try
      {
       buf = new Buffer(6);
       buf.addBytes( new Buffer(this.socketPTMFImp.getAddressLocal().getInetAddress().getAddress()),0,4);
       buf.addShort(this.socketPTMFImp.getCanalUnicast().getAddressUnicast().getPort(),4);

       this.idgl = new IDGL(buf,(byte)0);
       this.N_MAX_SOCKETS_GL = PTMF.MAX_SOCKETS_GL;
       this.N_SOCKETS = 1;

       //aï¿½adir idgl
       addIDGL(this.idgl,null);

       // Aï¿½adir este socket al vector de ID_SOCKETS del grupo.
       ID_Socket id = this.socketPTMFImp.getID_Socket();
       this.addID_Socket(id);
      }
      catch(ParametroInvalidoExcepcion e)
      {
        Log.log(mn,e.getMessage());
      }
      catch(PTMFExcepcion e)
      {
        Log.log(mn,e.getMessage());
      }


      //
      // TPDU --> PTMF.TPDU_CGL_GRUPO_LOCAL
      //
      enviarTPDU_CGL_GRUPO_LOCAL((byte)1);


      //Notificar creaciï¿½n...
      this.socketPTMFImp.sendPTMFEventConexion("CGL: Crear GL ->"+ this.idgl);


      //
      // CAMBIAR DE ESTADO --> BUSCAR_GL_VECINOS
      //
      this.setEstadoCGL(PTMF.ESTADO_CGL_BUSCAR_GL_VECINOS);
  }

  //==========================================================================
  /**
   * Estado BUSCAR_GL_VECINOS de la mï¿½quina de estados CGL.<br>
   * Mensajes permitidos:<p>
   * <table>
   * <tr><td> <b>Enviar</b> </td>
   *      <td> <UL>
   *           <IL> TPDU_CGL_BUSCAR_GRUPO_LOCAL
   *           </UL>
   *      </td></tr>
   * <tr><td> <b>Recibir SOLO</b></td>
   *      <td> <UL>
   *           <IL> TPDU_CGL_BUSCAR_GRUPO_LOCAL
               <IL> TPDU_CGL_BUSCAR_GRUPO_LOCAL_VECINO
   *           <IL> TPDU_CGL_GRUPO_LOCAL
   *           <IL> TPDU_CGL_DEJAR_GRUPO_LOCAL
   *           <IL> TPDU_CGL_ELIMINACION_GRUPO_LOCAL
   *           <IL> TPDU_CGL_SOCKET_ACEPTADO_EN_GRUPO_LOCAL
   *           <IL> TPDU_CGL_UNIRSE_A_GRUPO_LOCAL
   *           </UL>
   *      </td></tr>
   * </table>
   */
  private void maquinaEstadoCGL_BUSCAR_GL_VECINOS()
  {
    this.notificacionesTemporizadores();

    //
    // Obteber registros CGLs.
    //
    while (getRegistroCGL())
    {

      switch(tpduCGL.getSUBTIPO())
      {
        //
        // TPDU  <--  TPDU_CGL_BUSCAR_GRUPO_LOCAL
        //
       case PTMF.TPDU_CGL_BUSCAR_GRUPO_LOCAL:
        procesarTPDU_CGL_BUSCAR_GRUPO_LOCAL();
        break;

        //
        // TPDU  <--  TPDU_CGL_BUSCAR_GRUPO_LOCAL
        //
       case PTMF.TPDU_CGL_BUSCAR_GRUPO_LOCAL_VECINO:
        procesarTPDU_CGL_BUSCAR_GRUPO_LOCAL_VECINO();
        break;

        //
        // TPDU  <--  TPDU_CGL_GRUPO_LOCAL
        //
       case PTMF.TPDU_CGL_GRUPO_LOCAL:
        procesarTPDU_CGL_GRUPO_LOCAL();
        break;

        //
        // TPDU  <--  TPDU_CGL_GRUPO_LOCAL_VECINO
        //
       case PTMF.TPDU_CGL_GRUPO_LOCAL_VECINO:
        procesarTPDU_CGL_GRUPO_LOCAL_VECINO();
        break;

        //
        // TPDU  <--  TPDU_CGL_ELIMINACION_GRUPO_LOCAL
        //
       case PTMF.TPDU_CGL_ELIMINACION_GRUPO_LOCAL:
        procesarTPDU_CGL_ELIMINACION_GRUPO_LOCAL();
        break;

        //
        // TPDU  <--  TPDU_CGL_SOCKET_ACEPTADO_EN_GRUPO_LOCAL
        //
       case PTMF.TPDU_CGL_SOCKET_ACEPTADO_EN_GRUPO_LOCAL:
        procesarTPDU_CGL_SOCKET_ACEPTADO_EN_GRUPO_LOCAL();
        break;

        //
        // TPDU  <--  TPDU_CGL_DEJAR_GRUPO_LOCAL
        //
       case PTMF.TPDU_CGL_DEJAR_GRUPO_LOCAL:
          procesarTPDU_CGL_DEJAR_GRUPO_LOCAL();
        break;

        //
        // TPDU  <--  UNIRSE_A_GRUPO_LOCAL
        //
       case PTMF.TPDU_CGL_UNIRSE_A_GRUPO_LOCAL:
          procesarTPDU_CGL_UNIRSE_A_GRUPO_LOCAL();
        break;

       default:
          break;
      }

    }


    if(bBuscarGLVecino == false)
      return;

    //
    // CAMBIO A ESTADO CGL_MIEMBRO_GL SI:
    //  1. YA SE HA RECIBIDO ALGUN TPDU GRUPO_LOCAL
    //  2. SE HA EXCEDIDO EL Nï¿½ MAXIMO DE INTENTOS
    //
    if ( this.TTL_BUSCAR_GRUPO_LOCAL_VECINO > this.socketPTMFImp.getTTLSesion() /*|| this.treeMapIDGLPadresPotenciales.size()>0*/)
    {
      //
      // Cambiar de estado,
      // NOTA: NO SE MANDA UN MENSAJE GRUPO_LOCAL PORQUE
      // NO SE HA ENCONTRADO NINGï¿½N socket VECINO.
      //
      setEstadoCGL(PTMF.ESTADO_CGL_MIEMBRO_GL);
      return;
    }


    bBuscarGLVecino = false;
    //
    // TPDU --> BUSCAR_GRUPO_LOCAL_VECINO
    //
    enviarTPDU_CGL_BUSCAR_GRUPO_LOCAL_VECINO();


    //
    //Temporizador de espera ...
    //
    this.socketPTMFImp.getTemporizador().registrarFuncion(T_ESPERA_BUSQUEDA_GL_VECINO+(this.TTL*100),timerHandler,EVENTO_BUSCAR_GL_VECINO);
  }

  //==========================================================================
  /**
   * Estado DEJAR_GL de la mï¿½quina de estados CGL.<br>
   * Mensajes permitidos:<p>
   * <table>
   * <tr><td> <b>Enviar</b> </td>
   *      <td> <UL>
   *           <IL> DEJAR_GRUPO_LOCAL
   *           </UL>
   *      </td></tr>
   * <tr><td> <b>Recibir SOLO</b></td>
   *      <td> <UL>
   *           <IL> -
   *           </UL>
   *      </td></tr>
   * </table>
   */
  private void maquinaEstadoCGL_DEJAR_GL()
  {

   //
   // Decrementar el nï¿½mero de sockets en el GL...
   //
   this.N_SOCKETS--;

   //
   // TPDU --> DEJAR_GRUPO_LOCAL
   //
   enviarTPDU_CGL_DEJAR_GRUPO_LOCAL(this.socketPTMFImp.getID_Socket());

   if(this.N_SOCKETS == 0)
   {
     //
     // TPDU --> ELIMINACION_GRUPO_LOCAL
     //
     enviarTPDU_CGL_ELIMINACION_GRUPO_LOCAL();
   }

   //
   // Desactivar grupo Multicast
   this.socketPTMFImp.setGrupoMcastActivo(false);

   //
   // CAMBIAR DE ESTADO --> NULO
   //
   this.setEstadoCGL(PTMF.ESTADO_CGL_NULO);

  }

  //==========================================================================
  /**
   * Estado MONITOR de la mï¿½quina de estados CGL.<br>
   * En este estado el socket se encarga de monotorizar los mensajes CGL
   * y de imprimirlos.
   */
  private void maquinaEstadoCGL_MONITOR()
  {

    //Log.debug(Log.CGL,"CGL ","MAQUINA ESTADO CGL MONITOR");
    //
    // Obteber registro CGL.
    //
    while (getRegistroCGL())
    {
      Log.log("TPDUCGL "+src.toString(),"\n"+tpduCGL);
    }


  }


  //==========================================================================
  /**
   * Este mï¿½todo sincronizado establece el valor del atributo estadoCGL.<br>
   * @param estadoCGL Nuevo estado de la mï¿½quina de estados CGL.
   */
  synchronized void setEstadoCGL (int estadoCGL)
  {
     String mn = "CGLThread.setEstadoCGL";
     //
     // CAMBIAR AQUï¿½ LOS ESTADOS DE LAS VARIABLES DEPENDIENTES DEL ESTADO DE LA
     // Mï¿½QUINA.
     //
     if(estadoCGL == this.estadoCGL)
      return;

     switch(estadoCGL)
     {
        case PTMF.ESTADO_CGL_BUSCAR_GL:
            this.TTL = 0;
            this.N_INTENTOS = 0;
            this.estadoCGL = estadoCGL;
            this.bBuscarGL = true;
            this.bBuscarGLVecino = false;
            this.socketPTMFImp.setGrupoMcastActivo(false);
            Log.debug(Log.CGL,mn,"Estado CGL: PTMF.ESTADO_CGL_BUSCAR_GL");
             //Log.log("CGLThread","Estado CGL: PTMF.ESTADO_CGL_BUSCAR_GL");
             this.socketPTMFImp.sendPTMFEventConexion("CGL: Buscar Grupos Locales ...");
            break;
        case PTMF.ESTADO_CGL_ESPERAR_ACEPTACION_GL:
            this.N_INTENTOS=0;
            this.socketPTMFImp.setGrupoMcastActivo(false);
            this.estadoCGL = estadoCGL;
            Log.debug(Log.CGL,mn,"Estado CGL: PTMF.ESTADO_CGL_ESPERAR_ACEPTACION_GL");
            //Log.log("CGLThread","Estado CGL: PTMF.ESTADO_CGL_ESPERAR_ACEPTACION_GL");

            this.socketPTMFImp.sendPTMFEventConexion("CGL: Esperar aceptaciï¿½n en Grupo Local: "+ this.idgl);


            break;

        case PTMF.ESTADO_CGL_MIEMBRO_GL:
            this.estadoCGL = estadoCGL;
            this.bNotificarGL = false;
            this.bAceptarSocket = false;
            this.bNotificarGLVecinos = false;

            //
            //Activar grupo Multicast
            this.socketPTMFImp.setGrupoMcastActivo(true);

            Log.debug(Log.CGL,mn,"Estado CGL: PTMF.ESTADO_MIEMBRO_GL");
            Log.debug(Log.CGL,mn,"IDGL: " +  this.idgl);
            this.socketPTMFImp.sendPTMFEventConexion("CGL: Miembro Grupo Local: "+  this.idgl);

            //
            // LIBERAR THREAD DE LA APLICACIï¿½Nï¿½ï¿½ï¿½ï¿½ï¿½ï¿½
            //
            this.socketPTMFImp.getSemaforoAplicacion().up();

            break;

        case PTMF.ESTADO_CGL_CREAR_GL:
            this.estadoCGL = estadoCGL;
            this.socketPTMFImp.setGrupoMcastActivo(false);
            Log.debug(Log.CGL,mn,"Estado CGL: PTMF.ESTADO_CGL_CREAR_GL");
            //Log.log("CGLThread","Estado CGL: PTMF.ESTADO_CGL_CREAR_GL");


            break;

        case PTMF.ESTADO_CGL_BUSCAR_GL_VECINOS:
            this.estadoCGL = estadoCGL;
            this.TTL = 0;
            this.bBuscarGLVecino = true;
            Log.debug(Log.CGL,mn,"Estado CGL: PTMF.ESTADO_CGL_BUSCAR_GL_VECINOS");
            //Log.log("CGLThread","Estado CGL: PTMF.ESTADO_CGL_BUSCAR_GL_VECINOS");

            this.socketPTMFImp.sendPTMFEventConexion("CGL: Buscar Grupos Locales Vecinos ...");


            break;

        case PTMF.ESTADO_CGL_MONITOR:
            this.estadoCGL = estadoCGL;
            this.socketPTMFImp.setGrupoMcastActivo(false);
            Log.debug(Log.CGL,mn,"Estado CGL: PTMF.ESTADO_CGL_MONITOR");
            break;

        case PTMF.ESTADO_CGL_NULO:


            //
            // Limpiar variables a valores por defectos.
            //
            this.treeMapID_Socket = null;
            this.treeMapIDGLs = null;
            this.idgl = null;
            this.N_SOCKETS=0;
            this.N_MAX_SOCKETS_GL=0;
            this.TTL = 0;
            this.N_INTENTOS = 0;
            this.estadoCGL = estadoCGL;
            this.socketPTMFImp.setGrupoMcastActivo(false);

            //
            // LIBERAR THREAD DE LA APLICACIï¿½Nï¿½ï¿½ï¿½ï¿½ï¿½ï¿½
            //
            this.socketPTMFImp.getSemaforoAplicacion().up();


            Log.debug(Log.CGL,mn,"Estado CGL: PTMF.ESTADO_CGL_NULO");
            //Log.log("CGLThread","Estado CGL: PTMF.ESTADO_CGL_NULO");

            break;

        case PTMF.ESTADO_CGL_DEJAR_GL :
           this.estadoCGL = estadoCGL;
           Log.debug(Log.CGL,mn,"Estado CGL: PTMF.ESTADO_CGL_DEJAR_GL");
           //Log.log("CGLThread","Estado CGL: PTMF.ESTADO_CGL_DEJAR_GL");
            this.socketPTMFImp.sendPTMFEventConexion("CGL: Dejar Grupo Local ...");

           break;
        default:

          Log.log("ERROR FATAL (CGL)","ESTADO CGL INCORRECTO");
          Log.exit(-1);
     }
  }

  //==========================================================================
  /**
   * Este mï¿½todo sincronizado devuelve el valor del atributo estadoCGL.<br>
   * @return el estado de la mï¿½quina de estados CGL.
   */
  synchronized int getEstadoCGL ()
  {
   return estadoCGL;
  }

  //==========================================================================
  /**
   * Este mï¿½todo devuelve el IDGL de este socket.
   * @return Objeto IDGL
   */
  IDGL getIDGL ()
  {
   return this.idgl;
  }



  //==========================================================================
  /**
   * Este mï¿½todo obtiene un registro CGL del vector vectorRegistroCGL
   *  de la clase SocketPTMF, LOS DATOS DEL REGISTRO SON ALMACENADOS
   * EN LAS VARIABLES tpduCGL y src DE LA CLASE.
   * @return true si se obtuvo un registro CGL y false en caso contrario.
   */
  private boolean getRegistroCGL()
  {
    final String mn = "CGLThread.getRegistroCGL(TPDUCGL,Address)";
    RegistroCGL registroCGL = null;

    //
    // Obtener el registro CGL del vectorCGL de la clase.
    //

    if (socketPTMFImp.getVectorRegistroCGL().size() != 0)
    {
      registroCGL = (RegistroCGL) this.socketPTMFImp.getVectorRegistroCGL().remove(0);

      tpduCGL = registroCGL.tpduCGL;
      src = registroCGL.src;

      //COMPRABAR QUE EL PAQUETE NO ES ENVIADO POR NOSOTROS
      // OJO, Comprueba con una direciï¿½n local, pueden surgir problemas
      // si tenems varias interfaces.
      if( (this.getEstadoCGL()!= PTMF.ESTADO_CGL_MONITOR)
              &&
          (this.socketPTMFImp.getAddressLocal().getInetAddress().equals(src.getInetAddress()))
              && (tpduCGL.getPuertoUnicast()==this.socketPTMFImp.getAddressLocal().getPort()))
      {
      Log.debug(Log.TPDU_CGL,mn,"!cgl ");
        return false;
      }


      //Log.debug(Log.TPDU_CGL,mn,"Registro CGL Recibido:\nFuente-->"+src.toString()+"\nTPDU-->"+tpduCGL.toString());
      return true;
    }
    else
     return false;
  }


  //==========================================================================
  /**
   * procesarTPDU_CGL_SOCKET_ACEPTADO_EN_GRUPO_LOCAL. Procesa una cabecera de un TPDU
   *  SOCKET_ACEPTADO_EN_GRUPO_LOCAL
   */
  private void procesarTPDU_CGL_SOCKET_ACEPTADO_EN_GRUPO_LOCAL()
  {

    String mn = "CGLThread.procesarTPDU_CGL_SOCKET_ACEPTADO_EN_GRUPO_LOCAL";

    Log.debug(Log.CGL,mn,"TPDUCGL <-- SOCKET_ACEPTADO_EN_GRUPO_LOCAL "+tpduCGL.getIDGL() );

    if (! tpduCGL.getIDGL().equals(this.idgl))
        return;

    if(getEstadoCGL() == PTMF.ESTADO_CGL_ESPERAR_ACEPTACION_GL)
    {
        if (tpduCGL.getID_SOCKET() == null || !tpduCGL.getID_SOCKET().equals(this.socketPTMFImp.getID_Socket()))
          return;

        //Log.log("ID_SOCKET de ACEPTADO EN GRUPO LOCAL",""+tpduCGL.getID_SOCKET());
        //Log.log("ID_SOCKET LOCAL",""+this.socketPTMFImp.getID_Socket());

        if(!tpduCGL.getID_SOCKET().equals(this.socketPTMFImp.getID_Socket()))
          return;

        //1. Cancelar temporizador de bï¿½squeda de grupo local y time out
        //2.  y el flag correspondiente de notificaciï¿½n,
        //3.  y quitar el ID_Socket de la cola de aceptaciï¿½n
        this.socketPTMFImp.getTemporizador().cancelarFuncion(this.timerHandler,EVENTO_BUSCAR_GL);
        this.socketPTMFImp.getTemporizador().cancelarFuncion(this.timerHandler,EVENTO_TIME_OUT);

        this.bAceptarSocket= false;
        this.colaAceptacionID_SOCKET.remove(tpduCGL.getID_SOCKET());

        this.addID_Socket(tpduCGL.getID_SOCKET());

        //Log.log("1. tREEMAP id_sOCKETS "+tpduCGL.getTreeMapID_SOCKET() ,"");

        if (tpduCGL.getTreeMapID_SOCKET() != null)
        {
             this.addID_Socket(tpduCGL.getTreeMapID_SOCKET());
        }
        this.N_SOCKETS = tpduCGL.getN_SOCKETS();

        //Log.log("2. tREEMAP id_sOCKETS "+this.treeMapID_Socket,"");

        //Socket aceptado
        this.socketPTMFImp.sendPTMFEventConexion("CGL: SocketPTMF aceptado en Grupo Local: "+ this.idgl);


        //
        // CAMBIAR DE ESTADO --> MIEMBRO_GRUPO_LOCAL
        //
        this.setEstadoCGL(PTMF.ESTADO_CGL_MIEMBRO_GL);
    }
    else //ESTADo --> MIEMBRO_GRUPO_LOCAL
    {
        if(tpduCGL.getN_IDS() < 1)
        {
          Log.log("CGLThread","Error en el TPDUCGL SOCKET_ACEPTADO_EN_GRUPO_LOCAL, se espera un N_IDS con valor 1.");
          return;
        }

        //
        // Incrementar nï¿½mero de sockets y almacenarlo
        //
        if (colaAceptacionID_SOCKET.contains(tpduCGL.getID_SOCKET()))
        {
            colaAceptacionID_SOCKET.remove(tpduCGL.getID_SOCKET());
            this.socketPTMFImp.getTemporizador().cancelarFuncion(this.timerHandler,EVENTO_ACEPTAR_SOCKET);
            bAceptarSocket = false;
        }


        if( ! this.treeMapID_Socket.containsKey(tpduCGL.getID_SOCKET()))
        {
          // SI NO TENEMOS EL NUEVO SOCKET EN EL TREEMAP...
          this.addID_Socket(tpduCGL.getID_SOCKET());

          if (tpduCGL.getTreeMapID_SOCKET() != null)
             this.addID_Socket(tpduCGL.getTreeMapID_SOCKET());
          this.N_SOCKETS ++;
        }
        else
        {
           // SI YA LO TENï¿½AMOS....
           if (tpduCGL.getTreeMapID_SOCKET() != null)
             this.addID_Socket(tpduCGL.getTreeMapID_SOCKET());
        }

        if (this.N_SOCKETS != tpduCGL.getN_SOCKETS())
        {
         Log.log("CGLThread","Nï¿½mero de sockets en el grupo inconsistente. Recibido: "+tpduCGL.getN_SOCKETS()+" Esperado: "+this.N_SOCKETS+" AJUSTADO.");
         this.N_SOCKETS = tpduCGL.getN_SOCKETS();
        }
    }
  }

  //==========================================================================
  /**
   * procesarTPDU_CGL_UNIRSE_A_GRUPO_LOCAL. Procesa una cabecera de un TPDU
   *  UNIRSE_A_GRUPO_LOCAL
   */

  private void procesarTPDU_CGL_UNIRSE_A_GRUPO_LOCAL()
  {
    String mn = "CGLThread.procesarTPDU_CGL_UNIRSE_A_GRUPO_LOCAL";
    Log.debug(Log.CGL,mn,"TPDUCGL <-- UNIRSE_A_GRUPO_LOCAL"+"\n"+tpduCGL.getIDGL());

    if (!tpduCGL.getIDGL().equals(this.idgl))
        return;

    if (!tpduCGL.getFlagIP() )
        return;

    if(tpduCGL.getN_IDS() > 1)
    {
          Log.log("CGLThread","Error en el TPDUCGL UNIRSE_A_GRUPO_LOCAL, se espera un N_IDS con valor 1.");
          return;
    }


    //Log.debug(Log.CGL,mn,"this.N_SOCKETS "+this.N_SOCKETS);
    //Log.debug(Log.CGL,mn,"this.MAX_SOCKETS_GL "+this.N_MAX_SOCKETS_GL);

    if (this.N_SOCKETS < this.N_MAX_SOCKETS_GL)
    {
     if (tpduCGL.getID_SOCKET() == null)
     {
      Log.log("CGLThread","TPDUCGL UNIRSE_A_GRUPO_LOCAL sin identificador ID_SOCKET. NO SE PUEDE PROCESAR.");
      return;
     }

     //Aï¿½adir el socket a la cola de aceptaciï¿½n y lanzar el temporizador....
     this.colaAceptacionID_SOCKET.add(tpduCGL.getID_SOCKET());

     this.socketPTMFImp.getTemporizador().registrarFuncion(
      (random.nextInt(this.T_RETRASO_NOTIFICACION_SOCKET_ACEPTADO -T_BASE)+T_BASE) % this.T_RETRASO_NOTIFICACION_SOCKET_ACEPTADO ,
        this.timerHandler,this.EVENTO_ACEPTAR_SOCKET );
    }


  }

  //==========================================================================
  /**
   * procesarTPDU_CGL_DEJAR_GRUPO_LOCAL. Procesa una cabecera de un TPDU
   *  DEJAR_GRUPO_LOCAL
   */

  private void procesarTPDU_CGL_DEJAR_GRUPO_LOCAL()
  {
        String mn = "CGLThread.procesarTPDU_CGL_DEJAR_GRUPO_LOCAL";

        Log.debug(Log.CGL,mn,"TPDUCGL <-- DEJAR_GRUPO_LOCAL "+tpduCGL.getIDGL());

        if (! tpduCGL.getIDGL().equals(this.idgl))
          return;

        if (!tpduCGL.getFlagIP())
           return;

        //Comprobar que no somos "nozotros"...
        if(tpduCGL.getID_SOCKET().equals(this.socketPTMFImp.getID_Socket()))
        {
           //
           // eeeh, QUE SOY YO...
           //

           //
           // TPDU --> GRUPO_LOCAL
           //
           enviarTPDU_CGL_GRUPO_LOCAL((byte)this.socketPTMFImp.getTTLSesion());
        }

        if(tpduCGL.getN_IDS() != 1)
        {
          Log.log("CGLThread","Error en el TPDUCGL DEJAR_GRUPO_LOCAL, se espera un N_IDS con valor 1.");
          return;
        }

        //
        // 1. Quitar el socket del grupo local
        this.removeID_SOCKET(tpduCGL.getID_SOCKET());

  }

  //==========================================================================
  /**
   * procesarTPDU_CGL_ELIMINACION_GRUPO_LOCAL. Procesa una cabecera de un TPDU
   *  ELIMINACION_GRUPO_LOCAL
   */

  private void procesarTPDU_CGL_ELIMINACION_GRUPO_LOCAL()
  {
        String mn = "CGLThread.procesarTPDU_CGL_ELIMINACION_GRUPO_LOCAL";

        Log.debug(Log.CGL,mn,"TPDUCGL <-- ELIMINACION_GRUPO_LOCAL"+"\n"+tpduCGL.getIDGL());

        if (! tpduCGL.getIDGL().equals(this.idgl))
        {
          //
          //Quitar idgl del grupo local eliminado
          //
          this.removeIDGL(tpduCGL.getIDGL());
          comprobarCache(tpduCGL.getIDGL());
        }
        else
        {

         //
         // eeeh, QUE Aï¿½N ESTOY YO...
         //

         //
         // TPDU --> GRUPO_LOCAL
         //
         enviarTPDU_CGL_GRUPO_LOCAL((byte)this.socketPTMFImp.getTTLSesion());
        }
  }



 //==========================================================================
  /**
   * procesarTPDU_CGL_BUSCAR_GRUPO_LOCAL_VECINO. Procesa una cabecera de un TPDU

   *  BUSCAR_GRUPO_LOCAL_VECINO
   */

  private void procesarTPDU_CGL_BUSCAR_GRUPO_LOCAL_VECINO()
  {
     String mn = "CGLThread.procesarTPDU_CGL_ELIMINACION_GRUPO_LOCAL";

     Log.debug(Log.CGL,mn,"TPDUCGL <-- BUSCAR_GRUPO_LOCAL_VECINO "+tpduCGL.getIDGL());
    //Log.log("CGLThread","TPDUCGL <-- BUSCAR_GRUPO_LOCAL_VECINO "+tpduCGL.getIDGL());

    if(!tpduCGL.getIDGL().equals(this.idgl))
    {
     //Almacenar el IDGL Vecino...
     addIDGL();

     //Recordar el IDGL (y el TTL especialmente)
     this.treeMapRespuestaGLVecinos.put(tpduCGL.getIDGL(),null);

     if (!bLanzadoTemporizadorNotificacionGLVecino)
     {
       //Lanzar this.socketPTMFImp.getTemporizador()...
       this.socketPTMFImp.getTemporizador().registrarFuncion( (random.nextInt(this.T_RETRASO_NOTIFICACION_GL_VECINO -T_BASE)+100) % this.T_RETRASO_NOTIFICACION_GL_VECINO ,
          this.timerHandler,this.EVENTO_NOTIFICAR_GL_VECINOS);
       bLanzadoTemporizadorNotificacionGLVecino = true;
     }

    }
  }

  //==========================================================================
  /**
   * procesarTPDU_CGL_BUSCAR_GRUPO_LOCAL. Procesa una cabecera de un TPDU
   *  BUSCAR_GRUPO_LOCAL
   */
  private void procesarTPDU_CGL_BUSCAR_GRUPO_LOCAL()
  {
     String mn = "CGLThread.procesarTPDU_CGL_BUSCAR_GRUPO_LOCAL";

     Log.debug(Log.CGL,mn,"TPDUCGL <-- BUSCAR_GRUPO_LOCAL "+tpduCGL.getIDGL());
     // Log.log("CGLThread","TPDUCGL <-- BUSCAR_GRUPO_LOCAL "+tpduCGL.getIDGL());

    //Log.log("this.N_SOCKETS "+this.N_SOCKETS,"");
    //Log.log("PTMF.MAX_SOCKETS_GL "+PTMF.MAX_SOCKETS_GL,"");

    if(this.N_SOCKETS < PTMF.MAX_SOCKETS_GL)
    {
      if(!bLanzadoTemporizadorNotificacionGL)
      {
         //Lanzar temporizador de notificaciï¿½n de GRUPO LOCAL
         // ï¿½ï¿½ SOLO SI HAY ESPACIO PARA OTRO ID_SOCKET, SINO NOï¿½ï¿½ï¿½
        this.socketPTMFImp.getTemporizador().registrarFuncion( (random.nextInt(this.T_RETRASO_NOTIFICACION_GL-T_BASE)+5000) % this.T_RETRASO_NOTIFICACION_GL ,
            this.timerHandler,this.EVENTO_NOTIFICAR_GL);

        bLanzadoTemporizadorNotificacionGL = true;
        this.TTL_Notificacion_GL = tpduCGL.getIDGL().TTL;
      }
      //Comprobar si el TTL sel mensaje es mayor que el actual
     else if(tpduCGL.getTTL() > this.TTL_Notificacion_GL)
     {
        this.TTL_Notificacion_GL = tpduCGL.getIDGL().TTL;
     }

    }
  }

  //==========================================================================
  /**

   * procesarTPDU_CGL_BUSCAR_GL_PARA_EMISOR. Procesa una cabecera de un TPDU
   *  BUSCAR_GL_PARA_EMISOR
   */
  private void procesarTPDU_CGL_BUSCAR_GL_PARA_EMISOR()
  {
     String mn = "CGLThread.procesarTPDU_CGL_BUSCAR_GL_PARA_EMISOR";

     Log.debug(Log.CGL,mn,"TPDUCGL <-- BUSCAR_GL_PARA_EMISOR "+tpduCGL.getIDGL());

    if ((tpduCGL.getTreeMapIDGL() == null) || (tpduCGL.getTreeMapIDGL().size() <= 0))
      return;

    //Si no es de nuestro Grupo Local este socket...
    if(!tpduCGL.getIDGL().equals(this.idgl))
    {
      //Ver si nosotros somos CG para ese IDGL Emisor...
      if (this.esHijo((IDGL)tpduCGL.getIDGL_EMISOR(),this.idgl))
      {
        //Almacenar el IDGL_EMISOR del emisor para el que se busca "Padre" en la lista de respuesta....
        this.treeMapRespuestaBusquedaEmisores.put( tpduCGL.getIDGL_EMISOR(),null);

        //Lanzar temporizador de notificaciï¿½n de GRUPO LOCAL....
        this.socketPTMFImp.getTemporizador().registrarFuncion( (random.nextInt(this.T_RETRASO_NOTIFICACION_GL_PARA_EMISOR-T_BASE)+T_BASE) % this.T_RETRASO_NOTIFICACION_GL_PARA_EMISOR ,
          this.timerHandler,this.EVENTO_NOTIFICAR_GL_EMISOR);
      }
    }
  }


   //==========================================================================
  /**
   * procesarTPDU_CGL_GL_PARA_EMISOR. Procesa una cabecera de un TPDU
   *  GL_PARA_EMISOR
   */
  private void procesarTPDU_CGL_GL_PARA_EMISOR()
  {
    final String mn = "DatosThread.procesarTPDUCGL_GL_PARA_EMISOR()";
    Log.debug(Log.CGL,mn,"TPDUCGL <-- GL_PARA_EMISOR "+tpduCGL.getIDGL());

    if ((tpduCGL.getTreeMapIDGL() == null) || (tpduCGL.getTreeMapIDGL().size() <= 0))
      return;


    if((!tpduCGL.getIDGL().equals(this.idgl)) && (this.treeMapBusquedaEmisores.containsKey(tpduCGL.getIDGL_EMISOR())))
    {
      // eliminar IDGL ....
      this.treeMapBusquedaEmisores.remove(tpduCGL.getIDGL_EMISOR());

      //Guardar IDGL...
      this.addIDGL();

      //Despertar DatosThread...
      //Log.log (mn,"POR LO MENOS ME HAN DESPERTADO.");
      this.semaforoDatosThread.up();

      if (this.treeMapBusquedaEmisores.size() <= 0)
      {
        //Cancelar temporizador de busqueda....
        this.socketPTMFImp.getTemporizador().cancelarFuncion(this.timerHandler,this.EVENTO_BUSCAR_GL_EMISOR);
      }
    }

    if(tpduCGL.getIDGL().equals(this.idgl))
    {
      //Otro socket ha notificado el mensaje....

      //Cancelar temporizador de notificaciï¿½n....
      this.socketPTMFImp.getTemporizador().cancelarFuncion(this.timerHandler,this.EVENTO_NOTIFICAR_GL_EMISOR);

      //Si la respuesta se ha dado para un IDGL del que tambiï¿½n nosotros debemos
      // dar respuesta, eliminar nuestra respuesta....
      this.treeMapRespuestaBusquedaEmisores.remove( tpduCGL.getIDGL_EMISOR());
    }

  }



  //==========================================================================
  /**
   * ProcesarTPDU_CGL_GRUPO_LOCAL. Procesa una cabecera de un TPDU
   *  GRUPO_LOCAL.
   */
  private void procesarTPDU_CGL_GRUPO_LOCAL()
  {
      String mn = "CGLThread.procesarTPDU_CGL_GRUPO_LOCAL";

     Log.debug(Log.CGL,mn,"TPDUCGL <-- GRUPO_LOCAL"+"\n"+tpduCGL.getIDGL());
     Log.debug(Log.CGL,mn,"N_Sockets: "+tpduCGL.getN_SOCKETS());
     Log.debug(Log.CGL,mn,"N_Max_Sockets: "+tpduCGL.getN_MAX_SOCKETS());
   
     //Log.log("CGLThread","TPDUCGL <-- GRUPO_LOCAL: "+tpduCGL.getIDGL());

     //DEPURACION
     if(tpduCGL.getTreeMapIDGL()!= null && tpduCGL.getTreeMapIDGL().size() > 0)
      {
        Iterator iterator = tpduCGL.getTreeMapIDGL().keySet().iterator();
        while(iterator.hasNext())
        {
          IDGL idgl = (IDGL) iterator.next();
          Log.debug(Log.CGL,mn,"IDGL <--- "+idgl);
        }
      }

     if(this.getEstadoCGL() == PTMF.ESTADO_CGL_BUSCAR_GL)
     {
        //Almacenar el IDGL y la lista...
        addIDGL();

        //Comprobar que nos podemos unir al grupo...
        if (tpduCGL.getN_SOCKETS() >= tpduCGL.getN_MAX_SOCKETS())
          return;

        //
        // Almacenar datos del GL
        //
        this.idgl = tpduCGL.getIDGL();
        this.N_MAX_SOCKETS_GL = tpduCGL.getN_MAX_SOCKETS();
        this.N_SOCKETS = tpduCGL.getN_SOCKETS();


        //
        // TPDU --> UNIRSE_A_GRUPO_LOCAL
        //
        enviarTPDU_CGL_UNIRSE_A_GRUPO_LOCAL();

        //
        // CAMBIAR DE ESTADO --> ESPERAR_ACEPTACION
        //
        this.setEstadoCGL(PTMF.ESTADO_CGL_ESPERAR_ACEPTACION_GL);

        //
        // Lanzar temporizador time out.....
        this.socketPTMFImp.getTemporizador().registrarFuncion(T_ESPERA_ACEPTACION_EN_GRUPO_LOCAL,timerHandler,EVENTO_TIME_OUT);


     }
     else if(this.getEstadoCGL() == PTMF.ESTADO_CGL_MIEMBRO_GL)// ESTADO MIEMBRO_GL
     {
        if(tpduCGL.getIDGL().equals(this.idgl))
        {
            // Aqui se entra si el emisor del TPDU es de nuestro GRUPO_LOCAL

            //
            // Si hemos recibido una notificaciï¿½n de nuestro grupo local
            // significa:

            // 1ï¿½ --> que otro socket ha lanzado el TPDU GRUPO_LOCAL
            //        ante la recepciï¿½n de un mensaje BUSCAR_GRUPO_LOCAL


            this.socketPTMFImp.getTemporizador().cancelarFuncion(this.timerHandler,this.EVENTO_NOTIFICAR_GL);
            this.bNotificarGL= false;

            //Eliminar el IDGL....
            this.treeMapRespuestaGLVecinos.remove(tpduCGL.getIDGL());
        }
        else
        {
           //1. Cancelar temporizador de bï¿½squeda de grupo local
           //2.  y el flag correspondiente de notificaciï¿½n,
           this.socketPTMFImp.getTemporizador().cancelarFuncion(this.timerHandler,EVENTO_BUSCAR_GL);

           //Eliminar el IDGL.... (AQUï¿½ por si acaso)
           this.treeMapRespuestaGLVecinos.remove(tpduCGL.getIDGL());

           //Almacenar el IDGL Padre Potencial
           addIDGL();

        }
     }
  }

 //==========================================================================
  /**
   * ProcesarTPDU_CGL_GRUPO_LOCAL_VECINO. Procesa una cabecera de un TPDU
   *  GRUPO_LOCAL_VECINO.
   */
  private void procesarTPDU_CGL_GRUPO_LOCAL_VECINO()
  {
     String mn = "CGLThread.procesarTPDU_CGL_GRUPO_LOCAL_VECINO";

     Log.debug(Log.CGL,mn,"TPDUCGL <-- GRUPO_LOCAL"+"\n"+tpduCGL.getIDGL());
     //Log.log("CGLThread","TPDUCGL <-- GRUPO_LOCAL_VECINO: "+tpduCGL.getIDGL());

     //DEPURACION
     if(tpduCGL.getIDGL_EMISOR()!=null)
     {
          Log.debug(Log.CGL,mn,"IDGL_EMISOR <--- "+tpduCGL.getIDGL_EMISOR());
     }
     if(tpduCGL.getTreeMapIDGL()!= null && tpduCGL.getTreeMapIDGL().size() > 0)
     {
        Iterator iterator = tpduCGL.getTreeMapIDGL().keySet().iterator();

        while(iterator.hasNext())
        {
          IDGL idgl = (IDGL) iterator.next();
          Log.debug(Log.CGL,mn,"IDGL <--- "+idgl);
        }
     }
     //FIN_DEPURACION

     this.socketPTMFImp.sendPTMFEventConexion("CGL: GL Vecino: "+ tpduCGL.getIDGL());

     if(this.getEstadoCGL() == PTMF.ESTADO_CGL_BUSCAR_GL_VECINOS)
     {
        //Comprobar si es para nosotros...
        if(this.idgl != null && tpduCGL.getIDGL_EMISOR().equals(this.idgl))
        {
           //
           // Cancelar Temporizador
           //
           this.socketPTMFImp.getTemporizador().cancelarFuncion(this.timerHandler,this.EVENTO_BUSCAR_GL_VECINO);

           //Almacenar el IDGL y la lista de IDGLs...
           addIDGL();

           //
           // CAMBIAR DE ESTADO --> MIEMBRO_GRUPO_LOCAL
           //
           this.setEstadoCGL(PTMF.ESTADO_CGL_MIEMBRO_GL);
        }
        else
        {
           //Almacenar el IDGL y la lista de IDGLs...
           addIDGL();
        }
     }
     else // ESTADO MIEMBRO_GL
     {
        if(tpduCGL.getIDGL().equals(this.idgl))
        {
            // Aqui se entra si el emisor del TPDU es de nuestro GRUPO_LOCAL

            //
            // Si hemos recibido una notificaciï¿½n de nuestro grupo local
            // significa:

            // 1ï¿½ --> que otro socket ha lanzado el TPDU GRUPO_LOCAL_VECINO
            //        ante la recepciï¿½n de un mensaje BUSCAR_GRUPO_LOCAL_VECINO

            this.socketPTMFImp.getTemporizador().cancelarFuncion(this.timerHandler,this.EVENTO_NOTIFICAR_GL_VECINOS);
            this.bNotificarGL= false;

            //Eliminar el IDGL....
            this.treeMapRespuestaGLVecinos.remove(tpduCGL.getIDGL());
        }
        else
        {
           //1. Cancelar temporizador de bï¿½squeda de grupo local
           //2.  y el flag correspondiente de notificaciï¿½n,
           //this.socketPTMFImp.getTemporizador().cancelarFuncion(this.timerHandler,EVENTO_BUSCAR_GL_VECINOS);

           //Eliminar el IDGL.... (AQUï¿½ por si acaso)
           //this.treeMapRespuestaGLVecinos.remove(tpduCGL.getIDGL());

           //Almacenar el IDGL_EMISOR como alcanzable por el IDGL Fuente...
           addIDGL(tpduCGL.getIDGL_EMISOR());

           //Almacenar toda la lista de IDGLs...
           addIDGL();
        }
     }
  }

  //==========================================================================
  /**
   * Envï¿½a una TPDUCGL BUSCAR_GRUPO_LOCAL_VECINO
   */
  private void enviarTPDU_CGL_BUSCAR_GRUPO_LOCAL_VECINO()
  {
    Buffer   buf       = null;
    String mn = "CLThread.enviarTPDU_CGL_BUSCAR_GRUPO_LOCAL_VECINO";


    //
    // TPDU --> BUSCAR_GRUPO_LOCAL_VECINO
    //

    try
    {

      tpduCGL =  TPDUCGL.crearTPDUCGL
      (/*SocketPTMFImp*/      socketPTMFImp ,
       /*byte SUBTIPO*/     (byte)PTMF.TPDU_CGL_BUSCAR_GRUPO_LOCAL_VECINO ,
       /*byte TTL */        (byte)this.TTL_BUSCAR_GRUPO_LOCAL_VECINO ,
       /*short METRICA*/    (short)0 ,
       /*boolean flagIP*/   false ,
       /*N_MAX_SOCKETS*/    this.N_MAX_SOCKETS_GL,
       /*N_SOCKETS*/        this.N_SOCKETS,
       /*IDGL_EMISOR*/      null,
       /*treeMapIDGL */     (TreeMap)null,
       /*treeMapID_SOCKET */(TreeMap)null ,
       /*ID_SOCKET */       (ID_Socket)null
       );



      buf = tpduCGL.construirTPDU();

      //Enviar el buffer....
      send(buf,this.TTL_BUSCAR_GRUPO_LOCAL_VECINO/*TTL*/);

      Log.debug(Log.CGL,mn,"TPDUCGL --> BUSCAR_GRUPO_LOCAL_VECINO  TTL:"+tpduCGL.getTTL());
      //Log.log("CGLThread","TPDUCGL --> BUSCAR_GRUPO_LOCAL_VECINO  TTL:"+tpduCGL.getTTL());

      //
      // Incrementar TTL +2.
      // Mï¿½trica = 0 [Bï¿½squeda del grupo local en la subred --> TTL =1]      //
      //

      //this.TTL = (byte)this.socketPTMFImp.getTTLSesion();
      if(this.TTL_BUSCAR_GRUPO_LOCAL_VECINO<2)
        this.TTL_BUSCAR_GRUPO_LOCAL_VECINO ++;
      else
        this.TTL_BUSCAR_GRUPO_LOCAL_VECINO += 2;

      //if (this.TTL_BUSCAR_GRUPO_LOCAL_VECINO > this.socketPTMFImp.getTTLSesion())
      //{
      //  this.TTL_BUSCAR_GRUPO_LOCAL_VECINO = this.socketPTMFImp.getTTLSesion();
      //}

    }
    catch (ParametroInvalidoExcepcion e){this.error(e);}
    catch (PTMFExcepcion e){this.error(e);}
    catch (IOException e) {this.error(e);}

  }




 //==========================================================================
  /**
   * Envï¿½a una TPDUCGL UNIRSE_A_GRUPO_LOCAL
   */
  private void enviarTPDU_CGL_UNIRSE_A_GRUPO_LOCAL()
  {
    Buffer   buf       = null;
    String mn = "CLThread.enviarTPDU_CGL_UNIRSE_A_GRUPO_LOCAL";

    Log.debug(Log.CGL,mn,"TPDUCGL --> UNIRSE_A_GRUPO_LOCAL"+"\n"+tpduCGL.getIDGL());

    //
    // TPDU --> UNIRSE_A_GRUPO_LOCAL
    //
      try
      {
        tpduCGL =  TPDUCGL.crearTPDUCGL
        (/*SocketPTMFImp*/      this.socketPTMFImp ,
         /*byte SUBTIPO*/     (byte)PTMF.TPDU_CGL_UNIRSE_A_GRUPO_LOCAL,
         /*byte TTL */        (byte)1 ,
         /*short METRICA*/    (short)0 ,
         /*boolean flagIP*/   true ,
         /*N_MAX_SOCKETS*/    this.N_MAX_SOCKETS_GL,
         /*N_SOCKETS*/        1, // 1 ID_SOCKET, el de este socket que se une al grupo.
         /*treeMapIDGL */     (TreeMap)null,
         /*treeMapID_SOCKET */(TreeMap)null ,
         /*ID_SOCKET */
          // Poner ID_SOCKET de este socket
          this.socketPTMFImp.getID_Socket()
         );


          buf = tpduCGL.construirTPDU();

          //Enviar el buffer....
          send(buf,1/*TTL*/);

        }
        catch (ParametroInvalidoExcepcion e){this.error(e);}
        catch (PTMFExcepcion e){this.error(e);}
        catch (IOException e) {this.error(e);  }

  }



 //==========================================================================
  /**
   * Envï¿½a una TPDUCGL BUSCAR_GL_PARA_EMISOR
   * @param idglEmisor IDGL del Emisor para el que buscamos CG "PADRE"
   */
  private void enviarTPDU_CGL_BUSCAR_GL_PARA_EMISOR(IDGL idglEmisor)
  {
    Buffer   buf       = null;
    String mn = "CLThread.enviarTPDU_CGL_BUSCAR_GL_PARA_EMISOR";

    Log.debug(Log.CGL,mn,"TPDUCGL --> BUSCAR_GL_PARA_EMISOR"+"\n"+tpduCGL.getIDGL());

    //
    // TPDU --> BUSCAR_GL_PARA_EMISOR
    //
      try
        {
            // Poner IDGL Emisor pasado por parï¿½metro...
            TreeMap treemap = new TreeMap();
            treemap.put(idglEmisor,null);

            tpduCGL =  TPDUCGL.crearTPDUCGL
            (/*SocketPTMFImp*/      this.socketPTMFImp ,
             /*byte SUBTIPO*/     (byte)PTMF.TPDU_CGL_BUSCAR_GL_PARA_EMISOR,
             /*byte TTL */
              //
               (byte)this.TTL_BUSCAR_GRUPO_LOCAL_VECINO ,
             /*short METRICA*/    (short)0 ,
             /*boolean flagIP*/   false ,
             /*N_MAX_SOCKETS*/    this.N_MAX_SOCKETS_GL,
             /*N_SOCKETS*/        this.N_SOCKETS,
             /*treeMapIDGL */     (TreeMap)treemap ,
             /*treeMapID_SOCKET */(TreeMap)null ,
             /*ID_SOCKET */       null
             );

            buf = tpduCGL.construirTPDU();

            //Enviar el buffer....
            send(buf,(byte)this.getTTLGLMaximo()/*TTL*/);

            if(this.TTL_BUSCAR_GRUPO_LOCAL_VECINO<2)
              this.TTL_BUSCAR_GRUPO_LOCAL_VECINO ++;
            else
              this.TTL_BUSCAR_GRUPO_LOCAL_VECINO += 2;

            if (this.TTL_BUSCAR_GRUPO_LOCAL_VECINO > this.socketPTMFImp.getTTLSesion())
            {
                this.TTL_BUSCAR_GRUPO_LOCAL_VECINO = this.socketPTMFImp.getTTLSesion();
            }
        }
        catch (ParametroInvalidoExcepcion e){this.error(e);}
        catch (PTMFExcepcion e){this.error(e);}
        catch (IOException e) {this.error(e);  }
  }


 //==========================================================================
  /**
   * Envï¿½a una TPDUCGL GL_PARA_EMISOR notificando el CG para el grupo del emisor
   * preguntado.
   * @param idglEmisor IDGL del emisor para el que se busca un CG "Padre".
   * @param treeMapIDGLs Controladores de grupo "Padre" utilizados cuando emite el IDGL emisor
   * @param TTL utilizado para enviar los datos.
   */
  private void enviarTPDU_CGL_GL_PARA_EMISOR(IDGL idglEmisor, TreeMap treeMapIDGLs, byte TTL)
  {
    Buffer buf = null;
    String mn = "CLThread.enviarTPDU_CGL_GL_PARA_EMISOR";

    Log.debug(Log.CGL,mn,"TPDUCGL --> GL_PARA_EMISOR"+"\n"+tpduCGL.getIDGL());
    //
    // TPDU -->GL_PARA_EMISOR
    //
      try
        {

          // Poner IDGL del Emisor 1ï¿½ y despuï¿½s el del CG PAdre.....
          TreeMap treemap  = new TreeMap();
          treemap.put(idglEmisor,null);
          treemap.putAll(treeMapIDGLs);

          TPDUCGL tpduCGL =  TPDUCGL.crearTPDUCGL
          (/*SocketPTMFImp*/      this.socketPTMFImp ,
           /*byte SUBTIPO*/     (byte)PTMF.TPDU_CGL_GL_PARA_EMISOR,
           /*byte TTL */        TTL,          // TTL pasado por parï¿½metros para concretizar la distancia..
           /*short METRICA*/    (short)0 ,
           /*boolean flagIP*/   false ,
           /*N_MAX_SOCKETS*/    this.N_MAX_SOCKETS_GL,
           /*N_SOCKETS*/        this.N_SOCKETS,
           /*treeMapIDGL */     (TreeMap)treemap,
           /*treeMapID_SOCKET */(TreeMap)null ,
           /*ID_SOCKET */       null
           );


          buf = tpduCGL.construirTPDU();

          // Enviar el buffer..
          send(buf,TTL);
        }
        catch (ParametroInvalidoExcepcion e){this.error(e);}
        catch (PTMFExcepcion e){this.error(e);}
        catch (IOException e) {this.error(e);  }
  }

  //==========================================================================
  /**
   * Envï¿½a una TPDUCGL SOCKET_ACEPTADO_EN_GRUPO_LOCAL
   */
  private void enviarTPDU_CGL_SOCKET_ACEPTADO_EN_GRUPO_LOCAL(ID_Socket id_socket)
  {
    Buffer   buf       = null;
    String mn = "CLThread.enviarTPDU_CGL_SOCKET_ACEPTADO_EN_GRUPO_LOCAL";

    Log.debug(Log.CGL,mn,"TPDUCGL --> SOCKET_ACEPTADO_EN_GRUPO_LOCAL"+"\n"+tpduCGL.getIDGL());
    //
    // TPDU --> SOCKET_ACEPTADO_EN_GRUPO_LOCAL
    //

      try
        {
          tpduCGL =  TPDUCGL.crearTPDUCGL
          (/*SocketPTMFImp*/      this.socketPTMFImp ,
           /*byte SUBTIPO*/     (byte)PTMF.TPDU_CGL_SOCKET_ACEPTADO_EN_GRUPO_LOCAL,
           /*byte TTL */        (byte)1,
           /*short METRICA*/    (short)0 ,
           /*boolean flagIP*/   true ,
           /*N_MAX_SOCKETS*/    this.N_MAX_SOCKETS_GL,
           /*N_SOCKETS*/        this.N_SOCKETS,
           /*treeMapIDGL */     (TreeMap)null,
           /*treeMapID_SOCKET */this.treeMapID_Socket ,
           /*ID_SOCKET */       id_socket
           );

          // Log.log("---> treemap ID_SOCKETs "+this.treeMapID_Socket,"");
           buf = tpduCGL.construirTPDU();

           //Enviar el buffer....
           send(buf,1/*TTL*/);
        }
        catch (ParametroInvalidoExcepcion e){this.error(e);}
        catch (PTMFExcepcion e){this.error(e);}
        catch (IOException e) {this.error(e);  }

  }

  //==========================================================================
  /**
   * Envï¿½a una TPDUCGL GRUPO_LOCAL
   * @param TTL TTL usado para enviar la notificaciï¿½n de grupo local.
   */
  private void enviarTPDU_CGL_GRUPO_LOCAL(short TTL)
  {
    Buffer   buf       = null;
    int N_Ids = 0;
    String mn = "CLThread.enviarTPDU_CGL_GRUPO_LOCAL";

    //
    // TPDU --> PTMF.TPDU_CGL_GRUPO_LOCAL
    //
    try
    {

     tpduCGL =  TPDUCGL.crearTPDUCGL
     (/*SocketPTMFImp*/      this.socketPTMFImp ,
      /*byte SUBTIPO*/     (byte)PTMF.TPDU_CGL_GRUPO_LOCAL,
      /*byte TTL */        (byte)TTL,
      /*short METRICA*/    (short)0 ,
      /*boolean flagIP*/   false ,
      /*N_MAX_SOCKETS*/    this.N_MAX_SOCKETS_GL,
      /*N_SOCKETS*/        this.N_SOCKETS,
      /*IDGL_EMISOR*/      (IDGL)null,
      /*treeMapIDGL */     (TreeMap)null, /*MODIFICADO */
      /*treeMapID_SOCKET */(TreeMap)null,
      /*ID_SOCKET */       (ID_Socket)null
      );

      buf = tpduCGL.construirTPDU();

      //Enviar el buffer....
      send(buf,TTL/*TTL*/);

      Log.debug(Log.CGL,mn,"TPDUCGL --> GRUPO_LOCAL "+tpduCGL.getIDGL()+"TTL SEND: "+TTL);

      //  Log.log("CGLThread","TPDUCGL --> GRUPO_LOCAL "+tpduCGL.getIDGL()+" TTL SEND: "+TTL);
      if(treeMapIDGLs != null && treeMapIDGLs.size() > 0)
      {
        Iterator iterator = treeMapIDGLs.keySet().iterator();

        while(iterator.hasNext())
        {
          IDGL idgl = (IDGL) iterator.next();
          Log.debug(Log.CGL,mn,"IDGL --> "+idgl);
        }
      }
    }
    catch (ParametroInvalidoExcepcion e){this.error(e);}
    catch (PTMFExcepcion e){this.error(e);}
    catch (IOException e) {this.error(e);}

  }

  //==========================================================================
  /**
   * Envï¿½a un TPDU CGL GRUPO_LOCAL_VECINO
   * @param TTL TTL usado para enviar la notificaciï¿½n de grupo local.
   */
  private void enviarTPDU_CGL_GRUPO_LOCAL_VECINO(short TTL, IDGL idgl_Emisor)
  {
    Buffer   buf       = null;
    String mn = "CLThread.enviarTPDU_CGL_GRUPO_LOCAL_VECINO";

    TreeMap treemapIDGL = (TreeMap) this.treeMapIDGLs.clone(); //Clonar
    //Quitarnos de la lista
    treemapIDGL.remove(this.idgl);

    //
    // TPDU --> PTMF.TPDU_CGL_GRUPO_LOCAL_VECINO
    //
    try
    {

     tpduCGL =  TPDUCGL.crearTPDUCGL
     (/*SocketPTMFImp*/    this.socketPTMFImp ,
      /*byte SUBTIPO*/     (byte)PTMF.TPDU_CGL_GRUPO_LOCAL_VECINO,
      /*byte TTL */        (byte)TTL,
      /*short METRICA*/    (short)0 ,
      /*boolean flagIP*/   false ,
      /*N_MAX_SOCKETS*/    this.N_MAX_SOCKETS_GL,
      /*N_SOCKETS*/        this.N_SOCKETS,
      /*IDGL_EMISOR*/      idgl_Emisor,
      /*treeMapIDGL */     (TreeMap)treemapIDGL,
      /*treeMapID_SOCKET */(TreeMap)null,
      /*ID_SOCKET */       (ID_Socket)null
      );

      buf = tpduCGL.construirTPDU();

      //Enviar el buffer....
      send(buf,TTL/*TTL*/);

      //Log.debug(Log.CGL,"CGLThread","TPDUCGL --> GRUPO_LOCAL "+tpduCGL.getIDGL()+"TTL SEND: "+TTL);
      Log.debug(Log.CGL,mn,"TPDUCGL --> GRUPO_LOCAL_VECINO "+tpduCGL.getIDGL()+" TTL SEND: "+TTL);

      if(idgl_Emisor!=null)
        Log.log(" ","IDGL_EMISOR --> "+idgl_Emisor);

      if(treemapIDGL != null && treemapIDGL.size() > 0)
      {
        Iterator iterator = treemapIDGL.keySet().iterator();
        while(iterator.hasNext())
        {
          IDGL idgl = (IDGL) iterator.next();
          Log.debug(Log.CGL,mn,"IDGL --> "+idgl);
        }
      }
    }
    catch (ParametroInvalidoExcepcion e){this.error(e);}
    catch (PTMFExcepcion e){this.error(e);}
    catch (IOException e) {this.error(e);}

  }

  //==========================================================================
  /**
   * Envï¿½a una TPDUCGL DEJAR_GRUPO_LOCAL
   */
  private void enviarTPDU_CGL_DEJAR_GRUPO_LOCAL(ID_Socket id_socket)
  {
    Buffer   buf       = null;
    TreeMap  treeMapIPSocket = null;
    String mn = "CLThread.enviarTPDU_CGL_DEJAR_GRUPO_LOCAL";


    Log.debug(Log.CGL,mn,"TPDUCGL --> DEJAR_GRUPO_LOCAL"+"\n"+tpduCGL.getIDGL());
    //
    // TPDU --> PTMF.TPDU_CGL_GRUPO_LOCAL
    //
    try
    {

     tpduCGL =  TPDUCGL.crearTPDUCGL
     (/*SocketPTMFImp*/      this.socketPTMFImp ,
      /*byte SUBTIPO*/     (byte)PTMF.TPDU_CGL_DEJAR_GRUPO_LOCAL,
      /*byte TTL */        (byte)1,
      /*short METRICA*/    (short)0 ,
      /*boolean flagIP*/   true ,
      /*N_MAX_SOCKETS*/    this.N_MAX_SOCKETS_GL,
      /*N_SOCKETS*/        this.N_SOCKETS,
      /*treeMapIDGL */     (TreeMap)null,       /* ALEX ; FALTA MIRA DOCUMENTACION*/
      /*treeMapID_SOCKET */(TreeMap)null,       /* ALEX ; FALTA MIRA DOCUMENTACION*/
      /*ID_SOCKET */       id_socket
      );


      buf = tpduCGL.construirTPDU();

      //Enviar el buffer....
      send(buf,1/*TTL*/);

    }
    catch (ParametroInvalidoExcepcion e){this.error(e);}
    catch (PTMFExcepcion e){this.error(e);}
    catch (IOException e) {this.error(e);}

  }

  //==========================================================================
  /**
   * Envï¿½a una TPDUCGL ELIMINACION_GRUPO_LOCAL
   */
  private void enviarTPDU_CGL_ELIMINACION_GRUPO_LOCAL()
  {
    Buffer   buf       = null;
    String mn = "CLThread.ELIMINACION_GRUPO_LOCAL";

    Log.debug(Log.CGL,mn,"TPDUCGL --> ELIMINACION_GRUPO_LOCAL"+"\n"+tpduCGL.getIDGL());

    this.socketPTMFImp.sendPTMFEventConexion("CGL: Eliminar Grupo Local: "+ this.idgl);

    //
    // TPDU --> PTMF.TPDU_CGL_ELIMINACION_GRUPO_LOCAL
    //
    try
    {

     tpduCGL =  TPDUCGL.crearTPDUCGL
     (/*SocketPTMFImp*/      this.socketPTMFImp ,
      /*byte SUBTIPO*/     (byte)PTMF.TPDU_CGL_ELIMINACION_GRUPO_LOCAL,
      /*byte TTL */        (byte)this.socketPTMFImp.getTTLSesion(),
      /*short METRICA*/    (short)0 ,
      /*boolean flagIP*/   false,
      /*N_MAX_SOCKETS*/    this.N_MAX_SOCKETS_GL,
      /*N_SOCKETS*/        this.N_SOCKETS,
      /*treeMapIDGL */     (TreeMap)null,
      /*treeMapID_SOCKET */(TreeMap)null,
      /*ID_SOCKET */       (ID_Socket)null
      );


      buf = tpduCGL.construirTPDU();

      //Enviar el buffer....
      send(buf,(byte)this.socketPTMFImp.getTTLSesion()/*TTL*/);

    }
    catch (ParametroInvalidoExcepcion e){this.error(e);}
    catch (PTMFExcepcion e){this.error(e);}
    catch (IOException e) {this.error(e);}

  }


  //==========================================================================
  /**
   * Envï¿½a una TPDUCGL BUSCAR_GRUPO_LOCAL.
   */
  private void enviarTPDU_CGL_BUSCAR_GRUPO_LOCAL()
  {
    Buffer   buf       = null;
    TreeMap  treeMapIPSocket = null;
    String mn = "CLThread.enviarTPDU_CGL_BUSCAR_GRUPO_LOCAL";

    Log.debug(Log.CGL,mn,"TPDUCGL --> BUSCAR_GRUPO_LOCAL");
    //Log.log("CGLThread","TPDUCGL --> BUSCAR_GRUPO_LOCAL");
    //
    // TPDU --> BUSCAR_GRUPO_LOCAL
    //
    try
    {
     tpduCGL =  TPDUCGL.crearTPDUCGL
     (/*SocketPTMFImp*/      this.socketPTMFImp ,
      /*byte SUBTIPO*/     (byte)PTMF.TPDU_CGL_BUSCAR_GRUPO_LOCAL,
      /*byte TTL */        (byte)1,
      /*short METRICA*/    (short)0 ,
      /*boolean flagIP*/   false,
      /*N_MAX_SOCKETS*/    this.N_MAX_SOCKETS_GL,
      /*N_SOCKETS*/        this.N_SOCKETS,
      /*treeMapIDGL */     (TreeMap)null,
      /*treeMapID_SOCKET */(TreeMap)null,
      /*ID_SOCKET */       (ID_Socket)null
      );


      buf = tpduCGL.construirTPDU();

      //Enviar el buffer....
      send(buf,1/*TTL*/);
    }
    catch (ParametroInvalidoExcepcion e){this.error(e);}
    catch (PTMFExcepcion e){this.error(e);}
    catch (IOException e) {this.error(e);}

  }

  //==========================================================================
  /**
   * ERROR. ESTE Mï¿½TODO SE LLAMA SI OCURRE UN ERROR GRAVE EN LA CLASE (Socket
   * Cerrado, etc.). Llama al mï¿½todo error de SocketPTMF.
   * @see SocketPTMF.
   */
  private void error(IOException e)
  {
     this.socketPTMFImp.error(e);
  }



  //==========================================================================
  /**
   * Notificaciï¿½n de los temporizdores
   */
  private void notificacionesTemporizadores()
  {
    final String mn = "DatosThread.notificacionesTemporizadores ()";

    //
    // Aceptaciï¿½n en grupo local del socket...
    //
    if(bAceptarSocket)
    {
     this.bAceptarSocket = false;

     while(this.colaAceptacionID_SOCKET.size() != 0)
     {
       ID_Socket id_socket = (ID_Socket) this.colaAceptacionID_SOCKET.removeFirst();

       //
       // Ver si no se ha pasado el lï¿½mite....
       //
       if(this.N_SOCKETS < this.N_MAX_SOCKETS_GL)
       {
         this.N_SOCKETS++;
         this.addID_Socket(id_socket);
         this.addID_Socket(this.socketPTMFImp.getID_Socket());
         this.enviarTPDU_CGL_SOCKET_ACEPTADO_EN_GRUPO_LOCAL(id_socket);
       }
     }
    }

    //
    // Notificaciï¿½n del grupo local ...
    //
    if(this.bNotificarGL )
    {
      this.bNotificarGL = false;
      this.bLanzadoTemporizadorNotificacionGL = false;
      this.enviarTPDU_CGL_GRUPO_LOCAL(this.TTL_Notificacion_GL);
    }

    //
    // Notificaciï¿½n del grupo local a los GL vecinos...
    //
    if(this.bNotificarGLVecinos )
    {
      this.bNotificarGLVecinos = false;
      this.bLanzadoTemporizadorNotificacionGLVecino = false;

      //Enviar un TPDU para cada IDGL recibido pero
      // teniendo en cuenta que no se repitan los TPDU de forma innecesaria.

      Iterator iterator = this.treeMapRespuestaGLVecinos.keySet().iterator();
      short TTLMayor = 0;
      while(iterator.hasNext())
      {
        IDGL idgl = (IDGL) iterator.next();

        if (TTLMayor < idgl.TTL)
        {
          TTLMayor = idgl.TTL;
          //Enviar
          this.enviarTPDU_CGL_GRUPO_LOCAL_VECINO(idgl.TTL,idgl);

          //Eliminar el IDGL del treemap....
          iterator.remove();
        }
      }
    }

    //
    // Notificaciï¿½n de BUSQUEDA de EMISOR....
    //
    if (this.bNotificarBusquedaEmisor)
    {
      this.bNotificarBusquedaEmisor = false;
      Log.debug (Log.CGL,mn,"treemapbusquedaemisores: " + this.treeMapBusquedaEmisores);

      Iterator iterator = this.treeMapBusquedaEmisores.values().iterator();
      while(iterator.hasNext())
      {
        Intentos_Emisor intentos_emisor = (Intentos_Emisor)iterator.next();

        if(this.TTL_BUSCAR_GRUPO_LOCAL_VECINO >= this.socketPTMFImp.getTTLSesion())
        {
          this.TTL_BUSCAR_GRUPO_LOCAL_VECINO = 1;
          Log.debug (Log.CGL,mn,"Intentos MAXIMOS de busqueda de GL PADRES para el IDGL Emisor "+intentos_emisor.idglEmisor+"EXCEDIDO!!!");

          //Despertar DatosThread...
          //Log.log (mn,"POR LO MENOS ME HAN DESPERTADO.");
          this.semaforoDatosThread.up();

          /*if(intentos_emisor.numero_intentos > CGLThread.MAX_INTENTOS_BUSQUEDA_GL_EMISOR)
          {
            iterator.remove();
            Log.log("CGLThread.notificacionesTemporizadores","Intentos MAXIMOS de busqueda de GL PADRES para el IDGL Emisor "+intentos_emisor.idglEmisor+"EXCEDIDO!!!");

            //Despertar DatosThread...
            Log.log (mn,"POR LO MENOS ME HAN DESPERTADO.");
            this.semaforoDatosThread.up();
          }
          else
          {
            enviarTPDU_CGL_BUSCAR_GL_PARA_EMISOR(intentos_emisor.idglEmisor);
            intentos_emisor.numero_intentos++;

            // Aï¿½ADIDO ESTA Lï¿½NEA. Sï¿½LO PARA PODER SEGUIR PROBANDO
            //Temporizador de espera para volver a reintentar...
            //
            this.socketPTMFImp.getTemporizador().registrarFuncion(this.T_ESPERA_BUSQUEDA_GL_EMISOR,
              timerHandler,this.EVENTO_BUSCAR_GL_EMISOR);
          }
          */
        }
        else
        {
            enviarTPDU_CGL_BUSCAR_GL_PARA_EMISOR(intentos_emisor.idglEmisor);
            intentos_emisor.numero_intentos++;

            // ANTONIO: Aï¿½ADIDO ESTA Lï¿½NEA. Sï¿½LO PARA PODER SEGUIR PROBANDO
            //Temporizador de espera para volver a reintentar...
            //
            this.socketPTMFImp.getTemporizador().registrarFuncion(this.T_ESPERA_BUSQUEDA_GL_EMISOR,
             timerHandler,this.EVENTO_BUSCAR_GL_EMISOR);
        }
      }

      //Si quedan datos en la cola, lanzar el temporizador de nuevo
      if(treeMapBusquedaEmisores.size() > 0)
       //
       //Temporizador de espera ...
       //
      this.socketPTMFImp.getTemporizador().registrarFuncion(T_ESPERA_BUSQUEDA_GL_EMISOR,timerHandler,EVENTO_BUSCAR_GL_EMISOR);
    }

    //
    // Notificar CG para EMISOR
    //
    if (bNotificarCGEmisor)
    {
      this.bNotificarCGEmisor = false;

      Iterator iterator = this.treeMapRespuestaBusquedaEmisores.keySet().iterator();

      while(iterator.hasNext())
      {
        IDGL idgl = (IDGL)iterator.next();

        //Enviar TTL...
        this.enviarTPDU_CGL_GL_PARA_EMISOR(idgl,this.getCGHijos(),(byte)idgl.TTL);
      }
    }

    //
    // Retransmitir UNIRSE_A_GRUPO_LOCAL
    //
    if (bRetransmitirUnirseGrupoLocal)
    {
       bRetransmitirUnirseGrupoLocal = false;

       //
       // TPDU --> UNIRSE_A_GRUPO_LOCAL
       //
       enviarTPDU_CGL_UNIRSE_A_GRUPO_LOCAL();

       //
       // Lanzar temporizador time out.....
       this.socketPTMFImp.getTemporizador().registrarFuncion(T_ESPERA_ACEPTACION_EN_GRUPO_LOCAL,timerHandler,EVENTO_TIME_OUT);

    }
  }







  //==========================================================================
  /**
   * Comprobar la Cache
   */
  private void comprobarCache(IDGL idgl)
  {
     // NOTA: Comprobar si el idgl estï¿½ en las caches. Si es asï¿½ eliminamos
     // la entrada de la cache.
     //
     if( this.cachePadres.containsKey(idgl))
        this.cachePadres.remove(idgl);
     if( this.cacheHijos.containsKey(idgl))
        this.cacheHijos.remove(idgl);

  }


  //==========================================================================
  /**
   * Almacena un IDGL en la lista y actualiza la lista de IDGLs a los que alcanza<br>
   * @param idgl El IDGL key
   * @param treemap_idgl Un treemap de IDGL que alcanza el IDGL key.
   */
  private void addIDGL(IDGL idgl_key, TreeMap treemap_idgl)
  {
       boolean bNotificar = false;

       //Log.log("CGLThread","Almacenar IDGL -- "+idgl_key);

       if(idgl_key == null)
        return;

       //
       // Almacenar este IDGL en la lista de los IDGL a los que nosotros alcanzamos
       //

       if (!this.treeMapIDGLs.containsKey(idgl_key))
       {
           //Log.log("IDGL_KEY NO ESTA en la lista","");
           bNotificar = true;
       }
       else
       {
             //      Log.log("IDGL_KEY YA ESTA en la lista","");

       }

       if(treemap_idgl != null)
       {
           if(!this.treeMapIDGLs.containsKey(idgl_key))
           {
                   //Aï¿½adir el IDGL
                  this.treeMapIDGLs.put(idgl_key,new RegistroIDGL_TreeMap(idgl_key,treemap_idgl));
                  //Log.log("IDGL Aï¿½adido <--" +idgl_key,"");
           }
           else
           {
              RegistroIDGL_TreeMap reg = (RegistroIDGL_TreeMap) this.treeMapIDGLs.get(idgl_key);

              if(reg==null)
              {
                  this.treeMapIDGLs.put(idgl_key ,new RegistroIDGL_TreeMap(idgl_key,treemap_idgl));
              }

              if(reg != null)
              {
                  reg.treemap.putAll(treemap_idgl);

              }
           }
       }
       else
       {
            //Aï¿½adir el IDGL
            this.treeMapIDGLs.put(idgl_key,new RegistroIDGL_TreeMap(idgl_key,new TreeMap()));
            //Log.log("IDGL Aï¿½adido <--" +idgl_key,"");
       }


       //Comprobar cache
       comprobarCache(idgl_key);

       if(bNotificar)
           // Notificar Nuevo IDGL a los IDGLListeners registrados
           this.notificarNuevoIDGL(idgl_key);


       //Log.debug(Log.CGL,"CGLThread","Nï¿½ Hijos : "+(idgl_treemap.treemap.size()));
  }


  //==========================================================================
  /**
   * Almacena un IDGL en la lista y actualiza la lista de IDGLs a los que alcanza<br>
   */
  private void addIDGL()
  {
       //Log.log("CGLThread","Almacenar IDGL -- "+tpduCGL.getIDGL());

       this.addIDGL(tpduCGL.getIDGL(),tpduCGL.getTreeMapIDGL());
  }

  //==========================================================================
  /**
   * Almacena un IDGL  en la sublista de otro IDGL.<br>
   * @param idgl_GL_VECINO El 1ï¿½ IDGL del TPDU GRUPO_LOCAL_VECINO
   */
  private void addIDGL(IDGL idgl_GL_VECINO)
  {
       //Log.log("CGLThread","Almacenar IDGL  -- "+tpduCGL.getIDGL()+" como alcanzable por el IDGL: "+idgl_GL_VECINO);

       //
       // almacenarlo-...
       //

       TreeMap treemap = new TreeMap();

       if(idgl_GL_VECINO != null)
        treemap.put(idgl_GL_VECINO,null);


       this.addIDGL(tpduCGL.getIDGL(),treemap );

  }

  //==========================================================================
  /**
   * Almacena un ID_Socket y lo notifica a todos los ID_SocketListener
   * si no estaba ya almacenados.
   */
  private void addID_Socket(ID_Socket id_socket)
  {

    if(!this.treeMapID_Socket.containsKey(id_socket))
    {
      //Almacenarlo....
      this.treeMapID_Socket.put(id_socket,null);

      //Notificarlo...
      this.notificarNuevoID_Socket(id_socket);
    }


    //Log.log("addID_socket() ---> tREEMAP id_sOCKETS "+this.treeMapID_Socket ,"");

  }


  //==========================================================================
  /**
   * Almacena una lista de ID_Sockets y notifica aquellos ID_Socket
   * que no estaban ya almacenados.
   */
  private void addID_Socket(TreeMap treeMap)
  {
    Iterator iterator = treeMap.keySet().iterator();

    while(iterator.hasNext())
    {
       ID_Socket id = (ID_Socket) iterator.next();
       this.addID_Socket(id);
    }
  }

  //==========================================================================
  /**
   * Send envï¿½a un buffer por el canal multicast con el TTL que se especifique.
   * @param buf El Buffer a enviar
   * @param ttl El TTL utilizado para enviar el buffer.
   */
  private void send(Buffer buf,int ttl) throws IOException
  {
          //
          // ENVIAR.. (Mandar n mensajes separados cada n mseg.
          //
          for(int i = 0; i<REDUNDANCIA_CGL; i++)
          {
            this.socketPTMFImp.getCanalMcast().send(buf,(byte)ttl);
            this.socketPTMFImp.getTemporizador().sleep(TIEMPO_REDUNDANCIA);
          }
  }


  //==========================================================================
  /**
   * Clase Intentos_Emisor.<br>
   * Almacena el Emisor y los nï¿½mero de intentos por cada emisor.
   */
   class Intentos_Emisor
   {
      short numero_intentos = 0;
      IDGL idglEmisor = null;

      Intentos_Emisor(IDGL idglEmisor, short intentos)
      {
       this.numero_intentos = intentos;
       this.idglEmisor = idglEmisor;
      }
   }


}


