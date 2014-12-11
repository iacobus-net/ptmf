/*
 * AppletFTPMulticast.java	1.0 18/08/99
 *
 * Copyright (c) 1999 . All Rights Reserved.
 *
 * Autor: M. Alejandro García Domínguez (AlejandroGarcia@wanadoo.es)
 *        Antonio Berrocal Piris.
 *
 * Descripción: Applet para la interfaz genérica.
 *
 */

package iacobus.mftp;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
//import javax.swing.*;


/** Applet para presentar en un navegador la interfaz MFtp*/

public class AppletFTPMulticast extends JApplet {

	private static MFtp ftp;

	public AppletFTPMulticast() { super(); }

	/** Método init() sobrecargado.*/
	public void init()
	{
		ftp = new MFtp();
	}



}