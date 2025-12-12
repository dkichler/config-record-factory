package recordconfig;


import java.util.List;

public record EnumsConfig(
    Problem problem,
    List<Solution> solutions
) {
    public enum Problem {
        P1, P2, P3;
    }
    public enum Solution {
        S1, S2, S3;
    }
}
