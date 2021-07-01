package org.uma.jmetal.solution.mfeadoublesolution.impl;

import java.util.HashMap;

import org.uma.jmetal.solution.AbstractMFEASolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.solution.mfeadoublesolution.MFEADoubleSolution;
import org.uma.jmetal.util.errorchecking.Check;

public class DefaultMFEADoubleSolution extends AbstractMFEASolution<Double, DoubleSolution> implements MFEADoubleSolution{

    public DefaultMFEADoubleSolution(int numberOfObjectives, int numberOfTasks) {
        super(numberOfObjectives, numberOfTasks);

        for (int i = 0; i < numberOfObjectives; ++i){
            this.variables().set(i, randomGenerator.nextDouble());
        }
        this.setSkillFactor(-1);
    }

    public DefaultMFEADoubleSolution(DefaultMFEADoubleSolution solution) {
        super(solution.variables().size(), solution.solutionOfTask.size());

        this.setSkillFactor(solution.getSkillFactor());

        if (solution.attributes != null){
            this.attributes = new HashMap<>(solution.attributes);
        }

        for (int i = 0; i < solution.variables().size(); i++){
            this.variables().set(i, solution.variables().get(i));
        }

        for (int i = 0; i < solution.solutionOfTask.size(); i++){
            if (solution.getSolution(i) != null)
                this.solutionOfTask.set(i, (DoubleSolution) solution.getSolution(i).copy());
        }
    }

    @Override
    public Solution<Double> copy() {
        return new DefaultMFEADoubleSolution(this);
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
