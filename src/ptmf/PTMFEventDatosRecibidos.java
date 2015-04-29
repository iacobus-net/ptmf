//============================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	File: PTMFEventDatosRecibidos.java  1.0 14/03/2000
//
//
//	Description: Clase PTMFEventDatosRecibidos. Evento PTMF Datos Recibidos
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

