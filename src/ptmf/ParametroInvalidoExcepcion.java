//============================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
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
