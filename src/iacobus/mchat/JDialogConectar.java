/**
  
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

package iacobus.mchat;

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

  /** Clase mChat */
  mChat chatMulticast = null;

  /** Dirección IP MUlticast */
  private Address addressIPMulticast = null;

  /** Dirección IP de la Interfaz de salida */
  private Address addressIPInterfaz = null;

  /** TTL de la sesión */
  private Integer TTLSesion = null;

  /** Clave RC2 */
  private String stringNickName = null;

  /** Clave RC2 */
  private char[] charClaveRC2 = null;


  //Paneles
  private JPanel panelComponente = null;
  private JPanel panelBotones = null;
  private JPanel panel1 = null;
  private JPanel panel2 = null;
  private JPanel panel3 = null;
  private JPanel panel4 = null;
  private JPanel panel5 = null;
  private JPanel panelIP = null;
  private JPanel panelTTL = null;
  private JPanel panelClave = null;
  private JPanel panelPuerto = null;
  private JPanel panelRadioButtons = null;

  //Botones
  private JButton jButtonConectar = null;
  private JButton jButtonCancelar = null;

  //Etiquetas
  private JLabel jLabelIPMulticast= null;
  private JLabel jLabelTTLSesion = null;
  private JLabel jLabelClave = null;
  private JLabel jLabelNickName = null;
  private JLabel jLabelPuerto = null;

  // TextField
  private JTextField      jTextFieldIPMulticast = null;
  private JTextField      jTextFieldInterfaz = null;
  private JTextField      jTextFieldNickName = null;
  private JTextField      jTextFieldPuerto = null;
  private JPasswordField  jPasswordFieldClave = null;

  // Combobox
  private JComboBox  jComboBoxTTLSesion = null;

  //JChekBox
  private JCheckBox  jChekBoxInterfaz = null;
  private JCheckBox  jChekBoxSSL = null;

  //JRadioButton
  private JRadioButton  jRadioButtonFiable = null;
  private JRadioButton  jRadioButtonNoFiable = null;
  private JRadioButton  jRadioButtonNoFiableOrdenado = null;

  // ActionListeners
  private ActionListenerConectar actionListenerConectar = null;
  private ActionListenerCancelar actionListenerCancelar = null;

  // ItemListener
  private ItemListenerCheckBox itemListenerCheckBox = null;

 //==========================================================================
 /**
  * Constructor. Construye un JDialog.
  * @param frame El frame padre
  * @param title El título del diálogo
  * @param modal Boolean que especifica si el diálogo es modal o no
  */
 public JDialogConectar(mChat chatMulticast,Frame frame, String title, boolean modal) {
    super(frame, title, modal);
    try  {
      Init();
      pack();
      this.chatMulticast = chatMulticast;
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
  public JDialogConectar(mChat chatMulticast) {
    this(chatMulticast,null, "", true);
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
      panelComponente.add(getpanel2());
      panelComponente.add(getpanel3());
      panelComponente.add(getpanel4());
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
        panel1.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));

        // Añadir
        panel1.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel1.add(getpanelIP());
        panel1.add(getpanelPuerto());

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
        //panel2.setLayout(new BoxLayout(panel2,BoxLayout.X_AXIS));
         panel2.setLayout(new FlowLayout(FlowLayout.LEFT));

        //panel2.setBorder(BorderFactory.createEmptyBorder(0,5,5,5));
        panel2.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Añadir
        panel2.add(getpanelTTL());
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
        panelRadioButtons.setLayout(new BoxLayout(panelRadioButtons,BoxLayout.X_AXIS));
        panelRadioButtons.setBorder(BorderFactory.createEmptyBorder(15,0,0,0));
        panelRadioButtons.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Añadir
        ButtonGroup bg = new ButtonGroup();
        bg.add(getjRadioButtonFiable());
        bg.add(getjRadioButtonNoFiable());
        bg.add(getjRadioButtonNoFiableOrdenado());
        panelRadioButtons.add(getjRadioButtonFiable());
        panelRadioButtons.add(getjRadioButtonNoFiable());
        panelRadioButtons.add(getjRadioButtonNoFiableOrdenado());
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
        panel4.setLayout(new BoxLayout(panel4,BoxLayout.Y_AXIS));
        panel4.setBorder(BorderFactory.createEmptyBorder(0,5,10,5));

        // Añadir
        //panel4.add(Box.createRigidArea(new Dimension(5,5)));
        panel4.add(getjLabelNickName());
        panel4.add(getjTextFieldNickName());

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
        panel5.setLayout(new BoxLayout(panel5,BoxLayout.Y_AXIS));
        panel5.setBorder(new CompoundBorder(BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(0,3,0,0)));

        // Añadir
        //panel4.add(Box.createRigidArea(new Dimension(5,5)));
        panel5.add(getjChekBoxSSL());
        panel5.add(getpanelClave());

      }

      return panel5;
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
        //panelIP.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        //panelIP.setPreferredSize(new Dimension(50,10));

        panelIP.add(getjLabelIPMulticast());
        panelIP.add(getjTextFieldIPMulticast());
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
        panelPuerto.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));

       // panelIP.setPreferredSize(new Dimension(50,10));

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
        //panelTTL.setBorder(BorderFactory.createEmptyBorder(0,5,5,5));
        //panelTTL.setPreferredSize(new Dimension(50,10));

         // Añadir
        panelTTL.add(getjLabelTTLSesion());
        panelTTL.add(getjComboBoxTTLSesion());
        //panelTTL.add();
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
         jButtonConectar = new JButton("Conectar");
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
        jButtonCancelar = new JButton("Cancelar");
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
         jLabelPuerto = new JLabel("Puerto Multicast:");
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
        jLabelTTLSesion = new JLabel("TTL Sesión:");
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
        jLabelClave = new JLabel("Clave RC2:");
      }
       return jLabelClave;
   }

  //==========================================================================
  /**
   * Obtiene jLabelNickName
   * @return JLabel
   */
   private JLabel getjLabelNickName()
   {
      if (jLabelNickName == null)
      {
        jLabelNickName = new JLabel("NickName:");
      }
       return jLabelNickName;
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
        jTextFieldIPMulticast.setToolTipText("Una dirección IP Multicast p.ej: 224.2.2.2");
        jTextFieldIPMulticast.setBorder(BorderFactory.createLoweredBevelBorder());
        jTextFieldIPMulticast.setPreferredSize(new Dimension(200,20));
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
         jTextFieldPuerto.setToolTipText("Puerto Multicast");
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
         jTextFieldInterfaz.setToolTipText("Dirección IP de la Interfaz de salida para multihosts.");
      }
       return jTextFieldInterfaz;
   }

  //==========================================================================
  /**
   * Obtiene jTextFieldNickName
   * @return JTextField
   */
   private JTextField getjTextFieldNickName()
   {
      if (jTextFieldNickName == null)
      {
         jTextFieldNickName = new JTextField("");
         jTextFieldNickName.setBorder(BorderFactory.createLoweredBevelBorder());
         jTextFieldNickName.setAlignmentX(Component.LEFT_ALIGNMENT);
         jTextFieldNickName.setPreferredSize(new Dimension(150,20));
         jTextFieldNickName.setToolTipText("NickName que le identifica en el chat Multicast.");
      }
       return jTextFieldNickName;
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
        jPasswordFieldClave.setToolTipText("Texto para la generación de una clave para encriptar con RC2");
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
         for(int i=1; i < 256; i++)
           ttls.add(new Integer(i));

          jComboBoxTTLSesion = new JComboBox(ttls);
          jComboBoxTTLSesion.setAlignmentX(Component.LEFT_ALIGNMENT);
          jComboBoxTTLSesion.setEditable(false);
          jComboBoxTTLSesion.setSelectedIndex(7);
          jComboBoxTTLSesion.setPreferredSize(new Dimension(30,20));
          jComboBoxTTLSesion.setToolTipText("TTL utilizado en la sesión Multicast. Por defecto es 8.");

      }
       return jComboBoxTTLSesion ;
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
        jChekBoxInterfaz = new JCheckBox("Interfaz de salida");
        jChekBoxInterfaz.setToolTipText("Activar/Desactivar Interfaz de salida");
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
        jChekBoxSSL = new JCheckBox("Canal Multicast Seguro (RC2)");
        jChekBoxSSL.setToolTipText("Activar/Desactivar Canal Multicast Seguro");
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
        jRadioButtonFiable = new JRadioButton("Fiable",true);
        jRadioButtonFiable.setToolTipText("Modo FIABLE de PTMF");
        //jRadioButtonFiable.setPreferredSize(new Dimension(100,20));
      }
       return jRadioButtonFiable ;
   }

  //==========================================================================
  /**
   * Obtiene jRadioButtonNoFiable
   * @return JRadioButton
   */
   private JRadioButton getjRadioButtonNoFiable()
   {
      if (jRadioButtonNoFiable  == null)
      {
        jRadioButtonNoFiable = new JRadioButton("No Fiable",false);
        jRadioButtonNoFiable.setToolTipText("Modo NO-FIABLE de PTMF");
        //jRadioButtonNoFiable.setPreferredSize(new Dimension(100,20));
      }

       return jRadioButtonNoFiable ;
   }

   //==========================================================================
  /**
   * Obtiene jRadioButtonNoFiableOrdenado
   * @return JRadioButton
   */
   private JRadioButton getjRadioButtonNoFiableOrdenado()
   {
      if (jRadioButtonNoFiableOrdenado  == null)
      {
        jRadioButtonNoFiableOrdenado = new JRadioButton("No Fiable Ordenado",false);
        jRadioButtonNoFiableOrdenado.setToolTipText("Modo NO-FIABLE-ORDENADO de PTMF");
        //jRadioButtonNoFiableOrdenado.setPreferredSize(new Dimension(100,20));
      }

       return jRadioButtonNoFiableOrdenado;
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
   * Devuelve el NickName
   * @return Una cadena con el NickName
   */
  public String getNickName() { return stringNickName; }

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
        return PTMF.PTMF_FIABLE_RETRASADO;
      else if (this.getjRadioButtonNoFiableOrdenado().isSelected())
        return PTMF.PTMF_NO_FIABLE_ORDENADO;
      else
        return PTMF.PTMF_NO_FIABLE;
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
    "Dirección IP Multicast incorrecta.",
    "Una dirección IP Multicast es una dirección IP de clase D.",
    "Las direcciones IP de clase D se encuentran en el rango",
    "224.0.0.0 a 239.255.255.255 ",
    " ",
    "La dirección siguiente: p.ej. \"224.2.2.2\" es una dirección IP Multicast válida."
    };

    final String[] mensajeErrorIPInterfaz= {
    "Dirección IP de la interfaz de salida incorrecta.",
    " ",
    "Debe de especificar una dirección IP de una interfaz de salida",
    "disponible en su ordenador."
    };

    final String[] mensajeErrorClave= {
    "Debe de especificar algún texto en el campo clave.",
    "Este texto se utilizará para generar una clave con la ",
    "que se codificará la conexión Multicast utilizando el ",
    "criptosistema RC2.",
    "",
    "Todos los usuarios que deseen comunicarse de forma segura",
    "deberán de especificar la misma clave."
    };

    final String[] mensajeErrorNickName = {
    "Debe de especificar un nombre (nickname) que le ",
    "identifique en la sesión multicast de otro usuario."
    };

    final String[] mensajeErrorPuerto = {
    "Debe de especificar un Puerto Multicast entre 0 y 65535, ambos inclusive."
    };
    public void actionPerformed(ActionEvent e)
    {
      // Obtener cadenas...
      stringIPMulticast = getjTextFieldIPMulticast().getText();
      stringIPInterfaz = getjTextFieldInterfaz().getText();
      stringNickName = getjTextFieldNickName().getText();
      stringPuerto = getjTextFieldPuerto().getText();
      charClaveRC2 = getjPasswordFieldClave().getPassword();
      TTLSesion = (Integer) getjComboBoxTTLSesion().getSelectedItem();

      //Log.log("",stringIPMulticast);
      //Log.log("",stringIPInterfaz);
      //Log.log("",""+charClaveRC2);
      //Log.log("",""+TTLSesion);

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

      //Comprobar NickName
      if(stringNickName.equals(""))
      {
          ErrorNickName();
          return;
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


      //No visible....
      setVisible(false);


      //Conectar......
      chatMulticast.conectar(getIPMulticast(),stringIPMulticast,
       getIPInterfaz(),getTTLSesion(),
       getModo(),getNickName(),getClaveRC2());


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

    private void ErrorNickName()
    {
      JOptionPane.showMessageDialog(getContentPane(),mensajeErrorNickName,
				    "NickName", JOptionPane.INFORMATION_MESSAGE);
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

   JDialogConectar conectar = new JDialogConectar(null,frame,"Conexión Multicast...",true);
   conectar.show();

  }
}


