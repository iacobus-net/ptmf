//============================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	File: ImpVentanaTreeMap.java  1.0
//
//
//	Descripci�n: Clase ImpVentanaTreeMap. Implementa la clase abstracta
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
 * con n�mero de secuencia comprendidos entre el n�mero de secuencia inicial y
 * final de la ventana (inclusives).
 * <p>El espacio de memoria para almacenar los TPDUDatosNormal se localiza de
 * forma din�mica.
 * Es f�cil de modificar para que permita un n�mero ilimitado de
 * TPDUDatosNormal, puesto que no reserva el espacio previamente, si no
 * conforme lo va necesitando.
 * <p><b>Esta clase no est� preparada para trabajar concurrentemente. (hacer???)<\b>
 * De la clase TPDUDatosNormal no utiliza, nada m�s que su nombre, por lo que puede
 * ser enteramente modificada sin afectar a esta clase.
 * Almacena el n�mero de secuencia como un long.
 * No guarda TPDUDatosNormal null.
 * @version  1.0
 * @author Antonio Berrocal Piris
 * <A HREF="mailto:AntonioBP@wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
 * M. Alejandro Garc�a Dom�nguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 */
public class ImpVentanaTreeMap extends ImpVentana
{

//==========================================================================
/**
 * Crea una ventana con el tama�o m�ximo especificado.
 * Si el tama�o es menor que cero la ventana tendr� un tama�o m�ximo de cero.
 * @param tama�o indica el n�mero m�ximo de TPDUDatosNormal que caben en la ventana.
 * @throws ParametroInvalidoExcepcion lanzada si el n�mero de secuencia no es v�lido.
 * @see OutOfMemoryError
 */
public ImpVentanaTreeMap (int iTama�oMaximo,long lNSecInic) throws ParametroInvalidoExcepcion
{

 if (lNSecInic<0)
     { this.iCapacidadVentana = 0;
       this.lNumeroSecInicial = -1;
       throw new ParametroInvalidoExcepcion ("N�mero de sec " + lNSecInic
                                              + " no es v�lido.");
     }

 if (iTama�oMaximo>0)
 {

  this.treeMapTPDUDatosNormal = new TreeMap (); // Almacenar� los TPDUDatosNormal ordenados por n�mero de secuencia.
  if (this.treeMapTPDUDatosNormal == null)
       throw new OutOfMemoryError ("No se pudo localizar memoria para crear la ventana.");
   this.iCapacidadVentana = iTama�oMaximo;
  }
  else this.iCapacidadVentana = 0;

  this.lNumeroSecInicial = lNSecInic;
  this.lNumeroSecConsecutivo = -1;

}


//==========================================================================
/**
 * Devuelve el n�mero de secuencia inicial de la ventana.
 * @return n�mero de secuencia inicial de la ventana.
 */
public long impGetNumSecInicial ()
{
 return this.lNumeroSecInicial;
}

//==========================================================================
/**
 * Devuelve el n�mero de secuencia final de la ventana.
 * <p>Devuelve un n�mero negativo en caso de que el tama�o m�ximo de la ventana
 * sea cero (no existe n�mero de secuencia final).
 * @return n�mero de secuencia final de la ventana, -1 en si el tama�o
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
 * Devuelve el n�mero de secuencia m�s alto que verifica que todos los menores
 * o iguales a �l tiene un TPDUDatosNormal asociado.
 * @return el n�mero de secuencia mayor consecutivo, o -1 si no lo hay.
 */
public long impGetNumSecConsecutivo ()
{
 return this.lNumeroSecConsecutivo;
}

//==========================================================================
/**
 * Indica si la ventana contiene un TPDUDatosNormal con el n�mero de secuencia
 * indicado.
 * @param nSec n�mero de secuencia del TPDUDatosNormal que se quiere comprobar.
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
 * A�ade un TPDUDatosNormal a la ventana. El n�mero de secuencia debe estar comprendido
 * entre el n�mero de secuencia inicial y final de la ventana.
 * <p>El n�mero de secuencia indicado ser� el que se asocie al TPDUDatosNormal por lo
 * que este deber� corresponder con el n�mero de secuencia verdadero del TPDUDatosNormal.
 * @param lNSec n�mero de secuencia del TPDUDatosNormal que se quiere a�adir.
 * @param pqt TPDUDatosNormal a a�adir
 * @return el TPDUDatosNormal, si existe, que estaba asociado al n�mero de secuencia indicado,
 * o null si no existe.
 * @exception ParametroInvalidoExcepcion excepci�n lanzada cuando el n�mero de
 * secuencia no est� comprendido entre el n�mero de secuencia inicial y final
 * de la ventana.
 */
public TPDUDatosNormal impAddTPDUDatosNormal (long lNSec,TPDUDatosNormal pqt)
                                        throws ParametroInvalidoExcepcion
{
 long iIndice;
 TPDUDatosNormal pqtAntiguo = null;


 if (pqt==null)
   return null;

 // Comprobar si el TPDUDatosNormal pertenece a la ventana (hacer excepci�n).
 if ((lNSec>impGetNumSecFinal())||(lNSec<impGetNumSecInicial()))
     throw new ParametroInvalidoExcepcion ("El n�mero de secuencia "+lNSec+
                        ", no puede ser a�adido a esta ventana.");

 pqtAntiguo = (TPDUDatosNormal)this.treeMapTPDUDatosNormal.put(new Long(lNSec),pqt);

 // Comparar si se ha modificado el numeroSecConsecutivo
 if (this.lNumeroSecConsecutivo == -1)
   // Comprobar cual es el m�s alto consecutivo
   {
    for (long lNSecIt = this.impGetNumSecInicial();
                                    lNSec <= this.impGetNumSecFinal();lNSecIt++)
         if (!this.impContieneTPDUDatosNormal (lNSecIt))
                break;
         else this.lNumeroSecConsecutivo = lNSecIt;
   }
 else { // Ver si es continuaci�n del ya registrado
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
 * Elimina todos los TPDUDatosNormal de la ventana y fija un nuevo n�mero inicial.
 * El tama�o m�ximo de la ventana no es alterado.
 * @param lNSec nuevo n�mero de secuencia inicial de la ventana.
 * @exception ParametroInvalidoExcepcion lanzada cuando el n�mero de secuencia
 * pasado en el argumento no es v�lido.
 */
public void impReiniciar (long lNSec) throws ParametroInvalidoExcepcion
{
 if (lNSec<0)
       throw new ParametroInvalidoExcepcion ("No es v�lido un n�mero de secuencia menor que cero.");
 this.lNumeroSecInicial = lNSec;
 this.lNumeroSecConsecutivo = -1;
 if (this.iCapacidadVentana!=0)
      this.treeMapTPDUDatosNormal.clear ();

}

//==========================================================================
/**
 * Obtiene el TPDUDatosNormal que en esta ventana est� asociado al n�mero de secuencia
 * indicado  (no lo elimina).
 * <p>Se retorna una referencia al TPDUDatosNormal pedido, por lo que cualquier
 * modificaci�n que se le haga se reflejar� en la ventana, y viceversa.
 * @param lNSec n�mero de secuencia del TPDUDatosNormal.
 * @return el TPDUDatosNormal contenido en esta ventana y asociado al n�mero de secuencia
 * indicado, o null si no hay un TPDUDatosNormal asociado o si el n�mero de secuencia
 * no est� comprendido entre el n�mero de secuencia inicial y final de la ventana.
 */
public TPDUDatosNormal impGetTPDUDatosNormal (long lNSec)
{
if (this.iCapacidadVentana>0)
 return (TPDUDatosNormal) this.treeMapTPDUDatosNormal.get (new Long (lNSec));
return null;
} // Fin de impGetTPDUDatosNormal


//==========================================================================
/**
 * Obtiene todos los TPDUDatosNormal con n�mero de secuencia menor o igual al
 * indicado (no los elimina). Si el nSec es mayor que el n�mero de sec
 * final de la ventana, obtiene todos los TPDUDatosNormal que haya en la ventana.
 * <p>El vector retornado contiene las referencias a los TPDUDatosNormal pedidos,
 * por lo que cualquier modificaci�n de los mismos se refleja en los
 * TPDUDatosNormal de la ventana, y viceversa.
 * @param lNSec n�mero de secuencia.
 * @return vector con los TPDUDatosNormal pedidos o vac�o si no hay ninguno
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
 * Incrementa en una unidad el n�mero de secuencia inicial y final, eliminando
 * el TPDUDatosNormal, si existe, asociado al n�mero de secuencia inicial.
 * @return el TPDUDatosNormal asociado al n�mero de secuencia inicial de la ventana,
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

  this.lNumeroSecInicial ++; // Incrementar el n�mero de secuencia inicial.

  return pqt;
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
 * @param lNSec n�mero de secuencia
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

 // Actualizar el n�mero de secuencia inicial
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
 * Obtiene la capacidad de la ventana. �ste indica cuantos pares
 * [n�meros de secuencia,TPDUDatosNormal] caben en la ventana.
 * @return el tama�o m�ximo de la ventana.
 */
public int impGetCapacidadVentana ()
{
 return this.iCapacidadVentana;
}

//==========================================================================
/**
 * Obtiene el tama�o de la ventana. �ste indica cuantos pares
 * [n�meros de secuencia,TPDUDatosNormal] hay actualmente en la ventana.
 * @return el tama�o de la ventana.
 */
public long impGetTama�oVentana ()
{
   long lTama�o = 0;
   long lNum_final = this.impGetNumSecFinal();
   long lNum_inicio = this.impGetNumSecInicial();

   // Ojo, que el n�mero puede dar la vuelta, pasar su m�ximo permitido
   if(lNum_final >= lNum_inicio)
     lTama�o = lNum_final - lNum_inicio;
   else
   {
    // TRATAR CUANDO EL N�MERO HA DADO LA VUELTA
    Long limite = new Long("9223372036854775807"); //+=1  --> 2147483648: L�mite superior de un entero con signo.

    lTama�o = lNum_final + 1/* por el cero que no suma y si cuenta como n�mero de secuencia*/
                + (limite.longValue() - lNum_inicio +1/*se suma uno porque debe de ser 2147483648*/);
   }

   return lTama�o;
}

//==========================================================================
/**
 * Redimensiona la ventana a la nueva capacidad adaptando el n�mero de
 * secuencia final. El n�mero de secuencia inicial no es alterado.
 * <p>Si la nueva capacidad es inferior a la anterior, se eliminan los TPDUDatosNormal
 * con n�mero de secuencia asociado superior hasta adaptar el tama�o. Si es
 * superior, no se elimina ning�n TPDUDatosNormal.
 * @param iNuevoTama�oMaximo la nueva capacidad de la ventana.
 * @return vector con TPDUDatosNormal eliminados o vac�o.
 */
public Vector impCambiarCapacidadVentana (int iNuevoTama�oMaximo)
{
  int iDiferencia;
  Vector vectorEliminados = new Vector ();
  TPDUDatosNormal pqt;
  Long ultimaClave;
  long lNSec;
  boolean bFin;


  if (iNuevoTama�oMaximo<0)
    return vectorEliminados;

  if (iNuevoTama�oMaximo==0)
    {
     this.iCapacidadVentana = 0;
     this.lNumeroSecConsecutivo = -1;
     return vectorEliminados;
     }

  if (this.iCapacidadVentana==0)
  {
    this.treeMapTPDUDatosNormal = new TreeMap ();
    this.iCapacidadVentana = iNuevoTama�oMaximo;
    return vectorEliminados;
  }

  // Hay que eliminar TPDUDatosNormal.
  if (iNuevoTama�oMaximo < this.iCapacidadVentana)
   {
    // Hay que eliminar desde el n�mero de sec. final hasta el calculado
    // en nSec (inclusive).
    lNSec = impGetNumSecFinal () - (this.iCapacidadVentana - iNuevoTama�oMaximo) + 1;

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

  this.iCapacidadVentana = iNuevoTama�oMaximo;

  return vectorEliminados;
} // Fin de cambiarTama�oVentana


//==========================================================================
/**
 * De entre los n�meros de secuencia de la ventana con TPDUDatosNormal asociado, devuelve
 * el mayor.
 * @return n�mero de secuencia mayor con TPDUDatosNormal asociado, o -1 si ning�n
 * n�mero de secuencia de la ventana tiene TPDUDatosNormal asociado.
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
 * Indica cuantos pares [n�meros de secuencia,TPDUDatosNormal] caben en la ventana.
 */
private int iCapacidadVentana = 0;


/**
 * TreeMap que almacena los TPDUDatosNormal de la ventana, crece de forma
 * din�mica cada vez que se a�ade un TPDUDatosNormal. Los TPDUDatosNormal se ordenan
 * por n�mero de secuencia.
 * <p>Garantiza una complejidad log(n) para las operaciones de b�squeda
 * por clave (n�mero de secuencia).
 * <table border=1>
 *  <tr>  <td><b>Key:</b></td>
 *	    <td>Objeto Long con el n�mero de secuencia</td>
 *  </tr>
 *  <tr>  <td><b>Value:</b></td>
 *	    <td>{@link TPDUDatosNormal}</td>
 *  </tr>
 * </table>
 *
 */
private TreeMap treeMapTPDUDatosNormal = null;

/**
 * N�mero de secuencia inicial de la ventana.
 */
private long lNumeroSecInicial;

/**
 * N�mero de secuencia mayor, consecutivo. Si su valor es -1, indica que
 * no hay ning�n n�mero de secuencia consecutivo, lo cual, s�lo es posible
 * si no hay un TPDUDatosNormal asociado al n�mero de secuencia inicial de la ventana.
 */
private long lNumeroSecConsecutivo;

}
