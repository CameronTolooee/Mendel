package mendel.vptree;

import mendel.serialize.ByteSerializable;

/**
 * An interface that defines a single point in a vp-vptree. All points must have a
 * distance function that satisfies metric space.
 *
 * @author ctolooee
 */
public interface VPPoint extends Comparable<VPPoint>, ByteSerializable{

    public double getDistanceTo(VPPoint otherPoint);

}