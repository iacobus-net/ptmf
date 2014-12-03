
//============================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	Fichero: PTMFEventConexion.java  1.0 14/03/2000
//
//	Descripción: Clase PTMFEventConexion. Evento PTMF Conexion
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


import java.util.EventObject;

/**
 * La clase PTMFEventConexion es utilizada por PTMF para notificar informacion
 * relativa a la conexion Multicast
 */
public class PTMFEventConexion extends PTMFEvent
{

  /**
   * Constructor PTMFEventConexion
   * @param socket Un objeto SocketPTMFImp
   * @param sInformativa cadena Informativa
   */
  public PTMFEventConexion(SocketPTMFImp socket,String sInformativa)
  {
    super(socket,EVENTO_CONEXION,sInformativa);
  }

}

