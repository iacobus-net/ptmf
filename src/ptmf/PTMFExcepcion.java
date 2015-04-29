//============================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	File: PTMFExcepcion.java  1.0 30/08/99
//
//
//	Descripci�n: Clase PTMFExcepcion. Excepci�n gen�rica para PTMF
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
 * Clase PTMFExcepcion. Es una excepci�n gen�rica que devuelven diferentes
 * m�todos. La excepci�n se lanza en condiciones de error, si el m�todo
 * ha realizado su operaci�n correctamente la excepci�n no es lanzada.
 * La excepci�n contiene un mensaje (opcional) que explica el error ocurrido.
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
   * Este constructor crea un objeto excepci�n PTMFExcepcion con un mensaje
   * informativo del error ocurrido, adem�s imprime el mensaje en stdout.
   * @param mn Nombre del m�todo que lanz� la excepci�n.
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
