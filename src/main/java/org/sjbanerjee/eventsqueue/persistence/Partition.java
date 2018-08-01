package org.sjbanerjee.eventsqueue.persistence;

public class Partition {
    private String name;
    private int offset = -1;
    private int position;
    private static int SEGMENT_SIZE = 10; // FIXME - Dynamic offset allocation

    public Partition (String name){
        this.name = name;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getName(){
        return this.name;
    }
}
