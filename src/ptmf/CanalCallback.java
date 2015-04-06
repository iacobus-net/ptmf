//============================================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: CanalCallback.java  1.0 30/08/99
//
//
//	Description: Class CanalCallback. Interface for callback method of Canal class 
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
