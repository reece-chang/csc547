package segment;
import acp.*;
import pv.*;

public class ABintersectCD extends GO<PV2> {
  public final GO<PV2> a, b, c, d;

  public ABintersectCD (GO<PV2> a, GO<PV2> b, GO<PV2>c, GO<PV2> d) {
    this.a = a;
    this.b = b;
    this.c = c;
    this.d = d;
  }

  public PV2 calculate () {
    PV2 pa = a.xyz();
    PV2 pb = b.xyz();
    PV2 pc = c.xyz();
    PV2 pd = d.xyz();
    PV2 vab = pb.minus(pa);
    PV2 vcd = pd.minus(pc);
    PV2 vac = pc.minus(pa);

    // EXERCISE 2
    // Calculate point of intersection of ab and cd.
    
    Real t;
    
    t = (vcd.cross(vac)).over(vcd.cross(vab));
    
    PV2 p = pa.plus(vab.times(t));//
    
    System.out.println("ap x ab " + p.minus(pa).cross(vab) + 
                       " cp x cd " + p.minus(pc).cross(vcd));
    return p;
  }
}
