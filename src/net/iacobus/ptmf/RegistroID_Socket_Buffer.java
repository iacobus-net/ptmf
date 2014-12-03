//============================================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: RegistroID_SOCKET_Buffer.java  1.0 24/11/99
//
//
//	Descripción: Clase RegistroID_SOCKET_Buffer.
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
//----------------------------------------------------------------------------


package net.iacobus.ptmf;


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

