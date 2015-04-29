//============================================================
//
//	Copyright (c) 1999-2015. All Rights Reserved.
//
//------------------------------------------------------------
//
//	Fichero: Ventana.java  1.0
//
//
//	Descripci�n: Clase Ventana. Ventana independiente de la implementaci�n
//                   realizada.
//
// 	Authors: 
//		 Alejandro Garc�a-Dom�nguez (alejandro.garcia.dominguez@gmail.com)
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

import java.util.Vector;
import java.util.TreeMap;

/**
 * Clase Ventana. Define los m�todos de una ventana y los redirige a la
 * implementaci�n obtenida del m�todo f�brica de la clase ImpVentana, consiguiendo
 * ser independiente de la implementaci�n concreta.
 * En una ventana se pueden introducir TPDUDatosNormal
 * con n�mero de secuencia comprendidos entre el n�mero de secuencia inicial y
 * final de la ventana.<br>
 * <p><b>Esta clase no est� preparada para trabajar concurrentemente. </b>
 * De la clase TPDUDatosNormal no utiliza nada m�s que su nombre, por lo que puede
 * ser enteramente modificada sin afectar a esta clase.
 * Los n�meros de secuencia los trata como long.
 * @version  1.0
 * @author Antonio Berrocal Piris
 * <A HREF="mailto:AntonioBP@wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
 * M. Alejandro Garc�a Dom�nguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 */

public class Ventana
{
// ATRIBUTOS.
/**
 * Instancia que implementa los m�todos de la ventana.
 */
private ImpVentana impVentana;

/**  QUITAR
 * Instante de tiempo del �ltimo paquete de esta ventana que se ha enviado o
 * recibido de la red.
 */
long lTiempo = 0;

 /** N�mero de paquetes en la ventana */
 //private int num_paquetes = 0;

 /** Tama�o nuevo de la ventana */
 private int iNuevo_tama�o = 0;

 /** TreeMap con los n�meros de secuencia de los TPDU que han sido a�adidos
     a la ventana y que tienen el bit ACK activado */
 private TreeMap treeMapNSecTPDUConACK = null;


//==========================================================================
/**
 * Invoca el m�todo f�brica de {@link ImpVentana} para obtener una implementaci�n
 * concreta de ventana.
 * @param tama�oMaximo tama�o m�ximo de la ventana
 * @param nSecInicial n�mero de secuencia inicial de la ventana
 * @exception ParametroInvalidoExcepcion lanzada cuando el n�mero de secuencia
 * inicial no es v�lido.
 */
public Ventana (int iTama�oMaximo,long lNSecInicial)
                                throws ParametroInvalidoExcepcion
 {
  this.impVentana = ImpVentana.getImpVentana (iTama�oMaximo,lNSecInicial);
  treeMapNSecTPDUConACK = new TreeMap ();
 }


//==========================================================================
/**
 * Devuelve el n�mero de secuencia inicial de la ventana.
 * @return n�mero de secuencia inicial de la ventana.
 */
public long getNumSecInicial ()
{
 if (this.impVentana!=null)
    return this.impVentana.impGetNumSecInicial();
 return -1;
}


//==========================================================================
/**
 * Devuelve el n�mero de secuencia final de la ventana.
 * <p>Devuelve un n�mero negativo en caso de que el tama�o m�ximo de la ventana
 * sea cero (no existe n�mero de secuencia final).
 * @return n�mero de secuencia final de la ventana, -1 en si el tama�o
 * de la ventana es cero.
 */
public long getNumSecFinal ()
{
  return this.impVentana.impGetNumSecFinal();
}

//===========================================================================
/**
 * Devuelve el n�mero de secuencia m�s alto que verifica que todos los menores
 * o iguales a �l tiene un TPDUDatosNormal asociado.
 * @return el n�mero de secuencia mayor consecutivo, o -1 si no lo hay.
 */
public long getNumSecConsecutivo ()
{
 return this.impVentana.impGetNumSecConsecutivo ();
}


//===========================================================================
/**
 * Indica si nSec verifica, que todos los n�meros de secuencia de la ventana
 * menores o iguales a �l tienen un TPDUDatosNormal asociado.
 * IMP: SI NSEC ES MENOR AL INICIAL DEVUELVE TRUE
 * @param nSec n�mero de secuencia a verificar.
 * @return true si desde el n�mero de secuencia inicial de la ventana hasta
 * nSec todos tienen un TPDUDatosNormal asociado, false en caso contrario.
 */
public boolean esConsecutivo (long lNSec)
{

 // Si es menor que el n�mero de secuencia inicial de la ventana
 // devuelve true.
 if (this.getNumSecInicial()>lNSec)
   return true;
 if (this.getNumSecConsecutivo ()>= lNSec)
    return true;
 return false;
}

//==========================================================================
/**
 * Indica si la ventana contiene un TPDUDatosNormal con el n�mero de secuencia
 * indicado.
 * @param nSec n�mero de secuencia del TPDUDatosNormal que se quiere comprobar.
 * @return true si la ventana lo contiene,, false en caso contrario.
 */
public boolean contieneTPDUDatosNormal (long lNSec)
{
 return this.impVentana.impContieneTPDUDatosNormal (lNSec);
}


//==========================================================================
/**
 * Devuelve true si el n�mero de secuencia pasado por argumento est� comprendido
 * entre el n�mero de secuencia inicial y final de la ventana (incluidos).
 * No indica que la ventana contenga un TPDU asociado al n�mero de secuencia
 * indicado.
 */
public boolean nSecEntreInicialyFinal (long lNSec)
{
  if( (getNumSecInicial() <= lNSec)
        &&
      (getNumSecFinal()   >= lNSec) )
      return true;

  return false;
}




//==========================================================================
/**
 * A�ade un TPDUDatosNormal a la ventana. El n�mero de secuencia debe estar comprendido
 * entre el n�mero de secuencia inicial y final de la ventana.
 * <p>El n�mero de secuencia indicado ser� el que se asocie al TPDUDatosNormal por lo
 * que este deber� corresponder con el n�mero de secuencia verdadero del TPDUDatosNormal.
 * @param nSec n�mero de secuencia del TPDUDatosNormal que se quiere a�adir.
 * @return el TPDUDatosNormal, si existe, que estaba asociado al n�mero de secuencia indicado,
 * o null si no existe.
 * @exception ParametroInvalidoExcepcion excepci�n lanzada cuando el n�mero de
 * secuencia no est� comprendido entre el n�mero de secuencia inicial y final
 * de la ventana.
 */
public TPDUDatosNormal addTPDUDatosNormal (long lNSec,TPDUDatosNormal pqt)
                                throws ParametroInvalidoExcepcion
{
  TPDUDatosNormal tpdu  =null;

  //Comprobar que no se ha redimensionado la ventana
  if( this.iNuevo_tama�o != 0)
  {
      if (this.iNuevo_tama�o > this.impVentana.impGetTama�oVentana())
        throw new ParametroInvalidoExcepcion("Ventana llena");
      else
      {
        this.impVentana.impCambiarCapacidadVentana(iNuevo_tama�o);
        this.iNuevo_tama�o = 0;
      }
  }

  // A�adir el TPDU, si no se ha podido a�adir se lanza una excepci�n,
  // as� que no controlamos el tama�o aqu�, solo lo incrementamos.
  tpdu = this.impVentana.impAddTPDUDatosNormal (lNSec,pqt);

  if (pqt.getACK())
      treeMapNSecTPDUConACK.put (pqt.getNumeroSecuencia(),null);

  return tpdu;


}


//==========================================================================
/**
 * Elimina todos los TPDUDatosNormal de la ventana y fija un nuevo n�mero inicial.
 * El tama�o m�ximo de la ventana no es alterado.
 * @param nSec nuevo n�mero de secuencia inicial de la ventana.
 * @exception ParametroInvalidoExcepcion lanzada cuando el n�mero de secuencia
 * pasado en el argumento no es v�lido.
 */
public void reiniciar (long lNSec) throws ParametroInvalidoExcepcion
{
  this.impVentana.impReiniciar (lNSec);

  treeMapNSecTPDUConACK.clear();

}


//==========================================================================
/**
 * Obtiene el TPDUDatosNormal que en esta ventana est� asociado al n�mero de secuencia
 * indicado  (no lo elimina).
 * <p>Se retorna una referencia al TPDUDatosNormal pedido, por lo que cualquier
 * modificaci�n que se le haga se reflejar� en la ventana, y viceversa.
 * @param nSec n�mero de secuencia del TPDUDatosNormal.
 * @return el TPDUDatosNormal contenido en esta ventana y asociado al n�mero de secuencia
 * indicado, o null si no hay un TPDUDatosNormal asociado o si el n�mero de secuencia
 * no est� comprendido entre el n�mero de secuencia inicial y final de la ventana.
 */
public TPDUDatosNormal getTPDUDatosNormal (long lNSec)
{
  return this.impVentana.impGetTPDUDatosNormal(lNSec);
}

//==========================================================================
/**
 * Obtiene todos los TPDUDatosNormal con n�mero de secuencia menor o igual al
 * indicado (no los elimina). Si el nSec es mayor que el n�mero de sec
 * final de la ventana, obtiene todos los TPDUDatosNormal que haya en la ventana.
 * <p>El vector retornado contiene las referencias a los TPDUDatosNormal pedidos,
 * por lo que cualquier modificaci�n de los mismos se refleja en los
 * TPDUDatosNormal de la ventana.
 * @param nSec n�mero de secuencia.
 * @return vector con los TPDUDatosNormal pedidos o vac�o si no hay ninguno
 */
public Vector getTPDUDatosNormalMenorIgual (long lNSec)
{
  return this.impVentana.impGetTPDUDatosNormalMenorIgual (lNSec);
}


//==========================================================================
/**
 * Obtiene el TPDUDatosNormal con n�mero de secuencia mayor (no lo elimina).
 * <p>Se retorna una referencia al TPDUDatosNormal pedido, por lo que cualquier
 * modificaci�n que se le haga se reflejar� en la ventana, y viceversa.
 * @return el TPDUDatosNormal contenido en esta ventana con n�mero de secuencia mayor,
 * o null si no lo hay.
 */
public TPDUDatosNormal getTPDUNSecMayor ()
{
 // Optimizar, para que esto lo ofrezca la propia implementaci�n.
 return this.impVentana.impGetTPDUDatosNormal (this.getNumSecMayor());
}


//==========================================================================
/**
 * Incrementa en una unidad el n�mero de secuencia inicial y final, eliminando
 * el TPDUDatosNormal, si existe, asociado al n�mero de secuencia inicial.
 * @return el TPDUDatosNormal asociado al n�mero de secuencia inicial de la ventana,
 * o null si no hay un TPDUDatosNormal asociado.
 */
public TPDUDatosNormal removeTPDUDatosNormal ()
{
  final String mn = "Ventana.removeTPDUDatosNoraml()";
  TPDUDatosNormal tpdu = null;

  //Log.debug (Log.VENTANA,mn,"");

  tpdu = this.impVentana.impRemoveTPDUDatosNormal ();

  if ((this.iNuevo_tama�o != 0) && (this.iNuevo_tama�o >= this.impVentana.impGetTama�oVentana()))
  {
        this.impVentana.impCambiarCapacidadVentana(iNuevo_tama�o);
        this.iNuevo_tama�o = 0;
  }

  if (tpdu!=null && tpdu.getACK())
     treeMapNSecTPDUConACK.remove (tpdu.getNumeroSecuencia());

  return tpdu;
}


//==========================================================================
/**
 * Elimina todos los TPDUDatosNormal cuyo n�mero de secuencia asociado sea menor o igual
 * al indicado, si �ste es mayor que el n�mero de secuencia final de la ventana
 * se eliminan todos los TPDUDatosNormal.
 * <p>El n�mero de secuencia inicial pasa a ser el siguiente al indicado en el
 * argumento, salvo que �ste sea mayor que el n�mero de secuencia final de la
 * ventana, en cuyo caso pasa a ser el siguiente al n�mero de secuencia final
 * de la ventana. El n�mero de secuencia final se incrementa en la misma proporci�n
 * que el inicial.
 * @param nSec n�mero de secuencia
 */
public void removeTPDUDatosNormal (long lNSec)
{
  final String mn = "Ventana.removeTPDUDatosNoraml(nSec)";

  //Log.debug (Log.VENTANA,mn,""+nSec);

  this.impVentana.impRemoveTPDUDatosNormal(lNSec);
  if ((this.iNuevo_tama�o != 0) && (this.iNuevo_tama�o >= this.impVentana.impGetTama�oVentana()))
  {
        this.impVentana.impCambiarCapacidadVentana(iNuevo_tama�o);
        this.iNuevo_tama�o = 0;
  }

  // Me quedo con los estrictamente menor:
  try{
    this.treeMapNSecTPDUConACK.headMap (new NumeroSecuencia (lNSec+1)).clear();
  }catch (ParametroInvalidoExcepcion e){;}

  //Log.debug (Log.VENTANA,mn,"NSecInicial Vta: " + this.getNumSecInicial());

}

//==========================================================================
/**
 * Obtiene el tama�o m�ximo de la ventana. �ste indica cuantos pares
 * [n�meros de secuencia,TPDUDatosNormal] caben en la ventana.
 * @return el tama�o m�ximo de la ventana.
 */
public int getTama�oMaximoVentana ()
{
  return this.impVentana.impGetCapacidadVentana ();
}


//==========================================================================
/**
 * Redimensiona la ventana al nuevo tama�o m�ximo, adaptando el n�mero de
 * secuencia final. El n�mero de secuencia inicial no es alterado.
 * <p>
 * <B>LA VENTANA NO ELIMINA LOS TPDU CUANDO SE REDUCE LA VENTANA, ESPERA A QUE
 * SE ENTREGEN AL USUARIO O A LA RED PARA DESPU�S REDUCIR EL TAMA�O.</B>
 * @param nuevoTama�oMaximo el nuevo tama�o m�ximo de la ventana.
 * @return vector con TPDUDatosNormal eliminados o vac�o.
 */

public void setCapacidadVentana (int iNuevoTama�oMaximo)
{
  this.iNuevo_tama�o = iNuevoTama�oMaximo;

  if (this.impVentana.impGetTama�oVentana() <= this.iNuevo_tama�o)
  {
    this.impVentana.impCambiarCapacidadVentana(this.iNuevo_tama�o);
    this.iNuevo_tama�o = 0;
  }
}

//==========================================================================
/**
 * De entre los n�meros de secuencia de la ventana con TPDUDatosNormal asociado, devuelve
 * el mayor.
 * @return n�mero de secuencia mayor con TPDUDatosNormal asociado, o -1 si ning�n
 * n�mero de secuencia de la ventana tiene TPDUDatosNormal asociado.
 */
public long getNumSecMayor ()
{
  return this.impVentana.impGetNumSecMayor ();
}

  //============================================================================
  /**
   * Devuelve un treemap con los n�meros de secuencia de los tpdu que tienen el
   * bit ACK activado. Esta indexado por NumeroSecuencia al que se le asocia null.
   * Cualquier cambio en este treemap afecta al almacenado en la ventana y viceversa.
   */
/*  public TreeMap getTreeMapNSecTPDUConACK ()
   {
    return this.treeMapNSecTPDUConACK;
   }
  */
  //============================================================================
  /**
   * Devuelve el primer TPDU de la ventana que tenga el bit ACK activado. Si no
   * hay ninguno devuelve null.
   */
   public TPDUDatosNormal getPrimerTPDUConACKActivado ()
   {
    if (this.treeMapNSecTPDUConACK.isEmpty())
      return null;

    NumeroSecuencia nSec = (NumeroSecuencia)this.treeMapNSecTPDUConACK.firstKey();

    return getTPDUDatosNormal (nSec.tolong());
   }




  //============================================================================
  /**
   * Devuelve el tiempo en el cual se envi� o recibi� de la red un TPDU de esta
   * ventana.
   */
/*  public long getTiempo ()
  {
   return this.tiempo;
  }
  */
  //============================================================================
  /** SE SUPONE QUE TIENE QUE SER SUPERIOR AL QUE HAB�A , PERO COMO ES MANTENIDO
      DESDE FUERA, NO ME PREOCUPO.
   * Modifica el valor del tiempo en el cual se ha enviado o recibido de la red
   * un TPDU de esta ventana.
   * @param tiempoMsg  tiempo en milisegundos.
   */
/*  public void setTiempo (long tiempoMsg)
  {
   this.tiempo = tiempoMsg;
  }
  */

  public String toString ()
  {
   return this.impVentana.toString();
  }

}
