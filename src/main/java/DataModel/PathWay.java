package DataModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PathWay {
    private String path;

    public PathWay(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PathWay pathWay)) return false;
        return Objects.equals(path, pathWay.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    public String getCode() {
        return path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "PathWay{" +
                "path='" + path + '\'' +
                '}';
    }


}
