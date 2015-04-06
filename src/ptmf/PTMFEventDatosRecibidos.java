//============================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	File: PTMFEventDatosRecibidos.java  1.0 14/03/2000
//
//
//	Description: Clase PTMFEventDatosRecibidos. Evento PTMF Datos Recibidos
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

package ptmf;


import java.util.EventObject;

/**
 * La clase PTMFEventDatosRecibidos es utilizada por PTMF para indicar
 * que se han recibido datos.
 */
public class PTMFEventDatosRecibidos extends PTMFEvent
{

  /** Bytes */
  private int bytes = 0;

  /**
   * Constructor PTMFEventDatosRecibidos
   * @param socket Un objeto SocketPTMFImp
   * @param sInformativa cadena Informativa
   */
  public PTMFEventDatosRecibidos(SocketPTMFImp socket,String sInformativa,int bytes)
  {
    super(socket,EVENTO_DATOS_RECIBIDOS,sInformativa);
    this.bytes = bytes;
  }

  /**
   * Obtiene el número de bytes recibidos
   * @return el número de bytes
   */
  public int getBytes(){return this.bytes;}
}

