//============================================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: CanalCallback.java  1.0 30/08/99
//
//
//	Description: Class CanalCallback. Interface for callback method of Canal class 
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

import java.io.*;
import java.util.*;


/**
 * Interfaz que proporciona un método callback común para canales.
 * <p><b>Este método es llamado cuando se reciban datos en el socket
 * y solo si se ha establecido el método callback en la clase MulticastChannel
 * .</b>
 * @version  1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */

interface CanalCallback
{
  //==========================================================================
  /**
   * El método callback del canal.
   * @param arg Un argumento para el método.
   * @param buf un buffer que contiene los datos recibidos.  El handler debe de
   * copiar los datos del buffer fuera de este objeto <b>sin modificar el
   * buffer. </b>
   * @param src Un objeto Address conteniendo la dirección fuente de los datos.
   * El handler debe de copiar la dirección fuera de este objeto <b>sin modificar
   * los datos.</b>
   */
   void canalCallback(int arg, Buffer buf, Address src);

}
