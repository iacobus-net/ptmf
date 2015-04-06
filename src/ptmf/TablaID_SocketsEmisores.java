//============================================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: TablaID_SocketsEmisores.java  1.0 26/09/99
//
//
//	Descripci�n: Clase TablaID_SocketsEmisores. Almacena la informaci�n
//                   sobre los distintos emisores fuentes que hay en el grupo
//                   multicast.
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

import java.util.TreeMap;
import java.util.Iterator;

/**
 * Clase que almacena informaci�n sobre ID_Sockets que son fuentes de datos en el
 * grupo multicast.
 * Esta clase no es thread-safe.
 * @version  1.0
 * @author Antonio Berrocal Piris
 * <A HREF="mailto:AntonioBP@wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
 * M. Alejandro Garc�a Dom�nguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 */
public class TablaID_SocketsEmisores
{

 //ATRIBUTOS
 /**
  * Almacena la informaci�n sobre los id_sockets fuentes de datos.<br>
  * <table border=1>
  *  <tr>  <td><b>Key:</b></td>
  *	    <td>{@link ID_Socket} fuente.</td>
  *  </tr>
  *  <tr>  <td><b>Value:</b></td>
  *	    <td>Instancia de {@link RegistroTHE registro Tabla Emisores}.</td>
  *  </tr>
  * </table>
  */
 private TreeMap treeMapTablaID_Sockets = null;



 //==========================================================================
 /**
  * Constructor.
  */
  public TablaID_SocketsEmisores()
  {
   this.treeMapTablaID_Sockets = new TreeMap ();
  }

  //==========================================================================
  /**
   * A�ade un nuevo id_socket al que se le asocia una ventana de recepci�n.
   * @param id_Socket emisor fuente de datos a a�adir
   * @param idgl grupo local al que pertenece el id_Socket
   * @param iTtamMaxVtaRecep tama�o m�ximo de la ventana de recepci�n asociada
   * al id_Socket.
   * @param nSecInic n�mero de secuencia inicial de la ventana de recepci�n
   * asociada al id_Socket.
   * @return true si se ha a�adido, y false en caso contrario.
   * @exception ClaveDuplicadaExcepcion lanzada si id_Socket ya estaba registrado
   * @exception ParametroInvalidoExcepcion lanzada si nSecInic no es v�lido.
   */
  public void addID_Socket (ID_Socket id_Socket,
                            IDGL idgl,
                            int iTamMaxVtaRecep,
                            NumeroSecuencia nSecInic)
                                throws ClaveDuplicadaExcepcion,ParametroInvalidoExcepcion
  {
   if (this.treeMapTablaID_Sockets.containsKey (id_Socket))
      throw new ClaveDuplicadaExcepcion (
                    "Ya existe un ID_Socket emisor con la direcci�n indicada" + id_Socket);

   // Crear el registro para este ID_Sockets.
   RegistroTHE reg = new RegistroTHE ();

   reg.ventanaRecepcion = new VentanaRecepcion (iTamMaxVtaRecep,nSecInic.tolong ());
   // No se ha entregado ning�n TPDU al usuario procedente de id_Socket
   // IDGL del emisor
   reg.idgl = idgl;

   // Actualizamos el tiempo.
   reg.lTiempoUltimaRecepcion = Temporizador.tiempoActualEnMseg();

   this.treeMapTablaID_Sockets.put (id_Socket,reg);
  }

  //===========================================================================
  /**
   * Elimina id_Socket.
   * @param id_Socket
   * @return true si exist�a un ID_Socket y ha sido eliminado,o false en
   * caso contrario.
   */
  public boolean removeID_Socket (ID_Socket id_Socket)
  {
   if (this.treeMapTablaID_Sockets.remove (id_Socket)==null)
        return false;
   return true;
  }

  //==========================================================================
  /**
   * Actualiza el instante de tiempo �ltimo en que se ha recibido un {@link TPDUDatosNormal}
   * del id_Socket fuente indicado.
   * @param id_socket
   * @return true si id_socket est� registrado y ha sido actualizado.
   */
  public boolean actualizarTiempoUltimaRecepcion (ID_Socket id_socket)
  {
   RegistroTHE registroTHE = (RegistroTHE)this.treeMapTablaID_Sockets.get (id_socket);
   if (registroTHE==null)
     return false;

   long lTActual = Temporizador.tiempoActualEnMseg();
   if (lTActual > registroTHE.lTiempoUltimaRecepcion)
       registroTHE.lTiempoUltimaRecepcion = lTActual;
   return true;
  }

  //==========================================================================
  /**
   * Comprueba si ha transcurrido el tiempo m�ximo de inactividad {@link PTMF#TIEMPO_MAX_INACTIVIDAD_EMISOR}
   * permitido para los id_sockets fuentes de datos.
   * @return id_sockets que han superado el tiempo de inactividad m�ximo sin
   * que hayan enviado un {@link TPDUDatosNormal} al grupo multicast.
   * <table border=1>
   *  <tr>  <td><b>Key:</b></td>
   *	    <td>{@link ID_Socket} fuente.</td>
   *  </tr>
   *  <tr>  <td><b>Value:</b></td>
   *	    <td>NULL.</td>
   *  </tr>
   * </table>
   */
   public TreeMap comprobarInactividad()
   {
    TreeMap treeMapResult = new TreeMap();

    // Recorrer la tabla de CG Locales y ver quien ha superado el tiempo m�ximo
    // de inactividad.
    Iterator iteradorID_Socket = this.treeMapTablaID_Sockets.keySet().iterator();

    long lTActual = Temporizador.tiempoActualEnMseg();
    RegistroTHE registroTHENext;
    ID_Socket id_socketNext;
    while (iteradorID_Socket.hasNext())
     {
       id_socketNext = (ID_Socket)iteradorID_Socket.next();
       registroTHENext = (RegistroTHE)this.treeMapTablaID_Sockets.get (id_socketNext);

       if (registroTHENext==null)
        continue;

       if ( (lTActual - registroTHENext.lTiempoUltimaRecepcion)
             >
            PTMF.TIEMPO_MAX_INACTIVIDAD_EMISOR )
           treeMapResult.put (id_socketNext,null);
     }
    return treeMapResult;
   }

  //===========================================================================
  /**
   * Entrega al usuario de PTMF los datos que se hayan recibido correctamente
   * y que sean consecutivos del �ltimo entregado.
   * @param id_socket identificador del socket fuente de los datos a entregar.
   * @param colaRecepcion cola donde se depositar�n los datos.
   * @return true si no hay nada disponible que entregar al usuario o se ha
   * entregado todo lo disponible, y false en caso contrario.
   */
  public boolean entregaDatosUsuario (ID_Socket id_socket,ColaRecepcion colaRecepcion)
  {
   String mn ="TablaID_SocketEmisores.entregaDatosUsuario";

   if ((id_socket==null) || (colaRecepcion == null))
        return false;

   RegistroTHE registroTHE = (RegistroTHE) this.treeMapTablaID_Sockets.get (id_socket);

   if (registroTHE == null)
        return false;

   long nSecConsecutivo = registroTHE.ventanaRecepcion.getNumSecConsecutivo ();

   if (nSecConsecutivo<0)
        return false;

   // �Qu� pasa con los m�todos de la ventana que modifican los n�meros de
   // secuencia de la ventana ?
   if (registroTHE.lNSecAEntregar<registroTHE.ventanaRecepcion.getNumSecInicial())
   {
      // Entregar el inicial de la ventana.
       registroTHE.lNSecAEntregar = registroTHE.ventanaRecepcion.getNumSecInicial();
   }
   boolean bResult = false;
   for (;registroTHE.lNSecAEntregar<=nSecConsecutivo;registroTHE.lNSecAEntregar++)
     {
      TPDUDatosNormal tpduDatosNormal = registroTHE.ventanaRecepcion.
                                 getTPDUDatosNormal (registroTHE.lNSecAEntregar);

      if (tpduDatosNormal.getBufferDatos()!=null)
       {
        if (!colaRecepcion.add(id_socket,tpduDatosNormal.getBufferDatos(),tpduDatosNormal.getFIN_TRANSMISION()))
          {
            Log.debug (Log.DATOS_THREAD,mn,"COLA DE RECEPCION LLENA");
            Log.debug (Log.DATOS_THREAD,mn,"Tama�o de la cola de recepcion: " + colaRecepcion.getTama�o());
            Log.debug (Log.DATOS_THREAD,mn,"Bytes en la cola de recepcion: " + colaRecepcion.getCapacidad());
            Log.debug (Log.DATOS_THREAD,mn,"Tama�o del buffer a a�adir: " + tpduDatosNormal.getBufferDatos().getMaxLength());
            bResult=true;
            break;
            //return false;//CONTINUAR AQUI --> ALEX: 2/04/2003
                           // Ser� por que la cola esta llena, o algo as�.
                           // Puede ser un buen punto para averiguar problemas
                           // de control del flujo.
                           // Devolvemos false por que aunque le hayamos entregado
                           // algo, no le hemos podido entregar todo lo disponible.
          }
        }
      bResult = true;
     }
  return bResult;
  }

  //===========================================================================
  /**
   * Comprueba si id_tpdu ha sido entregado al usuario.<br>
   * <b>Nota:</b> Si este ha sido entregado, tambi�n se habr�n entregado todos
   * los datos anteriores (se entrega en orden).
   * @param id_tpdu
   * @return true si ha sido entregado.
   */
  public boolean entregadoAlUsuario (ID_TPDU id_tpdu)
  {
   if (id_tpdu == null)
     return false;

   RegistroTHE registroTHE = (RegistroTHE) this.treeMapTablaID_Sockets.get (id_tpdu.getID_Socket());

   if (registroTHE == null)
        return false;


   if (registroTHE.lNSecAEntregar>id_tpdu.getNumeroSecuencia().tolong())
     {
       return true;
     }
   else // Comprobar si es menor del inicial de la ventana.
        {
         if (registroTHE.ventanaRecepcion.getNumSecInicial()
                                        > id_tpdu.getNumeroSecuencia().tolong())
               return true;
        }

   return false;
  }

  //===========================================================================
  /**
   * Devuelve una referencia a la ventana de recepci�n asociada a id_Socket.<br>
   * <b>Las modificaciones realizadas a la ventana de recepci�n, a trav�s de la
   * referencia devuelta o de la almacenada en la tabla, se reflejar� en los
   * dos extremos.</b>
   * @param id_Socket
   * @return ventana de recepci�n asociada a id_Socket, o null si no existe.
   */
  public VentanaRecepcion getVentanaRecepcion (ID_Socket id_Socket)
  {
   RegistroTHE reg = (RegistroTHE)this.treeMapTablaID_Sockets.get (id_Socket);

   if (reg==null)
        return null;
   return reg.ventanaRecepcion;
  }

  //==========================================================================
  /**
   * Devuelve el IDGL del ID_Socket
   * @param id_Socket
   * @return idgl identificador del grupo local al que pertenece id_Socket.
   */
  public IDGL getIDGLFuente (ID_Socket id_Socket)
  {
   RegistroTHE reg = (RegistroTHE)this.treeMapTablaID_Sockets.get (id_Socket);

   if (reg==null)
        return null;
   return reg.idgl;
  }

  //==========================================================================
  /**
   * Devuelve un treeMap con los ID_Sockets fuentes.
   * @return
   * <table border=1>
   *  <tr>  <td><b>Key:</b></td>
   *	    <td>{@link ID_Socket} fuente.</td>
   *  </tr>
   *  <tr>  <td><b>Value:</b></td>
   *	    <td>NULL.</td>
   *  </tr>
   * </table>
   */
  public TreeMap getID_SocketEmisores ()
  {
    Iterator iteradorID_Socket = this.treeMapTablaID_Sockets.keySet().iterator();

    TreeMap treeMapResult = new TreeMap();
    while (iteradorID_Socket.hasNext())
      treeMapResult.put (iteradorID_Socket.next(),null);

    return treeMapResult;
  }

  //==========================================================================
  /**
   * Devuelve una cadena informativa.
   */
  public String toString ()
  {
   return this.treeMapTablaID_Sockets.toString ();
  }

}


/**
 * Guarda la informaci�n de un ID_Socket que es fuente de datos.
 * @see TablaID_SocketsEmisores#treeMapTablaID_Sockets
 * @version  1.0
 * @author M. Alejandro Garc�a Dom�nguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */
class RegistroTHE
{
  /** Ventana de recepci�n asociada al ID_Socket asociado al registro*/
  VentanaRecepcion ventanaRecepcion = null;

  /** IDGL del ID_Socket */
  IDGL idgl = null;

  /**
    * Instante de tiempo (mseg) en que se ha recibido el �ltimo {@link TPDUDatosNormal}
    * del id_Socket asociado al registro.
    */
  long lTiempoUltimaRecepcion = 0;

  /** N�mero de secuencia que se tiene que entregar al usuario. Los anteriores
    * a este ya han sido entregados. */
  long lNSecAEntregar = -1;

  //==========================================================================
  /**
   * Devuelve una cadena inforamativa.
   */
  public String toString ()
  {
   return this.ventanaRecepcion.toString () +
          this.idgl.toString() +
          "Tiempo Ultima Recepcion: " + this.lTiempoUltimaRecepcion +
          "NSec siguiente a entregar usuario: " + this.lNSecAEntregar;
  }
}
