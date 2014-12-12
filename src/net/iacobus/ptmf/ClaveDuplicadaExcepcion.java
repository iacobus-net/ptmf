//============================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	Fichero: ClaveDuplicadaExcepcion.java  1.0 30/08/99
//
//
//	Descripción: Clase ClaveDuplicadaExcepcion.
//
//  Historial: 
//	14/10/2014 Change Licence to LGPL
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


/**
  * Excepción lanzada al intentar añadir un elemento ya existente a una
  * estructura de datos indexada.
  * @version  1.0
  * @author Antonio Berrocal Piris
  * <A HREF="mailto:AntonioBP@wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
  * M. Alejandro García Domínguez
  * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 */
public class ClaveDuplicadaExcepcion extends PTMFExcepcion
{

  //==========================================================================
  /**
   * Constructor por defecto.
   */
  public ClaveDuplicadaExcepcion (String clave)
  {
   super ("La clave " +clave+" ya existe.");
  }
}






