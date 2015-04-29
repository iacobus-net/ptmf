//============================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	Fichero: PTMFEvent.java  1.0 30/08/99
//
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

