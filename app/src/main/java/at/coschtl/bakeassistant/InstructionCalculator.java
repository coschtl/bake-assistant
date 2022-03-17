package at.coschtl.bakeassistant;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import at.coschtl.bakeassistant.model.Recipe;
import at.coschtl.bakeassistant.model.Step;

public class InstructionCalculator implements Serializable {
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

    public Recipe getRecipe() {
        return recipe;
    }

    public InstructionCalculator recalculate(int changedInstructionPosition) {
        Calendar calMin = Calendar.getInstance();
        Calendar calMax = Calendar.getInstance();
        Date fixed = instructions.get(changedInstructionPosition).getTimeMin().date();

        calMin.setTime(fixed);
        calMax.setTime(fixed);
        for (int i = changedInstructionPosition; i < instructions.size(); i++) {
            Instruction instruction = instructions.get(i);
            instruction.setTimeMin(calMin.getTime());
            instruction.setTimeMax(calMax.getTime());
            Step step = instruction.getStep();
            if (step != null) {
                calMin.add(Calendar.MINUTE, (int) (step.getDurationMin() * step.getDurationUnit().getMinutes()));
                calMax.add(Calendar.MINUTE, (int) (step.getDurationMax() * step.getDurationUnit().getMinutes()));
            }
        }

        calMin.setTime(fixed);
        calMax.setTime(fixed);
        for (int i = changedInstructionPosition; i >= 0; i--) {
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

}
