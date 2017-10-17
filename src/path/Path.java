package path;
import java.util.List;
import java.util.ArrayList;
import acp.*;
import pv.*;
import hull.State;
import segment.Drawer;
import java.awt.Graphics2D;
import java.awt.Color;

public class Path {
  class PState implements State {
    List<GO<PV2>> ts = new ArrayList<GO<PV2>>();
    List<GO<PV2>> hs = new ArrayList<GO<PV2>>();
    int n;
    
    PState () {
      ts.addAll(tails);
      hs.addAll(heads);
      n = nPath;
    }

    public void draw (Graphics2D g) {
      for (int i = n; i < ts.size(); i++)
        Drawer.drawEdge(g, ts.get(i-1).xyz(), ts.get(i).xyz(), Color.blue, "");
      for (int i = n; i < hs.size(); i++)
        Drawer.drawEdge(g, hs.get(i-1).xyz(), hs.get(i).xyz(), Color.green, "");
      //for (int i = 1; i < ts.size() && i < hs.size() && ts.get(i) == hs.get(i); i++)
      for (int i = 1; i < n; i++)
        Drawer.drawEdge(g, hs.get(i-1).xyz(), hs.get(i).xyz(), Color.black, "");
    }
  }

  class TState extends PState {
    TState (GO<PV2> p) {
      ts.add(p);
    }
  }

  class HState extends PState {
    HState (GO<PV2> p) {
      hs.add(p);
    }
  }

  List<State> states = new ArrayList<State>();
  public int numStates () { return states.size(); }
  public State getState (int i) { return states.get(i); }

  public ArrayList<GO<PV2>> tails = new ArrayList<GO<PV2>>();
  public ArrayList<GO<PV2>> heads = new ArrayList<GO<PV2>>();
  public int nPath = 0;

  public void path (List<GO<PV2>[]> segs) {
    if (segs.size() == 0)
      return;
    tails.add(segs.get(0)[0]);
    heads.add(segs.get(0)[0]);
    nPath = 1;
    for (int i = 1; i < segs.size(); i++) {
      GO<PV2>[] seg = segs.get(i);
      addTail(seg[0]);
      addHead(seg[1]);
    }
  }

  void drawTail (GO<PV2> p) {
    // states.add(new TState(p));
    states.add(new PState());
  }

  void drawHead (GO<PV2> p) {
    // states.add(new HState(p));
    states.add(new PState());
  }

  void addTail (GO<PV2> p) {
    tails.add(p);
    states.add(new PState());
    int ts = tails.size();

    // EXERCISE
    // Do the convex hull thing to tails (the blue path).
    // nPath is the number of vertices on the common (black) path.
    // Those vertices are a fixed part of each path.
    // Add a new state at the bottom of the loop.

    while ((ts > nPath + 1) && (AreaABC.sign(p, tails.get(ts - 2), tails.get(ts - 3)) < 0)) {
    	tails.remove(ts - 2);
    	ts--;
    	states.add(new PState());
    }

    if (ts > nPath + 1)
      return;

    // EXERCISE
    // while the blue is to the wrong side of the first green
    // add the next green to the blue (before the last point)
    // and increment nPath
    // Add a new state at the bottom of the loop.

    while ((heads.size() > nPath) && (AreaABC.sign(p, heads.get(nPath - 1), heads.get(nPath)) > 0)) {
    	tails.add(ts - 1, heads.get(nPath));
    	nPath++;
    	ts++;
    	states.add(new PState());
    }

  }

  void addHead (GO<PV2> p) {
    heads.add(p);
    states.add(new PState());
    int hs = heads.size();

    // EXERCISE
    // Do the convex hull thing to heads (the green path).
    // nPath is the number of vertices on the common (black) path.
    // Those vertices are a fixed part of each path.
    // Add a new state at the bottom of the loop.

    while ((hs > nPath + 1) && (AreaABC.sign(p, heads.get(hs - 2), heads.get(hs - 3)) > 0)) {
    	heads.remove(hs - 2);
    	hs--;
    	states.add(new PState());
    }

    if (hs > nPath + 1)
      return;

    // EXERCISE
    // while the green is to the wrong side of the first blue
    // add the next blue to the green (before the last point)
    // and increment nPath
    // Add a new state at the bottom of the loop.

    while ((tails.size() > nPath) && (AreaABC.sign(p, tails.get(nPath - 1), tails.get(nPath)) < 0)) {
    	heads.add(hs - 1, tails.get(nPath));
    	nPath++;
    	hs++;
    	states.add(new PState());
    }
    
  }
}


  
           
