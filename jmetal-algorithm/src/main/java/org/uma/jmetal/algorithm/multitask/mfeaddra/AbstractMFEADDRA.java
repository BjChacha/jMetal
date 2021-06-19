package org.uma.jmetal.algorithm.multitask.mfeaddra;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.algorithm.multiobjective.moead.util.*;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.moead.AbstractMOEAD;
import org.uma.jmetal.algorithm.multiobjective.moead.util.MOEADUtils;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.problem.MultiTaskProblem;
import org.uma.jmetal.solution.MFEASolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.point.impl.IdealPoint;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

public abstract class AbstractMFEADDRA<S extends MFEASolution<?, ? extends Solution<?>>> implements Algorithm<List<S>> {
    protected enum NEIGHTBOR_TYPE {
        NEIGHBOR, POPULATION
    }

    protected MultiTaskProblem<S> multiTaskProblem;
    protected List<S> population;
    protected int populationSize;

    protected int evaluations;
    protected int maxEvaluations;

    protected JMetalRandom randomGenerator;

    protected CrossoverOperator<S> crossoverOperator;
    protected MutationOperator<S> mutationOperator;
    protected AbstractMOEAD.FunctionType decompositionType;

    protected double beta;
    protected int nr;
    protected int T;
    protected double rmp;

    protected int subSize;
    protected NEIGHTBOR_TYPE neighborType;

    protected List<IdealPoint> idealPoints;
    protected double[][][] lambda;
    protected int[][][] neighborhood;

    public AbstractMFEADDRA(MultiTaskProblem<S> multiTaskProblem, int populationSize, int maxEvaluations,
            CrossoverOperator<S> crossover, MutationOperator<S> mutation, AbstractMOEAD.FunctionType functionType,
            double neighborhoodSelectionProbability, int maximumNumberOfReplacedSolutions, int neighborSize,
            double rmp) {
        this.multiTaskProblem = multiTaskProblem;
        this.populationSize = populationSize;
        this.maxEvaluations = maxEvaluations;
        this.crossoverOperator = crossover;
        this.mutationOperator = mutation;
        this.beta = neighborhoodSelectionProbability;
        this.nr = maximumNumberOfReplacedSolutions;
        this.T = neighborSize;
        this.rmp = rmp;

        this.subSize = populationSize / multiTaskProblem.getNumberOfTasks();

        this.randomGenerator = JMetalRandom.getInstance();
    }

    protected void initNeighborhood() {
        this.neighborhood = new int[multiTaskProblem.getNumberOfTasks()][subSize][T];

        for (int i = 0; i < multiTaskProblem.getNumberOfTasks(); ++i) {
            double[] x = new double[subSize];
            int[] idx = new int[subSize];

            for (int j = 0; j < subSize; ++j) {
                for (int k = 0; k < subSize; ++k) {
                    // TODO: 这里算邻域把自己都算进去了
                    x[k] = MOEADUtils.distVector(lambda[i][j], lambda[i][k]);
                    idx[k] = subSize * i + k;
                }

                MOEADUtils.minFastSort(x, idx, subSize, T);
                System.arraycopy(idx, 0, neighborhood[i][j], 0, T);
            }
        }
    }

    protected void initWeights() {
        // Step 1
        this.lambda = new double[multiTaskProblem.getNumberOfTasks()][][];
        for (int i = 0; i < multiTaskProblem.getNumberOfTasks(); ++i) {
            this.lambda[i] = new double[subSize][multiTaskProblem.getTask(i).getNumberOfObjectives()];
        }

        // Step 2
        for (int i = 0; i < multiTaskProblem.getNumberOfTasks(); ++i) {
            this.lambda[i] = UniformPoint.generateWeightVector(subSize,
                    multiTaskProblem.getTask(i).getNumberOfObjectives());
        }
    }

    protected void initIdealPoint(){
        idealPoints = new ArrayList<>(multiTaskProblem.getNumberOfTasks());
        for (int i = 0; i < multiTaskProblem.getNumberOfTasks(); ++i){
            idealPoints.add(new IdealPoint(multiTaskProblem.getTask(i).getNumberOfObjectives()));
        }

        for (int i = 0; i < populationSize; ++i){
            int skillFactor = population.get(i).getSkillFactor();
            idealPoints.get(skillFactor).update(population.get(i).getSolution(skillFactor).objectives());
        }
    }

    protected void updateIdealPoint(S child, int id){
        idealPoints.get(id).update(child.getSolution(id).objectives());
    }

    protected void chooseNeighborType() {
        double rnd = randomGenerator.nextDouble();
        if (rnd < this.beta) {
            this.neighborType = NEIGHTBOR_TYPE.NEIGHBOR;
        } else {
            this.neighborType = NEIGHTBOR_TYPE.POPULATION;
        }
    }

    protected double fitnessFunction(int id, S individual, double[] lambda) {
        double maxFun = -1.0e+30;

        for (int n = 0; n < individual.getSolution(id).objectives().length; n++) {
            double diff = Math.abs(individual.getSolution(id).objectives()[n] - idealPoints.get(id).getValue(n));

            double feval;
            if (lambda[n] == 0) {
                feval = diff / 0.000001;
            } else {
                feval = diff / lambda[n];
            }
            if (feval > maxFun) {
                maxFun = feval;
            }
        }

        return maxFun;
    }

    protected void updateNeighborhood(S child, int id, int subproblem) {
        int size;
        int time = 0;

        if (neighborType == NEIGHTBOR_TYPE.NEIGHBOR) {
            size = neighborhood[id][subproblem].length;
        } else {
            size = subSize;
        }
        int[] perm = new int[size];

        MOEADUtils.randomPermutation(perm, size);

        for (int i = 0; i < size; i++) {
            int k;
            if (neighborType == NEIGHTBOR_TYPE.NEIGHBOR) {
                k = neighborhood[id][subproblem][perm[i]];
            } else {
                k = id * subSize + perm[i];
            }
            double f1, f2;

            f1 = fitnessFunction(id, population.get(k), lambda[id][k % subSize]);
            f2 = fitnessFunction(id, child, lambda[id][k % subSize]);

            if (f2 < f1) {
                population.set(k, (S) child.copy());
                time++;
            }

            if (time >= nr) {
                return;
            }
        }
    }

    protected List<S> parentSelection(int skillFactor, int subproblem) {
        List<Integer> matingPool = matingSelection(skillFactor, subproblem, 2);

        List<S> parents = new ArrayList<>(3);

        parents.add(population.get(matingPool.get(0)));
        parents.add(population.get(matingPool.get(1)));
        parents.add(population.get(skillFactor * subSize + subproblem));

        return parents;
    }

    protected List<Integer> matingSelection(int skillFactor, int subproblem, int numberOfSolutionsToSelect) {
        int neighbourSize;
        int selectedSolution;

        List<Integer> listOfSolutions = new ArrayList<>(numberOfSolutionsToSelect);

        neighbourSize = neighborhood[skillFactor][subproblem].length;
        while (listOfSolutions.size() < numberOfSolutionsToSelect) {
            int random;
            if (neighborType == NEIGHTBOR_TYPE.NEIGHBOR) {
                random = randomGenerator.nextInt(0, neighbourSize - 1);
                selectedSolution = neighborhood[skillFactor][subproblem][random];
            } else {
                selectedSolution = subSize * skillFactor + randomGenerator.nextInt(0, subSize - 1);
            }
            boolean flag = true;

            if (skillFactor * subSize + subproblem == selectedSolution) {
                flag = false;
            }

            for (Integer individualId : listOfSolutions) {
                if (individualId == selectedSolution) {
                    flag = false;
                    break;
                }
            }

            if (flag) {
                listOfSolutions.add(selectedSolution);
            }
        }

        return listOfSolutions;
    }

    @Override
    public List<S> getResult() {
        return population;
    }
}
