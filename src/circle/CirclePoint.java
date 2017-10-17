package circle;

import acp.*;
import pv.*;

public class CirclePoint extends GO<Scalar> {
  Circle c;
  GO<PV2> p;

  public CirclePoint (Circle c, GO<PV2> p) {
    this.c = c;
    this.p = p;
  }

  protected Scalar calculate () {
	PV2 cp = p.xyz().minus(c.center());
    Real distance = (cp.dot(cp)).minus(c.radius2());
    return new Scalar(distance);
  }

  public static int sign (Circle c, GO<PV2> p) {
    return new CirclePoint(c, p).sign();
  }
}


    
