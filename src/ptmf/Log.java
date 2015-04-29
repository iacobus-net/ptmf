//============================================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: Log.java  1.0 30/08/99
//
//
//	Descripción: Clase log. Imprime mensajes logging y de depuración en stdout.
//
// 	Authors: 
//		 Alejandro García-Domínguez (alejandro.garcia.dominguez@gmail.com)
//		 Antonio Berrocal Piris (antonioberrocalpiris@gmail.com)
//
//  Historial: 
//  07.04.2015 Changed licence to Apache 2.0     
//
//  This file is part of PTMF 
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//----------------------------------------------------------------------------


package ptmf;

import java.io.*;

/**
 * Clase Log. Imprime mensajes logging y de depuración en stdout.
 * @version 1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */

public class Log extends PrintStream {


  static OutputStream logfile;
  static PrintStream  oldStdout;
  static PrintStream  oldStderr;
  static boolean      bPantalla = false;


  /** Niveles de depuración */
  public static final int TODO            = 0xFFFFFFFF;
  public static final int IDGL            = 0x00000001;
  public static final int TPDU            = 0x00000002;
  public static final int TPDU_CGL        = 0x00000004;
  public static final int TPDU_DATOS      = 0x00000008;
  public static final int TEMPORIZADOR    = 0x00000010;
  public static final int CGL             = 0x00000020;
  public static final int SOCKET          = 0x00000040;
  public static final int CANAL_MULTICAST = 0x00000080;
  public static final int CANAL_UNICAST   = 0x00000090;
  public static final int ACK             = 0x00000100;
  public static final int TPDU_NORMAL     = 0x00000200;
  public static final int NACK            = 0x00000400;
  public static final int HACK            = 0x00000800;
  public static final int HSACK           = 0x00001000;
  public static final int MACK            = 0x00002000;
  public static final int CGLOCALES       = 0x00004000;
  public static final int ID_SOCKETS_EMISORES   = 0x00008000;
  public static final int TABLA_ASENTIMIENTOS = 0x00020000;
  public static final int VENTANA         = 0x00080000;
  public static final int DATOS_THREAD    = 0x00100000;
  public static final int TPDU_RTX        = 0x00200000;
  public static final int DATOS_USUARIO   = 0x00800000;
  public static final int HNACK           = 0x01000000;
  public static final int ID_SOCKET       = 0x02000000;
  public static final int ID_SOCKETINPUTSTREAM  = 0x03000000;

  private static boolean DEBUG = true;
  private static int NIVEL_DEPURACION = 0;

  //==========================================================================
  /**
   * Constructor.
   * @param ps Un objeto PrintStream.
   */
  Log(PrintStream ps) {
    super(ps);
  }


  //==========================================================================
  /**
   * Método debug
   * @param nivel Nivel de depuración
   * @param mn Nombre del método
   * @param msg Mensaje
   */
  public static void debug(int nivel,String mn,String msg) {

    if(DEBUG)
    {
      //log(null,"Nivel: "+NIVEL_DEPURACION+" nivel:"+nivel);
      if ((NIVEL_DEPURACION & nivel) == nivel)
      {
        log(mn,msg);
      }
    }
  }

  //==========================================================================
  /**
   * Método log
   * @param mn Nombre del método
   * @param msg Mensaje
   */
  public static void log(String mn,String msg) {

   if(mn != null)
     System.out.println("["+mn+"] "+msg);
   else
     System.out.println(msg);

  }


  //==========================================================================
  /**
   * Método log
   * @param mn Nombre del método
   * @param msg Mensaje
   */
  public static void log(int iNivel,String mn,String msg) {

  /*
   switch(iNivel)
   {
    case 0xFFFFFFFF:
      sNivel =  TODO;
      break;
    case 0x00000001:
      sNivel = IDGL;

  case 0x00000002:

  TPDU            =

case 0x00000004:

  TPDU            =

  case 0x00000008:
  TPDU_DATOS      =
  case 0x00000010:
  TEMPORIZADOR    =
  case 0x00000020:
  CGL             =
  case 0x00000040:
  SOCKET          =
  case 0x00000080:
  CANAL_MULTICAST             =

case
  ACK             = 0x00000100;
  TPDU_NORMAL     = 0x00000200;
  NACK            = 0x00000400;
  HACK            = 0x00000800;
  HSACK           = 0x00001000;
  MACK            = 0x00002000;
  CGLOCALES       = 0x00004000;
  ID_SOCKETS_EMISORES   = 0x00008000;
  TABLA_ASENTIMIENTOS = 0x00020000;
  VENTANA         = 0x00080000;
  DATOS_THREAD    = 0x00100000;
  TPDU_RTX        = 0x00200000;
  DATOS_USUARIO   = 0x00800000;
  HNACK           = 0x01000000;
  ID_SOCKET       = 0x02000000;

   }
*/
   if(mn != null)
   {
     System.out.println("["+iNivel+"]"+" {"+mn+"} "+msg);
   }
   else
   {
     System.out.println("["+iNivel+"] "+msg);
   }
  }
  //==========================================================================
  /**
   * Método exit
   * @param int Código de salida
   */
  public static void exit(int codigo) {
    System.exit(-1);
  }

  //==========================================================================
  /**
   * Este método establece el nivel de depuración
   * @param int Nivel de depuración.
   * <UL>
   * <IL>Si se requiere todos los niveles: NIVEL_TODOS </IL>
   * <IL>Cualquier nivel se puede poner como combinación OR a nivel de bits "||"
   *   de cualquier nivel. Por ejemplo: IDGL | DATOS </IL>
   * </UL>
   */
  public static void setNivelDepuracion(int nivel)
  {
     NIVEL_DEPURACION = nivel;

     if (nivel == 0)
     {
       DEBUG = false;
     }
  }

  //==========================================================================
  /**
   * Este método obtiene el nivel de depuración
   * @return Nivel de depuración.
   */
  public static int getNivelDepuracion()
  {
    return NIVEL_DEPURACION;
  }


  //==========================================================================
  /**
   * Este método activa/desactiva la DEPUIRACIÓN
   * @param boolean depuracion
   */
  public static void setDepuracion(boolean depuracion)
  {
    DEBUG = depuracion;
  }


  //==========================================================================
  /**
   * Logging a un fichero. redirecciona la salida estandar y la de error
   * a un fichero.
   * @param f Fichero
   * @param pantalla Indica si la información debe de imprimirse en la pantalla también.
   */
   public static void logToFile(String f, boolean bpantalla) throws IOException {

      // Almacenar los flujos antiguos.
      oldStdout = System.out;
      oldStderr = System.err;

      // Crear/Abrir el fichero.
      logfile = new PrintStream(new BufferedOutputStream( new FileOutputStream(f)));

      if (logfile != null)
      {
       // Comenzar el redireccionamiento.
       System.setOut(new Log(System.out));
       System.setErr(new Log(System.err));

       bPantalla = bpantalla;
      }
    }

  //==========================================================================
  /**
   * Parar el Logging a un fichero. Redirecciona la salida estandar y la de error
   * a sus opciones por defecto.
   */
    public static void reset() {

      System.setOut(oldStdout);
      System.setErr(oldStderr);
      bPantalla = false;
      try
      {

        logfile.close();

      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }

  //==========================================================================
  /**
   * Sobreescribir write de PrintStream.
   */
  public void write(int b) {
    try
    {
      logfile.write(b);

      if (bPantalla)
       oldStdout.write(b);
    }
    catch (Exception e)
    {
       e.printStackTrace();
       setError();
    }
    super.write(b);
  }

  //==========================================================================
  /**
   * Sobreescribir write de PrintStream.
   */
   public void write(byte buf[], int off, int len) {
      try
      {
        logfile.write(buf, off, len);

        if (bPantalla)
         oldStdout.write(buf,off,len);

      }
      catch (Exception e)
      {
            e.printStackTrace();
            setError();
      }

      super.write(buf, off, len);
    }
}
