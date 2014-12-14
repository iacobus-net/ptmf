/**
  AppletChatMulticast.java	1.0 18/08/99-2014

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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
//import javax.swing.*;


/** Applet para presentar en un navegador la interfaz SevidorSSL.*/

public class AppletChatMulticast extends JApplet {

	private static mChat chat;

	public AppletChatMulticast() { super(); }

	/** Método init() sobrecargado.*/
	public void init()
	{
		chat = new mChat(this);
	}
	
}