//============================================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: TimerHandler.java  1.0 30/08/99
//

//	Descripci�n: Interfaz TimerHandler. Interfaz para m�todo callback de un
//                   Temporizador.
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
//----------------------------------------------------------------------------


package ptmf;

/**
 * Interfaz que proporciona un m�todo callback com�n para Temporizadores.<br>
 * Interfaz utilizada para registrar avisos en el {@link Temporizador}, a la
 * finalizaci�n del tiempo, el Temporizador ejecutar� el m�todo callback pas�ndole
 * por par�metro los argumentos que registr�.
 * @version  1.0
 * @author Antonio Berrocal Piris
 * <A HREF="mailto:AntonioBP@wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
 * M. Alejandro Garc�a Dom�nguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 */
public interface TimerHandler
{
  //==========================================================================
  /**
   * El m�todo callback que ejecuta el {@link Temporizador}.
   * @param lArg primer argumento del callback.
   * @param o segundo argumento del callback.
   */
  public void TimerCallback(long lArg, Object o);

}

