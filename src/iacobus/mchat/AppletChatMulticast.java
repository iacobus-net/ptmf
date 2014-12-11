/*
 * AppletChatMulticast.java	1.0 18/08/99
 *
 * Copyright (c) 1999 . All Rights Reserved.
 * 
 * Autor: M. Alejandro García Domínguez (AlejandroGarcia@wanadoo.es)
 *        Antonio Berrocal Piris.
 *
 * Descripción: Applet para la interfaz genérica.
 * 
 */

package iacobus.mchat;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
//import javax.swing.*;


/** Applet para presentar en un navegador la interfaz SevidorSSL.*/

public class AppletChatMulticast extends JApplet {

	private static MChat chat;

	public AppletChatMulticast() { super(); }

	/** Método init() sobrecargado.*/
	public void init()
	{
		chat = new MChat(this);
	}
	
}