// JCP - Java Conformal Prediction framework
// Copyright (C) 2016  Anders Gidenstam
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
package se.hb.jcp.cp.measures;

import se.hb.jcp.cp.ConformalClassification;

/**
 * The S/Sum criterion is a prior efficiency measure based on the sum of the
 * p-values.
 * See [V. Vovk, V. Fedorova, I. Nouretdinov and A. Gammerman, "Criteria of
 * Efficiency for Conformal Prediction", COPA 2016, LNAI 9653, pp. 23-39, 2016]
 * for the definitions used here.
 *
 * @author anders.gidenstam(at)hb.se
 */
public class SumCriterion implements IPriorMeasure
{
    private static final String NAME = "Sum criterion";

    /**
     * Computes the Sum criterion measure for this prediction.
     * @param prediction the prediction.
     * @return the Sum criterion measure for this prediction. Small values are preferable.
     */
    @Override
    public double compute(ConformalClassification prediction)
    {
        double sum = 0.0;
        for (int i = 0; i < prediction.getPValues().size(); i++) {
            sum += prediction.getPValues().get(i);
        }
        return sum;
    }

    /**
     * Get the name of this measure.
     * @return the name of this measure.
     */
    @Override
    public String getName()
    {
        return NAME;
    }
}
