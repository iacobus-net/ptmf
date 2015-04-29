//============================================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: SocketPTMF.java  1.0 23/11/99
//
// 	Autores: 	M. Alejandro García Domínguez (alejandro.garcia.dominguez@gmail.com)
//						Antonio Berrocal Piris
//
//	Descripción: Clase Semaforo.
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


