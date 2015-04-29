//============================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	Fichero: SocketOcupadoExcepcion.java  1.0 30/08/99
//
//	Descripción: Clase SocketOcupadoExcepcion.
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
//------------------------------------------------------------

package ptmf;

import java.io.*;

/**
 * Clase SocketOcupadoExcepcion.<br>
 * Esta excepción es lanzado por el método send de la clase Socket cuando
 * el socket está en modo <b>NO-BLOQUEANTE</b> y los datos pasados no pueden
 * ser enviados al estar llena la ventana de Emisión. 
 * @version 1.0
 * @see Socket
 */
public class SocketOcupadoExcepcion extends IOException {

  //==========================================================================
  /**
   * Constructor por defecto.
   */
  public SocketOcupadoExcepcion() { super(); }

  //==========================================================================
  /**
   * Constructor con un mensaje informativo del error ocurrido.
   * @param msg La cadena informativa.
   */
  public SocketOcupadoExcepcion(String msg) { super(msg); }

  //==========================================================================
  /**
   * Este constructor crea un objeto excepción SocketOcupadoExcepcion con un mensaje
   * informativo del error ocurrido, además imprime el mensaje en stdout.
   * @param mn Nombre del método que lanzó la excepción.
   * @param msg La cadena informativa.
   */
  public SocketOcupadoExcepcion(String mn,String msg) {
      super("["+mn+"] "+msg);
      Log.log("PTMFExcepcion: "+mn,msg);
  }
}
