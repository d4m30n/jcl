package nz.ac.waikato.orca.ml;

import weka.core.DenseInstance;

import java.util.ArrayList;

import weka.core.Attribute;

public interface ValuesInterface {
  DenseInstance GetInstance(int InstType) throws Exception;

  boolean CompareTo(ValuesInterface vInterface);

  void AddToValues(double[] values) throws Exception;
}