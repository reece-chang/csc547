package pv;
import acp.*;

public class ABintersectCD extends GO<PV2> {
  static class Pair {
    GO<PV2> a, b;
    
    Pair (GO<PV2> a, GO<PV2> b) {
      Pair p = null;
      if (a instanceof ABintersectCD) {
        ABintersectCD ai = (ABintersectCD) a;
        if (b instanceof ABintersectCD) {
          ABintersectCD bi = (ABintersectCD) b;
          if (ai.contains(bi.ab))
            p = bi.ab;
          else if (ai.contains(bi.cd))
            p = bi.cd;
        }
        else {
          if (ai.ab.contains(b))
            p = ai.ab;
          else if (ai.cd.contains(b))
            p = ai.cd;
        }
      }
      else {
        if (b instanceof ABintersectCD) {
          ABintersectCD bi = (ABintersectCD) b;
          if (bi.ab.contains(a))
            p = bi.ab;
          else if (bi.cd.contains(a))
            p = bi.cd;
        }
      }
      if (p == null) {
        this.a = a;
        this.b = b;
      }
      else {
        this.a = p.a;
        this.b = p.b;
      }
    }
    
    boolean contains (GO<PV2> p) { return p == a || p == b; }
    boolean sameas (Pair p) { return p.contains(a) && p.contains(b); }
  }

  boolean contains (Pair p) { return p.sameas(ab) || p.sameas(cd); }

  public final Pair ab, cd;

  public ABintersectCD (GO<PV2> a, GO<PV2> b, GO<PV2>c, GO<PV2> d) {
    ab = new Pair(a, b);
    cd = new Pair(c, d);
  }

  public PV2 calculate () {
    PV2 pa = ab.a.xyz();
    PV2 pb = ab.b.xyz();
    PV2 pc = cd.a.xyz();
    PV2 pd = cd.b.xyz();
    PV2 vab = pb.minus(pa);
    PV2 vcd = pd.minus(pc);
    PV2 vca = pa.minus(pc);

    // (a + ab t - c) x cd = 0
    // (ca + ab t) x cd = 0
    // ca x cd + ab x cd t = 0
    // t = -(ca x cd) / (ab x cd)
    Real t = vca.cross(vcd).minus().over(vab.cross(vcd));
    PV2 p = pa.plus(vab.times(t));
    // System.out.println("ap x ab " + p.minus(pa).cross(vab) + 
    // " cp x cd " + p.minus(pc).cross(vcd));
    return p;
  }
}
