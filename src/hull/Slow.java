package hull;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Arrays;
import java.awt.Graphics2D;
import java.awt.Color;

import acp.*;
import pv.*;

public class Slow implements Huller {
  private static class Edge {
    GO<PV2> tail, head;
    Edge (GO<PV2> tail, GO<PV2> head) {
      this.tail = tail;
      this.head = head;
    }
  }

  private static class SState implements hull.State {
    List<Edge> edges = new ArrayList<Edge>();
    Edge edge;
    String label;
    GO<PV2> point;
    String[] labels;

    SState (List<Edge> edges, Edge edge, String label, GO<PV2> point) {
      this.edges.addAll(edges);
      this.edge = edge;
      this.label = label;
      this.point = point;
    }
    SState (List<Edge> edges, Edge edge, String label, GO<PV2> point,
            String label0, String label1, String label2) {
      this.edges.addAll(edges);
      this.edge = edge;
      this.label = label;
      this.point = point;
      String[] labels = { label0, label1, label2 };
      this.labels = labels;
    }
    public void draw (Graphics2D g) {
      for (Edge e : edges)
        Drawer.drawEdge(g, e.tail.xyz(), e.head.xyz(), Color.green, "");
        
      if (labels != null) {
        Drawer.drawPoint(g, edge.tail.xyz(), Color.red, labels[0]); 
        Drawer.drawPoint(g, edge.head.xyz(), Color.red, labels[1]); 
      }

      if (edge != null) {
        Drawer.drawEdge(g, edge.tail.xyz(), edge.head.xyz(), Color.blue, label);

        if (point != null) {
          Drawer.drawEdge(g, edge.tail.xyz(), point.xyz(), Color.red, "");
          Drawer.drawEdge(g, edge.head.xyz(), point.xyz(), Color.red, "");

          if (labels != null)
            Drawer.drawPoint(g, point.xyz(), Color.red, labels[2]); 
        }
      }
    }
  }
  
  List<State> states = new ArrayList<State>();
  public int numStates () { return states.size(); }
  public State getState (int i) { return states.get(i); }      

  /**
   * Calculate the convex hull of a set of points.
   * @param 	in	input points
   * @return list of point on hull in clockwise order (will appear counterclockwise on screen)
   */
  public List<GO<PV2>> hull (List<GO<PV2>> in) {
    states.clear();

    List<Edge> edges = new ArrayList<Edge>();

    states.add(new SState(edges, null, null, null));

    List<GO<PV2>> out = new ArrayList<GO<PV2>>();

    if (in.size() < 2) {
      out.addAll(in);
      return out;
    }

     for (int i = 0; i < in.size(); i++)
      for (int j = 0; j < in.size(); j++) {
        if (i == j)
          continue;
        Edge edge = new Edge(in.get(i), in.get(j));
        states.add(new SState(edges, edge, "", null, "p", "q", ""));

        for (int k = 0; k < in.size(); k++) {
          if (k == i || k == j)
            continue;
          GO<PV2> point = in.get(k);
          if (AreaABC.sign(edge.tail, edge.head, point) < 0) {
            states.add(new SState(edges, edge, "BAD", point, "p", "q", "r"));
            edge = null;
            break;
          }
          states.add(new SState(edges, edge, "OK", point, "p", "q", "r"));
        }
        
        if (edge != null) {
          states.add(new SState(edges, edge, "GOOD", null, "p", "q", ""));
          edges.add(edge);
          states.add(new SState(edges, null, null, null));
        }
      }

    Edge edge = edges.get(0);
    for (int i = 0; i < edges.size(); i++) {
      out.add(edge.tail);
      for (int j = 0; j < edges.size(); j++)
        if (edges.get(j).tail == edge.head) {
          edge = edges.get(j);
          break;
        }
    }
    

    return out;
  }
}
