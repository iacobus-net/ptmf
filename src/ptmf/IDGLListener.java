//============================================================================
//
//	Copyright (c) 1999-2000,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: IDGLListener.java  1.0 21/1/00
//
//
//	Descripción: Interfaz IDGLListener
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

package ptmf;


/**
 * Esta interfaz es utilizada por la clase CGLThread para notificar
 * eventos acerca de la incorporación/eliminación de IDGLs en
 * la jerarquía de control.
 */

public interface IDGLListener
{
  /**
   * Notifica que IDGL ha sido añadido
   * @param idgl Nuevo IDGL que se puede alcanzar
   */
  public void IDGLAñadido(IDGL idgl);

  /**
   * Notifica que IDGL ha sido eliminado
   * @param idgl IDGL que ha quedado fuera de alcanze
   */
  public void IDGLEliminado(IDGL idgl);

}
