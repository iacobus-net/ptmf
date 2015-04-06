//============================================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: ID_SocketInputStream.java  1.0 24/11/99
//
//	Description: Clase ID_SocketInputStream.
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
import java.io.InterruptedIOException;
import java.util.LinkedList;
import java.util.Iterator;

/**
 * ID_SocketInputStream es un stream de entrada para un determinado
 * ID_Socket.<br>
 * Los m�todos read() son s�ncronos. Utilice los Eventos PTMFEvent para
 * utilizarlos de forma as�ncrona.<br>
 * <b> ESTA CLASE ES THREAD-SAFE </B>
 * @version  1.0
 * @author M. Alejandro Garc�a Dom�nguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */
public class ID_SocketInputStream extends InputStream implements TimerHandler, Comparable
{
  /** Lista para almacenar los objetos Buffer */
  private LinkedList lista = null;

  /** ID_Socket de este flujo */
  private ID_Socket id_socket = null;

  /** N�mero de bytes disponible en la lista */
  private int tama�o = 0;

  /** Buffer que se est� procesando */
  private Buffer buf = null;

  /** SocketPTMFImp */
  private SocketPTMFImp socket = null;

  /** Flag de Flujo Cerrado*/
  private boolean bClose = false;

  /** Sem�foro de espera para SO_TIMEOUT */
  private Semaforo semaforo = null;

  /** BytesCopiados */
  private int bytesCopiados = -1;

  /**  Lista de objetos PTMFDatosRecibidosListeners  */
  private LinkedList  listaPTMFDatosRecibidosListeners = null;


  /** Mutex*/

  private Mutex mutex = null;


  //==========================================================================
  /**
   * Constructor.
   */
  ID_SocketInputStream(ID_Socket id_socket,SocketPTMFImp socket)
  {
   super();

   this.id_socket = id_socket;
   this.lista = new LinkedList();
   this.socket = socket;

   try
   {
     semaforo = new Semaforo(true,1);
     mutex = new Mutex();
     listaPTMFDatosRecibidosListeners  = new LinkedList();
   }
   catch(ParametroInvalidoExcepcion e)
   {
      Log.exit(-1);
   }
  }

  //==========================================================================
  /**
   * Interfaz TimerHandler
   */
   public void TimerCallback(long lArg,Object obj)
   {
      //Levantar el sem�foro
      this.semaforo.up();
   }

  //==========================================================================
  /**
   * Devuelve el ID_Socket
   * @return El Id_Socket de este flujo de entrada
   */
  public ID_Socket getID_Socket(){return this.id_socket; }


  //==========================================================================
  /**
   * Un m�todo para registrar un objeto que implementa la interfaz PTMFDatosRecibidosListener.
   * La interfaz PTMFDatosRecibidosListener se utiliza para notificar a las clases que se
   * registren de un evento PTMFEventDatosRecibidos
   * @param obj El objeto PTMFDatosRecibidosListener
   * @return PTMFExcepcion Se lanza cuando ocurre un error al registrar el
   *  objeto callback
   */
  public void addPTMFDatosRecibidosListener(PTMFDatosRecibidosListener obj)
  {
    this.listaPTMFDatosRecibidosListeners.add(obj);
  }

  //==========================================================================
  /**
   * Elimina un objeto PTMFDatosRecibidosListener
   * @param obj El objeto que implementa la interfaz PTMFDatosRecibidosListener
   */
  public void removePTMFDatosRecibidosListener(PTMFDatosRecibidosListener obj)
  {
    this.listaPTMFDatosRecibidosListeners.remove(obj);
  }

  //==========================================================================
  /**
   * Env�a un evento PTMFEvent del tipo EVENTO_DATOS_RECIBIDOS
   * con una cadena informativa.
   * @param mensaje Mensaje Informativo
   * @param id_socket Objeto ID_Socket que ha sido eliminado
   */
   private void sendPTMFEventDatosRecibidos(String mensaje,int nBytes)
   {

    if (this.listaPTMFDatosRecibidosListeners.size() != 0)
    {
     PTMFEventDatosRecibidos evento = new PTMFEventDatosRecibidos(this.socket,mensaje,nBytes);

     Iterator iterator = this.listaPTMFDatosRecibidosListeners.listIterator();
     while(iterator.hasNext())
     {
        PTMFDatosRecibidosListener ptmfListener = (PTMFDatosRecibidosListener)iterator.next();
        ptmfListener.actionPTMFDatosRecibidos(evento);
     }
    }
   }

  //==========================================================================
  /**
   *  A�ade un objeto Buffer a la lista
   *  @param Buf Un objeto RegistroBuffer
   *  @return true si el objeto se a�adido a la lista con �xito, false en caso
   *    contrario.
   */
   boolean add(RegistroBuffer regBuf)
   {
     try
     {
      //Sincronizr threads
      mutex.lock();

      if(this.lista.add(regBuf))
      {
        //buf.setOffset(0);
        this.tama�o += regBuf.getBuffer().getLength(); //Aumentar el tama�o

        //Notificar datos...
        sendPTMFEventDatosRecibidos("Datos recibidos",regBuf.getBuffer().getLength());

        this.semaforo.up();
        return true;
      }
      else
          return false;
     }
     finally
     {
        //Sincronizar threads..
        mutex.unlock();
     }
   }


  //==========================================================================
  /**
   * Lee el siguiente byte de datos desde el flujo de entrada.
   * El valor del byte es devuelto como un "int" en el rango 0 a 255.
   * El valor -1 se devuelve si se ha llegado al final del flujo
   * Este m�todo bloquea hasta que hay datos disponibles, se alcanza el final
   * del flujo o se lanza una excepci�n.
   *
   * @return El siguiente byte de datos, o -1 si se detecta el final del flujo.
   * @throws IOException si ocurre una excepci�n.
   */
   public int read()  throws IOException
   {
     int offset = 0;
     int valor = 0;

     if (bClose)
       return -1;
      //hrow new IOException("El Flujo ID_SocketInputStream est� cerrado.");

     if(this.lista == null) return -1;
     try
     {
       //Sincronizar threads
       mutex.lock();

       if(this.buf == null){
         if(!getNextBuffer())
            return -1; // No se ha podido obtener un nuevo Buffer, se ha alcanzado el final del flujo
       }
       else
       {
         offset = this.buf.getOffset();

         if( offset >= this.buf.getLength())
           if(!getNextBuffer())
              return -1; // No se ha podido obtener un nuevo Buffer, se ha alcanzado el final del flujo

         offset = this.buf.getOffset();
       }

       // Obtener el byte
       valor = (int) this.buf.getByte(offset);
       this.buf.setOffset(offset+=1);

       //Quitar en 1 el tama�o
       this.tama�o -= 1;

       this.mutex.unlock();
       //Decremenar tama�o de la cola de recepci�n
       this.socket.getColaRecepcion().decrementarTama�o(1);
       this.mutex.lock();

       return (valor);
     }

     finally
     {
       // Liberar R.C
       mutex.unlock();
     }
   }


   //==========================================================================

   /**
    * Obtiene el siguiente buffer de la lista.

    * Si la lista est� vac�a se duerme

    * @return Devuelve true si se ha obtenido el Buffer false si no hay datos.

    * @exception InterruptedIOException Se lanza si se ha alcanzo

    */

   private boolean getNextBuffer() throws InterruptedIOException

   {
       String mn = "ID_SocketInputStream.getNextBuffer.";
       boolean in = false;


       for(;;)

       {

        // Si no hay datos --> devolver false;

        if (this.lista.isEmpty())
        {
          if(this.bClose) //---> FIN FLUJO.
            return false;

          if(in)
          { //Si ya hemos entrado y sigue sin haber datos, regresamos, TIME_OUT excedido.
            throw new java.io.InterruptedIOException("TIME_OUT");
          }

          //Cancelar el temporizador....
          this.socket.getTemporizador().cancelarFuncion(this,0);

          //Si ya hemos leido algo, no esperar, regresar...
          if (bytesCopiados > 0)
            return false;

          //Establecer el Time_OUT....
          if(this.socket.getColaRecepcion().getTimeOut() > 0)
                      this.socket.getTemporizador().registrarFuncion(this.socket.getColaRecepcion().getTimeOut(),this,0);

          mutex.unlock();

          // A dormir....
          this.semaforo.down();
          mutex.lock();
          in = true;
          continue ;
        }
        else
        {
         RegistroBuffer regBuf = (RegistroBuffer) this.lista.removeFirst();
         this.buf = regBuf.getBuffer();
         this.buf.setOffset(0);

         //Comprobar Fin de Flujo
         if(regBuf.esFinTransmision())
         {
          this.bClose = true;
          Log.debug(Log.ID_SOCKETINPUTSTREAM,mn,"ID_SocketInputStream: getNextBuffer() --> FIN DEL FLUJO ");
          //Eliminar este ID_SocketInputStream de la Cola de Recepcion
          this.socket.getColaRecepcion().remove(this.id_socket);
          return false;
         }

         return true;
        }
       }
   }

  //==========================================================================
  /**

   * Lee algunos bytes del flujo de entrada y los almacena dentro del array buf

   * El n�mero de bytes le�do se devuelve como un entero. El m�todo bloquea

   * hasta que haya datos disponibles, se detecte el final del flujo o se lance

   * una excepci�n.

   * Si b es nulo, se lanza una excepci�n NullPointerException.

   * Si la longitud de b es cero, entonces no se leen bytes y se devuelve 0;

   * en caso contrario hay un intento de leer almenos un byte.

   * Si no hay bytes disponibles porque se ha llegado al final del flujo, se

   * devuelve el valor -1.

   * El primer byte le�do se almacena dentro del elemento b[0], el siguiente

   * dentro del b[1] y as� en adelante.

   * El n�mero de bytes le�do es, como mucho, igual a la longitud de b,

   * sea K el n�mero de bytes actualmente le�dos; esos bytes ser�n almacenados

   * entre los elementos b[0] a b[k-1], dejando los elementos b[k] hasta b[b.length-1]

   * sin afectar.

   * Si el primer byte no puede ser le�do por cualquier raz�n  (distinta de la

   * de final del fichero) entonces se lanza una excepci�n IOException.

   * Una excepci�n IOException es lanzada en particular si el flujo de entrada

   * ha sido cerrado.

   * El m�todo read(b) tiene el mismo efecto como: <br>
   *  read(b, 0, b.length)
   * @params b El buffer en el que son almacenados los datos.
   * @return El n�mero de bytes totales le�dos y almacenados en el buffer, o -1
   *  si no hay m�s datos porque se ha llegaso al final del flujo
   * @exception IOException - si ocurre un error de I/O
   * @see read(byte[], int, int)
   */
  public int read(byte[] b) throws IOException
  {
    return this.read(b,0,b.length);
  }

  //==========================================================================
  /**
   * Devuelve true si el flujo est� cerrado
   */
   boolean isClose(){ return this.bClose;}

  //==========================================================================
  /**
   * Lee hasta la longitud de bytes de datos desde el flujo de entrada y los
   * almacena en el array de bytes. Se intentar� leer tantos bytes como sea
   * posible. El n�mero de bytes le�do es devuelto como un entero.
   * Este m�todo bloquea hasta que haya datos disponibles, el final del flujo sea
   * detectado o se lance una excepci�n IOException.
   * Si b es null se lanza una excepci�n IOException.
   * Si off es negativa, o len es negativa, o off es mayor que la longitud
   * del array b entonces se lanza una excepci�n IndexOutOfBoundsException.
   * Si len es cero, entonces no se leen bytes  y se devuelve 0; en caso
   * contrario hay un intento de leer almenos un byte.
   * Si no hay bytes disponibles porque el flujo est� al final, se devuelve el
   * valor -1.
   * El primer byte le�do se almacena dentro del elemento b[0], el siguiente
   * dentro del b[1] y as� en adelante.

   * El n�mero de bytes le�do es, como mucho, igual a la longitud de b,

   * sea K el n�mero de bytes actualmente le�dos; esos bytes ser�n almacenados

   * entre los elementos b[0] a b[k-1], dejando los elementos b[k] hasta b[b.length-1]

   * sin afectar.

   * Si el primer byte no puede ser le�do por cualquier raz�n  (distinta de la

   * de final del fichero) entonces se lanza una excepci�n IOException.

   * Una excepci�n IOException es lanzada en particular si el flujo de entrada

   * ha sido cerrado.

   * @params b   El buffer en el que son almacenados los datos.
   * @param off El offset inicial del array b en la  que son copiados los datos.
   * @param len La longitud m�xima de los datos a leer.
   * @return El n�mero de bytes totales le�dos y almacenados en el buffer, o -1
   *  si no hay m�s datos porque se ha llegado al final del flujo
   * @exception IOException - si ocurre un error de I/O.
   */
   public int read(byte[] b, int off, int len) throws IOException
   {
     int offset = 0;
     int bytesACopiar = 0;
     bytesCopiados = -1;

     if (bClose)
      return -1;
      //throw new IOException("El Flujo ID_SocketInputStream est� cerrado.");

     if(this.lista == null) return -1;

     try
     {
       //Sincronizar threads
       mutex.lock();

       //establecer cuantos Bytes hay que copiar....
       if(off + len > b.length)
        bytesACopiar = b.length - off;
       else
        bytesACopiar = len;

       for(;;)
       {
         //Si el buffer es null, BUSCAR UNO.
         if(this.buf == null)
         {
           //Obtener un nuevo buffer de datos..
           if(!getNextBuffer())
              return bytesCopiados; // No se ha podido obtener un nuevo Buffer,
                                 // se ha alcanzado el final del flujo.
         }
         else         //Si el buffer no es null, ver si el offset del Buffer es mayot que la longitud, entonces busca un nuevo buffe porque no se puede obtener m�s datos.
         {
           offset = this.buf.getOffset();

           if( offset >= this.buf.getLength())
           {
             if(!getNextBuffer())
               return bytesCopiados; // No se ha podido obtener un nuevo Buffer, se ha alcanzado el final del flujo
             else
               offset = this.buf.getOffset();
           }
         }

         // Si la cantidad de datos disponibles en el buffer es mayor o igual al n� de BytesACopiar
         if( buf.getLength() - offset >= bytesACopiar)
         {
            if(bytesCopiados == -1)
              bytesCopiados = 0;

            // Copiar n� bytesACopiar.
            offset = this.buf.getOffset();
            System.arraycopy(buf.getBuffer(),offset,b,bytesCopiados + off,bytesACopiar);

            this.tama�o -= bytesACopiar;
            bytesCopiados += bytesACopiar;
            buf.setOffset(offset+bytesACopiar);

            this.mutex.unlock();
            //Decremenar tama�o de la cola de recepci�n
            this.socket.getColaRecepcion().decrementarTama�o(bytesACopiar);
            this.mutex.lock();

            return (bytesCopiados);
         }
         else //Si el Buffer tiene menos datos que el n� de bytes solicitados para copiar....
         {
            if(bytesCopiados == -1)
               bytesCopiados = 0;

            //Copia lo que queda del Buffer y buscar m�s datos...
            offset = this.buf.getOffset();
            System.arraycopy(buf.getBuffer(),offset,b,bytesCopiados+off,buf.getLength() - offset);

            bytesCopiados += (buf.getLength() - offset);  //BYtes copiados
            buf.setOffset(buf.getLength() - offset);
            bytesACopiar -= (buf.getLength() - offset); //Menos bytes a copiar
            this.tama�o -= (buf.getLength() - offset);  //Disminuir el tama�o

            this.mutex.unlock();
            //Decremenar tama�o de la cola de recepci�n
            this.socket.getColaRecepcion().decrementarTama�o(buf.getLength() - offset);
            this.mutex.lock();

            this.buf = null; //Este buffer ya se ha acabado, obtener Nuevo buffer

         }
       }
     }

     finally
     {
       // Liberar R.C
       mutex.unlock();
     }

   }


  //==========================================================================
  /**
   * Descarta n bytes de datos de este flujo de entrada.
   * El n�mero de bytes descartados es devuelto.
   * Si n es negativo, no se descarta ning�n byte.
   * @params n El n�mero de bytes a descartar.
   * @return El n�mero actual de bytes descartados.
   * @exception IOException - si ocurre un error de I/O
   */
  public long skip(long n) throws IOException
  {
    byte[] temp = new byte[(int)n];

    return (long)this.read(temp);
  }


  //==========================================================================
  /**
   * Devuelve el n�mero de bytes que pueden ser le�dos (o descartados) de
   * este flujo de entrada sin bloquear por la siguiente llamada a un m�todo
   * de lectura.
   * @return El n�mero de bytes que pueden ser le�dos desde este flujo de entrada.
   * @exception IOException - si ocurre un error de I/O
   */
  public int available() throws IOException
  {
    if (bClose)
      return 0;

    return (int)this.tama�o;
  }


  //==========================================================================
  /**
   * Notifica al fujo de su finalizaci�n.
   */
   void closeStream()
   {
      this.bClose = true;
      this.semaforo.up();
   }



  //==========================================================================
  /**
   * Cierra el flujo de entrada y elimina los recursos asociados
   * Una vez cerrado el flujo no se podr� volver a utilizarlo.
   */
   public void close()
   {
     try
     {
      //Decremenar tama�o de la cola de recepci�n
      this.socket.getColaRecepcion().decrementarTama�o(this.tama�o);

      //Eliminar este ID_SocketInputStream de la Cola de Recepcion
      this.socket.getColaRecepcion().remove(this.id_socket);

      this.mutex.lock();
      this.lista.clear();

      this.lista = null;
      this.id_socket = null;

      this.mutex.unlock();
      //this.mutex = null;
      this.tama�o = 0;

      //Close Padre...
      super.close();

      this.bClose = true;
     }
     catch(IOException e)
     {
        ;
     }

   }


  //==========================================================================
  /**
   * Implementaci�n del m�todo de la interfaz Comparable.
   * @param  ID_SocketInputStream con la que se compara.
   * @return mayor que cero si este IDGL es mayor que el pasado en el
   * argumento, menor que cero si es menor y cero si son iguales.
   */
 public int compareTo(Object obj)
 {
    if (obj== null)
      return 0;

    ID_SocketInputStream id = (ID_SocketInputStream) obj;

    return this.id_socket.compareTo(id.getID_Socket());

 }
}





