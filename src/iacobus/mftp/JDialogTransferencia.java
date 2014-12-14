/**
  Fichero: JDialogTransferencia.java  1.0 1/12/99
   
  Copyright (c) 2000-2014 . All Rights Reserved.
  Autor: Alejandro Garc�a Dom�nguez alejandro.garcia.dominguez@gmail.com   alejandro@iacobus.com
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

import iacobus.ptmf.Log;
import iacobus.ptmf.Address;
import iacobus.ptmf.PTMF;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.lang.Math;

 //==========================================================================
 /**
  * Clase JDialogTransferencia.
  * Implementa el di�logo de transferencia multicast.
  * Utiliza un Timer para medir tiempos
  */
 public class JDialogTransferencia extends JDialog
 {

  /** Clase ThreadSesionMulticast */
  //ThreadSesionMulticast sesionMulticast = null;

  /** Direcci�n IP MUlticast */
  private Address addressIPMulticast = null;

  /** Direcci�n IP de la Interfaz de salida */
  private Address addressIPInterfaz = null;

  /** TTL de la sesi�n */
  private Integer TTLSesion = null;

  /** Clave RC2 */
  private char[] charClaveRC2 = null;

  /** Nombre del fichero */
  private String sFileName = null;

  /** Tama�o del Fichero en bytes */
  private long lFileSize = 0;

  /** Bytes transmitidos */
  private long lBytesTransmitidos = 0;

  /** Bytes Restantes */
  private long lBytesRestantes = 0;

  /** Tiempo */
  private long lTiempo = 0;
  private long lTiempo_Inicial = 0;

  /** KB/seg */
  private double dKB_seg = 0;

  /** Icono del fichero a transferir */
  private Icon icon = null;


  //Paneles
  private JPanel panelComponente = null;
  private JPanel panelBotones = null;
  private JPanel panel1 = null;
  private JPanel panel3 = null;
  private JPanel panel2 = null;
  private JPanel panel5 = null;
  private JPanel panel4 = null;

  //Botones
  private JButton jButtonCancelar = null;

  //Etiquetas
  private JLabel jLabelIPMulticast= null;
  private JLabel jLabelTTLSesion = null;
  private JLabel jLabelFileName = null;
  private JLabel jLabelPuerto = null;
  private JLabel jLabelFileSize = null;
  private JLabel jLabelBytesTransmitidos = null;
  private JLabel jLabelBytesRestantes = null;
  private JLabel jLabelKseg = null;
  private JLabel jLabelLogo = null;
  private JLabel jLabelTiempo = null;
  private JLabel jLabelTiempoRestante = null;
  private JLabel jLabelIcon = null;

  //ProgressBar
  private JProgressBar jProgressBar = null;
  // ActionListeners
  private ActionListenerCancelar actionListenerCancelar = null;

  //Timer
  private Timer timer = null;

  /** FileEmision */
  private FileEmision fileEmision =  null;

 //==========================================================================
 /**
  * Constructor. Construye un JDialog.
  * @param frame El frame padre
  * @param title El t�tulo del di�logo
  * @param modal Boolean que especifica si el di�logo es modal o no
  */
 public JDialogTransferencia(FileEmision fileEmision,Frame frame, String title,
       boolean modal,String sFileName, long lFilesize,Icon icon) {
    super(frame, title, modal);
    try  {

      //this.sesionMulticast = sesionMulticast;
      this.fileEmision = fileEmision;
      this.sFileName = sFileName;
      this.lFileSize = lFilesize;
      this.icon = icon;
     // this.sFileName = "Luis perez.mp3";
     // this.iFileSize = 100000;

      Init();
      pack();
      Dimension	screenSize = Toolkit.getDefaultToolkit().getScreenSize();
     	setLocation(new Point(screenSize.width/2 - this.getWidth()/2, screenSize.height/2 - this.getHeight()/2));


    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }


 //==========================================================================
 /**
  * M�todo de inicio.
  * @exception Exception si hay un error en la creaci�n del di�logo.
  */
  private void Init() throws Exception {


    // A�adir paneles al di�logo
    getContentPane().add(getpanelComponente(),BorderLayout.CENTER);
    getContentPane().add(getpanelBotones(),BorderLayout.SOUTH);
    setResizable(false);

    //Obtener Tiempo...
    lTiempo_Inicial = System.currentTimeMillis();

    //Establer timer
    timer = new Timer(400,new ActionListener(){
                public void actionPerformed(ActionEvent evento)
                {
                  actualizar();
                }
              });

    //Lanzar el timer
    timer.start();
  }

  //=========================================================================
  /**
   * Actualizar Tiempo y Tama�o....
   */
   private void actualizar()
   {
      long lHoras = 0;
      long lMinutos = 0;
      long lSegundos = 0;

      //Calcular Tiempo en segundos
      lTiempo = (System.currentTimeMillis() - lTiempo_Inicial);

      //Calcular Horas
      lHoras = ((lTiempo/1000)/60)/60;
      lMinutos = ((lTiempo/1000)/60)%60;
      lSegundos = ((lTiempo/1000)%60);
      //Log.log("DEPU","Tiempo> "+lTiempo+"  "+lHoras+":"+lMinutos+":"+lSegundos);

      if (lMinutos <0 ) lMinutos = 0;
      if (lSegundos <0) lSegundos = 0;
      if (lBytesRestantes < 0) lBytesRestantes = 0;

      //Establecer el tiempo.....
      if(lHoras > 0)
        getjLabelTiempo().setText("Time> "+lHoras+" hr. "+lMinutos+" min.");
      else if(lMinutos > 0)
        getjLabelTiempo().setText("Time> "+lMinutos+" min. "+lSegundos+" seg.");
      else
        getjLabelTiempo().setText("Time> "+lSegundos+" seg.");

      //Actualizar KB/seg
       //Actualizar KB/seg
      this.dKB_seg = ((double)(lBytesTransmitidos)/(double)(lTiempo)) *1000;
      this.dKB_seg = (this.dKB_seg / 1024);
      //Log.log("\n\n KB_SEG: "+this.dKB_seg,"----------------------------------");



      //Tama�o....
      getjLabelBytesTransmitidos().setText("Transmit: "+this.lBytesTransmitidos);
      getjLabelBytesRestantes().setText("Remain: "+this.lBytesRestantes);

      //Tiempo restante....
      if(lBytesTransmitidos <= 0)
        lBytesTransmitidos = 1;
      long lTiempoRestante = lTiempo - ((lTiempo * lFileSize)/lBytesTransmitidos);
      //Calcular Horas
      lHoras = -((lTiempoRestante/1000)/60)/60;
      lMinutos = -((lTiempoRestante/1000)/60)%60;
      lSegundos = -((lTiempoRestante/1000)%60);


      if (lMinutos <0 ) lMinutos = 0;
      if (lSegundos <0) lSegundos = 0;

      if(lHoras > 0)
        getjLabelTiempoRestante().setText("T. Remain> "+lHoras+" hr. "+lMinutos+" min.");
      else if(lMinutos > 0)
        getjLabelTiempoRestante().setText("T. Remain> "+lMinutos+" min. "+lSegundos+" seg.");
      else
        getjLabelTiempoRestante().setText("T. Remain> "+lSegundos+" seg.");

      //Barra de Progreso...
      if(lFileSize <= 0)
        lFileSize = 1;
      int iPorcentaje = (int)((lBytesTransmitidos*100)/lFileSize);
      getJProgressBar().setValue(iPorcentaje);

      //KB/seg
      if (dKB_seg > 1)
      {
        int iParteEntera = (int)(dKB_seg );
        int iParteDecimal = (int)(dKB_seg *100)%100;
        this.getJLabelKseg().setText("KB/seg: "+iParteEntera+"."+iParteDecimal);
      }
      else
      {
        int i = (int)(dKB_seg * 100);
        this.getJLabelKseg().setText("KB/seg: 0."+i);
      }

      //Log.log("\n\n bytes trasmitidos: "+lBytesTransmitidos+"  ltiempo: "+lTiempo ,"*_*_*_*_*_*_*_*_*_*_*_*");
  }

  //=========================================================================
  /**
   * Establecer bytes transmitidos
   */
   public void setBytesTransmitidos(long lBytes)
   {
      this.lBytesTransmitidos = lBytes;
      this.lBytesRestantes  = this.lFileSize - this.lBytesTransmitidos;
   }

  //==========================================================================
  /**
   * Obtiene panelComponente
   * @return JPanel
   */
  private JPanel getpanelComponente()
   {
    if (panelComponente == null)
    {
      panelComponente = new JPanel();
      panelComponente.setLayout(new BoxLayout(panelComponente,BoxLayout.Y_AXIS));
      panelComponente.setBorder(new CompoundBorder(BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(5,5,5,5)));

      // A�adir
      panelComponente.add(getpanel1());
      panelComponente.add(getpanel2());
      panelComponente.add(getpanel4());
      panelComponente.add(getpanel3());
      panelComponente.add(getpanel5());

    }

    return panelComponente;
   }


  //==========================================================================
  /**
   * Obtiene panelBotones
   * @return JPanel
   */
  private JPanel getpanelBotones()
  {
    if (panelBotones == null)
    {
      panelBotones = new JPanel(true);
      panelBotones.setLayout(new FlowLayout(FlowLayout.RIGHT));

      // A�adir botones
      //panelBotones.add(getJLabelKseg());
      //panelBotones.add(Box.createHorizontalGlue());
      panelBotones.add(getjButtonCancelar());
    }
    return panelBotones;
  }


 //==========================================================================
 /**
	* Devuelve la barra de progreso
	* @return javax.swing.JProgressBar
	*/
	private JProgressBar getJProgressBar() {
		if (jProgressBar == null) {
				jProgressBar = new JProgressBar(0,100);
				jProgressBar.setStringPainted(true);
				jProgressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        jProgressBar.setPreferredSize(new Dimension(400,20));
		}
		return jProgressBar;
	}

  //==========================================================================
  /**
   * Obtiene panel1
   * @return JPanel
   */
   private JPanel getpanel1()
   {
      if (panel1 == null)
      {
        panel1 = new JPanel(true);
        panel1.setLayout(new BorderLayout());

        // A�adir
        //panel1.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel1.add(getjLabelIcon(),BorderLayout.WEST);
        panel1.add(getjLabelFileName(),BorderLayout.CENTER);
        //panel1.add(getJLabelLogo(),BorderLayout.EAST);
      }

      return panel1;
   }

  //==========================================================================
  /**
   * Obtiene panel1
   * @return JPanel
   */
   public  void setVisible(boolean bVisible)
   {
      if(bVisible == false && timer!=null)
        this.timer.stop();
      super.setVisible(bVisible);

       //Calcular Tiempo FINAL en segundos
      lTiempo = (System.currentTimeMillis() - lTiempo_Inicial);

      //Actualizar KB/seg
      this.dKB_seg = ((double)(lBytesTransmitidos * 1000)/(double)(lTiempo));
      this.dKB_seg = (this.dKB_seg / 1024);
      //Log.log("\n\n KB_SEG: "+this.dKB_seg,"----------------------------------");


   }

  //==========================================================================
  /**
   * Obtiene panel2
   * @return JPanel
   */
   private JPanel getpanel2()
   {
      if (panel2 == null)
      {
        panel2 = new JPanel(true);
        panel2.setLayout(new GridLayout(1,3));
        //panel2.setBorder(BorderFactory.createEmptyBorder(5,0,5,5));
        //panel2.setAlignmentX(Component.LEFT_ALIGNMENT);

        // A�adir
        panel2.add(this.getjLabelFileSize());
        panel2.add(this.getjLabelBytesTransmitidos());
        panel2.add(this.getjLabelBytesRestantes());
      }

      return panel2;
   }

  //==========================================================================
  /**
   * Obtiene panel3
   * @return JPanel
   */
   private JPanel getpanel3()
   {
      if (panel3 == null)
      {
        panel3 = new JPanel(true);
        panel3.setLayout(new BorderLayout());
        //panel3.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        //panel3.setAlignmentX(Component.LEFT_ALIGNMENT);

        // A�adir
        panel3.add(getJLabelKseg(),BorderLayout.WEST);
     }

      return panel3;
   }

 //==========================================================================
 /**
  * Devuelve el label Logo
  * return javax.swing.JButton
  */
  private JLabel getJLabelLogo()
  {
	 	if (jLabelLogo == null) {
 				jLabelLogo = new JLabel();

        jLabelLogo.setIcon(new ImageIcon(mFtp.getFTP().getImage("bola.gif")));

        jLabelLogo.setAlignmentX(Component.RIGHT_ALIGNMENT);
        jLabelLogo.setToolTipText("Power Transport Multicast Framework (PTMF).");

		}
		return jLabelLogo;
  }

  //==========================================================================
  /**
   * Obtiene panel4
   * @return JPanel
   */
   private JPanel getpanel4()
   {
      if (panel4 == null)
      {
        panel4 = new JPanel(true);
        panel4.setLayout(new GridLayout(1,3));
        //panel4.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        //panel4.setAlignmentX(Component.LEFT_ALIGNMENT);

        // A�adir
        panel4.add(getjLabelTiempo());
        panel4.add(getjLabelTiempoRestante());
        panel4.add(new JLabel(" "));
       }

      return panel4;
   }


 //==========================================================================
  /**
   * Obtiene panel5
   * @return JPanel
   */
   private JPanel getpanel5()
   {
      if (panel5 == null)
      {
        panel5 = new JPanel(true);
        panel5.setLayout(new BorderLayout());
        //panel5.setBorder(new TitledBorder(new CompoundBorder(BorderFactory.createEtchedBorder(),
        //        BorderFactory.createEmptyBorder(0,0,0,0)),"Canal seguro:"));

        /*panel5.setBorder(new CompoundBorder(BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(0,3,0,0)));
        */
        // A�adir
        //panel4.add(Box.createRigidArea(new Dimension(5,5)));
        panel5.add(getJProgressBar(),BorderLayout.CENTER);

      }

      return panel5;
   }



  //==========================================================================
  /**
   * Obtiene jButtonCancelar
   * @return JButton
   */
   private JButton getjButtonCancelar()
   {
      if (jButtonCancelar == null)
      {
        jButtonCancelar = new JButton("Cancelar");
        jButtonCancelar.addActionListener(getactionListenerCancelar());
        jButtonCancelar.setIcon(new ImageIcon(mFtp.getFTP().getImage("desconectar.gif")));
      }

       return jButtonCancelar;
   }


  //==========================================================================
  /**
   * Obtiene jLabelIPMulticast
   * @return JLabel
   */
   private JLabel getjLabelIPMulticast()
   {
      if (jLabelIPMulticast == null)
      {
         jLabelIPMulticast = new JLabel("IP Multicast:");
         jLabelIPMulticast.setAlignmentX(Component.LEFT_ALIGNMENT);
      }
       return jLabelIPMulticast;
   }

  //==========================================================================
  /**
   * Obtiene jLabelFile
   * @return JLabel
   */
   private JLabel getjLabelFileName()
   {
      if (jLabelFileName == null)
      {
         jLabelFileName = new JLabel("File: "+this.sFileName);
         jLabelFileName.setAlignmentX(Component.LEFT_ALIGNMENT);
      }
       return jLabelFileName;
   }

 //==========================================================================
  /**
   * Obtiene jLabelIcon
   * @return JLabel
   */
   private JLabel getjLabelIcon()
   {
      if (jLabelIcon == null)
      {
         if (this.icon == null)
            jLabelIcon = new JLabel("");
         else
            jLabelIcon = new JLabel(this.icon);
         jLabelIcon.setAlignmentX(Component.LEFT_ALIGNMENT);
      }
       return jLabelIcon;
   }

  //==========================================================================
  /**
   * Obtiene jLabelKseg
   * @return JLabel
   */
   private JLabel getJLabelKseg()
   {
      if (jLabelKseg == null)
      {
         jLabelKseg = new JLabel("Kb/seg: 0");
         jLabelKseg.setAlignmentX(Component.LEFT_ALIGNMENT);
         jLabelKseg.setBorder(BorderFactory.createEtchedBorder());

      }
       return jLabelKseg;
   }

  //==========================================================================
  /**
   * Obtiene jLabelFileSize
   * @return JLabel
   */
   private JLabel getjLabelFileSize()
   {
      if (jLabelFileSize == null)
      {
         jLabelFileSize = new JLabel("Size: "+this.lFileSize+" bytes");
         jLabelFileSize.setAlignmentX(Component.LEFT_ALIGNMENT);
      }
       return jLabelFileSize;
   }

  //==========================================================================
  /**
   * Obtiene jLabelBytesTransmitidos
   * @return JLabel
   */
   private JLabel getjLabelBytesTransmitidos()
   {
      if (jLabelBytesTransmitidos == null)
      {
         jLabelBytesTransmitidos = new JLabel("Trasmited: "+this.lBytesTransmitidos);
         jLabelBytesTransmitidos.setAlignmentX(Component.CENTER_ALIGNMENT);
      }
       return jLabelBytesTransmitidos;
   }

  //==========================================================================
  /**
   * Obtiene jLabelBytesRestantes
   * @return JLabel
   */
   private JLabel getjLabelBytesRestantes()
   {
      if (jLabelBytesRestantes == null)
      {
         jLabelBytesRestantes = new JLabel("Remain: "+this.lBytesRestantes);
         jLabelBytesRestantes.setAlignmentX(Component.RIGHT_ALIGNMENT);
      }
       return jLabelBytesRestantes;
   }

  //==========================================================================
  /**
   * Obtiene jLabelBytesRestantes
   * @return JLabel
   */
   private JLabel getjLabelTiempo()
   {
      if (jLabelTiempo == null)
      {
         jLabelTiempo = new JLabel("Time> 0:00");
         jLabelTiempo.setAlignmentX(Component.LEFT_ALIGNMENT);
      }
       return jLabelTiempo;
   }

  //==========================================================================
  /**
   * Obtiene lTiempo
   * @return long
   */
   long getlTiempo(){return this.lTiempo; }

 //==========================================================================
  /**
   * Obtiene BytyesTransmitidos
   * @return long
   */
   long getlBytesTransmitidos(){return this.lBytesTransmitidos; }

  //==========================================================================
  /**
   * Obtiene lKB_seg
   * @return long
   */
   double getdKB_seg(){return this.dKB_seg; }

  //==========================================================================
  /**
   * Obtiene getjLabelTiempoEstimado
   * @return JLabel
   */
   private JLabel getjLabelTiempoRestante()
   {
      if (jLabelTiempoRestante == null)
      {
         jLabelTiempoRestante = new JLabel("T. remain> 0:00");
         jLabelTiempoRestante.setAlignmentX(Component.LEFT_ALIGNMENT);
         jLabelTiempoRestante.setPreferredSize(new Dimension(150,20));
      }
       return jLabelTiempoRestante;
   }


  //==========================================================================
  /**
   * Obtiene jLabelPuerto
   * @return JLabel
   */
   private JLabel getjLabelPuerto()
   {
      if (jLabelPuerto == null)
      {
         jLabelPuerto = new JLabel("Multicast Port:");
         jLabelPuerto.setAlignmentX(Component.LEFT_ALIGNMENT);
      }
       return jLabelPuerto;
   }

  //==========================================================================
  /**
   * Obtiene jLabelTTLSesion
   * @return JLabel
   */
   private JLabel getjLabelTTLSesion()
   {
      if (jLabelTTLSesion == null)
      {
        jLabelTTLSesion = new JLabel("TTL Sesion:");
        jLabelTTLSesion.setAlignmentX(Component.LEFT_ALIGNMENT);
      }
       return jLabelTTLSesion;
   }





  //==========================================================================
  /**
   * Obtiene actionListenerCancelar
   * @return ActionListenerCancelar
   */
  private ActionListenerCancelar getactionListenerCancelar()
  {
    if(actionListenerCancelar == null)
    {
      actionListenerCancelar = new ActionListenerCancelar();
    }
     return actionListenerCancelar;
  }

//==========================================================================
  /**
   *  M�todo show().
   */
  public void show()
  {

    // LLamar al m�todo  show() del padre
    super.show();
  }


 //==========================================================================
 /**
  * Clase interna ActionListenerCancelar implementa ActionListener
  */
 class ActionListenerCancelar implements ActionListener
 {
    public void actionPerformed(ActionEvent e)
    {
      setVisible(false);
      fileEmision.stopTransferencia();
    }
 }


 //==========================================================================
 /**
  * Clase main para depurar
  */
 public  static void main(String[] args)
  {

   JFrame frame = new JFrame("Prueba de Di�logo");
   frame.show();

   JDialogTransferencia conectar = new JDialogTransferencia(null,frame,"Multicast connection...",true,"Prueba.exe",2345,null);
   conectar.show();

  }
}


