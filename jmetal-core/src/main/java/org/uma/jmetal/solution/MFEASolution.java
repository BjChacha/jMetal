package org.uma.jmetal.solution;

public interface MFEASolution<T, S> extends Solution<T> {

    void setSkillFactor(int skillFactor);

    int getSkillFactor();

    void setFactorialRank(int task, int rank);

    int getFactorialRank(int task);

    double getScalarFitness();

    S getSolution(int index);

    void setSolution(int index, S solution);
}
