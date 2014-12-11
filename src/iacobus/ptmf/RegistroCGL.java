//============================================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: RegistroCGL.java  1.0 15/10/99
//
//	Descripción: Clase RegistroCGL
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


package iacobus.ptmf;

/**
 * Esta clase encapsula un TPDUCGL y un objeto Address.<br>
 * Es utilizada en la clase SocketPTMF y en la clase CGLThread
 * para añadir y obtener los registros CGL respectivamente.
 * @version  1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:garcia@arconet.es">(garcia@arconet.es)</A><p>
 *			   Antonio Berrocal Piris
 */

class RegistroCGL
{

  /** Objeto TPDUCGL encapsulado */
  TPDUCGL tpduCGL = null;

  /** Objeto Address encapsulado */
  Address src = null;

  //==========================================================================
  /**
   * Constructor. Encapsula un objeto TPDUCGL y Address.
   * @param tpduCGL El objeto tpduCGL.
   * @param src La dirección del emisor del TPDUCGL.
   */
  RegistroCGL(TPDUCGL tpduCGL, Address src)
  {
    this.tpduCGL = tpduCGL;
    this.src = src;
  }
}