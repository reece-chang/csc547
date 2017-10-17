package segment;
import java.util.Set;
import java.util.HashSet;
import acp.*;
import pv.*;

public class Segment implements Comparable<Segment> {
  public final GO<PV2> tail, head;
  SweepNode node;
  Set<Segment> checked = new HashSet<Segment>();

  public Segment (GO<PV2> a, GO<PV2> b) {
    if (DiffY.sign(a, b) < 0) {
      tail = a;
      head = b;
    }
    else {
      tail = b;
      head = a;
    }
  }

  boolean intersects (Segment that) {
    // EXERCISE 1
    // Return true if this intersects that.    
	GO<PV2> a, b, c, d;
	a = this.tail;
	b = this.head;
	c = that.tail;
	d = that.head;
	
    return ((AreaABC.sign(a, b, c) != AreaABC.sign(a, b, d)) && (AreaABC.sign(c, d, a) != AreaABC.sign(c, d, b)));
  }

  GO<PV2> intersection (Segment that) {
    return new ABintersectCD(tail, head, that.tail, that.head);
  }

  public int compareTo (Segment that) {
    // EXERCISE 3
    // Compare the x position of this and that at the smallest value
    // of y they have in common.

    // (The graphics window has y increasing downwards and its
    // clockwise and counterclockwise are switched.)
	Segment lower, upper;
	
	if (DiffY.sign(this.tail, that.tail) > 0) {
		lower = that;
		upper = this;
	} else {
		lower = this;	
		upper = that;
	}
	
	return ((this == lower) ? 1 : -1) * AreaABC.sign(lower.tail, lower.head, upper.tail);

  }
}

