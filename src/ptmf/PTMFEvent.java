//============================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	Fichero: PTMFEvent.java  1.0 30/08/99
//
//
//	Description: Clase PTMFEvent. Evento PTMF
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
 * La clase PTMFEvent es una clase base para todos los eventos utilizados
 * por PTMF
 */
public class PTMFEvent extends EventObject
{
  /** Identificativo de notificación de información de conexión*/
  public static final int EVENTO_CONEXION= 1;

  /** Evento notificación ID_SOCKET*/
  public static final int EVENTO_ID_SOCKET = 2;

 /** Identificativo de una notificación de un IDGL*/
  public static final int EVENTO_IDGL = 3;

 /** Identificativo de notificación de datos recibidos*/
  public static final int EVENTO_DATOS_RECIBIDOS = 4;

 /** Identificativo de una notificación de un Error*/
  public static final int EVENTO_ERROR = 5;

  /** Cadena informativa del evento */
  private String sInformativa = null;

  /** Tipo de evento*/
  private int tipo_evento = 0;

  /**
   * Constructor PTMFEvent
   * @param socket Un objeto SocketPTMFImp
   * @param sInformativa cadena Informativa
   * @param evento El tipo de evento que se quiere crear
   */
  public PTMFEvent(SocketPTMFImp socket,int tipo_evento,String sInformativa)
  {
    super(socket);
    this.sInformativa = sInformativa;
    this.tipo_evento = tipo_evento;
  }

  /**
   * Obtiene la cadena informativa
   * @return Una cadena
   */
  public String getString(){return this.sInformativa;}


  /**
   * Obtiene el evento
   * @return un entero que indica el tipo de evento
   */
  public int getTipoEvent(){return this.tipo_evento;}
}

