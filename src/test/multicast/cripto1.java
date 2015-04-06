//============================================================
//
//	Copyright (c) 1999,2014 . All Rights Reserved.
//
//------------------------------------------------------------
//
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

package test.multicast;

import java.io.*;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.BadPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.SecretKey;

import ptmf.Log;
import iaik.security.cipher.PBEKeyBMP;

import java.security.Security;
import java.security.NoSuchAlgorithmException;

import iaik.security.provider.IAIK;

public class cripto1 {

  private Cipher cipher = null;
  private FileInputStream fileInputStream = null;
  private FileOutputStream fileOutputStream = null;
    PBEKeySpec pbeKeySpec;
    PBEParameterSpec pbeParamSpec;
    SecretKeyFactory keyFac;


  public cripto1(String sFileName) throws NoSuchPaddingException, NoSuchAlgorithmException
  ,java.security.spec.InvalidKeySpecException, java.security.InvalidAlgorithmParameterException
  , javax.crypto.BadPaddingException, java.security.InvalidKeyException
  , javax.crypto.IllegalBlockSizeException, java.security.NoSuchProviderException
  , java.io.IOException
  {

     //new  DatagramSocketPTMF(null,null,(byte)0,0,null,null);

     IAIK.addAsProvider(true);



    // Create the cipher desCipher =
    //cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");


    // Initialize the cipher for encryption
    //cipher.init(Cipher.ENCRYPT_MODE);





    // Salt
    byte[] salt = { (byte)0xc7, (byte)0x73, (byte)0x21, (byte)0x8c,
    (byte)0x7e, (byte)0xc8, (byte)0xee, (byte)0x99 };

    // Iteration count
    int count = 20;

    // Convert password into SecretKey object, using a PBE key
    pbeKeySpec = new PBEKeySpec("pepe".toCharArray());
    Log.log("DEPU 1","");

    // Create PBE Cipher
    Cipher pbeCipher = Cipher.getInstance("RC2/ECB/PKCS5Padding");//"PbeWithMD5AndDES_CBC");
    Cipher pbeUNCipher = Cipher.getInstance("RC2/ECB/PKCS5Padding");//"PbeWithMD5AndDES_CBC");
    Log.log("DEPU 4","");

    keyFac = SecretKeyFactory.getInstance("PBE","IAIK");//"PBEWithMD5AndDES");
    Log.log("DEPU 2","");

    SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);
    //PBEKeyBMP pbeKey  = new PBEKeyBMP("hola".toCharArray());

    Log.log("DEPU 3","");

    long lTiempoInicial = System.currentTimeMillis();

    // Create PBE parameter set
    pbeParamSpec = new PBEParameterSpec(salt, count);

    // Initialize PBE Cipher with key and parameters
    pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
    pbeUNCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);

    Log.log("DEPU 5","");
    long lTiempoFin = System.currentTimeMillis();

    Log.log("TIEMPO INIT: "+(lTiempoFin-lTiempoInicial),"");
    //File file = new File(sFileName);
    //FileInputStream fileInputStream = new FileInputStream(file);
    //Log.log("Abriendo fichero de entrada: "+sFileName,"");

    //FileOutputStream fileOutputStream = new FileOutputStream("salida_cipher.bmp");
    //Log.log("Abriendo fichero de salida: salida_cipher.jpg","");
    // Initialize the same cipher for decryption
    //desCipher.init(Cipher.DECRYPT_MODE, desKey);

    // Decrypt the ciphertext
    //byte[] cleartext1 = desCipher.doFinal(ciphertext);
    //int longitud = fileInputStream.available();
    //byte[] limpio = new byte[longitud];

    //Log.log("Leer: "+longitud+" bytes","");
    //fileInputStream.read(limpio);

    //TAMAÑO CABECERA
    //int SIZE_CABECERA = 1024*200;

    //Log.log("Copiar cabecera... ","");
    //byte[] cabecera = new byte[SIZE_CABECERA];
    //System.arraycopy(limpio,0,cabecera,0,SIZE_CABECERA);

    //Log.log("Cifrar... ","");
    //byte[] cifrado = pbeCipher.doFinal(limpio,SIZE_CABECERA,longitud-SIZE_CABECERA);
    //Log.log("Cifrado... ","");

    //Log.log("Escribiendo en el fichero de salida....","");
    //fileOutputStream.write(cabecera);
    //fileOutputStream.write(cifrado);

    //Log.log("Cerrando ficheros...","");
    //fileOutputStream.close();
    //fileInputStream.close();

   // Log.log("Texto claro:",""+new String(cleartext));
   // Log.log("Texto cifrado:",""+new String(ciphertext));


    // Our cleartext
    byte[] cleartext = "HOLA PEPE, HOLA PEPE, HOLA PEPE".getBytes();

    // Encrypt the cleartext
   // byte[] ciphertext = pbeCipher.doFinal(cleartext);

    lTiempoInicial = System.currentTimeMillis();

    // Encrypt the cleartext
    byte[] ciphertext1 = pbeCipher.doFinal(cleartext);
    lTiempoFin = System.currentTimeMillis();

    Log.log("TIEMPO ENCRIPTAR: "+(lTiempoFin-lTiempoInicial),"");

    byte[] ciphertext2 = pbeCipher.doFinal(cleartext);
    byte[] ciphertext3 = pbeCipher.doFinal(cleartext);
    byte[] ciphertext4 = pbeCipher.doFinal(cleartext);

    Log.log("1. Texto cifrado:",""+new String(ciphertext1));
    Log.log("2. Texto cifrado:",""+new String(ciphertext2));
    Log.log("3. Texto cifrado:",""+new String(ciphertext3));
    Log.log("4. Texto cifrado:",""+new String(ciphertext4));

    Log.log("3. Texto descifr:",""+new String(pbeUNCipher.doFinal(ciphertext3)));
    Log.log("1. Texto descifr:",""+new String(pbeUNCipher.doFinal(ciphertext1)));
    Log.log("2. Texto descifr:",""+new String(pbeUNCipher.doFinal(ciphertext2)));
    Log.log("4. Texto descifr:",""+new String(pbeUNCipher.doFinal(ciphertext4)));

    //Temporizador.sleep(8000);
  }


  public static void main(String[] args)
  {
    try
    {
      new cripto1(null);//args[0]);
    }
    catch(Exception e)
    {
     Log.log(""+e,"");
    }
  }
}
