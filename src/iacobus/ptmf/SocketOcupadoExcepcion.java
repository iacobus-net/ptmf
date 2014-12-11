//============================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	Fichero: SocketOcupadoExcepcion.java  1.0 30/08/99
//
//	Descripción: Clase SocketOcupadoExcepcion.
//
//
//  Authors: 
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

package iacobus.ptmf;

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
