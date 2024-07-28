package io.github.rkeeves.model;

public class R {
    public int rid = 0;
    public String name = "R";

    public static R copy(R r) {
        var rr = new R();
        rr.rid = r.rid;
        rr.name = r.name;
        return rr;
    }

    @Override
    public String toString() {
        return "R{" +
                "rid=" + rid +
                ", name='" + name + '\'' +
                '}';
    }
}
