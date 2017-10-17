import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.HashSet;

import acp.*;
import pv.*;
import path.*;

public class TestPath extends JApplet implements ActionListener {
  static protected JLabel label;
  static protected JButton button;
  
  DPanel d;
  
  public void init(){
    getContentPane().setLayout(new BorderLayout());
    
    d = new DPanel();
    d.setBackground(Color.white);
    getContentPane().add(d);
    
    label = new JLabel("Press and drag to make segments then click step repeatedly.");
    getContentPane().add("South", label);
    
    button = new JButton("step");
    button.addActionListener(this);
    getContentPane().add("North", button);
  }
  
  public void actionPerformed (ActionEvent e) {
    d.actionPerformed(e);
  }

  public static void main(String s[]) {
    JFrame f = new JFrame("TestPath");
    f.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {System.exit(0);}
      });
    JApplet applet = new TestPath();
    f.getContentPane().add("Center", applet);
    applet.init();
    f.pack();
    f.setSize(new Dimension(550,250));
    f.setVisible(true);
  }
  
  class DPanel extends JPanel implements MouseListener, MouseMotionListener {
    ArrayList<GO<PV2>[]> segments = new ArrayList<GO<PV2>[]>();
    
    public DPanel(){
      setBackground(Color.white);
      addMouseListener(this);
      addMouseMotionListener(this);
    }
    
    Path path;
    boolean stepping = false;
    int iStep = 0;
    List<Event> events;
    Iterator<Event> eventIt;
    double sweepY;
    ArrayList<GO<PV2>> intersections;

    public void actionPerformed (ActionEvent e) {
      if (!stepping) {
        iStep = 0;
        stepping = true;
      }
      else {
        iStep++;
        if (iStep == path.numStates())
          stepping = false;
      }
      repaint();
    }
    
    GO<PV2> pressed;

    // Handles the event of the user pressing down the mouse button.
    public void mousePressed(MouseEvent e){
      GO<PV2> mouse = new InputPoint(e.getX(), e.getY());
      System.out.println("mouse pressed " + mouse.xyz().x.approx() + " " + mouse.xyz().y.approx());
      
      pressed = mouse;
    }
    
    public void mouseDragged (MouseEvent e) {
    }
    
    public void mouseMoved (MouseEvent e) {
    }
    
    // Handles the event of a user releasing the mouse button.
    public void mouseReleased(MouseEvent e) {
      GO<PV2> mouse = new InputPoint(e.getX(), e.getY());
      System.out.println("mouse released " + mouse.xyz().x.approx() + " " + mouse.xyz().y.approx());
      
      GO[] segment = { pressed, mouse };
      segments.add((GO<PV2>[]) segment);
      pressed = null;
      repaint();
    }
    public void mouseClicked(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
    
    void drawEdge (Graphics2D g2, Color color, GO<PV2> p, GO<PV2> q) {
      if (false)
        System.out.println("drawEdge " + color + " " +
                           p.xyz().x.approx() + " " + p.xyz().y.approx() + " " +
                           q.xyz().x.approx() + " " + q.xyz().y.approx() + " ");
      g2.setPaint(color);
      g2.draw(new Line2D.Double(p.xyz().x.approx(), p.xyz().y.approx(), q.xyz().x.approx(), q.xyz().y.approx()));
    }
    
    private void drawPoint(Graphics2D g, GO<PV2> p, String label, int size, Color color) {
      g.setColor(color);
      
      float offset = size / 2.0f;
      g.fillRect((int)(p.xyz().x.approx() - offset),(int)(p.xyz().y.approx() - offset),size,size);
      g.drawString(label, (int)(p.xyz().x.approx() + offset), (int)(p.xyz().y.approx() + offset));
    }
		
    void drawCircle (Graphics2D g2, Color color, GO<PV2> c, double r) {
      g2.setColor(color);
      g2.drawArc((int) (c.xyz().x.approx() - r),
                 (int) (c.xyz().y.approx() - r),
                 (int) (2 * r), (int) (2 * r),
                 0, 360);
    }

    public void paintComponent(Graphics g){
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D)g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setStroke(new BasicStroke(1.0f));

      for (GO<PV2>[] s : segments)
        drawEdge(g2, Color.red, s[0], s[1]);

      if (!stepping) {
        path = new Path();
        path.path(segments);

        drawPaths(g2, path.tails, path.heads);
      }
      else {
        // drawPaths(g2, path.allTails.get(iStep), path.allHeads.get(iStep));
        path.getState(iStep).draw(g2);
      }
    }

    void drawPaths (Graphics2D g2, List<GO<PV2>> tails, List<GO<PV2>> heads) {
      for (int i = 1; i < tails.size(); i++)
        drawEdge(g2, Color.blue, tails.get(i-1), tails.get(i));
      
      for (int i = 1; i < heads.size(); i++)
        drawEdge(g2, Color.green, heads.get(i-1), heads.get(i));
      
      for (int i = 1;
           i < tails.size() && i < heads.size() &&
             tails.get(i) == heads.get(i); i++)
        drawEdge(g2, Color.black, heads.get(i-1), heads.get(i));
    }
  }
}
