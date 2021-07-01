package org.uma.jmetal.example.multitask.mfeaddra;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.moead.AbstractMOEAD;
import org.uma.jmetal.algorithm.multitask.mfeaddra.MFEADDRA;
import org.uma.jmetal.example.AlgorithmRunner;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.PolynomialMutation;
import org.uma.jmetal.problem.MultiTaskProblem;
import org.uma.jmetal.problem.multitask.cec2017.CIHS;
import org.uma.jmetal.problem.multitask.cec2017.CILS;
import org.uma.jmetal.problem.multitask.cec2017.CIMS;
import org.uma.jmetal.problem.multitask.cec2017.NIHS;
import org.uma.jmetal.problem.multitask.cec2017.NILS;
import org.uma.jmetal.problem.multitask.cec2017.NIMS;
import org.uma.jmetal.problem.multitask.cec2017.PIHS;
import org.uma.jmetal.problem.multitask.cec2017.PILS;
import org.uma.jmetal.problem.multitask.cec2017.PIMS;
import org.uma.jmetal.problem.multitask.cec2017.base.MO;
import org.uma.jmetal.qualityindicator.QualityIndicator;
import org.uma.jmetal.qualityindicator.impl.InvertedGenerationalDistance;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.solution.mfeadoublesolution.MFEADoubleSolution;
import org.uma.jmetal.util.AbstractAlgorithmRunner;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.VectorUtils;

public class MFEADDRARunner extends AbstractAlgorithmRunner {

    /**
     * @param args Command line arguments.
     * @throws SecurityException Invoking command: java
     *                           org.uma.jmetal.runner.multiobjective.MOEADRunner
     *                           problemName [referenceFront]
     */
    public static void main(String[] args) throws IOException {
        final int TIMES = 5;
        
        List<MultiTaskProblem<MFEADoubleSolution>> multiTaskProblemList;
        Algorithm<List<MFEADoubleSolution>> algorithm;
        CrossoverOperator crossover;
        MutationOperator mutation;
        List<List<String>> referenceFrontPathList;

        multiTaskProblemList = Arrays.asList(new CIHS(), new CIMS(), new CILS(), new PIHS(), new PIMS(), new PILS(),
                new NIHS(), new NIMS(), new NILS());

        referenceFrontPathList = new ArrayList<>();
        for (MultiTaskProblem<MFEADoubleSolution> p : multiTaskProblemList) {
            List<String> referenceFrontPaths = new ArrayList<>();
            for (int k = 0; k < p.getNumberOfTasks(); k++) {
                // referenceFrontPaths.add("benchmark/cec2017/PF/" + ((MO)
                // p.getTask(k)).gethType() + ".pf");
                referenceFrontPaths.add("resources/benchmark/cec2017/PF/" + ((MO) p.getTask(k)).gethType() + ".pf");
            }
            referenceFrontPathList.add(referenceFrontPaths);
        }

        DecimalFormat form = new DecimalFormat("#.####E0");
        long tim[] = new long[multiTaskProblemList.size()];

        for (int index = 0; index < multiTaskProblemList.size(); index++) {
            MultiTaskProblem<MFEADoubleSolution> multiTasksProblem = multiTaskProblemList.get(index);

            System.out
                    .println("===========================" + multiTasksProblem.getName() + "=========================");

            crossover = new DifferentialEvolutionCrossover(0.9, 0.5,
                    DifferentialEvolutionCrossover.DE_VARIANT.RAND_1_BIN);
            mutation = new PolynomialMutation(1.0 / multiTasksProblem.getNumberOfVariables(), 20.0);

            double ave[] = new double[multiTasksProblem.getNumberOfTasks()];

            for (int run = 0; run < TIMES; run++) {

                algorithm = new MFEADDRA<MFEADoubleSolution>(multiTasksProblem,
                        105 * multiTasksProblem.getNumberOfTasks(), 105 * multiTasksProblem.getNumberOfTasks() * 1000,
                        crossover, mutation, AbstractMOEAD.FunctionType.TCHE, 0.8, 2, 10, 0.1);

                AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();

                List<MFEADoubleSolution> population = (List<MFEADoubleSolution>) algorithm.getResult();

                long computingTime = algorithmRunner.getComputingTime();
                // JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

                tim[index] += computingTime;

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
                    double[][] referenceFronts = VectorUtils.readVectors(referenceFrontPathList.get(index).get(i),
                            "\\s");
                    QualityIndicator calculator = new InvertedGenerationalDistance(referenceFronts);
                    double igd = calculator
                            .compute(SolutionListUtils.getMatrixWithObjectiveValues(solutionList.get(i)));

                    System.out.print(multiTasksProblem.getTask(i).getName() + " = " + form.format(igd) + "\t");

                    ave[i] += igd;
                }
                System.out.println("\tTime --> " + computingTime);
            }

            System.out.println();
            System.out.println("Average Time ==> " + tim[index] / TIMES);
            System.out.println();
            for (int i = 0; i < multiTasksProblem.getNumberOfTasks(); i++) {
                System.out.println("Average IGD for " + multiTasksProblem.getTask(i).getName() + ": "
                        + form.format(ave[i] / TIMES));
            }

            System.out.println("");
        }
    }
}
