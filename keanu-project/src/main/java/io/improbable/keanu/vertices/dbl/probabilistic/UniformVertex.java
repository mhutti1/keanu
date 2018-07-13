package io.improbable.keanu.vertices.dbl.probabilistic;

import static java.util.Collections.singletonMap;

import static io.improbable.keanu.tensor.TensorShapeValidation.checkHasSingleNonScalarShapeOrAllScalar;
import static io.improbable.keanu.tensor.TensorShapeValidation.checkTensorsMatchNonScalarShapeOrAreScalar;

import java.util.Map;

import io.improbable.keanu.distributions.continuous.Uniform;
import io.improbable.keanu.tensor.dbl.DoubleTensor;
import io.improbable.keanu.vertices.Observable;
import io.improbable.keanu.vertices.Probabilistic;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.KeanuRandom;
import io.improbable.keanu.vertices.dbl.nonprobabilistic.ConstantDoubleVertex;
import io.improbable.keanu.vertices.update.ProbabilisticValueUpdater;

public class UniformVertex extends DoubleVertex implements Probabilistic<DoubleTensor> {

    private final DoubleVertex xMin;
    private final DoubleVertex xMax;

    /**
     * One xMin or xMax or both that match a proposed tensor shape of Uniform Vertex
     *
     * If all provided parameters are scalar then the proposed shape determines the shape
     *
     * @param tensorShape desired tensor shape
     * @param xMin  the inclusive lower bound of the Uniform with either the same shape as specified for this vertex or a scalar
     * @param xMax  the exclusive upper bound of the Uniform with either the same shape as specified for this vertex or a scalar
     */
    public UniformVertex(int[] tensorShape, DoubleVertex xMin, DoubleVertex xMax) {
        super(new ProbabilisticValueUpdater<>(), Observable.observableTypeFor(UniformVertex.class));

        checkTensorsMatchNonScalarShapeOrAreScalar(tensorShape, xMin.getShape(), xMax.getShape());

        this.xMin = xMin;
        this.xMax = xMax;
        setParents(xMin, xMax);
        setValue(DoubleTensor.placeHolder(tensorShape));
    }

    /**
     * One to one constructor for mapping some shape of mu and sigma to
     * a matching shaped Uniform Vertex
     *
     * @param xMin  the inclusive lower bound of the Uniform with either the same shape as specified for this vertex or a scalar
     * @param xMax  the exclusive upper bound of the Uniform with either the same shape as specified for this vertex or a scalar
     */
    public UniformVertex(DoubleVertex xMin, DoubleVertex xMax) {
        this(checkHasSingleNonScalarShapeOrAllScalar(xMin.getShape(), xMax.getShape()), xMin, xMax);
    }

    public UniformVertex(DoubleVertex xMin, double xMax) {
        this(xMin, new ConstantDoubleVertex(xMax));
    }

    public UniformVertex(double xMin, DoubleVertex xMax) {
        this(new ConstantDoubleVertex(xMin), xMax);
    }

    public UniformVertex(double xMin, double xMax) {
        this(new ConstantDoubleVertex(xMin), new ConstantDoubleVertex(xMax));
    }

    public UniformVertex(int[] tensorShape, DoubleVertex xMin, double xMax) {
        this(tensorShape, xMin, new ConstantDoubleVertex(xMax));
    }

    public UniformVertex(int[] tensorShape, double xMin, DoubleVertex xMax) {
        this(tensorShape, new ConstantDoubleVertex(xMin), xMax);
    }

    public UniformVertex(int[] tensorShape, double xMin, double xMax) {
        this(tensorShape, new ConstantDoubleVertex(xMin), new ConstantDoubleVertex(xMax));
    }

    public DoubleVertex getXMin() {
        return xMin;
    }

    public DoubleVertex getXMax() {
        return xMax;
    }

    @Override
    public double logProb(DoubleTensor value) {
        return Uniform.withParameters(xMin.getValue(), xMax.getValue()).logProb(value).sum();
    }

    @Override
    public Map<Long, DoubleTensor> dLogProb(DoubleTensor value) {

        DoubleTensor dlogPdf = DoubleTensor.zeros(this.xMax.getShape());
        dlogPdf = dlogPdf.setWithMaskInPlace(value.getGreaterThanMask(xMax.getValue()), Double.NEGATIVE_INFINITY);
        dlogPdf = dlogPdf.setWithMaskInPlace(value.getLessThanOrEqualToMask(xMin.getValue()), Double.POSITIVE_INFINITY);

        return singletonMap(getId(), dlogPdf);
    }

    @Override
    public DoubleTensor sample(KeanuRandom random) {
        return Uniform.withParameters(xMin.getValue(), xMax.getValue()).sample(getShape(), random);
    }

}
