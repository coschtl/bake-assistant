package at.coschtl.bakeassistant;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import at.coschtl.bakeassistant.model.Recipe;
import at.coschtl.bakeassistant.model.Step;
import at.coschtl.bakeassistant.ui.main.BakeAssistant;

public class InstructionCalculator {
    private Recipe recipe;
    private List<Instruction> instructions ;

    public InstructionCalculator(Recipe recipe) {
        this.recipe = recipe;
        instructions = new ArrayList<>(recipe.getSteps().size()+1);
    }

    public List<Instruction> calculateInstructions(Date start) {
        instructions.clear();
        Calendar calMin = Calendar.getInstance();
        Calendar calMax = Calendar.getInstance();
        calMin.setTime(start);
        calMax.setTime(start);
        for (Step step : recipe.getSteps()) {
            Instruction instruction = new Instruction(step);
            instruction.setTimeMin(calMin.getTime());
            instruction.setTimeMax(calMax.getTime());
            instructions.add(instruction);
            calMin.add(Calendar.MINUTE, (int) (step.getDurationMin() * step.getDurationUnit().getMinutes()));
            calMax.add(Calendar.MINUTE, (int) (step.getDurationMax() * step.getDurationUnit().getMinutes()));
        }
        Instruction instruction = new Instruction(BakeAssistant.CONTEXT.getString(R.string.done));
        instruction.setTimeMin(calMin.getTime());
        instruction.setTimeMax(calMax.getTime());
        instructions.add(instruction);
        return instructions;
    }

}
