//============================================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: SocketPTMFBeanInfo.java  1.0 24/09/99
//
//
//	Descripción: Bean info.
//
// 	Authors: 
//		 Alejandro García-Domínguez (alejandro.garcia.dominguez@gmail.com)
//		 Antonio Berrocal Piris (antonioberrocalpiris@gmail.com)
//
//  Historial: 
//  07.04.2015 Changed licence to Apache 2.0     
//
//  This file is part of PTMF 
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//----------------------------------------------------------------------------

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

 