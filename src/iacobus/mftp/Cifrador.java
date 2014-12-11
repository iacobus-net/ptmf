

package iacobus.mftp;

import iacobus.ptmf.*;

public class Cifrador{

  //Objeto cifrador...
  javax.crypto.Cipher cipher = null;

  //Objeto descifrador...
  javax.crypto.Cipher unCipher = null;

  MFtp ftp = null;
  public Cifrador(MFtp ftp,char[] clave)
  {

    this.ftp = ftp;
    // Salt
    byte[] salt = { (byte)0xe7, (byte)0x43, (byte)0x71, (byte)0xec,
    (byte)0x7e, (byte)0xb8, (byte)0xff, (byte)0x35 };

    // Número de iteraciones
    int count = 20;


     Log.log("ANTES DEL IF","");

     if(new String(clave) != "")
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

        // Crear los objetos cipher y unCipher
        cipher = javax.crypto.Cipher.getInstance("RC4");//"PbeWithMD5AndDES_CBC");
        unCipher = javax.crypto.Cipher.getInstance("RC4");
        Log.log("DEPU 4","");

        // Inicializar los cifradores con la llave y los parámetros...
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
        unCipher.init(javax.crypto.Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);
        Log.log("DEPU 5","");
       }
       catch( java.security.NoSuchAlgorithmException e)
       {
         this.ftp.error(e.toString());
         return;
       }
       catch(java.security.InvalidKeyException e)
       {
         this.ftp.error(e.toString());
         return;
       }
       catch(java.security.NoSuchProviderException e)
       {
         this.ftp.error(e.toString());
         return;
       }
       catch(javax.crypto.NoSuchPaddingException e)
       {
         this.ftp.error(e.toString());
         return;
       }
       catch(java.security.InvalidAlgorithmParameterException e)
       {
         this.ftp.error(e.toString());
         return;
       }
       catch(java.security.spec.InvalidKeySpecException e)
       {
         this.ftp.error(e.toString());
         return;
       }

    }


  }
}