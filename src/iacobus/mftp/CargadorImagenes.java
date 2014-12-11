

package iacobus.mftp;


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
        URL url = CargadorImagenes.class.getResource("PTMF/MFtp/images/" + fileName);

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
