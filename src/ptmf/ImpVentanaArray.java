//============================================================
//
//	Copyright (c) 1999-2015 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	Fichero: ImpVentanaArray.java  1.0
//
//	Description: Clase ImpVentanaArray. Implementa la clase abstracta
//                   ImpVentana, utilizando memoria est�tica, la cual es
//                   reservada en el constructor.
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
//------------------------------------------------------------



package ptmf;

import java.util.*;


/**
 * Clase que implementa una ventana.
 * <p>En una ventana se pueden introducir TPDUDatosNormal
 * con n�mero de secuencia comprendidos entre el n�mero de secuencia inicial y
 * final de la ventana (inclusives).
 * En el constructor se localiza toda la memoria necesaria para guardar
 * los TPDUDatosNormal que se vayan a�adiendo, este almacenamiento consite en un
 * array de TPDUDatosNormal.
 * <p><b>Esta clase no est� preparada para trabajar concurrentemente. <\b>
 * De la clase TPDUDatosNormal no utiliza, nada m�s que su nombre, por lo que puede
 * ser enteramente modificada sin afectar a esta clase.
 * Almacena el n�mero de secuencia como un long.
 * @version  1.0
 * @author Antonio Berrocal Piris
 * <A HREF="mailto:AntonioBP@wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
 * M. Alejandro Garc�a Dom�nguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(Malejandro.garcia.dominguez@gmail.com)</A><p>
 */
public class ImpVentanaArray extends ImpVentana
{

//==========================================================================
/**
 * Crea una ventana con el tama�o m�ximo especificado.
 * El tama�o tiene que ser mayor que cero, de lo contrario se creara la ventana
 * con un tama�o m�ximo de cero, no pudi�ndose a�adir ning�n TPDUDatosNormal.
 * @param tama�oMaximo n�mero m�ximo de TPDUDatosNormal que caben en la ventana
 * @param nSecInic n�mero de secuencia inicial
 * @exception ParametroInvalidoExcepcion lanzada cuando el n�mero de secuencia
 * inicial no es v�lido.
 */
public ImpVentanaArray (int iTama�oMaximo,long lNSecInic) throws ParametroInvalidoExcepcion
{

 if (lNSecInic<0)
     {
       this.iCapacidadVentana=0;
       this.iIndicePrimerTPDUDatosNormal=0;
       this.lNumeroSecInicial = -1;
       //throw new PTMFExcepcionNoVerificada ();
       throw new ParametroInvalidoExcepcion ("N�mero de sec " + lNSecInic
                                                + " no es v�lido.");
     }

 if (iTama�oMaximo>0)
   {
    this.vectorTPDUDatosNormal = new TPDUDatosNormal[iTama�oMaximo]; // Almacenar� los TPDUDatosNormal ordenados por n�mero de secuencia.
    if (this.vectorTPDUDatosNormal == null)
       throw new OutOfMemoryError ("No se pudo localizar memoria para crear la ventana.");
    this.iCapacidadVentana = iTama�oMaximo;
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
 * Dado un n�mero de secuencia devuelve el indice (posici�n) que ocupa
 * en el vector.
 * @param lNSec n�mero de secuencia a convertir en indice
 * @result indice correspondiente al n�mero de secuencia
 */
private int numSecAIndiceVector (long lNSec)
{

  // El nSec tiene que ser >= que el n�mero de secuencia inicial de la ventana.
  // El nSec tiene que ser <= que el n�mero de secuencia final de la ventana.
  if ( (lNSec<impGetNumSecInicial()) || (lNSec>impGetNumSecFinal ()))
   return -1;

  // Calcular la posici�n que le corresponde a nSec en el vector.
  return (int)(((lNSec-impGetNumSecInicial())+this.iIndicePrimerTPDUDatosNormal)
             %this.iCapacidadVentana);
}



//==========================================================================
/**
 * Devuelve el n�mero de secuencia inicial de la ventana.
 * @return n�mero de secuencia inicial de la ventana.
 */
public long impGetNumSecInicial ()
{
 return this.lNumeroSecInicial;
}

//==========================================================================
/**
 * Devuelve el n�mero de secuencia final de la ventana.
 * <p>Devuelve un n�mero negativo en caso de que el tama�o m�ximo de la ventana
 * sea cero (no existe n�mero de secuencia final).
 * @return n�mero de secuencia final de la ventana, -1 en si el tama�o
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
 * Devuelve el n�mero de secuencia m�s alto que verifica que todos los menores
 * o iguales a �l tienen un TPDUDatosNormal asociado.
 * @return el n�mero de secuencia mayor consecutivo, o -1 si no lo hay.
 */
public long impGetNumSecConsecutivo ()
{
 return this.lNumeroSecConsecutivo;
}


//==========================================================================
/**
 * Indica si la ventana contiene un TPDUDatosNormal con el n�mero de secuencia
 * indicado.
 * @param lNSec n�mero de secuencia del TPDUDatosNormal que se quiere comprobar.
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
 * A�ade un TPDUDatosNormal a la ventana. El n�mero de secuencia debe estar comprendido
 * entre el n�mero de secuencia inicial y final de la ventana.
 * <p>El n�mero de secuencia indicado ser� el que se asocie al TPDUDatosNormal por lo
 * que este deber� corresponder con el n�mero de secuencia verdadero del TPDUDatosNormal.
 * @param nSec n�mero de secuencia del TPDUDatosNormal que se quiere a�adir.
 * @param pqt TPDUDatosNormal a a�adir
 * @return el TPDUDatosNormal, si existe, que estaba asociado al n�mero de secuencia indicado,
 * o null si no existe.
 * @exception ParametroInvalidoExcepcion excepci�n lanzada cuando el n�mero de
 * secuencia no est� comprendido entre el n�mero de secuencia inicial y final
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
      // Comprobar cual es el m�s alto consecutivo
      {
       for (long lNSecIt = this.impGetNumSecInicial();
                                    lNSec <= this.impGetNumSecFinal();lNSecIt++)
         if (!this.impContieneTPDUDatosNormal (lNSecIt))
                break;
         else this.lNumeroSecConsecutivo = lNSecIt;
       }
    else { // Ver si es continuaci�n del ya registrado
           if ((lNSec - this.lNumeroSecConsecutivo)==1)
            for (long lNSecIt = lNSec; lNSec <= this.impGetNumSecFinal();lNSecIt++)
                  if (!this.impContieneTPDUDatosNormal (lNSecIt))
                     break;
                  else this.lNumeroSecConsecutivo = lNSecIt;
         }

    return pqtAntiguo;
  }
 else throw new ParametroInvalidoExcepcion ("El n�mero de secuencia "+lNSec+
                                 ", no puede ser a�adido a esta ventana.");

} // Fin de addTPDUDatosNormal


//==========================================================================
/**
 * Elimina todos los TPDUDatosNormal de la ventana y fija un nuevo n�mero inicial.
 * El tama�o m�ximo de la ventana no es alterado.
 * @param lNSec nuevo n�mero de secuencia inicial de la ventana.
 * @exception ParametroInvalidoExcepcion lanzada cuando el n�mero de secuencia
 * pasado en el argumento no es v�lido.
 */
public void impReiniciar (long lNSec) throws ParametroInvalidoExcepcion
{

 if (lNSec<0)
       throw new ParametroInvalidoExcepcion ("No es v�lido un n�mero de secuencia menor que cero.");

 this.iIndicePrimerTPDUDatosNormal = 0;
 this.lNumeroSecInicial = lNSec;
 this.lNumeroSecConsecutivo = -1;
 for (int i=0;i<this.iCapacidadVentana;i++)
        this.vectorTPDUDatosNormal[i] = null;
}

//==========================================================================
/**
 * Obtiene el TPDUDatosNormal que en esta ventana est� asociado al n�mero de secuencia
 * indicado (no lo elimina).
 * <p>Se retorna una referencia al TPDUDatosNormal pedido, por lo que cualquier
 * modificaci�n que se le haga se reflejar� en la ventana, y viceversa.
 * @param lNSec n�mero de secuencia del TPDUDatosNormal.
 * @return el TPDUDatosNormal contenido en esta ventana y asociado al n�mero de secuencia
 * indicado, o null si no hay un TPDUDatosNormal asociado o si el n�mero de secuencia
 * no est� comprendido entre el n�mero de secuencia inicial y final de la ventana.
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
 * Obtiene todos los TPDUDatosNormal con n�mero de secuencia menor o igual al
 * indicado (no los elimina). Si el nSec es mayor que el n�mero de sec
 * final de la ventana, obtiene todos los TPDUDatosNormal que haya en la ventana.
 * <p>El vector retornado contiene las referencias a los TPDUDatosNormal pedidos,
 * por lo que cualquier modificaci�n de los mismos se refleja en los
 * TPDUDatosNormal de la ventana, y viceversa.
 * @param lNSec n�mero de secuencia.
 * @return vector con los TPDUDatosNormal pedidos o vac�o si no hay ninguno
 */
public Vector impGetTPDUDatosNormalMenorIgual (long lNSec)
{
 Vector vector = new Vector();
 TPDUDatosNormal pqt;

 // Si el capacidadVentana es cero el nSec valdr� < 0
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
 * Incrementa en una unidad el n�mero de secuencia inicial y final, eliminando
 * el TPDUDatosNormal, si existe, asociado al n�mero de secuencia inicial.
 * @return el TPDUDatosNormal asociado al n�mero de secuencia inicial de la ventana,
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
 * Elimina todos los TPDUDatosNormal cuyo n�mero de secuencia asociado sea menor o igual
 * al indicado, si �ste es mayor que el n�mero de secuencia final de la ventana
 * se eliminan todos los TPDUDatosNormal.
 * <p>El n�mero de secuencia inicial pasa a ser el siguiente al indicado en el
 * argumento, salvo que �ste sea mayor que el n�mero de secuencia final de la
 * ventana, en cuyo caso pasa a ser el siguiente al n�mero de secuencia final
 * de la ventana. El n�mero de secuencia final se incrementa en la misma proporci�n
 * que el inicial.
 * @param lNSec n�mero de secuencia
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
 * Obtiene la capacidad de la ventana. �ste indica cuantos pares
 * [n�meros de secuencia,TPDUDatosNormal] caben en la ventana.
 * @return el tama�o m�ximo de la ventana.
 */
public int impGetCapacidadVentana ()
{
 return this.iCapacidadVentana;
}

//==========================================================================
/**
 * Redimensiona la ventana a la nueva capacidad adaptando el n�mero de
 * secuencia final. El n�mero de secuencia inicial no es alterado.
 * <p>Si la nueva capacidad es inferior a la anterior, se eliminan los TPDUDatosNormal
 * con n�mero de secuencia asociado superior hasta adaptar el tama�o. Si es
 * superior, no se elimina ning�n TPDUDatosNormal.
 * @param iNuevoTama�oMaximo la nueva capacidad de la ventana.
 * @return vector con TPDUDatosNormal eliminados o vac�o.
 */
public Vector impCambiarCapacidadVentana (int iNuevoTama�oMaximo)
{
 TPDUDatosNormal[] arrayPqts;
 TPDUDatosNormal pqt;
 Vector vectorEliminados = new Vector ();
 long nSec;

  if (iNuevoTama�oMaximo<0)
      return vectorEliminados;

  // No se tiene que tirar ning�n TPDUDatosNormal.
  if (iNuevoTama�oMaximo > this.iCapacidadVentana)
   {
    arrayPqts = new TPDUDatosNormal[iNuevoTama�oMaximo];
    // Copiar los TPDUDatosNormal al nuevo array.
    for (int i=0;i<this.iCapacidadVentana;i++)
        arrayPqts[i] = this.vectorTPDUDatosNormal[i];
    // Poner a null las posiciones a�n no ocupadas.
    for (int i=this.iCapacidadVentana;i<iNuevoTama�oMaximo;i++)
        arrayPqts[i] = null;
    this.iCapacidadVentana = iNuevoTama�oMaximo;
    this.vectorTPDUDatosNormal = arrayPqts;
   }


  // Hay que tirar los TPDUDatosNormal que no caben en el nuevo tama�o, se comienza
  // a eliminar TPDUDatosNormal por el final.
  // El nuevoTama�oMaximo tiene que ser un n�mero positivo.
  if (iNuevoTama�oMaximo < this.iCapacidadVentana)
   {
    arrayPqts = new TPDUDatosNormal[iNuevoTama�oMaximo];
    // Copiar los TPDUDatosNormal que cogan.
    nSec = impGetNumSecInicial();
    for (int i=0;i<iNuevoTama�oMaximo;i++)
       {
        arrayPqts[i] = this.vectorTPDUDatosNormal[numSecAIndiceVector(nSec)];
        nSec ++; // Incrementar el n�mero de secuencia.
        }
    // A�adir los TPDUDatosNormal eliminados al vector.
    for (int i=iNuevoTama�oMaximo;i<this.iCapacidadVentana;i++)
        {
        pqt = this.vectorTPDUDatosNormal[numSecAIndiceVector(nSec)];
        if (pqt!=null)
            vectorEliminados.addElement(pqt);
        nSec ++; // Incrementar el n�mero de secuencia.
        }

    this.iIndicePrimerTPDUDatosNormal = 0;
    this.iCapacidadVentana = iNuevoTama�oMaximo;
    // Actualizar el numeroSecConsecutivo
    if (this.lNumeroSecConsecutivo > this.impGetNumSecFinal())
        this.lNumeroSecConsecutivo = this.impGetNumSecFinal();
    this.vectorTPDUDatosNormal = arrayPqts;
   }

  return vectorEliminados;
} // Fin de cambiarTama�oVentana

//==========================================================================
/**
 * Obtiene el tama�o de la ventana. �ste indica cuantos pares
 * [n�meros de secuencia,TPDUDatosNormal] hay actualmente en la ventana.
 * @return el tama�o de la ventana.
 */
public long impGetTama�oVentana ()
{
   long lTama�o = 0;
   long lNum_final = this.impGetNumSecFinal();
   long lNum_inicio = this.impGetNumSecInicial();

   // Ojo, que el n�mero puede dar la vuelta, pasar su m�ximo permitido
   if(lNum_final >= lNum_inicio)
     lTama�o = lNum_final - lNum_inicio;
   else
   {
    // TRATAR CUANDO EL N�MERO HA DADO LA VUELTA
    Long limite = new Long("9223372036854775807"); //+=1  --> 2147483648: L�mite superior de un entero con signo.

    lTama�o = lNum_final + 1/* por el cero que no suma y si cuenta como n�mero de secuencia*/
                + (limite.longValue() - lNum_inicio +1/*se suma uno porque debe de ser 2147483648*/);
   }

   return lTama�o;
}

//==========================================================================
/**
 * De entre los n�meros de secuencia de la ventana con TPDUDatosNormal asociado, devuelve
 * el mayor.
 * @return n�mero de secuencia mayor con TPDUDatosNormal asociado, o -1 si ning�n
 * n�mero de secuencia de la ventana tiene TPDUDatosNormal asociado.
 */
public long impGetNumSecMayor ()
{
 long lNSec;

 lNSec = impGetNumSecFinal ();

 while ( (lNSec>=impGetNumSecInicial()) && (!impContieneTPDUDatosNormal(lNSec)) )
        lNSec --; // Decrementar el n�mero de secuencia.

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
 * Indica cuantos pares [n�meros de secuencia,TPDUDatosNormal] caben en la ventana.
 */
private int iCapacidadVentana = 0;

/**
 * Array que almacena los TPDUDatosNormal ordenados por el n�mero
 * de secuencia.
 * <p>En la posici�n indicada por "indicePrimerTPDUDatosNormal" se
 * almacena el TPDUDatosNormal asociado al n�mero de secuencia inicial,
 * en la posici�n siguiente el que tenga asociado el n�mero de secuencia
 * consecutivo al inicial, y as� sucesivamente. Es decir, cada posici�n
 * se corresponde con un n�mero de secuencia, correspondiendo al:
 *   <ul>
 *     <li>"n�mero de secuencia inicial de la ventana", la posici�n indicada
 *       por "indicePrimerTPDUDatosNormal"</li>
 *     <li>"n�mero de secuencia final de la ventana" , la posici�n resultante
 *       de incrementar circularmente "indicePrimerTPDUDatosNormal" en "capacidadVentana".</li>
 *   <ul>
 * <p><b>Esta relaci�n es din�mica, de forma que cuando se eliminan TPDUDatosNormal,
 * se desplaza el "indicePrimerTPDUDatosNormal" para que apunte a la posici�n
 * del nuevo n�mero de secuencia inicial, no teniendo que reasignar los
 * TPDUDatosNormal en el array.</b>
 */
private TPDUDatosNormal[] vectorTPDUDatosNormal = null;


/**
 * Posici�n en el array del TPDUDatosNormal con n�mero de secuencia menor.
 */
private int iIndicePrimerTPDUDatosNormal;

/**
 * N�mero de secuencia inicial de la ventana.
 */
private long lNumeroSecInicial;


/**
 * N�mero de secuencia mayor, consecutivo. Si su valor es -1, indica que
 * no hay ning�n n�mero de secuencia consecutivo, lo cual, s�lo es posible
 * si no hay un TPDUDatosNormal asociado al n�mero de secuencia inicial de la ventana.
 */
private long lNumeroSecConsecutivo;


}
