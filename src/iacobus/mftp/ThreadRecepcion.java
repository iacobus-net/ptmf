//============================================================================
//
//	Copyright (c) 1999 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: ThreadRecepcion.java  1.0 13/04/00
//
// 	Autores: M. Alejandro García Domínguez (AlejandroGarcia@wanadoo.es)
//		 Antonio Berrocal Piris
//
//	Descripción: Clase ThreadRecepcion.
//
//
//----------------------------------------------------------------------------


package iacobus.mftp;

import iacobus.ptmf.*;
import java.io.IOException;

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
