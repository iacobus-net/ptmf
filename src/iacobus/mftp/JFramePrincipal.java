package iacobus.mftp;

/**
 * <p>Title: mFtp</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c)2003-2014</p>
 * @author M. Alejandro Garcia Dominguez  alejandro.garcia.dominguez@gmail.com alejandro@iacobus.net
 * @version 1.1
 * 
 * 
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

import java.awt.*;
import javax.swing.*;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import javax.swing.border.*;
import java.awt.event.*;
//import ipworks.*;
import javax.swing.event.*;


public class JFramePrincipal extends JFrame
{


  //Optionally play with line styles.  Possible values are
  //"Angled", "Horizontal", and "None" (the default).
  private boolean playWithLineStyle = false;
  private String lineStyle = "Angled";
  private JFileChooser jFileChooser = null;

  //Logos...
  private static ImageIcon ImageIconLogoOn = null;
  private static ImageIcon ImageIconLogoOff = null;


  // ===================== GENERADO POR JBUILDER ======================
  TitledBorder titledBorder1;
  TitledBorder titledBorder2;
  TitledBorder titledBorder3;
  TitledBorder titledBorder4;
  TitledBorder titledBorder5;
  JPanel jPanelContentPanel = new JPanel();
  JLabel jLabelPTMF = new JLabel();
  GridLayout gridLayout1 = new GridLayout();
  JPanel jPanel1 = new JPanel();
  JLabel jLabel3 = new JLabel();
  JPanel jPanelReceptor = new JPanel();
  JPanel jPanel2 = new JPanel();
  JPanel jPanelTransmisor = new JPanel();
  JTabbedPane jTabbedPane1 = new JTabbedPane();
  BorderLayout borderLayout1 = new BorderLayout();
  BorderLayout borderLayout2 = new BorderLayout();
  JToolBar jToolBar1 = new JToolBar();
  JButton jButtonRxCancelar = new JButton();
  BorderLayout borderLayout3 = new BorderLayout();
  JToolBar jToolBar2 = new JToolBar();
  BorderLayout borderLayout4 = new BorderLayout();
  JButton jButtonTxEnviar = new JButton();
  JPanel jPanelInfo = new JPanel();
  TitledBorder titledBorder6;
  JToolBar jToolBarINformacion = new JToolBar();
  JSplitPane jSplitPaneInformacion = new JSplitPane();
  JScrollPane jScrollPaneIzquierdaInformacion = new JScrollPane();
  //JTextPane jTextPaneInformacion = mFtp.getJTextPane();
  JScrollPane jScrollPaneDerechaInformacion = new JScrollPane();
  JButton jButtonDesconectar = new JButton();
  BorderLayout borderLayout5 = new BorderLayout();
  JButton jButtonConectar = new JButton();
  Component component3;
  JPanel jPanelAbout = new JPanel();
  JScrollPane jScrollPane3 = new JScrollPane();
  JTextArea jTextArea1 = new JTextArea();
  TitledBorder titledBorder7;
  BorderLayout borderLayout6 = new BorderLayout();
  JPanel jPanel4 = new JPanel();
  JLabel jLabel1 = new JLabel();
  BorderLayout borderLayout7 = new BorderLayout();
  JPanel jPanelInformacion = new JPanel();
  JTextPane jTextPaneInformacion = new JTextPane();
  CardLayout cardLayoutInformacion = new CardLayout();
  JTextPane jTextPaneIDGL = new JTextPane();
  JTreeInformacion jTreeInformacion = new JTreeInformacion();
  JFileChooser jFileChooser1 = new JFileChooser();
  TitledBorder titledBorder8;
  JScrollPane jScrollPane2 = new JScrollPane();
  JTextPane jTextPaneTransmisor = new JTextPane();
  JTextPane jTextPaneReceptor = new JTextPane();
  TitledBorder titledBorder9;
  JLabel jLabel2 = new JLabel();
  Component component1;
  Component component4;
  Component component5;
  Component component6;
  Component component7;
  Component component8;

  /**
   * Frame principal de mFtp. Este frame se ha generado con el editor gráfico,
   * ha diferencia de la versión 1.0 que se realizó con programación en código.
   */
  public JFramePrincipal()
  {
    try
    {
      jbInit();

    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   *  Inicialización de los componentes.
   */
  void init()
  {
    //Cargar imágenes...
    ImageIconLogoOff = new ImageIcon(mFtp.getImage("logo.jpg"));
    ImageIconLogoOn = new ImageIcon(mFtp.getImage("bola.gif"));

    //Iniciar estilos...
    iniciarEstilos();

    //Iniciar árbol de información
    crearArbolInformacion();
  }


  /**
   * Inicia los estilos de los paneles de texto gráficos.
   */
  private void iniciarEstilos()
  {
   //Iniciar estilos
    // ================ Panel Información =========================
    mFtp.iniciarStylesJTextPane(jTextPaneInformacion);
    copyright();
    jTextPaneInformacion.getLayout().addLayoutComponent("jTextPaneInformacion",jTextPaneInformacion);


    // ================ Panel Receptores  =========================
    mFtp.iniciarStylesJTextPane(jTextPaneReceptor);
    mFtp.insertRecepcionString("Receiver","icono_informacion");


    // ================ Panel Transmisor  =========================
    mFtp.iniciarStylesJTextPane(jTextPaneTransmisor);
    mFtp.insertTransmisionString("Sender","icono_informacion");

  }


  /**
   * Crear el árbol de información
   */
  private  void crearArbolInformacion()
  {

      /*
          //Crear el nodo raíz
          DefaultMutableTreeNode top = new DefaultMutableTreeNode("PTMF");

          treeModel = new DefaultTreeModel(rootNode);

          this.jTreeInformacion = new JTree(treeModel);
          //jTreeInformacion.setEditable(true);

          jTreeInformacion.getSelectionModel().setSelectionMode
            (TreeSelectionModel.SINGLE_TREE_SELECTION);

          jTreeInformacion.setShowsRootHandles(true);
      */

      /*
          //Crear los nodos hijos
          DefaultMutableTreeNode nodoIDGL = null;
          DefaultMutableTreeNode idgl = null;

          //Nodo General
          nodoIDGL = new DefaultMutableTreeNode("Actividad");
          top.add(nodoIDGL);

          //Nodo IDGLs
          nodoIDGL = new DefaultMutableTreeNode("IDGLs");
          top.add(nodoIDGL);

          //original Tutorial
          idgl = new DefaultMutableTreeNode("IDGL 1");
          nodoIDGL.add(idgl);

          //Tutorial Continued
          idgl = new DefaultMutableTreeNode("IDGL 2");
          nodoIDGL.add(idgl);



          //Crear el árbol, permitir solo una selección
          this.jTreeInformacion = new JTree(top);
          this.jTreeInformacion.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      */


      if (playWithLineStyle) {
            this.jTreeInformacion.putClientProperty("JTree.lineStyle", lineStyle);
      }

      jScrollPaneIzquierdaInformacion.getViewport().add(jTreeInformacion, null);


      //Listen for when the selection changes.
      /*this.jTreeInformacion.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e)
            {
              // DefaultMutableTreeNode node = (DefaultMutableTreeNode)
              //                         jTreeInformacion.getLastSelectedPathComponent();

                if (node == null) return;

                Object nodeInfo = node.getUserObject();

                if (node.isLeaf())
                {
                   //BookInfo book = (BookInfo)nodeInfo;
                   //displayURL(book.bookURL);

                   cardLayoutInformacion.next(jPanelInformacion);
                  //mFtp.insertInformacion(jTextPaneInformacion,"panel cambiado");

                }
                else
                {
                   //displayURL(helpURL);
                }
            }
      });
      */
      this.pack();
  }

  /**
   * Inicialización de JBuilder
   * @throws Exception
   */
  private void jbInit() throws Exception
  {
    titledBorder1 = new TitledBorder("");
    titledBorder2 = new TitledBorder("");
    titledBorder3 = new TitledBorder("");
    titledBorder4 = new TitledBorder("");
    titledBorder5 = new TitledBorder("");
    titledBorder6 = new TitledBorder("");
    component3 = Box.createVerticalStrut(8);
    titledBorder7 = new TitledBorder("");
    titledBorder8 = new TitledBorder("");
    titledBorder9 = new TitledBorder("");
    component1 = Box.createVerticalStrut(8);
    component4 = Box.createVerticalStrut(8);
    component5 = Box.createHorizontalStrut(8);
    component6 = Box.createHorizontalStrut(8);
    component7 = Box.createVerticalStrut(8);
    component8 = Box.createVerticalStrut(8);
    jPanelContentPanel.setLayout(borderLayout2);
    jTabbedPane1.setTabPlacement(JTabbedPane.BOTTOM);
    jTabbedPane1.setBorder(BorderFactory.createEtchedBorder());
    jPanel2.setLayout(borderLayout1);
    jLabel3.setText(" mftp v1.2");
    jLabel3.setForeground(new Color(0, 0, 66));
    jLabel3.setFont(new java.awt.Font("Dialog", 1, 20));
    jPanel1.setLayout(gridLayout1);
    jPanel1.setPreferredSize(new Dimension(40, 40));
    jPanel1.setBorder(BorderFactory.createLineBorder(Color.black));
    jPanel1.setBackground(Color.white);
    jLabelPTMF.setText("   ");
    jLabelPTMF.setIcon(new ImageIcon(JFramePrincipal.class.getResource("images/logo.jpg")));
    jLabelPTMF.setHorizontalTextPosition(SwingConstants.RIGHT);
    jLabelPTMF.setHorizontalAlignment(SwingConstants.RIGHT);
   //this.setContentPane(jPanelContentPanel);
    this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    this.setTitle("Power Transport Multicast Framework");
    this.addWindowListener(new java.awt.event.WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        this_windowClosing(e);
      }
      public void windowClosed(WindowEvent e)
      {
        this_windowClosed(e);
      }
    });

    //this.getContentPane().add(jPanelContentPanel);
    jPanelContentPanel.setPreferredSize(new Dimension(600, 450));
    jPanelContentPanel.setToolTipText("");
    jButtonRxCancelar.setMaximumSize(new Dimension(100, 25));
    jButtonRxCancelar.setMinimumSize(new Dimension(97, 25));
    jButtonRxCancelar.setPreferredSize(new Dimension(97, 25));
    jButtonRxCancelar.setIcon(new ImageIcon(JFramePrincipal.class.getResource("images/desconectar.gif")));
    jButtonRxCancelar.setMnemonic('0');
    jButtonRxCancelar.setText("Stop");
    jPanelReceptor.setLayout(borderLayout3);
    jPanelTransmisor.setLayout(borderLayout4);
    jButtonTxEnviar.setEnabled(false);
    jButtonTxEnviar.setMaximumSize(new Dimension(120, 25));
    jButtonTxEnviar.setMinimumSize(new Dimension(91, 25));
    jButtonTxEnviar.setPreferredSize(new Dimension(120, 25));
    jButtonTxEnviar.setIcon(new ImageIcon(JFramePrincipal.class.getResource("images/enviar.gif")));
    jButtonTxEnviar.setMnemonic('0');
    jButtonTxEnviar.setText("Send file");
    jButtonTxEnviar.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButtonTxEnviar_actionPerformed(e);
      }
    });
    jButtonDesconectar.setEnabled(false);
    jButtonDesconectar.setMaximumSize(new Dimension(160, 25));
    jButtonDesconectar.setMinimumSize(new Dimension(97, 25));
    jButtonDesconectar.setPreferredSize(new Dimension(120, 25));
    jButtonDesconectar.setIcon(new ImageIcon(JFramePrincipal.class.getResource("images/desconectar.gif")));
    jButtonDesconectar.setMnemonic('0');
    jButtonDesconectar.setText("Disconnect");
    jButtonDesconectar.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButtonDesconectar_actionPerformed(e);
      }
    });
    jPanelInfo.setLayout(borderLayout5);
    jButtonConectar.setMaximumSize(new Dimension(90, 25));
    jButtonConectar.setMinimumSize(new Dimension(90, 25));
    jButtonConectar.setPreferredSize(new Dimension(100, 25));
    jButtonConectar.setDisabledIcon(new ImageIcon(JFramePrincipal.class.getResource("images/conectar_inhabilitado.gif")));
    jButtonConectar.setIcon(new ImageIcon(JFramePrincipal.class.getResource("images/conectar.gif")));
    jButtonConectar.setText("Connect");
    jButtonConectar.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        jButtonConectar_actionPerformed(e);
      }
    });
    jPanelAbout.setLayout(borderLayout6);
    jTextArea1.setBackground(Color.lightGray);
    jTextArea1.setBorder(BorderFactory.createLoweredBevelBorder());
    jTextArea1.setEditable(false);
    jTextArea1.setMargin(new Insets(5, 5, 5, 5));
    jTextArea1.setText("(C) Copyright 2000-2014  iacobus.net \n"
     +"Alejandro Garcia Dominguez  alejandro.garcia.dominguez@gmail.com // alejandro@iacobus.net \n"
     +"Antonio Berrocal Piris antonioberrocalpiris@gmail.com \n" 
     +"\n"
     +"Licensed under the Apache License, Version 2.0 (the \"License\"\n" 
     +"you may not use this file except in compliance with the License.\n" 
     +"You may obtain a copy of the License at \n" 
     +"\n" 
     +"http://www.apache.org/licenses/LICENSE-2.0\n" 
     +"\n" 
     +"Unless required by applicable law or agreed to in writing, software\n" 
     +"distributed under the License is distributed on an \"AS IS\" BASIS,\n" 
     +"WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" 
     +"See the License for the specific language governing permissions and\n" 
     +"limitations under the License.\n"
     );
    jTextArea1.setLineWrap(true);
    jTextArea1.setWrapStyleWord(true);
    jLabel1.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel1.setOpaque(true);
    jLabel1.setHorizontalAlignment(SwingConstants.LEFT);
    jLabel1.setHorizontalTextPosition(SwingConstants.LEFT);
    jLabel1.setText("   (C) 2000-2014  - iacobus.net    ");
    jPanel4.setLayout(borderLayout7);
    jPanelInformacion.setLayout(cardLayoutInformacion);

    jScrollPaneDerechaInformacion.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    jScrollPane2.setBorder(null);
    jTextPaneTransmisor.setAlignmentX((float) 0.0);
    jTextPaneTransmisor.setAlignmentY((float) 0.0);
    jTextPaneTransmisor.setBorder(BorderFactory.createLoweredBevelBorder());
    jTextPaneTransmisor.setMinimumSize(new Dimension(1, 25));
    jTextPaneTransmisor.setPreferredSize(new Dimension(1, 25));
    jTextPaneTransmisor.setEditable(false);
    jTextPaneReceptor.setEnabled(false);
    jTextPaneReceptor.setBorder(BorderFactory.createLoweredBevelBorder());
    jTextPaneReceptor.setEditable(false);
    jTreeInformacion.setPreferredSize(new Dimension(77, 200));
    jSplitPaneInformacion.setLastDividerLocation(160);
    jScrollPaneIzquierdaInformacion.setBorder(null);
    jLabel2.setAlignmentX((float) 10.0);
    jLabel2.setAlignmentY((float) 10.0);
    jLabel2.setPreferredSize(new Dimension(120, 47));
    jLabel2.setHorizontalAlignment(SwingConstants.LEFT);
    jLabel2.setHorizontalTextPosition(SwingConstants.LEFT);
    jLabel2.setIcon(new ImageIcon(JFramePrincipal.class.getResource("images/ptmf.jpg")));
    jLabel2.setText("    ");
    jToolBar2.add(jButtonTxEnviar, null);
    jPanelTransmisor.add(jScrollPane2,  BorderLayout.CENTER);
    jScrollPane2.getViewport().add(jTextPaneTransmisor, null);

    jPanelAbout.add(jScrollPane3, BorderLayout.CENTER);
    jScrollPane3.getViewport().add(jTextArea1, null);
    jPanelAbout.add(jPanel4,  BorderLayout.NORTH);
    jPanel4.add(jLabel2, BorderLayout.WEST);
    jPanel4.add(jLabel1,  BorderLayout.CENTER);
    jPanel4.add(component1, BorderLayout.NORTH);
    jPanel4.add(component7,  BorderLayout.SOUTH);
    jPanelAbout.add(component5,  BorderLayout.EAST);
    jPanelAbout.add(component6,  BorderLayout.WEST);
    jPanelAbout.add(component8, BorderLayout.SOUTH);
    //jPanel4.add(component4, nel4.add(component4,  BorderLayout.NORTH);
    //jPanelAbout.add(component5,  BorderLayout.EAST);
    jTabbedPane1.add(jPanelInfo, "Multicast Tree");
    jTabbedPane1.add(jPanelReceptor, "Receiver");
    jTabbedPane1.add(jPanelTransmisor, "Sender");
    jTabbedPane1.add(jPanelAbout,    "About...");
    jToolBar1.add(jButtonRxCancelar, null);
    jPanelReceptor.add(jTextPaneReceptor,  BorderLayout.CENTER);

    jToolBarINformacion.add(jButtonConectar, null);
    jToolBarINformacion.add(jButtonDesconectar, null);
    jPanelInfo.add(jSplitPaneInformacion, BorderLayout.CENTER);
    jSplitPaneInformacion.add(jScrollPaneDerechaInformacion, JSplitPane.RIGHT);
    jScrollPaneDerechaInformacion.getViewport().add(jPanelInformacion, null);
    jSplitPaneInformacion.add(jScrollPaneIzquierdaInformacion, JSplitPane.LEFT);
    jScrollPaneIzquierdaInformacion.getViewport().add(jTreeInformacion, null);
    jPanelInfo.add(jToolBarINformacion,  BorderLayout.NORTH);
    jPanel1.add(jLabel3, null);
    jPanel1.add(jLabelPTMF, null);
    jPanelContentPanel.add(jPanel2,  BorderLayout.CENTER);
    jPanel2.add(jTabbedPane1,  BorderLayout.CENTER);
    jPanelContentPanel.add(jPanel1,  BorderLayout.NORTH);

    jPanelReceptor.add(jToolBar1,  BorderLayout.SOUTH);
    jPanelTransmisor.add(jToolBar2,  BorderLayout.SOUTH);

    jToolBarINformacion.add(component3, null);
    this.getContentPane().add(jPanelContentPanel, null);
    jPanelInformacion.add(jTextPaneInformacion, "jTextPaneInformacion");
    jPanelInformacion.add(jTextPaneIDGL,  "jTextPaneIDGL");
    jSplitPaneInformacion.setDividerLocation(230);


  }


  void jToggleButtonTxConectar_actionPerformed(ActionEvent e)
  {
    //Conectar.
    //mFtp.getFTP().getJDialogConectar().show();
  }

  /**
   * Evento
   * @param e
   */
  void this_windowClosing(WindowEvent e)
  {
    if( JOptionPane.showConfirmDialog(this,(Object)"¿Really close?","Close",JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION)
    {
      mFtp.getFTP().desconectar();
      System.exit(0);
    }
  }

  /**
   * Evento
   * @param e
   */
  void this_windowClosed(WindowEvent e)
  {
    //System.exit(0);
    this.show();
  }

  /**
   * Acción Botón Conectar.
   * @param e
   */
  void jButtonConectar_actionPerformed(ActionEvent e)
  {
    //Conectar.
    mFtp.getFTP().getJDialogConectar().show();
  }

  /**
   * Acción desconectar
   * @param e
   */
  void jButtonDesconectar_actionPerformed(ActionEvent e)
  {
    mFtp.getFTP().desconectar();

    this.setTitle("Disconnect");
    this.jTreeInformacion.clear();
    //this.jTextPaneInformacion.setText("");
    //copyright();
    logoOff();


  }

  /**
   * Acción Enviar fichero
   * @param e
   * @return
   */
  void jButtonTxEnviar_actionPerformed(ActionEvent e)
  {
     mFtp ftp = mFtp.getFTP();
     //JFileChooser jFileChooser = null;

    if(this.jFileChooser== null)
    {
        this.jFileChooser = new JFileChooser();
    }

    if( this.jFileChooser.showDialog(this,"Send") == JFileChooser.APPROVE_OPTION )
    {

       if(this.jFileChooser.getSelectedFile()==null)
       {
          JOptionPane.showMessageDialog(this,"Any file selected for transmission."+mFtp.newline+"Please, select a file"+mFtp.newline+"You can use double click on the directory tree ","File don't selected.",JOptionPane.INFORMATION_MESSAGE);
          return;
       }

       if (JOptionPane.showConfirmDialog(this,"Send file "+this.jFileChooser.getSelectedFile().getAbsolutePath(),"Confirmación",JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION)
       {
           if (ftp.getSesionMulticast() == null)
           {
             JOptionPane.showMessageDialog(this,"There isn't multicast connection."+mFtp.newline+"Please init a multicast connection"+mFtp.newline+"Push \"Connect\" button"+mFtp.newline+"to init a new multicast connection","Disconnect",JOptionPane.INFORMATION_MESSAGE);
             return;
           }

           ftp.getSesionMulticast().sendFile(this.jFileChooser.getSelectedFile(),this.jFileChooser.getIcon(this.jFileChooser.getSelectedFile()));
       }
       else
       {
         ftp.insertTransmisionString("Transfer cancel by user","icono_informacion");
       }
      }
  }

  void jFileChooser_actionPerformed(ActionEvent e)
  {
      //Poner fichero elegido en jTextPaneTransmision
      //this.jTextPaneTransmisor.setText(jFileChooser.getSelectedFile().getAbsolutePath());
  }

  /**
   * Escribe el copyright
   */
  void copyright()
  {
    mFtp.insertInformacionString(mFtp.COPYRIGHT);
    mFtp.insertInformacionString(" "+mFtp.VERSION);

  }

  //==========================================================================
 /**
  * Inicia la animación....
  */
  void logoOn()
  {
    this.jLabelPTMF.setIcon(ImageIconLogoOn);
    this.jButtonConectar.setEnabled(false);
    this.jButtonDesconectar.setEnabled(true);

    //Limpiar paneles
    this.jTextPaneInformacion.setText("");
    copyright();

    this.jTextPaneReceptor.setText("");
    mFtp.getFTP().insertRecepcionString("Receiver","icono_informacion");

    this.jTextPaneTransmisor.setText("");
    mFtp.getFTP().insertTransmisionString("Sender","icono_informacion");

    if(mFtp.getFTP().esEmisor())
    {
      this.jButtonTxEnviar.setEnabled(true);
      this.jTextPaneTransmisor.setEnabled(true);

      this.jTextPaneReceptor.setEnabled(false);
    }
    else
    {
      this.jButtonTxEnviar.setEnabled(false);
      this.jTextPaneTransmisor.setEnabled(false);

      this.jTextPaneReceptor.setEnabled(true);
    }
  }

 //==========================================================================
 /**
  * Finaliza la animación....
  */
  void logoOff()
  {
    this.jLabelPTMF.setIcon(ImageIconLogoOff);
    this.jButtonConectar.setEnabled(true);
    this.jButtonDesconectar.setEnabled(false);

    this.jButtonTxEnviar.setEnabled(false);

  }

}
