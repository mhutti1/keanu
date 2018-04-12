package io.improbable.keanu.vertices.dbl.probabilistic;

import io.improbable.keanu.distributions.continuous.Laplace;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.ConstantDoubleVertex;

import java.util.Map;
import java.util.Random;

public class LaplaceDistribution extends ProbabilisticDouble {

    private final DoubleVertex mu;
    private final DoubleVertex beta;
    private final Random random;

    public LaplaceDistribution(DoubleVertex mu, DoubleVertex beta, Random random) {
        this.mu = mu;
        this.beta = beta;
        this.random = random;
        setValue(sample());
        setParents(mu, beta);
    }

    public LaplaceDistribution(DoubleVertex mu, DoubleVertex beta) {
        this(mu, beta, new Random());
    }

    public LaplaceDistribution(double mu, double beta, Random random) {
        this(new ConstantDoubleVertex(mu), new ConstantDoubleVertex(beta), random);
    }

    public LaplaceDistribution(double mu, double beta) {
        this(new ConstantDoubleVertex(mu), new ConstantDoubleVertex(beta), new Random());
    }

    @Override
    public double density(Double value) {
        return Laplace.pdf(mu.getValue(), beta.getValue(), value);
    }

    public double logDensity(Double value) {
        return Laplace.logPdf(mu.getValue(), beta.getValue(), value);
    }

    @Override
    public Map<String, Double> dDensityAtValue() {
        Laplace.Diff diff = Laplace.dPdf(mu.getValue(), beta.getValue(), getValue());
        return null;
    }

    @Override
    public Map<String, Double> dlnDensityAtValue() {
        Laplace.Diff diff = Laplace.dlnPdf(mu.getValue(), beta.getValue(), getValue());
        return null;
    }

    @Override
    public Double sample() {
        return Laplace.sample(mu.getValue(), beta.getValue(), random);
    }
}
