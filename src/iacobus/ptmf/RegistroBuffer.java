//============================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	Fichero: RegistroBuffer.java  1.0 24/9/99
//

//	Descripción: Clase RegistroBuffer.
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
//------------------------------------------------------------


package iacobus.ptmf;

/** Clase que encapsula un Buffer y el bit de Fin de transmisión */
public class RegistroBuffer
{

  /** Buffer */
  private Buffer buffer = null;

  /** Bit de Fin de Transmisión */
  private boolean bFinTransmision = false;

  //==========================================================================
  /**
   * Contructor.
   * @param buf. Buffer
   * @param bFinTransmision BBit de Fin de Transmisión.
   */
   public RegistroBuffer(Buffer buf,boolean bFinTransmision)
  {
      this.buffer = buf;
      this.bFinTransmision = bFinTransmision;

      if(this.buffer== null)
        Log.log("REGISTRO_BUFFER ---> BUFFER NULO¡¡¡¡¡","");
  }

  //==========================================================================
  /**
   * Obtiene el buffer.
   * @return Buffer
   */
   Buffer getBuffer() {return this.buffer;}

  //==========================================================================
  /**
   * Obtiene el bit de Fin de transmisión
   * @return boolean
   */
   boolean esFinTransmision() {return this.bFinTransmision;}

}