//============================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	Fichero: ImpVentanaArray.java  1.0
//
//	Description: Clase ImpVentanaArray. Implementa la clase abstracta
//                   ImpVentana, utilizando memoria estática, la cual es
//                   reservada en el constructor.
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
//------------------------------------------------------------



package ptmf;

import java.util.*;


/**
 * Clase que implementa una ventana.
 * <p>En una ventana se pueden introducir TPDUDatosNormal
 * con número de secuencia comprendidos entre el número de secuencia inicial y
 * final de la ventana (inclusives).
 * En el constructor se localiza toda la memoria necesaria para guardar
 * los TPDUDatosNormal que se vayan añadiendo, este almacenamiento consite en un
 * array de TPDUDatosNormal.
 * <p><b>Esta clase no está preparada para trabajar concurrentemente. <\b>
 * De la clase TPDUDatosNormal no utiliza, nada más que su nombre, por lo que puede
 * ser enteramente modificada sin afectar a esta clase.
 * Almacena el número de secuencia como un long.
 * @version  1.0
 * @author Antonio Berrocal Piris
 * <A HREF="mailto:AntonioBP@wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
 * M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(Malejandro.garcia.dominguez@gmail.com)</A><p>
 */
public class ImpVentanaArray extends ImpVentana
{

//==========================================================================
/**
 * Crea una ventana con el tamaño máximo especificado.
 * El tamaño tiene que ser mayor que cero, de lo contrario se creara la ventana
 * con un tamaño máximo de cero, no pudiéndose añadir ningún TPDUDatosNormal.
 * @param tamañoMaximo número máximo de TPDUDatosNormal que caben en la ventana
 * @param nSecInic número de secuencia inicial
 * @exception ParametroInvalidoExcepcion lanzada cuando el número de secuencia
 * inicial no es válido.
 */
public ImpVentanaArray (int iTamañoMaximo,long lNSecInic) throws ParametroInvalidoExcepcion
{

 if (lNSecInic<0)
     {
       this.iCapacidadVentana=0;
       this.iIndicePrimerTPDUDatosNormal=0;
       this.lNumeroSecInicial = -1;
       //throw new PTMFExcepcionNoVerificada ();
       throw new ParametroInvalidoExcepcion ("Número de sec " + lNSecInic
                                                + " no es válido.");
     }

 if (iTamañoMaximo>0)
   {
    this.vectorTPDUDatosNormal = new TPDUDatosNormal[iTamañoMaximo]; // Almacenará los TPDUDatosNormal ordenados por número de secuencia.
    if (this.vectorTPDUDatosNormal == null)
       throw new OutOfMemoryError ("No se pudo localizar memoria para crear la ventana.");
    this.iCapacidadVentana = iTamañoMaximo;
   }
 else {
         this.iIndicePrimerTPDUDatosNormal = 0;
         this.iCapacidadVentana = 0;
        }
 this.lNumeroSecInicial = lNSecInic;
 this.lNumeroSecConsecutivo = -1;
}

//==========================================================================
/**
 * Incrementa el indice circularmente.
 * @param iIndice indice a incrementar
 * @result el incremento de indice
 */
private int incrementarIndiceCircular (int iIndice)
{
  if ((iIndice + 1)>=this.iCapacidadVentana)
        return 0;
  return (iIndice + 1);
}


//==========================================================================
/**
 * Dado un número de secuencia devuelve el indice (posición) que ocupa
 * en el vector.
 * @param lNSec número de secuencia a convertir en indice
 * @result indice correspondiente al número de secuencia
 */
private int numSecAIndiceVector (long lNSec)
{

  // El nSec tiene que ser >= que el número de secuencia inicial de la ventana.
  // El nSec tiene que ser <= que el número de secuencia final de la ventana.
  if ( (lNSec<impGetNumSecInicial()) || (lNSec>impGetNumSecFinal ()))
   return -1;

  // Calcular la posición que le corresponde a nSec en el vector.
  return (int)(((lNSec-impGetNumSecInicial())+this.iIndicePrimerTPDUDatosNormal)
             %this.iCapacidadVentana);
}



//==========================================================================
/**
 * Devuelve el número de secuencia inicial de la ventana.
 * @return número de secuencia inicial de la ventana.
 */
public long impGetNumSecInicial ()
{
 return this.lNumeroSecInicial;
}

//==========================================================================
/**
 * Devuelve el número de secuencia final de la ventana.
 * <p>Devuelve un número negativo en caso de que el tamaño máximo de la ventana
 * sea cero (no existe número de secuencia final).
 * @return número de secuencia final de la ventana, -1 en si el tamaño
 * de la ventana es cero.
 */
public long impGetNumSecFinal ()
{
 if (this.iCapacidadVentana>0)
    return this.lNumeroSecInicial + this.iCapacidadVentana - 1;
 return -1;
}


//===========================================================================
/**
 * Devuelve el número de secuencia más alto que verifica que todos los menores
 * o iguales a él tienen un TPDUDatosNormal asociado.
 * @return el número de secuencia mayor consecutivo, o -1 si no lo hay.
 */
public long impGetNumSecConsecutivo ()
{
 return this.lNumeroSecConsecutivo;
}


//==========================================================================
/**
 * Indica si la ventana contiene un TPDUDatosNormal con el número de secuencia
 * indicado.
 * @param lNSec número de secuencia del TPDUDatosNormal que se quiere comprobar.
 * @return true si la ventana lo contiene,, false en caso contrario.
 */
public boolean impContieneTPDUDatosNormal (long lNSec)
{
 int iIndice;

 iIndice = numSecAIndiceVector (lNSec);
 if (iIndice>=0)
   return (this.vectorTPDUDatosNormal[iIndice]!=null);

 return false;
}


//==========================================================================
/**
 * Añade un TPDUDatosNormal a la ventana. El número de secuencia debe estar comprendido
 * entre el número de secuencia inicial y final de la ventana.
 * <p>El número de secuencia indicado será el que se asocie al TPDUDatosNormal por lo
 * que este deberá corresponder con el número de secuencia verdadero del TPDUDatosNormal.
 * @param nSec número de secuencia del TPDUDatosNormal que se quiere añadir.
 * @param pqt TPDUDatosNormal a añadir
 * @return el TPDUDatosNormal, si existe, que estaba asociado al número de secuencia indicado,
 * o null si no existe.
 * @exception ParametroInvalidoExcepcion excepción lanzada cuando el número de
 * secuencia no está comprendido entre el número de secuencia inicial y final
 * de la ventana.
 */
public TPDUDatosNormal impAddTPDUDatosNormal (long lNSec,TPDUDatosNormal pqt)
                                throws ParametroInvalidoExcepcion
{
 int iIndice;
 TPDUDatosNormal pqtAntiguo;


 iIndice = numSecAIndiceVector (lNSec);
 if (iIndice>=0)
  {
    pqtAntiguo = this.vectorTPDUDatosNormal[iIndice];
    this.vectorTPDUDatosNormal[iIndice] = pqt;

    // Comparar si se ha modificado el numeroSecConsecutivo
    if (this.lNumeroSecConsecutivo == -1)
      // Comprobar cual es el más alto consecutivo
      {
       for (long lNSecIt = this.impGetNumSecInicial();
                                    lNSec <= this.impGetNumSecFinal();lNSecIt++)
         if (!this.impContieneTPDUDatosNormal (lNSecIt))
                break;
         else this.lNumeroSecConsecutivo = lNSecIt;
       }
    else { // Ver si es continuación del ya registrado
           if ((lNSec - this.lNumeroSecConsecutivo)==1)
            for (long lNSecIt = lNSec; lNSec <= this.impGetNumSecFinal();lNSecIt++)
                  if (!this.impContieneTPDUDatosNormal (lNSecIt))
                     break;
                  else this.lNumeroSecConsecutivo = lNSecIt;
         }

    return pqtAntiguo;
  }
 else throw new ParametroInvalidoExcepcion ("El número de secuencia "+lNSec+
                                 ", no puede ser añadido a esta ventana.");

} // Fin de addTPDUDatosNormal


//==========================================================================
/**
 * Elimina todos los TPDUDatosNormal de la ventana y fija un nuevo número inicial.
 * El tamaño máximo de la ventana no es alterado.
 * @param lNSec nuevo número de secuencia inicial de la ventana.
 * @exception ParametroInvalidoExcepcion lanzada cuando el número de secuencia
 * pasado en el argumento no es válido.
 */
public void impReiniciar (long lNSec) throws ParametroInvalidoExcepcion
{

 if (lNSec<0)
       throw new ParametroInvalidoExcepcion ("No es válido un número de secuencia menor que cero.");

 this.iIndicePrimerTPDUDatosNormal = 0;
 this.lNumeroSecInicial = lNSec;
 this.lNumeroSecConsecutivo = -1;
 for (int i=0;i<this.iCapacidadVentana;i++)
        this.vectorTPDUDatosNormal[i] = null;
}

//==========================================================================
/**
 * Obtiene el TPDUDatosNormal que en esta ventana está asociado al número de secuencia
 * indicado (no lo elimina).
 * <p>Se retorna una referencia al TPDUDatosNormal pedido, por lo que cualquier
 * modificación que se le haga se reflejará en la ventana, y viceversa.
 * @param lNSec número de secuencia del TPDUDatosNormal.
 * @return el TPDUDatosNormal contenido en esta ventana y asociado al número de secuencia
 * indicado, o null si no hay un TPDUDatosNormal asociado o si el número de secuencia
 * no está comprendido entre el número de secuencia inicial y final de la ventana.
 */
public TPDUDatosNormal impGetTPDUDatosNormal (long lNSec)
{
 int iIndice;

 iIndice = numSecAIndiceVector (lNSec);

 if (iIndice>=0)
   return (this.vectorTPDUDatosNormal[iIndice]);

 return (null);
} // Fin de impGetTPDUDatosNormal


//==========================================================================
/**
 * Obtiene todos los TPDUDatosNormal con número de secuencia menor o igual al
 * indicado (no los elimina). Si el nSec es mayor que el número de sec
 * final de la ventana, obtiene todos los TPDUDatosNormal que haya en la ventana.
 * <p>El vector retornado contiene las referencias a los TPDUDatosNormal pedidos,
 * por lo que cualquier modificación de los mismos se refleja en los
 * TPDUDatosNormal de la ventana, y viceversa.
 * @param lNSec número de secuencia.
 * @return vector con los TPDUDatosNormal pedidos o vacío si no hay ninguno
 */
public Vector impGetTPDUDatosNormalMenorIgual (long lNSec)
{
 Vector vector = new Vector();
 TPDUDatosNormal pqt;

 // Si el capacidadVentana es cero el nSec valdrá < 0
 if (lNSec>impGetNumSecFinal())
        lNSec = impGetNumSecFinal ();

 for (long lNSecAux = impGetNumSecInicial ();lNSecAux <=lNSec;lNSecAux++)
  {
   pqt = this.vectorTPDUDatosNormal[numSecAIndiceVector(lNSecAux)];
   if (pqt!=null)
      vector.add (pqt);
  }

 return vector;
} // Fin de impGetTPDUDatosNormalMenorIgual


//==========================================================================
/**
 * Incrementa en una unidad el número de secuencia inicial y final, eliminando
 * el TPDUDatosNormal, si existe, asociado al número de secuencia inicial.
 * @return el TPDUDatosNormal asociado al número de secuencia inicial de la ventana,
 * o null si no hay un TPDUDatosNormal asociado.
 */
public TPDUDatosNormal impRemoveTPDUDatosNormal ()
{
 TPDUDatosNormal pqt;

   if (this.iCapacidadVentana==0)
     return null;

   // Actualizar el valor de numeroSecConsecutivo
   if (this.lNumeroSecConsecutivo==this.impGetNumSecInicial())
        this.lNumeroSecConsecutivo = -1;

   pqt = this.vectorTPDUDatosNormal[this.iIndicePrimerTPDUDatosNormal];
   this.vectorTPDUDatosNormal[this.iIndicePrimerTPDUDatosNormal] = null;
   this.lNumeroSecInicial ++;
   this.iIndicePrimerTPDUDatosNormal = this.incrementarIndiceCircular (this.iIndicePrimerTPDUDatosNormal);


   return pqt;
} // Fin de removeTPDUDatosNormal

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
 * @param lNSec número de secuencia
 */
public void impRemoveTPDUDatosNormal (long lNSec)
{

 if (this.iCapacidadVentana==0)
     return;


 if (lNSec>impGetNumSecFinal())
   {
    try{
    impReiniciar (impGetNumSecFinal()+1);
    }catch (ParametroInvalidoExcepcion e) {}
    return;
    }

 while ((lNSec>=impGetNumSecInicial())&&(lNSec<=impGetNumSecFinal()))
  {
   this.vectorTPDUDatosNormal[this.iIndicePrimerTPDUDatosNormal] = null;
   this.lNumeroSecInicial ++;
   this.iIndicePrimerTPDUDatosNormal = this.incrementarIndiceCircular (this.iIndicePrimerTPDUDatosNormal);
  }

 // Actualizar numeroSecConsecutivo
 if (this.lNumeroSecConsecutivo<this.impGetNumSecInicial())
   {
    this.lNumeroSecConsecutivo = -1;
    for (long lNSecIt = this.impGetNumSecInicial();
                                    lNSec <= this.impGetNumSecFinal();lNSecIt++)
      if (!this.impContieneTPDUDatosNormal (lNSecIt))
             break;
      else this.lNumeroSecConsecutivo = lNSecIt;
    }

}


//==========================================================================
/**
 * Obtiene la capacidad de la ventana. Éste indica cuantos pares
 * [números de secuencia,TPDUDatosNormal] caben en la ventana.
 * @return el tamaño máximo de la ventana.
 */
public int impGetCapacidadVentana ()
{
 return this.iCapacidadVentana;
}

//==========================================================================
/**
 * Redimensiona la ventana a la nueva capacidad adaptando el número de
 * secuencia final. El número de secuencia inicial no es alterado.
 * <p>Si la nueva capacidad es inferior a la anterior, se eliminan los TPDUDatosNormal
 * con número de secuencia asociado superior hasta adaptar el tamaño. Si es
 * superior, no se elimina ningún TPDUDatosNormal.
 * @param iNuevoTamañoMaximo la nueva capacidad de la ventana.
 * @return vector con TPDUDatosNormal eliminados o vacío.
 */
public Vector impCambiarCapacidadVentana (int iNuevoTamañoMaximo)
{
 TPDUDatosNormal[] arrayPqts;
 TPDUDatosNormal pqt;
 Vector vectorEliminados = new Vector ();
 long nSec;

  if (iNuevoTamañoMaximo<0)
      return vectorEliminados;

  // No se tiene que tirar ningún TPDUDatosNormal.
  if (iNuevoTamañoMaximo > this.iCapacidadVentana)
   {
    arrayPqts = new TPDUDatosNormal[iNuevoTamañoMaximo];
    // Copiar los TPDUDatosNormal al nuevo array.
    for (int i=0;i<this.iCapacidadVentana;i++)
        arrayPqts[i] = this.vectorTPDUDatosNormal[i];
    // Poner a null las posiciones aún no ocupadas.
    for (int i=this.iCapacidadVentana;i<iNuevoTamañoMaximo;i++)
        arrayPqts[i] = null;
    this.iCapacidadVentana = iNuevoTamañoMaximo;
    this.vectorTPDUDatosNormal = arrayPqts;
   }


  // Hay que tirar los TPDUDatosNormal que no caben en el nuevo tamaño, se comienza
  // a eliminar TPDUDatosNormal por el final.
  // El nuevoTamañoMaximo tiene que ser un número positivo.
  if (iNuevoTamañoMaximo < this.iCapacidadVentana)
   {
    arrayPqts = new TPDUDatosNormal[iNuevoTamañoMaximo];
    // Copiar los TPDUDatosNormal que cogan.
    nSec = impGetNumSecInicial();
    for (int i=0;i<iNuevoTamañoMaximo;i++)
       {
        arrayPqts[i] = this.vectorTPDUDatosNormal[numSecAIndiceVector(nSec)];
        nSec ++; // Incrementar el número de secuencia.
        }
    // Añadir los TPDUDatosNormal eliminados al vector.
    for (int i=iNuevoTamañoMaximo;i<this.iCapacidadVentana;i++)
        {
        pqt = this.vectorTPDUDatosNormal[numSecAIndiceVector(nSec)];
        if (pqt!=null)
            vectorEliminados.addElement(pqt);
        nSec ++; // Incrementar el número de secuencia.
        }

    this.iIndicePrimerTPDUDatosNormal = 0;
    this.iCapacidadVentana = iNuevoTamañoMaximo;
    // Actualizar el numeroSecConsecutivo
    if (this.lNumeroSecConsecutivo > this.impGetNumSecFinal())
        this.lNumeroSecConsecutivo = this.impGetNumSecFinal();
    this.vectorTPDUDatosNormal = arrayPqts;
   }

  return vectorEliminados;
} // Fin de cambiarTamañoVentana

//==========================================================================
/**
 * Obtiene el tamaño de la ventana. Éste indica cuantos pares
 * [números de secuencia,TPDUDatosNormal] hay actualmente en la ventana.
 * @return el tamaño de la ventana.
 */
public long impGetTamañoVentana ()
{
   long lTamaño = 0;
   long lNum_final = this.impGetNumSecFinal();
   long lNum_inicio = this.impGetNumSecInicial();

   // Ojo, que el número puede dar la vuelta, pasar su máximo permitido
   if(lNum_final >= lNum_inicio)
     lTamaño = lNum_final - lNum_inicio;
   else
   {
    // TRATAR CUANDO EL NÚMERO HA DADO LA VUELTA
    Long limite = new Long("9223372036854775807"); //+=1  --> 2147483648: Límite superior de un entero con signo.

    lTamaño = lNum_final + 1/* por el cero que no suma y si cuenta como número de secuencia*/
                + (limite.longValue() - lNum_inicio +1/*se suma uno porque debe de ser 2147483648*/);
   }

   return lTamaño;
}

//==========================================================================
/**
 * De entre los números de secuencia de la ventana con TPDUDatosNormal asociado, devuelve
 * el mayor.
 * @return número de secuencia mayor con TPDUDatosNormal asociado, o -1 si ningún
 * número de secuencia de la ventana tiene TPDUDatosNormal asociado.
 */
public long impGetNumSecMayor ()
{
 long lNSec;

 lNSec = impGetNumSecFinal ();

 while ( (lNSec>=impGetNumSecInicial()) && (!impContieneTPDUDatosNormal(lNSec)) )
        lNSec --; // Decrementar el número de secuencia.

 if (lNSec>=impGetNumSecInicial())
   {
     return lNSec;
    }
 return -1;
}


//==========================================================================
/**
 * Devuelve una cadena informativa.
 */
public String toString ()
{
 return this.vectorTPDUDatosNormal.toString();
}


// ATRIBUTOS

/**
 * Indica cuantos pares [números de secuencia,TPDUDatosNormal] caben en la ventana.
 */
private int iCapacidadVentana = 0;

/**
 * Array que almacena los TPDUDatosNormal ordenados por el número
 * de secuencia.
 * <p>En la posición indicada por "indicePrimerTPDUDatosNormal" se
 * almacena el TPDUDatosNormal asociado al número de secuencia inicial,
 * en la posición siguiente el que tenga asociado el número de secuencia
 * consecutivo al inicial, y así sucesivamente. Es decir, cada posición
 * se corresponde con un número de secuencia, correspondiendo al:
 *   <ul>
 *     <li>"número de secuencia inicial de la ventana", la posición indicada
 *       por "indicePrimerTPDUDatosNormal"</li>
 *     <li>"número de secuencia final de la ventana" , la posición resultante
 *       de incrementar circularmente "indicePrimerTPDUDatosNormal" en "capacidadVentana".</li>
 *   <ul>
 * <p><b>Esta relación es dinámica, de forma que cuando se eliminan TPDUDatosNormal,
 * se desplaza el "indicePrimerTPDUDatosNormal" para que apunte a la posición
 * del nuevo número de secuencia inicial, no teniendo que reasignar los
 * TPDUDatosNormal en el array.</b>
 */
private TPDUDatosNormal[] vectorTPDUDatosNormal = null;


/**
 * Posición en el array del TPDUDatosNormal con número de secuencia menor.
 */
private int iIndicePrimerTPDUDatosNormal;

/**
 * Número de secuencia inicial de la ventana.
 */
private long lNumeroSecInicial;


/**
 * Número de secuencia mayor, consecutivo. Si su valor es -1, indica que
 * no hay ningún número de secuencia consecutivo, lo cual, sólo es posible
 * si no hay un TPDUDatosNormal asociado al número de secuencia inicial de la ventana.
 */
private long lNumeroSecConsecutivo;


}
