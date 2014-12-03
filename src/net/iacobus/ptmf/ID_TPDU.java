//============================================================================
//
//	Copyright (c) 1999 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	File: ID_TPDU.java  1.0 24/10/99
//
//	Description: Clase ID_TPDU.  Identificador de TPDU de datos.
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
import java.lang.Comparable;



/**
 * Par formado por el id_socket fuente (identificador del socket fuente) y por
 * el número de secuencia. Identifica un TPDU de datos en el grupo Multicast.
 *
 * Los datos para identificar un ID_TPDU:
 * <UL>
 * <IL> {@link ID_Socket ID_Socket (IPv4 + Puerto Unicast)}</IL>
 * <IL> {@link NumeroSecuencia Número de Secuencia} </IL>
 * </UL>
 *
 * Una vez creado un objeto ID_TPDU no puede ser modificado.
 *
 * @version  1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 * Antonio Berrocal Piris
 * <A HREF="mailto:AntonioBP.wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
 */
public class ID_TPDU implements Cloneable,Comparable
{
 // ATRIBUTOS

 /** Identificador del socket Fuente */
 private ID_Socket id_Socket = null;

 /** Número de secuencia */
 private NumeroSecuencia nSec = null;

   //=========================================================================
   /**
    * Crea una nueva instancia, asignándole los valores indicados.
    * @param id_SocketSrc id_socket fuente.
    * @param nSecParam número de secuencia.
    * @exception ParametroInvalidoException lanzada si alguno de los argumentos
    * es null.
    */
   public ID_TPDU(ID_Socket id_SocketSrc, NumeroSecuencia nSecParam)
                     throws  ParametroInvalidoExcepcion
   {
    if ((id_SocketSrc==null)||(nSecParam==null))
       throw new ParametroInvalidoExcepcion ();

    this.id_Socket = id_SocketSrc;
    this.nSec = nSecParam;
   }

   //=========================================================================
   /**
    * Devuelve el identificador del socket
    */
   public ID_Socket getID_Socket ()
   {
    return this.id_Socket;
   }


   //==========================================================================
   /**
    * Devuelve el número de secuencia del ID_TPDU.
    */
   public NumeroSecuencia getNumeroSecuencia ()
   {
    return this.nSec;
   }

  //==========================================================================
  /**
   * Implementación del método de la interfaz Comparable.
   * Compara primero el id_socket y después el número de secuencia.
   * @param o ID_TPDU con el que se compara.
   * @return mayor que cero si este ID_TPDU es mayor que el pasado en el
   * argumento, menor que cero si es menor y cero si son iguales.
   */
  public int compareTo(Object o)
   {
    ID_TPDU id_TPDU = (ID_TPDU) o;

    int comp = this.id_Socket.compareTo (id_TPDU.id_Socket);

    // Si las direcciones son iguales, comparo los números de secuencia.
    if ( comp == 0 )
      return this.nSec.compareTo (id_TPDU.nSec);

    return comp;
 }

   //=================================================================
   /**
    * Comprueba si este ID_TPDU es igual al pasado por parámetro.
    * Dos ID_TPDU son iguales si tienen el mismo id_socket y el mismo
    * número de secuencia.
    * @param o ID_TPDU con el que se compara.
    * @return true si son iguales, y false en caso contrario.
    */
   public boolean equals (Object o)
   {
    ID_TPDU id_TPDU = (ID_TPDU) o;

    if (this.compareTo (id_TPDU) == 0)
        return true;

    return false;
   }


 //======================================================================
 /**
  * Devuelve una cadena informativa.
  */
 public String toString ()
 {
  return new String ("[" + this.id_Socket    + "," +
                           this.nSec         + "," +
                     "]");
 }




}

