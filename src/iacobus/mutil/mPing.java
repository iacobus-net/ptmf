package iacobus.mutil;

import java.net.MulticastSocket;
import iacobus.ptmf.Address;
import java.net.DatagramPacket;
import java.io.IOException;
import java.lang.Integer;
import iacobus.ptmf.Log;

/**
 * <p>Title: mPing</p>
 *
 * <p>Description: Test de red multicast</p>
 * Utilidad para enviar paquetes de sondeo multicast
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author Alejandro Garcia
 * @version 1.0
 */
public class mPing {
    public mPing() {
    }


    /** Tamaño del array de Transmisión/Recepcion */
    public static final int TAMAÑO_ARRAY_BYTES = 1024 * 2;


    /** Nueva Línea */
    private static final String newline = "\n";

    /** Dirección Multicast:puerto*/
    private Address dirIPMcast = null;

    /**TTL sesion */
    private int TTLSesion = 8;


    /** Socket Multicast Fiable*/
    private MulticastSocket socket = null;

    /** Flag de inicio del thread */
    private boolean runFlag = true;

    /** Ratio ed transferencia */
    private long lRatio = 0;


//==========================================================================
    /**
    * Constructor
    */
    public mPing(String dirIPMulticast, String puerto,String ttl) throws IOException
    {
    System.out.println("");
    System.out.println("");
    System.out.println("mPing v1.0");
    System.out.println("(C)2005 M.Alejandro Garcia");



    //Obtener parámetros
    this.dirIPMcast = new Address(dirIPMulticast,Integer.parseInt(puerto));
    this.TTLSesion = Integer.parseInt(ttl);

    }


//==========================================================================
    /**
    * Método Run()
    */
    public void run()
    {


    try
    {
        //Crear el socket multicast
        socket = new MulticastSocket(dirIPMcast.getPort());
        Log.log("","Socket creado. OK");

        //Join
        socket.joinGroup(dirIPMcast.getInetAddress());
        Log.log("","Join OK."+dirIPMcast.getHostAddress()+":"+dirIPMcast.getPort());

        //Buffer de recepcion
        socket.setReceiveBufferSize(1024*64);
        socket.setSendBufferSize(1024*64);

        socket.setTimeToLive(this.TTLSesion);
        Log.log("","TTL: "+this.TTLSesion);

         byte[] buf = new byte[this.TAMAÑO_ARRAY_BYTES];
         DatagramPacket recv = new DatagramPacket(buf, buf.length);
         int puerto = 0;
         int size = 0;
         int contador = 1;
        while(true)
        {
            //enviar datos cada segundo...
            String msg= "Ping nº "+contador;
            DatagramPacket hi = new DatagramPacket(msg.getBytes(), msg.length(),dirIPMcast.getInetAddress(),dirIPMcast.getPort());
            socket.send(hi);

            Log.log("","[mPing "+contador+"] "+dirIPMcast.getHostAddress()+":"+dirIPMcast.getPort());

            contador =contador+1;
            iacobus.ptmf.Temporizador.sleep(1000);
        }


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
      socket.leaveGroup(dirIPMcast.getInetAddress());
      socket.close();
    }
    catch(IOException e)
    {
       error(e.getMessage());
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
       // Log.log(mn,mensaje);
       // Log.log(mn,"Ratio Transferencia: "+iParteEntera+"."+iParteDecimal+" KB/Seg");
      }
      else
      {
        int i = (int)(dKB_seg * 100);
        //Log.log(mn,mensaje);
        //Log.log(mn,"Ratio Transferencia: 0."+i+" KB/Seg");
      }

    }






    /**
    * Imprime el mensaje de uso de la aplicación
    */
    private static void uso()
    {
    System.out.println("");
    System.out.println("mPing v1.0" );
    System.out.println("Uso: java mtest.mPing <dir. ip multicast> <puerto> <ttl>");
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
          mPing mping = new mPing(args[0],args[1],args[2]);
          mping.run();
      }
      catch(IOException io)
      {
        System.out.print(io.getMessage());
      }
      }



}
