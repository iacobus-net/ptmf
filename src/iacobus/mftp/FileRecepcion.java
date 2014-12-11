//============================================================================
//
//	Copyright (c) 1999 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: FileRecepcion.java  1.0 30/04/00
//
// 	Autores: 	M. Alejandro García Domínguez (AlejandroGarcia@wanadoo.es)
//						Antonio Berrocal Piris
//
//	Descripción: Clase FileRecepcion.
//
//
//----------------------------------------------------------------------------



package iacobus.mftp;

import iacobus.ptmf.*;
import java.io.*;
import javax.swing.JOptionPane;
import javax.swing.Icon;
import java.util.TreeMap;


 //==========================================================================
 /**
  * Clase FileRecepcion.
  * Todos los datos relacionados con el Fichero de recepción
  */
  class FileRecepcion extends Thread
  {

   /** Objeto File */
   private File file = null;

   /** Objeto FileOutputStream */
   private FileOutputStream fileOutputStream = null;

   /** Tamaño del Fichero */
   private long lFileSize = 0;

   /** Nombre del Fichero */
   private String sFileName = null;

   /** Diálogo de recepción */
   private JDialogRecepcion jDialogRecepcion = null;

   /**  ID_SocketInputStream */
   private ID_SocketInputStream id_socketIn = null;

   /** ID_Socket */
   private ID_Socket id_socket = null;

   /** ProtocoloFTPMulticast */
   private ProtocoloFTPMulticast protocoloFTPMulticast = null;

  /** Nueva Línea */
  private static final String newline = "\n";

  /** Bytes Leidos */
  private long lBytesLeidos = 0;


  //==========================================================================
  /**
   * Constructor. Utilizado en Modo NO FIABLE.
   */
   public  FileRecepcion(ProtocoloFTPMulticast protocoloFTPMulticast, ID_Socket id_socket) throws IOException
   {
      super("FileRecepcion");
      this.setDaemon(true);

      this.id_socket = id_socket;
      this.protocoloFTPMulticast = protocoloFTPMulticast;

  }

  //==========================================================================
  /**
   * Constructor. Utilizado en MODO FIABBLE
   * @exception IOException Si no se puede crear el fichero. <br>
   *  MOSTRAR EL STRING DE LA EXCEPCION COMO UN ERROR.
   */
   public FileRecepcion(ProtocoloFTPMulticast protocoloFTPMulticast,ID_SocketInputStream id_socketIn )
  throws IOException

   {
      super("FileRecepcion");
      this.setDaemon(true);

      this.protocoloFTPMulticast = protocoloFTPMulticast;
      this.id_socketIn = id_socketIn;
   }


  //==========================================================================
  /**
   * Método Run()
   */
  public void run()
  {
   //Log.log("\n\nFILERECEPCION","");
   try
   {
      //RECIBIR UN FICHERO
      this.receiveFile();

      if(this.id_socketIn!=null)
        this.protocoloFTPMulticast.removeFileRecepcion(this.id_socketIn);


   }
   catch(ParametroInvalidoExcepcion e)
   {
     protocoloFTPMulticast.getFTP().error(e.getMessage());
   }
   catch(PTMFExcepcion e)
   {
     protocoloFTPMulticast.getFTP().error(e.getMessage());
   }
   catch(IOException e)
   {
     protocoloFTPMulticast.getFTP().error(e.getMessage());
   }

   finally
   {

     Log.log("*.- FIN FileRecepcion FIABLE.","");
   }
  }

 //==========================================================================
 /**
  * Recibir UN FICHERO
  */
  private void receiveFile()  throws IOException
 {
     this.file = null;

     //Log.log("receiveFile()","");

     //---------------------------------
     //Recibir ID...
     if(!receiveIDFTPMulticast())
     {
        tirarBytes();
        return;
     }

     //Recibir FileSize...
     lFileSize = this.receiveFileSize();
     if(lFileSize <= 0)
     {
        tirarBytes();
        return;
     }

     //Recibir FileName...
     sFileName = this.receiveFileName();
     if (sFileName == null)
     {
        tirarBytes();
        return;
     }

      //Fichero temporal..
      this.file = File.createTempFile("mftp"+System.currentTimeMillis(),".tmp");

      //Comprobar si ya existe.
      if(this.file.exists())
      {
        if(!this.file.delete())
            throw new IOException("No se ha podido borrar el fichero "+sFileName+newline+"1. Compruebe si tiene privilegios."+newline+"2.Compruebe que el fichero no está siendo usado por otra aplicación.");

      }

      //Crear el fichero...
      if(!this.file.createNewFile())
      {
          //mensajeErrorEscritura();
          throw new IOException("No se ha podido CREAR el fichero "+sFileName+newline+"1. Compruebe si tiene privilegios."+newline+"2.Compruebe que el fichero no está siendo usado por otra aplicación.");

      }

      //Comprobar si se puede escribir.
      if(!this.file.canWrite())
      {
          //mensajeErrorEscritura();
          throw new IOException("No se puede ESCRIBIR en el fichero "+sFileName+newline+"1. Compruebe si tiene privilegios."+newline+"2.Compruebe que el fichero no está siendo usado por otra aplicación.");
      }

      //Flujo de salida al fichero...
      this.fileOutputStream = new FileOutputStream(file);

     //Iniciar JDialogRecepcion
     this.jDialogRecepcion = new JDialogRecepcion(this,null,"Recepción Multicast... "+this.id_socketIn.getID_Socket(),false,sFileName,lFileSize,null,this.id_socketIn.getID_Socket());
     this.jDialogRecepcion.show();

     try
     {
      if(protocoloFTPMulticast.getModo() == PTMF.PTMF_FIABLE_RETRASADO
      || protocoloFTPMulticast.getModo() == PTMF.PTMF_FIABLE)
      {
         MFtp.getFTP().insertRecepcionString("Iniciando la recepción... de "+sFileName,null);

         lBytesLeidos = 0;
         byte[] bytes = new byte[1024*2];

         while(protocoloFTPMulticast.esActiva()/* && (lBytesLeidos < lFileSize) */&& (this.file!=null))
         {

            //if(this.id_socketIn.available() > 0)
            //{
                //Log.log("Bytes disponibles: "+bytes.length,"");
                int iBytesLeidos = this.id_socketIn.read(bytes);

                //FIN DE FLUJO???...
                if(iBytesLeidos == -1)
                {
                  Log.log("FILERECPECION -> RECEIVEFILE : FIN DE FLUJO*","");
                  break;
                }

                //Ajustar tamaño...
                lBytesLeidos+= iBytesLeidos;
                this.jDialogRecepcion.setBytesRecibidos(lBytesLeidos);
                try
                {
                  this.fileOutputStream.write(bytes,0,iBytesLeidos);
                }
                catch(IOException e)
                {
                  mensajeErrorEscribiendo(e.getMessage());
                  throw e;
                }

            //}
            //else
            //  Temporizador.sleep(10);
         }

         //Mostrar resumen de recepción...
         this.resumenRecepcion();


      }
      else
      {
          //NUNCA DEBE DE ENTRAR AQUI.
      }
  }
  finally
  {

     //Eliminar este objeto del treemap en la clase ProtocoloFTPMulticast
     if(this.protocoloFTPMulticast.getModo() == PTMF.PTMF_FIABLE_RETRASADO
     || protocoloFTPMulticast.getModo() == PTMF.PTMF_FIABLE)
     {
         this.protocoloFTPMulticast.removeFileRecepcion(this.id_socketIn);
     }
     else
     {
        this.protocoloFTPMulticast.removeFileRecepcion(this.id_socket);
     }

     //desactivar el diálogo de recepción...
     if(this.jDialogRecepcion!= null)
      this.jDialogRecepcion.setVisible(false);


     //Cerrar  Flujos...
     this.id_socketIn.close();
     this.id_socketIn = null;

     if(this.fileOutputStream!= null)
      this.fileOutputStream.close();


     if(protocoloFTPMulticast.esActiva() && this.file!=null)
          {
             //Cambiar Localización y Nombre del fichero...
           File MFTPfile = new File(sFileName);
           if(MFTPfile.exists())
           {
             if(this.mensajeFileExists())
             {
              if (!MFTPfile.delete())
              {
                this.protocoloFTPMulticast.getFTP().error("No se ha podido eliminar el fichero: "+sFileName+newline+"1. Compruebe si tiene privilegios suficientes."+newline+"2.Compruebe que el fichero no está siendo usado por otra aplicación.");
                 //Eliminar temporal
                  this.file.delete();
                  this.file=null;
                return;
              }

               if(!file.renameTo(MFTPfile))
               {
                this.protocoloFTPMulticast.getFTP().error("No se ha podido renombrar a:"+sFileName+newline+"1. Compruebe si tiene privilegios suficientes."+newline+"2.Compruebe que el fichero no está siendo usado por otra aplicación.");
                //Eliminar temporal
                this.file.delete();
                  this.file=null;
               }
             }
           }
           else if(!file.renameTo(MFTPfile))
           {
             this.protocoloFTPMulticast.getFTP().error("No se ha podido renombrar a:"+sFileName+newline+"1. Compruebe si tiene privilegios suficientes."+newline+"2.Compruebe que el fichero no está siendo usado por otra aplicación.");
              //Eliminar temporal
              this.file.delete();
              this.file=null;
          }
    }




  }

 }

 //==========================================================================
 /**
  * Lee del flujo de entrada todos lo bytes y los tira.
  */
  private void tirarBytes()throws IOException
  {
    MFtp ftp = MFtp.getFTP();

    try
    {
         this.file = new File("nulo");

         ftp.insertRecepcionString("Transferencia ya iniciada. Esperando siguiente para sincronizacion...","icono_informacion" );
         while(protocoloFTPMulticast.esActiva() && (this.file!=null))
         {

                byte[] bytes = new byte[this.id_socketIn.available()];
                //Log.log("Bytes disponibles: "+bytes.length,"");
                int iBytesLeidos = this.id_socketIn.read(bytes);

                //FIN DE FLUJO???...
                if(iBytesLeidos == -1)
                {
                  Log.log("tirar bytes : FIN DE FLUJO**************************","");
                  return;
                }
         }
    }
    finally
    {
          this.file = null;
    }
  }


 //==========================================================================
 /**
  * Recibir UN FICHERO. MODO NO FIABLE
  */
 void receiveFile(long lFileSize, String sFileName)  throws IOException
 {

    this.lFileSize = lFileSize;
    this.sFileName = sFileName;
    Log.log("Fichero: "+sFileName+" longitud: "+lFileSize,"");
      //Fichero..
      this.file = new File(sFileName);

      //Comprobar si ya existe.
      if(this.file.exists())
      { if (mensajeFileExists())
        {
          if(!this.file.delete())
            throw new IOException("No se ha podido borrar el fichero "+sFileName+newline+"1. Compruebe si tiene privilegios."+newline+"2.Compruebe que el fichero no está siendo usado por otra aplicación.");
        }
        else
        {
            throw new IOException("El fichero "+sFileName+" ya existe. NO SOBREESCRIBIR.");
        }
      }

      //Crear el fichero...
      if(!this.file.createNewFile())
      {
          //mensajeErrorEscritura();
          throw new IOException("No se ha podido CREAR el fichero "+sFileName+newline+"1. Compruebe si tiene privilegios."+newline+"2.Compruebe que el fichero no está siendo usado por otra aplicación.");

      }

      //Comprobar si se puede escribir.
      if(!this.file.canWrite())
      {
          //mensajeErrorEscritura();
          throw new IOException("No se puede ESCRIBIR en el fichero "+sFileName+newline+"1. Compruebe si tiene privilegios."+newline+"2.Compruebe que el fichero no está siendo usado por otra aplicación.");
      }

      //Flujo de salida al fichero...
      this.fileOutputStream = new FileOutputStream(file);

     //Iniciar JDialogRecepcion
     this.jDialogRecepcion = new JDialogRecepcion(this,null,"Recepción Multicast...",false,sFileName,lFileSize,null,this.id_socket);
     this.jDialogRecepcion.show();

   }

 //==========================================================================
 /**
  * Añadir Bytes. MODO NO FIABLE.
  */
  void addBytes(byte[] aBytes,int iBytes) throws IOException
  {

     if(this.file==null)
      return;

     if(lBytesLeidos < lFileSize)
     {
         //Ajustar tamaño...
         lBytesLeidos+= iBytes;
         this.jDialogRecepcion.setBytesRecibidos(lBytesLeidos);
         try
         {
            this.fileOutputStream.write(aBytes,0,iBytes);
         }
         catch(IOException e)
         {
             mensajeErrorEscribiendo(e.getMessage());
             throw e;
         }
     }
     else
     {
       if(this.jDialogRecepcion!= null)
        this.jDialogRecepcion.setVisible(false);

       this.resumenRecepcion();

       //Cerrar  Flujos
       if(this.fileOutputStream!= null)
         this.fileOutputStream.close();

      //Eliminar de ProtocoloFTPMulticast
      this.protocoloFTPMulticast.removeFileRecepcion(this.id_socket);

    }
 }


 //==========================================================================
 /**
  * Resumen recepcion
  */
 private void resumenRecepcion()
 {
      long lHoras = 0;
      long lMinutos = 0;
      long lSegundos = 0;
      long lTiempo = this.jDialogRecepcion.getlTiempo();
      String mensaje = "Transferencia Finalizada. Recibido "+this.jDialogRecepcion.getlBytesRecibidos()+" bytes en ";
      MFtp ftp = MFtp.getFTP();

      if (lTiempo > 1000)
      {
        //Calcular Horas
        lHoras = ((lTiempo/1000)/60)/60;
        lMinutos = ((lTiempo/1000)/60)%60;
        lSegundos = ((lTiempo/1000)%60);

        //Establecer el tiempo.....
        if(lHoras > 0)
          mensaje+=(lHoras+" hr. "+lMinutos+" min.");
        else if(lMinutos > 0)
          mensaje+=(lMinutos+" min. "+lSegundos+" seg.");
        else
          mensaje+=(lSegundos+" seg.");
      }
      else
          mensaje+=(lTiempo+" mseg.");



      double dKB_seg = this.jDialogRecepcion.getdKB_seg();

      //Comprobar que no es 0
      if (lTiempo == 0 && dKB_seg ==0)
          dKB_seg = this.jDialogRecepcion.getlBytesRecibidos();

      if (dKB_seg > 1)
      {
        int iParteEntera = (int)(dKB_seg );
        int iParteDecimal = (int)(dKB_seg *100)%100;
        ftp.insertRecepcionString(mensaje+" Ratio Transferencia: "+iParteEntera+"."+iParteDecimal+" KB/Seg","icono_tarea");
      }
      else
      {
        int i = (int)(dKB_seg * 100);
        ftp.insertRecepcionString(mensaje+" Ratio Transferencia: 0."+i+" KB/Seg","icono_tarea");
      }

 }

 //==========================================================================
 /**
  * Recibir Identificador de MFtp PTMF v1.0
  * @return true si se ha recibido el Identificador de MFtp, false en caso contrario
  */
 private boolean receiveIDFTPMulticast() throws IOException
 {
   boolean bOK = false;
   try
   {
      Buffer buf = new Buffer(5);

      //Leer Datos...
      this.id_socketIn.read(buf.getBuffer());

      //Comprobar MAGIC
      if(buf.getInt(0) != 0x6DED757B)
        return bOK;

      //Comprobar VERSION
      if(buf.getByte(4) != 0x01)
        return bOK;

      bOK = true;

    Log.log("Iniciando la recepción MFtp...","");

   }
    finally{ return bOK;}
 }



 //==========================================================================
 /**
  * Recibir Nombre del Fichero
  * @return sFileName Nombre del Fichero
  */
 private String receiveFileName() throws IOException
 {
   String sFileName = null;
   byte[] bytes = null;
   try
   {
        Buffer buf = new Buffer(2);

        int iLong = 0;

        //Obtener la longitud del nombre del fichero...
        this.id_socketIn.read(buf.getBuffer());

        iLong = (int)buf.getShort(0);
        if(iLong > 0)
          bytes = new byte[iLong];
        this.id_socketIn.read(bytes);

        //Obtener el nombre del fichero...
        sFileName = new String(bytes);


        Log.log("Recibiendo fichero: "+sFileName+" del emisor: "+this.id_socketIn.getID_Socket(),"");
   }

  finally{ return sFileName;}
 }

 //==========================================================================
 /**
  * Recibir Tamaño del Fichero
  * @return lSize Tamaño del Fichero
  */
 private long receiveFileSize() throws IOException
 {
   long lFileSize = 0;
   try
   {
        Buffer buf = new Buffer(8);

        //Obtener la longitud del nombre del fichero...
        this.id_socketIn.read(buf.getBuffer());

        lFileSize = buf.getLong(0);

        Log.log("Tamaño: "+lFileSize+newline,"");
   }
   finally{ return lFileSize;}
 }

//==========================================================================
 /**
  * Recibir Nombre del Fichero
  * @param buf Un objeto Buffer con los datos.
  * @return sFileName Nombre del Fichero. null Si hay un error
  */
 public static String parseFileName(Buffer buf)
 {
   String sFileName = null;

   try
   {
        int iLong = 0;
        byte[] bytes = null;

        //Obtener la longitud del nombre del fichero...
        iLong = (int)buf.getInt(16);
        bytes = buf.getBytes(20,iLong);

        //Obtener el nombre del fichero...
        sFileName = new String(bytes);

       // this.getFTP().insertStringJTextPane(" ","icono_entrada");
       // this.getFTP().insertStringJTextPane("Recibiendo fichero: "+sFileName+newline,"entrada");
   }
   catch(ParametroInvalidoExcepcion e){;}

   finally{ return sFileName;}
 }

 //==========================================================================
 /**
  * Comprobar Tamaño del Fichero
  * @param buf Un objeto Buffer con los datos.
  * @return lSize Tamaño del Fichero. 0 si hay un error
  */
 public static long parseFileSize(Buffer buf) throws IOException
 {
   long lFileSize = 0;
   try
   {
      //Obtener la longitud  del fichero...
      lFileSize = buf.getLong(8);

     // this.getFTP().insertStringJTextPane(" ","icono_entrada");
     // this.getFTP().insertStringJTextPane("Tamaño: "+lFileSize+newline,"entrada");

   }
   catch(ParametroInvalidoExcepcion e){;}

   finally{ return lFileSize;}
 }

   //==========================================================================
 /**
  * Comprueba Identificador de MFtp PTMF v1.0
  * @param buf Un objeto Buffer con los datos leidos.
  * @return true si se ha recibido el Identificador de MFtp, false en caso contrario
  */
 public static boolean parseIDFTPMulticast(Buffer buf) throws IOException
 {
   boolean bOK = false;
   try
   {
      //Leer Magic
      if(buf.getInt(0) != ProtocoloFTPMulticast.MAGIC)
       return bOK;

      if( buf.getInt(4) != ProtocoloFTPMulticast.VERSION)
        return bOK;

      bOK = true;

   }
    finally{ return bOK;}
 }
 //==========================================================================
 /**
  * Recibir los bytes del fichero
  */
 private void receiveFileNO_Fiable(String sFileName)  throws IOException
 {
 /*    long lBytesLeidos = 0;

     try
     {
      // Crear Fichero.
      this.file = new File(sFileName);
      if(this.file.exists())
        if (mensajeFileExists())
        {
          if(!this.file.delete())
            return;
        }
        else
          return;

      //Crear el fichero...
      if(!this.file.createNewFile())
      {
        mensajeErrorEscritura();
        return;
      }

      if(!this.file.canWrite())
      {
        mensajeErrorEscritura();
        return;
      }

      //Flujo de salida al fichero...
      this.fileOutputStream = new FileOutputStream(file);
      if(this.modo == PTMF.PTMF_FIABLE_RETRASADO
      || this.modo == PTMF.PTMF_FIABLE)
      {
         this.getFTP().insertStringJTextPane(" ","icono_entrada");
         this.getFTP().insertStringJTextPane("Iniciando la recepción... de "+sFileName+newline,"entrada");

         while(runFlag && lBytesLeidos < lFileSize)
         {
            if(this.id_socketIn.available() > 0)
            {
                byte[] bytes = new byte[this.id_socketIn.available()];
                int iBytesLeidos = this.id_socketIn.read(bytes);

                //Ajustar tamaño...
                lBytesLeidos+= iBytesLeidos;
                this.jDialogRecepcion.setBytesRecibidos(lBytesLeidos);
                try
                {
                  this.fileOutputStream.write(bytes,0,iBytesLeidos);
                }
                catch(IOException e)
                {
                  mensajeErrorEscribiendo(e.getMessage());
                  throw e;
                }

            }
         }
         this.fileOutputStream.close();

      }
      else
      {
      }
  }
  finally{;}
  */
 }


 //==========================================================================
 /**
  * Mensaje de advertencia--> Error Escribiendo
  */
 private void mensajeErrorEscribiendo(String sError)
 {
    JOptionPane.showMessageDialog(null,"Se ha producido un error mientras se intentaba escribir en el fichero"+newline+"\""+sFileName+newline+"\""+"El error es el siguiente:"+sError ,
				    "Error Escritura",JOptionPane.ERROR_MESSAGE);
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
    int iOpcion =  JOptionPane.showConfirmDialog(null,"El fichero \""+sFileName+"\" ya existe."+newline+"¿Desea sobreescribir el fichero existente?",
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
  * Parar transferencia
  */
 public void stopTransferencia()
 {
   // this.bStop = true;
   if(this.file!= null)
    this.file.delete();

   this.file = null;
  MFtp.getFTP().insertRecepcionString("Recepción cancelada por el usuario.","icono_informacion");
 }
 }
