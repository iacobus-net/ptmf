//============================================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: IDGL_TreeMap.java  1.0 17/11/99
//
//
//	Descripción: IDGL
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

package ptmf;

import java.util.TreeMap;

/**
 * Esta clase encapsula un objeto IDGL y otro TreeMap,
 * se utiliza como VALUE en el TreeMap treeMapIDGLVecinos
 * dentro de la clase CGLThread
 */
class IDGL_TreeMap
{
  /** IDGL */
  IDGL idgl = null;

  /** TreeMap */
  TreeMap treemap = null;

  IDGL_TreeMap(IDGL idgl, TreeMap treemap)
  {
    this.idgl = idgl;
    this.treemap = treemap;
  }
}
