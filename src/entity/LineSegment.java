package entity;

import java.io.Serializable;
import java.util.Objects;

/** 轮廓上的线段 */
public class LineSegment implements Serializable {
    private Point first;
    private Point second;
    private LineSegment next;         // 其连接的下一条线段，即next的一个点与当前线段的一个点相同
    private Square square;            // 所属的正方形

    public LineSegment(Point first, Point second){
        this.first = first;
        this.second = second;
        this.next = null;
    }

    public Point getFirst() {
        return first;
    }

    public void setFirst(Point first) {
        this.first = first;
    }

    public Point getSecond() {
        return second;
    }

    public void setSecond(Point second) {
        this.second = second;
    }

    public LineSegment getNext() {
        return next;
    }

    public void setNext(LineSegment next) {
        this.next = next;
    }

    public Square getSquare() {
        return square;
    }

    public void setSquare(Square square) {
        this.square = square;
    }

    /**
     * point 是否为该线段的端点
     * @param point
     * @return
     */
    public boolean isEndPoint(Point point){
        return this.first.equals(point) || this.second.equals(point);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LineSegment that = (LineSegment) o;
        return Objects.equals(first, that.first) &&
                Objects.equals(second, that.second) &&
                Objects.equals(next, that.next);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second, next);
    }

    @Override
    public String toString() {
        return "LineSegment{" +
                "first=" + first +
                ", second=" + second +
                ", next=" + (next==null) +
                ", square=" + square +
                '}';
    }
}
