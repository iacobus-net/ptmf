//============================================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: Canal.java  1.0 30/08/99
//
//
//	Description: Clase Canal. Generic interface for a Channel 
//
//
//  Historial: 
//	14/10/2014 Change Licence to LGPL
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

package iacobus.ptmf;

import java.io.*;
import java.util.*;
import java.net.UnknownHostException;

/**
 * Interfaz que proporciona un conjunto de métodos común para canales.
 * @version  1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */

public interface Canal
{
  //==========================================================================
  /**
   * Un método para cerrar el canal y limpiar cualquier recurso que está siendo
   * utilizado por el canal. <b>Este método debe de ser llamado antes de que
   * el objeto sea dejado.</b>
   */
  public abstract void close();

  //==========================================================================
  /**
   * Un método para enviar un buffer de datos a la dirección destino especificada
   *  en la construcción del canal.
   * <b>El buffer y la dirección no son alterados en ningún momento.</b>
   * @param buf El buffer que contiene los datos a enviar.
   * @exception PTMFExcepcion Excepción genérica
   * @exception PTMFInvalidParameterException Parámetro incorrecto.
   * @exception IOException Error enviando los datos
   */
/*  public abstract void send(Buffer buf)
   throws PTMFExcepcion, ParametroInvalidoExcepcion, IOException;
  */
  //==========================================================================
  /**
   * Un método para recibir datos del canal en el buffer especificado.
   * La dirección del emisor de los datos es colocada en el objeto Address
   * proporcionado.
   * @param buf El buffer donde serán colocados los datos recibidos.
   * @param src La dirección del emisor de los datos.
   * @exception PTMFExcepcion Excepción genérica
   * @exception PTMFInvalidParameterException Parámetro incorrecto.
   * @exception IOException Error recibiendo los datos
   */
  public abstract void receive(Buffer buf, Address src)
     throws PTMFExcepcion, ParametroInvalidoExcepcion, IOException;

  //==========================================================================
  /**
   * Un método que devuelve el tamaño máximo en bytes del paquete para el canal.
   * @return El tamaño máximo del paquete en bytes para el canal.
   */
  public abstract int getMaxPacketSize();
}
