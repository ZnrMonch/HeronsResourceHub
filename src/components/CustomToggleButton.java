package components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.Icon;

public class CustomToggleButton extends CustomButton {
    private static final long serialVersionUID = 1L;

    private boolean isToggled = false;

    private String textOff;
    private String textOn;
    
    private Icon iconOff;
    private Icon iconOn;

    private List<Consumer<Boolean>> toggleListeners = new ArrayList<>();

    // Constructor for Text
    public CustomToggleButton(String textOff, String textOn) {
        this(textOff, textOn, 0);
    }

    public CustomToggleButton(String textOff, String textOn, int radius) {
        super(textOff, radius);
        this.textOff = textOff;
        this.textOn = textOn;
        initToggle();
    }

    public CustomToggleButton(Icon iconOff, Icon iconOn) {
        this(iconOff, iconOn, 0);
    }

    public CustomToggleButton(Icon iconOff, Icon iconOn, int radius) {
        super(iconOff, radius);
        this.iconOff = iconOff;
        this.iconOn = iconOn;
        initToggle();
    }

    private void initToggle() {
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isToggled = !isToggled;
                updateState();
                notifyListeners();
            }
        });
        updateState();
    }

    public boolean isToggled() {
        return isToggled;
    }

    public void setToggled(boolean toggled) {
        if (this.isToggled != toggled) {
            this.isToggled = toggled;
            updateState();
            notifyListeners();
        }
    }

    private void updateState() {
        if (textOff != null && textOn != null) {
            setText(isToggled ? textOn : textOff);
        } else if (iconOff != null && iconOn != null) {
            setIcon(isToggled ? iconOn : iconOff);
        }
    }

    public void addToggleListener(Consumer<Boolean> listener) {
        toggleListeners.add(listener);
    }

    private void notifyListeners() {
        for (Consumer<Boolean> listener : toggleListeners) {
            listener.accept(isToggled);
        }
    }
}