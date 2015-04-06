//============================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	File: PTMFEventID_SocketInputStream.java  1.0 13/04/2000
//
//
//	Descripción: Clase PTMFEventID_SocketInputStream. Evento PTMF ID_Socket
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
//
//------------------------------------------------------------

package ptmf;

import java.util.EventObject;

/**
    La clase PTMFEventID_SocketInputStream es un evento PTMF utilizado
    por la clase MulticastInputStream para notificar a la aplicación
    cuando hay un NUEVO FLUJO DE ENTRADA ID_SocketInputStream.<br>
    El evento lleva asociado el nuevo flujo ID_SocketInputStream añadido.
 */
public class PTMFEventID_SocketInputStream extends PTMFEvent
{

  /** ID_SocketInputStream */
  private ID_SocketInputStream id_socketInputStream = null;


  /**
   * Constructor PTMFEventID_SocketInputStream
   * @param socket Un objeto SocketPTMFImp
   * @param sInformativa cadena Informativa
   */
  public PTMFEventID_SocketInputStream(SocketPTMFImp socket,String sInformativa,
        ID_SocketInputStream id_socketInputStream)
  {
    super(socket,EVENTO_ID_SOCKET,sInformativa);
    this.id_socketInputStream = id_socketInputStream;

  }

  /**
   * Obtiene el ID_SocketInputStrem
   * @return el objeto ID_SocketInputStream
   */
  public ID_SocketInputStream getID_SocketInputStream(){return this.id_socketInputStream;}

}

