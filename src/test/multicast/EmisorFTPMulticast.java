//============================================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: ProtocoloFTPMulticast.java  1.0 1/12/99
//
//	Descripción: Clase ProtocoloFTPMulticast.
//
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
//----------------------------------------------------------------------------

package test.multicast;


import iacobus.ptmf.Address;
import iacobus.ptmf.Buffer;
import iacobus.ptmf.DatagramSocketPTMF;
import iacobus.ptmf.ID_Socket;
import iacobus.ptmf.ID_SocketInputStream;
import iacobus.ptmf.Log;
import iacobus.ptmf.MulticastInputStream;
import iacobus.ptmf.MulticastOutputStream;
import iacobus.ptmf.PTMF;
import iacobus.ptmf.PTMFConexionListener;
import iacobus.ptmf.PTMFDatosRecibidosListener;
import iacobus.ptmf.PTMFEventConexion;
import iacobus.ptmf.PTMFEventDatosRecibidos;
import iacobus.ptmf.PTMFEventIDGL;
import iacobus.ptmf.PTMFEventID_Socket;
import iacobus.ptmf.PTMFEventID_SocketInputStream;
import iacobus.ptmf.PTMFExcepcion;
import iacobus.ptmf.PTMFIDGLListener;
import iacobus.ptmf.PTMFID_SocketInputStreamListener;
import iacobus.ptmf.PTMFID_SocketListener;
import iacobus.ptmf.ParametroInvalidoExcepcion;
import iacobus.ptmf.RegistroID_Socket_Buffer;
import iacobus.ptmf.SocketPTMF;

import java.io.*;

import javax.swing.JOptionPane;
import javax.swing.Icon;

import java.util.TreeMap;
import java.util.Iterator;



/**
 * mFtp operativo versión 1.0 en modo texto para pruebas de consola
 * y depuración de PTMF.
 * <p>Title: PTMF v1.1</p>
 * <p>Description: Protocolo de Transporte Multicast Fiable</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.1
 */
public class EmisorFTPMulticast extends Thread
  implements PTMFDatosRecibidosListener, PTMFID_SocketInputStreamListener
    ,PTMFConexionListener, PTMFIDGLListener, PTMFID_SocketListener
{

  private boolean bEmisor = true;

  /** Nivel de Depuracion */
  private int NIVEL_DEPURACION = /* Log.SOCKET |  Log.CGL | Log.DATOS_THREAD |*/ Log.CANAL_MULTICAST  /*| Log.ACK | Log.NACK | Log.HNACK | Log.HSACK | Log.TPDU_RTX | Log.TEMPORIZADOR*/;

  private String mn = "EmisorFTPMulticast";

   /** MAGIC */
  public final static int MAGIC = 0x6DED757B;

  /** VERSION 1*/
  public final static int VERSION = 0x01;

  /** Tamaño del array de Transmisión/Recepcion */
  public static final int TAMAÑO_ARRAY_BYTES = 1024 * 2;

  /** Fichero */
  File file = null;

  /** Modo PTMF , Modo de fiabilidad del socket */
  private int modo = PTMF.PTMF_FIABLE;

  /** Flujo de salida del Fichero */
  private FileOutputStream fileOutputStream = null;

   /** Flujo de entrada del Fichero */
  private FileInputStream fileInputStream = null;

  /** TAmaño del Fichero */
  long lFileSize = 0;

  /** Nombre del Fichero*/
  String sFileName = null;

  /** Flag de lectura de datos del socket */
  private boolean bLeer = false;

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
  private int TTLSesion = 2;

  /** Registro_ID_Socket */
  private RegistroID_Socket_Buffer reg = null;


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
  public EmisorFTPMulticast(String dirIPMulticast, String puerto, String sFichero) throws IOException
  {
    super("EmisorFTPMulticast");

    System.out.println("");
    System.out.println("");
    System.out.println("-------------------------------------------");
    System.out.println("EmisorFTPMulticast v1.0");
    System.out.println("(C)2003 M.Alejandro Garcia");
    System.out.println("-------------------------------------------");
    System.out.println("");


    //Obtener parámetros
    this.dirIPMcast = new Address(dirIPMulticast,Integer.parseInt(puerto));
    this.sFileName = sFichero;
    this.file = new File(sFichero);

    if(file == null)
    {
        throw new IOException("El nombre del fichero es NULL.\n");
    }
    if(!this.file.exists())
    {
        throw new IOException("El sistema no reconoce "+this.file.getName()+" como un fichero válido");
    }
    if(!this.file.canRead())
    {
        throw new IOException("El fichero "+this.file.getName()+" no puede ser leído.Verifique que tiene permiso de lectura.");
    }

    //Crear el Flujo de lectura del Fichero....
    this.fileInputStream = new FileInputStream(this.file);

    //Log..
    Log.log(mn,"Fichero a transmitir: "+this.file.getName()+"/"+this.file.length()+" bytes");

    //setDaemon(true); En Recepción solamente

  }


 //==========================================================================
 /**
  * Método Run()
  */
 public void run()
 {

   this.setPriority(Thread.NORM_PRIORITY+5);

   try
   {
     runFlag = true;

     // Log.log("CRIPTOGRAFIA--> CLAVE:"+clave ,"");

    //Establecer nivel de depuracion
    SocketPTMF.setNivelDepuracion(NIVEL_DEPURACION);

     //1.- Crear el socket..
     if (modo == PTMF.PTMF_FIABLE_RETRASADO || modo == PTMF.PTMF_FIABLE)
     {
        //Log.log("MODO PTMF PTMF FIABLE /RETRASADO","");
        socket = new SocketPTMF(dirIPMcast,null,(byte)TTLSesion,modo,this);



       //Registrar listener eventos...
       //this.socket.addPTMFConexionListener(this);
       this.socket.addPTMFIDGLListener(this);
       this.socket.addPTMFID_SocketListener(this);

       //Obtener idgls e id_scoket
       int idgls = this.socket.getNumeroIDGLs();
       int id_sockets = this.socket.getNumeroID_Sockets();
       Log.log(mn,"IDGLS: "+idgls);
       Log.log(mn,"ID_Sockets: "+id_sockets);

       //Obtener Flujos de Entrada y de Salida
       this.out = this.getSocket().getMulticastOutputStream();
       this.inMcast = this.getSocket().getMulticastInputStream();

       if(runFlag==false)
         return;

     }
     else
     {

       datagramSocket = new DatagramSocketPTMF(dirIPMcast,dirIPInterfaz,(byte)TTLSesion,modo,this,null,null);

       if(runFlag==false)
          return;

       //Registrar listeners eventos...
       datagramSocket.addPTMFConexionListener(this);

     }

     if(runFlag==false)
        return;

     //Conectado¡¡¡
     Log.log(mn,"Conexión Multicast establecida con "+dirIPMcast+ "TTL= "+TTLSesion);

     if(runFlag==false)
        return;


     // ENVIAR/RECIBIR FICHEROS...
     if(bEmisor)
     {
        this.socket.setRatioUsuario(lRatio);
        Log.log(mn,"Ratio de transferencia: "+lRatio/1024+" KB/Seg");

        //Cerrar RECEPCION¡¡¡¡
        if(socket!=null)
          socket.desactivarRecepcion();

        //Cerrar RECEPCION¡¡¡¡
        if(datagramSocket!=null)
          datagramSocket.desactivarRecepcion();

        //Enviar fichero....
        sendFile();
     }
     else
     {
        this.waitReceiveFiles();
     }

     return;
  }
  catch(ParametroInvalidoExcepcion e)
  {
     error(e.getMessage());
  }
  catch(PTMFExcepcion e)
  {
     error(e.getMessage());
  }
  catch(IOException e)
  {
     error(e.getMessage());
  }

  //Limpiar....
  finally
  {
      //Cerrar el Socket
      close();
      //Log.log(mn,"Fin EmisionFTPMulticast");
  }
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
      //Log.log("FtpMulticast.finTransferencia",ioe.getMessage());
      //Log.log("Conexión Cerrada","");

      this.runFlag = false;
  }




 //==========================================================================
 /**
  * Implementación de la interfaz PTMFConexionListener
  */
 public void actionPTMFConexion(PTMFEventConexion evento)
 {
        Log.log("EventoPTMF",evento.getString());
 }




 //==========================================================================
 /**
  * Espera para la recepción de ficheros...
  */
 void waitReceiveFiles() throws IOException
 {
/*    if(this.getModo() == PTMF.PTMF_FIABLE_RETRASADO || this.getModo()  == PTMF.PTMF_FIABLE)
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
    Log.log("Esperando recepción de ficheros...","");


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
 */
 }


 //==========================================================================
 /**
  * recibirDatagrama();
  */
 private void recibirDatagrama() throws IOException
 {
  /* byte[] bytes = new byte[this.TAMAÑO_ARRAY_BYTES];
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

       Log.log("Iniciando la recepción... de "+sFileName,"");
       Log.log("Recibiendo fichero: "+sFileName+" del emisor: "+reg.getID_Socket(),"");
       Log.log("Tamaño: "+lFileSize,"");


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
*/
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
  * Implementación de la interfaz PTMFIDGLListener
  * para la recepción de datos en modo NO_FIABLE
  */
 public void actionPTMFIDGL(PTMFEventIDGL evento)
 {

    if( evento.esAñadido())
    {
       //Log.log("IDGLS: "+mFtp.getFTP().idgls,"");
       Log.log(mn,"Nuevo IDGL: "+evento.getIDGL());

    }
    else
    {
       //Log.log("IDGLS: "+mFtp.getFTP().idgls,"");
       Log.log(mn,"IDGL eliminado: "+evento.getIDGL());
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
       //Log.log("ID_Sockets: "+mFtp.getFTP().id_sockets,"");
       Log.log(mn,"Nuevo ID_Socket: "+evento.getID_Socket());
    }
    else
    {
      //Log.log("ID_Sockets: "+mFtp.getFTP().id_sockets,"");
      Log.log(mn,"ID_Socket eliminado: "+evento.getID_Socket());
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

 }



 //==========================================================================
 /**
  * PTMFEventID_SocketInputStream
  */
 public void actionPTMFID_SocketInputStream(PTMFEventID_SocketInputStream evento)
 {
   //Log.log("\n\nNUEVO ID_SOCKETINPUTSTREAM","");
/*
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
          error(ioe.toString());
       }
   }

*/
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


  /**
   * Método main.
   * @param args
   */
  public static void main(String[] args)
  {
    //Comprobar parámetros
    if(args.length != 3)
    {
      uso();
      return;
    }

    try
    {
      EmisorFTPMulticast emisorFtpMulticast = new EmisorFTPMulticast(args[0],args[1],args[2]);
      emisorFtpMulticast.run();
    }
    catch(IOException io)
    {
      System.out.print(io.getMessage());
    }
  }


   //==========================================================================
  /**
   * Enviar Fichero
   */
   void sendFile() throws IOException
   {

     try
     {
          //Información del fichero....
          Log.log(mn,"Iniciando transferencia mFtp....");
          //ftp.insertTransmisionString("Tamaño del fichero: "+this.file.length()+" bytes",null);

          //Enviar IDFTP, Tamaño y Nombre del Fichero.....
          this.sendCabeceraFTP(this.file.length(),this.file.getName());

          //Buffer
          byte[] aBytes =  new byte[TAMAÑO_ARRAY_BYTES];

          long lTiempoInicio = System.currentTimeMillis();
          long lBytesTransmitidos = 0;
          long lFile = this.file.length();
          int contador = 0;
          //Transferir el FICHERO....
          for(lBytesTransmitidos = 0; lBytesTransmitidos<lFile;contador++)
          {
              //Leer bytes...
              int iBytesLeidos = this.fileInputStream.read(aBytes);

              if(iBytesLeidos == -1)
                break; //FIN FLUJO....

              //Log.log("\n\nBYTES LEIDOS: "+iBytesLeidos,"");

              //Transmitir los bytes leidos...
              //this.sendBytes(aBytes,iBytesLeidos);
               this.out.write(aBytes,0,iBytesLeidos);

              //Ajustar bytes transmitidos..
              lBytesTransmitidos+= iBytesLeidos;

              if((contador % 128)==0)
              {
               System.out.print("\r["+mn+"] Bytes transmitidos: "+lBytesTransmitidos);
              }

          }
       /*   long lTiempo = System.currentTimeMillis() - lTiempoInicio;
          long lHoras = 0;
          long lMinutos = 0;
          long lSegundos = 0;

          String mensaje = "Transmitido "+lBytesTransmitidos+" bytes en ";

            //Calcular Horas
            lHoras = ((lTiempo/1000)/60)/60;
            lMinutos = ((lTiempo/1000)/60)%60;
            lSegundos = ((lTiempo/1000)%60);
       */
       //  Log.log(mensaje+lHoras+":"+lMinutos+":"+lSegundos,"");


         //Emisión Fichero Finalizada....
         resumenTransferencia(lTiempoInicio, lBytesTransmitidos);

       }
       finally
       {
          try
          {
            //Cerrar Flujo Multicast...
            if(out!=null)
              this.out.close();

            //Cerrar flujo fichero...
            if(this.fileInputStream!= null)
                this.fileInputStream.close();
          }
          catch(IOException ioe){;}

       }
   }


 //==========================================================================
 /**
  * Resumen
  */
 private void resumenTransferencia(long lTiempoInicio, long lBytesTransmitidos)
 {
      long lHoras = 0;
      long lMinutos = 0;
      long lSegundos = 0;
      long lMSegundos = 0;
      long lTiempo = System.currentTimeMillis() - lTiempoInicio;
      double dKB_seg =0;

      String mensaje = "Transmitido "+lBytesTransmitidos+" bytes en ";

      dKB_seg = ((double)(lBytesTransmitidos)/(double)(lTiempo)) *1000;
      dKB_seg = (dKB_seg / 1024);

      if (lTiempo > 1000)
      {
        //Calcular Horas
        lHoras = ((lTiempo/1000)/60)/60;
        lMinutos = ((lTiempo/1000)/60)%60;
        lSegundos = ((lTiempo/1000)%60);
        lMSegundos = (lTiempo%1000);

        //Establecer el tiempo.....
        if(lHoras > 0)
          mensaje+=(lHoras+" hr. "+lMinutos+" min.");
        else if(lMinutos > 0)
          mensaje+=(lMinutos+" min. "+lSegundos+" seg.");
        else
          mensaje+=(lSegundos+" seg."+lMSegundos+" mseg.");
      }
      else
          mensaje+=(lTiempo+" mseg.");


      System.out.println("");
      System.out.println("");

      if (dKB_seg > 1)
      {
        int iParteEntera = (int)(dKB_seg );
        int iParteDecimal = (int)(dKB_seg *100)%100;
        Log.log(mn,mensaje);
        Log.log(mn,"Ratio Transferencia: "+iParteEntera+"."+iParteDecimal+" KB/Seg");
      }
      else
      {
        int i = (int)(dKB_seg * 100);
        Log.log(mn,mensaje);
        Log.log(mn,"Ratio Transferencia: 0."+i+" KB/Seg");
      }

  }





 //==========================================================================
 /**
  * Enviar un array de bytes
  * @param aBytes Un array de bytes
  * @param iBytes Número de Bytes dentro del array a transmitir.
  */
 private void sendBytes(byte[] aBytes,int iBytes) throws IOException
 {
    if(modo == PTMF.PTMF_FIABLE_RETRASADO
    || this.modo == PTMF.PTMF_FIABLE)
    {
      this.out.write(aBytes,0,iBytes);

      //ALEX: depuracion, comentar.
      // Log.log("====================================================","");
      // Log.log("EmisorFTPMulticast.sendBytes:",new String(aBytes,0,iBytes));
      // Log.log("====================================================","");
    }
    else
    {
      Buffer buf = new Buffer(aBytes);
      buf.setLength(iBytes);
      this.getDatagramSocket().send(buf);
    }
 }


 //==========================================================================
 /**
  * Enviar Identificador de mFtp PTMF v1.0, Enviar Tamaño del Fichero,
  * Enviar Nombre del Fichero.....
  */
 private void sendCabeceraFTP(long lSize,String sFileName) throws IOException
 {

      Buffer buf = new Buffer(15 + sFileName.length());

     //ID_FTP
      buf.addInt(MAGIC,0);
      buf.addByte((byte)VERSION,4);

      //Tamaño.-
      buf.addLong(lSize,5);
      Log.log(mn,"Enviando tamaño: "+lSize);

      //Nombre del Fichero.-
      buf.addShort(sFileName.length(),13);
      buf.addBytes(new Buffer(sFileName.getBytes()),0,15,sFileName.length());

      Log.log(mn,"Enviando nombre del fichero: "+sFileName);

    if(modo == PTMF.PTMF_FIABLE_RETRASADO
    || modo == PTMF.PTMF_FIABLE)
    {
     //ENVIAR BUFFER Y STRING...
      this.out.write(buf.getBuffer());
    }
    else
    {
      //ENVIAR LOS DATOS.....
      this.getDatagramSocket().send(buf);
    }

 }

/**
 * Imprime el mensaje de uso de la aplicación
 */
 private static void uso()
 {
    System.out.println("");
    System.out.println("EmisorFTPMulticast v1.0" );
    System.out.println("Uso: java PTMF.EmisorFTPMulticast <dir. ip multicast> <puerto> <fichero>");
    System.out.println("");
    System.out.println("");
 }


 /**
  * Imprime el mensaje de error
  * @param sError
  */
 private void error(String sError)
 {
    System.out.println("=========================================");
    System.out.print("Error: ");
    System.out.println(sError);

 }
}