/**
  Fichero: JDialogConectar.java  1.0 1/12/99
  Copyright (c) 2000-2014 . All Rights Reserved.
  @Autor: Alejandro García Domínguez alejandro.garcia.dominguez@gmail.com   alejandro@iacobus.com
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
//----------------------------------------------------------------------------


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

 //==========================================================================
 /**
  * Clase JDialogConectar. Implementa el diálogo de conexión multicast.
  */
 public class JDialogConectar extends JDialog
 {

  /** Clase mFtp */
  //mFtp ftpMulticast = null;

  /** Dirección IP MUlticast */
  private Address addressIPMulticast = null;

  /** Dirección IP de la Interfaz de salida */
  private Address addressIPInterfaz = null;

  /** TTL de la sesión */
  private Integer TTLSesion = null;

  /** Clave RC2 */
  private char[] charClaveRC2 = null;


  //Paneles
  private JPanel panelComponente = null;
  private JPanel panelBotones = null;
  private JPanel panel1 = null;
  private JPanel panel3 = null;
  private JPanel panel2 = null;
  private JPanel panel5 = null;
  private JPanel panel4 = null;
  private JPanel panel6 = null;
  private JPanel panelEmisorReceptor = null;
  private JPanel panelIP = null;
  private JPanel panelTTL = null;
  private JPanel panelClave = null;
  private JPanel panelPuerto = null;
  private JPanel panelRadioButtons = null;

  //Botones
  private JButton jButtonConectar = null;
  private JButton jButtonCancelar = null;
  private JButton jButtonExaminar = null;

  //Etiquetas
  private JLabel jLabelIPMulticast= null;
  private JLabel jLabelTTLSesion = null;
  private JLabel jLabelClave = null;
  private JLabel jLabelFileName = null;
  private JLabel jLabelPuerto = null;

  // TextField
  private JTextField      jTextFieldIPMulticast = null;
  private JTextField      jTextFieldInterfaz = null;
  private JTextField      jTextFieldPuerto = null;
  private JPasswordField  jPasswordFieldClave = null;

  // Combobox
  private JComboBox  jComboBoxTTLSesion = null;
  private JComboBox  jComboBoxRatio = null;

  //JChekBox
  private JCheckBox  jChekBoxInterfaz = null;
  private JCheckBox  jChekBoxSSL = null;

  //JRadioButton
  private JRadioButton  jRadioButtonFiable = null;
  private JRadioButton  jRadioButtonNoFiable = null;
  private JRadioButton  jRadioButtonEmisor = null;
  private JRadioButton  jRadioButtonReceptor = null;
  private JRadioButton  jRadioButtonFiableRetrasado = null;

  // ActionListeners
  private ActionListenerConectar actionListenerConectar = null;
  private ActionListenerCancelar actionListenerCancelar = null;
  private ActionListenerEmisorReceptor actionListenerEmisorReceptor = null;

  // ItemListener
  private ItemListenerCheckBox itemListenerCheckBox = null;

 //==========================================================================
 /**
  * Constructor. Construye un JDialog.
  * @param frame El frame padre
  * @param title El título del diálogo
  * @param modal Boolean que especifica si el diálogo es modal o no
  */
 public JDialogConectar(Frame frame, String title, boolean modal) {
    super(frame, title, modal);
    try  {
      Init();
      pack();
      //this.ftpMulticast = ftpMulticast;
      Dimension	screenSize = Toolkit.getDefaultToolkit().getScreenSize();
     	setLocation(new Point(screenSize.width/2 - this.getWidth()/2, screenSize.height/2 - this.getHeight()/2));


    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

 //==========================================================================
 /**
  * Constructor genérico. Utiliza como JFrame null, de título "" y  crea un diálogo
  * modal
  */
  public JDialogConectar() {
    this(null, "", true);
  }


 //==========================================================================
 /**
  * Método de inicio.
  * @exception Exception si hay un error en la creación del diálogo.
  */
  private void Init() throws Exception {


    // Añadir paneles al diálogo
    getContentPane().add(getpanelComponente(),BorderLayout.CENTER);
    getContentPane().add(getpanelBotones(),BorderLayout.SOUTH);



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

      // Añadir
      panelComponente.add(getpanel1());
      panelComponente.add(getpanel3());
      panelComponente.add(getpanel4());
      panelComponente.add(getpanel6());
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

      // Añadir botones
      panelBotones.add(getjButtonConectar());
      panelBotones.add(getjButtonCancelar());
   }

   return panelBotones;
  }

  //==========================================================================
  /**
   * Obtiene panel1
   * @return JPanel
   */
   private JPanel getpanelClave()
   {
      if (panelClave == null)
      {
        panelClave = new JPanel(true);
        panelClave.setLayout(new FlowLayout(FlowLayout.LEFT));
        panelClave.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Añadir
        panelClave.add(getjLabelClave());
        panelClave.add(getjPasswordFieldClave());

      }

      return panelClave;
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
        panel1.setLayout(new BoxLayout(panel1,BoxLayout.X_AXIS));

        // Añadir
        panel1.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel1.add(getpanelIP());
        panel1.add(getpanelTTL());

      }

      return panel1;
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
        panel2.setLayout(new BoxLayout(panel2,BoxLayout.X_AXIS));
        panel2.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        panel2.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Añadir
        ButtonGroup bg = new ButtonGroup();
        bg.add(getjRadioButtonFiable());
        bg.add(getjRadioButtonFiableRetrasado());
        bg.add(getjRadioButtonNoFiable());

        panel2.add(getpanelPuerto());
        panel2.add(getpanelRadioButtons());
      }

      return panel2;
   }

  //==========================================================================
  /**
   * Obtiene panelRadioButtons
   * @return JPanel
   */
   private JPanel getpanelRadioButtons()
   {
      if (panelRadioButtons == null)
      {
        panelRadioButtons = new JPanel(true);
        panelRadioButtons.setLayout(new BoxLayout(panelRadioButtons,BoxLayout.Y_AXIS));
        panelRadioButtons.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        panelRadioButtons.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Añadir
        ButtonGroup bg = new ButtonGroup();
        bg.add(getjRadioButtonFiable());
        bg.add(getjRadioButtonFiableRetrasado());
        panelRadioButtons.add(getjRadioButtonFiable());
        panelRadioButtons.add(getjRadioButtonFiableRetrasado());
        //panelRadioButtons.add(getjRadioButtonNoFiable());

      }

      return panelRadioButtons;
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
        panel3.setLayout(new BoxLayout(panel3,BoxLayout.X_AXIS));
        panel3.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        panel3.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Añadir

        panel3.add(getjChekBoxInterfaz());
        panel3.add(getjTextFieldInterfaz());
      }

      return panel3;
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
        panel4.setLayout(new BoxLayout(panel4,BoxLayout.X_AXIS));
        //panel4.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        panel4.setBorder(new TitledBorder(new CompoundBorder(BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(0,0,0,0)),"mFtp mode:"));

        panel4.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Añadir
        panel4.add(getpanelEmisorReceptor());
       }

      return panel4;
   }

  //==========================================================================
  /**
   * Obtiene panel Emisor/Receptor
   * @return JPanel
   */
   private JPanel getpanelEmisorReceptor()
   {
      if (panelEmisorReceptor == null)
      {
        panelEmisorReceptor = new JPanel(true);
        panelEmisorReceptor.setLayout(new BoxLayout(panelEmisorReceptor,BoxLayout.Y_AXIS));
        //panelEmisorReceptor.setBorder(BorderFactory.createEtchedBorder(5,5,5,5));

        panelEmisorReceptor.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Añadir
        ButtonGroup bg = new ButtonGroup();
        bg.add(getjRadioButtonEmisor());
        bg.add(getjRadioButtonReceptor());

        panelEmisorReceptor.add(Box.createHorizontalGlue());
        panelEmisorReceptor.add(getjRadioButtonEmisor());
        panelEmisorReceptor.add(Box.createHorizontalGlue());
        panelEmisorReceptor.add(getjRadioButtonReceptor());
      }

      return panelEmisorReceptor;
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
        panel5.setLayout(new BoxLayout(panel5,BoxLayout.Y_AXIS));
        panel5.setBorder(new TitledBorder(new CompoundBorder(BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(0,0,0,0)),"Secure channel:"));

        /*panel5.setBorder(new CompoundBorder(BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(0,3,0,0)));
        */
        // Añadir
        //panel4.add(Box.createRigidArea(new Dimension(5,5)));
        panel5.add(getjChekBoxSSL());
        panel5.add(getpanelClave());

      }

      return panel5;
   }

  //==========================================================================
  /**
   * Obtiene panel6
   * @return JPanel
   */
   private JPanel getpanel6()
   {
      if (panel6 == null)
      {
        panel6 = new JPanel(true);
        panel6.setLayout(new BoxLayout(panel6,BoxLayout.X_AXIS));
        //panel4.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        panel6.setBorder(new TitledBorder(new CompoundBorder(BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(0,0,0,0)),"Transmission Rate:"));

        panel6.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Añadir
        panel6.add(new JLabel("   Transmission rate Kbits/seg:      "));
        panel6.add(getjComboBoxRatio());
       }

      return panel6;
   }



  //==========================================================================
  /**
   * Obtiene panelIP
   * @return JPanel
   */
   private JPanel getpanelIP()
   {
      if (panelIP == null)
      {
        panelIP = new JPanel(true);
        panelIP.setLayout(new BoxLayout(panelIP,BoxLayout.Y_AXIS));
        panelIP.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        panelIP.add(getjLabelIPMulticast());
        panelIP.add(getjTextFieldIPMulticast());
        panelIP.add(getpanelPuerto());
        //panelIP.add(getpanelRadioButtons());

      }

      return panelIP;
   }

  //==========================================================================
  /**
   * Obtiene panelPuerto
   * @return JPanel
   */
   private JPanel getpanelPuerto()
   {
      if (panelPuerto == null)
      {
        panelPuerto = new JPanel(true);
        panelPuerto.setLayout(new BoxLayout(panelPuerto,BoxLayout.Y_AXIS));
        panelPuerto.setBorder(BorderFactory.createEmptyBorder(3,0,5,0));


         // Añadir
        panelPuerto.add(getjLabelPuerto());
        panelPuerto.add(getjTextFieldPuerto());

      }

      return panelPuerto;
   }


  //==========================================================================
  /**
   * Obtiene panelTTL
   * @return JPanel
   */
   private JPanel getpanelTTL()
   {
      if (panelTTL == null)
      {
        panelTTL = new JPanel(true);
        panelTTL.setLayout(new BoxLayout(panelTTL,BoxLayout.Y_AXIS));
        panelTTL.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));


         // Añadir
        panelTTL.add(getjLabelTTLSesion());
        panelTTL.add(getjComboBoxTTLSesion());
        panelTTL.add(getpanelRadioButtons());
      }

      return panelTTL;
   }

  //==========================================================================
  /**
   * Obtiene jButtonConectar
   * @return JButton
   */
   private JButton getjButtonConectar()
   {
      if (jButtonConectar == null)
      {
         jButtonConectar = new JButton("Connect");
         jButtonConectar.addActionListener(getactionListenerConectar());
      }

      return jButtonConectar;
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
        jButtonCancelar = new JButton("Cancel");
        jButtonCancelar.addActionListener(getactionListenerCancelar());
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
   * Obtiene jLabelClave
   * @return JLabel
   */
   private JLabel getjLabelClave()
   {
      if (jLabelClave == null)
      {
        jLabelClave = new JLabel("Key RC2:");
      }
       return jLabelClave;
   }


  //==========================================================================
  /**
   * Obtiene jTextFieldIPMulticast
   * @return JTextField
   */
   private JTextField getjTextFieldIPMulticast ()
   {
      if (jTextFieldIPMulticast  == null)
      {
        jTextFieldIPMulticast = new JTextField("224.2.2.2");
        jTextFieldIPMulticast.setToolTipText("IP Multicast address i.e: 224.2.2.2");
        jTextFieldIPMulticast.setBorder(BorderFactory.createLoweredBevelBorder());
        jTextFieldIPMulticast.setPreferredSize(new Dimension(100,20));
        jTextFieldIPMulticast.setAlignmentX(Component.LEFT_ALIGNMENT);
      }
       return jTextFieldIPMulticast ;
   }

  //==========================================================================
  /**
   * Obtiene jTextFieldPuerto
   * @return JTextField
   */
   private JTextField getjTextFieldPuerto()
   {
      if (jTextFieldPuerto  == null)
      {
         jTextFieldPuerto = new JTextField("20");
         jTextFieldPuerto.setBorder(BorderFactory.createLoweredBevelBorder());
         jTextFieldPuerto.setAlignmentX(Component.LEFT_ALIGNMENT);
         jTextFieldPuerto.setPreferredSize(new Dimension(50,20));
         jTextFieldPuerto.setToolTipText("Multicast Port");
      }
       return jTextFieldPuerto;
   }

  //==========================================================================
  /**
   * Obtiene jTextFieldInterfaz
   * @return JTextField
   */
   private JTextField getjTextFieldInterfaz()
   {
      if (jTextFieldInterfaz  == null)
      {
         jTextFieldInterfaz = new JTextField("");
         jTextFieldInterfaz.setBorder(BorderFactory.createLoweredBevelBorder());
         jTextFieldInterfaz.setAlignmentX(Component.LEFT_ALIGNMENT);
         jTextFieldInterfaz.setEnabled(false);
         jTextFieldInterfaz.setBackground(Color.lightGray);
         jTextFieldInterfaz.setPreferredSize(new Dimension(150,20));
         jTextFieldInterfaz.setToolTipText("Interface IP address to use for multihome");
      }
       return jTextFieldInterfaz;
   }



  //==========================================================================
  /**
   * Obtiene jPasswordFieldClave
   * @return JPasswordField
   */
   private JPasswordField getjPasswordFieldClave()
   {
      if (jPasswordFieldClave == null)
      {
        jPasswordFieldClave = new JPasswordField(20);
        jPasswordFieldClave.setToolTipText("Text for generate rc2 key encryption");
        jPasswordFieldClave.setBorder(BorderFactory.createLoweredBevelBorder());
        jPasswordFieldClave.setAlignmentX(Component.LEFT_ALIGNMENT);
        jPasswordFieldClave.setPreferredSize(new Dimension(150,20));
        jPasswordFieldClave.setEnabled(false);
        jPasswordFieldClave.setBackground(Color.lightGray);

      }
       return jPasswordFieldClave;
   }

  //==========================================================================
  /**
   * Obtiene jComboBoxTTLSesion
   * @return JComboBox
   */
   private JComboBox getjComboBoxTTLSesion ()
   {
      if (jComboBoxTTLSesion  == null)
      {
         java.util.Vector ttls = new java.util.Vector();
         for(int i=1; i < 255; i++)
           ttls.add(new Integer(i));

          jComboBoxTTLSesion = new JComboBox(ttls);
          jComboBoxTTLSesion.setAlignmentX(Component.LEFT_ALIGNMENT);
          jComboBoxTTLSesion.setEditable(false);
          jComboBoxTTLSesion.setSelectedIndex(1);
          jComboBoxTTLSesion.setPreferredSize(new Dimension(30,20));
          jComboBoxTTLSesion.setToolTipText("TTL to use on Multicast sesion. Default is 8.");

      }
       return jComboBoxTTLSesion ;
   }

   //==========================================================================
  /**
   * Obtiene jComboBoxRatio
   * @return JComboBox
   */
   private JComboBox getjComboBoxRatio ()
   {
      if (jComboBoxRatio  == null)
      {
         java.util.Vector ratio = new java.util.Vector();

          for(int i=2048; i > 512; i-=256)
         {
           ratio.add(""+i+"   KB/seg");
         }

         for(int i=512; i > 64; i-=64)
         {
           ratio.add(""+i+"   KB/seg");
         }

         for(int i=64; i > 16; i-=32)
         {
           ratio.add(""+i+"   KB/seg");
         }

          jComboBoxRatio = new JComboBox(ratio);
          //jComboBoxRatio.setAlignmentX(Component.LEFT_ALIGNMENT);
          jComboBoxRatio.setEditable(false);
          jComboBoxRatio.setSelectedIndex(0);
          jComboBoxRatio.setEnabled(false);
          jComboBoxRatio.setPreferredSize(new Dimension(30,20));
          jComboBoxRatio.setToolTipText("Multicast transfer rate. Only sender mode");

      }
       return jComboBoxRatio ;
   }

  //==========================================================================
  /**
   * Obtiene jChekBoxInterfaz
   * @return JCheckBox
   */
   private JCheckBox  getjChekBoxInterfaz()
   {
      if (jChekBoxInterfaz == null)
      {
        jChekBoxInterfaz = new JCheckBox("Sender interface");
        jChekBoxInterfaz.setToolTipText("Enable/disable sender interface ");
        jChekBoxInterfaz.setPreferredSize(new Dimension(130,20));
        jChekBoxInterfaz.addItemListener(getitemListenerCheckBox());

      }
       return jChekBoxInterfaz;
   }

  //==========================================================================
  /**
   * Obtiene jChekBoxSSL
   * @return JCheckBox
   */
   private JCheckBox  getjChekBoxSSL()
   {
      if (jChekBoxSSL == null)
      {
        jChekBoxSSL = new JCheckBox("Multicast secure (RC2)");
        jChekBoxSSL.setToolTipText("Enable/disable Multicast secure channel");
        jChekBoxSSL.setAlignmentX(Component.LEFT_ALIGNMENT);
        jChekBoxSSL.addItemListener(getitemListenerCheckBox());

      }
       return jChekBoxSSL;
   }

  //==========================================================================
  /**
   * Obtiene jRadioButtonFiable
   * @return JRadioButton
   */
   private JRadioButton getjRadioButtonFiable()
   {
      if (jRadioButtonFiable  == null)
      {
        jRadioButtonFiable = new JRadioButton("Reliable",true);
        jRadioButtonFiable.setToolTipText("PTMF Reliable");
        jRadioButtonFiable.setPreferredSize(new Dimension(100,20));
      }
       return jRadioButtonFiable ;
   }

   //==========================================================================
  /**
   * Obtiene jRadiojRadioButtonFiableRetrasado
   * @return JRadioButton
   */
   private JRadioButton getjRadioButtonFiableRetrasado()
   {
      if (jRadioButtonFiableRetrasado  == null)
      {
        jRadioButtonFiableRetrasado = new JRadioButton("Late Reliable",false);
        jRadioButtonFiableRetrasado.setToolTipText("PTMF Late Reliable mode");
        jRadioButtonFiableRetrasado.setPreferredSize(new Dimension(100,20));
      }

       return jRadioButtonFiableRetrasado ;
   }

  //==========================================================================
  /**
   * Obtiene jRadiojRadioButtonFiableRetrasado
   * @return JRadioButton
   */
   private JRadioButton getjRadioButtonNoFiable()
   {
      if (jRadioButtonNoFiable  == null)
      {
        jRadioButtonNoFiable = new JRadioButton("No Reliable",false);
        jRadioButtonNoFiable.setToolTipText("PTMF No reliable mode");
        jRadioButtonNoFiable.setPreferredSize(new Dimension(100,20));
      }

       return jRadioButtonNoFiable ;
   }

  //==========================================================================
  /**
   * Obtiene jRadioButtonEmisor
   * @return JRadioButton
   */
   private JRadioButton getjRadioButtonEmisor()
   {
      if (jRadioButtonEmisor  == null)
      {
        jRadioButtonEmisor = new JRadioButton("\"Sender\"",false);
        jRadioButtonEmisor.setToolTipText("mFtp sender mode ");
        jRadioButtonEmisor.setPreferredSize(new Dimension(100,20));
        jRadioButtonEmisor.setActionCommand("Sender");
        jRadioButtonEmisor.addActionListener(getactionListenerEmisorReceptor());
      }

       return jRadioButtonEmisor ;
   }

  //==========================================================================
  /**
   * Obtiene jRadioButtonReceptor
   * @return JRadioButton
   */
   private JRadioButton getjRadioButtonReceptor()
   {
      if (jRadioButtonReceptor  == null)
      {
        jRadioButtonReceptor = new JRadioButton("\"Receiver\"",true);
        jRadioButtonReceptor.setToolTipText("mFtp receiver mode");
        jRadioButtonReceptor.setPreferredSize(new Dimension(100,20));
        jRadioButtonReceptor.setActionCommand("Receiver");
        jRadioButtonReceptor.addActionListener(getactionListenerEmisorReceptor());
      }

       return jRadioButtonReceptor ;
   }

  //==========================================================================
  /**
   * Obtiene actionListenerConectar
   * @return ActionListenerConectar
   */
  private ActionListenerConectar getactionListenerConectar()
  {
    if(actionListenerConectar == null)
    {
      actionListenerConectar = new ActionListenerConectar();
    }
     return actionListenerConectar;
  }

  //==========================================================================
  /**
   * Obtiene actionListenerEmisorReceptor
   * @return actionListenerEmisorReceptor
   */
  private ActionListenerEmisorReceptor getactionListenerEmisorReceptor()
  {
    if(actionListenerEmisorReceptor == null)
    {
      actionListenerEmisorReceptor = new ActionListenerEmisorReceptor();
    }
     return actionListenerEmisorReceptor;
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
   * Obtiene ItemListenerCheckBox
   * @return ActionListenerConectar
   */
  private ItemListenerCheckBox getitemListenerCheckBox()
  {
    if(itemListenerCheckBox == null)
    {
      itemListenerCheckBox = new ItemListenerCheckBox();
    }
     return itemListenerCheckBox;
  }

  //==========================================================================
  /**
   * Devuelve la dirección IP MUlticast
   * @return Una cadena con el IP Multicast
   */
  public Address getIPMulticast() { return addressIPMulticast;}

  //==========================================================================
  /**
   * Devuelve la dirección IP de la Interfaz de salida
   * @return Una cadena con el IP Multicast
   */
  public Address getIPInterfaz() { return addressIPInterfaz; }

  //==========================================================================
  /**
   * Devuelve el TTL de la sesión.
   * @return Un entero con el TTL de la sesión
   */
  public int getTTLSesion() { return TTLSesion.intValue();}


  //==========================================================================
  /**
   * Devuelve el Modo del protocolo : FIABLE o NO-FIABLE
   * @return Un entero con el TTL de la sesión
   */
  public int getModo()
   {
      if (this.getjRadioButtonFiable().isSelected())
        return PTMF.PTMF_FIABLE;
      else
        return PTMF.PTMF_FIABLE_RETRASADO;
    }

  //==========================================================================
  /**
   * Devuelve la clave RC2
   * @return Un char[] con la clave RC2
   */
  public char[] getClaveRC2() { return charClaveRC2;}

  //==========================================================================
  /**
   *  Método show().
   */
  public void show()
  {
    addressIPMulticast = null;
    addressIPInterfaz = null;
    TTLSesion = null;
    charClaveRC2 = null;

    // LLamar al método  show() del padre
    super.show();
  }

 //==========================================================================
 /**
  * Clase interna ActionListenerConectar implementa ActionListener
  */
 class ActionListenerConectar implements ActionListener
 {
    String stringIPMulticast = null;
    String stringIPInterfaz = null;
    String stringPuerto = null;

    final String[] mensajeErrorIPMulticast= {
    "Incorrect IP Multicast Address.",
    "An IPv4 Multicast is a class D address.",
    "The range must be between",
    "224.0.0.0 to 239.255.255.255 ",
    " ",
    "For example \"224.2.2.2\" "
    };

    final String[] mensajeErrorIPInterfaz= {
    "Incorrect IP address for sender interface ",
    " ",
    "PLease, especify an ip address for the interface available on your computer"
    
    };

    final String[] mensajeErrorClave= {
    "Please, introduce any text",
    "This text will be used to generate a key. This key will be used on RC2 cryptosystem",
    "",
    "All user must use the same text to communicate securitely"
    };

    final String[] mensajeErrorFileName ={
    "Please, select the file to be tramsited."
    };

    final String[] mensajeErrorPuerto = {
    "The multicast port must be between 0 and 65535, both inclusive."
    };
    public void actionPerformed(ActionEvent e)
    {
      // Obtener cadenas...
      stringIPMulticast = getjTextFieldIPMulticast().getText();
      stringIPInterfaz = getjTextFieldInterfaz().getText();
      stringPuerto = getjTextFieldPuerto().getText();
      charClaveRC2 = getjPasswordFieldClave().getPassword();
      TTLSesion = (Integer) getjComboBoxTTLSesion().getSelectedItem();
      String sRatio = (String) getjComboBoxRatio().getSelectedItem();
      sRatio = sRatio.substring(0,sRatio.length()-9);

      long lRatio = Long.parseLong(sRatio);

      // Pasar a bytes/seg.
      lRatio = (lRatio * 1024);

      //Log.log("",stringIPMulticast);
      //Log.log("",stringIPInterfaz);
      //Log.log("",""+charClaveRC2);
      //Log.log("",""+TTLSesion);
      //Log.log("JDialogConectar--> Ratio; "+lRatio+" Kbps","");

      // Comprobar que se ha escrito algo correcto.
      if(stringIPMulticast.equals(""))
      {
        ErrorIPMulticast();
        return;
      }

      //Comprobar que la dirección IP Multicast es correcta:
      try
      {
        addressIPMulticast = new Address(stringIPMulticast,0);

        if(!addressIPMulticast.isMulticastAddress())
        {
          ErrorIPMulticast();
          return;
        }

        //Comprobar puerto multicast
        if(stringPuerto.equals(""))
        {
          ErrorPuerto();
          return;
        }

        int puerto = Integer.parseInt(stringPuerto);
        if ( (puerto > 65536) || (puerto < 0))
        {
          ErrorPuerto();
          return;
        }

        //Establecer puerto
        addressIPMulticast.setPort(puerto);
      }
      catch(UnknownHostException ue)
      {
        ErrorIPMulticast();
        return;
      }
      catch(NumberFormatException  ue)
      {
        ErrorPuerto();
        return;
      }

      //Comprobar que la dirección IP de la Interfaz es correcta
      if(getjChekBoxInterfaz().isSelected())
      {

        if(stringIPInterfaz.equals(""))
        {
          ErrorIPInterfaz();
          return;
        }

        //Comprobar que la dirección IP Interfaz
        try
        {
          addressIPInterfaz = new Address(stringIPInterfaz,0);
        }
        catch(UnknownHostException ue)
        {
          ErrorIPInterfaz();
          return;
        }
      }

      //Comprobar la clave
      if(getjChekBoxSSL().isSelected())
      {
        if(""+charClaveRC2 =="")
        {
          ErrorClave();
          return;
        }
      }

      setVisible(false);

      //Conectar......
      mFtp.getFTP().conectar(getIPMulticast(),getIPInterfaz(),getTTLSesion(),
       getModo(),lRatio,getClaveRC2(),getjRadioButtonEmisor().isSelected());
    }


    private void ErrorPuerto()
    {
      JOptionPane.showMessageDialog(getContentPane(),mensajeErrorPuerto,
				    "Puerto Multicast", JOptionPane.INFORMATION_MESSAGE);
    }

    private void ErrorIPMulticast()
    {
      JOptionPane.showMessageDialog(getContentPane(),mensajeErrorIPMulticast,
				    "IP Multicast", JOptionPane.INFORMATION_MESSAGE);
    }

    private void ErrorIPInterfaz()
    {
      JOptionPane.showMessageDialog(getContentPane(),mensajeErrorIPInterfaz,
				    "IP Interfaz", JOptionPane.INFORMATION_MESSAGE);
    }

    private void ErrorClave()
    {
      JOptionPane.showMessageDialog(getContentPane(),mensajeErrorClave,
				    "Clave", JOptionPane.INFORMATION_MESSAGE);
    }

    private void ErrorFileName()
    {
      JOptionPane.showMessageDialog(getContentPane(),mensajeErrorFileName,
				    "FileName", JOptionPane.INFORMATION_MESSAGE);
    }

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
    }
 }


 //==========================================================================
 /**
  * Clase interna ActionListener implementa ActionListener
  */
 class ActionListenerEmisorReceptor implements ActionListener
 {
    public void actionPerformed(ActionEvent e)
    {
        if(e.getActionCommand() == "Emisor")
        {
         getjComboBoxRatio().setEnabled(true);
        }
        else
        {
         getjComboBoxRatio().setEnabled(false);
        }

    }
 }

 //==========================================================================
 /**
  * Clase interna ItemListenerCheckBox implementa ItemListener
  */
 class ItemListenerCheckBox implements ItemListener {

    public void itemStateChanged(ItemEvent e) {

        Object source = e.getItemSelectable();

        if (source == getjChekBoxInterfaz())
        {
           if (e.getStateChange() == ItemEvent.DESELECTED)
           {
              getjTextFieldInterfaz().setText("");
              getjTextFieldInterfaz().setEnabled(false);
              getjTextFieldInterfaz().setBackground(Color.lightGray);

           }
           else
           {
              getjTextFieldInterfaz().setEnabled(true);
              getjTextFieldInterfaz().setBackground(Color.white);
           }
        }
        else if (source == getjChekBoxSSL())
        {
           if (e.getStateChange() == ItemEvent.DESELECTED)
           {
              getjPasswordFieldClave().setText("");
              getjPasswordFieldClave().setEnabled(false);
              getjPasswordFieldClave().setBackground(Color.lightGray);
           }
           else
           {
              getjPasswordFieldClave().setEnabled(true);
              getjPasswordFieldClave().setBackground(Color.white);
           }
        }
    }
 }

 //==========================================================================
 /**
  * Clase main para depurar
  */
 public  static void main(String[] args)
  {

   JFrame frame = new JFrame("Prueba de Diálogo");
   frame.show();

   JDialogConectar conectar = new JDialogConectar(frame,"Multicast connection...",true);
   conectar.show();

  }
}


