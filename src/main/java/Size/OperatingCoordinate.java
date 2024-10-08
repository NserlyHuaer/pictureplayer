package Size;

public record OperatingCoordinate(int x, int y) {
    public int GetX() {
        return x;
    }

    public int GetY() {
        return y;
    }

    public OperatingCoordinate subtract(OperatingCoordinate subtrahend) {
        return new OperatingCoordinate(this.x - subtrahend.x, this.y - subtrahend.y);
    }

}
