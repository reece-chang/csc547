package circle;

import java.util.List;
import java.util.ArrayList;
import java.awt.Graphics2D;
import java.awt.Color;
import acp.*;
import pv.*;
import hull.State;

public class SmallestCircle {
  class CState implements State {
    Circle c;

    CState (Circle c) {
      this.c = c;
    }

    public void draw (Graphics2D g) {
      c.draw(g, Color.blue);
    }
  }

  List<State> states = new ArrayList<State>();
  public int numStates () { return states.size(); }
  public State getState (int i) { return states.get(i); }

  /**
   * The smallest circle containing points[0..n-1].
   */
  public Circle smallestCircle (List<GO<PV2>> points) {
    states.clear();

    int n = points.size();
    Circle circle = new Circle(points.get(0), points.get(1));
    states.add(new CState(circle));

    // EXERCISE
    for (int k = 2; k < points.size(); k++) {
    	GO<PV2> p = points.get(k);
    	if (!circle.contains(p)) {
    		circle = smallestCircle(p, points, k-1);
    	}
    }

    return circle;
  }

  /**
   * The smallest circle through p containing points[0..n-1]
   */
  public Circle smallestCircle (GO<PV2> p, List<GO<PV2>> points, int n) {
    Circle circle = new Circle(p, points.get(0));
    states.add(new CState(circle));

    // EXERCISE
    for (int k = 1; k <= n; k++) {
    	GO<PV2> q = points.get(k);
    	if (!circle.contains(q)) {
    		circle = smallestCircle(p, q, points, k-1);
    	}
    }
    return circle;
  }

  /**
   * The smallest circle through p and q containing points[0..n-1]
   */
  public Circle smallestCircle (GO<PV2> p, GO<PV2> q, List<GO<PV2>> points, int n) {
    Circle circle = new Circle(p, q);
    states.add(new CState(circle));

    // EXERCISE
    for (int k = 1; k <= n; k++) {
    	GO<PV2> r = points.get(k);
    	if (!circle.contains(r)) {
    		circle = new Circle(p, q, r);
    	}
    }

    return circle;
  }
}

  
