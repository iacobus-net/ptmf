//============================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	Fichero: VentanaRecepcion.java  1.0
//
//
//	Descripción: Clase VentanaRecepcion. Ventana de recepción independiente de
//                   la implementación, que permite añadir TPDUDatosNormals desordenados
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
 * Extiende la clase Ventana, redefiniendo el método de añadirTPDUDatosNormal para
 * que permita añadir TPDUDatosNormal desordenados y duplicados.
 * <p><b>Esta clase no está preparada para trabajar concurrentemente. </b>
 * De la clase TPDUDatosNormal no utiliza nada más que su nombre, por lo que puede
 * ser enteramente modificada sin afectar a esta clase.
 * Los números de secuencia los trata como long.
 * @version  1.0
 * @author Antonio Berrocal Piris
 * <A HREF="mailto:AntonioBP@wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
 * M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 */
public class VentanaRecepcion extends Ventana
{



  // ATRIBUTOS
  /**  QUITAR
   * Almacena los números de secuencia de la ventana que se consideran
   * pérdidos. Las causas pueden ser:
   *    - Se ha añadido un TPDUDatosNormal no consecutivo del último que hay en la
   *      ventana.
   *    - El resto los va añadiendo el usuario de la clase, a través de los
   *      métodos addNSecNoRecibido (NumeroSecuencia ).
   * Los números de secuencia no recibidos se van eliminando conforme se añaden
   * los TPDUDatosNormal a la ventana.
   * Está indexado por Número de secuencia.
   * El vector contiene objetos NumeroSecuencia.
   */
  private TreeMap treeMapNSecNoRecibidos = null;


  /**
   * Número de secuencia del TPDU mayor entregado al usuario. <br>
   * Si es null indica que todavía no ha sido entregado ningún tpdu de esta
   * fuente al usuario. <br>
   * Esta variable es modificada a través de la función
   */
//  NumeroSecuencia nSecEntregado ;




 //==========================================================================
 /**
  * Crea una ventana de recepción con tamaño máximo y el número de secuencia
  * inicial indicados.
  * @param tamañoMaximo tamaño máximo de la ventana.
  * @param nSecInicial número de secuencia inicial de la ventana.
  * @exception ParametroInvalidoExcepcion lanzada cuando el número de secuencia
  * inicial no es válido.
  */
 public VentanaRecepcion (int iTamañoMaximo,long lNSecInicial)
                                 throws ParametroInvalidoExcepcion
 {
  super (iTamañoMaximo,lNSecInicial);

  // Reserva memoria para

 }


//==========================================================================
/**
 * Añade un TPDUDatosNormal a la ventana. El número de secuencia debe estar comprendido
 * entre el número de secuencia inicial y final de la ventana.
 * <p>El número de secuencia indicado será el que se asocie al TPDUDatosNormal por lo
 * que este deberá corresponder con el número de secuencia verdadero del TPDUDatosNormal.
 * @param nSec número de secuencia del TPDUDatosNormal que se quiere añadir.
 * @return el TPDUDatosNormal, si existe, que estaba asociado al número de secuencia indicado,
 * o null si no existe.
 * @exception ParametroInvalidoExcepcion excepción lanzada cuando el número de
 * secuencia no está comprendido entre el número de secuencia inicial y final
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
  * Devuelve un vector con objetos Long que indican los números de secuencia
  * de la ventana que no tienen un TPDUDatosNormal asociado. Este vector está ordenado
  * crecientemente por número de secuencia.
  * @return vector ordenado con los números de secuencia que no tienen TPDUDatosNormal
  * asociado, o vector vacío si no todos los números de secuencia tienen TPDUDatosNormal
  * asociado.
  */
 public Vector getNumsSecNoAñadidos ()
 {
   final String mn = "VentanaRecepcion.getNumsSecNoAñadidos()";
   Vector vectorResult = new Vector ();
   long lNSec;

   lNSec = getNumSecInicial ();

   try{
    while (lNSec<=getNumSecFinal())
     {
      if (!contieneTPDUDatosNormal(lNSec))
         vectorResult.add (new NumeroSecuencia (lNSec));
      lNSec ++; // Incrementar número de secuencia.
     }
    } catch (ParametroInvalidoExcepcion e)
       {
        //Log.debug (Log.VENTANA,mn,"Número de secuencia no válido.");
       }
   return vectorResult;
 }

 //==========================================================================
 /**
  * Devuelve un vector con los números de secuencia que no tienen
  * TPDUDatosNormal asociado, y son menores o iguales al indicado.
  * Este vector está ordenado crecientemente por número de secuencia.
  * Cada elemento del vector es un objeto NumeroSecuencia.
  * @param nSec número de secuencia hasta el que se va a examinar la ventana
  * en busca de TPDU no recibidos.
  * @return vector ordenado con los números de secuencia no recibidos, o vacío
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
      lNSecCont ++; // Incrementar número de secuencia.
     }
    } catch (ParametroInvalidoExcepcion e)
       {
        //Log.debug (Log.VENTANA,mn,"Número de secuencia no válido.");
       }
   return vectorResult;
 }

 //============================================================================
 /**
  * Comprueba si el número de secuencia ha estado en la ventana, o tiene asociado
  * un TPDU.
  * @param nSecParam número de secuencia a comprobar.
  * @return true si nSecParam es menor que el número de secuencia inicial de la
  * ventana o tiene asociado un TPDU, false en caso contrario, es decir,
  * no tiene asociado un TPDU en esta ventana siendo mayor que el número de
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


