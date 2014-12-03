//============================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	Fichero: PTMFIDGLListener.java  1.0 24/9/99
//
//
//	Descripción: Clase PTMFIDGLListener.
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
 * Interfaz PTMFID_SocketListener.
 * Notificación de eventos PTMFEventID_Socket.<br>
 * <B>LOS EVENTOS DEBEN DE SER TRATADOS CON LA MAYOR BREVEDAD, PARA
 * NO AFECTAR AL RENDIMIENTO DE PTMF<B>. Por ejemplo, se deben de usar
 * los eventos para establecer banderas cuya acción sea realizada por
 * un thread aparte.
 * @version  1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */

public interface PTMFID_SocketListener
{
  //==========================================================================
  /**
   * El método callback
   * @param evento PTMFEventConexion
   */
  public void actionPTMFID_Socket(PTMFEventID_Socket evento);

}

