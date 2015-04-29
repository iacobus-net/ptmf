//============================================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: IDGL_TreeMap.java  1.0 17/11/99
//
//
//	Descripción: IDGL
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
