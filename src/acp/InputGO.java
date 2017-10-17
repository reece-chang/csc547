package acp;

import java.util.List;

public class InputGO <P extends XYZ> extends GO<P> {
  final protected P calculate () {
    changePrecision();
    return p;
  }

  protected InputGO (P p) { 
    this.p = (P) p.clone(); 
    for (int i = 0; i < p.size(); i++)
      assert p.get(i).lb() == p.get(i).ub() 
        : "input object has non-trivial interval";
  }
}


    
