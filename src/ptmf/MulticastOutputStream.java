//============================================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	File: MulticastOutputStream.java  1.0 24/11/99
//
//	Description: Clase MulticastOutputStream.
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

import java.io.OutputStream;
import java.io.IOException;

/**
 * MulticastOutputStream es un flujo de salida para SocketPTMF.
 * @version  1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */
public class MulticastOutputStream extends OutputStream
{

  /** SocketPTMFImp  */
  private SocketPTMFImp socketPTMFImp = null;

  /** Flujo Cerrado */
  private boolean bCerrado = false;

  //==========================================================================
  /**
   * Constructor.
   * @param socketPTMFImp El objeto SocketPTMFImp
   */
  MulticastOutputStream(SocketPTMFImp socketPTMFImp)
  {
   super();
   this.socketPTMFImp = socketPTMFImp;
  }

  //==========================================================================
  /** Devuleve true si el flujo está cerrado */
  boolean isClose(){return this.bCerrado;}

  //==========================================================================
  /**
   * Escribe un byte en el flujo de salida.
   * @param b Un entero, se escibe sólo los 8 bits más bajos, los 24 restantes
   * se descartan.
   * @exception IOException Se lanza si ocurre algún error en el flujo de salida.
   */
   public void write(int b) throws IOException
   {
     byte[] buf  = {(byte)b};

     write(buf);
   }

  //==========================================================================
  /**
   * Escribe un array de bytes en el flujo de salida.
   * @param b Un array de bytes.
   * @exception IOException Se lanza si ocurre algún error en el flujo de salida.
   */
   public void write(byte[] b) throws IOException
   {
      write(b,0,b.length);
   }

  //==========================================================================
  /**
   * Escribe len bytes del array de bytes b o hasta el final del array si lo
   * encuentra primero y a partir del offset off en el flujo de salida.
   * @param b Un array de bytes.
   * @param off Offset del array de bytes
   * @param len Número de bytes a copiar.
   * @exception IOException Se lanza si ocurre algún error en el flujo de salida.
   */
   public void write(byte[] b, int off, int len) throws IOException
   {
      boolean  encolado = false;


      if(!this.socketPTMFImp.isGrupoMcastActivo())
        throw new IOException("Grupo Multicast NO ACTIVO. SOCKET CERRADO");

      if(this.bCerrado)
        throw new IOException("El Flujo de salida ha sido cerrado.");

      if(!this.socketPTMFImp.getColaEmision().esActiva())
        throw new IOException("Se ha desactivado la emisión de datos en el SocketPTMF.");

      if((off == 0) && (b.length <= len))
      {
          // añadir el array de bytes entero...
           encolado = this.socketPTMFImp.getColaEmision().add(b,off,len,false);
      }
      else
      {
         // Comprobar longitud
         if( off + len > b.length)
           throw new IOException("MulticastOutputStream.write: Número de bytes a copiar incorrecto");

         encolado = this.socketPTMFImp.getColaEmision().add(b,off,len,false);
      }

      if (encolado)
         return;
      else
         throw new IOException("MulticastOutputStream.write: No se pudo escribir en el flujo de salida");

   }


  //==========================================================================
  /**
   * Close. Cierra el Flujo de Salida.<bR>
   * El cierre de este flujo produce que se cierren los flujos de entrada
   * ID_SocketInputStream en el lado de los receptores, así se notifica del
   * fin de una transmisión Multicast. Nótese que LA CONEXIÓN MULTICAST
   * NO SE CIERRA.<br>
   * Una vez llamado a close() no se puede escribir en el flujo.
   * Para volver a realizar una transmisión Multicast se debe de llamar
   * de nuevo a getMulticastOutputStream() en el SocketPTMF, este método
   * devolverá un nuevo flujo apto para realizar transferencias.
   * @exception IOException Se lanza si ocurre algún error en el flujo de salida.
   */
   public void close() throws IOException
   {
      byte[] abyte = {(byte)0xFF};

      if(bCerrado)
        return;

      if(!this.socketPTMFImp.isGrupoMcastActivo())
        throw new IOException("Grupo Multicast NO ACTIVO. SOCKET CERRADO");

      if(!this.socketPTMFImp.getColaEmision().esActiva())
        throw new IOException("Se ha desactivado la emisión de datos en el SocketPTMF.");

      // NOTIFICAMOS FIN DE FLUJO ENVIANDO EL BYTE 0xFF junto con el bit
      // FIN TRANSMISION a true.
      this.socketPTMFImp.getColaEmision().add(abyte,0,1,true);

      this.bCerrado = true;
      super.close();
   }
}
