/**
  Fichero: I18n.java	1.0 10/08/99

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
  limitations under the License.
*/

package mchat;


/**
	  Cadenas Internacionales. Soporte de Internacionalizaci�n.
    Definido los siguientes idiomas: (1) Espa�ol, (2) Ingl�s
 */


interface I18n
{
	String[] ADVERTENCIA_SWING_VM = {"!! ADVERTENCIA: Swing necesita una m�quina virtual igual o superior a 1.1.2 !!"
																  ,"!!!WARNING: Swing must be run with a 1.1.2 or higher version VM!!!"};
	String[] TITULO_APLICACION = {"mChat v1.0","mChat v1.0"};

	String[] MENUS = {"Men�s","Menus"};

	//INICIO
	String[] CARGANDO = {"Cargando, por favor espere...","Loading, Please wait..."};
	String[] CARGANDO_MENUS = {"Cargando los men�s...","Loading Menus..."};
	String[] CARGANDO_TOOLBAR = {"Cargando barra de herramientas...","Loading toolbar..."};
	String[] CARGANDO_PANEL_PRINCIPAL = {"Cargando panel principal...","Loading main panel..."};
	String[] CARGANDO_PANEL_INFORMACION = {"Cargando barra de estado...","Loading information panel..."};
	String[] CARGANDO_SPLIT = {"Cargando split...","Loading split..."};

	//Menu Archivo
	String[] MENU_ARCHIVO = {"Archivo","File"};
	String[] TOOLTIP_MENU_ARCHIVO = {"Men� Archivo","File"};
	int[] MNEMONIC_MENU_ARCHIVO = {'A','F'};
	//MenuItem Cerrar
	String[] MENUITEM_CERRAR = {"Cerrar","Close"};
	String[] TOOLTIP_MENUITEM_CERRAR = {"Cerrar","Close"};
  int[] MNEMONIC_MENUITEM_CERRAR = {'C','C'};
  //Menu Ayuda
	String[] MENU_AYUDA = {"Ayuda","Help"};
	String[] TOOLTIP_MENU_AYUDA = {"Men� Ayuda","Help"};
	int[] MNEMONIC_MENU_AYUDA = {'Y','H'};
	//MenuItem Acerca de...
	String[] MENUITEM_ACERCA_DE = {"Acerca de...","About..."};
	String[] TOOLTIP_MENUITEM_ACERCA_DE = {"Acerca de...","About..."};
	int[] MNEMONIC_MENUITEM_ACERCA_DE = {'A','A'};
	//Menu Opciones
	String[] MENU_OPCIONES = {"Opciones","Options"};
	String[] TOOLTIP_MENU_OPCIONES = {"Men� Opciones","Options"};
	int[] MNEMONIC_MENU_OPCIONES = {'O','O'};
	//Menu Edici�n
	String[] MENU_EDICION = {"Edici�n","Edit"};
	String[] TOOLTIP_MENU_EDICION = {"Men� Edici�n","Edit"};
	int[] MNEMONIC_MENU_EDICION = {'E','E'};


 	//About
 String[] ABOUT = {
   "mChat Versi�n 1.0 Copyright 2000\nAutores:\nM.Alejandro Garcia Dominguez (AlejandroGarcia@wanadoo.es)\nAntonio Berrocal Piris (AntonioBP@wanadoo.es)",
   "mChat Versi�n 1.0 Copyright 2000\nAutores:\nM.Alejandro Garcia Dominguez(AlejandroGarcia@wanadoo.es)\nAntonio Berrocal Piris (AntonioBP@wanadoo.es)"};

 	//About
 String[] PRESENTACION = {
   "PTMF: Protocolo de Transporte Multicast Fiable\nChatMulticast Versi�n 1.0 Copyright 2000",
   "PTMF: Protocolo de Transporte Multicast Fiable\nChatMulticast Version 1.0 Copyright 2000"};




	public String iString(String[] StringSet);
}