package org.uma.jmetal.problem;

import java.util.List;

import org.uma.jmetal.solution.MFEASolution;
import org.uma.jmetal.solution.Solution;

public abstract class AbstractMultiTaskProblem<S extends Solution<?>, T extends MFEASolution<?, S>>
        implements MultiTaskProblem<T> {
    private int numberOfVariables;
    private List<Problem<S>> taskList;
    private String name;

    protected void initNumberOfVariables() {
        int numVar = taskList.get(0).getNumberOfVariables();
        for (int i = 1; i < taskList.size(); i++) {
            if (taskList.get(i).getNumberOfVariables() > numVar) {
                numVar = taskList.get(i).getNumberOfVariables();
            }
        }
        setNumberOfVariables(numVar);
    }

    protected void resetting(T solution) {
        for (int i = 0; i < this.getNumberOfTasks(); i++) {
            solution.setSolution(i, null);
        }
    }

    public void setNumberOfVariables(int numberOfVariables) {
        this.numberOfVariables = numberOfVariables;
    }

    public void setTaskList(List<Problem<S>> taskList) {
        this.taskList = taskList;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Problem<S>> getTaskList() {
        return taskList;
    }

    public String getName() {
        return name;
    }

    @Override
    public Problem<S> getTask(int index) {
        return taskList.get(index);
    }

    @Override
    public int getNumberOfTasks() {
        return taskList.size();
    }

    @Override
    public int getNumberOfVariables() {
        return numberOfVariables;
    }
}