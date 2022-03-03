package at.coschtl.bakeassistant.ui.preparation;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import at.coschtl.bakeassistant.Instruction;

public interface SettableTime {
    boolean setTime(int hour, int minute);

    int getHour();

    int getMinute();


    class InstructionRowSettableTime implements SettableTime {
        private final LinearLayout row;
        private final Instruction instruction;

        public InstructionRowSettableTime(LinearLayout row, Instruction instruction) {
            this.row = row;
            this.instruction = instruction;
        }

        @Override
        public boolean setTime(int hour, int minute) {
            if (instruction.setExecutionTime(hour, minute)) {
                InstructionsAdapter.updateRow(row, instruction);
                return true;
            }
            return false;
        }

        @Override
        public int getHour() {
            return instruction.getTimeMin().hour();
        }

        @Override
        public int getMinute() {
            return  instruction.getTimeMin().minute();
        }
    }

    class TextViewSettableTime implements SettableTime {

        private final TextView textView;

        public TextViewSettableTime(TextView textView) {
            this.textView = textView;
        }

        @Override
        public boolean setTime(int hour, int minute) {
            textView.setText(twoDigits(hour) + ":" + twoDigits(minute));
            return true;
        }

        private String twoDigits(int i) {
            if (i<10) {
                return "0"+i;
            }
            return Integer.toString(i);
        }

        @Override
        public int getHour() {
            int pos = getColonPos();
            if (pos > 0) {
                return Integer.parseInt(textView.getText().toString().substring(0, pos));
            }
            return 0;
        }

        @Override
        public int getMinute() {
            int pos = getColonPos();
            if (pos > 0) {
                return Integer.parseInt(textView.getText().toString().substring(pos + 1));
            }
            return 0;
        }

        private int getColonPos() {
            if (textView.getText() == null) {
                return -1;
            }
            return textView.getText().toString().indexOf(':');
        }
    }
}
