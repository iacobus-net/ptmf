//============================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	Fichero: ImpVentana.java  1.0
//
// 	Autores: M. Alejandro García Domínguez (alejandro.garcia.dominguez@gmail.com)
//      	 Antonio Berrocal Piris (AntonioBP@wanadoo.es)
//
//	Descripción: Clase abstracta ImpVentana.
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


package ptmf;

import java.util.*;


/**
 * Clase abstracta que define los métodos que tienen que tener las distintas
 * implementaciones de una ventana.
 * <p>En una ventana se pueden introducir TPDUDatosNormal
 * con número de secuencia comprendidos entre el número de secuencia inicial y
 * final de la ventana (inclusives).
 * <p>Para elegir entre las distintas implementaciones de ventana que
 * existan, se puede usar el método fábrica <b>{@link #getImpVentana}</b>.
 * <p><b>Esta clase no está preparada para trabajar concurrentemente.</b>
 * De la clase TPDUDatosNormal no utiliza nada más que su nombre, por lo que puede
 * ser enteramente modificada sin afectar a esta clase.<br>
 * Los números de secuencia los trata como long.
 * @version  1.0
 * @author Antonio Berrocal Piris
 * <A HREF="mailto:AntonioBP@wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
 * M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 */
public abstract class ImpVentana
{

//==========================================================================
/**
 * Devuelve el número de secuencia inicial de la ventana.
 * @return número de secuencia inicial de la ventana.
 */
abstract public long impGetNumSecInicial ();


//==========================================================================
/**
 * Devuelve el número de secuencia final de la ventana.
 * <p>Devuelve un número negativo en caso de que el tamaño máximo de la ventana
 * sea cero (no existe número de secuencia final).
 * @return número de secuencia final de la ventana, -1 en si el tamaño
 * de la ventana es cero.
 */
abstract public long impGetNumSecFinal ();


//===========================================================================
/**
 * Devuelve el número de secuencia más alto que verifica que todos los menores
 * o iguales a él tiene un TPDUDatosNormal asociado.
 * @return el número de secuencia mayor consecutivo, o -1 si no lo hay.
 */
abstract public long impGetNumSecConsecutivo ();

//==========================================================================
/**
 * Indica si la ventana contiene un TPDUDatosNormal con el número de secuencia
 * indicado.
 * @param lNSec número de secuencia del TPDUDatosNormal que se quiere comprobar.
 * @return true si la ventana lo contiene,, false en caso contrario.
 */
abstract public boolean impContieneTPDUDatosNormal (long lNSec);

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
abstract public TPDUDatosNormal impAddTPDUDatosNormal (long lNSec,TPDUDatosNormal pqt)
                                        throws ParametroInvalidoExcepcion;


//==========================================================================
/**
 * Elimina todos los TPDUDatosNormal de la ventana y fija un nuevo número inicial.
 * El tamaño máximo de la ventana no es alterado.
 * @param nSec nuevo número de secuencia inicial de la ventana.
 * @exception ParametroInvalidoExcepcion lanzada cuando el número de secuencia
 * pasado en el argumento no es válido.
 */
abstract public void impReiniciar (long lNSec) throws ParametroInvalidoExcepcion;

//==========================================================================
/**
 * Obtiene el TPDUDatosNormal que en esta ventana está asociado al número de secuencia
 * indicado  (no lo elimina).
 * <p>Se retorna una referencia al TPDUDatosNormal pedido, por lo que cualquier
 * modificación que se le haga se reflejará en la ventana, y viceversa.
 * @param nSec número de secuencia del TPDUDatosNormal.
 * @return el TPDUDatosNormal contenido en esta ventana y asociado al número de secuencia
 * indicado, o null si no hay un TPDUDatosNormal asociado o si el número de secuencia
 * no está comprendido entre el número de secuencia inicial y final de la ventana.
 */
abstract public TPDUDatosNormal impGetTPDUDatosNormal (long nSec);



//==========================================================================
/**
 * Obtiene todos los TPDUDatosNormal con número de secuencia menor o igual al
 * indicado (no los elimina). Si el nSec es mayor que el número de sec
 * final de la ventana, obtiene todos los TPDUDatosNormal que haya en la ventana.
 * <p>El vector retornado contiene las referencias a los TPDUDatosNormal pedidos,
 * por lo que cualquier modificación de los mismos se refleja en los
 * TPDUDatosNormal de la ventana, y viceversa.
 * @param nSec número de secuencia.
 * @return vector con los TPDUDatosNormal pedidos o vacío si no hay ninguno
 */
abstract public Vector impGetTPDUDatosNormalMenorIgual (long lNSec);


//==========================================================================
/**
 * Incrementa en una unidad el número de secuencia inicial y final, eliminando
 * el TPDUDatosNormal, si existe, asociado al número de secuencia inicial.
 * @return el TPDUDatosNormal asociado al número de secuencia inicial de la ventana,
 * o null si no hay un TPDUDatosNormal asociado.
 */
abstract public TPDUDatosNormal impRemoveTPDUDatosNormal ();

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
abstract public void impRemoveTPDUDatosNormal (long lNSec);


//==========================================================================
/**
 * Obtiene la capacidad de la ventana. Éste indica cuantos pares
 * [números de secuencia,TPDUDatosNormal] caben en la ventana.
 * @return el tamaño máximo de la ventana.
 */
abstract public int impGetCapacidadVentana ();

//==========================================================================
/**
 * Obtiene el tamaño de la ventana. Éste indica cuantos pares
 * [números de secuencia,TPDUDatosNormal] hay actualmente en la ventana.
 * @return el tamaño de la ventana.
 */
abstract public long impGetTamañoVentana ();

//==========================================================================
/**
 * Redimensiona la ventana a la nueva capacidad adaptando el número de
 * secuencia final. El número de secuencia inicial no es alterado.
 * <p>Si la nueva capacidad es inferior al anterior, se eliminan los TPDUDatosNormal
 * con número de secuencia asociado superior hasta adaptar el tamaño. Si es
 * superior, no se elimina ningún TPDUDatosNormal.
 * @param nuevoTamañoMaximo la nueva capacidad de la ventana.
 * @return vector con TPDUDatosNormal eliminados o vacío.
 */
abstract public Vector impCambiarCapacidadVentana (int iNuevoTamañoMaximo);


//==========================================================================
/**
 * De entre los números de secuencia de la ventana con TPDUDatosNormal asociado, devuelve
 * el mayor.
 * @return número de secuencia mayor con TPDUDatosNormal asociado, o -1 si ningún
 * número de secuencia de la ventana tiene TPDUDatosNormal asociado.
 */
abstract public long impGetNumSecMayor ();



//==========================================================================
/**
 * Devuelve una implementación de ventana. El método elige entre las distintas
 * implementaciones que conoce.
 * <p>Si el tamaño máximo de la ventana a crear supera el umbral definido por
 * la constante TAMAÑOUMBRAL entonces elige la implementación realizada con
 * {@link TreeMap} en la cual la memoria se va localizando conforme se necesita,
 * en caso contrario elige la implementación hecha con arrays en la cual la
 * memoria total necesaria se localiza en la creación.
 * @param tamañoMaximo tamaño máximo que tendrá la ventana
 * @param nSec número de secuencia inicial de la ventana
 * @return una implementación de ventana.
 * @exception ParametroInvalidoExcepcion lanzada cuando el número de secuencia
 * inicial no es válido.
 */
static final public ImpVentana getImpVentana (int iTamañoMaximo,long lNSec)
        throws ParametroInvalidoExcepcion
{
 if (iTamañoMaximo>iTAMAÑOUMBRAL)
        return new ImpVentanaArray (iTamañoMaximo,lNSec);//ImpVentanaTreeMap (tamañoMaximo,nSec);
 return new ImpVentanaArray (iTamañoMaximo,lNSec);
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
 * Constante que define el umbral que determina que tipo de implementación
 * utilizar.
 */
 private static final int iTAMAÑOUMBRAL = 16;


}
