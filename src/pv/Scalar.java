package pv;

import acp.Real;
import acp.XYZ;

public class Scalar implements XYZ,Cloneable {
  public Real x;

  public Scalar (Real x) { this.x = x; }

  public int size () { return 1; }

  public Real get (int i) {
    if (i == 0)
      return x;
    throw new IndexOutOfBoundsException();    
  }

  public Real set (int i, Real v) {
    if (i == 0)
      return x = v;
    throw new IndexOutOfBoundsException();    
  }

  public Object clone () {
    try {
      return super.clone(); 
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }
}
