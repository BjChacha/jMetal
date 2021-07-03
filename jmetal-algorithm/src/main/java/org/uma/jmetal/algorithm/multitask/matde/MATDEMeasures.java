package org.uma.jmetal.algorithm.multitask.matde;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.problem.MultiTaskProblem;
import org.uma.jmetal.qualityindicator.impl.NormalizedHypervolume;
import org.uma.jmetal.solution.MFEASolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.VectorUtils;
import org.uma.jmetal.util.errorchecking.Check;
import org.uma.jmetal.util.measure.Measurable;
import org.uma.jmetal.util.measure.MeasureManager;
import org.uma.jmetal.util.measure.impl.BasicMeasure;
import org.uma.jmetal.util.measure.impl.CountingMeasure;
import org.uma.jmetal.util.measure.impl.DurationMeasure;
import org.uma.jmetal.util.measure.impl.SimpleMeasureManager;

@SuppressWarnings("serial")
public class MATDEMeasures<S extends MFEASolution<?, ? extends Solution<?>>> extends MATDE<S> implements Measurable {
    protected CountingMeasure evaluationsMeasure;
    protected DurationMeasure durationMeasure;
    protected SimpleMeasureManager measureManager;

    protected List<BasicMeasure<List<DoubleSolution>>> solutionListMeasure;
    protected List<BasicMeasure<Double>> indicateValue;

    protected double[][][] referenceFront;

    private int updateMeasureCycle;
    private long evaluationIncrement;

    public MATDEMeasures(MultiTaskProblem<S> problem, int populationSize, int archiveSize, int maxEvaluations,
            CrossoverOperator<S> crossover1, CrossoverOperator<S> crossover2, double alpha, double ro,
            double shrinkRate, double replacement, int updateMeasureCycle) {
        super(problem, populationSize, archiveSize, maxEvaluations, crossover1, crossover2, alpha, ro, shrinkRate,
                replacement);

        this.updateMeasureCycle = updateMeasureCycle;

        initMeasures();
    }

    public MATDEMeasures(MultiTaskProblem<S> problem, int populationSize, int archiveSize, int maxEvaluations,
            CrossoverOperator<S> crossover1, CrossoverOperator<S> crossover2, double alpha, double ro,
            double shrinkRate, double replacement) {
        this(problem, populationSize, archiveSize, maxEvaluations, crossover1, crossover2, alpha, ro, shrinkRate,
                replacement, 20);

        initMeasures();
    }

    @Override
    protected void initProgress(){
        evaluationsMeasure.reset(evaluations);
    }

    @Override
    protected void iteration(){
        long oldEvaluation = this.evaluations;
        super.iteration();
        evaluationIncrement = this.evaluations - oldEvaluation;
    }

    @Override
    protected void updateProgress(){
        evaluationsMeasure.increment(evaluationIncrement);
        Check.that(taskNum == this.population.size(),
                "The number of populations is not equal to the number of tasks.");
        Check.notNull(referenceFront);
        
        for (int i = 0; i < taskNum; i++) {
            // 更新种群
            solutionListMeasure.get(i).push((List<DoubleSolution>)population.get(i));
            // 更新指标值
            // double value = new InvertedGenerationalDistance(referenceFront[i]).compute(SolutionListUtils.getMatrixWithObjectiveValues(multiPopulations.get(i)));
            double value = new NormalizedHypervolume(referenceFront[i]).compute(SolutionListUtils.getMatrixWithObjectiveValues(population.get(i)));
            indicateValue.get(i).push(value);
        }
    }

    @Override
    protected boolean isStoppingConditionReached() {
        return evaluationsMeasure.get() >= maxEvaluations;
    }

    protected boolean isPeriodicUpdate(int cycle){
        return cycle != 0 && (evaluations - (evaluations % (taskNum * populationSize))) % cycle == 0;
    }

    @Override
    public void run() {
        durationMeasure.reset();
        durationMeasure.start();
        initState();
        initProgress();
        while (!isStoppingConditionReached()){
            iteration();
            if (isPeriodicUpdate(updateMeasureCycle)) {
                updateProgress();
            }
        }
        durationMeasure.stop();
    }

    private void initMeasures() {
        evaluationsMeasure = new CountingMeasure(0);
        durationMeasure = new DurationMeasure();
        solutionListMeasure = new ArrayList<>();
        indicateValue = new ArrayList<>();
        for (int i = 0; i < taskNum; i++) {
            solutionListMeasure.add(new BasicMeasure<>());
            indicateValue.add(new BasicMeasure<>());
        }

        measureManager = new SimpleMeasureManager();
        measureManager.setPullMeasure("currentExcutionTime", durationMeasure);
        measureManager.setPullMeasure("currentEvaluation", evaluationsMeasure);

        measureManager.setPushMeasure("currentEvaluation", evaluationsMeasure);
        for (int i = 0; i < taskNum; i++) {
            measureManager.setPushMeasure("indicator_" + i, indicateValue.get(i));
            measureManager.setPushMeasure("currentPopulation_" + i, solutionListMeasure.get(i));
        }
    }

    public MeasureManager getMeasureManager() {
        return measureManager;
    }

    public CountingMeasure getEvaluations() {
        return evaluationsMeasure;
    }

    public String getName() {
        return "MaTDE-Manager";
    }

    @Override
    public String getDescription() {
        return "MaTDE using measures";
    }

    public void setReferenceFront(List<String> referenceFrontPaths) throws IOException {
        double[][][] tmp = new double[referenceFrontPaths.size()][][];
        for (int i = 0; i < referenceFrontPaths.size(); i++) {
            tmp[i] = VectorUtils.readVectors(referenceFrontPaths.get(i), "\\s");
        }
        this.referenceFront = tmp;
    }
}
