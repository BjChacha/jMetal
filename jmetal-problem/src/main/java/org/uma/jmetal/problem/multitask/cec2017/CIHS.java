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
 * @Date: created in 19-1-14 21:50
 * @Version: v
 * @Descriptiom: #
 * 1#
 * @Modified by:
 */
public class CIHS extends AbstractMultiTaskDoubleProblem {
    public CIHS() {
        List<Problem<DoubleSolution>> taskList = new ArrayList<>(2);
        taskList.add(new MMDTLZ("CIHS1", 2, 50, -100, 100, 1, "sphere"));
        taskList.add(new MMZDT("CIHS2", 50, -100, 100, 1, "mean", "linear", "concave"));

        setTaskList(taskList);
        initNumberOfVariables();

        setName("CIHS");
    }
}
