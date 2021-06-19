package org.uma.jmetal.problem.multitask.cec2017;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.multitask.cec2017.base.MMDTLZ;
import org.uma.jmetal.problem.multitask.cec2017.base.MMZDT;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.problem.multitaskdoubleproblem.impl.AbstractMultiTaskDoubleProblem;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Zhi-Ming Dong, dzm.neu@gmail.com
 * @Date: created in 19-1-18 09:25
 * @Version: v
 * @Descriptiom: #
 * 1#
 * @Modified by:
 */
public class CILS extends AbstractMultiTaskDoubleProblem {
    public CILS() {
        List<Problem<DoubleSolution>> taskList = new ArrayList<>(2);
        taskList.add(new MMDTLZ("CILS1", 2, 50, -2, 2, 1, "rastrigin"));
        taskList.add(new MMZDT("CILS2", 50, -1, 1, 1, "ackley", "linear", "convex"));

        setTaskList(taskList);
        initNumberOfVariables();

        setName("CILS");
    }
}
