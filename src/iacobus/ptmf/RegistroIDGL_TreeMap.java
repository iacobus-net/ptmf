//============================================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: RegistroIDGL_TreeMap.java  1.0 24/11/99
//
//
//	Descripción: Clase RegistroIGDL_TreeMap
//
// 	
//  Authors: 
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

import java.util.TreeMap;

/**
 * <p>Title: PTMF v1.1</p>
 * <p>Description: Protocolo de Transporte Multicast Fiable</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.1
 */


  /**
   * Clase RegistroIDGL_TreeMap.<br>
   * Almacena un IDGL y un TreMap con IDGL.
   */
   public class RegistroIDGL_TreeMap
   {
      IDGL idgl = null;
      TreeMap treemap = null;

      RegistroIDGL_TreeMap(IDGL idgl,TreeMap treemap)
      {
       this.idgl = idgl;
       this.treemap = treemap;
      }

      /**
       * Obtiene el IDGL
       * @return
       */
      public IDGL getIDGL()
      {
        return idgl;
      }

      /**
       * Obtiene el treemap
       * @return
       */
      public TreeMap getTreeMap()
      {
        return treemap;
      }
   }