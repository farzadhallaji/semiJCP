// JCP - Java Conformal Prediction framework
// Copyright (C) 2015 - 2016  Anders Gidenstam
//
// This library is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
package se.hb.jcp.nc;

import se.hb.jcp.ml.IClassifier;
import se.hb.jcp.ml.IClassProbabilityClassifier;
import se.hb.jcp.ml.ISVMClassifier;

/**
 * Singleton factory for JCP classification nonconformity functions.
 *
 * @author anders.gidenstam(at)hb.se
 */

public final class ClassificationNonconformityFunctionFactory
{
    private static final
        ClassificationNonconformityFunctionFactory _theInstance =
            new ClassificationNonconformityFunctionFactory();
    private static final String[] _ncfNames =
        {
            "hinge loss nonconformity function",
            "SVM distance nonconformity function",
            "attribute average nonconformity function"
        };

    private ClassificationNonconformityFunctionFactory()
    {
    }

    public String[] getNonconformityFunctions()
    {
        return _ncfNames;
    }

    public IClassificationNonconformityFunction
        createNonconformityFunction(int type,
                                    double[] classes,
                                    IClassifier classifier)
    {
        switch (type) {
        case 0:
            if (!(classifier instanceof IClassProbabilityClassifier)) {
                classifier = new se.hb.jcp.ml.BogusClassProbabilityClassifier
                                     (classifier, classes);
            }
            return new HingeLossNonconformityFunction
                           (classes,
                            (IClassProbabilityClassifier)classifier);
        case 1:
            if (classifier instanceof ISVMClassifier) {
                return new SVMDistanceNonconformityFunction
                               (classes,
                                (ISVMClassifier)classifier);
            } else {
                throw new UnsupportedOperationException
                    ("The selected classifier does not implement the " +
                     "ISVMClassifier interface and, thus, cannot be used " +
                     "with the " + _ncfNames[1] + ".");
            }
        case 2:
            return new AverageClassificationNonconformityFunction(classes);
        default:
            throw new UnsupportedOperationException
                ("Unknown nonconformity function type.");
        }
    }

    public static ClassificationNonconformityFunctionFactory getInstance()
    {
        return _theInstance;
    }
}
