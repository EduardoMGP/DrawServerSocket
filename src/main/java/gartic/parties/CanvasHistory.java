package gartic.parties;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CanvasHistory {
    private String c;
    private double s;
    private Action a;
    private final List<CanvasCoord> data;

    public CanvasHistory() {
        this.data = new ArrayList<>();
    }

    public enum Action {
        ERASER,
        DRAW,
        FILL;

        Action() {
        }
    }

}
