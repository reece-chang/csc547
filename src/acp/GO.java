package acp;

import java.util.List;
import java.util.ArrayList;

public abstract class GO <P extends XYZ> {
  P p;

  protected abstract P calculate ();

  int precision () {
    return p == null ? 0 : 
      p.size() == 0 ? Real.curPrecision : p.get(0).precision();
  }

  public P xyz () {
    int prec = precision();
    
    if (prec < Real.curPrecision) {
      if (Real.handleSignException)
        safe_setp();
      else
        p = calculate();
      if (Real.curPrecision > 53 && prec <= 53)
        increased.add(this);
    }
    else
      assert prec == Real.curPrecision;

    return (P) p.clone();
  }

  public int size () { return p.size(); }

  public int sign (int i) {
    if (!Real.handleSignException)
      return xyz().get(i).sign();
    Real.handleSignException = false;
    boolean failed = false;
    while (true)
      try {
        int s = xyz().get(i).sign();
        if (failed)
          decreaseAll();
        Real.handleSignException = true;
        return s;
      } catch (Real.SignException se) {
        failed = true;
      }
  }

  public int sign () {
    return sign(0);
  }

  void safe_setp () {
    assert Real.handleSignException;
    Real.handleSignException = false;
    boolean failed = false;
    while (true)
      try {
        p = calculate();
        if (failed)
          decreaseAll();
        Real.handleSignException = true;
        return;
      } catch (Real.SignException se) {
        failed = true;
      }
  }

  private static List<GO> increased = new ArrayList<GO>();

  void changePrecision () {
    for (int i = 0; i < p.size(); i++)
      p.set(i, p.get(i).changePrecision());
  }

  static void decreaseAll () {
    Real.curPrecision = 53;
    for (GO o : increased)
      o.changePrecision();
    increased.clear();
  }
}


  
