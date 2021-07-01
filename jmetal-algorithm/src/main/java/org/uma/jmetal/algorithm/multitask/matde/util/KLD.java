package org.uma.jmetal.algorithm.multitask.matde.util;

import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.uma.jmetal.solution.MFEASolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.errorchecking.Check;

public class KLD<S extends MFEASolution<?, ? extends Solution<?>>> {
    private int maxVarNum;
    private int minVarNum;
    private int taskNum;

    private double cov[][][];
    private double covInv[][][];
    private double covDet[];

    private List<List<S>> archive;

    public KLD(List<List<S>> archive, int taskNum) {
        Check.notNull(archive);
        this.archive = archive;
        this.taskNum = taskNum;

        this.maxVarNum = 0;
        this.minVarNum = Integer.MAX_VALUE;

        for (int i = 0; i < archive.size(); i++){
            Check.notNull(archive.get(i));
            maxVarNum = Math.max(maxVarNum, archive.get(i).get(0).variables().size());
            minVarNum = Math.min(minVarNum, archive.get(i).get(0).variables().size());
        }

        this.cov = new double[taskNum][maxVarNum][maxVarNum];
        this.covInv = new double[taskNum][maxVarNum][maxVarNum];
        this.covDet = new double[taskNum];
    }

    public double[] getKDL(int task){
        double[] kld = new double[taskNum];
        double tr, u;
        double s1, s2;

        cov[task] = getCov(task).getData();
        covDet[task] = getCovDet(task);
        covInv[task] = getCovInv(task).getData();

        int varNum = archive.get(task).get(0).variables().size();
        
        for (int i = 0; i < taskNum; i++) {
            if (i == task)
                continue;

            cov[i] = getCov(i).getData();
            covDet[i] = getCovDet(i);
            covInv[i] = getCovInv(i).getData();

            tr = getTrace(task, i);
            u = getMul(task, i);
            s1 = Math.abs(0.5 * (tr + u - varNum + Math.log(covDet[task] / covDet[i])));

            tr = getTrace(i, task);
            u = getMul(i, task);
            s2 = Math.abs(0.5 * (tr + u - varNum + Math.log(covDet[i] / covDet[task])));

            kld[i] = 0.5 * (s1 + s2);
        }
        return kld;
    }

    private double getMul(int t1, int t2){
        double a[] = new double[maxVarNum];
        double sum;
        int i, j;

        for (i = 0; i < minVarNum; i++) {
            sum = 0;
            for (j = 0; j < minVarNum; j++) {
                sum += (getMeanOfIdx(t1, j) - getMeanOfIdx(t2, j)) * covInv[t1][j][i];
            }
            a[i] = sum;
        }

        sum = 0;
        for (i = 0; i < minVarNum; i++) {
            sum += (getMeanOfIdx(t1, i) - getMeanOfIdx(t2, i)) * a[i];
        }

        return sum;
    }

    // 计算t1 cov_inv和t2 cov的乘积的迹
    private double getTrace(int t1, int t2) {
        RealMatrix m1 = new Array2DRowRealMatrix(covInv[t1]);
        RealMatrix m2 = new Array2DRowRealMatrix(cov[t2]);
        RealMatrix m = m1.multiply(m2);
        return m.getTrace();
    }

    // 计算协方差矩阵的逆
    private RealMatrix getCovInv(int task){
        RealMatrix m = new Array2DRowRealMatrix(cov[task]);
        SingularValueDecomposition svd = new SingularValueDecomposition(m);
        DecompositionSolver solver = svd.getSolver();
        return solver.getInverse();
    }

    // 计算协方差矩阵的行列式
    private double getCovDet(int task){
        RealMatrix m = new Array2DRowRealMatrix(cov[task]);
        LUDecomposition L = new LUDecomposition(m);
        double det = L.getDeterminant();
        if (det < 1e-3)
            det = 1e-3;
        return det;
    }

    // 计算种群archives[task]中变量间的相关性（所有个体之和，取平均）
    private RealMatrix getCov(int task){
        int popSize = archive.get(task).size();
        int varSize = archive.get(task).get(0).variables().size();
        double[][] solutionMat = new double[popSize][varSize];
        for (int i = 0; i < popSize; i++){
            for (int j = 0; j < varSize; j++){
                solutionMat[i][j] = (double)archive.get(task).get(i).variables().get(j);
            }
        }
        Covariance c = new Covariance(solutionMat);
        return c.getCovarianceMatrix();
    }

    private double getMeanOfIdx(int taskId, int varId){
        double sum = 0;
        for (int i = 0; i < archive.get(taskId).size(); i++){
            sum += (double) archive.get(taskId).get(i).variables().get(varId);
        }
        return sum / archive.get(taskId).size();
    }
}

