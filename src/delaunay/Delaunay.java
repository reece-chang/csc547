package delaunay;

import java.util.List;
import java.util.ArrayList;
import java.awt.Graphics2D;
import java.awt.Color;
import acp.*;
import pv.*;
import hull.State;
import circle.Circle;
import segment.Drawer;

public class Delaunay {
  /** A snapshot of the current triangulation for animation purposes.
    * If a triangle is being checked, set check to that triangle.
    */
  public class DState implements State {
    public DState (Circle c) {
      for (Triangle t : Delaunay.this.triangles)
        if (t.c == null)
          triangles.add(t);
      this.c = c;
    }

    public List<Triangle> triangles = new ArrayList<Triangle>();
    public Circle c;

    public void draw (Graphics2D g) {
      for (Triangle t : triangles)
        t.draw(g, Color.green);
      if (c != null)
        c.draw(g, Color.red);
    }
  }

  public List<State> states = new ArrayList<State>();
  public int numStates () { return states.size(); }
  public State getState (int i) { return states.get(i); }

  public class Triangle {

    /** vertices */
    public GO<PV2>[] v = (GO<PV2>[]) new GO[3];

    /** neighbors:  t[i] is across from v[i] */
    public Triangle[] t = new Triangle[3];

    /** children:
     * If three children, then c[i] has a new vertex to replace v[i].
     */
    public Triangle[] c;

    /** Create a triangle with vertices a,b,c. Add this to triangles. */
    public Triangle (GO<PV2> a, GO<PV2> b, GO<PV2> c) {
      v[0] = a;
      v[1] = b;
      v[2] = c;
      triangles.add(this);
    }

    /** Create a triangle which is a copy of that, with the same
      * vertices and neighbors.  Add this to triangles.
      */
    public Triangle (Triangle that) {
      for (int i = 0; i < 3; i++) {
        v[i] = that.v[i];
        t[i] = that.t[i];
      }
      triangles.add(this);
    }

    public void draw (Graphics2D g, Color color) {
      Drawer.drawEdge(g, v[0].xyz(), v[1].xyz(), color, "");
      Drawer.drawEdge(g, v[1].xyz(), v[2].xyz(), color, "");
      Drawer.drawEdge(g, v[2].xyz(), v[0].xyz(), color, "");
    }

    /** Test of this triangles contains point p. */
    public boolean contains (GO<PV2> p) {
    	
      // Return false if p is on the wrong side of an edge.
     return ((AreaABC.sign(v[0], v[1], p) > 0) && (AreaABC.sign(v[1], v[2], p) > 0) && (AreaABC.sign(v[2], v[0], p) > 0));

    }
    
    /** Following child pointers, locate the descendant which contains p. */
    public Triangle locate (GO<PV2> p) {
    	
      if (c == null) {
        return this;
      }
      // Recurse on the child which contains p.

      for (int i = 0; i < c.length; i++) {
    	  if (c[i].contains(p)) 
    		  return c[i].locate(p);
      }

      assert(false);
      return null;
    }

    /** Split this triangle into three at p.
      * Child c[i] will have p at c[i].v[i].
      */
    public void split (GO<PV2> p) {
      // Remove when you implement:
      
      // Each child starts out as copy of this triangle.
      c = new Triangle[3];
      for (int i = 0; i < 3; i++) {
        c[i] = new Triangle(this);
        
        // For each child, set one v and update a neighbor.
  	    c[i].v[i] = p;
  	    c[i].updateNeighbor(i, this);
      }
      
      // For each child, make two other children neighbors.    
      for (int i = 0; i < 3; i++) {
    	  c[i].t[(i+1)%3] = c[(i+1)%3];
    	  c[i].t[(i+2)%3] = c[(i+2)%3];
      }

      // Debug
      for (int i = 0; i < 3; i++)
        c[i].checkNeighbors();

    }

    /** Find the index of neighbor that in t[].
      * Return -1 if that is not a neighbor.
      */
    public int find (Triangle that) {
      for (int i = 0; i < 3; i++)
        if (t[i] == that)
          return i;
      assert(false);
      return -1;
    }

    /** Check find method on neighbors.  Just for debugging. */
    public void checkNeighbors () {
      System.out.println("checkNeighbors " + this);
      if (t[0] != null)
        t[0].find(this);
      if (t[1] != null)
        t[1].find(this);
      if (t[2] != null)
        t[2].find(this);
    }

    /** Inform neighbor t[i] that it now has this triangle as a
      * neighbor in place of oldT.  Call it when oldT is split.
      */
    public void updateNeighbor (int i, Triangle oldT) {
      if (t[i] == null)
        return;
      t[i].t[t[i].find(oldT)] = this;
    }      

    /** Flip this triangle with neighbor t[i].
     * Children will both have v[i] at index i.
     */
    public void flip (int i) {
      // Remove this when you implement flip.
    	
    	
      c = new Triangle[2];
      t[i].c = c;
      // Children start out as copies of this triangle.
      c[0] = new Triangle(this);
      c[1] = new Triangle(this);

      // Initialize j and k to "i+1" and "i+2"
      int j, k;
      j = (i+1)%3;
      k = (i+2)%3;

      // Initialize i2, j2, k2 to the counterparts in t[i].
      int i2, j2, k2;
      i2 = t[i].find(this);
      j2 = (i2+1)%3;
      k2 = (i2+2)%3;
      
      // Set a vertex and neighbor of c[0].
      // Update two neighbors.
      c[0].v[k] = t[i].v[i2];
      c[0].t[i] = t[i].t[j2];
      c[0].updateNeighbor(k, this);
      c[0].updateNeighbor(i, t[i]);
      
      // Set a vertex and neighbor of c[1].
      // Update two neighbors.
      c[1].v[j] = t[i].v[i2];
      c[1].t[i] = t[i].t[k2];
      c[1].updateNeighbor(i, t[i]);
      c[1].updateNeighbor(j, this);

      // Make c[0] and c[1] neighbors of each other.	
      c[0].t[j] = c[1];
      c[1].t[k] = c[0];
      
      // debugging
      c[0].checkNeighbors();
      c[1].checkNeighbors();
    }

    /** Check if this triangle and neighbor t[i] should be flipped. If
      * it should, do the flip and recursively check the two new
      * triangles.
      */
    public void checkForFlip (int i) {
      Circle circle = new Circle(v[0], v[1], v[2]);
      states.add(new DState(circle));

      // Return if t[i] is null or
      // either new triangle would be clockwise or
      // circle does not contain opposite vertex of t[i].

      // Until you implement flip, the extra appearance of the circle
      // will signal that you detected the need to flip.
      if (t[i] == null)
        return;
      
      int i2 = t[i].find(this);
      
      int j = (i+1)%3;
      int k = (j+1)%3;
      
      GO<PV2> a = v[i];
      GO<PV2> b = v[j];
      GO<PV2> c = v[k];
      GO<PV2> d = t[i].v[i2];

      if (AreaABC.sign(a, b, d) < 0)
        return;

      if (AreaABC.sign(a, c, d) > 0)
        return;

      if (!circle.contains(d))
        return;

      states.add(new DState(circle));

      flip(i);
      states.add(new DState(null));

      // Recursive calls for when you have implemented flip.
      //if (false) {
        this.c[0].checkForFlip(i);
        this.c[1].checkForFlip(i);
      //}
    }
  }

  public List<Triangle> triangles = new ArrayList<Triangle>();
  public Triangle root;

  public void draw (Graphics2D g) {
    for (Triangle t : triangles)
      if (t.c == null)
        t.draw(g, Color.blue);
  }

  public void triangulate (List<GO<PV2>> in) {
    states.clear();
    triangles.clear();
    
    double minX = Double.POSITIVE_INFINITY;
    double minY = Double.POSITIVE_INFINITY;
    double maxX = Double.NEGATIVE_INFINITY;
    double maxY = Double.NEGATIVE_INFINITY;
    
    for (GO<PV2> p : in) {
      PV2 pp = p.xyz();
      double x = pp.x.approx();
      double y = pp.y.approx();
      if (minX > x)
        minX = x;
      if (minY > y)
        minY = y;
      if (maxX < x)
        maxX = x;
      if (maxY < y)
        maxY = y;
    }
    System.out.println(minX + " " + minY + " " + maxX + " " + maxY);
    root = new Triangle(new InputPoint(minX - 100, minY - 100),
                        new InputPoint(2 * maxX - minX + 200, minY - 100),
                        new InputPoint(minX - 100, 2 * maxY - minY + 200));

    for (GO<PV2> p : in) {
      states.add(new DState(null));

      Triangle t = root.locate(p);
      t.split(p);

      states.add(new DState(null));

      for (int i = 0; i < 3; i++)
        t.c[i].checkForFlip(i);
    }
  }
}
