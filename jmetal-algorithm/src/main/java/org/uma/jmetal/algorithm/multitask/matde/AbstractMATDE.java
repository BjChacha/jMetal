package org.uma.jmetal.algorithm.multitask.matde;

import java.util.Comparator;
import java.util.List;

import org.uma.jmetal.algorithm.impl.AbstractMultiPopulationEvolutionaryAlgorithm;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.problem.MultiTaskProblem;
import org.uma.jmetal.solution.MFEASolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

public abstract class AbstractMATDE<S extends MFEASolution<?, ? extends Solution<?>>> extends AbstractMultiPopulationEvolutionaryAlgorithm<S, List<List<S>>> {
    protected int taskNum;
    protected int archiveSize;
    protected List<List<S>> archive;

    protected double alpha;
    protected double ro;
    protected double shrinkRate;
    protected double replaceRate;

    protected double[][] probability;
    protected double[][] reward;

    // the default crossover is used for normally reproducing,
    // crossover2 is used for transfer
    protected CrossoverOperator<S> crossoverOperator2;
    protected Comparator<S> comparator;

    public AbstractMATDE(MultiTaskProblem<S> problem, int populationSize, int archiveSize, int maxEvaluations, CrossoverOperator<S> crossover1, CrossoverOperator<S> crossover2, double alpha, double ro, double shrinkRate, double replacement){
        this.problem = problem;
        this.taskNum = problem.getNumberOfTasks();
        this.populationSize = populationSize;
        this.archiveSize = archiveSize;
        this.maxEvaluations = maxEvaluations;
        this.crossoverOperator = crossover1;
        this.crossoverOperator2 = crossover2;
        this.alpha = alpha;
        this.ro = ro;
        this.shrinkRate = shrinkRate;
        this.replaceRate = replacement;

        probability = new double[taskNum][taskNum];
        reward = new double[taskNum][taskNum];
        this.randomGenerator = JMetalRandom.getInstance();
    }
}
