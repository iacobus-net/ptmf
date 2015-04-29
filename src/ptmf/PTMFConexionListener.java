//============================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	File: PTMFConexionListener.java  1.0 24/9/99
//
//
//	Description: Clase PTMFConexionListener.
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


