package org.uma.jmetal.util.errorchecking;

import org.uma.jmetal.util.errorchecking.exception.*;

import java.util.Arrays;
import java.util.Collection;

/**
 * Static class for error checking
 */
public class Check {
  public static void notNull(Object object) {
    if (null == object) {
      throw new NullParameterException() ;
    }
  }

  public static void probabilityIsValid(double value) {
    if ((value < 0.0) || (value > 1.0)) {
      throw new InvalidProbabilityValueException(value) ;
    }
  }

  public static void valueIsInRange(double value, double lowestValue, double highestValue) {
    if ((value < lowestValue) || (value > highestValue)) {
      throw new ValueOutOfRangeException(value, lowestValue, highestValue) ;
    }
  }

  public static void valueIsInRange(int value, int lowestValue, int highestValue) {
    if ((value < lowestValue) || (value > highestValue)) {
      throw new ValueOutOfRangeException(value, lowestValue, highestValue) ;
    }
  }

  public static void collectionIsNotEmpty(Collection<?> collection) {
    if (collection.isEmpty()) {
      throw new EmptyCollectionException() ;
    }
  }

  public static void that(boolean expression, String message) {
    if (!expression) {
        throw new InvalidConditionException(message) ;
    }
  }

  public static void notAllZero(double[] vector){
    if (Arrays.stream(vector).sum() == 0){
        throw new IllegalArgumentException("Error: vector is full of 0s.");
    }
  }

  public static void notNaN(double[] vector){
    if (Double.isNaN(Arrays.stream(vector).sum())){
        throw new IllegalArgumentException("Error: vector contains NaN.");
    }
  }
}
