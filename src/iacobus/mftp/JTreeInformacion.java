package iacobus.mftp;

/**
 * <p>Title: MFtp</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.1
 */




import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import iacobus.ptmf.*;
import java.util.Enumeration;

/**
 * Esta clase implementa el árbol de información de PTMF.
 * <p>Title: MFtp</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.1
 */
public class JTreeInformacion extends JPanel //implements TreeModel
{
    protected DefaultMutableTreeNode rootNode;
    protected DefaultMutableTreeNode nodoIDGL = null;
    protected DefaultMutableTreeNode nodoIDSocket = null;

    protected DefaultTreeModel treeModel;
    protected JTree tree;
    private Toolkit toolkit = Toolkit.getDefaultToolkit();

    public JTreeInformacion() {
        rootNode = new DefaultMutableTreeNode("PTMF");
        treeModel = new DefaultTreeModel(rootNode);
        //treeModel.addTreeModelListener(new MyTreeModelListener());

        tree = new JTree(treeModel);
        tree.setEditable(true);
        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setShowsRootHandles(true);

        //Nodo IDGLs
        nodoIDGL = new DefaultMutableTreeNode("IDGLs");
        rootNode.add(nodoIDGL);

        //Nodo IDSockets
        nodoIDSocket = new DefaultMutableTreeNode("IDSockets");
        rootNode.add(nodoIDSocket);


        JScrollPane scrollPane = new JScrollPane(tree);
        setLayout(new GridLayout(1,0));
        add(scrollPane);
    }

    /** Remove all nodes except the root node. */
    public void clear() {
        this.nodoIDGL.removeAllChildren();
        this.nodoIDSocket.removeAllChildren();
        treeModel.reload();
    }

    /** Remove the currently selected node. */
    public void removeCurrentNode() {
        TreePath currentSelection = tree.getSelectionPath();
        if (currentSelection != null) {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)
                         (currentSelection.getLastPathComponent());
            MutableTreeNode parent = (MutableTreeNode)(currentNode.getParent());
            if (parent != null) {
                treeModel.removeNodeFromParent(currentNode);
                return;
            }
        }

        // Either there was no selection, or the root was selected.
        toolkit.beep();
    }


    /**
     * Añade un IDGL al árbol
     */
    public DefaultMutableTreeNode addIDGL(IDGL idgl)
    {
       return addObject(nodoIDGL, idgl, true);
    }

    /**
     * Añade un ID_Socket al árbol
     */
    public DefaultMutableTreeNode addID_Socket(ID_Socket idSocket)
    {
       return addObject(nodoIDSocket, idSocket, true);
    }

   /**
     * Eliminar un IDGL al árbol
     */
    public void removeIDGL(IDGL idgl)
    {

      Enumeration nodosIDGLS =  nodoIDGL.children();

      while(nodosIDGLS.hasMoreElements())
      {
        DefaultMutableTreeNode nodo =  (DefaultMutableTreeNode) nodosIDGLS.nextElement();

        IDGL nodoIDGL =  (IDGL) nodo.getUserObject();

        if (nodoIDGL.equals(idgl))
        {
          treeModel.removeNodeFromParent(nodo);
        }
      }
    }


    /**
     * Eliminar un IDSocket al árbol
     */
    public void removeIDSocket(ID_Socket idSocket)
    {
      Enumeration nodosIDSockets =  this.nodoIDSocket.children();

      while(nodosIDSockets.hasMoreElements())
      {
        DefaultMutableTreeNode nodo =  (DefaultMutableTreeNode) nodosIDSockets.nextElement();

        ID_Socket nodoIDSocket=  (ID_Socket) nodo.getUserObject();

        if (nodoIDSocket.equals(idSocket))
        {
          treeModel.removeNodeFromParent(nodo);
        }
      }
    }


    /** Add child to the currently selected node. */
    public DefaultMutableTreeNode addObject(Object child) {
        DefaultMutableTreeNode parentNode = null;
        TreePath parentPath = tree.getSelectionPath();

        if (parentPath == null) {
            parentNode = rootNode;
        } else {
            parentNode = (DefaultMutableTreeNode)
                         (parentPath.getLastPathComponent());
        }

        return addObject(parentNode, child, true);
    }

    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
                                            Object child) {
        return addObject(parent, child, false);
    }


    /**
     * Añade un nodo al árbol dado un nodo padre
     * @param parent
     * @param child
     * @param shouldBeVisible
     * @return
     */
    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
                                            Object child,
                                            boolean shouldBeVisible) {
        DefaultMutableTreeNode childNode =
                new DefaultMutableTreeNode(child);

        if (parent == null) {
            parent = rootNode;
        }

        treeModel.insertNodeInto(childNode, parent,
                                 parent.getChildCount());

        // Make sure the user can see the lovely new node.
        if (shouldBeVisible) {
            tree.scrollPathToVisible(new TreePath(childNode.getPath()));
        }
        return childNode;
    }



    // ============  Interfaz  TreeModel ==============
    public void addTreeModelListener(TreeModelListener l)
    {
      ;
    }

    public void removeTreeModelListener(TreeModelListener l)
    {
      ;
    }

  public int getIndexOfChild(Object parent,
                           Object child)
  {

   return 0;
  }

  public void valueForPathChanged(TreePath path,
                                Object newValue)
  {
    ;
  }


   public boolean isLeaf(Object node)
   {
     return true;
   }

   public int getChildCount(Object parent)
   {
      return 0;
   }

   public void getChild(Object parent,
                       int index)
   {
    ;
   }

   public void getRoot()
   {
      ;
   }
}

