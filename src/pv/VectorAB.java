package pv;

import acp.*;

public class VectorAB extends GO<PV2> {
  GO<PV2> a, b;

  public VectorAB (GO<PV2> a, GO<PV2> b) { this.a = a; this.b = b; }

  protected PV2 calculate () {
    return a.xyz().minus(b.xyz());
  }
}
