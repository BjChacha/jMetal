package org.uma.jmetal.algorithm.multitask.mfeaddra;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.algorithm.multiobjective.moead.AbstractMOEAD;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.problem.MultiTaskProblem;
import org.uma.jmetal.qualityindicator.impl.NormalizedHypervolume;
import org.uma.jmetal.solution.MFEASolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.solution.mfeadoublesolution.MFEADoubleSolution;
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
public class MFEADDRAMeasures<S extends MFEASolution<?, ? extends Solution<?>>> extends MFEADDRA<S> implements Measurable {
    protected CountingMeasure evaluationsMeasure;
    protected DurationMeasure durationMeasure;
    protected SimpleMeasureManager measureManager;

    protected List<BasicMeasure<List<DoubleSolution>>> solutionListMeasure;
    protected List<BasicMeasure<Double>> indicateValue;

    protected int taskNum;
    protected double[][][] referenceFront;

    private int updateMeasureCycle;
    private long evaluationIncrement;

    public MFEADDRAMeasures(MultiTaskProblem<S> problem, int populationSize, int maxIterations,
            CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
            AbstractMOEAD.FunctionType functionType, double neighborhoodSelectionProbability,
            int maximumNumberOfReplacedSolutions, int neighborSize, double rmp, int updateMeasureCycle) {
        super(problem, populationSize, maxIterations, crossoverOperator, mutationOperator, functionType,
                neighborhoodSelectionProbability, maximumNumberOfReplacedSolutions, neighborSize, rmp);

        this.updateMeasureCycle = updateMeasureCycle;
        this.taskNum = problem.getNumberOfTasks();
        
        initMeasures();
    }

    public MFEADDRAMeasures(MultiTaskProblem<S> problem, int populationSize, int maxIterations,
            CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
            AbstractMOEAD.FunctionType functionType, double neighborhoodSelectionProbability,
            int maximumNumberOfReplacedSolutions, int neighborSize, double rmp) {
        this(problem, populationSize, maxIterations, crossoverOperator, mutationOperator, functionType,
                neighborhoodSelectionProbability, maximumNumberOfReplacedSolutions, neighborSize, rmp, 20);
    }

    @Override
    protected void initProgress() {
        evaluationsMeasure.reset(evaluations);
    }

    @Override
    protected void iteration() {
        long oldEvaluation = this.evaluations;
        super.iteration();
        evaluationIncrement = this.evaluations - oldEvaluation;
    }

    @Override
    protected void updateProgress() {
        evaluationsMeasure.increment(evaluationIncrement);
        List<List<DoubleSolution>> multiPopulations = getMultiPopulations();
        Check.that(taskNum == multiPopulations.size(),
                "The number of populations is not equal to the number of tasks.");
        Check.notNull(referenceFront);
        for (int i = 0; i < multiPopulations.size(); i++) {
            // 更新种群
            solutionListMeasure.get(i).push(multiPopulations.get(i));
            // 更新指标值
            // double value = new InvertedGenerationalDistance(referenceFront[i]).compute(SolutionListUtils.getMatrixWithObjectiveValues(multiPopulations.get(i)));
            double value = new NormalizedHypervolume(referenceFront[i]).compute(SolutionListUtils.getMatrixWithObjectiveValues(multiPopulations.get(i)));
            indicateValue.get(i).push(value);
        }
    }

    @Override
    protected boolean isStoppingConditionReached() {
        return evaluationsMeasure.get() >= maxEvaluations;
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

    protected boolean isPeriodicUpdate(int cycle){
        return cycle != 0 && generation % cycle == 0;
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

    private List<List<DoubleSolution>> getMultiPopulations() {
        List<List<DoubleSolution>> solutionList = new ArrayList<>(taskNum);
        for (int i = 0; i < taskNum; i++) {
            solutionList.add(new ArrayList<>());
        }
        for (int i = 0; i < population.size(); i++) {
            int skillFactor = population.get(i).getSkillFactor();
            solutionList.get(skillFactor).add(((List<MFEADoubleSolution>) population).get(i).getSolution(skillFactor));
        }
        return solutionList;
    }

    public MeasureManager getMeasureManager() {
        return measureManager;
    }

    public CountingMeasure getEvaluations() {
        return evaluationsMeasure;
    }

    public String getName() {
        return "MFEA/D-DRA-Measure";
    }

    @Override
    public String getDescription() {
        return "MFEA/D-DRA using measures";
    }

    public void setReferenceFront(List<String> referenceFrontPaths) throws IOException {
        double[][][] tmp = new double[referenceFrontPaths.size()][][];
        for (int i = 0; i < referenceFrontPaths.size(); i++) {
            tmp[i] = VectorUtils.readVectors(referenceFrontPaths.get(i), "\\s");
        }
        this.referenceFront = tmp;
    }
}
