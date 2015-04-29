//============================================================
//
//	Copyright (c) 1999-2015. All Rights Reserved.
//
//------------------------------------------------------------
//
//	Fichero: Ventana.java  1.0
//
//
//	Descripción: Clase Ventana. Ventana independiente de la implementación
//                   realizada.
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

import java.util.Vector;
import java.util.TreeMap;

/**
 * Clase Ventana. Define los métodos de una ventana y los redirige a la
 * implementación obtenida del método fábrica de la clase ImpVentana, consiguiendo
 * ser independiente de la implementación concreta.
 * En una ventana se pueden introducir TPDUDatosNormal
 * con número de secuencia comprendidos entre el número de secuencia inicial y
 * final de la ventana.<br>
 * <p><b>Esta clase no está preparada para trabajar concurrentemente. </b>
 * De la clase TPDUDatosNormal no utiliza nada más que su nombre, por lo que puede
 * ser enteramente modificada sin afectar a esta clase.
 * Los números de secuencia los trata como long.
 * @version  1.0
 * @author Antonio Berrocal Piris
 * <A HREF="mailto:AntonioBP@wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
 * M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 */

public class Ventana
{
// ATRIBUTOS.
/**
 * Instancia que implementa los métodos de la ventana.
 */
private ImpVentana impVentana;

/**  QUITAR
 * Instante de tiempo del último paquete de esta ventana que se ha enviado o
 * recibido de la red.
 */
long lTiempo = 0;

 /** Número de paquetes en la ventana */
 //private int num_paquetes = 0;

 /** Tamaño nuevo de la ventana */
 private int iNuevo_tamaño = 0;

 /** TreeMap con los números de secuencia de los TPDU que han sido añadidos
     a la ventana y que tienen el bit ACK activado */
 private TreeMap treeMapNSecTPDUConACK = null;


//==========================================================================
/**
 * Invoca el método fábrica de {@link ImpVentana} para obtener una implementación
 * concreta de ventana.
 * @param tamañoMaximo tamaño máximo de la ventana
 * @param nSecInicial número de secuencia inicial de la ventana
 * @exception ParametroInvalidoExcepcion lanzada cuando el número de secuencia
 * inicial no es válido.
 */
public Ventana (int iTamañoMaximo,long lNSecInicial)
                                throws ParametroInvalidoExcepcion
 {
  this.impVentana = ImpVentana.getImpVentana (iTamañoMaximo,lNSecInicial);
  treeMapNSecTPDUConACK = new TreeMap ();
 }


//==========================================================================
/**
 * Devuelve el número de secuencia inicial de la ventana.
 * @return número de secuencia inicial de la ventana.
 */
public long getNumSecInicial ()
{
 if (this.impVentana!=null)
    return this.impVentana.impGetNumSecInicial();
 return -1;
}


//==========================================================================
/**
 * Devuelve el número de secuencia final de la ventana.
 * <p>Devuelve un número negativo en caso de que el tamaño máximo de la ventana
 * sea cero (no existe número de secuencia final).
 * @return número de secuencia final de la ventana, -1 en si el tamaño
 * de la ventana es cero.
 */
public long getNumSecFinal ()
{
  return this.impVentana.impGetNumSecFinal();
}

//===========================================================================
/**
 * Devuelve el número de secuencia más alto que verifica que todos los menores
 * o iguales a él tiene un TPDUDatosNormal asociado.
 * @return el número de secuencia mayor consecutivo, o -1 si no lo hay.
 */
public long getNumSecConsecutivo ()
{
 return this.impVentana.impGetNumSecConsecutivo ();
}


//===========================================================================
/**
 * Indica si nSec verifica, que todos los números de secuencia de la ventana
 * menores o iguales a él tienen un TPDUDatosNormal asociado.
 * IMP: SI NSEC ES MENOR AL INICIAL DEVUELVE TRUE
 * @param nSec número de secuencia a verificar.
 * @return true si desde el número de secuencia inicial de la ventana hasta
 * nSec todos tienen un TPDUDatosNormal asociado, false en caso contrario.
 */
public boolean esConsecutivo (long lNSec)
{

 // Si es menor que el número de secuencia inicial de la ventana
 // devuelve true.
 if (this.getNumSecInicial()>lNSec)
   return true;
 if (this.getNumSecConsecutivo ()>= lNSec)
    return true;
 return false;
}

//==========================================================================
/**
 * Indica si la ventana contiene un TPDUDatosNormal con el número de secuencia
 * indicado.
 * @param nSec número de secuencia del TPDUDatosNormal que se quiere comprobar.
 * @return true si la ventana lo contiene,, false en caso contrario.
 */
public boolean contieneTPDUDatosNormal (long lNSec)
{
 return this.impVentana.impContieneTPDUDatosNormal (lNSec);
}


//==========================================================================
/**
 * Devuelve true si el número de secuencia pasado por argumento está comprendido
 * entre el número de secuencia inicial y final de la ventana (incluidos).
 * No indica que la ventana contenga un TPDU asociado al número de secuencia
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
 * Añade un TPDUDatosNormal a la ventana. El número de secuencia debe estar comprendido
 * entre el número de secuencia inicial y final de la ventana.
 * <p>El número de secuencia indicado será el que se asocie al TPDUDatosNormal por lo
 * que este deberá corresponder con el número de secuencia verdadero del TPDUDatosNormal.
 * @param nSec número de secuencia del TPDUDatosNormal que se quiere añadir.
 * @return el TPDUDatosNormal, si existe, que estaba asociado al número de secuencia indicado,
 * o null si no existe.
 * @exception ParametroInvalidoExcepcion excepción lanzada cuando el número de
 * secuencia no está comprendido entre el número de secuencia inicial y final
 * de la ventana.
 */
public TPDUDatosNormal addTPDUDatosNormal (long lNSec,TPDUDatosNormal pqt)
                                throws ParametroInvalidoExcepcion
{
  TPDUDatosNormal tpdu  =null;

  //Comprobar que no se ha redimensionado la ventana
  if( this.iNuevo_tamaño != 0)
  {
      if (this.iNuevo_tamaño > this.impVentana.impGetTamañoVentana())
        throw new ParametroInvalidoExcepcion("Ventana llena");
      else
      {
        this.impVentana.impCambiarCapacidadVentana(iNuevo_tamaño);
        this.iNuevo_tamaño = 0;
      }
  }

  // Añadir el TPDU, si no se ha podido añadir se lanza una excepción,
  // así que no controlamos el tamaño aquí, solo lo incrementamos.
  tpdu = this.impVentana.impAddTPDUDatosNormal (lNSec,pqt);

  if (pqt.getACK())
      treeMapNSecTPDUConACK.put (pqt.getNumeroSecuencia(),null);

  return tpdu;


}


//==========================================================================
/**
 * Elimina todos los TPDUDatosNormal de la ventana y fija un nuevo número inicial.
 * El tamaño máximo de la ventana no es alterado.
 * @param nSec nuevo número de secuencia inicial de la ventana.
 * @exception ParametroInvalidoExcepcion lanzada cuando el número de secuencia
 * pasado en el argumento no es válido.
 */
public void reiniciar (long lNSec) throws ParametroInvalidoExcepcion
{
  this.impVentana.impReiniciar (lNSec);

  treeMapNSecTPDUConACK.clear();

}


//==========================================================================
/**
 * Obtiene el TPDUDatosNormal que en esta ventana está asociado al número de secuencia
 * indicado  (no lo elimina).
 * <p>Se retorna una referencia al TPDUDatosNormal pedido, por lo que cualquier
 * modificación que se le haga se reflejará en la ventana, y viceversa.
 * @param nSec número de secuencia del TPDUDatosNormal.
 * @return el TPDUDatosNormal contenido en esta ventana y asociado al número de secuencia
 * indicado, o null si no hay un TPDUDatosNormal asociado o si el número de secuencia
 * no está comprendido entre el número de secuencia inicial y final de la ventana.
 */
public TPDUDatosNormal getTPDUDatosNormal (long lNSec)
{
  return this.impVentana.impGetTPDUDatosNormal(lNSec);
}

//==========================================================================
/**
 * Obtiene todos los TPDUDatosNormal con número de secuencia menor o igual al
 * indicado (no los elimina). Si el nSec es mayor que el número de sec
 * final de la ventana, obtiene todos los TPDUDatosNormal que haya en la ventana.
 * <p>El vector retornado contiene las referencias a los TPDUDatosNormal pedidos,
 * por lo que cualquier modificación de los mismos se refleja en los
 * TPDUDatosNormal de la ventana.
 * @param nSec número de secuencia.
 * @return vector con los TPDUDatosNormal pedidos o vacío si no hay ninguno
 */
public Vector getTPDUDatosNormalMenorIgual (long lNSec)
{
  return this.impVentana.impGetTPDUDatosNormalMenorIgual (lNSec);
}


//==========================================================================
/**
 * Obtiene el TPDUDatosNormal con número de secuencia mayor (no lo elimina).
 * <p>Se retorna una referencia al TPDUDatosNormal pedido, por lo que cualquier
 * modificación que se le haga se reflejará en la ventana, y viceversa.
 * @return el TPDUDatosNormal contenido en esta ventana con número de secuencia mayor,
 * o null si no lo hay.
 */
public TPDUDatosNormal getTPDUNSecMayor ()
{
 // Optimizar, para que esto lo ofrezca la propia implementación.
 return this.impVentana.impGetTPDUDatosNormal (this.getNumSecMayor());
}


//==========================================================================
/**
 * Incrementa en una unidad el número de secuencia inicial y final, eliminando
 * el TPDUDatosNormal, si existe, asociado al número de secuencia inicial.
 * @return el TPDUDatosNormal asociado al número de secuencia inicial de la ventana,
 * o null si no hay un TPDUDatosNormal asociado.
 */
public TPDUDatosNormal removeTPDUDatosNormal ()
{
  final String mn = "Ventana.removeTPDUDatosNoraml()";
  TPDUDatosNormal tpdu = null;

  //Log.debug (Log.VENTANA,mn,"");

  tpdu = this.impVentana.impRemoveTPDUDatosNormal ();

  if ((this.iNuevo_tamaño != 0) && (this.iNuevo_tamaño >= this.impVentana.impGetTamañoVentana()))
  {
        this.impVentana.impCambiarCapacidadVentana(iNuevo_tamaño);
        this.iNuevo_tamaño = 0;
  }

  if (tpdu!=null && tpdu.getACK())
     treeMapNSecTPDUConACK.remove (tpdu.getNumeroSecuencia());

  return tpdu;
}


//==========================================================================
/**
 * Elimina todos los TPDUDatosNormal cuyo número de secuencia asociado sea menor o igual
 * al indicado, si éste es mayor que el número de secuencia final de la ventana
 * se eliminan todos los TPDUDatosNormal.
 * <p>El número de secuencia inicial pasa a ser el siguiente al indicado en el
 * argumento, salvo que éste sea mayor que el número de secuencia final de la
 * ventana, en cuyo caso pasa a ser el siguiente al número de secuencia final
 * de la ventana. El número de secuencia final se incrementa en la misma proporción
 * que el inicial.
 * @param nSec número de secuencia
 */
public void removeTPDUDatosNormal (long lNSec)
{
  final String mn = "Ventana.removeTPDUDatosNoraml(nSec)";

  //Log.debug (Log.VENTANA,mn,""+nSec);

  this.impVentana.impRemoveTPDUDatosNormal(lNSec);
  if ((this.iNuevo_tamaño != 0) && (this.iNuevo_tamaño >= this.impVentana.impGetTamañoVentana()))
  {
        this.impVentana.impCambiarCapacidadVentana(iNuevo_tamaño);
        this.iNuevo_tamaño = 0;
  }

  // Me quedo con los estrictamente menor:
  try{
    this.treeMapNSecTPDUConACK.headMap (new NumeroSecuencia (lNSec+1)).clear();
  }catch (ParametroInvalidoExcepcion e){;}

  //Log.debug (Log.VENTANA,mn,"NSecInicial Vta: " + this.getNumSecInicial());

}

//==========================================================================
/**
 * Obtiene el tamaño máximo de la ventana. Éste indica cuantos pares
 * [números de secuencia,TPDUDatosNormal] caben en la ventana.
 * @return el tamaño máximo de la ventana.
 */
public int getTamañoMaximoVentana ()
{
  return this.impVentana.impGetCapacidadVentana ();
}


//==========================================================================
/**
 * Redimensiona la ventana al nuevo tamaño máximo, adaptando el número de
 * secuencia final. El número de secuencia inicial no es alterado.
 * <p>
 * <B>LA VENTANA NO ELIMINA LOS TPDU CUANDO SE REDUCE LA VENTANA, ESPERA A QUE
 * SE ENTREGEN AL USUARIO O A LA RED PARA DESPUÉS REDUCIR EL TAMAÑO.</B>
 * @param nuevoTamañoMaximo el nuevo tamaño máximo de la ventana.
 * @return vector con TPDUDatosNormal eliminados o vacío.
 */

public void setCapacidadVentana (int iNuevoTamañoMaximo)
{
  this.iNuevo_tamaño = iNuevoTamañoMaximo;

  if (this.impVentana.impGetTamañoVentana() <= this.iNuevo_tamaño)
  {
    this.impVentana.impCambiarCapacidadVentana(this.iNuevo_tamaño);
    this.iNuevo_tamaño = 0;
  }
}

//==========================================================================
/**
 * De entre los números de secuencia de la ventana con TPDUDatosNormal asociado, devuelve
 * el mayor.
 * @return número de secuencia mayor con TPDUDatosNormal asociado, o -1 si ningún
 * número de secuencia de la ventana tiene TPDUDatosNormal asociado.
 */
public long getNumSecMayor ()
{
  return this.impVentana.impGetNumSecMayor ();
}

  //============================================================================
  /**
   * Devuelve un treemap con los números de secuencia de los tpdu que tienen el
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
   * Devuelve el tiempo en el cual se envió o recibió de la red un TPDU de esta
   * ventana.
   */
/*  public long getTiempo ()
  {
   return this.tiempo;
  }
  */
  //============================================================================
  /** SE SUPONE QUE TIENE QUE SER SUPERIOR AL QUE HABÍA , PERO COMO ES MANTENIDO
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
