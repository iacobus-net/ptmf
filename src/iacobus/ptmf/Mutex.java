//============================================================================
//
//	Copyright (c) 1999 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	File: Mutex.java  1.0 15/09/99
//
//	Description: Clase Mutex.
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
//----------------------------------------------------------------------------

package iacobus.ptmf;

import java.io.*;
import java.util.*;


/**
 * Clase genérica Mutex. Región crítica.<p>
 * La calse Mutex se utiliza para Exclusion Mutua entre varios threads.
 * <p>
 * Esta clase es thread-safe.
 * @version  1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */

public class Mutex
{
  /**
   * Flag de bloqueo. Indica si la R.C esta bloqueada por otro thread.
   */
  private boolean  lock = false;

  //==========================================================================
  /**
   * El constructor por defecto.
   */
  public Mutex()
  {
    this.lock = false;
  }

  //==========================================================================
  /**
   * Intenta obtener acceso al mutex.  Regresa sólo cuando el objeto
   * está bloqueado.
   */
  public synchronized void lock()
  {
    while (this.lock)
    {
      try
      {
        wait();
      }
      catch (InterruptedException ie)
      {
      }
    }

    this.lock = true;
  }

  //==========================================================================
  /**
   * Libera el bloque sobre el objeto.
   */
  public synchronized void unlock()
  {
    this.lock = false;
    notifyAll();
  }


}
