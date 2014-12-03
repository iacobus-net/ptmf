//============================================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	File: ListaOrdID_TPDU.java  1.0 25/10/99
//
//
//	Description: Clase ListaOrdID_TPDU. Lista ordenada de ID_TPDU
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

package net.iacobus.ptmf;


import java.util.TreeMap;
import java.util.Iterator;
import java.util.SortedMap;


/**
 * Almacena ID_TPDU a los que permite asociar un objeto, o null.
 * Implementa todos los métodos de la interfaz Iterator menos remove (),
 * para permitir realizar recorridos por la lista. <p>
 * El orden es el establecido por la clase ID_TPDU, en su método
 * {@link ID_TPDU#compareTo(Object)}.
 *
 * <b>ID_TPDU tiene que implementar la interfaz comparable.</b>
 *
 * No es thread-safe.
 * @version  1.0
 * @author Antonio Berrocal Piris
 * <A HREF="mailto:AntonioBP@wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
 * M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 */
public class ListaOrdID_TPDU implements Cloneable
{
 // ATRIBUTOS
  /**
   * Almacena los ID_TPDU y los objetos asociados.
   * <table border=1>
   *  <tr>  <td><b>Key:</b></td>
   *	    <td>{@link ID_TPDU}</td>
   *  </tr>
   *  <tr>  <td><b>Value:</b></td>
   *	    <td>Object</td>
   *  </tr>
   * </table>
   */
  private TreeMap  treeMapID_TPDU = null;

  //==========================================================================
  /**
   * Constructor.
   */
   public ListaOrdID_TPDU()
   {
    this.treeMapID_TPDU = new TreeMap ();
   }

  //==========================================================================
  /**
   * Añade el ID_TPDU a la lista. Si existía previamente el valor antiguo
   * es reemplazado por el nuevo.
   * @param id_TPDU identificador de un TPDU
   * @param obj objeto que se asocia a id_TPDU. Puede ser null.
   * @return objeto antes asignado a id_TPDU o null si no lo tenía, o tenía
   * asignado expresamente null.
   */
  public Object put (ID_TPDU id_TPDU,Object obj)
  {
    return this.treeMapID_TPDU.put (id_TPDU,obj);
  }

  //==========================================================================
  /**
   * Copia todos los pares contenidos en lista a esta, reemplazando los de lista
   * por los de esta si conincide la clave.
   * @param lista lista a copiar
   */
  public void putAll (ListaOrdID_TPDU lista)
  {
   this.treeMapID_TPDU.putAll (lista.treeMapID_TPDU);
  }

  //==========================================================================
  /**
   * Devuelve el objeto asociado al id_tpdu indicado o null si no tiene.
   * @param id_TPDU
   * @return objeto asociado o null
   */
  public Object get (ID_TPDU id_TPDU)
  {
    return this.treeMapID_TPDU.get (id_TPDU);
  }

  //==========================================================================
  /**
   * Devuelve la primera clave de la lista
   * <b>Si la lista está vacía LANZA EXCEPCION NoSuchElementException</B>
   * @return primera clave de la lista.
   */
  public ID_TPDU firstKey ()
  {
   return (ID_TPDU)this.treeMapID_TPDU.firstKey();
  }

  //==========================================================================
  /**
   * Devuelve el objeto asociado a la primera clave de la lista.
   * @exception NoSuchElementException si la lista está vacia.
   */
  public Object removeFirstElement()
  {
   return this.treeMapID_TPDU.remove(this.treeMapID_TPDU.firstKey());
  }

  //==========================================================================
  /**
   * Elimina id_TPDU, si existe.
   * @param id_TPDU identificador a eliminar.
   * @return el objeto asociado con id_TPDU, o null si no lo había. Si devuelve
   * null puede ser por dos motivos, no existe o por ser el valor asociado a
   * id_TPDU.
   */
  public Object remove (ID_TPDU id_TPDU)
  {
    return this.treeMapID_TPDU.remove (id_TPDU);
  }

 //==========================================================================
 /**
  * Elimina todo el contenido de la lista.
  */
  public void clear ()
  {
   this.treeMapID_TPDU.clear ();
  }

  //=========================================================================
  /**
   * Devuelve true si contiene el id_TPDU
   * @param id_TPDU
   */
  public boolean contiene (ID_TPDU id_TPDU)
  {
   return this.treeMapID_TPDU.containsKey (id_TPDU);
  }

  //=========================================================================
  /**
   * Devuelve el menor id_tpdu utilizado como clave en la lista, cuyo id_socket
   * es el indicado. O null si no tiene ninguno
   * @param id_socket
   * @return id_tpdu menor registrado para id_socket
   */
  public ID_TPDU getID_TPDUMenor (ID_Socket id_socket)
  {
   Iterator iteradorID_TPDU  = this.iteradorID_TPDU ();

   // Cuando encuentre el primero paro. El treemap sigue un orden ascendente.
   ID_TPDU id_tpduNext;
   int iComp;
   while (iteradorID_TPDU.hasNext())
    {
     id_tpduNext = (ID_TPDU)iteradorID_TPDU.next();

     if (id_tpduNext != null)
       {
          iComp = id_tpduNext.getID_Socket().compareTo (id_socket);
          switch (iComp)
            {
             case 0: return id_tpduNext; // Son iguales
             case 1: return null; // socketNext > id_socket; no continuar
            }
        }
    } // Fin del while

   return null;
  }

  //==========================================================================
  /**
   * Elimina todos los id_TPDU cuyo id_socket es el indicado.
   * @param id_Socket
   * @return lista con los id_tpdu eliminados, o null si no había ninguno.
   */
  public ListaOrdID_TPDU removeID_Socket (ID_Socket id_Socket)
  {
   ListaOrdID_TPDU listaResult = new ListaOrdID_TPDU ();
   Iterator iterador = ((TreeMap)this.treeMapID_TPDU.clone()).keySet().iterator();

   while (iterador.hasNext())
    {
     ID_TPDU id_TPDU = (ID_TPDU)iterador.next ();

     if (id_TPDU.getID_Socket().equals (id_Socket))
        listaResult.put (id_TPDU,this.treeMapID_TPDU.remove(id_TPDU));
    }

   return listaResult;
  }

  //==========================================================================
  /**
   * Iterador sobre los IDs_TPDU de la lista, permite remove.
   */
  public Iterator iteradorID_TPDU ()
  {
   return new Iterator ()
             {
              private Iterator iterador = treeMapID_TPDU.keySet().iterator();

              public boolean hasNext()
               {
                return iterador.hasNext();
               }
              public Object next()
               {
                return iterador.next();
               }
              public void remove()
               {
                iterador.remove ();
               }
              };
  }


  //==========================================================================
  /**
   * Iterador sobre los IDs_TPDU de la lista, permite remove.
   */
  public Iterator iteradorObjetos ()
  {
   return new Iterator ()
             {
              private Iterator iterador = treeMapID_TPDU.values().iterator();

              public boolean hasNext()
               {
                return iterador.hasNext();
               }
              public Object next()
               {
                return iterador.next();
               }
              public void remove()
               {
                iterador.remove ();
               }
              };
  }

  //===========================================================================
  /**
   * Devuelve el número de ID_TPDU que hay en la lista.
   */
  public int size ()
  {
   return this.treeMapID_TPDU.size ();
  }

  //==========================================================================
  /**
   * Una lista A es equivalente a otra B, si los ID_TPDU que contiene A es
   * un subconjunto de los que contiene B.
   * @param lista lista con la que esta se compara
   * @return true si esta es equivalente a lista
   */
  public boolean equivalenteA (ListaOrdID_TPDU lista)
  {
   Iterator iterador = this.treeMapID_TPDU.keySet().iterator ();

   while (iterador.hasNext())
    {
     ID_TPDU id_TPDU = (ID_TPDU) iterador.next ();
     if (!lista.contiene (id_TPDU))
        return false;
    }
   return true;
  }

  //==========================================================================
  /**
   * Devuelve un SortedMap con los id_tpdu cuyo id_socket sea el indicado
   * y su número de secuencia sea mayor o igual a nSec, o null si
   * no había ninguno.<br>
   * <B>Los cambios realizados en el sortedMap se reflejarán en la lista.</B>
   * @param id_socket
   * @param nSec
   * @return sortedMap con los id_tpdu mayor o igual, o null.
   */
   public SortedMap getSublistaMayorIgual (ID_Socket id_socket,NumeroSecuencia nSec)
   {
    // Devuelve una sublista formada por los números de secuencia del id_socket
    // indicado, mayores o iguales al dado.
    try {
     ID_TPDU id_tpduLimInferior = new ID_TPDU (id_socket,nSec);
     ID_TPDU id_tpduLimSuperior = new ID_TPDU (id_socket,NumeroSecuencia.LIMITESUPERIOR);
     return this.treeMapID_TPDU.subMap (id_tpduLimInferior,id_tpduLimSuperior);
     }catch (ParametroInvalidoExcepcion pie)
          {
           Log.log ("",pie.toString());
           Log.exit (1);
          }
    return null;
  }

  //==========================================================================
  /**
   * Devuelve un SortedMap con los id_tpdu cuyo id_socket sea el indicado,
   * o null si no había ninguno.<br>
   * <B>Los cambios realizados en el sortedMap se reflejarán en la lista.</B>
   * @param id_socket
   * @return sortedMap con los id_tpdu mayor o igual, o null.
   */
  public SortedMap getSublista (ID_Socket id_socket)
  {
   return this.getSublistaMayorIgual (id_socket,NumeroSecuencia.LIMITEINFERIOR);
  }


  //==========================================================================
  /**
   * Devuelve un SortedMap con los id_tpdu cuyo id_socket sea el indicado
   * y su número de secuencia sea menor o igual a nSec, o null si
   * no había ninguno.<br>
   * <B>Los cambios realizados en el sortedMap se reflejarán en la lista.</B>
   * @param id_socket
   * @param nSec
   * @return sortedMap con los id_tpdu mayor o igual, o null.
   */
  public SortedMap getSublistaMenorIgual (ID_Socket id_socket,NumeroSecuencia nSec)
  {
    // Devuelve una sublista formada por los números de secuencia del id_socket
    // indicado, mayores o iguales al dado.
    try {
     ID_TPDU id_tpduLimInferior = new ID_TPDU (id_socket,NumeroSecuencia.LIMITEINFERIOR);
     NumeroSecuencia nSecSiguiente = new NumeroSecuencia (nSec.tolong()+1);
     ID_TPDU id_tpduLimSuperior = new ID_TPDU (id_socket,nSecSiguiente);
     return this.treeMapID_TPDU.subMap (id_tpduLimInferior,id_tpduLimSuperior);
    }
     catch (ParametroInvalidoExcepcion pie)
          {
           Log.log ("",pie.toString());
           Log.exit (1);
          }
    return null;


  }


  //==========================================================================
  /**
   * Implementación del método clone
   */
  public Object clone()
  {
     ListaOrdID_TPDU result = new ListaOrdID_TPDU ();
     result.treeMapID_TPDU  = (TreeMap)this.treeMapID_TPDU.clone();

     return result;
  }

  //==========================================================================
  /**
   * Devuelve una cadena informativa.
   */
  public String toString ()
  {
   return this.treeMapID_TPDU.toString();
  }


}
