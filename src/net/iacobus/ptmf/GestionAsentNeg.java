//============================================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: GestionAsentNeg.java  1.0 24/10/99
//
//
//	Descripción: Clase GestionAsentNeg. Almacena y gestiona los asentimientos
//                   negativos (NACK y HNACK) que se tienen que enviar.
//
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

import java.util.TreeMap;
import java.util.Iterator;
import java.util.Random;
import java.io.IOException;

/**
 * Almacena y gestiona los ID_TPDU perdidos y que tienen que ser solicitados en
 * un NACK o HNACK.
 * Esta clase no es thread-safe.
 * @version  1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */
public class GestionAsentNeg implements TimerHandler
{
 // ATRIBUTOS

  /** Almacena instancias de {@link RegistroAsentNeg}. Contienen las listas de
   * de ID_TPDUs que aún no han sido recibidos y que se tiene que enviar asentimiento
   * negativo.
   * <table border=1>
   *  <tr>  <td><b>Key:</b></td>
   *	    <td>Identificador del RegistroAsentNeg</td>
   *  </tr>
   *  <tr>  <td><b>Value:</b></td>
   *	    <td>Instancia de RegistroAsentNeg</td>
   *  </tr>
   * </table>
   */
  private TreeMap treeMapAsentNegEnEspera = null;


  /** Almacena instancias de {@link RegistroAsentNeg}. Contienen las listas de
   * de ID_TPDUs que aún no han sido recibidos y que ya han agotado los intentos
   * de solicitud.
   * <table border=1>
   *  <tr>  <td><b>Key:</b></td>
   *	    <td>Identificador del RegistroAsentNeg</td>
   *  </tr>
   *  <tr>  <td><b>Value:</b></td>
   *	    <td>Instancia de RegistroAsentNeg</td>
   *  </tr>
   * </table>
   */
  private TreeMap treeMapAsentNegIntentosAgotados = null;

  /**
   * Listas temporal con los ID_TPDUs que se van añadiendo con el método
   * {@link #addID_TPDU(ID_TPDU)}. Se mantienen en esta lista hasta que
   * se forma con ellos un asentimiento negativo (NACK o HNACK) y se envía.
   * El asentimiento negativo se forma cuando se llama a la función {@link #enviarNuevoAsentNeg()}
   * y si ha transcurrido el {@link #lTRandomAsentNeg} de espera. Una vez
   * enviado se almacena en un RegistroAsentNeg para posibles retransmisiones.
   */
  private ListaOrdID_TPDU listaID_TPDUNoRecibidos = null;

  /**
   * Almacena los ID_TPDUs solicitados por miembros del grupo local en un
   * asentimiento negativo multicast durante la 2ª mitad de RTT.<br>
   * <table border=1>
   *  <tr>  <td><b>Key:</b></td>
   *	    <td>ID_TPDU</td>
   *  </tr>
   *  <tr>  <td><b>Value:</b></td>
   *	    <td>NULL</td>
   *  </tr>
   * </table>
   */
  private ListaOrdID_TPDU listaID_TPDU2MitadRTT = null;

  /** Generador de números aleatorios. */
  private Random random  = null;

  /** Referencia al socket creado */
  private SocketPTMFImp socketPTMFImp = null;

  /**
   * Tiempo de espera aleatorio antes de enviar un nuevo asentimiento negativo.
   * Se inicia a un valor aleatorio cuando se introduce el primer ID_TPDU en la
   * lista {@link #listaID_TPDUNoRecibidos}.
   */
  private long lTRandomAsentNeg = 0;

  /** Evento disparado por el temporizador. Indica que ha finalizado el tiempo
      de espera aleatorio para enviar un nuevo asentimiento negativo que estaba
      en espera.
    */
  private static final int iEVENTO_FIN_T_RANDOM_ASENT_NEG = 1;

  /**
   * TimerHandler que se encarga de la gestión del fin de oportunidades para
   * solicitar todos los ID_TPDUs indicados en el registroAsentNeg identificado
   * en el argumento lArg1, pasado por parámetro junto con el evento.<br>
   */
  private TimerHandler timerHandlerFinTAsentNeg = new TimerHandler ()
       {
         /**
          * Función utilizada por el  {@link Temporizador} para indicar el evento
          * registrado.
          * @param lArg1 identificador del RegistroAsentNeg cuyas oportunidades
          * de solicitud han finalizado.
          * @param o no utilizado.
          */
         public void TimerCallback(long lArg1,Object o)
          {
           // arg1 es el identificador del sentNeg que ha caducado.
           RegistroAsentNeg reg = (RegistroAsentNeg)
                               treeMapAsentNegEnEspera.remove (new Long (lArg1));

           // Pasar a la lista de agotados
           if (reg!=null)
                 treeMapAsentNegIntentosAgotados.put (new Long (lArg1),reg);
          }
        };

 //============================================================================
 /**
  * Constructor. Crea e inicializa los atributos.
  * @param socketPTMFImpParam socketPTMFImp al que está vinculada.
  */
  public GestionAsentNeg (SocketPTMFImp socketPTMFImpParam)
  {
    this.socketPTMFImp = socketPTMFImpParam;
    this.treeMapAsentNegEnEspera = new TreeMap ();
    this.treeMapAsentNegIntentosAgotados = new TreeMap ();
    this.listaID_TPDUNoRecibidos = new ListaOrdID_TPDU ();
    this.random = new Random (Temporizador.tiempoActualEnMseg ());
    this.listaID_TPDU2MitadRTT = new ListaOrdID_TPDU ();
  }

  //==========================================================================
  /**
   * Añade un nuevo id_TPDU a la lista de no recibidos, si no está contenido
   * en ninguno de los asentimientos negativos enviados o pendientes de envío.
   * @param id_TPDU id_tpdu a solicitar en un asentimiento negativo.
   */
  public void addID_TPDU (ID_TPDU id_TPDU)
  {
    // Añadir si no está en ningún TPDUNACK
    if (!this.contiene (id_TPDU))
      {
       if (this.listaID_TPDUNoRecibidos.size()==0) // No estamos esperando.
        {
         // Si esta vacia la listaID_TPDUNoRecibidos, entonces iniciar lTRandomAsentNeg
         this.lTRandomAsentNeg = (this.random.nextInt (PTMF.MAX_TRANDOM_ASENT_NEG-PTMF.T_BASE)+PTMF.T_BASE)
                           % PTMF.MAX_TRANDOM_ASENT_NEG;
         this.socketPTMFImp.getTemporizador().registrarFuncion (this.lTRandomAsentNeg,this,
                                               this.iEVENTO_FIN_T_RANDOM_ASENT_NEG);
         } // Fin del if
       this.listaID_TPDUNoRecibidos.put (id_TPDU,null);
     } // Fin del if
  }

  //==========================================================================
  /**
   * Elimina todos los ID_TPDU que no han sido recibidos después de haber agotado
   * los intentos de solicitud.
   * @return lista con los ID_TPDU cuyos intentos de solicitud han acabado.
   * <table border=1>
   *  <tr>  <td><b>Key:</b></td>
   *	    <td>ID_TPDU</td>
   *  </tr>
   *  <tr>  <td><b>Value:</b></td>
   *	    <td>NULL</td>
   *  </tr>
   * </table>
   */
  public ListaOrdID_TPDU removeIDs_TPDUAgotados ()
  {
   // Recorrer treeMapAsentNegIntentosAgotados y pasarlo a lista Ordenada ID_TPDU
   if (this.treeMapAsentNegIntentosAgotados.size ()>0)
     {
      ListaOrdID_TPDU listaResult = new ListaOrdID_TPDU ();
      RegistroAsentNeg regNext = null;
      Iterator iterador = this.treeMapAsentNegIntentosAgotados.values().iterator();
      while (iterador.hasNext())
        {
         regNext = (RegistroAsentNeg)iterador.next ();
         listaResult.putAll (regNext.listaID_TPDU);
        }// Fin del while
      this.treeMapAsentNegIntentosAgotados.clear ();
      return listaResult;
     } // Fin del if
    return null;
   }

  //==========================================================================
  /**
   * Es llamada cuando se recibe un asentimiento negativo, enviado por otro
   * miembro del grupo local. Esta información es retenida en la {@link #listaID_TPDU2MitadRTT}
   * para realizar supresión en el envío de los asentimientos negativos pendientes.
   * @param listaAsentNeg lista con los ID_TPDU que se solicitan en un asentimiento
   * negativo recibido de la red.
   * @param bFinRTTParam si es true indica que ha sido recibido a la finalización
   * de RTT, y antes de enviar los asentimientos negativos pendientes.
   */
  public void actualizar (ListaOrdID_TPDU listaAsentNeg,boolean bFinRTTParam)
  {
    if (listaAsentNeg==null)
      return;

    RegistroAsentNeg reg = null;

    if (bFinRTTParam
        ||
        (this.socketPTMFImp.getTemporizador().getPorcentajeRTTActual() > 50) )// MAYOR DEL 50 %
      {
        // Actualizar la lista
        this.listaID_TPDU2MitadRTT.putAll (listaAsentNeg);
      }

    /* Comprobar si es equivalente al NACK potencial que está esperando
       en this.listaID_TPDUNoRecibidos para ser formado. Si es equivalente
       formar  dicho NACK para que cuente como enviado (SUPRESIÓN DE NACK).
    */
    ListaOrdID_TPDU lista = new ListaOrdID_TPDU();
    Iterator iteradorID_TPDU = this.listaID_TPDUNoRecibidos.iteradorID_TPDU();
    ID_TPDU id_tpdu = null;
    while (iteradorID_TPDU.hasNext())
    {
     id_tpdu = (ID_TPDU)iteradorID_TPDU.next();
     if (this.listaID_TPDU2MitadRTT.contiene (id_tpdu)
         ||
         listaAsentNeg.contiene (id_tpdu) )
        {
         // Quitar de la lista en espera.
         iteradorID_TPDU.remove ();

         // Añadir a la lista.
         lista.put (id_tpdu,null);
        } // Fin del if
    } // Fin del while

   if (lista.size()>0)
    {
     // Formar un registro de Vector AsentNeg.
     reg = new RegistroAsentNeg();
     reg.listaID_TPDU = lista;

     // Registrar aviso RTT como si lo hubiese enviado.
     this.socketPTMFImp.getTemporizador().registrarAvisoRTT (this.timerHandlerFinTAsentNeg,
                                     reg.getIdentificador());

     // Estamos en espera para rtx. el NACK
     this.treeMapAsentNegEnEspera.put (new Long (reg.getIdentificador()),reg);

     if (this.listaID_TPDUNoRecibidos.size()==0)
      {
       // Ya no estamos esperando
       this.lTRandomAsentNeg = 0;
       // Cancelar el temporizador por el que estábamos esperando.
       this.socketPTMFImp.getTemporizador().cancelarFuncion (this,this.iEVENTO_FIN_T_RANDOM_ASENT_NEG);
      }

     // En el caso de que sigan quedando en la lista no recibidos hay
     // que volver a lanzar lTRandomAsentNeg

   } // Fin del if.
  }

  //===========================================================================
  /**
   * Vacia la lista de asentimientos negativos recibidos en la segunda mitad de
   * RTT.
   * @see #listaID_TPDU2MitadRTT
   */
  public void vaciarIDs_AsentNeg2MitadRTT ()
  {
   this.listaID_TPDU2MitadRTT.clear ();
  }

  //===========================================================================
  /**
   * Comprueba si id_TPDU está en algunos de los asentimientos negativos pendientes.
   * @param id_TPDU id_tpdu a comprobar
   * @return true si existe, y false en caso contrario.
   */
  private boolean contiene (ID_TPDU id_TPDU)
  {
    if (this.listaID_TPDUNoRecibidos.contiene (id_TPDU))
        return true;

    Iterator iterador = this.treeMapAsentNegEnEspera.values().iterator();
    while (iterador.hasNext())
     {
      RegistroAsentNeg reg = (RegistroAsentNeg)iterador.next();
      if (reg.listaID_TPDU.contiene (id_TPDU))
            return true;
     } // Fin del while

    // Buscamos también en la lista de los dados por perdidos.
    iterador = this.treeMapAsentNegIntentosAgotados.values().iterator();
    while (iterador.hasNext())
     {
      RegistroAsentNeg reg = (RegistroAsentNeg)iterador.next();
      if (reg.listaID_TPDU.contiene (id_TPDU))
            return true;
     } // Fin del while

    return false;
  }


  //===========================================================================
  /**
   * Elimina el id_tpdu de los asentimientos pendientes de envío.
   * @param id_TPDU
   * @return true si existe y ha sido eliminado, false en caso contrario.
   */
  public boolean removeID_TPDU (ID_TPDU id_TPDU)
  {
   if (this.listaID_TPDUNoRecibidos.contiene (id_TPDU))
    {
     this.listaID_TPDUNoRecibidos.remove (id_TPDU);
     // Si la lista ha quedado vacía ya no estamos esperando para enviar NACK.
     if (this.listaID_TPDUNoRecibidos.size()==0)
      {
       // Ya no estamos esperando
       this.lTRandomAsentNeg = 0;
       // Cancelar el temporizador por el que estábamos esperando.
       this.socketPTMFImp.getTemporizador().cancelarFuncion (this,this.iEVENTO_FIN_T_RANDOM_ASENT_NEG);
      } // Fin del if
     return true;
    } // Fin del if


   Iterator iterador = this.treeMapAsentNegEnEspera.values().iterator();
   while (iterador.hasNext())
    {
     RegistroAsentNeg reg = (RegistroAsentNeg)iterador.next();
     if (reg.listaID_TPDU.contiene (id_TPDU))
      {
       reg.listaID_TPDU.remove (id_TPDU);
       if (reg.listaID_TPDU.size()==0) // Se ha quedado vacio.
         {
          // Detener temporizador que estaba esperando por este NACK.
          this.socketPTMFImp.getTemporizador().cancelarRTT (timerHandlerFinTAsentNeg,reg.getIdentificador());

          iterador.remove ();
         }
       return true;
      }
    } // Fin del for

   // Eliminar de los dados por perdidos
   iterador = this.treeMapAsentNegIntentosAgotados.values().iterator();
   while (iterador.hasNext())
    {
     RegistroAsentNeg reg = (RegistroAsentNeg)iterador.next();
     if (reg.listaID_TPDU.contiene (id_TPDU))
      {
       reg.listaID_TPDU.remove (id_TPDU);
       if (reg.listaID_TPDU.size()==0) // Se ha quedado vacio.
          iterador.remove ();
       return true;
      }
    } // Fin del for
   return false;
  }

  //===========================================================================
  /**
   * Elimina de los asentimientos negativos todos los ID_TPDU cuyo id_socket sea
   * el especificado.
   * @param id_socket id_socket a eliminar.
   */
  public void removeID_Socket (ID_Socket id_socket)
  {
   this.listaID_TPDUNoRecibidos.getSublista(id_socket).clear();

   // Si la lista ha quedado vacía ya no estamos esperando para enviar NACK.
   if (this.listaID_TPDUNoRecibidos.size()==0)
    {
     // Ya no estamos esperando
     this.lTRandomAsentNeg = 0;
     // Cancelar el temporizador por el que estábamos esperando.
     this.socketPTMFImp.getTemporizador().cancelarFuncion (this,this.iEVENTO_FIN_T_RANDOM_ASENT_NEG);
    } // Fin del if


   Iterator iterador = this.treeMapAsentNegEnEspera.values().iterator();
   while (iterador.hasNext())
    {
     RegistroAsentNeg reg = (RegistroAsentNeg)iterador.next();
     reg.listaID_TPDU.getSublista(id_socket).clear();
     if (reg.listaID_TPDU.size()==0) // Se ha quedado vacio.
       {
        // Detener temporizador que estaba esperando por este NACK.
        this.socketPTMFImp.getTemporizador().cancelarRTT (timerHandlerFinTAsentNeg,reg.getIdentificador());
        iterador.remove ();
       } // Fin del if
     } // Fin del while

   // Eliminar de los dados por perdidos ¿ ?
   iterador = this.treeMapAsentNegIntentosAgotados.values().iterator();
   while (iterador.hasNext())
    {
     RegistroAsentNeg reg = (RegistroAsentNeg)iterador.next();
     reg.listaID_TPDU.getSublista (id_socket).clear();
     if (reg.listaID_TPDU.size()==0) // Se ha quedado vacio.
          iterador.remove ();
    } // Fin del while
  }


  //===========================================================================
  /**
   * Envía un nuevo asentimiento negativo al grupo multicast.
   * @return si ha enviado un nuevo asentimiento negativo.
   */
  public boolean enviarNuevoAsentNeg ()
  {
    // Comprobar si se tiene que formar un nuevo TPDUNACK con la lista de NSec.
    if (this.lTRandomAsentNeg>0)
      return false;

    if (this.listaID_TPDUNoRecibidos.size ()>0) // SE PUEDE PONER UN UMBRAL
                                                // PARA EVITAR MANDAR UN NACK
                                                // CON UN SOLO ID_TPDU
       {
        // Formar un registro de Vector NACK.
        RegistroAsentNeg reg = new RegistroAsentNeg();
        reg.listaID_TPDU = (ListaOrdID_TPDU)this.listaID_TPDUNoRecibidos.clone();

        // ¿ poner a 1 el contador?


        // Limpiar la lista
        this.listaID_TPDUNoRecibidos.clear ();

        // Estamos en espera para rtx. el NACK
        this.treeMapAsentNegEnEspera.put (new Long (reg.getIdentificador()),reg);

        // Anotamos en el temporizador para que avise.
        this.socketPTMFImp.getTemporizador().registrarAvisoRTT (this.timerHandlerFinTAsentNeg,
                                         reg.getIdentificador());

        // Crear del registro un TPDUNACK a partir de la lista
        // listaID_TPDUNoRecibidos, EN PRINCIPIO CABEN TODOS.
        if (this.formarEnviarAsentNeg (reg))
         {
          // Actualizar la lista de NACK enviados-recibidos 2ª mitad RTT.
          actualizar (reg.listaID_TPDU,false);
          return true;
         }
        else return false;

       }

    return false;
  }

  //==========================================================================
  /**
   * Retransmite los asentimientos negativos pendientes, y que no han agotado
   * todas las oportunidades de solicituf.
   */
  public void RtxAsentNeg ()
  {
   // Selecciona un TPDUNACK a reenviar que no hay estado en la red en la
   // segunda mitad de RTT.

   // Tiene que RTX todos los que tenga en espera.
   RegistroAsentNeg reg = null;
   Iterator iterador = this.treeMapAsentNegEnEspera.values().iterator();
   while (iterador.hasNext())
    {
     reg = (RegistroAsentNeg)iterador.next();

     // Incrementar el contadorRtx
     reg.iContadorRtx ++ ;

     // Formar el NACK y enviarlo.
     this.formarEnviarAsentNeg(reg);
    } // Fin del while

  }


  //===========================================================================
  /**
   * Construye un asentimientos negativo a partir del registroAsentNeg especificado
   * y lo envía.
   * @param registroAsentNeg
   * @return true si ha construido el asent. neg. y lo ha envíado.
   */
  private boolean formarEnviarAsentNeg (RegistroAsentNeg registroAsentNeg)
  {
   final String mn = "GestionAsentNeg.formarEnviarAsentNeg (registroAsentNeg)";
   ListaOrdID_TPDU lista = null;

   if (registroAsentNeg==null)
     return false;

   // Comprobar para supresión de asentimientos negativos.
   // No se suprime en el caso de que se tenga que mandar por unicast,
   // es decir, cuando registroAsentNeg.contadorRtx >= 2
   if ( this.listaID_TPDU2MitadRTT.size () > 0
        &&
        registroAsentNeg.iContadorRtx < 2 )
    {
      lista = new ListaOrdID_TPDU ();
      Iterator iteradorID_TPDU = registroAsentNeg.listaID_TPDU.iteradorID_TPDU ();
      ID_TPDU id_tpdu = null;
      while (iteradorID_TPDU.hasNext())
       {
        id_tpdu  = (ID_TPDU)iteradorID_TPDU.next ();
        if (!(this.listaID_TPDU2MitadRTT.contiene (id_tpdu)))
           lista.put (id_tpdu,null);
       }// Fin del while
    }
   else lista = registroAsentNeg.listaID_TPDU;

   try {
     if (lista.size()>0)
       {
        // Se agotan las oportunidades por lo que se pide por UNICAST.
        if (registroAsentNeg.iContadorRtx >= 2) // 3ª vez que se RTX.
          /* Destino: Fuente de los TPDU por unicast. Habrá que dividir
             el TPDUHNACK en tantos como id_socket fuentes diferentes haya.
          */
          // Recorrer la lista y enviar un tpduHNACK a cada uno de los
          // socket fuentes. ARREGLA DE FORMA QUE A CADA UNO SÓLO LE LLEGUEN
          // INFORMACIÓN PROPIA, ES MUY IMPORTANTE, ES DECIR, QUE NO LE LLEGUEN
          // LO QUE ME FALTA DE OTRO.
          {
           Iterator iteradorID_TPDU = lista.iteradorID_TPDU();
           // Key = id_socket; value = ListaOrdID_TPDU
           TreeMap treeMapID_Socket = new TreeMap();
           ID_TPDU id_tpduNext;
           ID_Socket id_socketNext;
           ListaOrdID_TPDU listaOrdID_TPDUNext;
           while (iteradorID_TPDU.hasNext())
            {
             id_tpduNext =  (ID_TPDU)iteradorID_TPDU.next();
             id_socketNext = id_tpduNext.getID_Socket();
             listaOrdID_TPDUNext = (ListaOrdID_TPDU)treeMapID_Socket.get (id_socketNext);
             if (listaOrdID_TPDUNext==null)
              {
               listaOrdID_TPDUNext = new ListaOrdID_TPDU ();
               listaOrdID_TPDUNext.put (id_tpduNext,null);
               treeMapID_Socket.put (id_socketNext,listaOrdID_TPDUNext);
              }
             else {
                   listaOrdID_TPDUNext.put (id_tpduNext,null);
                  }
             } // Fin del while
            Iterator iteradorID_Socket = treeMapID_Socket.keySet().iterator();
            TPDUHNACK tpduHNACK;
            while (iteradorID_Socket.hasNext())
             {
              id_socketNext = (ID_Socket)iteradorID_Socket.next();
              listaOrdID_TPDUNext = (ListaOrdID_TPDU)treeMapID_Socket.get (id_socketNext);
              /* Puede dar problemas de espacio. */
              tpduHNACK = TPDUHNACK.crearTPDUHNACK (this.socketPTMFImp,listaOrdID_TPDUNext);
              Address dirUnicastDestino = new Address (id_socketNext);
              this.socketPTMFImp.getCanalUnicast().send (tpduHNACK.construirTPDUHNACK(),
                                                         dirUnicastDestino);
              Log.debug(Log.HNACK,mn,"--> ENVIADO UN HNACK: " + tpduHNACK.getListaID_TPDU());
             }
           return true;
          } // Fin del if

        /* Se manda por HNACK Si:
                A - No tenemos ningún vecino.
                B - contadorRtx >= 1
        */
        if (this.socketPTMFImp.getCGLThread().numeroVecinos()==0
            ||
            registroAsentNeg.iContadorRtx >=1)
            {
             /* Puede dar problemas de espacio. */
             TPDUHNACK tpduHNACK = TPDUHNACK.crearTPDUHNACK (this.socketPTMFImp,lista);

             /* Destino: Hosts padres jerárquicos y vecinos, para los distintos
                         id_tpdu que viajan en el HNACK.
             NECESITO EL TTL DE LOS PADRES JERÁRQUICOS. TENER EN CUENTA
             QUE PUEDEN IR DE DIFERENTES GRUPOS LOCALES EN LA LISTA DE NO RECIBIDOS.
             */
             this.socketPTMFImp.getCanalMcast().send (tpduHNACK.construirTPDUHNACK());
             Log.log (mn,"ENVIADO UN HNACK: " + tpduHNACK.getListaID_TPDU());
             return true;
            }

        // Se manda por NACK contadorRTX < 1
        /*
          Puede dar problemas de espacio.
        */                    // Este y el de abajo son TPDUNACK no HNACK
        TPDUNACK tpduNACK = TPDUNACK.crearTPDUNACK (this.socketPTMFImp,lista);
        // Enviarlo: llegar a los hijos directos.
        /*
           Destino: Vecinos.
           Si no hay vecinos mandar HNACK.
           Sólo comentar (no hacer): si ningún vecino ha mandado un ACK entonces
           también mandar un HNACK, será por que: o no le ha dado tiempo o no
           tienen el TPDU (sería lógico o lo reciben todos o ninguno).
        */
        this.socketPTMFImp.getCanalMcast().send (tpduNACK.construirTPDUNACK(),
                     (byte)this.socketPTMFImp.getCGLThread().getTTLSocketsGL());
        Log.log (mn,"ENVIADO UN NACK: " + tpduNACK.getListaID_TPDU());
        return true;
       } // Fin del if
    }catch (PTMFExcepcion e)
      {
       return false;
      }
     catch (ParametroInvalidoExcepcion pie)
      {
       return false;
      }
     catch (IOException ioe)
      {
       return false;
      }
   return false;
}

  //===========================================================================
  /**
   * Función Callback que se registra en el temporizador.
   * @param lArg1 Indica el tipo de evento que la disparó.
   * @param o no utilizado.
   */
  public void TimerCallback(long lArg1,Object o)
  {
   // Comprobar el evento
   switch ((int)lArg1)
        {
         case iEVENTO_FIN_T_RANDOM_ASENT_NEG:
                         this.lTRandomAsentNeg = 0;
                         break;
         default:  // No puede ocurrir.
        } /* Fin del switch */
  }
} // Fin de la clase GestionAsentNeg



//-----------------------------------------------------------------------------
//                        CLASE   RegistroAsentNeg
//-----------------------------------------------------------------------------

/**
 * Información a guardar en cada una de las celdas del vectorAsentNeg.
 * Recoge la información de un asentimiento negativo.
 * Esta clase no es thread-safe.
 * @version  1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */
class RegistroAsentNeg
{
  /** Indica el siguiente identificador que tiene que ser asignado */
  private static long lSiguienteIdentificador = 0;

  /** Identifica de forma unívoca un RegistroAsentNeg */
  private long lIdentificador;

  /** TPDUAsentNeg */
  public ListaOrdID_TPDU listaID_TPDU = null;

  /** Número de veces que ha estado en la red **/
  public int iContadorRtx = 0;

  //==========================================================================
  /**
   * Constructor por defecto.
   */
  public RegistroAsentNeg ()
  {
   this.lIdentificador = lSiguienteIdentificador;
   lSiguienteIdentificador ++;
  }

  //==========================================================================
  /**
   * Devuelve el identificador de este RegistroAsentNeg
   */
  public long getIdentificador ()
  {
   return this.lIdentificador;
  }

  //==========================================================================
  /**
   * Devuelve una cadena informativa.
   */
  public String toString ()
  {
   return "Identificador: " + this.lIdentificador +
          "\nContado: " + this.iContadorRtx +
          "\nLista ID_TPDU: " + this.listaID_TPDU;
  }
} // Fin de la clase RegistroAsentNeg

