package circle;

import java.awt.Graphics2D;
import java.awt.Color;
import acp.*;
import pv.*;
import segment.Drawer;

public class Circle extends GO<PV2Scalar> {
  GO<PV2> a, b, c;

  public PV2 center () { return xyz(); }
  public Real radius2 () { return xyz().s; }

  public Circle (GO<PV2> a, GO<PV2> b) {
    this.a = a;
    this.b = b;
  }

  public Circle (GO<PV2> a, GO<PV2> b, GO<PV2> c) {
    this.a = a;
    this.b = b;
    this.c = c;
  }

  public PV2Scalar calculate () {
    if (c == null) {
      PV2 ap = a.xyz();
      PV2 bp = b.xyz();
      PV2 center = ap.plus(bp).over(2);
      Real radius2 = ap.minus(center).dot(ap.minus(center));
      return new PV2Scalar(center, radius2);
    } else {
      PV2 ap = a.xyz();
      PV2 bp = b.xyz();
      PV2 cp = c.xyz();
      PV2 mp = bp.plus(ap).over(2);
      PV2 pp = cp.plus(bp).over(2);
      PV2 vn = cp.minus(bp);
      PV2 vu = bp.minus(ap).rot90();
      
      Real t = (vn.dot(pp.minus(mp))).over(vn.dot(vu));
      
      PV2 center = mp.plus(vu.times(t));
      Real radius2 = ap.minus(center).dot(ap.minus(center));
    	
      return new PV2Scalar(center, radius2);
    }
  }
  
  public boolean contains (GO<PV2> p) {
    return CirclePoint.sign(this, p) < 0;
  }

  public void draw (Graphics2D g, Color color) {
    PV2 c = center();
    Real r2 = radius2();
    double x = c.x.approx();
    double y = c.y.approx();
    double r = Math.sqrt(r2.approx());
    g.setColor(color);
    g.drawArc((int) (x - r), (int) (y - r), (int) (2 * r), (int) (2 * r),
              0, 360);
  }
}
