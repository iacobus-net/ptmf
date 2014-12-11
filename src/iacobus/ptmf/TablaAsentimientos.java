//============================================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: TablaAsentimientos.java  1.0 26/09/99
//
//
//	Descripción: Clase TablaAsentimientos. Almacena y gestiona los asentimientos
//                   positivos que se tienen que se tienen que recibir.
//
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
//----------------------------------------------------------------------------

package iacobus.ptmf;

import java.util.TreeMap;
import java.util.Vector;
import java.util.Iterator;
import java.util.SortedMap;

/**
 * 1. Almacena <b>los ID_TPDU por los que está esperando recibir asentimientos
 *    positivos.</b><br>
 * 2. Lleva el control de quien ha asentido (vecinos o hermanos --> ID_Socket)
 *    o (grupos locales --> IDGL)
 *
 * Esta clase no es thread-safe.
 *
 * @version  1.0
 * @author Antonio Berrocal Piris
 * <A HREF="mailto:AntonioBP@wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
 * M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 */
public class TablaAsentimientos
{
  /**
   * ID_TPDU por los que se está esperando asentimientos positivos.<br>
   * <table border=1>
   *  <tr>  <td><b>Key:</b></td>
   *	    <td>ID_TPDU</td>
   *  </tr>
   *  <tr>  <td><b>Value:</b></td>
   *	    <td>Instancia de RegistroAsentimientos</td>
   *  </tr>
   * </table>
   */
   private ListaOrdID_TPDU listaIDs_TPDUEnEsperaAsentimiento = null;

   /** Objeto datosThread */
   private DatosThread datosThread = null;

   /** Objeto CGLThread */
   private CGLThread cglThread = null;

  //==========================================================================
  /**
   * Crea los estructuras de datos necesarias.
   * @param cglThread objeto CGLThread
   * @throws ParametroInvalidoExcepcion lanzada si cglThread es null
   */
  public TablaAsentimientos(DatosThread datosThread) throws ParametroInvalidoExcepcion
  {
   if (datosThread == null)
    throw new ParametroInvalidoExcepcion("Puntero a DatosThread NULO.");


   this.datosThread = datosThread;
   this.cglThread = this.datosThread.getCGLThread ();

   if (this.cglThread == null)
    throw new ParametroInvalidoExcepcion("Puntero a cglThread NULO.");

   this.listaIDs_TPDUEnEsperaAsentimiento = new ListaOrdID_TPDU ();
  }

  //==========================================================================
  /**
   * Añade un nuevo ID_TPDU para el cual se está esperando recibir asentimientos.
   * Hace una copia de los asentimientos que ya se han recibido por su inmediato
   * superior, para la misma dirección fuente, si lo hay.
   * @param ID_TPDU identificador del TPDU
   * @param iNumeroRafaga número de ráfaga a la que pertenece el id_tdpu
   * @return true si no existía y ha sido añadido, y false en caso contrario.
   */
  public boolean addID_TPDUEnEsperaAsentimiento (ID_TPDU id_TPDU,int iNumeroRafaga)
  {
   // Comprobar si ya lo tenía registrado.
   if (!this.listaIDs_TPDUEnEsperaAsentimiento.contiene(id_TPDU))
    {
     SortedMap sortedMap = this.listaIDs_TPDUEnEsperaAsentimiento.getSublistaMayorIgual
                                              (id_TPDU.getID_Socket(),
                                               id_TPDU.getNumeroSecuencia());
     RegistroAsentimientos reg = null;
     // Coger el registro del id_tpdu inmediatamente superior y copiarlo a este.
     if (sortedMap.size()>0)
        {
         RegistroAsentimientos regSuperior = (RegistroAsentimientos)sortedMap.get (
                                                        sortedMap.firstKey());

         reg = (RegistroAsentimientos)regSuperior.clone();
         // Actualiza solamente los datos de quienes han asentido.
         reg.iNumeroRafaga = iNumeroRafaga;
        }
     else {
            reg = new RegistroAsentimientos();
            reg.iNumeroRafaga = iNumeroRafaga;
          }

     this.listaIDs_TPDUEnEsperaAsentimiento.put (id_TPDU,reg);
     return true;
    }
   return false;
  }

  //==========================================================================
  /**
   * Elimina ID_TPDU para el cual se está esperando recibir asentimientos.
   * Elimina todos los menores o iguales.
   * @param ID_TPDU identificador del TPDU
   * @return true si existía y ha sido eliminado, y false en caso contrario.
   */
  public void removeID_TPDUMenorIgualEnEsperaAsentimiento (ID_TPDU id_TPDU)
  {
   // Comprobar que somos CG Local para id_TPDU
   SortedMap sortedMap = this.listaIDs_TPDUEnEsperaAsentimiento.getSublistaMenorIgual
                                                 (id_TPDU.getID_Socket(),
                                                  id_TPDU.getNumeroSecuencia());
   sortedMap.clear ();
  }

  //==========================================================================
  /**
   * Anota como recibido de id_Socket un ACK para el id_tpdu indicado, así como
   * para todos los menores, por ser los ACK acumulativos. Comprueba que id_Socket
   * pertenece al grupo local (es vecino).
   * @return true si se estaba esperando recibir asentimiento por id_tpdu o por
   * alguno menor.
   */
  public boolean addACK (ID_Socket id_Socket, ID_TPDU id_tpdu)
  {
   if (!this.cglThread.esVecino (id_Socket))
        return false;

   SortedMap sortedMap = this.listaIDs_TPDUEnEsperaAsentimiento.getSublistaMenorIgual
                                             (id_tpdu.getID_Socket(),
                                              id_tpdu.getNumeroSecuencia());
   if (sortedMap==null)
       return false; // No está en espera de asentimiento.

   Iterator iteradorRegistros = sortedMap.values().iterator();
   RegistroAsentimientos regNext = null;

   // Actualizar todos los menores o iguales al dado.
   boolean bResult = false;
   while (iteradorRegistros.hasNext())
     {
      regNext = (RegistroAsentimientos)iteradorRegistros.next();
      // Actualizar el registro. Si existía previamente es reemplazado.
      regNext.treeMapEnviadoACK.put (id_Socket,null);
      bResult = true;
     } // Fin del while.

   return bResult;
  }

  //==========================================================================
  /**
   * id_Socket ha mandado ACK para todos los ID_TPDU que estan en espera de
   * asentimiento.
   * @return true si había algún ID_TPDU en espera.
   */
  public boolean addACKAID_TPDUEnEspera (ID_Socket id_Socket)
  {
   // ALEX: Comprobar que es miembro del grupo local (hermano)
   if (!this.cglThread.esVecino (id_Socket))
        return false;

   Iterator iteradorRegistros = this.listaIDs_TPDUEnEsperaAsentimiento.iteradorObjetos ();

   if (iteradorRegistros==null)
       return false; // No está en espera de asentimiento.

   RegistroAsentimientos regNext = null;

   // Actualizar todos los menores o iguales al dado.
   while (iteradorRegistros.hasNext())
     {
      regNext = (RegistroAsentimientos)iteradorRegistros.next();
      // Actualizar el registro. Si existía previamente es reemplazado.
      regNext.treeMapEnviadoACK.put (id_Socket,null);
     } // Fin del while.

   return true;
  }

  //==========================================================================
  /**
   * Anota como recibido de idgl un HACK para el id_tpdu indicado, así como
   * para todos los menores, por ser los HACK acumulativos. Comprueba que idgl
   * es hijo jerárquico.
   * @return true si se estaba esperando recibir asentimiento por id_tpdu o por
   * alguno menor.
   */
  public boolean addHACK (IDGL idgl,ID_TPDU id_tpdu)
  {
   if ((idgl==null)||(id_tpdu==null))
       return false;

   // Obtener idglFuente para id_tpdu
   IDGL idglFuente = this.datosThread.getIDGL (id_tpdu.getID_Socket());

   if (idglFuente == null)
        return false;

   // Comprobar si idgl es hijo para idglFuente.
   if (!this.cglThread.getCGHijos (idglFuente).containsKey (idgl))
        return false; // No es hijo jerárquico

   SortedMap sortedMap = this.listaIDs_TPDUEnEsperaAsentimiento.getSublistaMenorIgual
                                             (id_tpdu.getID_Socket(),
                                              id_tpdu.getNumeroSecuencia());
   if (sortedMap==null)
       return false;

   Iterator iteradorRegistros = sortedMap.values().iterator();
   RegistroAsentimientos regNext = null;

   // Actualizar todos los menores o iguales al dado.
   boolean bResult = false;
   while (iteradorRegistros.hasNext())
     {
      regNext = (RegistroAsentimientos)iteradorRegistros.next();
      // Actualizar el registro. Borrar de HSACK
      regNext.treeMapEnviadoHSACK.remove (idgl);

      regNext.treeMapEnviadoHACK.put (idgl,null);

      bResult = true;
     } // Fin del while.
   return bResult;
  }

  //==========================================================================
  /**
   * Anota como recibido de idgl un HSACK para el id_tpdu indicado, así como
   * para todos los menores, por ser los HSACK acumulativos. Comprueba que idgl
   * es hijo jerárquico.
   * @return true si se estaba esperando recibir asentimiento por id_tpdu o por
   * alguno menor.
   */
  public boolean addHSACK (IDGL idgl,ID_TPDU id_tpdu)
  {
   final String mn = "TablaAsentimientos.addHSACK (idgl,id_tpdu)";

   if ((idgl==null)||(id_tpdu==null))
      return false;

   // Obtener idglFuente
   IDGL idglFuente = this.datosThread.getIDGL (id_tpdu.getID_Socket());

   if (idglFuente==null)
        return false;

   // Comprobar si idgl es hijo para idglFuente.
   if (!this.cglThread.getCGHijos (idglFuente).containsKey (idgl))
        return false; // No es hijo jerárquico

   SortedMap sortedMap = this.listaIDs_TPDUEnEsperaAsentimiento.getSublistaMenorIgual
                                             (id_tpdu.getID_Socket(),
                                              id_tpdu.getNumeroSecuencia());

   if (sortedMap==null)
       return false;

   Iterator iteradorRegistros = sortedMap.values().iterator();
   RegistroAsentimientos regNext = null;

   // Actualizar todos los menores o iguales al dado.
   boolean bResult = false;
   while (iteradorRegistros.hasNext())
     {
      regNext = (RegistroAsentimientos)iteradorRegistros.next();

      // Si estaba registrado como HACK entonces no registrar como HSACK.
      if (regNext.treeMapEnviadoHACK.containsKey (idgl))
        continue;

      // Actualizar el registro.
      regNext.treeMapEnviadoHSACK.put (idgl,null);
      bResult = true;
     } // Fin del while.

   return bResult;
  }

  //==========================================================================
  /**
   * idgl ha mandado HSACK para todos los TPDU que están en espera de
   * asentimiento.
   * @return true si había algún ID_TPDU en espera.
   */
  public boolean addHSACKAID_TPDUEnEspera (IDGL idgl)
  {
   if (idgl==null)
      return false;

   Iterator iteradorID_TPDU = this.listaIDs_TPDUEnEsperaAsentimiento.iteradorID_TPDU();
   RegistroAsentimientos regNext = null;
   ID_TPDU id_tpduNext = null;
   while (iteradorID_TPDU.hasNext())
   {
      id_tpduNext = (ID_TPDU)iteradorID_TPDU.next();

      // Obtener idglFuente
      IDGL idglFuente = this.datosThread.getIDGL (id_tpduNext.getID_Socket());

      if (idglFuente==null)
          continue;

      // Comprobar si idgl es hijo para idglFuente.
      if (!this.cglThread.getCGHijos (idglFuente).containsKey (idgl))
        continue; // No es hijo jerárquico

      regNext = (RegistroAsentimientos)this.listaIDs_TPDUEnEsperaAsentimiento.get (id_tpduNext);

      // Si estaba registrado como HACK entonces no registrar como HSACK.
      if (regNext.treeMapEnviadoHACK.containsKey (idgl))
        continue;

     // Actualizar el registro.
     regNext.treeMapEnviadoHSACK.put (idgl,null);
   }
   return true;
  }

  //========================================================================
  /**
   * Devuelve una lista ordenada de ID_TPDU que han sido asentidos por
   * todos los vecinos e hijos jerárquicos.
   * @return lista con los ID_TPDU asentidos o vacía si no hay ninguno.
   */
  public ListaOrdID_TPDU getID_TPDUAsentidos ()
  {
    ListaOrdID_TPDU listaResult = new ListaOrdID_TPDU ();

    // Iterador con las claves (ID_TPDU) existentes.
    Iterator iterador = this.listaIDs_TPDUEnEsperaAsentimiento.iteradorID_TPDU();

    ID_TPDU id_tpduNext = null;
    RegistroAsentimientos reg = null;
    IDGL idglFuente = null;
    while (iterador.hasNext())
    {
     // Comprobar si id_tpduNext ha sido asentido
     id_tpduNext = (ID_TPDU) iterador.next ();
     reg = (RegistroAsentimientos)
                           this.listaIDs_TPDUEnEsperaAsentimiento.get (id_tpduNext);
     idglFuente = this.datosThread.getIDGL (id_tpduNext.getID_Socket());

     if (idglFuente==null)
        continue;

     if ( (this.cglThread.numeroVecinos()==reg.treeMapEnviadoACK.size())
           &&
          (this.cglThread.getCGHijos(idglFuente).size()==reg.treeMapEnviadoHACK.size()))
        // Ha sido asentido por todos.
        listaResult.put (id_tpduNext,null);
    } // Fin del while
   return listaResult;
  }

  //========================================================================
  /**
   * Devuelve una lista ordenada de los ID_TPDU que no han sido asentidos por
   * todos los vecinos e hijos jerárquicos.
   * @return lista con los ID_TPDU no asentidos o vacía si no hay ninguno.
   */
  public ListaOrdID_TPDU getID_TPDUNoAsentidos ()
  {
    ListaOrdID_TPDU listaResult = new ListaOrdID_TPDU ();

    // Iterador con las claves (ID_TPDU) existentes.
    Iterator iterador = this.listaIDs_TPDUEnEsperaAsentimiento.iteradorID_TPDU();

    ID_TPDU id_tpduNext = null;
    RegistroAsentimientos reg = null;
    IDGL idglFuente = null;
    while (iterador.hasNext())
    {
     // Comprobar si id_tpduNext ha sido asentido
     id_tpduNext = (ID_TPDU) iterador.next ();
     reg = (RegistroAsentimientos)
                           this.listaIDs_TPDUEnEsperaAsentimiento.get (id_tpduNext);
     idglFuente = this.datosThread.getIDGL (id_tpduNext.getID_Socket());

     if (idglFuente==null)
        continue;

     if ( (this.cglThread.numeroVecinos()==reg.treeMapEnviadoACK.size())
           &&
          (this.cglThread.getCGHijos(idglFuente).size()==reg.treeMapEnviadoHACK.size()))
        {/* Ha sido asentido por todos.*/}
     else
     {
       listaResult.put (id_tpduNext,null);

       //ALEX: depuración, comentar
       Log.debug(Log.TABLA_ASENTIMIENTOS,"TablaAsentimientos.getID_TPDUNoAsentidos","ID_TDPU No asentido por todos: "+id_tpduNext);
       Log.debug(Log.TABLA_ASENTIMIENTOS,"","Nº Vecinos: "+this.cglThread.numeroVecinos());
       Log.debug(Log.TABLA_ASENTIMIENTOS,"","Nº ACKs recibidos: "+reg.treeMapEnviadoACK.size());
       Log.debug(Log.TABLA_ASENTIMIENTOS,"","Nº CG Hijos: "+this.cglThread.getCGHijos(idglFuente).size());
       Log.debug(Log.TABLA_ASENTIMIENTOS,"","Nº HACKs recibidos: "+reg.treeMapEnviadoHACK.size());


     }

    } // Fin del while

   return listaResult;
  }

  //========================================================================
  /**
   * Elimina los ID_TPDU cuyo id_socket coincida con el especificado y que
   * pertenezcan a la ráfaga iNumeroRafaga.
   * @param id_socket al que tiene que pertenecer los ID_TPDU que elimine
   * @param iNumeroRafaga número de la ráfaga de los ID_TPDU a eliminar.
   * @return lista ordenada con los ID_TPDU eliminados, o vacía si no se eliminó
   * ninguno.
   */
  public ListaOrdID_TPDU removeID_TPDUEnEsperaAsentimiento (ID_Socket id_socket,
                                                                int iNumeroRafaga)
  {
    ListaOrdID_TPDU listaResult = new ListaOrdID_TPDU ();

    Iterator iteradorID_TPDU =
        this.listaIDs_TPDUEnEsperaAsentimiento.getSublista(id_socket).keySet().iterator();

    ID_TPDU id_tpduNext = null;
    RegistroAsentimientos reg = null;
    while (iteradorID_TPDU.hasNext())
     {
      id_tpduNext = (ID_TPDU)iteradorID_TPDU.next();
      reg = (RegistroAsentimientos)this.listaIDs_TPDUEnEsperaAsentimiento.get (id_tpduNext);
      if (reg==null)
         continue;

      if (reg.iNumeroRafaga==iNumeroRafaga)
        {
         iteradorID_TPDU.remove ();
         listaResult.put (id_tpduNext,null);
        }
     }// Fin del while

    return listaResult;
  }

  //========================================================================
  /**
   * Comprueba si se está esperando recibir asentimiento por id_tpdu
   * @param id_tpdu
   */
  public boolean contieneID_TPDU (ID_TPDU id_tpdu)
  {
   return this.listaIDs_TPDUEnEsperaAsentimiento.contiene (id_tpdu);
  }

  //========================================================================
  /**
   * Devuelve el menor ID_TPDU en espera de asentimiento, asociado al id_socket
   * indicado.
   * @param id_socket
   * @return id_tpdu menor en espera para id_socket
   */
  public ID_TPDU getID_TPDUMenorEnEsperaAsentimiento (ID_Socket id_socket)
   {
    return this.listaIDs_TPDUEnEsperaAsentimiento.getID_TPDUMenor (id_socket);
   }

  //========================================================================
  /**
   * Devuelve true si no se está esperando asentimiento por ningún TPDU.
   */
  public boolean estaVacia ()
  {
   return (this.listaIDs_TPDUEnEsperaAsentimiento.size()==0);
  }

  //========================================================================
  /**
   * Comprueba si id_TPDU ha sido asentido por todos los vecinos e hijos
   * jerárquicos.
   * @return true si ha sido asentido y false <b>si no se estaba en espera de
   * asentimiento para dicho id_TPDU</b> o no ha sido asentido.
   */
  public boolean asentido(ID_TPDU id_TPDU)
  {
     // No es necesario comprobar si uno con número de secuencia mayor ha sido
     // asentido puesto que se acualizan los asentimientos al añadir.
     // Comprobar si se está esperando por id_TPDU
     RegistroAsentimientos reg = (RegistroAsentimientos)
                                this.listaIDs_TPDUEnEsperaAsentimiento.get (id_TPDU);
     if (reg==null)
        return false;

     IDGL idglFuente = this.datosThread.getIDGL (id_TPDU.getID_Socket());

     if (idglFuente==null)
        return false;

     if ( (this.cglThread.numeroVecinos()==reg.treeMapEnviadoACK.size())
           &&
          (this.cglThread.getCGHijos(idglFuente).size()==reg.treeMapEnviadoHACK.size()))
             {
             return true;
             }
     return false;
  }

  //========================================================================
  /**
   * Convierte los HSACK que había recibido para id_tpdu en HACK.
   * @param id_tpdu
   */
  public void convertirHSACKaHACK (ID_TPDU id_tpdu)
  {
   if (!contieneID_TPDU(id_tpdu))
      return;

   // Iterador con los de menor o igual número de secuencia.
   SortedMap sortedMap = this.listaIDs_TPDUEnEsperaAsentimiento.getSublistaMenorIgual
                                             (id_tpdu.getID_Socket(),
                                              id_tpdu.getNumeroSecuencia());

   if (sortedMap==null)
       return;

   Iterator iteradorRegistros = sortedMap.values().iterator();
   RegistroAsentimientos regNext = null;

   // Actualizar todos los menores o iguales al dado.
   while (iteradorRegistros.hasNext())
     {
      regNext = (RegistroAsentimientos)iteradorRegistros.next();

      // Todos los idgl que esten en HSACK pasan a HACK
      regNext.treeMapEnviadoHACK.putAll (regNext.treeMapEnviadoHSACK);

      // Borrarlos de HSACK
      regNext.treeMapEnviadoHSACK.clear ();

     } // Fin del while.
  }

  //========================================================================
  /**
   * Comprueba si id_TPDU ha sido semiAsentido por todos los vecinos e hijos
   * jerárquicos. Es decir, si han mandado un ACK todos los ID_Sockets vecinos
   * y un HACK o un HSACK todos los hijos jerárquicos.
   * @param id_TPDU
   * @return true si ha sido semiasentido y false <b>si no se estaba en espera de
   * asentimiento para dicho id_TPDU</b> o no ha sido asentido.
   */
  public boolean semiAsentido(ID_TPDU id_TPDU)
  {
     // No es necesario comprobar si uno con número de secuencia mayor ha sido
     // asentido puesto que se acualizan los asentimientos al añadir.
     // Comprobar si se está esperando por id_TPDU
     RegistroAsentimientos reg = (RegistroAsentimientos)
                                this.listaIDs_TPDUEnEsperaAsentimiento.get (id_TPDU);

     if (reg==null)
        return false;

     IDGL idglFuente = this.datosThread.getIDGL(id_TPDU.getID_Socket());

     if (idglFuente==null)
        return false;

     if ( (this.cglThread.numeroVecinos()==reg.treeMapEnviadoACK.size())
           &&
          (this.cglThread.getCGHijos(idglFuente).size()==
              (reg.treeMapEnviadoHACK.size() + reg.treeMapEnviadoHSACK.size())))
             return true;
     return false;
  }

  //========================================================================
  /**
   * Comprueba si id_TPDU ha sido asentido por algún vecino.<br>
   * Es decir, si algún vecino ha mandado un ACK.
   * @return id_TPDU
   */
  public boolean algunACKID_Socket(ID_TPDU id_TPDU)
  {
     // Comprobar si se está esperando por id_TPDU
     RegistroAsentimientos reg = (RegistroAsentimientos)
                                this.listaIDs_TPDUEnEsperaAsentimiento.get (id_TPDU);

     // treeMapEnviadoACK y treeMapEnviadoHACK son disjuntos.
     if (reg==null)
        return false;

     if ( (reg.treeMapEnviadoACK.size ()!=0))
        return true;

     return false;
  }


 //===========================================================================
 /**
  * Devuelve un treemap con los ID_Sockets que no han enviado ACK pora id_TPDU,
  * o el vector vacío si no hay ninguno.
  * @param id_TPDU identificador de TPDU
  * @return un objeto treemap con los ID_Sockets o vacío.
   * <table border=1>
   *  <tr>  <td><b>Key:</b></td>
   *	    <td>{@link ID_Socket}</td>
   *  </tr>
   *  <tr>  <td><b>Value:</b></td>
   *	    <td>NULL</td>
   *  </tr>
   * </table>
  */
 public TreeMap getTreeMapID_SocketsNoEnviadoACK(ID_TPDU id_TPDU)
 {
  TreeMap treeMapResult = new TreeMap ();

  RegistroAsentimientos reg = (RegistroAsentimientos)
                                this.listaIDs_TPDUEnEsperaAsentimiento.get (id_TPDU);
  if (reg!=null)
  {
    // Comprobar que no ha sido asentido por todos los vecinos.
    // diferencia indica el número de hosts que faltan por asentir.
    int iDiferencia = this.cglThread.numeroVecinos() -
                                                  reg.treeMapEnviadoACK.size ();

    if (iDiferencia<=0)
      return treeMapResult; // Devolver treeMap vacío.

    // Comprobar para cada uno de los socket´s vecinos si ha enviado ACK.
    Iterator iterador = this.cglThread.getTreeMapID_SocketVecinos().keySet().iterator ();
    ID_Socket id_SocketNext = null;
    while (iterador.hasNext() || (iDiferencia>0))
    {
     id_SocketNext = (ID_Socket) iterador.next ();
     if (!(reg.treeMapEnviadoACK.containsKey (id_SocketNext)))
          {
           treeMapResult.put (id_SocketNext,null);
           iDiferencia--;
          }
    } // Fin del while
   } // Fin de if
   return treeMapResult;
 }

 //===========================================================================
 /**
  * Devuelve un treemap con los IDGLs que no han enviado HACK o HSACK
  * pora id_TPDU, o el vector vacío si no hay ninguno.
  * @param id_TPDU identificador de TPDU
  * @return un objeto treemap con los IDGLs o vacío.
   * <table border=1>
   *  <tr>  <td><b>Key:</b></td>
   *	    <td>{@link IDGL}</td>
   *  </tr>
   *  <tr>  <td><b>Value:</b></td>
   *	    <td>NULL</td>
   *  </tr>
   * </table>
  */
 public TreeMap getTreeMapIDGLNoEnviadoHACKoHSACK(ID_TPDU id_TPDU)
 {
  TreeMap treeMapResult = new TreeMap ();

  RegistroAsentimientos reg = (RegistroAsentimientos)
                                this.listaIDs_TPDUEnEsperaAsentimiento.get (id_TPDU);
  if (reg!=null)
  {
    IDGL idglFuente = this.datosThread.getIDGL(id_TPDU.getID_Socket());
    if (idglFuente==null)
        return treeMapResult;

    // Comprobar que no ha sido asentido por todos los hijos.
    // diferencia indica el número de hosts que faltan por asentir.
    int iDiferencia = this.cglThread.getCGHijos(idglFuente).size() -
               (reg.treeMapEnviadoHACK.size ()+reg.treeMapEnviadoHSACK.size ());

    if (iDiferencia<=0)
      return treeMapResult; // Devolver vector vacío.

    // Comprobar para cada uno de los hosts hijos si ha enviado algún tipo de
    // asentimiento.
    Iterator iterador = this.cglThread.getCGHijos(idglFuente).keySet().iterator ();
    IDGL idglNext = null;
    while (iterador.hasNext() || (iDiferencia>0))
    {
     idglNext = (IDGL) iterador.next ();

     if (!(reg.treeMapEnviadoHACK.containsKey (idglNext))
         &&
         !(reg.treeMapEnviadoHSACK.containsKey (idglNext)) )
          {
           treeMapResult.put (idglNext,null);
           iDiferencia--;
          }
    } // Fin del while
   } // Fin de if
   return treeMapResult;
 }

 //===========================================================================
 /**
  * Devuelve un treemap con los IDGLs que no han enviado HACK para id_TPDU,
  * o el vector vacío si no hay ninguno.
  * @param id_TPDU identificador de TPDU
  * @return  un objeto treemap con los IDGLs o vacío.
   * <table border=1>
   *  <tr>  <td><b>Key:</b></td>
   *	    <td>{@link IDGL}</td>
   *  </tr>
   *  <tr>  <td><b>Value:</b></td>
   *	    <td>NULL</td>
   *  </tr>
   * </table>
  */
 public TreeMap getTreeMapIDGLNoEnviadoHACK(ID_TPDU id_TPDU)
 {
  TreeMap treeMapResult = new TreeMap ();

  RegistroAsentimientos reg = (RegistroAsentimientos)
                                this.listaIDs_TPDUEnEsperaAsentimiento.get (id_TPDU);

  if (reg!=null)
  {
    IDGL idglFuente = this.datosThread.getIDGL(id_TPDU.getID_Socket());

    if (idglFuente==null)
        return treeMapResult;

    // Comprobar que no ha sido asentido por todos los hijos.
    // diferencia indica el número de hosts que faltan por asentir.
    int iDiferencia = this.cglThread.getCGHijos(idglFuente).size() -
                                                 reg.treeMapEnviadoHACK.size ();
    if (iDiferencia<=0)
      return treeMapResult; // Devolver vector vacío.

    // Comprobar para cada uno de los hosts hijos si ha enviado algún tipo de
    // asentimiento.
    Iterator iterador = this.cglThread.getCGHijos(idglFuente).keySet().iterator ();
    IDGL idglNext = null;
    while (iterador.hasNext() || (iDiferencia>0))
    {
     idglNext = (IDGL) iterador.next ();
     if (!(reg.treeMapEnviadoHACK.containsKey (idglNext)))
          {
           treeMapResult.put (idglNext,null);
           iDiferencia--;
          }
    } // Fin del while
   } // Fin de if
   return treeMapResult;
 }

  //==========================================================================
  /**
   * Anota como enviado un HSACK para id_tpdu.
   * @param id_tpdu
   */
  public void setEnviadoHSACK (ID_TPDU id_tpdu)
  {
    if (id_tpdu==null)
       return;

    RegistroAsentimientos reg = (RegistroAsentimientos)
                                this.listaIDs_TPDUEnEsperaAsentimiento.get (id_tpdu);
    if (reg==null)
       return;

    reg.bEnviadoHSACK = true;
  }

  //==========================================================================
  /**
   * Devuelve true si para el id_tpdu ya se ha enviado un HSACK.
   * @param id_tpdu
   */
  public boolean enviadoHSACK (ID_TPDU id_tpdu)
  {
    if (id_tpdu==null)
       return false;

    RegistroAsentimientos reg = (RegistroAsentimientos)
                                this.listaIDs_TPDUEnEsperaAsentimiento.get (id_tpdu);
    if (reg==null)
       return false;

    return (reg.bEnviadoHSACK);
  }

  //==========================================================================
  /**
   * Implementación de la interfaz ID_SocketListener. Elimina toda la
   * información sobre  id_socket.<br>
   * Este método es ejecutado por el thread <b>"ThreadCGL"</b>
   * @param id_socket
   */
   public void removeID_Socket (ID_Socket id_socket)
   {
     // Eliminar todos los id_tpdu que esten pendientes de asentimiento cuya
     // fuente es id_socket
     listaIDs_TPDUEnEsperaAsentimiento.removeID_Socket (id_socket);

     // Recorrer todos los registros de asentimientos y eliminar toda referencia
     // a id_socket.
     Iterator iterador = listaIDs_TPDUEnEsperaAsentimiento.iteradorObjetos();
     RegistroAsentimientos regNext = null;
     while (iterador.hasNext ())
     {
         regNext = (RegistroAsentimientos) iterador.next ();
         // Eliminar las referencias a id_socket
         regNext.treeMapEnviadoACK.remove (id_socket);
     } // Fin del while
   }

  //==========================================================================
  /**
   * Implementación de la interfaz IDGLListener. Elimina toda la información
   * sobre idgl.<br>
   * Este método es ejecutado por el thread <b>"ThreadCGL"</b>
   * @param idgl
   */
   public void removeIDGL(IDGL idgl)
   {
     // Recorrer todos los registros de asentimientos y eliminar toda referencia
     // a idgl.
     Iterator iterador = listaIDs_TPDUEnEsperaAsentimiento.iteradorObjetos();
     RegistroAsentimientos regNext = null;
     while (iterador.hasNext ())
     {
         regNext = (RegistroAsentimientos) iterador.next ();
         // Eliminar las referencias a idgl
         regNext.treeMapEnviadoHACK.remove (idgl);
         regNext.treeMapEnviadoHSACK.remove (idgl);
     }
   }

 //===========================================================================
 /**
  * Devuelve una cadena informativa.
  */
 public String toString ()
  {
   return this.listaIDs_TPDUEnEsperaAsentimiento.toString ();
  }

} // Fin de la clase TablaAsentimientos

//-----------------------------------------------------------------------------
//                      CLASE  RegistroAsentimientos
//-----------------------------------------------------------------------------

/**
 * Clase que almacena información sobre los asentimientos recibidos para un
 * ID_TDPU.<br>
 * @see TablaAsentimientos#listaIDs_TPDUEnEsperaAsentimiento
 * Esta clase no es thread-safe.
 * @version  1.0
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:garcia@arconet.es">(garcia@arconet.es)</A><p>
 *			   Antonio Berrocal Piris
 */
 class RegistroAsentimientos implements Cloneable
  {
   // ATRIBUTOS
   /**
    * Almacena la información sobre los id_socket que han enviado ACK.
    * <table border=1>
    *  <tr>  <td><b>Key:</b></td>
    *	    <td>{@link ID_Socket}</td>
    *  </tr>
    *  <tr>  <td><b>Value:</b></td>
    *	    <td>NULL</td>
    *  </tr>
    * </table>
    */
   TreeMap treeMapEnviadoACK   = null;

   /**
    * Almacena la información sobre los IDGL que han enviado HACK
    * <table border=1>
    *  <tr>  <td><b>Key:</b></td>
    *	    <td>{@link IDGL}</td>
    *  </tr>
    *  <tr>  <td><b>Value:</b></td>
    *	    <td>NULL</td>
    *  </tr>
    * </table>
    */
   TreeMap treeMapEnviadoHACK  = null;

   /**
    * Almacena la información sobre los IDGL que han enviado HSACK
    * <table border=1>
    *  <tr>  <td><b>Key:</b></td>
    *	    <td>{@link IDGL}</td>
    *  </tr>
    *  <tr>  <td><b>Value:</b></td>
    *	    <td>NULL</td>
    *  </tr>
    * </table>
    */
   TreeMap treeMapEnviadoHSACK = null;

   /** Indica si se ha enviado un HSACK para el tpdu */
   boolean bEnviadoHSACK = false;

   /** Número de ráfaga a la que pertenece al id_tpdu asociado */
   int iNumeroRafaga = -1;

   //==========================================================================
   /**
    * Crea las estructuras de datos necesarias.
    */
    public RegistroAsentimientos ()
    {
     this.treeMapEnviadoACK   = new TreeMap ();
     this.treeMapEnviadoHACK  = new TreeMap ();
     this.treeMapEnviadoHSACK = new TreeMap ();
    }

   //==========================================================================
   /**
    * Constructor privado para usar en la clonación.
    */
    private RegistroAsentimientos (int iNoUsado)
    {
    }
   //==========================================================================
   /**
    * Devuelve una cadena informativa del registro.
    */
   public String toString ()
   {
    return "ID_Socket han enviado ACK  : " + treeMapEnviadoACK +
           "\nIDGLs han enviado HACK : " + treeMapEnviadoHACK +
           "\nIDGLs han enviado HSACK: " + treeMapEnviadoHSACK +
           "\nEnviadoHSACK: "            + bEnviadoHSACK +
           "\nNumero rafaga: "           + iNumeroRafaga
           ;
   }

   //==========================================================================
   /**
    * Clona el registro
    */
   public Object clone ()
   {
    RegistroAsentimientos regAsentResult = new RegistroAsentimientos (0);
    regAsentResult.treeMapEnviadoACK = (TreeMap)this.treeMapEnviadoACK.clone();
    regAsentResult.treeMapEnviadoHACK = (TreeMap)this.treeMapEnviadoHACK.clone();
    regAsentResult.treeMapEnviadoHSACK = (TreeMap)this.treeMapEnviadoHSACK.clone();
    regAsentResult.bEnviadoHSACK = this.bEnviadoHSACK;
    regAsentResult.iNumeroRafaga = this.iNumeroRafaga;

    return regAsentResult;
   }

} // Fin de la clase RegistroAsentimientos

