//============================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	Fichero: PTMFCipher.java  1.0 24/11/99
//
//
//	Descripción: Interfaz PTMFCipher
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


package ptmf;

/**
 * La interfaz PTMFCipher se utiliza para proporcionar seguridad al Canal Multicast.
 * Su única misión es proporcionar dos objetos javax.crypto.Cipher para codificar
 * y descodificar, además de un método de iniciación del codificador y del descodificador.
 * En este método se deberá llamar al método init() del los objetos javax.crypto.Cipher.
 */
public interface PTMFCipher
{

  public javax.crypto.Cipher getCipher();

  public javax.crypto.Cipher getUncipher();

  public void init();
}
