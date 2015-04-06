/**
  
  Copyright (c) 2000-2014 . All Rights Reserved.
  @Autor: Alejandro Garc�a Dom�nguez alejandro.garcia.dominguez@gmail.com   alejandro@iacobus.com
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

package mchat;



//AWT
import java.awt.Container;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.Image;

//AWT.EVENT
import java.awt.event.WindowAdapter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;


//SWING
import javax.swing.*;
import javax.swing.text.*;

import ptmf.Address;
import ptmf.Log;

//UTIL
import java.util.Locale;
import java.util.Hashtable;
import java.util.Vector;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.Security;
import java.io.IOException;
import java.net.URL;
/*import iaik.utils.KeyAndCertificate;
import iaik.x509.X509Certificate;
import iaik.security.provider.IAIK;
*/


/**
 * 	<b>Una Interfaz para mChat, con soporte de
 *  internacionalizaci�n. Idiomas soportados por ahora: Espa�ol e Ingl�s.</b>
 *  Implementa las interfaces I18n y java.awt.event.ActionListener.
 */

public class mChat implements I18n
{

  /** Sesi�n Multicast */
  private ThreadSesionMulticast sesionMulticast = null;

  /** NickName de la sesi�n Multicast */
  private String nickName = null;

  /** Clave RC2 */
  private char[] clave = null;


	//================== Ancho y Alto de frame =================
  private static int INITIAL_WIDTH = 400;							//Ancho panel inicial
  private static int INITIAL_HEIGHT = 100;						//Alto panel inicial

  //=================== Componentes ==========================
  /** Frame principal */
	private JFrame jFrame= null;

  /** Split Principal */
	private JSplitPane jSplitPane= null;

  /** Split Superior */
	private JScrollPane jPanelSplitSuperior= null;

  /** Split Inferior */
	private JScrollPane jPanelSplitInferior= null;

  /** Panel Principal. En el est� el split y el panel de informaci�n */
	private JPanel jPanelPrincipal= null;
  private JPanel jPanelID = null;
  /** Panel inferior */
  private JTextPane jTextPaneInferior = null;

  /** Panel Superior */
  private JTextPane jTextPaneSuperior = null;

  /** Barra de herramientas */
	private JToolBar jToolBar= null;

  /** Panel Informativo inferior */
	private JPanel jPanelInformacion= null;

  /** Label de Informaci�n en el panel de informaci�n.*/
	private JLabel jLabelInformacion= null;
  /** Label logo */
	private JLabel jLabelLogo= null;
  private JLabel jLabelID_Sockets = null;
  private JLabel jLabelIDGLs = null;
  
  /** Bot�n Conectar*/
  private JButton jButtonConectar = null;

  /** Bot�n Desconectar*/
  private JButton jButtonDesconectar = null;

  /** Bot�n Enviar*/
  private JButton jButtonEnviar = null;

  /** Bot�n Limpiar*/
  private JButton jButtonLimpiar = null;


  /** Di�logo Conectar */
  private JDialogConectar jDialogConectar = null;

  //Logos...
  private static ImageIcon ImageIconLogoOn = null;
  private static ImageIcon ImageIconLogoOff = null;


	//============== Internacionalizaci�n (I18n) ===============
	private static Locale i18nParteDelMundo= null;  	  //para internacionalizaci�n
	private static Hashtable i18nMapaLocal= null;  	  	//para internacionalizaci�n
	private static int i18nIndiceLocal= 0;	  	  			//para internacionalizaci�n

	//======================= APPLET ===========================
	private javax.swing.JApplet jApplet=null;		//applet

	//===================== Pantalla Inicial ===================
  private  static int iTotalCarga = 21; 								//n� de componentes a cargar
  private  int iCurrentProgressValue=0;					//componentes cargados
  private  JLabel jLabelProgress = null;				//etiqueta de progreso
  private  JProgressBar jProgressBar = null;		//barra de progreso
	private  JPanel jPanelProgress = null;				//panel de carga inicial

  //======================= MEN�S ============================
  private  JMenuBar jMenuBar= null;							//Barra de Men�s
  private  JMenu jMenuArchivo= null;						//Men� Archivo
	private  JMenuItem jMenuItemCerrar= null;			//Menu-Item Cerrar
	private  JMenu jMenuAyuda= null; 							//Men� Ayuda
	private  JMenuItem jMenuItemAcercaDe= null;		//Menu-Item AcercaDe
	private  JMenu jMenuOpciones= null;						//Men� Opciones
  private  JMenu jMenuEdicion = null;           //Men� Edici�n
  private  JMenuItem jMenuItemCut   = null;     //Men�-Item Cut
  private  JMenuItem jMenuItemCopy  = null;     //Men�-Item Copy
  private  JMenuItem jMenuItemPaste = null;     //Men�-Item Paste
  private  JMenuItem jMenuItemConectar  = null;  //Men�-Item Conectar
  private  JMenuItem jMenuItemDesconectar = null;//Men�-Item desconectar
  private  JMenuItem jMenuItemIDGLs = null;     //Men�-Item IDGLS
  private  JMenuItem jMenuItemID_Sockets = null;     //Men�-Item ID_Sockets
  private  JMenuItem jMenuItemID_SocketsEmisores = null; //Menu-Item ID_SocketEmisores
  private  JRadioButtonMenuItem jRadioMenuItemWindows = null;
  private  JRadioButtonMenuItem jRadioMenuItemMetal = null;
  private  JRadioButtonMenuItem jRadioMenuItemMotif = null;

	//======================= Listeners ========================
  /** ActionListener para jMenuItemCerrar */
	private  ActionListenerCerrar actionListenerCerrar = null;
  /** ActionListener para jMenuItemAcercaDe */
	private  ActionListenerAcercaDe actionListenerAcercaDe = null;
  /** WindowAdapter para jFrame */
	private  WindowAdapter  jFrameCerrarVentana = null;
  /** ActionListener para Conectar */
  private  ActionListenerConectar actionListenerConectar = null;
  /** ActionListener para Desconectar */
  private  ActionListenerDesconectar actionListenerDesconectar = null;
 /** ActionListener para Look_feel */
  private ActionListenerLook_Feel actionListenerLook_Feel = null;


  /** ActionListener para Limpiar */
  private ActionListenerLimpiar actionListenerLimpiar = null;

  /** HashTable de Actions de JTextPane */
  private Hashtable actionsJTextPane = null;


  String newline = "\n";

	//*******Inicializaci�n est�tica de la clase para soporte de I18n******
 	static{
		//�En que parte del mundo estamos?
		i18nParteDelMundo = Locale.getDefault();

		//Crear una peque�a base de datos con localidades que soportamos
		i18nMapaLocal	= new Hashtable();
		i18nMapaLocal.put(new Locale("es","ES"),new Integer(0)); 	//Espa�ol/Spanish
		i18nMapaLocal.put(Locale.ENGLISH,new Integer(1));					//Ingl�s/English

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
	public mChat(javax.swing.JApplet unApplet) {
		 super();
		 jApplet = unApplet;
		 String vers = System.getProperty("java.version");
   	 if (vers.compareTo("1.1.2") < 0) {
       System.out.println(iString(ADVERTENCIA_SWING_VM));
     }
		 else
       iniciar();

  }


 //==========================================================================
 /**
	* Devuelve una interfaz RootPaneContainer para acceso desde un applet como
	* desde una aplicaci�n a los paneles que proporciona JRootPane en JFrame y
	* en JApplet.
	* @return Un objeto que implementa la interfaz RootPaneContainer.
	*/
	private RootPaneContainer getRootPaneContainer(){
		if (jApplet!=null)
			return jApplet ;
		else
			return jFrame;
	}


 //==========================================================================
 /**
  * Devuelve un objeto Container. Se utiliza para proporcionar compatibilidad
  * applet <--> aplicaci�n. El contenedor del applet es la ventana del
  *	navegador y la de la aplicaci�n un JFrame que hay que crear.
	* @return Un objeto Container.
	*/
	private Container getContainer() {
		if (jApplet !=null)
			return jApplet ;
		else
			return jFrame;
	}


 //==========================================================================
 /**
  * Indica si es un applet o una aplicaci�n.
	*		@return true si es un applet.
	*/
	private boolean esApplet() {
	 	if (jApplet !=null)
	 			return true;
	 	else
	 			return false;
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

		iniciarListeners();

    if(!esApplet())
		{
      //Obtener JFrame
    	getJFrame();


		}
		else
			iniciarJApplet();


		getContainer().repaint();
    getJPanelPrincipal().requestDefaultFocus();
	}

 //==========================================================================
 /**
	* Inicia los listeners para escuchar los eventos de los componentes.
	*/
	private void iniciarListeners() {

		//Cerrar presionado:
		actionListenerCerrar	= new ActionListenerCerrar();

		//AcercaDe presionado:
		actionListenerAcercaDe	= new ActionListenerAcercaDe ();

    //Conectar presionado:
		actionListenerConectar	= new ActionListenerConectar();

    //Desconectar presionado:
		actionListenerDesconectar	= new ActionListenerDesconectar();

    //Limpiar presionado:
    actionListenerLimpiar	= new ActionListenerLimpiar();

    //Look&Feel
   actionListenerLook_Feel = new ActionListenerLook_Feel();

    //cerrar la ventana (jFrame)
    jFrameCerrarVentana = new WindowAdapter() {
	    	public void windowClosing(WindowEvent e)
        {
          if (sesionMulticast != null)
          {
            sesionMulticast.stopThread();
          }
          System.exit(0);
        }
		};
 	}

 //==========================================================================
 /**
	* Devuelve la propiedad JFrame. Inicia la aplicaci�n.
	*	@return javax.swing.JFrame
	*/
	private JFrame getJFrame() {

		if (jFrame == null) {
			try {

				jFrame = new javax.swing.JFrame();
				jFrame.setName("mChat");
				jFrame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
				jFrame.setTitle(iString(TITULO_APLICACION));
				jFrame.setResizable(true);
        jFrame.getContentPane().setVisible(false);
				jFrame.setGlassPane(getJPanelProgress());
				jFrame.getGlassPane().setVisible(true);
        //Muestra el Frame para que se vea el glassPane con el panel de progresi�n
        getJFrame().show();
				jFrame.addWindowListener(jFrameCerrarVentana);
	  		jFrame.getContentPane().add(getJPanelPrincipal(),BorderLayout.CENTER);
				jFrame.getContentPane().add(getJToolBar(),BorderLayout.NORTH);

				jFrame.setJMenuBar(getJMenuBar());
				JOptionPane.setRootFrame((JFrame)getContainer());

         //Tama�o y posici�n en la pantalla
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int WIDTH = screenSize.width*4 / 5; //4/5 del tam�o de la pantalla
        int HEIGHT = screenSize.height*4 / 5;

       	//jFrame.setSize(WIDTH, HEIGHT);
    		jFrame.setLocation((screenSize.width/2) - (WIDTH/2),(screenSize.height/2) - (HEIGHT/2));


        // Ocultar glassPane y  Mostrar ContenPane
    		getJFrame().getGlassPane().setVisible(false);
      	getJFrame().getContentPane().setVisible(true);
        jFrame.pack();

        //Split...
        getJSplitPane().setDividerLocation(0.9);

        //jFrame.setLocation(100,100);
      	jFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

			} catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jFrame;
	}

  //==========================================================================
 /**
 	* M�todo para obtener im�genes del .jar
  * @param fileName Fichero imagen
 	*/
 public Image getImage(String fileName)
 {

        //Obtener la im�gen...
        URL url = mChat.class.getResource("images/" + fileName);
        Image img = getJFrame().getToolkit().createImage(url);

        /*MediaTracker tracker = new MediaTracker(cmp);
        tracker.addImage(img, 0);
        try {
            tracker.waitForID(0);
            if (tracker.isErrorAny()) {
                System.out.println("Error cargando imagen");
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        */
        return img;
}

 //==========================================================================
 /**
	* Inicia el Applet.
	*/
	private void iniciarJApplet(){

		if (jApplet != null) {
			try {
				jApplet.getContentPane().setVisible(false);
				jApplet.setGlassPane(getJPanelProgress());
				jApplet.getGlassPane().setVisible(true);
				jApplet.setVisible(true);
				jApplet.getContentPane().add(getJPanelPrincipal(),BorderLayout.CENTER);
				jApplet.getContentPane().add(getJToolBar(),BorderLayout.NORTH);
				jApplet.setJMenuBar(getJMenuBar());
				jApplet.getGlassPane().setVisible(false);
				jApplet.getContentPane().setVisible(true);
				jApplet.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

			} catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
	}


 //==========================================================================
 /**
	* Devuelve la etiqueta de progreso de la carga inicial.
	* @return javax.swing.JLabel
	*/
	private JLabel getJLabelProgress() {
		if (jLabelProgress == null) {
			try {
  			jLabelProgress= new JLabel(iString(CARGANDO));
				jLabelProgress.setAlignmentX(Component.CENTER_ALIGNMENT);
				Dimension d = new Dimension(400, 20);
				jLabelProgress.setMaximumSize(d);
				jLabelProgress.setPreferredSize(d);
				//jLabelProgress.setLabelFor(getJProgressBar());
			}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jLabelProgress;
	}

 //==========================================================================
 /**
	* Devuelve la barra de progreso de la carga inicial.
	* @return javax.swing.JProgressBar
	*/
	private JProgressBar getJProgressBar() {
		if (jProgressBar == null) {
			try {
  			jProgressBar = new JProgressBar(0, iTotalCarga);
				jProgressBar.setStringPainted(true);
				jProgressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
			}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};

		return jProgressBar;
	}


 //==========================================================================
 /**
	* Devuelve el panel de progreso de la carga inicial.
	* @return javax.swing.JPanel
	*/
	private JPanel getJPanelProgress() {
		if (jPanelProgress == null) {
			try {

				jPanelProgress = new JPanel(true);
		   	/* public Insets getInsets() {
					return new Insets(40,30,20,30);
	  		*/
				jPanelProgress.setLayout(new BoxLayout(jPanelProgress, BoxLayout.Y_AXIS));
				getRootPaneContainer().setGlassPane((Component)jPanelProgress);
				getRootPaneContainer().getGlassPane().setVisible(true);
				jPanelProgress.add(getJLabelProgress());
				jPanelProgress.add(Box.createRigidArea(new Dimension(1,20)));
				jPanelProgress.add(getJProgressBar());

				// mostrar contenedor
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

				if(!esApplet())
				{
					jFrame.setSize(INITIAL_WIDTH, INITIAL_HEIGHT);
					jFrame.setLocation(screenSize.width/2 - INITIAL_WIDTH/2,
			  		screenSize.height/2 - INITIAL_HEIGHT/2);
				}
				getContainer().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

	   }
	   catch (java.lang.Throwable e) {
					handleException(e);
		 }
		};
		return jPanelProgress;
	}

 //==========================================================================
 /**
  * Devuelve el panel principal de la aplicaci�n
  * return javax.swing.JPanel
  */
  private JPanel getJPanelPrincipal()
  {
	 	if (jPanelPrincipal== null) {
			try {
        setProgress("Cargango Panel Principal...");
				jPanelPrincipal = new JPanel(new BorderLayout(),true);
		    jPanelPrincipal.add(getJSplitPane(),BorderLayout.CENTER);
				jPanelPrincipal.add(getJPanelInformacion(),BorderLayout.SOUTH);
		
  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jPanelPrincipal;
  }


 //==========================================================================
 /**
  * Devuelve el panel split contenido en el panel principal
  * return javax.swing.JSplitPane
  */
  private JSplitPane getJSplitPane()
  {
	 	if (jSplitPane== null) {
			try {
        setProgress("Cargango Split...");
        jSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, getJPanelSplitSuperior(),getJPanelSplitInferior());
    		jSplitPane.setContinuousLayout(true);
    		jSplitPane.setPreferredSize(new Dimension(600, 400));
    		jSplitPane.setOneTouchExpandable(true);
        jSplitPane.setAutoscrolls(true);

  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jSplitPane;
  }


 //==========================================================================
 /**
  * Devuelve el panel split SUPERIOR contenido en el panel jSplitPanel
  * return javax.swing.JPanel
  */
  private JScrollPane getJPanelSplitSuperior()
  {
	 	if (jPanelSplitSuperior== null) {
			try {
        setProgress("Cargango Split Superior...");
				jPanelSplitSuperior = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS ,
                                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
        jPanelSplitSuperior.setViewportView(getJTextPaneSuperior());
        jPanelSplitSuperior.setPreferredSize(new Dimension(300, 300));
  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jPanelSplitSuperior;
  }


 //==========================================================================
 /**
  * Devuelve el panel split INFERIOR contenido en el panel jSplitPanel
  * return javax.swing.JPanel
  */
  private JScrollPane getJPanelSplitInferior()
  {
	 	if (jPanelSplitInferior == null) {
			try {
        setProgress("Cargango Split Inferior...");
				jPanelSplitInferior = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS ,
                                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
        jPanelSplitInferior.setViewportView(getJTextPaneInferior());
  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jPanelSplitInferior;
  }

 //==========================================================================
 /**
  * Devuelve el JTextPane INFERIOR contenido en el panel Split Inferior
  * return javax.swing.JPanel
  */
  private JTextPane getJTextPaneInferior()
  {
	 	if (jTextPaneInferior == null) {
			try {
        setProgress("Cargango Panel Inferior...");
				jTextPaneInferior = new JTextPane();
        jTextPaneInferior.setAutoscrolls(true);
        crearActionTableJTextPane(jTextPaneInferior);
        jTextPaneInferior.addKeyListener(new ChatKeyListener());
  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jTextPaneInferior;
  }

 //==========================================================================
 /**
  * Devuelve el panel SUPERIOR contenido en el panel Split Superior
  * return javax.swing.JPanel
  */
  private JTextPane getJTextPaneSuperior()
  {
	 	if (jTextPaneSuperior == null) {
			try {
        setProgress("Cargango Panel Superior...");
				jTextPaneSuperior = new JTextPane();
        //jTextPaneSuperior.setPreferredSize(new Dimension(2000,2000));
        //jTextPaneSuperior.setLayout(new ScrollPaneLayout());
        jTextPaneSuperior.setAutoscrolls(true);
        jTextPaneSuperior.setEditable(false);
        iniciarStylesJTextPane(jTextPaneSuperior);
        insertarPresentacion();
        //Prueba
        /*insertStringJTextPaneSuperior(" ","icono_informacion");
        insertStringJTextPaneSuperior("Cadena informativa : \"HOLA :)\""+newline,"informacion");

        insertStringJTextPaneSuperior(" ","icono_entrada");
        insertStringJTextPaneSuperior("CADENA de entrada: Peperr que pasa desde Bilbo...."+newline,"entrada");

        insertStringJTextPaneSuperior(" ","icono_salida");
        insertStringJTextPaneSuperior("CADENA de salida: Adios Manuel"+newline,"salida");
        */
  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jTextPaneSuperior;
  }

 //==========================================================================
 /**
  * Devuelve el panel de informaci�n en el panel jPanelPrincipal
  * return javax.swing.JPanel
  */
  private JPanel getJPanelInformacion()
  {
	 	if (jPanelInformacion== null) {
			try {
        setProgress("Cargango Panel de Informaci�n...");
				jPanelInformacion  = new JPanel(true);
        jPanelInformacion.setLayout(new BorderLayout(0,0));
        jPanelInformacion.add(getJLabelInformacion(),BorderLayout.CENTER);
        //jPanelInformacion.add(Box.createHorizontalGlue());
        jPanelInformacion.add(getJPanelID(),BorderLayout.EAST);
        //jPanelInformacion.setBorder(BorderFactory.createLineBorder(Color.black));
        //jPanelInformacion.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        jPanelInformacion.setBorder(new javax.swing.border.EtchedBorder(
  			                          javax.swing.border.EtchedBorder.LOWERED));
  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jPanelInformacion;
  }

//==========================================================================
 /**
  * Devuelve el panel de informaci�n en el panel jPanelID
  * return javax.swing.JPanel
  */
  private JPanel getJPanelID()
  {
	 	if (jPanelID== null) {
			try {
				jPanelID = new JPanel(true);
        jPanelID.setLayout(new FlowLayout(FlowLayout.RIGHT,0,0));
        jPanelID.add(getJLabelID_Sockets());
        jPanelID.add(getJLabelIDGLs());

       //jPanelInformacion.setBorder(BorderFactory.createLineBorder(Color.black));
        //jPanelInformacion.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        //jPanelID.setBorder(new javax.swing.border.EtchedBorder(
  			//                          javax.swing.border.EtchedBorder.LOWERED));
  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jPanelID;
  }


 //==========================================================================
 /**
  * Establece la cadena informativa de progreso jPanelPrincipal
  */
 private void setProgress(String sInformativa)
 {
   	getJLabelProgress().setText(sInformativa);
	  getJProgressBar().setValue(++iCurrentProgressValue);
 }

 //==========================================================================
 /**
  * Devuelve la etiqueta de informacion jLabelInformacion contenida en jPanelInformacion
  * return javax.swing.JLabel
  */
  JLabel getJLabelInformacion()
  {
	 	if (jLabelInformacion == null) {
      setProgress("Cargango Panel Informaci�n...");
			try {
				jLabelInformacion = new JLabel("Desconectado");
        jLabelInformacion.setPreferredSize(new Dimension(300,20));
       // 	jLabelInformacion.setBorder(new javax.swing.border.EtchedBorder(
  		//	javax.swing.border.EtchedBorder.LOWERED));
         jLabelInformacion.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));


  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jLabelInformacion ;
  }

 //==========================================================================
 /**
  * Devuelve la etiqueta de informacion ID_Sockets
  * return javax.swing.JLabel
  */
  JLabel getJLabelID_Sockets()
  {
	 	if (jLabelID_Sockets == null) {
			try
      {
				jLabelID_Sockets = new JLabel("ID_Sockets: 0");
       	jLabelID_Sockets.setBorder(new javax.swing.border.EtchedBorder(
  			javax.swing.border.EtchedBorder.LOWERED));
   		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jLabelID_Sockets ;
  }

 //==========================================================================
 /**
  * Devuelve la etiqueta de informacion IDGLs
  * return javax.swing.JLabel
  */
  JLabel getJLabelIDGLs()
  {
	 	if (jLabelIDGLs == null) {
			try
      {
				jLabelIDGLs = new JLabel("IDGLs: 0");
       	jLabelIDGLs.setBorder(new javax.swing.border.EtchedBorder(
  			javax.swing.border.EtchedBorder.LOWERED));
  		}
    	catch (java.lang.Throwable e)    {
					handleException(e);
			}
		};
		return jLabelIDGLs;
  }


 //==========================================================================
 /**
  * Establece el texto de la etiqueta LabelInformacion
  * @param cadena Cadena Informativa 
  */
  void setInformacion(String cadena)
  {
    getJLabelInformacion().setText(cadena);
  }

 //==========================================================================
 /**
  * Establece el t�tulo de la ventana
  * @param cadena Cadena Informativa
  */
  void setTitulo(String cadena)
  {
    getJFrame().setTitle(cadena);
  }

 //==========================================================================
 /**
  * Devuelve la barra de men�s de la aplicaci�n.
  * return javax.swing.JMenuBar
  */
  private JMenuBar getJMenuBar()
  {
	 	if (jMenuBar == null) {
			try {
        setProgress("Cargango Barra de Men�s...");
				jMenuBar = new JMenuBar();
				jMenuBar.setName("JMenuBar");
				jMenuBar.getAccessibleContext().setAccessibleName(iString(MENUS));
				jMenuBar.add(getJMenuArchivo());
        jMenuBar.add(getJMenuEdicion());
				jMenuBar.add(getJMenuOpciones());
        jMenuBar.add(Box.createHorizontalGlue());
				jMenuBar.add(getJMenuAyuda());
  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jMenuBar;
  }


 //==========================================================================
 /**
  * Devuelve el men� jMenuArchivo
  * return javax.swing.JMenu
  */
  private JMenu getJMenuArchivo()
  {
	 	if (jMenuArchivo == null) {
			try {
        setProgress("Cargango Men� Archivo...");
       	jMenuArchivo = new JMenu(iString(MENU_ARCHIVO));;
				jMenuArchivo.setName("JMenuArchivo");
				jMenuArchivo.setToolTipText(iString(TOOLTIP_MENU_ARCHIVO));
				jMenuArchivo.setMnemonic(iChar(MNEMONIC_MENU_ARCHIVO));
        jMenuArchivo.add(getJMenuItemConectar());
        jMenuArchivo.add(getJMenuItemDesconectar());
        jMenuArchivo.addSeparator();
				jMenuArchivo.add(getJMenuItemCerrar());
  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jMenuArchivo;
  }

 //==========================================================================
 /**
  * Devuelve el men� jMenuOpciones
  * return javax.swing.JMenu
  */
  private JMenu getJMenuOpciones()
  {
	 	if (jMenuOpciones == null) {
			try {
				jMenuOpciones = new JMenu(iString(MENU_OPCIONES));
				jMenuOpciones.setName("JMenuOpciones");
				jMenuOpciones.setToolTipText(iString(TOOLTIP_MENU_OPCIONES));
				jMenuOpciones.setMnemonic(iChar(MNEMONIC_MENU_OPCIONES));

        //A�adir items
        ButtonGroup group = new ButtonGroup();
        group.add(getJRadioMenuItemMetal());
        group.add(getJRadioMenuItemMotif());
        group.add(getJRadioMenuItemWindows());

        jMenuOpciones.add(getJRadioMenuItemMetal());
        jMenuOpciones.add(getJRadioMenuItemMotif());
        jMenuOpciones.add(getJRadioMenuItemWindows());

        //jMenuOpciones.addSeparator();

  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jMenuOpciones;
  }

 //==========================================================================
 /**
  * Devuelve el men� item jRadioMenuItemMotif
  * return javax.swing.JRadioButtonMenuItem
  */
  private JRadioButtonMenuItem getJRadioMenuItemMotif()
  {
	 	if (jRadioMenuItemMotif == null) {
			try {
				jRadioMenuItemMotif = new JRadioButtonMenuItem("Look&Feel MOTIF");
        jRadioMenuItemMotif.setActionCommand("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
  			//jRadioMenuItemMotif.setName("JMenuItemCerrar");
  			//jRadioMenuItemMotif.setToolTipText(iString(TOOLTIP_MENUITEM_CERRAR));
  			//jRadioMenuItemMotif.setMnemonic(iChar(MNEMONIC_MENUITEM_CERRAR));
 				jRadioMenuItemMotif.addActionListener(actionListenerLook_Feel);
  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jRadioMenuItemMotif;
  }

  //==========================================================================
 /**
  * Devuelve el men� item jRadioMenuItemMetal
  * return javax.swing.JRadioMenuItem
  */
  private JRadioButtonMenuItem getJRadioMenuItemMetal()
  {
	 	if (jRadioMenuItemMetal == null) {
			try {
				jRadioMenuItemMetal = new JRadioButtonMenuItem("Look&Feel METAL");
        jRadioMenuItemMetal.setSelected(true);
        jRadioMenuItemMetal.setActionCommand("javax.swing.plaf.metal.MetalLookAndFeel");
  			//jRadioMenuItemMetal.setName("JMenuItemCerrar");
  			//jRadioMenuItemMetal.setToolTipText(iString(TOOLTIP_MENUITEM_CERRAR));
  			//jRadioMenuItemMetal.setMnemonic(iChar(MNEMONIC_MENUITEM_CERRAR));
 				jRadioMenuItemMetal.addActionListener(actionListenerLook_Feel);
  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jRadioMenuItemMetal;
  }

 //==========================================================================
 /**
  * Devuelve el men� item jRadioMenuItemWindows
  * return javax.swing.JRadioMenuItem
  */
  private JRadioButtonMenuItem getJRadioMenuItemWindows()
  {
	 	if (jRadioMenuItemWindows == null) {
			try {
				jRadioMenuItemWindows= new JRadioButtonMenuItem("Look&Feel WINDOWS");
        jRadioMenuItemWindows.setActionCommand("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");

  			//jRadioMenuItemWindows.setName("JMenuItemCerrar");
  			//jRadioMenuItemWindows.setToolTipText(iString(TOOLTIP_MENUITEM_CERRAR));
  			//jRadioMenuItemWindows.setMnemonic(iChar(MNEMONIC_MENUITEM_CERRAR));
 				jRadioMenuItemWindows.addActionListener(actionListenerLook_Feel);
  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jRadioMenuItemWindows;
  }


 //==========================================================================
 /**
  * Devuelve el men� jMenuEdicion
  * return javax.swing.JMenu
  */
  private JMenu getJMenuEdicion()
  {
	 	if (jMenuEdicion == null) {
			try {
        setProgress("Cargango Men� Edici�n...");
				jMenuEdicion = new JMenu(iString(MENU_EDICION));
       	jMenuEdicion.setName("JMenuEdici�n");
				jMenuEdicion.setToolTipText(iString(TOOLTIP_MENU_EDICION));
				jMenuEdicion.setMnemonic(iChar(MNEMONIC_MENU_EDICION));

        // Insertar Action
        jMenuItemCut   = jMenuEdicion.add(getActionByNameJTextPane(DefaultEditorKit.cutAction));
        jMenuItemCut.setText("Cortar");
        jMenuItemCut.setIcon(new ImageIcon(getImage("cortar.gif")));
        jMenuItemCut.setMnemonic('r');
				jMenuItemCut.setToolTipText("Cortar el texto seleccionado al portapapeles");

        jMenuItemCopy  = jMenuEdicion.add(getActionByNameJTextPane(DefaultEditorKit.copyAction));
        jMenuItemCopy.setText("Copiar");
        jMenuItemCopy.setIcon(new ImageIcon(getImage("copiar.gif")));
        jMenuItemCopy.setMnemonic('C');
				jMenuItemCopy.setToolTipText("Copiar el texto seleccionado al portapapeles");

        jMenuItemPaste = jMenuEdicion.add(getActionByNameJTextPane(DefaultEditorKit.pasteAction));
        jMenuItemPaste.setText("Pegar");
        jMenuItemPaste.setIcon(new ImageIcon(getImage("pegar.gif")));
        jMenuItemPaste.setMnemonic('P');
				jMenuItemPaste.setToolTipText("Pegar texto desde el portapapeles");

  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jMenuEdicion;
  }

 //==========================================================================
 /**
  * Devuelve el men� jMenuAyuda
  * return javax.swing.JMenu
  */
  private JMenu getJMenuAyuda()
  {
	 	if (jMenuAyuda == null) {
			try {
        setProgress("Cargango Men� Ayuda...");
				jMenuAyuda = new JMenu(iString(MENU_AYUDA));
				jMenuAyuda.setName("JMenuAyuda");
				jMenuAyuda.setMnemonic(iChar(MNEMONIC_MENU_AYUDA));
				jMenuAyuda.setToolTipText(iString(TOOLTIP_MENU_AYUDA));
				jMenuAyuda.add(getJMenuItemAcercaDe());
  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jMenuAyuda;
  }

 //==========================================================================
 /**
  * Devuelve el men� item jMenuItemCerrar
  * return javax.swing.JMenuItem
  */
  private JMenuItem getJMenuItemCerrar()
  {
	 	if (jMenuItemCerrar == null) {
			try {
				jMenuItemCerrar = new JMenuItem(iString(MENUITEM_CERRAR));
  			jMenuItemCerrar.setName("JMenuItemCerrar");
  			jMenuItemCerrar.setToolTipText(iString(TOOLTIP_MENUITEM_CERRAR));
  			jMenuItemCerrar.setMnemonic(iChar(MNEMONIC_MENUITEM_CERRAR));
 				jMenuItemCerrar.addActionListener(actionListenerCerrar);
  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jMenuItemCerrar;
  }

 //==========================================================================
 /**
  * Devuelve el men� item jMenuItemID_Sockets
  * return javax.swing.JMenuItem
  */
  private JMenuItem getJMenuItemID_Sockets()
  {
	 	if (jMenuItemID_Sockets == null) {
			try {
				jMenuItemID_Sockets = new JMenuItem("ID_Sockets");
  			jMenuItemID_Sockets.setName("JMenuItemID_Sockets");
  			jMenuItemID_Sockets.setToolTipText("ID_Sockets que pertenecen a nuestro Grupo Local");
  			jMenuItemID_Sockets.setMnemonic('I');
 				//jMenuItemID_Sockets.addActionListener(actionListenerCerrar);
  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jMenuItemID_Sockets;
  }

 //==========================================================================
 /**
  * Devuelve el men� item jMenuItemID_SocketsEmisores
  * return javax.swing.JMenuItem
  */
  private JMenuItem getJMenuItemID_SocketsEmisores()
  {
	 	if (jMenuItemID_SocketsEmisores == null) {
			try {
				jMenuItemID_SocketsEmisores = new JMenuItem("ID_Sockets Emisores");
  			jMenuItemID_SocketsEmisores.setName("jMenuItemID_SocketsEmisores");
  			jMenuItemID_SocketsEmisores.setToolTipText("ID_Sockets que pertenecen a nuestro Grupo Local");
  			jMenuItemID_SocketsEmisores.setMnemonic('E');
 				//jMenuItemID_Sockets.addActionListener(actionListenerCerrar);
  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jMenuItemID_SocketsEmisores;
  }

 //==========================================================================
 /**
  * Devuelve el men� item jMenuItemIDGLs
  * return javax.swing.JMenuItem
  */
  private JMenuItem getJMenuItemIDGLs()
  {
	 	if (jMenuItemIDGLs == null) {
			try {
				jMenuItemIDGLs= new JMenuItem("IDGLs");
  			jMenuItemIDGLs.setName("jMenuItemIDGLs");
  			jMenuItemIDGLs.setToolTipText("IDGL (Identificadores de Grupos Locales) conocidos.");
  			jMenuItemIDGLs.setMnemonic('D');
 				//jMenuItemID_Sockets.addActionListener(actionListenerCerrar);
  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jMenuItemIDGLs;
  }

 //==========================================================================
 /**
  * Devuelve el men� item jMenuItemConectar
  * return javax.swing.JMenuItem
  */
  private JMenuItem getJMenuItemConectar()
  {
	 	if (jMenuItemConectar == null) {
			try {
				jMenuItemConectar = new JMenuItem("Conectar");
  			jMenuItemConectar.setName("JMenuItemConectar");
        jMenuItemConectar.setIcon(new ImageIcon(getImage("conectar.gif")));
        jMenuItemConectar.setDisabledIcon(new ImageIcon(getImage("conectar_inhabilitado.gif")));
        jMenuItemConectar.addActionListener(actionListenerConectar);
        jMenuItemConectar.setToolTipText("Conectar a una sesi�n Multicast");
  			jMenuItemConectar.setMnemonic('o');
  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jMenuItemConectar;
  }

 //==========================================================================
 /**
  * Devuelve el men� item jMenuItemDesconectar
  * return javax.swing.JMenuItem
  */
  private JMenuItem getJMenuItemDesconectar()
  {
	 	if (jMenuItemDesconectar == null) {
			try {
				jMenuItemDesconectar = new JMenuItem("Desconectar");
  			jMenuItemDesconectar.setName("JMenuItemDesconectar");
        jMenuItemDesconectar.setIcon(new ImageIcon(getImage("desconectar.gif")));
        jMenuItemDesconectar.addActionListener(actionListenerDesconectar);
        jMenuItemDesconectar.setToolTipText("Desconectar de la sesi�n Multicast");
  			jMenuItemDesconectar.setMnemonic('D');
  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jMenuItemDesconectar;
  }

 //==========================================================================
 /**
  * Devuelve el men� item jMenuItemAcercaDe
  * return javax.swing.JMenuItem
  */
  private JMenuItem getJMenuItemAcercaDe()
  {
	 	if (jMenuItemAcercaDe == null) {
			try {
				jMenuItemAcercaDe  = new JMenuItem(iString(MENUITEM_ACERCA_DE));
				jMenuItemAcercaDe.setName("JMenuItemAcercaDe");
  			jMenuItemAcercaDe.setToolTipText(iString(TOOLTIP_MENUITEM_ACERCA_DE));
  			jMenuItemAcercaDe.setMnemonic(iChar(MNEMONIC_MENUITEM_ACERCA_DE));
 				jMenuItemAcercaDe.addActionListener(actionListenerAcercaDe);
  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jMenuItemAcercaDe;
  }

 //=========================================================================
 /**
  * Crea la tabla de acciones de un JTextComponent
  */
  private void crearActionTableJTextPane(JTextComponent textComponent) {
    actionsJTextPane = new Hashtable();
    Action[] actionsArray = textComponent.getActions();
    for (int i = 0; i < actionsArray.length; i++) {
        Action a = actionsArray[i];
        actionsJTextPane.put(a.getValue(Action.NAME), a);
    }
  }

 //=========================================================================
 /**
  * Obtiene Acciones de un JTextComponent a partir del nombre.
  * @return Action
  */
  private Action getActionByNameJTextPane(String name) {
    return (Action)(actionsJTextPane.get(name));
  }



 //==========================================================================
 /**
  * Devuelve la barra de herramientas de la aplicaci�n
  * return javax.swing.JToolBar
  */
  private JToolBar getJToolBar()
  {
	 	if (jToolBar == null) {
			try {
        setProgress("Cargango Barra de Herramientas...");
				jToolBar = new JToolBar();
				jToolBar.setFloatable(true);
				jToolBar.add(this.getJButtonConectar());
				jToolBar.add(this.getJButtonDesconectar());
        jToolBar.addSeparator();
				jToolBar.add(this.getJButtonLimpiar());
        jToolBar.add(Box.createHorizontalGlue());
				jToolBar.add(this.getJLabelLogo());

  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jToolBar;
  }

 //==========================================================================
 /**
  * Devuelve el bot�n Conectar
  * return javax.swing.JButton
  */
  private JButton getJButtonConectar()
  {
	 	if (jButtonConectar == null) {
			try {
        setProgress("Cargango Bot�n Conectar...");
				jButtonConectar = new JButton("Conectar");
        jButtonConectar.setIcon(new ImageIcon(getImage("conectar.gif")));
        jButtonConectar.setDisabledIcon(new ImageIcon(getImage("conectar_inhabilitado.gif")));
        jButtonConectar.addActionListener(actionListenerConectar);
        jButtonConectar.setToolTipText("Conectar a una sesi�n Multicast");

      }
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jButtonConectar;
  }


 //==========================================================================
 /**
  * Devuelve el bot�n Desconectar
  * return javax.swing.JButton
  */
  private JButton getJButtonDesconectar()
  {
	 	if (jButtonDesconectar == null) {
			try {
        setProgress("Cargango Bot�n Desconectar...");
				jButtonDesconectar = new JButton("Desconectar");
        jButtonDesconectar.setIcon(new ImageIcon(getImage("desconectar.gif")));
        jButtonDesconectar.addActionListener(actionListenerDesconectar);
        jButtonDesconectar.setToolTipText("Desconectar de la sesi�n Multicast");
  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		}
		return jButtonDesconectar;
  }

 //==========================================================================
 /**
  * Devuelve el bot�n Enviar
  * return javax.swing.JButton
  */
  private JButton getJButtonEnviar()
  {
	 	if (jButtonEnviar == null) {
			try {
        setProgress("Cargango Bot�n Enviar...");
				jButtonEnviar = new JButton("Enviar");
  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jButtonEnviar;
  }


 //==========================================================================
 /**
  * Devuelve el bot�n Limpiar
  * return javax.swing.JButton
  */
  private JButton getJButtonLimpiar()
  {
	 	if (jButtonLimpiar == null) {
			try {
        setProgress("Cargango Bot�n Limpiar...");
				jButtonLimpiar = new JButton("Limpiar");
        jButtonLimpiar.addActionListener(actionListenerLimpiar);
  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jButtonLimpiar;
  }

 //==========================================================================
 /**
  * Devuelve el bot�n Logo
  * return javax.swing.JButton
  */
  private JLabel getJLabelLogo()
  {
	 	if (jLabelLogo == null) {
			try
      {
        setProgress("Cargango Logo...");
				jLabelLogo = new JLabel();

        //Cargar im�genes...
        ImageIconLogoOff = new ImageIcon(getImage("logo.jpg"));
        ImageIconLogoOn = new ImageIcon(getImage("logo.gif"));

        logoOff();
    		jLabelLogo.setAlignmentX(Component.RIGHT_ALIGNMENT);
        jLabelLogo.setToolTipText("Protocolo de Transporte Multicast Fiable (PTMF).");
  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
		return jLabelLogo;
  }

 //==========================================================================
 /**
  * Inicia la animaci�n....
  */
  void logoOn()
  {
    getJLabelLogo().setIcon(ImageIconLogoOn);
    getJButtonConectar().setEnabled(false);
    getJMenuItemConectar().setEnabled(false);
    getJButtonDesconectar().setEnabled(true);
    getJMenuItemDesconectar().setEnabled(true);

  }

 //==========================================================================
 /**
  * Finaliza la animaci�n....
  */
  void logoOff()
  {
    getJLabelLogo().setIcon(ImageIconLogoOff);
    getJButtonConectar().setEnabled(true);
    getJMenuItemConectar().setEnabled(true);
    getJButtonDesconectar().setEnabled(false);
    getJMenuItemDesconectar().setEnabled(false);

  }

 //==========================================================================
 /**
  * Devuelve el Di�logo Conectar
  * return JDialogConectar
  */
  private JDialogConectar getJDialogConectar()
  {
	 	if (jDialogConectar == null) {
			try {
				jDialogConectar = new JDialogConectar(this,getJFrame(),"Conexi�n Multicast...",true);
  		}
			catch (java.lang.Throwable e) {
					handleException(e);
			}
		};
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
 	* Muestra un mensaje de error
  * @param mensaje. El mensaje de Error
 	*/
  void error(String mensaje)
    {
      JOptionPane.showMessageDialog(getJFrame(),mensaje,
				    "ERROR", JOptionPane.ERROR_MESSAGE);
    }


 //==========================================================================
 /**
 	* Inserta una cadena en el JTextPaneSuperior
 	*/
  void insertStringJTextPaneSuperior(String cadena,String estilo)
  {
     Document doc = getJTextPaneSuperior().getDocument();

     try {
         doc.insertString(doc.getLength(), cadena,
                                 getJTextPaneSuperior().getStyle(estilo));
        this.getJPanelSplitSuperior().getVerticalScrollBar().setValue(this.getJPanelSplitSuperior().getVerticalScrollBar().getMaximum());

     } catch (BadLocationException ble) {
       error("No se pudo insertar texto en el panel superior."+newline+"Se recomienda finalizar el programa.");
     }

  }


 //==========================================================================
 /**
 	* Inicia los estilos de texto para un JTextPane
 	* @param exception java.lang.Throwable
 	*/
  private void iniciarStylesJTextPane(JTextPane textPane) {

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
        StyleConstants.setForeground(s,Color.green);

        s = textPane.addStyle("informacion", regular);
        StyleConstants.setFontSize(s, 12);
        StyleConstants.setFontFamily(s,"Arial");
        StyleConstants.setForeground(s,Color.black);


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
  void conectar(Address addressIPMulticast,
            String sIPMulticast,
            Address addressIPInterfaz,int ttlSesion,
            int modo, String nickname,char[] clave)
  {
      this.clave = clave;
      this.nickName = nickname;
      this.getJFrame().setTitle("PTMF: mChat - "+sIPMulticast+":"+ttlSesion);
      this.getJLabelInformacion().setText("Iniciando sesi�n multicast a "+sIPMulticast+":"+ttlSesion);
      getJTextPaneSuperior().setText("");
      insertarPresentacion();
      insertStringJTextPaneSuperior(" ","icono_informacion");
      insertStringJTextPaneSuperior("Iniciando sesi�n multicast a "+sIPMulticast+":"+ttlSesion+newline,"informacion");

      //Crear Thread Sesion Multicast...
      this.sesionMulticast = new ThreadSesionMulticast(this);
      this.sesionMulticast.conectar(addressIPMulticast,addressIPInterfaz,ttlSesion,modo,clave);
  }


 //==========================================================================
 /**
	* Desconectar
	*/
	void desconectar()
  {
     if (sesionMulticast!= null)
       sesionMulticast.stopThread();
     getJTextPaneSuperior().setText("");
     insertarPresentacion();
     logoOff();
     insertStringJTextPaneSuperior(" ","icono_informacion");
     insertStringJTextPaneSuperior("Se ha cerrado la conexi�n Multicast."+newline,"informacion");
     setInformacion("Desconectado");
     setTitulo("Desconectado");
	}

 //==========================================================================
 /**
  * Dialogo con mensaje informativo
  * @param mensaje Mensaje de Error
  */
  void mensajeInformativo(String mensaje)
  {
    JOptionPane.showMessageDialog(getJFrame(),mensaje,
				    "Informaci�n", JOptionPane.INFORMATION_MESSAGE);
  }


 //==========================================================================
 /**
  * Inserta el logotipo de PTMF en el panel de texto.
  */
  private void insertarPresentacion()
  {
    insertStringJTextPaneSuperior(" ","icono_presentacion");
    insertStringJTextPaneSuperior(newline+iString(PRESENTACION)+newline+newline,"presentacion");
  }

 //==========================================================================
 /**
	* El m�todo main de la aplicaci�n.
	*/
	public static void main(String s[]) {
		mChat chat = new mChat(null);
	}



 //==========================================================================
 /**
  * Clase interna ActionListenerAcercaDe implementa ActionListener
  */
 class ActionListenerAcercaDe implements ActionListener {
		   	public void actionPerformed(ActionEvent e)
    		{
			    JOptionPane.showMessageDialog
          (getContainer(), iString(ABOUT),
				    "Acerca de...", JOptionPane.INFORMATION_MESSAGE,
            new ImageIcon(getImage("ptmf.jpg")));

    		}
 }

 //==========================================================================
 /**
  * Clase interna ActionListenerConectar implementa ActionListener
  */
 class ActionListenerConectar implements ActionListener
 {
		   	public void actionPerformed(ActionEvent e)
    		{
         if ((sesionMulticast == null) || (!sesionMulticast.esActiva()))
         {
             getJDialogConectar().show();
         }
         else
       	   mensajeInformativo("Ya hay una conexi�n abierta."+newline+"si desea abrir otra cierre la anterior antes.");

    		}
 }


 //==========================================================================
 /**
  * Clase interna ActionListenerDesconectar implementa ActionListener
  */
 class ActionListenerDesconectar implements ActionListener
 {
		   	public void actionPerformed(ActionEvent e)
    		{
          desconectar();
    		}
 }


 //==========================================================================
 /**
  * Clase interna ActionListenerCerrar implementa ActionListener
  */
 class ActionListenerCerrar implements ActionListener
 {
		   	public void actionPerformed(ActionEvent e)
    		{
    			if (esApplet())
	  				jApplet.stop();
	  			else
	  				System.exit(0);
    		}
 }


//==========================================================================
 /**
  * Clase interna ActionListenerLimpiar implementa ActionListener
  */
 class ActionListenerLimpiar implements ActionListener
 {
		   	public void actionPerformed(ActionEvent e)
    		{
          if(sesionMulticast!= null && sesionMulticast.esActiva())
      			getJTextPaneSuperior().setText(" ");
          else
          {
            getJTextPaneSuperior().setText(" ");
            insertarPresentacion();
          }
    		}
 }

  //==========================================================================
 /**
  * Clase interna ActionListenerLook_Feel implementa ActionListener
  */
 class ActionListenerLook_Feel implements ActionListener
 {
      public void actionPerformed(ActionEvent e)
      {
         try
         {
          //FALTA SABER QUE LOOK_FEEL PONER...
          UIManager.setLookAndFeel(e.getActionCommand());
          SwingUtilities.updateComponentTreeUI(getJFrame());
          getJFrame().pack();
         }
         catch(javax.swing.UnsupportedLookAndFeelException ue)
         {
            error(ue.toString());
         }
         catch(java.lang.IllegalAccessException ue)
         {
            error(ue.toString());
         }
         catch(java.lang.InstantiationException ue)
         {
            error(ue.toString());
         }
         catch(java.lang.ClassNotFoundException ue)
         {
            error(ue.toString());
         }

      }
}

 //==========================================================================
 /**
  * Clase interna KeyListener extiende KeyAdapter
  */
 class ChatKeyListener extends KeyAdapter
 {
      String cad = null;

		   	public void keyReleased(KeyEvent e)
        {
         if(e.getKeyCode() == KeyEvent.VK_ENTER)
         {
           try
           {
            if (!sesionMulticast.esActiva())
              return;
            cad = getJTextPaneInferior().getText();
            //Log.log("cad: "+cad,"");

            sesionMulticast.sendString(" ["+nickName+"] "+cad);
            //Log.log("Cadena,longitud: "+cad.length(),"");
            getJTextPaneInferior().setText(" ");
            insertStringJTextPaneSuperior(" ","icono_salida");
            insertStringJTextPaneSuperior(cad,"salida");
           }
           catch(IOException ioe)
           {
             error("La conexi�n se ha cerrado");
             desconectar();
           }
         }
        }
 }

}
