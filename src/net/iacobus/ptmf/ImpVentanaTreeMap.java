//============================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	File: ImpVentanaTreeMap.java  1.0
//
//
//	Descripción: Clase ImpVentanaTreeMap. Implementa la clase abstracta
//                   ImpVentana, localizando memoria conforme se va necesitando.
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

import java.util.TreeMap;
import java.util.Vector;


/**
 * Clase que implementa una ventana.
 * <p>En una ventana se pueden introducir TPDUDatosNormal
 * con número de secuencia comprendidos entre el número de secuencia inicial y
 * final de la ventana (inclusives).
 * <p>El espacio de memoria para almacenar los TPDUDatosNormal se localiza de
 * forma dinámica.
 * Es fácil de modificar para que permita un número ilimitado de
 * TPDUDatosNormal, puesto que no reserva el espacio previamente, si no
 * conforme lo va necesitando.
 * <p><b>Esta clase no está preparada para trabajar concurrentemente. (hacer???)<\b>
 * De la clase TPDUDatosNormal no utiliza, nada más que su nombre, por lo que puede
 * ser enteramente modificada sin afectar a esta clase.
 * Almacena el número de secuencia como un long.
 * No guarda TPDUDatosNormal null.
 * @version  1.0
 * @author Antonio Berrocal Piris
 * <A HREF="mailto:AntonioBP@wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
 * M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 */
public class ImpVentanaTreeMap extends ImpVentana
{

//==========================================================================
/**
 * Crea una ventana con el tamaño máximo especificado.
 * Si el tamaño es menor que cero la ventana tendrá un tamaño máximo de cero.
 * @param tamaño indica el número máximo de TPDUDatosNormal que caben en la ventana.
 * @throws ParametroInvalidoExcepcion lanzada si el número de secuencia no es válido.
 * @see OutOfMemoryError
 */
public ImpVentanaTreeMap (int iTamañoMaximo,long lNSecInic) throws ParametroInvalidoExcepcion
{

 if (lNSecInic<0)
     { this.iCapacidadVentana = 0;
       this.lNumeroSecInicial = -1;
       throw new ParametroInvalidoExcepcion ("Número de sec " + lNSecInic
                                              + " no es válido.");
     }

 if (iTamañoMaximo>0)
 {

  this.treeMapTPDUDatosNormal = new TreeMap (); // Almacenará los TPDUDatosNormal ordenados por número de secuencia.
  if (this.treeMapTPDUDatosNormal == null)
       throw new OutOfMemoryError ("No se pudo localizar memoria para crear la ventana.");
   this.iCapacidadVentana = iTamañoMaximo;
  }
  else this.iCapacidadVentana = 0;

  this.lNumeroSecInicial = lNSecInic;
  this.lNumeroSecConsecutivo = -1;

}


//==========================================================================
/**
 * Devuelve el número de secuencia inicial de la ventana.
 * @return número de secuencia inicial de la ventana.
 */
public long impGetNumSecInicial ()
{
 return this.lNumeroSecInicial;
}

//==========================================================================
/**
 * Devuelve el número de secuencia final de la ventana.
 * <p>Devuelve un número negativo en caso de que el tamaño máximo de la ventana
 * sea cero (no existe número de secuencia final).
 * @return número de secuencia final de la ventana, -1 en si el tamaño
 * de la ventana es cero.
 */
public long impGetNumSecFinal ()
{
 if (this.iCapacidadVentana>0)
   return this.lNumeroSecInicial + this.iCapacidadVentana - 1;
 return -1;
}

//===========================================================================
/**
 * Devuelve el número de secuencia más alto que verifica que todos los menores
 * o iguales a él tiene un TPDUDatosNormal asociado.
 * @return el número de secuencia mayor consecutivo, o -1 si no lo hay.
 */
public long impGetNumSecConsecutivo ()
{
 return this.lNumeroSecConsecutivo;
}

//==========================================================================
/**
 * Indica si la ventana contiene un TPDUDatosNormal con el número de secuencia
 * indicado.
 * @param nSec número de secuencia del TPDUDatosNormal que se quiere comprobar.
 * @return true si la ventana lo contiene,, false en caso contrario.
 */
public boolean impContieneTPDUDatosNormal (long lNSec)
{
if (this.iCapacidadVentana>0)
 return (this.treeMapTPDUDatosNormal.containsKey (new Long (lNSec)));
return false;
}


//==========================================================================
/**
 * Añade un TPDUDatosNormal a la ventana. El número de secuencia debe estar comprendido
 * entre el número de secuencia inicial y final de la ventana.
 * <p>El número de secuencia indicado será el que se asocie al TPDUDatosNormal por lo
 * que este deberá corresponder con el número de secuencia verdadero del TPDUDatosNormal.
 * @param lNSec número de secuencia del TPDUDatosNormal que se quiere añadir.
 * @param pqt TPDUDatosNormal a añadir
 * @return el TPDUDatosNormal, si existe, que estaba asociado al número de secuencia indicado,
 * o null si no existe.
 * @exception ParametroInvalidoExcepcion excepción lanzada cuando el número de
 * secuencia no está comprendido entre el número de secuencia inicial y final
 * de la ventana.
 */
public TPDUDatosNormal impAddTPDUDatosNormal (long lNSec,TPDUDatosNormal pqt)
                                        throws ParametroInvalidoExcepcion
{
 long iIndice;
 TPDUDatosNormal pqtAntiguo = null;


 if (pqt==null)
   return null;

 // Comprobar si el TPDUDatosNormal pertenece a la ventana (hacer excepción).
 if ((lNSec>impGetNumSecFinal())||(lNSec<impGetNumSecInicial()))
     throw new ParametroInvalidoExcepcion ("El número de secuencia "+lNSec+
                        ", no puede ser añadido a esta ventana.");

 pqtAntiguo = (TPDUDatosNormal)this.treeMapTPDUDatosNormal.put(new Long(lNSec),pqt);

 // Comparar si se ha modificado el numeroSecConsecutivo
 if (this.lNumeroSecConsecutivo == -1)
   // Comprobar cual es el más alto consecutivo
   {
    for (long lNSecIt = this.impGetNumSecInicial();
                                    lNSec <= this.impGetNumSecFinal();lNSecIt++)
         if (!this.impContieneTPDUDatosNormal (lNSecIt))
                break;
         else this.lNumeroSecConsecutivo = lNSecIt;
   }
 else { // Ver si es continuación del ya registrado
       if ((lNSec - this.lNumeroSecConsecutivo)==1)
         for (long lNSecIt = lNSec; lNSec <= this.impGetNumSecFinal();lNSecIt++)
              if (!this.impContieneTPDUDatosNormal (lNSecIt))
                 break;
              else this.lNumeroSecConsecutivo = lNSecIt;
      }

 return pqtAntiguo;
} // Fin de addTPDUDatosNormal


//==========================================================================
/**
 * Elimina todos los TPDUDatosNormal de la ventana y fija un nuevo número inicial.
 * El tamaño máximo de la ventana no es alterado.
 * @param lNSec nuevo número de secuencia inicial de la ventana.
 * @exception ParametroInvalidoExcepcion lanzada cuando el número de secuencia
 * pasado en el argumento no es válido.
 */
public void impReiniciar (long lNSec) throws ParametroInvalidoExcepcion
{
 if (lNSec<0)
       throw new ParametroInvalidoExcepcion ("No es válido un número de secuencia menor que cero.");
 this.lNumeroSecInicial = lNSec;
 this.lNumeroSecConsecutivo = -1;
 if (this.iCapacidadVentana!=0)
      this.treeMapTPDUDatosNormal.clear ();

}

//==========================================================================
/**
 * Obtiene el TPDUDatosNormal que en esta ventana está asociado al número de secuencia
 * indicado  (no lo elimina).
 * <p>Se retorna una referencia al TPDUDatosNormal pedido, por lo que cualquier
 * modificación que se le haga se reflejará en la ventana, y viceversa.
 * @param lNSec número de secuencia del TPDUDatosNormal.
 * @return el TPDUDatosNormal contenido en esta ventana y asociado al número de secuencia
 * indicado, o null si no hay un TPDUDatosNormal asociado o si el número de secuencia
 * no está comprendido entre el número de secuencia inicial y final de la ventana.
 */
public TPDUDatosNormal impGetTPDUDatosNormal (long lNSec)
{
if (this.iCapacidadVentana>0)
 return (TPDUDatosNormal) this.treeMapTPDUDatosNormal.get (new Long (lNSec));
return null;
} // Fin de impGetTPDUDatosNormal


//==========================================================================
/**
 * Obtiene todos los TPDUDatosNormal con número de secuencia menor o igual al
 * indicado (no los elimina). Si el nSec es mayor que el número de sec
 * final de la ventana, obtiene todos los TPDUDatosNormal que haya en la ventana.
 * <p>El vector retornado contiene las referencias a los TPDUDatosNormal pedidos,
 * por lo que cualquier modificación de los mismos se refleja en los
 * TPDUDatosNormal de la ventana, y viceversa.
 * @param lNSec número de secuencia.
 * @return vector con los TPDUDatosNormal pedidos o vacío si no hay ninguno
 */
public Vector impGetTPDUDatosNormalMenorIgual (long lNSec)
{
 Vector vector;
 long lNSecAux;
 TPDUDatosNormal pqt;

 if (this.iCapacidadVentana<=0)
   return new Vector ();

 if (lNSec>impGetNumSecFinal ())
        lNSec = impGetNumSecFinal ();

 vector = new Vector (this.treeMapTPDUDatosNormal.size());

 if (this.treeMapTPDUDatosNormal.size () > 0)
   lNSecAux = ((Long)(this.treeMapTPDUDatosNormal.firstKey ())).longValue ();
 else return vector;

 while (lNSecAux<=lNSec)
 {
  pqt = (TPDUDatosNormal)this.treeMapTPDUDatosNormal.get (new Long (lNSecAux));
  if (pqt!=null)
    vector.add (pqt);
  lNSecAux ++ ; // Incrementar nSecAux
 }

 return vector;
} // Fin de impGetTPDUDatosNormalMenorIgual


//==========================================================================
/**
 * Incrementa en una unidad el número de secuencia inicial y final, eliminando
 * el TPDUDatosNormal, si existe, asociado al número de secuencia inicial.
 * @return el TPDUDatosNormal asociado al número de secuencia inicial de la ventana,
 * o null si no hay un TPDUDatosNormal asociado.
 */
public TPDUDatosNormal impRemoveTPDUDatosNormal ()
{
 TPDUDatosNormal pqt;

  if (this.iCapacidadVentana==0)
    return null;

  if ( impContieneTPDUDatosNormal (this.impGetNumSecInicial()) )
     {
       pqt = (TPDUDatosNormal)this.treeMapTPDUDatosNormal.remove (new Long (this.impGetNumSecInicial()));
     }
  else pqt = null;

  // Actualizar el valor de numeroSecConsecutivo
  if (this.lNumeroSecConsecutivo==this.impGetNumSecInicial())
        this.lNumeroSecConsecutivo = -1;

  this.lNumeroSecInicial ++; // Incrementar el número de secuencia inicial.

  return pqt;
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
 * @param lNSec número de secuencia
 */
public void impRemoveTPDUDatosNormal (long lNSec)
{
 Long key;
 boolean bFinBucle;

 if (this.iCapacidadVentana==0)
     return;

 if (lNSec < this.lNumeroSecInicial)
   return;

 if (lNSec>=impGetNumSecFinal())
      {
       this.treeMapTPDUDatosNormal.clear ();
       this.lNumeroSecInicial += this.iCapacidadVentana;
       this.lNumeroSecConsecutivo = -1;
       return ;
      }

 bFinBucle = false;
 while ( !(bFinBucle) )
  {
   if (this.treeMapTPDUDatosNormal.size ()>0)
    {
      key = (Long)this.treeMapTPDUDatosNormal.firstKey();
      if (key.longValue()<=lNSec)
       {
         this.treeMapTPDUDatosNormal.remove (key);
       }
      else bFinBucle = true;
     }
    else bFinBucle = true;
  }

 // Actualizar el número de secuencia inicial
 this.lNumeroSecInicial = lNSec + 1;

 // Actualizar numeroSecConsecutivo
 if (this.lNumeroSecConsecutivo<this.impGetNumSecInicial())
   {
    this.lNumeroSecConsecutivo = -1;
    for (long lNSecIt = this.impGetNumSecInicial();
                                    lNSec <= this.impGetNumSecFinal();lNSecIt++)
      if (!this.impContieneTPDUDatosNormal (lNSecIt))
             break;
      else this.lNumeroSecConsecutivo = lNSecIt;
    }
}


//==========================================================================
/**
 * Obtiene la capacidad de la ventana. Éste indica cuantos pares
 * [números de secuencia,TPDUDatosNormal] caben en la ventana.
 * @return el tamaño máximo de la ventana.
 */
public int impGetCapacidadVentana ()
{
 return this.iCapacidadVentana;
}

//==========================================================================
/**
 * Obtiene el tamaño de la ventana. Éste indica cuantos pares
 * [números de secuencia,TPDUDatosNormal] hay actualmente en la ventana.
 * @return el tamaño de la ventana.
 */
public long impGetTamañoVentana ()
{
   long lTamaño = 0;
   long lNum_final = this.impGetNumSecFinal();
   long lNum_inicio = this.impGetNumSecInicial();

   // Ojo, que el número puede dar la vuelta, pasar su máximo permitido
   if(lNum_final >= lNum_inicio)
     lTamaño = lNum_final - lNum_inicio;
   else
   {
    // TRATAR CUANDO EL NÚMERO HA DADO LA VUELTA
    Long limite = new Long("9223372036854775807"); //+=1  --> 2147483648: Límite superior de un entero con signo.

    lTamaño = lNum_final + 1/* por el cero que no suma y si cuenta como número de secuencia*/
                + (limite.longValue() - lNum_inicio +1/*se suma uno porque debe de ser 2147483648*/);
   }

   return lTamaño;
}

//==========================================================================
/**
 * Redimensiona la ventana a la nueva capacidad adaptando el número de
 * secuencia final. El número de secuencia inicial no es alterado.
 * <p>Si la nueva capacidad es inferior a la anterior, se eliminan los TPDUDatosNormal
 * con número de secuencia asociado superior hasta adaptar el tamaño. Si es
 * superior, no se elimina ningún TPDUDatosNormal.
 * @param iNuevoTamañoMaximo la nueva capacidad de la ventana.
 * @return vector con TPDUDatosNormal eliminados o vacío.
 */
public Vector impCambiarCapacidadVentana (int iNuevoTamañoMaximo)
{
  int iDiferencia;
  Vector vectorEliminados = new Vector ();
  TPDUDatosNormal pqt;
  Long ultimaClave;
  long lNSec;
  boolean bFin;


  if (iNuevoTamañoMaximo<0)
    return vectorEliminados;

  if (iNuevoTamañoMaximo==0)
    {
     this.iCapacidadVentana = 0;
     this.lNumeroSecConsecutivo = -1;
     return vectorEliminados;
     }

  if (this.iCapacidadVentana==0)
  {
    this.treeMapTPDUDatosNormal = new TreeMap ();
    this.iCapacidadVentana = iNuevoTamañoMaximo;
    return vectorEliminados;
  }

  // Hay que eliminar TPDUDatosNormal.
  if (iNuevoTamañoMaximo < this.iCapacidadVentana)
   {
    // Hay que eliminar desde el número de sec. final hasta el calculado
    // en nSec (inclusive).
    lNSec = impGetNumSecFinal () - (this.iCapacidadVentana - iNuevoTamañoMaximo) + 1;

    // Actualizar el numeroSecConsecutivo
    if (this.lNumeroSecConsecutivo >= lNSec)
        this.lNumeroSecConsecutivo = lNSec - 1;

    bFin = false;
    while ((this.treeMapTPDUDatosNormal.size()>0)&&(bFin==false))
     {
      ultimaClave = (Long)this.treeMapTPDUDatosNormal.lastKey ();
      if (ultimaClave.longValue()<lNSec)
        bFin = true;
      else
         vectorEliminados.add (this.treeMapTPDUDatosNormal.remove(ultimaClave));
     }
   }

  this.iCapacidadVentana = iNuevoTamañoMaximo;

  return vectorEliminados;
} // Fin de cambiarTamañoVentana


//==========================================================================
/**
 * De entre los números de secuencia de la ventana con TPDUDatosNormal asociado, devuelve
 * el mayor.
 * @return número de secuencia mayor con TPDUDatosNormal asociado, o -1 si ningún
 * número de secuencia de la ventana tiene TPDUDatosNormal asociado.
 */
public long impGetNumSecMayor ()
{

 if (this.iCapacidadVentana==0)
    return -1;

 if (this.treeMapTPDUDatosNormal.size()>0)
        return ((Long)this.treeMapTPDUDatosNormal.lastKey()).longValue();
 return -1;
}


public String toString ()
{
  return this.treeMapTPDUDatosNormal.toString();
}

// ATRIBUTOS


/**
 * Indica cuantos pares [números de secuencia,TPDUDatosNormal] caben en la ventana.
 */
private int iCapacidadVentana = 0;


/**
 * TreeMap que almacena los TPDUDatosNormal de la ventana, crece de forma
 * dinámica cada vez que se añade un TPDUDatosNormal. Los TPDUDatosNormal se ordenan
 * por número de secuencia.
 * <p>Garantiza una complejidad log(n) para las operaciones de búsqueda
 * por clave (número de secuencia).
 * <table border=1>
 *  <tr>  <td><b>Key:</b></td>
 *	    <td>Objeto Long con el número de secuencia</td>
 *  </tr>
 *  <tr>  <td><b>Value:</b></td>
 *	    <td>{@link TPDUDatosNormal}</td>
 *  </tr>
 * </table>
 *
 */
private TreeMap treeMapTPDUDatosNormal = null;

/**
 * Número de secuencia inicial de la ventana.
 */
private long lNumeroSecInicial;

/**
 * Número de secuencia mayor, consecutivo. Si su valor es -1, indica que
 * no hay ningún número de secuencia consecutivo, lo cual, sólo es posible
 * si no hay un TPDUDatosNormal asociado al número de secuencia inicial de la ventana.
 */
private long lNumeroSecConsecutivo;

}
