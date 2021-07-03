package org.uma.jmetal.algorithm.impl;

import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.problem.MultiTaskProblem;
import org.uma.jmetal.util.errorchecking.Check;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

public abstract class AbstractMultiPopulationEvolutionaryAlgorithm<S, R> implements Algorithm<R> {
    protected List<List<S>> population;
    protected MultiTaskProblem<S> problem;

    protected int populationSize;
    protected int taskNum;

    protected int evaluations;
    protected int maxEvaluations;

    protected JMetalRandom randomGenerator;

    protected CrossoverOperator<S> crossoverOperator;
    protected MutationOperator<S> mutationOperator;
    protected SelectionOperator<List<S>, S> selectionOperator;

    public List<List<S>> getPopulation(){
        return population;
    } 

    public List<S> getSubPopulation(int taskId){
        checkTaskId(taskId);
        return population.get(taskId);
    }

    public void setPopulation(List<List<S>> population){
        this.population = population;
    }

    public void setSubPopulation(int taskId, List<S> subPopulation){
        checkTaskId(taskId);
        this.population.set(taskId, subPopulation);
    }

    public void setProblem(MultiTaskProblem<S> problem){
        this.problem = problem;
    }

    public MultiTaskProblem<S> getProblem(){
        return this.problem;
    }

    @Override
    public void run(){
        initState();
        initProgress();
        while (!isStoppingConditionReached()){
            iteration();
            if (isUpdateConditionReached())
            updateProgress();
        }
    }

    protected abstract void initProgress();
    protected abstract void updateProgress();
    protected abstract void initState();
    protected abstract void iteration();
    protected abstract boolean isStoppingConditionReached();
    protected abstract boolean isUpdateConditionReached();

    private void checkTaskId(int taskId){
        Check.that(taskId >= 0 && taskId < taskNum, "Error: task id should be in [0, " + (taskNum-1) + "], but it is " + taskId + " .");
    }
}
