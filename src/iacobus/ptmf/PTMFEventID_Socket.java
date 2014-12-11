//============================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	Fichero: PTMFEventID_Socket.java  1.0 14/03/2000
//

//	Descripción: Clase PTMFEventID_Socket. Evento PTMF ID_Socket
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

package iacobus.ptmf;


import java.util.EventObject;

/**
 * La clase PTMFEventID_Socket es utilizada por PTMF para notificar la incorporacion
 * o eliminacion de un ID_Socket
 */
public class PTMFEventID_Socket extends PTMFEvent
{

  /** ID_Socket */
  private ID_Socket id_socket = null;

  /** Boolean bAñadido. especifica si el ID_Socket ha sido añadido o eliminado */
  private boolean bAñadido = false;

  /**
   * Constructor PTMFEventID_Socket
   * @param socket Un objeto SocketPTMFImp
   * @param sInformativa cadena Informativa
   */
  public PTMFEventID_Socket(SocketPTMFImp socket,String sInformativa,
        ID_Socket id_socket,boolean bAñadido)
  {
    super(socket,EVENTO_ID_SOCKET,sInformativa);
    this.id_socket = id_socket;
    this.bAñadido = bAñadido;
  }

  /**
   * Obtiene el ID_Socket
   * @return el objeto ID_Socket
   */
  public ID_Socket getID_Socket(){return this.id_socket;}

  /**
   * Boolean que indica si el ID_Socket ha sido añadido o eliminado del grupo local
   */
  public boolean esAñadido() { return this.bAñadido;}

}

