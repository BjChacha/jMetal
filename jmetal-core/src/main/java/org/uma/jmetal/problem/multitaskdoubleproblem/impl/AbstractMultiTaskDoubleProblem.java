package org.uma.jmetal.problem.multitaskdoubleproblem.impl;

import org.uma.jmetal.problem.AbstractMultiTaskProblem;
import org.uma.jmetal.problem.doubleproblem.DoubleProblem;
import org.uma.jmetal.problem.multitaskdoubleproblem.MultiTaskDoubleProblem;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.solution.doublesolution.impl.DefaultDoubleSolution;
import org.uma.jmetal.solution.mfeadoublesolution.MFEADoubleSolution;
import org.uma.jmetal.solution.mfeadoublesolution.impl.DefaultMFEADoubleSolution;

public abstract class AbstractMultiTaskDoubleProblem
        extends AbstractMultiTaskProblem<DoubleSolution, MFEADoubleSolution> implements MultiTaskDoubleProblem {

    @Override
    public void evaluate(MFEADoubleSolution mfeaSolution) {
        DoubleSolution solution = transform(mfeaSolution);

        int skillFactor = mfeaSolution.getSkillFactor();
        getTaskList().get(skillFactor).evaluate(solution);

        resetting(mfeaSolution);
        mfeaSolution.setSolution(skillFactor, solution);
    }

    @Override
    public MFEADoubleSolution createSolution() {
        return new DefaultMFEADoubleSolution(getNumberOfVariables(), getNumberOfTasks());
    }

    public DoubleSolution transform(MFEADoubleSolution mfeaSolution) {
        int skillFactor = mfeaSolution.getSkillFactor();
        DoubleProblem problem = (DoubleProblem) getTask(skillFactor);
        DoubleSolution solution = new DefaultDoubleSolution(problem.getNumberOfObjectives(),
                problem.getNumberOfConstraints(), problem.getBoundsForVariables());

        for (int i = 0; i < problem.getNumberOfVariables(); ++i) {
            solution.variables().set(i,
                    mfeaSolution.variables().get(i)
                            * (problem.getBoundsForVariables().get(i).getUpperBound()
                                    - problem.getBoundsForVariables().get(i).getUpperBound())
                            + problem.getBoundsForVariables().get(i).getUpperBound());
        }

        return solution;
    }

    @Override
    public Double getLowerBound(int index) {
        return 0.0;
    }

    @Override
    public Double getUpperBound(int index) {
        return 1.0;
    }
}
