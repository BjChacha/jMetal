package org.uma.jmetal.problem.multitaskdoubleproblem;

import org.uma.jmetal.problem.MultiTaskProblem;
import org.uma.jmetal.solution.mfeadoublesolution.MFEADoubleSolution;

public interface MultiTaskDoubleProblem extends MultiTaskProblem<MFEADoubleSolution>{
    Double getLowerBound(int index);
    Double getUpperBound(int index);
}