package at.coschtl.bakeassistant;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import at.coschtl.bakeassistant.model.Recipe;
import at.coschtl.bakeassistant.model.Step;

public class InstructionCalculator {
    private final Recipe recipe;
    private final List<Instruction> instructions;

    public InstructionCalculator(Recipe recipe) {
        this.recipe = recipe;
        instructions = new ArrayList<>(recipe.getSteps().size() + 1);
        Calendar calMin = Calendar.getInstance();
        Calendar calMax = Calendar.getInstance();
        Date start = new Date();
        calMin.setTime(start);
        calMax.setTime(start);
        instructions.add(Instruction.getStartInstruction(calMin.getTime(), calMax.getTime()));
        for (Step step : recipe.getSteps()) {
            Instruction instruction = new Instruction(step);
            instruction.setTimeMin(calMin.getTime());
            instruction.setTimeMax(calMax.getTime());
            instructions.add(instruction);
            calMin.add(Calendar.MINUTE, (int) (step.getDurationMin() * step.getDurationUnit().getMinutes()));
            calMax.add(Calendar.MINUTE, (int) (step.getDurationMax() * step.getDurationUnit().getMinutes()));
        }
        instructions.add(Instruction.getEndInstruction(calMin.getTime(), calMax.getTime()));
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public InstructionCalculator calculateFromEnd() {
        Calendar calMin = Calendar.getInstance();
        Calendar calMax = Calendar.getInstance();
        Date end = instructions.get(instructions.size() - 1).getTimeMin().date();
        calMin.setTime(end);
        calMax.setTime(end);
        for (int i = instructions.size() - 1; i >= 0; i--) {
            Instruction instruction = instructions.get(i);
            instruction.setTimeMin(calMin.getTime());
            instruction.setTimeMax(calMax.getTime());
            Step step = instruction.getStep();
            if (step != null) {
                calMin.add(Calendar.MINUTE, (int) (-1 * step.getDurationMax() * step.getDurationUnit().getMinutes()));
                calMax.add(Calendar.MINUTE, (int) (-1 * step.getDurationMin() * step.getDurationUnit().getMinutes()));
            }
        }
        return this;
    }

    public InstructionCalculator calculateFromStart() {
        Calendar calMin = Calendar.getInstance();
        Calendar calMax = Calendar.getInstance();
        Date start = instructions.get(0).getTimeMin().date();
        calMin.setTime(start);
        calMax.setTime(start);
        for (int i = 0; i < instructions.size(); i++) {
            Instruction instruction = instructions.get(i);
            instruction.setTimeMin(calMin.getTime());
            instruction.setTimeMax(calMax.getTime());
            Step step = instruction.getStep();
            if (step != null) {
                calMin.add(Calendar.MINUTE, (int) (step.getDurationMin() * step.getDurationUnit().getMinutes()));
                calMax.add(Calendar.MINUTE, (int) (step.getDurationMax() * step.getDurationUnit().getMinutes()));
            }
        }
        return this;
    }

}
