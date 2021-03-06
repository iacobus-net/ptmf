//============================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	Fichero: ErrorIniciarTempExcepcion.java  1.0 30/08/99
//
//	Description: Clase ErrorIniciarTempExcepcion.
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
//
//------------------------------------------------------------
package ptmf;

/**
  * Excepci�n lanzada si ocurre un error al iniciar un objeto Temporizador.
  * @see Temporizador
  * @version  1.0
  * @author Antonio Berrocal Piris
  * <A HREF="mailto:AntonioBP@wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
  * M. Alejandro Garc�a Dom�nguez
  * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 */
public class ErrorIniciarTempExcepcion extends PTMFExcepcion
{

  //==========================================================================
  /**
   * Constructor por defecto.
   */
  public ErrorIniciarTempExcepcion ()
  {
   super ("Error al iniciar el temporizador.");
  }
}