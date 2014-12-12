

package multicast.test;

import java.net.*;
import java.io.*;
import java.util.Random;
import java.lang.*;

import net.iacobus.ptmf.Buffer;
import net.iacobus.ptmf.PTMF;
import net.iacobus.ptmf.PTMFExcepcion;
import net.iacobus.ptmf.ParametroInvalidoExcepcion;

public class pruebaBuffer{

  public pruebaBuffer() {
  }

  void log(String msg)
  {
   System.out.println(msg);
  }

  public void run()
  {
    Buffer buf = null;
    log("Prueba Buffer...");

    try{
      buf = new Buffer(PTMF.TPDU_MAX_SIZE);





    log("Añadir datos...");
/*    buf.addByte((byte)150,);
    buf.addByte((byte)214);
    buf.addByte((byte)142);
    buf.addByte((byte)1);
    buf.addInt(5000);
    buf.addShort((short)45);
  */log("Longitud: "+buf.getLength());
    log("offset: "+buf.getOffset());
    log("maxLength: "+buf.getMaxLength());

    log("obtener datos");
    int off = 0;
    log("offset "+(off)+": "+buf.getByte(off));
    off++;
    log("offset "+off+": "+buf.getByte(off));
    off++;
    log("offset "+off+": "+buf.getByte(off));
    off++;
    log("offset "+off+": "+buf.getByte(off));
    off++;
    log("offset "+off+": "+buf.getInt(off));
    off+=4;
    log("offset "+off+": "+buf.getShort(off));

    String cadena = new String("Prueba CHAR");
    log(cadena);
    Buffer buffer = new Buffer(cadena.length()*2);
    log("Longitud de cadena: "+cadena.length());
    log("Añadir al buffer...");
    buffer.addArrayChar(cadena.toCharArray(),0);
    log("Buffer: "+buffer);
    log("Obtener del buffer...");

    char[] nueva_cadena = buffer.getArrayChar(0,cadena.length());
    log("Resultado....");
    log(new String(nueva_cadena));




     }
    catch(PTMFExcepcion e)
    {
     log(e.getMessage());
    }
    catch(ParametroInvalidoExcepcion e)
    {
     log(e.getMessage());
    }
    try
    {
      byte[] buffer={(byte)255,0,0,0};
       buf = new Buffer(buffer);

      byte b = -100;
      log("byte: "+b);

      int i = -234;
      log("int: "+i);

      i = b;
      log("int: "+i);

      log("Prueba de acceso a buffer");
      log("int: "+buf.getInt(0));
      log("byte 0: "+buf.getByte(0));
      log("byte 1: "+buf.getByte(1));
      log("byte 2: "+buf.getByte(2));
      log("byte 3: "+buf.getByte(3));

      log("short: "+buf.getShort(0));
    }
    catch(ParametroInvalidoExcepcion e)
    {
      log(e.getMessage());
    }
  }

  public final static void main(String[] args)
  {
    pruebaBuffer a = new pruebaBuffer();
    a.run();


    // Probar fórmula:

    /** Generador de números aleatorios. */
/*    Random random  = null;
    random = new Random (System.currentTimeMillis ());

    long tRandom;
    long result;

    for (int i=0;i<800;i++)
    {
     tRandom = random.nextInt (1000 - 200);
     result = (tRandom + 200) % 1000;

     if (result < 200)
       System.out.println ("------------------------->>>>><<<<<<<<----------------");
     System.out.println ("Iteracción " + i + ": result = (" + tRandom + " + 200) % 1000 = "
                        + result);
     if (result < 200)
       System.out.println ("------------------------->>>>><<<<<<<<----------------");

    } // Fin del for




*/

  }

}
