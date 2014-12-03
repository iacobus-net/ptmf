//============================================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: RegistroVectorDatos.java  1.0 15/10/99
//
//
//	Descripción: Clase RegistroVectorDatos.
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
//
//----------------------------------------------------------------------------


package net.iacobus.ptmf;


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
