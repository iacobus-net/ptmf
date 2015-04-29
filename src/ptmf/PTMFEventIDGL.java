//============================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	File: PTMFEventIDGL.java  1.0 14/03/2000
//
//
//	Description: Clase PTMFEventIDGL. Evento PTMF IDGL
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
//------------------------------------------------------------

package ptmf;


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

