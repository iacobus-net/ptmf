//============================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	Fichero: PTMF.java  1.0 24/9/99
//
//
//	Descripción: Clase ReceiveHandler.
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

import java.io.*;
import java.util.*;


/**
 * Interfaz que proporciona un método común receiveCallback() para la
 * notificación de la recepción de datos sobre el socket PTMF.
 * @version  1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:garcia@arconet.es">(garcia@arconet.es)</A><p>
 *			   Antonio Berrocal Piris
 */

public interface ReceiveHandler
{
  //==========================================================================
  /**
   * El método común receiveCallback
   * @param arg El argumento del método callback
   */
  public void receiveCallback(int arg);

}
