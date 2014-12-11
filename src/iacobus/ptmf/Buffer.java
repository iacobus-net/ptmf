//============================================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: Buffer.java  1.0 30/08/99
//
//	Description: Manage data as bytes.
//
//  Historial: 
//	14/10/2014 Change Licence to LGPL
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
//----------------------------------------------------------------------------

package iacobus.ptmf;

import java.io.*;
import java.util.*;


/**
 * Clase para un buffer gen�rico.
 * Esta clase no es thread-safe.
 * @version  1.0
 * @author M. Alejandro Garc�a Dom�nguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */

public class Buffer implements Cloneable
{
  /** El buffer */
  private byte[] buffer = null;

  /** Offset del buffer,<b> utilizar l�bremente, no afecta al funcionamiento
   *  de la clase</b> */
  private int  offset = 0;

  /** Longitud en bytes */
  private int  length = 0;

  /** Longitud m�xima del buffer */
  private int  maxLength = 0;


  //==========================================================================
  /**
   * constructor de copia.
   */
  private Buffer()
  {
    super();
    this.buffer    = null;
    this.offset    = 0;
    this.length    = 0;
    this.maxLength = 0;
  }

  //==========================================================================
  /**
   * Constructor que crea un buffer de un tama�o m�ximo
   * @param maxLen La m�xima longitud del buffer.
   * @exception ParametroInvalidoExcepcion
   * @exception PTMFExcepcion
   */
  public Buffer(int maxLen) throws ParametroInvalidoExcepcion, PTMFExcepcion
  {
    super();

    final String  mn = "Buffer.Buffer(int)";
    this.buffer    = null;
    this.offset    = 0;
    this.length    = 0;
    this.maxLength = 0;

    if (maxLen > 0)
    {
      this.buffer = new byte[maxLen];

      if (this.buffer == null)
      {
        throw new PTMFExcepcion(mn, "Error en reserva de memoria para el array. Longitud muy  grande.");
      }

      this.maxLength = maxLen;
    }
    else
    {
      throw new ParametroInvalidoExcepcion(mn, "maxLen inv�lido");
    }
  }

  //==========================================================================
  /**
   * Constructor que crea un objeto Buffer que encapsula un array de bytes.
   * La longitud y la longitud m�xima del buffer se establece a la longitud
   * del array de bytes pasados.
   * @param bytes Array de bytes que encapsula la clase.
   */
  public Buffer(byte[] bytes)
  {
    super();

    this.buffer    = bytes;
    this.offset    = 0;
    this.length    = bytes.length;
    this.maxLength = bytes.length;
  }

   //==========================================================================
  /**
   * Constructor que crea un objeto Buffer que encapsula un array de bytes.
   * La longitud y la longitud m�xima del buffer se establece a la longitud
   * del array de bytes pasados.
   * @param bytes Array de bytes que encapsula la clase.
   */
  public Buffer(byte[] bytes, boolean copy)
  {

    super();

    this.offset    = 0;
    this.length    = bytes.length;
    this.maxLength = bytes.length;

    byte[] tempArray = null;

    if(copy)
    {
      //������COPIAR DATOS DEL USUARIO!!!!
      tempArray = new byte[bytes.length];
      System.arraycopy(bytes,0,tempArray,0,bytes.length);
      this.buffer = tempArray;
    }
    else
    {
      this.buffer    = bytes;
    }

  }

  //==========================================================================
  /**
   * Constructor que crea un objeto Buffer a partir de otro.
   * <b>Los datos del buffer pueden ser copiados o compartidos.</b>
   * @param buffer Objeto Buffer a copiar.
   * @param copy Este par�metro establece cuando los datos son copiados (true) o
   * son compartidos(false). <b>���Cuidado con los datos compartidos!!!</b>
   */
  public Buffer(Buffer buffer,boolean copy)
  {
    super();

    //
    // Copiar los datos del buffer o compartirlos
    //

     this.offset    = buffer.getOffset();
     this.length    = buffer.getLength();
     this.maxLength = buffer.getMaxLength();

    if (copy)
    {
      this.buffer  = new byte[buffer.getMaxLength()];

      //
      //Copiar los datos
      //

      for(int i=0 ; i<this.length; i++)
        this.buffer[i]=(buffer.getBuffer())[i];
    }
    else
    {
      this.buffer    = buffer.getBuffer();
    }
  }

  //==========================================================================
  /**
   * El m�todo clone
   * @return El nuevo objeto clonado.
   */
  public Object clone()
  {
    final String  mn = "Buffer.clone";
    Buffer        buf = null;

    //
    // Crea un nuevo buffer de la longitud correcta y copia sobre �l el array .
    //

    if ((this.maxLength > 0) && (this.buffer != null))
    {
      try {
        buf = new Buffer(this.maxLength);

        buf.setOffset(this.offset);
        buf.setLength(this.getLength());

        System.arraycopy(this.buffer, 0, buf.buffer, 0, this.maxLength);

      }
      catch(ParametroInvalidoExcepcion e) {
        Log.log(mn, e.getMessage());
        System.exit(-1);
      }
      catch(PTMFExcepcion e) {
        Log.log(mn, e.getMessage());
        System.exit(-1);
      }

    }
    else
    {
      Log.log(mn, "Longitud m�xima o buffer err�neo");
      System.exit(-1);
    }

    return(buf);
  }


  //==========================================================================
  /**
   * Un m�todo para a�adir 8 bytes de un long en orden de host al buffer
   * en orden de red. <p><b>Los 8 bytes son a�adidos en la posici�n del offset.</b>
   * @param l El entero en orden de byte de host.
   * @throws ParametroInvalidoExcepcion cuando el offset es incorrecto
   */
  public void addLong(long l) throws ParametroInvalidoExcepcion
  {
    this.addLong(l,length);
  }

  //==========================================================================
  /**
   * Un m�todo para a�adir 8 bytes de un long en orden de host al buffer
   * en orden de red. <p><b>Los 8 bytes son a�adidos en la posici�n del offset.</b>
   * @param l El entero en orden de byte de host.
   * @param offset El offset dentro del buffer donde se a�adir� el entero.
   * @throws ParametroInvalidoExcepcion cuando el offset es incorrecto
   */
  public void addLong(long l,int offset) throws ParametroInvalidoExcepcion
  {
    final String mn = "Buffer.addLong(long,int)";
    //
    // Verificar el offset
    //

    if (offset+8 > maxLength)
    {
      throw new ParametroInvalidoExcepcion(mn,"�ndice superior a la longitud m�xima del buffer.");
    }


    buffer[offset]     = (byte)(l >>> 54);
    buffer[offset + 1] = (byte)(l >>> 48);
    buffer[offset + 2] = (byte)(l >>> 40);
    buffer[offset + 3] = (byte)(l >>> 32);
    buffer[offset + 4] = (byte)(l >>> 24);
    buffer[offset + 5] = (byte)(l >>> 16);
    buffer[offset + 6] = (byte)(l >>> 8);
    buffer[offset + 7] = (byte)l;


    if(offset+8 > length)
    	length = offset+8;
  }

  //==========================================================================
  /**
   * Un m�todo para a�adir 4 bytes de un entero en orden de host al buffer
   * en orden de red. <p><b>Los 4 bytes son a�adidos en la posici�n del offset.</b>
   * @param l El entero en orden de byte de host.
   * @param offset El offset dentro del buffer donde se a�adir� el entero.
   * @throws ParametroInvalidoExcepcion cuando el offset es incorrecto
   */
  public void addInt(long l,int offset) throws ParametroInvalidoExcepcion
  {
    final String mn = "Buffer.addInt(long,int)";
    //
    // Verificar el offset
    //

    if (offset+4 > maxLength)
    {
      throw new ParametroInvalidoExcepcion(mn,"�ndice superior a la longitud m�xima del buffer.");
    }


    buffer[offset]     = (byte)(l >>> 24);
    buffer[offset + 1] = (byte)(l >>> 16);
    buffer[offset + 2] = (byte)(l >>> 8);
    buffer[offset + 3] = (byte)l;


    if(offset+4 > length)
    	length = offset+4;
  }

	//==========================================================================
  /**
   * Un m�todo para a�adir 4 bytes de un entero en orden de host al buffer
   * en orden de red. <p><b>Los 4 bytes son a�adidos al final del buffer.</b>
   * @param i El entero en orden de byte de host.
   * @throws InvalidParameterException cuando el dato no cabe en el buffer
   */
  public void addInt(int i) throws ParametroInvalidoExcepcion
  {
    	addInt(i,length);
  }

  //==========================================================================
  /**
   * Un m�todo para a�adir 2 bytes de un short en orden de host al buffer
   * en orden de red. <p><b>Los 2 bytes son a�adidos en la posici�n del offset.</b>
   * @param i El short en orden de byte de host.
   * @param offset El offset dentro del buffer donde se a�adir� el entero.
   * @throws ParametroInvalidoExcepcion cuando el offset es incorrecto
   */
  public void addShort(int i,int offset) throws ParametroInvalidoExcepcion
  {
    final String mn="Buffer.addShort";
    //
    // Verificar el offset
    //

    if (offset+2 > maxLength)
    {
      throw new ParametroInvalidoExcepcion(mn,"�ndice superior a la longitud m�xima del buffer.");
    }


    buffer[offset]     = (byte)(i >>> 8);
    buffer[offset + 1] = (byte)i;

    if(offset+2 > length)
    	length = offset+2;
  }

 	//==========================================================================
  /**
   * Un m�todo para a�adir 2 bytes de un entero en orden de host al buffer
   * en orden de red. <p><b>Los 2 bytes son a�adidos al final del buffer.</b>
   * @param i El short en orden de byte de host.
   * @throws ParametroInvalidoExcepcion cuando el dato no cabe en el buffer
   */
  public void addShort(int i) throws ParametroInvalidoExcepcion
  {
    	addShort(i,length);
  }

  //==========================================================================
  /**
   * Un m�todo para a�adir un byte a un buffer
   * @param i El byte
   * @param offset El offset dentro del buffer donde se a�adir� el entero.
   * @throws ParametroInvalidoExcepcion cuando el dato no cabe en el buffer
   */
  public void addByte(byte i,int offset) throws ParametroInvalidoExcepcion
  {
    final String mn = "Buffer.addByte";
    //
    // Verificar el offset
    //
    if (offset+1 > maxLength)
    {
      throw new ParametroInvalidoExcepcion(mn,"�ndice superior a la longitud m�xima del buffer.");
    }


    buffer[offset]     = (byte)(i);

    if(offset+1 > length)
    	length = offset+1;
  }

  //==========================================================================
  /**
   * Un m�todo para a�adir 1 byte de un entero en orden de host al buffer
   * en orden de red. <p><b>El byte es a�adido al final del buffer.</b>
   * @param i El byte en orden de byte de host.
   * @throws ParametroInvalidoExcepcion cuando el dato no cabe en el buffer
   */
  public void addByte(byte i) throws ParametroInvalidoExcepcion
  {
    	addByte(i,length);
  }

  //==========================================================================
  /**
   * Un m�todo para a�adir el contenido de un buffer (buffer fuente) a este
   * buffer.
   * @param srcBuffer Buffer fuente
   * @param srcOffset Offset buffer fuente.
   * @param dstOffset Offset del buffer destino donde se comenzar� la copia.
   * @param length N�mero de bytes a copiar.
   */
  public void addBytes(Buffer srcBuffer, int srcOffset,int dstOffset, int length)
  {
    System.arraycopy(srcBuffer.getBuffer(),srcOffset,
                       buffer,dstOffset , length);
    this.length += length;

  }

  //==========================================================================
  /**
   * Un m�todo para a�adir una array de chars a este buffer.
   * @param aChar Array de Chars
   * @param iOffset Offset
   */
  public void addArrayChar(char[] aChar, int iOffset) throws ParametroInvalidoExcepcion
  {
    for(int i = 0; i<aChar.length; i++)
    {
      int iChar = aChar[i];
      this.addShort(iChar,iOffset+(i*2));
    }

    this.length += aChar.length*2;
  }
  //==========================================================================
  /**
   * Un m�todo para a�adir el contenido de un buffer (buffer fuente) <b> al
   * final del contenido de este buffer</b>. Se copia tanto como se pueda.
   * @param srcBuffer Buffer fuente
   * @param srcOffset Offset buffer fuente.
   * @param length N�mero de bytes a copiar.
   */
  public void addBytes(Buffer srcBuffer, int srcOffset, int length)
  {
    System.arraycopy(srcBuffer.getBuffer(), srcOffset,
                       buffer,this.length , length);
    this.length += length;

  }

  //==========================================================================
  /**
   * Un m�todo para extraer 4 bytes de un entero de un buffer usando offset
   * @param offset un offset adicional usado para localizar el entero en el buffer
   * @return  El entero en orden de byte de host
   * @throws ParametroInvalidoExcepcion cuando el offset es incorrecto
   */
  public long getInt(int offset) throws ParametroInvalidoExcepcion
  {
    final String mn = "Buffer.getInt(int)";
    //
    // Verificar el offset
    //
     if (offset + 4 > maxLength)
    {
      throw new ParametroInvalidoExcepcion(mn,"�ndice superior a la longitud m�xima del buffer." );
    }


    long l=((((long)buffer[offset] & 0xff) << 24) |
           (((long)buffer[offset + 1] & 0xff) << 16) |
           (((long)buffer[offset + 2] & 0xff) << 8) |
           ((long)buffer[offset + 3] & 0xff));

    return l;

 }

  //==========================================================================
  /**
   * Un m�todo para obtener una array de chars a este buffer.
   * @param iOffset Offset
   * @param iLongitud Numero de chars a copiar. Tenga en cuenta
   *                  que un char son 16 bits (2 bytes)
   */
  public char[] getArrayChar(int iOffset, int iLongitud) throws ParametroInvalidoExcepcion
  {
    char[] aChar = new char[iLongitud];

    for(int i = 0; i<iLongitud; i++)
    {
      aChar[i] =  (char)this.getShort(iOffset+i*2);
    }

    return aChar;
  }
 //==========================================================================
  /**
   * Un m�todo para extraer 4 bytes de un entero de un buffer usando offset
   * @param offset un offset adicional usado para localizar el entero en el buffer
   * @return  El entero en orden de byte de host
   * @throws ParametroInvalidoExcepcion cuando el offset es incorrecto
   */
  public long getLong(int offset) throws ParametroInvalidoExcepcion
  {
    final String mn = "Buffer.getLong(int)";
    //
    // Verificar el offset
    //
     if (offset + 8 > maxLength)
    {
      throw new ParametroInvalidoExcepcion(mn,"�ndice superior a la longitud m�xima del buffer." );
    }


    long l=((((long)buffer[offset] & 0xff) << 56) |
           (((long)buffer[offset + 1] & 0xff) << 48) |
           (((long)buffer[offset + 2] & 0xff) << 40) |
           ((long)buffer[offset + 3] & 0xff) << 32)|
           ((((long)buffer[offset +4] & 0xff) << 24) |
           (((long)buffer[offset + 5] & 0xff) << 16) |
           (((long)buffer[offset + 6] & 0xff) << 8) |
           ((long)buffer[offset + 7] & 0xff));

    return l;

 }

 	//==========================================================================
  /**
   * Un m�todo para extraer 2 bytes de un short de un buffer usando offset
   * @param offset un offset adicional usado para localizar el entero en el buffer
   * @return  El short en orden de byte de host
   * @throws ParametroInvalidoExcepcion cuando el offset es incorrecto
   */
  public  int getShort(int offset) throws ParametroInvalidoExcepcion
  {
    final String mn = "Buffer.getShort(int)";
    //
    // Verificar el offset
    //
    if (offset + 2 > maxLength)
    {
      throw new ParametroInvalidoExcepcion(mn,"�ndice superior a la longitud m�xima del buffer.");
    }


    return( ((buffer[offset] & 0xff) << 8) |(buffer[offset + 1] & 0xff));
  }

 	//==========================================================================
  /**
   * Un m�todo para extraer 1 byte de un entero de un buffer usando offset
   * @param offset Un offset adicional usado para localizar el byte en el buffer
   * @return  El byte
   * @throws ParametroInvalidoExcepcion cuando el offset es incorrecto
   */
  public  short getByte(int offset) throws ParametroInvalidoExcepcion
  {
    final String mn = "Buffer.getByte(int)";
    //
    // Verificar el offset
    //
    if (offset + 1 > maxLength)
    {
      throw new ParametroInvalidoExcepcion(mn,"�ndice superior a la longitud m�xima del buffer.");
    }

    return((short)(buffer[offset] & 0xff));
  }

  //==========================================================================
  /**
   * Un m�todo para extraer un array de bytes del buffer usando un offset
   * @param offset Un offset adicional usado para localizar el primer byte
   *  en el buffer que se debe de copiar
   * @param leng N� de bytes a copiar.
   * @return  El array de bytes
   * @throws ParametroInvalidoExcepcion cuando el offset es incorrecto
   */
  public byte[] getBytes(int offset,  int leng) throws ParametroInvalidoExcepcion
  {
    final String mn = "Buffer.getBytes(int,int)";
    byte[] buf = null;

    //
    // Verificar el offset
    //
    if (offset + leng > maxLength)
    {
      throw new ParametroInvalidoExcepcion(mn,"�ndice superior a la longitud m�xima del buffer.");
    }

    buf = new byte[leng];
    //Log.debug(Log.TPDU_CGL,mn,"Param offset: "+offset+"Param len: "+leng+" buf.length:"+buf.length+" buffer.length:"+buffer.length);

    //for (int i=0; i<leng; i++)
    //  buf[i] = buffer[offset+i];
    System.arraycopy(buffer,offset,buf,0,leng);

    return(buf);
  }

  //==========================================================================
  /**
   * Devuelve el buffer encapsulado
   * @return Un array de bytes.
   */
   public byte[] getBuffer() { return buffer;}

  //==========================================================================
  /**
   * Devuelve el n�mero de bytes en el buffer
   * @return Un entero indicando el n�mero de bytes.
   */
   public int getLength() { return length;}

  //==========================================================================
  /**
   * Devuelve la capacidad del buffer
   * @return Un entero indicando el n�mero de bytes que caben en el buffer.
   */
   public int getMaxLength() { return maxLength;}

  //==========================================================================
  /**
   * Devuelve el offset
   * @return Un entero indicando el offset del buffer.
   */
   public int getOffset() { return offset;}

  //==========================================================================
  /**
   * Establece el offset
   * @param offset El offset del buffer.
   */
   public void setOffset(int offset) {this.offset=offset;}

  //==========================================================================
  /**
   * Establece el n�mero de bytes que hay en el buffer, no el tama�o.<p>
   * <b>NOTA: ��EL COMETIDO DE ESTE M�TODO ES AJUSTAR EL TAMA�O DEL BUFFER
   * CUANDO HA SIDO MODIFICADO FUERA DE LA CLASE, por ejemplo cuando se ha
   * llamado al m�todo getBuffer() y este se ha modificado. <P>
   * NO SE RECOMIENDA SU USO, SALVO CASO IMPRESCINDIBLE, PARA AGREGAR DATOS
   * UTILICE LOS M�TODOS DE LA CLASE QUE AJUSTAN AUTOM�TICAMENTE LA LONGITUD
   * DEL CAMPO length.</b>
   *
   * @param offset El n�mero de bytes en el buffer.
   */
   public void setLength(int length) {this.length=length;}

   public void setMaxLength(int length)
    {
     if (this.length > this.maxLength)
        this.length =  this.maxLength;
     this.maxLength=length;

     if(this.maxLength > this.buffer.length)
      this.maxLength =this.buffer.length;
    }


  //==========================================================================
  /**
   * Resetea el contenido del buffer. Establece a cero la longitud del
   * contenido, el offset y rellena el buffer con ceros. El tama�o del buffer
   * no se modifica.
   */
   public void reset() {

      //
      // Resetear el buffer.
      //

      for(int i=0; i<buffer.length; i++)
        buffer[i]=0;

      length=0;
      offset=0;
   }

  //==========================================================================
  /**
   * devuelve una cadena identificativa del Buffer
   * @return Cadena identificativa de los primeros bytes del buffer.
   */
   public String toString()
   {
    String cad  = new String();

/*    int maxIteracciones = (this.maxLength > 10)? 10 : this.maxLength;

    try
    {
     for(int i = 0; i<maxIteracciones ; i++)
     {
      cad +="\n["+i+"]: "+this.getByte(i);
     }
    }
    catch(IOException e){;}
*/
    return new String (this.buffer);
   }

}