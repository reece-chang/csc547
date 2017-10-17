package pv;

import acp.*;

public class InputPoint extends InputGO<PV2> {
  public InputPoint (double x, double y) {
    super(PV2.input(x, y));
  }
}
