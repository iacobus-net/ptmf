//============================================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: ID_Socket.java  1.0 21/10/99
//
//	Description: ID_Socket
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
//----------------------------------------------------------------------------

package net.iacobus.ptmf;

import java.util.Comparator;

/**
 * ID_Socket identifica a un Socket PTMF dentro de Internet.
 * Los datos para identificar un Socket PTMF son:
 * <UL>
 * <IL> Una dirección IPv4 </IL>
 * <IL> Un Puerto Unicast </IL>
 * </UL>
 * @version  1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */
 public class ID_Socket implements Cloneable, Comparable{

  /** dirección ipv4 */
   IPv4 ipv4 = null;

  /** Puerto Unicast */
  private int PuertoUnicast = 0;

  //==========================================================================
  /**
   * Constructor.
   * @param id Buffer de bytes del identificador de grupo
   * @param ttl TTL del id de grupo.
   */
  ID_Socket(IPv4 ipv4, int puerto)
  {
    super();

    this.ipv4 = ipv4;
    this.PuertoUnicast = puerto;
  }

  //==========================================================================
  /**
   * Devuelve una referencia a la dirección del Socket
   */
  IPv4 getDireccion ()
  {
   return this.ipv4;
  }

  //==========================================================================
  /**
   * Devuelve el puerto unicast del socket.
   */
  int getPuertoUnicast ()
  {
   return this.PuertoUnicast;
  }

  //==========================================================================
  /**
   * Método clone.
   */
  public Object clone()
  {
   return ( new ID_Socket((IPv4)this.ipv4.clone(),this.PuertoUnicast));
  }


  //==========================================================================
  /**
   * Este método verifica si el objeto pasado como argumento es igual a este
   * objeto. Se comparan el IPv4 y el PuertoUnicast
   * @param obj Objeto a comparar con este.
   * @return true si el objeto es igual, false en caso contrario.
   */
  public boolean equals(Object obj)
  {
    if (this.compareTo(obj)==0)
        return true;
    else
        return false;
  }


  //==========================================================================
  /**
   * Implementación del método de la interfaz Comparable.
   * Compara primero con Ipv4 y después el Puerto Unicast.
   * @param o IDGL con la que se compara.
   * @return mayor que cero si este IDGL es mayor que el pasado en el
   * argumento, menor que cero si es menor y cero si son iguales.
   */
 public int compareTo(Object obj)
 {
    int hash1 = 0;
    int hash2 = 0;
    ID_Socket id = (ID_Socket) obj;

    hash1 = this.ipv4.hashCode();
    hash2 = id.ipv4.hashCode();

    if ( hash1 < hash2 )
        return -1;
    else if ( hash1 > hash2)
        return 1;
    else
    {
     if (this.PuertoUnicast < id.PuertoUnicast)
      return -1;
     else if (this.PuertoUnicast > id.PuertoUnicast)
      return 1;
     else
      return 0;
    }

 }


  //==========================================================================
  /**
   * Devuelve una cadena identificativa del objeto.
   * @return Cadena Indentificativa.
   */
  public String toString ()
  {
   return (""+this.ipv4+":"+this.PuertoUnicast);
  }


}
