package directed.draw;

enum StatModeEnum {
    NUMBER(StatMode.NUMBER), NUM_BY_LEN(StatMode.NUM_BY_LEN), NUM_BY_LEN_BY_NUM(StatMode.NUM_BY_LEN_BY_NUM);

    final StatMode mode;

    StatModeEnum(StatMode mode) {
        this.mode = mode;
    }

    public String toString() {
        return mode.toString();
    }
}
