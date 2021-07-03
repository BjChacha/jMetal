package org.uma.jmetal.algorithm.multitask.matde;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.uma.jmetal.algorithm.multitask.matde.util.KLD;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.RandomlyDifferentialEvolutionCrossover;
import org.uma.jmetal.operator.selection.impl.RankingAndCrowdingSelection;
import org.uma.jmetal.problem.MultiTaskProblem;
import org.uma.jmetal.solution.MFEASolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;

public class MATDE<S extends MFEASolution<?, ? extends Solution<?>>> extends AbstractMATDE<S>{
    public MATDE(MultiTaskProblem<S> problem, int populationSize, int archiveSize, int maxEvaluations, CrossoverOperator<S> crossover1,
            CrossoverOperator<S> crossover2, double alpha, double ro, double shrinkRate, double replacement) {
        super(problem, populationSize, archiveSize, maxEvaluations, crossover1, crossover2, alpha, ro, shrinkRate, replacement);

        for (int k = 0; k < taskNum; k++){
            Arrays.fill(probability[k], 0);
            Arrays.fill(reward[k], 1);
        }
    }

    @Override
    public List<List<S>> getResult() {
        return this.population;
    }

    @Override
    public String getName() {
        return "MATDE";
    }

    @Override
    public String getDescription() {
        return "Y. Chen et al., “An Adaptive Archive-Based Evolutionary Framework for Many-Task Optimization,” IEEE Transactions on Emerging Topics in Computational Intelligence 4, no. 3 (June 2020): 369–84, https://doi.org/10.1109/TETCI.2019.2916051.";
    }

    @Override
    protected void initProgress() {}

    @Override
    protected void updateProgress() {}

    @Override
    protected void initState() {
        initPopulation();


        evaluations = 0;
    }

    private void initPopulation() {
        this.population = new ArrayList<>(taskNum);
        this.archive = new ArrayList<>(taskNum);
        for (int k = 0; k < taskNum; k++){
            population.add(new ArrayList<>(populationSize));
            archive.add(new ArrayList<>(archiveSize));
        }
        for (int k = 0; k < taskNum; k++){
            for (int i = 0; i < populationSize; i++){
                S solution = problem.createSolution();
                solution.setSkillFactor(k);
                problem.evaluate(solution);
                evaluations ++;
                population.get(k).add(solution);
                putArchive(k, solution);
            }
        }
    }

    @Override
    protected void iteration(){
        createOffspringPopulation();
        updateArchive();
    }

    private void createOffspringPopulation(){
        for (int k = 0; k < taskNum; k++){
            List<S> offspringList = new ArrayList<>();
            if (randomGenerator.nextDouble() > alpha){
                for (int i = 0; i < populationSize; i++){
                    S offspring;
                    int r1 = i;
                    while (r1 == i){
                        r1 = randomGenerator.nextInt(0, populationSize - 1);
                    }
                    List<S> parents = new ArrayList<>(3);
                    parents.add(population.get(k).get(r1));
                    parents.add(population.get(k).get(i));
                    parents.add(population.get(k).get(i));
                    ((RandomlyDifferentialEvolutionCrossover)crossoverOperator).setCurrentSolution((DoubleSolution)population.get(k).get(i));
                    offspring = crossoverOperator.execute(parents).get(0);
                    
                    offspring.setSkillFactor(k);
                    problem.evaluate(offspring);
                    evaluations ++;

                    int flag = comparator.compare(offspring, population.get(k).get(i));
                    if (flag < 0){
                        population.get(k).set(i, offspring);
                    }else if (flag == 0){
                        offspringList.add(offspring);
                    }
                }
            }else{
                int assist = findAssistTaskId(k);
                // int assist = 1 - k;

                double[] pBest = new double[problem.getTask(k).getNumberOfObjectives()];
                for (int j = 0; j < pBest.length; j++){
                    pBest[j] = Double.MAX_VALUE;
                    for (int i = 0; i < populationSize; i++){
                        pBest[j] = Math.min(pBest[j], population.get(k).get(i).objectives()[j]);
                    }
                }

                for (int i = 0; i < populationSize; i++){
                    int r1 = randomGenerator.nextInt(0, populationSize - 1);
                    List<S> parents = new ArrayList<>(2);
                    parents.add(population.get(assist).get(r1));
                    parents.add(population.get(k).get(i));
                    S offspring = crossoverOperator2.execute(parents).get(randomGenerator.nextInt(0, 1));

                    offspring.setSkillFactor(k);
                    problem.evaluate(offspring);
                    evaluations ++;

                    int flag = comparator.compare(offspring, population.get(k).get(i));
                    if (flag < 0){
                        population.get(k).set(i, offspring);
                    }else if (flag == 0){
                        offspringList.add(offspring);
                    }
                }

                double[] pBestAfter = new double[problem.getTask(k).getNumberOfObjectives()];
                for (int j = 0; j < pBestAfter.length; j++){
                    pBestAfter[j] = Double.MAX_VALUE;
                    for (int i = 0; i < populationSize; i++){
                        pBestAfter[j] = Math.min(pBestAfter[j], population.get(k).get(i).objectives()[j]);
                    }
                }

                boolean isBetter = false;
                for (int i = 0; i < pBestAfter.length; i++){
                    if (pBestAfter[i] < pBest[i]){
                        isBetter = true;
                        break;
                    }
                }

                if (isBetter){
                    reward[k][assist] /= shrinkRate;
                }else{
                    reward[k][assist] *= shrinkRate;
                }
            }

            // Merge, sort and select
            List<S> jointPopulation = new ArrayList<>();
            jointPopulation.addAll(population.get(k));
            jointPopulation.addAll(offspringList);
            RankingAndCrowdingSelection<S> selection = new RankingAndCrowdingSelection<S>(populationSize);
            List<S> selectedPopulation = selection.execute(jointPopulation);
            population.set(k, selectedPopulation);
        }
    }

    private int findAssistTaskId(int taskId){
        KLD<S> kldCalculator = new KLD<>(archive, taskNum);
        double[] kld = kldCalculator.getKDL(taskId);

        double sum = 0;
        for (int k = 0; k < taskNum; k++){
            if (k == taskId){
                continue;
            }
            probability[taskId][k] = ro * probability[taskId][k] + reward[taskId][k] / (1 + Math.log(1 + kld[k]));
            sum += probability[taskId][k];
        }

        // 轮盘赌
        double s = 0;
        double p = randomGenerator.nextDouble();
        int idx = 0;
        for (idx = 0; idx < taskNum - 1; idx++){
            if (idx == taskId)
                continue;
            s += probability[taskId][idx] / sum;
            if (s >= p)
                break;
        }
        return idx;
    }

    private void updateArchive() {
        for (int k = 0; k < taskNum; k++){
            for (int i = 0; i < populationSize; i++){
                if (randomGenerator.nextDouble()  < replaceRate){
                    putArchive(k, population.get(k).get(i));
                }
            }
        }
    }

    private void putArchive(int taskId, S solution) {
        if (archive.get(taskId).size() < archiveSize){
            archive.get(taskId).add(solution);
        }else{
            int idx = randomGenerator.nextInt(0, archiveSize - 1);
            archive.get(taskId).set(idx, solution);
        }
    }

    @Override
    protected boolean isStoppingConditionReached() {
        return evaluations >= maxEvaluations;
    }

    @Override
    protected boolean isUpdateConditionReached() {
        return false;
    }
}
