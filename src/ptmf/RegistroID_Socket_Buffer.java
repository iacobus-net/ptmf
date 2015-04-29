//============================================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: RegistroID_SOCKET_Buffer.java  1.0 24/11/99
//
//
//	Descripción: Clase RegistroID_SOCKET_Buffer.
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


package ptmf;


/**
 * La clase RegistroID_SOCKET_Buffer almacena una referencia a un objeto Buffer
 * y al socket emisor de esos datos, un objeto ID_SOCKET
 * Esta clase no es thread-safe.
 * @version  1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:garcia@arconet.es">(garcia@arconet.es)</A><p>
 *			   Antonio Berrocal Piris
 */

public class RegistroID_Socket_Buffer {

  /** El objeto Buffer */
  private Buffer buf = null;

  /** El objeto ID_SOCKET*/
  private ID_Socket id_socket = null;

  /** Bit de Fin de Transmisión */
  private boolean bFinTransmision = false;

  //==========================================================================
  /**
   * Constructor genérico.
   * @param id_socket ID_SOCKET del emsior de los datos
   * @param buf Datos enviados.
   */
  RegistroID_Socket_Buffer(ID_Socket id_socket,Buffer buf,boolean bFinTransmision)
  {
   this.id_socket = id_socket;
   this.buf = buf;
   this.bFinTransmision = bFinTransmision;
  }

  //==========================================================================
  /**
   * Obtiene el objeto ID_SOCKET
   * @return Objeto ID_SOCKET.
   */
  public ID_Socket getID_Socket()
  {
    return this.id_socket;
  }

  //==========================================================================
  /**
   * Obtiene el objeto Buffer.
   * @return Objeto Buffer
   */
  public Buffer getBuffer()
  {
    return this.buf;
  }

  //==========================================================================
  /**
   * Obtiene el bit de Fin Transmision
   * @return boolean
   */
  public boolean esFinTransmision()
  {
    return this.bFinTransmision;
  }


  //==========================================================================
  /**
   * Devuelve una cadena informativa del objeto.
   */
  public String toString ()
  {
    return "ID_Socket: " + this.id_socket +
           "\nDatos:     " + this.buf
           ;

  }


}

