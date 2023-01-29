package beans;

import utils.SmartCityUtils;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@XmlRootElement
public class Position {

    private int x, y;

    public Position() {
        this(0, 0);
    }

    public Position(int x, int y) {
        SmartCityUtils.checkPosition(x, y);
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        SmartCityUtils.checkPosition(x, y);
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        SmartCityUtils.checkPosition(x, y);
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
