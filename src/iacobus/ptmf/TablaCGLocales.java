//============================================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	File: TablaCGLocales.java  1.0 25/10/99
//
// 	Autores: M. Alejandro Garc�a Dom�nguez (alejandro.garcia.dominguez@gmail.com)
//      	 Antonio Berrocal Piris (AntonioBP@wanadoo.es)
//
//	Description: Clase TablaCGLocales. Almacena la iformaci�n sobre los CG
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
 * Almacena la informaci�n sobre los CG locales. Cada TPDU Datos con el bit set
 * ACK activado tiene asignado un CG local que gestionar� su fiabilidad. La tabla
 * asocia el ID_TPDU al ID_Socket que es su CG local. <br>
 *
 * Esta clase no es thread-safe.
 * @version  1.0
 * @author Antonio Berrocal Piris
 * <A HREF="mailto:AntonioBP@wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
 * M. Alejandro Garc�a Dom�nguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 */
public class TablaCGLocales

{
  // ATRIBUTOS
  /**
   * Informaci�n sobre los rafagas de ID_TPDU y de los CG Local asociado.
   * <table border=1>
   *  <tr>  <td><b>Key:</b></td>
   *	    <td>{@link ID_Socket} fuente</td>
   *  </tr>
   *  <tr>  <td><b>Value:</b></td>
   *	    <td>
   *            <table>
   *                  <tr>  <td><b>Key:</b></td>
   *        	            <td>N�mer� de r�faga en un objeto Integer.</td>
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
   * A�ade una nuevo rafaga cuyo CG es el indicado en id_SocketCGLocal.<br>
   * Si la r�faga ya estaba registrada es actualizada.
   * @param id_SocketFuente id_socket emisor fuente de la r�faga.
   * @param iNumRafagaParam n�mero de la r�faga, tiene que ser mayor de cero.
   * @param nSecInicial n�mero de secuencia inicial donde comienza la r�faga, no
   * puede ser null.
   * @param id_SocketCGLocal id_socket que es CG de la r�faga.
   * @return true si la rafaga ha sido a�adido, y false en caso contrario.
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

   // A�adir la nueva r�faga
   RegistroRafaga reg   = new RegistroRafaga ();
   reg.id_SocketCGLocal = id_SocketCGLocal;
   reg.numeroSecInic    = nSecInicial;

   TreeMap treeMapRafagas = (TreeMap)this.treeMapTablaCGLocales.get (id_SocketFuente);
   if (treeMapRafagas!=null)
       {
        if (!treeMapRafagas.containsKey (new Integer (iNumRafagaParam)))
               treeMapRafagas.put (new Integer (iNumRafagaParam),reg);
        else { // Ya estaba registrada. S�lo actualizo
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
   * Actualiza la r�faga, si existe, con el nuevo n�mero de secuencia, si es menor
   * que el que hab�a registrado.
   * @param id_SocketFuente id_socket emisor fuente de la r�faga.
   * @param iNumRafagaParam n�mero de la r�faga, tiene que ser mayor de cero.
   * @param nSecInicial n�mero de secuencia inicial donde comienza la r�faga, no
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
        // Si no ten�a n�mero Sec inicial asignado, asignarlo
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
   * Actualiza la r�faga con un nuevo CG local.
   * @param id_SocketFuente id_socket emisor fuente de la r�faga.
   * @param iNumRafagaParam n�mero de la r�faga, tiene que ser mayor de cero.
   * @param id_SocketCGLocal id_socket que es CG de la r�faga.
   * @return true si la rafaga ha sido actualizada, y false, si la r�faga no
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
   * Devuelve el CG Local para la r�faga indicada.
   * @param id_SocketSrc id_socket emisor fuente de la r�faga.
   * @param iNumRafaga n�mero de la r�faga, tiene que ser mayor de cero.
   * @return CG local de la r�faga, o null si no est� registrada.
   */
  public ID_Socket getCGLocal (ID_Socket id_SocketSrc,int iNumeroRafaga)
  {
   final String mn = "TablaCGLocales.getCGLocal (id_Socket,numRaf)";

   if (id_SocketSrc==null)
      return null;

   // Est� indexado por la direcci�n fuente.
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
   * @return CG local, o null si la r�faga a la que pertenece id_TPDU no est�
   * registrada.
   */
  public ID_Socket getCGLocal (ID_TPDU id_TPDU)
  {
    ID_Socket id_SocketEmisor = id_TPDU.getID_Socket ();
    NumeroSecuencia nSec      = id_TPDU.getNumeroSecuencia ();

    // Est� indexado por la direcci�n fuente.
    TreeMap treeMapRafagas = (TreeMap)this.treeMapTablaCGLocales.get (id_SocketEmisor);


    if (treeMapRafagas==null)
        return null;

    // Buscar por todas las r�fagas asociadas al emisor del id_TPDU
    Iterator iterador = treeMapRafagas.values().iterator ();
    RegistroRafaga regNext = null;
    ID_Socket id_SocketCGLocal = null;
    while (iterador.hasNext())
     {
      regNext = (RegistroRafaga)iterador.next ();
      // Comprobar si tiene n�mero de secuencia inicial almacenado.
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
   * Elimina el la r�faga indicada.
   * @param id_SocketSrc id_socket emisor fuente de la r�faga.
   * @param iNumRafagaParam n�mero de la r�faga, tiene que ser mayor de cero.
   * @return true si la rafaga ha sido eliminada, y false, si la r�faga no
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
   * Elimina las r�fagas registradas a id_SocketSrc cuyo n�mero de secuencia
   * final sea menor o igual que nSecParam. El n�mero de secuencia final es
   * es el anterior del inicial de la r�faga inmediatamente siguiente.
   * Para eliminar, no es necesario que la r�faga sea la inmediatamente siguiente,
   * bastar� con la siguiente en {@link #treeMapTablaCGLocales}, puesto que el
   * n�mero de sec. final ser�, en todo caso, menor o igual al calculado.
   * @param id_SocketSrc id_socket emisor fuente de la r�faga.
   * @param nSecParam n�mero de secuencia.
   * @return true si se ha eliminado alguna r�faga y false en caso contrario.
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
      // N�mero de secuencia final de rafagaAnterior
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
      else break; // No hay que seguir buscando, puesto que est� ordenado
                   // por n�mero de r�faga, y una r�faga posterior tiene un
                   // n�mero de secuencia final mayor.

     } // Fin del while
    } // Fin del bloque de sentencias

   if (integerRafagaEliminar != null)
    { // Eliminar todas las r�fagas menores o iguales a rafagaEliminar
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
   * Elimina las r�fagas registradas cuyo emisor fuente sea el indicado.
   * @param id_Socket fuente de las r�fagas registradas a eliminar.
   */
  public void removeID_SocketFuente (ID_Socket id_Socket)
  {
    this.treeMapTablaCGLocales.remove (id_Socket);
  }

  //==========================================================================
  /**
   * Elimina todas las ocurrencias de id_socket, ya sea como fuente de r�fagas o
   * como CG Local de otros emisores fuentes.
   * @param id_Socket id_socket a eliminar.
   */
  public void removeID_Socket (ID_Socket id_Socket)
  {
   this.treeMapTablaCGLocales.remove (id_Socket);

   // Recorrer todas las fuentes y eliminar las r�fagas cuyo CG Local sea id_socket
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
   * Devuelve el n�mero de secuencia inicial de la r�faga.
   * @param id_SocketFuente id_socket emisor fuente de la r�faga.
   * @param iNumRafagaParam n�mero de la r�faga, tiene que ser mayor de cero.
   * @return n�mero de secuencia inicial de la r�faga, o null si no estaba registrada.
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
 * Almecena id_socket CG local, y n�meroSec inicio de la r�faga.
 *
 * Esta clase no es thread-safe.
 * @version  1.0
 * @author M. Alejandro Garc�a Dom�nguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */
class RegistroRafaga

{

 /** id_Socket del CG local de la r�faga */

 ID_Socket id_SocketCGLocal = null;


 /** N�mero de secuencia de inicio de la r�faga */

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









