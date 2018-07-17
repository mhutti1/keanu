package io.improbable.keanu.distributions.continuous;

import static io.improbable.keanu.distributions.dual.Duals.MU;
import static io.improbable.keanu.distributions.dual.Duals.SIGMA;
import static io.improbable.keanu.distributions.dual.Duals.X;

import io.improbable.keanu.distributions.ContinuousDistribution;
import io.improbable.keanu.distributions.dual.Duals;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.dbl.KeanuRandom;

public class Gaussian implements ContinuousDistribution {

    public static final double SQRT_2PI = Math.sqrt(Math.PI * 2);
    public static final double LN_SQRT_2PI = Math.log(SQRT_2PI);
    private final DoubleTensor mu;
    private final DoubleTensor sigma;

    public static ContinuousDistribution withParameters(DoubleTensor mu, DoubleTensor sigma) {
        return new Gaussian(mu, sigma);
    }

    private Gaussian(DoubleTensor mu, DoubleTensor sigma) {
        this.mu = mu;
        this.sigma = sigma;
    }

    @Override
    public DoubleTensor sample(int[] shape, KeanuRandom random) {
        DoubleTensor unityGaussian = random.nextGaussian(shape);
        return unityGaussian.timesInPlace(sigma).plusInPlace(mu);
    }

    @Override
    public DoubleTensor logProb(DoubleTensor x) {
        final DoubleTensor lnSigma = sigma.log();
        final DoubleTensor xMinusMuSquared = x.minus(mu).powInPlace(2);
        final DoubleTensor xMinusMuSquaredOver2Variance = xMinusMuSquared.divInPlace(sigma.pow(2).timesInPlace(2.0));
        return xMinusMuSquaredOver2Variance.plusInPlace(lnSigma).plusInPlace(LN_SQRT_2PI).unaryMinusInPlace();
    }

    @Override
    public Duals dLogProb(DoubleTensor x) {
        final DoubleTensor variance = sigma.pow(2);
        final DoubleTensor xMinusMu = x.minus(mu);

        final DoubleTensor dlnP_dmu = xMinusMu.div(variance);
        final DoubleTensor dlnP_dx = dlnP_dmu.unaryMinus();
        final DoubleTensor dlnP_dsigma = xMinusMu.powInPlace(2)
            .divInPlace(variance.timesInPlace(sigma))
            .minusInPlace(sigma.reciprocal());

        return new Duals()
            .put(MU, dlnP_dmu)
            .put(SIGMA, dlnP_dsigma)
            .put(X, dlnP_dx);
    }
}
