//============================================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: SocketPTMF.java  1.0 23/11/99
//
// 	Autores: 	M. Alejandro García Domínguez (alejandro.garcia.dominguez@gmail.com)
//						Antonio Berrocal Piris
//
//	Descripción: Clase Semaforo.
//  Authors: 
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
//
//----------------------------------------------------------------------------


package ptmf;


/**
 * Clase Semáforo. La clase semáforo se utiliza para sincronizar varios
 * threads.
 * <br>Implementación de un semáforo binario y contador.
 * @version  1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */
public class Semaforo
{
  /** Contador del semáforo */
  int contador = 0;

  /** Contador inicial */
  int contadorInicial = 0;

  /** Objetos en espera */
  int nObjetosEspera = 0;

  public  Semaforo(boolean binario, int contador) throws ParametroInvalidoExcepcion
  {
    if (binario && (contador > 1))
      throw new ParametroInvalidoExcepcion("Semaforo.Semaforo","Intento de crear un semáforo binario con un contador mayor que uno.");

    if (contador < 0)
      throw new ParametroInvalidoExcepcion("Semaforo.Semaforo","El parámetro contador es menor que cero.");

    this.contador = contador;
    this.contadorInicial = contador;
  }

  public synchronized void down()
  {
    //    Log.log("Semaforo "+this,"DOWN. Contador: "+contador);
    if(this.contador > 0)
      this.contador--;

    if (this.contador == 0)
    {
      try
      {
        nObjetosEspera++;
        this.wait();
      }
      catch(InterruptedException e)
      {
       Log.log("Semaforo.down",e.getMessage());
       Log.exit(-1);
      }
    }

  }

  public synchronized void up()
  {
   //   Log.log("Semaforo "+this,"UP. Contador: "+contador+" en Espera: "+nObjetosEspera );
   if (this.contador<=0)
   {
    nObjetosEspera--;

    if(nObjetosEspera == 0)
      incrementar();

    this.notify();
   }
   else
    incrementar();
  }

  private void incrementar()
  {
    this.contador++;
    if (this.contador > this.contadorInicial)
            this.contador = this.contadorInicial;
  }
}


