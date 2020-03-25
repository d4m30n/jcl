package nz.ac.waikato.orca.ml;

import java.util.ArrayList;

import nz.ac.waikato.orca.ModelLQR;
import weka.core.Attribute;
import weka.core.DenseInstance;

public class Values implements ValuesInterface {

  public static final int NumberOfAttributes = 6;
  public static final int CPUType = 0;
  public static final int MemoryType = 1;
  private static final int HASH = 1;
  private static final int SLEEP = HASH + 1;
  private static final int BUTTONS = SLEEP + 1;
  private static final int DEPTH = BUTTONS + 1;
  private static final int BREADTH = DEPTH + 1;

  private double CPU, Memory;
  public final int Hash, Sleep, Buttons, Depth, Breadth;
  private int TotalValues;

  public Values(double CPU, double Memory, int Hash, int Sleep, int Buttons, int Depth, int Breadth) {
    this.CPU = CPU;
    this.Memory = Memory;
    this.Hash = Hash;
    this.Sleep = Sleep;
    this.Buttons = Buttons;
    this.Depth = Depth;
    this.Breadth = Breadth;
    TotalValues = 1;
  }

  @Override
  public DenseInstance GetInstance(int InstType) throws Exception {
    if (InstType != CPUType && InstType != MemoryType)
      throw new Exception("Invald Type given Can only pass 0 - CPU or 1 - Memory");
    DenseInstance Inst = new DenseInstance(NumberOfAttributes);
    if (InstType == CPUType)
      Inst.setValue(0, ModelLQR.encodeMeasurement(CPU, ModelLQR.CPU));
    else if (InstType == MemoryType)
      Inst.setValue(0, ModelLQR.encodeMeasurement(Memory, ModelLQR.MEMORY));
    Inst.setValue(HASH, ModelLQR.encodeParameter(Hash));
    Inst.setValue(SLEEP, ModelLQR.encodeParameter(Sleep));
    Inst.setValue(BUTTONS, ModelLQR.encodeParameter(Buttons));
    Inst.setValue(DEPTH, ModelLQR.encodeParameter(Depth));
    Inst.setValue(BREADTH, ModelLQR.encodeParameter(Breadth));

    return Inst;
  }

  @Override
  public boolean CompareTo(ValuesInterface vInterface) {
    if (!(vInterface instanceof Values))
      return false;
    Values ValuesClass = (Values) vInterface;
    if (this.Hash != ValuesClass.Hash)
      return false;
    else if (this.Sleep != ValuesClass.Sleep)
      return false;
    else if (this.Buttons != ValuesClass.Buttons)
      return false;
    else if (this.Depth != ValuesClass.Depth)
      return false;
    else if (this.Breadth != ValuesClass.Breadth)
      return false;
    return true;
  }

  @Override
  public void AddToValues(double[] values) throws Exception {
    if (values.length > 2 || values.length < 2)
      throw new Exception("Invalid number of values plese have 2 values with values[0] = CPU and values[1] = Memory");
    double CPU = values[0];
    double Memory = values[1];
    this.CPU = MovingAdv(CPU, this.CPU, TotalValues);
    this.Memory = MovingAdv(Memory, this.Memory, TotalValues);
    TotalValues++;
  }

  private double MovingAdv(double NewValue, double OldValue, int NumberOfValues) {
    double NewAdv = OldValue + ((NewValue - OldValue) / NumberOfValues + 1);
    return NewAdv;
  }

  public static ArrayList<Attribute> GetArrtibutesList(int InstType) {
    ArrayList<Attribute> attributes = new ArrayList<>();
    if (InstType == CPUType)
      attributes.add(new Attribute("CPU"));
    else if (InstType == MemoryType)
      attributes.add(new Attribute("Memory"));
    attributes.add(new Attribute("Hash"));
    attributes.add(new Attribute("Sleep"));
    attributes.add(new Attribute("Buttons"));
    attributes.add(new Attribute("Depth"));
    attributes.add(new Attribute("Breadth"));
    return attributes;
  }

}