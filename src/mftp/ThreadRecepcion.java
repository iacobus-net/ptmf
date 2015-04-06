/**
  Fichero: ThreadRecepcion.java  1.0 13/04/00
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

package mftp;

import java.io.IOException;

import ptmf.*;

 //==========================================================================
 /**
  * Clase ThreadSesionMulticast. Gestiona la conexión Multicast.
  */
 public class ThreadRecepcion extends  Thread //implements PTMFDatosRecibidosListener
 {

  /** Thread Sesion Multicast */
  private ThreadSesionMulticast threadSesionMulticast = null;

  private ProtocoloFTPMulticast protocoloFTPMulticast = null;
  /** ID_SocketInputStream */
  private ID_SocketInputStream id_socketIn = null;

 //==========================================================================
 /**
  * Constructor
  */
  public ThreadRecepcion( ThreadSesionMulticast threadSesionMulticast,  ID_SocketInputStream id_socketIn)
  {
    super("ThreadRecepcion");

    this.threadSesionMulticast = threadSesionMulticast;
    this.id_socketIn = id_socketIn;

    //Poner como demonio....
    setDaemon(true);
  }





}
