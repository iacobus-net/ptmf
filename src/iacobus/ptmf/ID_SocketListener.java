//============================================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	File: ID_SocketListener.java  1.0 21/10/99
//
//
//	Descripción: Interfaz ID_SocketListener
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

/**
 * Esta interfaz es utilizada por la clase CGLThread para notificar
 * eventos acerca de la incorporación/eliminación de sockets en el grupo local.-
 */

public interface ID_SocketListener
{
  /**
   * Notifica que Id_Socket ha sido añadido al grupo local
   * @param id_socket ID_Socket añadido al grupo local
   */
  public void ID_SocketAñadido(ID_Socket id_socket);

  /**
   * Notifica que Id_Socket ha sido eliminado
   * @param id_socket ID_Socket eliminado del Grupo Local
   */
  public void ID_SocketEliminado(ID_Socket id_socket);
}
