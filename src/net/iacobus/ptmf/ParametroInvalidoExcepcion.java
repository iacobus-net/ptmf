//============================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	Fichero: ParametroInvalidoExcepcion.java  1.0 11/9/99
//
//
//	Descripción: Clase ParametroInvalidoExcepcion.
//
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
 * Clase ParametroInvalidoExcepcion. Esta excepción es lanzada
 * cuando se pasa algún parámetro inválido a algún método en las
 * clases del paquete PTMF
 * @version 1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */

public class ParametroInvalidoExcepcion extends IOException {

  //==========================================================================
  /**
   * Constructor por defecto.
   */
  public ParametroInvalidoExcepcion() { super();
  // PRUEBAS
  Log.log ("EXCEPCION: PARAMETROINVALIDOEXCEPCION","");
//  System.exit (1);

   }

  //==========================================================================
  /**
   * Constructor con un mensaje informativo del error ocurrido.
   * @param msg La cadena informativa.
   */
  public ParametroInvalidoExcepcion(String msg) { super(msg);
  // PRUEBAS
  Log.log ("EXCEPCION: PARAMETROINVALIDOEXCEPCION. ",msg);

//  System.exit (1);

  }

  //==========================================================================
  /**
   * Este constructor crea un objeto excepción PTMFParametroInvalidoExcepcioncon un mensaje
   * informativo del error ocurrido, además imprime el mensaje en stdout.
   * @param mn Nombre del método que lanzó la excepción.
   * @param msg La cadena informativa.
   */
  public ParametroInvalidoExcepcion(String mn,String msg) {
      super("["+mn+"] "+msg);
      Log.log("PTMFExcepcion: "+mn,msg);
  // PRUEBAS
  Log.log ("EXCEPCION: PARAMETROINVALIDOEXCEPCION. ",mn+" "+msg);


  //  System.exit (1);
  }


}
