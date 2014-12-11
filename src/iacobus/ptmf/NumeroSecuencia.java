//============================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	File: NumeroSecuencia.java  1.0
//
//	Descripci�n: Clase NumeroSecuencia. Operaciones y almacen para
//                   manejar un n�mero de secuencia.
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
 * Clase que almacena un n�mero de secuencia y ofrece las operaciones
 * necesarias para su manejo. <br>
 * Un n�mero de secuencia est� formado por un entero de 32 bits (sin signo)
 * que identifica un {@link TPDUDatosNormal} enviado por un id_socket.<br>
 *
 * <b>Una vez creado no puede ser modificado.</b>
 * @version 1.0
 * @author Antonio Berrocal Piris
 * <A HREF="mailto:AntonioBP@wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
 * M. Alejandro Garc�a Dom�nguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 */
public class NumeroSecuencia implements Cloneable,Comparable

{

 /** Almacena el valor del n�mero de secuencia. */
  private long lNumeroSecuencia = 1;

  //=================================================================
  /**
   * Crea un objeto n�mero de secuencia con el valor indicado.
   * @param numSec valor del n�mero de secuencia
   * @exception ParametroInvalidoExcepcion lanzada si el n�mero de secuencia no es
   * v�lido.
   */
   public NumeroSecuencia (long lNumSec) throws ParametroInvalidoExcepcion
   {
    final String mn = "NumeroSecuencia.Numerosecuencia(long)";
    if (esValido (lNumSec))
        this.lNumeroSecuencia = lNumSec;
    else throw new ParametroInvalidoExcepcion ("El n�mero de secuencia " + lNumSec +
                                                " no es v�lido.");
   }

  //=================================================================
  /**
   * Comprueba que el n�mero de secuencia indicado es v�lido, para lo cual
   * debe ser mayor o igual a cero.
   * @param numSec n�mero de secuencia
   * @return true si el n�mero de secuencia es v�lido, false en caso contrario.
   */
  private boolean esValido (long lNumSec)
  {
   if (lNumSec < 0)
        return false;
   return true;
  }

  //=================================================================
  /**
   * Devuelve el n�mero de secuencia como un long.
   * @return n�mero de secuencia como long
   */
  public long tolong()
  {
     return this.lNumeroSecuencia;
  }

   //=================================================================
   /**
    * Implementa el m�todo de la interfaz {@link Comparable}.
    * @param o n�mero de secuencia para comparar con este, si no es una
    * instancia de {@link NumeroSecuencia} se lanzar� la excepci�n
    * {@link java.lang.ClassCastException}.
    * @return -1 si este n�mero de secuencia es menor que el dado,
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
    * Comprueba si este n�mero de secuencia es igual al pasado por par�metro.
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
    * Comprueba si este n�mero de secuencia es igual al pasado por par�metro.
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
    * Comprueba si este n�mero de secuencia es mayor o igual al pasado por par�metro.
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
    * Comprueba si este n�mero de secuencia es mayor al pasado por par�metro.
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
    * Comprueba si este n�mero de secuencia es menor al pasado por par�metro.
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
    * Comprueba si este n�mero de secuencia es menor o igual al pasado por par�metro.
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
    * Devuelve el n�mero de secuencia siguiente.
    * @return n�mero de secuencia siguiente
    */
   public long getSiguiente ()
   {
     return (this.lNumeroSecuencia+1);
   }

   //=================================================================
   /**
    * Devuelve el n�mero de secuencia anterior, o 0 si no existe.
    * @return n�mero de secuencia anterior, o 0 si no existe.
    */
   public NumeroSecuencia getAnterior () throws PTMFExcepcion
   {
    try {
      if (this.lNumeroSecuencia>0)
            return new NumeroSecuencia (this.lNumeroSecuencia-1);
     }catch (ParametroInvalidoExcepcion e)
         {throw new PTMFExcepcion (e.toString());}

     throw new PTMFExcepcion ("El n�mero de secuencia no es v�lido.");
   }

   //=================================================================
   /**
    * Incrementa el n�mero de secuencia en la cantidad indicada.
    * Si cantidad es menor que cero, no se incrementa.
    * @param iCantidad en que se incrementa el n�mero de secuencia
    * @exception ParametroInvalidoExcepcion
    */
   public NumeroSecuencia incrementar (long lCantidad)
      throws ParametroInvalidoExcepcion
   {
    return new NumeroSecuencia (this.lNumeroSecuencia+lCantidad);
   }

   //=================================================================
   /**
    * Decrementa el n�mero de secuencia en una unidad si es mayor que cero.
    * @throws  PTMFExcepcion
    */
   public NumeroSecuencia decrementar ()
      throws PTMFExcepcion
   {
    // Al crearse el nuevo n�mero se lanza la excepci�n si no es v�lido.
    try{
      return new NumeroSecuencia (this.lNumeroSecuencia-1);
     }catch (ParametroInvalidoExcepcion e)
        {throw new PTMFExcepcion (e.toString());}
   }

   //=================================================================
   /**
    * Crea un copia de este n�mero de secuencia. Lo que devulve no es una referencia
    * a este objeto, sino un nuevo objeto cuyos datos son copias de este.
    * @return n�mero de secuencia clon de este.
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
    * Devuelve el n�mero de secuencia mayor posible (LIMITESUPERIOR).
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
    * Devuelve el n�mero de secuencia menor posible (LIMITEINFERIOR).
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
    * Devuelve una cadena representaci�n del n�mero de secuencia.
    * @return cadena representaci�n del n�mero de secuencia.
    */
   public String toString ()
   {
    return "NSec: " + this.lNumeroSecuencia;
   }




}
