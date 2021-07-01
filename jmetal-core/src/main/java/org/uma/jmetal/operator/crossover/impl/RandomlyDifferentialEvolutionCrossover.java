package org.uma.jmetal.operator.crossover.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.solution.util.repairsolution.RepairDoubleSolution;
import org.uma.jmetal.solution.util.repairsolution.impl.RepairDoubleSolutionWithBoundValue;
import org.uma.jmetal.util.bounds.Bounds;
import org.uma.jmetal.util.errorchecking.Check;
import org.uma.jmetal.util.errorchecking.JMetalException;
import org.uma.jmetal.util.pseudorandom.BoundedRandomGenerator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

/**
 * Differential evolution crossover operator
 *
 * @author Antonio J. Nebro
 *     <p>Comments: - The operator receives two parameters: the current individual and an array of
 *     three parent individuals - The best and rand variants depends on the third parent, according
 *     whether it represents the current of the "best" individual or a random one. The
 *     implementation of both variants are the same, due to that the parent selection is external to
 *     the crossover operator. - Implemented variants: - rand/1/bin (best/1/bin) - rand/1/exp
 *     (best/1/exp) - current-to-rand/1 (current-to-best/1) - current-to-rand/1/bin
 *     (current-to-best/1/bin) - current-to-rand/1/exp (current-to-best/1/exp)
 */
@SuppressWarnings("serial")
public class RandomlyDifferentialEvolutionCrossover implements CrossoverOperator<DoubleSolution> {
  public enum DE_VARIANT {
    RAND_1_BIN,
    RAND_1_EXP,
    RAND_2_BIN,
    RAND_2_EXP,
    BEST_1_BIN,
    BEST_1_EXP,
    BEST_2_BIN,
    BEST_2_EXP,
    RAND_TO_BEST_1_BIN,
    RAND_TO_BEST_1_EXP,
    CURRENT_TO_RAND_1_BIN,
    CURRENT_TO_RAND_1_EXP
  }

  public enum DE_CROSSOVER_TYPE {
    BIN,
    EXP
  }

  public enum DE_MUTATION_TYPE {
    RAND,
    BEST,
    RAND_TO_BEST,
    CURRENT_TO_RAND
  }

  private static final DE_VARIANT DEFAULT_DE_VARIANT = DE_VARIANT.RAND_1_BIN;

  private static final double DEFAULT_CR_LB = 0.1;
  private static final double DEFAULT_CR_UB = 0.9;
  private static final double DEFAULT_F_LB = 0.1;
  private static final double DEFAULT_F_UB = 2.0;

  private double cr_lb;
  private double cr_ub;
  private double f_lb;
  private double f_ub;

  private int numberOfDifferenceVectors = 1;
  private DE_CROSSOVER_TYPE crossoverType = DE_CROSSOVER_TYPE.BIN;
  private DE_MUTATION_TYPE mutationType = DE_MUTATION_TYPE.RAND;

  private DE_VARIANT variant;

  private DoubleSolution currentSolution = null;
  private DoubleSolution bestSolution = null;

  private BoundedRandomGenerator<Integer> jRandomGenerator;
  private BoundedRandomGenerator<Double> crRandomGenerator;

  private RepairDoubleSolution solutionRepair;

  private JMetalRandom randomGenerator = JMetalRandom.getInstance();

  /** Constructor */
  public RandomlyDifferentialEvolutionCrossover() {
    this(DEFAULT_CR_LB, DEFAULT_CR_UB, DEFAULT_F_LB, DEFAULT_F_UB, DEFAULT_DE_VARIANT);
  }

  /**
   * Constructor
   *
   * @param cr
   * @param f
   * @param variant
   */
  public RandomlyDifferentialEvolutionCrossover(double cr_lb, double cr_up, double f_lb, double f_up, DE_VARIANT variant) {
    this(
        cr_lb,
        cr_up,
        f_lb,
        f_up,
        variant,
        (a, b) -> JMetalRandom.getInstance().nextInt(a, b),
        (a, b) -> JMetalRandom.getInstance().nextDouble(a, b));
  }

  /**
   * Constructor
   *
   * @param cr
   * @param f
   * @param variant
   * @param randomGenerator
   */
  public RandomlyDifferentialEvolutionCrossover(
      double cr_lb, double cr_ub, double f_lb, double f_ub, DE_VARIANT variant, RandomGenerator<Double> randomGenerator) {
    this(
        cr_lb,
        cr_ub,
        f_lb,
        f_ub,
        variant,
        BoundedRandomGenerator.fromDoubleToInteger(randomGenerator),
        BoundedRandomGenerator.bound(randomGenerator));
  }

  /**
   * Constructor
   *
   * @param cr
   * @param f
   * @param variant
   * @param jRandomGenerator
   * @param crRandomGenerator
   */
  public RandomlyDifferentialEvolutionCrossover(
      double cr_lb,
      double cr_ub,
      double f_lb,
      double f_ub,
      DE_VARIANT variant,
      BoundedRandomGenerator<Integer> jRandomGenerator,
      BoundedRandomGenerator<Double> crRandomGenerator) {
    this.cr_lb = cr_lb;
    this.cr_ub = cr_ub;
    this.f_lb = f_lb;
    this.f_ub = f_ub;
    this.variant = variant;

    analyzeVariant(variant);

    this.jRandomGenerator = jRandomGenerator;
    this.crRandomGenerator = crRandomGenerator;

    solutionRepair = new RepairDoubleSolutionWithBoundValue();
  }

  private void analyzeVariant(DE_VARIANT variant) {
    switch (variant) {
      case RAND_1_BIN:
      case RAND_1_EXP:
      case BEST_1_BIN:
      case BEST_1_EXP:
      case RAND_TO_BEST_1_BIN:
      case RAND_TO_BEST_1_EXP:
      case CURRENT_TO_RAND_1_BIN:
      case CURRENT_TO_RAND_1_EXP:
        numberOfDifferenceVectors = 1;
        break;
      case RAND_2_BIN:
      case RAND_2_EXP:
      case BEST_2_BIN:
      case BEST_2_EXP:
        numberOfDifferenceVectors = 2;
        break;
      default:
        throw new JMetalException("DE variant type invalid: " + variant);
    }

    switch (variant) {
      case RAND_1_BIN:
      case BEST_1_BIN:
      case RAND_TO_BEST_1_BIN:
      case CURRENT_TO_RAND_1_BIN:
      case RAND_2_BIN:
      case BEST_2_BIN:
        crossoverType = DE_CROSSOVER_TYPE.BIN;
        break;
      case RAND_1_EXP:
      case BEST_1_EXP:
      case RAND_TO_BEST_1_EXP:
      case CURRENT_TO_RAND_1_EXP:
      case RAND_2_EXP:
      case BEST_2_EXP:
        crossoverType = DE_CROSSOVER_TYPE.EXP;
        break;
      default:
        throw new JMetalException("DE crossover type invalid: " + variant);
    }

    switch (variant) {
      case RAND_1_BIN:
      case RAND_1_EXP:
      case RAND_2_BIN:
      case RAND_2_EXP:
        mutationType = DE_MUTATION_TYPE.RAND;
        break;
      case BEST_1_BIN:
      case BEST_1_EXP:
      case BEST_2_BIN:
      case BEST_2_EXP:
        mutationType = DE_MUTATION_TYPE.BEST;
        break;
      case CURRENT_TO_RAND_1_BIN:
      case CURRENT_TO_RAND_1_EXP:
        mutationType = DE_MUTATION_TYPE.CURRENT_TO_RAND;
        break;
      case RAND_TO_BEST_1_BIN:
      case RAND_TO_BEST_1_EXP:
        mutationType = DE_MUTATION_TYPE.RAND_TO_BEST;
        break;
      default:
        throw new JMetalException("DE mutation type invalid: " + variant);
    }
  }

  /* Getters */
  public double getCr() {
    return randomGenerator.nextDouble(cr_lb, cr_ub);
  }

  public double getF() {
    return randomGenerator.nextDouble(f_lb, f_ub);
  }

  public DE_VARIANT getVariant() {
    return variant;
  }

  public int getNumberOfDifferenceVectors() {
    return numberOfDifferenceVectors;
  }

  public DE_CROSSOVER_TYPE getCrossoverType() {
    return crossoverType;
  }

  public DE_MUTATION_TYPE getMutationType() {
    return mutationType;
  }

  public int getNumberOfRequiredParents() {
    return 1 + numberOfDifferenceVectors * 2;
  }

  public int getNumberOfGeneratedChildren() {
    return 1;
  }

  public double getCrossoverProbability() {
    return 1.0;
  }

  /* Setters */
  public void setCurrentSolution(DoubleSolution current) {
    this.currentSolution = current;
  }

  public void setBestSolution(DoubleSolution bestSolution) {
    this.bestSolution = bestSolution;
  }

  public void setCr_LB(double cr_lb) {
    this.cr_lb = cr_lb;
  }

  public void setCr_UB(double cr_ub) {
    this.cr_ub = cr_ub;
  }

  public void setF_LB(double f_lb) {
    this.f_lb = f_lb;
  }

  public void setF_UB(double f_ub) {
    this.f_ub = f_ub;
  }

  /** Execute() method */
  @Override
  public List<DoubleSolution> execute(List<DoubleSolution> parentSolutions) {
    double cr = getCr();
    
    DoubleSolution child = (DoubleSolution) currentSolution.copy();

    int numberOfVariables = parentSolutions.get(0).variables().size();
    int jrand = jRandomGenerator.getRandomValue(0, numberOfVariables - 1);

    Double[][] parent = new Double[getNumberOfRequiredParents()][];

    IntStream.range(0, getNumberOfRequiredParents())
        .forEach(
            i -> {
              parent[i] = new Double[numberOfVariables];
              parentSolutions.get(i).variables().toArray(parent[i]);
            });

    if (crossoverType.equals(DE_CROSSOVER_TYPE.BIN)) {
      for (int j = 0; j < numberOfVariables; j++) {
        if (crRandomGenerator.getRandomValue(0.0, 1.0) < cr || j == jrand) {
          double value = mutate(parent, j);

          child.variables().set(j, value);
        }
      }
    } else if (crossoverType.equals(DE_CROSSOVER_TYPE.EXP)) {
      int j = jRandomGenerator.getRandomValue(0, numberOfVariables - 1);
      int l = 0;

      do {
        double value = mutate(parent, j);

        child.variables().set(j, value);

        j = (j + 1) % numberOfVariables;
        l++;
      } while ((crRandomGenerator.getRandomValue(0.0, 1.0) < cr) && (l < numberOfVariables));
    }

    repairVariableValues(child);

    List<DoubleSolution> result = new ArrayList<>(1);
    result.add(child);
    return result;
  }

  private void repairVariableValues(DoubleSolution solution) {
    IntStream.range(0, solution.variables().size())
        .forEach(
            i -> {
              Bounds<Double> bounds = solution.getBounds(i);
              solution.variables().set(
                  i,
                  solutionRepair.repairSolutionVariableValue(
                      solution.variables().get(i), bounds.getLowerBound(), bounds.getUpperBound()));
            });
  }

  private double mutate(Double[][] parent, int index) {
    double value = 0;
    if (mutationType.equals(DE_MUTATION_TYPE.RAND)) {
      value = randMutation(parent, index, numberOfDifferenceVectors);
    } else if (mutationType.equals(DE_MUTATION_TYPE.BEST)) {
      value = bestMutation(parent, index, numberOfDifferenceVectors);
    } else if (mutationType.equals(DE_MUTATION_TYPE.RAND_TO_BEST)) {
      value = bestRandToBestMutation(parent, index);
    }

    return value;
  }

  private double randMutation(Double[][] parent, int index, int numberOfDifferenceVectors) {
    double f = getF();

    if (numberOfDifferenceVectors == 1) {
      return parent[2][index] + f * (parent[0][index] - parent[1][index]);
    } else if (numberOfDifferenceVectors == 2) {
      return parent[4][index]
          + f * (parent[0][index] - parent[1][index])
          + f * (parent[2][index] - parent[3][index]);
    } else {
      throw new JMetalException(
          "Number of difference vectors invalid: " + numberOfDifferenceVectors);
    }
  }

  private double bestMutation(Double[][] parent, int index, int numberOfDifferenceVectors) {
    Check.notNull(bestSolution);

    double f = getF();

    if (numberOfDifferenceVectors == 1) {
      return bestSolution.variables().get(index) + f * (parent[0][index] - parent[1][index]);
    } else if (numberOfDifferenceVectors == 2) {
      return bestSolution.variables().get(index)
          + f * (parent[0][index] - parent[1][index])
          + f * (parent[2][index] - parent[3][index]);
    } else {
      throw new JMetalException(
          "Number of difference vectors invalid: " + numberOfDifferenceVectors);
    }
  }

  private double bestRandToBestMutation(Double[][] parent, int index) {
    Check.notNull(bestSolution);
    Check.notNull(currentSolution);
    double f = getF();
    return currentSolution.variables().get(index)
        + f * (bestSolution.variables().get(index) - currentSolution.variables().get(index))
        + f * (parent[0][index] - parent[1][index]);
  }

  public static DE_VARIANT getVariantFromString(String variant) {
    DE_VARIANT deVariant;
    switch (variant) {
      case "RAND_1_BIN":
        deVariant = DE_VARIANT.RAND_1_BIN;
        break;
      case "RAND_2_BIN":
        deVariant = DE_VARIANT.RAND_2_BIN;
        break;
      case "BEST_1_BIN":
        deVariant = DE_VARIANT.BEST_1_BIN;
        break;
      case "BEST_1_EXP":
        deVariant = DE_VARIANT.BEST_1_EXP;
        break;
      case "BEST_2_BIN":
        deVariant = DE_VARIANT.BEST_2_BIN;
        break;
      case "BEST_2_EXP":
        deVariant = DE_VARIANT.BEST_2_EXP;
        break;
      case "RAND_TO_BEST_1_BIN":
        deVariant = DE_VARIANT.RAND_TO_BEST_1_BIN;
        break;
      case "RAND_TO_BEST_1_EXP":
        deVariant = DE_VARIANT.RAND_TO_BEST_1_EXP;
        break;
      case "CURRENT_TO_RAND_1_BIN":
        deVariant = DE_VARIANT.CURRENT_TO_RAND_1_BIN;
        break;
      case "CURRENT_TO_RAND_1_EXP":
        deVariant = DE_VARIANT.CURRENT_TO_RAND_1_EXP;
        break;
      default:
        throw new JMetalException("Invalid differential evolution variant: " + variant);
    }
    return deVariant;
  }
}
