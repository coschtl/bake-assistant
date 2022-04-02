package at.coschtl.bakeassistant;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import at.coschtl.bakeassistant.model.Recipe;
import at.coschtl.bakeassistant.model.Step;
import at.coschtl.bakeassistant.util.Time;

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

    public InstructionCalculator recalculateBySeconds(int changedInstructionPosition, int diffSeconds, int minimumStepToRecalculate) {
        recalculateRemaining(changedInstructionPosition, diffSeconds);
        recalculatePredecessors(changedInstructionPosition, diffSeconds, minimumStepToRecalculate);
        return this;
    }

    public InstructionCalculator recalculatePredecessors(int changedInstructionPosition, int diffSeconds, int minimumStepToRecalculate) {
        if (minimumStepToRecalculate >= 0  && minimumStepToRecalculate<changedInstructionPosition) {
            return this;
        }
        Calendar cal = Calendar.getInstance();
        for (int i = changedInstructionPosition - 1; i >= 0; i--) {
            Instruction instruction = instructions.get(i);
            instruction.setTimeMin(getNewDate(instruction.getTimeMin(), diffSeconds, cal));
            instruction.setTimeMax(getNewDate(instruction.getTimeMax(), diffSeconds, cal));
        }
        return this;
    }

    private Date getNewDate(Time origTime, int diffSeconds, Calendar cal) {
        cal.setTime(origTime.date());
        cal.add(Calendar.SECOND, diffSeconds);
        return cal.getTime();
    }

    public InstructionCalculator recalculateRemaining(int changedInstructionPosition, int diffSeconds) {
        Calendar cal = Calendar.getInstance();
        Instruction instruction = instructions.get(changedInstructionPosition);
        instruction.setTimeMin(getNewDate(instruction.getTimeMin(), diffSeconds, cal));
        instruction.setTimeMax(instruction.getTimeMin().date());
        for (int i = changedInstructionPosition + 1; i < instructions.size(); i++) {
            Date newTimeMin = getNewDate(instruction.getTimeMin(), instruction.getDurationMinSeconds(), cal);
            Date newTimeMax = getNewDate(instruction.getTimeMax(), instruction.getDurationMaxSeconds(), cal);
            instruction = instructions.get(i);
            instruction.setTimeMin(newTimeMin);
            instruction.setTimeMax(newTimeMax);
        }
        return this;
    }

}
