//============================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	Fichero: Temporizador.java  1.0
//
//
//	Descripción: Clase Temporizador. Proporciona métodos útiles para
//                   manejar temporizaciones. Avisos premeditados,....
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
//
//------------------------------------------------------------

package net.iacobus.ptmf;

import java.util.LinkedList;

/**
 * Clase que proporciona métodos para temporizar eventos a través de
 * callbacks.<br>
 * <br>
 * <p>Utiliza un thread para medir el tiempo.
 * <p>Se puede acceder concurrentemente de forma segura (es thread-safe).
 * <p>Es independiente de cualquier otra clase, sólo utiliza la interfaz
 * {@link TimerHandler} para realizar callbacks.
 * @version  1.0
 * @author Antonio Berrocal Piris
 * <A HREF="mailto:AntonioBP@wanadoo.es">(AntonioBP@wanadoo.es)</A><p>
 * M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 */
public class  Temporizador
{

 // ATRIBUTOS

 /**
  * Cola con la lista de vencimientos de los id_tpdu registrados para avisar
  * cuando se supere el tiempo máximo de espera ({@link PTMF#OPORTUNIDADES_RTT} * {@link PTMF#RTT})<br>
  * Cuando este tiempo es superado se ejecuta el callback registrado con el id_tpdu.<br>
  * Contiene instancias de {@link RegistroColaVencimiento}.
  * Los campos válidos de los registros que almacena son:
  * <ul>
  *    <li>{@link RegistroColaVencimiento#object}</li>
  *    <li>{@link RegistroColaVencimiento#id_tpdu}</li>
  *    <li>{@link RegistroColaVencimiento#lArg}</li>
  *    <li>{@link RegistroColaVencimiento#lTiempoFinal}</li>
  * </ul>
  */
  private LinkedList colaVencimientoRTT = null;


 /**
  * Cola con la lista de vencimientos de las funciones registradas para que
  * se ejecute el callback transcurrido un tiempo indicado.<br>
  * Contiene instancias de {@link RegistroColaVencimiento}.
  * Los campos válidos de los registros que almacena son:
  * <ul>
  *    <li>{@link RegistroColaVencimiento#object}</li>
  *    <li>{@link RegistroColaVencimiento#o}</li>
  *    <li>{@link RegistroColaVencimiento#lArg}</li>
  *    <li>{@link RegistroColaVencimiento#lTiempoFinal}</li>
  * </ul>
  */
  private LinkedList colaVencimientoFunciones = null;

 /**
  * Cola con la lista de vencimientos de las funciones periódicas registradas.<br>
  * Contiene instancias de {@link RegistroColaVencimiento}.
  * Los campos válidos de los registros que almacena son:
  * <ul>
  *    <li>{@link RegistroColaVencimiento#object}</li>
  *    <li>{@link RegistroColaVencimiento#o}</li>
  *    <li>{@link RegistroColaVencimiento#lArg}</li>
  *    <li>{@link RegistroColaVencimiento#lTiempoFinal}</li>
  *    <li>{@link RegistroColaVencimiento#lNPeriodos}</li>
  *    <li>{@link RegistroColaVencimiento#lTPeriodo}</li>
  * </ul>
  */
  private LinkedList colaVencimientoFuncionesPeriodicas = null;

 /**
  * Indica el número de RTT pendientes.
  */
  private int iContador=0;

  /**
   * Instante de tiempo que marca cuando el temporizador queda inactivo.
   * Si vale cero, indica que no tiene que llamar más a la función callbacks
   * de aviso de fin RTT.
   */
  private long lTiempoFinal=0;

  /**
   * Tiempo de finalización del siguiente RTT.
   * Cuando este tiempo es alcanzado se ejecuta la  función callback registrada
   * con {@link #registrarFuncionRTT(TimerHandler)} o {@link #registrarFuncionRTT(TimerHandler,long)}.
   */
  private  long lTiempoSiguienteRTT = 0;

  /**
   * Variable que indica si ha sido inicilizado el temporizador.
   */
  private  boolean bInicializado=false;

  /**
   * Callback que se llama cada RTT (tiempo de RTT).
   */
  private  TimerHandler funcionRTT = null;

  /**
   * Thread utilizado para medir los tiempos, y ejecutar los callback.
   */
  private  ThreadTemporizador threadTemporizador = null;

  /**
   * Primer argumento de la función callback registrada en
   * con {@link #registrarFuncionRTT(TimerHandler)} o {@link #registrarFuncionRTT(TimerHandler,long)}.
   */
  private  long lArgFuncionRTT;

  /**
   * Máximo número de RTT que pueden pasar por cada ID_TPDU registrado.
   */
  private  final int iINTENTOS = PTMF.OPORTUNIDADES_RTT + 1; // Número de intentos para dar por finalizado un NS.

  /**
   * Tiempo de RTT en milisegundos.
   */
  private  final long lRTT = PTMF.RTT; // Milisegundos.



  //NO OLVIDAR QUE LO QUE EJECUTE EL TEMPORIZADOR DEBERÁ SER MÍNIMO


 //==========================================================================
 /**
  * Constructor.
  * @throws ErrorIniciarTempExcepcion
  */
 public Temporizador () throws ErrorIniciarTempExcepcion
 {
  this.iniciar ();
 }

 //==========================================================================
 /**
  * Registra un objeto TimerHandler cuyo método será  ejecutado cada vez que
  * finalice el periodo indicado. Permite indicar el número de periodos.<br>
  * El tiempo indicado debe ser mayor que cero.
  * Si el número de periodos es menor o igual a cero, entonces se entiende que
  * son infinitos.
  * @param obj contiene el método callback, al que se pasará por argumento
  * 0 y null.
  * @param lTPeriodo milisegundos de duración del periodo, tiene que ser mayor de cero.
  * @paran lNPeriodos número de periodos durante los que tiene que ejecutar el
  * objeto (obj) registrado.
  * @see TimerHandler
  */
public  synchronized void registrarFuncionPeriodica (
                        TimerHandler obj,
                        long lTPeriodo,  //Expresado en mseg.
                        long lNPeriodos)
{
  registrarFuncionPeriodica (obj,lTPeriodo,lNPeriodos,0);
}


 //==========================================================================
 /**
  * Registra un objeto TimerHandler cuyo método será  ejecutado cada vez que
  * finalice el periodo indicado. Permite indicar el número de periodos.<br>
  * El tiempo indicado debe ser mayor que cero.
  * Si el número de periodos es menor o igual a cero, entonces se entiende que
  * son infinitos.
  * @param obj contiene el método callback, al que se pasará por argumento
  * lArg y null.
  * @param lTPeriodo milisegundos de duración del periodo, tiene que ser mayor de cero.
  * @paran lNPeriodos número de periodos durante los que tiene que ejecutar el
  * objeto (obj) registrado.
  * @param lArg primer argumento del callback.
  * @see TimerHandler
  */
public  synchronized void registrarFuncionPeriodica (
                        TimerHandler obj,
                        long lTPeriodo,  //Expresado en mseg.
                        long lNPeriodos,
                        long lArg)
{
 final String mn = "Temporizador.registrarFuncionPeriodica (...)";
  long lTA;

  if (obj == null || lTPeriodo <= 0)
    return;


 try{
  if (!bInicializado)
       iniciar();
  } catch (ErrorIniciarTempExcepcion e)
        {
         Log.log (mn,e.toString());
         System.exit(0);
         }

  RegistroColaVencimiento registro = new RegistroColaVencimiento ();

  registro.object = obj;
  registro.lArg = lArg;
  registro.o = null;
  registro.id_tpdu = null;
  registro.lTPeriodo = lTPeriodo;
  if (lNPeriodos<0)
     registro.lNPeriodos = 0;
  else registro.lNPeriodos = lNPeriodos;

  lTA = System.currentTimeMillis();
  registro.lTiempoFinal=lTA+lTPeriodo;

  // Registrar Callbacks
  if (obj!=null)
        colaVencimientoFuncionesPeriodicas.addLast(registro);
}

 //==========================================================================
 /**
  * Elimina de la cola de vencimientos periódicos (@link #colaVencimientoFuncionesPeriodicas})
  * el primer registro encontrado cuyo timerHandler coincida con el indicado.
  * @param timerHandler timerHandler con el que compara los registrados.
  */
 public  synchronized void cancelarFuncionPeriodica (TimerHandler timerHandler)
 {
  cancelarFuncionPeriodica (timerHandler,0);
 }

 //==========================================================================
 /**
  * Elimina de la cola de vencimientos periódicos (@link #colaVencimientoFuncionesPeriodicas})
  * el primer registro encontrado cuyo timerHandler y lArg coincida con los
  * indicados
  * @param timerHandler
  * @param lArg
  */
 public  synchronized void cancelarFuncionPeriodica (TimerHandler timerHandler,
                                                           long lArg)
 {
  final String mn = "Temporizador.cancelarArg1 (long)";
  RegistroColaVencimiento registro;


 try{
  if (!bInicializado)
       iniciar();
  } catch (ErrorIniciarTempExcepcion e)
        {
         Log.log (mn,e.toString());
         System.exit(0);
         }

 for (int i=0; i<colaVencimientoFuncionesPeriodicas.size(); i++)
 {
   registro = (RegistroColaVencimiento) colaVencimientoFuncionesPeriodicas.get(i);

   if ( (registro.object == timerHandler) && (registro.lArg == lArg))
    {
         colaVencimientoFuncionesPeriodicas.remove (i);
         return ; // Sólo elimina la 1ª ocurriencia.
    }
  } // Fin de for.

  comprobarColasVencimientos ();
 }



   //==========================================================================
  /**
   * Registra la función que será llamada en cada vencimiento del tiempo de RTT.
   * @param obj Objeto que contiene la funcion callback que será llamada
   * cada vencimiento de RTT. Si vale null no se ejecutará ninguna función cada
   * vez que venza el RTT. Los parámetros que se le pasarán son lArg y null.
   * @param lArg1 primer argumento de la función callback
   * @see TimerHandler
   */
 public  synchronized void registrarFuncionRTT (TimerHandler obj,long lArg)
 {
   funcionRTT = obj;
   lArgFuncionRTT = lArg;
 }

  //==========================================================================
  /**
   * Registra la función que será llamada en cada vencimiento del tiempo de RTT.
   * @param obj Objeto que contiene la funcion callback que será llamada
   * cada vencimiento de RTT. Si vale null no se ejecutará ninguna función cada
   * vez que venza el RTT. Los parámetros que se le pasarán son 0 y null.
   * @see TimerHandler
   */
 public  synchronized void registrarFuncionRTT (TimerHandler obj)
 {
   final String mn = "Temporizador.registrarFuncionRTT (TimerHandler)";

   registrarFuncionRTT (obj,0);
 }


//==========================================================================
/**
 * Indica si hay una función de aviso cada RTT registrada.
 * @return true en caso de que haya una función registrada y false en otro caso.
 */
public  synchronized boolean registradaFuncionRTT ()
{
   return (funcionRTT==null);
}


  //==========================================================================
  /**
   * Cancela los avisos (ejecución de callback) que se generán cada RTT (tiempo
   * de RTT). La cola de vencimientos de RTT ({@link #colaVencimientoRTT}) no se
   * es alterada, por lo que los callbacks registrados se irán ejecutando cuando
   * se alcace el tiempo final para los id_tpdu registrados.<br>
   * Para volver a activarlos se tiene que registrar la funcionRTT de nuevo.
   */
  public  synchronized void cancelarAvisoRTT ()
  {
   funcionRTT = null;
  }


 //===========================================================================
 /**
  * Indica por que parte de RTT vamos, es decir, indica un porcentaje (tantos
  * por ciento) sobre el RTT consumido hasta este momento.
  * @return 0 indica que no hay RTT registrados o que no ha transcurrido nada
  * del RTT actual. Si es mayor que cero, indica el tanto por ciento que ha
  * transcurrido del RTT actual.
  */
 public  synchronized int getPorcentajeRTTActual ()
 {

  if (iContador>0) // Se está esperando por RTT
    {
     long lDiferencia = lTiempoSiguienteRTT - System.currentTimeMillis ();
     if (lDiferencia <= 0)
        return 100; // Se ha consumido el 100 % del RTT actual.
     if (lRTT > 0)
        return ( 100 - (int)((lDiferencia*100)/lRTT) ) ;
    }
  return 0;
 }


 //==========================================================================
 /**
  * <b>Registra un objeto TimerHandler cuyo método será ejecutado en cada vencimiento
  * de RTT. </b>
  * @param obj contiene el método callback, al que se pasará por argumento
  * lArg y null.
  * @param lArg primer argumento del callback.
  */
 public  synchronized long registrarAvisoRTT (TimerHandler obj,long lArg)
 {
  return registrarAvisoRTT (obj,lArg,null);

 }

 //==========================================================================
 /**
  * Registra un nuevo id_tpdu para avisar cada RTT.
  * <p>Añade una nueva entrada en la cola de vencimientos de RTT ({@link #colaVencimientoRTT}).
  * Cuando se alcance la espera máxima ({@link PTMF#OPORTUNIDADES_RTT} * {@link PTMF#RTT})
  * para este id_tpdu se ejecuta el callback contenido en el objeto (obj).
  * @param obj contiene el método que se ejecutará cuando expire el <b>tiempo de espera
  * máximo</b> para el id_tpdu indicado, al que se pasará por argumento lArg y
  * id_tpdu
  * @param lArg primer argumento que se le pasa a la función callback.
  */
 public  synchronized long registrarAvisoRTT (TimerHandler obj,
                                                    long lArg,ID_TPDU id_tpdu)
 {
  final String mn = "Temporizador.registrarAvisoRTT (TimerHandler,arg,id_tpdu)";


  RegistroColaVencimiento registro = new RegistroColaVencimiento();
  long lTF,lTA;
  long lAux;


 try{
  if (!bInicializado)
       iniciar();
  } catch (ErrorIniciarTempExcepcion e)
        {
         Log.log (mn,e.toString());
         System.exit(0);
         }


  registro.object=obj;
  registro.lArg = lArg;
  registro.id_tpdu = id_tpdu;
  registro.o = id_tpdu;

  lAux=0;
  lTA = System.currentTimeMillis();
  lTF = lTA + iINTENTOS*lRTT;
  if ((iContador>0)&&(lTiempoFinal>0))
      {
      lAux=Math.round((lTF-lTiempoFinal)/(double)lRTT);
      iContador+=lAux;
      lTiempoFinal+=lAux*lRTT;
      }
    else {
           iContador = iINTENTOS;
           lTiempoFinal=lTF;
         }
  registro.lTiempoFinal=lTiempoFinal;
  if ((iContador>0)&&(lTiempoSiguienteRTT==0))
        lTiempoSiguienteRTT=lTA+lRTT;

  // Registrar Callbacks
  if (obj!=null)
        colaVencimientoRTT.addLast(registro);

  return lTA;
  }// Fin de registrarAvisoRTT ()


 //==========================================================================
 /**
  * Elimina de la cola de vencimientos de RTT ({@link #colaVencimientoRTT}) el
  * registro encontrado cuyo timerHandler y lArg coincida con el indicado.<br>
  * Sólo elimina la primera ocurrencia que encuentre.
  * @param timerHandler
  * @param lArg
  */
 public  synchronized void cancelarRTT (TimerHandler timerHandler,long lArg)
 {
  final String mn = "Temporizador.cancelarRTT (timerHandler,lArg)";
  RegistroColaVencimiento registro;

  try{
   if (!bInicializado)
       iniciar();
   } catch (ErrorIniciarTempExcepcion e)
        {
         Log.log (mn,e.toString());
         System.exit(0);
         }

  /*
    La colaVencimiento está ordenada crecientemente por el número de secuencia.
  */
  for (int i=0; i<colaVencimientoRTT.size(); i++)
  {
   registro = (RegistroColaVencimiento) colaVencimientoRTT.get(i);

   if ( (registro.object == timerHandler) && (registro.lArg == lArg))
    {
         colaVencimientoRTT.remove (i);
         return ; // Sólo elimina la 1ª ocurriencia.
    }
   } // Fin de for.

  comprobarColasVencimientos ();
 }



 //==========================================================================
 /**
  * Elimina de la cola de vencimientos de RTT ({@link #colaVencimientoRTT})
  * los registros encontrados cuyo timerHandler sea el indicado, y cuyo segundo
  * argumento sea un id_tpdu igual al pasado por argumento.<br>
  * Sólo elimina la primera ocurrencia.
  * @param timerHandler timerHandler con el que compara los registrados en
  * ({@link #colaVencimientoRTT}).
  * @param id_TPDU id_tpdu con el que compara los registrados en ({@link #colaVencimientoRTT}).
  */
 public synchronized void cancelarRTTID_TPDU (TimerHandler timerHandler,
                                                  ID_TPDU id_TPDU)
 {
  final String mn = "Temporizador.cancelarRTTID_TPDU (timerHandler,id_TPDU)";
  RegistroColaVencimiento registro;


 try{
  if (!bInicializado)
       iniciar();
  } catch (ErrorIniciarTempExcepcion e)
        {
         Log.log (mn,e.toString());
         System.exit(0);
         }


 for (int i=0; i<colaVencimientoRTT.size(); i++)
 {
   registro = (RegistroColaVencimiento) colaVencimientoRTT.get(i);

   if ( (registro==null) || (registro.id_tpdu==null) )
      continue;

   if ((registro.object==timerHandler)&&(registro.id_tpdu.equals (id_TPDU)))
    {
         colaVencimientoRTT.remove (i);
         return ; // Sólo elimina la 1ª ocurriencia.
    }
  } // Fin de for.


  comprobarColasVencimientos ();

} // Fin del cancelarID_TPDU(id_tpdu)



 //==========================================================================
 /**
  * Elimina de la cola de vencimientos de RTT ({@link #colaVencimientoRTT})
  * los registros encontrados cuyo timerHandler sea el indicado, y cuyo segundo
  * argumento sea un id_tpdu con id_socket igual y número de secuencia menor o
  * igual al id_TPDU indicado en el argumento.
  * @param timerHandler timerHandler con el que compara los registrados en
  * ({@link #colaVencimientoRTT}).
  * @param id_TPDU id_tpdu con el que compara los registrados en ({@link #colaVencimientoRTT}).
  */
 public  synchronized void cancelarRTTID_TPDUMenorIgual (
                                                      TimerHandler timerHandler,
                                                      ID_TPDU id_TPDU)
 {
  final String mn = "Temporizador.cancelarRTTID_TPDUMenorIgual (timerHandler,id_tpdu)";
  RegistroColaVencimiento registro;

 try{
  if (!bInicializado)
       iniciar();
  } catch (ErrorIniciarTempExcepcion e)
        {
         Log.log (mn,e.toString());
         System.exit(0);
         }


  if (id_TPDU==null)
     return;

  for (int i=(colaVencimientoRTT.size()-1); i>=0; i--)
  {
   registro = (RegistroColaVencimiento) colaVencimientoRTT.get(i);


   if ((registro==null)||(registro.id_tpdu == null))
      continue;

   if (registro.object!=timerHandler)
      continue;

   if (!registro.id_tpdu.getID_Socket().equals (id_TPDU.getID_Socket()))
      continue;


   if (registro.id_tpdu.getNumeroSecuencia().compareTo(
                                           id_TPDU.getNumeroSecuencia())<=0)
      {
         colaVencimientoRTT.remove (i);
       }

  } // Fin de for.
  comprobarColasVencimientos ();
 }


 //==========================================================================
 /**
  * Registra un objeto TimerHandler cuyo método será  ejecutado cuando se alcance
  * el tiempo indicado.<br>
  * El tiempo indicado debe ser mayor que cero.
  * @param obj contiene el método callback, al que se pasarán por argumento
  * 0 y null.
  * @param lMseg milisegundos.
  * @see TimerHandler
  */
 public  synchronized void registrarFuncion (TimerHandler obj,long lMseg)
 {
  final String mn = "Temporizador.registrarFuncion (TimerHandler,mseg)";

  registrarFuncion (lMseg,obj,0,null);

  }// Fin de registrarFuncion ()

 //==========================================================================
 /**
  * Registra un objeto TimerHandler cuyo método será  ejecutado cuando se alcance
  * el tiempo indicado.<br>
  * El tiempo indicado debe ser mayor que cero.
  * @param lMseg milisegundos.
  * @param obj contiene el método callback, al que se pasará por argumento
  * lArg y null.
  * @param lArg primer argumento del callback
  * @see TimerHandler
  */
 public  synchronized void registrarFuncion (long lMseg,TimerHandler obj,
                                                   long lArg)
 {
  registrarFuncion (lMseg,obj,lArg,null);
 }// Fin de registrarFuncion ()

 //==========================================================================
 /**
  * Registra un objeto TimerHandler cuyo método será  ejecutado cuando se alcance
  * el tiempo indicado.<br>
  * El tiempo indicado debe ser mayor que cero.
  * @param lMseg milisegundos.
  * @param obj contiene el método callback, al que se pasará por argumento
  * lArg y null.
  * @param lArg primer argumento del callback
  * @param o segundo argumento del callback
  * @see TimerHandler
  */
 public  synchronized void registrarFuncion (long lMseg,TimerHandler obj,
                                                   long lArg,
                                                   Object o)
 {
  final String mn = "Temporizador.registrarFuncion (lMseg,timerHandler,lArg,obj)";
  RegistroColaVencimiento registro = new RegistroColaVencimiento();
  long lTA;

  if (lMseg<=0)
        return;

 try{
  if (!bInicializado)
       iniciar();
  } catch (ErrorIniciarTempExcepcion e)
        {
         Log.log (mn,e.toString());
         System.exit(0);
         }

  registro.object  = obj;
  registro.lArg    = lArg;
  registro.o       = o;
  registro.id_tpdu = null;

  lTA = System.currentTimeMillis();
  registro.lTiempoFinal=lTA+lMseg;

  // Registrar Callbacks
  if (obj!=null)
        colaVencimientoFunciones.addLast(registro);


  }// Fin de registrarFuncion ()


 //==========================================================================
 /**
  * Elimina de la cola de vencimientos periódicos (@link #colaVencimientoFunciones})
  * el primer registro encontrado cuyo timerHandler y lArg coincida con los
  * indicados
  * @param timerHandler
  * @param lArg
  */
 public  synchronized void cancelarFuncion (TimerHandler timerHandler,long lArg)
 {
  final String mn = "Temporizador.cancelarFuncion (timerHandler,lArg)";
  RegistroColaVencimiento registro;

  try{
   if (!bInicializado)
       iniciar();
   } catch (ErrorIniciarTempExcepcion e)
        {
         Log.log (mn,e.toString());
         System.exit(0);
         }
  for (int i=0; i<colaVencimientoFunciones.size(); i++)
  {
   registro = (RegistroColaVencimiento) colaVencimientoFunciones.get(i);

   if ( (registro.object == timerHandler) && (registro.lArg == lArg))
    {
         colaVencimientoFunciones.remove (i);
         return ; // Sólo elimina la 1ª ocurriencia.
    }
  } // Fin de for.

  comprobarColasVencimientos ();
 }



//==========================================================================
/**
 * Función que sólo se ejecuta una vez. Inicia el hilo encargado de medir
 * el tiempo y crea las colas de vencimientos.
 * @throws ErrorIniciarTempExcepcion Excepción lanzada si hay un error al
 * iniciar el temporizador.
 */
private  void iniciar () throws ErrorIniciarTempExcepcion
 {
  final String mn = "Temporizador.iniciar";

  if (colaVencimientoRTT == null)
     colaVencimientoRTT = new LinkedList ();
  else throw new ErrorIniciarTempExcepcion ();

  if (colaVencimientoFunciones == null)
     colaVencimientoFunciones = new LinkedList ();
  else throw new ErrorIniciarTempExcepcion ();

  if (colaVencimientoFuncionesPeriodicas == null)
     colaVencimientoFuncionesPeriodicas = new LinkedList ();
  else throw new ErrorIniciarTempExcepcion ();

  if (threadTemporizador==null)
    {
     threadTemporizador = new ThreadTemporizador (this);
     if (threadTemporizador!=null)
        threadTemporizador.start();
     else throw new ErrorIniciarTempExcepcion ();
     }
  bInicializado = true;
 }

  //==========================================================================
  /**
   * Duerme al thread que lo ejecute durante los milisegundos indicados.
   * @param mseg milisegundos de espera.
   */
   public static void sleep(long lMseg)
   {
    if (lMseg<0)
       return;

    try
     {
      Thread.sleep(lMseg);
     }
      catch (InterruptedException e)
      { }
    }

  //==========================================================================
  /**
   * Devuelve el tiempo actual, expresado  en milisegundos.
   */
   public static long tiempoActualEnMseg ()
   {
    return System.currentTimeMillis ();
   }

  //==========================================================================
  /**
   * Cede el procesador a cualquier otra tarea que este esperando.
   */
   public static void yield()
   {
      Thread.yield();
   }


 //==========================================================================
 /**
  * Cancela todas las operaciones del temporizador hasta que no se vuelvan a
  * registrar más callbacks.
  */
 public synchronized void cancelarTodoTemporizador ()
 {
    if (colaVencimientoRTT!=null)
        colaVencimientoRTT.clear();
    if (colaVencimientoFunciones!=null)
        colaVencimientoFunciones.clear();
    if (colaVencimientoFuncionesPeriodicas!=null)
        colaVencimientoFuncionesPeriodicas.clear();

    iContador = 0;
    lTiempoFinal = 0;
    lTiempoSiguienteRTT = 0;
 }



 //==========================================================================
 /**
  * Comprueba si las colas han quedado vacías y actualiza variables.
  */
  private  synchronized void comprobarColasVencimientos ()
  {
   if (colaVencimientoRTT.size()==0 && colaVencimientoFunciones.size()==0
        && colaVencimientoFuncionesPeriodicas.size()==0)
          cancelarTodoTemporizador ();
   else if (colaVencimientoRTT.size ()==0)
             { // Cancelar el contador, es decir, no se tienen que generar más RTT
              iContador = 0;
              lTiempoFinal = 0;
              lTiempoSiguienteRTT = 0;
             }
  }


 //==========================================================================
 /**
  * Reinicia el temporizador.
  */
 public  synchronized void reiniciar ()
 {
   final String mn = "Temporizador.reiniciar";

   try{
    if (!bInicializado)
        iniciar();
    } catch (ErrorIniciarTempExcepcion e)
        {
         Log.log (mn,e.toString());
         System.exit(0);
         }

   cancelarTodoTemporizador ();
 }

 //==========================================================================
 /**
  * Busca el siguiente objeto callback cuyo tiempo final es igual o menor al
  * tiempo indicado. Esta función controla si se ha alcanzado el tiempo de
  * espera del siguiente RTT o ha finalizado el tiempo para algún número de
  * secuencia registrado en la colas de vencimientos.
  * @param lTiempo tiempo en milisegundos utilizado para comparar con los
  * registrados.
  */
  synchronized boolean buscarSiguiente (long lTiempo)
 {
  RegistroColaVencimiento  elemento=null;


 // Comprueba si ha vencido el tiempo máximo de espera de algún número de
 // secuencia registrado en la cola de vencimientos.
 for (int i=0;i<colaVencimientoRTT.size();i++)
 {
   elemento = (RegistroColaVencimiento) colaVencimientoRTT.get(i);
   if (lTiempo>elemento.lTiempoFinal)
        {
         threadTemporizador.objeto = elemento.object;
         threadTemporizador.lCallbackArg = elemento.lArg;
         threadTemporizador.callbackObject = elemento.id_tpdu;
         colaVencimientoRTT.remove (i);
         if (colaVencimientoRTT.size()==0)
          {
           if (colaVencimientoFunciones.size()==0)
             cancelarTodoTemporizador ();
           else { // Cancelar el contador, es decir, no se tienen que generar más RTT
                 iContador = 0;
                 lTiempoFinal = 0;
                 lTiempoSiguienteRTT = 0;
                }
          }
         return (true);
        }
   } // Fin de for.

  // Comprueba si ha vencido RTT.
  if ((iContador>0)&&(lTiempo>= lTiempoSiguienteRTT))
    {
     threadTemporizador.objeto = funcionRTT;
     threadTemporizador.lCallbackArg = lArgFuncionRTT;
     threadTemporizador.callbackObject = null;
     iContador--;
     if (iContador>0)
       lTiempoSiguienteRTT+=lRTT;
     else {
           lTiempoSiguienteRTT=0;
           lTiempoFinal=0;
           }
     return (true);
    }

 // Comprueba si ha vencido el tiempo máximo de espera de algún número de
 // secuencia registrado en la cola de vencimientos.
 for (int i=0;i<colaVencimientoFunciones.size();i++)
 {
   elemento = (RegistroColaVencimiento) colaVencimientoFunciones.get(i);
   if (lTiempo>elemento.lTiempoFinal)
        {
         threadTemporizador.objeto = elemento.object;
         threadTemporizador.lCallbackArg = elemento.lArg;
         threadTemporizador.callbackObject = elemento.o;
         colaVencimientoFunciones.remove (i);
         return (true);
        }
   } // Fin de for.*/

 // Comprueba las funciones periódicas registradas.
 for (int i=0;i<colaVencimientoFuncionesPeriodicas.size();i++)
 {
   elemento = (RegistroColaVencimiento) colaVencimientoFuncionesPeriodicas.get(i);
   if (lTiempo>elemento.lTiempoFinal)
        {
         threadTemporizador.objeto = elemento.object;
         threadTemporizador.lCallbackArg = elemento.lArg;
         threadTemporizador.callbackObject = elemento.o;
         if (elemento.lNPeriodos==1)
           colaVencimientoFuncionesPeriodicas.remove (i); // Eliminar
         else // Actualizar el nPeriodos (Es 0 o mayor que 1)
              {
               // ¿ O LE SUMO tiempo actual (variable tiempo)??
               elemento.lTiempoFinal += elemento.lTPeriodo; // Incrementar en periodo
               if (elemento.lNPeriodos > 0) //
                  elemento.lNPeriodos --;
              }
         return (true);
        }
   } // Fin de for.*/


  return (false);
 }

  //==========================================================================
  /**
   * Devuelve una cadena informativa.
   */
  public String toString ()
  {
   return "";//colaVencimientoRTT.toString () + colaVencimientoFunciones.toString();
  }


}// Fin clase Temporizador.


//==================================================================
//           CLASE     RegistroColaVencimiento
//==================================================================


/**
 * Almacena la información sobre cuando tiene que ejecutarse un callback
 * y con que argumentos.<br>
 * Clase que contiene un elemento de la cola de vencimientos.
 * @see Temporizador#colaVencimientoFunciones
 * @see Temporizador#colaVencimientoFuncionesPeriodicas
 * @see Temporizador#colaVencimientoRTT
 * @version 1.0
 * @author M. Alejandro García Domínguez.
 *  Antonio Berrocal Piris.
 */
class RegistroColaVencimiento
{
  // ATRIBUTOS
  /**
   * Objeto conteniendo la función callback, que será ejecutada cuando sea
   * alcanzado el tiempoFinal.
   */
  public TimerHandler object = null;

  /**
   * Instante de tiempo a partir del cual este número de secuencia es retirado de
   * la cola de vencimientos porque se le ha agotado el tiempo de espera.
   */
  public long lTiempoFinal;

  /** Primer argumento de la función callback. */
  public long lArg;

  /** Segundo argumento de la función callback*/
  public Object o;

  /** Segundo argumento de la función callback, cuando lo que se registro fue
    * un id_tpdu para avisar al finalizar las oportunidades ({@link Temporizador#iINTENTOS})
    **/
  public ID_TPDU id_tpdu;

  /** Tiempo Periodo */
  public long lTPeriodo = 0;

  /** Múmero de periodos */
  public long lNPeriodos = 1;


  //==========================================================================
  /**
   * Devuelve una cadena informativa.
   */
  public String toString ()
  {
   return "TimerHandler: " + this.object +
          " arg1: " + this.lArg +
          " o: " + this.o +
          " ID_TPDU: " + this.id_tpdu
          ;
   }

} // Fin de la clase RegistroColaVencimiento.


//==================================================================
//           CLASE     ThreadTemporizador
//==================================================================


/**
 * Clase que implementa un thread encargado de medir el tiempo.
 * @see Temporizador
 * @version 1.0
 * @author M. Alejandro García Domínguez.
 * Antonio Berrocal Piris.
 */
class ThreadTemporizador extends Thread
{
  /**
   * Objeto temporizador asociado al thread.
   */
  private Temporizador temporizador = null;

  //==========================================================================
  /**
   * Crea el thread.
   * @param temporizadorParam temporizador que utiliza el thread.
   */
 public ThreadTemporizador (Temporizador temporizadorParam)
 {
   super();

   this.temporizador = temporizadorParam;

   setDaemon(true);
   }

  //==========================================================================
  /**
   * Método run que ejecuta un bucle infinito que mide el tiempo. LLama a la
   * función {@link Temporizador#buscarSiguiente(long)} para averiguar los
   * si tiene que ejecutar algún callback, y si es así, lo ejecuta.
   */
 public void run ()
 {
  long lTiempo;
  boolean bReintentar;
  final String mn = "ThreadTemporizador.run";


   while (true)
    {
     try
     {
      sleep(50); // Espera 50 milisegundos.
      }
      catch (InterruptedException e)
      {}

     lTiempo = System.currentTimeMillis();  // Obtiene el tiempo actual.

     bReintentar = true;

     while (bReintentar)
     {
      bReintentar = temporizador.buscarSiguiente(lTiempo);
      if (bReintentar&&(objeto!=null))
        {
         objeto.TimerCallback (lCallbackArg,callbackObject); // Ejecuta la función callback
        }
     }
    }
 }

// ATRIBUTOS

 /**
  * Objeto que contiene la función callback que se tiene que ejecutar.
  */
 public TimerHandler objeto = null;

 /**
  * Primer argumento de la función callback.
  */
 public long lCallbackArg = 0;

 /**
  * Primer argumento de la función callback.
  */
 public Object callbackObject;

} // Fin de clase ThreadTemporizador




