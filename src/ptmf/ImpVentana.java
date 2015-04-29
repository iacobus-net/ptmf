//============================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	Fichero: ImpVentana.java  1.0
//
// 	Autores: M. Alejandro Garc�a Dom�nguez (alejandro.garcia.dominguez@gmail.com)
//      	 Antonio Berrocal Piris (AntonioBP@wanadoo.es)
//
//	Descripci�n: Clase abstracta ImpVentana.
//
// 	Authors: 
//		 Alejandro Garc�a-Dom�nguez (alejandro.garcia.dominguez@gmail.com)
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
//------------------------------------------------------------


package ptmf;

import java.util.*;


/**
 * Clase abstracta que define los m�todos que tienen que tener las distintas
 * implementaciones de una ventana.
 * <p>En una ventana se pueden introducir TPDUDatosNormal
 * con n�mero de secuencia comprendidos entre el n�mero de secuencia inicial y
 * final de la ventana (inclusives).
 * <p>Para elegir entre las distintas implementaciones de ventana que
 * existan, se puede usar el m�todo f�brica <b>{@link #getImpVentana}</b>.
 * <p><b>Esta clase no est� preparada para trabajar concurrentemente.</b>
 * De la clase TPDUDatosNormal no utiliza nada m�s que su nombre, por lo que puede
 * ser enteramente modificada sin afectar a esta clase.<br>
 * Los n�meros de secuencia los trata como long.
 * @version  1.0
 * @author Antonio Berrocal Piris
 * <A HREF="mailto:AntonioBP@wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
 * M. Alejandro Garc�a Dom�nguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 */
public abstract class ImpVentana
{

//==========================================================================
/**
 * Devuelve el n�mero de secuencia inicial de la ventana.
 * @return n�mero de secuencia inicial de la ventana.
 */
abstract public long impGetNumSecInicial ();


//==========================================================================
/**
 * Devuelve el n�mero de secuencia final de la ventana.
 * <p>Devuelve un n�mero negativo en caso de que el tama�o m�ximo de la ventana
 * sea cero (no existe n�mero de secuencia final).
 * @return n�mero de secuencia final de la ventana, -1 en si el tama�o
 * de la ventana es cero.
 */
abstract public long impGetNumSecFinal ();


//===========================================================================
/**
 * Devuelve el n�mero de secuencia m�s alto que verifica que todos los menores
 * o iguales a �l tiene un TPDUDatosNormal asociado.
 * @return el n�mero de secuencia mayor consecutivo, o -1 si no lo hay.
 */
abstract public long impGetNumSecConsecutivo ();

//==========================================================================
/**
 * Indica si la ventana contiene un TPDUDatosNormal con el n�mero de secuencia
 * indicado.
 * @param lNSec n�mero de secuencia del TPDUDatosNormal que se quiere comprobar.
 * @return true si la ventana lo contiene,, false en caso contrario.
 */
abstract public boolean impContieneTPDUDatosNormal (long lNSec);

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
abstract public TPDUDatosNormal impAddTPDUDatosNormal (long lNSec,TPDUDatosNormal pqt)
                                        throws ParametroInvalidoExcepcion;


//==========================================================================
/**
 * Elimina todos los TPDUDatosNormal de la ventana y fija un nuevo n�mero inicial.
 * El tama�o m�ximo de la ventana no es alterado.
 * @param nSec nuevo n�mero de secuencia inicial de la ventana.
 * @exception ParametroInvalidoExcepcion lanzada cuando el n�mero de secuencia
 * pasado en el argumento no es v�lido.
 */
abstract public void impReiniciar (long lNSec) throws ParametroInvalidoExcepcion;

//==========================================================================
/**
 * Obtiene el TPDUDatosNormal que en esta ventana est� asociado al n�mero de secuencia
 * indicado  (no lo elimina).
 * <p>Se retorna una referencia al TPDUDatosNormal pedido, por lo que cualquier
 * modificaci�n que se le haga se reflejar� en la ventana, y viceversa.
 * @param nSec n�mero de secuencia del TPDUDatosNormal.
 * @return el TPDUDatosNormal contenido en esta ventana y asociado al n�mero de secuencia
 * indicado, o null si no hay un TPDUDatosNormal asociado o si el n�mero de secuencia
 * no est� comprendido entre el n�mero de secuencia inicial y final de la ventana.
 */
abstract public TPDUDatosNormal impGetTPDUDatosNormal (long nSec);



//==========================================================================
/**
 * Obtiene todos los TPDUDatosNormal con n�mero de secuencia menor o igual al
 * indicado (no los elimina). Si el nSec es mayor que el n�mero de sec
 * final de la ventana, obtiene todos los TPDUDatosNormal que haya en la ventana.
 * <p>El vector retornado contiene las referencias a los TPDUDatosNormal pedidos,
 * por lo que cualquier modificaci�n de los mismos se refleja en los
 * TPDUDatosNormal de la ventana, y viceversa.
 * @param nSec n�mero de secuencia.
 * @return vector con los TPDUDatosNormal pedidos o vac�o si no hay ninguno
 */
abstract public Vector impGetTPDUDatosNormalMenorIgual (long lNSec);


//==========================================================================
/**
 * Incrementa en una unidad el n�mero de secuencia inicial y final, eliminando
 * el TPDUDatosNormal, si existe, asociado al n�mero de secuencia inicial.
 * @return el TPDUDatosNormal asociado al n�mero de secuencia inicial de la ventana,
 * o null si no hay un TPDUDatosNormal asociado.
 */
abstract public TPDUDatosNormal impRemoveTPDUDatosNormal ();

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
abstract public void impRemoveTPDUDatosNormal (long lNSec);


//==========================================================================
/**
 * Obtiene la capacidad de la ventana. �ste indica cuantos pares
 * [n�meros de secuencia,TPDUDatosNormal] caben en la ventana.
 * @return el tama�o m�ximo de la ventana.
 */
abstract public int impGetCapacidadVentana ();

//==========================================================================
/**
 * Obtiene el tama�o de la ventana. �ste indica cuantos pares
 * [n�meros de secuencia,TPDUDatosNormal] hay actualmente en la ventana.
 * @return el tama�o de la ventana.
 */
abstract public long impGetTama�oVentana ();

//==========================================================================
/**
 * Redimensiona la ventana a la nueva capacidad adaptando el n�mero de
 * secuencia final. El n�mero de secuencia inicial no es alterado.
 * <p>Si la nueva capacidad es inferior al anterior, se eliminan los TPDUDatosNormal
 * con n�mero de secuencia asociado superior hasta adaptar el tama�o. Si es
 * superior, no se elimina ning�n TPDUDatosNormal.
 * @param nuevoTama�oMaximo la nueva capacidad de la ventana.
 * @return vector con TPDUDatosNormal eliminados o vac�o.
 */
abstract public Vector impCambiarCapacidadVentana (int iNuevoTama�oMaximo);


//==========================================================================
/**
 * De entre los n�meros de secuencia de la ventana con TPDUDatosNormal asociado, devuelve
 * el mayor.
 * @return n�mero de secuencia mayor con TPDUDatosNormal asociado, o -1 si ning�n
 * n�mero de secuencia de la ventana tiene TPDUDatosNormal asociado.
 */
abstract public long impGetNumSecMayor ();



//==========================================================================
/**
 * Devuelve una implementaci�n de ventana. El m�todo elige entre las distintas
 * implementaciones que conoce.
 * <p>Si el tama�o m�ximo de la ventana a crear supera el umbral definido por
 * la constante TAMA�OUMBRAL entonces elige la implementaci�n realizada con
 * {@link TreeMap} en la cual la memoria se va localizando conforme se necesita,
 * en caso contrario elige la implementaci�n hecha con arrays en la cual la
 * memoria total necesaria se localiza en la creaci�n.
 * @param tama�oMaximo tama�o m�ximo que tendr� la ventana
 * @param nSec n�mero de secuencia inicial de la ventana
 * @return una implementaci�n de ventana.
 * @exception ParametroInvalidoExcepcion lanzada cuando el n�mero de secuencia
 * inicial no es v�lido.
 */
static final public ImpVentana getImpVentana (int iTama�oMaximo,long lNSec)
        throws ParametroInvalidoExcepcion
{
 if (iTama�oMaximo>iTAMA�OUMBRAL)
        return new ImpVentanaArray (iTama�oMaximo,lNSec);//ImpVentanaTreeMap (tama�oMaximo,nSec);
 return new ImpVentanaArray (iTama�oMaximo,lNSec);
}


//==========================================================================
/**
 * Devuelve una cadena informativa.
 */
public String toString ()
{
 // Redefinir en las subclases.
 return "";
}


/**
 * Constante que define el umbral que determina que tipo de implementaci�n
 * utilizar.
 */
 private static final int iTAMA�OUMBRAL = 16;


}
