/**
  
  Copyright (c) 2000-2014 . All Rights Reserved.
  @Autor: Alejandro García Domínguez alejandro.garcia.dominguez@gmail.com   alejandro@iacobus.com
         Antonio Berrocal Piris antonioberrocalpiris@gmail.com
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations 
  */

package iacobus.mchat;

import iacobus.ptmf.*;

//IO
import java.io.Writer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.BufferedWriter;
import java.io.InputStreamReader;


 //==========================================================================
 /**
  * Clase ThreadSesionMulticast. Gestiona la conexión Multicast.
  */
 public class ThreadSesionMulticast extends Thread
 implements PTMFConexionListener, PTMFDatosRecibidosListener, PTMFIDGLListener, PTMFID_SocketListener
 {

  /** Socket Multicast Fiable*/
  private SocketPTMF socket = null;

  /** Modo de fiabilidad del socket*/
  private int modo = 0;

  /** Socket Multicast No Fiable*/
  private DatagramSocketPTMF datagramSocket = null;

  /** mChat */
  private mChat chat = null;

  /** Flag de lectura de datos del socket */
  private boolean bLeer = false;

  /** Semáforo binario */
  private Semaforo semaforo = null;

  /** Flag de inicio del thread */
  private boolean runFlag = false;

  /** Flujo de salida */
  private Writer out = null;

  /** Flujo de entrada Multicast*/
  private MulticastInputStream inMcast = null;

  /** Flujo de entrada*/
  //private BufferedReader in = null;

  /** Flujo de entrada ID_SocketInputStream */
  private ID_SocketInputStream id_socketIn = null;

  /** Nueva Línea */
  private String newline = "\n";

  /** Address dirIPMcast */
  private Address dirIPMcast = null;

  /** Address dirIPInterfaz */
  private Address dirIPInterfaz = null;

  /** clave */
  private char[] clave = null;

  /**TTL sesion */
  private int TTLSesion = 0;

  /** Registro_ID_Socket */
  private RegistroID_Socket_Buffer reg = null;

  /** Flag de Sesion Aciva */
  private boolean bActiva = false;

  /** Numero de IDGLs */
  private int idgls = 0;

  /** Numero de ID_Sockets */
  private int id_sockets = 0;

 //==========================================================================
 /**
  * Constructor
  */
  public ThreadSesionMulticast(mChat chat)
  {
    super();

    this.chat = chat;
    setDaemon(true);
  }

 //==========================================================================
 /**
  * Conectar. Inicia el Thread y establece una sesión Multicast
  * con los parámetros establecidos....
  */
  public void conectar(Address dirIPMcast,
      Address dirIPInterfaz,int TTLSesion,int modo, char[] clave)
  {
    this.dirIPMcast = dirIPMcast;
    this.dirIPInterfaz = dirIPInterfaz;
    this.modo = modo;
    this.TTLSesion = TTLSesion;
    this.clave = clave;
    start();
  }


 //==========================================================================
 /**
  * Método Run()
  */
 public void run()
 {
   Cipher cipher = null;
   boolean bCipher = false;
   try
   {
     runFlag = true;


     if(!(new String(clave).equals("")))
     {
        Log.log("CRIPTOGRAFIA--> CLAVE:"+clave ,"");
        cipher = Cipher.getInstance(this.chat,clave);

        if(cipher == null)
        {
          this.chat.error("No se ha podido crear los objetos de Cifrado.");
          return;
        }
        else
         bCipher = true;
     }

     //Crear el socket..
     if (modo == PTMF.PTMF_FIABLE_RETRASADO || modo == PTMF.PTMF_FIABLE)
     {
       //Iniciar Logo...
       chat.logoOn();

       Log.log("Criptografia --> "+bCipher,"");
       if(cipher != null && cipher.getCipher()!=null)
         Log.log("Codificador: "+cipher.getCipher(),"");
       if(cipher != null && cipher.getUncipher()!=null)
         Log.log("Descodificador: "+cipher.getUncipher(),"");

       if(bCipher)
         socket = new SocketPTMF(dirIPMcast,dirIPInterfaz,(byte)TTLSesion,modo,this,cipher.getCipher(),cipher.getUncipher());
       else
         socket = new SocketPTMF(dirIPMcast,dirIPInterfaz,(byte)TTLSesion,modo,this);


       //Registrar listener eventos...
       //this.socket.addPTMFConexionListener(this);
       this.socket.addPTMFIDGLListener(this);
       this.socket.addPTMFID_SocketListener(this);

       //Obtener idgls e id_scoket
       this.idgls = this.socket.getNumeroIDGLs();
       this.id_sockets = this.socket.getNumeroID_Sockets();
       this.chat.getJLabelIDGLs().setText("IDGLS: "+this.idgls);
       this.chat.getJLabelID_Sockets().setText("ID_Sockets: "+this.id_sockets);

       //Registrar PTMFDatosRecibidos
       socket.getMulticastInputStream().addPTMFDatosRecibidosListener(this);
     }
     else
     {
       //Iniciar Logo...
       chat.logoOn();

       if(bCipher)
         datagramSocket = new DatagramSocketPTMF(dirIPMcast,dirIPInterfaz,(byte)TTLSesion,modo,this,cipher.getCipher(),cipher.getUncipher());
       else
          datagramSocket = new DatagramSocketPTMF(dirIPMcast,dirIPInterfaz,(byte)TTLSesion,modo,this,null,null);

       //Registrar PTMFConexion
       //datagramSocket.addPTMFConexionListener(this);

       //Registrar PTMFDatosRecibidos
       datagramSocket.addPTMFDatosRecibidosListener(this);
     }

     bActiva = true;

     //Conectado¡¡¡
     chat.getJLabelInformacion().setText("Conexión Multicast establecida con "+dirIPMcast+ "TTL: "+TTLSesion);

     //Crear semáforo
     semaforo = new Semaforo(true,1);


     //Obtener flujos de entrada/salida en los modos FIABLE
     if (modo == PTMF.PTMF_FIABLE || modo == PTMF.PTMF_FIABLE_RETRASADO)
     {
       out = new BufferedWriter (new OutputStreamWriter((OutputStream)socket.getMulticastOutputStream()),255);
       inMcast = socket.getMulticastInputStream();
     }


     //Bucle principal....
     while(runFlag)
     {
      //Dormir si no hay que leer
      if(!bLeer)
         semaforo.down();
      else
      {

       bLeer = false;
       Log.log("DEPU 1","");

       if (modo == PTMF.PTMF_FIABLE_RETRASADO || modo == PTMF.PTMF_FIABLE)
       {
           //Mientras haya datos que leer
           while(this.inMcast.available()>0)
           {

             //Obtener el Flujo de entrada....
             id_socketIn = inMcast.nextID_SocketInputStream();

             if(id_socketIn != null && (id_socketIn.available() > 0))
             {
                chat.insertStringJTextPaneSuperior(" ","icono_entrada");
                chat.insertStringJTextPaneSuperior(id_socketIn.getID_Socket().toString(),"entrada");
                byte[] bytes = new byte[id_socketIn.available()];
                id_socketIn.read(bytes);
                chat.insertStringJTextPaneSuperior(new String(bytes),"entrada");
             }
          }
       }
       else
       {
           //Mientras haya datos que leer
           while(datagramSocket.available()>0)
           {
             reg = datagramSocket.receive();
              if(reg.esFinTransmision())
                break;
             chat.insertStringJTextPaneSuperior(" ","icono_entrada");
             chat.insertStringJTextPaneSuperior(reg.getID_Socket().toString()+": "+new String(reg.getBuffer().getBuffer()),"entrada");
           }
       }
      }
     }

  }
  catch(ParametroInvalidoExcepcion e)
  {
    finalizar();
    chat.error(e.getMessage());
  }
  catch(PTMFExcepcion e)
  {
    finalizar();
    chat.error(e.getMessage());
  }
  catch(IOException e)
  {
    finalizar();
    chat.error(e.getMessage());
  }

  finally
  {
     //Cerrar el Socket
     close();

     //Limpiar ...
     finalizar();
  }
 }

 //==========================================================================
 /**
  * Cerrar el Socket
  */
 void close()
 {
   try
    {
      //Cerrar el Socket...
      if (modo == PTMF.PTMF_FIABLE_RETRASADO || modo == PTMF.PTMF_FIABLE)
      {
        if(socket!= null)
          socket.close(true);
      }
      else
      {
        if(datagramSocket!= null)
          datagramSocket.close();
      }

      socket =  null;
    }
    catch(PTMFExcepcion e)
    {
        finalizar();
    }
 }

 //==========================================================================
 /**
  * Finalizar
  */
 public void finalizar()
 {
     runFlag = false;
      bActiva = false;
     //Parar Logo...
     chat.logoOff();
 }

 //==========================================================================
 /**
  * Método stopThread()
  */
 public void stopThread()
 {
   this.runFlag = false;
   if(semaforo != null)
   {
     //Despertar...
     semaforo.up();
   }
 }

 //==========================================================================
 /**
  * Indica si la sesión está activa o desactivada.
  * @return true si la sesión está activa, false en caso contrario
  */
 public boolean esActiva()
 {
   return this.runFlag;
 }

 //==========================================================================
 /**
  * sendStream envía la cadena pasada como argumento por el canal Multicast.
  * @exception IOException se lanza si ocurre un error
  */
 public void sendString(String cadena) throws IOException
 {
     if (cadena == null) return;

    //Log.log("SendSetring. tamaño"+cadena.length(),"");
     if ((this.modo == PTMF.PTMF_FIABLE)|| (this.modo ==PTMF.PTMF_FIABLE_RETRASADO))
    {
      //Obtener el flujo de salida multicast...
      MulticastOutputStream out = this.socket.getMulticastOutputStream();

      //Enviar los datos....
      out.write(cadena.getBytes());

    }
    else
    {
      this.datagramSocket.send(new Buffer(cadena.getBytes()));
    }
 }

 //==========================================================================
 /**
  * Implementación de la interfaz PTMFDatosRecibidosListener
  */
 public void actionPTMFDatosRecibidos(PTMFEventDatosRecibidos evento)
 {
    // Hay datos, despertar al thread si estaba dormido
    this.bLeer = true;
    this.semaforo.up();
    Log.log("actionListener --> Datos Recibidos","");
 }

 //==========================================================================
 /**
  * Implementación de la interfaz PTMFConexionListener
  */
 public void actionPTMFConexion(PTMFEventConexion evento)
 {
    if(chat != null)
    {
      chat.insertStringJTextPaneSuperior(" ","icono_informacion");
      chat.insertStringJTextPaneSuperior("CONEXIÓN: ","ID_SOCKET");
      chat.insertStringJTextPaneSuperior(evento.getString()+newline,"informacion"+newline);
    }
 }


 //==========================================================================
 /**
  * Implementación de la interfaz PTMFIDGLListener
  * para la recepción de datos en modo NO_FIABLE
  */
 public void actionPTMFIDGL(PTMFEventIDGL evento)
 {
    if( evento.esAñadido())
    {
     this.idgls = this.socket.getNumeroIDGLs();
     this.chat.getJLabelIDGLs().setText("IDGLS: "+this.idgls);
     this.chat.insertStringJTextPaneSuperior(" ","icono_informacion");
     this.chat.insertStringJTextPaneSuperior("Notificación nuevo IDGL: "+evento.getIDGL()+newline,"informacion");

    }
    else
    {
     this.idgls = this.socket.getNumeroIDGLs();
     this.chat.getJLabelIDGLs().setText("IDGLS: "+this.idgls);
     this.chat.insertStringJTextPaneSuperior(" ","icono_informacion");
     this.chat.insertStringJTextPaneSuperior("IDGL eliminado: "+evento.getIDGL()+newline,"informacion");
    }
 }

  //==========================================================================
 /**
  * Implementación de la interfaz PTMFIDGLListener
  * para la recepción de datos en modo NO_FIABLE
  */
 public void actionPTMFID_Socket(PTMFEventID_Socket evento)
 {
    if( evento.esAñadido())
    {
     this.id_sockets = this.socket.getNumeroID_Sockets();
     this.chat.getJLabelID_Sockets().setText("ID_Sockets: "+this.id_sockets);
     this.chat.insertStringJTextPaneSuperior(" ","icono_informacion");
     this.chat.insertStringJTextPaneSuperior("Notificación nuevo ID_Socket: "+evento.getID_Socket()+newline,"informacion");
    }
    else
    {
     this.id_sockets = this.socket.getNumeroID_Sockets();
     this.chat.getJLabelID_Sockets().setText("ID_Sockets: "+this.id_sockets);
     this.chat.insertStringJTextPaneSuperior(" ","icono_informacion");
     this.chat.insertStringJTextPaneSuperior("ID_Socket eliminado: "+evento.getID_Socket()+newline,"informacion");
   }

 }

 }