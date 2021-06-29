package org.uma.jmetal.example.multitask.mfeaddra;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.moead.AbstractMOEAD;
import org.uma.jmetal.algorithm.multitask.mfeaddra.MFEADDRAMeasures;
import org.uma.jmetal.example.AlgorithmRunner;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.PolynomialMutation;
import org.uma.jmetal.problem.MultiTaskProblem;
import org.uma.jmetal.problem.multitask.cec2017.CIHS;
import org.uma.jmetal.problem.multitask.cec2017.base.MO;
import org.uma.jmetal.qualityindicator.QualityIndicator;
import org.uma.jmetal.qualityindicator.impl.InvertedGenerationalDistance;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.solution.mfeadoublesolution.MFEADoubleSolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.VectorUtils;
import org.uma.jmetal.util.chartcontainer.ChartContainer;
import org.uma.jmetal.util.measure.MeasureListener;
import org.uma.jmetal.util.measure.MeasureManager;
import org.uma.jmetal.util.measure.impl.BasicMeasure;
import org.uma.jmetal.util.measure.impl.CountingMeasure;

public class MFEADDRAWithMeasureRunner extends AbstractAlgorithmRunner {

    /**
     * @param args Command line arguments.
     * @throws SecurityException Invoking command: java
     *                           org.uma.jmetal.runner.multiobjective.MOEADRunner problemName
     *                           [referenceFront]
     */
    public static void main(String[] args) throws IOException {
        final int TIMES = 1;
        final int PLOT_UPDATE_PERIOD = 20;

        List<MultiTaskProblem<MFEADoubleSolution>> multiTaskProblemList;
        Algorithm<List<MFEADoubleSolution>> algorithm;
        CrossoverOperator crossover;
        MutationOperator mutation;
        List<List<String>> referenceFrontPathList;

        // multiTaskProblemList = Arrays.asList(new CIHS(), new CIMS(), new CILS(), new PIHS(), new PIMS(), new PILS(), new NIHS(), new NIMS(), new NILS());
        multiTaskProblemList = Arrays.asList(new CIHS());

        referenceFrontPathList = new ArrayList<>();
        for (MultiTaskProblem<MFEADoubleSolution> p: multiTaskProblemList){
            List<String> referenceFrontPaths = new ArrayList<>();
            for (int k = 0; k < p.getNumberOfTasks(); k++){
                // referenceFrontPaths.add("benchmark/cec2017/PF/" + ((MO) p.getTask(k)).gethType() + ".pf");
                referenceFrontPaths.add("resources/benchmark/cec2017/PF/" + ((MO) p.getTask(k)).gethType() + ".pf");
            }
            referenceFrontPathList.add(referenceFrontPaths);
        }

        DecimalFormat form = new DecimalFormat("#.####E0");
        long times[] = new long[multiTaskProblemList.size()];

        for (int index = 0; index < multiTaskProblemList.size(); index++) {
            MultiTaskProblem<MFEADoubleSolution> multiTasksProblem = multiTaskProblemList.get(index);
            
            System.out.println("===========================" + multiTasksProblem.getName() + "=========================");

            crossover = new DifferentialEvolutionCrossover(0.9, 0.5, DifferentialEvolutionCrossover.DE_VARIANT.RAND_1_BIN);
            mutation = new PolynomialMutation(1.0 / multiTasksProblem.getNumberOfVariables(), 20.0);

            double ave[] = new double[multiTasksProblem.getNumberOfTasks()];

            for (int run = 0; run < TIMES; run++) {
                algorithm = new MFEADDRAMeasures<MFEADoubleSolution>(
                        multiTasksProblem,
                        105 * multiTasksProblem.getNumberOfTasks(),
                        105 * multiTasksProblem.getNumberOfTasks() * 1000,
                        crossover,
                        mutation,
                        AbstractMOEAD.FunctionType.TCHE,
                        0.8,
                        2,
                        10,
                        0.1,
                        PLOT_UPDATE_PERIOD);

                ((MFEADDRAMeasures<MFEADoubleSolution>) algorithm).setReferenceFront(referenceFrontPathList.get(index));

                MeasureManager measureManager = ((MFEADDRAMeasures<MFEADoubleSolution>) algorithm).getMeasureManager();

                CountingMeasure iterationMeasure = (CountingMeasure) measureManager.<Long>getPushMeasure("currentEvaluation");
                List<BasicMeasure<List<DoubleSolution>>> solutionListMeasure = new ArrayList<>();
                List<BasicMeasure<Double>> indicatorMeasure = new ArrayList<>();
                for (int i = 0; i < multiTasksProblem.getNumberOfTasks(); i++){
                    solutionListMeasure.add((BasicMeasure<List<DoubleSolution>>) measureManager.<List<DoubleSolution>>getPushMeasure("currentPopulation_" + i));
                    indicatorMeasure.add((BasicMeasure<Double>) measureManager.<Double>getPushMeasure("indicator_" + i));
                }

                ChartContainer[] charts = new ChartContainer[multiTasksProblem.getNumberOfTasks()];
                for (int k = 0; k < multiTasksProblem.getNumberOfTasks(); k++){
                    charts[k] = new ChartContainer(algorithm.getName(), 100);
                    charts[k].setFrontChart(0, 1, referenceFrontPathList.get(index).get(k), "\\s");
                    charts[k].addIndicatorChart("NormHV");
                    charts[k].initChart();

                    solutionListMeasure.get(k).register(new ChartListener(charts[k]));
                    iterationMeasure.register(new IterationListener(charts[k]));
                    indicatorMeasure.get(k).register(new IndicatorListener("NormHV", charts[k]));
                }
                AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();

                for (int k = 0; k < multiTasksProblem.getNumberOfTasks(); k++){
                    charts[k].saveChart("./chart", BitmapFormat.PNG);
                }
                List<MFEADoubleSolution> population = (List<MFEADoubleSolution>) algorithm.getResult();

                long computingTime = algorithmRunner.getComputingTime();
//                JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

                times[index] += computingTime;

                List<List<DoubleSolution>> solutionList = new ArrayList<>(multiTasksProblem.getNumberOfTasks());
                for (int i = 0; i < multiTasksProblem.getNumberOfTasks(); i++) {
                    solutionList.add(new ArrayList<>());
                }

                for (int i = 0; i < population.size(); i++) {
                    int skillFactor = population.get(i).getSkillFactor();
                    solutionList.get(skillFactor).add(population.get(i).getSolution(skillFactor));
                }

                System.out.print(run + "\t");

                for (int i = 0; i < multiTasksProblem.getNumberOfTasks(); i++) {
                    double[][] referenceFronts = VectorUtils.readVectors(referenceFrontPathList.get(index).get(i), "\\s");
                    QualityIndicator calculator = new InvertedGenerationalDistance(referenceFronts);
                    double igd = calculator.compute(SolutionListUtils.getMatrixWithObjectiveValues(solutionList.get(i)));

                    System.out.print(multiTasksProblem.getTask(i).getName() + " = " + form.format(igd) + "\t");
                    ave[i] += igd;
                }
                System.out.println("\tTime --> " + computingTime);
            }

            System.out.println();
            System.out.println("Average Time ==> " + times[index] / TIMES);
            System.out.println();
            for (int i = 0; i < multiTasksProblem.getNumberOfTasks(); i++) {
                    System.out.println("Average IGD for " + multiTasksProblem.getTask(i).getName() + ": " + form.format(ave[i] / TIMES));
            }

            System.out.println("");
        }
    }

    private static class IterationListener implements MeasureListener<Long>{
        ChartContainer chart;

        public IterationListener(ChartContainer chart){
            this.chart = chart;
            this.chart.getChart("NormHV").setTitle("Iteration: " + 0);
        }

        @Override
        synchronized public void measureGenerated(Long iteration){
            if (this.chart != null){
                this.chart.getChart("NormHV").setTitle("Iteration: " + iteration);
            }
        }
    }

    private static class IndicatorListener implements MeasureListener<Double>{
        ChartContainer chart;
        String indicator;

        public IndicatorListener(String indicator, ChartContainer chart){
            this.chart = chart;
            this.indicator = indicator;
        }

        @Override
        synchronized public void measureGenerated(Double value){
            if (this.chart != null){
                this.chart.updateIndicatorChart(this.indicator, value);
                this.chart.refreshCharts(0);
            }
        }
    }

    private static class ChartListener implements MeasureListener<List<DoubleSolution>>{
        private ChartContainer chart;
        private int iteration = 0;

        public ChartListener(ChartContainer chart){
            this.chart = chart;
            this.chart.getFrontChart().setTitle("Iteration: " + this.iteration);
        }

        private void refreshChart(List<DoubleSolution> solutionList){
            if(this.chart != null){
                iteration ++;
                this.chart.getFrontChart().setTitle("Iteration: " + this.iteration);
                this.chart.updateFrontCharts(solutionList);
                this.chart.refreshCharts();
            }
        }

        @Override
        synchronized public void measureGenerated(List<DoubleSolution> solutions){
            refreshChart(solutions);
        }
    }
}
