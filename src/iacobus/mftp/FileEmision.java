/**
  Fichero: FileEmision.java  1.0 30/04/00
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


 //==========================================================================
 /**
  * Clase FileEmision.
  * Todos los datos relacionados con el Fichero de Emisión
  */
  class FileEmision
  {

   /** Objeto File */
   private File file = null;

   /** Objeto FileInputStream */
   private FileInputStream fileInputStream = null;

     /** Diálogo de recepción */
   private JDialogTransferencia jDialogTransferencia = null;

   /** ProtocoloFTPMulticast */
   private ProtocoloFTPMulticast protocoloFTPMulticast = null;

   /** Nueva Línea */
   private static final String newline = "\n";

   /** Icon */
   private Icon icon = null;

   /** Flag para para la transferencia */
   private boolean bStop = false;

   /** Flujo de Salida Multicast */
   private MulticastOutputStream out = null;

  //==========================================================================
  /**
   * Constructor.
   */
   public  FileEmision(ProtocoloFTPMulticast protocoloFTPMulticast,File file,Icon icon) throws IOException
   {
      this.file = file;
      this.protocoloFTPMulticast = protocoloFTPMulticast;
      this.icon = icon;

      if(this.protocoloFTPMulticast.getModo() == PTMF.PTMF_FIABLE_RETRASADO
        || this.protocoloFTPMulticast.getModo() == PTMF.PTMF_FIABLE )
      {
        //Flujo de salida Multicast ....
        out = this.protocoloFTPMulticast.getSocket().getMulticastOutputStream();
        if(out == null)
        {
          throw new IOException("Multicast sender flow is NULL.\n");
        }
      }

      if(file == null)
      {
        throw new IOException("File name is NULL.\n");
      }


      if(!this.file.exists())
      {
        throw new IOException("The system is not locating "+this.file.getName()+" \n as a valid file");
      }

      if(!this.file.canRead())
      {
        throw new IOException("The file "+this.file.getName()+" \n can't be reader.\nCheck read permission");
      }


      //Crear el Flujo de lectura del Fichero....
      fileInputStream = new FileInputStream(this.file);

      //Flujo de Salida Multicast...
      //this.out = this.protocoloFTPMulticast.getMulticastOutputStream();

      //Crear diálogo de transferencia.....
      this.jDialogTransferencia =  new JDialogTransferencia(this,null,
      "Transferencia Multicast...", false,this.file.getName(),this.file.length(),this.icon);

  }


  //==========================================================================
  /**
   * Enviar Fichero
   */
   void sendFile() throws IOException
   {
     mFtp ftp = mFtp.getFTP();

     try
     {
          //Información del fichero....
          ftp.insertTransmisionString("Init multicast transfer....","icono_informacion");
          ftp.insertTransmisionString("File size: "+this.file.length()+" bytes",null);

          if(!this.protocoloFTPMulticast.esActiva())
            return;

          //Mostrar el diálogo...
          this.jDialogTransferencia.show();

          //Enviar IDFTP, Tamaño y Nombre del Fichero.....
          this.sendCabeceraFTP(this.file.length(),this.file.getName());

          //Buffer
          byte[] aBytes =  new byte[ProtocoloFTPMulticast.TAMAÑO_ARRAY_BYTES];

          long lTiempoInicio = System.currentTimeMillis();
          long lBytesTransmitidos = 0;
          long lFile = this.file.length();

          if(!this.protocoloFTPMulticast.esActiva())
            return;

          //Transferir el FICHERO....
          for(lBytesTransmitidos = 0; lBytesTransmitidos<lFile && this.protocoloFTPMulticast.esActiva();)
          {
              //Leer bytes...
              int iBytesLeidos = this.fileInputStream.read(aBytes);

              if(iBytesLeidos == -1)
                break; //FIN FLUJO....

              //Log.log("\n\nBYTES LEIDOS: "+iBytesLeidos,"");

              //Transmitir los bytes leidos...
              this.sendBytes(aBytes,iBytesLeidos);

              //Ajustar bytes transmitidos..
              lBytesTransmitidos+= iBytesLeidos;
                  this.jDialogTransferencia.setBytesTransmitidos(lBytesTransmitidos);

              if(this.bStop == true || !this.protocoloFTPMulticast.esActiva())
              {
                 file = null;

                 ftp.insertTransmisionString("Transfer canceled by user.","icono_informacion");
                 return;
              }
          }
          long lTiempo = System.currentTimeMillis() - lTiempoInicio;
          long lHoras = 0;
          long lMinutos = 0;
          long lSegundos = 0;

          String mensaje = "Transmited "+lBytesTransmitidos+" bytes in ";

            //Calcular Horas
            lHoras = ((lTiempo/1000)/60)/60;
            lMinutos = ((lTiempo/1000)/60)%60;
            lSegundos = ((lTiempo/1000)%60);

         Log.log(mensaje+lHoras+":"+lMinutos+":"+lSegundos,"");


         this.jDialogTransferencia.setVisible(false);

         //Emisión Fichero Finalizada....
         resumenTransferencia();
         this.file = null;
         this.bStop = true;
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
  * Parar transferencia
  */
 public void stopTransferencia()
 {
   this.bStop = true;
   this.file = null;

   this.protocoloFTPMulticast.file = null;
 }


 //==========================================================================
 /**
  * Resumen
  */
 private void resumenTransferencia()
 {
      long lHoras = 0;
      long lMinutos = 0;
      long lSegundos = 0;
      long lMSegundos = 0;
      long lTiempo = this.jDialogTransferencia.getlTiempo();
      String mensaje = "Transmited "+this.jDialogTransferencia.getlBytesTransmitidos()+" bytes in ";
      mFtp ftp = mFtp.getFTP();

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



      double dKB_seg = this.jDialogTransferencia.getdKB_seg();

      if (dKB_seg > 1)
      {
        int iParteEntera = (int)(dKB_seg );
        int iParteDecimal = (int)(dKB_seg *100)%100;
        ftp.insertTransmisionString(mensaje+" Transfer rate "+iParteEntera+"."+iParteDecimal+" KB/Seg","icono_tarea");
      }
      else
      {
        int i = (int)(dKB_seg * 100);
        ftp.insertTransmisionString(mensaje+" Transfer rate: 0."+i+" KB/Seg","icono_tarea");
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
    if(this.protocoloFTPMulticast.getModo() == PTMF.PTMF_FIABLE_RETRASADO
    || this.protocoloFTPMulticast.getModo() == PTMF.PTMF_FIABLE)
    {
      this.out.write(aBytes,0,iBytes);
    }
    else
    {
      Buffer buf = new Buffer(aBytes);
      buf.setLength(iBytes);
      this.protocoloFTPMulticast.getDatagramSocket().send(buf);
    }
 }


 //==========================================================================
 /**
  * Enviar Identificador de mFtp PTMF v1.0, Enviar Tamaño del Fichero,
  * Enviar Nombre del Fichero.....
  */
 private void sendCabeceraFTP(long lSize,String sFileName) throws IOException
 {
      mFtp ftp = mFtp.getFTP();

      Buffer buf = new Buffer(15 + sFileName.length());

     //ID_FTP
      buf.addInt(ProtocoloFTPMulticast.MAGIC,0);
      buf.addByte((byte)ProtocoloFTPMulticast.VERSION,4);

      //Tamaño.-
      buf.addLong(lSize,5);
      ftp.insertTransmisionString("Sending size: "+lSize,null);

      //Nombre del Fichero.-
      buf.addShort(sFileName.length(),13);
      buf.addBytes(new Buffer(sFileName.getBytes()),0,15,sFileName.length());

      ftp.insertTransmisionString("Sending file name: "+sFileName,null);

    if(this.protocoloFTPMulticast.getModo() == PTMF.PTMF_FIABLE_RETRASADO
    || this.protocoloFTPMulticast.getModo() == PTMF.PTMF_FIABLE)
    {
     //ENVIAR BUFFER Y STRING...
      this.out.write(buf.getBuffer());
    }
    else
    {
      //ENVIAR LOS DATOS.....
      this.protocoloFTPMulticast.getDatagramSocket().send(buf);
    }

 }

 }
