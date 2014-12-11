//============================================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	File: IPv4.java  1.0 21/10/99
//
//
//	Descripción: IPv4
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
//
//----------------------------------------------------------------------------

package iacobus.ptmf;

import java.util.Comparator;

/**
 * Clase IPv4, encapsula la dirección IPv4 de un host
 * @version  1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */
 class IPv4 implements Cloneable,Comparable{

  /** bytes de la dirección*/
  Buffer ipv4 = null;

  /** dirección IPv4 como una cadena x.x.x.x */
  private String sHostAddress = null;

  /** HashCode */
  private int iHashCode = 0;

  //==========================================================================
  /**
   * Constructor.
   * @param ip Buffer de bytes con la dirección IPv4
   */
  IPv4(Buffer ip)
  {
    super();

    this.ipv4 = ip;

    //Obtener Host Address...
    if(this.ipv4 != null)
    {
     short A = 0;
     short B = 0;
     short C = 0;
     short D = 0;

     try
     {
       A = this.ipv4.getByte(0);
       B = this.ipv4.getByte(1);
       C = this.ipv4.getByte(2);
       D = this.ipv4.getByte(3);
     }
     catch(ParametroInvalidoExcepcion e)
     { ; }

     sHostAddress =  A+"."+B+"."+C+"."+D  ;
    }

    //Calcular HAshCode...
    try
    {
     if ( (this.ipv4 != null) && (this.ipv4.getBuffer() != null))
      iHashCode = ((int)this.ipv4.getInt(0));
    }
    catch(ParametroInvalidoExcepcion e){;}

  }

  //==========================================================================
  /**
   * Devuelve la dirección IPv4 como una cadena x.x.x.x
   * @return Una cadena en formato x.x.x.x
   */
   String getHostAddress()
   {
     return this.sHostAddress;
   }

  //==========================================================================
  /**
   * Devuelve el código hash para este IP.
   * @return el código hash para el IP
   */
  public int hashCode()
  {
    return this.iHashCode;
  }

  //==========================================================================
  /**
   * Este método verifica si el objeto pasado como argumento es igual a este
   * objeto.
   * @param obj Objeto a comparar con este.
   * @return true si el objeto es igual, false en caso contrario.
   */
  public boolean equals(Object obj)
  {
    IPv4 ip = (IPv4) obj;

    if(this.hashCode()!= ip.hashCode())
       return false;
    else
       return true;
  }

  //==========================================================================
  /**
   * Este método compara dos objetos pasados como argumentos.
   * @param o1 Objeto 1 a comparar
   * @param o2 Objeto 2 a comparar
   * @return Devuelve un entero negativo, cero o un entero positivo si el
   *   primer argumento es menor que, igual a, o mayor que el segundo.
   */
  public int compare(Object o1, Object o2)
  {
    int hash1 = 0;
    int hash2 = 0;
    IPv4 ipv4_1 = (IPv4) o1;
    IPv4 ipv4_2 = (IPv4) o2;

    //Log.log("","En compare TIO");
    hash1 = ipv4_1.hashCode();
    hash2 = ipv4_2.hashCode();
    Log.log("hash1",""+hash1);
    Log.log("hash1",""+hash2);

    if ( hash1 < hash2 )
        return -1;
    else if ( hash1 > hash2)
        return 1;
    else return 0;
  }

  //==========================================================================
  /**
   * Implementación del método de la interfaz Comparable.
   * Compara primero la dirección y después el número de secuencia.
   * @param o IPv4 con la que se compara.
   * @return mayor que cero si este IPv4 es mayor que el pasado en el
   * argumento, menor que cero si es menor y cero si son iguales.
   */
 public int compareTo(Object o)
 {
    int hash1 = 0;
    int hash2 = 0;
    IPv4 ipv4_1 = (IPv4) o;

    hash1 = this.hashCode();
    hash2 = ipv4_1.hashCode();

    if ( hash1 < hash2 )
        return -1;
    else if ( hash1 > hash2)
        return 1;
    else return 0;
  }

  //==========================================================================
  /**
   * Devuelve una cadena ipv4entificativa del objeto.
   * @return Cadena Indentificativa.
   */

  public String toString ()
  {
   return ("IP: " + getHostAddress());
  }

  //==========================================================================
  /**
   * Implementación de la interfaz Cloneable. Método clone
   * @return El nuevo objeto clonado.
   */
  public Object clone()
  {
   return new IPv4((Buffer)this.ipv4.clone());
  }

}
