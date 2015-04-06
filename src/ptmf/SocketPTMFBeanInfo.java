
//Title:        PTMF
//Version:      
//Copyright:    Copyright (c) 1998,2014
//Author:       Antonio Berrocal Piris
//Company:      
//Description:  Protocolo de Transporte Multicast Fiable
//  Historial: 
//	14/10/2014 Change Licence to LGPL
//
// 	Authors: 
//		 Alejandro Garcia Dominguez (alejandro.garcia.dominguez@gmail.com)
//		 Antonio Berrocal Piris (antonioberrocalpiris@gmail.com)
//
//
//      This file is part of PTMF 
//
//      PTMF is free software: you can redistribute it and/or modify
//      it under the terms of the Lesser GNU General Public License as published by
//      the Free Software Foundation, either version 3 of the License, or
//      (at your option) any later version.
//
//      PTMF is distributed in the hope that it will be useful,
//      but WITHOUT ANY WARRANTY; without even the implied warranty of
//      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//      Lesser GNU General Public License for more details.
//
//      You should have received a copy of the Lesser GNU General Public License
//      along with PTMF.  If not, see <http://www.gnu.org/licenses/>.

package ptmf;

import java.beans.*;

public class SocketPTMFBeanInfo extends SimpleBeanInfo
{
  Class beanClass = SocketPTMF.class;
  String iconColor16x16Filename;
  String iconColor32x32Filename;
  String iconMono16x16Filename;
  String iconMono32x32Filename;

  
  public SocketPTMFBeanInfo()
  {
  }

  public PropertyDescriptor[] getPropertyDescriptors()
  {
    try 
    {
      PropertyDescriptor _capacidadColaEmision = new PropertyDescriptor("capacidadColaEmision", beanClass, "getCapacidadColaEmision", "setCapacidadColaEmision");
      
      PropertyDescriptor _capacidadColaRecepcion = new PropertyDescriptor("capacidadColaRecepcion", beanClass, "getCapacidadColaRecepcion", "setCapacidadColaRecepcion");
      
      PropertyDescriptor _ID_Sockets = new PropertyDescriptor("ID_Sockets", beanClass, "getID_Sockets", null);
      
      PropertyDescriptor _IDGLs = new PropertyDescriptor("IDGLs", beanClass, "getIDGLs", null);
      
      PropertyDescriptor _multicastInputStream = new PropertyDescriptor("multicastInputStream", beanClass, "getMulticastInputStream", null);
      
      PropertyDescriptor _multicastOutputStream = new PropertyDescriptor("multicastOutputStream", beanClass, "getMulticastOutputStream", null);
      
      PropertyDescriptor[] pds = new PropertyDescriptor[] {
        _capacidadColaEmision,
        _capacidadColaRecepcion,
        _ID_Sockets,
        _IDGLs,
        _multicastInputStream,
        _multicastOutputStream,
      };
      return pds;
    }
    catch (IntrospectionException ex)
    {
      ex.printStackTrace();
      return null;
    }
  }

  public java.awt.Image getIcon(int iconKind)
  {
    switch (iconKind) {
    case BeanInfo.ICON_COLOR_16x16:
      return iconColor16x16Filename != null ? loadImage(iconColor16x16Filename) : null;
    case BeanInfo.ICON_COLOR_32x32:
      return iconColor32x32Filename != null ? loadImage(iconColor32x32Filename) : null;
    case BeanInfo.ICON_MONO_16x16:
      return iconMono16x16Filename != null ? loadImage(iconMono16x16Filename) : null;
    case BeanInfo.ICON_MONO_32x32:
      return iconMono32x32Filename != null ? loadImage(iconMono32x32Filename) : null;
    }
    return null;
  }
}

 