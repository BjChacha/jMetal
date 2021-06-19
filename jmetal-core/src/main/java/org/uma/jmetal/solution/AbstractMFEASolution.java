package org.uma.jmetal.solution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.uma.jmetal.util.pseudorandom.JMetalRandom;

public abstract class AbstractMFEASolution<T, S extends Solution<?>> implements MFEASolution<T, S> {
    public enum ATTRIBUTE_NAME {
        SKILL_FACTOR, FACTORIAL_RANK, SCALAR_FITNESS
    };

    private double[] objectives;
    private List<T> variables;
    private double[] constrains;

    protected List<S> solutionOfTask;
    protected final JMetalRandom randomGenerator;

    protected Map<Object, Object> attributes;

    protected AbstractMFEASolution(int numberOfObjectives, int numberOfTasks) {
        this.attributes = new HashMap<>();
        this.randomGenerator = JMetalRandom.getInstance();

        variables = new ArrayList<>(numberOfObjectives);
        for (int i = 0; i < numberOfObjectives; ++i) {
            variables.add(null);
        }

        solutionOfTask = new ArrayList<>(numberOfTasks);
        for (int i = 0; i < numberOfTasks; ++i) {
            solutionOfTask.add(null);
        }
    }

    @Override
    public List<T> variables() {
        return variables;
    }

    @Override
    public double[] objectives() {
        return objectives;
    }

    @Override
    public double[] constraints() {
        return constrains;
    }

    @Override
    public Map<Object, Object> attributes() {
        return attributes;
    }

    @Override
    public void setSkillFactor(int skillFactor) {
        this.attributes.put(ATTRIBUTE_NAME.SKILL_FACTOR, skillFactor);
    }

    @Override
    public int getSkillFactor() {
        return (int) this.attributes.get(ATTRIBUTE_NAME.SKILL_FACTOR);
    }

    @Override
    public void setFactorialRank(int task, int rank) {
        this.solutionOfTask.get(task).attributes().put(ATTRIBUTE_NAME.FACTORIAL_RANK, rank);
    }

    @Override
    public int getFactorialRank(int task) {
        return (int) getSolution(getSkillFactor()).attributes().get(ATTRIBUTE_NAME.FACTORIAL_RANK);
    }

    @Override
    public double getScalarFitness() {
        return 1.0 / (getFactorialRank(getSkillFactor()) + 1);
    }

    @Override
    public S getSolution(int index) {
        return this.solutionOfTask.get(index);
    }

    @Override
    public void setSolution(int index, S solution) {
        this.solutionOfTask.set(index, solution);
    }
}
