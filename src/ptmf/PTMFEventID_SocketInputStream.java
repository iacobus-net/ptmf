//============================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	File: PTMFEventID_SocketInputStream.java  1.0 13/04/2000
//
//
//	Descripción: Clase PTMFEventID_SocketInputStream. Evento PTMF ID_Socket
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

