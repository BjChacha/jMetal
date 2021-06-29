package org.uma.jmetal.problem;

import java.io.Serializable;

public interface MultiTaskProblem<S> extends Serializable {

    int getNumberOfTasks();

    // TODO: 2020/2/7 这个地方泛型的利用有些不妥
    Problem getTask(int index);

    int getNumberOfVariables();

    String getName();

    void evaluate(S solution);

    S createSolution();
}