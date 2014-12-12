//============================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	File: ColaEmision.java  1.0 24/11/99
//
//	Description: Clase ColaEmision.
//
//  Historial: 
//	14/10/2014 Change Licence to LGPL
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

package net.iacobus.ptmf;

import java.util.LinkedList;
import java.util.Collections;
import java.util.List;

/**
 * ColaEmision implementa una cola donde se almacenan los datos del
 * usurio en forma de objetos TPDUDatosNormal que encapsula los
 * datos y aptos para procesar y encolar en la Ventana de Emisión.<br>
 * Esta clase es thread-safe.
 * @version  1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 * @see Buffer
 */


class ColaEmision
{

  /**
   * Lista enlazada para almacenar los objetos Buffer que encapsulan los
   * arrays de bytes
   */
  private LinkedList lista = null;

  /** La capacidad de la cola */
  private int iCapacidad = 0;

  /** El tamaño actual de la cola */
  private int iTamaño = 0;

 /**
   * Indica si los datos que vayan llegando a la cola de emisión serán
   * encolados o simplemente no se tendrán en cuenta.
   */
  private boolean bEncolarDatos = true;

  /** Semáfor */
  private Semaforo semaforo = null;

  /** SocketPTMF */
  private SocketPTMFImp socket = null;

  /** Mutex */
  private Mutex mutex = null;

  /**
   * Flag de obtenciones. <br>
   * Utilizado para evitar situaciones de carrera entre un get y un remove.
   * Evita que se solapen buffers en uno en la función add.
   */
  private boolean bGet = false;

  /**
   * Boolean bVacia. Indica si la cola está vacia
   */
   private boolean bVacia = true;
  //==========================================================================
  /**
   * Constructor ColaEmisión
   * @param iCapacidad Capacidad de la cola en bytes.
   * @param socket SocketPTMFImp
   */
  protected ColaEmision( int iCapacidad,SocketPTMFImp socket)
  {
    this.iCapacidad = iCapacidad;
    this.socket = socket;

    lista = new LinkedList();
    try
    {
      semaforo = new Semaforo(true,1);
      mutex = new Mutex();
    }
    catch(ParametroInvalidoExcepcion e)
    {
     Log.log("ColaEmision --> ERROR FATAL",e.getMessage());
    }
   }


  //==========================================================================
  /**
   * Obtener la capacidad de la cola
   * @return un entero con la capacidad de la cola
   */
  int getCapacidad()
  {
    try
    {
      //Sincronizar threads...
      this.mutex.lock();

      return this.iCapacidad;
    }
    finally
    {

     //Sincronizar threads...
     this.mutex.unlock();
    }
  }

 //==========================================================================
  /**
   * Informa si la cola esta vacia
   * @return boolean true , la cola está vacia, false en caso contrario
   */
  boolean  vacia()
  {
    return bVacia;
  }

  //==========================================================================
  /**
   * Obtener el tamaño de la cola
   * @return un entero con el tamaño de la cola
   */
  int  getTamaño()
  {
    try
    {
      //Sincronizar threads...
      this.mutex.lock();

     return this.iTamaño;
    }
    finally
    {
      //Sincronizar threads...
      this.mutex.unlock();
    }
  }

  //==========================================================================
  /**
   * Establece el valor de la variable encolarDatos.
   */
  void setEncolarDatos (boolean bEncolarDatosParam)
  {
      //Sincronizar threads...
      this.mutex.lock();

      this.bEncolarDatos = bEncolarDatosParam;

      //Sincronizar threads...
      this.mutex.unlock();

  }

  //==========================================================================
  /**
   * Verifica si se puede encolar datos en la cola.
   * @return true si la cola esta activa y false en caso contrario.
   */
  boolean esActiva(){ return this.bEncolarDatos; }



  //==========================================================================
  /**
   * Obtiene la lista interna de la cola.
   * @return LinkedList La lista enlazada.
   */
   LinkedList getLista()
   {
      return this.lista;
   }



  //==========================================================================
  /**
   * Añade el RegistroBuffer pasado al principio de la cola de emisión.
   * Utilizado para casos en los que se borre de la cola y se tenga necesidad de
   * volver a añadir.
   * @param buffer El objeto RegistroBuffer a insertar al proincipio de la lista.
   */
  void addFirst(RegistroBuffer buffer)
  {
    try
    {
      //Sincronizar threads...
      this.mutex.lock();

      this.lista.addFirst(buffer);
      this.iTamaño += buffer.getBuffer().getLength();
    }

    finally
    {
      if(this.iTamaño > 0)
        this.bVacia = false;
      else
        this.bVacia = true;

       //Sincronizar threads...
      this.mutex.unlock();

    }
  }


  //==========================================================================
  /**
   * Añadir un array de bytes al final de la cola.<br>
   * El array de bytes es encapsulado en un objeto Buffer y este es añadido
   * a la cola.
   * Si el socket está en modo FIABLE, si el último Buffer está medio lleno,
   * este método intenta llenarlo.
   * <b>Si no hay capacidad suficiente en el buffer el thread se queda bloqueado
   * hasta que se puedan añadir los datos en la cola</b><br>
   * <b> EL ARRAY DE BYTES SE COPIA.</b>
   * @param array Un array de bytes.
   * @param array_offet Offset del array
   * @param array_length Longitud a copiar
   * @param bParamFin Bit de Fin de Transmisión
   * @return true si la operación se ha realizado con éxito, false en caso contrario
   */
  private int iBytesAcumulados = 0;
  boolean add(byte[] array,int array_offset,int array_length,boolean bParamFin)
  {
    final String mn = "ColaEmision.add";
    Buffer buf      = null;   // último Buffer en la cola
    Buffer datos    = null;   // Buffer de datos a añadir a la cola
    boolean bFin = false;     //FIN TRANSMISION


    try
    {
      //Sincronizar threads...
      this.mutex.lock();

      // Tirar los datos si no se tienen que encolar.
      if (!this.bEncolarDatos)
      {
        return false;
      }


      //Comprobar que el array de bytes no es mayor que la capacidad de la cola
      if (array_length > this.iCapacidad)
      {
          throw new PTMFExcepcion("No se pudo insertar los datos en la cola. El dato que se quiere insertar es mayor que la capacidad de la cola.");
      }

      //PONER EL BIT DE FIN EN ESTE BUFFER.
      if(bParamFin)
      {
        bFin = true;
      }


       // Comprobar que hay capacidad en la cola
       while(this.iTamaño + array_length > this.iCapacidad ) //ANTES WHILE
       {
          //Sincronizar threads...
          this.mutex.unlock();

          //
          // No hay capacidad suficiente en el buffer, esperar a que la halla
          //
          // Log.log("COLA EMISION LLENA, DURMIENDO EL THREAD USUARIO","");
          this.semaforo.down();
          // Log.log("THREAD USUARIO DESPERTADO","");

          //Sincronizar threads...
          this.mutex.lock();
       }


       //Encapsular el array de bytes y !!!COPIARLO!!
       datos = new Buffer(array_length);
       datos.addBytes(new Buffer(array),array_offset,0,array_length);

       //Añadir a la cola
        if(!this.getLista().add(new RegistroBuffer(datos,bFin)))
        {
          return false; // No se ha encolado
        }

        // calcular el nuevo tamaño de la cola....
        this.iTamaño += (array_length);




      //ALEX: depuracion, comentar.
      // Log.log("====================================================","");
      // Log.log("ColaEmision.add:",new String(datos.getBuffer()));
      // Log.log("====================================================","");


      return true; //OK. ENCOLADO.
     }
     catch(ParametroInvalidoExcepcion e)
     {
       Log.log(mn,e.getMessage());
       return false;
     }
     catch(PTMFExcepcion e)
     {
       Log.log(mn,e.getMessage());
       return false;
     }


     finally
     {
      if(this.iTamaño > 0)
        this.bVacia = false;
      else
        this.bVacia = true;

      //Sincronizar threads...
      this.mutex.unlock();
     }

  }




  //==========================================================================
  /**
   * Añadir un array de bytes al final de la cola.<br>
   * El array de bytes es encapsulado en un objeto Buffer y este es añadido
   * a la cola.
   * Si el socket está en modo FIABLE, si el último Buffer está medio lleno,
   * este método intenta llenarlo.
   * <b>Si no hay capacidad suficiente en el buffer el thread se queda bloqueado
   * hasta que se puedan añadir los datos en la cola</b><br>
   * <b> EL ARRAY DE BYTES NO SE COPIA?.</b>
   * @param array Un array de bytes.
   * @param array_offet Offset del array
   * @param array_length Longitud a copiar
   * @param bParamFin Bit de Fin de Transmisión
   * @return true si la operación se ha realizado con éxito, false en caso contrario
   */
   /*  2/05/2003 *****************  ANTIGUO ***************************

  private int iBytesAcumulados = 0;
  boolean add(byte[] array,int array_offset,int array_length,boolean bParamFin)
  {
    final String mn = "ColaEmision.add";
    Buffer bufArray = null;   // Buffer para encapsular el array
    Buffer buf      = null;   // último Buffer en la cola
    Buffer datos    = null;   // Buffer de datos a añadir a la cola
    int longNuevoBuffer  = 0; // Longitud del nuevo buffer que se añadirá a la cola
    int longArray        = 0; //Longitud del array de bytes pasados
    int offset       =  0;    // offset del array
    boolean bFin = false;     //FIN TRANSMISION


    try
    {
      //Sincronizar threads...
      this.mutex.lock();

      //Log.log("COLA EMISION. ADD -> tamaño: "+array_length,"");
      //iBytesAcumulados+=array_length;
      //Log.log("COLA EMISION. Bytes Acumulados: "+iBytesAcumulados,"");

     //ALEX: depuracion, comentar.
     //  Log.log("====================================================","");
     //  Log.log("ColaEmision.add:",new String(array,array_offset,array_length));
     //  Log.log("====================================================","");

      //Longitud del array;
      longArray        = array_length;

      // Tirar los datos si no se tienen que encolar.
      if (!this.bEncolarDatos)
      {
        return false;
      }

     /* if(this.getLista().size() != 0)
      {
        //
        // Obtener el último Buffer de la cola
        // Política--> CONSIDERAMOS "BUFFER NO LLENO" cuando el tamaño es
        //             menor de 3/4 del tamaño del Payload del TPDU
        //
        buf = (Buffer) this.lista.getLast();

        if((this.socket.getModo() == PTMF.PTMF_FIABLE || this.socket.getModo() == PTMF.PTMF_FIABLE_RETRASADO )
        && (buf.getLength() < (PTMF.MTU *3)/4)
        && !bGet)
        {
              this.getLista().removeLast();

              //Encapsular el array de bytes y !!!COPIARLO!!
              bufArray = new Buffer(array_length + buf.getLength());
              buf.setOffset(0);
              bufArray.addBytes(buf,0,buf.getLength());
              bufArray.addBytes(new Buffer(array),array_offset,array_length);

              //Ajustar el tamaño...
              tamaño-=buf.getLength();

              longArray = bufArray.getLength();
        }
        else
        {
          //Encapsular el array de bytes y !!!COPIARLO!!
           bufArray = new Buffer(array_length);
           bufArray.addBytes(new Buffer(array),array_offset,0,array_length);
        }
      }
      else
        {
      */    //Encapsular el array de bytes y !!!COPIARLO!!
 /*         bufArray = new Buffer(array_length);
           bufArray.addBytes(new Buffer(array),array_offset,0,array_length);
      //  }


      while(offset < longArray)
      {

        //MODO FIABLE
        if(this.socket.getModo() == PTMF.PTMF_FIABLE || this.socket.getModo() == PTMF.PTMF_FIABLE_RETRASADO)
        {
          //CALCULAR TAMAÑO DEL NUEVO BUFFER
          // En Modo Fiable el tamaño máximo de un TPDU será el MTU.
          if( longArray - offset >  PTMF.MTU)
            longNuevoBuffer = PTMF.MTU; //Buffer máximo
          else
          {
            longNuevoBuffer = longArray - offset;

            //PONER EL BIT DE FIN EN ESTE BUFFER.
            if(bParamFin)
              bFin = true;
          }

        }
        else
        {

          //CALCULAR TAMAÑO DEL NUEVO BUFFER
          //EN Modo NO FIABLE el tamaño máximo de un TPDU será TPDU_MAX_SIZE_PAYLOAD(64Kb)
          if( longArray - offset >  PTMF.TPDU_MAX_SIZE_PAYLOAD)
          {
            longNuevoBuffer = PTMF.TPDU_MAX_SIZE_PAYLOAD; //Buffer máximo

             //TIRAR LO QUE SOBRE SI PASA PTMF.TPDU_MAX_SIZE_PAYLOAD
              // Lo hacemos ajustando el offset..
              offset = longArray;
          }
          else
          {
            longNuevoBuffer = longArray - offset;
          }

          //PONER EL BIT DE FIN EN ESTE BUFFER.
          if(bParamFin)
              bFin = true;

        }


        // Comprobar que hay capacidad en la cola
        while(this.iTamaño + longNuevoBuffer > this.iCapacidad ) //ANTES WHILE
        {
          //Sincronizar threads...
          this.mutex.unlock();

          //
          // No hay capacidad suficiente en el buffer, esperar a que la halla
          //
          // Log.log("COLA EMISION LLENA, DURMIENDO EL THREAD USUARIO","");
          this.semaforo.down();
          // Log.log("THREAD USUARIO DESPERTADO","");

          //Sincronizar threads...
          this.mutex.lock();
        }


        // Crear el nuevo buffer
        datos = new Buffer(longNuevoBuffer);

        /*if(longUltimoBuffer != 0){
          datos.addBytes(buf,0,longUltimoBuffer);
          longUltimoBuffer = 0;
        }
        */

/*        datos.addBytes(bufArray,0,longNuevoBuffer);

        //Añadir a la cola
        if(!this.getLista().add(new RegistroBuffer(datos,bFin)))
          return false; // No se ha encolado

        // calcular el nuevo tamaño de la cola....
        this.iTamaño += (longNuevoBuffer);
        offset += longNuevoBuffer;

      }
      //      Log.log("COLA EMISION. ADD -> tamaño: "+this.tamaño,"");




      return true; //OK. ENCOLADO.
     }
     catch(ParametroInvalidoExcepcion e)
     {
       Log.log(mn,e.getMessage());
       return false;
     }
     catch(PTMFExcepcion e)
     {
       Log.log(mn,e.getMessage());
       return false;
     }


     finally
     {
      if(this.iTamaño > 0)
        this.bVacia = false;
      else
        this.bVacia = true;

      //Sincronizar threads...
      this.mutex.unlock();
     }

  }
*/
  //==========================================================================
  /**
   * Elimina un objeto RegistroBuffer de la cabeza de la cola.<br>
   * @return El objeto RegistroBuffer eliminado o null si no se ha eliminado ninguno
   */
  RegistroBuffer remove()
  {
    RegistroBuffer regBuf = null;

    try
    {
     //Sincronizar threads...
     this.mutex.lock();


      if(lista.size() != 0)
      {
        regBuf = (RegistroBuffer) lista.removeFirst();

        if(regBuf != null)
        {
          this.iTamaño -= regBuf.getBuffer().getLength();
          this.bGet = false;
        }

      }
        //      Log.log("COLA EMISION . REMOVE TAMAÑO-->"+this.tamaño,"");
        //
        // Semáforo arriba, despertar al thread de la aplicación para ver si puede
        // encolar los datos...
        //
        this.semaforo.up();

      //ALEX: depuracion, comentar.
      //Log.log("====================================================","");
      //Log.log("ColaEmision.remove:",new String(regBuf.getBuffer().getBuffer()));
      //Log.log("====================================================","");


      return regBuf;
    }

    finally
    {
      if(this.iTamaño > 0)
        this.bVacia = false;
      else
        this.bVacia = true;

      //Sincronizar threads...
      this.mutex.unlock();
    }
  }



  //==========================================================================
  /**
   * Establece la capacidad de la cola en bytes
   * @param la nueva capacidad de la cola de emisión
   */
  void setCapacidad(int capacidad)
  {
   try
   {
     //Sincronizar threads...
     this.mutex.lock();
       if(capacidad > 0)
       {
        if (capacidad < PTMF.TPDU_MAX_SIZE_PAYLOAD)
          this.iCapacidad = PTMF.TPDU_MAX_SIZE_PAYLOAD;
        else
         this.iCapacidad = capacidad;
       }



     //
     // Semáforo arriba, despertar al thread de la aplicación para ver si puede
     // encolar los datos...
     //
     this.semaforo.up();

   }

   finally
   {
      //Sincronizar threads...
      this.mutex.unlock();
   }

  }

}
