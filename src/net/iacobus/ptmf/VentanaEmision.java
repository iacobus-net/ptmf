//============================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	Fichero: VentanaEmision.java  1.0
//
// 	Autores: M. Alejandro García Domínguez (alejandro.garcia.dominguez@gmail.com)
//      	 Antonio Berrocal Piris (AntonioBP@wanadoo.es)
//
//	Descripción: Clase VentanaEmision. Ventana de emisión independiente de
//                   la implementación, que no permite añadir TPDUDatosNormal desordenados.
//                   Si permite añadir TPDUDatosNormal duplicados.
//                   Es Thread-Safe.
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

/**
 * Extiende la clase Ventana, redefiniendo el método de añadirTPDUDatosNormal para
 * que no permita añadir TPDUDatosNormal desordenados.
 * <p><b>Esta clase está preparada para trabajar concurrentemente. </b>
 * De la clase TPDUDatosNormal no utiliza nada más que su nombre, por lo que puede
 * ser enteramente modificada sin afectar a esta clase.
 * Los números de secuencia los trata como long.
 * @version  1.0
 * @author Antonio Berrocal Piris
 * <A HREF="mailto:AntonioBP@wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
 * M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 */
public class VentanaEmision extends Ventana
{

  // ATRIBUTOS

  /** QUITAR Y CONTROLARLO DESDE DONDE SE EMITE.
   * Número de secuencia del siguiente TPDUDatosNormal que se tiene que enviar.<br>
   * Comienza con el número de secuencia inicial de la ventana y se incrementa
   * cada vez que se invoca a la función {@siguienteTPDUDatosNormal ()}.
   */
  long lNumeroSecSiguiente;

 //==========================================================================
 /**
  * Crea una ventana de emisión con tamaño máximo y el número de secuencia
  * inicial indicados.
  * @param tamañoMaximo tamaño máximo de la ventana.
  * @param nSecInicial número de secuencia inicial de la ventana.
  * @exception ParametroInvalidoExcepcion lanzada cuando el número de secuencia
  * inicial no es válido.
  */
 public VentanaEmision (int iTamañoMaximo,long lNSecInicial)
                                throws ParametroInvalidoExcepcion
 {
  super (iTamañoMaximo,lNSecInicial);

  final String mn = "VentanaEmision.VentanaEmision (tamañoMax,nSecInic)";

  //Log.debug (Log.VENTANA,mn,"Tamaño Max : " + tamañoMaximo + "  NSecInic: " + nSecInicial);

  this.lNumeroSecSiguiente = lNSecInicial;
 }

//==========================================================================
/**
 * Añade un TPDUDatosNormal a la ventana. El número de secuencia debe estar comprendido
 * entre el número de secuencia inicial y final de la ventana.
 * <p>El número de secuencia indicado será el que se asocie al TPDUDatosNormal por lo
 * que este deberá corresponder con el número de secuencia verdadero del TPDUDatosNormal.
 * <p>Los TPDUDatosNormal se tienen que añadir en orden consecutivo, partiendo del
 * número de secuencia inicial.
 * @param nSec número de secuencia del TPDUDatosNormal que se quiere añadir.
 * @return el TPDUDatosNormal, si existe, que estaba asociado al número de secuencia indicado,
 * o null si no existe.
 * @exception ParametroInvalidoExcepcion excepción lanzada cuando el número de
 * secuencia no es el inicial y no es consecutivo.
 */
public /*synchronized */TPDUDatosNormal addTPDUDatosNormal (long lNSec,TPDUDatosNormal pqt)
                throws ParametroInvalidoExcepcion
{
 final String mn = "VentanaEmision.addTPDUDatosNormal (nSec,tpduDatosNormal)";

// Si se quiere EVITAR DUPLICADOS, preguntar primero, antes de añadir
// si existe tal TPDUDatosNormal.

// Si el número es el siguiente al número de secuencia final de la ventana
// al intentar añadirlo el método super.addTPDUDatosNormal lanzará la excepción.
  //Log.debug (Log.VENTANA,mn,"Añadir NSec: " + nSec);
  if ( (this.getNumSecInicial()==lNSec) || (this.contieneTPDUDatosNormal (lNSec-1)) )
     return super.addTPDUDatosNormal (lNSec,pqt);
  else
     throw new ParametroInvalidoExcepcion ("Número de secuencia no consecutivo: "+lNSec);

}

//=========================================================================
/**
 * Devuelve true si la ventana no contiene ningún TPDU.
 */
 public boolean estaVacia ()
 {
  // Esta vacia si no tiene asociado un TPDU al número de sec. inicial de la
  // ventana.
  if (this.contieneTPDUDatosNormal (this.getNumSecInicial()))
     return false;
  return true;
 }

 //=========================================================================
/**
 * Devuelve true si la ventana no contiene ningún TPDU.
 */
 public boolean estaLlena ()
 {
  // Esta llena si tiene asociado un TPDU al número de sec. final de la
  // ventana.
  if (this.contieneTPDUDatosNormal (this.getNumSecFinal()))
     return true;
  return false;
 }


//=========================================================================
/**
 * La primera vez que se ejecuta devuelve el TPDUDatosNormal con número de secuencia
 * inicial. La siguientes ejecuciones devuelven el TPDUDatosNormal con número de
 * secuencia consecutivo al último que devolvío. Si no hay TPDUDatosNormal con dicho
 * número de secuencia devulve null, en cuyo caso la siguiente vez que se ejecute
 * retornará el TPDUDatosNormal con el mismo número de secuencia.
 * A partir de devolver el último TPDUDatosNormal de la ventana retornará null.<p>
 *
 * Lo que retorna es una referencia al TPDUDatosNormal, por lo que cualquier
 * modificación que se le haga se reflejará en la ventana, y viceversa.
 * @return TPDUDatosNormal TPDUDatosNormal siguiente, o null si no hay.
 */
 public TPDUDatosNormal siguienteTPDUDatosNormal ()
 {
  TPDUDatosNormal tpduDatosNormal = super.getTPDUDatosNormal (this.lNumeroSecSiguiente);

  if ((tpduDatosNormal!=null)&&(super.getNumSecFinal()>=this.lNumeroSecSiguiente))
        this.lNumeroSecSiguiente ++; // Incrementar número de secuencia.
                                    // Al final, queda apuntando al siguiente
                                    // del final de la ventana.

  return tpduDatosNormal;
 }

//==========================================================================
/**
 * Elimina todos los TPDUDatosNormal de la ventana y fija un nuevo número inicial.
 * El tamaño máximo de la ventana no es alterado.
 * Actualiza el número de secuencia siguiente, al inicial de la ventana.
 * @param nSec nuevo número de secuencia inicial de la ventana.
 * @exception ParametroInvalidoExcepcion lanzada cuando el número de secuencia
 * pasado en el argumento no es válido.
 */
public void reiniciar (long lNSec) throws ParametroInvalidoExcepcion
{
  super.reiniciar (lNSec);
  this.lNumeroSecSiguiente = this.getNumSecInicial ();
}


//==========================================================================
/**
 * Incrementa en una unidad el número de secuencia inicial y final, eliminando
 * el TPDUDatosNormal, si existe, asociado al número de secuencia inicial.
 * Actualiza, si es necesario, el número de secuencia siguiente.
 * @return el TPDUDatosNormal asociado al número de secuencia inicial de la ventana,
 * o null si no hay un TPDUDatosNormal asociado.
 */
public TPDUDatosNormal removeTPDUDatosNormal ()
{
 final String mn = "VentanaEmision.removeTPDUDatosNormal ()";

 //Log.debug (Log.VENTANA,mn,"");

  TPDUDatosNormal tpduDatos = super.removeTPDUDatosNormal ();
  if (this.lNumeroSecSiguiente<this.getNumSecInicial())
      this.lNumeroSecSiguiente = this.getNumSecInicial();
  return tpduDatos;
}


//==========================================================================
/**
 * Elimina todos los TPDUDatosNormal cuyo número de secuencia asociado sea menor o igual
 * al indicado, si éste es mayor que el número de secuencia final de la ventana
 * se eliminan todos los TPDUDatosNormal.
 * <p>El número de secuencia inicial pasa a ser el siguiente al indicado en el
 * argumento, salvo que éste sea mayor que el número de secuencia final de la
 * ventana, en cuyo caso pasa a ser el siguiente al número de secuencia final
 * de la ventana. El número de secuencia final se incrementa en la misma proporción
 * que el inicial.
 * @param nSec número de secuencia
 */
public void removeTPDUDatosNormal (long lNSec)
{
 final String mn = "VentanaEmision.removeTPDUDatosNormal (nSec)";

  //Log.debug (Log.VENTANA,mn,""+nSec);
  super.removeTPDUDatosNormal (lNSec);
  if (this.lNumeroSecSiguiente<this.getNumSecInicial())
      this.lNumeroSecSiguiente = this.getNumSecInicial();
}


//==========================================================================
/**
 * Redimensiona la ventana al nuevo tamaño máximo, adaptando el número de
 * secuencia final. El número de secuencia inicial no es alterado.
 * <p>
 * <B>LA VENTANA NO ELIMINA LOS TPDU CUANDO SE REDUCE LA VENTANA, ESPERA A QUE
 * SE ENTREGEN AL USUARIO O A LA RED PARA DESPUÉS REDUCIR EL TAMAÑO.</B>
 * @param nuevoTamañoMaximo el nuevo tamaño máximo de la ventana.
 * @return vector con TPDUDatosNormal eliminados o vacío.
 */

public void setCapacidadVentana(int iNuevoTamañoMaximo)
{
  super.setCapacidadVentana (iNuevoTamañoMaximo);

  // Si es mayor, queda apuntando al siguiente del núm. sec. final de la ventana,
  // indicando que se ha recorrido toda la ventana, es decir, de momento en esta
  // ventana no hay siguientes.
  if (this.lNumeroSecSiguiente>this.getNumSecFinal())
    this.lNumeroSecSiguiente = this.getNumSecFinal()+1;

}


}

