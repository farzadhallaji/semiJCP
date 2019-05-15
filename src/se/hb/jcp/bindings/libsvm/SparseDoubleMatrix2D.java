// JCP - Java Conformal Prediction framework
// Copyright (C) 2014 - 2016, 2018  Anders Gidenstam
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
// The public interface is based on cern.colt.matrix.DoubleMatrix2D.
package se.hb.jcp.bindings.libsvm;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Class for sparse 2-d matrices holding <tt>double</tt> elements in
 * the sparse format expected by the C library libsvm. See the
 * documentation for libsvm for more details.
 *
 * @author anders.gidenstam(at)hb.se
*/
// TODO: Make sure to adhere to colt's conventions.

public class SparseDoubleMatrix2D extends DoubleMatrix2D
{
    /**
     * C-side pointer to an array of svm_node arrays storing the matrix
     * contents.
     */
    protected long Cptr;
    /**
     * SparseDoubleMatrix1D views of rows in this matrix.
     * These are created and stored on demand.
     */
    protected SparseDoubleMatrix1D[] rowViews;

    /**
     * Constructs a matrix with a given number of rows and columns.
     * All entries are initially <tt>0</tt>.
     * @param rows the number of rows the matrix shall have.
     * @param columns the number of columns the matrix shall have.
     * @throws IllegalArgumentException if
               <tt>rows&lt;0 || columns&lt;0 || (double)columns*rows &gt;
               Integer.MAX_VALUE</tt>.
    */
    public SparseDoubleMatrix2D(int rows, int columns)
    {
        setUp(rows, columns);
        Cptr = native_matrix_create(rows, columns);
    }

    /**
     * Construct and returns a new empty matrix <i>of the same dynamic type</i>
     * as the receiver, having the specified number of rows and columns.
     * For example, if the receiver is an instance of type
     * <tt>SparseDoubleMatrix2D</tt> the new matrix must also be of type
     * <tt>DenseDoubleMatrix2D</tt>, if the receiver is an instance of type
     * <tt>SparseDoubleMatrix2D</tt> the new matrix must also be of type
     * <tt>SparseDoubleMatrix2D</tt>, etc.
     *
     * In general, the new matrix should have internal parametrization as
     * similar as possible.
     *
     * @param rows the number of rows the matrix shall have.
     * @param columns the number of columns the matrix shall have.
     * @return  a new empty matrix of the same dynamic type.
     */
    public DoubleMatrix2D like(int rows, int columns)
    {
        return new SparseDoubleMatrix2D(rows, columns);
    }

    /**
     * Construct and returns a new 1-d matrix <i>of the corresponding dynamic
     * type</i>, entirelly independent of the receiver.
     * For example, if the receiver is an instance of type
     * <tt>DenseDoubleMatrix2D</tt> the new matrix must be of type
     * <tt>DenseDoubleMatrix1D</tt>, if the receiver is an instance of type
     * <tt>SparseDoubleMatrix2D</tt> the new matrix must be of type
     * <tt>SparseDoubleMatrix1D</tt>, etc.
     *
     * @param  size the number of cells the matrix shall have.
     * @return  a new matrix of the corresponding dynamic type.
     */
    public DoubleMatrix1D like1D(int size)
    {
        return new SparseDoubleMatrix1D(size);
    }

    /**
     * Construct and returns a new 1-d matrix <i>of the corresponding dynamic
     * type</i>, sharing the same cells.
     * For example, if the receiver is an instance of type
     * <tt>DenseDoubleMatrix2D</tt> the new matrix must be of type
     * <tt>DenseDoubleMatrix1D</tt>, if the receiver is an instance of type
     * <tt>SparseDoubleMatrix2D</tt> the new matrix must be of type
     * <tt>SparseDoubleMatrix1D</tt>, etc.
     *
     * @param size the number of cells the matrix shall have.
     * @param zero the index of the first element.
     * @param stride the number of indexes between any two elements, i.e.
     *        <tt>index(i+1)-index(i)</tt>.
     * @return  a new matrix of the corresponding dynamic type.
     */
    protected DoubleMatrix1D like1D(int size, int zero, int stride)
    {
        // FIXME: If needed.
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Returns the matrix cell value at coordinate <tt>[row,column]</tt>.
     *
     * <p>Provided with invalid parameters this method may return invalid
     * objects without throwing any exception.
     * <b>You should only use this method when you are absolutely sure that
     * the coordinate is within bounds.</b>
     * Precondition (unchecked): <tt>0 &lt;= column &lt; columns() &amp;&amp;
     * 0 &lt;= row &lt; rows()</tt>.
     *
     * @param row     the index of the row-coordinate.
     * @param column  the index of the column-coordinate.
     * @return the value at the specified coordinate.
     */
    public double getQuick(int row, int column)
    {
        return native_matrix_get(Cptr, row, column);
    }

    /**
     * Sets the matrix cell at coordinate <tt>[row,column]</tt> to the
     * specified value.
     *
     * <p>Provided with invalid parameters this method may access illegal
     * indexes without throwing any exception.
     * <b>You should only use this method when you are absolutely sure that
     * the coordinate is within bounds.</b>
     * Precondition (unchecked): <tt>0 &lt;= column &lt; columns() &amp;&amp;
     *                               0 &lt;= row &lt; rows()</tt>.
     *
     * @param row     the index of the row-coordinate.
     * @param column  the index of the column-coordinate.
     * @param value   the value to be filled into the specified cell.
     */
    public void setQuick(int row, int column, double value)
    {
        native_matrix_set(Cptr, row, column, value);
    }

    /**
     * Returns one row of the matrix as a SparseDoubleMatrix1D.
     * The resulting matrix will share storage with this instance.
     *
     * @param row     the row to return.
     * @return a <tt>SparseDoubleMatrix1D</tt> representing a view of the row.
     */
    public SparseDoubleMatrix1D getRow(int row)
    {
        checkRow(row);
        if (rowViews[row] == null) {
            // NOTE: This is a view, it should not increase the RC as its
            //       content is shared with the matrix. The parent pointer
            //       keeps the matrix alive.
            rowViews[row] =
                new SparseDoubleMatrix1D(columns,
                                         this,
                                         native_matrix_get_row(Cptr, row));
        }
        return rowViews[row];
    }

    /**
       Constructs and returns a new <i>slice view</i> representing the
       columns of the given row.
       The returned view is backed by this matrix, so changes in the
       returned view are reflected in this matrix, and vice-versa.
       To obtain a slice view on subranges, construct a sub-ranging
       view (<tt>viewPart( ...)</tt>), then apply this method to the
       sub-range view.

       <p> <b>Example:</b> <table border="0"> <tr nowrap>
       <td valign="top">2 x 3 matrix: <br> 1, 2, 3<br> 4, 5, 6</td>
       <td>viewRow(0) ==&gt;</td>
       <td valign="top">Matrix1D of size 3:<br> 1, 2, 3</td> </tr> </table>

       @param row the row to fix.
       @return a new slice view.
       @throws IndexOutOfBoundsException if <tt>row < 0 || row >= rows()</tt>.
       @see #viewColumn(int)
    */
    public DoubleMatrix1D viewRow(int row)
    {
        return getRow(row);
    }

    /**
     * Replaces one row of the matrix.
     *
     * FIXME: Verify the RC handling - is the old row RC decreased enough?
     *        Should the SparseDoubleMatrix1D row object be replaced instead
     *        of updated, i.e. should old row views see the new state?
     *
     * @param row      the row to replace.
     * @param indices  the indices to be filled in the new row.
     * @param values   the values to be filled into the new row.
     */
    public void setRow(int      row,
                       int[]    indices,
                       double[] values)
    {
        // FIXME: Verify row index and indices.
        long newRow = native_matrix_set_row(Cptr, row, indices, values);
        if (rowViews[row] != null) {
            rowViews[row].Cptr = newRow;
        }
    }

    /**
     * Construct and returns a new selection view.
     *
     * @param rowOffsets the offsets of the visible elements.
     * @param columnOffsets the offsets of the visible elements.
     * @return a new view.
     */
    protected DoubleMatrix2D viewSelectionLike(int[] rowOffsets,
                                               int[] columnOffsets)
    {
        // FIXME: If needed.
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Sets up a matrix with a given number of rows and columns.
     * @param rows the number of rows the matrix shall have.
     * @param columns the number of columns the matrix shall have.
     * @throws IllegalArgumentException if <tt>rows&lt;0 || columns&lt;0 ||
     *         (double)columns*rows &gt; Integer.MAX_VALUE</tt>.
     */
    protected void setUp(int rows, int columns)
    {
        super.setUp(rows,columns);
        rowViews = new SparseDoubleMatrix1D[rows];
    }

    protected void finalize() throws Throwable
    {
        if (Cptr != 0 && isNoView) {
            native_matrix_free(Cptr, rows);
            Cptr = 0;
        }
    }

    private void writeObject(ObjectOutputStream oos)
        throws java.io.IOException
    {
        // FIXME. If needed.
        throw new UnsupportedOperationException("Not implemented");
    }

    private void readObject(ObjectInputStream ois)
        throws ClassNotFoundException, java.io.IOException
    {
        // FIXME. If needed.
        throw new UnsupportedOperationException("Not implemented");
    }

    // Internal native functions.
    private static native long native_matrix_create(int rows, int columns);
    private static native void native_matrix_free(long ptr, int rows);
    private static native double native_matrix_get(long ptr,
                                                   int row, int column);
    private static native void native_matrix_set(long ptr,
                                                 int row, int column,
                                                 double value);
    private static native long native_matrix_get_row(long ptr,
                                                     int row);
    private static native long native_matrix_set_row(long ptr,
                                                     int row,
                                                     int[] columns,
                                                     double[] values);

    static {
        // FIXME: It would have been better not to repeat this here and
        //        keep it only in the svm class.
        try {
            System.loadLibrary("svm");
        } catch (UnsatisfiedLinkError e) {
            System.out.println
                ("Could not load libsvm.");
            System.exit(1);
        }
        try {
            System.loadLibrary("svm-jni");
        } catch (UnsatisfiedLinkError e) {
            System.out.println
                ("Could not load native JNI wrapper code for libsvm.");
            System.exit(1);
        }
    }
}
