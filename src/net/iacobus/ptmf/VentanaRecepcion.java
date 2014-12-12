//============================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	Fichero: VentanaRecepcion.java  1.0
//
//
//	Descripci�n: Clase VentanaRecepcion. Ventana de recepci�n independiente de
//                   la implementaci�n, que permite a�adir TPDUDatosNormals desordenados
//                   y duplicados.
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
//------------------------------------------------------------


package net.iacobus.ptmf;

import java.util.Vector;
import java.util.TreeMap;

/**
 * Extiende la clase Ventana, redefiniendo el m�todo de a�adirTPDUDatosNormal para
 * que permita a�adir TPDUDatosNormal desordenados y duplicados.
 * <p><b>Esta clase no est� preparada para trabajar concurrentemente. </b>
 * De la clase TPDUDatosNormal no utiliza nada m�s que su nombre, por lo que puede
 * ser enteramente modificada sin afectar a esta clase.
 * Los n�meros de secuencia los trata como long.
 * @version  1.0
 * @author Antonio Berrocal Piris
 * <A HREF="mailto:AntonioBP@wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
 * M. Alejandro Garc�a Dom�nguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 */
public class VentanaRecepcion extends Ventana
{



  // ATRIBUTOS
  /**  QUITAR
   * Almacena los n�meros de secuencia de la ventana que se consideran
   * p�rdidos. Las causas pueden ser:
   *    - Se ha a�adido un TPDUDatosNormal no consecutivo del �ltimo que hay en la
   *      ventana.
   *    - El resto los va a�adiendo el usuario de la clase, a trav�s de los
   *      m�todos addNSecNoRecibido (NumeroSecuencia ).
   * Los n�meros de secuencia no recibidos se van eliminando conforme se a�aden
   * los TPDUDatosNormal a la ventana.
   * Est� indexado por N�mero de secuencia.
   * El vector contiene objetos NumeroSecuencia.
   */
  private TreeMap treeMapNSecNoRecibidos = null;


  /**
   * N�mero de secuencia del TPDU mayor entregado al usuario. <br>
   * Si es null indica que todav�a no ha sido entregado ning�n tpdu de esta
   * fuente al usuario. <br>
   * Esta variable es modificada a trav�s de la funci�n
   */
//  NumeroSecuencia nSecEntregado ;




 //==========================================================================
 /**
  * Crea una ventana de recepci�n con tama�o m�ximo y el n�mero de secuencia
  * inicial indicados.
  * @param tama�oMaximo tama�o m�ximo de la ventana.
  * @param nSecInicial n�mero de secuencia inicial de la ventana.
  * @exception ParametroInvalidoExcepcion lanzada cuando el n�mero de secuencia
  * inicial no es v�lido.
  */
 public VentanaRecepcion (int iTama�oMaximo,long lNSecInicial)
                                 throws ParametroInvalidoExcepcion
 {
  super (iTama�oMaximo,lNSecInicial);

  // Reserva memoria para

 }


//==========================================================================
/**
 * A�ade un TPDUDatosNormal a la ventana. El n�mero de secuencia debe estar comprendido
 * entre el n�mero de secuencia inicial y final de la ventana.
 * <p>El n�mero de secuencia indicado ser� el que se asocie al TPDUDatosNormal por lo
 * que este deber� corresponder con el n�mero de secuencia verdadero del TPDUDatosNormal.
 * @param nSec n�mero de secuencia del TPDUDatosNormal que se quiere a�adir.
 * @return el TPDUDatosNormal, si existe, que estaba asociado al n�mero de secuencia indicado,
 * o null si no existe.
 * @exception ParametroInvalidoExcepcion excepci�n lanzada cuando el n�mero de
 * secuencia no est� comprendido entre el n�mero de secuencia inicial y final
 * de la ventana.
 */
public TPDUDatosNormal addTPDUDatosNormal (long lNSec,TPDUDatosNormal pqt)
                                throws ParametroInvalidoExcepcion
{
  final String mn = "VentanaRecepcion.addTPDUDatosNormal (nSec,pqt)";
  TPDUDatosNormal tpduResult = super.addTPDUDatosNormal (lNSec,pqt);
  //Log.debug (Log.VENTANA,mn,"" +nSec);
  return tpduResult;
}

 //==========================================================================
 /**
  * Devuelve un vector con objetos Long que indican los n�meros de secuencia
  * de la ventana que no tienen un TPDUDatosNormal asociado. Este vector est� ordenado
  * crecientemente por n�mero de secuencia.
  * @return vector ordenado con los n�meros de secuencia que no tienen TPDUDatosNormal
  * asociado, o vector vac�o si no todos los n�meros de secuencia tienen TPDUDatosNormal
  * asociado.
  */
 public Vector getNumsSecNoA�adidos ()
 {
   final String mn = "VentanaRecepcion.getNumsSecNoA�adidos()";
   Vector vectorResult = new Vector ();
   long lNSec;

   lNSec = getNumSecInicial ();

   try{
    while (lNSec<=getNumSecFinal())
     {
      if (!contieneTPDUDatosNormal(lNSec))
         vectorResult.add (new NumeroSecuencia (lNSec));
      lNSec ++; // Incrementar n�mero de secuencia.
     }
    } catch (ParametroInvalidoExcepcion e)
       {
        //Log.debug (Log.VENTANA,mn,"N�mero de secuencia no v�lido.");
       }
   return vectorResult;
 }

 //==========================================================================
 /**
  * Devuelve un vector con los n�meros de secuencia que no tienen
  * TPDUDatosNormal asociado, y son menores o iguales al indicado.
  * Este vector est� ordenado crecientemente por n�mero de secuencia.
  * Cada elemento del vector es un objeto NumeroSecuencia.
  * @param nSec n�mero de secuencia hasta el que se va a examinar la ventana
  * en busca de TPDU no recibidos.
  * @return vector ordenado con los n�meros de secuencia no recibidos, o vac�o
  * si se recibieron todos.
  *
  * <p> OPTIMIZAR: PONER UN VECTOR COMO ATRIBUTO Y EN EL SE VAN GUARDANDO LOS
  *                NUMEROS DE SECUENCIA QUE FALTEN (NO CONSECUTIVOS)
  *
  */
 public Vector getNumsSecNoRecibidos (NumeroSecuencia nSec)
 {
   final String mn = "VentanaRecepcion.getNumsSecNoRecibidos(nSec)";
   Vector vectorResult = new Vector ();

   long lNSecCont  = getNumSecInicial ();
   long lNSecMayor = 0;

   if (nSec.tolong()>getNumSecFinal())
    lNSecMayor = getNumSecFinal();
   else lNSecMayor = nSec.tolong();

   try{
    while (lNSecCont<=lNSecMayor)
     {
      if (!contieneTPDUDatosNormal(lNSecCont))
         vectorResult.add (new NumeroSecuencia (lNSecCont));
      lNSecCont ++; // Incrementar n�mero de secuencia.
     }
    } catch (ParametroInvalidoExcepcion e)
       {
        //Log.debug (Log.VENTANA,mn,"N�mero de secuencia no v�lido.");
       }
   return vectorResult;
 }

 //============================================================================
 /**
  * Comprueba si el n�mero de secuencia ha estado en la ventana, o tiene asociado
  * un TPDU.
  * @param nSecParam n�mero de secuencia a comprobar.
  * @return true si nSecParam es menor que el n�mero de secuencia inicial de la
  * ventana o tiene asociado un TPDU, false en caso contrario, es decir,
  * no tiene asociado un TPDU en esta ventana siendo mayor que el n�mero de
  * secuencia inicial.
  */
  public boolean recibido (NumeroSecuencia nSecParam)
  {
   long lNSec = nSecParam.tolong();
   long lNSecInicial = this.getNumSecInicial ();

   if (lNSec<lNSecInicial)
       return true;

   return this.contieneTPDUDatosNormal (lNSec);

  }


} // Fin de la clase VentanaRecepcion


