package io.improbable.keanu.distributions.continuous;

import io.improbable.keanu.tensor.Tensor;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import org.nd4j.linalg.util.ArrayUtil;

public class Laplace {

    private Laplace() {
    }

    /**
     * @param shape  shape of tensor returned
     * @param mu     location
     * @param beta   shape
     * @param random source of randomness
     * @return a random number from the Laplace distribution
     */
    public static DoubleTensor sample(int[] shape, DoubleTensor mu, DoubleTensor beta, KeanuRandom random) {
        Tensor.FlattenedView<Double> muWrapped = mu.getFlattenedView();
        Tensor.FlattenedView<Double> betaWrapped = beta.getFlattenedView();

        int length = ArrayUtil.prod(shape);
        double[] samples = new double[length];
        for (int i = 0; i < length; i++) {
            samples[i] = sample(muWrapped.getOrScalar(i), betaWrapped.getOrScalar(i), random);
        }

        return DoubleTensor.create(samples, shape);
    }

    private static double sample(double mu, double beta, KeanuRandom random) {
        if (beta <= 0.0) {
            throw new IllegalArgumentException("Invalid value for beta: " + beta);
        }
        if (random.nextDouble() > 0.5) {
            return mu + beta * Math.log(random.nextDouble());
        } else {
            return mu - beta * Math.log(random.nextDouble());
        }
    }

    public static DoubleTensor logPdf(DoubleTensor mu, DoubleTensor beta, DoubleTensor x) {
        final DoubleTensor muMinusXAbsNegDivBeta = mu.minus(x).abs().divInPlace(beta);
        final DoubleTensor logTwoBeta = beta.times(2).logInPlace();
        return muMinusXAbsNegDivBeta.plusInPlace(logTwoBeta).unaryMinus();
    }

    public static DiffLogP dlnPdf(DoubleTensor mu, DoubleTensor beta, DoubleTensor x) {
        final DoubleTensor muMinusX = mu.minus(x);
        final DoubleTensor muMinusXAbs = muMinusX.abs();

        final DoubleTensor denominator = muMinusXAbs.times(beta);

        final DoubleTensor dLogPdx = muMinusX.divInPlace(denominator);
        final DoubleTensor dLogPdMu = x.minus(mu).divInPlace(denominator);
        final DoubleTensor dLogPdBeta = muMinusXAbs.minusInPlace(beta).divInPlace(beta.pow(2));

        return new DiffLogP(dLogPdMu, dLogPdBeta, dLogPdx);
    }

    public static class DiffLogP {
        public final DoubleTensor dLogPdmu;
        public final DoubleTensor dLogPdbeta;
        public final DoubleTensor dLogPdx;

        public DiffLogP(DoubleTensor dLogPdmu, DoubleTensor dLogPdbeta, DoubleTensor dLogPdx) {
            this.dLogPdmu = dLogPdmu;
            this.dLogPdbeta = dLogPdbeta;
            this.dLogPdx = dLogPdx;
        }
    }

}
