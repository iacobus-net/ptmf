//============================================================================
//
//	Copyright (c) 1999 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	File: MulticastOutputStream.java  1.0 24/11/99
//
//	Description: Clase MulticastOutputStream.
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


package iacobus.ptmf;

import java.io.OutputStream;
import java.io.IOException;

/**
 * MulticastOutputStream es un flujo de salida para SocketPTMF.
 * @version  1.0
 * @author M. Alejandro Garc�a Dom�nguez
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
  /** Devuleve true si el flujo est� cerrado */
  boolean isClose(){return this.bCerrado;}

  //==========================================================================
  /**
   * Escribe un byte en el flujo de salida.
   * @param b Un entero, se escibe s�lo los 8 bits m�s bajos, los 24 restantes
   * se descartan.
   * @exception IOException Se lanza si ocurre alg�n error en el flujo de salida.
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
   * @exception IOException Se lanza si ocurre alg�n error en el flujo de salida.
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
   * @param len N�mero de bytes a copiar.
   * @exception IOException Se lanza si ocurre alg�n error en el flujo de salida.
   */
   public void write(byte[] b, int off, int len) throws IOException
   {
      boolean  encolado = false;


      if(!this.socketPTMFImp.isGrupoMcastActivo())
        throw new IOException("Grupo Multicast NO ACTIVO. SOCKET CERRADO");

      if(this.bCerrado)
        throw new IOException("El Flujo de salida ha sido cerrado.");

      if(!this.socketPTMFImp.getColaEmision().esActiva())
        throw new IOException("Se ha desactivado la emisi�n de datos en el SocketPTMF.");

      if((off == 0) && (b.length <= len))
      {
          // a�adir el array de bytes entero...
           encolado = this.socketPTMFImp.getColaEmision().add(b,off,len,false);
      }
      else
      {
         // Comprobar longitud
         if( off + len > b.length)
           throw new IOException("MulticastOutputStream.write: N�mero de bytes a copiar incorrecto");

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
   * ID_SocketInputStream en el lado de los receptores, as� se notifica del
   * fin de una transmisi�n Multicast. N�tese que LA CONEXI�N MULTICAST
   * NO SE CIERRA.<br>
   * Una vez llamado a close() no se puede escribir en el flujo.
   * Para volver a realizar una transmisi�n Multicast se debe de llamar
   * de nuevo a getMulticastOutputStream() en el SocketPTMF, este m�todo
   * devolver� un nuevo flujo apto para realizar transferencias.
   * @exception IOException Se lanza si ocurre alg�n error en el flujo de salida.
   */
   public void close() throws IOException
   {
      byte[] abyte = {(byte)0xFF};

      if(bCerrado)
        return;

      if(!this.socketPTMFImp.isGrupoMcastActivo())
        throw new IOException("Grupo Multicast NO ACTIVO. SOCKET CERRADO");

      if(!this.socketPTMFImp.getColaEmision().esActiva())
        throw new IOException("Se ha desactivado la emisi�n de datos en el SocketPTMF.");

      // NOTIFICAMOS FIN DE FLUJO ENVIANDO EL BYTE 0xFF junto con el bit
      // FIN TRANSMISION a true.
      this.socketPTMFImp.getColaEmision().add(abyte,0,1,true);

      this.bCerrado = true;
      super.close();
   }
}
