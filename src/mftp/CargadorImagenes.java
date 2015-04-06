package mftp;

/**
  Copyright (c) 2000-2014 . All Rights Reserved.
  Autor: Alejandro García Domínguez alejandro.garcia.dominguez@gmail.com   alejandro@iacobus.com
         Antonio Berrocal Piris antonioberrocalpiris@gmail.com
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

import java.awt.*;
import java.awt.image.*;
import java.net.URL;
import java.util.Hashtable;


/**
 * 
 */
public class CargadorImagenes extends Component {

    public static Hashtable cache;

    private String[] images =
    {
        "desconectar.gif", "conectar.gif"
    };

    public CargadorImagenes() {
        cache = new Hashtable(images.length);
        for (int i = 0; i < images.length; i++) {
            cache.put(images[i], getImage(images[i], this));
        }
    }


    public static Image getImage(String fileName, Component cmp) {
        URL url = CargadorImagenes.class.getResource("PTMF/mFtp/images/" + fileName);

        Image img = cmp.getToolkit().createImage(url);
        MediaTracker tracker = new MediaTracker(cmp);
        tracker.addImage(img, 0);
        try {
            tracker.waitForID(0);
            if (tracker.isErrorAny()) {
                System.out.println("Error cargando imagen");
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return img;
    }
}
