//============================================================================
//
//	Copyright (c) 1999 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	File: IDGL.java  1.0 21/10/99
//
//
//	Description: IDGL
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
//----------------------------------------------------------------------------

package iacobus.ptmf;

import java.util.Comparator;

/**
 * IDGL encapsula el identificador del grupo local:
 *  IDGL = IPv4 + Puerto Unicast
 *
 *
 * y el TTL que indica la distancia hacia ese grupo local.
 * @version  1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */
public class IDGL implements Cloneable ,Comparable{

  /** identificador de grupo local*/
  Buffer id = null;

  /** TTL del identificador de grupo local */
  short TTL = 0;

  /** String ID */
  private String sID = null;

  /** HashCode */
  private int iHashCode = 0;

  //==========================================================================
  /**
   * Constructor.
   * @param id Buffer de bytes del identificador de grupo
   * @param ttl TTL del id de grupo.
   */
  IDGL(Buffer id, byte ttl)
  {
    super();

    this.id = id;
    if(id.getMaxLength() < 6)
    {
      //Error fatal.
      Log.log("IDGL()","ERROR FATAL: IDGL menor de 6 bytes.");
      Log.exit(-1);
    }

    this.TTL = ttl;
    iHashCode = getHashCode();
    sID = getsID();

  }

  //==========================================================================
  /**
   * Constructor sin parámetros.
   * Crea un id 0.0.0.0.0.0 y un TTL 0
   */
 /* IDGL()
  {
    super();

    try
    {
      this.id = new Buffer(6);
      id.addByte((byte)0,0);
      id.addByte((byte)0,1);
      id.addByte((byte)0,2);
      id.addByte((byte)0,3);
      id.addByte((byte)0,4);
      id.addByte((byte)0,5);
    }
    catch(PTMFExcepcion e){;}
    catch(ParametroInvalidoExcepcion e){;}

    this.TTL = 0;

    iHashCode = getHashCode();
    sID = getsID();

  }
   */
  //==========================================================================
  /**
   * Implementación de la interfaz Cloneable. Método clone
   * @return El nuevo objeto clonado.
   */
  public Object clone()
  {
     return new IDGL((Buffer)this.id.clone(),(byte)this.TTL);
  }

  //==========================================================================
  /**
   * Devuelve el identificador de grupo como una cadena x.x.x.x.X.X
   * @return Una cadena del identificador de grupo en formato x.x.x.x.X.X
   */
   String getsID()
   {
     short A = 0;
     short B = 0;
     short C = 0;
     short D = 0;
     short P1 = 0;
     short P2 = 0;

     try
     {
       A = this.id.getByte(0);
       B = this.id.getByte(1);
       C = this.id.getByte(2);
       D = this.id.getByte(3);
       P1 = this.id.getByte(4);
       P2 = this.id.getByte(5);
     }
     catch(ParametroInvalidoExcepcion e)
     {
      ;
     }

     //Obtención del puerto
     int puerto = P1;
     puerto = puerto << 8;
     puerto |= P2;

     String ID = A+"."+B+"."+C+"."+D+":"+puerto; //P1+"."+P2  ;

     return ID;
   }

  //==========================================================================
  /**
   * Devuelve el identificador de grupo como una cadena x.x.x.x.X.X
   * @return Una cadena del identificador de grupo en formato x.x.x.x.X.X
   */
   String getStringID() { return this.sID;}

  //==========================================================================
  /**
   * Devuelve el código hash para este IDGL.
   * El código hash se basa sólo en el identificador del grupo local.
   * @return el código hash para el IDGL
   */
  public int hashCode(){ return this.iHashCode;}

  //==========================================================================
  /**
   * Devuelve el código hash para este IDGL.
   * El código hash se basa sólo en el identificador del grupo local.
   * @return el código hash para el IDGL
   */
  public int getHashCode()
  {
    try
    {
     if ( (this.id != null) && (this.id.getBuffer() != null))
      return ((int)this.id.getInt(2));
     else
       return 0;

    }

    catch(ParametroInvalidoExcepcion e){;}

    return 0;
  }

  //==========================================================================
  /**
   * Este método verifica si el objeto pasado como argumento es igual a este
   * objeto. Se comparan el TTL y el identificador del grupo local.
   * @param obj Objeto a comparar con este.
   * @return true si el objeto es igual, false en caso contrario.
   */
  public boolean equals(Object obj)
  {
    IDGL idgl = (IDGL) obj;

    //if (this.TTL == idgl.TTL)
    //{
      for (int i=0; i< 6; i++)
       if (this.id.getBuffer()[i] != idgl.id.getBuffer()[i])
        return false;

      return true;
    //}
    //return false;
  }

  //==========================================================================
  /**
   * Implementación del método de la interfaz Comparable.
   * Compara primero la dirección y después el número de secuencia.
   * @param o IDGL con la que se compara.
   * @return mayor que cero si este IDGL es mayor que el pasado en el
   * argumento, menor que cero si es menor y cero si son iguales.
   */
 public int compareTo(Object o)
 {
    int hash1 = 0;
    int hash2 = 0;
    IDGL idgl = (IDGL) o;

    hash1 = this.hashCode();
    hash2 = idgl.hashCode();

    if ( hash1 < hash2 )
        return -1;
    else if ( hash1 > hash2)
        return 1;
    else return 0;

 }
  //==========================================================================
  /**
   * Este método compara dos objetos pasados como argumentos.
   * @param o1 Objeto 1 a comparar
   * @param o2 Objeto 2 a comparar
   * @return Devuelve un entero negativo, cero o un entero positivo si el
   *   primer argumento es menor que, igual a, o mayor que el segundo.
   */
  public int compare(Object o1, Object o2)
  {
    int hash1 = 0;
    int hash2 = 0;

    hash1 = o1.hashCode();
    hash2 = o2.hashCode();

    if ( hash1 < hash2 )
        return -1;
    else if ( hash1 > hash2)
        return 1;
    else return 0;
  }

  //==========================================================================
  /**
   * DEVUELVE IDGL COMO UN LONG PARA AÑADIR A LOS TPDU
   */
  public long getIDGL ()
  {
   return this.hashCode();
  }

  //==========================================================================
  /**
   * Devuelve una cadena identificativa del objeto.
   * @return Cadena Indentificativa.
   */

  public String toString ()
  {
   return ("[IDGL: "+getStringID()+"] TTL="+this.TTL);
  }

}
