//============================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	File: PTMFExcepcion.java  1.0 30/08/99
//
//
//	Descripción: Clase PTMFExcepcion. Excepción genérica para PTMF
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

/**
 * Clase PTMFExcepcion. Es una excepción genérica que devuelven diferentes
 * métodos. La excepción se lanza en condiciones de error, si el método
 * ha realizado su operación correctamente la excepción no es lanzada.
 * La excepción contiene un mensaje (opcional) que explica el error ocurrido.
 * @version 1.0
 */
public class PTMFExcepcion extends IOException {

  //==========================================================================
  /**
   * Constructor por defecto.
   */
  public PTMFExcepcion() { super();
  // PRUEBAS
  Log.log ("EXCEPCION: PTMFEXCEPCION. ","");


//  System.exit (1);
   }

  //==========================================================================
  /**
   * Constructor con un mensaje informativo del error ocurrido.
   * @param msg La cadena informativa.
   */
  public PTMFExcepcion(String msg) { super(msg);
  // PRUEBAS

  Log.log ("EXCEPCION: PTMFEXCEPCION. ",msg);

  //  System.exit (1);
   }

  //==========================================================================
  /**
   * Este constructor crea un objeto excepción PTMFExcepcion con un mensaje
   * informativo del error ocurrido, además imprime el mensaje en stdout.
   * @param mn Nombre del método que lanzó la excepción.
   * @param msg La cadena informativa.
   */
  public PTMFExcepcion(String mn,String msg) {
      super("["+mn+"] "+msg);
      Log.log("PTMFExcepcion: "+mn,msg);
  // PRUEBAS
  Log.log ("EXCEPCION: PTMFEXCEPCION. ",mn+" "+msg);

//  System.exit (1);

  }
}
