package iacobus.mftp;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;

/**
 * <p>Title: MFtp</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.1
 */

public class Applet1 extends Applet
{
  boolean isStandalone = false;
  //Get a parameter value
  public String getParameter(String key, String def)
  {
    return isStandalone ? System.getProperty(key, def) :
      (getParameter(key) != null ? getParameter(key) : def);
  }

  //Construct the applet
  public Applet1()
  {
  }
  //Initialize the applet
  public void init()
  {
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  //Component initialization
  private void jbInit() throws Exception
  {
  }
  //Start the applet
  public void start()
  {
  }
  //Stop the applet
  public void stop()
  {
  }
  //Destroy the applet
  public void destroy()
  {
  }
  //Get Applet information
  public String getAppletInfo()
  {
    return "Applet Information";
  }
  //Get parameter info
  public String[][] getParameterInfo()
  {
    return null;
  }
  //Main method
  public static void main(String[] args)
  {
    Applet1 applet = new Applet1();
    applet.isStandalone = true;
    Frame frame;
    frame = new Frame()
    {
      protected void processWindowEvent(WindowEvent e)
      {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING)
        {
          System.exit(0);
        }
      }
      public synchronized void setTitle(String title)
      {
        super.setTitle(title);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      }
    };
    frame.setTitle("Applet Frame");
    frame.add(applet, BorderLayout.CENTER);
    applet.init();
    applet.start();
    frame.setSize(400,320);
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setLocation((d.width - frame.getSize().width) / 2, (d.height - frame.getSize().height) / 2);
    frame.setVisible(true);
  }
}