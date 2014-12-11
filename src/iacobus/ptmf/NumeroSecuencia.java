//============================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	File: NumeroSecuencia.java  1.0
//
//	Descripción: Clase NumeroSecuencia. Operaciones y almacen para
//                   manejar un número de secuencia.
//
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

package iacobus.ptmf;

import java.lang.Cloneable;
import java.lang.Comparable;


/**
 * Clase que almacena un número de secuencia y ofrece las operaciones
 * necesarias para su manejo. <br>
 * Un número de secuencia está formado por un entero de 32 bits (sin signo)
 * que identifica un {@link TPDUDatosNormal} enviado por un id_socket.<br>
 *
 * <b>Una vez creado no puede ser modificado.</b>
 * @version 1.0
 * @author Antonio Berrocal Piris
 * <A HREF="mailto:AntonioBP@wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
 * M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 */
public class NumeroSecuencia implements Cloneable,Comparable

{

 /** Almacena el valor del número de secuencia. */
  private long lNumeroSecuencia = 1;

  //=================================================================
  /**
   * Crea un objeto número de secuencia con el valor indicado.
   * @param numSec valor del número de secuencia
   * @exception ParametroInvalidoExcepcion lanzada si el número de secuencia no es
   * válido.
   */
   public NumeroSecuencia (long lNumSec) throws ParametroInvalidoExcepcion
   {
    final String mn = "NumeroSecuencia.Numerosecuencia(long)";
    if (esValido (lNumSec))
        this.lNumeroSecuencia = lNumSec;
    else throw new ParametroInvalidoExcepcion ("El número de secuencia " + lNumSec +
                                                " no es válido.");
   }

  //=================================================================
  /**
   * Comprueba que el número de secuencia indicado es válido, para lo cual
   * debe ser mayor o igual a cero.
   * @param numSec número de secuencia
   * @return true si el número de secuencia es válido, false en caso contrario.
   */
  private boolean esValido (long lNumSec)
  {
   if (lNumSec < 0)
        return false;
   return true;
  }

  //=================================================================
  /**
   * Devuelve el número de secuencia como un long.
   * @return número de secuencia como long
   */
  public long tolong()
  {
     return this.lNumeroSecuencia;
  }

   //=================================================================
   /**
    * Implementa el método de la interfaz {@link Comparable}.
    * @param o número de secuencia para comparar con este, si no es una
    * instancia de {@link NumeroSecuencia} se lanzará la excepción
    * {@link java.lang.ClassCastException}.
    * @return -1 si este número de secuencia es menor que el dado,
    * 1 si es mayor y 0 si son iguales.
    */
   public int compareTo(Object o)
   {
    NumeroSecuencia ns = (NumeroSecuencia) o;

    if (this.lNumeroSecuencia<ns.tolong())
        return -1;
    if (this.lNumeroSecuencia>ns.tolong())
        return 1;
    return 0;
   }

   //=================================================================
   /**
    * Comprueba si este número de secuencia es igual al pasado por parámetro.
    * @return true si son iguales, y false en caso contrario.
    */
   public boolean equals (Object o)
   {
    NumeroSecuencia ns = (NumeroSecuencia) o;

    if (this.lNumeroSecuencia==ns.tolong())
        return true;
    return false;
   }

   //=================================================================
   /**
    * Comprueba si este número de secuencia es igual al pasado por parámetro.
    * @return true si son iguales, y false en caso contrario.
    */
   public boolean igual (NumeroSecuencia o)
   {
    if (this.compareTo(o)==0)
        return true;
    return false;
   }

   //=================================================================
   /**
    * Comprueba si este número de secuencia es mayor o igual al pasado por parámetro.
    * @return true si son iguales, y false en caso contrario.
    */
   public boolean mayorIgual (NumeroSecuencia o)
   {
    if (this.compareTo(o)>=0)
        return true;
    return false;
   }

   //=================================================================
   /**
    * Comprueba si este número de secuencia es mayor al pasado por parámetro.
    * @return true si es mayor, y false en caso contrario.
    */
   public boolean mayor (NumeroSecuencia o)
   {
    if (this.compareTo (o)>0)
        return true;
    return false;
   }

   //=================================================================
   /**
    * Comprueba si este número de secuencia es menor al pasado por parámetro.
    * @return true si es menor, y false en caso contrario.
    */
   public boolean menor (NumeroSecuencia o)
   {
    if (this.compareTo (o)<0)
        return true;
    return false;
   }

   //=================================================================
   /**
    * Comprueba si este número de secuencia es menor o igual al pasado por parámetro.
    * @return true si es menor, y false en caso contrario.
    */
   public boolean menorIgual (NumeroSecuencia o)
   {
    if (this.compareTo (o)<=0)
        return true;
    return false;
   }

   //=================================================================
   /**
    * Devuelve el número de secuencia siguiente.
    * @return número de secuencia siguiente
    */
   public long getSiguiente ()
   {
     return (this.lNumeroSecuencia+1);
   }

   //=================================================================
   /**
    * Devuelve el número de secuencia anterior, o 0 si no existe.
    * @return número de secuencia anterior, o 0 si no existe.
    */
   public NumeroSecuencia getAnterior () throws PTMFExcepcion
   {
    try {
      if (this.lNumeroSecuencia>0)
            return new NumeroSecuencia (this.lNumeroSecuencia-1);
     }catch (ParametroInvalidoExcepcion e)
         {throw new PTMFExcepcion (e.toString());}

     throw new PTMFExcepcion ("El número de secuencia no es válido.");
   }

   //=================================================================
   /**
    * Incrementa el número de secuencia en la cantidad indicada.
    * Si cantidad es menor que cero, no se incrementa.
    * @param iCantidad en que se incrementa el número de secuencia
    * @exception ParametroInvalidoExcepcion
    */
   public NumeroSecuencia incrementar (long lCantidad)
      throws ParametroInvalidoExcepcion
   {
    return new NumeroSecuencia (this.lNumeroSecuencia+lCantidad);
   }

   //=================================================================
   /**
    * Decrementa el número de secuencia en una unidad si es mayor que cero.
    * @throws  PTMFExcepcion
    */
   public NumeroSecuencia decrementar ()
      throws PTMFExcepcion
   {
    // Al crearse el nuevo número se lanza la excepción si no es válido.
    try{
      return new NumeroSecuencia (this.lNumeroSecuencia-1);
     }catch (ParametroInvalidoExcepcion e)
        {throw new PTMFExcepcion (e.toString());}
   }

   //=================================================================
   /**
    * Crea un copia de este número de secuencia. Lo que devulve no es una referencia
    * a este objeto, sino un nuevo objeto cuyos datos son copias de este.
    * @return número de secuencia clon de este.
    */
   public Object clone()
   {
    final String mn = "NumeroSecuencia.clone";
     try{
       return new NumeroSecuencia (this.lNumeroSecuencia);
      }catch (ParametroInvalidoExcepcion e)
             {}
     return null;

   }

   //=================================================================
   /**
    * Devuelve el número de secuencia mayor posible (LIMITESUPERIOR).
    */
   public static NumeroSecuencia LIMITESUPERIOR;

   static {
    try
    {
     LIMITESUPERIOR = new NumeroSecuencia (Long.MAX_VALUE-1);
    }
    catch(java.lang.Exception e)
    {
      ;
    }
   }

   //=================================================================
   /**
    * Devuelve el número de secuencia menor posible (LIMITEINFERIOR).
    */
   public static NumeroSecuencia LIMITEINFERIOR;

   static {
     try
     {
      LIMITEINFERIOR = new NumeroSecuencia (1);
     }
     catch(java.lang.Exception e)
     {
      ;
     }
   }

   //=================================================================
   /**
    * Devuelve una cadena representación del número de secuencia.
    * @return cadena representación del número de secuencia.
    */
   public String toString ()
   {
    return "NSec: " + this.lNumeroSecuencia;
   }




}
