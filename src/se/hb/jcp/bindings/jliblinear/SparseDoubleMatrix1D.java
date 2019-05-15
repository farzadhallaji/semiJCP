// JCP - Java Conformal Prediction framework
// Copyright (C) 2015  Anders Gidenstam
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
// The public interface is based on cern.colt.matrix.DoubleMatrix1D.
package se.hb.jcp.bindings.jliblinear;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;

/**
 * Class for sparse 1-d matrices (aka <i>vectors</i>) holding
 * <tt>double</tt> elements in the sparse format expected by the Java
 * library liblinear. See the documentation for liblinear for more details.
 *
 * @author anders.gidenstam(at)hb.se
*/
// TODO: Make sure to adhere to colt's conventions.

public class SparseDoubleMatrix1D extends DoubleMatrix1D
{
    /**
     * Internal array of Feature nodes as the Java implementation of
     * liblinear expects.
     * NOTE: Internal Feature indices MUST start from 1.
     */
    Feature[] nodes;

    /**
     * Constructs a matrix with a copy of the given values.
     * The values are copied. So subsequent changes in <tt>values</tt>
     * are not reflected in the matrix, and vice-versa.
     *
     * @param values the values to be filled into the new matrix.
     */
    public SparseDoubleMatrix1D(double[] values)
    {
        setUp(values.length);
        // FIXME: The result is always a dense vector.
        nodes = new Feature[values.length];
        for (int i = 0; i < values.length; i++) {
            nodes[i] = new FeatureNode(i+1, values[i]);
        }
    }

    /**
     * Constructs a matrix with a copy of the given values.
     * The values are copied. So subsequent changes in <tt>values</tt>
     * are not reflected in the matrix, and vice-versa.
     * All other entries are initially <tt>0</tt>.
     *
     * @param columns  the number of columns the matrix shall have.
     * @param indices  the indices to be filled in the new matrix.
     * @param values   the values to be filled into the new matrix.
     */
    public SparseDoubleMatrix1D(int      columns,
                                int[]    indices,
                                double[] values)
    {
        setUp(columns);
        nodes = new Feature[indices.length];
        for (int i = 0; i < indices.length; i++) {
            nodes[i] = new FeatureNode(indices[i]+1, values[i]);
        }
    }

    /**
     * Constructs a matrix with a given number of columns.
     * All entries are initially <tt>0</tt>.
     * @param columns the number of columns the matrix shall have.
     * @throws IllegalArgumentException if
               <tt>columns&lt;0 || columns &gt; Integer.MAX_VALUE</tt>.
    */
    public SparseDoubleMatrix1D(int columns)
    {
        setUp(columns);
        nodes = new Feature[0];
    }

    SparseDoubleMatrix1D(int columns, Feature[] nodes)
    {
        setUp(columns);
        isNoView = false;
        this.nodes = nodes;
    }

    /**
     * Replaces all cell values of the receiver with the values of
     * another matrix.  Both matrices must have the same size.
     * If both matrices share the same cells (as is the case if they
     * are views derived from the same matrix) and intersect in an
     * ambiguous way, then replaces <i>as if</i> using an intermediate
     * auxiliary deep copy of <tt>other</tt>.
     *
     * @param     other   the source matrix to copy from (may be identical to the receiver).
     * @return <tt>this</tt> (for convenience only).
     * @throws      IllegalArgumentException if <tt>size() != other.size()</tt>.
     */
    public DoubleMatrix1D assign(DoubleMatrix1D other)
    {
        if (other==this) {
            return this;
        }
        checkSize(other);
        if (other instanceof SparseDoubleMatrix1D) {
            // FIXME: Should this be a deep copy?
            this.nodes = ((SparseDoubleMatrix1D)other).nodes;
            return this;
        } else {
            IntArrayList indexList = new IntArrayList();
            DoubleArrayList valueList = new DoubleArrayList();
            other.getNonZeros(indexList, valueList);
            nodes = new Feature[indexList.size()];
            for (int i = 0; i < indexList.size(); i++) {
                nodes[i] =
                    new FeatureNode(indexList.get(i)+1, valueList.get(i));
            }
            return this;
        }
    }

    /**
     * Construct and returns a new empty matrix <i>of the same dynamic
     * type</i> as the receiver, having the specified size.
     * For example, if the receiver is an instance of type
     * <tt>DenseDoubleMatrix1D</tt> the new matrix must also be of
     * type <tt>DenseDoubleMatrix1D</tt>, if the receiver is an
     * instance of type <tt>SparseDoubleMatrix1D</tt> the new matrix
     * must also be of type <tt>SparseDoubleMatrix1D</tt>, etc.  In
     * general, the new matrix should have internal parametrization as
     * similar as possible.
     *
     * @param size  the number of cell the matrix shall have.
     * @return  a new empty matrix of the same dynamic type.
     */
    public DoubleMatrix1D like(int size)
    {
        return new SparseDoubleMatrix1D(size);
    }

    /**
     * Construct and returns a new 2-d matrix <i>of the corresponding
     * dynamic type</i>, entirelly independent of the receiver.
     * For example, if the receiver is an instance of type
     * <tt>DenseDoubleMatrix1D</tt> the new matrix must be of type
     * <tt>DenseDoubleMatrix2D</tt>, if the receiver is an instance of
     * type <tt>SparseDoubleMatrix1D</tt> the new matrix must be of
     * type <tt>SparseDoubleMatrix2D</tt>, etc.
     *
     * @param rows the number of rows the matrix shall have.
     * @param columns the number of columns the matrix shall have.
     * @return  a new matrix of the corresponding dynamic type.
     */
    public DoubleMatrix2D like2D(int rows, int columns)
    {
        return new SparseDoubleMatrix2D(rows, columns);
    }

    /**
     * Returns the matrix cell value at coordinate <tt>column</tt>.
     *
     * <p>Provided with invalid parameters this method may return invalid
     * objects without throwing any exception.
     * <b>You should only use this method when you are absolutely sure that
     * the coordinate is within bounds.</b>
     * Precondition (unchecked): <tt>0 &lt;= column &lt; size()</tt>.
     *
     * @param column  the index of the column-coordinate.
     * @return the value at the specified coordinate.
     */
    public double getQuick(int column)
    {
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i].getIndex() == column+1) {
                return nodes[i].getValue();
            }
        }
        return 0.0;
    }

    /**
     * Sets the matrix cell at coordinate <tt>index</tt> to the
     * specified value.
     *
     * <p>Provided with invalid parameters this method may access
     * illegal indexes without throwing any exception.
     * <b>You should only use this method when you are absolutely sure that
     * the coordinate is within bounds.</b>
     * Precondition (unchecked): <tt>0 &lt;= column &lt; size()</tt>.
     *
     * @param index  the index of the cell.
     * @param value  the value to be filled into the specified cell.
     */
    public void setQuick(int index, double value)
    {
        int i;
        for (i = 0; i < nodes.length; i++) {
            if (nodes[i].getIndex() == index+1) {
                nodes[i].setValue(value);
                return;
            }
        }
        if (value != 0.0) {
            Feature[] old = nodes;
            nodes = new FeatureNode[old.length + 1];
            for (i = 0; i < old.length && old[i].getIndex() < index+1; i++) {
                nodes[i] = old[i];
            }
            nodes[i] = new FeatureNode(index+1, value);
            for (; i < old.length; i++) {
                nodes[i + 1] = old[i];
            }
        }
    }

    /**
     * Construct and returns a new selection view.
     *
     * @param offsets  the offsets of the visible elements.
     * @return a new view.
     */
    protected DoubleMatrix1D viewSelectionLike(int[] offsets)
    {
        // FIXME: If needed.
        throw new UnsupportedOperationException("Not implemented");
    }
}
