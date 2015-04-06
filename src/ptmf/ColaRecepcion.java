
//============================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	Fichero: ColaRecepcion.java  1.0 24/11/99
//
//
//	Descripción: Clase ColaRcepcion.
//               Clase RegistroEmisor_Buffer
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

package ptmf;

import java.util.LinkedList;
import java.util.TreeMap;
import java.util.Iterator;
import java.io.IOException;

/**
 * ColaRecepcion es un buffer de recepción para los datos que se obtienen
 * del canal multicast, para cada socket Id_Socket del que se obtenga
 * datos se crea un ID_SocketInputStream donde se almacena los datos para
 * que el usuario pueda acceder a ellos mediante los métodos sobreescritos
 * de la clase InputStream.<br>
 * Esta clase es thread-safe.
 * @version  1.0
 * @author
 *  Antonio Berrocal Piris
 *  <A HREF="mailto:AntonioBP@wanadoo.es">(AntonioBP@wanadoo.es)</A><br>
 *  M. Alejandro García Domínguez
 *  <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *
 * @see Buffer
 */


class ColaRecepcion implements TimerHandler
{

  /**
   * TreeMap key: ID_Socket value: ID_SocketInputStream
   */
  private TreeMap treemap = null;

  /** socket */
  private SocketPTMFImp socket = null;

  /** Lista de Objetos Id_Socket_Buffer */
  private LinkedList listaID_Socket_Buffer = null;

  /** La capacidad de la cola */
  private int iCapacidad = 0;

  /** El tamaño actual de la cola */
  private int iTamaño = 0;

  /** Semáforo */
  private Semaforo semaforo = null;

  /** Time-Out*/
  private int iTimeOut = 0;

  /**
   * Indica si los datos que vayan llegando a la cola de recepción serán
   * encolados o simplemente no se tendrán en cuenta.
   */
  private boolean bEncolarDatos = true;

  /** Mutex */
  private Mutex mutex = null;

  /**  Lista de objetos PTMFId_SocketInputStreamListeners  */
  private LinkedList  listaPTMFID_SocketInputStreamListeners = null;

  //==========================================================================
  /**
   * Contructor genérico.
   * @param iCapacidad Capacidad de la cola en bytes.
   * @param socket El objeto SocketPTMFImp
   */
  ColaRecepcion(int iCapacidad, SocketPTMFImp socket)
  {
    this.iCapacidad = iCapacidad;
    this.socket = socket;

    if((socket.getModo() == PTMF.PTMF_FIABLE)||(socket.getModo () == PTMF.PTMF_FIABLE_RETRASADO))
    {
      this.treemap = new TreeMap();
      this.listaPTMFID_SocketInputStreamListeners = new LinkedList();
     }
    else
      this.listaID_Socket_Buffer = new LinkedList();

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
   * Obtener la capacidad de la cola, expresado en bytes.
   * @return un entero con la capacidad en bytes de la cola
   */
  int getCapacidad(){return this.iCapacidad; }

  //==========================================================================
  /**
   * Obtener el tamaño de la cola.El número de bytes que hay en la cola.
   * @return un entero con el tamaño de la cola
   */
  int getTamaño(){ return this.iTamaño; }


  //==========================================================================
  /**
   * Establece el tamaño de la cola
   * @param n_bytes Número de bytes a decrementar el tamaño
   * @return El tamaño de la cola decrementado.
   */
  int decrementarTamaño(int n_bytes)
  {
    try
    {
      //Sincronizar threads...
      this.mutex.lock();


      this.iTamaño -= n_bytes;

      //Log.log("DECrementar tamaño de la ColaRecepcion: -"+n_bytes+" TAMAÑO: "+tamaño,"");
      if(this.iTamaño <0)
        this.iTamaño = 0;

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
   * Establece el valor de la variable encolarDatos. LLama a la función reset()
   * @param bEncolarDatosParam true si se quiere encolar los datos, false en caso contrario.
   */
  void setEncolarDatos (boolean bEncolarDatosParam)
  {
   try
   {
      //Sincronizar threads...
      this.mutex.lock();

     if (bEncolarDatosParam)
     {
       this.bEncolarDatos = true;
       return;
     }

     // encolarDatosParam es false
     if (bEncolarDatosParam)
     {
        //Sincronizar threads...
       this.mutex.unlock();
       this.reset();
       //Sincronizar threads...
       this.mutex.lock();

       this.bEncolarDatos = false;
     }

     return;
   }

   finally
   {
      //Sincronizar threads...
      this.mutex.unlock();
   }

  }

  //==========================================================================
  /**
   * Añadir un objeto Buffer y Address del emisor del buffer al final
   * de la cola.<br>
   * <b> NI EL BUFFER NI EL ID_Socket SE COPIAN.</b>
   * @param emisor El objeto Address del emisor que envió el Buffer.
   * @param buf El Buffer
   * @param bFinTransmision Bit de Fin de Transmision.
   * @return true si la operación se ha realizado con éxito, false en caso contrario
   */
  boolean add(ID_Socket id_socket,Buffer buf,boolean bFinTransmision)
  {
    ID_SocketInputStream in = null;

    try
    {
      //Sincronizar threads...
      this.mutex.lock();


      if (!bEncolarDatos)
        return true;

      if(buf == null)
        throw new NullPointerException("ColaRecepcion.add: Parámetro buf nulo");

      int longitudBuf = buf.getLength();

      if(longitudBuf + this.iTamaño > this.iCapacidad)
        return false;

      if(id_socket == null)
        throw new NullPointerException("ColaRecepcion.add: Parámetro id_socket nulo");


      //Modo Fiable
      if((this.socket.getModo() == PTMF.PTMF_FIABLE)||(this.socket.getModo()==PTMF.PTMF_FIABLE_RETRASADO))
      {
        if(this.treemap.containsKey(id_socket))
          in = (ID_SocketInputStream)this.treemap.get(id_socket);

        else
        {
          in = new ID_SocketInputStream(id_socket,this.socket);
          this.treemap.put(id_socket,in);
          //Log.log("NUEVO ID_SOCKETIMPUTASTREAM para  el socket:" +id_socket,"");
          //Notificar Nuevo ID_SocketInputStream
          this.sendPTMFEventId_SocketInputStream(in);
       }


        //COMPROBAR SI EL DATO RECIBIDO ES FIN DE FLUJO
        if(!bFinTransmision)
        {
          // Añadir el Buffer a la lista
          if (!in.add(new RegistroBuffer(buf,bFinTransmision)))
            return false;
          // calcular el nuevo tamaño....
          this.iTamaño += buf.getLength();
          // Log.log ("ColaRecepcion","Añadido datos sin problemas.");
        }
        else      //Si Fin Emision --> ELIMINAR EL ID_SOCKETINPUTSTREAM DEL TREEMAP
        {
          //try
          //{
           //if ((buf.getLength() != 1) && (buf.getByte(0) != (short)0xFF))
           //{
              // Añadir el Buffer a la lista
              if (!in.add(new RegistroBuffer(buf,bFinTransmision)))
                return false;
              //Log.log ("ColaRecepcion","Añadido datos sin problemas.");
              // calcular el nuevo tamaño....
              this.iTamaño += longitudBuf;

          // }
          //}
          //catch(ParametroInvalidoExcepcion e){;}

          //FIN DE FLUJO.
          this.treemap.remove(id_socket);
        }

      }
      else //Modo No-Fiable y no_fiable_ordenado
      {
        this.listaID_Socket_Buffer.add(new RegistroID_Socket_Buffer(id_socket,buf,bFinTransmision));

        // calcular el nuevo tamaño....
        this.iTamaño += longitudBuf;
      }


      //levantar el semáforo
      this.semaforo.up();

      //Sincronizar threads...
      this.mutex.unlock();

      //Notificar al usuario que se han recibido nuevos datos...
      // NOTA Esta notificacion es valida si el usuario se ha registrado
      // en las clases DatagramSocket o MulticastInputStream.
      this.socket.sendPTMFEventDatosRecibidos("Datos Recibidos",buf.getLength());

      //Sincronizar threads...
      this.mutex.lock();
      //Log.log("COLA RECEPCION "," ADD -> Tamaño: "+buf.getLength());
      //Log.log("COLA RECEPCION ",""+buf);

      return true;

    }
    finally
    {
      //Sincronizar threads...
      this.mutex.unlock();
    }
  }

 //==========================================================================
  /**
   * Un método para registrar un objeto que implementa la interfaz PTMFDatosRecibidosListener.
   * La interfaz PTMFDatosRecibidosListener se utiliza para notificar a las clases que se
   * registren de un evento PTMFEventIDGL
   * @param obj El objeto Indohandler.
   * @return PTMFExcepcion Se lanza cuando ocurre un error al registrar el
   *  objeto callback
   */
  public void addPTMFID_SocketInputStreamListener(PTMFID_SocketInputStreamListener obj)
  {
      //Log.log("\n\nLISTENER AÑADIDO","");
      this.listaPTMFID_SocketInputStreamListeners.add(obj);
  }

  //==========================================================================
  /**
   * Elimina un objeto PTMFDatosRecibidosListener
   */
  public void removePTMFID_SocketInputStreamListenerListener(PTMFID_SocketInputStreamListener obj)
  {
    this.listaPTMFID_SocketInputStreamListeners.remove(obj);
  }

  //==========================================================================
  /**
   * Eliminar un ID_SocketInputStream asociado a un ID_Socket.
   * NOTA: ES LLAMADO POR ID_SCOKETINPUTSTREAM EN EL CLOSE()
   *        Y POR DATOSTHREAD
   * @param id_socket El objeto h aeliminar.
   */
  void remove(ID_Socket id_socket)
  {
    try
    {
      //Sincronizar threads...
      this.mutex.lock();

     //Modo Fiable
     if((this.socket.getModo() == PTMF.PTMF_FIABLE)||(this.socket.getModo()==PTMF.PTMF_FIABLE_RETRASADO))
     {
      //Si hay datos en el flujo ID_SocketInputStream asociado,
      //Añadir el ID_Socket a eliminar al treemap de eliminación ...
      ID_SocketInputStream id = (ID_SocketInputStream)this.treemap.get(id_socket);
      if(id!=null)
        //Cerrar el flujo...
        id.closeStream();

      //Eliminar el flujo...
      this.treemap.remove(id_socket);
      //Log.log("Eliminado el flujo para el socket:" +id_socket,"");

     }
     //NOTA: El tamaño se actualiza cuando se lee del flujo ID_SocketInputStream.

     return;
    }
    finally
    {
      //Sincronizar threads...
      this.mutex.unlock();
    }

  }

  //==========================================================================
  /**
   * Elimina todos los recursos de la cola de Recepcion
   */
  void reset()
  {
    try
    {
      //Sincronizar threads...
      this.mutex.lock();

      this.iTamaño = 0;

      if((this.socket.getModo() == PTMF.PTMF_FIABLE)||(this.socket.getModo() ==PTMF.PTMF_FIABLE_RETRASADO))
        // vaciar el treemap
        this.treemap.clear();
      else
        this.listaID_Socket_Buffer.clear();
    }
    finally
    {
      //Sincronizar threads...
      this.mutex.unlock();
    }
  }

  //==========================================================================
  /**
   * Envía un evento PTMFEvent del tipo EVENTO_DATOS_RECIBIDOS
   * con una cadena informativa.
   * @param mensaje Mensaje Informativo
   * @param id_socket Objeto ID_Socket que ha sido eliminado
   */
   private void sendPTMFEventId_SocketInputStream(ID_SocketInputStream id)
   {
    if (id == null)
      return;

    if (this.listaPTMFID_SocketInputStreamListeners.size() != 0)
    {
     //Log.log("\n\nENVIANDO EVENTGO","");

     PTMFEventID_SocketInputStream evento = new PTMFEventID_SocketInputStream(this.socket,"Nuevo ID_SocketInputStream",id);

     Iterator iterator = this.listaPTMFID_SocketInputStreamListeners.listIterator();
     while(iterator.hasNext())
     {
        PTMFID_SocketInputStreamListener ptmfListener = (PTMFID_SocketInputStreamListener)iterator.next();
        ptmfListener.actionPTMFID_SocketInputStream(evento);
     }
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

      if(iCapacidad > 0)
        this.iCapacidad = iCapacidad;


      return;
    }
    finally
    {
      //Sincronizar threads...
      this.mutex.unlock();

    }
  }

  //==========================================================================
  /**
   * Obtiene el treemap de la cola.
   * @return El treemap de la cola.
   */
  TreeMap getTreeMap()
  {
    try
    {
      //Sincronizar threads...
      this.mutex.lock();

      return this.treemap;
    }
    finally
    {
       //Sincronizar threads...
      this.mutex.unlock();
    }
  }

  //==========================================================================
  /**
   * Obtiene el siguiente dato recibido.<br>
   * ESTE MÉTODO SOLO ES VÁLIDO EN EL MODO NO-FIABLE.<BR>
   * Para averiguar si hay datos en la cola utilice la función getTamaño()<br>
   * @param id_socket ID_Socket del socket emisor de los datos
   * @param buf Buffer con los datos emitidos por el socket Id_Socket
   * @return true si se ha realizado la operación con éxito, falso
   *  en caso contrario.
   * @exception java.io.InterruptedIOException Se lanza si se alcanza el TimeOut.
   */
  RegistroID_Socket_Buffer remove() throws java.io.InterruptedIOException
  {
    RegistroID_Socket_Buffer id_socket_buffer = null;

    try
    {
     //Sincronizar threads...
     this.mutex.lock();


     for(;;)
     {
      if(this.socket.getModo()  == PTMF.PTMF_NO_FIABLE)
      {
       if((this.iTamaño > 0) && (!this.listaID_Socket_Buffer.isEmpty()))
       {
        id_socket_buffer= (RegistroID_Socket_Buffer) this.listaID_Socket_Buffer.removeFirst();

        //--> ACTUALIZAR TAMAÑO
        this.iTamaño -= id_socket_buffer.getBuffer().getLength();
       }
       else if(this.iTamaño <= 0)
       {
          this.socket.getTemporizador().cancelarFuncion(this,0);

          //Establecer SO_TIMEOUT
          if(this.iTimeOut > 0)
            this.socket.getTemporizador().registrarFuncion(iTimeOut,this,0);


          //Sincronizar threads...
          this.mutex.unlock();

          //
          // No hay datos que leer, bloquear al thread
          //
          this.semaforo.down();

          //Sincronizar threads...
          this.mutex.lock();

          if(this.iTamaño > 0)
           continue ;
          else   //Me ha despertado el TIME OUT....
            throw new java.io.InterruptedIOException("TIME OUT");
       }
      }

      return id_socket_buffer;

     }//fin-for
    }
    finally
    {
      //Sincronizar threads...
      this.mutex.unlock();

    }
  }


  /**
   * Obtiene el siguiente flujo ID_socketInputStream
   * @return ID_SocketInputStream
   */
  ID_SocketInputStream nextID_SocketInputStream()
  {
    Iterator iterator = null;
    try
    {
     //Sincronizar threads...
     this.mutex.lock();

     if(iterator == null)
       iterator = this.treemap.values().iterator();

      if(iterator == null) return null;

      while(iterator.hasNext())
      {
         ID_SocketInputStream in = (ID_SocketInputStream)iterator.next();

         if(in.available() > 0) //¿Tiene este buffer datos, palomo?
          return in;
         //else          //¿HAy que eliminar el Flujo? --> FIN
         //  iterator.remove();
     }

      return null; // No se ha encontrado un flujo con datos.
    }
    catch(IOException e)
    {
     return null;
    }

    finally
    {
      //Sincronizar threads...
      this.mutex.unlock();
    }
  }

  //==========================================================================
  /**
   * Establece el tiempo de espera máximo que el thread de usuario espera
   * en una llamada al método receive() sin que hallan llegado datos.
   * @param iTiempo Tiempo máximo de espera en mseg. 0 espera infinita.
   */
  void setTimeOut(int iTiempo)
  {
    this.iTimeOut = iTiempo;

    if(this.iTimeOut == 0)
      this.socket.getTemporizador().cancelarFuncion(this,0);
  }

  //==========================================================================
  /**
   * Devuelve el tiempo de espera máximo que el thread de usuario espera
   * en una llamada al método receive() sin que hallan llegado datos.
   * @return  iTiempo Tiempo máximo de espera en mseg. 0 espera infinita.
   */
  int getTimeOut()
  {
     return this.iTimeOut;
  }

  /** TimerCallback */
  public void TimerCallback(long larg1,Object obf1)
  {
    this.semaforo.up();
  }
}





