//============================================================================
//
//	Copyright (c) 1999 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: MFtp.java  1.0 1/12/99
//
// 	Autores: M. Alejandro Garc�a Dom�nguez (malejandrogarcia@wanadoo.es)
//		 Antonio Berrocal Piris
//
//	Descripci�n: Clase MFtp.
//
//
//----------------------------------------------------------------------------


package iacobus.mftp;

import iacobus.ptmf.Log;
import iacobus.ptmf.Address;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.*;

import java.util.Locale;
import java.util.Hashtable;
import java.util.Vector;
import java.net.Socket;
import java.net.URL;

import java.security.PrivateKey;
import java.security.Security;

import java.io.IOException;

/*import iaik.utils.KeyAndCertificate;
import iaik.x509.X509Certificate;
import iaik.security.provider.IAIK;
*/


/**
 *  <b>Aplicacion MFtp, con soporte para
 *  internacionalizaci�n.
 *  Implementa el Protocolo MFtp v1.0
 *  <br> Idiomas soportados por ahora: Espa�ol e Ingl�s.</b>
 *  Implementa las interfaces I18n para Internacionalizacion.
 */

public class MFtp implements I18n
{

  /** Copyright */
  public static String COPYRIGHT = "(C) Copyright 2000 - 2003 M. Alejandro Garc�a Dom�nguez";

  /** Copyright */
  public static String VERSION = "MFtp v1.1";


  /** Sesi�n Multicast */
  private ProtocoloFTPMulticast sesionMulticast = null;

  /** Emisor o Receptor*/
  private boolean bEmisor=false;

  /** Nombre del fichero */
  private String stringFileName = null;

  /** Clave RC2 */
  private char[] clave = null;

  /** Clase FTP */
  private static MFtp ftp = null;


  //=================== Componentes ==========================
  /** Frame principal */
  private static JFramePrincipal jFrame= null;


  /** Label logo */
  private JLabel jLabelLogo= null;

  /** Di�logo Conectar */
  private JDialogConectar jDialogConectar = null;

  //============== Internacionalizaci�n (I18n) ===============
  private static Locale i18nParteDelMundo= null;  	  //para internacionalizaci�n
  private static Hashtable i18nMapaLocal= null;  	  	//para internacionalizaci�n
  private static int i18nIndiceLocal= 0;	  	  			//para internacionalizaci�n



  static final String newline = "\n";

  int idgls = 0;
  int id_sockets = 0;

  //*******Inicializaci�n est�tica de la clase para soporte de I18n******
  static{
	//�En que parte del mundo estamos?
	i18nParteDelMundo = Locale.getDefault();

	//Crear una peque�a base de datos con localidades que soportamos
	i18nMapaLocal	= new Hashtable();
	i18nMapaLocal.put(new Locale("es","ES"),new Integer(0)); 	//Espa�ol/Spanish
	i18nMapaLocal.put(Locale.ENGLISH,new Integer(1));		//Ingl�s/English

	Integer locIndex = (Integer) i18nMapaLocal.get(i18nParteDelMundo);
	if (locIndex == null) { locIndex = new Integer(0); }

	//Esblecer el �ndice
	i18nIndiceLocal = locIndex.intValue();
  }

 //==========================================================================
 /**
  * Constructor gen�rico para un applet y una aplicaci�n.
  * @param unApplet El applet en el navegador o null para una aplicaci�n.
  */
  public MFtp() {
     super();
     this.ftp = this;
     String vers = System.getProperty("java.version");
     if (vers.compareTo("1.1.2") < 0)
     {
       System.out.println(iString(ADVERTENCIA_SWING_VM));
     }
     else
     {
	 iniciar();
     }
  }

 //==========================================================================
 /**
  *  Obtiene una instancia FTP
  */
  static MFtp getFTP(){ return ftp;}


 //==========================================================================




 /**
  * Obtiene la sesi�n Multicast FTP.
  * @return
  */
  ProtocoloFTPMulticast getSesionMulticast()
 {
    return sesionMulticast;
 }


 //==========================================================================
 /**
  * <b> Devuelve una cadena adaptada a la localidad.</b> Esta funci�n se utiliza para dar a la aplicaci�n
  *	un soporte de internacionalizaci�n.
  *	params StringSet Un array de cadenas definido en la interfaz I18n (Internacionalizacion)
  *	return Devuelve una cadena adaptada a la localidad.
  */
  public String iString(String[] StringSet)
	{
		String cadenaLocal;

		try{
			//seleccionar la cadena correcta para esta localidad
			cadenaLocal = StringSet[i18nIndiceLocal];
		}
		catch(IndexOutOfBoundsException e){

			//Usar espa�ol por defecto
			cadenaLocal = StringSet[0];

			//pero imprimir un mensaje de advertencia.
			System.out.println("ERROR DE INTERNACIONALIZACI�N: cadena local \""+cadenaLocal+"\" no encontrada!!");
			System.out.println("(el �ndice era "+i18nIndiceLocal+" )");
		}

		return cadenaLocal;
	}


 //==========================================================================
 /**
  * <b> Devuelve un caracter adaptado a la localidad.</b> Esta funci�n se utiliza para dar a la aplicaci�n
  *	un soporte de internacionalizaci�n.
  *	params StringSet Un array de cadenas definido en la interfaz I18n (Internacionalizacion)
  *	return Devuelve un entero representando al caracter adaptada a la localidad.
  */
  public int iChar(int[] StringSet)
  {
		int charLocal;

		try{
			//seleccionar la cadena correcta para esta localidad
			charLocal = StringSet[i18nIndiceLocal];
		}
		catch(IndexOutOfBoundsException e){

			//Usar espa�ol por defecto
			charLocal = StringSet[0];

			//pero imprimir un mensaje de advertencia.
			System.out.println("ERROR DE INTERNACIONALIZACI�N: char local \""+charLocal+"\" no encontrada!!");
			System.out.println("(el �ndice era "+i18nIndiceLocal+" )");
		}

		return charLocal;
  }

 //==========================================================================
 /**
  * Inicia la interfaz de usuario del applet/aplicaci�n.
  */
  private void iniciar()
  {
		//iniciarListeners();

                getJFrame().show(); //Obtener Frame principal

    /* ANTERIOR

    		if(!esApplet())
		{
	        // Ocultar glassPane y  Mostrar ContenPane
    		getJFrame().getGlassPane().setVisible(false);
			getJFrame().getContentPane().setVisible(true);

		}
		else
			iniciarJApplet();

		getContainer().repaint();
        getJPanelPrincipal().requestDefaultFocus();
      */
 }


 //==========================================================================
 /**
  * Devuelve la propiedad JFrame. Inicia la aplicaci�n.
  *	@return javax.swing.JFrame
  */
  static JFramePrincipal getJFrame() {

		if (jFrame == null)
                {

                   jFrame = new JFramePrincipal();

                   //Inicializa algunos componentes...
                   jFrame.init();

                  //Tama�o y posici�n en la pantalla
                  Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

                  int WIDTH = 750;//screenSize.width*1 / 2; //4/5 del tam�o de la pantalla
                  int HEIGHT = 500;//screenSize.height*1 / 2;

                  jFrame.setSize(WIDTH, HEIGHT);
                  //jFrame.setLocation((screenSize.width/2) - (WIDTH/2),(screenSize.height/2) - (HEIGHT/2));
                  jFrame.pack();

                  //jFrame.setLocation(100,100);
                  jFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));


		}

		return jFrame;
	}




 //==========================================================================
 /**
  * M�todo para obtener im�genes del .jar
  * @param fileName Fichero imagen
  */
 static Image getImage(String fileName)
 {

        //Obtener la im�gen...
        URL url = MFtp.class.getResource("images/" + fileName);
        /*if(this.esApplet())
        {
          Image img = getJApplet().getToolkit().createImage(url);
                  return img;

        }
        else
        {*/
         Image img = getJFrame().getToolkit().createImage(url);
         return img;

        //}

    }

 //==========================================================================
 /**
  * Inserta una cadena de informaci�n en en el panel de informaci�n general
  * @param cadena Cadena Informativa.
  */
  static void insertInformacionString(String cadena)
  {
      insertStringJTextPane(jFrame.jTextPaneInformacion," ","icono_informacion");
      insertStringJTextPane(jFrame.jTextPaneInformacion,cadena+newline,"informacion");
  }


  /**
   * Inserta una cadena de informaci�n en el panel de transmisi�n
   * @param cadena Cadena Informativa.
   */
  static void insertTransmisionString(String cadena,String icono)
  {
      if(icono != null)
      {
        insertStringJTextPane(jFrame.jTextPaneTransmisor," ",icono);
      }
      else
      {
        insertStringJTextPane(jFrame.jTextPaneTransmisor," ","icono_salida");
      }


      insertStringJTextPane(jFrame.jTextPaneTransmisor,cadena+newline,"salida");
  }

/**
   * Inserta una cadena de informaci�n en el panel de transmisi�n
   * @param cadena Cadena Informativa.
   */
  static void insertRecepcionString(String cadena,String icono)
  {
      if(icono != null)
      {
        insertStringJTextPane(jFrame.jTextPaneReceptor," ",icono);
      }
      else
      {
        insertStringJTextPane(jFrame.jTextPaneReceptor," ","icono_entrada");
      }


      insertStringJTextPane(jFrame.jTextPaneReceptor,cadena+newline,"entrada");
  }

 //==========================================================================
 /**
  * Inserta una cadena en el JTextPane
  */
  static void insertStringJTextPane(JTextPane pane, String cadena,String estilo)
  {
     Document doc = pane.getDocument();

     try
     {
         doc.insertString(doc.getLength(), cadena, pane.getStyle(estilo));
         // this.getJScrollPane().getVerticalScrollBar().setValue(this.getJScrollPane().getVerticalScrollBar().getMaximum());
     }
     catch (BadLocationException ble)
     {
         error("No se pudo insertar texto en el panel superior."+newline+"Se recomienda finalizar el programa.");
     }

  }




 //==========================================================================
 /**
  * Devuelve el panel de texto informativo
  * return javax.swing.JPanel
  */
  JTextPane getJTextPaneInformacion()
  {
	return this.jFrame.jTextPaneInformacion;
  }




 //==========================================================================
 /**
  * Cierra la transmisi�n si est� en curso y cancela el thread Multicast.
  */
  void close()
  {
    if (this.sesionMulticast != null)
    {
      this.insertInformacionString("Cerrando la Sesi�n Multicast....");
      this.sesionMulticast.close();
      this.sesionMulticast.stopThread();
      this.insertInformacionString("Desconectado");
      this.sesionMulticast = null;
    }
  }


 //==========================================================================
 /**
  * Devuelve el Di�logo Conectar
  * return JDialogConectar
  */
  JDialogConectar getJDialogConectar()
  {
	 if (jDialogConectar == null)
         {
           try
           {
            	jDialogConectar = new JDialogConectar(null,"Conexi�n Multicast...",true);

           }
	   catch (java.lang.Throwable e)
           {
		handleException(e);
	   }
      	}

        return jDialogConectar;
  }


 //==========================================================================
 /**
 	* Llamada cuando una parte lanza una excepci�n.
 	* @param exception java.lang.Throwable
 	*/
	private void handleException(Throwable exception) {
		 System.out.println("--------- UNCAUGHT EXCEPTION ---------");
		 exception.printStackTrace(System.out);
	}

 //==========================================================================
 /**
  * Indica si el programa est� en modo Emisor o receptor Multicast
	*/
  boolean esEmisor()
  {
    return this.bEmisor;
  }

 //==========================================================================
 /**
 	* Muestra un mensaje de error
  * @param mensaje. El mensaje de Error
 	*/
  static void error(String mensaje)
    {
     /*if(!this.esApplet())
      JOptionPane.showMessageDialog(getJFrame(),mensaje,
				    "ERROR", JOptionPane.ERROR_MESSAGE);
    else*/
      JOptionPane.showMessageDialog(null,mensaje,
				    "ERROR", JOptionPane.ERROR_MESSAGE);

    }


 //==========================================================================
 /**
 	* Inicia los estilos de texto para un JTextPane
 	* @param exception java.lang.Throwable
 	*/
  static void iniciarStylesJTextPane(JTextPane textPane) {

        //Iniciar algunos estilos
        Style def = StyleContext.getDefaultStyleContext().
                                        getStyle(StyleContext.DEFAULT_STYLE);

        Style regular = textPane.addStyle("regular", def);
        StyleConstants.setFontFamily(def, "SansSerif");


        Style s = textPane.addStyle("ID_SOCKET", regular);
        StyleConstants.setBold(s, true);
        StyleConstants.setFontSize(s, 12);
        StyleConstants.setFontFamily(s,"Arial");
        StyleConstants.setForeground(s,Color.black);

        s = textPane.addStyle("icono_presentacion", regular);
        StyleConstants.setAlignment(s, StyleConstants.ALIGN_LEFT);
        StyleConstants.setIcon(s, new ImageIcon(getImage("ptmf.jpg")));

        s = textPane.addStyle("icono_tarea", regular);
        StyleConstants.setAlignment(s, StyleConstants.ALIGN_LEFT);
        StyleConstants.setIcon(s, new ImageIcon(getImage("informacion.gif")));

        s = textPane.addStyle("icono_entrada", regular);
        StyleConstants.setAlignment(s, StyleConstants.ALIGN_LEFT);
        StyleConstants.setIcon(s, new ImageIcon(getImage("flecha_entrada.jpg")));

        s = textPane.addStyle("icono_salida", regular);
        StyleConstants.setAlignment(s, StyleConstants.ALIGN_LEFT);
        StyleConstants.setIcon(s, new ImageIcon(getImage("flecha_salida.jpg")));

        s = textPane.addStyle("icono_informacion", regular);
        StyleConstants.setAlignment(s, StyleConstants.ALIGN_LEFT);
        StyleConstants.setIcon(s, new ImageIcon(getImage("informacion.jpg")));

        s = textPane.addStyle("entrada", regular);
        StyleConstants.setFontSize(s, 12);
        StyleConstants.setFontFamily(s,"Arial");
        StyleConstants.setForeground(s,Color.blue);

        s = textPane.addStyle("salida", regular);
        StyleConstants.setFontSize(s, 12);
        StyleConstants.setFontFamily(s,"Arial");
        StyleConstants.setForeground(s,Color.darkGray);

        s = textPane.addStyle("informacion", regular);
        StyleConstants.setFontSize(s, 12);
        StyleConstants.setFontFamily(s,"Arial");
        StyleConstants.setForeground(s,Color.black);

        s = textPane.addStyle("error", regular);
        StyleConstants.setFontSize(s, 12);
        StyleConstants.setFontFamily(s,"Arial");
        StyleConstants.setForeground(s,Color.red);

        s = textPane.addStyle("presentacion", regular);
        StyleConstants.setFontSize(s, 12);
        StyleConstants.setFontFamily(s,"Arial");
        StyleConstants.setForeground(s,Color.black);
        StyleConstants.setBold(s, true);

    }



 //==========================================================================
 /**
  * Conectar, crea el thread que controla la sesi�n Multicast
  * @param addressIPMulticast Direcci�n IP MUlticast a la que nos conectamos
  * @param addressIPInterfaz Direcci�n IP de la interfaz de salida.
  * @param ttlSesion TTL utilizado en la sesi�n Multicast.
  * @param modo Modo edl protocolo PTMF: FIABLE o NO_FIABLE
  * @param clave Clave RC2
  * @param nickName NickName utilizado en la sesion Chat Multicast
	*/
  void conectar(Address addressIPMulticast,Address addressIPInterfaz,int ttlSesion,
          int modo ,long lRatio,char[] clave, boolean bEmisor)
  {
      this.clave = clave;
      this.bEmisor = bEmisor;


      this.getJFrame().setTitle("PTMF: MFtp - "+addressIPMulticast+" TTL="+ttlSesion);
      this.insertInformacionString("Iniciando sesi�n multicast a "+addressIPMulticast+" TTL="+ttlSesion);

      try
      {
        //Crear Thread Sesion Multicast...
        this.sesionMulticast = new ProtocoloFTPMulticast();
        this.sesionMulticast.conectar(addressIPMulticast,addressIPInterfaz,ttlSesion,lRatio,modo,clave);
      }
      catch(IOException ioe)
      {
        this.error(ioe.toString());
      }
  }

 //==========================================================================
 /**
  * desconectar
  */
  public  void desconectar()
  {
     if (sesionMulticast != null)
       sesionMulticast.stopThread();

     //logoOff();
     this.insertStringJTextPane(getJTextPaneInformacion()," ","icono_informacion");
     this.insertStringJTextPane(getJTextPaneInformacion(),"Se ha cerrado la conexi�n Multicast."+newline,"informacion");

     // reset
     idgls = 0;
     id_sockets = 0;
     //getJLabelIDGLs().setText("IDGLS: "+idgls);
     //getJLabelID_Sockets().setText("ID_Sockets: "+id_sockets);

  }

  void logoOff()
  {
    this.jFrame.logoOff();
  }

  void logoOn()
  {
    this.jFrame.logoOn();
  }

 //==========================================================================
 /**
  * M�todo main de la aplicaci�n.
  */
  public static void main(String s[])
  {
     MFtp ftp = new MFtp();
  }



}