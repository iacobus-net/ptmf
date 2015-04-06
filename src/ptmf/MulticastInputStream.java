//============================================================================
//
//	Copyright (c) 1999 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	File: MulticastInputStream.java  1.0 24/11/99
//
//	Description: Clase MulticastInputStream.
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


package ptmf;

import java.io.InputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * MulticastInputStream es un "GESTOR" de flujos de entrada
 * ID_SocketInputStream para el SocketPTMF. <br>
 * Existe un objeto ID_SocketInputStream por cada emisor.
 * @version  1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */
public class MulticastInputStream implements Enumeration
{
  /** SocketPTMF   */
  private SocketPTMFImp socket = null;

  /** Iterador para los flujos */
  private Iterator  iterator = null;


  //==========================================================================
  /**
   * Constructor. Crea un flujo de salida MulticastInpustStream.
   * <br> Un "gestor" de flujos ID_SocketImputStream
   * @param socket El objeto SocketPTMFImp
   */
  MulticastInputStream(SocketPTMFImp socket)
  {
   super();

   this.socket = socket;

   if(this.socket == null)
    throw new NullPointerException("MulticastInputStream: Parámetro colaRecepción nulo.");
  }


  //==========================================================================
  /**
   * Un método para registrar un objeto que implementa la interfaz PTMFID_SocketInputStreamListener.
   * La interfaz PTMFID_SocketInputStreamListener se utiliza para notificar a
   * las clases que se registren de un evento PTMFEventID_SocketInputStream
   * @param obj El objeto PTMFID_SocketInputStreamListener
   */
  public void addPTMFID_SocketInputStreamListener(PTMFID_SocketInputStreamListener obj)
  {
   this.socket.addPTMFID_SocketInputStreamListener (obj);
  }

  //==========================================================================
  /**
   * Elimina un objeto PTMFID_SocketInputStreamListener
   * @param obj el objeto ID_SocketInputStreamListener a eliminar
   */
  public void removePTMFID_SocketInputStreamListener(PTMFID_SocketInputStreamListener obj)
  {
    this.socket.removePTMFID_SocketInputStreamListener (obj);
  }

  //==========================================================================
  /**
   * Un método para registrar un objeto que implementa la interfaz PTMFDatosRecibidosListener.
   * La interfaz PTMFDatosRecibidosListener se utiliza para notificar a las clases que se
   * registren de un evento PTMFEventDatosRecibidos
   * @param obj El objeto PTMFDatosRecibidosListener
   * @return PTMFExcepcion Se lanza cuando ocurre un error al registrar el
   *  objeto callback
   */
  public void addPTMFDatosRecibidosListener(PTMFDatosRecibidosListener obj)
  {
    this.socket.addPTMFDatosRecibidosListener(obj);
  }

  //==========================================================================
  /**
   * Elimina un objeto PTMFDatosRecibidosListener
   * @param obj El objeto que implementa la interfaz PTMFDatosRecibidosListener
   */
  public void removePTMFDatosRecibidosListener(PTMFDatosRecibidosListener obj)
  {
    this.socket.removePTMFDatosRecibidosListener(obj);
  }

  //==========================================================================
  /**
   * Devuelve el número de bytes que pueden ser leídos (o descartados) de
   * este flujo de entrada sin bloquear por la siguiente llamada a un método
   * de lectura.
   * @return El número de bytes que pueden ser leídos desde este flujo de entrada.
   * @exception IOException - si ocurre un error de I/O
   */
  public int available() throws IOException
  {
    return this.socket.getColaRecepcion().getTamaño();
  }

  //==========================================================================
  /**
   * Devuelve lo mismo que hasMoreID_SocketInputStream()
   * @return true si hay más ID_SocketInputStream con datos para leer y false en caso contrario.
   */
  public boolean hasMoreElements()
  {
    return this.hasMoreID_SocketInputStream();
  }

  //==========================================================================
  /**
   * Comprueba si hay más ID_SocketInputStream con datos para leer.
   * @return true si hay más ID_SocketInputStream con datos para leer y false en caso contrario.
   */
  public boolean hasMoreID_SocketInputStream()
  {
    try
    {
     if(this.available() <= 0)
        return false;
     else
        return true;
    }
    catch(IOException e){ return false;}
  }

  //==========================================================================
  /**
   * Devuelve lo mismo que nextID_SocketInputStream().
   * @return Un objeto ID_SocketInputStream que contiene datos que leer.
   */
  public Object nextElement() throws NoSuchElementException
  {
   return this.nextID_SocketInputStream();
  }


  //==========================================================================
  /**
   * Devuelve el siguiente flujo de entrada ID_SocketInputStream listo para leer
   * datos.
   * Si no hay ningún flujo con datos disponible se lanza la excpeción NoSuchElementException.
   * @return un objeto ID_SocketInputStream que contiene datos que leer.
   */
  public ID_SocketInputStream nextID_SocketInputStream() throws NoSuchElementException
  {
    //Ver primero si hay datos, si no "pa" que vamos a devolver un flujo.
    try
    {
     if(this.available() <= 0)
        throw new NoSuchElementException();
    }
    catch(IOException e){ ;}

    return this.socket.getColaRecepcion().nextID_SocketInputStream();
  }



  //==========================================================================
  /**
   * Cierra este flujo y elimina todos los recursos asociados a él
   * Una vez cerrado el flujo no se podrá volver a utilizarlo.
   */
  public void close()
  {
   Iterator i = this.socket.getColaRecepcion().getTreeMap().values().iterator();

   try
   {
    while(i.hasNext())
     {
        ID_SocketInputStream id = (ID_SocketInputStream) i.next();
        id.close();
     }

     //this.socketPTMFImp.getColaRecepcion() = null;
     this.iterator = null;
   }
   /*catch(IOException e)
   {
    return;
   }
   */
   finally
   {
    ;
   }
  }

}
