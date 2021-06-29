package org.uma.jmetal.algorithm.multitask.mfeaddra;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.optim.MaxEval;
import org.netlib.util.booleanW;
import org.uma.jmetal.algorithm.multiobjective.moead.AbstractMOEAD;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.problem.MultiTaskProblem;
import org.uma.jmetal.solution.MFEASolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;

public class MFEADDRA<S extends MFEASolution<?, ? extends Solution<?>>> extends AbstractMFEADDRA<S> {
    protected List<S> savedValues;
    protected int rate;

    private final String UTILITY = "utility";
    private int generation;

    public MFEADDRA(MultiTaskProblem<S> multiTaskProblem,
                    int populationSize,
                    int maxEvaluations,
                    CrossoverOperator<S> crossover,
                    MutationOperator<S> mutation,
                    AbstractMOEAD.FunctionType functionType,
                    double neighborhoodSelectionProbability,
                    int maximumNumberOfReplacedSolutions,
                    int neighborSize,
                    double rmp,
                    int rate) {
        super(multiTaskProblem, populationSize, maxEvaluations, crossover, mutation, functionType, neighborhoodSelectionProbability, maximumNumberOfReplacedSolutions, neighborSize, rmp);

        this.rate = rate;
        this.savedValues = new ArrayList<>(populationSize);
    }

    public MFEADDRA(MultiTaskProblem<S> multiTaskProblem,
                    int populationSize,
                    int maxEvaluations,
                    CrossoverOperator<S> crossover,
                    MutationOperator<S> mutation,
                    AbstractMOEAD.FunctionType functionType,
                    double neighborhoodSelectionProbability,
                    int maximumNumberOfReplacedSolutions,
                    int neighborSize,
                    double rmp) {
        this(multiTaskProblem,
                populationSize,
                maxEvaluations,
                crossover,
                mutation,
                functionType,
                neighborhoodSelectionProbability,
                maximumNumberOfReplacedSolutions,
                neighborSize,
                rmp,
                30);
    }


    // @Override
    // public void run() {
    //     initWeights();
    //     initNeighborhood();

    //     initPopulation();
    //     initIdealPoint();

    //     generation = 0;

    //     do {
    //         List<Integer> order = tourSelection(10);

    //         for (int i = 0; i < order.size(); i++) {
    //             int subProblemId = order.get(i);

    //             int skillFactor = population.get(subProblemId).getSkillFactor();
    //             int subproblem = subProblemId % subSize;

    //             chooseNeighborType();
    //             List<S> parents = parentSelection(skillFactor, subproblem);

    //             ((DifferentialEvolutionCrossover) (CrossoverOperator<?>) crossoverOperator).setCurrentSolution((DoubleSolution) population.get(subProblemId));
    //             List<S> children = crossoverOperator.execute(parents);
    //             S child = children.get(0);
    //             mutationOperator.execute(child);

    //             int id = chooseTask(skillFactor);
    //             child.setSkillFactor(id);

    //             multiTaskProblem.evaluate(child);

    //             evaluations++;

    //             updateIdealPoint(child, id);

    //             updateNeighborhood(child, id, subproblem);
    //         }

    //         generation++;
    //         if (rate != 0 && generation % rate == 0) {
    //             updateUtility();
    //         }

    //     } while (evaluations < maxEvaluations);
    // }

    private void updateUtility() {
        double f1, f2, uti, delta;
        for (int n = 0; n < populationSize; n++) {
            int skillFactor = population.get(n).getSkillFactor();
            int subproblem = n % subSize;

            f1 = fitnessFunction(skillFactor, population.get(n), lambda[skillFactor][subproblem]);
            f2 = fitnessFunction(skillFactor, (S) savedValues.get(n), lambda[skillFactor][subproblem]);

            // delta = f2 - f1;
            delta = (f2 - f1) / f2;


            if (delta > 0.001) {
                population.get(n).attributes().put(UTILITY, 1.0);
            } else {
//                uti = (0.95 + (0.05 * delta / 0.001)) * utility[n];
                uti = (0.95 + (0.05 * delta / 0.001)) * ((double) population.get(n).attributes().get(UTILITY));
                population.get(n).attributes().put(UTILITY, uti < 1.0 ? uti : 1.0);
            }
            savedValues.set(n, (S) population.get(n).copy());
        }
    }

    private List<Integer> tourSelection(int depth) {
        List<Integer> selected = new ArrayList<Integer>();
        List<Integer> candidate = new ArrayList<Integer>();

//        for (int k = 0; k < problem.getNumberOfObjectives(); k++) {
//            // WARNING! HERE YOU HAVE TO USE THE WEIGHT PROVIDED BY QINGFU Et AL (NOT
//            // SORTED!!!!)
//            selected.add(k);
//        }
//
//        for (int n = problem.getNumberOfObjectives(); n < populationSize; n++) {
//            // set of unselected weights
//            candidate.add(n);
//        }

        for (int n = 0; n < populationSize; n++) {
            candidate.add(n);
        }

        while (selected.size() < (int) (populationSize / 5.0)) {
            int best_idd = (int) (randomGenerator.nextDouble() * candidate.size());
            int i2;
            int best_sub = candidate.get(best_idd);
            int s2;
            for (int i = 1; i < depth; i++) {
                i2 = (int) (randomGenerator.nextDouble() * candidate.size());
                s2 = candidate.get(i2);
                if ((double) population.get(s2).attributes().get(UTILITY) >
                        (double) population.get(best_idd).attributes().get(UTILITY)) {
                    best_idd = i2;
                    best_sub = s2;
                }
            }
            selected.add(best_sub);
            candidate.remove(best_idd);
        }
        return selected;
    }

    private int chooseTask(int skillFactor) {
        double rd = randomGenerator.nextDouble();

        if (rd < rmp) {
            // redefine neighborType
            neighborType = NEIGHTBOR_TYPE.POPULATION;

            int randTask = randomGenerator.nextInt(0, multiTaskProblem.getNumberOfTasks() - 1);
            while (randTask == skillFactor) {
                randTask = randomGenerator.nextInt(0, multiTaskProblem.getNumberOfTasks() - 1);
            }

            return randTask;
        } else {
            return skillFactor;
        }
    }

    protected void initPopulation() {
        this.population = new ArrayList<>(populationSize);
        for (int i = 0; i < populationSize; i++) {
            int id = i / subSize;
            S solution = multiTaskProblem.createSolution();
            solution.setSkillFactor(id);

            solution.attributes().put(UTILITY, 1.0);

            multiTaskProblem.evaluate(solution);
            evaluations++;
            population.add(solution);

            savedValues.add((S) solution.copy());
        }
    }

    @Override
    public String getName() {
        return "MFEAD-DRA";
    }

    @Override
    public String getDescription() {
        return "A Multiobjective multifactorial optimization algorithm based on decomposition and dynamic resource allocation strategy.";
    }

    @Override
    protected void initState() {
        initWeights();
        initNeighborhood();
        initPopulation();
        initIdealPoint();

        this.generation = 0;
        this.evaluations = 0;
    }

    @Override
    protected void iteration() {
        List<Integer> order = tourSelection(10);
        for (int i = 0; i < order.size(); i++) {
            int subProblemId = order.get(i);

            int skillFactor = population.get(subProblemId).getSkillFactor();
            int subproblem = subProblemId % subSize;

            chooseNeighborType();
            List<S> parents = parentSelection(skillFactor, subproblem);

            ((DifferentialEvolutionCrossover) (CrossoverOperator<?>) crossoverOperator).setCurrentSolution((DoubleSolution) population.get(subProblemId));
            List<S> children = crossoverOperator.execute(parents);
            S child = children.get(0);
            mutationOperator.execute(child);

            int id = chooseTask(skillFactor);
            child.setSkillFactor(id);
            multiTaskProblem.evaluate(child);

            this.evaluations++;
            updateIdealPoint(child, id);
            updateNeighborhood(child, id, subproblem);
        }

        generation++;
        if (rate != 0 && generation % rate == 0) {
            updateUtility();
        }
    }

    @Override
    protected void initProgress() { }

    @Override
    protected void updateProgress() {}

    @Override
    protected boolean isStoppingConditionReached() {
        return evaluations >= maxEvaluations;
    }

    protected boolean isPeriodicUpdate(int T){
        return T != 0 && generation % T == 0;
    }

    @Override
    public void run(){
        initState();
        initProgress();
        while (!isStoppingConditionReached()){
            iteration();
            if (isPeriodicUpdate(20)) {
                updateProgress();
            }
        }
    }
}
