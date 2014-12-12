
//============================================================
//
//	Copyright (c) 1999 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	File: PTMFEventError.java  1.0 14/03/2000
//
//
//	Description: Clase PTMFEventError. Evento PTMF Error
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
 * La clase PTMFEventError es utilizada por PTMF para notificar de un error
 * en el protocolo.
 */
public class PTMFEventError extends PTMFEvent
{
  /** Identificativo de una notificación de un Error*/
  public static final int EVENTO_ERROR = 6;


  /**
   * Constructor PTMFEventError
   * @param socket Un objeto SocketPTMFImp
   * @param sInformativa cadena Informativa
   * @param evento El tipo de evento que se quiere crear
   */
  public PTMFEventError(SocketPTMFImp socket,String sInformativa)
  {
    super(socket,EVENTO_ERROR,sInformativa);
  }


}

