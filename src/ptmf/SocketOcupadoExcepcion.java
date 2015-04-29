//============================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	Fichero: SocketOcupadoExcepcion.java  1.0 30/08/99
//
//	Descripci�n: Clase SocketOcupadoExcepcion.
//
//
// 	Authors: 
//		 Alejandro Garc�a-Dom�nguez (alejandro.garcia.dominguez@gmail.com)
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
 * Esta excepci�n es lanzado por el m�todo send de la clase Socket cuando
 * el socket est� en modo <b>NO-BLOQUEANTE</b> y los datos pasados no pueden
 * ser enviados al estar llena la ventana de Emisi�n. 
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
   * Este constructor crea un objeto excepci�n SocketOcupadoExcepcion con un mensaje
   * informativo del error ocurrido, adem�s imprime el mensaje en stdout.
   * @param mn Nombre del m�todo que lanz� la excepci�n.
   * @param msg La cadena informativa.
   */
  public SocketOcupadoExcepcion(String mn,String msg) {
      super("["+mn+"] "+msg);
      Log.log("PTMFExcepcion: "+mn,msg);
  }
}
