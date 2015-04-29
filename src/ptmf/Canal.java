//============================================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: Canal.java  1.0 30/08/99
//
//
//	Description: Clase Canal. Generic interface for a Channel 
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
//----------------------------------------------------------------------------

package ptmf;

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
