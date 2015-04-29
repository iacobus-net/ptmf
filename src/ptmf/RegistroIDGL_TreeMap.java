//============================================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: RegistroIDGL_TreeMap.java  1.0 24/11/99
//
//
//	Descripción: Clase RegistroIGDL_TreeMap
//
// 	
// 	Authors: 
//		 Alejandro García-Domínguez (alejandro.garcia.dominguez@gmail.com)
//		 Antonio Berrocal Piris (antonioberrocalpiris@gmail.com)
//
//  Historial: 
//  07.04.2015 Changed licence to Apache 2.0     
//
//  This file is part of PTMF 
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//----------------------------------------------------------------------------

package ptmf;

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