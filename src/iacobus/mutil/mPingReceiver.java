package iacobus.mutil;

import iacobus.ptmf.*;
import java.net.*;
import java.io.*;
import javax.swing.JOptionPane;
import javax.swing.Icon;
import java.util.TreeMap;
import java.util.Iterator;



/**
 * <p>Title: MTest</p>
 *
 * <p>Description: Test de red multicast</p>
 * Utilidad de verificacion de paquetes multicast en la red. Se pone en escucha
 * e imprime la información multicast que le llegue.
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author Alejandro Garcia
 * @version 1.0
 */
public class mPingReceiver {


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
public mPingReceiver(String dirIPMulticast, String puerto) throws IOException
{
System.out.println("");
System.out.println("");
System.out.println("mPingReceiver v1.0");
System.out.println("(C)2005-2014 M.Alejandro Garcia");
System.out.println("");


//Obtener parámetros
this.dirIPMcast = new Address(dirIPMulticast,Integer.parseInt(puerto));

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

     byte[] buf = new byte[this.TAMAÑO_ARRAY_BYTES];
     DatagramPacket recv = new DatagramPacket(buf, buf.length);
     int puerto = 0;
     int size = 0;
     int contador = 0;
    while(true)
    {
        //Leer datos...
        socket.receive(recv);
        puerto = recv.getPort();
        size = recv.getLength();


        Address add = new Address(recv.getAddress(),puerto);
        contador =contador+1;
        Log.log("","[Paquete "+contador+"] "+add.getHostAddress()+":"+dirIPMcast.getPort()+ " size: "+size);

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


/**
* Método main.
* @param args
*/
public static void main(String[] args)
{
//Comprobar parámetros
if(args.length != 2)
{
  uso();
  return;
}

try
{
    mPingReceiver mescucha = new mPingReceiver(args[0],args[1]);
    mescucha.run();
}
catch(IOException io)
{
  System.out.print(io.getMessage());
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
System.out.println("mPingReceiver v1.0" );
System.out.println("Uso: java iacobus.mutil.mPingReceiver <dir. ip multicast> <puerto>");
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



