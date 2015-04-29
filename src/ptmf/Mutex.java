//============================================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	File: Mutex.java  1.0 15/09/99
//
//	Description: Clase Mutex.
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
//----------------------------------------------------------------------------

package ptmf;

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
