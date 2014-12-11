//============================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	File: PTMFEventIDGL.java  1.0 14/03/2000
//
//
//	Description: Clase PTMFEventIDGL. Evento PTMF IDGL
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
 *  La clase PTMFEventIDGL es utilizada por PTMF para notificar la incorporacion
 * o eliminacion de un IDGL
 */
public class PTMFEventIDGL extends PTMFEvent
{

  /** IDGL */
  private IDGL idgl = null;

  /** Boolean bAñadido. especifica si el IDGL ha sido añadido  o eliminado */
  private boolean bAñadido = false;

  /**
   * Constructor PTMFEvent
   * @param socket Un objeto SocketPTMFImp
   * @param sInformativa cadena Informativa
   * @param evento El tipo de evento que se quiere crear
   */
  public PTMFEventIDGL(SocketPTMFImp socket,String sInformativa,IDGL idgl, boolean bAñadido)
  {
    super(socket,EVENTO_IDGL,sInformativa);
    this.idgl = idgl;
    this.bAñadido = bAñadido;
  }

  /**
   * Obtiene el IDGL
   * @return el objeto IDGL
   */
  public IDGL getIDGL(){return this.idgl;}

  /**
   * Boolean que indica si el IDGL ha sido añadido o eliminado a la jerarquía
   * del Grupo Local
   */
  public boolean esAñadido() { return this.bAñadido;}

}

