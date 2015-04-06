//============================================================
//
//	Copyright (c) 1999 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	File: PTMFConexionListener.java  1.0 24/9/99
//
//
//	Description: Clase PTMFConexionListener.
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

package ptmf;

import java.io.*;
import java.util.*;


/**
 * Interfaz PTMFConexionListener.
 * Notificación de mensajes informativos por parte del socket PTMF
 * a la aplicación relativos a la conexión.<br>
 * <B>LOS EVENTOS DEBEN DE SER TRATADOS CON LA MAYOR BREVEDAD, PARA
 * NO AFECTAR AL RENDIMIENTO DE PTMF<B>. Por ejemplo, se deben de usar
 * los eventos para establecer banderas cuya acción sea realizada por
 * un thread aparte.
 * @version  1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */

public interface PTMFConexionListener
{
  //==========================================================================
  /**
   * El método callback
   * @param evento PTMFEventConexion
   */
  public void actionPTMFConexion(PTMFEventConexion evento);

}


