//============================================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	File: Addres.java  1.0 30/08/99
//
//	Description: Clase Address. Encapsula InetAddres y un puerto TCP/UDP
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
//
//
//----------------------------------------------------------------------------

package ptmf;

import java.io.*;
import java.util.*;
import java.net.*;
import java.lang.Comparable;


/**
 * Clase que encapsula una direccion IP y un puerto TCP/UDP.
 * Esta clase no es thread-safe.
 * @version  1.0
 * @author M. Alejandro Garc�a Dom�nguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */

public class Address implements Cloneable,Comparable
{
  // ATRIBUTOS

  /** La direcci�n internet */
  private InetAddress  inetAddress = null;

  /** El n�mero de puerto */
  private int  inetPort = 0;


  //==========================================================================
  /**
   * constructor por defecto.
   * @exception UnknownHostException La direcci�n "0.0.0.0" no pudo ser
   * resuelta, indica un fallo en el sistema de red.
   */
  public Address() throws UnknownHostException
  {
    super();
    final String mn = "Address.Address()";

    this.inetAddress = InetAddress.getByName("0.0.0.0");
  }

  //==========================================================================
  /**
   * Constructor. Establece la direcci�n y el puerto.
   * @param s Direcci�n del host.
   * @param puerto Puerto TCP/UDP
   * @exception UnknownHostException No se pudo resolver la direcci�n del host
   *  especificado.
   */
  public Address(String s,int puerto) throws UnknownHostException
  {
    super();

    this.inetAddress = InetAddress.getByName(s);
    inetPort = puerto;
  }
  //==========================================================================
  /**
   * Constructor. Establece la direcci�n y el puerto.
   * @param s Direcci�n del host.
   * @param puerto Puerto TCP/UDP
   * @exception UnknownHostException No se pudo resolver la direcci�n del host
   *  especificado.
PROBAR SI HACE RESOLUCI�N DE NOMBRES (DNS)
   */
  public Address(ID_Socket id_socket) throws UnknownHostException
  {
    super();
    final String mn = "Address.Address (id_socket)";

    // No se hace resoluci�n de nombres (DNS)
    this.inetAddress = InetAddress.getByName(id_socket.getDireccion().getHostAddress());
    inetPort = id_socket.getPuertoUnicast();


  }

  //==========================================================================
  /**
   * Constructor. Establece la direcci�n y el puerto.
   * @param dir Direcci�n del host.
   * @param puerto Puerto TCP/UDP
   * @exception ParametroInvalidoExcepcion lanzada si dir es null
   */
  public Address(InetAddress dir,int puerto) throws ParametroInvalidoExcepcion
  {
    super();

    final String mn = "Address.Address (InetAddress , int )";

    if (dir==null)
      throw new ParametroInvalidoExcepcion (mn,"dir no puede ser null.");

    this.inetAddress = dir;
    inetPort = puerto;
  }

  //==========================================================================
  /**
   * M�todo clone
   * @return El nuevo objeto clonado
   */
  public Object clone()
  {
    final String  mn = "Address.clone()";
    Address       addr = null;

    //
    // Clonar la direcci�n y el puerto.
    //

    if (this.inetAddress != null)
    {
      try{
        addr = new Address();
        addr.inetAddress = this.inetAddress;
//            InetAddress.getByName(this.inetAddress.getHostAddress());
      }
      catch(UnknownHostException e)
      { //Log.debug (Log.SOCKET,mn,"Direcci�n de host desconocida");
      ;}


      addr.inetPort = this.inetPort;
    }

    return(addr);
  }

  //==========================================================================
  /**
   * Establece el puerto UDP
   * @param El nuevo puerto
   */
  public void setPort(int port)
  {
    this.inetPort = port;
  }

  //==========================================================================
  /**
   * Establece el n�mero de puerto a "any"=0.
   */
  public void setAnyPort()
  {
    //
    // Establece el n�mero de puerto a cero.
    //
    this.inetPort = 0;
  }

  //==========================================================================
  /**
   * Establece la direcci�n
   * @param La nueva direcci�n como una cadena
   * @exception UnknownHostException
   */
  public void setAddress(String address) throws UnknownHostException
  {
    this.inetAddress = InetAddress.getByName(address);
  }

  //==========================================================================
  /**
   * Establece la direcci�n a "any".
   * @exception UnknownHostException
   */
  public void setAnyAddress() throws  UnknownHostException
  {
    //
    // Establece la direcci�n a "0.0.0.0".
    //
    this.inetAddress = InetAddress.getByName("0.0.0.0");
  }

  //==========================================================================
  /**
   * devuelve el n�mero de puerto
   * @return El n�mero de puerto
   */
  public int getPort()
  {
    return(this.inetPort);
  }

  //==========================================================================
  /**
   * devuelve la direcci�n como una cadena "x.x.x.x".
   * @return La direcci�n como una cadena
   */
  public String getHostAddress()
  {
    return(this.inetAddress.getHostAddress());
  }

  //==========================================================================
  /**
   * devuelve una cadena con el nombre del host.
   * @return El nombre del host
   */
  public String getHostName()
  {
    return(this.inetAddress.getHostName());
  }

  static public Address getLocalHost ()
  {
   return null;
  }


  //==========================================================================
  /**
   * devuelve la direcci�n IP como un objeto InetAddress.
   * @return LA direcci�n IP como objeto InetAddress
   */
  public InetAddress getInetAddress()
  {
    return(this.inetAddress);
  }

  //==========================================================================
  /**
   * Chequea si la direcci�n es multicast
   * @return True si la direcci�n es multicast, falso en caso contrario.
   */
  public boolean isMulticastAddress()
  {
    return(this.inetAddress.isMulticastAddress());
  }


  //==========================================================================
  /**
   * Implementaci�n del m�todo de la interfaz Comparable.
   * Compara la direcci�n IP y si son iguales compara el puerto.
   * @param o Direcci�n con la que se compara.
   * @return mayor que cero si esta direcci�n es mayor que la pasada en el
   * argumento, menor que cero si es menor y cero si son iguales.
   */
 public int compareTo(Object o)
 {
    String dir = ((Address) o).getInetAddress().getHostAddress();
    String estaDir = this.getInetAddress().getHostAddress();

    // Compara las direcciones IP.
    if (estaDir.compareTo(dir)<0)
        return -1;
    if (estaDir.compareTo(dir)>0)
        return 1;

    // La direcciones IP son iguales.
    // Comparar el puerto.
    if ( this.getPort()<((Address) o).getPort() )
        return -1;
    if ( this.getPort()>((Address) o).getPort() )
        return 1;

    // La direcci�n IP y el n�mero de puerto son iguales.
    return 0;

 }

   //=================================================================
   /**
    * Comprueba si esta direcci�n es igual a la pasada por par�metro.
    * Dos direcciones son iguales si tienen la misma direcci�n IP y el
    * el mismo puerto.
    * @param o Direcci�n con la que se compara.
    * @return true si son iguales, y false en caso contrario.
    */
   public boolean equals (Object o)
   {
    Address dir = (Address) o;

    if ( (this.inetAddress.equals(dir.inetAddress)) &&
         (this.getPort () == dir.getPort ()) )
        return true;

    return false;
   }

    /**
	 * Comprueba si esta direcci�n es igual a la pasada por par�metro.
	 * Dos direcciones son iguales si tienen la misma direcci�n IP y el
	 * el mismo puerto. en este caso el puerto no se compara
	 * @param o Direcci�n con la que se compara.
	 * @return true si son iguales, y false en caso contrario.
	 */
	public boolean equals (InetAddress inet)
	{

		byte[] inet1 = this.inetAddress.getAddress();
		byte[] inet2 = inet.getAddress();

		for(int i = 0; i <= inet1.length-1; i++)
		{
			if (inet1[i] != inet2[i])
			{
				return false;
			}
		}

		return true;
	}
  //==================================================================
  /**
   * Devuelve un c�digo hash para esta address.
   */
  public int hashCode ()
  {
   // El c�digo devuelto es la direcci�n IP del host, por lo que dos hosts
   // con la misma direcci�n IP, tendr�n el mismo c�digo hash.
   return this.inetAddress.hashCode ();
  }

  //==================================================================
  /**
   * Devuelve una instancia de ID_Socket que representa esta direcci�n
   */
  public ID_Socket toID_Socket ()
  {
   Buffer buf = new Buffer (this.inetAddress.getAddress ());
   return new ID_Socket (new IPv4 (buf),this.inetPort);
  }


  //==================================================================
  /**
   * Devuelve una instancia de IDGL que representa esta direcci�n
   */
  public IDGL toIDGL()
  {
    Buffer buf = null;
    try
    {
      buf = new Buffer(7);
      buf.addBytes(new Buffer(this.inetAddress.getAddress()),0,0,4);
      buf.addShort(this.getPort(),5);

    }
    catch(PTMFExcepcion e){;}
    catch(ParametroInvalidoExcepcion e){;}

    return new IDGL(buf,(byte)0/*+ttl*/);
  }

  //==================================================================
  /**
   * Devuelve una instancia de ID_Socket que representa esta direcci�n
   */
  public IPv4 toIPv4 ()
  {
   Buffer buf = new Buffer (this.inetAddress.getAddress ());
   return new IPv4 (buf);
  }


 //======================================================================
 /**
  * Devuelve una cadena informativa.
  * @return cadena con la direcci�n del Host.
  */
 public String toString ()
 {
   return "["+this.toIPv4().toString()+":" + this.inetPort+"]";
 }

 static void main (String[] args)
 {
  final String mn = "Address.main";

  try {

  Address dirLocal = new Address (InetAddress.getLocalHost(),299);

  ID_Socket id_Socket = dirLocal.toID_Socket ();
  } catch (ParametroInvalidoExcepcion e) {Log.log (mn,e.toString());}
    catch (UnknownHostException e) {Log.log (mn,e.toString());}
 } // Fin de main

} // Fin de Address
