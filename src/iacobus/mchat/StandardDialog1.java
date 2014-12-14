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
  limitations 
  */

package  iacobus.mchat;

import java.awt.*;
import java.awt.event.*;

import javax.swing.JPanel;

//import borland.jbcl.layout.*;
//import borland.jbcl.control.*;

public class StandardDialog1 extends Dialog {
  Panel panel1 = new Panel();
  FlowLayout xYLayout1 = new FlowLayout();
  JPanel bevelPanel1 = new JPanel();
  Button button1 = new Button();
  Button button2 = new Button();
  Label label1 = new Label();
  ScrollPane scrollPane1 = new ScrollPane();
  List list1 = new List();
  TextField textField1 = new TextField();
  Checkbox checkbox1 = new Checkbox();
  CheckboxGroup checkboxGroup1 = new CheckboxGroup();

  public StandardDialog1(Frame frame, String title, boolean modal) {
    super(frame, title, modal);
    try {
      jbInit();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    add(panel1);
    pack();
  }

  public StandardDialog1(Frame frame, String title) {
    this(frame, title, false);
  }

  public StandardDialog1(Frame frame) {
    this(frame, "", false);
  }

  private void jbInit() throws Exception {
    //xYLayout1.setWidth(320);
    //xYLayout1.setHeight(243);
    button1.setLabel("OK");
    button1.addActionListener(new StandardDialog1_button1_actionAdapter(this));
    button2.setLabel("Cancel");
    label1.setBounds(new Rectangle(264, 29, 96, 22));
    label1.setText("TTLSesion");
    scrollPane1.setBounds(new Rectangle(267, 52, 101, 91));
    list1.setBounds(new Rectangle(-3, 9, 104, 69));
    textField1.setBounds(new Rectangle(44, 55, 148, 26));
    textField1.setText("textField1");
    checkbox1.setBounds(new Rectangle(67, 201, 81, 21));
    checkbox1.setLabel("checkbox1");
    button2.addActionListener(new StandardDialog1_button2_actionAdapter(this));
    this.addWindowListener(new StandardDialog1_this_windowAdapter(this));
    panel1.setLayout(xYLayout1);
    panel1.add(bevelPanel1); //, new XYConstraints(9, 10, 298, 191));
    panel1.add(button1); //, new XYConstraints(67, 211, 74, 25));
    panel1.add(button2); //, new XYConstraints(163, 211, 77, 25));
    panel1.add(label1, null);
    panel1.add(scrollPane1, null);
    scrollPane1.add(list1, null);
    panel1.add(textField1, null);
    panel1.add(checkbox1, null);
  }                                                    
  // OK
  void button1_actionPerformed(ActionEvent e) {
    dispose();
  }

  // Cancel
  void button2_actionPerformed(ActionEvent e) {
    dispose();
  }
  
  void this_windowClosing(WindowEvent e) {
    dispose();
  }
}

class StandardDialog1_button1_actionAdapter implements ActionListener {
  StandardDialog1 adaptee;

  StandardDialog1_button1_actionAdapter(StandardDialog1 adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.button1_actionPerformed(e);
  }
}

class StandardDialog1_button2_actionAdapter implements ActionListener {
  StandardDialog1 adaptee;

  StandardDialog1_button2_actionAdapter(StandardDialog1 adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.button2_actionPerformed(e);
  }
}

class StandardDialog1_this_windowAdapter extends WindowAdapter {
  StandardDialog1 adaptee;

  StandardDialog1_this_windowAdapter(StandardDialog1 adaptee) {
    this.adaptee = adaptee;
  }

  public void windowClosing(WindowEvent e) {
    adaptee.this_windowClosing(e);
  }
}
                
