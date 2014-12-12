//============================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	Fichero: VentanaEmision.java  1.0
//
// 	Autores: M. Alejandro Garc�a Dom�nguez (alejandro.garcia.dominguez@gmail.com)
//      	 Antonio Berrocal Piris (AntonioBP@wanadoo.es)
//
//	Descripci�n: Clase VentanaEmision. Ventana de emisi�n independiente de
//                   la implementaci�n, que no permite a�adir TPDUDatosNormal desordenados.
//                   Si permite a�adir TPDUDatosNormal duplicados.
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
 * Extiende la clase Ventana, redefiniendo el m�todo de a�adirTPDUDatosNormal para
 * que no permita a�adir TPDUDatosNormal desordenados.
 * <p><b>Esta clase est� preparada para trabajar concurrentemente. </b>
 * De la clase TPDUDatosNormal no utiliza nada m�s que su nombre, por lo que puede
 * ser enteramente modificada sin afectar a esta clase.
 * Los n�meros de secuencia los trata como long.
 * @version  1.0
 * @author Antonio Berrocal Piris
 * <A HREF="mailto:AntonioBP@wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
 * M. Alejandro Garc�a Dom�nguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 */
public class VentanaEmision extends Ventana
{

  // ATRIBUTOS

  /** QUITAR Y CONTROLARLO DESDE DONDE SE EMITE.
   * N�mero de secuencia del siguiente TPDUDatosNormal que se tiene que enviar.<br>
   * Comienza con el n�mero de secuencia inicial de la ventana y se incrementa
   * cada vez que se invoca a la funci�n {@siguienteTPDUDatosNormal ()}.
   */
  long lNumeroSecSiguiente;

 //==========================================================================
 /**
  * Crea una ventana de emisi�n con tama�o m�ximo y el n�mero de secuencia
  * inicial indicados.
  * @param tama�oMaximo tama�o m�ximo de la ventana.
  * @param nSecInicial n�mero de secuencia inicial de la ventana.
  * @exception ParametroInvalidoExcepcion lanzada cuando el n�mero de secuencia
  * inicial no es v�lido.
  */
 public VentanaEmision (int iTama�oMaximo,long lNSecInicial)
                                throws ParametroInvalidoExcepcion
 {
  super (iTama�oMaximo,lNSecInicial);

  final String mn = "VentanaEmision.VentanaEmision (tama�oMax,nSecInic)";

  //Log.debug (Log.VENTANA,mn,"Tama�o Max : " + tama�oMaximo + "  NSecInic: " + nSecInicial);

  this.lNumeroSecSiguiente = lNSecInicial;
 }

//==========================================================================
/**
 * A�ade un TPDUDatosNormal a la ventana. El n�mero de secuencia debe estar comprendido
 * entre el n�mero de secuencia inicial y final de la ventana.
 * <p>El n�mero de secuencia indicado ser� el que se asocie al TPDUDatosNormal por lo
 * que este deber� corresponder con el n�mero de secuencia verdadero del TPDUDatosNormal.
 * <p>Los TPDUDatosNormal se tienen que a�adir en orden consecutivo, partiendo del
 * n�mero de secuencia inicial.
 * @param nSec n�mero de secuencia del TPDUDatosNormal que se quiere a�adir.
 * @return el TPDUDatosNormal, si existe, que estaba asociado al n�mero de secuencia indicado,
 * o null si no existe.
 * @exception ParametroInvalidoExcepcion excepci�n lanzada cuando el n�mero de
 * secuencia no es el inicial y no es consecutivo.
 */
public /*synchronized */TPDUDatosNormal addTPDUDatosNormal (long lNSec,TPDUDatosNormal pqt)
                throws ParametroInvalidoExcepcion
{
 final String mn = "VentanaEmision.addTPDUDatosNormal (nSec,tpduDatosNormal)";

// Si se quiere EVITAR DUPLICADOS, preguntar primero, antes de a�adir
// si existe tal TPDUDatosNormal.

// Si el n�mero es el siguiente al n�mero de secuencia final de la ventana
// al intentar a�adirlo el m�todo super.addTPDUDatosNormal lanzar� la excepci�n.
  //Log.debug (Log.VENTANA,mn,"A�adir NSec: " + nSec);
  if ( (this.getNumSecInicial()==lNSec) || (this.contieneTPDUDatosNormal (lNSec-1)) )
     return super.addTPDUDatosNormal (lNSec,pqt);
  else
     throw new ParametroInvalidoExcepcion ("N�mero de secuencia no consecutivo: "+lNSec);

}

//=========================================================================
/**
 * Devuelve true si la ventana no contiene ning�n TPDU.
 */
 public boolean estaVacia ()
 {
  // Esta vacia si no tiene asociado un TPDU al n�mero de sec. inicial de la
  // ventana.
  if (this.contieneTPDUDatosNormal (this.getNumSecInicial()))
     return false;
  return true;
 }

 //=========================================================================
/**
 * Devuelve true si la ventana no contiene ning�n TPDU.
 */
 public boolean estaLlena ()
 {
  // Esta llena si tiene asociado un TPDU al n�mero de sec. final de la
  // ventana.
  if (this.contieneTPDUDatosNormal (this.getNumSecFinal()))
     return true;
  return false;
 }


//=========================================================================
/**
 * La primera vez que se ejecuta devuelve el TPDUDatosNormal con n�mero de secuencia
 * inicial. La siguientes ejecuciones devuelven el TPDUDatosNormal con n�mero de
 * secuencia consecutivo al �ltimo que devolv�o. Si no hay TPDUDatosNormal con dicho
 * n�mero de secuencia devulve null, en cuyo caso la siguiente vez que se ejecute
 * retornar� el TPDUDatosNormal con el mismo n�mero de secuencia.
 * A partir de devolver el �ltimo TPDUDatosNormal de la ventana retornar� null.<p>
 *
 * Lo que retorna es una referencia al TPDUDatosNormal, por lo que cualquier
 * modificaci�n que se le haga se reflejar� en la ventana, y viceversa.
 * @return TPDUDatosNormal TPDUDatosNormal siguiente, o null si no hay.
 */
 public TPDUDatosNormal siguienteTPDUDatosNormal ()
 {
  TPDUDatosNormal tpduDatosNormal = super.getTPDUDatosNormal (this.lNumeroSecSiguiente);

  if ((tpduDatosNormal!=null)&&(super.getNumSecFinal()>=this.lNumeroSecSiguiente))
        this.lNumeroSecSiguiente ++; // Incrementar n�mero de secuencia.
                                    // Al final, queda apuntando al siguiente
                                    // del final de la ventana.

  return tpduDatosNormal;
 }

//==========================================================================
/**
 * Elimina todos los TPDUDatosNormal de la ventana y fija un nuevo n�mero inicial.
 * El tama�o m�ximo de la ventana no es alterado.
 * Actualiza el n�mero de secuencia siguiente, al inicial de la ventana.
 * @param nSec nuevo n�mero de secuencia inicial de la ventana.
 * @exception ParametroInvalidoExcepcion lanzada cuando el n�mero de secuencia
 * pasado en el argumento no es v�lido.
 */
public void reiniciar (long lNSec) throws ParametroInvalidoExcepcion
{
  super.reiniciar (lNSec);
  this.lNumeroSecSiguiente = this.getNumSecInicial ();
}


//==========================================================================
/**
 * Incrementa en una unidad el n�mero de secuencia inicial y final, eliminando
 * el TPDUDatosNormal, si existe, asociado al n�mero de secuencia inicial.
 * Actualiza, si es necesario, el n�mero de secuencia siguiente.
 * @return el TPDUDatosNormal asociado al n�mero de secuencia inicial de la ventana,
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
 * Elimina todos los TPDUDatosNormal cuyo n�mero de secuencia asociado sea menor o igual
 * al indicado, si �ste es mayor que el n�mero de secuencia final de la ventana
 * se eliminan todos los TPDUDatosNormal.
 * <p>El n�mero de secuencia inicial pasa a ser el siguiente al indicado en el
 * argumento, salvo que �ste sea mayor que el n�mero de secuencia final de la
 * ventana, en cuyo caso pasa a ser el siguiente al n�mero de secuencia final
 * de la ventana. El n�mero de secuencia final se incrementa en la misma proporci�n
 * que el inicial.
 * @param nSec n�mero de secuencia
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
 * Redimensiona la ventana al nuevo tama�o m�ximo, adaptando el n�mero de
 * secuencia final. El n�mero de secuencia inicial no es alterado.
 * <p>
 * <B>LA VENTANA NO ELIMINA LOS TPDU CUANDO SE REDUCE LA VENTANA, ESPERA A QUE
 * SE ENTREGEN AL USUARIO O A LA RED PARA DESPU�S REDUCIR EL TAMA�O.</B>
 * @param nuevoTama�oMaximo el nuevo tama�o m�ximo de la ventana.
 * @return vector con TPDUDatosNormal eliminados o vac�o.
 */

public void setCapacidadVentana(int iNuevoTama�oMaximo)
{
  super.setCapacidadVentana (iNuevoTama�oMaximo);

  // Si es mayor, queda apuntando al siguiente del n�m. sec. final de la ventana,
  // indicando que se ha recorrido toda la ventana, es decir, de momento en esta
  // ventana no hay siguientes.
  if (this.lNumeroSecSiguiente>this.getNumSecFinal())
    this.lNumeroSecSiguiente = this.getNumSecFinal()+1;

}


}

