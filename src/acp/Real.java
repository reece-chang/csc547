package acp;
import java.util.Random;

public abstract class Real {
  static double delta = 1.0 / (1 << 26);
  static int curPrecision = 16;
  static int maxPrecision = 256;
  static boolean handleSignException = true;

  private static Random random = new Random();
  private static double random1 () {
    return random.nextDouble() * 2 - 1;
  }

  public static Real input (double x) { 
    return constant(x * (1 + random1() * delta));
  }

  public static Real constant (double x) { 
    return curPrecision == 16 ? new DoubleInterval(x) : new BigInterval(x);
  }

  public Real plus (double d) { return plus(constant(d)); }
  public Real minus (double d) { return minus(constant(d)); }
  public Real times (double d) { return times(constant(d)); }
  public Real over (double d) { return over(constant(d)); }

  public abstract int precision ();
  public abstract Real plus (Real that);
  public abstract Real minus ();
  public abstract Real minus (Real that);
  public abstract Real times (Real that);
  public abstract Real over (Real that);
  public abstract Real sqrt ();
  public abstract int weakSign ();

  int sign () {
    int s = weakSign();
    if (s != 0)
      return s;
    if (true)
      throw new PrecisionException();
    curPrecision *= 2;
    if (curPrecision > maxPrecision)
      throw new PrecisionException();
    throw new SignException();
  }
    
  public abstract double lb ();
  public abstract double ub ();
  public double mid () { return (lb() + ub()) / 2.0; }
  public double approx () { return mid(); }
  public String toString () { return "[" + lb() + ", " + ub() + "]"; }

  Real changePrecision () {
    return curPrecision == 16 ? 
      new DoubleInterval(this) : new BigInterval(this);
  }

  class SignException extends RuntimeException {
    SignException () {
      super("sign exception");
    }
  }

  class PrecisionException extends RuntimeException {
    PrecisionException () {
      super("precision exception");
    }
  }
}



  
      
