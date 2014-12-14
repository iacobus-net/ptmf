/**
  Fichero: ProtocoloFTPMulticast.java  1.0 1/12/99
  Copyright (c) 2000-2014 . All Rights Reserved.
  Autor: Alejandro García Domínguez alejandro.garcia.dominguez@gmail.com   alejandro@iacobus.com
         Antonio Berrocal Piris antonioberrocalpiris@gmail.com
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package iacobus.mftp;

import iacobus.ptmf.*;
import java.io.*;
import javax.swing.JOptionPane;
import javax.swing.Icon;
import java.util.TreeMap;
import java.util.Iterator;

/**
 * Protocolo FTP Multicast versión 1.0
 */
public class ProtocoloFTPMulticast extends Thread
  implements PTMFDatosRecibidosListener, PTMFID_SocketInputStreamListener
    ,PTMFConexionListener, PTMFIDGLListener, PTMFID_SocketListener
{

   /** MAGIC */
  public final static int MAGIC = 0x6DED757B;

  /** VERSION 1*/
  public final static int VERSION = 0x01;

  /** Tamaño del array de Transmisión/Recepcion */
  public static final int TAMAÑO_ARRAY_BYTES = 1024 * 2;

  /** Fichero */
  File file = null;

  /** Flujo de salida del Fichero */
  private FileOutputStream fileOutputStream = null;

  /** TAmaño del Fichero */
  long lFileSize = 0;

  /** Nombre del Fichero*/
  String sFileName = null;

  /** Flag de lectura de datos del socket */
  private boolean bLeer = false;

  /** Semáforo binario de ESPERA*/
  private Semaforo semaforoFin = null;

  /** Semáforo binario para EMISION*/
  private Semaforo semaforoEmision = null;

  /** Semáforo binario para RECEPCION*/
  private Semaforo semaforoRecepcion = null;

    /** Flag de parada de la transferencia */
  private boolean bStop = false;

  /** Flujo de salida */
  private MulticastOutputStream out = null;

  /** Flujo de entrada Multicast*/
  private MulticastInputStream inMcast = null;

  /** Nueva Línea */
  private static final String newline = "\n";

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

  /** Icono del fichero enviado*/
  private Icon icon = null;

  /**
   * TreeMap de Threads ThreadRecepcion. KEY= ID_SocketInputStream. VALUE=FileRecepcion
   *  UTILIZADO EN MODO FIABLE.
   */
  private TreeMap treemapID_SocketInputStream = null;

  /**
   * TreeMap de ID_SOCKETS. KEY= ID_SOCKET. VALUE= Filerecepcion
   * UTILIZADO EN MODO NO_FIABLE.
   */
  private TreeMap treemapID_Socket = null;

  /** Socket Multicast Fiable*/
  private SocketPTMF socket = null;

  /** Modo de fiabilidad del socket*/
  private int modo = 0;

  /** Socket Multicast No Fiable*/
  private DatagramSocketPTMF datagramSocket = null;

  /** Flag de inicio del thread */
  private boolean runFlag = true;

  /** Ratio ed transferencia */
  private long lRatio = 0;


 //==========================================================================
 /**
  * Constructor
  */
  public ProtocoloFTPMulticast() throws IOException
  {
    super("ProtocoloFTPMulticast");

    setDaemon(true);

    try
    {
      //Crear semáforos
      semaforoFin = new Semaforo(true,1);
      semaforoEmision = new Semaforo(true,1);
      semaforoRecepcion = new Semaforo(true,1);
    }
    catch(ParametroInvalidoExcepcion e){;}

  }


 //==========================================================================
 /**
  * Método Run()
  */
 public void run()
 {
   Cipher cipher = null;
   boolean bCipher = false;
   mFtp ftp = mFtp.getFTP();
   try
   {
     runFlag = true;

       // Log.log("CRIPTOGRAFIA--> CLAVE:"+clave ,"");

     if(!(new String(clave).equals("")))
     {
        Log.log("CRIPTOGRAFIA--> CLAVE:"+clave ,"");
        cipher = Cipher.getInstance( clave);

        if(cipher == null)
        {
          mFtp.getFTP().error("No se ha podido crear los objetos de Cifrado.");
          return;
        }
        else
         bCipher = true;
     }

    //Establecer nivel de depuracion
    SocketPTMF.setNivelDepuracion(Log.ACK | Log.HACK | Log.NACK | Log.HNACK | Log.HSACK | Log.TPDU_RTX);


     //1.- Crear el socket..
     if (modo == PTMF.PTMF_FIABLE_RETRASADO || modo == PTMF.PTMF_FIABLE)
     {
        //Log.log("MODO PTMF PTMF FIABLE /RETRASADO","");
       //Iniciar Logo...
        mFtp.getFTP().logoOn();
       if(bCipher)
         socket = new SocketPTMF(dirIPMcast,dirIPInterfaz,(byte)TTLSesion,modo,this,cipher.getCipher(),cipher.getUncipher());
       else
         socket = new SocketPTMF(dirIPMcast,dirIPInterfaz,(byte)TTLSesion,modo,this);


       //Registrar listener eventos...
       //this.socket.addPTMFConexionListener(this);
       this.socket.addPTMFIDGLListener(this);
       this.socket.addPTMFID_SocketListener(this);

       //Obtener idgls e id_scoket
       ftp.idgls = this.socket.getNumeroIDGLs();
       ftp.id_sockets = this.socket.getNumeroID_Sockets();
       //ftp.getJLabelIDGLs().setText("IDGLS: "+ftp.idgls);
       //ftp.getJLabelID_Sockets().setText("ID_Sockets: "+ftp.id_sockets);

       //Obtener los IDGLs conocidos...
       TreeMap treemapIDGL = this.socket.getIDGLs();
       Iterator iterator = treemapIDGL.values().iterator();

       while(iterator.hasNext())
       {
          RegistroIDGL_TreeMap regIDGL = (RegistroIDGL_TreeMap) iterator.next();

          ftp.getJFrame().jTreeInformacion.addIDGL(regIDGL.getIDGL());
       }

       //Obtener los IDGLs conocidos...
       TreeMap treemapIDSocket = this.socket.getID_Sockets();
       Iterator iteratorSockets = treemapIDSocket.keySet().iterator();

       while(iteratorSockets.hasNext())
       {
          ID_Socket idSocket = (ID_Socket) iteratorSockets.next();

          ftp.getJFrame().jTreeInformacion.addID_Socket(idSocket);
       }


       //Obtener Flujos de Entrada y de Salida
       this.out = this.getSocket().getMulticastOutputStream();
       this.inMcast = this.getSocket().getMulticastInputStream();

       if(runFlag==false)
         return;

     }
     else
     {
       //Iniciar Logo...
        ftp.logoOn();

       if(bCipher)
         datagramSocket = new DatagramSocketPTMF(dirIPMcast,dirIPInterfaz,(byte)TTLSesion,modo,this,cipher.getCipher(),cipher.getUncipher());
       else
         datagramSocket = new DatagramSocketPTMF(dirIPMcast,dirIPInterfaz,(byte)TTLSesion,modo,this,null,null);

       if(runFlag==false)
          return;

       //Registrar listeners eventos...
       datagramSocket.addPTMFConexionListener(this);

     }

     if(runFlag==false)
        return;

     //Conectado¡¡¡
     ftp.insertInformacionString("Conexión Multicast establecida con "+dirIPMcast+ "TTL= "+TTLSesion);

     if(runFlag==false)
        return;


     // ENVIAR/RECIBIR FICHEROS...
     if( ftp.esEmisor())
     {

        this.socket.setRatioUsuario(lRatio);
        ftp.insertInformacionString("Ratio de transferencia: "+lRatio/1024+" KB/Seg");


        //Cerrar RECEPCION¡¡¡¡
        if(socket!=null)
          socket.desactivarRecepcion();

        //Cerrar RECEPCION¡¡¡¡
        if(datagramSocket!=null)
          datagramSocket.desactivarRecepcion();


        ftp.getJFrame().jPanelTransmisor.setEnabled(true);
        ftp.getJFrame().jPanelReceptor.setEnabled(false);

        this.waitSendFiles();
     }
     else
     {
        ftp.getJFrame().jPanelTransmisor.setEnabled(false);
        ftp.getJFrame().jPanelReceptor.setEnabled(true);

        this.waitReceiveFiles();
     }

     return;
  }
  catch(ParametroInvalidoExcepcion e)
  {
     ftp.error(e.getMessage());
  }
  catch(PTMFExcepcion e)
  {
     ftp.error(e.getMessage());
  }
  catch(IOException e)
  {
     ftp.error(e.getMessage());
  }


  //Limpiar....
  finally
  {
      //Cerrar el Socket
      close();
      Log.log("FIN ProtocoloFTPMulticast","");
  }
 }

 //==========================================================================
 /**
  * conectar
  */
  public void conectar(Address dirIPMcast,
      Address dirIPInterfaz,int TTLSesion,long lRatio, int modo, char[] clave)
  {
    this.dirIPMcast = dirIPMcast;
    this.dirIPInterfaz = dirIPInterfaz;
    this.modo = modo;
    this.TTLSesion = TTLSesion;
    this.clave = clave;
    this.lRatio = lRatio;

    // Iniciar el thread
    this.start();
 }

 //==========================================================================
 /**
  * getSocket()
  */
 SocketPTMF getSocket(){ return this.socket;}

 //==========================================================================
 /**
  * getDatagramSocket()
  */
 DatagramSocketPTMF getDatagramSocket(){ return this.datagramSocket;}

 //==========================================================================
 /**
  * getFTP()
  */
 mFtp getFTP(){ return mFtp.getFTP();}

  //==========================================================================
 /**
  * getModo()
  */
 int getModo(){ return this.modo;}

 //==========================================================================
 /**
  * getMulticastOutputStream()
  */
 //MulticastOutputStream getMulticastOutputStream(){ return this.out;}

 //==========================================================================
 /**
  * Cerrar el Socket
  */
 void close()
 {
   try
    {
      //Cerrar el Socket...
      if (modo  == PTMF.PTMF_FIABLE_RETRASADO || modo  == PTMF.PTMF_FIABLE)
        {
         if(socket!= null)
         {
            socket.closeEmision();
            socket.close(PTMF.CLOSE_ESTABLE);
         }
        }
      else
        if(datagramSocket!= null)
            datagramSocket.close();

         //if(semaforoFin == null)

    // ?¿?¿?¿?¿??¿?¿ ThreadRecepcion.interrupted();*******************-----

     //Despertar...
     if (semaforoFin != null)
       semaforoFin.up();

     if(semaforoEmision != null)
       semaforoEmision.up();

     if(semaforoRecepcion != null)
       semaforoEmision.up();


    }
    catch(PTMFExcepcion e)
    {
            finTransferencia(e);
    }
 }

 //==========================================================================
 /**
  * FinTransferencia
  */
  void finTransferencia(IOException ioe)
  {
      mFtp ftp = mFtp.getFTP();

      ftp.insertStringJTextPane(ftp.getJTextPaneInformacion(),ioe.getMessage(),"error");
      ftp.insertInformacionString("Conexión Cerrada");
      mFtp.getFTP().logoOff();
      this.runFlag = false;
  }

 //==========================================================================
 /**
  * Método stopThread()
  */
 public void stopThread()
 {
   this.runFlag = false;

   if(semaforoRecepcion!= null)
     semaforoRecepcion.up();

     if (semaforoFin != null)
       semaforoFin.up();

     if(semaforoEmision != null)
       semaforoEmision.up();

     if(semaforoRecepcion != null)
       semaforoEmision.up();


  //if( this.protocoloFTPMulticast!= null)
  // this.protocoloFTPMulticast.close();
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
  * Implementación de la interfaz PTMFConexionListener
  */
 public void actionPTMFConexion(PTMFEventConexion evento)
 {
    mFtp ftp = mFtp.getFTP();
    //Log.log("actionPTMFConexion","");
    //Log.log("actionPTMFConexion: "+evento.getString(),"");

    if( ftp != null && runFlag==true)
    {
       ftp.insertInformacionString(evento.getString());
       //ftp.insertStringJTextPane(" ","icono_informacion");
       //ftp.insertInformacionString(evento.getString());
    }
 }

 //==========================================================================

 //==========================================================================
 /**
  * Espera para la emisión de ficheros...
  */
 void waitSendFiles() throws IOException
 {
         //BUCLE PRINCIPAL
     while(this.esActiva())
     {
          //Si NO HAY NADA QUE EMITIR--> DORMIR HASTA QUE LO HAYA.
          if(file== null || this.bStop == true)
           this.semaforoEmision.down();

          //Verificar si se ha cerrado la conexión...
          if(!this.esActiva())
          {
            limpiar();
            return;
          }

          //Enviar fichero....
          FileEmision fileEmision = new FileEmision(this,this.file,this.icon);
          fileEmision.sendFile();

          if(!this.esActiva())
          {
            limpiar();
            return;
          }

          this.file = null;
   }
 }

 //==========================================================================
 /**
  * Espera para la recepción de ficheros...
  */
 void waitReceiveFiles() throws IOException
 {
    if(this.getModo() == PTMF.PTMF_FIABLE_RETRASADO || this.getModo()  == PTMF.PTMF_FIABLE)
    {
      //Registrar ID_SocketInputStreamListener
      this.getSocket().getMulticastInputStream().addPTMFID_SocketInputStreamListener(this);

    }
    else
    {
      //Registrar PTMFDatosRecibidos
      this.getDatagramSocket().addPTMFDatosRecibidosListener(this);

      //Crear el Treemap si es NULL
      if(this.treemapID_Socket == null)
       this.treemapID_Socket = new TreeMap();
    }

    //Información..
    mFtp.getFTP().insertInformacionString("Esperando recepción de ficheros...");
    mFtp.getFTP().insertRecepcionString("Esperando recepción de ficheros...","icono_informacion");

    //***** BUCLE PRINCIPAL *****
    while(this.esActiva())
    {

      if(this.getModo() != PTMF.PTMF_FIABLE_RETRASADO && this.getModo()  != PTMF.PTMF_FIABLE)
      {
        //MODO NO-FIABLE
        //ESPERAR A QUE HAYA DATOS..
        this.semaforoRecepcion.down();

        //Leer Bytes NO FIABLE...
        recibirDatagrama();
      }
      else
      { //MODO FIABLE
        //ESPERAR A QUE FINALICE EL THREAD SESION MULTICAST,
        // LA RECEPCION SE HACE DE FORMA ASÍNCRONA CON EL LISTENER ID_SOCKETINPUTSTREAM...
        while(this.esActiva())
          Temporizador.sleep(500);
      }
    }//FIN WHILE PRINCIPAL
 }


 //==========================================================================
 /**
  * recibirDatagrama();
  */
 private void recibirDatagrama() throws IOException
 {
   byte[] bytes = new byte[this.TAMAÑO_ARRAY_BYTES];
   String sFileName = null;
   long lFileSize = 0;

   //1.- **Leer DATOS**
   RegistroID_Socket_Buffer reg = this.getDatagramSocket().receive();

   //Crear el Treemap si es NULL
   if(this.treemapID_Socket == null)
    this.treemapID_Socket = new TreeMap();


   // SI EL ID_Socket no está en el treemap, Significa CONEXIÓN NUEVA....
   if(!this.treemapID_Socket.containsKey(reg.getID_Socket()))
   {
       Log.log("NUEVO ID_Socket: "+reg.getID_Socket(),"");
       Buffer buf = reg.getBuffer();

       //Comprobar IDFTP
       if (!FileRecepcion.parseIDFTPMulticast(buf))
        return;

       //Comprobar Tamaño
       lFileSize = FileRecepcion.parseFileSize(buf);
       if(lFileSize <= 0)
        return;

       //Comprobar FileName
       sFileName = FileRecepcion.parseFileName(buf);
       if( sFileName == null)
        return;

       // protocoloFTPMulticast.getFTP().insertStringJTextPane(" ","icono_entrada");
       mFtp.getFTP().insertRecepcionString("Iniciando la recepción... de "+sFileName,null);

       //this.getFTP().insertStringJTextPane(" ","icono_entrada");
       mFtp.getFTP().insertRecepcionString("Recibiendo fichero: "+sFileName+" del emisor: "+reg.getID_Socket(),null);
       //this.getFTP().insertStringJTextPane(" ","icono_entrada");
       mFtp.getFTP().insertRecepcionString("Tamaño: "+lFileSize,null);


       //Nuevo FileRecepcion...
       FileRecepcion fileRecepcion  = new FileRecepcion(this,reg.getID_Socket());
       //Recibir fichero
       fileRecepcion.receiveFile(lFileSize,sFileName);

       //Añadir nuevo FileRecepcion al treemap...
       this.treemapID_Socket.put(reg.getID_Socket(),fileRecepcion);

   }
   else
   {
      //Obtener FileRecepcion...
      FileRecepcion fileRecepcion = (FileRecepcion) this.treemapID_Socket.get(reg.getID_Socket());

      //Añadir los bytes leídos...
      fileRecepcion.addBytes(reg.getBuffer().getBuffer(),reg.getBuffer().getLength());

      //FALTA FALTA FALTA FALTA FALTA FALTA FALTA FALTA FALTA FALTA
      //FALTA FALTA FALTA FALTA FALTA FALTA FALTA FALTA FALTA FALTA
      //FALTA FALTA FALTA FALTA FALTA FALTA FALTA FALTA

      //ALES FALYA CONTENPLAR FIN DE FLUJO ¿?¿?¿¿?¿????¿?¿? *****************---------

      //  FALTA FALTA FALTA FALTA FALTA FALTA FALTA FALTA FALTA
      //FALTA FALTA FALTA FALTA FALTA FALTA FALTA FALTA FALTA FALTA

   }

 }


 //==========================================================================
 /**
  * Eliminar un FileRecepcion. MODO NO FIABLE.
  * @param ID_Socket
  */
 void removeFileRecepcion(ID_Socket id_socket)
 {
    this.treemapID_Socket.remove(id_socket);
 }

 //==========================================================================
 /**
  * Eliminar un FileRecepcion. MODO FIABLE.
  */
  void removeFileRecepcion(ID_SocketInputStream idIn)
  {
    //Log.log("Remove fileRecepcion: "+idIn,"");

    if (idIn == null)
      Log.log("idIN ES NULLLLL¡¡¡¡","");
    if (this.treemapID_SocketInputStream!= null)
      this.treemapID_SocketInputStream.remove(idIn);
  }


 //==========================================================================
 /**
  * Mensaje de advertencia--> El Fichero Existe. Petición de sobreescribir.
  * @return true si se quiere sobreescribir, false en caso contrario.
  */
 private boolean mensajeFileExists()
 {
  boolean b = false;
  try
  {
    int iOpcion =  JOptionPane.showConfirmDialog(null,"El fichero "+sFileName+newline+"ya existe. ¿Desea sobreescribir el fichero existente?",
				    "Sobreescribir", JOptionPane.YES_NO_OPTION);
    if(iOpcion == JOptionPane.YES_OPTION)
      b = true;


  }
  finally
  {
   return b;
  }
 }

 //==========================================================================
 /**
  * Mensaje de advertencia--> No se puede escribir en el fichero.
  */
 private void mensajeErrorEscritura()
 {
    JOptionPane.showMessageDialog(null,"No se puede escribir en el fichero: "+sFileName+newline+"no se tiene permiso de escritura"+newline+"Verifique los permisos de escritura"+newline+" y que tiene suficiente privilegio para escribir." ,
				    "Error Escritura",JOptionPane.ERROR_MESSAGE);
 }

 //==========================================================================
 /**
  * Mensaje de advertencia--> Error Escribiendo
  */
 private void mensajeErrorEscribiendo(String sError)
 {
    JOptionPane.showMessageDialog(null,"Se ha producido un error mientras se intentaba escribir en el fichero"+newline+sFileName+newline+"El error es el siguiente:"+sError ,
				    "Error Escritura",JOptionPane.ERROR_MESSAGE);
 }





//==========================================================================
 /**
  * sendFile envía el fichero sFile por el canal Multicast.
  * @param file el fichero que se desea transmitir por Multicast
  * @param icon Icono representativo del fichero
  * @return Boolean. true si se ha iniciado la transferencia, false en caso contrario.
  */
 public boolean sendFile(File file,Icon icon)
 {
    if (!esActiva())
      return false;

    if(this.file!=null)
    {
      errorFile("Ya hay una transferencia en curso.\nPor favor, espere a que termine para poder iniciar otra.");
      return false;
    }

    //Asignar..
    this.file = file;
    this.icon = icon;

    //Enviar...
    this.bStop = false;
    this.semaforoEmision.up();
    return true;
 }




 //==========================================================================
 /**
  * Implementación de la interfaz PTMFIDGLListener
  * para la recepción de datos en modo NO_FIABLE
  */
 public void actionPTMFIDGL(PTMFEventIDGL evento)
 {
    mFtp ftp = mFtp.getFTP();

    if( evento.esAñadido())
    {
       ftp.idgls = this.socket.getNumeroIDGLs();

       //Añadir el IDGL al árbol de información
       ftp.getJFrame().jTreeInformacion.addIDGL(evento.getIDGL());

       mFtp.getFTP().insertInformacionString("IDGLS: "+mFtp.getFTP().idgls);
       mFtp.getFTP().insertInformacionString("Nuevo IDGL: "+evento.getIDGL());

    }
    else
    {
      ftp.idgls = this.socket.getNumeroIDGLs();

      //Eliminar IDGLs del árbol
      ftp.getJFrame().jTreeInformacion.removeIDGL(evento.getIDGL());

       mFtp.getFTP().insertInformacionString("IDGLS: "+mFtp.getFTP().idgls);
       mFtp.getFTP().insertInformacionString("IDGL eliminado: "+evento.getIDGL());
    }
 }

  //==========================================================================
 /**
  * Implementación de la interfaz PTMFIDGLListener
  * para la recepción de datos en modo NO_FIABLE
  */
 public void actionPTMFID_Socket(PTMFEventID_Socket evento)
 {
    mFtp ftp = mFtp.getFTP();

    if( evento.esAñadido())
    {
      ftp.id_sockets = this.socket.getNumeroID_Sockets();

      //Añadir el ID_Socket al árbol de información
      ftp.getJFrame().jTreeInformacion.addID_Socket(evento.getID_Socket());

       mFtp.getFTP().insertInformacionString("ID_Sockets: "+mFtp.getFTP().id_sockets);
       mFtp.getFTP().insertInformacionString("Nuevo ID_Socket: "+evento.getID_Socket());
    }
    else
    {
      ftp.id_sockets = this.socket.getNumeroID_Sockets();

      //Añadir el ID_Socket al árbol de información
      ftp.getJFrame().jTreeInformacion.removeIDSocket(evento.getID_Socket());

      ftp.insertInformacionString("ID_Sockets: "+mFtp.getFTP().id_sockets);
      ftp.insertInformacionString("ID_Socket eliminado: "+evento.getID_Socket());
    }

 }

 //==========================================================================
 /**
  * Implementación de la interfaz PTMFDatosRecibidosListener
  * para la recepción de datos en modo NO_FIABLE
  */
 public void actionPTMFDatosRecibidos(PTMFEventDatosRecibidos evento)
 {
    // Hay datos, despertar si estaba dormido
    this.bLeer = true;
    this.semaforoRecepcion.up();
 }



 //==========================================================================
 /**
  * PTMFEventID_SocketInputStream
  */
 public void actionPTMFID_SocketInputStream(PTMFEventID_SocketInputStream evento)
 {
   //Log.log("\n\nNUEVO ID_SOCKETINPUTSTREAM","");

   //Crear TreeMap threads de recepcion ...
   if ( treemapID_SocketInputStream == null)
      this.treemapID_SocketInputStream = new TreeMap();

   ID_SocketInputStream idIn = evento.getID_SocketInputStream();
   if (idIn == null)
   {
    //Log.log("\n\nNUEVO ID_SOCKETINPUTSTREAM: NULL","");
    return;
   }

   //Log.log("\n\n ID_SOCKETINPUTSTREAM: --->OK","");

   if( !this.treemapID_SocketInputStream.containsKey(idIn))
   {
        // Log.log("\n\n PARA CREAR THREADS","");

       try
       {
          //PONER --> Obtener lFilesize y sFileName antes de crear FileRecepcion
          FileRecepcion fileRecepcion = new FileRecepcion(this,idIn);
          //Log.log("\n\nCREANDO THREAD Filerecepcion","");

          this.treemapID_SocketInputStream.put(idIn,fileRecepcion);

          //Iniciar thread...
          fileRecepcion.start();
       }
       catch(IOException ioe)
       {
          mFtp.getFTP().error(ioe.toString());
       }
   }
 }
 //==========================================================================
 /**
  * Error Abriendo el Fichero.
  * @param sCadenaInformativa
  */
 private void errorFile(String sCadenaInformativa)
 {
   JOptionPane.showMessageDialog(null,sCadenaInformativa,
				    "Error", JOptionPane.ERROR_MESSAGE);

 }





 //==========================================================================
  /**
   *  Limpiar variables....
   */
  private void limpiar()
  {
     this.file = null;
     this.bStop = true;
  }




}