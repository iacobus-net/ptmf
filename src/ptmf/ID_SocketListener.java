//============================================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	File: ID_SocketListener.java  1.0 21/10/99
//
//
//	Descripción: Interfaz ID_SocketListener
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
