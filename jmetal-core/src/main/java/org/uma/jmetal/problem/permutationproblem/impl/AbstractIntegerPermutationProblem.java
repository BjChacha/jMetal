package org.uma.jmetal.problem.permutationproblem.impl;

import org.uma.jmetal.problem.impl.AbstractGenericProblem;
import org.uma.jmetal.problem.permutationproblem.PermutationProblem;
import org.uma.jmetal.solution.permutationsolution.PermutationSolution;
import org.uma.jmetal.solution.permutationsolution.integerpermutation.IntegerPermutationSolution;

@SuppressWarnings("serial")
public abstract class AbstractIntegerPermutationProblem
    extends AbstractGenericProblem<PermutationSolution<Integer>> implements
    PermutationProblem<PermutationSolution<Integer>> {

  /* Getters */

  /* Setters */

  @Override
  public PermutationSolution<Integer> createSolution() {
    return new IntegerPermutationSolution(getNumberOfVariables(), getPermutationLength()) ;
  }
}