//============================================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	File: TablaCGLocales.java  1.0 25/10/99
//
// 	Autores: M. Alejandro García Domínguez (alejandro.garcia.dominguez@gmail.com)
//      	 Antonio Berrocal Piris (AntonioBP@wanadoo.es)
//
//	Description: Clase TablaCGLocales. Almacena la iformación sobre los CG
//                   locales.
//
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

import java.util.TreeMap;
import java.util.Iterator;

/**
 * Almacena la información sobre los CG locales. Cada TPDU Datos con el bit set
 * ACK activado tiene asignado un CG local que gestionará su fiabilidad. La tabla
 * asocia el ID_TPDU al ID_Socket que es su CG local. <br>
 *
 * Esta clase no es thread-safe.
 * @version  1.0
 * @author Antonio Berrocal Piris
 * <A HREF="mailto:AntonioBP@wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
 * M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 */
public class TablaCGLocales

{
  // ATRIBUTOS
  /**
   * Información sobre los rafagas de ID_TPDU y de los CG Local asociado.
   * <table border=1>
   *  <tr>  <td><b>Key:</b></td>
   *	    <td>{@link ID_Socket} fuente</td>
   *  </tr>
   *  <tr>  <td><b>Value:</b></td>
   *	    <td>
   *            <table>
   *                  <tr>  <td><b>Key:</b></td>
   *        	            <td>Númeró de ráfaga en un objeto Integer.</td>
   *                  </tr>
   *                  <tr>  <td><b>Value:</b></td>
   *	                    <td>Instancia de {@link RegistroRafaga}</td>
   *                  </tr>
   *            </table>
   *  </tr>
   * </table>
   */
  private TreeMap treeMapTablaCGLocales = null;

  //==========================================================================
  /**
   * Constructor.
   */
  public TablaCGLocales ()
  {
   this.treeMapTablaCGLocales = new TreeMap ();
  }

  //==========================================================================
  /**
   * Añade una nuevo rafaga cuyo CG es el indicado en id_SocketCGLocal.<br>
   * Si la ráfaga ya estaba registrada es actualizada.
   * @param id_SocketFuente id_socket emisor fuente de la ráfaga.
   * @param iNumRafagaParam número de la ráfaga, tiene que ser mayor de cero.
   * @param nSecInicial número de secuencia inicial donde comienza la ráfaga, no
   * puede ser null.
   * @param id_SocketCGLocal id_socket que es CG de la ráfaga.
   * @return true si la rafaga ha sido añadido, y false en caso contrario.
   */
  public boolean addRafaga (ID_Socket id_SocketFuente,
                            int iNumRafagaParam,
                            NumeroSecuencia nSecInicial,
                            ID_Socket id_SocketCGLocal)
  {
   final String mn = "TablaCGLocales.addRafaga(..)";

   if (nSecInicial == null)
       return false;

   if (iNumRafagaParam<=0)
       return false;

   // Añadir la nueva ráfaga
   RegistroRafaga reg   = new RegistroRafaga ();
   reg.id_SocketCGLocal = id_SocketCGLocal;
   reg.numeroSecInic    = nSecInicial;

   TreeMap treeMapRafagas = (TreeMap)this.treeMapTablaCGLocales.get (id_SocketFuente);
   if (treeMapRafagas!=null)
       {
        if (!treeMapRafagas.containsKey (new Integer (iNumRafagaParam)))
               treeMapRafagas.put (new Integer (iNumRafagaParam),reg);
        else { // Ya estaba registrada. Sólo actualizo
               treeMapRafagas.put (new Integer (iNumRafagaParam),reg);
             }
       }
   else {
         treeMapRafagas = new TreeMap ();
         treeMapRafagas.put (new Integer (iNumRafagaParam),reg);
         this.treeMapTablaCGLocales.put (id_SocketFuente,treeMapRafagas);
        }

   //Log.log (mn,"Contenido de TABLACGLOCALES: " + this.treeMapTablaCGLocales);
   return true;
}

  //==========================================================================
  /**
   * Actualiza la ráfaga, si existe, con el nuevo número de secuencia, si es menor
   * que el que había registrado.
   * @param id_SocketFuente id_socket emisor fuente de la ráfaga.
   * @param iNumRafagaParam número de la ráfaga, tiene que ser mayor de cero.
   * @param nSecInicial número de secuencia inicial donde comienza la ráfaga, no
   * puede ser null.
   * @return true si la rafaga ha sido actualizada, y false en caso contrario.
   */
  public boolean actualizarNumSecInicial (ID_Socket id_SocketFuente,
                                          int iNumRafagaParam,
                                          NumeroSecuencia nSecInicialParam)
  {
   if (iNumRafagaParam<=0)
       return false;

   if (nSecInicialParam == null)
       return false;

   RegistroRafaga reg     = null;

   TreeMap treeMapRafagas = (TreeMap)this.treeMapTablaCGLocales.get (id_SocketFuente);

   if (treeMapRafagas!=null)
   {
     reg = (RegistroRafaga)treeMapRafagas.get (new Integer (iNumRafagaParam));
     if (reg!=null)
      { // Actualizar
        // Si no tenía número Sec inicial asignado, asignarlo
        if (reg.numeroSecInic==null)
         {
          reg.numeroSecInic = nSecInicialParam;
          return true;
         }
        // Si es menor que el inicial, ponerlo como inicial.
        if (reg.numeroSecInic.compareTo (nSecInicialParam) > 0)
          {
           reg.numeroSecInic = nSecInicialParam;
          }
        return true;
      }
   }
   return false;
  }

  //==========================================================================
  /**
   * Actualiza la ráfaga con un nuevo CG local.
   * @param id_SocketFuente id_socket emisor fuente de la ráfaga.
   * @param iNumRafagaParam número de la ráfaga, tiene que ser mayor de cero.
   * @param id_SocketCGLocal id_socket que es CG de la ráfaga.
   * @return true si la rafaga ha sido actualizada, y false, si la ráfaga no
   * estaba registrada.
   */
  public boolean actualizaID_SocketCGLocal (ID_Socket id_SocketSrc,int iNumRafagaParam,
                                      ID_Socket id_SocketCGLocal)
  {
   final String mn = "TablaCGLoclases.actualizaID_SocketCGLocal (...)";

   if (iNumRafagaParam<=0)
       return false;

   TreeMap treeMapRafagas = (TreeMap)this.treeMapTablaCGLocales.get (id_SocketSrc);

   if (treeMapRafagas!=null)
   {
     RegistroRafaga reg = (RegistroRafaga)
                                treeMapRafagas.get (new Integer (iNumRafagaParam));
     if (reg!=null)
      { // Actualizar
       reg.id_SocketCGLocal = id_SocketCGLocal;
       //Log.log (mn,"Contenido de TABLACGLOCALES: " + this.treeMapTablaCGLocales);
       return true;
      }
   }
   return false;
  }

  //==========================================================================
  /**
   * Devuelve el CG Local para la ráfaga indicada.
   * @param id_SocketSrc id_socket emisor fuente de la ráfaga.
   * @param iNumRafaga número de la ráfaga, tiene que ser mayor de cero.
   * @return CG local de la ráfaga, o null si no está registrada.
   */
  public ID_Socket getCGLocal (ID_Socket id_SocketSrc,int iNumeroRafaga)
  {
   final String mn = "TablaCGLocales.getCGLocal (id_Socket,numRaf)";

   if (id_SocketSrc==null)
      return null;

   // Está indexado por la dirección fuente.
   TreeMap treeMapRafagas = (TreeMap) this.treeMapTablaCGLocales.get (id_SocketSrc);

   if (treeMapRafagas==null)
      return null;

   RegistroRafaga reg = (RegistroRafaga)
                              treeMapRafagas.get (new Integer (iNumeroRafaga));

   if (reg==null)
     return null;

   return reg.id_SocketCGLocal;
  }


  //==========================================================================
  /**
   * Devuelve el CG Local para el id_TPDU indicado.
   * @param id_TPDU
   * @return CG local, o null si la ráfaga a la que pertenece id_TPDU no está
   * registrada.
   */
  public ID_Socket getCGLocal (ID_TPDU id_TPDU)
  {
    ID_Socket id_SocketEmisor = id_TPDU.getID_Socket ();
    NumeroSecuencia nSec      = id_TPDU.getNumeroSecuencia ();

    // Está indexado por la dirección fuente.
    TreeMap treeMapRafagas = (TreeMap)this.treeMapTablaCGLocales.get (id_SocketEmisor);


    if (treeMapRafagas==null)
        return null;

    // Buscar por todas las ráfagas asociadas al emisor del id_TPDU
    Iterator iterador = treeMapRafagas.values().iterator ();
    RegistroRafaga regNext = null;
    ID_Socket id_SocketCGLocal = null;
    while (iterador.hasNext())
     {
      regNext = (RegistroRafaga)iterador.next ();
      // Comprobar si tiene número de secuencia inicial almacenado.
      if (regNext.numeroSecInic==null)
         continue;

      if (regNext.numeroSecInic.compareTo (nSec) <= 0)
         id_SocketCGLocal = regNext.id_SocketCGLocal;
      else break;
     } // Fin del while

    return id_SocketCGLocal;
  }

  //==========================================================================
  /**
   * Elimina el la ráfaga indicada.
   * @param id_SocketSrc id_socket emisor fuente de la ráfaga.
   * @param iNumRafagaParam número de la ráfaga, tiene que ser mayor de cero.
   * @return true si la rafaga ha sido eliminada, y false, si la ráfaga no
   * estaba registrada.
   */
  public boolean removeRafaga (ID_Socket id_SocketSrc,int iNumeroRafagaParam)
  {
    if ((id_SocketSrc==null) || (iNumeroRafagaParam<=0))
       return false;

    TreeMap treeMapRafagas = (TreeMap) this.treeMapTablaCGLocales.get (id_SocketSrc);

    if (treeMapRafagas!=null)
       if (treeMapRafagas.remove (new Integer (iNumeroRafagaParam)) != null)
                return true;
    return false;
  }

  //==========================================================================
  /**
   * Elimina las ráfagas registradas a id_SocketSrc cuyo número de secuencia
   * final sea menor o igual que nSecParam. El número de secuencia final es
   * es el anterior del inicial de la ráfaga inmediatamente siguiente.
   * Para eliminar, no es necesario que la ráfaga sea la inmediatamente siguiente,
   * bastará con la siguiente en {@link #treeMapTablaCGLocales}, puesto que el
   * número de sec. final será, en todo caso, menor o igual al calculado.
   * @param id_SocketSrc id_socket emisor fuente de la ráfaga.
   * @param nSecParam número de secuencia.
   * @return true si se ha eliminado alguna ráfaga y false en caso contrario.
   */
  public void removeRafagaNSecFinalMenorIgual (ID_Socket id_SocketSrc,
                                               NumeroSecuencia nSecParam)
  {
    if ((id_SocketSrc==null) || (nSecParam==null))
       return;

    // Buscar id_SocketSrc
    TreeMap treeMapRafagas = (TreeMap) this.treeMapTablaCGLocales.get (id_SocketSrc);

    if (treeMapRafagas==null)
     return;

    Integer integerRafagaAnterior = null;
    Integer integerRafaga = null;
    Integer integerRafagaEliminar = null;
    { // Inicio del bloque de sentencias.
    NumeroSecuencia nSecFinal = null;
    Iterator iteradorRafagas = treeMapRafagas.keySet().iterator ();
    RegistroRafaga reg = null;
    if (iteradorRafagas.hasNext())
      integerRafagaAnterior = (Integer)iteradorRafagas.next();
    while (iteradorRafagas.hasNext())
     {
      integerRafaga = (Integer)iteradorRafagas.next();
      reg = (RegistroRafaga)treeMapRafagas.get (integerRafaga);
      if (reg.numeroSecInic == null) // Comprobar add...
         continue;
      // Número de secuencia final de rafagaAnterior
      try{
        nSecFinal = reg.numeroSecInic.decrementar ();
      }catch (PTMFExcepcion e)
         {
          return;
         }
      if (nSecFinal.compareTo (nSecParam)<=0)
        {
         // Eliminar rafagaAnterior. Salta excepcion por estar iterando sobre ella,
         // IMPORTANTE: OBSERVAR
         // treeMapRafagas.remove (rafagaAnterior);
         integerRafagaEliminar = integerRafagaAnterior;

         // Actualizar rafagaAnterior
         integerRafagaAnterior = integerRafaga;
        }
      else break; // No hay que seguir buscando, puesto que está ordenado
                   // por número de ráfaga, y una ráfaga posterior tiene un
                   // número de secuencia final mayor.

     } // Fin del while
    } // Fin del bloque de sentencias

   if (integerRafagaEliminar != null)
    { // Eliminar todas las ráfagas menores o iguales a rafagaEliminar
      Iterator iteradorRafagas = treeMapRafagas.keySet().iterator ();
      while (iteradorRafagas.hasNext())
       {
        integerRafaga = (Integer)iteradorRafagas.next ();
        if (integerRafaga.intValue()<=integerRafagaEliminar.intValue())
          iteradorRafagas.remove();
        else return;
       } // Fin del while
    } // Fin del if
  }

  //==========================================================================
  /**
   * Elimina las ráfagas registradas cuyo emisor fuente sea el indicado.
   * @param id_Socket fuente de las ráfagas registradas a eliminar.
   */
  public void removeID_SocketFuente (ID_Socket id_Socket)
  {
    this.treeMapTablaCGLocales.remove (id_Socket);
  }

  //==========================================================================
  /**
   * Elimina todas las ocurrencias de id_socket, ya sea como fuente de ráfagas o
   * como CG Local de otros emisores fuentes.
   * @param id_Socket id_socket a eliminar.
   */
  public void removeID_Socket (ID_Socket id_Socket)
  {
   this.treeMapTablaCGLocales.remove (id_Socket);

   // Recorrer todas las fuentes y eliminar las ráfagas cuyo CG Local sea id_socket
   Iterator iteradorTreeMapRafagas = this.treeMapTablaCGLocales.values().iterator();
   TreeMap treeMapRafagasNext;
   while (iteradorTreeMapRafagas.hasNext())
    {
     treeMapRafagasNext = (TreeMap)iteradorTreeMapRafagas.next();
     if (treeMapRafagasNext == null)
        continue;

     Iterator iteradorRegistroRafagas = treeMapRafagasNext.values().iterator();
     RegistroRafaga regRafNext;
     while (iteradorRegistroRafagas.hasNext())
      {
       regRafNext = (RegistroRafaga)iteradorRegistroRafagas.next();
       if (regRafNext==null)
         continue;

       if (regRafNext.id_SocketCGLocal!=null
           &&
           regRafNext.id_SocketCGLocal.equals (id_Socket))
               iteradorRegistroRafagas.remove();

      } // Fin del while
    }// Fin del while
  }

  //=========================================================================
  /**
   * Devuelve el número de secuencia inicial de la ráfaga.
   * @param id_SocketFuente id_socket emisor fuente de la ráfaga.
   * @param iNumRafagaParam número de la ráfaga, tiene que ser mayor de cero.
   * @return número de secuencia inicial de la ráfaga, o null si no estaba registrada.
   */
  public NumeroSecuencia getNumSecInicialRafaga (ID_Socket id_SocketFuente,
                                                 int iNumRafagaParam)
  {
   TreeMap treeMapRafagas = (TreeMap) this.treeMapTablaCGLocales.get (id_SocketFuente);
   if (treeMapRafagas!=null)
     {
      RegistroRafaga reg = (RegistroRafaga)
                                treeMapRafagas.get (new Integer (iNumRafagaParam));
      if (reg!=null)
       return reg.numeroSecInic;
     }
   return null;
  }

 //===========================================================================
 /**

  * Devuelve una cadena informativa con los CG Locales.

  */

 public String toString ()

 {

  return this.treeMapTablaCGLocales.toString();

 }


}// Fin de la clase TablaCGLocales


//---------------------------------------------------------------------------
//                          CLASE RegistroRafaga
//---------------------------------------------------------------------------

/**
 * Almecena id_socket CG local, y númeroSec inicio de la ráfaga.
 *
 * Esta clase no es thread-safe.
 * @version  1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */
class RegistroRafaga

{

 /** id_Socket del CG local de la ráfaga */

 ID_Socket id_SocketCGLocal = null;


 /** Número de secuencia de inicio de la ráfaga */

 NumeroSecuencia numeroSecInic = null;


 //===========================================================================

 /**

  * Devuelve una cadena informativa del RegistroRafaga

  */

 public String toString ()

 {

  return "id_SocketCGLocal : " + id_SocketCGLocal +

         " Num. Sec Inicial : " + numeroSecInic

         ;

 }


}









