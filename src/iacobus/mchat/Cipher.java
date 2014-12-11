
 //============================================================================
//
//	Copyright (c) 1999 . All Rights Reserved.
//
//----------------------------------------------------------------------------
//
//	Fichero: Cipher.java  1.0 1/12/99
//
// 	Autores: 	M. Alejandro García Domínguez (AlejandroGarcia@wanadoo.es)
//						Antonio Berrocal Piris
//
//	Descripción: Clase Cipher
//
//
//----------------------------------------------------------------------------

package iacobus.mchat;

import iacobus.ptmf.*;

/** Clase Cipher. Crea los objetos de codificación y de decodificación. */
class Cipher{

  /** Objeto cifrador */
  javax.crypto.Cipher cipher = null;

  /** Objeto descifrador */
  javax.crypto.Cipher unCipher = null;

  /** MChat */
  MChat ftp = null;

  private  byte[] salt = { (byte)0xe7, (byte)0x43, (byte)0x71, (byte)0xec,
    (byte)0x7e, (byte)0xb8, (byte)0xff, (byte)0x35 };

  // Número de iteraciones
  private int count = 20;

  //==========================================================================
 /**
  * Constructor Cipher protegido.
  */
  protected Cipher(MChat ftp,char[] clave)
  {
    ftp = ftp;
    // Salt

     Log.log("ANTES DEL IF","");

     if(clave != null)
     {
       Log.log("CRIPTOGRAFIA--> CLave:"+clave,"");
       // Criptografía --> JCE 1.2
       // Utilizar PKCS#5 para crear clave secreta.
       try
       {
        //Añadir proveedor..
        iaik.security.provider.IAIK.addAsProvider(true);

        // Establecer parámetros PBE
        javax.crypto.spec.PBEParameterSpec pbeParamSpec = new javax.crypto.spec.PBEParameterSpec(salt, count);

        // Covertir password en un objeto SecretKey, usando una llave PBE.
        javax.crypto.spec.PBEKeySpec pbeKeySpec = new javax.crypto.spec.PBEKeySpec(clave);
        Log.log("DEPU 1","");

        javax.crypto.SecretKeyFactory secKeyFac = javax.crypto.SecretKeyFactory.getInstance("PBE","IAIK");//"PBEWithMD5AndDES");
        Log.log("DEPU 2","");

        javax.crypto.SecretKey pbeKey = secKeyFac.generateSecret(pbeKeySpec);
        Log.log("DEPU 3","");

        // Crear los objetos cipher
        cipher = javax.crypto.Cipher.getInstance("RC2/ECB/PKCS5Padding");//"PbeWithMD5AndDES_CBC");
        unCipher = javax.crypto.Cipher.getInstance("RC2/ECB/PKCS5Padding");
        Log.log("DEPU 4","");

        // Inicializar los cifradores con la llave y los parámetros...
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
        unCipher.init(javax.crypto.Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);
        Log.log("DEPU 5","");
       }
       catch( java.security.NoSuchAlgorithmException e)
       {
         ftp.error(e.toString());
         return;
       }
       catch(java.security.InvalidKeyException e)
       {
         ftp.error(e.toString());
         return;
       }
       catch(java.security.NoSuchProviderException e)
       {
         ftp.error(e.toString());
         return;
       }
       catch(javax.crypto.NoSuchPaddingException e)
       {
         ftp.error(e.toString());
         return;
       }
       catch(java.security.InvalidAlgorithmParameterException e)
       {
         ftp.error(e.toString());
         return;
       }
       catch(java.security.spec.InvalidKeySpecException e)
       {
         ftp.error(e.toString());
                  return;
       }

    }
  }

 //==========================================================================
 /**
  * Devuleve el objeto cifrador
  */
  javax.crypto.Cipher getCipher(){ return this.cipher;}

 //==========================================================================
 /**
  * Devuleve el objeto descifrador
  */
  javax.crypto.Cipher getUncipher(){ return this.unCipher;}


 //==========================================================================
 /**
  * Obtien un objeto Cipher.
  */
  static  Cipher getInstance(MChat ftp,char[] clave)
  {

     return new Cipher(ftp,clave);
  }
}