//============================================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: RegistroCGL.java  1.0 15/10/99
//
//	Descripción: Clase RegistroCGL
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