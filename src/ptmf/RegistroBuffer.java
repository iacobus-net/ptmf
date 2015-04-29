//============================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	Fichero: RegistroBuffer.java  1.0 24/9/99
//
//	Descripción: Clase RegistroBuffer.
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