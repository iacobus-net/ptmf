//============================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//------------------------------------------------------------
//
//	Fichero: PTMF.java  1.0 9/9/99
//
//
//	Descripción: Clase PTMF.
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

package ptmf;

import java.io.*;
import java.util.*;

/**
 * <STRONG><B>Clase PTMF. "Protocolo de Transporte Multicast Fiable"</STRONG>
 * Constantes generales para todas las clases.</B>
 * @author M. Alejandro García Domínguez
 * <A HREF="mailto:alejandro.garcia.dominguez@gmail.com">(alejandro.garcia.dominguez@gmail.com)</A><p>
 *			   Antonio Berrocal Piris
 */

public class PTMF
{

  //==========================================================================
  //
  //  VARIABLES ESTÁTICAS GLOBALES.
  //

  /** Modo FIABLE del protocolo PTMF  */
  public static final int PTMF_FIABLE      = 0x1000;
  /** Modo NO_FIABLE del protocolo PTMF  */
  public static final int PTMF_NO_FIABLE   = 0x1001;
  /** Modo MONITOR del protocolo PTMF  */
  public static final int PTMF_MONITOR     = 0x1002;
  /** Modo FIABLE_RETRASADO del protocolo PTMF  */
  public static final int PTMF_FIABLE_RETRASADO = 0x1004;
  /** Modo NO_FIABLE_ORDENADO del protocolo PTMF  */
  public static final int PTMF_NO_FIABLE_ORDENADO = 0x1008;

  /** Cierre estable de la conexion Multicast  */
  public static final boolean CLOSE_ESTABLE   = true;
  /** Cierre Immediato de la conexion Multicast  */
  public static final boolean CLOSE_INMEDIATO = false;


  /** Número de versión de PTMF. */
  public static final int VERSION = 1;


  /**
   * Tamaño máximo (en bytes) que puede tener un TPDU. 63Kb, reservamos 1Kb para
   * las Cabeceras del Nivel de Transporte y del Nivel de Red (IP); el tamaño
   * máxio un datagrama IP es 64Kb, incluye datos y cabecera IP.
   */
  public static final int TPDU_MAX_SIZE = 1024 *63;
 /**
   * Número máximo de bytes que pueden viajar en un TPDU de datos. Hay que reservar
   * suficientes bytes para enviar identificadores de idgl o id_socket que no han
   * enviado asentimiento, para el caso en el que necesite ser rtx. Al menos deberá
   * caber un identificador idgl o id_socket (el más grande de los dos).
   * Reservamos para 20 identificadores: 20 * 6 = 120 bytes
   */
  public static final int TPDU_MAX_SIZE_PAYLOAD = TPDU_MAX_SIZE
                                                  - TPDUDatosNormal.LONGHEADER;

  /**
   * MTU (Maximum Transfer Unit). EL MTU NO PUEDE SER SUPERIOR AL TPDU_MAX_SIZE.
   */
   public static final int MTU = 1024 * 1; //2


  /**
   * TAMAÑO VENTANA DE EMISIÓN. Nº de TPDUs que caben en la ventana.
   */
  public static final int TAMAÑO_VENTANA_EMISION   = 24;//24;

  /**
   * TAMAÑO VENTANA DE RECEPCIÓN. Nº de TPDUs que caben en la ventana.
   */
  public static final int TAMAÑO_VENTANA_RECEPCION = 24;//24;


  /** Tamaño por defecto de la Cola de Emisión en bytes*/
  public static final int TAMAÑO_DEFECTO_COLA_EMISION   = 1024 * 60; //64kB

  /** Tamaño por defecto de la Cola de Recepción en bytes*/
  public static final int TAMAÑO_DEFECTO_COLA_RECEPCION = 1024 * 300; //300kB

 /**
  * Tamaño del buffer de emision del socket en bytes.
  * En algunos S.O. como W'95 existe una limitacion de 64Kb
  */
  public static final int SIZE_BUFFER_SOCKET_EMISION = 1024 * 64;//1024 * 100;

 /**
  * Tamaño del buffer de recepcion del socket en bytes.
  * En algunos S.O. como W'95 existe una limitacion de 64Kb
  */
  public static final int SIZE_BUFFER_SOCKET_RECEPCION = 1024 * 64;//1024 * 300;

/**
 * Numero de TPDUs que DatosThread puede procesar consecutivamente.
 */
  public static final int MAX_TPDU_PROCESAR_CONSECUTIVAMENTE = (PTMF.TAMAÑO_VENTANA_EMISION/2) +1;

  /** TPDU Tipo CGL */
  public static final int TPDU_CGL      = 0;
  /** TPDU Tipo DATOS */
  public static final int TPDU_DATOS    = 1;

  /** Subtipos TPDU DATOS NORMAL*/
  public static final byte SUBTIPO_TPDU_DATOS_NORMAL     = 0x00; // 000
  /** Subtipos TPDU DATOS RTX*/
  public static final byte SUBTIPO_TPDU_DATOS_RTX        = 0x01; // 001
  /** Subtipos TPDU DATOS MACK*/
  public static final byte SUBTIPO_TPDU_DATOS_MACK       = 0x02; // 010
  /** Subtipos TPDU DATOS ACK*/
  public static final byte SUBTIPO_TPDU_DATOS_ACK        = 0x03; // 011
  /** Subtipos TPDU DATOS HACK*/
  public static final byte SUBTIPO_TPDU_DATOS_HACK       = 0x04; // 101
  /** Subtipos TPDU DATOS HSACK*/
  public static final byte SUBTIPO_TPDU_DATOS_HSACK      = 0x05; // 101
  /** Subtipos TPDU DATOS NACK*/
  public static final byte SUBTIPO_TPDU_DATOS_NACK       = 0x06; // 110
  /** Subtipos TPDU DATOS HNACK*/
  public static final byte SUBTIPO_TPDU_DATOS_HNACK      = 0x07; // 111

  /** Ambito de TTL de host */
  public static final int AMBITO_HOST          = 0;
  /** Ambito de TTL de Subred */
  public static final int AMBITO_SUBRED        = 1;
  /** Ambito de TTL de Local */
  public static final int AMBITO_LOCAL         = 8;
  /** Ambito de TTL de Regional */
  public static final int AMBITO_REGIONAL      = 32;
  /** Ambito de TTL de Europea */
  public static final int AMBITO_EUROPEA       = 64;
  /** Ambito de TTL de Internacional */
  public static final int AMBITO_INTERNACIONAL = 128;
  /** Ambito de TTL de Inrestringido */
  public static final int AMBITO_INRESTRINGIDO = 255;





  /**Subtipo TPDU CGL BUSCAR_GRUPO_LOCAL */
  public static final byte TPDU_CGL_BUSCAR_GRUPO_LOCAL        = 1;
  /**Subtipo TPDU CGL GRUPO_LOCAL */
  public static final byte TPDU_CGL_GRUPO_LOCAL               = 2;
  /**Subtipo TPDU CGL UNIRSE_A_GRUPO_LOCAL */
  public static final byte TPDU_CGL_UNIRSE_A_GRUPO_LOCAL      = 3;
  /**Subtipo TPDU CGL SCOKET_ACEPTADO_EN_GRUPO_LOCAL */
  public static final byte TPDU_CGL_SOCKET_ACEPTADO_EN_GRUPO_LOCAL = 4;
  /**Subtipo TPDU CGL DEJAR_GRUPO_LOCAL */
  public static final byte TPDU_CGL_DEJAR_GRUPO_LOCAL         = 5;
  /**Subtipo TPDU CGL ELIMINACION_GRUPO_LOCAL */
  public static final byte TPDU_CGL_ELIMINACION_GRUPO_LOCAL   = 6;
  /**Subtipo TPDU CGL BUSCAR_GRUPO_LOCAL_VECINO */
  public static final byte TPDU_CGL_BUSCAR_GRUPO_LOCAL_VECINO = 7;
  /**Subtipo TPDU CGL GRUPO_LOCAL_VECINO */
  public static final byte TPDU_CGL_GRUPO_LOCAL_VECINO        = 8;
  /**Subtipo TPDU CGL BUSCAR_GL_PARA_EMISOR */
  public static final byte TPDU_CGL_BUSCAR_GL_PARA_EMISOR     = 9;
  /**Subtipo TPDU CGL GL_PARA_EMISOR */
  public static final byte TPDU_CGL_GL_PARA_EMISOR            = 10;

  /** Estado de la máquina CGL: BUSCAR_GL  */
  public static final int ESTADO_CGL_BUSCAR_GL               = 0x100;
  /** Estado de la máquina CGL: ESPERAR_ACEPTACION_GL  */
  public static final int ESTADO_CGL_ESPERAR_ACEPTACION_GL   = 0x103;
  /** Estado de la máquina CGL: MIEMBRO_GL  */
  public static final int ESTADO_CGL_MIEMBRO_GL              = 0x104;
  /** Estado de la máquina CGL: CREAR_GL  */
  public static final int ESTADO_CGL_CREAR_GL                = 0x105;
  /** Estado de la máquina CGL: BUSCAR_GL_VECINOS  */
  public static final int ESTADO_CGL_BUSCAR_GL_VECINOS       = 0x106;
  /** Estado de la máquina CGL: ESTADO_CGL_DEJAR_GL  */
  public static final int ESTADO_CGL_DEJAR_GL                = 0x107;
  /** Estado de la máquina CGL: ESTADO_CGL_MONITOR  */
  public static final int ESTADO_CGL_MONITOR                 = 0x110;
  /** Estado de la máquina CGL: ESTADO_CGL_NULO  */
  public static final int ESTADO_CGL_NULO                    = 0x111;

  /** Número máximo de sockets en el Grupo Local (GL) */
  public static final int MAX_SOCKETS_GL     =   24;


  /** RTT  */
  public static final int RTT = 3000; //10000;  // 1 Segundos

  /** OPORTUNIDADES_RTT  */
  public static final int OPORTUNIDADES_RTT = 3; // Número de intentos

  /** Tiempo entre envios de cada TPDU  */
  public static final int T_MIN_ENTRE_ENVIOS = 0;

  /** Tiempo base que se usa para calcular los tiempos aleatorios.*/
  public static final int T_BASE = 200; // 0.2 Segundos

  /** Máximo tiempo aleatorio para enviar un MACK. Tiene que ser mayor que T_BASE*/
  public static final int MAX_TRANDOM_MACK = 800; // 0.8 Segundos

  /** Máximo tiempo aleatoriO para enviar un ASENT_NEG. Tiene que ser mayor que T_BASE*/
  public static final int MAX_TRANDOM_ASENT_NEG = 2000; // 2 Segundos



  /** Número de TPDU INICIALES por ráfaga */
  public static final int TPDUS_POR_RAFAGA  = 1000; //100000 ;//PTMF.TAMAÑO_VENTANA_EMISION;

  /** Petición de ACK INICIALES cada X TPDU. */
  public static final int ACKS_CADA_X_TPDUS = (PTMF.TAMAÑO_VENTANA_EMISION * 4)/5;

  /**
   * Tiempo máximo desde que se inserta un TPDU sin el bit ACK activado en
   * la ventana de emisión y uno con el bit ACK activado.
   */
  public static final long T_MAX_TPDU_SIN_ACK = PTMF.RTT *1;//  / 2;

 /**
  * Ratio de emision de datos de usuario por defecto. En bytes por segundo.
  */
  public static final long RATIO_USUARIO_BYTES_X_SEG  = 1024*1024*100;

 /**
  * Tiempo máximo que puede emplear un socket o idgl nuevo en sincronizarse
  * con los emisores.
  */
  public static final long TIEMPO_MAXIMO_SINCRONIZACION = RTT*OPORTUNIDADES_RTT + T_MAX_TPDU_SIN_ACK;


  /**
   * Tiempo en mseg que mide los intervalos para la comprobación de la
   * inactividad de emisores.
   */
  public static final long TIEMPO_COMPROBACION_INACTIVIDAD_EMISORES = 60000; // 60 seg.


  /**
   * Tiempo de inactividad máximo durante el cual un emisor puede estar inactivo.
   */
  public static final long TIEMPO_MAX_INACTIVIDAD_EMISOR = 60000 * 10; // 10 minutos




}
