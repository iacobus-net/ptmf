//============================================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: RegistroVectorDatos.java  1.0 15/10/99
//
//
//	Descripción: Clase RegistroVectorDatos.
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


/**
 * Encapsula los datos que deben almacenarse en el vector de TPDUDatos.<br><br>
 *
 * Esta clase no es thread-safe.
 * @version  1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */

class RegistroVectorDatos
{

 //========================================================================
 /**
  * Crea una instancia con los valores indicados.
  */
 public RegistroVectorDatos (TPDUDatos tpduDatosParam,ID_Socket id_SocketParam)
 {
  this.tpduDatos = tpduDatosParam;
  this.id_Socket = id_SocketParam;
 }

 // ATRIBUTOS

 /** TPDUDatos recibido. */
 TPDUDatos tpduDatos = null;

 /** Dirección del cliente que envío el TPDUDatos. */
 ID_Socket id_Socket = null;
}
